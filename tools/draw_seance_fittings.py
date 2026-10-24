"""Draw the séance drawing room's cupboard, letters, sitting record and album, with their native models."""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
PREFIX = 'the_oldest_house:block/seance/'
WOOD, GRAIN, DARK, BRASS = '#4a2a1c', '#5a3524', '#2c1810', '#b8913f'

def shade(c, k):
    """A colour lightened (k > 0) or darkened (k < 0) by k, in 0..255 steps."""
    c = c.lstrip('#'); r, g, b = (int(c[i:i + 2], 16) for i in (0, 2, 4))
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), 255)

def grain(im, base, seed, streaks=7, knots=1):
    """Vanilla-style plank grain: per-pixel tone noise, long wavy streaks and the odd knot, all from a fixed seed."""
    rnd = random.Random(seed); px = im.load()
    for y in range(32):
        row = rnd.randint(-5, 5)
        for x in range(32):
            px[x, y] = shade(base, row + rnd.randint(-4, 4))
    for _ in range(streaks):
        y0, amp, period, phase, k = rnd.randint(0, 31), rnd.choice((0, 1, 1, 2)), rnd.uniform(6, 14), rnd.uniform(0, 6), rnd.choice((-16, -12, -9, 8))
        x0 = rnd.randint(0, 31); length = rnd.randint(10, 32)
        for i in range(length):
            x = (x0 + i) % 32; y = (y0 + round(math.sin((x + phase) / period * math.pi) * amp)) % 32
            px[x, y] = shade(base, k + rnd.randint(-2, 2))
    for _ in range(knots):
        kx, ky = rnd.randint(4, 27), rnd.randint(4, 27)
        for dx in range(-2, 3):
            for dy in range(-1, 2):
                if abs(dx) + abs(dy) <= 2:
                    px[kx + dx, ky + dy] = shade(base, -22 if dy == 0 and abs(dx) <= 1 else -10)

