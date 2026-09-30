# The Mother of Strays — 0.4.5

The den is a recurring anchor dealt through normal labyrinth routing. It contains the Mother, three missing cats, a bandaged Pekingese, a chair, a record on a lectern, shelves of lost objects and a bell for paging. The gallery ladder overlooks a dry well.

Meaningful item despawns and tamed-pet deaths are archived across dimensions from installation onward. Items retain their components; pets retain their saved identity, owner, name and appearance. Equipment and carried inventories are excluded from recovery because they already follow Minecraft's death-drop rules. The three initial named objects are anonymous authored keepsakes. One retrieval is allowed per actual visit; reconnecting inside the den does not reset it. Claiming a shelf is atomic. Split stacks returning by expiry cannot duplicate the archive. Only a pet's original owner may reclaim it. The bell reaches all archived pets, with twelve displayed per page.

Right-click a frame to take its object. Right-click the Mother with that object to return it, or with a loved offering to exchange it. Named or enchanted objects qualify immediately. An unstackable meaningful object qualifies after ten minutes in that player's inventory. Right-click an archived pet with such an offering to reclaim it. Released pets can follow their owner out of the House.

An active unpaid claim grows her deformation over three minutes spent inside the House. She searches dark, supported standing positions with walls, corners or occlusion between herself and the player. She begins about 30–40 blocks away and closes toward 8–16 blocks, repositioning more frequently. Every nearby player's line of sight vetoes a move. Looking at her stops her navigation. Threats pause outside the House or offline. At three minutes she warns the debtor; at three and a half minutes she approaches to reclaim an object with a nonlethal grip, hunger and immediate restoration of her ordinary face.

Asking to adopt the Pekingese empty-handed starts her threat. The dialogue acknowledges the offer: she regards disposal as mercy, but the scene does not explain away the cruelty. After thirty seconds she can move unseen to the gallery. A full fifteen-second intervention period begins when she actually reaches its ledge. Climb the ladder and right-click the dog with a loved offering, or offer one to the Mother below. Rescue can also occur before she reaches the gallery. If she lets go, the fall is lethal to the untamed dog. His death is permanent, and her face becomes ordinary again.

To salve her, peacefully return a borrowed object, freely give a loved object, and save the Pekingese. With all three acts complete, she stops stalking and creates no new claims. To end her presence permanently, settle any outstanding claims, take the lectern's **Things kept** record, and crouch while presenting it to her. She disappears, all remaining collection entries become freely reclaimable, and future despawns/deaths are no longer collected. This farewell affects the Mother; it does not end the House.

## Development playtest

1. In an operator test world, use `/oldesthouse door mother` and enter it. Normal dealer routing also reaches this anchor.
2. Take a keepsake. Leave and explore connected gray rooms with dark cover. Look behind you, change corners, and compare her shape and distance before and after the warning. A small lit room may have no safe stalking position.
3. Return the actual object; on a later visit, trade a named offering instead. Confirm the shelf and inventory do not duplicate it. Try two players claiming the same shelf.
4. Ask for the dog without an offering. Observe the gallery, then intervene with a named offering during its fifteen-second window. In a separate disposable test world, let the fall occur and confirm he stays gone after reload.
5. Return an object, give a voluntary loved offering and rescue the dog. Use the record while crouching for the final farewell. Confirm the saved result after restart.
6. Check a named tamed pet's recovery and travel out of the House. Check that ordinary losses remain ordinary after the farewell.

Upgrading an existing version-10 labyrinth adds only the new den; it preserves the previously built rooms and player markers. `/oldesthouse labyrinth build` remains an explicit full rebuild.

The build's GameTests cover archive fidelity, multiplayer claims, visits, persistence, split stacks, offerings, corruption, dog timing, rescue/farewell state, saved pet identity, concealed-cover scoring, entity registration and item-lifetime compatibility. An in-game client playtest remains necessary for movement pacing, fall presentation and visual tuning.

## Model authoring

The appearance reference is Johnnie in Mark Z. Danielewski's *House of Leaves*, footnote 249, chapter XI, pp. 266–267 in the cited edition. The passage gives her a petite figure, platinum hair, excessive eyeliner and a conspicuously full bust. Johnny's later perception of her makeup and mouth becomes grotesque. These descriptions guide the ordinary form and its deterioration; they do not establish a literal supernatural transformation. The bob, wine-coloured shawl, dark dress, burgundy nails and the sixteen-stage transformation are this mod's adaptations. They replace the earlier invented elderly, gray-bun appearance. A public quotation of the relevant passage is in [Engl 252's reading notes](https://engl252.wordpress.com/readings-of-house-of-leaves/).

`tools/generate_mother_assets.py` emits the Java geometry, sixteen Mother UV textures, the Pekingese texture, local-pivot JSON model sources, `art/mother_model_preview.png` and `art/mother_texture_stages.png`. Pillow and numpy are authoring dependencies only. Mother textures are 512×1024 pixels on a 128×256 logical UV layout; the model source records both resolutions. All sixteen steps are used by the entity renderer.

The progression keeps her identifiable. Stages 1–4 principally cool her complexion and reduce the warmth of her makeup. Stages 5–8 add faint shadows, creases and very small posture changes. Stages 9–12 introduce uneven eyelids, a strained mouth and longer, more curled fingers. Stages 13–16 deepen those changes and lengthen her chin slightly. Shape changes are continuous and eased; her head stretches at most 14 percent rather than 40 percent, and the chin extension begins only after 74 percent corruption. Her eyes stay visible throughout. Reclamation or the dog's death still restores the ordinary appearance immediately.

Her petite scale is reflected in her collision dimensions and eye height. Her hands still fold to carry the dog. The Pekingese retains its short-legged, flat-faced model, curled tail, floppy ears and head bandage. Both previews render the authored cuboids and their actual textures. The stage sheet also magnifies the exact face UVs; those are texture samples, not separately drawn portraits.
