import static org.junit.Assert.*;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UtilTest {
    @Test
    public void calculateCosine() {
        Map<Integer, Double> userX = new HashMap<>();
        Map<Integer, Double> userY = new HashMap<>();

        userX.put(1, 4.0);
        userX.put(4, 5.0);
        userX.put(5, 1.0);

        userY.put(1, 5.0);
        userY.put(2, 5.0);
        userY.put(3, 4.0);

        assertEquals(0.380, Util.calculateCosine(userX, userY, 0), 0.001);
    }

}