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
	public abstract void setupMode();
	public abstract void handleSelection(MapPoint p);
	public abstract void handleMouseOver(MapPoint p);
	public abstract void handleMouseOut();
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

