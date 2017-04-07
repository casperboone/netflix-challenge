public class LatentFactorsTemporal extends LatentFactors {
   static double lamdaYear = 0.1;

    @Override
    protected double computeRating(double mean, Rating ratingDetails, Matrix P, Matrix Q) {
        double yearBias = 0.0;
        if (ratingDetails.getMovie().getYear() > 0) {
            // oudste 1919
            // nieuwste 2000
            yearBias = ((ratingDetails.getMovie().getYear() - 1919) / (2000-1919)) - 0.5;
        }

        return super.computeRating(mean, ratingDetails, P, Q) + lamdaYear * yearBias;
    }
}
