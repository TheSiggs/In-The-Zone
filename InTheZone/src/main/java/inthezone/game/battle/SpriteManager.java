package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.RoadBlock;
import inthezone.battle.Targetable;
import inthezone.battle.Zone;
import isogame.engine.AnimationChain;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A class to keep track of all the sprites.
 * */
public class SpriteManager {
	private final BattleView view;

	private final Map<Integer, Character> characters = new HashMap<>();
	private final Map<MapPoint, Sprite> temporaryImmobileObjects = new HashMap<>();
	public final Collection<MapPoint> zones = new HashSet<>();
	private final Collection<Sprite> sprites = new ArrayList<>();

	private int spritesInMotion = 0;

	private final DecalRenderer decals;

	public SpriteManager(
		BattleView view, Collection<Sprite> sprites,
		DecalRenderer decals, Runnable onAnimationsFinished
	) {
		this.view = view;
		this.sprites.addAll(sprites);
		this.decals = decals;

		for (Sprite s : this.sprites) {
			Stage stage = view.getStage();
			stage.addSprite(s);
			s.setDecalRenderer(decals);

			AnimationChain chain = new AnimationChain(s);
			stage.registerAnimationChain(chain);
			chain.doOnFinished(() -> {
				s.setAnimation("idle");

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
	public Character getCharacterById(int id) {return characters.get(id);}

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
	public void scheduleTeleport(Character a, MapPoint t) {
		Stage stage = view.getStage();

		int id = a.id;
		Sprite s = stage.getSpritesByTile(a.getPos()).stream()
			.filter(x -> x.userData != null && x.userData.equals(id)).findFirst().get();

		stage.queueTeleportSprite(s, t);
		spritesInMotion += 1;
	}

	/**
	 * Schedule a movement operation.
	 * */
	public void scheduleMovement(
		String animation, double speed, List<MapPoint> path, Character affected
	) {
		Stage stage = view.getStage();
		MapPoint start = path.get(0);
		MapPoint end = path.get(1);
		MapPoint v = end.subtract(start);

		int id = affected.id;
		Sprite s = stage.getSpritesByTile(start).stream()
			.filter(x -> x.userData != null && x.userData.equals(id)).findFirst().get();

		for (MapPoint p : path.subList(2, path.size())) {
			if (!end.add(v).equals(p)) {
				stage.queueMoveSprite(s, start, end, animation, speed);
				start = end;
				v = p.subtract(start);
			}
			end = p;
		}

		stage.queueMoveSprite(s, start, end, animation, speed);
		spritesInMotion += 1;
	}

	/**
	 * Update the character models.
	 * */
	public void updateCharacters(List<? extends Targetable> characters) {
		if (this.characters.isEmpty()) {
			for (Targetable t : characters) {
				if (t instanceof Character) {
					Character c = (Character) t;
					this.characters.put(c.id, c);
					decals.registerCharacter(c);
				}
			}
			view.hud.init(this.characters.values().stream()
				.filter(c -> c.player == view.player).collect(Collectors.toList()));

		} else {
			for (Targetable t : characters) {
				if (t instanceof Character) {
					Character c = (Character) t;
					Character old = this.characters.get(c.id);
					view.updateSelectedCharacter(c);

					if (old != null) {
						if (c.player == view.player) view.hud.updateAbilities(c, c.hasMana());
						final CharacterInfoBox box = view.hud.characters.get(c.id);

						if (box != null) box.updateCharacter(c);
						decals.updateCharacter(c);
					}

					if (c.isDead()) {
						Sprite s = view.getStage().getSpritesByTile(c.getPos()).stream()
							.filter(x -> x.userData != null && x.userData.equals(c.id)).findFirst().get();
						s.setAnimation("dead");
					}

					this.characters.put(c.id, c);
				}
			}
		}

		handleTemporaryImmobileObjects(characters);
	}

	private void handleTemporaryImmobileObjects(Collection<? extends Targetable> tios) {
		for (Targetable t : tios) {
			if (t instanceof Character) {
				continue;

			} else if (t instanceof Zone) {
				if (t.reap()) {
					zones.removeAll(((Zone) t).range);
					view.resetHighlighting();

				} else {
					zones.addAll(((Zone) t).range);
					view.resetHighlighting();
				}

			} else if (t.reap()) {
				Sprite s = temporaryImmobileObjects.remove(t.getPos());
				if (s != null) view.getStage().removeSprite(s);

			} else if (!temporaryImmobileObjects.containsKey(t.getPos())) {
				Sprite s = new Sprite(t.getSprite());
				s.pos = t.getPos();
				view.getStage().addSprite(s);
				temporaryImmobileObjects.put(t.getPos(), s);

			} else if (t instanceof RoadBlock) {
				Sprite s = temporaryImmobileObjects.get(t.getPos());
				if (s != null && ((RoadBlock) t).hits > 0) {
					s.setAnimation("hit");
				}
			}
		}
	}

}

