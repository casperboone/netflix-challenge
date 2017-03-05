import java.util.HashMap;

public class Movie {

    private int index;
    private int year;
    private String title;
    private HashMap<Integer, Double> ratings;
    private double averageRating = 2.5;

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

    /**
     * Compute average rating of movie.
     */
    public void computeAverage() {
        averageRating = Util.calculateAverage(ratings);
    }

    /**
     * Get the average rating.
     *
     * @return Movie average rating.
     */
    public double getAverageRating() {
        return averageRating;
    }

}

