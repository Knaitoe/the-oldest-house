"""Original sounds for Bike night's bikes (0.4.463): a bike's tyres letting go of the road for a hop, and both of them coming back
down; the playing card pegged into the Rattler's spokes, and the roar it makes when it takes the bike; the Lamp's dynamo against its
tyre. And the park's (0.4.464): the failing lamp striking and buzzing as it comes back on, and the chain on the gate when a hand
tries it. And the playground's (0.4.465): a swing's hangers as it comes through the bottom of its arc, and a ball of sand packed in
a hand, thrown, and bursting on what it meets.

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
    'park/lamp_buzz': ('park.lamp_buzz', 'Lamp buzzes', 12),
    'park/chain': ('park.chain', 'Chain rattles', 12),
    'park/swing_creak': ('park.swing_creak', 'Swing creaks', 12),
    'park/sand_scoop': ('park.sand_scoop', 'Sand is packed', 8),
    'park/sand_throw': ('park.sand_throw', 'Sand ball is thrown', 10),
    'park/sand_burst': ('park.sand_burst', 'Sand ball bursts', 14),
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
    print(f'Verified Bike night\'s {len(ROWS)} original cues.')


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


def lamp_buzz():
    # A discharge lamp that will not stay lit: the starter's tick, the tube catching with a rasp, and the ballast's hum settling under it.
    length = .9
    n = seconds(length)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((0, .7), (.07, .5), (.16, .8)):
        i = seconds(at)
        c = click(.01, 2500, 7500, .0015) * level
        out[i:i + len(c)] += c
    catch = np.clip((t - .16) / .05, 0, 1)
    hum = (np.sin(2 * np.pi * 120 * t) * .5 + np.sin(2 * np.pi * 240 * t) * .25 + np.sin(2 * np.pi * 360 * t) * .14 + np.sin(2 * np.pi * 600 * t) * .06)
    rasp = band(rng.standard_normal(n), 1800, 6000) * (.5 + .5 * np.sign(np.sin(2 * np.pi * 120 * t))) * np.exp(-np.clip(t - .16, 0, None) / .12) * .5
    settle = .35 + .65 * np.exp(-np.clip(t - .16, 0, None) / .25)
    out += (hum * settle * .6 + rasp) * catch * np.clip((length - t) / .2, 0, 1)
    return out


def chain():
    # A hand on the gate: the leaves shift an inch against the chain, the links run and knock on the iron, and the padlock swings back against a bar.
    length = .8
    n = seconds(length)
    out = np.zeros(n)
    knocks = ((0, 1.0, 2100), (.05, .6, 2900), (.09, .75, 2400), (.15, .5, 3300), (.21, .65, 2600), (.3, .35, 3100), (.47, .55, 1500), (.62, .25, 1500))
    for at, level, pitch in knocks:
        i = seconds(at)
        m = n - i
        t = times(m)
        ring = (np.sin(2 * np.pi * pitch * t) + .6 * np.sin(2 * np.pi * pitch * 1.52 * t) + .35 * np.sin(2 * np.pi * pitch * 2.71 * t)) * np.exp(-t / .035)
        hit = band(rng.standard_normal(m), 1200, 7000) * np.exp(-t / .006)
        out[i:] += (ring * .5 + hit * .8) * level
    t = times(n)
    out += np.sin(2 * np.pi * 180 * t) * np.exp(-t / .09) * .3                   # the leaf itself, low, as it takes up the slack
    return out


def swing_creak():
    # A swing's hanger turning in its eye under a weight: a dry squeak that climbs and falls back, and the links settling after it.
    length = .42
    n = seconds(length)
    t = times(n)
    pitch = 880 + 420 * np.sin(np.pi * np.clip(t / .26, 0, 1)) ** 2
    phase = 2 * np.pi * np.cumsum(pitch) / RATE
    squeak = (np.sin(phase) + .45 * np.sin(2 * phase) + .2 * np.sin(3.02 * phase)) * np.sin(np.pi * np.clip(t / .3, 0, 1)) ** 1.5
    grit = band(rng.standard_normal(n), 2500, 7000) * np.sin(np.pi * np.clip(t / .3, 0, 1)) * .25
    out = squeak * (.6 + .4 * np.sign(np.sin(2 * np.pi * 37 * t))) * .5 + grit
    for at, level, ring in ((.3, .35, 2700), (.34, .22, 3400), (.38, .15, 2300)):
        i = seconds(at)
        m = n - i
        u = times(m)
        out[i:] += np.sin(2 * np.pi * ring * u) * np.exp(-u / .02) * level
    return out


def sand_scoop():
    # A hand goes into damp sand and comes up full, and the other hand pats it round: two short gritty swells.
    length = .36
    n = seconds(length)
    t = times(n)
    grit = band(rng.standard_normal(n), 900, 5200)
    scoop = np.sin(np.pi * np.clip(t / .17, 0, 1)) ** 2
    pat = np.exp(-np.clip(t - .22, 0, None) / .025) * (t >= .22)
    low = np.sin(2 * np.pi * 140 * t) * np.exp(-np.clip(t - .22, 0, None) / .03) * (t >= .22)
    return grit * (scoop * .7 + pat * .9) + low * .35


def sand_throw():
    # An arm coming over, and a small heavy thing leaving the hand: a soft rush that is gone at once.
    length = .2
    n = seconds(length)
    t = times(n)
    rush = band(rng.standard_normal(n), 350, 2600) * np.sin(np.pi * np.clip(t / length, 0, 1)) ** 2
    sift = band(rng.standard_normal(n), 3000, 8000) * np.exp(-np.clip(t - .1, 0, None) / .03) * (t >= .1) * .3
    return rush + sift


def sand_burst():
    # A ball of sand meeting something: a dull knock with no ring in it, and the sand going everywhere after.
    length = .5
    n = seconds(length)
    t = times(n)
    knock = np.sin(2 * np.pi * (120 - 60 * np.clip(t / .05, 0, 1)) * t) * np.exp(-t / .03)
    spray = band(rng.standard_normal(n), 1800, 8000) * np.exp(-t / .07)
    out = knock * .9 + spray * .7
    for _ in range(14):
        i = seconds(.06 + rng.random() * .36)
        c = click(.006, 2500, 8000, .0012) * (.1 + rng.random() * .25) * (1 - i / n)
        out[i:i + len(c)] += c[:max(0, n - i)]
    return out


MAKERS = {'bike/hop': hop, 'bike/land': land, 'bike/card': card, 'bike/turbo': turbo, 'bike/dynamo': dynamo, 'park/lamp_buzz': lamp_buzz, 'park/chain': chain,
          'park/swing_creak': swing_creak, 'park/sand_scoop': sand_scoop, 'park/sand_throw': sand_throw, 'park/sand_burst': sand_burst}


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
