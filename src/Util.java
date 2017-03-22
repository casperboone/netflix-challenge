import java.lang.Math;
import java.util.*;

public class Util {

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

    public static double rmse(RatingList predictions, RatingList actualRatings) {
        double sum = 0.0;

        for (int i = 0; i < predictions.size(); i++) {
            sum += Math.pow(predictions.get(i).getRating()
                    - actualRatings.get(i).getRating(), 2);
        }

        return Math.sqrt((1.0 / predictions.size()) * sum);
    }

    public static Map<Integer, Double> subtractAverage(Map<Integer, Double> a) {
        return subtractScalarVector(calculateAverage(a), new HashMap<>(a));
    }

    // LATENT FACTORS UTIL:


    public static Map<Integer, Map<Integer, Double>> initializeLatentFactor(int nK, int nF) {
        // Initialize factors with randomly generated numbers
        Map<Integer, Map<Integer, Double>> F = new HashMap<>();

        // Random number generator (uniform)
        Random randomInteger = new Random();

        // Initialize a vector and add to map
        for (int k = 0; k < nK; k++) {
            Map<Integer, Double> A = new HashMap<Integer, Double>();
            for (int f = 0; f < nF; f++) {
                A.put(f, randomInteger.nextDouble() - 0.5);
            }
            F.put(k, A);
        }
        return F;
    }


    public static double rootMeanSquaredError(ArrayList<User> Ru, MovieList movieList, UserList userList,
                                              Map<Integer, Map<Integer, Double>> Q, Map<Integer, Map<Integer, Double>> P, double overallMean) {
        // Compute the square root of the mean of squared errors
        double norm = 0.0;
        double rmse = 0.0;
        for (int u = 0; u < Ru.size(); u++) {
            for (int m : Ru.get(u).getRatings().keySet()) {
                // Compute squared difference between true and predicted rating
                rmse += Math.pow(Ru.get(u).getRatings().get(m) - (innerProduct(Q.get(u), P.get(m)) + overallMean + movieList.get(m).getBias(overallMean) + userList.get(u).getBias(overallMean)), 2);
                norm += 1;
            }
        }
        return Math.sqrt(rmse / norm);
    }

//    public static double updateQuf(ArrayList<User> Ru, Map<Integer, Map<Integer, Double>> Q,
//                                   Map<Integer, Map<Integer, Double>> P, int u, int f, int nF, double lambdaQ) {
//        // Compute update in Q for user u and factor f
//
////		...CODE HERE...
//
//    }
//
//    public static double updatePmf(ArrayList<Movie> Rm, Map<Integer, Map<Integer, Double>> Q,
//                                   Map<Integer, Map<Integer, Double>> P, int m, int f, int nF, double lambdaP) {
//        // Compute update in P for movie m and factor f
//
////		...CODE HERE...
//
//    }


}
