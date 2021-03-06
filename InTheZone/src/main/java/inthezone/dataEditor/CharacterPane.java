package inthezone.dataEditor;

import com.diffplug.common.base.Errors;

import isogame.engine.CorruptDataException;
import isogame.engine.SpriteInfo;
import isogame.gui.PositiveIntegerField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Stats;

public class CharacterPane extends TitledPane {
	private final File dataRoot;
	private final File gfxRoot;

	private final GridPane grid = new GridPane();
	private final Button portrait;
	private final Button bigPortrait;
	private final TextField name;
	private final CheckBox playable;
	private final ComboBox<SpriteInfo> spriteA;
	private final ComboBox<SpriteInfo> spriteB;
	private final ObservableList<SpriteInfo> spriteList =
		FXCollections.observableArrayList();
	private final PositiveIntegerField ap;
	private final PositiveIntegerField mp;
	private final PositiveIntegerField power;
	private final PositiveIntegerListTextField hp;
	private final PositiveIntegerListTextField attack;
	private final PositiveIntegerListTextField defence;
	private final TextArea flavourText;
	private final TreeItem<AbilityInfoModel> abilitiesRoot;

	private String portraitFilename;
	private Image portraitImage;
	private String bigPortraitFilename;
	private Image bigPortraitImage;

	public String getName() {
		return name.getText() == null? "" : name.getText();
	}

	public CharacterInfo getCharacter()
		throws CorruptDataException
	{
		final List<Integer> hpCurve = hp.getValue();
		final List<Integer> attackCurve = attack.getValue();
		final List<Integer> defenceCurve = defence.getValue();

		final int baseHP = hpCurve.isEmpty()? 0 : hpCurve.remove(0);
		final int baseAttack = attackCurve.isEmpty()? 0 : attackCurve.remove(0);
		final int baseDefence = defenceCurve.isEmpty()? 0 : defenceCurve.remove(0);

		return new CharacterInfo(
			name.getText(),
			flavourText.getText(),
			spriteA.getValue(),
			spriteB.getValue(),
			portraitImage,
			portraitFilename,
			bigPortraitImage,
			bigPortraitFilename,
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
				.map(Errors.rethrow()
				.wrapFunction(i -> AbilitiesPane.encodeAbility(i, false)))
				.collect(Collectors.toList());
		} catch (RuntimeException e) {
			if (e.getCause() instanceof CorruptDataException) {
				throw (CorruptDataException) e.getCause();
			} else throw e;
		}
	}

	private static void decodeAbility(
		final AbilityInfo a, final boolean isMana,
		final boolean isSubsequent,
		final TreeItem<AbilityInfoModel> parent
	) {
		final AbilityInfoModel base = new AbilityInfoModel(isMana, isSubsequent);
		base.init(a);
		final TreeItem<AbilityInfoModel> baseItem = new TreeItem<>(base);
		AbilitiesPane.setItemGraphic(baseItem);
		parent.getChildren().add(baseItem);

		if (a.subsequent.isPresent())
			decodeAbility(a.subsequent.get(), false, true, isSubsequent? parent : baseItem);

		if (a.mana.isPresent())
			decodeAbility(a.mana.get(), true, false, baseItem);
	}

	private static ImageView makePortraitImage(final Image img) {
		final ImageView r = new ImageView(img);
		r.setFitHeight(80);
		r.setPreserveRatio(true);
		return r;
	}

	private List<Integer> decodeCurve(
		final int base, final List<Integer> curve
	) {
		final List<Integer> r = new ArrayList<>();
		r.add(base);
		r.addAll(curve);
		return r;
	}

