"""Original sounds for the House's yard (0.4.309): a station wagon idling, a car passing on the state route beyond the trees, a crow in
the dead tree, and the swollen creek at the end of the road. 0.4.358 adds the clothesline's quiet return: a wet sheet's weight and the
line snapping taut. Synthesized; no recordings. Existing files are kept.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'idle': ('An engine idles', 20),
    'traffic': ('A car passes beyond the trees', 48),
    'crow': ('A crow calls', 32),
    'creek': ('A creek runs high', 16),
    'follow': ('Footsteps', 12),
    'wind': ('Wind bends the trees', 24),
    # 0.4.358, the clothesline's quiet return: only the weight of wet fabric and the line, no footsteps.
    'wet_sheet': ('Wet linen sags on the line', 12),
    'line_snap': ('A washing line snaps taut', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/yard/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'yard.{name}'] == {'sounds': [{'name': f'the_oldest_house:yard/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.yard.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.yard.{name}'] == subtitle, name
    print(f'Verified the yard\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3090)


def idle():
    # Two seconds of a big engine ticking over: a lumpy low firing pulse, exhaust rumble, a faint belt whine. Loops cleanly.
    n = seconds(2.0)
    t = times(n)
    pulse = (np.sin(2 * np.pi * 13 * t) > .6).astype(float)
    rumble = band(rng.standard_normal(n), 40, 220) * (.5 + .5 * pulse)
    whine = np.sin(2 * np.pi * 820 * t) * .02
    return (rumble * .8 + np.sin(2 * np.pi * 39 * t) * .2 * pulse + whine) * np.ones(n)


def traffic():
    # A car going by on wet road: tyre hiss swelling and falling, the engine note dropping as it passes.
    n = seconds(4.0)
    t = times(n)
    near = np.exp(-((t - 2.0) / .7) ** 2)
    hiss = band(rng.standard_normal(n), 800, 6000) * near * .6
    f = 140 - 40 * np.tanh((t - 2.0) * 3)
    engine = np.sin(2 * np.pi * np.cumsum(f) / RATE) * near * .25
    return hiss + engine + band(rng.standard_normal(n), 60, 300) * near * .3


def crow():
    n = seconds(1.6)
    out = np.zeros(n)
    for at in (0.0, .5):
        k = seconds(.35)
        tk = times(k)
        f = 520 - 120 * tk / .35
        caw = sum(np.sin(2 * np.pi * np.cumsum(f * h) / RATE) / h for h in range(1, 6))
        place(out, band(caw + rng.standard_normal(k) * .4, 400, 3500) * envelope(k, .02, .12) * .6, at)
    return out


def creek():
    n = seconds(3.0)
    t = times(n)
    rush = band(rng.standard_normal(n), 300, 3000) * (.7 + .3 * np.sin(2 * np.pi * .7 * t))
    out = rush * .5
    for _ in range(30):
        k = seconds(.04)
        tk = times(k)
        place(out, np.sin(2 * np.pi * rng.uniform(600, 1800) * (1 + 2 * tk) * tk) * envelope(k, .002, .02) * .15, rng.uniform(0, 2.9))
    return out


def follow():
    out = np.zeros(seconds(.8))
    for at in (.02, .43):
        k = seconds(.18)
        t = times(k)
        sole = band(rng.standard_normal(k), 80, 700) * envelope(k, .008, .1)
        grit = band(rng.standard_normal(k), 1300, 5500) * np.exp(-t * 27)
        place(out, sole * .7 + grit * .25, at)
    return out


def wind():
    n = seconds(3.5)
    t = times(n)
    breath = band(rng.standard_normal(n), 70, 2400)
    leaves = band(rng.standard_normal(n), 1600, 5500) * (.45 + .2 * np.sin(2 * np.pi * 3.7 * t))
    return (breath * .65 + leaves * .18) * envelope(n, .35, .45)


def wet_sheet():
    # A sheet heavy with water gathered and let go: a dull, damp whump, the cloth slapping on itself, a few drips after.
    n = seconds(1.2)
    t = times(n)
    out = band(rng.standard_normal(n), 60, 420) * np.exp(-t * 9) * .9
    out += band(rng.standard_normal(n), 500, 2600) * np.exp(-((t - .06) / .05) ** 2) * .35
    for _ in range(6):
        k = seconds(.03)
        tk = times(k)
        place(out, np.sin(2 * np.pi * rng.uniform(900, 1600) * (1 + 3 * tk) * tk) * envelope(k, .002, .015) * .12, rng.uniform(.35, 1.1))
    return out


def line_snap():
    # The line pulled taut when the sheet falls back: a short click, then a low thrum along the cord dying quickly.
    n = seconds(.9)
    t = times(n)
    click = band(rng.standard_normal(n), 1500, 7000) * np.exp(-t * 160) * .6
    thrum = sum(np.sin(2 * np.pi * 96 * h * t) / h ** 1.4 for h in range(1, 7)) * np.exp(-t * 7) * .55
    return click + thrum * (1 + .15 * np.sin(2 * np.pi * 9 * t))


MAKERS = {'idle' : idle, 'traffic': traffic, 'crow': crow, 'creek': creek, 'follow': follow, 'wind': wind, 'wet_sheet': wet_sheet, 'line_snap': line_snap}


def main():
    folder = ASSETS / 'sounds/yard'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'yard.{name}',
               {'sounds': [{'name': f'the_oldest_house:yard/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.yard.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.yard.{name}', subtitle)


if __name__ == '__main__':
    main()
