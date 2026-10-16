# Character review — 0.4.100

The owner requested examination of every generated character, excluding Borrasca, and clarified the target: aesthetically pleasant designs fitted to Minecraft, not literal painted faces. This supersedes earlier image-byte preservation only for the 117 sheets explicitly listed in `art/inventory/character_review_04100.json`. The remaining 649 packaged PNGs retain their exact original bytes, including every Borrasca painting.

## Findings and changes

- Replaced portrait faces, repeated suit pictures, generated fabric pictures and non-integral export grids with per-face pixel paintings. Faces use 16 × 16 authored pixels on the eight-unit Minecraft head; ears, hair, collars, seams, cuffs, hands, pockets, trouser creases and soles have their own intended surfaces. Palette changes distinguish the original people and outfits. Club despair, changed family, Father injuries, Marjorie's dinner and John's hand keep their state-specific art.
- Harrigan now uses connected elbows and knees. The study pose has hanging lower legs and feet at the floor. His body remains the original saved ArmorStand; living, dead-in-chair and casket states and the three mourner appearances use the same existing synchronization. The casket pose folds his hands over the torso, supports his head over the pillow, centers an adult-proportioned body inside the shorter coffin and resets all joints before another actor/state renders. The coffin's physical shape matches its new mesh; the original block address and contents/phone choices remain.
- Marjorie's dinner now uses the same 128 × 64 logical layout as her other phases. The emergency atlas rescale is gone; native jaw, blink and mouth effects remain.
- Denny's vest, back, trousers and boots now fit the standard native islands. Removed the special leg remap and alternate torso draw. The five cottage characters now use that same pixel grid: Walter keeps his spectacles, white whiskers, green cardigan and watch chain; Ruth her plum dress, apron and pinned bun; Will his striped shirt, braces and red neckerchief; the sergeant his frock coat, moustache and medals; the messenger his black coat, bowler and striped trousers. Fine noise and soft facial shading are removed. The old cottage generator delegates to the common painter.
- All nine nightclub people and both moods use the same native layout. Removed the separately cropped DJ portrait and its extra head render; his normal model head and head-attached headphones now render together.
- Native segment caps have their own painted elbow/knee swatches, keeping palm/sole islands at the actual extremities. A native baked-vertex regression reproduced the hand-coloured elbow before the fix. Road crew hard hats receive complete crown and brim faces; only the actual female passenger wears the corpse gown.
- Native coat/gown/bandage footprints stay within their texture islands. Father injury marks belong to the actual left-leg bandage; mirrored base legs are intact. The medium's veil retains a face opening.
- Elk sheets retain opaque padding around every complete native net, as required by the existing elk/fan coverage regression. Elk hide, wounds, skinned remains, rabbit fur and Minotaur/vulture materials use grouped pixel marks rather than photographed-looking surface grain. The vulture now selects its actual 256 × 512 material sheet, matching its native UV layer.
- Mother stages, cloth girl, the Goatman, Pekingese, intentional featureless figures, scarecrow and player-skin doubles were examined against their model definitions and retained where they already fit. The old generated-room/item materials are outside this character pass.

## Scope and preservation

155 entity sheets are accounted for below, including character variants and ancillary prop materials. The new review renders 109 human texture selections on their production model layers, from both sides, plus four Harrigan/coffin views and two creature grids (20 new native screenshots). Existing phase proofs additionally exercise all sixteen Mother skins, witch jaws and attacks, the cloth girl, all literary/family/stranger/killer states, all Ghosts poses, the nightclub morph, hotel gestures, actual Goatman window disguises and real socket clients/reconnects. Pure player-skin doubles continue to use the actual owner's standard/slim skin rather than a generated face.

Borrasca is deliberately not redesigned or reviewed in this pass. Its source, assets and registrations remain; existing regression jobs still run.

No new actor IDs, story sources, resolutions or endings. All personal progress, original bodies, finite originals, player edits, source texts, saved footage, recent Goatman exchange/knocking, watched-witch retreat and blindfold completion repairs remain. Layout 45 / protocol 72; 46 eligible sources / 35 required resolutions / at least two kinds / three House endings.

