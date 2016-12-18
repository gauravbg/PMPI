package com.project.ai.main;

import java.util.ArrayList;

import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.db.DatabaseHelper;

public class TestDriver {

	public void testDBQueries(String dbPath) {
		DatabaseHelper databaseHelper = new DatabaseHelper();
		databaseHelper.setDatabasePath(dbPath);
		//		databaseHelper.getAllMatchesInGameweek("2009/2010", "13");
		// databaseHelper.getListOfPlayersPlayed("10260", "489132", 5);
		//		databaseHelper.getStandingsOfSeasonForGameWeek("2009/2010", "38");
    // databaseHelper.getPositionOfPlayer("27430", "489132");
		
		ArrayList<PlayerInfo> players = new ArrayList<>();
		PlayerInfo player1 = new PlayerInfo();
		player1.setPlayerName("Darren Fletcher");
		player1.setPlayerId("24148");
		PlayerInfo player2 = new PlayerInfo();
		player2.setPlayerName("Ryan Giggs");
		player2.setPlayerId("24154");
		PlayerInfo player3 = new PlayerInfo();
		player3.setPlayerName("Wayne Rooney");
		player3.setPlayerId("30829");
		PlayerInfo player4 = new PlayerInfo();
		player4.setPlayerName("Cristiano Ronaldo");
		player4.setPlayerId("30893");
		PlayerInfo player5 = new PlayerInfo();
		player5.setPlayerName("Craig Bellamy");
		player5.setPlayerId("33781");
		PlayerInfo player6 = new PlayerInfo();
		player6.setPlayerName("Carlton Cole");
		player6.setPlayerId("34543");
		players.add(player1);
		players.add(player2);
		players.add(player3);
		players.add(player4);
		players.add(player5);
		players.add(player6);
		//databaseHelper.getRatingsOfPlayers(players, "489132");
		
		databaseHelper.getPlayerInfluenceInLastMatches("489132", players, 5);
	}

}
