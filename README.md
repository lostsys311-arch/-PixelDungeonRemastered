# Pixel Dungeon Remastered

A roguelike dungeon crawler for Android, inspired by Pixel Dungeon.

## Features

- Procedurally generated dungeons (rooms connected by corridors)
- Turn-based combat with monsters
- Field-of-view and exploration fog
- Items: potions, scrolls, weapons, armor, food, gold
- 10 dungeon depths with increasing difficulty
- Touch controls: swipe to move, tap to wait

## Build

### Locally
```bash
./gradlew assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions
Push to `main` branch — the workflow in `.github/workflows/build.yml` builds the APK automatically. Download from the Actions tab.

## Controls

| Action | Gesture |
|--------|---------|
| Move | Swipe in direction |
| Wait a turn | Tap on screen |
| Attack | Swipe toward enemy |

## License

GPL v3
