package com.project.ai.interfaces;

import java.util.HashMap;

public interface ITeamStrength {

	// residual matches difficulty
	// Returns <teamId, [previousstandings 1 to howManyPrevSeasons]>
	HashMap<String, int[]> getPreviousStandingsAllOpponents(String teamId, String matchId, int howManyPrevSeasons, String curSeason);

	// Get the <standings, points, wins, draws, losses, GF, GA, GD> for a team for a
	// particular season
	HashMap<String, int[]> getStandingsOfSeason(String season);

	// Get the <standings, points, wins, draws, losses, GF, GA, GD> for a team for a
	// particular season, particular game week
	HashMap<String, int[]> getStandingsOfSeasonForGameWeek(String season, String gameWeek);

	// Returns final total points in previous seasons by that team
	int getTotalPointsHistory(String teamId, String whichPrevSeason);

}
