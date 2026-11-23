# The cold room, part 2: the night (0.4.755)

The second part of the cold room (design: [WAXWORKS_AND_COLD_ROOM_0_4_749.md](WAXWORKS_AND_COLD_ROOM_0_4_749.md); part 1:
[COLD_ROOM_0_4_754.md](COLD_ROOM_0_4_754.md)). The house now has its night: things in the back bedroom move while nobody sees them,
and the voice in the bed speaks. The rite, the offer and the end come in parts 3 and 4; the House still does not deal it.

## The clock (`ColdNight`)

One clock for each copy of the house, running while a living reader is anywhere in it. When the house has been empty for half a
minute, the room is put back as it was built (the bed down, the drawers shut, the cross upright) and the clock forgets it.

## What moves

- From fifteen seconds in, one thing in the back bedroom is tried every ten seconds:
  - the bed rises off the boards, head and foot together, all four legs clear (the fitting's new stages 2 and 3; the girl lies three
    pixels higher with it);
  - after it has been up three seconds, it comes down again;
  - the drawers slide out;
  - the cross turns upside down on its nail.
- Each can move only while nobody can see it: no reader, and no spectator's camera, within 70 degrees of it with a clear line, or
  within two blocks. Whatever is looked at stays as it is. If nothing can move unseen, nothing moves.
- Everyone in the house hears it: the bed's knock (`cold_room.bed`), the drawers (`cold_room.drawer`), the cross on its nail. Each
  reader's count of what they heard is kept for the account (`StirsHeard`).
- A hand on the risen bed: "The bed stands clear of the floor. All four legs are off the boards."

## The voice

- While readers are in the back bedroom, she speaks every twelve seconds to one of them, in turn. The line is that reader's alone, in
  their chat, with a breath under it (`cold_room.voice`) that only they hear. Spectators are never spoken to.
- What she has for a reader is made only from what the House has really kept of them, in this order:
  1. their name;
  2. the last companion the House saw them lose, by name, and how (the game's own death message);
  3. their last death and where, and how many times they have died in the House (when more than once);
  4. the last line of their own they wrote in their journal in the House, exactly, and only where the House's read is on (the same
     lines the House is allowed to quote anywhere else);
  5. their name again;
  6. then only what is true in the room: that they came up alone, or that the others there cannot hear her; the priest's letter; the
     stair; the priest at the window; the lamp.
- She never invents a history. A reader with no lost companion, no death and no journal line hears only their name and the room.
- What she used is kept for the account (`VoiceUsed`: name, companion, death, own words).

## Lines

All new, for the owner's review:

- First time: "From the bed, in a voice that is not hers:" then the line; after: "From the bed:".
- "%name. That's what they call you, isn't it? %name."
- "Come closer, %name. You can hear me from there. Come closer anyway."
- "%pet is here with me. Not all of it."
- "I know how it went for %pet. %message. Ask me what it heard last."
- "%message, %place. I was there. I had the best view."
- "You have died %n times in this house. I was counting. You weren't."
- "“%line” You wrote that. I read everything you write."
- "Nobody came up with you. He asked for two." / "%others can't hear me. Only you can. Don't tell them what I said."
- "Keep your coat on. He told you that. I read it over his shoulder."
- "Five stairs up. You'll go down them again. Not the way you came."
- "He's at the window. He won't look at you while I'm talking to you."
- "The lamp is the only warm thing in here. Go and stand by it. I'll wait."

## Assets

- `tools/draw_cold_room_art.py`: the bed's risen head and foot (`bed_2`, `bed_3`).
- `tools/generate_cold_room_audio.py`: `bed`, `drawer`, `voice` (five cues in all).

## Checks

- `ColdRoomTests#whatMovesUnseenAndTheVoice`: with a reader looking out of the window, the bed rises (head and foot together), the
  drawers slide out and the cross turns, and both readers in the house hear each; put back, the room is as built; turned to the bed,
  only the drawers behind move; a spectator's camera on the drawers keeps them still; what she has for the first reader is their name
  and their lost companion, and nothing of theirs is in the second's; she speaks to each in turn and never to the spectator.
- The part 1 cases, and the group, cast and two-reader sweeps.
