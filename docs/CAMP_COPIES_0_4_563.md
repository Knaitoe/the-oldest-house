# A camp each: Camp Blood by copy (0.4.563)

The owner, 13 November: "All multiplayer vignettes should be hooked through that system. If full".

Camp Blood has been one camp for everyone: one man walking one schedule, with every reader in the camp his to hunt, and one mess-hall
bell for all of them. This release gives the camp copies and a number of places. It is the last of the shared stories to get them
(see "Decided here" for the hotel).

New builds only. No layout, protocol or Witness change.

## Full

| Story | Places | Why |
|---|---|---|
| Camp Blood | 6 | a counselor to each of the six cabins |

A camp's places are held by the readers in it now. The camp keeps no roll, so a reader who walks out keeps no place.

- The seventh reader through the door goes to the next camp, built whole a million blocks west by the camp's own builder and dressed by
  the same passes as the first. Their door waits while it is made ready; it is made ready ahead when a camp is down to its last place.
- **Each camp has its own man**, saved under the camp's own key (`LiteraryVignettes.actorId`: the first camp's as it always was, copy k's
  as `Killer@k`). His schedule is kept on him, so each camp keeps its own day: reveille, breakfast, inspection, crafts, campfire and
  lights out, each at its own time.
- **He walks, looks and listens in his own camp** (`CarcassHunt.baseOf`): his walks, his patrols and where a reader can hide from him are
  measured from his own camp, and only the readers in his camp are his. A reader in another camp is nobody to him.
- **The bell** in each camp calls that camp's man and nobody else's.
- A camp's day runs only while somebody is in it, as before.
- A reader's own part is theirs wherever they are, as before: what they have read, whether they were chased, whether they rang the bell.
- The accounts already count company within one copy (0.4.547).

## The case

`LiteraryTests.aSeventhReaderIsGivenACampOfTheirOwn`: six readers at Camp Blood are full to a seventh and never to one of the six. The
seventh's door waits while a second camp is made ready, a million blocks west, and they go in by its own door. The second camp's bell,
schedule stand, clipboard, radio, PA horn, journals, papers and every bunk stand where the first camp's do. Each camp has its own man,
walking in his own camp and keeping his own schedule. The seventh rings the second camp's bell, and it calls the second camp's man and
not the first's. When the six put the story down, the first camp's day waits, while the second's runs on for the seventh.

The Movie Night copy case (0.4.562) now also compares the second lake with the first, block for block.

## What testing found

- **The first camp in the case was not the camp the House builds.** The literary cases lay a scene down with its builder alone. The
  House then finishes every scene it builds with its passes: the polish, the composition and the review that moved the camp's paper
  from a vanilla lectern onto the camp's notice board. A copy is finished by the same passes, so the second camp's paper was on its
  notice board while the case's first camp still had the bare lectern. Nothing was wrong with the copy: the comparison was against an
  unfinished original. The case now finishes the first camp as the House does, while it is still empty, before anyone arrives, and
  then compares them.

## Local runs

- The camp copy case, the Movie Night copy case, every slasher case, and the scene-review and Truant cases that use the camp's man:
  21 of 21.
- The quick checks and the structure halves check.

## Decided here (any of it can move)

- Six places: a counselor to each cabin.
- **The hotel stays one building.** It was made for any number of guests at once, and each guest's stay has always been their own: their
  nights, the snow at their windows, their table for two, their account and their belongings in the hotel's keeping. What the guests
  share is the building: the ballroom's dancers, the bar, the bath, the boiler, the graves. Copying it would mean copying the hotel, its
  grounds and the numbered corridor's 217 together, which the copies do not yet do (one place to a copy). Say if a full hotel should be
  another hotel, and it will be built.
- **The Sphinx**: her halls are the House's own, and stay shared on purpose.
