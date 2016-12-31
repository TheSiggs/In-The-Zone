package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.comptroller.InfoMoveRange;
import inthezone.comptroller.InfoPath;
import isogame.engine.MapPoint;
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
	private final Queue<Character> moveQueue = new LinkedList<>();
	private final List<MapPoint> moveDestinations = new ArrayList<>();
	private final int moveRange;

	private Character moving = null;

	public ModeMoveEffect(
		BattleView view, ModeAnimating lastMode,
		Collection<Character> moveQueue, int moveRange
	) {
		super(view, null);
		this.lastMode = lastMode;
		this.moveQueue.addAll(moveQueue);
		this.moveRange = moveRange;
	}

	@Override public Mode updateSelectedCharacter(Character selectedCharacter) {
		return new ModeMoveEffect(view,
			lastMode.updateSelectedCharacter(selectedCharacter),
			moveQueue, moveRange);
	}

	@Override public boolean canCancel() {return false;}

	@Override public Mode setupMode() {
		moving = moveQueue.peek();

		if (moving == null) {
			view.battle.completeEffect(moveDestinations);
			return lastMode;

		} else {
			Stage stage = view.getStage();
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

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			moveDestinations.add(p);
			moveQueue.remove();
			view.setMode(setupMode());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);

		getFutureWithRetry(view.battle.requestInfo(new InfoPath(moving, p, moveRange)))
			.ifPresent(path -> path.stream()
				.forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH)));
	}
}

