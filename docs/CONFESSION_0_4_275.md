# The listener's questions, answered from memory (0.4.275)

The owner: "Might be worth revising the confession vignette to use the new dialogue tree, house memory, and personhood vignettes".

## What stays

The interview room, the listener, the blank book from the low shelf (or one of the reader's own), the eight dictated chapters and
their eight questions, the thirty seconds of silence, his rest between chapters, the count, the sealed journal in the wall, the
later return to read it, the account and the Witness source. Typed chat still works: whatever a reader types while his question is
open is taken down as their own words, never reaches public chat, and has nothing written under it.

## What changes

**He asks in the conversation panel.** When a chapter's sentence is down, the listener's question opens in the reader's own panel
(the one A Place for You introduced), with answers to choose. Saying nothing is always the last one and is written down as
"(did not answer)". Closing the panel leaves the question waiting; turning back to him (right-click) brings it up again, and so does
standing before him after the server has restarted. His fragment as he writes, and the silence when it comes, appear in the panel
rather than in chat.

**The answers come from what the House remembers of that reader.** `labyrinth/ConfessionMemory` builds them at the moment he asks,
from the reader's own record only:

| Question | True answer from | Untrue answer | Written under the untrue answer |
|---|---|---|---|
| What name did you come here under? | the reader's name | "A name that isn't mine." | the name |
| Which door here did you open first? | the first quiet room they went through and what they did there ("The kitchen. I drew myself smiling, at the kitchen table…") | "I don't remember." | the same, in the House's second person ("You drew yourself…") |
| What did you take because nobody stopped you? | the coat off the peg (or outfit off the rail); else what they kept hold of in the waiting room; else the bathroom counter's thing | "Nothing." | "You took the wool overcoat off the peg." |
| Who is missing from your account? | the latest companion the House saw them lose, by name ("my wolf" if it had none) | "Nobody." | the companion ("Moss.", "Your wolf.") |
| When you tell it, who do you tell it to? | whoever has spoken to them most in the panel (TalkHistory) | "Nobody. I keep it." | "You talked to Kit more than to anyone." |
| How many? Say the number. | the count of villagers, wolves, iron golems and wandering traders killed | "None." (when there were some), "I stopped counting." | the number |
| Whose name would you rather I did not write? | the people most recently heard from, and "My own." | (a wish: nothing is corrected) | |
| Now say it in your own words. What did you do? | the reader's own earlier replies to other people, word for word; and "I'll tell it as it happened.", the House's first breath about their quiet rooms in the first person | "I did what I was told." | the House's first breath about them, in its second person |

Where the House has no memory to go on (the quiet rooms not walked, or the read turned off in the server configuration; no
companion lost; nobody talked to), it offers plain answers and corrects nothing. It never fills a silence.

**A line in another hand.** When an untrue answer is chosen, the journal takes it down as it was said, and under it:
"(Under it, in another hand: …)". The reader sees it appear in their book at once, and the panel says so. The sealed journal keeps
it under the answer. The account adds one line before the signature: how many answers had a line under them ("Under one of my
answers was a line I had not written. It was right."), or that none did. A journal sealed before this version has no such line.

**The reader's own replies are marked.** TalkHistory now keeps an `R` flag on each line the reader chose in the panel, so the
listener can offer their own words back without mistaking narration for them. Older history has no flags and offers none.

## Rules kept

Everything is the reader's own: another reader's record never reaches their offers or their journal, and spectators keep nothing.
The House writing never names the reader's gender, colour or age; the quiet-room clauses it recites are the same observations the
account's opening already uses. Speech is in the panel or over his head, not in chat. New builds only. No Witness change:
74 / 56 / two kinds / three endings.

## Tests

- `LiteraryTests.theActualCarriedQuillResumesAcrossVisitsAndTheSealedJournalIsReadPersonally` now checks the panel at every question
  (the question line, saying nothing last), answers the second question in the panel (the front door, nothing under it), the sixth
  untruly ("I stopped counting.", the House writes "None." under it), the fourth by silence and the rest by chat, then checks the
  sealed journal and the account's line.
- `LiteraryTests.theListenersAnswersComeFromWhatTheHouseRemembers` (new, batch `literary_confession_memory`): a reader with a
  kitchen drawing, a coat off the peg, a lost companion and a conversation with Kit is offered each of the above, true answers with
  nothing under them, untrue ones with the House's line; saying nothing is last everywhere and never corrected; a second reader is
  offered none of it; TalkHistory marks the reader's own reply; the sealed pages carry the line under the right answer only.
