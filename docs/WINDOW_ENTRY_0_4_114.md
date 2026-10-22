# Window entry and paw, 0.4.114

The October 21 playtest reported a stiff child-disguise crawl, an impostor stuck outside after it and a misoriented paw thumb. This pass continues the route-priority fixes at `b475b1ef`.

The original impostor now reaches, hoists onto the sill, pulls through with articulated elbows and knees, then lowers its feet and stands inside the bathroom. The child and monster meshes use the existing pixel textures. A tracked, saved progress value drives the same sequence on clients; native mid-sash reload keeps the actor UUID, position and remaining route. Ordinary cousins retain their individual walking profiles and reset the additional joints before another pose uses the renderer.

Admission uses native collision movement through the actual open sash and bathroom doorway. Its route retains control until the original actor reaches the floor among the sleeping cousins. Dawn waits for that completion, including when a blocked exterior circuit made the admission late. Closing the front door remains necessary for survival; an open-window admission still produces the existing haunting rather than Witness credit.

The paw's thumb begins beside the palm and curls across its front toward the folded fingers. Its native geometry check measures the open and closed tip positions and confirms that a previous wish cannot change its rest pose.

## Verification

The existing finished-camper delayed-admission regression now checks all four progress quarters, actual low collision, native mid-sash entity reload, standing bathroom departure and arrival on the sleeping floor before dawn. It retains the independent seven demands, complete blocked/unblocked circuits, original identity, haunted outcome and exclusion of survival credit.

Seven additional native GPU pages show the child and monster at reach, sill, pull and recovery, plus the actual paw open, half curled and closed, each in two views. The proof samples 101 child poses, checks pose reset and measures thumb opposition. It runs in the standard native chain and independently with `gradle runClient -PwindowEntrySmoke`. CI requires its marker and every page and retains failed readbacks.

The declared gameplay corpus remains 716, including 95 multiplayer cases. All twelve exact-source CI jobs, existing 99 architecture views, both actual clients and reconnect checks remain required. Compilation and native validation are pending; this document does not claim a verified release.

Version 0.4.114; protocol 87 because the child gains tracked entry progress; labyrinth layout 46 and manor layout 2. The playable Witness pool remains 52, requiring 39 personal resolutions across at least two kinds, with three House endings. No new source, geometry migration, actor replacement, inventory replenishment or PNG repaint is introduced. The six uncanny rooms and earlier Claude, Borrasca, hotel, home and portrait work remain in the branch.
