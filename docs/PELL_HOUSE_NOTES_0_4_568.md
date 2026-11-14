# The Pell house, the owner's playtest notes: Tilly's hair (0.4.568)

From the owner's notes on playing the Pell house on 13 November (the first two batches: `docs/PELL_HOUSE_NOTES_0_4_566.md` and
`docs/PELL_HOUSE_NOTES_0_4_567.md`). New builds only; no layout or Witness change. Protocol 190: a reader playing Tilly is told how
long her hair has grown (below).

## Tilly's hair grows

The owner: "tilly should also have visible hair growth. we have the texture overlay for that, right? mimicking the sheet physics".

**What the story gives it.** Tilly's skin paints her hair as she came, to her jaw: "Dad did it in one go. It always came out
crooked." June brushes it at bedtime and says "There. It'll grow long here." Until now it never did.

**How long it grows.** It grows faster than hair grows:

- **The first visit.** As she came, to her jaw.
- **A week in.** Past her shoulders: down her back to her shoulder blades, and over each shoulder onto her collarbones.
- **The last night.** As long as Nessa's: down to the small of her back, and over her shoulders onto her chest.

The length is the visit's (`PellPeople.hairFor`, kept in Tilly's marks by `PellPeople.growHair` while the house plays the visit).
Nothing else grows it.

**Nessa.** Under the freezer's lid she has the same length, near black, with frost in it. On the last night the two are alike.

**How it is drawn** (`client/PellHair`). It is drawn over the cast model from the body, so it hangs as Tilly stands whichever way she
turns her head:

- four strands down her back from under the nape, a little uneven at the ends;
- a lock over each shoulder onto her chest;
- each strand and lock in a few links, from its own sheet (`tools/draw_pell_hair.py`, in the skins' own browns, at the skins' grid);
- streaks that run on unbroken from link to link, the side toward her body in shadow, and ragged ends.

**How it moves.** It moves as Tess's hair moves in the lake at the lake house (`LakeHairDynamics`), but on land. Each link is on a
spring:

- it swings back as she walks, and further as she runs;
- it swings aside as she steps sideways or turns, the ends lagging;
- it lifts as she drops and settles as she rises;
- it bobs with her stride;
- it stirs a little while she stands.

The locks never swing into her chest. Lying down (asleep in Nessa's bed, or Nessa under the lid) it lies spread on the pillow above
her head and does not move.

**A reader playing Tilly.** Their figure has the same hair at the same length. Their look (`PellTillyPayload`) now says how long it
has grown, which is why the protocol moves to 190: an older client would not read the field.

## The line across a sleeper's face

The owner, on the render of the hair: "Do you not see the dark line on her face?" Nessa under the lid and Tilly asleep each had a dark
bar straight across the face at the eyes. I had passed the render without seeing it.

It was not the hair. When the cast close their eyes (asleep, or lying still) the model draws a strip of lid over the eye row
(`LiteraryRenderers.CastModel`, `eyelids`). The strip had no depth, so it had a back face as well as a front. The back face took its
pixels from the side of the head, which on these skins is hair, and it is drawn after the front, so the hair won. It has been so
since the sleeping pose came in (0.4.202), for everyone who uses it: the Pell people, the brother in Mapping the Interior and the
people in bed at People Lights.

- The lid is drawn from the front only.
- On the Pell people it sits half a row higher. Their faces have the eyes half a row above where the lid sat, so it covered only the
  lower half of each eye and the row under it. Now it covers the eyes.

## Run 1599

Two structure jobs failed on 0.4.566.

**A Place for You with two readers.** The cast checks found two faults:

- **Mara in the wall.** On her way to her chair Mara stood in the jamb of the kitchen archway for a few ticks. The last of the way
  into a chair is a short slide, and the slide went in a straight line through the corner. She slides now only when the straight
  way is clear of the walls (`PlaceForYou.slideClear`); until then she walks round.
- **Alex saying the same line again.** Behind this was a real fault. The reader clicked Alex every few seconds, and every time he
  began their first conversation over again. `LiteraryVignettes.talk` handed the scene a copy of the reader's record, then saved
  that copy over whatever the scene had saved itself. A Place for You saves through its own reads, so who the reader had talked
  to was lost each time. The talk and the blow dispatchers now save only what was changed in the copy, laid over the record as it
  stands (`saveOver`); a scene's own saves are kept, in every scene. The check also gave the panel's answer to whoever stood
  nearest, which at the supper table was Kit; anyone near the panel may now answer it (`CastConduct.mayAnswer`).

**Two structure cases.** `marksStayPhysicalAndObservedMarksAreProtected` and the case about watched mobs outliving an empty world
need a world nobody is in. A mock player from the two-reader door case added in 0.4.565 was still standing in it: that case made
a player and never removed it. It removes it now.

## Still to come

The rest of the notes, in the next releases:

- The sleepers' pose.
- Faces.
- What the people and the readers do through the day.
- The interior.
- The flower bed.

## Tests

The Pell house namespace:

- **The first visit.** `thePeopleStandWhereTheFirstVisitPutsThem` finds Tilly's hair as she came.
- **A week in.** `aWeekInTheHouseIsAsJuneHasMadeItAndSheSlipsOnTheName` finds it past her shoulders.
- **The last night.** `theLastNightBeginsWithThePoolLitTheKnivesPutAwayAndCallumOnTheLine` finds it at its longest.
- **A reader as Tilly.** `aReaderAsTillyInJunesHandsIsAskedOnThePanelAndAnswersOrNot` finds that reader's look sent with the last
  night's length.
- **The sheets.** `tools/draw_pell_hair.py --check` is in the quick checks. It reads the lengths and places from `PellHair` and fails
  if they disagree.
