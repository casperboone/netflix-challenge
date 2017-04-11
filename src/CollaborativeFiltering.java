import java.lang.System;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Main Collaborative Filtering class. Actual predictions are done
 * by instances of the Predictor class. This class prepares,
 * manages and coordinates the predictors.
 */
public class CollaborativeFiltering {
    public final static int NEIGHBOURHOOD_SIZE = 25;
    public final static int MAX_THREADS = 20;
    public final static int RATINGS_PER_THREAD = 20;

    public static void main(String[] args) throws InterruptedException {

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
        String predictionsFile = "data/predictions.csv";
        if (args.length >= 1) {
            predictionsFile = args[0];
        }
        RatingList predRatings = new RatingList();
        predRatings.readFile(predictionsFile, userList, movieList);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        predictRatings(userList, movieList, ratings, predRatings);

        // Write result file
        predRatings.writeResultsFile("submissions/submission_" + System.currentTimeMillis() + ".csv");
    }

    public static RatingList predictRatings(UserList userList,
                                            MovieList movieList, RatingList ratingList, RatingList predRatings) throws InterruptedException {
        // Compute mean of all ratings (used for global bias)
        ratingList.computeAverage();

        // Compute mean rating per movie (used for global bias)
        movieList.forEach(Movie::computeAverage);

        // Compute mean rating per user (used for global bias)
        userList.forEach(User::computeAverage);

        long startTime = System.currentTimeMillis();

        // Use an executor service to make sure there never run more than maxThreads threads at a single time
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);

        System.out.println("Running predictions..");

        // Loop over to-be-predicted ratings and divide them over the multiple threads
        for (int i = 0; i < predRatings.size(); i += RATINGS_PER_THREAD) {
            // create a predictor (new thread) that computes the to be computes rating i till i + numberOfRatingsPerThread
            executorService.submit(new Predictor(userList, movieList, ratingList, predRatings, i, i + RATINGS_PER_THREAD));
        }

        // Stop executor service and wait till it is finished
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // Give a status update every 5 seconds
            Thread.sleep(5000);
            giveStatusUpdate(startTime, executorService);
        }

        System.out.println("Done, it took " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");

        // Return predictions
        return predRatings;
    }

    private static void giveStatusUpdate(long startTime, ExecutorService executorService) {
        long total = ((ThreadPoolExecutor) executorService).getTaskCount();
        long done = ((ThreadPoolExecutor) executorService).getCompletedTaskCount();
        long remaining = total - done;

        long secondsSoFar = ((System.currentTimeMillis() - startTime) / 1000);
        System.out.print("Running predictions " + done + "/" + total);
        System.out.print(", so far it took " + secondsSoFar + " seconds. ");
        System.out.println("Only " + remaining * RATINGS_PER_THREAD + " more items to go.");
    }
}
