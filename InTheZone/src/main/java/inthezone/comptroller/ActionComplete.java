package inthezone.comptroller;

import inthezone.battle.BattleState;
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
	private final BattleState battle;

	public ActionComplete(BattleState battle, List<MapPoint> completion) {
		super(Optional.empty());
		this.battle = battle;
		this.completion = completion;
	}

	void completeCommand(InstantEffectCommand cmd) throws CommandException {
		cmd.complete(battle, completion);
	}
}

