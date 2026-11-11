#!/usr/bin/env python3
"""0.4.448: the names cut in the Triple Tree (owner: "The tree is old. So is the town and its secrets. There should be names all over
this tree."). Draws the pictures laid over the bark of the_oldest_house:borrasca_names, its models and its states.

Each picture is sixteen pixels square and clear but for what is cut in it: two names in a three-by-five alphabet, pale tan where the
cut has weathered and dark where the bark has closed over an old one, and here and there a flat scraped patch where a name was taken
out. No name is on the Tree twice. Four more pictures are the two names beside the reader's own carving: both there; one scraped
out, the cut still pale; both scraped out; and both patches years later, gone grey.

The block's model is a block of bark (the game's own oak bark, used in place, not copied) with one of these pictures a hair proud of
each of its four sides, a different one on each. Run with --check to verify the files without writing them.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'
FACES = 20

FONT = {
    'A': '010101111101101', 'B': '110101110101110', 'C': '011100100100011', 'D': '110101101101110', 'E': '111100110100111',
    'F': '111100110100100', 'G': '011100101101011', 'H': '101101111101101', 'I': '111010010010111', 'J': '001001001101010',
    'K': '101101110101101', 'L': '100100100100111', 'M': '101111111101101', 'N': '110101101101101', 'O': '010101101101010',
    'P': '110101110100100', 'R': '110101110110101', 'S': '011100010001110', 'T': '111010010010010', 'U': '101101101101111',
    'V': '101101101101010', 'W': '101101101111101', 'Y': '101101010010010', '+': '000010111010000',
}
# Names long grown over, and names of people who are grown now and still in town.
OLD = ['RUTH', 'CAL', 'OTIS', 'MAE', 'ROY', 'IDA', 'CLEM', 'FAY', 'WES', 'DOT', 'AMOS', 'BO', 'NORA', 'HAL', 'LOU', 'VERN', 'ETTA',
       'JUNE', 'ABE', 'MAC', 'OPAL', 'SID', 'ROSA', 'T+L', 'E+W', 'R+M']
GROWN = ['GUS', 'EARL', 'KURT', 'RAY', 'DEB', 'LYLE', 'PAM', 'DON', 'TODD', 'KIM', 'RICK', 'LORI', 'MIKE', 'J+S', 'C+D', 'BETH', 'DALE']
WORDS = OLD + GROWN
TONES = {'fresh': (238, 218, 176), 'grown': (198, 172, 126), 'old': (30, 22, 14)}


def cut(px, x, y, word, tone, rnd):
    r, g, b = TONES[tone]
    for ch in word:
        for i, bit in enumerate(FONT[ch]):
            if bit == '1' and 0 <= x + i % 3 < 16 and 0 <= y + i // 3 < 16:
                j = rnd.randint(-7, 7)
                px[x + i % 3, y + i // 3] = (r + j, g + j, b + j, 255)
        x += 4


def scrape(px, x, y, width, rnd, fresh):
    """Where a name was: the bark taken off flat, pale while it is new and grey once it has weathered."""
    r, g, b = (210, 184, 138) if fresh else (116, 96, 70)
    for dy in range(5):
        for dx in range(width):
            if rnd.random() < .1 or not 0 <= x + dx < 16:
                continue
            j = rnd.randint(-10, 10)
            px[x + dx, y + dy] = (r + j, g + j, b + j, 255)


def face(k):
    rnd = random.Random(4480 + k); im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    order = list(range(len(WORDS))); random.Random(4479).shuffle(order)
    for row, y in enumerate((1 + rnd.randint(0, 1), 9 + rnd.randint(0, 1))):
        if (k * 2 + row) % 9 == 4:
            scrape(px, rnd.randint(0, 2), y, rnd.randint(10, 13), rnd, False); continue
        word = WORDS[order[(k * 2 + row) % len(order)]]
        cut(px, rnd.randint(0, max(0, 16 - len(word) * 4 + 1)), y, word, 'old' if word in OLD else 'grown', rnd)
    return im


def friends(stage):
    """PAT and KEN, cut this year, beside the carving. Stage 1: his is scraped out; stage 2: hers too; stage 3: both patches years
    old, gone grey."""
    rnd = random.Random(4490 + min(stage, 2)); im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    if stage >= 1:
        scrape(px, 1, 2, 12, rnd, stage < 3)
    else:
        cut(px, 2, 2, 'PAT', 'fresh', rnd)
    if stage >= 2:
        scrape(px, 3, 10, 12, rnd, stage < 3)
    else:
        cut(px, 4, 10, 'KEN', 'fresh', rnd)
    return im


def textures():
    t = {f'names_{k:02d}': face(k) for k in range(FACES)}
    t.update({f'friends_{s}': friends(s) for s in range(4)})
    return t


def model(n, e, s, w):
    bark = {'texture': '#bark'}
    sides = {d: {**bark, 'cullface': d} for d in ('north', 'south', 'east', 'west', 'up', 'down')}
    lift = .05
    def over(lo, hi, d, key):
        return {'from': lo, 'to': hi, 'faces': {d: {'texture': '#' + key, 'cullface': d}}}
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
            'textures': {'particle': 'minecraft:block/oak_log', 'bark': 'minecraft:block/oak_log',
                         'n': f'{MOD}:block/borrasca_names/{n}', 'e': f'{MOD}:block/borrasca_names/{e}',
                         's': f'{MOD}:block/borrasca_names/{s}', 'w': f'{MOD}:block/borrasca_names/{w}'},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': sides},
                         over([0, 0, -lift], [16, 16, 0], 'north', 'n'), over([0, 0, 16], [16, 16, 16 + lift], 'south', 's'),
                         over([16, 0, 0], [16 + lift, 16, 16], 'east', 'e'), over([-lift, 0, 0], [0, 16, 16], 'west', 'w')]}


def models():
    m = {}
    for k in range(FACES):
        m[f'bark_{k:02d}'] = model(*(f'names_{(k + 7 * i) % FACES:02d}' for i in range(4)))
    for s in range(4):
        # The two names face south, toward the trail; old names on the other three sides.
        m[f'bark_{FACES + s}'] = model('names_03', 'names_11', f'friends_{s}', 'names_16')
    return m


def states():
    return {'variants': {f'face={k}': {'model': f'{MOD}:block/borrasca_names/bark_{k:02d}' if k < FACES else f'{MOD}:block/borrasca_names/bark_{k}'} for k in range(FACES + 4)}}


def dump(data):
    return json.dumps(data, indent=2, sort_keys=True) + '\n'


def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); args = parser.parse_args()
    wrong = []
    for name, im in textures().items():
        path = ASSETS / 'textures/block/borrasca_names' / f'{name}.png'
        if args.check:
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                wrong.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path)
    files = {ASSETS / 'models/block/borrasca_names' / f'{name}.json': dump(data) for name, data in models().items()}
    files[ASSETS / 'blockstates/borrasca_names.json'] = dump(states())
    for path, text in files.items():
        if args.check:
            if not path.exists() or path.read_text() != text:
                wrong.append(str(path))
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(text)
    lang = ASSETS / 'lang/en_us.json'; words = json.loads(lang.read_text())
    if words.get(f'block.{MOD}.borrasca_names') != 'Carved Bark':
        if args.check:
            wrong.append('lang: block.' + MOD + '.borrasca_names')
        else:
            words[f'block.{MOD}.borrasca_names'] = 'Carved Bark'; lang.write_text(json.dumps(words, indent=2, ensure_ascii=False) + '\n')
    if wrong:
        raise SystemExit('The Tree\'s names are out of date: ' + ', '.join(wrong[:8]))
    print(('Verified' if args.check else 'Drew') + f' the Tree\'s names: {len(textures())} pictures, {len(models())} models.')


if __name__ == '__main__':
    main()
