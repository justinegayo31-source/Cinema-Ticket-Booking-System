package cinema;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Search, cancel, print, and export booking records. */
public class BookingHistoryFrame extends BaseFrame {
    private static final String[] COLUMNS = {
        "Booking ID", "Customer", "Movie", "Showtime", "Seats",
        "Regular", "Premium", "Total", "Status", "Date"
    };

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JLabel totals = UI.label("");
    private final javax.swing.JComboBox<String> statusFilter = new javax.swing.JComboBox<>(
            new String[]{"All statuses", "Confirmed", "Partially cancelled", "Cancelled"});

    public BookingHistoryFrame() {
        super("Booking history | Cinema Booking", 1200, 700);
        setLayout(new BorderLayout());
        add(UI.header("BOOKING HISTORY", "View receipts, cancel bookings and release seats"), BorderLayout.NORTH);

        JPanel body = UI.panel();
        body.setLayout(new BorderLayout(0, 12));
        table.setRowHeight(29);
        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {110, 150, 170, 100, 145, 65, 70, 120, 100, 145};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JPanel searchPanel = new JPanel(new BorderLayout(12, 0));
        searchPanel.setBackground(UI.WHITE);
        searchPanel.add(UI.label("Search booking / customer / movie:"), BorderLayout.WEST);
        JTextField search = new JTextField();
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String query = search.getText().trim();
                applyFilter(query);
            }
            @Override public void insertUpdate(DocumentEvent event) { filter(); }
            @Override public void removeUpdate(DocumentEvent event) { filter(); }
            @Override public void changedUpdate(DocumentEvent event) { filter(); }
        });
        searchPanel.add(search, BorderLayout.CENTER);
        searchPanel.add(statusFilter, BorderLayout.EAST);
        statusFilter.addActionListener(event -> applyFilter(search.getText()));
        body.add(searchPanel, BorderLayout.NORTH);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(UI.WHITE);
        footer.add(totals, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(UI.WHITE);
        JButton receipt = UI.outline("View / Print receipt");
        receipt.addActionListener(event -> showReceipt());
        JButton cancel = UI.button("Cancel Booking", UI.CORAL);
        cancel.addActionListener(event -> cancelBooking());
        JButton export = UI.button("Export CSV", UI.TEAL);
        export.addActionListener(event -> exportCsv());
        actions.add(receipt);
        actions.add(cancel);
        JButton partial = UI.button("Cancel selected seats", UI.ORANGE);
        partial.addActionListener(event -> cancelIndividualSeats());
        actions.add(partial);
        actions.add(export);
        footer.add(actions, BorderLayout.EAST);
        body.add(footer, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
        add(bottom("Back to dashboard", () -> go(new DashboardFrame())), BorderLayout.SOUTH);
        loadRows();
    }

    private void applyFilter(String text) {
        String query = text.trim();
        String status = String.valueOf(statusFilter.getSelectedItem());
        java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
        if (!query.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
        if (!status.equals("All statuses")) filters.add(RowFilter.regexFilter("^" + Pattern.quote(status) + "$", 8));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private void cancelIndividualSeats() {
        BookingRecord booking = selectedRecord();
        if (booking == null) return;
        java.util.List<String> active = booking.activeSeats();
        if (active.isEmpty()) { UI.error(this, "This booking has no active seats."); return; }
        javax.swing.JList<String> choices = new javax.swing.JList<>(active.toArray(new String[0]));
        choices.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        choices.setVisibleRowCount(Math.min(8, active.size()));
        if (JOptionPane.showConfirmDialog(this, new JScrollPane(choices),
                "Select seats to cancel | " + booking.id,
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        java.util.List<String> selected = choices.getSelectedValuesList();
        if (selected.isEmpty()) { UI.error(this, "Select at least one seat."); return; }
        double expected = 0;
        for (String code : selected) expected += booking.seatPrices != null
                ? booking.seatPrices.getOrDefault(code, 0.0)
                : booking.subtotal / Math.max(1, booking.quantity());
        int accepted = JOptionPane.showConfirmDialog(this,
                "Cancel seats " + String.join(", ", selected) + "?\n"
                + "Value to refund manually: PHP " + String.format("%,.2f", expected)
                + "\nThis will free only those seats. No automatic refund is issued.",
                "Confirm seat cancellation", JOptionPane.YES_NO_OPTION);
        if (accepted != JOptionPane.YES_OPTION) return;
        booking.cancelSeats(selected);
        DataStore.save();
        loadRows();
        UI.info(this, "Seats released. Process any refund separately.");
    }

    private void loadRows() {
        model.setRowCount(0);
        for (BookingRecord booking : DataStore.bookings) {
            model.addRow(new Object[] {
                booking.id, booking.customer, booking.movieTitle, booking.showtime,
                String.join(", ", booking.activeSeats()), booking.regular, booking.premium,
                String.format("PHP %,.2f", booking.activeTotal()),
                booking.status(), booking.date
            });
        }
        long active = DataStore.bookings.stream().filter(booking -> !booking.cancelled).count();
        totals.setText(String.format("%d active bookings  |  %d tickets  |  Revenue PHP %,.2f",
                active, DataStore.tickets(), DataStore.revenue()));
    }

    private BookingRecord selectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UI.error(this, "Select a booking first.");
            return null;
        }
        return DataStore.bookings.get(table.convertRowIndexToModel(viewRow));
    }

    private void cancelBooking() {
        BookingRecord booking = selectedRecord();
        if (booking == null) return;
        if (booking.cancelled) {
            UI.info(this, "This booking is already cancelled.");
            return;
        }

        String message = "Cancel booking " + booking.id + " for " + booking.customer
                + "?\nMovie: " + booking.movieTitle
                + "\nSeats: " + booking.seats
                + "\n\nThe seats will become available again."
                + "\nThe transaction will remain in history as Cancelled.";
        int choice = JOptionPane.showConfirmDialog(this, message,
                "Confirm cancellation", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        booking.cancelled = true;
        DataStore.save();
        loadRows();
        UI.info(this, "Booking " + booking.id + " cancelled. Seats released.");
    }

    private String receiptText(BookingRecord booking) {
        return String.format(
            "CINEMA BOOKING RECEIPT\n" +
            "----------------------------------------\n" +
            "Reference : %s\nStatus    : %s\nDate      : %s\n" +
            "Customer  : %s\nMovie     : %s\nShowtime  : %s\n" +
            "Screen    : %s\nSeats     : %s\n" +
            "Regular   : %d\nPremium   : %d\n" +
            "----------------------------------------\n" +
            "Total     : PHP %,.2f\nCash      : PHP %,.2f\n" +
            "Change    : PHP %,.2f\n",
            booking.id, booking.status().toUpperCase(),
            booking.date, booking.customer, booking.movieTitle,
            booking.showtime, booking.screen, String.join(", ", booking.activeSeats()),
            booking.regular, booking.premium, booking.activeTotal(),
            booking.cash, booking.cash - booking.subtotal);
    }

    private void showReceipt() {
        BookingRecord booking = selectedRecord();
        if (booking == null) return;
        JTextArea text = new JTextArea(receiptText(booking), 19, 43);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 12));
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(text),
                "Receipt " + booking.id, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                text.print();
            } catch (PrinterException exception) {
                UI.error(this, "Could not print receipt: " + exception.getMessage());
            }
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("cinema-bookings.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this,
                "Overwrite the existing file?", "Export CSV",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        StringBuilder csv = new StringBuilder(
                "Booking ID,Customer,Movie,Showtime,Screen,Seats,Regular,Premium,Total,Cash,Status,Date\n");
        for (BookingRecord booking : DataStore.bookings) {
            String[] values = {
                booking.id, booking.customer, booking.movieTitle,
                booking.showtime, booking.screen, String.join("; ", booking.activeSeats()),
                String.valueOf(booking.regular), String.valueOf(booking.premium),
                String.valueOf(booking.activeTotal()), String.valueOf(booking.cash),
                booking.status(), booking.date
            };
            for (int i = 0; i < values.length; i++) {
                if (i > 0) csv.append(',');
                csv.append('"').append(values[i].replace("\"", "\"\"")).append('"');
            }
            csv.append('\n');
        }
        try {
            Files.writeString(path, "\uFEFF" + csv, StandardCharsets.UTF_8);
            UI.info(this, "Booking report exported to:\n" + path);
        } catch (IOException exception) {
            UI.error(this, "Could not export report: " + exception.getMessage());
        }
    }
}
