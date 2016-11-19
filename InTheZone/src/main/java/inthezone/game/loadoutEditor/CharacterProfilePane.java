package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterProfile;
import javafx.beans.property.ReadOnlyObjectProperty;
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
import java.util.stream.Collectors;

public class CharacterProfilePane extends HBox{
	private CharacterProfileModel profile = null;

	private final ObservableList<AbilityInfo> basicAbilitiesModel = FXCollections.observableArrayList();

	private final Button addAbility = new Button("Add ability");
	private final VBox allAbilitiesPane = new VBox();
	private final VBox selectedPane = new VBox();
	private final ListView<AbilityInfo> allAbilities = new ListView<>();
	private final ListView<AbilityInfo> selectedAbilities = new ListView<>();
	private final ComboBox<AbilityInfo> basicAbilities = new ComboBox<>(basicAbilitiesModel);

	public ReadOnlyObjectProperty<CharacterProfile> profileProperty() {
		return profile == null ? null : profile.profileProperty();
	}

	public CharacterProfilePane() {
		super();

		allAbilitiesPane.getChildren().addAll(addAbility, allAbilities);

		addAbility.setOnAction(event -> {
			AbilityInfo i = allAbilities.getSelectionModel().getSelectedItem();
			if (i != null && !profile.abilities.contains(i)) {
				profile.abilities.add(i);
			}
		});

		selectedPane.getChildren().add(new HBox(new Label("Basic ability"), basicAbilities));
		selectedPane.getChildren().add(selectedAbilities);

		this.getChildren().addAll(selectedPane, allAbilitiesPane);
	}

	public void setCharacterProfile(CharacterProfile c) {
		profile = new CharacterProfileModel(c);

		allAbilities.setItems(FXCollections.observableArrayList(
			c.rootCharacter.abilities.stream()
				.filter(a -> a.type != AbilityType.BASIC)
				.collect(Collectors.toList())));

		basicAbilitiesModel.clear();
		for (AbilityInfo a : c.rootCharacter.abilities)
			if (a.type == AbilityType.BASIC) basicAbilitiesModel.add(a);
		basicAbilities.getSelectionModel().select(profile.basicAbility.getValue());

		selectedAbilities.setItems(profile.abilities);
		selectedAbilities.setCellFactory(
			RemovableAbilityCell.forListView(profile.abilities));
	}
}

class RemovableAbilityCell extends ListCell<AbilityInfo> {
	private final ObservableList<AbilityInfo> items;

	private HBox cell = null;
	private final Label name = new Label();
	private final Button remove = new Button("✕");

	private void makeCell() {
		cell = new HBox();
		cell.getChildren().addAll(name, remove);
		this.setGraphic(cell);
	}

	public static Callback<ListView<AbilityInfo>, ListCell<AbilityInfo>>
		forListView(ObservableList<AbilityInfo> items)
	{
		return (listView -> new RemovableAbilityCell(items));
	}

	public RemovableAbilityCell(ObservableList<AbilityInfo> items) {
		this.items = items;
	}

	@Override
	protected void updateItem(AbilityInfo item, boolean empty) {
		super.updateItem(item, empty);

		if (!empty) {
			if (cell == null) makeCell();
			name.setText(item.name);
			remove.setOnAction(event -> items.remove(item));
		}
	}
}

