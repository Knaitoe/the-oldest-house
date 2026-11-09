# Borrasca: the source weighed against the vignette (0.4.419)

The owner, November 9: "please review the borrasca pdf and weigh it against the existing vignette". The PDF is the whole story in
four parts, 63 pages. This note compares the story's shape with the vignette as it stands after 0.4.303, records what 0.4.419
changes, and lists the larger choices that are the owner's to make. As with every adapted source, only its shape is taken. Every
line the vignette shows is its own.

## What the vignette already shares with the story

- **A new child in a small mining town in the Ozarks** with a friend who leads, and a tree of three trunks from one root with a fort
  in it. Children cut their names into the tree by custom.
- **The legend withholds its cause.** There is a tall shining figure, a far metallic sound off the mountain, a boundary children
  don't cross, and a place at the top of the ridge called Borrasca that nobody explains. The mile markers on the trail echo the
  story's marker carved in the fort.
- **Someone is taken near the tree, and the official story is that she left.** The vignette's friend, Nell, is taken at the third
  marker. Her poster goes up and is later marked CLOSED.
- **Years later the reader comes back older** to the same tree and the same Sheriff. The same lie is told again, and the Sheriff
  knows more than he should.
- **The Shiny Gentleman is machinery on the ridge.** The story's refinery machine and the vignette's crusher both make the noise
  the children heard, and both stand where the figure stood.
- **The operation runs through generations of one family and the Sheriff's office.** In the vignette that is E., W. and J.
  Prescott in the night book, approved by the Sheriff's office on every page. The vignette's lower rooms, the unsent letter and the
  numbered stakes behind the shed keep the story's captivity and deaths off-screen and on paper.
- **Other children have gone the same way.** Dana, Eli, Wren and Mara are on the store's board over the years.

## Where the vignette departs from the story on purpose

These follow earlier owner decisions (docs/BORRASCA_FOUR_VISITS_PROPOSAL.md and docs/BORRASCA_LOWER_ROOMS_0_4_237.md), and 0.4.419
keeps them.

- **A friend is taken, not an older sibling.** The House never gives the reader a family, so the betrayal by a parent who covers it
  up stays with the Sheriff, who is nobody's father here.
- **The reader returns grown.** The story's narrator returns as a teenager.
- **Nell is not found.** The story finds the sister alive and unwilling to leave, and rescues the friend. The vignette's night book
  says "transfer, and nothing after", and no fate is invented for her.
- **Nothing is explained in a paragraph.** The story ends on a mother's letter that sets out the whole operation. The proposal
  rules that out, so the vignette's papers are records that implicate without summarising.
- **Nothing is shown happening.** No assault, no bodies in the machine, no captive displayed. The story shows all of these.
- **Names.** The approved proposal kept the story's Drisking, the Prescotts, the Triple Tree and the Shiny Gentleman. That is
  closer than other adaptations go, so it is noted here for the owner. Nothing in 0.4.419 adds another name from the story.

## What the story has that the vignette lacked

### 1. What the word means (added in 0.4.419)

In the story the mystery word turns out to be ordinary. It is an old miners' term for a mine that gives nothing back: the first
shaft in the system to run dry, and the place the operation runs from. The vignette had Nell's note ("Nobody will say what it
means. Dad says it doesn't mean anything") and never answered it.

### 2. The town's history and its debt to the Prescotts (added in 0.4.419)

In the story the mines closed after children were lost in them, and the blasting poisoned the town's water. One family stayed and
brought the town back, and the town looked away because of the work and the money. The vignette implicated the Sheriff and the
Prescotts, but the town was only a backdrop.

### 3. The tree as home in a children's game (added in 0.4.419)

In the story the children play tag around a carved tree, and a child who is caught "has to meet" the shining figure. A rhyme about
the man waiting under the tree runs through the story. The vignette's Nell plays tag but had no rhyme.

### 4. Why it is done (owner's decision)

This is the story's central reveal. The poisoned water left the town unable to have children. The operation on the mountain
supplied children to the town's families and sold others. The town knew and took them, and the friend is one of those children
herself. The vignette has no motive past fees in a night book.

Bringing this in would mean:

- the night book recording births and placements with Drisking families;
- a register in the church or the county office whose dates line up with it;
- the friend's own birth on that register;
- the content note naming the sale of children.

All of it can stay non-graphic and on paper, like the lower rooms. But it changes what the vignette is about and what the trailhead
note warns of, so it is left to the owner.

### 5. The old man who remembers it wrong (owner's decision)

Halfway through the story, the old head of the family, in a care home with dementia, mistakes the visitors for people from his own
time. In fragments, he gives away more than his son ever would. An original version would put the eldest Prescott (the "E." of 1964)
on a porch in town on the adult visits. That is a new actor, skin and lines, so it is offered rather than built.

### Not taken, with reasons

- **The rotted sign.** In the story, the legend's name for the pale figures comes from the letters left on a decayed mine sign.
  The joke belongs to that story, and the vignette never names the pale figures, so an original version has nothing to spell.
- **The friend rescued and the sister who won't leave.** This would invent Nell's fate, which the proposal rules out.
- **The explaining letter.** The proposal rules out an explanatory paragraph.
- **The beating.** The story's friend is beaten nearly to death on the page. The vignette shows no violence happening.

## What 0.4.419 changes

- **The store's framed history.** A framed page of typing is hung on the store's south wall, between the windows. On the two
  childhood visits it is too high to read ("You can't read it from down here"). On the adult visits it reads as a page from the
  town's historical society:
  - ore found in 1887;
  - the first shaft to give out was the north working at the top of the ridge, and miners had a word for a mine that gives nothing
    back;
  - in 1953 children went into the old workings and did not come out, and the county blasted the shafts shut;
  - the water ran red for two years and most families left;
  - the Prescotts stayed, took the county contract and the old depot, and brought the town back;
  - since 1961 the town has grown every year.

  It is donated by the Prescott family, and it says nothing about what the depot does.
- **The tag rhyme.** Nell's notes in the fort gain a last page: the tree is home in tag, and a rhyme of her own says that the name
  you leave where the trunks meet is how he knows you. The aged notes stain it like the rest.
- **The account.** A reader who read the frame to its end keeps a line: the page said what Borrasca meant, the town's water had run
  red for two years, and after that the town grew every year.

## Tests

`BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits` now also checks:

- the childhood notes carry the rhyme;
- a child can't open the frame;
- grown, the frame opens and names the word and the Prescotts;
- the account line appears only for a reader who read the frame.

## What stays

- **Witness:** 86 / 65 / 63; Borrasca keeps its single Understanding source and `silenced_the_mountain`.
- **Protocol:** 170.
- **Layout:** 135.
- **Content note:** unchanged.
- **New builds only.**
