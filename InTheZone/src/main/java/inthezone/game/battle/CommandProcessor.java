package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.commands.PushCommand;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.UseAbilityCommand;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.PullPush;
import inthezone.battle.instant.Teleport;
import inthezone.battle.Targetable;
import inthezone.battle.Zone;
import isogame.engine.MapPoint;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Schedule animations in response to commands from the battle layer.
 * */
public class CommandProcessor {
	private final static double walkSpeed = 1.2;
	private final static double pushSpeed = 2;

	private final Queue<ExecutedCommand> commandQueue = new LinkedList<>();
	public final Map<Integer, Character> characters = new HashMap<>();
	private final Map<MapPoint, Sprite> temporaryImmobileObjects = new HashMap<>();
	public final Collection<MapPoint> zones = new HashSet<>();

	private final BattleView view;

	public CommandProcessor(BattleView view) {
		this.view = view;
	}

	public boolean isEmpty() {
		return commandQueue.isEmpty();
	}

	public Optional<Character> getCharacterAt(MapPoint p) {
		return characters.values().stream()
			.filter(c -> c.getPos().equals(p)).findFirst();
	}

	/**
	 * Put a command on the queue.
	 * */
	public void queueCommand(ExecutedCommand command) {
		commandQueue.add(command);
	}

	/**
	 * Execute the next command on the queue.
	 * @return true if an animation was started, otherwise false
	 * */
	public boolean doNextCommand() {
		ExecutedCommand ec = commandQueue.poll();
		if (ec == null) return false;

		final boolean registeredAnimations;

		if (ec.cmd instanceof UseAbilityCommand && !view.isMyTurn.getValue()) {
			UseAbilityCommand ua = (UseAbilityCommand) ec.cmd;
			Targetable agent = ec.affected.get(0);
			if (agent instanceof Character) {
				view.hud.writeMessage(((Character) agent).name + " uses " + ua.ability + "!");
			} else {
				view.hud.writeMessage("It's a trap!");
			}
		}

		if (ec.cmd instanceof MoveCommand) {
			List<MapPoint> path = ((MoveCommand) ec.cmd).path;
			if (path.size() < 2) return false;

			Character agent = (Character) ec.affected.get(0);
			scheduleMovement("walk", walkSpeed, path, agent);
			registeredAnimations = true;

		} else if (ec.cmd instanceof PushCommand) {
			// The first element in the affected characters list for a push command
			// is the agent of the push.  This element must be removed before
			// proceeding to the processing of the push effect.
			registeredAnimations = instantEffect(((PushCommand) ec.cmd).effect,
				ec.affected.subList(1, ec.affected.size()));

		} else if (ec.cmd instanceof InstantEffectCommand) {
			registeredAnimations = instantEffect(((InstantEffectCommand) ec.cmd).getEffect(), ec.affected);

		} else if (ec.cmd instanceof EndTurnCommand) {
			view.isMyTurn.setValue(!view.isMyTurn.getValue());
			registeredAnimations = false;

		} else if (ec.cmd instanceof ResignCommand) {
			if (((ResignCommand) ec.cmd).player != view.player) {
				Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
				a.setHeaderText("Opponent resigns");
				a.showAndWait();
			}
			registeredAnimations = false;

		} else if (ec.cmd instanceof UseAbilityCommand && view.isMyTurn.getValue()) {
			view.getMode().updateAffected(ec.affected);
			registeredAnimations = false;

		} else {
			registeredAnimations = false;
		}

		updateCharacters(ec.affected);
		return registeredAnimations;
	}

	/**
	 * @return true if a new animation was started.
	 * */
	private boolean instantEffect(
		InstantEffect effect, List<Targetable> affectedCharacters
	) {
		Stage stage = view.getStage();

		if (effect instanceof PullPush) {
			PullPush pullpush = (PullPush) effect;
			if (pullpush.paths.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid pull or push, this cannot happen");
			}

			for (int i = 0; i < pullpush.paths.size(); i++) {
				scheduleMovement("idle", pushSpeed,
					pullpush.paths.get(i), (Character) affectedCharacters.get(i));
			}
			return true;

		} else if (effect instanceof Teleport) {
			Teleport teleport = (Teleport) effect;
			List<MapPoint> destinations = teleport.getDestinations();
			if (destinations == null || destinations.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid teleport, this cannot happen");
			}

			for (int i = 0; i < destinations.size(); i++) {
				int id = ((Character) affectedCharacters.get(i)).id;
				MapPoint tile = characters.get(id).getPos();
				Sprite s = stage.getSpritesByTile(tile).stream()
					.filter(x -> x.userData.equals(id)).findFirst().get();
				stage.queueTeleportSprite(s, destinations.get(i));
			}
			return true;

		} else {
			return false;
		}
	}

	private void scheduleMovement(
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
	}

	public void updateCharacters(List<? extends Targetable> characters) {
		if (this.characters.isEmpty()) {
			for (Targetable c : characters) {
				if (c instanceof Character)
					this.characters.put(((Character) c).id, (Character) c);
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
						CharacterInfoBox box = view.hud.characters.get(c.id);
						if (box != null) {
							box.updateAP(c.getAP(), c.getStats().ap);
							box.updateMP(c.getMP(), c.getStats().mp);
							box.updateHP(c.getHP(), c.getMaxHP());
							box.updateStatus(c.getStatusBuff(), c.getStatusDebuff());
						}

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
			}
		}
	}
}

