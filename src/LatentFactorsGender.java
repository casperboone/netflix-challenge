/**
 * Latent Factors with gender bias. Before running this change Util.lf to this class.
 */
public class LatentFactorsGender extends LatentFactors {
    static double lambdaGender = 0.1;

    /**
     * r = originalR +  λgender *(number of people with same gender that rated the movie higher than 3 /
     * total number of people that rated higher than 3) - 0.5
     * -0.5 is so that the bias has a negative impact when it is not a good recommendation
     * based on gender.
     */
    @Override
    protected double computeRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        // total male ranked higher than 3
        int totalMale = ratingDetails.getMovie().getTotalMale(userList);

        // total female ranked higher than 3
        int totalFemale = ratingDetails.getMovie().getTotalFemale(userList);

        double genderBias = 0.0;
        if (totalFemale + totalMale > 0) {
            genderBias = ((ratingDetails.getUser().isMale() ? totalMale : totalFemale) / (totalFemale + totalMale)) - 0.5;
        }

        return super.computeRating(mean, ratingDetails, P, Q) + lambdaGender * genderBias;
    }
}
