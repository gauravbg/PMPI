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
		} else if (score>13) {
			return 3;
		} else if (score>10) {
			return 4;
		} else if(score>9) {
			return 5;
		} else if(score>8) {
			return 6;
		} else {
			return 7;
		}
	}
	
	public static double getOpponentDifficultyVariance(int rank, int remMatches) {
		double adjustment = 0;
		
		if(rank==1) {
			return 0.9 + adjustment;
		} else if(rank==2) {
			return 0.933 + adjustment;
		} else if (rank==3) {
			return 0.966 + adjustment;
		} else if (rank==4) {
			return 1+ adjustment;
		} else if(rank==5) {
			return 1.0333 + adjustment;
		} else if(rank==6) {
			return 1.0666 + adjustment;
		} else {
			return 1.1 + adjustment;
		}
	}
	
	
	public static double[] getMeanSD(double form, int availability) {
		double[] a = new double[2];
		if(availability ==1) {
			a[0] = form;
			a[1] = 0.0001;
		} else if(availability ==2 ) {
			a[0] = form * 0.8;
			a[1] = 0.001;
		} else if(availability ==3 ) {
			a[0] = form * 0.6;
			a[1] = 0.005;
		} else if(availability == 4 ) {
			a[0] = form * 0.4;
			a[1] = 0.01;
		} else {
			a[0] = form *0.2;
			a[1] = 0.05;
		}
		
		return a;
	}
	
	public static int getPlayerAvailability(int count) {
		if(count>10) {
			return 1;
		} else if(count>8) {
			return 2;
		} else if (count>5) {
			return 3;
		} else if(count>3) {
			return 4;
		} else {
			return 5;
		}
	}
	
	public static double getPositionLikelihood(String pos) {
		double likelihood;
		if(pos.equals("ATT")) {
			likelihood = 0.6;
		} else if(pos.equals("MID")) {
			likelihood = 0.25;
		} else if (pos.equals("DEF")) {
			likelihood = 0.15;
		} else {
			likelihood = 0;
		}
		
		return likelihood;
	}
	
	public static double scaleGoal(int goals) {
		double likelihood;
		if(goals > 5) {
			likelihood = 0.95;
		} else if(goals > 4) {
			likelihood = 0.8;
		} else if (goals > 3) {
			likelihood = 0.6;
		} else if (goals > 2){
			likelihood = 0.4;
		} else {
			likelihood = 0.3;
		}
		
		return likelihood;
	}
	
	public static double scaleAssists(int ass) {
		double likelihood;
		if(ass > 5) {
			likelihood = 0.95;
		} else if(ass > 4) {
			likelihood = 0.8;
		} else if (ass > 3) {
			likelihood = 0.6;
		} else if (ass > 2){
			likelihood = 0.4;
		} else {
			likelihood = 0.3;
		}
		
		return likelihood;
	}
	
	public static double scaleShotsOnTarget(int count) {
		double likelihood;
		if(count > 10) {
			likelihood = 0.95;
		} else if(count > 8) {
			likelihood = 0.8;
		} else if (count > 6) {
			likelihood = 0.6;
		} else if (count > 4){
			likelihood = 0.4;
		} else {
			likelihood = 0.3;
		}
		
		return likelihood;
	}
	
	public static double scaleShotsOffTarget(int count) {
		double likelihood;
		if(count > 10) {
			likelihood = 0.95;
		} else if(count > 8) {
			likelihood = 0.8;
		} else if (count > 6) {
			likelihood = 0.6;
		} else if (count > 4){
			likelihood = 0.4;
		} else {
			likelihood = 0.3;
		}
		
		return likelihood;
	}
	
	public static double[] getMeanSDAfterPlayerReturn(double meanSD, int noOfPlayerReturning) {
		double[] a = new double[2];
		
		if(noOfPlayerReturning ==0) {
			a[0] = meanSD;
			a[1] = 0.01;
		} else if(noOfPlayerReturning ==1 ) {
			a[0] = meanSD + ((1-meanSD) * 0.1);
			a[1] = 0.01;
		} else if(noOfPlayerReturning ==2 ) {
			a[0] = meanSD + ((1-meanSD) * 0.2);
			a[1] = 0.01;
		} else {
			a[0] = meanSD + ((1-meanSD) * 0.3);
			a[1] = 0.01;
		} 
		
		return a;
	}

}
