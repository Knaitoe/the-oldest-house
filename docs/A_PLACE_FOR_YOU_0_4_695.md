# A Place for You: friends, or more than that (0.4.695)

The eleventh slice, and the last part of item 7 of `docs/A_PLACE_FOR_YOU_PLAN.md`: friendship or romance with the guide (Alex or Ren),
settled between the two of them and never inferred.

Code: `PlaceForYouBond` (new); hooks in `PlaceForYouOlder` (the landing's "something of our own" no longer closes the exchange),
`PlaceForYou` (the evening's clock, talking) and the account in `PlaceForYouTexts`. No new art.

## What happens

- **On the landing.** "Then let's have something of our own." used to end there. Now the guide says "All right. Something of our own.
  Starting tonight." and the two of them settle what kind:
  - **The guide asks.** "As friends, or more than that?" The reader can say as friends; more than that, if the guide wants it too; or
    that they don't know yet.
  - **More than that, from a guide who doesn't know the reader well enough.** Unless the two of them really stood under the eaves
    together (in the coat room's read, the guide was the one beside the reader), the guide says so, and asks to be asked again before
    the night is out.
  - **The guide offers first.** When the House's read points at the guide (MEN and Alex, WOMEN and Ren, or BOTH) and they did stand
    under the eaves together, the guide says "I'd like it to be more than friends. If you would." The reader can say they'd like that
    too; that they didn't mean it that way (a friendship, not a refusal: "Then friends. That's no smaller."); or that they don't know
    yet.
- **Asked again.** A reader told to ask again is asked about it once, the next time they talk to the guide at dinner or in the open
  house before the guide has said where they are going: "I'm asking again." (yes), "Friends is enough.", or "Not now."
- **At the end.** Whatever was settled, it doesn't decide where the guide goes. If it was more than friends, once the guide has said
  where they are going and the reader is near, the guide says "What we said on the landing doesn't decide where I go."

Nothing is inferred from anything else the reader did; nobody else in the house remarks on it.

## With more than one reader

The landing, the question and the answer are each reader's own copy and record. Two readers can settle different things with the
same guide.

## New lines

Every line below is new in this release.

**The reader**

- "As friends." / "More than that, if you want it too." / "I don't know yet." *(new, flagged)*
- "I'd like that too." / "I didn't mean it that way." *(new, flagged)*
- "I'm asking again." / "Friends is enough." / "Not now." *(new, flagged)*

**Alex or Ren, the guide**

- "As friends, or more than that?" *(new, flagged)*
- "Friends, then." / "I do." / "Then we don't have to know tonight." *(new, flagged)*
- "I don't know you well enough to say yes to that yet." / "Ask me again before the night's out." *(new, flagged)*
- "I'd like it to be more than friends. If you would." / "Then that's ours." / "Then friends. That's no smaller." *(new, flagged)*
- "You were going to ask me something again." / "All right. Yes." / "Ask me tomorrow what that means. I'd like there to be a
  tomorrow to ask in." / "All right." *(new, flagged)*
- "What we said on the landing doesn't decide where I go." *(new, flagged)*

**The account** (after the landing)

- "[Guide] asked if I meant as friends, or more than that." then one of " I said as friends." / " I said more than that, if [guide]
  wanted it too, and [he / Ren] did." / " I said I didn't know yet." *(new, flagged)*
- Not known well enough: " I said more than that, if [guide] wanted it too. [Guide] didn't know me well enough to say yes to that
  yet, and said to ask again before the night was out." then " I didn't." / " Later I asked again, and [guide] said yes." / " Later I
  said friends was enough." / " Later [guide] reminded me, and I said not now." *(new, flagged)*
- Offered: "[Guide] said, \"I'd like it to be more than friends. If you would.\"" then " I said I would." / " I said I hadn't meant it
  that way. Friends, then, [guide] said, and that was no smaller." / " I said I didn't know yet. We didn't have to know tonight,
  [guide] said." *(new, flagged)*
- " At the end [guide] said that what we had said on the landing didn't decide where [he / Ren] went." *(new, flagged)*

## Cases

- `PlaceForYouTests#theBondAskedAgain` (new): something of our own on the landing; the question; more than that from a guide met
  only tonight, not known well enough; asked again at dinner, once; yes; the account; the other answers' sentences from the record.
- `PlaceForYouTests#theBondOffered` (new): the read points at Alex and they stood under the eaves; Alex offers first; the reader says
  yes; Alex says where he is going and then that the landing doesn't decide it; the account.

## Results

Locally: the two new cases, `theOlderPictures`, `theBelievedPast`, and the cast and together conduct checks for A Place for You, 6 of
6, on the first run.
