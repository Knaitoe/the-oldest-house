# Player-experience integration, 0.4.56

This candidate ports Claude's reviewed work from `1212a7a3d967dba965f5c01f86afb2d8ac0d80f0` onto the verified 0.4.55 development tree. It implements the first clarity, accessibility, measurement and progress pass in `PLAYER_EXPERIENCE_PLAN.md`. The remaining workstreams retain proposal status.

## Implemented

- All 56 sound events have descriptive English subtitles. Package validation rejects any missing event or translation. Native creature voices remain audible in the staircase.
- Construction delays explain that the room is settling. Three attempts at the same physical door within a real-time minute add practical guidance. Attempts at other doors or after reconnect do not carry that count.
- Dormant doors name their room and explain rediscovery. Hide-and-clap's held doors and permanently occupied rooms explain the refusal. The church correctly names essays before the key; hearths explain their actual prerequisite.
- The personal Witness account includes a prose resolution count, broad milestones derived from the unchanged quota, unfinished entries based on actual personal beginnings/previous discoveries, and a hint when another kind of ending is missing. New beginnings confer no credit. Spectators, shared completion and borrowed accounts confer none. Carried accounts update on beginnings, resolutions and login; stored account copies retain their existing behavior.
- The opt-in server log stays local and defaults to hashed player identifiers. Every event records actual dimension, connection, place, occupancy and activity. Position/activity changes are sampled once a second, with a 30-second heartbeat. Only actual Witness sources are logged as stories.
- The summarizer includes the final room and session tail, excludes offline/manor time from labyrinth hours, retains known evidence after a missing logout, measures empty and single-beat expeditions, and counts separate refusal episodes by actual gate and connection. Quiet intervals exclude presence inside unfinished stories and personal note scenes. Legacy occupancy is explicitly marked as inferred. JSON output is available with `--json`.
- Preset migration runs once. Existing carved-place records survive layout upgrades. Construction checkpoints prevent deferred/restarted repair sweeps from restaging removed props, including legacy records that already entered their sweep.
- Private note scenes refuse offhand use, mining, placement and left clicks. Player-owned saves decide recovery; stale world snapshots cannot overwrite later player position/inventory. Room records retain only return pose. The late kitchen uses completed cup placement; idle lookups are cached and stopped engines released.
- Falling masonry keeps mounted attachments. Older House of Leaves title pages retain their exact original words. Tom's finite handoff uses the correct saved reward flag.

## Deferred decisions and work

A missing carried lighter or empty key desk does not prove loss: the original may be stored, dropped, carried by a peer, or saved with an offline player. The proposed unlimited lighter replacement is excluded. Church-key recovery and other substitutes need a custody-preserving policy; no finite items are replenished here.

Quota, opening wait, content freeze and darkness changes remain undecided. The current 43 eligible sources, 33 required personal resolutions, at least two kinds, three endings, layout 35 and protocol 34 remain. The opaque covered-well blackout and enclosed staircase remain exact. Survey maps, new co-op beats, manor displays, pacing/breathers, callbacks and reading options remain future work.

## Validation required on the exact candidate

- All 417 declared native gameplay cases and every focused suite, including 27 exploration, 41 multiplayer, nine cave and ten staircase cases.
- Seventeen journey-metric regressions, including all five defects reproduced in the review.
- Native rendering/wrapping of the new count, kind and unfinished account pages; all prior writing, atlas, texture, package, model and architecture proofs.
- All 69 architecture views, previous scene/hunt assets and absolute covered-well black-pixel proof.
- Two simultaneous real socket clients and two same-profile reconnects, with preserved movement and note-scene recovery.

Exact-source CI is pending. A successful focused job alone is not release verification.
