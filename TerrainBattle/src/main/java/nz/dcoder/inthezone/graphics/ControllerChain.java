package nz.dcoder.inthezone.graphics;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

public class ControllerChain {
	private final List<Consumer<Token>> chain = new ArrayList<>();
	private final Runnable chainStart;
	private final Runnable chainDone;
	private final List<Runnable> continuations = new ArrayList<>();
	private boolean running = false;

	public ControllerChain(Runnable chainStart, Runnable chainDone) {
		this.chainStart = chainStart;
		this.chainDone = chainDone;
	}

	/**
	 * Tracks how many animations are currently playing.
	 * */
	public class Token {
		private int count;

		/**
		 * You can't make your own Token.  The only way to get a Token is by
		 * queueing an animation with queueAnimation.
		 * */
		private Token() {
			count = 1;
		}

		public void startAnimation() {
			count += 1;
		}

		public void endAnimation() {
			count -= 1;
			if (count <= 0) {
				nextAnimation();
			}
		}
	}

	/**
	 * Queue an animation.  The animation starts playing as soon as the currently
	 * playing animation finishes.  The Token object is a magic token to be
	 * passed as a parameter to methods that require it.  It is not to be
	 * inspected or manipulated.  The Token is part of a mechanism that forces
	 * you to call queue animations in the proper manner.
	 *
	 * @param animation Typically a lambda expression that starts some animations
	 * with the setAnimation(name, n, token) call, or through a PathController.
	 * Each of these animations runs in parallel.  Sequential animations are made
	 * by repeated calls to queueAnimation.
	 *
	 * NOTE: If the animation lambda doesn't start any animations, or if it only
	 * starts single frame animations, then stack frames will build up until a
	 * "proper" animation is started.  This could cause a stack overflow in
	 * extreme cases.  A possible solution might be trampolining.  A better
	 * solution (also better from an artistic point of view) would be to use more
	 * animations.
	 *
	 * NOTE: There is a similar problem with the continuations.  This is unlikely
	 * to be a problem if continuations are used only to implement compound
	 * abilities.  (i.e. we need some other mechanism for cinematics).
	 * */
	public void queueAnimation(Consumer<Token> animation) {
		if (running) {
			chain.add(animation);
		} else {
			running = true;
			chainStart.run();

			Token t =  new Token();
			animation.accept(t);
			t.endAnimation();
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
			Consumer<Token> next = chain.remove(0);

			Token t = new Token();
			next.accept(t);
			t.endAnimation();
		}
	}
}

