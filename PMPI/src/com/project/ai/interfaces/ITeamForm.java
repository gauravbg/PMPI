package com.project.ai.interfaces;

public interface ITeamForm {
	
	// Get players who played in the last so many games
	void getPlayersPlayed(String teamId, String matchId, int lastHowManyGames);

}
