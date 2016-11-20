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
	private abstract void handleSelection(MapPoint p);
	private abstract void handleMouseOver(MapPoint p);
	private abstract void handleMouseOut();

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

