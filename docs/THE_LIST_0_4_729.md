# The list, part 8a: warned off, and everyone gone (0.4.729)

The first of Amber's side (`docs/THE_LIST_PLAN.md`, part 8): another reader warns the five off and drives them out of town, and the
ending where every name gets away (`docs/THE_LIST_PROPOSAL.md`, Endings: "Everyone gone"). No protocol, layout or Witness change.

## Warned off

During any night's hunt, a hand from a reader who is not the one who went in, on one of the five who can still run (at their place,
running, hidden or cornered), warns them off. They say so over their head and make for the highway, south down Avenue K; the reader is
told "Deek goes off toward the highway." (by name). A hand on one already leaving is told so. Spectators' hands do nothing.

- **On their way.** The one who went in can still catch up: the first time, they fight back once (as when running) and go on; the next
  time, they cower where they are, to be taken as any cornered one is, unless warned off again.
- **Gone.** Five seconds or more on their way and out of sight of every player within 48 blocks, they are out of town for good: never
  made again on any night. The one who warned them is told "Nobody has seen Deek since the highway."; the one who went in, "One of the
  hearts goes quiet, a long way off."

## Everyone gone

If, on the last night, every one of the five has left town and none is dead, the night ends when the last one goes: "There's nobody
left in town to go to. You walk out onto the playa as it gets light, and the hands are your own again." The one who went in's outcome
is `everyone_got_away`.

A reader on Amber's side who got any of them out ends `got_them_out`.

## The account

- The one who went in, everyone gone: "By the last morning every one of them had left town. I walked out onto the playa and stood there
  until it was light."
- A reader on Amber's side: "I told Deek and Mel to get out of town, and nobody in Lamesa saw them again." (whom they warned), and if
  everyone went, "By the last morning there was nobody on the list left in Lamesa."

## Single and multiplayer

- **Alone**, the one who went in cannot warn anyone; the five only leave with someone on Amber's side.
- **With others**, each reader keeps only those they warned; the one who went in can catch and turn back those leaving; one who has
  left is gone for everyone. **Spectators** neither warn nor keep anything.

## Not built yet

The rest of Amber's side (hiding them, barricading, keeping a heart quiet, knocking the slasher down, testing the rules for real).

## Made as

- **`TheListHunt`**: states `LEAVING` and `LEFT`; a runner's `warnedBy` and `leaving`; `warn`, `unseen`, `leave`; `LEAVE_MIN` 100,
  `LEAVE_SEEN` 48, `OUT_OF_TOWN`; `left`, `warnedBy` for checks.
- **`TheListNight`**: `left`, saved, never made again. **`TheListNights`**: `alive` leaves them out; at first light (or when the last
  goes on night four) all five gone and none dead gives `AllGone`; outcomes `everyone_got_away` and `got_them_out`.
- **`TheListPeople.ensure`**: one gone tonight is not made again. **`TheListTexts`**: `LEAVING`, `WARNED`, `ALREADY_LEAVING`, `GOT_OUT`,
  `HEART_GONE`, `END_ALL_GONE`; the account's lines.

## Tests

`TheListTests.warnedOff` (new, batch `the_list_warn`): night four with the one who went in, two readers on Amber's side and a spectator.
The spectator's hand does nothing; the two warn off all five between them. Caught up with, Deek fights once and goes on; caught again
he cowers, and is warned off again. Out of sight, all five go; the night ends `everyone_got_away`; each warner keeps exactly whom they
got out; the spectator keeps nothing; none of the five is made again; the accounts say so.

## Every new line

Over their heads, warned off: Deek "Fine. I've got the truck. I'm gone."; Wes "Where am I supposed to go? Okay. Okay, I'm going.";
Mel "My aunt's in Big Spring. I can walk to the highway."; Lesley "I'm not waiting around to find out."; Shannon "Tell Jenna I'm going to my sister's. Tell her I'm sorry."

Grey cues: "%s goes off toward the highway."; "%s is already on the way out."; "Nobody has seen %s since the highway."; "One of the
hearts goes quiet, a long way off."; "There's nobody left in town to go to. You walk out onto the playa as it gets light, and the hands
are your own again."

The account: the lines above.
