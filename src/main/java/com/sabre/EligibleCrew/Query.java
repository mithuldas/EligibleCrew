package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

class Query
{
	private String fleet;
	private String rankCode;
	private String rosterMonth;
	private String rosterYear;
	private String nonLeaveDays;
	private String uprankInd;
	private String headerSubquery;
	private String baseFleetSubquery;
	private String nonLeaveSubquery;
	private String finalQuery;
	private static final String[] VALID_FLEETS = {"330","320","737","777","SU9"};
	
	/* Standard constructor with minimum required parameters */	
 	Query(String rankCode, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays)
	{
		this.rankCode=rankCode;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
		
		if(validateInput().size()==0){
			generateHeader();
			generateBaseFleet();
			generateNonLeave();
			assembleQuery();
		}
		
		else{ throwInvalidInputError(validateInput()); }
	}
	
	/* 	create with UPRANK option */
	Query(String rankCode, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays, String uprankInd)
	{
		this.rankCode=rankCode;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
		this.uprankInd=uprankInd;
		
		if(validateInput().size()==0){
			generateHeader();
			generateBaseFleet();
			generateNonLeave();
			assembleQuery();
		}
		
		else{ throwInvalidInputError(validateInput()); }
	}
	
	private List validateInput()
	{
		List errorMessages = new ArrayList();
		
		boolean rankIsValid=false;
		boolean fleetIsValid=false;
		boolean rosterMonthIsValid=false;
		boolean rosterYearIsValid=false;
		boolean nonLeaveDaysIsValid=false;
		boolean uprankIsValid=false;
		
		String[] validRanks=Rank.getValidRanks();

		for(int i=0; i<validRanks.length; i++){
			if(validRanks[i].equals(rankCode.toUpperCase()))
				rankIsValid=true; 
		}
		
		for(int i=0; i<VALID_FLEETS.length; i++){
			if(VALID_FLEETS[i].equals(fleet.toUpperCase()))
				fleetIsValid=true;
		}
		
		if(isInteger(rosterMonth) && Integer.parseInt(rosterMonth)>0 && Integer.parseInt(rosterMonth)<=12)
			rosterMonthIsValid=true;
		if(isInteger(rosterYear) && Integer.parseInt(rosterYear)>=2000 && Integer.parseInt(rosterYear)<=2099)
			rosterYearIsValid=true;
		if(isInteger(nonLeaveDays) && Integer.parseInt(nonLeaveDays)>=0 && Integer.parseInt(nonLeaveDays)<=31)
			nonLeaveDaysIsValid=true;
		if((uprankInd!=null && uprankInd.toUpperCase().equals("Y")) || uprankInd==null)
			uprankIsValid=true;
		if(!rankIsValid)
			errorMessages.add("Rank code is invalid ("+rankCode+")");
		if(!fleetIsValid)
			errorMessages.add("Fleet code is invalid ("+fleet+")");
		if(!rosterMonthIsValid)
			errorMessages.add("Roster month must be between 0 and 12 ("+rosterMonth+")");
		if(!rosterYearIsValid)
			errorMessages.add("Roster year must be between 2000 and 2099 ("+rosterYear+")");
		if(!nonLeaveDaysIsValid)
			errorMessages.add("Leave days must be between 0 and 31 ("+nonLeaveDays+")");
		if(!uprankIsValid)
			errorMessages.add("Uprank indicator (if required) should be Y ("+uprankInd+")");
		
		return errorMessages;	
	}
	
	void throwInvalidInputError(List errorMessages){
		String errors = "";
		Iterator iter = errorMessages.iterator();
		while(iter.hasNext()){
			errors=errors+iter.next();
			if(iter.hasNext())
				errors=errors+"\n";
		}
		
		
		throw new IllegalArgumentException("Inputs are invalid:\n"+errors);
	}
	
