"""Original sounds for After the People Lights Have Gone Off (0.4.253): Kelly's chair rolling on each of the ground floor's surfaces (the
foyer's large tiles, the living room's boards, the kitchen's small tiles, the hall runner), and the quieter sound of someone pulling
themselves along a floor in the dark, breathing.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

# The cues, their subtitles and how far they carry. The chair is heard through the house; the pulling only close by.
ROWS = {
    'wheels_tile': ('Wheels roll on tile', 14),
    'wheels_wood': ('Wheels roll on boards', 14),
    'wheels_kitchen': ('Wheels roll on kitchen tile', 14),
    'wheels_runner': ('Wheels roll on a runner, softly', 8),
    'pulling': ('Someone pulls along the floor, breathing', 6),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/people/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'people.{name}'] == {'sounds': [{'name': f'the_oldest_house:people/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.people.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.people.{name}'] == subtitle, name
    print(f'Verified the house\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(253)
LENGTH = 1.6


def roll(low, high, grain, joints, joint_low, joint_high, rumble):
    """A chair's wheels over a floor: the tyres' hiss in the floor's own band, a low rumble, and a tick each time a wheel crosses a seam."""
    n = seconds(LENGTH)
    t = times(n)
    out = band(rng.standard_normal(n), low, high) * grain * (.85 + .15 * np.sin(2 * np.pi * 2.1 * t))
    out += np.sin(2 * np.pi * rumble * t) * .08 * (1 + .3 * np.sin(2 * np.pi * 1.4 * t))
    out += band(rng.standard_normal(n), 60, 260) * .2
    for at in joints:
        k = seconds(.05)
        place(out, band(rng.standard_normal(k), joint_low, joint_high) * envelope(k, .001, .012) * .5, at)
        place(out, band(rng.standard_normal(k), joint_low, joint_high) * envelope(k, .001, .012) * .35, at + .07)
    return out * adsr(n, .12, .3)


def wheels_tile():
    # Large tiles: a hard, bright hiss and a clack at each wide grout line.
    return roll(900, 4200, .3, (.25, .78, 1.3), 1500, 4000, 70)


def wheels_wood():
    # Boards: warmer and lower, a hollow knock each time a wheel crosses a board's end, the odd creak.
    out = roll(250, 1600, .32, (.18, .5, .83, 1.16), 300, 1100, 55)
    k = seconds(.3)
    tk = times(k)
    place(out, np.sin(2 * np.pi * (480 + 90 * np.sin(2 * np.pi * 3 * tk)) * tk) * adsr(k, .05, .15) * .12, .9)
    return out


def wheels_kitchen():
    # Small tiles: a quick patter of seams, closer together than the foyer's, and a little brighter.
    return roll(1200, 5000, .26, [.12 + i * .17 for i in range(8)], 2000, 5200, 75)


def wheels_runner():
    # The runner: the tyres nearly silent on the pile, a soft brush and a low pressure through the boards beneath.
    n = seconds(LENGTH)
    t = times(n)
    out = band(rng.standard_normal(n), 200, 900) * .22 * (.8 + .2 * np.sin(2 * np.pi * 1.7 * t))
    out += band(rng.standard_normal(n), 50, 180) * .18
    return out * adsr(n, .2, .4)


def pulling():
    # In the dark: a body drawn along a floor by its arms, a slide of cloth on boards, a breath in and out with each pull.
    out = np.zeros(seconds(3.2))
    for i, at in enumerate((.1, 1.15, 2.2)):
        k = seconds(.55)
        tk = times(k)
        slide = band(rng.standard_normal(k), 300, 2400) * adsr(k, .08, .25) * (.6 + .4 * np.sin(2 * np.pi * 1.6 * tk)) * .45
        place(out, slide, at)
        b = seconds(.45)
        breath = band(rng.standard_normal(b), 500, 2000) * adsr(b, .12, .25) * .28
        place(out, breath, at + .5)
        palm = seconds(.06)
        place(out, band(rng.standard_normal(palm), 200, 900) * envelope(palm, .001, .015) * .5, at - .02)
    return out


MAKERS = {'wheels_tile': wheels_tile, 'wheels_wood': wheels_wood, 'wheels_kitchen': wheels_kitchen, 'wheels_runner': wheels_runner, 'pulling': pulling}


def main():
    folder = ASSETS / 'sounds/people'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'people.{name}',
               {'sounds': [{'name': f'the_oldest_house:people/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.people.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.people.{name}', subtitle)


if __name__ == '__main__':
    main()
