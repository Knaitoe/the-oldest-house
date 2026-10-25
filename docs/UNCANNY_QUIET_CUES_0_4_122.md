# Quiet uncanny cues — 0.4.122

The owner’s playtest shows the diner’s atmospheric text staying on screen throughout the encounter, and confirms the same problem across all six uncanny rooms. Remove the permanent room-description HUD and every repeating server action-bar narration. The actual room, its sounds and its visible changes carry those cues.

This covers the painted house’s directional creaks, the diner’s frost and rattling knives, the tunnel’s red/green commentary, the bedroom’s camera/whistling commentary, the cellar’s heartbeat/fold commentary and the foyer’s floodlight, buzzer and latch commentary. Physical tunnel signs, signal lights, cameras, floodlight changes, source papers, NPC dialogue, deliberate interaction responses and once-only failure feedback remain.

Lower ambient sound levels and leave gaps: creaks every 8–14 occupied seconds while a figure is at the edge of sight; rattles every 9–17 seconds; bedroom calls every 10–18 seconds; the buzzer every 9–16 seconds. The tunnel tone sounds only when its signal changes. Cellar heartbeats still follow actual new folds, quietly; physical lamp/latch sounds are quieter too. Sound schedules use each reader’s existing occupied clock, pause with reading menus and do not advance any story or grant progress.

Integrate on the latest development branch, `feature/borrasca-four-visits-0.4.108`, retaining its `52ed7cf4` depot-rust update and the reviewed environment, hallway/note and character work through `63aebc5b`. No source text, geometry, original item, actor identity, hitbox, Witness quota, layout or protocol changes. The 737 declared native cases and existing complete checks remain required; candidate validation is pending.
