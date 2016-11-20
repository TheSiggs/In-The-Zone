package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.PushCommandRequest;
import isogame.engine.MapPoint;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;

public class ModePush extends Mode {
	private final Character selectedCharacter;
	private final BattleView view;

	public ModePush(BattleView view, Character selectedCharacter) {
		this.view = view;
		this.selectedCharacter = selectedCharacter;

		MapPoint centre = c.getPos();
		Collection<MapPoint> r = new ArrayList<>();
		r.add(centre.add(new MapPoint( 1, 0)));
		r.add(centre.add(new MapPoint(-1, 0)));
		r.add(centre.add(new MapPoint( 0, 1)));
		r.add(centre.add(new MapPoint( 0, -1)));

		Stage stage = view.getStage();
		r.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
		view.canvas.setSelectable(r);
	}

	@Override private void handleSelection(MapPoint p) {
		if (view.canvas.isSelectable(p)) {
			view.battle.requestCommand(new PushCommandRequest(selectedCharacter.getPos(), p));
			view.setDefaultMode();
		} else {
			view.selectCharacter(Optional.empty());
		}
	}

	@Override private void handleMouseOver(MapPoint p) {
		Stage stage = view.getStage();
		stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
		if (view.canvas.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
	}

	@Override private void handleMouseOut() {
		return;
	}
}

