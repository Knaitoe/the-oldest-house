# Small fixes from the vignette review, and Mr. Harrigan — 0.4.105

On October 18 the owner asked for the smaller batch of fixes from the gameplay and storytelling review of every vignette,
then (before it was verified) for the Floorboards hint to become a hatchet in the room, and for Mr. Harrigan's Phone to
become three visits with a real Mr. Harrigan.
The candidate is 0.4.105 on verified 0.4.104 (fd6a73f). Protocol 76 and layout 45 are unchanged; no saved state changes
shape. 46 sources / 35 resolutions / two kinds / three endings are unchanged.

## Floorboards: the room has its hatchet

The loose board comes up only under an axe. The room supplied none and nothing mentioned one, and every attempt on the
board with anything else was refused *and* added 35 of the room's 100 noise points, so a player digging where the
heartbeat is would be sent back out after three tries with no explanation.

The owner asked for no hint text. Instead the room has what it needs:

- **A hatchet**, lying on a chopping block (a stripped spruce log) in the corner beside the wardrobe by the door. It is an
  ordinary iron axe, held in an invisible frame so it lies flat on the block. Hitting it or right-clicking it picks it up.
- **It stays in the room.** The room is shared, so its one hatchet never leaves: carried by someone inside the room, or
  dropped on its floor, it stays where it is; anywhere else (carried out through the door, say) the House takes it back and
  lays it on the block again. The block is reserved from the dressing and polish passes.
- **Attempts on the board itself add no noise.** Breaking anything else in the room is still loud. Right-clicking the board
  without an axe still only knocks.

## Account titles

The literary account books were titled with internal ids ("The account: mapping interior", "devils rock", "elk carcasses").
They now use the House's prose place names (`StaircaseProse.place`): "The account: the crawlspace house", "the diary room",
and so on. A written book title holds 32 characters, so the few names that would not fit are shortened ("the nursery", "the
workshop", "the fan room", "the crawlspace", "the ridge", "the cabin", "the copied home", "the cottage", "the lake house").
Three Fingers, Lake House and Borrasca had no prose names; they gain "the cottage on the mill road", "the house under the
lake" and "Drisking", which also reach any staircase prose that names them. The House's experience page no longer names a
room the reader turned back from by its id.

## Elk Fan: the account and the Witness line agree

The Witness line said the light was repaired and that "on later returns the shape became tape, then a tooth". What happens
is the other way round: the reader stands under the blades until they are thrown out with a tooth in their hand, and the
room is taped off when they come back. The line now reads "I fixed the light and stood under the turning blades until they
threw me out. A tooth was in my hand. When I went back, tape held the shape of the deer." The account no longer names the
mechanic ("Five seconds was longer than I thought" becomes "I stayed up there longer than I meant to").

## Masque: the chime costs something

The steward's paper says to stand still at the chime, but moving only made a red figure speak.

- A second after the first stroke (20 ticks into the chime), a reader who is still moving is put back a room, at the near
  door of the room before the one they were in, facing the clock. The red figure there says "The clock is speaking.", and the
  action bar says "You moved while the clock spoke. You are a room further back."
- The rooms past it count again only once they are crossed again (stopping the clock still needs all seven).
- It happens at most once a chime, and not at all once the clock has stopped.
- The party music now starts in every world: it was checked on one tick that the five-tick scene clock could miss forever.

## Hide-and-Clap: the stakes before the blindfold

Failing the game kills the player and drops everything they carry, even with keepInventory on; the code does this on
purpose, so the drops stay ordinary recoverable items. That is unchanged. What changes is that a reader is told before the
blindfold goes on: entering the room now adds a chat line, "If the minute runs out you die, and drop everything you carry,
even with keepInventory on." The note stays in the child's voice.

## Mr. Harrigan: three visits, and a real Mr. Harrigan

The owner asked for the funeral to be its own, third visit instead of a door off the study on the second, and for Mr.
Harrigan to be a rendered person with alive and dead states rather than an armor stand a player could take apart.

- **The first visit** is unchanged: he sits in his chair, the reader reads him the book on the lectern, and the scratch
  ticket gives twelve emeralds and the spare phone.
- **The second visit** is his study alone. He is dead in his chair; his phone lies on the table beside him. The doorway in
  the far wall is sealed in planks: there is no funeral yet. Taking his phone is this visit's beat. Carrying it out of the
  study resolves nothing; it is on its way to the funeral. Until his phone is taken, the study stays the study.
- **The third visit** is the funeral, laid out in the same room. His chair is gone; chairs stand in rows facing the far wall,
  where the doorway has been opened; his shelves are draped; the lectern holds a guest book ("In memory", signed by the
  town); flowers stand on his table. Beyond the doorway he lies in his casket and three mourners stand by. Holding his phone
  and right-clicking the casket buries it with him (`buried_phone`, and the reader's own phone reaches his number outside the
  House, as before); leaving the funeral with it keeps it (`kept_phone`). The Witness story, its title and line are unchanged.
- **He is one scene actor** (`LiteraryActor`, roles `HARRIGAN` and `HARRIGAN_MOURNER`), drawn with his existing art and
  model: seated alive, his head turning toward the nearest reader and his chest rising; seated dead, slumped as it fell; laid
  in the casket. Blows do nothing, and there is nothing on him to strip. The same body is moved, never replaced. His
  mourners are scene actors too, each with their line. The old armor stand, its appearance component and its repair tick are
  gone (the mod is unreleased).

## Validation scope

- **Floorboards:** both pry cases now first try the board by hand: it stays, the event is refused and the noise does not
  change; breaking a wall afterwards is still loud. A new case finds the hatchet on its block, picks it up, keeps it while
  its holder is in the room, and has it back on the block once they carry it out.
- **Titles:** the Elk Fan case checks every account title is a prose name within 32 characters, and the fan's own title.
- **Masque (new case):** standing still through a chime keeps the reader's place and rooms; moving puts them back at the near
  door of the room before, forgets the rooms past it, and a second move in the same chime does not put them back again.
- **Harrigan:** the pose case follows one actor through all three visits (alive and seated, unharmed by a blow, with
  nothing to strip, persisting through NBT; dead in the same chair with the funeral sealed; taking his phone as the beat; laid
  in the casket with the study made ready, the guest book and three mourners). The wake case checks the funeral is the third
  visit, that the study stays the study until his phone is taken, and that the mourners persist through a reload. The
  dialogue case keeps the phone through the study and resolves it on leaving the funeral. The layout-append case keeps his
  actor's identity and state.