def paintings():
    out = {}
    def tile(name, color):
        im = Image.new('RGBA', (32, 32), color)
        out[name] = im
        return ImageDraw.Draw(im)
    def wood(name, seed, knots=1):
        im = Image.new('RGBA', (32, 32), WOOD); grain(im, WOOD, seed, knots=knots); out[name] = im
        return ImageDraw.Draw(im)
    wood('wood', 11)
    d = wood('cabinet_top', 12, 0); d.rectangle((0, 0, 31, 31), outline=DARK)
    d = wood('cabinet_side', 13); d.rectangle((2, 4, 29, 28), outline=DARK); d.line((3, 5, 28, 5), fill='#6b4330')
    # Front: cornice molding (rows 0-3), two paneled doors with a brass escutcheon (rows 4-28), plinth (29-31).
    d = wood('cabinet_front', 14, 0)
    d.rectangle((0, 0, 31, 3), fill=DARK); d.line((0, 1, 31, 1), fill='#6b4330'); d.line((0, 3, 31, 3), fill='#1e100a')
    d.rectangle((0, 29, 31, 31), fill=DARK); d.line((0, 29, 31, 29), fill='#6b4330')
    for x0 in (2, 16):
        d.rectangle((x0, 5, x0 + 13, 27), outline=DARK)
        for y0 in (7, 17):
            d.rectangle((x0 + 2, y0, x0 + 11, y0 + 8), outline='#6b4330'); d.line((x0 + 3, y0 + 8, x0 + 11, y0 + 8), fill='#2a160d')
            d.rectangle((x0 + 3, y0 + 1, x0 + 10, y0 + 7), fill=GRAIN)
    d.line((16, 5, 16, 27), fill='#1e100a')
    d.rectangle((14, 14, 17, 18), fill=BRASS); d.point((14, 14), fill='#e0c070'); d.point((15, 16), fill=DARK); d.point((16, 16), fill=DARK)
    d = tile('cabinet_inside', '#1f120b')
    for y in (11, 20):
        d.rectangle((2, y, 29, y + 1), fill=GRAIN); d.line((2, y + 2, 29, y + 2), fill='#140b06')
    d.rectangle((5, 6, 9, 10), fill='#6e7a74'); d.line((5, 6, 9, 6), fill='#9aa59c')
    d.rectangle((18, 15, 26, 19), fill='#c9bba0'); d.line((18, 17, 26, 17), fill='#a8987c')
    d.rectangle((11, 23, 14, 28), fill='#8a6c3a'); d.point((12, 24), fill='#b08a4c')
    d.rectangle((0, 0, 31, 31), outline=DARK)
    d = wood('cabinet_door', 15, 0); d.rectangle((0, 0, 31, 31), outline=DARK); d.rectangle((4, 4, 27, 27), outline=WOOD)
    # A bundle of unposted letters, tied crosswise with a faded ribbon; a stamp and a postmark that never went out.
    d = tile('letters', '#e2d6b4')
    for y in range(4, 30, 5):
        d.line((3, y, 28, y), fill='#c8b98f')
    d.line((3, 2, 28, 2), fill='#f1e7c8'); d.rectangle((0, 0, 31, 31), outline='#9c8a63')
    d.rectangle((14, 0, 17, 31), fill='#8c3b3b'); d.rectangle((0, 14, 31, 17), fill='#8c3b3b'); d.line((14, 0, 14, 31), fill='#a64b48'); d.line((0, 14, 31, 14), fill='#a64b48')
    d.rectangle((13, 13, 18, 18), fill='#a64b48'); d.point((15, 15), fill='#6e2c2c')
    d.rectangle((22, 3, 27, 9), fill='#5b6f8a'); d.rectangle((23, 4, 26, 8), outline='#c7cfd8')
    d.arc((18, 4, 25, 11), 200, 340, fill='#6d6470'); d.arc((17, 5, 24, 12), 200, 340, fill='#6d6470')
    for y, w in ((21, 7), (24, 5), (27, 6)):
        d.line((5, y, 5 + w, y), fill='#4a4458')
    d = tile('letters_side', '#e2d6b4')
    for y in range(1, 32, 3):
        d.line((0, y, 31, y), fill='#bfae84')
    d.rectangle((14, 0, 17, 31), fill='#8c3b3b')
    # The sitting record lies open: two pages of a cramped, uneven hand in brown ink, and a drop of candle wax.
    im = Image.new('RGBA', (32, 32), '#e8dcc0'); out['record_pages'] = im; d = ImageDraw.Draw(im); rnd = random.Random(21)
    d.rectangle((0, 0, 31, 31), outline='#6b3b2a'); d.line((15, 1, 15, 30), fill='#a8987c'); d.line((16, 1, 16, 30), fill='#d2c6a8')
    for x0, x1 in ((3, 13), (18, 29)):
        for y in range(4, 28, 3):
            x = x0
            while x < x1 - 1:
                w = rnd.randint(1, 4)
                for i in range(w):
                    if x + i < x1 and rnd.random() < .85:
                        d.point((x + i, y + (1 if rnd.random() < .2 else 0)), fill=rnd.choice(('#5a4636', '#6a5442', '#4e3c30')))
                x += w + 1
    d.ellipse((21, 21, 25, 24), fill='#efe8d6', outline='#d7cba8'); d.point((22, 22), fill='#fbf6e8')
    d = tile('leather', '#5a1f1c'); d.rectangle((1, 1, 30, 30), outline='#b8913f')
    for x, y in [(6, 9), (21, 14), (12, 25), (26, 6)]:
        d.point((x, y), fill='#6e2b27')
    # The album: green leather, a gilt frame and an oval window over a sepia family portrait.
    d = tile('album_top', '#27402f')
    d.rectangle((2, 2, 29, 29), outline=BRASS); d.rectangle((4, 4, 27, 27), outline='#8a7038')
    d.ellipse((8, 7, 23, 24), fill='#e3d0a8', outline=BRASS)
    # Three sitters, apart: father, mother and a child, each a face under dark hair over dark shoulders.
    figure, face = '#5a4330', '#b08c64'
    for hx, hy, bx0, bx1 in ((10, 10, 9, 12), (15, 10, 14, 17), (20, 14, 19, 21)):
        d.line((hx, hy - 1, hx + 1, hy - 1), fill=figure); d.rectangle((hx, hy, hx + 1, hy + 1), fill=face)
        d.rectangle((bx0, hy + 2, bx1, 21), fill=figure)
    d.line((9, 22, 22, 22), fill='#b29a70')
    for x, y in ((0, 0), (27, 0), (0, 27), (27, 27)):
        d.rectangle((x, y, x + 4, y + 4), fill=BRASS); d.point((x + 1, y + 1), fill='#e0c070')
    d = tile('album_side', '#27402f')
    d.rectangle((0, 6, 31, 25), fill='#e4d7b5')
    for y in range(8, 25, 3):
        d.line((0, y, 31, y), fill='#c4b38a')
    d.line((0, 6, 31, 6), fill=BRASS); d.line((0, 25, 31, 25), fill=BRASS)
    return out

