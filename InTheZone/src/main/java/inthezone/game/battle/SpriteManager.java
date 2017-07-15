package inthezone.game.battle;

import isogame.engine.AnimationChain;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import isogame.engine.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import inthezone.battle.Character;
import inthezone.battle.RoadBlock;
import inthezone.battle.Targetable;
import inthezone.battle.Trap;
import inthezone.battle.Zone;
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

	private final DecalRenderer decals;

	private final StandardSprites standardSprites;

	public SpriteManager(
		final BattleView view, final Collection<Sprite> sprites,
		final StandardSprites standardSprites,
		final DecalRenderer decals, final Runnable onAnimationsFinished
	) {
		this.view = view;
		this.sprites.addAll(sprites);
		this.standardSprites = standardSprites;
		this.decals = decals;

		for (Sprite s : this.sprites) {
			final Stage stage = view.getStage();
			stage.addSprite(s);
			s.setDecalRenderer(decals);

			final AnimationChain chain = new AnimationChain(s);
			stage.registerAnimationChain(chain);
			chain.doOnFinished(() -> {
				final Character c = characters.get((Integer) s.userData);
				s.setAnimation(c.isDead()? "dead" : "idle");

				spritesInMotion -= 1;
				if (spritesInMotion < 0) {
					throw new RuntimeException("Invalid UI state.  Untracked animation detected");
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
	public Optional<Character> getCharacterAt(MapPoint p) {
		return characters.values().stream()
			.filter(c -> c.getPos().equals(p)).findFirst();
	}

	/**
	 * Schedule a teleport operation.
	 * */
	public void scheduleTeleport(final Character a, final MapPoint t) {
		final Stage stage = view.getStage();

		final int id = a.id;
		final Sprite s = stage.allSprites.stream()
			.filter(x -> x.userData != null && x.userData.equals(id)).findFirst().get();

		stage.queueTeleportSprite(s, t);
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
			.filter(x -> x.userData != null && x.userData.equals(id)).findFirst().get();

		for (MapPoint p : path.subList(2, path.size())) {
			stage.queueMoveSprite(s, start, end, animation, speed);
			start = end;
			end = p;
		}

		stage.queueMoveSprite(s, start, end, animation, speed);
		spritesInMotion += 1;
	}

	/**
	 * Update the character models.
	 * */
	public void updateCharacters(final List<? extends Targetable> characters) {
		if (this.characters.isEmpty()) {
			for (Targetable t : characters) {
				if (t instanceof Character) {
					final Character c = (Character) t;
					this.characters.put(c.id, c);
					decals.registerCharacter(c);
				}
			}
			view.hud.init(this.characters.values());

		} else {
			for (Targetable t : characters) {
				if (t instanceof Character) {
					final Character c = (Character) t;
					final Character old = this.characters.get(c.id);
					view.updateSelectedCharacter(c);

					if (old != null) {
						final CharacterInfoBox box = view.hud.characters.get(c.id);
						if (box != null) box.updateCharacter(c);
						decals.updateCharacter(c);

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
		for (Targetable t : tios) {
			if (t instanceof Character) {
				continue;

			} else if (t instanceof Zone) {
				final Zone z = (Zone) t;
				final Stage stage = view.getStage();

				if (z.reap()) {
					for (MapPoint p : z.range) {
						final Sprite s = zones.remove(p);
						if (s != null) stage.removeSprite(s);
					}

				} else if (!zones.containsKey(z.centre)) {
					for (MapPoint p : z.range) {
						final Sprite s = new Sprite(z.getSprite());
						s.pos = p;
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
					s.pos = t.getPos();
					view.getStage().addSprite(s);
					roadblocks.put(t.getPos(), s);

				} else {
					final Sprite s = roadblocks.get(t.getPos());
					if (s != null && ((RoadBlock) t).hasBeenHit()) {
						s.setAnimation("hit");
					}
				}

			} else if (t instanceof Trap) {
				if (t.reap()) {
					final Sprite s = traps.remove(t.getPos());
					if (s != null) view.getStage().removeSprite(s);

				} else if (!traps.containsKey(t.getPos())) {
					final Sprite s;
					if (view.player == Player.PLAYER_OBSERVER ||
						((Trap) t).parent.player.equals(view.player)
					) {
						s = new Sprite(t.getSprite());
					} else {
						s = new Sprite(standardSprites.trap);
					}

					s.pos = t.getPos();
					view.getStage().addSprite(s);
					traps.put(t.getPos(), s);
				}

			}
		}
	}
}

