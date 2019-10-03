package com.codingame.utg2019;

public class ItemCoord extends Coord {
    static int INSTANCE_COUNT = 0; 
	
    private int id;

    public ItemCoord(int x, int y) {
        super(x, y);
        this.id = INSTANCE_COUNT++ + Config.AGENTS_PER_PLAYER * 2;
    }

    public int getId() {
    	return id;
    }
}
