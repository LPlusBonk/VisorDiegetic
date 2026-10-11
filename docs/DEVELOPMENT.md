# Development

## Targets

`gradle.properties` holds every version. The mod follows the newest Minecraft version that Visor builds for.

| | Version |
|---|---|
| Minecraft | 26.3 |
| Mod loader | Fabric Loader 0.19.5 |
| Fabric API | 0.161.0 |
| Java | 25 |
| Visor | 0.6.0-beta |

Minecraft 26.1 and later ship unobfuscated. The build uses Mojang's names directly and has no mappings.

## Requirements

- JDK 25. Gradle 9.7.1 fails on Java 27 with `Unsupported class file major version 71`, so JDK 25 must be the default or `JAVA_HOME` must point to it.
- An OpenXR runtime and a headset, to test anything in VR.

The Gradle wrapper downloads Gradle. Do not install it separately.

## Build and run

```
./gradlew build
./gradlew runClient
```

`build` writes the mod jar to `build/libs/`.

`runClient` starts a development client with Fabric API, Visor, and this mod loaded. The game directory is `run/`. The build pulls the full Visor jar onto the run classpath, so Visor needs no manual install.

Start the OpenXR runtime before `runClient`. Without one, Visor logs `XR_ERROR_RUNTIME_UNAVAILABLE`, sets its play mode to `DISABLED`, and the game runs flat.

The development client signs in as an offline placeholder account. Single-player works, and so does a local server with `online-mode=false`.

These log entries are expected in a development client:

| Log entry | Cause |
|---|---|
| `Failed to load library flite` | The narrator's speech library is not installed. |
| `Could not authorize you against Realms server` | Offline account. |
| `Friends service unauthorized (401)` | Offline account. |
| `Failed to set window icon` | The Wayland compositor lacks the icon protocol. |

A healthy start contains `Visor Diegetic client addon loaded`.

## Layout

```
src/main/java/io/github/lplusbonk/visordiegetic/
  VisorDiegeticMod.java     Fabric entrypoint, registers the addon with Visor
  VisorDiegetic.java        Mod id, name, logger
  client/                   Client addon. Rendering and input stay in this package.
  server/                   Dedicated server addon
src/main/resources/
  fabric.mod.json           Mod manifest, filled from gradle.properties at build time
```

Visor scans `client/` for its registration annotations.

## Dependency on Visor

The build takes Visor from Modrinth's Maven repository.

```
maven.modrinth:visor:<visor version>+mc<minecraft version>-fabric:api
```

The `api` classifier is the compile dependency. The same coordinate without the classifier is the full mod and is a runtime dependency only.

Code in this mod imports from `org.vmstudio.visor.api` and never from `org.vmstudio.visor.core`. When something exists only in core, reimplement it or reach it with a mixin, and record the dependency in [VISOR.md](VISOR.md).

## Reading Minecraft and Visor source

Minecraft is closed source. `./gradlew genSources` decompiles it into a sources jar, and an editor then opens Minecraft classes like any library. A Java language server also decompiles a class on demand when it has no sources.

Clone Visor and VisorEssentials next to this repository to read them. Their `mixin` packages show which Minecraft methods matter for hands, inventory screens, and item use.

To find where Minecraft implements a behaviour:

1. Open a class with a likely name, such as `Inventory`, `Slot`, `AbstractContainerMenu`, `ItemEntity`, or `MultiPlayerGameMode`.
2. Follow references and call hierarchies outward.
3. Or set a breakpoint, perform the action in game, and read the call stack.

`./gradlew runClient --debug-jvm` starts the client suspended and listening for a debugger on port 5005.

## Mixins

A mixin patches a Minecraft class when the game loads it. It couples the mod to Minecraft internals and breaks when they change. Use the first option that works:

1. A Visor event or API.
2. A Fabric API event.
3. A mixin.

Conventions:

- Prefix every member a mixin adds with `visorDiegetic$`.
- Prefer `@WrapOperation` over `@Redirect`. Two mods can wrap the same call, and only one can redirect it.
- Mixin targets are strings, and the compiler does not check them. Launch the game after changing one. A wrong target fails at startup and the error names the mixin.
- `javap -s -p` on a class prints the descriptors that `method` and `@At(target = ...)` need.
- The JVM flag `-Dmixin.debug.export=true` writes each patched class to `run/.mixin.out/`.

## Editor notes

- Lombok is on the compile classpath, and Visor's API uses it. A Java language server needs the Lombok agent to resolve generated accessors.
- IntelliJ's Minecraft Development plugin validates mixin targets. Language-server editors do not.
- Visor's source contains Stonecutter comments such as `//? if >=1.21.9 {`. They select code per Minecraft version. This mod targets one version and does not use them.
