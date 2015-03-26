package com.sabre.EligibleCrew;

class Rank
{
	private static final String[] VALID_RANKS = {"FD","CPT","FO","PA","BCA","GM","BA","EC"};
	private String rankCode;
	
	public Rank(String rank)
	{
		this.rankCode=rank;
	}
	
	public boolean isValid()
	{
		boolean rankIsValid = false;
		
		for(int i=0; i<VALID_RANKS.length; i++){
			if(this.rankCode.equals(VALID_RANKS[i])) {
				rankIsValid = true;
			}
		}
		return rankIsValid;
	}

}