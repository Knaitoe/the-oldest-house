# The cabin bargain (0.4.51)

Owner-requested rework of the Cabin at the End of the World (`END_WORLD_CABIN`, slot 66). The visitors no longer ask for an object or a pet. They ask for the reader's body, in a fixed order, and every gift is permanent. Either answer resolves the story.

Status: verified. Verified source 9c8cf8bda4c92894ac195b79942cb955fcd0714c, run 37945073729: all 394 native gameplay tests (including the three cabin cases), the literary, hotel, hallway, seam, staircase, multiplayer-story and exploration suites, the two-client live expedition, the native client proofs (including the one-arm model and armour proof and the three new keepsake meshes) and all seventy-one architecture views passed.

## 0.4.52: the cabin, waking, the door and the storm

Playtest repairs and rework, at the owner's request. Status: released. Verified source 0eeb65fd1a970c66144b44080ce101ebba06fe42, run 37959728692: all 394 native gameplay tests, every focused suite, the two-client live expedition, the native client proofs and all seventy-one architecture views passed. Its multiplayer-story job first ran out of Java heap while the staircase leak test set up its world, before any of that test's players joined, and passed on its single re-run; the commit before passed the same job unchanged. The rework before the revision passed every job on a039e9315fd0418653873f4ff5090d0e753f16df, run 37954228601: all 394 native gameplay tests, every focused suite, the two-client live expedition, the native client proofs and all seventy-one architecture views.

