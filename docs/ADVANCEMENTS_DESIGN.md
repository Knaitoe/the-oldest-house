# Advancements — design proposal (October 29)

The owner asked for an advancements table for the mod, in Minecraft's advancements menu. This is the proposal; nothing is built yet.

## Principles

- **Knowledge is personal.** Each reader earns their own advancements, and none is announced in chat (`announce_to_chat` is off everywhere), so nobody learns from chat which story another reader finished. The reader still gets the toast. In shared scenes, a witness earns a story's advancement exactly when the Witness record gives them that story.
- **No spoilers.** Stories, endings and the deepest places are hidden until earned. Minecraft shows the rest of the tree only as the reader reaches its neighbours, so the menu fills in as they go.
- **Nothing is given.** No experience, items or recipes as rewards. Advancements record; they never pay, so the House's finite supplies stay finite.
- **The quiet rooms and the read are never advancements.** The five quiet rooms (and the proposed self-portrait) have no stakes by design. An advancement for them would also tell the reader, and anyone looking at their menu, what the House read.
- **Written in the House's voice.**
  - The journey's lines are short and say "you".
  - A story's description is the first line of that story's account, in the reader's own "I", so the second tab reads as an index of their account.
- **Existing worlds.** On login, the House awards whatever the save already records: stories, depth, fires, endings and the rest. Testers keep their history.

## Two tabs

1. **This House Has Ghosts in It:** the journey. The way in, depth, the great staircase, the endings, and what the reader keeps or loses along the way.
2. **An Account:** the stories, under the five kinds of ending, with the progress toward the Witness.

Frames follow Minecraft's three kinds:
- **task:** an ordinary step;
- **goal:** a milestone;
- **challenge:** the hardest; the purple frame and fanfare are reserved for the endings and for hearing every room.

## Tab 1 — This House Has Ghosts in It

Background: the manor's wallpaper. Icons are proposals.

| Id | Title | Description | Parent | Frame | Hidden | Earned when | Icon |
|---|---|---|---|---|---|---|---|
| `letter` | This House Has Ghosts in It | Someone wrote to you about a house. | (root) | task | no | the opening's letter arrives | paper |
| `manor` | The manor | You went in. | `letter` | task | no | the reader first enters the House | oak door |
| `room_between` | The room between rooms | A door in the study opened onto somewhere it shouldn't. | `manor` | task | no | the reader enters the room between rooms | the study's door |
| `hallway` | The impossible hallway | It was longer inside than the house is. | `room_between` | task | no | the reader walks into the impossible hallway | a painting |
| `junction` | Past the far door | The hallway ended in a door, and the door did not end. | `hallway` | task | no | the reader crosses into the junction | iron door |
| `six_doors` | Six doors deep | Far enough in for the House to start telling stories. | `junction` | task | no | return depth 6 | written book |
| `growl` | Something below | You heard it breathe. | `six_doors` | task | yes | the reader first hears the Growl | sculk shrieker |
| `stone` | Stone | The papered walls gave way to stone. | `six_doors` | task | no | the reader first enters a stone hall (depth 12) | cobblestone |
| `twenty_doors` | Twenty doors deep | Deep enough to be offered the stairs. | `stone` | goal | no | return depth 20 | ladder |
| `camp` | The explorers' camp | Someone came this way before you and left their things. | `twenty_doors` | task | yes | the reader reaches the camp at the head of the great staircase | campfire |
| `bound` | Your own book | You bound the first leaf of your House of Leaves. | `camp` | task | yes | the reader binds their first leaf | their House of Leaves |
| `first_fire` | The first fire | Your own words, burned for light. | `bound` | task | yes | the reader lights the first hearth | flint and steel |
| `five_fires` | Five fires | The book is ash, and the stairs are lit. | `first_fire` | goal | yes | the reader lights the fifth hearth | soul lantern |
| `cell` | The cell | At the bottom, a door with bars. | `five_fires` | goal | yes | the reader reaches the Minotaur's chamber | iron bars |
| `ending_witness` | The Witness | You opened the door with both hands empty. | `cell` | challenge | yes | the peaceful release | the original weapon |
| `ending_wound` | The wound | The House came down, and you got out. | `cell` | challenge | yes | the wound and the escape from the collapse | Holloway's shield |
| `ending_locked` | Locked out | The House closed its door on you. | `cell` | task | yes | the defeat that locks the reader out | barrier |
| `stray` | A stray | Something that lives in the House let you feed it. | `manor` | task | no | the reader tames a House stray | cooked mutton |
| `together` | Not alone | You took a companion through a door with you. | `stray` | task | no | an owned companion follows the reader through a House door | lead |
| `mother` | The Mother of Strays | She keeps what you lose. | `manor` | task | yes | something of the reader's goes into her keeping | the den's shelf |
| `ransom` | Paid for | You bought something back from her. | `mother` | task | yes | the reader ransoms or trades something back | emerald |
| `chalk` | Chalk | Marks, to find your way back. | `junction` | task | no | the reader has chalk | chalk |
| `spool` | A line back | String, tied off where you came in. | `chalk` | task | no | the reader has a spool | spool |
| `letters` | Correspondence | Someone in the House has been writing to you. | `manor` | task | no | the reader reads their first House letter | letter |
| `likeness` | A likeness | The House painted you into the family. | `manor` | task | yes | the reader's first-death family portrait is painted | painting |

