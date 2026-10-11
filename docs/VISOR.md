# Visor and VisorEssentials

What this mod can use from Visor, what Visor lacks, and how the mod runs next to VisorEssentials.

These findings come from reading the source of Visor and VisorEssentials at tag `0.6.0-beta`. Where a finding was also checked by running the game, the text says so. Recheck this file when the Visor version in `gradle.properties` changes.

## Repositories

| Repository                                                             | Contents                                                                                                   |
| ---------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| [Visor](https://github.com/VisorModStudio/Visor)                       | The VR mod.                                                                                                |
| [VisorEssentials](https://github.com/VisorModStudio/VisorEssentials)   | An addon by the Visor team. A current example of how an addon is built.                                    |
| [VisorAddonFabric](https://github.com/VisorModStudio/VisorAddonFabric) | A starter template. It pins Minecraft 1.20.1 and an old dependency source, so do not copy its build files. |

Visor has three kinds of module.

- `visor-api` is the public API: interfaces, events, annotations, network classes. Visor publishes it as a separate `api` jar. Addons compile against this jar only.
- `visor-core` is the implementation: OpenXR, rendering, poses, and nearly all mixins into Minecraft. Addons do not import it.
- `visor-fabric`, `visor-forge`, and `visor-neoforge` each wrap the other two for one mod loader.

VisorEssentials mirrors this with `essentials-core` and one module per loader.

## How an addon registers

A Fabric `ModInitializer` calls `VisorAPI.registerAddon` with a `VisorAddon`. The mod passes a different addon object on a client and on a dedicated server, chosen by `ModLoader.get().isDedicatedServer()`.

Visor calls `onAddonRegister` first, which is the place to create a network channel. It calls `onAddonLoad` later. Visor scans the package named by `getAddonPackagePath` for its registration annotations, such as `@RegisterVRItemPose` and `@RegisterVisorTask`, and instantiates the annotated classes.

Checked by running: Visor registers and loads this mod's addon.

## What the API provides

| Need                      | API                                                                                                                                                   |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Which hand                | `HandType` with `MAIN` and `OFFHAND`. It converts to vanilla `InteractionHand`.                                                                       |
| Hand and controller poses | `VRLocalPlayer.getRawController(HandType)`, `getRawHands()`, and the processed `VRPlayerPoseClient` with `getMainHand()`, `getOffhand()`, `getHmd()`. |
| Hand joints               | `RawHand.getJoint(VRHandJointType)`, 26 joints in OpenXR order.                                                                                       |
| Haptics                   | `VRInputManager.triggerHapticPulse(HandType, frequency, amplitude, durationNanoSec)` and the constants in `HapticFeedback`.                           |
| Button input              | The action framework in `client.input.action`, and `ActionButtonVREvent` for presses and releases.                                                    |
| Swing detection           | `SwingEntityVREvent` and `SwingBlockVREvent`.                                                                                                         |
| Held item pose            | `VRHandItemPose`, registered with `@RegisterVRItemPose`.                                                                                              |
| Hand rendering mode       | `HandRenderStateVREvent`.                                                                                                                             |
| World-space panels        | `VROverlay` and its subclasses, anchored with `PoseAnchor` to the head or a hand.                                                                     |
| Networking                | `VisorChannel.builder(...)` with `toServer` and `toClient` handlers, and `sendToClientTracking(Entity)`.                                              |
| Body trackers             | `VRLocalPlayer.getRawTrackers()`.                                                                                                                     |
| Events                    | Listeners marked `@VREventHandler`, registered on `VisorAPI.eventBus()`.                                                                              |

Visor already sends each player's head, hand, and tracker poses to the server, and the server relays them to nearby clients.

## What Visor does not provide

### Held items

Visor keeps vanilla's model of one selected hotbar slot plus one off-hand slot. Its two-handed mode lets the player choose which hotbar slot the off hand mirrors, through `VRLocalPlayer.setOffhandSlot(int)`. The server setting `isTwoHandedVR()` gates it.

A hand cannot hold a stack that is outside the hotbar, and Visor has no notion of a hand carrying a stack between slots. This mod needs its own held-item state.

### Physical slots

Nothing in Visor or VisorEssentials lets a hand reach a slot. Their inventory panels draw vanilla screens on a world-space quad, and the player points a cursor at them. Item movement goes through vanilla `Slot` clicks.

### Body tracking

`VRPlayerPose.getBodyYaw()` exists, and its documentation says it may differ from head rotation. On the client it does not. `LocalPlayerPose.calcBodyYaw()` and `RemotePlayerPose.calcBodyYaw()` both return the head yaw.

The full-body avatar is the vanilla player model turned to the head yaw. Visor solves the arms toward the hands and counter-rotates the head. The legs and torso play vanilla's walk animation. `VRBodyFull` defines parts for the head and both hands only.

The server has a different calculation. `PlayerPoseServerImpl.calcBodyYaw()` turns the line between the hands by 90 degrees, flips it to face the same way as the head, and blends it 70 to 30 with the head direction. The client does not use it.

`VRBodyPartType` lists waist, chest, shoulders, elbows, knees, ankles, and feet. Only real body trackers fill them. `XrRawPoseHandler` reads tracker poses each frame, and `getRawTrackers()` exposes them. Without trackers they are empty, and Visor contains no estimation for them. The Visor team has said estimation is not planned for at least this year (as of 2026-09-11).

For this mod:

- Anchoring body slots to Visor's body yaw would move every slot when the head turns. The mod computes its own torso direction from the head and both hands.
- When a waist or chest tracker is tracking, the mod reads it from `getRawTrackers()` and skips the estimate.
- The mod does not need the `VRBody` classes. They describe the rendered avatar, and an addon can extend them through `VRBodyType.createBody()`.

## What VisorEssentials adds

At the time of writing, VisorEssentials has three features. Each has a toggle in its settings screen.

Better Inventory:

- `VROverlayInventory` anchors an inventory panel to the off-hand controller. The panel shows while the player points at it or looks at it. It holds the hotbar, the storage grid, armor, the off-hand slot, the 2 by 2 crafting grid, and the recipe book.
- `VROverlayContainer` intercepts container screens and shows them as a panel near the block or entity. It covers chests, shulker boxes, dispensers, hoppers, furnaces, the crafting table, anvil, smithing table, grindstone, stonecutter, loom, cartography table, brewing stand, enchanting table, beacon, villager trading, and horses.
- `VROverlayDraggedItem` draws the carried stack on the pointing hand.

Better Bow:

- The player draws by pulling the hands apart, and draw distance sets arrow power.
- The server needs VisorEssentials for this. It is on in single-player and off on a dedicated server that does not enable it.

Overlay Focus Visualizer:

- An indicator appears above other players who are looking at a panel.

The radial hotbar, the keyboard, the chat and HUD panels, the VR settings, and the physical swing, eat, climb, crawl, and swim actions all belong to Visor itself.

## Coexistence

This mod runs with or without VisorEssentials installed.

- Better Inventory conflicts with goal 3. It must be off while this mod is active.
- The radial hotbar is part of Visor core. This mod must turn it off whether or not VisorEssentials is present.
- Better Bow uses both hands and its own draw gesture. Grip holding must account for it.
- Overlay Focus Visualizer does not conflict.

[ROADMAP.md](ROADMAP.md#open-questions) lists what is unresolved here.
