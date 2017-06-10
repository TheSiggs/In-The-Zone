package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Casting;
import inthezone.battle.Character;
import inthezone.battle.commands.AbilityAgentType;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.Targetable;
import inthezone.comptroller.InfoAffected;
import inthezone.comptroller.InfoAttackArea;
import inthezone.comptroller.InfoTargeting;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
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

	@Override public String toString() {
		return "==ModeTarget== canCancel: " + canCancel +
			", agent: " + selectedCharacter.getPos().toString() +
			", castFrom: " + castFrom.toString();
	}

	@Override public boolean canCancel() {return canCancel;}

	// used for recursion to track which characters we've already rebounded off.
	// An ability cannot rebound off the same character twice, except for the
	// agent of the ability (necessary for hit and run).
	private Set<MapPoint> retargetedFrom = new HashSet<>();

	private final Collection<Casting> allCastings = new ArrayList<>();
	private final Collection<Casting> thisRoundCastings = new ArrayList<>();

	public ModeTarget(
		BattleView view, Character selectedCharacter, Ability ability
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
		this.recastFrom = new LinkedList<>();
		this.recastFrom.add(subsequentLevelMarker);
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
		this.recastFrom = new LinkedList<>();
		this.recastFrom.addAll(recastFrom);
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
		view.getStage().clearAllHighlighting();
		view.numTargets.setValue(remainingTargets);
		view.multiTargeting.setValue(targetingAbility.info.range.nTargets > 1);

		final boolean onlyAffectsSelf = targetingAbility.info.range.targetMode.self &&
			!targetingAbility.info.range.targetMode.enemies &&
			!targetingAbility.info.range.targetMode.allies;

		final boolean noAOE = targetingAbility.info.range.radius == 0;
		final int range = targetingAbility.info.range.range;

		if (targetingAbility.info.range.range == 0) {
			// range 0 abilities get applied immediately
			allCastings.add(new Casting(castFrom, castFrom));
			return applyInstantly();

		} else if (onlyAffectsSelf && noAOE &&
			castFrom.distance(selectedCharacter.getPos()) <= range
		) {
			// this ability can only affect the agent, so apply it immediately
			allCastings.add(new Casting(castFrom, selectedCharacter.getPos()));
			return applyInstantly();

		} else {
			if (recursionLevel > 0) {
				view.hud.writeMessage("Recursive rebound " + recursionLevel + "!");
			}
			if (!canCancel) {
				view.hud.writeMessage("The attack rebounds!");
			}
			Stage stage = view.getStage();
			getFutureWithRetry(view.battle.requestInfo(new InfoTargeting(
				selectedCharacter, castFrom, retargetedFrom, targetingAbility)))
				.ifPresent(tr -> {
					tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(tr);
				});
		}

		return this;
	}

	private static boolean isCancellableEffect(Optional<InstantEffectInfo> e) {
		return e.map(eff ->
			eff.type == InstantEffectType.TELEPORT ||
			eff.type == InstantEffectType.MOVE).orElse(false);
	}

	/**
	 * Apply the ability instantly, offering the user to cancel if possible.
	 * */
	private Mode applyInstantly() {

		if (this.canCancel() &&
			!isCancellableEffect(targetingAbility.info.instantBefore)
		) {
			final Alert a = new Alert(Alert.AlertType.CONFIRMATION);
			a.setHeaderText(null);
			final String description =
				(new AbilityDescription(targetingAbility.info)).toString();

			final Text text = new Text(description);
			text.setWrappingWidth(400);
			a.getDialogPane().setContent(text);
			a.setGraphic(new ImageView(targetingAbility.info.media.icon));

			a.getButtonTypes().clear();
			a.getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			if (a.showAndWait().map(r -> r == ButtonType.APPLY).orElse(false)) {
				return applyAbility();
			} else {
				return this;
			}
		} else {
			return applyAbility();
		}
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

	private static final MapPoint subsequentLevelMarker = new MapPoint(-1, -1);

	private Mode applyAbility() {
		if (allCastings.isEmpty()) return this;

		if (recursionLevel > 0 && !recastFrom.isEmpty()) {
			castFrom = recastFrom.poll();
			if (castFrom.equals(subsequentLevelMarker)) castFrom = recastFrom.poll();
			remainingTargets = targetingAbility.info.range.nTargets;
			if (castFrom != null) return this; else {
				recastFrom.add(subsequentLevelMarker);
			}
		}

		if (recursionLevel < targetingAbility.info.recursion) {
			this.recursionLevel += 1;

			// Get the recast points.
			getFutureWithRetry(view.battle.requestInfo(new InfoAffected(
					selectedCharacter, targetingAbility, thisRoundCastings)))
				.ifPresent(affected -> {
					queueRecastPoints(affected);
					retargetedFrom.addAll(affected.stream().map(t ->
						t.getPos()).collect(Collectors.toList()));
				});
			recastFrom.add(subsequentLevelMarker);

			thisRoundCastings.clear();

			castFrom = recastFrom.poll();
			if (castFrom.equals(subsequentLevelMarker)) castFrom = recastFrom.poll();
			remainingTargets = targetingAbility.info.range.nTargets;
			if (castFrom != null) return this; else {
				recastFrom.clear();
				recastFrom.add(subsequentLevelMarker);
			}
		}

		// Get the recast points for subsequent abilities.  This may not work
		// properly if the ability also has recursion.
		getFutureWithRetry(view.battle.requestInfo(new InfoAffected(
				selectedCharacter, targetingAbility, allCastings)))
			.ifPresent(this::queueRecastPoints);

		canCancel = false;
		view.multiTargeting.setValue(false);

		// the agent of the ability can always be retargeted
		retargetedFrom.remove(selectedCharacter.getPos());
		view.battle.requestCommand(new UseAbilityCommandRequest(
			selectedCharacter.getPos(), AbilityAgentType.CHARACTER,
			targetingAbility,
			recursionLevel > 0 ? new HashSet<>() : retargetedFrom,
			allCastings));

		retargetedFrom.addAll(recastFrom);

		Optional<Ability> nextAbility = targetingAbility.getSubsequent();

		if (!recastFrom.isEmpty()) {
			castFrom = recastFrom.poll();
			if (!castFrom.equals(subsequentLevelMarker)) {
				thisRoundCastings.clear();
				allCastings.clear();
				remainingTargets = targetingAbility.info.range.nTargets;
				return castFrom == null?
					new ModeAnimating(view) : new ModeAnimating(view, this);
			}
		}

		if (nextAbility.isPresent()) {
			thisRoundCastings.clear();
			allCastings.clear();
			targetingAbility = nextAbility.get();

			castFrom = recastFrom.poll();
			recastFrom.add(subsequentLevelMarker);
			remainingTargets = targetingAbility.info.range.nTargets;
			return castFrom == null?
				new ModeAnimating(view) : new ModeAnimating(view, this);

		} else {
			return new ModeAnimating(view);
		}
	}

	private void queueRecastPoints(Collection<Targetable> affected) {
		Collection<MapPoint> affected1 = affected.stream()
			.filter(t -> t instanceof Character &&
				!retargetedFrom.contains(t.getPos()))
			.map(t -> t.getPos())
			.collect(Collectors.toList());
		recastFrom.addAll(affected1);
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			view.setMode(addTarget(p));
		} else if (canCancel) {
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

