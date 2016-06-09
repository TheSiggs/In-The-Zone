package inthezone.battle.data;

import inthezone.battle.Character;
import inthezone.battle.InventoryItem;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import java.util.Collection;
import org.json.simple.JSONObject;

public class Loadout implements HasJSONRepresentation {
	public final Character c1;
	public final Character c2;
	public final Character c3;
	public final Character c4;
	public final Collection<InventoryItem> items;

	public Loadout(
		Character c1, Character c2, Character c3, Character c4,
		Collection<InventoryItem> items
	) {
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		this.c4 = c4;
		this.items = items;
	}

	@Override
	public JSONObject getJSON() {
		// TODO: implement this
		return new JSONObject();
	}

	public static Loadout fromJSON(JSONObject json) throws CorruptDataException {
		// TODO: implement this
		return new Loadout(null, null, null, null, null);
	}
}

