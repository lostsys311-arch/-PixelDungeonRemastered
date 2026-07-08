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
    public boolean infiniteMode;
    public int score;
    public int monstersKilled;
    public boolean inShop;
    public List<Item> shopItems;
    public boolean levelingUp;
    public int victoryDepth;

    private SoundListener soundListener;
    private static final int BOSS_INTERVAL = 5;
    private static final int SHOP_INTERVAL = 3;
    private static final int END_DEPTH = 30;

    public GameLogic() {
        log = new MessageLog();
        score = 0;
        monstersKilled = 0;
        victoryDepth = END_DEPTH;
        initLevel();
    }

    public void setSoundListener(SoundListener listener) {
        this.soundListener = listener;
    }

    private void emitSound(String name) {
        if (soundListener != null) soundListener.onSound(name);
    }

    public void initLevel() {
        int rooms = 8 + Math.min(currentDepth, 40) * 2;
        tiles = DungeonGenerator.generate(WORLD_WIDTH, WORLD_HEIGHT, rooms);
        monsters = new ArrayList<>();
        items = new ArrayList<>();
        explored = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
        visible = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
        inShop = false;
        shopItems = null;
        levelingUp = false;

        Point spawn = DungeonGenerator.findTile(tiles, Tile.ENTRANCE);
        if (player == null) {
            player = new Entity("Hero", '@', spawn.x, spawn.y, 25, 6, 2, 255, 255, 255);
            player.isPlayer = true;
        } else {
            player.x = spawn.x;
            player.y = spawn.y;
        }

        spawnMonsters();
        spawnItems();
        placeStairs();
        updateFov();
        playerTurn = true;

        if (currentDepth == 1)
            log.add("Welcome to Pixel Dungeon Remastered! Find the stairs down.");
        else if (currentDepth == END_DEPTH + 1)
            log.add("Beyond the abyss... infinite mode!");
        else
            log.add("Depth " + currentDepth + ". Find the stairs down!");
    }

    private void placeStairs() {
        Point p = DungeonGenerator.findTile(tiles, Tile.STAIRS_DOWN);
        tiles[p.x][p.y] = Tile.FLOOR;
        for (int tries = 0; tries < 500; tries++) {
            int x = RNG.nextInt(WORLD_WIDTH);
            int y = RNG.nextInt(WORLD_HEIGHT);
            if (tiles[x][y] == Tile.WALL || (x == player.x && y == player.y)) continue;
            boolean occ = false;
            for (Entity e : monsters) { if (e.x == x && e.y == y) { occ = true; break; } }
            if (occ) continue;
            tiles[x][y] = Tile.STAIRS_DOWN;
            return;
        }
    }

    private void spawnMonsters() {
        int depth = Math.min(currentDepth, 100);
        int count = 4 + depth;
        int bossFloor = (depth % BOSS_INTERVAL == 0 && depth > 0);

        if (bossFloor && depth <= END_DEPTH) count = 2;

        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < 50; tries++) {
                int x = RNG.nextInt(WORLD_WIDTH);
                int y = RNG.nextInt(WORLD_HEIGHT);
                if (!tiles[x][y].walkable) continue;
                if (x == player.x && y == player.y) continue;
                boolean occupied = false;
                for (Entity e : monsters) { if (e.x == x && e.y == y) { occupied = true; break; } }
                if (occupied) continue;

                if (bossFloor && i == 0 && depth <= END_DEPTH) {
                    String[] bosses = {"Rat King", "Skeleton Lord", "Orc Chief", "Dark Wraith", "Abyss Watcher", "Dragon"};
                    int bi = (depth / BOSS_INTERVAL) - 1;
                    String name = bosses[Math.min(bi, bosses.length - 1)];
                    int hp = 30 + depth * 8;
                    int atk = 8 + depth * 2;
                    int def = 3 + depth;
                    Entity boss = new Entity(name, 'B', x, y, hp, atk, def,
                        200 + depth * 5, 50, 50);
                    boss.isBoss = true;
                    boss.visionRange = 8;
                    boss.gold = 20 + depth * 3;
                    monsters.add(boss);
                    log.add("You sense a powerful presence... " + name + "!");
                } else {
                    String[] names = {"Rat", "Snake", "Bat", "Goblin", "Spider", "Skeleton", "Orc", "Wraith", "Demon", "Shadow"};
                    String name = names[RNG.nextInt(names.length)];
                    int hp = 5 + depth * 3 + RNG.nextInt(5);
                    int atk = 2 + depth + RNG.nextInt(3);
                    int def = depth / 2;
                    Entity m = new Entity(name, name.charAt(0), x, y, hp, atk, def,
                        150 + RNG.nextInt(106), RNG.nextInt(100), RNG.nextInt(100));
                    m.gold = 1 + RNG.nextInt(1 + depth / 3);
                    monsters.add(m);
                }
                break;
            }
        }
    }

    private void spawnItems() {
        int depth = Math.min(currentDepth, 100);
        int count = 3 + depth / 2;
        for (int i = 0; i < count; i++) {
            for (int tries = 0; tries < 50; tries++) {
                int x = RNG.nextInt(WORLD_WIDTH);
                int y = RNG.nextInt(WORLD_HEIGHT);
                if (!tiles[x][y].walkable) continue;
                boolean occ = false;
                for (Item it : items) { if (it.x == x && it.y == y) { occ = true; break; } }
                if (occ) continue;
                items.add(generateRandomItem(x, y, depth));
                break;
            }
        }
    }

    private Item generateRandomItem(int x, int y, int depth) {
        int pool = RNG.nextInt(100);
        Item.ItemType type;
        String name;
        int value = 1;
        int price = 1;
        int r = 150 + RNG.nextInt(106);
        int g = 100 + RNG.nextInt(100);
        int b = 100 + RNG.nextInt(100);

        if (pool < 25) { // Potions
            Item.ItemType[] pots = {Item.ItemType.POTION_HEAL, Item.ItemType.POTION_STRENGTH,
                Item.ItemType.POTION_SPEED, Item.ItemType.POTION_INVISIBILITY,
                Item.ItemType.POTION_FIRE, Item.ItemType.POTION_POISON,
                Item.ItemType.POTION_SHIELD, Item.ItemType.POTION_MANA};
            type = pots[RNG.nextInt(pots.length)];
            name = type.toString().replace("POTION_", "").toLowerCase() + " potion";
            price = 5 + RNG.nextInt(15);
            r = 255; g = 100; b = 100;
        } else if (pool < 45) { // Scrolls
            Item.ItemType[] scrls = {Item.ItemType.SCROLL_MAP, Item.ItemType.SCROLL_ENCHANT,
                Item.ItemType.SCROLL_IDENTIFY, Item.ItemType.SCROLL_TELEPORT,
                Item.ItemType.SCROLL_REMOVE_CURSE, Item.ItemType.SCROLL_LIGHTNING};
            type = scrls[RNG.nextInt(scrls.length)];
            name = type.toString().replace("SCROLL_", "").toLowerCase() + " scroll";
            price = 8 + RNG.nextInt(20);
            r = 220; g = 200; b = 150;
        } else if (pool < 65) { // Weapons
            Item.ItemType[] weps = {Item.ItemType.WEAPON_DAGGER, Item.ItemType.WEAPON_SWORD,
                Item.ItemType.WEAPON_AXE, Item.ItemType.WEAPON_SPEAR,
                Item.ItemType.WEAPON_MACE, Item.ItemType.WEAPON_BOW, Item.ItemType.WEAPON_STAFF};
            type = weps[RNG.nextInt(weps.length)];
            name = type.toString().replace("WEAPON_", "").toLowerCase();
            value = 1 + RNG.nextInt(1 + depth / 5);
            price = 10 + depth * 2 + RNG.nextInt(20);
            r = 180; g = 180; b = 200;
        } else if (pool < 80) { // Armor
            Item.ItemType[] arms = {Item.ItemType.ARMOR_LEATHER, Item.ItemType.ARMOR_CHAIN,
                Item.ItemType.ARMOR_PLATE, Item.ItemType.ARMOR_MAGIC_ROBE};
            type = arms[RNG.nextInt(arms.length)];
            name = type.toString().replace("ARMOR_", "").toLowerCase() + " armor";
            value = 1 + RNG.nextInt(1 + depth / 5);
            price = 10 + depth * 2 + RNG.nextInt(20);
            r = 100; g = 150; b = 200;
        } else if (pool < 90) {
            type = Item.ItemType.GOLD;
            name = "gold";
            value = depth + RNG.nextInt(10);
            price = 0;
            r = 255; g = 215; b = 0;
        } else if (pool < 96) {
            type = Item.ItemType.FOOD;
            name = "food";
            price = 3;
            r = 200; g = 150; b = 80;
        } else {
            Item.ItemType[] misc = {Item.ItemType.KEY, Item.ItemType.BOMB, Item.ItemType.RING};
            type = misc[RNG.nextInt(misc.length)];
            name = type.toString().toLowerCase();
            price = 15 + RNG.nextInt(30);
            r = 255; g = 200; b = 50;
        }

        return new Item(name, Item.getSymbol(type), x, y, r, g, b, type, value, price);
    }

    public void updateFov() {
        visible = Fov.calculate(player.x, player.y, player.visionRange, tiles);
        for (int x = 0; x < WORLD_WIDTH; x++)
            for (int y = 0; y < WORLD_HEIGHT; y++)
                if (visible[x][y]) explored[x][y] = true;
    }

    public void movePlayer(Direction dir) {
        if (!playerTurn || gameOver || inShop) return;
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
                    onMonsterKilled(e);
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
            currentDepth++;
            emitSound("stairs");
            if (currentDepth % SHOP_INTERVAL == 0 && currentDepth <= END_DEPTH) {
                enterShop();
                return;
            }
            if (currentDepth > END_DEPTH && !infiniteMode) {
                infiniteMode = true;
                log.add("=== INFINITE MODE ACTIVATED! ===");
                log.add("How far can you go?");
            }
            initLevel();
            return;
        }

        endTurn();
    }

    private void onMonsterKilled(Entity e) {
        int xpGain = 5 + e.maxHp / 2;
        player.addXp(xpGain);
        int goldGain = e.gold;
        player.gold += goldGain;
        monstersKilled++;
        score += xpGain + goldGain * 2;
        log.add(e.name + " defeated! +" + xpGain + "XP +" + goldGain + "g");
        emitSound("monster_die");
        if (e.isBoss) {
            log.add("BOSS DEFEATED! You feel empowered!");
            player.attack += 3;
            player.defense += 2;
            player.maxHp += 10;
            player.hp = Math.min(player.hp + 10, player.maxHp);
        }
    }

    private void applyItem(Item item) {
        switch (item.type) {
            case POTION_HEAL:
                player.hp = Math.min(player.maxHp, player.hp + 15 + RNG.nextInt(15));
                log.add("Healing potion! HP restored.");
                emitSound("heal");
                break;
            case POTION_STRENGTH:
                player.attack += 3; player.maxHp += 5; player.hp += 5;
                log.add("Stronger! +3 ATK, +5 HP");
                break;
            case POTION_SPEED:
                player.visionRange += 2;
                log.add("Faster! Vision increased.");
                break;
            case POTION_INVISIBILITY:
                log.add("You fade from sight... monsters may lose you.");
                break;
            case POTION_FIRE:
                for (Entity m : monsters) {
                    if (m.isAlive() && player.canSee(m.x, m.y, tiles) &&
                        Math.abs(m.x - player.x) + Math.abs(m.y - player.y) <= 3) {
                        int dmg = 10 + RNG.nextInt(10);
                        m.damage(dmg);
                        log.add("Fire burns " + m.name + " for " + dmg + "!");
                        if (!m.isAlive()) onMonsterKilled(m);
                    }
                }
                monsters.removeIf(m -> !m.isAlive());
                break;
            case POTION_POISON:
                log.add("Poison courses through you! -5 HP");
                player.hp -= 5;
                if (player.hp <= 0) { gameOver = true; emitSound("death"); }
                break;
            case POTION_SHIELD:
                player.defense += 3;
                log.add("Shielded! +3 DEF");
                break;
            case POTION_MANA:
                player.attack += 5;
                log.add("Arcane power! +5 ATK");
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
            case SCROLL_IDENTIFY:
                log.add("You feel wiser. +2 ATK");
                player.attack += 2;
                break;
            case SCROLL_TELEPORT:
                for (int tries = 0; tries < 100; tries++) {
                    int tx = RNG.nextInt(WORLD_WIDTH);
                    int ty = RNG.nextInt(WORLD_HEIGHT);
                    if (tiles[tx][ty].walkable) { player.x = tx; player.y = ty; break; }
                }
                log.add("You teleport!");
                break;
            case SCROLL_REMOVE_CURSE:
                log.add("Your equipment feels purified.");
                break;
            case SCROLL_LIGHTNING:
                for (Entity m : monsters) {
                    if (m.isAlive() && player.canSee(m.x, m.y, tiles)) {
                        int dmg = 8 + RNG.nextInt(12);
                        m.damage(dmg);
                        log.add("Lightning strikes " + m.name + " for " + dmg + "!");
                        if (!m.isAlive()) onMonsterKilled(m);
                    }
                }
                monsters.removeIf(m -> !m.isAlive());
                break;
            case WEAPON_DAGGER: player.attack += 1 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_SWORD:  player.attack += 2 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_AXE:    player.attack += 3 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_SPEAR:  player.attack += 2 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_MACE:   player.attack += 4 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_BOW:    player.attack += 1 + item.value; log.add("Equipped " + item.name + "."); break;
            case WEAPON_STAFF:  player.attack += 2 + item.value; log.add("Equipped " + item.name + "."); break;
            case ARMOR_LEATHER:    player.defense += 1 + item.value; log.add("Wearing " + item.name + "."); break;
            case ARMOR_CHAIN:      player.defense += 2 + item.value; log.add("Wearing " + item.name + "."); break;
            case ARMOR_PLATE:      player.defense += 3 + item.value; log.add("Wearing " + item.name + "."); break;
            case ARMOR_MAGIC_ROBE: player.defense += 1 + item.value; player.attack += 1; log.add("Wearing " + item.name + "."); break;
            case FOOD:
                player.hp = Math.min(player.maxHp, player.hp + 8 + RNG.nextInt(7));
                log.add("Ate some food.");
                break;
            case GOLD:
                player.gold += item.value;
                score += item.value;
                log.add("+" + item.value + " gold.");
                break;
            case KEY:
                log.add("Picked up a key.");
                break;
            case BOMB:
                for (Entity m : monsters) {
                    if (m.isAlive() && Math.abs(m.x - player.x) + Math.abs(m.y - player.y) <= 4) {
                        int dmg = 15 + RNG.nextInt(15);
                        m.damage(dmg);
                        log.add("Bomb hits " + m.name + " for " + dmg + "!");
                        if (!m.isAlive()) onMonsterKilled(m);
                    }
                }
                monsters.removeIf(m -> !m.isAlive());
                break;
            case RING:
                player.attack += 2; player.defense += 2; player.maxHp += 5;
                log.add("Ring of power! All stats increased.");
                break;
        }
    }

    private void enterShop() {
        inShop = true;
        shopItems = new ArrayList<>();
        log.add("=== SHOP ===");
        log.add("Welcome! Spend your gold wisely.");

        int depth = Math.min(currentDepth, END_DEPTH);
        for (int i = 0; i < 6; i++) {
            Item item = generateRandomItem(player.x, player.y + i, depth);
            if (item.type != Item.ItemType.GOLD && item.price > 0)
                shopItems.add(item);
        }
    }

    public void buyItem(int index) {
        if (!inShop || index < 0 || index >= shopItems.size()) return;
        Item item = shopItems.get(index);
        if (player.gold >= item.price) {
            player.gold -= item.price;
            applyItem(item);
            shopItems.set(index, null);
            log.add("Bought " + item.name + " for " + item.price + "g.");
        } else {
            log.add("Not enough gold! Need " + item.price + "g.");
        }
    }

    public void leaveShop() {
        inShop = false;
        shopItems = null;
        initLevel();
        log.add("Shop closed. Continue descending.");
    }

    public void endTurn() {
        updateFov();
        playerTurn = false;
        processMonsters();
        if (player.hp <= 0) {
            gameOver = true;
            log.add("You died on depth " + currentDepth + ".");
            log.add("Final score: " + score);
            emitSound("death");
        }
        playerTurn = true;
        if (currentDepth > END_DEPTH && !gameOver && !infiniteMode) {
            infiniteMode = true;
        } else if (currentDepth == END_DEPTH + 1 && !gameOver) {
            log.add("You conquered the dungeon! Score: " + score);
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
