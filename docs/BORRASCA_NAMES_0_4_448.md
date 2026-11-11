# Borrasca: the names on the Triple Tree (0.4.448)

The owner, of the trunk beside the carving (note 17 of docs/BORRASCA_PLAYTEST_2026_11_10.md): "The tree is old. So is the town
and its secrets. There should be names all over this tree."

## What is on the Tree now

- **Names on every face.** The burl the three trunks part from, the two courses of it above that, the east trunk as high as a
  child on another's shoulders can reach, the thick of that trunk at its foot, and all three trunks where they pass through the
  fort are cut with names: two to a face, on every side of every block. There are about a hundred such blocks.
- **Three ages.** A name cut within living memory is tan, the wood weathered: GUS, EARL, KURT, PAM and the rest are people who
  are grown now and still in town. An older one is a dark groove the bark has nearly closed over. And here and there is a flat
  patch, gone grey, where a name was taken off.
- **No name is on the Tree twice.** The old ones: RUTH, CAL, OTIS, MAE, ROY, IDA, CLEM, FAY, WES, DOT, AMOS, BO, NORA, HAL, LOU,
  VERN, ETTA, JUNE, ABE, MAC, OPAL, SID, ROSA, T+L, E+W, R+M. The grown ones: GUS, EARL, KURT, RAY, DEB, LYLE, PAM, DON, TODD,
  KIM, RICK, LORI, MIKE, J+S, C+D, BETH, DALE.
- **Patrick's and Kendra's**, beside the reader's own carving: PAT and KEN, cut this year, the wood still pale. By the second
  visit his is a flat pale patch. By the third hers is too. Years later both patches have gone grey. A hand on the block says
  what is there: "PAT. KEN. Cut this year. The wood is still pale." / "KEN, and over it a flat pale patch the length of a short
  name." / "Two flat pale patches, one over the other." / "Two flat patches, one over the other, gone grey."
- **A hand on the old names** finds one of: "Names, and older names under them. Some are only grooves now." "The bark has closed
  over most of these. A few can still be read." "Every one cut with a different knife." "Among the names, flat grey patches
  where a name was taken off."
- **The carving itself is as it was**: DANA, JESS + M and LEE, then the friend's name and the reader's, in the game's own
  letters. On the second visit its line now says the scraped name is "beside them" (it said "lower down the trunk"). A second
  carving used to be set two blocks along on the second visit, with a gouge and the same three old names again; the block there
  is now the one with Patrick's and Kendra's names.

## How it is made

`BorrascaNamesBlock` is a block of bark with a `face` from 0 to 23. Faces 0 to 19 are the old names; a block takes its face from
where it stands, so no two side by side agree. Faces 20 to 23 are the block beside the carving at each visit
(`BorrascaNamesBlock.friends(visit)`), set by the story when a visit's scenery is laid.

`tools/draw_borrasca_names.py` draws the twenty-four pictures (sixteen pixels square, clear but for what is cut, in a three by five
alphabet), the models and the states. A model is the game's own oak bark, used in place and not copied, with a picture a hair
proud of each of its four sides, a different one on each. `--check` verifies them; the quick gate and the build run it.

Each reader's hill is their own, so the block follows that reader's visit and nobody else's.

## Tests and proof

- `BorrascaTests.theTownFortAndDepotAreRealBuildingsAndTheTownsPapersChangeBetweenVisits`: at least sixty blocks of the Tree are
  cut with names; no two side by side on the burl's face carry the same; the carving keeps its place with their two names beside
  it; a hand on either is answered; the block reads both gone by the third visit and long gone by the fourth.
- The Borrasca client proof has a nineteenth view, `native-borrasca-names.png`: the burl's face as it is built, and the block
  beside the carving at each of the four visits.
