# Clarity and expedition rhythm, 0.4.57

This pass implements the next four agreed items from [the player experience plan](PLAYER_EXPERIENCE_PLAN.md), on verified 0.4.56. Native validation of this exact source is required before release.

## What changes

- **Refusals explain the next action.** Loose leaves distinguish missing bindings, missing carried originals, earlier leaves and already bound or burned stories. Their read-only preview remains available. Kitchen, laundry, drawer and late-kitchen chores name the missing task. The finale lectern and Witness approach distinguish unfinished personal accounts from an unread passage. Companion commands explain reach, ownership and unavailable routes. Chalk explains unsupported or occupied placement and refuses to erase someone else's mark without spending durability.
- **Retreat gets one truthful explanation.** A living explorer receives one short line near a story's actual entry, while the exit is still available. It is saved per player, including private copied homes. Held games, committed stories, the locked well and the Goatman encounter do not promise an open exit. The hint awards no progression.
- **Completion earns a breather.** A new personal Witness resolution or successful traversal through a physical hazard's far door queues one saved calm deal. Retreat, observation and replayed story credit do not queue it. On the next fresh ordinary or quiet arrival with no previous shared discovery, all newly drawn choices are quiet rooms or ordinary halls, favouring short halls. Discovered personal maps and shared routes stay exact; scent and active pet rescue keep priority. A delayed breather survives reconnect. It does not stack, grant credit or change encounter configuration.
- **Familiar halls can change unseen.** The alcove hall gains a supported picture, the stone arcade a bracket lamp, and the stone landing a small rug at the side. After staging on a real visit and two further occupied returns, the picture faces the wall, the lamp goes out or the rug corner folds. Staging and change both require already loaded chunks and entity sections, no living resident, and every camera—including spectators—at least 48 blocks away. A removed, rotated or replaced piece stays edited; saved checkpoints prevent refilling. Routes, original containers and finite contents are untouched. Six native meshes and all 24 horizontal states use existing vanilla textures.

The opt-in local log adds `hazard_resolve`, `retreat_guidance` and actual-site refusals for these actions. Existing measurements and personal account text remain.

## Preserved rules and remaining work

Layout 35 / protocol 34; matching 0.4.57 client and server JARs. The pool remains forty-three eligible sources, thirty-three personal resolutions across two kinds, and three endings. Original actor and companion identity, health, orders, finite supplies, exact books, personal progression, completed endings and established darkness remain.

The full custody/recovery audit, solo/duo human playtests, survey, keeping shelf, personal callbacks, session escalation and optional cooperative moments remain in the plan. Opening pace, quota and darkness changes remain decisions informed by playtests. This pass does not replenish lost finite items.

## Required evidence

- All 428 declared native gameplay cases; 38 focused exploration cases, including eleven new regressions for personal completion, saved/known/shared routes, search priority, truthful retreat, spectator/pet vacancy, original edits, supported meshes, chalk and companion refusals.
- Every existing focused suite: 41 multiplayer, nine cave, ten staircase, sixteen literary, seven hotel, three hallway and four seam cases; seventeen measurement regressions.
- Complete native writing proof over 1,334 pages, all original scene/asset checks, 69 architecture views, 90 native views including the new hallway mesh sheet, and package/version/resource checks.
- A dedicated-server expedition with two actual socket clients and two same-profile reconnects. Candidate JARs from the main and independent seam builds must agree exactly.

The release notes record the actual source SHA, native run and resulting JAR checksum after these checks finish.
