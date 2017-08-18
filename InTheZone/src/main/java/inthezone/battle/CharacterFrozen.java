package inthezone.battle;

import isogame.engine.SpriteInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javafx.scene.image.Image;

import inthezone.battle.data.Player;
import inthezone.battle.status.StatusEffect;

/**
 * A completely immutable view of a character
 * */
public class CharacterFrozen extends TargetableFrozen {
	private final Character root;

	public CharacterFrozen(final Character c) {
		super(c);
		this.root = c;

		getRevengeBonus = root.getRevengeBonus();
		getAP = root.getAP();
		getMP = root.getMP();
		getHP = root.getHP();
		getMaxHP = root.getMaxHP();
		hasCover = root.hasCover();
		isVampiric = root.isVampiric();
		isStunned = root.isStunned();
		isImprisoned = root.isImprisoned();
		isPanicked = root.isPanicked();
		isFeared = root.isFeared();
		getStatusBuff = root.getStatusBuff();
		getStatusDebuff = root.getStatusDebuff();
	}

	public int getId() { return root.id; }
	public String getName() { return root.name; }
 	public Player getPlayer() { return root.player; }
	public SpriteInfo getSprite() { return root.sprite; }
	public Image getPortrait() { return root.portrait; }

	public Collection<Ability> getAbilities() {
		return new ArrayList<>(root.abilities);
	}

	public Ability getBasicAbility() { return root.basicAbility; }

	final double getRevengeBonus;
	final int getAP;
	final int getMP;
	final int getHP;
	final int getMaxHP;
	final boolean hasCover;
	final boolean isVampiric;
	final boolean isStunned;
	final boolean isImprisoned;
	final boolean isPanicked;
	final boolean isFeared;
	final Optional<StatusEffect> getStatusBuff;
	final Optional<StatusEffect> getStatusDebuff;

	public double getRevengeBonus() { return getRevengeBonus; }
	public int getAP() { return getAP; }
	public int getMP() { return getMP; }
	public int getHP() { return getHP; }
	public int getMaxHP() { return getMaxHP; }
	public boolean hasCover() { return hasCover; }
	public boolean isVampiric() { return isVampiric; }
	public boolean isStunned() { return isStunned; }
	public boolean isImprisoned() { return isImprisoned; }
	public boolean isPanicked() { return isPanicked; }
	public boolean isFeared() { return isFeared; }
	public Optional<StatusEffect> getStatusBuff() { return getStatusBuff; }
	public Optional<StatusEffect> getStatusDebuff() { return getStatusDebuff; }

	public boolean isAbilityBlocked(final Ability a) {
		return Character.isAbilityBlocked(
			isDead, isStunned, getStatusBuff, getStatusDebuff, a);
	}
}

