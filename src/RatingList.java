import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RatingList extends ArrayList<Rating> {

    private static final long serialVersionUID = 1L;

    private double averageRating;


    public void readFile(String filename, UserList userList, MovieList movieList) {
        readFile(filename, userList, movieList, true, 0, Integer.MAX_VALUE);
    }

    // Reads in a file with rating data
    public void readFile(String filename, UserList userList, MovieList movieList, boolean includeRatings, int start, int end) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(filename));

            int i = 0;

            while ((line = br.readLine()) != null) {
                i++;
                if (i < start) continue;
                if (i > end) break;

                String[] ratingData = line.split(";");
                if (ratingData.length == 3 && includeRatings) {
                    add(new Rating(
                        userList.get(Integer.parseInt(ratingData[0]) - 1),
                        movieList.get(Integer.parseInt(ratingData[1]) - 1),
                        Double.parseDouble(ratingData[2])));
                } else {
                    add(new Rating(
                        userList.get(Integer.parseInt(ratingData[0]) - 1),
                        movieList.get(Integer.parseInt(ratingData[1]) - 1),
                        0.0));
                }
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

    // Writes a result file
    public void writeResultsFile(String filename) {
        PrintWriter pw;
        try {
            pw = new PrintWriter(filename);
            pw.println("Id,Rating");
            for (int i = 0; i < size(); i++) {
                pw.println((i + 1) + "," + get(i).getRating());
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Compute overall average rating (used for global bias)
     */
    public void computeAverage() {
        double sum = 0.0;
        for (Rating rating : this) {
            sum += rating.getRating();
        }
        averageRating = sum / this.size();
    }

    /**
     * Get the average rating.
     *
     * @return Overall average rating.
     */
    public double getAverageRating() {
        return averageRating;
    }
}
