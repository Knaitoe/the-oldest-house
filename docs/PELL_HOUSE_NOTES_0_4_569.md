# The Pell house, the owner's playtest notes: faces (0.4.569)

From the owner's notes on playing the Pell house on 13 November (the earlier batches: `docs/PELL_HOUSE_NOTES_0_4_566.md`,
`docs/PELL_HOUSE_NOTES_0_4_567.md` and `docs/PELL_HOUSE_NOTES_0_4_568.md`). New builds only. No protocol, layout or Witness change.

## Faces that speak, blink and change

The owner: "npcs should have wired faces/mouths/blinks/emotes as in ghosts set, too". Then, while the first version was being checked:
"You can try to paint unique expressions - a character who defaults to a grimace might have a harsher smile, etc".

The first version moved the same few pixels on every face: a lid, a brow and a mouth line, alike for all of them. It was set aside
for the owner's way. Each of the Pell house's people now has looks painted for them alone, on their own skin's face grid, in their
own colours, and each look is drawn the way that face would make it.

**What each look is.** For each look, their sheet (`pell_faces_<name>.png`, drawn by `tools/draw_pell_faces.py`) holds:

- the upper half of the face (hair, brows, eyes) with the eyes open, and with them shut;
- the lower half (mouth, chin) closed, a little open and wide open.

The face is drawn over the head from one upper half and one lower half (`client/PellFaces`), so any look can blink and speak.

**Blinks.** Each blinks on a clock of their own, every four to eight seconds, for three ticks. Asleep, lying still, and Nessa under
the lid, the eyes stay shut.

**Speaking.** When a line of theirs goes up over them, the mouth opens and shuts on its syllables, a little and wide, with pauses
between the phrases, for about as long as the line takes to say. A line already up when they come into view was said before the
reader saw them, and their mouth does not move for it. Callum chews while he eats. Every look can speak except Nessa's.

**Looks.** Each has a face they rest in, a face for what they are doing, and now and then, for a few seconds, a passing look.

## Who they are

The owner's rule, a harsher smile on a face that rests in a grimace, is the rule for every look: each is drawn from the face the
person rests in.

**June.** She rests in a small closed smile that does not reach her eyes.

- Brushing Tilly's hair, or holding her in the water, her brows soften and her eyes half close: tender.
- At the line at night, pressing her under, and in the swim and the photographs: set, the brows down and the mouth pressed.
- In the water after, and now and then anywhere: the smile is simply not there, and she stares.
- With her hand wrapped, it hurts her now and then: one eye squeezed, the teeth clenched.
- Led by a reader's hand: lost, eyes wide, mouth slack.
- On the stool by the freezer: grief, with tears and a trembling mouth.

**Tilly.** Her face is open. Now and then she listens, eyes up and mouth round, or smiles to herself. At the window at first light
her grin is a child's, a tooth out. Having her hair brushed she is content. Led by June's hand she is unsure.

**Callum.** He rests drawn: shadowed eyes, the mouth turned down in a grimace.

- So his smile on the step at first light is a harsh one: tight, pulled up on one side only, the teeth showing on that side and a
  crease pulled into the cheek by it.
- He looks up now and then.
- His anger at the knife bares his teeth.
- Mimicking a reader's crouch, his face is empty.
- Sitting at the table he is calm; eating, he chews.

**Gail.** Her smile is broad and easy. Now and then concern presses it flat. Holding a page out, she is earnest.

**Nessa.** Her face does not change: shut eyes, frost on the cheeks and brow, a cold mouth.

## How it is drawn

The Ghosts set's faces are built into its own mesh (`GhostsSkinMesh`), with a jaw and lids as parts. The Pell people are drawn by
the cast model that every literary actor shares, so their faces are drawn over it instead:

- a layer on the cast renderer (`PellFaces.Layer`) draws two quads a hair in front of the face, from the chosen cells of their
  sheet;
- the cast model's own shut lids are put away while a painted face is drawn, so the two never cover each other;
- the hat layer is drawn after, so Tilly's hat band stays in front;
- nothing is drawn while the head is hidden or the model is faded.

What they say is not marked as speech on every line, so the client watches for a new line going up over them (the name the line
is shown in).

## Checks

- **The sheets.** `tools/draw_pell_faces.py --check` is in CI and the quick checks. It reads the looks and their order from
  `PellFaces`, fails if they disagree with the art, and fails if any skin's face is not painted on its own grid.
- **The render.** `CharacterArtVisualProof.verifyPellFaces`, in the native character-art check:
  - every sheet is the size its looks make;
  - each person's resting look shuts its eyes for a blink and opens its mouth to speak;
  - the face is two quads exactly over the face, from the upper half of one cell and the lower half of another;
  - a painted face puts the cast's own lids away;
  - anyone else asleep (the brother in Mapping the Interior) still has the lids, one front face on the cheek rows (0.4.568);
  - the looks follow what each person is doing: June's rest, slip, tenderness, setting and grief; Tilly at the window; Callum's
    rest, anger and harsh smile; Gail with the page; Nessa unchanged.

## Still to come

The rest of the notes, in the next releases:

- What the people and the readers do through the day: idle behaviour for June, Tilly and Callum, and things for readers to do.
- The interior.
- The flower bed.
- Each vignette door's own bleed and sound in the halls.
