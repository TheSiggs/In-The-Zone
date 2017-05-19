package inthezone.battle.data;

public class TargetMode {
	public final boolean self;
	public final boolean enemies;
	public final boolean allies;

	public TargetMode(String mode) {
		this.self = mode.contains("S");
		this.enemies = mode.contains("E");
		this.allies = mode.contains("A");
	}

	@Override public boolean equals(Object x) {
		if (x == null) {
			return false;
		} else if (x instanceof TargetMode) {
			final TargetMode t = (TargetMode) x;
			return t.self == self && t.enemies == enemies && t.allies == allies;
		} else return false;
	}

	@Override public int hashCode() {
		return Boolean.hashCode(self) + 2 * Boolean.hashCode(enemies) + 4 * Boolean.hashCode(allies);
	}

	@Override
	public String toString() {
		return
			(self? "S" : "") +
			(enemies? "E" : "") +
			(allies? "A" : "");
	}
}

