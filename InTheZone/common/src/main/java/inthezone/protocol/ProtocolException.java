package inthezone.protocol;

/**
 * Occurs when the protocol is not followed properly.
 * */
public class ProtocolException extends Exception {
	public ProtocolException() {
		super();
	}

	public ProtocolException(final String msg) {
		super(msg);
	}

	public ProtocolException(final String msg, final Exception cause) {
		super(msg, cause);
	}
}

