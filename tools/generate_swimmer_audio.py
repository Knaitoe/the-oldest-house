"""Original sounds for the last swimmer (0.4.227): a swimmer's turn against the tiles, heard through water; the cover's reel
ratcheting a lane further out; and a wet footstep pressing the cover up from underneath.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root.
Existing files are kept, so the committed sounds only change when they are deleted first.

The office radio (swimmer.radio) has its own sound event with no sound in it: it ships silent. A resource pack of the player's
own may fill it (assets/the_oldest_house/sounds.json with "swimmer.radio": {"replace": true, "sounds": [...]}); nothing is
committed here.
"""
import json
import sys
from pathlib import Path

# The cues, their subtitles and how far they carry: the check below needs nothing but these and the files.
ROWS = {'turn': ('Swimmer turns at the wall', 24), 'haul': ('Pool cover reels out', 16), 'step': ('Wet footstep under the cover', 24)}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/swimmer/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'swimmer.{name}'] == {'sounds': [{'name': f'the_oldest_house:swimmer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.swimmer.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.swimmer.{name}'] == subtitle, name
    assert sounds['swimmer.radio'] == {'sounds': [], 'subtitle': 'subtitles.the_oldest_house.swimmer.radio'}, 'the radio ships silent'
    assert not (ASSET_ROOT / 'sounds/swimmer/radio.ogg').exists(), 'no recording of the song is committed'
    print('Verified the pool\'s three original cues and its silent radio.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(227)


def turn():
    # A flat hand and a heel on tile under water: a dull double knock, rounded off by the water, and a swirl after it.
    length = 1.1
    n = seconds(length)
    out = np.zeros(n)
    for at, gain in ((0.0, 1.0), (0.07, 0.7)):
        m = seconds(.12)
        knock = np.sin(2 * np.pi * 180 * times(m)) * envelope(m, .002, .035) + band(rng.standard_normal(m), 200, 1200) * envelope(m, .001, .02) * .6
        place(out, knock * gain, at + .02)
    swirl = band(rng.standard_normal(n), 120, 900) * adsr(n, .15, .6) * (.5 + .5 * np.sin(2 * np.pi * 3 * times(n))) * .25
    return band(out + swirl, 80, 1600)


def haul():
    # The reel turns a lane's worth: a ratchet's clicks, the cover's fabric dragging over the coping, a slap of water.
    length = 1.0
    n = seconds(length)
    out = np.zeros(n)
    for i in range(7):
        m = seconds(.02)
        place(out, band(rng.standard_normal(m), 1500, 7000) * envelope(m, .0005, .006) * .7, .05 + i * .1)
    drag = band(rng.standard_normal(n), 300, 2500) * adsr(n, .1, .3) * .3
    m = seconds(.25)
    slap = band(rng.standard_normal(m), 150, 1500) * envelope(m, .004, .08) * .5
    place(out, slap, .72)
    return out + drag


def step():
    # Something presses up under the cover: the fabric stretching, a heavy wet slap from beneath, water running off.
    length = 1.2
    n = seconds(length)
    out = np.zeros(n)
    m = seconds(.3)
    stretch = band(rng.standard_normal(m), 60, 500) * adsr(m, .2, .05) * .5
    place(out, stretch, 0)
    m = seconds(.16)
    slap = band(rng.standard_normal(m), 90, 900) * envelope(m, .003, .05) + np.sin(2 * np.pi * 70 * times(m)) * envelope(m, .002, .06) * .8
    place(out, slap, .28)
    m = seconds(.8)
    run = band(rng.standard_normal(m), 1200, 6000) * adsr(m, .05, .6) * .15
    place(out, run, .36)
    return band(out, 50, 6500)


SOUNDS = {name: ({'turn': turn, 'haul': haul, 'step': step}[name], subtitle, reach) for name, (subtitle, reach) in ROWS.items()}


def main():
    folder = ASSETS / 'sounds/swimmer'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, subtitle, reach) in SOUNDS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(make()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'swimmer.{name}',
               {'sounds': [{'name': f'the_oldest_house:swimmer/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.swimmer.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.swimmer.{name}', subtitle)
    # The radio: an empty event, for the player's own resource pack to fill.
    update(ASSETS / 'sounds.json', 'swimmer.radio', {'sounds': [], 'subtitle': 'subtitles.the_oldest_house.swimmer.radio'})
    update(ASSETS / 'lang/en_us.json', 'subtitles.the_oldest_house.swimmer.radio', 'Radio plays in the office')


if __name__ == '__main__':
    main()
