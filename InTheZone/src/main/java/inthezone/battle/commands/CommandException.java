package inthezone.battle.commands;

public class CommandException extends Exception {
	public CommandException() {
		super();
	}

	public CommandException(String msg) {
		super(msg);
	}

	public CommandException(String msg, Exception e) {
		super(msg, e);
	}
}

