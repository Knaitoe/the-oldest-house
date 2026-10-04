# House exit freeze — 0.4.40

The 0.4.39 playtest log shows the exit door completing on the server in 15 ms, while the client held the transition frame for 51.5 seconds. The hold spans vanilla's level switch, in which the client tears down the previous level's render sections.

The House interior is 4,064 blocks tall: 254 section layers against the Overworld's 24. At render distance 12 the client keeps 25 × 25 × 254 ≈ 158,750 render sections inside the House, and Minecraft 1.21.1 gives every section five GPU vertex buffers when it is created, even when it holds only air. Entering the House creates millions of GPU objects quickly; leaving deletes them in one frame, which the playtest's integrated graphics took about fifty seconds to do.

## Change

A client mixin on the render section defers its buffers. A section receives them, on the render thread, when its compilation is scheduled and it actually holds blocks; any other caller asking for a buffer still receives one. Empty air never allocates GPU objects, so the interior costs about as much as an ordinary level and leaving it releases only what was drawn. Released buffers are forgotten so a reused section creates fresh ones.

No server, world, gameplay or save data changes. Layout 32 / protocol 32.

## Verification

The client proof run loads the real render section class, fails if the mixin is absent, builds one section outside any level and checks that it starts with no buffers, creates all layers on request and releases every one. Transition logs now report the live section-buffer count with the held-frame time, so a playtest log shows directly whether the exit still holds.
