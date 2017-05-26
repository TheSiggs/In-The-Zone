package inthezone.dataEditor;

import inthezone.battle.data.AbilityInfo;
import isogame.engine.SpriteInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AbilityInfoModel {
	private final BooleanProperty banned;
	private final StringProperty name;
	private final StringProperty icon;
	private final StringProperty type;
	private final BooleanProperty trap;
	private final StringProperty zone;
	private final ObjectProperty<SpriteInfo> zoneTrapSprite;
	private final IntegerProperty ap;
	private final IntegerProperty mp;
	private final IntegerProperty pp;
	private final DoubleProperty eff;
	private final DoubleProperty chance;
	private final BooleanProperty heal;

	private final IntegerProperty range;
	private final IntegerProperty radius;
	private final BooleanProperty piercing;
	private final StringProperty targetMode;
	private final IntegerProperty nTargets;
	private final BooleanProperty los;

	private final BooleanProperty isMana;
	private final BooleanProperty isSubsequent;
	private final IntegerProperty recursion;

	private final StringProperty instantBefore;
	private final StringProperty instantAfter;
	private final StringProperty statusEffect;

	public AbilityInfoModel(boolean isMana, boolean isSubsequent) {
		this.banned = new SimpleBooleanProperty(false);
		this.name = new SimpleStringProperty("New ability");
		this.icon = new SimpleStringProperty("");
		this.type = new SimpleStringProperty("skill");
		this.trap = new SimpleBooleanProperty(false);
		this.zone = new SimpleStringProperty("None");
		this.zoneTrapSprite = new SimpleObjectProperty<>(null);
		this.ap = new SimpleIntegerProperty(2);
		this.mp = new SimpleIntegerProperty(0);
		this.pp = new SimpleIntegerProperty(1);
		this.eff = new SimpleDoubleProperty(1.0);
		this.chance = new SimpleDoubleProperty(1.0);
		this.heal = new SimpleBooleanProperty(false);
		this.range = new SimpleIntegerProperty(1);
		this.radius = new SimpleIntegerProperty(1);
		this.piercing = new SimpleBooleanProperty(false);
		this.targetMode = new SimpleStringProperty("E");
		this.nTargets = new SimpleIntegerProperty(1);
		this.los = new SimpleBooleanProperty(true);
		this.isMana = new SimpleBooleanProperty(isMana);
		this.isSubsequent = new SimpleBooleanProperty(isSubsequent);
		this.recursion = new SimpleIntegerProperty(0);
		this.instantBefore = new SimpleStringProperty("none");
		this.instantAfter = new SimpleStringProperty("none");
		this.statusEffect = new SimpleStringProperty("none");
	}

	public void init(AbilityInfo i) {
		this.banned.setValue(i.banned);
		this.name.setValue(i.name);
		this.icon.setValue(i.iconFile);
		this.type.setValue(i.type.toString().toLowerCase());
		this.trap.setValue(i.trap);
		this.zone.setValue(i.zone.toString().toLowerCase());
		this.zoneTrapSprite.setValue(i.zoneTrapSprite.orElse(null));
		this.ap.setValue(i.ap);
		this.mp.setValue(i.mp);
		this.pp.setValue(i.pp);
		this.eff.setValue(i.eff);
		this.chance.setValue(i.chance);
		this.heal.setValue(i.heal);
		this.range.setValue(i.range.range);
		this.radius.setValue(i.range.radius);
		this.piercing.setValue(i.range.piercing);
		this.targetMode.setValue(i.range.targetMode.toString());
		this.nTargets.setValue(i.range.nTargets);
		this.los.setValue(i.range.los);
		this.recursion.setValue(i.recursion);
		this.instantBefore.setValue(i.instantBefore.map(x -> x.toString().toLowerCase()).orElse("none"));
		this.instantAfter.setValue(i.instantAfter.map(x -> x.toString().toLowerCase()).orElse("none"));
		this.statusEffect.setValue(i.statusEffect.map(x -> x.toString().toLowerCase()).orElse("none"));
	}

	public AbilityInfoModel cloneMana() {
		AbilityInfoModel r = this.clone(true, false);
		r.name.setValue(name.getValue() + " + Mana");
		r.ap.setValue(0);
		r.mp.setValue(0);
		r.pp.setValue(0);
		return r;
	}

	public AbilityInfoModel cloneSubsequent() {
		AbilityInfoModel r = this.clone(false, true);
		String oldName = name.getValue();
		r.name.setValue(oldName.endsWith("I")? oldName + "I" : oldName + " II");
		r.ap.setValue(0);
		r.mp.setValue(0);
		r.pp.setValue(0);
		return r;
	}

	private AbilityInfoModel clone(boolean isMana, boolean isSubsequent) {
		AbilityInfoModel r = new AbilityInfoModel(isMana, isSubsequent);
		r.banned.setValue(banned.getValue());
		r.name.setValue(name.getValue());
		r.icon.setValue(icon.getValue());
		r.type.setValue(type.getValue());
		r.trap.setValue(trap.getValue());
		r.zone.setValue(zone.getValue());
		r.zoneTrapSprite.setValue(zoneTrapSprite.getValue());
		r.ap.setValue(ap.getValue());
		r.mp.setValue(mp.getValue());
		r.pp.setValue(pp.getValue());
		r.eff.setValue(eff.getValue());
		r.chance.setValue(chance.getValue());
		r.heal.setValue(heal.getValue());
		r.radius.setValue(radius.getValue());
		r.piercing.setValue(piercing.getValue());
		r.targetMode.setValue(targetMode.getValue());
		r.nTargets.setValue(nTargets.getValue());
		r.los.setValue(los.getValue());
		r.recursion.setValue(recursion.getValue());
		r.instantBefore.setValue(instantBefore.getValue());
		r.instantAfter.setValue(instantAfter.getValue());
		r.statusEffect.setValue(statusEffect.getValue());
		return r;
	}

	public BooleanProperty bannedProperty() { return banned; }
	public boolean getBanned() { return banned.getValue(); }

	public StringProperty nameProperty() { return name; }
	public String getName() { return name.getValue(); }

	public StringProperty iconProperty() { return icon; }
	public String getIcon() { return icon.getValue(); }

	public StringProperty typeProperty() { return type; }
	public String getType() { return type.getValue(); }

	public BooleanProperty trapProperty() { return trap; }
	public boolean getTrap() { return trap.getValue(); }

	public StringProperty zoneProperty() { return zone; }
	public String getZone() { return zone.getValue(); }

	public ObjectProperty<SpriteInfo> zoneTrapSpriteProperty() { return zoneTrapSprite; }
	public SpriteInfo getZoneTrapSprite() { return zoneTrapSprite.getValue(); }

	public IntegerProperty apProperty() { return ap; }
	public int getAP() { return ap.getValue(); }

	public IntegerProperty mpProperty() { return mp; }
	public int getMP() { return mp.getValue(); }

	public IntegerProperty ppProperty() { return pp; }
	public int getPP() { return pp.getValue(); }

	public DoubleProperty effProperty() { return eff; }
	public double getEff() { return eff.getValue(); }

	public DoubleProperty chanceProperty() { return chance; }
	public double getChance() { return chance.getValue(); }

	public BooleanProperty healProperty() { return heal; }
	public boolean getHeal() { return heal.getValue(); }

	public IntegerProperty rangeProperty() { return range; }
	public int getRange() { return range.getValue(); }

	public IntegerProperty radiusProperty() { return radius; }
	public int getRadius() { return radius.getValue(); }

	public BooleanProperty piercingProperty() { return piercing; }
	public boolean getPiercing() { return piercing.getValue(); }

	public StringProperty targetModeProperty() { return targetMode; }
	public String getTargetMode() { return targetMode.getValue(); }

	public IntegerProperty nTargetsProperty() { return nTargets; }
	public int getnTargets() { return nTargets.getValue(); }

	public BooleanProperty losProperty() { return los; }
	public boolean getLOS() { return los.getValue(); }

	public BooleanProperty isManaProperty() { return isMana; }
	public boolean getIsMana() { return isMana.getValue(); }

	public BooleanProperty isSubsequentProperty() { return isSubsequent; }
	public boolean getIsSubsequent() { return isSubsequent.getValue(); }

	public IntegerProperty recursionProperty() { return recursion; }
	public int getRecursion() { return recursion.getValue(); }

	public StringProperty instantBeforeProperty() { return instantBefore; }
	public String getInstantBefore() { return instantBefore.getValue(); }

	public StringProperty instantAfterProperty() { return instantAfter; }
	public String getInstantAfter() { return instantAfter.getValue(); }

	public StringProperty statusEffectProperty() { return statusEffect; }
	public String getStatusEffect() { return statusEffect.getValue(); }
}

