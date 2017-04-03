package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import java.util.Optional;
import org.json.simple.JSONObject;

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
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
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
		final Object rbanned = json.get("banned");
		final Object rname = json.get("name");
		final Object rtype = json.get("type");
		final Object rtrap = json.get("trap");
		final Object rzoneTrapSprite = json.get("zoneTrapSprite");
		final Object rzoneTurns = json.get("zoneTurns"); // deprecated
		final Object rboundZone = json.get("boundZone"); // deprecated
		final Object rzone = json.get("zone");
		final Object rap = json.get("ap");
		final Object rmp = json.get("mp");
		final Object rpp = json.get("pp");
		final Object reff = json.get("eff");
		final Object rchance = json.get("chance");
		final Object rheal = json.get("heal");
		final Object rrange = json.get("range");
		final Object rmana = json.get("mana");
		final Object rsubsequent = json.get("subsequent");
		final Object rrecursion = json.get("recursion");
		final Object rinstantBefore = json.get("instantBefore");
		final Object rinstantAfter = json.get("instantAfter");
		final Object rstatusEffect = json.get("statusEffect");

		try {
			if (rname == null) throw new CorruptDataException("Missing name in ability");
			final String name = (String) rname;

			if (rtype == null) throw new CorruptDataException("Missing type in ability " + name);
			if (rap == null) throw new CorruptDataException("Missing ap in ability " + name);
			if (rmp == null) throw new CorruptDataException("Missing mp in ability " + name);
			if (rpp == null) throw new CorruptDataException("Missing pp in ability " + name);
			if (reff == null) throw new CorruptDataException("Missing eff in ability " + name);
			if (rchance == null) throw new CorruptDataException("Missing chance in ability " + name);
			if (rheal == null) throw new CorruptDataException("Missing heal in ability " + name);
			if (rrange == null) throw new CorruptDataException("Missing range in ability " + name);
			if (rrecursion == null) throw new CorruptDataException("Missing recursion in ability " + name);
			if (rzone == null) throw new CorruptDataException("Missing zone type in ability " + name);

			final boolean banned = rbanned == null? false : (Boolean) rbanned;
			final AbilityType type = AbilityType.parse((String) rtype);
			final boolean trap = rtrap == null? false : (Boolean) rtrap;
			final Number ap = (Number) rap;
			final Number mp = (Number) rmp;
			final Number pp = (Number) rpp;
			final Number eff = (Number) reff;
			final Number chance = (Number) rchance;
			final boolean heal = (Boolean) rheal;
			final Range range = Range.fromJSON((JSONObject) rrange);
			final Number recursion = (Number) rrecursion;
			final AbilityZoneType zone = AbilityZoneType.fromString((String) rzone);

			final Optional<AbilityInfo> mana;
			final Optional<AbilityInfo> subsequent;
			final Optional<InstantEffectInfo> instantBefore;
			final Optional<InstantEffectInfo> instantAfter;
			final Optional<StatusEffectInfo> statusEffect;

			if (rmana == null) mana = Optional.empty(); else {
				mana = Optional.of(AbilityInfo.fromJSON((JSONObject) rmana, lib));
			}
			if (rsubsequent == null) subsequent = Optional.empty(); else {
				subsequent = Optional.of(AbilityInfo.fromJSON((JSONObject) rsubsequent, lib));
			}
			if (rinstantBefore == null) instantBefore = Optional.empty(); else {
				instantBefore = Optional.of(new InstantEffectInfo((String) rinstantBefore));
			}
			if (rinstantAfter == null) instantAfter = Optional.empty(); else {
				instantAfter = Optional.of(new InstantEffectInfo((String) rinstantAfter));
			}
			if (rstatusEffect == null) statusEffect = Optional.empty(); else {
				statusEffect = Optional.of(new StatusEffectInfo((String) rstatusEffect));
			}

			final Optional<SpriteInfo> zoneTrapSprite;
			if (rzoneTrapSprite == null) {
				zoneTrapSprite = Optional.empty();
			} else {
				zoneTrapSprite = Optional.of(lib.getSprite((String) rzoneTrapSprite));
			}

			return new AbilityInfo(
				banned, name, type, trap, zone, zoneTrapSprite,
				ap.intValue(), mp.intValue(),
				pp.intValue(), eff.doubleValue(),
				chance.doubleValue(), heal, range,
				mana, subsequent, recursion.intValue(),
				instantBefore, instantAfter, statusEffect);

		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in ability", e);
		}
	}
}

