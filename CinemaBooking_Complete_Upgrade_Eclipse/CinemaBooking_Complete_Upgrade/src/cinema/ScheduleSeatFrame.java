package cinema;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Independent screening sessions with their own seat availability. */
public final class ScheduleSeatFrame extends BaseFrame {
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Screening ID", "Movie", "Showtime", "Screen", "Available", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public ScheduleSeatFrame() {
        super("Schedules and seats | Cinema Booking", 1080, 680);
        setLayout(new BorderLayout());
        add(UI.header("SCREENING SCHEDULES", "Every showtime has an independent 40-seat map"), BorderLayout.NORTH);
        JPanel content = UI.panel();
        content.setLayout(new BorderLayout(0, 12));
        table.setRowHeight(29);
        table.setAutoCreateRowSorter(true);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        controls.setBackground(UI.WHITE);
        JButton add = UI.button("Add screening", UI.TEAL);
        JButton edit = UI.button("Edit screening", UI.ORANGE);
        JButton close = UI.button("Open / Close", UI.GOLD);
        JButton delete = UI.button("Delete screening", UI.CORAL);
        controls.add(add); controls.add(edit); controls.add(close); controls.add(delete);
        add.addActionListener(e -> configure(null));
        edit.addActionListener(e -> { Screening s = selected(); if (s != null) configure(s); });
        close.addActionListener(e -> {
            Screening s = selected();
            if (s == null) return;
            s.status = s.isOpen() ? "Closed/Unavailable" : "Open/Available";
            DataStore.save(); refresh();
        });
        delete.addActionListener(e -> {
            Screening s = selected();
            if (s == null) return;
            if (DataStore.bookings.stream().anyMatch(b -> s.id.equals(b.screeningId))) {
                UI.error(this, "This screening has booking history; close it instead."); return;
            }
            if (JOptionPane.showConfirmDialog(this, "Delete screening " + s.id + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DataStore.screenings.remove(s); DataStore.save(); refresh();
            }
        });
        content.add(controls, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
        add(bottom("Back to dashboard", () -> go(new DashboardFrame())), BorderLayout.SOUTH);
        refresh();
    }

    private Screening selected() {
        int row = table.getSelectedRow();
        if (row < 0) { UI.error(this, "Select a screening first."); return null; }
        return DataStore.screenings.get(table.convertRowIndexToModel(row));
    }

    private void refresh() {
        model.setRowCount(0);
        for (Screening screening : DataStore.screenings) {
            Movie movie = DataStore.findMovie(screening.movieId);
            model.addRow(new Object[]{screening.id, movie == null ? "Missing movie" : movie.title,
                    screening.showtime, screening.screen, 40 - DataStore.sold(screening), screening.status});
        }
    }

    private void configure(Screening current) {
        if (DataStore.movies.isEmpty()) { UI.error(this, "Add a movie first."); return; }
        JComboBox<Movie> movies = new JComboBox<>(DataStore.movies.toArray(new Movie[0]));
        JTextField time = new JTextField(current == null ? "7:00 PM" : current.showtime);
        JTextField screen = new JTextField(current == null ? "Screen 1" : current.screen);
        JComboBox<String> status = new JComboBox<>(new String[]{"Open/Available", "Closed/Unavailable"});
        if (current != null) {
            movies.setSelectedItem(DataStore.findMovie(current.movieId));
            movies.setEnabled(false);
            status.setSelectedItem(current.status);
        }
        JPanel fields = new JPanel(new GridLayout(0, 1, 5, 6));
        fields.add(UI.label("Movie")); fields.add(movies);
        fields.add(UI.label("Showtime (e.g. 7:00 PM)")); fields.add(time);
        fields.add(UI.label("Screen")); fields.add(screen);
        fields.add(UI.label("Status")); fields.add(status);
        if (JOptionPane.showConfirmDialog(this, fields, current == null ? "Add screening" : "Edit screening",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Movie movie = (Movie) movies.getSelectedItem();
        String when = time.getText().trim(), where = screen.getText().trim();
        if (movie == null || when.isBlank() || where.isBlank()) {
            UI.error(this, "All fields are required."); return;
        }
        for (Screening s : DataStore.schedules(movie)) {
            if (s != current && s.showtime.equalsIgnoreCase(when) && s.screen.equalsIgnoreCase(where)) {
                UI.error(this, "This movie already has that showtime on the same screen."); return;
            }
        }
        if (current != null) {
            if (DataStore.sold(current) > 0 && (!current.showtime.equals(when) || !current.screen.equals(where))) {
                UI.error(this, "Cannot move a booked screening. Close it and add another."); return;
            }
            current.showtime = when; current.screen = where;
            current.status = (String) status.getSelectedItem();
        } else {
            DataStore.screenings.add(new Screening(DataStore.newScreeningId(movie), movie.id,
                    when, where, (String) status.getSelectedItem()));
        }
        DataStore.save(); refresh();
    }
}
