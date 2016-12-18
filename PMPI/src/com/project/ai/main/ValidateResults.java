package com.project.ai.main;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.project.ai.dataclasses.TestMatchResultsInfo;
import com.project.ai.db.DBConnectionManager;

public class ValidateResults extends DBConnectionManager {

	public ValidateResults() {
		super();
	}

	public static int PREDICTION_FOR_HOW_MANY_MATCHES = 5;
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
		System.out.println("Actual results size: " + getActualResults.size());
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
						System.out.println("Goal scorer is: " + goalScorer);
						influentialPlayer.add(goalScorer);

					}
					NodeList assisterList = allGoals.getElementsByTagName("player2");
					for (int k = 0; k < assisterList.getLength(); ++k)
					{
						Element as = (Element) assisterList.item(k);
						String assister = as.getFirstChild().getNodeValue();
						System.out.println("Assister is: " + assister);
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
		System.out.println("No of Influential Players: " + influentialPlayer.size());
		testMatchResults.setListOfInfluentialPlayers(influentialPlayer);
	}

	public HashMap<String, TestMatchResultsInfo> validateResults (String gameweek, String season) {
		HashMap<String, TestMatchResultsInfo> getActualResults = new HashMap<>();
		getActualResult(gameweek, season, getActualResults);

		System.out.println("Player Attributes map size: " + getActualResults.size());
		for (Entry<String, TestMatchResultsInfo> entry : getActualResults.entrySet()) {
			System.out.println("-------------------------------");
			String key = entry.getKey();
			System.out.println("Key: " + key);
			TestMatchResultsInfo values = entry.getValue();
			System.out.println("Values: " + values.getHomeTeamGoals() + " : " + values.getAwayTeamGoals());
			for(int i = 0; i < values.getListOfInfluentialPlayers().size(); i++)
				System.out.println("Influential Players: " + values.getListOfInfluentialPlayers().get(i));
		}

		return getActualResults;
	}
}
