package com.project.ai.interfaces;

import java.util.ArrayList;

public interface ITeamStrength {

	//Number of Wins
	int getNofWinsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
	
	//Number of Draws
	int getNofDrawsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
		
	//Number of Losses
	int getNofLossesHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
	
	//residual matches difficulty
	ArrayList<ArrayList<Integer>> getPreviousStandingsAllOponents(long teamId, String matchId, int howManyPrevSeasons);  
	
	//Returns final total points in previous seasons by that team.
	int getTotalPoints(long teamId, int whichPrevSeason);
	 
}
