# The list, part 6c: the field house (0.4.725)

The last part of night three (`docs/THE_LIST_PLAN.md`, part 6): whoever is left on the list hides together in the field house, with the
field's lights on. In skeleton. No protocol, layout or Witness change.

## Where they are

On night three the ones left on the list do not go out to their own places. They stand together in the field house's locker room,
south of the bench, and both of its doors are bolted behind them: the back door onto the field (both leaves) and the street door onto
Main Street. The school has the field's lights on, so the field and the ground between it and the field house are lit; the one who went
in is in light there, and they are seen further off.

At ten o'clock the one who went in is told, after the hook: "Over at the school, the field's lights are on."

## Getting in

A bolted steel door gives at the fifth blow, as on night two. Once one of them hears or sees the one who went in, they scream, and the
rest run with them.

## Where they hide

Each has two hiding places inside the field house, a way to the first and from there to the second, as on the other nights:

| Who | First | Second |
|---|---|---|
| Deek | the far end of the showers | the corner by the coach's wall |
| Wes | the coach's office, through the gap in the bench | along the north lockers |
| Mel | the south-west corner | along the back wall |
| Lesley | the north-west corner, through the gap in the bench | back through it, by the bench |
| Shannon | the showers | the lockers on the west wall |

Found, they fight back and run on to the second; found there, they cower. Torn loose from the hook, they run their second way back to
the first. Everything else about the hunt is as on the other nights.

## First light

The lights go out, the doors are put back, and the five who are left go back to their own places.

## Single and multiplayer

- **Alone**, the field house is the one who went in's to break into.
- **With others**, the bolted doors and the lights are the town's, seen by everyone; readers on Amber's side are told nothing new.
- **Spectators** are not heard or seen by the five.

## Not built yet

The banks of floodlights on the field's towers still look dark: the light is there, but a lit bank needs its own art and is left for the
polish in part 9.

## Made as

- **`TheListHuddle`** (new): `AT` (where each stands), `STREET_DOOR`, `WAYS` and `BACK` (their two ways inside and back), `DOORS`, `huddled`,
  `hidingPlace`, `bolt`, `LIGHTS`, `lights` and `lit`. The field's lights are light blocks six up over the field, every seven blocks across
  and eight along, and a row between the field and the field house.
- **`TheListHunt`**: each runner keeps tonight's ways (its own, or the field house's on night three); on night three the field house's
  doors are bolted when the hunt begins, and the lights are on for that hunt only; `doors` puts back the street door too.
- **`TheListPeople.spot`**: on night three the five are at the field house.
- **`TheListNights.begin`**: the cue on night three. **`TheListTexts`**: `FIELD_LIT`. No new sound, block or art.

## Tests

`TheListTests.theFieldHouse` (new, batch `the_list_field_house`): night three begun, the reader is told the field's lights are on; each
of the five stands in the locker room; the three doors are bolted and shut, and the lights are on. Five blows open the back door; the
reader in the doorway is heard and every one of them runs to their first hiding place in the field house. At first light the lights are
out, they are back at their places and night three is over. The skeleton case (`toTheEnd`) now holds the field house's ground
as well.

## Every new line

Grey cue on the action bar, to the one who went in at the start of night three: "Over at the school, the field's lights are on."
