# Four more keepsakes that do something — 0.4.198

The owner: "most of the items you provided the player with don't really have an effect on the gameplay, either inside or outside of the house." 0.4.192 gave the first nine keepsakes an effect. This change does four more, from the audit's table (docs/SUCCESS_AND_FAILURE_AUDIT.md, "Every artifact does something"). As before:
- each works only for its own reader;
- each adds no Witness credit;
- each leaves the item itself unchanged.

## The four
- **Zampanò's leaves** (any leaf pulled from the courtyard drawers), held in either hand in a plain hall. White ash drifts at the handle of the hall's door back toward the manor, seen only by the holder (`Keepsakes.leafWay`). The survey measured the way through. The leaf's tooltip says so.
- **The photograph with the vulture** (the plain), held in either hand while standing still for a second (`Keepsakes.holdingStill`).
  - A monster more than six blocks away cannot fix on its keeper (`LivingChangeTargetEvent`).
  - One already hunting them from that far looks away.
  - A step ends it. The frame was made by holding still while the bird waited.
- **Her compass** (Borrasca), used once the story is over (its account taken). Away from the House, the needle settles toward where the reader last fell, with the direction and about how many blocks. In the House it settles toward the Mother's den, where lost things are kept. It found the missing. During the story the compass keeps its own uses.
- **Her name patch** (Borrasca), carried. Once a House day, when the reader dies away from the House, whatever is in their main hand stays out of the fall and is in their inventory again when they wake (`Keepsakes.sew`/`unsew`, saved in `keepsakes_0198`). With keepInventory on there is nothing to keep, so it does nothing. Dying inside the House is the Mother's matter and is untouched.

## Not in this change
- **The key to Mother's room** (the Wheel): the audit's effect, one reclaim from the Mother at no cost, works through the Mother's ransom. The owner has asked that the Mother not be changed, so it waits for the owner's word.

## Literature
- Each new keepsake carries a line saying what it does, in the room's words:
  - new photographs and new Borrasca originals get it as lore (`Keepsakes.line`);
  - the leaves get it in their tooltip.
- The Witness account's Borrasca line gains an afterwards sentence when the patch has kept something: "Afterwards, once, when I fell away from the House, her name kept what was in my hand." (`AccountAfterwards.patch`).

## Test
`KeepsakeTests.theLeavesPhotographCompassAndPatchEachDoWhatTheirStoryDid` checks:
- the still photograph keeps a zombie seven blocks off from fixing on its keeper, while one two blocks off can;
- the compass settles 40 blocks east toward the last fall;
- the patch keeps a sword out of the fall, gives it back on waking, and only once a House day;
- the account's afterwards line;
- a leaf held in a straight hall marks its entry door, and does nothing empty-handed.

All four keepsake cases pass locally.

## What stays
- **Protocol 102, layout 52.**
- **Witness unchanged:** 55 / 42 / two kinds / three endings.
- **No PNG changes.**
