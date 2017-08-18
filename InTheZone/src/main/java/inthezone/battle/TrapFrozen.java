package inthezone.battle;

/**
 * An immutable view of a trap
 * */
public class TrapFrozen extends TargetableFrozen {
	private final Trap root;

	public TrapFrozen(final Trap root) {
		super(root);
		this.root = root;
	}

	public Ability getAbility() { return root.ability; }
	public CharacterFrozen getParent() { return root.getParent().freeze(); }
}

