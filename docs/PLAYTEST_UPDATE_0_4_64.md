# October 9 playtest update — 0.4.64

Preparation branch: `feat/playtest-0.4.64`, based on the fully verified 0.4.63 source `cf90e331f911c81950de940a54800d47e21ab4ad`.

## First implementation

- A private yacht killer can take a native, gravity-driven hop over a single-height yacht rail when the takeoff, landing and crouched jump arc fit. Rails stay intact. Hull walls, stacked posts, low roofs and unsupported water landings remain obstacles. The original owner, body, pursuit speed and reader-presence clock remain authoritative.
- Mining yacht windows shatters glass while retaining the frame and a broken texture. Broken portholes still admit the same real crawling body on both client and server. Previously mined-out portholes in older saves remain usable. Native canceled break events and hull protection remain in force.
- The well starts with a quiet, slower inhale/exhale and grows heavier over the occupied wait. Local condensation accompanies exhalation; two original taunts arrive from above the cover. The actual world remains obscured by the existing darkness compositor.
- The cabin's physical CRT shows rising pixel water over its personal picture during the storm.
- Fresh frozen-lake originals have only the atmospheric first page. Existing owned originals remain exact.

## Additional implementation

- The Plains equipment now includes a 16-pixel camera and viewfinder. Developing a picture uses the real client framebuffer, quantized to Minecraft's native map palette. The server grants the original only for its own pending exposure while the reader is still aiming. A peer or replay cannot spend that exposure. Existing photographs remain immutable. The former concrete rectangle is replaced by a distant, reader-owned child silhouette.
- Fresh trailer rounds have eight cousins, opaque spruce doors, later subtle window cues, movement and sounds outside the windows, a returning real runner, and crouched frightened children. Existing saved rounds retain their cast and automatic serving. Fresh supplies are finite packets of four: open the packet and serve individual franks on the native plates. A late enrolled player no longer freezes the evening clock.
- Cabin notes sit on a nightstand. A bounded saved repair archives any older physical original before raising its reading surface. Final ledgers render only for the reader whose account is available, and disappear for that reader after collection.
- The courtyard uses open-legged chess tables with two opposing chairs. Archive papers are grouped on plaster with four pixel variants; sealed windows have literal oak boards over dark panes. Existing-world edits wait for native entity sections, distant cameras and clear bodies, preserve player edits and containers, and run once after prior composition.
- Keys, the ribbon, and the new camera use crisp 16-pixel sprites. Existing cousin skins are retained; the additional cousins have their own skins. The 3D wheel key retains its native mesh.

## Review repairs

A review of the candidate found these, now repaired:

- **Frozen lake.** The one-page source could never be read: a native lectern shows no page buttons for a single page, and reading was only recorded on a page turn. Opening a one-page paper now reads it to the end, for its own reader only. A new native case opens it as a real lectern does.
- **Trailer door.** A round everyone left after supper froze, and every new player was turned away at the door until a member returned. The latecomer refusal now applies only while the evening is occupied. An empty one begins again on entry.
- **Trailer window run.** The extra cousin used to plan from the seat it had left and walk back through the trailer wall. Its window spots also floated, faced away from the glass and scraped while it was elsewhere. Now:
  - once it is out by the fire, it runs to the nearest window and stands on its real sill, facing in;
  - it scrapes at that glass and goes back to its place;
  - a cousin whose goal changes partway along finishes the leg it is on, rather than replanning from where it set out.
- **Older rounds.** Rounds saved before 0.4.64 keep their own evening: no request to serve packets that don't exist, and the bathroom-window line at 200.
- **Plains camera.**
  - The exposure is taken from the level before the hand and HUD are drawn, so the crosshair, chat and viewfinder never reach the map.
  - If the distant boy has not arrived at the client within twenty frames (beyond its view distance), the camera records his dark standing figure where the server says he stands.
  - A low view distance no longer withholds the photograph and memory.
- **Cabin nightstand.** A booked lectern is emptied in place after its original is archived, so it never drops a duplicate. The same applies to the 0.4.55 source repair. The repair also accepts the notes the 0.4.55 pass may already have laid on the old lectern, rather than waiting forever. It now uses the 0.4.55 guards, including costume stands and the lake witch.
- **Rail hop.**
  - A hop that falls short is remembered for ten seconds, so the blocked-route recovery plans around that rail instead of hopping in place.
  - The hop tears through the foliage it was judged to pass, as a step does.
  - The killer stands again when a hop ends or is cancelled.
  - The rail is checked first, before the pathfinder's support reads.
- **Glazing.** Portholes keep glass sounds but return to the approved break time (about nine seconds, tool or not). Shattered and whole panes each draw their shared faces.
- **Tests and tools.**
  - The rail-hop case restores the corridor and deck it builds outside the elk room after its batch.
  - The multiplayer-story cases, whose held sections load one after another, wait two minutes before starting instead of one. The assertions are unchanged.
  - `build_novel_assets.py` no longer overwrites the 0.4.64 window, paper, key and ribbon art.
  - `generate_playtest_assets.py` draws the franks package sprite.

## Validation

Candidate validation is pending. The full suite now has 480 native cases. Required checks include the full native gameplay suite, focused literary and multiplayer suites, two actual clients mining/crawling through persistent shattered frames, native rail-vault motion, two actual clients developing their independently captured camera frames, all resource PNG/JSON checks, client book-visibility and native collection checks, complete writing and native visual proofs. The existing Elk visual proof includes intact and shattered variants of both yacht glazing blocks.

Layout remains **38**. Network protocol is **38**; use matching **0.4.64** clients and server. The quota remains **43 sources / 33 personal resolutions across two kinds**, with three endings and the original opening pace.