- **Owner revision: foreshadowing, not instructions.** A vignette is a puzzle: the reader should not know what is coming, though some foresight is welcome.
  - **The source.** The visitors' own letter, which laid out the whole bargain, is gone. In its place is the cabin's guest book: three entries by earlier guests. Four strangers on the beach at sundown, a knock nobody answered and wet prints going out along the jetty with none coming back, and a last undated entry: "They said it had to be my choice, and it was. I kept everything I came with."
  - **The asks.** Leonard asks for the reader's *life*; Adriane says only "More." Neither names a heart. The heart is only what it costs, and the reader learns that by giving. Sabrina still asks for the arm, but no longer says she will walk into the lake on a refusal.
  - **The menu.** Giving reads "Give Leonard your life" (or Adriane, or Sabrina's arm) with one line: "There is no taking it back." Refusing carries no lore at all.
  - **The account.** It now tells what was asked and what it cost: "Leonard asked for my life, and I gave it. It cost a heart."
  - **Waking.** "Someone has left a guest book by the beds."

- **Waking.** Every arrival begins inside: the screen goes black and the reader is laid in the first free bed of two in the bedroom. They sleep for three seconds (a native bed, so the bed view and getting up are the game's own), then wake: "You wake in a bed that is not yours." With both beds taken, a third reader wakes standing beside them. Sleeping here never sets a respawn point. A NeoForge `CanContinueSleepingEvent` listener, registered reflectively because the event's package moved between versions, keeps the reader asleep for their moment even by day.
- **The knock.** Once awake, the reader hears three slow knocks at the front door every eight seconds. Opening it answers them (so does speaking to them, or stepping out within six blocks of Leonard and seeing him). The visitors must be answered again on each visit; the speech resumes where it stopped. Three wait on the porch, clear of the door, and Redmond waits on the grass.
- **Pacing.** Each line waits 3 to 7.5 seconds by its length, plus 1.5 seconds when the speaker changes. A speech starts two seconds after the door opens, and the next asker waits 4.5 seconds after a gift.
- **The account.** It now lies on the dining table, where the visitors say it is. When it is readable, the reader is told "It lies on the dining table, by the window", and it glimmers for that reader alone until read. The guest book (the source) stands by the beds. Reading any literary account before its source now says so ("This account answers 'Guest book'. Read that first"); before, it silently did not count, which is why the playtest's account gave no globe.
- **The cabin.** The shell is the same: 25 by 23, the slab roof, the porch. Inside it is rebuilt:
  - a bedroom with two beds under the lake window, a lamp table and a chest of drawers, behind its own door;
  - a kitchen with a stove, sink, counter, shelf and table;
  - a living room with the television on its cabinet, an armchair, sofa and footstool facing it, a brick hearth with a mantel, and the dining table;
  - eight framed windows on every side (the lake, the porch, both woods), with open shutters outside.
- **The way out.** A small spruce shed now stands at the edge of the yard, at the end of a trodden path, with a window, a workbench and a lantern. Its door leads in, and the House's door is at its back: it is the only way out. The visitors' walk to the lake now leaves the porch by its steps.
- **The storm.** It is still the reader's alone, and now unmistakable:
  - **Rain.** Custom slanted rain streaks fall wherever the sky is open, splashing where they land, up to eighty per tick at full strength (reduced by the particle setting).
  - **Leaves.** Wind-torn leaves are driven across the yard once the storm is past a third.
  - **Lightning.** Real lightning bolts strike the lake and woods, client-only, so they have no fire, no damage and nobody else sees them. The game draws them, flashes the sky and plays its crack and thunder. They come every few seconds at the height of the storm, and a near one strikes at each gift and each visitor going under.
  - **Wind.** A heavy looping gale (fourteen seconds, seamless) runs under everything at the storm's strength, with gusts swelling over it.
  - **The rest.** The vanilla rain, the darkened sky and fog all remain.
- **Saved worlds.** Layout 37. Worlds at layouts 32 to 36 carve the cabin again, last and only when it is empty and loaded (`ElkUpgrade.rebuilds(place, version)`, generalized from the elk scene). Displaced animals and items go to the porch. Every personal record, the visitors' identities and the readers' answers are kept.

## The sequence

Four visitors wait on the grass on either side of the path to the porch steps, facing the door. They are **private to each reader**: every reader has their own four (actor keys `Visitor0..3_<reader>`), visible and audible only to that reader. The earlier shared strangers (`Stranger0..3`) are discarded the first time they load.

| Order | Visitor | Asks for | Permanent effect |
| --- | --- | --- | --- |
| 1 | Leonard | The reader's life (it costs one heart) | −2 maximum health |
| 2 | Adriane | "More." (another heart) | −2 maximum health (−4 in all) |
| 3 | Sabrina, a nurse | The arm of the off hand ("something… handy") | No off hand, ever |
| — | Redmond | Nothing; he speaks for "our" world | — |

- **Meeting.** Coming within eight blocks of Leonard and seeing him starts his speech. A reader who goes inside first hears knocking at the door every fifteen seconds until they come out.
- **The words.** The visitors speak to the reader by name, from real facts:
  - deaths ("You've died 4 times and got up again. That costs you nothing, so it doesn't count.");
  - the bed the reader made and its biome;
  - days lived;
  - nights slept.

  They begin with *your* world ending ("The water came up over the bed you made in the plains"). Adriane and Redmond turn it toward *theirs* ("I have a boy. Where we come from, the sea is already in the streets"). Redmond's last word after the sacrifice keeps it open: "It was ours, you know. The world. We let you think it was yours."
- **Answering.** Speaking to any visitor (or touching the television) during an ask opens a native nine-slot dialogue. It offers three choices:
  - give;
  - refuse (since the owner revision, with no lore);
  - not yet.

  Since the owner revision, a gift says only "There is no taking it back." The arm is asked for twice: the second dialogue, "Are you sure?", opens on the next tick. "Not yet" and leaving are always allowed. Hearts already given stay given, and the reader resumes at the next ask on a later visit.
- **No silent failures.** Every refused action says why:
  - asking early ("Let them finish.");
  - not yet met ("Go out to them.");
  - already answered;
  - an answer given twice.
- **The storm.** It is the reader's own weather, sent by `CabinStormPayload`:
  - calm before the meeting;
  - rising at each speech and each ask (18 → 32 → 46 → 60 → 74 → 88);
  - 100 through the arm;
  - breaking to clear at the waking.

  A refusal leaves it heavy (76). The client renders vanilla rain, splashes and rain sounds, and darkens the sky and fog through two client-only mixins:
  - `Level.getRainLevel` and `getThunderLevel`, for the reader's client level only;
  - `Biome.hasPrecipitation`, on the render thread only.

  The outside pockets stand in the void biome, so they otherwise never rain. On top of that it adds:
  - lightning flashes, which honour *Hide Lightning Flashes*;
  - delayed thunder;
  - a synthesized wind-and-rain cue;
  - a slight wind sway that scales with *Screen Effects*.

  The television picture is tinted by the same storm, with rain running down it.

## The arm

A held scene (`CabinBargain.held`), about eleven seconds:

1. The reader kneels and Sabrina stands in front of them.
2. The view is turned to her. Movement and jumping are held by transient modifiers. Doors, pearls and mounts are locked through `LiteraryVignettes.retreatLocked`.
3. The cord is tightened above the elbow (tick 22).
4. Leonard reassures them (52).
5. "Breathe in." (78)
6. **The blow (90).**
   - A wet chop that nearby players hear.
   - A burst of dark red.
   - A violent camera jolt and a red flash.
   - Then black, with wet sounds, ringing and Sabrina's voice in captions over the dark.
7. The reader comes to lying on the boards (150 to 170), the storm gone.
8. They are released at 230, with nausea and a minute of weakness.

The arm is committed at consent (`BodyLoss.takeArm`), so a logout cannot avoid it. It is shown to every viewer at the blow (`BodyLoss.reveal`). A reader who logs out mid-scene finishes it on their next login.

## The body, afterwards (`BodyLoss`)

- **Saved per player.** The state lives in `cabin_body_0451` (`Hearts`, `Arm`, times). It is permanent and personal, with no cure.
- **Hearts.** A permanent `MAX_HEALTH` modifier `the_oldest_house:cabin_hearts` of −2 per heart. It is reapplied idempotently on login, clone, respawn and dimension change. Health never exceeds what is left.
- **No off hand.**
  - Anything placed there goes back into the pack, or to the reader's feet; it is never destroyed.
  - The swap-hands key is cancelled ("There is no other hand to pass it to.").
  - `Player.getMainArm` returns the kept arm whatever the handedness option later says (common mixin `OneArmMixin`).
- **Drawn without it, for every viewer.** `BodyLossPayload` syncs the missing side to all clients. The player model's arm and sleeve are not drawn, in third person, first person and the two-handed map. A zero scale carries into armour copied from the pose, so no chestplate sleeve hangs in the air (proved natively in `LiteraryVisualProof.oneArm`). A stump layer draws the shoulder from the reader's own skin and sleeve pixels, wound in stained linen.
- **The keeper holds the arm.** "<name>'s left arm" (`cabin_given_arm`) is sealed in the Mother's collection, never returned, and stands on her shelves. It is kept for later use.
- **Phantom pain.** Rare (at most once per thirty minutes of the reader's own play, then by chance) and contextual. Lines include:
  - underwater, "The water closes around a hand you left at the cabin.";
  - in rain;
  - on ladders;
  - holding a shield;
  - at low health;
  - inside the House ("a shelf is holding your left hand");
  - at night in the Overworld, "The stump itches where Sabrina tied the cord.".

  Readers who gave only hearts get heartbeat lines.

## Refusing

Refusing at any ask is an answer. The player must be willing to sacrifice all, so stopping at the arm is a refusal too.

- **The one who asked walks into the lake.** The route runs east around the cabin, to the shore and down the jetty, and off its end (`ROUTE`, a pure function of time). The walker follows the ground and steps off. There is a splash and a flash for the reader, and the walker sinks and is gone for good.
- **One room of the reader's own goes dark, for them alone.** It is saved in `literary_cabin_closures_0451`. The dealer, doors and `canDeal` honour it through `LiteraryCabinChoices.closed`, and everyone else still gets the room. `personalClosure` chooses, in order:
  1. the unfinished story room the reader most recently walked out of;
  2. else an unfinished built room they have not reached;
  3. else, only when nothing is left unfinished, a finished one.

  It never chooses:
  - Holloway's camp (the finale's shield route);
  - the cabin;
  - a family copy;
  - a room holding one of the reader's saved ways back or their own animals;
  - the elk lot while the elk fan it opens is unfinished;
  - a room that would leave the account short of thirty-three across two kinds.
- **Nothing already given comes back.**

## The television

`CabinScreen` takes 24 by 16 pictures without loading anything synchronously. Chunks are ticketed (`the_oldest_house_cabin_screen`). The picture is taken once they stand, or after thirty seconds from whatever stands, through a loaded-only `BlockGetter` and a voxel walk. Tickets are released either way.

- **During the asks: home.** The reader's home (their Overworld bed, else the world spawn) is shown from high above as a map draws it, with the bed lit. It is darkened by the storm.
- **After a sacrifice:** the same home, clear.
- **After a refusal:** the closed room, from just inside its door, until its lights go out (150 ticks).

The reader is told "The television inside is showing something. Watch it." Watching it for the established 200 ticks makes the account readable.

## Resolution and the globes

Both answers resolve the same Witness source with the same id, kind and account entry. The outcomes are `gave_two_hearts_and_an_arm` and `refused_the_visitors_after_<n>_gifts`. The pool is unchanged: forty-three sources / thirty-three resolutions / two kinds / three endings. The account is titled "An account of the visitors". It is written from what happened and names the closed room in prose (`StaircaseProse.place`), never by internal id.

The globes mirror each other. Each works only for the reader it was left for ("Only for the one it was left for").

| | Whole snow globe (sacrifice) | Cracked snow globe (refusal) |
| --- | --- | --- |
| Inside | Snow over a cabin, a lake and a jetty | The same, leaking, a third of it dry |
| Shaken | Snow drifts; tells whether it has settled | Barely moves; tells whether water is left |
| On a death | Once each in-game day, the keeper survives at one heart. Two seconds of full resistance and ten of fire resistance, then the snow must settle | Once, ever, the same; then it is dry for good |
| Never | Against the void or a kill command, inside a committed finale, or for anyone else | The same |

The rationale: the reader who gave two hearts and an arm loses a lot. Those losses are a quarter of their health, the shield-and-sword and totem-in-the-off-hand habits, and torchlight in the other hand. A world that did not end keeps them, renewably. The reader who kept everything gets one more fall of snow.

## Earlier answers and saved worlds

- **A reader who answered before 0.4.51** (`ChoiceMade` without `Bargain0451`) keeps their answer, screen and account exactly. Their screen is captured asynchronously if it was missing. They still receive the jar.
- **The world-wide closure of 0.4.36** is migrated once (`Personal0451`). It becomes the personal closure of its stored `Chooser`, and the room reopens for everyone else. The earlier value is kept as `WorldRetired0436`. The 0.4.37 safety repair still runs first.
- **Readers mid-choice** before the update meet the visitors.
- **Geometry is unchanged.** The visitors stand on the grass off the porch, so the cane chair at (4,0,-10) is no longer occupied. The layout stays 36.
- **Protocol 35** (two new payloads: `body_loss`, `cabin_storm`).

## Other places that needed an off hand

- **The staircase hearths.** A one-armed reader holds their House of Leaves in the hand they kept and strikes a flint and steel (Tom's lighter included) from the pack.
- **The collapse.** With flint and steel in hand, a one-armed reader burns loose paper from the pack; never a book or a keepsake. Without paper they are told so.
- **No change needed:**
  - the church key (either hand);
  - the Witness ending and the shallows (empty hands);
  - Holloway's shield (it works in the main hand);
  - the Minotaur fight (shield and weapon alternate in one hand).

## Assets

`tools/generate_cabin_bargain_assets.py` makes the following; no samples, recordings or words:

- nine original mono cues, every one subtitled:
  - knock;
  - stumbling heart;
  - cord;
  - blade through bone;
  - wet;
  - ringing;
  - storm;
  - globe;
  - cracked globe;
- the two globe meshes (translucent glass over a tiny cabin);
- the given arm;
- the 32×16 bandage.

## Verification

Three literary cases:

- the fixed order, each permanent loss and a reapplied heart modifier;
- the sealed off hand, the cancelled swap and the kept main arm;
- the keeper's sealed arm and a peer untouched;
- the held scene and its end;
- the walk;
- the home picture and the reading;
- the whole globe's daily catch;
- a refusal after one gift that closes only the reader's own room and keeps the heart;
- the walker;
- the room picture going dark;
- the cracked globe's single catch;
- the migration of an earlier world closure to its chooser across a reload.

Plus:

- the native one-arm model and armour proof;
- the three new keepsake meshes;
- the asset packaging checks;
- the complete suite.
