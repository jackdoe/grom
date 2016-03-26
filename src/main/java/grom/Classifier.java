package grom;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.*;
import java.io.*;
// naive bayesian classifier, almost line for line from:
// https://github.com/jbrukh/bayesian/blob/master/bayesian.go

public class Classifier implements Serializable {
    public ClassData[] datas;

    public Classifier(int n) {
        datas = new ClassData[n];
        for (int i = 0; i < n; i++)
            datas[i] = new ClassData();
    }

    public void learn(int which, String[] document) {
        for (String s : document)
            datas[which].inc(s);
    }

    public void wordFrequencies(int which, String[] words, BiConsumer<String, Double> consumer) {
        for (String s : words)
            consumer.accept(s, datas[which].getWordProb(s));
    }

    public double[] getPriors() {
        double[] priors = new double[datas.length];
        int sum = 0;
        for (int i = 0; i < datas.length; i++) {
            priors[i] = datas[i].total;
            sum += datas[i].total;
        }

        if (sum != 0) {
            for (int i = 0; i < datas.length; i++)
                priors[i] /= sum;
        }
        return priors;
    }

    public double[] probScores(String[] document) {
        double[] scores = getPriors(); // just use one array for both dest scores
                                       // and priors
        double sum = 0;

        for (int i = 0; i < datas.length; i++) {
            double score = scores[i];
            for (String s : document)
                score *= datas[i].getWordProb(s);
            scores[i] = score;
            sum += score;
        }

        if (sum > 0) {
            for (int i = 0; i < datas.length; i++)
                scores[i] /= sum;
        }

        return scores;
    }

    public class ClassData implements Serializable {
        public static final double defaultProb = 0.00000000001D;
        public int total;
        // XXX: move this to something more compact
        public Map<String, Integer> freqs = new HashMap<String,Integer>();

        public void inc(String word) {
            total++;
            freqs.compute(word, (k,v) -> (v == null) ? 1 : v + 1);
        }

        public double getWordProb(String word) {
            Integer c = freqs.get(word);
            if (c == null)
                return defaultProb;
            return ((double) c) / ((double) total);
        }

        public double getWordsProb(String[] words) {
            double prob = 1D;
            for (String s : words)
                prob *= getWordProb(s);
            return prob;
        }
    }
}
