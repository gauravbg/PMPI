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
	
	public static int getOpponentDifficultyRank(double score) {
		
		if(score>18) {
			return 1;
		} else if(score>16) {
			return 2;
		} else if (score>14) {
			return 3;
		} else if (score>11) {
			return 4;
		} else if(score>8) {
			return 5;
		} else if(score>5) {
			return 6;
		} else {
			return 7;
		}
	}
	
	public static double getOpponentDifficultyVariance(int rank, int remMatches) {
		double adjustment = 1;
		if(rank==1) {
			return 0.9 * adjustment;
		} else if(rank==2) {
			return 0.933 * adjustment;
		} else if (rank==3) {
			return 0.966 * adjustment;
		} else if (rank==4) {
			return 1* adjustment;
		} else if(rank==5) {
			return 1.0333 * adjustment;
		} else if(rank==6) {
			return 1.0666 * adjustment;
		} else {
			return 1.1 * adjustment;
		}
	}

}
