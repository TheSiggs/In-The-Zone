package inthezone.game.lobby;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import inthezone.ai.SimpleAI;
import inthezone.battle.commands.StartBattleCommand;
import inthezone.battle.commands.StartBattleCommandRequest;
import inthezone.battle.data.CharacterProfile;
import inthezone.battle.data.GameDataFactory;
import inthezone.battle.data.Loadout;
import inthezone.battle.data.Player;
import inthezone.game.ClientConfig;
import inthezone.game.ContentPane;
import inthezone.game.battle.BattleView;
import inthezone.game.battle.ModalDialog;
import inthezone.game.guiComponents.KeyboardOptions;
import inthezone.game.loadoutEditor.LoadoutOverview;

public class LobbyView extends StackPane {
	private final ContentPane parent;
	private final GameDataFactory gameData;
	private final ClientConfig config;

	private final BorderPane gui = new BorderPane();
	private final HBox mainMenuBack = new HBox();
	private final MenuBar mainMenu = new MenuBar();
	private final Menu homeMenu = new Menu(null);
	private final Menu playMenu = new Menu(null);
	private final Menu loadoutsMenu = new Menu(null);
	private final Menu optionsMenu = new Menu(null);
	private final Menu logoutMenu = new Menu(null);

	private final MenuItem practiceGame = new MenuItem("Practice");
	private final MenuItem queueGame = new MenuItem("Queue");
	private final MenuItem keybindings = new MenuItem("Keyboard");

	private	final PlayersList players;
	private final NewsPanel newsPanel = new NewsPanel();
	private final VetoPanel vetoPanel;
	private final DisconnectedPanel disconnected = new DisconnectedPanel();
	private final StatusPanel status;

	private final ModalDialog modalDialog = new ModalDialog();

	public LobbyView(
		final ContentPane parent,
		final GameDataFactory gameData,
		final ClientConfig config
	) {
		this.parent = parent;
		this.gameData = gameData;
		this.config = config;

		playMenu.getItems().addAll(practiceGame, queueGame);
		optionsMenu.getItems().addAll(keybindings);
		mainMenu.getMenus().addAll(
			homeMenu, playMenu, loadoutsMenu, optionsMenu, logoutMenu);

		players = new PlayersList(this::issueChallenge);
		status = new StatusPanel(this::cancelChallenge);

		final Node homeLabel = new Label("Home");
		final Node loadoutsLabel = new Label("Loadouts");
		final Node logoutLabel = new Label("Logout");

		homeMenu.setId("menu-button-first");
		homeMenu.setGraphic(homeLabel);
		playMenu.setGraphic(new Label("Play"));
		loadoutsMenu.setGraphic(loadoutsLabel);
		optionsMenu.setGraphic(new Label("Options"));
		logoutMenu.setGraphic(logoutLabel);

		homeLabel.setOnMouseClicked(event -> homeScreen());

		practiceGame.setOnAction(event -> startPracticeGame());

		queueGame.setOnAction(event -> enterQueue());

		loadoutsLabel.setOnMouseClicked(event -> {
			parent.showScreen(new LoadoutOverview(parent), v -> {});
		});

		this.vetoPanel = new VetoPanel(
			gameData, this::homeScreen, this::actuallyEnterQueue);

		keybindings.setOnAction(event -> {
			final KeyboardOptions dialog = new KeyboardOptions(config);
			modalDialog.showDialog(dialog, r -> {
				if (r == KeyboardOptions.doneButton) {
					config.getKeyBindingTable().loadBindings(dialog.resultTable);
					config.writeConfig();
				}
			});
		});

		logoutLabel.setOnMouseClicked(event -> {
			parent.doLogout();
		});

		this.getStylesheets().addAll("/GUI.css", "/lobby.css");
		this.getStyleClass().add("gui-pane");

		mainMenu.setUseSystemMenuBar(false);

		final Separator leftSpace = new Separator(Orientation.HORIZONTAL);
		final Separator rightSpace = new Separator(Orientation.HORIZONTAL);
		HBox.setHgrow(leftSpace, Priority.ALWAYS);
		HBox.setHgrow(rightSpace, Priority.ALWAYS);
		leftSpace.setMaxWidth(Double.MAX_VALUE);
		rightSpace.setMaxWidth(Double.MAX_VALUE);
		mainMenuBack.getChildren().addAll(leftSpace, mainMenu, rightSpace);

		mainMenuBack.getStyleClass().add("main-menu");

		gui.setTop(mainMenuBack);
		gui.setLeft(players);
		gui.setCenter(newsPanel);

		modalDialog.setOnShow(() -> {
			modalDialog.requestFocus();
			gui.setMouseTransparent(true);
		});
		modalDialog.setOnClose(() -> gui.setMouseTransparent(false));

		this.getChildren().addAll(gui, modalDialog);
	}

