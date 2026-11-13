# The Pell house 14: the tapes (0.4.536)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its first
visit whole, and its second, a week in, has the afternoon and June's swim (`docs/PELL_HOUSE_WEEK_0_4_533.md`), the knife
(`docs/PELL_HOUSE_KNIFE_0_4_534.md`) and June's hand (`docs/PELL_HOUSE_HAND_0_4_535.md`). This is its fourteenth slice, the tapes,
from the proposal's "2. A week in":

> **The tapes:** the shed is open. The older sibling can watch the tapes in order, and the rite becomes readable (below).

and from its section "The tapes":

> nine original home recordings by an older woman. She is never named, and she speaks to someone off camera who has lost a child. [...]
> **In order:** watching them in order is what turns the house's details into meaning [...]. Out of order they are only a frightening
> woman talking.

The freezer and the journal's false entries come after. The House still does not deal the house.

## The shed is open

- **A week in,** the shed's padlock hangs open on its hasp, and a hand opens and shuts its door ("The padlock hangs open on its hasp.").
- **On her rounds** June unlocks it and opens it as on the first night, and shuts it once nobody is in it. From then until the visit
  ends, it is padlocked again.
- **On the first visit** it is padlocked, as it was, and the set and the tapes are only what they are.

## The deck

- **The tape in the deck when a visit begins is the eighth,** the one about the water. Its case is the empty one on the shelf. Whoever
  puts the deck on first hears that one, out of its place.
- **A hand on the television** plays the tape in the deck from its start ("The deck clunks and the screen comes up."), or, while it
  plays, stops it ("The picture goes.").
- **The picture** is the tape's own shot, its date stamped in the corner, on the portable television's screen: the deck locks on through
  snow and a rolling picture; then grain, a tracking band, jittered lines, colour slipping, the head-switch at the foot; dropouts,
  more of them on the two tapes written over and most on the one in the deck, which has been watched most. Snow at its end.
- **Her lines** are captions from the screen ("On the tape: ..."), heard by anybody within eight blocks of the set, with a woman's voice
  under them, muffled by the speaker ("A woman's voice, on a tape").
- **The screen is lit** while it plays (the set gives a little light).

## The shelf

A hand on the tapes opens the conversation panel:

- "Tapes in plain cases, a date on each spine in the same hand."
- The empty case: "20 April, written over. The case is empty."
- Each other tape, by its spine, in order, to take; and "Leave them".

Taking one, the deck's tape goes back in its case and the deck takes the chosen one, which plays from its start ("The tape in the deck
goes back in its case. The deck takes the other.").

## The tapes

Each runs about forty seconds at the usual reading time (the world's reading-time setting lengthens or shortens them, as it does every
line). All nine, in order, take about six minutes.

| Spine | What she says |
|---|---|
| 2 March | "Is it on? The little light's on. Good." / "You asked me how. I'll show you once, and you watch it as often as you need to." / "Don't ring me about it after. And don't tell a soul, not even family." |
| 9 March | "First, the ring. Paint is best. Salt blows away, and chalk washes off." / "One ring, all the way round. No gap in it, not a door's width, not a finger's." / "What you call back can't go where the ring isn't. That's all a ring is." |
| 16 March | "Round the house, not round a room. You'll want to keep it small. Don't." / "It goes looking for the thin place, so go round every night with the brush." / "If it's scuffed in the morning, it was tested. Paint it again." |
| 23 March | "Now. It won't stay in an empty ring. It needs somebody to stay in." / "A child holds it best. One nobody will come asking after." / "Keep him inside the ring. He mustn't ever cross it, or it goes out with him." |
| 30 March | "He doesn't eat. I know how that sounds. Water, yes. Never food." / "Food brings him back into himself, and what he's holding goes." / "Lay him a place at every meal all the same. It has to think it's at a table." |
| 6 April, written over | "Then you want the other one. A girl, as near to yours as you can find." / "Near in the way that matters. You'll know it when you see her sit down." / "Not one with people who'd come looking. One that's been passed about." |
| 13 April | "She has to answer to the name. Say it every day. Say it as if you forgot." / "Give her the room. The clothes, the brush. Let her sleep where yours slept." / "The day she turns her head to it without thinking, she's ready." |
| 20 April, written over (in the deck) | "It's done in water. Where you lost yours, if you can bear it." / "Warm it. Leave the light on under it. She shouldn't be frightened." / "Say the name the whole time. Keep her under till she answers to it." / "Then it's poured into her, and you'll have yours back." |
| 27 April | "The boy you can let go of, once it's poured. He won't remember the ring." / "That's all of it. I'm tired now." / "Turn it off. Then wind it back, and watch it again from the start." |

The pictures (`tools/draw_pell_tapes.py`): a kitchen with the woman at its table; a line of paint along a floor; a line round a garden;
a ring round a room; a place laid; two photographs side by side; a girl's bedroom; a pool at night with its light on; and close, a hand.
Nothing says what the boy is, or whether any of it works.

## Watching

- **The deck is the house's.** Everybody near it hears the same tape, and anybody can put it on, stop it or change it.
- **Watching from its start:** a reader in the shed, or near the screen (within five blocks) with nothing in the way, when the tape
  begins or before her first line. A reader who comes in after her first line is not watching that tape.
- **Still watching at its end:** within eight blocks of the set, alive, all the way through. A reader who goes further off stops
  watching it.
