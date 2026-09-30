# Clap-and-seek — 0.4.6

Entering the unfinished child's room locks its entry and reserves the game for that player. Other players cannot take the reserved turn. The room's wall note is refreshed when the reservation begins, so an existing save receives the new rules without rebuilding the labyrinth.

The sixty-second timer starts when the blindfold is equipped, including the child's count to ten. Its temporary binding is enforced by the server; the original cloth and its components are preserved. The client keeps the opaque cloth overlay during inventory predictions and enforces first-person view and a visible HUD, including on each rendered frame. The narrow floor gap, hotbar, action bar and subtitle arrows remain readable. Milk does not affect the cloth.

Follow the claps. The girl's authored model and UV atlas provide actual bare feet and a cotton nightdress. Only the participant's renderer shows her. Open the final wardrobe while still blindfolded to win. The cloth returns to the inventory with its original components, the room releases, the drawing appears and the vignette is completed. Opening it at or after the deadline does not win.

At sixty seconds the game becomes a failure. Her feet approach for about a second; a four-tick camera twist suggests a snapped neck, followed by fatal damage. The twist respects the FOV-effects setting. The player respawns in the House dimension's domestic manor hall, two blocks before the impossible hallway threshold, facing it. Their normal bed respawn remains intact for future deaths.

Items are dropped, not deleted or immediately handed to the Mother. In an ordinary world vanilla produces death drops. With `keepInventory`, this specific failure instead drops carried inventory, armor and offhand as ordinary item entities. A held menu-cursor stack also drops. The temporary cloth binding is removed before the drop. Counts, names, durability and components survive, and the Mother's owner record is applied to forced drops. The drops can be recovered; meaningful abandoned items reach her collection only when they despawn, using her existing lifespan and farewell rules. Respawning grants no duplicate inventory.

The room reservation, original cloth, start time, clap progress and pending respawn are saved. Logging out retains the reservation and deadline; a running server's clock keeps advancing. On reconnect after expiry, the failure presentation plays before death. A stopped or paused single-player world does not advance game ticks. Failure restores the blindfold frame and clears the wardrobe for another attempt; success completes the one-shot.

## Playtest

1. Use `/oldesthouse door hide_and_clap` and cross into the room. Confirm the entry shuts immediately, stays locked and does not route you back out. Read the refreshed note and retrieve the cloth.
2. Wear it. Try removing it, swapping helmets, dropping it, toggling third-person view and hiding the HUD. Confirm the floor gap and subtitle arrows remain visible while the room stays concealed.
3. Follow the claps and open the wardrobe before the minute ends. Confirm the blindfold releases, drawing appears and exit works.
4. Reset using `/oldesthouse vignette hide_and_clap reset`. Wait after equipping. Confirm the approaching feet, brief camera twist and fatal ending. Respawn and confirm the position in the manor rather than at your bed or the hallway's far end.
5. Repeat with named, damaged items in inventory, armor and offhand, first with normal death rules and then `keepInventory`. Return to the room to verify real drops and no inventory duplication. Leave a meaningful drop until it expires and check the Mother's collection. A life-extension mod should still postpone collection.
6. Reconnect during a round, then restart a test server. Confirm the lock and original deadline remain. A second player should not take the reserved turn or see the private apparition.

## Native assets

`tools/generate_clap_assets.py` emits `ClapGhostModel.java`, a 512×512 girl atlas on a 128×128 logical UV layout, the 32×32 blindfold item, a 64×16 woven cloth edge, the local-pivot model source and `art/clap_seek_preview.png`. The preview renders the actual geometry and UVs. Pillow and numpy are authoring dependencies only. Client visual timing and foot visibility still need an in-game playtest.
