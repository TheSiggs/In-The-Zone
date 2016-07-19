package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CharacterInfo implements HasJSONRepresentation {
	public final String name;
	public final SpriteInfo sprite;
	public final Stats stats;
	public final Collection<AbilityInfo> abilities;

	private final Map<String, AbilityInfo> abilitiesIndex = new HashMap<>();

	public CharacterInfo(
		String name, SpriteInfo sprite, Stats stats, Collection<AbilityInfo> abilities
	) {
		this.name = name;
		this.stats = stats;
		this.sprite = sprite;
		this.abilities = abilities;

		for (AbilityInfo a : abilities) abilitiesIndex.put(a.name, a);
	}

	/**
	 * Get an ability by name.  May return null.
	 * */
	public AbilityInfo lookupAbility(String name) {
		return abilitiesIndex.get(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("name", name);
		r.put("sprite", sprite.id);
		r.put("stats", stats.getJSON());
		JSONArray as = new JSONArray();
		for (AbilityInfo a : abilities) {
			as.add(a.getJSON());
		}
		r.put("abilities", as);
		return r;
	}

	public static CharacterInfo fromJSON(JSONObject json, Library lib)
		throws CorruptDataException
	{
		Object rname = json.get("name");
		Object rstats = json.get("stats");
		Object rsprite = json.get("sprite");
		Object rabilities = json.get("abilities");

		try {
			if (rname == null) throw new CorruptDataException("Missing character name");
			String name = (String) rname;

			if (rstats == null)
				throw new CorruptDataException("Missing character stats");
			Stats stats = Stats.fromJSON((JSONObject) rstats);

			// If someone ever manages to leave the sprite for a new character null,
			// then the game data won't load without some manual fixup.  Must be
			// careful then about adding new characters.  (Or fix the data editor so
			// that it won't save until all the character sprites are set).
			if (rsprite == null)
				throw new CorruptDataException("Missing character sprite");

			SpriteInfo sprite = lib.getSprite((String) rsprite);

			if (rabilities == null)
				throw new CorruptDataException("No abilities defined for character " + name);
			JSONArray abilities = (JSONArray) rabilities;

			Collection<AbilityInfo> allAbilities = new LinkedList<>();
			for (Object a : abilities) {
				allAbilities.add(AbilityInfo.fromJSON((JSONObject) a));
			}

			return new CharacterInfo(name, sprite, stats, allAbilities);
		} catch(ClassCastException e) {
			throw new CorruptDataException("Type error in character", e);
		}
	}
}

