package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Casting;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTarget extends Mode {
	private final Character selectedCharacter;
	private final Queue<MapPoint> recastFrom;
	private Ability targetingAbility;
	private MapPoint castFrom;
	private int recursionLevel = 0;
	private int remainingTargets;
	private boolean canCancel = true;

	@Override public boolean canCancel() {return canCancel;}

	// used for recursion to track which characters we've already rebounded off.
	private Set<MapPoint> retargetedFrom = new HashSet<>();

	private final Collection<Casting> allCastings = new ArrayList<>();
	private final Collection<Casting> thisRoundCastings = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter, Ability ability
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = new LinkedList<>();
		this.targetingAbility = ability;
		this.castFrom = selectedCharacter.getPos();
		this.remainingTargets = ability.info.range.nTargets;
	}

	private ModeTarget(
		BattleView view,
		Character selectedCharacter,
		Queue<MapPoint> recastFrom,
		Ability targetingAbility,
		MapPoint castFrom,
		boolean canCancel,
		int recursionLevel,
		int remainingTargets,
		Set<MapPoint> retargetedFrom,
		Collection<Casting> allCastings,
		Collection<Casting> thisRoundCastings
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = recastFrom;
		this.targetingAbility = targetingAbility;
		this.castFrom = castFrom;
		this.canCancel = canCancel;
		this.recursionLevel = recursionLevel;
		this.remainingTargets = remainingTargets;
		this.retargetedFrom.addAll(retargetedFrom);
		this.allCastings.addAll(allCastings);
		this.thisRoundCastings.addAll(thisRoundCastings);
	}

	@Override public Mode updateSelectedCharacter(Character selectedCharacter) {
		ModeTarget r = new ModeTarget(
			view, selectedCharacter, recastFrom,
			targetingAbility, castFrom, canCancel, recursionLevel,
			remainingTargets, retargetedFrom,
			allCastings, thisRoundCastings);
		return r;
	}

	@Override public Mode retarget(Map<MapPoint, MapPoint> retargeting) {
		castFrom = retargeting.getOrDefault(castFrom, castFrom);
		retargetedFrom = retargetedFrom.stream()
			.map(x -> retargeting.getOrDefault(x, x))
			.collect(Collectors.toSet());
		for (int i = 0; i < recastFrom.size(); i++) {
			MapPoint x = recastFrom.poll();
			recastFrom.add(retargeting.getOrDefault(x, x));
		}
		return this;
	}

	@Override public Mode setupMode() {
		System.err.println("Set up targeting");

		view.getStage().clearAllHighlighting();
		view.numTargets.setValue(remainingTargets);
		view.multiTargeting.setValue(targetingAbility.info.range.nTargets > 1);

		if (targetingAbility.info.range.range == 0) {
			// range 0 abilities get applied immediately
			allCastings.add(new Casting(castFrom, castFrom));
			return applyAbility();

		} else {
			if (recursionLevel > 0) {
				view.hud.writeMessage("Recursive rebound " + recursionLevel + "!");
			}
			if (!canCancel) {
				view.hud.writeMessage("The attack rebounds!");
			}
			Stage stage = view.getStage();
			getFutureWithRetry(view.battle.requestInfo(new InfoTargeting(
				selectedCharacter, castFrom, targetingAbility))).ifPresent(tr -> {
					tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(tr);
				});
		}

		return this;
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public Mode applyNow() {
		allCastings.addAll(thisRoundCastings);
		return applyAbility();
	}

	private Mode addTarget(MapPoint p) {
		thisRoundCastings.add(new Casting(castFrom, p));
		remainingTargets -= 1;
		if (remainingTargets <= 0) {
			allCastings.addAll(thisRoundCastings);
			return applyAbility();
		} else {
			return this;
		}
	}

	private Mode applyAbility() {
		if (allCastings.isEmpty()) {
			return this;

		} else if (!recastFrom.isEmpty()) {
			castFrom = recastFrom.poll();
			remainingTargets = targetingAbility.info.range.nTargets;
			return this;

		} else if (recursionLevel < targetingAbility.info.recursion) {
			recursionLevel += 1;

			// Get the recast points.
			getFutureWithRetry(view.battle.requestInfo(new InfoAffected(
					selectedCharacter, targetingAbility, thisRoundCastings)))
				.ifPresent(affected -> {
					queueRecastPoints(affected);
					retargetedFrom.addAll(affected.stream().map(t ->
						t.getPos()).collect(Collectors.toList()));
				});

			thisRoundCastings.clear();

			castFrom = recastFrom.poll();
			remainingTargets = targetingAbility.info.range.nTargets;
			if (castFrom != null) return this;
		}

		// Get the recast points for subsequent abilities.  This may not work
		// properly if the ability also has recursion.
		getFutureWithRetry(view.battle.requestInfo(new InfoAffected(
				selectedCharacter, targetingAbility, allCastings)))
			.ifPresent(this::queueRecastPoints);

		System.err.println(allCastings);
		System.err.println("Recast queue: " + recastFrom);
		System.err.println("Retargeted from : " + retargetedFrom);

		canCancel = false;
		retargetedFrom.addAll(recastFrom);
		view.multiTargeting.setValue(false);
		view.battle.requestCommand(new UseAbilityCommandRequest(
			selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
			targetingAbility, allCastings));

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (nextAbility.isPresent()) {
			System.err.println("Got subsequent");
			thisRoundCastings.clear();
			allCastings.clear();
			targetingAbility = nextAbility.get();

			castFrom = recastFrom.poll();
			System.err.println("Recasting from " + castFrom);
			remainingTargets = targetingAbility.info.range.nTargets;
			return castFrom == null?
				new ModeAnimating(view) : new ModeAnimating(view, this);

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
				!retargetedFrom.contains(t.getPos()))
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

