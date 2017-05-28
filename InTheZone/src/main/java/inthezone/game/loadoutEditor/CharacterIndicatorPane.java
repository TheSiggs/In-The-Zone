package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Stats;
import inthezone.game.RollerScrollPane;
import javafx.beans.binding.StringExpression;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CharacterIndicatorPane extends AnchorPane {
	private CharacterProfileModel profile = null;

	private final Label placeholder = new Label("Click to add a character");

	private final Label name = new Label("");
	private final Label hp = new Label("0");
	private final Label attack = new Label("0");
	private final Label defence = new Label("0");
	private final Label power = new Label("0");
	private final Label pp = new Label("0");
	private final GridPane info = new GridPane();
	private final ImageView portrait = new ImageView();

	private final Map<String, ImageView> abilities = new HashMap<>();
	private final RollerScrollPane abilitiesListContainer;
	private final HBox abilitiesList = new HBox(2);

	public CharacterIndicatorPane(Optional<CharacterProfileModel> profile) {
		super();

		abilitiesList.setAlignment(Pos.CENTER_LEFT);
		abilitiesListContainer = new RollerScrollPane(abilitiesList, true);

		if (profile.isPresent()) {
			setupCharacterProfile(profile.get());
		} else {
			AnchorPane.setTopAnchor(placeholder, 0d);
			AnchorPane.setBottomAnchor(placeholder, 0d);
			AnchorPane.setLeftAnchor(placeholder, 0d);
			AnchorPane.setRightAnchor(placeholder, 0d);
			this.getChildren().add(placeholder);
		}
	}

	private void setupCharacterProfile(CharacterProfileModel profile) {
		this.getChildren().removeAll();

		this.profile = profile;

		this.getStyleClass().add("panel");
		name.getStyleClass().add("character-indicator-panel-title");
		abilitiesListContainer.getStyleClass().add("clear-panel");

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

		AnchorPane.setTopAnchor(info, 10d);
		AnchorPane.setLeftAnchor(info, 10d);

		AnchorPane.setTopAnchor(portrait, 10d);
		AnchorPane.setRightAnchor(portrait, 0d);

		AnchorPane.setBottomAnchor(abilitiesListContainer, 0d);
		AnchorPane.setLeftAnchor(abilitiesListContainer, 0d);
		AnchorPane.setRightAnchor(abilitiesListContainer, 0d);

		this.getChildren().addAll(portrait, info, abilitiesListContainer);

		profile.profileProperty().addListener((x, p0, p1) -> updateProfile(p1));
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

		for (AbilityInfo a : profile.allAbilities()) {
			if (!abilities.containsKey(a.name)) {
				final ImageView i = new ImageView(a.icon);
				final Tooltip t = new Tooltip((new AbilityDescription(a)).toString());
				t.setWrapText(true);
				t.setMaxWidth(300);
				Tooltip.install(i, t);

				abilities.put(a.name, i);
				abilitiesList.getChildren().add(i);
			}
		}

		abilitiesList.getChildren().removeAll(abilitiesList.getChildren().stream()
			.filter(x -> !abilities.containsValue(x))
			.collect(Collectors.toList()));

		portrait.setImage(profile.rootCharacter.portrait);
		portrait.setViewport(new Rectangle2D(0, 0,
			profile.rootCharacter.portrait.getWidth(), 160));
	}
}

