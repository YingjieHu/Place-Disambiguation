package edu.ucsb.stko;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.StringTokenizer;
import java.util.Vector;

import com.opencsv.CSVReader;

//import au.com.bytecode.opencsv.CSVReader;

public class ModelTrainer 
{

	public static void main(String[] args) 
	{
		try 
		{
			String newlineSymbol = System.getProperty("line.separator");
			
			// set target city
			String disambiguationTarget = "washington";
			
			// construct model
			
			Hashtable<String, Hashtable<String, Double>> wikiModelHashtable = trainModelUsingWikipedia(disambiguationTarget);
			Hashtable<String, Hashtable<String, Double>> dbpediaModelHashtable = trainModelUsingDBpedia(disambiguationTarget);
			Hashtable<String, Hashtable<String, Double>> resultModelHashtable = wikiModelHashtable;//combineHashtables(wikiModelHashtable, dbpediaModelHashtable);
			resultModelHashtable = calculateTFIDF(resultModelHashtable);
			
			Hashtable<String, Hashtable<String, Double>> dbpediaConceptModelHashtable = trainConceptModelUsingDBpedia(disambiguationTarget);
			Hashtable<String, Hashtable<String, Double>> conceptIDFTable = calculateTFIDF(dbpediaConceptModelHashtable);
			
			// open test file
			File testFile = new File("testData.txt");
			FileReader testFileReader = new FileReader(testFile);
			BufferedReader testFileBufferedReader = new BufferedReader(testFileReader);
			
			// calculate prior probability
			Hashtable<String, Double> priorProbabilityTable = new Hashtable<String,Double>();
			String inputLineString = null;
			double totalCount = 0;
			while((inputLineString = testFileBufferedReader.readLine())!= null)
			{
				String[] thisRecordInfo = inputLineString.split("\\|");
				String cityName = thisRecordInfo[0];
				Double recordCount = priorProbabilityTable.get(cityName);
				if(recordCount == null)
					recordCount = new Double(0);
				
				priorProbabilityTable.put(cityName, recordCount+1);
				totalCount = totalCount + 1;
			}
			testFileBufferedReader.close();
			
			Enumeration<String> cityEnumeration = priorProbabilityTable.keys();
			while(cityEnumeration.hasMoreElements())
			{
				String cityName = cityEnumeration.nextElement();
				priorProbabilityTable.put(cityName, priorProbabilityTable.get(cityName)/totalCount);
			}
			
			
			File outputFile = new File("Washington_Wikipedia_only.csv");
			if(outputFile.exists())
			{
				outputFile.delete();
				outputFile.createNewFile();
			}
			FileWriter outputFileWriter = new FileWriter(outputFile,true);
			//outputFileWriter.append("precision,recall,sensitive"+newlineSymbol);
			
			
			
			double lampda = 0.45;
			double highestPrecison = 0;
			double highestPrecisionLapmda = 0;
			
			double sensitiveParameter = -1; 
			
			for(sensitiveParameter= 0; sensitiveParameter<=1.000011;sensitiveParameter+=0.00001)
			{
				testFile = new File("testData.txt");
				testFileReader = new FileReader(testFile);
				testFileBufferedReader = new BufferedReader(testFileReader);
				
				inputLineString = null;
				double correctNum = 0;
				double totalRetrieved = 0;
				while((inputLineString = testFileBufferedReader.readLine())!= null)
				{
					String[] recordInfo = inputLineString.split("\\|");
					String trueCity =  recordInfo[0];
					String citySentence = recordInfo[1];
					
					//System.out.println("True city is :"+trueCity);
					//System.out.println("The sentence is :"+citySentence);
					Vector<String> predictCity = disambiguateUsingTFIDFAndPrior(disambiguationTarget, citySentence, priorProbabilityTable, conceptIDFTable, resultModelHashtable, lampda, sensitiveParameter);
					//Vector<String> predictCity = disambiguateUsingPriorAlone(priorProbabilityTable);
					//System.out.println("Predicted is :");
					for(int i=0;i<predictCity.size();i++)
					{
						String cityName = predictCity.get(i); 
						//System.out.println(cityName);
						
						if(trueCity.equals(cityName))
						{
							correctNum++;
						}
						totalRetrieved++;
					}
					//System.out.println("-------------------------------");
					
					
					//System.out.println("True city is: "+trueCity+", predicted city is: "+predictCity+", prediction is "+isCorrect+"... ");*/
				
				}
				testFileBufferedReader.close();
				
				double thisPrecison = correctNum/(totalRetrieved+0.000001);
				double thisRecall = correctNum/totalCount;
				//System.out.println("retrieved "+totalRetrieved+", totalCount is "+ totalCount);
				System.out.println("precision is "+thisPrecison+", recall is "+ thisRecall+", sensitiveParameter is "+sensitiveParameter+", lambda is "+lampda);
				outputFileWriter.append(thisPrecison+","+thisRecall+","+sensitiveParameter+","+lampda+newlineSymbol);
				/*if(thisPrecison>highestPrecison)
				{
					highestPrecison = thisPrecison;
					highestPrecisionLapmda = lampda;
				}*/
				
				//sensitiveParameter += 0.0001;
				//lampda += 0.01;
				
			}
			outputFileWriter.close();
			
			//System.out.println("Highest precision is: "+highestPrecison+", lampda value is: "+highestPrecisionLapmda);
			
			
			
			
			/*
			Enumeration<String> cityEnumeration = resultModelHashtable.keys();
			while(cityEnumeration.hasMoreElements())
			{
				String cityName = cityEnumeration.nextElement();
				System.out.println("City name is: "+cityName);
				
				Hashtable<String, Double> termFrequencyTable = resultModelHashtable.get(cityName);
				
				Enumeration<String> termStringEnumeration = termFrequencyTable.keys();
				while(termStringEnumeration.hasMoreElements())
				{
					String thisTerm = termStringEnumeration.nextElement();
					Double termCount = termFrequencyTable.get(thisTerm);
					
					System.out.println(" "+thisTerm+": "+termCount);
				}
				
			}*/
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	public static Vector<String> disambiguateUsingTFIDFAndPrior(String disambiguateTarget, String cityDescription, Hashtable<String, Double> priorProbabilityTable,  Hashtable<String, Hashtable<String, Double>> cityConceptIDFTable, Hashtable<String, Hashtable<String, Double>> cityTFIDFTable, double lampda, double sensitiveParameter)
	{
		try 
		{
			// calculate concept matching score
			Hashtable<String, Double> conceptMatchHashtable = new Hashtable<String, Double>();
			Enumeration<String> cityNamesEnumeration = cityConceptIDFTable.keys();
			
			double minEvaluationScore = 1;
			double maxEvaluationScore = 0;
			while(cityNamesEnumeration.hasMoreElements())
			{
				String thisCityName = cityNamesEnumeration.nextElement();
				if(thisCityName.equals("Washington, Kansas") && cityDescription.equals("According to the United States Census Bureau, the borough has a total area of 2.0 square miles (5.1 km2).The surrounding landscape includes rolling hills as well as Pohatcong Mountain a ridge, approximately 6 mi (10 km) long, in the well known Appalachian Mountains that extends from west Phillipsburg northeast approximately to Washington."))
				{
					//System.out.println("here");
				}
				
				Hashtable<String, Double> thisCityConceptTable = cityConceptIDFTable.get(thisCityName);
				
				Enumeration<String> thisCityConceptsEnumeration = thisCityConceptTable.keys();
				double totalScore = 0;
				double matchedScore = 0;
				
				while(thisCityConceptsEnumeration.hasMoreElements())
				{
					String thisConcept = thisCityConceptsEnumeration.nextElement();
					double conceptScore = thisCityConceptTable.get(thisConcept);
					totalScore += conceptScore;
					if(cityDescription.contains(thisConcept))
						matchedScore += conceptScore;
					else
					{
						String[] conceptWords = thisConcept.split(" ");
						if(conceptWords.length>1)
						{
							String lowercasedDescrip = cityDescription.toLowerCase();
							if(lowercasedDescrip.contains(thisConcept.toLowerCase()))
								matchedScore += conceptScore;
						}
					}
				}
				
				double thisEvaluationScore = matchedScore/totalScore;
				conceptMatchHashtable.put(thisCityName, thisEvaluationScore);
				
				if(thisEvaluationScore < minEvaluationScore) minEvaluationScore = thisEvaluationScore;
				if(thisEvaluationScore > maxEvaluationScore) maxEvaluationScore = thisEvaluationScore;
			}
			
			// normalize the concept values
			cityNamesEnumeration = conceptMatchHashtable.keys();
			while(cityNamesEnumeration.hasMoreElements())
			{
				String thisCityName = cityNamesEnumeration.nextElement();
				double cityEvaluationScore = conceptMatchHashtable.get(thisCityName);
				
				double normalizedCityEvaluationScore = (cityEvaluationScore - minEvaluationScore) / (maxEvaluationScore - minEvaluationScore+0.000001);
				conceptMatchHashtable.put(thisCityName, normalizedCityEvaluationScore);
			}
			
			
			
			
			
			
			cityDescription = cityDescription.replaceAll("\\p{Punct}", "").toLowerCase();
			cityDescription = cityDescription.replaceAll("\\s+", " ").trim();
			cityDescription = StopWordsRemover.removeStopWords(cityDescription);
			
			// now begin to construct a hashtable to represent the description 
			Hashtable<String, Double> descriptionHashtable = new Hashtable<String, Double>();
			StringTokenizer descContentTokenizer = new StringTokenizer(cityDescription);
			
			while (descContentTokenizer.hasMoreTokens()) 
			{
				String thisToken =  ensureSingular(descContentTokenizer.nextToken()).trim();
				if(thisToken.equalsIgnoreCase(disambiguateTarget)) continue;
				
				if(thisToken.matches("\\d+"))
				{
					double numberDoubleValue = Double.parseDouble(thisToken);
					String roundedValueString = ""+Math.round(numberDoubleValue);
					thisToken = roundedValueString;
				}
				
				Double tokenCountDouble = descriptionHashtable.get(thisToken);
				if(tokenCountDouble ==  null)
					tokenCountDouble = new Double(0);
				
				descriptionHashtable.put(thisToken, tokenCountDouble+1);
			}
			
			
			Comparator<CityScore> cityComparator = new Comparator<CityScore>() {

				public int compare(CityScore o1, CityScore o2) {
					if(o1.scoreValue < o2.scoreValue)
						return 1;
					else if(o1.scoreValue > o2.scoreValue)
						return -1;
					else 
						return 0;
				}
			};
			
			
			Enumeration<String> cityEnumeration = cityTFIDFTable.keys();
			Hashtable<String, Double> cityContextEvaluationTable = new Hashtable<String, Double>();
			minEvaluationScore = 1;
			maxEvaluationScore = 0;
			while(cityEnumeration.hasMoreElements())
			{
				String thisCityName = cityEnumeration.nextElement();
				Hashtable<String, Double> thisCityTable = cityTFIDFTable.get(thisCityName);
				
				double cosineScore = calculateCosineSimilarity(descriptionHashtable, thisCityTable);
				double thisEvaluationScore = cosineScore* priorProbabilityTable.get(thisCityName);
				
				if(thisEvaluationScore > maxEvaluationScore) maxEvaluationScore = thisEvaluationScore;
				if(thisEvaluationScore < minEvaluationScore) minEvaluationScore = thisEvaluationScore;
				
				cityContextEvaluationTable.put(thisCityName, thisEvaluationScore);
			}
			
			
			
			PriorityQueue<CityScore> priorityQueue = new PriorityQueue<CityScore>(10,cityComparator);
			
			// now do normalization
			cityEnumeration = cityContextEvaluationTable.keys();
			while (cityEnumeration.hasMoreElements()) 
			{
				String thisCityName = cityEnumeration.nextElement();
				double thisEvaluationScore = cityContextEvaluationTable.get(thisCityName);
				
				double normalizedContextScore = (thisEvaluationScore - minEvaluationScore)/ (maxEvaluationScore - minEvaluationScore+0.000000001);
				
				//double totalCandidateScore = lampda * conceptMatchHashtable.get(thisCityName) + (1-lampda)* normalizedContextScore ;
				
				double totalCandidateScore = normalizedContextScore;
				CityScore thisCityScore = new CityScore(thisCityName, totalCandidateScore);//; //
				
				priorityQueue.add(thisCityScore);
			}
			
			
			
			
			// calculate the output places
			Vector<String> resultVector = new Vector<String>();
			CityScore[] candidateCities = new CityScore[priorityQueue.size()];
			int candidateIndex = 0;
	
			while(!priorityQueue.isEmpty())
			{
				CityScore thisCityScore = priorityQueue.remove();
				candidateCities[candidateIndex] = thisCityScore;
				candidateIndex++;
				//resultVector.add(thisCityScore.cityName);
				//System.out.println(thisCityScore.cityName+": "+thisCityScore.scoreValue);
			}
			
			// calculate average difference
			
			
			/*double meanDifference = 0;
			for(int i=0;i<candidateCities.length-1;i++)
			{
				meanDifference += candidateCities[i].scoreValue - candidateCities[i+1].scoreValue;
			}
			meanDifference = meanDifference/ (candidateCities.length-1);
			
			double stdOfCityScore = 0;
			for(int i=0;i<candidateCities.length-1;i++)
			{
				double differency = candidateCities[i].scoreValue - candidateCities[i+1].scoreValue;
				stdOfCityScore += (differency- meanDifference)*(differency- meanDifference);
			}
			stdOfCityScore = Math.sqrt(stdOfCityScore / (candidateCities.length-2));
			
			for(int i=0;i<candidateCities.length-1;i++)
			{
				double differency = candidateCities[i].scoreValue - candidateCities[i+1].scoreValue;
				
				if(differency < (meanDifference+stdOfCityScore))
					resultVector.add(candidateCities[i].cityName);
				else
				{
					resultVector.add(candidateCities[i].cityName);
					break;
				}
			}*/
			
			
			if(sensitiveParameter != -1)
			{
				for(int i=0;i<candidateCities.length;i++)
				{
					if(candidateCities[i].scoreValue >= sensitiveParameter)
						resultVector.add(candidateCities[i].cityName);
				}
			}
			else
			{
				resultVector.add(candidateCities[0].cityName);
			}
			
			
			return resultVector;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static double calculateCosineSimilarity(Hashtable<String, Double> descHashtable, Hashtable<String, Double> candidateHashtable)
	{
		Enumeration<String> descTermsEnumeration = descHashtable.keys();
		double vectorMultiplyValue = 0;
		double descVectorValue = 0;
		double candVectorValue = 0;
		
		while(descTermsEnumeration.hasMoreElements())
		{
			String thisTerm = descTermsEnumeration.nextElement();
			Double descFreq = descHashtable.get(thisTerm);
			Double candFreq = candidateHashtable.get(thisTerm);
			if(candFreq == null)
				candFreq = 0.0;
			
			vectorMultiplyValue += descFreq * candFreq;
			descVectorValue += descFreq * descFreq;
		}
		
		Enumeration<String> candTermEnumeration = candidateHashtable.keys();
		while(candTermEnumeration.hasMoreElements())
		{
			String candTerm = candTermEnumeration.nextElement();
			double candFreq = candidateHashtable.get(candTerm);
			candVectorValue += candFreq * candFreq;
		}
		
		double result = vectorMultiplyValue / (Math.sqrt(descVectorValue) * Math.sqrt(candVectorValue));
		
		return result;
	}
	
	
	public static Vector<String> disambiguateUsingPriorAlone(Hashtable<String, Double> priorProbabilityTable)
	{
		Vector<String> predictionResultVector = new Vector<String>();
		
		Enumeration<String> cityNamesEnumeration = priorProbabilityTable.keys();
		
		double randomScore = Math.random();
		double currentScore = 0;
		String cityName = null;
		while(cityNamesEnumeration.hasMoreElements())
		{
			cityName = cityNamesEnumeration.nextElement();
			double cityPriorProbability = priorProbabilityTable.get(cityName);
			
			currentScore += cityPriorProbability;
			if(currentScore > randomScore)
			{
				predictionResultVector.add(cityName);
				return predictionResultVector;
			}
			
		}
		
		predictionResultVector.add(cityName);
		return predictionResultVector;
		
	}
	
	
	
	public static Hashtable<String, Hashtable<String, Double>> trainModelUsingWikipedia(String disambiguationTarget)
	{
		try 
		{
			File wikiFolder = new File("originWikipedia");
			File[] wikiFiles = wikiFolder.listFiles();
			
			Hashtable<String, Hashtable<String, Double>> resultHashtable = new Hashtable<String, Hashtable<String, Double>>();
			
			for (int i = 0; i < wikiFiles.length; i++) 
			{
				File thisWikiFile = wikiFiles[i];
				String cityName = thisWikiFile.getName().replace(".txt", "");
				
				FileReader thisWikiFileReader = new FileReader(thisWikiFile);
				BufferedReader thisWikiBufferedReader = new BufferedReader(thisWikiFileReader);
				
				StringBuffer contentBuffer = new StringBuffer();
				String inputLine = null;
				
				while ((inputLine = thisWikiBufferedReader.readLine()) != null) 
				{
					contentBuffer.append(inputLine);
				}
				thisWikiBufferedReader.close();
				
				String wikiContent = contentBuffer.toString();
				wikiContent = wikiContent.replaceAll("[^a-zA-Z]"," ").replaceAll("\\s+"," ").toLowerCase();
				wikiContent = StopWordsRemover.removeStopWords(wikiContent);
				
				StringTokenizer wikiContentTokenizer = new StringTokenizer(wikiContent);
				Hashtable<String, Double> thisCityHashtable = new Hashtable<String, Double>();
				while (wikiContentTokenizer.hasMoreTokens()) 
				{
					String thisToken =  ensureSingular(wikiContentTokenizer.nextToken()).trim();
					
					if(thisToken.equalsIgnoreCase(disambiguationTarget)) continue;
					
					Double tokenCountDouble = thisCityHashtable.get(thisToken);
					if(tokenCountDouble ==  null)
						tokenCountDouble = new Double(0);
					
					thisCityHashtable.put(thisToken, tokenCountDouble+1);
				}
				
				resultHashtable.put(cityName, thisCityHashtable);
			}
			
			return resultHashtable;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Hashtable<String, Hashtable<String, Double>> trainConceptModelUsingDBpedia(String disambiguationTarget)
	{
		try 
		{	
			String[] propertyList = {"isPartOf","subdivisionName","deathPlace","location","birthPlace","hometown","city","etymology","placeOfDeath", "routeEnd",
					                 "routeStart","placeOfBirth","placeofburial","district","region","state","nearestCity","seat","countySeat","wikiPageRedirects","nick","governmentType"};
			
			
			File dbpediaFolder = new File("originDBpedia");
			File[] dbFiles = dbpediaFolder.listFiles();
			
			Hashtable<String, Hashtable<String, Double>> resultHashtable = new Hashtable<String, Hashtable<String, Double>>();
			
			for (int i = 0; i < dbFiles.length; i++) 
			{
				File thisDBFile = dbFiles[i];
				String cityName = thisDBFile.getName().replace(".csv", "");
				
				String cityDBpediaName = cityName.replaceAll(" ", "_");
				
				FileReader thisDBFileReader = new FileReader(thisDBFile);
				CSVReader reader = new CSVReader(thisDBFileReader);
			  
				String[] inputLine = reader.readNext();
				Hashtable<String, Double> thisCityHashtable = new Hashtable<String, Double>();
				while ((inputLine = reader.readNext()) != null) 
				{	
					String subjectString = inputLine[0];
					String propertyString = inputLine[1];
					String objectString = inputLine[2];
					
					// examine if this is what we want
					boolean isPropertySuitable = false;
					for(int j=0;j<propertyList.length;j++)
					{
						if(propertyString.contains(propertyList[j]))
						{
							isPropertySuitable = true;
							break;
						}
					}
					
					if(!isPropertySuitable) continue;
					
					String conceptString = objectString;
					if(conceptString.contains(cityDBpediaName))
						conceptString = subjectString;
					
					
					if(!conceptString.startsWith("http://") && !propertyString.contains("nick")) continue;
					
					int lastSlashIndex = conceptString.lastIndexOf("/");
					if(lastSlashIndex!= -1)
						conceptString = conceptString.substring(lastSlashIndex+1);
					
					
					String[] subConceptStrings = conceptString.split(",");
					for(int j=0;j<subConceptStrings.length;j++)
					{
						String thisSubConcept = subConceptStrings[j];
						int parenThesisIndex = thisSubConcept.indexOf("(");
						if(parenThesisIndex!= -1)
							thisSubConcept = thisSubConcept.substring(0, parenThesisIndex);
						
						thisSubConcept = thisSubConcept.replaceAll("_", " ").trim();
						
						if(thisSubConcept.equalsIgnoreCase(disambiguationTarget)) continue;
						if(thisSubConcept.equalsIgnoreCase(disambiguationTarget+" county")) continue;
						if(thisSubConcept.equalsIgnoreCase("United States")) continue;
						
						if(thisSubConcept.length()<3)
						{
							//if(!thisSubConcept.equals(thisSubConcept.toUpperCase()))
								//continue;
							thisSubConcept = thisSubConcept.toUpperCase();
						}
						//thisSubConcept = thisSubConcept.replaceAll("County", "");
						//String[] detailedSubConcepts = thisSubConcept.split(" ");
						
						//for(int k=0;k<detailedSubConcepts.length;k++)
						//{
							thisCityHashtable.put(thisSubConcept.trim(), 1.0);
						//}
						
					}
				}
				reader.close();
				
				resultHashtable.put(cityName,thisCityHashtable);
				
			}			
			return resultHashtable;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	
	
	public static Hashtable<String, Hashtable<String, Double>> trainModelUsingDBpedia(String disambiguationTarget)
	{
		try 
		{
			File dbpediaFolder = new File("originDBpedia");
			File[] dbFiles = dbpediaFolder.listFiles();
			
			Hashtable<String, Hashtable<String, Double>> resultHashtable = new Hashtable<String, Hashtable<String, Double>>();
			
			for (int i = 0; i < dbFiles.length; i++) 
			{
				File thisDBFile = dbFiles[i];
				String cityName = thisDBFile.getName().replace(".csv", "");
				
				FileReader thisDBFileReader = new FileReader(thisDBFile);
				CSVReader reader = new CSVReader(thisDBFileReader);
			  
				String[] inputLine = reader.readNext();
				Hashtable<String, Double> thisCityHashtable = new Hashtable<String, Double>();
				while ((inputLine = reader.readNext()) != null) 
				{				
					String objectString = inputLine[2];
					
					if(objectString.length()<2)
						continue;
					
					Vector<String> objectProcessedValues = new Vector<String>();
					
					if(objectString.matches("\\d+"))
					{
						double numberDoubleValue = Double.parseDouble(objectString);
						String roundedValueString = ""+Math.round(numberDoubleValue);
						objectProcessedValues.add(roundedValueString);
					}
					else 
					{
						
						if(objectString.startsWith("http://"))
						{
							int lastSlashIndex = objectString.lastIndexOf("/");
							objectString = objectString.substring(lastSlashIndex+1);
						}
						
						objectString = objectString.replaceAll("\\d+","");
							
						if(objectString.matches(".*[^a-zA-Z\\p{Punct}].*")) continue;
						
						objectString = objectString.replaceAll("\\p{Punct}"," ").replaceAll("\\s+"," ").trim();
						
						if(objectString.length()<2) continue;
						
						String[] subTermArray = objectString.split(" ");
						
						for (int j = 0; j < subTermArray.length; j++) 
						{
							String detailedTermString = StopWordsRemover.removeStopWords(splitCamelCase(subTermArray[j]));
							if(detailedTermString.length()<2) continue;
							
							String[] detailedTermArray = detailedTermString.split(" ");
							for(int k=0;k<detailedTermArray.length;k++)
							{
								objectProcessedValues.add(ensureSingular(detailedTermArray[k]));
							}
							
						}
					}
					
					
					for(int j=0;j<objectProcessedValues.size();j++)
					{
						String thisTerm = objectProcessedValues.get(j).trim();
						if(thisTerm.equalsIgnoreCase(disambiguationTarget)) continue;
						
						Double termFrequencyDouble = thisCityHashtable.get(thisTerm);
						if(termFrequencyDouble == null)
						{
							termFrequencyDouble = new Double(0);
						}
						thisCityHashtable.put(thisTerm, termFrequencyDouble+1);
					}
					
				}
				reader.close();
				
				resultHashtable.put(cityName,thisCityHashtable);
				
			}			
			return resultHashtable;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Hashtable<String, Hashtable<String, Double>> combineHashtables(Hashtable<String, Hashtable<String, Double>> hashtable1, Hashtable<String, Hashtable<String, Double>> hashtable2)
	{
		try 
		{
			Enumeration<String> cityNamesEnumeration = hashtable1.keys();
			
			Hashtable<String, Hashtable<String, Double>> combinedTable = new Hashtable<String, Hashtable<String, Double>>();
			while(cityNamesEnumeration.hasMoreElements())
			{
				String thisCityName = cityNamesEnumeration.nextElement();
				
				Hashtable<String, Double> thisCityHashtable1 = hashtable1.get(thisCityName);
				Hashtable<String, Double> thisCityHashtable2 = hashtable2.get(thisCityName);
				
				Enumeration<String> thisCityHashtable2Enumeration = thisCityHashtable2.keys();
				while(thisCityHashtable2Enumeration.hasMoreElements())
				{
					String thisTerm = thisCityHashtable2Enumeration.nextElement();
					Double termFrequncy = thisCityHashtable1.get(thisTerm);
					if(termFrequncy == null)
					{
						if(thisTerm.matches("\\w+")) continue;
						
						termFrequncy = new Double(0);
					}
					
					thisCityHashtable1.put(thisTerm, thisCityHashtable2.get(thisTerm)+termFrequncy);
				}
				
				combinedTable.put(thisCityName, thisCityHashtable1);
			}
			
			return combinedTable;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Hashtable<String, Hashtable<String, Double>> calculateTFIDF(Hashtable<String, Hashtable<String, Double>> inputHashtable)
	{
		Enumeration<String> cityNameEnumeration = inputHashtable.keys();
		//Hashtable<String, Hashtable<String, Double>> resultHashtable = new Hashtable<String, Hashtable<String, Double>>();
		
		while(cityNameEnumeration.hasMoreElements())
		{
			String cityName = cityNameEnumeration.nextElement();
			Hashtable<String, Double> cityTermCountHashtable = inputHashtable.get(cityName);
			
			Enumeration<String> termEnumeration = cityTermCountHashtable.keys();
			double totalNumberOfTerms = 0;
			while (termEnumeration.hasMoreElements()) 
			{
				String thisTerm = termEnumeration.nextElement();
				Double thisTermCount = cityTermCountHashtable.get(thisTerm);
				totalNumberOfTerms += thisTermCount;
			}
			
			cityTermCountHashtable.put("city_total_term", totalNumberOfTerms);
			inputHashtable.put(cityName, cityTermCountHashtable);
			
		}
		
		
		cityNameEnumeration = inputHashtable.keys();
		while(cityNameEnumeration.hasMoreElements())
		{
			String cityName = cityNameEnumeration.nextElement();
			Hashtable<String, Double> termCountHashtable = inputHashtable.get(cityName);
			
			Enumeration<String> terms = termCountHashtable.keys();
			double totalTermInCity = termCountHashtable.get("city_total_term");
			while(terms.hasMoreElements())
			{
				String termString = terms.nextElement(); 
				if(termString.equals("city_total_term")) continue;
				
				// TF
				Double countDouble = termCountHashtable.get(termString);
				double termFrequency = countDouble / totalTermInCity;
				
				//IDF
				Enumeration<String> iDFCityEnumeration = inputHashtable.keys();
				double cityNum = inputHashtable.size();
				double cityWithTerm = 0;
				while(iDFCityEnumeration.hasMoreElements())
				{
					String iDFCityName = iDFCityEnumeration.nextElement();
					Hashtable<String, Double> iDFTermCountHashtable = inputHashtable.get(iDFCityName);
					if(iDFTermCountHashtable.get(termString) != null) 
						cityWithTerm++;
				}
				double inverseDFrequency =1+  Math.log((cityNum+1)/cityWithTerm);
				termCountHashtable.put(termString, termFrequency * inverseDFrequency);
			}	
		}
		
		return inputHashtable;
		//
	}
	
	public static String ensureSingular(String word)
	{
		//String[] result = word.split(" ");
		String lastWord = word;//result[result.length-1];
		if(lastWord.endsWith("es"))
		{
				lastWord = Inflector.getInstance().singularize(lastWord);						
		}
		else if (lastWord.endsWith("s") && !lastWord.endsWith("ss")) 
		{
				lastWord = Inflector.getInstance().singularize(lastWord);		
		}
		
		/*String finalResult = "";
		
		for (int i = 0; i < (result.length-1); i++)
		{
				finalResult += result[i]+" ";
		}
		finalResult += lastWord;
		return finalResult;*/
		return lastWord;
	}
	
	public static String splitCamelCase(String s) 
	{
	   return s.replaceAll(
	      String.format("%s|%s|%s",
	         "(?<=[A-Z])(?=[A-Z][a-z])",
	         "(?<=[^A-Z])(?=[A-Z])",
	         "(?<=[A-Za-z])(?=[^A-Za-z])"
	      ),
	      " "
	   );
	}
	
	
	
	

}



class CityScore
{
	public String cityName;
	public double scoreValue;
	
	public CityScore(String cityName, double scoreValue)
	{
		this.cityName = cityName;
		this.scoreValue = scoreValue;
	}
}

