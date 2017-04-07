
public class CollaborativeFilteringTrainRatingsGenerator {
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
        predRatings.readFile("data/ratings.csv", userList, movieList, false, 0, Integer.MAX_VALUE);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        CollaborativeFiltering.predictRatings(userList, movieList, ratings, predRatings);

        predRatings.writeResultsFile("stacking/collaborative_filtering.csv");
    }
}
