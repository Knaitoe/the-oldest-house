# October 9 playtest update — 0.4.64

Preparation branch: `feat/playtest-0.4.64`, based on the fully verified 0.4.63 source `cf90e331f911c81950de940a54800d47e21ab4ad`.

## First implementation

- A private yacht killer can take a native, gravity-driven hop over a single-height yacht rail when the takeoff, landing and crouched jump arc fit. Rails stay intact. Hull walls, stacked posts, low roofs and unsupported water landings remain obstacles. The original owner, body, pursuit speed and reader-presence clock remain authoritative.
- Mining yacht windows shatters glass while retaining the frame and a broken texture. Broken portholes still admit the same real crawling body on both client and server. Previously mined-out portholes in older saves remain usable. Native canceled break events and hull protection remain in force.
- The well starts with a quiet, slower inhale/exhale and grows heavier over the occupied wait. Local condensation accompanies exhalation; two original taunts arrive from above the cover. The actual world remains obscured by the existing darkness compositor.
- The cabin's physical CRT shows rising pixel water over its personal picture during the storm.
- Fresh frozen-lake originals have only the atmospheric first page. Existing owned originals remain exact.

## Remaining notes in this pass

| Scene | Work to incorporate |
| --- | --- |
| Plains | Replace the spyglass with a textured camera; save the actual viewed frame; use a distant boy silhouette. |
| Trailer | More cousins, packaged supplies and player serving; later window hint, obscured doorway, window movement/sound, frightened poses, and progression checks. |
| Cabin | Raise the found notes onto the nightstand; hide the final account until it is available to that reader. |
| Courtyard/archive | Outdoor furniture with two chess chairs, fewer incidental props, varied wall papers, and readable boarded windows. |
| Items | Bring the remaining key/ribbon and related artwork into the vanilla pixel style. |

## Validation

Candidate validation is pending. Required checks include the full native gameplay suite, focused literary and multiplayer suites, two actual clients mining/crawling through persistent shattered frames, native rail-vault motion, all resource PNG/JSON checks, complete writing and native visual proofs. The existing Elk visual proof includes intact and shattered variants of both yacht glazing blocks.

Layout remains **38**. Network protocol is **38**; use matching **0.4.64** clients and server. The quota remains **43 sources / 33 personal resolutions across two kinds**, with three endings and the original opening pace.
