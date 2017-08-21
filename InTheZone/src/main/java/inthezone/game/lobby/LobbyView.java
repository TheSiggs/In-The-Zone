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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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
import inthezone.game.loadoutEditor.LoadoutOverview;

public class LobbyView extends BorderPane {
	private final ContentPane parent;
	private final GameDataFactory gameData;
	private final ClientConfig config;

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

		setTop(mainMenuBack);
		setLeft(players);
		setCenter(newsPanel);
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

			final Alert a = new Alert(
				Alert.AlertType.CONFIRMATION, prompt,
				ButtonType.YES, ButtonType.NO);
			a.setHeaderText(status.getWaitingMessage());
			a.showAndWait().ifPresent(r -> {
				if (r == ButtonType.YES) cancelChallenge();
			});
		}

		if (config.loadouts.size() < 1) {
			final Alert a = new Alert(
				Alert.AlertType.INFORMATION, null, ButtonType.OK);
			a.setHeaderText(
				"You must create at least one loadout before starting a game");
			a.showAndWait();
			return;
		}

		if (status.getWaitingStatus() == StatusPanel.WaitingStatus.NONE) {
			try {
				parent.showScreen(
					new ChallengePane(gameData, config, Optional.empty(),
						Player.PLAYER_A, "You", "AI"), getStartSandpitCont());
			} catch (final CorruptDataException e) {
				final Alert a = new Alert(Alert.AlertType.ERROR,
					e.getMessage(), ButtonType.CLOSE);
				a.setHeaderText("Game data corrupt");
				a.showAndWait();
				System.exit(1);
			}
		}
	}

	private Optional<String> currentChallenge;

	public void cancelChallenge() {
		currentChallenge.ifPresent(parent::cancelChallenge);
		status.waitingDone();
		setLeft(players);
	}

	public void issueChallenge(final String player) {
		if (config.loadouts.isEmpty()) {
			final Alert a = new Alert(
				Alert.AlertType.INFORMATION, null, ButtonType.OK);
			a.setHeaderText(
				"You must create at least one loadout before issuing a challenge");
			a.showAndWait();
			return;
		}

		if (thisPlayer == null) {
			final Alert a = new Alert(
				Alert.AlertType.ERROR, null, ButtonType.CLOSE);
			a.setHeaderText(
				"Cannot issue challenge due to an internal error (player name is unknown)");
			a.showAndWait();
			return;
		}

		try {
			parent.showScreen(
				new ChallengePane(gameData, config, Optional.empty(),
					Player.randomPlayer(), thisPlayer, player), oCmdReq ->
						oCmdReq.ifPresent(cmdReq -> {
							parent.network.challengePlayer(cmdReq, player);
							status.waitForChallenge(player);
							this.setLeft(status);
						}));
		} catch (final CorruptDataException e) {
			final Alert a = new Alert(Alert.AlertType.ERROR,
				e.getMessage(), ButtonType.CLOSE);
			a.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			a.setHeaderText("Error initialising challenge panel");
			a.showAndWait();
		}
	}

	public void issuedChallenge(final String player) {
		currentChallenge = Optional.of(player);
		status.waitForChallenge(player);
		setLeft(status);
	}

	public void challengeAccepted(final String player) {
		status.waitingDone();
		setLeft(players);
	}

	public void challengeRejected(final String player) {
		final Optional<String> waitingFor = status.getWaitingForPlayer();

		status.waitingDone();
		setLeft(players);
		if (waitingFor.map(p -> p.equals(player)).orElse(false)) {
			final Alert a = new Alert(
				Alert.AlertType.INFORMATION, null, ButtonType.OK);
			a.setHeaderText(player + " rejected your challenge!");
			a.showAndWait();
		}
	}

	public void challengeError(final String player) {
		status.waitingDone();
		setLeft(players);
	}

	public void challengeFrom(
		final String player, final StartBattleCommandRequest otherCmd
	) {
		if (config.loadouts.isEmpty()) {
			// Automatically refuse the challenge
			parent.network.refuseChallenge(player, thisPlayer);
		}

		final Alert a = new Alert(
			Alert.AlertType.CONFIRMATION, "Accept this challenge?",
			ButtonType.YES, ButtonType.NO);
		a.setHeaderText(player + " challenges you to battle!");
		a.showAndWait().ifPresent(r -> {
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
										final Alert ae = new Alert(
											Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
										ae.setHeaderText("Error initialising battle");
										ae.showAndWait();
									}
								}
							});
				} catch (final CorruptDataException e) {
					parent.network.refuseChallenge(player, thisPlayer);
					final Alert ae = new Alert(
						Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
					ae.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
					ae.setHeaderText("Error initialising challenge panel");
					ae.showAndWait();
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
					final Alert a = new Alert(Alert.AlertType.ERROR,
						e.getMessage(), ButtonType.OK);
					a.setHeaderText("Error starting game");
					a.showAndWait();
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

