package com.codingame.utg2019.action;

import com.codingame.utg2019.Coord;
import com.codingame.utg2019.Item;

public class DigAction implements Action {

    private Coord target;

    public Coord getTarget() {
        return target;
    }

    public DigAction(Coord target) {
        this.target = target;
    }

    @Override
    public boolean isMove() {
        return false;
    }

    @Override
    public boolean isDig() {
        return true;
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
