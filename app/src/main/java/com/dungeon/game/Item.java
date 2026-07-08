package com.dungeon.game;

public class Item {
    public String name;
    public char symbol;
    public int x, y;
    public int r, g, b;
    public ItemType type;
    public int value;

    public enum ItemType {
        POTION_HEAL, POTION_STRENGTH, SCROLL_MAP, SCROLL_ENCHANT,
        WEAPON_SWORD, WEAPON_DAGGER, WEAPON_AXE,
        ARMOR_CHAIN, ARMOR_PLATE, ARMOR_LEATHER,
        GOLD, FOOD, KEY
    }

    public Item(String name, char symbol, int x, int y, int r, int g, int b, ItemType type, int value) {
        this.name = name;
        this.symbol = symbol;
        this.x = x; this.y = y;
        this.r = r; this.g = g; this.b = b;
        this.type = type;
        this.value = value;
    }
}
