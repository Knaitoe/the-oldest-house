# The Pell house 22: the torn page (0.4.544)

The owner, 12 November: "start by finishing each unfinished vignette". The Pell house (`docs/PELL_HOUSE_PROPOSAL.md`) has its endings,
its aftermath (`docs/PELL_HOUSE_AFTER_0_4_542.md`) and, since its twenty-first slice, what follows a reader after a failure
(`docs/PELL_HOUSE_FAILURE_0_4_543.md`). This is its twenty-second: the false page torn out of the journal. From the proposal's "Being
framed":

> - **Tearing it out.** The reader can tear such a page out (`docs/JOURNAL_TORN_PAGES_0_4_199.md`), but a torn false page always turns
>   up again:
>   - in Gail's folder on the last visit, in her hands;
>   - after the vignette, pinned to a wall in a later hall.

## Tearing it out

- A page with a line in the reader's own hand on it (the house's false entries, `docs/PELL_HOUSE_ENTRIES_0_4_537.md`) can be torn out like
  any written page, even when the House began that page for the line and the reader wrote nothing on it. Until now such a page was
  refused as blank.
- The corner curls under the pointer and the page comes away as ever. The reader's own words on it, if any, pass the text filter as
  before; the line in their hand goes with the page.
- Pinned up, a page shows the lines in the writer's own hand at a glance, after their own words.

## In Gail's folder

- **When:** from first light on the last visit.
- **Whose:** every such page of each reader the morning is for.
- **From where:** wherever it was. Out of their pocket or anybody's (the copy left there is blank, as every copy of a page is once the page
  is somewhere else); off any wall; up from any floor. Never from in front of anybody who can see it where it is: then a second later.
- **Held out:** once Gail has said her piece at the foot of the path, a reader whose page she has who comes within five blocks of her has
  it held out to them, once.
  - After Tilly answered: once she has said her piece.
  - After Tilly came out: once Tilly has answered her, or straight after good morning if Tilly is not with her.
  - She takes it out of her folder and holds it out in her right hand, the folder against her chest in her left: "June gave me this,
    love. It's your writing, isn't it?"
  - The reader is told: "Gail takes a page out of her folder and holds it out to you. It is the page you tore out of your journal."
  - A hand on her then is a hand on the page: it opens for them to read, as any torn page is read.
  - When they have read it, or after ten seconds, she puts it back: "It'll have to go in your file." It stays in her folder.
- **The car:** after Tilly answered, Gail's car waits for the reader while she has a page of theirs to hold out to them, or holds it out.
  Otherwise, going to her puts a reader in the car, as before.
- Each reader is shown only their own page. Other readers see her hold it out and hear what she says.

## Pinned in a later hall

- **When:** once a reader's ending at the house has landed.
- **Which:** each such page of theirs that the House has not itself put up: in Gail's folder, or anywhere else.
- **Where:** pinned to a wall in the next ordinary hall they come into, one page a hall:
  - at their head height, three to twelve blocks from them;
  - on a solid block with its face clear, not beside a door, where no other page is;
  - where nobody, spectators included, can see it go up; behind them if it can be.
- **What they notice:** the pin, heard by them alone. The copy in any pocket is blank.
- **Like any pinned page:** anybody can read it; only its writer can take it down, back into their journal. It does not count among the
  six pages a reader may leave out in the House.
- **Again:** torn out again, it turns up again in the next hall. Knocked down (its wall gone), or moved by anybody, it turns up again
  too.
- If there is no such wall in that hall within a minute, it waits for the next one.

## What each reader keeps

