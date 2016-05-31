// this class uses test data to test topic modeling

package edu.ucsb.stko;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang3.text.WordUtils;

import com.opencsv.CSVReader;

public class TestModel1 {

	public static void main(String[] args) throws Exception {
		// parameters for reading the testing file
		String filePath = "gold_test_noStateName_r.csv";
		String PathToPlaceNameList = "10MostCommonUSNames_wCoords.csv";
		ArrayList<String> testText = new ArrayList<>();
		ArrayList<String> groundTruth = new ArrayList<>();
		ArrayList<String> surfaceName = new ArrayList<>();
		// reading testing file
		try {
			CSVReader reader = new CSVReader(new FileReader(filePath), '\t');
			String[] nextLine;
			while((nextLine = reader.readNext()) != null)
			{
				testText.add(nextLine[0]);
				groundTruth.add(nextLine[1]);
				surfaceName.add(nextLine[2]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
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
           //String[] gids = lineS[1].substring(1, lineS[1].length()-1).split(",");
           String gid = lineS[1];
           //for (String gid : gids) {
           gridIdTopicVectors.put(Integer.parseInt(gid), topicVector);
           //}
        }
        br.close();
 
        TopicInferencerForShortText inferencer = new TopicInferencerForShortText("fuller7_200.inferencer","fuller7.mallet");
        PointToGridIdMatcher gridIdMatcher = new PointToGridIdMatcher();
        
        File file =new File("testingResult.csv");
    	if(!file.exists()){
    		file.createNewFile();
    	}
        FileWriter fw = new FileWriter(file, true);  // append
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw);
        // pw.println("");
		for (int i = 0; i < testText.size(); i++) {
			// Make the first letter uppercase
			String surfaceForm = WordUtils.capitalize(surfaceName.get(i));
			ArrayList<Candidate> candidateList = Candidate.constructCandidateList(surfaceForm, PathToPlaceNameList);
			String shortText = testText.get(i);
			int[] gridIds = gridIdMatcher.getGridIds(candidateList);
			double[] shortTextTopics = inferencer.inferTopics(shortText);
			Hashtable<Integer,Double> gridIdScores = gridIdTopicVectors.getSimilarityScores(shortTextTopics);
			
	        System.out.println("The text string is: " + shortText);
	        System.out.println("Surface Form: " + surfaceForm);
	        
	        // Use string buffer for faster concatenation
	        StringBuffer temp = new StringBuffer(surfaceName.get(i));
//	        temp.append("\t");
	        for (int j = 0; j < gridIds.length; j++) {
	            Candidate candidate = candidateList.get(j);
	            double score = gridIdScores.get(gridIds[j]);
	            temp.append("\t");
	            temp.append(candidate.getPlaceName());
	            temp.append("\t");
	            temp.append(score);
//	            System.out.println(score + "\t" + candidate.getPlaceName());
	        }
//	        temp.append("\n");
	        pw.println(temp);
	        
//	        break;
		}
		pw.close();

	}

}
