"""Last Weekend at Pinehook (0.4.364): the cabin's fittings and the weekend's things.

16x16 textures in vanilla's manner, from small palettes with per-pixel noise and fixed seeds:
- the stereo, off and playing; the television with its tape deck, off, Casey's film and the rented film;
- the breaker box, on and with its main switch pulled; a ceiling lamp, dark and lit;
- the stove, cold, cooking and done; Jude's duffel, zipped, open, and open with the costume gone;
- the bag behind the shed, with its strap and without; the cooler; the shelf of rented films; the first-aid kit, full and taken;
- and the things readers carry: the cooler, the camera, the torch, the mixtape, the portable player, the rented film, the first-aid
  bag and the elastic strap.

Writes the pinehook_fitting block's models and states, the items' models, and their names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_tank_fittings import dot, fill, frame, element
from draw_sphinx_fittings import noisy

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/pinehook/'

WOOD = ['#7a5a3a', '#74543a', '#805e3e']
PLASTIC = ['#3a3836', '#363432', '#3e3c3a']
STEEL = ['#9a9c9e', '#94969a', '#a0a2a6']
CANVAS = ['#6e6a4c', '#686448', '#747050']
CREAM = ['#e4dcc8', '#ddd5c0', '#e8e0cc']


def clear():
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0))


def stereo(on):
    im = noisy(PLASTIC, 6400)
    frame(im, 0, 0, 16, 16, '#222120')
    fill(im, 2, 2, 7, 9, '#2a2928')
    fill(im, 9, 2, 14, 9, '#2a2928')
    for x0 in (2, 9):
        for y in range(3, 9, 2):
            for x in range(x0 + 1, x0 + 4):
                dot(im, x, y, '#4a4846')
    fill(im, 3, 11, 13, 13, '#1a1918')
    for x in range(4, 12):
        dot(im, x, 12, '#5a5856')
    dot(im, 14, 12, '#d8a040' if on else '#4a3a2a')
    if on:
        for x in (4, 6, 8, 10):
            dot(im, x, 11, '#c89030', 200)
    return im


def tv(state):
    im = noisy(WOOD, 6401)
    frame(im, 0, 0, 16, 16, '#4a3420')
    fill(im, 2, 2, 14, 11, '#1e2226')
    frame(im, 2, 2, 14, 11, '#2a2e32')
    if state == 1:
        rnd = random.Random(6402)
        for y in range(3, 10):
            for x in range(3, 13):
                dot(im, x, y, rnd.choice(['#a8b8c8', '#b8c8d4', '#c8d4dc', '#98a8b8']))
        fill(im, 5, 6, 7, 10, '#6a5a4a')
        fill(im, 9, 5, 11, 10, '#5a4a3a')
    elif state == 2:
        rnd = random.Random(6403)
        for y in range(3, 10):
            for x in range(3, 13):
                dot(im, x, y, rnd.choice(['#2a3a4a', '#1e2a36', '#34465a']))
        for x in range(4, 12):
            dot(im, x, 8, '#3a4a3a')
        fill(im, 8, 4, 9, 8, '#c8c8c8')
        dot(im, 8, 3, '#e8e8e8')
    fill(im, 2, 12, 14, 15, '#2a2826')
    fill(im, 4, 13, 10, 14, '#121110')
    dot(im, 12, 13, '#a83020' if state else '#3a2a20')
    return im


def breaker(pulled):
    im = noisy(STEEL, 6404)
    frame(im, 0, 0, 16, 16, '#5a5c5e')
    fill(im, 2, 2, 14, 14, '#6e7072')
    for i, y in enumerate((3, 6, 9)):
        for x in (4, 9):
            fill(im, x, y, x + 3, y + 2, '#2a2a2a')
            dot(im, x + 1, y, '#e8e4d8')
    fill(im, 6, 12, 10, 14, '#2a2a2a')
    if pulled:
        dot(im, 7, 13, '#c83020'); dot(im, 8, 13, '#c83020')
    else:
        dot(im, 7, 12, '#e8e4d8'); dot(im, 8, 12, '#e8e4d8')
    return im


def lamp(lit):
    im = clear()
    glass = ['#f4e8c0', '#f8eec8', '#f0e4b8'] if lit else ['#7a7468', '#746e62', '#807a6e']
    rnd = random.Random(6405 + lit)
    for y in range(4, 12):
        for x in range(4, 12):
            dot(im, x, y, rnd.choice(glass))
    frame(im, 4, 4, 12, 12, '#5a5048')
    for x in range(7, 9):
        for y in range(0, 4):
            dot(im, x, y, '#3a3632')
    return im


def stove(state):
    im = noisy(['#e0dcd4', '#d8d4cc', '#e4e0d8'], 6406)
    frame(im, 0, 0, 16, 16, '#8a8680')
    fill(im, 2, 9, 14, 15, '#2a2826')
    for x0, y0 in ((2, 2), (9, 2)):
        frame(im, x0, y0, x0 + 5, y0 + 5, '#3a3836')
        if state == 1:
            dot(im, x0 + 2, y0 + 2, '#e05a20'); dot(im, x0 + 3, y0 + 2, '#f07a30')
    if state >= 1:
        fill(im, 3, 3, 6, 6, '#3a3a3a')
        for x, y in ((4, 4), (5, 5)):
            dot(im, x, y, '#7a4a2a' if state == 1 else '#5a3a22')
    return im


def duffel(state):
    im = noisy(CANVAS, 6407)
    frame(im, 0, 0, 16, 16, '#4a4630')
    if state == 0:
        for x in range(1, 15):
            dot(im, x, 7, '#c8c0a0')
        dot(im, 13, 7, '#e8e0c0')
    else:
        fill(im, 2, 3, 14, 10, '#2a2818')
        if state == 1:
            fill(im, 3, 4, 9, 8, '#4a5a6a')
            fill(im, 10, 4, 13, 7, '#d8ccb0')
        fill(im, 4, 9, 12, 10, '#9a9c9e')
        dot(im, 12, 9, '#6a4a2a')
    return im


def bag(full):
    im = noisy(['#8a8468', '#847e62', '#908a6e'], 6408)
    frame(im, 0, 0, 16, 16, '#5a5440')
    fill(im, 3, 3, 13, 8, '#3a3628')
    fill(im, 5, 4, 9, 7, '#7a7a7a')
    if full:
        for x in range(4, 13):
            dot(im, x, 9, '#1a1a1a')
        dot(im, 4, 9, '#b0b0b0'); dot(im, 12, 9, '#b0b0b0')
    return im


def cooler():
    im = noisy(['#c84a3a', '#c04636', '#d04e3e'], 6409)
    frame(im, 0, 0, 16, 16, '#8a2e24')
    fill(im, 0, 0, 16, 4, '#ece8e0')
    frame(im, 0, 0, 16, 4, '#b0aca4')
    fill(im, 6, 1, 10, 3, '#9a968e')
    return im


def tapes():
    im = noisy(WOOD, 6410)
    frame(im, 0, 0, 16, 16, '#4a3420')
    rnd = random.Random(6411)
    for y0 in (1, 9):
        x = 1
        while x < 15:
            c = rnd.choice(['#2a3a5a', '#5a2a2a', '#2a4a3a', '#4a3a2a', '#1a1a1a', '#6a5a2a'])
            for y in range(y0, y0 + 6):
                dot(im, x, y, c)
                dot(im, x + 1, y, c)
            dot(im, x, y0 + 1, '#e8e4d8')
            x += 3
        for xx in range(16):
            dot(im, xx, y0 + 6, '#3a2818')
    return im


def kit(full):
    im = noisy(['#e8e4dc', '#e2ded6', '#eeeae2'], 6412)
    frame(im, 0, 0, 16, 16, '#9a968e')
    if full:
        fill(im, 7, 4, 9, 12, '#c82a20')
        fill(im, 4, 7, 12, 9, '#c82a20')
    else:
        fill(im, 2, 2, 14, 14, '#8a867e')
        dot(im, 8, 3, '#4a4846')
    return im


def oak():
    return noisy(WOOD, 6413)


def steel():
    return noisy(STEEL, 6414)


def canvas():
    return noisy(CANVAS, 6415)


def white():
    return noisy(['#ece8e0', '#e6e2da', '#f0ece4'], 6416)


def black():
    return noisy(PLASTIC, 6417)


# ------------------------------------------------------------------------------------------------ the things carried
def item_cooler():
    im = clear()
    fill(im, 2, 5, 14, 14, '#c84a3a'); fill(im, 2, 4, 14, 6, '#ece8e0'); frame(im, 2, 4, 14, 14, '#8a2e24')
    fill(im, 6, 2, 10, 4, '#9a968e')
    return im


def item_camera():
    im = clear()
    fill(im, 2, 5, 14, 13, '#2a2826'); frame(im, 2, 5, 14, 13, '#141312')
    fill(im, 6, 7, 11, 12, '#4a5a6a'); frame(im, 6, 7, 11, 12, '#1a1a1a'); dot(im, 8, 9, '#c8d8e8')
    fill(im, 11, 3, 14, 5, '#d8d4cc'); dot(im, 4, 6, '#c8c4bc')
    return im


def item_torch(lit=False):
    im = clear()
    for i in range(9):
        x, y = 3 + i, 12 - i
        dot(im, x, y, '#3a3836'); dot(im, x + 1, y, '#4a4846')
    fill(im, 11, 2, 14, 5, '#9a9c9e'); dot(im, 13, 2, '#f8eec0')
    dot(im, 6, 9, '#c83020')
    return im


def item_mixtape():
    im = clear()
    fill(im, 2, 4, 14, 12, '#1a1a1a'); frame(im, 2, 4, 14, 12, '#3a3a3a')
    fill(im, 3, 5, 13, 8, '#ece4cc')
    for x in range(4, 12, 2):
        dot(im, x, 6, '#2a3a8a')
    frame(im, 4, 9, 7, 11, '#6a6a6a'); frame(im, 9, 9, 12, 11, '#6a6a6a')
    return im


def item_player():
    im = clear()
    fill(im, 4, 3, 12, 14, '#5a6a7a'); frame(im, 4, 3, 12, 14, '#2a3440')
    fill(im, 5, 5, 11, 9, '#1a1a1a'); frame(im, 6, 6, 10, 8, '#6a6a6a')
    for x in range(5, 11, 2):
        dot(im, x, 11, '#2a2a2a')
    for y in range(1, 4):
        dot(im, 3, y, '#2a2a2a')
    dot(im, 2, 1, '#2a2a2a'); dot(im, 13, 2, '#2a2a2a')
    return im


def item_tape():
    im = clear()
    fill(im, 3, 2, 13, 14, '#1a1a1a'); frame(im, 3, 2, 13, 14, '#3a3a3a')
    fill(im, 4, 3, 12, 9, '#3a1a1a')
    for x in range(6, 10):
        dot(im, x, 5, '#d8d8d8')
    dot(im, 8, 4, '#f0f0f0')
    fill(im, 4, 10, 12, 13, '#e8c840')
    return im


def item_firstaid():
    im = clear()
    fill(im, 2, 4, 14, 13, '#c82a20'); frame(im, 2, 4, 14, 13, '#7a1a14')
    fill(im, 7, 6, 9, 12, '#f0ece4'); fill(im, 5, 8, 11, 10, '#f0ece4')
    fill(im, 6, 2, 10, 4, '#7a1a14')
    return im


def item_elastic():
    im = clear()
    for i in range(10):
        x, y = 3 + i, 4 + (i % 3)
        dot(im, x, y, '#1a1a1a'); dot(im, x, y + 1, '#2a2a2a')
    for x, y in ((2, 4), (13, 6)):
        frame(im, x - 1, y - 1, x + 2, y + 3, '#b0b0b0')
    for i in range(6):
        dot(im, 4 + i, 11 + (i % 2), '#1a1a1a')
    return im


ITEMS = {'pinehook_cooler': (item_cooler, 'Cooler'), 'pinehook_camera': (item_camera, 'Camera'), 'pinehook_torch': (item_torch, 'Torch'),
         'pinehook_mixtape': (item_mixtape, 'Mixtape'), 'pinehook_player': (item_player, 'Portable player'), 'pinehook_tape': (item_tape, 'Rented film'),
         'pinehook_firstaid': (item_firstaid, 'First-aid bag'), 'pinehook_elastic': (item_elastic, 'Elastic strap')}


def textures():
    t = {'oak': oak(), 'steel': steel(), 'canvas': canvas(), 'white': white(), 'black': black(), 'cooler': cooler(), 'tapes': tapes()}
    t.update({f'stereo_{n}': stereo(n == 1) for n in range(2)})
    t.update({f'tv_{n}': tv(n) for n in range(3)})
    t.update({f'breaker_{n}': breaker(n == 1) for n in range(2)})
    t.update({f'lamp_{n}': lamp(n) for n in range(2)})
    t.update({f'stove_{n}': stove(n) for n in range(3)})
    t.update({f'duffel_{n}': duffel(n) for n in range(3)})
    t.update({f'bag_{n}': bag(n == 0) for n in range(2)})
    t.update({f'kit_{n}': kit(n == 0) for n in range(2)})
    out = {f'block/pinehook/{k}.png': v for k, v in t.items()}
    out.update({f'item/{k}.png': f() for k, (f, _) in ITEMS.items()})
    return out


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': True, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def front(face_key, side='o'):
    return {'north': face_key, 'south': side, 'east': side, 'west': side, 'up': side, 'down': side}


def fitting_models():
    m = {}
    for n in range(2):
        m[f'stereo_{n}'] = model([element([1, 0, 3], [15, 10, 13], front('f', 'b'))], {'f': f'stereo_{n}', 'b': 'black'}, 'black')
        m[f'breaker_{n}'] = model([element([4, 2, 13], [12, 14, 16], front('f', 's'))], {'f': f'breaker_{n}', 's': 'steel'}, 'steel')
        m[f'lamp_{n}'] = model([element([5, 9, 5], [11, 13, 11], {'all': 'g'}), element([7.5, 13, 7.5], [8.5, 16, 8.5], {'all': 'c'})], {'g': f'lamp_{n}', 'c': 'black'}, f'lamp_{n}')
        m[f'bag_{n}'] = model([element([3, 0, 3], [13, 7, 13], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c', 'up': 't'})], {'c': 'canvas', 't': f'bag_{n}'}, 'canvas')
        m[f'kit_{n}'] = model([element([3, 3, 13], [13, 13, 16], front('f', 'w'))], {'f': f'kit_{n}', 'w': 'white'}, 'white')
    for n in range(3):
        m[f'tv_{n}'] = model([element([1, 0, 2], [15, 14, 14], front('f', 'o'))], {'f': f'tv_{n}', 'o': 'oak'}, 'oak')
        m[f'stove_{n}'] = model([element([0, 0, 0], [16, 15, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w', 'up': 't'})], {'w': 'white', 't': f'stove_{n}'}, 'white')
        m[f'duffel_{n}'] = model([element([2, 0, 4], [14, 6, 12], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c', 'up': 't'})], {'c': 'canvas', 't': f'duffel_{n}'}, 'canvas')
    m['cooler_0'] = model([element([1, 0, 3], [15, 11, 13], {'all': 'c'})], {'c': 'cooler'}, 'cooler')
    m['tapes_0'] = model([element([0, 0, 9], [16, 16, 16], front('f', 'o'))], {'f': 'tapes', 'o': 'oak'}, 'oak')
    return m


KINDS = ['stereo', 'tv', 'breaker', 'lamp', 'stove', 'duffel', 'bag', 'cooler', 'tapes', 'kit']
STAGES = {'stereo': 2, 'tv': 3, 'breaker': 2, 'lamp': 2, 'stove': 3, 'duffel': 3, 'bag': 2, 'cooler': 1, 'tapes': 1, 'kit': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {f'models/block/pinehook/{k}.json': v for k, v in fitting_models().items()}
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES[kind] else STAGES[kind] - 1
                v = {'model': NS + f'block/pinehook/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pinehook_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    return out


NAMES = {'block.the_oldest_house.pinehook_fitting': 'Cabin Fittings', **{f'item.the_oldest_house.{k}': n for k, (_, n) in ITEMS.items()}}


def main():
    check = argparse.ArgumentParser(); check.add_argument('--check', action='store_true'); check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/pinehook/{tex.split("pinehook/")[1]}.png' in images, (key, tex)
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
    if check:
        for key, name in NAMES.items():
            assert lang.get(key) == name, key
    else:
        lang.update(NAMES); lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the cabin at Pinehook: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
