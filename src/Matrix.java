import java.util.HashMap;
import java.util.Map;

/**
 * Simple matrix class to be used for for instance P or Q in latent factors.
 * Adds a little bit extra behaviour and improves readability a lot.
 */
public class Matrix extends HashMap<Integer, HashMap<Integer, Double>> {
    public Matrix duplicate() {
        Matrix newMatrix = new Matrix();
        for (Map.Entry<Integer, HashMap<Integer, Double>> entry : entrySet()) {
            newMatrix.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        return newMatrix;
    }
}
