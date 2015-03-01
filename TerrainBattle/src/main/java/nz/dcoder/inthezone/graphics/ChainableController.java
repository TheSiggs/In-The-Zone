package nz.dcoder.inthezone.graphics;

import com.jme3.scene.control.AbstractControl;

public abstract class ChainableController extends AbstractControl {
	private final ControllerChain chain;

	public ChainableController(ControllerChain chain) {
		this.chain = chain;
	}

	protected void endControl() {
		this.setEnabled(false);
		chain.nextAnimation();
	}
}

