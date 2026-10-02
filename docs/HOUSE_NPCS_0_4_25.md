# House people · 0.4.25

Holloway now has a shared, persistent human actor and a playable camp and hunt.
Harrigan has alive/dead human skins over the original saved seated body. Tom and
Karen's private double continue to use the appropriate explorer's skin, including
standard/slim arms and a native adult default when a profile is unavailable.
The Mother, Hillary, Minotaur and existing child/lake actors retain their custom
models. Casts for unshipped borrowed vignettes remain planned.

## Holloway's camp

The dirt hut contains a real finite barrel: sixteen torches, food and a pickaxe.
Native opening is not theft. Actually taking a source slot arms that explorer's
next physical visit. Shared menus see the same items and do not refill. Every explorer may take one personal survey copy from the native book menu; taking that copy also arms the next visit, so late players are not blocked by an empty shared barrel. Reading or borrowing a copy does not arm it. The
field survey points north, through pillars, a trapped domestic gallery and broken
stone, to a service latch on the left. Signs grow less practical along the route.

On the next visit Holloway pursues active looters in the three connected arenas.
His crossbow uses native line of sight; cover breaks it. A correctly faced shield
or striking him buys a brief stagger. He is not a lootable kill. When sight is
lost he searches the pursued explorer's most recent actual, still-present placed
torch. Native torches are permitted in this scene; other building stays protected.

Spend at least four present seconds in each arena, traverse them in order, see
the actual hunter, then crouch and pull the service latch. The explorer receives
one battered vanilla shield and one locked native map with only its conjectural
upper half drawn. The service door returns through that explorer's own waypoint.
The existing finale preparation shield remains available.

## Multiplayer and saves

- One shared actor UUID; all tracking clients see it. A peer entering does not
  restage a room, respawn the hunter or replenish containers.
- Theft, visits, arena time, sightings, torch markers, completion and finite rewards
  belong to the native player UUID. A non-looter is not an attack target. Observers
  and borrowed items confer no completion.
- Each participating explorer must pull the latch. A peer's latch does not resolve
  the other player's run or force them out.
- Native departure or death ends an unfinished attempt; theft persists. In-room
  logout/restart pauses and resumes that visit. Offline time confers no arena time.
- Layout 22 appends slot 39 to layout 21. Old rooms, containers, dialogue, finite
  books, completed endings, actor UUIDs and companion custody remain in place.
- Harrigan's existing native head equipment carries the appearance component, so
  vanilla tracking and saved equipment synchronize skins to late joiners. No
  actor replacement or new appearance payload is required. Protocol 24 requires matching client/server builds for the new shared human entity.

The full personal escape is the seventeenth eligible Witness source, Survival.
The derived quota is thirteen across at least two kinds. There are three endings.
Camp arrival, opening supplies, shared geometry and an early latch are excluded.

## Art and native review

The built-in ImageGen tool produced the clothing material sheet. The selected
full master is `art/npcs/clothing-materials-generated.png` in the working copy;
the reproducible native import is versioned as `art/npcs/clothing-materials.png`.
`tools/generate_house_npcs.py` imports the actual generated cloth into exact
64×64 Minecraft player UVs, then authors faces, glasses, lapels, cuffs, straps
and boots at native pixel positions. It creates:

- `src/main/resources/assets/the_oldest_house/textures/entity/holloway.png`
- `src/main/resources/assets/the_oldest_house/textures/entity/harrigan.png`
- `src/main/resources/assets/the_oldest_house/textures/entity/harrigan_dead.png`

Final built-in prompt:

> Use case: stylized-concept. Asset type: clothing material texture sheet for Minecraft human NPC skins in The Oldest House. Primary request: one square, flat, seamless-looking pixel art material sheet, divided into four exactly equal quadrants with no gutters and no labels. Top left: faded olive explorer canvas, grubby seams and subtle worn weave, desaturated green brown. Top right: charcoal-black elderly gentleman's wool suit, fine subdued vertical weave and a few quiet lighter worn threads. Bottom left: rusty maroon and dark brown small check flannel, faded, low contrast. Bottom right: old ivory cotton shirt linen, creased and slightly yellowed. Composition: orthographic flat textile swatches only, each quadrant filled edge to edge. Style: restrained Minecraft pixel art, crisp square pixels, each quadrant should look like a 32 by 32 pixel texture enlarged nearest-neighbor, narrow palette and subtle variation; surfaces read even when sampled at low resolution. Lighting: baked diffuse neutral, no directional light or shadows. Avoid: people, heads, clothing silhouettes, objects, letters, text, logos, borders, photorealism, smooth gradients, high contrast noise.

Required native tests cover simultaneous ordinary/shift-click cache takes,
two-player hunt and independent latch credit, finite rewards and borrowed shield,
observer/death exclusion, actual torch placement ownership, in-room logout and
saved reload, original Harrigan actor/equipment persistence and append-only
layout upgrades. The client proof loads the actual model layers, skins and
renderer registrations, renders native UV meshes in standing, seated and lying
poses, and verifies all written pages still fit native books.

Direct playtest: `/oldesthouse door holloway_camp`; walk through the test door.
Read the survey, take a supply, leave and return through the same door. Use
`/oldesthouse vignette holloway_camp status` for personal progression.
