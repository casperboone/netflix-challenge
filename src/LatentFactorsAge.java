public class LatentFactorsAge extends LatentFactors {
    static double lambdaAge = 0.25;

    // (2 * (max - min)) / 3
    static double ageDifference = (1 * (56 - 18)) / 3;

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
