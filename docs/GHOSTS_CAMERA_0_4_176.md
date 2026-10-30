# The Ghosts camera: click to roll, click to stop — 0.4.176

The owner:

> "Can you ensure the camera in ghosts set behaves like the camera in the canoe, too? Players can click to record and click to stop recording rather than having to hold it?"

And, on the journal plan:

> "yes, the house should quote the player in some carefully chosen instances."

## The camera

The canoe's phone starts recording on one use. The Ghosts set's camcorder used to film only while use was held down. It now works like the phone:

- **One use starts it rolling.** "Recording." shows above the hotbar, with the same record sound as the phone.
- **The next use stops it.** "Stopped recording." shows, with the sound a little lower.
- **Holding the button down counts once.** It starts or stops the camera a single time; it does not flicker.
- **It films while it is in the reader's hand and rolling.**
  - Put away, it films nothing.
  - Taken out again, it is still rolling.
  - The red REC light and the first-person viewfinder follow the same rule.
- **The reader walks at full pace while filming.** Holding use used to slow them to a camera walk.
- **Using it on one of the family toggles it too.** Put it away to talk, as before.
- **It films only on the set.** Carried out of it, it stops. Used off the set, it says there is nothing to film.
- **What counts as filming is unchanged:**
  - it must be the reader's own camera;
  - the shot must be centred and not blocked;
  - each frame's nonce is used once;
  - day four's privacy is respected only when the reader stops recording or puts the camera away.
- **Wording.** The call sheet now says to use the camera to start recording and use it again to stop. The tooltip says the same and shows whether it is rolling.

The rolling flag (`Rolling0576`) lives on the camcorder itself. Old camcorders start stopped.

## Quoting the reader

The journal plan (docs/JOURNAL_DESIGN.md) now records the owner's yes and chooses three moments. In each, the House quotes the reader's own unaltered words, from pages begun in the House, and only to that reader:

- **In their House of Leaves:** one line written in the place a chapter tells of.
- **The co-author, once:** it copies one of the reader's lines onto a blank page, word for word.
- **At an ending:** the last page quotes the first line they wrote in the House.

The House never quotes:
- a deleted or torn line;
- a line naming another player;
- anything at all for a reader who opted out of the House's read.

This is step 8 of the journal's build order. Nothing of it is built yet.

## Tests

- **`GhostsSetTests.theCameraRollsOnOneClickAndStopsOnTheNext`** checks that:
  - the camera starts stopped;
  - one use rolls it, and letting go keeps it rolling without holding the reader in use;
  - the next use stops it;
  - put away, it films nothing, and taken out it is still rolling;
  - a borrowed rolling camera is not the peer's;
  - carried off the set it stops, and off the set it will not start.
- **The other 23 Ghosts cases** set their cameras rolling the new way. All 24 pass locally.
- **The live Ghosts proof's clients** click the camera once, never holding the key.

## Versions

- **Mod version:** 0.4.176.
- **Protocol:** 98 (the flag rides on the item's own data).
- **Layout:** 51.
- **Witness:** 55 sources, 42 required, two kinds, three endings.
