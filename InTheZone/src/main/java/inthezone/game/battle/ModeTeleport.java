package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTeleport extends Mode {
	private final Mode lastMode;
	private final Queue<Character> teleportQueue = new LinkedList<>();
	private final List<MapPoint> teleportDestinations = new ArrayList<>();
	private final int teleportRange;

	public ModeTeleport(
		BattleView view, Mode lastMode,
		Collection<Character> teleportQueue, int teleportRange
	) {
		super(view);
		this.lastMode = lastMode;
		this.teleportQueue.addAll(teleportQueue);
		this.teleportRange = teleportRange;
	}

	@Override public Mode setupMode() {
		Character teleporting = teleportQueue.peek();
		if (teleporting == null) {
			view.battle.completeEffect(teleportDestinations);
			return lastMode;

		} else {
			Stage stage = view.getStage();
			stage.clearAllHighlighting();
			getFutureWithRetry(view.battle.getTeleportRange(teleporting, teleportRange))
				.ifPresent(mr -> {
					mr.add(teleporting.getPos());
					mr.removeAll(teleportDestinations);
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
					view.setSelectable(mr);
				});
			return this;
		}

	}

	@Override public boolean canCancel() {return false;}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			teleportDestinations.add(p);
			teleportQueue.remove();
			Mode r = setupMode();
			if (r != this) view.setMode(r);
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);
		if (view.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_PATH);
	}

	@Override public void handleMouseOut() {
		return;
	}
}

