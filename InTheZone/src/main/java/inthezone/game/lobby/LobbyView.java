package inthezone.game.lobby;

import isogame.engine.CorruptDataException;
import isogame.engine.MapPoint;
import isogame.engine.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

		practiceGame.setOnAction(event -> startPracticeGame());

		loadoutsLabel.setOnMouseClicked(event -> {
			parent.showScreen(new LoadoutOverview(parent), v -> {});
		});

		logoutLabel.setOnMouseClicked(event -> {
			parent.network.logout();
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

	public void connectionLost() {
	}

	public void reconnected() {
	}

	public void startPracticeGame() {
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

	private Optional<String> currentChallenge;

	public void cancelChallenge() {
		currentChallenge.ifPresent(parent::cancelChallenge);
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

	public void challengeRejected(final String player) {
		final Optional<String> waitingFor = status.getWaitingForPlayer();

		status.waitingDone();
		gui.setLeft(players);
		if (waitingFor.map(p -> p.equals(player)).orElse(false)) {
			modalDialog.showMessage(player + " rejected your challenge!");
		}
	}

	public void challengeError(final String player) {
		status.waitingDone();
		gui.setLeft(players);
	}

	public void challengeFrom(
		final String player, final StartBattleCommandRequest otherCmd
	) {
		if (config.loadouts.isEmpty()) {
			// Automatically refuse the challenge
			parent.network.refuseChallenge(player, thisPlayer);
		}

		final String message = player + " challenges you to battle!";
		final String prompt = "Accept this challenge?";
		modalDialog.showConfirmation(prompt, message, r -> {
			if (r == ButtonType.YES) {
				try {
					parent.showScreen(
						new ChallengePane(gameData, config, Optional.of(otherCmd.stage),
							otherCmd.player.otherPlayer(), thisPlayer, player),
							oCmdReq -> {
								if (!oCmdReq.isPresent()) {
									parent.network.refuseChallenge(player, thisPlayer);
								} else {
									try {
										final StartBattleCommandRequest cmdReq = oCmdReq.get();
										final StartBattleCommand ready =
											cmdReq.makeCommand(otherCmd, gameData);
										parent.network.acceptChallenge(
											ready, otherCmd.player.otherPlayer(), player);
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
						(new StartBattleCommandRequest(start.stage, op, "AI", l,
							startTiles.stream().collect(Collectors.toList())))
							.makeCommand(start, gameData);

					// start the battle
					parent.showScreen(new BattleView(
						ready, Player.PLAYER_A, new SimpleAI(), Optional.empty(), gameData),
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

