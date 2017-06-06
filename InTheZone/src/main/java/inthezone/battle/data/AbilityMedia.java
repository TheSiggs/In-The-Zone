package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.HasJSONRepresentation;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class AbilityMedia implements HasJSONRepresentation {
	public final static String DEFAULT_ICON = "abilities/default.png";

	public final Image icon;
	public final String iconFile;
	public final Optional<SpriteInfo> zoneTrapSprite;

	public AbilityMedia(
		Image icon,
		String iconFile,
		Optional<SpriteInfo> zoneTrapSprite
	) {
		this.icon = icon;
		this.iconFile = iconFile;
		this.zoneTrapSprite = zoneTrapSprite;
	}

	@Override public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("icon", iconFile);
		zoneTrapSprite.ifPresent(e -> r.put("zoneTrapSprite", e.id));
		return r;
	}

	public static AbilityMedia fromJSON(
		JSONObject json, ResourceLocator loc, Library lib
	) throws CorruptDataException {
		try {
			final String iconFile = json.optString("icon", DEFAULT_ICON);
			final String rzoneTrapSprite = json.optString("zoneTrapSprite", null);

			final Optional<SpriteInfo> zoneTrapSprite;
			if (rzoneTrapSprite == null) {
				zoneTrapSprite = Optional.empty();
			} else {
				zoneTrapSprite = Optional.of(lib.getSprite((String) rzoneTrapSprite));
			}

			final Image icon;
			try {
				icon = new Image(loc.gfx(iconFile));
			} catch (IOException e) {
				throw new CorruptDataException("Cannot find ability icon " + iconFile);
			}

			return new AbilityMedia(icon, iconFile, zoneTrapSprite);

		} catch (JSONException e) {
			throw new CorruptDataException("Error parsing ability media, " + e.getMessage(), e);
		}
	}
}

