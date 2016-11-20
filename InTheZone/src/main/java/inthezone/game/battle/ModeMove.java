package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;

/**
 * The selected character is to be moved.
 * */
public class MoveMode extends Mode {
	private Character selectedCharacter;
	private BattleView view;

	public MoveMode(BattleView view, Character selectedCharacter) {
		this.selectedCharacter = character;

		getFutureWithRetry(view.battle.getMoveRange(selectedCharacter)).ifPresent(mr -> {
			mr.stream().forEach(p -> view.getStage().setHighlight(p, HIGHLIGHT_MOVE));
			view.canvas.setSelectable(mr);
		});
	}

	@Override private void handleSelection(MapPoint p) {
		if (oc.isPresent() && oc.get().player == player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else if (canvas.isSelectable(p)) {
			view.requestCommand(
				new MoveCommandRequest(selectedCharacter.getPos(), p, selectedCharacterc.player));
			view.setDefaultMode();
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override private void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);

		view.getFutureWithRetry(view.battle.getPath(selectedCharacter, p))
			.ifPresent(path -> path.stream()
				.forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH)));
	}

	@Override private void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_PATH);
	}
}

