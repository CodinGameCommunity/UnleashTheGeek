package com.codingame.utg2019.action;

import java.util.List;

import com.codingame.utg2019.Coord;
import com.codingame.utg2019.Item;

public class RequestAction implements Action {

    private Item item;

    public RequestAction(Item item) {
        this.item = item;
    }
    
    @Override
    public boolean isMove() {
        return false;
    }

    @Override
    public boolean isDig() {
        return false;
    }

    @Override
    public boolean isRequest() {
        return true;
    }

    @Override
    public Coord getTarget() {
        return null;
    }
    @Override
    public Item getItem() {
        return item;
    }

}
