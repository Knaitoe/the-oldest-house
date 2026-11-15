"""The Supper (0.4.348): the Marsh farmhouse at the end of the corn maze, at dusk.

Draws, in native Minecraft pixels from fixed seeds: three materials (the dining room's faded wallpaper, the farmhouse's worn floor
boards, and the kitchen's checked linoleum), and every fitting of the marsh_fitting block, its models facing north and turned by its
facing: the coir doormat at the top of the porch steps; a set of car keys on their fob; a coat on the hook by the door, and the same
coat over the back of a kitchen chair; the porch light, lit and dark; an upstairs window, lit behind its curtains and dark; the bread
board with its loaf; the roast under its cover and carved; the butcher's block; the dinner bell in its yoke; and the supper card
tacked to the post at the mouth of the maze. Also the two carried things' item pictures (the keys and the bell's clapper). Writes the
models and block states and the blocks' and items' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import flicker
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/marsh/'


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
def wallpaper():
    # A small sprig in two rows on a faded ground, the stripes between them gone soft with sun.
    base = '#d8cdb0'
    im = new(base=base, seed=3480, spread=3)
    for x in (0, 8):
        for y in range(16):
            dot(im, x, y, shade(base, -10)[:3])
    for cx, cy in ((4, 3), (12, 11)):
        dot(im, cx, cy, '#a46a5c'); dot(im, cx - 1, cy, '#b47a6a'); dot(im, cx + 1, cy, '#b47a6a'); dot(im, cx, cy - 1, '#b47a6a')
        dot(im, cx, cy + 1, '#7c8a5a'); dot(im, cx - 1, cy + 2, '#7c8a5a'); dot(im, cx + 1, cy + 2, '#6a7a4c')
    return im


def boards():
    # Wide farmhouse boards, worn paler down the middle where people walk, nail heads at the ends.
    base = '#9a7448'
    im = new(base=base, seed=3481, spread=4); rnd = random.Random(3482)
    for y in (4, 9, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -34)[:3])
    for row, (y0, y1) in enumerate(((0, 4), (5, 9), (10, 15))):
        for y in range(y0, y1):
            for x in range(5, 11):
                if rnd.random() < .5:
                    dot(im, x, y, shade(base, 8)[:3])
        x = (3, 12, 7)[row]
        dot(im, x, y0 + 1, '#4a3a2a')
    for _ in range(9):
        y = rnd.randrange(16)
        if y not in (4, 9, 15):
            x = rnd.randrange(15)
            dot(im, x, y, shade(base, -14)[:3]); dot(im, x + 1, y, shade(base, -14)[:3])
    return im


def linoleum():
    # Kitchen linoleum in a check, scuffed grey along the run from the cooker to the sink.
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(3483)
    for y in range(16):
        for x in range(16):
            dark = ((x // 4) + (y // 4)) % 2 == 0
            px[x, y] = shade('#5e6a62' if dark else '#d4ccb4', rnd.randint(-4, 4))
    for _ in range(12):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8a8478')
    return im


def materials():
    return {'wallpaper': wallpaper(), 'boards': boards(), 'linoleum': linoleum()}


# ------------------------------------------------------------------------------------------------ fittings' faces
def coir():
    base = '#9a7a46'
    im = new(base=base, seed=3484, spread=10)
    frame(im, 0, 0, 16, 16, '#5a4426')
    for y in range(2, 14, 2):
        for x in range(2, 14):
            if (x + y) % 3 == 0:
                dot(im, x, y, shade(base, -22)[:3])
    return im


def coat_face(back=False):
    # A wool coat, buttoned, its collar turned: the front with a placket and buttons, the back plain with a seam.
    base = '#7a6a52'
    im = new(base=base, seed=3485 + back, spread=4)
    if back:
        for y in range(16):
            dot(im, 8, y, shade(base, -16)[:3])
        return im
    for y in range(16):
        dot(im, 7, y, shade(base, -18)[:3]); dot(im, 8, y, shade(base, 6)[:3])
    for y in (4, 8, 12):
        dot(im, 9, y, '#2e2620')
    for x in range(3, 13):
        dot(im, x, 0, shade(base, -24)[:3]); dot(im, x, 1, shade(base, 10)[:3])
    for y in (10, 11):
        for x in (2, 3, 4, 11, 12, 13):
            dot(im, x, y, shade(base, -12)[:3])
    return im


def lamp_glass(lit):
    im = new(base='#f4dc8a' if lit else '#5a5850', seed=3487 + lit, spread=6 if lit else 3)
    frame(im, 0, 0, 16, 16, '#2a2826')
    for x in range(16):
        dot(im, x, 8, '#2a2826')
    for y in range(16):
        dot(im, 8, y, '#2a2826')
    if lit:
        for x in range(5, 11):
            for y in range(5, 11):
                dot(im, x, y, '#fff2c0')
    return im


def window(lit):
    # An upstairs sash window behind curtains drawn half across: lit, the room warm behind them; dark, only the glass.
    im = new(base='#e9cf86' if lit else '#2a3038', seed=3489 + lit, spread=5 if lit else 3)
    for x in range(16):
        dot(im, x, 0, '#e8e2d4'); dot(im, x, 15, '#e8e2d4'); dot(im, x, 8, '#e8e2d4')
    for y in range(16):
        dot(im, 0, y, '#e8e2d4'); dot(im, 15, y, '#e8e2d4'); dot(im, 7, y, '#e8e2d4'); dot(im, 8, y, '#e8e2d4')
    curtain = '#c8a87a' if lit else '#4a4640'
    for y in range(1, 15):
        if y == 8:
            continue
        for x in (1, 2, 3, 12, 13, 14):
            dot(im, x, y, shade(curtain, -10 if x in (2, 13) else 0)[:3])
    if lit:
        for y in range(2, 7):
            for x in range(5, 7):
                dot(im, x, y, '#fff0c4')
    return im


def loaf(top):
    base = '#b47a3c'
    im = new(base=base, seed=3491 + top, spread=6)
    if top:
        for x in range(3, 13, 3):
            for y in range(4, 12):
                dot(im, x, y, shade(base, -26)[:3])
    else:
        for x in range(16):
            dot(im, x, 15, '#d8b880')
    return im


def roast():
    base = '#7a3e26'
    im = new(base=base, seed=3493, spread=8)
    for y in range(5, 11):
        for x in range(3, 13):
            dot(im, x, y, '#b86a58' if (x + y) % 2 else '#a85a48')
    for x in range(3, 13):
        dot(im, x, 4, '#e8d8c4'); dot(im, x, 11, '#e8d8c4')
    return im


def cover():
    base = '#b8bcc0'
    im = new(base=base, seed=3494, spread=3)
    for x in range(16):
        dot(im, x, 2, '#e4e8ec'); dot(im, x, 13, '#7a7e84')
    for y in range(3, 13):
        dot(im, 4, y, '#d8dce0')
    return im


def platter():
    im = new(base='#e8e4d8', seed=3495, spread=2)
    frame(im, 0, 0, 16, 16, '#3a5a8a')
    frame(im, 1, 1, 15, 15, '#c8c4b8')
    return im


def endgrain():
    # A butcher's block from above: squares of end grain, rings in each, and the knife's hollow worn into the middle.
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(3496)
    for y in range(16):
        for x in range(16):
            tone = rnd.randint(-5, 5) + (((x // 4) + (y // 4)) % 2) * 8
            px[x, y] = shade('#b48a58', tone)
            if x % 4 in (1, 2) and y % 4 in (1, 2) and rnd.random() < .5:
                px[x, y] = shade('#9a7244', tone)
    for x in range(5, 11):
        for y in range(5, 11):
            px[x, y] = shade(px[x, y], -14)
    for x, y in ((6, 7), (9, 8), (7, 10)):
        dot(im, x, y, '#5a3a24')
    return im


def block_side():
    im = new(base='#a07a4a', seed=3497, spread=4)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#7a5a34')
    return im


def bell_iron():
    im = new(base='#2e2c2a', seed=3498, spread=4)
    for y in range(16):
        dot(im, 3, y, '#4e4a46'); dot(im, 4, y, '#5a5650')
    return im


def card():
    # The supper card: a square of cream card tacked by its corner, a hand's few lines on it, no words to read at this size.
    im = new(base='#ece2c8', seed=3499, spread=2)
    frame(im, 0, 0, 16, 16, '#b8a888')
    for y in (4, 7, 10):
        for x in range(3, 13):
            if (x * 7 + y) % 5:
                dot(im, x, y, '#3a3a4a')
    dot(im, 2, 2, '#8a8a8a')
    return im


def keys_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x, y in ((3, 3), (4, 3), (5, 3), (2, 4), (6, 4), (2, 5), (6, 5), (3, 6), (4, 6), (5, 6)):
        dot(im, x, y, '#b8bcc0')
    fill(im, 2, 8, 6, 14, '#3a3a44')
    dot(im, 3, 9, '#6a6a7a'); dot(im, 4, 10, '#8a2a2a')
    for i in range(6):
        dot(im, 7 + i, 5 + i, '#c8a44a'); dot(im, 8 + i, 5 + i, '#a8842a')
    dot(im, 11, 10, '#a8842a'); dot(im, 13, 12, '#a8842a'); dot(im, 13, 13, '#c8a44a')
    return im


def clapper_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(1, 10):
        dot(im, 7, y, '#4a4642'); dot(im, 8, y, '#2e2c2a')
    dot(im, 7, 0, '#6a6660'); dot(im, 8, 0, '#6a6660')
    fill(im, 5, 10, 11, 15, '#2e2c2a')
    dot(im, 6, 11, '#5a5650'); dot(im, 6, 12, '#4e4a46')
    return im


# 0.4.583 (the owner: "a white box floats in the air above the long table (with a dark block on top)", and the table's textures).
# The table was the film set's kitchen table: a one-pixel top showing a four-pixel square of the set's atlas over the whole block,
# on legs showing another. Its lamp was the set's ceiling lamp, stood on the table: a shade hanging from a stem that reached up to
# nothing. The farm has a table and a lamp of its own. Every face takes a piece of its picture of the face's own size (PIECES names
# them, part() checks them), and the table's boards run the same way on every block of it: no model of it is turned.
PIECES = {
    'table_top': {'top': (0, 0, 16, 16)},
    'table_parts': {'edge': (0, 0, 16, 2), 'edge_long': (0, 2, 16, 2), 'apron': (0, 4, 16, 3), 'apron_12': (0, 4, 12, 3), 'apron_under': (0, 7, 16, 1),
                    'apron_under_12': (0, 7, 12, 1), 'leg': (0, 8, 3, 8), 'leg_head': (3, 8, 3, 6)},
    'oil_lamp': {'font': (0, 0, 6, 3), 'font_top': (0, 3, 6, 6), 'burner': (6, 0, 2, 2), 'chimney': (8, 0, 4, 7), 'chimney_top': (12, 0, 4, 4)},
}


def table_top():
    # Three scrubbed boards the length of the table, a seam between each, the grain along them.
    base = '#b08a5a'
    im = new(base=base, seed=3601, spread=4); rnd = random.Random(3602)
    for x in (4, 9, 15):
        for y in range(16):
            dot(im, x, y, shade(base, -30)[:3])
    for x0, x1 in ((0, 4), (5, 9), (10, 15)):
        for _ in range(3):
            x = rnd.randrange(x0, x1); y = rnd.randrange(0, 13)
            for k in range(rnd.randrange(2, 5)):
                dot(im, x, y + k, shade(base, -13)[:3])
    return im


def table_parts():
    # The boards' ends (across the table's ends) and their long edge (down its sides), the rail under them and its underside, a
    # leg, and the square head of it where the rails meet.
    base = '#a07c4e'; im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(3603)
    for y0 in (0, 2):
        fill(im, 0, y0, 16, y0 + 1, shade(base, 12)[:3]); fill(im, 0, y0 + 1, 16, y0 + 2, shade(base, -16)[:3])
    for x in (4, 9, 15):
        dot(im, x, 0, shade(base, -30)[:3]); dot(im, x, 1, shade(base, -36)[:3])
    fill(im, 0, 4, 16, 6, shade(base, -6)[:3]); fill(im, 0, 6, 16, 7, shade(base, -24)[:3])
    for _ in range(6):
        x = rnd.randrange(15); dot(im, x, 4 + rnd.randrange(2), shade(base, -16)[:3]); dot(im, x + 1, 5, shade(base, -16)[:3])
    fill(im, 0, 7, 16, 8, shade(base, -36)[:3])
    fill(im, 0, 8, 3, 16, shade(base, -2)[:3]); fill(im, 2, 8, 3, 16, shade(base, -22)[:3]); fill(im, 0, 8, 1, 16, shade(base, 10)[:3])
    fill(im, 0, 15, 3, 16, shade(base, -34)[:3])
    fill(im, 3, 8, 6, 14, shade(base, 4)[:3]); fill(im, 5, 8, 6, 14, shade(base, -20)[:3]); fill(im, 3, 13, 6, 14, shade(base, -26)[:3]); dot(im, 4, 10, shade(base, -40)[:3])
    return im


def oil_lamp():
    # A brass font, the burner on it, and the glass chimney with the flame inside.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); brass = '#c8a44a'
    fill(im, 0, 0, 6, 1, shade(brass, 18)[:3]); fill(im, 0, 1, 6, 2, brass); fill(im, 0, 2, 6, 3, shade(brass, -40)[:3]); dot(im, 1, 1, shade(brass, 40)[:3])
    fill(im, 0, 3, 6, 9, shade(brass, 6)[:3]); frame(im, 0, 3, 6, 9, shade(brass, -28)[:3]); fill(im, 2, 5, 4, 7, '#3a3226')
    fill(im, 6, 0, 8, 1, '#5a5040'); fill(im, 6, 1, 8, 2, '#3a3226')
    fill(im, 8, 0, 12, 7, '#b9c6bd'); fill(im, 9, 0, 11, 7, '#f2e3b0'); fill(im, 9, 0, 11, 1, '#d9d6b4')
    for y, c in ((2, '#ffd35a'), (3, '#ffe9a0'), (4, '#fff6d0'), (5, '#ffb43c')):
        fill(im, 9, y, 11, y + 1, c)
    dot(im, 9, 2, '#f2e3b0'); fill(im, 8, 6, 12, 7, '#98a69d'); fill(im, 9, 6, 11, 7, '#4a3a20')
    fill(im, 12, 0, 16, 4, '#b9c6bd'); fill(im, 13, 1, 15, 3, '#6a5630')
    return im


def textures():
    out = {f'block/marsh_{k}.png': v for k, v in materials().items()}
    t = {
        'coir': coir(), 'fob': new(base='#2e2e36', seed=3500, spread=3), 'steel': new(base='#b8bcc0', seed=3501, spread=4),
        'brass': new(base='#c8a44a', seed=3502, spread=6), 'iron': new(base='#3a3836', seed=3503, spread=3),
        'coat': coat_face(), 'coat_back': coat_face(True), 'chair': new(base='#8a6a44', seed=3504, spread=5),
        'glass_lit': lamp_glass(True), 'glass_dark': lamp_glass(False), 'lamp_metal': new(base='#24282a', seed=3505, spread=3),
        'window_lit': window(True), 'window_dark': window(False), 'board': new(base='#c4a272', seed=3506, spread=4),
        'crust': loaf(False), 'crust_top': loaf(True), 'cover': cover(), 'roast': roast(), 'platter': platter(),
        'endgrain': endgrain(), 'block_side': block_side(), 'bell': bell_iron(), 'yoke': new(base='#6a4a2c', seed=3507, spread=4),
        'card': card(), 'post': new(base='#7a6a52', seed=3508, spread=5),
        'table_top': table_top(), 'table_parts': table_parts(), 'oil_lamp': oil_lamp(),
    }
    out.update({f'block/marsh/{k}.png': v for k, v in t.items()})
    out['item/marsh_keys.png'] = keys_item()
    out['item/marsh_clapper.png'] = clapper_item()
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


SIDES = ('north', 'south', 'east', 'west')


def part(lo, hi, faces, lit=False):
    """A box whose named faces each take a piece of a picture: faces maps a face to (picture, piece) or (picture, piece, turn). The
    piece must be the face's own size (turned a quarter, its sides the other way round), so nothing is stretched."""
    across = {'north': (hi[0] - lo[0], hi[1] - lo[1]), 'east': (hi[2] - lo[2], hi[1] - lo[1]), 'up': (hi[0] - lo[0], hi[2] - lo[2])}
    across.update(south=across['north'], west=across['east'], down=across['up'])
    out = {}
    for face, spec in faces.items():
        picture, piece = spec[0], spec[1]; turn = spec[2] if len(spec) > 2 else 0
        x, y, w, h = PIECES[picture][piece]
        assert (w, h) == (across[face] if turn in (0, 180) else across[face][::-1]), (lo, hi, face, piece, across[face])
        out[face] = {'texture': '#' + picture, 'uv': [x, y, x + w, y + h]}
        if turn:
            out[face]['rotation'] = turn
    e = {'from': lo, 'to': hi, 'faces': out}
    if lit:
        e['shade'] = False; e['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    return e


def table_cell(sides=(), leg=None):
    """One block of the supper table: its boards, their edge on the table's outer sides, the rail under them along those sides, and
    a leg at the corner named ('nw', 'ne', 'se', 'sw'). A corner block's rails start past its leg."""
    T, K = 'table_top', 'table_parts'
    els = [part([0, 14, 0], [16, 16, 16], {'up': (T, 'top'), 'down': (T, 'top'), **{f: (K, 'edge' if f in ('north', 'south') else 'edge_long') for f in sides}})]
    if leg:
        x, z = (1 if 'w' in leg else 12), (1 if 'n' in leg else 12)
        els.append(part([x, 8, z], [x + 3, 14, z + 3], {f: (K, 'leg_head') for f in SIDES}))
        els.append(part([x, 0, z], [x + 3, 8, z + 3], {f: (K, 'leg') for f in SIDES}))
    for side in sides:
        lo, hi = 0, 16
        if leg:
            first = ('w' in leg) if side in ('north', 'south') else ('n' in leg)
            lo, hi = (4, 16) if first else (0, 12)
        piece, under = ('apron', 'apron_under') if hi - lo == 16 else ('apron_12', 'apron_under_12')
        if side in ('north', 'south'):
            z = 1 if side == 'north' else 14
            els.append(part([lo, 11, z], [hi, 14, z + 1], {'north': (K, piece), 'south': (K, piece), 'down': (K, under)}))
        else:
            x = 1 if side == 'west' else 14
            els.append(part([x, 11, lo], [x + 1, 14, hi], {'east': (K, piece), 'west': (K, piece), 'down': (K, under, 90)}))
    return model(els, {T: T, K: K}, T)


# The table's blocks, by what the builder asks for: a block in the middle, one on an edge (its facing the side that is outward), one
# at a corner (its facing the corner, as the set's table had it: north for the north-west, east for the north-east, and so round).
CORNER = {'north': ('nw', ('north', 'west')), 'east': ('ne', ('north', 'east')), 'south': ('se', ('south', 'east')), 'west': ('sw', ('south', 'west'))}


def table_models():
    m = {'table': table_cell()}
    for facing in SIDES:
        m[f'table_edge_{facing}'] = table_cell((facing,))
        corner, sides = CORNER[facing]
        m[f'table_leg_{facing}'] = table_cell(sides, corner)
    return m


def chair(extra, textures):
    # A plain kitchen chair facing north (its sitter would face north): four legs, the seat at eight pixels, the back to the south.
    e = []
    for x, z in ((2, 2), (12.5, 2), (2, 12.5), (12.5, 12.5)):
        e.append(element([x, 0, z], [x + 1.5, 7, z + 1.5], {'all': 'c'}))
    e.append(element([2, 7, 2], [14, 8, 14], {'all': 'c'}))
    for x in (2, 12.5):
        e.append(element([x, 8, 12.5], [x + 1.5, 19, 14], {'all': 'c'}))
    e.append(element([2, 16, 12.5], [14, 19, 14], {'all': 'c'}))
    return model(e + extra, {'c': 'chair', **textures}, 'chair')


def fitting_models():
    m = {}
    m['mat_0'] = model([element([1, 0, 3], [15, 0.5, 13], {'up': 'm', 'down': 'm', 'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm'})], {'m': 'coir'}, 'coir')
    m['keys_0'] = model([element([4, 0, 6], [8, 1, 9.5], {'all': 'f'}), element([7.5, 0, 6.5], [10.5, 0.3, 9.5], {'all': 's'}),
                         element([9.5, 0, 7.5], [13.5, 0.4, 8.5], {'all': 'b'}), element([10, 0, 8.5], [12, 0.4, 10], {'all': 's'})],
                        {'f': 'fob', 's': 'steel', 'b': 'brass'}, 'brass')
    # On the hook by the door: the hook in the wall to the south, the coat hanging from it, shoulders squared, hem a hand off the floor.
    m['coat_0'] = model([element([7, 14, 15], [9, 15, 16], {'all': 'i'}), element([7.5, 13, 14], [8.5, 15, 15], {'all': 'i'}),
                         element([3, 3, 13], [13, 13, 15], {'north': 'k', 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'}),
                         element([4, 13, 13.2], [12, 14, 14.8], {'all': 'b'}),
                         element([2, 6, 13.2], [3, 13, 14.8], {'all': 'b'}), element([13, 6, 13.2], [14, 13, 14.8], {'all': 'b'})],
                        {'i': 'iron', 'k': 'coat', 'b': 'coat_back'}, 'coat')
    # Over the back of a kitchen chair: the shoulders hooked over the top rail, the body hanging down the back, a sleeve on the seat.
    m['coat_1'] = chair([element([1.5, 9, 11.5], [14.5, 19.5, 15], {'north': 'b', 'south': 'k', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'}),
                         element([3, 8, 6], [6, 9, 12], {'all': 'b'})], {'k': 'coat', 'b': 'coat_back'})
    for stage, glass in enumerate(('glass_lit', 'glass_dark')):
        m[f'porch_light_{stage}'] = model([element([6, 6, 15], [10, 13, 16], {'all': 'l'}), element([7, 9, 13], [9, 10, 15], {'all': 'l'}),
                                           element([5, 4, 9], [11, 10, 13], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'l', 'down': 'l'}),
                                           element([4.5, 10, 8.5], [11.5, 11, 13.5], {'all': 'l'})], {'l': 'lamp_metal', 'g': glass}, glass)
    for stage, glass in enumerate(('window_lit', 'window_dark')):
        m[f'window_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w'})], {'w': glass}, glass)
    m['bread_0'] = model([element([2, 0, 4], [14, 1, 12], {'all': 'd'}), element([4, 1, 5.5], [12, 5, 10.5], {'all': 'c', 'up': 't'})],
                         {'d': 'board', 'c': 'crust', 't': 'crust_top'}, 'crust')
    m['roast_0'] = model([element([1, 0, 1], [15, 0.5, 15], {'all': 'p'}), element([3, 0.5, 3], [13, 6, 13], {'all': 'v'}),
                          element([4, 6, 4], [12, 7, 12], {'all': 'v'}), element([7, 7, 7], [9, 8.5, 9], {'all': 'v'})],
                         {'p': 'platter', 'v': 'cover'}, 'cover')
    m['roast_1'] = model([element([1, 0, 1], [15, 0.5, 15], {'all': 'p'}), element([4, 0.5, 4], [12, 5, 12], {'all': 'r'}),
                          element([12.5, 0.5, 3], [13.5, 1, 13], {'all': 's'})],
                         {'p': 'platter', 'r': 'roast', 's': 'steel'}, 'roast')
    m['butcher_0'] = model([element([1, 10, 1], [15, 16, 15], {'all': 's', 'up': 'e', 'down': 'e'}),
                            *[element([x, 0, z], [x + 2.5, 10, z + 2.5], {'all': 's'}) for x, z in ((1.5, 1.5), (12, 1.5), (1.5, 12), (12, 12))],
                            element([3, 3, 3], [13, 4, 13], {'all': 's'})],
                           {'s': 'block_side', 'e': 'endgrain'}, 'endgrain')
    # The dinner bell in its yoke on the post below it: the yoke's two arms and crossbar, the bell hung beneath, its rope tied off.
    m['bell_0'] = model([element([2, 0, 7], [3.5, 11, 9], {'all': 'y'}), element([12.5, 0, 7], [14, 11, 9], {'all': 'y'}),
                         element([2, 11, 6.5], [14, 13, 9.5], {'all': 'y'}), element([6, 5, 6], [10, 10, 10], {'all': 'i'}),
                         element([5, 3, 5], [11, 5, 11], {'all': 'i'}), element([7.5, 10, 7.5], [8.5, 11, 8.5], {'all': 'i'}),
                         element([7.5, 1, 7.5], [8.5, 3, 8.5], {'all': 'i'}), element([6, 0, 7.5], [7, 6, 8.5], {'all': 'y'})],
                        {'y': 'yoke', 'i': 'bell'}, 'bell')
    m['card_0'] = model([element([5, 0, 5], [11, 16, 11], {'all': 'p'}), element([4, 8, 4.5], [12, 14, 5], {'north': 'c', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})],
                        {'p': 'post', 'c': 'card'}, 'card')
    # The oil lamp on the supper table: the font, the burner, the chimney with its flame.
    L = 'oil_lamp'
    m['oil_lamp_0'] = model([part([5, 0, 5], [11, 3, 11], {**{f: (L, 'font') for f in SIDES}, 'up': (L, 'font_top')}),
                             part([7, 3, 7], [9, 5, 9], {f: (L, 'burner') for f in SIDES}),
                             part([6, 5, 6], [10, 12, 10], {**{f: (L, 'chimney') for f in SIDES}, 'up': (L, 'chimney_top')}, lit=True)], {L: L}, L)
    m.update(table_models())
    return m


KINDS = ['mat', 'keys', 'coat', 'porch_light', 'window', 'bread', 'roast', 'butcher', 'bell', 'card', 'table', 'table_edge', 'table_leg', 'oil_lamp']
# The table's blocks are never turned by their facing (their boards would turn with them): each facing has a model of its own.
UNTURNED = {'table': lambda facing: 'table', 'table_edge': lambda facing: f'table_edge_{facing}', 'table_leg': lambda facing: f'table_leg_{facing}'}
STAGES = {'coat': 2, 'porch_light': 2, 'window': 2, 'roast': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 2
NAMES = {'marsh_fitting': 'Farmhouse Fitting', 'marsh_wallpaper': 'Faded Wallpaper', 'marsh_boards': 'Farmhouse Boards', 'marsh_linoleum': 'Kitchen Linoleum'}
ITEM_NAMES = {'item.the_oldest_house.marsh_keys': 'Car Keys on a Fob', 'item.the_oldest_house.marsh_clapper': 'Dinner Bell Clapper'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/marsh_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/marsh_{name}'}}
        out[f'blockstates/marsh_{name}.json'] = {'variants': {'': {'model': NS + f'block/marsh_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/marsh/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/marsh/{kind}_{drawn}'}
                if kind in UNTURNED:
                    v = {'model': NS + 'block/marsh/' + UNTURNED[kind](facing)}
                elif y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/marsh_fitting.json'] = {'variants': variants}
    for item in ('keys', 'clapper'):
        out[f'models/item/marsh_{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/marsh_{item}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert (UNTURNED[kind]('north') if kind in UNTURNED else f'{kind}_{stage}') in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/marsh/{tex.split("marsh/")[1]}.png' in images, (key, tex)
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
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the Marsh farmhouse: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
