package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.game.RollerScrollPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.util.StringConverter;
import java.util.List;

public class CharacterProfilePane extends HBox {
	private final VBox leftSection = new VBox(4);
	private final HBox topSection = new HBox(4);
	private final VBox scrollAbilities;
	private final ListView<AbilityInfo> abilities = new ListView<>();

	private final AbilityDescriptionPanel descriptionPanel =
		new AbilityDescriptionPanel();
	private final Button addAbility = new Button("Add ability");

	private final VBox stats = new VBox(4);
	private final Spinner<Integer> hp = new Spinner<>();
	private final Spinner<Integer> attack = new Spinner<>();
	private final Spinner<Integer> defence = new Spinner<>();

	private final ComboBox<AbilityInfo> basicAbility = new ComboBox<>();
	private final AbilityDescriptionPanel basicDescriptionPanel =
		new AbilityDescriptionPanel();

	private final StackPane portraitWrapper = new StackPane();
	private final ImageView portrait = new ImageView();

	private final RollerScrollPane bottomScroll;
	private final HBox bottomSection = new HBox(10);

	private CharacterProfileModel model = null;

	private final static double descriptionWidth = 320;
	private final static double abilitiesListWidth = 200;
	private final static double abilitiesListHeight = 520;
	private final static double basicsWidth = 280;

	public CharacterProfilePane() {
		scrollAbilities = new VBox(abilities);

		scrollAbilities.getStyleClass().add("panel");
		scrollAbilities.setPrefWidth(abilitiesListWidth);
		scrollAbilities.setPrefHeight(abilitiesListHeight);
		scrollAbilities.setMinWidth(abilitiesListWidth);
		abilities.getStyleClass().add("gui-list");

		bottomScroll = new RollerScrollPane(bottomSection, true);

		descriptionPanel.setMinWidth(descriptionWidth);
		descriptionPanel.setMaxWidth(descriptionWidth);

		addAbility.getStyleClass().add("gui-button");
		addAbility.setId("add-ability-button");
		addAbility.setFocusTraversable(false);
		descriptionPanel.getChildren().add(addAbility);

		basicAbility.getStyleClass().add("gui-combo");
		basicAbility.setId("basic-ability");
		basicAbility.setMaxWidth(Double.MAX_VALUE);
		basicAbility.setConverter(new StringConverter<AbilityInfo>() {
			@Override public AbilityInfo fromString(String string) {
				return null;
			}

			@Override public String toString(AbilityInfo info) {
				return "Basic ability:\n" + info.toString();
			}
		});

		basicDescriptionPanel.setId("basic-description");
		basicDescriptionPanel.setMinWidth(basicsWidth);
		basicDescriptionPanel.setMaxWidth(basicsWidth);

		stats.getChildren().addAll(hp, attack, defence,
			basicAbility, basicDescriptionPanel);
		VBox.setVgrow(basicDescriptionPanel, Priority.ALWAYS);

		topSection.getChildren().addAll(scrollAbilities, descriptionPanel, stats);

		HBox.setHgrow(bottomScroll, Priority.ALWAYS);

		hp.getStyleClass().addAll("panel", "gui-spinner");
		attack.getStyleClass().addAll("panel", "gui-spinner");
		defence.getStyleClass().addAll("panel", "gui-spinner");

		hp.setMaxWidth(Double.MAX_VALUE);
		attack.setMaxWidth(Double.MAX_VALUE);
		defence.setMaxWidth(Double.MAX_VALUE);
		stats.setFillWidth(true);

		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		leftSection.getChildren().addAll(spacer1, topSection, bottomScroll, spacer2);

		portraitWrapper.getChildren().add(portrait);

		HBox.setHgrow(portraitWrapper, Priority.ALWAYS);
		this.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().addAll(leftSection, portraitWrapper);

		abilities.getSelectionModel().selectedItemProperty()
			.addListener((v, o, n) -> descriptionPanel.setAbility(n));

		basicAbility.getSelectionModel().selectedItemProperty()
			.addListener((v, o, n) -> basicDescriptionPanel.setAbility(n));

		addAbility.setOnAction(event -> {
			final AbilityInfo a = abilities.getSelectionModel().getSelectedItem();
			if (a != null && !model.abilities.contains(a)) {
				model.abilities.add(a);
				bottomSection.getChildren().add(new AbilityButton(
					a, true, bottomSection.getChildren(), model.abilities));
			}
		});
	}

