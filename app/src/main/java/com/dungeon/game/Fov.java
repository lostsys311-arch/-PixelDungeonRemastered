package com.dungeon.game;

public class Fov {
    public static boolean[][] calculate(int px, int py, int radius, Tile[][] tiles) {
        int w = tiles.length, h = tiles[0].length;
        boolean[][] visible = new boolean[w][h];
        visible[px][py] = true;
        for (int octant = 0; octant < 8; octant++)
            castLight(px, py, radius, tiles, visible, 1, 1.0, 0.0, 0, octant, w, h);
        return visible;
    }

    private static void castLight(int cx, int cy, int radius, Tile[][] tiles,
                                   boolean[][] visible, int row, double startSlope,
                                   double endSlope, int col, int octant, int w, int h) {
        if (startSlope < endSlope) return;
        boolean blocked = false;
        double d = row;

        while (d <= radius) {
            double minSlope = (col - 0.5) / (d + 0.5);
            double maxSlope = (col + 0.5) / (d - 0.5);
            if (startSlope < maxSlope) maxSlope = startSlope;
            if (endSlope > minSlope) minSlope = endSlope;
            int top = (int)Math.floor(d * maxSlope);
            int bot = (int)Math.ceil(d * minSlope);

            if (bot <= top) {
                for (int i = bot; i <= top; i++) {
                    int x = cx, y = cy;
                    switch (octant) {
                        case 0: x = cx + col; y = cy - i; break;
                        case 1: x = cx + i; y = cy - col; break;
                        case 2: x = cx + i; y = cy + col; break;
                        case 3: x = cx + col; y = cy + i; break;
                        case 4: x = cx - col; y = cy + i; break;
                        case 5: x = cx - i; y = cy + col; break;
                        case 6: x = cx - i; y = cy - col; break;
                        case 7: x = cx - col; y = cy - i; break;
                    }
                    if (x < 0 || x >= w || y < 0 || y >= h) continue;
                    visible[x][y] = true;
                    if (!tiles[x][y].walkable) blocked = true;
                }
            }
            if (blocked) {
                col++; d++;
                if (d <= radius)
                    castLight(cx, cy, radius, tiles, visible, (int)d + 1,
                        startSlope, (col - 0.5) / (d - 0.5), col, octant, w, h);
                return;
            } else {
                col++; d++;
            }
        }
    }
}
