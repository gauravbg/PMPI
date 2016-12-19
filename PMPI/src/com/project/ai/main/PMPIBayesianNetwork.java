package com.project.ai.main;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerAttributesInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TestMatchResultsInfo;
import com.project.ai.db.DatabaseHelper;

import jdistlib.Beta;
import jdistlib.Normal;
import jdistlib.Uniform;
import jdistlib.rng.MersenneTwister;

public class PMPIBayesianNetwork {

	private MatchInfo match;
	private String gameWeeek;
	private String season;
	private DatabaseHelper dbHelper;
	
	
	public PMPIBayesianNetwork(MatchInfo match, String season, String gameWeek) {		
		
		this.match = match;
		this.gameWeeek = gameWeek;
		this.season = season;
	}
	
	
	public TestMatchResultsInfo predict() {
		
		TestMatchResultsInfo result = new TestMatchResultsInfo();
		double[] teamStrengthScores = getTeamStrength();
		double[] teamFormScores = getTeamForm();
		double[] teamScores = TeamRanks.getTeamScores(teamStrengthScores, teamFormScores);
		double winProbabalityForAwayTeam = teamScores[1];
		double winProbabalityForHomeTeam = teamScores[0];
		ArrayList<Player> allPlayers = getPlayerPerformnce(teamScores[0], teamScores[1]);
		ArrayList<String> rankedPlayers = new ArrayList<>();
		for(int i=0; i<allPlayers.size(); i++) {
			rankedPlayers.add(allPlayers.get(i).id);
		}
		result.setHomeTeamId(match.getHomeTeamId());
		result.setAwayTeamId(match.getAwayTeamId());
		result.setListOfInfluentialPlayers(rankedPlayers);
		result.setMatchId(match.getMatchId());
		result.setWinProbabalityForAwayTeam(winProbabalityForAwayTeam);
		result.setWinProbabalityForHomeTeam(winProbabalityForHomeTeam);
		return result;
	}


	private double[] getTeamStrength() {
		
		System.out.println("----------------- TEAM STRENGTH ---------------------");
		dbHelper = new DatabaseHelper();
		ArrayList<HashMap<String, int[]>> prevSeasonStandings = new ArrayList<>();
		String[] seasonYears = season.split("/");
		int[] currYears = {Integer.parseInt(seasonYears[0]), Integer.parseInt(seasonYears[1])};
		for(int i=0; i<5; i++) {
			String prevSeason = Integer.toString(currYears[0]-i-1) +"/" +Integer.toString(currYears[1]-i-1);
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
		
		
		double homeWinProb = calculateMatchOutcomesCount(1000, winsCountHome, drawsCountHome + lossCountHome, "Home Win");
		double awayWinProb = calculateMatchOutcomesCount(1000, winsCountAway, drawsCountAway + lossCountAway, "Away Win");
		double homeDrawProb = calculateMatchOutcomesCount(1000, drawsCountHome, winsCountHome + lossCountHome, "Home Draw");
		double awayDrawProb = calculateMatchOutcomesCount(1000, drawsCountAway, winsCountAway + lossCountHome, "Away Draw");
		double homeLossprob = calculateMatchOutcomesCount(1000, lossCountHome, drawsCountHome + winsCountHome, "Home Loss");
		double awayLossProb = calculateMatchOutcomesCount(1000, lossCountAway, drawsCountHome + winsCountAway, "Away Loss");
		
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
			if(prevHome != null) {
				prevHomeTotal += prevHome[1];
			}
			else
				prevHomeTotal += 35;
			if(prevAway != null) 
				prevAwayTotal += prevAway[1];
			else
				prevAwayTotal += 35;
		}
		double prevHomeAvg = prevHomeTotal/5;
		double prevAwayAvg = prevAwayTotal/5;
		
		System.out.println("prevHomeAvg:"+ prevHomeAvg);
		System.out.println("prevAwayAvg:"+ prevAwayAvg);
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
		double homeHistoryVar = homeSquaredTotal/5;
		double awayHistoryVar = awaySquaredTotal/5;
		
		
		double[] homeDist = Normal.random(1000, prevHomeAvg, Math.sqrt(homeHistoryVar), new MersenneTwister());
		double[] awayDist = Normal.random(1000, prevAwayAvg, Math.sqrt(awayHistoryVar), new MersenneTwister());
		double one = 0;
		double two = 0;
		for(int i=0; i<1000;i++) {
			one += homeDist[i];
			two += awayDist[i];
		}
		
		int homePointHistory = (int) (one/1000);
		int awayPointHistory = (int) (two/1000);
		
		System.out.println("homeHistoryVar: " + homeHistoryVar);
		System.out.println("awayHistoryVar: " + awayHistoryVar);
		System.out.println("homePointHistory: " + homePointHistory);
		System.out.println("awayPointHistory: " + awayPointHistory);

		
		int homeTotal = TeamRanks.getTotalPoints(expHomeTotalPoints, homePointHistory, Integer.parseInt(gameWeeek));
		int awayTotal = TeamRanks.getTotalPoints(expEwayTotalPoints, awayPointHistory, Integer.parseInt(gameWeeek));
		
		double homeScore = Math.min(1, (double)homeTotal/100);
		double awayScore = Math.min(1, (double)awayTotal/100);
		System.out.println("homeTotal: " + homeTotal);
		System.out.println("awayTotal: " + awayTotal);
		System.out.println("homeScore: " + homeScore);
		System.out.println("awayScore: " + awayScore);
		
		double[] scores = {Math.min(1, (double)homeTotal/100), Math.min(1, (double)awayTotal/100)};
		return scores;
	}
	
	
	

