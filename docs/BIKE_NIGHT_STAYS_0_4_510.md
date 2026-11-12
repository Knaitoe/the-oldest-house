# Bike night 27: caught in the Hollow: dawn, and what stays (0.4.510)

The proposal (`docs/BIKE_NIGHT_PROPOSAL.md`, "Failure: caught in the Hollow"): "**The moment:** a specter catches the
reader. The screen goes to mist. **Afterwards:** they come round at dawn outside the graveyard's front gate, alive, the
bike gone. **What stays:** the vignette is never dealt to them again; in that town, in every copy any reader later
visits, there is a new small headstone at the end of the row with the old date. It carries this reader's name and the
date of their night, and their bike leans against it, rusting."

The mist, coming round outside the gate, the bike gone and never being dealt the night again were built (0.4.499,
0.4.508). It was still night outside the gate, and nothing stayed.

## Dawn

For somebody the Hollow has had, it is dawn from then on, wherever in the place they are. It is their own sky: the
server tells their client (`BikeNightMistPayload.dawn`, for as long as they are there), and their sky goes from the
night's hour to first light over five seconds, most of it behind the mist that is over them when they are taken, and
stays there. Everybody else in the town is still in the night.

It is a slow change of light. Nothing flashes.

What they are told when they come round says so: "You come round on the pavement outside the cemetery gate. It is
getting light. The bike is gone."

It is dawn for them on any later night too, if they are ever there again (they are not dealt it; they may follow
somebody in).

## The stone

Who the Hollow has had is kept with the world, in the order it had them: their name as it was that night, and the make
they were on (`BikeNightStones`). Each has a place at the end of the row of small stones: east of the row first, at
the row's own spacing, then west of it. There are thirty-two places.

In every night after, in every copy of the town, a new small stone stands in each of those places. It is one of the
row's in shape, in marble not yet gone grey, with nothing grown on it and no rain run down it, and where the row's
have their year it has the year of the night itself. A hand on it is told what is cut on it: the name, in capitals as
the row's are, and "OCTOBER 31, 1984."

Their bike leans against the back of it, rusted: the make they were on, the same pieces with the paint gone to rust
and its colour still under it, and what was chrome rust. Somebody caught on foot has a stone and no bike.

Both are put there as everything of the Hollow's is: the first moment nobody could be looking at the place. So the
stone is there the same night, for whoever comes back up the hill. A stone is a block and stays. A bike is not saved,
and is stood there again each night.

Somebody caught again has the one stone. Nothing is said to the reader whose name it is that is not said to anybody.

## The year

Nothing in the town says what year the night is in. The proposal says "a small town in the 1980s", the row is 1957,
and the last of the other stones is 1979. The new stones need a year, on their faces and in what is cut on them, and
they have **1984**. That is a guess, and the owner's to change (`BikeNightTexts.NIGHT_YEAR`, and `NEW_YEAR` in
`tools/draw_calder_graves_art.py`, which draws it on the stone).

## How it is drawn

- `CalderGraveBlock.NEW_STONE`: a seventh small stone, drawn by `tools/draw_calder_graves_art.py`.
- Each of the six bikes rusted: a rusted paint card for each and a rust for what was chrome, on the same models
  (`tools/draw_bike_night_art.py`); a pack bike of `PackBike.RUSTED`'s looks, propped fourteen degrees over
  (`PackBike.prop`).
- The proof's page of what follows has a new stone beside one of the row's with the Lamp against it, from in front,
  and the Hopper's from behind. Every part of every rusted bike is required to have its model.

## The cases

One new case, 98 in the Bike night namespace, and the case in which the Hollow has a reader goes further.

- **New:** the thirty-two places are open grass in a new town, east of the row and then west. A reader on the Lamp is
  caught, with another on the hill looking at the first place: it is kept with the world, and for three seconds
  nothing is put there. They turn away: the stone stands, facing as the row's do, and the Lamp leans behind it,
  rusted, where it should be, nobody's to take. A hand on the stone is told the name and the date; a hand on one of
  the row's is told what it always was. The night over, the stone stands and the bike has gone with the night; in the
  next it is back. The same reader caught again has no second stone. Another, caught on foot, has a stone and no bike.
  A new stone that is nobody's is taken away.
- **Caught:** it is dawn for the one who was caught, told with the mist that was over them and again every half
  second, and for nobody else.

## Not built, not seen, and left with the owner

- **The year** (above).
- **The same night.** The stone and the rusted bike are there as soon as nobody is looking, the same night. If the
  owner wants them only on later nights, it is one condition.
- **Thirty-two places.** The thirty-third reader the Hollow has has no stone.
- **The name** is the reader's name as the game has it, in capitals. Nothing is done about a long one: a hand is told
  it whole, and the face of the stone only ever shows the line of a name, as the row's do.
- **Dawn** is the sky and the light only. The town's lamps, people and cars are as they are for everybody.
- Nobody has seen the dawn in a client: the cases see that the reader is told, and the proof does not draw skies.
- Still not built: the keepsake (the owner's call). (The bike chained to a school fence for somebody caught in the
  town: 0.4.511, `docs/BIKE_NIGHT_CHAINED_0_4_511.md`.)
