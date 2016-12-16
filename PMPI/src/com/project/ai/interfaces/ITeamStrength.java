package com.project.ai.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

public interface ITeamStrength {

	//Number of Wins
	int getNofWinsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
	
	//Number of Draws
	int getNofDrawsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
		
	//Number of Losses
	int getNofLossesHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons);
	
	//residual matches difficulty
	ArrayList<ArrayList<Integer>> getPreviousStandingsAllOpponents(String teamId, String matchId, int howManyPrevSeasons);  
	
	// Get the <standings, points, wins, draws, losses, GF, GA, GD> for a team for a particular season
	HashMap<String, int[]> getStandingsOfPreviousSeason(String season);
	
	//Returns final total points in previous seasons by that team
	int getTotalPointsHistory(String teamId, String whichPrevSeason);
	 
}
