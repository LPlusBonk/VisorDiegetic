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
| Button input              | The action framework in `client.input.action`, and `ActionButtonVREvent` for presses and releases. See [Input actions](#input-actions).               |
| World-space drawing       | `RenderPipelineStageVREvent` and `VRGameEffect`. See [Drawing in the world](#drawing-in-the-world).                                                   |
| Swing detection           | `SwingEntityVREvent` and `SwingBlockVREvent`.                                                                                                         |
| Held item pose            | `VRHandItemPose`, registered with `@RegisterVRItemPose`.                                                                                              |
| Hand rendering mode       | `HandRenderStateVREvent`.                                                                                                                             |
| World-space panels        | `VROverlay` and its subclasses, anchored with `PoseAnchor` to the head or a hand.                                                                     |
| Networking                | `VisorChannel.builder(...)` with `toServer` and `toClient` handlers, and `sendToClientTracking(Entity)`.                                              |
| Body trackers             | `VRLocalPlayer.getRawTrackers()`.                                                                                                                     |
| Events                    | Listeners marked `@VREventHandler`, registered on `VisorAPI.eventBus()`.                                                                              |

Visor already sends each player's head, hand, and tracker poses to the server, and the server relays them to nearby clients.

## Input actions

Read from source. Nothing in this section has run in a headset.

### Actions and bindings

A `VRAction` reads one controller input and belongs to one `VRActionSet`. `VRActionButton` is the base class for a button. A subclass implements `onPress`, `onRelease`, and `getDefaultBindings()`.

`getDefaultBindings()` returns one `ActionBinding` per `VRInteractionProfileType`. A profile type is a controller family, such as `OCULUS_TOUCH` or `VALVE_INDEX`. `new ActionBinding(rightHandedId, leftHandedId)` takes the button for a right-handed player first. `ActionBinding.EMPTY` leaves the action unbound on that profile.

Visor has no action named for the grip. The grip is a button id, a constant on the profile class of each controller family in `me.phoenixra.atumvr.api.input.profile.types`. An example is `OculusTouchProfile.BUTTON_GRIP_RIGHT`. Visor's own actions take the grip by default:

| Profile                                                      | Default action on the grip                    |
| ------------------------------------------------------------ | --------------------------------------------- |
| `OCULUS_TOUCH`, `VIVE_COSMOS`, `HP_MIXED_REALITY`, `STEAM_FRAME` | The radial hotbar, `hotbar_main` and `hotbar_offhand` |
| `VIVE`, `WINDOWS_MOTION`                                     | Right mouse                                   |
| `VALVE_INDEX`                                                | Middle mouse, on the grip force button        |

The player can rebind every action in Visor's settings. Visor stores the bindings of a set in `controls/<set id>.json`.

### Registration

Visor registers a `VRActionSet` subclass when the class meets three conditions:

- It carries `@RegisterActionSet`.
- It has a public constructor that takes one `VisorAddon`.
- It is in the package that `getAddonPackagePath` names, or in a subpackage.

`AddonManagerImpl.loadAddon` passes the addon to every component registry and then calls `onAddonLoad`. Each registry asks `ModLoader.getClassesAnnotated` for its annotation. On Fabric that method walks the class files under the package and reads their annotations with ASM, without initialising the classes. `ActionSetRegistry` then calls the constructor by reflection. It logs a failure and continues with the next class.

The `VRActionSet` constructor calls `loadActions()`. Fields of the subclass are not yet assigned at that point.

Checked by compiling: a class that uses `OculusTouchProfile`, `VRInteractionProfileType`, and `ActionBinding` compiles against the `api` jar alone. That jar bundles the `me.phoenixra.atumvr` and `me.phoenixra.atumconfig` packages.

### One active set

Only one action set is active at a time. Each tick `VRInputManagerImpl.preTick` takes the first set, in priority order, whose `isEnabledAndCanActivate()` returns true. Only that set's actions update. Sets of equal priority keep their registration order, and the core addon registers first.

Visor's `game` set is active while a player exists and no screen is open. Its `menu` set is active while a screen is open. Both have normal priority.

This rules out a separate action set for the grip:

- An addon set of normal priority that activates during play never becomes active, because `game` comes first.
- An addon set of higher priority replaces `game`, and movement, turning, and jumping stop.
- `VRActionSet` has no public method that adds an action to an existing set. `addKeyAction` accepts keyboard key actions only.
- A set registered with the id `game` replaces Visor's set. The movement, turn, and jump actions it would need are in `visor-core`.

### Reading the grip during play

Two approaches stay inside the API.

1. Listen for `ActionButtonVREvent` and match `getActionButton().getId()` against `hotbar_main` and `hotbar_offhand`. `isPressEvent()` tells a press from a release. The event is cancelable, and a canceled event skips the action's `onPress` or `onRelease`. `TaskHotBar` opens the radial hotbar only from those two calls, so canceling both events also turns the radial hotbar off. This approach follows whatever button the player bound to the hotbar. That is the grip by default on four of the seven profiles.
2. Read the button itself. `VisorAPI.client().getInputManager().getProfileManager().getActiveProfile()` returns the current `XRInteractionProfile`, or null. `ActionBinding.getButton(profile, leftHanded)` returns a `VRActionDataButton` with `isPressed()` and `isButtonChanged()`. This works whichever set is active and always reads the physical grip. It does not stop the action that Visor has bound to the same button.

`VRActionDataButton` reports pressed or not pressed. Whether the API exposes the analog squeeze value is not yet known.

## Drawing in the world

Read from source, except where a paragraph says it ran.

Visor draws its own world-space elements in three stages of each render pass, named by `RenderPipelineStage`: `AFTER_SOLID`, `AFTER_TRANSLUCENT`, and `AFTER_WORLD`. `VRRenderPass` lists the passes: `EYE_LEFT`, `EYE_RIGHT`, `CENTER`, `THIRD_PERSON`, and `GUI`. A handler runs once for each pass that draws the world. An addon can draw at two places.

- `RenderPipelineStageVREvent` fires after Visor's drawing for each stage. It carries the stage, the `VRRenderPass`, the `PoseStack`, and the partial tick. A listener implements `VREventListener`, marks a method with `@VREventHandler`, and registers with `VisorAPI.eventBus().registerListener(addon, listener)`. `BowAimCrosshairRenderer` in VisorEssentials draws its crosshair this way.
- A `VRGameEffect` subclass with `@RegisterVRGameEffect` has `render(renderPass, poseStack, partialTicks)`, which Visor calls during `AFTER_WORLD`. Visor calls it when `isVisible` returns true and either `isGlobal()` returns true or the current `VRDecorator` lists the effect's id. `GameEffectTrackerDebug` in `visor-core` draws axis markers on body trackers this way. A constant turns it off, and it uses helpers from `visor-core`.

Visor's API has no helper that draws a debug marker at a position. Minecraft 26.3 has one in `net.minecraft.gizmos.Gizmos`, with `point`, `line`, `arrow`, `cuboid`, `circle`, and `billboardText`. Each takes world-space positions and returns a `GizmoProperties` with `setAlwaysOnTop()` and `persistForMillis(int)`. A gizmo call throws `IllegalStateException` when the calling thread has no `GizmoCollector`. The collector behaviour below was read from Minecraft's bytecode with `javap` and from Visor's source.

Minecraft has three collectors:

| Collector             | Open during                                             | Lifetime of a gizmo                                   |
| --------------------- | ------------------------------------------------------- | ----------------------------------------------------- |
| Per tick              | `Minecraft.tick()` and the packet and task queue before it | Until the next frame in which a client tick runs      |
| Per frame, main thread | `GameRenderer.update` and `GameRenderer.extract`        | One world extract, unless `persistForMillis` extends it |
| Per frame, render thread | `GameRenderer.render`                                 | Until the next world render                           |

`LevelExtractor.extract` copies the latest tick's gizmos and the main-thread gizmos to the render-thread collector. `LevelRenderer` drains that collector and draws it on every world render.

Visor renders each pass through `McRenderUtils.renderGame`, which opens both per-frame collectors and runs a full extract and render. The tick gizmos are copied again on every extract, so a gizmo added during a tick is drawn in every pass, including both eyes. It moves 20 times a second, so a marker on a hand trails the hand.

A gizmo added from a `RenderPipelineStageVREvent` handler lands in the render-thread collector after that pass has drained it. The next pass draws it, with the wrong eye's data. `RenderPhaseStartedVREvent` fires when the phase changes and not for each eye. No hook for a per-eye gizmo turned up.

For code that runs on a tick, a `VisorTask` with `@RegisterVisorTask` and `getType()` returning `TaskType.VR_PLAYER_TICK` fits. Visor calls it from a mixin into `LocalPlayer.aiStep`, which runs inside `Minecraft.tick()`. It runs only while VR is active and the local player ticks, so it needs no check that a world is loaded. `isActive(player)` is the place for further conditions.

Checked by running in a headset: a `VR_PLAYER_TICK` task that calls `Gizmos.point` at `getPoseData().getGripHand(HandType)` for each hand draws a marker on each hand, in both eyes. `getPoseData()` returns the pose of the current tick. A task must return a non-empty id from `getId()`, or Visor logs an error and skips it.

Both examples draw the same way:

1. Take the pose for the frame from `VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER)`. Its positions are in world space. `getGripHand(HandType)` gives the grip pose of a hand.
2. Take the camera from `getCameraPose(renderPass)`.
3. Push the `PoseStack`, set it to identity, multiply by the transpose of the camera rotation, and translate by the target position minus the camera position.
4. Emit vertices with `McVertexBuilder`, and set shader and GL state with `McShaders` and `McGlState`. All three are in `org.vmstudio.visor.api.compatibility.mcversion.render`.
5. Pop the `PoseStack` and restore every GL state that changed.

The event handler should return early when `VisorAPI.clientState().stateMode().isNotActive()`.

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
