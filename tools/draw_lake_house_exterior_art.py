"""The lake house's outside (0.4.379, docs/TEXTURE_PASS_LAKE_HOUSE_0_4_379.md): the texture pass's third interior release.

The lake house's rooms already had their own surfaces (lake_house_floor, _wall, _cedar, _pool_stone). Draws, in native Minecraft
pixels from fixed seeds, what was still vanilla round them: the drowned house's brick with silt in its joints and a film of
algae (two patterns, picked by position), its silted foundation stone, the boathouse's weathered board-and-batten, the dock's
bleached boards, and the culvert's board-formed concrete and the same stained where the water stands. Writes the models and block
states, and the blocks' names. --check verifies everything written.

0.4.560 (docs/CULVERT_0_4_560.md): the culvert in three pours picked by position, so its tie holes no longer repeat block after
block; the joints between its cast sections, a seam crusted with lime; the coping on its headwalls; and what is painted on its
walls, at the height of a reader sitting in a canoe: arrows the way on, a count, hand stencils, the house under its waterline, a
helmet, a half door, a spiral and bubbles. No words and nobody's name.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/lake_house/'

BRICK = '#6d4b3e'
SILT = '#857b62'
ALGAE = '#4f5e36'
STONE = '#6a6c66'
SIDING = '#7b7166'
DOCK = '#8e897d'
CONCRETE = '#9b9a93'
STAIN = '#2e3a2c'

BRICKS = 2


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(a, b, t):
    a, b = (rgb(a) if isinstance(a, str) else a[:3]), (rgb(b) if isinstance(b, str) else b[:3])
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def drowned_brick(seed, k=0):
    """Brick in stretcher bond, four courses a block, gone dark under the lake: silt lying in the joints, a green film of algae over
    the top of each brick where it settles. The second pattern is further gone, the film thicker and a brick's face spalled away."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tones = {}
    film = 0.35 if k == 0 else 0.6
    for y in range(16):
        course = y // 4
        for x in range(16):
            key = (course, ((x + 4 * (course % 2)) // 8) % 2)
            if key not in tones:
                tones[key] = rnd.randint(-8, 8)
            joint = y % 4 == 3 or (x + 4 * (course % 2)) % 8 == 0
            if joint:
                im.putpixel((x, y), shade(SILT, rnd.randint(-6, 4)))
                continue
            c = shade(BRICK, tones[key] + rnd.randint(-3, 3))
            if y % 4 == 0 and rnd.random() < film + .3:
                c = mix(c, ALGAE, .55 + rnd.random() * .2)
            elif rnd.random() < film * .25:
                c = mix(c, ALGAE, .35)
            im.putpixel((x, y), c)
    if k == 1:
        cx, cy = rnd.randrange(2, 10), rnd.randrange(0, 3) * 4 + 1
        for dx in range(4):
            for dy in range(2):
                im.putpixel((cx + dx, cy + dy), shade(BRICK, -22 + rnd.randint(-3, 3)))
    return im


def silt_stone(seed):
    """The foundation: two courses of grey stone under a blanket of silt, thickest along the top of each course, the joints filled."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    cuts = [rnd.randrange(5, 11), rnd.randrange(3, 13)]
    for y in range(16):
        course, row = y // 8, y % 8
        for x in range(16):
            c = shade(STONE, rnd.randint(-4, 4) + (-3 if x < cuts[course] else 3))
            c = mix(c, SILT, max(0, .55 - row * .08) + rnd.random() * .15)
            if row == 7 or x == cuts[course]:
                c = shade(SILT, -14 + rnd.randint(-3, 3))
            im.putpixel((x, y), c)
    return im


def siding(seed):
    """Board-and-batten gone silver: wide boards with their grain run up them, a narrow batten over each joint, lit down one
    edge and shadowed down the other, a nail head or two."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    grain = [rnd.randint(-5, 5) for _ in range(16)]
    for x in range(16):
        col = x % 8
        for y in range(16):
            if col == 0:
                k = 9 + rnd.randint(-2, 2)
            elif col == 1:
                k = -4 + rnd.randint(-2, 2)
            elif col == 2:
                k = -14 + rnd.randint(-2, 2)
            else:
                k = grain[x] + rnd.randint(-2, 2) + (y * 3 + x) % 7 // 3
            im.putpixel((x, y), shade(SIDING, k))
    for x, y in ((0, 3), (8, 11), (1, 12), (9, 4)):
        im.putpixel((x, y), shade(SIDING, -22))
    return im


def dock(seed):
    """Dock boards bleached grey by the lake, four a block with a dark gap between them, the grain open and a nail at each end."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    tone = [rnd.randint(-5, 5) for _ in range(4)]
    for x in range(16):
        b, col = x // 4, x % 4
        for y in range(16):
            k = tone[b] + rnd.randint(-3, 3) + ((y + b * 5) % 5 == 0) * -5
            if col == 3:
                k = -34 + rnd.randint(-3, 3)
            elif col == 0:
                k += 5
            im.putpixel((x, y), shade(DOCK, k))
    for b in range(4):
        for y in (1, 14):
            im.putpixel((b * 4 + 1, y), shade(DOCK, -24))
    return im


# Where the form ties went through, one set per pour; the third pour has a patched void instead of a second tie.
TIES = [((4, 5), (12, 9)), ((2, 10), (10, 2)), ((8, 6),)]
POURS = 3


def culvert(seed, stained=False, k=0):
    """Board-formed concrete: the faint lines of the shuttering boards across it every four pixels, two tie holes. Stained: dark
    and green where the water stands, darkest at the foot, a slime run down from the top. Three pours (0.4.560): the tie holes
    elsewhere, and in the third a patched void, smoother and paler than what is round it."""
    rnd = random.Random(seed + 7 * k)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            tone = rnd.randint(-3, 3) + (-6 if y % 4 == 3 else 2 if y % 4 == 0 else 0)
            c = shade(CONCRETE, tone)
            if stained:
                c = mix(c, STAIN, min(.85, .25 + y * .04 + rnd.random() * .1))
            im.putpixel((x, y), c)
    if k == 2:
        for x in range(9, 13):
            for y in range(10, 13):
                im.putpixel((x, y), mix(im.getpixel((x, y)), shade(CONCRETE, 9), .7))
    for x, y in TIES[k]:
        im.putpixel((x, y), shade(CONCRETE, -40))
        im.putpixel((x + 1, y), shade(CONCRETE, -22))
    if stained:
        for x in (rnd.randrange(1, 7), rnd.randrange(9, 15)):
            for y in range(0, rnd.randrange(5, 10)):
                im.putpixel((x, y), mix(im.getpixel((x, y)), ALGAE, .6))
    return im


LIME = '#dcdbd0'


def joint(base, seed):
    """Where two cast sections of the tube meet: a dark seam down the middle, the sealant long gone, and lime crusted white
    either side of it where water from the drive above has come through. On the ceiling the same face is turned across the
    tube, so the seam runs from wall to wall."""
    rnd = random.Random(seed)
    im = base.copy()
    for y in range(16):
        im.putpixel((7, y), shade(im.getpixel((7, y)), -46))
        im.putpixel((8, y), shade(im.getpixel((8, y)), -24))
        im.putpixel((6, y), shade(im.getpixel((6, y)), 6))
        for x, odds, t in ((9, .55, .75), (6, .35, .6), (10, .2, .55), (5, .1, .5)):
            if rnd.random() < odds:
                im.putpixel((x, y), mix(im.getpixel((x, y)), LIME, t))
    return im


# The paints, long faded into the concrete.
PAINTS = {'w': ('#dddbd2', .72), 'k': ('#222326', .68), 'r': ('#7d342c', .66), 't': ('#2f5f66', .7)}
# Each mark as it is painted, sixteen pixels a side; the house is drawn across two blocks.
MARKS = {
    'arrow_right': [
        '................',
        '................',
        '................',
        '................',
        '.........w......',
        '.........ww.....',
        '.........www....',
        '.wwwwwwwwwwww...',
        '.wwwwwwwwwwww...',
        '.........www....',
        '.........ww.....',
        '.........w......',
        '................',
        '................',
        '................',
        '................'],
    'tally': [
        '................',
        '................',
        '................',
        '................',
        '.k.k.k.kk.k.k.k.',
        '.k.k.k.k..k.k.k.',
        '.k.k.kkk..k.k.k.',
        '.k.k.k.k..k.k.k.',
        '.k.k.k.k..k.k.k.',
        '.k.kkk.k..k.k.k.',
        '.kkk.k.k..k.k.k.',
        'kk.k.k.k..k.k.k.',
        '................',
        '................',
        '................',
        '................'],
    'house': [
        '................................',
        '..t...t...t...t...t...t...t...t.',
        '.t.t.t.t.t.t.t.t.t.t.t.t.t.t.t.t',
        't...t...t...t...t...t...t...t...',
        '...............ww...............',
        '.............ww..ww.............',
        '...........ww......ww...........',
        '.........ww..........ww.........',
        '.......ww..............ww.......',
        '.....wwwwwwwwwwwwwwwwwwwwww.....',
        '.......w..............w.........',
        '.......w..www.....wwww.w........',
        '.......w..w.w.....w..w.w........',
        '.......w..w.w.....wwww.w........',
        '.......w..w.w..........w........',
        '......wwwwwwwwwwwwwwwwwww.......'],
    'helmet': [
        '................',
        '......kkkk......',
        '.....k....k.....',
        '....k.wwww.k....',
        '....k.w..w.k....',
        '....k.wwww.k....',
        '.....k....k.....',
        '......kkkk......',
        '....kkkkkkkk....',
        '...k.kkkkkk.k...',
        '...k.kkkkkk.k...',
        '.....kkkkkk.....',
        '.....kk..kk.....',
        '.....kk..kk.....',
        '....kkk..kkk....',
        '................'],
    'half_door': [
        '................',
        '................',
        '....wwwwwww.....',
        '....wkkkkkw.....',
        '....wkkkkkw.....',
        '....wkkkkkw.....',
        '....wkkkkkw.....',
        '....wkkkkkw.....',
        '....wwwwwww.....',
        '....w.....w.....',
        '....w.....w.....',
        '....w...w.w.....',
        '....w.....w.....',
        '....w.....w.....',
        '....wwwwwww.....',
        '................'],
    'spiral': [
        '................',
        '................',
        '..rrrrrrrrrrr...',
        '..r.........r...',
        '..r.rrrrrrr.r...',
        '..r.r.....r.r...',
        '..r.r.rrr.r.r...',
        '..r.r.r.r.r.r...',
        '..r.r.r...r.r...',
        '..r.r.rrrrr.r...',
        '..r.r.......r...',
        '..r.rrrrrrrrr...',
        '..r.............',
        '..r.............',
        '................',
        '................'],
    'bubbles': [
        '................',
        '...........ttt..',
        '..........t...t.',
        '..........t...t.',
        '..........t...t.',
        '...........ttt..',
        '......tt........',
        '.....t..t.......',
        '.....t..t.......',
        '......tt........',
        '..........w.....',
        '.........w.w....',
        '..........w.....',
        '.....t..........',
        '....t.t.........',
        '.....t..........'],
}
# A hand pressed flat to the wall with paint blown round it: the hand itself is left bare.
HAND = [
    '................',
    '.......#........',
    '.....#.#.#......',
    '.....#.#.#.#....',
    '.....#.#.#.#....',
    '.....#.#.#.#....',
    '.....#######....',
    '..#..#######....',
    '..##.#######....',
    '...#########....',
    '....########....',
    '.....#######....',
    '.....#######....',
    '......#####.....',
    '................',
    '................']
MARK_NAMES = ['arrow_left', 'arrow_right', 'tally', 'hand', 'house_left', 'house_right', 'helmet', 'half_door', 'spiral', 'bubbles']


def paint(base, rows, seed):
    """Paint laid onto the concrete, faded unevenly, with a little overspray round its edges."""
    rnd = random.Random(seed)
    im = base.copy()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch not in PAINTS:
                continue
            colour, strength = PAINTS[ch]
            im.putpixel((x, y), mix(im.getpixel((x, y)), colour, strength * (.78 + rnd.random() * .22)))
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and rows[ny][nx] == '.' and rnd.random() < .12:
                    im.putpixel((nx, ny), mix(im.getpixel((nx, ny)), colour, strength * .3))
    return im


def stencil(base, seed):
    """A hand stencil: paint blown round a hand held to the wall, thick against its outline and thinning away from it."""
    rnd = random.Random(seed)
    im = base.copy()
    colour, strength = PAINTS['k']
    hand = {(x, y) for y, row in enumerate(HAND) for x, ch in enumerate(row) if ch == '#'}
    for y in range(16):
        for x in range(16):
            if (x, y) in hand:
                continue
            d = min((max(abs(x - hx), abs(y - hy)) for hx, hy in hand), default=9)
            if d <= 3 and rnd.random() < (1.0, .8, .35)[d - 1]:
                im.putpixel((x, y), mix(im.getpixel((x, y)), colour, strength * (1.0, .7, .4)[d - 1] * (.8 + rnd.random() * .2)))
    return im


def marks():
    out = {}
    for i, name in enumerate(MARK_NAMES):
        base = culvert(3794, k=i % POURS)
        if name == 'hand':
            out[name] = stencil(base, 3810 + i)
            continue
        if name == 'arrow_left':
            rows = [row[::-1] for row in MARKS['arrow_right']]
        elif name in ('house_left', 'house_right'):
            rows = [row[:16] if name == 'house_left' else row[16:] for row in MARKS['house']]
        else:
            rows = MARKS[name]
        out[name] = paint(base, rows, 3810 + i)
    return out


def coping_top(seed):
    """The top of the headwalls' coping: smoother than the walls, weathered pale, moss in the cracks along its edges."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        for x in range(16):
            c = shade(CONCRETE, 6 + rnd.randint(-3, 3))
            if (y in (0, 15) or x in (0, 15)) and rnd.random() < .3:
                c = mix(c, ALGAE, .45)
            im.putpixel((x, y), c)
    for x in range(3, 13):
        if rnd.random() < .5:
            im.putpixel((x, 7 + (x % 3 == 0)), shade(CONCRETE, -16))
    return im


BLOCKS = [
    ('drowned_brick', 'Drowned Brick'),
    ('silt_stone', 'Silted Stone'),
    ('siding', 'Weathered Siding'),
    ('dock', 'Dock Boards'),
    ('culvert', 'Culvert Concrete'),
    ('culvert_stained', 'Stained Culvert Concrete'),
    ('culvert_mark', 'Marked Culvert Concrete'),
    ('culvert_coping', 'Culvert Coping'),
]


def textures():
    out = {'silt_stone': silt_stone(3791), 'siding': siding(3792), 'dock': dock(3793)}
    for k in range(POURS):
        out['culvert' + (f'_{k}' if k else '')] = culvert(3794, k=k)
        out['culvert_stained' + (f'_{k}' if k else '')] = culvert(3794, stained=True, k=k)
    out['culvert_joint'] = joint(out['culvert'], 3795)
    out['culvert_stained_joint'] = joint(out['culvert_stained'], 3796)
    out['culvert_coping_top'] = coping_top(3797)
    for name, im in marks().items():
        out['culvert_mark_' + name] = im
    for k in range(BRICKS):
        out['drowned_brick' + (f'_{k}' if k else '')] = drowned_brick(3790 + 10 * k, k)
    return out


def joint_model(texture):
    """One cube: the seam upright on the walls' faces, and turned across the tube on the ceiling's."""
    faces = {d: {'texture': '#joint', 'cullface': d} for d in ('north', 'south', 'east', 'west')}
    for d in ('up', 'down'):
        faces[d] = {'texture': '#joint', 'cullface': d, 'rotation': 90}
    return {'parent': 'minecraft:block/block', 'textures': {'particle': P + texture, 'joint': P + texture},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': faces}]}


def files():
    out = {}
    for name, _ in BLOCKS:
        model = NS + f'block/lake_house_{name}'
        out[f'models/block/lake_house_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        variant = {'model': model}
        if name == 'drowned_brick':
            # Picked by where it stands: most of the brick has a light film, some is further gone.
            out[f'models/block/lake_house_{name}_1.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_1'}}
            variant = [{'model': model, 'weight': 2}, {'model': model + '_1'}]
        elif name == 'dock':
            variant = [{'model': model}, {'model': model, 'y': 180}]
        elif name in ('culvert', 'culvert_stained'):
            # Three pours picked by position, and the joint between two sections.
            for k in range(1, POURS):
                out[f'models/block/lake_house_{name}_{k}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + f'{name}_{k}'}}
            out[f'models/block/lake_house_{name}_joint.json'] = joint_model(f'{name}_joint')
            out[f'blockstates/lake_house_{name}.json'] = {'variants': {
                'joint=false': [{'model': model, 'weight': 2}] + [{'model': model + f'_{k}'} for k in range(1, POURS)],
                'joint=true': {'model': model + '_joint'}}}
            continue
        elif name == 'culvert_mark':
            del out[f'models/block/lake_house_{name}.json']
            variants = {}
            for mark in MARK_NAMES:
                out[f'models/block/lake_house_culvert_mark_{mark}.json'] = {'parent': 'minecraft:block/orientable', 'textures': {
                    'front': P + f'culvert_mark_{mark}', 'side': P + 'culvert', 'top': P + 'culvert', 'particle': P + 'culvert'}}
                for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
                    variants[f'facing={facing},mark={mark}'] = {'model': NS + f'block/lake_house_culvert_mark_{mark}', **({'y': y} if y else {})}
            out[f'blockstates/lake_house_{name}.json'] = {'variants': variants}
            continue
        elif name == 'culvert_coping':
            textures = {'bottom': P + 'culvert', 'top': P + 'culvert_coping_top', 'side': P + 'culvert'}
            out[f'models/block/lake_house_{name}.json'] = {'parent': 'minecraft:block/slab', 'textures': textures}
            out[f'models/block/lake_house_{name}_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': textures}
            out[f'models/block/lake_house_{name}_double.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': textures}
            out[f'blockstates/lake_house_{name}.json'] = {'variants': {'type=bottom': {'model': model}, 'type=top': {'model': model + '_top'},
                                                                      'type=double': {'model': model + '_double'}}}
            continue
        out[f'blockstates/lake_house_{name}.json'] = {'variants': {'': variant}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key, im in images.items():
        path = A / 'textures/block/lake_house' / f'{key}.png'
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
        key = f'block.the_oldest_house.lake_house_{name}'
        if check:
            assert lang.get(key) == title, key
        else:
            lang[key] = title
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the lake house\'s outside: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
