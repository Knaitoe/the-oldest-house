"""Original sounds for The list (0.4.699, docs/THE_LIST_0_4_699.md): a small cotton town on the South Plains at night.

Part 1a has the town's own night: crickets in the yards and the cotton, a dog behind a fence a few yards off, the drive-in's
speakers on Main Street murmuring with nobody in the stalls, a loose sheet of tin on the gin knocking in the wind, a train's horn out past
the playa, the wind over the flat land, and the water tower ticking as it cools; part 1b (0.4.700) adds a sprinkler working a lawn;
part 1c (0.4.701) the pasture's windmill turning, a pumpjack's stroke and the cattle guard under a reader's feet.
Nothing in them is frightening yet.

Synthesized from noise with Movie Night's building blocks; no recordings. Mono, so Minecraft places each one where it happens. Run
from the repository root. Existing files are kept, so the committed sounds only change when deleted first.
"""
import json
import sys
from pathlib import Path

ROWS = {
    'insects': ('Crickets chirp', 16),
    'dog': ('A dog barks behind a fence', 32),
    'speaker': ('A drive-in speaker murmurs', 12),
    'tin': ('Loose tin knocks in the wind', 32),
    'train': ('A train horn sounds far off', 64),
    'wind': ('Wind blows over the flat land', 48),
    'tower': ('The water tower ticks', 24),
    'sprinkler': ('A sprinkler ticks across a lawn', 16),
    'windmill': ('A windmill turns and creaks', 32),
    'pumpjack': ('A pumpjack knocks on its downstroke', 32),
    'guard': ('A cattle guard rattles underfoot', 16),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/the_list/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'the_list.{name}'] == {'sounds': [{'name': f'the_oldest_house:the_list/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.the_list.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.the_list.{name}'] == subtitle, name
    print(f'Verified Lamesa\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, normalized, update  # noqa: E402

rng = np.random.default_rng(9699)


def insects():
    # Four seconds of field crickets at night: three of them, each chirping in its own rhythm, a chirp being four quick pulses of a
    # bright tone; one nearer than the others; under them the steady thin trill of many more a long way off.
    n = seconds(4.0)
    t = times(n)
    out = np.zeros(n)
    for period, pitch, level, start in ((.62, 4400, 1.0, .05), (.81, 4900, .55, .3), (.55, 4100, .35, .5)):
        at = start
        while at < 3.8:
            for k in range(4):
                x = t - (at + k * .024)
                on = (x >= 0) & (x < .016)
                out += np.sin(2 * np.pi * pitch * x) * on * np.sin(np.pi * np.clip(x / .016, 0, 1)) * level
            at += period * rng.uniform(.95, 1.05)
    far = band(rng.standard_normal(n), 3800, 5200) * (.6 + .4 * np.sin(2 * np.pi * 18 * t) ** 2) * .12
    edge = np.clip(t / .3, 0, 1) * np.clip((4.0 - t) / .3, 0, 1)
    return (out + far) * edge


def dog():
    # A dog behind a fence a few yards off, at night: three barks, the middle one higher, each a short voiced burst whose pitch drops
    # as it ends, with the chest's low thump at its start; the chain-link rattling under the last as it jumps at the fence.
    n = seconds(2.2)
    t = times(n)
    out = np.zeros(n)
    for at, pitch, length in ((.1, 420, .2), (.48, 520, .17), (.86, 400, .26)):
        x = t - at
        on = (x >= 0) & (x < length)
        f = pitch * (1.15 - .4 * np.clip(x / length, 0, 1))
        phase = np.cumsum(f * on) / RATE
        voice = np.sign(np.sin(2 * np.pi * phase)) * on
        shape = np.clip(x / .015, 0, 1) * np.clip((length - x) / .06, 0, 1) * on
        out += (band(voice, 500, 1400) + band(voice, 1600, 3000) * .5 + band(rng.standard_normal(n), 800, 2500) * .3 * on) * shape
        out += band(rng.standard_normal(n), 60, 220) * np.exp(-np.clip(x, 0, None) / .03) * (x >= 0) * .8
    x = t - .9
    rattle = band(rng.standard_normal(n), 2500, 7000) * (np.sin(2 * np.pi * 22 * t) > .5) * np.exp(-np.clip(x, 0, None) / .35) * (x >= 0) * .35
    return out + rattle


def speaker():
    # Four seconds of a drive-in speaker on its post with nobody in the stall: the radio station the stand plays, very small, a song
    # with a voice in it, all squeezed through a tin cone, and the hiss and hum of the line it runs on.
    n = seconds(4.0)
    t = times(n)
    beat = (np.sin(2 * np.pi * 2.0 * t) > .92) * band(rng.standard_normal(n), 200, 900) * .8
    chord = sum(np.sin(2 * np.pi * f * t) for f in (220, 277, 330)) * (.6 + .4 * np.sin(2 * np.pi * .5 * t))
    words = np.zeros(n)
    at = .4
    while at < 3.6:
        length = rng.uniform(.18, .4)
        x = t - at
        shape = np.clip(x / .04, 0, 1) * np.clip((length - x) / .05, 0, 1) * (x >= 0)
        f = rng.uniform(190, 260) * (1 + .1 * np.sin(2 * np.pi * 5 * x))
        voiced = np.sign(np.sin(2 * np.pi * np.cumsum(f) / RATE)) * .4
        words += (band(voiced, 400, 1000) + band(voiced, 1200, 2600) * .5) * shape
        at += length + rng.choice((.06, .1, .4), p=(.5, .3, .2))
    hum = np.sin(2 * np.pi * 60 * t) * .08
    hiss = band(rng.standard_normal(n), 3000, 6000) * .08
    cone = band(beat + chord * .25 + words, 450, 3200)
    edge = np.clip(t / .3, 0, 1) * np.clip((4.0 - t) / .3, 0, 1)
    return (cone + hum + hiss) * edge


def tin():
    # A loose sheet of corrugated tin on the gin's wall, lifted by a gust and let fall twice: a hollow clatter, the sheet ringing on
    # a few low bending partials, and a dry rattle at the nail that holds it.
    n = seconds(2.6)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.05, 1.0), (.62, .7), (.74, .4)):
        x = t - at
        on = x >= 0
        ring = sum(a * np.sin(2 * np.pi * f * x + p) for f, a, p in ((147, 1, 0), (233, .7, 1), (389, .5, 2), (611, .3, .5)))
        out += ring * np.exp(-np.clip(x, 0, None) / .4) * on * level * .6
        out += band(rng.standard_normal(n), 300, 5000) * np.exp(-np.clip(x, 0, None) / .025) * on * level
    nail = band(rng.standard_normal(n), 3000, 8000) * (np.sin(2 * np.pi * 30 * t) > .6) * np.exp(-np.clip(t - .62, 0, None) / .3) * (t > .62) * .25
    gust = band(rng.standard_normal(n), 80, 500) * np.sin(np.pi * np.clip(t / 2.6, 0, 1)) * .3
    return out + nail + gust


def train():
    # A freight train's horn a long way off past the playa, at night: two long blasts and a short one of a chord of three reeds, softened
    # by the distance so only the body of it carries, each swelling in and dying away; and under them the low rumble of the train.
    n = seconds(7.0)
    t = times(n)
    out = np.zeros(n)
    for at, length in ((.3, 1.6), (2.3, 1.6), (4.3, .7)):
        x = t - at
        shape = np.clip(x / .25, 0, 1) * np.clip((length - x) / .4, 0, 1) * (x >= 0)
        chord = sum(np.sin(2 * np.pi * f * t) + .4 * np.sin(4 * np.pi * f * t) for f in (311, 370, 440))
        out += band(chord, 200, 1200) * shape
    rumble = band(rng.standard_normal(n), 25, 120) * .5 * np.clip(t / 2.0, 0, 1) * np.clip((7.0 - t) / 2.0, 0, 1)
    edge = np.clip(t / .3, 0, 1) * np.clip((7.0 - t) / 1.0, 0, 1)
    return (out * .7 + rumble) * edge


def wind():
    # Six seconds of wind over flat land with nothing much in its way: a broad low hush rising and falling in long gusts, a thin whistle
    # in the wires at the top of each gust, and the dry hiss of the cotton's leaves.
    n = seconds(6.0)
    t = times(n)
    gust = .5 + .5 * np.sin(2 * np.pi * t / 5.6 + .4) ** 2 * (.7 + .3 * np.sin(2 * np.pi * t / 1.9))
    hush = band(rng.standard_normal(n), 50, 600) * gust
    whistle = band(rng.standard_normal(n), 1100, 1500) * np.clip(gust - .75, 0, 1) * 1.8
    rustle = band(rng.standard_normal(n), 3000, 9000) * np.interp(t - .4, t, gust, left=gust[0]) ** 2 * .4
    edge = np.clip(t / .6, 0, 1) * np.clip((6.0 - t) / .6, 0, 1)
    return (hush + whistle * .35 + rustle) * edge


def tower():
    # The water tower cooling after a hot day: two sharp metallic ticks, a pause, a third, each with the ring of the empty steel above
    # it; and a long low groan of the tank settling, very soft.
    n = seconds(3.0)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.1, 1.0), (.35, .6), (1.6, .8)):
        x = t - at
        on = x >= 0
        out += band(rng.standard_normal(n), 2000, 7000) * np.exp(-np.clip(x, 0, None) / .004) * on * level
        out += sum(a * np.sin(2 * np.pi * f * x) for f, a in ((812, 1), (1347, .6), (2101, .3))) * np.exp(-np.clip(x, 0, None) / .5) * on * level * .3
    groan = band(rng.standard_normal(n), 40, 140) * np.sin(np.pi * np.clip(t / 3.0, 0, 1)) * .35
    groan += np.sin(2 * np.pi * (55 + 6 * t) * t) * np.sin(np.pi * np.clip((t - .8) / 2.0, 0, 1)) * (t > .8) * .2
    return out + groan


