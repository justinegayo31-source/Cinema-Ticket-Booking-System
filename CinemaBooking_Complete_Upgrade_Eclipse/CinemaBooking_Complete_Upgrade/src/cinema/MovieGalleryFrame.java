package cinema;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Poster and banner gallery with movie screening availability. */
public final class MovieGalleryFrame extends BaseFrame {
    public MovieGalleryFrame() {
        super("Movie gallery | Cinema Booking", 1100, 770);
        setLayout(new BorderLayout());
        add(UI.header("NOW SHOWING", "Posters, banners and available showtimes"), BorderLayout.NORTH);
        JPanel gallery = new JPanel(new GridLayout(0, 2, 15, 15));
        gallery.setBackground(UI.BG);
        gallery.setBorder(new EmptyBorder(18, 20, 18, 20));
        for (Movie movie : DataStore.movies) gallery.add(card(movie));
        JScrollPane scroll = new JScrollPane(gallery);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
        add(bottom("Back to dashboard", () -> go(new DashboardFrame())), BorderLayout.SOUTH);
    }

    private JPanel card(Movie movie) {
        JPanel panel = UI.panel();
        panel.setLayout(new BorderLayout(10, 10));
        JLabel banner = UI.picture(movie.banner, 470, 100);
        panel.add(banner, BorderLayout.NORTH);
        JPanel details = new JPanel(new BorderLayout(12, 0));
        details.setBackground(UI.WHITE);
        details.add(UI.picture(movie.poster, 116, 168), BorderLayout.WEST);
        JTextArea text = new JTextArea();
        text.setOpaque(false);
        text.setEditable(false);
        text.setForeground(UI.INK);
        text.setFont(UI.FONT);
        StringBuilder info = new StringBuilder(movie.title + "\n\n"
                + movie.genre + " | " + movie.status + "\n\n"
                + String.format("Regular: PHP %,.2f\nPremium: PHP %,.2f\n\n",
                        movie.regularPrice, movie.premiumPrice));
        for (Screening show : DataStore.schedules(movie)) {
            info.append(show.showtime).append(" / ").append(show.screen).append("  ·  ")
                    .append(40 - DataStore.sold(show)).append(" seats left\n");
        }
        text.setText(info.toString());
        details.add(text, BorderLayout.CENTER);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }
}
