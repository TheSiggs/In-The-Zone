package nz.dcoder.inthezone.graphics;

import java.util.ArrayList;
import java.util.List;

public class ControllerChain {
	private final List<Runnable> chain = new ArrayList<>();
	private final Runnable chainStart;
	private final Runnable chainDone;
	private final List<Runnable> continuations = new ArrayList<>();
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
			chainStart.run();
			animation.run();
		}
	}

	/**
	 * Queue an action to be executed after all the animations finish.  One
	 * continuation is executed for each time the animations finish.  This is the
	 * mechanism by which we can build complex abilities such as Ahren's
	 * transport ability.
	 * */
	public void queueContinuation(Runnable continuation) {
		continuations.add(continuation);
	}

	/**
	 * Advance to the next animation.  Called automatically when the previous
	 * animation finishes.
	 * */
	void nextAnimation() {
		if (chain.size() == 0) {
			chainDone.run();
			running = false;
			if (continuations.size() > 0) {
				continuations.remove(0).run();
			}
		} else {
			Runnable next = chain.remove(0);
			next.run();
		}
	}
}

