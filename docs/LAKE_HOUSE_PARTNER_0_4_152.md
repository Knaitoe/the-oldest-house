# The lake house partner — 0.4.152

The owner's October 28 note: Tess could have a male variant, chosen by the reader's romantic interest, and the writing has to follow. `docs/HOUSE_READ_DESIGN.md` already planned it under `DRAWN_TO`:
- Tess for `WOMEN` and `BOTH`;
- a young man in her place for `MEN`;
- Tess as a friend rather than a date for `NO_ONE`.

## Who the reader goes down with

- **The read.** `HouseRead.Trait` gains `DRAWN_TO` (`MEN`, `WOMEN`, `BOTH`, `NO_ONE`), and `HouseRead.drawnTo` returns it only when settled, since the design counts the partner as a larger change. The shelter, which will read it, is not built yet, and nothing writes categories (the owner deferred the classifier). Until then every reader meets Tess, on a first date, exactly as before apart from the writing below.
- **Chosen once.** The partner is chosen on the reader's first visit and saved with their lake house record (`Partner0152`: `tess`, `theo` or `friend`), so they never change partway through the story, even if the read changes later.
  - A reader who had already met Tess before 0.4.152, or is past their first visit, keeps Tess.
  - A tentative read keeps Tess.
- **Theo.** Theo is a working name; the owner may change it. He is the young man in Tess's place, of her age:
  - dark brown hair, brown eyes;
  - a navy short-sleeved swim shirt and trunks to mid-thigh with a pale hem, barefoot;
  - painted with the shared character painter (`tools/paint_character_skins.py`) on the same pixel grid as Tess, and added to the native character art review.
  - He is his own actor role (`LiteraryActor.LAKE_THEO`), so he has no long-hair meshes, and he does everything Tess does: rows behind the reader, swims beside them, points up underwater, minds the compressor on the second visit, says goodbye after the ending, and leaves when a second reader arrives. Since 0.4.156 that second reader can go down in his place, seen as him (`docs/LAKE_HOUSE_PAIRS_0_4_156.md`).
  - Like Tess, he is the reader's alone, and spectators never see him.

## The writing

Tess's lines are written without he or she, so most stay as they are. What changes:

- **The name.** Every line is headed with the partner's name, and the narration names them: "Theo mouths something, then points upward.", "A black dress is coming down through the water. Theo points up." The second of these now names the partner only when they are there with the reader.
- **The first greeting** sets the tone, then gives the rule as before:
  - on a date (Tess or Theo): "I didn't think a first date would come with a culvert. We can go through that tunnel. But if there's something down there, let's not ask how."
  - as friends: "Some way to spend a Saturday. We can go through that tunnel. But if there's something down there, let's not ask how."
- **The account** opens with who the reader went down with, once they have met on the dock:
  - "Theo and I took the green canoe out on our first date."
  - "Tess and I took the green canoe out one weekend, the way friends do."
- **The Witness line** names them the same way: "Theo and I went out on the lake on our first date and found a house on the bottom of it. Every time we went down it let us a little further in. We kept to one rule and didn't ask how. In the end I went back to meet whoever lived there."
- **Older records.** A reader who never met a partner, and every record from before 0.4.152, keeps their account and Witness line word for word.

## Tests

One native case joins the lake house cases (15):
- a reader settled as drawn to men meets Theo, his own actor, on a first date; spectators never see him; his account and Witness line name him; a later change of read keeps him;
- a reader settled as drawn to no one meets Tess as a friend, and her account says so;
- a tentative read, and no read at all, meet Tess on a date;
- a reader who met Tess before the partner was chosen keeps her, and an older record keeps its account word for word.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings. One additive skin (`literary_lake_theo.png`); every other PNG is unchanged.
