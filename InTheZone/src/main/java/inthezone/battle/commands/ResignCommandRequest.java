package inthezone.battle.commands;

import inthezone.battle.BattleState;
import inthezone.battle.data.Player;

public class ResignCommandRequest extends CommandRequest {
	private final Player player;

	public ResignCommandRequest(Player player) {
		this.player = player;
	}

	@Override
	public Command makeCommand(BattleState battleState) throws CommandException {
		return new ResignCommand(player);
	}
}


