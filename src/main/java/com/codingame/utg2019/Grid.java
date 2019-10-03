package com.codingame.utg2019;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import com.codingame.game.Player;

public class Grid {
    int width, height;
    Map<Coord, Cell> cells;
    Map<Item, List<Set<ItemCoord>>> items;
    List<Set<ItemCoord>> traps, radars;

    Random random;

    public Grid(int width, int height, Random random, int playerCount) {
        this.random = random;
        this.width = width;
        this.height = height;
        cells = new HashMap<>();
        items = new HashMap<>();
        traps = new ArrayList<>(playerCount);
        radars = new ArrayList<>(playerCount);
        items.put(Item.RADAR, radars);
        items.put(Item.TRAP, traps);

        for (int i = 0; i < playerCount; ++i) {
            radars.add(new HashSet<>());
            traps.add(new HashSet<>());
        }

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Coord coord = new Coord(x, y);
                Cell cell = new Cell();
                cells.put(coord, cell);
            }
        }
    }

    public Cell get(int x, int y) {
        return cells.getOrDefault(new Coord(x, y), Cell.NO_CELL);
    }

    public List<Coord> unrollPath(PathNode current) {
        List<Coord> path = new ArrayList<>();
        while (!current.isStart()) {
            path.add(0, current.coord);
            current = current.previous;
        }
        return path;
    }

    // This greedy best first search was originally designed with a different control scheme in mind for the robots
    public List<Coord> findPath(Coord start, Coord target, List<Coord> restricted) {
        PriorityQueue<PathNode> queue = new PriorityQueue<>(byDistanceTo(target));
        Set<Coord> computed = new HashSet<>();

        List<PathNode> closest = new ArrayList<>();
        int closestBy = 0;

        queue.add(new PathNode(start));
        computed.add(start);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.coord.equals(target)) {
                return unrollPath(current);
            } else {
                int distance = current.coord.gameDistanceTo(target);
                if (closest.isEmpty() || closestBy > distance) {
                    closest.clear();
                    closest.add(current);
                    closestBy = distance;
                } else if (!closest.isEmpty() && closestBy == distance) {
                    closest.add(current);
                }
            }
            if (current.steps < Config.AGENTS_MOVE_DISTANCE) {
                List<Coord> neighbours = getNeighbours(current.coord);
                for (Coord neigh : neighbours) {
                    if (!restricted.contains(neigh) && !computed.contains(neigh)) {
                        queue.add(new PathNode(neigh, current));
                        computed.add(neigh);
                    }
                }
            }
        }
        if (closest.isEmpty()) {
            return Collections.emptyList();
        }
        return unrollPath(closest.get(random.nextInt(closest.size())));
    }

    public List<Coord> getNeighbours(Coord pos) {
        List<Coord> neighs = new ArrayList<>();
        for (Coord delta : Config.ADJACENCY.deltas) {
            Coord n = new Coord(pos.getX() + delta.getX(), pos.getY() + delta.getY());
            if (get(n) != Cell.NO_CELL) {
                neighs.add(n);
            }
        }
        return neighs;
    }

    Cell get(Coord n) {
        return get(n.getX(), n.getY());
    }

    private Comparator<PathNode> byDistanceTo(Coord target) {
        return Comparator.comparing(node -> node.coord.gameDistanceTo(target));

    }

    public List<Coord> getClosestTarget(Coord from, List<Coord> targets) {
        List<Coord> closest = new ArrayList<>();
        int closestBy = 0;
        for (Coord neigh : targets) {
            int distance = from.gameDistanceTo(neigh);
            if (closest.isEmpty() || closestBy > distance) {
                closest.clear();
                closest.add(neigh);
                closestBy = distance;
            } else if (!closest.isEmpty() && closestBy == distance) {
                closest.add(neigh);
            }
        }
        return closest;
    }

    public boolean hasTrap(Coord pos) {
        return traps
            .stream()
            .anyMatch(list -> list.contains(pos));
    }

    public void removeTrap(Coord pos) {
        traps.forEach(list -> list.remove(pos));
    }

    public boolean destroyRadar(Coord pos, Player destroyer) {
        boolean destroyed = false;
        for (int i = 0; i < radars.size(); ++i) {
            if (i != destroyer.getIndex()) {
                destroyed |= radars.get(i).remove(pos);
            }
        }
        return destroyed;
    }

    public void insertItem(Coord pos, Item item, Player itemOwner) {
        Set<ItemCoord> itemSet = getItems(item, itemOwner);
        itemSet.add(new ItemCoord(pos.getX(), pos.getY()));
    }

    public List<Coord> getHQAccesses() {
        List<Coord> coords = new ArrayList<>(Config.MAP_HEIGHT);
        for (int y = 0; y < Config.MAP_HEIGHT; ++y) {
            coords.add(new Coord(0, y));
        }
        return coords;
    }

    public boolean isOreVisibleTo(int x, int y, Player player) {
        return radars.get(player.getIndex())
            .stream()
            .anyMatch(pos -> {
                if (Config.EUCLIDEAN_RADAR) {
                    return pos.euclideanTo(x, y) <= Config.RADAR_RANGE;
                } else {
                    return pos.gameDistanceTo(x, y) <= Config.RADAR_RANGE;
                }
            });
    }

    public Set<ItemCoord> getItems(Item item, Player player) {
        return items
            .get(item)
            .get(player.getIndex());
    }

    public Collection<Coord> getCellsInRange(Coord coord, int range) {
        Set<Coord> result = new HashSet<>();
        Queue<PathNode> queue = new LinkedList<>();
        queue.add(new PathNode(coord));
        while (!queue.isEmpty()) {
            PathNode e = queue.poll();
            result.add(e.coord);
            if (e.steps < range) {
                getNeighbours(e.coord)
                    .stream()
                    .forEach(neigh -> {
                        if (!result.contains(neigh)) {
                            queue.add(new PathNode(neigh, e));
                        }
                    });
            }
        }

        return result;
    }
}
