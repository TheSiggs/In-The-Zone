package inthezone.game.battle;

import inthezone.battle.Character;
import isogame.engine.MapPoint;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_TARGET;

public class ModeTargetItem extends Mode {
	private final BattleView view;
	private final Character selectedCharacter;

	public ModeTargetItem(BattleView view, Character selectedCharacter) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;
	}

	@Override private void handleSelection(MapPoint p) {
		} else if (canvas.isSelectable(p)) {
			view.battle.requestCommand(new UseItemCommandRequest(
				selectedCharacter.getPos(), p));
			view.areAllItemsUsed.setValue(true);
			view.setDefaultMode();
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override private void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		getFutureWithRetry(view.battle.getItemTargetingInfo(selectedCharacter))
			.ifPresent(tr -> {
				tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
				canvas.setSelectable(tr);
			});
	}

	@Override private void handleMouseOut() {
		view.canvas.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
	}
}

