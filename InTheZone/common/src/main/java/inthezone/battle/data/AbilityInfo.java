package inthezone.battle.data;

import java.util.Optional;
import ssjsjs.JSONable;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;

public class AbilityInfo implements JSONable {
	public final boolean banned;
	public final String name;
	public final AbilityMedia media;
	public final AbilityType type;
	public final boolean trap;
	public final AbilityZoneType zone;
	public final int ap;
	public final int mp;
	public final int pp;
	public final double eff;
	public final double chance;
	public final boolean heal;
	public final Range range;
	public final Optional<AbilityInfo> mana;
	public final Optional<AbilityInfo> subsequent;
	public final int recursion;
	public final Optional<InstantEffectInfo> instantBefore;
	public final Optional<InstantEffectInfo> instantAfter;
	public final Optional<StatusEffectInfo> statusEffect;

	private boolean isMana;
	private boolean isSubsequent;

	public boolean isMana() { return isMana; }
	public boolean isSubsequent() { return isSubsequent; }

	@Override public String toString() {
		return name;
	}

	@JSONConstructor
	public AbilityInfo(
		@Field("banned") boolean banned,
		@Field("name") String name,
		@Field("type") AbilityType type,
		@Field("media") AbilityMedia media,
		@Field("trap") boolean trap,
		@Field("zone") AbilityZoneType zone,
		@Field("ap") int ap,
		@Field("mp") int mp,
		@Field("pp") int pp,
		@Field("eff") double eff,
		@Field("chance") double chance,
		@Field("heal") boolean heal,
		@Field("range") Range range,
		@Field("mana") Optional<AbilityInfo> mana,
		@Field("subsequent") Optional<AbilityInfo> subsequent,
		@Field("recursion") int recursion,
		@Field("instanceBefore") Optional<InstantEffectInfo> instantBefore,
		@Field("instantAfter") Optional<InstantEffectInfo> instantAfter,
		@Field("statusEffect") Optional<StatusEffectInfo> statusEffect
	) {
		this.banned = banned;
		this.name = name;
		this.media = media;
		this.type = type;
		this.trap = trap;
		this.zone = zone;
		this.ap = ap;
		this.mp = mp;
		this.pp = pp;
		this.eff = eff;
		this.chance = chance;
		this.heal = heal;
		this.range = range;
		this.mana = mana;
		this.subsequent = subsequent;
		this.recursion = recursion;
		this.instantBefore = instantBefore;
		this.instantAfter = instantAfter;
		this.statusEffect = statusEffect;
	}


	/**
	 * Invoke after deserialization to set the isMana and isSubsequent properties.
	 * */
	void fixAttributes() {
		fixAttributes(false, false);
	}

	private void fixAttributes(final boolean isMana, final boolean isSubsequent) {
		this.isMana = isMana;
		this.isSubsequent = isSubsequent;
		mana.ifPresent(a -> a.fixAttributes(true, false));
		subsequent.ifPresent(a -> a.fixAttributes(isMana, true));
	}

	/**
	 * Does this ability inflict some kind of damage on targets.
	 * */
	public boolean isDangerous() {
		return
			dealsDamage() ||
			statusEffect.map(e -> e.kind == StatusEffectKind.DEBUFF).orElse(false);
	}

	/**
	 * Does this ability affect traps.
	 * */
	public boolean affectsTraps() {
		return
			instantBefore.map(i -> i.type == InstantEffectType.DEFUSE).orElse(false) ||
			instantAfter.map(i -> i.type == InstantEffectType.DEFUSE).orElse(false);
	}

	/**
	 * Does this ability affect zones.
	 * */
	public boolean affectsZones() {
		return
			instantBefore.map(i -> i.type == InstantEffectType.PURGE).orElse(false) ||
			instantAfter.map(i -> i.type == InstantEffectType.PURGE).orElse(false);
	}

	/**
	 * Does this ability deal damage.
	 * */
	public boolean dealsDamage() {
		return eff > 0d && !heal;
	}
}

