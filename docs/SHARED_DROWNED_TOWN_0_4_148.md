# The drowned town's ending, shared — 0.4.148

The owner's October 28 direction: the drowned town and lake-witch scenes should mostly support several players. Proofrock already ran as one story for the whole world: the essays dried, the church key, the visits, the roof and the witch all belong to the town, not to any one reader, and several readers could already play it together. What it lacked was an account for a reader who was there when someone else opened the roof. This release adds that, as the second scene drafted for every way of being there (`docs/COOP_FOUNDATION_0_4_146.md`).

## A story that belongs to the world

The paw's story belongs to a thread. The drowned town's belongs to the world, so its ending works differently:

- **Everyone there watched it.** When a reader lets the breath go beneath the roof hatch and the roof opens, every other living player in Proofrock watched it, whichever thread they came in with. Spectators are not credited.
- **There for the essays.** A reader who was in Proofrock during the first visit, while the essays were drying, keeps a witness's account: they dried the essays, came back for the key, and stood in the drowned church while the one who opened the roof let the breath go.
- **Came later.** A reader who first came to Proofrock after the essays had dried keeps a late joiner's account: they followed the one who opened it down to the church and watched the hymn go out, and never read what the school had to say.
- **Each with the Witness credit.** Both accounts name the one who opened the roof by their player name. The actor's own account is unchanged.
- **Missed it.** A reader who comes after the ending examines the open roof hatch for its aftermath, as before.

The House notes who was in Proofrock during the first visit, on arrival and every second while they are there.

## Tests

One native case joins the co-op cases, which now run four (the quiet-room job runs 21). Two readers are in Proofrock for the essays. On the second visit a newcomer comes too, a spectator is there, and one reader is elsewhere. When the roof opens:

- the one who opened it holds an actor's account, unchanged;
- the reader there for the essays keeps a witness's account naming them;
- the newcomer keeps a late joiner's;
- nobody elsewhere and no spectator is credited;
- the open roof hatch is still the aftermath for anyone who missed it.

The existing drowned-town cases (the native furnace, door, key and roof sequence, the aftermath, the essays and the reload) pass unchanged.

No layout, protocol or Witness pool change: 54 / 41 / two kinds / three endings.
