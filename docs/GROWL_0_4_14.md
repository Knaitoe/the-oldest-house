# The Oldest House 0.4.14

NeoForge 1.21.1, Java 21. Install the same version on client and server; network protocol remains 14.

## The Growl and the changing House

The Growl was already present: four original mono clips, far/below/near choices, sporadic depth-sensitive timing and a rare cellar wake. This update connects its actual playback path to House reactivity and widens its spatial variation.

Each eligible utterance makes **one 30% roll**. A successful roll queues one real change in the room where it was heard. The opportunity waits two seconds, then retries once per second until an unseen change is available. Retries do not roll again. Pending counts, selected/applied statistics and the room being changed survive saves. Multiple selected utterances retain separate counts. Logout pauses the owner's opportunities; `/oldesthouse reset` clears them.

- A closed ordinary gray door can acquire a different ordinary destination for the listener. Nobody may be watching either half, nobody may be within four blocks, and the door must be fully closed. Other explorers keep their own deals. Entry/return doors, manual test doors, destinations already offering a vignette, anomaly, finale or quiet stop, and Hillary's indicated door are preserved. The existing personal return stack continues to guide companions home.
- A portable light can physically retreat through the existing light-rearrangement system. Both endpoints must be unseen, and the light is conserved. Timed halls can use their existing unseen authored-light layouts.
- Six doors deep or farther, an owned or old explorer's unseen floor arrow can turn, or one string segment can disappear. The existing twelve-block player distance, observation checks and shared two-minute tampering cooldown remain in force. Another player's marks are not selected.
- A Growl heard in the ordinary manor, including the cellar cue, queues a subtle manor change: a painting, door, candle, chair or guest bed. Existing observation rules cover both the interior and its exterior proxy. Story containers and finite vignette rewards are unaffected.

Natural Growls remain absent from vignettes and quiet rooms. Pending opportunities pause while their owner is resting in one, inside an authored scene, outside the House, offline, or committed to a finale. A pending change can remain waiting when the original room has no eligible unseen object. Growls remain personal sounds; physical lights and marks are shared. The manor's existing morning changes and independent rare light/marker changes continue.

## Spatial delivery

| Voice | Source position | Native attenuation distance |
| --- | --- | --- |
| Far | 24–72 blocks horizontally; any direction, with vertical variation | 96 blocks |
| Below | 8–32 blocks down, with up to seven blocks of lateral drift per axis | 48 blocks |
| Near | 4–12 blocks behind, with varied bearing and height | 32 blocks |
| Cellar wake | 5.5–11.5 blocks below the floor, with small lateral drift | 48 blocks |

Ordinary utterances vary in volume from 0.78 to 1.0 and pitch from 0.88 to 1.06. The cellar remains a stronger, separately staged cue. Near voices remain a late-depth possibility; the existing far/below/near depth weights, random gaps and minimum gap of 150 seconds are unchanged. The original files remain mono and retain native positional attenuation. See the [NeoForge sound documentation](https://docs.neoforged.net/docs/1.21.1/resources/client/sounds/) for the engine's mono and variable-range requirements.

## Testing

With the House active, test a sound at your current position:

```text
/oldesthouse growl far
/oldesthouse growl below
/oldesthouse growl near
```

Each eligible manual sound uses the same 30% roll. To test a change without waiting for a successful roll, enter an active ordinary hall or manor and use `/oldesthouse growl change`. Look away from a closed ordinary door or leave an eligible light behind you. `/oldesthouse growl status` reports natural selections, actual changes, pending opportunities and the last change. `/oldesthouse growl basement` retains the staged cellar-wake fixture.

Eight new required GameTests cover native positional packets and spatial variance, actual mono Vorbis assets and attenuation definitions, one roll per actual playback, quiet/vignette exclusions, observed/open/nearby door protection, personal deals and exact returns, saved deferred opportunities, owned deep marks and cooldown, conservation of a moved light, and a real manor/proxy shift. Existing Growl timing, cellar, exploration, vignette and ending tests remain enabled.

This pass adds no vignette: Witness remains six of eight eligible stories (75%, rounded up) across at least two kinds. There are still three ending options. Phone in the Canoe remains the suggested next Indian Lake site.
