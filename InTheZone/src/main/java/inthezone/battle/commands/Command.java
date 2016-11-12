package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.HasJSONRepresentation;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class Command implements HasJSONRepresentation {
	/**
	 * Do a command.
	 * @return All the characters that were affected by the command
	 * */
	public abstract List<Targetable> doCmd(Battle turn) throws CommandException;

	/**
	 * Do a command, computing trap and zone triggers
	 * @param turn The state of the battle
	 * @return A new set of commands, including all the trap and zone effects.
	 * */
	public List<ExecutedCommand> doCmdComputingTriggers(Battle turn)
		throws CommandException
	{
		List<ExecutedCommand> r = new ArrayList<>();
		r.add(new ExecutedCommand(this, doCmd(turn)));
		return r;
	}

	/**
	 * Parse a command.
	 * */
	public static Command fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		if (okind == null) throw new ProtocolException("Missing command kind");
		switch (CommandKind.fromString((String) okind)) {
			case ENDTURN: return EndTurnCommand.fromJSON(json);
			case MOVE: return MoveCommand.fromJSON(json);
			case PUSH: return PushCommand.fromJSON(json);
			case ABILITY: return UseAbilityCommand.fromJSON(json);
			case INSTANT: return InstantEffectCommand.fromJSON(json);
			case ITEM: return UseItemCommand.fromJSON(json);
			case RESIGN: return ResignCommand.fromJSON(json);
			case FATIGUE: return FatigueCommand.fromJSON(json);
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

