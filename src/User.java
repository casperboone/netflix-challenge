import java.util.HashMap;

public class User {

    int index;
    int age;
    int profession;
    boolean male;
    HashMap<Integer, Double> ratings;

    public User(int _index, boolean _male, int _age, int _profession) {
        this.index = _index;
        this.male = _male;
        this.age = _age;
        this.profession = _profession;
        this.ratings = new HashMap<Integer, Double>();
    }

    public void putRating(Integer movie, double rating) {
        ratings.put(movie, rating);
    }

    public HashMap<Integer, Double> getRatings() {
        return ratings;
    }

    public int getIndex() {
        return index;
    }

    public boolean isMale() {
        return male;
    }

    public int getAge() {
        return age;
    }

    public int getProfession() {
        return profession;
    }

}
