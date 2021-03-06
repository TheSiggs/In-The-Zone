package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class AbilityInfo implements HasJSONRepresentation {
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

	public final boolean isMana;
	public final boolean isSubsequent;

	@Override
	public String toString() {
		return name;
	}

	public AbilityInfo(
		boolean banned,
		String name,
		AbilityType type,
		AbilityMedia media,
		boolean trap,
		AbilityZoneType zone,
		int ap,
		int mp,
		int pp,
		double eff,
		double chance,
		boolean heal,
		Range range,
		Optional<AbilityInfo> mana,
		Optional<AbilityInfo> subsequent,
		int recursion,
		Optional<InstantEffectInfo> instantBefore,
		Optional<InstantEffectInfo> instantAfter,
		Optional<StatusEffectInfo> statusEffect,
		boolean isMana, boolean isSubsequent
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
		this.isMana = isMana;
		this.isSubsequent = isSubsequent;
	}

	@Override public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("banned", banned);
		r.put("name", name);
		r.put("media", media.getJSON());
		r.put("type", type.toString());
		r.put("trap", trap);
		r.put("zone", zone.toString());
		r.put("ap", ap);
		r.put("mp", mp);
		r.put("pp", pp);
		r.put("eff", eff);
		r.put("chance", chance);
		r.put("heal", heal);
		r.put("range", range.getJSON());
		mana.ifPresent(m -> r.put("mana", m.getJSON()));
		subsequent.ifPresent(s -> r.put("subsequent", s.getJSON()));
		r.put("recursion", recursion);
		instantBefore.ifPresent(e -> r.put("instantBefore", e.toString()));
		instantAfter.ifPresent(e -> r.put("instantAfter", e.toString()));
		statusEffect.ifPresent(e -> r.put("statusEffect", e.toString()));
		return r;
	}

	public boolean isDangerous() {
		return
			dealsDamage() ||
			statusEffect.map(e -> e.kind == StatusEffectKind.DEBUFF).orElse(false);
	}

	public boolean affectsTraps() {
		return
			instantBefore.map(i -> i.type == InstantEffectType.DEFUSE).orElse(false) ||
			instantAfter.map(i -> i.type == InstantEffectType.DEFUSE).orElse(false);
	}

	public boolean affectsZones() {
		return
			instantBefore.map(i -> i.type == InstantEffectType.PURGE).orElse(false) ||
			instantAfter.map(i -> i.type == InstantEffectType.PURGE).orElse(false);
	}

	public boolean dealsDamage() {
		return eff > 0d && !heal;
	}

	public static AbilityInfo fromJSON(
		JSONObject json, ResourceLocator loc, Library lib
	) throws CorruptDataException {
		return fromJSON(json, loc, lib, false, false);
	}

	public static AbilityInfo fromJSON(
		JSONObject json, ResourceLocator loc, Library lib,
		boolean isMana, boolean isSubsequent
	) throws CorruptDataException {
		try {
			final boolean banned = json.optBoolean("banned", false);
			final String name = json.getString("name");
			final AbilityType type = AbilityType.parse(json.getString("type"));
			final AbilityMedia media = AbilityMedia.fromJSON(json.getJSONObject("media"), loc, lib);
			final boolean trap = json.optBoolean("trap", false);
			final AbilityZoneType zone = AbilityZoneType.fromString(json.getString("zone"));
			final int ap = json.getInt("ap");
			final int mp = json.getInt("mp");
			final int pp = json.getInt("pp");
			final double eff = json.getDouble("eff");
			final double chance = json.getDouble("chance");
			final boolean heal = json.getBoolean("heal");
			final Range range = Range.fromJSON(json.getJSONObject("range"));
			final int recursion = json.getInt("recursion");

			final JSONObject rmana = json.optJSONObject("mana");
			final JSONObject rsubsequent = json.optJSONObject("subsequent");
			final String rinstantBefore = json.optString("instantBefore", null);
			final String rinstantAfter = json.optString("instantAfter", null);
			final String rstatusEffect = json.optString("statusEffect", null);

			final Optional<AbilityInfo> mana;
			final Optional<AbilityInfo> subsequent;
			final Optional<InstantEffectInfo> instantBefore;
			final Optional<InstantEffectInfo> instantAfter;
			final Optional<StatusEffectInfo> statusEffect;

			if (rmana == null) mana = Optional.empty(); else {
				mana = Optional.of(AbilityInfo.fromJSON(rmana, loc, lib, true, false));
			}
			if (rsubsequent == null) subsequent = Optional.empty(); else {
				subsequent = Optional.of(AbilityInfo.fromJSON(
					rsubsequent, loc, lib, isMana, true));
			}
			if (rinstantBefore == null) instantBefore = Optional.empty(); else {
				instantBefore = Optional.of(new InstantEffectInfo(rinstantBefore));
			}
			if (rinstantAfter == null) instantAfter = Optional.empty(); else {
				instantAfter = Optional.of(new InstantEffectInfo(rinstantAfter));
			}
			if (rstatusEffect == null) statusEffect = Optional.empty(); else {
				statusEffect = Optional.of(new StatusEffectInfo(rstatusEffect));
			}

			return new AbilityInfo(
				banned, name, type, media, trap, zone,
				ap, mp, pp, eff, chance, heal, range, mana,
				subsequent, recursion,
				instantBefore, instantAfter, statusEffect,
				isMana, isSubsequent);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing ability info, " + e.getMessage(), e);
		}
	}
}

