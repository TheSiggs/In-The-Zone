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
	public final Character agent;
	private final StandardSprites sprites;
	private boolean defused = false;

	public Trap(
		MapPoint pos, Ability ability, Character agent, StandardSprites sprites
	) {
		this.pos = pos;
		this.ability = ability;
		this.agent = agent;
		this.sprites = sprites;
	}

	@Override public boolean blocksSpace(Player player) {return false;}
	@Override public boolean blocksPath(Player player) {return false;}

	@Override public Stats getStats() {return new Stats();}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return 0;}
	@Override public double getDefenceBuff() {return 0;}
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
}

