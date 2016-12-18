package com.project.ai.dataclasses;

import java.util.ArrayList;

public class TestMatchResultsInfo {
	
	String matchId;
	String homeTeamId;
	String awayTeamId;
	String homeTeamGoals;
	String awayTeamGoals;
	ArrayList<String> listOfInfluentialPlayers;
	
	public String getMatchId() {
		return matchId;
	}
	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}
	public String getHomeTeamId() {
		return homeTeamId;
	}
	public void setHomeTeamId(String homeTeamId) {
		this.homeTeamId = homeTeamId;
	}
	public String getAwayTeamId() {
		return awayTeamId;
	}
	public void setAwayTeamId(String awayTeamId) {
		this.awayTeamId = awayTeamId;
	}
	public String getHomeTeamGoals() {
		return homeTeamGoals;
	}
	public void setHomeTeamGoals(String homeTeamGoals) {
		this.homeTeamGoals = homeTeamGoals;
	}
	public String getAwayTeamGoals() {
		return awayTeamGoals;
	}
	public void setAwayTeamGoals(String awayTeamGoals) {
		this.awayTeamGoals = awayTeamGoals;
	}
	public ArrayList<String> getListOfInfluentialPlayers() {
		return listOfInfluentialPlayers;
	}
	public void setListOfInfluentialPlayers(ArrayList<String> listOfInfluentialPlayers) {
		this.listOfInfluentialPlayers = listOfInfluentialPlayers;
	}

}
