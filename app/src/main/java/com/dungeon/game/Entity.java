package com.dungeon.game;

import java.util.Random;

public class Entity {
    private static final Random RNG = new Random();

    public String name;
    public char symbol;
    public int x, y;
    public int hp, maxHp;
    public int attack, defense;
    public int visionRange;
    public int r, g, b;
    public boolean isPlayer;

    public Entity(String name, char symbol, int x, int y, int hp, int attack, int defense, int r, int g, int b) {
        this.name = name;
        this.symbol = symbol;
        this.x = x; this.y = y;
        this.hp = hp; this.maxHp = hp;
        this.attack = attack; this.defense = defense;
        this.visionRange = 6;
        this.r = r; this.g = g; this.b = b;
        this.isPlayer = false;
    }

    public boolean isAlive() { return hp > 0; }

    public int damage(int amount) {
        int actual = Math.max(0, amount - defense);
        hp -= actual;
        return actual;
    }

    public int attack(Entity other) {
        int roll = RNG.nextInt(attack) + 1;
        return other.damage(roll);
    }

    public boolean canSee(int tx, int ty, Tile[][] tiles) {
        int dist = Math.abs(tx - x) + Math.abs(ty - y);
        if (dist > visionRange) return false;
        return hasLineOfSight(x, y, tx, ty, tiles);
    }

    public static boolean hasLineOfSight(int x0, int y0, int x1, int y1, Tile[][] tiles) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (cx != x1 || cy != y1) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx) { err += dx; cy += sy; }
            if ((cx != x1 || cy != y1) && !tiles[cx][cy].walkable) return false;
        }
        return true;
    }
}
