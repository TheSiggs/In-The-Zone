package inthezone.battle.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSONConstructor;
import ssjsjs.JSONable;

/**
 * A user defined loadout.  Essentially a collection of character profiles.
 * */
public class Loadout implements JSONable {
	public final String name;
	public final List<CharacterProfile> characters = new ArrayList<>();
	public final List<CharacterProfile> otherCharacters = new ArrayList<>();

	public final static int maxPP = 25;
	public final static int maxCharacters = 4;

	@JSONConstructor
	public Loadout(
		@Field("name") final String name,
		@Field("characters") final Collection<CharacterProfile> characters,
		@Field("otherCharacters") final Collection<CharacterProfile> otherCharacters
	) {
		this.name = name;
		this.characters.addAll(characters);
		this.otherCharacters.addAll(otherCharacters);
	}

	/**
	 * Determine if this loadout is suitable for tournament play. i.e. it has no
	 * banned abilities and the total cost is acceptable.
	 * */
	public boolean isLegitimate() {
		return
			characters.size() > 0 && characters.size() <= maxCharacters &&
			!characters.stream().flatMap(c -> c.abilities.stream())
				.anyMatch(a -> a.banned) &&
			characters.stream().map(c -> c.computeCost()).collect(
				Collectors.summingInt(x -> (int) x)) <= maxPP;
	}

	@Override public String toString() {
		return name + (isLegitimate()? "" : " (BANNED)");
	}
}

