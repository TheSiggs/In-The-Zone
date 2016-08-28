package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.List;

public interface InstantEffect extends HasJSONRepresentation {
	public List<Character> apply(Battle battle);

	/**
	 * Determine if this instant effect is ready to run
	 * */
	public boolean isComplete();

	/**
	 * Attempt to complete this instant effect.
	 * @return true on success, false on fail.
	 * */
	public boolean complete(MapPoint p);
}

