"""The quiet and threshold halls (0.4.375, docs/TEXTURE_PASS_PAINT_0_4_375.md): the texture pass's release 2b.

Draws, in native Minecraft pixels from fixed seeds, the surfaces of the halls that test a reader rather than lead them on (the last
sconce, the watched threshold, the mirror hall, the blind stretch and the quiet hall): sheet linoleum laid in tiles and the same
worn pale down the middle, painted plaster and the same marked, a wall foot of darker paint between a rubber cove and a dado rail,
dark panelling for the quiet hall, and a ceiling of fibre tiles in a grid. Writes the models and block states, and the blocks'
names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/hall_paint/'

LINO = '#a39c8d'
LINO_SEAM = '#857e71'
PAINT = '#c6c1b0'
LOWER = '#7b8273'
RAIL = '#5b4734'
COVE = '#3b3936'
WOOD = '#4b3425'
TILE = '#d3cfc3'
GRID = '#aeaba2'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def lino(seed, worn=False):
    """Four tiles a block, each its own tone, flecked; along the seams a faint dark line. Worn: paler towards the middle of each
    tile where feet go, the flecks rubbed faint, and one scuff."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = [rnd.randint(-5, 5) for _ in range(4)]
    for y in range(16):
        for x in range(16):
            tile = (y // 8) * 2 + x // 8
            k = tones[tile] + rnd.randint(-2, 2)
            if worn:
                cx, cy = (x // 8) * 8 + 3.5, (y // 8) * 8 + 3.5
                k += 9 - int(1.6 * (abs(x - cx) + abs(y - cy)))
            im.putpixel((x, y), shade(LINO, k))
    for _ in range(26 if not worn else 12):
        x, y = rnd.randrange(16), rnd.randrange(16)
        k = rnd.choice((-16, -12, 12, 16)) if not worn else rnd.choice((-7, 7))
        im.putpixel((x, y), shade(im.getpixel((x, y)), k))
    for i in range(16):
        im.putpixel((i, 0), shade(LINO_SEAM, rnd.randint(-2, 2) + (6 if worn else 0)))
        im.putpixel((i, 8), shade(LINO_SEAM, rnd.randint(-2, 2) + (6 if worn else 0)))
        im.putpixel((0, i), shade(LINO_SEAM, rnd.randint(-2, 2) + (6 if worn else 0)))
        im.putpixel((8, i), shade(LINO_SEAM, rnd.randint(-2, 2) + (6 if worn else 0)))
    if worn:
        x, y = rnd.randrange(2, 9), rnd.randrange(9, 14)
        for dx in range(4):
            im.putpixel((x + dx, y + (dx // 2)), shade(im.getpixel((x + dx, y + (dx // 2))), -14))
    return im


def paint(seed, marked=False):
    """Plaster under several coats of flat paint: barely any grain, a roller's faint stripes. Marked: a scuff where something was
    carried past, and a chip where the paint has come away and shows the darker coat under it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    stripes = [rnd.choice((0, 0, 0, 1, -1)) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(PAINT, stripes[x] + rnd.randint(-2, 2)))
    if marked:
        x, y = rnd.randrange(1, 8), rnd.randrange(5, 11)
        for dx in range(rnd.randrange(5, 8)):
            yy = y + (1 if dx > 3 else 0)
            im.putpixel((x + dx, yy), shade(im.getpixel((x + dx, yy)), -13 + dx))
        cx, cy = rnd.randrange(10, 13), rnd.randrange(2, 6)
        flake = [(cx, cy), (cx + 1, cy), (cx + 1, cy + 1), (cx + 2, cy + 1), (cx + 3, cy + 1), (cx + 2, cy + 2)]
        for px, py in flake:
            im.putpixel((px, py), shade(LOWER, 22 + rnd.randint(-3, 3)))
        for px, py in ((cx, cy - 1), (cx + 1, cy - 1), (cx + 2, cy), (cx + 3, cy)):
            im.putpixel((px, py), shade(PAINT, 9))
    return im


def dado(seed):
    """The foot of a painted wall: a dado rail of stained wood along the top, lit on its upper edge; below it the darker paint that
    takes the knocks, scuffed here and there; and at the floor a rubber cove, lit where it curves out."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            if y < 3:
                c = shade(RAIL, rnd.randint(-2, 2) + (12 if y == 0 else -6 if y == 2 else 0))
            elif y < 13:
                c = shade(LOWER, rnd.randint(-2, 2) + (-8 if y == 3 else 0))
            else:
                c = shade(COVE, rnd.randint(-2, 2) + (10 if y == 13 else 0))
            im.putpixel((x, y), c)
    for _ in range(3):
        x, y = rnd.randrange(16), rnd.randrange(8, 12)
        im.putpixel((x, y), shade(im.getpixel((x, y)), -14))
    return im


def panel(seed, foot=False):
    """Dark stained panelling: a frame of stiles and rails round a sunk panel, its grain running up the block, the frame lit along
    its upper edges. With a foot, a skirting board takes the bottom of the block."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    grain = [rnd.randint(-4, 4) for _ in range(16)]
    bottom = 11 if foot else 14
    for y in range(16):
        for x in range(16):
            frame = x < 2 or x > 13 or y < 2 or y >= bottom
            if frame:
                k = rnd.randint(-2, 2) + (8 if y == 0 or (x == 0 and y < bottom) else 0)
            else:
                k = grain[x] - 8 + rnd.randint(-1, 1)
                if x == 2 or y == 2:
                    k -= 8
                elif x == 13 or y == bottom - 1:
                    k += 6
            im.putpixel((x, y), shade(WOOD, k))
    if foot:
        for x in range(16):
            for y in range(12, 16):
                im.putpixel((x, y), shade(WOOD, rnd.randint(-2, 2) - 12 + (12 if y == 12 else 0)))
    return im


def ceiling(seed):
    """Fibre ceiling tiles in a light metal grid, four to a block, each pitted with the short worm marks the tiles are pressed with."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(TILE, rnd.randint(-2, 2)))
    for _ in range(14):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        for _ in range(rnd.randrange(1, 3)):
            if 0 <= x < 16 and 0 <= y < 16 and x % 8 and y % 8:
                im.putpixel((x, y), shade(TILE, -16))
            x += rnd.choice((-1, 0, 1)); y += rnd.choice((0, 1))
    for i in range(16):
        im.putpixel((i, 0), shade(GRID, 6)); im.putpixel((i, 8), shade(GRID, 6))
        im.putpixel((0, i), shade(GRID, -2)); im.putpixel((8, i), shade(GRID, -2))
    return im


BLOCKS = [
    ('lino', 'Linoleum'),
    ('lino_worn', 'Worn Linoleum'),
    ('wall', 'Painted Plaster'),
    ('wall_marked', 'Marked Plaster'),
    ('dado', 'Dado'),
    ('panel', 'Panelling'),
    ('panel_low', 'Panelling with Skirting'),
    ('ceiling', 'Ceiling Tiles'),
]


def textures():
    return {
        'lino': lino(375), 'lino_worn': lino(375, worn=True), 'wall': paint(3751), 'wall_marked': paint(3751, marked=True),
        'dado': dado(3752), 'panel': panel(3753), 'panel_low': panel(3753, foot=True), 'ceiling': ceiling(3754),
    }


def files():
    out = {}
    for name, _ in BLOCKS:
        out[f'models/block/hall_paint_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        model = NS + f'block/hall_paint_{name}'
        variant = {'model': model}
        if name.startswith('lino'):
            # The linoleum turns with where it lies, so a long floor does not show its repeat.
            variant = [{'model': model}, {'model': model, 'y': 90}, {'model': model, 'y': 180}, {'model': model, 'y': 270}]
        out[f'blockstates/hall_paint_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/hall_paint' / f'{key}.png'
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), key
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for name, title in BLOCKS:
        key = f'block.the_oldest_house.hall_paint_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the painted halls: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
