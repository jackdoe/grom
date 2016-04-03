package grom;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import net.openhft.chronicle.map.*;
import org.nustaq.serialization.FSTConfiguration;
import java.util.*;
import java.io.File;

@Path("/classify")
@Produces(MediaType.APPLICATION_JSON)
public class ClassifyResource {
    private static final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
    private static final String DEFAULT_ID = "_default_";
    private static ChronicleMap<String, byte[]> classifiers;

    public ClassifyResource() { }

    static {
        try {
            classifiers = ChronicleMap
                .of(String.class, byte[].class)
                .averageKeySize(32)
                .averageValueSize(128)
                .entries(50_000)
                .createPersistedTo(new File("/tmp/grom.cmap")); // XXX: parameterize
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Classifier getClassifier(String id) {
        byte[] cdata = classifiers.get(id);
        return cdata == null ? new Classifier(2) : (Classifier)conf.asObject(cdata);
    }

    public static class Input {
        public String classifierId = DEFAULT_ID;
        public static class InputDocument {
            public List<Long> words;
            public int whichClass;
        }
        public List<InputDocument> query;
    }
    public static class Output {
        public long took;
        public Object result;
        public Output(long took, Object result) {
            this.took = took;
            this.result = result;
        }
    }

    @PUT
    public Output learn(Input input) {
        long t0 = System.currentTimeMillis();
        Classifier classifier = getClassifier(input.classifierId);

        for (Input.InputDocument d : input.query)
            classifier.learn(d.whichClass, d.words);

        classifiers.put(input.classifierId, conf.asByteArray(classifier));
        return new Output(System.currentTimeMillis() - t0, true);
    }

    @POST
    public Output query(Input input) {
        long t0 = System.currentTimeMillis();
        Classifier classifier = getClassifier(input.classifierId);

        List<double[]> result = new ArrayList<>(input.query.size());
        for (Input.InputDocument d : input.query)
            result.add(classifier.probScores(d.words));

        return new Output(System.currentTimeMillis() - t0, result);
    }
}
