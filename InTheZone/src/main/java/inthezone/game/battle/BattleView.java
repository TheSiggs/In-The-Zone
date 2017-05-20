package inthezone.game.battle;

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
import inthezone.battle.Targetable;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import inthezone.comptroller.Network;
import inthezone.game.DialogScreen;
import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.Stage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import static inthezone.game.battle.Highlighters.HIGHLIGHT_ZONE;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	// battle state
	public final BattleInProgress battle;
	public final Player player;
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
	public final BooleanProperty areAllItemsUsed = new SimpleBooleanProperty(false);
	public final BooleanProperty cannotCancel = new SimpleBooleanProperty(false);

	// Are we expecting an animation completion event from the game engine.
	private boolean inAnimation = false;

	// Instant effect completions must be delayed until the command queue is empty.
	private Optional<Runnable> instantEffectCompletion = Optional.empty();

	public BattleView(
		StartBattleCommand startBattle, Player player,
		CommandGenerator otherPlayer,
		Network network, GameDataFactory gameData
	) throws CorruptDataException {
		super();
		this.setMinSize(0, 0);

		this.player = player;
		this.commands = new CommandProcessor(this);
		this.hud = new HUD(this, gameData.getStandardSprites());

		final DecalRenderer decals = new DecalRenderer(this, gameData.getStandardSprites());

		System.err.println("Playing as " + player);

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, false, Highlighters.highlights);
		canvas.widthProperty().bind(this.widthProperty());
		canvas.heightProperty().bind(this.heightProperty());
		canvas.startAnimating();
		canvas.setFocusTraversable(true);
		canvas.doOnSelection(
			p -> {
				if (p == null && mode.canCancel()) {
					selectCharacter(Optional.empty());
				} else  {
					mode.handleSelection(p);
				}
			});
		canvas.doOnMouseOverSprite(decals::handleMouseOver);
		canvas.doOnMouseOutSprite(decals::handleMouseOut);
		canvas.doOnMouseOver(p -> mode.handleMouseOver(p));
		canvas.doOnMouseOut(() -> mode.handleMouseOut());

		final Collection<Sprite> allSprites = startBattle.makeSprites();
		this.sprites = new SpriteManager(this, allSprites, decals, () -> {
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

		battle = new BattleInProgress(
			startBattle, player, otherPlayer, network, gameData, this);
		(new Thread(battle)).start();

		// init the mode
		canvas.setSelectableSprites(allSprites);
		canvas.setMouseOverSprites(allSprites);
		if (allSprites.size() > 0) {
			canvas.centreOnTile(allSprites.iterator().next().pos);
		}
		setMode(new ModeOtherTurn(this));
		this.getChildren().addAll(canvas, hud);
	}

	private Mode mode;

	public Mode getMode() {return mode;}

	/**
	 * Switch to a different UI mode.
	 * */
	public void setMode(Mode mode) {
		this.mode = mode.setupMode();
		this.cannotCancel.setValue(!mode.canCancel());
		System.err.println("Set mode " + mode + " transforming to " + this.mode);
	}

	/**
	 * Update the UI mode to reflect characters moving around.
	 * */
	public void retargetMode(Map<MapPoint, MapPoint> retargeting) {
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
	public void selectCharacterById(int id) {
		selectCharacter(Optional.ofNullable(sprites.getCharacterById(id)));
	}

	/**
	 * Select or deselect a character.
	 * @param c If empty, then deselect all characters.
	 * */
	public void selectCharacter(Optional<Character> c) {
		outOfTurnSelect(c);
		if (mode.isInteractive()) setMode(new ModeSelect(this));
	}

	/**
	 * Select or deselect a character when it's not your turn.
	 * @param c If empty, the deselect all characters.
	 * */
	public void outOfTurnSelect(Optional<Character> c) {
		battle.cancel();
		selectedCharacter = c;
		isCharacterSelected.setValue(c.isPresent());
		hud.selectCharacter(c);
	}

	/**
	 * Update information about the selected character.
	 * */
	public void updateSelectedCharacter(Character c) {
		selectedCharacter.ifPresent(sc -> {
			if (sc.id == c.id) {
				selectedCharacter = Optional.of(c);
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
	public void setSelectable(Collection<MapPoint> mr) {
		canvas.setSelectable(mr);
	}

	/**
	 * Determine if a map point is selectable;
	 * */
	public boolean isSelectable(MapPoint p) {
		return canvas.isSelectable(p);
	}

	/**
	 * Send the end turn message.
	 * */
	public void sendEndTurn() {
		battle.cancel();
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
		selectedCharacter = Optional.empty();
		isCharacterSelected.setValue(false);
		hud.selectCharacter(Optional.empty());
		battle.requestCommand(new ResignCommandRequest(player));
		setMode(new ModeAnimating(this));
	}

	/**
	 * The selected character uses an item
	 * */
	public void useItem() {
		try {
			battle.cancel();
			setMode(new ModeTargetItem(this, selectedCharacter.get()));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to use item but no character was selected");
		}
	}

	/**
	 * The selected character uses an ability.
	 * */
	public void useAbility(Ability ability) {
		try {
			battle.cancel();
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
			setMode(new ModePush(this, selectedCharacter.get()));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to push but no character was selected");
		}
	}

	@Override
	public void endBattle(BattleOutcome outcome) {
		selectCharacter(Optional.empty());
		setMode(new ModeOtherTurn(this));
		hud.doEndMode(outcome);
	}

	public void handleEndBattle(Optional<BattleOutcome> outcome) {
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
	public void badCommand(CommandException e) {
		Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		a.setHeaderText("Game error!");
		a.showAndWait();
		battle.requestCommand(new ResignCommandRequest(player));
	}

	@Override
	public void command(ExecutedCommand ec) {
		System.err.println(ec.cmd.getJSON());
		commands.queueCommand(ec);
		if (!inAnimation) inAnimation = commands.doNextCommand();
		if (!inAnimation && commands.isComplete()) setMode(mode.animationDone());
	}

	@Override
	public void completeEffect(InstantEffect e, boolean canCancel) {
		if (instantEffectCompletion.isPresent()) throw new RuntimeException(
			"Invalid UI state.  Attempted to complete an instant effect, but we're already completing a different instant effect");

		instantEffectCompletion = Optional.of(() -> {
			if (e instanceof Teleport) {
				hud.writeMessage("Select teleport destination");
				Teleport teleport = (Teleport) e;

				try {
					setMode(new ModeTeleport(this, (ModeAnimating) this.mode,
						teleport.affectedCharacters, teleport.range, canCancel));
				} catch (ClassCastException ee) {
					throw new RuntimeException(
						"Invalid UI state.  Attempted to teleport from a non-animating mode.");
				}

			} else if (e instanceof Move) {
				hud.writeMessage("Select move destination");
				Move move = (Move) e;

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

