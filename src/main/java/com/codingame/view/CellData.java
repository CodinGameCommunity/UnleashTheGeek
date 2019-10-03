package com.codingame.view;

import com.codingame.utg2019.Coord;

public class CellData {
    int x;
    int y;
    int ore;
    
    public CellData(Coord coord, int ore) {
        x = coord.getX();
        y = coord.getY();
        this.ore = ore;
    }
    
}
