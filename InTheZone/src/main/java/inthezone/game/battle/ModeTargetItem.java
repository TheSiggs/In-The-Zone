package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.UseItemCommandRequest;
import inthezone.comptroller.InfoTargetingItem;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTargetItem extends Mode {
	private final Character selectedCharacter;

	public ModeTargetItem(BattleView view, Character selectedCharacter) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode updateSelectedCharacter(Character selectedCharacter) {
		return new ModeTargetItem(view, selectedCharacter);
	}

	@Override public Mode setupMode() {
		Stage stage = view.getStage();
		getFutureWithRetry(view.battle.requestInfo(new InfoTargetingItem(selectedCharacter)))
			.ifPresent(tr -> {
				tr.stream().forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_TARGET));
				view.setSelectable(tr);
			});

		return this;
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			view.battle.requestCommand(new UseItemCommandRequest(
				selectedCharacter.getPos(), p));
			view.areAllItemsUsed.setValue(true);
			view.setMode(new ModeSelect(view));
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (view.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

