# Writing and the blue word — 0.4.24

Custom pages use opaque connected strokes, explicit four-pixel spaces and a shared nine-pixel line grid. Will keeps a slanted hand; Karen and Pelafina use a steadier hand; Zampano is upright; the child keeps deterministic alternate letters; the notebook retains uneven scratched capitals. Hotel plaques keep their larger face. Correct equal-width glyph-map rows replace the escaped-character errors in the old child/hotel definitions. Every face includes native punctuation and a Minecraft fallback for other characters. The old Navidson and narrator IDs, and the child's private-use alternate IDs, remain valid for already saved books and resource packs.

The complete standalone word **house** is blue, regardless of case, including possessives and punctuation. Household, boathouse, player identifiers and other longer words retain their original formatting. Newly authored pages store the blue ink. A client-only Font mixin also colors existing saved books, item names/tooltips, dialogue, captions, native signs/labels, subtitles and chat at display time. It covers native String, styled sequence and outlined text paths. Paper uses a dark ink blue (`#2447B2`); HUD/chat text uses a lighter blue (`#789FFF`). Fonts, emphasis, strikethrough, click/hover actions, characters, glyph indices and native wrapping remain intact. The display pass does not rewrite signed messages, translations, saved inventories or other players' text.

No rooms or saved discoveries are rebuilt. Layout remains 21, network protocol 23; Witness remains twelve of sixteen eligible stories across at least two kinds, with three endings.

## Checking the writing

`/oldesthouse writing samples` gives six short specimens: Will, Karen, Zampano, child, Pelafina and claw. `/oldesthouse writing notes` still gives the seventeen serial-note previews without changing personal discovery progress.

`tools/generate_writing_fonts.py` is the common source for the glyph atlases. The household/novel asset generators defer their claw and Pelafina faces to it. `tools/verify_writing_fonts.py` checks imported files and the built JAR: PNG CRCs, glyph mapping, ASCII and narrative punctuation, opaque ink, native fallbacks, old alternate IDs and line bounds.

Five native GameTests cover styled and localized words, whole-word boundaries, saved child glyphs, source immutability and sink termination, and export the actual seventeen-note corpus for the client check. The opt-in client probe (`gradle runClient -PfontSmoke`, after `gradle runGameTestServer`) loads the real fonts, checks native page wrapping for the opening letter, novel correspondence and serial notes, renders all six hands plus uncolored saved-style text, and captures the native framebuffer. Normal play never opens the probe. Shader-pack and full playthrough compatibility still warrant an in-game check.
