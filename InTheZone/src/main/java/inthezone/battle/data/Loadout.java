package inthezone.battle.data;

import inthezone.battle.Character;
import inthezone.battle.InventoryItem;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Loadout implements HasJSONRepresentation {
	public final List<CharacterProfile> characters;

	public Loadout(
		List<CharacterProfile> characters
	) {
		this.characters = characters;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject o = new JSONObject();
		JSONArray cs = new JSONArray();
		for (CharacterProfile p : characters) cs.add(p.getJSON());
		o.put("characters", cs);
		return o;
	}

	public static Loadout fromJSON(
		JSONObject json, GameDataFactory gameData
	) throws CorruptDataException {
		Object ocs = json.get("characters");
		if (ocs == null) throw new CorruptDataException("Missing characters in loadout");

		try {
			final List<JSONObject> cs =
				jsonArrayToList((JSONArray) ocs, JSONObject.class);
			final List<CharacterProfile> r = new ArrayList<>();
			for (JSONObject c : cs) r.add(CharacterProfile.fromJSON(c, gameData));

			return new Loadout(r);
		} catch (ClassCastException e) {
			throw new CorruptDataException("Type error in loadout", e);
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
}

