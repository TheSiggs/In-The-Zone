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
	public final Map<StatusEffectType, Image> statusEffects = new HashMap<>();

	public StandardSprites(Library l, ResourceLocator loc) throws CorruptDataException {
		this.roadBlock = l.getSprite("roadblock");
		this.trap = l.getSprite("trap");
		if (roadBlock == null) throw CorruptDataException("Missing roadblock sprite");
		if (trap == null) throw CorruptDataException("Missing trap sprite");
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
	}
}

