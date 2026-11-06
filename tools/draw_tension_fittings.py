"""The Tension Room (0.4.306): a utility loft's cable gear, and the loft.

Draws, in native Minecraft pixels from fixed seeds: three materials (limewashed plaster, painted loft boards, the plaster with a red
painted mark where the counterweight should stand) and every part of the tension_fitting block, its models facing north (front to the
room, back to the wall) and turned by its facing: a hand-wheel at three turns of its spokes on its drum; the cleat, cast off and made
fast with the cable's turns round it; the counterweight on its shackle; the locking pin, seated, half drawn and drawn free; the cable
(a run along the ceiling, a rise down a wall); the sheave at the head of the shaft; and the sprung platform. Writes the models, the
block states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

from draw_tank_fittings import rgb, shade, new, fill, dot, frame, line, element

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/tension/'


# ------------------------------------------------------------------------------------------------ materials
def plaster(seed=3060):
    im = new(base='#d8d2c0', seed=seed, spread=4); rnd = random.Random(seed + 1)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#bdb6a2')
    return im


def boards():
    # Loft boards painted a tired green-grey, worn to the wood along the middle of each.
    base = '#6e7a68'
    im = new(base=base, seed=3062, spread=4); rnd = random.Random(3063)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -30)[:3])
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.choice((1, 5, 9, 13))
        dot(im, x, y, '#8a7456')
    return im


def mark():
    # The plaster with a hand-painted red band and an arrow pointing at it from below.
    im = plaster(3064)
    fill(im, 0, 6, 16, 9, '#a8322a')
    line(im, 7, 15, 7, 10, '#a8322a'); line(im, 8, 15, 8, 10, '#a8322a'); line(im, 5, 12, 7, 10, '#a8322a'); line(im, 10, 12, 8, 10, '#a8322a')
    return im


def materials():
    return {'plaster': plaster(), 'boards': boards(), 'mark': mark()}


# ------------------------------------------------------------------------------------------------ fittings
def iron(seed=3070):
    return new(base='#3c3e40', seed=seed, spread=3)


def rope():
    # Hemp cable: a twist of light and dark strands running diagonally.
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            dot(im, x, y, '#b09a6a' if (x + y) % 4 < 2 else '#7e6a44')
    return im


def wheel(turn):
    # The hand-wheel from the front: a rim, a hub and four spokes, turned a sixth further at each stage, a handle knob on the rim.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rim = '#5a3a22'
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6 <= d <= 7.6:
                dot(im, x, y, rim)
    for k in range(4):
        a = math.radians(turn * 30 + k * 90)
        line(im, round(7.5 + 1.5 * math.cos(a)), round(7.5 + 1.5 * math.sin(a)), round(7.5 + 6.5 * math.cos(a)), round(7.5 + 6.5 * math.sin(a)), rim)
    a = math.radians(turn * 30 + 45)
    kx, ky = round(7.5 + 6.6 * math.cos(a)), round(7.5 + 6.6 * math.sin(a))
    fill(im, kx - 1, ky - 1, kx + 1, ky + 1, '#c8a060')
    fill(im, 6, 6, 10, 10, '#2c2e30'); dot(im, 7, 7, '#8a8c8e')
    return im


def cleat(fast):
    # A horn cleat on its plate, seen from the front; made fast, figure-of-eight turns of rope across it.
    im = new(base='#8a8478', seed=3072, spread=2)
    frame(im, 0, 0, 16, 16, '#4a463e')
    fill(im, 2, 6, 14, 10, '#3c3e40'); fill(im, 6, 4, 10, 12, '#3c3e40')
    if fast:
        line(im, 2, 4, 13, 11, '#b09a6a'); line(im, 2, 11, 13, 4, '#b09a6a'); line(im, 3, 4, 14, 11, '#7e6a44'); line(im, 3, 11, 14, 4, '#7e6a44')
    return im


def weight():
    im = new(base='#2e3032', seed=3073, spread=4)
    frame(im, 0, 0, 16, 16, '#1c1d1e')
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        dot(im, x, y, '#6a6c6e')
    fill(im, 5, 6, 11, 10, '#46484a')
    return im


def brass():
    return new(base='#b08a3a', seed=3074, spread=6)


def slats():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3075)
    for x0 in (0, 4, 8, 12):
        for x in range(x0, x0 + 4):
            for y in range(16):
                dot(im, x, y, shade('#8e7450', rnd.randint(-6, 6))[:3])
        for y in range(16):
            dot(im, x0 + 3, y, '#5e4a32')
    return im


def spring():
    # The platform's side: its frame on top, the coils of the springs under it.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 3, '#6e5a3e')
    for x0 in (2, 11):
        for y in range(3, 16, 2):
            fill(im, x0, y, x0 + 3, y + 1, '#7a7e82')
    return im


def textures():
    out = {f'block/tension_{k}.png': v for k, v in materials().items()}
    t = {'iron': iron(), 'rope': rope(), 'weight': weight(), 'brass': brass(), 'slats': slats(), 'spring': spring(),
         'cleat_0': cleat(False), 'cleat_1': cleat(True)}
    for turn in range(3):
        t[f'wheel_{turn}'] = wheel(turn)
    out.update({f'block/tension/{k}.png': v for k, v in t.items()})
    return out


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for turn in range(3):
        m[f'wheel_{turn}'] = model([element([5, 5, 12], [11, 11, 16], {'all': 'r'}), element([7, 7, 11], [9, 9, 12], {'all': 'i'}),
                                    element([1, 1, 10.5], [15, 15, 11], {'north': 'w', 'south': 'w'}, uv={'north': [0, 0, 16, 16], 'south': [16, 0, 0, 16]})],
                                   {'r': 'rope', 'i': 'iron', 'w': f'wheel_{turn}'}, 'iron')
    for stage in range(2):
        m[f'cleat_{stage}'] = model([element([3, 4, 14], [13, 12, 16], {'all': 'i', 'north': 'c'}, uv={'north': [0, 0, 16, 16]})],
                                    {'i': 'iron', 'c': f'cleat_{stage}'}, 'iron')
    m['weight_0'] = model([element([3, 0, 3], [13, 12, 13], {'all': 'w'}), element([7, 12, 7], [9, 16, 9], {'all': 'i'})], {'w': 'weight', 'i': 'iron'}, 'weight')
    m['release_bar_0'] = model([element([1, 5, 14], [15, 11, 16], {'all': 'i'}),
                                element([2, 7, 11], [14, 9, 14], {'all': 'b'})], {'i': 'iron', 'b': 'brass'}, 'iron')
    for stage in range(3):
        out = stage * 5  # seated, the pin reaches out across into the platform frame; drawn, back in its housing
        m[f'pin_{stage}'] = model([element([4, 4, 15], [12, 12, 16], {'all': 'i'}), element([6, 6, out], [10, 10, 15], {'all': 'b'})], {'i': 'iron', 'b': 'brass'}, 'brass')
    m['cable_0'] = model([element([0, 7, 7], [16, 9, 9], {'all': 'r'})], {'r': 'rope'}, 'rope')
    m['cable_1'] = model([element([7, 0, 7], [9, 16, 9], {'all': 'r'})], {'r': 'rope'}, 'rope')
    m['sheave_0'] = model([element([7, 0, 4], [9, 10, 12], {'all': 'i'}), element([4, 10, 7], [12, 16, 9], {'all': 'i'}), element([7.5, 0, 0], [8.5, 1, 16], {'all': 'r'})],
                          {'i': 'iron', 'r': 'rope'}, 'iron')
    m['platform_0'] = model([element([0, 13, 0], [16, 16, 16], {'up': 's', 'down': 's', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f'},
                                     uv={f: [0, 0, 16, 3] for f in ('north', 'south', 'east', 'west')}),
                             element([0, 8, 0], [16, 13, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f'}, uv={f: [0, 3, 16, 8] for f in ('north', 'south', 'east', 'west')})],
                            {'s': 'slats', 'f': 'spring'}, 'slats')
    return m


KINDS = ['wheel', 'cleat', 'weight', 'pin', 'cable', 'sheave', 'platform', 'release_bar']
STAGES = {'wheel': 3, 'cleat': 2, 'weight': 1, 'pin': 3, 'cable': 2, 'sheave': 1, 'platform': 1, 'release_bar': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 5
NAMES = {'tension_fitting': 'Cable Gear', 'tension_plaster': 'Limewashed Plaster', 'tension_boards': 'Painted Loft Boards', 'tension_mark': 'Marked Plaster'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/tension_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/tension_{name}'}}
        out[f'blockstates/tension_{name}.json'] = {'variants': {'': {'model': NS + f'block/tension_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/tension/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/tension/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/tension_fitting.json'] = {'variants': variants}
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
            assert f'block/tension/{tex.split("tension/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the cable gear: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
