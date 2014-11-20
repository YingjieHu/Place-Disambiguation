package edu.ucsb.stko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

public class GridIdTopicVectors extends Hashtable<Integer, double[]> {
    
    /**
     * Returns the Jensen-Shannon divergence.
     */
    public double jensenShannonDivergence(double[] p1, double[] p2) {
      assert(p1.length == p2.length);
      double[] average = new double[p1.length];
      for (int i = 0; i < p1.length; ++i) {
        average[i] += (p1[i] + p2[i])/2;
      }
      return (klDivergence(p1, average) + klDivergence(p2, average))/2;
    }
    
    public static final double log2 = Math.log(2);
    /**
     * Returns the KL divergence, K(p1 || p2).
     *
     * The log is w.r.t. base 2. <p>
     *
     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
     * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
     * 
     */
    public double klDivergence(double[] p1, double[] p2) {


        double klDiv = 0.0;

        for (int i = 0; i < p1.length; ++i) {
            if (p1[i] == 0) { continue; }
            if (p2[i] == 0.0) { continue; } // Limin

            klDiv += p1[i] * Math.log( p1[i] / p2[i] );
        }

        return klDiv / log2; // moved this division out of the loop -DM
    }
    
    public Hashtable<Integer,Double> getSimilarityScores(double[] topicVector) {
        Hashtable<Integer,Double> scores = new Hashtable<Integer,Double>();
        for (Enumeration<Integer> keys = keys(); keys.hasMoreElements();) {
            Integer key = keys.nextElement();
            double[] value = get(key);
            double js = jensenShannonDivergence(value,topicVector);
            scores.put(key, js);
        }
        return scores;
    }
    
    public ArrayList<GridIdScorePair> getSortedSimilarityScores(double[] topicVector) {
        Hashtable<Integer,Double> scores = getSimilarityScores(topicVector);
        ArrayList<Map.Entry<Integer, Double>> l = new ArrayList(scores.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<Integer, Double>>(){
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        ArrayList<GridIdScorePair> sortedScores = new ArrayList<GridIdScorePair>();
        for (Map.Entry<Integer, Double> e : l) {
            GridIdScorePair gridIdScore = new GridIdScorePair(e.getKey(), e.getValue());
            sortedScores.add(gridIdScore);
        }
        return sortedScores;
    }
}
