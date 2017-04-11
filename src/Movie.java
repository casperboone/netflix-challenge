import java.util.HashMap;
import java.util.Map;

public class Movie {

    private int index;
    private int year;
    private String title;
    private HashMap<Integer, Double> ratings;
    private double averageRating;
    private HashMap<Movie, Neighbour<Movie>> otherMovies;
    private int totalMale = -1;
    private int totalFemale = -1;
    private double averageAge = Double.NEGATIVE_INFINITY;

    public Movie(int index, int year, String title) {
        this.index = index;
        this.year = year;
        this.title = title;
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

    /**
     * Get total male viewers that rated higher than 3.
     */
    public int getTotalMale(UserList users) {
        if (totalMale == -1) {
            totalMale = 0;
            for (Map.Entry<Integer, Double> rating : ratings.entrySet()) {
                User user = users.get(rating.getKey());
                if (user.isMale() && rating.getValue() > 3) {
                    totalMale++;
                }
            }
        }
        return totalMale;
    }
    /**
     * Get total female viewers that rated higher than 3.
     */
    public int getTotalFemale(UserList users) {
        if (totalFemale == -1) {
            totalFemale = 0;
            for (Map.Entry<Integer, Double> rating : ratings.entrySet()) {
                User user = users.get(rating.getKey());
                if (!user.isMale() && rating.getValue() > 3) {
                    totalFemale++;
                }
            }
        }
        return totalFemale;
    }

    /**
     * Get average age of people that rated higher than 3.
     */
    public double getAverageAge(UserList users) {
        if (Double.isInfinite(averageAge)) {
            int count = 0;
            int total = 0;
            for (Map.Entry<Integer, Double> rating : ratings.entrySet()) {
                User user = users.get(rating.getKey());
                if (rating.getValue() > 3 && user.getAge() != 1) {
                    total += user.getAge();
                    count++;
                }
            }
            averageAge = total / (double) count;
        }
        return averageAge;
    }
}

