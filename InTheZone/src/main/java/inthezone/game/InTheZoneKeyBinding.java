package inthezone.game;

import isogame.engine.KeyBinding;

import java.util.ArrayList;
import java.util.List;

public final class InTheZoneKeyBinding extends KeyBinding {
	public static final KeyBinding altpath =
		new InTheZoneKeyBinding("Alternative path");
	public static final KeyBinding enter = new InTheZoneKeyBinding("enter");
	public static final KeyBinding cancel = new InTheZoneKeyBinding("cancel");

	protected InTheZoneKeyBinding(final String name) {
		super(name);
	}

	public static KeyBinding valueOf(final String s) {
		switch (s) {
			case "cancel": return cancel;
			case "enter": return enter;
			case "Alternative path": return altpath;
			default: return KeyBinding.valueOf(s);
		}
	}

	public static List<KeyBinding> allBindings() {
		final List<KeyBinding> r = new ArrayList<>();
		r.add(altpath);
		r.addAll(KeyBinding.allBindings());
		return r;
	}
}

