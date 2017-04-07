import java.io.*;
import java.util.List;

public class LatentFactorsFromFile {

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

        // Compute mean of all ratings (used for global bias)
        ratings.computeAverage();

        // Compute mean rating per movie (used for global bias)
        movieList.forEach(Movie::computeAverage);

        // Compute mean rating per user (used for global bias)
        userList.forEach(User::computeAverage);

        List<Matrix> pqFile = null;
        try (
                InputStream file = new FileInputStream("pq/gender_9f_04lbd_85535/final_pq.mat");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer)
        ) {
            pqFile = (List<Matrix>) input.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Matrix P = pqFile.get(0);
        Matrix Q = pqFile.get(1);


        System.out.print("Running predictions..");
        Util.lf.userList = userList;
        for (Rating rating : predRatings) {
            // Set to be predicted rating to dot(P[movie], Q[user]) + mean + user bias + movie bias
            rating.setRating(Util.lf.predictRating(ratings.getAverageRating(), rating, P, Q));
        }

        String filename = "submissions/submission_" + System.currentTimeMillis() + ".csv";
        System.out.println(filename);
        predRatings.writeResultsFile(filename);
    }
}
