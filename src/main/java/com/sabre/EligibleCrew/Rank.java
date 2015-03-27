package com.sabre.EligibleCrew;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

class Rank
{
	private static final String[] VALID_RANKS = {"FD","CPT","FO","PA","BCA","GM","BA","EC"};
	private String pairingRank;
	
	public Rank(String rank, String uprankInd)
	{
		pairingRank=rank;
	}
	
	public boolean isValid()
	{
		boolean rankIsValid = false;
		
		for(int i=0; i<VALID_RANKS.length; i++){
			if(pairingRank.equals(VALID_RANKS[i])) {
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
		
		if(pairingRank.equals("FD")){
			finalRanks.add("CPT");
			finalRanks.add("FO");
		}

		if(pairingRank.equals("CPT")){
			finalRanks.add("CPT");
		}

		return finalRanks;
	}
	
	public List getPossibleUpranks()
	{
		List upranks = new ArrayList();
		
		if(pairingRank.equals("PA") || pairingRank.equals("GM")){
			upranks.add("BCA"); upranks.add("EC");
		}
		
		if(pairingRank.equals("BCA")){
			upranks.add("EC");
		}
		
		return upranks;
	}
	
	public boolean requiresActiveQual()
	{
		List ranksThatNeedActiveQual = 
		new ArrayList(new ActiveRankQualsDAO().getActiveRankQuals());
	
		Iterator iter = ranksThatNeedActiveQual.iterator();
		while(iter.hasNext()){
			if(iter.next().equals(pairingRank))
				return true;
		}
		
		return false;
	}
}