package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
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
import javafx.scene.control.TextArea;
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
	private final TextArea description = new TextArea("<no ability selected>");
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

		allAbilities.setEditable(false);
		allAbilitiesPane.getChildren().addAll(addAbility, allAbilities, description);

		description.setEditable(false);
		description.setWrapText(true);
		description.setPrefRowCount(3);
		description.setPrefWidth(280);

		allAbilities.setPlaceholder(new Label("No character selected"));
		selectedAbilities.setPlaceholder(new Label("No abilities chosen"));

		allAbilities.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
			if (n == null) {
				description.setText("<no ability selected>");
			} else {
				description.setText(new AbilityDescription(n).toString());
			}
		});

		addAbility.setOnAction(event -> addAbility());

		addAbility.disableProperty().bind(allAbilities.getSelectionModel()
			.selectedItemProperty().isNull());

		allAbilities.setOnMouseClicked(event -> {
			if (event.getClickCount() > 1) addAbility();
		});

		hp.setPrefWidth(100);
		attack.setPrefWidth(100);
		defence.setPrefWidth(100);

		selectedPane.getChildren().add(new HBox(new Label("Basic ability"), basicAbilities));
		selectedPane.getChildren().add(selectedAbilities);

		abilitiesArea.getChildren().addAll(selectedPane, allAbilitiesPane);
		this.getChildren().addAll(toolbar, abilitiesArea);
	}

	private void addAbility() {
		AbilityInfo i = allAbilities.getSelectionModel().getSelectedItem();
		if (i != null && !profile.abilities.contains(i)) {
			profile.abilities.add(i);
		}
	}

	public void setCharacterProfile(CharacterProfileModel profile) {
		if (this.profile != null) this.profile.unbindAll();

		this.profile = profile;

		if (profile == null) {
			allAbilities.setItems(null);
		} else {
			allAbilities.setItems(FXCollections.observableArrayList(
				profile.rootCharacter.abilities.stream()
					.filter(a -> !a.banned && a.type != AbilityType.BASIC)
					.collect(Collectors.toList())));
		}

		basicAbilitiesModel.clear();
		selectedAbilities.setItems(null);

		if (profile != null) {
			for (AbilityInfo a : profile.rootCharacter.abilities)
				if (a.type == AbilityType.BASIC) basicAbilitiesModel.add(a);
			basicAbilities.getSelectionModel().select(profile.basicAbility.getValue());
			profile.basicAbility.bind(
				basicAbilities.getSelectionModel().selectedItemProperty());

			selectedAbilities.setItems(profile.abilities);
			selectedAbilities.setCellFactory(
				RemovableAbilityCell.forListView(profile.abilities));

			hp.setValueFactory(new PPSpinnerFactory(profile.hpPP.getValue(),
				profile.rootCharacter.stats.hp, profile.rootCharacter.hpCurve));
			attack.setValueFactory(new PPSpinnerFactory(profile.attackPP.getValue(),
				profile.rootCharacter.stats.attack, profile.rootCharacter.attackCurve));
			defence.setValueFactory(new PPSpinnerFactory(profile.defencePP.getValue(),
				profile.rootCharacter.stats.defence, profile.rootCharacter.defenceCurve));

			// Force the spinners to update the display area
			hp.getEditor().setText(
				hp.getValueFactory().getConverter().toString(profile.hpPP.getValue()));
			attack.getEditor().setText(
				attack.getValueFactory().getConverter().toString(profile.attackPP.getValue()));
			defence.getEditor().setText(
				defence.getValueFactory().getConverter().toString(profile.defencePP.getValue()));

			profile.hpPP.bind(hp.valueProperty());
			profile.attackPP.bind(attack.valueProperty());
			profile.defencePP.bind(defence.valueProperty());
		}
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
	}

	public static Callback<ListView<AbilityInfo>, ListCell<AbilityInfo>>
		forListView(ObservableList<AbilityInfo> items)
	{
		return (listView -> new RemovableAbilityCell(items));
	}

	public RemovableAbilityCell(ObservableList<AbilityInfo> items) {
		this.setText(null);
		this.items = items;
	}

	@Override
	protected void updateItem(AbilityInfo item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			this.setGraphic(null);
		} else {
			if (cell == null) makeCell();
			this.setGraphic(cell);
			name.setText(item.name + (item.banned? "(BANNED)" : ""));
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
		int v0 = getValue() - steps;
		int v1 = getValue() - steps - 1;
		while (v1 >= 0 && values[v0] == values[v1]) {
			v0 -= 1;
			v1 -= 1;
		}
		setValue(v0 < 0? 0 : v0);
	}

	@Override public void increment(int steps) {
		final int v0 = getValue();
		int v1 = getValue() + steps;
		while (v1 < (values.length - 1) && values[v0] == values[v1]) v1 += 1;
		setValue(v1 >= values.length? (values.length - 1) : v1);
	}
}

