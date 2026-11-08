"""The child's room and the hotel's boiler room (0.4.380, docs/TEXTURE_PASS_ROOMS_0_4_380.md): the texture pass's fourth interior release.

Draws, in native Minecraft pixels from fixed seeds, what was still vanilla in two rooms built whole. The child's room: honey floorboards,
cream beadboard to the rail, the rail itself, a plaster ceiling (some of it with the pale stars a child sticks up there), the corner
posts, and the cellar's whitewashed brick and cracked floor. The boiler room: soot-dark brick, stained concrete, the underside of
the porch floor on its joists, and the service stair's worn stone. Writes the models and block states (the stair's in full) and the
blocks' names. --check verifies everything written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/room_shell/'

HONEY = '#b88a58'
CREAM = '#e6dcc4'
WHITE = '#eee8d8'
PLASTER = '#ebe7dc'
STAR = '#e9ebb4'
OLD_BRICK = '#7c4c3c'
OLD_MORTAR = '#9b9384'
WASH = '#d8d4c6'
CELLAR = '#8b8880'
SOOT_BRICK = '#6a3c2e'
SOOT_MORTAR = '#56514a'
CONCRETE = '#807d75'
OIL = '#3d3a34'
JOIST = '#4e3a28'
BOARD = '#7a5c3e'
STEP = '#8e8d87'

CEILINGS = 2
FLOORS = 3


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(a, b, t):
    a, b = (rgb(a) if isinstance(a, str) else a[:3]), (rgb(b) if isinstance(b, str) else b[:3])
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def floorboards(seed):
    """Honey pine boards, four a block, the end joints staggered, a nail by each, the grain drawn along them in faint runs."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    joint = [rnd.randrange(2, 14) for _ in range(4)]
    tone = [rnd.randint(-6, 6) for _ in range(4)]
    grain = [[rnd.choice((0, 0, 0, -4, 3)) for _ in range(16)] for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            run = grain[b][(y // 3 + col * 5) % 16] if col in (1, 2) else 0
            k = tone[b] + rnd.randint(-2, 2) + run + (-14 if col == 3 else 5 if col == 0 else 0)
            if y == joint[b]:
                k -= 16
            im.putpixel((x, y), shade(HONEY, k))
    for b in range(4):
        y = (joint[b] + 2) % 16
        im.putpixel((b * 4 + 1, y), shade(HONEY, -20))
    return im


def beadboard(seed):
    """Cream beadboard: narrow tongue-and-groove boards stood upright, a bead down each joint, lit on one side."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        col = x % 4
        for y in range(16):
            k = rnd.randint(-2, 2) + (-16 if col == 0 else 7 if col == 1 else 0)
            im.putpixel((x, y), shade(CREAM, k))
    return im


def rail(seed):
    """The rail: a painted moulding along the top of the panelling, lit along its round and shadowed under its lip."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    profile = [6, 10, 12, 8, 0, -4, -2, 4, 2, 0, -6, -16, -10, -2, 0, 2]
    for y in range(16):
        for x in range(16):
            k = profile[y] + rnd.randint(-2, 2)
            im.putpixel((x, y), shade(WHITE, k) if y < 11 else shade(CREAM, k))
    return im


def ceiling(seed, stars=False):
    """Plaster ceiling, soft and pale, the trowel's marks barely there. Stars: three or four of the pale stars a child sticks up."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cloud = [[rnd.randint(-2, 2) for _ in range(4)] for _ in range(4)]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(PLASTER, cloud[y // 4][x // 4] + rnd.randint(-2, 1)))
    if stars:
        spots = [(3, 3), (11, 5), (6, 11), (13, 12)]
        for cx, cy in spots[:rnd.randint(3, 4)]:
            cx += rnd.randint(-1, 1); cy += rnd.randint(-1, 1)
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                im.putpixel((cx + dx, cy + dy), shade(STAR, -6 if (dx or dy) else 6))
    return im


def post(seed):
    """The corner post: a square post painted cream, a bead run down each arris."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for x in range(16):
        k0 = 10 if x in (1, 14) else -14 if x in (0, 15) else -6 if x in (2, 13) else 0
        for y in range(16):
            im.putpixel((x, y), shade(CREAM, k0 + rnd.randint(-2, 2) - 4))
    return im


def brick(seed, base, mortar, soot=False, wash=False):
    """Brick in stretcher bond, four courses a block. Soot: blackened in patches by the boiler's smoke. Wash: whitewashed long ago,
    the wash still whole on some bricks and the joints, flaked off others down to the red."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones, washed, sooted = {}, {}, {}
    for y in range(16):
        course = y // 4
        for x in range(16):
            key = (course, ((x + 4 * (course % 2)) // 8) % 2)
            if key not in tones:
                tones[key] = rnd.randint(-8, 8)
                washed[key] = rnd.random() < .55
                sooted[key] = rnd.random() * .45
            joint = y % 4 == 3 or (x + 4 * (course % 2)) % 8 == 0
            c = shade(mortar, rnd.randint(-3, 3)) if joint else shade(base, tones[key] + rnd.randint(-2, 2) + (4 if y % 4 == 0 else 0))
            if soot:
                c = mix(c, '#1e1a17', .2 + sooted[key] + rnd.random() * .06)
            if wash and (joint or washed[key]) and rnd.random() < .9:
                c = mix(c, WASH, .75 + rnd.random() * .15)
            im.putpixel((x, y), c)
    return im


def cellar_floor(seed):
    """The cellar floor: old poured concrete, dusty, a crack wandering across it."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(CELLAR, rnd.randint(-4, 4)))
    x, y = rnd.randrange(1, 6), rnd.randrange(4, 12)
    end = x + rnd.randrange(6, 10)
    while x < end:
        im.putpixel((x, y), shade(CELLAR, -30))
        if y + 1 < 16:
            im.putpixel((x, y + 1), shade(CELLAR, 6))
        x += 1; y = max(1, min(14, y + rnd.choice((0, 0, 1, -1))))
    return im


def boiler_floor(seed, k=0):
    """Concrete under the boiler: trowelled smooth once and worn. The second pattern has an oil stain, the third a drain grate."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(CONCRETE, rnd.randint(-4, 4)))
    cx, cy = rnd.randrange(4, 12), rnd.randrange(4, 12)
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2 * 1.4) ** .5 + rnd.random() * .8
            if k == 1 and d < 4.6:
                im.putpixel((x, y), mix(im.getpixel((x, y)), OIL, .6 - d * .11))
    if k == 2:
        for y in range(5, 11):
            for x in range(5, 11):
                edge = x in (5, 10) or y in (5, 10)
                im.putpixel((x, y), shade('#4a4a46', 8 if edge else (-18 if (x + y) % 2 else -4)))
    return im


def joists(seed):
    """The underside of the porch floor: boards run one way, a dark joist across them every half block, lit along its foot."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (-10 if x % 4 == 3 else 0)
            im.putpixel((x, y), shade(BOARD, k))
    for y0 in (2, 10):
        for x in range(16):
            for dy, k in ((0, -6), (1, 0), (2, 0), (3, 10)):
                im.putpixel((x, y0 + dy), shade(JOIST, k + rnd.randint(-2, 2)))
    return im


def step(seed, worn=False):
    """The service stair's stone: coursed blocks with a tooled face. Worn (the tread): hollowed pale in the middle."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            k = rnd.randint(-3, 3) + (6 if y % 8 == 0 else -2 if y % 8 == 6 else 0)
            if y % 8 == 7 or (x == (5 if y < 8 else 11)):
                k = -22 + rnd.randint(-2, 2)
            if worn:
                d = abs(x - 7.5) / 7.5 + abs(y - 9) / 9
                k += max(0, int(16 * (1 - d)))
            im.putpixel((x, y), shade(STEP, k))
    return im


BLOCKS = [
    ('nursery_floor', 'Nursery Floorboards'),
    ('nursery_panel', 'Nursery Panelling'),
    ('nursery_rail', 'Nursery Rail'),
    ('nursery_ceiling', 'Nursery Ceiling'),
    ('nursery_post', 'Nursery Corner Post'),
    ('cellar_brick', 'Cellar Brick'),
    ('cellar_floor', 'Cellar Floor'),
    ('boiler_brick', 'Boiler Room Brick'),
    ('boiler_floor', 'Boiler Room Floor'),
    ('boiler_joists', 'Porch Floor Joists'),
    ('boiler_stone', 'Service Stair Stone'),
    ('boiler_stair', 'Service Stair'),
]


def textures():
    out = {'nursery_floor': floorboards(3801), 'nursery_panel': beadboard(3802), 'nursery_rail': rail(3803),
           'nursery_post': post(3805), 'cellar_brick': brick(3806, OLD_BRICK, OLD_MORTAR, wash=True), 'cellar_floor': cellar_floor(3807),
           'boiler_brick': brick(3808, SOOT_BRICK, SOOT_MORTAR, soot=True), 'boiler_joists': joists(3810),
           'boiler_stone': step(3811), 'boiler_stair': step(3811, worn=True)}
    for k in range(CEILINGS):
        out['nursery_ceiling' + (f'_{k}' if k else '')] = ceiling(3804 + 10 * k, stars=k == 1)
    for k in range(FLOORS):
        out['boiler_floor' + (f'_{k}' if k else '')] = boiler_floor(3809 + 10 * k, k)
    return out


def stair_states(model):
    """The stair's block states, laid out as vanilla's own stairs are: every facing, half and shape, turned and lit alike."""
    rot = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
    out = {}
    for facing, y0 in rot.items():
        for half in ('bottom', 'top'):
            for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
                kind = '' if shape == 'straight' else '_inner' if shape.startswith('inner') else '_outer'
                y = y0
                if half == 'bottom' and shape.endswith('left'):
                    y = y0 - 90
                if half == 'top' and shape.endswith('right'):
                    y = y0 + 90
                v = {'model': model + kind}
                if half == 'top':
                    v['x'] = 180
                y %= 360
                if y:
                    v['y'] = y
                if half == 'top' or y:
                    v['uvlock'] = True
                out[f'facing={facing},half={half},shape={shape}'] = v
    return out


def cube(name):
    return {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/room_shell_{name}'
        variant = {'model': model}
        if name == 'boiler_stair':
            tex = {'bottom': P + 'boiler_stone', 'top': P + 'boiler_stair', 'side': P + 'boiler_stone'}
            out[f'models/block/room_shell_{name}.json'] = {'parent': 'minecraft:block/stairs', 'textures': tex}
            out[f'models/block/room_shell_{name}_inner.json'] = {'parent': 'minecraft:block/inner_stairs', 'textures': tex}
            out[f'models/block/room_shell_{name}_outer.json'] = {'parent': 'minecraft:block/outer_stairs', 'textures': tex}
            out[f'blockstates/room_shell_{name}.json'] = {'variants': stair_states(model)}
            continue
        if name == 'boiler_joists':
            # The ceiling is the porch's floor too: plain spruce from above, the joists from the boiler room below.
            out[f'models/block/room_shell_{name}.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
                'top': 'minecraft:block/spruce_planks', 'side': 'minecraft:block/spruce_planks', 'bottom': P + name}}
        else:
            out[f'models/block/room_shell_{name}.json'] = cube(name)
        if name == 'nursery_ceiling':
            out[f'models/block/room_shell_{name}_1.json'] = cube(name + '_1')
            variant = [{'model': model, 'weight': 3}, {'model': model + '_1'}]
        elif name == 'boiler_floor':
            for k in range(1, FLOORS):
                out[f'models/block/room_shell_{name}_{k}.json'] = cube(name + f'_{k}')
            variant = [{'model': model, 'weight': 6}, {'model': model + '_1', 'weight': 2}, {'model': model + '_2'}]
        elif name in ('nursery_floor', 'cellar_floor'):
            variant = [{'model': model}, {'model': model, 'y': 180}]
        out[f'blockstates/room_shell_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/room_shell' / f'{key}.png'
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
        key = f'block.the_oldest_house.room_shell_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the child\'s room and the boiler room: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
