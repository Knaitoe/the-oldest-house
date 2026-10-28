# The Goatman's campers — 0.4.150

The owner's October 28 direction: the Goatman can be shared, with extra players taking the place of other campers, so the headcount stays the same as solo. The count, seating, supper and dialogue must treat each player as that cousin. The owner's later note: with several readers, one of them resets the generator instead of the cousin who goes for gas, and their return to the camper still sends the Goatman child running out past them; the others can fetch the condiments too.

Several readers could already play one evening together, but each reader was one more child: two readers made nine places at the table, three made ten. Since 0.4.146 a group at the Goatman's door was dealt a shared scene instead. This release lets friends go in together and keeps the camp the size it is for one reader.

## A cousin's place

- **The first reader is the latecomer,** as before: they take nobody's place.
- **Every reader who arrives with them,** before anyone reaches the camp, takes the place of one cousin. That cousin is not at the camp. A cousin already standing there goes only while nobody, spectators included, is looking at them.
- **The second reader takes the place of the cousin who goes for gas,** and the job with it (below). Every reader after that takes another cousin's place: never the thing or the two who carry the jars. Of the rest, the cousin whose fear keeps them in their seat goes first, then the one who paces; the cousin who covers their ears goes last.
- **The headcount stays as it is for one reader.** The pan, the plates, the frank packs, the chairs and the bunks are counted for the readers and the cousins still staged, so three readers make the same eight places one reader makes. The puzzle is unchanged: one more child at the table than there are places.
- **The reader is that cousin.** They eat from that cousin's plate, sit in their chair, and sleep in their bunk; a cousin who is there keeps their own bunk. The cousins' talk at supper and through the night skips the lines of a cousin who is not there.
- **A reader who comes once the camp is reached** is one more child, as before. So is every reader once all the places that can be taken are taken.
- **Saved evenings.** Who stands in for whom is saved with the round. Rounds begun before 0.4.150, and rounds made old-style by a fixture, have no stand-ins.

Each reader still sits out the night for themselves, and each who does resolves the story as before. Nobody is a witness here: a reader who opens the door to it, or is carried off from their bed, fails the night for themselves only.

## The one who goes for gas

- **Sent out at the first plate.** Where a lone reader's evening has the older cousin say the generator is out of gas, a cousin now says it to the gas reader by name: the can is in the shed across the yard, fill it and pull the cord, we'll save yours. If the gas reader is not there at that moment, the cousin goes after all, as he would for one reader, and the reader is one more child when they come back.
- **Out of the trailer, they are away.** The one who doesn't belong goes in to their chair once they are outdoors and clear of the door, as it went in to the cousin's. The door is pulled to behind them as before. A reminder comes now and then while the generator still needs filling.
- **The generator.** It is the one in the shed. Each use is a pull of the cord: it coughs and dies on the first two, and catches on the third once the thing has sat down in their chair (or after ten seconds away, if it never does). Then they head back.
- **The door is not theirs to open from outside.** At the shut door they knock, and somebody inside has to open it. Knocking counts as the first knock on the door, as the cousin's did.
- **Their return sends the thing out.** When the door is open and they are close to it, or step inside, the one in their chair runs out past them to the fire, as it ran past the cousin. Once they are in, the quiet and the night follow as before.
- **Left outside.** If nobody opens the door in time, or they are still out at the shed when the woods go quiet, they are gone: carried home, haunted, with the face of the cousin whose place they had. The night is wrong for everyone, as it was when the cousin was lost.
- **How the cousins speak of them** (owner): "he" or "she" only when the coatroom has settled how the reader presents (a masculine or feminine read), saved when they are sent out; otherwise by name and "they": "Save <name>'s place. They'll be back." The two fear lines that ask after the cousin who went for gas ("Didn't he already get back?", "If he's out there, who came back in?") follow the same rule. Nothing writes the coatroom's reads yet (the owner deferred the classifier), so for now every gas reader is named and "they"; the lines change by themselves once it does.

## The condiments