- `PageShown`: Gail has held their page out to them.
- On the torn page itself (the journal's saved data): `Where` KEPT for a page in someone's keeping (Gail's folder), and `House` for a page
  the House put where it is.

## How it is made

- **`labyrinth.PellPages`** (new):
  - which pages: `torn`, `inFolder`;
  - Gail's folder: `folder`, `gail`, `talk`, `owed`;
  - the later hall: `arrived`, `tick`, `next`, `turnUp`, `spot`.
- **`journal.TornPages`:**
  - `Where.KEPT` and the House flag (saved as `House`), cleared whenever anybody else moves the page;
  - `keep` (into someone's keeping) and `turnUp` (pinned by the House), each a new copy of the page so that any old one is blank;
  - lookups: `pages`, `where`, `byHouse`, `current`, `writer`, `leftAs`;
  - a page with only a line in the reader's hand tears out, and the glance shows those lines.
- **`journal.JournalPage`:** `ownHand`, `ownLines`.
- **`client.JournalScreen`:** such a page's corner curls and tears (`tearable`).
- **`PellDawn`:**
  - the folder from first light;
  - Gail holding a page out at the curb, in both mornings, never in the middle of what she says to Tilly;
  - the car waits;
  - a hand on her is a hand on the page.
- **`PellPeople.SHOW`:** Gail holding a page out; its pose in `LiteraryRenderers`.
- **Hooks:** `LabyrinthDoors.arrive` calls `PellPages.arrived`; `PellAfterlife`'s tick calls `PellPages.tick`.
- **`PellTexts`:** `PAGE_HELD_OUT`, `GAIL_PAGE`, `GAIL_PAGE_FILE`.
- **`PellPeople`:** `hold`, `release` and `ground` (the lot held, above); `ensure`, `make` and the visit's beginning wait for the ground.

## Also: the lot held while anybody is at the house

Run 1578 (38d255bd), and once a local run, found Callum on the last night at the far end of the hall, where the first visit has him,
and not out on the line. The house made a second Callum there while the first stood on ground whose entities had not loaded yet: most
likely the back of the line, where the last night had just moved him, so that he dropped out of the house until it loaded.

- While anybody is at the house its whole lot, street to back fence, is kept loaded, entities and all (`PellPeople.hold`), and let go
  when the house has stood empty long enough to begin its visit again.
- Nobody is made, and no visit begins, until every part of the lot has its entities loaded (`PellPeople.ground`), so nobody is made twice
  over one who has yet to load, wherever on the lot they are.

## Decided here (any of it can move)

- Gail has the pages from first light, from wherever they are, but never takes one from in front of anybody who can see it.
- She holds out one page to each reader, once; a reader with several torn false pages is shown the first. She shows it at five blocks,
  and keeps it afterwards.
- Her words: "June gave me this, love. It's your writing, isn't it?" and "It'll have to go in your file."
- After the ending, a page turns up in the next ordinary hall, one a hall, behind the reader if it can be, and the pin is heard.
- A page the House put up does not count toward the six. Its writer can still take it back into the journal; tearing it out again starts
  it over.
- A copy of the page left in a chest stays there, blank, until somebody takes it out.

## Not yet

- Gail's third mark and the older sibling taken away (the owner's open question 2).
- A reader playing Tilly (co-op), and the kids next door.

## Checked

- `PellHouseTests.atFirstLightGailHasTheTornFalsePageInHerFolderAndHoldsItOut`. Two readers on the last night, Tilly answered.
  - The first has one of the house's lines in their own hand on a page with nothing else on it, and tears it out. The other tears out a
    page they wrote themselves.
  - At first light the false page is in Gail's folder, and the copy in the first reader's pocket is blank. The other's own page stays
    with them.
  - When Gail has said her piece and the first reader comes near, she holds the page out to them from her folder, the folder in her
    other hand, and they are told what it is. The other reader is told nothing.
  - The car waits for them while she holds it out. A hand on her opens the page for them to read.
  - Then she puts it back in her folder, keeps it, and the car takes them. Nothing is held out to the other reader.
- `PellHouseTests.afterTheHouseTheTornFalsePageTurnsUpPinnedInALaterHall`. Two readers in a closed stone hall, each with a torn page with
  one of the house's lines in their own hand on it. The first has had their ending at the house; the other has not.
  - The first's page turns up pinned to the hall's wall: behind them and out of their sight, at their head height, a few blocks off.
  - It is the House's doing and not counted among the pages they may leave. Its first lines show at a glance.
  - The pin is heard by them alone, and the copy in their pocket is blank.
  - The other's page does not turn up.
  - Anybody can read it; nobody but its writer can take it down.
  - Taken back into the journal and torn out again, it turns up again. Knocked down, it is due to turn up again.
- `PellHouseTests.theLastNightBeginsWithThePoolLitTheKnivesPutAwayAndCallumOnTheLine`, which failed in run 1578, now passes with the lot
  held; every other Pell case passes with it too.
