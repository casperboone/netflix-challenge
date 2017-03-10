/**
 * Created by casperboone on 05-03-17.
 */
public class CollaborativeFilteringTestSet {
    public static void main(String[] args) throws InterruptedException {
        // Read user list
        UserList userList = new UserList();
        userList.readFile("data/users.csv");

        // Read movie list
        MovieList movieList = new MovieList();
        movieList.readFile("data/movies.csv");

        // Read rating list
        RatingList ratings = new RatingList();
        ratings.readFile("data/ratings.csv", userList, movieList, true, 90000, Integer.MAX_VALUE);

        // Make predictions file
        RatingList predRatings = new RatingList();
        predRatings.readFile("data/ratings.csv", userList, movieList, false, 0, 90000);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        CollaborativeFiltering.predictRatings(userList, movieList, ratings, predRatings);

        RatingList actualRatings = new RatingList();
        actualRatings.readFile("data/ratings.csv", userList, movieList, true, 0, 90000);

        System.out.println("RMSE: " + Util.rmse(predRatings, actualRatings));
    }
}
