package nz.dcoder.inthezone.data_model;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.List;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * The interface through which the model layer communicates to the presentation layer
 * */
public class BattleController {
	public Consumer<Turn> onPlayerTurnStart = null;
	public Consumer<Turn> onAIPlayerTurnStart = null;
	public Consumer<DoBattleEnd> onBattleEnd = null;
	public Consumer<DoMoveInfo> onMove = null;
	public Consumer<DoAbilityInfo> onAbility = null;
	public Consumer<DoCharacterDeath> onDeath = null;
	public Consumer<DoObjectDestruction> onDestruction = null;

	void callOnPlayerTurnStart(Turn turn) {
		if (onPlayerTurnStart != null) onPlayerTurnStart.accept(turn);
	}

	void callOnAIPlayerTurnStart(Turn turn) {
		if (onAIPlayerTurnStart != null) onAIPlayerTurnStart.accept(turn);
	}

	void callOnBattleEnd(boolean playerWins) {
		if (onBattleEnd != null) onBattleEnd.accept(new DoBattleEnd(playerWins));
	}

	void callOnMove(Position start, List<Position> path) {
		if (onMove != null) onMove.accept(new DoMoveInfo(start, path));
	}

	void callOnAbility(
		Position agentPos,
		Position agentTarget,
		Collection<Position> targets,
		AbilityInfo ability
	) {
		if (onAbility != null) onAbility.accept(
			new DoAbilityInfo(agentPos, agentTarget, targets, ability));
	}

	void callOnDeath(BattleObject body) {
		if (onDeath != null) onDeath.accept(
			new DoCharacterDeath(body.position, body.name));
	}

	void callOnDestruction(BattleObject object) {
		if (onDestruction != null) onDestruction.accept(
			new DoObjectDestruction(object.position));
	}
}

