package inthezone.game.battle;

import isogame.engine.MapPoint;
import isogame.engine.Stage;
import java.util.Stack;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ZONE;

public class ModeManager {
	private Mode mode = null;
	private final Stack<Mode> modes = new Stack<>();
	private Mode baseMode;
	private final Mode otherTurnBaseMode;
	private final BattleView view;

	public ModeManager(
		Mode baseMode, Mode otherTurnBaseMode, BattleView view
	) {
		this.view = view;
		this.baseMode = baseMode;
		this.otherTurnBaseMode = otherTurnBaseMode;
	}

	/**
	 * Get the current mode.
	 * */
	public Mode getMode() {return mode;}

	/**
	 * Switch to a different mode.
	 * */
	public void switchMode(Mode mode) {
		modes.push(this.mode);
		setMode(mode);
	}

	/**
	 * Call when the current mode ends.
	 * */
	public void nextMode() {
		if (modes.empty()) {
			setMode(view.isMyTurn.getValue()? baseMode : otherTurnBaseMode);
		} else {
			setMode(modes.pop());
		}
	}

	/**
	 * Update the default mode.
	 * */
	public void setBaseMode(Mode baseMode) {
		this.baseMode = baseMode;
	}

	/**
	 * Reset to the default mode.
	 * */
	public void resetMode() {
		modes.clear();
		nextMode();
	}

	private void setMode(Mode mode) {
		this.mode = mode;
		mode.setupMode();
		Stage stage = view.getStage();
		for (MapPoint p : view.zones) stage.setHighlight(p, HIGHLIGHT_ZONE);
	}
}

