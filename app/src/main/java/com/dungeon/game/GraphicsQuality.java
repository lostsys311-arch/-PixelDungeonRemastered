package com.dungeon.game;

public enum GraphicsQuality {
    POTATO(10, 12, false, false),
    LOW(14, 16, true, false),
    MID(18, 20, true, false),
    HIGH(22, 24, true, true),
    FANCY(28, 32, true, true);

    public final int viewTilesX;
    public final int tileSize;
    public final boolean smoothLighting;
    public final boolean detailedSprites;

    GraphicsQuality(int viewTilesX, int tileSize, boolean smoothLighting, boolean detailedSprites) {
        this.viewTilesX = viewTilesX;
        this.tileSize = tileSize;
        this.smoothLighting = smoothLighting;
        this.detailedSprites = detailedSprites;
    }

    public int viewTilesY(int screenW, int screenH) {
        return (int)((float)viewTilesX * screenH / screenW);
    }
}
