package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CharacterIndicatorCell extends ListCell<CharacterProfileModel> {
	private CharacterIndicatorPane cell = null;

	public static Callback<ListView<CharacterProfileModel>, ListCell<CharacterProfileModel>>
		forListView()
	{
		return (listView -> new CharacterIndicatorCell());
	}

	@Override
	public void updateItem(CharacterProfileModel profile, boolean empty) {
		super.updateItem(profile, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			cell = new CharacterIndicatorPane(profile);
			setGraphic(cell);
		}
	}
}

