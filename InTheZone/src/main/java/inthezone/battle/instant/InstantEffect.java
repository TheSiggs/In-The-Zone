package inthezone.battle.instant;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.MapPoint;
import java.util.List;

public interface InstantEffect extends HasJSONRepresentation {
	public List<Character> apply(Battle battle);
}

