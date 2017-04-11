/**
 * Latent Factors with temporal bias. Before running this change Util.lf to this class.
 */
public class LatentFactorsTemporal extends LatentFactors {
    static double lamdaYear = 0.1;

    /**
     * r = originalR + λyear * ((year - min year) (max year - min year))
     * -0.5 is so that the bias has a negative impact when it is not a good recommendation
     * based on the release year.
     */
    @Override
    protected double computeRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        double yearBias = 0.0;
        if (ratingDetails.getMovie().getYear() > 0) {
            // min year 1919
            // max year 2000
            yearBias = ((ratingDetails.getMovie().getYear() - 1919) / (2000 - 1919)) - 0.5;
        }

        return super.computeRating(mean, ratingDetails, P, Q) + lamdaYear * yearBias;
    }
}
