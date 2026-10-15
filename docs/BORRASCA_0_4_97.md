# Borrasca — development candidate 0.4.97

The House opens onto an original, restrained adaptation of C. K. Walker's four-part **Borrasca**. Drisking's crime remains human. The impossible entry belongs to the House. There is no supernatural boss, assault animation, assault audio, nudity, victim item or reproductive simulation. This adaptation uses adult cast/victim records and newly written dialogue and papers; it does not reproduce the story's prose or attempt Part V.

The source and place names are credited to [C. K. Walker's original](https://ck-walker.com/2016/04/12/borrasca/). The realization, dialogue, documents, geometry, atlas and synthetic sound design are new work for this mod.

## Four returns

| Visit | Physical work | What changes |
| --- | --- | --- |
| 1 | Meet Kyle and Kimber, climb the native treehouse ladder and examine the carved plank. | A familiar town and a distant metallic sound. |
| 2 | Reach the upstairs bedroom and read the public register and mineral survey. | A missing person's belongings contradict the official closure. |
| 3 | Follow the graded road, examine the damaged sign and familiar pickup, read the service docket and see the machine. | The mountain sound belongs to a working refinery. |
| 4 | Confirm the content note at the office, read the criminal register and survivor's statement, take the original, operate the fire release and crouch under the service pipe onto the logging descent. | Familiar people are implicated. The sheriff follows through native navigation after the original is taken. |

The House entry permits retreat throughout. The service release opens without demanding completion of the investigation. A successful final account requires the reader's actual original, personally read documents, the service descent and native crouch, then reading the porch account and a successful return through the saved House door. Reading a borrowed record, arriving, requesting departure, logging out or dying grants no Witness. One source, kind `understanding`; the playable pool is 46 and its 75% rounded-up requirement is 35, across at least two kinds. All three House endings and existing earned completions remain.

## Persistence and native construction

Slot 98 appends after lake-house slots 96–97. Layout 45 never reconstructs a prior built scene. Each reader receives a persistent outdoor site in a separate 512-block column, four original saved native cast UUIDs, original papers and visit state. Completed sites are never carved again. First construction waits asynchronously for native chunks, uses the existing 4,096-visit/six-millisecond construction slices, saves command/subcommand cursors and releases all construction tickets. Resuming a partial build skips already applied commands and edits. Native entity sections must be loaded before cast creation or an unseen relocation; a missing saved original is awaited rather than replaced. Peers do not track each other's cast.

## Art and sound

`textures/entity/borrasca_atlas.png` is a new opaque 4×4 painted atlas: four faces; four clothing materials; denim, trousers, skin and hair; timber, galvanized metal, rust and paper. Explicit native face UVs fit its islands. Existing PNG files are not changed. The same atlas supplies the new block fittings and the native rotating machine. Joints use the existing native humanoid movement sampler.

Image-generation prompt: a square, edge-to-edge sixteen-tile atlas with no gutters or labels; four neutral adult faces, forest plaid/ochre/tan/indigo fabrics, denim/charcoal/skin/hair, and timber/steel/rust/cream paper; restrained painterly pixel-art diffuse materials. The original generated image is retained unchanged in the repository. Native block atlas registration is explicit.

`tools/compose_borrasca.py` deterministically composes original mono grinder, motor wind-down, timber creak and wind recordings without sampled speech or music. The muffled grinder is a phase-aligned low-pass copy of the same take. The fixed refinery position drives spatial panning, distance gain and enclosure filtering on the client. Power can be switched off at the actual machine. Vanilla music, including already-playing music, is suppressed while the reader occupies the leased site; departure/death/dimension changes clear the lease. There is no score. All sounds have subtitles and follow the Ambient/Master controls.

## Verification status

First checkpoint `47e1287` compiles. Its early GPU job identified the missing block-atlas registration; this is corrected in the working follow-up. The first checkpoint's complete old corpus is still running. This is a development candidate, not a verified handoff.

The follow-up adds seven dedicated native cases: append/eligibility; actual walking up the house stair and graded ridge, including sign reach; actual ladder ascent and crouch clearance; four staged-position/native-paper/native-return sequences; original cast/privacy/borrowed-record/reload preservation; saved bounded construction resume; and interrupted departure/re-entry. Fixture positions in the story sequence are explicit staging and do not certify an ordinary four-visit playthrough. The actual ladder case drives native travel on successive ticks. Six staged GPU views cover the four faces and connected gaits from front/side/back/dim plus all fixture facings and two machine phases. Three new native architecture cutaways retain all prior 94 views, for 97 total. All twelve existing CI jobs, the whole declared GameTest corpus, earlier GPU proofs and both actual-client reconnect proofs remain required before delivery. Further connected-client Borrasca coverage is in progress.

Connected-client phases 54–59 retain the original 53 phases and both reconnects. Both clients use the actual source door and production construction, read native source pages, check the fixed sound and vanilla-music exclusion, walk to and climb the authored ladder, then take distinct original records through native menus. An explicitly staged final-visit fixture selects the already-read investigation for the actual fire release, crouched low-pipe movement and saved native source return. A partial/staged route must award neither reader a Witness. These targeted checks remain pending until the exact-source run finishes.

Manual review still required: a normal survival traversal of all four visits, readability on the owner's display, subjective headphone distance/occlusion mix, and adversarial movement through the new terrain. No claim of complete ordinary-play verification is made.

Operator entry on a test-world copy: `/oldesthouse test vignette borrasca`. Use the placed native door, wait for first-time construction, and re-use it for returns. No operator command supplies a story completion.
