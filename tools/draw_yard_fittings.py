"""The House's yard (0.4.309): clapboard, wet gravel, the garage door, the porch bulb and the station wagon, in vanilla's manner.

16x16 textures from small palettes with per-pixel noise: white-painted clapboard with grey shadow lines; gravel darkened and glossed by
rain; a garage door of dark vertical boards with a handle; a bare bulb on its cord; and the wagon: faded green paint over wood-grain
panelling, its grille and lit headlights, windows, roof rack, tailgate and tail-lamps, wheels. --check verifies everything.
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
P = NS + 'block/yard/'


def clapboard():
    im = noisy(['#e4e2da', '#dcdad2', '#e8e6e0'], 3090, [4, 3, 2])
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#a8a69e')
    return im


def wet_gravel():
    im = noisy(['#6e6a66', '#5c5854', '#7c7874', '#4e4a48', '#8a8682'], 3091, [3, 3, 2, 2, 1])
    rnd = random.Random(3092)
    for _ in range(7):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a4a8ac')
    return im


def garage_door():
    im = noisy(['#3a2a1c', '#34261a', '#40301f'], 3093)
    for x in (3, 7, 11, 15):
        for y in range(16):
            dot(im, x, y, '#22180f')
    return im


def paint():
    return noisy(['#5e7458', '#566c50', '#647a5e'], 3094)


def side():
    im = paint()
    grain = noisy(['#8a5a30', '#7e522a', '#946236'], 3095)
    for y in range(7, 14):
        for x in range(16):
            im.putpixel((x, y), grain.getpixel((x, y)))
    for x in range(16):
        dot(im, x, 6, '#c8c8c0'); dot(im, x, 14, '#2c2c2c'); dot(im, x, 15, '#2c2c2c')
    return im


def grille(lit):
    im = noisy(['#8a8c8e', '#7e8082', '#96989a'], 3096)
    for y in range(5, 12, 2):
        fill(im, 5, y, 11, y + 1, '#2a2a2a')
    for x0 in (0, 12):
        fill(im, x0, 5, x0 + 4, 11, '#fff6c8' if lit else '#9a9a8a')
        if lit:
            dot(im, x0 + 1, 6, '#ffffff'); dot(im, x0 + 2, 6, '#ffffff')
    fill(im, 0, 13, 16, 15, '#b0b2b4')
    return im


def window():
    im = paint()
    fill(im, 1, 2, 15, 14, '#1c2630')
    line(im, 3, 12, 8, 4, '#3c4a56'); line(im, 4, 12, 9, 4, '#2c3a46')
    return im


def roof():
    im = paint()
    for x in (2, 13):
        for y in range(16):
            dot(im, x, y, '#2c2c2c')
    return im


def tailgate():
    im = side()
    fill(im, 1, 2, 15, 6, '#1c2630')
    fill(im, 0, 8, 3, 11, '#8a1a14'); fill(im, 13, 8, 16, 11, '#8a1a14')
    return im


def wheel():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 7.6:
                dot(im, x, y, '#1c1c1c' if d > 3.5 else '#8a8c8e')
    return im


def bulb():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 7, 0, 9, 9, '#2a2a2a'); fill(im, 6, 9, 10, 11, '#8a8c8e')
    fill(im, 5, 11, 11, 16, '#fff6c8'); dot(im, 6, 12, '#ffffff')
    return im


def textures():
    out = {'block/yard_clapboard.png': clapboard(), 'block/yard_wet_gravel.png': wet_gravel(), 'block/yard_garage_door.png': garage_door()}
    t = {'paint': paint(), 'side': side(), 'grille_lit': grille(True), 'window': window(), 'roof': roof(), 'tailgate': tailgate(), 'wheel': wheel(), 'bulb': bulb()}
    out.update({f'block/yard/{k}.png': v for k, v in t.items()})
    return out


def model(elements, textures, particle):
    return {'ambientocclusion': True, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    m['bulb_0'] = model([element([7, 6, 7], [9, 16, 9], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}, uv={f: [7, 0, 9, 16] for f in ('north', 'south', 'east', 'west')}),
                         element([6, 6, 6], [10, 9, 10], {'all': 'b'}, uv={f: [5, 11, 9, 14] for f in ('north', 'south', 'east', 'west', 'up', 'down')})], {'b': 'bulb'}, 'bulb')
    body = element([0, 3, 0], [16, 14, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})
    m['body_0'] = model([body], {'s': 'side', 'p': 'paint'}, 'paint')
    m['body_1'] = model([body, element([-0.5, 0, 3], [0.5, 9, 12], {'west': 'w', 'east': 'w'}), element([15.5, 0, 3], [16.5, 9, 12], {'west': 'w', 'east': 'w'})], {'s': 'side', 'p': 'paint', 'w': 'wheel'}, 'paint')
    m['nose_0'] = model([element([0, 3, 0], [16, 12, 16], {'north': 'g', 'south': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'g': 'grille_lit', 's': 'side', 'p': 'paint'}, 'paint')
    m['cabin_0'] = model([element([0, 0, 0], [16, 11, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'r', 'down': 'p'})], {'w': 'window', 'r': 'roof', 'p': 'paint'}, 'roof')
    m['tail_0'] = model([element([0, 3, 0], [16, 14, 16], {'south': 't', 'north': 'p', 'east': 's', 'west': 's', 'up': 'p', 'down': 'p'})], {'t': 'tailgate', 's': 'side', 'p': 'paint'}, 'paint')
    return m


KINDS = ['bulb', 'nose', 'body', 'cabin', 'tail']
STAGES = {'bulb': 1, 'nose': 1, 'body': 2, 'cabin': 1, 'tail': 1}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'yard_fitting': 'Yard Fittings', 'yard_clapboard': 'Clapboard', 'yard_wet_gravel': 'Wet Gravel', 'yard_garage_door': 'Garage Door'}


def files():
    out = {}
    for name in ('clapboard', 'wet_gravel', 'garage_door'):
        out[f'models/block/yard_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/yard_{name}'}}
        out[f'blockstates/yard_{name}.json'] = {'variants': {'': {'model': NS + f'block/yard_{name}'}}}
    out.update({f'models/block/yard/{k}.json': v for k, v in fitting_models().items()})
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': NS + f'block/yard/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/yard_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    for key, obj in fitting_models().items():
        for tex in obj['textures'].values():
            assert f'block/yard/{tex.split("yard/")[1]}.png' in images, (key, tex)
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
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the yard: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
