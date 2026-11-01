"""Drafts and pressure (0.4.230): the draft rooms, a laundry and scullery, and three plain plastered rooms with a ceiling grate.

Draws, in native Minecraft pixels from fixed seeds: three materials (distempered plaster, scrubbed boards, scullery quarry tiles) and
every fitting of the draft_fitting block, its models facing north and turned by its facing: a wall vent with its grille shut and open;
a sash window open and shut; a sheet on the line hanging still and bellied by the air; a curtain hanging still and pulled flat back
against its wall; the plaster where a door is hidden, plain (exactly the plaster round it), with dust lifting off its edges, and
swung open on a passage; a louvre shut and open; the ceiling grate; and the washing line. Writes the models and block states, and
the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/draft/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, 0, a))


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


PLASTER = '#c9cbb8'


def plaster():
    return new(base=PLASTER, seed=2300, spread=3)


def materials():
    m = {'plaster': plaster()}
    boards = new(base='#c4a878', seed=2301, spread=4)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(boards, x, y, '#a08858')
    m['boards'] = boards
    tile = new(base='#9a4a38', seed=2302, spread=6)
    for i in range(16):
        dot(tile, i, 7, '#6a3a2c'); dot(tile, i, 15, '#6a3a2c'); dot(tile, 7, i, '#6a3a2c'); dot(tile, 15, i, '#6a3a2c')
    m['tile'] = tile
    return m


def vent(opened):
    im = plaster()
    fill(im, 2, 3, 14, 13, '#5a5e58')
    for y in range(4, 12, 2):
        fill(im, 3, y, 13, y + 1, '#1a1c1a' if opened else '#7a7e78')
    frame(im, 2, 3, 14, 13, '#3a3e38')
    return im


def sash(opened):
    im = new(base='#e8e4d8', seed=2310, spread=2)
    frame(im, 0, 0, 16, 16, '#c8c4b8')
    if opened:
        fill(im, 2, 9, 14, 15, '#101418')
        fill(im, 2, 2, 14, 8, '#5a6a7a'); fill(im, 7, 2, 9, 8, '#e8e4d8')
        fill(im, 1, 7, 15, 9, '#d8d4c8')
    else:
        fill(im, 2, 2, 14, 7, '#5a6a7a'); fill(im, 2, 9, 14, 14, '#5a6a7a')
        fill(im, 7, 2, 9, 14, '#e8e4d8'); fill(im, 1, 7, 15, 9, '#d8d4c8')
    return im


def sheet():
    im = new(base='#ecebe2', seed=2312, spread=2)
    for x in range(0, 16, 5):
        for y in range(16):
            dot(im, x, y, '#d8d6cc')
    return im


def curtain():
    im = new(base='#6a7a5a', seed=2314, spread=3)
    for x in range(1, 16, 3):
        for y in range(16):
            dot(im, x, y, '#5a6a4a')
    return im


def seam_dust():
    im = plaster()
    for y in range(0, 16, 2):
        dot(im, 0, y, '#e8eade', 220); dot(im, 15, (y + 1) % 16, '#e8eade', 220)
    return im


def louvre(opened):
    im = plaster()
    fill(im, 2, 2, 14, 14, '#7a6a52')
    for y in range(3, 13, 2):
        fill(im, 3, y, 13, y + 1, '#1a1612' if opened else '#5a4a38')
    frame(im, 2, 2, 14, 14, '#4a3a28')
    dot(im, 13, 8, '#c8a858')
    return im


def grate():
    im = new(base='#4a4a48', seed=2318, spread=2)
    for x in range(1, 16, 3):
        for y in range(16):
            dot(im, x, y, '#141414')
    frame(im, 0, 0, 16, 16, '#3a3a38')
    return im


def line():
    return new(base='#d8ccb0', seed=2320, spread=2)


def passage():
    return new(base='#2a2a28', seed=2322, spread=2)


def textures():
    out = {f'block/draft/{k}.png': v for k, v in materials().items()}
    t = {'vent_0': vent(False), 'vent_1': vent(True), 'sash_0': sash(True), 'sash_1': sash(False), 'sheet': sheet(), 'curtain': curtain(),
         'seam_dust': seam_dust(), 'louvre_0': louvre(False), 'louvre_1': louvre(True), 'grate': grate(), 'line': line(), 'passage': passage(),
         'frame': new(base='#c8c4b8', seed=2324, spread=2)}
    out.update({f'block/draft/{k}.png': v for k, v in t.items()})
    return out


def element(lo, hi, textures, uv=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def cube(front, rest, particle=None):
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'r', 'north': 'f'})], {'r': rest, 'f': front}, particle or rest, 'solid')


def fitting_models():
    m = {}
    m['vent_0'] = cube('vent_0', 'plaster'); m['vent_1'] = cube('vent_1', 'plaster')
    m['sash_0'] = cube('sash_0', 'frame'); m['sash_1'] = cube('sash_1', 'frame')
    m['louvre_0'] = cube('louvre_0', 'plaster'); m['louvre_1'] = cube('louvre_1', 'plaster')
    # A sheet hangs from the line above it, down past the bottom of its block; bellied, it curves out toward the way it faces.
    m['sheet_0'] = model([element([0, -10, 7.5], [16, 16, 8.5], {'all': 's'})], {'s': 'sheet'}, 'sheet')
    m['sheet_1'] = model([element([0, 8, 6.5], [16, 16, 7.5], {'all': 's'}), element([0, -2, 4], [16, 8, 5], {'all': 's'}), element([0, -10, 6], [16, -2, 7], {'all': 's'})], {'s': 'sheet'}, 'sheet')
    # A curtain on its rail before a wall; pulled, it lies flat against the wall behind it.
    m['curtain_0'] = model([element([1, 0, 12], [15, 16, 14], {'all': 'c'}), element([0, 15, 14], [16, 16, 15], {'all': 'l'})], {'c': 'curtain', 'l': 'line'}, 'curtain')
    m['curtain_1'] = model([element([1, 0, 15], [15, 16, 15.8], {'all': 'c'}), element([0, 15, 14], [16, 16, 15], {'all': 'l'})], {'c': 'curtain', 'l': 'line'}, 'curtain')
    # The seam: plain, it is the plaster itself; with the air at it, dust lifts off its edges; open, a panel swung back on a dark passage.
    m['seam_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p'})], {'p': 'plaster'}, 'plaster', 'solid')
    m['seam_1'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'p', 'north': 'd'})], {'p': 'plaster', 'd': 'seam_dust'}, 'plaster', 'solid')
    m['seam_2'] = model([element([0, 0, 0], [2, 16, 16], {'all': 'p'})], {'p': 'plaster'}, 'plaster')
    m['grate_0'] = model([element([0, 14, 0], [16, 16, 16], {'all': 'g'})], {'g': 'grate'}, 'grate')
    m['line_0'] = model([element([0, 14, 7.5], [16, 15, 8.5], {'all': 'l'})], {'l': 'line'}, 'line')
    return m


KINDS = ['vent', 'sash', 'sheet', 'curtain', 'seam', 'louvre', 'grate', 'line']
STAGES = {'vent': 2, 'sash': 2, 'sheet': 2, 'curtain': 2, 'seam': 3, 'louvre': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'draft_fitting': 'Draft Fitting', 'draft_plaster': 'Distempered Plaster', 'draft_boards': 'Scrubbed Boards', 'draft_tile': 'Quarry Tile'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/draft_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/draft_{name}.json'] = {'variants': {'': {'model': NS + f'block/draft_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/draft/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/draft/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/draft_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/draft/{tex.split("draft/")[1]}.png' in images, (key, tex)
    # The hidden door, unlit by the air, is the plaster round it: the same texture, not a near likeness.
    assert models['seam_0']['textures']['p'] == P + 'plaster'
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for block, name in NAMES.items():
        key = f'block.the_oldest_house.{block}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the draft rooms: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
