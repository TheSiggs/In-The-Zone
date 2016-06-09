package inthezone.battle.commands;

import inthezone.protocol.ProtocolException;
import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;

public class CommandDecoder {
	private static final CommandDecoder instance = new CommandDecoder();
	private final Map<String, Function<JSONObject, Command>> decoders;

	public static CommandDecoder get() {
		return instance;
	}

	private CommandDecoder() {
		decoders = new HashMap<>();
	}

	public void registerDecoder(
		String commandName, Function<JSONObject, Command> decoder
	) {
		decoders.put(commandName, decoder);
	}

	public Command decode(JSONObject json) throws ProtocolException {
		Object oname = json.get("name");
		if (oname == null) throw new ProtocolException("Expected command name");
		try {
			String name = (String) oname;
			Function<JSONObject, Command> decoder = decoders.get(name);
			if (decoder == null) throw new ProtocolException("Didn't understand command");
			return decoder.apply(json);
		} catch (ClassCastException e) {
			throw new ProtocolException("malformed command");
		} catch (RuntimeException e) {
			if (e.getCause() instanceof ProtocolException) {
				throw (ProtocolException) e.getCause();
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}