## Tab 2 — An Account

Background: the account's paper.

**Progress**

| Id | Title | Description | Parent | Frame | Hidden | Earned when | Icon |
|---|---|---|---|---|---|---|---|
| `account` | An Account | I am writing down what I saw, before I begin remembering something else. | (root) | task | no | the reader first receives their account | written book |
| `first_story` | Heard to the end | One room told me how it ends. | `account` | task | no | the first personal resolution | writable book |
| `two_kinds` | Another kind of ending | Not everything here ends the same way. | `first_story` | goal | no | resolutions of two kinds (the Witness needs two) | two books |
| `five_stories` | Five rooms | I have heard five rooms to the end. | `first_story` | task | no | 5 resolutions | book |
| `twenty_stories` | Twenty rooms | I have heard twenty rooms to the end. | `five_stories` | goal | no | 20 resolutions | bookshelf |
| `enough` | Enough to be heard | I have heard enough of the House to speak for it. | `twenty_stories` | goal | no | the Witness quota: 75% of the eligible pool, rounded up, across two kinds (41 of 54 today), derived like the account's | lectern |
| `every_story` | Every room | There is nothing left in the House I haven't heard to the end. | `enough` | challenge | yes | every eligible story (54 today) | enchanted book |

**The five kinds.** Each is earned with the reader's first story of that kind. They stay visible, because seeing an unearned kind teaches the two-kinds rule. Each has its stories as hidden children.

| Id | Title | Description | Stories |
|---|---|---|---|
| `survival` | What I survived | Some rooms end with me getting out. | 16 |
| `understanding` | What I understood | Some rooms end with me understanding something. | 17 |
| `memory` | What I remembered | Some rooms end with something remembered. | 10 |
| `connection` | Who answered | Some rooms end with someone answering. | 7 |
| `release` | What was let go | Some rooms end with something let go. | 4 |

**The stories.**
- The title is the story's account title, and the description is the first line of its account.
- They are hidden until earned, task frames, and earned exactly when the Witness record resolves the story for the reader, in any role it credits.
- The icon is the scene's own object: the item or block the account is read from, or that ends the scene, to be listed for review at build time.

