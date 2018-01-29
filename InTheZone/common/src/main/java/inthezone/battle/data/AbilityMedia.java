package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Optional;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.Implicit;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

/**
 * Icons and sprites and other assets associated with an ability.
 * */
public class AbilityMedia implements JSONable {
	public final static String DEFAULT_ICON = "abilities/default.png";

	public final Image icon;
	public final String iconFile;
	public final Optional<SpriteInfo> zoneTrapSprite;
	public final Optional<SpriteInfo> obstacleSprite;

	private final Optional<String> zoneTrapSpriteID;
	private final Optional<String> obstacleSpriteID;

	@JSON
	public AbilityMedia(
		@Implicit("locator") final ResourceLocator loc,
		@Implicit("library") final Library lib,
		@Field("iconFile")@As("icon") final String iconFile,
		@Field("zoneTrapSpriteID")@As("zoneTrapSprite") final Optional<String> zoneTrapSpriteID,
		@Field("obstacleSpriteID")@As("obstableSprite") final Optional<String> obstacleSpriteID
	) throws CorruptDataException {
		try {
			this.icon = new Image(loc.gfx(iconFile));
		} catch (final IOException e) {
			throw new CorruptDataException("Cannot find ability icon \"" +
				iconFile + "\"");
		}

		if (obstacleSpriteID.isPresent()) {
			obstacleSprite = Optional.of(lib.getSprite(obstacleSpriteID.get()));
		} else {
			obstacleSprite = Optional.empty();
		}

		if (zoneTrapSpriteID.isPresent()) {
			zoneTrapSprite = Optional.of(lib.getSprite(zoneTrapSpriteID.get()));
		} else {
			zoneTrapSprite = Optional.empty();
		}

		this.iconFile = iconFile.equals("") ? DEFAULT_ICON : iconFile;
		this.zoneTrapSpriteID = zoneTrapSpriteID;
		this.obstacleSpriteID = obstacleSpriteID;
	}
}

