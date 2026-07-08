package com.dungeon.game;

import android.graphics.*;

public class TileAtlas {
    private Bitmap[] tileTextures;
    private Bitmap[] entityTextures;
    private Bitmap[] itemTextures;
    private int size;
    private boolean detailed;

    public TileAtlas(int size, boolean detailed) {
        this.size = size;
        this.detailed = detailed;
        tileTextures = new Bitmap[Tile.values().length];
        entityTextures = new Bitmap[8];
        itemTextures = new Bitmap[Item.ItemType.values().length];
        generate();
    }

    private void generate() {
        for (Tile t : Tile.values())
            tileTextures[t.ordinal()] = genTile(t);
        for (int i = 0; i < 8; i++)
            entityTextures[i] = genEntity(i);
        for (Item.ItemType t : Item.ItemType.values())
            itemTextures[t.ordinal()] = genItem(t);
    }

    public Bitmap getTile(Tile t) { return tileTextures[t.ordinal()]; }
    public Bitmap getEntity(int idx) { return entityTextures[idx % 8]; }
    public Bitmap getItem(Item.ItemType t) { return itemTextures[t.ordinal()]; }
    public int getSize() { return size; }

    private Bitmap genTile(Tile tile) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        int s = size;

        switch (tile) {
            case WALL:
                c.drawColor(Color.rgb(30, 30, 35));
                if (detailed) {
                    Paint p = new Paint();
                    for (int i = 0; i < s; i += 4) {
                        p.setColor((i / 4) % 2 == 0 ? Color.rgb(45, 45, 50) : Color.rgb(35, 35, 40));
                        c.drawRect(i, 0, i + 4, s / 2, p);
                    }
                    p.setColor(Color.rgb(55, 55, 60));
                    c.drawRect(0, s / 2 - 1, s, s / 2 + 1, p);
                }
                break;
            case FLOOR:
                c.drawColor(Color.rgb(35, 30, 25));
                if (detailed) {
                    Paint p = new Paint();
                    p.setColor(Color.rgb(50, 45, 35));
                    p.setStrokeWidth(1);
                    for (int i = 2; i < s; i += 4) {
                        c.drawLine(0, i, s, i, p);
                        c.drawLine(i, 0, i, s, p);
                    }
                }
                break;
            case CORRIDOR:
                c.drawColor(Color.rgb(30, 28, 25));
                break;
            case DOOR:
                c.drawColor(Color.rgb(80, 60, 30));
                if (detailed) {
                    Paint p = new Paint();
                    p.setColor(Color.rgb(60, 40, 20));
                    c.drawRect(s / 4, 0, s * 3 / 4, s, p);
                    p.setColor(Color.rgb(180, 160, 80));
                    c.drawCircle(s * 3 / 4 - 2, s / 2, 2, p);
                }
                break;
            case STAIRS_DOWN:
                c.drawColor(Color.rgb(30, 28, 20));
                Paint p = new Paint();
                p.setColor(Color.rgb(200, 180, 50));
                p.setStrokeWidth(2);
                float cx = s / 2f, cy = s / 2f;
                for (int i = 0; i < 4; i++) {
                    float r = (s / 2f) * (1f - i * 0.2f);
                    c.drawCircle(cx, cy, r, p);
                }
                break;
            case STAIRS_UP:
                c.drawColor(Color.rgb(30, 28, 20));
                p = new Paint();
                p.setColor(Color.rgb(200, 180, 50));
                p.setStrokeWidth(2);
                cx = s / 2f; cy = s / 2f;
                for (int i = 3; i >= 0; i--) {
                    float r = (s / 2f) * (1f - i * 0.2f);
                    c.drawCircle(cx, cy, r, p);
                }
                break;
            case WATER:
                c.drawColor(Color.rgb(0, 50, 140));
                if (detailed) {
                    p = new Paint();
                    p.setColor(Color.rgb(0, 80, 180));
                    p.setStrokeWidth(1);
                    for (int i = 2; i < s; i += 5)
                        c.drawLine(0, i, s, i, p);
                }
                break;
            case GRASS:
                c.drawColor(Color.rgb(0, 60, 20));
                if (detailed) {
                    p = new Paint();
                    p.setColor(Color.rgb(0, 120, 40));
                    for (int i = 0; i < s; i += 3)
                        c.drawRect(i, 0, i + 1, s, p);
                }
                break;
            case ENTRANCE:
                c.drawColor(Color.rgb(40, 35, 30));
                p = new Paint();
                p.setColor(Color.rgb(180, 180, 180));
                p.setStrokeWidth(2);
                c.drawRect(s / 4, s / 4, s * 3 / 4, s * 3 / 4, p);
                break;
        }
        return bmp;
    }

    private Bitmap genEntity(int idx) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        int s = size;

        int[] colors = {
            0xFFCC4444, 0xFF44AA44, 0xFF8844CC, 0xFFCCCC44,
            0xFFCC8844, 0xFF44CCCC, 0xFFCC44CC, 0xFF888888
        };
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(colors[idx % colors.length]);

        if (detailed) {
            c.drawCircle(s / 2f, s * 0.3f, s * 0.25f, p);
            c.drawRect(s * 0.2f, s * 0.45f, s * 0.8f, s * 0.85f, p);
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            c.drawCircle(s * 0.38f, s * 0.27f, 2, p);
            c.drawCircle(s * 0.62f, s * 0.27f, 2, p);
        } else {
            c.drawCircle(s / 2f, s / 2f, s * 0.35f, p);
        }
        return bmp;
    }

    private Bitmap genItem(Item.ItemType type) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        int s = size;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        switch (type) {
            case POTION_HEAL:
                p.setColor(0xFF00FF00);
                c.drawOval(new RectF(s * 0.25f, s * 0.2f, s * 0.75f, s * 0.8f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.1f, s * 0.65f, s * 0.25f, p);
                break;
            case POTION_STRENGTH:
                p.setColor(0xFFFF4444);
                c.drawOval(new RectF(s * 0.25f, s * 0.2f, s * 0.75f, s * 0.8f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.1f, s * 0.65f, s * 0.25f, p);
                break;
            case SCROLL_MAP:
                p.setColor(0xFFDDCC88);
                c.drawRoundRect(new RectF(s * 0.2f, s * 0.15f, s * 0.8f, s * 0.85f), 3, 3, p);
                p.setColor(0xFF444444);
                c.drawLine(s * 0.35f, s * 0.35f, s * 0.65f, s * 0.35f, p);
                c.drawLine(s * 0.35f, s * 0.5f, s * 0.65f, s * 0.5f, p);
                c.drawLine(s * 0.35f, s * 0.65f, s * 0.55f, s * 0.65f, p);
                break;
            case SCROLL_ENCHANT:
                p.setColor(0xFFCC88DD);
                c.drawRoundRect(new RectF(s * 0.2f, s * 0.15f, s * 0.8f, s * 0.85f), 3, 3, p);
                p.setColor(0xFFFFDD00);
                c.drawCircle(s / 2f, s / 2f, s * 0.15f, p);
                break;
            case WEAPON_SWORD:
                p.setColor(0xFFCCCCCC);
                c.drawRect(s * 0.42f, s * 0.1f, s * 0.58f, s * 0.7f, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.35f, s * 0.7f, s * 0.65f, s * 0.85f, p);
                break;
            case WEAPON_DAGGER:
                p.setColor(0xFFBBBBBB);
                Path path = new Path();
                path.moveTo(s / 2f, s * 0.1f);
                path.lineTo(s * 0.6f, s * 0.65f);
                path.lineTo(s * 0.4f, s * 0.65f);
                path.close();
                c.drawPath(path, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.38f, s * 0.65f, s * 0.62f, s * 0.8f, p);
                break;
            case WEAPON_AXE:
                p.setColor(0xFFAAAAAA);
                c.drawRect(s * 0.45f, s * 0.15f, s * 0.55f, s * 0.75f, p);
                p.setColor(0xFFCCCCCC);
                c.drawArc(new RectF(s * 0.15f, s * 0.15f, s * 0.85f, s * 0.55f), 225, 90, true, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.38f, s * 0.75f, s * 0.62f, s * 0.9f, p);
                break;
            case ARMOR_LEATHER:
                p.setColor(0xFF885533);
                c.drawRect(s * 0.2f, s * 0.25f, s * 0.8f, s * 0.8f, p);
                p.setColor(0xFF664422);
                c.drawLine(s * 0.4f, s * 0.3f, s * 0.4f, s * 0.75f, p);
                c.drawLine(s * 0.6f, s * 0.3f, s * 0.6f, s * 0.75f, p);
                break;
            case ARMOR_CHAIN:
                p.setColor(0xFF888888);
                c.drawRect(s * 0.2f, s * 0.25f, s * 0.8f, s * 0.8f, p);
                p.setColor(0xFFAAAAAA);
                for (float i = 0.25f; i < 0.8f; i += 0.12f)
                    c.drawCircle(s * i, s * 0.5f, 2, p);
                break;
            case ARMOR_PLATE:
                p.setColor(0xFF9999BB);
                c.drawRect(s * 0.15f, s * 0.2f, s * 0.85f, s * 0.85f, p);
                p.setColor(0xFF777799);
                c.drawRect(s * 0.3f, s * 0.15f, s * 0.7f, s * 0.25f, p);
                break;
            case GOLD:
                p.setColor(0xFFFFDD00);
                c.drawCircle(s / 2f, s / 2f, s * 0.3f, p);
                p.setColor(0xFFFF8800);
                c.drawCircle(s / 2f, s / 2f, s * 0.25f, p);
                break;
            case FOOD:
                p.setColor(0xFFCC8844);
                c.drawRoundRect(new RectF(s * 0.2f, s * 0.3f, s * 0.8f, s * 0.7f), 5, 5, p);
                p.setColor(0xFFAA6633);
                c.drawRect(s * 0.45f, s * 0.7f, s * 0.55f, s * 0.85f, p);
                break;
            case KEY:
                p.setColor(0xFFFFDD00);
                c.drawCircle(s * 0.3f, s * 0.4f, s * 0.18f, p);
                c.drawRect(s * 0.4f, s * 0.35f, s * 0.75f, s * 0.45f, p);
                c.drawRect(s * 0.55f, s * 0.45f, s * 0.65f, s * 0.75f, p);
                break;
        }
        return bmp;
    }

    public Bitmap getPlayerSprite() {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        int s = size;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(0xFFFFDD44);
        c.drawCircle(s / 2f, s * 0.3f, s * 0.25f, p);
        p.setColor(0xFF4488FF);
        c.drawRect(s * 0.2f, s * 0.45f, s * 0.8f, s * 0.85f, p);
        if (detailed) {
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            c.drawCircle(s * 0.38f, s * 0.27f, 2, p);
            c.drawCircle(s * 0.62f, s * 0.27f, 2, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF4488FF);
            c.drawRect(s * 0.15f, s * 0.55f, s * 0.3f, s * 0.75f, p);
            c.drawRect(s * 0.7f, s * 0.55f, s * 0.85f, s * 0.75f, p);
        }
        return bmp;
    }
}
