package com.codingame.utg2019;

public enum Item {
    TRAP(Config.TYPE_TRAP), RADAR(Config.TYPE_RADAR), ORE(Config.TYPE_ORE), NOTHING(Config.TYPE_NONE);

    private int typeId;

    private Item(int typeId) {
        this.typeId = typeId;
    }
    public int getTypeId() {
        return typeId;
    }

}
