# The Resonant Pipes (0.4.360)

This is item 2 of the owner's "New Puzzle and Hazard Room Designs":

> Certain pipes in the walls carry a faint continuous tone. Blocking or opening sections of pipe changes which tone is heard in each
> room. The early room teaches that the sound is continuous and travels. The deep version requires the reader to create a specific
> combination of tones that unlocks a sympathetic latch.

The owner's notes ask for the following:
- three pitches;
- valves and removable short sections that change which pitches reach which room;
- a latch of tuned reeds that lifts a catch only when the right combination reaches it;
- dust shivering on the reeds when the combination is right.

The early room is one long service corridor:
- three runs of pipe;
- valves at each end of the runs;
- two removable sections;
- a latch that wants the middle pitch alone.

The deep version is three rooms sharing one network. All three pitches must arrive together. Opening one path tends to spoil another.

The release adds a new puzzle family, RESONANCE, with two rooms:
- the resonant pipe (`RESONANT_PIPE`, slot 215, early);
- the resonant pipes (`RESONANT_PIPES`, slot 216, deep). It comes from the strange depths, once the resonant pipe is solved.

The layout version is 115 (new builds only) and the protocol is 154 (one new block). The rooms follow `PuzzleRooms`' pacing:
- they are offered only after a story, and rarely;
- nothing counts down, hurts or is used up;
- every fitting can be turned back;
- solved stays solved, for everyone;
- no Witness credit.

There is no Witness or source-kind change: 80 / 60 / two kinds / three endings. Code: `ResonantPipes`, `ResonanceBlock`.

## How the sound works

- **The drums.** Three iron drums sit on brackets over the entry. Each has one, two or three painted bands for its note: low, middle or
  high. Each hums its note into its own run of pipe. A hand on a drum names the note.
- **Pipe.** The brass pipe the drums feed runs overhead, the length of the room. A run carrying a note trembles, and dust lifts off it.
  A hand on it names what runs there ("The pipe trembles under your hand. A low note and a high note run along it together."), so
  the notes can be traced from drum to reeds. A silent run is cold and still.
- **Both ways.** A note travels a pipe in both directions. A note that reaches a joint goes back up every other open length from it,
  so a run cut from its own drum can still carry other notes, coming back from where the runs meet.
- **Valves.** A red wheel on a fatter body, with its handle bar along the run when open and across it when shut. Turning it opens or
  shuts the run there.
- **Sections.** A short length of pipe between two flanges. Lifted out, it hangs on a bracket below the run and breaks the run.
  Fitted, it carries the note.
- **Cross pipes.** In the deep rooms, a valve on a pipe that joins two runs. Left open, it lets one run's note into the other.
- **The reeds.** A box of brass reeds behind a grille, beside the far door. Each box listens for one note:
  - nothing reaching it: the reeds are still;
  - its own note alone: the reeds sing one clear note and the dust lifts off them;
  - anything else, a wrong note or its own with others: the reeds buzz and rattle.
- **The latch.** An iron catch across the far door, wired to the reeds. While it is down, the far door says it won't give. When every
  reed box in the room sings, the catch lifts with a click and the room is solved: "The reeds sing together, and the catch on the door
  lifts." The catch stays up after that, whatever anyone turns later.

**How the room works it out.** Each note spreads from its drum along every open length of pipe, as a search across the network's
joints. A length is open when it has no fitting, when its valve is open, or when its section is fitted.

**While a reader is in the room.** Every half second the room recomputes the network:
- it sets each pipe's trembling;
- it sets each reed box's state.

**Every two seconds.** The room plays these sounds:
- each note reaching a reed box, at the box;
- a buzz where a box buzzes;
- a soft hum at each drum.

The notes are two seconds long and are laid end to end, so they sound continuous. Ash-white dust lifts off singing reeds.

## The resonant pipe (early)

A long stone-brick service corridor with spruce boards, about three wide and twenty long. The three drums hang over the entry:
- low on the west;
- middle in the centre;
- high on the east.

Their runs go overhead the length of the corridor:
- each has a valve by the drums and another by the far end;
- the middle and high runs each have a short section halfway;
- at the far end the three runs meet in a manifold;
- from the manifold one pipe drops to the reed box by the far door.

