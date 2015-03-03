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
import javafx.scene.layout.Pane;
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

    @FXML private ProgressBar hp;
    @FXML private ProgressBar ap;
    @FXML private ProgressBar mp;
		@FXML private Label hpLabel;
		@FXML private Label apLabel;
		@FXML private Label mpLabel;
		@FXML private TilePane topMenu;
		@FXML private TilePane attackMenu;
		@FXML private TilePane magicMenu;
		@FXML private Pane selectedCharacter;

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

		CharacterName currentCharacter = null;

		private void setPoints(ProgressBar bar, Label label, Points points) {
			bar.setProgress((double) points.total / (double) points.max);
			if (label != null) label.setText(points.toString());
		}

		public void selectCharacter(CharacterInfo info) {
			currentCharacter = info.name;
			setPoints(hp, hpLabel, info.hp);
			setPoints(ap, apLabel, info.ap);
			setPoints(mp, mpLabel, info.mp);

			selectedCharacter.setVisible(true);
			topMenu.setVisible(true);
		}

		public void deselectCharacter() {
			selectedCharacter.setVisible(false);
			topMenu.setVisible(false);
		}

		public void updateMP(CharacterName name, Points points) {
			if (name.equals(currentCharacter)) {
				setPoints(mp, mpLabel, points);
			}
		}

		public void updateAP(CharacterName name, Points points) {
			if (name.equals(currentCharacter)) {
				setPoints(ap, apLabel, points);
			}
		}
		
		public void updateHP(CharacterName name, Points points) {
			if (name.equals(currentCharacter)) {
				setPoints(hp, hpLabel, points);
			}
		}
}

