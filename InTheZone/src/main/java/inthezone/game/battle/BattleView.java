package inthezone.game.battle;

import isogame.engine.CorruptDataException;
import isogame.engine.KeyBinding;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.ResignCommandRequest;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.Move;
import inthezone.battle.instant.Teleport;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import inthezone.comptroller.InfoMoveRange;
import inthezone.comptroller.Network;
import inthezone.game.DialogScreen;
import inthezone.game.InTheZoneKeyBinding;
import inthezone.protocol.ProtocolException;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	// battle state
	public final BattleInProgress battle;
	public final Player player;

	public final String playerAName;
	public final String playerBName;

	public final CommandProcessor commands;
	public final SpriteManager sprites;
	private Optional<Character> selectedCharacter = Optional.empty();

	// UI components
	private final MapView canvas;
	public final HUD hud;

	// Status properties for the HUD
	public final BooleanProperty isMyTurn = new SimpleBooleanProperty(true);
	public final BooleanProperty isCharacterSelected = new SimpleBooleanProperty(false);
	public final BooleanProperty multiTargeting = new SimpleBooleanProperty(false);
	public final IntegerProperty numTargets = new SimpleIntegerProperty(0);
	public final BooleanProperty cannotCancel = new SimpleBooleanProperty(false);
	public final IntegerProperty remainingPotions = new SimpleIntegerProperty(1);

	// Are we expecting an animation completion event from the game engine.
	private boolean inAnimation = false;

	// Instant effect completions must be delayed until the command queue is empty.
	private Optional<Runnable> instantEffectCompletion = Optional.empty();

	public final ModalDialog modalDialog = new ModalDialog(this);

	/**
	 * Start a standard battle
	 * */
	public BattleView(
		final StartBattleCommand startBattle,
		final Player player,
		final CommandGenerator otherPlayer,
		final Optional<Network> network,
		final GameDataFactory gameData
	) throws CorruptDataException {
		this(startBattle, player,
			Optional.empty(), otherPlayer, network, gameData,
			view -> new StandardHUD(view, gameData.getStandardSprites()));
	}

	/**
	 * Start a saved battle in replay mode
	 * */
	public BattleView(
		final PlaybackGenerator pb,
		final InputStream in,
		final GameDataFactory gameData
	) throws IOException, ProtocolException, CorruptDataException {
		this(
			pb.start(new BufferedReader(new InputStreamReader(in, "UTF-8")), gameData),
			Player.PLAYER_OBSERVER,
			Optional.of(pb), pb,
			Optional.empty(), gameData,
			view -> new ReplayHUD(view, gameData.getStandardSprites(), pb));
	}

	public BattleView(
		final StartBattleCommand startBattle,
		final Player player,
		final Optional<CommandGenerator> thisPlayerGenerator,
		final CommandGenerator otherPlayer,
		final Optional<Network> network,
		final GameDataFactory gameData,
		final Function<BattleView, HUD> hud
	) throws CorruptDataException {
		this.setMinSize(0, 0);

		this.player = player;
		this.playerAName = startBattle.p1Name;
		this.playerBName = startBattle.p2Name;

		this.commands = new CommandProcessor(this);
		this.hud = hud.apply(this);

		final DecalRenderer decals = new DecalRenderer(this,
			gameData.getStandardSprites());

		System.err.println("Playing as " + player);

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, false,
			Highlighters.highlights);

		canvas.startAnimating();
		canvas.setFocusTraversable(true);
		canvas.doOnSelection(
			(selection, button) -> {
				if (modalDialog.isShowing()) {
					if (button == MouseButton.SECONDARY) {
						modalDialog.doCancel();
					}
				} else if (button == MouseButton.SECONDARY) {
					if (mode.canCancel()) cancelAbility();
				} else if (selection.isEmpty() && mode.canCancel()) {
					selectCharacter(Optional.empty());
				} else {
					mode.handleSelection(selection);
				}
			});
		canvas.doOnMouseOverSprite(decals::handleMouseOver);
		canvas.doOnMouseOutSprite(decals::handleMouseOut);
		canvas.doOnMouseOver(p -> mode.handleMouseOver(p));
		canvas.doOnMouseOut(() -> mode.handleMouseOut());

		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.W), KeyBinding.scrollUp);
		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.A), KeyBinding.scrollLeft);
		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.S), KeyBinding.scrollDown);
		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.D), KeyBinding.scrollRight);
		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.ESCAPE), InTheZoneKeyBinding.cancel);
		canvas.keyBindings.keys.put(new KeyCodeCombination(KeyCode.ENTER), InTheZoneKeyBinding.enter);

		canvas.doOnKeyReleased(action -> {
			if (action == InTheZoneKeyBinding.cancel) {
				if (modalDialog.isShowing()) {
					modalDialog.doCancel();
				} else {
					cancelAbility();
				}
			} else if (action == InTheZoneKeyBinding.enter) {
				if (modalDialog.isShowing()) {
					modalDialog.doDefault();
				}
			}
		});

		final Collection<Sprite> allSprites = startBattle.makeSprites();
		this.sprites = new SpriteManager(
			this, allSprites, gameData.getStandardSprites(), decals,
			() -> {
				inAnimation = false;

				while (!inAnimation && !commands.isEmpty()) {
					inAnimation = commands.doNextCommand();
				}

				if (!inAnimation) {
					if (commands.isEmpty() && instantEffectCompletion.isPresent()) {
						instantEffectCompletion.get().run();
						instantEffectCompletion = Optional.empty();
					} else if (commands.isComplete()) {
						setMode(mode.animationDone());
					}
				}
		});

		final Player firstPlayer;
		if (player == Player.PLAYER_OBSERVER) {
			if (startBattle.p1GoesFirst) {
				firstPlayer = Player.PLAYER_A;
			} else {
				firstPlayer = Player.PLAYER_B;
			}
		} else {
			firstPlayer = player;
		}

		battle = new BattleInProgress(
			startBattle, firstPlayer, thisPlayerGenerator,
			otherPlayer, network, gameData, this);

		(new Thread(battle)).start();

		// init the mode
		canvas.setSelectableSprites(allSprites);
		canvas.setMouseOverSprites(allSprites);

		final List<MapPoint> startTiles = startBattle.getStartTiles(player);
		if (startTiles.size() > 0) canvas.centreOnTile(startTiles.get(0));

		setMode(new ModeOtherTurn(this));
		this.getChildren().addAll(canvas, this.hud, modalDialog);
	}

	private Mode mode;

	public Mode getMode() {return mode;}

	/**
	 * Switch to a different UI mode.
	 * */
	public void setMode(final Mode mode) {
		this.mode = mode.setupMode();
		this.cannotCancel.setValue(!mode.canCancel());
		System.err.println("Set mode " + mode + " transforming to " + this.mode);
		if ((this.mode instanceof ModeSelect ||
			this.mode instanceof ModeMove) && !anyValidMoves()
		) {
			sendEndTurn();
		}
	}

	/**
	 * Update the UI mode to reflect characters moving around.
	 * */
	public void retargetMode(final Map<MapPoint, MapPoint> retargeting) {
		this.mode = this.mode.retarget(retargeting);
		this.cannotCancel.setValue(!mode.canCancel());
	}

	/**
	 * Restore the highlighting.
	 * */
	public void resetHighlighting() {
		setMode(this.mode);
	}

	/**
	 * Get the currently selected character.
	 * */
	public Optional<Character> getSelectedCharacter() {return selectedCharacter;}

	/**
	 * Select a particular character.
	 * */
	public void selectCharacterById(final int id) {
		if (mode.canCancel()) {
			selectCharacter(Optional.ofNullable(sprites.getCharacterById(id)));
		}
	}

	/**
	 * Select or deselect a character.
	 * @param c If empty, then deselect all characters.
	 * */
	public void selectCharacter(final Optional<Character> c) {
		if (mode.canCancel()) {
			outOfTurnSelect(c);
			if (mode.isInteractive()) {
				if (!(mode instanceof ModeSelect || mode instanceof ModeMove)) {
					hud.writeMessage("Cancelled");
				}
				setMode(new ModeSelect(this));
			}
		}
	}

	/**
	 * Select or deselect a character when it's not your turn.
	 * @param c If empty, the deselect all characters.
	 * */
	public void outOfTurnSelect(final Optional<Character> c) {
		battle.cancel();
		canvas.resetMouseHandlers();
		selectedCharacter = c;
		isCharacterSelected.setValue(c.isPresent());
		hud.selectCharacter(c);
	}

	public void cancelAbility() {
		if (mode instanceof ModeMove) {
			selectCharacter(Optional.empty());
		} else {
			selectCharacter(selectedCharacter);
		}
	}

	/**
	 * Update information about the selected character.
	 * */
	public void updateSelectedCharacter(final Character c) {
		selectedCharacter.ifPresent(sc -> {
			if (sc.id == c.id) {
				selectedCharacter = Optional.of(c);
				hud.selectCharacter(selectedCharacter);
				setMode(mode.updateSelectedCharacter(c));
			}
		});
	}

	/**
	 * Get the current stage.
	 * */
	public Stage getStage() {
		return canvas.getStage();
	}

	/**
	 * Set the map points that can be selected.
	 * */
	public void setSelectable(final Collection<MapPoint> mr) {
		canvas.setSelectable(mr);
	}

	/**
	 * Determine if a map point is selectable;
	 * */
	public boolean isSelectable(final MapPoint p) {
		return canvas.isSelectable(p);
	}

	/**
	 * Send the end turn message.
	 * */
	public void sendEndTurn() {
		if (modalDialog.getCurrentDialog().map(d ->
			d instanceof AbilityConfirmationDialog).orElse(false)
		) {
			modalDialog.closeModalDialog();
		}

		selectedCharacter = Optional.empty();
		isCharacterSelected.setValue(false);
		hud.selectCharacter(Optional.empty());
		battle.requestCommand(new EndTurnCommandRequest(player));
		setMode(new ModeAnimating(this));
	}

	/**
	 * Send the resign message
	 * */
	public void sendResign() {
		if (modalDialog.getCurrentDialog().map(d ->
			d instanceof AbilityConfirmationDialog).orElse(false)
		) {
			modalDialog.closeModalDialog();
		}

		final DialogPane dialog = new DialogPane();
		dialog.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);

		dialog.getStylesheets().add("dialogs.css");
		dialog.setHeaderText(null);
		dialog.setGraphic(null);
		dialog.setContentText("Really resign?");

		modalDialog.showDialog(dialog, r -> {
			if (r == ButtonType.YES) {
				selectedCharacter = Optional.empty();
				isCharacterSelected.setValue(false);
				hud.selectCharacter(Optional.empty());
				battle.requestCommand(new ResignCommandRequest(player));
				setMode(new ModeAnimating(this));
			}
		});
	}

	/**
	 * The selected character uses an item
	 * */
	public void useItem() {
		try {
			battle.cancel();
			canvas.resetMouseHandlers();
			setMode(new ModeTargetItem(this, selectedCharacter.get()));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to use item but no character was selected");
		}
	}

	/**
	 * The selected character uses an ability.
	 * */
	public void useAbility(final Ability ability) {
		try {
			battle.cancel();
			canvas.resetMouseHandlers();
			setMode(new ModeTarget(this, selectedCharacter.get(), ability));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to target ability but no character was selected");
		}
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public void applyAbility() {
		if (mode instanceof ModeTarget)
			setMode(((ModeTarget) mode).applyNow());
	}

	/**
	 * The selected character initiates a push.
	 * */
	public void usePush() {
		try {
			battle.cancel();
			canvas.resetMouseHandlers();
			setMode(new ModePush(this, selectedCharacter.get()));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to push but no character was selected");
		}
	}

	public boolean anyValidMoves() {
		return sprites.characters.values().stream()
			.filter(c -> c.player == player)
			.anyMatch(c -> anyValidMovesFor(c));
	}

	public boolean anyValidMovesFor(final Character c) {
		final boolean canMove =
			mode.getFutureWithRetry(battle.requestInfo(new InfoMoveRange(c)))
				.map(r -> !r.isEmpty()).orElse(false);

		final boolean canAttack = !c.isDead() && !c.isStunned() && c.getAP() >= 1;

		return canMove || canAttack;
	}

	@Override
	public void endBattle(final BattleOutcome outcome) {
		selectCharacter(Optional.empty());
		setMode(new ModeOtherTurn(this));
		hud.doEndMode(outcome);
	}

	public void handleEndBattle(final Optional<BattleOutcome> outcome) {
		canvas.stopAnimating();
		onDone.accept(outcome);
	}

	public void waitForOtherClientToReconnect() {
		setMode(new ModeWaitForReconnect(this, mode));
		hud.doReconnectMode(false);
	}

	public void otherClientReconnects() {
		if (mode instanceof ModeWaitForReconnect) {
			setMode(((ModeWaitForReconnect) mode).getPrevious());
		}
		hud.endReconnectMode();
	}

	@Override
	public void badCommand(final CommandException e) {
		final Alert a = new Alert(Alert.AlertType.ERROR,
			e.getMessage(), ButtonType.CLOSE);
		a.setHeaderText("Game error!");
		a.showAndWait();
		battle.requestCommand(new ResignCommandRequest(player));
	}

	@Override
	public void command(final ExecutedCommand ec) {
		System.err.println(ec.cmd.getJSON());
		commands.queueCommand(ec);
		if (!inAnimation) inAnimation = commands.doNextCommand();
		if (!(mode instanceof ModeAnimating) && !(mode instanceof ModeOtherTurn))
			setMode(new ModeAnimating(this, mode));
		if (!inAnimation && commands.isComplete()) setMode(mode.animationDone());
	}

	@Override
	public void completeEffect(final InstantEffect e, final boolean canCancel) {
		if (instantEffectCompletion.isPresent()) throw new RuntimeException(
			"Invalid UI state.  Attempted to complete an instant effect, but we're already completing a different instant effect");

		instantEffectCompletion = Optional.of(() -> {
			if (e instanceof Teleport) {
				hud.writeMessage("Select teleport destination");
				final Teleport teleport = (Teleport) e;

				try {
					setMode(new ModeTeleport(this, (ModeAnimating) this.mode,
						teleport.affectedCharacters, teleport.range, canCancel));
				} catch (ClassCastException ee) {
					throw new RuntimeException(
						"Invalid UI state.  Attempted to teleport from a non-animating mode.");
				}

			} else if (e instanceof Move) {
				hud.writeMessage("Select move destination");
				final Move move = (Move) e;

				try {
					setMode(new ModeMoveEffect(this, (ModeAnimating) this.mode,
						move.affectedCharacters, move.range, canCancel));
				} catch (ClassCastException ee) {
					throw new RuntimeException(
						"Invalid UI state.  Attempted to do move effect from a non-animating mode.");
				}
				
			} else {
				throw new RuntimeException("Cannot complete instant effect " + e);
			}
		});

		if (!inAnimation) {
			instantEffectCompletion.get().run();
			instantEffectCompletion = Optional.empty();
		}
	}
}

