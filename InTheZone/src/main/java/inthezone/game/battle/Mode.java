package inthezone.game.battle;

import inthezone.battle.Character;
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
	 * */
	public Mode updateSelectedCharacter(final Character selected) {
		return this;
	}

	/**
	 * Register retargeting information.
	 * */
	public Mode retarget(final Map<MapPoint, MapPoint> retargeting) {
		return this;
	}

	/**
	 * An animation is complete.
	 * @param affected The targetables affected by the animation.
	 * */
	public Mode animationDone() {
		throw new RuntimeException(
			"GUI entered an invalid state.  Animation completed but we weren't waiting on any animation");
	}

	public boolean isInteractive() {return true;}
	public void handleSelection(final SelectionInfo selection) {}
	public void handleMouseOver(final MapPoint p) {}
	public void handleMouseOut() {}
	public boolean canCancel() {return true;}

	public static <T> Optional<T> getFutureWithRetry(final Future<T> f) {
		while (true) {
			try {
				return Optional.of(f.get());
			} catch (InterruptedException e) {
				/* ignore */
			} catch (ExecutionException e) {
				return Optional.empty();
			} catch (CancellationException e) {
				return Optional.empty();
			} 
		}
	}
}

