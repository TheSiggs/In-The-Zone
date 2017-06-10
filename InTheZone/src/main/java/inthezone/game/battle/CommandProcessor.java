package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.EndTurnCommand;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.FatigueCommand;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.commands.PushCommand;
import inthezone.battle.commands.StartTurnCommand;
import inthezone.battle.commands.UseAbilityCommand;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.Move;
import inthezone.battle.instant.PullPush;
import inthezone.battle.instant.Teleport;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

/**
 * Schedule animations in response to commands from the battle layer.
 * */
public class CommandProcessor {
	private final static double walkSpeed = 1.2;
	private final static double pushSpeed = 2;

	private final Queue<ExecutedCommand> commandQueue = new LinkedList<>();
	private final BattleView view;

	private boolean isComplete = false;

	public CommandProcessor(BattleView view) {
		this.view = view;
	}

	public boolean isEmpty() {
		return commandQueue.isEmpty();
	}

	public boolean isComplete() {return isComplete;}

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
		final ExecutedCommand ec = commandQueue.poll();
		if (ec == null) return false;

		final boolean registeredAnimations;

		if (ec.cmd instanceof UseAbilityCommand && !view.isMyTurn.getValue()) {
			switch (((UseAbilityCommand) ec.cmd).agentType) {
				case CHARACTER:
					final UseAbilityCommand ua = (UseAbilityCommand) ec.cmd;
					final Targetable agent = ec.affected.get(0);
					final String name = ((Character) agent).name;

					if (ua.placedTraps) view.hud.writeMessage(name + " sets a trap!");
					else if (ua.placedZones) view.hud.writeMessage(name + " places a zone!");
					else view.hud.writeMessage(name + " uses " + ua.ability + "!");

					break;
				case ZONE:
					view.hud.writeMessage("A zone is triggered!");
					break;
				case TRAP:
					view.hud.writeMessage("A trap is triggered!");
					break;
			}
		}

		if (ec.cmd instanceof MoveCommand) {
			List<MapPoint> path = ((MoveCommand) ec.cmd).path;
			if (path.size() < 2) throw new RuntimeException("Invalid move path");

			Character agent = (Character) ec.affected.get(0);
			view.sprites.scheduleMovement("walk", walkSpeed, path, agent);
			registeredAnimations = true;

		} else if (ec.cmd instanceof PushCommand) {
			if (ec.affected.isEmpty()) {
				registeredAnimations = false;
			} else {
				// The first element in the affected characters list for a push command
				// is the agent of the push.  This element must be removed before
				// proceeding to the processing of the push effect.
				registeredAnimations = instantEffect(((PushCommand) ec.cmd).effect,
					ec.affected.subList(1, ec.affected.size()));
			}

		} else if (ec.cmd instanceof InstantEffectCommand) {
			registeredAnimations = instantEffect(((InstantEffectCommand) ec.cmd).getEffect(), ec.affected);

		} else if (ec.cmd instanceof EndTurnCommand) {
			EndTurnCommand cmd = (EndTurnCommand) ec.cmd;

			if (cmd.player == view.player) {
				view.isMyTurn.setValue(false);
				view.setMode(new ModeOtherTurn(view));
			} else {
				view.isMyTurn.setValue(true);
			}
			registeredAnimations = false;

		} else if (ec.cmd instanceof FatigueCommand) {
			view.hud.notifyFatigue();
			registeredAnimations = false;

		} else if (ec.cmd instanceof StartTurnCommand) {
			StartTurnCommand cmd = (StartTurnCommand) ec.cmd;
			view.hud.notifyRound();

			if (cmd.player == view.player) {
				view.isMyTurn.setValue(true);
				view.setMode(new ModeAnimating(view));
				view.hud.writeMessage("Your turn");
				registeredAnimations = false;
			} else {
				view.isMyTurn.setValue(false);
				view.hud.writeMessage(view.otherPlayerName + "'s turn");
				registeredAnimations = false;
			}

		} else if (ec.cmd instanceof UseAbilityCommand && view.isMyTurn.getValue()) {
			registeredAnimations = false;

		} else {
			registeredAnimations = false;
		}

		view.sprites.updateCharacters(ec.affected);
		isComplete = ec.lastInSequence;
		return registeredAnimations;
	}

	/**
	 * @return true if a new animation was started.
	 * */
	private boolean instantEffect(
		InstantEffect effect, List<Targetable> affectedCharacters
	) {
		view.retargetMode(effect.getRetargeting());
		if (effect instanceof PullPush) {
			PullPush pullpush = (PullPush) effect;

			if (pullpush.paths.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid pull or push, this cannot happen");
			}

			for (int i = 0; i < pullpush.paths.size(); i++) {
				final Character c = (Character) affectedCharacters.get(i);
				view.sprites.scheduleMovement(c.isDead()? "dead" : "idle",
					pushSpeed, pullpush.paths.get(i), c);
			}

			return !pullpush.paths.isEmpty();

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
			return !destinations.isEmpty();

		} else if (effect instanceof Move) {
			Move move = (Move) effect;

			if (move.paths.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid move effect, this cannot happen");
			}

			for (int i = 0; i < move.paths.size(); i++) {
				view.sprites.scheduleMovement("walk", walkSpeed,
					move.paths.get(i), (Character) affectedCharacters.get(i));
			}
			return !move.paths.isEmpty();

		} else {
			return false;
		}
	}
}

