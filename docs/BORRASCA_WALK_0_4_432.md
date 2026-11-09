# Borrasca: talking on the walk up (0.4.432)

The owner, asking for the longer hill: "expand the hill totally, so it's a walk to the tree and a farther walk to the mill. The walk
gives Nell and the player and other potential npcs, if not occupied by players, time to talk and bond." The hill was lengthened in
0.4.419 and 0.4.420, but on the walk itself Kelly (Nell then) said nothing except "Come on!" when the reader fell behind.

This part gives Kelly things to say on both walks: up to the Tree on the first visit, and on toward the mill on the second.

- Once the games by the trailhead are over and she sets off for the Tree, she says four things on the way, one at a time:
  - "My mom says I can go as far as the Tree and not past it. So the Tree's the edge of everything."
  - "Dana used to walk me up here. She knew all the birds. I only know the loud ones."
  - "If you hear the knocking, count it in your head. Don't say the number out loud. That's the rule."
  - "Do you want to be best friends? You don't have to say. You can just keep walking."
- She talks only while the reader is within eight blocks of her, four seconds after her last line at the earliest, and never over
  anything else being said. A reader who runs on ahead or hangs back misses what she says while they are away; she does not repeat it.
- What she says is a caption like everyone's, and goes into the journal's "What was said" (0.4.429).
- The account remembers it: "She talked on the way up to the Tree." or, if the reader heard all four, "She talked the whole way up to
  the Tree, and asked if I wanted to be best friends."

## The walk to the mill

On the second visit, between the Tree and the clearing where she is taken, Patrick is missing. She says four things the same way, never
while she is calling the reader down behind the timber:

- "Patrick's dad sat out on the porch with the light on all night. Kendra says he never went in."
- "The sheriff told my mom Patrick ran off. He didn't even take his bike."
- "Same rule as before. If you hear the knocking, count it in your head."
- "If something happens, you run home. Don't wait for me. Promise."

The account adds "The next time, on the way to the mill, she told me about Patrick.", and, if the reader heard all four, "and made me
promise to run home if anything happened."

## Code and tests

`BorrascaStory.walkTalk(p,own,friend,lines,key)` (`WALK_TALK` and `WALK_TALK_TWO`, `WALK_TALK_GAP` 80 ticks, `WALK_TALK_NEAR` 8 blocks;
`WalkTalk`/`WalkTalkWait` and `WalkTalkTwo`/`WalkTalkTwoWait` saved). `BorrascaTests.kellyTalksOnTheWalkUpToTheTreeWhileTheReaderKeepsNear`
checks that hanging back a dozen blocks hears nothing, that walking beside her does, and that the journal and the account keep it;
`kellyTalksAboutPatrickOnTheWalkToTheMill` walks beside her on the second visit to the clearing.

Still to come in this part of the rework: the other children walking along when no other reader takes their place.

## Run 1498: a line kept twice

Run 1498 failed one exploration case, `TruantTests.johnnyResumesAnInterruptedQuestionWithTheSameReader`, on "recovery keeps one
transcript". The 0.4.429 journal change keeps every line an actor says aloud for whoever hears it, so a line Johnny said inside his
conversation panel was kept twice: once in the panel's transcript and again as overheard. `TalkHistory.overheard` now leaves a line to
the reader's open exchange when that exchange has just set it down (`Talk.holds`). The case now counts the panel's transcripts only;
what Johnny says aloud at the held door, outside the panel, is rightly kept apart under its own heading.

## Numbers

No protocol, Witness or layout change: protocol 173, Witness 86/65/63, layout 135. New builds only.
