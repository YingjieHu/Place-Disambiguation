package edu.ucsb.stko;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class PointToGridIdMatcher {
    private Connection conn;
    String dbUrl = "jdbc:postgresql://localhost:5432/pdisambig";
    String dbUser = "pdisambig";
    String dbPasswd = "l+>yVcKy+8e&,P";
    
    public PointToGridIdMatcher(String dbUrl, String dbUser, String dbPasswd) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPasswd = dbPasswd;
    }
    
    public PointToGridIdMatcher() {
    }
    
    public int[] getGridIds(ArrayList<Candidate> candidates) throws SQLException {
        conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
        int[] gridIds = new int[candidates.size()];
        
        for (int i = 0; i < candidates.size(); i++) {
            String q = "SELECT gid FROM trigrid.fuller4t7 WHERE ST_Intersects(geom, SELECT ST_SetSRID(ST_MakePoint("+candidates.get(i).getLongitude()+", "+candidates.get(i).getLatitude()+"),4326)) AND ST_Length(ST_LongestLine(geom,geom)) < 300";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(q);
            while (rs.next()) { // should be only 1
                gridIds[i] = rs.getInt(0);
                break;
            }
            rs.close();
            st.close();
        }
        conn.close();
        return gridIds;
    }
}
