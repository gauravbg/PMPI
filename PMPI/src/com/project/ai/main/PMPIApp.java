package com.project.ai.main;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TestMatchResultsInfo;
import com.project.ai.db.DatabaseHelper;

public class PMPIApp {

	private static ArrayList<MatchInfo> matches;
	private static ArrayList<PlayerInfo> homePlayers;
	private static ArrayList<PlayerInfo> awayPlayers;
	private static int match;
	
	public static void main(String[] args) {

		System.out.println("Welcome to EPL Player Match Performance Index");
		Scanner in = new Scanner(System.in);
		
		if(args[1].equals("TEST")) {
			System.out.println("IN TEST MODE:");
//			//TestDriver testdriver = new TestDriver();
//			//testdriver.testDBQueries(args[0]);
//			ValidateResults valResults = new ValidateResults();
//			valResults.setDatabasePath(args[0]);
//			valResults.getPlayersWhoScoredOrAssisted("489132", "10260");
			//valResults.computeActualResults("37", "2008/2009");
			ValidateResults results = new ValidateResults();
			results.setDatabasePath(args[0]);
			results.compareResults("7", "2015/2016", "", false);
			
		} else {
		
			System.out.print("Enter Season (YYYY/YYYY): ");
			String season = in.next();
			System.out.print("Enter Gameweek (1-38): ");
			String gw = in.next();
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
			System.out.println("Predicting for " + matches.get(match-1).getHomeTeamLongName() + " VS " + matches.get(match-1).getAwayTeamLongName() + ": ");
			PMPIBayesianNetwork bayesNet = new PMPIBayesianNetwork(matches.get(match-1), season, gw);
			TestMatchResultsInfo results  = bayesNet.predict();
			System.out.println();
			System.out.println();
			System.out.println(matches.get(match-1).getHomeTeamLongName() + " VS " + matches.get(match-1).getAwayTeamLongName() + ": ");
			System.out.println("****************************PREDICTED RESULT**********************************");
			System.out.println("Influential players: ");
			ArrayList<String> players = results.getListOfInfluentialPlayers();
			for(int i=0; i<5; i++) {
				String pid = players.get(i);
				for(int j=0; j<homePlayers.size() ;j++) {
					String id = homePlayers.get(j).getPlayerId();
					String id1 = awayPlayers.get(j).getPlayerId();
					if(id.equals(pid)) {
						System.out.println("" + (i+1)+". " + homePlayers.get(j).getPlayerName());						
					}
					if(id1.equals(pid)) {
						System.out.println("" + (i+1)+". " + awayPlayers.get(j).getPlayerName());						
					}
				}
			}
			
			System.out.println("PMPI Score " +matches.get(match-1).getHomeTeamLongName() + ": "  + results.getWinProbabalityForHomeTeam());
			System.out.println("PMPI Score " +matches.get(match-1).getAwayTeamLongName() + ": "  + results.getWinProbabalityForAwayTeam());
			System.out.println("****************************ACTUAL RESULT**********************************");
			ValidateResults actualResultsGen = new ValidateResults();
			int prevGw = Integer.parseInt(gw)-1;
			TestMatchResultsInfo actualResults= actualResultsGen.compareResults(Integer.toString(prevGw), "2015/2016", matches.get(match-1).getMatchId(), true);
			ArrayList<String> pls =actualResults.getListOfInfluentialPlayers();
			System.out.println("Players who scored or assisted: ");
			HashSet<String> hs = new HashSet<>();
			hs.addAll(pls);
			pls.clear();
			pls.addAll(hs);
			for(int i=0; i<pls.size();i++) {
				String pid = pls.get(i);
				for(int j=0; j<homePlayers.size() ;j++) {
					String id = homePlayers.get(j).getPlayerId();
					String id1 = awayPlayers.get(j).getPlayerId();
					if(id.equals(pid)) {
						System.out.println("" + (i+1)+". " + homePlayers.get(j).getPlayerName());						
					}
					if(id1.equals(pid)) {
						System.out.println("" + (i+1)+". " + awayPlayers.get(j).getPlayerName());						
					}
				}
			}
			System.out.println("Actual Score " +matches.get(match-1).getHomeTeamLongName() + ": "  + actualResults.getHomeTeamGoals());
			System.out.println("Actual Score " +matches.get(match-1).getAwayTeamLongName() + ": "  + actualResults.getAwayTeamGoals());
			
			in.close();

		}
		
	}


}
