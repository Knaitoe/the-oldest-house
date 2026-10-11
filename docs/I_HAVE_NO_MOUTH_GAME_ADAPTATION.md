# I Have No Mouth: five circuits through one door

Status: design and feasibility review, not implemented by 0.4.82. This replaces the earlier short-story-shaped “The Kept” proposal as the direction under discussion. The owner asked about adapting the computer game's critical scenes, playing as its characters, and clarified the progression: no selection hub; the same door takes a reader to the next character only after they complete the prior circuit.

## Feasibility

Feasible as a substantial episodic expansion. Five condensed character chapters and a final AM circuit fit the mod's form. Recreating the complete original game's dialogue trees, every inventory puzzle and every alternate solution would be a much larger project. The expensive part is meaningful branching, reliable save/resume and convincing character behavior, rather than the ability to build the locations.

The game manual describes five protagonists, personal trials, choices that affect their standing and a final outcome shaped by those choices. The adaptation should preserve that structure. The owner’s door replaces character selection; it does not turn the characters into NPCs the reader merely follows.

Proposed scope: three or four essential spaces, two or three consequential interactions and one complete resolution per character; approximately 15–25 minutes per circuit as a design target, not a measured duration. AM remains present throughout, with an authored final confrontation after the fifth chapter. This is several updates' work, with a complete playable first chapter proving the approach before the other four are built.

## The door and personal progression

Suggested order: Gorrister, Ellen, Benny, Nimdok, Ted, then AM's interior. This order is a proposal; the sequential progression is the owner's decision.

- A new reader first crosses as Gorrister. Their name in dialogue, first-person hands, character body, memories and available actions belong to that role while inside.
- Leaving early, disconnecting or dying never advances the circuit. A return resumes the same character at the saved checkpoint. A failed attempt can restart its unresolved segment without trapping the reader behind an unrecoverable puzzle.
- Completion means reaching an authored character closure through the necessary personal actions. Merely entering, collecting a prop, watching a peer or leaving is insufficient.
- Both a compassionate closure and a deliberately different valid closure can finish a circuit. Store what the reader actually chose. Progression should not require guessing a hidden “perfect” answer five times; the consequences matter in AM's final circuit.
- On the next crossing after completion, the same door leads to the next character. Do not jump directly into the next chapter while the player is still reading the prior account.
- After Ted, the next entry becomes the AM finale. A final outcome then closes this literary source; the door can offer the finished account or a deliberate replay without erasing the original outcome.
- Store character, checkpoint, local puzzle facts, unique item custody, completed circuits and their outcomes per reader. Shared world clocks, another reader's character and group exits cannot advance that record.

The role is a temporary performance of a fictional character. Keep the player's real profile, permanent skin, native stats and other House stories intact. The character's backstory must never be presented as an accusation about the real player.

## Essential playable scenes

The following is a condensed adaptation plan, not a claim that the original game has only these rooms or solutions. New dialogue and purpose-built Minecraft environments carry the narrative.

| Circuit | Spaces to retain | What the player does | What its closure must establish |
| --- | --- | --- | --- |
| Gorrister | Desert zeppelin, exposed mooring/heart scene, roadside diner, the family revelation | Explore the damaged airship, recover a physical heart through a tethered climb, question the people in the diner, reconcile contradictory evidence | Decide how to respond to guilt and a desire to stop living; the airship's final departure follows the choice |
| Ellen | Yellow mechanical pyramid, temple chambers, computer/elevator chamber, sarcophagus | Reassemble a device, operate the computer, confront an implied traumatic memory, use her engineering to interrupt AM's trap | Recover agency through an action the reader performs; panic changes the experience without replacing the puzzle with an arbitrary lockout |
| Benny | Starving village, orchard/food trap, the child's shelter, sacrificial altar | Experience hunger through the role, discover why food does not solve the situation, build a relationship with the child, choose what to do when the ritual begins | Concern for someone other than himself must be actionable; a sacrifice or refusal has a saved personal result |
| Nimdok | Camp entrance, ward and records, laboratory, golem/prisoner escape | Read evidence of his own involvement, question witnesses, refuse or follow an order, operate the laboratory and direct the golem | Recognition and responsibility require a consequential action; freeing people cannot be replaced by choosing an “apology” dialogue option |
| Ted | Castle approach, Ellen's sickroom, servant/witch rooms, chapel and final bargain | Test contradictory claims, navigate tempting bargains, obtain the means to help Ellen, choose whom to trust | Trust and care are resolved through conduct, rather than a quest marker identifying the truthful NPC |
| AM | A changing machine interior, three conflicting voices, the last decision | Use the consequences and knowledge retained from all five circuits to undermine, bargain with or submit to AM | Give a complete authored ending shaped by this reader's circuits; any body transformation remains scoped to the fiction by default |

