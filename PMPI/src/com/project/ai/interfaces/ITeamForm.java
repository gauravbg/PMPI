package com.project.ai.interfaces;

import java.util.ArrayList;

import com.project.ai.dataclasses.PlayerInfo;

public interface ITeamForm {
	
	// Get players who played in the lastHowManyGames
	ArrayList<PlayerInfo> getListOfPlayersPlayed(String teamId, String matchId, int inHowManyGames);

}
