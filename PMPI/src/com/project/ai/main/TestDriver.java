package com.project.ai.main;

import com.project.ai.db.DatabaseHelper;

public class TestDriver {

	public void testDBQueries(String dbPath) {
		DatabaseHelper databaseHelper = new DatabaseHelper();
		databaseHelper.setDatabasePath(dbPath);
		//		databaseHelper.getAllMatchesInGameweek("2009/2010", "13");
//		databaseHelper.getListOfPlayersPlayed("10260", "489132", 5);
		databaseHelper.getStandingsOfSeasonForGameWeek("2009/2010", "38");
	}

}
