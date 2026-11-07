# The Sphinx: the dream (0.4.342)

From the owner's draft "The Sphinx" (4 November), "The last question":

> *"When you open the door, who gets out?"*

The owner, on who dreams it:

- "A player who did not encounter the sphinx should not have the dream."
- "By the time a reader is experiencing the dream, the house should be closed to them one way or another."
- "The dream should build the sphinx's whole body as a massive entity. As described in the document."
- "It should be so large as to occupy most of the player's screen."
- "Do you have her feet angled downwards while in flight? You should also match the tone with her face."

## Who dreams it

A reader dreams it only if both of these are true:

- **They have met her.** They have resolved three of her riddles. A riddle counts as resolved if they answered it, deliberately walked away from it, or forced it.
- **The House is closed to them.** It closed through an ending of their own (Witness, escape, or locked out) or through the House's collapse.

Everything else about the dream belongs to that reader alone.

## The survey and the invitation

- **The survey at a threshold.** After a third resolution, the reader's own folded field survey is tucked into the frame of the next ordinary hall door they come through. This happens only if they don't already carry one. Her rooms, puzzle rooms and hazard rooms don't count as ordinary doors.
- **The survey in a pocket.** A reader whose House closed before they reached such a door finds the survey in their pocket. If they are without one and haven't yet invited the dream, they find another once a day.
- **The invitation.** Using their own survey on an Overworld bed sets it down on the floor beside the bed, where anyone can see it lying.
  - Someone else's survey can't be set down, and neither can a survey belonging to a reader who never met her.
  - Setting it down copies the bedroom the reader is standing in, the same way the Red Room copies a room.
  - The reader's private dream site starts building, a slice at a time.
- **The dream comes** the next time they sleep in that bed with the House closed to them. A reader whose House is still open can set the survey down, but sleeps there and dreams nothing until it closes.

When the dream begins, the House fixes the reader's record:

| What | How it is set |
|---|---|
| **Reach** | Three, five or eight blocks, from accuracy as in the draft. Short with nothing answered or under half right. Medium from half right with fewer than three answered, or from half to under three-quarters. Deep at three-quarters right or better across three or more. |
| **Manner** | Hostile if they ever pulled a stop pin; dismissive after two walkaways; patient otherwise. |
| **Carving** | From the riddle they most recently solved themselves, or worn smooth if they never solved one. |
| **Chipped** | A hostile dream leaves one corner freshly broken. |

The survey is taken up into the dream.

## Her face

Her face fills the screen out of the dark, close enough that only her eyes and mouth are in view.

- It is the face of the first crossing.
- Her eyes are on the dreamer.
- The skin beneath one eye shifts when she draws breath.
- Her mouth moves as she asks the question (0.4.341).

Her words come in the action bar, as every unseen voice's do. Its echo comes back from high above. While her face is up, the reader lies in the dream's bed. Then the first waking begins under her face, and the face goes slowly off it.

## The first waking

The reader wakes in a private likeness of their own bedroom, in morning light. It is the room they were standing in when they set the survey down; with no room to copy, it is a small staged bedroom. Outside it is the dream site:

- the room standing alone as a little house, with a plain gable roof;
- a fenced yard and a path to a gate;
- fields of wheat in every direction;
- a hedgerow beyond.

The front door is the room's own door, or a plain one cut into the gap. It is shut, with sand heaped against it outside and a skin of sand blown in under it. The way the door gives is the manner of the stone to come:

- **Patient.** The sand gives little by little. Each push moves it, and the third lets the door swing.
- **Dismissive.** The door sticks. When the reader pauses, the sand is heard to settle, and the next push opens it.
- **Hostile.** The door comes free hard enough to hit the wall, and grit flies.

She is on the roof the whole time, and inside the room the reader can hear her breathing overhead. Her forepaws rest on the ridge. Her body slopes down behind the house, as steeply as the ridge is high, to hind feet on the ground. Her head is up and turned down toward the door and the yard.

The house is small under her. Her paws overhang its gables, her hind feet stand in the wheat well behind it, and her head is high over the door. From the yard, her chest and face fill most of the view, and her head turns and bows to whoever is in the yard.

When she goes depends on the manner:

- **Patient.** She waits until the reader is out in the yard and looks up at her. She also goes after fifteen seconds outside.
- **Dismissive.** She lifts off while the door sticks. When the reader comes out she is already over the far field, and she holds there until they look.
- **Hostile.** She goes as the door bangs open, passing low over the roof.

Her flight is the same in each manner:

- **Lifting off.** Grit and sand come down off the roof over the yard.
- **Over the yard.** She passes overhead with her wings open, and the underside of a wing covers the yard.
- **Away over the fields.** She climbs until her whole body can be seen against the sky. About a hundred blocks off it still fills most of the view. Then the haze takes her.
- **The sound of her going** arrives a beat after she has gone.
- **The dream ends.** The first waking goes dark.

If the reader never opens the door, she goes after two minutes anyway, heard from inside.

## The second waking

The reader wakes again in their real bed, lying in it a moment, and the night passes as it would for any sleeper.

- **The front door.** This is the door they have used most, if it is near the bed; otherwise the nearest door. It taps against its frame, open, though only they can see it open. It does this through that morning while they are near.
- **The stone.** The piece of the Dream Stela sits on the floor just inside that door, in a palm-sized piece of granite with sand still in its cuts. With no door near the bed, it sits on the floor beside the bed.
- **The claim.** Only the dreamer can pick the stone up. It never despawns and can't be destroyed. If it is lost from the sill, it is put back the next time they come near.

