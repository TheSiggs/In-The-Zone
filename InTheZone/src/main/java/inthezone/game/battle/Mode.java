package inthezone.game.battle;

import isogame.engine.MapPoint;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Optional;

/**
 * A GUI mode.
 * */
public abstract class Mode {
	protected final BattleView view;

	protected Mode(BattleView view) {
		this.view = view;
	}

	/**
	 * Start this mode.  Can trigger an instant transition to another mode.
	 * */
	public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		return this;
	}

	/**
	 * An animation is complete.
	 * */
	public Mode animationDone() {
		throw new RuntimeException(
			"GUI entered an invalid state.  Animation completed but we weren't waiting on any animation");
	}

	public void handleSelection(MapPoint p) {}
	public void handleMouseOver(MapPoint p) {}
	public void handleMouseOut() {}
	public boolean isInteractive() {return true;}
	public boolean canCancel() {return true;}

	public static <T> Optional<T> getFutureWithRetry(Future<T> f) {
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

