package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.Stats;
import javafx.beans.binding.StringExpression;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class CharacterIndicatorPane extends GridPane {
	private CharacterProfileModel profile;

	private final Label name = new Label("");
	private final Label power = new Label("0");
	private final Label hp = new Label("0");
	private final Label attack = new Label("0");
	private final Label defence = new Label("0");
	private final Label cost = new Label("0");

	public CharacterIndicatorPane(CharacterProfileModel profile) {
		super();

		this.profile = profile;

		name.setText(profile.rootCharacter.name);
		this.add(name, 0, 0, 2, 1);
		this.add(new Label("Power:"),   0, 1);
		this.add(new Label("HP:"),      0, 2);
		this.add(new Label("Attack:"),  0, 3);
		this.add(new Label("Defence:"), 0, 4);
		this.add(new Label("Cost:"), 0, 5);

		this.add(power,   1, 1);
		this.add(hp,      1, 2);
		this.add(attack,  1, 3);
		this.add(defence, 1, 4);
		this.add(cost, 1, 5);

		profile.profileProperty().addListener((x, p0, p1) -> updateProfile(p1));
		cost.textProperty().bind(StringExpression.stringExpression(profile.costProperty()));
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
	}
}

