package cinema;

import java.io.Serializable;
public class Movie implements Serializable {
    private static final long serialVersionUID = 2L;
    public String id, title, genre, showtime, screen, status, poster, banner;
    public double regularPrice, premiumPrice;
    public Movie(String id, String title, String genre, String showtime, String screen, String status, double regularPrice, double premiumPrice, String poster, String banner) {
        this.id=id;
        this.title=title;
        this.genre=genre;
        this.showtime=showtime;
        this.screen=screen;
        this.status=status;
        this.regularPrice=regularPrice;
        this.premiumPrice=premiumPrice;
        this.poster=poster;
        this.banner=banner;
    }
    public boolean isOpen() {
        return "Open/Available".equals(status);
    }
    public String toString() {
        return title+"  |  "+showtime;
    }
}
