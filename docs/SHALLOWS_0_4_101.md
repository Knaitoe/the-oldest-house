# The Shallows, reworked — 0.4.101

Minecraft 1.21.1 · NeoForge 21.1.251 or later · matching 0.4.101 client and server (protocol 73, layout 45)

The owner asked for the Shallows to be reworked: the reader sees the girl, the witch's human skin, carried between
them and a boy; the reader swings her back and forth with separate keys; the boys talk as the scene goes on; on the
count they let go of her mid-swing, trying to throw her in the lake for a witch. Nobody knows what she is, or will
become. The lake does not take her. Her body breaks on the water, limbs twisted. After a breath she gets up, and
runs away into the woods on all fours, across the water.

Built on 0.4.100 (4f10828). Everything else in the House is unchanged.

## The bank

Dealing is unchanged: only after this reader's own hunt in Drowned Town, never again after they have thrown her.

Stacey lies on her back on the sand at the edge of the water. **Lonnie**, a boy only this reader is sent, stands at her
feet holding her ankles up. The three saved boys on the bank, **Bud**, **Hank** and **Clay**, keep their original
bodies and UUIDs; 0.4.101 only gives Hank and Clay faces of their own. Lonnie has a fourth. All four are painted with
the common 0.4.100 painter (`lake_boy`, `lake_boy_1`–`3`).

While she lies there the boys tease her about being a witch (she will curse you; witches float; ask her where she goes
at night), and she asks to be let up. The lines repeat slowly if the reader waits.

## The carry

Right-click her with empty hands (a one-armed reader's off hand is already empty) to take her wrists. The reader is
placed at her head, facing Lonnie across her; she hangs between them, arms stretched over her head to the reader's
hands. The grip is the existing owner-bound temporary item; dropping it, sneaking or being moved off the spot lets go
of her. She goes back down on the sand with Lonnie still holding her feet, and the count starts again.

**The reader's strafe keys swing her.** The lake is on the reader's left: the left key swings her out over the water,
the right key brings her back. Walking and jumping are held while she is carried; sneaking stays, to let go. The
actionbar names the reader's own bindings. Each press is one stroke (about 0.6 seconds); a press made early is kept and
applied as soon as the stroke allows, and the same key twice is not a stroke ("Other way. With me."). Lonnie counts the
strokes out: "One!", "Two!", "Three!". The swing grows with each stroke. Left alone for 2.5 seconds it dies down and the
count starts over.

On the third stroke out both boys let go **in the middle of the swing**, while she is still rising toward the water. She
leaves with the swing's own motion plus the heave, and falls as anything thrown falls, about six blocks out.

## The lake

She stops at the first thing under her: the surface of the water, not the water. There is no plunge. Her bones break on
it (an original subtitled sound, a flat wet slap with cracks); a little water splashes and nothing goes under. The
landing is the throwing beat: it records the reader among the boys in the essays, resolves the Witness source (still
`threw_her`) and exhausts the scene for them alone, exactly as the old splash did.

She lies still, face down, every joint at an angle it cannot take, for three and a half seconds. The boys stand frozen.
Clay whispers that she didn't go under; Hank says her name. Then she gets up wrong: a twitch, the head snapping round,
the hands planting with the elbows bent backward, then up onto all fours in hitches, bones grinding. She runs north
across the surface of the lake on all fours, her head upside down, into the woods on the far bank. The boys back away.
When she is gone: "Nobody saw that." "Witches float. She floated, that's all." Lonnie: "That was all of us."

The aftermath runs on saved phase time, so a reload, a disconnect or a departure never repeats or loses the throw.

## Privacy and multiplayer

The girl and Lonnie are owner-bound: native tracking sends them only to their reader, as before. Two readers in the
memory at once get separate carry places along the bank. Every line, every reaction of the three shared boys (jeering,
cheering, freezing, recoiling, whispering), their heads following her, and every Shallows sound is sent to the one
reader whose memory it is. Lines appear over the speaker and in chat.

Striking her, a bank boy or Lonnie before the throw still darkens the visit with no credit. After the throw a blow is
ignored. Leaving keeps an unfinished scene retryable and removes both private people and the grip.

## Saved worlds

No geometry changes; layout 45 stays. A visit saved before 0.4.101 keeps its girl and gains Lonnie on the next arrival.
Old phases still load: a standing girl lies down, an old sinking body plays the new aftermath. Recollections no longer
use native gravity; Shallows places them every tick, the flight included.

## Network and assets

Protocol 73: one client-to-server payload (the strafe key pressed, nothing else; the server derives the reader, the carry
and the stroke), one server-to-client payload (a line and a reaction over one speaker), synced swing and carry data on
the girl and the boys. Four original subtitled sounds, synthesized by `tools/generate_shallows_audio.py`: the swing, her
break on the water, her bones as she rises, the crossing. Three new 64-pixel boy paintings join the character inventory
(769 PNGs accounted: 120 painted, 649 unchanged).

## Validation scope

Two Shallows native cases rewritten and one added (650 declared): the full carry, alternate-key strokes, the count, the
release on three, a landing on the surface that never goes under, the essay name and Witness for the owner only, the
still body, the crossing northward and arrival in the woods; letting go by sneaking, a dying swing restarting the count,
and a blow to the boy at her feet; private carriers and separate carry places for two readers with saved originals.
A new native client proof renders eight views of the scene on the production meshes and transforms and checks that she
lies along the carry with her head to the reader, that strokes carry her and both carriers' arms toward the water and
back, that she lies flat when broken and crawls low.
