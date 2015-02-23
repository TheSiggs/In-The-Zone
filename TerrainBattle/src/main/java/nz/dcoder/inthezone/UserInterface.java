/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone;

import com.jme3.scene.Node;
import com.jme3x.jfx.JmeFxContainer;
import com.jme3x.jfx.JmeFxScreenContainer;
import javafx.application.Platform;

import nz.dcoder.inthezone.concurrent.RunnableWithController;

/**
 *
 * @author denz
 */
public class UserInterface {
	private final RunnableWithController guiThread;

	public UserInterface(Main game) {
		Node guiNode = game.getGuiNode();

		JmeFxScreenContainer jmeFx = JmeFxContainer.install(game, guiNode, true, null);
		guiThread = new RunnableWithController(jmeFx, game);
		Platform.runLater(guiThread);
		guiNode.attachChild(guiThread.getJmeFx().getJmeNode());
	}
}

