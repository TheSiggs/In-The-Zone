package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Stats;
import inthezone.game.RollerScrollPane;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CharacterIndicatorPane extends AnchorPane {
	private final LoadoutModel loadouts;

	private CharacterProfileModel profile = null;

	private final Label name = new Label("");
	private final Label hp = new Label("0");
	private final Label attack = new Label("0");
	private final Label defence = new Label("0");
	private final Label power = new Label("0");
	private final Label pp = new Label("0");
	private final GridPane info = new GridPane();
	private final ImageView portrait = new ImageView();

	private final ObjectProperty<CharacterProfileModel> selectedCharacter;

	private final Map<String, ImageView> abilities = new HashMap<>();
	private final RollerScrollPane abilitiesListContainer;
	private final HBox abilitiesList = new HBox(2);

	private final HBox switchButtons = new HBox();
	private final Button switchLeft = new Button(null,
		new ImageView(new Image("/gui_assets/arrow_left.png")));
	private final Button switchRight = new Button(null,
		new ImageView(new Image("/gui_assets/arrow_right.png")));

	public CharacterIndicatorPane(
		LoadoutModel loadouts, CharacterProfileModel profile,
		ObjectProperty<CharacterProfileModel> selectedCharacter
	) {
		this.loadouts = loadouts;

		this.selectedCharacter = selectedCharacter;
		this.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY))
				selectedCharacter.set(this.profile);
		});

		abilitiesList.setAlignment(Pos.CENTER_LEFT);
		abilitiesListContainer = new RollerScrollPane(abilitiesList, true);
		abilitiesListContainer.setScrollWheelEnable(false);

		setupCharacterProfile(profile);
	}

	private void setupCharacterProfile(CharacterProfileModel profile) {
		this.getChildren().removeAll();

		this.profile = profile;

		this.getStyleClass().add("panel");
		name.getStyleClass().add("character-indicator-panel-title");
		abilitiesListContainer.getStyleClass().add("opaque-roller-scroller");

		name.setText(profile.rootCharacter.name);

		info.setVgap(2); info.setHgap(2);
		info.add(name, 0, 0, 2, 1);
		info.add(new Label("HP:"),      0, 2);
		info.add(new Label("Attack:"),  0, 3);
		info.add(new Label("Defence:"), 0, 4);
		info.add(new Label("Power:"),   0, 5);
		info.add(new Label("Cost:"),    0, 6);

		info.add(hp,      1, 2);
		info.add(attack,  1, 3);
		info.add(defence, 1, 4);
		info.add(power,   1, 5);
		info.add(pp,      1, 6);

		switchButtons.getChildren().addAll(switchLeft, switchRight);
		switchButtons.getStyleClass().add("switch-buttons");

		switchLeft.setOnAction(event -> {
			updateProfileModel(loadouts.substituteLeft(this.profile));
			selectedCharacter.set(this.profile);
		});

		switchRight.setOnAction(event -> {
			updateProfileModel(loadouts.substituteRight(this.profile));
			selectedCharacter.set(this.profile);
		});

		AnchorPane.setTopAnchor(switchButtons, 10d);
		AnchorPane.setRightAnchor(switchButtons, 10d);

		AnchorPane.setTopAnchor(info, 10d);
		AnchorPane.setLeftAnchor(info, 10d);

		AnchorPane.setTopAnchor(portrait, 10d);
		AnchorPane.setRightAnchor(portrait, 0d);

		AnchorPane.setBottomAnchor(abilitiesListContainer, 0d);
		AnchorPane.setLeftAnchor(abilitiesListContainer, 0d);
		AnchorPane.setRightAnchor(abilitiesListContainer, 0d);

		this.getChildren().addAll(portrait, info,
			switchButtons, abilitiesListContainer);

		updateProfileModel(profile);
	}

	private void updateProfileModel(CharacterProfileModel profile) {
		this.profile = profile;
		profile.setProfileUpdateReceiver(this::updateProfile);
		pp.textProperty().bind(StringExpression.stringExpression(profile.costProperty()));
		updateProfile(profile.profileProperty().getValue());
	}

	private void updateProfile(CharacterProfile profile) {
		if (profile == null) return;

		Stats baseStats = profile.getBaseStats();
		this.name.setText(profile.rootCharacter.name);
		this.power.setText("" + baseStats.power);
		this.hp.setText("" + baseStats.hp);
		this.attack.setText("" + baseStats.attack);
		this.defence.setText("" + baseStats.defence);

		abilities.clear();
		abilitiesList.getChildren().clear();
		for (AbilityInfo a : profile.allAbilities()) {
			final ImageView i = new ImageView(a.media.icon);
			final Tooltip t = new Tooltip((new AbilityDescription(a)).toString());
			t.setWrapText(true);
			t.setMaxWidth(300);
			Tooltip.install(i, t);

			abilities.put(a.name, i);
			abilitiesList.getChildren().add(i);
		}
		abilitiesListContainer.layout();
		abilitiesListContainer.setScrollPos(abilitiesListContainer.getScrollMin());

		abilitiesList.getChildren().removeAll(abilitiesList.getChildren().stream()
			.filter(x -> !abilities.containsValue(x))
			.collect(Collectors.toList()));

		portrait.setImage(profile.rootCharacter.portrait);
		portrait.setViewport(new Rectangle2D(0, 0,
			profile.rootCharacter.portrait.getWidth(), 160));
	}
}

