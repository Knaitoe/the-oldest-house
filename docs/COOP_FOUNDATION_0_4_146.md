# Going in together — 0.4.146

The owner's October 28 direction: two or more players can go into a vignette together. Most vignettes are shared, a reader who watches gets their own account and the Witness credit, and multi-visit vignettes follow one story per thread. This release builds the shared part that every vignette uses. Making each scene itself shared (the paw first, then the child's room, the drowned town and the Goatman's campers) follows scene by scene, because each one needs its own drafted accounts.

## Following a friend through a door

- **The same scene.** When a reader goes through a door into a shared vignette, the door is held open behind them for 30 seconds. A friend who goes through the same door in that time arrives in the same scene, whatever that door had been dealt for them. Two readers who walk through together always end up in the same room.
- **Each reader plays it as their own.** Following changes only where the friend arrives. Everything they do in the scene is still their own: their papers, their choices, their own account if they do the key act themselves. A peer's act never counts as the reader's own act.
- **Who can follow.** A friend follows unless they have already finished that vignette or belong to a different thread of it. In either case they go through their own door instead.
- **Mixed groups.** If the first reader through has friends standing at the door with them (within four blocks), and some of those friends cannot share what lies behind it, the House deals that door a vignette none of them has started. If there is no such vignette, the group splits: the first reader keeps their own door, and each friend gets theirs.
- **Who counts as a friend at the door.** Living players within four blocks. Spectators are never part of a group.

## Which scenes are shared

- **Private.** The backseat (one driver's seat), the silo, Borrasca, the family and old-cabin copies and the Red Room stay one reader's own. The five quiet rooms are never dealt, so they are untouched. Since 0.4.151, Harrigan, the confession, the Holy Rabbit, the Wheel house, the Whale institute, the phone in the canoe, the diner and the split-feed bedroom are private too, and one reader's at a time (`docs/PRIVATE_SCENES_0_4_151.md`).
- **Not yet.** The Goatman's headcount needs players to take the place of cousins, and the hide-and-clap game has one blindfold. Until those scenes are made shared, a group is never led into them: a group at their door is dealt a scene everyone can share, or splits. The Goatman is shared from 0.4.150; hide-and-clap became a private scene with copies in 0.4.158 (`docs/HIDE_AND_CLAP_COPIES_0_4_158.md`), so nothing is held back any more.
- **Everything else** is shared: friends follow each other in.

## One story per thread

Every shared vignette now keeps threads, saved with the House:

- **A thread** is the readers present on a vignette's first visit. Each reader is in at most one thread per vignette.
- **Picking it up.** A reader who comes back belongs to the thread they started. A visit begins when someone from the thread arrives and nobody from it is there yet.
- **Late joiners.** A reader new to the vignette who arrives while a thread member is there joins that thread. If the thread is past its first visit, they join it partway.
- **A beat that needs one person.** A scene can say its next beat needs a particular member, such as the holder of the paw. Until they come, the House does not deal that vignette to the other members alone. The others can still follow the holder in.
- **A holder who never returns.** If the holder of a thread's one object stays away for three in-game days, the scene takes the object back where it came from, so no thread is stuck.

## An account for each way of being there

When a reader ends a shared story as its actor, the thread's ending is recorded with who acted. Then:

- **Watched.** A member there throughout gets their own account and the Witness credit.
- **Came in partway.** A member who joined partway gets a late joiner's account and the credit.
- **Missed it.** A member who was elsewhere gets the aftermath account and the credit the next time they come into the scene.
- **Saw the consequence.** A scene can also give the "absent" account, for a reader who comes back to the result of something done while they were away.

Each of these accounts is prose drafted for that scene and that way of being there. It is not the actor's account with the subject swapped. Another reader is named by their player name, and every reader is still "you" in their own account. That writing is the main cost of sharing a scene, so this credit applies only to scenes whose accounts are drafted. No scene is drafted in this release; the monkey's paw is next. Until a scene is drafted, friends still go in together and each reader earns their own account by acting, as before.

The Witness record keeps each reader's role beside the story. Every account written before 0.4.146 is an actor's.

## Tests

Three native cases in a new co-op job namespace, run with the quiet-room job, which now runs 20:

- **Following.** Through a real hall door, a friend follows the first reader into the Hill nursery instead of the hall they had been dealt. They share one thread, both from its first visit. A friend who finished the nursery goes on through their own door and joins no thread. The thread survives a reload.
- **Mixed groups.** A friend at the door who finished the nursery gets the group a scene neither has started. With nothing left that neither has started, the first reader keeps their own door. A friend who can share the scene changes nothing. A group at the Goatman's door is dealt a shared scene instead. A spectator at the door is not part of the group.
- **One thread, every way of being there.** With a scene drafted for the fixture, three readers share a first visit. On a later visit a newcomer joins partway, and a spectator joins no thread. The holder's beat waits for them, and the object goes back once they have been gone too long. When the actor ends it, a member there throughout and the newcomer each read their own words, naming who acted. The absent member reads the aftermath when they come back. Another reader's act never marks the scene as this reader's own. Roles and the ended thread survive a reload.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
