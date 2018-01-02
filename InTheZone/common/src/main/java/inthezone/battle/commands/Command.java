package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import ssjsjs.JSONable;

public abstract class Command implements JSONable {
	/**
	 * Do a command.
	 * @return All the characters that were affected by the command
	 * */
	public abstract List<? extends Targetable> doCmd(
		final Battle turn) throws CommandException;

	/**
	 * Do a command, computing trap and zone triggers
	 * @param turn The state of the battle
	 * @return A new set of commands, including all the trap and zone effects.
	 * */
	public List<ExecutedCommand> doCmdComputingTriggers(final Battle turn)
		throws CommandException
	{
		final List<ExecutedCommand> r = new ArrayList<>();
		r.add(new ExecutedCommand(this, doCmd(turn)));
		return r;
	}

	protected boolean canCancel = true;

	public boolean canCancel() {
		return this.canCancel;
	}

	/**
	 * Parse a command.
	 * */
	public static Command fromJSON(final JSONObject json)
		throws ProtocolException
	{
		final Object okind = json.get("kind");
		if (okind == null) throw new ProtocolException("Missing command kind");
		switch (CommandKind.valueOf((String) okind)) {
			case ENDTURN: return EndTurnCommand.fromJSON(json);
			case STARTTURN: throw new RuntimeException("Cannot receive start turn command");
			case MOVE: return MoveCommand.fromJSON(json);
			case PUSH: return PushCommand.fromJSON(json);
			case ABILITY: return UseAbilityCommand.fromJSON(json);
			case INSTANT: return InstantEffectCommand.fromJSON(json);
			case ITEM: return UseItemCommand.fromJSON(json);
			case RESIGN: return ResignCommand.fromJSON(json);
			case FATIGUE: return FatigueCommand.fromJSON(json);
			case NULL: return NullCommand.fromJSON(json);
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

