/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.jfx;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
//import javax.swing.AbstractButton;
import nz.dcoder.inthezone.OldMain;
import javafx.scene.layout.Pane;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextFlow;
//import nz.dcoder.inthezone.Main;

/**
 * FXML Controller class
 *
 * @author denz
 */
public class MainHUDController implements Initializable {

    public static OldMain app;
    @FXML
    public Canvas canvas;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* 
         GraphicsContext ctx = canvas.getGraphicsContext2D();
         ctx.setFill(new Color(1.0, 0.0, 0.0, 1.0));
         ctx.rect(10, 10, 50, 50);
         ctx.fill();
         */
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        app.enqueue(new Callable<OldMain>() {

            @Override
            public OldMain call() throws Exception {
                app.someMethod();
                return app;
            }
        });
    }

    @FXML
    private ProgressBar health;
    
    public void setHealthValue(double value) {
        health.setProgress(value);
        
    }

    @FXML
    private void healthUp(ActionEvent event) {
        getHealth().setProgress(getHealth().getProgress() + 0.1);

        System.out.println("Health Up!");
    }

    @FXML
    private void healthDown(ActionEvent event) {
        getHealth().setProgress(getHealth().getProgress() - 0.1);

        if (getHealth().getProgress() == 0) {
            System.out.println("This Character is Dead!");
        }
        System.out.println("Health Down!");
    }
    
    @FXML
    private ProgressBar movePoints;
    
    public void setMovePointValue(double value) {
        movePoints.setProgress(value);
    }
        
    public synchronized void decreaseMovementPoints(double value, double total) {
       getMovePoints().setProgress(getMovePoints().getProgress() - 0.1);
       
       if (getMovePoints().getProgress() == 0) {
           System.out.println("This Character cannot Move!");
       }
       System.out.println("This Character has Moved!");
    }

    /**
     * @return movement points
     */
    public ProgressBar getMovePoints(){
        return movePoints;
    }
    /**
     * @param movePoints the movePoints to set
     */
    public void setMovePoints(ProgressBar movePoints) {
        this.movePoints = movePoints;
    }
    
    /**
     * @return the health
     */
    public ProgressBar getHealth() {
        return health;
    }
    /**
     * @param health the health to set
     */
    public void setHealth(ProgressBar health) {
        this.health = health;
    }
    

    @FXML
    private Button AttackButton = new Button();
    @FXML
    private Pane AttackMenu = new Pane();
    @FXML
    private Button MagicButton = new Button();
    @FXML
    private Pane MagicMenu = new Pane();

    /**
     * Show Attack Menu
     */
    public void showAttackMenu() {
       AttackMenu.setVisible(true);
     //  MagicMenu.setVisible(false);
    }
    public void hideAttackMenu() {
       AttackMenu.setVisible(false);
      // MagicMenu.setVisible(false);
    } 
    /**
     * Show Magic Menu
     */   
    public void showMagicMenu() {
        MagicMenu.setVisible(true);
       // AttackMenu.setVisible(false);
    }
    public void hideMagicMenu() {
        MagicMenu.setVisible(false);
       // AttackMenu.setVisible(false);
    }
    
    
}
