package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ModeTeleport extends Mode {
	private final BattleView view;
	private final Queue<Character> teleportQueue;
	private final List<MapPoint> teleportDestinations = new ArrayList<>();
	private final int teleportRange;

	public ModeTeleport(
		BattleView view, Queue<Character> teleportQueue, int teleportRange
	) {
		this.view = view;
		this.teleportQueue = teleportQueue;
		this.teleportRange = teleportRange;

		nextTeleport();
	}

	@Overide private void handleSelection(MapPoint p) {
		if (view.canvas.isSelectable(p)) {
			teleportDestinations.add(p);
			teleportQueue.remove();
			nextTeleport();
		}
	}

	public void nextTeleport() {
		Character teleporting = teleportQueue.peek();
		if (teleporting == null) {
			view.battle.completeEffect(teleportDestinations);
			view.resetMode();
		} else {
			Stage stage = view.getStage();
			stage.clearAllHighlighting();
			view.getFutureWithRetry(view.battle.getTeleportRange(teleporting, teleportRange))
				.ifPresent(mr -> {
					mr.add(teleporting.getPos());
					mr.removeAll(teleportDestinations);
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.canvas.setSelectable(mr);
				});
		}
	}


	@Overide private void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);
		if (view.canvas.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_PATH);
	}

	@Overide private void handleMouseOut() {
		return;
	}
}

