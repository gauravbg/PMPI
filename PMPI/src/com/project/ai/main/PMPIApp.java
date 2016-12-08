package com.project.ai.main;
import java.util.Scanner;

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
	}
	
	
	
}
