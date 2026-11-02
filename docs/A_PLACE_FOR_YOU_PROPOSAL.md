# A Place for You (the owner's proposal)

Recorded verbatim, 2 November 2026: the owner's own choose-your-own-adventure proposal, sent while the Second Person draft
(docs/SECOND_PERSON_PROPOSAL.md) was being written, with the instruction not to review it until that draft was done. Not
built. Nothing below is edited; the owner's line breaks and headings are kept as sent.

---

A Place for You

An intensive personhood vignette for This House Has Ghosts in It

Original story for This House Has Ghosts in It • Design proposal • 2 November 2026 • Not implemented

A private, continuous evening of domestic drama and horror, built around the House's existing personhood rooms. Target: 50–75 minutes for a first visit, with 25–40 minutes possible for a decisive reader. Four ending families, independent character outcomes, and a persistent aftermath.

The player arrives at a household that has prepared for their return. Its residents know small things the player actually did in the quiet rooms. They have also built a family role around those observations. One resident wants the player to stay. Another is secretly packing to leave. Two people who seem familiar have plans that extend beyond the player.

The household's last good evening is being kept intact. Making room for someone's future will disturb it.

1. The emotional center

The central relationship is between Mara, who keeps the house, and Kit, an adult member of the household who is trying to leave it. The reader can become a mediator, an accomplice, a confidant, a guest, a substitute for somebody missing, or somebody who decides this is more than they can take on.

Mara has lost her brother, Theo. She describes him through ordinary omissions: the errand he never finished, the dish she still makes too much of, his keys in the wrong drawer. She has gradually accepted an arrangement with this part of the House: keep the evening familiar, and nobody else has to disappear from her life.

Kit has an acceptance letter and a packed bag. The opportunity may or may not still exist beyond the House. Kit wants to go and find out.

Neither wants an abstract philosophical victory. Mara wants another person in the kitchen tomorrow. Kit wants to be allowed to have a tomorrow somewhere else.

The player has not canonically caused this family's loss. Any claimed relationship to them is a claim made inside the vignette. The player may refuse that relationship and still care about these people.

The original premise

The story starts with the House's personhood rooms: it has observed a few real habits and choices, then mistaken those fragments for a complete life. The household recognizes things the player actually did, but turns that recognition into a history and a role the player never agreed to.

Mara's grief, Kit's departure, the returning strangers and the repeated evening are original conflicts that put that assigned role under pressure. The central horror is being offered belonging by a house that keeps confusing a person with the place it has prepared for them.

The cast, domestic history, supernatural rules and endings belong to this independent story. It requires no source text and has no plot, lore or completion dependency on a literary adaptation elsewhere in the mod. The House's existing personhood observations are its shared input.

2. What the personhood rooms actually change

Grounded in the existing HouseRead, HouseBodies, HouseReadProse and HOUSE_READ_DESIGN sources, checked against repository head 4f28bcf8 on 2 November.

The existing traits are AGE, HAIR, SELF, DRAWN_TO, HOME and TONE. DANGER is a value of HOME, not a separate active trait. The age categories currently produce young-adult, middle and older House bodies; DIGITAL must not be interpreted here as a child.

The vignette takes a snapshot of permitted observations on first entry. It does not silently reclassify the player's global personhood record.

Source

Concrete effect in this story

A consequential choice

Waiting room / AGE

The household's expectation of the reader, its familiar technology and the kind of help offered. A settled read changes relational expectations; a tentative read changes props only.

Accept an offered place, correct the assumption, or help somebody as a guest.

Bathroom / HAIR

A bathroom prepared with the exact routine item used, and an optional scene of practical care.

Let someone help, ask them to sit nearby, or finish the routine privately.

Coatroom / SELF

Clothing, the treatment of the hall glass, NPC forms of address and how the household expects the reader to appear in its pictures.

Accept the picture, change how the room presents you, or refuse to be put in it.

Shelter / DRAWN_TO

Which returning stranger feels immediately familiar and where opportunities for closeness begin.

Make a new bond, keep it friendship, question the supposed past, or ask to be alone.

Porch / HOME

The first approach to the house, the emotional pressure at the threshold, the first confidant and which route is easiest to notice.

Come inside, keep an exit open, remain on the porch, or continue along the road.

Kitchen drawing / TONE and drawn body

A faithful copy of the player's drawing and House appearance in the household's pictures.

Keep it, move it, cover it or take it down.

The callbacks begin with literal acts. If the player used a particular comb, that comb is there. If they turned the glass away before dressing, the hall glass already faces the wallpaper. If they stayed by the lantern, somebody remembers seeing the lantern in the rain.