	private ArrayList<Player> getPlayerPerformnce(double homeTeamStrength, double awayTeamStrength) {
		
		System.out.println("----------------- PLAYER PERFORMANCE ---------------------");
		//team strength
		//player position
		//player form
		//player strength
		ArrayList<PlayerInfo> nextHomepPlayers = dbHelper.getAllPlayers(match.getHomeTeamId(), match.getMatchId());
		ArrayList<PlayerInfo> nextAwayPlayers = dbHelper.getAllPlayers(match.getAwayTeamId(), match.getMatchId());
		
		//Add Sub players here
		ValidateResults results = new ValidateResults();
		ArrayList<String> scorersHome = results.getPlayersWhoScoredOrAssisted(match.getMatchId(), match.getHomeTeamId());
		ArrayList<String> scorersAway = results.getPlayersWhoScoredOrAssisted(match.getMatchId(), match.getAwayTeamId());
		
		for(int i=0; i<scorersHome.size();i++) {
			String player = scorersHome.get(i);
			boolean playerFound = false;
			for(int j=0; j<nextHomepPlayers.size(); j++) {
				
				try {
					
					if(nextHomepPlayers.get(j).getPlayerId().equals(player)) {
						playerFound = true;
						break;
					}
				} catch (Exception e) {
				}
			}
			
			if(!playerFound) {
				PlayerInfo newPlayer = new PlayerInfo();
				newPlayer.setPlayerId(player);
				newPlayer.setPlayerName("Sub Player");
				nextHomepPlayers.add(newPlayer);
			}
		}
		
		for(int i=0; i<scorersAway.size();i++) {
			String player = scorersAway.get(i);
			boolean playerFound = false;
			for(int j=0; j<nextAwayPlayers.size(); j++) {

				try {
					if(nextAwayPlayers.get(j).getPlayerId().equals(player)) {
						playerFound = true;
						break;
					}
				} catch (Exception e) {
				}
				
			}
			
			if(!playerFound) {
				PlayerInfo newPlayer = new PlayerInfo();
				newPlayer.setPlayerId(player);
				newPlayer.setPlayerName("Sub Player");
				nextAwayPlayers.add(newPlayer);
			}
		}
		
		HashMap<String, PlayerAttributesInfo> homePlayerStats = dbHelper.getPlayerInfluenceInLastMatches(match.getMatchId(), nextHomepPlayers, 5);
		HashMap<String, PlayerAttributesInfo> awayPlayerStats = dbHelper.getPlayerInfluenceInLastMatches(match.getMatchId(), nextAwayPlayers, 5);
		
		ArrayList<Player> homePlayers = new ArrayList<>();
		ArrayList<Player> awayPlayers = new ArrayList<>();
		
		for(int i=0; i<nextHomepPlayers.size(); i++) {
			
			String playerId = nextHomepPlayers.get(i).getPlayerId();
			PlayerAttributesInfo playerStats = homePlayerStats.get(playerId);
			System.out.print(nextHomepPlayers.get(i).getPlayerName() + "-->");
			Player newPlayer = new Player(playerId, 0);
			newPlayer.setScore(getPlayerScore(homeTeamStrength, playerStats));
			homePlayers.add(newPlayer);
			
		}
		
		for(int i=0; i<nextAwayPlayers.size(); i++) {
			
			String playerId = nextAwayPlayers.get(i).getPlayerId();
			PlayerAttributesInfo playerStats = awayPlayerStats.get(playerId);
			System.out.print(nextAwayPlayers.get(i).getPlayerName() + "-->");
			Player newPlayer = new Player(playerId, 0);
			newPlayer.setScore(getPlayerScore(awayTeamStrength, playerStats));
			awayPlayers.add(newPlayer);
			
		}
		
		ArrayList<Player> allPlayers = new ArrayList<>();
		allPlayers.addAll(homePlayers);
		allPlayers.addAll(awayPlayers);
		Collections.sort(allPlayers, new Comparator<Player>() {
			@Override
			public int compare(Player arg0, Player arg1) {
				if(arg0.score < arg1.score)
					return 1;
				else if(arg0.score > arg1.score)
					return -1;
				else
					return 0;
			}
	    });
		
		
		return allPlayers;
	}


