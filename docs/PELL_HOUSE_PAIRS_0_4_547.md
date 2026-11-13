# The Pell house 25: a house to each pair (0.4.547)

The owner, 13 November: "Ensure you're using our previously established system for private rooms. Remember, the house should generate a
second instance for a third and fourth player, etc? Also ensure you're adjusting the accounts to reflect accordingly. Please use your own
judgement."

The proposal already said "Pairs are saved, as at the lake house ... A third reader gets a house of their own" and "Copies: a copy per pair
(the scene-copy system)". This slice builds that, on the scene copies every private scene uses (`docs/SCENE_COPIES_0_4_153.md`,
`SceneInstances`) and in pairs as the lake house's are (`docs/LAKE_HOUSE_PAIRS_0_4_156.md`).

The proposal also gave a third or fourth reader near a pair a part as the kids next door, on the far side of the back fence. With a house
to each pair there is nobody left over to play them, so they are not built (the proposal is updated to say so).

## Who goes into which house

- **The first reader** takes a house nobody keeps: the original first, then the copies in order.
- **The second** joins them. A reader joins a reader who:
  - is in a house of theirs now, or went through its door in the last ten seconds and has not arrived yet, so that friends who walk
    through together are never split;
  - is the only living reader there (spectators are nobody);
  - still has the house to finish (it can be dealt to them, and their account is not ready);
  - has nobody with them yet, or whoever was has had their ending.
- **A third reader** finds both places in the house taken and gets a house of their own. While it is made ready, their door stays shut:
  "Someone is already in The Pell house. Another is being made ready for you. Try the door again in a moment."
- **A fourth** joins the third, **a fifth** has a house of their own again, and so on, up to the server's `sceneCopies` (eight by
  default); past that, the door opens onto a hall ("It will wait for you").
- **Pairs are saved,** both ways round. Unlike the lake house's diver and partner, neither's story is the other's: each of the two goes
  back into the pair's house on every visit, with the other there or not, until their own ending. Nobody else can join either of them
  while the other's story is under way. Somebody new may join a reader whose pair has had their ending.
- **A reader whose ending has landed** comes back to the house as its aftermath (`PellAftermath`) only in a house nobody is in and
  nobody keeps for a story still under way: their own first, else any such house; with none, their door opens onto a hall. Another
  house seen so is not made theirs: the house they keep is still the one they were at. A pair's own house is never held from them by
  somebody seeing its aftermath, and a house stands as its aftermath only while nobody unfinished is at it, as before.
- **Made ready ahead:** another house is built in the background only when there are more readers on the server than the houses standing
  (or being made) hold, two to each. Two friends alone on a server never cause a second house to be built.
- **Who plays whom is each house's own.** The cane hangs on the hook in every house, and whoever takes it in a house is that house's Tilly.
  With nobody taking it, both readers are older siblings, as before.
- An operator's test door still opens onto the original.

## Each house is its own

- Everything the house keeps is its own, as it always was for one house: June, Tilly, Callum and Gail, which visit it is and the hour, the
  line, the pool, the freezer, the tapes, the cane on its hook. Readers in another house are never part of it: not counted at the table,
  not told of the cane, not seen by Callum.
- A copy stands a million blocks west of the House, built by the house's own builder in bounded slices, as every copy is. The House knows
  it for the Pell house from where it stands.

## The accounts

- **Who was with whom.** The House's thread of a shared story (who came through it with whom, `docs/COOP_FOUNDATION_0_4_146.md`) is now
  joined only by readers in the same copy of a scene. Before, a reader arriving in a copy could join the thread of a reader in the
  original, and their accounts would name each other. This holds for every scene with copies that friends share: the Pell house, Bike
  night's towns and the lake house's lakes.
- **The account book** (`PellTexts.account`, `tillyAccount`) names the reader's own pair:
  - an older sibling whose house had another older sibling: "June Pell took us in: Tilly, <name> and me.", and among June's rules,
    "<name> and I were in the garage, for our privacy.";
  - the reader as Tilly: "June gave <name> the garage across the yard.";
  - in an older sibling's account the reader who took the cane is Tilly, and is not named a second time.
  - Each reader's record keeps who they were at the house with (`With0547`), by name, so the account is the same whether or not the other
    is online when it is read.
