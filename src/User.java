import java.util.HashMap;
import java.util.PriorityQueue;

public class User {

    private int index;
    private int age;
    private int profession;
    private boolean male;
    private HashMap<Integer, Double> ratings;
    private double averageRating;
    private PriorityQueue<Neighbour<User>> neighbours;

    public User(int _index, boolean _male, int _age, int _profession) {
        this.index = _index;
        this.male = _male;
        this.age = _age;
        this.profession = _profession;
        this.ratings = new HashMap<>();
    }

    public void putRating(Integer movie, double rating) {
        ratings.put(movie, rating);
    }

    public HashMap<Integer, Double> getRatings() {
        return ratings;
    }

    public int getIndex() {
        return index;
    }

    public boolean isMale() {
        return male;
    }

    public int getAge() {
        return age;
    }

    public int getProfession() {
        return profession;
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

    public PriorityQueue<Neighbour<User>> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(PriorityQueue<Neighbour<User>> neighbours) {
        this.neighbours = neighbours;
    }
}