	private String thisPlayer = null;

	public void joinLobby(
		final String thisPlayer, final Collection<String> players
	) {
		this.thisPlayer = thisPlayer;
		for (final String p : players) {
			if (!p.equals(thisPlayer)) this.players.addPlayer(p);
		}
	}

	public void playerJoins(final String player) {
		players.addPlayer(player);
	}

	public void playerLeaves(final String player) {
		players.removePlayer(player);
	}

	public void playerEntersGame(final String player) {
		players.removePlayer(player);
	}

	private boolean isConnected = true;

	public void connectionLost() {
		System.err.println("Connection lost!");
		gui.setLeft(null);
		gui.setCenter(disconnected);
		isConnected = false;
	}

	public void reconnected() {
		gui.setLeft(players);
		gui.setCenter(newsPanel);
		isConnected = true;
	}

	public void homeScreen() {
		if (isConnected) {
			gui.setCenter(newsPanel);
			if (status.getWaitingStatus() == StatusPanel.WaitingStatus.NONE) {
				gui.setLeft(players);
			} else {
				gui.setLeft(status);
			}
		} else {
			gui.setLeft(null);
			gui.setCenter(disconnected);
		}
	}

	public void enterQueue() {
		final StatusPanel.WaitingStatus wait = status.getWaitingStatus();
		if (wait == StatusPanel.WaitingStatus.QUEUE) {
			modalDialog.showMessage("You are already in the queue");
		} else if (wait == StatusPanel.WaitingStatus.CHALLENGE) {
			final String prompt = "Cancel challenge?";
			modalDialog.showConfirmation(prompt, status.getWaitingMessage(), r -> {
				if (r == ButtonType.YES) {
					cancelChallenge();
					showVetoPanel();
				}
			});
		} else {
			showVetoPanel();
		}
	}

	private void showVetoPanel() {
		gui.setLeft(null);
		gui.setCenter(vetoPanel);
	}

	private void actuallyEnterQueue(final List<String> vetoStages) {
		parent.enterQueue(vetoStages);
		status.waitInQueue();
		gui.setCenter(newsPanel);
		gui.setLeft(status);
	}

	public void startPracticeGame() {
		homeScreen();
		final StatusPanel.WaitingStatus wait = status.getWaitingStatus();

		if (wait != StatusPanel.WaitingStatus.NONE) {
			final String prompt;
			if (wait == StatusPanel.WaitingStatus.CHALLENGE) {
				prompt = "Cancel challenge?";
			} else if (wait == StatusPanel.WaitingStatus.QUEUE) {
				prompt = "Abandon queue?";
			} else {
				prompt = "Stop waiting?";
			}

			modalDialog.showConfirmation(prompt, status.getWaitingMessage(), r -> {
				if (r == ButtonType.YES) {
					cancelChallenge();
					actuallyStartPracticeGame();
				}
			});

		} else {
			actuallyStartPracticeGame();
		}
	}

