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
- [x] Saved shelf/window repair and native checks; two native close-up views added. Final viewing angles are being verified.
- [x] Six wetter tearing variations decode cleanly; the complete native cellar/action cases pass.
- [ ] Longer tunnel, broken-neck silhouette, pursuit balance, preservation and actual-client escape.
- [x] Rare deterministic route variations (2% plus .45 percentage points per depth, capped at 14%), saved discovery, actual original material/mesh pairs, unobserved changes and edit preservation; native checks pass.
- [x] Shared fragment/home palettes and fifteen finite household histories across four homes; all sixteen current interior cases pass, including original transfer, empty mounts and old-save preservation.
- [ ] Final focused tests, native GPU readbacks, real two-client/reconnect checks and source CI.
- [ ] Next: implement and verify the corn silo vignette after this batch is pushed.

The prior 0.4.133 exploration correction is pushed as 3804d8f. Its complete 55-case exploration job and the corrected eleven-view native rendering step passed on GitHub. Complete prior CI was still running when this continuation began.

Backseat checkpoint: actual tree collision occurs after 61 acceleration ticks on both clients. Camera peel grows from 0.094m to approximately 0.17m; continuous wind grows from .208 to .549. Both acceleration captures, original seats/notebook, private audio, mirror changes, same-profile reconnect and independent confirmed House returns pass. The prior failed camera-restoration attempt was corrected and repeated in full. This checkpoint does not include the unfinished domestic/tunnel/shelf work. Full source CI remains required.

Current environment checkpoint: all 83 combined uncanny, environment, service and interior native cases pass with clean shutdown. The final sixteen interior cases pass again after the record placement and close-up export adjustments. Twenty-six native environment cutaways and the existing asset pages rendered; inspection led to better shelf/window angles and a clearer shoulder-to-head neck connection, whose final renders remain pending. The full actual two-client tunnel sprint/traffic/music/departure/reconnect proof and full source CI remain pending.

Tunnel follow-up: original sixteen-second muffled passing traffic, owner-local native ambient loop, cancellation of newly requested vanilla MUSIC sounds and stopping the existing music manager only while inside. Exit, death, world change and logout release the loop. The actual-client proof explicitly attempts vanilla music during traffic and requires it to remain inactive.

Stories: the father's bicycle photograph, volunteer-coaching award, saved child drawing, school-play reminder, child's reply and unfinished letter connect to the blue shirt, bottles and empty bedroom. The family retains a work rota, child's house drawing and clothes note; students share rent marginalia, a record-sleeve note and a crossed-out forwarding address; the grandmother keeps a revised cake recipe, mending ticket and a family invitation that fits both chairs. These are ordinary fictional households, optional histories and finite shared originals, with no added Witness.

CI follow-up: the former literary failure exposed Backseat's missing miniature flavour line; it is now authored. The former full live run reached phase 54 after both earlier reconnects but clients walked into unloaded overworld terrain while the host already had a floor. That fixture now waits for genuine compiled client terrain and native grounded acknowledgment before walking; no production transition or physical assertions are bypassed. A subsequent trailer-client job exited 137 and remains under inspection.
