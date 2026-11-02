"""The Night Operator (0.4.252): the exchange's cord switchboard.

Draws, in native Minecraft pixels from fixed seeds, every fitting of the exchange_fitting block, its models facing north (their face to
the room) and turned by its facing: a line's jack in a black panel framed in oak, a brass socket ring and a lamp cap over it, the lamp
dark, lit, or lit with a cord's plug in the socket and its cord hanging; the incoming line's panel with its larger lamp, dark or lit;
the field of small lamps over the jacks with their brass label strips; the board's shelf of plugs and keys; the headset hook with one
headset or two; the cork board of shift notes. Writes the models and block states, and the block's name. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/exchange/'


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


OAK = '#6a4628'
OAK_DARK = '#46301a'
BAKELITE = '#1c1a18'
BRASS = '#b08a3a'
BRASS_DARK = '#6c5220'
AMBER = '#5a3a14'
AMBER_LIT = '#ffcc5a'
CORD = '#6a1e1a'


def oak():
    im = new(base=OAK, seed=2800, spread=4)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, shade(OAK, -10))
    return im


def jack_face(lit):
    """One line on the board: a black panel in an oak frame, the lamp cap above, the brass ring of the socket below it."""
    im = new(base=BAKELITE, seed=2810, spread=2)
    frame(im, 0, 0, 16, 16, OAK); frame(im, 1, 1, 15, 15, OAK_DARK)
    fill(im, 6, 3, 10, 6, AMBER_LIT if lit else AMBER)
    if lit:
        dot(im, 7, 3, '#fff2c0')
    frame(im, 6, 3, 10, 6, BRASS_DARK)
    fill(im, 6, 8, 10, 12, BRASS); fill(im, 7, 9, 9, 11, '#0a0908')
    frame(im, 6, 8, 10, 12, BRASS_DARK)
    fill(im, 4, 13, 12, 14, '#d8cfb4')
    return im


def caller_face(lit):
    """The incoming line: a larger lamp under a brass strip stamped LINE, and its own socket."""
    im = new(base=BAKELITE, seed=2820, spread=2)
    frame(im, 0, 0, 16, 16, OAK); frame(im, 1, 1, 15, 15, OAK_DARK)
    fill(im, 3, 2, 13, 4, BRASS)
    for x in (5, 7, 9, 11):
        dot(im, x, 3, BRASS_DARK)
    fill(im, 5, 5, 11, 10, AMBER_LIT if lit else AMBER)
    if lit:
        fill(im, 6, 6, 8, 7, '#fff2c0')
    frame(im, 5, 5, 11, 10, BRASS_DARK)
    fill(im, 6, 11, 10, 14, BRASS); fill(im, 7, 12, 9, 13, '#0a0908')
    return im


def lamp_field():
    """The lamps over the lines: two rows of small caps, each over its brass label strip."""
    im = new(base=BAKELITE, seed=2830, spread=2)
    frame(im, 0, 0, 16, 16, OAK); frame(im, 1, 1, 15, 15, OAK_DARK)
    for row in (3, 9):
        for x in (3, 7, 11):
            fill(im, x, row, x + 2, row + 2, AMBER)
            dot(im, x, row, shade(AMBER, 20))
        fill(im, 3, row + 3, 13, row + 4, BRASS_DARK)
    return im


def desk_front():
    im = oak()
    frame(im, 0, 0, 16, 16, OAK_DARK)
    for x in (3, 7, 11):
        fill(im, x, 4, x + 2, 7, '#2a2622'); dot(im, x, 4, '#d8cfb4')
    fill(im, 1, 10, 15, 11, OAK_DARK)
    return im


def desk_top():
    """The shelf from above: a row of plugs standing in their holes, their cords running back, and a row of keys."""
    im = new(base=OAK, seed=2840, spread=3)
    frame(im, 0, 0, 16, 16, OAK_DARK)
    for x in (2, 6, 10, 14):
        fill(im, x - 1, 3, x + 1, 5, BRASS); dot(im, x - 1, 3, '#e0c070')
        fill(im, x - 1, 5, x, 15, CORD)
    for x in (4, 8, 12):
        fill(im, x - 1, 10, x + 1, 12, '#2a2622'); dot(im, x - 1, 10, '#8a8478')
    return im


def cork():
    im = new(base='#a87a4a', seed=2850, spread=10)
    frame(im, 0, 0, 16, 16, OAK_DARK)
    rnd = random.Random(2851)
    for (x0, y0, x1, y1, paper) in ((2, 2, 7, 8, '#ece6d0'), (8, 3, 14, 7, '#f0e8a8'), (3, 9, 9, 14, '#e8e4dc'), (10, 9, 14, 13, '#ece6d0')):
        fill(im, x0, y0, x1, y1, paper)
        for y in range(y0 + 1, y1 - 1, 2):
            for x in range(x0 + 1, x1 - 1):
                if rnd.random() < .7:
                    dot(im, x, y, '#6a6458')
        dot(im, (x0 + x1) // 2, y0, '#b02a22')
    return im


def headset():
    return new(base='#1e1e22', seed=2860, spread=3)


def textures():
    t = {'oak': oak(), 'jack_0': jack_face(False), 'jack_1': jack_face(True), 'caller_0': caller_face(False), 'caller_1': caller_face(True),
         'lamps': lamp_field(), 'desk_front': desk_front(), 'desk_top': desk_top(), 'cork': cork(), 'headset': headset(),
         'brass': new(base=BRASS, seed=2870, spread=8), 'cord': new(base=CORD, seed=2872, spread=6)}
    return {f'block/exchange/{k}.png': v for k, v in t.items()}


def element(lo, hi, textures):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def panel(face):
    return element([0, 0, 6], [16, 16, 16], {'all': 'o', 'north': face})


def jack_model(stage):
    els = [panel('f')]
    if stage == 2:
        els.append(element([7, 8, 3], [9, 10, 6], {'all': 'b'}))
        els.append(element([7.5, 0, 3.5], [8.5, 8, 4.5], {'all': 'c'}))
    return model(els, {'o': 'oak', 'f': 'jack_1' if stage else 'jack_0', 'b': 'brass', 'c': 'cord'}, 'oak')


def headset_els(dz):
    """A headset hanging from the hook: the band over the top, an earpiece at each end."""
    return [element([4, 13, 12 - dz], [12, 14, 13 - dz], {'all': 'h'}),
            element([4, 9, 12 - dz], [5, 13, 13 - dz], {'all': 'h'}), element([11, 9, 12 - dz], [12, 13, 13 - dz], {'all': 'h'}),
            element([3, 6, 11.5 - dz], [6, 9, 13.5 - dz], {'all': 'h'}), element([10, 6, 11.5 - dz], [13, 9, 13.5 - dz], {'all': 'h'})]


def fitting_models():
    m = {}
    for stage in range(3):
        m[f'jack_{stage}'] = jack_model(stage)
    m['caller_0'] = model([panel('f')], {'o': 'oak', 'f': 'caller_0'}, 'oak')
    m['caller_1'] = model([panel('f')], {'o': 'oak', 'f': 'caller_1'}, 'oak')
    m['panel_0'] = model([panel('f')], {'o': 'oak', 'f': 'lamps'}, 'oak')
    m['desk_0'] = model([element([0, 0, 2], [16, 14, 16], {'all': 'o', 'north': 'f', 'up': 't'})], {'o': 'oak', 'f': 'desk_front', 't': 'desk_top'}, 'oak')
    hook = [element([7, 13, 13], [9, 15, 16], {'all': 'b'})]
    m['hook_0'] = model(hook + headset_els(0), {'b': 'brass', 'h': 'headset'}, 'brass')
    m['hook_1'] = model(hook + headset_els(0) + headset_els(2.5), {'b': 'brass', 'h': 'headset'}, 'brass')
    m['notes_0'] = model([element([0, 2, 14], [16, 14, 16], {'all': 'o', 'north': 'k'})], {'o': 'oak', 'k': 'cork'}, 'cork')
    return m


KINDS = ['jack', 'caller', 'panel', 'desk', 'hook', 'notes']
STAGES = {'jack': 3, 'caller': 2, 'hook': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 3
NAMES = {'exchange_fitting': 'Switchboard'}


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/exchange/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/exchange/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/exchange_fitting.json'] = {'variants': variants}
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
            assert f'block/exchange/{tex.split("exchange/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the exchange\'s switchboard: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
