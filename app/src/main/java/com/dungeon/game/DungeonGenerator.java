package com.dungeon.game;

import java.util.*;

public class DungeonGenerator {
    private static final Random RNG = new Random();

    public static Tile[][] generate(int width, int height, int roomCount) {
        Tile[][] tiles = new Tile[width][height];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                tiles[x][y] = Tile.WALL;

        List<Room> rooms = new ArrayList<>();

        for (int i = 0; i < roomCount * 3; i++) {
            int rw = 4 + RNG.nextInt(6);
            int rh = 4 + RNG.nextInt(6);
            int rx = 1 + RNG.nextInt(width - rw - 2);
            int ry = 1 + RNG.nextInt(height - rh - 2);
            Room room = new Room(rx, ry, rx + rw, ry + rh);

            boolean overlaps = false;
            for (Room other : rooms) {
                if (room.intersects(other)) { overlaps = true; break; }
            }
            if (!overlaps) {
                rooms.add(room);
                carveRoom(tiles, room);
            }
        }

        if (rooms.size() < 2) {
            Room room = new Room(width / 4, height / 4, 3 * width / 4, 3 * height / 4);
            rooms.add(room);
            carveRoom(tiles, room);
        }

        for (int i = 1; i < rooms.size(); i++) {
            Room a = rooms.get(i - 1);
            Room b = rooms.get(i);
            connectRooms(tiles, a.centerX(), a.centerY(), b.centerX(), b.centerY());
        }

        tiles[rooms.get(0).centerX()][rooms.get(0).centerY()] = Tile.ENTRANCE;
        int last = rooms.size() - 1;
        tiles[rooms.get(last).centerX()][rooms.get(last).centerY()] = Tile.STAIRS_DOWN;

        return tiles;
    }

    private static void carveRoom(Tile[][] tiles, Room room) {
        for (int x = room.x1 + 1; x < room.x2; x++)
            for (int y = room.y1 + 1; y < room.y2; y++)
                tiles[x][y] = Tile.FLOOR;
    }

    private static void connectRooms(Tile[][] tiles, int x1, int y1, int x2, int y2) {
        int x = x1, y = y1;
        while (x != x2) {
            if (tiles[x][y] == Tile.WALL) tiles[x][y] = Tile.CORRIDOR;
            else if (tiles[x][y] == Tile.FLOOR) tiles[x][y] = Tile.DOOR;
            x += (x2 > x) ? 1 : -1;
        }
        while (y != y2) {
            if (tiles[x][y] == Tile.WALL) tiles[x][y] = Tile.CORRIDOR;
            else if (tiles[x][y] == Tile.FLOOR) tiles[x][y] = Tile.DOOR;
            y += (y2 > y) ? 1 : -1;
        }
    }

    public static Point findTile(Tile[][] tiles, Tile target) {
        int w = tiles.length, h = tiles[0].length;
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                if (tiles[x][y] == target)
                    return new Point(x, y);
        return new Point(1, 1);
    }

    static class Room {
        int x1, y1, x2, y2;
        Room(int x1, int y1, int x2, int y2) {
            this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
        }
        int centerX() { return (x1 + x2) / 2; }
        int centerY() { return (y1 + y2) / 2; }
        boolean intersects(Room o) {
            return x1 <= o.x2 + 1 && x2 >= o.x1 - 1 && y1 <= o.y2 + 1 && y2 >= o.y1 - 1;
        }
    }

    public static class Point {
        public final int x, y;
        public Point(int x, int y) { this.x = x; this.y = y; }
    }
}
