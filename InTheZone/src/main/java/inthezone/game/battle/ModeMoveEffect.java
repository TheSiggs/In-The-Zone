package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.comptroller.InfoMoveRange;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;

public class ModeMoveEffect extends ModeMove {
	private final ModeAnimating lastMode;
	private final Queue<Character> moveQueue = new LinkedList<>();
	private final List<MapPoint> moveDestinations = new ArrayList<>();
	private final int moveRange;

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
		Character moving = moveQueue.peek();
		super.selectedCharacter = moving;
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
}

