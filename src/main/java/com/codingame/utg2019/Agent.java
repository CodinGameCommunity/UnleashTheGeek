package com.codingame.utg2019;

import com.codingame.game.Player;
import com.codingame.utg2019.action.Action;

public class Agent {
    private Player owner;
    private int id;
    private boolean dead;
    private int respawnIn;
    private String message;

    private Coord position;
    private Item inventory;
    private Action intent;
    private Coord initialPosition;

    public Agent(int id, Player owner, Coord pos) {
        this.owner = owner;
        this.position = pos;
        this.initialPosition = pos;
        this.inventory = Item.NOTHING;
        this.id = id;
        intent = Action.NO_ACTION;
    }

    public void setMessage(String message) {
        this.message = message;
        if (message != null && message.length() > 48) {
            this.message = message.substring(0, 46) + "...";
        }

    }

    public void setPosition(Coord coord) {
        this.position = coord;
    }

    public Coord getPosition() {
        return position;
    }

    public void setIntent(Action intent) {
        this.intent = intent;
    }

    public void reset() {
        intent = Action.NO_ACTION;
        message = null;
    }

    public Action getIntent() {
        return intent;
    }

    public int getId() {
        return id;
    }

    public Player getOwner() {
        return owner;
    }

    public void receiveOre() {
        inventory = Item.ORE;
    }

    public void factoryReset() {
        reset();
        position = initialPosition;
        inventory = Item.NOTHING;
        dead = false;
    }

    public Item getInventory() {
        return inventory;
    }

    public void setInventory(Item collected) {
        inventory = collected;
    }

    public boolean isDead() {
        return dead;
    }

    public void die() {
        dead = true;
        respawnIn = Config.AGENT_RESPAWN_TIME;
        intent = Action.NO_ACTION;
    }

    public void decrementRespawnTimer() {
        respawnIn--;
    }

    // Robots could respawn at some point during development
    public boolean shouldRespawn() {
        return dead && respawnIn <= 0;
    }

    public String getMessage() {
        return message;
    }

}
