# Harrigan's dead states and funeral presentation — 0.4.106

Based on Claude's current `claude/shallows-rework` at `efb5df8`, whose twelve jobs passed in
[run 39208513280](https://github.com/Knaitoe/the-oldest-house/actions/runs/39208513280).
All of that branch's Shallows, lake nights, phone screen, storytelling, lake house, Bly, Masque
and rebuilt Stanley changes are retained. The earlier paused integration work is separate.

The review traced the three-visit Harrigan implementation, its actual scene actor, native
interaction, renderer and existing visual tests against the two reported playtest failures.
The existing tests staged an already-loaded actor, so they did not cover an older entity
snapshot loading after a newer visit had been saved. The passing baseline did not establish
that the reported loading path worked.

## Repairs

The saved visit is authoritative. Whenever the original actor is recovered, its phase and
position are reconciled if necessary. An older living snapshot on visit two becomes the same
dead body in the chair; an older chair snapshot on visit three becomes the same body in the
actual casket. This does not require another arrival, rebuild the room, or replace the UUID.
Stable actors are left in place. Phase changes clear motion and settle rotation history.

Corpse interaction checks the saved visit immediately, including before the next scene tick.
There is no speech, acknowledgment or living reaction after his death. The renderer uses the
existing pale, closed-eye dead painting with a fixed slumped chair pose or folded-hands casket
pose. Living head tracking and breathing remain exclusive to the first visit.

The mourners have three distinct jointed poses: bowed with hands clasped, hand at the cheek
with the other arm supporting the elbow, and hand over the heart. Their fitted pixel outfits
have a white lapel flower, a folded pocket handkerchief and black mourning band, or a muted
waistcoat and watch chain. Their original UUIDs and dialogue remain.

The entrance has a freestanding Peabody & Sons plaque with custom pixel lettering, laurel,
brass frame, finished back and supported feet. Right-clicking reads it. The casket uses its own
polished wood, restrained brass and quilted ivory lining. Five new material sheets are generated
by `tools/draw_harrigan_funeral_assets.py`. Only the three requested mourner sheets change;
all other pre-existing PNGs, including Borrasca, remain exact.

Fresh funeral homes place the plaque. An unchanged authored old sign is replaced only in a
loaded, unseen, empty entrance; edited signs and removed props are left alone.

No personal outcome, finite phone, source or ending changes: 46 eligible sources, 35 required
resolutions across two kinds, three House endings. Layout 45 remains. Protocol 80 distinguishes
the additional plaque block state from older clients.

## Validation

Two native regressions restore actual older actor NBT after the newer visit state, then check
silent interaction, normal tick recovery, original identity, physical casket placement,
mourners, native save/load and absence of unearned items or Witness credit.

The existing socket expedition gains three phases after all earlier checks and reconnects.
Both actual clients must render and interact with the same original through all three visits,
check the actual renderer's selected skin and corpse pose stability, and save all six frames
before acknowledging. The third visit also checks the physical casket and three mourners.
Visit prerequisites are explicit fixtures; this is not a claim of an uninterrupted manual
three-visit playthrough.

The character GPU review retains all earlier views and adds a dedicated plaque/three-mourner
page. The casket views use the actual new materials. Source paint, texture CRC and generator
checks, plus the seventeen journey measurement tests, pass locally. Java/native checks require
CI in this workspace. All 676 declared native cases, all twelve exact-source jobs, existing
GPU/architecture checks and both reconnects are required. Final candidate validation is pending.
