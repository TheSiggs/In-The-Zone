package inthezone.game;

import isogame.engine.KeyBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Key binding end points.
 * */
public final class InTheZoneKeyBinding extends KeyBinding {
	public static final KeyBinding altpath =
		new InTheZoneKeyBinding("Alternative path", false);
	public static final KeyBinding enter =
		new InTheZoneKeyBinding("Enter", false);
	public static final KeyBinding cancel =
		new InTheZoneKeyBinding("Cancel", false);

	public static final KeyBinding next =
		new InTheZoneKeyBinding("Next character", true);
	public static final KeyBinding prev =
		new InTheZoneKeyBinding("Previous character", true);
	public static final KeyBinding character1 =
		new InTheZoneKeyBinding("Select character 1", true);
	public static final KeyBinding character2 =
		new InTheZoneKeyBinding("Select character 2", true);
	public static final KeyBinding character3 =
		new InTheZoneKeyBinding("Select character 3", true);
	public static final KeyBinding character4 =
		new InTheZoneKeyBinding("Select character 4", true);
	public static final KeyBinding clearSelected =
		new InTheZoneKeyBinding("Clear selection", true);

	public static final KeyBinding attack =
		new InTheZoneKeyBinding("Attack", true);
	public static final KeyBinding push =
		new InTheZoneKeyBinding("Push", true);
	public static final KeyBinding potion =
		new InTheZoneKeyBinding("Use potion", true);
	public static final KeyBinding special =
		new InTheZoneKeyBinding("Special ability", true);
	public static final KeyBinding a1 =
		new InTheZoneKeyBinding("Ability 1", true);
	public static final KeyBinding a2 =
		new InTheZoneKeyBinding("Ability 2", true);
	public static final KeyBinding a3 =
		new InTheZoneKeyBinding("Ability 3", true);
	public static final KeyBinding a4 =
		new InTheZoneKeyBinding("Ability 4", true);
	public static final KeyBinding a5 =
		new InTheZoneKeyBinding("Ability 5", true);
	public static final KeyBinding a6 =
		new InTheZoneKeyBinding("Ability 6", true);

	public static final KeyBinding endTurn =
		new InTheZoneKeyBinding("End turn", true);

	public final boolean hudBinding;
	protected InTheZoneKeyBinding(
		final String name, final boolean hudBinding
	) {
		super(name);
		this.hudBinding = hudBinding;
	}

	public static KeyBinding valueOf(final String s) {
		switch (s) {
			case "Cancel": return cancel;
			case "Enter": return enter;
			case "Alternative path": return altpath;
			case "Next character": return next;
			case "Previous character": return prev;
			case "Select character 1": return character1;
			case "Select character 2": return character2;
			case "Select character 3": return character3;
			case "Select character 4": return character4;
			case "Clear selection": return clearSelected;
			case "Attack": return attack;
			case "Push": return push;
			case "Use potion": return potion;
			case "Special ability": return special;
			case "Ability 1": return a1;
			case "Ability 2": return a2;
			case "Ability 3": return a3;
			case "Ability 4": return a4;
			case "Ability 5": return a5;
			case "Ability 6": return a6;
			case "End turn": return endTurn;
			default: return KeyBinding.valueOf(s);
		}
	}

	public static List<KeyBinding> allBindings() {
		final List<KeyBinding> r = new ArrayList<>();
		r.add(altpath);
		r.addAll(KeyBinding.allBindings());
		r.add(next);
		r.add(prev);
		r.add(character1);
		r.add(character2);
		r.add(character3);
		r.add(character4);
		r.add(clearSelected);
		r.add(attack);
		r.add(push);
		r.add(potion);
		r.add(special);
		r.add(a1);
		r.add(a2);
		r.add(a3);
		r.add(a4);
		r.add(a5);
		r.add(a6);
		r.add(endTurn);
		return r;
	}
}

