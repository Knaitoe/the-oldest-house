# No two faces flicker, anywhere in the pack (0.4.597)

## Where this comes from

The owner's standing instruction is "ok. please work your way down the list". The next item on that list was a flicker audit of every model in the pack.

Flicker has been found three times before, each time by eye and one model at a time: the trailer's griddle (0.4.201), the bikes (0.4.463, when `tools/flicker.py` was first written and two tools began to use it) and the Pell house's quilt (0.4.577). This release looks at every model at once, cures what it finds, and makes the workflow refuse a model that flickers.

No protocol, layout version or Witness change. Nothing in the game's code changes. New builds only.

## What flicker is

Two faces of different pieces that lie in one plane, face the same way and overlap are drawn at the same depth. Where they show different colours, the game draws one in some pixels and the other in the rest, and which one changes as the reader moves. On a still picture it is a speckle or a streak. In play it crawls.

Three things look like it and are not:

- Two faces with the very same four corners. The game draws the later over the earlier, and its own grass block relies on that.
- A clear pixel. It fights nothing.
- The same picture laid the same way on both faces.

## What the tool did not know

The tool from 0.4.463 looked at the shapes of the pieces and at one colour for each face. It could not read a picture, so it could not tell a real fight from two faces showing the same pixels, and only two tools called it. It now:

- lays each face's picture as the game does (the corners' order, the default placing, a turned or mirrored placing, a turned piece and its rescaling are taken from the game's own source);
- compares the two faces pixel for pixel over the shape they share;
- leaves out what a third face hides;
- follows a model's parents inside the pack;
- reads the first frame of a moving picture.

## What it found

The pack has 7,202 models. 5,553 of them have pieces, their own or a parent's.

