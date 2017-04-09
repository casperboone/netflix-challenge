import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;

// coeff initializen

/**
 * Executes ensemble actions.
 */
public class EnsembleStacking {

    int numberOfFolds = 20;

    private EnsembleSource[] trainSources = { // winning 0.028630998907666745 - 1 0.07834832922747179 - 2 0.5803512789872395 - 3 0.312669392877622
            new EnsembleSource("stacking/gender_23f_04lbd_85658.csv", 0.0), //vrijwel 0
            new EnsembleSource("stacking/temporal_9f_04lbd_85819.csv", 0.0), // vrij wel 0
            new EnsembleSource("stacking/gender_9f_04lbd_85535.csv", 0.0),

//            new EnsembleSource("stacking/age_9f_04lbd_85893.csv", 0.0),
//            new EnsembleSource("stacking/age_23f_075lbd_85937.csv", 0.0),
//            new EnsembleSource("stacking/standard_9f_04lbd_85562.csv", 0.0),
//            new EnsembleSource("stacking/standard_23f_075lbd_86019.csv", 0.0),
//            new EnsembleSource("stacking/temporal_23f_075lbd_85643.csv", 0.0),

            new EnsembleSource("stacking/collaborative_filtering.csv", 0.0),
//            new EnsembleSource("stacking/collaborative_filtering.csv", 0.0),
//            new EnsembleSource("stacking/collaborative_filtering.csv", 0.0),
    };

    private EnsembleSource actualTrainRatings = new EnsembleSource("stacking/actual_train_ratings.csv", 500);

    /**
     * Map of the final results.
     */
    private HashMap<Integer, Double> results = new HashMap<>();

    /**
     * Perform ensemble actions.
     */
    public EnsembleStacking() {
        // Read all sources from file into memory
        for (EnsembleSource ensembleSource : trainSources) {
            readFile(ensembleSource);
            normalize(ensembleSource.getItems());
        }
        readFile(actualTrainRatings);
        normalize(actualTrainRatings.getItems());


        for (EnsembleSource trainSource : trainSources) {
            trainSource.initializeWeight();
        }

        ArrayList<ArrayList<Integer>> folds = createFolds();


        for (ArrayList<Integer> fold : folds) {

            ArrayList<Integer> test = fold;
            ArrayList<ArrayList<Integer>> train = new ArrayList<>(folds);
            train.remove(test);

            trainModel(train);

            System.out.println(computeTestRMSE(test));
        }


        System.out.println("Result:");
        int i = 0;
        for (EnsembleSource trainSource : trainSources) {
            System.out.print(i + " " + trainSource.getWeightAveragedWeighedByRMSE() + " - ");
            i++;
        }


        System.out.println("done!");
    }

    private void trainModel(ArrayList<ArrayList<Integer>> train) {
        // initialize coefficients (done by default)
        for (EnsembleSource trainSource : trainSources) {
            trainSource.initializeWeight();
        }

        int maxIterations = 30;
        double learningRate = 0.001;

        for (int iter = 0; iter < maxIterations; iter++) {
            for (ArrayList<Integer> trainKeys : train) {

                for (int trainKey : trainKeys) {

                    double yHat = predict(trainKey);

                    for (EnsembleSource trainSource : trainSources) {
                        double b = trainSource.getItems().get(trainKey);
                        double a = actualTrainRatings.getItems().get(trainKey);
//                        System.out.println(a + " " + yHat);
                        double val = trainSource.getWeight()
                                + learningRate * (trainSource.getWeight() * b * (a - yHat));

//                        double val = trainSource.getWeight()
//                                + learningRate * (actualTrainRatings.getItems().get(trainKey) - trainSource.getItems().get(trainKey));


                        trainSource.setWeight(val);
                    }

                }

            }

            fixWeights();
            int i = 0;
            for (EnsembleSource trainSource : trainSources) {
                System.out.print("[" + iter +"] " + i + " " + trainSource.getFixedWeight() + " - ");
                i++;
            }
            System.out.println();
        }
    }

    private void fixWeights() {
        double total = 0.0;
        for (EnsembleSource trainSource : trainSources) {
            total += Math.abs(trainSource.getWeight());
        }

        for (EnsembleSource trainSource : trainSources) {
            trainSource.setFixedWeight(Math.abs(trainSource.getWeight()) / total);
        }
    }

    private double predict(int key) {
        double result = 0.0;
        for (EnsembleSource trainSource : trainSources) {
            result += trainSource.getWeight() * trainSource.getItems().get(key);
        }
        if (Double.isInfinite(result)) {
            System.out.println("stuk2");
        }
        return result;
    }

    private double predictFixed(int key) {
        double result = 0.0;
        for (EnsembleSource trainSource : trainSources) {
            result += trainSource.getFixedWeight() * trainSource.getItems().get(key);
        }
        if (Double.isInfinite(result)) {
            System.out.println("stuk2");
        }
        return result;
    }

    private double rmse(HashMap<Integer, Double> actual, HashMap<Integer, Double> predicted) {
        // Compute RMSE
        double sum = 0.0;

        for (int key : predicted.keySet()) {
            sum += Math.pow(predicted.get(key) - actual.get(key), 2);
        }

        return Math.sqrt(sum / predicted.size());
    }

    private void normalize(HashMap<Integer, Double> data) {
        for (int key : data.keySet()) {
            data.put(key, ((data.get(key) - 3) / 4));
        }
    }

    private double computeTestRMSE(ArrayList<Integer> test) {
        HashMap<Integer, Double> testPredictions = new HashMap<>();
        for (Integer index : test) {
            testPredictions.put(index, predictFixed(index));
        }

        double rmse = rmse(actualTrainRatings.getItems(), testPredictions);

        for (EnsembleSource trainSource : trainSources) {
            trainSource.cacheFixedWeight(rmse);
        }

        return rmse;
    }

    private ArrayList<ArrayList<Integer>> createFolds() {
        int foldSize = (actualTrainRatings.getItems().size() + numberOfFolds - 1) / numberOfFolds;

        ArrayList<ArrayList<Integer>> folds = new ArrayList<>();

        ArrayList<Integer> permutedList = new ArrayList<>(actualTrainRatings.getItems().keySet());
        Collections.shuffle(permutedList);

        int position = 0;

        // create folds
        for (int f = 0; f < numberOfFolds; f++) {
            folds.add(f, new ArrayList<>());

            int end = position + foldSize;
            int i = position;
            for (; i < (permutedList.size() < end ? permutedList.size() : end); i++) {
                folds.get(f).add(permutedList.get(i));
            }
            position = i;
        }

        return folds;
    }

    /**
     * Entry point of ensemble executor.
     */
    public static void main(String[] args) {
        new EnsembleStacking();
    }

    /**
     * Read a file (specified in an ensemble source) into memory and store it in the source object.
     *
     * @param source An ensemble source
     */
    private void readFile(EnsembleSource source) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(source.getFileName()));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                source.addItem(Integer.parseInt(data[0]), Double.parseDouble(data[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write the final results back to a file.
     *
     * @param filename Filename of the results file.
     */
    private void writeResultsFile(String filename) {
        PrintWriter pw;
        try {
            pw = new PrintWriter(filename);
            pw.println("Id,Rating");
            for (Map.Entry<Integer, Double> result : results.entrySet()) {
                pw.println(result.getKey() + "," + result.getValue());
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
