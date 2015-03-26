package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


class QueryBuilder
{

	private String fleet;
	private String rankCode;
	private String rosterMonth;
	private String rosterYear;
	private String nonLeaveDays;
	private String uprankInd;
	private List finalRanks;
	private Map brfSubqueries;
/* 
 * Standard constructor with minimum required parameters
 */	
	
 	QueryBuilder(String rankCode, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays)
	{
		this.rankCode=rankCode;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
	}
	
/* 	
 * create with UPRANK option
 */	
 
	QueryBuilder(String rankCode, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays, String uprankInd)
	{
		this.rankCode=rankCode;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
		this.uprankInd=uprankInd;
	}
				 
	void finalizeRanks()
	{

		List finalRanks = new ArrayList();
		Rank rank = new Rank(this.rankCode);
		
		if(!rank.isValid()) {
			System.out.println("Invalid rank"); // convert to logging
		}
		
		if(rankCode.equals("FD")){
			finalRanks.add("CPT");
			finalRanks.add("FO");
		}

		if(rankCode.equals("CPT")){
			finalRanks.add("CPT");
		}
		
		this.finalRanks=finalRanks;			
	}
	
				 
 	void CreateBaseRankFleetComponents(List ranks) // map in case of multi component, I.e, CPT and FO
	{
		Map brfSubqueries = new HashMap();
		
		if(!(ranks.size()==1 || ranks.size()==2)) {
			System.out.println("Invalid rank count, won't proceed"); // add error logging for invalid number of ranks
		}
		
		else {
			for(int i=0; i<2; i++){
				String brfQuery=
				"SELECT /* +RULE*/ DISTINCT A.STAFF_NUM as Staff_Number," + 
				"	A.PREFERRED_NAME," + 
				"	A.FIRST_NAME," + 
				"	A.SURNAME," + 
				"	B.RANK_CD," + 
				"	C.BASE," + 
				"	P.RSRC_GRP_CD" + 
				" FROM CREW_RANK_V B," + 
				"	CREW_BASE_V C," + 
				"	CREW_RSRC_GRP_V P," + 
				"	CREW_V A" + 
				" WHERE A.STAFF_NUM = B.STAFF_NUM" + 
				"	AND A.STAFF_NUM = C.STAFF_NUM" + 
				"	AND A.STAFF_NUM = P.STAFF_NUM" + 
				"	AND (" + 
				"		A.TERM_DT >= to_date((select dt from roster_str),'DDMONYY')" + 
				"		OR A.TERM_DT IS NULL" + 
				"		)" + 
				"	AND (" + 
				"		A.RETR_DT >= to_date((select dt from roster_str),'DDMONYY')" + 
				"		OR A.RETR_DT IS NULL" + 
				"		)" + 
				"	AND (" + 
				"		(" + 
				"			to_date((select dt from roster_str),'DDMONYY') < B.EXP_DT" + 
				"			OR B.EXP_DT IS NULL" + 
				"			)" + 
				"		AND (select end_dt from roster_end) > B.EFF_DT" + 
				"		)" + 
				"	AND (" + 
				"		(" + 
				"			to_date((select dt from roster_str),'DDMONYY') < C.EXP_DT" + 
				"			OR C.EXP_DT IS NULL" + 
				"			)" + 
				"		AND (select end_dt from roster_end) > C.EFF_DT" + 
				"		)" + 
				"	AND A.STAFF_NUM IN (" + 
				"		SELECT STAFF_NUM" + 
				"		FROM CREW_FLEET_V" + 
				"		WHERE VALID_IND = 'Y'" + 
				"			AND FLEET_CD IN (" + 
				"				'"+this.fleet+"'" + 
				"				)" + 
				"			AND (" + 
				"				(" + 
				"					to_date((select dt from roster_str),'DDMONYY') BETWEEN EFF_DT" + 
				"						AND NVL(EXP_DT, TO_DATE('31-DEC-2999', 'DD-MON-YYYY'))" + 
				"					)" + 
				"				OR (" + 
				"					(select end_dt from roster_end) BETWEEN EFF_DT" + 
				"						AND NVL(EXP_DT, TO_DATE('31-DEC-2999', 'DD-MON-YYYY'))" + 
				"					)" + 
				"				)" + 
				"		GROUP BY STAFF_NUM" + 
				"		)" + 
				"	AND B.RANK_CD IN (" + 
				"		'"+ranks.get(i)+"'" + 
				"		)" + 
				"	AND (" + 
				"		(" + 
				"			to_date((select dt from roster_str),'DDMONYY') < P.EXP_DT" + 
				"			OR P.EXP_DT IS NULL" + 
				"			)" + 
				"		AND (select end_dt from roster_end) > P.EFF_DT" + 
				"		)";
				
				brfSubqueries.put(ranks.get(i), brfQuery);
			}
		}
		this.brfSubqueries=brfSubqueries;
	
	}
	
 	void printSubqueries(){
		finalizeRanks();
		CreateBaseRankFleetComponents(finalRanks);
		
		if(this.brfSubqueries==null)
			System.out.println("NULL!");
		else
			System.out.println("NOT NULL!");
		
		System.out.println(brfSubqueries.get(finalRanks.get(0)));
		System.out.println(brfSubqueries.get(finalRanks.get(1)));
	}
	
/* 	Map getQualComponents()
	{
	
	
	}

	String getNonLeaveDaysComponent()
	{
	
	
	}   */
	
}