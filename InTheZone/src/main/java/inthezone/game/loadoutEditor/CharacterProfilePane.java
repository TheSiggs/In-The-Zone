package inthezone.game.loadoutEditor;

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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import java.util.List;

public class CharacterProfilePane extends HBox {
	private final VBox leftSection = new VBox(10);
	private final HBox topSection = new HBox(10);
	private final RollerScrollPane scrollAbilities;
	private final ListView<AbilityInfo> abilities = new ListView<>();
	private final VBox descriptionPanel = new VBox(10);
	private final Label description = new Label("");
	private final Button addAbility = new Button("Add ability");
	private final VBox stats = new VBox(10);
	private final Spinner<Integer> hp = new Spinner<>();
	private final Spinner<Integer> attack = new Spinner<>();
	private final Spinner<Integer> defence = new Spinner<>();
	private final Label basicAbility = new Label("Basic ability");
	private final RollerScrollPane scrollBasics;
	private final ListView<AbilityInfo> basics = new ListView<>();
	private final StackPane portraitWrapper = new StackPane();
	private final ImageView portrait = new ImageView();

	private final RollerScrollPane bottomScroll;
	private final HBox bottomSection = new HBox(10);

	private CharacterProfileModel model = null;

	public CharacterProfilePane() {
		scrollAbilities = new RollerScrollPane(abilities, false);
		scrollBasics = new RollerScrollPane(basics, false);
		bottomScroll = new RollerScrollPane(bottomSection, true);

		descriptionPanel.getChildren().addAll(description, addAbility);
		VBox.setVgrow(description, Priority.ALWAYS);

		stats.getChildren().addAll(hp, attack, defence, basicAbility, scrollBasics);
		VBox.setVgrow(scrollBasics, Priority.ALWAYS);

		topSection.getChildren().addAll(scrollAbilities, descriptionPanel, stats);

		HBox.setHgrow(bottomScroll, Priority.ALWAYS);

		final Separator spacer1 = new Separator(Orientation.VERTICAL);
		final Separator spacer2 = new Separator(Orientation.VERTICAL);
		VBox.setVgrow(spacer1, Priority.ALWAYS);
		VBox.setVgrow(spacer2, Priority.ALWAYS);
		leftSection.getChildren().addAll(spacer1, topSection, bottomScroll, spacer2);

		portraitWrapper.getChildren().add(portrait);

		HBox.setHgrow(portraitWrapper, Priority.ALWAYS);
		this.setAlignment(Pos.CENTER_LEFT);
		this.getChildren().addAll(leftSection, portraitWrapper);
	}

	public void setProfile(CharacterProfileModel model) {
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
		basics.setItems(basicsList);
		basics.getSelectionModel().select(profile.basicAbility);

		hp.setValueFactory(new PPSpinnerFactory(
			profile.hpPP, info.hpCurve.get(0), info.hpCurve));
		attack.setValueFactory(new PPSpinnerFactory(
			profile.attackPP, info.attackCurve.get(0), info.attackCurve));
		defence.setValueFactory(new PPSpinnerFactory(
			profile.defencePP, info.defenceCurve.get(0), info.defenceCurve));

		bottomSection.getChildren().clear();
		for (AbilityInfo a : profile.abilities) {
			bottomSection.getChildren().add(new ImageView(a.icon));
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

