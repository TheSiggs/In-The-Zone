package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

import nz.dcoder.inthezone.data_model.pure.Position;

/**
 * Used to control an active graphics object, such as a character or a battle
 * object.
 * */
public abstract class ModelGraphics implements AnimEventListener {
	protected final Spatial spatial;
	protected final AnimChannel channel;
	protected final AnimControl control;
	protected final ControllerChain controllerChain;
	protected final PathController pathController;

	protected Position p;

	public ModelGraphics(Graphics graphics, Spatial spatial, Position p, String kind) {
		this.controllerChain = graphics.getControllerChain();

		this.spatial = spatial;

		spatial.setUserData("p", new SaveablePosition(p));
		spatial.setUserData("kind", kind);

		this.setPosition(p);

		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		
		pathController = new PathController(graphics, this);
		pathController.setSpatial(spatial);
		this.spatial.addControl(pathController);
	}

  public abstract Quaternion getUprightRotation();

	public Position getPosition() {
		return p;
	}

	protected void setPositionInternal(Position p) {
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

	public PathController getPathController() {
		return pathController;
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
	 * Set an animation to run until cancelled.  This can be queued at the end of
	 * a controller chain to restore the idle animation.
	 * */
	public void setAnimation(String name) {
		limitedAnimation = false;
		channel.setAnim(name);
		controllerChain.nextAnimation();
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

	public String getAnimation() {
		return channel.getAnimationName();
	}
}

