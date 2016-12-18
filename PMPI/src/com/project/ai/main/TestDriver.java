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

		//		ArrayList<PlayerInfo> players = new ArrayList<>();
		//		PlayerInfo player1 = new PlayerInfo();
		//		player1.setPlayerName("Ryan Giggs");
		//		player1.setPlayerId("24154");
		//		PlayerInfo player2 = new PlayerInfo();
		//		player2.setPlayerName("Wayne Rooney");
		//		player2.setPlayerId("30829");
		//		players.add(player1);
		//		players.add(player2);
		//		databaseHelper.getRatingsOfPlayers(players, "489132");

		databaseHelper.getPositionOfPlayer("27430", "489132");
	}

}
