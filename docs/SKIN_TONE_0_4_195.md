# The drawn tone on the reader's own skin — 0.4.195

The owner, in the second document of October 30: "I'm realizing that skin tone isn't being properly applied."

## The cause
The kitchen table's drawing records a skin tone (`HouseReadKitchen.tone`). That tone was only ever used to colour a House body (`HouseBodies.worn`). A House body is worn only by readers whose own skin does not look human (0.4.163). A reader with an ordinary human skin drew their face and hands in a crayon and then kept their own colouring everywhere. Nothing they drew reached them.

## The fix
- **Server.** `HouseBodies.toned(reader)` gives a human-skinned reader who drew a tone a stand-in entry while they are inside the House: the same places, the same dimensions and the same manor exception as a body. The entry is a `Body` with `hair == HouseBodies.OWN`, so `Body.own()` is true. Only the tone travels. `HouseBodies.worn(server)` adds these entries to the bodies payload, so every client hears of every toned reader, and the existing resync on going in and out, on a read settling and on a skin changing covers it.
- **Client.** `HouseBodyClient.skin` reads the reader's own skin past the stand-in (`real`), downloads that texture and recolours it (`tonedImage`), and registers the result as a dynamic texture under the reader's House-body location. It keeps the reader's cape, elytra and slim or wide arms. `tonedImage` takes the skin's own tone to be the commonest skin colour on the lower face. Every pixel close to it in colour becomes the drawn tone plus that pixel's own difference from the base, so the painter's shading, blush and shadow survive. Hair, eyes, clothes and anything not near the face's tone are left alone. The result is cached against the source texture, so a skin that finishes loading later is toned again.
- `HouseBodyClient.wearing` is still true only for a painted House body, so the human-skinned reader's model, the tailoring checks and the inventory hiding are unchanged.

## Literature
The drawing's prose (`HouseReadProse.kitchen`) already tells that the reader drew themselves and hung the picture. It names no colour, and the House's writing never names a colour, so no line changes here. The body and face chosen in the drawing come with the next change, which writes them into the prose.

## Test
`HouseReadReaderTests.aReaderWhoseFaceIsNotHumanWearsTheBodyTheHouseReadInsideTheHouseOnly` now also restarts a human-skinned reader's TONE with a real crayon. It checks that the reader is toned inside the House with that tone and no body, that the payload sends their entry, and that outside the House they are not toned. house_read: 23/23 locally.

## What stays
- **Protocol 101, layout 51.** The Body codec already carries `hair` as a plain int.
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.** The toned skin is made at run time from the reader's own skin.
