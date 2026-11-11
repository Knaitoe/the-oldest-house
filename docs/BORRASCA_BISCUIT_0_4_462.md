# Biscuit (0.4.462)

The first half of part 5 of docs/BORRASCA_LIFE_PROPOSAL.md. The owner, 10 November: "I'd also add more active play - hide and
seek, tag, maybe the kids find an animal in the woods." And of the whole list: "Most of these should be optional."

The store's noticeboard has had "Lost: brown dog, answers to Biscuit." since it was first written. Nobody had lost one.

## What is there

On the first visit a brown dog lies under a tree a few steps east of the first marker, a chewed end of rope trailing from his
collar. He is the Corleys': the kennel behind their house has stood empty since the houses were built.

- **Found.** Patrick and Kendra come with the reader as far as the first marker. Before they say goodbye there, Kendra hears him:
  "Wait. Hear that? There's a dog in there." Patrick knows whose: "That's Biscuit. He's the one on the board at the store. He
  won't come to me." A reader who comes near without them hears him whine now and then until they have found him.
- **A first hand on him looks.** "A brown dog lies under the tree, thin, a chewed end of rope trailing from his collar. He
  watches your hand."
- **A second takes up the rope.** He gets to his feet and follows on it. The rope is the game's own lead, drawn from his collar
  to the reader's hand.
- **Let go.** A hand on him again lets the rope go. So does getting more than nine blocks ahead of him, getting on a bike, or
  leaving the town. He sits down where he is, and can be taken up again. Nothing waits on him and nothing is lost by leaving him
  where he is.
- **Home.** Led to within four blocks of the Corleys' gate, he pulls the rope out of the reader's hand, goes in at the gate and
  up the path, and lies down on the porch beside the door. "At his own gate he pulls the rope out of your hand and goes up the
  path."
- **Mrs. Corley** is on the church's lawn that day (docs/BORRASCA_SUPPER_0_4_461.md). When he goes by on the rope within ten
  blocks of her she turns to him: "Is that Biscuit? Lord, he's thin. Take him on home for me, hon. The gate's on the latch."
  Asked once he is home: "You brought him home. I'd stopped putting his bowl out."

## After

- **That day** he lies on his porch. A hand on him: "Biscuit, on his own porch. His tail thumps the boards twice."
- **The second visit.** If he was brought home he is on the porch, the same dog, and his notice is down from the store's board:
  the board's picture has a paler patch of cork and a pin hole where his sheet hung, and its first page ends "A pin hole in a
  paler square of cork, where a notice was." That page is a new original, so a reader who read the board the day before keeps
  that copy as it was. If he was not brought home he is not in the trees any more, and the notice is still up.
- **Years on** he is not there.
- **The account** has him if the reader found him: "There was a dog in the trees by the first marker, a chewed rope trailing
  from his collar. It was Biscuit, off the notice at the store." Then one of: "I led him down the street on it to his own gate."
  "I led him part of the way home." "I left him where he was."

## What he is

`BorrascaDog` is the game's wolf in body, walk and sitting, with none of a wolf's business. He hunts nothing and is angered by
nothing. He cannot be fed, tamed, bred or hurt. Nobody's lead goes on him, and when his rope is let go nothing drops: there is no
lead to be had from him. A hand on him never reaches the game's handling of a wolf or of a lead (`BorrascaDogs.touched` takes the
event first, on the client too, so a client does not see a lead come off that the server kept on).

He is drawn on the game's wolf model by his own renderer, on a skin painted from nothing at the model's own sixteen pixels to the
block (`tools/draw_borrasca_dog.py`): a brown coat with a darker saddle, a tan muzzle, chest, belly and socks, dark ears, a red
collar with a brass tag, and the end of the rope down his chest.

What is heard of him is told as a dog: his bark, whine and pant are the game's own wolf recordings under his own subtitles ("Dog
barks", "Dog whines", "Dog pants").

His way home from the gate is walked: in at the gate to the path beyond it, then up the path to the porch. If he cannot find it
within twenty seconds he is put on the porch when nobody can see him go.

## Multiplayer

Every reader's Drisking is their own, and so is their dog. He is named in that reader's record, and a dog the record does not
name, or one loaded back into a town he is no longer in, is not let back into the world (as with the townspeople since 0.4.444).
Another reader's hand does nothing to him. The game test runs two readers: one brings him home, one takes the rope and leaves.

## What was built

- `BorrascaDog` (entity `borrasca_dog`) and `BorrascaDogs` (where he is, the rope, the gate, the marker, the hand, the account).
- `BorrascaStory`: his tick each reader tick; the two lines at the marker before the farewells; leaving, returning and dying put
  a dog on the rope back under his tree.
- `BorrascaTexts.boardNoDog`, with `board(visit, noticeDown)` and `boardKey(visit, noticeDown)`.
- `BorrascaPapers.Sheet.BOARD_HOME`: the childhood board's picture without his sheet (`tools/draw_borrasca_papers.py`); it is
  the last sheet, so no other sheet's blocks move.
- `BorrascaSupper.said`: Mrs. Corley's line once he is home.
- Sounds `borrasca.dog_bark`, `borrasca.dog_whine`, `borrasca.dog_pant`.
- New builds only. No layout, protocol or Witness change. A client and server must both be 0.4.462: he is a new kind of entity.

## Checks

- `BorrascaTests#theLostDogIsFoundByTheFirstMarkerAndLedHomeOnHisRope`, with two readers: where he lies and by what; that he is
  nobody's, cannot be fed or hurt and takes no lead; the two lines at the marker, once each; a first hand looks and a second
  takes the rope; let go by a hand, and through the game's own event, and no lead falls; another reader's hand does nothing; he
  follows; a reader who gets too far ahead loses the rope; Mrs. Corley sees him go by; home at the gate, in at it and up the path
  on his own feet; the gate is the Corleys'; the account at each stage; her line once he is home; the notice up that day; a
  reader who leaves with the rope finds him back under his tree; on the second visit the one brought home is on the porch and
  the one left is gone; the board's words and its picture for each reader.
- One new page of the client proof, `dog`: six views of him on the wolf model, and his skin.
- `tools/draw_borrasca_dog.py --check` in the workflow's quick job.
- Run locally: the whole Borrasca suite, 48 of 48; the quick gate; the Borrasca client proof, 32 views.

## What I could not check

I have not played it by hand. The game test drives the server, and the proof shows his skin on the model, but nobody has walked
him down the street. In particular I have not seen the rope drawn from his collar to a child-sized reader's hand, how he takes the
steps of the trail and the turn at the gate at walking pace behind a reader, or how he looks lying down (the proof shows him
standing).

## Choices of mine

These are mine and easy to change: that he is the Corleys' dog; where he lies; that the rope is his own and not an item; that a
first hand only looks; that he is gone from the trees on the second visit if he was not brought home.
