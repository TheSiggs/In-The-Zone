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

	public EndManager(BattleView view, Optional<BattleOutcome> outcome, boolean resigned) {
		super();

		this.setAlignment(Pos.CENTER);
		this.getStyleClass().add("end-manager");

		message.setText(
			outcome.map(o -> {
				switch (o) {
					case WIN: return "You win!";
					case LOSE: return "You lose.";
					case DRAW: return "It's a draw.";
					default: throw new RuntimeException("This cannot happen");
				}
			}).orElse(resigned? "You resigned." : "Other player resigned."));


		close.setOnAction(event -> view.handleEndBattle(outcome));
		this.getChildren().addAll(message, close);
	}
}

