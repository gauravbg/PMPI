package com.project.ai.interfaces;
import java.util.ArrayList;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TeamInfo;

public interface IBasicTeamsInfo {

	//Return all matches played in that gameweek
	ArrayList<MatchInfo> getAllMatchesInGameweek(String season, String gameWeek);
	
	//Return all players playing in that match for that team
	ArrayList<PlayerInfo> getAllPlayers(String teamId, String matchId);
	
	// Return the list of all teams
	ArrayList<TeamInfo> getAllTeams(String year);
	
}
