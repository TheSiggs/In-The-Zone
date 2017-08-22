package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AbilityDescriptionPanel extends VBox {
	private final Label abilityName = new Label("");
	private final Label description = new Label("");
	private final Label manaUpgrade = new Label("↓ Mana upgrade ↓");
	private final Label manaAbilityName = new Label("");
	private final Label manaDescription = new Label("");

	public AbilityDescriptionPanel() {
		super(10);

		this.getStyleClass().addAll("panel", "padded-panel");
		abilityName.getStyleClass().add("ability-name");
		manaAbilityName.getStyleClass().add("ability-name");
		manaUpgrade.getStyleClass().add("mana-upgrade");

		abilityName.setMaxWidth(Double.MAX_VALUE);
		manaAbilityName.setMaxWidth(Double.MAX_VALUE);
		manaUpgrade.setMaxWidth(Double.MAX_VALUE);
		description.setAlignment(Pos.CENTER_LEFT);
		manaDescription.setAlignment(Pos.CENTER_LEFT);
		manaUpgrade.setAlignment(Pos.CENTER_LEFT);

		this.setAlignment(Pos.BOTTOM_CENTER);
		this.setFillWidth(true);
		this.getChildren().addAll(
			abilityName, description, manaUpgrade,
			manaAbilityName, manaDescription);

		description.setAlignment(Pos.TOP_LEFT);
		description.setWrapText(true);
		description.setMaxWidth(Double.MAX_VALUE);

		manaDescription.setAlignment(Pos.TOP_LEFT);
		manaDescription.setWrapText(true);
		manaDescription.setMaxWidth(Double.MAX_VALUE);

		manaDescription.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(manaDescription, Priority.ALWAYS);
	}

	public void setAbility(final AbilityInfo a) {
		if (a == null) {
			abilityName.setText("No ability selected");
			abilityName.setGraphic(null);
			description.setText("No ability selected");
			manaUpgrade.setVisible(false);
			manaAbilityName.setText("");
			manaAbilityName.setGraphic(null);
			manaDescription.setText("");
		} else {
			final AbilityDescription d = new AbilityDescription(a);
			abilityName.setText(d.getTitle() + " (" + a.pp + " PP)\n" + d.getLoadoutInfoLine());
			abilityName.setGraphic(new ImageView(a.media.icon));
			description.setText(d.getDescription());

			if (a.mana.isPresent()) {
				final AbilityDescription md = new AbilityDescription(a.mana.get());
				manaUpgrade.setVisible(true);
				manaAbilityName.setText(md.getTitle() + "\n" + md.getLoadoutInfoLine());
				manaAbilityName.setGraphic(new ImageView(a.mana.get().media.icon));
				manaDescription.setText(md.getDescription());
			} else {
				manaUpgrade.setVisible(false);
				manaAbilityName.setText("");
				manaAbilityName.setGraphic(null);
				manaDescription.setText("");
			}
		}
	}
}

