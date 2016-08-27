package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.protocol.ProtocolException;
import isogame.engine.HasJSONRepresentation;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class Command implements HasJSONRepresentation {
	/**
	 * Do a command.
	 * @return All the characters that were affected by the command
	 * */
	public abstract List<Character> doCmd(Battle turn) throws CommandException;

	/**
	 * Parse a command.
	 * */
	public static Command fromJSON(JSONObject json) throws ProtocolException {
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
			default: throw new RuntimeException("This cannot happen");
		}
	}
}

