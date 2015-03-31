package com.sabre.EligibleCrew;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.FileOutputStream;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Driver;
import java.sql.SQLException;

class ConsoleRunner
{	

	private static String options = 
			 "Options: <Rank Code> <Fleet Code> <Roster Month> <Roster Year> "+
			 "<# of non leave days> {upranking indicator} (optional)\n"+
			 "Examples:\nGM 777 3 2015 6 Y\nPA 330 3 2015 3";
			 
	public static void main(String[] args) throws Exception
	{
		Query qb = null;
		
		if(argumentsValid(args)){
			if(args.length==5)
				qb = new Query (args[0], args[1], args[2], args[3], args[4]);
			
			if(args.length==6)
				qb = new Query (args[0], args[1], args[2], args[3], args[4], args[5]);
				
			writeExcel("query_output.xls", qb);
		}
		
	}
	
	static ResultSet getResults(String query) throws Exception {
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		Connection conn;
		Statement stmt;
		conn = DriverManager.getConnection
		("jdbc:oracle:thin:@192.168.0.106:1522:aeroflot", "acdba", "acdba");    
		stmt = conn.createStatement();
							
		return stmt.executeQuery(query);		
	
	}
	
	static boolean argumentsValid(String[] args){
		if(args.length==0){
			System.out.println
			(options);
			 return false;
		}
		
		else if(args.length==5){
			return true;
		}
		
		else if(args.length==6){
			return true;
		}
		
		System.out.println("Invalid number of arguments\n\n" + options);
		return false;
	}
	
	static void writeExcel(String pathAndFilename, Query query) throws Exception{
	
		FileOutputStream out = new FileOutputStream("workbook.xls");
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet();
		HSSFRow row = null;	
					
		ResultSet rs = getResults(query.get());
		//sheet.createRow(0).createCell(0).setCellValue("Cockmagic?");
		int rowCounter=1;
		
		row=sheet.createRow(0);
		row.createCell(0).setCellValue("STAFF_NUMBER");
		row.createCell(1).setCellValue("PREFERRED_NAME");
		row.createCell(2).setCellValue("FIRST_NAME");
		row.createCell(3).setCellValue("SURNAME");
		row.createCell(4).setCellValue("RANK_CD");
		row.createCell(5).setCellValue("BASE");
		row.createCell(6).setCellValue("RSRC_GRP_CD");
		while (rs.next()) {
			row=sheet.createRow(rowCounter);
			row.createCell(0).setCellValue(rs.getString("STAFF_NUMBER"));
			row.createCell(1).setCellValue(rs.getString("PREFERRED_NAME"));
			row.createCell(2).setCellValue(rs.getString("FIRST_NAME"));
			row.createCell(3).setCellValue(rs.getString("SURNAME"));
			row.createCell(4).setCellValue(rs.getString("RANK_CD"));
			row.createCell(5).setCellValue(rs.getString("BASE"));
			row.createCell(6).setCellValue(rs.getString("RSRC_GRP_CD"));
			rowCounter++;
		}
		
		workbook.write(out);
		out.close();
	
	}
}