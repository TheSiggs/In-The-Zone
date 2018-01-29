package inthezone.battle.status;

import isogame.engine.MapPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import ssjsjs.annotations.As;
import ssjsjs.annotations.Field;
import ssjsjs.annotations.JSON;

public class FearedStatusEffect extends StatusEffect {
	private final MapPoint agentPos;
	private final Character agent;
	private final int g;

	public FearedStatusEffect(
		final StatusEffectInfo info,
		final int startTurn,
		final Character agent
	) {
		super(info, startTurn);
		this.agentPos = agent.getPos();
		this.agent = agent;
		g = info.param;
	}

	/**
	 * Construct an unresolved FearedStatusEffect
	 * */
	@JSON
	private FearedStatusEffect(
		@Field("info") final StatusEffectInfo info,
		@Field("startTurn") final int startTurn,
		@Field("agentPos")@As("agent") final MapPoint agent
	) {
		super(info, startTurn);
		this.agent = null;
		this.agentPos = agent;
		g = info.param;
	}

	@Override
	public StatusEffect resolve(final BattleState battle) throws ProtocolException {
		if (agent != null) return this; else {
			return new FearedStatusEffect(info, startTurn,
				battle.getCharacterAt(agentPos).orElseThrow(() ->
					new ProtocolException("Cannot find feared agent")));
		}
	}

	public List<Command> doBeforeTurn(final Battle battle, final Character c) {
		if (c.isDead()) return new ArrayList<>();

		final Set<MapPoint> targets = new HashSet<>();
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

