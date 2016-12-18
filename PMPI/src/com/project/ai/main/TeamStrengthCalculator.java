package com.project.ai.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.db.DatabaseHelper;

import jdistlib.Beta;
import jdistlib.rng.MersenneTwister;

public class TeamStrengthCalculator {

	private MatchInfo match;
	private String gameWeeek;
	private String season;
	
	
	public TeamStrengthCalculator(MatchInfo match, String season, String gameWeek) {		
		
		this.match = match;
		this.gameWeeek = gameWeek;
		this.season = season;
		generateTeamStrength();
	}


	private void generateTeamStrength() {
		
		DatabaseHelper dbHelper = new DatabaseHelper();
		ArrayList<HashMap<String, int[]>> prevSeasonStandings = new ArrayList<>();
		String[] seasonYears = season.split("/");
		System.out.println("S:" +season);
		int[] currYears = {Integer.parseInt(seasonYears[0]), Integer.parseInt(seasonYears[1])};
		for(int i=0; i<1; i++) {
			String prevSeason = Integer.toString(currYears[0]-i-1) +"/" +Integer.toString(currYears[1]-i-1);
			System.out.println(prevSeason);
			prevSeasonStandings.add(dbHelper.getStandingsOfSeason(prevSeason));			
		}
		
		
		int prevGameWeek = Integer.parseInt(gameWeeek)-1;
		HashMap<String, int[]> currSeason = dbHelper.getStandingsOfSeasonForGameWeek(season, Integer.toString(prevGameWeek));
		int[] homeStanding = currSeason.get(match.getHomeTeamId());
		int[] awayStanding = currSeason.get(match.getAwayTeamId());
		
		int homeTeamRank = TeamRanks.getRank(homeStanding[1]);
		System.out.println("Home Team Rank: " + homeTeamRank);
		int awayTeamRank = TeamRanks.getRank(awayStanding[1]);
		System.out.println("Away Team Rank: " + awayTeamRank);
		int winsCountHome = 0;
		int winsCountAway = 0;
		int drawsCountHome = 0;
		int drawsCountAway = 0;
		int lossCountHome = 0;
		int lossCountAway = 0;
		
		prevSeasonStandings.add(currSeason);
		
		for(int i=0; i<prevSeasonStandings.size(); i++) {
			
			winsCountHome += getResultCountForRanks(prevSeasonStandings.get(i), 2, homeTeamRank);
			winsCountAway += getResultCountForRanks(prevSeasonStandings.get(i), 2, awayTeamRank);
			drawsCountHome += getResultCountForRanks(prevSeasonStandings.get(i), 3, homeTeamRank);
			drawsCountAway += getResultCountForRanks(prevSeasonStandings.get(i), 3, awayTeamRank);
			lossCountHome += getResultCountForRanks(prevSeasonStandings.get(i), 4, homeTeamRank);
			lossCountAway += getResultCountForRanks(prevSeasonStandings.get(i), 4, awayTeamRank);
		}
		
		
		int homeWins = calculateMatchOutcomesCount(1000, winsCountHome, drawsCountHome + lossCountHome);
		int awayWins = calculateMatchOutcomesCount(1000, winsCountAway, drawsCountAway + lossCountAway);
		int homeDraws = calculateMatchOutcomesCount(1000, drawsCountHome, winsCountHome + lossCountHome);
		int awayDraws = calculateMatchOutcomesCount(1000, drawsCountAway, winsCountAway + lossCountHome);
		int homeLosses = calculateMatchOutcomesCount(1000, lossCountHome, drawsCountHome + winsCountHome);
		int awayLosses = calculateMatchOutcomesCount(1000, lossCountAway, drawsCountHome + winsCountAway);
		
	}

	private int calculateMatchOutcomesCount(int sampleSize, int alpha, int beta) {
		
		double[] homeWinDist = Beta.random(1000, alpha+1, beta+2, new MersenneTwister());
		double total = 0;
		for(int i=0; i<1000;i++) {
			total += homeWinDist[i];
		}
		double prob = total/1000;
		System.out.println("-----------------------------");
		System.out.println("prob: " + prob);
		System.out.println("Count: " + (int)(prob * Integer.parseInt(gameWeeek)));
		return (int) (prob * Integer.parseInt(gameWeeek));
	}


	private int getResultCountForRanks(HashMap<String, int[]> season, int col, int rank) {
		
		int resultCount = 0;
		Iterator<Entry<String, int[]>> it = season.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        int[] columns =  (int[]) pair.getValue();
	        if(rank == TeamRanks.getRank(columns[1])) {
	        	resultCount = resultCount + columns[col];
	        }
	    }
	    
	    return resultCount;
	}
	
	
}
