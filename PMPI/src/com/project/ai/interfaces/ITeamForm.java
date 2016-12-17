package com.project.ai.interfaces;

public interface ITeamForm {
	
	// Get players who played in the lastHowManyGames
	void getListOfPlayersPlayed(String teamId, String matchId, int inHowManyGames);

}
