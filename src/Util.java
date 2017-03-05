import java.lang.Math;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class Util {

    public static double calculateCosine(Map<Integer, Double> userX, Map<Integer, Double> userY, int nM) {
        // Compute cosine similarity between two users

        ..CODE HERE..
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
}
