package com.project.ai.db;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

public class DatabaseHelper extends DBConnectionManager implements IBasicTeamsInfo {
	
	public static String EPL_COUNTRY_ID = "1729";

	@Override
	public ArrayList<TeamInfo> getAllTeams(String year) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<MatchInfo> getAllMatches(String date) {
		return null;
	}

	@Override
	public ArrayList<PlayerInfo> getAllPlayers(long teamId, String matchId) {
		// TODO Auto-generated method stub
		return null;
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
