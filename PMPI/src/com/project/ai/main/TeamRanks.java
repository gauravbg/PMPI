package com.project.ai.main;

public class TeamRanks {

	public static int getRank(int points) {

		int limit = 25;
		int rank = 14;
		while(rank >= 1) {
			if(points < limit) {
				return rank;
			} else {
				limit = limit + 5;
				rank--;
			}
		}
		
		if (rank == 0)
			return 1;
		return rank;
	}

}
