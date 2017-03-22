import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class User {

    private int index;
    private int age;
    private int profession;
    private boolean male;
    private HashMap<Integer, Double> ratings;
    private HashMap<Integer, Double> normalizedRatings;
    private double averageRating;

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

    public void normalizeRatings() {
        computeAverage();

        normalizedRatings = new HashMap<>();

        for (Map.Entry<Integer, Double> movieRating : ratings.entrySet()) {
            normalizedRatings.put(movieRating.getKey(), movieRating.getValue() - averageRating);
        }
    }

    public HashMap<Integer, Double> getNormalizedRatings() {
        return normalizedRatings;
    }
}
