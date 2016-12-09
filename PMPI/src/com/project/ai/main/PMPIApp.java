package com.project.ai.main;
import java.util.Scanner;


public class PMPIApp {

	
	public static void main(String[] args) {
		
		System.out.println("Welcome to EPL player match performance predictor (2015-16 Season).");
		System.out.println("Enter month number (08 to 12, 01 to 05): ");
		Scanner in = new Scanner(System.in);
		int selectedMonth = in.nextInt();
		if(selectedMonth != 6 && selectedMonth != 7) {
			String date = "";
			if(selectedMonth >= 8 && selectedMonth <= 12)
				date = date + selectedMonth + "/2015";
			else
				date = date + selectedMonth + "/2016";
			
		} else {
			System.out.println("Wrong input!");
		}
			
		
		in.close();
	}
	
	
	
}
