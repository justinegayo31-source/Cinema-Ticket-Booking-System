package cinema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
public class TicketBookingFrame extends BaseFrame {
    private JComboBox<Movie> chooser;
    private JComboBox<Screening> showtimes;
    private JLabel poster,banner,regular, premium,total,available;
    private JTextField customer,cash;
    private JTextArea receipt;
    private SeatMapPanel seatMap;
    private DefaultTableModel selectedModel;
    private JButton confirm;
    public TicketBookingFrame() {
        super("Book tickets | Cinema Booking",1290,850);
        setLayout(new BorderLayout(0,8));
        add(UI.header("BOOK TICKETS","Choose a screening, select seats and confirm payment"),BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(14,0));
        body.setBackground(UI.BG);
        body.setBorder(new EmptyBorder(13,16,12,16));
        JPanel left=UI.panel();
        left.setLayout(new BorderLayout(0,10));
        left.setPreferredSize(new Dimension(275,650));
        JPanel detail=new JPanel();
        detail.setBackground(UI.WHITE);
        detail.setLayout(new BoxLayout(detail,BoxLayout.Y_AXIS));
        detail.add(UI.label("NOW SHOWING"));
        detail.add(Box.createVerticalStrut(10));
        chooser=new JComboBox<>(DataStore.movies.toArray(new Movie[0]));
        detail.add(chooser);
        detail.add(Box.createVerticalStrut(10));
        detail.add(UI.label("SHOWTIME / SCREEN"));
        showtimes = new JComboBox<>();
        detail.add(showtimes);
        detail.add(Box.createVerticalStrut(15));
        banner=new JLabel();
        banner.setAlignmentX(LEFT_ALIGNMENT);
        detail.add(banner);
        detail.add(Box.createVerticalStrut(10));
        poster=new JLabel();
        poster.setAlignmentX(LEFT_ALIGNMENT);
        detail.add(poster);
        detail.add(Box.createVerticalStrut(10));
        available=UI.label(" ");
        detail.add(available);
        detail.add(Box.createVerticalStrut(8));
        regular=UI.label(" ");
        premium=UI.label(" ");
        detail.add(regular);
        detail.add(premium);
        left.add(detail,BorderLayout.NORTH);
        body.add(left,BorderLayout.WEST);
        JPanel center=UI.panel();
        center.setLayout(new BorderLayout(0,8));
        center.add(UI.label("SELECT SEATS   ·   A–C Regular / D–E Premium"),BorderLayout.NORTH);
        seatMap=new SeatMapPanel(this::refresh);
        center.add(seatMap,BorderLayout.CENTER);
        body.add(center,BorderLayout.CENTER);
        JPanel right=UI.panel();
        right.setLayout(new BorderLayout(0,9));
        right.setPreferredSize(new Dimension(315,650));
        JPanel form=new JPanel();
        form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));
        form.setBackground(UI.WHITE);
        form.add(UI.label("CHECKOUT"));
        form.add(UI.label("Customer name"));
        customer=new JTextField();
        customer.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        form.add(customer);
        form.add(Box.createVerticalStrut(9));
        form.add(UI.label("Selected seats"));
        selectedModel=new DefaultTableModel(new String[] {
            "Seat","Type","Price"
        }
        ,0) {
            public boolean isCellEditable(int r,int c) {
                return false;
            }
        }
        ;
        JTable table=new JTable(selectedModel);
        table.setRowHeight(24);
        JScrollPane list=new JScrollPane(table);
        list.setPreferredSize(new Dimension(240,145));
        list.setMaximumSize(new Dimension(Integer.MAX_VALUE,145));
        form.add(list);
        form.add(Box.createVerticalStrut(9));
        total=new JLabel("TOTAL    PHP 0.00");
        total.setFont(new Font("Dialog",Font.BOLD,19));
        total.setForeground(UI.RED);
        form.add(total);
        form.add(Box.createVerticalStrut(9));
        form.add(UI.label("Cash received (PHP)"));
        cash=new JTextField();
        cash.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        form.add(cash);
        form.add(Box.createVerticalStrut(9));
        confirm=UI.button("REVIEW & CONFIRM", UI.CORAL);
        confirm.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
        form.add(confirm);
        right.add(form,BorderLayout.NORTH);
        receipt=new JTextArea(12,20);
        receipt.setEditable(false);
        receipt.setFont(new Font("Monospaced",Font.PLAIN,11));
        receipt.setBackground(UI.BG);
        receipt.setBorder(new EmptyBorder(8,8,8,8));
        JScrollPane receiptScroll=new JScrollPane(receipt);
        receiptScroll.setBorder(BorderFactory.createTitledBorder("Latest receipt"));
        right.add(receiptScroll,BorderLayout.CENTER);
        JButton printTicket = UI.outline("Print latest receipt");
        printTicket.addActionListener(e -> {
            if (receipt.getText().isBlank()) {
                UI.error(this, "Complete a booking before printing a receipt."); return;
            }
            try {
                receipt.print();
            }
            catch (java.awt.print.PrinterException ex) {
                UI.error(this, "Printing failed: " + ex.getMessage());
            }
        }
        );
        right.add(printTicket,BorderLayout.SOUTH);
        body.add(right,BorderLayout.EAST);
        add(body,BorderLayout.CENTER);
        add(bottom("Back to dashboard",()->go(new DashboardFrame())),BorderLayout.SOUTH);
        chooser.addActionListener(e->showSelected());
        showtimes.addActionListener(e->showShowing());
        confirm.addActionListener(e->checkout());
        showSelected();
    }
    private Movie movie() {
        return (Movie)chooser.getSelectedItem();
    }
    private Screening screening() {
        return (Screening) showtimes.getSelectedItem();
    }

    private void showSelected() {
        Movie movie = movie();
        showtimes.removeAllItems();
        if (movie == null) return;
        banner.setIcon(UI.image(movie.banner, 232, 90));
        poster.setIcon(UI.image(movie.poster, 125, 165));
        regular.setText(String.format("Regular   PHP %,.2f", movie.regularPrice));
        premium.setText(String.format("Premium  PHP %,.2f", movie.premiumPrice));
        for (Screening screening : DataStore.schedules(movie)) showtimes.addItem(screening);
        showShowing();
    }

    private void showShowing() {
        Movie movie = movie();
        Screening screening = screening();
        if (movie == null) return;
        int remaining = 40 - DataStore.sold(screening);
        available.setText(remaining + " / 40 seats available  •  "
                + (screening == null ? "No screening" : screening.screen));
        seatMap.showScreening(movie, screening);
        confirm.setEnabled(movie.isOpen() && screening != null && screening.isOpen());
    }

    private void refresh() {
        if(selectedModel==null||total==null)return;
        Movie m=movie();
        if(m==null)return;
        selectedModel.setRowCount(0);
        java.util.List<String> seats=seatMap.selected();
        Collections.sort(seats);
        for(String code:seats) {
            boolean prem=code.charAt(0)>='D';
            selectedModel.addRow(new Object[] {
                code,prem?"Premium":"Regular",String.format("PHP %.2f",prem?m.premiumPrice:m.regularPrice)
            }
            );
        }
        total.setText(String.format("TOTAL    PHP %,.2f",amount()));
    }
    private double amount() {
        Movie m=movie();
        if(m==null)return 0;
        return seatMap.regular()*m.regularPrice+seatMap.premium()*m.premiumPrice;
    }
    private void checkout() {
        Movie m=movie();
        Screening showing = screening();
        if(m==null||!m.isOpen()||showing==null||!showing.isOpen()) {
            UI.error(this,"This screening is closed.");
            return;
        }
        String name=customer.getText().trim();
        if(name.isEmpty()) {
            UI.error(this,"Enter a customer name.");
            return;
        }
        java.util.List<String> seats=seatMap.selected();
        if(seats.isEmpty()) {
            UI.error(this,"Select at least one available seat.");
            return;
        }
        double paid;
        try {
            paid=Double.parseDouble(cash.getText().trim());
            if(!Double.isFinite(paid)||paid<0)throw new NumberFormatException();
        }
        catch(NumberFormatException ex) {
            UI.error(this,"Enter a valid cash payment.");
            return;
        }
        double amount=amount();
        if(paid<amount) {
            UI.error(this,"Insufficient payment. Total due is PHP "+String.format("%,.2f",amount));
            return;
        }
        Collections.sort(seats);
        String joined=String.join(", ",seats);
        String details=String.format("Movie: %s\nShowtime: %s | %s\nCustomer: %s\nSeats: %s\nRegular: %d x PHP %,.2f\nPremium: %d x PHP %,.2f\nTOTAL: PHP %,.2f\nCASH: PHP %,.2f\nCHANGE: PHP %,.2f", m.title,showing.showtime,showing.screen,name,joined,seatMap.regular(),m.regularPrice,seatMap.premium(),m.premiumPrice,amount,paid,paid-amount);
        int result=JOptionPane.showConfirmDialog(this,details,"Confirm ticket booking",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE);
        if(result!=JOptionPane.OK_OPTION)return;
        if(!Collections.disjoint(seats,DataStore.booked(showing))) {
            UI.error(this,"A selected seat has already been booked. Please select again.");
            seatMap.showScreening(m, showing);
            return;
        }
        BookingRecord b=new BookingRecord(DataStore.bookingId(),name,m,showing,joined,seatMap.regular(),seatMap.premium(),amount,paid);
        DataStore.bookings.add(b);
        DataStore.save();
        receipt.setText("CINEMA BOOKING RECEIPT\n-------------------------------\nRef: "+b.id+"\nDate: "+b.date+"\n"+details+"\n-------------------------------\nThank you for your booking.");
        seatMap.showScreening(m, showing);
        available.setText((40-DataStore.sold(showing))+" / 40 seats available  •  "+showing.screen);
        customer.setText("");
        cash.setText("");
        UI.info(this,"Booking confirmed: "+b.id+"\nSelected seats are now marked Booked.");
    }
}
