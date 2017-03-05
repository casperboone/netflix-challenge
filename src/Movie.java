import java.util.HashMap;

public class Movie {

    int index;
    int year;
    String title;
    HashMap<Integer, Double> ratings;

    public Movie(int _index, int _year, String _title) {
        this.index = _index;
        this.year = _year;
        this.title = _title;
        this.ratings = new HashMap<Integer, Double>();
    }

    public void putRating(Integer user, Double rating) {
        ratings.put(user, rating);
    }

    public HashMap<Integer, Double> getRatings() {
        return ratings;
    }

    public int getIndex() {
        return index;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

}

