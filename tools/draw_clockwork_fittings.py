"""The Clockwork Attic (0.4.362): master clocks, wall gears, end levers, the weight, its catch and the bars, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise and fixed seeds:
- a brass gear of eight teeth in four positions a quarter of a tooth apart, so stepping through them turns it;
- an iron pin to push through its teeth;
- a clock case's glazed front with the pendulum hanging still, swung left and swung right, and its sides and top;
- the clock face with its hands in four places;
- iron, the lever's grip and the bars.

Writes the clockwork_fitting block's models and states and its name. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

from draw_tank_fittings import shade, dot, fill, frame, element, line
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/clockwork/'

BRASS = ['#b48c3c', '#a8823a', '#c09848']
WOOD = ['#5c3a22', '#56361f', '#623e25']
IRON = ['#4a4a4e', '#444448', '#525256']


def clear():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def gear(stage):
    """A gear centred in the block, eight teeth, turned by a quarter tooth per stage; hub, spokes and a dark rim line."""
    im = clear()
    rnd = random.Random(3700)
    phase = stage * (2 * math.pi / 8) / 4
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx) - phase
            tooth = (math.cos(a * 8) > 0.15)
            outer = 7.6 if tooth else 6.0
            if r <= outer:
                c = rnd.choice(BRASS)
                if 2.2 < r < 4.6:
                    # Four spokes between the hub and the rim; the rest open.
                    sa = (a % (math.pi / 2))
                    if not (sa < 0.32 or sa > math.pi / 2 - 0.32):
                        continue
                if r > outer - 0.9 or 4.6 <= r < 5.4:
                    c = shade(c, -34)
                if r <= 1.6:
                    c = '#3a3a3e'
                im.putpixel((x, y), shade(c, 0))
    return im


def pin():
    im = clear()
    for y in range(0, 5):
        dot(im, 7, y, '#6a6a70'); dot(im, 8, y, '#8a8a90')
    dot(im, 6, 0, '#5a5a60'); dot(im, 9, 0, '#5a5a60')
    return im


def case_front(swing):
    im = noisy(WOOD, 3701)
    frame(im, 0, 0, 16, 16, '#3a2414')
    fill(im, 4, 2, 12, 14, '#1c140e')
    frame(im, 3, 1, 13, 15, '#8a6a3a')
    # The rod from the top of the window, and the bob at its foot, hanging still or swung.
    off = {0: 0, 1: -2, 2: 2}[swing]
    line(im, 8, 2, 8 + off, 10, '#a08040')
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            dot(im, 8 + off + dx, 11 + dy, '#c8a050' if dx or dy else '#e0c070')
    return im


def case_side():
    im = noisy(WOOD, 3702)
    frame(im, 0, 0, 16, 16, '#3a2414')
    frame(im, 2, 2, 14, 14, '#4a2e1a')
    return im


def case_top():
    im = noisy(WOOD, 3703)
    frame(im, 0, 0, 16, 16, '#3a2414')
    return im


def face(stage):
    im = noisy(WOOD, 3704)
    frame(im, 0, 0, 16, 16, '#3a2414')
    for y in range(16):
        for x in range(16):
            if math.hypot(x - 7.5, y - 7.5) <= 6.2:
                im.putpixel((x, y), shade('#e8e0c8', random.Random(x * 31 + y).randint(-6, 4)))
    for k in range(12):
        a = k * math.pi / 6
        dot(im, round(7.5 + 5.4 * math.cos(a)), round(7.5 + 5.4 * math.sin(a)), '#2a2420')
    # The minute hand goes round once over the four positions, the hour hand a quarter as far.
    m = -math.pi / 2 + stage * math.pi / 2
    h = -math.pi / 2 + stage * math.pi / 8 + 0.4
    line(im, 8, 8, round(7.5 + 4.6 * math.cos(m)), round(7.5 + 4.6 * math.sin(m)), '#1a1612')
    line(im, 8, 8, round(7.5 + 3.0 * math.cos(h)), round(7.5 + 3.0 * math.sin(h)), '#3a2a20')
    dot(im, 8, 8, '#8a6a3a')
    return im


def iron():
    return noisy(IRON, 3705)


def grip():
    im = noisy(['#6a4426', '#62401f', '#704a2a'], 3706)
    for y in (3, 8, 13):
        for x in range(16):
            dot(im, x, y, '#4a2e18')
    return im


def bars():
    im = clear()
    for x in (1, 5, 10, 14):
        for y in range(16):
            dot(im, x, y, '#3e3e44'); dot(im, x + 1, y, '#5a5a60')
    for x in range(16):
        dot(im, x, 1, '#4a4a50'); dot(im, x, 14, '#4a4a50')
    return im


def textures():
    t = {f'gear_{s}': gear(s) for s in range(4)}
    t.update({f'case_front_{s}': case_front(s) for s in range(3)})
    t.update({f'face_{s}': face(s) for s in range(4)})
    t.update({'pin': pin(), 'case_side': case_side(), 'case_top': case_top(), 'iron': iron(), 'grip': grip(), 'bars': bars()})
    return {f'block/clockwork/{k}.png': v for k, v in t.items()}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': True, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    case = {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'up': 't', 'down': 't'}
    for s in range(3):
        m[f'case_{s}'] = model([element([2, 0, 6], [14, 16, 16], case)], {'f': f'case_front_{s}', 's': 'case_side', 't': 'case_top'}, 'case_side')
    for s in range(4):
        m[f'face_{s}'] = model([element([2, 0, 6], [14, 16, 16], case), element([1, 14, 5], [15, 16, 16], {'all': 's'})],
                               {'f': f'face_{s}', 's': 'case_side', 't': 'case_top'}, 'case_side')
        plate = element([0, 0, 13], [16, 16, 14], {'north': 'g', 'south': 'g'})
        axle = element([7, 7, 14], [9, 9, 16], {'all': 'i'})
        m[f'gear_{s}'] = model([plate, axle], {'g': f'gear_{s}', 'i': 'iron'}, 'iron')
        m[f'pinned_{s}'] = model([plate, axle, element([0, 0, 12.5], [16, 16, 12.5], {'north': 'p', 'south': 'p'})], {'g': f'gear_{s}', 'i': 'iron', 'p': 'pin'}, 'iron')
    base = element([4, 0, 13], [12, 6, 16], {'all': 'i'})
    m['lever_0'] = model([base, element([7, 5, 12], [9, 15, 14], {'all': 'i'}), element([6, 14, 11], [10, 16, 15], {'all': 'w'})], {'i': 'iron', 'w': 'grip'}, 'iron')
    m['lever_1'] = model([base, element([7, 0, 10], [9, 6, 12], {'all': 'i'}), element([6, 0, 8], [10, 2, 12], {'all': 'w'})], {'i': 'iron', 'w': 'grip'}, 'iron')
    m['weight_0'] = model([element([4, 0, 4], [12, 8, 12], {'all': 'i'}), element([7, 8, 7], [9, 10, 9], {'all': 'i'}), element([7.5, 10, 7.5], [8.5, 16, 8.5], {'all': 'i'})], {'i': 'iron'}, 'iron')
    m['catch_0'] = model([element([6, 6, 12], [10, 10, 16], {'all': 'i'}), element([7, 2, 13], [9, 10, 15], {'all': 'i'})], {'i': 'iron'}, 'iron')
    m['catch_1'] = model([element([6, 6, 12], [10, 10, 16], {'all': 'i'}), element([0, 7, 7], [16, 9, 9], {'all': 'i'})], {'i': 'iron'}, 'iron')
    m['gate_0'] = model([element([0, 0, 7], [16, 16, 9], {'north': 'b', 'south': 'b'})], {'b': 'bars'}, 'iron')
    m['gate_1'] = model([element([0, 12, 7], [16, 16, 9], {'north': 'b', 'south': 'b'}, uv={'north': [0, 0, 16, 4], 'south': [0, 0, 16, 4]})], {'b': 'bars'}, 'iron')
    return m


KINDS = ['case', 'face', 'gear', 'pinned', 'lever', 'weight', 'catch', 'gate']
STAGES = {'case': 3, 'face': 4, 'gear': 4, 'pinned': 4, 'lever': 2, 'weight': 1, 'catch': 2, 'gate': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/clockwork/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/clockwork/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/clockwork_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/clockwork/{tex.split("clockwork/")[1]}.png' in images, (key, tex)
    # The four gear positions really differ, so stepping through them shows the gear turning.
    frames = [images[f'block/clockwork/gear_{s}.png'].tobytes() for s in range(4)]
    assert len(set(frames)) == 4
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'block.the_oldest_house.clockwork_fitting'
    if check:
        assert lang.get(key) == 'Clockwork', key
    else:
        lang[key] = 'Clockwork'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the clockwork: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
