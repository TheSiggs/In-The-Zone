package inthezone.battle.status;

import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import java.util.ArrayList;
import java.util.List;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

public class PointStatusEffect extends StatusEffect {
	private final int ap;
	private final int mp;

	@JSON
	public PointStatusEffect(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn,
		@Field("ap") final int ap,
		@Field("mp") final int mp
	) {
		super(info, startTurn);

		this.ap = ap;
		this.mp = mp;
	}

	@Override public Stats getBaseStatsBuff() {
		return new Stats(ap, mp, 0, 0, 0, 0);
	}

	@Override public List<Command> doNow(final Character c) {
		c.pointsBuff(ap, mp, 0);
		return new ArrayList<>();
	}
}

