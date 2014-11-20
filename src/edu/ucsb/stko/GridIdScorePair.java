package edu.ucsb.stko;

public class GridIdScorePair {
    int gridId;
    double score;
    
    public GridIdScorePair(int gridId, double score) {
        this.gridId = gridId;
        this.score = score;
    }
    
    public int getGridId() {
        return gridId;
    }
    
    public double getScore() {
        return score;
    }
}
