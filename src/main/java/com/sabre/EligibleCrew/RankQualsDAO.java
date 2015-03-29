package com.sabre.EligibleCrew;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class RankQualsDAO
{
	/* Get a list of ranks that require quals for "Active" ranks as well */
	public List getActiveRankQuals()
	{
		List ActiveRankQuals = new ArrayList();  
		
		try{
		    DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			DriverManager.setLoginTimeout(15);
		    Connection conn = DriverManager.getConnection
		    ("jdbc:oracle:thin:@192.168.0.106:1522:aeroflot", "acdba", "acdba");    
		    Statement stmt = conn.createStatement();
			
			String query = "select active_rank from ACTING_RANK_QUALS_V " +
							"where active_rank=acting_rank and exp_dt is null";
							
		    ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				ActiveRankQuals.add(rs.getString("ACTIVE_RANK"));
		    }
		    conn.close();
		}
		
		catch(SQLException s) {
			System.out.println (s.getMessage());
		}

		return ActiveRankQuals;
	}
	
	public Map getActingRankQuals(String fleetCd)
	{
		Map ActingRankQuals = new HashMap();  
		
		try{
		    DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		    Connection conn = DriverManager.getConnection
		    ("jdbc:oracle:thin:@192.168.0.106:1522:aeroflot", "acdba", "acdba");    
		    Statement stmt = conn.createStatement();
			
			String query = "select distinct acting_rank, qual_cd from ACTING_RANK_QUALS_V "+
							"where fleet_cd= '"+fleetCd+"'";
							
		    ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				ActingRankQuals.put(rs.getString("ACTING_RANK"), rs.getString("QUAL_CD"));
		    }
		    conn.close();
		}
		
		catch(SQLException s) {
			System.out.println (s.getMessage());
		}

		return ActingRankQuals;
	}
}