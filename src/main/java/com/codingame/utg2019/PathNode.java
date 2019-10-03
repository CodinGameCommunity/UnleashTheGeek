package com.codingame.utg2019;

public class PathNode {
    Coord coord;
    PathNode previous;
    int steps;

    public PathNode(Coord coord, PathNode previous) {
        this.coord = coord;
        this.previous = previous;
        this.steps = previous.steps + 1;
    }

    public PathNode(Coord coord) {
        this.coord = coord;
    }

    public boolean isStart() {
        return previous == null;
    }

}