def sprinkler():
    # An impact sprinkler working a lawn at night: a tick and a burst of spray, a step at a time across its arc, a dozen steps; then the
    # quick ratchet of the arm going back to the start of it.
    n = seconds(5.0)
    t = times(n)
    out = np.zeros(n)
    for k in range(12):
        at = .12 + k * .3
        x = t - at
        on = x >= 0
        out += band(rng.standard_normal(n), 1800, 5200) * np.exp(-np.clip(x, 0, None) / .003) * on * .9
        out += band(rng.standard_normal(n), 2500, 9000) * np.exp(-np.clip(x, 0, None) / .09) * on * .35
    back = (t > 3.85) & (t < 4.55)
    clicks = (np.sin(2 * np.pi * 34 * t) > .6) * back
    out += band(rng.standard_normal(n), 1500, 4500) * clicks * .5
    out += band(rng.standard_normal(n), 3000, 9000) * back * .12
    return out


def windmill():
    # One turn of a stock-tank windmill in a light wind, about two and a half seconds: the whir of the wheel's sheet blades going round, a
    # dry squeal of the pump rod in its guide as it rises, the clunk of the rod turning over at the top, and a groan as it goes down.
    n = seconds(2.5)
    t = times(n)
    whir = band(rng.standard_normal(n), 300, 1400) * (.55 + .45 * np.sin(2 * np.pi * 7.2 * t) ** 2) * .35
    squeal = np.zeros(n)
    on = (t > .25) & (t < .95)
    x = np.clip((t - .25) / .7, 0, 1)
    squeal += np.sin(2 * np.pi * (1650 + 260 * x) * t) * np.sin(np.pi * x) * on * .22
    squeal += np.sin(2 * np.pi * (2420 + 300 * x) * t) * np.sin(np.pi * x) ** 2 * on * .08
    clunk = band(rng.standard_normal(n), 120, 900) * np.exp(-np.clip(t - 1.02, 0, None) / .03) * (t > 1.02) * .9
    clunk += np.sin(2 * np.pi * 180 * (t - 1.02)) * np.exp(-np.clip(t - 1.02, 0, None) / .08) * (t > 1.02) * .4
    down = (t > 1.15) & (t < 2.2)
    y = np.clip((t - 1.15) / 1.05, 0, 1)
    groan = np.sin(2 * np.pi * (240 - 60 * y) * t) * np.sin(np.pi * y) * down * .18 + band(rng.standard_normal(n), 150, 500) * np.sin(np.pi * y) * down * .12
    edge = np.clip(t / .15, 0, 1) * np.clip((2.5 - t) / .2, 0, 1)
    return (whir + squeal + clunk + groan) * edge


