package inthezone.battle;

import inthezone.battle.data.WeaponInfo;

public class Weapon extends InventoryItem {
	public final WeaponInfo info;

	public Weapon(WeaponInfo info) {
		this.info = info;
	}
}

