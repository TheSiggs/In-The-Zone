package inthezone.protocol;

public class ProtocolException extends Exception {
	public ProtocolException() {
		super();
	}

	public ProtocolException(String msg) {
		super(msg);
	}

	public ProtocolException(String msg, Exception cause) {
		super(msg, cause);
	}
}

