package inthezone.battle;

import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

public class Trap implements Targetable {
	public final MapPoint pos;
	public final Ability ability;

	private final boolean hasMana;
	private final double chanceBuff;
	private final double attackBuff;
	private final double defenceBuff;
	private final Stats stats;

	private final StandardSprites sprites;
	private boolean defused = false;

	public Trap(
		MapPoint pos,
		boolean hasMana,
		Ability ability,
		Character agent,
		StandardSprites sprites
	) {
		this.pos = pos;
		this.hasMana = hasMana;
		this.ability = ability;
		this.sprites = sprites;

		this.chanceBuff = agent.getChanceBuff();
		this.attackBuff = agent.getAttackBuff();
		this.defenceBuff = agent.getDefenceBuff();
		this.stats = agent.getStats();
	}

	@Override public boolean blocksSpace(Player player) {return false;}
	@Override public boolean blocksPath(Player player) {return false;}

	@Override public Stats getStats() {return stats;}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return attackBuff;}
	@Override public double getDefenceBuff() {return defenceBuff;}
	@Override public void dealDamage(int damage) {return;}
	@Override public void defuse() {defused = true;}
	@Override public void cleanse() {return;}
	@Override public void purge() {return;}
	@Override public void applyStatus(StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(Character character) {return true;}
	@Override public boolean isDead() {return defused;}
	@Override public SpriteInfo getSprite() {return sprites.trap;}
	@Override public boolean reap() {return defused;}

	@Override public boolean hasMana() {return hasMana;}
	@Override public double getChanceBuff() {return chanceBuff;}
}

