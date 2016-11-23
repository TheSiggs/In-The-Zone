package inthezone.comptroller;

import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.InstantEffectCommand;
import isogame.engine.MapPoint;
import java.util.List;
import java.util.Optional;

/**
 * Complete a command.
 * */
public class ActionComplete extends Action {
	private final List<MapPoint> completion;

	public ActionComplete(List<MapPoint> completion) {
		super(Optional.empty());
		this.completion = completion;
	}

	void completeCommand(InstantEffectCommand cmd) throws CommandException {
		cmd.complete(completion);
	}
}

