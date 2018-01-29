package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.Implicit;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

/**
 * A character with a selection of abilities and status buffs.
 * */
public class CharacterProfile implements JSONable {
	public final CharacterInfo rootCharacter;
	public final Collection<AbilityInfo> abilities;
	public final AbilityInfo basicAbility;
	public final int attackPP;
	public final int hpPP;

	private final int extraAttack;
	private final int extraHP;

	private final String rootCharacterName;

	/**
	 * Get a list of all the abilities that this character profile can perform.
	 * */
	public Collection<AbilityInfo> allAbilities() {
		final List<AbilityInfo> r = new LinkedList<>(abilities);
		r.add(0, basicAbility);
		return r;
	}

	/**
	 * Create a default character profile with no abilities or extra status buffs selected.
	 * */
	public CharacterProfile(final CharacterInfo rootCharacter)
		throws CorruptDataException
	{
		this(
			rootCharacter, 
			rootCharacter.abilities.stream()
				.filter(a -> a.type == AbilityType.SPECIAL)
				.collect(Collectors.toList()),
			rootCharacter.abilities.stream()
				.filter(a -> a.type == AbilityType.BASIC)
				.findFirst().orElseThrow(() ->
					new CorruptDataException("No basic ability for " + rootCharacter.name)),
			0,
			0);
	}

	public CharacterProfile(
		final CharacterInfo rootCharacter,
		final Collection<AbilityInfo> abilities,
		final AbilityInfo basicAbility,
		final int attackPP,
		final int hpPP
	) throws CorruptDataException {
		this.rootCharacter = rootCharacter;
		this.rootCharacterName = rootCharacter.name;
		this.abilities = abilities;
		this.basicAbility = basicAbility;

		this.attackPP = attackPP;
		this.hpPP = hpPP;

		if (attackPP < 0 || attackPP > rootCharacter.attackCurve.size())
			throw new CorruptDataException("Invalid attack pp " + attackPP);
		if (hpPP < 0 || hpPP > rootCharacter.hpCurve.size())
			throw new CorruptDataException("Invalid hp pp " + hpPP);

		this.extraAttack = attackPP == 0? 0 :
			rootCharacter.attackCurve.get(attackPP - 1) - rootCharacter.stats.attack;
		this.extraHP = hpPP == 0? 0 :
			rootCharacter.hpCurve.get(hpPP - 1) - rootCharacter.stats.hp;
	}

	@JSON
	public CharacterProfile(
		@Implicit("gameData") final GameDataFactory gameData,
		@Field("rootCharacterName")@As("for") final String rootCharacterName,
		@Field("abilities") final Collection<AbilityInfo> abilities,
		@Field("basicAbility") final AbilityInfo basicAbility,
		@Field("attackPP")@As("attack") final int attackPP,
		@Field("hpPP")@As("HP") final int hpPP
	) throws CorruptDataException {
		this(gameData.getCharacter(rootCharacterName), abilities, basicAbility, attackPP, hpPP);
	}

	/**
	 * Get the base stats, taking PP into account.
	 * */
	public Stats getBaseStats() {
		return rootCharacter.stats.add(new Stats(
			0, 0, 0, extraHP, extraAttack, 0));
	}

	/**
	 * Compute the cost of this profile in PP.
	 * */
	public int computeCost() {
		return hpPP + attackPP + basicAbility.pp +
			abilities.stream().map(a -> a.pp).collect(
				Collectors.summingInt(x -> (int) x));
	}

	@Override public String toString() {
		return rootCharacter.name;
	}
}

