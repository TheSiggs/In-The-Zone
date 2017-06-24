package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.PushCommandRequest;
import inthezone.comptroller.InfoPush;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;

public class ModePush extends Mode {
	private final Character selectedCharacter;

	public ModePush(
		final BattleView view, final Character selectedCharacter
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode updateSelectedCharacter(
		final Character selectedCharacter
	) {
		return new ModePush(view, selectedCharacter);
	}

	@Override public Mode setupMode() {
		final Stage stage = view.getStage();
		stage.clearAllHighlighting();

		getFutureWithRetry(view.battle.requestInfo(new InfoPush(selectedCharacter)))
			.ifPresent(mr -> {
				mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
				view.setSelectable(mr);
			});

		return this;
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final MapPoint p = selection.pointPriority().get();

		if (view.isSelectable(p)) {
			view.battle.requestCommand(
				new PushCommandRequest(selectedCharacter.getPos(), p));
			view.setMode(new ModeAnimating(view));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(final MapPoint p) {
		final Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (view.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

