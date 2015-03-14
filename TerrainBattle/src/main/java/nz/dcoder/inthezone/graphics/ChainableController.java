package nz.dcoder.inthezone.graphics;

import com.jme3.scene.control.AbstractControl;

public abstract class ChainableController extends AbstractControl {
	private ControllerChain.Token token;

	protected void startControl(ControllerChain.Token token) {
		this.token = token;
		token.startAnimation();
	}

	protected void endControl() {
		this.setEnabled(false);
		token.endAnimation();
	}
}

