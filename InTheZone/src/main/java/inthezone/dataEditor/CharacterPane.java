package inthezone.dataEditor;

import com.diffplug.common.base.Errors;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterInfo;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.Range;
import inthezone.battle.data.Stats;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.TargetMode;
import isogame.engine.CorruptDataException;
import isogame.engine.SpriteInfo;
import isogame.gui.PositiveIntegerField;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
	private final PositiveIntegerField hp;
	private final PositiveIntegerField attack;
	private final PositiveIntegerField defence;
	private final TreeItem<AbilityInfoModel> abilitiesRoot;

	private String portraitFilename;
	private Image portraitImage;

	public String getName() {
		return name.getText() == null? "" : name.getText();
	}

	private static AbilityInfo encodeAbility(
		TreeItem<AbilityInfoModel> item, boolean isMana
	) throws CorruptDataException
	{
		try {
			Optional<AbilityInfo> mana;
			if (isMana) mana = Optional.empty(); else {
				mana = item.getChildren().stream()
					.filter(i -> i.getValue().getIsMana()).findAny()
					.map(Errors.rethrow().wrapFunction(i -> encodeAbility(i, true)));
			}
			List<TreeItem<AbilityInfoModel>> subs =
				item.getChildren().stream()
					.filter(i -> i.getValue().getIsSubsequent())
					.collect(Collectors.toList());
			Optional<AbilityInfo> subsequent = Optional.empty();
			for (int i = subs.size() - 1; i >= 0; i--) {
				subsequent = Optional.of(encodeAbility0(
					subs.get(i).getValue(), Optional.empty(), subsequent));
			}
			return encodeAbility0(item.getValue(), mana, subsequent);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof CorruptDataException) {
				throw (CorruptDataException) e.getCause();
			} else throw e;
		}
	}

	private static AbilityInfo encodeAbility0(
		AbilityInfoModel a,
		Optional<AbilityInfo> mana,
		Optional<AbilityInfo> subsequent
	) throws CorruptDataException
	{
		Optional<InstantEffectInfo> ib = Optional.empty();
		Optional<InstantEffectInfo> ia = Optional.empty();
		Optional<StatusEffectInfo> se = Optional.empty();

		try {
			ib = Optional.of(new InstantEffectInfo(a.getInstantBefore()));
		} catch (CorruptDataException e) { /* IGNORE */ }
		try {
			ia = Optional.of(new InstantEffectInfo(a.getInstantAfter()));
		} catch (CorruptDataException e) { /* IGNORE */ }
		try {
			se = Optional.of(new StatusEffectInfo(a.getStatusEffect()));
		} catch (CorruptDataException e) { /* IGNORE */ }

		return new AbilityInfo(
			a.getName(),
			AbilityType.parse(a.getType()),
			a.getTrap(),
			a.getZoneTurns(),
			a.getBoundZone(),
			a.getAP(),
			a.getMP(),
			a.getPP(),
			a.getEff(),
			a.getChance(),
			a.getHeal(),
			new Range(
				a.getRange(),
				a.getRadius(),
				a.getPiercing(),
				new TargetMode(a.getTargetMode()),
				a.getnTargets(),
				a.getLOS()
			),
			mana,
			subsequent,
			a.getRecursion(),
			ib, ia, se);
	}

	public CharacterInfo getCharacter()
		throws CorruptDataException
	{
		return new CharacterInfo(
			name.getText(),
			sprite.getValue(),
			portraitImage,
			portraitFilename,
			new Stats(
				ap.getValue(), mp.getValue(),
				power.getValue(), hp.getValue(),
				attack.getValue(), defence.getValue()),
			getAbilities(),
			playable.isSelected());
	}

	public Collection<AbilityInfo> getAbilities() throws CorruptDataException {
		try {
			return abilitiesRoot.getChildren().stream()
				.map(Errors.rethrow().wrapFunction(i -> encodeAbility(i, false)))
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
		hp = new PositiveIntegerField(character.stats.hp);
		attack = new PositiveIntegerField(character.stats.attack);
		defence = new PositiveIntegerField(character.stats.defence);

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

		for (SpriteInfo i : gameData.getGlobalSprites()) spriteList.add(i);
		sprite.getSelectionModel().select(character.sprite);

		this.expandedProperty().addListener((v, oldv, newv) -> {
			if (newv) {
				abilities.setAbilities(name.textProperty(), abilitiesRoot);
			}
		});
	}
}

