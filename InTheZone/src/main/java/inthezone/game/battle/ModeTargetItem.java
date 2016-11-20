package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.UseItemCommandRequest;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTargetItem extends Mode {
	private final BattleView view;
	private final Character selectedCharacter;

	public ModeTargetItem(BattleView view, Character selectedCharacter) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			view.battle.requestCommand(new UseItemCommandRequest(
				selectedCharacter.getPos(), p));
			view.areAllItemsUsed.setValue(true);
			view.setDefaultMode();
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override public void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		getFutureWithRetry(view.battle.getItemTargetingInfo(selectedCharacter))
			.ifPresent(tr -> {
				tr.stream().forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_TARGET));
				view.setSelectable(tr);
			});
	}

	@Override public void handleMouseOut() {
		view.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

