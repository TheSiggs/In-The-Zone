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
import inthezone.battle.instant.Teleport;
import inthezone.battle.Targetable;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import inthezone.comptroller.Network;
import inthezone.game.DialogScreen;
import isogame.engine.AnimationChain;
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

	private boolean inAnimation = false;

	public BattleView(
		StartBattleCommand startBattle, Player player,
		CommandGenerator otherPlayer,
		Network network, GameDataFactory gameData
	) {
		super();

		this.player = player;
		this.commands = new CommandProcessor(this);
		this.hud = new HUD(this, gameData.getStandardSprites());

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
		canvas.doOnMouseOver(p -> mode.handleMouseOver(p));
		canvas.doOnMouseOut(() -> mode.handleMouseOut());

		final DecalRenderer decals = new DecalRenderer(this);
		final Collection<Sprite> sprites = startBattle.makeSprites();
		for (Sprite s : sprites) {
			Stage stage = getStage();
			stage.addSprite(s);
			s.setDecalRenderer(decals);

			AnimationChain chain = new AnimationChain(s);
			stage.registerAnimationChain(chain);
			chain.doOnFinished(() -> {
				s.setAnimation("idle");

				inAnimation = false;
				while (!inAnimation && !commands.isEmpty()) {
					inAnimation = commands.doNextCommand();
				}
				if (!inAnimation) setMode(mode.animationDone());
			});
		}

		battle = new BattleInProgress(
			startBattle, player, otherPlayer, network, gameData, this);
		(new Thread(battle)).start();

		// init the mode
		canvas.setSelectableSprites(sprites);
		setMode(new ModeSelect(this));
		this.getChildren().addAll(canvas, hud);
	}

	private Mode mode;

	public Mode getMode() {return mode;}

	/**
	 * Switch to a different UI mode.
	 * */
	public void setMode(Mode mode) {
		this.mode = mode.setupMode();
		System.err.println("Setting mode " + mode + ", transformed to " + this.mode);
		Stage stage = getStage();
		for (MapPoint p : commands.zones) stage.setHighlight(p, HIGHLIGHT_ZONE);
	}

	/**
	 * Restore the highlighting.
	 * */
	public void resetHighlighting() {
		setMode(mode);
	}

	/**
	 * Get the currently selected character.
	 * */
	public Optional<Character> getSelectedCharacter() {return selectedCharacter;}

	/**
	 * Select a particular character.
	 * */
	public void selectCharacterById(int id) {
		selectCharacter(Optional.ofNullable(commands.characters.get(id)));
	}

	/**
	 * Select or deselect a character.
	 * @param c If empty, then deselect all characters.
	 * */
	public void selectCharacter(Optional<Character> c) {
		if (mode.isInteractive()) {
			if (c.isPresent() && !c.get().isDead()) {
				selectedCharacter = c;
				isCharacterSelected.setValue(true);
				setMode(new ModeMove(this, c.get()));
			} else {
				selectedCharacter = Optional.empty();
				isCharacterSelected.setValue(false);
				setMode(new ModeSelect(this));
			}
			hud.selectCharacter(c.orElse(null));
		}
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
		battle.requestCommand(new EndTurnCommandRequest());
		setMode(new ModeAnimating(this));
	}

	/**
	 * Send the resign message
	 * */
	public void sendResign() {
		battle.requestCommand(new ResignCommandRequest(player));
		setMode(new ModeAnimating(this));
	}

	/**
	 * The selected character uses an item
	 * */
	public void useItem() {
		try {
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
			setMode(((ModeTarget) mode).applyAbility());
	}

	/**
	 * The selected character initiates a push.
	 * */
	public void usePush() {
		try {
			setMode(new ModePush(this, selectedCharacter.get()));
		} catch (NoSuchElementException e) {
			throw new RuntimeException(
				"Attempted to push but no character was selected");
		}
	}

	@Override
	public void startTurn(List<Targetable> characters) {
		isMyTurn.setValue(true);
		commands.updateCharacters(characters);
		setMode(new ModeSelect(this));
	}

	@Override
	public void endTurn(List<Targetable> characters) {
		isMyTurn.setValue(false);
		commands.updateCharacters(characters);
	}

	@Override
	public void endBattle(BattleOutcome outcome) {
		onDone.accept(Optional.of(outcome));
	}

	@Override
	public void badCommand(CommandException e) {
		Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		a.setHeaderText("Game error!");
		a.showAndWait();
	}

	@Override
	public void command(ExecutedCommand ec) {
		commands.queueCommand(ec);
		if (!inAnimation) inAnimation = commands.doNextCommand();
		if (!inAnimation) setMode(mode.animationDone());
	}

	@Override
	public void completeEffect(InstantEffect e) {
		if (e instanceof Teleport) {
			hud.writeMessage("Select teleport destination");
			Teleport teleport = (Teleport) e;
			setMode(new ModeTeleport(this, mode,
				teleport.affectedCharacters, teleport.range));

		} else {
			throw new RuntimeException("Cannot complete instant effect " + e);
		}
	}
}

