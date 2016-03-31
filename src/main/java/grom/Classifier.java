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

    public void learn(int which, List<Long> document) {
        for (long word : document)
            datas[which].inc(word);
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

    public double[] probScores(List<Long> document) {
        double[] scores = getPriors(); // just use one array for both dest scores
                                       // and priors
        double sum = 0;

        for (int i = 0; i < datas.length; i++) {
            double score = scores[i];
            for (long word : document)
                score *= datas[i].getWordProb(word);
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
        public long[] freqs = null;
        public int total;

        // use one sorted array of longs (word << 32 | frequency) instead of Map<String,Counter>
        // Arrays.binarySearch returns (-insertion_point - 1), so when we look for a key
        // we just have to do if (freqs[insertion_point] >> 32) == word), the binary
        // search should never find exact match because we are searching for (word << 32)
        // but what we actually store is 'word << 31 | frequency (minimum 1)'
        public int insertionPoint(long word) {
            int idx = Arrays.binarySearch(freqs, word << 32L);
            if (idx < 0)
                return -(idx + 1);

            throw new IllegalStateException("should not be able to find exact match");
        }

        public void inc(long word) {
            total++;

            if (freqs == null) {
                freqs = new long[1];
                freqs[0] = (word << 32L) | 1L;
            } else {
                int ip = insertionPoint(word);
                if (ip >= 0 && ip < freqs.length && (freqs[ip] >> 32L) == word) {
                    freqs[ip]++;
                } else {
                    freqs = Arrays.copyOf(freqs, freqs.length + 1);
                    for (int i = freqs.length - 1; i > ip; i--) {
                        freqs[i] = freqs[i - 1];
                    }
                    freqs[ip] = (word << 32L) | 1L;
                }
            }
        }

        public double getWordProb(long word) {
            if (freqs != null) {
                int ip = insertionPoint(word);
                if (ip >= 0 && ip < freqs.length && (freqs[ip] >> 32L) == word) {
                    return (freqs[ip] & 0xFFFFFFFFL) / (double) total;
                }
            }
            return defaultProb;
        }
    }
}
