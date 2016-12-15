package com.project.ai.main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import com.project.ai.db.DBConnectionManager;
import com.project.ai.db.DatabaseHelper;

public class PMPIApp {
	
	public static void main(String[] args) {
		
		System.out.println("Welcome to EPL player match performance predictor.");
		System.out.println("Enter 1 for prediction in 2014 season, Enter 2 for prediction in 2015 season: ");
		Scanner in = new Scanner(System.in);
		int selectedYear = in.nextInt();
		if(selectedYear == 1) {
			
		} else if (selectedYear == 2) {
			
		} else {
			System.out.println("Wrong input!");
		}
		
		in.close();
		
		PMPIApp pmpiApp = new PMPIApp();
		pmpiApp.accessDatabaseHelper();
	}
	
	public void accessDatabaseHelper() {
		DatabaseHelper databaseHelper = new DatabaseHelper();
		databaseHelper.getAllMatches("2008-10-29 00:00:00");
		
		databaseHelper.getAllPlayers("10260", "489132");
	}
}
