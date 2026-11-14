# Usher's coffin (0.4.587)

The owner, playing on 14 November:

> "corpse is still moving her arms in house of usher. she's also slightly edging outside of the coffin"

> "right-clicking the coffin with her in it still has the lid staying open?"

New builds only. One new line, a control line (below). No protocol, layout or Witness change. The house, the vault, the
woman, what she does after the lid is chosen, and both endings are as they were.

## What was there

- **Two small boxes, not one coffin.** The vault's coffin was two of the literary rooms' shared coffin props set end to
  end. Each was a box of its own, ten pixels wide inside, with a board across each end, so two boards crossed the
  middle of her.
- **She was wider than it.** She is 13.6 pixels across the arms and the boxes were ten wide inside, so her arms lay in
  the side boards and showed through them.
- **She lay too high.** Her middle was seven pixels up and her head is 6.8 thick, so her face stood above the boards,
  and through the lid when it was down.
- **Nothing held her still.** She had no pose of her own in the coffin, so the slow sway every standing figure's arms
  have ran on her, lying down.
- **The lid's rule was hidden.** A plain use of the coffin meant "leave it open", for good, and said "The lid stays
  open. Return to the wing." Only a use while crouching closed it, and nothing in the room said so. So a right-click
  left the lid open because that is what the rule made a right-click mean.

## What it is now

### The coffin

- **The house's own, one box two blocks long:** the half her feet are in and the half her head is in.
  - Fourteen pixels wide inside and twenty-eight long, on a floor two pixels thick with linen laid in it.
  - Boards one pixel thick along the sides and two at the ends, to a rim nine pixels up.
  - Two brass handles on each long side.
- **Open,** the lid stands on its hinges along one long side, a block high above the rim.
- **Shut,** it lies on the rim.
- **Its pictures** are drawn at sixteen pixels to the block by `tools/draw_usher_art.py`: lengthwise boards with nails,
  a lid of three boards on two rails, linen, brass.
- **It is solid** to ten pixels up, open or shut, so nobody walks through it.

### The woman

- **She lies lower:** her back on the linen, her face under the rim, and under the lid when it is down.
- **She is still.** She has a pose of her own: every part at rest, her arms at her sides and turned a little in toward
  her, her legs straight. The sway does not reach her.
- **She fits.** 13.6 pixels across in fourteen, 27.2 long in twenty-eight.

### The lid

- **A hand on the lid says how it works.** The first use of the coffin chooses nothing. It shows, in the grey italic of
  the House's other controls:

  > (Use the lid again to close it for good. Walk away to leave it as it is.)

  This is the one new line.
- **A second use closes it, for good.** Both halves shut, and the line is the one that was there: "The lid has closed.
  Return and listen."
  - The second use counts only half a second or more after the first, so a held button is one hand and not two.
- **Leaving it open is walking away.** When nobody is left in the house, and somebody who stood at the open coffin has
  walked out of it, the lid has been left as it is. Each of them is told what was said before: "The lid stays open.
  Return to the wing."
  - It counts only for a reader who knows what the lid asks: one who has read the vault register through ("Tonight I
    will close the lid for good, or leave it as it is"), or who has put a hand on the lid.
  - A reader who goes offline inside the house has not walked out of it.
- **Crouching no longer matters.**
- **With more than one reader** it is as before: the lid is the house's, the first choice is everyone's, and a reader
  who comes after is told "Someone closed the lid before you came. Return and listen." or "Someone left the lid open
  before you came. Return to the wing."

## Checks

- **A new case for the lid** (`LiteraryTests.aHandOnTheLidAsksAndASecondUseShutsIt`):
  - she lies at the middle of the foot half, still;
  - both halves are open, and solid;
  - a reader who knows nothing stands at the coffin and walks out, and nothing is kept or chosen;
  - a first use chooses nothing and is kept; a second use at once does nothing;
  - a use half a second later shuts both halves; walking out afterwards changes nothing.
- **The open-lid ending's case** now reaches it by standing at the coffin and walking out of the house.
- **Ten cases in all pass:** the house, the coffin's return, the storm night's reading, readers together, Roderick and
  the account, the open-lid ending, the new lid case, staying inside as the house falls, the co-op line when one reader
  chooses, and the two-reader scene check.
- **The game's own renderer,** in the coffin proof: the coffin open with her in it, and shut. The proof now also
  - walks every box she is drawn with (head, hair, gown, arms, legs) and requires each inside the boards, under the
    rim and above the floor;
  - requires her to be her whole length;
  - poses her at four different moments, after the sway has been applied, and requires her the same at each.
- **The art tool's check,** the quick checks, and a compile.

Not checked:

- **Nobody has stood in the vault in a played game** since the change.
- **The proof's frame is lit evenly and from above.** The vault is dark, and nobody has looked at her there.
- **A box's own size is what is measured.** Her hair and gown stand a fraction of a pixel off her (0.4 at most); that
  fraction is inside the rim by arithmetic and is not measured.

## Caught while checking

- **The proof had been drawing a child.** A model is made "young" until something says otherwise, and drawn young she
  is half her length. The first frame of the new coffin showed a small figure in the middle of the box with linen
  beyond her head, and passed, because the proof tested an outline and not her. She is drawn grown now, and measured.
- **A held button.** The game repeats a use every fifth of a second while the button is down, so "use it twice" would
  have closed the lid on one long press. Hence the half second.
- **Walking out without knowing.** A reader who has never read what the lid asks could have "left it open" by passing
  the coffin on the way out. It counts only for one who knows.

## For the owner

- **Two uses to close, walking away to leave it open,** are choices of mine to make the rule one a reader can find.
  If the lid should close on the first right-click, that is one line.
- **The new control line** is mine. It can be reworded or removed; without it the first use would have to close.
- **The lid stands open along one side** and does not swing: it is open, then shut. A lid that swings down would be an
  animation of its own.
- **The old shared coffin prop** is left in the game, unused. Nothing else uses it.