	public boolean isInteger( String input ) 
	{
		try {
			Integer.parseInt( input );
			return true;
		}
		catch( Exception e ) {
			return false;
		}
	}
	
	
	private void generateHeader()
	{
		String rosterStrDt = generateSqlRosterPeriod();
		
		headerSubquery =
		"with roster_str as (select '"+rosterStrDt+"' as dt from dual), "+
		"	 min_working_days as (select '"+nonLeaveDays+"' as non_leave_days from dual), "+
		"     roster_end as (select last_day(to_date(dt,'DDMONYY HH24MI')+86399/86400) as  end_dt from roster_str) ";
	
	}
	
	String generateSqlRosterPeriod()
	{
		/* Transform rosterMonth and rosterYear inputs into a format agreeable with SQL. */
		if(Integer.parseInt(rosterYear)<2010)
			rosterYear="0"+(Integer.parseInt(rosterYear)-2000);
		else
			rosterYear=String.valueOf(Integer.parseInt(rosterYear)-2000);
		
		if(rosterMonth.equals("1")) { rosterMonth="JAN"; }
		if(rosterMonth.equals("2")) { rosterMonth="FEB"; }
		if(rosterMonth.equals("3")) { rosterMonth="MAR"; }
		if(rosterMonth.equals("4")) { rosterMonth="APR"; }
		if(rosterMonth.equals("5")) { rosterMonth="MAY"; }
		if(rosterMonth.equals("6")) { rosterMonth="JUN"; }
		if(rosterMonth.equals("7")) { rosterMonth="JUL"; }
		if(rosterMonth.equals("8")) { rosterMonth="AUG"; }
		if(rosterMonth.equals("9")) { rosterMonth="SEP"; }
		if(rosterMonth.equals("10")) { rosterMonth="OCT"; }
		if(rosterMonth.equals("11")) { rosterMonth="NOV"; }
		if(rosterMonth.equals("12")) { rosterMonth="DEC"; }

		return "01"+rosterMonth+rosterYear;
	}
				 
 	private void generateBaseFleet() // map in case of multi component, I.e, CPT and FO
	{
		baseFleetSubquery=
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
		"				'"+fleet+"'" + 
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
		"		)";
	}
	
