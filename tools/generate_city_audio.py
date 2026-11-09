"""Original sounds for the city on the ice (0.4.409): wind across the ice and the pass, torn aeroplane fabric flapping in it, something
piping in the dark under the city, wings in the dark where no birds can be seen, and the wet push of what fills the tunnel.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it
happens. Run from tools/. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'wind': ('Wind across the ice', 48),
    'flap': ('Torn fabric flaps', 16),
    'piping': ('Something pipes in the dark', 64),
    'birds': ('Wings in the dark', 24),
    'mass': ('Something wet pushes through the tunnel', 40),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/city/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'city.{name}'] == {'sounds': [{'name': f'the_oldest_house:city/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.city.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.city.{name}'] == subtitle, name
    print(f'Verified the city on the ice\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, place, normalized, update  # noqa: E402

rng = np.random.default_rng(9409)


def held(n, attack, release):
    """Up over the attack, held, and down over the release: for cues that loop or sustain."""
    t = times(n)
    return np.minimum(1, t / attack) * np.minimum(1, (t[-1] - t) / release)


def wind():
    # Wind over open ice: broad low noise swelling and falling in long gusts, a thin whistle riding the strongest of them.
    n = seconds(6.0)
    t = times(n)
    gusts = .55 + .45 * np.sin(2 * np.pi * .23 * t) * np.sin(2 * np.pi * .11 * t + .7)
    body = band(rng.standard_normal(n), 80, 900) * gusts
    hiss = band(rng.standard_normal(n), 1800, 5000) * gusts ** 2 * .25
    whistle = np.sin(2 * np.pi * np.cumsum(620 + 140 * np.sin(2 * np.pi * .3 * t)) / RATE) * np.maximum(0, gusts - .6) * .35
    return (body + hiss + whistle) * held(n, .8, .8)


def flap():
    # Doped aeroplane fabric torn open and snapping in the wind: three loose cracks, each with a short rattle.
    n = seconds(1.6)
    out = np.zeros(n)
    for at, size in ((.05, 1.0), (.55, .7), (1.0, .85)):
        k = seconds(.22)
        tk = times(k)
        crack = band(rng.standard_normal(k), 300, 4200) * np.exp(-tk / .03) * size
        rattle = band(rng.standard_normal(k), 900, 3000) * (np.sin(2 * np.pi * 34 * tk) > 0) * np.exp(-tk / .09) * .35 * size
        place(out, crack + rattle, at)
    return out


def piping():
    # A call in the dark under the city: three falling-and-rising notes, reedy and slightly wrong, with a long cave echo behind them.
    n = seconds(3.4)
    t = times(n)
    out = np.zeros(n)
    for at, f0 in ((.0, 880), (.55, 740), (1.1, 990)):
        k = seconds(.48)
        tk = times(k)
        pitch = f0 * (1 - .06 * np.sin(np.pi * tk / .48)) + 9 * np.sin(2 * np.pi * 6.5 * tk)
        phase = 2 * np.pi * np.cumsum(pitch) / RATE
        note = (np.sin(phase) * .55 + np.sin(2 * phase) * .2 + np.sin(3.02 * phase) * .12) * held(k, .04, .12)
        place(out, note + band(rng.standard_normal(k), f0, f0 * 3) * .05 * held(k, .04, .12), at)
    echo = np.zeros(n)
    for delay, gain in ((.21, .45), (.47, .3), (.83, .18)):
        d = seconds(delay)
        echo[d:] += out[:n - d] * gain
    return band(out + echo, 300, 6000) * np.exp(-np.maximum(0, t - 2.2) / .5)


def birds():
    # Wings in the dark: a flurry of soft beats close by, uneven, going away; never a cry.
    n = seconds(2.0)
    out = np.zeros(n)
    at = 0.0
    while at < 1.5:
        k = seconds(.07)
        tk = times(k)
        place(out, band(rng.standard_normal(k), 250, 2500) * np.sin(np.pi * tk / .07) * (1 - at / 1.8), at)
        at += .06 + rng.random() * .07
    return out


def mass():
    # Something that fills a tunnel wall to wall pushing through it: a deep wet churn, slow sucking pulses, the air shoved ahead of it.
    n = seconds(3.2)
    t = times(n)
    churn = band(rng.standard_normal(n), 30, 260) * (.6 + .4 * np.sin(2 * np.pi * 1.3 * t))
    suck = band(rng.standard_normal(n), 400, 1600) * np.maximum(0, np.sin(2 * np.pi * .9 * t)) ** 6 * .45
    groan = np.sin(2 * np.pi * np.cumsum(46 + 6 * np.sin(2 * np.pi * .4 * t)) / RATE) * .35
    air = band(rng.standard_normal(n), 1200, 6000) * .12
    return (churn + suck + groan + air) * held(n, .4, .6)


MAKERS = {'wind': wind, 'flap': flap, 'piping': piping, 'birds': birds, 'mass': mass}


def main():
    folder = ASSETS / 'sounds/city'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'city.{name}',
               {'sounds': [{'name': f'the_oldest_house:city/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.city.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.city.{name}', subtitle)


if __name__ == '__main__':
    main()
