package com.sabre.EligibleCrew;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

class Qualification
{
	private String[] CC_FLEET_SPECIFIC = {"AC","E","K"};
	private String[] FD_FLEET_SPECIFIC = {"EG","EW","G2","PC","LC"};
	private String[] CC_NON_FLEET_SPECIFIC = 
	{"MC","MCH","DGS","MKK","FAL","FCHK","GO","PARU"};
	private String[] FD_NON_FLEET_SPECIFIC = 
	{"INTF","MC","MCH","PARU"};
	String fleet;
	String pairingRank;
	Map fleetQualNames;
	private List requiredComponents;
	protected Map queryComponents;
	
	public Qualification(String rank, String fleetCd){
		pairingRank=rank;
		fleet=fleetCd;
		fleetQualNames=defineFleetQualNames();
		determineComponents();
	}
	
	public int getNumComponents(){
		return requiredComponents.size();
	}
	
 	String generateNonFleetPart(){
		String nonFleetComponent="";
		if(rankIsCC()){
			for(int i=0; i<CC_NON_FLEET_SPECIFIC.length; i++){
				nonFleetComponent = nonFleetComponent+"and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd='"+CC_NON_FLEET_SPECIFIC[i]+"' and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
				if(i!=CC_NON_FLEET_SPECIFIC.length-1) nonFleetComponent=nonFleetComponent+"\n";
			}
		}
		
		if(!rankIsCC()){
			for(int i=0; i<FD_NON_FLEET_SPECIFIC.length; i++){
				nonFleetComponent = nonFleetComponent+"and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd='"+FD_NON_FLEET_SPECIFIC[i]+"' and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
				if(i!=FD_NON_FLEET_SPECIFIC.length-1) nonFleetComponent=nonFleetComponent+"\n";
			}		
		}
		return nonFleetComponent;
	}
	
	String generateFleetPart(){
		String fleetComponent="";
		if(rankIsCC()){
			for(int i=0; i<CC_FLEET_SPECIFIC.length; i++){
				fleetComponent = fleetComponent+"and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd='"+formFleetSpecQualName(CC_FLEET_SPECIFIC[i])+"' and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
				if(i!=CC_FLEET_SPECIFIC.length-1) fleetComponent=fleetComponent+"\n";
			}
		}
		
		if(!rankIsCC()){
			for(int i=0; i<FD_FLEET_SPECIFIC.length; i++){
				fleetComponent = fleetComponent+"and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd='"+formFleetSpecQualName(FD_FLEET_SPECIFIC[i])+"' and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
				if(i!=FD_FLEET_SPECIFIC.length-1) fleetComponent=fleetComponent+"\n";
			}		
		}
		return fleetComponent;	
	}
	
	String generateLanguagePart(){
		return "and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd in ('ENG4','ENG5','ENG6') and NVL(q.expiry_dts,TO_DATE('31-DEC-2999','DD-MON-YYYY'))>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
	}
	
	String generateG1Part(){
		return "and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd in ('"+formFleetSpecQualName("G1")+"','ALG1') and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440))";
	}
	
	String generateLicenseCheck(String componentRank){
		String lcComp=null;
		
		if(componentRank.equals("FO"))
			lcComp= "and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd in ('CPL','ATPL') and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
		if(componentRank.equals("CPT"))
			lcComp= "and exists (select 1 from crew_qualifications_v q where a.staff_num=q.staff_num and qual_cd='ATPL' and q.expiry_dts>(to_date((select dt from roster_str),'DDMONYY') + 4 + 23/24 + 59/1440)) ";
		
		return lcComp;
	}	
	
	Map defineFleetQualNames()
	{
		Map fleetQualNames = new HashMap();
		if(rankIsCC()){
			fleetQualNames.put("777","77");
			fleetQualNames.put("330","33");
			fleetQualNames.put("SU9","S9");
			fleetQualNames.put("737","73");
			fleetQualNames.put("320","32");
		}
		/* FD */
		else {
			fleetQualNames.put("777","77");
			fleetQualNames.put("330","33");
			fleetQualNames.put("SU9","95");
			fleetQualNames.put("737","37");
			fleetQualNames.put("320","32");
		}
		return fleetQualNames;
	}
	
	public String formFleetSpecQualName(String qualCd)
	{
		String fullQualName=null;
		if(rankIsCC())
			fullQualName=qualCd+fleetQualNames.get(fleet);
		if(!rankIsCC())
			fullQualName=fleetQualNames.get(fleet)+qualCd;
		
		return fullQualName;
	}
	
 	void determineComponents()
	{
		requiredComponents = new ArrayList();	
		queryComponents = new HashMap();
		
		/* This is the only logic that results in more than one component */
		if(pairingRank.equals("FD")){
			requiredComponents.add("FO");
			queryComponents.put("FO", generateComponent("FO"));
			requiredComponents.add("CPT");
			queryComponents.put("CPT", generateComponent("CPT"));
		}
		/* Everything else results in only one component */
		if(!pairingRank.equals("FD")){
			requiredComponents.add(pairingRank);
			queryComponents.put(pairingRank, generateComponent(pairingRank));
		}
	}
	
	String generateComponent(String componentRank){
		String qualComponent=null;
  		if(componentRank.equals("FO") || componentRank.equals("CPT")){
 			qualComponent=	generateNonFleetPart()+ "\n" +
							generateFleetPart()+ "\n" +
							generateLanguagePart()+ "\n" +
							generateG1Part()+ "\n" +
							generateLicenseCheck(componentRank);
		}

		/* if componentrank is any CC rank, generate a simple query */
		if(!componentRank.equals("FO") && !componentRank.equals("CPT")){
			qualComponent=	generateNonFleetPart()+ "\n" +
							generateFleetPart();
		}
		
		return qualComponent;
	}
	
	void examineComponents()
	{
		Iterator requiredComponentsIter = requiredComponents.iterator();
		
		while(requiredComponentsIter.hasNext()){
			System.out.println(queryComponents.get(requiredComponentsIter.next()) + "\n\n");
		}
	}
	
	boolean rankIsCC()
	{
		if(!pairingRank.equals("FD") 
		&& !pairingRank.equals("FO") 
		&& !pairingRank.equals("CPT"))
		return true;
		
		else return false;
	}
	

}