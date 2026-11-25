package cinema;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.*;

/** Manual backup and restore; does not automatically upload any data. */
public final class BackupFrame extends BaseFrame {
    public BackupFrame() {
        super("Backup and restore | Cinema Booking", 760, 470);
        setLayout(new BorderLayout());
        add(UI.header("BACKUP & RESTORE", "Keep a copy before updating or transferring computers"), BorderLayout.NORTH);
        JPanel content = UI.panel();
        content.setLayout(new GridLayout(0, 1, 0, 18));
        JLabel location = UI.label("Local storage: " + DataStore.storageFolder());
        content.add(location);
        JButton backup = UI.button("Create ZIP backup", UI.TEAL);
        JButton restore = UI.button("Restore ZIP backup", UI.CORAL);
        content.add(backup); content.add(restore);
        content.add(UI.label("Restore replaces local records. A pre-restore copy is kept on disk."));
        add(content, BorderLayout.CENTER);
        add(bottom("Back to dashboard", () -> go(new DashboardFrame())), BorderLayout.SOUTH);
        backup.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            picker.setSelectedFile(new java.io.File("cinema-backup.zip"));
            if (picker.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            Path selected = picker.getSelectedFile().toPath();
            if (java.nio.file.Files.exists(selected) && JOptionPane.showConfirmDialog(this,
                    "Overwrite this backup?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try { DataStore.backup(selected); UI.info(this, "Backup created: " + selected); }
            catch (IOException | IllegalStateException ex) { UI.error(this, ex.getMessage()); }
        });
        restore.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            if (picker.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            if (JOptionPane.showConfirmDialog(this,
                    "Restore this backup? Current records will be replaced.\nOnly restore backups you trust.",
                    "Confirm restore", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try { DataStore.restore(picker.getSelectedFile().toPath());
                UI.info(this, "Backup restored. Dashboard will refresh."); go(new DashboardFrame()); }
            catch (IOException | IllegalStateException ex) { UI.error(this, "Restore failed: " + ex.getMessage()); }
        });
    }
}
