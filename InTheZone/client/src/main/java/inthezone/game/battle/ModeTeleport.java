package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import inthezone.comptroller.InfoTeleportRange;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

/**
 * The teleport instant effect needs more information.
 * */
public class ModeTeleport extends Mode {
	private final ModeAnimating lastMode;
	private final Queue<CharacterFrozen> teleportQueue = new LinkedList<>();
	private final List<MapPoint> teleportDestinations = new ArrayList<>();
	private final int teleportRange;
	private final boolean canCancel;

	public ModeTeleport(
		final BattleView view,
		final ModeAnimating lastMode,
		final Collection<CharacterFrozen> teleportQueue,
		final int teleportRange,
		final boolean canCancel
	) {
		super(view);
		this.lastMode = lastMode;
		this.teleportQueue.addAll(teleportQueue);
		this.teleportRange = teleportRange;
		this.canCancel = canCancel;
		System.err.println("Teleport queue: " + teleportQueue);
	}

	@Override public Mode updateSelectedCharacter(
		final CharacterFrozen selectedCharacter
	) {
		return new ModeTeleport(view,
			lastMode.updateSelectedCharacter(selectedCharacter),
			teleportQueue, teleportRange, canCancel);
	}

	@Override public Mode setupMode() {
		final CharacterFrozen teleporting = teleportQueue.peek();
		if (teleporting == null) {
			view.battle.completeEffect(teleportDestinations);
			return lastMode;

		} else {
			Stage stage = view.getStage();
			stage.clearAllHighlighting();
			getFutureWithRetry(view.battle.requestInfo(
				new InfoTeleportRange(teleporting, teleportRange))).ifPresent(mr -> {
					mr.add(teleporting.getPos());
					mr.removeAll(teleportDestinations);
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(mr);
				});
			return this;
		}
	}

	@Override public boolean canCancel() {return canCancel;}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<MapPoint> p = selection.pointPriority();

		if (p.isPresent() && view.isSelectable(p.get())) {
			teleportDestinations.add(p.get());
			teleportQueue.remove();
			view.setMode(setupMode());
		}
	}

	@Override public void handleMouseOver(final MapPoint p) {
		final Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);
		if (view.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_PATH);
	}

	@Override public void handleMouseOut() {
		return;
	}
}

