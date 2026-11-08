"""Proofrock's streets and houses (0.4.377, docs/TEXTURE_PASS_PROOFROCK_0_4_377.md): the texture pass's first interior release.

Draws, in native Minecraft pixels from fixed seeds, the town's own surfaces: asphalt and the same patched, the painted centre line
(turned with the street it runs down), concrete sidewalk slabs and the same cracked in four ways, a granite kerb, lap clapboard in
three paints (fresh, a second colour and weathered back to the wood), and painted cinder block for the motel and the garage. Writes
the models and block states, and the blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/proofrock/'

ASPHALT = '#3b3b3e'
PATCH = '#2c2c2f'
SEAL = '#1d1d20'
LINE = '#c8a640'
SLAB = '#a19e97'
JOINT = '#76736d'
GRANITE = '#8c8b88'
CREAM = '#d9d0b6'
SEA = '#8ea2a6'
WOOD = '#8b8172'
FADED = '#c2bba6'
BLOCK = '#c5c1b5'
MOSS = '#5b6b38'

CRACKS = 4
WEATHERED = 3


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def asphalt(seed):
    """Asphalt: fine dark grain, the aggregate showing through as single pale and dark stones, none of them in rows."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(ASPHALT, rnd.randint(-3, 3)))
    for _ in range(22):
        x, y = rnd.randrange(16), rnd.randrange(16)
        im.putpixel((x, y), shade(ASPHALT, rnd.choice((14, 10, 8, -8))))
    return im


def patched(seed):
    """A patch of newer, smoother asphalt let into the old, its edges not quite square, sealed round with a thin band a shade
    darker than either."""
    rnd = random.Random(seed)
    im = asphalt(seed)
    y0, y1 = rnd.randrange(1, 4), rnd.randrange(11, 15)
    left = [rnd.randrange(1, 4)]
    right = [rnd.randrange(11, 15)]
    for _ in range(y0, y1):
        left.append(max(0, min(5, left[-1] + rnd.choice((0, 0, 0, 1, -1)))))
        right.append(max(10, min(15, right[-1] + rnd.choice((0, 0, 0, 1, -1)))))
    for i, y in enumerate(range(y0, y1 + 1)):
        for x in range(left[i], right[i] + 1):
            edge = x in (left[i], right[i]) or y in (y0, y1)
            im.putpixel((x, y), shade(SEAL, rnd.randint(4, 8)) if edge and rnd.random() < 0.8 else shade(PATCH, rnd.randint(-2, 2)))
    for _ in range(6):
        i = rnd.randrange(1, y1 - y0)
        x = rnd.randrange(left[i] + 1, right[i])
        im.putpixel((x, y0 + i), shade(PATCH, rnd.choice((7, 10, -5))))
    return im


def road_line(seed):
    """The centre line: a stripe of paint down the middle of the asphalt, worn through to the road in places and lifting at one
    edge. Drawn running north to south; the block state turns it for a street that runs east to west."""
    rnd = random.Random(seed)
    im = asphalt(seed)
    for y in range(16):
        for x in range(6, 10):
            if rnd.random() < 0.06:
                continue
            k = rnd.randint(-6, 4) + (6 if x == 7 else 0)
            im.putpixel((x, y), shade(LINE, k))
    return im


