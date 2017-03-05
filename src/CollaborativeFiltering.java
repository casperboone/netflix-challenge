/* TU Delft
 * BSc Computer Science
 * TI2735-C Data Mining 2016-2017
 * Project Deliverable 01: Collaborative Filtering
 */

import java.lang.System;
import java.util.PriorityQueue;

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
            Rating predRating = predRatings.get(i);

            // Inform progress
            if (i % 10 == 0) {
                System.out.println("Running predictions " + (i + 1) + "/" + predRatings.size());
            }

            // Hard-code size of neighborhood
            int N = 1000;

            // Compute similarity with other users
            PriorityQueue<Neighbour<User>> neighbours = new PriorityQueue<>(N);
            for (User other : userList) {

                // Compute similarity
                double similarity = Util.calculateCosine(
                    Util.subtractAverage(predRating.getUser().getRatings()),
                    Util.subtractAverage(other.getRatings()),
                    nM
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

            // Construct weighted average
            // r_xi = [sum over all neighbours y: sim(x,y) * r_yi] / sum over all neighbours y: sim(x,y)]
            // In the average we only count those among the N neighbours who have rated i. (MMDS page 325)
            double numerator = 0.0;
            double denominator = 0.0;
            for (Neighbour<User> neighbour : neighbours) {
                Double neighbourRating = neighbour.getResource().getRatings().get(predRating.getMovie().getIndex()-1);
                if (neighbourRating != null) {
                    numerator += neighbour.getSimilarity() * neighbourRating;
                    denominator += neighbour.getSimilarity();
                }
            }

            double prediction = numerator / denominator;
            // If there were no neighbours we have rated i, we set the prediction to a default
            if (Double.isNaN(prediction)) {
                prediction = 2.5;
            }

//            System.out.println(i + " " + predRating.getUser().getIndex() + ": " +prediction);
            
            // Set predicted rating
            predRating.setRating(prediction);
        }
        System.out.println("done.");

        // Return predictions
        return predRatings;
    }
}
