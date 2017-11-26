package inthezone.game.battle;

import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import inthezone.battle.BattleOutcome;

/**
 * A control to tell the player how the game ended.
 * */
public class EndManager extends HBox {
	private final Label message = new Label(" ");
	private final Button close = new Button("Close");

	/**
	 * @param view a reference back the the BattleView
	 * @param outcome the outcome of the battle
	 * */
	public EndManager(final BattleView view, final BattleOutcome outcome) {
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
			case ERROR: m = "The game has ended due to an error"; break;
			case OTHER_ERROR: m = "The game has ended due to an error on the other player's computer"; break;
			default: throw new RuntimeException("This cannot happen");
		}

		message.setText(m);

		close.setOnAction(event -> view.handleEndBattle(Optional.of(outcome)));
		this.getChildren().addAll(message, close);
	}
}

