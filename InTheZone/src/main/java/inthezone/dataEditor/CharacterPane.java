package inthezone.dataEditor;

import com.diffplug.common.base.Errors;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Stats;
import isogame.engine.CorruptDataException;
import isogame.engine.SpriteInfo;
import isogame.gui.PositiveIntegerField;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterPane extends TitledPane {
	private final File dataRoot;
	private final File gfxRoot;

	private final GridPane grid = new GridPane();
	private final Button portrait;
	private final TextField name;
	private final CheckBox playable;
	private final ComboBox<SpriteInfo> sprite;
	private final ObservableList<SpriteInfo> spriteList =
		FXCollections.observableArrayList();
	private final PositiveIntegerField ap;
	private final PositiveIntegerField mp;
	private final PositiveIntegerField power;
	private final PositiveIntegerListTextField hp;
	private final PositiveIntegerListTextField attack;
	private final PositiveIntegerListTextField defence;
	private final TreeItem<AbilityInfoModel> abilitiesRoot;

	private String portraitFilename;
	private Image portraitImage;

	public String getName() {
		return name.getText() == null? "" : name.getText();
	}

	public CharacterInfo getCharacter()
		throws CorruptDataException
	{
		List<Integer> hpCurve = hp.getValue();
		List<Integer> attackCurve = attack.getValue();
		List<Integer> defenceCurve = defence.getValue();

		int baseHP = hpCurve.isEmpty()? 0 : hpCurve.remove(0);
		int baseAttack = attackCurve.isEmpty()? 0 : attackCurve.remove(0);
		int baseDefence = defenceCurve.isEmpty()? 0 : defenceCurve.remove(0);

		return new CharacterInfo(
			name.getText(),
			sprite.getValue(),
			portraitImage,
			portraitFilename,
			new Stats(
				ap.getValue(), mp.getValue(),
				power.getValue(), baseHP,
				baseAttack, baseDefence),
			getAbilities(),
			playable.isSelected(),
			hpCurve, attackCurve, defenceCurve);
	}

	public Collection<AbilityInfo> getAbilities() throws CorruptDataException {
		try {
			return abilitiesRoot.getChildren().stream()
				.map(Errors.rethrow().wrapFunction(i -> AbilitiesPane.encodeAbility(i, false)))
				.collect(Collectors.toList());
		} catch (RuntimeException e) {
			if (e.getCause() instanceof CorruptDataException) {
				throw (CorruptDataException) e.getCause();
			} else throw e;
		}
	}

	private static void decodeAbility(
		AbilityInfo a, boolean isMana, boolean isSubsequent,
		TreeItem<AbilityInfoModel> parent
	) {
		AbilityInfoModel base = new AbilityInfoModel(isMana, isSubsequent);
		base.init(a);
		TreeItem<AbilityInfoModel> baseItem = new TreeItem<>(base);
		AbilitiesPane.setItemGraphic(baseItem);
		parent.getChildren().add(baseItem);

		if (a.subsequent.isPresent())
			decodeAbility(a.subsequent.get(), false, true, isSubsequent? parent : baseItem);

		if (a.mana.isPresent())
			decodeAbility(a.mana.get(), true, false, baseItem);
	}

	private static ImageView makePortraitImage(Image img) {
		ImageView r = new ImageView(img);
		r.setFitHeight(80);
		r.setPreserveRatio(true);
		return r;
	}

	private List<Integer> decodeCurve(int base, List<Integer> curve) {
		List<Integer> r = new ArrayList<>();
		r.add(base);
		r.addAll(curve);
		return r;
	}

	public CharacterPane(
		File dataRoot,
		CharacterInfo character,
		GameDataFactory gameData,
		WritableValue<Boolean> changed,
		AbilitiesPane abilities
	) {
		super();

		this.dataRoot = dataRoot;
		this.gfxRoot = new File(dataRoot, "gfx");

		abilitiesRoot = new TreeItem<>(new AbilityInfoModel(false, false));
		abilitiesRoot.setExpanded(true);

		for (AbilityInfo i : character.abilities) {
			decodeAbility(i, false, false, abilitiesRoot);
		}

		portraitFilename = character.portraitFile;
		portraitImage = character.portrait;
		portrait = new Button(null, makePortraitImage(portraitImage));

		name = new TextField(character.name);
		playable = new CheckBox("Playable");
		playable.setSelected(character.playable);
		sprite = new ComboBox<>(spriteList);
		ap = new PositiveIntegerField(character.stats.ap);
		mp = new PositiveIntegerField(character.stats.mp);
		power = new PositiveIntegerField(character.stats.power);

		hp = new PositiveIntegerListTextField(decodeCurve(
			character.stats.hp, character.hpCurve));
		attack = new PositiveIntegerListTextField(decodeCurve(
			character.stats.attack, character.attackCurve));
		defence = new PositiveIntegerListTextField(decodeCurve(
			character.stats.defence, character.defenceCurve));

		portrait.setOnAction(x -> {
			changed.setValue(true);

			FileChooser fc = new FileChooser();
			fc.setTitle("Choose portrait file");
			fc.setInitialDirectory(gfxRoot);
			fc.getExtensionFilters().addAll(new ExtensionFilter("Graphics files",
				"*.png", "*.PNG", "*.jpg", "*.JPG",
				"*.jpeg", "*.JPEG", "*.bmp", "*.BMP"));
			File r = fc.showOpenDialog(this.getScene().getWindow());
			if (r != null) {
				try {
					String path = gfxRoot.toPath().relativize(r.toPath()).toString();
					portraitImage = new Image(new FileInputStream(r));
					portraitFilename = path;
					portrait.setGraphic(makePortraitImage(portraitImage));
				} catch (IOException e) {
					Alert error = new Alert(Alert.AlertType.ERROR);
					error.setTitle("Cannot load image from file " + r.toString());
					error.setHeaderText(e.toString());
					error.showAndWait();
				}
			}
		});

		grid.add(portrait, 1, 0);
		grid.addRow(1, new Label("Name"), name);
		grid.add(playable, 1, 2);
		grid.addRow(3, new Label("Sprite"), sprite);
		grid.addRow(4, new Label("Base AP"), ap);
		grid.addRow(5, new Label("Base MP"), mp);
		grid.addRow(6, new Label("Base Power"), power);
		grid.addRow(7, new Label("Base HP"), hp);
		grid.addRow(8, new Label("Base Attack"), attack);
		grid.addRow(9, new Label("Base Defence"), defence);

		name.textProperty().addListener(c -> changed.setValue(true));
		sprite.valueProperty().addListener(c -> changed.setValue(true));
		ap.textProperty().addListener(c -> changed.setValue(true));
		mp.textProperty().addListener(c -> changed.setValue(true));
		power.textProperty().addListener(c -> changed.setValue(true));
		hp.textProperty().addListener(c -> changed.setValue(true));
		attack.textProperty().addListener(c -> changed.setValue(true));
		defence.textProperty().addListener(c -> changed.setValue(true));

		this.setText(character.name);
		this.setContent(grid);
		this.textProperty().bind(name.textProperty());

		final int CHARACTER = gameData.getPriorityLevel("CHARACTER");
		for (SpriteInfo i : gameData.getGlobalSprites()) {
			if (i.priority == CHARACTER) spriteList.add(i);
		}
		sprite.getSelectionModel().select(character.sprite);

		this.expandedProperty().addListener((v, oldv, newv) -> {
			if (newv) {
				abilities.setAbilities(name.textProperty(), abilitiesRoot);
			}
		});
	}
}

