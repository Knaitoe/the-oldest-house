# Playtest corrections, 0.4.127

Based on de949298, retaining the 0.4.125 environment and 0.4.126 object-handling work in PR 10. This is an implementation checkpoint, not a verified release.

Owner's combined scope:

- Repair the open mailbox: real hollow compartments, attached swung handle, valid local sprite UVs, matching collision.
- Hingeless frames can be examined but never opened by hand; automatic openings have a visible solid black recess rather than access to unbuilt space. Review doors, routes, support and fall containment.
- Floodlight vignette: design a restrained, physical motion/stillness mechanic and a less passive visitor. Proposed only until the design is settled: a courtyard fitting operated from inside, decaying motion rather than a fixed light timer, small changes visible in successive periods of stillness. Retain a real hinged escape, no repeating narration or arbitrary waiting gate.
- Last Stop: actual patron contact starts the personal conclusion even if the player missed the elk; slower walking, patrons disappear only for the resolved reader, bartender acknowledges past closing and the reader's appearance.
- Second Waitress: a subtle entrance greeting, actual waitress seats the reader and returns to the front; the second arrives discreetly afterward. No early explicit warning before she is solid. The monstrous appearance requires eating her meal, not elapsed time alone. Keep finite original food and independent readers.
- Painted Interior: wire the current ghoulish family art to the actual peripheral family, and make actual contact a registered failure independently of gaze/corner conditions.
- Layered Man: all actual tissue is cuttable by the cleaver; hidden unbreakability, slightly slower spread, recognizable growing human anatomy with the key inside, an actual saved overrun failure at the basement containment boundary, and the original worker's brief call for help on entry.
- Improve the physical camera fitting's native pixel materials and model detailing.
- Tunnel woman: dedicated art, bent neck rather than a crawling torso, delayed/specific appearance states, hostile combat classification.
- Friendly cast: multiple deliberate strikes with private restrained warnings before ejection; hostile actors excluded.
- Junction: move the original note lectern away from the west-door approach, preserving the original book/page in existing saves.
- House disturbances: sparse inert overturned furniture and directional floor scrapes, clear of doors and normal walking routes, preserving original usable furniture/custody.

Validation still required: native gameplay regressions, saved-world/peer preservation, native visual inspection and complete exact-source CI. Keep 52 eligible sources / 39 required / two kinds / three endings, protocol 88 and layout 46/manor 2.
