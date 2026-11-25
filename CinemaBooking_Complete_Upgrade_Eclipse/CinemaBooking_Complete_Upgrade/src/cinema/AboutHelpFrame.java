package cinema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
public class AboutHelpFrame extends BaseFrame {
    public AboutHelpFrame() {
        super("About & help | Cinema Booking",760,610);
        setLayout(new BorderLayout());
        add(UI.header("ABOUT & HELP","Cinema Booking System  •  Java Swing final project"),BorderLayout.NORTH);
        JTextArea area = new JTextArea(
                "GETTING STARTED\n\n"
                + "1. Sign in with demo credentials: admin / admin.\n"
                + "2. Manage Movies: maintain titles, poster/banner images and prices.\n"
                + "3. Screening Schedules: create multiple showtimes per movie.\n"
                + "4. Movie Gallery: browse posters, banners and screening availability.\n"
                + "5. Book Tickets: select movie and showtime, then choose seat buttons.\n"
                + "6. Gold seats = Regular; teal seats = Premium; coral = Selected; gray = Booked.\n"
                + "7. Review the seat cart, enter customer and cash, and confirm.\n"
                + "8. Print the receipt using the Print latest receipt button.\n"
                + "9. Booking History: search, filter and cancel full or individual seats.\n"
                + "10. Sales Reports: see totals by movie and by date.\n"
                + "11. Backup / Restore: save and restore a local ZIP backup.\n"
                + "12. Dashboard: switch between light and dark mode.\n\n"
                + "IMPORTANT\n\n"
                + "- Each screening has 40 independent seats (A-C Regular, D-E Premium).\n"
                + "- Cancelled seats become selectable again; refunds are handled manually.\n"
                + "- Imported image files are copied into local application storage.\n"
                + "- Records are saved in your home folder under .cinema-booking-system.\n"
                + "- This is a local classroom demonstration, not a live payment service.\n"
                + "- Default admin credentials are for demonstration only.\n");
        area.setEditable(false);
        area.setFont(new Font("Dialog",Font.PLAIN,14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new Insets(22,24,22,24));
        add(new JScrollPane(area),BorderLayout.CENTER);
        add(bottom("Back to dashboard",()->go(new DashboardFrame())),BorderLayout.SOUTH);
    }
}
