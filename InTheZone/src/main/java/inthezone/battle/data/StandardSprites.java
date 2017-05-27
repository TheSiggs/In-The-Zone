package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Load and store the stock sprites.
 * */
public class StandardSprites {
	public final SpriteInfo roadBlock;
	public final SpriteInfo trap;
	public final SpriteInfo zone;
	public final Map<StatusEffectType, Image> statusEffects = new HashMap<>();
	public final Image attackIcon;
	public final Image pushIcon;
	public final Image potionIcon;

	public StandardSprites(Library l, ResourceLocator loc) throws CorruptDataException {
		System.err.println("Make standard sprites");
		this.roadBlock = l.getSprite("roadblock");
		this.trap = l.getSprite("trap");
		this.zone = l.getSprite("zone");
		if (roadBlock == null) throw new CorruptDataException("Missing roadblock sprite");
		if (trap == null) throw new CorruptDataException("Missing trap sprite");
		if (zone == null) throw new CorruptDataException("Missing zone sprite");
		Arrays.stream(StatusEffectType.class.getEnumConstants()).forEach(t -> {
			try {
				statusEffects.put(t, new Image(loc.gfx(t.getIconName())));
			} catch (IOException e) {
				/* Do nothing for now.  We'll detect this condition later */
			}
		});

		// Detect errors in the status effects map
		if (statusEffects.size() != StatusEffectType.class.getEnumConstants().length) {
			throw new CorruptDataException("Missing status effect icons");
		}

		try {
			this.attackIcon = new Image(loc.gfx("abilities/attack.png"));
			this.potionIcon = new Image(loc.gfx("abilities/potion.png"));
			this.pushIcon = new Image(loc.gfx("abilities/push.png"));
		} catch (IOException e) {
			throw new CorruptDataException("Missing command icon", e);
		}
	}
}

