# A Little Longer, slice 2: Rowan (0.4.622)

The plan's line (docs/A_LITTLE_LONGER_PLAN.md): "Rowan. Painted (two adult casts), standing, walking real paths, humming at the bench,
the conversation panel." The House still does not deal the place; the evening's conversation begins with the next slice.

## What is built

- **The cast** (`LongerRowan.castFor`). Fixed at the first meeting from the House's settled DRAWN_TO read: men, the man (stranger
  phase 211); women, the woman (210); both, nobody or unread, one of the two by the reader's own id. Kept in the reader's record
  (`RowanCast0622`) and never changed, whatever the read says later. Each reader's copy of the shop has its own Rowan, made once the
  copy's entities are loaded and never made twice.
- **The skins** (`tools/paint_character_skins.py`, `literary_longer_rowan_woman` and `_man`). The same work in either body: a shirt
  with the sleeves turned back, a canvas bib apron with its straps over the shoulders and crossed at the back, a pocket with a pencil
  and a small screwdriver standing in it, the strings at the waist and a scorch from the iron low on one side. Hair tied back; for
  the man, a shadow of stubble.
- **At the bench.** Sat in the mended chair facing the bench (the chair is a seat to the House now, so Rowan is drawn sitting in
  it), both hands in the open back of the radio, the fingers at work by turns, the head bent to it and moving a little with the
  tune. Rowan hums the same five notes now and then and tunes a radio through its static between times; both carry through the
  back door into the corridor (the proposal: "The player hears a radio being tuned and someone humming before seeing Rowan").
- **Errands, walked on their own feet.** Every fifteen to thirty occupied minutes' worth of ticks (900 to 1,800), Rowan stands, walks
  the aisle to the shelf of waiting sets and puts a hand up to a tag, goes on to stand behind the counter leaning on it and looking
  at the shop door, then comes back to the chair. The body turns into the way it walks and turns on the spot at a sharp corner; no
  step is longer than a walking pace. Nobody is sent on an errand within twenty seconds of a hand on them; spoken to at the shelf or
  the counter, they stay there a while longer.
- **The head** turns to a reader near them they can see, no further than a head turns.
- **A hand on Rowan** opens the panel with where they have got to. Nothing is asked of anybody yet.
- **Sounds** (`tools/generate_longer_audio.py`, synthesized, no recordings): `longer.hum` ("Someone hums") and `longer.tuning`
  ("A radio is tuned").

## New lines, flagged for the owner

All Rowan's, in the panel and over their head, never in chat:

- At the bench, the first time: "Sorry. One minute. I nearly have it."
- At the bench, after: "Nearly. Really, this time."
- At the shelf: "I had a valve this size. I know I had one."
- At the counter: "Nobody has come in all night. I still look."
- Walking: "Hold on. I'm only going to look at something."

Rowan's name stands over these in the panel although the reader has not been told it; the work order is signed "R.". If the owner
would rather the name wait for the next slice's introduction, the panel can show nothing over the line until then.

## Checks

- `LongerTests.rowanSitsAtTheBenchHummingAndKeepsTheCastTheyWereMetIn`: the read picks the cast, the record keeps it through a changed
  read and a second arrival, one Rowan only, seated facing the bench, the hum and the tuning heard from the corridor, the panel's
  first and later lines, nothing in chat.
- `LongerTests.rowanWalksTheirErrandOnTheirOwnFeetAndComesBackToTheChair`: every way clear for a body (but the chair stood up out
  of), the shelf and its gesture, the line there and the longer stay, the counter and its gesture, back in the chair and turned to
  the bench, no step longer than a walking pace.
- Review frames of both casts at the bench (three angles), the shelf, the counter (two angles), standing front and back and walking.
  Fixed from them: the counter spot stood inside the counter's edge, the shelf spot stood too far off for the hand to reach a tag,
  and the man's stubble read as dirt (now a shadow).
