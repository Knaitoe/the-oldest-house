"""The Window Cleaner (0.4.251): the plant room's emergency winch, the cleaner's cradle in the light well, and the corridor's casement.

Draws, in native Minecraft pixels from fixed seeds, every fitting of the winch_fitting block, its models facing north and turned by its
facing: the winch, a cable drum between two cast cheeks with its crank a quarter turn further round at each of four stages; the brake,
a red-knobbed lever on an iron quadrant, on (upright) or off (thrown forward); the cradle's slatted floor, at the top of its block; its guard rail; the steel
cable it hangs from; the casement onto the well, shut or swung open. Writes the models and block states, and the block's name.
--check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/winch/'


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


IRON = '#3e4446'
IRON_HI = '#5c6466'
GREEN = '#3e5a46'
RED = '#a42a22'
STEEL = '#9aa2a4'
GALV = '#8e9692'
OAK = '#8e6234'


def cheek():
    """A cast iron cheek, painted machine green, its bolt heads and the bearing boss."""
    im = new(base=GREEN, seed=2700, spread=3)
    frame(im, 0, 0, 16, 16, shade(GREEN, -18))
    fill(im, 6, 6, 10, 10, IRON); frame(im, 6, 6, 10, 10, IRON_HI)
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(im, x, y, IRON_HI)
    return im


def drum():
    """The drum with its cable wound on, coil by coil, and a flange edge at each end."""
    im = new(base=STEEL, seed=2710, spread=3)
    for y in range(16):
        k = 10 if y % 2 == 0 else -14
        for x in range(1, 15):
            dot(im, x, y, shade(STEEL, k + (x * 3 + y) % 5 - 2))
    fill(im, 0, 0, 1, 16, shade(GREEN, -10)); fill(im, 15, 0, 16, 16, shade(GREEN, -10))
    return im


def iron():
    return new(base=IRON, seed=2720, spread=3)


def handle():
    im = new(base=OAK, seed=2722, spread=4)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, shade(OAK, -16))
    return im


def red():
    im = new(base=RED, seed=2724, spread=5)
    fill(im, 5, 2, 9, 5, shade(RED, 30))
    return im


def quadrant():
    """The brake's quadrant plate, its two notches, ON and OFF stamped in."""
    im = new(base=IRON, seed=2730, spread=3)
    frame(im, 0, 0, 16, 16, shade(IRON, -16))
    fill(im, 3, 2, 5, 4, '#1e2222'); fill(im, 11, 2, 13, 4, '#1e2222')
    for x in (3, 4, 11, 12):
        dot(im, x, 6, IRON_HI)
    return im


def slats():
    """The cradle's floor: aluminium slats, gaps between, a bolt at each end of each."""
    im = new(base='#2a2c2c', seed=2740, spread=2)
    for x0 in range(0, 16, 4):
        fill(im, x0, 0, x0 + 3, 16, GALV)
        fill(im, x0, 0, x0 + 1, 16, shade(GALV, 14))
        dot(im, x0 + 1, 1, IRON); dot(im, x0 + 1, 14, IRON)
    return im


def rail():
    im = new(base=GALV, seed=2750, spread=4)
    for y in range(0, 16, 5):
        for x in range(16):
            dot(im, x, y, shade(GALV, -16))
    return im


def cable():
    """Twisted steel rope: a diagonal lay, light and dark."""
    im = new(base=STEEL, seed=2760, spread=2)
    for y in range(16):
        for x in range(16):
            if (x + y) % 3 == 0:
                dot(im, x, y, shade(STEEL, -26))
            elif (x + y) % 3 == 1:
                dot(im, x, y, shade(STEEL, 16))
    return im


