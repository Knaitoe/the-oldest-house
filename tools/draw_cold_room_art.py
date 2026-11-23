"""The cold room (0.4.754): a rented house on a steep street in winter.

Draws, in native Minecraft pixels from fixed seeds: eight materials (the hall's boards, the stair runner, the faded wallpaper, the
ceilings' plaster, the kitchen's linoleum; outside, the house's brick, the rendered retaining walls and the steps' worn flags); the two
flights of stairs (the treads inside, of the boards; the steps outside, of the flags); and every fitting of the cold_room_fitting block,
its models facing north and turned by its facing (a thing against a wall faces away from it):

- BED: the iron bed, its head with the pillow (0) and its foot (1), the straps buckled to the frame at both.
- DRESSER (shut, every drawer out), BOOK (shut, open), CRUCIFIX (upright, turned upside down), NIGHTSTAND (with the lamp, with the flask).
- RADIATOR, CHAIR, HALL_TABLE (the key bowl, the letter), HOOKS, KITCHEN_TABLE, STOVE, SINK, BULB, STREETLAMP (post, lantern).

Writes the models and block states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/cold_room/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=3, size=(16, 16)):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); w, h = im.size
    for y in range(max(0, y0), min(h, y1)):
        for x in range(max(0, x0), min(w, x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        im.putpixel((x, y), shade(colour, 0, a))


def line(im, x0, y0, x1, y1, colour):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), colour)


def frame(im, colour, dark):
    w, h = im.size
    for i in range(w):
        dot(im, i, 0, colour); dot(im, i, h - 1, dark)
    for i in range(h):
        dot(im, 0, i, colour); dot(im, w - 1, i, dark)


# ------------------------------------------------------------------------------------------------ materials
def boards():
    # Narrow pine boards run the length of the hall, a seam every four, nail heads at the joists, worn paler down the middle.
    im = new('#6e5238', 75401, 3)
    for x in (0, 4, 8, 12):
        for y in range(16):
            dot(im, x, y, '#4e3826')
    for x, y in ((2, 3), (6, 11), (10, 3), (14, 11)):
        dot(im, x, y, '#3a2a1c')
    for y in (5, 13):
        for x in range(16):
            if x % 4:
                dot(im, x, y, '#644a32')
    for x in (5, 6, 9, 10):
        for y in range(16):
            r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (min(255, r + 8), min(255, g + 7), min(255, b + 5), a))
    return im


def runner():
    # The stair runner: worn wool with a border at either edge and a small repeat down the middle.
    im = new('#6a3a34', 75402, 3)
    for y in range(16):
        dot(im, 1, y, '#8a6a48'); dot(im, 14, y, '#8a6a48'); dot(im, 2, y, '#4a2622'); dot(im, 13, y, '#4a2622')
    for y0 in (2, 10):
        for dy, w in ((0, 0), (1, 1), (2, 2), (3, 1), (4, 0)):
            for dx in range(-w, w + 1):
                dot(im, 7 + dx, y0 + dy, '#86584a'); dot(im, 8 + dx, y0 + dy, '#86584a')
    rnd = random.Random(75403)
    for _ in range(10):
        dot(im, rnd.randrange(4, 12), rnd.randrange(16), '#5a302a')
    return im


def paper():
    # Faded wallpaper: a sprig repeat on a ground gone the shade of weak tea, a seam down one side, a damp bloom low on the left.
    im = new('#b8aa8c', 75404, 2)
    for ox, oy in ((4, 3), (12, 11), (12, -5), (4, 19)):
        for dx, dy in ((0, 0), (0, 1), (0, 2), (-1, 1), (1, 2), (-1, 3), (1, 0)):
            dot(im, ox + dx, oy + dy, '#988a6c')
        dot(im, ox, oy - 1, '#a08e6a')
    for y in range(16):
        dot(im, 15, y, '#a89a7c')
    for x, y in ((0, 13), (1, 13), (0, 14), (1, 14), (2, 14), (0, 15), (1, 15), (2, 15), (3, 15)):
        dot(im, x, y, '#a49678')
    return im


def plaster():
    # Plaster ceiling, a hairline crack across one corner.
    im = new('#c8c2b4', 75405, 2)
    for x, y in ((10, 0), (11, 1), (11, 2), (12, 3), (13, 3), (14, 4), (15, 5)):
        dot(im, x, y, '#aaa496')
    return im


def lino():
    # Kitchen linoleum in a small check, scuffed in front of the sink.
    im = new('#9a9280', 75406, 2)
    for y in range(16):
        for x in range(16):
            if (x // 4 + y // 4) % 2:
                dot(im, x, y, shade('#7e7868', random.Random(75407 + x * 16 + y).randint(-2, 2)))
    for x, y in ((5, 6), (6, 6), (6, 7), (9, 9), (10, 9)):
        dot(im, x, y, '#8a8474')
    return im


def brick():
    # The house's brick, sooted at the joints.
    im = new('#7a4a3a', 75408, 4)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#5a4a42')
    for row, y0 in enumerate((0, 4, 8, 12)):
        for x in ((7, 15) if row % 2 == 0 else (3, 11)):
            for y in range(y0, y0 + 3):
                dot(im, x, y, '#5a4a42')
    return im


def render():
    # The retaining walls' render, pitted, a vertical stain where water runs down from the coping.
    im = new('#8a867c', 75409, 4)
    rnd = random.Random(75410)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#76726a')
    for y in range(16):
        dot(im, 11, y, '#7a766c')
        if y % 3:
            dot(im, 12, y, '#7e7a70')
    return im


def flags():
    # The steps' flags: worn stone, a crack, frost in the low places.
    im = new('#7c7a74', 75411, 3)
    for x in range(16):
        dot(im, x, 0, '#9a9890'); dot(im, x, 15, '#5e5c56')
    for x, y in ((3, 5), (4, 6), (5, 6), (6, 7), (7, 9)):
        dot(im, x, y, '#5a5852')
    for x, y in ((10, 10), (11, 10), (12, 11), (2, 12), (3, 12)):
        dot(im, x, y, '#c8ccd0')
    return im


def materials():
    return {'boards': boards(), 'runner': runner(), 'paper': paper(), 'plaster': plaster(), 'lino': lino(), 'brick': brick(),
            'render': render(), 'flags': flags()}


# ------------------------------------------------------------------------------------------------ fitting textures
IRON, SHEET, WOOD = '#3a3a3e', '#d8d4c8', '#5a4430'


def wood(seed, base=WOOD):
    im = new(base, seed, 3)
    rnd = random.Random(seed + 1)
    for _ in range(4):
        y = rnd.randrange(16)
        for x in range(16):
            if rnd.random() < .7:
                dot(im, x, y, shade(base, -10))
    return im


def iron():
    im = new(IRON, 75420, 3)
    for i in range(16):
        dot(im, i, 0, '#5a5a60'); dot(im, 0, i, '#5a5a60')
    return im


def sheet():
    # The bed's sheet, rumpled and damp: folds as long shadows, a darker patch where it is wettest.
    im = new(SHEET, 75421, 3)
    for y, x0, x1 in ((3, 1, 12), (7, 3, 15), (11, 0, 9), (13, 6, 14)):
        for x in range(x0, x1):
            dot(im, x, y, '#bcb8ac'); dot(im, x, y - 1, '#e6e2d8')
    for x, y in ((9, 8), (10, 8), (9, 9), (10, 9), (11, 9), (10, 10)):
        dot(im, x, y, '#c4c0b2')
    return im


def ticking():
    # The mattress's side under the sheet: striped ticking.
    im = new('#c8c4b4', 75422, 2)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#8a8e9a')
    return im


def pillow():
    im = new('#e0dcd0', 75423, 2)
    frame(im, '#eeeae0', '#c2beb2')
    for x in range(3, 13):
        dot(im, x, 8, '#ccc8bc')
    return im


def strap():
    # A canvas strap with its brass buckle.
    im = new('#8a7a5a', 75424, 3)
    for x in range(16):
        dot(im, x, 0, '#6a5a3e'); dot(im, x, 15, '#6a5a3e')
    for x, y in ((6, 4), (7, 4), (8, 4), (9, 4), (6, 11), (7, 11), (8, 11), (9, 11)):
        dot(im, x, y, '#b09a4a')
    for y in range(4, 12):
        dot(im, 6, y, '#b09a4a'); dot(im, 9, y, '#b09a4a')
    return im


def dresser_front(open_):
    # Three drawers in a dark wood front, each with two pulls; out, the fronts stand forward and their sides show.
    im = wood(75425, '#4a3424')
    for y0 in (1, 6, 11):
        fill(im, 1, y0, 15, y0 + 4, '#563e2a')
        for x in range(1, 15):
            dot(im, x, y0, '#6a503a'); dot(im, x, y0 + 3, '#3a2818')
        dot(im, 4, y0 + 2, '#b09a62'); dot(im, 11, y0 + 2, '#b09a62')
    return im


def drawer_side():
    im = wood(75426, '#7a6248')
    return im


def book_cover():
    im = new('#3a2a22', 75427, 2)
    frame(im, '#4a3a2e', '#2a1e18')
    for x, y in ((7, 5), (8, 5), (7, 6), (8, 6), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (7, 8), (8, 8), (7, 9), (8, 9), (7, 10), (8, 10)):
        dot(im, x, y, '#a08a52')
    return im


def book_pages():
    # Open: two pages of small close print and a ribbon down the gutter.
    im = new('#d8ceb4', 75428, 2)
    for y in range(2, 15, 2):
        for x in list(range(1, 7)) + list(range(9, 15)):
            if random.Random(75429 + x * 31 + y).random() < .75:
                dot(im, x, y, '#6a6250')
    for y in range(16):
        dot(im, 7, y, '#a89e84'); dot(im, 8, y, '#6a1e22')
    return im


def cross_wood():
    im = wood(75430, '#3e2c1e')
    return im


def lamp_shade():
    im = new('#d8c49a', 75431, 2)
    for x in range(16):
        dot(im, x, 0, '#c0ac82'); dot(im, x, 15, '#c0ac82')
    return im


def lamp_base():
    return new('#8a7a62', 75432, 3)


def flask_glass():
    im = new('#9aaab0', 75433, 2)
    for y in range(4, 16):
        for x in range(16):
            dot(im, x, y, shade('#7a8e98', random.Random(75434 + y).randint(-2, 2)), 220)
    for y in range(2, 14):
        dot(im, 3, y, '#d8e0e4')
    return im


def cork():
    return new('#9a7a52', 75435, 4)


def radiator():
    # Cast iron columns, painted over many times, the paint flaking at the top of each.
    im = new('#b4b0a4', 75436, 2)
    for x in range(1, 16, 3):
        for y in range(16):
            dot(im, x, y, '#8e8a80')
    for x in (2, 5, 8, 11, 14):
        dot(im, x, 1, '#6a665c')
    return im


def chair_wood():
    return wood(75437, '#6a5034')


def letter():
    # The priest's letter, folded once, a line of a hand across it.
    im = new('#e4dcc4', 75438, 2)
    for y in (4, 7, 10):
        for x in range(2, 14):
            if (x * 7 + y) % 5:
                dot(im, x, y, '#3a3446')
    for x in range(16):
        dot(im, x, 8, '#cac2aa')
    return im


def bowl():
    im = new('#7a5a4a', 75439, 3)
    frame(im, '#9a7a62', '#5a3e32')
    return im


def hook_board():
    im = wood(75440, '#5a4430')
    for x in (3, 8, 13):
        dot(im, x, 6, '#2a2a2a'); dot(im, x, 7, '#2a2a2a')
    return im


def coat(seed, base):
    im = new(base, seed, 3)
    for y in range(16):
        dot(im, 7, y, shade(base, -18))
    for x, y in ((6, 4), (6, 8), (6, 12)):
        dot(im, x, y, '#2a2620')
    return im


def snow_coat():
    im = coat(75441, '#2e3036')
    for x in range(16):
        if x % 3:
            dot(im, x, 0, '#e8ecee'); dot(im, x, 1, '#d4d8dc')
    return im


def oilcloth():
    # The kitchen table's oilcloth in a faded check, a cup ring, cigarette burns near the ashtray.
    im = new('#c4b89a', 75442, 2)
    for y in range(16):
        for x in range(16):
            if (x // 2 + y // 2) % 2:
                dot(im, x, y, '#a89c80')
    for x, y in ((3, 4), (4, 3), (5, 3), (6, 4), (6, 5), (5, 6), (4, 6), (3, 5)):
        dot(im, x, y, '#8a7a5e')
    for x, y in ((11, 11), (13, 12)):
        dot(im, x, y, '#4a3a2a')
    return im


def crockery():
    return new('#d8d4ca', 75443, 2)


def enamel(seed=75444):
    im = new('#d4d0c4', seed, 2)
    frame(im, '#e4e0d4', '#a4a094')
    return im


def stove_top():
    im = enamel(75445)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                if abs(dx) == 2 or abs(dy) == 2:
                    dot(im, cx + dx, cy + dy, '#2a2a2c')
        dot(im, cx, cy, '#3a5a8a')
    return im


def stove_front():
    im = enamel(75446)
    fill(im, 2, 6, 14, 14, '#bcb8ac')
    frame_box(im, 2, 6, 14, 14, '#8a867a')
    for x in (3, 6, 9, 12):
        dot(im, x, 3, '#2a2a2c')
    return im


def frame_box(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


def sink_top():
    im = enamel(75447)
    fill(im, 3, 3, 13, 13, '#a8a49a')
    frame_box(im, 3, 3, 13, 13, '#8a867c')
    for x, y in ((5, 5), (7, 5), (9, 8), (6, 9)):
        dot(im, x, y, '#e4e0d8')
    return im


def cupboard():
    im = wood(75448, '#c0b49a')
    for y in range(16):
        dot(im, 8, y, '#9a8e76')
    dot(im, 6, 4, '#4a4a4a'); dot(im, 9, 4, '#4a4a4a')
    return im


def metal():
    return new('#a4a8ac', 75449, 3)


def flex():
    return new('#2a2622', 75450, 2)


def bulb_glass():
    im = new('#f4ecc8', 75451, 2)
    for x, y in ((5, 5), (6, 5), (5, 6)):
        dot(im, x, y, '#ffffff')
    return im


def lamp_post():
    return new('#2a2c2e', 75452, 2)


def lamp_glass():
    im = new('#f0dca0', 75453, 3)
    frame(im, '#2a2c2e', '#2a2c2e')
    for y in range(16):
        dot(im, 8, y, '#2a2c2e')
    return im


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rotation=None):
    faces = {}
    keys = ['north', 'south', 'east', 'west', 'up', 'down']
    if 'all' in textures:
        textures = {**{k: textures['all'] for k in keys}, **{k: v for k, v in textures.items() if k != 'all'}}
    for k in keys:
        if k in textures:
            face = {'texture': '#' + textures[k]}
            if uv and k in uv:
                face['uv'] = uv[k]
            faces[k] = face
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, cutout=True):
    def where(v):
        return v if ':' in v else P + v
    m = {'ambientocclusion': False, 'textures': {**{k: where(v) for k, v in textures.items()}, 'particle': where(particle)}, 'elements': elements}
    if cutout:
        m['render_type'] = 'minecraft:cutout'
    return m


def bed_model(stage):
    t = {'i': 'iron', 's': 'sheet', 'k': 'ticking', 'p': 'pillow', 'r': 'strap'}
    e = [element([0, 5, 0], [16, 10, 16], {'all': 'k', 'up': 's'})]
    if stage == 0:
        # The head: the iron headboard at the wall end (south), its posts and bars, the pillow against it; straps at the shoulders.
        e += [element([0, 0, 14.5], [1.5, 16, 16], {'all': 'i'}), element([14.5, 0, 14.5], [16, 16, 16], {'all': 'i'}),
              element([1.5, 14, 15], [14.5, 15, 15.5], {'all': 'i'}), element([1.5, 10, 15], [14.5, 11, 15.5], {'all': 'i'}),
              element([4.5, 10, 15], [5.5, 14, 15.5], {'all': 'i'}), element([8, 10, 15], [9, 14, 15.5], {'all': 'i'}),
              element([11.5, 10, 15], [12.5, 14, 15.5], {'all': 'i'}),
              element([2, 10, 9], [14, 12, 14.5], {'all': 'p'}),
              element([-0.2, 6, 5], [16.2, 10.2, 7], {'east': 'r', 'west': 'r', 'up': 'r'})]
    else:
        # The foot: the footboard at the north end, lower, and the straps at the ankles; the legs at its corners.
        e += [element([0, 0, 0], [1.5, 12, 1.5], {'all': 'i'}), element([14.5, 0, 0], [16, 12, 1.5], {'all': 'i'}),
              element([1.5, 10, 0.5], [14.5, 11, 1], {'all': 'i'}), element([1.5, 7, 0.5], [14.5, 8, 1], {'all': 'i'}),
              element([-0.2, 6, 6], [16.2, 10.2, 8], {'east': 'r', 'west': 'r', 'up': 'r'})]
    e += [element([0, 0, 0], [1.5, 5, 1.5], {'all': 'i'}), element([14.5, 0, 0], [16, 5, 1.5], {'all': 'i'})] if stage == 0 else \
         [element([0, 0, 14.5], [1.5, 5, 16], {'all': 'i'}), element([14.5, 0, 14.5], [16, 5, 16], {'all': 'i'})]
    return model(e, t, 'iron')


def dresser_model(stage):
    t = {'w': 'dresser_wood', 'f': 'dresser_shut' if stage == 0 else 'dresser_out', 'd': 'drawer_side'}
    e = [element([1, 0, 3], [15, 16, 16], {'all': 'w', 'north': 'f'})]
    if stage == 1:
        for y0 in (1, 6, 11):
            e.append(element([2, y0, -3], [14, y0 + 4, 3], {'all': 'd', 'north': 'f'}, uv={'north': [1, 15 - y0 - 4, 15, 15 - y0]}))
    return model(e, t, 'dresser_wood', False)


def book_model(stage):
    if stage == 0:
        return model([element([4, 0, 5], [12, 2, 11], {'all': 'c'})], {'c': 'book_cover'}, 'book_cover')
    return model([element([2, 0, 5], [14, 0.5, 11], {'all': 'c'}), element([2.5, 0.5, 5.5], [13.5, 1, 10.5], {'all': 'c', 'up': 'p'})],
                 {'c': 'book_cover', 'p': 'book_pages'}, 'book_pages')


def crucifix_model(stage):
    t = {'w': 'cross_wood', 'i': 'iron'}
    if stage == 0:
        e = [element([7, 3, 15], [9, 14, 16], {'all': 'w'}), element([5, 10, 15], [11, 12, 16], {'all': 'w'})]
    else:
        e = [element([7, 2, 15], [9, 13, 16], {'all': 'w'}), element([5, 3, 15], [11, 5, 16], {'all': 'w'})]
    e.append(element([7.5, 14, 15.5], [8.5, 15, 16], {'all': 'i'}))
    return model(e, t, 'cross_wood')


def nightstand_model(stage):
    t = {'w': 'table_wood', 'b': 'lamp_base', 's': 'lamp_shade', 'g': 'flask', 'k': 'cork'}
    e = [element([2, 9, 2], [14, 10, 14], {'all': 'w'}), element([2.5, 0, 2.5], [13.5, 9, 13.5], {'all': 'w'})]
    if stage == 0:
        e += [element([6, 10, 6], [10, 11, 10], {'all': 'b'}), element([7.5, 11, 7.5], [8.5, 15, 8.5], {'all': 'b'}),
              element([5, 15, 5], [11, 20, 11], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 's'})]
    else:
        e += [element([6, 10, 6], [10, 15, 10], {'all': 'g'}), element([7, 15, 7], [9, 17, 9], {'all': 'g'}), element([7, 17, 7], [9, 18, 9], {'all': 'k'})]
    return model(e, t, 'table_wood')


def radiator_model():
    e = [element([1, 1, 10], [15, 11, 15], {'all': 'r'}), element([2, 0, 11], [3, 1, 14], {'all': 'r'}), element([13, 0, 11], [14, 1, 14], {'all': 'r'})]
    return model(e, {'r': 'radiator'}, 'radiator', False)


def chair_model():
    t = {'w': 'chair_wood'}
    e = [element([2, 9, 2], [14, 10, 14], {'all': 'w'})]
    for x, z in ((2, 2), (12, 2), (2, 12), (12, 12)):
        e.append(element([x, 0, z], [x + 2, 9, z + 2], {'all': 'w'}))
    e += [element([2, 10, 12], [4, 20, 14], {'all': 'w'}), element([12, 10, 12], [14, 20, 14], {'all': 'w'}),
          element([4, 16, 12.5], [12, 19, 13.5], {'all': 'w'})]
    return model(e, t, 'chair_wood')


def hall_table_model(stage):
    t = {'w': 'table_wood', 'l': 'letter', 'b': 'bowl'}
    e = [element([1, 12, 8], [15, 13, 16], {'all': 'w'})]
    for x, z in ((1.5, 8.5), (13.5, 8.5), (1.5, 14.5), (13.5, 14.5)):
        e.append(element([x, 0, z], [x + 1, 12, z + 1], {'all': 'w'}))
    e.append(element([9, 13, 10], [13, 14.5, 14], {'all': 'b'}))
    if stage == 1:
        e.append(element([3, 13, 10], [8, 13.25, 14], {'all': 'l'}))
    return model(e, t, 'table_wood')


def hooks_model():
    e = [element([1, 12, 15], [15, 14, 16], {'all': 'h'}), element([2, 0, 12.5], [7, 12, 15], {'all': 'a'}), element([9, 5, 13], [13, 12, 15], {'all': 'b'})]
    return model(e, {'h': 'hook_board', 'a': 'coat_long', 'b': 'coat_small'}, 'hook_board')


def kitchen_table_model():
    t = {'w': 'table_wood', 'o': 'oilcloth', 'c': 'crockery'}
    e = [element([0, 11, 0], [16, 12, 16], {'all': 'w', 'up': 'o'})]
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        e.append(element([x, 0, z], [x + 2, 11, z + 2], {'all': 'w'}))
    e += [element([3, 12, 3], [6, 14.5, 6], {'all': 'c'}), element([10, 12, 10], [14, 12.75, 14], {'all': 'c'})]
    return model(e, t, 'table_wood')


def stove_model():
    return model([element([1, 0, 2], [15, 14, 16], {'all': 'e', 'north': 'f', 'up': 't'}), element([1, 14, 15], [15, 18, 16], {'all': 'e'})],
                 {'e': 'enamel', 'f': 'stove_front', 't': 'stove_top'}, 'enamel', False)


def sink_model():
    return model([element([0, 0, 3], [16, 11, 16], {'all': 'c'}), element([0, 11, 3], [16, 14, 16], {'all': 'e', 'up': 's'}),
                  element([7, 14, 13], [9, 18, 14.5], {'all': 'm'}), element([7, 17, 10], [9, 18, 13], {'all': 'm'})],
                 {'c': 'cupboard', 'e': 'enamel', 's': 'sink_top', 'm': 'metal'}, 'enamel')


def bulb_model():
    return model([element([7.5, 8, 7.5], [8.5, 16, 8.5], {'all': 'f'}), element([6.5, 4, 6.5], [9.5, 8, 9.5], {'all': 'g'})],
                 {'f': 'flex', 'g': 'bulb'}, 'bulb')


def streetlamp_model(stage):
    t = {'p': 'lamp_post', 'g': 'lamp_glass'}
    if stage == 0:
        return model([element([6, 0, 6], [10, 16, 10], {'all': 'p'})], t, 'lamp_post')
    return model([element([6.5, 0, 6.5], [9.5, 5, 9.5], {'all': 'p'}), element([4, 5, 4], [12, 11, 12], {'all': 'g', 'up': 'p', 'down': 'p'}),
                  element([3, 11, 3], [13, 12, 13], {'all': 'p'}), element([6, 12, 6], [10, 14, 10], {'all': 'p'})], t, 'lamp_glass')


def fitting_models():
    m = {'radiator_0': radiator_model(), 'chair_0': chair_model(), 'hooks_0': hooks_model(), 'kitchen_table_0': kitchen_table_model(),
         'stove_0': stove_model(), 'sink_0': sink_model(), 'bulb_0': bulb_model()}
    for s in range(2):
        m[f'bed_{s}'] = bed_model(s); m[f'dresser_{s}'] = dresser_model(s); m[f'book_{s}'] = book_model(s)
        m[f'crucifix_{s}'] = crucifix_model(s); m[f'nightstand_{s}'] = nightstand_model(s); m[f'hall_table_{s}'] = hall_table_model(s)
        m[f'streetlamp_{s}'] = streetlamp_model(s)
    return m


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/cold_room_{name}.png'] = im
    faces = {
        'iron': iron(), 'sheet': sheet(), 'ticking': ticking(), 'pillow': pillow(), 'strap': strap(),
        'dresser_wood': wood(75460, '#4a3424'), 'dresser_shut': dresser_front(False), 'dresser_out': dresser_front(True), 'drawer_side': drawer_side(),
        'book_cover': book_cover(), 'book_pages': book_pages(), 'cross_wood': cross_wood(), 'table_wood': wood(75461),
        'lamp_base': lamp_base(), 'lamp_shade': lamp_shade(), 'flask': flask_glass(), 'cork': cork(), 'radiator': radiator(),
        'chair_wood': chair_wood(), 'letter': letter(), 'bowl': bowl(), 'hook_board': hook_board(), 'coat_long': snow_coat(),
        'coat_small': coat(75462, '#5a6a7a'), 'oilcloth': oilcloth(), 'crockery': crockery(), 'enamel': enamel(), 'stove_top': stove_top(),
        'stove_front': stove_front(), 'sink_top': sink_top(), 'cupboard': cupboard(), 'metal': metal(), 'flex': flex(), 'bulb': bulb_glass(),
        'lamp_post': lamp_post(), 'lamp_glass': lamp_glass(),
    }
    for name, im in faces.items():
        out[f'block/cold_room/{name}.png'] = im
    return out


KINDS = ['bed', 'dresser', 'book', 'crucifix', 'nightstand', 'radiator', 'chair', 'hall_table', 'hooks', 'kitchen_table', 'stove', 'sink', 'bulb', 'streetlamp']
STAGES = {'bed': 2, 'dresser': 2, 'book': 2, 'crucifix': 2, 'nightstand': 2, 'hall_table': 2, 'streetlamp': 2}
STAGE_MAX = 4
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'cold_room_fitting': 'Rented House Fitting', 'cold_room_boards': 'Hall Boards', 'cold_room_runner': 'Stair Runner',
         'cold_room_paper': 'Faded Wallpaper', 'cold_room_plaster': 'Plaster Ceiling', 'cold_room_lino': 'Kitchen Linoleum',
         'cold_room_brick': 'House Brick', 'cold_room_render': 'Rendered Wall', 'cold_room_flags': 'Worn Flags',
         'cold_room_treads': 'Stair Treads', 'cold_room_steps': 'Outside Steps'}
STAIRS = {'cold_room_treads': 'cold_room_boards', 'cold_room_steps': 'cold_room_flags'}


def stairs_state(name):
    base = {'east': 0, 'south': 90, 'west': 180, 'north': 270}; v = {}
    for f in ['east', 'north', 'south', 'west']:
        for half in ['bottom', 'top']:
            for shape in ['inner_left', 'inner_right', 'outer_left', 'outer_right', 'straight']:
                suffix = '' if shape == 'straight' else '_' + shape.split('_')[0]
                y = base[f]
                if shape != 'straight':
                    y += (-90 if shape.endswith('left') else 0) if half == 'bottom' else (0 if shape.endswith('left') else 90)
                e = {'model': NS + f'block/{name}{suffix}', 'uvlock': True}
                if half == 'top':
                    e['x'] = 180
                if y % 360:
                    e['y'] = y % 360
                v[f'facing={f},half={half},shape={shape}'] = e
    return {'variants': v}


def files():
    out = {}
    for name in materials():
        out[f'models/block/cold_room_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/cold_room_{name}'}}
        out[f'blockstates/cold_room_{name}.json'] = {'variants': {'': {'model': NS + f'block/cold_room_{name}'}}}
    for name, tex in STAIRS.items():
        t = {k: NS + f'block/{tex}' for k in ('bottom', 'top', 'side')}
        for suffix, parent in (('', 'stairs'), ('_inner', 'inner_stairs'), ('_outer', 'outer_stairs')):
            out[f'models/block/{name}{suffix}.json'] = {'parent': 'minecraft:block/' + parent, 'textures': t}
        out[f'blockstates/{name}.json'] = stairs_state(name)
    for key, obj in fitting_models().items():
        out[f'models/block/cold_room/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/cold_room/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/cold_room_fitting.json'] = {'variants': variants}
    return out


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--check', action='store_true')
    args.add_argument('--out', default=None, help='write under this directory instead of the mod (for a look before it goes in)')
    opts = args.parse_args()
    check = opts.check
    base = Path(opts.out) / 'assets/the_oldest_house' if opts.out else A
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            if tex.startswith(P):
                assert f'block/cold_room/{tex[len(P):]}.png' in images, (key, tex)
    # The cross turned is not the cross upright; the open book shows its pages; the shut chest is not the open one.
    assert models['crucifix_0'] != models['crucifix_1'] and models['book_0'] != models['book_1'] and models['dresser_0'] != models['dresser_1']
    for rel, im in images.items():
        path = base / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = base / rel
        if check:
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    if opts.out and not check:
        print(f'Drew the cold room under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the cold room: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