- With several readers the cousins say "Somebody grab the ketchup and mustard from the cooler. And the jars," and give the readers about ten seconds before going themselves.
- Once the frank packs are all out, a reader who uses the cooler takes a pair of bottles nobody has gone for: the ketchup and mustard, or the jars.
- A bottle in hand goes to its own place on the table when the reader uses it within reach.
- A cousin never goes for a pair a reader took, and the cousin who carries the jars no longer waits on a ketchup and mustard a reader set out.

## Bedtime with several readers

- **Whose bed it comes to.** When the thing has got in and the readers are in their bunks, it comes first to the latecomer's bed, then to the others from the bunk farthest from the door inward. Each visit is that reader's own: it crawls there unseen, springs up at the foot of the bed, and that reader alone goes black and is carried home.
- **A friend who stays up** is never chosen. They hear it laugh, they can see it spring up at a friend's bunk, and then the bunk is empty. Once it has finished with every bed, morning comes for them too; it got in, so they take it home.
- **The bedtime talk** is for a group: "Nobody's going out there. We stay in here, all of us. Everybody take a bunk." In the morning the cousin waiting on the trail says "you all walk so slow".

## Friends go in together

The Goatman is no longer held back: friends through the same door arrive in the same evening, and a group at its door keeps it. Hide-and-clap, with its one blindfold, is still held.

## The account

A reader who sat the night out with others keeps a Witness line that says so: "I came up the trail late with <names>." The gas reader adds "I was the one who went out across the yard to fill the generator while the others ate." Any other reader who had a cousin's place adds "and the cousins had kept my place at the table." The rest of the line follows, in the plural where it was everyone: "We kept it closed until the knocking stopped." A reader alone, and every reader who finished before 0.4.150, keeps the line they had.

The trailer status command (`/ghosthouse test goatman status` and its alias) now also says how many readers are in cousins' places.

## Tests

- **New native cases.**
  - Three readers come in together and a fourth once the camp is reached. The first takes nobody's place; the second takes the place of the cousin who goes for gas, with the job; the third another, never the thing, a carrier or the cousin who covers their ears. Those two cousins are not at the camp, and the camp has eight places, as for one reader. A reader cannot lie down in the bunk of a cousin who is there, and the reader in a cousin's place sleeps in that cousin's bunk. The fourth takes nobody's place and makes a ninth. The Witness line names who came along, says whose place was kept or who went for gas, and is unchanged for a reader alone.
  - **The generator.** The first plate sends the gas reader out, not a cousin. Out at the shed they are away. The cord coughs twice, and once more while the thing has not sat down in their chair; then it catches. At the shut door they knock and it stays shut until the reader inside opens it. As they come back, the thing runs out past them, and they are back in with no cousin ever sent for gas.
  - **Left outside.** A gas reader still at the shed when the woods go quiet is gone, carried home and haunted, while the reader inside stays.
  - **How they are spoken of.** A gas reader the coatroom has read as presenting feminine is "she" in the cousins' lines and the fear line about getting back; a gas reader with no settled read is named and "they".
  - **Bed order.** With three readers in their bunks, the latecomer's bed comes first, then the bunk farthest from the door; a reader awake is never chosen.
  - **The condiments.** Once the packs are out, a reader takes the ketchup and mustard from the cooler; the cousins leave that pair and wait a while for the jars. The reader sets both bottles out, and after the wait a cousin goes for the jars without waiting on the reader's pair.
- **Updated cases.** The two-reader supper and operator plating cases now count eight places and two frank packs. The fear-roles and bedside cases, which are about every cousin, enrol readers who came after the camp was reached. The co-op mixed-group case now checks that a group at the Goatman's door keeps it, and uses hide-and-clap for a held scene. The Backseat private-road case stands its peer back from the door, so the pair is not a group crossing.
- **Live proof.** The two actual clients cross one door into the trailer together; exactly one takes the place of the cousin who goes for gas, that cousin goes from the camp, and both clients see seven cousins, six of them afraid through the night.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
