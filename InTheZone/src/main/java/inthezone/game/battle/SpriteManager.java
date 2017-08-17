package inthezone.game.battle;

import isogame.engine.MapPoint;
import isogame.engine.MoveSpriteAnimation;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import isogame.engine.TeleportAnimation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.Tooltip;

import inthezone.battle.Character;
import inthezone.battle.RoadBlock;
import inthezone.battle.Targetable;
import inthezone.battle.Trap;
import inthezone.battle.Zone;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;

/**
 * A class to keep track of all the sprites.
 * */
public class SpriteManager {
	private final BattleView view;

	public final Map<Integer, Character> characters = new HashMap<>();

	private final Map<MapPoint, Sprite> traps = new HashMap<>();

	private final Map<MapPoint, Sprite> roadblocks = new HashMap<>();

	public final Map<MapPoint, Sprite> zones = new HashMap<>();

	private final Collection<Sprite> sprites = new ArrayList<>();

	private int spritesInMotion = 0;

	private final Map<Integer, DecalPanel> decals = new HashMap<>();

	private final StandardSprites standardSprites;

	private final Tooltip destructibleObjectTooltip =
		new Tooltip("A destructible obstacle, can be destroyed in 2 hits");

	private final Tooltip destructibleObjectTooltip2 =
		new Tooltip("A destructible obstacle, can be destroyed in 1 hit");

	public SpriteManager(
		final BattleView view, final Collection<Sprite> sprites,
		final StandardSprites standardSprites,
		final Runnable onAnimationsFinished
	) {
		this.view = view;
		this.sprites.addAll(sprites);
		this.standardSprites = standardSprites;

		for (final Sprite s : this.sprites) {
			view.getStage().addSprite(s);

			s.doOnExternalAnimationFinished(() -> {
				final Character c = characters.get((Integer) s.userData);
				s.setAnimation(c.isDead()? "dead" : "idle");

				spritesInMotion -= 1;
				if (spritesInMotion < 0) {
					throw new RuntimeException(
						"Invalid UI state.  Untracked animation detected");
				} else if (spritesInMotion == 0) {
					spritesInMotion = 0;
					onAnimationsFinished.run();
				}
			});
		}
	}

	/**
	 * Get a character by its id.
	 * */
	public Character getCharacterById(final int id) {return characters.get(id);}

	/**
	 * Get a character by its position.
	 * */
	public Optional<Character> getCharacterAt(final MapPoint p) {
		return characters.values().stream()
			.filter(c -> c.getPos().equals(p)).findFirst();
	}

	/**
	 * Schedule a teleport operation.
	 * */
	public void scheduleTeleport(final Character a, final MapPoint t) {
		final int id = a.id;
		final Sprite s = view.getStage().allSprites.stream()
			.filter(x -> x.userData != null && x.userData.equals(id))
			.findFirst().get();

		s.queueExternalAnimation(new TeleportAnimation(s, t));
		spritesInMotion += 1;
	}

	/**
	 * Schedule a movement operation.
	 * */
	public void scheduleMovement(
		final String animation,
		final double speed,
		final List<MapPoint> path,
		final Character affected
	) {
		final Stage stage = view.getStage();
		MapPoint start = path.get(0);
		MapPoint end = path.get(1);

		final int id = affected.id;
		final Sprite s = stage.allSprites.stream()
			.filter(x -> x.userData != null && x.userData.equals(id))
			.findFirst().get();

		for (final MapPoint p : path.subList(2, path.size())) {
			s.queueExternalAnimation(new MoveSpriteAnimation(
				start, end, s, animation, speed, stage.terrain));
			start = end;
			end = p;
		}

		s.queueExternalAnimation(new MoveSpriteAnimation(
			start, end, s, animation, speed, stage.terrain));
		spritesInMotion += 1;
	}

	public void updateSelectionStatus() {
		final int selectedId =
			view.getSelectedCharacter().map(c -> c.id).orElse(-1);
		for (final Integer i : decals.keySet()) {
			decals.get(i).updateSelectionStatus(i == selectedId);
		}
	}

