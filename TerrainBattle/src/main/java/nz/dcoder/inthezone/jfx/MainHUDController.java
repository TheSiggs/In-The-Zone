/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.jfx;

import com.jme3.app.SimpleApplication;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import nz.dcoder.inthezone.Main;

/**
 * FXML Controller class
 *
 * @author denz
 */
public class MainHUDController implements Initializable {

    public static Main app;
    @FXML
    public Canvas canvas;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(new Color(1.0, 0.0, 0.0, 1.0));
        ctx.rect(10, 10, 50, 50);
        ctx.fill();
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        app.enqueue(new Callable<Main>() {

            @Override
            public Main call() throws Exception {
                app.someMethod();
                return app;
            }
        });
    }
    
    @FXML
    private ProgressBar health;

    @FXML
    private void healthUp(ActionEvent event) {
        health.setProgress(health.getProgress() + 0.1);

        System.out.println("Health Up");
    }

    @FXML
    private void healthDown(ActionEvent event) {
        health.setProgress(health.getProgress() - 0.1);

        System.out.println("Health Down");
    }
}
