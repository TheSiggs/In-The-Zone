package inthezone.battle;

import isogame.engine.MapPoint;

import java.util.HashSet;
import java.util.Set;

/**
 * An immutable view of a zone.
 * */
public class ZoneFrozen extends TargetableFrozen {
	private final Zone root;

	public ZoneFrozen(final Zone root) {
		super(root);
		this.root = root;
	}

	public MapPoint getCentre() { return root.centre; }
	public Set<MapPoint> getRange() { return new HashSet<>(root.range); }
	public Ability getAbility() { return root.ability; }
	public CharacterFrozen getParent() { return root.parent.freeze(); }
}