| Kind | Id | Title | Description |
|---|---|---|---|
| understanding | `floorboards` | The floor | I took up the board. |
| survival | `hide_and_clap` | The wardrobe | The game ended at the wardrobe. |
| connection | `harrigan` | The study | Reading had given us somewhere to sit together. |
| survival | `model_home` | The window | The tree had crossed the glass. |
| understanding | `drowned_town` | The lake | I dried the school essays and returned for the key. |
| understanding | `preserved_cave` | The congregation | I walked between the people the lake had kept. |
| memory | `shallows` | The boys | I held her wrists while another boy held her feet, and we swung her out over the water on three. |
| memory | `phone_canoe` | The recording | I took the canoe over the drowned steeple. |
| survival | `goatman` | The trailer | There were more of us than there were places at the table. |
| survival | `ted_caver` | The small passage | I worked until the hole would take my shoulders. |
| understanding | `zampano_courtyard` | The readers | I coaxed the cat from the mat and read the survey behind the sealed windows. |
| connection | `whale` | Return to sender | I posted a letter and the answer was already in my hand, dated before I had asked. |
| memory | `barn_well` | The cover | I climbed below the initials. |
| memory | `plain` | The distant frame | The bird circled something beyond the dunes. |
| understanding | `hospital` | The call button | I stayed through the ward's short night. |
| survival | `holloway_camp` | The counted lights | I took supplies from the dirt hut. |
| release | `mother_of_strays` | The keeper | She let something go. |
| connection | `seance` | The empty chair | They could hear the cupboard, but they could not see my hand. |
| understanding | `wallpaper_nursery` | The pattern | I watched the shape cross the seams. |
| understanding | `hotel` | The closing account | I was in the photograph, at a party I never went to. |
| understanding | `hill_nursery` | The returned knocks | The cellar returned every knock I made. |
| memory | `miniatures` | The little rooms | I recognized the rooms on the tables. |
| release | `masque` | The last clock | I crossed seven colored rooms and put my hand on the clock. |
| understanding | `usher` | The coffin lid | I returned to the consequence of the lid I had chosen. |
| understanding | `winchester` | The upper ledger | A long stair gained one floor. |
| survival | `child_room` | The disappearing exits | I crawled below the bed after the windows and doors had gone. |
| connection | `crimson_hall` | The recorded voices | I played all three voices through their endings. |
| survival | `bly_route` | The wet boards | I waited in a shut cupboard while the drips passed. |
| survival | `elk_lot` | The looping field | The field turned back upon itself. |
| understanding | `elk_fan` | The broken light | I fixed the light and stood under the turning blades until they threw me out. |
| connection | `mapping_interior` | The crawl beyond the roof | The passage under the house ran farther than the roof. |
| understanding | `holy_rabbit` | The food in the hollow | The rabbit was alive every morning, and every night the meat came in its skin. |
| understanding | `confession` | The listener's signature | I wrote until the speaker began counting the listener. |
| survival | `elk_carcasses` | The passing boots | I held still beneath the hides. |
| survival | `costume_night` | The thirteenth witch | One witch held still whenever I looked. |
| survival | `movie_night` | The flashes over water | Each burst held her where it found her, and I rowed in the dark between them to the pier. |
| survival | `winter_lake` | The dare | I carried her ribbon out to the hole where she went down, and a hand took it. |
| survival | `camp_blood` | The service road | The essay had remembered an ordinary summer. |
| memory | `devils_rock` | The eight loose pages | I read the eight pages in order. |
| memory | `wheel` | The numbered doors | Every numbered room had measured me against her. |
| connection | `ghosts_set` | The tape and the cut | I returned to film the family. |
| release | `end_world_cabin` | The visitors at dusk | I answered the visitors for myself. |
| memory | `family_copy` | The house they remembered | They welcomed me into a house I had actually lived in, and thanked me at the door for looking after it. |
| release | `three_fingers` | The last finger | I took the paw out of the fire and wished on it. |
| connection | `lake_house` | The house in the third lake | There was a house on the bottom of the lake, and every time we went down it let us a little further in. |
| understanding | `borrasca` | The name beside mine | I came back grown. |
| understanding | `painted_interior` | The painted sun | I took a painted plank down. |
| understanding | `second_waitress` | The second apron | The second waitress became heavy enough to carry a plate. |
| survival | `tunnel_periphery` | Under the green lights | She kept pace without running. |
| memory | `missing_sleep` | The empty mattress | The bed was empty during the missing hours. |
| understanding | `layered_man` | The cellar shift | The knife broke against the next wet layer. |
| survival | `motion_light` | The unhinged door | The floodlight went out while I watched the courtyard. |
| survival | `backseat` | Twenty seconds | I closed the car door against the rain. |
| memory | `silo` | Yellow dust | I climbed above the corn while a voice below invented places she could have gone. |

The tables count 25 journey advancements and 66 for the account: 7 for progress, 5 for the kinds and 54 stories. A new vignette that joins the Witness pool adds one story advancement, and the pool's derived counts follow it.

## How it would be built

- **One table, generated.**
  - A tool (`tools/generate_advancements.py`) writes the advancement JSON under `data/the_oldest_house/advancement/house/` and `.../account/`, and the titles and descriptions into the language file, from one table.
  - The story rows come from `WitnessAccount.Story`, so the titles always match the account.
  - A check fails the build if a story has no advancement, or if any advancement announces to chat or gives a reward.
- **Awarded by the House.**
  - Most criteria are `minecraft:impossible`, awarded from the events that already record these facts:
    - the opening sequence;
    - the doors and the return depth;
    - the staircase's binding, fires and chamber;
    - the finale's phases;
    - the companions;
    - the Mother's keeping;
    - the correspondence;
    - the first-death portrait;
    - the Witness record.
  - Chalk and the spool use Minecraft's own `inventory_changed`.
- **Login sync.** Joining awards whatever the save already holds, so existing readers' menus show their history.
- **No new protocol:** advancements travel in Minecraft's own packets.

## Tests

- Every eligible story has an advancement whose title is its account title.
- Every advancement is off for chat and gives no reward.
- The hidden flags match the tables.
- Resolving a story awards its story, its kind and the matching progress steps. A witness's credit does the same, and a peer's act does not.
- Login sync awards a saved history once.
- No quiet-room act, and no read, awards anything.

## Open questions for the owner

1. **Two tabs, or one?** One tab would hold about ninety entries.
2. **Locked out:** keep it as a hidden advancement, or leave defeat out of the menu?
3. **Story descriptions** in the account's first-person "I" (the menu as an index of the account), or in the House's "you" like the first tab?
4. **The five kinds visible** (teaching the two-kinds rule), or hidden until earned?
5. **Anything missing.** For example the Red Room, the Goatman's haunting following a reader home, or a first death in the House.
