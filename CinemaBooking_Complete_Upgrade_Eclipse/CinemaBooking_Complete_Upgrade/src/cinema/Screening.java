package cinema;

import java.io.Serializable;

/** One movie showing. Seat reservations are independent for every screening. */
public final class Screening implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String id;
    public final String movieId;
    public String showtime;
    public String screen;
    public String status;

    public Screening(String id, String movieId, String showtime, String screen, String status) {
        this.id = id;
        this.movieId = movieId;
        this.showtime = showtime;
        this.screen = screen;
        this.status = status;
    }

    public boolean isOpen() {
        return "Open/Available".equals(status);
    }

    @Override
    public String toString() {
        return showtime + "  |  " + screen + "  |  " + status;
    }
}
