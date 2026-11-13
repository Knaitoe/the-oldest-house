# The Pell house 24: Tilly's senses (0.4.546)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house's twenty-third slice let a second reader play Tilly
(`docs/PELL_HOUSE_TILLY_0_4_545.md`). This is its twenty-fourth: what Tilly knows of the house that the older sibling does not. From the
proposal's "Tilly: believed, and cannot see":

> - **Partial sight.** Her view is light and shape, never black. Edges and faces dissolve past a few blocks, and strong light blooms.
>   - It scales with `Comfort.effects()` like every other effect, with no floor.
>   - Tilly is never the joke and never helpless. Her sight is a different way of knowing the house, and she is better at the house than
>     the reader in several ways.
> - **The cane.** Its tap sounds, and for a moment it outlines what is in reach: the edge of a step, a doorframe, the pool's coping, the
>   line underfoot. This is how she moves well, not a handicap.
> - **Hearing.** Some sounds reach only Tilly, subtitled for her alone:
>   - singing through Nessa's wall at night;
>   - the freezer's hum, which she can follow to the laundry;
>   - the tape hiss from the shed;
>   - June on the phone in the kitchen, telling Gail the things the older sibling never hears said.
> - **Feeling the line.** She cannot see the line, but a cold falls over her each time she crosses it (a short frost at the screen's
>   edges, no damage).

Everything here is the reader as Tilly's alone. With nobody playing her, the house's Tilly has these senses in her own way, and nobody
needs to be shown them.

## Her sight

- **Who:** the reader as Tilly, at the house, on their own screen. Nobody else's view changes.
- **The haze:** past a few blocks, edges and faces dissolve into a soft haze. At full strength things are clear to about two blocks and
  gone into it by nine.
  - It is never black: by day it is pale, at night a dim grey, so the house is still a shape in the dark.
  - Faces go into it too: another reader's name is not shown past where the haze closes.
- **The bloom:** standing in bright light (a lamp, the pool lit from below, a window by day), the edges of the view go pale with it.
- **Comfort:** the haze and the bloom follow the screen-effects setting, with no floor. At nothing, Tilly's view is the reader's own. The
  haze eases by ratio, so half the setting looks like half the haze.
- **In the water** the game's own underwater view is left as it is.

## The cane

- **Hers:** while a reader is Tilly at the house, the long cane from the hook is in their hand (in the slot they hold, if it is empty;
  otherwise the first room for it). It is marked as theirs.
  - It cannot be dropped.
  - Let go of any other way (put in a cupboard, taken out of the hand), it comes back to them within a quarter of a second, and a
    cane of theirs left lying about is gone.
  - With no room for it, they have none until there is room.
  - It goes back on the hook when they go: out of the house, dead, logged out.
  - In anybody else's hands, or theirs once they are no longer Tilly at the house, a marked cane is gone. An unmarked cane (one made by
    an operator) is left alone.
- **The tap:** the use key, on the air or on anything the house does not answer to a hand. Once a second at most; a second tap within it
  does nothing. Everybody near hears a cane tapping.
- **What it shows:** for two seconds, to the reader as Tilly alone, what is in reach is outlined, and the outlines show through the
  haze:
  - the edges of steps up and down, of a drop, and of the pool's water, within three blocks. Walls, fences and doors are not steps, and
    the house's furniture is passed over (the cane finds the floor under it);
  - the curb, the deck's boards and the pool's coping, where they lie level with what is beside them;
  - the frames of doorways: each door's, the slider's, the garage's and the shed's, the gates', its two posts and its head;
  - the line underfoot, its paint's two edges, only where it is unbroken, within two blocks.
- The outlines are what the cane found, so they show at any screen-effects setting.

## What only she hears

Each is a sound sent to the reader as Tilly alone, placed where it comes from, with its own subtitle, so only their subtitles show it.

- **Singing through Nessa's wall.** At night (from June's goodnight until morning, and on the last night until June comes for Tilly),
  with the reader as Tilly in Nessa's room or the hall outside it: ten seconds after they come into earshot, then every thirty-five to
  fifty-five seconds, a woman's voice singing, wordless and low, through the wall at the head of the bed. "Singing, through the wall".
- **The freezer's hum.** Whenever they are within sixteen blocks of it: a low hum from the laundry, louder as they come nearer, so they
  can follow it there. "A low hum". Having heard it (`HeardHum`), reaching the freezer they keep that they followed it (`FollowedHum`).
