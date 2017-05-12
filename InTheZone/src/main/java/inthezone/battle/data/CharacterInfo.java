package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CharacterInfo implements HasJSONRepresentation {
	public final String name;
	public final String portraitFile;
	public final Image portrait;
	public final SpriteInfo sprite;
	public final Stats stats;
	public final Collection<AbilityInfo> abilities;
	public final boolean playable;

	public final List<Integer> hpCurve;
	public final List<Integer> attackCurve;
	public final List<Integer> defenceCurve;

	@Override public String toString() {
		return name;
	}

	private final Map<String, AbilityInfo> abilitiesIndex = new HashMap<>();

	public CharacterInfo(
		String name,
		SpriteInfo sprite,
		Image portrait,
		String portraitFile,
		Stats stats,
		Collection<AbilityInfo> abilities,
		boolean playable,
		List<Integer> hpCurve,
		List<Integer> attackCurve,
		List<Integer> defenceCurve
	) {
		this.name = name;
		this.stats = stats;
		this.sprite = sprite;
		this.portrait = portrait;
		this.portraitFile = portraitFile;
		this.abilities = abilities;
		this.playable = playable;

		this.hpCurve = hpCurve;
		this.attackCurve = attackCurve;
		this.defenceCurve = defenceCurve;

		for (AbilityInfo a : abilities) abilitiesIndex.put(a.name, a);
	}

	/**
	 * Get an ability by name.  May return null.
	 * */
	public AbilityInfo lookupAbility(String name) {
		return abilitiesIndex.get(name);
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("name", name);
		r.put("sprite", sprite.id);
		r.put("portrait", portraitFile);
		r.put("playable", playable);
		r.put("stats", stats.getJSON());
		final JSONArray as = new JSONArray();
		for (AbilityInfo a : abilities) as.put(a.getJSON());
		r.put("abilities", as);
		r.put("hpCurve", makeCurve(hpCurve));
		r.put("attackCurve", makeCurve(attackCurve));
		r.put("defenceCurve", makeCurve(defenceCurve));
		return r;
	}

	private static JSONArray makeCurve(List<Integer> curve) {
		final JSONArray r = new JSONArray();
		for (Integer i : curve) r.put(i);
		return r;
	}

	private static List<Integer> decodeCurve(JSONArray a) {
		List<Integer> r = new ArrayList<>();
		for (int i = 0; i < a.length(); i++) r.add(a.getInt(i));
		return r;
	}

	public static CharacterInfo fromJSON(
		JSONObject json, ResourceLocator loc, Library lib
	) throws CorruptDataException
	{
		try {
			final String name = json.getString("name");
			final Stats stats = Stats.fromJSON(json.getJSONObject("stats"));
			final SpriteInfo sprite = lib.getSprite(json.getString("sprite"));
			final String portraitFile = json.getString("portrait");
			final boolean playable = json.getBoolean("playable");
			final JSONArray abilities = json.getJSONArray("abilities");
			final JSONArray rhpCurve = json.getJSONArray("hpCurve");
			final JSONArray rattackCurve = json.getJSONArray("attackCurve");
			final JSONArray rdefenceCurve = json.getJSONArray("defenceCurve");

			Image portrait;
			try {
				portrait = new Image(loc.gfx(portraitFile));
			} catch (IOException e) {
				throw new CorruptDataException("Cannot find character portrait");
			}

			final Collection<AbilityInfo> allAbilities = new LinkedList<>();
			for (Object a : abilities) {
				allAbilities.add(AbilityInfo.fromJSON((JSONObject) a, lib));
			}

			final List<Integer> hpCurve = decodeCurve(rhpCurve);
			final List<Integer> attackCurve = decodeCurve(rattackCurve);
			final List<Integer> defenceCurve = decodeCurve(rdefenceCurve);

			return new CharacterInfo(
				name, sprite, portrait, portraitFile, stats, allAbilities, playable,
				hpCurve, attackCurve, defenceCurve);

		} catch(ClassCastException e) {
			throw new CorruptDataException("Type error in character", e);
		} catch(JSONException e) {
			throw new CorruptDataException("Error parsing character info, " + e.getMessage(), e);
		}
	}
}

