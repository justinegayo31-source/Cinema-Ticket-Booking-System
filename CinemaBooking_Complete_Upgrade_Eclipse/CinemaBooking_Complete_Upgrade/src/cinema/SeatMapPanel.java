package cinema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
public class SeatMapPanel extends JPanel {
    public interface Listener {
        void changed();
    }
    private Movie movie;
    private Screening screening;
    private final Map<String,JToggleButton> buttons=new LinkedHashMap<>();
    private final Set<String> chosen=new LinkedHashSet<>();
    private final Listener listener;
    public SeatMapPanel(Listener listener) {
        this.listener=listener;
        setBackground(UI.WHITE);
        setLayout(new BorderLayout(0,12));
    }
    public void showMovie(Movie m) {
        showScreening(m, DataStore.schedules(m).isEmpty() ? null : DataStore.schedules(m).get(0));
    }
    public void showScreening(Movie m, Screening showing) {
        movie=m;
        screening=showing;
        chosen.clear();
        buttons.clear();
        removeAll();
        if(m==null) {
            revalidate();
            repaint();
            return;
        }
        JPanel wrap=new JPanel(new BorderLayout(0,13));
        wrap.setBackground(UI.WHITE);
        wrap.setBorder(new EmptyBorder(10,12,8,12));
        JLabel screen=new JLabel("SCREEN",SwingConstants.CENTER);
        screen.setOpaque(true);
        screen.setBackground(UI.BG);
        screen.setForeground(UI.INK);
        screen.setFont(UI.BOLD);
        screen.setBorder(new EmptyBorder(7,0,7,0));
        wrap.add(screen,BorderLayout.NORTH);
        JPanel seats=new JPanel(new GridLayout(5,9,7,7));
        seats.setBackground(UI.WHITE);
        Set<String> booked=DataStore.booked(screening);
        for(int row=0; row<5; row++) {
            String letter="ABCDE".substring(row,row+1);
            JLabel r=new JLabel(letter,SwingConstants.CENTER);
            r.setFont(UI.BOLD);
            seats.add(r);
            for(int col=1; col<=8; col++) {
                String code=letter+col;
                JToggleButton b=new JToggleButton(code);
                b.setFont(new Font("Dialog",Font.BOLD,11));
                b.setOpaque(true);
                b.setFocusPainted(false);
                b.setMargin(new Insets(8,2,8,2));
                b.setEnabled(!booked.contains(code) && m.isOpen() && screening != null && screening.isOpen());
                buttons.put(code,b);
                if(booked.contains(code)) {
                    b.setBackground(UI.BORDER);
                    b.setForeground(UI.MUTED);
                }
                else b.setBackground(row>=3 ? UI.TEAL : UI.GOLD);
                if (!booked.contains(code)) b.setForeground(UI.INK);
                b.addActionListener(e-> {
                    if(b.isSelected())chosen.add(code); else chosen.remove(code); paintButtons(); listener.changed();
                }
                );
                seats.add(b);
            }
        }
        wrap.add(seats,BorderLayout.CENTER);
        JPanel legend=new JPanel(new FlowLayout(FlowLayout.CENTER,14,2));
        legend.setBackground(UI.WHITE);
        for(String item:new String[] {
            "Available regular (A-C)","Available premium (D-E)","Selected","Booked"
        }
        ) {
            Color c=item.startsWith("Available regular")?UI.GOLD:item.startsWith("Available premium")?UI.TEAL:item.equals("Selected")?UI.CORAL:UI.BORDER;
            JLabel lbl=new JLabel("■  "+item);
            lbl.setForeground(c.equals(UI.CORAL)?UI.CORAL:item.equals("Booked")?UI.MUTED:UI.INK);
            lbl.setFont(new Font("Dialog",Font.PLAIN,11));
            legend.add(lbl);
        }
        add(wrap,BorderLayout.CENTER);
        add(legend,BorderLayout.SOUTH);
        revalidate();
        repaint();
        listener.changed();
    }
    private void paintButtons() {
        for(Map.Entry<String,JToggleButton> entry:buttons.entrySet()) {
            String code=entry.getKey();
            JToggleButton b=entry.getValue();
            if(!b.isEnabled())continue;
            b.setBackground(chosen.contains(code)?UI.CORAL:code.charAt(0)>='D'?UI.TEAL:UI.GOLD);
            b.setForeground(chosen.contains(code) ? Color.WHITE : UI.INK);
        }
    }
    public List<String> selected() {
        return new ArrayList<>(chosen);
    }
    public int regular() {
        int n=0;
        for(String s:chosen)if(s.charAt(0)<='C')n++;
        return n;
    }
    public int premium() {
        return chosen.size()-regular();
    }
}
