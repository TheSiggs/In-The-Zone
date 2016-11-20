package inthezone.game.battle;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.commands.ExecutedCommand;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.commands.PushCommand;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.ResignCommandRequest;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.UseAbilityCommand;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.PullPush;
import inthezone.battle.instant.Teleport;
import inthezone.battle.Targetable;
import inthezone.battle.Zone;
import inthezone.comptroller.BattleInProgress;
import inthezone.comptroller.BattleListener;
import inthezone.comptroller.Network;
import inthezone.game.DialogScreen;
import isogame.engine.AnimationChain;
import isogame.engine.Highlighter;
import isogame.engine.MapPoint;
import isogame.engine.MapView;
import isogame.engine.Sprite;
import isogame.engine.SpriteDecalRenderer;
import isogame.engine.Stage;
import isogame.GlobalConstants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	private final static double walkSpeed = 1.2;
	private final static double pushSpeed = 2;

	private final MapView canvas;
	private final Player player;
	private final BattleInProgress battle;

	private final static int HIGHLIGHT_ZONE       = 0;
	private final static int HIGHLIGHT_TARGET     = 1;
	private final static int HIGHLIGHT_MOVE       = 2;
	private final static int HIGHLIGHT_PATH       = 3;
	private final static int HIGHLIGHT_ATTACKAREA = 4;
	private final Highlighter[] highlights = new Highlighter[] {
		new Highlighter(Color.rgb(0xFF, 0x88, 0x00, 0.2)),
		new Highlighter(Color.rgb(0xFF, 0xFF, 0x00, 0.2)),
		new Highlighter(Color.rgb(0x00, 0xFF, 0x00, 0.2)),
		new Highlighter(Color.rgb(0x00, 0x00, 0xFF, 0.2)),
		new Highlighter(Color.rgb(0xFF, 0x00, 0x00, 0.2))};

	// The HUD GUI components
	private final HUD hud;

	// Map from character ids to characters
	private Map<Integer, Character> characters = null;

	// Temporary objects that are immobile, such as roadblocks
	private final Map<MapPoint, Sprite> temporaryImmobileObjects = new HashMap<>();

	// Keep track of which squares contain zones
	private final Collection<MapPoint> zones = new HashSet<>();

	// the selected character
	private Optional<Character> selectedCharacter = Optional.empty();

	// the current ability.  If mode is TARGET then this must not be null.
	private Optional<Ability> rootTargetingAbility = Optional.empty();
	private Optional<Ability> targetingAbility = Optional.empty();
	private Collection<MapPoint> targets = new ArrayList<>();
	private boolean targetingItem = false;

	// A queue of points from which to recast a recursive ability
	private MapPoint castFrom = null;
	private Queue<MapPoint> recastFrom = new LinkedList<>();

	// A queue of characters to retarget
	private int teleportRange = 0;
	private Character teleporting = null;
	private Queue<Character> teleportQueue = new LinkedList<>();
	private List<MapPoint> teleportDestinations = new ArrayList<>();

	private Mode mode = new ModeSelect(this);

	// status properties for the HUD
	public final BooleanProperty isMyTurn = new SimpleBooleanProperty(true);
	public final BooleanProperty isCharacterSelected = new SimpleBooleanProperty(false);
	public final BooleanProperty multiTargeting = new SimpleBooleanProperty(false);
	public final IntegerProperty numTargets = new SimpleIntegerProperty(0);
	public final BooleanProperty areAllItemsUsed = new SimpleBooleanProperty(false);

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
		this.hud = new HUD(this, gameData.getStandardSprites());

		this.canvas = new MapView(this,
			gameData.getStage(startBattle.stage), true, false, highlights);
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
				if (isMyTurn.getValue()) {
					setDefaultMode();
					doNextCommand();
				} else {
					setMode(new ModeOtherTurn());
				}
			});
		}

		canvas.setSelectableSprites(sprites);
		
		battle = new BattleInProgress(
			startBattle, player, otherPlayer, network, gameData, this);
		(new Thread(battle)).start();

		this.getChildren().addAll(canvas, hud);
	}

	public void setDefaultMode() {
		if (teleportQueue.size() > 0) {
			setMode(new ModeTeleport(this, teleportQueue, teleportRange));
		} else if (selectedCharacter.isPresent()) {
			setMode(new ModeMove(this, selectedCharacter.get()));
		} else {
			setMode(new ModeSelect(this));
		}
	}

	public void selectCharacterById(int id) {
		selectCharacter(Optional.ofNullable(characters.get(id)));
	}

	public void selectCharacter(Optional<Character> c) {
		if (mode != OTHER_TURN && mode != ANIMATING) {
			if (c.isPresent() && !c.get().isDead()) {
				selectedCharacter = c;
				isCharacterSelected.setValue(true);
				setMode(new ModeMove(this, c));
			} else {
				selectedCharacter = Optional.empty();
				isCharacterSelected.setValue(false);
				setMode(new ModeSelect(this));
			}
			hud.selectCharacter(c.orElse(null));
		}
	}

	private Consumer<MapPoint> handleSelection() {
		return p -> {
			if (p == null && mode != TELEPORT) {
				selectCharacter(Optional.empty());
			} else  {
				mode.handleSelection(p);
			}
		};
	}

	private void setMode(Mode mode) {
		this.mode = mode;
		for (MapPoint p : zones) stage.setHighlight(p, HIGHLIGHT_ZONE);
	}

	private Consumer<MapPoint> handleMouseOver() {
		return p -> mode.handleMouseOver();
	}

	private Runnable handleMouseOut() {
		return () -> mode.handleMouseOut();
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
	 * Send the resign message
	 * */
	public void sendResign() {
		battle.requestCommand(new ResignCommandRequest(player));
	}

	/**
	 * The selected character uses an item
	 * */
	public void useItem() {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to use item but no character was selected");

		targetingItem = true;
		this.castFrom = selectedCharacter.map(c -> c.getPos()).orElse(null);
		setupTargeting();
	}

	/**
	 * The selected character uses an ability.
	 * */
	public void useAbility(Ability ability) {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to target ability but no character was selected");

		targetingItem = false;
		rootTargetingAbility = Optional.of(ability);
		targetingAbility = Optional.of(ability);
		this.castFrom = selectedCharacter.map(c -> c.getPos()).orElse(null);
		setupTargeting();

		// range 0 abilities get applied immediately
		if (ability.info.range.range == 0) {
			targets.add(castFrom);
			applyAbility();
		}
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public void applyAbility() {
		if (mode instanceof ModeTarget) {
			((ModeTarget) mode).applyAbility();
		}
	}

	/**
	 * The selected character initiates a push.
	 * */
	public void usePush() {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to push but no character was selected");

		setMode(new ModePush(this, selectedCharacter));
	}

	@Override
	public void startTurn(List<Targetable> characters) {
		isMyTurn.setValue(true);
		updateCharacters(characters);
		setMode(new ModeSelect(););
	}

	@Override
	public void endTurn(List<Targetable> characters) {
		isMyTurn.setValue(false);
		updateCharacters(characters);
		setMode(new ModeOtherTurn());
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

	private Queue<ExecutedCommand> commandQueue = new LinkedList<>();

	@Override
	public void command(ExecutedCommand ec) {
		commandQueue.add(ec);
		if (mode != ANIMATING) doNextCommand();
	}

	private void doNextCommand() {
		ExecutedCommand ec = commandQueue.poll();
		if (ec == null) return;

		if (ec.cmd instanceof UseAbilityCommand && !isMyTurn.getValue()) {
			UseAbilityCommand ua = (UseAbilityCommand) ec.cmd;
			Targetable agent = ec.affected.get(0);
			if (agent instanceof Character) {
				hud.writeMessage(((Character) agent).name + " uses " + ua.ability + "!");
			} else {
				hud.writeMessage("It's a trap!");
			}
		}

		if (ec.cmd instanceof MoveCommand) {
			List<MapPoint> path = ((MoveCommand) ec.cmd).path;
			if (path.size() < 2) return;

			Character agent = (Character) ec.affected.get(0);
			scheduleMovement("walk", walkSpeed, path, agent);

		} else if (ec.cmd instanceof PushCommand) {
			// The first element in the affected characters list for a push command
			// is the agent of the push.  This element must be removed before
			// proceeding to the processing of the push effect.
			instantEffect(((PushCommand) ec.cmd).effect,
				ec.affected.subList(1, ec.affected.size()));

		} else if (ec.cmd instanceof InstantEffectCommand) {
			instantEffect(((InstantEffectCommand) ec.cmd).getEffect(), ec.affected);

		} else if (ec.cmd instanceof ResignCommand) {
			if (((ResignCommand) ec.cmd).player != player) {
				Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
				a.setHeaderText("Opponent resigns");
				a.showAndWait();
			}

		} else if (ec.cmd instanceof UseAbilityCommand && isMyTurn.getValue() &&
			targetingAbility.map(a -> a.recursionLevel > 0).orElse(false)
		) {
			// add all of the targets of this ability to the recast from list
			for (DamageToTarget d: ((UseAbilityCommand) ec.cmd).getTargets()) {
				recastFrom.add(d.target);
			}
			// TODO: revisit this
			//setMode(TARGET);
		}

		updateCharacters(ec.affected);
		if (!(mode instanceof ModeAnimating)) doNextCommand();
	}

	private void instantEffect(
		InstantEffect effect, List<Targetable> affectedCharacters
	) {
		Stage stage = canvas.getStage();

		if (effect instanceof PullPush) {
			PullPush pullpush = (PullPush) effect;
			if (pullpush.paths.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid pull or push, this cannot happen");
			}

			for (int i = 0; i < pullpush.paths.size(); i++) {
				scheduleMovement("idle", pushSpeed,
					pullpush.paths.get(i), (Character) affectedCharacters.get(i));
			}

		} else if (effect instanceof Teleport) {
			Teleport teleport = (Teleport) effect;
			List<MapPoint> destinations = teleport.getDestinations();
			if (destinations == null || destinations.size() != affectedCharacters.size()) {
				throw new RuntimeException("Invalid teleport, this cannot happen");
			}

			for (int i = 0; i < destinations.size(); i++) {
				int id = ((Character) affectedCharacters.get(i)).id;
				MapPoint tile = characters.get(id).getPos();
				Sprite s = stage.getSpritesByTile(tile).stream()
					.filter(x -> x.userData.equals(id)).findFirst().get();
				stage.queueTeleportSprite(s, destinations.get(i));
			}
		}

		updateCharacters(affectedCharacters);
	}

	private void scheduleMovement(
		String animation, double speed, List<MapPoint> path, Character affected
	) {
		setMode(new ModeAnimating());
		Stage stage = canvas.getStage();
		MapPoint start = path.get(0);
		MapPoint end = path.get(1);
		MapPoint v = end.subtract(start);

		int id = affected.id;
		Sprite s = stage.getSpritesByTile(start).stream()
			.filter(x -> x.userData != null && x.userData.equals(id)).findFirst().get();

		for (MapPoint p : path.subList(2, path.size())) {
			if (!end.add(v).equals(p)) {
				stage.queueMoveSprite(s, start, end, animation, speed);
				start = end;
				v = p.subtract(start);
			}
			end = p;
		}

		stage.queueMoveSprite(s, start, end, animation, speed);
	}

	private void updateCharacters(List<? extends Targetable> characters) {
		if (this.characters == null) {
			this.characters = new HashMap<>();
			for (Targetable c : characters) {
				if (c instanceof Character)
					this.characters.put(((Character) c).id, (Character) c);
			}
			hud.init(this.characters.values().stream()
				.filter(c -> c.player == player).collect(Collectors.toList()));
		} else {
			for (Targetable t : characters) {
				if (t instanceof Character) {
					Character c = (Character) t;
					Character old = this.characters.get(c.id);
					selectedCharacter.ifPresent(sc -> {
						if (sc.id == c.id) selectedCharacter = Optional.of(c);
					});

					if (old != null) {
						if (c.player == player) hud.updateAbilities(c, c.hasMana());
						CharacterInfoBox box = hud.characters.get(c.id);
						if (box != null) {
							box.updateAP(c.getAP(), c.getStats().ap);
							box.updateMP(c.getMP(), c.getStats().mp);
							box.updateHP(c.getHP(), c.getMaxHP());
							box.updateStatus(c.getStatusBuff(), c.getStatusDebuff());
						}

					}

					if (c.isDead()) {
						Sprite s = canvas.getStage().getSpritesByTile(c.getPos()).stream()
							.filter(x -> x.userData != null && x.userData.equals(c.id)).findFirst().get();
						s.setAnimation("dead");
					}

					this.characters.put(c.id, c);
				}
			}
		}

		handleTemporaryImmobileObjects(characters);
	}

	private void handleTemporaryImmobileObjects(Collection<? extends Targetable> tios) {
		for (Targetable t : tios) {
			if (t instanceof Character) {
				continue;

			} else if (t instanceof Zone) {
				if (t.reap()) {
					zones.removeAll(((Zone) t).range);
					// TODO: reset highlighting

				} else {
					zones.addAll(((Zone) t).range);
					// TODO: reset highlighting
				}
				
			} else if (t.reap()) {
				Sprite s = temporaryImmobileObjects.remove(t.getPos());
				if (s != null) canvas.getStage().removeSprite(s);

			} else if (!temporaryImmobileObjects.containsKey(t.getPos())) {
				Sprite s = new Sprite(t.getSprite());
				s.pos = t.getPos();
				canvas.getStage().addSprite(s);
				temporaryImmobileObjects.put(t.getPos(), s);
			}
		}
	}

	@Override
	public void completeEffect(InstantEffect e) {
		if (e instanceof Teleport) {
			hud.writeMessage("Select teleport destination");
			Teleport teleport = (Teleport) e;
			teleportRange = teleport.range;
			teleportQueue.clear();
			teleportQueue.addAll(teleport.affectedCharacters);

			setMode(new ModeTeleport(this, teleportQueue, teleportRange));
		} else {
			throw new RuntimeException("Cannot complete instant effect " + e);
		}
	}
}

