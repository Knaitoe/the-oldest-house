"""Johnny Truant's service halls (0.4.307): the chalk arrow, the steam main, the lift brakes and the lift's car, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise: chalk strokes (none, left, right) on a transparent plane; a cast-iron steam
main, still, shivering (a pale blur along it) and venting (a white plume); a brake lever with a red grip, off and hauled on; the car's
boards in an iron frame. Writes models, block states and the block's name. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, fill, dot, frame, line, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/truant_service/'
CHALK = '#ecebe4'


def arrow(direction):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3070 + direction)
    if direction == 0:
        for _ in range(5):
            dot(im, rnd.randrange(3, 13), rnd.randrange(12, 16), CHALK, 120)
        return im
    y = 8
    for x in range(2, 14):
        dot(im, x, y + (1 if rnd.random() < .2 else 0), CHALK)
    tip, back = (2, 1) if direction == 1 else (13, -1)
    for i in range(1, 5):
        dot(im, tip + back * i, y - i, CHALK); dot(im, tip + back * i, y + i, CHALK)
    if direction == 2:
        # The smudge of the one rubbed out.
        for x in range(3, 13):
            if rnd.random() < .5:
                dot(im, x, 11 + rnd.randrange(2), CHALK, 70)
    return im


def iron_pipe(stage):
    im = noisy(['#4a4a4a', '#525252', '#444444'], 3071)
    for x in range(16):
        dot(im, x, 0, '#2c2c2c'); dot(im, x, 15, '#2c2c2c'); dot(im, x, 4, '#6a6a6a')
    for x in (3, 11):
        for y in range(16):
            dot(im, x, y, '#363636')
    if stage == 1:
        rnd = random.Random(3072)
        for _ in range(26):
            dot(im, rnd.randrange(16), rnd.randrange(1, 15), '#8a8a8a', 160)
    return im


def plume():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3073)
    for y in range(16):
        for x in range(16):
            if rnd.random() < .55 - abs(x - 7.5) / 20:
                dot(im, x, y, shade('#f0f0f0', rnd.randint(-20, 0))[:3], 170)
    return im


def lever_iron():
    return noisy(['#5a5a5a', '#4e4e4e', '#666666'], 3074)


def grip():
    return noisy(['#9c2a22', '#8a221c', '#aa3428'], 3075)


def boards():
    im = noisy(['#8a6a44', '#7e603c', '#94744c'], 3076)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#5a432a')
    frame(im, 0, 0, 16, 16, '#3c3c3c')
    return im


def textures():
    t = {f'arrow_{d}': arrow(d) for d in range(3)}
    t.update({'pipe_0': iron_pipe(0), 'pipe_1': iron_pipe(1), 'plume': plume(), 'iron': lever_iron(), 'grip': grip(), 'boards': boards()})
    return {f'block/truant_service/{k}.png': v for k, v in t.items()}


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def lever(on):
    rot = {'origin': [8, 1, 13], 'axis': 'x', 'angle': -45 if on else 22.5}
    handle = element([7, 1, 12], [9, 14, 14], {'all': 'i'}); handle['rotation'] = rot
    knob = element([6.5, 12, 11.5], [9.5, 15, 14.5], {'all': 'g'}); knob['rotation'] = rot
    return [element([5, 0, 10], [11, 2, 16], {'all': 'i'}), handle, knob]


def fitting_models():
    m = {}
    for d in range(3):
        m[f'arrow_{d}'] = model([element([0, 0, 15.9], [16, 16, 16], {'north': 'c'}, uv={'north': [0, 0, 16, 16]})], {'c': f'arrow_{d}'}, f'arrow_{d}')
    for s in range(3):
        els = [element([0, 5, 9], [16, 11, 15], {'all': 'p'}), element([2, 4, 15], [4, 12, 16], {'all': 'p'}), element([12, 4, 15], [14, 12, 16], {'all': 'p'})]
        if s == 2:
            els.append(element([2, -16, 9], [14, 5, 15], {'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v'}))
        m[f'pipe_{s}'] = model(els, {'p': f'pipe_{min(s, 1)}', 'v': 'plume'}, 'pipe_0')
    for s in range(2):
        m[f'brake_{s}'] = model(lever(s == 1), {'i': 'iron', 'g': 'grip'}, 'iron')
        m[f'car_brake_{s}'] = model(lever(s == 1), {'i': 'iron', 'g': 'grip'}, 'iron')
    m['platform_0'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'b'}), element([0, 10, 0], [16, 13, 2], {'all': 'i'}), element([0, 10, 14], [16, 13, 16], {'all': 'i'})],
                            {'b': 'boards', 'i': 'iron'}, 'boards')
    return m


KINDS = ['arrow', 'pipe', 'brake', 'car_brake', 'platform']
STAGES = {'arrow': 3, 'pipe': 3, 'brake': 2, 'car_brake': 2, 'platform': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/truant_service/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(3):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/truant_service/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/truant_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    for key, obj in fitting_models().items():
        for tex in obj['textures'].values():
            assert f'block/truant_service/{tex.split("truant_service/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        assert im.size == (16, 16)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'block.the_oldest_house.truant_fitting'
    if check:
        assert lang.get(key) == 'Service Fittings', key
    else:
        lang[key] = 'Service Fittings'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' Truant\'s service fittings: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
