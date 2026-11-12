# The Pell house 6: the first evening (0.4.526)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot, its rooms, its line, its people and its first visit's day (`docs/PELL_HOUSE_VISIT_0_4_525.md`). This is its sixth
slice: the first visit's evening, from the proposal's "1. The placement": "the siblings unpack. Dinner is at the table, and June serves
Tilly first." The night and the morning are the next slice. The House still does not deal the house.

## What happens

- **The house keeps its own hours.** The visit begins in the late afternoon, and the afternoon holds while June shows them round.
  Once the day is settled the hour goes on at an ordinary day's pace, an hour in fifty seconds, to six.
- **Six o'clock.** June calls them from the kitchen: "Dinner!" A reader too far off to hear her is told it where they are, in grey:
  June, calling from the kitchen.
- **The table.** June lays four places. Tilly comes from Nessa's room and sits by the hall door; Callum comes from the end of the hall
  and sits by the stove. June takes the saucepan from the stove and waits for somebody to come to the kitchen or sit at the table, or,
  if nobody comes, for half a minute.
- **She serves Tilly first.** "There you go, love." Then a place for each reader: where a reader is sitting first, then a free place
  for any who are not. "And one for you." Callum's plate she passes over without a word, and it stays empty. She asks after Tilly's
  ("Is that all right, love? Tell me if it's too hot.") and takes the saucepan back to the stove.
- **A place for nobody.** With fewer readers than free chairs, the chair nobody takes has a place laid at it all the same. Nothing is
  put on it.
- **Eating.** A hand on the table finds what is at the place nearest the reader: Tilly's dinner; Callum's plate, nothing on it; the
  place laid for nobody. At a reader's own place it is their dinner, sausages, mash and peas, and they eat it, once; it feeds them like
  any meal.
- **Clearing up.** When the plates are done, or dinner has gone on a minute, June says "Leave those. I'll do them." Tilly and Callum
  get down and go back, Tilly to Nessa's room and Callum to the end of the hall. June clears the table once they have gone, takes the
  plates to the sink, and goes back to her stove.
- **The evening.** The hour goes on to half past eight, and holds there. What comes after (June sending them to bed, the doors locked,
  the night) is the next slice.

Nobody is hurt and nothing is forced. A reader can stay away from dinner; June serves without them, and their plate waits.

## What each reader keeps

Each reader's record keeps whether they were there to see June serve Tilly first ("SawTillyServed", anyone within earshot of June when
she does) and whether they ate ("Ate"). Two readers can come away differently: the one who came in from the garage late did not see
Tilly served.

## How it is made

- **The hour.** `PellVisit.clock` keeps the house's own day time (`Household.time`) and whether it is going on: held at `AFTERNOON`
  (16:48) through the tour, going on to `SIX` once the day is settled, held through dinner, going on to `EVENING` (half past eight)
  after it. It is sent to each reader at the house (`PellPayload`: the time and the rate) when it starts or stops going on, when they
  first come and once a second; the client (`PellClient`) keeps the sun moving between packets at that rate. The Pell house has its own
  scene clock mode, 22 (`SceneClock`), whose fallback is the late afternoon. Nessa's talking clock says the house's hour.
- **The table.** `PellVisit.TABLE` is the kitchen table's four places: each chair, where its plate lies, which way a body sitting there
  faces, and where one stands to get into it. Tilly's place is the chair by the hall door and Callum's the chair by the stove; if a
  reader is already sitting in one, they take the next free chair.
- **Sitting.** The kitchen chairs are seats (`PellThing.seat`): a reader sits in one with an empty hand (`HouseSitting`), never where
  one of the house's people is. Tilly and Callum walk to the chair and are put into it, turned to the table, and drawn sitting
  (`LiteraryRenderers.seat`). Sitting at the table, Callum's body stays turned to it and only his head follows a reader.
- **Walking.** `PellVisit.go` sends one of the people along a way, to sit at the end of it or stand facing a way; `walkers` moves them a
  step a tick on the shared footpaths, with the same unseen fallback as June's tour.
- **The plates** are item displays lying on the table (`KitchenFittings.lay`), tagged so that any left from before are taken away when
  the visit begins again. They are four items drawn by `tools/draw_pell_people.py` at sixteen pixels to the block: the plate laid, the
  dinner served, the plate scraped, and June's saucepan, which she holds while she serves. A served place's display is replaced by the
  scraped one when its dinner is eaten.
- **Dinner's steps** follow the day's: `SETTLED` (going on to six), `CALLED`, `LAID`, `SERVING`, `EATING`, `CLEARING`, `CLEARED`.
- **A hand on Callum at the table** gets what he does there instead (`PellTexts.CALLUM_TABLE`).
- **Left empty, the house begins again** as before, and now also with the afternoon's hour, nobody sitting or walking, the table bare
  and June's hands empty.

## Decided here

The proposal gives the beat but not the meal, the table or the timing. These are mine, and any of them can be moved:

- **Dinner is sausages, mash and peas,** with gravy.
- **Tilly sits by the hall door and Callum by the stove;** a reader sits where they like, and June serves them where they sit.
- **Callum's plate is laid and stays empty.** June passes it over without a word. (The tapes, later, are where the reader learns why a
  child would be kept hungry.)
- **A free chair gets a place laid at it**, which nobody is served. It says nothing about whose it is.
- **June serves once somebody has come,** or after half a minute if nobody does; dinner is over when the plates are done or after a
  minute.
- **The afternoon holds through the tour** so the tour is never cut off by evening; the hour then goes on at an ordinary day's pace
  (an hour of the day in fifty seconds) to six, and again after dinner to half past eight.
- **June eats nothing at the table.** She stands at her stove while they eat.

## The case

`PellHouseTests.dinnerAtSixServesTillyFirst`, with two readers, checks:

- the visit begins in the late afternoon, and once the day is settled the hour goes on toward six;
- June calls at six; the reader in the garage is told the call where they are, the one in the living room hears June herself;
- June lays four places, Tilly's by the hall door and Callum's by the stove, and the two of them sit in those chairs;
- nobody is served before a reader comes; when one sits at the table June serves Tilly first, then the reader where they sit, then a
  place for the other reader, and passes Callum's over;
- the reader at the table saw Tilly served and the one in the garage did not;
- a hand on the table at the reader's place eats their dinner once and feeds them; a second hand finds the plate scraped; beside
  Callum's chair the nearest plate is his, with nothing on it; a hand on Callum at the table gets what he does there; the other reader
  sits at their place and eats theirs;
- Tilly eats hers; June clears, Tilly and Callum get down, the table is cleared and June takes the plates to the sink;
- Tilly goes back to Nessa's room and Callum to the end of the hall, and the evening goes on to half past eight and holds there;
- left, the visit begins again in the afternoon, the table bare and nobody sitting, and each reader keeps what they did.

The first visit's case (`theFirstVisitShowsThemRoundAndGivesJunesRules`) still holds with the evening after it.

## Not yet

Bedtime (Tilly's hair brushed, the older sibling sent out to the garage, the doors locked from inside), the night (June out with the
tin, the singing through the wall, Callum on the line), the morning (breakfast, Gail's call), and everything after.

## Numbers

No layout change. No Witness change. New: four items (three plates and a saucepan) and their art, one network payload, one scene
clock mode.
