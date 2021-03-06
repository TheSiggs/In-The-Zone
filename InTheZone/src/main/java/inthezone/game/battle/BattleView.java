package inthezone.game.battle;

import isogame.engine.CameraAngle;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import isogame.engine.Tile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
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
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.CharacterFrozen;
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
import inthezone.game.ClientConfig;
import inthezone.game.DialogScreen;
import inthezone.game.InTheZoneKeyBinding;
import inthezone.protocol.ProtocolException;

/**
 * The main battle view.  This is the root handler for all messages concerning
 * an ongoing battle.
 * */
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
	private Optional<CharacterFrozen> selectedCharacter = Optional.empty();

	// UI components
	public final MapView canvas;
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

	public final ModalDialog modalDialog = new ModalDialog();

	/**
	 * Start a standard battle.
	 * @param startBattle the StartBattleCommand
	 * @param player this player
	 * @param otherPlayer a method to get the commands of the other player
	 * @param network the network
	 * @param gameData the game data
	 * @param config client configuration
	 * */
	public BattleView(
		final StartBattleCommand startBattle,
		final Player player,
		final CommandGenerator otherPlayer,
		final Optional<Network> network,
		final GameDataFactory gameData,
		final ClientConfig config
	) throws CorruptDataException {
		this(startBattle, player,
			Optional.empty(), otherPlayer, network, gameData, config,
			view -> new StandardHUD(view, gameData.getStandardSprites(), config));
	}

	/**
	 * Start a saved battle in replay mode.
	 * @param pb the PlayerbackGenerator
	 * @param in the saved game
	 * @param gameData the game data
	 * @param config client configuration
	 * */
	public BattleView(
		final PlaybackGenerator pb,
		final InputStream in,
		final GameDataFactory gameData,
		final ClientConfig config
	) throws IOException, ProtocolException, CorruptDataException {
		this(
			pb.start(new BufferedReader(new InputStreamReader(in, "UTF-8")), gameData),
			Player.PLAYER_OBSERVER,
			Optional.of(pb), pb,
			Optional.empty(), gameData, config,
			view -> new ReplayHUD(view, gameData.getStandardSprites(), pb));
	}

	/**
	 * Main constructor.
	 * @param startBattle the StartBattleCommand
	 * @param player this Player
	 * @param thisPlayerGenerator CommandGenerator for this player
	 * @param otherPlayer CommandGenerator for the other player
	 * @param network the network
	 * @param gameData the game data
	 * @param config client configuration
	 * @param hud a method to construct the HUD
	 * */
	public BattleView(
		final StartBattleCommand startBattle,
		final Player player,
		final Optional<CommandGenerator> thisPlayerGenerator,
		final CommandGenerator otherPlayer,
		final Optional<Network> network,
		final GameDataFactory gameData,
		final ClientConfig config,
		final Function<BattleView, HUD> hud
	) throws CorruptDataException {
		this.setMinSize(0, 0);

		this.player = player;
		this.playerAName = startBattle.p1Name;
		this.playerBName = startBattle.p2Name;

		this.commands = new CommandProcessor(this);

		// must construct the canvas first, because it has to be a child of the HUD
		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, false,
			Highlighters.highlights);

		this.hud = hud.apply(this);
		modalDialog.setOnShow(this.hud::modalStart);
		modalDialog.setOnClose(this.hud::modalEnd);

		System.err.println("Playing as " + player);

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
		canvas.doOnMouseOver(p -> mode.handleMouseOver(p));
		canvas.doOnMouseOut(() -> mode.handleMouseOut());

		canvas.keyBindings.loadBindings(config.getKeyBindingTable());

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
			} else if (action == InTheZoneKeyBinding.altpath) {
				if (mode != null) mode.nextPath();
			} else {
				this.hud.handleKey(action);
			}
		});

		final Collection<Sprite> allSprites = startBattle.makeSprites();
		this.sprites = new SpriteManager(
			this, allSprites, gameData.getStandardSprites(),
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

		// add basic tooltips
		final Iterator<Tile> tiles =
			getStage().terrain.iterateTiles(CameraAngle.UL);
		while (tiles.hasNext()) {
			final Tile tile = tiles.next();
			if (tile.isManaZone) {
				Tooltip.install(tile.subGraph, sprites.getManaZoneTooltip());
			}
		}

		setMode(new ModeOtherTurn(this));
		this.getChildren().addAll(this.hud, modalDialog);
	}

	private Mode mode;

	/**
	 * Get the current UI mode.
	 * */
	public Mode getMode() {return mode;}

	/**
	 * Switch to a different UI mode.
	 * @param mode the new mode
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
	public Optional<CharacterFrozen> getSelectedCharacter() {
		return selectedCharacter;
	}

	/**
	 * Select a particular character.
	 * @param id the id of the character to select
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
	public void selectCharacter(final Optional<CharacterFrozen> c) {
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
	public void outOfTurnSelect(final Optional<CharacterFrozen> c) {
		battle.cancel();
		canvas.resetMouseHandlers();
		selectedCharacter = c;
		isCharacterSelected.setValue(c.isPresent());
		hud.selectCharacter(c);
		sprites.updateSelectionStatus();
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
	 * @param c the character to update
	 * */
	public void updateSelectedCharacter(final CharacterFrozen c) {
		selectedCharacter.ifPresent(sc -> {
			if (sc.getId() == c.getId()) {
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
	 * @param mr the selectable points
	 * */
	public void setSelectable(final Collection<MapPoint> mr) {
		canvas.setSelectable(mr);
	}

	/**
	 * Determine if a map point is selectable;
	 * @param p the point to check
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
		sprites.updateSelectionStatus();
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
				sprites.updateSelectionStatus();
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
	 * @param ability the ability to use
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

	/**
	 * Determine if there are any valid moves (if not then the turn can be ended
	 * automatically).
	 * */
	public boolean anyValidMoves() {
		return sprites.characters.values().stream()
			.filter(c -> c.getPlayer() == player)
			.anyMatch(c -> anyValidMovesFor(c));
	}

	/**
	 * Determine if there are any valid moves for a character.
	 * @param c the character to check
	 * */
	public boolean anyValidMovesFor(final CharacterFrozen c) {
		final boolean canMove =
			mode.getFutureWithRetry(battle.requestInfo(new InfoMoveRange(c)))
				.map(r -> !r.isEmpty()).orElse(false);

		final boolean canAttack = !c.isDead() && !c.isStunned() && c.getAP() >= 1;

		return canMove || canAttack;
	}

	/**
	 * Notify that the battle has ended.
	 * @param outcome the outcome of the battle
	 * */
	@Override public void endBattle(final BattleOutcome outcome) {
		selectCharacter(Optional.empty());
		setMode(new ModeOtherTurn(this));
		hud.doEndMode(outcome);
	}

	/**
	 * Handle an end of battle situation.
	 * @param outcome the outcome of the battle
	 * */
	public void handleEndBattle(final Optional<BattleOutcome> outcome) {
		canvas.stopAnimating();
		onDone.accept(outcome);
	}

	/**
	 * Wait for the other client to reconnect.
	 * */
	public void waitForOtherClientToReconnect() {
		setMode(new ModeWaitForReconnect(this, mode));
		hud.doReconnectMode(false);
	}

	/**
	 * Notification that the other client has reconnected.
	 * */
	public void otherClientReconnects() {
		if (mode instanceof ModeWaitForReconnect) {
			setMode(((ModeWaitForReconnect) mode).getPrevious());
		}
		hud.endReconnectMode();
	}

	private boolean handlingError = false;

	/**
	 * Notification that one of the clients attempted to execute a bad command.
	 * @param e the error
	 * */
	@Override public void badCommand(final CommandException e) {
		System.err.println("Game error: ");
		e.printStackTrace();

		if (handlingError) {
			System.err.println("Double fault!");
			System.exit(100);
		}
		handlingError = true;

		final Alert a = new Alert(Alert.AlertType.ERROR,
			e.getMessage(), ButtonType.CLOSE);
		a.setHeaderText("Game error!");
		a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		a.showAndWait();
		battle.requestCommand(new ResignCommandRequest(player));
	}

	/**
	 * Handle a command.
	 * @param ec the command to handle
	 * */
	@Override public void command(final ExecutedCommand ec) {
		System.err.println(ec.cmd.getJSON());
		commands.queueCommand(ec);
		if (!inAnimation) inAnimation = commands.doNextCommand();
		if (!(mode instanceof ModeAnimating) && !(mode instanceof ModeOtherTurn))
			setMode(new ModeAnimating(this, mode));
		if (!inAnimation && commands.isComplete()) setMode(mode.animationDone());

		handlingError = false;
	}

	/**
	 * Get more information from the player concerning an instant effect.
	 * @param e the effect to complete
	 * @param canCancel true if this effect can still be cancelled, otherwise
	 * false.
	 * */
	@Override public void completeEffect(
		final InstantEffect e, final boolean canCancel
	) {
		if (instantEffectCompletion.isPresent()) throw new RuntimeException(
			"Invalid UI state.  Attempted to complete an instant effect, but we're already completing a different instant effect");

		instantEffectCompletion = Optional.of(() -> {
			if (e instanceof Teleport) {
				hud.writeMessage("Select teleport destination");
				final Teleport teleport = (Teleport) e;

				try {
					setMode(new ModeTeleport(this, (ModeAnimating) this.mode,
						teleport.getAffectedCharacters(), teleport.range, canCancel));
				} catch (final ClassCastException ee) {
					throw new RuntimeException(
						"Invalid UI state.  Attempted to teleport from a non-animating mode.");
				}

			} else if (e instanceof Move) {
				hud.writeMessage("Select move destination");
				final Move move = (Move) e;

				try {
					setMode(new ModeMoveEffect(this, (ModeAnimating) this.mode,
						move.getAffectedCharacters(), move.range, canCancel));
				} catch (final ClassCastException ee) {
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

