package inthezone.game.lobby;

import isogame.GlobalConstants;
import isogame.engine.CameraAngle;
import isogame.engine.FacingDirection;
import isogame.engine.SpriteAnimation;
import isogame.engine.SpriteInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Player;

public class CharacterSelector extends Canvas {
	private final List<CharacterProfile> characters = new ArrayList<>();
	private final List<Boolean> enabled = new ArrayList<>();
	private Optional<CharacterProfile> selected = Optional.empty();

	private int cw = 0;
	private int w = 0;
	private int h = 0;

	private Player player = Player.PLAYER_OBSERVER;

	final double scaleFactor = 2;

	public CharacterSelector() {
		this(new ArrayList<>(), Player.PLAYER_OBSERVER);
	}

	public CharacterSelector(
		final Collection<CharacterProfile> characters, final Player player
	) {
		this.player = player;
		this.getGraphicsContext2D().scale(1.0d/scaleFactor, 1.0d/scaleFactor);
		setCharacters(characters, player);
	}

	public void setCharacters(
		final Collection<CharacterProfile> characters,
		final Player player
	) {
		this.player = player;
		this.characters.clear();
		this.enabled.clear();
		this.selected = Optional.empty();

		this.characters.addAll(characters);
		for (CharacterProfile p : characters) this.enabled.add(true);

		if (characters.size() > 0) {
			final SpriteAnimation a = player == Player.PLAYER_A?
				this.characters.get(0).rootCharacter.spriteA.defaultAnimation:
				this.characters.get(0).rootCharacter.spriteB.defaultAnimation;
			this.cw = a.w;
			this.w = cw * characters.size();
			this.h = a.h;
			this.setHeight(h / scaleFactor);
			this.setWidth(w / scaleFactor);

			this.setOnMouseClicked(event -> {
				int x = (int) ((double) event.getX() * scaleFactor);
				int y = (int) (((double) event.getY() * scaleFactor) -
					(double) h + GlobalConstants.TILEH);
				int i = (int) Math.floor((double) x / GlobalConstants.TILEW);
				x = (int) ((double) x - ((double) i * GlobalConstants.TILEW));
				final SpriteAnimation anim = player == Player.PLAYER_A?
					this.characters.get(i).rootCharacter.spriteA.animations.get("idle"):
					this.characters.get(i).rootCharacter.spriteB.animations.get("idle");
				if (anim.hitTest(x, y, 0, CameraAngle.UL, FacingDirection.DOWN)) {
					selected = Optional.of(this.characters.get(i));
					render();
				}
			});
		} else {
			cw = 0;
			h = 0;
			w = 0;
		}

		render();
	}

	private void render() {
		final GraphicsContext gx = this.getGraphicsContext2D();
		gx.clearRect(0, 0, w, h);
		for (int i = 0; i < characters.size(); i++) {
			renderCharacter(gx, characters.get(i), i);
		}
	}

	private void renderCharacter(
		final GraphicsContext gx, final CharacterProfile p, final int i
	) {
		if (!enabled.get(i)) return;

		final SpriteInfo sprite = player == Player.PLAYER_A?
			p.rootCharacter.spriteA: p.rootCharacter.spriteB;

		final SpriteAnimation a = sprite.animations.get(
			selected.map(c -> {
				if (c.rootCharacter.name.equals(p.rootCharacter.name))
					return "selected"; else return "idle";}).orElse("idle"));

		gx.save();
		gx.translate(i * cw, 0);
		a.renderFrame(gx, 0, (int) GlobalConstants.TILEW, 0,
			CameraAngle.UL, FacingDirection.DOWN);
		gx.restore();
	}

	public Optional<CharacterProfile> getSelectedCharacter() {
		return selected;
	}

	public void setSelectedCharacter(final Optional<CharacterProfile> c) {
		selected = c;
		render();
	}

	public void setCharacterEnabled(
		final CharacterProfile character, final boolean enabled
	) {
		final int i = characters.indexOf(character);

		if (i >= 0 && i < this.enabled.size()) this.enabled.set(i, enabled);

		if (
			selected.map(c ->
				c.rootCharacter.name.equals(character.rootCharacter.name))
			.orElse(false)
		) {
			selected = Optional.empty();
		}
		render();
	}
}

