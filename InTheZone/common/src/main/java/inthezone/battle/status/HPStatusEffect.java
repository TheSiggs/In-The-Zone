package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.StatusEffectInfo;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * A status effect that does ongoing damage or healing before the players turn.
 * */
public class HPStatusEffect extends StatusEffect {
	private final int hp;

	@JSON
	public HPStatusEffect(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn,
		@Field("hp") final int hp
	) {
		super(info, startTurn);

		this.hp = hp;
	}

	@Override public List<Command> doBeforeTurn(
		final Battle battle, final Character c
	) {
		c.pointsBuff(0, 0, hp);
		return new ArrayList<>();
	}
}

