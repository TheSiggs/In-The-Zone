package inthezone.battle.commands;

public class CommandException extends Exception {
	public CommandException() {
		super();
	}

	public CommandException(final String msg) {
		super(msg);
	}

	public CommandException(final String msg, final Exception e) {
		super(msg, e);
	}
}

