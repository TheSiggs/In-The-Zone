package inthezone.game.battle;

import inthezone.battle.Character;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HUD extends AnchorPane {
	private FlowPane characterInfoBoxes = new FlowPane();
	private Button endTurnButton = new Button("End turn");
	private Button itemsButton = new Button("Items");
	private Button abilitiesButton = new Button("Abilities");

	public final Map<Integer, CharacterInfoBox> characters = new HashMap<>();

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

	public void selectCharacter(Character c) {
		int id = c == null? -1 : c.id;
		for (CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	public void init(BattleView view, Collection<Character> characters) {
		for (Character c : characters) {
			CharacterInfoBox box = new CharacterInfoBox(c);
			box.setOnMouseClicked(event -> {
				view.selectCharacterById(c.id);
				selectCharacter(c);
			});
			this.characters.put(c.id, box);
			characterInfoBoxes.getChildren().add(box);
		}
	}

}

