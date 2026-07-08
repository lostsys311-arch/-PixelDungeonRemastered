package com.dungeon.game;

import android.graphics.*;

public class TileAtlas {
    private Bitmap[] tileTextures;
    private Bitmap[] entityTextures;
    private Bitmap[] itemTextures;
    private Bitmap playerSprite;
    private Bitmap playerSpriteFancy;
    private int size;
    private boolean detailed;

    public TileAtlas(int size, boolean detailed) {
        this.size = size;
        this.detailed = detailed;
        tileTextures = new Bitmap[Tile.values().length];
        entityTextures = new Bitmap[16];
        itemTextures = new Bitmap[Item.ItemType.values().length];
        generate();
    }

    private void generate() {
        for (Tile t : Tile.values())
            tileTextures[t.ordinal()] = genTile(t);
        for (int i = 0; i < 16; i++)
            entityTextures[i] = genEntity(i);
        for (Item.ItemType t : Item.ItemType.values())
            itemTextures[t.ordinal()] = genItem(t);
        playerSprite = genPlayer(false);
        if (detailed) playerSpriteFancy = genPlayer(true);
    }

    public Bitmap getTile(Tile t) { return tileTextures[t.ordinal()]; }
    public Bitmap getEntity(int idx) { return entityTextures[Math.abs(idx) % 16]; }
    public Bitmap getItem(Item.ItemType t) { return itemTextures[t.ordinal()]; }
    public Bitmap getPlayerSprite(boolean fancy) { return (fancy && playerSpriteFancy != null) ? playerSpriteFancy : playerSprite; }
    public int getSize() { return size; }

    private Bitmap genTile(Tile tile) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int s = size;

