package inthezone.battle.game;

import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTarget extends Mode {
	private final BattleView view;
	private final Character selectedCharacter;
	private Ability rootTargetingAbility = Optional.empty();
	private Ability targetingAbility = Optional.empty();
	private MapPoint castFrom;

	private Collection<MapPoint> targets = new ArrayList<>();

	private ModeTarget(
		BattleView view, Character selectedCharacter,
		Ability ability, Queue<MapPoint> recastFrom
	) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
		this.rootTargetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();

		view.canvas.getStage().clearAllHighlighting();
	}

	public static Mode getModeTarget(
		BattleView view, Character selectedCharacter,
		Ability ability, Queue<MapPoint> recastFrom
	) {
		ModeTarget r = new ModeTarget(view, selectedCharacter, ability, recastFrom);
		return r.setupTargeting(ability);
	}

	private Mode setupTargeting(Ability a) {
		this.targetingAbility = a;
		view.numTargets.setValue(a.info.range.nTargets);
		view.multiTargeting.setValue(a.info.range.nTargets > 1);
		if (a.recursionLevel > 0) {
			return new ModeAnimating();

		} else {
			if (targetingAbility.recursionLevel > 0) {
				castFrom = recastFrom.poll();
				if (castFrom == null) {
					return new ModeMove(view, selectedCharacter);
				}
			}

			Stage stage = view.getStage();
			getFutureWithRetry(view.battle.getTargetingInfo(
				selectedCharacter, castFrom, targetingAbility.)).ifPresent(tr -> {
					tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.canvas.setSelectable(tr);
				});
			}
	}

	private void addTarget(MapPoint p) {
		targets.add(p);
		view.numTargets.setValue(numTargets.getValue() - 1);
		if (numTargets.getValue() == 0) applyAbility();
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public void applyAbility() {
		if (!targets.isEmpty()) {
			battle.requestCommand(new UseAbilityCommandRequest(
				selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
				selectedCharacter.getPos(), targets, targetingAbility));
		}
		targets.clear();

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			hud.writeMessage("Subsequent!");
			targetingAbility
			setupTargeting(nextAbility.get());
		} else {
			nextAbility = rootTargetingAbility.getRecursion();
			if (nextAbility.isPresent()) {
				hud.writeMessage("Recursive rebound!");
				rootTargetingAbility = nextAbility.get();
				setupTargeting(nextAbility.get());
			} else {
				view.setDefaultMode();
			}
		}
	}

	@Override private void handleSelection(MapPoint p) {
		if (view.canvas.isSelectable(p)) {
			addTarget(p);
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override private void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (!stage.isHighlighted(p)) return; // TODO: is this necessary

		if (targetingItem) {
			if (view.canvas.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
		} else {
			getFutureWithRetry(battle.getAttackArea(
					selectedCharacter, castFrom, p, targetingAbility.get()))
				.ifPresent(area -> area.stream().forEach(pp ->
					stage.setHighlight(pp, HIGHLIGHT_ATTACKAREA)));
		}
	}

	@Override private void handleMouseOut() {
		view.canvas.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