def sash():
    """A casement's frame, painted over and over in cream, its glass dark with the well behind it, and its stiff catch."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    cream = '#d6cdb4'
    fill(im, 0, 0, 16, 16, cream)
    fill(im, 2, 2, 14, 14, '#2a3238', 150)
    for i in range(3, 9):
        dot(im, i, 12 - i, '#6a7a84', 170)
    frame(im, 0, 0, 16, 16, shade(cream, -26))
    frame(im, 2, 2, 14, 14, shade(cream, -18))
    fill(im, 13, 7, 15, 9, '#8a7448')
    return im


def paint():
    im = new(base='#d6cdb4', seed=2770, spread=3)
    for y in range(0, 16, 3):
        dot(im, (y * 5) % 16, y, '#c4ba9e')
    return im


def textures():
    t = {'cheek': cheek(), 'drum': drum(), 'iron': iron(), 'handle': handle(), 'red': red(), 'quadrant': quadrant(),
         'slats': slats(), 'rail': rail(), 'cable': cable(), 'sash': sash(), 'paint': paint()}
    return {f'block/winch/{k}.png': v for k, v in t.items()}


def element(lo, hi, textures, rotation=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


# The crank's arm and knob at each quarter turn, round the axle at y 8, z 7, on the drum's east cheek.
CRANK = [
    ([15, 7, 6], [16, 14, 8], [16, 12, 6], [18, 14, 8]),
    ([15, 7, 1], [16, 9, 8], [16, 7, 1], [18, 9, 3]),
    ([15, 2, 6], [16, 9, 8], [16, 2, 6], [18, 4, 8]),
    ([15, 7, 6], [16, 9, 13], [16, 7, 11], [18, 9, 13]),
]


def drum_model(stage):
    arm_lo, arm_hi, knob_lo, knob_hi = CRANK[stage]
    els = [element([1, 1, 2], [3, 15, 12], {'all': 'c'}), element([13, 1, 2], [15, 15, 12], {'all': 'c'}),
           element([3, 4, 3], [13, 12, 11], {'all': 'd', 'east': 'i', 'west': 'i'}),
           element([0, 7, 6], [16, 9, 8], {'all': 'i'}),
           element([1, 0, 1], [15, 1, 13], {'all': 'i'}),
           element(arm_lo, arm_hi, {'all': 'i'}), element(knob_lo, knob_hi, {'all': 'h'})]
    return model(els, {'c': 'cheek', 'd': 'drum', 'i': 'iron', 'h': 'handle'}, 'cheek')


def brake_model(off):
    lever = [element([7, 4, 2], [9, 13, 4], {'all': 'i'}), element([6, 12, 1.5], [10, 15, 4.5], {'all': 'r'})]
    if off:
        rot = {'origin': [8, 4, 3], 'axis': 'x', 'angle': -45}
        lever = [dict(e, rotation=rot) for e in lever]
    els = [element([5, 0, 0], [11, 5, 6], {'all': 'i', 'north': 'q', 'south': 'q'})] + lever
    return model(els, {'i': 'iron', 'q': 'quadrant', 'r': 'red'}, 'quadrant')


def fitting_models():
    m = {}
    for stage in range(4):
        m[f'drum_{stage}'] = drum_model(stage)
    m['brake_0'] = brake_model(False)
    m['brake_1'] = brake_model(True)
    m['cradle_0'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'r', 'up': 's', 'down': 's'})], {'s': 'slats', 'r': 'rail'}, 'slats')
    m['rail_0'] = model([element([0, 0, 13], [2, 15, 15], {'all': 'r'}), element([14, 0, 13], [16, 15, 15], {'all': 'r'}),
                         element([0, 13, 13], [16, 15, 15], {'all': 'r'}), element([0, 7, 13.5], [16, 8.5, 14.5], {'all': 'r'})],
                        {'r': 'rail'}, 'rail')
    m['cable_0'] = model([element([7.5, 0, 7.5], [8.5, 16, 8.5], {'all': 'k'})], {'k': 'cable'}, 'cable')
    m['casement_0'] = model([element([0, 0, 0], [16, 16, 2], {'all': 'p', 'north': 'g', 'south': 'g'})], {'p': 'paint', 'g': 'sash'}, 'sash', 'translucent')
    m['casement_1'] = model([element([0, 0, 0], [2, 16, 16], {'all': 'p', 'east': 'g', 'west': 'g'})], {'p': 'paint', 'g': 'sash'}, 'sash', 'translucent')
    return m


KINDS = ['drum', 'brake', 'cradle', 'rail', 'cable', 'casement']
STAGES = {'drum': 4, 'brake': 2, 'casement': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'winch_fitting': 'Winch Fitting'}


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/winch/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/winch/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/winch_fitting.json'] = {'variants': variants}
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
            assert f'block/winch/{tex.split("winch/")[1]}.png' in images, (key, tex)
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
    print(('Verified' if check else 'Drew') + f' the window cleaner\'s winch: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
