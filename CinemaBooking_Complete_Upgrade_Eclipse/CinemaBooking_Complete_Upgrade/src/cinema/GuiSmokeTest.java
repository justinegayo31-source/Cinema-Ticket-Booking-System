package cinema;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Optional GUI smoke-test, used to verify screens launch. */
public final class GuiSmokeTest {
    public static void main(String[] args) throws Exception {
        System.setProperty("user.home", Files.createTempDirectory("cinema-gui-").toString());
        UI.setup();
        DataStore.load();
        JFrame[] frames = {
            new LoginFrame(), new DashboardFrame(), new MovieManagementFrame(),
            new ScheduleSeatFrame(), new TicketBookingFrame(), new BookingHistoryFrame(),
            new MovieGalleryFrame(), new SalesReportFrame(), new BackupFrame(),
            new AboutHelpFrame()
        };
        Path screenshots = Paths.get(args.length > 0 ? args[0] : ".");
        Files.createDirectories(screenshots);
        for (JFrame frame : frames) {
            SwingUtilities.invokeAndWait(() -> frame.setVisible(true));
            Thread.sleep(200);
            BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            SwingUtilities.invokeAndWait(() -> frame.paint(image.getGraphics()));
            ImageIO.write(image, "png", screenshots.resolve(frame.getClass().getSimpleName() + ".png").toFile());
            SwingUtilities.invokeAndWait(frame::dispose);
            System.out.println("OK " + frame.getClass().getSimpleName());
        }
        System.exit(0);
    }
}
