package cinema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
public class MovieManagementFrame extends BaseFrame {
    private JTextField id,title,showtime,screen,regular,premium,poster,banner;
    private JComboBox<String> genre;
    private JComboBox<String> status;
    private DefaultTableModel model;
    private JTable table;
    public MovieManagementFrame() {
        super("Manage movies | Cinema Booking",1180,750);
        setLayout(new BorderLayout());
        add(UI.header("MANAGE MOVIES","Maintain screening details, images, and ticket prices"),BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(13,13));
        body.setBackground(UI.BG);
        body.setBorder(new EmptyBorder(15,18,12,18));
        JPanel form=UI.panel();
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(400,530));
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(5,6,5,6);
        g.fill=GridBagConstraints.HORIZONTAL;
        id=new JTextField();
        title=new JTextField();
        genre = new JComboBox<>(new String[] {
            "Action", "Adventure", "Animation", "Anime", "Biography",
            "Comedy", "Crime", "Documentary", "Drama", "Family",
            "Fantasy", "Film Noir", "Historical", "Horror", "Independent",
            "Martial Arts", "Musical", "Mystery", "Romance", "Romantic Comedy",
            "Sci-Fi", "Science Fiction", "Short Film", "Sports", "Superhero",
            "Suspense", "Thriller", "War", "Western", "Other"
        });
        genre.setEditable(true); // Users can type a genre that is not in the list.
        showtime=new JTextField();
        screen=new JTextField();
        regular=new JTextField();
        premium=new JTextField();
        poster=new JTextField();
        banner=new JTextField();
        status=new JComboBox<>(new String[] {
            "Open/Available","Closed/Unavailable"
        }
        );
        row(form,g,0,"Movie ID",id);
        row(form,g,1,"Title",title);
        row(form,g,2,"Category / genre",genre);
        row(form,g,3,"Showtime",showtime);
        row(form,g,4,"Venue / screen",screen);
        row(form,g,5,"Regular price",regular);
        row(form,g,6,"Premium price",premium);
        row(form,g,7,"Status",status);
        imageRow(form,g,8,"Poster image",poster);
        imageRow(form,g,9,"Banner image",banner);
        JLabel note=new JLabel("Images: PNG or JPG · Minimum 2 per movie");
        note.setForeground(UI.MUTED);
        g.gridx=0;
        g.gridy=10;
        g.gridwidth=2;
        form.add(note,g);
        JButton add=UI.button("ADD MOVIE", UI.CORAL);
        add.addActionListener(e->addMovie());
        g.gridy=11;
        form.add(add,g);
        body.add(form,BorderLayout.WEST);
        JPanel list=UI.panel();
        list.setLayout(new BorderLayout(0,12));
        model=new DefaultTableModel(new String[] {
            "ID","Movie","Showtime","Screen","Regular","Premium","Status"
        }
        ,0) {
            public boolean isCellEditable(int r,int c) {
                return false;
            }
        }
        ;
        table=new JTable(model);
        table.setRowHeight(27);
        table.setAutoCreateRowSorter(true);
        list.add(UI.label("MOVIE CATALOG"),BorderLayout.NORTH);
        list.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(UI.WHITE);
        JButton close=UI.button("Open / Close", UI.TEAL);
        close.addActionListener(e->toggle());
        actions.add(close);
        JButton update = UI.button("Edit details", UI.ORANGE);
        update.addActionListener(e -> updateMovie());
        actions.add(update);
        JButton images = UI.button("Replace images", UI.GOLD);
        images.addActionListener(e -> replaceImages());
        actions.add(images);
        JButton delete=UI.button("Delete selected", UI.CHARCOAL);
        delete.addActionListener(e->delete());
        actions.add(delete);
        list.add(actions,BorderLayout.SOUTH);
        body.add(list,BorderLayout.CENTER);
        add(body,BorderLayout.CENTER);
        add(bottom("Back to dashboard",()->go(new DashboardFrame())),BorderLayout.SOUTH);
        reload();
    }
    private void row(JPanel p,GridBagConstraints g,int y,String name,JComponent field) {
        g.gridwidth=1;
        g.gridx=0;
        g.gridy=y;
        g.weightx=0.30;
        p.add(UI.label(name),g);
        g.gridx=1;
        g.weightx=0.70;
        p.add(field,g);
    }
    private void imageRow(JPanel p,GridBagConstraints g,int y,String name,JTextField text) {
        JPanel picker=new JPanel(new BorderLayout(5,0));
        picker.setBackground(UI.WHITE);
        picker.add(text,BorderLayout.CENTER);
        JButton browse=UI.outline("Browse");
        browse.addActionListener(e-> {
            JFileChooser chooser=new JFileChooser(); if(chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)text.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        );
        picker.add(browse,BorderLayout.EAST);
        row(p,g,y,name,picker);
    }
    private void reload() {
        model.setRowCount(0);
        for(Movie m:DataStore.movies)model.addRow(new Object[] {
            m.id,m.title,m.showtime,m.screen,String.format("%.2f",m.regularPrice),String.format("%.2f",m.premiumPrice),m.status
        }
        );
    }
    private int selected() {
        int view=table.getSelectedRow();
        return view<0?-1:table.convertRowIndexToModel(view);
    }
    private boolean imageValid(String s) {
        if(s.startsWith("/cinema/assets/"))return UI.class.getResource(s)!=null;
        File f=new File(s);
        if(!f.isFile())return false;
        String n=f.getName().toLowerCase();
        return n.endsWith(".png")||n.endsWith(".jpg")||n.endsWith(".jpeg");
    }
    private void addMovie() {
        try {
            String a=id.getText().trim(),b=title.getText().trim(),c=String.valueOf(genre.getSelectedItem()).trim(),d=showtime.getText().trim(),e=screen.getText().trim();
            String f=poster.getText().trim(),h=banner.getText().trim();
            double rp=Double.parseDouble(regular.getText().trim()),pp=Double.parseDouble(premium.getText().trim());
            if(a.isEmpty()||b.isEmpty()||c.isEmpty()||d.isEmpty()||e.isEmpty()) {
                UI.error(this,"ID, title, genre, schedule and screen are required.");
                return;
            }
            if(DataStore.duplicate(a)) {
                UI.error(this,"Movie ID already exists.");
                return;
            }
            if(!Double.isFinite(rp)||!Double.isFinite(pp)||rp<=0||pp<=0) {
                UI.error(this,"Ticket prices must be positive numbers.");
                return;
            }
            if(!imageValid(f)||!imageValid(h)) {
                UI.error(this,"Choose existing PNG/JPG poster AND banner image files.");
                return;
            }
            if(new File(f).equals(new File(h))) {
                UI.error(this,"Poster and banner must be two different images.");
                return;
            }
            // A copy is saved in the app data directory so moving the original image
            // will not break the poster or banner in the cinema catalog.
            String savedPoster = f.startsWith("/cinema/assets/") ? f : DataStore.importImage(f);
            String savedBanner = h.startsWith("/cinema/assets/") ? h : DataStore.importImage(h);
            Movie m = new Movie(a, b, c, d, e, (String) status.getSelectedItem(), rp, pp, savedPoster, savedBanner);
            DataStore.movies.add(m);
            DataStore.screenings.add(new Screening(DataStore.newScreeningId(m), m.id, m.showtime, m.screen, m.status));
            DataStore.save();
            reload();
            for(JTextField t:new JTextField[] {
                id,title,showtime,screen,regular,premium,poster,banner
            }
            )t.setText("");
            genre.setSelectedIndex(0);
            UI.info(this,"Movie added.");
        }
        catch(NumberFormatException ex) {
            UI.error(this,"Enter valid ticket prices.");
        }
        catch(IOException ex) {
            UI.error(this, "Could not import a poster/banner: " + ex.getMessage());
        }
    }
    private void replaceImages() {
        int index = selected();
        if (index < 0) { UI.error(this, "Select a movie first."); return; }
        Movie movie = DataStore.movies.get(index);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose new poster PNG/JPG");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String newPoster = chooser.getSelectedFile().getAbsolutePath();
        chooser.setDialogTitle("Choose new banner PNG/JPG");
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String newBanner = chooser.getSelectedFile().getAbsolutePath();
        if (!imageValid(newPoster) || !imageValid(newBanner) || newPoster.equals(newBanner)) {
            UI.error(this, "Poster and banner must be two different PNG/JPG images.");
            return;
        }
        try {
            movie.poster = DataStore.importImage(newPoster);
            movie.banner = DataStore.importImage(newBanner);
            DataStore.save();
            UI.info(this, "Poster and banner replaced for " + movie.title + ".");
        } catch (IOException ex) {
            UI.error(this, "Could not update images: " + ex.getMessage());
        }
    }

    private void updateMovie() {
        int index = selected();
        if (index < 0) { UI.error(this, "Select a movie to edit."); return; }
        Movie movie = DataStore.movies.get(index);
        JTextField titleField = new JTextField(movie.title);
        JTextField genreField = new JTextField(movie.genre);
        JTextField regularField = new JTextField(String.valueOf(movie.regularPrice));
        JTextField premiumField = new JTextField(String.valueOf(movie.premiumPrice));
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 5));
        form.add(UI.label("Title")); form.add(titleField);
        form.add(UI.label("Genre")); form.add(genreField);
        form.add(UI.label("Regular price")); form.add(regularField);
        form.add(UI.label("Premium price")); form.add(premiumField);
        if (JOptionPane.showConfirmDialog(this, form, "Edit " + movie.id,
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            double r = Double.parseDouble(regularField.getText().trim());
            double p = Double.parseDouble(premiumField.getText().trim());
            if (titleField.getText().isBlank() || genreField.getText().isBlank()
                    || !Double.isFinite(r) || !Double.isFinite(p) || r <= 0 || p <= 0) {
                UI.error(this, "Enter a valid title, genre and positive prices."); return;
            }
            movie.title = titleField.getText().trim();
            movie.genre = genreField.getText().trim();
            movie.regularPrice = r; movie.premiumPrice = p;
            DataStore.save(); reload();
        } catch (NumberFormatException ex) {
            UI.error(this, "Enter valid prices.");
        }
    }

    private void toggle() {
        int i=selected();
        if(i<0) {
            UI.error(this,"Select a movie.");
            return;
        }
        Movie m=DataStore.movies.get(i);
        m.status=m.isOpen()?"Closed/Unavailable":"Open/Available";
        DataStore.save();
        reload();
    }
    private void delete() {
        int i=selected();
        if(i<0) {
            UI.error(this,"Select a movie.");
            return;
        }
        Movie m=DataStore.movies.get(i);
        for (BookingRecord booking : DataStore.bookings) {

            if (booking.movieId.equals(m.id)
                    && booking.activeQuantity() > 0) {

                UI.error(
                    this,
                    "This movie has active bookings. "
                    + "Cancel them before deleting the movie."
                );

                return;
            }
        }
       
        if(JOptionPane.showConfirmDialog(this,"Delete "+m.title+"?","Confirm deletion",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            DataStore.movies.remove(i);
            DataStore.screenings.removeIf(s -> s.movieId.equals(m.id));
            DataStore.save();
            reload();
        }
    }
}
