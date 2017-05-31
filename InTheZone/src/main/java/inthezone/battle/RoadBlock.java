package inthezone.battle;

import inthezone.battle.commands.Command;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;
import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoadBlock extends Targetable {
	private final static int HITS_TO_DESTROY = 2;
	private final MapPoint pos;
	private int hits;
	private final SpriteInfo sprite;

	private Optional<Zone> boundZone;

	public RoadBlock(
		MapPoint pos, StandardSprites sprites
	) {
		this.pos = pos;
		this.boundZone = Optional.empty();
		hits = HITS_TO_DESTROY;
		this.sprite = sprites.roadBlock;
	}

	private RoadBlock(
		MapPoint pos, Optional<Zone> boundZone, int hits, SpriteInfo sprite
	) {
		this.pos = pos;
		this.boundZone = boundZone;
		this.hits = hits;
		this.sprite = sprite;
	}

	public void bindZone(Zone zone) {
		this.boundZone = Optional.of(zone);
	}

	@Override public boolean blocksSpace() {return true;}
	@Override public boolean blocksPath(Player player) {return true;}

	@Override public Stats getStats() {return new Stats();}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return 0;}
	@Override public double getDefenceBuff() {return 0;}
	@Override public void dealDamage(int damage) {
		if (damage <= 0) return;

		hits -= 1;
		if (hits < 0) hits = 0;
		if (isDead()) {
			boundZone.ifPresent(z -> {
				System.err.println("Purging zone " + z.toString());
				z.purge();
			});
		}
	}
	public boolean hasBeenHit() {return hits < HITS_TO_DESTROY;}
	@Override public void defuse() {return;}
	@Override public void cleanse() {return;}
	@Override public void purge() {return;}
	@Override public void revive() {return;}
	@Override public void applyStatus(Battle battle, StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(Character character) {return true;}
	@Override public boolean isDead() {return hits == 0;}
	@Override public boolean reap() {return isDead();}

	@Override public boolean hasMana() {return false;}
	@Override public double getChanceBuff() {return 0;}

	@Override public SpriteInfo getSprite() {return sprite;}

	@Override public RoadBlock clone() {return new RoadBlock(pos, boundZone, hits, sprite);}
}

