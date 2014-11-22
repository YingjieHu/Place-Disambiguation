package edu.ucsb.stko;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class TopicInferencerForShortText {
    TopicInferencer inferencer;
    String inferencerFilename = "fuller7_200.inferencer";
    String pipeFilename = "fuller7.mallet";
    int numIterations = 100;
    int thinning = 10;
    int burnIn = 10;
    Pipe instancePipe;
   
    public TopicInferencerForShortText() throws Exception {
        inferencer = TopicInferencer.read(new File(inferencerFilename));
        InstanceList previousInstanceList = InstanceList.load (new File(pipeFilename));
        instancePipe = previousInstanceList.getPipe();
    }
    
    public TopicInferencerForShortText(String inferencerFilename, String pipeFilename) throws Exception {
        this.inferencerFilename = inferencerFilename;
        this.pipeFilename = pipeFilename;
        inferencer = TopicInferencer.read(new File(inferencerFilename));
        InstanceList previousInstanceList = InstanceList.load (new File(pipeFilename));
        instancePipe = previousInstanceList.getPipe();
    }
    
    public double[] inferTopics(String shortText) {
        double[] topicDistribution = null;
        Reader fileReader = new InputStreamReader(new ByteArrayInputStream(shortText.getBytes()));
        InstanceList instances = new InstanceList (instancePipe);
        instances.addThruPipe (new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1));
        for (Instance instance: instances) {      
            topicDistribution = inferencer.getSampledDistribution(instance, numIterations, thinning, burnIn);
            break; //assume only one! TODO: handle multiple
        }
        return topicDistribution;
    }
   
}
