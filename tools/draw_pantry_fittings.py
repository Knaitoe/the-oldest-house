"""The Cold Pantry (0.4.363): the ice-box, the zinc tub, the shutters, frosted pipes, the thermometer, the cold store's door and the cake.

16x16 textures in vanilla's manner, from small palettes with per-pixel noise and fixed seeds:
- the ice-box's front with its lid, and inside, nothing to three blocks of ice;
- the zinc tub holding nothing to three;
- a ventilation shutter shut, and open on daylight;
- a pipe bare and under three thicknesses of frost;
- a wall thermometer at four heights;
- the cold store's heavy door, shut and standing open;
- a wedding cake under a glass bell, perfect, slumped and sweating, and sunk and furred.

Writes the pantry_fitting block's models and states and its name. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import shade, dot, fill, frame, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/pantry/'

OAK = ['#8a6a44', '#846440', '#906e48']
ZINC = ['#9aa0a4', '#94999e', '#a2a8ac']
ICE = ['#b8d4e8', '#c4dcee', '#acc8de']
FROST = '#eef4f8'


def clear():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def icebox_front():
    im = noisy(OAK, 3800)
    frame(im, 0, 0, 16, 16, '#4a3420')
    frame(im, 2, 2, 14, 14, '#6a4c2c')
    for x in (12, 13):
        dot(im, x, 7, '#c0c4c8'); dot(im, x, 8, '#8a8e92')
    return im


def icebox_top(ice):
    """Looking down into the open box: its zinc lining, and as many blocks of ice as it holds."""
    im = noisy(ZINC, 3801)
    frame(im, 0, 0, 16, 16, '#4a3420')
    frame(im, 1, 1, 15, 15, '#6a6e72')
    spots = [(2, 2), (8, 2), (2, 8)]
    rnd = random.Random(3802)
    for i in range(ice):
        x0, y0 = spots[i]
        for y in range(y0, y0 + 6):
            for x in range(x0, x0 + 6):
                dot(im, x, y, rnd.choice(ICE))
        frame(im, x0, y0, x0 + 6, y0 + 6, '#8eaec4')
        dot(im, x0 + 1, y0 + 1, FROST)
    return im


def tub(ice):
    im = noisy(ZINC, 3803)
    frame(im, 0, 0, 16, 16, '#6a6e72')
    rnd = random.Random(3804 + ice)
    if ice == 0:
        for _ in range(10):
            dot(im, rnd.randrange(2, 14), rnd.randrange(2, 14), '#8a9aa6', 170)
    for i in range(ice):
        x0, y0 = [(2, 3), (8, 4), (5, 9)][i]
        for y in range(y0, y0 + 5):
            for x in range(x0, x0 + 5):
                dot(im, x, y, rnd.choice(ICE))
        dot(im, x0 + 1, y0 + 1, FROST)
    return im


def shutter(opened):
    im = noisy(['#5c4a36', '#56442f', '#62503a'], 3805)
    frame(im, 0, 0, 16, 16, '#3a2c1c')
    if opened:
        fill(im, 2, 2, 14, 14, '#e8f0f4')
        for x in range(2, 14):
            dot(im, x, 2, '#ffffff')
        for y in range(3, 14, 3):
            for x in range(2, 14):
                dot(im, x, y, '#c8d4dc')
    else:
        for y in range(2, 14, 2):
            for x in range(2, 14):
                dot(im, x, y, '#3e3022')
    return im


def pipe(frost):
    im = noisy(['#7a7e82', '#74787c', '#80848a'], 3806)
    for x in range(16):
        dot(im, x, 0, '#55595d'); dot(im, x, 15, '#55595d')
    rnd = random.Random(3807 + frost)
    for _ in range(frost * 22):
        dot(im, rnd.randrange(16), rnd.randrange(16), FROST, 160 + 30 * frost)
    if frost == 3:
        for x in range(0, 16, 3):
            dot(im, x, 15, FROST)
    return im


def thermo(level):
    im = noisy(OAK, 3808)
    frame(im, 0, 0, 16, 16, '#4a3420')
    fill(im, 6, 2, 10, 14, '#ece6d4')
    for y in range(3, 13):
        dot(im, 7, y, '#c8c2b0'); dot(im, 8, y, '#c8c2b0')
    top = 12 - level * 3
    for y in range(top, 13):
        dot(im, 7, y, '#b02020'); dot(im, 8, y, '#c83030')
    for y in (4, 7, 10):
        dot(im, 10, y, '#4a3420')
    return im


def store_door():
    im = noisy(['#6e6a64', '#68645e', '#74706a'], 3809)
    frame(im, 0, 0, 16, 16, '#4a4640')
    for y in (3, 12):
        for x in range(1, 15):
            dot(im, x, y, '#8a8680')
    dot(im, 12, 8, '#c0b8a8'); dot(im, 13, 8, '#a09888')
    return im


def cake_tex(stage):
    im = clear()
    icing = ['#f4f0e4', '#eee8da', '#f8f4ea']
    rnd = random.Random(3810 + stage)
    tiers = [(2, 10, 14, 15), (4, 6, 12, 10), (6, 2, 10, 6)]
    for t, (x0, y0, x1, y1) in enumerate(tiers):
        sag = stage if t < 2 else stage * 2
        for y in range(y0 + (sag if t == 2 else 0), y1):
            for x in range(x0, x1):
                c = rnd.choice(icing)
                if stage == 2 and t == 0 and rnd.random() < .45:
                    c = '#8a8e80'
                dot(im, x, y, c)
        for x in range(x0, x1, 2):
            dot(im, x, y1 - 1, '#e0d6c0')
    if stage >= 1:
        for _ in range(6):
            dot(im, rnd.randrange(1, 15), rnd.randrange(0, 15), '#d8e8f0', 140)
    return im


def glass():
    im = clear()
    for y in range(16):
        dot(im, 1, y, '#e8f4f8', 90); dot(im, 14, y, '#e8f4f8', 90)
    for x in range(1, 15):
        dot(im, x, 0, '#e8f4f8', 90)
    dot(im, 3, 3, '#ffffff', 160)
    return im


def marble():
    return noisy(['#e4e2dc', '#dcdad4', '#eceae4'], 3811)


def textures():
    t = {'icebox_front': icebox_front(), 'store_door': store_door(), 'glass': glass(), 'marble': marble(), 'oak': noisy(OAK, 3812), 'zinc': noisy(ZINC, 3813)}
    t.update({f'icebox_top_{n}': icebox_top(n) for n in range(4)})
    t.update({f'tub_{n}': tub(n) for n in range(4)})
    t.update({f'shutter_{n}': shutter(n == 1) for n in range(2)})
    t.update({f'pipe_{n}': pipe(n) for n in range(4)})
    t.update({f'thermo_{n}': thermo(n) for n in range(4)})
    t.update({f'cake_{n}': cake_tex(n) for n in range(3)})
    return {f'block/pantry/{k}.png': v for k, v in t.items()}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': True, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for n in range(4):
        m[f'icebox_{n}'] = model([element([1, 0, 1], [15, 13, 15], {'north': 'f', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 't', 'down': 'o'})],
                                 {'f': 'icebox_front', 'o': 'oak', 't': f'icebox_top_{n}'}, 'oak')
        m[f'tub_{n}'] = model([element([2, 0, 2], [14, 7, 14], {'north': 'z', 'south': 'z', 'east': 'z', 'west': 'z', 'down': 'z', 'up': 't'})],
                              {'z': 'zinc', 't': f'tub_{n}'}, 'zinc')
        m[f'pipe_{n}'] = model([element([0, 11, 6], [16, 15, 10], {'all': 'p'})], {'p': f'pipe_{n}'}, f'pipe_{n}')
        m[f'thermo_{n}'] = model([element([5, 1, 15], [11, 15, 16], {'north': 't', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'o', 'down': 'o'})],
                                 {'t': f'thermo_{n}', 'o': 'oak'}, 'oak')
    for n in range(2):
        m[f'shutter_{n}'] = model([element([0, 0, 14], [16, 16, 16], {'north': 's', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'o', 'down': 'o'})],
                                  {'s': f'shutter_{n}', 'o': 'oak'}, 'oak')
    # The cold store's door: shut, a heavy slab in its frame; open, swung back against the jamb.
    for kind in ('store_door', 'store_door_top'):
        m[f'{kind}_0'] = model([element([0, 0, 13], [16, 16, 16], {'all': 'd'})], {'d': 'store_door'}, 'store_door')
        m[f'{kind}_1'] = model([element([0, 0, 0], [3, 16, 16], {'all': 'd'})], {'d': 'store_door'}, 'store_door')
    for n in range(3):
        m[f'cake_{n}'] = model([element([0, 0, 0], [16, 2, 16], {'all': 'm'}),
                                element([3, 2, 8], [13, 14, 8], {'north': 'c', 'south': 'c'}),
                                element([8, 2, 3], [8, 14, 13], {'east': 'c', 'west': 'c'}),
                                element([2, 2, 2], [14, 15, 14], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g'})],
                               {'m': 'marble', 'c': f'cake_{n}', 'g': 'glass'}, 'marble', 'translucent')
    return m


KINDS = ['icebox', 'tub', 'shutter', 'pipe', 'thermo', 'store_door', 'store_door_top', 'cake']
STAGES = {'icebox': 4, 'tub': 4, 'shutter': 2, 'pipe': 4, 'thermo': 4, 'store_door': 2, 'store_door_top': 2, 'cake': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/pantry/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES[kind] else STAGES[kind] - 1
                v = {'model': NS + f'block/pantry/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pantry_fitting.json'] = {'variants': variants}
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
            assert f'block/pantry/{tex.split("pantry/")[1]}.png' in images, (key, tex)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'block.the_oldest_house.pantry_fitting'
    if check:
        assert lang.get(key) == 'Pantry Fittings', key
    else:
        lang[key] = 'Pantry Fittings'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the cold pantry: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
