import java.util.HashMap;

public class Movie {

    private int index;
    private int year;
    private String title;
    private HashMap<Integer, Double> ratings;
    private double averageRating;
    private HashMap<Movie, Neighbour<Movie>> otherMovies;

    public Movie(int _index, int _year, String _title) {
        this.index = _index;
        this.year = _year;
        this.title = _title;
        this.ratings = new HashMap<>();
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

    public double getBias(double overallMean) {
        if (Double.isNaN(averageRating)) {
            return 0.0;
        }
        return averageRating - overallMean;
    }

    /**
     * Get distances (similary) from this movie to all other movies.
     */
    public HashMap<Movie, Neighbour<Movie>> getOtherMovies() {
        return otherMovies;
    }

    /**
     * Set distances (similary) from this movie to all other movies.
     */
    public void setOtherMovies(HashMap<Movie, Neighbour<Movie>> otherMovies) {
        this.otherMovies = otherMovies;
    }

}