def sidewalk(seed):
    """A poured concrete slab, one a block: a broom finish drawn across it, the tooled joint along two edges with its lip lit,
    and a few pits."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    streak = [rnd.choice((0, 0, 1, -1, -2)) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(SLAB, streak[y] + rnd.randint(-2, 2)))
    for i in range(16):
        im.putpixel((i, 0), shade(JOINT, rnd.randint(-2, 2)))
        im.putpixel((0, i), shade(JOINT, rnd.randint(-2, 2)))
        if i:
            im.putpixel((i, 1), shade(SLAB, 8 + rnd.randint(-1, 1)))
            im.putpixel((1, i), shade(SLAB, 6 + rnd.randint(-1, 1)))
    for _ in range(4):
        x, y = rnd.randrange(3, 15), rnd.randrange(3, 15)
        im.putpixel((x, y), shade(SLAB, -14))
    return im


def crack_line(im, points, deep=-30, lit=7):
    for x, y in points:
        if 0 <= x < 16 and 0 <= y < 16:
            im.putpixel((x, y), shade(JOINT, deep))
            if x + 1 < 16 and (x + 1, y) not in points:
                im.putpixel((x + 1, y), shade(im.getpixel((x + 1, y)), lit))


def cracked(seed, k):
    """Four broken slabs, so a run of them never repeats: a crack from corner to corner; a corner broken off and sunk; a crack
    across with weeds come up through it; a slab heaved by a root, its far half darker where it tilts away."""
    rnd = random.Random(seed + 5)
    im = sidewalk(seed)
    if k == 0:
        path, x, y = [], rnd.randrange(2, 4), 2
        while y < 16 and x < 16:
            path.append((x, y))
            step = rnd.choice(((1, 1), (0, 1), (1, 0), (1, 1), (2, 1), (0, 1)))
            x += step[0]; y += step[1]
        crack_line(im, path)
        # A short branch off the main crack.
        bx, by = path[len(path) // 2]
        crack_line(im, [(bx - 1 - i, by + 1 + i // 2) for i in range(3)], deep=-20)
    elif k == 1:
        cx, cy = rnd.randrange(9, 11), rnd.randrange(9, 11)
        for y in range(cy, 16):
            for x in range(cx + (y - cy) // 2, 16):
                im.putpixel((x, y), shade(im.getpixel((x, y)), -11))
        crack_line(im, [(cx + (y - cy) // 2, y) for y in range(cy, 16)] + [(x, cy) for x in range(cx, 16)])
    elif k == 2:
        path, y = [], rnd.randrange(6, 10)
        for x in range(2, 16):
            path.append((x, y)); y += rnd.choice((0, 0, 1, -1))
        crack_line(im, path)
        for x, y in rnd.sample(path[2:-2], 4):
            im.putpixel((x, y), shade(MOSS, rnd.randint(-6, 8)))
            if y > 0:
                im.putpixel((x, y - 1), shade(MOSS, rnd.randint(0, 12)))
    else:
        tilt = rnd.randrange(7, 10)
        for y in range(16):
            for x in range(16):
                if x + y // 3 > tilt + 4:
                    im.putpixel((x, y), shade(im.getpixel((x, y)), -9))
        crack_line(im, [(tilt + 4 - y // 3, y) for y in range(2, 16)], deep=-24)
    return im


def kerb(seed):
    """A granite kerbstone, one to a block, laid running north to south: a speckled grey face, its two long edges worn, the joint
    to the next stone at the foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (-6 if x in (0, 15) else -2 if x in (1, 14) else 0)
            im.putpixel((x, y), shade(GRANITE, k))
    for _ in range(16):
        x, y = rnd.randrange(16), rnd.randrange(15)
        im.putpixel((x, y), shade(GRANITE, rnd.choice((-18, -14, 12, 16))))
    for x in range(16):
        im.putpixel((x, 15), shade(JOINT, rnd.randint(-6, 0)))
    return im


def clapboard(seed, paint, worn=False):
    """Lap siding, four boards a block: each board lit along its top, its lower lip throwing a dark line on the board below, the
    grain faint under the paint. Worn: the paint faded and gone from the boards in long runs along the grain, so the grey wood shows."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    grain = [[rnd.randint(-3, 3) for _ in range(16)] for _ in range(4)]
    bare = set()
    if worn:
        # One or two long runs a board where the paint has gone, the whole face of the board between its lit top and its lip, the
        # ends of each run ragged by a pixel.
        for board in range(4):
            for _ in range(rnd.choice((1, 1, 2))):
                x0, n = rnd.randrange(-3, 12), rnd.randrange(4, 12)
                for x in range(max(0, x0), min(16, x0 + n)):
                    for row in (1, 2):
                        if x in (x0, x0 + n - 1) and rnd.random() < 0.5:
                            continue
                        bare.add((x, board * 4 + row))
    for y in range(16):
        board, row = y // 4, y % 4
        for x in range(16):
            k = grain[board][x] // 2 + rnd.randint(-1, 1)
            k += 7 if row == 0 else -14 if row == 3 else 0
            c = shade(WOOD, grain[board][x] + rnd.randint(-2, 2) - (5 if row == 2 else 0)) if (x, y) in bare else shade(paint, k)
            im.putpixel((x, y), c)
    # A nail head at each board's end, where the boards are fixed to the studs.
    for board in range(4):
        im.putpixel((1 + 7 * (board % 2), board * 4 + 1), shade(paint, -24))
    return im


def chipped(im, seed, paint):
    """A place where the paint has come away from the clapboard and shows the board under it."""
    rnd = random.Random(seed)
    for _ in range(1):
        x, board = rnd.randrange(3, 12), rnd.randrange(4)
        for dx in range(rnd.randrange(2, 4)):
            im.putpixel((x + dx, board * 4 + 1), shade(WOOD, rnd.randint(-4, 2)))
            if dx:
                im.putpixel((x + dx, board * 4 + 2), shade(WOOD, rnd.randint(-8, -2)))
        im.putpixel((x - 1, board * 4 + 1), shade(paint, 10))
    return im


def cinder(seed):
    """Concrete block under several coats of paint: four courses a block, two blocks to a course and the courses staggered, the
    joints struck, and the block's open texture showing through the paint as small pits."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-2, 2) + (4 if y % 4 == 0 else 0)
            if rnd.random() < 0.08:
                k -= 8
            im.putpixel((x, y), shade(BLOCK, k))
    for y in range(16):
        course = y // 4
        for x in range(16):
            if y % 4 == 3 or (x + 4 * (course % 2)) % 8 == 0:
                im.putpixel((x, y), shade(BLOCK, -19 + rnd.randint(-2, 2)))
    return im


