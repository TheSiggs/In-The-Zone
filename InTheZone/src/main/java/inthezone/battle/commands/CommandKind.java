package inthezone.battle.commands;

import inthezone.protocol.ProtocolException;

/**
 * All the kinds of commands there are, except for the StartBattleCommand,
 * which behaves a little differently to the others.
 * */
public enum CommandKind {
	ATTACK, ENDTURN, MOVE, PUSH, ABILITY, ITEM, RESIGN;

	@Override
	public String toString() {
		switch (this) {
			case ATTACK: return "Attack";
			case ENDTURN: return "End";
			case MOVE: return "Move";
			case PUSH: return "Push";
			case ABILITY: return "Ability";
			case ITEM: return "Item";
			case RESIGN: return "Resign";
			default: throw new RuntimeException("This cannot happen");
		}
	}

	public static CommandKind fromString(String s) throws ProtocolException {
		switch (s) {
			case "Attack": return CommandKind.ATTACK;
			case "End": return CommandKind.ENDTURN;
			case "Move": return CommandKind.MOVE;
			case "Push": return CommandKind.PUSH;
			case "Ability": return CommandKind.ABILITY;
			case "Item": return CommandKind.ITEM;
			case "Resign": return CommandKind.RESIGN;
			default: throw new ProtocolException("Unrecognised command kind " + s);
		}
	}
}

