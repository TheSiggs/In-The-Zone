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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterProfilePane extends VBox {
	private CharacterProfileModel profile = null;

	private final ObservableList<AbilityInfo> basicAbilitiesModel = FXCollections.observableArrayList();

	private final HBox abilitiesArea = new HBox();
	private final FlowPane toolbar = new FlowPane();

	private final Spinner<Integer> hp = new Spinner<>(0, 0, 0);
	private final Spinner<Integer> attack = new Spinner<>(0, 0, 0);
	private final Spinner<Integer> defence = new Spinner<>(0, 0, 0);

	private final Button addAbility = new Button("Use ability");
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

		toolbar.getChildren().addAll(
			new Label("HP"), hp,
			new Label("Attack"), attack,
			new Label("Defence"), defence);

		allAbilitiesPane.getChildren().addAll(addAbility, allAbilities);

		addAbility.setOnAction(event -> {
			AbilityInfo i = allAbilities.getSelectionModel().getSelectedItem();
			if (i != null && !profile.abilities.contains(i)) {
				profile.abilities.add(i);
			}
		});

		hp.setPrefWidth(100);
		attack.setPrefWidth(100);
		defence.setPrefWidth(100);

		selectedPane.getChildren().add(new HBox(new Label("Basic ability"), basicAbilities));
		selectedPane.getChildren().add(selectedAbilities);

		abilitiesArea.getChildren().addAll(selectedPane, allAbilitiesPane);
		this.getChildren().addAll(toolbar, abilitiesArea);
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

		hp.setValueFactory(new PPSpinnerFactory(c.hpPP,
			c.rootCharacter.stats.hp, c.rootCharacter.hpCurve));
		attack.setValueFactory(new PPSpinnerFactory(c.attackPP,
			c.rootCharacter.stats.attack, c.rootCharacter.attackCurve));
		defence.setValueFactory(new PPSpinnerFactory(c.defencePP,
			c.rootCharacter.stats.defence, c.rootCharacter.defenceCurve));

		// Force the spinners to update the display area
		hp.getEditor().setText(
			hp.getValueFactory().getConverter().toString(c.hpPP));
		attack.getEditor().setText(
			attack.getValueFactory().getConverter().toString(c.attackPP));
		defence.getEditor().setText(
			defence.getValueFactory().getConverter().toString(c.defencePP));
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

class PPSpinnerFactory extends SpinnerValueFactory<Integer> { 
	private final int[] values;

	public PPSpinnerFactory(int init, int base, List<Integer> curve) {

		values = new int[1 + curve.size()];
		values[0] = base;
		for (int i = 0; i < curve.size(); i++) values[i + 1] = curve.get(i);

		setConverter(new StringConverter<Integer>() {
			@Override public Integer fromString(String s) {
				final int raw = Integer.parseInt(s);
				for (int i = 0; i < values.length; i++) {
					if (values[i] == raw) return i;
				}
				return 0;
			}

			@Override public String toString(Integer i) {
				if (i < 0 || i >= values.length) {
					return "";
				} else {
					return "" + values[i];
				}
			}
		});

		setValue(init);
		setWrapAround(false);
	}

	@Override public void decrement(int steps) {
		final int v1 = getValue() - steps;
		setValue(v1 < 0? 0 : v1);
	}

	@Override public void increment(int steps) {
		final int v1 = getValue() + steps;
		setValue(v1 >= values.length? (values.length - 1) : v1);
	}
}

