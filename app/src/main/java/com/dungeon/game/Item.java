package com.dungeon.game;

public class Item {
    public String name;
    public char symbol;
    public int x, y;
    public int r, g, b;
    public ItemType type;
    public int value;
    public int price;

    public enum ItemType {
        POTION_HEAL, POTION_STRENGTH, POTION_SPEED, POTION_INVISIBILITY,
        POTION_FIRE, POTION_POISON, POTION_SHIELD, POTION_MANA,
        SCROLL_MAP, SCROLL_ENCHANT, SCROLL_IDENTIFY, SCROLL_TELEPORT,
        SCROLL_REMOVE_CURSE, SCROLL_LIGHTNING,
        WEAPON_DAGGER, WEAPON_SWORD, WEAPON_AXE, WEAPON_SPEAR, WEAPON_MACE, WEAPON_BOW, WEAPON_STAFF,
        ARMOR_LEATHER, ARMOR_CHAIN, ARMOR_PLATE, ARMOR_MAGIC_ROBE,
        GOLD, FOOD, KEY, BOMB, RING
    }

    public Item(String name, char symbol, int x, int y, int r, int g, int b, ItemType type, int value, int price) {
        this.name = name;
        this.symbol = symbol;
        this.x = x; this.y = y;
        this.r = r; this.g = g; this.b = b;
        this.type = type;
        this.value = value;
        this.price = price;
    }

    public static char getSymbol(ItemType type) {
        return switch (type) {
            case POTION_HEAL, POTION_STRENGTH, POTION_SPEED, POTION_INVISIBILITY,
                 POTION_FIRE, POTION_POISON, POTION_SHIELD, POTION_MANA -> '!';
            case SCROLL_MAP, SCROLL_ENCHANT, SCROLL_IDENTIFY, SCROLL_TELEPORT,
                 SCROLL_REMOVE_CURSE, SCROLL_LIGHTNING -> '?';
            case WEAPON_DAGGER, WEAPON_SWORD, WEAPON_AXE, WEAPON_SPEAR, WEAPON_MACE, WEAPON_BOW, WEAPON_STAFF -> '/';
            case ARMOR_LEATHER, ARMOR_CHAIN, ARMOR_PLATE, ARMOR_MAGIC_ROBE -> '[';
            case GOLD -> '$';
            case FOOD -> '%';
            case KEY -> '+';
            case BOMB -> '*';
            case RING -> '=';
        };
    }
}
