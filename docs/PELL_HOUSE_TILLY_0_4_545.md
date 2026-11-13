# The Pell house 23: a reader as Tilly (0.4.545)

The owner, 12 November: "start by finishing each unfinished vignette, then move on to ensuring multiplayer functionality worked as
intended in each vignette." The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its whole week, its endings and its aftermath. This is its
twenty-third slice: the second of two readers may play Tilly. From the proposal's "Who plays whom":

> - **Two readers.** One reader may play Tilly. The other is the older sibling.
>   - **Picking roles.** A long cane and a small coat hang on the hook inside the front door, and whoever takes the cane is Tilly.

and from "Tilly: believed, and cannot see":

> - **Her own verb: not answering.** [...] The answers are chosen on the conversation panel, never typed in chat. [...] Silence, or her own
>   name, holds it back.
> - **Co-op lines.** Tilly's own lines come to her reader in grey, to say in their own words or not, as Tess's do.

Tilly's senses (her sight, the cane's tap, what only she hears, the cold of the line) are the next slice. The kids next door, Gail's third
mark and a house to each pair are after that.

## The cane

- **Where:** on the hook inside the front door, beside the small coat (`PellTilly.HOOK`, the hall's west wall).
- **When:** on the first visit, from when June has the front door open until her tour is done, with another reader at the house.
  - Each reader there is told once: "Inside the front door a long cane hangs on a hook beside a small coat. Whoever takes the cane is
    Tilly."
  - Before then, or alone, the cane is only a cane: a hand on the hook says what hangs there.
- **Taking it:** a hand on the hook. The reader who takes it is Tilly from then on, on every visit, until their ending at the house.
  - They are told: "You take the cane from the hook. To [the others], you are Tilly now. Tilly's lines come to you in grey: say them in
    your own words, or don't."
  - Each other reader is told: "[name] takes the cane from the hook. To you, they are Tilly now."
  - Nobody else can take it after that, and nobody takes it on a later visit.
- **The hook:** bare of the cane while a reader is Tilly at the house; the cane is back on it when they go, and when the visit begins
  again without them.
- **If nobody takes it**, Tilly is the house's, as she always was.

## Tilly's body

- **Hidden where the reader is.** The house's own Tilly stays, invisible and untouchable, wherever the reader is, so that everything the
  house does with Tilly it does with them: who is at the table, who is asleep, who is on the edge of Nessa's bed.
- **Carried where June leads her.** Where June has Tilly by the hand (woken and led down at midnight, held in the water, answered and led
  home), the reader goes with her, at her feet, and does not get down.
- **A child's size,** as Tilly is, while they are at the house; their own again when they go, log out or die (looked at twice a second,
  so also when nobody is left at the house).
- **Drawn as Tilly** to everyone at the house, the reader included (`client.PellTillyLook`): her cane in hand, her hand out in June's,
  asleep in Nessa's bed, her hair being brushed.
- **Her plate:** Tilly's place at the table is theirs.
- **Her bed:** Nessa's bed is a bed to them. At bedtime a hand on it sits them on its edge for June to brush their hair; at night it lays
  them down in it, and while everybody at the house is asleep the night hurries on.
  - At bedtime June waits on the edge of the bed for them for thirty seconds, then says goodnight anyway.
  - Brushed and tucked in, they are laid down if they are by the bed.
- **At midnight on the last night** June goes to Nessa's bed for Tilly. A reader as Tilly who is not by it is told she is there saying
  the name; twenty seconds later the house brings them there, in a moment's dark.
- **Nothing of the older sibling's is theirs:** June never sends them to the garage or blames them, Gail never marks their file, the
  journal is never written in their hand, they have no turn on the telephone, and Callum's part of the night is not theirs.

## Tilly's lines

- Whatever Tilly would say comes to the reader in grey when she would say it, as "Tilly might say: ..." Nobody hears it from the
  house's Tilly. What they say, if anything, is their own.
- Lines the house keeps for a reader as Tilly come to them in grey too: June going to the bed at bedtime, June leading them, the paint
  under a bare foot, June taking them back, a hand finding theirs.

## The name

- **A week in,** June calls them Nessa at bedtime and again at breakfast, and they are asked on the panel: Tilly's own answer, the one
  June wants, or nothing. June hears whichever they say. Nothing is said for them; after eight seconds the panel closes and the house
  goes on.
- **In the water on the last night,** each time June says the name the reader is asked on the panel. A reader who answered to it a
  week in is reminded of that the first time: "You've said it to me before, love. Say it again."
  - "Yes, Mum." answers to the name. It is heard as when Tilly answers, June says "There's my Nessa. There you are.", takes them home and
    puts them to bed in Nessa's bed.
  - "I'm Tilly." gives their own name. June goes on.
  - "Say nothing." June goes on.
  - The house's Tilly never answers for them: only an answer to the name ends it.
- **Holding out:** a reader as Tilly who never answers keeps their own name until it is light. The night goes on to first light; June's
  hands come away and she stops calling.

## Out