The reed box wants the middle note alone.

**The start.**
- Every valve is open.
- The high run's section is fitted.
- The middle run's section is out on its bracket.

So the low and high notes reach the reeds together and they buzz. The middle note runs up to the gap and no further. Past the gap the
middle run still trembles, but with the low and high notes coming back from the manifold, as a hand on it tells.

**Solving.** The reader needs to do three things:
- fit the middle section;
- shut the low run, at either valve;
- shut the high run, at either valve or by lifting its section out.

There are several ways. Built again once solved, the room is left with the low and high valves shut by the drums and both sections
fitted.

## The resonant pipes (deep)

Three stone-brick rooms in a row, A, B and C, with a doorway and a three-pane window in each dividing wall. Through the windows a
reader can see the next room's pipes and reeds.

**The runs.** The three drums hang over the entry in A. Their runs go along all three rooms:
- the low run on the west;
- the middle run in the centre;
- the high run on the east.

**Cross pipes.**
- In A, one joins the middle and high runs. It starts open.
- In B, one passes over the ceiling from the high run to the low. It starts open.
- In B, another joins the high and middle runs. It starts shut.

**The high run's loop.** The high run is bricked up at the wall into C. It reaches C only by a loop that climbs through the ceiling of
B, crosses over the wall and comes down into C. A valve in C opens it, and it starts shut.

**The reeds.** Three reed boxes stand by the far door in C: low on the low run, middle on the middle run, high at the foot of the loop.
Each wants its own note alone.

**The start.** The two open cross pipes mix all three notes into the low and middle runs, so their reeds buzz. Nothing reaches the high
reeds, which are still.

**Solving.** Only one setting lifts the catch:
- shut the first room's cross pipe;
- shut the cross pipe over the second room;
- leave the other cross pipe in the second room shut;
- open the loop in the last room.

Opening the loop before clearing the cross pipes only carries the low note to the high reeds with their own. Opening the second
room's other cross pipe spoils the middle reeds. Built again once solved, the rooms are left in that one working setting.

## Assets

- **One new block**, `resonance_fitting` ("Resonant Pipework"). It has a horizontal facing, seven kinds (drum, pipe, riser, valve,
  section, reed, latch) and a stage from 0 to 2. Only the drum collides.
- **Art.** The textures, models and block states are drawn by `tools/draw_resonance_fittings.py`, in vanilla's manner: 16×16 from
  small palettes with per-pixel noise. There are ten textures:
  - three drum faces;
  - dull and trembling brass;
  - iron;
  - the valve wheel;
  - three reed boxes: still, singing with lifted dust, and buzzing with blurred reeds.
- **Sound.** Five subtitled sounds are synthesized by `tools/generate_resonance_audio.py` from Movie Night's building blocks. No
  recordings are used. The three notes:
  - low, 110 Hz: "A low note hums in the pipes";
  - middle, 165 Hz: "A middle note hums in the pipes";
  - high, 247 Hz: "A high note hums in the pipes".

  The other two are the reeds' buzz ("Reeds buzz") and the catch ("A catch lifts").
- **Checks.** CI and the quick gate check both tools with `--check`.

## Tests

ResonanceTests (exploration namespace, batches `resonant_pipe` and `resonant_pipes`), with a real reader's hands:
- **`theMiddleNoteAloneLiftsTheCatch`:**
  - the start buzzes; the middle note stops at its gap, and past it the middle run carries only the low and high notes from the
    manifold;
  - fitting the section brings all three notes, and shutting the low valve quiets that run;
  - lifting the high section out leaves the middle note alone, and the catch lifts;
  - a reopened valve makes the reeds buzz again, but the room stays solved and the catch stays up;
  - built again, it is solved and the reeds sing;
  - nothing is earned.
- **`eachNoteAloneAtItsOwnReedLiftsTheCatch`:**
  - the high note climbs the loop and stops at its shut valve;
  - each wrong setting tried on the way buzzes and lifts nothing;
  - the one working setting lifts the catch;
  - built again, it is solved;
  - nothing is earned.
