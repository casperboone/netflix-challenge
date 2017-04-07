import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes ensemble actions.
 */
public class Ensemble {
    /**
     * List of sources to be used including the weight they should have. It is based on files
     * to be able to quickly change weights without recomputing everything.
     */
    private EnsembleSource[] ensembleSources = {
//            new EnsembleSource("submissions/submission_1490457887340.csv", .365), // LF 9 - 0.05 lambda - 0.02 LR
//            new EnsembleSource("submissions/submission_1491420825112.csv", .34), // LF - temporal: 9 - 0.04 lambda - 0.02 / 0.002 LR
////           new EnsembleSource("submissions/submission_1491425905643.csv", .36), // LF - gender: 9 - 0.04 lambda - 0.02 / 0.002 LR
            // new EnsembleSource("submissions/submission_1490453791923.csv", .125), // LF 23 - 0.075 lambda - 0.02 LR
////            new EnsembleSource("submissions/submission_1489179032660.csv", .64) // CF


            new EnsembleSource("submissions/submission_1491577351210.csv", 0.25265622258547543), // gender_23f_04lbd_85658
            new EnsembleSource("submissions/submission_1491577293903.csv", 0.2160796452848671), // temporal_9f_04lbd_85819
            new EnsembleSource("submissions/submission_1491425905643.csv", 0.03706982596208227), // LF - gender: 9 - 0.04 lambda - 0.02 / 0.002 LR


//            new EnsembleSource("submissions/age_9f_04lbd_85893.csv", 0.11882958585089694),
//            new EnsembleSource("submissions/age_23f_075lbd_85937.csv", 0.07644448269935332),
//            new EnsembleSource("submissions/standard_9f_04lbd_85562.csv", 0.12136516103896347),
//            new EnsembleSource("submissions/standard_23f_075lbd_86019.csv", 0.12684913076295615),
//            new EnsembleSource("submissions/temporal_23f_075lbd_85643.csv", 0.21201871037134407),

            new EnsembleSource("submissions/submission_1489179032660.csv", 0.16468312670116678), // CF
            new EnsembleSource("submissions/submission_1489179032660.csv", 0.1647489884546739), // CF
            new EnsembleSource("submissions/submission_1489179032660.csv", 0.16476219101173453), // CF

    };
    /**
     * Map of the final results.
     */
    private HashMap<Integer, Double> results = new HashMap<>();

    /**
     * Perform ensemble actions.
     */
    public Ensemble() {
        // Read all sources from file into memory
        for (EnsembleSource ensembleSource : ensembleSources) {
            readFile(ensembleSource);
        }

        // For a certain item in all sources, multiply it with the weight and add it to the total of the item
        for (Integer key : ensembleSources[0].getItems().keySet()) {
            double sum = 0.0;
            for (EnsembleSource ensembleSource : ensembleSources) {
                sum += ensembleSource.getWeight() * ensembleSource.getItems().get(key);
            }
            results.put(key, sum);
        }

        // Write the final results to a submission file
        writeResultsFile("submissions/ensemble/s_" + System.currentTimeMillis() + ".csv");

        System.out.println("done!");
    }

    /**
     * Entry point of ensemble executor.
     */
    public static void main(String[] args) {
        new Ensemble();
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
