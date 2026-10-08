# Combined hunt update — 0.4.60

This release combines the verified Stacy/slasher release (source `072cb10865285f2dbb57ac473942f3725c90fc91`) with Claude's verified two-stage elk rework and subsequent repairs (branch head `a2273a06a13122ae4305fb576b001084a71a9ce7`). Earlier player-experience changes are already present and are not applied again. Exact-head verification of this combined candidate remains required.

## Included gameplay

All of 0.4.59's Stacy behavior remains: doubled hunting movement, physical tight-gap and open-door navigation, bounded unsuccessful cover searches, ordinary wooden-door cracking, supported hiding pockets, passing hand swipes, a one-to-three-second pause on reaching cover, jointed scrabbling, and synced hand-rake or opening-jaw attacks. The recalled girl and original skins remain unchanged.

Claude's elk scene replaces the old field with the yacht, murdered passengers, water escape, stream valley, construction site, concealed cave, articulated carcass pile and crew's gate. The source retains its story ID, kind and account. Its personal resolution still requires reading the source, holding still through the actual search, seeing the boots and reaching and reading the ending at the gate. The service door provides a second return route. The yacht, site, bodies, carcasses and killer use Claude's integrated assets; the new block materials have vanilla pixel density. See [the scene specification](ELK_CARCASSES_0_4_50.md).

## Shared and private control

Stacy and Camp Blood each retain one physical actor and one native actor clock. Camp Blood uses the bounded sight/sound path search and physical leaf breaking from 0.4.59. Its personal server loop evaluates each reader's escape; it never moves or strikes with the shared actor again for each peer. Additional guarded low leaf pockets provide actual crouching clearance.

The elk killers are private per reader, as Claude designed. An owned elk killer cannot also run the earlier shared carcass controller. Its single native movement driver keeps terrain navigation, adds a bounded physical foliage/low-cover fallback, and ends a failed route after four occupied seconds. Recovery abandons a route rather than teleporting to its target or granting a completed search. Offline, departed and spectator owners cannot advance the saved movement clock. Native interaction noises remember only their actual position for that reader's killer. Ordinary close attacks require actual line of sight; the authored reaching attack at the carcass hollow remains.

Leaves are shared physical scenery. A killer may tear only foliage intersecting its actual next body step, subject to mob griefing and native destruction vetoes. Other readers see that world change, but receive no personal hunt facts or ending evidence from it. Solid walls, protected doors and containers remain outside foliage clearance. The new killer's watching, searching, running, crouched and chopping poses coexist with native crouching under low cover.

## Old worlds and custody

Layout 36 / protocol 34. Layouts 32–35 receive Claude's elk rebuild after other work, only when the native chunks and entity sections are ready and all actual player cameras are away. Every execution slice rechecks vacancy, including a player returning during construction. Old forest and reading-surface dressing cannot overwrite the new yacht/cave.

Native pets, residents and finite dropped items receive saved temporary protection for the whole sliced rebuild, preventing burial damage, falls or item expiry between slices. Settling restores original physics, AI, vulnerability and dropped-item age; health, ownership, orders, UUIDs and components stay with the same objects. A save midway retains this protection until the rebuild finishes. The old shared killer keeps his identity and is adopted by the first personal reader afterwards. Existing reader originals, facts, outcomes, depleted supplies and completed endings are preserved.

No source is added: 43 eligible sources, 33 personal resolutions across two kinds, three endings. Witness difficulty, the three-day/two-sleep opening and deliberate dark sequences retain the owner's decisions. Novel correspondence, all sixteen blind scribbles, Ted permissions, finale testing routes and every intervening repair remain included. The unfinished survey/evidence prototype stays separate.

## Verification contract

Current local checks pass: all 311 PNGs decode with valid CRCs, eight font grids pass, all 17 journey-log regressions pass, 559 JSON resources parse, all 56 sound events have subtitles, and the 46 novel finds and sixteen blind scribbles remain byte exact against 0.4.59. The Java 21 / Gradle 9.2.1 build stops before compilation because dependency resolution reports `java.net.SocketException: Network is unreachable`. No combined runtime JAR, native test result or client proof is claimed yet; the complete checks below remain pending.

Require all 452 declared native GameTests, all eight focused suites (including 60 multiplayer cases), the 17 journey-log regressions, the full original/novel writing corpus, eight font grids, the playable package and every prior native client proof. Claude's native elk cast sheet joins Stacy's articulated attack sheet, and the two new elk views bring the architecture proof to 71 views. All 311 native PNGs must decode; the only altered original PNG is Claude's approved killer atlas.

The native private-foliage case checks physical movement, leaf clearance, ownership, solid-wall preservation, peer isolation and exclusion of the shared controller. The private-clock case checks observer pause, native save/reload and four-second failure without relocation. Existing shared slasher cases now exercise Camp Blood. The elk upgrade case also checks saved protection and restoration of a native pet and finite item.

The two actual socket clients keep both same-profile reconnects and every prior door, book, private note room, Stacy door-break, crouching and shared-leaf test. They additionally receive two distinct owned elk killers: each actual native renderer must expose exactly its own actor. One killer then physically clears a leaf passage while the other's identity and waiting position remain unchanged. Neither player receives ending credit from these world changes.

Minecraft 1.21.1 / NeoForge 21.1.251; install the same 0.4.60 JAR on the server and every client.
