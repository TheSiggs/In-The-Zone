package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CharacterInfo implements HasJSONRepresentation {
	public final String name;
	public final String portraitFile;
	public final Image portrait;
	public final SpriteInfo sprite;
	public final Stats stats;
	public final Collection<AbilityInfo> abilities;
	public final boolean playable;

	private final Map<String, AbilityInfo> abilitiesIndex = new HashMap<>();

	public CharacterInfo(
		String name,
		SpriteInfo sprite,
		Image portrait,
		String portraitFile,
		Stats stats,
		Collection<AbilityInfo> abilities,
		boolean playable
	) {
		this.name = name;
		this.stats = stats;
		this.sprite = sprite;
		this.portrait = portrait;
		this.portraitFile = portraitFile;
		this.abilities = abilities;
		this.playable = playable;

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
		r.put("portrait", portraitFile);
		r.put("playable", playable);
		r.put("stats", stats.getJSON());
		JSONArray as = new JSONArray();
		for (AbilityInfo a : abilities) {
			as.add(a.getJSON());
		}
		r.put("abilities", as);
		return r;
	}

	public static CharacterInfo fromJSON(
		JSONObject json, ResourceLocator loc, Library lib
	) throws CorruptDataException
	{
		Object rname = json.get("name");
		Object rstats = json.get("stats");
		Object rsprite = json.get("sprite");
		Object rportrait = json.get("portrait");
		Object rplayable = json.get("playable");
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

			if (rportrait == null)
				throw new CorruptDataException("Missing character portrait");

			Image portrait;
			String portraitFile;
			try {
				portraitFile = (String) rportrait;
				portrait = new Image(loc.gfx(portraitFile));
			} catch (IOException e) {
				throw new CorruptDataException("Cannot find character portrait");
			}

			if (rplayable == null)
				throw new CorruptDataException("Missing character sprite");
			boolean playable = (Boolean) rplayable;

			if (rabilities == null)
				throw new CorruptDataException("No abilities defined for character " + name);
			JSONArray abilities = (JSONArray) rabilities;

			Collection<AbilityInfo> allAbilities = new LinkedList<>();
			for (Object a : abilities) {
				allAbilities.add(AbilityInfo.fromJSON((JSONObject) a));
			}

			return new CharacterInfo(
				name, sprite, portrait, portraitFile, stats, allAbilities, playable);
		} catch(ClassCastException e) {
			throw new CorruptDataException("Type error in character", e);
		}
	}
}

