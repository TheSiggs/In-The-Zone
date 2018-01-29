package inthezone.battle.data;

import ssjsjs.JSONable;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

/**
 * Basic character stats
 * */
public class Stats implements JSONable {
	public final int ap;
	public final int mp;
	public final int power;
	public final int hp;
	public final int attack;
	public final int defence;

	public Stats() {
		this.ap = 0;
		this.mp = 0;
		this.power = 0;
		this.hp = 0;
		this.attack = 0;
		this.defence = 0;
	}

	@JSON
	public Stats(
		@Field("ap") final int ap,
		@Field("mp") final int mp,
		@Field("power") final int power,
		@Field("hp") final int hp,
		@Field("attack") final int attack,
		@Field("defence") final int defence
	) {
		this.ap = ap;
		this.mp = mp;
		this.power = power;
		this.hp = hp;
		this.attack = attack;
		this.defence = defence;
	}

	public Stats add(final Stats stats) {
		return new Stats(
			ap + stats.ap,
			mp + stats.mp,
			power + stats.power,
			hp + stats.hp,
			attack + stats.attack,
			defence + stats.defence);
	}
}

