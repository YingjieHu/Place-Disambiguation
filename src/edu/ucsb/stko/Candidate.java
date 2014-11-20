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
}
