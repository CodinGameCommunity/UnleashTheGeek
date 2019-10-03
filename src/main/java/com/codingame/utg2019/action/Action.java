package com.codingame.utg2019.action;

import com.codingame.utg2019.Coord;
import com.codingame.utg2019.Item;

public interface Action {
    Action NO_ACTION = new Action() {

        @Override
        public boolean isMove() {
            return false;
        }

        @Override
        public boolean isDig() {
            return false;
        }

        @Override
        public Coord getTarget() {
            return null;
        }

        @Override
        public boolean isRequest() {
            return false;
        }

        @Override
        public Item getItem() {
            return Item.NOTHING;
        }

    };

    public Coord getTarget();

    public boolean isMove();

    public boolean isDig();

    public boolean isRequest();

    public Item getItem();
}
