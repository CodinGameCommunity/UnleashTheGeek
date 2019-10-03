
package com.codingame.utg2019;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.game.Player;
import com.codingame.game.Referee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.core.Tooltip;
import com.codingame.utg2019.action.Action;
import com.codingame.utg2019.action.MoveAction;
import com.codingame.utils.Padding;
import com.codingame.view.AgentData;
import com.codingame.view.CellData;
import com.codingame.view.EventData;
import com.codingame.view.FrameViewData;
import com.codingame.view.GlobalViewData;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Game {
    @Inject private MultiplayerGameManager<Player> gameManager;

    private Random random;
    private Supplier<Stream<? extends Agent>> allAgents;
    private List<Agent> deadAgents;
    private Grid grid;
    private List<EventData> viewerEvents;

    private int trapsPlaced = 0;
    private int robotsDestroyed = 0;
    private int oreDelivered = 0;

    public int getTrapsPlaced() {
        return trapsPlaced;
    }

    public int getRobotsDestroyed() {
        return robotsDestroyed;
    }

    public int getOreDelivered() {
        return oreDelivered;
    }

    private void convertIntents() {
        allAgents.get().forEach(agent -> {
            Action intent = agent.getIntent();
            if (intent.isDig()) {
                Cell cell = grid.get(intent.getTarget());
                if (!cell.isValid() || cell.hasAccessToHQ()) {
                    agent.setIntent(new MoveAction(intent.getTarget()));
                } else if (agent.getPosition().gameDistanceTo(intent.getTarget()) > Config.AGENT_INTERACT_RADIUS) {
                    List<Coord> closest = grid.getClosestTarget(agent.getPosition(), grid.getNeighbours(intent.getTarget()));

                    Coord target = closest.get(randomInt(closest.size()));
                    agent.setIntent(new MoveAction(target));
                }
            } else if (intent.isRequest()) {
                if (!grid.get(agent.getPosition()).hasAccessToHQ()) {
                    List<Coord> closest = grid.getClosestTarget(agent.getPosition(), grid.getHQAccesses());
                    Coord target = closest.get(randomInt(closest.size()));
                    agent.setIntent(new MoveAction(target));
                    gameManager.addToGameSummary(agent.getId() + " wanted to request but instead moves to " + target);
                }
            }

            if (intent.isMove()) {
                if (agent.getPosition().getX() == intent.getTarget().getX() &&
                    agent.getPosition().getY() == intent.getTarget().getY()) {
                    agent.setIntent(Action.NO_ACTION);
                    gameManager.addToGameSummary(agent.getId() + " stays in place");
                }
            }
        });
    }

    private void decrementCooldowns() {
        deadAgents.forEach(da -> da.decrementRespawnTimer());
        gameManager.getPlayers()
            .forEach(Player::decrementCooldowns);
    }

    private void destroyAgent(Agent agent) {
        agent.die();
        deadAgents.add(agent);

        EventData event = new EventData();
        event.type = EventData.AGENT_DEATH;
        event.agent = agent.getId();
        viewerEvents.add(event);
        robotsDestroyed++;
        gameManager.addTooltip(new Tooltip(agent.getOwner().getIndex(), String.format("Robot %d destroyed!", agent.getId())));
    }

    private Grid generateMap() {
        Grid grid = newMap();

        // Set up ore
        int cellCount = Config.MAP_WIDTH * Config.MAP_HEIGHT;
        int clustersMin = Math.max(1, (int) (cellCount * Config.MAP_CLUSTER_DISTRIBUTION_MIN));
        int clustersMax = Math.max(clustersMin, (int) (cellCount * Config.MAP_CLUSTER_DISTRIBUTION_MAX));
        int oreClusterCount = randomInt(clustersMin, clustersMax + 1);

        Padding padding = new Padding()
            .setLeft(3)
            .setRight(2)
            .setTop(2)
            .setBottom(2);

        int tries = 0;
        while (oreClusterCount > 0 && tries < 1000) {

            double factor = Math.pow(random.nextFloat(), Config.MAP_ORE_COEFF_X);
            int x = interpolate(padding.left, Config.MAP_WIDTH - padding.right, factor);
            int y = randomInt(padding.top, Config.MAP_HEIGHT - padding.bottom);

            if (grid.get(x, y).getOre() == 0) {
                Coord clusterCenter = new Coord(x, y);
                for (int i = 0; i < Config.MAP_CLUSTER_SIZE; ++i) {
                    for (int j = 0; j < Config.MAP_CLUSTER_SIZE; ++j) {
                        x = clusterCenter.getX() + (i - Config.MAP_CLUSTER_SIZE / 2);
                        y = clusterCenter.getY() + (j - Config.MAP_CLUSTER_SIZE / 2);

                        int chances = clusterCenter.manhattanTo(x, y) * 2 + 1;
                        int hit = randomInt(chances);
                        if (hit == 0) {
                            // Place ore in cell
                            int amount = randomInt(Config.MAP_ORE_IN_CELL_MIN, Config.MAP_ORE_IN_CELL_MAX + 1);
                            grid.get(x, y).setOre(amount);
                        }
                    }
                }
                oreClusterCount--;
            }
            tries++;
        }

        return grid;
    }

    private Grid newMap() {
        Grid grid = new Grid(Config.MAP_WIDTH, Config.MAP_HEIGHT, random, gameManager.getPlayerCount());

        //First column has access to hq
        for (int y = 0; y < Config.MAP_HEIGHT; ++y) {
            grid.get(0, y).setAccessToHQ(true);
        }
        return grid;
    }

    private List<Agent> getAllAgentsOfIndex(List<List<Agent>> teams, int idx) {
        return teams
            .stream()
            .filter(team -> team.size() > idx)
            .map(team -> team.get(idx))
            .collect(Collectors.toList());
    }

    public FrameViewData getCurrentFrameData() {
        FrameViewData data = new FrameViewData();
        // Entities
        data.agents = allAgents.get()
            .map(Game::agentToAgentFrameData)
            .collect(Collectors.toList());
        data.events = viewerEvents;
        data.scores = gameManager.getPlayers()
            .stream()
            .map(Player::getScore)
            .collect(Collectors.toList());
        return data;
    }

    static private AgentData agentToAgentFrameData(Agent agent) {
        AgentData data = new AgentData();
        data.id = agent.getId();
        data.x = agent.getPosition().getX();
        data.y = agent.getPosition().getY();
        if (agent.getIntent().getTarget() != null) {
            data.tx = agent.getIntent().getTarget().getX();
            data.ty = agent.getIntent().getTarget().getY();
        }
        data.message = agent.getMessage();
        data.dead = agent.isDead();

        return data;
    }

    public List<String> getCurrentFrameInfoFor(Player player) {
        List<String> lines = new ArrayList<>();
        Player opponent;

        if (player != gameManager.getPlayer(0)) {
            opponent = gameManager.getPlayer(0);
        } else {
            opponent = gameManager.getPlayer(1);
        }

        lines.add(Referee.join(player.getOre(), opponent.getOre()));

        // map
        for (int y = 0; y < grid.height; ++y) {
            List<String> row = new ArrayList<>(grid.width);
            for (int x = 0; x < grid.width; ++x) {
                Cell c = grid.get(x, y);

                String oreValue;
                if (grid.isOreVisibleTo(x, y, player)) {

                    oreValue = String.valueOf(c.getOre());
                } else {
                    oreValue = "?";
                }

                row.add(Referee.join(oreValue, c.isHole() ? 1 : 0));
            }
            // <visibleOreAmount> <hole>
            lines.add(row.stream().collect(Collectors.joining(" ")));
        }

        List<String> entities = new ArrayList<>();
        allAgents.get().forEach(
            agent -> entities.add(
                // <id> <type(owner)> <x> <y> <item>
                agentToString(agent, player)
            )
        );

        entities.addAll(itemsToStrings(player, Item.RADAR));
        entities.addAll(itemsToStrings(player, Item.TRAP));

        // <unitCount> <radarCooldown> <trapCooldown>
        lines.add(Referee.join(entities.size(), player.getCooldown(Item.RADAR), player.getCooldown(Item.TRAP)));
        lines.addAll(entities);

        return lines;
    }

    private List<String> itemsToStrings(Player player, Item item) {
        return grid.getItems(item, player)
            .stream()
            .map(
                pos -> Referee.join(pos.getId(), item.getTypeId(), pos.getX(), pos.getY(), item.getTypeId())
            ).collect(Collectors.toList());
    }

    private List<List<Agent>> getFilteredTeams(Predicate<Agent> filter) {
        return getTeamList()
            .stream()
            .map(team -> {
                return team
                    .stream()
                    .filter(filter)
                    .collect(Collectors.toList());
            })
            .collect(Collectors.toList());
    }

    public List<String> getGlobalInfoFor(Player player) {
        List<String> lines = new ArrayList<>();
        // <width> <height>
        lines.add(Referee.join(Config.MAP_WIDTH, Config.MAP_HEIGHT));
        return lines;
    }

    public GlobalViewData getGlobalViewData() {
        GlobalViewData data = new GlobalViewData();
        data.width = Config.MAP_WIDTH;
        data.height = Config.MAP_HEIGHT;
        data.agentsPerPlayer = Config.AGENTS_PER_PLAYER;
        data.ore = grid.cells.entrySet()
            .stream()
            .filter(e -> e.getValue().getOre() > 0)
            .map(e -> {
                Coord coord = e.getKey();
                Cell cell = e.getValue();
                return new CellData(coord, cell.getOre());
            })
            .collect(Collectors.toList());

        return data;
    }

    public Grid getGrid() {
        return grid;
    }

    private <T> int getLargetSize(List<List<T>> lists) {
        return lists
            .stream()
            .map(List::size)
            .max(Comparator.naturalOrder())
            .get();
    }

    private List<List<Agent>> getMoversByTeam() {
        return getFilteredTeams(agent -> agent.getIntent().isMove());
    }

    private List<List<Agent>> getTeamList() {
        return gameManager.getActivePlayers()
            .stream()
            .map(Player::getAgents)
            .collect(Collectors.toList());
    }

    public void init(long seed) {

        viewerEvents = new ArrayList<>();
        deadAgents = new ArrayList<>(Config.AGENTS_PER_PLAYER * gameManager.getPlayerCount());
        allAgents = () -> gameManager.getPlayers().stream().flatMap(p -> p.getAgents().stream());

        random = new Random(seed);
    }

    public void initGameState(String state) {
        if (state == null) {
            grid = generateMap();
            initPlayers();
        } else {
            // The code below was exclusively for debugging the game's engine.
            int[] agentCount = new int[2];
            List<String> commands = Stream.of(state.split("\n|;"))
                .map(String::trim)
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toList());
            commands.forEach(command -> {
                String[] tokens = command.split(" ");
                if (tokens[0].startsWith("ROBOT")) {
                    int playerIdx = Integer.parseInt(tokens[0].substring(5, 6));
                    agentCount[playerIdx]++;
                }
            });

            int agentsPerPlayer = Math.max(Math.max(agentCount[0], agentCount[1]), 1);
            Config.AGENTS_PER_PLAYER = agentsPerPlayer;
            grid = newMap();
            commands.forEach(command -> {
                String[] tokens = command.split(" ");

                if (tokens[0].startsWith("ROBOT")) {
                    int x = Integer.valueOf(tokens[1]);
                    int y = Integer.valueOf(tokens[2]);
                    Coord pos = new Coord(x, y);
                    int playerIdx = Integer.parseInt(tokens[0].substring(5, 6));
                    Item carry = Item.valueOf(tokens[3]);
                    Player player = gameManager.getPlayers().get(playerIdx);
                    Agent agent = new Agent(agentsPerPlayer * player.getIndex() + player.getAgents().size(), player, pos);
                    agent.setInventory(carry);
                    player.addAgent(agent);
                    if (carry != Item.NOTHING) {
                        EventData event = new EventData();
                        event.type = EventData.REQUEST;
                        event.item = carry.getTypeId();
                        event.agent = agent.getId();
                        viewerEvents.add(event);
                    }

                }
            });

            for (int i = 0; i < agentsPerPlayer; ++i) {
                for (Player player : gameManager.getPlayers()) {
                    if (player.getAgents().size() <= i) {
                        Coord pos = new Coord(0, 0);
                        Agent agent = new Agent(Config.AGENTS_PER_PLAYER * player.getIndex() + i, player, pos);
                        player.addAgent(agent);
                        destroyAgent(agent);
                    }
                }
            }

            commands.forEach(command -> {
                String[] tokens = command.split(" ");

                if ("COOLDOWN".equals(tokens[0])) {
                    int playerIdx = Integer.parseInt(tokens[1]);
                    Item item = Item.valueOf(tokens[2]);
                    int cooldown = Integer.parseInt(tokens[3]);
                    gameManager.getPlayer(playerIdx).setCooldown(item, cooldown);
                    return;
                } else if ("SCORE".equals(tokens[0])) {
                    int playerIdx = Integer.parseInt(tokens[1]);
                    int score = Integer.parseInt(tokens[2]);
                    gameManager.getPlayer(playerIdx).setOre(score);
                    return;
                }

                int x = Integer.valueOf(tokens[1]);
                int y = Integer.valueOf(tokens[2]);
                Coord pos = new Coord(x, y);
                if (!tokens[0].startsWith("ROBOT")) {
                    Item item = Item.valueOf(tokens[0]);
                    if (item == Item.ORE) {
                        grid.get(x, y).setOre(Integer.parseInt(tokens[3]));
                    } else {
                        Cell cell = grid.get(x, y);
                        cell.setHole(true);

                        int playerIdx = 0;
                        if (item != Item.NOTHING) {
                            playerIdx = Integer.parseInt(tokens[3]);
                            grid.insertItem(pos, item, gameManager.getPlayer(playerIdx));
                        }

                        EventData event = new EventData();
                        event.type = EventData.BURY;
                        event.x = pos.getX();
                        event.y = pos.getY();
                        event.agent = gameManager.getPlayer(playerIdx).getAgents().get(0).getId();
                        event.item = item.getTypeId();
                        viewerEvents.add(event);
                    }
                }
            });

        }

    }

    public void initPlayers() {
        int spaces = Config.MAP_HEIGHT / 2;

        LinkedList<Integer> available = new LinkedList<>();
        for (int i = 0; i < spaces; ++i) {
            available.add(i);
        }
        Collections.shuffle(available, random);
        for (int i = 0; i < Config.AGENTS_PER_PLAYER; ++i) {
            Integer y = null;
            if (!available.isEmpty()) {
                y = available.poll();
            } else {
                y = randomInt(spaces);
            }

            for (Player player : gameManager.getPlayers()) {
                Coord pos = new Coord(0, y * 2 + (Config.AGENTS_START_PACKED ? 0 : player.getIndex()));
                Agent agent = new Agent(Config.AGENTS_PER_PLAYER * player.getIndex() + i, player, pos);
                player.addAgent(agent);
            }
        }
    }

    private int interpolate(int low, int high, double factor) {
        return (int) (low + factor * (high - low));
    }

    public void performGameUpdate(int turn) {
        convertIntents();

        resolveTraps();
        resolveDigs();
        resolveRequests();

        decrementCooldowns();

        resolveMoves();
        resolveDelivers();
        respawnDeadAgents();
    }

    /**
     * Get a random value within [0;high[
     */
    public int randomInt(int high) {
        return randomInt(0, high);
    }

    /**
     * Get a random value within [low;high[
     */
    private int randomInt(int low, int high) {
        return low + random.nextInt(high - low);
    }

    /**
     * Called before player outputs are handled
     */
    public void resetGameTurnData() {
        for (Player p : gameManager.getPlayers()) {
            p.reset();
        }
        viewerEvents.clear();
    }

    private void resolveDelivers() {
        allAgents.get()
            .filter(agent -> !agent.isDead() && grid.get(agent.getPosition()).hasAccessToHQ())
            .forEach(agent -> {
                if (agent.getInventory() == Item.ORE) {
                    agent.getOwner().scoreOre();
                    agent.setInventory(Item.NOTHING);

                    EventData event = new EventData();
                    event.type = EventData.GIVE_ORE;
                    event.agent = agent.getId();
                    viewerEvents.add(event);
                    oreDelivered++;
                }
            });
    }

    private void resolveDigs() {
        Map<Coord, List<List<Agent>>> digRequests = allAgents
            .get()
            .map(Agent::getIntent)
            .filter(Action::isDig)
            .map(Action::getTarget)
            .distinct()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    c -> getFilteredTeams(agent -> {
                        return agent.getIntent().isDig() && agent.getIntent().getTarget().equals(c);
                    })
                )
            );

        digRequests.forEach((pos, teams) -> {
            Cell cell = grid.get(pos);
            int maxAgentsInTeam = getLargetSize(teams);

            Map<Integer, List<Item>> buried = new HashMap<>();

            //Radar destruction
            teams
                .stream()
                .filter(team -> !team.isEmpty())
                .map(team -> team.get(0))
                .forEach(agent -> {
                    boolean destroyed = grid.destroyRadar(pos, agent.getOwner());
                    if (destroyed) {
                        EventData event = new EventData();
                        event.type = EventData.RADAR_DESTRUCTION;
                        event.x = pos.getX();
                        event.y = pos.getY();
                        event.agent = agent.getId();
                        viewerEvents.add(event);
                    }
                });

            //Ore collection
            for (int idx = 0; idx < maxAgentsInTeam; ++idx) {
                List<Agent> currentDiggers = getAllAgentsOfIndex(teams, idx);
                List<Agent> oreCollectors = new ArrayList<>();

                currentDiggers
                    .stream()
                    .forEach(agent -> {
                        EventData event = new EventData();
                        event.type = EventData.BURY;
                        event.x = pos.getX();
                        event.y = pos.getY();
                        event.agent = agent.getId();
                        event.item = agent.getInventory().getTypeId();
                        viewerEvents.add(event);

                        if (agent.getInventory() != Item.ORE) {
                            oreCollectors.add(agent);
                        }
                        if (agent.getInventory() != Item.NOTHING) {
                            buried.putIfAbsent(agent.getOwner().getIndex(), new ArrayList<>());
                            buried.get(agent.getOwner().getIndex()).add(agent.getInventory());
                            agent.setInventory(Item.NOTHING);
                        }
                        if (event.item == Item.TRAP.getTypeId()) {
                            trapsPlaced++;
                        }
                    });

                // Drill hole
                if (!cell.isHole()) {
                    cell.setHole(true);
                }
                if (cell.getOre() > 0) {
                    cell.reduceOre(oreCollectors.size());
                    oreCollectors
                        .stream()
                        .forEach(agent -> {
                            agent.receiveOre();

                            EventData event = new EventData();
                            event.type = EventData.GET_ORE;
                            event.x = pos.getX();
                            event.y = pos.getY();
                            event.agent = agent.getId();
                            viewerEvents.add(event);
                        });
                }
            }

            // Item insertion
            buried.forEach((playerIndex, itemSet) -> {
                for (Item item : itemSet) {
                    if (item == Item.ORE) {
                        cell.incrementOre();
                    } else {
                        grid.insertItem(pos, item, gameManager.getPlayer(playerIndex));
                    }
                }
            });
        });

    }

    private void resolveMoves() {
        List<List<Agent>> movers = getMoversByTeam();

        for (List<Agent> team : movers) {
            for (Agent agent : team) {
                MoveAction action = (MoveAction) agent.getIntent();

                List<Coord> obstacles = Config.ROBOTS_CAN_OCCUPY_SAME_CELL
                    ? Collections.emptyList()
                    : team.stream()
                        .map(Agent::getPosition)
                        .collect(Collectors.toList());

                List<Coord> path = grid.findPath(
                    agent.getPosition(),
                    action.getTarget(),
                    obstacles
                );
                action.setPath(path);
                if (!path.isEmpty()) {
                    agent.setPosition(path.get(path.size() - 1));
                }
            }
        }
    }

    private static int compareByInventorySpace(Agent a, Agent b) {
        boolean hasItemA = a.getInventory() != Item.NOTHING;
        boolean hasItemB = b.getInventory() != Item.NOTHING;
        if (hasItemA && !hasItemB)
            return 1;
        if (!hasItemA && hasItemB)
            return -1;
        return 0;
    }

    private void resolveRequests() {
        allAgents.get()
            .filter(agent -> agent.getIntent().isRequest())
            .sorted(Game::compareByInventorySpace)
            .forEach(agent -> {
                Item item = agent.getIntent().getItem();
                gameManager.addToGameSummary(agent.getId() + " requested a " + item.name());
                if (agent.getOwner().getCooldown(item) == 0) {
                    agent.getOwner().startCooldown(item);
                    agent.setInventory(item);
                    gameManager.addToGameSummary("and was given one\n");

                    EventData event = new EventData();
                    event.type = EventData.REQUEST;
                    event.item = item.getTypeId();
                    event.agent = agent.getId();
                    viewerEvents.add(event);
                } else {
                    gameManager.addToGameSummary("but cooldown is " + agent.getOwner().getCooldown(item) + "\n");
                }
            });

    }

    private void resolveTraps() {
        Set<Coord> triggeredTraps = allAgents
            .get()
            .map(Agent::getIntent)
            .filter(intent -> intent.isDig())
            .filter(intent -> grid.hasTrap(intent.getTarget()))
            .map(Action::getTarget)
            .collect(Collectors.toSet());

        Set<Coord> deathZone;

        if (Config.TRAP_CHAIN_REACTION) {
            deathZone = new HashSet<>(triggeredTraps);
            Queue<Coord> exploding = new LinkedList<Coord>(triggeredTraps);
            while (!exploding.isEmpty()) {
                Coord trap = exploding.poll();

                grid.getCellsInRange(trap, Config.TRAP_RANGE)
                    .stream()
                    .forEach(c -> {
                        deathZone.add(c);
                        if (grid.hasTrap(c) && !triggeredTraps.contains(c)) {
                            exploding.add(c);
                            triggeredTraps.add(c);
                        }
                    });
            }
        } else {
            deathZone = triggeredTraps
                .stream()
                .flatMap(coord -> {
                    return grid.getCellsInRange(coord, Config.TRAP_RANGE).stream();
                })
                .collect(Collectors.toSet());
        }
        deathZone
            .stream()
            .forEach(coord -> grid.removeTrap(coord));

        allAgents
            .get()
            .filter(agent -> !agent.isDead() && deathZone.contains(agent.getPosition()))
            .collect(Collectors.toList())
            .forEach(this::destroyAgent);

        for (Coord trigger : triggeredTraps) {
            EventData event = new EventData();
            event.type = EventData.EXPLOSION;
            event.x = trigger.getX();
            event.y = trigger.getY();
            viewerEvents.add(event);
        }
    }

    private void respawnDeadAgents() {
        deadAgents
            .stream()
            .filter(Agent::shouldRespawn)
            .collect(Collectors.toList())
            .stream()
            .forEach(da -> {
                deadAgents.remove(da);
                da.factoryReset();

                EventData event = new EventData();
                event.type = EventData.RESPAWN;
                event.agent = da.getId();
                viewerEvents.add(event);
            });

    }

    private String agentToString(Agent agent, Player player) {
        return Referee.join(
            agent.getId(),
            player == agent.getOwner() ? Config.TYPE_MY_AGENT : Config.TYPE_FOE_AGENT,
            agent.isDead() ? -1 : agent.getPosition().getX(),
            agent.isDead() ? -1 : agent.getPosition().getY(),
            player == agent.getOwner() ? agent.getInventory().getTypeId() : Item.NOTHING.getTypeId()
        );
    }

    public boolean gameOver(int turn) {
        if (getRemainingOre() == 0) {
            return true;
        }
        // Player with most ore is only player with live bots
        Player playerWithMostOre = getPlayerWithMostOre();
        if (playerWithMostOre != null) {
            return gameManager.getPlayers().stream()
                .filter(player -> player != playerWithMostOre)
                .flatMap(player -> player.getAgents().stream())
                .allMatch(Agent::isDead);
        }

        // No bots left
        return gameManager.getPlayers().stream()
            .flatMap(player -> player.getAgents().stream())
            .allMatch(Agent::isDead);
    }

    private Player getPlayerWithMostOre() {
        int most = 0;
        Player result = null;
        for (Player player : gameManager.getPlayers()) {
            if (result == null || player.getOre() > most) {
                most = player.getOre();
                result = player;
            } else if (player.getOre() == most) {
                return null;
            }
        }
        return result;
    }

    private int getRemainingOre() {
        int remainingOre = 0;

        for (Cell c : grid.cells.values()) {
            remainingOre += c.getOre();
        }
        for (Player player : gameManager.getPlayers()) {
            for (Agent agent : player.getAgents()) {
                if (agent.getInventory() == Item.ORE) {
                    remainingOre += 1;
                }
            }
        }
        ;
        return remainingOre;
    }
}
