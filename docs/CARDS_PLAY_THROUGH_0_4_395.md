# The quiet rooms' cards play through (0.4.395)

The owner, November 8: "remove the read from my pace from the personhood vignette intro/outro text please."

## The change

- **No button on the cards.** The quiet rooms' opening and closing cards (the House's line on black as each room begins and ends) no longer offer **Read at my pace**.
- **Every card plays through on its own.**
  - Each page rises out of the black, holds long enough to read, and sinks back.
  - The card closes itself after its last page, as it did before 0.4.288.
- **The manual pages are gone with the button.** Their **Back**, **Continue** and **Done**, and the arrow keys, went with it ([READING_AND_RECONNECT_0_4_288.md](READING_AND_RECONNECT_0_4_288.md) records how they worked).
- **Nothing else changes.**
  - The words of the cards are the same.
  - Their sounds are the same.
  - The time each page holds is the same.

## Code

- `client.HouseReadLineScreen` has no widgets and no manual mode.
- A held card the server sends (`HouseReadLinePayload.manual`) is let go at once with `HouseReadCardActionPayload(token, true)`, so the server never waits on a card the client will not hold.
- The server's `HouseReadStory.cardAction` is unchanged. It only holds a card when asked, and the client no longer asks.

## Tests

`client.HouseReadLineProof` (run by the house-read client job) now checks that:

- the opening card has no buttons;
- it stays open through its pages;
- it reaches its last page on its own;
- it closes itself on the tick its time runs out.

The proof's three still images are unchanged.

## What stays

- **Witness:** 81 / 61 / two kinds / three endings.
- **Protocol:** 163. The payloads are unchanged.
- **Layout:** 130.
