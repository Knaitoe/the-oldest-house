# House exit repair — 0.4.39

The 0.4.38 playtest reported a long pause and a door that did not open visibly while leaving the House, plus a broken door at initial spawn. The supplied log records a 4.4-second server hitch during initial interior preparation. It records an exit request while labyrinth construction continues, but has no exit completion/client hold timings, so its subsequent log gap does not establish the duration or cause of an exit stall.

## Changes

- The held-frame screen's 1.5-second deadline previously remained gated by vanilla's readiness supplier. It now releases when that deadline passes and the destination player and level exist. It still releases earlier when the surroundings are compiled and ready.
- A door opened for a dimension crossing stays protected from the forty-tick idle closer until the player passes, leaves the area or reaches the existing 200-tick passage timeout. A player clicking from 2.85 blocks inside the front door no longer loses that protection outside the two-block idle radius.
- Native authoritative mirror writes suppress temporary neighbor shape recomputation and drops. Copying one door half before the other half/support no longer destroys the copied state while the destination is incomplete. Real player block interactions remain unchanged.
- Client frame capture/release and server transition sync/move durations are logged separately, with the transition token, to identify any remaining pause in a subsequent playtest.

Layout 32, protocol 32, all existing geometry, finite inventory/custody and personal progression remain. No new story or rebuild is introduced. The prior construction pacing fixes remain; this patch does not claim that every native scene builder or cold-chunk operation is bounded.

## Native regression checks

`SeamDoorTests` adds two engine-level cases. The first uses a real survival player with a native mock connection, posts the closed-door interaction, waits for the actual dimension crossing, checks both door halves in both dimensions after the idle timeout, checks original container custody, then walks through and verifies closure. The second copies upper/lower halves and their floor in an incomplete order, advances native ticks and checks surviving state/half identity and absence of item debris.

Complete validation requires all 323 declared native cases, two focused seam cases, three focused hallway cases, sixteen literary cases and seven hotel cases. The existing thirteen client proofs, fifty-three architecture views, three sky elevations, 434 native book pages, 226 PNG textures and package checks remain required. CI produces a focused candidate earlier; only the matching complete green run verifies the release.

The 0.4.38 baseline passed its complete 321-case suite and all existing proofs at source `4ecf8744ea0cec47f333dfb102aa77e968037743`, [run 37217585760](https://github.com/Knaitoe/the-oldest-house/actions/runs/37217585760). The new run supplies evidence for this patch. A native server test cannot reproduce this user's client/GPU held-frame timing; that limit is repaired in source and the new timing messages support direct playtesting.
