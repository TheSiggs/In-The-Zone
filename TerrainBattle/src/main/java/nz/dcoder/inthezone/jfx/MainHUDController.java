/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.jfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.TilePane;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Collection;

import nz.dcoder.inthezone.data_model.pure.CharacterInfo;
import nz.dcoder.inthezone.data_model.pure.CharacterName;
import nz.dcoder.inthezone.data_model.pure.Points;
import nz.dcoder.inthezone.input.GameActionListener;

/**
 * FXML Controller class
 *
 * @author denz
 */
public class MainHUDController implements Initializable {
    @FXML public Canvas canvas;

    /**
     * Initializes the controller class.
     */
    @Override public void initialize(URL url, ResourceBundle rb) {
    }

		private GameActionListener input;
		public void setGameInput(GameActionListener input) {
			this.input = input;
		}

		public void turnStart(
			boolean isPlayerTurn,
			Collection<CharacterInfo> players,
			Collection<CharacterInfo> npcs
		) {
		}

		public void selectCharacter(CharacterInfo info) {
		}

		public void deselectCharacter() {
		}

		public void updateMP(CharacterName name, Points mp) {
		}

		public void updateAP(CharacterName name, Points ap) {
		}
		
		public void updateHP(CharacterName name, Points hp) {
		}

    @FXML private ProgressBar hp;
    @FXML private ProgressBar ap;
    @FXML private ProgressBar mp;
		@FXML private Label hpLabel;
		@FXML private Label apLabel;
		@FXML private Label mpLabel;
		@FXML private TilePane topMenu;
		@FXML private TilePane AttackMenu;
		@FXML private TilePane MagicMenu;
    
}
