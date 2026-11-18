#!/usr/bin/env python3
"""0.4.453: the household furniture at sixteen pixels to the block.

The owner, of the reading desk: "It's got an hd texture that isn't in line with vanilla." The desk was redrawn in 0.4.452. The
rest of the household furniture (the walnut desk, the cane chair, the bedside table, the chest of drawers, the two armchairs, the
sofa, the footstool, the formica table and stool, the radiator and the washing machine) was drawn from eleven pictures 32 pixels
square, twice the game's scale. This draws each of them again at sixteen, in its own colours, under textures/block/household/, and
points every model that used an old one at the new one. The old pictures are left where they are: the art inventory records them.

Models are not rewritten, only pointed: their shapes and the parts of each picture they show are as they were. Every one of those
parts falls on whole pixels at sixteen.

Run with --check to verify the files without writing them.
"""
from pathlib import Path
import argparse
import math
import random
import re
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'
FOLDER = ASSETS / 'textures/block/household'
NAMES = ['walnut', 'cane', 'bedside_front', 'drawer_front', 'blue_weave', 'floral', 'green_cloth', 'formica', 'metal_legs', 'enamel',
         'washer_front']
OLD = re.compile(r'"' + MOD + r':block/(' + '|'.join(NAMES) + r')"')
BRASS, BRASS_DARK = (181, 151, 90), (120, 96, 52)


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def put(im, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), (rgb(c) if isinstance(c, str) else c) + (255,))


def grain(seed, tones, streak):
    """Wood with the grain upright: each column a long run of one tone, changing a little at a time, and a dark streak here and there."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        y = 0
        while y < 16:
            run = rnd.randint(4, 9)
            c = rnd.choice(tones)
            for k in range(run):
                put(im, x, y + k, c)
            y += run
    for _ in range(3):
        x, y0 = rnd.randint(0, 15), rnd.randint(0, 9)
        for y in range(y0, y0 + rnd.randint(3, 6)):
            put(im, x, y, streak)
    return im


def weave(a, b):
    """Cloth: two close tones, a thread each way."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            put(im, x, y, a if (x + y) % 2 == 0 else b)
    return im


def walnut():
    return grain(21, ['#5a3f2c', '#5a3f2c', '#614633', '#543926'], '#463020')


def cane():
    """Woven cane: strands three pixels wide over and under each other, a dark gap where four of them meet."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            gx, gy = x % 4 == 3, y % 4 == 3
            over = (x // 4 + y // 4) % 2 == 0
            if gx and gy:
                c = '#735b33'
            elif gx:
                c = '#987946' if over else '#735b33'
            elif gy:
                c = '#735b33' if over else '#987946'
            else:
                c = '#b7985d' if over else '#ad8d54'
            put(im, x, y, c)
    return im


def drawer(im, x0, x1, y, wood, light, shade, pulls):
    """One drawer's face, four rows deep from `y`: a lit top edge, two rows of wood with its pulls, a shaded foot."""
    for x in range(x0, x1):
        put(im, x, y, light)
        put(im, x, y + 1, wood)
        put(im, x, y + 2, wood)
        put(im, x, y + 3, shade)
    for px in pulls:
        put(im, px, y + 1, BRASS)
        put(im, px + 1, y + 1, BRASS_DARK)


def bedside_front():
    """The bedside table's face. The model shows rows 0 to 10: two drawers, with a dark line over, between and under them."""
    im = grain(22, ['#6d4c2e', '#6d4c2e', '#725133', '#684829'], '#5c3f25')
    for y in (0, 5, 10):
        for x in range(16):
            put(im, x, y, '#4a321f')
    for y in (1, 6):
        drawer(im, 1, 15, y, rgb('#704f31'), rgb('#7f5f3c'), rgb('#5e4127'), (7,))
    return im


