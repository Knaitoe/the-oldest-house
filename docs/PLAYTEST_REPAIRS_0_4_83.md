# Playtest repairs 0.4.83

The October 11 playtest video and cottage screenshots show the fan-room floor image failing, the elders facing away from the hearth, a bare chess table and a mill envelope that is only paper. The owner also reports Stacey staying in pursuit and asks for Camp Blood beds to offer concealment, custom camp décor, and a slower slasher with three more seconds between attacks.

This candidate includes the 0.4.82 outdoor visibility changes and the completion-gated AM **design document**. AM gameplay is not part of this build.

## Fan room

The floor image uses the same fitted cow-elk atlas as the actual elk, but it previously lacked the native model-axis inversion and used an arbitrary height below the fan. It now uses the proper lying-body transform, the actual seven-block fan-to-floor distance, and light sampled at the floor. Its renderer bounds include the floor image. The rotating blades, first-person working-platform restriction, intermittent glimpse, wound state, saved ladder and existing repair/story sequence remain.

## Cottage and paw

Walter and Ruth sit nearer the hearth; Ruth faces toward the fire. Walter and the sergeant sit across a table bearing a custom board and thirty individually modelled chessmen. The son remains beside them. Existing actor UUIDs and personal dialogue/stages remain.

A successful wish sends only its owner a brief close focus on a jointed dried paw. Its wrist twists and exactly one finger curls at two joints; previously spent fingers remain curled. The view lasts 3.2 seconds, may be dismissed, never pauses multiplayer, and clears on death, logout or departure. The server remains the authority for the wish and finger count; the animation awards nothing.

The mill envelope is a native readable two-page letter with a custom envelope icon. Normal use opens the private native reading menu while the exact original stays in inventory. It explains the accident, the firm's refusal of responsibility, and the finite seven-emerald/three-ingot payment. Reading cannot issue more money or confer ending credit. Old paid paper envelopes with the original exact name and lore are upgraded in place when their reader returns, preserving their components and count.

## Stacey

An exposed fallback rush previously left `exposedHunt` set after striking, which disabled the next withdrawal. A strike or wound now starts a fresh withdrawal search. The one-to-three-second hide still begins only on reaching real cover, and low player health no longer bypasses the retreat. If cover is physically unavailable, the existing bounded fallback remains; she is never required to disappear through a wall. Body UUID, refuge rules, physical door breaking, offline clocks and shared multiplayer timing remain.

## Camp Blood

Six cabins receive weathered cedar and worn floors, six checked blankets, raised metal beds, numbered cabin signs, notices, first-aid cabinets, oars, life rings, canvas trunks, boots and warm camp lanterns. Twenty-seven camp textures and seventy-two native models produce 288 variants. The native cabin doors, windows, source papers, drawers, forest, path and ending remain.

Use a bunk to crouch into a native crawl-sized body, then move under its mattress. There are twelve pixels of real clearance between the corner legs. A reader beneath the bed is concealed from the slasher and does not broadcast ordinary crawling footsteps. Opening things or making other deliberate noise can still reveal a last known position; hiding earns no evidence. Leaving, death, logout and dimension changes restore the original pose. No player attribute or inventory is replaced.

Both slasher controllers move ten percent more slowly. Both now wait 90 ticks (4.5 seconds) between attacks, up from 30 ticks (1.5 seconds). One shared Camp Blood actor still runs one attack clock regardless of reader count; the elk hunter remains personal. Movement is physical and does not acquire a hidden player's live coordinates.

## Saved worlds and verification

Layout remains 42. Protocol is 55; client and server need the same build. No slots, discovered links, original identities, rewards, Witness requirements (44 sources / 33 resolutions / two kinds) or three endings are added or reset.

The small cottage seating repair waits for a loaded, unwatched scene and accepts only its old authored cells. Old occupied cottages keep the old seating until that repair can run. Saved Camp Blood dressing considers at most 64 cells per vacant pass, checkpoints its index and replaces only the original planks/beds/lanterns or empty decorative cells. Fresh construction uses the existing sliced build queue. Player edits and block entities are preserved.

Validation pending on the complete public source. Required: all twelve CI jobs; all 541 native cases; 635 decoded textures; every previous architecture/client/socket proof; seven new native GPU views of the fan-floor elk, camp bunk/décor, chessboard and articulated paw; both real clients must also use and crawl under the beds, walk back out with restored bodies, and render separate received finger counts. The distant-mob screenshot fixture waits for its native loaded corridor and uses a real glass window, so terrain cannot obscure its visual evidence. Four new gameplay cases exercise real bed input and crawl movement, the shared slasher attack clock, Stacey's retreat after an exposed strike at low player health, and private finite readable compensation.

The corrected 0.4.82 socket evidence passed at source `852c340` in run `38179838519`: both real clients tracked moving mobs at approximately 100 blocks, both saw the moving manor projection, privacy/frustum/range controls held, and departure removed only the scenery projection. That run's Goatman job was blocked by a Minecraft asset-download HTTP 304 failure before gameplay; this is not complete-suite verification of the combined candidate.

The first 0.4.83 candidate (`f97c3af`, run `38181415170`) stopped at compilation: the expanded fan bounds passed block positions to the native constructor that requires vectors. Corrected before gameplay validation.
