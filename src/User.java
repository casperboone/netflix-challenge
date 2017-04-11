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
    private HashMap<User, Neighbour<User>> otherUsers;

    public User(int index, boolean male, int age, int profession) {
        this.index = index;
        this.male = male;
        this.age = age;
        this.profession = profession;
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

    /**
     * Compute user bias, the user average rating - the overall average rating.
     */
    public double getBias(double overallMean) {
        if (Double.isNaN(averageRating)) {
            return 0.0;
        }
        return averageRating - overallMean;
    }

    /**
     * Normalize ratings (subtract user average).
     */
    public void normalizeRatings() {
        computeAverage();

        normalizedRatings = new HashMap<>();

        for (Map.Entry<Integer, Double> movieRating : ratings.entrySet()) {
            normalizedRatings.put(movieRating.getKey(), movieRating.getValue() - averageRating);
        }
    }

    /**
     * Get (already) normalized ratings.
     */
    public HashMap<Integer, Double> getNormalizedRatings() {
        return normalizedRatings;
    }


    /**
     * Get distances (similary) from this movie to all other movies.
     */
    public HashMap<User, Neighbour<User>> getOtherUsers() {
        return otherUsers;
    }

    /**
     * Set distances (similary) from this movie to all other movies.
     */
    public void setOtherUsers(HashMap<User, Neighbour<User>> otherUsers) {
        this.otherUsers = otherUsers;
    }
}
