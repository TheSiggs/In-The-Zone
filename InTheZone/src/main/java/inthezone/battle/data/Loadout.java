package inthezone.battle.data;

import inthezone.battle.Character;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Loadout implements HasJSONRepresentation {
	public final String name;
	public final List<CharacterProfile> characters = new ArrayList<>();
	public final List<CharacterProfile> otherCharacters = new ArrayList<>();

	public final static int maxPP = 30;
	public final static int maxCharacters = 4;

	public Loadout(
		String name,
		List<CharacterProfile> characters,
		List<CharacterProfile> otherCharacters
	) {
		this.name = name;
		this.characters.addAll(characters);
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

	@Override
	public JSONObject getJSON() {
		final JSONObject o = new JSONObject();
		final JSONArray cs = new JSONArray();
		final JSONArray ocs = new JSONArray();
		for (CharacterProfile p : characters) cs.put(p.getJSON());
		for (CharacterProfile p : otherCharacters) ocs.put(p.getJSON());
		o.put("name", name);
		o.put("characters", cs);
		o.put("otherCharacters", ocs);
		return o;
	}

	public static Loadout fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws CorruptDataException {
		try {
			final String name = json.getString("name");
			final List<JSONObject> cs =
				jsonArrayToList(json.getJSONArray("characters"), JSONObject.class);
			final JSONArray ocs = json.optJSONArray("otherCharacters");

			final List<CharacterProfile> r = new ArrayList<>();
			for (JSONObject c : cs) r.add(CharacterProfile.fromJSON(c, gameData));

			final List<CharacterProfile> others = new ArrayList<>();
			if (ocs != null) {
				for (Object c : ocs)
					others.add(CharacterProfile.fromJSON((JSONObject) c, gameData));
			}

			return new Loadout(name, r, others);

		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in loadout", e);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing loadout, " + e.getMessage(), e);
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

	@Override public String toString() {
		return name + (isLegitimate()? "" : " (BANNED)");
	}
}