- **The tape hiss.** From the second visit, while the shed's deck is quiet: tape hiss from the shed, within eighteen blocks. "Tape hiss".
- **June on the telephone.** During Gail's call, while June is on the telephone, after each of her first four lines, she says something
  low into the receiver where she thinks nobody can hear it. The reader as Tilly, anywhere in the house, hears it: a caption from where
  she stands, in grey. The older sibling never hears it said. It is kept in their journal's record of what was said
  (`HeardJuneQuietly`, how many).
  - The first morning: "Tilly's in Nessa's bed. It fits, Gail. It fits." "The older one watches me. All the time." "No. Nobody goes
    near the pool. I've seen to that." "A week. Give me a week and you'll see."
  - A week in: "Tilly answers to it now. Doesn't even notice." (if they answered to the name a week in) or "Tilly still won't answer to
    it. That'll come."; then "The older one's been telling stories. You'll hear them. Don't listen." "It's all ready for next week.
    Nothing's changed." "You'll see, after. You won't know the difference."
  - The call waits for each of them as for her other lines, and goes on.

## Feeling the line

- Each time the reader as Tilly steps onto the line, on their own feet, wherever it is unbroken, a short cold falls over them: frost at
  the edges of their screen, the game's own frost, for a second and a half. No damage, no slowing.
- It follows the screen-effects setting, with no floor.
- They keep how many times (`LineCold`).

## How it is made

- **`labyrinth.PellSenses`** (new): the cane (giving it, taking it back, the tap and what it finds), what only Tilly hears, June's quiet
  lines (`hush`), and the cold of the line. Called from `PellTilly.after` each tick; `PellTilly` takes the cane back when the reader
  goes.
- **`labyrinth.PellCaneItem`** (new): `PellRegistry.CANE` is one now. Its use is the tap; it cannot be dropped; anywhere but in the hands
  of the reader who is Tilly at the house a marked cane is gone, and let fall it is gone too.
- **`network.PellSensePayload`** (new): the outlines after a tap; the line's cold; a line of June's said quietly.
- **`client.PellTillySight`** (new): the haze, its colour, faces in it, the bloom, the outlines and the cold, all but the outlines scaled
  by `Comfort.effects()`; and June's quiet lines as captions (`SpeechCaptions.voices`).
- **Sounds** (`tools/generate_pell_audio.py`): `pell.singing`, `pell.hum`, `pell.hiss`, each subtitled. The hum and the hiss are four
  seconds each and sent again as each ends.
- **`PellTexts`**: June's quiet lines (`JUNE_QUIETLY_FIRST`, `JUNE_QUIETLY_ANSWERS`, `JUNE_QUIETLY_NOT_YET`, `JUNE_QUIETLY_WEEK`,
  `juneQuietly`). **`PellCall`**: after each of June's lines, one of hers said low for the reader as Tilly. **`PellPeople.Household`**:
  the senses' own times.

## Decided here (any of it can move)

- The cane is an item in the hand, so the tap is the use key; a reader as Tilly without room for it has no tap until there is room.
- The haze is two blocks clear and nine gone at full strength; never black.
- Another reader's name goes into the haze with their face.
- The singing is a woman's voice, wordless; whose it is is not said. It comes from the wall at the head of Nessa's bed.
- The cane finds the curb, the deck and the coping by what they are, though they lie level, and passes over the furniture.
- The outlines show at any screen-effects setting: they are what the cane found, not an effect.
- The line's cold is the game's own frost at the edges of the screen.
- Four quiet lines to a call; a week in, the first knows whether Tilly has answered to the name.

## Checked

- Three new cases:
  - the cane: in the reader as Tilly's hand and not the other's; a tap just inside the front door heard by both and shown to the reader as
    Tilly alone, the front door's frame among it, and a second tap at once showing nothing; on the curb its edge; on the front step the
    line under the mat; it cannot be dropped; put away, it comes back to their hand; gone from the house, it goes back on its hook;
  - a week in by day: the freezer's hum to the reader as Tilly alone, heard and then followed to the freezer; the tape hiss from the
    shed, to them alone; the line's cold each time they step onto it, counted, and nothing for the other on it too;
  - the first visit at night and in the morning: singing through the wall to the reader as Tilly in Nessa's room and not to the other in
    the hall outside; on the telephone, June's four quiet lines to the reader as Tilly alone, in order, kept in their record of what was
    said and not the other's, and the call going on.
