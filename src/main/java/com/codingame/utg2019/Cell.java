package com.codingame.utg2019;

public class Cell {
    public static final Cell NO_CELL = new Cell() {
        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public int getOre() {
            return 0;
        }
    };

    private boolean accessToHQ;
    private boolean hole;
    private int ore;

    public boolean isValid() {
        return true;
    }

    public int getOre() {
        return ore;
    }

    public Cell() {

    } 

    public void setAccessToHQ(boolean acecssToHQ) {
        this.accessToHQ = acecssToHQ;

    }

    public void setOre(int ore) {
        this.ore = ore;
    }

    public void setHole(boolean hole) {
        this.hole = hole;

    }

    public void reduceOre(int amount) {
        ore = Math.max(0, ore - amount);
    }

    public void incrementOre() {
        ore++;
    }

    public boolean hasAccessToHQ() {
        return accessToHQ;
    }

    public boolean isHole() {
        return hole;
    }

}
