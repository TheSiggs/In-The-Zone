package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import java.util.ArrayList;
import java.util.List;

public class PointStatusEffect extends StatusEffect {
	private final int ap;
	private final int mp;
	private final int hp;

	public PointStatusEffect(int ap, int mp, int hp) {
		super();

		this.ap = ap;
		this.mp = mp;
		this.hp = hp;
	}

	@Override public List<Command> doBeforeTurn(Battle battle, Character c) {
		c.pointsBuff(ap, mp, hp);
		return new ArrayList<>();
	}
}

