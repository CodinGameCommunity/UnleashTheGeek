package com.codingame.utg2019;

import java.util.Properties;

public class Config {
    public static enum Adjacency {
        FOUR_ADJACENCY(new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1)), EIGHT_ADJACENCY(
            new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1), new Coord(-1, -1),
            new Coord(1, -1), new Coord(-1, 1), new Coord(1, 1)
        );

        public final Coord[] deltas;

        private Adjacency(Coord... deltas) {
            this.deltas = deltas;
        }
    }

    public static final Adjacency FOUR_ADJACENCY = Adjacency.FOUR_ADJACENCY;
    public static final Adjacency EIGHT_ADJACENCY = Adjacency.EIGHT_ADJACENCY;

    public static final int TYPE_NONE = -1;
    public static final int TYPE_MY_AGENT = 0;
    public static final int TYPE_FOE_AGENT = 1;
    public static final int TYPE_RADAR = 2;
    public static final int TYPE_TRAP = 3;
    public static final int TYPE_ORE = 4;

    public static Adjacency ADJACENCY = FOUR_ADJACENCY;
    public static int AGENTS_MOVE_DISTANCE = 4;
    public static int AGENTS_PER_PLAYER = 5;
    public static int AGENT_INTERACT_RADIUS = 1;
    public static int AGENT_RESPAWN_TIME = 999;
    public static int MAP_CLUSTER_SIZE = 5;
    public static double MAP_ORE_COEFF_X = 0.55;
    public static int MAP_HEIGHT = 15;
    public static int MAP_WIDTH = 30;
    public static double MAP_CLUSTER_DISTRIBUTION_MAX = 0.064;
    public static double MAP_CLUSTER_DISTRIBUTION_MIN = 0.032;
    public static int MAP_ORE_IN_CELL_MAX = 3;
    public static int MAP_ORE_IN_CELL_MIN = 1;
    public static int RADAR_COOLDOWN = 5;
    public static int RADAR_RANGE = 4;
    public static boolean ROBOTS_CAN_OCCUPY_SAME_CELL = true;
    public static boolean TRAP_CHAIN_REACTION = true;
    public static boolean TRAP_FRIENDLY_FIRE = true;
    public static int TRAP_COOLDOWN = 5;
    public static int TRAP_RANGE = 1;
    public static boolean EUCLIDEAN_RADAR = false;
    public static boolean AGENTS_START_PACKED = true;

    public static void take(Properties params) {
        ADJACENCY = getFromParams(params, "ADJACENCY", ADJACENCY);
        EUCLIDEAN_RADAR = getFromParams(params, "EUCLIDEAN_RADAR", EUCLIDEAN_RADAR);
        AGENTS_MOVE_DISTANCE = getFromParams(params, "AGENTS_MOVE_DISTANCE", AGENTS_MOVE_DISTANCE);
        AGENTS_PER_PLAYER = getFromParams(params, "AGENTS_PER_PLAYER", AGENTS_PER_PLAYER);
        AGENT_INTERACT_RADIUS = getFromParams(params, "AGENT_INTERACT_RADIUS", AGENT_INTERACT_RADIUS);
        AGENT_RESPAWN_TIME = getFromParams(params, "AGENT_RESPAWN_TIME", AGENT_RESPAWN_TIME);
        MAP_CLUSTER_SIZE = getFromParams(params, "MAP_CLUSTER_SIZE", MAP_CLUSTER_SIZE);
        MAP_ORE_COEFF_X = getFromParams(params, "MAP_ORE_COEFF_X", MAP_ORE_COEFF_X);
        MAP_HEIGHT = getFromParams(params, "MAP_HEIGHT", MAP_HEIGHT);
        MAP_WIDTH = getFromParams(params, "MAP_WIDTH", MAP_WIDTH);
        MAP_ORE_IN_CELL_MAX = getFromParams(params, "MAP_ORE_IN_CELL_MAX", MAP_ORE_IN_CELL_MAX);
        MAP_CLUSTER_DISTRIBUTION_MIN = getFromParams(params, "MAP_CLUSTER_DISTRIBUTION_MIN", MAP_CLUSTER_DISTRIBUTION_MIN);
        MAP_CLUSTER_DISTRIBUTION_MAX = getFromParams(params, "MAP_CLUSTER_DISTRIBUTION_MAX", MAP_CLUSTER_DISTRIBUTION_MAX);
        MAP_ORE_IN_CELL_MIN = getFromParams(params, "MAP_ORE_IN_CELL_MIN", MAP_ORE_IN_CELL_MIN);
        RADAR_COOLDOWN = getFromParams(params, "RADAR_COOLDOWN", RADAR_COOLDOWN);
        RADAR_RANGE = getFromParams(params, "RADAR_RANGE", RADAR_RANGE);
        TRAP_RANGE = getFromParams(params, "TRAP_RANGE", TRAP_RANGE);
        ROBOTS_CAN_OCCUPY_SAME_CELL = getFromParams(params, "ROBOTS_CAN_OCCUPY_SAME_CELL", ROBOTS_CAN_OCCUPY_SAME_CELL);
        TRAP_CHAIN_REACTION = getFromParams(params, "TRAP_CHAIN_REACTION", TRAP_CHAIN_REACTION);
        TRAP_COOLDOWN = getFromParams(params, "TRAP_COOLDOWN", TRAP_COOLDOWN);
    }

    private static double getFromParams(Properties params, String name, double defaultValue) {
        String inputValue = params.getProperty(name);
        if (inputValue != null) {
            try {
                return Double.parseDouble(inputValue);
            } catch (NumberFormatException e) {
                // Do naught
            }
        }
        return defaultValue;
    }

    private static Adjacency getFromParams(Properties params, String name, Adjacency defaultValue) {
        String inputValue = params.getProperty(name);
        if (inputValue != null) {
            try {
                return Adjacency.valueOf(inputValue);
            } catch (IllegalArgumentException e) {
                // Do naught
            }
        }
        return defaultValue;

    }

    private static int getFromParams(Properties params, String name, int defaultValue) {
        String inputValue = params.getProperty(name);
        if (inputValue != null) {
            try {
                return Integer.parseInt(inputValue);
            } catch (NumberFormatException e) {
                // Do naught
            }
        }
        return defaultValue;
    }

    private static boolean getFromParams(Properties params, String name, boolean defaultValue) {
        String inputValue = params.getProperty(name);
        if (inputValue != null) {
            try {
                return new Boolean(inputValue);
            } catch (NumberFormatException e) {
                // Do naught
            }
        }
        return defaultValue;
    }

    public static void give(Properties params) {
        params.setProperty("ADJACENCY", String.valueOf(ADJACENCY));
        params.setProperty("AGENTS_MOVE_DISTANCE", String.valueOf(AGENTS_MOVE_DISTANCE));
        params.setProperty("AGENTS_PER_PLAYER", String.valueOf(AGENTS_PER_PLAYER));
        params.setProperty("AGENT_INTERACT_RADIUS", String.valueOf(AGENT_INTERACT_RADIUS));
        params.setProperty("AGENT_RESPAWN_TIME", String.valueOf(AGENT_RESPAWN_TIME));
        params.setProperty("MAP_CLUSTER_SIZE", String.valueOf(MAP_CLUSTER_SIZE));
        params.setProperty("MAP_ORE_COEFF_X", String.valueOf(MAP_ORE_COEFF_X));
        params.setProperty("MAP_HEIGHT", String.valueOf(MAP_HEIGHT));
        params.setProperty("MAP_WIDTH", String.valueOf(MAP_WIDTH));
        params.setProperty("MAP_CLUSTER_DISTRIBUTION_MAX", String.valueOf(MAP_CLUSTER_DISTRIBUTION_MAX));
        params.setProperty("MAP_ORE_IN_CELL_MAX", String.valueOf(MAP_ORE_IN_CELL_MAX));
        params.setProperty("MAP_CLUSTER_DISTRIBUTION_MIN", String.valueOf(MAP_CLUSTER_DISTRIBUTION_MIN));
        params.setProperty("MAP_ORE_IN_CELL_MIN", String.valueOf(MAP_ORE_IN_CELL_MIN));
        params.setProperty("RADAR_COOLDOWN", String.valueOf(RADAR_COOLDOWN));
        params.setProperty("RADAR_RANGE", String.valueOf(RADAR_RANGE));
        params.setProperty("TRAP_RANGE", String.valueOf(TRAP_RANGE));
        params.setProperty("ROBOTS_CAN_OCCUPY_SAME_CELL", String.valueOf(ROBOTS_CAN_OCCUPY_SAME_CELL));
        params.setProperty("TRAP_CHAIN_REACTION", String.valueOf(TRAP_CHAIN_REACTION));
        params.setProperty("TRAP_COOLDOWN", String.valueOf(TRAP_COOLDOWN));
        params.setProperty("EUCLIDEAN_RADAR", String.valueOf(EUCLIDEAN_RADAR));
        params.setProperty("AGENTS_START_PACKED", String.valueOf(AGENTS_START_PACKED));
    }

}
