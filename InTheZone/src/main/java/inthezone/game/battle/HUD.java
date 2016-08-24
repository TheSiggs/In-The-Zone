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
	private final FlowPane characterInfoBoxes = new FlowPane();
	private final Button endTurnButton = new Button("End turn");
	private final Button itemsButton = new Button("Items");
	private final Button abilitiesButton = new Button("Abilities");

	private final BattleView view;

	public final Map<Integer, CharacterInfoBox> characters = new HashMap<>();

	public HUD(BattleView view) {
		super();

		this.view = view;

		endTurnButton.setOnAction(event -> view.sendEndTurn());

		endTurnButton.disableProperty().bind(view.isMyTurn.not());
		itemsButton.disableProperty().bind(view.isMyTurn.not());
		abilitiesButton.disableProperty().bind(view.isMyTurn.not());

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

	public void init(Collection<Character> characters) {
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

