package inthezone.battle.game;

import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

public class ModeTarget extends Mode {
	private final BattleView view;
	private final Character selectedCharacter;
	private Ability rootTargetingAbility = Optional.empty();
	private Ability targetingAbility = Optional.empty();

	private Collection<MapPoint> targets = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter,
		Ability ability, Queue<MapPoint> recastFrom
	) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
		this.rootTargetingAbility = ability;

		setupTargeting(ability);

		MapPoint castFrom = selectedCharacter.getPos();
		if (targetingAbility.recursionLevel > 0) {
			castFrom = recastFrom.poll();
			if (castFrom == null) {
				view.setDefaultMode(); return;
			}
		}

		Stage stage = view.getStage();
		view.getFutureWithRetry(view.battle.getTargetingInfo(
			selectedCharacter, castFrom, targetingAbility.)).ifPresent(tr -> {
				tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
				view.canvas.setSelectable(tr);
			});
	}

	private void setupTargeting(Ability a) {
		this.targetingAbility = a;
		view.numTargets.setValue(a.info.range.nTargets);
		view.multiTargeting.setValue(a.info.range.nTargets > 1);
		if (a.recursionLevel > 0) {
			view.setDefaultMode();
		} else {
			view.setMode(TARGET);
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
	}

	@Override private void handleMouseOut() {
	}
}

