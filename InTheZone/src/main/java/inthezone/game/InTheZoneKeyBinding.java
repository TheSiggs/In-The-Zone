package inthezone.game;

import isogame.engine.KeyBinding;

import java.util.HashSet;
import java.util.Set;

public final class InTheZoneKeyBinding extends KeyBinding {
	public static final KeyBinding cancel = new InTheZoneKeyBinding("cancel");
	public static final KeyBinding enter = new InTheZoneKeyBinding("enter");
	public static final KeyBinding altpath = new InTheZoneKeyBinding("altpath");

	protected InTheZoneKeyBinding(final String name) {
		super(name);
	}

	public static KeyBinding valueOf(final String s) {
		switch (s) {
			case "cancel": return cancel;
			case "enter": return enter;
			case "altpath": return altpath;
			default: return KeyBinding.valueOf(s);
		}
	}

	public static Set<KeyBinding> allBindings() {
		final Set<KeyBinding> r = new HashSet<>();
		r.add(cancel);
		r.add(enter);
		r.add(altpath);
		r.addAll(KeyBinding.allBindings());
		return r;
	}
}

