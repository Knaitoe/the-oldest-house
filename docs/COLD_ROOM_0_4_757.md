# The cold room, part 4: the offer and the end (0.4.757)

The last part of the cold room (design: [WAXWORKS_AND_COLD_ROOM_0_4_749.md](WAXWORKS_AND_COLD_ROOM_0_4_749.md); earlier parts:
[COLD_ROOM_0_4_754.md](COLD_ROOM_0_4_754.md), [COLD_ROOM_0_4_755.md](COLD_ROOM_0_4_755.md),
[COLD_ROOM_0_4_756.md](COLD_ROOM_0_4_756.md)). The House now deals it, and it is a Witness source of the understanding kind: pool 97,
quota 33, two kinds, three endings.

## The offer (`ColdEnd`)

- From the ninth page, each reader in the back bedroom who has not had it is offered something, on the conversation panel. It is made
  only from what the House has kept of that reader:
  1. the last companion it saw them lose, by name: "I have %pet here. Let me out of her and %pet comes back to you, the way it was
     before.";
  2. otherwise their last death, and where: "%message, %place. I can make it so that never happened. Let me out of her.";
  3. otherwise: "Let me out of her and I'll show you the way home. Not the House's way. Yours."
- **Where?** (spoken): she laughs ("She laughs. It goes on for a long time. Nothing is given back."), the view goes dark for a moment,
  and the reader is at the foot of the stairs by the front door: "The book is upstairs, and your place in it is gone." They read no
  more that night, hold nothing, and are not asked the last line. They are not finished; a later night in a fresh house starts over.
- **Keep reading**: the panel closes, and the reader keeps reading.
- Spectators are offered nothing.

## The last page

- The twelfth page is blank. Every reader within reach of the bed (standing or kneeling), who did not answer her, is asked once:
  "The last page of the book is blank. Whatever goes there has to be said by someone in the room."
- **Take me instead.** (spoken, so the others in the room hear it): the first to say it is taken. "Everything goes white." The bed
  comes down; the cold goes. The taker is at the window with their hands on the glass; then at the foot of the steps under the lamp,
  "There is snow on your coat. You are breathing."; then back in the kitchen, where her mother looks up, then past them, at the stairs.
  Their outcome is `took_her_place`.
- The other readers in the house are told: "The cold goes out of the room all at once. She is asleep. %name is not in the room." Their
  outcome is `were_in_the_room`.
- **Say nothing**: the panel closes; that reader is not asked again that night.

## The morning

- If nobody says it within thirty seconds of the blank page, the morning comes: "The window goes grey. She is asleep, and breathing. The
  cold has lifted a little. On the stairs, slow steps: her mother has come up, and stops on the landing."
- Her mother waits on the landing, facing the bedroom door, for the rest of that night.
- The readers' outcome is `saw_the_morning`.

After either end nothing in the room moves and the voice says nothing more. A reader who answered her, a spectator, or a reader who
never heard a page read is not finished.

## The account

The account ("The account: the cold room") waits on the kitchen table, in the reader's own hand, from their own record:

- who else was in the room (the others who read or held her that night);
- how long they read, whether they held her, how many times they used the water;
- what the voice had of them (their name; a lost companion; a death; their own words), and that things moved where nobody was looking;
- what she offered at the ninth page, whether they once asked her where, and whether they kept reading;
- the priest at the eleventh page;
- how it ended: the line said ("Whatever went down the steps with me has not said anything since."), someone else taken, or the morning
  ("It was over. Nobody knows for how long.").

## Witness and the House

- `WitnessAccount.Story.COLD_ROOM`: "The back bedroom", understanding: "I knelt by a bed in a cold house on a hill and read from a book,
  while something in the bed said my name."
- Advancement `account/cold_room` (icon: snowball); stair figure "the priest from the cold house"; Pell forged line "I said the line
  that is not in the book, and I came back up the steps by myself."
- WitnessPool 97 / 33 / 73.

## Checks

- `ColdRoomTests#theOfferAnsweredAndTheOneWhoIsTaken`: two readers and a spectator. At the ninth page each reader is offered, the first
  their lost companion by name, the second only the way home; the spectator nothing. The second answers and is sent to the hall, and
  reads no more even kneeling at the book. At the blank page the first is asked and the second and the spectator are not. The first
  says it: taken, the cold gone, `took_her_place`; at the window, at the foot of the steps, in the kitchen; the account on the kitchen
  table opens and says what they did. The one who answered and the spectator are not finished.
- `ColdRoomTests#nobodySaysItAndTheMorningComes`: two readers and a spectator; both asked, both say nothing and are not asked again; the
  morning comes, the cold goes, her mother is on the landing and stays one of her through a later arrival; both saw the morning, the
  spectator is not finished; the account names the other reader; nothing in the room moves after.
- The earlier cold room cases; the group, cast and two-reader sweeps; the Witness pool, stair company, Pell forged lines and
  advancement cases.
