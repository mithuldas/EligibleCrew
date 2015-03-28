package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


class Query
{

	private String fleet;
	private String rankCode;
	private String rosterMonth;
	private String rosterYear;
	private String nonLeaveDays;
	private String uprankInd;
	private String header;
	private String baseFleet;
	private String nonLeave;
/* 
 * Standard constructor with minimum required parameters
 */	
	
 	Query(String rankCode, String fleet, String rosterMonth, String rosterYear,
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
 
	Query(String rankCode, String fleet, String rosterMonth, String rosterYear,
				 String nonLeaveDays, String uprankInd)
	{
		this.rankCode=rankCode;
		this.fleet=fleet;
		this.rosterMonth=rosterMonth;
		this.rosterYear=rosterYear;
		this.nonLeaveDays=nonLeaveDays;
		this.uprankInd=uprankInd;
	}
	
	void generateHeader()
	{
		String rosterStrDt = "01" + rosterMonth + rosterYear;
		
		header =
		"with roster_str as (select '"+rosterStrDt+"' as dt from dual), "+
		"	 min_working_days as (select '"+nonLeaveDays+"' as non_leave_days from dual), "+
		"     roster_end as (select last_day(to_date(dt,'DDMONYY HH24MI')+86399/86400) as  end_dt from roster_str) ";
	
	}
				 
 	void generateBaseFleet() // map in case of multi component, I.e, CPT and FO
	{
		baseFleet=
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
	
	void generateNonLeave()
	{
		nonLeave =
				"   and a.staff_num not in( "+
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
 	void printSubqueries(){
		Rank rank = new Rank(rankCode, uprankInd);
		if(rank.requiresActiveQual())
			System.out.println("Rank needs acting rank qual...");
		else if(!rank.requiresActiveQual())
			System.out.println("Rank does not need acting rank qual...");
		
		rank.examineComponents();
		
/*  		generateHeader();
		generateBaseFleet();
		generateNonLeave();
		
		System.out.println(header);
		System.out.println(baseFleet);
 		System.out.println(brfSubqueries.get(rank.getQueryRankList().get(0)));
		System.out.println(brfSubqueries.get(rank.getQueryRankList().get(1))); 
		System.out.println(nonLeave); */
	}
	


	
}