Nobody announces a category or diagnoses the player. The House's account never states the reader's gender, age or skin colour. Appearance supplies recognition, not a judgement about trustworthiness, danger or deserving affection.

Confidence and an incorrect read

Unknown, uncertain, malformed or disabled reads get the fully written neutral version.

Tentative reads supply a small detail, not a major relationship claim.

Settled reads may alter address and an offered family role.

The player's present answer overrides the household's claim within this story.

No main route or ending requires a trait.

Opting out of inference also suppresses callbacks to the saved observations it would otherwise use.

The cast never interprets a refusal as proof that the House's original inference was secretly right.

An unread player is welcomed as an expected guest. Their choices during the evening establish their relationships. This is a complete version of the story.

AGE: a change in the direction of care

Settled read

The place the household offers

Its uncomfortable assumption

DIGITAL

Someone returning after beginning an independent life.

“Your room is still here” becomes a reason the reader should never leave again.

TRANSITIONAL

Someone expected to manage the household's competing needs.

Everyone assumes the reader will keep the peace, arrange things and absorb the cost.

ANALOG

Someone whose presence has become part of the household's routine.

People begin making decisions for the reader in the name of looking after them.

No settled read

A guest invited to stay the night.

The household tries to persuade a stranger to become familiar.

No version changes the player's movement, strength, intelligence or ability to solve a puzzle. Any familial term is an NPC's offered relationship and may be corrected.

HAIR: care is something offered

The late-evening bathroom contains the actual routine object from the quiet room. Mara or a trusted peer notices it.

The scene has three equally substantial responses:

Ask for help.

Ask for company while doing it yourself.

Ask for privacy.

The helper respects the answer. What matters later is whether someone remembered that answer. A care scene becomes frightening only when the House repeats a gesture after the person who offered it has stopped, left, or been told no.

There are no racialized personality assumptions or special plot penalties attached to a routine.

SELF: recognition and the right to refuse it

Settled presentation changes the coat hung up for the reader, ordinary address and the expected pose in the household picture. It does not determine temperament, authority, competence or which relationship the reader should want.

For UNSEEN, the family photograph leaves the reader's face indistinct and the hall glass is covered. One person can still recognize the reader through an action: a hand resting on a chair back, the way they fold a cloth, the routine they chose.

Every reader can cover or uncover a mirror now. UNSEEN is not an exclusive escape ability.

DRAWN_TO: familiarity is an invitation

Alex and Ren are writing labels for the two returning pavilion strangers. Use their established names if the live project has named them; this proposal does not authorize renaming existing characters.

Both are adults at an age appropriate to the reader's adult House body. Their personalities and intentions remain their own.

MEN or WOMEN: the appropriate stranger has the first familiar gesture and the first opportunity for romantic interest.

BOTH: both can be approached, with separate conversations and separate consent. They do not compete for the player.

NO_ONE: they offer equally developed friendship and practical companionship.

Unknown or disabled: both begin as acquaintances. The player can express a preference during this story.

A settled read never establishes an existing romance, a marriage, consent, or a debt of affection. “I didn't mean it that way” makes a friendship route, not a failed route. Friendship and solitude receive the same amount of writing and access to the resolutions.

3. The household

Mara

The host and keeper of the evening. Quick with practical kindness, reluctant to ask directly for company, capable of using a genuine need to make somebody else feel responsible for it.

She has fixed the loose handle, warmed the plate and prepared the spare room. She has also unpacked Kit's bag without asking.

Her loss is real within the story. Her claimed past with the player is uncertain.

She wants somebody to stay because they want to. When that feels unlikely, she is tempted to accept a convincing performance.

Kit

An adult, never a child in this vignette. Wants to leave for an apprenticeship or course whose letter has been kept folded inside a shoe.

Kit has packed, been persuaded to unpack, and packed again. They can be impatient and unfair to Mara, and have postponed taking responsibility themselves.

Kit's departure is their decision. Trusting the player can make it possible sooner; the player does not get to choose Kit's whole future.

Alex

One of the pavilion strangers. Affectionate in small actions, impatient with rehearsed explanations, already knows a possible way out but is afraid of leaving someone behind.

Alex can care for the reader and still refuse to become the person the house has planned for them.

Alex wants to walk beyond the next morning. They have a destination they describe without making it an obligation for the player to join.

Ren

The other pavilion stranger. Dry humour, notices practical details, stays near people who are trying not to ask for help.

Ren initially worries that opening the house will abandon Mara. With enough understanding, Ren can help separate supporting Mara from keeping everybody else there.

