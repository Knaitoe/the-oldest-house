# The Pell house 18: the freezer (0.4.540)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its first
two visits whole, and its last night runs to midnight in the pool and getting Tilly out (`docs/PELL_HOUSE_POOL_0_4_539.md`). This is its
eighteenth slice: the freezer. From the proposal's "3. The last night":

> - **The freezer** is unlocked on the last night. The key is on June's lanyard on the robe by the pool chair, so it can be taken while
>   she is in the water. Inside is Nessa, under frosted plastic, in the dress from the photographs, intact and still. The reader knows
>   her from every room.

and from "The last night, in play":

> - **The freezer.**
>   - **Optional.** Opening it is not needed to succeed, but it changes the account.
>   - **Bringing June:** a reader who brings June to the open freezer and says Nessa's name to her, not to Tilly, ends her calling. June
>     sits down by the freezer and stops.

And from the content lines: "Her body is shown still and intact, never wounded or decayed."

The endings come next (dawn, Gail's car, the account, the keepsake), and the account is where opening the freezer and bringing June
change what is told. A reader playing Tilly comes with co-op. The House still does not deal the house.

## The lanyard

- **In the robe's pocket.** On the last night, from when June drops her robe over the pool chair on her way into the water until she
  puts it on again, its pocket has her lanyard in it. A hand on the chair takes it: "In the robe's pocket, a lanyard with three keys on
  it. You take it." It is an item of its own (the Lanyard), one to a house, and it opens only that house's freezer.
- **One at a time.** A second hand on the chair finds "The robe's pocket is empty." A lanyard that nobody at the house has, and that is
  not lying on the lot, goes back to the pocket within a second, and is taken from anybody who carried it away. A reader with hands full
  is told so and takes nothing.
- On the visit beginning again, the lanyard is back where it was, from whoever has it.

## The freezer

- **Padlocked on every visit**, as before: "A chest freezer, humming, padlocked. On its lid a folded towel and a hairbrush with long hair
  in it." A hand on it without the lanyard gets that, and nothing else.
- **The padlock.** A hand on it with the lanyard about the reader: "You try the keys on the lanyard. The third one turns, and the padlock
  comes open." It hangs open after ("The freezer's padlock hangs open.").
- **The lid.** A hand on it then lifts the lid, with a cold breath out of it: "You lift the lid. Cold comes up out of it." and "Under
  frosted plastic, a girl lies on her back in a party dress, a paper crown flat beside her head. The girl from every photograph in the
  house. She is still." The lid stays up, against the wall ("The freezer stands open, its lid up against the wall. Cold comes up out of
  it."). A hand on it again: "She is there under the plastic, as still as before."
- Anybody can lift the lid once the padlock is open; only the lanyard opens the padlock.

## Nessa

- **In the freezer on every visit**, under the lid: on her back along it, her head at the lock end, a sheet of frosted plastic over her.
  She does not move, turn her head or breathe; her eyes are shut. Frost is in her hair. She is drawn the size Tilly is.
- **Her clothes** are the party dress and paper crown from the birthday tapes ("Every birthday, a crown."). No photograph in the house
  shows a dress, and the tapes are where the reader has seen her dressed.
- **A hand on her:** "The plastic is cold. Under it, she is colder."
- **Seen.** Readers near enough to see into the open freezer (within four and a half blocks, looking at it, with nothing in the way)
  keep that they saw her.

## Bringing June

- **Her hand.** With the freezer open and June's hands free of Tilly (somebody else has Tilly's hand, or Tilly is out), a reader's hand
  on June takes hers: "You take June's hand. She lets you." In the water the reader has to be within reach of her ("She is out in the
  water, out of reach."). One hand at a time ("She has hold of your hand.", "She is holding somebody else's hand.").
- **She follows** the way the reader walked, a step behind: through the water at a wade, up out of it at the edge, and on through the
  house. Now and then she still says the name ("Nessa?", "Nessa, love?", "Where's my Nessa?"). A reader more than five blocks off lets
  go of her ("Her hand slips out of yours. She stands where she is."); let go in the water, she goes back to calling.
- **At the freezer** (in the laundry, near it), a hand on her: she looks into it, then at the reader. "Nessa?" The reader can say the
  name to her ("Nessa." or "June. It's Nessa.") or say nothing.
- **Told the name**, June sits down on the stool beside the freezer, her hand on the plastic, her head bowed to it, and does not call
  again: "June looks down into the freezer for a long time. Then she sits on the stool beside it, her hand on the plastic, and does not
  call again." Readers near enough to hear are told so; readers out on the lot are told "In the house, June has stopped calling." A hand
  on her after: "June does not look up." She stays there for the rest of the night.
- **What it changes now:** June no longer holds Tilly or calls. A Tilly still in the water stands where she is until a hand takes hers,
  and nobody comes to take her back. What it changes in the account is the endings slice.

## What each reader keeps

- `TookLanyard`, `UnlockedFreezer`, `OpenedFreezer`, `SawNessa`;
- `TookJunesHand`, `BroughtJune` (the reader who said the name), `SawJuneSit` (anybody near enough to hear it).

## How it is made

- **`labyrinth.PellFreezer`**: the lanyard (taken from the chair on the last night, one to a house, put back when nobody has it, back with
  the visit); the freezer (padlock, lid, a hand on what is under it, who sees into it); Nessa (made in the freezer with the people, never
  twice, laid back where she lies if she is ever moved); `restart` with the visit.
- **`PellRite`**: a hand on June with the freezer open (`freezerHand`), June led along the reader's way (`juneFollows`, `wade`), the name at
  the freezer (`ask`, `named`), and June by her (`byHer`). While June is led or sat, the rite's water no longer stands her, turns her or
  has her call.
- `PellPeople`: Nessa as the cast's fifth body (phase 199, not one of the four who walk the house); gestures `JUNE_LED`, `STILL` and
  `BY_HER`; the freezer's state on the house. A hand on Nessa goes to her line, not to talk.
- `PellThing.FREEZER_OPEN`: the open freezer, hollow, its lid up; `PellRegistry.LANYARD`.
- Art: `tools/draw_pell_things.py` (the open freezer's inside, plastic and lid), `tools/draw_pell_people.py` (the lanyard),
  `tools/paint_character_skins.py` (Nessa, with the catalog in `tools/character_art_catalog.py`).
- Client: `LiteraryRenderers` draws June led and by her (on the stool), and Nessa lying in the freezer, found from the freezer's cells.

## Decided here (any of it can move)

- Nessa wears a party dress and the paper crown from the birthday tapes: no photograph in the house shows a dress.
- The lanyard is only in the robe's pocket on the last night, while the robe is over the chair. Three keys; the third opens the padlock.
- Opening the padlock and lifting the lid are two hands, and the lid stays up for the rest of the night.
- June follows only with the freezer open and her hands free of Tilly. She will not leave Tilly to follow a hand.
- The name is said to her at the freezer, through the panel; saying nothing leaves her standing there.
- Told the name, she sits on the stool by the freezer for the rest of the night, and the reader who said it keeps it.
- Nessa is in the freezer on every visit, under the lid, so the house never has to put her there.

## Checked

- `PellHouseTests.junesLanyardInTheRobeOpensTheFreezerAndNessaIsUnderTheLid`: two readers on the last night with the robe over the chair;
  Nessa lying still in the shut, padlocked freezer; a hand without the lanyard gets the padlocked freezer; the first reader takes the
  lanyard, the second finds the pocket empty; the padlock, then the lid (both lines), the lid up; the second reader, looking in from the
  laundry, keeps that they saw her; a hand on Nessa; the lanyard taken from the first reader goes back to the pocket and the second takes
  it.
- `PellHouseTests.broughtToTheOpenFreezerAndToldTheNameJuneSitsDownByItAndStops`: the freezer open, the laundry door open, June holding
  Tilly in the water; the second reader takes Tilly's hand, the first takes June's; June follows out of the water, round the pool, in at
  the sliding door, through the house and into the laundry, keeping close at every turn; at the freezer the panel asks, and the name is
  said; June on the stool, turned to the freezer; the reader keeps it and the reader out at the pool is told she has stopped; nothing more
  called after; Tilly let go in the water is not taken back; a hand on June after.
- The people case now counts Nessa with the four: asked again, each of the five is there once.
