package cinema;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Cinema operations landing page and navigation. */
public class DashboardFrame extends BaseFrame {
    public DashboardFrame() {
        super("Dashboard | Cinema Booking", 1120, 770);
        setLayout(new BorderLayout());
        add(UI.header("CINEMA BOOKING", "Movies  /  Seats  /  Transactions"),
                BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 22));
        body.setBackground(UI.BG);
        body.setBorder(new EmptyBorder(25, 28, 25, 28));

        JPanel statistics = new JPanel(new GridLayout(1, 3, 16, 16));
        statistics.setOpaque(false);
        statistics.add(stat("NOW SHOWING",
                String.valueOf(DataStore.movies.stream().filter(Movie::isOpen).count())));
        statistics.add(stat("TICKETS SOLD", String.valueOf(DataStore.tickets())));
        statistics.add(stat("TOTAL SALES", String.format("PHP %,.2f", DataStore.revenue())));
        body.add(statistics, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(3, 3, 14, 14));
        menu.setOpaque(false);
        menu.add(nav("Book tickets", "Seat selection and checkout", UI.CORAL,
                () -> go(new TicketBookingFrame())));
        menu.add(nav("Manage movies", "Posters, banners and prices", UI.ORANGE,
                () -> go(new MovieManagementFrame())));
        menu.add(nav("Schedules & seats", "Showtimes and availability", UI.TEAL,
                () -> go(new ScheduleSeatFrame())));
        menu.add(nav("Booking history", "Transactions and receipts", UI.GOLD,
                () -> go(new BookingHistoryFrame())));
        menu.add(nav("Movie gallery", "Browse posters & banners", UI.TEAL,
                () -> go(new MovieGalleryFrame())));
        menu.add(nav("Sales reports", "Daily sales and movie breakdown", UI.ORANGE,
                () -> go(new SalesReportFrame())));
        menu.add(nav("Backup / Restore", "Protect local booking data", UI.GOLD,
                () -> go(new BackupFrame())));
        menu.add(nav("About & help", "Project guide", UI.CHARCOAL,
                () -> go(new AboutHelpFrame())));
        menu.add(nav("Log out", "Return to sign-in", UI.CHARCOAL,
                () -> go(new LoginFrame())));
        body.add(menu, BorderLayout.CENTER);
       
        add(body, BorderLayout.CENTER);
    }

    private JPanel stat(String title, String value) {
        JPanel panel = UI.panel();
        panel.setLayout(new BorderLayout(0, 16));
        JLabel heading = UI.label(title);
        heading.setForeground(UI.MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Dialog", Font.BOLD, 26));
        number.setForeground(UI.INK);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(number, BorderLayout.CENTER);
        return panel;
    }

    private JButton nav(String title, String subtitle, Color color, Runnable action) {
        String text = "<html><div style='text-align:left'>"
                + "<span style='font-size:16px'>" + title + "</span><br>"
                + "<span style='font-size:11px'>" + subtitle + "</span></div></html>";
        JButton button = UI.button(text, color);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> action.run());
        return button;
    }
}