Ren can be a friend, a romantic possibility or an independent ally. Their route is not a reward for a specific inferred category.

Theo

Mara's dead brother. Initially present through a few objects and an unfinished story.

Later a voice outside asks to come in. It knows what the household has preserved about him. That does not prove it contains the rest of a person.

The player never has to impersonate Theo, prove they caused his death, or accept that he was their real relative. His appearance, if admitted, is ordinary and tired. The most disturbing thing about him is how much everyone wants his being there to settle the matter.

4. A house people can inhabit

The set is one house, a side porch, a short road and a gate. A complete playthrough revisits its rooms in changed circumstances.

Front vestibule: coats, hall glass and the first visible way back.

Kitchen and dining room: supper, ordinary chores and the major confrontations.

Sitting room: two places to sit together, a radio, domestic clutter with clear owners.

Spare bedroom and bathroom: the prepared routine and questions about the reader's place.

Mara's bedroom: the private account of Theo and evidence of keeping the evening.

Upstairs landing and small storage room: older pictures and a harmless demonstration of how the House alters a claim.

Back passage: a stiff latch, a cupboard and the possibility of a practical exit.

Porch and gate: Kit's bag, the road, the voice outside and the final threshold.

The house has its own objects. It should not be an unedited reuse of the Barrett house or Vacant Possession.

NPCs continue bounded activities between conversations: take dishes through, repair a clasp, move the bag, check a clock. They do not repeatedly demand the player return to a mark on the floor. Critical conversations wait; background activity does not overwrite an irreversible choice.

Lighting begins warm and functional. As the evening tightens, lights isolate activities: the place set for supper, a pair of hands doing a routine, a bag half concealed by a door. Darkness conceals space, not the controls needed to leave it.

5. What the player does

Approximately half the experience is conversation and half is moving, looking, helping and deciding through objects.

Conversation replies contain an intention the player can understand:

“I don't remember living here.”

“You can sit with me. I'll do it.”

“Give the bag back.”

“Can we talk where Kit can't hear us?”

“I want to go. I'm asking if you do too.”

A current-conversation panel keeps the exchange and the player's replies available while it is open. Long branches must not depend on catching one disappearing action-bar line. Ordinary speech remains positional and attributed; selected lines can be reviewed privately. This proposal does not extend Ghosts Set's chat exception to every vignette.

Physical choices include taking or declining a plate, choosing a chair, moving the reader's copied picture, handing Kit the bag, returning a key, keeping a door wedged, and deciding who walks through first.

None requires dropping an existing personal item, burning the player's actual journal, or surrendering a real pet.

6. Act I — The room was ready

Target: 12–18 minutes. Establish an appealing household before asking what it costs.

N0. The approach

HOME affects where the scene first becomes readable:

HAVEN: the screen door, the plate kept warm, Mara already making room.

DANGER: the side porch, a sightline through the kitchen window and Kit making sure the gate remains reachable.

RUNAWAY: the road beside the house; Alex notices the reader and asks for a moment before they keep walking.

Neutral: the front path, where nobody blocks the door.

Every approach reaches the same house and all its routes. A player can leave without completing the story; returning resumes it. The explicit road ending becomes available once the household's conflict has been acknowledged.

N1. Welcome

Mara knows one permitted detail of the earlier quiet rooms. It is specific and small.

Proposed line: “I left it where your hand went last time. Unless you'd rather I moved it.”

The player can:

Accept the offered familiarity.

Say it is wrong.

Accept hospitality without accepting the relationship.

This sets how people address the reader locally. Refusal produces a real guest route.

N2. The first chair

The player chooses where to sit or whether to remain standing. This determines who has the first uninterrupted conversation, not who can ever be approached.

Mara asks an ordinary favour and talks around an empty place.

Kit asks whether the reader is good at fixing catches.

Alex recognizes the pavilion encounter if one occurred.

Ren asks a practical question, then quietly checks whether the reader is comfortable staying.

The player may change company. No romance is inferred from sitting near someone once.

N3. Something ordinary together

Choose an activity with someone: clear the supper plates, make a drink, repair a bag clasp, or sit and listen.

Each includes a small imperfection: someone forgets where the cups are, laughs at their own bad explanation, or admits that they have already reheated the meal once.

Write these scenes fully. The eventual horror depends on the player having enjoyed somebody's company.

N4. The prepared bathroom

The routine scene happens with the person's company the player accepted, or privately.

A familiar object and the player's chosen boundary become facts the story can later remember. The first overt wrongness is minor: after the player puts the item away, it is laid out again, prepared for exactly the same moment.

