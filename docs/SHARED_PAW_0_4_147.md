# The monkey's paw, shared — 0.4.147

The owner's October 28 direction for the monkey's paw: everyone hears the story, one reader holds the paw, and the finger clench shows as an animation on the paw in that reader's hand, so the others see it happen. This is the first scene built on the co-op foundation (`docs/COOP_FOUNDATION_0_4_146.md`), and the first drafted for every way of being there.

## One evening for the whole thread

The readers who come into the cottage together are one thread. Everything the cottage does, it does for the whole thread:

- **One family.** The father, the mother, Will, the soldier and the man from the mill belong to the thread. Everyone in it sees the same people and hears what they say. A reader of another thread in the cottage at the same time sees their own family, not this one.
- **One clock.** The soldier's story, the paw thrown on the fire, the news and the knocking run on the thread's clock. It moves once a tick however many of the thread are there, and only while one of them is.
- **Anyone can ask.** Whoever talks to the soldier hurries his story along for the whole thread, and everyone hears the answer. Each reader's own count of what they asked is still theirs.
- **The news comes to whoever is there.** On a later visit the son's chair is empty and the man from the mill comes to the door, for whoever of the thread has come back.

## One holder

- **Whoever pulls the paw from the fire holds it.** Pulling it out still costs half a heart, never more than a reader can bear. Once it is held, nobody else can take it from the fire, and anyone else trying is told whose hand it is in.
- **Only the holder wishes.** The paw does not move in anyone else's hand. The companion it may name is one the House saw the holder lose.
- **What was wished for comes to the holder.** The money, the collar, the food, Will's watch or the coal go to whoever wished, the first time they are there after it came.
- **The ending is the holder's.** The last wish, giving the paw back to the fire, and drawing the bolt once the knocking has begun are the holder's to do. If anyone else goes to the door, the mother stops them: "Not you. He isn't knocking for you."
- **A lost paw** is in the fire again for its holder, as before, and the old one answers no more.
- **A holder who never comes back.** If the holder stays away for three in-game days, the paw goes back in the fire. Whoever of the thread comes next can take it out and hold it, with the fingers it still has; the one the old holder carries is only a dried paw.
- **Beats that wait for the holder.** The first wish and the wish for Will back need the holder. Until they come, the House deals the others something else alone; they can still follow the holder in. The news, the empty chair and the knocking need nobody in particular.

## The finger going down

Each wish curls one finger down onto the palm.

- **The holder** feels it twist and sees the close-up, as before.
- **Everyone who can see the holder's hand** sees the finger curl on the held paw, in four steps over about a second: the finger up, bending at the tip, folding at the knuckle, down. The paw stack carries the moment it happened, and every client draws the step from it, so the animation works in first and third person, for any number of watchers, and for anyone who walks in while it plays.
- **Everyone there** is told in words too: "In <holder>'s hand the paw twists like a live thing. A finger curls down and stays down."

The two in-between frames for each finger are new 16-pixel sprites, drawn from the existing paw sprites in their palette by `tools/draw_paw_clench.py` (`--check` verifies the frames, their models and the paw's overrides).

## An account for every way of being there

The holder who ends it reads the pocketbook and the Bible as before, and their account is unchanged. Everyone else of the thread gets their own account, in their own words, and the Witness credit:

- **Watched** (there for the wish and the ending): they sat by the same fire, watched the holder wish and each wish come home wrong, and never touched it.
- **Came in partway:** they came in when the holder already had the paw and the family were already afraid of it, and saw the rest.
- **Saw only the consequence** (away for the wish, back for the news): they were away when the holder wished and came back in time to see the wish arrive at the door.
- **Missed the ending:** the next time they come into the cottage, they find the fire low, the door bolted and nobody speaking of what knocked.

Each account names the holder by their player name. The first three end on how it ended: the last wish, the door opened onto the empty road, or the paw back on the fire.

## Saved worlds

A reader whose part of the story began before 0.4.147 brings it into their thread the first time they are in the cottage: its story becomes the thread's, they hold the paw if they had taken it, and their private family is replaced by the thread's.

## Tests

The four paw cases are rewritten for the shared cottage, and the speech case keeps two households as two threads:

- **The fire.** Three readers come in; two share a thread and one is moved to a thread of their own. The thread's soldier is seen by its two readers and not the third. Asking him hurries the story for both. One pulls the paw out and holds it; the other cannot take it from the fire, and it does not move in their hand. The first wish waits for its holder. The wish takes a finger on the paw and in the story, the finger curls on the held stack and then stays down, the holder feels it and the friend watches it, and the other thread hears none of it.
- **The news and the accounts.** Four readers come in together; one was away for the wish. On a later visit the news comes to everyone there and the collar to the holder only. A newcomer joins partway. The friend cannot open the door. The holder opens it onto nobody. The friend keeps a witness's account naming the holder, the one away for the wish keeps one of seeing only what it brought, and the newcomer keeps a late joiner's. Nobody away is credited, and the holder resolves as the one who acted. The one who missed the ending reads the aftermath when they come back.
- **The mill envelope** pays the holder alone, once.
- **The endings.** The last wish, the paw back on the fire, a fire that will not take it before any wish, and a lost paw each end or resume their own thread. A holder gone three days leaves the paw in the fire, and the old paw answers no more. Whoever of the thread comes takes it out again.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
