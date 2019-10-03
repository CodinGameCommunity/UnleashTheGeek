package com.codingame.utg2019;

public class Coord {
    protected final int x;
    protected final int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double euclideanTo(int x, int y) {
        return Math.sqrt(sqrEuclideanTo(x, y));
    }

    private double sqrEuclideanTo(int x, int y) {
        return Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        Coord other = (Coord) obj;
        if (x != other.x) return false;
        if (y != other.y) return false;
        return true;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int manhattanTo(Coord other) {
        return manhattanTo(other.x, other.y);
    }

    public int gameDistanceTo(int x, int y) {
        return Config.ADJACENCY == Config.FOUR_ADJACENCY ? manhattanTo(x, y) : chebyshevTo(x, y);
    }

    public int chebyshevTo(int x, int y) {
        return Math.max(Math.abs(x - this.x), Math.abs(y - this.y));
    }

    public int manhattanTo(int x, int y) {
        return Math.abs(x - this.x) + Math.abs(y - this.y);
    }

    public int gameDistanceTo(Coord coord) {
        return gameDistanceTo(coord.x, coord.y);
    }

}
