"""Original sounds for the Sphinx (0.4.306): her voice (close enough to be a person's, coming back from somewhere far larger), a breath
taken at a person's scale and answered at hers, the latch lifting, a door catching at its stop, the stop pin drawn, the weight of her
hand coming down, and the umbrella stand dragged across tile.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'voice': ('A voice asks a question', 32),
    'breath': ('Something vast draws breath', 32),
    'latch': ('A latch lifts', 10),
    'catch': ('A door catches at its stop', 12),
    'pin': ('A pin comes free', 10),
    'strike': ('A huge hand comes down', 24),
    'scrape': ('Something scrapes on tile', 10),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/sphinx/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'sphinx.{name}'] == {'sounds': [{'name': f'the_oldest_house:sphinx/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.sphinx.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.sphinx.{name}'] == subtitle, name
    print(f'Verified the Sphinx\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(3060)


def hall(x, length=2.5, decay=1.1):
    # A space far bigger than any room: convolve with a long, dark, decaying noise tail.
    k = seconds(length)
    tail = band(rng.standard_normal(k), 80, 2500) * np.exp(-times(k) / decay * 3)
    wet = np.convolve(x, tail)[:len(x) + k] * .02
    wet = np.pad(wet, (0, len(x) + k - len(wet)))
    out = np.zeros(len(x) + k)
    out[:len(x)] += x
    return out + wet


def voice():
    # Twelve syllables of breathy, voiced murmur, a person's pitch, then the same words coming back from far away.
    n = seconds(3.0)
    out = np.zeros(n)
    at = .1
    for i in range(12):
        k = seconds(rng.uniform(.13, .22))
        tk = times(k)
        f0 = 150 + 20 * np.sin(i * 1.3)
        voiced = sum(np.sin(2 * np.pi * f0 * h * tk) / h for h in range(1, 7))
        formant = band(voiced + rng.standard_normal(k) * .6, rng.uniform(500, 900), rng.uniform(1300, 2400))
        place(out, formant * envelope(k, .02, .08) * .5, at)
        at += k / RATE + rng.uniform(.02, .07)
    return hall(out, 3.0, 1.4)


def breath():
    # An inhale at a person's scale, and then the room filling with one a hundred times larger.
    n = seconds(3.2)
    t = times(n)
    small = band(rng.standard_normal(seconds(.7)), 600, 4000) * envelope(seconds(.7), .4, .25) * .3
    out = np.zeros(n)
    place(out, small, 0.0)
    k = seconds(2.3)
    tk = times(k)
    vast = band(rng.standard_normal(k), 40, 400) * np.sin(np.pi * tk / 2.3) ** 2 * .9
    vast += np.sin(2 * np.pi * 32 * tk) * np.sin(np.pi * tk / 2.3) ** 2 * .3
    place(out, vast, .8)
    return out


def latch():
    n = seconds(.4)
    out = np.zeros(n)
    for at, f in ((0.0, 2400), (.12, 1800)):
        k = seconds(.06)
        place(out, band(rng.standard_normal(k), f, f * 2.5) * envelope(k, .001, .02) * .6, at)
    return out


def catch():
    n = seconds(.7)
    out = np.zeros(n)
    k = seconds(.25)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 110 * tk) + .5 * band(rng.standard_normal(k), 200, 1800)) * envelope(k, .002, .08) * .8, 0.0)
    k = seconds(.3)
    place(out, band(rng.standard_normal(k), 2000, 6000) * envelope(k, .005, .1) * .25, .05)
    return out


def pin():
    n = seconds(.8)
    out = np.zeros(n)
    k = seconds(.45)
    tk = times(k)
    place(out, np.sin(2 * np.pi * np.cumsum(1400 + 700 * tk / .45) / RATE) * envelope(k, .02, .1) * .12, 0.0)
    place(out, band(rng.standard_normal(k), 3000, 9000) * envelope(k, .02, .1) * .2, 0.0)
    k = seconds(.2)
    place(out, band(rng.standard_normal(k), 1500, 5000) * envelope(k, .001, .04) * .5, .48)
    return out


def strike():
    n = seconds(1.8)
    out = np.zeros(n)
    k = seconds(1.2)
    tk = times(k)
    place(out, (np.sin(2 * np.pi * 45 * tk) + .6 * np.sin(2 * np.pi * 70 * tk)) * envelope(k, .004, .5) * .9, 0.0)
    place(out, band(rng.standard_normal(k), 100, 1500) * envelope(k, .002, .3) * .6, 0.0)
    k = seconds(.8)
    place(out, band(rng.standard_normal(k), 2000, 8000) * envelope(k, .05, .4) * .15, .2)
    return hall(out, 1.5, .8)


def scrape():
    n = seconds(.7)
    t = times(n)
    out = band(rng.standard_normal(n), 300, 2500) * (.6 + .4 * np.sin(2 * np.pi * 23 * t)) * adsr(n, .05, .2) * .5
    return out


MAKERS = {'voice': voice, 'breath': breath, 'latch': latch, 'catch': catch, 'pin': pin, 'strike': strike, 'scrape': scrape}


def main():
    folder = ASSETS / 'sounds/sphinx'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'sphinx.{name}',
               {'sounds': [{'name': f'the_oldest_house:sphinx/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.sphinx.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.sphinx.{name}', subtitle)


if __name__ == '__main__':
    main()
