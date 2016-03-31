package grom;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.twitter.finagle.Service;
import com.twitter.finagle.SimpleFilter;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finatra.http.JavaHttpServer;
import com.twitter.finatra.http.filters.CommonFilters;
import com.twitter.finatra.http.routing.HttpRouter;
import javax.inject.Inject;
import com.twitter.finatra.http.JavaController;
import java.util.*;

import com.twitter.finatra.request.JsonIgnoreBody;
import com.twitter.util.Future;
import com.twitter.util.Stopwatch;
import net.openhft.chronicle.map.*;
import org.nustaq.serialization.FSTConfiguration;
import scala.runtime.Boxed;
import scala.runtime.BoxedUnit;
import static scala.compat.java8.JFunction.*;

import java.io.*;
public class MainServer extends JavaHttpServer {
    public static class MainController extends JavaController {
        public static final int GOOD = 0;
        public static final int BAD = 1;
        public static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        public static final String DEFAULT_ID = "_default_";

        public static ChronicleMap<String, byte[]> classifiers;
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

        public static long[] getWords(List<String> input) {
            long[] l = new long[input.size()];
            for (int i = 0; i < l.length; i++)
                l[i] = (long) Integer.parseInt(input.get(i));
            return l;
        }

        public static class Input {
            public String classifierId = DEFAULT_ID;
            public static class InputDocument {
                public List<Long> words;
                public int whichClass;
            }
            public List<InputDocument> query;
        }
        public static Map<String,Object> out(Object result, long took) {
            Map<String,Object> o = new HashMap<>(2);
            o.put("result", result);
            o.put("took", took);
            return o;
        }

        private static final ObjectMapper mapper = new ObjectMapper();

        public void configureRoutes() {
            get("/learn", request -> {
                try {
                    long t0 = System.currentTimeMillis();
                    Input input = mapper.readValue(request.getInputStream(), Input.class);
                    Classifier classifier = getClassifier(input.classifierId);
                    for (Input.InputDocument d : input.query)
                        classifier.learn(d.whichClass, d.words);

                    classifiers.put(input.classifierId, conf.asByteArray(classifier));
                    return out(true, System.currentTimeMillis() - t0);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });

            get("/query", request -> {
                try {
                    long t0 = System.currentTimeMillis();
                    Input input = mapper.readValue(request.getInputStream(), Input.class);
                    Classifier classifier = getClassifier(input.classifierId);

                    List<double[]> result = new ArrayList<>(input.query.size());
                    for (Input.InputDocument d : input.query)
                        result.add(classifier.probScores(d.words));
                    return out(result, System.currentTimeMillis() - t0);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    @Override
    public void configureHttp(HttpRouter httpRouter) {
        httpRouter
                .filter(CommonFilters.class)
                .add(MainController.class);
    }
}
