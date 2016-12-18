package com.project.ai.main;
import java.util.ArrayList;
import java.util.Scanner;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.db.DatabaseHelper;

public class PMPIApp {

	private static ArrayList<MatchInfo> matches;
	private static ArrayList<PlayerInfo> homePlayers;
	private static ArrayList<PlayerInfo> awayPlayers;
	private static int match;
	private static int team;
	private static int player;
	
	public static void main(String[] args) {

		System.out.println("Welcome to EPL Player Match Performance Index");
		Scanner in = new Scanner(System.in);
		
		if(args[1].equals("TEST")) {
			System.out.println("IN TEST MODE:");
			TestDriver testdriver = new TestDriver();
			testdriver.testDBQueries(args[0]);
			
		} else {
		
			System.out.print("Enter Season (YYYY/YYYY): ");
			String season = "2011/2012";
			System.out.print("Enter Gameweek (1-38): ");
			String gw = "37";
			DatabaseHelper dbHelper = new DatabaseHelper();
			dbHelper.setDatabasePath(args[0]);
			matches = dbHelper.getAllMatchesInGameweek(season, gw);
			for (int i = 0; i < matches.size(); i++) {
				System.out.print(""+(i+1) + ". ");
				System.out.println(matches.get(i).getHomeTeamLongName() + " VS " + matches.get(i).getAwayTeamLongName());
			}
			System.out.println("------------------------------------------------------------------");
			System.out.print("Pick a match (Enter row number): ");
			int match= in.nextInt();
			
			if(match >= 1 && match <=matches.size()){
				homePlayers = dbHelper.getAllPlayers(matches.get(match-1).getHomeTeamId(), matches.get(match-1).getMatchId());
				awayPlayers = dbHelper.getAllPlayers(matches.get(match-1).getAwayTeamId(), matches.get(match-1).getMatchId());
				System.out.println("Home team players:");
				for (int i = 0; i < homePlayers.size(); i++) {
					System.out.print(""+(i+1) + ". ");
					System.out.println(homePlayers.get(i).getPlayerName());
				}
				System.out.println("------------------------------------------------------------------");
				System.out.println("Away team players:");
				for (int i = 0; i < awayPlayers.size(); i++) {
					System.out.print(""+(i+1) + ". ");
					System.out.println(awayPlayers.get(i).getPlayerName());
				}
			}
			
			System.out.println("------------------------------------------------------------------");
			System.out.print("Pick home team or away team (0 or 1): ");
			team= 1;
			System.out.print("Pick a player from the chosen team (Enter row number): ");
			int player= 1;
			System.out.println("------------------------------------------------------------------");
			System.out.println("The algorithm will find player performance index for the following match and player:");
			System.out.println("Match: " + matches.get(match-1).getHomeTeamLongName() + " VS " + matches.get(match-1).getAwayTeamLongName());
			if(team == 0) {
				System.out.println("Player: " + homePlayers.get(player-1).getPlayerName());
			} else {
				System.out.println("Player: " + awayPlayers.get(player-1).getPlayerName());
			}
			TeamStrengthCalculator teamStrength = new TeamStrengthCalculator(matches.get(match-1), (team==0?homePlayers.get(player-1):awayPlayers.get(player-1)), (team==0?true:false), Integer.parseInt(gw));
			
			in.close();

		}
		
		
	}

	private static void predictPlayerPerformace() {

		
	}

}
