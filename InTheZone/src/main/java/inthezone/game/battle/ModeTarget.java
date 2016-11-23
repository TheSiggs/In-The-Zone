package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.comptroller.InfoAttackArea;
import inthezone.comptroller.InfoTargeting;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTarget extends Mode {
	private final Character selectedCharacter;
	private final Queue<MapPoint> recastFrom;
	private Ability rootTargetingAbility;
	private Ability targetingAbility;
	private MapPoint castFrom;
	private int recursionLevel = 0;

	private Collection<MapPoint> targets = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter, Ability ability
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = new LinkedList<>();
		this.rootTargetingAbility = ability;
		this.targetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		view.numTargets.setValue(targetingAbility.info.range.nTargets);
		view.multiTargeting.setValue(targetingAbility.info.range.nTargets > 1);

		if (recursionLevel > 0) {
			castFrom = recastFrom.poll();
			if (castFrom == null) {
				return applyAbility();
			}
		}

		if (targetingAbility.info.range.range == 0) {
			// range 0 abilities get applied immediately
			targets.add(castFrom);
			return applyAbility();

		} else {
			Stage stage = view.getStage();
			getFutureWithRetry(view.battle.requestInfo(new InfoTargeting(
				selectedCharacter, castFrom, targetingAbility))).ifPresent(tr -> {
					tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(tr);
				});
		}

		return this;
	}

	private Mode addTarget(MapPoint p) {
		targets.add(p);
		view.numTargets.setValue(view.numTargets.getValue() - 1);
		if (view.numTargets.getValue() == 0) {
			return applyAbility();
		} else {
			return this;
		}
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public Mode applyAbility() {
		if (targets.isEmpty()) {
			return this;

		} else if (recursionLevel < targetingAbility.info.recursion) {
			recursionLevel += 1;
			return this;

		} else {
			view.battle.requestCommand(new UseAbilityCommandRequest(
				selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
				selectedCharacter.getPos(), targets, targetingAbility));
			targets.clear();
		}


		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			targetingAbility = nextAbility.get();
			return new ModeAnimating(view, this);

		} else {
			return new ModeAnimating(view);
		}
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			Mode r = addTarget(p);
			if (r != this) view.setMode(r);
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (!stage.isHighlighted(p)) return;

		getFutureWithRetry(view.battle.requestInfo(new InfoAttackArea(
				selectedCharacter, targetingAbility, castFrom, p)))
			.ifPresent(area -> area.stream().forEach(pp ->
				stage.setHighlight(pp, HIGHLIGHT_ATTACKAREA)));
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