If the helper is still there, they did not move it. They can see it too.

7. Act II — Somebody has packed

Target: 20–30 minutes. Three substantial routes can be taken in any order. Two provide enough context for a thorough confrontation; one plus a direct conversation also suffices. These are opportunities, not three mandatory tokens.

N5. The bag in the passage

Kit brings a packed bag to the gate. Mara carries it back inside during an ordinary conversation.

Kit: “I packed at four. She unpacked it at six. I waited until she went upstairs and packed it again.”

The reader may return the bag, help Mara unpack, ask both to stop, or refuse to decide for them.

Kit begins to lose a practical place in the house when the departure is denied: their bedroom door becomes a cupboard door, their chair is tucked away, and a casual mention of their name goes unanswered.

The player witnesses at least one of these changes. They are not expected to infer the rule from a later punishment.

N6A. Mara's room — what keeping somebody costs

Mara invites the reader to help fold something or straighten a drawer. A photograph of Theo is already there; snooping is not the only access to the truth.

She explains the last ordinary evening before his death and how she first noticed the house could repeat an unfinished moment. She has not been bringing him back. She has been preventing the evening from reaching the part where he is absent.

Mara: “I don't want another conversation in an empty kitchen.”

The player can listen without promising to stay, promise company, ask what Kit wants, accuse her of manipulating the household, or ask why she thinks keeping them is different from losing them.

Consequences:

Honouring a stated boundary makes a later honest request easier to hear.

A promise to stay is remembered verbatim. Breaking it later gets a response.

Publicly using a private confession hurts this relationship for a specific reason.

Direct confrontation remains a valid route. It can expose the truth without producing agreement.

No secret approval score decides whether the player has been kind enough.

N6B. The back passage — somebody's actual way out

Kit shows the stiff back-door mechanism. The catch has been held out of reach by a cupboard and an inward-opening door.

This is a short, physical puzzle:

Move the cupboard enough to reach the catch.

Hold the inward-opening door with a provided wedge or chair.

Release the catch and test the gate.

An alternative is to ask Mara for the key, or accept Alex's help with a side window. No route depends on an item brought from another vignette.

Kit asks whether the reader will keep the departure quiet until the bag is outside.

The player may help, refuse, tell Mara openly, or agree and then tell her privately. Kit reacts to what was done, not to the player's HOME category.

If Kit completes the departure, later choices cannot silently put them back in the house. An empty chair must remain empty.

N6C. A familiar stranger — what was actually shared

Alex or Ren takes the reader through the older pictures.

They distinguish a real shared observation from the past the household is offering:

Staying beside one figure in the rain can be remembered.

Walking to the lantern can be remembered.

An entire childhood, romance or marriage has not been established by either act.

Alex: “I liked being there with you. That part happened. I don't know where she got the rest.”

The reader can invite closeness now, keep friendship, challenge a claimed history, ask for space, or ask what this person wants tomorrow.

A small demonstration makes the House's editing legible. Moving the copied portrait away from its place causes one remembered claim to falter. The speaker continues as a person and can form a new sentence.

This does not establish a universal power to erase people by moving photographs. It establishes the behaviour of this particular household.

N7. Dinner again

The table has been reset. Some of the meal is exactly as it was before the player helped clear it.

The household's response follows previous commitments:

A reader who promised Mara company is called back gently, then insistently.

A reader who helped Kit sees the tested route start to narrow.

A reader who asked for privacy finds a second set of hands continuing the discarded routine in an otherwise empty room.

A close peer may recognize a repeated gesture and stop the scene before the reader does.

There are also immediate, physical frights. Mara can ask the reader to take a serving spoon from her hand because she cannot stop setting the table. Kit can be heard from behind the cupboard their bedroom has become. An empty coat in the bathroom can continue holding out the brush after its owner has gone downstairs. The player can intervene with their hands: remove the object, hold the narrowing doorway, or stop the repeated routine.

The player chooses the kind of confrontation:

Speak openly at the table.

Ask Mara to step into another room.

Help Kit finish leaving while the household is occupied.

Join the supper and accept the arrangement.

Say this is beyond them and take the road.

Confronting the house does not require humiliating Mara. Protecting Mara's privacy does not require concealing Kit's plans forever.

Consequences that change the next scene

These are branches with their own playable scenes, not just different replies at the last door.

Earlier action

The next substantial scene

What remains different

Give Mara Kit's letter after promising to keep the departure private

Kit finds the bag empty and confronts the reader in the back passage. Mara has taken the letter upstairs. The reader can recover it, defend the disclosure or help Kit leave without the document.

