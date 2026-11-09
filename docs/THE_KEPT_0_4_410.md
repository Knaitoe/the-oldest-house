# The Kept (0.4.410)

This is the next series' Ellison proposal ([DESIGN_PROPOSALS_NEXT.md](DESIGN_PROPOSALS_NEXT.md), section 5), built as "The Kept". The owner, on the next series: "Continue with textures and then vignettes ... Don't wait for my approval, just continue."

**Which shape.** It follows the short-story shape in section 5, not the computer-game adaptation the proposal mentions as an updated direction. The game's five playable characters and their circuits would recreate a specific existing game, so they are not built. The short-story shape uses only the source's outline, with original people and original text. This is flagged for the owner.

Its open decisions are taken as the proposal recommends:

- **The punishment:** (c), a persistent blank face, with chat left alone.
- **Skins:** the four captives wear painted placeholder skins, not the readers' own.

The source is treated as protected, so only its shape is used:

- no names, no monologue and no text from it;
- the title phrase appears nowhere in the game;
- every line here is original;
- in the game the place is called "the machine".

## The place

The place is an interior slot, 45 blocks across and 98 deep, inside a machine. One walk runs north from the reader's door.

- **The corridor of cabinets.** Cabinets stand on both sides, humming, with reels and rows of small lamps. Cathode screens are set into them every few blocks. Cable runs along the floor.
  - **The printer** stands by the door on its stand, with its paper run out on the floor (the source).
  - **The tray** it prints into stands across from it (where the account waits).
  - **The four captives** sit on the floor against the cabinets.
- **The cave of ice.** The machine has made it, badly, out of its own casing: frost underfoot, rime on the walls, cabinets sealed in it, icicles that do not drip.
- **The wire.** A room of wire grown into a thicket too dense to push through. One crawl runs under it, a block high, turning twice.
- **The hall.** Flocked paper on the walls, the pattern not quite right, and a chequered floor.
  - **Three long tables** are laid for nobody, with a hundred chairs at them. Each chair turns to face whoever is nearest it.
  - **At the head of the hall** is a crate of tins.
  - **In the side walls** are four cells behind iron doors, with glare from wall to wall inside them.
  - **The way on** is a door in the north wall.

## The four

Four captives, kept longer than a lifetime, starving and bickering, wait in the machine:

- **Silas:** tired, and long past believing anything;
- **Petra:** sharp, and protective of Tamsin;
- **Wendell:** who talks about food;
- **Tamsin:** who asks the reader about outside.

The machine has promised them food at the end of the walk.

- **Getting up.** They sit until a reader comes near, then get up and walk behind whoever has gone furthest, single file, bickering in pairs.
- **The wire.** At the wire they go round by some way of their own. When the reader comes out of the crawl, they are already at the mouth of the hall.
- **The crate.** They go up the aisle to the crate. A reader who tries it finds tins, and nothing to open them with. The four understand it one by one.
- **The asking.** Then, one at a time, each goes to the door of their cell and asks the reader to let them go: shut their eyes, and open it.
- **Shutting the eyes.** A reader shuts their eyes by crouching by the door. The view goes dark, and after three seconds their hand finds the door.
  - A door clicked with eyes open does nothing.
  - The first time, the reader is told how to shut their eyes.
- **The release.** The door opens onto the glare. The captive walks into it, and is gone, and the door shuts.

## What the screens read out

As the reader walks, the machine reads them their own record, in its own hand across the middle of the view. It says only what the House has really recorded:

- **In the corridor:** how many times the reader has died, as the world counted it.
- **In the cave:** the names of the companions the House saw them lose.
- **At the wire:** the stories they walked out of before the end, by the House's own names for them. "The door behind you is open."
- **In the hall:** a hundred chairs, and how many people there are.
- **At the crate:** that it promised them food, and there is food.
- **On a later visit:** that the reader came back, and how many times.

Screens near a reader wake; the rest are dark.

## The cold

The cave is cold, and the frost overlay comes with it. It is held short of freezing, so it never hurts.

## The punishment, and the account

A reader who has let all four go, in one visit or across several, is noticed:

1. Every screen in the machine wakes at once.
2. It tells them they let the four go, and asks what they would say to it now, if they could.
3. **The punishment.** It takes their mouth. From then on every viewer, and every mirror, draws that reader's face smooth below the eyes. It is permanent, saved with the world, and survives death, doors and logins. Their chat is untouched. Nothing in the House gives it back yet.
4. The account then waits in the tray by the door. There is one source (`the_kept`, release).

The account says:

- the corridor and the four;
- what the screens told that reader;
- the cave, the wire and the hall of chairs;
- the tins;
- the doors opened with eyes shut;
- the screens waking, and the face;
- "I am writing this down because I still can."

## Multiplayer

- **Shared:** the four, the chairs, the screens and the cave's cold.
- **Each reader's own:** each release counts for every reader whose eyes were shut by that door when it opened, and only for them.
  - The punishment, the account and what the screens read out are each reader's own.
- **Putting them back.** When the machine is empty, it puts the four back. They remember a reader who let them go before.
- **Leaving.** A reader who refuses can always walk out, with no credit, and come back.

## Made

- **Code:**
  - `KeptSite` builds the machine whole: corridor, cave, wire and crawl, hall, chairs, crate and cells. It also turns the chairs and wakes the screens.
  - `Kept` runs the four, the walk, the screens' record, the cold, the crate, the asking, the eyes and the release, and the noticing.
  - `KeptTexts` holds the printout, the machine's lines, the four's lines and the account.
  - `FaceLoss` keeps the punishment. `FaceLossPayload` and `FaceLossClient` draw it for every viewer: a flat patch of the reader's own skin, taken from the back of the hand, over the lower face.
- **`KeptRegistry` registers:**
  - eight materials (casing, grating, ceiling, rime, frost, flock, checker, glare);
  - `KeptBlock` (twelve kinds: cabinet, screen, cable, tube, icicle, printer, tray, wire, table, chair, crate, chandelier);
  - six sounds: hum, wake, tins, weep, glare, notice.
- **The four** are painted placeholder skins on stranger phases 135 to 138, with sitting, hunched, hand-on-door, weeping and reaching poses.
- **Art and sound:** `tools/draw_kept_art.py` draws 32 textures and 35 models and states. `tools/generate_kept_audio.py` synthesizes the sounds.
- New builds only.

## Tests

`KeptTests` has two cases in the literary namespace:

- **the walk:**
  - the machine built whole: the printer, tray and crate, a hundred chairs, four shut cells with glare behind them, the crawl under the wire, the doorways and the screens;
  - the four get up when a reader comes near;
  - the screens read out the reader's deaths as the world counted them, then what they lost and what they walked out of;
  - the cave is cold short of freezing;
  - the four are at the mouth of the hall first, and the chairs turn to the reader;
  - nothing is ready, and the crate has nothing to open the tins with;
- **the doors:**
  - a door opened with eyes open does nothing, and the reader is told how to shut them;
  - with eyes shut the door opens onto the glare, and only the reader whose eyes were shut is credited;
  - the captive is gone and the next one asks;
  - the reader who lets all four go is noticed, loses their mouth for good, and reads the account; the other reader does not;
  - on the next visit the four are back, sat in the corridor, and remember the reader.

## What stays

- **Witness:** 85 sources / 64 resolutions / two kinds / three endings (one new release source).
- **Layout:** 134 (slot 236).
- **Protocol:** 167.
