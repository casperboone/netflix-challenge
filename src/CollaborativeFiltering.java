/* TU Delft
 * BSc Computer Science
 * TI2735-C Data Mining 2016-2017
 * Project Deliverable 01: Collaborative Filtering
 */

import java.lang.System;
import java.util.Arrays;
import java.util.Collections;

public class CollaborativeFiltering {

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
        predRatings.writeResultsFile("submission.csv");
    }

    public static RatingList predictRatings(UserList userList,
                                            MovieList movieList, RatingList ratingList, RatingList predRatings) {

        // Number of users and movies
        int nU = userList.size();
        int nM = movieList.size();

        // Loop over to-be-predicted ratings
        System.out.println("Running predictions..");
        for (int i = 0; i < predRatings.size(); i++) {

            // Inform progress
            if (i % 1000 == 0) {
                System.out.println("Running predictions " + (i + 1) + "/" + predRatings.size());
            }


            // Compute similarity with other users (tip: cosine similarity)
//			..CODE HERE..

            // Hard-code size of neighborhood
            int N = 25;

            // Construct weighted average
//			..CODE HERE..


            // Set predicted rating
            predRatings.get(i).setRating(prediction);
        }
        System.out.print("done.");

        // Return predictions
        return predRatings;
    }
}