	public CharacterPane(
		final File dataRoot,
		final CharacterInfo character,
		final GameDataFactory gameData,
		final WritableValue<Boolean> changed,
		final AbilitiesPane abilities
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
		bigPortraitFilename = character.bigPortraitFile;
		bigPortraitImage = character.bigPortrait;
		portrait = new Button(null, makePortraitImage(portraitImage));
		bigPortrait = new Button(null, makePortraitImage(bigPortraitImage));

		name = new TextField(character.name);
		playable = new CheckBox("Playable");
		playable.setSelected(character.playable);
		spriteA = new ComboBox<>(spriteList);
		spriteB = new ComboBox<>(spriteList);
		ap = new PositiveIntegerField(character.stats.ap);
		mp = new PositiveIntegerField(character.stats.mp);
		power = new PositiveIntegerField(character.stats.power);

		hp = new PositiveIntegerListTextField(decodeCurve(
			character.stats.hp, character.hpCurve));
		attack = new PositiveIntegerListTextField(decodeCurve(
			character.stats.attack, character.attackCurve));
		defence = new PositiveIntegerListTextField(decodeCurve(
			character.stats.defence, character.defenceCurve));

		flavourText = new TextArea();
		if (!character.flavourText.equals(""))
			flavourText.setText(character.flavourText);
		flavourText.setWrapText(true);
		flavourText.setMinHeight(80);
		flavourText.setMaxWidth(260);
		flavourText.setPromptText("General description for this character");

		portrait.setOnAction(x -> {
			changed.setValue(true);
			updatePortrait(false);
		});
		bigPortrait.setOnAction(x -> {
			changed.setValue(true);
			updatePortrait(true);
		});

		grid.add(new HBox(portrait, bigPortrait), 1, 0);
		grid.addRow(1, new Label("Name"), name);
		grid.add(playable, 1, 2);
		grid.addRow(3, new Label("Player A Sprite"), spriteA);
		grid.addRow(4, new Label("Player B Sprite"), spriteB);
		grid.addRow(5, new Label("Base AP"), ap);
		grid.addRow(6, new Label("Base MP"), mp);
		grid.addRow(7, new Label("Base Power"), power);
		grid.addRow(8, new Label("Base HP"), hp);
		grid.addRow(9, new Label("Base Attack"), attack);
		grid.addRow(10, new Label("Base Defence"), defence);
		grid.add(flavourText, 0, 11, 2, 1);

		name.textProperty().addListener(c -> changed.setValue(true));
		spriteA.valueProperty().addListener(c -> changed.setValue(true));
		spriteB.valueProperty().addListener(c -> changed.setValue(true));
		ap.textProperty().addListener(c -> changed.setValue(true));
		mp.textProperty().addListener(c -> changed.setValue(true));
		power.textProperty().addListener(c -> changed.setValue(true));
		hp.textProperty().addListener(c -> changed.setValue(true));
		attack.textProperty().addListener(c -> changed.setValue(true));
		defence.textProperty().addListener(c -> changed.setValue(true));
		flavourText.textProperty().addListener(c -> changed.setValue(true));

		this.setText(character.name);
		this.setContent(grid);
		this.textProperty().bind(name.textProperty());

		final int CHARACTER = gameData.getPriorityLevel("CHARACTER");
		for (SpriteInfo i : gameData.getGlobalSprites()) {
			if (i.priority == CHARACTER) spriteList.add(i);
		}
		spriteA.getSelectionModel().select(character.spriteA);
		spriteB.getSelectionModel().select(character.spriteB);

		this.expandedProperty().addListener((v, oldv, newv) -> {
			if (newv) {
				abilities.setAbilities(name.textProperty(), abilitiesRoot);
			}
		});
	}

	private void updatePortrait(final boolean big) {
		final FileChooser fc = new FileChooser();
		fc.setTitle("Choose portrait file");
		fc.setInitialDirectory(gfxRoot);
		fc.getExtensionFilters().addAll(new ExtensionFilter("Graphics files",
			"*.png", "*.PNG", "*.jpg", "*.JPG",
			"*.jpeg", "*.JPEG", "*.bmp", "*.BMP"));
		final File r = fc.showOpenDialog(this.getScene().getWindow());
		if (r != null) {
			try {
				final String path = gfxRoot.toPath().relativize(r.toPath()).toString();
				if (big) {
					bigPortraitImage = new Image(new FileInputStream(r));
					bigPortraitFilename = path;
					bigPortrait.setGraphic(makePortraitImage(bigPortraitImage));
				} else {
					portraitImage = new Image(new FileInputStream(r));
					portraitFilename = path;
					portrait.setGraphic(makePortraitImage(portraitImage));
				}
			} catch (IOException e) {
				final Alert error = new Alert(Alert.AlertType.ERROR);
				error.setTitle("Cannot load image from file " + r.toString());
				error.setHeaderText(e.toString());
				error.showAndWait();
			}
		}
	}
}

