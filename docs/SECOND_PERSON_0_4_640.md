# Second Person: who comes for supper (0.4.640)

The proposal's DRAWN_TO variant ("Someone to care for", docs/SECOND_PERSON_PROPOSAL.md, chapter 2). New builds only. No layout,
protocol or Witness change: 90 sources / 68 required / two kinds / three endings. A reader the House has not settled keeps Sam,
unchanged.

## What changes

The arrival snapshot keeps the House's read of who the reader is drawn to, settled only (`DrawnTo`; the server option that turns the
read off is honoured, since an unread trait gives nothing). `SecondPersonStory.guest(own)`:

- **MEN:** Dev, from work, coming for supper for the first time.
- **WOMEN:** Hannah, from work, the same.
- **NO_ONE:** Marcus, a friend who needs somewhere to live. His evening is about whether he moves in, with its own talk, its own
  question at the end ("Could I stay? Not tonight. I mean live here."), the resident's own answer, and his own lines in spring, at the
  two cups and in the account ("Marcus moved in." / "Marcus found somewhere else.").
- **BOTH, or no settled read:** Sam, as before, with what they are to each other left to the resident. Judgement: the proposal's two
  evenings for BOTH are not built; one guest whose part is left open carries "cannot decide" without doubling the season.

Every place the guest was named follows (`SecondPersonStory.guest`): the resident's "...'s coming. Right.", the window line, the cold
remark's record, the spring knock and its talk panel, the spring decision, the two cups and the account. Each guest has their own
stranger phase (Sam 76, Dev 214, Hannah 215, Marcus 216) and painted skin (`literary_flat_dev`, `_hannah`, `_marcus`, by the shared
painter; catalog entries added). No pronoun is used for the reader.

## New lines (all new in this slice)

Marcus's evening: "This is good of you. I mean it." / "It's only supper." / "I'm on Gerry's settee. Gerry has views about the
settee." / "How long can you stay there?" / "Till the end of the month. Then I don't know." / "Iris feeds me. She'd feed you." /
"I'd pay my way. I'd do the bins." / "Nobody does the bins."

Marcus's question and the answers: "Could I stay? Not tonight. I mean live here. Just till I'm sorted." / "...All right. Yes.
Stay." / "I don't think I can. I'm sorry."

Spring: "Marcus says we could find somewhere bigger. The two of us." / "Marcus and me are going to look for somewhere."

Two cups: "Marcus is on lates this week. We hardly see each other. I like knowing he's in." / "Marcus found a room in the end. He
rings on Sundays."

Account: "When Marcus came round about the spare room ..." / "Marcus moved in." / "Marcus found somewhere else."

The other lines are the existing ones with the guest's name in place of Sam's.

## Checks

- `SecondPersonTests#whoComesForSupper` (batch second_guests): each read brings its guest and phase (four guests, four faces); the
  account names the guest and nobody else; Marcus moved in; spring and the two cups know who.
- The three skin sheets reviewed beside Sam's.

## Multiplayer

Each reader's flat is their own copy and the guest is from their own snapshot.
