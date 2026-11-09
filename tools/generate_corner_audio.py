"""Original sounds for The House on the Corner (0.4.408): the iron gate on its hinges, a glass jar set down on a board, a door shut
softly somewhere deep in a house, cicadas in the afternoon heat, and the faint hum of lights that have no power.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'gate': ('An iron gate creaks', 16),
    'jar': ('Glass clinks', 8),
    'shut': ('A door shuts, deep in the house', 32),
    'cicadas': ('Cicadas', 24),
    'hum': ('A faint electric hum', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/corner/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'corner.{name}'] == {'sounds': [{'name': f'the_oldest_house:corner/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.corner.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.corner.{name}'] == subtitle, name
    print(f'Verified the corner house\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9408)


def gate():
    # Wrought iron swung on a dry hinge: a slow rising groan with a rough edge, and the latch knocking at the end.
    n = seconds(1.6)
    t = times(n)
    pitch = 260 + 180 * np.sin(np.pi * np.minimum(1, t / 1.3)) + 30 * np.sin(2 * np.pi * 7 * t)
    groan = np.sin(2 * np.pi * np.cumsum(pitch) / RATE) * .4 + np.sign(np.sin(2 * np.pi * np.cumsum(pitch * 2) / RATE)) * .08
    groan = band(groan + band(rng.standard_normal(n), 800, 3000) * .12, 150, 4000) * envelope(n, .05, .3)
    k = seconds(.12)
    tk = times(k)
    place(groan, (np.sin(2 * np.pi * 900 * tk) * .6 + band(rng.standard_normal(k), 400, 3000) * .5) * np.exp(-tk / .02), 1.35)
    return groan


def jar():
    # A glass jar set down on a wooden board: a soft knock and a high clear ring that dies quickly.
    n = seconds(.8)
    t = times(n)
    ring = (np.sin(2 * np.pi * 2350 * t) * .5 + np.sin(2 * np.pi * 3710 * t) * .25 + np.sin(2 * np.pi * 5230 * t) * .1) * np.exp(-t / .18)
    knock = band(rng.standard_normal(n), 150, 900) * np.exp(-t / .015) * .7
    return ring + knock


def shut():
    # A door closing somewhere deep in a house: a muffled thud, its low ring in the walls, then nothing.
    n = seconds(2.0)
    t = times(n)
    thud = band(rng.standard_normal(n), 40, 220) * np.exp(-t / .06)
    body = np.sin(2 * np.pi * 68 * t) * np.exp(-t / .5) * .5 + np.sin(2 * np.pi * 103 * t) * np.exp(-t / .35) * .25
    latch = np.zeros(n)
    k = seconds(.05)
    place(latch, band(rng.standard_normal(k), 1200, 5000) * np.exp(-times(k) / .008) * .3, .02)
    return band(thud + body, 30, 900) + latch


def cicadas():
    # Insects in the afternoon heat: a buzzing pulse train in a narrow high band, swelling and falling away in waves.
    n = seconds(5.0)
    t = times(n)
    out = np.zeros(n)
    for f, rate, ph in ((4600, 38, .0), (5200, 44, 1.1), (4100, 31, 2.2)):
        pulses = (np.sin(2 * np.pi * rate * t + ph) > .2).astype(float)
        out += band(rng.standard_normal(n), f - 500, f + 500) * pulses * (.5 + .5 * np.sin(2 * np.pi * .35 * t + ph))
    return out * envelope(n, .8, 1.2)


def hum():
    # Lamps lit with no power to them: a low mains hum and its overtones, steady, faint.
    n = seconds(3.0)
    t = times(n)
    tone = np.sin(2 * np.pi * 100 * t) * .5 + np.sin(2 * np.pi * 200 * t) * .3 + np.sin(2 * np.pi * 300 * t) * .12 + np.sin(2 * np.pi * 50 * t) * .2
    return tone * (1 + .05 * np.sin(2 * np.pi * .7 * t)) * envelope(n, .5, .5)


MAKERS = {'gate': gate, 'jar': jar, 'shut': shut, 'cicadas': cicadas, 'hum': hum}


def main():
    folder = ASSETS / 'sounds/corner'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'corner.{name}',
               {'sounds': [{'name': f'the_oldest_house:corner/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.corner.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.corner.{name}', subtitle)


if __name__ == '__main__':
    main()
