package com.codingame.view;

public class EventData {
    public static final int REQUEST = 0;
    public static final int RADAR_DESTRUCTION = 1;
    public static final int BURY = 2;
    public static final int GET_ORE = 3;
    public static final int GIVE_ORE = 4;
    public static final int EXPLOSION = 5;
    public static final int AGENT_DEATH = 6;
    public static final int RESPAWN = 7;

    public Integer type, item, agent, x, y;
}
