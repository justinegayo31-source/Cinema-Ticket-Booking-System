package cinema;

import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {
        UI.setup();
        DataStore.load();
        SwingUtilities.invokeLater(()->new LoginFrame().setVisible(true));
    }
}
