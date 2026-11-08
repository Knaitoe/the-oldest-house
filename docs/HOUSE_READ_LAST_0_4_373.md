# The House's read: the last leftovers (0.4.373)

The read's remaining leftovers (`docs/HOUSE_READ_0_4_159.md`, "Not built yet"):
- the silk wrap on a sleeping double;
- the two open proposals: Tom and Kate as grown names, and the House of Leaves narrator following `AGE`.

- **Witness:** unchanged, 81 / 61 / two kinds / three endings.
- **Layout:** 122. **Protocol:** 163, unchanged.
- **New assets:** none. The wrap is the bathroom's own silk headwrap style.

## The silk wrap on a double asleep

The sleeping double now exists: Second Person's resident is the reader's House body, drawn with the reader's look, and at night it lies down in the flat's bed. The design waited for a double like this.

- **When it shows:** a double of the reader lying asleep wears the silk headwrap for a reader whose hair the House reads as wrapped for the night (tentative is enough, as for the look's other small things).
- **Otherwise:**
  - awake, the double wears what the bathroom glass kept, as before;
  - for anyone else, it wears what the glass kept even asleep.
- **Who sees it:** only the reader's own client. The look is never sent to another client, and the live model never wears it.

The rule is `HouseReadLookClient.style`, and the drawing is `HouseReadHair.SILK_WRAP`. Any double that lies down follows it, including The Ones Already Inside's sleeping doubles when they are built.

## The narrator follows `AGE`

`StaircaseProse.voice` turns the drawn voice by age. When the House knows the reader's age (tentative is enough), the voice of their House of Leaves follows it:
- **`ANALOG`:** the ledger.
- **`TRANSITIONAL`:** the letter.
- **`DIGITAL`:** the witness or the survey, whichever of the two the book's seed leans to.

With no age read the voice is drawn from the seed as before, so a book with no read is exactly today's. A book already written keeps its words; only new books follow the read.

## Tom and Kate

Decided: the shelter strangers grow up with the reader.
- **For `DIGITAL` readers and readers with no read:** Tommy and Katie.
- **For `TRANSITIONAL` and `ANALOG` readers:** Tom and Kate.

Nothing is built for this, because nothing the House writes names them yet. The shelter keeps them unnamed, and the account calls them *the boy by the rail* and *the girl with the bottle*, or *the man* and *the woman* for older readers. The names are the rule for a later room that needs them.

One caution for that room: Devil's Rock already has its own Tommy and Kate, the Sanderson children. A room that names the shelter strangers should not land in the same night as Devil's Rock without a line that keeps them apart.

## Tests

- **The house-read book case** now checks the narrator:
  - with no age read the voice is the seed's;
  - `ANALOG` gives a ledger and `TRANSITIONAL` a letter;
  - `DIGITAL` gives a witness or a survey, the seed's own when it drew one of those.
- **The client hair check** checks that the silk wrap shows only for a double asleep with a wrapped read.

The house-read job still counts 44 cases.
