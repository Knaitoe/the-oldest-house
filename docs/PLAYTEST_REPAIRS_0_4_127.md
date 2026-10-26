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


## Implementation checkpoint

The source now includes the listed encounter, mining, anatomy, hostile/friendly, projector, Plains pose/camera wording, junction note, and child-bed/dialogue corrections. Ten new native cases exercise actual controller use, contact, original custody and reader isolation; declared corpus 767. The added standard-UV family and bent-neck sheets pass the shared painter/art catalog check (154 human selections, all 649 protected PNGs unchanged). The body refit is loaded, vacant, 128 visits / 3 ms per tick, saved and once-only; it preserves recorded cuts, existing block entities, original worker/key/progress and player edits. Native and visual verification is pending.

Owner's additional house scope follows this repair build: only a small selection of domestic fragments becomes deeper handcrafted homes; most remain shallow glimpses. Four distinct households (parents with several children, student rental, solitary drinker, grandmother), contextual physical interactions, custom materials as needed. Sparse inert tossed furniture/floor scrapes are separate atmosphere. The latest composition photograph also calls for purposeful positioning of room furnishings instead of isolated stock cubes surrounding a lectern. These additions remain in progress; they are not claimed implemented by this checkpoint.


Dinnerbone follow-up: retain the original ceiling actor, native inversion, dark skin, unseen arrival/turning and stored UUID. Its idle uses long held poses, a few degrees of slow head inclination, a slight weight shift and a delayed movement of one hand. The feet stay planted; the whole body is not rotated a second time. Timing derives from saved world time and the original UUID, with a varied long cycle. No repeated voice or added attack. This is a mod interpretation of the source film's use of implication, empty space and practical light, informed by [Kyle Edward Ball's interview](https://www.inverse.com/entertainment/skinamarink-director-kyle-edward-ball-interview), rather than a claim that this exact animation occurs in the film.


Repair validation checkpoint: local Java 21 compilation and all 44 native uncanny cases pass after correcting the bed push to the side clear of its nightlight, moving the patron-contact fixture out of a parked pickup, and following the new human anatomy in both original-key routes. The tunnel now progresses through green signals independently of appearance, cannot credit sight of a dormant invisible body, and chooses an unseen collision-free bay between ceiling fittings. The obsolete proximity teleport beside false frames is removed. Full exact-source CI and inspection of the new Dinnerbone/Plains views remain required. The temporary tooling bootstrap workflow has been removed.
