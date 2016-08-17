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
import inthezone.battle.data.WeaponInfo;
import isogame.engine.CorruptDataException;
import isogame.engine.SpriteInfo;
import isogame.gui.PositiveIntegerField;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CharacterPane extends TitledPane {
	private final GridPane grid = new GridPane();
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
	private final TreeItem<WeaponInfoModel> weaponsRoot;

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
				a.getRibbon(),
				new TargetMode(a.getTargetMode()),
				a.getnTargets(),
				a.getLOS()
			),
			a.getUseWeaponRange(),
			mana,
			subsequent,
			a.getRecursion(),
			ib, ia, se);
	}

	private WeaponInfo encodeWeapon(
		TreeItem<WeaponInfoModel> item, Collection<AbilityInfo> abilities
	) {
		WeaponInfoModel w = item.getValue();
		String abilityName = w.getAttack();
		return new WeaponInfo(
			w.getName(),
			w.getRange(),
			name.getText(),
			abilities.stream()
				.filter(a -> a.name.equals(abilityName))
				.findAny().orElse(WeaponInfo.defaultAbility));
	}

	public CharacterInfo getCharacter()
		throws CorruptDataException
	{
		return new CharacterInfo(
			name.getText(),
			sprite.getValue(),
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

	public Collection<WeaponInfo> getWeapons() throws CorruptDataException {
		Collection<AbilityInfo> abilities = getAbilities();
		return weaponsRoot.getChildren().stream()
			.map(i -> encodeWeapon(i, abilities))
			.collect(Collectors.toList());
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

	private static TreeItem<WeaponInfoModel> decodeWeapon(WeaponInfo w) {
		WeaponInfoModel base = new WeaponInfoModel();
		base.init(w);
		return new TreeItem<>(base);
	}

	public CharacterPane(
		CharacterInfo character,
		GameDataFactory gameData,
		WritableValue<Boolean> changed,
		AbilitiesPane abilities,
		Collection<WeaponInfo> characterWeapons,
		WeaponsDialog weapons
	) {
		super();

		abilitiesRoot = new TreeItem<>(new AbilityInfoModel(false, false));
		weaponsRoot = new TreeItem<>(new WeaponInfoModel());
		abilitiesRoot.setExpanded(true);
		weaponsRoot.setExpanded(true);

		for (AbilityInfo i : character.abilities) {
			decodeAbility(i, false, false, abilitiesRoot);
		}
		for (WeaponInfo i : characterWeapons) {
			weaponsRoot.getChildren().add(decodeWeapon(i));
		}

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

		Button weaponsButton = new Button("Weapons ...");

		grid.addRow(0, new Label("Name"), name);
		grid.add(playable, 1, 1);
		grid.addRow(2, new Label("Sprite"), sprite);
		grid.addRow(3, new Label("Base AP"), ap);
		grid.addRow(4, new Label("Base MP"), mp);
		grid.addRow(5, new Label("Base Power"), power);
		grid.addRow(6, new Label("Base HP"), hp);
		grid.addRow(7, new Label("Base Attack"), attack);
		grid.addRow(8, new Label("Base Defence"), defence);
		grid.add(weaponsButton, 1, 9);

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
				weapons.setCharacter(name.textProperty(), weaponsRoot, abilitiesRoot);
			}
		});

		weaponsButton.setOnAction(event -> {
			if (!weapons.isShowing()) {
				weapons.show();
			}
		});
	}
}

