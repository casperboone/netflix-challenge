/**
 * Perform cross validation with a variable sized, randomly chosen, validation set.
 */
public class LatentFactorsCrossValidation {
    public static void main(String[] args) throws InterruptedException {
        // Size of the validation set
        double percentage = 0.1;

        // Read user list
        UserList userList = new UserList();
        userList.readFile("data/users.csv");

        // Read movie list
        MovieList movieList = new MovieList();
        movieList.readFile("data/movies.csv");

        // List of ratings used for training
        RatingList trainingRatings = new RatingList();
        // List of to be predicted validation ratings with a rating set to 0
        RatingList validationUnknownRatings = new RatingList();
        // List of validation ratings with its actual rating value
        RatingList validationKnownRatings = new RatingList();

        // Original ratings before splitting up in different sets
        RatingList orgiginalRatings = new RatingList();
        orgiginalRatings.readFile("data/ratings.csv", userList, movieList);

        for (Rating rating : orgiginalRatings) {
            // Generate random number between 0 and 1, and add the rating to the validation set
            // if it below the percentage. Else add it to the training set
            if (Math.random() <= percentage) {
                validationUnknownRatings.add(new Rating(rating.getUser(), rating.getMovie(), 0.0));
                validationKnownRatings.add(rating);
            } else {
                trainingRatings.add(rating);
            }
        }

        // Add ratings to user and movie lists
        userList.addRatings(trainingRatings);
        movieList.addRatings(trainingRatings);

        // Make validation sets available in latent factors
        LatentFactors.validationSetKnown = validationKnownRatings;
        LatentFactors.validationSetUnknown = validationUnknownRatings;

        // Perform rating predictions (validation RMSE is displayed during running as well)
        Util.lf.predictRatings(userList, movieList, trainingRatings, validationUnknownRatings);

        // Output final RMSE of validation set
        System.out.println("Validation RMSE: " + Util.rmse(validationUnknownRatings, validationKnownRatings));
    }
}
