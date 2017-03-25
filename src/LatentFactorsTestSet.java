/**
 * Very simple cross validation with a non random, fixed size, validation
 * set. Convenient for getting accurate feedback if something is still
 * working or to determine that a certain improvement is not the
 * cause of random behaviour.
 */
public class LatentFactorsTestSet {
    public static void main(String[] args) throws InterruptedException {
        int start = 819171;
        int end = 910190;

        // Read user list
        UserList userList = new UserList();
        userList.readFile("data/users.csv");

        // Read movie list
        MovieList movieList = new MovieList();
        movieList.readFile("data/movies.csv");

        // Read rating list
        RatingList ratings = new RatingList();
        ratings.readFile("data/ratings.csv", userList, movieList, true, 0, start);

        // Make predictions file
        RatingList predRatings = new RatingList();
        predRatings.readFile("data/ratings.csv", userList, movieList, false, start, end);

        // Add ratings to user and movie lists
        userList.addRatings(ratings);
        movieList.addRatings(ratings);

        // Perform rating predictions
        LatentFactors.predictRatings(userList, movieList, ratings, predRatings);

        RatingList actualRatings = new RatingList();
        actualRatings.readFile("data/ratings.csv", userList, movieList, true, start, end);

        System.out.println("RMSE: " + Util.rmse(predRatings, actualRatings));
    }
}
