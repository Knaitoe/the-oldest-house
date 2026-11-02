"""Original sounds for the Night Operator (0.4.252): the rooms the exchange's jacks reach, each heard down a telephone line (thin, close,
the bottom and the top cut away, a little hiss and crackle on it), the exchange's own line, and the switchboard's buzzer.

Synthesized from oscillators and noise with Movie Night's building blocks; no recordings. Run from the repository root. Existing files
are kept, so the committed sounds only change when they are deleted first.
"""
import json
import sys
from pathlib import Path

# The cues, their subtitles and how far they carry. The lines are played to one listener at their own ear.
ROWS = {
    'line_water': ('On the line: water running', 4),
    'line_fridge': ('On the line: a fridge humming', 4),
    'line_boards': ('On the line: footsteps on bare boards', 4),
    'line_clock': ('On the line: a clock ticking', 4),
    'line_rain': ('On the line: rain on a window', 4),
    'line_radio': ('On the line: a radio between stations', 4),
    'line_kettle': ('On the line: a kettle coming to the boil', 4),
    'line_quiet': ('On the line: nothing', 4),
    'line_exchange': ('On the line: a switchboard, and breathing', 4),
    'buzz': ('A switchboard buzzes', 12),
}
ASSET_ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'


def check():
    sounds = json.loads((ASSET_ROOT / 'sounds.json').read_text())
    lang = json.loads((ASSET_ROOT / 'lang/en_us.json').read_text())
    for name, (subtitle, reach) in ROWS.items():
        path = ASSET_ROOT / f'sounds/operator/{name}.ogg'
        assert path.exists() and path.stat().st_size > 1000, path
        assert sounds[f'operator.{name}'] == {'sounds': [{'name': f'the_oldest_house:operator/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.operator.{name}'}, name
        assert lang[f'subtitles.the_oldest_house.operator.{name}'] == subtitle, name
    print(f'Verified the exchange\'s {len(ROWS)} original cues.')


if __name__ == '__main__' and '--check' in sys.argv:
    check()
    sys.exit(0)

import numpy as np  # noqa: E402
import soundfile  # noqa: E402

from generate_movie_night_audio import ASSETS, RATE, seconds, times, band, envelope, adsr, place, normalized, update  # noqa: E402

rng = np.random.default_rng(252)
LENGTH = 3.6


def telephone(x, hiss=.035, crackle=.004):
    """Down a line: the bottom and the top gone, a little hard at the peaks, the line's own hiss and an odd crackle on it."""
    n = len(x)
    out = band(x, 320, 3300)
    out = np.tanh(out * 1.6) / 1.6
    out = out + band(rng.standard_normal(n), 400, 3200) * hiss
    pops = rng.random(n) < crackle / 40
    out[pops] += rng.standard_normal(int(pops.sum())) * .25
    return band(out, 300, 3400) * adsr(n, .04, .12)


def tick(m=.04, f=1800, gain=1.0):
    k = seconds(m)
    return (band(rng.standard_normal(k), 1200, 3200) * envelope(k, .0005, .006) + np.sin(2 * np.pi * f * times(k)) * envelope(k, .0005, .008) * .5) * gain


def line_water():
    # A tap left running into a sink: a steady bright rush with a slow wobble in it, and the odd spatter.
    n = seconds(LENGTH)
    t = times(n)
    out = band(rng.standard_normal(n), 700, 3000) * (.8 + .2 * np.sin(2 * np.pi * 3.3 * t)) * .5
    out += band(rng.standard_normal(n), 400, 1100) * .25
    for at in rng.uniform(.2, LENGTH - .3, 7):
        place(out, band(rng.standard_normal(seconds(.08)), 1000, 2600) * envelope(seconds(.08), .002, .02) * .3, at)
    return telephone(out)


def line_fridge():
    # A fridge's motor: its hum's upper partials (the line has no bottom to carry the rest), a rattle in its housing.
    n = seconds(LENGTH)
    t = times(n)
    hum = sum(np.sin(2 * np.pi * f * t) * w for f, w in ((350, .6), (400, .5), (470, .3), (700, .2), (1050, .08)))
    hum *= 1 + .1 * np.sin(2 * np.pi * 1.3 * t)
    rattle = band(rng.standard_normal(n), 500, 1400) * (.5 + .5 * np.sin(2 * np.pi * 25 * t)) * .25
    return telephone(hum * .4 + rattle)


def line_boards():
    # Someone walking on bare boards in an empty room: slow heavy steps, a creak under two of them, the room's echo.
    out = np.zeros(seconds(LENGTH))
    for i, at in enumerate((.25, .95, 1.65, 2.4, 3.05)):
        k = seconds(.25)
        step = band(rng.standard_normal(k), 300, 1200) * envelope(k, .003, .04) + np.sin(2 * np.pi * 380 * times(k)) * envelope(k, .003, .05) * .5
        place(out, step * (.9 if i % 2 else .75), at)
        if i in (1, 3):
            c = seconds(.35)
            tc = times(c)
            creak = np.sin(2 * np.pi * (520 + 140 * np.sin(2 * np.pi * 2.5 * tc)) * tc) * adsr(c, .05, .2) * (.6 + .4 * np.sin(2 * np.pi * 31 * tc)) * .25
            place(out, creak, at + .05)
    echo = np.zeros_like(out)
    d = seconds(.11)
    echo[d:] = out[:-d] * .3
    return telephone(out + echo)


def line_clock():
    # A clock in a quiet room, nothing else: tick, tock, the second of each pair a little lower.
    out = np.zeros(seconds(LENGTH))
    for i in range(7):
        place(out, tick(f=1900 if i % 2 == 0 else 1500, gain=.8 if i % 2 == 0 else .6), .15 + i * .5)
    return telephone(out, hiss=.03)


def line_rain():
    # Rain against a window: a soft wash and a scatter of drops tapping the glass.
    n = seconds(LENGTH)
    out = band(rng.standard_normal(n), 900, 3000) * .18
    for at in rng.uniform(.05, LENGTH - .05, 120):
        k = seconds(.02)
        place(out, band(rng.standard_normal(k), 1500, 3200) * envelope(k, .0005, .004) * rng.uniform(.2, .6), at)
    return telephone(out)


def line_radio():
    # A radio between stations: hiss, a whistle wandering in pitch, and for a moment someone talking too far off to make out.
    n = seconds(LENGTH)
    t = times(n)
    out = band(rng.standard_normal(n), 500, 3200) * .35
    whistle = np.sin(2 * np.pi * np.cumsum(1300 + 600 * np.sin(2 * np.pi * .4 * t)) / RATE) * .18 * (.5 + .5 * np.sin(2 * np.pi * .7 * t))
    syll = (np.sin(2 * np.pi * 3.4 * t) > .1).astype(float)
    syll = np.convolve(syll, np.ones(600) / 600, mode='same')
    voice = (np.sin(2 * np.pi * np.cumsum(170 * (1 + .1 * np.sin(2 * np.pi * .8 * t))) / RATE) * 0 + band(rng.standard_normal(n), 500, 1500)) * syll * .3
    voice *= np.clip((t - 1.4) / .3, 0, 1) * np.clip((2.6 - t) / .3, 0, 1)
    return telephone(out + whistle + voice, hiss=.02)


def line_kettle():
    # A kettle coming to the boil: a rising rush and rumble, bubbles thickening, and the first breath of its whistle at the end.
    n = seconds(LENGTH)
    t = times(n)
    rise = np.clip(t / LENGTH, 0, 1)
    out = band(rng.standard_normal(n), 400, 1800) * (.15 + .45 * rise)
    for at in rng.uniform(.3, LENGTH - .1, 40):
        k = seconds(.03)
        place(out, np.sin(2 * np.pi * rng.uniform(700, 1600) * times(k)) * envelope(k, .001, .01) * .25 * min(1, at / LENGTH * 1.6), at)
    whistle = np.sin(2 * np.pi * (1900 + 300 * rise) * t) * np.clip((t - 2.7) / .8, 0, 1) * .25
    return telephone(out + whistle)


def line_quiet():
    # Nothing on the line but the line: its hiss, and now and then a click as if someone had picked up and put down.
    n = seconds(LENGTH)
    out = np.zeros(n)
    for at in (.9, 2.6):
        place(out, tick(.03, 1200, .5), at)
    return telephone(out, hiss=.06, crackle=.01)


def line_exchange():
    # A switchroom, heard down its own line: relays clicking in a rack, a lamp's hum, and someone breathing close to a mouthpiece.
    n = seconds(LENGTH)
    t = times(n)
    out = (np.sin(2 * np.pi * 400 * t) * .08 + np.sin(2 * np.pi * 800 * t) * .04)
    for at in (.3, .34, 1.2, 1.9, 1.93, 2.95):
        place(out, tick(.025, 2400, .6), at)
    for at in (.4, 2.1):
        k = seconds(1.2)
        breath = band(rng.standard_normal(k), 500, 2200) * adsr(k, .45, .6) * .35
        place(out, breath, at)
    return telephone(out, hiss=.03)


def buzz():
    # The switchboard's buzzer: two short burrs, a coil rattling a striker.
    out = np.zeros(seconds(1.2))
    for at in (.02, .5):
        k = seconds(.32)
        tk = times(k)
        burr = np.sign(np.sin(2 * np.pi * 120 * tk)) * .3 + band(rng.standard_normal(k), 200, 2000) * (.5 + .5 * np.sign(np.sin(2 * np.pi * 60 * tk))) * .3
        place(out, band(burr, 100, 3000) * adsr(k, .005, .02), at)
    return out


MAKERS = {'line_water': line_water, 'line_fridge': line_fridge, 'line_boards': line_boards, 'line_clock': line_clock, 'line_rain': line_rain,
          'line_radio': line_radio, 'line_kettle': line_kettle, 'line_quiet': line_quiet, 'line_exchange': line_exchange, 'buzz': buzz}


def main():
    folder = ASSETS / 'sounds/operator'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (subtitle, reach) in ROWS.items():
        target = folder / f'{name}.ogg'
        if not target.exists():
            soundfile.write(str(target), normalized(MAKERS[name]()).astype(np.float32), RATE, format='OGG', subtype='VORBIS')
        update(ASSETS / 'sounds.json', f'operator.{name}',
               {'sounds': [{'name': f'the_oldest_house:operator/{name}', 'attenuation_distance': reach}], 'subtitle': f'subtitles.the_oldest_house.operator.{name}'})
        update(ASSETS / 'lang/en_us.json', f'subtitles.the_oldest_house.operator.{name}', subtitle)


if __name__ == '__main__':
    main()
