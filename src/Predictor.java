import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class Predictor extends Thread {
    private UserList userList;
    private MovieList movieList;
    private RatingList ratingList;
    private RatingList predRatings;
    private int start;
    private int end;

    public Predictor(UserList userList, MovieList movieList, RatingList ratingList, RatingList predRatings, int start, int end) {
        this.userList = userList;
        this.movieList = movieList;
        this.ratingList = ratingList;
        this.predRatings = predRatings;
        this.start = start;
        this.end = end;
    }

    public void run() {
        // Loop over to-be-predicted ratings
        for (int i = start; i < end; i++) {
            Rating predRating = predRatings.get(i);

            // Compute similarity with other users
            computeSimilarityIfUnknown(predRating, movieList);

            // Construct weighted average and set it to the item
            predRating.setRating(computeWeighedAverage(predRating, ratingList));
        }
    }

    private static void computeSimilarityIfUnknown(Rating predRating, MovieList movieList) {
        if (predRating.getMovie().getNeighbours() != null) {
            return;
        }

        PriorityQueue<Neighbour<Movie>> neighbours = new PriorityQueue<>(CollaborativeFiltering.NEIGHBOURHOOD_SIZE);

        for (Movie other : movieList) {
            // Compute similarity
            double similarity = Util.calculateCosine(
                    Util.subtractAverage(predRating.getMovie().getRatings()),
                    Util.subtractAverage(other.getRatings())
            );

            // double similarity = jaccardDistance(predRating.getMovie(), other);
            // double similarity = pearsonCorrelationCoefficient(predRating.getMovie(), other);

            // Make sure we only keep the top N
            if (neighbours.size() < CollaborativeFiltering.NEIGHBOURHOOD_SIZE) {
                neighbours.add(new Neighbour<>(other, similarity));
                continue;
            }

            // If the current size is > N, check if the first element has a lower similarity
            if (neighbours.peek().getSimilarity() < similarity) {
                neighbours.poll();
                neighbours.add(new Neighbour<>(other, similarity));
            }
        }
        predRating.getMovie().setNeighbours(neighbours);
    }

    /**
     * Compute the weighed average which is the new rating of an item.
     * r_xi = b_xi + [sum over all neighbours y: sim(x,y) * (r_yi - b_yi)] / sum over all neighbours y: sim(x,y)]
     * In the average we only count those among the N neighbours who have rated i. (MMDS page 325)
     *
     * @param predRating
     * @param ratingList
     * @return Weighed average
     */
    private static double computeWeighedAverage(Rating predRating, RatingList ratingList) {
        //TODO: use weighted sum with interpolation weights (slide 48)
        double numerator = 0.0;
        double denominator = 0.0;

        for (Neighbour<Movie> neighbour : predRating.getMovie().getNeighbours()) {
            Double neighbourRating = neighbour.getResource().getRatings().get(predRating.getUser().getIndex() - 1);

            // if the neighbour has not rated the item, skip the neighbour
            if (neighbourRating != null) {
                numerator += neighbour.getSimilarity() * (neighbourRating - getBaseline(ratingList, predRating.getUser(), neighbour.getResource()));
                denominator += neighbour.getSimilarity();
            }
        }

        double prediction = getBaseline(ratingList, predRating.getUser(), predRating.getMovie())
                + numerator / denominator;

        // If there were no neighbours we have rated i, we set the prediction to a default
        if (Double.isNaN(prediction)) {
            return ratingList.getAverageRating();
        }

        return prediction;
    }

    /**
     * Compute the pearson correlation coefficient.
     *
     * @param movieX
     * @param movieY
     * @return Pearson correlation coefficient
     */
    private static double pearsonCorrelationCoefficient(Movie movieX, Movie movieY) {
        // users from movieX.getRatings() intersect users from movieY.getRatings();
        Set<Integer> usersThatLikeXAndY = new HashSet<>(movieX.getRatings().keySet());
        usersThatLikeXAndY.retainAll(movieY.getRatings().keySet());

        double numerator = 0.0;
        double denominatorLeft = 0.0;
        double denominatorRight = 0.0;
        for (Integer user : usersThatLikeXAndY) {
            // (r_xs - avg(r_x))(r_ys - avg(r_y)) [slide 27]
            numerator += (movieX.getRatings().get(user) - movieX.getAverageRating()) * (movieY.getRatings().get(user) - movieY.getAverageRating());
            // (r_xs - avg(r_x))^2
            denominatorLeft += Math.pow(movieX.getRatings().get(user) - movieX.getAverageRating(), 2);
            // (r_ys - avg(r_y))^2
            denominatorRight += Math.pow(movieY.getRatings().get(user) - movieY.getAverageRating(), 2);
        }

        denominatorLeft = Math.sqrt(denominatorLeft);
        denominatorRight = Math.sqrt(denominatorRight);

        return numerator / (denominatorLeft * denominatorRight);
    }

    /**
     * Compute the Jaccard the distance between two movies.
     *
     * @param movieX
     * @param movieY
     * @return Jaccard distance
     */
    private static double jaccardDistance(Movie movieX, Movie movieY) {
        // users from movieX.getRatings() intersect users from movieY.getRatings();
        Set<Integer> usersThatLikeXAndY = new HashSet<>(movieX.getRatings().keySet());
        usersThatLikeXAndY.retainAll(movieY.getRatings().keySet());

        // users in common / union
        return usersThatLikeXAndY.size() / (movieX.getRatings().size() + movieY.getRatings().size() - usersThatLikeXAndY.size());
    }

    /**
     * Compute baseline estimate: u + (avg rating movie i - u) + (avg rating user x - u).
     *
     * @param ratingList
     * @param movie
     * @param user
     * @return Baseline estimate
     */
    private static double getBaseline(RatingList ratingList, User user, Movie movie) {
        return ratingList.getAverageRating()
                + (movie.getAverageRating() - ratingList.getAverageRating())
                + (user.getAverageRating() - ratingList.getAverageRating());
    }
}
