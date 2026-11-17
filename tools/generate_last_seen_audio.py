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
    # 0.4.670, the night (docs/LAST_SEEN_0_4_670.md).
    'chain': ('A chain is wound round the gates', 32),
    'switch': ('A flashlight clicks on', 16),
    'voices': ('Men talk low on the avenue', 40),
    'rattle': ('Someone tries the back door', 16),
    'climb': ('Someone climbs the railing', 24),
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


def chain():
    # A length of chain drawn through iron bars and wound round twice: links rattling against each other, a dull clank on the bar each
    # time it comes round, and the padlock's hasp snapped shut at the end.
    n = seconds(2.6)
    t = times(n)
    out = np.zeros(n)
    for start, end in ((.05, .8), (.95, 1.7)):
        span = (t >= start) & (t < end)
        env = np.clip((t - start) / .05, 0, 1) * np.clip((end - t) / .1, 0, 1) * span
        links = np.zeros(n)
        at = start
        while at < end:
            x = t - at
            links += band(rng.standard_normal(n), 2500, 9000) * np.exp(-np.clip(x, 0, None) / .008) * (x >= 0) * rng.uniform(.4, 1)
            at += rng.uniform(.018, .05)
        out += links * env
        x = t - end + .05
        out += band(rng.standard_normal(n), 120, 900) * np.exp(-np.clip(x, 0, None) / .05) * (x >= 0) * 1.2
    x = t - 2.15
    hasp = band(rng.standard_normal(n), 1800, 7000) * np.exp(-np.clip(x, 0, None) / .004) * (x >= 0) * 1.4
    ring = np.sin(2 * np.pi * 3100 * t) * np.exp(-np.clip(x, 0, None) / .06) * (x >= 0) * .25
    return out + hasp + ring


def switch():
    # A flashlight's rubber thumb switch: the press, a hard click inside, and the faint ring of its tube.
    n = seconds(.35)
    t = times(n)
    press = band(rng.standard_normal(n), 300, 2000) * np.exp(-np.clip(t - .01, 0, None) / .01) * (t >= .01) * .4
    x = t - .05
    click = band(rng.standard_normal(n), 2000, 9000) * np.exp(-np.clip(x, 0, None) / .003) * (x >= 0) * 1.3
    tube = np.sin(2 * np.pi * 1650 * t) * np.exp(-np.clip(x, 0, None) / .05) * (x >= 0) * .12
    return press + click + tube


def voices():
    # Three men talking low some way off: voiced syllables at a man's pitch, each from a buzz shaped by two moving resonances, in
    # short phrases with gaps, one voice now and then over another. No words come through.
    n = seconds(5.0)
    t = times(n)
    out = np.zeros(n)
    for voice, (pitch, f1, f2, offset) in enumerate(((104, 520, 1250, .1), (122, 600, 1500, 1.4), (96, 480, 1100, 2.9))):
        at = offset
        while at < 4.6:
            words = rng.integers(2, 6)
            for _ in range(words):
                length = rng.uniform(.09, .22)
                x = t - at
                span = (x >= 0) & (x < length)
                env = np.sin(np.clip(x / length, 0, 1) * np.pi) * span
                glide = pitch * (1 + .06 * np.sin(2 * np.pi * rng.uniform(1.5, 3) * x))
                phase = 2 * np.pi * np.cumsum(glide) / RATE
                buzz = np.sign(np.sin(phase)) * .5 + np.sin(phase)
                shaped = band(buzz, f1 * rng.uniform(.8, 1.2) - 120, f1 * rng.uniform(.8, 1.2) + 160) + .5 * band(buzz, f2 - 200, f2 + 300)
                out += shaped * env * rng.uniform(.5, 1)
                at += length + rng.uniform(.03, .08)
            at += rng.uniform(.5, 1.2)
    hush = band(rng.standard_normal(n), 200, 2500) * .05
    return band(out, 80, 3000) + hush


def rattle():
    # A door handle tried from outside: the lever pressed down, the latch knocking in its keep twice, and the door shoved once in its
    # frame.
    n = seconds(1.6)
    t = times(n)
    out = np.zeros(n)
    for at in (.1, .45, .8):
        x = t - at
        out += band(rng.standard_normal(n), 900, 5000) * np.exp(-np.clip(x, 0, None) / .02) * (x >= 0) * .9
        out += band(rng.standard_normal(n), 100, 600) * np.exp(-np.clip(x, 0, None) / .04) * (x >= 0) * .7
    x = t - 1.15
    shove = band(rng.standard_normal(n), 60, 500) * np.exp(-np.clip(x, 0, None) / .09) * (x >= 0) * 1.4
    creak = band(rng.standard_normal(n), 300, 1400) * np.clip((t - 1.15) / .03, 0, 1) * np.exp(-np.clip(t - 1.15, 0, None) / .2) * .3
    return out + shove + creak


def climb():
    # A man going over an iron railing: a hand slapping the top bar, a boot scraping up the bars, the railing ringing as his weight
    # comes on it, and his landing on gravel on the far side.
    n = seconds(1.8)
    t = times(n)
    slap = band(rng.standard_normal(n), 400, 3000) * np.exp(-np.clip(t - .05, 0, None) / .02) * (t >= .05)
    scrape = band(rng.standard_normal(n), 1500, 6000) * np.clip((t - .25) / .03, 0, 1) * np.clip((.65 - t) / .05, 0, 1) * .5
    ring = (np.sin(2 * np.pi * 420 * t) + .5 * np.sin(2 * np.pi * 1130 * t)) * np.exp(-np.clip(t - .7, 0, None) / .4) * (t >= .7) * .35
    land = band(rng.standard_normal(n), 80, 2500) * np.exp(-np.clip(t - 1.35, 0, None) / .07) * (t >= 1.35) * 1.3
    return slap + scrape + ring + land


MAKERS = {'traffic': traffic, 'pigeons': pigeons, 'paper': paper, 'typewriter': typewriter, 'chain': chain, 'switch': switch,
          'voices': voices, 'rattle': rattle, 'climb': climb}


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
