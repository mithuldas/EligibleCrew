package com.sabre.EligibleCrew;

import java.util.List;
import java.util.ArrayList;

class Rank
{
	private static final String[] VALID_RANKS = {"FD","CPT","FO","PA","BCA","GM","BA","EC"};
	private String rankCode;
	
	public Rank(String rank, String uprankInd)
	{
		rankCode=rank;
	}
	
	public boolean isValid()
	{
		boolean rankIsValid = false;
		
		for(int i=0; i<VALID_RANKS.length; i++){
			if(rankCode.equals(VALID_RANKS[i])) {
				rankIsValid = true;
			}
		}
		return rankIsValid;
	}
	
	public List getQueryRankList() // logic in this function depends on uprank is required
	{
		List finalRanks = new ArrayList();
		
		if(!isValid()) {
			System.out.println("Invalid rank"); // convert to logging
		}
		
		if(rankCode.equals("FD")){
			finalRanks.add("CPT");
			finalRanks.add("FO");
		}

		if(rankCode.equals("CPT")){
			finalRanks.add("CPT");
		}

		return finalRanks;
	}

}