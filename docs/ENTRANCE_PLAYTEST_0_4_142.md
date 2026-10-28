# Entrance playtest repairs — 0.4.142

Five physical doors stand along the original approach, 36, 29, 22, 15 and 8 blocks before the labyrinth far door. In order they lead to the waiting room, bathroom before bed, coatroom, lakeside shelter and porch. Each reader retains their own existing saved copy. Both the entrance and the quiet way on return to the same approach doorway; the five moments no longer intercept ordinary or operator scene routes. No new slot, copied room address, protocol, Witness source or classifier is introduced.

A missing boundary registration affected the earlier private rooms: the normal player tick could treat a quiet room as a wall breach and send its reader to the Overworld at the private room coordinates. An ordinary `cross_hall` door could previously offer such a room, making that failure appear to belong to `cross_hall`. The complete native House tick now recognizes built quiet rooms, including observers, while owner checks continue to gate interactions. This source finding fits the reported death; the native reproduction must establish it before the repair is considered verified.

Older active interleaved visits keep their stored source and next ordinary destination. The original two-reader continuation case remains as an explicit saved-visit regression. Removed entrance doors and edited walls are not rebuilt by the once-only installer. Fresh and loaded, vacant original wall cells receive the native doors; the existing opening remains required.

Two added native regressions run the full House boundary tick rather than only advancing pending fades. One physically enters all five stories, idles after the fade with a real observer, reloads saved state and retreats to each original entrance. The other uses an actual `cross_hall` command door, moves a survival player through all three branches and checks health, possessions and its one original return. A negative-control run removes only the new containment guard and must reproduce the idle-room boundary failure. All original quiet-room cases and deadlines remain.

Validation is pending. These changes do not establish a verified release. The broader PR remains draft.
