package inthezone.game.loadoutEditor;

import inthezone.battle.data.AbilityDescription;
import inthezone.battle.data.AbilityInfo;
import inthezone.battle.data.AbilityType;
import inthezone.battle.data.CharacterProfile;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterProfilePane extends VBox {
}

class PPSpinnerFactory extends SpinnerValueFactory<Integer> { 
	private final int[] values;

	public PPSpinnerFactory(int init, int base, List<Integer> curve) {

		values = new int[1 + curve.size()];
		values[0] = base;
		for (int i = 0; i < curve.size(); i++) values[i + 1] = curve.get(i);

		setConverter(new StringConverter<Integer>() {
			@Override public Integer fromString(String s) {
				final int raw = Integer.parseInt(s);
				for (int i = 0; i < values.length; i++) {
					if (values[i] == raw) return i;
				}
				return 0;
			}

			@Override public String toString(Integer i) {
				if (i < 0 || i >= values.length) {
					return "";
				} else {
					return "" + values[i];
				}
			}
		});

		setValue(init);
		setWrapAround(false);
	}

	@Override public void decrement(int steps) {
		int v0 = getValue() - steps;
		int v1 = getValue() - steps - 1;
		while (v1 >= 0 && values[v0] == values[v1]) {
			v0 -= 1;
			v1 -= 1;
		}
		setValue(v0 < 0? 0 : v0);
	}

	@Override public void increment(int steps) {
		final int v0 = getValue();
		int v1 = getValue() + steps;
		while (v1 < (values.length - 1) && values[v0] == values[v1]) v1 += 1;
		setValue(v1 >= values.length? (values.length - 1) : v1);
	}
}

