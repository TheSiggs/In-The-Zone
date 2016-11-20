package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.MoveCommandRequest;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;

/**
 * The selected character is to be moved.
 * */
public class ModeMove extends Mode {
	private Character selectedCharacter;
	private BattleView view;

	public ModeMove(BattleView view, Character selectedCharacter) {
		this.selectedCharacter = selectedCharacter;

		view.getStage().clearAllHighlighting();

		getFutureWithRetry(view.battle.getMoveRange(selectedCharacter)).ifPresent(mr -> {
			mr.stream().forEach(p -> view.getStage().setHighlight(p, HIGHLIGHT_MOVE));
			view.setSelectable(mr);
		});
	}

	@Override public void handleSelection(MapPoint p) {
		Optional<Character> oc = view.getCharacterAt(p);

		if (oc.isPresent() && oc.get().player == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else if (view.isSelectable(p)) {
			view.battle.requestCommand(
				new MoveCommandRequest(selectedCharacter.getPos(), p, selectedCharacter.player));
			view.setDefaultMode();
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);

		getFutureWithRetry(view.battle.getPath(selectedCharacter, p))
			.ifPresent(path -> path.stream()
				.forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH)));
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_PATH);
	}
}

