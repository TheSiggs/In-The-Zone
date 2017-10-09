package inthezone.game.battle;

import inthezone.battle.CharacterFrozen;
import isogame.engine.MapPoint;
import isogame.engine.SelectionInfo;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A GUI mode.
 * */
public abstract class Mode {
	protected final BattleView view;

	protected Mode(final BattleView view) {
		this.view = view;
	}

	/**
	 * Start this mode.  Can trigger an instant transition to another mode.
	 * Must be idempotent.
	 * */
	public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		return this;
	}

	/**
	 * Some properties of the selected character have changed.
	 * @param selected the selected character
	 * */
	public Mode updateSelectedCharacter(final CharacterFrozen selected) {
		return this;
	}

	/**
	 * Register retargeting information.
	 * @param retargeting the retargeting information
	 * */
	public Mode retarget(final Map<MapPoint, MapPoint> retargeting) {
		return this;
	}

	/**
	 * An animation is complete.
	 * */
	public Mode animationDone() {
		throw new RuntimeException(
			"GUI entered an invalid state.  Animation completed but we weren't waiting on any animation");
	}

	/**
	 * */
	public void nextPath() { return; }

	/**
	 * @return true if this mode permits the user to issue commands, otherwise
	 * false.
	 * */
	public boolean isInteractive() {return true;}

	/**
	 * Handle a selection event.
	 * @param selection information about what was selected
	 * */
	public void handleSelection(final SelectionInfo selection) {}

	/**
	 * Handler a mouse over event.
	 * @param p the point the mouse moved over
	 * */
	public void handleMouseOver(final MapPoint p) {}

	/**
	 * Handle a mouse out event.
	 * */
	public void handleMouseOut() {}

	/**
	 * @return true if this mode can be cancelled, otherwise false
	 * */
	public boolean canCancel() {return true;}

	/**
	 * Get a future value, or Optional.empty() if ther eis an error.
	 * */
	public static <T> Optional<T> getFutureWithRetry(final Future<T> f) {
		while (true) {
			try {
				return Optional.of(f.get());
			} catch (InterruptedException e) {
				/* ignore */
			} catch (final ExecutionException e) {
				return Optional.empty();
			} catch (final CancellationException e) {
				return Optional.empty();
			} 
		}
	}
}