- **A hand:** another reader's hand on the reader as Tilly, in the water or after, is a hand on Tilly. June's hands come away, the reader
  as Tilly is let down off June's Tilly, told whose hand it is, and has Tilly's question in grey ("Is that you?"). They walk themselves;
  the hand only has to stay within five blocks.
- **The paint:** a bare foot on the line where it is whole is put back off it.
- **Through a gap,** with the hand or on their own, they are out. The other reader who brought them out keeps that they did.

## Endings

| Who | What happened | Outcome | Account |
|---|---|---|---|
| Reader as Tilly | out through a gap | `came_out_with_own_name` | ready (`PellTexts.tillyAccount`) |
| Reader as Tilly | kept their own name until light | `kept_own_name_until_light` | ready |
| Reader as Tilly | answered to the name | `answered_to_nessa` | none; ended, nothing resolved |
| The other reader | brought them out, saw it, or was there | as before | ready |
| The other reader | Tilly held out alone, nobody got them out | `tilly_held_out_alone` | none |
| The other reader | Tilly answered | `tilly_answered` | none, as before |

- **After answering,** June stands the reader as Tilly at Nessa's window at first light, in a moment's dark, and nobody else sees it.
  The older sibling's ending is theirs (`TillyStayed`); the reader as Tilly's is not. When everybody else has gone with Gail, the reader
  as Tilly watches the car go and is set down in the street, on their own.
- **Tilly's account** is in their own words: the cane, Nessa's room, the brushing, the name before (answered or not), the pool, their own
  name, the hand (by name) or none, out with somebody or alone, Gail, the street. "We" only when somebody brought them out.
- **The Witness line** is Tilly's own for a reader who played her. For a reader whose Tilly was another reader it says "Tilly", never
  "she": "On the last night, in the water, Tilly would not answer to any name but Tilly, and came out through a gap in the line."

## What each reader keeps

- `PlaysTilly`: they are Tilly at the house. `TillyReader`, `TillyName`: who was, for the other.
- `CaneHint`: told of the cane.
- `TillyBrushed`, `TillyBrushedVisits`: brushed at bedtime, and on which visits.
- `AnsweredNessa`, `SaidOwnNameBefore`: a week in, answered to the name, or gave their own.
- `SaidOwnName`, `AnsweredInWater`: in the water, gave their own name, or answered to the other.
- `TillyHand`: whose hand took theirs. `OutWith`: who brought them out, if anybody. `CameOut`, `HeldOut`.

## How it is made

- **`labyrinth.PellTilly`** (new): who is Tilly (`before`, from the readers' own records each tick), the body (`after`: hidden, carried,
  small, the look), the cane (`hint`, `hook`), the lines (`prompt`), the panel (`ask`, `askName`, `asked`), the bed (`bed`, `tuck`,
  `atBed`), the house's hands on them (`kept`, `window`), and another reader's hand (`interact`).
- **`network.PellTillyPayload`** and **`client.PellTillyLook`**: the reader drawn as Tilly, in her pose, lapsing after three seconds
  unrenewed.
- **`LiteraryActor`**: the hidden Tilly cannot be touched or pushed, shows no health, and carries the reader at her feet.
- **`PellThing`**: Nessa's bed is a bed.
- **`PellPeople.Household`**: the reader as Tilly and their waits. **`PellVisit`**, **`PellMorning`**, **`PellNight`**, **`PellRite`**,
  **`PellDawn`**: Tilly's part of each beat goes to the reader, and the older sibling's to the others only.
- **`PellTexts`**: the hint, the cane, Tilly's lines in grey, the house's lines for a reader as Tilly, the panel's answers, June's line
  for them, `tillyAccount` and `witness`. **`WitnessAccount`**: the house's Witness line by `PellTexts.witness`.
- **`LiteraryVignettes.watched`**: can leave out the reader the house is moving.

## Decided here (any of it can move)

- The cane can be taken for as long as June's first tour lasts, not "a short while": the tour is the natural window.
- A reader as Tilly is Tilly until their ending; there is no giving the cane back.
- An answer to the name a week in is kept and is in the account; in the water only an answer there ends it.
- The panel a week in waits eight seconds; in the water it waits for the next call.
- The reader as Tilly cannot be the older sibling on a later visit; the pair is saved in their records.
- Until the kids next door are built, a reader who does not take the cane is a second older sibling, as before.

## Checked

- Four new cases:
  - the cane taken on the first visit (only once the door is open, the hint to each, the records, the bare hook, the other's hand takes
    nothing, the hidden Tilly where the reader is, a child's size, drawn as Tilly to both, a line in grey to them alone; both out of the
    house at once, their size their own again; the other back, Tilly the house's again and the cane on its hook);
  - the name on the panel in the water (June's reminder to a reader who answered a week in, asked only them, their own name, under
    with June, answering, heard, June's line, put to bed, the window at first light, the endings);
  - another reader's hand and out through the gap (the hand, told whose, Tilly's question in grey, put back off the paint, out together,
    both accounts ready, Tilly's account and both Witness lines);
  - holding out until light (June's hands come away, the other hears her stop, the endings).
