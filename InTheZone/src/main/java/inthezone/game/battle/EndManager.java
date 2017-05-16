package inthezone.game.battle;

import inthezone.battle.BattleOutcome;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import java.util.Optional;

public class EndManager extends HBox {
	private final Label message = new Label(" ");
	private final Button close = new Button("Close");

	public EndManager(BattleView view, BattleOutcome outcome) {
		super();

		this.setAlignment(Pos.CENTER);
		this.getStyleClass().add("end-manager");

		String m = "";
		switch (outcome) {
			case WIN: m = "You win!"; break;
			case LOSE: m = "You lose."; break;
			case DRAW: m = "It's a draw."; break;
			case RESIGN: m = "You resigned."; break;
			case OTHER_RESIGNED: m = "Other player resigned."; break;
			case OTHER_LOGGED_OUT: m = "Other player logged out or disconnected."; break;
			default: throw new RuntimeException("This cannot happen");
		}

		message.setText(m);

		close.setOnAction(event -> view.handleEndBattle(Optional.of(outcome)));
		this.getChildren().addAll(message, close);
	}
}

