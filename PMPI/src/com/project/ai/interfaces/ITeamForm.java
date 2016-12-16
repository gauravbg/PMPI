package com.project.ai.interfaces;

public interface ITeamForm {
	
	// Get players who played in the last lastHowManyGames
	void getListOfPlayers(String teamId, String matchId, int lastHowManyGames);

}
