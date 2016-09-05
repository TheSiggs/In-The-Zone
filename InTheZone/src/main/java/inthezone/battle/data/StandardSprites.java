package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;

/**
 * Load and store the stock sprites.
 * */
public class StandardSprites {
	public final SpriteInfo roadBlock;

	public StandardSprites(Library l) throws CorruptDataException {
		this.roadBlock = l.getSprite("roadblock");
	}
}

