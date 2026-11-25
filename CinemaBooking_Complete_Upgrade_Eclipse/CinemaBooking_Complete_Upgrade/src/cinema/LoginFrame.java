package cinema;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Staff login. Three failed attempts are enforced but never displayed. */
public class LoginFrame extends BaseFrame {
    private static final int MAX_ATTEMPTS = 3;
    private int failedAttempts = 0;

    public LoginFrame() {
        super("Sign in | Cinema Booking", 480, 420);
        setResizable(false);
        setLayout(new BorderLayout());
        add(UI.header("CINEMA BOOKING", "Staff sign-in"), BorderLayout.NORTH);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UI.BG);

        JPanel form = UI.panel();
        form.setLayout(new GridLayout(0, 1, 0, 10));
        form.setPreferredSize(new Dimension(340, 250));

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setBackground(UI.WHITE);
        showPassword.setForeground(UI.INK);
        JButton signIn = UI.button("SIGN IN", UI.CORAL);

        form.add(UI.label("Username"));
        form.add(username);
        form.add(UI.label("Password"));
        form.add(password);
        form.add(showPassword);
        form.add(signIn);

        char originalEcho = password.getEchoChar();
        showPassword.addActionListener(e -> password.setEchoChar(
                showPassword.isSelected() ? (char) 0 : originalEcho));

        signIn.addActionListener(e -> {
            String enteredUsername = username.getText().trim();
            String enteredPassword = new String(password.getPassword());
            if ("admin".equals(enteredUsername) && "admin".equals(enteredPassword)) {
                go(new DashboardFrame());
                return;
            }
            failedAttempts++;
            password.setText("");
            if (failedAttempts >= MAX_ATTEMPTS) {
                UI.error(this, "Maximum login attempts reached. The application will close.");
                dispose();
                System.exit(0);
            } else {
                UI.error(this, "Incorrect username or password.");
            }
        });

        outer.add(form);
        add(outer, BorderLayout.CENTER);
        getRootPane().setDefaultButton(signIn);
    }
}