def drawer_front():
    """A chest's face: four drawers, each under a dark line, two brass pulls to a drawer. The desks show its right-hand six columns,
    which hold one pull of each."""
    im = grain(23, ['#593e2a', '#593e2a', '#5d422e', '#543926'], '#463020')
    for k in range(4):
        y = k * 4
        for x in range(16):
            put(im, x, y, '#3d2a1d')
            put(im, x, y + 1, '#8f6f46' if 1 <= x <= 14 else '#614632')
        for px in (4, 11):
            put(im, px, y + 2, BRASS)
            put(im, px + 1, y + 2, BRASS_DARK)
    return im


def blue_weave():
    im = weave(rgb('#465a70'), rgb('#42566c'))
    for k in range(16):
        for line in (3, 11):
            put(im, line, k, '#556778')
            put(im, k, line, '#556778')
    return im


def floral():
    im = weave(rgb('#ada185'), rgb('#a99d81'))
    for x, y in ((3, 3), (11, 2), (7, 10), (14, 13)):
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            put(im, (x + dx) % 16, (y + dy) % 16, '#926c63')
        put(im, x, y, '#d1ae5c')
        put(im, (x + 1) % 16, (y + 2) % 16, '#5f7a4c')
        put(im, (x + 1) % 16, (y + 3) % 16, '#5f7a4c')
    return im


def green_cloth():
    return weave(rgb('#546a50'), rgb('#4f654b'))


def formica():
    """Laminate: flat, with the fleck it was sold with."""
    rnd = random.Random(27)
    im = Image.new('RGBA', (16, 16), rgb('#b76d55') + (255,))
    for c, n in (('#be755d', 14), ('#a8644f', 9)):
        for _ in range(n):
            put(im, rnd.randint(0, 15), rnd.randint(0, 15), c)
    return im


def metal_legs():
    """Tube: every other column lit, so that a leg two pixels wide has a lit side and a dark one."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            put(im, x, y, '#7b796a' if x % 2 == 0 else '#5f5d4f')
    return im


def enamel():
    rnd = random.Random(29)
    im = Image.new('RGBA', (16, 16), rgb('#d3cdb5') + (255,))
    for _ in range(12):
        put(im, rnd.randint(0, 15), rnd.randint(0, 15), '#d9d3bb')
    for _ in range(6):
        put(im, rnd.randint(0, 15), rnd.randint(0, 15), '#cbc5ad')
    return im


def washer_front():
    """The machine's face: a strip of controls, the round door with the wash behind its glass, and a kick plate."""
    im = enamel()
    for x in range(16):
        for y in range(1, 4):
            put(im, x, y, '#c6c2b0')
        put(im, x, 4, '#b3ae9b')
        put(im, x, 15, '#8d8a7a')
    for x in range(2, 7):
        put(im, x, 2, '#75776e')
    put(im, 11, 2, '#4e514a')
    put(im, 12, 2, '#4e514a')
    put(im, 11, 1, '#75776e')
    for y in range(5, 15):
        for x in range(2, 14):
            d = math.hypot(x - 7.5, y - 9.5)
            if d < 3.3:
                put(im, x, y, '#6f7a76' if y >= 11 or (y == 10 and 6 <= x <= 9) else '#2e3a3c')
            elif d < 4.7:
                put(im, x, y, '#75776e')
    put(im, 6, 8, '#9fb3b3')
    put(im, 5, 9, '#9fb3b3')
    return im


