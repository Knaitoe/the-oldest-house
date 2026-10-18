# Small fixes from the vignette review, and Mr. Harrigan — 0.4.105

On October 18 the owner asked for the smaller batch of fixes from the gameplay and storytelling review of every vignette,
then (before it was verified) for the Floorboards hint to become a hatchet in the room, for Mr. Harrigan's Phone to
become three visits with a real Mr. Harrigan, and then for the third visit to be a real funeral home and for the phone to
have a screen from which the player can call and text him as its notes say.
The candidate is 0.4.105 on verified 0.4.104 (fd6a73f). Protocol 77 (the phone's screen adds two packets); layout 45 is
unchanged. 46 sources / 35 resolutions / two kinds / three endings are unchanged.

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
- **The third visit** is his funeral, at a funeral home built in the same slot in place of the study (the study's
  things are gone: no chair, no lectern, no shelves). Through the door is a foyer on dark oak boards: a standing sign
  ("PEABODY & SONS / Funeral Home / Visitation for / Mr. Harrigan"), a guest book on a lectern ("In memory", signed by the
  town), flowers on pedestals and benches along the walls. Beyond a panelled partition is the chapel: burgundy carpet, panelled
  and papered walls, a red aisle between five rows of pews, lanterns on chains, and at the far end his casket between
  flowers and candle stands, where he lies. Three mourners stand by, two at the casket and one at the chapel door. Holding
  his phone and right-clicking the casket buries it with him (`buried_phone`, and the reader's own phone reaches his number
  outside the House, as before); leaving the funeral with it keeps it (`kept_phone`). The Witness story, its title and line
  are unchanged. If the visits were ever counted back, the study is built again.
- **He is one scene actor** (`LiteraryActor`, roles `HARRIGAN` and `HARRIGAN_MOURNER`), drawn with his existing art and
  model: seated alive, his head turning toward the nearest reader and his chest rising; seated dead, slumped as it fell; laid
  in the casket. Blows do nothing, and there is nothing on him to strip. The same body is moved, never replaced. His
  mourners are scene actors too, each with their line. The old armor stand, its appearance component and its repair tick are
  gone (the mod is unreleased).

## Mr. Harrigan's phone has a screen

The phone's notes say: text him a creature's name from outside the House, once a day; a hostile species dies within 64
blocks of the caller's Overworld bed at the next dawn; a harmless or named creature brings him in person; players can be
named where the server allows it; nothing goes from inside the House; and his number answers only if his phone was buried
with him. All of that already ran, but only by typing a name in chat. Using the phone (the reader's own, from the scratch
ticket) now opens its screen, and the screen does what the notes say.

- **Home:** the time back home in large figures (the phone keeps the Overworld clock, even in the House), signal bars or
  "No service", and Mr. Harrigan's contact with the last message, a Call button and a Message button.
- **Messages:** the thread with him, kept per reader (the last forty lines): the reader's texts on the right, anything that
  comes back on the left, and quiet notes in the middle ("Delivered", "No answer.", "Call · his voicemail"). A box takes a
  name and Send (or Enter) texts it to him. The reply is what the call already answered: a hostile species gets his
  acknowledgement, a harmless one "Delivered", an unknown name "?", a forbidden one or a player where players are off his
  scrambled "don't".
- **Calls:** the phone rings out (a ringback tone). With his phone buried and the day's name not yet given, the line picks
  up on his voicemail: "This is Harrigan. I don't pick up this phone. If you have a name for me, leave it after the tone."
  then the tone, and a name left there counts as the day's text. Otherwise it rings until it gives up. In the House the
  call fails at once: no service. If the reader kept his phone instead, calling his number makes *his* phone ring in their
  pocket, and texts to him buzz there too: nothing reaches him.
- **Once a day** holds across both: after the day's name, texts get "No answer." and calls ring out.
- The screen replaces typing a name in chat after using the phone (the chat listener is gone), and the notes now say so
  ("Use it to open it. Outside the House: call him, or text him a name." and "Try a hostile species, such as zombie. One a
  day.").
- A typed name is checked against online players first, then the nearest creature with that name, then a species, and
  only then against players who have been on the server (a missing profile cache no longer matters, and a name that could
  be no id no longer throws).
- Six subtitled sounds (`tools/generate_phone_audio.py`): "Phone rings", "Line picks up", "Voicemail tone", "Message sent",
  "Message received", "His phone rings". The server owns the thread and every outcome; the screen only shows what it is sent.

## Validation scope

- **Floorboards:** both pry cases now first try the board by hand: it stays, the event is refused and the noise does not
  change; breaking a wall afterwards is still loud. A new case finds the hatchet on its block, picks it up, keeps it while
  its holder is in the room, and has it back on the block once they carry it out.
- **Titles:** the Elk Fan case checks every account title is a prose name within 32 characters, and the fan's own title.
- **Masque (new case):** standing still through a chime keeps the reader's place and rooms; moving puts them back at the near
  door of the room before, forgets the rooms past it, and a second move in the same chime does not put them back again.
- **Harrigan:** the pose case follows one actor through all three visits (alive and seated, unharmed by a blow, with
  nothing to strip, persisting through NBT; dead in the same chair with the funeral sealed; taking his phone as the beat; laid
  in the casket in the funeral home: chair and lectern gone, the sign, the guest book, the pews, the aisle and three
  mourners). The wake case checks the funeral is the third
  visit, that the study stays the study until his phone is taken, and that the mourners persist through a reload. The
  dialogue case keeps the phone through the study and resolves it on leaving the funeral. The layout-append case keeps his
  actor's identity and state.
- **Phone (new case):** a connected call reaches his voicemail; a texted "zombie" is delivered and acknowledged and marks
  the zombie for the next dawn by the caller's bed; a second name that day gets no answer and changes nothing, and a second
  call rings out; the next day a name left on voicemail counts; with his phone kept, his number rings and buzzes in the
  caller's pocket and nothing reaches him; inside the House nothing is delivered and calls have no service; the screen is
  sent the clock, the line and the whole thread.
- **Phone screen (client proof):** three native views of the screen (home, messages, a voicemail call with its input),
  saved as `native-phone-0..2.png` and required by the build job.
