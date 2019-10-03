package com.codingame.game;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;
import com.codingame.utg2019.Agent;
import com.codingame.utg2019.Config;
import com.codingame.utg2019.Item;

public class Player extends AbstractMultiplayerPlayer {

    private List<Agent> agents = new ArrayList<>();
    private int ore;
    private Map<Item, Integer> cooldowns;
    private Map<Item, Integer> cooldownTimes;

    public Player() {
        agents = new ArrayList<>(Config.AGENTS_PER_PLAYER);
        cooldowns = new EnumMap<>(Item.class);
        cooldowns.put(Item.RADAR, 0);
        cooldowns.put(Item.TRAP, 0);
        cooldownTimes = new EnumMap<>(Item.class);
        cooldownTimes.put(Item.RADAR, Config.RADAR_COOLDOWN);
        cooldownTimes.put(Item.TRAP, Config.TRAP_COOLDOWN);
    }

    public void addAgent(Agent agent) {
        agents.add(agent);

    }

    public void decrementCooldowns() {
        cooldowns.forEach((k, v) -> {
            cooldowns.compute(k, (item, cooldown) -> {
                return (cooldown > 0) ? cooldown - 1 : cooldown;
            });
        });
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public int getCooldown(Item item) {
        return Objects.requireNonNull(
            cooldowns.get(item)
        );
    }

    @Override
    public int getExpectedOutputLines() {
        return agents.size();
    }

    public int getOre() {
        return ore;
    }

    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public void reset() {
        agents.forEach(a -> a.reset());
    }

    public void scoreOre() {
        ore++;
        setScore(ore);
    }

    public void startCooldown(Item item) {
        cooldowns.put(item, cooldownTimes.get(item));
    }

    public void setCooldown(Item item, int cooldown) {
        cooldowns.put(item, cooldown);
        
    }

    public void setOre(int score) {
        ore = score;
        setScore(score);
    }
}
