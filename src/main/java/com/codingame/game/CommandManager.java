package com.codingame.game;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.utg2019.Agent;
import com.codingame.utg2019.Coord;
import com.codingame.utg2019.Item;
import com.codingame.utg2019.action.Action;
import com.codingame.utg2019.action.DigAction;
import com.codingame.utg2019.action.MoveAction;
import com.codingame.utg2019.action.RequestAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CommandManager {

    @Inject private MultiplayerGameManager<Player> gameManager;

    static final Pattern PLAYER_MOVE_PATTERN = Pattern.compile(
        "^MOVE\\s+(?<x>-?\\d+)\\s+(?<y>-?\\d+)"
            + "(?:\\s+(?<message>.+))?"
            + "\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    static final Pattern PLAYER_INTERACT_PATTERN = Pattern.compile(
        "^(INTERACT|DIG)\\s+(?<x>-?\\d+)\\s+(?<y>-?\\d+)"
            + "(?:\\s+(?<message>.+))?"
            + "\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    static final Pattern PLAYER_REQUEST_PATTERN = Pattern.compile(
        "^REQUEST\\s+(?<item>(?:TRAP|RADAR))"
            + "(?:\\s+(?<message>.+))?"
            + "\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    static final Pattern PLAYER_WAIT_PATTERN = Pattern.compile(
        "^WAIT"
            + "(?:\\s+(?<message>.+))?"
            + "\\s*$",
        Pattern.CASE_INSENSITIVE
    );

    static String EXPECTED = "DIG <x> <y> | REQUEST <item> | MOVE <x> <y> | WAIT";

    public void handleCommands(Player player, List<String> lines) {
        int i = 0;
        for (String line : lines) {
            Agent agent = player.getAgents().get(i++);
            if (agent.isDead()) {
                continue;
            }

            try {

                Matcher match = PLAYER_WAIT_PATTERN.matcher(line);
                if (match.matches()) {
                    //Message
                    matchMessage(agent, match);
                    continue;
                }

                match = PLAYER_MOVE_PATTERN.matcher(line);
                if (match.matches()) {
                    int x = Integer.valueOf(match.group("x"));
                    int y = Integer.valueOf(match.group("y"));

                    Action intent = new MoveAction(new Coord(x, y));
                    agent.setIntent(intent);

                    //Message
                    matchMessage(agent, match);
                    continue;
                }

                match = PLAYER_INTERACT_PATTERN.matcher(line);
                if (match.matches()) {
                    int x = Integer.valueOf(match.group("x"));
                    int y = Integer.valueOf(match.group("y"));

                    Action intent = new DigAction(new Coord(x, y));
                    agent.setIntent(intent);

                    //Message
                    matchMessage(agent, match);
                    continue;
                }

                match = PLAYER_REQUEST_PATTERN.matcher(line);
                if (match.matches()) {
                    Item item = Item.valueOf(match.group("item").toUpperCase());

                    Action intent = new RequestAction(item);
                    agent.setIntent(intent);

                    //Message
                    matchMessage(agent, match);
                    continue;
                }

                throw new InvalidInputException(EXPECTED, line);

            } catch (InvalidInputException e) {
                deactivatePlayer(player, e.getMessage());
                gameManager.addToGameSummary("Bad command: " + e.getMessage());
                return;
            } catch (Exception e) {

                deactivatePlayer(player, new InvalidInputException(e.toString(), EXPECTED, line).getMessage());
                gameManager.addToGameSummary("Bad command: " + e.getMessage());
                return;
            }

        }
    }

    private void deactivatePlayer(Player player, String message) {
        player.deactivate(escapeHTMLEntities(message));
    }

    private String escapeHTMLEntities(String message) {
        return message
            .replace("&lt;", "<")
            .replace("&gt;", ">");
    }

    private void matchMessage(Agent agent, Matcher match) {
        String message = match.group("message");
        if (message != null) {
            String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
            String messageWithoutEmojis = message.replaceAll(characterFilter, "");
            agent.setMessage(messageWithoutEmojis);
        }
    }
}
