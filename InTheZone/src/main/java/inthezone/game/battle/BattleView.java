package inthezone.game.battle;

import inthezone.ai.CommandGenerator;
import inthezone.battle.Ability;
import inthezone.battle.BattleOutcome;
import inthezone.battle.Character;
import inthezone.battle.commands.Command;
import inthezone.battle.commands.CommandException;
import inthezone.battle.commands.EndTurnCommandRequest;
import inthezone.battle.commands.InstantEffectCommand;
import inthezone.battle.commands.MoveCommand;
import inthezone.battle.commands.MoveCommandRequest;
import inthezone.battle.commands.PushCommand;
import inthezone.battle.commands.PushCommandRequest;
import inthezone.battle.commands.ResignCommand;
import inthezone.battle.commands.ResignCommandRequest;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.UseAbilityCommand;
import inthezone.battle.commands.UseAbilityCommandRequest;
import inthezone.battle.DamageToTarget;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Player;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.InstantEffect;
import inthezone.battle.instant.PullPush;
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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import static inthezone.game.battle.BattleViewMode.*;

public class BattleView
	extends DialogScreen<BattleOutcome> implements BattleListener
{
	private final static double walkSpeed = 1.2;
	private final static double pushSpeed = 2;

	private final MapView canvas;
	private final Player player;
	private final BattleInProgress battle;

	private final static int HIGHLIGHT_TARGET = 0;
	private final static int HIGHLIGHT_MOVE = 1;
	private final static int HIGHLIGHT_PATH = 2;
	private final static int HIGHLIGHT_ATTACKAREA = 3;
	private final Paint[] highlights = new Paint[] {
		Color.rgb(0xFF, 0xFF, 0x00, 0.2),
		Color.rgb(0x00, 0xFF, 0x00, 0.2),
		Color.rgb(0x00, 0x00, 0xFF, 0.2),
		Color.rgb(0xFF, 0x00, 0x00, 0.2)};

	// The HUD GUI components
	private final HUD hud;

	// Map from character ids to characters
	private Map<Integer, Character> characters = null;

	// Temporary objects that are immobile, such as roadblocks
	private final Map<MapPoint, Sprite> temporaryImmobileObjects = new HashMap<>();

	// the selected character
	private Optional<Character> selectedCharacter = Optional.empty();

	// the current ability.  If mode is TARGET then this must not be null.
	private Optional<Ability> rootTargetingAbility = Optional.empty();
	private Optional<Ability> targetingAbility = Optional.empty();
	private Collection<MapPoint> targets = new ArrayList<>();

	// A queue of points from which to recast a recursive ability
	private MapPoint castFrom = null;
	private Queue<MapPoint> recastFrom = new LinkedList<>();

	// A queue of characters to retarget
	private int teleportRange = 0;
	private Character teleporting = null;
	private Queue<Character> teleportQueue = new LinkedList<>();
	private List<MapPoint> teleportDestinations = new ArrayList<>();

	private BattleViewMode mode = SELECT;

	// status properties for the HUD
	public final BooleanProperty isMyTurn = new SimpleBooleanProperty(true);
	public final BooleanProperty isCharacterSelected = new SimpleBooleanProperty(false);
	public final BooleanProperty multiTargeting = new SimpleBooleanProperty(false);
	public final IntegerProperty numTargets = new SimpleIntegerProperty(0);

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
					if (teleportQueue.size() > 0) setMode(TELEPORT);
					else if (selectedCharacter.isPresent()) setMode(MOVE);
					else setMode(SELECT);
				} else {
					setMode(OTHER_TURN);
				}
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
			} catch (CancellationException e) {
				return Optional.empty();
			} 
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
				setMode(MOVE);
			} else {
				selectedCharacter = Optional.empty();
				isCharacterSelected.setValue(false);
				setMode(SELECT);
			}
			hud.selectCharacter(c.orElse(null));
		}
	}

	private void addTarget(MapPoint p) {
		targets.add(p);
		numTargets.setValue(numTargets.getValue() - 1);
		if (numTargets.getValue() == 0) applyAbility();
	}


	private Consumer<MapPoint> handleSelection() {
		return p -> {
			if (mode == ANIMATING) return;
			if (p == null && mode != TELEPORT) {
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

				case TELEPORT:
					if (canvas.isSelectable(p)) {
						teleportDestinations.add(p);
						teleportQueue.remove();
						setMode(TELEPORT);
					}
					break;

				case PUSH:
					if (canvas.isSelectable(p)) {
						selectedCharacter.ifPresent(c -> battle.requestCommand(
							new PushCommandRequest(c.getPos(), p)));
						setMode(MOVE);
					} else {
						selectCharacter(Optional.empty());
					}
					break;

				case TARGET:
					if (canvas.isSelectable(p)) {
						addTarget(p);
					} else {
						selectCharacter(Optional.empty());
					}
					break;
			}

		};
	}

	private void setMode(BattleViewMode mode) {
		Stage stage = canvas.getStage();
		Character c;

		switch (mode) {
			case OTHER_TURN:
				cancelAbility();
				canvas.getStage().clearAllHighlighting();
				selectCharacter(Optional.empty());
				break;

			case ANIMATING:
				canvas.getStage().clearAllHighlighting();
				break;

			case SELECT:
				cancelAbility();
				canvas.getStage().clearAllHighlighting();
				break;

			case PUSH:
				canvas.getStage().clearAllHighlighting();
				c = selectedCharacter.orElseThrow(() -> new RuntimeException(
					"Attempted to move but no character was selected"));

				MapPoint centre = c.getPos();
				Collection<MapPoint> r = new ArrayList<>();
				r.add(centre.add(new MapPoint( 1, 0)));
				r.add(centre.add(new MapPoint(-1, 0)));
				r.add(centre.add(new MapPoint( 0, 1)));
				r.add(centre.add(new MapPoint( 0, -1)));

				r.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
				canvas.setSelectable(r);

				break;

			case MOVE:
				cancelAbility();
				stage.clearAllHighlighting();
				c = selectedCharacter.orElseThrow(() -> new RuntimeException(
					"Attempted to move but no character was selected"));

				getFutureWithRetry(battle.getMoveRange(c)).ifPresent(mr -> {
					mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_MOVE));
					canvas.setSelectable(mr);
				});
				break;

			case TELEPORT:
				teleporting = teleportQueue.peek();
				if (teleporting == null) {
					battle.completeEffect(teleportDestinations);
					setMode(MOVE);
				} else {
					canvas.getStage().clearAllHighlighting();
					getFutureWithRetry(battle.getTeleportRange(teleporting, teleportRange))
						.ifPresent(mr -> {
							mr.add(teleporting.getPos());
							mr.removeAll(teleportDestinations);
							mr.stream().forEach(p -> stage.setHighlight(p, HIGHLIGHT_TARGET));
							canvas.setSelectable(mr);
						});
				}
				break;

			case TARGET:
				stage.clearAllHighlighting();
				c = selectedCharacter.orElseThrow(() -> new RuntimeException(
					"Attempted to move but no character was selected"));
				if (!targetingAbility.isPresent())
					throw new RuntimeException("Attempted to target null ability");

				if (targetingAbility.get().recursionLevel > 0) {
					this.castFrom = recastFrom.poll();
					if (castFrom == null) {
						setMode(MOVE); return;
					}
				}

				getFutureWithRetry(battle.getTargetingInfo(c, castFrom, targetingAbility.get()))
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
			Stage stage = canvas.getStage();

			switch (mode) {
				case MOVE:
					stage.clearHighlighting(HIGHLIGHT_PATH);

					selectedCharacter.ifPresent(c -> {
						getFutureWithRetry(battle.getPath(c, p)).ifPresent(path ->
							path.stream().forEach(pp ->
								stage.setHighlight(pp, HIGHLIGHT_PATH)));
					});
				break;

				case TARGET:
					stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
					if (!stage.isHighlighted(p)) return;

					selectedCharacter.ifPresent(c -> {
						getFutureWithRetry(battle.getAttackArea(
								c, castFrom, p, targetingAbility.get()))
							.ifPresent(area -> area.stream().forEach(pp ->
								stage.setHighlight(pp, HIGHLIGHT_ATTACKAREA)));
					});
					break;

				case PUSH:
					stage.clearHighlighting(HIGHLIGHT_ATTACKAREA);
					if (canvas.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_ATTACKAREA);
					break;

				case TELEPORT:
					stage.clearHighlighting(HIGHLIGHT_PATH);
					if (canvas.isSelectable(p)) stage.setHighlight(p, HIGHLIGHT_PATH);
					break;
			}
		};
	}

	private Runnable handleMouseOut() {
		return () -> {
			switch (mode) {
				case MOVE:
					canvas.getStage().clearHighlighting(HIGHLIGHT_PATH);
				case TARGET:
					canvas.getStage().clearHighlighting(HIGHLIGHT_ATTACKAREA);
			}
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

	public void sendResign() {
		battle.requestCommand(new ResignCommandRequest(player));
	}

	/**
	 * The selected character uses an ability.
	 * */
	public void useAbility(Ability ability) {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to target ability but no character was selected");
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

	private void setupTargeting() {
		targetingAbility.ifPresent(a -> {
			numTargets.setValue(a.info.range.nTargets);
			multiTargeting.setValue(a.info.range.nTargets > 1);
			setMode(a.recursionLevel > 0 ? ANIMATING : TARGET);
		});
	}

	private void cancelAbility() {
		recastFrom.clear();
		numTargets.setValue(0);
		multiTargeting.setValue(false);
	}

	/**
	 * Apply the selected ability now, even if we haven't selected the maximum
	 * number of targets.
	 * */
	public void applyAbility() {
		if (targets.size() > 0) {
			selectedCharacter.ifPresent(c ->
				battle.requestCommand(new UseAbilityCommandRequest(
					c.getPos(), c.getPos(), targets, targetingAbility.get())));
		}
		targets.clear();

		targetingAbility = targetingAbility.flatMap(a -> a.getSubsequent());
		if (targetingAbility.isPresent()) {
			hud.writeMessage("Subsequent!");
			setupTargeting();
		} else {
			rootTargetingAbility = rootTargetingAbility.flatMap(a -> a.getRecursion());
			targetingAbility = rootTargetingAbility;
			if (targetingAbility.isPresent()) {
				hud.writeMessage("Recursive rebound!");
				setupTargeting();
			} else {
				cancelAbility();
				setMode(MOVE);
			}
		}
	}

	/**
	 * The selected character initiates a push.
	 * */
	public void usePush() {
		if (!selectedCharacter.isPresent()) throw new RuntimeException(
			"Attempted to push but no character was selected");

		setMode(PUSH);
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
	public void endBattle(BattleOutcome outcome) {
		onDone.accept(Optional.of(outcome));
	}

	@Override
	public void badCommand(CommandException e) {
		Alert a = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		a.setHeaderText("Game error!");
		a.showAndWait();
	}

	private static Character getAgent(List<Targetable> ts) {
		return (Character) ts.get(0);
	}

	@Override
	public void command(Command cmd, List<Targetable> affectedCharacters) {
		if (cmd instanceof UseAbilityCommand && !isMyTurn.getValue()) {
			UseAbilityCommand ua = (UseAbilityCommand) cmd;
			hud.writeMessage(
				getAgent(affectedCharacters).name + " uses " + ua.ability + "!");
		}

		if (cmd instanceof MoveCommand) {
			List<MapPoint> path = ((MoveCommand) cmd).path;
			if (path.size() < 2) return;

			scheduleMovement("walk", walkSpeed, path, getAgent(affectedCharacters));

		} else if (cmd instanceof PushCommand) {
			// The first element in the affected characters list for a push command
			// is the agent of the push.  This element must be removed before
			// proceeding to the processing of the push effect.
			instantEffect(((PushCommand) cmd).effect,
				affectedCharacters.subList(1, affectedCharacters.size()));

		} else if (cmd instanceof InstantEffectCommand) {
			instantEffect(((InstantEffectCommand) cmd).getEffect(), affectedCharacters);

		} else if (cmd instanceof ResignCommand) {
			if (((ResignCommand) cmd).player != player) {
				Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
				a.setHeaderText("Opponent resigns");
				a.showAndWait();
			}

		} else if (cmd instanceof UseAbilityCommand && isMyTurn.getValue() &&
			targetingAbility.map(a -> a.recursionLevel > 0).orElse(false)
		) {
			// add all of the targets of this ability to the recast from list
			for (DamageToTarget d: ((UseAbilityCommand) cmd).getTargets()) {
				recastFrom.add(d.target);
			}
			setMode(TARGET);
		}

		updateCharacters(affectedCharacters);
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
		setMode(ANIMATING);
		Stage stage = canvas.getStage();
		MapPoint start = path.get(0);
		MapPoint end = path.get(1);
		MapPoint v = end.subtract(start);

		int id = affected.id;
		Sprite s = stage.getSpritesByTile(start).stream()
			.filter(x -> x.userData.equals(id)).findFirst().get();

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
							.filter(x -> x.userData.equals(c.id)).findFirst().get();
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
			if (t instanceof Character) continue;
			if (!temporaryImmobileObjects.containsKey(t.getPos())) {
				Sprite s = new Sprite(t.getSprite());
				s.pos = t.getPos();
				canvas.getStage().addSprite(s);
				temporaryImmobileObjects.put(t.getPos(), s);
			} else if (t.reap()) {
				Sprite s = temporaryImmobileObjects.get(t.getPos());
				canvas.getStage().removeSprite(s);
				temporaryImmobileObjects.remove(t.getPos());
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
			teleportDestinations.clear();
			setMode(TELEPORT);
		} else {
			throw new RuntimeException("Cannot complete instant effect " + e);
		}
	}
}

