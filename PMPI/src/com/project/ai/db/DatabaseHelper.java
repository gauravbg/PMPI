package com.project.ai.db;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TeamInfo;
import com.project.ai.interfaces.IBasicTeamsInfo;
import com.project.ai.interfaces.ITeamStrength;

public class DatabaseHelper extends DBConnectionManager implements IBasicTeamsInfo, ITeamStrength {

	public static String EPL_LEAGUE_ID = "1729";

	@Override
	public ArrayList<TeamInfo> getAllTeams(String year) {
		Connection connection = getConnection();
		ArrayList<TeamInfo> allTeams = new ArrayList<>();

		String getAllTeamsQuery = "Select distinct A.home_team_api_id, B.team_long_name, B.team_short_name "
				+ "From Match as A, Team as B Where A.home_team_api_id = B.team_api_id And season = ? And league_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getAllTeamsQuery);
			preparedStatement.setString(1, year);
			preparedStatement.setString(2, EPL_LEAGUE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String teamId = resultSet.getString("home_team_api_id");
				String teamLongName = resultSet.getString("team_long_name");
				String teamShortName = resultSet.getString("team_short_name");

				TeamInfo teamInfo = new TeamInfo();
				teamInfo.setTeamId(teamId);
				teamInfo.setTeamLongName(teamLongName);
				teamInfo.setTeamShortName(teamShortName);

				allTeams.add(teamInfo);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allTeams;
	}

	//Date should be in "yyyy-MM-dd hh:mm:ss" format. Use SimpleDateFormat and pass the string
	@Override
	public ArrayList<MatchInfo> getAllMatches(String date) {
		Connection connection = getConnection();
		ArrayList<MatchInfo> allMatches = new ArrayList<>();

		String getAllMatchesOnThisDayQuery = "Select season, date, match_api_id, home_team_api_id, away_team_api_id, league_id "
				+ "From Match Where date = ? And league_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getAllMatchesOnThisDayQuery);
			preparedStatement.setString(1, date);
			preparedStatement.setString(2, EPL_LEAGUE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String season = resultSet.getString("season");
				String dateResult = resultSet.getString("date");
				String match_api_id = resultSet.getString("match_api_id");
				String home_team_api_id = resultSet.getString("home_team_api_id");
				String away_team_api_id = resultSet.getString("away_team_api_id");
				String league_id = resultSet.getString("league_id");

				String[] homeTeamNames = getTeamLongAndShortNames(home_team_api_id);
				String[] awayTeamNames = getTeamLongAndShortNames(away_team_api_id);

				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setHomeTeamId(home_team_api_id);
				matchInfo.setHomeTeamLongName(homeTeamNames[0]);
				matchInfo.setHomeTeamShortName(homeTeamNames[1]);

				matchInfo.setAwayTeamId(away_team_api_id);
				matchInfo.setAwayTeamLongName(homeTeamNames[0]);
				matchInfo.setAwayTeamShortName(homeTeamNames[1]);

				matchInfo.setMatchId(match_api_id);

				allMatches.add(matchInfo);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allMatches;
	}

	@Override
	public ArrayList<PlayerInfo> getAllPlayers(String teamId, String matchId) {
		Connection connection = getConnection();
		ArrayList<PlayerInfo> allPlayers = new ArrayList<>();
		String getHomeAndAwayTeamIdsQuery = "Select home_team_api_id, away_team_api_id, home_player_1, home_player_2, "
				+ "home_player_3, home_player_4, home_player_5, home_player_6, home_player_7, home_player_8, home_player_9,"
				+ "home_player_10, home_player_11, away_player_1, away_player_2, away_player_3, away_player_4, away_player_5,"
				+ "away_player_6, away_player_7, away_player_8, away_player_9, away_player_10, away_player_11"
				+ " From Match Where match_api_id = ?";
		PreparedStatement preparedStatement;

		try {
			preparedStatement = connection.prepareStatement(getHomeAndAwayTeamIdsQuery);
			preparedStatement.setString(1, matchId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String home_team_api_id = resultSet.getString("home_team_api_id");
				String away_team_api_id = resultSet.getString("away_team_api_id");

				StringBuilder baseColumnName = new StringBuilder();
				if(home_team_api_id != null && away_team_api_id != null) {
					if(teamId.equals(home_team_api_id)) {
						baseColumnName.append("home_player_");
					} else if(teamId.equals(away_team_api_id)) {
						baseColumnName.append("away_player_");
					} else {
						break;
					}
					for(int i = 1; i <= 11; i++){
						StringBuilder column = new StringBuilder(baseColumnName);
						String columnName = column.append(i).toString();
						String playerId = resultSet.getString(columnName);
						String playerName = getPlayerName(playerId);

						PlayerInfo playerInfo = new PlayerInfo();
						playerInfo.setPlayerId(playerId);
						playerInfo.setPlayerName(playerName);

						allPlayers.add(playerInfo);
					}
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allPlayers;
	}
	
	@Override
	public int getNofWinsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons) {
		return 0;
	}

	@Override
	public int getNofDrawsHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons) {
		return 0;
	}

	@Override
	public int getNofLossesHistory(int rangeMin, int rangeMax, String currentSeason, int howManyPrevSeasons) {
		return 0;
	}

	@Override
	public ArrayList<ArrayList<Integer>> getPreviousStandingsAllOponents(long teamId, String matchId,
			int howManyPrevSeasons) {
		return null;
	}

	@Override
	public int getTotalPoints(long teamId, int whichPrevSeason) {
		return 0;
	}

	private String[] getTeamLongAndShortNames(String teamApiId) {
		String[] teamNames = new String[2];
		Connection connection = getConnection();
		String getTeamNameQuery = "Select team_long_name, team_short_name From Team Where team_api_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getTeamNameQuery);
			preparedStatement.setString(1, teamApiId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String team_long_name = resultSet.getString("team_long_name");
				String team_short_name = resultSet.getString("team_short_name");
				teamNames[0] = team_long_name;
				teamNames[1] = team_short_name;
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return teamNames;
	}
	
	private String getPlayerName(String playerId) {
		Connection connection = getConnection();
		String playerName = "";
		String getPlayerNameQuery = "Select player_name From Player Where player_api_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getPlayerNameQuery);
			preparedStatement.setString(1, playerId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				playerName = resultSet.getString("player_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return playerName;
	}

	//Just for reference purpose, the following 2 methods will be removed
	public void temporaryDatabaseFunction() {
		Connection connection = getConnection();
		String teamApiId = "";
		String teamName = "Manchester United";
		String getTeamIdQuery = "SELECT team_api_id FROM Team WHERE team_long_name = ?";
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(getTeamIdQuery);
			statement.setString(1, "Manchester United");
			ResultSet resultSet = statement  
					.executeQuery();
			while(resultSet.next()) {
				teamApiId = resultSet.getString("team_api_id");
			}

			//			String query = "SELECT sql FROM sqlite_master WHERE tbl_name = 'Match' AND type = 'table'";
			//			PreparedStatement st = connection.prepareStatement(query);
			//			ResultSet rs = st.executeQuery();
			//			while(rs.next()) {
			//				teamApiId = rs.getString("sql");
			//			}

			System.out.println("id: " + teamApiId);
			findMatchResult(teamApiId);

			resultSet.close();  
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void findMatchResult(String teamApiId) {
		int thisTeamGoalCount = 0;
		int oppositionGoalCount = 0;
		Connection connection = getConnection();
		if(connection != null) {
			String getGoalsQuery = "Select season, date, goal From Match where home_team_api_id = ?";
			try {
				PreparedStatement preparedStatement = connection.prepareStatement(getGoalsQuery);
				preparedStatement.setString(1, teamApiId);
				ResultSet resultSet = preparedStatement.executeQuery();
				resultSet.next();
				resultSet.next();
				String goals = resultSet.getString("goal");
				String season = resultSet.getString("season");
				String date = resultSet.getString("date");

				System.out.println("date: " + date);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				Document doc = null;
				try {
					dBuilder = dbFactory.newDocumentBuilder();
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(goals));
					doc = dBuilder.parse(is);
					doc.getDocumentElement().normalize();

					System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

					NodeList nodeList = doc.getDocumentElement().getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);
						if(node instanceof Element) {
							NodeList childNodes = node.getChildNodes();
							for (int j = 0; j < childNodes.getLength(); j++) {
								Node childNode = childNodes.item(j);
								if(childNode instanceof Element) {
									if(childNode.getNodeName().equals("team")) {
										String scoredBy = childNode.getTextContent();
										if(scoredBy != null) {
											if( scoredBy.equals(teamApiId)) {
												thisTeamGoalCount++;
											} else {
												oppositionGoalCount++;
											}
										}
									}
								}
							}
						}
					}
					if(thisTeamGoalCount == oppositionGoalCount) {
						System.out.println("Draw");
					} else if (thisTeamGoalCount > oppositionGoalCount) {
						System.out.println("Win");
					} else {
						System.out.println("Lose");
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