	private void generateNonLeave()
	{
		nonLeaveSubquery =
		"and a.staff_num not in( "+
		"   SELECT DISTINCT A.STAFF_NUM FROM   CREW_RANK_V B,CREW_BASE_V C , CREW_RSRC_GRP_V P,  CREW_V A,  "+
		"(select round(sum(days_off_in_period)) as total_days_off, staff_num from (select r.*, (select end_dt from roster_end)- to_date((select dt from roster_str),'DDMONYY') as days_off_in_period from roster_v r where ACT_STR_DT<to_date((select dt from roster_str),'DDMONYY') and act_end_dt>(select end_dt from roster_end) and duty_cd in (select duty_cd from assignment_types_v where duty_type in ('L','O')) and delete_ind='N' "+
		"union all "+
		"select r.*, act_end_dt-to_date((select dt from roster_str),'DDMONYY') as days_off_in_period from roster_v r where duty_cd in (select duty_cd from assignment_types_v where duty_type in ('L','O')) and ACT_STR_DT<to_date((select dt from roster_str),'DDMONYY') and act_end_dt between to_date((select dt from roster_str),'DDMONYY') and (select end_dt from roster_end) and delete_ind='N' "+
		"union all "+
		"select r.*, (select end_dt from roster_end)-ACT_STR_DT as days_off_in_period from roster_v r where duty_cd in (select duty_cd from assignment_types_v where duty_type in ('L','O')) and ACT_STR_DT between to_date((select dt from roster_str),'DDMONYY') and (select end_dt from roster_end) and act_end_dt> (select end_dt from roster_end) and delete_ind='N' "+
		"union all "+
		"select r.*, ACT_END_DT-ACT_STR_DT as days_off_in_period from roster_v r where duty_cd in (select duty_cd from assignment_types_v where duty_type in ('L','O')) and ACT_STR_DT >=to_date((select dt from roster_str),'DDMONYY') and act_end_dt<= (select end_dt from roster_end) and delete_ind='N') "+
		"group by staff_num order by total_days_off desc) day_off "+
		"WHERE A.STAFF_NUM = B.STAFF_NUM  AND A.STAFF_NUM = C.STAFF_NUM  AND A.STAFF_NUM = P.STAFF_NUM and day_off.staff_num=a.staff_num AND (A.TERM_DT >= to_date((select dt from roster_str),'DDMONYY') OR A.TERM_DT IS NULL )  AND (A.RETR_DT >= to_date((select dt from roster_str),'DDMONYY') OR A.RETR_DT IS NULL )  AND ((to_date((select dt from roster_str),'DDMONYY') < B.EXP_DT OR B.EXP_DT is NULL) AND (select end_dt from roster_end) > B.EFF_DT)  AND ((to_date((select dt from roster_str),'DDMONYY') < C.EXP_DT OR C.EXP_DT is NULL) AND (select end_dt from roster_end) > C.EFF_DT)   AND A.STAFF_NUM IN  (  SELECT STAFF_NUM FROM CREW_FLEET_V  WHERE VALID_IND = 'Y' AND  FLEET_CD IN ('320','330','767','IL9','SU9','M1F','777','737') AND  ( (to_date((select dt from roster_str),'DDMONYY') BETWEEN EFF_DT AND NVL(EXP_DT,TO_DATE('31-DEC-2999','DD-MON-YYYY'))) OR  ((select end_dt from roster_end) BETWEEN EFF_DT AND NVL(EXP_DT,TO_DATE('31-DEC-2999','DD-MON-YYYY'))) )  GROUP BY STAFF_NUM  )  AND (C.BASE IN ('KRR','VVO','KHV','LED','SVO','OVB','CEK','OMS','SVX','IKT','KJA','KGD','BAX','EVN','MRV')  AND C.PRIM_BASE = 'Y')  AND B.RANK_CD IN ('CPT','FO','ENG','NVG','FDT','PA','GM','BCA','EC','CDT','CTN','CIN')  AND ((to_date((select dt from roster_str),'DDMONYY') < P.EXP_DT OR P.EXP_DT is NULL) AND (select end_dt from roster_end) > P.EFF_DT) "+
		"and total_days_off> to_char((select end_dt from roster_end),'DD')- (select non_leave_days from min_working_days)  "+
		") ";
	}
	
	private void assembleQuery(){
		Rank rank = new Rank(rankCode, fleet, uprankInd);
		Qualification qual = new Qualification(rankCode, fleet);
		
		if (rank.getNumComponents()==1){
			finalQuery=	headerSubquery+"\n"+
						baseFleetSubquery+"\n"+
						rank.queryComponents.get(rankCode)+"\n"+
						qual.queryComponents.get(rankCode)+"\n"+
						nonLeaveSubquery;
		}
		
		if (rank.getNumComponents()==2 && qual.getNumComponents()==1){
			finalQuery= headerSubquery+"\n"+
						baseFleetSubquery+"\n"+
						rank.queryComponents.get(rankCode)+"\n"+
						qual.queryComponents.get(rankCode)+"\n"+	
						nonLeaveSubquery+ "\n union \n"+
						baseFleetSubquery+"\n"+
						rank.queryComponents.get("Uprank")+"\n"+
						qual.queryComponents.get(rankCode)+"\n"+	
						nonLeaveSubquery;
						}
						
		if (rank.getNumComponents()==2 && qual.getNumComponents()==2){
			finalQuery= headerSubquery+"\n"+
						baseFleetSubquery+"\n"+
						rank.queryComponents.get("CPT")+"\n"+
						qual.queryComponents.get("CPT")+"\n"+	
						nonLeaveSubquery+ "\n union \n"+
						baseFleetSubquery+"\n"+
						rank.queryComponents.get("FO")+"\n"+
						qual.queryComponents.get("FO")+"\n"+	
						nonLeaveSubquery;
						}
	}
	
	public String get(){
		return finalQuery;
	}
}