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
	public double getRevengeBonus() { return root.getRevengeBonus(); }

	public int getAP() { return root.getAP(); }
	public int getMP() { return root.getMP(); }
	public int getHP() { return root.getHP(); }
	public int getMaxHP() { return root.getMaxHP(); }
	public boolean hasCover() { return root.hasCover(); }
	public boolean isVampiric() { return root.isVampiric(); }
	public boolean isStunned() { return root.isStunned(); }
	public boolean isImprisoned() { return root.isImprisoned(); }
	public boolean isPanicked() { return root.isPanicked(); }
	public boolean isFeared() { return root.isFeared(); }

	public boolean isAbilityBlocked(final Ability a) {
		return root.isAbilityBlocked(a);
	}

	public Optional<StatusEffect> getStatusBuff() { return root.getStatusBuff(); }
	public Optional<StatusEffect> getStatusDebuff() {
		return root.getStatusDebuff();
	}
}

