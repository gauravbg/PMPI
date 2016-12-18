package com.project.ai.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.db.DatabaseHelper;

import jdistlib.Beta;
import jdistlib.Normal;
import jdistlib.Uniform;
import jdistlib.rng.MersenneTwister;

public class TeamStrengthCalculator {

	private MatchInfo match;
	private String gameWeeek;
	private String season;
	private DatabaseHelper dbHelper;
	
	
	public TeamStrengthCalculator(MatchInfo match, String season, String gameWeek) {		
		
		this.match = match;
		this.gameWeeek = gameWeek;
		this.season = season;
		generateTeamStrength();
	}


	private void generateTeamStrength() {
		
		dbHelper = new DatabaseHelper();
		ArrayList<HashMap<String, int[]>> prevSeasonStandings = new ArrayList<>();
		String[] seasonYears = season.split("/");
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
		
		
		double homeWinProb = calculateMatchOutcomesCount(1000, winsCountHome, drawsCountHome + lossCountHome);
		double awayWinProb = calculateMatchOutcomesCount(1000, winsCountAway, drawsCountAway + lossCountAway);
		double homeDrawProb = calculateMatchOutcomesCount(1000, drawsCountHome, winsCountHome + lossCountHome);
		double awayDrawProb = calculateMatchOutcomesCount(1000, drawsCountAway, winsCountAway + lossCountHome);
		double homeLossprob = calculateMatchOutcomesCount(1000, lossCountHome, drawsCountHome + winsCountHome);
		double awayLossProb = calculateMatchOutcomesCount(1000, lossCountAway, drawsCountHome + winsCountAway);
		
		int homeOppDiffculty = getResidualMatchToughness(match.getHomeTeamId());
		int awayOppDiffculty = getResidualMatchToughness(match.getAwayTeamId());
		
		int expHomeTotalPoints = getPredictedPoints(homeWinProb, homeDrawProb, homeLossprob, homeOppDiffculty);
		int expEwayTotalPoints = getPredictedPoints(awayWinProb, awayDrawProb, awayLossProb, awayOppDiffculty);
		
		System.out.println("Future Opp Difficulty Home: " + homeOppDiffculty);
		System.out.println("Future Opp Difficulty Away: " + awayOppDiffculty);
		System.out.println("expHomeTotalPoints: " + expHomeTotalPoints);
		System.out.println("expEwayTotalPoints: " + expEwayTotalPoints);
		
		
		int prevHomeTotal=0;
		int prevAwayTotal=0;
		for(int i=0; i<prevSeasonStandings.size()-1; i++) {
			int[] prevHome = prevSeasonStandings.get(i).get(match.getHomeTeamId());
			int[] prevAway = prevSeasonStandings.get(i).get(match.getAwayTeamId());
			if(prevHome != null)
				prevHomeTotal += prevHome[1];
			else
				prevHomeTotal += 35;
			if(prevAway != null) 
				prevAwayTotal += prevAway[1];
			else
				prevAwayTotal += 35;
		}
		double prevHomeAvg = prevHomeTotal/5;
		double prevAwayAvg = prevAwayTotal/5;
		int homeSquaredTotal = 0;
		int awaySquaredTotal = 0;
		for(int i=0; i<prevSeasonStandings.size()-1; i++) {
			int[] prevHome = prevSeasonStandings.get(i).get(match.getHomeTeamId());
			int[] prevAway = prevSeasonStandings.get(i).get(match.getAwayTeamId());
			if(prevHome != null)
				homeSquaredTotal += (prevHome[1]-prevHomeAvg) * (prevHome[1]-prevHomeAvg);
			else
				homeSquaredTotal += (35-prevHomeAvg) * (35-prevHomeAvg);
			if(prevAway != null) 
				awaySquaredTotal += (prevAway[1]-prevAwayAvg) * (prevAway[1]-prevAwayAvg);
			else
				awaySquaredTotal += (35-prevAwayAvg) * (35-prevAwayAvg);
		}
		double homeVar = homeSquaredTotal/5;
		double awayVar = awaySquaredTotal/5;
		
		
		double[] homeDist = Normal.random(1000, expHomeTotalPoints, Math.sqrt(homeVar), new MersenneTwister());
		double[] awayDist = Normal.random(1000, expEwayTotalPoints, Math.sqrt(awayVar), new MersenneTwister());
		double one = 0;
		double two = 0;
		for(int i=0; i<1000;i++) {
			one += homeDist[i];
			two += awayDist[i];
		}
		int homePointWithInc = (int) (one/1000);
		int awayPointWithInc = (int) (two/1000);
		
		System.out.println("homeVar: " + homeVar);
		System.out.println("awayVar: " + awayVar);
		System.out.println("homePointWithInc: " + homePointWithInc);
		System.out.println("awayPointWithInc: " + awayPointWithInc);
		
		
	}

	private int getPredictedPoints(double WinProb, double DrawProb, double Lossprob, int OppDiffculty) {
		int matchesPlayed = Integer.parseInt(gameWeeek)-1;
		int remMatches = 38-matchesPlayed;
		double currPoints = (matchesPlayed * WinProb * 3) + (matchesPlayed * DrawProb *1);
		double expPoints = ((remMatches * WinProb * 3) + (remMatches * DrawProb *1)) * TeamRanks.getOpponentDifficultyVariance(OppDiffculty, remMatches);
		if(expPoints>114) {
			expPoints = 114;
		}
		return (int) (currPoints + expPoints);
	}


	private int getResidualMatchToughness(String teamId) {
		HashMap<String, int[]> prevStandings = dbHelper.getPreviousStandingsAllOpponents(teamId, match.getMatchId(), 5, "2008/2009");
		Iterator<Entry<String, int[]>> it = prevStandings.entrySet().iterator();
		double totalOpponentScore = 0;
		int oppCount = 0;
	    while (it.hasNext()) {
	    	oppCount++;
	        Map.Entry pair = (Map.Entry)it.next();
	        int[] finishPos =  (int[]) pair.getValue();
	        double[] weights = {0.5, 0.3, 0.1, 0.05, 0.05};
	        double weightedScore = 0;
	        for(int i=0; i<finishPos.length; i++) {
	        	weightedScore += (21-finishPos[i]) * weights[i];
	        }
	        totalOpponentScore += weightedScore;
	    }
		double avgOpponentScore = totalOpponentScore/oppCount;
		return TeamRanks.getOpponentDifficultyRank(avgOpponentScore);
	}


	private double calculateMatchOutcomesCount(int sampleSize, int alpha, int beta) {
		
		double[] homeWinDist = Beta.random(1000, alpha+1, beta+2, new MersenneTwister());
		double total = 0;
		for(int i=0; i<1000;i++) {
			total += homeWinDist[i];
		}
		double prob = total/1000;
		System.out.println("-----------------------------");
		System.out.println("prob: " + prob);
		return prob;
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
