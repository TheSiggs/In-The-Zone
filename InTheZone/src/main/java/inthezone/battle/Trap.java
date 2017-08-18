package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;

public class Trap extends Targetable implements HasParentAgent {
	public final MapPoint pos;
	public final Ability ability;
	public final Character parent;

	private final boolean hasMana;
	private final double chanceBuff;
	private final double attackBuff;
	private final double defenceBuff;
	private final Stats stats;

	private final SpriteInfo sprite;
	private boolean defused = false;

	@Override public Character getParent() {return parent;}

	public Trap(
		final MapPoint pos,
		final boolean hasMana,
		final Ability ability,
		final Character agent,
		final StandardSprites sprites
	) {
		this.pos = pos;
		this.hasMana = hasMana;
		this.ability = ability;
		this.parent = agent;
		this.sprite = ability.info.media.zoneTrapSprite.orElse(sprites.trap);

		this.chanceBuff = agent.getChanceBuff();
		this.attackBuff = agent.getAttackBuff();
		this.defenceBuff = agent.getDefenceBuff();
		this.stats = agent.getStats();
	}

	public Trap(
		final MapPoint pos,
		final Ability ability,
		final Character parent,
		final boolean hasMana,
		final double chanceBuff,
		final double attackBuff,
		final double defenceBuff,
		final Stats stats,
		final SpriteInfo sprite,
		final boolean defused
	) {
		this.pos = pos;
		this.ability = ability;
		this.parent = parent;
		this.hasMana = hasMana;
		this.chanceBuff = chanceBuff;
		this.attackBuff = attackBuff;
		this.defenceBuff = defenceBuff;
		this.stats = stats;
		this.sprite = sprite;
		this.defused = defused;
	}

	@Override public boolean blocksSpace() {return false;}
	@Override public boolean blocksPath(Player player) {return false;}

	@Override public Stats getStats() {return stats;}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public void dealDamage(final int damage) {return;}
	@Override public void defuse() {defused = true;}
	@Override public void cleanse() {return;}
	@Override public void purge() {return;}
	@Override public void revive() {return;}
	@Override public void applyStatus(
		final Battle battle, final StatusEffect status) {}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(final Character character) {return true;}
	@Override public boolean isDead() {return defused;}
	@Override public SpriteInfo getSprite() {return sprite;}
	@Override public boolean reap() {return defused;}

	@Override public boolean hasMana() {return hasMana;}
	@Override public double getChanceBuff() {return chanceBuff;}

	@Override public TrapFrozen freeze() {
		return new TrapFrozen(this);
	}
}

