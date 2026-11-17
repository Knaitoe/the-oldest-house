"""Original sounds for Last Seen (0.4.667, docs/LAST_SEEN_0_4_667.md): a city park in the morning, and the office across from it.

The place has only its everyday sounds in this part: the avenue's traffic beyond the railing, the pigeons on the path, a folder drawn off
a shelf, and the typewriter on the archivist's desk. Nothing in them is frightening; the vignette's weight is in what the park holds, not
in what it plays.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'traffic': ('Traffic passes on the avenue', 40),
    'pigeons': ('Pigeons coo', 16),
    'paper': ('A folder slides off a shelf', 10),
    'typewriter': ('A typewriter clacks', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/last_seen/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'last_seen.{name}'] == {'sounds': [{'name': f'the_oldest_house:last_seen/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.last_seen.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.last_seen.{name}'] == subtitle, name
    print(f'Verified Last Seen\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9667)


def traffic():
    # Six seconds of the avenue past the railing: the steady hush of tyres on asphalt, and three cars going by at their own speeds,
    # each a swell of road noise with an engine's low note under it that drops as it passes; faded at both ends to lay end to end.
    n = seconds(6.0)
    t = times(n)
    hush = band(rng.standard_normal(n), 120, 1800) * .35
    passes = np.zeros(n)
    for centre, width, pitch in ((1.4, .9, 92), (3.3, .7, 118), (5.0, 1.1, 74)):
        swell = np.exp(-((t - centre) / width) ** 2)
        doppler = pitch * (1 + .06 * np.tanh((centre - t) / (width * .6)))
        engine = np.sin(2 * np.pi * np.cumsum(doppler) / RATE) * .5 + np.sin(4 * np.pi * np.cumsum(doppler) / RATE) * .2
        tyres = band(rng.standard_normal(n), 300, 3200)
        passes += swell * (tyres * .9 + engine * .6)
    edge = np.clip(t / .5, 0, 1) * np.clip((6.0 - t) / .5, 0, 1)
    return (hush + passes) * edge


def coo(t, start, pitch):
    # One pigeon's call: a soft "roo-coo", two swells of a breathy low voice, the second falling.
    x = t - start
    on = (x >= 0) & (x < .75)
    first = np.exp(-((x - .16) / .09) ** 2)
    second = np.exp(-((x - .5) / .14) ** 2)
    f = pitch * (1 + .12 * first - .1 * np.clip((x - .38) / .3, 0, 1))
    phase = 2 * np.pi * np.cumsum(f) / RATE
    voice = (np.sin(phase) + .35 * np.sin(2 * phase)) * (first * .8 + second)
    return voice * on


def pigeons():
    # Three seconds on the path: two birds cooing over each other, and once a short clatter of wings as one lifts and settles.
    n = seconds(3.0)
    t = times(n)
    calls = coo(t, .1, 290) + coo(t, .95, 270) * .8 + coo(t, 1.9, 300) * .9
    breath = band(rng.standard_normal(n), 200, 900) * .12
    calls = calls + breath * (np.abs(calls) > .05)
    wing = np.zeros(n)
    for k in range(7):
        at = 1.3 + k * .055
        wing += np.exp(-np.clip(t - at, 0, None) / .018) * (t >= at)
    flutter = band(rng.standard_normal(n), 500, 5000) * wing * .7
    return calls * .9 + flutter


def paper():
    # A folder drawn off a crowded shelf: card scraping past card, a soft knock as it clears, and the pages inside settling.
    n = seconds(1.1)
    t = times(n)
    drag = np.clip(t / .05, 0, 1) * np.clip((.55 - t) / .08, 0, 1)
    scrape = band(rng.standard_normal(n), 1200, 6500) * drag * (.6 + .4 * np.sin(2 * np.pi * 23 * t) ** 2)
    knock = band(rng.standard_normal(n), 150, 1200) * np.exp(-np.clip(t - .56, 0, None) / .03) * (t >= .56) * 1.1
    settle = band(rng.standard_normal(n), 2000, 8000) * np.exp(-np.clip(t - .62, 0, None) / .15) * (t >= .62) * .35
    return scrape + knock + settle


def typewriter():
    # A line typed on a manual typewriter: uneven strikes, each a sharp metal tick with the platen's thud under it, a pause for a
    # word, and at the end the little bell and the carriage drawn back.
    n = seconds(3.2)
    t = times(n)
    out = np.zeros(n)
    at = .1
    strikes = []
    while at < 2.4:
        strikes.append(at)
        at += rng.uniform(.09, .2) + (.25 if rng.random() < .15 else 0)
    for s in strikes:
        x = t - s
        hit = (x >= 0)
        tick = band(rng.standard_normal(n), 2500, 9000) * np.exp(-np.clip(x, 0, None) / .006) * hit
        thud = band(rng.standard_normal(n), 90, 700) * np.exp(-np.clip(x, 0, None) / .02) * hit
        out += tick * rng.uniform(.7, 1.0) + thud * .8
    bell_at = 2.55
    x = t - bell_at
    bell = (np.sin(2 * np.pi * 2350 * t) + .4 * np.sin(2 * np.pi * 5870 * t)) * np.exp(-np.clip(x, 0, None) / .35) * (x >= 0) * .5
    carriage = band(rng.standard_normal(n), 300, 3000) * np.clip((t - 2.7) / .05, 0, 1) * np.clip((3.15 - t) / .05, 0, 1) * (.4 + .3 * np.sin(2 * np.pi * 31 * t) ** 2)
    return out + bell + carriage


MAKERS = {'traffic': traffic, 'pigeons': pigeons, 'paper': paper, 'typewriter': typewriter}


def main():
    folder = ASSETS / 'sounds/last_seen'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'last_seen.{name}',
               {'sounds': [{'name': f'the_oldest_house:last_seen/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.last_seen.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.last_seen.{name}', subtitle)


if __name__ == '__main__':
    main()
