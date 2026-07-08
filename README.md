# Pixel Dungeon Remastered

[![GitHub release](https://img.shields.io/badge/release-1.0--beta-blue)](https://github.com/lostsys311-arch/-PixelDungeonRemastered/releases)
[![Build APK](https://github.com/lostsys311-arch/-PixelDungeonRemastered/actions/workflows/build.yml/badge.svg)](https://github.com/lostsys311-arch/-PixelDungeonRemastered/actions)

A **FOSS** roguelike dungeon crawler for Android with infinite replayability.

**17+ hours of gameplay** | **30 depths + infinite mode** | **Level-up system**

Programmatic pixel art sprites, procedural sound effects, zero external assets.

## Download

Grab the latest APK from the **[Releases](https://github.com/lostsys311-arch/-PixelDungeonRemastered/releases)** page.

## Features

- **30 dungeon depths** with procedurally generated rooms and corridors
- **Infinite mode** — continue beyond depth 30, enemies scale forever
- **XP & leveling** — gain XP from kills, level up for +ATK/+DEF/+HP
- **Boss fights** every 5 depths (Rat King, Skeleton Lord, Orc Chief, etc.)
- **Shop** every 3 depths — spend gold on potions, scrolls, weapons, armor
- **7 weapons**: Dagger, Sword, Axe, Spear, Mace, Bow, Staff
- **4 armors**: Leather, Chain, Plate, Magic Robe
- **8 potions**: Heal, Strength, Speed, Invisibility, Fire, Poison, Shield, Mana
- **6 scrolls**: Map, Enchant, Identify, Teleport, Remove Curse, Lightning
- **Special items**: Bombs, Rings of Power, Food, Keys
- **8 monster types** with scaling difficulty
- **Currency system** — gold drops from kills, spent at shops
- **Score tracking** — based on gold collected, depths cleared, monsters killed
- **Field-of-view** with exploration fog
- **Touch controls**: swipe to move, tap to wait

## Quality Settings

Tap the gear icon (⚙) in-game. Settings persist per session.

| Level | Viewport | Tile Size | Lighting | Sprites |
|-------|----------|-----------|----------|---------|
| Potato | 10×8 | 12px | Off | Basic |
| Low | 14×11 | 16px | Basic | Basic |
| Mid | 18×14 | 20px | Smooth | Basic |
| High | 22×17 | 24px | Smooth | Detailed |
| Fancy | 28×22 | 32px | Smooth+Glow | Detailed |

## Sound Effects

All sounds are procedurally generated at runtime — no audio files needed.
Combat hits, item pickups, stairs, healing, death, victory, footsteps.

## Build

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Controls

| Action | Gesture |
|--------|---------|
| Move | Swipe in direction |
| Wait / skip turn | Tap on screen |
| Attack | Swipe toward enemy |
| Settings | Tap gear icon |

## License

GPL v3 — Free and Open Source Software
