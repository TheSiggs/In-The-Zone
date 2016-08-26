package inthezone.game.battle;

import inthezone.battle.Ability;
import inthezone.battle.Character;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HUD extends AnchorPane {
	private final FlowPane characterInfoBoxes = new FlowPane();
	private final VBox endButtons = new VBox();
	private final Button endTurnButton = new Button("End turn");
	private final Button resignButton = new Button("Resign");
	private final Button itemsButton = new Button("Items");
	private final Button abilitiesButton = new Button("Abilities");
	private final MultiTargetAssistant multiTargetAssistant;

	private final ContextMenu abilitiesMenu = new ContextMenu();
	private final MenuItem attackItem = new MenuItem("Attack");
	private final MenuItem pushItem = new MenuItem("Push");

	private final BattleView view;

	public final Map<Integer, CharacterInfoBox> characters = new HashMap<>();

	public HUD(BattleView view) {
		super();

		this.view = view;
		this.multiTargetAssistant = new MultiTargetAssistant(view);

		attackItem.setOnAction(event -> view.useAttack());
		pushItem.setOnAction(event -> view.usePush());

		endTurnButton.setOnAction(event -> view.sendEndTurn());
		resignButton.setOnAction(event -> view.sendResign());

		endButtons.getChildren().addAll(endTurnButton, resignButton);

		abilitiesButton.setOnAction(event ->
			abilitiesMenu.show(abilitiesButton, Side.TOP, 0, 0));

		endTurnButton.disableProperty().bind(view.isMyTurn.not());
		resignButton.disableProperty().bind(view.isMyTurn.not());
		itemsButton.disableProperty().bind(view.isMyTurn.not()
			.or(view.isCharacterSelected.not()));
		abilitiesButton.disableProperty().bind(view.isMyTurn.not()
			.or(view.isCharacterSelected.not()));

		AnchorPane.setTopAnchor(characterInfoBoxes, 0d);
		AnchorPane.setLeftAnchor(characterInfoBoxes, 0d);

		AnchorPane.setTopAnchor(endButtons, 0d);
		AnchorPane.setRightAnchor(endButtons, 0d);

		AnchorPane.setBottomAnchor(abilitiesButton, 0d);
		AnchorPane.setRightAnchor(abilitiesButton, 0d);

		AnchorPane.setBottomAnchor(itemsButton, 0d);
		AnchorPane.setLeftAnchor(itemsButton, 0d);

		AnchorPane.setBottomAnchor(multiTargetAssistant, 0d);
		AnchorPane.setLeftAnchor(multiTargetAssistant, 200d);

		this.getChildren().addAll(
			characterInfoBoxes, endButtons,
			abilitiesButton, itemsButton, multiTargetAssistant
		);
	}

	public void selectCharacter(Character c) {
		int id = c == null? -1 : c.id;
		if (c != null) updateAbilities(c, c.hasMana());

		for (CharacterInfoBox box : characters.values())
			box.setSelected(box.id == id);
	}

	public void updateAbilities(Character c, boolean mana) {
		abilitiesMenu.getItems().clear();
		abilitiesMenu.getItems().addAll(attackItem, pushItem);

		for (Ability a : c.abilities) {
			final Ability ability = mana ? a.getMana() : a;

			MenuItem i = new MenuItem(ability.info.name);
			i.setDisable(ability.info.ap > c.getAP() || ability.info.mp > c.getMP());
			i.setOnAction(event -> view.useAbility(ability));
			abilitiesMenu.getItems().add(i);
		}
	}

	public void init(Collection<Character> characters) {
		for (Character c : characters) {
			CharacterInfoBox box = new CharacterInfoBox(c);
			box.setOnMouseClicked(event -> {
				view.selectCharacterById(c.id);
			});
			this.characters.put(c.id, box);
			characterInfoBoxes.getChildren().add(box);
		}
	}
}

