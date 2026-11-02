"""After the People Lights Have Gone Off (0.4.253): the house Stan built for his daughter and her husband, at night.

Draws, in native Minecraft pixels from fixed seeds: ten materials (the foyer's large tiles, the kitchen's and bathroom's smaller ones,
the living room's oak boards, the bedroom's and study's carpet, the plaster, the high-gloss trim, the lap siding, the asphalt
shingles, the garage's concrete and the driveway), and every fitting of the people_fitting block, its models facing north and turned
by its facing: the wheelchair (facing the way its sitter would, footrests to the front, the seat's top at eight pixels); the newel
post; the bathroom mirror hung where Stan put it and lowered where she can see herself; Mark's work case; the photograph in the
foyer; the loft's rail; the old air mattress; Stan's folded offer; the wall phone; the cards, squared and dealt; the grab rail; and
the hall runner laid and rolled up. Writes the models and block states, the carried things' item models (the case and the folded
spare chair), and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/people/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(16):
            for x in range(16):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, 0, a))


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# ------------------------------------------------------------------------------------------------ materials
def tiles(size, base, grout, seed, spread=3):
    # Square tiles with a grout line; each tile its own slight shade, as fired tiles are.
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(seed)
    tone = {(tx, ty): rnd.randint(-6, 6) for tx in range(16 // size) for ty in range(16 // size)}
    for y in range(16):
        for x in range(16):
            if x % size == size - 1 or y % size == size - 1:
                px[x, y] = shade(grout, rnd.randint(-2, 2))
            else:
                px[x, y] = shade(base, tone[(x // size, y // size)] + rnd.randint(-spread, spread))
    return im


def boards():
    base = '#b88a54'
    im = new(base=base, seed=2530, spread=4); rnd = random.Random(2531)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -38)[:3])
    for row, x in enumerate((5, 12, 2, 9)):
        for dy in range(3):
            dot(im, x, row * 4 + dy, shade(base, -30)[:3])
    for _ in range(10):
        y = rnd.randrange(16)
        if y % 4 != 3:
            x = rnd.randrange(14)
            dot(im, x, y, shade(base, -14)[:3]); dot(im, x + 1, y, shade(base, -14)[:3])
    return im


def carpet():
    base = '#8a8478'
    im = new(base=base, seed=2532, spread=5); rnd = random.Random(2533)
    for _ in range(30):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade(base, rnd.choice([-12, 9]))[:3])
    return im


def siding():
    # Lap siding: each course shades from its lit top edge to the shadow under the next.
    base = '#9aa2a6'
    im = new(base=base, seed=2534, spread=2)
    for course in range(4):
        y0 = course * 4
        for x in range(16):
            dot(im, x, y0, shade(base, 10)[:3]); dot(im, x, y0 + 3, shade(base, -30)[:3]); dot(im, x, y0 + 2, shade(base, -10)[:3])
    for x, course in ((4, 0), (13, 1), (8, 2), (1, 3)):
        for dy in range(3):
            dot(im, x, course * 4 + dy, shade(base, -18)[:3])
    return im


def shingle():
    base = '#3e3a3a'
    im = new(base=base, seed=2535, spread=6)
    for row in range(4):
        y = row * 4 + 3; off = 0 if row % 2 == 0 else 4
        for x in range(16):
            dot(im, x, y, '#242122')
        for x in range(off, 16, 8):
            for dy in range(3):
                dot(im, x, row * 4 + dy, '#2a2627')
    return im


def concrete(base, seed, cracks):
    im = new(base=base, seed=seed, spread=5); rnd = random.Random(seed + 1)
    for _ in range(18):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade(base, rnd.choice([-14, 10]))[:3])
    if cracks:
        x, y = 3, 0
        while y < 16:
            dot(im, x, y, shade(base, -34)[:3]); y += 1; x += rnd.choice([-1, 0, 0, 1])
    return im


def materials():
    m = {}
    m['tile_large'] = tiles(8, '#c9a882', '#8a7a68', 2536)
    m['tile_small'] = tiles(4, '#e4e2da', '#a8a69e', 2537, 2)
    m['boards'] = boards()
    m['carpet'] = carpet()
    m['plaster'] = new(base='#e2dcd0', seed=2538, spread=2)
    trim = new(base='#f2f0ea', seed=2539, spread=1)
    for x in range(16):
        dot(trim, x, 2, '#ffffff'); dot(trim, x, 13, '#cfccc4'); dot(trim, x, 14, '#c4c0b8')
    m['trim'] = trim
    m['siding'] = siding()
    m['shingle'] = shingle()
    m['concrete'] = concrete('#8e8c88', 2540, False)
    m['drive'] = concrete('#5c5a58', 2542, True)
    return m


# ------------------------------------------------------------------------------------------------ fittings
def wheel_face():
    # The big wheel seen from the side: a black tyre, the push rim inside it, spokes to the hub; transparent outside the circle.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    c = 7.5
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - c, y - c)
            if r > 7.6:
                continue
            if r > 6.2:
                im.putpixel((x, y), shade('#1c1c1e', 0))
            elif r > 5.2:
                im.putpixel((x, y), shade('#9a9ca0', 0))
            elif r < 1.3:
                im.putpixel((x, y), shade('#5a5c60', 0))
            else:
                a = math.atan2(y - c, x - c)
                if min(abs(math.sin(a * 4)), 1) < .22:
                    im.putpixel((x, y), shade('#b4b6ba', 0))
    return im


def caster_face():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 7.6:
                im.putpixel((x, y), shade('#1c1c1e' if r > 4 else '#6a6c70', 0))
    return im


def mirror_glass():
    im = new(base='#b4c2c8', seed=2550, spread=2)
    for i in range(16):
        for d in (0, 1):
            dot(im, i + d - 4, 15 - i, '#e8f0f2')
        dot(im, i + 5, 15 - i, '#d4e0e4')
    frame(im, 0, 0, 16, 16, '#d8d4c8')
    return im


def case_side():
    im = new(base='#2a2624', seed=2551, spread=3)
    for x in range(16):
        dot(im, x, 2, '#3a3532'); dot(im, x, 13, '#1c1918')
    for x in (3, 12):
        fill(im, x, 4, x + 2, 7, '#b89850'); dot(im, x, 6, '#7a6230')
    return im


def photograph():
    # A wedding photograph: two of them on a step, a door behind, a dark frame. Pixels, not a face.
    im = new(base='#3a2c22', seed=2552, spread=2)
    fill(im, 3, 3, 13, 13, '#c8c2b0')
    fill(im, 3, 3, 13, 7, '#9aa8b0')
    fill(im, 5, 4, 8, 9, '#6a5a4a')
    fill(im, 4, 10, 12, 13, '#a8a090')
    fill(im, 6, 6, 8, 12, '#2a2a30'); dot(im, 6, 5, '#d2a888'); dot(im, 7, 5, '#d2a888'); dot(im, 6, 4, '#3a2a20'); dot(im, 7, 4, '#3a2a20')
    fill(im, 9, 7, 12, 12, '#f2f0ea'); dot(im, 9, 6, '#c89878'); dot(im, 10, 6, '#c89878'); fill(im, 9, 5, 11, 6, '#6a4a30')
    return im


def mattress():
    base = '#5a6e88'
    im = new(base=base, seed=2553, spread=3)
    for x in range(1, 16, 4):
        for y in range(16):
            dot(im, x, y, shade(base, -22)[:3])
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -14)[:3])
    dot(im, 13, 2, '#d8d4c8'); dot(im, 14, 2, '#d8d4c8')
    return im


def offer():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 4, 3, 12, 13, '#f2eee2')
    for x in range(4, 12):
        dot(im, x, 8, '#c8c2b4')
    for y in (4, 6, 10):
        for x in range(5, 10 + (y % 3)):
            dot(im, x, y, '#3a3a52')
    dot(im, 10, 11, '#3a3a52'); dot(im, 9, 11, '#3a3a52')
    return im


def cards(dealt):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    back, face, red, black = '#7a2a2e', '#f4f2ec', '#b42a2a', '#1e1e22'
    if not dealt:
        fill(im, 6, 5, 11, 12, back); frame(im, 6, 5, 11, 12, '#e8e4d8')
        return im
    # Two hands face down, a discard face up, the deck between.
    for x0, y0 in ((2, 1), (4, 1), (2, 12), (4, 12)):
        fill(im, x0, y0, x0 + 3, y0 + 3, back); frame(im, x0, y0, x0 + 3, y0 + 3, '#e8e4d8')
    fill(im, 6, 6, 9, 10, back); frame(im, 6, 6, 9, 10, '#e8e4d8')
    fill(im, 10, 6, 13, 10, face); dot(im, 11, 7, red); dot(im, 11, 8, red)
    fill(im, 11, 10, 14, 14, face); dot(im, 12, 11, black)
    return im


def phone_body():
    im = new(base='#d8ccb0', seed=2554, spread=2)
    fill(im, 5, 9, 11, 14, '#c4b898')
    for y in (10, 12):
        for x in (6, 8, 10):
            dot(im, x, y, '#6a6250')
    return im


def runner(rolled):
    base, border, figure = '#6a3a3a', '#2e3a4e', '#c8a870'
    if rolled:
        im = new(base=base, seed=2556, spread=3)
        for x in range(0, 16, 4):
            for y in range(16):
                dot(im, x, y, border)
        return im
    im = new(base=base, seed=2555, spread=4)
    fill(im, 0, 0, 2, 16, border); fill(im, 14, 0, 16, 16, border)
    for y in range(16):
        dot(im, 2, y, figure); dot(im, 13, y, figure)
    for y in range(1, 16, 8):
        for d in range(4):
            dot(im, 8 - d, y + d, figure); dot(im, 7 + d, y + d, figure); dot(im, 8 - d, y + 6 - d, figure); dot(im, 7 + d, y + 6 - d, figure)
    return im


def runner_end():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 7.6:
                ring = int(r) % 2
                im.putpixel((x, y), shade('#6a3a3a' if ring else '#4a2a2a', 0))
    return im


def textures():
    out = {f'block/people_{k}.png': v for k, v in materials().items()}
    t = {
        'wheel': wheel_face(), 'caster': caster_face(), 'chrome': new(base='#a8aaae', seed=2560, spread=4),
        'tyre': new(base='#1c1c1e', seed=2561, spread=2), 'sling': new(base='#2a2c32', seed=2562, spread=3),
        'gloss': materials()['trim'], 'newel_cap': new(base='#4a3220', seed=2563, spread=3),
        'glass': mirror_glass(), 'case': case_side(), 'case_top': new(base='#2a2624', seed=2564, spread=3),
        'photo': photograph(), 'frame': new(base='#3a2c22', seed=2565, spread=2), 'mattress': mattress(),
        'offer': offer(), 'cards_0': cards(False), 'cards_1': cards(True), 'phone': phone_body(),
        'cord': new(base='#c4b898', seed=2566, spread=3), 'steel': new(base='#c4c8cc', seed=2567, spread=3),
        'runner': runner(False), 'runner_roll': runner(True), 'runner_end': runner_end(),
    }
    out.update({f'block/people/{k}.png': v for k, v in t.items()})
    return out


def element(lo, hi, textures, uv=None, rotation=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        if rotation and f in rotation:
            faces[f]['rotation'] = rotation[f]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def chair():
    # Facing north as its sitter would: the footrests to the north, the big wheels behind, the push handles at the back.
    e = []
    for x in (1.5, 14):
        e.append(element([x, 0, 4.5], [x + 0.5, 11, 15.5], {'east': 'w', 'west': 'w'}, uv={'east': [0, 0, 16, 16], 'west': [0, 0, 16, 16]}))
    for x in (4, 11):
        e.append(element([x, 0, 1], [x + 1, 3, 4], {'east': 'k', 'west': 'k'}, uv={'east': [0, 0, 16, 16], 'west': [0, 0, 16, 16]}))
        e.append(element([x, 3, 2], [x + 1, 7, 3], {'all': 'c'}))
    for x in (2.5, 12.5):
        e.append(element([x, 6, 3], [x + 1, 7, 14], {'all': 'c'}))
        e.append(element([x, 7, 13], [x + 1, 15, 14], {'all': 'c'}))
        e.append(element([x, 10, 5], [x + 1, 11, 12], {'all': 's'}))
        e.append(element([x, 7, 5], [x + 1, 10, 6], {'all': 'c'}))
        e.append(element([x, 14, 14], [x + 1, 15, 16], {'all': 't'}))
    e.append(element([3.5, 7, 4], [12.5, 8, 13], {'all': 's'}))
    e.append(element([3.5, 8, 12.5], [12.5, 14, 13.5], {'all': 's'}))
    e.append(element([4, 1, 0], [12, 2, 3], {'all': 'c', 'up': 't'}))
    return model(e, {'w': 'wheel', 'k': 'caster', 'c': 'chrome', 's': 'sling', 't': 'tyre'}, 'sling')


def fitting_models():
    m = {}
    m['chair_0'] = chair()
    m['newel_0'] = model([element([5, 0, 5], [11, 13, 11], {'all': 'g'}), element([4, 13, 4], [12, 15, 12], {'all': 'n'}), element([6, 15, 6], [10, 16, 10], {'all': 'n'})],
                         {'g': 'gloss', 'n': 'newel_cap'}, 'gloss')
    for stage, (y0, y1) in enumerate(((6, 16), (0, 10))):
        m[f'mirror_{stage}'] = model([element([3, y0, 15], [13, y1, 16], {'north': 'm', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'}, uv={'north': [3, 16 - y1, 13, 16 - y0]})],
                                     {'m': 'glass', 'f': 'frame'}, 'glass')
    m['case_0'] = model([element([3, 0, 5], [13, 6, 11], {'all': 'c', 'up': 'u', 'down': 'u'}, uv={'north': [3, 5, 13, 11], 'south': [3, 5, 13, 11]}),
                         element([6, 7, 7.5], [10, 8, 8.5], {'all': 'u'}), element([6, 6, 7.5], [7, 7, 8.5], {'all': 'u'}), element([9, 6, 7.5], [10, 7, 8.5], {'all': 'u'})],
                        {'c': 'case', 'u': 'case_top'}, 'case')
    m['photo_0'] = model([element([3, 3, 15], [13, 13, 16], {'north': 'p', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'}, uv={'north': [3, 3, 13, 13]})],
                         {'p': 'photo', 'f': 'frame'}, 'photo')
    rail = [element([0, 0, 13], [16, 1, 15], {'all': 'g'}), element([0, 14, 12], [16, 16, 16], {'all': 'g'})]
    rail += [element([x, 1, 13.5], [x + 1, 14, 14.5], {'all': 'g'}) for x in (1.5, 5.5, 9.5, 13.5)]
    m['railing_0'] = model(rail, {'g': 'gloss'}, 'gloss')
    m['mattress_0'] = model([element([0, 0, 0], [16, 2, 16], {'all': 'm'}, uv={'north': [0, 0, 16, 2], 'south': [0, 0, 16, 2], 'east': [0, 0, 16, 2], 'west': [0, 0, 16, 2]}),
                             element([1, 2, 1], [15, 2.5, 15], {'up': 'm'})], {'m': 'mattress'}, 'mattress')
    m['offer_0'] = model([element([3, 0, 3], [13, 0.25, 13], {'up': 'o', 'down': 'o'}, uv={'up': [3, 3, 13, 13], 'down': [3, 3, 13, 13]})], {'o': 'offer'}, 'offer')
    for stage in range(2):
        m[f'cards_{stage}'] = model([element([1, 0, 1], [15, 0.25 + stage * 0.25, 15], {'up': 'c'}, uv={'up': [1, 1, 15, 15]})] +
                                    ([element([6, 0.25, 5], [11, 1, 12], {'all': 'c'}, uv={'up': [6, 5, 11, 12]})] if stage == 0 else []),
                                    {'c': f'cards_{stage}'}, f'cards_{stage}')
    m['phone_0'] = model([element([5, 3, 14], [11, 12, 16], {'all': 'p'}), element([6, 4, 12.5], [10, 12, 14], {'all': 'p'}, uv={'north': [6, 2, 10, 10]}),
                          element([7.5, 0.5, 13], [8.5, 4, 14], {'all': 'c'})], {'p': 'phone', 'c': 'cord'}, 'phone')
    m['grab_rail_0'] = model([element([1, 6, 13], [15, 8, 14], {'all': 's'}), element([1, 5.5, 14], [3, 8.5, 16], {'all': 's'}), element([13, 5.5, 14], [15, 8.5, 16], {'all': 's'})],
                             {'s': 'steel'}, 'steel')
    m['runner_0'] = model([element([0, 0, 0], [16, 0.5, 16], {'up': 'r', 'down': 'r'})], {'r': 'runner'}, 'runner')
    m['runner_1'] = model([element([1, 0, 10], [15, 5, 15], {'north': 'r', 'south': 'r', 'up': 'r', 'down': 'r', 'east': 'n', 'west': 'n'}, uv={'east': [0, 0, 16, 16], 'west': [0, 0, 16, 16]})],
                          {'r': 'runner_roll', 'n': 'runner_end'}, 'runner_roll')
    return m


KINDS = ['chair', 'newel', 'mirror', 'case', 'photo', 'railing', 'mattress', 'offer', 'phone', 'cards', 'grab_rail', 'runner']
STAGES = {'mirror': 2, 'cards': 2, 'runner': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 2
CARRIED = ['case', 'chair']
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.625, 0.625, 0.625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
}
NAMES = {'people_fitting': 'Household Fitting', 'people_tile_large': 'Foyer Tile', 'people_tile_small': 'Kitchen Tile', 'people_boards': 'Oak Boards',
         'people_carpet': 'Carpet', 'people_plaster': 'Painted Plaster', 'people_trim': 'Gloss Trim', 'people_siding': 'Lap Siding',
         'people_shingle': 'Asphalt Shingle', 'people_concrete': 'Garage Concrete', 'people_drive': 'Driveway'}
ITEM_NAMES = {'item.the_oldest_house.people_carried': 'Something to Carry'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/people_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/people_{name}'}}
        out[f'blockstates/people_{name}.json'] = {'variants': {'': {'model': NS + f'block/people_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/people/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/people/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/people_fitting.json'] = {'variants': variants}
    for kind in CARRIED:
        out[f'models/item/people_carried_{kind}.json'] = {'parent': NS + f'block/people/{kind}_0', 'display': DISPLAY}
    out['models/item/people_carried.json'] = {'parent': NS + 'item/people_carried_case',
                                              'overrides': [{'predicate': {'custom_model_data': i + 1}, 'model': NS + f'item/people_carried_{kind}'} for i, kind in enumerate(CARRIED)]}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/people/{tex.split("people/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
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
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the house Stan built: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
