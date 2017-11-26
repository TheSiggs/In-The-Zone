package inthezone.game.battle;

import isogame.engine.MapPoint;
import isogame.engine.MoveSpriteAnimation;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import isogame.engine.TeleportAnimation;
import isogame.engine.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.Tooltip;

import inthezone.battle.CharacterFrozen;
import inthezone.battle.RoadBlockFrozen;
import inthezone.battle.TargetableFrozen;
import inthezone.battle.TrapFrozen;
import inthezone.battle.ZoneFrozen;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.Player;
import inthezone.battle.data.StandardSprites;
import inthezone.battle.data.StatusEffectDescription;

/**
 * A class to keep track of all the sprites.
 * */
public class SpriteManager {
	private final BattleView view;

	public final Map<Integer, CharacterFrozen> characters = new HashMap<>();

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

	/**
	 * @param view a reference back to the BattleView
	 * @param sprites the initial set of sprites to manage
	 * @param standardSprites the standard sprites (icons etc.)
	 * @param onAnimationsFinished a handler for when all the animations in a
	 * series are finished
	 * */
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
				final CharacterFrozen c = characters.get((Integer) s.userData);
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
	 * @param id the id of the character to get.
	 * */
	public CharacterFrozen getCharacterById(final int id) {
		return characters.get(id);
	}

	/**
	 * Get a character by its position.
	 * @param p the location of the character to get
	 * */
	public Optional<CharacterFrozen> getCharacterAt(final MapPoint p) {
		return characters.values().stream()
			.filter(c -> c.getPos().equals(p)).findFirst();
	}

	/**
	 * Schedule a teleport operation.
	 * @param a the character to teleport
	 * @param t the teleport target square
	 * */
	public void scheduleTeleport(final CharacterFrozen a, final MapPoint t) {
		final int id = a.getId();
		final Sprite s = view.getStage().allSprites.stream()
			.filter(x -> x.userData != null && x.userData.equals(id))
			.findFirst().get();

		s.queueExternalAnimation(new TeleportAnimation(s, t));
		spritesInMotion += 1;
	}

