package com.project.ai.interfaces;

public interface ITeamPerformance {

	//Number of Wins
	int getNofWinsHistory(int rangeMin, int rangeMax, String currentYear, int howManyPrevYears);
	
	//Number of Draws
	int getNofDrawsHistory(int rangeMin, int rangeMax, String currentYear, int howManyPrevYears);
		
	//Number of Losses
	int getNofLossesHistory(int rangeMin, int rangeMax, String currentYear, int howManyPrevYears);	
	 
}
