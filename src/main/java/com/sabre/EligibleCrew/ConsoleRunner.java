package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

class ConsoleRunner
{	 
	public static void main(String[] args)
	{
		QueryBuilder qb = new QueryBuilder
						 (args[0], args[1], args[2], args[3], args[4]);
					
		qb.printSubqueries();
/* 		List ranks = new ArrayList();
		ranks = qb.finalizeRanks();
		
		Iterator iter = ranks.iterator();
		while(iter.hasNext()){
			System.out.println(iter.next()); 
		} */
	}
}