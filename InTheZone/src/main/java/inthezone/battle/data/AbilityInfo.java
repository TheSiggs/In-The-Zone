package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import java.util.Optional;
import org.json.JSONObject;
import org.json.JSONException;

public class AbilityInfo implements HasJSONRepresentation {
	public final boolean banned;
	public final String name;
	public final AbilityType type;
	public final boolean trap;
	public final AbilityZoneType zone;
	public final Optional<SpriteInfo> zoneTrapSprite;
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

	@Override
	public String toString() {
		return name;
	}

	public AbilityInfo(
		boolean banned,
		String name,
		AbilityType type,
		boolean trap,
		AbilityZoneType zone,
		Optional<SpriteInfo> zoneTrapSprite,
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
		Optional<StatusEffectInfo> statusEffect
	) {
		this.banned = banned;
		this.name = name;
		this.type = type;
		this.trap = trap;
		this.zoneTrapSprite = zoneTrapSprite;
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

	@Override
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("banned", banned);
		r.put("name", name);
		r.put("type", type.toString());
		r.put("trap", trap);
		r.put("zone", zone.toString());
		zoneTrapSprite.ifPresent(e -> r.put("zoneTrapSprite", e.id));
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

	public static AbilityInfo fromJSON(JSONObject json, Library lib)
		throws CorruptDataException
	{
		try {
			final boolean banned = json.optBoolean("banned", false);
			final String name = json.getString("name");
			final AbilityType type = AbilityType.parse(json.getString("type"));
			final boolean trap = json.optBoolean("trap", false);
			final String rzoneTrapSprite = json.optString("zoneTrapSprite", null);
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
				mana = Optional.of(AbilityInfo.fromJSON(rmana, lib));
			}
			if (rsubsequent == null) subsequent = Optional.empty(); else {
				subsequent = Optional.of(AbilityInfo.fromJSON(rsubsequent, lib));
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

			final Optional<SpriteInfo> zoneTrapSprite;
			if (rzoneTrapSprite == null) {
				zoneTrapSprite = Optional.empty();
			} else {
				zoneTrapSprite = Optional.of(lib.getSprite((String) rzoneTrapSprite));
			}

			return new AbilityInfo(
				banned, name, type, trap, zone, zoneTrapSprite,
				ap, mp, pp, eff, chance, heal, range, mana,
				subsequent, recursion, instantBefore, instantAfter, statusEffect);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing ability info, " + e.getMessage(), e);
		}
	}
}

