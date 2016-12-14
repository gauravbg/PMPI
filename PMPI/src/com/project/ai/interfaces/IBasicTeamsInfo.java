package com.project.ai.interfaces;
import java.util.ArrayList;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TeamInfo;

public interface IBasicTeamsInfo {

	//Return all matches played in that month
	//Date - mm/yyyy format
	ArrayList<MatchInfo> getAllMatches(String date);
	
	//Return all players playing in that match for that team
	ArrayList<PlayerInfo> getAllPlayers(long teamId, String matchId);
	
	// Return the list of all teams
	ArrayList<TeamInfo> getAllTeams(String year);
	
}