def pumpjack():
    # One stroke's bottom on a pumpjack: the beam's weight coming down onto the rods, a heavy knock where the slack takes up, the
    # counterweights' long sigh going round, and the engine's low thrum under it all.
    n = seconds(3.0)
    t = times(n)
    thrum = (np.sin(2 * np.pi * 29 * t) * .5 + np.sin(2 * np.pi * 58 * t) * .25) * (.8 + .2 * np.sin(2 * np.pi * 3.1 * t)) * .3
    thrum += band(rng.standard_normal(n), 40, 220) * .15
    knock = band(rng.standard_normal(n), 80, 1200) * np.exp(-np.clip(t - .4, 0, None) / .025) * (t > .4) * 1.2
    knock += sum(a * np.sin(2 * np.pi * f * (t - .4)) for f, a in ((96, 1), (233, .5), (611, .2))) * np.exp(-np.clip(t - .4, 0, None) / .18) * (t > .4) * .7
    y = np.clip((t - .55) / 2.0, 0, 1)
    sigh = band(rng.standard_normal(n), 200, 900) * np.sin(np.pi * y) * (t > .55) * .25
    creak = np.sin(2 * np.pi * (420 + 90 * y) * t) * np.sin(np.pi * y) ** 3 * (t > .55) * .1
    edge = np.clip(t / .2, 0, 1) * np.clip((3.0 - t) / .4, 0, 1)
    return (thrum + knock + sigh + creak) * edge


