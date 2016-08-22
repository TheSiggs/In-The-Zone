package inthezone.game.battle;

import inthezone.battle.Character;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import java.util.Collection;

public class HUD extends AnchorPane {
	private FlowPane characterInfoBoxes = new FlowPane();
	private Button endTurnButton = new Button("End turn");
	private Button itemsButton = new Button("Items");
	private Button abilitiesButton = new Button("Abilities");

	public HUD() {
		super();

		AnchorPane.setTopAnchor(characterInfoBoxes, 0d);
		AnchorPane.setLeftAnchor(characterInfoBoxes, 0d);

		AnchorPane.setTopAnchor(endTurnButton, 0d);
		AnchorPane.setRightAnchor(endTurnButton, 0d);

		AnchorPane.setBottomAnchor(abilitiesButton, 0d);
		AnchorPane.setRightAnchor(abilitiesButton, 0d);

		AnchorPane.setBottomAnchor(itemsButton, 0d);
		AnchorPane.setLeftAnchor(itemsButton, 0d);

		this.getChildren().addAll(
			characterInfoBoxes, endTurnButton, abilitiesButton, itemsButton
		);
	}

	public void init(Collection<Character> characters) {
		for (Character c : characters) {
			characterInfoBoxes.getChildren().add(new CharacterInfoBox(c));
		}
	}

}