def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures['all'])
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e

def table_legs():
    parts = [element([1, 11, 1], [15, 13, 15], {'all': 'wood'})]
    for x in (2, 12):
        for z in (2, 12):
            parts.append(element([x, 0, z], [x + 2, 11, z + 2], {'all': 'wood', 'up': None}))
    return parts

def models():
    def model(elements, materials):
        return {'ambientocclusion': True, 'textures': {**{m: PREFIX + m for m in materials}, 'particle': PREFIX + 'wood'}, 'elements': elements}
    result = {}
    cabinet = [element([0.5, 0, 3.5], [15.5, 1.5, 16], {'all': 'cabinet_side', 'north': 'cabinet_front', 'down': 'wood'}),
               element([0, 14, 3], [16, 16, 16], {'all': 'cabinet_side', 'north': 'cabinet_front', 'up': 'cabinet_top'})]
    result['seance_cabinet'] = model(cabinet + [element([1, 1.5, 4], [15, 14, 16], {'all': 'cabinet_side', 'north': 'cabinet_front', 'up': None, 'down': None})],
                                     ['cabinet_front', 'cabinet_side', 'cabinet_top', 'wood'])
    result['seance_cabinet_open'] = model(cabinet + [element([1, 1.5, 4], [15, 14, 16], {'all': 'cabinet_side', 'north': 'cabinet_inside', 'up': None, 'down': None}),
                                                     element([0.2, 2, -3], [1, 14, 4], {'all': 'cabinet_door'}),
                                                     element([15, 2, -3], [15.8, 14, 4], {'all': 'cabinet_door'})],
                                          ['cabinet_front', 'cabinet_side', 'cabinet_top', 'cabinet_inside', 'cabinet_door', 'wood'])
    result['seance_letters'] = model(table_legs() + [element([4, 13, 5], [12, 15.5, 11], {'all': 'letters_side', 'up': 'letters', 'down': None},
                                                             uv={'up': [0, 0, 16, 16], 'north': [0, 0, 16, 5], 'south': [0, 0, 16, 5], 'east': [0, 0, 16, 5], 'west': [0, 0, 16, 5]})],
                                     ['wood', 'letters', 'letters_side'])
    tilt = {'angle': -22.5, 'axis': 'x', 'origin': [8, 13, 8]}
    result['seance_sitting'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'wood'}),
                                      element([6.5, 2, 6.5], [9.5, 12, 9.5], {'all': 'wood', 'up': None, 'down': None}),
                                      element([1, 12, 2], [15, 13.5, 14], {'all': 'wood'}, tilt),
                                      element([2, 13.5, 3], [14, 14.5, 13], {'all': 'leather', 'up': 'record_pages', 'down': None}, tilt,
                                              uv={'up': [0, 0, 16, 16], 'north': [0, 0, 16, 1], 'south': [0, 0, 16, 1], 'east': [0, 0, 16, 1], 'west': [0, 0, 16, 1]})],
                                     ['wood', 'leather', 'record_pages'])
    result['seance_album'] = model(table_legs() + [element([3, 13, 3.5], [13, 16, 12.5], {'all': 'album_side', 'up': 'album_top', 'down': None},
                                                           uv={'up': [0, 0, 16, 16], 'north': [0, 3, 16, 12.5], 'south': [0, 3, 16, 12.5], 'east': [0, 3, 16, 12.5], 'west': [0, 3, 16, 12.5]})],
                                   ['wood', 'album_top', 'album_side'])
    files = {f'models/block/{k}.json': v for k, v in result.items()}
    variants = {}
    for kind in ('cabinet', 'letters', 'sitting', 'album'):
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            for open_ in ('false', 'true'):
                v = {'model': 'the_oldest_house:block/seance_' + kind + ('_open' if kind == 'cabinet' and open_ == 'true' else '')}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},open={open_}'] = v
    files['blockstates/seance_fitting.json'] = {'variants': variants}
    return files

def main():
    p = argparse.ArgumentParser(); p.add_argument('--check', action='store_true'); check = p.parse_args().check
    for name, im in paintings().items():
        path = A / f'textures/block/seance/{name}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for name, obj in models().items():
        path = A / name
        if check:
            assert json.loads(path.read_text()) == obj, name
        else:
            path.write_text(json.dumps(obj, indent=2) + '\n')
    print(('Verified' if check else 'Painted') + ' the séance cupboard, letters, sitting record and album.')

if __name__ == '__main__':
    main()
