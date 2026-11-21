# The list, part 8b: a door held, a heart kept quiet, the slasher put down (0.4.730)

More of Amber's side (`docs/THE_LIST_PLAN.md`, part 8), after warning names off (`docs/THE_LIST_0_4_729.md`). Each is for a reader in
town who is not the one who went in, during a hunt. No protocol, layout or Witness change.

## Barricading

A hand on a shut door the one who went in could strike (a neighbor's locked house door, or a steel door one of the five has bolted)
braces it: "You get your shoulder against the door and your feet set." Each reader who braces it and stays within four blocks of it,
alive, adds three blows before it gives, up to two readers (six). Past the door's own blows, each is held: "The door jumps in its frame.
Somebody on this side is holding it." A reader who steps away no longer holds it. The bracing goes when the door gives or the hunt ends.
A second hand from the same reader: "You're already holding it."; a third reader: "There's no room on the door for anyone else."

## Knocking the slasher down

A blow from such a reader on the one who went in puts the body down for its five seconds, as a blow from one of the five does: the one
who went in is told "Something catches you across the back of the head."; the one who did it, "It goes down in the dirt." Then thirty
seconds before that reader can do it again ("Your arm won't come up again yet.").

## Keeping a heart quiet

Crouched within two and a half blocks of one of the five who is hidden or cornered, such a reader keeps that heart from the one who went
in, and the pull with it, for as long as they stay. The first time each reader does so for each name in a hunt: "You get down next to
Mel and put a hand flat on their back. The breathing slows."

## The account

For a reader on Amber's side, as was so: "I held a door shut once with my back against it while it shook." (or how many times); "Once I
hit it from behind as hard as I could and it went down. It got up." (or how many times); "I lay next to Mel in the dark and kept a hand
on them until the breathing slowed." (whose).

## Single and multiplayer

- **Alone**, none of this happens: there is nobody on Amber's side.
- **With others**, each reader keeps only what they did; two can hold one door; anyone can put the slasher down, each on their own
  wait. **Spectators** brace nothing, put nobody down and keep nothing.

## Not built yet

Testing the rules for real, and the rest of what is kept (part 9).

## Made as

- **`TheListAmber`** (new): `BRACE` 3, `MOST` 2, `AGAIN` 600, `HUSH` 2.5, `REACH` 4; `side`, `extra`, `braces`, `brace`, `unbrace`,
  `clear`, `knock` (and the attack event), `hushes`, `hushed`, `rest`. Kept while the server runs, never saved.
- **`TheListBody.strike`**: a door's blows to give are its own plus its bracing; the last cue waits for the last blow. The hearts and
  the pull leave out a kept-quiet heart. **`TheListHunt`**: each runner's `hush` and `hushers`, worked out each tick; `hushed`;
  `hideNow` for checks. **`TheList.click`**: a hand on such a door braces it. **`TheListTexts`**: the cues above; the account's lines.

## Tests

- `TheListTests.amberSide` (new, batch `the_list_amber`): night two's hunt with the one who went in, two readers on Amber's side and a
  spectator. Both brace a neighbor's door (not twice; the spectator not at all); it takes nine blows, the fourth on held, and the
  bracing goes. One puts the slasher down and cannot again at once; the spectator cannot. The other crouches by Mel, hidden: her heart
  is kept from the pull until they move off. Each record and account keeps its own.
- `TheListTests.theHunt`: the other reader's hand on the bolted steel braces it, and stepped away no longer holds it.

## Every new line

Grey cues, as above.
