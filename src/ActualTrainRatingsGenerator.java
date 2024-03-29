/**
 * Created by casperboone on 07-04-17.
 */
public class ActualTrainRatingsGenerator {

    public static void main(String[] args) {
        // Read user list
        UserList userList = new UserList();
        userList.readFile("data/users.csv");

        // Read movie list
        MovieList movieList = new MovieList();
        movieList.readFile("data/movies.csv");
        
        // Read ratings
        RatingList ratings = new RatingList();
        ratings.readFile("data/ratings.csv", userList, movieList);
        // Write ratings to file in submission format
        ratings.writeResultsFile("stacking/actual_train_ratings.csv");
    }
}
