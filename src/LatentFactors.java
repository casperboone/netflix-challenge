/* TU Delft
 * BSc Computer Science
 * TI2735-C Data Mining 2016-2017
 * Project Deliverable 02: Latent Factors
 */

import java.lang.System;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LatentFactors {

    public static void main(String[] args) {

        // Read user list
        UserList userList = new UserList();
        userList.readFile("data/users.csv");

        // Read movie list
        MovieList movieList = new MovieList();
        movieList.readFile("data/movies.csv");

        // Read rating list
        RatingList ratings = new RatingList();
        ratings.readFile("data/ratings.csv", userList, movieList);

        // Make predictions file
        RatingList predRatings = new RatingList();
        predRatings.readFile("data/predictions.csv", userList, movieList);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        predictRatings(userList, movieList, ratings, predRatings);

        // Write result file
        String filename = "submissions/submission_" + System.currentTimeMillis() + ".csv";
        System.out.println(filename);
        predRatings.writeResultsFile(filename);
    }

    public static RatingList predictRatings(UserList userList,
                                            MovieList movieList, RatingList ratingList, RatingList predRatings) {
        // Compute mean of all ratings (used for global bias)
        ratingList.computeAverage();

        // Compute mean rating per movie (used for global bias)
        movieList.forEach(Movie::computeAverage);

        // Compute mean rating per user (used for global bias)
        userList.forEach(User::computeAverage);

        // Number of users and movies
        int nU = userList.size();
        int nM = movieList.size();

        // Normalize ratings for every user separately
//        userList.get(0).computeAverage();
//        System.out.println(userList.get(0).getAverageRating());
//        System.out.println(userList.get(0).getRatings());
//        userList.forEach(User::normalizeRatings); //maybe also for movies, see page 334
//        System.out.println(userList.get(0).getNormalizedRatings());

        // Number of factors
        int nF = 18; // 0.88057,

        // Regularization parameters
        double lambdaQ = 0.15;
        double lambdaP = 0.15;
        double lambdaUserBias = 0.05;
        double lambdaMovieBias = 0.05;

        // Optimization parameters
        double learningRate = 0.01;

        double xTolerance = 0.0001;
        int maxIterations = 80;
        double RMSE_ = Double.POSITIVE_INFINITY;

        // Initialize random number generator
        Random rng = new Random();

        // Initialize factors
        Map<Integer, Map<Integer, Double>> Q = Util.initializeLatentFactor(nU, nF); // users
        Map<Integer, Map<Integer, Double>> P = Util.initializeLatentFactor(nM, nF); // movies

        // Perform optimization
        double previousRMSE = Double.POSITIVE_INFINITY;
        for (int i = 0; i < maxIterations; i++) {
            for (Rating rating : ratingList) {
                int userIndex = rating.getUser().getIndex() - 1; // i
                int movieIndex = rating.getMovie().getIndex() - 1; // x

                double userBias = rating.getUser().getBias(ratingList.getAverageRating());
                double movieBias = rating.getMovie().getBias(ratingList.getAverageRating());

                double error = rating.getRating() - (
                        Util.innerProduct(Q.get(userIndex), P.get(movieIndex))
                                + ratingList.getAverageRating()
                                + userBias
                                + movieBias
                );

                double effects = lambdaMovieBias * movieBias + lambdaUserBias * userBias;

                Map<Integer, Double> qTemp = new HashMap<>(Q.get(userIndex));

                for (int k = 0; k < nF; k++) {
                    double descentQ = error * P.get(movieIndex).get(k) - lambdaQ * Q.get(userIndex).get(k) - effects;
                    double qValue = Q.get(userIndex).get(k) + learningRate * descentQ;
                    Q.get(userIndex).put(k, qValue);
                }
                for (int k = 0; k < nF; k++) {
                    double descentP = error * qTemp.get(k) - lambdaP * P.get(movieIndex).get(k) - effects;
                    double pValue = P.get(movieIndex).get(k) + learningRate * descentP;
                    P.get(movieIndex).put(k, pValue);
                }
            }

            double RMSE = Util.rootMeanSquaredError(userList, movieList, userList, Q, P, ratingList.getAverageRating());
            if (Math.abs(previousRMSE-RMSE) <= xTolerance) {
                break;
            }
            System.out.println(i + "  " + RMSE);
            previousRMSE = RMSE;
        }

        // Loop over to-be-predicted ratings
        System.out.print("Running predictions..");
        for (Rating rating : predRatings) {
            double a = ratingList.getAverageRating() + rating.getUser().getBias(ratingList.getAverageRating()) + rating.getMovie().getBias(ratingList.getAverageRating())
                    + Util.innerProduct(Q.get(rating.getUser().getIndex() - 1), P.get(rating.getMovie().getIndex() - 1));
            if (Double.isNaN(a)) {
                System.out.println("stuk");
            }
            rating.setRating(fixRating(a));
        }
        System.out.print("done.");

        // Return predictions
        return predRatings;
    }

    private static double fixRating(double rating) {
        if (rating > 5) {
            return 5;
        }
        if (rating < 1) {
            return 1;
        }
        return rating;
    }
}