	private void actuallyStartPracticeGame() {
		if (config.loadouts.size() < 1) {
			modalDialog.showMessage(
				"You must create at least one loadout before starting a game");
			return;
		}

		if (status.getWaitingStatus() == StatusPanel.WaitingStatus.NONE) {
			try {
				parent.showScreen(
					new ChallengePane(gameData, config, Optional.empty(),
						Player.PLAYER_A, "You", "AI"), getStartSandpitCont());
			} catch (final CorruptDataException e) {
				modalDialog.showError(e, "Game data corrupt", () -> System.exit(1));
			}
		}
	}

	private Optional<String> currentChallenge = Optional.empty();

	/**
	 * Cancel all current challenges and exit the queue
	 * */
	public void cancelChallenge() {
		currentChallenge.ifPresent(parent::cancelChallenge);
		parent.cancelQueue();
		status.waitingDone();
		gui.setLeft(players);
	}

	public void issueChallenge(final String player) {
		if (config.loadouts.isEmpty()) {
			modalDialog.showMessage(
				"You must create at least one loadout before issuing a challenge");
			return;
		}

		if (thisPlayer == null) {
			modalDialog.showError(null,
				"Cannot issue challenge due to an internal error (player name is unknown)");
			return;
		}

		try {
			parent.showScreen(
				new ChallengePane(gameData, config, Optional.empty(),
					Player.randomPlayer(), thisPlayer, player), oCmdReq ->
						oCmdReq.ifPresent(cmdReq -> {
							parent.network.challengePlayer(cmdReq, player);
							status.waitForChallenge(player);
							gui.setLeft(status);
						}));
		} catch (final CorruptDataException e) {
			modalDialog.showError(e, "Error initialising challenge panel");
		}
	}

	public void issuedChallenge(final String player) {
		currentChallenge = Optional.of(player);
		status.waitForChallenge(player);
		gui.setLeft(status);
	}

	public void challengeAccepted(final String player) {
		status.waitingDone();
		gui.setLeft(players);
	}

	public void challengeRejected(
		final String player, final boolean notReady
	) {
		final Optional<String> waitingFor = status.getWaitingForPlayer();

		status.waitingDone();
		gui.setLeft(players);
		if (waitingFor.map(p -> p.equals(player)).orElse(false)) {
			final String message;
			if (notReady) message = player + " is not ready to battle";
			else message = player + " rejected your challenge!";

			modalDialog.showMessage(message);
		}
	}

	public void challengeError(final String player) {
		status.waitingDone();
		gui.setLeft(players);
	}

	private Optional<ChallengePane> cancellableChallenge = Optional.empty();
	private final Set<String> incomingChallenges = new HashSet<>();

	public void cancellationFrom(final String player) {
		incomingChallenges.remove(player);
		cancellableChallenge.ifPresent(p -> p.forceCancel());
	}

	/**
	 * Handle a cancellation of our queued status
	 * */
	public void queueCancellation() {
		status.getWaitingForPlayer().ifPresent(op ->
			modalDialog.showMessage(op + " cancelled the game"));

		incomingChallenges.clear();
		cancellableChallenge.ifPresent(p -> p.forceCancel());
		status.waitingDone();
		gui.setLeft(players);
	}

