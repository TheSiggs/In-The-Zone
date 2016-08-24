package inthezone.game.battle;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.commands.MoveCommandRequest;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import inthezone.comptroller.Network;
import inthezone.game.DialogScreen;
import isogame.engine.AnimationChain;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import isogame.engine.Stage;
import isogame.GlobalConstants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import static inthezone.game.battle.BattleViewMode.*;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	private final static double walkSpeed = 1.2;

	private final MapView canvas;
	private final Player player;
	private final BattleInProgress battle;

	private final static int HIGHLIGHT_TARGET = 0;
	private final static int HIGHLIGHT_MOVE = 1;
	private final static int HIGHLIGHT_PATH = 2;
	private final Paint[] highlights = new Paint[] {
		Color.rgb(0xFF, 0x00, 0x00, 0.2),
		Color.rgb(0x00, 0xFF, 0x00, 0.2),
		Color.rgb(0x00, 0x00, 0xFF, 0.2)};

	// The HUD GUI components
	private final HUD hud;

	// Map from character ids to characters
	private Map<Integer, Character> characters = null;

	// the selected character
	private Optional<Character> selectedCharacter = Optional.empty();

	// the current ability.  If mode is TARGET then this must not be null.
	private Ability targetingAbility = null;

	private BattleViewMode mode = SELECT;

	// status properties for the HUD
	public final BooleanProperty isMyTurn = new SimpleBooleanProperty(true);
	public final BooleanProperty isCharacterSelected = new SimpleBooleanProperty(false);

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
		StartBattleCommand startBattle, Player player,
		CommandGenerator otherPlayer,
		Network network, GameDataFactory gameData
	) {
		super();

		this.player = player;
		this.hud = new HUD(this);

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, highlights);
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());
		canvas.startAnimating();
		canvas.setFocusTraversable(true);
		canvas.doOnSelection(handleSelection());
		canvas.doOnMouseOver(handleMouseOver());
		canvas.doOnMouseOut(handleMouseOut());

		Collection<Sprite> sprites = startBattle.makeSprites();
		for (Sprite s : sprites) {
			Stage stage = canvas.getStage();
			stage.addSprite(s);
			s.setDecalRenderer(renderDecals);

			AnimationChain chain = new AnimationChain(s);
			stage.registerAnimationChain(chain);
			chain.doOnFinished(() -> {
				s.setAnimation("idle");
				if (selectedCharacter.isPresent()) setMode(MOVE); else setMode(SELECT);
			});
		}

		canvas.setSelectableSprites(sprites);
		
		battle = new BattleInProgress(
			startBattle, player, otherPlayer, network, gameData, this);
		(new Thread(battle)).start();

		this.getChildren().addAll(canvas, hud);
	}

	private static <T> Optional<T> getFutureWithRetry(Future<T> f) {
		while (true) {
			try {
				return Optional.of(f.get());
			} catch (InterruptedException e) {
				/* ignore */
			} catch (ExecutionException e) {
				return Optional.empty();
			}
		}
	}

	public void selectCharacterById(int id) {
		selectCharacter(Optional.ofNullable(characters.get(id)));
	}

	public void selectCharacter(Optional<Character> c) {
		if (mode != OTHER_TURN && mode != ANIMATING) {
			selectedCharacter = c;
			if (c.isPresent()) {
				isCharacterSelected.setValue(true);
				setMode(MOVE);
			} else {
				isCharacterSelected.setValue(false);
				setMode(SELECT);
			}
			hud.selectCharacter(c.orElse(null));
		}
	}

	private Consumer<MapPoint> handleSelection() {
		return p -> {
			if (mode == ANIMATING) return;
			if (p == null) {
				selectCharacter(Optional.empty()); return;
			}

			Optional<Character> oc = characters.values().stream()
				.filter(c -> c.getPos().equals(p)).findFirst();

			switch (mode) {
				case SELECT:
					if (oc.isPresent() && oc.get().player == player) {
						selectCharacter(Optional.of(oc.get()));
					} else {
						selectCharacter(Optional.empty());
					}
					break;

				case MOVE:
					if (oc.isPresent() && oc.get().player == player) {
						selectCharacter(Optional.of(oc.get()));
					} else if (canvas.isSelectable(p)) {
						selectedCharacter.ifPresent(c -> battle.requestCommand(
							new MoveCommandRequest(c.getPos(), p, c.player)));
						setMode(MOVE);
					} else {
						selectCharacter(Optional.empty());
					}
					break;

				case TARGET:
					break;
			}

		};
	}

	private void setMode(BattleViewMode mode) {
		Stage stage;
		Character c;

		switch (mode) {
			case OTHER_TURN:
				canvas.getStage().clearAllHighlighting();
				selectCharacter(Optional.empty());
				break;

			case ANIMATING:
				canvas.getStage().clearAllHighlighting();
				break;

			case SELECT:
				canvas.getStage().clearAllHighlighting();
				break;

			case MOVE:
				canvas.getStage().clearAllHighlighting();
				c = selectedCharacter.orElseThrow(() -> new RuntimeException(
					"Attempted to move but no character was selected"));

				stage = canvas.getStage();
				getFutureWithRetry(battle.getMoveRange(c)).ifPresent(mr -> {
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
					canvas.setSelectable(mr);
				});
				break;

			case TARGET:
				canvas.getStage().clearAllHighlighting();
				c = selectedCharacter.orElseThrow(() -> new RuntimeException(
					"Attempted to move but no character was selected"));
				if (targetingAbility == null)
					throw new RuntimeException("Attempted to target null ability");

				stage = canvas.getStage();
				getFutureWithRetry(battle.getTargetingInfo(c, targetingAbility))
					.ifPresent(tr -> {
						tr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
						canvas.setSelectable(tr);
					});
				break;
		}
		this.mode = mode;
	}

	private Consumer<MapPoint> handleMouseOver() {
		return p -> {
			canvas.getStage().clearHighlighting(2);

			selectedCharacter.ifPresent(c -> {
				Stage stage = canvas.getStage();
				getFutureWithRetry(battle.getPath(c, p)).ifPresent(path -> {
					path.stream().forEach(pp -> stage.setHighlight(pp, HIGHLIGHT_PATH));});
			});
		};
	}

	private Runnable handleMouseOut() {
		return () -> {
			canvas.getStage().clearHighlighting(2);
		};
	}

	private SpriteDecalRenderer renderDecals = (cx, s, t, angle) -> {
		// selection arrow
		selectedCharacter.ifPresent(c -> {
			if (s.userData.equals(c.id)) {
				cx.setFill(sarrowColor);
				cx.fillPolygon(sarrowx, sarrowy, 3);
			}
		});
	};

	/**
	 * Send the end turn message.
	 * */
	public void sendEndTurn() {
		battle.requestCommand(new EndTurnCommandRequest());
	}

	/**
	 * The selected character uses an ability.
	 * */
	public void useAbility(Ability ability) {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to target ability but no character was selected");
		targetingAbility = ability;
		setMode(TARGET);
	}

	/**
	 * The selected character attacks.
	 * */
	public void useAttack() {
	}

	/**
	 * The selected character initiates a push.
	 * */
	public void usePush() {
	}

	@Override
	public void startTurn(List<Character> characters) {
		isMyTurn.setValue(true);
		updateCharacters(characters);
		setMode(SELECT);
	}

	@Override
	public void endTurn(List<Character> characters) {
		isMyTurn.setValue(false);
		updateCharacters(characters);
		setMode(OTHER_TURN);
	}

	@Override
	public void endBattle(boolean playerWins) {
	}

	@Override
	public void badCommand(CommandException e) {
	}

	@Override
	public void command(Command cmd, List<Character> affectedCharacters) {
		if (cmd instanceof MoveCommand) {
			List<MapPoint> path = ((MoveCommand) cmd).path;
			if (path.size() < 2) return;

			setMode(ANIMATING);
			Stage stage = canvas.getStage();
			MapPoint start = path.get(0);
			MapPoint end = path.get(1);
			MapPoint v = end.subtract(start);

			int id = affectedCharacters.get(0).id;
			Sprite s = stage.getSpritesByTile(start).stream()
				.filter(x -> x.userData.equals(id)).findFirst().get();

			for (MapPoint p : path.subList(2, path.size())) {
				if (!end.add(v).equals(p)) {
					stage.queueMoveSprite(s, start, end, "walk", walkSpeed);
					start = end;
					v = p.subtract(start);
				}
				end = p;
			}

			stage.queueMoveSprite(s, start, end, "walk", walkSpeed);
		}

		updateCharacters(affectedCharacters);
	}
	
	private void updateCharacters(List<Character> characters) {
		if (this.characters == null) {
			this.characters = new HashMap<>();
			for (Character c : characters) this.characters.put(c.id, c);
			hud.init(characters.stream()
				.filter(c -> c.player == player).collect(Collectors.toList()));
		} else {
			for (Character c : characters) {
				Character old = this.characters.get(c.id);
				selectedCharacter.ifPresent(sc -> {
					if (sc.id == c.id) selectedCharacter = Optional.of(c);
				});
				if (old != null) {
					CharacterInfoBox box = hud.characters.get(c.id);
					if (box != null) {
						box.updateAP(c.getAP(), c.getStats().ap);
						box.updateMP(c.getMP(), c.getStats().mp);
						box.updateHP(c.getHP(), c.getMaxHP());
					}
				}
				this.characters.put(c.id, c);
			}
		}
	}
}