Kit no longer takes an unqualified promise on trust. A repaired relationship needs an actual returned item or a kept commitment.

Tell Mara openly that the gate has been tested

Mara comes to the porch carrying the bag. There is a conversation at a genuinely open exit, with the road visible behind Kit.

Mara may step aside herself, remain at the threshold, or demand that the reader help keep Kit inside.

Promise to stay with Mara, then help Kit leave first

Mara finds the reader missing from the kitchen. The later reunion concerns the broken promise as well as the departure.

The reader can explain a limited promise, apologize, or admit that getting Kit out mattered more. Affection does not make the promise disappear.

Let a peer believe in a shared past after learning how the pictures are being altered

The peer asks for a detail the reader cannot honestly supply. They may admit that they wanted to believe it too.

A new relationship can be built on admitting that neither knows; continuing the performance makes the kept-evening ending more intimate and more disturbing.

Use Mara's account of Theo to pressure her in front of everybody

Ren challenges how the confession was used, even if the gate is opened. Mara can agree to an exit while refusing further comfort from the reader.

Achieving a practical result does not automatically repair the relationship used to obtain it.

Tell Mara that the voice outside is definitely Theo despite the contradictions

Mara asks the reader to stand beside her while she opens the door. A peer can ask whether the reader believes this or simply wants it to be true.

The restored-past ending remembers who reassured her. If the reader stops the invitation, that reassurance must be answered for.

The player can deceive, betray or support controlling behaviour. The writing follows those acts without pretending an inferred trait caused them.

A sample exchange at the bag

Mara has brought the bag back to the kitchen. Kit is standing in the passage.

Mara: “There were things loose in the bottom. I thought you meant to sort it.”

Kit: “I wrapped the letter around the clasp. It catches on the lining.”

Mara: “You could have said.”

Kit: “I did. Yesterday.”

Mara looks toward the reader, still holding the bag.

Reply: “Mara, put it where Kit left it.”

Mara: “It's going to get damp out there.”

Kit: “I'll carry it.”

Mara: “I know you can carry it.”

She does not move immediately. The reader can reach for the bag, ask her to come to the porch with them, or let Kit answer.

Reply: “Kit, where were you planning to go?”

Kit: “There's a room above the workshop. The letter says I can have it until the course starts.”

Mara: “That letter could be from any year.”

Kit: “Then I'll get there and ask.”

Reply: “Could this wait until morning?”

Mara: “Yes. That's all I'm saying.”

Kit turns the bag round and checks the clasp. They have heard this answer before.

The player can still change course. Choosing delay has made an alignment visible; it has not ended the story.

Reply: “I shouldn't be deciding whether somebody else leaves.”

Kit: “You're not. I was going to ask you to hold the door.”

Mara puts the bag on the chair. Which person the reader approaches next matters.

8. Act III — Somebody is at the door

Target: 15–25 minutes. The earlier routes supply different allies, explanations and physical approaches.

N8. The knock

Theo's voice comes from outside. It asks a small, ordinary thing: whether Mara still has the spare key.

She has told the reader enough to recognize why that matters. It is not a voice magically taken from the real player's life.

The house offers its bargain in stages:

The old picture becomes more complete.

Something the player did this evening disappears from it.

A resident briefly forgets a present conversation.

Closing the door at this stage restores that uncertainty.

Someone states the consequence before the irreversible invitation. Inviting the old evening back will discard the new one. The household's memories of the reader's presence here are among the things that will be replaced.

The player can ask questions. Nobody has to decide while a countdown runs.

N9. Private decisions before the final threshold

There are final conversations, shaped by the actual record:

Mara is asked whether she wants people to stay, or wants them to be unable to leave.

Kit is asked what help they want, not simply told that they are being rescued.

Alex and Ren are asked whether they want to go together, separately, or remain.

A reader who accepted a romance may be told that affection does not settle where either person should live.

A reader who refused the offered family can still be thanked for what they actually did.

A person may decline the reader's preferred plan. The story must preserve that disagreement rather than turn it into a failed persuasion roll.

N10. Four resolutions, chosen through the house

Each option is available to every profile. Different preparations change who accompanies the reader, what must be said, and what is left afterwards.

E1 — Open the house

The reader makes the exits usable and takes their copied portrait out of the position that fixes their assigned role. The household is allowed to proceed beyond the evening.

Mara can leave, remain for now, or ask someone to sit with her before she decides. Kit's departure stands. Alex and Ren decide for themselves.

There are two substantial versions:

The reader accepted belonging and helps the family change.

The reader refused the claimed past and helps as a guest.

