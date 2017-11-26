package inthezone.dataEditor;

import isogame.gui.TypedTextField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositiveIntegerListTextField extends TypedTextField<List<Integer>> {
	@Override protected Optional<List<Integer>> parseValue(String t) {
		try {
			String[] ts = t.split("\\s*,\\s*");
			List<Integer> r = new ArrayList<>();
			for (String s : ts) {
				r.add(Integer.parseUnsignedInt(s));
			}
			return Optional.of(r);

		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	@Override protected List<Integer> getDefaultValue() {return new ArrayList<>();}

	@Override protected String showValue(List<Integer> is) {
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (Integer i : is) {
			if (!first) out.append(",");
			first = false;
			out.append(i.toString());
		}

		return out.toString();
	}

	public PositiveIntegerListTextField(List<Integer> init) {
		super(init);
	}

	public PositiveIntegerListTextField(String text) {
		super(text);
	}

	public PositiveIntegerListTextField() {
		super();
	}
}

