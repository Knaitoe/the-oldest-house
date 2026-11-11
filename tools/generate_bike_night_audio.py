"""Original sounds for Bike night's bikes (0.4.463): a bike's tyres letting go of the road for a hop, and both of them coming back
down; the playing card pegged into the Rattler's spokes, and the roar it makes when it takes the bike; the Lamp's dynamo against its
tyre.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one at the
bike. The game plays the card and the dynamo over and over while the bike moves, higher and louder the faster it goes, so each is a
short piece that begins and ends quietly. Run from tools/. Existing files are kept, so the committed sounds only change when deleted
first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'bike/hop': ('bike.hop', 'Bike hops', 12),
    'bike/land': ('bike.land', 'Bike lands', 14),
    'bike/card': ('bike.card', 'Card rattles in spokes', 14),
    'bike/turbo': ('bike.turbo', 'Card roars in spokes', 20),
    'bike/dynamo': ('bike.dynamo', 'Dynamo whines', 8),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for path_name, (event, subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/{path_name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[event] == {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'}, event
        assert lang[f'subtitles.the_oldest_house.{event}'] == subtitle, event
    print(f'Verified the bikes\' {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9463)


def click(length=.012, low=1500, high=6500, decay=.002):
    m = seconds(length)
    return band(rng.standard_normal(m), low, high) * np.exp(-times(m) / decay)


def hop():
    # Both tyres let go of the road: grit thrown off them, the frame's own low spring upward, and the chain slapping its stay.
    n = seconds(.2)
    t = times(n)
    grit = band(rng.standard_normal(n), 500, 3500) * np.exp(-t / .018) * .6
    spring = np.sin(2 * np.pi * np.cumsum(120 + 150 * np.clip(t / .08, 0, 1)) / RATE) * np.exp(-t / .045) * .5
    out = grit + spring
    i = seconds(.05)
    c = click(.02, 1800, 5000, .004) * .35
    out[i:i + len(c)] += c
    return out


def land():
    # The back wheel and then the front: two low thumps, the tyres biting, and the chain and a loose mudguard after them.
    n = seconds(.34)
    out = np.zeros(n)
    for at, level in ((0, 1.0), (.045, .8)):
        i = seconds(at)
        m = n - i
        t = times(m)
        thump = np.sin(2 * np.pi * np.cumsum(110 - 45 * np.clip(t / .05, 0, 1)) / RATE) * np.exp(-t / .05) * .8
        bite = band(rng.standard_normal(m), 300, 2200) * np.exp(-t / .025) * .45
        out[i:] += (thump + bite) * level
    for at, level in ((.07, .4), (.11, .25), (.16, .15)):
        i = seconds(at)
        c = click(.03, 2000, 6000, .006) * level
        out[i:i + len(c)] += c
    return out


def card(length=.42, rate0=46, rate1=52, level=.7):
    # A playing card flicked by every spoke that passes: a run of dry clicks, never quite even, each with the card's own papery ring.
    n = seconds(length)
    out = np.zeros(n)
    at = 0.0
    k = 0
    while at < length - .02:
        rate = rate0 + (rate1 - rate0) * at / length
        i = seconds(at)
        c = click(.014, 1400, 6000, .0022) * (level * (.75 + .25 * rng.random()))
        m = seconds(.014)
        tt = times(m)
        ring = np.sin(2 * np.pi * (880 + 60 * rng.random()) * tt) * np.exp(-tt / .004) * .25 * level
        end = min(n, i + m)
        out[i:end] += (c + ring)[:end - i]
        at += (1 / rate) * (.9 + .2 * rng.random())
        k += 1
    edge = seconds(.03)
    out[:edge] *= np.linspace(0, 1, edge)
    out[-edge:] *= np.linspace(1, 0, edge)
    return out


def turbo():
    # The card taking the bike: the clicks run together into a roar that climbs, with the air going past, and falls away.
    length = 1.2
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    at = 0.0
    while at < length - .02:
        rate = 45 + 110 * np.clip(at / .5, 0, 1)
        i = seconds(at)
        c = click(.012, 1200, 7000, .002) * (.5 + .3 * rng.random())
        end = min(n, i + len(c))
        out[i:end] += c[:end - i]
        at += (1 / rate) * (.92 + .16 * rng.random())
    drone = np.sin(2 * np.pi * np.cumsum(90 + 150 * np.clip(t / .5, 0, 1)) / RATE)
    drone = np.sign(drone) * np.abs(drone) ** .5 * .22
    rush = band(rng.standard_normal(n), 700, 4000) * np.clip(t / .4, 0, 1) * .3
    shape = np.clip(t / .05, 0, 1) * np.clip((length - t) / .35, 0, 1)
    return (out + drone + rush) * shape


def dynamo():
    # A bottle dynamo's little wheel against the tyre: a thin whine with a rough edge, and the rubber's hiss under it.
    length = .5
    n = seconds(length)
    t = times(n)
    wobble = 1 + .012 * np.sin(2 * np.pi * 9 * t)
    phase = 2 * np.pi * np.cumsum(430 * wobble) / RATE
    whine = np.sin(phase) * .5 + np.sin(2 * phase) * .22 + np.sin(3 * phase) * .12 + np.sin(5 * phase) * .05
    hiss = band(rng.standard_normal(n), 2500, 7000) * .12
    edge = np.clip(t / .06, 0, 1) * np.clip((length - t) / .06, 0, 1)
    return (whine + hiss) * edge


MAKERS = {'bike/hop': hop, 'bike/land': land, 'bike/card': card, 'bike/turbo': turbo, 'bike/dynamo': dynamo}


def main():
    for path_name, (event, subtitle, reach) in ROWS.items():
        target = ASSETS / f'sounds/{path_name}.ogg'
        target.parent.mkdir(parents=True, exist_ok=True)
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[path_name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', event,
               {'sounds': [{'name': f'the_oldest_house:{path_name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.{event}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.{event}', subtitle)


if __name__ == '__main__':
    main()
