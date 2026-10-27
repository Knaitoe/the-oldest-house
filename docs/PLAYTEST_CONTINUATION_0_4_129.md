# Playtest continuation

## 0.4.129 architecture checkpoint

Based on 58965b5 in draft PR 10. The original empty-home pass had incorrect sofa orientations, plaster showing through windows, and routes tested from too far inside the extension. Cupboard backing cubes also hid their opened doors, and the red chair's metal post caps shared the upholstery's top plane.

The four authored variants now continue through a two-wide opening around the original snippet's counters, bed and finite storage. A short extension of a free edge gives the passage a clear approach. The actual native walking test starts in the main hall, crosses the snippet, and reaches each room. Sofas face the coffee tables; storage fronts face into the room; the grandmother's paired chairs face her radio. The family bedroom has its occupied wardrobe against a partition and its packing box beside the bare closet. Windows have a solid black wool backing.

New worlds select one complete home, with a one-in-four chance of a second in another hall. Selection is fixed by the world seed. The remaining snippets stay short. Existing full homes are retained. Original-state, vacant-room migration repairs the 0.4.128 cells in 256 visits or three milliseconds per tick, one actual slice globally. Native BEs, finite contents, player edits and completed removals remain. Existing opened fittings retain their state when reoriented.

Wardrobes, refrigerators and sideboards now have actual cavities. Their doors and handles swing outward together; native picking and collision use the same cuboids. Opening waits for living bodies in the added door volume. Outward leaf UVs remain within their own sprite. The red diner chair's supports end beneath its upholstered backrest; booth meshes are unchanged.

Validation: all eight native interior/home cases pass locally, including a legacy repair through the original snippet and an unchanged original barrel/player chest. The actual client produced all 22 interior cutaways and six object sheets; reviewed home views show the revised furniture and the front-facing object sheet shows actual cupboard interiors. Final Gradle build, canonical model/material checks and the packaged art check pass (154 human selections, 649 protected PNGs). Full CI remains pending. No protocol, layout, Witness quota or household NPC change. Native corpus: 772 cases.

## Further accepted playtest notes, still in progress

- Diner: a finite menu handed to the seated player; choose pancakes, waffles, eggs and cheese, or grits. The original waitress faces the booth, takes the order, goes into the back to place it, and gives the second waitress an opportunity to approach. Deliver an individually textured spoiled version of the selected breakfast. Fix the existing meal's missing texture.
- The second waitress remains apparently human until the player actually eats. While that player remains seated at the booth, her spaced dialogue progresses through "Aren't you going to eat?", "What's wrong, honey?", "What's wrong?", "What's wrong?", "Eat up.", "Eat eat eat." Continue the pressure while seated, without a forced timed ending or narration throughout the whole vignette. Maintain original meal custody and personal multiplayer state.
- Smooth waitress movement, turning and poses; review the other recently modified NPCs for discontinuities as well.
- Child's room: more time between parent lines; remove text that explicitly locates the voice under the bed or instructs the player to go there.
- The pictured quiet room needs a composed reading/rest and utility arrangement, not scattered desks, cache and basin. Preserve its original note/cache and water functionality.
- Tunnel: a thinner, frail woman who is too tall for the concrete tunnel and whose neck folds unnaturally under it; concrete ceiling as well as walls. Add an actual end stoplight, corresponding room hue/lighting, and synchronized movement. Repair the personal conclusion. Keep delayed appearance, hostile classification, and original private identity.
- End-world cabin: simple native-pixel journal, custom wooden table and log seating, correct sink geometry, plants on window sills, a thicker fireplace back, and a genuinely glass firefly jar with moving, emissive, independently pulsing lights. Review the supplied cabin video as well as the stills.
- Layered Man: additional worker dialogue on entering the basement, directing the player to the cleaver and offering brief story fragments; restrained lines from the growing man, room rumble when he speaks, ooze at the base, and substantially slower expansion/regrowth that can actually be contained with the cleaver.
- Friendly NPC courtesy: reset strikes on each actual room entry, and choose varied subtle/ominous text from a separate pool at each escalating stage before the existing ejection threshold. Do not reset on routine tick/interaction or change hostile exemptions.

The earlier CI on 58965b5 passed the interior job and most other jobs, but failed one Elk carcass search assertion and timed out on native client login. Those failures are not a verified release and require investigation; source compilation alone is insufficient.
