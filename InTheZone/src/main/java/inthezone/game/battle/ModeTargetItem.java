package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import inthezone.battle.commands.UseItemCommandRequest;
import inthezone.comptroller.InfoTargetingItem;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTargetItem extends Mode {
	private final CharacterFrozen selectedCharacter;

	public ModeTargetItem(
		final BattleView view, final CharacterFrozen selectedCharacter
	) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode updateSelectedCharacter(
		final CharacterFrozen selectedCharacter
	) {
		return new ModeTargetItem(view, selectedCharacter);
	}

	@Override public Mode setupMode() {
		final Stage stage = view.getStage();
		getFutureWithRetry(view.battle.requestInfo(
			new InfoTargetingItem(selectedCharacter)))
			.ifPresent(tr -> {
				tr.stream().forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_TARGET));
				view.setSelectable(tr);
			});

		return this;
	}

	@Override public void handleSelection(final SelectionInfo selection) {
		final Optional<MapPoint> p = selection.pointPriority();

		if (p.isPresent() && view.isSelectable(p.get())) {
			view.battle.requestCommand(new UseItemCommandRequest(
				selectedCharacter.getPos(), p.get()));
			view.remainingPotions.set(view.remainingPotions.get() - 1);
			view.setMode(new ModeAnimating(view));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		final Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (view.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

