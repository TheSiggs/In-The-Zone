package inthezone.game.loadoutEditor;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class LoadoutCell extends ListCell<LoadoutModel> {
	public static Callback<ListView<LoadoutModel>, ListCell<LoadoutModel>>
		forListView()
	{
		return (listView -> new LoadoutCell());
	}

	@Override
	public void updateItem(LoadoutModel item, boolean isEmpty) {
		super.updateItem(item, isEmpty);
		
		if (!isEmpty) textProperty().bind(item.name);
	}
}

