package inthezone.game.guiComponents;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;

public class RollingChoiceBox<T> extends Group {
	private final HBox root = new HBox();
	private final ComboBox<T> choice;
	private final Button left = new Button("←");
	private final Button right = new Button("→");

	public RollingChoiceBox() {
		this(new ComboBox<>());
	}

	public RollingChoiceBox(ObservableList<T> items) {
		this(new ComboBox<>(items));
	}

	private RollingChoiceBox(ComboBox<T> choice) {
		this.choice = choice;
		choice.setButtonCell(null);
		choice.setMaxWidth(Double.MAX_VALUE);
		choice.setMaxHeight(Double.MAX_VALUE);
		HBox.setHgrow(choice, Priority.ALWAYS);
		root.setFillHeight(true);
		root.setAlignment(Pos.CENTER);
		root.getChildren().addAll(left, choice, right);
		this.getChildren().add(root);

		choice.setButtonCell(new CenteredListCell<>());

		this.getStyleClass().add("rolling-choice-box");
		left.getStyleClass().add("left-button");
		right.getStyleClass().add("right-button");

		left.setOnAction(event -> diffSelection(-1));
		right.setOnAction(event -> diffSelection(+1));
		choice.setMouseTransparent(true);

		left.setFocusTraversable(false);
		choice.setFocusTraversable(false);
		right.setFocusTraversable(false);
	}

	private void diffSelection(int diff) {
		final SingleSelectionModel s = choice.getSelectionModel();
		final int totalItems = choice.getItems().size();
		final int i = s.getSelectedIndex();
		if (totalItems > 0) {
			if (i == -1) {
				s.select(0);
			} else {
				s.select((i + totalItems + diff) % totalItems);
			}
		}
	}


	// Expose some of the underlying properties of the choicebox
	
	public StringConverter<T> getConverter() { return choice.getConverter(); }
	public void setConverter(StringConverter<T> v) { choice.setConverter(v); }
	public ObjectProperty<StringConverter<T>> converterProperty() {
		return choice.converterProperty();
	}

	public ObservableList<T> getItems() { return choice.getItems(); }
	public void setItems(ObservableList<T> v) { choice.setItems(v); }
	public ObjectProperty<ObservableList<T>> itemsProperty() {
		return itemsProperty();
	}

	public EventHandler<ActionEvent> getOnAction() { return choice.getOnAction(); }
	public void setOnAction(EventHandler<ActionEvent> v) { choice.setOnAction(v); }
	public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
		return onActionProperty();
	}

	public SingleSelectionModel<T> getSelectionModel() { return choice.getSelectionModel(); }
	public void setSelectionModel(SingleSelectionModel<T> v) { choice.setSelectionModel(v); }
	public ObjectProperty<SingleSelectionModel<T>> selectionModelProperty() {
		return selectionModelProperty();
	}

	public T getValue() { return choice.getValue(); }
	public void setValue(T v) { choice.setValue(v); }
	public ObjectProperty<T> valueProperty() {
		return valueProperty();
	}
}

class CenteredListCell<T> extends ListCell<T> {
	public CenteredListCell() {
		this.setMaxWidth(Double.MAX_VALUE);
		this.setAlignment(Pos.BASELINE_CENTER);
	}

	@Override public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		setText(empty || item == null ? null : item.toString());
	}
}

