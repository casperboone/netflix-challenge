import java.util.HashMap;

/**
 * A source used in ensemble actions.
 */
public class EnsembleSource {
    private String fileName;
    private double weight;

    /**
     * Map of all predictions in the ensemble source (which is a submission file).
     * Only contains items after reading a file into memory.
     */
    private HashMap<Integer, Double> items = new HashMap<>();

    public EnsembleSource(String fileName, double weight) {
        this.fileName = fileName;
        this.weight = weight;
    }

    public String getFileName() {
        return fileName;
    }

    public double getWeight() {
        return weight;
    }

    public HashMap<Integer, Double> getItems() {
        return items;
    }

    public void addItem(int key, double value) {
        items.put(key, value);
    }
}
