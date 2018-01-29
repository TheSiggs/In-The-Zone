package inthezone.battle.data;

import isogame.engine.CorruptDataException;
import isogame.engine.Library;
import isogame.engine.SpriteInfo;
import isogame.resource.ResourceLocator;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.Implicit;
import ssjsjs.annotations.JSON;
import ssjsjs.JSONable;

public class CharacterInfo implements JSONable {
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

	public final List<Integer> hpCurve = new ArrayList<>();
	public final List<Integer> attackCurve = new ArrayList<>();
	public final List<Integer> defenceCurve = new ArrayList<>();

	private final String spriteAID;
	private final String spriteBID;

	@Override public String toString() {
		return name;
	}

	private final Map<String, AbilityInfo> abilitiesIndex = new HashMap<>();

	@JSON
	public CharacterInfo(
		@Implicit("locator") final ResourceLocator loc,
		@Implicit("library") final Library lib,
		@Field("name") final String name,
		@Field("flavour") final String flavourText,
		@Field("spriteAID")@As("spriteA") final String spriteAID,
		@Field("spriteBID")@As("spriteB") final String spriteBID,
		@Field("portraitFile")@As("portrait") final String portraitFile,
		@Field("bigPortraitFile")@As("bigPortrait") final String bigPortraitFile,
		@Field("stats") final Stats stats,
		@Field("abilities") final Collection<AbilityInfo> abilities,
		@Field("playable") final boolean playable,
		@Field("hpCurve") final Integer[] hpCurve,
		@Field("attackCurve") final Integer[] attackCurve,
		@Field("defenceCurve") final Integer[] defenceCurve
	) throws CorruptDataException {
		try {
			this.portrait = new Image(loc.gfx(portraitFile));
			this.bigPortrait = new Image(loc.gfx(bigPortraitFile));
		} catch (final IOException e) {
			throw new CorruptDataException(
				"Cannot find character portrait for " + name, e);
		}

		this.spriteA = lib.getSprite(spriteAID);
		this.spriteB = lib.getSprite(spriteBID);

		this.name = name;
		this.flavourText = flavourText;
		this.stats = stats;
		this.spriteAID = spriteAID;
		this.spriteBID = spriteBID;
		this.portraitFile = portraitFile;
		this.bigPortraitFile = bigPortraitFile;
		this.abilities = abilities;
		this.playable = playable;

		this.hpCurve.addAll(Arrays.asList(hpCurve));
		this.attackCurve.addAll(Arrays.asList(attackCurve));
		this.defenceCurve.addAll(Arrays.asList(defenceCurve));

		for (final AbilityInfo a : abilities) {
			a.fixAttributes();
			abilitiesIndex.put(a.name, a);
		}
	}

	/**
	 * Get an ability by name.  May return null.
	 * */
	public AbilityInfo lookupAbility(final String name) {
		return abilitiesIndex.get(name);
	}
}