| | Before | After |
|---|---|---|
| Models whose faces flicker | 514 | 0 |
| Pairs of faces | 1,829 | 0 |
| Area, in square pixels (a block's face is 256) | 11,626 | 0 |
| A see-through face laid twice | 0 | 0 |

Of the 514, 197 flickered over more than sixteen square pixels and 39 over less than one. Most were in the oldest models: 228 in the top folder, 74 in the household's, 48 in the leaks', 23 each in the crossing's and the lodging's.

## The cure

**What was tried first.** Moving one piece's side out by a hair. It cured the face and moved the piece's other faces with it, which left a hairline strip at a seam in 93 of 504 models. It was dropped.

**What was tried second.** A bare copy of the face that should show, with no thickness, a twentieth of a pixel in front of the face behind. From the front it was cured. Where two copies met at a corner, a line a pixel wide ran down between them: the piece behind, seen through the gap. A proof frame of the courtyard's chair showed it.

**What was tried third.** The same copy closed all round with four sides back to where the face was. The corner was cured. But a side facing up is lit brighter than the face it stands on, and wherever the piece behind carried on past the copy's border the side stood proud of it and showed as a line of bright dots. The proof frames showed that too.

**What is done.** Over the face that should show, a thin piece is laid: a copy of that face a fiftieth of a pixel in front of the face behind. Along its border it has a side back to where the face was, but only where nothing lies beyond the border in the face's plane. A side is drawn as the piece's own face on that side is at its edge, so that face seems to run on over it. Where the face behind carries on past the border there is no side, and what can be seen under the copy from a slant is that same face, beside itself. Nothing that was there is moved or changed. A model reads as it did, with a few more lines at the end.

```json
{"__comment": "laid over the face behind it so the two do not flicker (tools/flicker.py)", "from": [0.5, 0, 1.98], "to": [5, 4, 2], "faces": {"north": {"texture": "#tread", "uv": [0, 0, 16, 16]}, "west": {"texture": "#tread", "uv": [0.02667, 0, 0, 16]}, "up": {"texture": "#tread", "uv": [0, 0.02667, 16, 0]}, "down": {"texture": "#tread", "uv": [0, 16, 16, 15.97333]}}}
```

**A side that would itself clash is cut back.** A side can land in one plane with the face of some other piece that stands against the border. There the side is cut away, and the other piece stands in its place. What is left of a side is kept as strips of its own. Of 33,550 pixels of border round the laid pieces, 18,225 have a side over them.

**Which face shows.** Where one face is already a little behind the other, it stays behind. Of two in one plane, the smaller shows. Of two the same size, the later piece shows. The smaller is nearly always the thing laid on the larger: a clasp on a ledger, a grille on a record player, a rail on a bed's head.

**How far proud.** A fiftieth of a pixel, and it is a trade. What is laid is seen as a step that deep: about a pixel on the screen from one block away, half a pixel from two. And it holds only as far as the game's depth can tell it from what is under it: by arithmetic (a near plane at 0.05 blocks, 24 bits of depth) about thirty blocks, past which the speckle comes back, a few pixels across at that distance. A twentieth held to fifty blocks and was a pixel wide from two blocks away. A fiftieth is also the gap two models already had by design, and they are let be. Where one laid piece lies over another, the upper stands two or three fiftieths proud: 153 of them do.

**Told from the pieces alone.** The cure does not read pictures. A tool has to be able to settle a model before its pictures exist, and a picture can be redrawn later without the model being rebuilt. By the pieces alone 528 models clash, a few more than flicker by the pixel today.

**One by hand.** The payphone's water is see-through, and a copy laid over it would have drawn it twice. Its back and underside now stop a hair short of the set's, in its own tool.

In all, 1,856 pieces are laid in 525 models, with 3,119 whole sides and 2,313 strips, and the three payphones are redrawn: 528 files. The pack's faces go from 112,503 to 119,791.

## How it stays cured

- A tool that writes a model writes it with `flicker.dumps`, which settles the model as it is written. 142 tools do, at 331 places.
- A tool that checks a model compares it with `flicker.settled`. 78 tools do.
- A tool that builds one model from another reads it with `flicker.bare`, which gives the pieces without the copies. The household's toppled chairs do.
- The quick job runs `tools/flicker.py --check`. It fails on any model in the pack whose faces flicker by the pixel, and on a see-through face laid twice.
- `tools/flicker.py --cure` settles the whole pack in place, in each file's own layout. Run again, it changes nothing.

Thirty-four tools write models without naming their pieces in a way the change could find. Their models do not flicker today, and the workflow's check will stop one that later does.

## What changed in the repository

- `tools/flicker.py`, rewritten.
- 143 other tools: how they write and check.
- 528 model files.
- `.github/workflows/build.yml`: one line in the quick job.
- the working notes: the rule.

## Checks

All of these were run on the cure as it ended, unless it says otherwise.

- `tools/flicker.py --check` passes. `--cure` run a second time settles nothing.
- All 198 tool commands the workflow runs pass. They passed before the change too.
- Six tools were run in write mode (the lodging's things, the household's lives, the payphones, the fan ladder, the night ward, the bike night), with every model, state file and picture in the pack hashed before and after. All came back byte for byte but one number: the night ward's tool writes the bedside table's height as `16` where the file has held `16.0` since before this release. The game and the check read them as the same number, and the file is left as it was.
- Thirty-six models, the worst of each kind, were drawn before and after from six sides and looked at. That was with the second form of the cure. Which face shows has not changed since.
- The client proof chain passes, 64 of 64.
- Its 329 frames were compared with the frames from before the cure. 253 are the same to the pixel. All 76 that differ were looked at:
  - In 27 the difference changes from run to run and has nothing to do with the cure: the wallpaper that moves, the phone's blurred view and its playback, a few pixels of text.
  - In the other 49 it is the cure. The speckle is gone from the fan ladder's top, the ward's chairs, the fountain's rim, the remembered chair, the cottage's chess table and the courtyard's chair. Elsewhere it is a line under a pixel wide where a copy sits a hair in front, or two faces of the same wood changing places.
- The courtyard's chair, the ward's chair and the fountain's rim were looked at enlarged in each form the cure took. In the last there is no line at the corner and no bright dot along an edge.
- The quick checks, and the package and branding checks on a built jar.

## Not checked

- Nobody has walked round a cured model in play. The proofs are still pictures.
- Which face shows was looked at on 36 of the 528 models, and on what the proof frames happen to hold.
- The sides were looked at only in those frames: a few chairs, a ladder, a fountain, a casket.
- How far away a copy holds is arithmetic. No client was set thirty blocks from one.
- Not covered at all: the bodies drawn in code (people, creatures, the canoe), a face of one block against a face of the block beside it, and two models drawn in one block by a state file.

## For the owner

- **The smaller face is in front.** It is a rule, not a judgement on each model, and on some it is a choice you may want the other way. Two the proofs show: at the top of the fan ladder the two leg ends now show in front of the iron bracket that joins them, and on the ward's chair the seat's edge runs across the leg. Say which models read wrong and I will put the other face in front in their tools.
- **Two gaps of 0.02 pixels are let be** (the model home's television, the star ceiling). They were drawn that way on purpose and hold to about thirty blocks.
- **How far proud** is the one number that changes what you see. Thinner shows less up close and gives out sooner far off. If you see speckle on something across a street, say so and it goes up. If you see a bright edge on furniture at arm's length, it comes down.
- The other numbers (how near two faces must be to count as in one plane, how much of a face must be hidden for it not to count) are mine.
