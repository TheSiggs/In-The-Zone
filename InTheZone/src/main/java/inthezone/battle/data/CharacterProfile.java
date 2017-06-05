package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	public Collection<AbilityInfo> allAbilities() {
		final List<AbilityInfo> r = new LinkedList<>(abilities);
		r.add(0, basicAbility);
		return r;
	}

	public CharacterProfile(CharacterInfo rootCharacter)
		throws CorruptDataException
	{
		this.rootCharacter = rootCharacter;
		this.abilities = new ArrayList<>();
		abilities.addAll(rootCharacter.abilities.stream()
			.filter(a -> a.type == AbilityType.SPECIAL)
			.collect(Collectors.toList()));
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
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		final JSONArray a = new JSONArray();
		abilities.stream().forEach(x -> a.put(x.name));
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
		try {
			final CharacterInfo root = gameData.getCharacter(json.getString("for"));
			if (root == null) throw new CorruptDataException(
				"No such character \"" + json.getString("for") + "\" in character profile");

			final Collection<AbilityInfo> abilities =
				jsonArrayToList(json.getJSONArray("abilities"), String.class).stream()
					.map(n -> root.lookupAbility(n))
					.collect(Collectors.toList());

			final AbilityInfo basicAbility =
				root.lookupAbility(json.getString("basicAbility"));

			if (abilities.stream().anyMatch(x -> x == null) || basicAbility == null)
				throw new CorruptDataException("No such ability error in character profile");

			final int attack = json.getInt("attack");
			final int defence = json.getInt("defence");
			final int hp = json.getInt("HP");

			return new CharacterProfile(
				root, abilities, basicAbility, attack, defence, hp);

		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in character profile", e);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing character profile, " + e.getMessage(), e);
		}
	}

	private static <T> List<T> jsonArrayToList(JSONArray a, Class<T> clazz)
		throws ClassCastException
	{
		List<T> r = new ArrayList<>();
		int limit = a.length();
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

