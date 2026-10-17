# The Holy Rabbit, a pass — 0.4.101

The owner asked for a pass on *The Father, the Son, and the Holy Rabbit* after the October 17 gameplay and storytelling
review. It ships in the same candidate as the reworked Shallows (protocol 73, layout 45). No geometry, actor, item or
sound changes; 46 sources / 35 resolutions / two kinds / three endings are unchanged. Every earlier owner direction
stands: the explicit stay or an owned meal commits the visit, hunger stays at one, the cold uses the native freezing
overlay, Father walks, harvests and tosses the same living Slaney, belief comes before the wound, and the food is
explained only after the wound is seen.

## What could go wrong before

The review found that a committed reader (whose doors are locked until the story ends) could be trapped for good:

- **The trail could become lethal forever.** Once Father had gone, every visit to the trail past thirty seconds of
  accumulated trail time cost two hearts a second, and the counter never reset. It also started counting while Father
  was still leaving, against the 0.4.90 direction that the escape cold waits for his retreat. At the frozen walking
  speed the 85-block trail takes about forty seconds, so a normal walk ran out of time.
- **The carving could fail to appear.** The ending required the carving to have been read, but it could only appear in
  the single tick the second night turned, and only if nobody was looking at that spot. A reader in the hollow often is.
- **The mornings could be missed.** Slaney counted only if seen in the first thirty seconds of a morning.
- **Nothing healed.** Hunger at one stops natural regeneration, while freezing outside the shelter and Father's
  wake-up shake both took health, so a long stay could kill the child before the trail.

## The pass

**Nothing traps the reader.**

- The trail's escape cold starts only once Father's retreat has finished, and starts over whenever the reader is back
  under the tree. It allows a minute before it bites, which leaves room for the walk.
- Father will not take Slaney until the reader has seen him alive that morning. He stops, points and calls ("Look. By
  the snow. Look at him before I go.") until they look. A morning now counts any time before he takes the rabbit, and
  all through the last day, when he can no longer get up. Two mornings are therefore always seen by the third night.
- The carving appears on the last day at the first moment nobody is looking at it. Father points to it once if the
  reader hasn't looked up ("I cut something into the tree for you. Up high, where the snow can't get at it."). It is
  now something to remember him by rather than a key. The way out needs the two meals, the two mornings, the wound seen
  and his going, which is the owner's 0.4.88 list. A carving the reader did look at goes into their account.

**The cold hurts but does not kill.** As with the House's temperature hazards, the cold and Father's shaking never take
the last three hearts. Near that floor the native frost stays just short of fully frozen, so vanilla's freeze damage
stops, and other hurts are skipped. Father still shakes a drowsing child awake. Walking past the trees before the
trail opens is turned back by the wind ("Father hasn't gone that way"), not by death.

**The story is told by what the child sees.**

- The source, the child's own pages, no longer gives away the ending: the meat comes in a rabbit skin, the trousers are
  wet "from the creek". Its third page drops the instructions for the child's own words, keeping the same hints: he
  shakes them awake, Slaney is back every morning, and when the snow lets them go they go fast and don't stop.
- On the first morning Father points to "the little one from your dream" and names him only once the child has seen
  him, so the name is no longer given twice.
- The belief line is the child's: "Last night you ate. This morning he came back."
- The wound is shown and not explained: his cut trouser leg, a cloth bled through, and Slaney sitting where he sat this
  morning. Father's "You needed to eat" follows, as before.
- The account names the rabbit as the reader knew him (Slaney, or the name they gave him) and states nothing the child
  did not see. It mentions the carving only if they looked. It ends on its own last line, under the Witness title
  "The food in the hollow", without the shared folded-paper afterword.

Saved worlds: a reader partway through keeps their night, meals and records. The new behaviour applies from their next
tick. Already collected accounts keep their saved words.

## Validation scope

Three native cases added (653 declared):

- The cold and Father's shaking leave three hearts, and the trail turns the child back before it opens.
- The escape cold waits for Father's retreat and resets under the tree. The carving appears unseen on the last day,
  the way out does not need it, and the account remembers it only if it was read.
- Father waits for the reader to see Slaney before the apparent harvest.

The existing motion, two-night, ending and shelter cases are unchanged.
