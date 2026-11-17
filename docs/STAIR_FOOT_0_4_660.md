# The foot of the stair (0.4.660)

The owner, with the notes that led to 0.4.656: "Should there be any fanfare at the platform leading to the Minotaur's cell? You don't
have to implement the suggestions at face value. Please use your own discretion and judgement."

By judgement, none. The descent is long and dark, and its score and its growls have been with the reader the whole way down. Coming
off the last tread is marked by those things stopping, not by anything new beginning: the score ends, the growls stop, and after a
hush the cell answers once. No Witness, layout or protocol change. New builds only.

## Now

- **The score ends on the last tread.** When a reader comes off the stair into the chamber before the cell, the descent's score dies
  away over two seconds and stops. It does not start again until they are back on the stair, and no game music starts in its place.
  The chamber is kept as quiet of the game's own cave sounds as the stair is.
- **The growls stop.** The growls, the shaking, the dust and the falling stone that came every half minute or so on the way down do
  not come at the foot of the stair.
- **Then the cell answers.** Once a reader has stood at the foot of the stair for five seconds together, the creature growls once, from
  the cell, for that reader alone. It does so once for each reader, ever. A reader who goes back up the stair before the five seconds are
  over, or is gone from the foot for more than a second, starts the five seconds again the next time they stand there.
- **The lanterns hang at the gate.** The chamber's two lanterns hang either side of the cell's gate, six blocks up on chains from the
  ceiling, clear above the creature's horns. Before, they hung by the chamber's side walls. What is lit at the foot of the stair now is
  the bars, and the lectern before them.
- **The line about the bars waits.** "Use the bars to open the cell. The stairs still lead back. The cell does not." used to be shown as
  the reader stepped off the stair. It is shown now a few steps short of the bars, where it is read before the bars are in reach, and
  it is kept the moment it is shown, so it is shown once.
- **Nothing else.** No words, no sting, no title, and nothing slams.

## With others

Each reader's hush and growl are their own. The growl is sent to that reader alone, from the cell, and another reader standing there
hears their own when their five seconds are over. The score belongs to each reader's own game. The lanterns are the same for everyone.

## Checked

- `StaircaseAccessTests.theFootOfTheStairIsQuietUntilTheCellAnswers`:
  - The chamber from the doorway off the stair to the cell is the foot; the stair's last steps, the well above the chamber and
    beyond its walls are not.
  - On the stair a reader hears the descent's growls; at the foot they stop.
  - The finale's own tick starts the hush at the foot, and nothing is said in the middle of the chamber.
  - No growl for the first five seconds; then one; then never again, even after going back up the stair and coming down.
  - A second reader who goes back up before the five seconds are over loses the hush; coming down again takes all five seconds, and so
    does standing there again after more than a second away; then the cell answers them too.
  - Near the bars, the line is shown and kept, a few steps short of them.
  - Each gate lantern hangs above the creature's head and below the ceiling, beside the gate and before the bars.
- The staircase cases beside it (a door lands on the first tread, the climb back up, the readers' people on the stair and at their
  fires) and the descent's growl, shake and debris cases.
- Review frames of the chamber at eye level: off the stair, from its middle, before the bars, from one side, the lanterns close, and
  back from the gate.

## Not covered

- The score dying away is drawn by each reader's game and is checked by reading it, not by a case or a client frame.

## New lines

None. The line about the bars is the one there was; only when it is shown has changed.
