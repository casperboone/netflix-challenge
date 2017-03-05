import java.util.Comparator;

public class Sorter implements Comparator<Integer> {

    double[] A;

    public Sorter(double[] _array) {
        // Constructor
        this.A = _array;
    }

    public Integer[] createIndexArray() {
        // Create index list, 0..n-1
        Integer[] indexes = new Integer[A.length];
        for (int i = 0; i < A.length; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2) {
        // Compare entries in array
        return Double.compare(A[index1], A[index2]);
    }
}