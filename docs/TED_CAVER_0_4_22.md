# The small passage — 0.4.22

A playable vignette inspired by the excavation, claustrophobia and uncertain return in *Ted the Caver*. The field notebook, cave layout and encounter text are original to this mod. The cave never identifies what is beyond the stone.

## Play

The dealer can offer `ted_caver` at the usual six-crossing story depth, with the existing deliberate-scent exception. The room is a resumable personal vignette. Arriving reveals an ordinary camp: a lamp, provisions, an equipment barrel, a field notebook and a rope beside a ladder.

Read or take the notebook at the lectern. Its initial pages explain the route and the controls. There is one iron pickaxe and a small finite supply of food and torches in the barrel. Native left-click or right-click with any pickaxe works the cracked block below the shaft. Twenty-four strokes, at least twenty-five ticks apart, gradually open it. Work is shared and saved; repeated clicks and simultaneous hands cannot accelerate it. The opening remains open for later explorers.

Crouch at either mouth of the narrow passage to crawl. The passage is physically one block high and one block wide. A temporary native swimming pose supplies the crawling collision box and synchronized visual pose. The chamber at the far end is tall enough to stand. Examine the chiseled mark on the left wall, then the smooth stone opposite. The stone rolls away from a smaller passage. Later explorers inspect the shifted stone themselves; another player's interaction does not write their account.

Remain in the low chamber beyond the stone long enough for the draught to change and the line to tighten. The notebook then describes the retreat. Stone scraping comes from behind, and occasional small native motion impulses pull toward the chamber. Return through the original squeeze and climb the actual ladder above the rope. The return is survivable; this vignette does not script a taking or automatically kill its explorer.

Companions refuse the shaft and wait at the safe landing. The same native pet keeps its UUID, owner, health and selected order. Sitting, leashed and passenger pets are excluded from the temporary refusal. When the explorer leaves the shaft area, the normal selected order resumes.

## Journal and personal resolution

The notebook has nine progressively available original pages. Each reader uses an independent native book menu. Its one collected original updates only in that owner's inventory or cursor as their exploration develops. It remains a legitimate vignette yield for Hillary and a Mother bargain, while a borrowed or traded copy transfers no story progress. Collection stays finite across save/reload, and a lost copy can still be read at the camp lectern.

Personal progress records work, the inward crawl, inspection of the mark and stone, time in the low chamber, the return crawl and the final climb. Only the last physical return grants the `ted_caver` survival resolution, once. Arrival, shared excavation, looting, witnessing another explorer, the chamber alone, spectators, and disconnected players receive no credit. Interrupted exploration resumes with its saved observations. A short pair of private scraping echoes later in the House adds the last undated journal entry; they do not force a return or change ending credit.

The eligible Witness pool grows from ten to eleven stories. The automatically derived quota is now **nine distinct resolutions across at least two kinds**. The three ending options remain. Existing evidence, deliberate reading and completed endings are preserved; an uncommitted eight-source account needs one additional resolution.

## Upgrade and verification

Layout 20 appends slot 32. All prior slot coordinates and leak ordinals remain stable; the dry stone hint appends ordinal 12. Upgrading a version-19 world does not rebuild old actors, vignettes, furniture, containers or caches. Saved excavation, shifted-stone state, reader progress and finite tool supply are retained. Native crawling restores the prior forced pose on leaving, logout, spectator mode, native death, reset and server stop.

Six native GameTests cover slot bounds, actual timed pickaxe events, independent finite journal menus, native collision through the whole inward/return squeeze and real ladder, personal multiplayer credit, saved-data reload, pose cleanup, companion identity and an upgrade preserving old storage and scene evidence. Witness fixtures check the eight/nine boundary, quota derivation, reload and final account directions.

Operator checks: `/oldesthouse door ted_caver` places a test entrance; `/oldesthouse vignette ted_caver status` reports saved work and the operator's personal progress. Use the ordinary door to enter and the notebook for controls. `/oldesthouse labyrinth build` is an explicit destructive operator rebuild, not the automatic upgrade path.

The package is checked by the JDK 21 build and native required GameTest suite. Client visual/audio feel still needs an in-game playthrough.
