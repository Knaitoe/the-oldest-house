# Window entry and paw, 0.4.114

The October 22 review clarifies the earlier crawl request: the floor crawl should remain, the Goatman must enter disguised as a cousin, and the child still sticks on the porch after knocking. This correction supersedes the standing recovery and monster-entry previews in the first 0.4.114 draft.

The route now reads the actual collision surface under the porch tread instead of aiming at a fixed half-block height inside saved or player-edited wood. The new native regression starts the original child below a full-height first tread, climbs both steps, delivers all six post-circuit knocks, saves the same body and descends to the bathroom window. The earlier fixed-height code remained at `(0.5, 1, -52.5999)` for over 300 native ticks in this case; the unmodified finished porch passed its separate seven-case baseline.

The Goatman keeps a child/cousin disguise through the approach, sash and complete indoor route. A monster that is watched waits outside until it can change unseen. The finite circuit disguise deck still never repeats a child; if it is exhausted, admission uses the original body's familiar cousin identity. Admission neither replaces the actor nor changes its underlying saved identity. Native tests cover watched refusal, unseen admission, exhausted-deck save/reload and a constant child visage during the actual route.

The original impostor reaches, rises toward the outer ledge, hoists onto the sill, pulls through with articulated elbows and knees, then lowers directly into the floor crawl. It crawls through the real bathroom doorway and aisle to the sleeping floor. Opposite hands and knees alternate from actual distance travelled; planted tips stay at the floor. The window/crawl joint transition is continuous. Tracked, saved state retains the actor UUID, position, low collision, disguise and remaining route through mid-sash and indoor entity reloads. Every existing pixel texture stays unchanged; ordinary cousin walking profiles reset the additional joints.

Admission retains route priority until floor arrival; dawn waits even after a delayed exterior circuit. Survival and haunting outcomes, original identities, physical collision, all seven demands and every exterior circuit remain authoritative. The paw thumb's previous correction still opposes across the palm and resets between wishes.

## Verification

The declared corpus is 717 gameplay cases, including the existing 95 multiplayer cases. The new isolated porch case also runs in the default full suite and focused Goatman CI job (`the_oldest_house_goatman_entry`). The existing delayed-admission case now requires all four entry quarters, actual low collision, native sash and indoor reloads, continuous cousin disguise and floor crawl completion before dawn; its earlier independent cue, route, identity and outcome assertions remain.

Nine native GPU pages show two cousin disguises at reach, sill, pull, lowering and both floor-crawl strides, plus the paw open, half curled and closed, in two views. The proof samples 101 window poses and 65 crawl poses, measures native hand/foot floor positions, checks the continuous transition and ordinary-pose reset, and retains the thumb checks. Run it independently with `gradle runClient -PwindowEntrySmoke`; standard native CI requires every page and its marker.

Local native verification passed all three focused admission cases: delayed complete window/floor entry, the raised-tread post-knock route and watched/exhausted-deck disguise behavior. An independent check of the shipped native model classes passed 101 window poses, 65 crawl poses, the continuous transition, floor-tip bounds and ordinary cousin reset.

Exact-source validation remains pending. Prior source `111c8231` passed focused gameplay and multiplayer jobs; its CCTV phase-nine head readback and full-suite chunk serialization remain unresolved. Those earlier results do not verify these corrected poses/routes. Require all twelve exact-source jobs, native GPU pages, prior 99 architecture views, both actual clients and reconnects before calling this release verified.

Version 0.4.114 / protocol 88 / labyrinth layout 46 / manor layout 2. Witness remains 52 sources / 39 required / two kinds / three endings. No source, geometry migration, actor replacement, inventory replenishment or PNG repaint is introduced. All earlier Claude, Borrasca, hotel, home, portrait and uncanny-room work remains.
