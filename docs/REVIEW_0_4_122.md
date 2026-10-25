# October 25 review

Reviewed development head `062f6a3ea355f014b654855b4a14a57b4881e757` (23 commits after `b6d74646`) and the unfinished environment draft. Development run [40189102388](https://github.com/Knaitoe/the-oldest-house/actions/runs/40189102388) passes all twelve jobs. The earlier environment draft passed ten jobs, with failures in the full server suite and live-client expedition; it was not ready to ship.

## Findings and corrections

- The old domestic upgrade regression still expected a hallway barrel. The owner explicitly removed those barrels. It now checks matching spruce paneling at all three cells, retaining cache, drop, companion and evidence preservation checks.
- The environment draft crowded the notebook approach with a chair and hamper. The two live readers also walked to the same point and could displace one another after closing the paper. The furniture now leaves two clear routes; each client walks a separate route with ordinary input, gravity and collision. The server waits for both grounded bodies before moving either. No walking, source, actor-identity, CCTV-pixel or reconnect requirement is removed.
- The added diner capture was inside the co-located peer’s head. The readers now stand separately for that capture.
- Karen’s projection settings were only consulted when initially loading pictures. Changing them during a visit retained the old texture list and could leave a snapshot queued. Preference changes now release those textures, invalidate an old load and cancel a disabled snapshot. The native client check exercises loaded-slide and queued-snapshot cleanup.
- First environment cutaways clipped their rear corners in the GUI depth range. Their depth is now centred; this is a preview correction, not a missing-world geometry repair.

## Review scope

Read the recent gameplay and configuration changes and inspected native views of Borrasca, its cast, the Barrett house, Winchester, the Wheel, and the environment draft. The newer rooms have distinct finishes and authored layouts; full-bright overview images are not proof of lighting or of every room’s visual quality. In particular, the fixed filming routes still leave large open areas in the Barrett house, and the full-height upper floors are not visible in its standard ground-floor view.

The unfinished diner composition was tightened with two smaller café settings, distributed housed lights, finished furniture and a coherent roadside site. The waitresses use the established cast painter. Existing texts, finite originals, personal progress and source/account anchors are preserved.

## Validation

All ten local checks pass: uncanny environments/collision, common cast painting, character-art catalog and unchanged-painting checks, Barrett home, Borrasca cast, camp session, tract house, Wheel, Winchester and Borrasca trail assets. The combined source declares 737 native cases. Its native compilation, both-client walking/CCTV/reconnect run, refreshed environment images and full twelve-job matrix must complete before calling this combined candidate verified.
