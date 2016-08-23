package inthezone.battle.commands;

import inthezone.protocol.ProtocolException;

/**
 * All the kinds of commands there are, except for the StartBattleCommand,
 * which behaves a little differently to the others.
 * */
public enum CommandKind {
	ATTACK, ENDTURN, MOVE, PUSH, ABILITY, ITEM;

	@Override
	public String toString() {
		switch (this) {
			case ATTACK: return "Attack";
			case ENDTURN: return "End";
			case MOVE: return "Move";
			case PUSH: return "Push";
			case ABILITY: return "Ability";
			case ITEM: return "Item";
			default: throw new RuntimeException("This cannot happen");
		}
	}

	public static CommandKind fromString(String s) throws ProtocolException {
		if (s.equals("Attack")) {
			return CommandKind.ATTACK;
		} else if (s.equals("End")) {
			return CommandKind.ENDTURN;
		} else if (s.equals("Move")) {
			return CommandKind.MOVE;
		} else if (s.equals("Push")) {
			return CommandKind.PUSH;
		} else if (s.equals("Ability")) {
			return CommandKind.ABILITY;
		} else if (s.equals("Item")) {
			return CommandKind.ITEM;
		} else {
			throw new ProtocolException("Unrecognised command kind " + s);
		}
	}
}

