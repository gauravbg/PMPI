package com.project.ai.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import com.project.ai.dataclasses.PlayerAttributesInfo;
import com.project.ai.dataclasses.PlayerInfo;

public interface IPlayerInfluence {
	
	/** Returns an object of PlayerAttributes with
	 * @param matchId
	 * @param 
	 * @param howManyMatches
	 * @return <Position, Overall Rating, Goals, Assists, Shots On, Shots Off
	 */
	HashMap<String, PlayerAttributesInfo> getPlayerInfluenceInLastMatches(String matchId, ArrayList<PlayerInfo> players, int howManyMatches);

}
