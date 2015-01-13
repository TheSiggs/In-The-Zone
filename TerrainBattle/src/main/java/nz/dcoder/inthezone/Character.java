/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.scene.Spatial;
import nz.dcoder.inthezone.objects.CharacterState;

/**
 *
 * @author informatics-palmerson
 */
public class Character implements AnimEventListener {

	private Spatial spatial = null;
	private int x = 0;
	private int y = 0;
	private AnimChannel channel;
	private AnimControl control;
	private CharacterState state;

	public Character(Spatial spatial) {
		this.spatial = spatial;
		// 3d_objects/creatures/goblin/animations/goblin.skeleton.xml
		control = spatial.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		setAnimation("idleA");
		this.state = new CharacterState();
		state.setCharacter(this);
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

	/**
	 * @return the state
	 */
	public CharacterState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(CharacterState state) {
		this.state = state;
	}

	public void die() {
		this.getSpatial().getParent().detachChild(this.getSpatial());
	}
}