def guard():
    # A cattle guard under one step: the pipes ring against their crossbars, a hollow knock from the pit under them, a short dull rattle.
    n = seconds(.8)
    t = times(n)
    out = np.zeros(n)
    for at, level in ((.03, 1.0), (.09, .55), (.16, .3)):
        x = t - at
        on = x >= 0
        out += band(rng.standard_normal(n), 900, 4000) * np.exp(-np.clip(x, 0, None) / .006) * on * level
        out += sum(a * np.sin(2 * np.pi * f * x) for f, a in ((690, 1), (1180, .5), (1930, .25))) * np.exp(-np.clip(x, 0, None) / .12) * on * level * .4
    out += band(rng.standard_normal(n), 60, 300) * np.exp(-np.clip(t - .03, 0, None) / .07) * (t > .03) * .6
    return out


MAKERS = {'insects': insects, 'dog': dog, 'speaker': speaker, 'tin': tin, 'train': train, 'wind': wind, 'tower': tower, 'sprinkler': sprinkler,
          'windmill': windmill, 'pumpjack': pumpjack, 'guard': guard}


def main():
    folder = ASSETS / 'sounds/the_list'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'the_list.{name}',
               {'sounds': [{'name': f'the_oldest_house:the_list/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.the_list.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.the_list.{name}', subtitle)


if __name__ == '__main__':
    main()
