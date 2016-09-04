package inthezone.battle.commands;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.InstantEffectFactory;
import inthezone.battle.Targetable;
import inthezone.protocol.ProtocolException;
import isogame.engine.MapPoint;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.simple.JSONObject;

public class InstantEffectCommand extends Command {
	private InstantEffect effect;
	private boolean isComplete;
	private boolean waitingForCompletion = false;

	private final Optional<UseAbilityCommand> postAbility;
	private final Optional<InstantEffectCommand> postEffect;

	public InstantEffect getEffect() {
		return effect;
	}

	public InstantEffectCommand(
		InstantEffect effect,
		Optional<UseAbilityCommand> postAbility,
		Optional<InstantEffectCommand> postEffect
	) {
		this.effect = effect;
		this.postAbility = postAbility;
		this.postEffect = postEffect;
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
	public void complete(List<MapPoint> ps) throws CommandException {
		if (!effect.complete(ps))
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
				InstantEffectFactory.fromJSON((JSONObject) oeffect),
				Optional.empty(), Optional.empty());
		} catch (ClassCastException e) {
			throw new ProtocolException("Error parsing effect command", e);
		}
	}

	private void retarget(BattleState battle, Map<MapPoint, MapPoint> retarget) {
		this.effect = effect.retarget(battle, retarget);
	}

	@Override
	public List<Targetable> doCmd(Battle battle) throws CommandException {
		List<Targetable> affected = effect.apply(battle);

		if (postAbility.isPresent() || postEffect.isPresent()) {
			Map<MapPoint, MapPoint> retarget = effect.getRetargeting();
			postAbility.ifPresent(a -> a.retarget(retarget));
			postEffect.ifPresent(a -> a.retarget(battle.battleState, retarget));
		}

		return affected;
	}
}


