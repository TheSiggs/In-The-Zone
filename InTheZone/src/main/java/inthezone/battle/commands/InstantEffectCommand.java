package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.Character;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.InstantEffectFactory;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.List;
import org.json.simple.JSONObject;

public class InstantEffectCommand extends Command {
	public final InstantEffect effect;
	private boolean isComplete;
	private boolean waitingForCompletion = false;

	public InstantEffectCommand(InstantEffect effect) {
		this.effect = effect;
		isComplete = effect.isComplete();
	}

	/**
	 * Returns true if the command is ready to run, otherwise false.
	 * @throws CommandException if it is called while the command is already
	 * waiting for completion.
	 * */
	public boolean isCompletedOrRequestCompletion() throws CommandException {
		if (isComplete) return true; else {
			if (waitingForCompletion) {
				throw new CommandException("Incomplete instant effect");
			} else {
				waitingForCompletion = true;
				return false;
			}
		}
	}

	/**
	 * Attempt to complete this command.
	 * */
	public void complete(MapPoint p) throws CommandException {
		if (!effect.complete(p))
			throw new CommandException("Could not complete instant effect");
		isComplete = true;
		waitingForCompletion = false;
	}

	@Override 
	@SuppressWarnings("unchecked")
	public JSONObject getJSON() {
		JSONObject r = new JSONObject();
		r.put("kind", CommandKind.INSTANT.toString());
		r.put("effect", effect.getJSON());
		return r;
	}

	public static InstantEffectCommand fromJSON(JSONObject json)
		throws ProtocolException
	{
		Object okind = json.get("kind");
		Object oeffect = json.get("effect");

		if (okind == null) throw new ProtocolException("Missing command type");
		if (oeffect == null) throw new ProtocolException("Missing effect");

		if (CommandKind.fromString((String) okind) != CommandKind.INSTANT)
			throw new ProtocolException("Expected effect command");

		try {
			return new InstantEffectCommand(
				InstantEffectFactory.fromJSON((JSONObject) oeffect));
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing effect command", e);
		}
	}

	@Override
	public List<Character> doCmd(Battle battle) throws CommandException {
		return effect.apply(battle);
	}
}