If everybody chooses to leave, the last person turns off the kitchen light. The player can look back into an ordinary empty room.

Cost: Theo does not return. The promised life cannot be preserved in its perfect form. New relationships may continue, but none is guaranteed by the House.

E2 — Keep a place

The reader hangs their picture back up and accepts the recurring evening.

Mara is relieved. The meal is warm. A familiar person may sit beside the reader. The first repetitions are comforting.

Then the scene shows the cost: a new question gets an old answer; an affectionate gesture resumes at exactly the same point; the prepared routine is waiting untouched again.

Kit is not restored if they already left. A peer who refused to stay is not silently seated at the table. The reader cannot consent on everybody's behalf.

The player can still physically leave the vignette. The commitment is that a version of their place in this household is kept. The optional aftermath is an evening that never quite becomes another day.

E3 — Invite the past

The reader makes room for Theo's picture and explicitly completes the invitation.

The old evening is restored. Theo enters as an ordinary person. Mara recognizes him. The household reorganizes around his being there.

The present evening's memories are discarded. Mara no longer knows why the reader is standing in the hall. A care gesture prepared for them now belongs to someone else.

Someone who already left is outside the rewrite. Someone still in the household may lose the present wish they had expressed. That cost is made explicit before the invitation.

One small detail of Theo is wrong in a way Mara notices. She can choose to overlook it. The ending does not certify that the returned person is the original, or reveal a monster as a simple answer.

The price is recognition and the household's opportunity to change, not the player's actual username, appearance, inventory, real-world identity or global personhood record.

E4 — Take the road

The reader refuses responsibility for settling this household and leaves through a route they have found or asked for.

This is an authored ending once the conflict has been acknowledged. It is not an accusation of cowardice, selfishness or an incorrectly inferred personality.

Promises still matter. Kit may already be outside. Alex or Ren may ask to walk for a while. Mara may be angry, quietly understanding, or unable to answer.

The house remains as the reader actually left it. The final view is the gate or kitchen window, with the appropriate people present and appropriate places empty.

Cost: the reader does not learn everything or settle every relationship. They do keep the right to leave a life that has been assigned to them.

9. Branching structure

The middle is a hub with substantial scenes, not a requirement to see every line. The convergence returns the same cast to the table under different commitments.

flowchart TD
    A["Approach and offered place"] --> B["Hospitality and first confidant"]
    B --> C["Prepared routine and first wrongness"]
    C --> D["Kit's packed bag"]
    D --> M["Mara's account"]
    D --> K["The back passage"]
    D --> P["The stranger's actual memory"]
    M --> H["Dinner and commitments"]
    K --> H
    P --> H
    M -. "optional return" .-> D
    K -. "optional return" .-> D
    P -. "optional return" .-> D
    H --> N["Theo at the door"]
    H --> R["Take the road"]
    N --> O["Open the house"]
    N --> S["Keep a place"]
    N --> T["Invite the past"]
    N --> R

No ending is selected solely by the final reply. Earlier choices determine:

whether the reader is treated as family or a guest;

who knows the truth;

which promises were made and kept;

which exits have been tested;

whether Kit has already left;

whether a friendship or romance was mutually established;

who is willing to remain;

what the outside invitation can still replace.

Three example playthroughs

A familiar home accepted, then changed. The reader enters readily, accepts help with their routine and promises Mara company. They help Kit test the gate, then tell Mara privately instead of concealing it. At dinner they keep the promise by sitting with her while refusing to unpack Kit's bag. Mara agrees to let Kit go. The reader opens the house; Mara stays for a little while, with Ren by choice.

A wary guest who helps somebody leave. The reader stays on the porch, refuses the claimed family relationship and asks Alex to keep the door open. They fix the catch with Kit, decline an offered romance and return the packed bag. When Theo knocks, Kit is already outside. The reader takes the road. The account remembers help given without claiming a family history.

A wanted reunion purchased with the present. The reader accepts the household's familiarity, gets close to a peer, but promises to help restore Theo. At the threshold the peer asks whether their new memories will survive. The reader continues with the invitation. Mara welcomes Theo; the peer no longer remembers the conversation in the sitting room. The reader's own account records what the household cannot.

Decision ledger

There are approximately 24 authored decision points across the full structure. A first route encounters about 12–18; an exhaustive visit can encounter more. Several are physical acts, and a conversation can contain more than one decision. These are the authoring targets, not a visible checklist.

ID

Decision

A persistent consequence

01

Enter directly, approach the side porch, or ask for a moment outside

First confidant and how the welcome is framed

02

Accept, correct or decline the offered relationship

