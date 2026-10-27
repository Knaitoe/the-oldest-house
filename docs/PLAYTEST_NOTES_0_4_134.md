# October 27 continued playtest work — 0.4.134

This is an active development record. Unchecked items remain unfinished; no release verification is claimed.

## Owner requests

- Backseat: a more inhuman humanoid with altered shoulder, neck and facial proportions and a new fitted native texture; modest camera pullback during acceleration, rising wind whistle, a few more seconds, a loud crash.
- Whale shelves/windows: move the shelves back against the partition while keeping their faces toward the stairs; repair the disconnected window panels shown in the screenshots. Preserve original cabinet inventories and source papers.
- Layered man: flesh destruction should sound like wet tissue pulling and tearing.
- Tunnel periphery: a visibly broken neck, a substantially longer route, larger strides and difficult but possible escape; actual arrival beyond the last signal should bring relief.
- Hallway bleeds: overturned furniture is a rare, saved variation of ordinary domestic rooms, with probability increasing at greater personal depth. It must not be tied to one permanent disturbed layout.
- Large bleeds: carry each small fragment's flooring and furniture family into its expanded home. Add connected, readable environmental story beats. For the father's home, use child drawings, old awards, family photographs and notes to show the family's history rather than scattering unrelated props.
- After completing and verifying this batch, continue directly to the approved corn silo vignette in docs/STRUCTURAL_UNCANNY_0_4_132.md.

## Constraints

Keep the draft branch and push reviewable work. Use authored native pixel art; preserve the 649 protected historical PNGs, original actors/items/UUIDs, personal Witness records, finite supplies and player edits. Saved updates must be bounded, resident/entity-ready and unobserved; never routinely rebuild, restock or reroll a discovered room. These are improvements to existing stories; layout 47, protocol 88 and Witness 53 eligible / 40 required across two kinds / three endings are unchanged unless an actual implementation requires otherwise.

## Work and verification

- [x] Backseat custom creature mesh and fitted pixel painting implemented; all eighteen native GPU views pass and have been inspected.
- [x] Native car acceleration, small rearward camera offset and continuous private wind implemented; a fresh full two-actual-client and same-profile reconnect proof passes.
- [x] Heavier original glass/steel sound and a 36-tick native impact hold implemented. Audio decodes without clipping; all five focused native cases pass with clean shutdown.
- [ ] Shelves and window migration, native/GPU checks.
- [ ] Six wetter tearing variations and native action checks.
- [ ] Longer tunnel, broken-neck silhouette, pursuit balance, preservation and actual-client escape.
- [ ] Saved depth-dependent domestic disturbance selection and all normal-room variants.
- [ ] Shared domestic floor/furniture palettes and environmental narrative sequence.
- [ ] Final focused tests, native GPU readbacks, real two-client/reconnect checks and source CI.
- [ ] Next: implement and verify the corn silo vignette after this batch is pushed.

The prior 0.4.133 exploration correction is pushed as 3804d8f. Its complete 55-case exploration job and the corrected eleven-view native rendering step passed on GitHub. Complete prior CI was still running when this continuation began.

Backseat checkpoint: actual tree collision occurs after 61 acceleration ticks on both clients. Camera peel grows from 0.094m to approximately 0.17m; continuous wind grows from .208 to .549. Both acceleration captures, original seats/notebook, private audio, mirror changes, same-profile reconnect and independent confirmed House returns pass. The prior failed camera-restoration attempt was corrected and repeated in full. This checkpoint does not include the unfinished domestic/tunnel/shelf work. Full source CI remains required.
