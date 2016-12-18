package com.project.ai.interfaces;

import com.project.ai.dataclasses.PlayerAttributes;

public interface IPlayerInfluence {
	
	/** Returns an object of PlayerAttributes with
	 * @param matchId
	 * @param playerId
	 * @param howManyMatches
	 * @return <Position, Overall Rating, Goals, Assists, Shots On, Shots Off
	 */
	PlayerAttributes getPlayerInfluenceInLastMatches(String matchId, String playerId, int howManyMatches);

}