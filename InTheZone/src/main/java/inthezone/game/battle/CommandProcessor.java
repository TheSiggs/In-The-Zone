package inthezone.game.battle;

import inthezone.battle.Character;
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
import isogame.engine.MapPoint;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Schedule animations in response to commands from the battle layer.
 * */
public class CommandProcessor {
	private final static double walkSpeed = 1.2;
	private final static double pushSpeed = 2;

	private final Queue<ExecutedCommand> commandQueue = new LinkedList<>();
	private final BattleView view;

	public CommandProcessor(BattleView view) {
		this.view = view;
	}

	public boolean isEmpty() {
		return commandQueue.isEmpty();
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
			view.sprites.scheduleMovement("walk", walkSpeed, path, agent);
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

		view.sprites.updateCharacters(ec.affected);
		return registeredAnimations;
	}

	/**
	 * @return true if a new animation was started.
	 * */
	private boolean instantEffect(
		InstantEffect effect, List<Targetable> affectedCharacters
	) {
		if (effect instanceof PullPush) {
			PullPush pullpush = (PullPush) effect;
			if (pullpush.paths.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid pull or push, this cannot happen");
			}

			for (int i = 0; i < pullpush.paths.size(); i++) {
				view.sprites.scheduleMovement("idle", pushSpeed,
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
				view.sprites.scheduleTeleport(
					view.sprites.getCharacterById(id), destinations.get(i));
			}
			return true;

		} else {
			return false;
		}
	}

}

