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
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.dcoder.inthezone.jfx.MainHUDController;
import nz.dcoder.inthezone.Main;

/**
 *
 * @author inthezone
 */
public class UserInterface {
	public UserInterface(Main game) {
		Node guiNode = game.getGuiNode();

		JmeFxScreenContainer jmeFx = JmeFxContainer.install(game, guiNode, true, null);
		Platform.runLater(new LaunchJavaFxGUI(jmeFx, game));
		guiNode.attachChild(jmeFx.getJmeNode());
	}
}

/**
 * A runnable to launch the GUI, with facilities to get back a reference to the
 * GUI's controller object (and the Scene in case we need it).
 * */
class LaunchJavaFxGUI implements Runnable {
	private MainHUDController controller;
	private Scene scene;
	private JmeFxScreenContainer jmeFx;
	private Main app;

	public LaunchJavaFxGUI(JmeFxScreenContainer jmeFx, Main app) {
		this.jmeFx = jmeFx;
		this.app = app;
	}

	/**
	 * Synchronized to avoid race conditions with getController and getScene.
	 * */
	@Override public synchronized void run() {
		try {
			URL location = MainHUDController.class.getResource("mainHUD.fxml");

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

			Parent root = (Parent) fxmlLoader.load(location.openStream());

			this.scene = new Scene(root, 300, 275);
			MainHUDController.app = this.app;
			scene.setFill(Color.TRANSPARENT);
			jmeFx.setScene(getScene(), root);

			this.controller = fxmlLoader.getController();

			// controller.getHealth().setProgress(1.0);

		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @return the controller
	 */
	public synchronized MainHUDController getController() {
		return controller;
	}

	/**
	 * @return the scene
	 */
	public synchronized Scene getScene() {
		return scene;
	}
}

