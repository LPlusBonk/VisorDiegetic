# Visor Diegetic

An addon for [Visor](https://github.com/VisorModStudio/Visor), the OpenXR VR mod for Minecraft. It replaces the hotbar, the inventory, and item interaction with slots on the player's body and gestures made with the hands.

## Status

Early development. The mod builds and loads as a Visor addon and adds no gameplay yet. [docs/ROADMAP.md](docs/ROADMAP.md) tracks progress.

## Requirements

- Minecraft 26.3 with Fabric Loader and Fabric API
- Visor 0.6.0-beta or later
- Java 25

## Build

```
./gradlew build
```

The jar is written to `build/libs/`. `./gradlew runClient` starts a development client with Visor and this mod loaded.

## Documentation

- [docs/VISION.md](docs/VISION.md): what the mod aims to be.
- [docs/ROADMAP.md](docs/ROADMAP.md): first-build scope, milestones, decisions, and open questions.
- [docs/VISOR.md](docs/VISOR.md): what Visor and VisorEssentials provide, and how this mod runs beside them.
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md): toolchain, running, project layout, and mixin conventions.
- [AGENTS.md](AGENTS.md): policies for AI agents working in this repository.

## License

[Mozilla Public License 2.0](LICENSE)
