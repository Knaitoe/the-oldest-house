# The list, part 5b: night two's other tests (0.4.722)

The rest of night two's tests (`docs/THE_LIST_PLAN.md`, part 5). After the field Amber tests the field house's lock, whether he can tell
whose heart is behind a wall, and what a fall off the water tower does. Each test writes a rule into the notebook, and two of those
rules are wrong. The first name goes out only after the last one. No protocol, layout or Witness change.

## The order

The tests come in turn, decided by the rules already written in the one who went in's record. A town picked up again mid-test finds
Amber starting the next test that has no rule yet. Two seconds after each rule is written, she starts the next test.

## The door

1. **In.** She tells them the field house is next: she is going in and locking it, and they get in. She walks up the field, through the
   gap in the fence, opens the field house's two back doors and steps inside.
2. **The bolt.** If the one who went in is inside, she tells them to get out first; they have to be on the other side of it. Once they
   are out, she shuts both doors and bolts them. They hear the doors bang shut and the bolt go across.
3. **Struck.** The bolted doors open to nobody's hand. With the body, a hand or a blow on one is a strike, as on any bolted steel door in
   the hunt: the fifth blow tears the bolt out and the door swings in.
4. **Written.** She says she counted five, and that it's a steel door. The rule: locks don't stop him, steel doesn't either, five hits.

## The heart

1. **Out.** She sends them back out to stay out until she bangs on something. If they are inside, she tells them to get out.
2. **Hidden.** Once they are out, she goes into one of the two rooms behind the block wall, the coach's office or the showers, picked at
   random. Two seconds after she gets there she bangs a locker door; they are told.
3. **The guess.** Whichever of the two rooms they come into first is their guess. The body hears her heart through the wall as it hears
   anyone's (`TheListBody.hearts`), so the guess can be made by ear.
4. **Written.** The right room: she asks how they knew which one, there's a wall, and writes that he hears your heart through a wall and
   knows it's yours. The wrong room: she laughs and writes that he hears hearts through a wall but can't tell whose and has to guess.
   That rule is wrong: what the guess was made from is the same either way.

## The tower

1. **The walk.** She says it's the last one: the water tower, up and off it. She leaves the field house by its back doors, crosses the
   field to its gate, goes east along Seventh, over the highway and the tracks, and waits at the tower's foot looking up.
2. **Up.** On the catwalk, she calls up that they go off it now, not by the ladder.
3. **Off.** Off the catwalk and falling, not on the ladder, the body lands, goes down whatever it had left, and gets up after the usual five
   seconds. Climbing back down the ladder is not the test; she tells them it's the ladder, off the side, and they can go up again.
4. **Written.** Up again, she says five seconds and they got up, and that higher would be longer, it has to be. The rule: if he falls he
   stays down a while, five seconds off the tower, higher, longer. That rule is wrong too: it is five seconds from any height.

## The first name

Five seconds after the last rule is written, the first name goes out and night two's hunt begins, as in 0.4.721.

## Single and multiplayer

- **Alone**, the tests are the one who went in's: only their strikes, their guess and their fall count.
- **With others**, Amber is one person in the town and everyone near her hears her; the bolted doors open to nobody's hand. The other
  readers' side of night two is part 8.
- **Spectators** strike nothing and count for nothing.

## Retreat

The House's door stays open all through the tests.

## Ended

The night ended, the field house's back doors are shut again and Amber's walk is forgotten with the rest of the den.

## Made as

- **`TheListDen`**: `DOOR`, `HEART`, `TOWER`, `DONE` and `test(rules)` (the next test for the rules written), `RULE_DOOR` 1,
  `RULE_HEART` 2, `RULE_NO_HEART` 3, `RULE_TOWER` 4; `BACK_L` and `BACK_R` (the field house's back doors), `DOOR_WAY`, `BEHIND`,
  `ROOM_WAY` and `ROOM_OUT` (into and out of the office and the showers), `TOWER_WAY`, `UNDER`, `KNOCK_AFTER` 40, `ASK_AFTER` 40;
  `tests` (one tick of whichever test is on), `along` (a way a step at a time, the back doors opened as she comes to them), `room`,
  `bolted`, `onCatwalk`, `tested`; for checks `toTests`, `atTest`, `heartRoom`, `knocked`, `climbed`. Her place on a test's way is kept per
  town, never saved, like the walk to the field. Talked to during a test she says what the test is; after the last, that that's all of
  them.
- **`TheListBody`**: `strike` takes a door Amber bolted as it takes one the five bolted; `fall` puts the body down after a long fall.
- **`TheList.click`**: a door Amber bolted opens to nobody's hand.
- **`TheListNights.ran`**: the first name waits for every test; `toRan` (for a check) writes the tests' rules first.
- **`TheListTexts`**: `RULES` gains four rules; the tests' lines.
- No new sound, block or art.

## Tests

`TheListTests.nightTwoTests` (new, batch `the_list_tests`): after the field, the door is next. Set down behind the doors with the one who
went in inside, she does not bolt them; outside, she does, and they hear it. A hand does not open them, four blows do not do it, the fifth
springs one, and she counted. Sent out, they hear a locker bang; she is in the room she chose; coming into the other room first writes the
wrong rule and not the right one, and no name is out yet. At the tower, up on the catwalk she sees them; off it, the body goes down,
gets up, and the last rule is written. The notebook they carry has all four, the wrong ones too, and the first name goes out.

`toTheEnd` passes with `toRan` writing the tests' rules.

## Every new line

Grey cues on the action bar, to the one who went in:

- "The field house doors bang shut. A bolt goes across on the inside."
- "Inside the field house a locker door bangs once."
- "The ground comes up all at once." (the body down after the fall)

Amber, over her head:

- The door: "Field house. I'm going in and locking it. You get in." / "Out first. You have to be on the other side of it." / "Five. I
  counted. That's a steel door."
- The heart: "Now go back out. Stay out till I bang on something." / "Out. Both feet." / "How'd you know which one? There's a wall." /
  "Wrong room. Ha. So you can't tell."
- The tower: "Last one. The water tower. You go up and you come off it." / "All the way up. Now off. Not the ladder." / "That's the
  ladder. Off the side." / "Five seconds and you got up. Higher would be longer, right? Has to be."
- Talked to: the door, "Next one's the field house. It has a lock on it."; the heart, "I'm not telling you where I am. That's the whole
  test."; the tower, "Up the ladder, off the catwalk. I'll watch from here."; after the last, "That's all of them. That's what we know."

The notebook's "TESTED" pages gain, as written, two rules to a page so none runs past what a book shows:

- "2. Locks don't stop him. Steel doesn't either. Five hits. (The field house.)"
- "3. He hears your heart through a wall, and he knows it's yours. (The field house again.)" or "3. He hears hearts through a wall, but
  he can't tell whose. He has to guess. (The field house again.)"
- "4. If he falls he stays down a while. Five seconds off the tower. Higher, longer. (The water tower.)"
