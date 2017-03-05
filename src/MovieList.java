import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;


public class MovieList extends ArrayList<Movie> {

    private static final long serialVersionUID = 1L;

    // Reads in a file with movies data
    public void readFile(String filename) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                String[] movieData = line.split(";");
                add(Integer.parseInt(movieData[0]) - 1,
                    new Movie(Integer.parseInt(movieData[0]),
                        Integer.parseInt(movieData[1]),
                        movieData[2]));
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

    public void addRatings(ArrayList<Rating> ratingList) {
        // Add ratings from a rating list to the user list
        for (int i = 0; i < ratingList.size(); i++) {

            // Get user and movie index of rating i
            int movieIndex = ratingList.get(i).getMovie().getIndex() - 1;
            int userIndex = ratingList.get(i).getUser().getIndex() - 1;

            // Add rating i to user and movie index
            get(movieIndex).putRating(userIndex, ratingList.get(i).getRating());
        }
    }
}

