/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.graphics;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author informatics-palmerson
 */
public class CharacterGraphics implements AnimEventListener {

	private Spatial spatial = null;
	private int x = 0;
	private int y = 0;
	private AnimChannel channel;
	private AnimControl control;

	public CharacterGraphics(Spatial spatial) {
		this.spatial = spatial;
		// 3d_objects/creatures/goblin/animations/goblin.skeleton.xml
		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		setAnimation("idleA");
	}

	/**
	 * @return the spatial
	 */
	public Spatial getSpatial() {
		return spatial;
	}

	/**
	 * @param spatial the spatial to set
	 */
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		/*
		if (animName.equals("walk")) {
			channel.setAnim("idleA", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
		*/
	}

	@Override
	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

	}

	public void setAnimation(String name) {
		channel.setAnim(name);
	}

	public void die() {
		/*
		Node parent = this.getSpatial().getParent();
		if (parent != null) {
			parent.detachChild(this.getSpatial());
		}
				*/
	}
}
