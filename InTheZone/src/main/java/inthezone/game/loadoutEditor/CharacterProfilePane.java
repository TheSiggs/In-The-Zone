package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class CharacterProfilePane extends HBox{
	private CharacterProfileModel profile = null;

	private final ObservableList<AbilityInfo> basicAbilitiesModel = FXCollections.observableArrayList();
	private final ObservableList<AbilityInfo> selectedAbilitiesModel = FXCollections.observableArrayList();

	private final Button addAbility = new Button("Add ability");
	private final VBox allAbilitiesPane = new VBox();
	private final VBox selectedPane = new VBox();
	private final ListView<AbilityInfo> allAbilities = new ListView<>();
	private final ListView<AbilityInfo> selectedAbilities = new ListView<>(selectedAbilitiesModel);
	private final ComboBox<AbilityInfo> basicAbilities = new ComboBox<>(basicAbilitiesModel);

	public CharacterProfilePane() {
		super();

		allAbilitiesPane.getChildren().addAll(addAbility, allAbilities);

		addAbility.setOnAction(event -> {
			AbilityInfo i = allAbilities.getSelectionModel().getSelectedItem();
			if (i != null && !selectedAbilitiesModel.contains(i)) {
				selectedAbilitiesModel.add(i);
			}
		});

		selectedPane.getChildren().add(new HBox(new Label("Basic ability"), basicAbilities));
		selectedPane.getChildren().add(selectedAbilities);
		selectedAbilities.setCellFactory(
			RemovableAbilityCell.forListView(selectedAbilitiesModel));

		this.getChildren().addAll(selectedPane, allAbilitiesPane);
	}

	public void setCharacterProfile(CharacterProfile c) {
		profile = new CharacterProfileModel(c);

		allAbilities.setItems(FXCollections.observableArrayList(
			c.rootCharacter.abilities));

		basicAbilitiesModel.clear();
		for (AbilityInfo a : c.rootCharacter.abilities)
			if (a.type == AbilityType.BASIC) basicAbilitiesModel.add(a);
		basicAbilities.getSelectionModel().select(profile.basicAbility.getValue());

		selectedAbilitiesModel.clear();
		selectedAbilitiesModel.addAll(profile.abilities);
	}

	public CharacterProfile getCharacterProfile() {
		if (profile == null) return null;
			return profile.encodeProfile();
	}
}

class RemovableAbilityCell extends ListCell<AbilityInfo> {
	private final ObservableList<AbilityInfo> items;

	private final HBox cell = new HBox();
	private final Label name = new Label();
	private final Button remove = new Button("✕");

	public static Callback<ListView<AbilityInfo>, ListCell<AbilityInfo>>
		forListView(ObservableList<AbilityInfo> items)
	{
		return (listView -> new RemovableAbilityCell(items));
	}

	public RemovableAbilityCell(ObservableList<AbilityInfo> items) {
		this.items = items;
		cell.getChildren().addAll(name, remove);
		this.setGraphic(cell);
	}

	@Override
	protected void updateItem(AbilityInfo item, boolean empty) {
		super.updateItem(item, empty);

		if (!empty) {
			name.setText(item.name);
			remove.setOnAction(event -> items.remove(item));
		}
	}
}

