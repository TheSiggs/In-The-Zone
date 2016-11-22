package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.commands.PushCommandRequest;
import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ATTACKAREA;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_MOVE;

public class ModePush extends Mode {
	private final Character selectedCharacter;

	public ModePush(BattleView view, Character selectedCharacter) {
		super(view);
		this.selectedCharacter = selectedCharacter;
	}

	@Override public Mode setupMode() {
		view.getStage().clearAllHighlighting();

		MapPoint centre = selectedCharacter.getPos();
		Collection<MapPoint> r = new ArrayList<>();
		r.add(centre.add(new MapPoint( 1, 0)));
		r.add(centre.add(new MapPoint(-1, 0)));
		r.add(centre.add(new MapPoint( 0, 1)));
		r.add(centre.add(new MapPoint( 0, -1)));

		Stage stage = view.getStage();
		r.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
		view.setSelectable(r);

		return this;
	}

	@Override public void handleSelection(MapPoint p) {
		if (view.isSelectable(p)) {
			view.battle.requestCommand(new PushCommandRequest(selectedCharacter.getPos(), p));
			view.setMode(new ModeAnimating(view));
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

