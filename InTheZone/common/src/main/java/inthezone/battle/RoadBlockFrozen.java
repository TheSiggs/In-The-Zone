package inthezone.battle;

/**
 * A completely immutable view of a roadblock
 * */
public class RoadBlockFrozen extends TargetableFrozen {
	private final RoadBlock root;
	private final boolean hasBeenHit;

	public RoadBlockFrozen(final RoadBlock root) {
		super(root);
		this.root = root;
		hasBeenHit = root.hasBeenHit();
	}
	
	public boolean hasBeenHit() { return hasBeenHit; }
}

