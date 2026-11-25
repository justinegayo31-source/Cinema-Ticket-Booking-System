package cinema;

import javax.swing.*;
import java.awt.*;
public abstract class BaseFrame extends JFrame {
    protected BaseFrame(String title,int width,int height) {
        super(title);
        setSize(width,height);
        setMinimumSize(new Dimension(Math.min(width,850),Math.min(height,560)));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UI.BG);
    }
    protected void go(JFrame frame) {
        dispose();
        frame.setVisible(true);
    }
    protected JPanel bottom(String label,Runnable action) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));
        p.setBackground(UI.BG);
        JButton b=UI.outline(label);
        b.addActionListener(e->action.run());
        p.add(b);
        return p;
    }
}
