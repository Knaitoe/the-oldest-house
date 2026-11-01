# Traces in the stone: the grit gallery (0.4.234)

The owner, 1 November, asked whether the dust stopping at the sixth crossing was the proper design move. It was right for the
shut-up rooms themselves: they are a plaster-and-paper hall, and once the halls turn to stone at seven, a domestic hall there would
break the House's rule that the deep ways on are stone. But the idea the dust carries ("Readers can know a room by what they did in
it") should not end where the plaster does. Traces matter more in the depths, where readers return less and the halls are harder to
tell apart. So the stone depths get their own room that keeps prints, made of what the stone has: grit shed from its own vault.

`labyrinth/TraceRooms.buildStoneDust`, a new kind of the existing `trace_fitting` block (`GRIT`), and one new gray place (slot 154).
Layout 77; no vignette, source kind, Witness or protocol change.

## The grit gallery (STONE_DUST, slot 154)

An ordinary stone hall, built whole, from the turn to stone (seven forward crossings) until only the dark halls remain (twenty-one),
at the weight the shut-up rooms had above it (12). It is one of `StoneHalls.isStone`, so it counts as stone everywhere stone counts:
the stone-depth rule, the hall atmosphere and its stone sound, and the "stone" step of the House's progress.

- **The gallery.** Aged masonry (the stone halls' own cracked and mossy brick), a vault chamfered with upturned steps, ribs on
  pilasters every six blocks. One rib has come down: its gap in the vault, its stones on the floor under it, where they fell. A side
  door and a far door lead on.
- **Someone's place.** A table, a loose chair, and a lantern left standing on the floor by the table. Nothing explains who.
- **Grit.** Every floor cell is grit over polished andesite (`TraceBlock` GRIT). It keeps prints and scores exactly as the shut-up
  rooms' dust does (the same five stages, the same path of 48 cells for each reader), and nothing tidies it away. While a reader is
  in the gallery, a fleck of grit falls from the vault near them now and then, for them alone, so it is plain where the grit comes
  from.
- **The chair** stands in grit (its stage 2), is pulled a block at a time and turned crouching as the dust chair is, and scores the
  grit it leaves; it scrapes on the stone as it goes.
- **Dependable returns and the rare exception** are the same as in the dust: prints, a chair or a wedge found as left is a
  dependable return; after six of them, one departure in four may add a second set of prints beside the reader's own, or a second
  chair across their table, once to a place for each reader and with nobody there to see. The gallery is its own place, so a reader
  who has had the exception in the shut-up rooms can meet it once more down here.
- **The wedge** holds the gallery's entry as it holds any ordinary hall's.

## Art

`tools/draw_trace_fittings.py`: grit (clean, prints, the second prints, both, dragged) over a stone floor, cooler and coarser than
the dust, with darker grains and pale chips (22 textures). Its check holds that clean grit shows no bare stone and walked grit does.

## Tests

`TraceRoomTests#theGritGalleryKeepsPrintsBelowTheTurnToStone` (exploration namespace): an ordinary stone hall built whole, with no
story, built ahead of the stone depths and dealt from seven to twenty, where the shut-up rooms leave off; grit over a stone floor
and masonry walls; the table, the lantern, the fallen rib; the side and far doors; a walk printing the grit north and nowhere else,
the path the reader's own; the chair pulled a block into the grit, scoring where it stood; six dependable returns and, leaving with
nobody looking, a second set of prints beside the reader's own, once.
