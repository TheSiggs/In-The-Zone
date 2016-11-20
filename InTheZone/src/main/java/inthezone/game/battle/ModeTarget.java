package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
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

	private ModeTarget(
		BattleView view, Character selectedCharacter,
		Ability ability, Queue<MapPoint> recastFrom
	) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = recastFrom;
		this.rootTargetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();

		view.getStage().clearAllHighlighting();
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
			return new ModeAnimating(view);

		} else {
			if (targetingAbility.recursionLevel > 0) {
				castFrom = recastFrom.poll();
				if (castFrom == null) {
					return new ModeMove(view, selectedCharacter);
				}
			}

			if (a.info.range.range == 0) {
				// range 0 abilities get applied immediately
				targets.add(castFrom);
				return applyAbility();

			} else {
				Stage stage = view.getStage();
				getFutureWithRetry(view.battle.getTargetingInfo(
					selectedCharacter, castFrom, targetingAbility)).ifPresent(tr -> {
						tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
						view.setSelectable(tr);
					});
			}
		}

		return this;
	}

	private Mode addTarget(MapPoint p) {
		targets.add(p);
		view.numTargets.setValue(view.numTargets.getValue() - 1);
		if (view.numTargets.getValue() == 0) return applyAbility(); else return this;
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public Mode applyAbility() {
		if (!targets.isEmpty()) {
			view.battle.requestCommand(new UseAbilityCommandRequest(
				selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
				selectedCharacter.getPos(), targets, targetingAbility));
		}
		targets.clear();

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			// TODO: rethink this
			//hud.writeMessage("Subsequent!");
			targetingAbility = nextAbility.get();
			return setupTargeting(targetingAbility);
		} else {
			nextAbility = rootTargetingAbility.getRecursion();
			if (nextAbility.isPresent()) {
				//hud.writeMessage("Recursive rebound!");
				rootTargetingAbility = nextAbility.get();
				return setupTargeting(nextAbility.get());
			} else {
				return new ModeMove(view, selectedCharacter);
			}
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
		if (!stage.isHighlighted(p)) return; // TODO: is this necessary

		getFutureWithRetry(view.battle.getAttackArea(
				selectedCharacter, castFrom, p, targetingAbility))
			.ifPresent(area -> area.stream().forEach(pp ->
				stage.setHighlight(pp, HIGHLIGHT_ATTACKAREA)));
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

