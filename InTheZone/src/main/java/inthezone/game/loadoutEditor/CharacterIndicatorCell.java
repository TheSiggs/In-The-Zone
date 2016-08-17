package inthezone.game.loadoutEditor;

import inthezone.battle.data.CharacterProfile;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CharacterIndicatorCell extends ListCell<CharacterProfile> {
	private CharacterIndicatorPane cell = null;

	public static Callback<ListView<CharacterProfile>, ListCell<CharacterProfile>>
		forListView()
	{
		return (listView -> new CharacterIndicatorCell());
	}

	@Override
	public void updateItem(CharacterProfile profile, boolean empty) {
		super.updateItem(profile, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (cell == null) { 
				cell = new CharacterIndicatorPane(profile);
			} else {
				cell.updateProfile(profile);
			}
			setGraphic(cell);
		}
	}
}

