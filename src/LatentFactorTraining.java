import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Perform training for parameters of latent factors using cross
 * validation. Goal is to find a good combination of the best
 * parameters, that can be further optimized if needed.
 */
public class LatentFactorTraining {
    public static void main(String[] args) throws InterruptedException {
        // Sets of possible values for parameters to limit the total number
        // Other parameter types can be added / removed when needed
        int[] factors = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 20, 21, 23, 25, 27, 29};
        double[] lambdas = {0.025, 0.05, 0.075, 0.10};

        // Execute multiple times to get a more accurate result
        for (int i = 0; i < 3; i++) {
            // For each factor / lambda combination
            for (int factor : factors) {
                for (double lambda : lambdas) {
                    System.out.println("Training iteration: " + i);

                    // Clear the validation statistics, so we start clean
                    LatentFactors.validationStatistics.clear();

                    LatentFactors.nF = factor;
                    LatentFactors.lambdaP = lambda;
                    LatentFactors.lambdaQ = lambda;

                    // Execute latent factors cross validation with the parameters
                    LatentFactorsCrossValidation.main(args);

                    // Write the validation statistics and other related data to a file,
                    // so the results can later be analyzed to improve the algorithm.
                    writeStatisticsToFile(factor, lambda, LatentFactors.validationStatistics);

                    System.out.println("done");
                }
            }
        }
    }

    /**
     * Write validation statistics to a file.
     */
    private static void writeStatisticsToFile(int factors, double lambda, List<Double> statistics) {
        String filename = "training/s_" + (statistics.get(statistics.size() - 1)) + "_" + factors + "_" + lambda + "_" + statistics.size() + ".txt";
        PrintWriter pw;
        try {
            pw = new PrintWriter(filename);
            pw.println("Final: " + statistics.get(statistics.size() - 1));
            pw.println("Iterations: " + statistics.size());
            pw.println("Lambda: " + lambda);
            pw.println("Factors: " + factors);
            pw.println(" ");

            for (int i = 0; i < statistics.size(); i++) {
                pw.println(i + " " + statistics.get(i));
            }

            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
