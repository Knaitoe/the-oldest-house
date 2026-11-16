# Every face on its own picture (0.4.650)

The sweep the 0.4.649 note promised: every block and item model in the pack is now mapped onto its own texture, one pixel of
texture to one pixel of model. Nothing in play changes but how faces look. No Witness count, layout or protocol change.

## What was wrong

A model face takes its pixels from its texture by a uv, a square of 0 to 16 on that texture. Three things put a face off its
texture:

- **A uv named off the texture** (nine models). The game draws whatever lies at those coordinates in the sheet it packs every
  texture into, and does not stop at the texture's edge, so such a face shows a strip of some other texture: whichever one the
  sheet happens to put beside it. In the frames: the plain's note stand had dark marks along its top rail, and one of the tract
  house television's two aerials was brown wood. The others were slivers (the script stand's lip, the floor-plan stand, the
  crossing's bars, coil and floor grate, the lake house key bowl, the swimmer's cover).
- **A piece longer than its texture, with faces that name no uv** (29 models: foot- and headboards, wardrobes, the life-stage
  beds and drawers, doors, the borrowed-light mirrors, the Pitt's slabs, the town sign, the last stop's counter, a pipe and an
  outlet). A face that names no uv takes one from where it lies in the block, so a face 22 long gets a uv 22 long. Since 0.4.240
  the mod's `ModelUvRangeMixin` has squeezed such a face's whole texture onto it when the game reads the model, so its pixels were
  drawn taller (or wider) than they are wide: the child's bed's footboard had a star three-eighths taller than the one painted on it.
- **A face that names no uv and lies past its block** by less than a texture's length (144 models). The same mixin shifted those
  onto the texture, to its edge.

**A correction to 0.4.649.** That note said a face with no uv reaching past its block "can be off the edge of its texture and
into whatever is next to it in the atlas", and that the King's twelve models showed no fault only because of how the atlas
happened to be laid out. Only a uv *named* off the texture does that. A face with no uv was already kept on its texture by the
mixin, shifted or squeezed, which is why the King's frames looked right. The King's tool now writes explicit squares, as before.

## What is done now

`tools/flicker.py` fits every model as it settles it, in the tool that writes it (`flicker.dumps`) and across the pack
(`--cure`):

- A piece longer than its texture along a face that names no uv is **cut where the texture would begin again**, every sixteen
  on the block's own grid. Each part maps its texture one pixel to one pixel, and the texture carries on across the cut as if it
  repeated: the footboard above its bed shows the bottom rows of its boards again, square, and the star is the size it is
  painted. The parts keep the piece's turn and light, and every face but the ones the cut makes inside it.
- A face that **names its own uv** on a piece that is cut keeps its picture whole: each part names its share of it, laid on the
  face's corners as the game lays them (its rotation included). The town sign's lettering is still one picture across its three
  parts.
- A face's uv that still leaves its texture is **slid by the whole textures that keep the most of it on the texture**, as if
  the texture repeated: a face wholly past its block's edge comes on whole (where the mixin started it at the texture's edge),
  and one lying across an edge is then moved the least way onto the texture, which is what the mixin did.

`python3 tools/flicker.py --check`, in the quick gate, now also fails on any face mapped off its texture or any piece longer
than its texture: every tool that writes models through `flicker.dumps` writes them fitted, and the pack as it lies is fitted.
167 model files changed (40 gained parts). `ModelUvRangeMixin` stays: with the pack fitted it changes nothing, and it still keeps
a model the check has not seen on its texture.

## Checked

- `flicker.py --check` passes, and so does every quick check (each art tool's `--check` rewrites its models through
  `flicker.dumps` and agrees with the files).
- Look review: all 167 models from two sides each, before and after, from a headless client (56 pages). 61 changed and 106
  look the same. Every changed one was looked at side by side:
  - the bleeds are gone (the note stand's rail is clean, both aerials are metal);
  - squeezed faces are square again (the child's bed's footboard and headboard, the nursery bed, the open box,
    the gallery doors, the mirrors, which now show their glass square, the outlet's pipe);
  - pictures named across a cut piece are whole (the town sign, the award plaques);
  - nothing that was right before is wrong now.
- The first pass started every span lying across the texture's far edge at its near edge. The frames showed it (the swing's
  bracket, reaching half a pixel below its block, took the top rows of its wood where it had the bottom ones) and eleven models
  changed for no good reason. Such spans now move the least way, as the mixin moved them, and those eleven look as they did.
- Fewer faces draw nothing than before: 300 against 310 (below).

## Found, not changed here

Scanning the pictures behind every face (a check written for this review, not yet in the gate) found 300 faces in 62 models
that map only onto fully transparent pixels of their own texture, so draw nothing. They were there before this sweep (310 under
the mixin's mapping): the sweep made twelve of them visible (the Stanley table candles' flames), and two faces of the crossing's
frayed coil, which named a uv off their texture and so showed whatever lay beside it, now fall on its empty corner. Some are whole parts
missing: the Stanley dining table's candle has no body (its faces fall on the empty middle of the vanilla candle sheet, so its
flame now stands on nothing), the dead-air mast's pole, the Kept chandelier's cable, the Usher candelabra's flames. Others are the
edges of thin plates falling on a texture's empty margin. They are the next slice (0.4.651).
