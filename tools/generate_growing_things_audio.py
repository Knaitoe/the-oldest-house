"""Original sounds for Growing Things (0.4.350): the kitchen floor's boards lifting, the window frame giving, a spade in the ground, and
the shoots moving with nothing to move them.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'boards': ('Floorboards creak and lift', 24),
    'frame': ('A window frame splits', 24),
    'spade': ('A spade bites into the ground', 20),
    'shoots': ('Something rustles in the shoots', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/growing/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'growing.{name}'] == {'sounds': [{'name': f'the_oldest_house:growing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.growing.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.growing.{name}'] == subtitle, name
    print(f'Verified the Barretts\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9350)


def creak(length, start, end, loud=1.0):
    # A slow wooden groan: a rough pitched rasp whose pitch drifts, filtered to the body of a board.
    n = seconds(length)
    tk = times(n)
    f = start + (end - start) * tk / length + 6 * np.sin(2 * np.pi * 3.1 * tk)
    phase = 2 * np.pi * np.cumsum(f) / RATE
    rasp = np.sign(np.sin(phase)) * (0.5 + 0.5 * rng.random(n)) * .4 + np.sin(phase * 2) * .3
    rasp = band(rasp, 150, 1800)
    env = np.sin(np.pi * np.minimum(1, tk / length)) ** .8
    return rasp * env * loud


def crack(loud=1.0):
    k = seconds(.08)
    return band(rng.standard_normal(k), 800, 7000) * envelope(k, .0005, .03) * loud


def boards():
    # The floor groaning as it lifts, two creaks overlapping, a nail giving, then a board snapping up.
    n = seconds(2.6)
    out = np.zeros(n)
    place(out, creak(1.4, 90, 70), 0)
    place(out, creak(1.2, 120, 150, .8), .7)
    place(out, crack(.5), 1.1)
    place(out, crack(1.0), 1.9)
    k = seconds(.4)
    place(out, band(rng.standard_normal(k), 200, 2400) * envelope(k, .002, .25) * .6, 1.92)
    return out


def frame():
    # Old paint and putty giving: a high strained creak, the glass ticking, a split, and the glass settling.
    n = seconds(2.2)
    out = np.zeros(n)
    place(out, creak(1.0, 260, 340, .7), 0)
    for at in (.5, .8, .95):
        k = seconds(.02)
        place(out, np.sin(2 * np.pi * 4200 * times(k)) * envelope(k, .0002, .01) * .4, at)
    place(out, crack(1.0), 1.1)
    k = seconds(.9)
    tk = times(k)
    tinkle = sum(np.sin(2 * np.pi * f * tk) * np.exp(-tk * d) for f, d in ((3100, 9), (4700, 12), (5900, 14))) * .15
    place(out, tinkle * (rng.random(k) > .7), 1.15)
    return out


def spade():
    # A spade pushed into wet ground: the scrape of the blade going in, a soft thud of earth turned over.
    n = seconds(1.0)
    out = np.zeros(n)
    k = seconds(.35)
    tk = times(k)
    place(out, band(rng.standard_normal(k), 400, 3200) * np.minimum(1, tk / .05) * np.maximum(0, 1 - tk / .35) * .7, 0)
    k = seconds(.3)
    place(out, band(rng.standard_normal(k), 60, 400) * envelope(k, .005, .2) * 1.0, .4)
    return out


def shoots():
    # Stems brushing together with nothing moving them: a dry whispering that swells and passes.
    n = seconds(2.4)
    tk = times(n)
    hiss = band(rng.standard_normal(n), 1500, 7000) * (rng.random(n) > .55)
    swell = np.sin(np.pi * tk / 2.4) ** 2
    return hiss * swell * .6


MAKERS = {'boards': boards, 'frame': frame, 'spade': spade, 'shoots': shoots}


def main():
    folder = ASSETS / 'sounds/growing'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'growing.{name}',
               {'sounds': [{'name': f'the_oldest_house:growing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.growing.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.growing.{name}', subtitle)


if __name__ == '__main__':
    main()
