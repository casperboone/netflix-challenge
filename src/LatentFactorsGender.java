public class LatentFactorsGender extends LatentFactors {
    static double lambdaGender = 0.1;

    // MAYBE SHOULD BE SOMETHING WITH AVERAGE OF PEOPLE WITH SAME GENDER

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
