# Cable rooms, the yard and the picture (0.4.313)

This candidate continues the owner's Google Docs review: finish the fridge's hair choice, extend the early cable puzzle into its deep rooms, and build two ordinary outdoor connectors.

## The picture on the fridge

The drawing's hair stays a preview on the kitchen table. Hanging the picture commits the last crayon used on the hair, from the existing six natural hair colours, and broadcasts the resulting body to everyone. The sheet on the fridge is the authority. Play colours keep the account's default hair.

The bathroom's added hair geometry reads the current House body colour directly. It no longer depends on a cached sample of the body's texture, whose identifier stays the same when repainted. The account remembers filling in the hair without naming a colour. Native coverage extends the real kitchen sequence, checks separate readers and observers, and round-trips the saved read.

## The three cable rooms

`TENSION_ROOMS`, slot 188, is a deep gray puzzle in the existing TENSION family. The normal shared puzzle rhythm offers it from the strange depths after the early Tension Room is solved. Layout 97 appends it; existing rooms are left standing.

One continuous cable runs through three utility rooms. A marked counterweight and entry wheel stand in the first; the second has a winding station and a door on a closer; the third has the last drum, pin and sprung platform. Real windows expose neighboring mechanisms. Taking up the entry wheel's four turns and the middle drum's two holds the door open. The far drum's two turns draw the last pin. A crouching hand pays out. There is no time limit or consumed material.

Paying out can release the door while someone is beyond it. The far-side push-bar opens a return without changing the cable, and the closer waits until the walker and doorway are clear. Living occupants block weight movement or door closure before any turn is committed. Solid obstructions or inadequate rider headroom block the platform. Actual living riders are carried without replacement. Once caught, the platform remains up even if the cable is paid out; the cable and solved state are saved. No Witness credit.

## Two more ways through the yard

`YARD_DITCH`, slot 189: a narrow drainage cut between eight-block hedges, boot-deep water over stone slabs, marker posts, and a raised dry concrete culvert under a road. The pipe is really one block high. Using a lip or crouching there puts the actual player into a crawl; ordinary movement crosses it in either direction. Server and client agree on the pose. The original pose returns outside the pipe or on departure; an arrival inside the pipe reacquires the crawl. Traffic is placed above it. Both ends retain a way back.

`YARD_CLOTHESLINE`, slot 190: three linen curtains across firm ground, hung between posts. A hand or brushing body gathers a curtain to its post for everyone. It falls back after everyone clears it. Replaced blocks and removed attachments are preserved. Quiet, positional following footsteps cease beyond the final sheet. Linen and concrete are generated at native texture resolution; the following steps are original synthesized audio with a positional subtitle.

The turnaround, ditch and clothesline share the yard's previous total deal weight. They are ordinary outdoor gray places with no story credit. The new connectors are built whole by their own builder, use the normal door registry and are exported for native architecture views. Protocol 129 adds the crawl pose payload.

Operator entries: `/ghosthouse labyrinth place tension_rooms`, `/ghosthouse labyrinth place yard_ditch`, `/ghosthouse labyrinth place yard_clothesline`.

## What the previous run found

Run [42207821963](https://github.com/Knaitoe/the-oldest-house/actions/runs/42207821963), source `f709f31`, failed four exploration assertions: heavy/light attributes, seven personal stair landings, the receding door, and the fourth parlour's scale. Those fixtures started fixed-delay measurements without a native-readiness wait. The full run also failed the sconce test's fixed-delay check for the warm flame. Its measurements now begin on the same readiness condition. Their assertions and measured intervals remain; readiness now has its own bounded, named wait before the measurement. This repair changes no gameplay.

The quick native gate selects the preceding counter, cable and rail cases, those five hazard cases, the kitchen and body cases, and four new cable/yard cases. Its expected pass count is derived from the selection. Complete namespaces still run independently.

## Validation

Local compilation and all 17 selected native cases pass, including the five previously failing hazards, both hair cases, the original counter/cable/rail cases, both deep cable cases and both yard passages. Asset/model checks, all 3,416 PNGs, Java syntax parsing and the 17 existing log-analysis unit tests pass. The full exploration namespace also passes all 113 required cases, and the packaged JAR passes the branding/package check. Client views and exact-commit CI remain pending.

The remaining yard hazards and regrouping from private story exits are separate work. These passages do not implement the Supper, Tall Grass, Growing Things or Last Crossing stories.
