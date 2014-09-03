package com.esri.apl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDataGenerator 
{

	public static void main(String[] args) 
	{
		try 
		{
			String targetCity = "Washington";
			
			File testingDataDirectory = new File("originGovData");
			File[] testDataFiles = testingDataDirectory.listFiles();
			
			File outputTestFile = new File("testData.txt");
			if(outputTestFile.exists())
			{
				outputTestFile.delete();
				outputTestFile.createNewFile();
			}
			FileWriter outputFileWriter = new FileWriter(outputTestFile, true);
			
			String newlineSymbol = System.getProperty("line.separator");
			
			for (File testDataFile : testDataFiles) 
			{
				String cityName = testDataFile.getName().replace(".txt", "");
				
				FileReader thisTestDataFileReader = new FileReader(testDataFile);
				BufferedReader thisTestDataBufferedReader = new BufferedReader(thisTestDataFileReader);
				
				StringBuffer contentBuffer = new StringBuffer();
				String inputString = null;
				
				while((inputString = thisTestDataBufferedReader.readLine())!= null)
				{
					contentBuffer.append(inputString);
				}
				thisTestDataBufferedReader.close();
				
			
				Vector<String> sentencesVector = paragraphBreaker(contentBuffer.toString().replaceAll(newlineSymbol, "  ").replaceAll("\\s+", " "));
				for(int i=0;i<sentencesVector.size();i++)
				{
					String thisSentence = sentencesVector.get(i).trim();
					/*int indexOfPeriod = thisSentence.indexOf(".");
					
					if(indexOfPeriod!= (thisSentence.length()-1))
					{
						while ((indexOfPeriod!= (thisSentence.length()-1)) && (indexOfPeriod!=-1)) 
						{
							if (thisSentence.substring(indexOfPeriod+1, indexOfPeriod+2).matches("\\w")) 
							{
								String subString = thisSentence.substring(0,indexOfPeriod+1);
								
								if(subString.contains(targetCity))
								{
									outputFileWriter.append(cityName+"|"+subString+newlineSymbol);
								}
								
								thisSentence = thisSentence.substring(indexOfPeriod+1);
							}
							
							indexOfPeriod = thisSentence.indexOf(".", indexOfPeriod+1);
						} 
					}*/
					
					if(thisSentence.contains(targetCity))
					{
						outputFileWriter.append(cityName+"|"+thisSentence+newlineSymbol);	
					}
					
					
					
					System.out.println(thisSentence);
				}
				
			}
			
			outputFileWriter.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}
	
	
	public static Vector<String> paragraphBreaker(String paragraph)
	{
		//[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)
	    Pattern re = Pattern.compile("[^.!?][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
	    Matcher reMatcher = re.matcher(paragraph);
	    
	    Vector<String> resultSentencesVector = new Vector<>();
	    while (reMatcher.find()) {
	    	resultSentencesVector.add(reMatcher.group());
	    }
	    return resultSentencesVector;
	}

}
