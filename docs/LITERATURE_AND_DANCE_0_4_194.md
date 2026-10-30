# The writing follows the rooms, and the reader's hands in the dance — 0.4.194

## The writing (owner: "Ensure you're updating literature too. Accounts, house or leaves etc.")

Everything built in 0.4.186–0.4.193 is now written down wherever the reader reads about themselves. Every line comes from what the reader's records actually hold, never from what might have happened. As with all House writing, nothing names the reader's sex, a colour or an age.

### What happened afterwards (`AccountAfterwards`)

The Witness account adds a sentence or two after a story's own line. The rooms' own accounts use the same words, so the two never disagree.

| Story | Afterwards, as the account puts it |
|---|---|
| **Mr. Harrigan** | **Kept:** his number still sends a line now and then, and has never asked for a name.<br>**Buried:** the rot goes as far as the names given: his greeting slowed, then letters missing and the casket's smell, then the phone ringing at night to a missed call. |
| **Ted the Caver** | They can hold their breath longer under water, and the cave pulled the line back once, twice or three times. After three: "Then it let go." |
| **The floorboards** | How many times the heart sent them out before, and that it was louder each time. |
| **The Last Stop** | Once they raised a hand to her and she followed them home. Either "She still does", or on the last night they kept their hands at their sides and she let them pass. The room's account opens with this. |
| **Bly** | How often the Lady walked them to the water's edge at home, until the cupboard night. The room's account says it too. |
| **The elk carcasses** | The hides' smell came out with them, until water took it off. The room's account ends: "The smell of the hides came with me." |
| **The diner** | After tasting her meal: "What was on the plate turned over in me on the way out." Also in the room's account. |
| **The eight rooms the manor remembers** | Once the echo has begun, a line in the manor's terms: the pattern on the great room's west wall, the tree outside the front window, the door onto plaster, the bear on the stairs, the pew in the hall, the small figure, the walls that knock (and answer, if answered), and what sits behind the reader's shoulder in the night glass (once seen). |

The Usher account (0.4.186) and the Shallows account (0.4.191) already said theirs.

### The House of Leaves

Three new facts can be written onto the leaves (`StaircaseProse`, gathered in `StaircaseAccount.facts`):

| Fact | When it appears | Ledger line |
|---|---|---|
| **followed** | Something follows the reader home now: the woman from the field, the Lady from the lake, or Mr. Harrigan's calls. | "Followed home: …" |
| **remembered** | The manor has kept rooms for the reader. It names the first. | "Kept in the manor: … rooms" |
| **unwound** | The cave has taken Ted's line back. | "Line taken back: …" |

Each fact also has three phrasings in the narrators' voices, and a margin memory for the fourth leaf. Books bound after this build can carry these facts. A saved original keeps the words it was written with.

### The keepsakes' own lines

A keepsake marked for its reader now carries a line saying what it does, in the room's words:
- **The tooth:** "In your other hand it keeps your feet under you."
- **The bulb:** "It still lights for a little while, if you ask it."
- **Father's knife:** "Father wasted nothing he killed."
- **The rabbit's foot:** "Carried, things fall your way a little more often."
- **The brother's photograph:** "Kept close, it watches over yours."
- **The wax cylinders:** "Played, the voices keep the house."
- **The diary's last page:** "The last page knows the way home."
- **The register leaf:** "The dead read it too."
- **The cap:** keeps its own line and adds "Worn, the herd takes you for one of its own."

Keepsakes already given keep the text they had.

## The reader's hands in the dance (owner: "The NPCs arms look great. The players arms are still in their neutral pose")

In the shelter's slow dance, the partner's hands are at the reader's shoulders, but the reader's arms hung at their sides. Now, while one of the reader's own strangers dances within reach (`DanceClient`):
- **The reader's model** holds the partner at the waist. Arms go forward and in, the body and legs sway with the partner's own sway, and sleeves and trousers follow. It shows from behind, in a mirror, and to anyone who can see the partner.
- **In first person**, both hands come up in front, a hand's width apart, swaying with the partner. A reader missing an arm shows only the arm they have.

It is drawn on the client from the partner's synced gesture, so the server and the dance itself are unchanged.

## Tests

- **New: `KeepsakeTests.theAccountsLeavesAndKeepsakesSayWhatHappenedAfterwards`.** It checks:
  - Harrigan's freed and rotting lines;
  - Ted's, Tell-Tale's and the hides' lines;
  - the Last Stop's account opening on the night she followed;
  - Bly's account remembering the carries;
  - the Witness account carrying the manor's echo and the line taken back;
  - the tooth's line, and the cap keeping its own line and gaining one;
  - the leaves gathering "remembered" and "unwound".
- **The writing namespace** passes (6), and the native font proof renders every specimen leaf, the new facts included.
- **The hides case** now checks the count the smell decides, and the direction only when the cow's path has started. A cow in mid-hop starts its path a second later, as it does in play.

## What stays

- **Protocol and layout unchanged:** protocol 101, layout 51.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
