import java.util.HashMap;
import java.util.PriorityQueue;

public class Movie {

    private int index;
    private int year;
    private String title;
    private HashMap<Integer, Double> ratings;
    private double averageRating = CollaborativeFiltering.DEFAULT_RATING;
    private PriorityQueue<Neighbour<Movie>> neighbours;

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

    public PriorityQueue<Neighbour<Movie>> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(PriorityQueue<Neighbour<Movie>> neighbours) {
        this.neighbours = neighbours;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "index=" + index +
                ", year=" + year +
                ", title='" + title + '\'' +
                '}';
    }
}

