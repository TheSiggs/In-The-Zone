package nz.dcoder.inthezone.graphics;

import com.jme3.animation.Animation;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Represents the visual part of a battle object (such as a corpse or a
 * boulder)
 * */
public class ObjectGraphics implements AnimEventListener {
	private final Spatial spatial;
	private final AnimChannel channel;
	private final AnimControl control;
	private final ControllerChain controllerChain;

	private Position p;

	public ObjectGraphics(Graphics graphics, Spatial spatial, Position p) {
		this.controllerChain = graphics.getControllerChain();

		this.spatial = spatial;

		spatial.setUserData("p", new SaveablePosition(p));
		spatial.setUserData("kind", "object");

		this.setPosition(p);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
	}

	/**
	 * Hack to allow extending a model with animations that weren't supplied by
	 * the artist.  This should be removed at first opportunity.
	 * */
	void addAnim(Animation anim) {
		control.addAnim(anim);
	}

	public Position getPosition() {
		return p;
	}

	void setPositionInternal(Position p) {
		this.p = p;
		((SaveablePosition) this.spatial.getUserData("p")).setPosition(p);
	}

	public void setPosition(Position p) {
		setPositionInternal(p);

		Vector3f translation = Graphics.positionToVector(p);
		spatial.setLocalTranslation(translation);
	}

	/**
	 * @return the spatial
	 */
	public Spatial getSpatial() {
		return spatial;
	}

	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (limitedAnimation) {
			animRepeats -= 1;
			if (animRepeats < 1) {
				channel.setLoopMode(LoopMode.DontLoop);
				limitedAnimation = false;
				controllerChain.nextAnimation();
			}
		}
	}

	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
	}

	/**
	 * Set an animation to run until cancelled
	 * */
	public void setAnimation(String name) {
		limitedAnimation = false;
		channel.setAnim(name);
	}

	private boolean limitedAnimation = false;
	private int animRepeats = 0;

	/**
	 * Set an animation to run n times, and notify when done
	 * */
	public void setAnimation(String name, int n) {
		limitedAnimation = true;
		animRepeats = n;
		channel.setAnim(name);
	}
}

