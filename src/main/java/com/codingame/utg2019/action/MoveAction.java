package com.codingame.utg2019.action;

import java.util.List;

import com.codingame.utg2019.Coord;
import com.codingame.utg2019.Item;

public class MoveAction implements Action {

    private Coord destination;
    private List<Coord> path;

    public Coord getTarget() {
        return destination;
    }

    public MoveAction(Coord destination) {
        this.destination = destination;
    }

    public void setPath(List<Coord> path) {
        this.path = path;
    }

    @Override
    public boolean isMove() {
        return true;
    }

    @Override
    public boolean isDig() {
        return false;
    }

    @Override
    public boolean isRequest() {
        return false;
    }

    @Override
    public Item getItem() {
        return Item.NOTHING;
    }

}
