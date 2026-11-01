# Reading the House through what it does: the owner's proposals of 1 November

The owner, 1 November, after the 0.4.225 review fixes:

> "Consolidate protection for authored rooms. A single room property should exclude them from generic furnishing and repair
> passes. Maintaining several separate exclusion lists makes each new room another opportunity for accidental alterations.
> I'd especially like more ways to read the House through its physical behavior, plus a couple of smaller stories with clear
> actions and endings."

Then six proposals (drafts and pressure; Vacant Possession; sound through connected doors; The Last Swimmer; rooms that keep the
reader's traces; a recurring, familiar room), and: "For the lighter puzzles specifically, I'd keep expanding familiar mechanisms
into unfamiliar spaces: a simple draft becomes ventilation across several rooms; a door wedge becomes a way to hold an unstable
route open. Learning something useful should itself feel rewarding." And: "I'd like to play the last swimmer against Armand
Hammer's Falling Out the Sky lyrics, contextually."

This records how each is built and in what order. Each is its own release, so each can be played and corrected before the next.

## Build order

| Release | What | Kind |
|---|---|---|
| 0.4.226 | Authored rooms: one property | Structure (`docs/AUTHORED_ROOMS_0_4_226.md`) |
| 0.4.227 | The Last Swimmer | Story, one visit, eligible Witness source |
| 0.4.228 | Vacant Possession | Story, one visit, eligible Witness source |
| 0.4.229 | Sound through connected doors | House behaviour, everywhere |
| 0.4.230 | Drafts and pressure | House behaviour, two puzzle rooms |
| 0.4.231 | Traces, and the door wedge | House behaviour, everywhere |
| 0.4.232 | The familiar room | Recurring room |

The two stories come first because they stand alone. Sound comes before drafts and the familiar room because both lean on it: the
familiar room's clock and tap are meant to be recognised through another entrance, and a draft is something you also hear.

## 1. The authored-room property (0.4.226)

Done; see `docs/AUTHORED_ROOMS_0_4_226.md`. Every room says how it is made, `Made.WHOLE` or `Made.DRESSED`, and every generic pass
asks that and nothing else. A room that says nothing is built whole, so the new rooms below are protected without being listed
anywhere.

## 2. The Last Swimmer (0.4.227)

A small municipal pool at closing. Built whole: the pool hall (six lanes, tiled deck, lane ropes, a pace clock with a sweep hand on
the end wall, starting blocks, a skylight whose square of sky lies on the water), the attendant's office with its window onto the
hall, the changing-room corridor, and the exit.

- **The attendant** asks for help pulling the cover across. The cover is on a reel along one side; the reader works the reel's
  crank with the attendant on the other end, and the cover runs out across the water a lane at a time.
- **The swimmer** keeps swimming in whatever is left uncovered. Mostly heard: each turn against the tiles, the splashing moving
  down the lane. Seen only in glimpses, a pale shape under the surface for a moment. As the opening narrows to one lane, the
  lengths keep taking exactly the same time.
- **Once the cover closes**, the splashing stops. A pause. Then wet footsteps cross the cover toward the reader, each one pressing
  the cover up from underneath.
- **The attendant quietly holds the exit open.** Walking out is the ending. Nothing chases and nothing kills; lingering only brings
  the footsteps to the edge nearest the reader.
- Private: each reader gets their own pool (scene copies). Witness: one source, "understanding".

**Against "Falling Out the Sky".** The scene is written against the song (Armand Hammer and The Alchemist, *Haram*, 2021, with Earl
Sweatshirt): an unhurried, loping summer track set against a pool closing at night. The swimmer's patient, unchanging lengths keep
the song's kind of time while the space around them closes. The skylight's square of sky on the water puts the sky under the
swimmer, so whatever comes up is coming out of it. The attendant's office radio is where the song belongs.

What the mod does **not** do: copy the lyrics into the game, or ship the recording. Both belong to the artists, and the House's own
rule is no copyrighted text in the repository. Instead:
- the radio has its own sound event, `the_oldest_house:swimmer.radio`, which ships silent. Anyone may fill it from their own copy
  of the track in a local resource pack (`assets/the_oldest_house/sounds/swimmer/radio.ogg`); nothing is committed;
- the swimmer's length (`LENGTH_TICKS`) is one constant, so the lengths can be set to the track's tempo;
- every line in the scene is original and plays off the title's image (the sky, falling, a summer evening), never the song's words.

## 3. Vacant Possession (0.4.228)

A small apartment on a landing. An older tenant asks for help emptying it.

- Each piece (dresser, rug, bed, wardrobe, table, armchair, boxes, lamp) is lifted with a right-click and carried down to the van
  as a carried item; set down in the van, it stays there.
- What each piece was hiding is there when it goes: height marks behind the dresser, an unfaded rectangle under the rug, scratches
  round where the bed's legs stood, a lighter square on the wallpaper behind the wardrobe.
- The tenant's words come fainter as the room empties: their voice quieter, their lines shorter, then broken off.
- At the end one chair is left, and they're sitting in it. They stand when the reader comes near: "That's the last of it."
- Carry it out, and the flat is completely empty: the reader goes down the stairs alone. Or put it back, and the tenant sits down
  again: the reader goes down with the light still on in the window above. Either finishes the story, with its own account line.
- Not a chase. Private. Witness: one source, both choices counting once.

## 4. Sound through connected doors (0.4.229)

Sound follows the House's actual connections.

- **Knocking.** Striking a closed door inside the House knocks on it (the House never lets doors break, so the strike was wasted
  before). The knock is heard at that door and, faintly, at the door it connects to: wherever that door leads for the knocker,
  however impossible the place.
- **Doors shutting** carry the same way, as a dull thud on the far side.
- **Room sounds.** A ticking clock, a running tap or a radio near a door is heard faintly by someone standing at the door that
  leads to it, through the wood.
- Two readers can find each other by knocking, shutting doors and listening; a solo reader can recognise a room by its clock
  through another entrance, and show that two distant passages share a wall.

## 5. Drafts and pressure (0.4.230)

Air moves from one opening to another, and you can see it: curtains pulling toward a doorway, dust moving under a closed door, a
door settling shut when another passage opens.

- **Early room:** two adjoining spaces, a vent with a grille that opens and shuts, and a door. Following the draft, and turning it
  with the door and the vent, opens the way on. Teaches what the House's air does.
- **Deep room:** several adjoining spaces carry one draft through them; one wall has a concealed door, found only by watching what
  the air does at it (dust lifting off a seam, a curtain pulled flat toward plain plaster).
- Empty rooms then have something to investigate without a note or an encounter: ordinary halls carry faint drafts toward their
  open doors.

## 6. Traces, and the door wedge (0.4.231)

- Dust keeps footprints; a chair stays where it was dragged; a wedge holds a door where it was left. Readers can know a room by what
  they did in it.
- **The wedge** holds an unstable route open: a wedged door is not shut behind the reader, and the hall beyond it stays what it was.
- Rarely, and only after many dependable returns: a second set of prints beside the reader's own, or a second chair drawn up to
  their table. Most traces stay exactly as left, so the exceptions carry weight.

## 7. The familiar room (0.4.232)

A modest kitchen that comes back through different approaches.

- Its contents persist: supplies left, chairs arranged. Its ordinary sounds (the fridge, the clock, the tap) are its own, and can be
  heard through other doors (0.4.229).
- Over several visits, its window shows passages the reader has already walked, from above.
- Shared: groups who find it meet there.