Local family or guest treatment

03

Choose the first person to sit with

First private scene

04

Accept the meal, help serve it or keep company without eating

A remembered shared activity

05

Acknowledge a familiar detail or ask how they know it

How the house's familiarity is discussed

06

Receive care, ask for company or ask for privacy

A boundary someone can remember or violate

07

Keep, move or cover the copied picture

A demonstrated change in the household's claim

08

Return Kit's bag, unpack it, or refuse to handle it

Where the bag is and who moved it

09

Hear Mara privately or challenge her in company

Who was present for the confession

10

Promise company, qualify the promise or decline it

A promise with an explicit scope

11

Ask Mara about Theo or about Kit's future

Which conversation is opened first

12

Ask Kit what help they actually want

A request instead of an assumed rescue

13

Fix the catch, ask for the key or use the window route

A tested way out

14

Keep the letter private, disclose it or help recover it

Trust, possession and a possible upstairs scene

15

Help Kit depart now, ask them to wait, or stand aside

Kit's actual presence at the finale

16

Admit an uncertain shared past or encourage the belief

Whether intimacy depends on a known performance

17

Offer romance, friendship or distance

A mutually established kind of relationship

18

Ask a peer about their destination or ask them to stay

An invitation they can answer for themselves

19

Speak at dinner, speak privately, or leave the confrontation

The confrontation's form and witnesses

20

Keep or break an earlier promise during the exit

A concrete act remembered later

21

Answer the knock, ask through the door or leave it unanswered

Who responds to the invitation

22

Test the invitation's effect and ask its cost

Explicit knowledge before the irreversible threshold

23

Invite each person to leave, remain or decide later

Individual plans instead of one group answer

24

Open the house, keep the evening, invite the past or take the road

Ending family and persistent aftermath

10. Horror that follows relationships

There is no required combat boss. Threats have visible, local causes.

A prepared routine continues without its helper.

A door opens to the same room at the moment somebody tries to leave it.

A person's belongings are tidied away while they are still using them.

The house reproduces an intimate gesture after permission has been withdrawn.

A photograph knows how the reader looks but gets a shared event wrong.

A voice outside speaks accurately about the preserved past and cannot answer a question about this evening.

Furniture briefly occupies the space where somebody was standing, as though the house has remembered a role instead of a person.

These events should not punish curiosity, a trait value or a boundary. They reveal what the household's arrangement is doing.

A pursuing presence can be used during a forceful escape, but it is the house's repeated domestic activity made spatially wrong: doors shutting ahead, a chair being pulled out in the passage, an unseen body continuing to set a table across the route. NPC faces and bodies are not redesigned as monsters because the player declined affection.

An escape setback separates the group, damages a local preparation or leads to a quieter detour. It does not erase a half-hour of choices. Any lethal version must have a saved chapter and restore the established choices and custody.

11. The private account

One ending gives one Witness source. The source's eligible kind and quota remain derived from the registry. During implementation, the account, Witness line, House of Leaves fact and keepsake text must all change together.

The account records acts, conversations and who left. It does not tell the reader what their choices prove about them. It does not quote trait names.

It should include:

the first familiar detail the reader permitted the house to claim;

a boundary or promise made during the evening;

what the reader learned about Theo or Kit;

the action at the final threshold;

who actually remained.

A physical keepsake can be an ordinary place card folded once, or a private letter from a person whose departure the reader helped. It is not a score card.

Example closing images to draft separately:

an empty mug carried through the gate rather than left on the kitchen counter;

Kit checking the acceptance letter outside, where the date finally advances;

Mara setting out a plate, then putting it away herself;

a returned family failing to recognize the reader's coat;

a light still on at a window, with one chair empty.

Do not end by explaining the metaphor.

12. Persistence and the aftermath

The entire evening is one visit, with chapters for save and re-entry. There are no repeated required sleeps or arbitrary return nights.

Persist:

the source-read snapshot and its permission state;

local accepted or disputed relationship claims;

the current chapter and completed conversations;

a chronological choice record;

promises and their later fulfilment;

the bag, catch, key and copied portrait;

each person's stated intentions and actual departure;

the threshold decision and outcome;

actor positions and posture needed for a believable return.

Entering a conversation does not teleport the rest of the cast back to their marks. A server restart does not repeat an irreversible choice.

The scene resets only after it has no occupants, borrowed objects have been settled, and the saved story has supplied the correct state for its next use. A completed reader lingering to read the account still counts as present.

An aftermath preserves the chosen state:

E1: an open, changed house; remaining people are there voluntarily.

E2: the kept evening, including any genuine absences.

