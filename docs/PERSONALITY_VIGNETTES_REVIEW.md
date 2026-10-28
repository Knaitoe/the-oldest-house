# Review: the personality vignettes — October 28

The owner asked for a review of the architecture and design of the personality vignettes: the five quiet rooms (waiting room, bathroom, coatroom, shelter, porch), the House read record, and the six tailored moments that read it. This review was written against the code at 0.4.150 and `docs/HOUSE_READ_DESIGN.md`.

## The short version

The rooms are well built as private, safe, literal sets. Each reader gets their own copy of each room. Every figure is bound to that reader and every sound is private. Borrowed coats and objects are lent with exact custody and returned on leaving. The notes write themselves only when unwatched, nothing in the rooms can hurt anyone, and the rooms chain from the junction as asked.

What the rooms are for does not exist yet. They log what the reader did as sentences that nothing reads. The category record knows only two of the five traits, and nothing writes to it. So every reader reads as unknown and all six tailored moments stay neutral. Today the House has five pleasant scenes and a diary, which is the state the design document says is not worth building.

The most valuable next step is the classifier for `HAIR` and `SELF`, whose six consumers already exist, followed by the personal account opening with the read. That is the design's own first slice, and it is the test of whether the idea shows.

## What is built well

- **Private copies, cleanly allocated.**
  - Each reader gets an index. Their five rooms sit at fixed offsets from the House origin, built asynchronously in bounded slices with chunk tickets, and resumable from a saved cursor.
  - Over 4,096 readers, the House stops offering the rooms rather than failing.
- **Custody is careful.**
  - Coats, boots, the phone, the paper, the glasses and the cone are loans tagged to their reader.
  - The reader's own items are saved before a loan replaces them and restored when the reader leaves.
  - Loans dropped on the floor are cleaned up, and a swapped loan never becomes a second take-home item.
- **The notes behave as designed.** Each note shows its blank side first. It gains its writing only when the reader is not looking, with a gaze check that sees through glass and bars, so the change is never seen happening.
- **Harmless and gentle.**
  - Damage is cancelled inside the rooms.
  - The coatroom's cold lifts when the reader dresses.
  - The shelter's figures stop short of the reader and never follow, speak or touch.
- **The read side is safe by default.** `HouseRead.category` returns only tentative or settled values, so every consumer falls back to today's text, and the `address` helper never uses a pronoun.
- **Tests** cover building, custody, notes, the chain and the six consumers, using fixture categories.

## Architecture findings

1. **Two stores with no bridge.**
   - Each room's visit keeps free-text actions such as "lifted the curl cream from position 2", up to 64 per room (`house_read_rooms_04138`).
   - The category record (`house_read/<reader>`) holds only `HAIR` and `SELF`.
   - Nothing maps the first to the second. A classifier built on the sentences would have to parse prose, and changing a sentence would silently change a read.
   - *Recommendation:* when an act happens, also save a structured signal (trait, value, first or second, tick, inside the window or not). Keep the sentences for the account.
2. **"The first instinct counts" is not captured.**
   - No room defines when its window opens and closes, or marks which act came first.
   - The porch's 200 ticks after the whistle, for example, is not a window in code: an act after it is recorded like one inside it.
   - *Recommendation:* give each room its window state and mark the first deliberate act within it.
3. **Observations the design relies on are not recorded:**
   - **Coatroom:** standing at the mirror for two seconds after dressing.
   - **Shelter:**
     - the share of gaze and time each figure gets (the 8° framing, the 65% share);
     - the second signal, staying beside a figure until the song ends; only the fact of having stood beside them is kept.
   - **Porch:**
     - looking through the lit window and then not going in;
     - which of the three ways on the reader took (kitchen, side gate or road). The way out is the strongest `HOME` signal, and leaving by it records nothing today.
   - **Waiting room:** keeping the paper, glasses or phone in hand until the door clicks.
   - **Bathroom:** the hot tap settling Set D alone.
4. **The trait list is incomplete.**
   - `HouseRead.Trait` has `HAIR` and `SELF` only.
   - Without `AGE`, the shelter's strangers cannot match the reader's age.
   - Without `HOME`, authority figures cannot read it, and nothing can read `DRAWN_TO` either.
5. **Per-tick cost grows with every past reader.**
   - The room tick runs for every online player every tick. Inside a room it reads and saves that reader's whole saved state each tick.
   - `inside` recomputes five room positions and state lookups several times a tick.
   - `contains`, which the House boundary check calls each tick for players in the House, walks every reader who ever had a room, times five rooms.
   - This is fine for a few players. On a long-running server it grows with everyone who ever played.
   - *Recommendation:* cache each online player's active room when they enter and leave, and save a visit only when it changes.
