package com.codingame.game;

import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.utg2019.Config;
import com.codingame.utg2019.Game;
import com.codingame.view.FrameViewData;
import com.codingame.view.GlobalViewData;
import com.codingame.view.ViewModule;
import com.codingame.view.endscreen.EndScreenModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Referee extends AbstractReferee {

    @Inject private MultiplayerGameManager<Player> gameManager;
    @Inject private ViewModule viewModule;
    @Inject private CommandManager commandManager;
    @Inject private EndScreenModule endScreenModule;
    @Inject private Game game;

    long seed;

    @Override
    public void init() {

        viewModule.setReferee(this);
        this.seed = gameManager.getSeed();

        computeConfiguration(gameManager.getGameParameters());

        try {
            game.init(seed);
            String state = gameManager.getGameParameters().getProperty("state");
            game.initGameState(state);
            sendGlobalInfo();

            gameManager.setFrameDuration(1000);
            gameManager.setMaxTurns(200);
            gameManager.setTurnMaxTime(50);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Referee failed to initialize");
            abort();
        }
    }

    private void computeConfiguration(Properties gameParameters) {
        Config.take(gameParameters);
//        Config.give(gameParameters);
    }

    private void abort() {
        System.err.println("Unexpected game end");
        gameManager.endGame();
    }

    private void sendGlobalInfo() {
        for (Player player : gameManager.getActivePlayers()) {
            for (String line : game.getGlobalInfoFor(player)) {
                player.sendInputLine(line);
            }
        }
    }

    @Override
    public void gameTurn(int turn) {
        game.resetGameTurnData();

        // Give input to players
        for (Player player : gameManager.getActivePlayers()) {
            for (String line : game.getCurrentFrameInfoFor(player)) {
                player.sendInputLine(line);
            }
            player.execute();
        }
        // Get output from players
        handlePlayerCommands();

        game.performGameUpdate(turn);

        if (game.gameOver(turn) || gameManager.getActivePlayers().size() < 2) {
            gameManager.endGame();
        }
    }

    private void handlePlayerCommands() {
        for (Player player : gameManager.getActivePlayers()) {
            try {
                commandManager.handleCommands(player, player.getOutputs());
            } catch (TimeoutException e) {
                player.deactivate("Timeout!");
                gameManager.addToGameSummary(player.getNicknameToken() + " has not provided " + player.getExpectedOutputLines() + " lines in time");
            }
        }

    }

    static public String join(Object... args) {
        return Stream.of(args).map(String::valueOf).collect(Collectors.joining(" "));
    }

    @Override
    public void onEnd() {
        gameManager.getPlayers().forEach(player -> player.setScore(player.isActive() ? player.getOre() : -1));
        int reference = gameManager.getPlayer(0).getScore();
        boolean tie = gameManager.getPlayers()
            .stream()
            .skip(1)
            .mapToInt(Player::getScore)
            .allMatch(i -> i == reference);

        endScreenModule.setScores(
            gameManager.getPlayers()
                .stream()
                .mapToInt(Player::getScore)
                .toArray(),
            tie
        );

        gameManager.putMetadata("robotsDestroyed", String.valueOf(game.getRobotsDestroyed()));
        gameManager.putMetadata("trapsPlaced", String.valueOf(game.getTrapsPlaced()));
        gameManager.putMetadata("oreDelivered", String.valueOf(game.getOreDelivered()));
    }

    public FrameViewData getCurrentFrameData() {
        return game.getCurrentFrameData();
    }

    public GlobalViewData getGlobalData() {
        return game.getGlobalViewData();
    }

}
