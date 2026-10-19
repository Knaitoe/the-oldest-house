# Borrasca — development 0.4.108

This implements the [reviewed four-visit design](BORRASCA_FOUR_VISITS_PROPOSAL.md) for new builds. It replaces the earlier town itinerary with the pine trail, the Triple Tree pact, Kimber's disappearance, the adult return and Sheriff's blockade, and the refinery investigation.

The development branch starts at `89ec9f9`, retaining the Claude, Harrigan and night-ward work. Version 0.4.108 / network protocol 82 / layout 45. Borrasca remains one Understanding source: 46 eligible stories, 35 required across two kinds, three House endings. The optional restored name changes the bark and adds an account line.

## Implemented checkpoint

- One continuous outdoor trail, four marker plates, ladder/fort, a fern gully for an adult's crouch and an enclosed compound with a service descent.
- Native child scale for visits one and two, released for the adult return and on leaving the site.
- Original saved Friend, Sheriff, Prescott and workers; native per-tick movement, knife/compass/flashlight hand poses and a physical walking escort.
- Personal carved names drawn by a native block-entity renderer. The steel gouge remains under the restored name.
- Two finite owner-bound originals, the compass and cloth name patch; native pickup/drop protection and saved compass confiscation/reclamation.
- Timed taking with occluded departure, directional glare controlled by the vanilla screen-effect setting, dust cues, a real flashlight cone and nonlethal detection/retry.
- A routine impound record naming the Sheriff's office and Prescott, and a saved irreversible breaker shutdown. The first-visit legend withholds the cause.
- One new 64×64 atlas containing sixteen authored 16×16 material tiles, plus a 32×32 cast atlas with eight-pixel faces matching vanilla heads. Limited palettes, connected pixel clusters and hard pixel edges keep props, people and the machine consistent with vanilla. Native prop/item models and four original synthetic contact/triad recordings. All pre-existing PNGs remain unchanged.

## Verification

Development checkpoint: compilation and expanded gameplay/client verification are pending. Do not describe this as a verified release or a completed ordinary four-visit playthrough. Existing Borrasca progression checks and connected drivers are being rewritten for the replacement; unrelated coverage stays in place. All twelve CI jobs and the declared native corpus remain required.

Eleven replacement Borrasca native cases cover child/Friend ladder motion and descent, a continuous second-visit walk with both crouched shelters, adult gully/service clearance, four confirmed returns with explicitly staged endpoints, ownership/privacy, reload, finite original custody, taking aftermath, denial/handover and continuous escort. The connected drivers now use the original Friend and name patch; final-visit socket staging cannot fabricate the three earlier returns. Ten native asset views cover all 104 fitting/facing variants, every used Friend/Sheriff gesture, the original items and five carving/name states. Architecture adds gully and taking-clearing views. There are 681 declared native cases, 1,000 PNGs and 99 architecture views. These checks still need to pass on the current source.

Early native runs exposed and corrected the bark registration, the actor's movement speed/travel integration, an over-strict follow leash, a route point inside the trunk and a missing rear-office doorway. The second route now goes around its shelter logs; descending service steps also admit native crouching. The carving is at child hand height and remains below the adult reader's gaze. All these repairs require current-source reruns.

The preceding 0.4.107 build failed the hospital supported-detail check: the new bedside cabinet top stopped at 15/16 block height, leaving the authored towels unsupported. This checkpoint brings both native collision and the matching cabinet mesh to the block top. Its deterministic ward asset check passes; the full architecture regression must pass again.

The initial generated material study was rejected as too detailed for vanilla. The candidate texture is drawn from scratch at native pixel resolution by `tools/generate_borrasca_trail_assets.py`, which also creates the meshes. `--check` verifies every pixel and generated definition. The material specification is retained at `docs/art/borrasca-trail-atlas.prompt.txt`. `tools/compose_borrasca_trail.py` creates only the four new recordings.
