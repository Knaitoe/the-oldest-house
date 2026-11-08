"""Johnny Truant's service halls (0.4.307): the chalk arrow, the steam main, the lift brakes and the lift's car, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise: chalk strokes (none, left, right) on a transparent plane; a cast-iron steam
main, still, shivering (a pale blur along it) and venting (a white plume); a brake lever with a red grip, off and hauled on; the car's
boards in an iron frame; and (0.4.359) his copying room's sheaf of pages (written, run by the wet, blank and ruled), its blotter (clean, and
with a line taken off backwards), and the pigeonholes stuffed with papers. Writes models, block states and the block's name. --check
verifies everything.
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


INK = '#2a2a3a'


def paper(kind):
    """A sheet of his papers, ruled: written in a small even hand (0), the same with one line run by a drip (1), or blank (2)."""
    im = noisy(['#e8e2d0', '#e2dcc8', '#ece6d6'], 3590); rnd = random.Random(3591)
    for y in (3, 6, 9, 12):
        for x in range(1, 15):
            dot(im, x, y + 1, '#c9c4b4')
    if kind != 2:
        for y in (3, 6, 9, 12):
            x = 2
            while x < 14:
                word = rnd.randrange(1, 4)
                for i in range(word):
                    if x + i < 14:
                        dot(im, x + i, y, INK, 220 if rnd.random() < .8 else 150)
                x += word + 1
    if kind == 1:
        # The run: a drip landed on the third line and took its ink down the sheet in a streak.
        for x in range(3, 13):
            dot(im, x, 9, '#7a7a8c', 150)
        for y in range(9, 16):
            for x in (7, 8):
                dot(im, x, y, '#5a5a70', max(60, 190 - (y - 9) * 18))
    frame(im, 0, 0, 16, 16, '#bdb6a2')
    return im


def blotter(inked):
    im = noisy(['#3f5a3c', '#3a5437', '#466243'], 3592)
    for corner in ((0, 0), (12, 0), (0, 12), (12, 12)):
        for dx in range(4):
            for dy in range(4):
                if dx + dy < 4:
                    x = corner[0] + (dx if corner[0] == 0 else 3 - dx); y = corner[1] + (dy if corner[1] == 0 else 3 - dy)
                    dot(im, x, y, '#5a3a24')
    if inked:
        # A line of writing taken off backwards where the wet page lay on it.
        rnd = random.Random(3593); x = 13
        while x > 2:
            word = rnd.randrange(1, 4)
            for i in range(word):
                if x - i > 2:
                    dot(im, x - i, 8, '#1e2030', 200)
            x -= word + 1
        for x in (7, 8):
            dot(im, x, 9, '#1e2030', 120); dot(im, x, 10, '#1e2030', 80)
    return im


def pigeonholes():
    """The front of a run of pigeonholes in dark oak, each stuffed with folded paper."""
    im = noisy(['#3e2a18', '#382614', '#44301c'], 3594); rnd = random.Random(3595)
    for cx in (1, 6, 11):
        for cy in (1, 6, 11):
            for x in range(cx, cx + 4):
                for y in range(cy, cy + 4):
                    dot(im, x, y, '#1c140c')
            for x in range(cx, cx + 4):
                if rnd.random() < .85:
                    for y in range(cy + 1 + rnd.randrange(2), cy + 4):
                        dot(im, x, y, shade('#e2dcc8', rnd.randint(-30, 0))[:3])
    return im


def textures():
    t = {f'arrow_{d}': arrow(d) for d in range(3)}
    t.update({f'paper_{k}': paper(k) for k in range(3)})
    t.update({'blotter_0': blotter(False), 'blotter_1': blotter(True), 'pigeonholes': pigeonholes(), 'oak': noisy(['#3e2a18', '#382614', '#44301c'], 3596)})
    t.update({'pipe_0': iron_pipe(0), 'pipe_1': iron_pipe(1), 'plume': plume(), 'iron': lever_iron(), 'grip': grip(), 'boards': boards(), 'leaf': noisy(['#324228','#3d502e','#4a5c36'],3161)})
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
    for stage in range(2):
        m[f'hasp_{stage}'] = model([element([4, 5, 14], [12, 8, 16], {'all':'i'}),element([7, 1 if stage else 8, 12], [9, 8 if stage else 15, 14], {'all':'i'})], {'i':'iron'}, 'iron')
        m[f'wedge_{stage}'] = model([] if stage == 0 else [element([2,0,2],[13,1,14],{'all':'b'}),element([4,1,6],[12,3,14],{'all':'b'})], {'b':'boards'}, 'boards')
        m[f'hatch_{stage}'] = model([element([0,0,13],[16,16,16],{'all':'b'})] if stage == 0 else [element([0,13,0],[16,16,3],{'all':'b'})], {'b':'boards'}, 'boards')
        m[f'stock_chain_{stage}'] = model([element([2,7,14],[14,9,16],{'all':'i'})] if stage == 0 else [element([6,0,14],[8,13,16],{'all':'i'})], {'i':'iron'}, 'iron')
    m['split_0'] = model([element([0,0,13],[7,16,16],{'all':'b'}),element([9,0,13],[16,16,16],{'all':'b'})], {'b':'boards'}, 'boards')
    for stage in range(3):
        e=element([0,0,7],[16,16,9],{'all':'l'})
        if stage:e['rotation']={'origin':[8,0,8],'axis':'z','angle':-22.5 if stage==2 else 22.5,'rescale':False}
        m[f'bough_{stage}']=model([e],{'l':'leaf'},'leaf')
    # 0.4.359, the copying room: a sheaf of pages lying on the desk, the blotter, and a run of pigeonholes (a whole block).
    for k in range(3):
        under = element([3, 0, 3.5], [14, .25, 14], {'up': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})
        top = element([2, .25, 2.5], [13, .5, 13], {'up': 'w', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}, uv={'up': [0, 0, 16, 16]})
        m[f'page_{k}'] = model([under, top], {'b': 'paper_2', 'w': f'paper_{k}'}, f'paper_{k}')
    for k in range(2):
        m[f'blotter_{k}'] = model([element([1, 0, 2], [15, .5, 14], {'up': 'f', 'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'down': 'o'})], {'f': f'blotter_{k}', 'o': 'oak'}, f'blotter_{k}')
        m[f'blotter_{k}']['elements'][0]['faces']['up']['uv'] = [0, 0, 16, 16]
    m['pigeonholes_0'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'p', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'o', 'down': 'o'})], {'p': 'pigeonholes', 'o': 'oak'}, 'oak')
    return m


KINDS = ['arrow', 'pipe', 'brake', 'car_brake', 'platform', 'hasp', 'wedge', 'hatch', 'split', 'stock_chain', 'bough', 'page', 'blotter', 'pigeonholes']
STAGES = {'arrow': 3, 'pipe': 3, 'brake': 2, 'car_brake': 2, 'platform': 1, 'hasp': 2, 'wedge': 2, 'hatch': 2, 'split': 1, 'stock_chain': 2, 'bough': 3, 'page': 3, 'blotter': 2, 'pigeonholes': 1}
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