        switch (tile) {
            case WALL:
                if (detailed) {
                    p.setColor(Color.rgb(25, 25, 30));
                    c.drawRect(0, 0, s, s, p);
                    p.setColor(Color.rgb(40, 40, 48));
                    for (int i = 0; i < s; i += s / 3) {
                        c.drawRect(0, i, s, i + 1, p);
                        c.drawRect(i, 0, i + 1, s, p);
                    }
                    p.setColor(Color.rgb(50, 50, 60));
                    c.drawRect(s / 3, 0, s / 3 + 2, s, p);
                    c.drawRect(2 * s / 3, 0, 2 * s / 3 + 2, s, p);
                } else {
                    p.setColor(Color.rgb(30, 30, 35));
                    c.drawRect(0, 0, s, s, p);
                }
                break;
            case FLOOR:
                if (detailed) {
                    p.setColor(Color.rgb(35, 32, 28));
                    c.drawRect(0, 0, s, s, p);
                    p.setColor(Color.rgb(45, 40, 35));
                    p.setStrokeWidth(1);
                    for (int i = 1; i < s; i += s / 3) {
                        c.drawLine(0, i, s, i, p);
                        c.drawLine(i, 0, i, s, p);
                    }
                    p.setColor(Color.rgb(55, 48, 40));
                    c.drawCircle(s * 0.2f, s * 0.2f, 2, p);
                    c.drawCircle(s * 0.7f, s * 0.6f, 1.5f, p);
                } else {
                    p.setColor(Color.rgb(35, 30, 25));
                    c.drawRect(0, 0, s, s, p);
                }
                break;
            case CORRIDOR:
                p.setColor(Color.rgb(30, 28, 25));
                c.drawRect(0, 0, s, s, p);
                break;
            case DOOR:
                if (detailed) {
                    p.setColor(Color.rgb(60, 45, 25));
                    c.drawRect(0, 0, s, s, p);
                    p.setColor(Color.rgb(80, 60, 30));
                    c.drawRect(2, 2, s - 2, s - 2, p);
                    p.setColor(Color.rgb(160, 140, 80));
                    c.drawCircle(s * 0.7f, s * 0.5f, 3, p);
                } else {
                    p.setColor(Color.rgb(80, 60, 30));
                    c.drawRect(0, 0, s, s, p);
                }
                break;
            case STAIRS_DOWN:
                p.setColor(Color.rgb(25, 23, 18));
                c.drawRect(0, 0, s, s, p);
                p.setColor(Color.rgb(180, 160, 40));
                p.setStrokeWidth(2);
                float cx = s / 2f, cy = s / 2f;
                for (int i = 0; i < 4; i++) {
                    c.drawCircle(cx, cy, (s / 2f - 2) * (1f - i * 0.22f), p);
                }
                break;
            case STAIRS_UP:
                p.setColor(Color.rgb(25, 23, 18));
                c.drawRect(0, 0, s, s, p);
                p.setColor(Color.rgb(180, 160, 40));
                p.setStrokeWidth(2);
                cx = s / 2f; cy = s / 2f;
                for (int i = 3; i >= 0; i--) {
                    c.drawCircle(cx, cy, (s / 2f - 2) * (1f - i * 0.22f), p);
                }
                break;
            case WATER:
                if (detailed) {
                    p.setColor(Color.rgb(0, 45, 130));
                    c.drawRect(0, 0, s, s, p);
                    p.setColor(Color.rgb(0, 70, 160));
                    for (int i = 0; i < s; i += 4) {
                        c.drawLine(0, i, s, i - 1, p);
                    }
                    p.setColor(Color.rgb(40, 120, 200));
                    c.drawCircle(s * 0.3f, s * 0.3f, 3, p);
                    c.drawCircle(s * 0.6f, s * 0.7f, 2, p);
                } else {
                    p.setColor(Color.rgb(0, 50, 140));
                    c.drawRect(0, 0, s, s, p);
                }
                break;
            case GRASS:
                if (detailed) {
                    p.setColor(Color.rgb(0, 55, 18));
                    c.drawRect(0, 0, s, s, p);
                    p.setColor(Color.rgb(0, 100, 35));
                    for (int i = 1; i < s; i += 3) {
                        c.drawRect(i, 0, i + 1, s, p);
                        c.drawRect(i + 1, -1, i + 2, s - 1, p);
                    }
                } else {
                    p.setColor(Color.rgb(0, 60, 20));
                    c.drawRect(0, 0, s, s, p);
                }
                break;
            case ENTRANCE:
                p.setColor(Color.rgb(35, 30, 25));
                c.drawRect(0, 0, s, s, p);
                p.setColor(Color.rgb(160, 160, 160));
                p.setStrokeWidth(2);
                c.drawRect(s / 4, s / 4, s * 3 / 4, s * 3 / 4, p);
                p.setColor(Color.rgb(200, 200, 200));
                c.drawCircle(s / 2f, s / 2f, 3, p);
                break;
        }
        return bmp;
    }

    private Bitmap genEntity(int idx) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int s = size;

        int[][] cols = {
            {0xCC, 0x44, 0x44}, {0x44, 0xAA, 0x44}, {0x88, 0x44, 0xCC}, {0xCC, 0xCC, 0x44},
            {0xCC, 0x88, 0x44}, {0x44, 0xCC, 0xCC}, {0xCC, 0x44, 0xCC}, {0x88, 0x88, 0x88},
            {0xDD, 0x66, 0x66}, {0x55, 0xCC, 0x55}, {0x99, 0x55, 0xDD}, {0xDD, 0xDD, 0x55},
            {0xDD, 0x99, 0x55}, {0x55, 0xDD, 0xDD}, {0xDD, 0x55, 0xDD}, {0x99, 0x99, 0x99}
        };
        int[] col = cols[Math.abs(idx) % 16];

        p.setColor(Color.rgb(col[0], col[1], col[2]));

        if (detailed) {
            c.drawCircle(s / 2f, s * 0.25f, s * 0.22f, p);
            c.drawRect(s * 0.2f, s * 0.42f, s * 0.8f, s * 0.75f, p);
            p.setColor(Color.WHITE);
            c.drawCircle(s * 0.38f, s * 0.22f, 2, p);
            c.drawCircle(s * 0.62f, s * 0.22f, 2, p);
            p.setColor(Color.rgb(col[0], col[1], col[2]));
            c.drawRect(s * 0.15f, s * 0.55f, s * 0.3f, s * 0.7f, p);
            c.drawRect(s * 0.7f, s * 0.55f, s * 0.85f, s * 0.7f, p);
        } else {
            c.drawCircle(s / 2f, s / 2f, s * 0.35f, p);
        }
        return bmp;
    }

    private Bitmap genItem(Item.ItemType type) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int s = size;

        switch (type) {
            case POTION_HEAL:
                p.setColor(0xFF44CC44);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                if (detailed) {
                    p.setColor(0x88FFFFFF);
                    c.drawCircle(s * 0.35f, s * 0.45f, 2, p);
                }
                break;
            case POTION_STRENGTH:
                p.setColor(0xFFFF4444);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_SPEED:
                p.setColor(0xFF44CCFF);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_INVISIBILITY:
                p.setColor(0xFFCCCCDD);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0x88FFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_FIRE:
                p.setColor(0xFFFF6600);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_POISON:
                p.setColor(0xFF8800AA);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_SHIELD:
                p.setColor(0xFF4488FF);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case POTION_MANA:
                p.setColor(0xFFAA44FF);
                c.drawOval(new RectF(s * 0.22f, s * 0.18f, s * 0.78f, s * 0.82f), p);
                p.setColor(0xAAFFFFFF);
                c.drawRect(s * 0.35f, s * 0.08f, s * 0.65f, s * 0.22f, p);
                break;
            case SCROLL_MAP:
                p.setColor(0xFFDDCC88);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFF444444);
                c.drawLine(s * 0.32f, s * 0.32f, s * 0.68f, s * 0.32f, p);
                c.drawLine(s * 0.32f, s * 0.45f, s * 0.68f, s * 0.45f, p);
                c.drawLine(s * 0.32f, s * 0.58f, s * 0.55f, s * 0.58f, p);
                break;
            case SCROLL_ENCHANT:
                p.setColor(0xFFCC88DD);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFFFFDD00);
                c.drawCircle(s / 2f, s / 2f, s * 0.12f, p);
                break;
            case SCROLL_IDENTIFY:
                p.setColor(0xFF88CCAA);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFF336633);
                c.drawCircle(s / 2f, s / 2f, s * 0.1f, p);
                break;
            case SCROLL_TELEPORT:
                p.setColor(0xFF88AADD);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFF4488CC);
                c.drawCircle(s / 2f, s / 2f, s * 0.1f, p);
                break;
            case SCROLL_REMOVE_CURSE:
                p.setColor(0xFFFF88AA);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFFCC2244);
                c.drawLine(s * 0.3f, s * 0.3f, s * 0.7f, s * 0.7f, p);
                c.drawLine(s * 0.7f, s * 0.3f, s * 0.3f, s * 0.7f, p);
                break;
            case SCROLL_LIGHTNING:
                p.setColor(0xFFFFFF88);
                c.drawRoundRect(new RectF(s * 0.18f, s * 0.12f, s * 0.82f, s * 0.88f), 3, 3, p);
                p.setColor(0xFFFFCC00);
                Path path = new Path();
                path.moveTo(s * 0.4f, s * 0.2f);
                path.lineTo(s * 0.55f, s * 0.4f);
                path.lineTo(s * 0.4f, s * 0.45f);
                path.lineTo(s * 0.6f, s * 0.8f);
                p.setStrokeWidth(2);
                p.setStyle(Paint.Style.STROKE);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
                break;
            case WEAPON_DAGGER:
                p.setColor(0xFFCCCCCC);
                path = new Path();
                path.moveTo(s / 2f, s * 0.08f);
                path.lineTo(s * 0.62f, s * 0.6f);
                path.lineTo(s * 0.38f, s * 0.6f);
                path.close();
                c.drawPath(path, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.38f, s * 0.6f, s * 0.62f, s * 0.78f, p);
                break;
            case WEAPON_SWORD:
                p.setColor(0xFFCCCCCC);
                c.drawRect(s * 0.42f, s * 0.08f, s * 0.58f, s * 0.65f, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.35f, s * 0.65f, s * 0.65f, s * 0.82f, p);
                break;
            case WEAPON_AXE:
                p.setColor(0xFFAAAAAA);
                c.drawRect(s * 0.45f, s * 0.12f, s * 0.55f, s * 0.7f, p);
                p.setColor(0xFFCCCCCC);
                c.drawArc(new RectF(s * 0.12f, s * 0.12f, s * 0.88f, s * 0.52f), 225, 90, true, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.38f, s * 0.7f, s * 0.62f, s * 0.85f, p);
                break;
            case WEAPON_SPEAR:
                p.setColor(0xFFDDDDDD);
                path = new Path();
                path.moveTo(s / 2f, s * 0.05f);
                path.lineTo(s * 0.55f, s * 0.35f);
                path.lineTo(s * 0.45f, s * 0.35f);
                path.close();
                c.drawPath(path, p);
                p.setColor(0xFF886644);
                c.drawRect(s * 0.45f, s * 0.35f, s * 0.55f, s * 0.85f, p);
                break;
            case WEAPON_MACE:
                p.setColor(0xFF888888);
                c.drawRect(s * 0.46f, s * 0.5f, s * 0.54f, s * 0.85f, p);
                p.setColor(0xFFAAAAAA);
                c.drawCircle(s / 2f, s * 0.25f, s * 0.25f, p);
                p.setColor(0xFF884422);
                c.drawRect(s * 0.38f, s * 0.85f, s * 0.62f, s * 0.95f, p);
                break;
            case WEAPON_BOW:
                p.setColor(0xFF886644);
                Path bow = new Path();
                bow.moveTo(s * 0.3f, s * 0.15f);
                bow.quadTo(s * 0.5f, s * 0.1f, s * 0.7f, s * 0.15f);
                bow.quadTo(s * 0.65f, s * 0.5f, s * 0.7f, s * 0.85f);
                bow.quadTo(s * 0.5f, s * 0.9f, s * 0.3f, s * 0.85f);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(2);
                c.drawPath(bow, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFCCBBAA);
                c.drawLine(s * 0.3f, s * 0.15f, s * 0.7f, s * 0.85f, p);
                break;
            case WEAPON_STAFF:
                p.setColor(0xFF886644);
                c.drawRect(s * 0.45f, s * 0.1f, s * 0.55f, s * 0.85f, p);
                p.setColor(0xFF44AAFF);
                c.drawCircle(s / 2f, s * 0.12f, s * 0.15f, p);
                break;
            case ARMOR_LEATHER:
                p.setColor(0xFF885533);
                c.drawRect(s * 0.2f, s * 0.22f, s * 0.8f, s * 0.78f, p);
                p.setColor(0xFF664422);
                c.drawLine(s * 0.4f, s * 0.3f, s * 0.4f, s * 0.7f, p);
                c.drawLine(s * 0.6f, s * 0.3f, s * 0.6f, s * 0.7f, p);
                break;
            case ARMOR_CHAIN:
                p.setColor(0xFF888888);
                c.drawRect(s * 0.2f, s * 0.22f, s * 0.8f, s * 0.78f, p);
                p.setColor(0xFFAAAAAA);
                for (float i = 0.3f; i < 0.8f; i += 0.12f)
                    c.drawCircle(s * i, s * 0.5f, 2, p);
                break;
            case ARMOR_PLATE:
                p.setColor(0xFF9999BB);
                c.drawRect(s * 0.15f, s * 0.18f, s * 0.85f, s * 0.82f, p);
                p.setColor(0xFF777799);
                c.drawRect(s * 0.3f, s * 0.12f, s * 0.7f, s * 0.22f, p);
                break;
            case ARMOR_MAGIC_ROBE:
                p.setColor(0xFF6644AA);
                c.drawRect(s * 0.18f, s * 0.2f, s * 0.82f, s * 0.8f, p);
                p.setColor(0xFF8866CC);
                c.drawCircle(s / 2f, s * 0.35f, 3, p);
                c.drawCircle(s * 0.3f, s * 0.5f, 2, p);
                c.drawCircle(s * 0.7f, s * 0.5f, 2, p);
                break;
            case GOLD:
                p.setColor(0xFFFFDD00);
                c.drawCircle(s / 2f, s / 2f, s * 0.3f, p);
                p.setColor(0xFFFF8800);
                c.drawCircle(s / 2f, s / 2f, s * 0.2f, p);
                if (detailed) {
                    p.setColor(0xFFFFFFFF);
                    c.drawCircle(s * 0.42f, s * 0.42f, 1.5f, p);
                }
                break;
            case FOOD:
                p.setColor(0xFFCC8844);
                c.drawRoundRect(new RectF(s * 0.2f, s * 0.28f, s * 0.8f, s * 0.72f), 5, 5, p);
                p.setColor(0xFFAA6633);
                c.drawRect(s * 0.45f, s * 0.72f, s * 0.55f, s * 0.85f, p);
                break;
            case KEY:
                p.setColor(0xFFFFDD00);
                c.drawCircle(s * 0.3f, s * 0.35f, s * 0.18f, p);
                c.drawRect(s * 0.42f, s * 0.3f, s * 0.75f, s * 0.4f, p);
                c.drawRect(s * 0.55f, s * 0.4f, s * 0.65f, s * 0.7f, p);
                break;
            case BOMB:
                p.setColor(0xFF444444);
                c.drawCircle(s / 2f, s / 2f, s * 0.3f, p);
                p.setColor(0xFFFF6600);
                c.drawRect(s * 0.4f, s * 0.05f, s * 0.6f, s * 0.2f, p);
                if (detailed) {
                    p.setColor(0xFFFFAA00);
                    c.drawLine(s * 0.45f, s * 0.05f, s * 0.45f, s * 0.15f, p);
                    c.drawLine(s * 0.55f, s * 0.05f, s * 0.55f, s * 0.15f, p);
                }
                break;
            case RING:
                p.setColor(0xFFFFDD00);
                p.setStrokeWidth(3);
                p.setStyle(Paint.Style.STROKE);
                c.drawCircle(s / 2f, s / 2f, s * 0.3f, p);
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFFF8800);
                c.drawCircle(s / 2f, s / 2f, 3, p);
                break;
        }
        return bmp;
    }

    private Bitmap genPlayer(boolean fancy) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int s = size;

        p.setColor(0xFFFFDD44);
        c.drawCircle(s / 2f, s * 0.28f, s * 0.22f, p);
        p.setColor(0xFF4488FF);
        c.drawRect(s * 0.2f, s * 0.45f, s * 0.8f, s * 0.75f, p);

        if (fancy) {
            p.setColor(Color.WHITE);
            c.drawCircle(s * 0.38f, s * 0.25f, 2, p);
            c.drawCircle(s * 0.62f, s * 0.25f, 2, p);
            p.setColor(0xFF4488FF);
            c.drawRect(s * 0.12f, s * 0.55f, s * 0.28f, s * 0.7f, p);
            c.drawRect(s * 0.72f, s * 0.55f, s * 0.88f, s * 0.7f, p);
            p.setColor(0xFF3355AA);
            c.drawRect(s * 0.2f, s * 0.75f, s * 0.8f, s * 0.78f, p);
            p.setColor(0xFFFFDD44);
            c.drawCircle(s / 2f, s * 0.08f, 3, p);
        } else {
            p.setColor(0xFF4488FF);
            c.drawRect(s * 0.15f, s * 0.55f, s * 0.3f, s * 0.7f, p);
            c.drawRect(s * 0.7f, s * 0.55f, s * 0.85f, s * 0.7f, p);
        }
        return bmp;
    }
}