Taking the stone ends the dream.

## Her body

The owner: "Please give more attention to its body. Give it more detail ... you should rely on multiple blocks for design. You also need to give it actual forearms."

`SphinxDreamEntity` is her whole body, twelve times a mob's scale, so that from the yard she fills most of the screen. She is built of about a hundred and seventy blocks. Each face of her coat takes its own patch of the coat's picture at one texel to a model pixel, so every block has the same grain and no two the same patch. Her parts:

- **Forelegs.** Each has:
  - a shoulder, with the blade of the shoulder rising toward the withers;
  - an upper arm with the triceps behind it;
  - an elbow standing out at the back, with a tuft of coarse hair;
  - a forearm, thick below the elbow and slimmer above the wrist, with the long muscle down its front;
  - a wrist with its dewclaw;
  - a paw: the pad, the heel pad behind it, four toes with their pads and knuckles, and a hooked claw on each.
- **Her body.** The breast, deep in front of the forelegs, and the keel under the chest; the chest and withers; the ribs, with three ridges showing along each flank; the loin, with the soft belly tucked up behind the ribs; the haunch, the points of her hips and the rump; and the line of her spine. About fifty blocks from forepaws to haunch.
- **Hind legs.** Each has:
  - the great thigh;
  - the stifle;
  - the shin, with the calf heavy behind it;
  - the hock, its point standing out behind;
  - the long bone down to the foot;
  - a paw like the front ones.

  The joints are set so that her hind feet stand on the ground behind the house, whatever the height of the ridge.
- **Neck and collar.** A neck under the headcloth, which falls behind her head over the nape and down in a plait. Two striped lappets hang in front of her shoulders. A broad collar of lapis, gold and carnelian beads lies across her breast.
- **Head.** The headcloth's crown, drawn tight, and its sides flaring out behind her face. The face of the first crossing, some twenty blocks across, with its eyes turning to the camera, its blink and its mouth. Perched, her head turns and bows to look down at the reader.
- **Tail.** Six blocks narrowing to the tuft. It droops off the haunch and curls back over the field while she waits, and streams behind her as she flies.
- **Wings.** Each wing has two segments:
  - the scapulars where it meets her back;
  - a thick leading edge;
  - rows of coverts, with a faded painted band;
  - eight separate secondaries scalloping the trailing edge;
  - nine primaries that fan out as she opens to nearly two hundred blocks.

  Folded, the hand lies back along the arm, and the feathers lie back toward her tail along her flank, their tips crossing her rump. She flies high enough over the yard that the wingtips clear the fields.
- **Legs in flight.** Her forelegs swing back and down under her chest, and her hind legs trail back and down from the hip. All four paws hang with their toes angled down.

`tools/draw_sphinx_dream.py` draws her body on one picture:

- her coat, in the worn umber of her face's stone rather than a living lion's tawny, darker along her back, paler under her belly, and in shadow inside her legs;
- the claws' horn and the pads' leather;
- the coverts in their scalloped rows, and the faded band of painted lapis and gold;
- one flight feather, top and underside, each also turned end for end for the folded wing, each feather block showing its own outline;
- the beaded collar;
- the headcloth and the tuft.

Only the dreamer is ever sent her body. She can't be touched or hurt, and she is never saved.

## Multiplayer

Everything about the dream is the reader's own:

- their survey and their invitation;
- their dream site (one row in the outside dimension, 256 blocks apart);
- their body of her;
- their door state;
- their stone.

Several readers can dream at once and wake on their own time. A friend sees the survey lying by the bed, and later a stone on a sill they can't take.

Logging off during her face or the first waking begins the first waking again on return. Logging off after the second waking leaves the stone waiting.

The House's exclusion of readers it is closed to doesn't apply inside a reader's own dream site. Nobody is hurt in the dream, and the dream's house can't be broken or built on.

## Assets

`tools/draw_sphinx_fittings.py` adds **drifted sand** (`SphinxBlock` kind `DRIFT`, three stages).

`tools/draw_sphinx_dream.py` draws:

- her body (`entity/sphinx/body.png`);
- the stela's ten pictures and item models: four carvings and smooth, each whole and chipped;
- the item's and entity's names.

Every sound is one already made for her, apart from vanilla door, page and dust sounds. Protocol 140 (`sphinx_dream`).

## Tests

`SphinxDreamTests#theDreamComesToThoseWhoMetHerOnceTheHouseIsClosed` (exploration) runs five readers at once. It checks:

- the three manners from the records, and who met her;
- the House closed to three readers by three different endings, and open to the fourth;
- the survey in a door frame (once) and in a pocket, and none for the reader who never met her;
- survey invitations: one beside a real copied bedroom; refused to the stranger; allowed but dreamless for the reader whose House is open;
- a dream site each, and the records fixed as the dreams begin;
- morning, and her body sent only to its dreamer;
- the patient door's three pushes, the hostile door and launch, and the dismissive stick, settle and open;
- her lifting off when looked at, and holding over the far field until she is seen;
- the second waking in the real bed, the stone at their own door, deep and carved, and the hostile reader's smooth and chipped;
- nobody else able to take a stone, and its owner taking it;
- no Witness credit.

The client proof has two pages for the dream:

- **The dream page** shows her as the reader's own screen does: seventy-degree views from the yard under her, from the gate, under her wing over the yard, and from the doorstep as she holds over the far field.
- **The stela page** shows her body as a model and the stela's ten pieces. The model is shown four ways: perched from the yard, perched from the side, flying from the side with her paws hanging, and flying from under her wings.

Next, 0.4.343: what the stone does against loose fill.
