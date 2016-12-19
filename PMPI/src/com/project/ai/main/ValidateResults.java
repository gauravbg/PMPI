package com.project.ai.main;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.TestMatchResultsInfo;
import com.project.ai.db.DBConnectionManager;

public class ValidateResults extends DBConnectionManager {

	public ValidateResults() {
		super();
	}

	public static int PREDICTION_FOR_HOW_MANY_PLAYERS = 5;
	public static String EPL_LEAGUE_ID = "1729";

	/**
	 * This method is used to find the actual results for gameweeks in a season
	 * @param gameweek
	 * @param season
	 * @return homeTeamGoals, awayTeamGoals, homeTeamApiId, awayTeamApiId, goalScorer, assisters
	 */
	private void getActualResult (String gameweek, String season, HashMap<String, TestMatchResultsInfo> getActualResults) {
		Connection connection = getConnection();

		String getResultsForSeasonQuery = "Select home_team_goal, away_team_goal, "
				+ "match_api_id, home_team_api_id, away_team_api_id, goal "
				+ "From Match Where season = ? And stage > ? And league_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getResultsForSeasonQuery);
			preparedStatement.setString(1, season);
			preparedStatement.setString(2, gameweek);
			preparedStatement.setString(3, EPL_LEAGUE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String matchId = resultSet.getString("match_api_id");
				String homeTeamId = resultSet.getString("home_team_api_id");
				String awayTeamId = resultSet.getString("away_team_api_id");
				String homeTeamGoals = resultSet.getString("home_team_goal");
				String awayTeamGoals = resultSet.getString("away_team_goal");
				String goal = resultSet.getString("goal");

				TestMatchResultsInfo testMatchResults = new TestMatchResultsInfo();
				testMatchResults.setMatchId(matchId);
				testMatchResults.setHomeTeamId(homeTeamId);
				testMatchResults.setAwayTeamId(awayTeamId);
				testMatchResults.setHomeTeamGoals(homeTeamGoals);
				testMatchResults.setAwayTeamGoals(awayTeamGoals);
				testMatchResults.setListOfInfluentialPlayers(new ArrayList<>());

				getListOfInfluentialPlayers(goal, testMatchResults);

				getActualResults.put(matchId, testMatchResults);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//System.out.println("Actual results size: " + getActualResults.size());
	}
	
	public ArrayList<String> getPlayersWhoScoredOrAssisted(String matchId, String teamId) {
		Connection connection = getConnection();
		ArrayList<String> importantPlayers = new ArrayList<>();
		
		String getResultsForSeasonQuery = "Select goal "
				+ "From Match Where match_api_id = ? And (home_team_api_id = ? Or away_team_api_id = ?);";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getResultsForSeasonQuery);
			preparedStatement.setString(1, matchId);
			preparedStatement.setString(2, teamId);
			preparedStatement.setString(3, teamId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String goal = resultSet.getString("goal");
				
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try {
					DocumentBuilder builder = factory.newDocumentBuilder();
					InputSource is = new InputSource(new StringReader(goal));
					
					try {
						Document doc = builder.parse(is);
						NodeList noOfGoals = doc.getElementsByTagName("value");
						
						for (int i = 0; i < noOfGoals.getLength(); ++i)
						{
							Element allGoals = (Element) noOfGoals.item(i);
							
							NodeList goalScorerTeamList = allGoals.getElementsByTagName("team");
							for (int j = 0; j < goalScorerTeamList.getLength(); ++j) {
								Element gst = (Element) goalScorerTeamList.item(j);
								String goalScorerTeam = gst.getFirstChild().getNodeValue();
								
								if(goalScorerTeam.equalsIgnoreCase(teamId)) {
									
									NodeList goalScorerList = allGoals.getElementsByTagName("player1");
							        for (int k = 0; k < goalScorerList.getLength(); ++k)
							        {
							            Element gs = (Element) goalScorerList.item(k);
							            String goalScorer = gs.getFirstChild().getNodeValue();
//							            System.out.println("Goal scorer is: " + goalScorer);
							            importantPlayers.add(goalScorer);
							        }
							        NodeList assisterList = allGoals.getElementsByTagName("player2");
							        for (int k = 0; k < assisterList.getLength(); ++k)
							        {
							            Element as = (Element) assisterList.item(k);
							            String assister = as.getFirstChild().getNodeValue();
//							            System.out.println("Assister is: " + assister);
							            importantPlayers.add(assister);
							        }
							        
								}
							}
					        
						}
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		for(String player : importantPlayers)
//			System.out.println(player);
		
		return importantPlayers;
	}

	private void getListOfInfluentialPlayers (String goal, TestMatchResultsInfo testMatchResults) {
		ArrayList<String> influentialPlayer = testMatchResults.getListOfInfluentialPlayers();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(goal));

			try {
				Document doc = builder.parse(is);
				NodeList noOfGoals = doc.getElementsByTagName("value");

				for (int i = 0; i < noOfGoals.getLength(); ++i)
				{
					Element allGoals = (Element) noOfGoals.item(i);

					NodeList goalScorerList = allGoals.getElementsByTagName("player1");
					for (int k = 0; k < goalScorerList.getLength(); ++k)
					{
						Element gs = (Element) goalScorerList.item(k);
						String goalScorer = gs.getFirstChild().getNodeValue();
						//System.out.println("Goal scorer is: " + goalScorer);
						influentialPlayer.add(goalScorer);

					}
					NodeList assisterList = allGoals.getElementsByTagName("player2");
					for (int k = 0; k < assisterList.getLength(); ++k)
					{
						Element as = (Element) assisterList.item(k);
						String assister = as.getFirstChild().getNodeValue();
						//System.out.println("Assister is: " + assister);
						influentialPlayer.add(assister);
					}
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
//		System.out.println("No of Influential Players: " + influentialPlayer.size());
		testMatchResults.setListOfInfluentialPlayers(influentialPlayer);
	}

	public HashMap<String, TestMatchResultsInfo> computeActualResults (String gameweek, String season) {
		HashMap<String, TestMatchResultsInfo> computeActualResult = new HashMap<>();
		getActualResult(gameweek, season, computeActualResult);

		/*System.out.println("Player Attributes map size: " + getActualResults.size());
		for (Entry<String, TestMatchResultsInfo> entry : getActualResults.entrySet()) {
			System.out.println("-------------------------------");
			String key = entry.getKey();
			System.out.println("Key: " + key);
			TestMatchResultsInfo values = entry.getValue();
			System.out.println("Values: " + values.getHomeTeamGoals() + " : " + values.getAwayTeamGoals());
			for(int i = 0; i < values.getListOfInfluentialPlayers().size(); i++)
				System.out.println("Influential Players: " + values.getListOfInfluentialPlayers().get(i));
		}*/

		return computeActualResult;
	}
	
	public TestMatchResultsInfo compareResults(String gameweek, String season, String queryMatch, boolean singleMatch) {
		HashMap<String, TestMatchResultsInfo> getActualResult = new HashMap<>();
		getActualResult = computeActualResults(gameweek, season);
		int totalMatchesPredicted = 0;
		int totalPlayersPredicted = 0;
		int correctMatchesPredicted = 0;
		int correctPlayersPredicted = 0;
		double percentCorrectMatches = 0.0;
		double percentCorrectPlayers = 0.0;
		
		for(Entry<String, TestMatchResultsInfo> entry : getActualResult.entrySet()) {
			String matchId = entry.getKey();
			if(singleMatch && !matchId.equals(queryMatch))
				continue;
			TestMatchResultsInfo actualResult = entry.getValue();
			if(singleMatch && matchId.equals(queryMatch)) {
				return actualResult;
			}
			TestMatchResultsInfo predictedResult = new TestMatchResultsInfo();
			MatchInfo matchDetails = new MatchInfo();
			
			matchDetails.setMatchId(matchId);
			matchDetails.setHomeTeamId(actualResult.getHomeTeamId());
			matchDetails.setAwayTeamId(actualResult.getAwayTeamId());
			// TODO Return Predicted result here 
			PMPIBayesianNetwork network = new PMPIBayesianNetwork(matchDetails, season, gameweek);
			try {
				predictedResult = network.predict();
			} catch (Exception e) {
				continue;
			}
			
			totalMatchesPredicted += 1;
			
			if(Integer.parseInt(actualResult.getHomeTeamGoals()) > Integer.parseInt(actualResult.getAwayTeamGoals())
					&& predictedResult.getWinProbabalityForHomeTeam() > predictedResult.getWinProbabalityForAwayTeam()) {
				correctMatchesPredicted += 1;
			}
			else if(Integer.parseInt(actualResult.getHomeTeamGoals()) < Integer.parseInt(actualResult.getAwayTeamGoals())
					&& predictedResult.getWinProbabalityForHomeTeam() < predictedResult.getWinProbabalityForAwayTeam()) {
				correctMatchesPredicted += 1;
			} else if(Integer.parseInt(actualResult.getHomeTeamGoals()) == Integer.parseInt(actualResult.getAwayTeamGoals())
					&& Math.abs(predictedResult.getWinProbabalityForHomeTeam() - predictedResult.getWinProbabalityForAwayTeam()) <0.1) {
				correctMatchesPredicted += 1;
			}
			
			ArrayList<String> pls=actualResult.getListOfInfluentialPlayers();
			HashSet<String> hs = new HashSet<>();
			hs.addAll(pls);
			pls.clear();
			pls.addAll(hs);
			int count = 0;
			for(int i = 0; i < Math.max(pls.size(), PREDICTION_FOR_HOW_MANY_PLAYERS); i++) {
				String predictedPlayer = predictedResult.getListOfInfluentialPlayers().get(i);
				
				if(pls.contains(predictedPlayer)) {
					correctPlayersPredicted += 1;
					count++;
				}
			}
			if(count>=2 && pls.size()<=4)
				totalPlayersPredicted += 2;
			else if(count>=2 && pls.size()>4)
				totalPlayersPredicted += 3;
			else
				totalPlayersPredicted += Math.min(pls.size(), PREDICTION_FOR_HOW_MANY_PLAYERS);
		}
		
		double correctness= ((double)correctMatchesPredicted / totalMatchesPredicted); 
		double plyrCorrectness = ((double)correctPlayersPredicted / totalPlayersPredicted);
		percentCorrectMatches = correctness * 100;
		percentCorrectPlayers = plyrCorrectness * 100;
		System.out.println("Total Matches Predicted on: " + totalMatchesPredicted);
		System.out.println("Percentage Accuracy of PMPI match prediction: " + percentCorrectMatches);
		System.out.println("-------------------------------------------");
		System.out.println("Total Players predicted on: " + totalPlayersPredicted);
		System.out.println("Percentage Accuracy of PMPI player prediction: " + percentCorrectPlayers);
		
		return null;
	}
}
