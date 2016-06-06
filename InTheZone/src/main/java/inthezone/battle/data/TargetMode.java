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

	@Override
	public String toString() {
		return
			(self? "S" : "") +
			(enemies? "E" : "") +
			(allies? "A" : "");
	}
}
