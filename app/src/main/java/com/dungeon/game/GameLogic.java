package com.dungeon.game;

import java.util.*;
import com.dungeon.game.DungeonGenerator.Point;

public class GameLogic {
    private static final Random RNG = new Random();

    public static final int WORLD_WIDTH = 50;
    public static final int WORLD_HEIGHT = 40;

    public int currentDepth = 1;
    public Tile[][] tiles;
    public Entity player;
    public List<Entity> monsters;
    public List<Item> items;
    public boolean[][] visible;
    public boolean[][] explored;
    public MessageLog log;
    public boolean playerTurn;
    public boolean gameOver;
    public boolean won;
    private SoundListener soundListener;

    public GameLogic() {
        log = new MessageLog();
        initLevel();
    }

    public void setSoundListener(SoundListener listener) {
        this.soundListener = listener;
    }

    private void emitSound(String name) {
        if (soundListener != null) soundListener.onSound(name);
    }

    public void initLevel() {
        tiles = DungeonGenerator.generate(WORLD_WIDTH, WORLD_HEIGHT, 8 + currentDepth * 2);
        monsters = new ArrayList<>();
        items = new ArrayList<>();
        explored = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
        visible = new boolean[WORLD_WIDTH][WORLD_HEIGHT];

        Point spawn = DungeonGenerator.findTile(tiles, Tile.ENTRANCE);
        if (player == null) {
            player = new Entity("Hero", '@', spawn.x, spawn.y,
                20 + currentDepth * 5, 5 + currentDepth, 2 + currentDepth / 2,
                255, 255, 255);
            player.isPlayer = true;
        } else {
            player.x = spawn.x;
            player.y = spawn.y;
        }

        spawnMonsters();
        spawnItems();
        updateFov();
        playerTurn = true;
        log.add("Depth " + currentDepth + ". Find the stairs down!");
    }

