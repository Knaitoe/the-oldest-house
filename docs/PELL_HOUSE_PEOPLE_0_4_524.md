# The Pell house 4: the people (0.4.524)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its
street and lot (`docs/PELL_HOUSE_STREET_0_4_518.md`), its rooms (`docs/PELL_HOUSE_THINGS_0_4_522.md`) and its line
(`docs/PELL_HOUSE_LINE_0_4_523.md`). This is its fourth slice: the people, from the proposal's "The people", standing where its first
visit puts them. Nothing happens yet, and the House still does not deal the house.

## Who is there

As the proposal's "1. The placement" has it: "Gail drops them off. June shows them round, and Callum watches from the end of the
hall."

- **June Pell**, at the foot of her front step, beside the mat and just short of the line, looking down the path. A cardigan with
  its sleeves pushed up, her reading glasses hanging on their chain on her front, and paint at the quick of two nails of her right
  hand.
- **Gail Mercer**, the caseworker, on the path, turned to June: her folder held flat to her chest, her lanyard and its card.
- **Tilly**, beside Gail on the path, turned to the house: her sunhat on, her backpack on her back with a keyring on its pocket, and
  her long cane in her right hand, its tip on the path ahead of her.
- **Callum**, at the far end of the hall, watching the front door: barefoot, in a jumper and trousers that were somebody else's
  first, the trousers rolled at the ankle, his hair wet and the jumper wet at the shoulders where it drips.

June, Gail and Tilly turn their heads to a reader near them, a little each moment, and not their bodies. Callum watches a reader from
further off, anywhere in the house or on the lot, and turns his whole body to keep them in sight.

## A hand on them

- **June, Gail and Tilly** each say the next of their lines, as a caption with their name, never in chat. June is kind and specific,
  and slightly wrong ("Inside the line after dark, love. It's for the dog."). Gail has always just been talking to June. Tilly asks
  what she cannot see.
- **Callum** does not speak. A hand on him gets what he does instead, in grey: he looks at the reader's hand, steps back as far as
  they stepped forward, holds out his hand a moment late.

None of their lines names the reader as anything, or names a colour or an age.

## How they are made

- `PellPeople` holds the four, on stranger phases 195 to 198 (`LiteraryActor`). Each is made where the first visit puts them
  (`PellPeople.FIRST_VISIT`) when a reader arrives, and on each reader's tick if one is missing. One already there (after a reload)
  is taken up again, never made twice, and nobody is made where the ground's entities have not loaded.
- Their skins are painted by the shared painter (`tools/paint_character_skins.py`: `literary_pell_june`, `_tilly`, `_callum`,
  `_gail`), with new marks of their own: June's glasses on their chain, her pushed-up sleeves and her two painted nails; Tilly's
  sunhat, backpack, straps and keyring; Callum's stretched neck, wet shoulders and hair, rolled trousers and bare feet; Gail's lanyard
  and card. They are in the character catalog, so the cast proof draws them.
- They look about on a tick of their own, every game tick (`PellPeople.serverTick`), not the scene's quarter-second reader tick,
  which would turn their heads in steps. Their bodies stay where they are turned (`LiteraryActor.holdsBody`): a still mob's body
  is otherwise brought round to its head after half a second, on the client as well as the server, and June would face a reader
  she only glanced at.
- Tilly's cane and Gail's folder are items (`pell_cane`, `pell_folder`), drawn by `tools/draw_pell_people.py` at sixteen pixels to
  the block, with `--check` (CI runs it). They are held in the hand, and the cast model holds them: Tilly's right arm out a little
  with the cane down and ahead of her; Gail's arms across her chest over the folder. Nobody is given one.
- June and Gail are full size. Tilly and Callum are children, Callum the smaller of the two.

## Decided here

The proposal says who they are, but not exactly where each stands. These are mine, and any of them can be moved:

- **June meets them at the foot of the step**, beside the mat, on the path just short of the line. The line's front run is the
  step's row; she never stands on it. Callum on the line is a later beat of its own.
- **Gail and Tilly are on the path**, between the curb and the step, as if just out of the car. A reader coming up the street comes
  up behind them.
- **Callum is at the hall's west end**, past the photographs, where he can see the front door down the length of the hall.
- **Tilly's cane is in her right hand.** Her sunhat stays on.
- **Callum's hand lines** are what he does, not what he says, in the grey of the house's other cues.

## The case

`PellHouseTests.thePeopleStandWhereTheFirstVisitPutsThem` checks:

- the four are at the house when a reader arrives, each where the first visit puts them and turned the way it says, in their own
  skins and sizes;
- Tilly holds her cane and Gail her folder, and June and Callum hold nothing;
- June stands short of the line, not on it;
- asked again, nobody is made twice, even when the house has forgotten them (as after a reload); one taken away is made again, and
  only that one;
- a hand on June, Gail and Tilly gets their first lines, as captions over them with their names and not in chat; a second hand
  while a line is up does not cut it short, and the next hand after it gets the next line; a hand on Callum gets what he does,
  and no caption;
- a reader near June turns her head and not her body; a reader at the step, across the house from Callum, turns his body and head
  to them.

## Not yet

The arrival itself (Gail leaving, June showing them round), the rules given as they walk, dinner, the night, Callum copying the
reader and standing on the line, Tilly's sight and cane taps, a reader playing Tilly, the neighbours, June's night look, and
everything after.

## Numbers

No layout change. No Witness change. New: four skins, two items and their two textures and models, one lang line each.
