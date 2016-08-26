package inthezone.game.battle;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

/**
 * The multitargeting assistant, which is visible only when the number of
 * targets remaining is greater than 1.
 * */
public class MultiTargetAssistant extends FlowPane {
	private final Label message = new Label("");
	private final Button done = new Button("done");

	public MultiTargetAssistant(BattleView view) {
		super();

		this.getStyleClass().add("multitarget-assistant");

		view.numTargets.addListener((o, n0, n) -> {
			if (n.equals(1)) {
				message.setText("Choose " + n + " more target or ");
			} else {
				message.setText("Choose " + n + " more targets or ");
			}
		});
		this.visibleProperty().bind(view.multiTargeting);

		done.setOnAction(event -> view.applyAbility());

		this.getChildren().addAll(message, done);
	}


}

