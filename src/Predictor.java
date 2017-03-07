import java.util.PriorityQueue;

public class Predictor extends Thread {
    private UserList userList;
    private MovieList movieList;
    private RatingList ratingList;
    private RatingList predRatings;
    private int start;
    private int end;
    private long startTime;

    public Predictor(UserList userList, MovieList movieList, RatingList ratingList, RatingList predRatings, int start, int end, long startTime) {
        this.userList = userList;
        this.movieList = movieList;
        this.ratingList = ratingList;
        this.predRatings = predRatings;
        this.start = start;
        this.end = end;
        this.startTime = startTime;
    }

    public void run() {
        // Loop over to-be-predicted ratings
        for (int i = start; i < end; i++) {
            Rating predRating = predRatings.get(i);

            // Hard-code size of neighborhood
            int N = 1000;

            // Compute similarity with other users
            if (predRating.getMovie().getNeighbours() == null) {
                PriorityQueue<Neighbour<Movie>> neighbours = new PriorityQueue<>(N);
                for (Movie other : movieList) {

                    // Compute similarity
                    double similarity = Util.calculateCosine(
                            Util.subtractAverage(predRating.getMovie().getRatings()),
                            Util.subtractAverage(other.getRatings())
                    );

                    // Make sure we only keep the top N
                    if (neighbours.size() < N) {
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

            // Construct weighted average
            // r_xi = b_xi + [sum over all neighbours y: sim(x,y) * (r_yi - b_yi)] / sum over all neighbours y: sim(x,y)]
            // In the average we only count those among the N neighbours who have rated i. (MMDS page 325)
            //TODO: use weighted sum with interpolation weights (slide 48)
            double numerator = 0.0;
            double denominator = 0.0;
            for (Neighbour<Movie> neighbour : predRating.getMovie().getNeighbours()) {
                Double neighbourRating = neighbour.getResource().getRatings().get(predRating.getUser().getIndex() - 1);
                if (neighbourRating != null) {
                    numerator += neighbour.getSimilarity() * (neighbourRating - getBaseline(ratingList, predRating.getUser(), neighbour.getResource()));
                    denominator += neighbour.getSimilarity();
                }
            }

            double prediction = getBaseline(ratingList, predRating.getUser(), predRating.getMovie())
                + numerator / denominator;

            // If there were no neighbours we have rated i, we set the prediction to a default
            if (Double.isNaN(prediction)) {
                prediction = CollaborativeFiltering.DEFAULT_RATING;
            }

//            System.out.println(i + " " + predRating.getUser().getIndex() + ": " +prediction);

            // Set predicted rating
            predRating.setRating(prediction);
        }
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