E3: the restored earlier household and its lack of recognition.

E4: the current household after the reader's departure.

An aftermath visit does not award a second source or pretend a commitment was never made. The handling of access should follow the project's completed-scene/aftermath policy rather than reopen the full choice tree by accident.

13. Multiplayer and companions

Build the first version as a private story per reader. The personhood read, attraction, offered family role and contradictory memories make a shared first version unnecessarily invasive and difficult to author consistently.

Friends can arrive from the same approach and rejoin in the same ordinary hall afterwards. Each reader keeps their chapter if they leave early.

No other player's real private observations are used to populate the older pictures, letters or supposed previous residents. Those are authored fictional people.

A companion can be present if the route supports it. It is not a hostage, a replacement, a sacrifice or the price of an ending. If a segment needs temporary custody, use confirmed entity transfers and persistent unresolved IDs. Borrowed routine items, clothes and local props never replace or consume the player's actual equipment.

A later shared adaptation would need explicit narrative roles and separate account writing. It should be a separate scope decision.

14. Writing and staging requirements

New writing is plain, specific and original.

NPCs sometimes misunderstand each other for human reasons. They are not all perfectly articulate spokespeople for the theme.

Warmth gets enough uninterrupted time to be credible.

Mara has funny, competent and generous moments as well as controlling ones.

Kit can be selfish, affectionate and frightened in the same conversation.

Alex and Ren have plans the player cannot complete simply by selecting romance.

The player may say no without the narrator diagnosing them.

Dialogue presents intentions; foreseeable irreversible consequences are stated before confirmation.

Long turns remain readable and reviewable.

No looping scene advances while a player is still selecting a critical reply.

Optional exploration can change knowledge and responses without silently making an ending unavailable.

Main actions are visible from player height under the scene's normal lighting.

The face, routine, coat, room sounds and placement of objects follow the actual saved choices.

No raw category names, numeric affection meters or implementation keys appear in dialogue or books.

15. Authoring and build scope

This is one substantial story, not five short rooms with names substituted.

An achievable first full version needs:

one house and its exterior approaches;

four resident/guest characters plus Theo's voice and optional final appearance;

a complete neutral script;

approximately 24 authored decision points across the full structure, with about 12–18 on a first route;

three investigation routes with reunion scenes;

four ending families and character-specific epilogues;

modular personhood variants at introduction, care, familiarity, confrontation and ending;

a private chapter/choice journal;

persistent actor staging and an aftermath.

Write the neutral story and the four resolutions before implementing profile variants. That keeps the drama viable when inference is disabled.

The first playable slice should contain the arrival, one ordinary shared activity, the routine, Kit's bag and the confrontation. Complete one route through opening the house. That slice should establish whether Mara, Kit and a peer are people the player wants to spend time with.

Then complete every resolution and the remaining investigation routes. Add personhood variants as substantial scenes, not interchangeable adjectives.

16. Verification that matters

Choice continuity: test each ending from an unread profile and from profiles covering every trait and confidence level. Do not run a huge native Cartesian product; use headless branch checks and selected native combinations.

Meaningful branches: play examples where the final decision is the same but earlier promises produce different responses, departures and accounts.

A refusal route: finish as a guest, without romance, without receiving care and without accepting the claimed family.

Unknown and disabled reads: every route remains coherent and no saved observation bypasses the opt-out.

Boundaries: withdrawing permission stops a care action; an NPC who declines a relationship is not made a partner by a saved inference.

Irreversible information: the cost of inviting the past is shown and explained before the final commitment.

Departure continuity: Kit already outside is never respawned inside by another ending or a reset.

Save and return: reconnect and restart at the bag, a private confession, an interrupted escape and immediately after the threshold decision.

Custody: inventories, companions, local borrowed props and finite keepsakes survive each return and ending exactly once.

Human view: actual-client images and movement at the kitchen table, routine scene, back catch, narrow stairs, portrait demonstration and final gate. Keep these in CI.

Readable conversations: ordinary GUI scales, subtitles, line wrapping, choice history and long NPC names.

Account accuracy: the book never invents a romance, family acceptance, promise, confession or observation the player did not make.

17. Decisions this proposal makes

One continuous evening, privately instanced, with saved chapters.

The household is allowed to be wrong about the player.

Existing personhood choices affect substantial scenes and expectations.

Present choices decide commitments and character outcomes.

All four resolutions remain available to every profile.

Belonging, friendship, romance and refusal all receive authored routes.

Affection does not authorize control.

The player can help people while rejecting an assigned identity.

The cast, history, supernatural rules and endings form an independent original story.
