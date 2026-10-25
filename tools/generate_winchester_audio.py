#!/usr/bin/env python3
"""The Winchester house's three cues (0.4.121), synthesized deterministically: a carpenter driving a nail behind the wall,
a hand saw drawn through a board, and one stroke of the hall clock that strikes thirteen. Registers each with a subtitle.
"""
import json
import math
import random
from pathlib import Path

import numpy as np
import soundfile

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house'
OUT = ROOT / 'sounds/literary'
SR = 22050
CAPTIONS = {
    'winchester_hammer': 'A hammer drives a nail behind the wall',
    'winchester_saw': 'A saw works through a board behind the wall',
    'winchester_chime': 'The clock strikes',
}


def envelope(n, attack, release):
    t = np.arange(n) / SR
    return np.minimum(1, np.minimum(t / max(attack, 1e-4), (n / SR - t) / max(release, 1e-4))).clip(0, 1)


def hammer():
    """Four blows, the last two harder: a dull knock with the board ringing a little, heard through plaster."""
    rng = np.random.default_rng(1101)
    out = np.zeros(int(SR * 1.9))
    for i, (at, force) in enumerate(((0.05, .55), (.48, .6), (.92, .85), (1.36, 1.0))):
        n = int(SR * .32)
        t = np.arange(n) / SR
        knock = rng.uniform(-1, 1, n) * np.exp(-t * 90)
        board = sum(np.sin(2 * math.pi * f * t) * a for f, a in ((182, .5), (311, .3), (547, .14))) * np.exp(-t * 24)
        nail = np.sin(2 * math.pi * (1630 + 120 * i) * t) * np.exp(-t * 60) * .12
        s = int(SR * at)
        out[s:s + n] += force * (knock * .6 + board + nail)
    # The wall between: a soft low-pass.
    smooth = np.convolve(out, np.ones(6) / 6, mode='same')
    return smooth / np.abs(smooth).max() * .7


def saw():
    """Six strokes of a hand saw, push and pull, the teeth rasping at a rate that rises and falls with each stroke."""
    rng = random.Random(1102)
    n = int(SR * 2.4)
    out = np.zeros(n)
    t = np.arange(n) / SR
    stroke = .4
    for k in range(6):
        s, e = int(SR * k * stroke), int(SR * (k + 1) * stroke)
        u = (t[s:e] - k * stroke) / stroke
        speed = np.clip(np.sin(math.pi * u), 0, 1)
        teeth = 70 + 110 * speed
        phase = np.cumsum(teeth) / SR
        rasp = (np.mod(phase, 1) < .35).astype(float) * 2 - 1
        noise = np.array([rng.uniform(-1, 1) for _ in range(e - s)])
        level = (.75 if k % 2 == 0 else .55) * speed ** .7
        out[s:e] = level * (rasp * .25 + noise * .55) * (.6 + .4 * np.sin(2 * math.pi * 7 * u) ** 2)
    hp = out - np.convolve(out, np.ones(9) / 9, mode='same')
    body = np.convolve(out, np.ones(4) / 4, mode='same') * .4
    mix = hp * .8 + body
    return mix / np.abs(mix).max() * .6 * envelope(n, .02, .2)


def chime():
    """One deep stroke of a hall clock's gong: a struck bar with inharmonic partials and a long, slowly beating decay."""
    n = int(SR * 2.6)
    t = np.arange(n) / SR
    base = 131.0
    parts = ((1.0, .55, 1.6), (2.76, .3, 2.6), (5.4, .16, 4.2), (8.93, .08, 6.5), (1.004, .25, 1.4))
    v = sum(a * np.sin(2 * math.pi * base * r * t) * np.exp(-t * d) for r, a, d in parts)
    strike = np.random.default_rng(1103).uniform(-1, 1, n) * np.exp(-t * 140) * .25
    mix = v + strike
    return mix / np.abs(mix).max() * .75 * envelope(n, .004, .3)


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    for name, fn in (('winchester_hammer', hammer), ('winchester_saw', saw), ('winchester_chime', chime)):
        soundfile.write(str(OUT / f'{name}.ogg'), fn().astype(np.float32), SR, format='OGG', subtype='VORBIS')
    sounds = ROOT / 'sounds.json'
    s = json.loads(sounds.read_text())
    lang = ROOT / 'lang/en_us.json'
    l = json.loads(lang.read_text())
    for name, caption in CAPTIONS.items():
        s['literary.' + name] = {'subtitle': 'subtitles.the_oldest_house.' + name, 'sounds': [{'name': 'the_oldest_house:literary/' + name}]}
        l['subtitles.the_oldest_house.' + name] = caption
    sounds.write_text(json.dumps(s, indent=2) + '\n')
    lang.write_text(json.dumps(l, ensure_ascii=False, indent=2) + '\n')
    print(f'Generated {len(CAPTIONS)} Winchester cues with registered subtitles.')


if __name__ == '__main__':
    main()
