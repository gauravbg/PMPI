package com.project.ai.interfaces;

public interface ITeamPerformance {

	//numberdeducted: 1= previous year, 2= year bfr previous and so on.
	int getTotalPointsOfPastSeasons(String currentSeasonYear, int yearDedcuted); 
	 
}
