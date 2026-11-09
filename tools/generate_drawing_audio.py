"""Original sounds for The Drawing (0.4.407): a crowd's murmur going round a square, a folded slip drawn out of the box or unfolded,
stones picked up off a pile, lemonade poured from a jug into a glass, and the day cut to white.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'murmur': ('A murmur goes round the crowd', 32),
    'slip': ('Paper rustles', 12),
    'stones': ('Stones clatter', 24),
    'pour': ('Lemonade is poured', 12),
    'white': ('A high ringing', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/drawing/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'drawing.{name}'] == {'sounds': [{'name': f'the_oldest_house:drawing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.drawing.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.drawing.{name}'] == subtitle, name
    print(f'Verified the drawing\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9407)


def murmur():
    # Many voices low and indistinct, swelling as the word goes round and settling again: band-limited noise shaped by a slow
    # syllabic flutter, with a few voiced vowel tones under it, no words.
    n = seconds(3.6)
    t = times(n)
    out = band(rng.standard_normal(n), 180, 1400) * .5
    flutter = np.zeros(n)
    for f, ph in ((3.1, .2), (4.3, 1.1), (5.2, 2.3), (2.6, .7)):
        flutter += .25 * (1 + np.sin(2 * np.pi * f * t + ph))
    voiced = np.zeros(n)
    for f0 in (118, 142, 165, 196, 221):
        drift = f0 * (1 + .03 * np.sin(2 * np.pi * .4 * t + f0))
        voiced += np.sin(2 * np.pi * np.cumsum(drift) / RATE) * .06
    swell = np.sin(np.pi * np.minimum(1, t / 3.6)) ** .8
    return (out * flutter + band(voiced, 150, 900)) * swell


def slip():
    # A small square of paper: drawn out past others with a dry rustle, then unfolded in two crisp creaks.
    n = seconds(.9)
    out = np.zeros(n)
    k = seconds(.35)
    place(out, band(rng.standard_normal(k), 2000, 9000) * envelope(k, .02, .2) * .7, 0)
    for at in (.5, .68):
        k = seconds(.07)
        place(out, band(rng.standard_normal(k), 1500, 7000) * envelope(k, .002, .04), at)
    return out


def stones():
    # A handful of smooth stones lifted off a pile: knocks of stone on stone, short and bright, scattered and overlapping.
    n = seconds(1.6)
    out = np.zeros(n)
    for _ in range(14):
        at = rng.uniform(0, 1.35)
        k = seconds(.05)
        tk = times(k)
        f = rng.uniform(1800, 3600)
        knock = np.sin(2 * np.pi * f * tk) * np.exp(-tk / .008) + band(rng.standard_normal(k), 1500, 8000) * np.exp(-tk / .004) * .6
        place(out, knock * rng.uniform(.4, 1), at)
    return out


def pour():
    # Lemonade from a jug into a glass: a liquid trickle whose pitch rises as the glass fills, a chink of ice at the start.
    n = seconds(1.8)
    t = times(n)
    rise = 500 + 900 * (t / 1.8)
    gurgle = np.sin(2 * np.pi * np.cumsum(rise + 80 * np.sin(2 * np.pi * 23 * t)) / RATE) * .25
    water = band(rng.standard_normal(n), 600, 4000) * .4
    body = (gurgle + water) * envelope(n, .05, .3)
    k = seconds(.08)
    tk = times(k)
    place(body, np.sin(2 * np.pi * 3400 * tk) * np.exp(-tk / .02) * .5, .02)
    return body


def white():
    # The day cut to white: a pure high tone that comes on at once and hangs, a softer octave under it, fading out slowly.
    n = seconds(3.0)
    t = times(n)
    tone = np.sin(2 * np.pi * 2093 * t) * .6 + np.sin(2 * np.pi * 1046.5 * t) * .25 + np.sin(2 * np.pi * 4186 * t) * .08
    return tone * np.minimum(1, t / .004) * np.exp(-t / 1.4)


MAKERS = {'murmur': murmur, 'slip': slip, 'stones': stones, 'pour': pour, 'white': white}


def main():
    folder = ASSETS / 'sounds/drawing'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'drawing.{name}',
               {'sounds': [{'name': f'the_oldest_house:drawing/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.drawing.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.drawing.{name}', subtitle)


if __name__ == '__main__':
    main()
