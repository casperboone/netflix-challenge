import java.lang.Math;
import java.util.*;

public class Util {
    static LatentFactors lf = new LatentFactorsGender();

    public static double calculateCosine(Map<Integer, Double> userX, Map<Integer, Double> userY) {
        // Compute cosine similarity between two users
        return innerProduct(userX, userY) / (euclideanNorm(userX) * euclideanNorm(userY));
    }

    public static double calculateAverage(Map<Integer, Double> a) {
        // Compute average of ratings
        double sum = 0;
        for (double value : a.values()) {
            sum += value;
        }
        return sum / a.size();
    }

    public static double innerProduct(Map<Integer, Double> a, Map<Integer, Double> b) {
        // Compute sum of the product of elements of two vectors

        // Get intersection of keys
        Set<Integer> K = new HashSet<Integer>(a.keySet());
        K.retainAll(b.keySet());

        double ab = 0.0;
        for (int k : K) {
            ab += a.get(k) * b.get(k);
        }
        return ab;
    }

    public static double euclideanNorm(Map<Integer, Double> a) {
        // Compute Euclidean norm of a vector
        double norm = 0.0;
        for (double value : a.values()) {
            norm += Math.pow(value, 2);
        }
        return Math.sqrt(norm);
    }

    public static Map<Integer, Double> subtractScalarVector(double a, Map<Integer, Double> b) {
        // Subtract a scalar from a vector
        for (int key : b.keySet()) {
            b.put(key, b.get(key) - a);
        }
        return b;
    }

    public static Map<Integer, Double> subtractAverage(Map<Integer, Double> a) {
        return subtractScalarVector(calculateAverage(a), new HashMap<>(a));
    }

    public static Matrix initializeLatentFactor(int nK, int nF) {
        // Initialize factors with randomly generated numbers
        Matrix F = new Matrix();

        // Random number generator (uniform)
        Random randomInteger = new Random();

        // Initialize a vector and add to map
        for (int k = 0; k < nK; k++) {
            HashMap<Integer, Double> A = new HashMap<>();
            for (int f = 0; f < nF; f++) {
                A.put(f, randomInteger.nextDouble() - 0.5);
            }
            F.put(k, A);
        }
        return F;
    }

    public static double rmse(RatingList predictions, RatingList actualRatings) {
        // Compute RMSE
        double sum = 0.0;

        for (int i = 0; i < predictions.size(); i++) {
            sum += Math.pow(predictions.get(i).getRating() - actualRatings.get(i).getRating(), 2);
        }

        return Math.sqrt(sum / predictions.size());
    }

    public static double rmseKnownRatings(LatentFactors lf, RatingList actualRatings, Matrix P, Matrix Q) {
        // Determine the predicted rating and compare it with the value of the already known rating
        // Specifically for latent factors
        RatingList predictions = new RatingList();

        for (Rating rating : actualRatings) {
            predictions.add(new Rating(
                    rating.getUser(),
                    rating.getMovie(),
                    lf.predictRating(actualRatings.getAverageRating(), rating, P, Q)
            ));
        }

        return rmse(predictions, actualRatings);
    }

}