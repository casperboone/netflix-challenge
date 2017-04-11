import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * First implementation of trained combiner using stochastic gradient descent.
 * There is also a second trained combiner implementation in python/combiner.py.
 */
public class CombinerTrained {

    int numberOfFolds = 20;
    int maxIterations = 30;
    double learningRate = 0.001;

    /**
     * Training sources are the results of predictions for all ratings
     * of the training sets (by different predictors).
     */
    private CombinerSource[] trainSources = {
            new CombinerSource("stacking/gender_23f_04lbd_85658.csv", 0.0),
            new CombinerSource("stacking/temporal_9f_04lbd_85819.csv", 0.0),
            new CombinerSource("stacking/gender_9f_04lbd_85535.csv", 0.0),
            new CombinerSource("stacking/collaborative_filtering.csv", 0.0),

//            new CombinerSource("stacking/age_9f_04lbd_85893.csv", 0.0),
//            new CombinerSource("stacking/age_23f_075lbd_85937.csv", 0.0),
//            new CombinerSource("stacking/standard_9f_04lbd_85562.csv", 0.0),
//            new CombinerSource("stacking/standard_23f_075lbd_86019.csv", 0.0),
//            new CombinerSource("stacking/temporal_23f_075lbd_85643.csv", 0.0),
    };

    /**
     * Actual training ratings.
     */
    private CombinerSource actualTrainRatings = new CombinerSource("stacking/actual_train_ratings.csv", 500);

    /**
     * Perform ensemble actions.
     */
    public CombinerTrained() {
        // Read all sources from file into memory and normalize them
        for (CombinerSource combinerSource : trainSources) {
            readFile(combinerSource);
            normalize(combinerSource.getItems());
        }
        readFile(actualTrainRatings);
        normalize(actualTrainRatings.getItems());

        // Create all folds
        ArrayList<ArrayList<Integer>> folds = createFolds();

        for (ArrayList<Integer> fold : folds) {
            // Assign test set
            ArrayList<Integer> test = fold;
            // All other folds are the training set
            ArrayList<ArrayList<Integer>> train = new ArrayList<>(folds);
            train.remove(test);

            // Train our model
            trainModel(train);

            // Output RMSE of the test set.
            System.out.println(computeTestRMSE(test));
        }


        // Output all weights by constructing a weighted average based on the weights computed
        // in every fold and there corresponding RMSE scores.
        System.out.println("Result:");
        int i = 0;
        for (CombinerSource trainSource : trainSources) {
            System.out.print(i + " " + trainSource.getWeightAveragedWeighedByRMSE() + " - ");
            i++;
        }
        System.out.println("done!");
    }

    private void trainModel(ArrayList<ArrayList<Integer>> train) {
        // initialize coefficients
        for (CombinerSource trainSource : trainSources) {
            trainSource.initializeWeight();
        }

        // Loop maxIterations times
        for (int iter = 0; iter < maxIterations; iter++) {
            // Loop over all folds of the training set
            for (ArrayList<Integer> trainKeys : train) {
                // Loop over all keys of rating items in the current training set
                for (int trainKey : trainKeys) {
                    // Compute error, the actual rating - the predicted rating of our current model
                    double error = actualTrainRatings.getItems().get(trainKey) - predict(trainKey);

                    // Update weights of all train sources
                    // Update rule: w_t = w_t + learningRate * (predictedRatingOfTrainSource * error)
                    for (CombinerSource trainSource : trainSources) {
                        double trainSourceRating = trainSource.getItems().get(trainKey);
                        double value = trainSource.getWeight() + learningRate * (trainSourceRating * error);

                        trainSource.setWeight(value);
                    }
                }
            }

            fixWeights();
            outputFixedWeights(iter);
        }
    }

    /**
     * Scale all weights of train sources between 0 and 1.
     */
    private void fixWeights() {
        double total = 0.0;
        for (CombinerSource trainSource : trainSources) {
            total += Math.abs(trainSource.getWeight());
        }

        for (CombinerSource trainSource : trainSources) {
            trainSource.setFixedWeight(Math.abs(trainSource.getWeight()) / total);
        }
    }

    /**
     * Compute weighted average with train source ratings and their weights.
     */
    private double predict(int key) {
        double result = 0.0;
        for (CombinerSource trainSource : trainSources) {
            result += trainSource.getWeight() * trainSource.getItems().get(key);
        }
        return result;
    }

    /**
     * Compute weighted average with train source ratings and their fixed weights.
     */
    private double predictFixed(int key) {
        double result = 0.0;
        for (CombinerSource trainSource : trainSources) {
            result += trainSource.getFixedWeight() * trainSource.getItems().get(key);
        }
        return result;
    }

    /**
     * Normalize ratings between 0 and 1.
     */
    private void normalize(HashMap<Integer, Double> data) {
        for (int key : data.keySet()) {
            data.put(key, ((data.get(key) - 3) / 4));
        }
    }

    /**
     * Compute the RMSE  of two hashmaps.
     */
    private double rmse(HashMap<Integer, Double> actual, HashMap<Integer, Double> predicted) {
        double sum = 0.0;

        for (int key : predicted.keySet()) {
            sum += Math.pow(predicted.get(key) - actual.get(key), 2);
        }

        return Math.sqrt(sum / predicted.size());
    }

    /**
     * Compute RMSE on test set.
     */
    private double computeTestRMSE(ArrayList<Integer> test) {
        HashMap<Integer, Double> testPredictions = new HashMap<>();
        for (Integer index : test) {
            testPredictions.put(index, predictFixed(index));
        }

        double rmse = rmse(actualTrainRatings.getItems(), testPredictions);

        // Cache rmse and fixed weight for final result
        for (CombinerSource trainSource : trainSources) {
            trainSource.cacheFixedWeight(rmse);
        }

        return rmse;
    }

    /**
     * Output fixed weights to screen.
     */
    private void outputFixedWeights(int iter) {
        int i = 0;
        for (CombinerSource trainSource : trainSources) {
            System.out.print("[" + iter +"] " + i + " " + trainSource.getFixedWeight() + " - ");
            i++;
        }
        System.out.println();
    }

    /**
     * Create k-folds of random items.
     */
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
        new CombinerTrained();
    }

    /**
     * Read a file (specified in an ensemble source) into memory and store it in the source object.
     *
     * @param source An ensemble source
     */
    private void readFile(CombinerSource source) {
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
}
