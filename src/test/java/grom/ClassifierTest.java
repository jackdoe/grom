package grom;
import java.io.File;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

public class ClassifierTest {
    public static final int GOOD = 0;
    public static final int BAD = 1;

    @Test
    public void testClassifier() throws Exception {
        Classifier c = new Classifier(2);
        long[] data = {2, 1};
        c.learn(GOOD,data);
        assertEquals(2, c.datas[GOOD].total);
        assertEquals(0, c.datas[BAD].total);

        assertEquals(1.0, c.probScores(data)[GOOD],0.01);
        assertEquals(0.0, c.probScores(data)[BAD],0.01);

        c.learn(GOOD,data);
        assertEquals(4, c.datas[GOOD].total);
        assertEquals(0, c.datas[BAD].total);
        assertEquals(1.0, c.probScores(data)[GOOD],0.01);
        assertEquals(0.0, c.probScores(data)[BAD],0.01);

        c.learn(BAD,data);
        assertEquals(4, c.datas[GOOD].total);
        assertEquals(2, c.datas[BAD].total);
        assertEquals(0.66, c.probScores(data)[GOOD],0.01);
        assertEquals(0.33, c.probScores(data)[BAD],0.01);

        c.learn(BAD,data);
        assertEquals(4, c.datas[GOOD].total);
        assertEquals(4, c.datas[BAD].total);
        assertEquals(0.5, c.probScores(data)[GOOD],0.01);
        assertEquals(0.5, c.probScores(data)[BAD],0.01);
    }
}
