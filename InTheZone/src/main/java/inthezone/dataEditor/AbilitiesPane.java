package inthezone.dataEditor;

import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.AbilityZoneType;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.InstantEffectInfo;
import inthezone.battle.data.InstantEffectType;
import inthezone.battle.data.StatusEffectInfo;
import inthezone.battle.data.StatusEffectType;
import isogame.engine.SpriteInfo;
import isogame.gui.FloatingField;
import isogame.gui.PositiveIntegerField;
import isogame.gui.StringField;
import isogame.gui.TypedTextFieldTreeTableCell;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AbilitiesPane extends VBox {
	private final TreeTableView<AbilityInfoModel> table;
	private final ObservableList<Integer> selected;

	private final FlowPane tools = new FlowPane(Orientation.HORIZONTAL);
	private final Button add = new Button("Add ability");
	private final Button remove = new Button("Remove ability");
	private final Button addSubsequent = new Button("Add subsequent ability");
	private final Button addMana = new Button("Add mana ability");
	private final Button up = new Button("Up");
	private final Button down = new Button("Down");
	
	private final TreeTableColumn<AbilityInfoModel, Boolean> banned = new TreeTableColumn<>("Banned");
	private final TreeTableColumn<AbilityInfoModel, String> name = new TreeTableColumn<>("Name");
	private final TreeTableColumn<AbilityInfoModel, String> type = new TreeTableColumn<>("Type");
	private final TreeTableColumn<AbilityInfoModel, Boolean> trap = new TreeTableColumn<>("Trap");
	private final TreeTableColumn<AbilityInfoModel, String> zone = new TreeTableColumn<>("Zone");
	private final TreeTableColumn<AbilityInfoModel, SpriteInfo> zoneTrapSprite = new TreeTableColumn<>("T/S Sprite");
	private final TreeTableColumn<AbilityInfoModel, Integer> ap = new TreeTableColumn<>("AP cost");
	private final TreeTableColumn<AbilityInfoModel, Integer> mp = new TreeTableColumn<>("MP cost");
	private final TreeTableColumn<AbilityInfoModel, Integer> pp = new TreeTableColumn<>("PP cost");
	private final TreeTableColumn<AbilityInfoModel, Double> eff = new TreeTableColumn<>("Efficiency");
	private final TreeTableColumn<AbilityInfoModel, Double> chance = new TreeTableColumn<>("Chance to inflict status");
	private final TreeTableColumn<AbilityInfoModel, Boolean> heal = new TreeTableColumn<>("Healing");

	private final TreeTableColumn<AbilityInfoModel, Integer> range = new TreeTableColumn<>("Range");
	private final TreeTableColumn<AbilityInfoModel, Integer> radius = new TreeTableColumn<>("Radius");
	private final TreeTableColumn<AbilityInfoModel, Boolean> piercing = new TreeTableColumn<>("Piercing");
	private final TreeTableColumn<AbilityInfoModel, String> targetMode = new TreeTableColumn<>("Target");
	private final TreeTableColumn<AbilityInfoModel, Integer> nTargets = new TreeTableColumn<>("Max targets");
	private final TreeTableColumn<AbilityInfoModel, Boolean> los = new TreeTableColumn<>("LOS required");

	private final TreeTableColumn<AbilityInfoModel, Integer> recursion = new TreeTableColumn<>("Recursion");

	private final TreeTableColumn<AbilityInfoModel, String> instantBefore = new TreeTableColumn<>("Instant before damage");
	private final TreeTableColumn<AbilityInfoModel, String> instantAfter = new TreeTableColumn<>("Instant after damage");
	private final TreeTableColumn<AbilityInfoModel, String> statusEffect = new TreeTableColumn<>("Status effect");

	private final ObservableList<String> types =
		FXCollections.observableArrayList(enumValues(AbilityType.class));

	private final ObservableList<String> zoneTypes =
		FXCollections.observableArrayList(enumValues(AbilityZoneType.class));
	
	private final ObservableList<SpriteInfo> spriteList = FXCollections.observableArrayList();

	private final ObservableList<String> targetModes =
		FXCollections.observableArrayList("E", "A", "S", "EA", "ES", "AS", "EAS");

	private static String[] enumValues(Class<? extends Enum> c) {
		return Arrays.stream(c.getEnumConstants())
			.map(x -> x.toString().toLowerCase())
			.collect(Collectors.toList()).toArray(new String[0]);
	}

	private final static Image manaIcon = new Image(
		AbilitiesPane.class.getResourceAsStream("/editor_assets/mana_ability.png"));
	private final static Image subsequentIcon = new Image(
		AbilitiesPane.class.getResourceAsStream("/editor_assets/sub_ability.png"));
	
	public static void setItemGraphic(TreeItem<AbilityInfoModel> item) {
		AbilityInfoModel v = item.getValue();
		if (v.getIsMana()) {
			item.setGraphic(new ImageView(manaIcon));
		} else if (v.getIsSubsequent()) {
			item.setGraphic(new ImageView(subsequentIcon));
		}
	}

	private final Label noCharacterSelectedMessage = new Label("No character selected");
	private final Label noAbilitiesMessage = new Label();

	private SimpleBooleanProperty isCharacterLoaded =
		new SimpleBooleanProperty(false);
	private TreeItem<AbilityInfoModel> tableRoot;
	public void setAbilities(
		ObservableValue<String> name, TreeItem<AbilityInfoModel> abilities
	) {
		isCharacterLoaded.setValue(true);
		tableRoot = abilities;
		tableRoot.setExpanded(true);
		table.setRoot(tableRoot);
		table.setShowRoot(false);
		table.setPlaceholder(noAbilitiesMessage);
		noAbilitiesMessage.textProperty().bind(
			Bindings.concat("No abilities defined for ", name));
	}
	public void clearAbilities() {
		setAbilities(
			new SimpleStringProperty(""),
			new TreeItem<>(new AbilityInfoModel(false, false)));
		isCharacterLoaded.setValue(false);
		table.setPlaceholder(noCharacterSelectedMessage);
	}

	public AbilitiesPane(WritableValue<Boolean> changed, GameDataFactory gameData) {
		super();

		for (SpriteInfo i : gameData.getGlobalSprites()) spriteList.add(i);

		tableRoot = new TreeItem<>(new AbilityInfoModel(false, false));
		tableRoot.setExpanded(true);
		table = new TreeTableView<AbilityInfoModel>(tableRoot);
		table.setShowRoot(false);
		table.setEditable(true);
		table.setPlaceholder(noCharacterSelectedMessage);
		TreeTableView.TreeTableViewSelectionModel<AbilityInfoModel> selection =
			table.getSelectionModel();
		selected = selection.getSelectedIndices();

		tools.getChildren().addAll(add, remove, addSubsequent, addMana, up, down);
		VBox.setVgrow(table, Priority.ALWAYS);
		this.getChildren().addAll(tools, table);

		add.disableProperty().bind(isCharacterLoaded.not());
		remove.disableProperty().bind(Bindings.isEmpty(selected));
		addSubsequent.disableProperty().bind(Bindings.isEmpty(selected));
		addMana.disableProperty().bind(
			Bindings.isEmpty(selected)
			.or(new BooleanBinding() {
				{
					super.bind(selected);
				}

				@Override
				protected boolean computeValue() {
					TreeItem<AbilityInfoModel> item = table.getTreeItem(selected.get(0));
					if (item == null) return false;
					AbilityInfoModel v = item.getValue();
					AbilityInfoModel pv = item.getParent().getValue();
					boolean hasMana = item.getChildren().stream()
						.anyMatch(a -> a.getValue().getIsMana());
					return hasMana || v.getIsMana() || v.getIsSubsequent() || pv.getIsMana();
				}
			}));

		add.setOnAction(event -> {
			if (isCharacterLoaded.getValue()) {
				TreeItem<AbilityInfoModel> item =
					new TreeItem<>(new AbilityInfoModel(false, false));
				ObservableList<TreeItem<AbilityInfoModel>> children = tableRoot.getChildren();
				children.add(item);
				changed.setValue(true);

				// force a change event in the list of abilities when the type is
				// changed, so that the weapons dialog box can detect it
				item.getValue().typeProperty().addListener(
					c -> children.set(children.indexOf(item), item));
			}
		});

		remove.setOnAction(event -> {
			selected.stream()
				.sorted((a, b) -> {if (b < a) return -1; else if (b > a) return 1; else return 0;})
				.forEach(i -> {
					TreeItem<AbilityInfoModel> item = table.getTreeItem(i);
					item.getParent().getChildren().remove(item);
				});
				changed.setValue(true);
		});

		addMana.setOnAction(event -> {
			if (selected.size() >= 1) {
				TreeItem<AbilityInfoModel> item = table.getTreeItem(selected.get(0));
				AbilityInfoModel v = item.getValue();
				AbilityInfoModel pv = item.getParent().getValue();
				if (!v.getIsMana() && !v.getIsSubsequent() && !pv.getIsMana()) {
					TreeItem<AbilityInfoModel> newMana = new TreeItem<>(v.cloneMana());
					setItemGraphic(newMana);
					item.getChildren().add(newMana);
					item.setExpanded(true);
					selection.select(newMana);
					changed.setValue(true);
				}
			}
		});

		addSubsequent.setOnAction(event -> {
			if (selected.size() >= 1) {
				TreeItem<AbilityInfoModel> item = table.getTreeItem(selected.get(0));
				TreeItem<AbilityInfoModel> parent;
				if (item.getValue().getIsSubsequent()) {
					parent = item.getParent();
				} else {
					parent = item;
				}

				List<TreeItem<AbilityInfoModel>> all = parent.getChildren();
				int i;
				AbilityInfoModel toClone = item.getValue();
				for (i = 0; i < all.size(); i++) {
					if (all.get(i).getValue().getIsSubsequent()) {
						toClone = all.get(i).getValue();
					} else {
						break;
					}
				}

				parent.setExpanded(true);
				TreeItem<AbilityInfoModel> newSubsequent =
					new TreeItem<>(toClone.cloneSubsequent());
				setItemGraphic(newSubsequent);
				all.add(i, newSubsequent);
				selection.select(newSubsequent);
				changed.setValue(true);
			}
		});

		up.setOnAction(event -> {
			if (selected.size() >= 1) {
				TreeItem<AbilityInfoModel> item = table.getTreeItem(selected.get(0));
				AbilityInfoModel v = item.getValue();
				if (!v.getIsMana()) {
					List<TreeItem<AbilityInfoModel>> all = item.getParent().getChildren();
					int i = all.indexOf(item);
					if (i > 0) {
						all.remove(i);
						all.add(i - 1, item);
						selection.select(item);
						changed.setValue(true);
					}
				}
			}
		});

		down.setOnAction(event -> {
			if (selected.size() >= 1) {
				TreeItem<AbilityInfoModel> item = table.getTreeItem(selected.get(0));
				AbilityInfoModel v = item.getValue();
				if (!v.getIsMana()) {
					List<TreeItem<AbilityInfoModel>> all = item.getParent().getChildren();
					int i = all.indexOf(item);
					if (i < all.size() - 1 && !all.get(i + 1).getValue().getIsMana()) {
						all.remove(i);
						all.add(i + 1, item);
						selection.select(item);
						changed.setValue(true);
					}
				}
			}
		});

		banned.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Boolean>("banned"));
		name.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("name"));
		type.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("type"));
		trap.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Boolean>("trap"));
		zone.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("zone"));
		zoneTrapSprite.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, SpriteInfo>("zoneTrapSprite"));
		ap.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("ap"));
		mp.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("mp"));
		pp.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("pp"));
		eff.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Double>("eff"));
		chance.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Double>("chance"));
		heal.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Boolean>("heal"));
		range.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("range"));
		radius.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("radius"));
		piercing.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Boolean>("piercing"));
		targetMode.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("targetMode"));
		nTargets.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("nTargets"));
		los.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Boolean>("los"));
		recursion.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, Integer>("recursion"));
		instantBefore.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("instantBefore"));
		instantAfter.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("instantAfter"));
		statusEffect.setCellValueFactory(new TreeItemPropertyValueFactory<AbilityInfoModel, String>("statusEffect"));

		banned.setSortable(false);
		name.setSortable(false);
		name.setPrefWidth(240);
		type.setSortable(false);
		trap.setSortable(false);
		zone.setSortable(false);
		zoneTrapSprite.setSortable(false);
		ap.setSortable(false);
		mp.setSortable(false);
		pp.setSortable(false);
		eff.setSortable(false);
		chance.setSortable(false);
		heal.setSortable(false);
		range.setSortable(false);
		radius.setSortable(false);
		piercing.setSortable(false);
		targetMode.setSortable(false);
		nTargets.setSortable(false);
		nTargets.setPrefWidth(120);
		los.setSortable(false);
		los.setPrefWidth(120);
		recursion.setSortable(false);
		instantBefore.setSortable(false);
		instantBefore.setPrefWidth(180);
		instantAfter.setSortable(false);
		instantAfter.setPrefWidth(180);
		statusEffect.setSortable(false);
		statusEffect.setPrefWidth(200);

		banned.setCellFactory(CheckBoxTreeTableCell.<AbilityInfoModel>
			forTreeTableColumn(banned));
		name.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, String>
			forTreeTableColumn(StringField::new, selection));
		type.setCellFactory(ChoiceBoxTreeTableCell.<AbilityInfoModel, String>
			forTreeTableColumn(types));
		trap.setCellFactory(CheckBoxTreeTableCell.<AbilityInfoModel>
			forTreeTableColumn(trap));
		zone.setCellFactory(ChoiceBoxTreeTableCell.<AbilityInfoModel, String>
			forTreeTableColumn(zoneTypes));
		zoneTrapSprite.setCellFactory(ChoiceBoxTreeTableCell.<AbilityInfoModel, SpriteInfo>
			forTreeTableColumn(spriteList));
		ap.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		mp.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		pp.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		eff.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Double>
			forTreeTableColumn(FloatingField::new, selection));
		chance.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Double>
			forTreeTableColumn(FloatingField::new, selection));
		heal.setCellFactory(CheckBoxTreeTableCell.<AbilityInfoModel>
			forTreeTableColumn(heal));
		range.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		radius.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		piercing.setCellFactory(CheckBoxTreeTableCell.<AbilityInfoModel>
			forTreeTableColumn(piercing));
		targetMode.setCellFactory(ChoiceBoxTreeTableCell.<AbilityInfoModel, String>
			forTreeTableColumn(targetModes));
		nTargets.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		los.setCellFactory(CheckBoxTreeTableCell.<AbilityInfoModel>
			forTreeTableColumn(los));
		recursion.setCellFactory(TypedTextFieldTreeTableCell.<AbilityInfoModel, Integer>
			forTreeTableColumn(PositiveIntegerField::new, selection));
		instantBefore.setCellFactory(EffectField.<AbilityInfoModel>
			forTreeTableColumn(enumValues(InstantEffectType.class), selection));
		instantAfter.setCellFactory(EffectField.<AbilityInfoModel>
			forTreeTableColumn(enumValues(InstantEffectType.class), selection));
		statusEffect.setCellFactory(EffectField.<AbilityInfoModel>
			forTreeTableColumn(enumValues(StatusEffectType.class), selection));

		hookOnEditCommit(banned, changed);
		hookOnEditCommit(name, changed);
		hookOnEditCommit(type, changed);
		hookOnEditCommit(trap, changed);
		hookOnEditCommit(zone, changed);
		hookOnEditCommit(zoneTrapSprite, changed);
		hookOnEditCommit(ap, changed);
		hookOnEditCommit(mp, changed);
		hookOnEditCommit(pp, changed);
		hookOnEditCommit(eff, changed);
		hookOnEditCommit(chance, changed);
		hookOnEditCommit(heal, changed);
		hookOnEditCommit(range, changed);
		hookOnEditCommit(radius, changed);
		hookOnEditCommit(piercing, changed);
		hookOnEditCommit(targetMode, changed);
		hookOnEditCommit(nTargets, changed);
		hookOnEditCommit(los, changed);
		hookOnEditCommit(recursion, changed);
		hookOnEditCommit(instantBefore, changed);
		hookOnEditCommit(instantAfter, changed);
		hookOnEditCommit(statusEffect, changed);


		@SuppressWarnings("unchecked")
		boolean v = table.getColumns().setAll(
			banned, name, type, trap, zone, zoneTrapSprite,
			ap, mp, pp, eff, chance, heal, range, radius,
			piercing, targetMode, nTargets, los,
			recursion, instantBefore, instantAfter, statusEffect);
	}

	private static <S, T> void hookOnEditCommit(
		TreeTableColumn<S, T> column, WritableValue<Boolean> changed
	) {
		EventHandler<TreeTableColumn.CellEditEvent<S,T>> oldHandler = column.getOnEditCommit();
		column.setOnEditCommit(e -> {
			changed.setValue(true);
			oldHandler.handle(e);
		});
	}
}