	/**
	 * Schedule a movement operation.
	 * @param animation the name of the animation to play
	 * @param speed the speed of the animation
	 * @param path the path to follow
	 * @param affected the character to move
	 * */
	public void scheduleMovement(
		final String animation,
		final double speed,
		final List<MapPoint> path,
		final CharacterFrozen affected
	) {
		final Stage stage = view.getStage();
		MapPoint start = path.get(0);
		MapPoint end = path.get(1);

		final int id = affected.getId();
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

	/**
	 * Update the selection status in the UI.
	 * */
	public void updateSelectionStatus() {
		final int selectedId =
			view.getSelectedCharacter().map(c -> c.getId()).orElse(-1);
		for (final Integer i : decals.keySet()) {
			decals.get(i).updateSelectionStatus(i == selectedId);
		}
	}

	/**
	 * Create a tooltip for a character
	 * @param c the character to get the tooltip for
	 * */
	private Tooltip characterTooltip(final CharacterFrozen c) {
		final StringBuilder out = new StringBuilder();
		out.append(c.getName());
		if (c.getPlayer() != view.player) out.append(" (enemy)");

		if (c.getStatusBuff().isPresent() || c.getStatusDebuff().isPresent()) {
			out.append("\n\n");
		}

		c.getStatusBuff().ifPresent(s ->
			out.append(new StatusEffectDescription(s.getInfo()).toString()));
		if (c.getStatusBuff().isPresent() && c.getStatusDebuff().isPresent()) {
			out.append("\n\n");
		}
		c.getStatusDebuff().ifPresent(s ->
			out.append(new StatusEffectDescription(s.getInfo()).toString()));

		final Tooltip r = new Tooltip(out.toString());
		r.setWrapText(true);
		r.setMaxWidth(300);

		return r;
	}

	/**
	 * Update the character models.
	 * @param characters the characters to update
	 * */
	public void updateCharacters(
		final List<? extends TargetableFrozen> characters
	) {
		if (this.characters.isEmpty()) {
			for (final TargetableFrozen t : characters) {
				if (t instanceof CharacterFrozen) {
					final CharacterFrozen c = (CharacterFrozen) t;
					final boolean isSelected = view.getSelectedCharacter()
						.map(s -> s.getId() == c.getId()).orElse(false);

					this.characters.put(c.getId(), c);
					final DecalPanel panel = new DecalPanel(standardSprites);
					decals.put(c.getId(), panel);
					panel.updateCharacter(c, isSelected);

					final Sprite characterSprite = view.getStage().allSprites.stream()
						.filter(x -> x.userData != null &&
							x.userData.equals(c.getId())).findFirst().get();

					Tooltip.install(
						characterSprite.sceneGraphNode, characterTooltip(c));
					characterSprite.sceneGraph.getChildren().add(panel);
				}
			}
			view.hud.init(this.characters.values());

		} else {
			for (final TargetableFrozen t : characters) {
				if (t instanceof CharacterFrozen) {
					final CharacterFrozen c = (CharacterFrozen) t;
					final CharacterFrozen old = this.characters.get(c.getId());
					view.updateSelectedCharacter(c);

					if (old != null) {
						final CharacterInfoBox box = view.hud.characters.get(c.getId());
						if (box != null) box.updateCharacter(c);

						final boolean isSelected = view.getSelectedCharacter()
							.map(s -> s.getId() == c.getId()).orElse(false);
						decals.get(c.getId()).updateCharacter(c, isSelected);

						final Sprite characterSprite = view.getStage().allSprites.stream()
							.filter(x -> x.userData != null &&
								x.userData.equals(c.getId())).findFirst().get();

						if (c.isDead()) {
							characterSprite.setAnimation("dead");
						} else if (!c.isDead() && old.isDead()) {
							characterSprite.setAnimation("idle");
						}

						Tooltip.install(
							characterSprite.sceneGraphNode, characterTooltip(c));
					}

					this.characters.put(c.getId(), c);
				}
			}
		}

		handleTemporaryImmobileObjects(characters);
	}

	/**
	 * Get a tooltip for a mana zone.
	 * */
	public static Tooltip getManaZoneTooltip() {
		final Tooltip tt = new Tooltip("Mana zone." +
			"  Put your characters on mana zones for an extra power boost");
		tt.setWrapText(true);
		tt.setMaxWidth(300);
		return tt;
	}

	/**
	 * Handle other battle objects that are not characters.
	 * */
	private void handleTemporaryImmobileObjects(
		final Collection<? extends TargetableFrozen> tios
	) {
		for (final TargetableFrozen t : tios) {
			if (t instanceof CharacterFrozen) {
				continue;

			} else if (t instanceof ZoneFrozen) {
				final ZoneFrozen z = (ZoneFrozen) t;
				final Stage stage = view.getStage();

				if (z.reap()) {
					for (final MapPoint p : z.getRange()) {
						final Sprite s = zones.remove(p);
						if (s != null) stage.removeSprite(s);
						final Tile tile = stage.terrain.getTile(p);
						final Object tt = tile.userData;
						if (tt != null) {
							if (tile.isManaZone) {
								Tooltip.install(tile.subGraph, getManaZoneTooltip());
							} else {
								Tooltip.uninstall(tile.subGraph, (Tooltip) tt);
							}
						}
					}

				} else if (!zones.containsKey(z.getCentre())) {
					for (final MapPoint p : z.getRange()) {
						final Sprite s = new Sprite(z.getSprite());
						s.setPos(p);
						zones.put(p, s);
						stage.addSprite(s);

						final Tile tile = stage.terrain.getTile(p);
						final Object tt = tile.userData;
						if (tt != null) Tooltip.uninstall(tile.subGraph, (Tooltip) tt);
						final boolean isMine = view.player == Player.PLAYER_OBSERVER ||
							z.getParent().getPlayer().equals(view.player);
						final Tooltip tt1 = new Tooltip(
							new AbilityDescription(z.getAbility().info)
								.getCreatedObjectDescription(isMine));

						tt1.setWrapText(true);
						tt1.setMaxWidth(300);

						Tooltip.install(tile.subGraph, tt1);
						tile.userData = tt1;
					}
				}

			} else if (t instanceof RoadBlockFrozen) {
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
					if (s != null && ((RoadBlockFrozen) t).hasBeenHit()) {
						s.setAnimation("hit");
						Tooltip.install(s.sceneGraphNode, destructibleObjectTooltip2);
					} else {
						Tooltip.install(s.sceneGraphNode, destructibleObjectTooltip);
					}
				}

			} else if (t instanceof TrapFrozen) {
				if (t.reap()) {
					final Sprite s = traps.remove(t.getPos());
					if (s != null) view.getStage().removeSprite(s);

				} else if (!traps.containsKey(t.getPos())) {
					final Sprite s;
					final Tooltip tooltip;
					if (view.player == Player.PLAYER_OBSERVER ||
						((TrapFrozen) t).getParent().getPlayer().equals(view.player)
					) {
						tooltip = new Tooltip(
							(new AbilityDescription(((TrapFrozen) t).getAbility().info))
								.getCreatedObjectDescription(true));
						tooltip.setWrapText(true);
						tooltip.setMaxWidth(300);
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

