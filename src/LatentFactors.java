import java.io.*;
import java.lang.System;
import java.util.*;

/**
 * Latent factor model.
 */
public class LatentFactors {

    // Number of factors
    static int nF = 9;

    // Regularization parameters
    static double lambdaP = 0.04;
    static double lambdaQ = 0.04;

    // Learning rate
    static double learningRate = 0.02;

    // Tolerance value. If difference between the average of the last two RMSEs of the
    // training set and the current RMSE, is smaller than this value, we stop.
    static double xTolerance = 0.0001;

    // Maximum number of iterations
    static int maxIterations = 100;

    // Validation set. Used to display validation RMSEs while running the algorithms.
    // These sets are set in LatentFactorsCrossValidation.
    static RatingList validationSetKnown = null;
    static RatingList validationSetUnknown = null;

    // A list with the validation RMSE per iteration. Used to trace back the performance.
    static List<Double> validationStatistics = new ArrayList<>();

    public UserList userList = null;
    protected MovieList movieList = null;
    protected RatingList ratings = null;
    protected RatingList predRatings = null;

    /**
     * Entry point of LatentFactors.
     */
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
        Util.lf.predictRatings(userList, movieList, ratings, predRatings);

        // Write result file
        String filename = "submissions/submission_" + System.currentTimeMillis() + ".csv";
        System.out.println(filename);
        predRatings.writeResultsFile(filename);
    }

    /**
     * Predict ratings using latent factors.
     */
    public RatingList predictRatings(UserList userList,
                                     MovieList movieList, RatingList ratingList, RatingList predRatings) {
        this.userList = userList;
        this.movieList = movieList;
        this.ratings = ratingList;
        this.predRatings = predRatings;

        // Compute mean of all ratings (used for global bias)
        ratingList.computeAverage();

        // Compute mean rating per movie (used for global bias)
        movieList.forEach(Movie::computeAverage);

        // Compute mean rating per user (used for global bias)
        userList.forEach(User::computeAverage);

        // Number of users and movies
        int nU = userList.size();
        int nM = movieList.size();

        // We don't explicitly normalize here. The way the algorithm (specifically
        // the error function)is constructed already handles this.

        System.out.println("Starting run with " + nF + " factors and lambda " + lambdaP);

        // Track start time to determine run time
        long start = System.currentTimeMillis();

        // Initialize factors
        Matrix P = Util.initializeLatentFactor(nM, nF); // movies
        Matrix Q = Util.initializeLatentFactor(nU, nF); // users

        // Save initial P and Q, used for debugging and backtracking to optimal points
        writePQToFile(P, Q, "initial");

        // P and Q of previous iterations. Used during manual interactive
        // training to go back to a previous state.
        List<Matrix> oldP = new ArrayList<>();
        List<Matrix> oldQ = new ArrayList<>();

        // Keep track of the last two RMSE training set scores
        double[] previousRMSE = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};

        // Perform optimization over at most maxIterations
        for (int i = 0; i < maxIterations; i++) {
            if (i > 25) {
                learningRate = 0.002;
            }

            // Add current P and Q to the history lists.
            oldP.add(P.duplicate());
            oldQ.add(Q.duplicate());

            // Update P and Q for every rating
            for (Rating rating : ratingList) {
                int userIndex = rating.getUser().getIndex() - 1;
                int movieIndex = rating.getMovie().getIndex() - 1;

                // error = actual rating - predicted rating
                //       = actual rating - ( dot(P[movie], Q[user]) + mean + user bias + movie bias )
                double error = rating.getRating() - predictRating(ratingList.getAverageRating(), rating, P, Q);

                // Temporarily save the current values of Q for this user, so we have
                // the correct values when we want to start updating P (the Q value
                // will then already have been changed).
                Map<Integer, Double> qTemp = new HashMap<>(Q.get(userIndex));

                // Update value in Q for the current user, for each factor.
                // New value for Q[user][factor] = (old Q[user][factor])
                //      + learningRate * (error * P[movie][factor] - lambda * (old Q[user][factor]))
                for (int k = 0; k < nF; k++) {
                    double descentQ = error * P.get(movieIndex).get(k) - lambdaQ * Q.get(userIndex).get(k);
                    double qValue = Q.get(userIndex).get(k) + learningRate * descentQ;
                    Q.get(userIndex).put(k, qValue);
                }

                // Update value in P for the current movie, for each factor.
                // New value for P[movie][factor] = (old P[movie][factor])
                //      + learningRate * (error * Q[user][factor] - lambda * (old P[movie][factor]))
                for (int k = 0; k < nF; k++) {
                    double descentP = error * qTemp.get(k) - lambdaP * P.get(movieIndex).get(k);
                    double pValue = P.get(movieIndex).get(k) + learningRate * descentP;
                    P.get(movieIndex).put(k, pValue);
                }
            }

            // Compute RMSE of training set
            double RMSE = Util.rmseKnownRatings(this, ratingList, P, Q);
            // If difference between the average of the last two RMSEs of the training set
            // and the current RMSE, is smaller than this value, we stop training.
            if (Math.abs(((previousRMSE[0] + previousRMSE[1]) / 2) - RMSE) <= xTolerance) {
                break;
            }
            // Keep track of last two RMSE scores.
            previousRMSE[0] = previousRMSE[1];
            previousRMSE[1] = RMSE;

            // Output current iteration and RMSE score
            System.out.println(i + "  " + RMSE);

            // Compute and output RMSE of validation set, if available
            double validationRMSE = computeValidationSetRMSE(ratingList, Q, P);
            System.out.println("Validation set RMSE: " + computeValidationSetRMSE(ratingList, Q, P));
            validationStatistics.add(validationRMSE);

            // Save P and Q, used for debugging and backtracking to optimal points
            writePQToFile(P, Q, Integer.toString(i));
        }

        // Save P and Q, used for debugging and backtracking to optimal points
        writePQToFile(P, Q, "final");

        // Loop over to-be-predicted ratings to predict them
        System.out.print("Running predictions..");
        for (Rating rating : predRatings) {
            // Set to be predicted rating to dot(P[movie], Q[user]) + mean + user bias + movie bias
            rating.setRating(predictRating(ratingList.getAverageRating(), rating, P, Q));
        }

        // Output run time
        System.out.println("done. It took " + 0.001 * (System.currentTimeMillis() - start));

        // Return predictions
        return predRatings;
    }

    /**
     * Predict the current rating of a certain movie/user pair with the following formula:
     * rating = dot(P[movie], Q[user]) + mean + user bias + movie bias
     * <p>
     * We cap the rating if it is below 1 and above 5 to its boundary.
     */
    public double predictRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        double rating = computeRating(mean, ratingDetails, P, Q);

        if (rating > 5) {
            return 5;
        }
        if (rating < 1) {
            return 1;
        }

        return rating;
    }

    /**
     * Computes the current rating of a certain movie/user pair with the following formula:
     * rating = dot(P[movie], Q[user]) + mean + user bias + movie bias
     */
    protected double computeRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        return mean + ratingDetails.getUser().getBias(mean) + ratingDetails.getMovie().getBias(mean)
                + Util.innerProduct(Q.get(ratingDetails.getUser().getIndex() - 1), P.get(ratingDetails.getMovie().getIndex() - 1));
    }

    /**
     * Compute the RMSE of the validation set (if we have a validation set).
     */
    protected double computeValidationSetRMSE(RatingList ratingList, Matrix Q, Matrix P) {
        if (validationSetKnown == null || validationSetUnknown == null) {
            return -1;
        }

        // Predict the ratings for all items in the validation set.
        for (Rating rating : validationSetUnknown) {
            rating.setRating(predictRating(ratingList.getAverageRating(), rating, P, Q));
        }

        return Util.rmse(validationSetUnknown, validationSetKnown);
    }

    /**
     * Write P and Q to file, used for debugging and backtracking to optimal points.
     */
    public static void writePQToFile(Matrix P, Matrix Q, String name) {
        List<Matrix> pqList = new ArrayList<>();
        pqList.add(P);
        pqList.add(Q);

        try (
                OutputStream file = new FileOutputStream("pq/" + name + "_pq.mat");
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer)
        ) {
            output.writeObject(pqList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