BLOCKS = [
    ('asphalt', 'Asphalt'),
    ('asphalt_patched', 'Patched Asphalt'),
    ('road_line', 'Road Line'),
    ('sidewalk', 'Sidewalk'),
    ('sidewalk_cracked', 'Cracked Sidewalk'),
    ('kerb', 'Kerb'),
    ('clapboard', 'Clapboard'),
    ('clapboard_painted', 'Painted Clapboard'),
    ('clapboard_weathered', 'Weathered Clapboard'),
    ('cinder_block', 'Painted Block'),
]


def textures():
    out = {
        'asphalt': asphalt(377), 'asphalt_patched': patched(3771), 'road_line': road_line(3772), 'sidewalk': sidewalk(3773),
        'kerb': kerb(3775), 'clapboard': clapboard(3776, CREAM), 'clapboard_painted': clapboard(3777, SEA), 'clapboard_painted_1': chipped(clapboard(3777, SEA), 3787, SEA),
        'cinder_block': cinder(3779),
    }
    for k in range(CRACKS):
        out['sidewalk_cracked' + (f'_{k}' if k else '')] = cracked(3774 + 10 * k, k)
    for k in range(WEATHERED):
        out['clapboard_weathered' + (f'_{k}' if k else '')] = clapboard(3778 + 10 * k, FADED, worn=True)
    return out


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/proofrock_{name}'
        out[f'models/block/proofrock_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        if name == 'road_line':
            # The line runs down the street: as drawn for a street running north to south, turned a quarter for east to west.
            out[f'blockstates/proofrock_{name}.json'] = {'variants': {
                'axis=x': {'model': model, 'y': 90}, 'axis=y': {'model': model}, 'axis=z': {'model': model}}}
            continue
        variant = {'model': model}
        if name == 'sidewalk_cracked':
            # Each cracked slab picks one of the four breaks by where it lies.
            for k in range(1, CRACKS):
                out[f'models/block/proofrock_{name}_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_{k}'}}
            variant = [{'model': model}] + [{'model': model + f'_{k}'} for k in range(1, CRACKS)]
        elif name == 'clapboard_painted':
            # Now and then a board has lost a flake of its paint.
            out[f'models/block/proofrock_{name}_1.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_1'}}
            variant = [{'model': model, 'weight': 3}, {'model': model + '_1'}]
        elif name == 'clapboard_weathered':
            # Three walls' worth of wear, picked by where the board stands, so no two neighbours lose their paint alike.
            for k in range(1, WEATHERED):
                out[f'models/block/proofrock_{name}_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_{k}'}}
            variant = [{'model': model}] + [{'model': model + f'_{k}'} for k in range(1, WEATHERED)]
        elif name in ('asphalt', 'asphalt_patched', 'sidewalk'):
            # The road and the slabs turn with where they lie, so a long street does not show its repeat.
            variant = [{'model': model}, {'model': model, 'y': 90}, {'model': model, 'y': 180}, {'model': model, 'y': 270}]
        out[f'blockstates/proofrock_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/proofrock' / f'{key}.png'
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
        key = f'block.the_oldest_house.proofrock_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' Proofrock: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
