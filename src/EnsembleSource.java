import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * A source used in ensemble actions.
 */
public class EnsembleSource {
    private String fileName;
    private double weight;
    private double fixedWeight;
    private ArrayList<Double> weightHistory = new ArrayList<>();
    private ArrayList<Double> rmseHistory = new ArrayList<>();

    public double getFixedWeight() {
        return fixedWeight;
    }

    public void setFixedWeight(double fixedWeight) {
        this.fixedWeight = fixedWeight;
    }

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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void initializeWeight() {
        weight = new Random().nextDouble();
    }

    public void cacheFixedWeight(double rmse) {
        weightHistory.add(fixedWeight);
        rmseHistory.add(1 / rmse);
    }

    public double getWeightAveragedWeighedByRMSE() {
        double rmseSum = 0;
        for (double rmse : rmseHistory) {
            rmseSum += rmse;
        }

        double sum = 0.0;
        for (int i = 0; i < weightHistory.size(); i++) {
            sum += (rmseHistory.get(i) / rmseSum) * weightHistory.get(i);
        }

        return sum;
    }

    public ArrayList<Double> getWeightHistory() {
        return weightHistory;
    }
}
