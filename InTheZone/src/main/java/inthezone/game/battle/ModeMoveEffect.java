package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import inthezone.comptroller.InfoMoveRange;
import inthezone.comptroller.InfoPath;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;

public class ModeMoveEffect extends ModeMove {
	private final ModeAnimating lastMode;
	private final Queue<CharacterFrozen> moveQueue = new LinkedList<>();
	private final List<MapPoint> moveDestinations = new ArrayList<>();
	private final int moveRange;
	private boolean canCancel;

	private CharacterFrozen moving = null;

	public ModeMoveEffect(
		final BattleView view,
		final ModeAnimating lastMode,
		final Collection<CharacterFrozen> moveQueue,
		final int moveRange,
		final boolean canCancel
	) {
		super(view, null);
		this.lastMode = lastMode;
		this.moveQueue.addAll(moveQueue);
		this.moveRange = moveRange;
		this.canCancel = canCancel;
	}

	@Override public Mode updateSelectedCharacter(
		final CharacterFrozen selectedCharacter
	) {
		return new ModeMoveEffect(view,
			lastMode.updateSelectedCharacter(selectedCharacter),
			moveQueue, moveRange, canCancel);
	}

	@Override public boolean canCancel() {return canCancel;}

	@Override public Mode setupMode() {
		moving = moveQueue.peek();

		if (moving == null) {
			view.battle.completeEffect(moveDestinations);
			return lastMode;

		} else {
			final Stage stage = view.getStage();
			stage.clearAllHighlighting();
			getFutureWithRetry(view.battle.requestInfo(
				new InfoMoveRange(moving, moveRange))).ifPresent(mr -> {
					mr.add(moving.getPos());
					mr.removeAll(moveDestinations);
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
					view.setSelectable(mr);
				});
			return this;
		}
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final MapPoint p = selection.pointPriority().get();

		if (view.isSelectable(p)) {
			moveDestinations.add(p);
			moveQueue.remove();
			view.setMode(setupMode());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		final Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);

		getFutureWithRetry(view.battle.requestInfo(new InfoPath(moving, p, moveRange)))
			.ifPresent(path -> path.stream()
				.forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH)));
	}
}