	private boolean checkCancelled(
		final String player, final boolean notify
	) {
		if (
			!incomingChallenges.contains(player) ||
			status.getWaitingStatus() == StatusPanel.WaitingStatus.QUEUED_SETUP
		) {
			if (notify) modalDialog.showMessage(
				player + " cancelled the game");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Process a challenge (or queued game) from another player.
	 * @param player The player to accept the game from
	 * @param fromQueue true if this game came off the queue (otherwise it was a
	 * challenge)
	 * @param otherCmd the StartBattleCommandRequest to initialise the
	 * ChallengePane from
	 * */
	public void challengeFrom(
		final String player,
		final boolean fromQueue,
		final StartBattleCommandRequest otherCmd
	) {
		if (config.loadouts.isEmpty()) {
			// Automatically refuse the challenge
			parent.network.refuseChallenge(player, thisPlayer);
		}

		incomingChallenges.add(player);

		final String message;
		final String prompt;

		if (fromQueue) {
			message = player + " wants to battle!";
			prompt = "Accept this game?";
		} else {
			message = player + " challenges you to battle!";
			prompt = "Accept this challenge?";
		}

		modalDialog.showConfirmation(prompt, message, r -> {
			if (r == ButtonType.YES) {
				if (checkCancelled(player, true)) return;

				final StatusPanel.WaitingStatus wait = status.getWaitingStatus();
				if (
					(!fromQueue && wait != StatusPanel.WaitingStatus.NONE) ||
					(fromQueue && wait != StatusPanel.WaitingStatus.QUEUE)
				) cancelChallenge();

				try {
					cancellableChallenge = Optional.of(new ChallengePane(
						gameData, config, Optional.of(otherCmd.stage),
						otherCmd.player.otherPlayer(), thisPlayer, player));

					parent.showScreen(
						cancellableChallenge.get(),
							oCmdReq -> {
								cancellableChallenge = Optional.empty();
								if (checkCancelled(player, true)) return;

								if (!oCmdReq.isPresent()) {
									parent.network.refuseChallenge(player, thisPlayer);
								} else if (fromQueue) {
									final StartBattleCommandRequest cmdReq = oCmdReq.get();
									parent.network.acceptQueuedGame(
										cmdReq, cmdReq.player, player);
									incomingChallenges.remove(player);
									status.waitForOtherPlayer(player);
								} else {
									try {
										final StartBattleCommandRequest cmdReq = oCmdReq.get();
										final StartBattleCommand ready =
											cmdReq.makeCommand(otherCmd, gameData);
										parent.network.acceptChallenge(
											ready, otherCmd.player.otherPlayer(), player);
										incomingChallenges.remove(player);
									} catch (final CorruptDataException e) {
										parent.network.refuseChallenge(player, thisPlayer);
										modalDialog.showError(e, "Error initialising battle");
									}
								}
							});
				} catch (final CorruptDataException e) {
					parent.network.refuseChallenge(player, thisPlayer);
					modalDialog.showError(e, "Error initialising challenge panel");
				}
			} else {
				if (checkCancelled(player, false)) return;
				parent.network.refuseChallenge(player, thisPlayer);
			}
		});
	}

	private Consumer<Optional<StartBattleCommandRequest>> getStartSandpitCont() {
		return ostart -> {
			ostart.ifPresent(start -> {
				try {
					// prepare the AI position
					final Player op = start.getOtherPlayer();
					final Stage si = gameData.getStage(start.stage);
					Collection<MapPoint> startTiles = op == Player.PLAYER_A ?
						si.terrain.getPlayerStartTiles() :
						si.terrain.getAIStartTiles();
					final Loadout l = makeSandpitLoadout(start, startTiles, gameData);

					// prepare the battle
					final StartBattleCommand ready =
						(new StartBattleCommandRequest(start.stage, op, "AI", Optional.of(l),
							Optional.of(startTiles.stream().collect(Collectors.toList()))))
							.makeCommand(start, gameData);

					// start the battle
					parent.showScreen(new BattleView(
						ready, Player.PLAYER_A, new SimpleAI(),
						Optional.empty(), gameData, config),
						winCond -> System.err.println("Battle over: " + winCond));
				} catch (final CorruptDataException e) {
					modalDialog.showError(e, "Error starting game", () -> System.exit(1));
				}
			});
		};
	}

	private static Loadout makeSandpitLoadout(
		final StartBattleCommandRequest start,
		final Collection<MapPoint> startTiles,
		final GameDataFactory gameData
	) throws CorruptDataException {
		final List<CharacterProfile> characters = new ArrayList<>();
		for (int i = 0; i < startTiles.size(); i++)
			characters.add(new CharacterProfile(gameData.getCharacter("Robot")));

		return new Loadout("Sandpit", characters, new ArrayList<>());
	}
}

