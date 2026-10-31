# Mapping the Interior, reworked

> "I'm realizing too that mapping_interior, based on Stephen Graham Jones's mapping the interior needs a near total rework as
> well." The owner sent a chapter-by-chapter summary of the novella, and on the open choices (the ending, the regalia, the old
> choice under the house): "I'll leave all that for you to decide".

The old room was a siding box with a silhouette pacing four stops, a brother on a bed and a pile of belongings at the end of a
dirt tunnel. This rebuild follows the novella's shape: four nights in the same small house, 2:49 in the morning each time.

## What the story gives

- A twelve-year-old (Junior), his mother and his little brother Dino, who has seizures, in a modular house on a skirt at the
  edge of things: packed dirt, a propane tank, an old tetherball pole, the neighbour's dogs.
- The first sighting while sleepwalking: his dead father crossing from the kitchen doorway to the hall that leads back to the
  utility room. The father died when Junior was four; he would have been "the best dancer of us all".
- Junior searches for proof he was there, a bead or a feather. "Ghosts need anchors."
- Deadfooting: walk on feet that have gone to sleep and you might step into some other world.
- Under the house, through the skirt: something wrong down there, a dry plywood door with a pull ring in the dirt.
- The neighbour's dogs, dead under the skirt; the deputy who starts driving the boys to school.
- The father getting more solid while Dino gets worse: a mark on Dino's neck; the father standing over his bed.
- The father drowned once. At the end Junior holds him under in the water: "I was going to drown him again."
- An adult Junior looking back, the old walls rising around him.

## Decisions

- **The ending is the drowning,** at water below the plywood door under the house. No gun, and no deputy: the book's
  pistol stays out of the room.
- **The regalia is shown by sound and outline, not painted.** The design document already warns against transplanting
  Blackfeet-specific material carelessly. He is heard by the bells at his ankles, and his outline gains detail night by night
  (a dancer's silhouette, feathers as shape, no painted beadwork or sacred items). Nobody in the room names a nation.
- **The bead replaces the pile.** The old choice (break his things or take them) becomes: drown him and let the bead go into
  the water with him, or keep the bead and leave him in the water. The photograph keepsake becomes the bead.

## Nights

Every night begins the same way: the reader comes to standing at the front window, the clock on the stove reading 2:49.

1. **The crossing.** He crosses once, from the kitchen doorway down the hall to the utility room, heard by his bells before he
   is seen. Where he walked there is a single bead on the carpet. The floor plan (the source, in the older brother's hand) lies
   on the kitchen table; Dino sleeps; Mom's door is shut.
2. **Mapping.** The graph-paper floor plan is a real map: carried under the house, it shows the reader's own mark sliding out
   past the roofline. Outside, one skirt panel at the back is loose; underneath, the crawl runs on past the walls to the plywood
   door, which will not lift yet. The dogs throw themselves at their fence. Standing still until the feet go numb, then walking
   (deadfooting), shows his footprints and the hatch in the utility room floor that leads down the same way. In the night Dino
   has a seizure; kneeling by him turns him on his side until it passes. He crosses again, more of him.
3. **The dogs.** Their run is empty, the gate hanging. In the pocket halfway along the crawl, what is left of them. Dino is
   further gone. He is nearly solid, and he is standing over Dino's bed.
4. **The water.** He bends over Dino; when the reader comes in, he goes the way he always goes, and the plywood door under the
   house stands open over black water. He is in it, rising. Holding him under is held, not clicked: he thrashes, the reader is
   thrown off, and the reader has to get back on him and keep crouching on him until he stops. Then the bead can go into the water after him, or stay in the
   reader's hand.
5. **After.** On the next night, seeing Dino readies the account: awake at the kitchen table if he was drowned and the bead let
   go; asleep and hard to wake, with bells somewhere in the house, if the bead was kept.

## Writing

The source is "A floor plan", in the older brother's hand, rewritten so it describes the house and the walking and never the
ending. The account looks back from years later and ends on the walls rising. The Witness line, its advancement description,
the co-op line, the miniature note and the House of Leaves fact follow. The Witness kind stays connection; the pool and the
quota do not change.

## Multiplayer

The house is a private scene (each reader their own father, brother and bead); a party that comes in together shares one
night, and the drowning needs one pair of hands, anyone's, while the others watch from the edge.

## Built

`MappingHouse` builds the lot whole (the house, the crawl, the tunnel, the water, the shaft to the pole, the dog run) and is
left alone by every generic dressing pass; `MappingNights` runs the nights; `tools/draw_mapping_bead.py` draws the bead. The
scene's clock is fixed at 2:49 (a new scene-clock mode). Details in docs/OWNER_NOTES_0_4_201.md.
