package com.project.ai.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.project.ai.dataclasses.PlayerInfo;

public interface ITeamForm {
	
	// Get players who played in the lastHowManyGames
	ArrayList<PlayerInfo> getListOfPlayersPlayed(String teamId, String matchId, int inLastHowManyGames);
	
	// Rank players based on their ratings. Returns <PlayerId, Rating (out of 11)>
	HashMap<String, Integer> getRankingOfPlayersFromTeam(ArrayList<PlayerInfo> playerIds, String matchId);

}
