package inthezone.game.lobby;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Stats;
import inthezone.game.RollerScrollPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SmallCharacterInfoPanel extends VBox {
	private CharacterProfile profile;
	private final Label name = new Label();
	private final Label stats = new Label();
	private final RollerScrollPane abilitiesWrapper;
	private final HBox abilities = new HBox(2);

	public SmallCharacterInfoPanel(CharacterProfile profile) {
		super(2);
		this.profile = profile;

		this.setMaxWidth(260);
		this.setMinWidth(260);

		abilitiesWrapper = new RollerScrollPane(abilities, true);
		abilities.setAlignment(Pos.CENTER_LEFT);

		this.getStyleClass().addAll("panel", "small-character-info-panel");
		name.getStyleClass().add("character-indicator-panel-title");
		abilitiesWrapper.getStyleClass().add("opaque-roller-scroller");

		setProfile(profile);

		this.getChildren().addAll(name, stats, abilitiesWrapper);
	}

	public void setProfile(CharacterProfile profile) {
		this.profile = profile;

		name.setText(profile.rootCharacter.name);
		final Stats baseStats = profile.getBaseStats();
		stats.setText(
			"HP:" + baseStats.hp +
			" Att:" + baseStats.attack +
			" Def:" + baseStats.defence +
			" Pow:" + baseStats.power);

		for (AbilityInfo a : profile.allAbilities()) {
			final ImageView i = new ImageView(a.media.icon);
			final Tooltip t = new Tooltip((new AbilityDescription(a)).toString());
			t.setWrapText(true);
			t.setMaxWidth(300);
			Tooltip.install(i, t);

			abilities.getChildren().add(i);
		}

		abilitiesWrapper.layout();
		abilitiesWrapper.setScrollPos(abilitiesWrapper.getScrollMin());
	}
}

