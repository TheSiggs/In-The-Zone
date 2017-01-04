package inthezone.game.battle;

import inthezone.battle.Character;
import inthezone.battle.Targetable;
import isogame.engine.MapPoint;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.List;
import java.util.Map;
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
	 * Must be idempotent.
	 * */
	public Mode setupMode() {
		view.getStage().clearAllHighlighting();
		return this;
	}

	/**
	 * Some properties of the selected character have changed.
	 * */
	public Mode updateSelectedCharacter(Character selected) {
		return this;
	}

	/**
	 * Register retargeting information.
	 * */
	public Mode retarget(Map<MapPoint, MapPoint> retargeting) {
		return this;
	}

	/**
	 * Update the characters that have been affected by this mode.
	 * */
	public void updateAffected(List<Targetable> affected) {}

	/**
	 * An animation is complete.
	 * @param affected The targetables affected by the animation.
	 * */
	public Mode animationDone() {
		throw new RuntimeException(
			"GUI entered an invalid state.  Animation completed but we weren't waiting on any animation");
	}

	public boolean isInteractive() {return true;}
	public void handleSelection(MapPoint p) {}
	public void handleMouseOver(MapPoint p) {}
	public void handleMouseOut() {}
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

