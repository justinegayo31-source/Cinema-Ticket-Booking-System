package cinema;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** A paid booking. Partial cancellations retain their history. */
public final class BookingRecord implements Serializable {
    private static final long serialVersionUID = 2L;

    public final String id, customer, movieId, movieTitle, showtime, screen, seats, date;
    public final int regular, premium;
    public final double subtotal, cash;
    public boolean cancelled;
    // Fields added after the original project; old saved records load with null defaults.
    public String screeningId;
    public Map<String, Double> seatPrices;
    public Set<String> cancelledSeatCodes;

    public BookingRecord(String id, String customer, Movie movie, String seats,
                         int regular, int premium, double subtotal, double cash) {
        this(id, customer, movie, null, seats, regular, premium, subtotal, cash);
    }

    public BookingRecord(String id, String customer, Movie movie, Screening screening,
                         String seats, int regular, int premium, double subtotal, double cash) {
        this.id = id;
        this.customer = customer;
        this.movieId = movie.id;
        this.movieTitle = movie.title;
        this.showtime = screening == null ? movie.showtime : screening.showtime;
        this.screen = screening == null ? movie.screen : screening.screen;
        this.screeningId = screening == null ? null : screening.id;
        this.seats = seats;
        this.regular = regular;
        this.premium = premium;
        this.subtotal = subtotal;
        this.cash = cash;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.seatPrices = new LinkedHashMap<>();
        this.cancelledSeatCodes = new LinkedHashSet<>();
        for (String code : seatCodes()) {
            seatPrices.put(code, code.charAt(0) >= 'D' ? movie.premiumPrice : movie.regularPrice);
        }
    }

    public List<String> seatCodes() {
        if (seats == null || seats.isBlank()) return Collections.emptyList();
        return Arrays.asList(seats.split(",\\s*"));
    }

    public List<String> activeSeats() {
        if (cancelled) return Collections.emptyList();
        List<String> active = new ArrayList<>();
        for (String code : seatCodes()) {
            if (cancelledSeatCodes == null || !cancelledSeatCodes.contains(code)) active.add(code);
        }
        return active;
    }

    public int quantity() { return regular + premium; }
    public int activeQuantity() { return activeSeats().size(); }

    public double activeTotal() {
        if (cancelled) return 0;
        if (seatPrices == null || seatPrices.isEmpty()) {
            return subtotal * activeQuantity() / Math.max(1, quantity());
        }
        double total = 0;
        for (String code : activeSeats()) total += seatPrices.getOrDefault(code, 0.0);
        return total;
    }

    public double cancelSeats(Collection<String> codes) {
        if (cancelledSeatCodes == null) cancelledSeatCodes = new LinkedHashSet<>();
        double before = activeTotal();
        for (String code : codes) {
            if (activeSeats().contains(code)) cancelledSeatCodes.add(code);
        }
        if (activeQuantity() == 0) cancelled = true;
        return before - activeTotal();
    }

    public String status() {
        if (cancelled || activeQuantity() == 0) return "Cancelled";
        return activeQuantity() < quantity() ? "Partially cancelled" : "Confirmed";
    }
}
