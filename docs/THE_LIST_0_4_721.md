# The list, to its end (0.4.721)

The owner, on 18 November: "Please focus on the skeleton. Get it completable. We'll polish animations and textures later." So this part
takes *The list* from where night two's field leaves it to an end, an account and a Witness story, in skeleton form. Night two's later
tests (the field house's door, the tower), night three's town out with flashlights and night four's plan (`docs/THE_LIST_PLAN.md`, parts
5 to 9) are not built; what is built gives each of them a night to go in.

## The nights

1. **The first name.** Five seconds after Amber writes the first rule down she says the first one is out, and night two's hunt begins
   (`TheListNights.HUNT2`): the hunt of part 4 on night two. Night two ends when the one who went in lies down on their own bed, or at
   first light.
2. **Night three.** The one who went in, back in the town, begins night three at ten in their kitchen with the hay hook back and the body
   theirs; the hunt begins at once (`HUNT3`). Bed or first light ends it.
3. **Night four.** The same, and the gin runs (the auger and the press are night four's). It ends at bed, at first light, or once all five
   are dead.
4. **The end** (`OVER`). The body is the one who went in's own again. Every reader in the town, and the one who went in wherever they
   are, is ready; the account is on the tape deck in the reader's room. Reading the pad by the phone and then the account gives each
   reader the Witness story, as for any other source. Spectators earn nothing.

The town keeps who of the five is dead from one night to the next: one dead on an earlier night is not made again, by the town or by
the next hunt. A town whose four nights are over, come into by a reader who has not had one and nobody else in it, begins again round
them.

Operators: `/ghosthouse test the_list night three` and `night four` begin those nights round the operator.

## What is kept

- **Each reader's own:** how many nights they have had (`Night`, 1 to 4), whom they finished (`Kills`), the rule in the notebook
  (`Rules`), whether they were the one who went in (`WentIn`), ready and outcome. Outcomes: `went_down_the_whole_list`,
  `crossed_names_off`, `crossed_no_names_off`, and `was_in_town_at_the_end` for anyone else.
- **The town's own:** the night, and who of the five is dead (`Dead`).

## Witness

*The list* is a Witness source of the memory kind: pool 95, still 33 required (capped), two kinds, three endings. Its House line in the
Pell journal, its account advancement, its figure on the great staircase (Amber, with her notebook) and its together line (from 0.4.699)
are in place.

## Every new line

Amber, over her head, when the first name goes out:

- "That's the first one. They're out there now. Go on."

Grey cues on the action bar:

- "Across town a porch light goes off." (the one who went in, as the first name goes out)
- "Somewhere a screen door bangs, and somebody starts running." (anyone else)
- "Ten o'clock. The hay hook is on the kitchen table where you didn't leave it." (night three)
- "Ten o'clock. Over the highway the gin is running. You can hear it from the kitchen." (night four)
- "It gets light over the gin, and the hands are your own again. The tape deck in your room is running." (the end, the one who went in)
- "It gets light over the gin. Nobody is out on the streets yet." (the end, anyone else)

The account, on the tape deck:

- "Four nights running it was ten o'clock when I came in. The first night Deek took me to the gin and shut me in the seed house, and I
  came to under the water tower with a sack over my head."
- "I had the hook in my hand. At the end every name on the list was still walking around town." (none finished)
- "There were five names on the list. By the last morning I had been to every one of them." (all five)
- "There were five names on the list. By the last morning I had been to …." (those finished, by name)
- "Amber wrote one rule in her notebook from the field, in her own hand: he can't run, and doesn't need to." (the first rule written)
- "Somebody else had the sack and the hook. I was in town when it got light the last time." (anyone else)
- "The list was five names long."

The Witness line: "Four nights in a cotton town I wore a sack over my head and carried a hay hook, and there were five names on a list."
The House's line in the Pell journal: "I went down the list myself, one name a night."

## Checked

`TheListTests.toTheEnd`: two readers and a spectator from night two's first name to the end; the bed ending night two; Wes, dead the
night before, not made again on night three; the gin on night four; both readers ready with their own outcome and account; each reading
the pad and the account and holding the story; the spectator holding nothing.
