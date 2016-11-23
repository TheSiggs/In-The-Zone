package inthezone.comptroller;

import inthezone.battle.Ability;
import inthezone.battle.Battle;
import inthezone.battle.commands.CommandRequest;
import java.util.Optional;

/**
 * The superclass of all battle controller actions.
 * */
public class Action {
	private final Optional<CommandRequest> crq;

	public Action(Optional<CommandRequest> crq) {
		this.crq = crq;
	}

	/**
	 * Get the command request, if there is one.
	 * */
	public Optional<CommandRequest> getCommandRequest() {return crq;}

	/**
	 * Complete the action.
	 * */
	public void completeAction(Battle battle) {}
}

