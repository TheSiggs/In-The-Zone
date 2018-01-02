package inthezone.battle.commands;

/**
 * All the kinds of commands there are, except for the StartBattleCommand,
 * which behaves a little differently to the others.
 * */
public enum CommandKind {
	ENDTURN, STARTTURN, MOVE, PUSH, ABILITY,
	INSTANT, ITEM, RESIGN, FATIGUE, NULL;
}

