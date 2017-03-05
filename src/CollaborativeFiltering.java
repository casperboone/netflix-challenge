/* TU Delft
 * BSc Computer Science
 * TI2735-C Data Mining 2016-2017
 * Project Deliverable 01: Collaborative Filtering
 */

import java.lang.System;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CollaborativeFiltering {

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
        RatingList predRatings = new RatingList();
        predRatings.readFile(args[0], userList, movieList);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        predictRatings(userList, movieList, ratings, predRatings);

        // Write result file
        predRatings.writeResultsFile("submission_"+System.currentTimeMillis()+".csv");
    }

    public static RatingList predictRatings(UserList userList,
                                            MovieList movieList, RatingList ratingList, RatingList predRatings) throws InterruptedException {
        // Compute mean of all ratings
        ratingList.computeAverage();

        // Compute mean rating per movie
        movieList.stream().forEach(Movie::computeAverage);

        // Compute mean rating per user
        userList.stream().forEach(User::computeAverage);


        long startTime = System.currentTimeMillis();


        int numberOfRatingsPerThread = 20;
        int maxThreads = 20;

        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        
        // Loop over to-be-predicted ratings
        System.out.println("Running predictions..");
        for (int i = 0; i < predRatings.size(); i += numberOfRatingsPerThread) {

            Predictor test = new Predictor(userList, movieList, ratingList, predRatings, i, i + numberOfRatingsPerThread, startTime);
            executorService.submit(test);
        }

        executorService.shutdown();
        while (! executorService.isTerminated()) {
            Thread.sleep(5000);
            long total = ((ThreadPoolExecutor) executorService).getTaskCount();
            long done = ((ThreadPoolExecutor) executorService).getCompletedTaskCount();
            long remaining = total - done;

            long secondsSoFar = ((System.currentTimeMillis() - startTime) / 1000);
            long expectedDuration = Math.round((secondsSoFar / (double) done) * remaining);
            System.out.print("Running predictions " + done + "/" + total);
            System.out.print(", so far it took " + secondsSoFar + " seconds. ");
            System.out.println("With " + remaining + " more items to go, it will probably take another " + expectedDuration + " seconds.");

        }

        System.out.println("Done, it took : " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds");

        // Return predictions
        return predRatings;
    }
}
