package grom;
import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.twitter.finatra.http.JavaHttpServer;
import com.twitter.finatra.http.filters.CommonFilters;
import com.twitter.finatra.http.routing.HttpRouter;
import javax.inject.Inject;
import com.twitter.finatra.http.JavaController;
import java.util.*;
import net.openhft.chronicle.map.*;
import org.nustaq.serialization.FSTConfiguration;

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

        public void configureRoutes() {
            get("/learn", request -> {
                    String id = request.getParam("id",DEFAULT_ID);
                    Classifier classifier = getClassifier(id);
                    String[] words = request.getParams("words").toArray(new String[] {});
                    int which = request.getIntParam("class");
                    classifier.learn(which, words);

                    classifiers.put(id, conf.asByteArray(classifier));
                    return true;
                });

            get("/query", request -> {
                    Classifier classifier = getClassifier(request.getParam("id",DEFAULT_ID));

                    String[] words = request.getParams("words").toArray(new String[] {});
                    return classifier.probScores(words);
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
