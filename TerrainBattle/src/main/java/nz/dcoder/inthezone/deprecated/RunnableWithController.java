/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.deprecated;

import com.jme3x.jfx.JmeFxScreenContainer;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import nz.dcoder.inthezone.OldMain;
import nz.dcoder.inthezone.jfx.MainHUDController;

/**
 *
 * @author denz
 */
public class RunnableWithController implements Runnable {

	private MainHUDController controller;
	private Scene scene;
	private JmeFxScreenContainer jmeFx;
	private OldMain app;

	public RunnableWithController(JmeFxScreenContainer jmeFx, OldMain app) {
		this.jmeFx = jmeFx;
		this.app = app;
	}

	@Override
	public void run() {
		try {
			URL location = (new MainHUDController()).getClass().getResource("mainHUD.fxml");

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(location);
			fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

			Parent root1 = (Parent) fxmlLoader.load(location.openStream());

			setScene(new Scene(root1, 300, 275));
			MainHUDController.app = getApp();
			getScene().setFill(Color.TRANSPARENT);
			getJmeFx().setScene(getScene(), root1);
			controller = fxmlLoader.getController();
			this.getController().getHealth().setProgress(1.0);
		} catch (IOException ex) {
			Logger.getLogger(OldMain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @return the controller
	 */
	public MainHUDController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(MainHUDController controller) {
		this.controller = controller;
	}

	/**
	 * @return the scene
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * @param scene the scene to set
	 */
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	/**
	 * @return the jmeFx
	 */
	public JmeFxScreenContainer getJmeFx() {
		return jmeFx;
	}

	/**
	 * @param jmeFx the jmeFx to set
	 */
	public void setJmeFx(JmeFxScreenContainer jmeFx) {
		this.jmeFx = jmeFx;
	}

	/**
	 * @return the app
	 */
	public OldMain getApp() {
		return app;
	}

	/**
	 * @param app the app to set
	 */
	public void setApp(OldMain app) {
		this.app = app;
	}
}
