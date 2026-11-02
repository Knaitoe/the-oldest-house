"""The Header Tank (0.4.256): a utility room's waterworks, and the utility room.

Draws, in native Minecraft pixels from fixed seeds: six materials (quarry tiles, glazed wall brick, the same brick with the dark tide
line of an old water level, with a fainter older one, galvanized plate, attic boards) and every part of the tank_fitting block, its
models facing north (front to the room, back to the wall) and turned by its facing: the slatted platform on its floats; the feed
valve's wheel, shut and open; the hand pump, its handle up and down; a pipe's mouth, dry and running; the gauge board for the tank
overhead, its float at five heights; a pipe on the wall (a run, a rise, a joint); and the transfer valve's dial, shut, turned one way
and turned the other. Writes the models, the block states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/tank/'


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


def line(im, x0, y0, x1, y1, colour):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), colour)


# ------------------------------------------------------------------------------------------------ materials
def tiles():
    # Quarry tiles, four to a block, worn paler where feet go, the grout dark.
    im = Image.new('RGBA', (16, 16)); rnd = random.Random(2560)
    tones = ['#9a5a3e', '#a4644a', '#8e5236', '#a05e42']
    for ty in range(2):
        for tx in range(2):
            base = tones[ty * 2 + tx]
            for y in range(8):
                for x in range(8):
                    im.putpixel((tx * 8 + x, ty * 8 + y), shade(base, rnd.randint(-5, 5)))
    for i in range(16):
        dot(im, i, 7, '#4a3428'); dot(im, i, 15, '#4a3428'); dot(im, 7, i, '#4a3428'); dot(im, 15, i, '#4a3428')
    return im


def glazed(seed):
    # Glazed wall brick, long and pale, a gloss line along each course.
    base, joint = '#dcdcd2', '#a8a8a0'
    im = new(base=base, seed=seed, spread=3)
    for row in range(4):
        y = row * 4 + 3
        for x in range(16):
            dot(im, x, y, joint); dot(im, x, row * 4, shade(base, 14)[:3])
        off = 0 if row % 2 == 0 else 8
        for x in range(off, 16, 16):
            for dy in range(3):
                dot(im, x, row * 4 + dy, joint)
    return im


def marked(strong):
    # The same brick with the tide line of a level the water used to stand at, the stain thickest at its top edge.
    im = glazed(2562 if strong else 2563); rnd = random.Random(2564 if strong else 2565)
    top, depth = (3, 9) if strong else (6, 5)
    for x in range(16):
        edge = top + (1 if rnd.random() < .3 else 0)
        dot(im, x, edge, '#3e3428' if strong else '#9a9480')
        for y in range(edge + 1, edge + depth):
            k = -62 + (y - edge) * 7 if strong else -18 + (y - edge) * 3
            px = im.getpixel((x, y)); dot(im, x, y, shade(px, k)[:3])
    return im


def plate():
    im = new(base='#8c9296', seed=2566, spread=4); rnd = random.Random(2567)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a8aeb2')
    frame(im, 0, 0, 16, 16, '#6a7074')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13), (7, 2), (7, 13)):
        dot(im, x, y, '#c4c8cc')
    return im


def boards():
    base = '#8e7656'
    im = new(base=base, seed=2568, spread=5)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -26)[:3])
    for x, y in ((4, 1), (12, 5), (2, 9), (10, 13)):
        dot(im, x, y, '#4a3a28')
    return im


def materials():
    return {'tiles': tiles(), 'brick': glazed(2561), 'mark': marked(True), 'faint': marked(False), 'plate': plate(), 'boards': boards()}


# ------------------------------------------------------------------------------------------------ fittings
def slats():
    # The platform seen from above: boards with gaps between them, the water showing through.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(2570)
    for x0 in (0, 4, 8, 12):
        for x in range(x0, x0 + 3):
            for y in range(16):
                dot(im, x, y, shade('#9a7e58', rnd.randint(-6, 6))[:3])
        for y in range(16):
            dot(im, x0 + 2, y, '#6e5a3e')
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#5a4630')
    return im


def floats():
    # The platform's edge: a board on top, then the drum of a float under it.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 2, '#8a6e4a'); fill(im, 0, 2, 16, 6, '#5a646a')
    for x in (3, 12):
        dot(im, x, 3, '#3a4044'); dot(im, x, 4, '#3a4044')
    return im


def iron(seed=2571):
    return new(base='#3a3e42', seed=seed, spread=3)


def pipe():
    im = new(base='#8a9094', seed=2572, spread=3)
    for y in range(16):
        dot(im, 0, y, '#5e6468'); dot(im, 15, y, '#5e6468'); dot(im, 5, y, '#a8aeb2')
    return im


def wheel(open_):
    # The valve's handwheel from the front: a rim, a hub and spokes, turned an eighth when open.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rim = '#8a2a20'
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6 <= d <= 7.6:
                dot(im, x, y, rim)
    if open_:
        line(im, 3, 3, 12, 12, rim); line(im, 3, 12, 12, 3, rim)
    else:
        line(im, 1, 8, 14, 8, rim); line(im, 8, 1, 8, 14, rim)
    fill(im, 6, 6, 10, 10, '#4a4e52'); dot(im, 7, 7, '#9a9ea2')
    return im


def pump_body():
    im = new(base='#2e4a3a', seed=2573, spread=3)
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#1e3428')
    return im


def mouth(running):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 7.6:
                dot(im, x, y, '#8a9094' if d > 4.5 else ('#4a6e86' if running else '#1a1c1e'))
    return im


def stream():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(2574)
    for y in range(16):
        for x in range(4, 12):
            if rnd.random() < .75:
                dot(im, x, y, shade('#5a86a8', rnd.randint(-10, 18))[:3], 210)
    return im


def gauge(level):
    # The gauge board: a scale of five marks, the float on its rod, the pointer at the tank's level.
    im = new(base='#e0d8c0', seed=2575, spread=2)
    frame(im, 0, 0, 16, 16, '#5a4a36')
    for i in range(5):
        y = 13 - i * 3
        fill(im, 2, y, 6 if i % 2 == 0 else 5, y + 1, '#2a2420')
    fill(im, 9, 1, 10, 15, '#6a6e72')
    y = 13 - level * 3
    fill(im, 8, y - 1, 12, y + 2, '#b03a2a'); fill(im, 5, y, 9, y + 1, '#b03a2a')
    return im


def joint():
    im = new(base='#7a8084', seed=2576, spread=3)
    frame(im, 0, 0, 16, 16, '#5a6064')
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        dot(im, x, y, '#b4babe')
    return im


def dial(state):
    # The transfer valve's dial plate: a lever across it, upright when shut, over to one side or the other when open.
    im = new(base='#b8b0a0', seed=2577, spread=2)
    frame(im, 0, 0, 16, 16, '#5a5040')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(im, x, y, '#3a3428')
    lever = '#2a2420'
    if state == 0:
        fill(im, 7, 2, 9, 14, lever)
    elif state == 1:
        line(im, 3, 7, 12, 7, lever); line(im, 3, 8, 12, 8, lever); line(im, 3, 7, 5, 5, lever); line(im, 3, 8, 5, 10, lever)
    else:
        line(im, 3, 7, 12, 7, lever); line(im, 3, 8, 12, 8, lever); line(im, 12, 7, 10, 5, lever); line(im, 12, 8, 10, 10, lever)
    fill(im, 7, 7, 9, 9, '#8a2a20')
    return im


def textures():
    out = {f'block/tank_{k}.png': v for k, v in materials().items()}
    t = {'slats': slats(), 'floats': floats(), 'iron': iron(), 'pipe': pipe(), 'wheel_0': wheel(False), 'wheel_1': wheel(True),
         'pump': pump_body(), 'mouth_0': mouth(False), 'mouth_1': mouth(True), 'stream': stream(), 'joint': joint(),
         'board': new(base='#6a5038', seed=2578, spread=3)}
    for level in range(5):
        t[f'gauge_{level}'] = gauge(level)
    for state in range(3):
        t[f'dial_{state}'] = dial(state)
    out.update({f'block/tank/{k}.png': v for k, v in t.items()})
    return out


def element(lo, hi, textures, uv=None, rotation=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        if rotation and f in rotation:
            faces[f]['rotation'] = rotation[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['platform_0'] = model([element([0, 13, 0], [16, 16, 16], {'up': 's', 'down': 's', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f'},
                                     uv={f: [0, 0, 16, 3] for f in ('north', 'south', 'east', 'west')}),
                             element([2, 10, 2], [6, 13, 14], {'all': 'd'}), element([10, 10, 2], [14, 13, 14], {'all': 'd'})],
                            {'s': 'slats', 'f': 'floats', 'd': 'iron'}, 'slats')
    for stage in range(2):
        m[f'valve_{stage}'] = model([element([7, 7, 13], [9, 9, 16], {'all': 'p'}), element([3, 3, 11], [13, 13, 12], {'north': 'w', 'south': 'w'}, uv={'north': [0, 0, 16, 16], 'south': [16, 0, 0, 16]}),
                                     element([7, 0, 14], [9, 16, 16], {'all': 'p'})],
                                    {'p': 'pipe', 'w': f'wheel_{stage}'}, 'pipe')
    for stage in range(2):
        handle = element([7, 13, 3], [9, 14, 11], {'all': 'i'}) if stage == 0 else element([7, 10, 3], [9, 11, 11], {'all': 'i'})
        m[f'pump_{stage}'] = model([element([5, 0, 7], [11, 12, 13], {'all': 'b'}), element([6, 12, 8], [10, 14, 12], {'all': 'i'}),
                                    element([7, 8, 4], [9, 10, 7], {'all': 'i'}), handle, element([7, 0, 13], [9, 16, 16], {'all': 'p'})],
                                   {'b': 'pump', 'i': 'iron', 'p': 'pipe'}, 'pump')
    m['outlet_0'] = model([element([5, 5, 10], [11, 11, 16], {'all': 'p', 'north': 'm'}, uv={'north': [0, 0, 16, 16]})], {'p': 'pipe', 'm': 'mouth_0'}, 'pipe')
    m['outlet_1'] = model([element([5, 5, 10], [11, 11, 16], {'all': 'p', 'north': 'm'}, uv={'north': [0, 0, 16, 16]}),
                           element([6, -16, 8], [10, 6, 10], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'})],
                          {'p': 'pipe', 'm': 'mouth_1', 'w': 'stream'}, 'pipe')
    for level in range(5):
        m[f'gauge_{level}'] = model([element([3, 2, 14], [13, 14, 16], {'all': 'b', 'north': 'g'}, uv={'north': [0, 0, 16, 16]})], {'b': 'board', 'g': f'gauge_{level}'}, 'board')
    m['pipe_0'] = model([element([0, 6, 13], [16, 10, 16], {'all': 'p'})], {'p': 'pipe'}, 'pipe')
    m['pipe_1'] = model([element([6, 0, 13], [10, 16, 16], {'all': 'p'})], {'p': 'pipe'}, 'pipe')
    m['pipe_2'] = model([element([4, 4, 12], [12, 12, 16], {'all': 'j'}), element([6, 0, 13], [10, 4, 16], {'all': 'p'}), element([6, 12, 13], [10, 16, 16], {'all': 'p'})],
                        {'j': 'joint', 'p': 'pipe'}, 'joint')
    for state in range(3):
        m[f'transfer_{state}'] = model([element([3, 3, 14], [13, 13, 16], {'all': 'b', 'north': 'd'}, uv={'north': [0, 0, 16, 16]}), element([7, 0, 15], [9, 3, 16], {'all': 'p'})],
                                       {'b': 'pipe', 'd': f'dial_{state}', 'p': 'pipe'}, 'pipe')
    return m


KINDS = ['platform', 'valve', 'pump', 'outlet', 'gauge', 'pipe', 'transfer']
STAGES = {'platform': 1, 'valve': 2, 'pump': 2, 'outlet': 2, 'gauge': 5, 'pipe': 3, 'transfer': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 5
NAMES = {'tank_fitting': 'Waterworks', 'tank_tiles': 'Quarry Tiles', 'tank_brick': 'Glazed Brick', 'tank_mark': 'Watermarked Brick',
         'tank_faint': 'Faintly Watermarked Brick', 'tank_plate': 'Galvanized Plate', 'tank_boards': 'Attic Boards'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/tank_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/tank_{name}'}}
        out[f'blockstates/tank_{name}.json'] = {'variants': {'': {'model': NS + f'block/tank_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/tank/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/tank/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/tank_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/tank/{tex.split("tank/")[1]}.png' in images, (key, tex)
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
    for key, name in {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the waterworks: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