Ellen's trauma and Nimdok's historical atrocity belong to their character stories. Convey them through testimony, architecture and aftermath; the playable choices concern recovery or complicity. The chapters need restraint and convincing writing to carry their significance.

## What the existing code already supplies

| Existing system | Use in the adaptation | New work still needed |
| --- | --- | --- |
| `LiteraryVignettes`, its private papers and saved personal state | Chapters, dialogue menus, personal source/ending custody and earned outcomes | One saved circuit controller and explicit completion conditions |
| `LiteraryCopies` and owner-bound actors | Independent readers inhabiting the same fiction | Generalize private instances beyond the two current copy scenes; retain each chapter's exact puzzles and finite props |
| Sliced builds and append-only route slots | One new door destination without rebuilding old worlds | New chapter plans and private instance leases; any new layout/protocol bump belongs to the implementation release |
| Native return routes, hotel custody and transition cleanup | Temporary role inventory and restoration of the player's originals | Transactional role custody across every entry, retreat, death and reconnect |
| Owned attribute modifiers and child/one-arm presentation | Character-specific movement, body and first-person presentation | A role renderer and hand textures with complete restoration; avoid changing the real account skin |
| Priority subtitles, spatial sounds and private screen effects | AM interrupts local dialogue, environmental manipulation and character fear | A scripted AM voice/channel, subtitled audio and bespoke effects; no live AI service is required |
| Native game tests, actual connected-client expedition and GPU proofs | Personal progression, state persistence, reachable scenes and visual review | New full-circuit cases and two readers at different chapters |

Use private physical instances for puzzles whose doors or geometry change. A client-hidden actor alone does not make a shared door or container private. Spectators can observe allowed scenery but must not complete another reader's role. Keep owner-bound story items inert for peers and keep finite original custody across retries.

## Art and sound

Five temporary protagonist appearances and first-person hand/outfit textures; distinct airship/diner, machine-temple, village, camp/laboratory and castle materials; circuit-specific interactive props; an AM screen language and machine interior. Design these for the native UVs and Minecraft scale. Use the existing diner, machinery, ladders, foliage, ward fixtures, furniture and book renderers where they fit, with new textures and meshes where the scenes need them. Original writing, textures and sound should be authored for the mod.

AM's lines hold priority over local conversation and must reach the active reader even when its current screen/actor is outside the camera. Use perceptible environmental cues as well as subtitles. Fear builds from the role's actual scene events; it is not present simply because a character has spawned.

## First complete implementation

Start with Gorrister: one door, one private airship/diner circuit, the full role-entry/restore transaction, a persistent checkpoint, a real final choice and the completed-circuit handoff. Initially the next chapter can remain unavailable until its playable release; never register unfinished chapters as eligible Witness sources.

Before expanding, verify normal play and replay, two readers at different checkpoints, interruptions during inventory custody, loss/reissue of a story prop without duplication, entity identity, reachable tether/heart interactions, priority AM subtitles and exact restoration on exit/death/logout. Require the current complete suite and exact-source client proofs as for every release.

Then add Ellen, Benny, Nimdok and Ted on the same controller, followed by the AM finale. Treat the complete six-circuit work as one literary Witness source, with the earned final account as its resolution, rather than counting each character as a separate source. Until it is fully playable the current 44/33 quota remains. If released into the present pool as one new source, 45 eligible stories require 34 resolutions across at least two kinds; calculate this from the actual pool at release.

## References and design boundary

- [Publisher-distributed game manual](https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/245390/manuals/IHNMAIMS%20Manual.pdf?t=1738280421): protagonist roles, personal missions, spiritual standing and consequential endings.
- [Nightdive's game description](https://nightdivestudios.com/i-have-no-mouth-and-i-must-scream/): five separate character environments and psychological/moral play.
- [Original Prima strategy/hint book, ISBN 0-7615-0359-5](https://www.mocagh.org/miscgame/nomouth-hintbook.pdf): original chapter and location structure. The archive's complete PDF was unavailable to this review's reader; its indexed contents and chapter references were available. Detailed puzzle equivalence should be checked against the actual game before implementation.

Room budgets, order, timings, checkpoint behavior, condensed actions, restoration rules and Witness integration above are proposed mod design. The door's completion-gated sequential roles are the owner's instructions.
