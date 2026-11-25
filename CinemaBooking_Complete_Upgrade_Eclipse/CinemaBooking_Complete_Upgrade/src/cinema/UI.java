package cinema;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;

/** Shared typography, palette and reusable Swing components. */
public final class UI {
    // Movie Theater Admit One palette supplied for this project.
    public static final Color GOLD = new Color(0xE5BA72);
    public static final Color ORANGE = new Color(0xD8905D);
    public static final Color CORAL = new Color(0xC8674F);
    public static final Color TEAL = new Color(0x599BA3);
    public static final Color CHARCOAL = new Color(0x39312C);

    // Backward-compatible names used by the other Java screens.
    public static final Color NAVY = CHARCOAL;
    public static final Color RED = CORAL;
    public static final Color BLUE = TEAL;
    public static final Color GREEN = ORANGE;
    public static Color BG = new Color(0xF8F5F0);
    public static Color WHITE = Color.WHITE;
    public static Color INK = CHARCOAL;
    public static Color MUTED = new Color(0x73685F);
    public static Color BORDER = new Color(0xDFD6CC);

    public static final Font FONT = new Font("Dialog", Font.PLAIN, 13);
    public static final Font BOLD = new Font("Dialog", Font.BOLD, 13);

    private static boolean dark = java.util.prefs.Preferences.userNodeForPackage(UI.class)
            .getBoolean("darkTheme", false);

    private UI() {}

    public static boolean darkMode() { return dark; }

    public static void toggleTheme() {
        dark = !dark;
        java.util.prefs.Preferences.userNodeForPackage(UI.class).putBoolean("darkTheme", dark);
        applyTheme();
    }

    private static void applyTheme() {
        BG = dark ? new Color(0x211D1A) : new Color(0xF8F5F0);
        WHITE = dark ? new Color(0x302A26) : Color.WHITE;
        INK = dark ? new Color(0xF4EBDE) : CHARCOAL;
        MUTED = dark ? new Color(0xC1B1A5) : new Color(0x73685F);
        BORDER = dark ? new Color(0x554940) : new Color(0xDFD6CC);
    }

    public static void setup() {
        applyTheme();
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing's default look and feel remains usable.
        }
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", BOLD);
        UIManager.put("Table.selectionBackground", GOLD);
        UIManager.put("Table.selectionForeground", CHARCOAL);
        UIManager.put("TextField.background", WHITE);
        UIManager.put("TextField.foreground", INK);
        UIManager.put("ComboBox.background", WHITE);
        UIManager.put("ComboBox.foreground", INK);
        UIManager.put("Table.background", WHITE);
        UIManager.put("Table.foreground", INK);
        UIManager.put("TableHeader.background", GOLD);
        UIManager.put("TableHeader.foreground", CHARCOAL);
    }

    /** Accessible foreground color for the supplied button background. */
    private static Color contrastText(Color color) {
        double brightness = (0.2126 * color.getRed())
                + (0.7152 * color.getGreen())
                + (0.0722 * color.getBlue());
        return brightness >= 145 ? INK : WHITE;
    }

    public static JButton button(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(BOLD);
        button.setBackground(background);
        button.setForeground(contrastText(background));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 16, 11, 16));
        return button;
    }

    public static JButton outline(String text) {
        JButton button = new JButton(text);
        button.setFont(BOLD);
        button.setForeground(CHARCOAL);
        button.setBackground(GOLD);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER), new EmptyBorder(10, 14, 10, 14)));
        return button;
    }

    public static JPanel header(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(CHARCOAL);
        panel.setBorder(new EmptyBorder(19, 24, 18, 24));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Dialog", Font.BOLD, 21));
        heading.setForeground(WHITE);

        JLabel description = new JLabel(subtitle);
        description.setFont(FONT);
        description.setForeground(GOLD);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(description, BorderLayout.SOUTH);
        return panel;
    }

    public static JPanel panel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER), new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    public static JLabel label(String value) {
        JLabel label = new JLabel(value);
        label.setFont(BOLD);
        label.setForeground(INK);
        return label;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Cinema Booking", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message,
                "Check the details", JOptionPane.WARNING_MESSAGE);
    }

    public static ImageIcon image(String path, int width, int height) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            java.net.URL url = UI.class.getResource(path);
            ImageIcon source = url != null
                    ? new ImageIcon(url)
                    : new ImageIcon((path.startsWith("posters/")
                            ? DataStore.storageFolder().resolve(path).toFile()
                            : new File(path)).getAbsolutePath());
            if (source.getIconWidth() < 1) {
                return null;
            }
            return new ImageIcon(source.getImage().getScaledInstance(
                    width, height, Image.SCALE_SMOOTH));
        } catch (Exception ex) {
            return null;
        }
    }

    public static JLabel picture(String path, int width, int height) {
        JLabel label = new JLabel("Image unavailable", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(width, height));
        label.setOpaque(true);
        label.setBackground(BG);
        label.setForeground(MUTED);
        ImageIcon icon = image(path, width, height);
        if (icon != null) {
            label.setText("");
            label.setIcon(icon);
        }
        return label;
    }
}