- **A tape watched through** is kept ("TapesWatched"). Watched in turn from the first, the reader's way through them goes on
  ("TapeRun"); through all nine in turn, they have read the tapes ("ReadTheTapes"). A tape watched before its turn is kept too
  ("WatchedOutOfOrder"); it does not undo the way through them.
- **Each of her lines** a reader hears is kept once in their journal, under the tape's date ("A tape, 20 April"), said by "On the tape".

## June

- **Coming for the tin on her rounds,** at the bench, she turns the deck off if a tape is playing. Anybody within earshot is told:
  "June turns the deck off." Nobody has watched it. She asks anybody in the shed out, as on the first night, and padlocks it.

## What each reader keeps

- which tapes they have watched through ("TapesWatched", one bit to a tape, the first tape the lowest);
- how far through them in turn they have got ("TapeRun", 0 to 9), and that they have read them ("ReadTheTapes");
- that they watched one before its turn ("WatchedOutOfOrder");
- which of her lines they have heard ("TapeLines", one bit to a line), so that each is kept in their journal once.

## How it is made

- **`PellTapes`** has the deck: `play`, `stop` and `tick` (her lines, who is watching, the tape's end), the shelf (`shelf`, on the
  conversation panel, key `pell_tapes`), the hands (`tv`, `shedHand`), June's rounds (`juneStops`, from `PellNight` as she takes the
  tin) and `restart`. Its timing is the same on both sides: `schedule`, `length` and `lineAt`, from `LEAD` 40, `TAIL` 40, `PAUSE` 12 and
  each line's reading time (`Dialogue.ticks` at the world's setting, sent with the tape). `WATCH` 5, `AWAY` 8, `EARSHOT` 8.
- **The household** keeps which tape is in the deck (`deck`, the eighth when a visit begins), whether it is playing and since when
  (`tapePlaying`, `tapeStart`), its reading pace (`tapePace`), how many of its lines have been said (`tapeLine`), who is watching it
  (`tapeWatchers`) and who has been sent it (`tapeTold`). `PellVisit.tick` runs the deck whatever the hour; `PellVisit.restart` puts
  the eighth back and stops it.
- **`PellTapePayload`** (protocol 188): the screen's place and facing, the tape, the tick it began and its reading pace; a tape of -1
  stops it. Sent to every reader at the house while it plays, and to anybody who comes.
- **`client.PellTapeClient`** builds the picture each tick from `textures/pell/tapes.png` (96 by 72 shots, three to a row) and draws
  it on the screen, and gives the captions her line as a voice from the screen. **`client.SpeechCaptions`** now takes voices with no
  body (`SpeechCaptions.Voice`, `voices`), captioned with the people about by distance.
- **The set** has a lit model (`shed_tv_1`, `portable_tv_lit`, `tools/draw_pell_things.py`) and gives light 4 while it plays.
- **Two new sounds** (`tools/generate_pell_audio.py`): `pell.tape_play` (the deck taking a tape: a clunk, the motor, the hiss coming
  up; "A video deck takes a tape") and `pell.tape_voice` (three takes of a woman's muffled voice through a small speaker; "A woman's
  voice, on a tape").
- **The pictures** are drawn by `tools/draw_pell_tapes.py` (checked in CI with `--check`).
- **For tests:** `PellTapes.deck`, `playing`, `watchers`, `nearEnd` (the tape put on to just before its end) and `asWatched` (a reader
  as if they had watched these tapes).

## Decided here (any of it can move)

- The shed is open by day a week in, until June's rounds; she padlocks it then, as on the first night.
- The eighth tape, about the water, is in the deck when a visit begins, its case the empty one.
- One shot to a tape, its date in the corner; the two written over and the one in the deck more worn.
- Watching means there from her first line to the end, within earshot; anybody can stop or change the tape for everybody.
- In order means each tape in turn from the first, whatever was watched between; a tape watched early is kept, and does not break it.
- June turns the deck off when she comes for the tin; nothing more is made of it yet.
- Nothing in the house changes for a reader who has read the tapes; the account and the endings will use it.

## Checked

- **Two new cases:**
  - `aWeekInTheShedIsOpenAndItsTapesPlayForWhoeverIsByTheDeck`: two readers a week in. The shed opens to a hand; the eighth tape plays,
    the screen lit, watched from the shed and not from the pool; her first line heard and kept in the journal under its date, and not by
    the pool. Watched to its end, kept before its turn; the screen dark. The shelf's spines, the empty case named, and the first taken:
    it plays from its start. The other, in after her first line, is not watching; the first, walked off past earshot, stops; neither has
    it. Played again with both in the shed, both watch it through and are on to the second, the other in turn. A hand on the set while
    it plays stops it. The other, through the rest in turn, watches the ninth from the shelf and has read the tapes; the first has not.
  - `theTapesWaitForAWeekInAndJuneTurnsTheDeckOffOnHerRounds`: the first visit, the shed padlocked and the set and the tapes only what
    they are; a week in, the reader opens the shed and waits in it; at eleven June goes out, and with a tape playing she comes in for the
    tin and turns the deck off. Nobody has watched it.
- **Pell 24/24 locally** (the twenty-two earlier cases and these two), the architecture namespace, the quick checks,
  `tools/draw_pell_tapes.py --check`, `tools/draw_pell_things.py --check` and `tools/generate_pell_audio.py --check`.
- **The picture and the captions are client-side;** they compile, and CI's client jobs load them.
