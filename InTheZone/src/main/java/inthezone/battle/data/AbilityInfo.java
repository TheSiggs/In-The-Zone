package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.Optional;
import org.json.simple.JSONObject;

public class AbilityInfo implements HasJSONRepresentation {
	public final String name;
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

	@Override
	public String toString() {
		return name;
	}

	public AbilityInfo(
		String name,
		AbilityType type,
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
		Optional<StatusEffectInfo> statusEffect
	) {
		this.name = name;
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

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("name", name);
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

	public static AbilityInfo fromJSON(JSONObject json)
		throws CorruptDataException
	{
		Object rname = json.get("name");
		Object rtype = json.get("type");
		Object rtrap = json.get("trap");
		Object rzoneTurns = json.get("zoneTurns"); // deprecated
		Object rboundZone = json.get("boundZone"); // deprecated
		Object rzone = json.get("zone");
		Object rap = json.get("ap");
		Object rmp = json.get("mp");
		Object rpp = json.get("pp");
		Object reff = json.get("eff");
		Object rchance = json.get("chance");
		Object rheal = json.get("heal");
		Object rrange = json.get("range");
		Object rmana = json.get("mana");
		Object rsubsequent = json.get("subsequent");
		Object rrecursion = json.get("recursion");
		Object rinstantBefore = json.get("instantBefore");
		Object rinstantAfter = json.get("instantAfter");
		Object rstatusEffect = json.get("statusEffect");

		try {
			if (rname == null) throw new CorruptDataException("Missing name in ability");
			String name = (String) rname;

			if (rtype == null) throw new CorruptDataException("Missing type in ability " + name);
			if (rap == null) throw new CorruptDataException("Missing ap in ability " + name);
			if (rmp == null) throw new CorruptDataException("Missing mp in ability " + name);
			if (rpp == null) throw new CorruptDataException("Missing pp in ability " + name);
			if (reff == null) throw new CorruptDataException("Missing eff in ability " + name);
			if (rchance == null) throw new CorruptDataException("Missing chance in ability " + name);
			if (rheal == null) throw new CorruptDataException("Missing heal in ability " + name);
			if (rrange == null) throw new CorruptDataException("Missing range in ability " + name);
			if (rrecursion == null) throw new CorruptDataException("Missing recursion in ability " + name);

			AbilityType type = AbilityType.parse((String) rtype);
			boolean trap = rtrap == null? false : (Boolean) rtrap;
			Number ap = (Number) rap;
			Number mp = (Number) rmp;
			Number pp = (Number) rpp;
			Number eff = (Number) reff;
			Number chance = (Number) rchance;
			boolean heal = (Boolean) rheal;
			Range range = Range.fromJSON((JSONObject) rrange);
			Number recursion = (Number) rrecursion;
			Optional<AbilityInfo> mana;
			Optional<AbilityInfo> subsequent;
			Optional<InstantEffectInfo> instantBefore;
			Optional<InstantEffectInfo> instantAfter;
			Optional<StatusEffectInfo> statusEffect;

			AbilityZoneType zone;
			if (rzone == null) {
				// boundZone and zoneTurns are deprecated, but we might have to parse
				// them for compatability.
				if (rboundZone != null && ((Boolean) rboundZone)) {
					zone = AbilityZoneType.BOUND_ZONE;
				} else if (rzoneTurns != null && ((Number) rzoneTurns).intValue() > 0) {
					zone = AbilityZoneType.ZONE;
				} else {
					zone = AbilityZoneType.NONE;
				}
			} else {
				zone = AbilityZoneType.fromString((String) rzone);
			}

			if (rmana == null) mana = Optional.empty(); else {
				mana = Optional.of(AbilityInfo.fromJSON((JSONObject) rmana));
			}
			if (rsubsequent == null) subsequent = Optional.empty(); else {
				subsequent = Optional.of(AbilityInfo.fromJSON((JSONObject) rsubsequent));
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

			return new AbilityInfo(
				name, type, trap, zone,
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

