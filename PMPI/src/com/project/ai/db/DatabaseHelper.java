package com.project.ai.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.project.ai.dataclasses.MatchInfo;
import com.project.ai.dataclasses.PlayerInfo;
import com.project.ai.dataclasses.TeamInfo;
import com.project.ai.interfaces.IBasicTeamsInfo;
import com.project.ai.interfaces.ITeamForm;
import com.project.ai.interfaces.ITeamStrength;

public class DatabaseHelper extends DBConnectionManager implements IBasicTeamsInfo, ITeamStrength, ITeamForm {

	public static String EPL_LEAGUE_ID = "1729";

	@Override
	public ArrayList<TeamInfo> getAllTeams(String season) {
		Connection connection = getConnection();
		ArrayList<TeamInfo> allTeams = new ArrayList<>();

		String getAllTeamsQuery = "Select distinct A.home_team_api_id, B.team_long_name, B.team_short_name "
				+ "From Match as A, Team as B Where A.home_team_api_id = B.team_api_id And season = ? And league_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getAllTeamsQuery);
			preparedStatement.setString(1, season);
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

	@Override
	public ArrayList<MatchInfo> getAllMatchesInGameweek(String season, String gameWeek) {
		Connection connection = getConnection();
		ArrayList<MatchInfo> allMatches = new ArrayList<>();

		String getAllMatchesOnThisDayQuery = "Select season, date, match_api_id, home_team_api_id, away_team_api_id, league_id "
				+ "From Match Where season = ? And league_id = ? And stage = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getAllMatchesOnThisDayQuery);
			preparedStatement.setString(1, season);
			preparedStatement.setString(2, EPL_LEAGUE_ID);
			preparedStatement.setString(3, gameWeek);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String match_api_id = resultSet.getString("match_api_id");
				String home_team_api_id = resultSet.getString("home_team_api_id");
				String away_team_api_id = resultSet.getString("away_team_api_id");

				String[] homeTeamNames = getTeamLongAndShortNames(home_team_api_id);
				String[] awayTeamNames = getTeamLongAndShortNames(away_team_api_id);

				MatchInfo matchInfo = new MatchInfo();
				matchInfo.setHomeTeamId(home_team_api_id);
				matchInfo.setHomeTeamLongName(homeTeamNames[0]);
				matchInfo.setHomeTeamShortName(homeTeamNames[1]);

				matchInfo.setAwayTeamId(away_team_api_id);
				matchInfo.setAwayTeamLongName(awayTeamNames[0]);
				matchInfo.setAwayTeamShortName(awayTeamNames[1]);

				matchInfo.setMatchId(match_api_id);

				allMatches.add(matchInfo);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("All matches size: " + allMatches.size());
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

	// Returns <teamId, [previousstandings 1 to howManyPrevSeasons]>
	@Override
	public HashMap<String, int[]> getPreviousStandingsAllOpponents(String teamId, String matchId,
			int howManyPrevSeasons) {
		HashMap<String, int[]> opponentsPreviousStandings = new HashMap<>();
		ArrayList<String> opponents = getFutureOpponents(teamId, matchId);
		//TODO: change current season later on
		String curSeason = "2012/2013";
		String[] currentSeasonHalves = curSeason.split("/");
		int currentSeasonFirstHalf = Integer.valueOf(currentSeasonHalves[0]);
		int currentSeasonSecondHalf = Integer.valueOf(currentSeasonHalves[1]);
		if(opponents != null) {
			for(int i = 1; i <= howManyPrevSeasons; i++) {
				StringBuilder previousSeasonStringBuilder = new StringBuilder();
				previousSeasonStringBuilder.append(currentSeasonFirstHalf - i);
				previousSeasonStringBuilder.append("/");
				previousSeasonStringBuilder.append(currentSeasonSecondHalf - i);
				HashMap<String, int[]> previousSeasonStandings = 
						getStandingsOfSeason(previousSeasonStringBuilder.toString());
				for(String opponent : opponents) {
					int[] records = previousSeasonStandings.get(opponent);
					if(records != null) {
						int standing = records[0];

						if(opponentsPreviousStandings.containsKey(opponent)) {
							int[] previousSeasonsStandings = opponentsPreviousStandings.get(opponent);
							previousSeasonsStandings[i - 1] = standing;
						} else {
							int[] previousSeasonStanding = new int[howManyPrevSeasons];
							previousSeasonStanding[0] = standing;
							opponentsPreviousStandings.put(opponent, previousSeasonStanding);
						}
					} else {
						//If the team was not present in a previous season, 20th position is given to it
						if(opponentsPreviousStandings.containsKey(opponent)) {
							int[] previousSeasonsStandings = opponentsPreviousStandings.get(opponent);
							previousSeasonsStandings[i - 1] = 20;
						} else {
							int[] previousSeasonStanding = new int[howManyPrevSeasons];
							previousSeasonStanding[0] = 20;
							opponentsPreviousStandings.put(opponent, previousSeasonStanding);
						}
					}
				}
			}
		}
		/*for (Map.Entry<String, int[]> entry : opponentsPreviousStandings.entrySet()) {
			System.out.println("-------------------------------");
			String key = entry.getKey();
			System.out.println("Key: " + key);
			int[] values = entry.getValue();
			System.out.println("Values");
			for (int i = 0; i < howManyPrevSeasons; i++) {
				System.out.println(i + ": " + values[i]);
			}
		}*/
		return opponentsPreviousStandings;
	}

	@Override
	/* Returns a HashMap of the form <TeamId, [Standings, Points, Wins, Draws, Losses, GF, GA, GD]> */
	public HashMap<String, int[]> getStandingsOfSeason(String season) {
		Connection connection = getConnection();
		HashMap<String, int[]> standings = new HashMap<>();
		int goalsScored, goalsAgainst, goalDifference;

		String getStandingsQuery = "Select home_team_goal, away_team_goal, home_team_api_id, away_team_api_id "
				+ "From Match Where season = ? And league_id = ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getStandingsQuery);
			preparedStatement.setString(1, season);
			preparedStatement.setString(2, EPL_LEAGUE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String homeTeamGoal = resultSet.getString("home_team_goal");
				String awayTeamGoal = resultSet.getString("away_team_goal");
				String homeTeamId = resultSet.getString("home_team_api_id");
				String awayTeamId = resultSet.getString("away_team_api_id");

				if(!(standings.containsKey(homeTeamId)))
					standings.put(homeTeamId, new int[8]);
				if(!(standings.containsKey(awayTeamId)))
					standings.put(awayTeamId, new int[8]);

				int[] homeTeamResult = standings.get(homeTeamId);
				int[] awayTeamResult = standings.get(awayTeamId);

				if(Integer.parseInt(homeTeamGoal) > Integer.parseInt(awayTeamGoal)) {
					homeTeamResult[2] += 1;			// win for home team
					homeTeamResult[1] += 3;			// 3 points for home team

					awayTeamResult[4] += 1;			// loss for away team
				}
				else if(Integer.parseInt(homeTeamGoal) == Integer.parseInt(awayTeamGoal)) {
					homeTeamResult[3] += 1;			// draw for home team
					homeTeamResult[1] += 1;			// 1 point for home team

					awayTeamResult[3] += 1;			// draw for away team
					awayTeamResult[1] += 1;			// 1 point for away team
				}
				else {
					homeTeamResult[4] += 1;			// loss for home team

					awayTeamResult[2] += 1;			// win for away team
					awayTeamResult[1] += 3;			// 3 points for away win
				}
				homeTeamResult[5] += Integer.parseInt(homeTeamGoal);	// GF
				homeTeamResult[6] += Integer.parseInt(awayTeamGoal);	// GA
				homeTeamResult[7] += (Integer.parseInt(homeTeamGoal) - Integer.parseInt(awayTeamGoal));

				awayTeamResult[5] += Integer.parseInt(awayTeamGoal);	// GF
				awayTeamResult[6] += Integer.parseInt(homeTeamGoal);	// GA
				awayTeamResult[7] += Integer.parseInt(awayTeamGoal) - Integer.parseInt(homeTeamGoal);

				standings.put(homeTeamId, homeTeamResult);
				standings.put(awayTeamId, awayTeamResult);
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<Map.Entry<String, int[]>> rank =
				new LinkedList<Map.Entry<String, int[]>>(standings.entrySet() );

		Collections.sort( rank, new Comparator<Map.Entry<String, int[]>>()
		{
			@Override
			public int compare(Entry<String, int[]> arg0, Entry<String, int[]> arg1) {
				if(Integer.compare(arg0.getValue()[1], arg1.getValue()[1]) != 0)
					return Integer.compare(arg0.getValue()[1], arg1.getValue()[1]);
				// If two teams have same number of points
				else {
					if(Integer.compare(arg0.getValue()[7], arg1.getValue()[7]) != 0)
						return Integer.compare(arg0.getValue()[7], arg1.getValue()[7]);
					//teams have same goal difference
					else {
						if(Integer.compare(arg0.getValue()[5], arg1.getValue()[5]) != 0)
							return Integer.compare(arg0.getValue()[5], arg1.getValue()[5]);
						//if same number of goals scored, return alphabetically
						else
							return Integer.compare(Integer.parseInt(arg0.getKey()), Integer.parseInt(arg1.getKey()));
					}
				}
			}
		} );

		Map<String, int[]> result = new LinkedHashMap<String, int[]>();
		int i = 20;
		for (Map.Entry<String, int[]> entry : rank)
		{
			entry.getValue()[0] = i--;
			result.put( entry.getKey(), entry.getValue() );
		}

		/* for (Map.Entry<String, int[]> entry : rank)
		{
			System.out.println(entry.getKey() + " : " + entry.getValue()[0] + " : " + entry.getValue()[7] );
		} */

		return (HashMap<String, int[]>) result;
	}

	@Override
	public int getTotalPointsHistory(String teamId, String whichPrevSeason) {
		Connection connection = getConnection();
		int pointsHistory = 0;

		String getTotalHomePointsQuery = "Select home_team_goal, away_team_goal, home_team_api_id, away_team_api_id "
				+ "From Match Where season = ? And (home_team_api_id = ? or away_team_api_id = ?);";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getTotalHomePointsQuery);
			preparedStatement.setString(1, whichPrevSeason);
			preparedStatement.setString(2, teamId);
			preparedStatement.setString(3, teamId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String homeTeamGoal = resultSet.getString("home_team_goal");
				String awayTeamGoal = resultSet.getString("away_team_goal");
				String homeTeamId = resultSet.getString("home_team_api_id");
				String awayTeamId = resultSet.getString("away_team_api_id");

				if(homeTeamId.equalsIgnoreCase(teamId)) {
					if(Integer.parseInt(homeTeamGoal) > Integer.parseInt(awayTeamGoal))
						pointsHistory += 3;
					else if(Integer.parseInt(homeTeamGoal) == Integer.parseInt(awayTeamGoal))
						pointsHistory += 1;
				}

				else if(awayTeamId.equalsIgnoreCase(teamId)) {
					if(Integer.parseInt(homeTeamGoal) < Integer.parseInt(awayTeamGoal))
						pointsHistory += 3;
					else if(Integer.parseInt(homeTeamGoal) == Integer.parseInt(awayTeamGoal))
						pointsHistory += 1;
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pointsHistory;
	}

	@Override

	public void getListOfPlayersPlayed(String teamId, String matchId, int inHowManyGames) {
		Connection connection = getConnection();
		HashMap<String, ArrayList<PlayerInfo>> playersPlayedMap = new HashMap<>();
		String getPlayersInLastFewMatchesQuery = "Select match_api_id, date, home_team_api_id, away_team_api_id, "
				+ "home_player_1, home_player_2, home_player_3, home_player_4, home_player_5, home_player_6, home_player_7, "
				+ "home_player_8, home_player_9, home_player_10, home_player_11, away_player_1, away_player_2, away_player_3, "
				+ "away_player_4, away_player_5, away_player_6, away_player_7, away_player_8, away_player_9, away_player_10, "
				+ "away_player_11 From Match Where date < (Select date from Match Where match_api_id = ?)"
				+ "And (home_team_api_id = ? Or away_team_api_id = ?) "
				+ "Order By date DESC Limit ?;";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(getPlayersInLastFewMatchesQuery);
			preparedStatement.setString(1, matchId);
			preparedStatement.setString(2, teamId);
			preparedStatement.setString(3, teamId);
			preparedStatement.setString(4, String.valueOf(inHowManyGames));
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String match_id = resultSet.getString("match_api_id");
				String home_team_api_id = resultSet.getString("home_team_api_id");
				String away_team_api_id = resultSet.getString("away_team_api_id");

				ArrayList<PlayerInfo> playersPlayedList = new ArrayList<>();

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

						playersPlayedList.add(playerInfo);
					}
					playersPlayedMap.put(match_id, playersPlayedList);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Map size: " + playersPlayedMap.size());
		for (Map.Entry<String, ArrayList<PlayerInfo>> entry : playersPlayedMap.entrySet()) {
			System.out.println("-------------------------------");
			String key = entry.getKey();
			System.out.println("Key: " + key);
			ArrayList<PlayerInfo> values = entry.getValue();
			System.out.println("Values");
			for (PlayerInfo value : values) {
				System.out.println("Player: " + value.getPlayerName());
			}
		}
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

	private ArrayList<String> getFutureOpponents(String teamId, String matchId) {
		Connection connection = getConnection();
		ArrayList<String> opponents = new ArrayList<>();
		//First find future opponents of this team
		String futureOpponentsQuery = "With all_opponents as "
				+ "(Select home_team_api_id, away_team_api_id From Match "
				+ "Where (home_team_api_id = ? Or away_team_api_id = ?) "
				+ "And date > (Select date From Match Where match_api_id = ?) "
				+ "And season = (Select season From Match Where match_api_id = ?)) "
				+ "Select home_team_api_id as opposition From all_opponents Where away_team_api_id = ? Union "
				+ "Select away_team_api_id From all_opponents Where home_team_api_id = ?;";

		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement(futureOpponentsQuery);
			preparedStatement.setString(1, teamId);
			preparedStatement.setString(2, teamId);
			preparedStatement.setString(3, matchId);
			preparedStatement.setString(4, matchId);
			preparedStatement.setString(5, teamId);
			preparedStatement.setString(6, teamId);
			ResultSet resultSetAway = preparedStatement.executeQuery();
			while(resultSetAway.next()) {
				String opposition = resultSetAway.getString("opposition");
				if(opposition != null) {
					if(!opponents.contains(opposition))
						opponents.add(opposition);
				}
			}
			preparedStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//		System.out.println("Opponents size: " + opponents.size());
		return opponents;
	}
}
