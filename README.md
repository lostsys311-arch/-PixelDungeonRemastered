# Pixel Dungeon Remastered

[![GitHub release](https://img.shields.io/badge/release-1.0--beta-blue)](https://github.com/lostsys311-arch/-PixelDungeonRemastered/releases)
[![Build APK](https://github.com/lostsys311-arch/-PixelDungeonRemastered/actions/workflows/build.yml/badge.svg)](https://github.com/lostsys311-arch/-PixelDungeonRemastered/actions)

A roguelike dungeon crawler for Android, inspired by Pixel Dungeon. Programmatic pixel art sprites, procedural sound effects, no external assets.

## Download

Grab the latest APK from the **[Releases](https://github.com/lostsys311-arch/-PixelDungeonRemastered/releases)** page.

## Features

- Procedurally generated dungeons (rooms + corridors)
- Turn-based combat with 8 monster types
- Field-of-view with exploration fog
- Items: potions, scrolls, weapons, armor, food, gold
- 10 dungeon depths with scaling difficulty
- **Graphics quality levels**: Potato, Low, Mid, High, Fancy
- **Procedural sound effects**: combat, items, stairs, death, victory
- Touch controls: swipe to move, tap to wait

## Quality Settings

| Level | Viewport | Tile Size | Lighting | Sprites |
|-------|----------|-----------|----------|---------|
| Potato | 10×8 | 12px | Off | Basic |
| Low | 14×11 | 16px | Basic | Basic |
| Mid | 18×14 | 20px | Smooth | Basic |
| High | 22×17 | 24px | Smooth | Detailed |
| Fancy | 28×22 | 32px | Smooth+Glow | Detailed |

Tap the gear icon (⚙) in-game to change quality.

## Build

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## Controls

| Action | Gesture |
|--------|---------|
| Move | Swipe in direction |
| Wait a turn | Tap on screen |
| Attack | Swipe toward enemy |
| Settings | Tap gear icon |

## License

GPL v3
