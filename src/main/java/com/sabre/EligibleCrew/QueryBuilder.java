package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


class QueryBuilder
{
	private static final String[] VALID_RANKS = {"FD","CPT","FO","PA","BCA","GM","BA","EC"};
	private String fleet;
	private String rank;
	private String rosterMonth;
	private String rosterYear;
	private String nonLeaveDays;
	private String uprankInd;
	
/* 
 * Standard constructor with minimum required parameters
 */	
	
 	QueryBuilder(String rank, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays)
	{
		this.rank=rank;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
	}
	
/* 	
 * create with UPRANK option
 */	
 
	QueryBuilder(String rank, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays, String uprankInd)
	{
		this.rank=rank;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
		this.uprankInd=uprankInd;
	}
				 
	List finalizeRanks()
	{

		List ranks = new ArrayList();
		
		if(!rankIsValid(rank)) {
			System.out.println("Invalid rank"); // convert to logging
			return ranks;
		}
		
		if(rank.equals("FD")){
			ranks.add("CPT");
			ranks.add("FO");
		}

		if(rank.equals("CPT")){
			ranks.add("CPT");
		}
		
		return ranks;			
	}
	
	boolean rankIsValid(String rank)
	{
		boolean rankIsValid = false;
		
		for(int i=0; i<VALID_RANKS.length; i++){
			if(this.rank.equals(VALID_RANKS[i])) {
				rankIsValid = true;
			}
		}
		
		return rankIsValid;
	}
				 
/* 	Map getBaseRankFleetComponents() // map in case of multi component, I.e, CPT and FO
	{
	
	
	}
	
 	Map getQualComponents()
	{
	
	
	}

	String getNonLeaveDaysComponent()
	{
	
	
	}   */
	
}