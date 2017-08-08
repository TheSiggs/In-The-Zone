package inthezone.battle.status;

import inthezone.battle.Battle;
import inthezone.battle.BattleState;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.instant.PullPush;
import inthezone.protocol.ProtocolException;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class FearedStatusEffect extends StatusEffect {
	private final MapPoint agentPos;
	private final Character agent;
	private final int g;

	public FearedStatusEffect(
		final StatusEffectInfo info, final Character agent, final int startTurn
	) {
		super(info, startTurn);
		this.agentPos = agent.getPos();
		this.agent = agent;
		g = info.param;
	}

	/**
	 * Construct an unresolved FearedStatusEffect
	 * */
	private FearedStatusEffect(
		final StatusEffectInfo info, final MapPoint agent, final int startTurn
	) {
		super(info, startTurn);
		this.agent = null;
		this.agentPos = agent;
		g = info.param;
	}

	@Override 
	public JSONObject getJSON() {
		final JSONObject r = new JSONObject();
		r.put("info", info.toString());
		r.put("startTurn", startTurn);
		r.put("agent", agentPos.getJSON());
		return r;
	}

	public static FearedStatusEffect fromJSON(final JSONObject o)
		throws ProtocolException
	{
		try {
			final MapPoint agent = MapPoint.fromJSON(o.getJSONObject("agent"));
			final int startTurn = o.getInt("startTurn");
			final StatusEffectInfo info = new StatusEffectInfo(o.getString("info"));

			return new FearedStatusEffect(info, agent, startTurn);

		} catch (JSONException|CorruptDataException e) {
			throw new ProtocolException("Error parsing feared status effect", e);
		}
	}

	@Override
	public StatusEffect resolve(final BattleState battle) throws ProtocolException {
		if (agent != null) return this; else {
			return new FearedStatusEffect(info, 
				battle.getCharacterAt(agentPos).orElseThrow(() ->
					new ProtocolException("Cannot find feared agent")), startTurn);
		}
	}

	public List<Command> doBeforeTurn(final Battle battle, final Character c) {
		if (c.isDead()) return new ArrayList<>();

		final Collection<MapPoint> targets = new ArrayList<>();
		targets.add(c.getPos());

		final PullPush p = PullPush.getEffect(
			battle.battleState,
			new InstantEffectInfo(InstantEffectType.PUSH, g),
			agent.getPos(), targets, true);

		final List<Command> r = new ArrayList<>();
		r.add(new InstantEffectCommand(p, Optional.empty(), Optional.empty()));
		return r;
	}

	@Override public boolean isBeforeTurnExhaustive() {return true;}
}

