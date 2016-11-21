package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
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
	private final BattleView view;
	private final Character selectedCharacter;
	private final Queue<MapPoint> recastFrom;
	private Ability rootTargetingAbility;
	private Ability targetingAbility;
	private MapPoint castFrom;

	private Collection<MapPoint> targets = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter, Ability ability
	) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = new LinkedList<>();
		this.rootTargetingAbility = ability;
		this.targetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();
	}

	@Override public void setupMode() {
		view.getStage().clearAllHighlighting();
		view.numTargets.setValue(targetingAbility.info.range.nTargets);
		view.multiTargeting.setValue(targetingAbility.info.range.nTargets > 1);

		if (targetingAbility.recursionLevel > 0) {
			castFrom = recastFrom.poll();
			if (castFrom == null) {
				view.modes.resetMode(); return;
			}
		}

		if (targetingAbility.info.range.range == 0) {
			// range 0 abilities get applied immediately
			targets.add(castFrom);
			applyAbility();

		} else {
			Stage stage = view.getStage();
			getFutureWithRetry(view.battle.getTargetingInfo(
				selectedCharacter, castFrom, targetingAbility)).ifPresent(tr -> {
					tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(tr);
				});
		}
	}

	private void addTarget(MapPoint p) {
		targets.add(p);
		view.numTargets.setValue(view.numTargets.getValue() - 1);
		if (view.numTargets.getValue() == 0) applyAbility();
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public void applyAbility() {
		if (targets.isEmpty()) return; else {
			view.battle.requestCommand(new UseAbilityCommandRequest(
				selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
				selectedCharacter.getPos(), targets, targetingAbility));
			targets.clear();
		}

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			// TODO: rethink this
			//hud.writeMessage("Subsequent!");
			targetingAbility = nextAbility.get();
			setupMode();
		} else {
			nextAbility = rootTargetingAbility.getRecursion();
			if (nextAbility.isPresent()) {
				//hud.writeMessage("Recursive rebound!");
				rootTargetingAbility = nextAbility.get();
				targetingAbility = rootTargetingAbility;
				setupMode();
			} else {
				view.modes.resetMode();
			}
		}
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			addTarget(p);
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (!stage.isHighlighted(p)) return;

		getFutureWithRetry(view.battle.getAttackArea(
				selectedCharacter, castFrom, p, targetingAbility))
			.ifPresent(area -> area.stream().forEach(pp ->
				stage.setHighlight(pp, HIGHLIGHT_ATTACKAREA)));
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

