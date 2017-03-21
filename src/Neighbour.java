/**
 * Neigbouring element of for instance a User or Movie. Primarily used to keep
 * track of similarity.
 */
public class Neighbour<T> implements Comparable<Neighbour<T>> {
    private T resource;
    private double similarity;

    public Neighbour(T resource, double similarity) {
        this.resource = resource;
        this.similarity = similarity;
    }

    /**
     * @return Similarity of this neighbour.
     */
    public double getSimilarity() {
        return similarity;
    }

    /**
     * @return The object of the neighbour, for instance a user or movie.
     */
    public T getResource() {
        return resource;
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Neighbour<T> other) {
        return Double.compare(this.similarity, other.similarity);
    }

}