def stand_zinc():
    """0.4.704, the hall stand's drip tray: galvanised zinc with its spangle, a lit rim, a darker line inside it, and the marks
    left where wet umbrellas stood."""
    rnd = random.Random(31)
    im = Image.new('RGBA', (16, 16), rgb('#878c89') + (255,))
    for _ in range(26):
        put(im, rnd.randint(0, 15), rnd.randint(0, 15), '#949995')
    for _ in range(14):
        put(im, rnd.randint(0, 15), rnd.randint(0, 15), '#7d827f')
    for x, y in ((5, 6), (10, 9), (7, 11)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
            put(im, x + dx, y + dy, '#737874')
    put(im, 11, 4, '#8b6b4b')
    put(im, 12, 4, '#7a5d41')
    for k in range(16):
        put(im, k, 0, '#a5aaa6')
        put(im, 0, k, '#a5aaa6')
        put(im, k, 15, '#6c716e')
        put(im, 15, k, '#6c716e')
    return im


def stand_glass():
    """0.4.704, the hall stand's small mirror: old silvered glass, darker towards its foot, a pale sheen across it from corner to
    corner, and the brown spots where the silver behind it has gone."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            put(im, x, y, '#66707a' if y < 6 else '#5d6670' if y < 11 else '#555e67')
    for k in range(3, 13):
        put(im, k, 15 - k, '#8b96a1')
        put(im, k + 1, 15 - k, '#76818c')
        put(im, k - 1, 15 - k, '#727c87')
    for x, y, c in ((2, 3, '#5f5747'), (3, 2, '#6b6352'), (13, 12, '#5f5747'), (12, 13, '#6b6352'), (14, 6, '#5f5747'), (1, 10, '#6b6352')):
        put(im, x, y, c)
    for k in range(16):
        put(im, k, 0, '#7d8791')
        put(im, 0, k, '#7d8791')
    return im


def stand_brass():
    """0.4.704, the hall stand's hooks: brass rod, every other column lit, so a hook one pixel thick shows a lit side and a dark."""
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            put(im, x, y, BRASS if x % 2 == 0 else BRASS_DARK)
    for y in range(0, 16, 4):
        put(im, 0, y, '#d6bd7c')
    return im


def pictures():
    return {'walnut': walnut(), 'cane': cane(), 'bedside_front': bedside_front(), 'drawer_front': drawer_front(),
            'blue_weave': blue_weave(), 'floral': floral(), 'green_cloth': green_cloth(), 'formica': formica(),
            'metal_legs': metal_legs(), 'enamel': enamel(), 'washer_front': washer_front(),
            'stand_zinc': stand_zinc(), 'stand_glass': stand_glass(), 'stand_brass': stand_brass()}


FACES = ('north', 'south', 'east', 'west', 'up', 'down')


def uv(face, f, t):
    """The game's own mapping of a face of a box onto its picture, so every face falls on whole pixels as the box does."""
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {'north': [16 - x1, 16 - y1, 16 - x0, 16 - y0], 'south': [x0, 16 - y1, x1, 16 - y0], 'west': [z0, 16 - y1, z1, 16 - y0],
            'east': [16 - z1, 16 - y1, 16 - z0, 16 - y0], 'up': [x0, z0, x1, z1], 'down': [x0, 16 - z1, x1, 16 - z0]}[face]


def part(f, t, texture, faces=FACES, front=None):
    out = {'from': list(f), 'to': list(t), 'faces': {}}
    for face in faces:
        out['faces'][face] = {'uv': uv(face, f, t), 'texture': '#' + (front if front and face == 'north' else texture)}
    return out


def hall_stand():
    """0.4.704, the hall stand in the Navidsons' front hall (HouseKeeping.STAND), drawn facing north, its back to the wall at the
    south: two back posts under a crest, a panel between them with a small mirror in a frame and a brass hook on each post; in front,
    two short posts and a rail all round at hip height, and in the well they make a zinc drip tray with its own walls, so that what a
    reader stands in it stands in a tray."""
    w, z, g, b = 'walnut', 'zinc', 'glass', 'brass'
    parts = [
        part((1, 0, 13), (3, 15, 15), w), part((13, 0, 13), (15, 15, 15), w), part((0, 15, 12), (16, 16, 16), w),
        part((3, 5, 14), (13, 15, 15), w, ('north', 'up', 'down')),
        part((3, 14, 13), (13, 15, 14), w, ('north', 'down')), part((3, 7, 13), (13, 8, 14), w, ('north', 'up')),
        part((3, 8, 13), (4, 14, 14), w, ('north', 'east')), part((12, 8, 13), (13, 14, 14), w, ('north', 'west')),
        part((4, 8, 13.5), (12, 14, 14), g, ('north',)),
        part((1.5, 12, 11), (2.5, 13, 13), b, ('north', 'east', 'west', 'up', 'down')), part((1.5, 13, 11), (2.5, 14, 12), b, ('north', 'south', 'east', 'west', 'up')),
        part((13.5, 12, 11), (14.5, 13, 13), b, ('north', 'east', 'west', 'up', 'down')), part((13.5, 13, 11), (14.5, 14, 12), b, ('north', 'south', 'east', 'west', 'up')),
        part((1, 0, 3), (3, 9, 5), w), part((13, 0, 3), (15, 9, 5), w),
        part((3, 7, 3), (13, 9, 5), w, ('north', 'south', 'up', 'down')),
        part((1, 7, 5), (3, 9, 13), w, ('east', 'west', 'up', 'down')), part((13, 7, 5), (15, 9, 13), w, ('east', 'west', 'up', 'down')),
        part((1, 1, 5), (3, 2, 13), w, ('east', 'west', 'up', 'down')), part((13, 1, 5), (15, 2, 13), w, ('east', 'west', 'up', 'down')),
        part((3, 0, 5), (13, 0.5, 13), z, ('up',)),
        part((3, 0, 5), (13, 2, 6), z, ('north', 'south', 'up')), part((3, 0, 12), (13, 2, 13), z, ('north', 'south', 'up')),
        part((3, 0, 6), (4, 2, 12), z, ('east', 'up')), part((12, 0, 6), (13, 2, 12), z, ('west', 'up')),
    ]
    return {'parent': 'minecraft:block/block',
            'textures': {'walnut': f'{MOD}:block/household/walnut', 'particle': f'{MOD}:block/household/walnut',
                         'zinc': f'{MOD}:block/household/stand_zinc', 'glass': f'{MOD}:block/household/stand_glass',
                         'brass': f'{MOD}:block/household/stand_brass'},
            'elements': parts}


def stand_files():
    """The stand's model, and the furniture's block states with its four facings, as the tool writes them."""
    import json
    model_path = ASSETS / 'models/block/furniture_hall_stand.json'
    states_path = ASSETS / 'blockstates/household_furniture.json'
    states = json.loads(states_path.read_text())
    for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        states['variants'][f'facing={facing},kind=hall_stand'] = {'model': f'{MOD}:block/furniture_hall_stand', 'y': y, 'uvlock': False}
    return {model_path: json.dumps(hall_stand(), indent=2) + '\n', states_path: json.dumps(states, indent=2) + '\n'}


def pointed():
    """Every model that names one of the old pictures, as it reads once pointed at the new one."""
    out = {}
    for path in sorted((ASSETS / 'models').rglob('*.json')):
        text = path.read_text()
        new = OLD.sub(lambda m: f'"{MOD}:block/household/{m.group(1)}"', text)
        if new != text:
            out[path] = new
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--sheet')
    args = ap.parse_args()
    pics = pictures()
    if args.sheet:
        page = Image.new('RGBA', (len(pics) * 136 + 8, 144), (90, 90, 90, 255))
        for i, im in enumerate(pics.values()):
            page.paste(im.resize((128, 128), Image.NEAREST), (8 + i * 136, 8))
        page.save(args.sheet)
        return
    stale = pointed()
    if args.check:
        for name, im in pics.items():
            path = FOLDER / f'{name}.png'
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool draws')
        extra = {p.stem for p in FOLDER.glob('*.png')} - set(pics)
        if extra:
            raise SystemExit(f'pictures the tool does not make: {sorted(extra)}')
        if stale:
            raise SystemExit(f'{len(stale)} models still name a 32-pixel household picture, {next(iter(stale)).relative_to(ROOT)} among them')
        for path, text in stand_files().items():
            if not path.exists() or path.read_text() != text:
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool writes for the hall stand')
        print(f'the household furniture: {len(pics)} pictures at sixteen pixels, and no model on the old ones')
        return
    FOLDER.mkdir(parents=True, exist_ok=True)
    for name, im in pics.items():
        im.save(FOLDER / f'{name}.png')
    for path, text in stale.items():
        path.write_text(text)
    for path, text in stand_files().items():
        path.write_text(text)
    print(f'the household furniture: {len(pics)} pictures, {len(stale)} models pointed at them')


if __name__ == '__main__':
    main()
