# The Left-Right Game (0.4.205)

The owner's first proposal of 31 October (docs/OWNER_VIGNETTE_PROPOSALS_2026_10_31.md, section 1), built after the Polaroid
Trail. The road past a toll forks and keeps forking, and there is one rule: left, then right, never the same way twice.

Slot 131, `LabyrinthPlace.LEFT_RIGHT`, a multi-visit literary scene outside (two in the morning on the scene clock). Layout 61,
protocol 112. Witness: a new survival source, **60 sources / 45 required / two kinds / three endings**.

## The place

`LeftRightRoad` builds it whole (the polish, review and playtest passes skip it):

- **The toll.** The reader's door is in an overpass abutment. The Bristol Ferry toll stands under a concrete canopy with
  fluorescent tubes (one dead). Its booth sits on the island with windows either side, and on its steel desk are the teletype
  roll (the source), the CB and the convoy log (the account). There is a crate of flares by the door, and the drop-arm gates
  are raised. A maintenance shed beside the toll has the way on.
- **The approach.** Jersey barriers and sodium lamps on both shoulders, a double yellow line, a wall of dark forest.
- **The fork.** A divider runs under a sign gantry, with a sign for each branch in the night's colours. The two branches run
  on either side of a strip of forest into the fog.
- **The salt.** East, behind the trees, is a cracked white flat with unlit lamps and a barricade of crushed sedans three high.

The branches end in fog. Before the trees, the road carries the reader back onto the approach: their yaw is kept, and the
next fork's dressing has already been swapped in. Things beside the road are dressed per night and per fork by
`LeftRightRoad.dress`:

- rusted sedans on the toll night;
- reeds, a downed power pole, the chained gate and the phone box at the Bluejay cut;
- doorless pastel houses with only dormer windows in Jubilation;
- snow, buckled asphalt and bent signs on the sinkhole night.

One fitting block, `left_right_fitting` (24 kinds), and ten materials are drawn by `tools/draw_left_right_art.py`. Seven
subtitled sounds come from `tools/generate_left_right_audio.py`: the CB, the payphone, a lit window's hum, her lunge, a flare,
the wagon idling, and the gate. A client fog (`LeftRightClient`, from `LeftRightFogPayload`) closes the road in from four
blocks to eighteen, in a wet brown dark. On the salt it turns bleached white over two seconds, and on the sinkhole night it is a
grey snow-light.

## The rule

Left, then right, then left: the expected turn flips at every fork. The CB says which side a night starts on (left, right,
left, right for the four nights). Taking the right branch past the line where the road carries the reader on writes the turn
down and brings up the next fork. Taking the wrong branch leaves the reader on the salt once they are past the divider's end.

**Marjorie.** On the salt she stands by the cars nine blocks ahead, in a wet coat with her hair over the top of her face and
her jaw hanging. She is still while anyone holds her in sight (within about 27 degrees of their look). While nobody quite
does, she comes a step at a time. The moment someone turns away (past about 75 degrees), or she reaches arm's length, she
lunges. Backing out across the salt's edge while she is held in sight makes it the road again, at the same fork.

The lunge on the first night is a lesson. Apollo explains it over the CB and the reader is back at the fork. After that, a
lunge ends the night: the reader is put back out into the House, and the night starts again from the toll. She is two block
halves that step a block at a time and turn to face the nearest reader. She leaves condensation where she stood.

**Flares.** From the second night the crate gives three flares a night. A flare struck on the divider at the fork burns for
twelve seconds. While it burns, the wrong branch is only a road. Its end, or the line where the road carries the reader on,
still takes them to the salt.

## The four nights

1. **The toll.** Four forks: left, right, left, right. Then barriers go out across both branches and the night is done.
2. **The Bluejay cut.** Three forks, starting right. At the second fork the left branch is chained shut, and the brass
   conduit key hangs in a glass phone box down the right, which is the wrong way. With a flare on the divider the reader walks
   the right branch, takes the key and walks back. The key opens the gate, and the reader takes the left. The phone rings.
   Answered, Bonnie speaks in three lines, her words backwards in the action bar as they come up in the condensation, while the
   box floods a step at a time.
3. **Jubilation.** Four forks among pastel houses with no front doors and no lower windows, only dormers. The dormers light
   and darken, and someone moves behind the curtains of the dark ones. A lit window within about 20 degrees of the reader's
   look, while they are not looking down, slows them: ten percent a step, down to ninety percent. Pitching down past 35
   degrees to look at the yellow line lets it go. Held at its strongest for five seconds, the curtain parts and the reader is
   put out of the road.
4. **The sinkhole.** Two forks, starting right, in freezing snow among bent signs. Then the road falls away past the gantry
   into a pit of slabs and black ice. Alice's green station wagon idles on the approach, hazards blinking and exhaust in the
   cold. Letting its tailgate down readies the account (`opened_the_tailgate`) and gives Apollo's last flare.

## Voices and the account

There are three hands:

- **Apollo** writes the teletype roll with the rule (the source), speaks over the CB at every fork, and keeps a convoy log
  with one entry a night, read at the booth.
- **Bonnie** speaks on the payphone, backwards.
- **The reader** keeps their own road log in Apollo's way. It holds the turns of each night, the flare and the key, Bonnie's
  words the right way round, how many times the lit windows held them, the salt (how many times, how many backed out, the
  lunges) and the wagon, with Apollo's last line folded in. Read at the booth, it resolves the road (survival).

**Apollo's last flare** is owner-bound. Struck on any floor in the House, it burns red for twelve seconds and can be lit
again after three minutes. It is a light the House does not take back.

## Multiplayer

The road is one and shared by everyone on it, with one sequence. A wrong turn by any of the readers takes all of them to the
salt. One reader can hold her in sight while another backs the party out. When the right way is taken, everyone on the
branches is carried back onto the approach together. Each reader's own record keeps their nights, turns and account. A reader
who arrives while others are on the road walks the night they are on. The tailgate readies the account only for readers on
their own fourth night. Five seconds after the road empties, it is set back for the next night.

## Tests

`LeftRightTests`, seven cases in the literary namespace:

- the teletype, and the first night starting on the left;
- four right turns carrying the reader on until the road is closed;
- a wrong turn to the salt, and backing out with her held;
- the first lunge as a lesson, and the next ending the night;
- the flare, Bonnie, the key and the gate;
- a lit dormer slowing the reader until they look down;
- the tailgate, Apollo's last flare and the log resolving the road.

The Witness cases count 60 / 45.