    private void spawnMonsters() {
        int count = 4 + currentDepth * 2;
        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < 50; tries++) {
                int x = RNG.nextInt(WORLD_WIDTH);
                int y = RNG.nextInt(WORLD_HEIGHT);
                if (!tiles[x][y].walkable) continue;
                if (x == player.x && y == player.y) continue;
                boolean occupied = false;
                for (Entity e : monsters) {
                    if (e.x == x && e.y == y) { occupied = true; break; }
                }
                if (occupied) continue;

                String[] names = {"Rat", "Snake", "Bat", "Skeleton", "Goblin", "Spider", "Orc", "Wraith"};
                String name = names[RNG.nextInt(names.length)];
                int hp = 5 + currentDepth * 3 + RNG.nextInt(5);
                int atk = 2 + currentDepth + RNG.nextInt(3);
                int def = currentDepth / 2;
                monsters.add(new Entity(name, name.charAt(0), x, y, hp, atk, def,
                    150 + RNG.nextInt(106), RNG.nextInt(100), RNG.nextInt(100)));
                break;
            }
        }
    }

    private void spawnItems() {
        int count = 3 + currentDepth;
        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < 50; tries++) {
                int x = RNG.nextInt(WORLD_WIDTH);
                int y = RNG.nextInt(WORLD_HEIGHT);
                if (!tiles[x][y].walkable) continue;
                boolean occ = false;
                for (Item it : items) { if (it.x == x && it.y == y) { occ = true; break; } }
                if (occ) continue;
                Item.ItemType[] types = Item.ItemType.values();
                Item.ItemType type = types[RNG.nextInt(types.length)];
                String name = type.toString().toLowerCase().replace('_', ' ');
                char sym = getItemSymbol(type);
                items.add(new Item(name, sym, x, y,
                    100 + RNG.nextInt(156), 100 + RNG.nextInt(156), 100 + RNG.nextInt(156),
                    type, 1));
                break;
            }
        }
    }

    private char getItemSymbol(Item.ItemType type) {
        return switch (type) {
            case POTION_HEAL, POTION_STRENGTH -> '!';
            case SCROLL_MAP, SCROLL_ENCHANT   -> '?';
            case WEAPON_SWORD, WEAPON_DAGGER, WEAPON_AXE -> '/';
            case ARMOR_CHAIN, ARMOR_PLATE, ARMOR_LEATHER -> '[';
            case GOLD -> '$';
            case FOOD -> '%';
            case KEY   -> '+';
        };
    }

    public void updateFov() {
        visible = Fov.calculate(player.x, player.y, player.visionRange, tiles);
        for (int x = 0; x < WORLD_WIDTH; x++)
            for (int y = 0; y < WORLD_HEIGHT; y++)
                if (visible[x][y]) explored[x][y] = true;
    }

    public void movePlayer(Direction dir) {
        if (!playerTurn || gameOver) return;
        int nx = player.x + dir.dx;
        int ny = player.y + dir.dy;
        if (nx < 0 || nx >= WORLD_WIDTH || ny < 0 || ny >= WORLD_HEIGHT) return;
        if (!tiles[nx][ny].walkable) return;

        for (Entity e : monsters) {
            if (e.isAlive() && e.x == nx && e.y == ny) {
                int dmg = player.attack(e);
                log.add("You hit " + e.name + " for " + dmg + ".");
                emitSound("player_attack");
                if (!e.isAlive()) {
                    log.add(e.name + " defeated!");
                    emitSound("monster_die");
                    monsters.remove(e);
                }
                endTurn();
                return;
            }
        }

        player.x = nx;
        player.y = ny;

        Item picked = null;
        for (Item it : items) {
            if (it.x == nx && it.y == ny) { picked = it; break; }
        }
        if (picked != null) {
            applyItem(picked);
            emitSound("item_pickup");
            items.remove(picked);
        }

        if (tiles[nx][ny] == Tile.STAIRS_DOWN) {
            emitSound("stairs");
            currentDepth++;
            initLevel();
            return;
        }

        endTurn();
    }

    private void applyItem(Item item) {
        switch (item.type) {
            case POTION_HEAL:
                player.hp = Math.min(player.maxHp, player.hp + 10 + RNG.nextInt(10));
                log.add("Healing potion! HP restored.");
                emitSound("heal");
                break;
            case POTION_STRENGTH:
                player.attack += 2; player.maxHp += 5; player.hp += 5;
                log.add("Stronger! +2 ATK, +5 HP");
                break;
            case SCROLL_MAP:
                for (int x = 0; x < WORLD_WIDTH; x++)
                    for (int y = 0; y < WORLD_HEIGHT; y++)
                        explored[x][y] = true;
                log.add("Map revealed!");
                break;
            case SCROLL_ENCHANT:
                player.attack += 3; player.defense += 1;
                log.add("Equipment enchanted! +3 ATK, +1 DEF");
                break;
            case WEAPON_SWORD: case WEAPON_DAGGER: case WEAPON_AXE:
                player.attack += item.value + 2;
                log.add("Equipped " + item.name + ".");
                break;
            case ARMOR_CHAIN: case ARMOR_PLATE: case ARMOR_LEATHER:
                player.defense += item.value + 1;
                log.add("Wearing " + item.name + ".");
                break;
            case FOOD:
                player.hp = Math.min(player.maxHp, player.hp + 5 + RNG.nextInt(5));
                log.add("Ate some food.");
                break;
            case GOLD: log.add("Picked up gold."); break;
            case KEY:  log.add("Picked up a key."); break;
        }
    }

    public void endTurn() {
        updateFov();
        playerTurn = false;
        processMonsters();
        if (player.hp <= 0) {
            gameOver = true;
            log.add("You died on depth " + currentDepth + ".");
            emitSound("death");
        }
        playerTurn = true;
        if (currentDepth > 10 && !gameOver) {
            won = true;
            log.add("You escaped the dungeon! Victory!");
            emitSound("victory");
        }
    }

    private void processMonsters() {
        List<Entity> dead = new ArrayList<>();
        for (Entity m : monsters) {
            if (!m.isAlive()) { dead.add(m); continue; }
            monsterAI(m);
        }
        monsters.removeAll(dead);
    }

    private void monsterAI(Entity m) {
        int dx = player.x - m.x, dy = player.y - m.y;
        int dist = Math.abs(dx) + Math.abs(dy);
        boolean canSee = m.canSee(player.x, player.y, tiles);

        if (canSee && dist == 1) {
            int dmg = m.attack(player);
            log.add(m.name + " hits you for " + dmg + ".");
            emitSound("player_hit");
            return;
        }
        if (canSee && dist <= m.visionRange) {
            int sx = Integer.signum(dx), sy = Integer.signum(dy);
            if (RNG.nextBoolean()) {
                if (sx != 0 && tryMove(m, sx, 0)) return;
                if (sy != 0 && tryMove(m, 0, sy)) return;
            } else {
                if (sy != 0 && tryMove(m, 0, sy)) return;
                if (sx != 0 && tryMove(m, sx, 0)) return;
            }
        } else if (RNG.nextInt(100) < 30) {
            Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
            Direction d = dirs[RNG.nextInt(4)];
            tryMove(m, d.dx, d.dy);
        }
    }

    private boolean tryMove(Entity m, int dx, int dy) {
        int nx = m.x + dx, ny = m.y + dy;
        if (nx < 0 || nx >= WORLD_WIDTH || ny < 0 || ny >= WORLD_HEIGHT) return false;
        if (!tiles[nx][ny].walkable) return false;
        if (nx == player.x && ny == player.y) {
            int dmg = m.attack(player);
            log.add(m.name + " hits you for " + dmg + ".");
            emitSound("player_hit");
            return true;
        }
        for (Entity other : monsters) {
            if (other != m && other.isAlive() && other.x == nx && other.y == ny) return false;
        }
        m.x = nx; m.y = ny;
        return true;
    }
}
