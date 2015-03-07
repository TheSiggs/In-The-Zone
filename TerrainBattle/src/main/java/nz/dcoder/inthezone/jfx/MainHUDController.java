/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.dcoder.inthezone.jfx;

import com.jme3x.jfx.JmeFxContainer;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.Node;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.text.Text;

import nz.dcoder.inthezone.data_model.pure.AbilityClass;
import nz.dcoder.inthezone.data_model.pure.AbilityInfo;
import nz.dcoder.inthezone.data_model.pure.AbilityName;
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
	private static final int menuWidth = 160;

	@FXML public Canvas canvas;

	// a stack of sub menus to simplify the implementation of the back buttons.
	// Prepend to the start of the list to add a new menu to the stack.  Remove
	// from the start of the list to pop menus off the stack.
	Pane currentMenu = null;
	private List<Pane> subMenus = new LinkedList<>();

	private GameActionListener.GUIListener input;
	public void setGameInput(GameActionListener.GUIListener input) {
		this.input = input;
	}

	private JmeFxContainer jmeFx = null;
	public void setContainer(JmeFxContainer jmeFx) {
		this.jmeFx = jmeFx;
	}

	/**
	* Initializes the controller class.
	*/
	@Override public void initialize(URL url, ResourceBundle rb) {
		currentMenu = topTopMenu;
	}

	private void pushMenu(Pane menu) {
		subMenus.add(0, currentMenu);
		currentMenu.setVisible(false);
		currentMenu = menu;
		currentMenu.setVisible(true);
	}

	private void hideMenus() {
		currentMenu.setVisible(false);
		for (Pane m : subMenus) {
			m.setVisible(false);
			currentMenu = m;
		}
		currentMenu.setVisible(true);  // always leave one visible menu
		subMenus.clear();
	}

	@FXML private ProgressBar hp;
	@FXML private ProgressBar ap;
	@FXML private ProgressBar mp;
	@FXML private Label hpLabel;
	@FXML private Label apLabel;
	@FXML private Label mpLabel;
        @FXML private Text textPrompt;
	@FXML private TilePane topTopMenu;
	@FXML private TilePane topMenu;
	@FXML private TilePane attackMenu;
	@FXML private TilePane magicMenu;
	@FXML private TilePane itemMenu;
	@FXML private Pane selectedCharacter;

	@FXML private Button attackExit;
	@FXML private Button magicExit;
	@FXML private Button itemExit;

	@FXML private Button noAttacks;
	@FXML private Button noMagic;
	@FXML private Button noItems;

	@FXML protected void onMoveButton(ActionEvent event) {
		input.notifyMove();                 
	}
        
	AbilityName pushAbility = new AbilityName("push");
	@FXML protected void onPushButton(ActionEvent event) {
		input.notifyTarget(pushAbility, 1);
		jmeFx.loseFocus();
	}

	@FXML protected void onAttackButton(ActionEvent event) {
		pushMenu(attackMenu);
		jmeFx.loseFocus();
	}

	@FXML protected void onMagicButton(ActionEvent event) {
		pushMenu(magicMenu);
		jmeFx.loseFocus();
	}

	@FXML protected void onItemButton(ActionEvent event) {
		pushMenu(itemMenu);
		jmeFx.loseFocus();
	}

	@FXML protected void onEndButton(ActionEvent event) {
		input.notifyEndTurn();
		jmeFx.loseFocus();
	}

	@FXML protected void onBackButton(ActionEvent event) {
		if (subMenus.size() > 0) {
			currentMenu.setVisible(false);
			currentMenu = subMenus.remove(0);
			currentMenu.setVisible(true);
		}
		jmeFx.loseFocus();
	}

	public void turnStart(
		boolean isPlayerTurn,
		Collection<CharacterInfo> players,
		Collection<CharacterInfo> npcs
	) {
		selectedCharacter.setVisible(false);
		hideMenus();
	}

	CharacterName currentCharacter = null;

	private void setPoints(ProgressBar bar, Label label, Points points) {
		bar.setProgress((double) points.total / (double) points.max);
		if (label != null) label.setText(points.toString());
	}

	public void selectCharacter(CharacterInfo info) {
		deselectCharacter();

		currentCharacter = info.name;
		setPoints(hp, hpLabel, info.hp);
		setPoints(ap, apLabel, info.ap);
		setPoints(mp, mpLabel, info.mp);
		rebuildMenus(info.abilities);

		selectedCharacter.setVisible(true);
		pushMenu(topMenu);
	}

	public void deselectCharacter() {
		selectedCharacter.setVisible(false);
		hideMenus();
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

	private void rebuildMenus(Collection<AbilityInfo> abilities) {
		ObservableList<Node> attack = attackMenu.getChildren();
		ObservableList<Node> magic = magicMenu.getChildren();

		attack.clear();
		magic.clear();

		for (AbilityInfo ability : abilities) {
			Button b = new Button(ability.name.toString() + " (AP " + ability.cost + ")");
			b.setMnemonicParsing(false);
			b.setOnAction(event -> {
				input.notifyTarget(ability.name, ability.repeats);
				jmeFx.loseFocus();
			});
			b.setPrefWidth(menuWidth);

			b.setTooltip(new Tooltip(
				"Range: " + ability.range +
				", Area of effect: " + ability.areaOfEffect));

			if (ability.aClass == AbilityClass.PHYSICAL) {
				attack.add(b);
			} else if (ability.aClass == AbilityClass.MAGICAL) {
				magic.add(b);
			}
		}

		if (attack.size() == 0) attack.add(noAttacks);
		if (magic.size() == 0) magic.add(noMagic);

		attack.add(attackExit);
		magic.add(magicExit);
	}   
        
        public void textPrompt(CharacterName name, Points points) { 
            //JLabel characterSelected = new JLabel(currentCharacter + "Has been selected!", JLabel.BOTTOM);
            //JTextField characterSelected = new JTextField(currentCharacter + "Has been selected!");
            //JTextField characterHP = new JTextField(currentCharacter + "Now has " + hp + " health left!");
            //JTextField characterPush = new JTextField(currentCharacter + "Used " + pushAbility + "!");
            
            if (name.equals (currentCharacter) ) {
                textPrompt.setText(currentCharacter + "Has been selected!");    
            }            
        }
}

