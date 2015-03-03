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
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.graphics.Graphics;
import nz.dcoder.inthezone.input.GameActionListener;
import nz.dcoder.inthezone.jfx.MainHUDController;
import nz.dcoder.inthezone.Main;

/**
 *
 * @author inthezone
 */
public class UserInterface {
	final GameActionListener input;
	final MainHUDController controller;

	public UserInterface(Main game, Graphics g) {
		this.input = new GameActionListener(game.getInputManager(), g, this);

		Node guiNode = game.getGuiNode();

		JmeFxScreenContainer jmeFx = JmeFxContainer.install(game, guiNode, true, null);
		guiNode.attachChild(jmeFx.getJmeNode());

		// The CompletableFuture allows us to safely move a single piece of data
		// between two threads
		CompletableFuture<MainHUDController> hud = new CompletableFuture<>();

		LaunchJavaFxGUI launcher = new LaunchJavaFxGUI(jmeFx, input, hud);
		Platform.runLater(launcher);
		try {
			this.controller = hud.get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new RuntimeException(
				"Failed to create GUI because: " + e.getMessage(), e);
		}

	}

	public GameActionListener getGameActionListener() {
		return input;
	}

	public void turnStart(
		boolean isPlayerTurn,
		Collection<CharacterInfo> players,
		Collection<CharacterInfo> npcs
	) {
		Platform.runLater(() -> controller.turnStart(isPlayerTurn, players, npcs));
	}

	public void selectCharacter(CharacterInfo info) {
		Platform.runLater(() -> controller.selectCharacter(info));
	}

	public void deselectCharacter() {
		Platform.runLater(() -> controller.deselectCharacter());
	}

	public void updateMP(CharacterName name, Points mp) {
		Platform.runLater(() -> controller.updateMP(name, mp));
	}

	public void updateAP(CharacterName name, Points ap) {
		Platform.runLater(() -> controller.updateAP(name, ap));
	}
	
	public void updateHP(CharacterName name, Points hp) {
		Platform.runLater(() -> controller.updateHP(name, hp));
	}
}

/**
 * A runnable to launch the GUI, with facilities to get back a reference to the
 * GUI's controller object (and the Scene in case we need it).
 * */
class LaunchJavaFxGUI implements Runnable {
	private final JmeFxScreenContainer jmeFx;
	private final GameActionListener input;

	private final CompletableFuture<MainHUDController> hud;

	public LaunchJavaFxGUI(
		JmeFxScreenContainer jmeFx,
		GameActionListener input,
		CompletableFuture<MainHUDController> hud
	) {
		this.jmeFx = jmeFx;
		this.input = input;
		this.hud = hud;
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

			Scene scene = new Scene(root, 300, 275);
			scene.setFill(Color.TRANSPARENT);
			jmeFx.setScene(scene, root);

			MainHUDController controller = fxmlLoader.getController();
			controller.setGameInput(input);
			hud.complete(controller);

		} catch (IOException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			// cancel returning the HUD controller if it hasn't already been returned
			hud.cancel(true);
		}
	}
}

