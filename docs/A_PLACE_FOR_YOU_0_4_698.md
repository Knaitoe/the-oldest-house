# A Place for You: recognition, and what the reader keeps (0.4.698)

The fourteenth slice, and the last of item 9 of `docs/A_PLACE_FOR_YOU_PLAN.md`: the waiting room's read of how the reader presents
(SELF), the hall glass every reader can cover or uncover, the keepsake and the House of Leaves fact. With 0.4.697 (AGE) this changes
the account, the Witness line, the House of Leaves and the keepsake together, as the plan asks. That finishes the plan.

Code: `PlaceForYouSelf` (new); hooks in `PlaceForYou` (the snapshot, arrival, the evening's clock, right-clicks), `PlaceForYouRoom`
(the washing), `PlaceForYouSite` (the hooks), the account in `PlaceForYouTexts`, the Witness line in `WitnessAccount`, a fact kind in
`StaircaseProse` and `StaircaseAccount`, and the keepsake's line in `Keepsakes`. Art: three coats for the end hook
(`tools/draw_place_for_you_art.py`), and the rubbed-out face drawn in code (`HouseReadKitchenClient`), shown on the house-read client
proof's kitchen page beside the same picture with its face (`HouseReadKitchenProof`).

## What happens

- **The coat on the end hook.** The proposal: "Settled presentation changes the coat hung up for the reader." The household's two
  coats hang in the hall as before; for a reader read as MASC a short jacket with a collar and a patch pocket takes the second one's
  place on the end hook, for FEM a long coat with a belt and two rows of buttons, for ANDROGYNOUS a knitted coat. A reader not read,
  or read as UNSEEN, has no coat of theirs there. A hand on the hooks says which. Nothing else about the reader follows from it.
- **UNSEEN.** The proposal: "the family photograph leaves the reader's face indistinct and the hall glass is covered. One person can
  still recognize the reader through an action." The hall glass is covered before the reader comes in, and in their picture on the
  fridge (and the copy on the landing) the face is rubbed out, smeared past the pencil line as if by a thumb. Someone knows them anyway,
  by the first of three things they do:
  - **The routine they chose.** Done in the bathroom with someone's help or company, the one with them knows the order they do it in.
  - **Folding.** Upstairs with Mara, at the pillowcase, she knows how they fold.
  - **A hand on a chair back.** Standing behind their own chair, looking at it, for two seconds between supper and dinner, Ren knows
    the way they stand there (Mara, if Ren is gone).
  It is said once, over that person's head. Nobody explains it.
- **The hall glass, for everyone.** "Every reader can cover or uncover a mirror now. UNSEEN is not an exclusive escape ability." A hand
  on the hall glass hangs the cloth over it, or takes it off, or turns it back round. If Mara said at the door that she had covered or
  turned it for the reader, "Unless you'd rather I didn't", and the reader uncovers it, she lets it be.
- **The keepsake.** Once the account has been read, the place card at the reader's place at the table is theirs to take, once. The
  first time they come near the table after the account, a cue says it is still there.
- **The House of Leaves.** A new fact, `expected`, from how the reader left the house, once the account has been read.

The account gets a paragraph after the read's own (0.4.697) when there is something to say: the coat; for UNSEEN, the covered glass,
the rubbed-out face and who knew them anyway; and what the reader last did to the hall glass. The Witness line, for an UNSEEN reader
someone knew, gets a sentence after the read's.

## With more than one reader

Each reader's evening is a copy of its own: their own coat on their own copy's hooks, their own glass, their own picture and their
own place card. Who knew an UNSEEN reader, and how, is in that reader's record alone.

## New lines

Every line below is new in this release.

**Cues** *(new, flagged)*

- "One of their coats on the first hook, and on the end one, a short jacket. They've hung it where you'd reach for it." (and "a long
  coat with a belt", "a knitted coat")
- "Two coats on the hooks. Neither of them is yours."
- "You hang the cloth over the hall glass." / "You take the cloth off the hall glass." / "You turn the hall glass back round to face
  the hall."
- "Your name is still on its card at your place."
- "You fold your place card flat and keep it." / "You have the card."

**Over their heads** *(new, flagged)*

- Mara, the glass uncovered after she offered: "All right. I won't put it back."
- The one with them, by the routine: "That's how you always did it. Same order every time."
- Mara, folding: "Thirds, then the corners in. You always folded them like that."
- Ren, behind the chair: "You used to stand like that, behind your chair, till everyone else had sat down."

**The keepsake** *(new, flagged)*: "Place card", with the line "Your name, in Mara's hand, folded to stand at a place laid for you."

**The account** *(new, flagged)*: "A short jacket hung on the end hook for me." (or "A long coat with a belt", "A knitted coat"); "The
hall glass was covered before I came in, and in my picture on their fridge the face was rubbed out, as if by a thumb."; "<Who> knew me
anyway, by the order I did my <routine> in." / "by how I folded a pillowcase." / "by how I stood behind my chair until everyone else
had sat down."; "I hung a cloth over the hall glass." / "I took the cloth off the hall glass." / "I turned the hall glass back round to
face the hall."

**The Witness line** *(new, flagged)*: "Nobody there knew my face. <Who> knew me anyway."

**The House of Leaves** *(new, flagged)*, the fact `expected`, ledger "Mara's house: {x}":

- "A house you had never lived in laid a place for you. You left it {x}."
- "Your name was on a card at somebody's table. You left that house {x}."
- "They kept an evening for you in a house across the road. You left it {x}."
- Margin: "A house expected you. In the margin, a folded card with nothing written on it."
- {x}: "with its door propped", "with your place laid", "to the man at the door", "by the road".

## Cases

- `PlaceForYouTests#theFaceRubbedOut` (new): read as UNSEEN; the face in the picture rubbed out, the glass covered, no coat of theirs;
  the glass uncovered and covered again; the hooks answer with a cue; standing behind their chair, Ren knows them, over Ren's head; the
  account's paragraph word for word and in the account; the Witness line; after the account, the place card taken once, with its name
  and line; the House of Leaves fact. From the record: the three coats and none for an unread reader, the other two recognitions, and
  the fact for every ending and none before the account.
- `PlaceForYouTests#invitingThePast` and `#theDirectionOfCare`: the reader was placed at (6.5, -14.5) and (6.5, -15.5), inside the
  kitchen table, and pushed out of it wherever the collision put them. In run 1722 that left Mara unable to see the reader in the invite
  case, so she never turned to them. Both now stand at the table's north end, clear of it, and the invite wait says where everyone is.

## Results

Locally: `theFaceRubbedOut`, `theDirectionOfCare`, `invitingThePast`, `keepingAPlace`, `aReaderTheHouseKnowsTakesTheRoad`,
`theHouseAfterwards`, `marasRoom` and A Place for You's cast and together conduct checks, 9 of 9. The first run failed only
`theFaceRubbedOut`: its second click on the card found the card already gone, so the click no longer reached the card's own answer; a
click on the card's place after the account now goes to the card whatever is there.

Seen: eye-level frames of the hall with each of the three coats on the end hook and the hall glass covered. They showed the end hook
holds the reader's coat in place of the household's second one, so the hooks hold two coats, not three; the cue now says so. The
house-read client proof draws the fridge door with the picture and with its face rubbed out, side by side; the smear covers the face
and runs past its edge, and the eyes, mouth and tear are gone. The eye-level frames cannot show the fridge picture: they rebuild a scene
from its blocks and draw only a few kinds of block entity themselves, and the fridge door's picture is not one of them.
