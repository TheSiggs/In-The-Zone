package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import inthezone.battle.commands.PushCommandRequest;
import inthezone.comptroller.InfoPush;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;

/**
 * The selected character is to push.
 * */
public class ModePush extends Mode {
	private final CharacterFrozen selectedCharacter;

	public ModePush(
		final BattleView view, final CharacterFrozen selectedCharacter
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode updateSelectedCharacter(
		final CharacterFrozen selectedCharacter
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
		final Optional<MapPoint> p = selection.pointPriority();

		if (p.isPresent() && view.isSelectable(p.get())) {
			view.battle.requestCommand(
				new PushCommandRequest(selectedCharacter.getPos(), p.get()));
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

