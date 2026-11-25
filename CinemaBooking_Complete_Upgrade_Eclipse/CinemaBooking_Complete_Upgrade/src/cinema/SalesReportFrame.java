package cinema;

import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Active revenue and sales report with per-movie and per-day breakdowns. */
public final class SalesReportFrame extends BaseFrame {
    public SalesReportFrame() {
        super("Sales reports | Cinema Booking", 1050, 730);
        setLayout(new BorderLayout());
        add(UI.header("SALES REPORTS", "Active bookings only · cancelled seats excluded"), BorderLayout.NORTH);
        JPanel body = UI.panel();
        body.setLayout(new BorderLayout(8, 12));
        long cancelled = DataStore.bookings.stream().filter(b -> b.cancelled).count();
        JLabel overview = UI.label(String.format(
                "Tickets sold: %d    |    Net sales: PHP %,.2f    |    Cancelled bookings: %d",
                DataStore.tickets(), DataStore.revenue(), cancelled));
        body.add(overview, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        DefaultTableModel byMovie = new DefaultTableModel(
                new String[]{"Movie", "Tickets sold", "Net revenue (PHP)"}, 0);
        for (Movie movie : DataStore.movies) {
            int count = 0; double sum = 0;
            for (BookingRecord b : DataStore.bookings) if (b.movieId.equals(movie.id)) {
                count += b.activeQuantity(); sum += b.activeTotal();
            }
            byMovie.addRow(new Object[]{movie.title, count, String.format("%,.2f", sum)});
        }
        tabs.addTab("By movie", table(byMovie));
        Map<String, double[]> totals = new TreeMap<>();
        for (BookingRecord b : DataStore.bookings) {
            String day = b.date == null || b.date.length() < 10 ? "Unknown" : b.date.substring(0, 10);
            double[] values = totals.computeIfAbsent(day, k -> new double[2]);
            values[0] += b.activeQuantity(); values[1] += b.activeTotal();
        }
        DefaultTableModel byDay = new DefaultTableModel(new String[]{"Date", "Tickets", "Net revenue (PHP)"}, 0);
        for (Map.Entry<String, double[]> entry : totals.entrySet()) {
            byDay.addRow(new Object[]{entry.getKey(), (int) entry.getValue()[0],
                    String.format("%,.2f", entry.getValue()[1])});
        }
        tabs.addTab("By date", table(byDay));
        body.add(tabs, BorderLayout.CENTER);
        JLabel note = UI.label("Note: cancellations adjust reported sales; cash refunds are processed separately.");
        body.add(note, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
        add(bottom("Back to dashboard", () -> go(new DashboardFrame())), BorderLayout.SOUTH);
    }

    private JScrollPane table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        return new JScrollPane(table);
    }
}
