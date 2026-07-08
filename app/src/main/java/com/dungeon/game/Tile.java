package com.dungeon.game;

public enum Tile {
    WALL('#', false, 0, 0, 0),
    FLOOR('.', true, 40, 40, 40),
    CORRIDOR('#', true, 50, 50, 50),
    DOOR('+', true, 139, 90, 43),
    STAIRS_DOWN('>', true, 255, 255, 0),
    STAIRS_UP('<', true, 255, 255, 0),
    WATER('~', true, 0, 100, 200),
    GRASS('"', true, 0, 150, 0),
    ENTRANCE('F', true, 255, 255, 255);

    public final char symbol;
    public final boolean walkable;
    public final int r, g, b;

    Tile(char symbol, boolean walkable, int r, int g, int b) {
        this.symbol = symbol;
        this.walkable = walkable;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
