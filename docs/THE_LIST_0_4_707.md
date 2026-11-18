# The list, part 3b: night one, home (0.4.707)

The second half of the third part of *The list* (`docs/THE_LIST_PLAN.md`). Part 3a (`docs/THE_LIST_0_4_706.md`) ran night one from
the party at the stock tank to coming to under the water tower. This half takes it from there to first light:

- the walk home, past Amber's window;
- first light, in bed or wherever the reader is when it comes;
- the rest of the night for the other readers in town;
- and the House dealing Lamesa.

## Dealt

The House now deals the town. It is dealt from ten crossings down, to a reader who has not had night one. Once they have had it, it is
not dealt to them again until night two exists (part 4).

The night begins on its own. The first reader through the door of a town with no night on is the one Deek picks, and the party at the
tank starts round them. A reader who comes in while a night is on joins it, as in 3a.

The town is one of the House's copies, five to a copy: the one who comes back wrong and four on Amber's side. A group dealt together
shares one town, and the first of them through the door is its slasher. A reader alone gets a town of their own.

A town whose night one is over, with nobody else in it, is put back for a reader who has not had night one, and the night begins again
round them. Part 5 will keep a slasher's town for their four nights; until night two exists there is nothing in it to keep.

## The walk home

From the cut until first light, Amber is up. She stands at the window of her front room over Seventh, beside her desk, facing the
street, with a page out of a notebook in her hand.

- **Her pages.** A reader out in the street in front of her house, near enough to read a page (fourteen blocks of the window), sees her
  hold one up to the glass.
  - The one who went in, with the sack on, reads: YOU'RE WEARING A FEED SACK.
  - With it off: YOU LOOK LIKE YOU GOT DUG UP.
  - Anyone else: WHAT HAPPENED OUT THERE?
  - Three seconds later she turns it over, writes, and holds up the second, the same for everyone: TOMORROW. BACK DOOR. AFTER TEN.
  - Each reader's pages are their own and come once a night. Two readers passing together each get their own first page.
- **Her window.** A knuckle on her glass (within six blocks of her) gets one thing back through it: "Go home. And don't knock on the
  front tomorrow."
- **Home.** The one who went in goes home by the street, the back fence or any way they like. A hand on their own bed (the head or the
  foot), once they have come to, is lying down.

## First light

Night one ends in one of two ways, whichever comes first:

- **Bed.** The one who went in lies down on their own bed. The view goes black for everyone in town, and it is first light.
- **The clock.** The hour comes round to first light, about six minutes after the cut, wherever anyone is.

At first light:

- The town's hour stops at first light and stays there. The night is over.
- Amber goes back to her chair, her hands empty.
- Each reader in town, and the one who went in wherever they are, keeps that they have had night one (`Night`) and where they were when
  it got light (`Light1`): in bed, in the house, out in the town, or away (the one who went in, gone from the town by the House's door).
- A few seconds later each reader in town is told it is light:
  - the one who went in, by where they are: in bed, home on their feet, or not home yet;
  - anyone else, that it is light over the gin.
- The town is not dealt to any of them again until night two exists.

## The other readers' night

A reader on Amber's side rides to the gin with everyone else, sees the door shut and is told nobody is left (3a). From the cut they
have the rest of the night to themselves: they can walk home past Amber's window, read her pages and knock on her glass, or leave by the
House's door. They are told when it gets light, and their record keeps where they were. What they can do against the slasher begins on
night two (part 8).

## Single and multiplayer

- **Alone**, the reader is the one who went in. They walk home past Amber's window and read their pages, then lie down or wait out the
  clock.
- **With others**, the night is the town's.
  - Only the one who went in can end it in bed, and the black falls on everyone in town when they do.
  - Everyone else's first light comes with theirs, or with the clock.
  - Amber's pages are each reader's own. A knock on her glass is answered to the reader who knocked.
  - Every record is the reader's own: whether they have had night one, where they were at first light, and their pages.
- **Spectators** see the hour and Amber at her window. They get no pages and keep no record, and they never begin a night.

## Retreat

As in 3a: night one can be left by the House's door until the seed house door shuts, and the one inside cannot leave from then until the
cut. From the cut on, everyone can leave. A reader who leaves before first light has not had night one, unless they are the one who went
in: theirs ends with first light wherever they are.

## Made as

- **`TheList`**: dealt from `DEPTH` 10 to a reader whose record has no `Night`; `PARTY` 5. `arrive` passes the reader's record on to the
  night. A hand on the reader's own bed (`BED_HEAD`, `BED_FOOT`) asks the night first.
- **`SceneInstances`**: the town is copyable, five to a copy.
- **`TheListNight`**:
  - A seventh phase, `DONE` (first light); the shared record keeps `Bed`, and a town held without a night keeps `Quiet`.
  - `arrive` begins night one round a reader who has not had it, in a town with no night that is not held quiet; and puts back a town
    whose night one is over when that reader is the only one in it.
  - `pages`: Amber at her window (`AT_WINDOW`, beside her desk), within `PAGE_REACH` 14 of `WINDOW`, the second page `PAGE_GAP` 60 ticks
    after the first; `Pages1` and `PageAt1` in the reader's record.
  - `bed` (from the hand on the bed) and the clock reaching `LIGHT` both come to `dawn`: the black on a bed (`HouseFadePayload` 20/60/40),
    `Night` and `Light1` for every participant in town and the one who went in, the hour sent stopped. The bed's hand saves the reader's
    own record after the night has written it, so the bed writes it there too.
  - `LIGHT_CUE` 70 ticks after first light, each reader is told.
  - `talk`: Amber in `AFTER` answers through the glass.
- **`TheListPeople`**: Amber's place in `AFTER` is `AT_WINDOW`, facing the street, holding a sheet of paper. `carries`: what any of
  the town's people hold is drawn in their hands (Mel's camcorder from 3a was held but never drawn); `pageUp`: Amber with a page.
