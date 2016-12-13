package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CharacterProfile implements HasJSONRepresentation {
	public final CharacterInfo rootCharacter;
	public final Collection<AbilityInfo> abilities;
	public final AbilityInfo basicAbility;
	public final int attackPP;
	public final int defencePP;
	public final int hpPP;

	private final int extraAttack;
	private final int extraDefence;
	private final int extraHP;

	public CharacterProfile(CharacterInfo rootCharacter)
		throws CorruptDataException
	{
		this.rootCharacter = rootCharacter;
		this.abilities = new ArrayList<>();
		basicAbility = rootCharacter.abilities.stream()
			.filter(a -> a.type == AbilityType.BASIC)
			.findFirst().orElseThrow(() ->
				new CorruptDataException("No basic ability for " + rootCharacter.name));
		extraAttack = 0;
		extraDefence = 0;
		extraHP = 0;

		attackPP = 0;
		defencePP = 0;
		hpPP = 0;
	}

	public CharacterProfile(
		CharacterInfo rootCharacter,
		Collection<AbilityInfo> abilities,
		AbilityInfo basicAbility,
		int attackPP,
		int defencePP,
		int hpPP
	) throws CorruptDataException {
		this.rootCharacter = rootCharacter;
		this.abilities = abilities;
		this.basicAbility = basicAbility;

		this.attackPP = attackPP;
		this.defencePP = defencePP;
		this.hpPP = hpPP;

		if (attackPP < 0 || attackPP > rootCharacter.attackCurve.size())
			throw new CorruptDataException("Invalid attack pp " + attackPP);
		if (defencePP < 0 || defencePP > rootCharacter.defenceCurve.size())
			throw new CorruptDataException("Invalid defence pp " + defencePP);
		if (hpPP < 0 || hpPP > rootCharacter.hpCurve.size())
			throw new CorruptDataException("Invalid hp pp " + hpPP);

		this.extraAttack = attackPP == 0? 0 :
			rootCharacter.attackCurve.get(attackPP - 1) - rootCharacter.stats.attack;
		this.extraDefence = defencePP == 0? 0 :
			rootCharacter.defenceCurve.get(defencePP - 1) - rootCharacter.stats.defence;
		this.extraHP = hpPP == 0? 0 :
			rootCharacter.hpCurve.get(hpPP - 1) - rootCharacter.stats.hp;
	}

	/**
	 * Get the base stats, taking PP into account.
	 * */
	public Stats getBaseStats() {
		return rootCharacter.stats.add(new Stats(
			0, 0, 0, extraHP, extraAttack, extraDefence));
	}

	/**
	 * Compute the cost of this profile in PP.
	 * */
	public int computeCost() {
		return hpPP + attackPP + defencePP + basicAbility.pp +
			abilities.stream().map(a -> a.pp).collect(
				Collectors.summingInt(x -> (int) x));
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		JSONArray a = new JSONArray();
		abilities.stream().forEach(x -> a.add(x.name));
		r.put("for", rootCharacter.name);
		r.put("abilities", a);
		r.put("basicAbility", basicAbility.name);
		r.put("attack", attackPP);
		r.put("defence", defencePP);
		r.put("HP", hpPP);
		return r;
	}

	public static CharacterProfile fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws CorruptDataException {
		Object oroot = json.get("for");
		Object oabilities = json.get("abilities");
		Object obasicAbility = json.get("basicAbility");
		Object oattack = json.get("attack");
		Object odefence = json.get("defence");
		Object ohp = json.get("HP");

		if (oroot == null) throw new CorruptDataException("Missing root in character profile");
		if (oabilities == null) throw new CorruptDataException("Missing abilities in character profile");
		if (obasicAbility == null) throw new CorruptDataException("Missing basicAbility in character profile");
		if (oattack == null) throw new CorruptDataException("Missing attack in character profile");
		if (odefence == null) throw new CorruptDataException("Missing defence in character profile");
		if (ohp == null) throw new CorruptDataException("Missing HP in character profile");

		try {
			final CharacterInfo root = gameData.getCharacter((String) oroot);
			if (root == null) throw new CorruptDataException(
				"No such character " + (String) oroot + " in character profile");

			final Collection<AbilityInfo> abilities =
				jsonArrayToList((JSONArray) oabilities, String.class).stream()
					.map(n -> root.lookupAbility(n))
					.collect(Collectors.toList());

			final AbilityInfo basicAbility = root.lookupAbility((String) obasicAbility);

			if (abilities.stream().anyMatch(x -> x == null) || basicAbility == null)
				throw new CorruptDataException("No such ability error in character profile");

			return new CharacterProfile(root, abilities, basicAbility,
				((Number) oattack).intValue(),
				((Number) odefence).intValue(),
				((Number) ohp).intValue());
		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in character profile", e);
		}
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		List<T> r = new ArrayList<>();
		int limit = a.size();
		for (int i = 0; i < limit; i++) {
			r.add(clazz.cast(a.get(i)));
		}
		return r;
	}

	@Override
	public String toString() {
		return rootCharacter.name;
	}
}

