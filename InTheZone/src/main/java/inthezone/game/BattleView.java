package inthezone.game;

import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import isogame.GlobalConstants;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.Optional;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	private final MapView canvas;
	private final Paint[] highlights;
	private final Player player;
	private final BattleInProgress battle;

	// ready to accept command requests from the user
	private boolean ready = true;
	private Collection<Character> characters = null;

	// the selected character
	private Optional<Character> selectedCharacter = Optional.empty();

	private final Color sarrowColor = Color.rgb(0x00, 0xFF, 0x00, 0.9);
	private final double[] sarrowx = new double[] {
		GlobalConstants.TILEW * 3.0/8.0,
		GlobalConstants.TILEW * 5.0/8.0,
		GlobalConstants.TILEW / 2.0};
	private final double[] sarrowy = new double[] {
		GlobalConstants.TILEW * (-1.0)/8.0,
		GlobalConstants.TILEW * (-1.0)/8.0,
		0};

	public BattleView(
		StartBattleCommand startBattle, Player player, GameDataFactory gameData
	) {
		super();

		this.player = player;

		highlights = new Paint[] {
			Color.rgb(0x00, 0xFF, 0x00, 0.2),
			Color.rgb(0xFF, 0x00, 0x00, 0.2),
			Color.rgb(0x00, 0x00, 0xFF, 0.2)};

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, highlights);
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());
		canvas.startAnimating();
		canvas.setFocusTraversable(true);
		canvas.doOnSpriteSelection(handleSelection());

		for (Sprite s : startBattle.makeSprites()) {
			s.setDecalRenderer(renderDecals);
			canvas.getStage().addSprite(s);
		}
		
		battle = new BattleInProgress(
			startBattle, player, gameData, this);
		(new Thread(battle)).start();

		this.getChildren().addAll(canvas);
	}

	public void selectCharacter(Optional<Character> c) {
		selectedCharacter = c;
	}

	private Consumer<MapPoint> handleSelection() {
		return p -> {
			if (!ready && characters != null) return;
			if (p == null) {
				selectCharacter(Optional.empty()); return;
			}

			Optional<Character> oc =
				characters.stream().filter(c -> c.getPos() == p).findFirst();

			if (oc.isPresent() && oc.get().player == player) {
				selectCharacter(Optional.of(oc.get()));
			} else {
				selectCharacter(Optional.empty());
			}
		};
	}

	private SpriteDecalRenderer renderDecals = (cx, s, t, angle) -> {
		// selection arrow
		selectedCharacter.ifPresent(c -> {
			if (s.pos.equals(c.getPos())) {
				cx.setFill(sarrowColor);
				cx.fillPolygon(sarrowx, sarrowy, 3);
			}
		});
	};

	@Override
	public void startTurn() {
	}

	@Override
	public void endTurn() {
	}

	@Override
	public void endBattle(boolean playerWins) {
	}

	@Override
	public void badCommand(CommandException e) {
	}

	@Override
	public void command(Command cmd) {
	}
	
	@Override
	public void updateCharacters(Collection<Character> characters) {
		this.characters = characters;
	}
}

