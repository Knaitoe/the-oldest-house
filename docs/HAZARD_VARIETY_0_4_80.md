# Gray hazard variety — 0.4.80

Both owner proposals are integrated on the current playtest branch, after Claude's Last Stop corrections and the five named cast skins. Layout 41 / protocol 53; matching clients and server are required. Exact-source validation is pending.

Sixteen new gray slots append after 77. No earlier slot, discovered link, saved route, actor, original paper or Witness resolution changes. Existing hazard presets remain 28/42/54 percent with two visits of spacing; sibling layouts replace a family's choice rather than doubling its weight. Shared routes choose their sibling deterministically. Construction uses the normal depth-gated, bounded `BuildBlocks` slice queue and retains prior saved carve checkpoints.

| Slot | Map | Depth | Native cost and way through |
| --- | --- | --- | --- |
| 78 | Cold store | 6 | Forty-block locker, frosted crates and hooks. Native frost and slowing; five heat lamps eight blocks apart; nearby readers freeze at half rate. The compressor opens its latch for ten occupied seconds in each thirty-second cycle. Freezing damage cannot take health below three hearts. |
| 79 | Listing hall | 6 | Ship plating, rails and shallow wet bilges. Shared slow listing slides bodies and dropped items; crouching or holding a rail secures footing. Level moments permit a crossing. Local camera roll respects screen effects. |
| 80 | Quiet hall | 6 | Real sculk sensors beneath custom boards hear native vibrations. Six activations fill a shared meter, darken the lamps and send a harmless tall walker down the hall. Each reader it passes returns to the entrance. |
| 81 | Unwatched maze | 10 | Six by six cells. A panel exchanges one closed and one open edge only when both are unseen and clear. Every resulting arrangement stays connected. Native chalk marks travel with the panel, including their facing and arrow. |
| 82 | Mirror hall | 10 | Translucent mirror glass, mirrored furniture and three doors. Each reader receives only their own delayed native player model and skin, first one second late, then two. At the doors the reflection waits at the reader's real exit; wrong doors loop only that reader. |
| 83 | Heavy and light | 10 | Real transient gravity, jump, speed and fall-damage modifiers. Low ledges in the heavy half; floating jumps, a central down-draught, a net and supported recovery ladders in the light half. |
| 84 | Nested rooms | 14 | Four repeated parlours, unchanged furniture and increasingly small openings. Native scale goes through 1/.7/.5/.34. The last opening is in the skirting. Leaving restores scale over three seconds; death and logout clear it immediately. |
| 85 | Counting stair | 14 | Seven real landings with identical “4” plaques. Only the seventh personally traversed landing has a true door. Wrong doors return that reader to the top and restart their count. No counter appears on screen. Hunger costs accompany the climb. |
| 86 | Bent cistern | 6 | A winding channel steps down and up; three ceiling vaults contain real air pockets. |
| 87 | Spiral walkway | 10 | A rising spiral around a pit, three deliberately cut outer gaps, outward pulls and supported recovery ladders. |
| 88 | Corner pressure | 14 | An L-bend closes from the corner outwards. Closing cells wait for bodies and objects to clear, and the central retreat lane remains open. |
| 89 | Receding hall | 6 | A real far door and partition move away while approaching and come closer faster than the reader when backing away. Its stable door identity keeps all saved deals. No instruction reveals the trick. |
| 90 | Last sconce | 10 | One sconce follows the strongest warm portable flame. The strongest flame survives; lesser flames eventually extinguish. |
| 91 | Watched threshold | 10 | A door changes wall only when all source and destination cells are unseen and clear. Actual spectator and borrowed cameras count. Watching two walls can pin the threshold. |
| 92 | Duplicate rooms | 14 | Three furnished rooms in a row. One omits its clock. Only that room's door is real; wrong doors return to the start without changing the route stack. |
| 93 | Turning gravity | 14 | Four directions of pull, turning each twenty occupied seconds. A cross of ledges, shallow trenches, rails and supported ladders offers shelter. |

Shared clocks pause while no eligible reader occupies a room. Spectators watch but cannot advance puzzles. Runtime engines belong to their server and saved origin, so test servers and replacement worlds do not share state. Environmental freezing, falls, drowning and pressure damage in these maps cannot reduce health below three hearts. Ordinary unrelated damage retains its native behavior.

Movement and scale use distinct transient modifier IDs. Leaving, death, logout, clone, login and server shutdown remove only the owned modifiers; unrelated equipment and mod effects remain. Private mirror packets use short leases, clear on exit and logout, and spawn no networked reflected entity.

`tools/generate_hazard_assets.py` draws 22 new 16-pixel environment textures and one 128×64 faceless walker atlas. Twenty block kinds and their two stages have collision-matched models. The five existing Last Stop atlases are retained. Its native cast proof now compares the cast's capitalized state keys with lowercase texture filenames correctly.

Required validation: every earlier native case and proof; the new exploration cases for native geometry, sensors, cameras, chalk, connected paths, temperature, personal counts and attribute cleanup; 87 architecture views; three hazard material/dark/mirror views; the three Last Stop cast views; package and texture checks; all twelve CI jobs and the actual connected-client/reconnect suite. No new Witness source: 43 sources / 33 resolutions / two kinds / three endings.