- **The Witness book** closes the house's line on the reader's own pair, by their part in it (`CoopAccounts.PELL`, `PellTexts.company`),
  in place of "<name> was in that street too.":
  - the reader as Tilly: "<name> was the older one, and had the garage across the yard.";
  - an older sibling: "<name> took the cane off the hook, and was Tilly." for the reader who played her, and "<name> had the garage
    with me." for another older sibling.
  - Where a record has nothing to go on: "<names> was (were) at the house on Wren Close with me."

## How it is made

- **`SceneInstances`:** the Pell house has copies. `pell` is its own choice before the usual one: a reader goes into their pair's house; a
  reader with nobody joins a reader alone in a house of theirs (`alone`), counting one through its door in the last `ENTERING_TICKS`
  (200); a reader whose ending has landed goes to their own house or another nobody is in or keeps. Only a reader with the house still to
  finish holds a house from its pair (`held`). Pairs are saved both ways round under `Pairs`
  (`siblingOf`, and `pairOf(data, place, reader)` for either the lake house or the Pell house). `occupied` leaves out the reader's pair at
  either scene. `ahead` builds the next house only for more readers than the houses hold, two to each.
- **`VignetteThreads`:** a reader's presence is by copy (`where`): a reader joins only the thread of somebody in the same copy as them.
- **`SharedVignettes.Together`:** a coda may be written from the reader's own record (a default method; every other scene's is as
  before). `WitnessAccount` asks for it so.
- **`CoopAccounts.PELL`**, **`PellTexts.company`**, **`PellTexts.others`**: the Witness close and the account lines.
- **`PellHouse.met`:** each reader tick, whoever else is at the reader's house with them, both with the house still to finish, goes into
  their record (`With0547`).

## Decided here (any of it can move)

- The kids next door are not built: the third and fourth readers have a house of their own.
- A pair's two readers are equals: either goes back into the pair's house without the other.
- Ten seconds for a friend through the door behind to count as arriving together.
- Another house is made ready only for more readers on the server than the houses hold, two to each.
- Somebody new may join a reader whose pair has had their ending.
- A reader whose ending has landed sees the aftermath only in a house nobody is in or keeps: their own first, else any; never a pair's.
- The Witness close names the reader's pair by their part in the house rather than "in that street".

## Checked

- One new case (`PellHouseTests.aThirdAndFourthReaderHaveAHouseOfTheirOwnAndEachAccountNamesItsOwnPair`, batch `pell_pairs`), five readers
  coming to the door one after another:
  - the first takes the house; the second, through the door behind them before they have arrived, joins them, and each is the other's
    pair; the third's door waits while a second house is made ready;
  - once it stands, the third's door opens onto it (its own entry); the fourth joins the third; the fifth waits for a house of their own;
  - each reader's house is the one they are at; the House knows the second house for the Pell house; it is built as the first (its screen
    door and letterbox);
  - each pair shares one story, and the two houses do not share one;
  - each house has its own people, its own June among them;
  - each reader's record has their own pair and nobody from the other house; with the first house's first reader as Tilly, Tilly's account
    names who had the garage, the older sibling's names nobody else as taken in, and the second house's two older siblings are taken in
    together and shared the garage; no account names anybody from the other house;
  - a reader whose ending has landed, coming back while their pair's story is still under way, is sent into neither house, and still
    keeps their own; their pair goes back into theirs;
  - read, each Witness line closes on the reader's own pair, by their part: "<name> was the older one ...", "<name> took the cane off the
    hook, and was Tilly.", "<name> had the garage with me." both ways; no line names the other house or says "in that street";
  - once the pair's other half has had their ending too and gone, the first sees the aftermath in their own house.
- Every earlier Pell case unchanged, and the co-op cases' check that every shared scene writes a whole sentence naming its party.
