package nz.dcoder.inthezone.graphics;

import java.util.ArrayList;
import java.util.List;

public class ControllerChain {
	private final List<Runnable> chain = new ArrayList<>();
	private final Runnable chainStart;
	private final Runnable chainDone;
	private boolean running = false;

	public ControllerChain(Runnable chainStart, Runnable chainDone) {
		this.chainStart = chainStart;
		this.chainDone = chainDone;
	}

	/**
	 * Queue an animation to start running as soon as possible.
	 * */
	public void queueAnimation(Runnable animation) {
		if (running) {
			chain.add(animation);
		} else {
			running = true;
			animation.run();
			chainStart.run();
		}
	}

	/**
	 * Advance to the next animation.  Called automatically when the previous
	 * animation finishes.
	 * */
	void nextAnimation() {
		if (chain.size() == 0) {
			chainDone.run();
			running = false;
		} else {
			Runnable next = chain.remove(0);
			next.run();
		}
	}
}

