# Vision

Visor Diegetic is an addon for [Visor](https://github.com/VisorModStudio/Visor), the OpenXR VR mod for Minecraft. It replaces the hotbar, the inventory, block menus, and item interaction with slots that exist in the world and gestures made with the hands.

Visor and [VisorEssentials](https://github.com/VisorModStudio/VisorEssentials), both by the Visor team, already make Minecraft play like a current native VR title, and they keep growing in that direction. This project adapts that work toward a different target, which is a player physically inside the game world. It gives up some compatibility, simplicity, and approachability to get there.

Menus, screens, and controller button bindings are removed wherever a physical interaction can replace them. The result should still look and feel like vanilla Minecraft, with the player transported into it.

## Design test

A player should be able to swap controllers for basic hand tracking and change almost nothing about how they play. Three inputs are exempt: the movement stick, jumping, and snap turning. Converting those to gestures would cost comfort and control.

The test guides design decisions. Hand tracking is not a supported way to play. (At least not yet, mostly because there is no reliable way to handle locomotion.)

## Goals

[ROADMAP.md](ROADMAP.md) says which goals the first build includes.

### 1. Hands and grip holding

A hand is both the active hotbar slot and the cursor that carries an item between slots in a vanilla menu. A stack held in a hand can go into any slot, and any slot's stack can come out into a hand.

- Holding the grip holds the stack. Releasing the grip away from any slot drops it.
- A hand hovers a slot by coming near it or touching it.
- Releasing the grip while hovering a slot performs the operation that slot offers. Each operation has its own haptic and audio cue, so a player can tell them apart without looking.

| Slot state                                 | Cue                   | Result of releasing                                          |
| ------------------------------------------ | --------------------- | ------------------------------------------------------------ |
| Empty                                      | Low continuous rumble | The held stack moves into the slot.                          |
| Same item, and the held stack fits         | Low continuous rumble | The stacks merge.                                            |
| Same item, and the held stack does not fit | Short, regular pulses | The slot fills. The remainder stays in the hand.             |
| Different item, or a full stack            | Pairs of pulses       | The stacks swap. The slot's former stack is now in the hand. |

After a partial merge or a swap, the hand holds a stack while the grip is open. The player has a short grace window to close the grip again. If the window passes, the stack drops.

Every later goal reuses these operations and cues.

### 2. Placing blocks

Placing a block uses the grip and needs no separate button.

- Pointing the palm at a target and briefly opening and closing the grip places one block from the held stack.
- Keeping the palm out with the grip held places blocks continuously. When placement starts, the mod infers the plane or axis the player is building along. Dragging the hand along it keeps placing, even when the hand no longer points at a valid block face. This makes bridging over a gap possible.

### 3. Physical hotbar and inventory

Eight to ten hotbar slots sit at fixed places around the player's body and replace the radial hotbar. Reaching to a slot and opening the grip stores the held stack. Reaching to a slot and closing the grip takes its stack.

The body does not show stored items directly. A slot appears only while a hand is near it.

Body slots have two display rules that other slots do not, because a body slot is often outside the player's view.

- A body slot always faces the camera, and renders on top of anything else.
- When a body slot is outside the view, its display moves to the nearest edge of the view with an arrow toward the slot, as games do for off-screen waypoints. The player can read the slot's contents without turning to look.

While a hand holding a stack hovers a body slot, the display also shows the pending operation. It shows an icon for the operation and the stack that would remain in the hand as a result, if applicable.

The stomach slot stands for the general inventory.

- Opening the grip while hovering it sends the held stack to the inventory.
- It follows the body slot display rules, so the player can tell it from nearby chest and waist slots.
- Its display previews the real destination. If the inventory holds a partial stack of the held item, the display shows that stack, and it shows the remainder when the held stack does not fit.
- Gripping the slot itself with an empty hand and pulling away from the body pulls out the inventory grid, like a projector screen. Letting go retracts the grid. The player can pass it to the other hand without closing it.
- Slots in the grid are ordinary slots. They do not face the camera or move to the view edge, because the grid is already in view.

Armor has body slots too. The helmet slot sits beside the head, accessible from either side, the chestplate slot at the chest, the leggings slot between the legs, and the boots slot near the feet.

### 4. Diegetic block menus

Crafting tables, furnaces, anvils, and other blocks with a menu show their individual slots on the block itself, located as makes sense. The slots appear when a hand comes near. No screen opens.

The crafting result is never a hologram floating above the block as done in similar projects. Each station gets a physical action that performs the craft. These are sketches:

- Crafting table. The player lays ingredients in a 3 by 3 grid on the table top and strikes the table with an empty hand. The result appears back in the grid or perhaps drops as an item on the table.
- Anvil. A valid recipe spawns a hammer. The player picks it up and strikes the anvil.
- Enchanting table. The book starts closed. Placing lapis on its cover opens it to blank pages. Holding an enchantable item turns it to the first enchantment, and the player turns pages for the others. Closing the book on the item enchants it.

### 5. Picking up and dropping items

Automatic pickup becomes optional.

- In automatic mode, a picked-up item tops up a matching stack in a body slot. Anything else goes to the inventory. Pickup never fills an empty body slot.
- In manual mode, the player points an empty palm at a dropped item to highlight it, then closes the grip to pull it into the hand.
- Using the manual gesture with both hands pulls every highlighted item straight into the inventory.

### 6. Sprinting

The joystick walks. Swinging the arms as if jogging sprints.

Combat needs exceptions. Raising an arm overhead or drawing it back behind the body also triggers a sprint. A weapon swing may raise the top speed in the direction of initiation for a moment. This is not a boost, thrust, or lunge though, it only increases the top speed available in that specific direction. The user still must chose to input that direction, and cannot "steer" or turn which direction is increased, but may still choose to move at normal speed in the other directions.

### 7. Further ideas

None of these are planned.

- Looking straight up shows the player's own nametag. Pulling it down opens chat with a keyboard, or the pause menu.
- Pulling arrows out of the player's body.
- Leaving the world or accessing the pause menu by gripping in front of the face and pulling away, as if removing a headset.
- A slash with a weapon or tool leaves a tilted red grid on black where it landed, which fades.
