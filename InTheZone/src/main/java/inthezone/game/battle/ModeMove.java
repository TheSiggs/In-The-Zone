package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.MoveCommandRequest;
import inthezone.comptroller.InfoMoveRange;
import inthezone.comptroller.InfoPath;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_PATH;

/**
 * The selected character is to be moved.
 * */
public class ModeMove extends Mode {
	protected final Character selectedCharacter;

	public ModeMove(
		final BattleView view, final Character selectedCharacter
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode updateSelectedCharacter(
		final Character selectedCharacter
	) {
		return new ModeMove(view, selectedCharacter);
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();

		getFutureWithRetry(view.battle.requestInfo(
			new InfoMoveRange(selectedCharacter)))
				.ifPresent(mr -> {
					mr.stream().forEach(p ->
						view.getStage().setHighlight(p, HIGHLIGHT_MOVE));
					view.setSelectable(mr);
				});

		return this;
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<Character> oc = selection.spritePriority()
			.flatMap(p -> view.sprites.getCharacterAt(p));

		final Optional<MapPoint> p = selection.pointPriority();

		if (p.isPresent() && view.isSelectable(p.get())) {
			view.battle.requestCommand(
				new MoveCommandRequest(
					selectedCharacter.getPos(), p.get(), selectedCharacter.player));
			view.setMode(new ModeAnimating(view));
		} else if (oc.isPresent() && oc.get().player == view.player) {
			view.selectCharacter(Optional.of(oc.get()));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(final MapPoint p) {
		final Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_PATH);

		getFutureWithRetry(view.battle.requestInfo(
			new InfoPath(selectedCharacter, p)))
				.ifPresent(path -> path.stream()
					.forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH)));
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_PATH);
	}
}