	private double getPlayerScore(double teamStrength, PlayerAttributesInfo playerStats) {
		
		double playerForm = getPlayerForm(playerStats);
		double pos = TeamRanks.getPositionLikelihood(playerStats.getPosition());
		double rating = (double)playerStats.getOverallRating()/100; 
		double score = 0.3 * teamStrength + 0.31 * pos + 0.29* playerForm + 0.1 *(rating);
		System.out.printf("Score: %.3f ", score);
		System.out.print(" | ");
		System.out.printf("%.3f",teamStrength);
		System.out.print(" | " + pos + " | ");
		System.out.printf("%.3f", playerForm);
		System.out.println(" | " + rating);
		return score;
	}


	private double getPlayerForm(PlayerAttributesInfo playerStats) {
		
		double goals = TeamRanks.scaleGoal(playerStats.getGoals());
		double assists = TeamRanks.scaleGoal(playerStats.getAssists());
		double shotsOn = TeamRanks.scaleGoal(playerStats.getShotsOnTarget());
		double shotsOff = TeamRanks.scaleGoal(playerStats.getShotsOffTarget());
		
		double form = goals * 0.55 + assists * 0.3 + shotsOn * 0.1 + shotsOff * 0.05; 
		return form;
		
	}


	private double[] getTeamForm() {
		
		System.out.println("----------------- TEAM FORM ---------------------");

		ArrayList<ArrayList<Integer>> homePrevResults = dbHelper.getPreviousResults(match.getHomeTeamId(), match.getMatchId(), 5);
		ArrayList<ArrayList<Integer>> awayPrevResults = dbHelper.getPreviousResults(match.getAwayTeamId(), match.getMatchId(), 5);
		
		int homePoints = 0;
		int awayPoints = 0;
		for(int i=0; i<homePrevResults.size(); i++) {
			if(homePrevResults.get(i).get(0)>homePrevResults.get(i).get(1)) {
				//Win
				homePoints +=3;
			} else if(homePrevResults.get(i).get(0)==homePrevResults.get(i).get(1)) {
				homePoints +=1;
			}
		}
		
		for(int i=0; i<awayPrevResults.size(); i++) {
			if(awayPrevResults.get(i).get(0)>awayPrevResults.get(i).get(1)) {
				//Win
				awayPoints +=3;
			} else if(awayPrevResults.get(i).get(0)==awayPrevResults.get(i).get(1)) {
				awayPoints +=1;
			}
		}
		
		double homeForm = ((double)(homePoints+1)/17); //avoiding over confidence in form hence 17
		double awayForm = ((double)(awayPoints+1)/17); //avoiding over confidence in form hence 17
		
		System.out.println("homeForm:" + homeForm);
		System.out.println("awayForm:" + awayForm);
		
		ArrayList<PlayerInfo> homePlayers = dbHelper.getListOfPlayersPlayed(match.getHomeTeamId(), match.getMatchId(), 5);
		ArrayList<PlayerInfo> awayPlayers = dbHelper.getListOfPlayersPlayed(match.getAwayTeamId(), match.getMatchId(), 5);
		
		HashMap<String, Integer> homePlayersRated = dbHelper.getRatingsOfPlayers(homePlayers, match.getMatchId());
		HashMap<String, Integer> awayPlayersRated = dbHelper.getRatingsOfPlayers(awayPlayers, match.getMatchId());
		
		
		ArrayList<Player> sortedHomePlayers = new ArrayList<>();
		ArrayList<Player> sortedAwayPlayers = new ArrayList<>();
		Iterator<Entry<String, Integer>> it = homePlayersRated.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Player player = new Player((String)pair.getKey(), (int)(pair.getValue()));
	        sortedHomePlayers.add(player);
	        }
	    it = awayPlayersRated.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        Player player = new Player((String)pair.getKey(), (int)(pair.getValue()));
	        sortedAwayPlayers.add(player);
	        }
	    	    
	    Collections.sort(sortedAwayPlayers, new Comparator<Player>() {
			@Override
			public int compare(Player arg0, Player arg1) {
				return arg1.rating.compareTo(arg0.rating);
			}
	    });
	    Collections.sort(sortedHomePlayers, new Comparator<Player>() {
			@Override
			public int compare(Player arg0, Player arg1) {
				return arg1.rating.compareTo(arg0.rating);
			}
	    });
	    
		
		ArrayList<PlayerInfo> nextHomepPlayers = dbHelper.getAllPlayers(match.getHomeTeamId(), match.getMatchId());
		ArrayList<PlayerInfo> nextAwayPlayers = dbHelper.getAllPlayers(match.getAwayTeamId(), match.getMatchId());
		
		int homePLayersPlaying = 0;
		for(int i=0; i<11; i++) {
			String playerId = sortedHomePlayers.get(i).id;
			for(int j=0; j<11; j++) {
				PlayerInfo pInfo = nextHomepPlayers.get(j);
				if(pInfo != null) {
				
					String pl = pInfo.getPlayerId();
					if(pl != null && pl.equals(playerId)) {
						homePLayersPlaying++;
						break;
					}
				}
				
			}
		}
		
		int awayPLayersPlaying = 0;
		for(int i=0; i<11; i++) {
			String playerId = sortedAwayPlayers.get(i).id;
			for(int j=0; j<11; j++) {
				try {
				
					if(nextAwayPlayers.get(j).getPlayerId().equals(playerId)) {
						awayPLayersPlaying++;
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		}
		
		System.out.println("homePLayersPlaying: " + homePLayersPlaying);
		System.out.println("awayPLayersPlaying: " + awayPLayersPlaying);
		double[] homeMeanSD = TeamRanks.getMeanSD(homeForm, TeamRanks.getPlayerAvailability(homePLayersPlaying));
		double[] awayMeanSD = TeamRanks.getMeanSD(awayForm, TeamRanks.getPlayerAvailability(awayPLayersPlaying));
		
		System.out.println("homeMeanSD:" + homeMeanSD[0] + ", " +homeMeanSD[1]);
		System.out.println("awayMeanSD:" + awayMeanSD[0] + ", " + awayMeanSD[1]);
		
		double[] homeDist = Normal.random(1000, homeMeanSD[0], homeMeanSD[1], new MersenneTwister());
		double[] awayDist = Normal.random(1000, awayMeanSD[0], awayMeanSD[1], new MersenneTwister());
		double one = 0;
		double two = 0;
		for(int i=0; i<1000;i++) {
			one += homeDist[i];
			two += awayDist[i];
		}
		
		double homeMeanForm = one/1000;
		double awayMeanForm = two/1000;
		
		int homePlayersReturning = 0;
		int awayPlayersReturning = 0;
		for(int i=0; i<11; i++) {
			String playerId = nextHomepPlayers.get(i).getPlayerId();
			int rankSixRating = sortedHomePlayers.get(6).rating;
			ArrayList<PlayerInfo> player = new ArrayList<>();
			player.add(nextHomepPlayers.get(i));
			HashMap<String, Integer> thisPlayerRanking = dbHelper.getRatingsOfPlayers(player, match.getMatchId());
			try {
			
				if(!homePlayersRated.containsKey(playerId) && (thisPlayerRanking.get(playerId) > rankSixRating)) {
					homePlayersReturning++;
				}
			} catch (NullPointerException e) {
			}
			
			
		}
		
		for(int i=0; i<11; i++) {
			String playerId = nextAwayPlayers.get(i).getPlayerId();
			int rankSixRating = sortedAwayPlayers.get(6).rating;
			ArrayList<PlayerInfo> player = new ArrayList<>();
			player.add(nextAwayPlayers.get(i));
			HashMap<String, Integer> thisPlayerRanking = dbHelper.getRatingsOfPlayers(player, match.getMatchId());
			if(!awayPlayersRated.containsKey(playerId) && (thisPlayerRanking.get(playerId) > rankSixRating)) {
				awayPlayersReturning++;
			}
			
		}
		
		System.out.println("homePlayersReturning: " + homePlayersReturning);
		System.out.println("awayPlayersReturning: " + awayPlayersReturning);
		double[] homeMeanSDAftReturn = TeamRanks.getMeanSDAfterPlayerReturn(homeMeanForm, homePlayersReturning);
		double[] awayMeanSDAftReturn = TeamRanks.getMeanSDAfterPlayerReturn(awayMeanForm, awayPlayersReturning);
		
		System.out.println("homeMeanSDAftReturn: " + homeMeanSDAftReturn[0] + "," +homeMeanSDAftReturn[1]);
		System.out.println("awayMeanSDAftReturn: " + awayMeanSDAftReturn[0] + "," +awayMeanSDAftReturn[1]);
		
		
		double[] formScores = {homeMeanSDAftReturn[0], homeMeanSDAftReturn[0]};
		return formScores;
	
	}
	
	
	public HashMap<String, Integer> sortHashMapByValues(
	        HashMap<String, Integer> passedMap) {
		ArrayList<String> mapKeys = new ArrayList<>(passedMap.keySet());
		ArrayList<Integer> mapValues = new ArrayList<>(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	
	    HashMap<String, Integer> sortedMap =
	        new HashMap<>();
	
	    Iterator<Integer> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        int val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();
	
	        while (keyIt.hasNext()) {
	            String key = keyIt.next();
	            int comp1 = passedMap.get(key);
	            int comp2 = val;
	
	            if (comp1 == comp2) {
	                keyIt.remove();
	                sortedMap.put(key, val);
	                break;
	            }
	        }
	    }
	    return sortedMap;
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
		HashMap<String, int[]> prevStandings = dbHelper.getPreviousStandingsAllOpponents(teamId, match.getMatchId(), 5, season);
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


	private double calculateMatchOutcomesCount(int sampleSize, int alpha, int beta, String str) {
		
		double[] homeWinDist = Beta.random(1000, alpha+1, beta+2, new MersenneTwister());
		double total = 0;
		for(int i=0; i<1000;i++) {
			total += homeWinDist[i];
		}
		double prob = total/1000;
		System.out.println(str + " Prob: " + prob);
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
	
	class Player{
		String id;
		Integer rating;
		double score;
		
		Player(String id, Integer rating) {
		
			this.id = id;
			this.rating = rating;
		}
		
		void setScore(double score) {
			
			this.score = score;
		}
	}
	
	
}