6. **Data disclosure.** The design promises the README will say plainly that the save keeps these observations and categories. The README does not say so yet, and the observations are already being kept.

## Design findings

1. **The bar is not met yet.** The design sets four conditions for the idea to be worth building: the House of Leaves written with the read, the account opening with it, every trait changing something noticeable, and two friends finishing with different books. None holds while nothing writes categories.
2. **Minecraft affordances bias the waiting room.**
   - The paper, glasses, magazines and phone sit seven to nine blocks from the door. The TV and cooler (`TRANSITIONAL`) stand at the back, about thirteen blocks away.
   - Most readers click whatever they reach first, so the read would skew away from `TRANSITIONAL`.
   - *Recommendation:* equalise the distances or shuffle the sides per reader, as the bathroom and coatroom already do.
3. **The coatroom's "one from each side".**
   - The build shuffles three coats across three pegs, which removes the bias.
   - But "left" and "right" no longer exist, so the `ANDROGYNOUS` rule for taking one of each needs restating: two different coats, or the barn jacket.
   - Boots are a separate garment. The design should say whether boots signal anything, or only the first coat.
4. **The porch's three ways on are the read.** Recording which one the reader takes (finding 3) costs little and is worth more than any other porch signal.
5. **`DRAWN_TO` is the most sensitive thing the House infers.** The design already keeps it private and out of the playtest log. Consider a server option to turn trait inference off, or `DRAWN_TO` alone, alongside the README disclosure.
6. **Co-op after the chain.**
   - The rooms are rightly private, so two friends who come through the junction together each walk their own five rooms.
   - Each comes out into the hall their own junction door was dealt, so friends may not meet again until chance brings them together.
   - *Recommendation:* hold the junction door's hall for the group, as `GroupCrossings` holds a vignette door, so friends who started together come out into the same hall.

## The reflection

The owner asked for the reflection in the coatroom glass to be reviewed with the rest. The design says the House never redraws the reader: the glass shows their own skin, dressed in what they borrowed, and only in glass and copies.

What it did well:
- It shows the reader's own skin, standard or slim, with the coat and boots they actually borrowed fitted over it.
- A turned or covered glass shows nothing.
- A spectator sees nothing, and only the reader is ever in their own copy of the room.

What was wrong:
1. **It was a portrait, not a reflection.**
   - It was always drawn full height, from the floor nearly to the top of the frame, centred and facing out, wherever the reader stood.
   - It was not reversed left for right.
   - Only the head tilted. The figure never turned when the reader turned, and never moved its limbs when they walked.
2. **It showed from anywhere,** including from behind the glass or far off to one side.
3. **The design's second signal was not recorded:** standing at the glass for two seconds after dressing.

Fixed in 0.4.150:
- **Reflected.** The reader is drawn as they stand and reflected through the glass's plane: reversed left for right, and pressed into the glass as before.
- **The size and place a mirror gives.** A plane mirror shows anyone at half their height, whatever the distance, so the figure is half the reader's height on the glass.
  - Its feet fall halfway between the reader's eye and the floor, and it sits opposite the reader along the glass, within the frame.
  - Crouching lowers it.
- **It moves with them.** It turns with the reader's body, its head follows their look, its limbs swing as they walk, and crouching and riding poses carry over.
- **Only from in front.** It shows within twelve blocks of the glass, and not from far off to the side.
- **The House keeps the look:** two seconds at the uncovered, unturned glass in the borrowed layers, looking into it, is recorded as "stood at the hall glass in the borrowed coat".

Still to come:
- **`UNSEEN` hides the face** in every mirror and double. The reader's client does not know their read, which stays on the server. A private message carrying only a reader's own read to their own client would be needed once the classifier writes one.
- **The reading glasses for `ANALOG`,** once `AGE` exists.
- **The same reflection in later mirrors.**
- **Glass facing other than south.** The coatroom's faces south, and the renderer still assumes it.

## Suggested order

1. Structured signals, room windows and the classifier for `HAIR` and `SELF`. Their consumers exist, so this is the first slice the design describes.
2. The account opening that recites the reader's habits back to them.
3. The missing observations, starting with the porch's way out and the coatroom mirror.
4. `AGE`, `DRAWN_TO` and `HOME` in the trait list, with the shelter's strangers following `AGE`.
5. The per-tick caching, the README disclosure and the inference option.
6. The waiting room's layout, and regrouping friends after the chain.

Only the reflection fixes above came with this review, in 0.4.150. The rest are proposals.
