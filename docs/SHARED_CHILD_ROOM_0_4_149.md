# The child's room, together — 0.4.149

The owner's October 28 direction: the child's room can be shared, and every shared vignette's account must be drafted for each way a reader can have been part of it. The room was already one room for everyone in it: the windows and doors go once, unseen, for whoever is there, and the toys and the mobile over the bed are the same for all. What a reader earns was always their own: each must look at every sealed exit and crawl down for themselves (since 0.4.37). This release keeps that and writes each reader's account for how they were there.

## Everyone in the room is shut in

The paw has one holder and the drowned town has one reader who opens the roof, so the others there are witnesses. The child's room has no witnesses. Everyone who has read the card is shut in, the doorway back to the House is wall to them, and the only way out is their own crawl beneath the bed. So:

- **Nobody is credited for another's crawl.** The child's room is not drafted with `SharedVignettes.draft`; when one reader reads the last account, nobody else in the room is resolved by it.
- **Each reader resolves it as the one who did it,** when they read the account in the basement themselves.
- **Friends still go in together.** The room is followable, so a friend through the same door arrives in the same room (`docs/COOP_FOUNDATION_0_4_146.md`).

## An account for how each reader was there

While the room closes around a reader, the House notes everyone else living in the room with them, in the order they were first there together. When the reader stands up in the basement, it notes who of them had already gone beneath the bed and who was still in the room above. Spectators are never noted.

The account in the basement then reads, in the reader's own words:

- **Who began it.** A reader who was there from the start, with others, is told who opened the bedtime card: "It began when I opened the bedtime card," or "It began when <name> opened the bedtime card."
- **Who was in the room.** From the start: "The windows went first. Then the doors. <names> were in the room with me, and none of us saw one go."
- **Coming in late.** A reader who came in after the exits had begun to go says how far it had gone ("When I came in, the windows had already gone. The doors went while I was there.", up to "every way out of the room was already wall") and who was in there with them.
- **Following someone down.** "<names> had gone beneath the bed before me. I crawled along the clean lines their bodies had left in the dust, on a strip of floor that still led somewhere."
- **Leaving someone behind.** "Above me, <names> were still in the room."

Other readers are named by their player name. When everyone named in one sentence is everyone named in the sentence before, the account says "they" instead of naming them again.

A reader alone, there from the start, reads the account the room always gave: "The windows went first. Then the doors. I crawled beneath the bed and found a strip of floor that still led somewhere. I stood up only after I had left it."

## The Witness line

The room's line in the Witness account follows the same facts, briefly:

- with others: "I was in the child's room with <names> when it closed.";
- late: "I came into the child's room after its windows had begun to go." (or "after its windows and doors had gone");
- followed: "I followed <names> below the bed.";
- always ending "I found a floor that still led somewhere."

A reader alone keeps the line it always had.

## Saved worlds

A reader who finished before 0.4.149 keeps their account and Witness line word for word. The once-only rebuild of an older room (`child_room_0474`) also forgets the new notes for readers who have not finished, and who opened the card, so a rebuilt room starts its notes afresh.

## Tests

One native case joins the co-op cases, which now run five (the quiet-room job runs 22). Two readers are in the room from the start and a spectator watches; the first opens the card, and the room closes around them unseen. A third reader comes in after every exit has gone. Each reads the card and looks at every sealed exit for themselves, then they crawl down one at a time while the others wait upstairs:

- each resolves the room as the one who did it, on their own reading, and nobody still upstairs is credited for another's crawl;
- the first opened the card, went under first and left the other two above;
- the second followed the first down and left the third above;
- the third came into a shut room and followed both;
- each Witness line says who was there, whom they followed, or that they came in late;
- the spectator is in nobody's account and earns nothing;
- a reader who finished before 0.4.149 keeps their words.

The existing child's room cases (the unseen sequence and personal crawl, the late reader and the once-only rebuild) pass unchanged.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
