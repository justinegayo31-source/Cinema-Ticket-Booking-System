package cinema;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Local persistence and authoritative seat availability. */
public final class DataStore {
    public static final List<Movie> movies = new ArrayList<>();
    public static final List<BookingRecord> bookings = new ArrayList<>();
    public static final List<Screening> screenings = new ArrayList<>();

    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".cinema-booking-system");
    private static final Path FILE = DIR.resolve("records.dat");
    private static int nextId = 1001;
    private DataStore() {}

    // Keep the original serialized type and UID for compatibility with older saved data.
    private static class Saved implements Serializable {
        private static final long serialVersionUID = 2L;
        List<Movie> movies;
        List<BookingRecord> bookings;
        List<Screening> screenings;
        int nextId;
        Saved() {
            movies = new ArrayList<>(DataStore.movies);
            bookings = new ArrayList<>(DataStore.bookings);
            screenings = new ArrayList<>(DataStore.screenings);
            nextId = DataStore.nextId;
        }
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(FILE))) {
                Saved state = (Saved) in.readObject();
                movies.clear(); movies.addAll(state.movies);
                bookings.clear(); bookings.addAll(state.bookings);
                screenings.clear();
                if (state.screenings != null) screenings.addAll(state.screenings);
                nextId = Math.max(1001, state.nextId);
                if (screenings.isEmpty()) migrateOldShowtimes();
                return;
            } catch (Exception ex) {
                System.err.println("Could not read existing data: " + ex.getMessage());
                // Do not silently overwrite corrupt/unreadable user data.
                throw new IllegalStateException("Data could not be opened. Back up " + FILE, ex);
            }
        }
        movies.clear(); bookings.clear(); screenings.clear(); nextId = 1001;
        movies.add(new Movie("MOV-01", "Dune: Part Two", "Sci-Fi", "1:00 PM", "Screen 1", "Open/Available", 300, 420,
                "/cinema/assets/dune_poster.png", "/cinema/assets/dune_banner.png"));
        movies.add(new Movie("MOV-02", "Inside Out 2", "Animation", "3:30 PM", "Screen 2", "Open/Available", 280, 390,
                "/cinema/assets/inside_poster.png", "/cinema/assets/inside_banner.png"));
        movies.add(new Movie("MOV-03", "A Quiet Place: Day One", "Horror", "6:00 PM", "Screen 3", "Open/Available", 310, 430,
                "/cinema/assets/quiet_poster.png", "/cinema/assets/quiet_banner.png"));
        migrateOldShowtimes();
        save();
    }

    private static void migrateOldShowtimes() {
        for (Movie movie : movies) {
            screenings.add(new Screening(movie.id + "-S1", movie.id, movie.showtime, movie.screen, movie.status));
        }
        for (BookingRecord record : bookings) {
            if (record.screeningId == null) {
                Screening found = screenings.stream().filter(s -> s.movieId.equals(record.movieId)
                        && s.showtime.equals(record.showtime) && s.screen.equals(record.screen)).findFirst().orElse(null);
                if (found != null) record.screeningId = found.id;
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(DIR);
            Path temp = DIR.resolve("records.tmp");
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temp))) {
                out.writeObject(new Saved());
            }
            Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save cinema records: " + ex.getMessage(), ex);
        }
    }

    public static Movie findMovie(String id) {
        for (Movie movie : movies) if (movie.id.equals(id)) return movie;
        return null;
    }
    public static List<Screening> schedules(Movie movie) {
        List<Screening> list = new ArrayList<>();
        if (movie != null) for (Screening screening : screenings)
            if (screening.movieId.equals(movie.id)) list.add(screening);
        return list;
    }
    public static boolean duplicate(String id) {
        return findMovie(id) != null;
    }
    public static String newScreeningId(Movie movie) {
        String id;
        do { id = movie.id + "-" + UUID.randomUUID().toString().substring(0, 8); }
        while (screeningIdExists(id));
        return id;
    }
    private static boolean screeningIdExists(String id) {
        for (Screening screening : screenings) if (screening.id.equals(id)) return true;
        return false;
    }
    public static String bookingId() { return "BKG-" + (nextId++); }

    public static Set<String> booked(Screening screening) {
        Set<String> result = new HashSet<>();
        if (screening == null) return result;
        for (BookingRecord record : bookings) {
            if (!record.cancelled && screening.id.equals(record.screeningId)) result.addAll(record.activeSeats());
        }
        return result;
    }
    public static Set<String> booked(Movie movie) {
        List<Screening> list = schedules(movie);
        return list.isEmpty() ? Collections.emptySet() : booked(list.get(0));
    }
    public static int sold(Screening screening) { return booked(screening).size(); }
    public static int sold(Movie movie) {
        int count = 0;
        for (Screening screening : schedules(movie)) count += sold(screening);
        return count;
    }
    public static double revenue() {
        double sum = 0;
        for (BookingRecord record : bookings) sum += record.activeTotal();
        return sum;
    }
    public static int tickets() {
        int count = 0;
        for (BookingRecord record : bookings) count += record.activeQuantity();
        return count;
    }
    public static Path storageFolder() { return DIR; }

    public static String importImage(String sourcePath) throws IOException {
        Path source = Paths.get(sourcePath);
        String lower = source.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!Files.isRegularFile(source) || !(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")))
            throw new IOException("Choose an existing PNG/JPG image.");
        Files.createDirectories(DIR.resolve("posters"));
        String extension = lower.substring(lower.lastIndexOf('.'));
        Path target = DIR.resolve("posters").resolve(UUID.randomUUID() + extension);
        Files.copy(source, target);
        return "posters/" + target.getFileName();
    }

    /** Backup/restore only the application's own stored files. */
    public static void backup(Path destination) throws IOException {
        save();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destination))) {
            try (java.util.stream.Stream<Path> files = Files.walk(DIR)) {
                for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                    if (file.getFileName().toString().equals("records.tmp")) continue;
                    zip.putNextEntry(new ZipEntry(DIR.relativize(file).toString().replace('\\', '/')));
                    Files.copy(file, zip);
                    zip.closeEntry();
                }
            }
        }
    }
    public static void restore(Path source) throws IOException {
        // Inspect backup before touching current data; never extract outside app directory.
        Path staged = Files.createTempDirectory("cinema-restore-");
        try {
            boolean hasRecords = false;
            try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(source))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    Path relative = Paths.get(entry.getName()).normalize();
                    if (relative.isAbsolute() || relative.startsWith("..")) throw new IOException("Unsafe backup entry.");
                    if (!entry.getName().equals("records.dat") && !entry.getName().startsWith("posters/"))
                        throw new IOException("Unexpected file in backup: " + entry.getName());
                    Path target = staged.resolve(relative).normalize();
                    Files.createDirectories(target.getParent());
                    if (!entry.isDirectory()) {
                        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                        if (entry.getName().equals("records.dat")) hasRecords = true;
                    }
                    zip.closeEntry();
                }
            }
            if (!hasRecords) throw new IOException("No records.dat in backup.");
            Files.createDirectories(DIR);
            // Save a recovery copy of current records before replacement.
            if (Files.exists(FILE)) Files.copy(FILE, DIR.resolve("records-before-restore.dat"), StandardCopyOption.REPLACE_EXISTING);
            try (java.util.stream.Stream<Path> files = Files.walk(staged)) {
                for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                    Path target = DIR.resolve(staged.relativize(file));
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            load();
        } finally {
            try (java.util.stream.Stream<Path> files = Files.walk(staged)) {
                files.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
        }
    }
}
