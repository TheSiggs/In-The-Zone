package inthezone.battle;

/**
 * A completely immutable view of a roadblock
 * */
public class RoadBlockFrozen extends TargetableFrozen {
	private final RoadBlock root;

	public RoadBlockFrozen(final RoadBlock root) {
		super(root);
		this.root = root;
	}
	
	public boolean hasBeenHit() { return root.hasBeenHit(); }
}

