package edu.ucsb.stko;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class TopicModelMatchTester {

    public static void main(String[] args) throws Exception {
        GridIdTopicVectors gridIdTopicVectors = new GridIdTopicVectors();
        String thetaFilename = "fuller7_200.theta";
        int numTopics = 200;
        BufferedReader br = new BufferedReader(new FileReader(new File(thetaFilename)));
        String line;
        // read in all the topic vectors into hashtable
        while ((line = br.readLine()) != null) {
           double[] topicVector = new double[numTopics];
           String[] lineS = line.trim().split("\t");
           int topic = 0;
           double value = 0.0;
           for (int i = 2; i < lineS.length; i++) {
               if (i % 2 == 0) {
                   topic = Integer.parseInt(lineS[i]);
               } else {
                   value = Double.parseDouble(lineS[i]);
                   topicVector[topic] = value;
               }
           }
           String[] gids = lineS[1].substring(1, lineS.length-1).split(",");
           for (String gid : gids) {
               gridIdTopicVectors.put(Integer.parseInt(gid), topicVector);
           }
        }
        br.close();
        
        TopicInferencerForShortText inferencer = new TopicInferencerForShortText();
        PointToGridIdMatcher gridIdMatcher = new PointToGridIdMatcher();
        String shortText = "Washington's icy nights forced the Bobcats into human settlements";
        Candidate[] candidates = {
                new Candidate(1, "Washington, Massachusetts", -73.12, 42.37),
                new Candidate(2, "Washington, Georgia", -82.74, 33.74),
                new Candidate(3, "Washington, Illinois", -89.42, 40.7),
                new Candidate(4, "Washington, Indiana", -87.17, 38.66),
                new Candidate(5, "Washington, Iowa", -91.69, 41.3),
                new Candidate(6, "Washington, Missouri", -91.01, 38.55),
                new Candidate(7, "Washington, New Hampshire", -72.1, 43.18),
                new Candidate(8, "Washington, New Jersey", -74.98, 40.76),
                new Candidate(9, "Washington, North Carolina", -77.05, 35.55),
                new Candidate(10, "Washington, Pennsylvania", -80.25, 40.18),
                new Candidate(11, "Washington, Virginia", -78.16, 38.71),
                new Candidate(12, "Washington, D.C.", -77.04, 38.9)
        };
        ArrayList<Candidate> candidateArrayList = new ArrayList<Candidate>(Arrays.asList(candidates));
        int[] gridIds = gridIdMatcher.getGridIds(candidateArrayList);
        
        double[] shortTextTopics = inferencer.inferTopics(shortText);
        
        Hashtable<Integer,Double> gridIdScores = gridIdTopicVectors.getSimilarityScores(shortTextTopics);
        
        for (int i = 0; i < gridIds.length; i++) {
            Candidate candidate = candidates[i];
            double score = gridIdScores.get(gridIds[i]);
            System.out.println(score + "\t" + candidate.getPlaceName());
        }
    }

}