	public void setProfile(CharacterProfileModel model) {
		if (this.model != null) this.model.unbindAll();

		this.model = model;
		final CharacterProfile profile = model.profileProperty().get();
		final CharacterInfo info = profile.rootCharacter;

		portrait.setImage(profile.rootCharacter.bigPortrait);
		portrait.setPreserveRatio(true);
		portrait.setFitHeight(720);

		final ObservableList<AbilityInfo> abilitiesList =
			FXCollections.<AbilityInfo>observableArrayList();
		final ObservableList<AbilityInfo> basicsList =
			FXCollections.<AbilityInfo>observableArrayList();

		for (AbilityInfo a : info.abilities) {
			if (a.type == AbilityType.BASIC) {
				basicsList.add(a);
			} else if (a.type != AbilityType.SPECIAL) {
				abilitiesList.add(a);
			}
		}

		abilities.setItems(abilitiesList);
		abilities.getSelectionModel().select(0);

		basicAbility.setItems(basicsList);
		basicAbility.getSelectionModel().select(model.basicAbility.get());
		model.basicAbility.bind(
			basicAbility.getSelectionModel().selectedItemProperty());

		abilities.setPrefHeight(abilitiesList.size() * 30 + 6);
		scrollAbilities.layout();

		hp.setValueFactory(new PPSpinnerFactory("Health: ",
			profile.hpPP, info.stats.hp, info.hpCurve));
		attack.setValueFactory(new PPSpinnerFactory("Attack: ",
			profile.attackPP, info.stats.attack, info.attackCurve));
		defence.setValueFactory(new PPSpinnerFactory("Defence: ",
			profile.defencePP, info.stats.defence, info.defenceCurve));

		model.hpPP.bind(hp.valueProperty());
		model.attackPP.bind(attack.valueProperty());
		model.defencePP.bind(defence.valueProperty());

		bottomSection.getChildren().clear();
		for (AbilityInfo a : profile.abilities) {
			final boolean removable =
				a.type != AbilityType.BASIC &&
				a.type != AbilityType.SPECIAL;
			bottomSection.getChildren().add(new AbilityButton(
				a, removable, bottomSection.getChildren(), model.abilities));
		}
	}
}

class AbilityButton extends AnchorPane {
	private final StackPane imageWrapper = new StackPane();
	private final ImageView image = new ImageView();
	private final Button remove = new Button();

	public AbilityButton(
		AbilityInfo a, boolean removable,
		ObservableList<Node> abilityButtons,
		ObservableList<AbilityInfo> abilities
	) {
		this.getStyleClass().add("ability-button");
		this.setMinWidth(60);
		this.setMinHeight(60);
		image.setImage(a.icon);
		imageWrapper.getChildren().add(image);
		AnchorPane.setTopAnchor(imageWrapper, 0d);
		AnchorPane.setBottomAnchor(imageWrapper, 0d);
		AnchorPane.setLeftAnchor(imageWrapper, 0d);
		AnchorPane.setRightAnchor(imageWrapper, 0d);

		AnchorPane.setTopAnchor(remove, 0d);
		AnchorPane.setRightAnchor(remove, 0d);

		this.getChildren().add(imageWrapper);

		if (removable) {
			this.getChildren().add(remove);
			remove.setOnAction(event -> {
				abilityButtons.remove(this);
				abilities.remove(a);
			});
		}
	}
}

class PPSpinnerFactory extends SpinnerValueFactory<Integer> { 
	private final int[] values;

	public PPSpinnerFactory(
		String prefix, int init, int base, List<Integer> curve
	) {

		values = new int[1 + curve.size()];
		values[0] = base;
		for (int i = 0; i < curve.size(); i++) values[i + 1] = curve.get(i);

		setConverter(new StringConverter<Integer>() {
			@Override public Integer fromString(String s) {
				final int raw = Integer.parseInt(s.substring(prefix.length()));
				for (int i = 0; i < values.length; i++) {
					if (values[i] == raw) return i;
				}
				return 0;
			}

			@Override public String toString(Integer i) {
				return prefix + ((i < 0 || i >= values.length)? "" : values[i]);
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

