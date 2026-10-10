#!/usr/bin/env python3
"""Ted the Caver 0.4.71: the packed rubble texture and six original, synthesized cave sounds.

Nothing is sampled. Every sound is built from seeded noise and simple oscillators, then written as mono Ogg Vorbis.
Run from anywhere; it rewrites only its own files and its own keys in sounds.json and en_us.json.
"""
import json
import math
import random
from pathlib import Path

import numpy as np
import soundfile
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
ASSETS = ROOT / 'assets/the_oldest_house'
RATE = 22050


def rubble_texture():
    rng = random.Random(471)
    size = 16
    # Packed chunks of deepslate and tuff with dark gaps between them; a few pale grit specks.
    seeds = [(rng.uniform(0, size), rng.uniform(0, size), rng.choice(('deep', 'deep', 'tuff', 'cobble'))) for _ in range(11)]
    tones = {'deep': (64, 64, 70), 'tuff': (104, 104, 92), 'cobble': (86, 84, 82)}
    image = Image.new('RGBA', (size, size))
    for y in range(size):
        for x in range(size):
            best = sorted(seeds, key=lambda s: min((x - s[0]) % size, (s[0] - x) % size) ** 2 + min((y - s[1]) % size, (s[1] - y) % size) ** 2)
            first, second = best[0], best[1]
            d1 = math.hypot(min((x - first[0]) % size, (first[0] - x) % size), min((y - first[1]) % size, (first[1] - y) % size))
            d2 = math.hypot(min((x - second[0]) % size, (second[0] - x) % size), min((y - second[1]) % size, (second[1] - y) % size))
            r, g, b = tones[first[2]]
            shade = 1.0 - 0.12 * (d1 / 3.0) + rng.uniform(-0.06, 0.06)
            if d2 - d1 < 0.9:
                shade *= 0.45  # the gap between two chunks
            if rng.random() < 0.035:
                r, g, b, shade = 150, 146, 132, 1.0  # grit
            image.putpixel((x, y), tuple(max(0, min(255, round(c * shade))) for c in (r, g, b)) + (255,))
    path = ASSETS / 'textures/block/cave_rubble.png'
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def marks_texture():
    """0.4.72: the marked rock. Cave stone with old cuts in it, half under a pale mineral crust; it should read as wall."""
    rng = random.Random(472)
    size = 16
    image = Image.new('RGBA', (size, size))
    base = []
    for y in range(size):
        row = []
        for x in range(size):
            v = 118 + rng.randint(-14, 12)
            if rng.random() < 0.12:
                v -= 22  # pits in the stone
            row.append(v)
        base.append(row)
    # Three tool cuts and a curved one that might be the line of a shoulder, cut deeper at the bottom edge of each stroke.
    cuts = set()
    for x0, y0, length in ((3, 3, 7), (6, 2, 8), (9, 4, 6)):
        for i in range(length):
            cuts.add((x0 + i // 3, y0 + i))
    for i in range(7):
        cuts.add((10 + round(2.2 * math.sin(i / 2.2)), 7 + i))
    # The crust: a pale calcite-coloured skin over the upper part, thin and broken where the cuts run under it.
    crust = set()
    for x in range(size):
        edge = 5 + round(2.5 * math.sin(x / 2.6 + 1.0)) + rng.randint(0, 1)
        for y in range(edge):
            if rng.random() < 0.86:
                crust.add((x, y))
    for y in range(size):
        for x in range(size):
            v = base[y][x]
            r, g, b = v, v, v + 2
            if (x, y) in cuts:
                r, g, b = v - 52, v - 52, v - 48
                if (x, y + 1) not in cuts:
                    r, g, b = v - 64, v - 64, v - 60
            elif (x - 1, y) in cuts:
                r, g, b = v + 14, v + 14, v + 12  # the lit lip of a cut
            if (x, y) in crust:
                mix = 0.55 if (x, y) in cuts else 0.72
                cr, cg, cb = 214, 206, 190
                r, g, b = (round(r * (1 - mix) + cr * mix), round(g * (1 - mix) + cg * mix), round(b * (1 - mix) + cb * mix))
            image.putpixel((x, y), tuple(max(0, min(255, c)) for c in (r, g, b)) + (255,))
    path = ASSETS / 'textures/block/cave_marks.png'
    image.save(path)
    (ASSETS / 'blockstates/cave_marks.json').write_text(json.dumps({'variants': {'': {'model': 'the_oldest_house:block/cave_marks'}}}, indent=2) + '\n')
    (ASSETS / 'models/block/cave_marks.json').write_text(json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': 'the_oldest_house:block/cave_marks'}}, indent=2) + '\n')
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    lang['block.the_oldest_house.cave_marks'] = 'Marked rock'
    write_json(path, lang, original)


def noise(seconds, seed):
    return np.random.default_rng(seed).uniform(-1, 1, round(RATE * seconds))


def lowpass(signal, alpha):
    out = np.empty_like(signal)
    acc = 0.0
    for i, v in enumerate(signal):
        acc += alpha * (v - acc)
        out[i] = acc
    return out


def comb(signal, hz, feedback):
    delay = max(1, round(RATE / hz))
    out = signal.copy()
    for i in range(delay, len(out)):
        out[i] += feedback * out[i - delay]
    return out


def envelope(t, rise, fall, total):
    return np.clip(np.minimum(t / rise, (total - t) / fall), 0, 1)


def normalize(signal, peak=0.8):
    return signal / max(1e-9, np.max(np.abs(signal))) * peak


def breath(seed, out):
    seconds = 3.6
    t = np.arange(round(RATE * seconds)) / RATE
    air = lowpass(noise(seconds, seed), 0.08) - lowpass(noise(seconds, seed + 1), 0.006)
    body = comb(air, 178 if out else 212, 0.55)
    shape = np.sin(np.pi * np.clip(t / seconds, 0, 1)) ** (1.2 if out else 2.4)
    if not out:
        shape = shape * np.clip((seconds - t) / 0.25, 0, 1)  # an in-breath stops short
    rumble = np.sin(2 * np.pi * (41 + 3 * np.sin(t * 1.3)) * t) * 0.18
    return normalize((body * 1.4 + rumble) * shape, 0.7)


def scrape():
    seconds = 1.7
    t = np.arange(round(RATE * seconds)) / RATE
    grit = lowpass(noise(seconds, 902), 0.35) - lowpass(noise(seconds, 903), 0.03)
    rng = np.random.default_rng(904)
    bursts = np.zeros_like(t)
    for start in np.cumsum(rng.uniform(0.05, 0.16, 18)):
        if start < seconds - 0.1:
            bursts += np.exp(-((t - start) / 0.035) ** 2) * rng.uniform(0.5, 1.0)
    grind = np.sin(2 * np.pi * 63 * t + 3 * np.sin(2 * np.pi * 7 * t)) * 0.25
    return normalize((grit * (0.35 + bursts) + grind * bursts) * envelope(t, 0.12, 0.4, seconds), 0.75)


def line_taut():
    seconds = 0.9
    t = np.arange(round(RATE * seconds)) / RATE
    glide = 128 + 110 * np.clip(t / 0.5, 0, 1)
    tone = np.sin(2 * np.pi * np.cumsum(glide) / RATE) * np.exp(-t * 3.2)
    rng = np.random.default_rng(905)
    creak = np.zeros_like(t)
    for start in np.cumsum(np.linspace(0.06, 0.012, 22)):
        if start < 0.5:
            creak += np.exp(-np.abs(t - start) / 0.0025) * rng.uniform(0.6, 1.0)
    fibre = lowpass(noise(seconds, 906), 0.3) * np.exp(-t * 5)
    return normalize(tone * 0.6 + creak * 0.5 + fibre * 0.3, 0.7)


def stone_roll():
    seconds = 2.3
    t = np.arange(round(RATE * seconds)) / RATE
    rumble = np.sin(2 * np.pi * (52 + 9 * np.sin(2 * np.pi * 1.7 * t)) * t)
    grit = lowpass(noise(seconds, 907), 0.2) - lowpass(noise(seconds, 908), 0.02)
    roll = envelope(t, 0.25, 0.35, 1.9) * (0.6 + 0.4 * np.sin(2 * np.pi * 2.3 * t) ** 2)
    thud_t = np.clip(t - 1.95, 0, None)
    thud = np.sin(2 * np.pi * 46 * thud_t) * np.exp(-thud_t * 14) * (t > 1.95)
    return normalize(rumble * 0.55 * roll + grit * 0.7 * roll + thud * 1.1, 0.8)


def chisel():
    seconds = 2.2
    t = np.arange(round(RATE * seconds)) / RATE
    out = np.zeros_like(t)
    for start in (0.15, 0.62, 1.31):
        local = np.clip(t - start, 0, None)
        on = t >= start
        ring = np.sin(2 * np.pi * 2150 * local) * np.exp(-local * 38) + np.sin(2 * np.pi * 3320 * local) * np.exp(-local * 55) * 0.5
        knock = lowpass(noise(seconds, int(start * 1000)), 0.4) * np.exp(-local * 70)
        out += (ring * 0.6 + knock) * on
    # Heard through rock: dull the top and let it hang.
    out = comb(lowpass(out, 0.18), 31, 0.45)
    return normalize(out, 0.6)


def gasp():
    """0.4.73: a sharp, ragged in-breath after blacking out, then a cough of grit."""
    seconds = 1.6
    t = np.arange(round(RATE * seconds)) / RATE
    rasp = lowpass(noise(seconds, 940), 0.55) - lowpass(noise(seconds, 941), 0.05)
    pull = np.clip(t / 0.08, 0, 1) * np.exp(-np.clip(t - 0.08, 0, None) * 5.5) * (t < 0.7)
    flutter = 0.65 + 0.35 * np.sin(2 * np.pi * 23 * t)
    cough_t = np.clip(t - 0.95, 0, None)
    cough = (lowpass(noise(seconds, 942), 0.3) * np.exp(-cough_t * 16) + np.sin(2 * np.pi * 140 * cough_t) * np.exp(-cough_t * 30) * 0.4) * (t > 0.95)
    return normalize(comb(rasp * pull * flutter, 640, 0.25) * 1.2 + cough * 0.8, 0.7)


SOUNDS = {
    'exhale': (lambda: breath(900, True), 'Air breathes out of the stone'),
    'inhale': (lambda: breath(910, False), 'Air draws back into the stone'),
    'scrape': (scrape, 'Stone scrapes behind you'),
    'line_taut': (line_taut, 'Line pulls taut'),
    'stone_roll': (stone_roll, 'Heavy stone rolls inward'),
    'chisel': (chisel, 'A chisel taps behind a wall'),
    'gasp': (gasp, 'You gasp for air'),
}


def write_json(path, data, original):
    text = json.dumps(data, indent=2, ensure_ascii=False)
    if original.endswith('\n'):
        text += '\n'
    path.write_text(text)


def sounds():
    folder = ASSETS / 'sounds/caver'
    folder.mkdir(parents=True, exist_ok=True)
    for name, (make, _) in SOUNDS.items():
        soundfile.write(str(folder / f'{name}.ogg'), make().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    path = ASSETS / 'sounds.json'
    original = path.read_text()
    table = json.loads(original)
    for name in SOUNDS:
        table[f'caver.{name}'] = {'sounds': [{'name': f'the_oldest_house:caver/{name}'}], 'subtitle': f'subtitles.the_oldest_house.caver.{name}'}
    write_json(path, table, original)


def lang_and_block():
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    for name, (_, subtitle) in SOUNDS.items():
        lang[f'subtitles.the_oldest_house.caver.{name}'] = subtitle
    lang['block.the_oldest_house.cave_rubble'] = 'Packed rubble'
    write_json(path, lang, original)
    (ASSETS / 'blockstates/cave_rubble.json').write_text(json.dumps({'variants': {'': {'model': 'the_oldest_house:block/cave_rubble'}}}, indent=2) + '\n')
    (ASSETS / 'models/block/cave_rubble.json').write_text(json.dumps({'parent': 'minecraft:block/cube_all', 'textures': {'all': 'the_oldest_house:block/cave_rubble'}}, indent=2) + '\n')
    tag = ROOT / 'data/minecraft/tags/block/mineable/pickaxe.json'
    tag.parent.mkdir(parents=True, exist_ok=True)
    tag.write_text(json.dumps({'replace': False, 'values': ['the_oldest_house:cave_rubble']}, indent=2) + '\n')


def gasp_only():
    soundfile.write(str(ASSETS / 'sounds/caver/gasp.ogg'), gasp().astype(np.float32), RATE, format='OGG', subtype='VORBIS')
    for path, key, value in ((ASSETS / 'sounds.json', 'caver.gasp', {'sounds': [{'name': 'the_oldest_house:caver/gasp'}], 'subtitle': 'subtitles.the_oldest_house.caver.gasp'}),
                             (ASSETS / 'lang/en_us.json', 'subtitles.the_oldest_house.caver.gasp', 'You gasp for air')):
        original = path.read_text()
        table = json.loads(original)
        table[key] = value
        write_json(path, table, original)


if __name__ == '__main__':
    import sys
    if sys.argv[1:] == ['marks']:
        marks_texture()  # 0.4.72 only; leaves the 0.4.71 files exactly as they are
    elif sys.argv[1:] == ['gasp']:
        gasp_only()  # 0.4.73 only
    else:
        rubble_texture()
        sounds()
        lang_and_block()
        marks_texture()