- **`TheListSite`**: Amber's front window is two panes, over her desk and beside it.
- **Client**: with a page, Amber's right arm is held straight out toward the glass and a little to her right (`CastModel.pageUp`), and
  the page is drawn at the end of it, flat across the arm, so its face is to the street (`LiteraryRenderers.page`).
- **Art** (`tools/draw_the_list_art.py`): the page (`entity/the_list/page.png`): torn along the top where the spiral was, faintly ruled,
  a margin, three lines of marker capitals. The art check proves it is torn, clear outside the sheet and written on.
- **`TheListCommands`**: `night light` brings first light. `night off` also holds the town quiet, so no night begins round anyone coming
  in until `/ghosthouse test the_list night` begins one.

## Tests

`TheListTests.nightOneHome` (new, batch `the_list_home`), with a second reader:

- Night one begins round the first reader through the door; the town is a copy for five.
- After the cut and the get-up, with the sack on, both readers stand in the street before Amber's house.
- Amber is at her window, holding a page, in clear floor.
- Each reader gets their own first page (the sack; what happened) and then the second.
- A knock on her glass is answered.
- The one who went in lies down on their own bed: first light, a black for both, the hour sent stopped.
- Records: the one who went in has had night one, in bed; the other, out in the town. Neither is dealt the town again, and neither is
  kept from the House's door.
- Each is told it is light, by where they are, and Amber goes back to her chair with empty hands.

`TheListTests.lamesaAtMidnight` now checks the dealing (from ten crossings down, and only before night one). Every list case holds the
town quiet while it sets up, so that no night begins round its readers unless the case begins one.

## Seen in the frames

Five frames at eye level of Amber at her window, from the street, close, at an angle and from inside behind her, and one of Mel at the
party, were looked at before this was called done.

- The window was one pane, over the desk; she stood beside it, so from the street she was behind the wall and only her hand showed. The
  window is two panes now, and from Seventh she is seen at it with the page up beside her face.
- She first held the game's own paper, whose picture is a slanted sheet; held flat to the glass it was a white diamond. She holds a
  notebook page of its own now, drawn for it.
- Nothing the town's people held was drawn: Mel's camcorder at the party was in her hand only on the server. It shows now.

## Found and fixed

- Amber's first place at her window was the cell just inside it, which is the front wall's lining. She stands beside her desk now, with
  the page at the window.
- The bed's first light was written to the reader's record and then overwritten, because the hand that reaches the bed saves the record
  it was handed after the night has run. The bed now writes into that record too.

## Every new line

Grey cues on the action bar:

- Amber's pages, the first to the one who went in with the sack on: "Amber holds a page against her window. In marker: YOU'RE WEARING A
  FEED SACK."
- With the sack off: "Amber holds a page against her window. In marker: YOU LOOK LIKE YOU GOT DUG UP."
- To anyone else: "Amber holds a page against her window. In marker: WHAT HAPPENED OUT THERE?"
- The second, to everyone: "She turns it over, writes, and holds it up again: TOMORROW. BACK DOOR. AFTER TEN."
- First light, to the one who went in, in bed: "First light at the blinds. Your shoes are still on, and there is caliche on the sheet."
- In their house, not lying down: "It gets light, and you are home, still on your feet. Down the street a sprinkler comes on."
- Out in the town: "It gets light, and you are not home yet. A screen door bangs somewhere down the street."
- To anyone else in town: "It gets light over the gin. Sprinklers come on, and a dog starts up somewhere."

Speech, over her head as her name:

- Amber, through the glass after the cut: "Go home. And don't knock on the front tomorrow."

For operators: "Night one is over and the town is put back; no night begins round anyone coming in until /ghosthouse test the_list
night."
