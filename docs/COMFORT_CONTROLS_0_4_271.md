# One rule for visual comfort (0.4.271)

The seventh of the owner's quality-of-life notes of 2 November (docs/OWNER_QOL_NOTES_2026_11_02.md):

> Consistent visual-comfort controls. Several scenes respect vanilla effect settings already. Make that behavior consistent:
> `WayOutClient`, for example, clamps the effects multiplier to at least `0.3`, and its ending static bypasses that multiplier. A
> player selecting zero should get predictable results for cosmetic effects.

Code: `client/Comfort`, read by every client effect below; `tools/verify_comfort.py` in the quick gate.

## The rule

Vanilla's two Accessibility sliders decide everything cosmetic:
- **Distortion Effects.** Every shake, wobble, tilt and tumble of the camera, every flash, and every static, snow or grain laid over
  sight scales with it. At 0 they are gone; at 100% they are as authored. Nothing has a floor under it.
- **FOV Effects.** Any narrowing of sight that is a feeling rather than a lens scales with it.

Some effects are what the scene needs the reader to see, or not see. Those do not scale:
- the dark of the covered well, a blindfold, the dark of the elk's drag;
- the fades between places, the cards' words;
- a viewfinder or a lens: the camcorder, the plains camera, the work print's viewfinder;
- a camera held still for a take, the Goatman's bed view;
- a frame of the reader's own footage.

## What changed

- **WayOutClient (the way out).** The 0.3 floor is gone, and the ending's static now scales like the rest. At zero the static is
  gone, and the line on black still comes.
- **Now following the setting.** Effects that ignored it before:
  - the lake phone's tumble into the water (the spin and the swing scale; the fall from sky to water does not);
  - the wobble of a Ghosts take;
  - the canoe lifted at Movie Night;
  - the jolts of the elk's drag;
  - the polaroid camera's flash;
  - the snow at the end of dead air;
  - the work print's grain;
  - the one-pixel shiver of the Goatman's demands.
- **Settings read through Comfort.** These already followed the setting and are unchanged in effect:
  - the uncanny rooms' pulse, wash and lurch;
  - the caver's shake and wheeze;
  - the staircase and scene shakes;
  - the cabin storm;
  - the hazards' tilt;
  - the backseat sway;
  - the hotel flash;
  - grounding;
  - Borrasca's rising dark.
- **The caver's narrowing of sight.** It now follows FOV Effects, not neither.
- **The clapping game's turned-around view.** It now follows Distortion Effects, like every other camera motion. It used FOV Effects
  before.

## Kept honest

`tools/verify_comfort.py` fails the quick gate if any source except `client/Comfort` reads either slider directly, or puts a floor
under `Comfort.effects()` or `Comfort.fov()`. The native client proof runs as before.
