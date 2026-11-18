# The list, part 4e-2: the gin's press (0.4.719)

The second and last of the gin's set pieces (`docs/THE_LIST_PLAN.md`), after the auger (`docs/THE_LIST_0_4_718.md`). Like the auger it is
kept for the fourth night. With it the set pieces of part 4 are all built; part 5 begins the nights themselves.

## The press

The bale press stands at the gin's north end by the press door (`docs/THE_LIST_0_4_699.md`): a chamber below and the ram's head above.

1. **In.** The slasher with hold of one of the five beside the press puts a hand on it (or strikes it). Before the fourth night the
   slasher is told the press is shut down and its chamber will not open; nothing happens. On the fourth, the chamber opens, the slasher
   shoves them into it, drops the latch and lets go: the ram starts down. Their words, a scream; anyone on the list near enough runs, as in
   4a. What they had in hand they let fall. The slasher walks free, and the House's door is open to them again.
2. **The ram.** Over four seconds the ram comes down. They begin stood up in the chamber with the arms over the head pushing against it;
   halfway, bent under it; the motor strains every second and they fight it, but there is no getting out from inside.
3. **The release.** Anyone else's hand on the press, or on the one in it, hauls the release: the ram stops and goes back up, they come out
   onto the floor in front of the press and run. The chamber shuts again behind them.
4. **All the way down.** Left, the ram comes all the way down and they die folded in the chamber under it. The slasher is told; anyone in
   town within forty blocks who is not the slasher is told from which way a scream stopped. The kill is kept in the slasher's record, even
   if they have logged off since. Blood pools on the floor in front of the press.
5. **After.** A hand on the press there finds them folded in the chamber under the ram.

## Found

As in 4a-3: the rest of the five who see them say so (Shannon's own line says where), and calm ones run; a reader who is not the
slasher, near and looking into the chamber, is told once a hunt that they are folded in it under the ram. Since the body lies inside the
press, it is seen by the open chamber rather than by its own eyes.

## Single and multiplayer

- **Alone**, nobody else reaches the release: the reader shoves one of the five in and the ram does the rest.
- **With others**, the release is any other reader's: one who gets to the press in time stops the ram and lets them out; the slasher's own
  hand only finds them in the chamber. The ram, the motor and the scream are the same for everyone near.
- **Spectators** touch nothing and are told nothing.

## Retreat

Once the chamber has them, the slasher has let go: their walk and the House's door are their own again, as after any hold let go.

## Ended

The hunt ended (or begun again), everyone goes back to their place, unmarked, and the chamber is shut as it was built.

## Made as

- **`TheListGin`**: `PRESS` (the press's lower half), `RAM` 80; `press` (which blocks of the town it is), `chamber` and `front`, `open`
  and `shut` (the chamber taken away while someone is in it, put back after); `hand` sends the press to the hunt.
- **`TheListHunt`**: the state `PRESS`; gestures `PRESSED` (25), `BENT` (26) and `FOLDED` (27); a runner's `pressed` and `ram`; `press`
  (the slasher's shove, anyone else's release, a hand on the dead), `pressedIn`, `chambered`, `pressing` (each tick: the ram, the strain,
  the struggle), `released`, `crushed`; `ram` for tests. The end of a hunt moves anyone left in the chamber out and shuts it.
- **Client**: three poses (`LiteraryRenderers.CastModel.hunted`): stood with the arms up against the ram; bent under it, the arms still
  up; folded down in the chamber with the arms slack. The low poses are set back into the one-block chamber (`back`).
- **Sound**: the existing `the_list.strain` (the ram coming down) and the vanilla bone crack and piston sounds. No new sound or art.

## Tests

`TheListTests.thePress` (new, batch `the_list_press`): the first night: the slasher's hand on the press with hold of Deek is told it is
shut down and changes nothing. The fourth night: the slasher's hand shoves Deek in; the slasher's own hand on the press finds him held;
the other reader's hand hauls the release: Deek is out in front of the press and running, the chamber shut. The hunt begun again, Wes is
shoved in, bent halfway, and crushed: the mark, the slasher told, the kill kept, the scream stopped; the other reader's hand finds him
still, and the other reader, looking, finds him in the press. Ended: the press as it was.

## Seen in the frames

Eye-level frames of Deek just shoved in, bent halfway and dead, each from north, west, east, northeast and close. The chamber is the
press's lower half taken away, so he stands in the opening with his upper half under the ram's head. The first frames had the bent and
folded poses reaching out of the front of the chamber, the folded body lying a block clear of the press; both are now set back into the
chamber (`CastModel.back`). Bent, he is down on buckling knees with the hands still up against the ram; dead, he is folded at the foot of
the press under it, the blood pooled in front.

## Every new line

Speech, over the speaker's head as their name, shoved into the press:

- Deek: "Get me out of here! Get me out!"
- Wes: "It's coming down! It's coming down on me!"
- Mel: "There's a release on the side! Somebody pull it!"
- Lesley: "I can't stand up in here! I can't stand up!"
- Shannon: "Jenna! Jenna, I'm in the press!"

Shannon, coming across one of the others in the press (the others keep their lines from 4a-3): "That's %s. That's %s in the press."

Grey cues on the action bar, to the slasher:

- "You shove %s into the press's chamber and drop the latch. The ram starts down."
- "The ram comes all the way down. %s goes still."
- Before the fourth night: "The press is shut down tonight. Its chamber won't open."

To the slasher's hand on the press with one in it: "%s is in the press's chamber, the ram coming down."

To anyone else hauling the release: "You haul the release. The ram stops and goes back up, and %s comes out of the chamber."

To anyone whose hand is on the press where one died: "%s is folded in the press's chamber under the ram."

To a reader who is not the slasher, coming across them: "%s is folded in the press's chamber, under the ram."