	/**
	 * Update the character models.
	 * */
	public void updateCharacters(final List<? extends Targetable> characters) {
		if (this.characters.isEmpty()) {
			for (final Targetable t : characters) {
				if (t instanceof Character) {
					final Character c = (Character) t;
					final boolean isSelected = view.getSelectedCharacter()
						.map(s -> s.id == c.id).orElse(false);

					this.characters.put(c.id, c);
					final DecalPanel panel = new DecalPanel(standardSprites);
					decals.put(c.id, panel);
					panel.updateCharacter(c, isSelected);

					final Sprite characterSprite = view.getStage().allSprites.stream()
						.filter(x -> x.userData != null &&
							x.userData.equals(c.id)).findFirst().get();

					characterSprite.sceneGraph.getChildren().add(panel);
				}
			}
			view.hud.init(this.characters.values());

		} else {
			for (final Targetable t : characters) {
				if (t instanceof Character) {
					final Character c = (Character) t;
					final Character old = this.characters.get(c.id);
					view.updateSelectedCharacter(c);

					if (old != null) {
						final CharacterInfoBox box = view.hud.characters.get(c.id);
						if (box != null) box.updateCharacter(c);

						final boolean isSelected = view.getSelectedCharacter()
							.map(s -> s.id == c.id).orElse(false);
						decals.get(c.id).updateCharacter(c, isSelected);

						final Sprite characterSprite = view.getStage().allSprites.stream()
							.filter(x -> x.userData != null &&
								x.userData.equals(c.id)).findFirst().get();

						if (c.isDead()) {
							characterSprite.setAnimation("dead");
						} else if (!c.isDead() && old.isDead()) {
							characterSprite.setAnimation("idle");
						}
					}


					this.characters.put(c.id, c);
				}
			}
		}

		handleTemporaryImmobileObjects(characters);
	}

	private void handleTemporaryImmobileObjects(
		final Collection<? extends Targetable> tios
	) {
		for (final Targetable t : tios) {
			if (t instanceof Character) {
				continue;

			} else if (t instanceof Zone) {
				final Zone z = (Zone) t;
				final Stage stage = view.getStage();

				if (z.reap()) {
					for (final MapPoint p : z.range) {
						final Sprite s = zones.remove(p);
						if (s != null) stage.removeSprite(s);
					}

				} else if (!zones.containsKey(z.centre)) {
					for (final MapPoint p : z.range) {
						final Sprite s = new Sprite(z.getSprite());
						s.setPos(p);
						zones.put(p, s);
						stage.addSprite(s);
					}
				}

			} else if (t instanceof RoadBlock) {
				if (t.reap()) {
					final Sprite s = roadblocks.remove(t.getPos());
					if (s != null) view.getStage().removeSprite(s);

				} else if (!roadblocks.containsKey(t.getPos())) {
					final Sprite s = new Sprite(t.getSprite());
					Tooltip.install(s.sceneGraphNode, destructibleObjectTooltip);
					s.setPos(t.getPos());
					view.getStage().addSprite(s);
					roadblocks.put(t.getPos(), s);

				} else {
					final Sprite s = roadblocks.get(t.getPos());
					if (s != null && ((RoadBlock) t).hasBeenHit()) {
						s.setAnimation("hit");
						Tooltip.install(s.sceneGraphNode, destructibleObjectTooltip2);
					} else {
						Tooltip.install(s.sceneGraphNode, destructibleObjectTooltip);
					}
				}

			} else if (t instanceof Trap) {
				if (t.reap()) {
					final Sprite s = traps.remove(t.getPos());
					if (s != null) view.getStage().removeSprite(s);

				} else if (!traps.containsKey(t.getPos())) {
					final Sprite s;
					final Tooltip tooltip;
					if (view.player == Player.PLAYER_OBSERVER ||
						((Trap) t).parent.player.equals(view.player)
					) {
						tooltip = new Tooltip(
							(new AbilityDescription(((Trap) t).ability.info))
								.getCreatedObjectDescription(true));
						tooltip.setWrapText(true);
						tooltip.setPrefWidth(300);
						s = new Sprite(t.getSprite());
					} else {
						tooltip = new Tooltip("It's a trap!");
						s = new Sprite(standardSprites.trap);
					}

					Tooltip.install(s.sceneGraphNode, tooltip);

					s.setPos(t.getPos());
					view.getStage().addSprite(s);
					traps.put(t.getPos(), s);
				}

			}
		}
	}
}

