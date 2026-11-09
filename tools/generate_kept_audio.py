"""Original sounds for the Kept (0.4.410): the machine's hum, a cathode screen waking, tins knocked together in their crate, someone
weeping, the cell door opening onto glare, and the machine noticing.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from tools/. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'hum': ('Machines hum', 32),
    'wake': ('A screen wakes', 12),
    'tins': ('Tins knock together', 16),
    'weep': ('Someone weeps', 16),
    'glare': ('A door opens onto glare', 24),
    'notice': ('The machine notices', 48),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/kept/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'kept.{name}'] == {'sounds': [{'name': f'the_oldest_house:kept/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.kept.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.kept.{name}'] == subtitle, name
    print(f'Verified the Kept\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9410)


def held(n, attack, release):
    t = times(n)
    return np.minimum(1, t / attack) * np.minimum(1, (t[-1] - t) / release)


def hum():
    # A great many machines a long way off and close by: a mains drone and its harmonics, a slow beat between two of them, relays ticking.
    n = seconds(5.0)
    t = times(n)
    drone = sum(np.sin(2 * np.pi * f * t) * g for f, g in ((50, .5), (100, .3), (150, .18), (201.5, .12)))
    beat = .7 + .3 * np.sin(2 * np.pi * .4 * t)
    hiss = band(rng.standard_normal(n), 2000, 7000) * .05
    out = drone * beat + hiss
    for at in rng.uniform(.2, 4.6, 9):
        k = seconds(.02)
        place(out, band(rng.standard_normal(k), 1500, 5000) * np.exp(-times(k) / .004) * .5, at)
    return out * held(n, .6, .6)


def wake():
    # A cathode screen coming on: a thump, the high whine of the flyback rising into place, and static settling.
    n = seconds(1.2)
    t = times(n)
    thump = np.sin(2 * np.pi * 60 * t) * np.exp(-t / .05) * .8
    whine = np.sin(2 * np.pi * np.cumsum(9000 + 6700 * np.minimum(1, t / .3)) / RATE) * .08 * held(n, .05, .3)
    static = band(rng.standard_normal(n), 800, 6000) * np.exp(-t / .25) * .4
    return thump + whine + static


def tins():
    # Tins in straw, knocked together and shifted: dull hollow knocks, a few rolling, a rustle under them.
    n = seconds(1.6)
    out = band(rng.standard_normal(n), 2000, 6000) * .05
    for at in (.05, .2, .32, .6, .9, 1.1):
        k = seconds(.25)
        tk = times(k)
        f = rng.uniform(650, 1100)
        knock = (np.sin(2 * np.pi * f * tk) + .5 * np.sin(2 * np.pi * f * 2.7 * tk)) * np.exp(-tk / .05)
        place(out, knock * rng.uniform(.5, 1), at)
    return out


def weep():
    # Someone weeping quietly into their hands: breath catching in uneven sobs, a low voiced sound under each, then a long breath out.
    n = seconds(3.2)
    out = np.zeros(n)
    at = 0.1
    while at < 2.4:
        k = seconds(.28)
        tk = times(k)
        env = np.sin(np.pi * tk / .28) ** 2
        voice = np.sin(2 * np.pi * np.cumsum(210 - 40 * tk / .28 + 6 * np.sin(2 * np.pi * 7 * tk)) / RATE)
        breath = band(rng.standard_normal(k), 400, 2600)
        place(out, (voice * .25 + breath * .35) * env, at)
        at += .3 + rng.random() * .25
    k = seconds(.7)
    place(out, band(rng.standard_normal(k), 300, 1800) * held(k, .1, .5) * .3, 2.45)
    return out


def glare():
    # The door onto the glare: an iron latch, the door swinging, and a pressure of high air rising and then gone quiet.
    n = seconds(2.6)
    t = times(n)
    latch = band(rng.standard_normal(n), 1500, 5000) * np.exp(-t / .02) * .9
    swing = np.sin(2 * np.pi * np.cumsum(140 + 40 * np.minimum(1, t / .6)) / RATE) * np.exp(-t / .5) * .3
    air = band(rng.standard_normal(n), 3000, 9000) * np.minimum(1, t / 1.0) * np.exp(-np.maximum(0, t - 1.4) / .3) * .35
    tone = np.sin(2 * np.pi * 1760 * t) * np.minimum(1, t / 1.2) * np.exp(-np.maximum(0, t - 1.4) / .25) * .12
    return latch + swing + air + tone


def notice():
    # The machine noticing: everything at once, a deep swell from below, every screen's whine together, and a pulse like a held breath let go.
    n = seconds(4.0)
    t = times(n)
    swell = sum(np.sin(2 * np.pi * f * t) * g for f, g in ((41, .6), (61.5, .35), (82, .25))) * np.minimum(1, t / 1.5)
    whine = sum(np.sin(2 * np.pi * f * t) for f in (9100, 9230, 9405)) * .03 * np.minimum(1, t / 1.0)
    pulse = (np.sin(2 * np.pi * 1.1 * t) > .6) * band(rng.standard_normal(n), 60, 400) * .4
    return (swell + whine + pulse) * held(n, .8, .9)


MAKERS = {'hum': hum, 'wake': wake, 'tins': tins, 'weep': weep, 'glare': glare, 'notice': notice}


def main():
    folder = ASSETS / 'sounds/kept'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'kept.{name}',
               {'sounds': [{'name': f'the_oldest_house:kept/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.kept.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.kept.{name}', subtitle)


if __name__ == '__main__':
    main()
