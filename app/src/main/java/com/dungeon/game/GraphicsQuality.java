package com.dungeon.game;

public enum GraphicsQuality {
    POTATO(10, false, false),
    LOW(11, true, false),
    MID(12, true, false),
    HIGH(13, true, true),
    FANCY(14, true, true);

    public final int viewTilesX;
    public final boolean smoothLighting;
    public final boolean detailedSprites;

    GraphicsQuality(int viewTilesX, boolean smoothLighting, boolean detailedSprites) {
        this.viewTilesX = viewTilesX;
        this.smoothLighting = smoothLighting;
        this.detailedSprites = detailedSprites;
    }
}
