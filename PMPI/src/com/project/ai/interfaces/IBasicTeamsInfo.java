package com.project.ai.interfaces;
import java.util.ArrayList;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TeamInfo;

public interface IBasicTeamsInfo {

	//Return all teams playing in that season
	//year - yyyy format
	ArrayList<TeamInfo> getAllTeams(String year);
	
	//Return all matches played in that month
	//Date - mm/yyyy format
	ArrayList<MatchInfo> getAllMatches(String date);
	
	//Return all players players in that match for that team
	ArrayList<PlayerInfo> getAllPlayers(long teamId, String matchId);
	
}
