# Roadmap

This file tracks scope, progress, decisions, and open questions. Update it in the same change that moves any of them.

Goal numbers refer to [VISION.md](VISION.md).

## First build

The first build makes hands a complete way to equip, stow, swap, and retrieve items. It contains:

- Goal 1 in full: grip holding, the four slot operations, their cues, and the grace window.
- Goal 3 in full: body hotbar slots, the stomach slot and pull-out inventory, armor slots, and the body slot display rules.
- The automatic pickup rule from goal 5: pickup tops up matching stacks in body slots and sends everything else to the inventory.

Block menus keep their vanilla screens in the first build.
Without manual item pickup, the first build is expected to feel somewhat inconvenient.

## Milestones

Each milestone proves one risk before the next one builds on it.

| #   | Milestone                                                                      | Proves                                         | Status      |
| --- | ------------------------------------------------------------------------------ | ---------------------------------------------- | ----------- |
| 1   | Project builds and loads as a Visor addon                                      | Toolchain and the Visor dependency work        | Done        |
| 2   | Debug marker on each hand, haptic pulse on grip                                | The mod can read hand poses and the grip input | In progress |
| 3   | Torso estimator with debug markers at chest, waist, and beside the head        | Body anchors hold still when the head turns    | Not started |
| 4   | One body slot that stores and returns a stack, with the server moving the item | The held-item model works end to end           | Not started |
| 5   | Remaining first-build features                                                 |                                                | Not started |

Milestone 5 covers the other body slots, all cues, the view-edge display, the stomach slot and inventory grid, armor slots, the pickup rule, and turning off the features this mod replaces. See [VISOR.md](VISOR.md#coexistence).

Milestone 1 was checked by running the development client without a VR runtime. Visor registered and loaded the addon. The development client has since started a VR session in a headset with the addon loaded. [DEVELOPMENT.md](DEVELOPMENT.md#running-in-vr-on-linux) records the configuration.

Milestone 2 has its hand markers. `DebugHandGizmos` draws one on each hand, checked in a headset. The haptic pulse on grip remains.

## Later builds

Each of these is independent of the others and builds on the first build.

- Goal 2, placing blocks.
- Goal 4, diegetic block menus. Each station is its own design task.
- Goal 5, manual pickup.
- Goal 6, sprinting. It touches movement and combat and needs none of the item work.
- Goal 7.

## Decisions

- Slots detect hands by proximity and contact only. Each slot has a trigger volume significantly larger than its visual. When volumes overlap, the nearest slot wins. Pointing is reserved for placing blocks and manual pickup.
- Body slot count and positions are data. A file defines the layout so it can change without code changes. The first build has no in-game layout editor.
- A layout may define more than one position for the same slot. The helmet slot uses this, with one position on each side of the head.
- The mod keeps its own record of what each hand holds. Visor's two-handed mode can only point the off hand at a hotbar slot, and goal 1 needs a hand to hold any stack. See [VISOR.md](VISOR.md#held-items).
- The mod estimates the torso itself. Visor's body yaw is the head yaw. See [VISOR.md](VISOR.md#body-tracking).
- The mod runs alongside VisorEssentials and replaces only its inventory feature. See [VISOR.md](VISOR.md#coexistence).
- State that other players or the server need goes over a `VisorChannel`, the same way Visor syncs poses.
- The first build targets one Minecraft version on Fabric. See [DEVELOPMENT.md](DEVELOPMENT.md#targets).

## Open questions

- How the mod reads the grip. Visor has no grip action, and an addon cannot use its own action set during play. [VISOR.md](VISOR.md#reading-the-grip-during-play) describes two candidates. Milestone 2 picks one and tests it in a headset.
- Whether the API exposes the analog squeeze value of the grip, and whether grip holding needs it.
- Where a held stack lives on the server. Vanilla has a selected hotbar slot, one off-hand slot, and the cursor stack of an open menu. Milestone 4 needs an answer.
- Whether body slot assignments need their own synced state, or whether clients can derive them from vanilla inventory data.
- How to turn off Visor's radial hotbar from an addon. Canceling `ActionButtonVREvent` for `hotbar_main` and `hotbar_offhand` is a candidate, read from source and not yet run. See [VISOR.md](VISOR.md#reading-the-grip-during-play).
- How a held bow and arrows behave under grip holding, and how that fits with the VisorEssentials bow.
- The length of the grace window.
- Which stack the stomach slot previews when the inventory holds several partial stacks of the held item.
- How the torso estimator behaves for a seated player.
