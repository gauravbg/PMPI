package com.project.ai.dataclasses;

public class PlayerAttributes {
	
	String position;
	int overallRating;
	int goals;
	int assists;
	int shotsOnTarget;
	int shotsOffTarget;
	
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public int getOverallRating() {
		return overallRating;
	}
	public void setOverallRating(int overallRating) {
		this.overallRating = overallRating;
	}
	public int getGoals() {
		return goals;
	}
	public void setGoals(int goals) {
		this.goals = goals;
	}
	public int getAssists() {
		return assists;
	}
	public void setAssists(int assists) {
		this.assists = assists;
	}
	public int getShotsOnTarget() {
		return shotsOnTarget;
	}
	public void setShotsOnTarget(int shotsOnTarget) {
		this.shotsOnTarget = shotsOnTarget;
	}
	public int getShotsOffTarget() {
		return shotsOffTarget;
	}
	public void setShotsOffTarget(int shotsOffTarget) {
		this.shotsOffTarget = shotsOffTarget;
	}

}
