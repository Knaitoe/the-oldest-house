# Six uncanny rooms — 0.4.113 development draft

Revised against the supplied concepts and texts in 0.4.116: see [the six rooms against their texts](UNCANNY_TEXT_PASS_0_4_116.md).

The owner supplied the six concepts, complete found texts and artifact tags. This implementation adds those rooms to new House builds while retaining all earlier Claude, funeral, hospital, Borrasca, Plains, home-encounter, portrait and Goatman changes. This is an unverified development candidate.

| Room | Discovery surface | Required resolution | Fixed Witness kind |
| --- | --- | --- | --- |
| Boarded House (`painted_interior`) | Wallpaper scrap on kitchen table | Read the scrap, witness a Propped original at the edge of actual camera view, pry painted boarding, leave by the unpainted side door, read account | Understanding |
| Blue-Light Diner (`second_waitress`) | Receipt at cash register | Witness the same false waitress become physical and serve her finite original meal; refuse through the real waitress, read staff-side account | Understanding |
| Stooping Giant (`tunnel_periphery`) | Maintenance clipboard near entrance | Walk the native tunnel, gain ground during green signals, reach the final landing, read account | Survival |
| Split-Feed Bedroom (`missing_sleep`) | Notebook beside CRT desk upstairs | Attempt bed twice, inspect all four actual live feeds, see own skin in hallway feed and marionette directly, physically close parents' open door, read account | Memory |
| Unbuttoned Basement (`layered_man`) | Greased ledger at stair head | Strip real layers with a worn tool, recover the original key and bring it up; alternatively shut the cellar after the worker's blade breaks; read own account | Understanding |
| Buzzer Threshold (`motion_light`) | Scrawl beside intercom | Witness two genuine floodlight dark transitions, identify/open the hinged service door and cross it, read account | Survival |

All supplied words remain in `UncannyTexts`, split on whitespace into native 145-character pages. Artifacts carry their complete supplied lore. Reading menus pause personal hazard clocks. The optional bedroom bed attempt commits the night; a bounded failure releases that commitment. Ordinary entry permits retreat. Failure grants no Witness credit and leaves the House return available.

The Book's 75-percent rule remains derived from the fixed eligible pool: 52 sources, 39 personal resolutions and at least two kinds. Existing three terminal endings remain. No outcome changes a source's kind.

## Art, sound and accessibility

Materials are original runtime 64×64 pixel atlases containing sixteen 16×16 tiles. Actors use 64×64 skins with eight-pixel faces, native player joints and dedicated poses. Fittings and items have block-scale meshes. Nearest sampling retains crisp pixels. No existing PNG is repainted. Four native GPU views cover new actors, materials, fittings and inventory models.

Directional wood creaks have matching left/right text. Signals pair color with GREEN/RED, distinct shapes and notes. Frost has both visible accumulation and rattling; growth has heartbeat and static HUD pulse. The floodlight changes actual emitted light from 15 to 0 with relay and text cues. Existing native Minecraft sounds are reused; there are no new recorded audio files.

The four CCTV channels are actual native level renders into small nearest-sampled textures. They display the same saved doll body as the reader's current wide/slim skin with normal gait; direct view uses its own jerking pose. No prerecorded substitute is used. Actual-client capture, culling, texture provenance, peer isolation and reconnect still require verification before release.

## Artifacts

- Bone Ring: hold in offhand for native client-only outlines and intentions of nearby tracked humanoids, through walls. Other players display Unknown. Owner-private actors remain private.
- Smoky Mirror: use while holding to spend two actual health points for 240 ticks of true sight. Cancelled/invulnerable/fully absorbed damage cannot fuel it. Sneak-use reads its supplied tag. Holding a ring and using a mirror never grants Witness credit.

The Boarded House's completed account awards one ring; the bedroom awards one mirror. Saved rewards and original plates/keys are finite. Removing an original actor or item does not regenerate it. Shared physical changes do not award a peer's personal account.

## Operator testing

On an already built new-layout House:

```text
/ghosthouse vignette uncanny painted_interior start
/ghosthouse vignette uncanny second_waitress start
/ghosthouse vignette uncanny tunnel_periphery start
/ghosthouse vignette uncanny missing_sleep start
/ghosthouse vignette uncanny layered_man start
/ghosthouse vignette uncanny motion_light start
/ghosthouse vignette uncanny missing_sleep status
/ghosthouse vignette uncanny return
/give @s the_oldest_house:bone_ring
/give @s the_oldest_house:smoky_mirror
```

Start uses existing built geometry and saves the operator's exact return location. It never rebuilds a site, restocks original items or awards a resolution. Status reads saved state. Return restores the saved operator position.

## Validation gates

19 new focused native cases, 716 total declarations; previous 695 cases and 95 focused multiplayer cases remain. The exact-source twelve-job matrix, complete native suite, native GPU figures, architecture exports, both actual clients and existing reconnects remain mandatory. CCTV needs additional real-client evidence and reconnect coverage. This document records implementation intent; no new-room test pass is claimed yet.

Previous 0.4.112 repair baseline: `71a90f5c35f340d9a90a52d8e3c43fcadbea75e1`, all twelve source jobs passed in [run 39491525825](https://github.com/Knaitoe/the-oldest-house/actions/runs/39491525825).


Hotel screenshot follow-up: the wall sconce now has a broad flush backplate, a visible horizontal brass arm and an overlapping vertical shade stem. The native outline follows the same pieces. The existing hotel native case checks every actual interior/porch placement against its backing wall; the fitting proof adds eight front/side views across all four facings. These changes reuse the original shade and brass textures. Native validation remains pending.

The hotel follow-up replaces the permanently seated guest with a dedicated decayed creature, using an original 64×64 texture and an eight-pixel face. The saved original hides under the tub's opaque water surface. One bathroom-entry roll has a one-in-three chance and a three-minute occupied cooldown. The rise lasts 60 ticks; she then uses real collision to follow through the bathroom opening and Room 217's door into the hallway. She fades after reaching the hall, or after contact applies native frost and 80 ticks of slowing to her target. The encounter adds no damage or Witness credit. A missing original is never replaced.

Two native cases cover emergence/reload/contact/cooldown/peer isolation and her actual tub-to-hall route. The hotel now has fourteen focused cases; the full corpus has 716. Six fitting GPU pages include all four lamp orientations and six creature pose views.

Operator commands, while inside the existing built hotel:
```text
/ghosthouse vignette hotel_bath start
/ghosthouse vignette hotel_bath status
/ghosthouse vignette hotel_bath dismiss
```
Start forces the existing loaded dormant original's encounter. Dismiss fades the caller's encounter. Neither operation restocks, rebuilds or grants credit. Validation remains pending.
