package inthezone.battle;

import isogame.engine.MapPoint;
import isogame.engine.SpriteInfo;

import java.util.Optional;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.Stats;
import inthezone.battle.status.StatusEffect;

public class RoadBlock extends Targetable {
	private final static int HITS_TO_DESTROY = 2;
	private final MapPoint pos;
	private int hits;
	private final SpriteInfo sprite;

	private Optional<Zone> boundZone;

	public RoadBlock(
		final MapPoint pos,
		final Optional<AbilityInfo> a,
		final StandardSprites sprites
	) {
		this.pos = pos;
		this.boundZone = Optional.empty();
		hits = HITS_TO_DESTROY;
		this.sprite = a.flatMap(aa -> aa.media.obstacleSprite).orElse(sprites.roadBlock);
	}

	private RoadBlock(
		final MapPoint pos,
		final Optional<Zone> boundZone,
		final int hits,
		final SpriteInfo sprite
	) {
		this.pos = pos;
		this.boundZone = boundZone;
		this.hits = hits;
		this.sprite = sprite;
	}

	public void bindZone(final Zone zone) {
		this.boundZone = Optional.of(zone);
	}

	@Override public boolean blocksSpace() {return true;}
	@Override public boolean blocksPath(Player player) {return true;}

	@Override public Stats getStats() {return new Stats();}
	@Override public MapPoint getPos() {return pos;}
	@Override public double getAttackBuff() {return 0;}
	@Override public double getDefenceBuff() {return 0;}
	@Override public void dealDamage(final int damage) {
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
	@Override public void applyStatus(
		final Battle battle, final StatusEffect status) {return;}
	@Override public boolean isPushable() {return false;}
	@Override public boolean isEnemyOf(final Character character) {return true;}
	@Override public boolean isDead() {return hits == 0;}
	@Override public boolean reap() {return isDead();}

	@Override public boolean hasMana() {return false;}
	@Override public double getChanceBuff() {return 0;}

	@Override public SpriteInfo getSprite() {return sprite;}

	@Override public RoadBlockFrozen freeze() {
		return new RoadBlockFrozen(this);
	}

	@Override public boolean isAffectedBy(final InstantEffectInfo instant) {
		return false;
	}
}

