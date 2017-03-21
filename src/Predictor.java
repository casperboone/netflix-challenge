import java.util.*;

/**
 * This class predicts all movie rating for a given range of the predRatings list.
 */
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
            PriorityQueue<Neighbour<Movie>> neighbours = computeSimilarity(predRating, movieList);

            // Construct weighted average and set it to the item
            predRating.setRating(computeWeighedAverage(predRating, ratingList, neighbours));
        }
    }

    private static PriorityQueue<Neighbour<Movie>> computeSimilarity(Rating predRating, MovieList movieList) {
        // Pre-compute and cache the distance to all other movies
        computeSimilaritiesToOtherMoviesIfUnknown(predRating.getMovie(), movieList);

        PriorityQueue<Neighbour<Movie>> neighbours = new PriorityQueue<>(CollaborativeFiltering.NEIGHBOURHOOD_SIZE);

        // Loop over all other movies with precomputed similarity of the movie
        for (Neighbour<Movie> other : predRating.getMovie().getOtherMovies().values()) {
            // If movie does not have a rating by the user of predRating, skip it
            if (other.getResource().getRatings().get(predRating.getUser().getIndex() - 1) == null) {
                continue;
            }

            // Add the first N to the priority queue
            if (neighbours.size() < CollaborativeFiltering.NEIGHBOURHOOD_SIZE) {
                neighbours.add(other);
                continue;
            }

            // If the current size is > N, check if the first element has a lower similarity. If so, replace it.
            if (neighbours.peek().getSimilarity() < other.getSimilarity()) {
                neighbours.poll();
                neighbours.add(other);
            }
        }

        return neighbours;
    }

    /**
     * Compute all distance to other movies of a certain movie (if we have not done this yet). This is useful
     * so that we can 'cache' all the similarities. After having computed them once,
     * the program runs very fast (about 40k ratings per 5 seconds).
     *
     * @param movie
     * @param movieList
     */
    private static void computeSimilaritiesToOtherMoviesIfUnknown(Movie movie, MovieList movieList) {
        // If we have already computed the distance to all other movies, we can stop
        if (movie.getOtherMovies() != null) {
            return;
        }

        HashMap<Movie, Neighbour<Movie>> neighbours = new HashMap<>();

        for (Movie other : movieList) {
            // Compute cosine similarity to other movies
            double similarity = Util.calculateCosine(
                    Util.subtractAverage(movie.getRatings()),
                    Util.subtractAverage(other.getRatings())
            );

            if (Double.isNaN(similarity)) {
                similarity = 0.0;
            }

            neighbours.put(other, new Neighbour<>(other, similarity));
        }

        movie.setOtherMovies(neighbours);
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
    private static double computeWeighedAverage(Rating predRating, RatingList ratingList, PriorityQueue<Neighbour<Movie>> neighbours) {
        double numerator = 0.0;
        double denominator = 0.0;

        // Sum over all neighbours [see formula in javadoc]
        for (Neighbour<Movie> neighbour : neighbours) {
            Double neighbourRating = neighbour.getResource().getRatings().get(predRating.getUser().getIndex() - 1);

            // sim(x,y) * (r_yi - b_yi) [see formula in javadoc]
            numerator += neighbour.getSimilarity() * (neighbourRating - getBaseline(ratingList, predRating.getUser(), neighbour.getResource()));
            // sim(x,y)  [see formula in javadoc]
            denominator += neighbour.getSimilarity();
        }

        // If the denominator is smaller than 0.01, for instance when there were no similar movies
        if (Math.abs(denominator) < 0.001) {
            return ratingList.getAverageRating();
        }

        // r_xi = b_xi + ... [see formula in javadoc]
        return getBaseline(ratingList, predRating.getUser(), predRating.getMovie()) + numerator / denominator;
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
}
