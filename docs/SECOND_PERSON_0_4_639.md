# Second Person: why they are here (0.4.639)

The proposal's AGE variant ("Why they are here", docs/SECOND_PERSON_PROPOSAL.md, chapter 1). New builds only. No layout, protocol
or Witness change: 90 sources / 68 required / two kinds / three endings. A reader the House has not read keeps the summer exactly as
it was.

## What changes

The snapshot taken on arrival now keeps the House's read of age (`Age`, tentative or settled) and whether it was settled
(`AgeSettled`). `SecondPersonStory.age(own)` returns DIGITAL, TRANSITIONAL, ANALOG or nothing.

- **The chair on the landing (ANALOG, tentative or settled).** As the resident comes up with the last box, a green armchair is set
  on the landing at (7, UP, -24), its back to the landing's east wall, facing in. It is clear of the stair (x 4-5) and of both doors.
  It is the "chair that will not go through the door". The flat's reset clears it. Following the proposal, a tentative read changes
  only the objects; the chair is the one object built here, so a tentative DIGITAL or TRANSITIONAL read changes nothing yet.
- **What they say (settled only).** Sitting on the last box, after "Right. That's everything.", the resident says one line for
  their age.
- **The account (settled only).** One sentence after the unpacking paragraph.

Judgement: the proposal's laptop, air mattress and photographs with nowhere to go are not built in this slice; the lines and the
chair carry the variant. They can be added as objects later without changing the record.

## New lines (all new in this slice)

Said on the last box:
- DIGITAL: "Every kitchen box labelled from home. KETTLE. MUGS. FIRST NIGHT."
- TRANSITIONAL: "\"KITCHEN, not the good knives.\" Right. Half of everything."
- ANALOG: "The chair'll have to stay on the landing. It won't go through."

In the account:
- DIGITAL: "It was their first flat. Every kitchen box had been labelled at home."
- TRANSITIONAL: "Half of a shared life had come up the stairs, in boxes labelled in someone else's hand."
- ANALOG: "A long life somewhere else had come down to what would go through the door. The chair stayed on the landing."

## Checks

- `SecondPersonTests#whyTheyAreHere` (batch second_age): every age has a line and an account sentence; a tentative age changes no
  words; no read leaves the summer as it was; a settled ANALOG reader gets the chair on the landing as the resident comes up, and the
  account keeps it.
- Review frames of the chair on the landing floor against the wall (from the stair, the flat door, above and behind): it stands on
  the floor, faces into the landing, and its back meets the wall.

## Multiplayer

Each reader's flat is their own copy and the read is from their own snapshot, so one reader's chair is never on another's landing.
