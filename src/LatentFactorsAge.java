/**
 * Latent Factors with age bias. Before running this change Util.lf to this class.
 */
public class LatentFactorsAge extends LatentFactors {
    static double lambdaAge = 0.25;

    // (1 * (max - min)) / 3
    static double ageDifference = (1 * (56 - 18)) / 3;

    /**
     * Ajusts ratings based on the age of the users with prediction rule
     * r = originalR + λage∗(1−|age−avgAge|(maxAge−minAge)/3), where
     * avgAge is the average age of people that rated moviexhigher than 3.
     * This gives higher ratings forpeople with the same age as other viewers,
     * and lower ratings for movies that peers have not rated positively.
     */
    @Override
    protected double computeRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        double ageBias = 0.0;

        if (ratingDetails.getUser().getAge() != 1) {
            // average age rankings > 3
            double averageAge = ratingDetails.getMovie().getAverageAge(userList);

            if (!Double.isNaN(averageAge)) {
                // 1 - |user_age - movie_average_age| / ((2 * (max_age - min_age)) / 3)
                ageBias = 1 - (Math.abs(ratingDetails.getUser().getAge() - averageAge) / ageDifference);
                if (ageBias < 0) ageBias = 0;
            }
        }

        return super.computeRating(mean, ratingDetails, P, Q) + lambdaAge * ageBias;
    }
}
