package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.Targetable;
import inthezone.comptroller.InfoAffected;
import inthezone.comptroller.InfoAttackArea;
import inthezone.comptroller.InfoTargeting;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTarget extends Mode {
	private final Character selectedCharacter;
	private final Queue<MapPoint> recastFrom;
	private Ability rootTargetingAbility;
	private Ability targetingAbility;
	private MapPoint castFrom;
	private int recursionLevel = 0;
	private int remainingTargets;

	private final Collection<MapPoint> allTargets = new ArrayList<>();
	private final Collection<MapPoint> thisRoundTargets = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter, Ability ability
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = new LinkedList<>();
		this.rootTargetingAbility = ability;
		this.targetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();
		this.remainingTargets = ability.info.range.nTargets;
	}

	private ModeTarget(
		BattleView view,
		Character selectedCharacter,
		Queue<MapPoint> recastFrom,
		Ability rootTargetingAbility,
		Ability targetingAbility,
		MapPoint castFrom,
		int recursionLevel,
		int remainingTargets,
		Collection<MapPoint> allTargets,
		Collection<MapPoint> thisRoundTargets
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = recastFrom;
		this.rootTargetingAbility = rootTargetingAbility;
		this.targetingAbility = targetingAbility;
		this.castFrom = castFrom;
		this.recursionLevel = recursionLevel;
		this.remainingTargets = remainingTargets;
		this.allTargets.addAll(allTargets);
		this.thisRoundTargets.addAll(thisRoundTargets);
	}

	@Override public Mode updateSelectedCharacter(Character selectedCharacter) {
		return new ModeTarget(
			view, selectedCharacter, recastFrom,
			rootTargetingAbility, targetingAbility,
			castFrom, recursionLevel,
			remainingTargets, allTargets, thisRoundTargets);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		view.numTargets.setValue(remainingTargets);
		view.multiTargeting.setValue(targetingAbility.info.range.nTargets > 1);

		if (targetingAbility.info.range.range == 0) {
			// range 0 abilities get applied immediately
			allTargets.add(castFrom);
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
		thisRoundTargets.add(p);
		remainingTargets -= 1;
		if (remainingTargets <= 0) {
			allTargets.addAll(thisRoundTargets);
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
		if (allTargets.isEmpty()) {
			return this;

		} else {
			if (recursionLevel < targetingAbility.info.recursion) {
				recursionLevel += 1;

				// get the recast points
				getFutureWithRetry(view.battle.requestInfo(new InfoAffected(
						selectedCharacter, targetingAbility, castFrom, thisRoundTargets)))
					.ifPresent(affected -> queueRecastPoints(affected));

				thisRoundTargets.clear();
				castFrom = recastFrom.poll();
				remainingTargets = targetingAbility.info.range.nTargets;
				if (castFrom != null) return this;
			}

			view.battle.requestCommand(new UseAbilityCommandRequest(
				selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
				selectedCharacter.getPos(), allTargets, targetingAbility));
		}

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			thisRoundTargets.clear();
			targetingAbility = nextAbility.get();
			return new ModeAnimating(view, this);

		} else {
			return new ModeAnimating(view);
		}
	}

	@Override public void updateAffected(List<Targetable> affected) {
		queueRecastPoints(affected);
	}

	private void queueRecastPoints(Collection<Targetable> affected) {
		recastFrom.addAll(affected.stream()
			.filter(t -> t instanceof Character &&
				((Character) t).player == view.player)
			.map(t -> t.getPos())
			.collect(Collectors.toList()));
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			view.setMode(addTarget(p));
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