## Validation status

- Java compilation and initial build pass locally.
- All 766 PNGs decode with valid CRCs. Exact painting reproduction, opaque native base faces, integer pixel grid, source hashes and excluded/unrelated art checks pass.
- The fresh 20-view native pass completes after the joint-cap/helmet follow-up. Native coffin bounds, folded hands, seated knees, cap UVs and renderer-reuse resets pass. The cap regression fails on the earlier implementation and passes on the corrected native mesh. The five cottage follow-up sheets pass exact painting, UV, alpha and exclusion checks; their updated native page 17 remains required in the final exact-source run. Final complete-suite checks remain pending.
- The first exact-source matrix passed eleven jobs, including both actual clients/reconnects, but the full corpus crashed in Shallows word expiration. Native entity removal shifted the lookup being traversed. The follow-up collects due words before removal and adds a 128-word native regression with future words, unrelated displays and an original named actor. No prior case is removed or weakened.
- Complete exact-source twelve-job CI, 649 native gameplay cases, all earlier writing/package/GPU/97 architecture views and actual two-client/reconnect proofs are required. Pending results are not a verified release. Staged mesh views do not claim an uninterrupted ordinary playthrough.

## Per-sheet accounting

| Sheet | Finding / action | Model and review |
| --- | --- | --- |
| `arm_stump_bandage` | Ancillary prop/material; unchanged | Not a generated character |
| `borrasca_atlas` | Excluded by owner; unchanged | Borrasca |
| `caged_child` | Repainted on native face islands | player; `native-character-art-04.png` |
| `clap_ghost_girl` | Model-specific pixel net retained | ClapGhostModel; creature grid and four existing native stride views |
| `costume_scarecrow` | Intentional stitched sack, straw and clothing retained | CostumeModel / native scene review |
| `finale_materials` | Repainted native material islands | Minotaur / vulture; new creature grids and prior Minotaur views |
| `finale_witness` | Repainted on native face islands | player; `native-character-art-04.png` |
| `ghosts_barry` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_father_wanderly` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_john` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_john_hand` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_marjorie` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_marjorie_dinner` | Repainted on native face islands | ghosts; `native-character-art-04.png` |
| `ghosts_merry` | Repainted on native face islands | ghosts; `native-character-art-05.png` |
| `ghosts_sarah` | Repainted on native face islands | ghosts; `native-character-art-05.png` |
| `goatman` | Model-specific pixel net / eye layer retained | GoatmanFigureModel; creature grid, prior window and phase proofs |
| `goatman_eyes` | Model-specific pixel net / eye layer retained | GoatmanFigureModel; creature grid, prior window and phase proofs |
| `harrigan` | Repainted on native face islands | harrigan; `native-character-art-05.png` |
| `harrigan_dead` | Repainted on native face islands | harrigan; `native-character-art-05.png` |
| `harrigan_mourner_0` | Repainted on native face islands | harrigan; `native-character-art-05.png` |
| `harrigan_mourner_1` | Repainted on native face islands | harrigan; `native-character-art-05.png` |
| `harrigan_mourner_2` | Repainted on native face islands | harrigan; `native-character-art-05.png` |
| `holloway` | Repainted on native face islands | player; `native-character-art-05.png` |
| `hotel_bartender` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `hotel_dancer` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `hotel_dancer_dress` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `hotel_hose` | Ancillary prop/material; unchanged | Not a generated character |
| `hotel_pianist` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `hotel_porter` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `hotel_scarred_guest` | Repainted on native face islands; historical skin alias | hotel; `native-character-art-06.png` |
| `lake_boy` | Repainted on native face islands | player; `native-character-art-06.png` |
| `lake_congregant` | Repainted on native face islands | player; `native-character-art-06.png` |
| `lake_congregant_1` | Repainted on native face islands | player; `native-character-art-07.png` |
| `lake_congregant_2` | Repainted on native face islands | player; `native-character-art-07.png` |
| `lake_congregant_3` | Repainted on native face islands | player; `native-character-art-07.png` |
| `lake_house_canoe` | Ancillary prop/material; unchanged | Not a generated character |
| `lake_house_canoe_chipped` | Ancillary prop/material; unchanged | Not a generated character |
| `lake_house_canoe_scraped` | Ancillary prop/material; unchanged | Not a generated character |
| `lake_house_tanks` | Ancillary prop/material; unchanged | Not a generated character |
| `lake_preacher` | Repainted on native face islands | player; `native-character-art-07.png` |
| `lake_witch` | Repainted on native face islands | witch; `native-character-art-07.png` |
| `lake_witch_memory` | Repainted on native face islands | witch; `native-character-art-07.png` |
| `literary_bar_denny` | Repainted on native face islands | literary; `native-character-art-07.png` |
| `literary_bar_hollis` | Repainted on native face islands | literary; `native-character-art-07.png` |
| `literary_bar_marla` | Repainted on native face islands | literary; `native-character-art-08.png` |
| `literary_bar_russ` | Repainted on native face islands | literary; `native-character-art-08.png` |
| `literary_bar_wade` | Repainted on native face islands | literary; `native-character-art-08.png` |
| `literary_brother` | Repainted on native face islands | literary; `native-character-art-08.png` |
| `literary_camera` | Ancillary prop/material; unchanged | Not a generated character |
| `literary_coffin_woman` | Repainted on native face islands | literary; `native-character-art-08.png` |
| `literary_crew_0` | Repainted on native face islands | corpse; `native-character-art-08.png` |
| `literary_crew_1` | Repainted on native face islands | corpse; `native-character-art-08.png` |
| `literary_crew_2` | Repainted on native face islands | corpse; `native-character-art-08.png` |
| `literary_crew_3` | Repainted on native face islands | corpse; `native-character-art-09.png` |
| `literary_dancer` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_elk` | Repainted native animal islands | Elk model; new creature grid and existing elk/fan/dead poses |
| `literary_elk_carcass` | Repainted native animal islands | Elk model; new creature grid and existing elk/fan/dead poses |
| `literary_elk_carcass_skinned` | Repainted native animal islands | Elk model; new creature grid and existing elk/fan/dead poses |
| `literary_elk_wounded` | Repainted native animal islands | Elk model; new creature grid and existing elk/fan/dead poses |
| `literary_family_child` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_family_child_changed` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_family_father` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_family_father_changed` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_family_mother` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_family_mother_changed` | Repainted on native face islands | literary; `native-character-art-09.png` |
| `literary_fan` | Ancillary prop/material; unchanged | Not a generated character |
| `literary_father` | Repainted on native face islands | literary; `native-character-art-10.png` |
| `literary_father_bandaged` | Repainted on native face islands | literary; `native-character-art-10.png` |
| `literary_father_mangled` | Repainted on native face islands | literary; `native-character-art-10.png` |
| `literary_guest_0` | Repainted on native face islands | corpse; `native-character-art-10.png` |
| `literary_guest_1` | Repainted on native face islands | corpse; `native-character-art-10.png` |
| `literary_guest_2` | Repainted on native face islands | corpse; `native-character-art-10.png` |
| `literary_guest_3` | Repainted on native face islands | corpse; `native-character-art-10.png` |
| `literary_herd_eyes` | Ancillary prop/material; unchanged | Not a generated character |
| `literary_killer` | Repainted on native face islands | literary; `native-character-art-10.png` |
| `literary_lady` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_lake_tess` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_lake_wax` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_mother` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_old_man` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_paw_messenger` | Repainted native islands; original period outfit retained | literary; `native-character-art-17.png` |
| `literary_paw_ruth` | Repainted native islands; original period outfit retained | literary; `native-character-art-17.png` |
| `literary_paw_sergeant` | Repainted native islands; original period outfit retained | literary; `native-character-art-17.png` |
| `literary_paw_walter` | Repainted native islands; original period outfit retained | literary; `native-character-art-17.png` |
| `literary_paw_will` | Repainted native islands; original period outfit retained | literary; `native-character-art-17.png` |
| `literary_plain_boy` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_quiet_walker` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_red_figure` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_silhouette` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_silhouette_1` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_silhouette_2` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_silhouette_3` | Intentional featureless silhouette retained | Literary cast; all six silhouette phases in native literary review |
| `literary_snow_rabbit` | Pixel fur; native eyes, nose and inner-ear layer retained | Native RabbitModel / HolyRabbitRenderer; creature and Holy Rabbit views |
| `literary_stranger_0` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_stranger_1` | Repainted on native face islands | literary; `native-character-art-11.png` |
| `literary_stranger_2` | Repainted on native face islands | literary; `native-character-art-12.png` |
| `literary_stranger_3` | Repainted on native face islands | literary; `native-character-art-12.png` |
| `literary_visitor` | Repainted on native face islands | literary; `native-character-art-12.png` |
| `minotaur_materials` | Repainted native material islands | Minotaur / vulture; new creature grids and prior Minotaur views |
| `mother_of_strays` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_1` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_10` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_11` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_12` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_13` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_14` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_15` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_2` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_3` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_4` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_5` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_6` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_7` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_8` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_of_strays_9` | Model-specific pixel net retained | MotherModel; all 16 stages in existing character phase views |
| `mother_pekingese` | Model-specific pixel net retained | MotherPekingeseModel; new creature grid |
| `nightclub_dancer_braids` | Repainted on native face islands | club; `native-character-art-12.png` |
| `nightclub_dancer_braids_despair` | Repainted on native face islands | club; `native-character-art-12.png` |
| `nightclub_dancer_brick` | Repainted on native face islands | club; `native-character-art-12.png` |
| `nightclub_dancer_brick_despair` | Repainted on native face islands | club; `native-character-art-12.png` |
| `nightclub_dancer_copper` | Repainted on native face islands | club; `native-character-art-12.png` |
| `nightclub_dancer_copper_despair` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_female` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_female_despair` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_male` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_male_despair` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_mustard` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_mustard_despair` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_silver` | Repainted on native face islands | club; `native-character-art-13.png` |
| `nightclub_dancer_silver_despair` | Repainted on native face islands | club; `native-character-art-14.png` |
| `nightclub_dancer_vest` | Repainted on native face islands | club; `native-character-art-14.png` |
| `nightclub_dancer_vest_despair` | Repainted on native face islands | club; `native-character-art-14.png` |
| `nightclub_dj` | Repainted on native face islands | club; `native-character-art-14.png` |
| `nightclub_dj_despair` | Repainted on native face islands | club; `native-character-art-14.png` |
| `nightclub_dj_head0490` | Repainted compatibility sheet; portrait crop no longer used | Production DJ now uses the ordinary club head and headphones |
| `paw_clutch` | Ancillary prop/material; unchanged | Not a generated character |
| `seance_daughter` | Repainted on native face islands | seance; `native-character-art-14.png` |
| `seance_father` | Repainted on native face islands | seance; `native-character-art-14.png` |
| `seance_medium` | Repainted on native face islands | seance; `native-character-art-14.png` |
| `seance_mother` | Repainted on native face islands | seance; `native-character-art-15.png` |
| `stanley_bartender` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `stanley_dancer` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `stanley_dancer_dress` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `stanley_pianist` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `stanley_porter` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `stanley_scarred_guest` | Repainted on native face islands | hotel; `native-character-art-15.png` |
| `trailer_child_0` | Repainted on native face islands | cousin; `native-character-art-15.png` |
| `trailer_child_1` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_2` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_3` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_4` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_5` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_6` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_7` | Repainted on native face islands | cousin; `native-character-art-16.png` |
| `trailer_child_8` | Repainted on native face islands | cousin; `native-character-art-16.png` |
