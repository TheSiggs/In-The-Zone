package inthezone.game.lobby;

import inthezone.battle.data.CharacterProfile;
import isogame.engine.CameraAngle;
import isogame.engine.FacingDirection;
import isogame.engine.SpriteAnimation;
import isogame.GlobalConstants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CharacterSelector extends Canvas {
	private final List<CharacterProfile> characters = new ArrayList<>();
	private final List<Boolean> enabled = new ArrayList<>();
	private Optional<CharacterProfile> selected = Optional.empty();

	private int cw = 0;
	private int w = 0;
	private int h = 0;

	final double scaleFactor = 4;

	public CharacterSelector() {
		this(new ArrayList<>());
	}

	public CharacterSelector(Collection<CharacterProfile> characters) {
		super();
		this.getGraphicsContext2D().scale(1.0d/scaleFactor, 1.0d/scaleFactor);
		setCharacters(characters);
	}

	public void setCharacters(Collection<CharacterProfile> characters) {
		this.characters.clear();
		this.enabled.clear();
		this.selected = Optional.empty();

		this.characters.addAll(characters);
		for (CharacterProfile p : characters) this.enabled.add(true);

		if (characters.size() > 0) {
			SpriteAnimation a =
				this.characters.get(0).rootCharacter.sprite.defaultAnimation;
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
				SpriteAnimation anim =
					this.characters.get(i).rootCharacter.sprite.animations.get("idle");
				if (anim.hitTest(x, y, 0)) {
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
		GraphicsContext gx = this.getGraphicsContext2D();
		gx.clearRect(0, 0, w, h);
		for (int i = 0; i < characters.size(); i++) {
			renderCharacter(gx, characters.get(i), i);
		}
	}

	private void renderCharacter(GraphicsContext gx, CharacterProfile p, int i) {
		if (!enabled.get(i)) return;

		final SpriteAnimation a = p.rootCharacter.sprite.animations.get(
			selected.map(c -> {
				if (c.rootCharacter.name.equals(p.rootCharacter.name))
					return "selected"; else return "idle";}).orElse("idle"));

		a.renderFrame(gx, i * cw, h - (int) GlobalConstants.TILEH,
			0, CameraAngle.UL, FacingDirection.DOWN);

	}

	public Optional<CharacterProfile> getSelectedCharacter() {
		return selected;
	}

	public void setCharacterEnabled(CharacterProfile character, boolean enabled) {
	}

}

