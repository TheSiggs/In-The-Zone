package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CharacterInfo implements HasJSONRepresentation {
	public final String name;
	public final String flavourText;
	public final String portraitFile;
	public final Image portrait;
	public final String bigPortraitFile;
	public final Image bigPortrait;
	public final SpriteInfo spriteA;
	public final SpriteInfo spriteB;
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
		final String name,
		final String flavourText,
		final SpriteInfo spriteA,
		final SpriteInfo spriteB,
		final Image portrait,
		final String portraitFile,
		final Image bigPortrait,
		final String bigPortraitFile,
		final Stats stats,
		final Collection<AbilityInfo> abilities,
		final boolean playable,
		final List<Integer> hpCurve,
		final List<Integer> attackCurve,
		final List<Integer> defenceCurve
	) {
		this.name = name;
		this.flavourText = flavourText;
		this.stats = stats;
		this.spriteA = spriteA;
		this.spriteB = spriteB;
		this.portrait = portrait;
		this.portraitFile = portraitFile;
		this.bigPortrait = bigPortrait;
		this.bigPortraitFile = bigPortraitFile;
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
	public AbilityInfo lookupAbility(final String name) {
		return abilitiesIndex.get(name);
	}

	@Override
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("name", name);
		r.put("flavour", flavourText);
		r.put("spriteA", spriteA.id);
		r.put("spriteB", spriteB.id);
		r.put("portrait", portraitFile);
		r.put("bigPortrait", bigPortraitFile);
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

	private static JSONArray makeCurve(final List<Integer> curve) {
		final JSONArray r = new JSONArray();
		for (Integer i : curve) r.put(i);
		return r;
	}

	private static List<Integer> decodeCurve(final JSONArray a) {
		final List<Integer> r = new ArrayList<>();
		for (int i = 0; i < a.length(); i++) r.add(a.getInt(i));
		return r;
	}

	public static CharacterInfo fromJSON(
		final JSONObject json, final ResourceLocator loc, final Library lib
	) throws CorruptDataException
	{
		try {
			final String name = json.getString("name");
			final String flavour = json.optString("flavour", "");
			final Stats stats = Stats.fromJSON(json.getJSONObject("stats"));

			final SpriteInfo spriteA;
			final SpriteInfo spriteB;

			if (json.has("spriteA") && json.has("spriteB")) {
				spriteA = lib.getSprite(json.getString("spriteA"));
				spriteB = lib.getSprite(json.getString("spriteB"));
			} else {
				spriteA = lib.getSprite(json.getString("sprite"));
				spriteB = spriteA;
			}
			
			final String portraitFile = json.getString("portrait");
			final String bigPortraitFile =
				json.optString("bigPortrait", "portrait/generic.png");
			final boolean playable = json.getBoolean("playable");
			final JSONArray abilities = json.getJSONArray("abilities");
			final JSONArray rhpCurve = json.getJSONArray("hpCurve");
			final JSONArray rattackCurve = json.getJSONArray("attackCurve");
			final JSONArray rdefenceCurve = json.getJSONArray("defenceCurve");

			Image portrait;
			Image bigPortrait;
			try {
				portrait = new Image(loc.gfx(portraitFile));
				bigPortrait = new Image(loc.gfx(bigPortraitFile));
			} catch (IOException e) {
				throw new CorruptDataException(
					"Cannot find character portrait for " + name, e);
			}

			final Collection<AbilityInfo> allAbilities = new LinkedList<>();
			for (Object a : abilities) {
				allAbilities.add(AbilityInfo.fromJSON((JSONObject) a, loc, lib));
			}

			final List<Integer> hpCurve = decodeCurve(rhpCurve);
			final List<Integer> attackCurve = decodeCurve(rattackCurve);
			final List<Integer> defenceCurve = decodeCurve(rdefenceCurve);

			return new CharacterInfo(
				name, flavour, spriteA, spriteB, portrait,
				portraitFile, bigPortrait, bigPortraitFile,
				stats, allAbilities, playable, hpCurve, attackCurve, defenceCurve);

		} catch(ClassCastException e) {
			throw new CorruptDataException("Type error in character", e);
		} catch(JSONException e) {
			throw new CorruptDataException("Error parsing character info, " + e.getMessage(), e);
		}
	}
}

