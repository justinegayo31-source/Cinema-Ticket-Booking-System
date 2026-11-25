package cinema;

import java.nio.file.*;
import java.util.*;

/** Run this optional non-GUI test with Run As > Java Application. */
public final class ProjectSelfTest {
    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS " + label);
    }

    public static void main(String[] args) throws Exception {
        Path temporaryHome = Files.createTempDirectory("cinema-selftest-");
        System.setProperty("user.home", temporaryHome.toString());
        DataStore.load();
        check(DataStore.movies.size() >= 3, "Three sample movies");
        Movie movie = DataStore.movies.get(0);
        Screening first = DataStore.schedules(movie).get(0);
        Screening later = new Screening(DataStore.newScreeningId(movie), movie.id,
                "9:00 PM", "Screen 1", "Open/Available");
        DataStore.screenings.add(later);
        BookingRecord record = new BookingRecord(DataStore.bookingId(), "Test Customer", movie,
                first, "A1, A2, D1", 2, 1, 2 * movie.regularPrice + movie.premiumPrice, 1500);
        DataStore.bookings.add(record);
        check(DataStore.booked(first).contains("A1"), "Booking reserves first showtime");
        check(!DataStore.booked(later).contains("A1"), "Separate showtime remains available");
        double refunded = record.cancelSeats(Collections.singleton("A1"));
        check(Math.abs(refunded - movie.regularPrice) < .01, "Individual seat cancellation amount");
        check(!DataStore.booked(first).contains("A1"), "Cancelled seat released");
        check(DataStore.booked(first).contains("A2") && DataStore.booked(first).contains("D1"),
                "Other reserved seats remain booked");
        check(DataStore.tickets() == 2, "Ticket total after partial cancellation");
        DataStore.save();
        DataStore.load();
        check(DataStore.bookings.size() == 1, "Booking persists after reload");
        check(DataStore.screenings.size() == 4, "New showtime persists after reload");
        Path zip = temporaryHome.resolve("backup.zip");
        DataStore.backup(zip);
        check(Files.size(zip) > 0, "Backup generated");
        DataStore.bookings.clear(); DataStore.save();
        DataStore.restore(zip);
        check(DataStore.bookings.size() == 1, "Backup restore recovers bookings");
        System.out.println("ALL SELF-TESTS PASSED");
    }
}
