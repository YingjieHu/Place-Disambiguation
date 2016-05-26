package edu.ucsb.stko;

public class Candidate {
    private int id = -1;
    private double latitude;
    private double longitude;
    private String placeName;
    
    public Candidate(String placeName, double longitude, double latitude) {
        this.placeName = placeName;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    public Candidate(int id, String placeName, double longitude, double latitude) {
        this.id = id;
        this.placeName = placeName;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    
    public int getId() {
        return id;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public String getPlaceName() {
        return placeName;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
    
    
    public static ArrayList<ArrayList<String>> getFullCandidateListFromFile(String filePath)
    {
    	ArrayList<ArrayList<String>> candidateInfoList = new ArrayList<>();
    	ArrayList<String> placeNames = new ArrayList<>();
    	//ArrayList<String> placeTypes = new ArrayList<>();
    	ArrayList<String> dbLinks = new ArrayList<>();
    	ArrayList<String> lngs = new ArrayList<>();
    	ArrayList<String> lats = new ArrayList<>();    	
    	try {
			CSVReader reader = new CSVReader(new FileReader(filePath), ',');
			String [] nextLine;
			while((nextLine = reader.readNext()) != null)
			{
				placeNames.add(nextLine[0].toLowerCase());
				//placeTypes.add(nextLine[1]);
				dbLinks.add(nextLine[2]);
				lngs.add(nextLine[3]);
				lats.add(nextLine[4]);
			}
			//System.out.println(placeNames.size());
			reader.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	candidateInfoList.add(placeNames);
    	candidateInfoList.add(dbLinks);
    	candidateInfoList.add(lngs);
    	candidateInfoList.add(lats);
    	return candidateInfoList;
    }
    
    
    public static ArrayList<ArrayList<String>> getCandidates(String ambiPlaceName, ArrayList<ArrayList<String>> candidateInfoList)
    {
    	ArrayList<ArrayList<String>> theCandidates = new ArrayList<>();
    	ArrayList<String> thePlaceNames = new ArrayList<>();
    	ArrayList<String> theDbLinks = new ArrayList<>();
    	ArrayList<String> theLngs = new ArrayList<>();
    	ArrayList<String> theLats = new ArrayList<>();    	
    	
    	ArrayList<String> placeNames = candidateInfoList.get(0);
    	ArrayList<String> dbLinks = candidateInfoList.get(1);
    	ArrayList<String> lngs = candidateInfoList.get(2);
    	ArrayList<String> lats = candidateInfoList.get(3);
    	for(int i=0; i<placeNames.size(); i++)
    	{
    		String placeNameWNoStateName = placeNames.get(i).substring(0, placeNames.get(i).indexOf(","));
    		if(placeNameWNoStateName.toLowerCase().contains(ambiPlaceName.toLowerCase()))
    		{
    			thePlaceNames.add(placeNames.get(i));
    			theDbLinks.add(dbLinks.get(i));
    			theLngs.add(lngs.get(i));
    			theLats.add(lats.get(i));
    		}
    	}
    	theCandidates.add(thePlaceNames);
    	theCandidates.add(theDbLinks);
    	theCandidates.add(theLngs);
    	theCandidates.add(theLats);
    	
    	return theCandidates;
    }
    
    
    public static ArrayList<Candidate> constructCandidateList(String ambiPlaceName, String filePath)
    {
    	ArrayList<ArrayList<String>> allCandidates = getFullCandidateListFromFile(filePath);
    	ArrayList<ArrayList<String>> candidates = getCandidates(ambiPlaceName, allCandidates);
    	
    	ArrayList<Candidate> candidateList = new ArrayList<>();
    	for(int i=0; i<candidates.get(0).size(); i++)
    	{
    		String placeEntityName = candidates.get(0).get(i);
    		double placeEntityLng = Double.parseDouble(candidates.get(2).get(i));
    		double placeEntityLat = Double.parseDouble(candidates.get(3).get(i));
    		Candidate entityCandidate= new Candidate(i+1, placeEntityName, placeEntityLng, placeEntityLat);
    		candidateList.add(entityCandidate);
    	}
    	return candidateList;
    }
    
    
    public static void main(String[] args) {
    	final String PathToPlaceNameList = "/home/yiting/Dropbox/STKO_ResearchProject/placeNameDisambiguation/TestingDataSet/MostCommonUSNames_wCoords.csv";
    	ArrayList<Candidate> candidateList = constructCandidateList("Washington", PathToPlaceNameList);
    	for(Candidate candidate:candidateList)
    	{
    		System.out.println(candidate.getId() + ", " + candidate.getPlaceName() + ", " + candidate.getLongitude() + ", " + candidate.getLatitude());
    	}
    }
}
