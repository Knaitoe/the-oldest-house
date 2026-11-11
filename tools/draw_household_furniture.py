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


def pictures():
    return {'walnut': walnut(), 'cane': cane(), 'bedside_front': bedside_front(), 'drawer_front': drawer_front(),
            'blue_weave': blue_weave(), 'floral': floral(), 'green_cloth': green_cloth(), 'formica': formica(),
            'metal_legs': metal_legs(), 'enamel': enamel(), 'washer_front': washer_front()}


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
        print(f'the household furniture: {len(pics)} pictures at sixteen pixels, and no model on the old ones')
        return
    FOLDER.mkdir(parents=True, exist_ok=True)
    for name, im in pics.items():
        im.save(FOLDER / f'{name}.png')
    for path, text in stale.items():
        path.write_text(text)
    print(f'the household furniture: {len(pics)} pictures, {len(stale)} models pointed at them')


if __name__ == '__main__':
    main()
