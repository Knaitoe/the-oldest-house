# Johnny Truant's copying room: the missing sentence (0.4.359)

The last of the owner's "Johnny Truant · Encounter Pairs": the third pair of the deep dark halls, after the borrowed light and the hand
at your back. With it every pair in that document is built.

From the document:
- *Help:* "He supplies the line the reader lost from an account and waits while they compare it with the physical evidence."
- *Invested haunt:* "He quotes the reader's own words back with one sentence missing, changing who sounds responsible for what happened.
  The original account is still available. A friend can see which passage he left out while the reader decides whether to engage."

New builds only. No Witness change: 80 / 60 / two kinds / three endings. Layout 114 (one new place). Protocol 153 (three new fittings
and a sound).

## The room

`TRUANT_SENTENCE` (slot 214), one of his places, dealt only from the deep halls (twelve passes). It shares the one Johnny, his record,
the five-crossing weighting and the short gap between his halls.

- A dark passage leads into a long room lined with pigeonholes, two high, every one stuffed with folded sheets in the same small hand.
- At the far end stands his desk, with one lantern on it, the room's only light. On the desk lie a page and a green blotter.
- A drip comes from the ceiling onto the page.
- He stands behind the desk. The way on is round the end of it and out of the far corner.

## The account

The page is an account of one reader's history with him, written as if they were saying it. It is built from his record of what
happened and what they said:
- what he did for them (up to two things, such as holding the door or steadying them on the ledge);
- the place they told him about and their answer, in their words;
- their promise ("I said I'd come back.", "I said maybe.", "I told him no.") and whether they kept it;
- the last favour he asked and what they did;
- the last harm he did and how it ended for them;
- whether he said afterwards that he wasn't asking for anything.

## Help

- **The lost line.** The drip has run one line of the reader's account, nothing left of it but its length. It is their own words if he
  has them, else their promise, else the first thing he did.
- **He gives it back.** When they read the page he gives them the line, aloud: "The wet got at that one. It said, ..." Then: "The
  blotter took the ink when it ran. Look, if you want."
- **The evidence.** The blotter has the line taken off backwards. Read after he gave it back, it is the same words, word for word. He
  says "Same words. I don't change anybody's words." That help is kept (`HELP_SENTENCE`). The page is whole for that reader from then on.

## Turned

His turn here is `SENTENCE`. It is chosen like his others, and only for a reader past twenty passes with a history with him. While
anyone under twenty is in the room it is a claim. It is also a claim when the reader's account has nothing in it for him to leave out.

1. **He starts reading.** Once the one he wants is in among the pigeonholes, he says "Listen. Your own words. I took them down the way
   you said them." Then he reads the account aloud to everyone there, a line at a time.
2. **The line he leaves out.** He skips one sentence. That sentence is, in order of preference:
   - his harm, so that how it ended sounds like their doing ("I went over the edge.");
   - a promise they kept, so it sounds broken;
   - a favour they did for him;
   - their refusal.
3. **What is kept.** Once the left-out line has gone by, the misquoting is kept against him (harm `sentence`, `Misquoted`, the line
   itself). Everyone else there has it kept as heard.
4. **When he finishes.** He says "That's how you told it." The reader can:
   - say the missing line aloud ("That's not the part I was reading.");
   - say "That's not what I said." ("It's what you said. Every word of it's yours.");
   - say "That's all you get.";
   - or walk on, which is always open.
5. **The page.** All the while, the page on the desk has every line of their account. Anyone who reads it, friend or reader, can say
   the missing line aloud, and he stops: "I know what it says." A friend's word against his is kept for both (`Interrupted`,
   `Contradicted`).

- **Struck,** he stops reading.
- **Help after harm.** At the next meeting he says "I left a line out, reading it to them. I know which one.", asks nothing, and the
  help is real.

## His favour

"Put the lantern out. I know these by heart. Do you?" Agreed, the reader puts out the desk's lantern, the room's only light. It comes
back after a minute, or as soon as the room is empty, as the lamp does.

## Assets

- `tools/draw_truant_service_fittings.py` adds three fittings in vanilla's manner:
  - the page: written, run by the wet, or blank and ruled;
  - the blotter: clean, or with a line taken off backwards;
  - the pigeonholes.
- `tools/generate_truant_service_audio.py` adds one subtitled sound, "Paper is turned over".

## Tests

`TruantSentenceTests` (exploration):

1. **`johnnyGivesBackTheLostLineAndTheBlotterBearsItOut`.**
   - The room is dealt only from twelve. The page, blotter, lantern and drip are there.
   - The account is the reader's, and the wet takes their own line.
   - He gives the line back, aloud. The blotter shows it backwards.
   - The help is kept, and the page is whole for them afterwards.
2. **`johnnyReadsTheirWordsBackWithHisHarmLeftOut`.**
   - With nothing to leave out, it is a claim. With a reader under twenty there, it is a claim.
   - Alone with a friend past twenty, he waits until the reader is in among the pigeonholes, then reads their account aloud without his
     push.
   - The misquoting is kept, and the friend heard it.
   - The friend reads the whole account on the desk, says the missing line, and he stops. Both records keep it.

The existing Truant, service-hall, slot-overlap, gray-maze and architecture cases still pass.
