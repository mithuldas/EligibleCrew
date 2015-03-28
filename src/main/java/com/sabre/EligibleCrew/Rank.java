package com.sabre.EligibleCrew;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class Rank
{
	private static final String[] VALID_RANKS = {"FD","CPT","FO","PA","BCA","GM","BA","EC"};
	private String pairingRank;
	private String uprankInd;
	private String fleetCd;
	private List requiredComponents;
	/* required components is the key for queryComponents */
	private Map queryComponents;
	
	public Rank(String rank, String fleet, String uprank_Ind)
	{
		uprankInd=uprank_Ind;
		pairingRank=rank;
		fleetCd=fleet;
		determineComponents();
	}
	
	public List getPairingRankAsList(){
		List pairingRankList = new ArrayList();
		pairingRankList.add(pairingRank);
		return pairingRankList;
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
		new ArrayList(new RankQualsDAO().getActiveRankQuals());
	
		Iterator iter = ranksThatNeedActiveQual.iterator();
		while(iter.hasNext()){
			if(iter.next().equals(pairingRank))
				return true;
		}
		
		return false;
	}
	
	boolean rankIsCC(){
		if(!pairingRank.equals("FD") 
		&& !pairingRank.equals("FO") 
		&& !pairingRank.equals("CPT"))
		return true;
		
		else return false;
	}
	
	void determineComponents() // needs to be thoroughly tested
	{
		requiredComponents = new ArrayList();	
		queryComponents = new HashMap();
		
		if (pairingRank.equals("FD")){
			List foList = new ArrayList();
			foList.add("FO");
			List cptList = new ArrayList();
			cptList.add("CPT");
		
			requiredComponents.add("FO");
			queryComponents.put("FO", generateComponent("FO", foList, "Simple"));
			requiredComponents.add("CPT");
			queryComponents.put("CPT", generateComponent("CPT", cptList, "Simple"));
		}
		
		if(pairingRank.equals("CPT") || pairingRank.equals("FO")){
			requiredComponents.add(pairingRank);
			queryComponents.put
			(pairingRank, generateComponent(pairingRank, getPairingRankAsList(), "Simple"));
		}
		
		/* if the pairing rank is CC and upranking is not needed*/
		if( rankIsCC() && uprankInd==null) {
			requiredComponents.add(pairingRank);
			if(requiresActiveQual()) {
				queryComponents.put
				(pairingRank, generateComponent(pairingRank, getPairingRankAsList(), "Complex"));
			}
			if(!requiresActiveQual()) {
				queryComponents.put
				(pairingRank, generateComponent(pairingRank, getPairingRankAsList(), "Simple"));
			}
		}
		
		/* if the pairing rank is CC, upranking is required and pairingrank needs qualcheck */
		if(rankIsCC() && uprankInd!=null && requiresActiveQual()) {
			requiredComponents.add(pairingRank);
			List componentRanks = new ArrayList(getPossibleUpranks());
			componentRanks.add(pairingRank);
			queryComponents.put(pairingRank, generateComponent(pairingRank, componentRanks, "Complex"));
		}
		
		if(rankIsCC() && uprankInd!=null && !requiresActiveQual() && getPossibleUpranks()!=null) {
			requiredComponents.add(pairingRank);
			queryComponents.put
			(pairingRank, generateComponent(pairingRank, getPairingRankAsList(), "Simple"));
			
			requiredComponents.add("Uprank");
			queryComponents.put
			("Uprank", generateComponent(pairingRank, getPossibleUpranks(), "Complex"));
		}
	}
	
	String generateComponent(String pairingRank, List componentRanks, String componentType)
	{
		String ranks= transformListToSQL(componentRanks);
		String component=null;
		Map actingRankQuals = new HashMap(new RankQualsDAO().getActingRankQuals(fleetCd));
		
		if(componentType.equals("Simple")){
		component=
		"		AND B.RANK_CD IN ( "+
		"		"+ranks+
		"		) "+
		"	AND ( "+
		"		( "+
		"			to_date((select dt from roster_str),'DDMONYY') < P.EXP_DT "+
		"			OR P.EXP_DT IS NULL "+
		"			) "+
		"		AND (select end_dt from roster_end) > P.EFF_DT "+
		"		) ";
		
		}
		
		if(componentType.equals("Complex")){
			component= 
			"		AND B.RANK_CD IN ( "+
			"		"+ranks+
			"		) "+
			"	AND ( "+
			"		( "+
			"			to_date((select dt from roster_str),'DDMONYY') < P.EXP_DT "+
			"			OR P.EXP_DT IS NULL "+
			"			) "+
			"		AND (select end_dt from roster_end) > P.EFF_DT "+
			"		) "+
			"	and exists (select 1 from crew_qualifications_v q where q.staff_num=a.staff_num and qual_cd='"+actingRankQuals.get(pairingRank)+"' AND ( "+
			"				(EXPIRY_DTS IS NULL) "+
			"				OR EXPIRY_DTS > to_date((select dt from roster_str),'DDMONYY') "+
			"				) "+
			"			AND ISSUED_DTS < (select end_dt from roster_end)) ";
		}
		return component;		
	}
	
	String transformListToSQL(List list){
		String sqlInput="'";
		Iterator iter = list.iterator();
		while(iter.hasNext()){
			sqlInput=sqlInput+iter.next()+"'";
			if(iter.hasNext())
				sqlInput=sqlInput+",'";
		}
		return sqlInput;
	}

	void examineComponents()
	{
		Iterator requiredComponentsIter = requiredComponents.iterator();
		
		while(requiredComponentsIter.hasNext()){
			System.out.println(queryComponents.get(requiredComponentsIter.next())+"\n\n");
		}
	
	}
}