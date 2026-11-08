"""Growing Things (0.4.350): the Barretts' yard and kitchen on a Saturday, years before the filming.

Draws, in native Minecraft pixels from fixed seeds, every fitting of the growing_fitting block, its models facing north and turned by
its facing: shoots coming up where nothing was planted, in four heights (crossed planes, like vanilla's grass); the back step, bare with
a few shoots at its edges, grown over, and gone under; the kitchen floor's boards lifting in three stages, the shoots coming up through
the gaps; the kitchen window's frame, square, bowing in at the sill, and split with shoots through the bottom pane; the washing on the
line (a sheet, a shirt, a towel, a small dress) and the bare line; the kitchen table's two halves under their oilcloth; the girls'
card table's two halves; the kitchen counter by the hall door; the spade standing upright in the ground; the Saturday list on the gate
post; a child's drawing held to a fridge door by a magnet; and the washing basket, empty and full. Also the carried things' item
pictures: a plate with a name scratched on its rim, the supper pot, the loaf, and a dry seed head. Writes the models and block states
and the blocks' and items' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/growing/'


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


# ------------------------------------------------------------------------------------------------ faces
SHOOT = ['#d6dcae', '#c8d09a', '#e2e6c0', '#b8c486']


def shoots(height, seed):
    # Thin new shoots, pale for want of light, each with a bent head; the tallest lean. Drawn on a cutout plane, rooted at the bottom.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed)
    for x in (2, 5, 7, 10, 13):
        h = max(2, height - rnd.randint(0, 3)); lean = rnd.choice((-1, 0, 0, 1))
        for i in range(h):
            xx = x + (lean if i > h * .6 else 0)
            dot(im, xx, 15 - i, SHOOT[rnd.randrange(4)])
        top = 15 - h
        dot(im, x + lean + (1 if lean >= 0 else -1), top + 1, '#eef0d4'); dot(im, x + lean, top, '#f2f2dc')
        if h > 6:
            dot(im, x + (1 if lean <= 0 else -1), 15 - h // 2, '#c0ca90')
    return im


def boards(base='#a8865a', seed=3600):
    im = new(base=base, seed=seed, spread=4)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, shade(base, -30)[:3])
    rnd = random.Random(seed + 1)
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade(base, -14)[:3])
    return im


def boards_split():
    # The same boards torn along a seam, the gap dark with the shoots coming through it.
    im = boards(seed=3602)
    for y in range(6, 10):
        for x in range(16):
            dot(im, x, y, '#2a2018')
    for x in (2, 5, 9, 12, 14):
        for y in range(6, 10):
            if (x + y) % 2 == 0:
                dot(im, x, y, SHOOT[(x + y) % 4])
    return im


def step_wood():
    im = boards(base='#8a7a66', seed=3604)
    for x in range(16):
        dot(im, x, 0, '#b0a090')
    return im


def mossy(cover):
    # The step's tread with shoots across it: at the edges first, then over all of it.
    im = step_wood(); rnd = random.Random(3606 + cover)
    for y in range(16):
        for x in range(16):
            edge = min(x, 15 - x, y, 15 - y)
            if rnd.random() < (.5 if edge < 2 else .08) * (1 + cover * 2):
                dot(im, x, y, SHOOT[rnd.randrange(4)])
    return im


def growth():
    im = new(base='#c4cc94', seed=3608, spread=10)
    rnd = random.Random(3609)
    for _ in range(30):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(('#e6e8c8', '#9aa86a', '#d8dcae')))
    return im


def frame_wood():
    return new(base='#e2ddd0', seed=3610, spread=3)


def glass(cracked):
    # Window glass: mostly clear, a highlight streak; cracked, a star of fine lines from low on the pane.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(4):
        dot(im, 3 + i, 10 - i, '#ffffff', 150); dot(im, 4 + i, 10 - i, '#e8f0f4', 110)
    if cracked:
        for dx, dy in ((1, -1), (-1, -1), (1, 0), (-1, 0), (0, -1), (1, -2), (-2, -1)):
            for k in range(1, 7):
                dot(im, 8 + dx * k, 13 + dy * k, '#f8f8f8', 200)
    return im


def sheet():
    im = new(base='#ece8de', seed=3612, spread=3)
    for y in range(16):
        dot(im, 0, y, '#d4d0c4'); dot(im, 15, y, '#d4d0c4')
    for x in range(16):
        dot(im, x, 15, '#d8d4c8')
    return im


def shirt():
    im = new(base='#8ca4bc', seed=3613, spread=4)
    for y in range(16):
        dot(im, 8, y, '#6e86a0')
    for y in (3, 7, 11):
        dot(im, 9, y, '#e8e8e8')
    return im


def towel():
    im = new(base='#d89a6a', seed=3614, spread=4)
    for y in (2, 3, 12, 13):
        for x in range(16):
            dot(im, x, y, '#f0e0c8')
    return im


def dress():
    im = new(base='#c88aa0', seed=3615, spread=4)
    for y in range(0, 16, 4):
        for x in range(0, 16, 4):
            dot(im, x + (y // 4) % 2 * 2, y + 1, '#f4e4ea')
    return im


def oilcloth():
    # A checked oilcloth over the table top, worn through at the corner where the girls sit.
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(3616)
    for y in range(16):
        for x in range(16):
            dark = ((x // 4) + (y // 4)) % 2 == 0
            px[x, y] = shade('#b85a4e' if dark else '#ece4d4', rnd.randint(-4, 4))
    return im


def table_leg():
    return new(base='#7a5a3a', seed=3617, spread=4)


def card_top():
    im = new(base='#3e4a3a', seed=3618, spread=3)
    frame(im, 0, 0, 16, 16, '#2a2a26')
    return im


def counter_front():
    im = new(base='#d8d2c0', seed=3619, spread=3)
    frame(im, 1, 3, 15, 15, '#b4ae9c')
    dot(im, 12, 9, '#6a6a64'); dot(im, 12, 10, '#6a6a64')
    return im


def counter_top():
    return new(base='#a8a49a', seed=3620, spread=4)


def spade_iron():
    return new(base='#6a6c6e', seed=3621, spread=6)


def spade_handle():
    return new(base='#9a7a50', seed=3622, spread=4)


def list_paper():
    # The Saturday list in a grown-up's hand down the page, a child's pencil squeezed in at the bottom.
    im = new(base='#f2eee2', seed=3623, spread=2)
    for y in (2, 4, 6, 8, 10):
        for x in range(2, 14 - (y % 3) * 2):
            if (x + y) % 5:
                dot(im, x, y, '#3a3e5a')
    for x in range(3, 11):
        if x % 2:
            dot(im, x, 13, '#8a8a8a')
    return im


def post():
    return new(base='#7a6a52', seed=3624, spread=5)


def twine():
    return new(base='#c8b48a', seed=3625, spread=8)


def drawing():
    # A child's drawing: a house, a yard of tall strokes up to the windows, and a figure standing in them with its arms out.
    im = new(base='#f4f2ec', seed=3626, spread=2)
    frame(im, 0, 0, 16, 16, '#dcdad2')
    fill(im, 3, 4, 9, 9, '#c86a5a'); dot(im, 6, 3, '#c86a5a'); dot(im, 5, 3, '#c86a5a'); dot(im, 7, 3, '#c86a5a')
    dot(im, 5, 6, '#4a6a9a'); dot(im, 7, 6, '#4a6a9a')
    for x in range(1, 15):
        for y in range(9 + (x % 3), 15):
            if (x + y) % 2 == 0:
                dot(im, x, y, '#5a9a4a')
    for y in (8, 9, 10, 11):
        dot(im, 12, y, '#2a2a2a')
    dot(im, 12, 7, '#2a2a2a'); dot(im, 11, 9, '#2a2a2a'); dot(im, 13, 9, '#2a2a2a')
    dot(im, 2, 1, '#e0c040'); dot(im, 3, 1, '#e0c040'); dot(im, 2, 2, '#e0c040'); dot(im, 3, 2, '#e0c040')
    return im


def magnet():
    return new(base='#d04a3a', seed=3627, spread=6)


def wicker(full):
    im = new(base='#b89a62', seed=3628, spread=6)
    for y in range(0, 16, 2):
        for x in range(16):
            if (x + y // 2) % 3 == 0:
                dot(im, x, y, '#8a6e40')
    return im


def folded():
    im = new(base='#e8e4da', seed=3629, spread=3)
    for y in (3, 7, 11):
        for x in range(16):
            dot(im, x, y, '#ccc8bc')
    fill(im, 2, 8, 9, 11, '#8ca4bc'); fill(im, 10, 4, 15, 7, '#d89a6a')
    return im


# ------------------------------------------------------------------------------------------------ items
def plate_item():
    # A plain plate from above, its rim with a name scratched into it.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 7.2:
                im.putpixel((x, y), shade('#ece8de' if d > 5 else '#f6f4ee', 0))
            if 6.2 < d < 6.8:
                im.putpixel((x, y), shade('#c4c0b4', 0))
    for x in range(5, 11):
        if x % 2:
            dot(im, x, 2, '#7a766a')
    return im


def pot_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 3, 6, 13, 14, '#4a4c4e'); fill(im, 2, 5, 14, 6, '#6a6c6e'); fill(im, 6, 3, 10, 5, '#5a5c5e')
    fill(im, 0, 8, 2, 9, '#3a3c3e'); fill(im, 14, 8, 16, 9, '#3a3c3e')
    for x in range(4, 12):
        dot(im, x, 7, '#7a7c7e')
    return im


def loaf_item():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 2, 6, 14, 12, '#b0703a'); fill(im, 3, 5, 13, 6, '#c88448'); fill(im, 3, 12, 13, 13, '#8a5428')
    for x in (5, 8, 11):
        dot(im, x, 7, '#e0b070'); dot(im, x - 1, 8, '#e0b070')
    return im


def seed_head_item():
    # A dry seed head on its stalk, the seeds still held, papery.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(9):
        dot(im, 4 + i // 3, 15 - i, '#a89a6a')
    for y in range(1, 8):
        for x in range(6, 12):
            if (x + y) % 2 == 0 and abs(x - 8.5) + abs(y - 4) < 5:
                dot(im, x, y, '#d0c08a' if y % 3 else '#b8a874')
    return im


def textures():
    t = {
        'shoots_0': shoots(4, 3630), 'shoots_1': shoots(7, 3631), 'shoots_2': shoots(11, 3632), 'shoots_3': shoots(15, 3633),
        'boards': boards(), 'boards_split': boards_split(), 'step': step_wood(), 'step_mossy_0': mossy(0), 'step_mossy_1': mossy(2),
        'growth': growth(), 'frame': frame_wood(), 'glass': glass(False), 'glass_cracked': glass(True),
        'sheet': sheet(), 'shirt': shirt(), 'towel': towel(), 'dress': dress(), 'twine': twine(),
        'oilcloth': oilcloth(), 'leg': table_leg(), 'card_top': card_top(), 'counter_front': counter_front(), 'counter_top': counter_top(),
        'spade_iron': spade_iron(), 'spade_handle': spade_handle(), 'list': list_paper(), 'post': post(),
        'drawing': drawing(), 'magnet': magnet(), 'wicker': wicker(False), 'folded': folded(),
    }
    out = {f'block/growing/{k}.png': v for k, v in t.items()}
    out['item/growing_plate.png'] = plate_item()
    out['item/growing_pot.png'] = pot_item()
    out['item/growing_loaf.png'] = loaf_item()
    out['item/growing_seed_head.png'] = seed_head_item()
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None, shade_faces=True):
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
    if not shade_faces:
        e['shade'] = False
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def crossed(tex, height=16):
    # Two planes crossing on the diagonals, as vanilla draws grass.
    e = []
    for angle in (45, -45):
        e.append({'from': [0.8, 0, 8], 'to': [15.2, height, 8], 'shade': False,
                  'rotation': {'origin': [8, 8, 8], 'axis': 'y', 'angle': angle, 'rescale': True},
                  'faces': {'north': {'uv': [0, 16 - height, 16, 16], 'texture': '#' + tex}, 'south': {'uv': [0, 16 - height, 16, 16], 'texture': '#' + tex}}})
    return e


def hanging(tex, x0, x1, bottom):
    # A garment pegged to the line along the top of the block, hanging flat across it.
    return [element([0, 15, 7.75], [16, 15.5, 8.25], {'all': 't'}),
            element([x0, bottom, 7.9], [x1, 15, 8.1], {'north': tex, 'south': tex, 'east': tex, 'west': tex, 'up': tex, 'down': tex},
                    uv={'north': [x0, 16 - 15, x1, 16 - bottom], 'south': [x0, 16 - 15, x1, 16 - bottom]}),
            element([x0 + 1, 14.5, 7.6], [x0 + 2, 15.8, 8.4], {'all': 'p'}), element([x1 - 2, 14.5, 7.6], [x1 - 1, 15.8, 8.4], {'all': 'p'})]


def fitting_models():
    m = {}
    for stage, h in enumerate((5, 8, 12, 16)):
        m[f'shoot_{stage}'] = model(crossed('s', h), {'s': f'shoots_{stage}'}, f'shoots_{stage}')
    # The back step: a deep tread eight pixels up, its riser to the north.
    step = element([0, 0, 0], [16, 8, 16], {'all': 'w', 'up': 'm'})
    m['steps_0'] = model([step] + crossed('s', 5), {'w': 'step', 'm': 'step_mossy_0', 's': 'shoots_0'}, 'step')
    m['steps_1'] = model([step] + crossed('s', 8), {'w': 'step', 'm': 'step_mossy_1', 's': 'shoots_1'}, 'step')
    m['steps_2'] = model([element([0, 0, 0], [16, 8, 16], {'all': 'g'}), element([1, 8, 1], [15, 12, 15], {'all': 'g'})] + crossed('s', 16),
                         {'g': 'growth', 's': 'shoots_3'}, 'growth')
    # The kitchen floor, lying over the boards beneath it: flat, a ridge, a tent with the shoots in its gap, heaved and broken.
    m['floor_0'] = model([element([0, 0, 0], [16, 1, 16], {'all': 'b'})], {'b': 'boards'}, 'boards')
    m['floor_1'] = model([element([0, 0, 0], [16, 2, 16], {'all': 'b'}), element([0, 2, 5], [16, 4, 11], {'all': 'b'})], {'b': 'boards'}, 'boards')
    m['floor_2'] = model([element([0, 0, 0], [16, 4, 5], {'all': 'b'}), element([0, 0, 11], [16, 4, 16], {'all': 'b'}),
                          element([0, 4, 3], [16, 6.5, 6], {'all': 'b'}), element([0, 4, 10], [16, 6.5, 13], {'all': 'b'}),
                          element([0, 6.5, 5], [16, 9, 7.5], {'all': 'b'}), element([0, 6.5, 8.5], [16, 9, 11], {'all': 'b'})] + crossed('s', 12),
                         {'b': 'boards', 's': 'shoots_2'}, 'boards')
    m['floor_3'] = model([element([0, 0, 0], [16, 7, 4], {'all': 'b'}), element([0, 0, 12], [16, 7, 16], {'all': 'b'}),
                          element([0, 7, 3], [16, 11, 6], {'all': 'b', 'up': 'x'}), element([0, 7, 10], [16, 11, 13], {'all': 'b', 'up': 'x'}),
                          element([0, 11, 5], [16, 14, 7], {'all': 'b'}), element([0, 11, 9], [16, 14, 11], {'all': 'b'})] + crossed('s', 16),
                         {'b': 'boards', 'x': 'boards_split', 's': 'shoots_3'}, 'boards')
    # The kitchen window: a frame round a pane set in the wall; bowed, the sill pushed up in the middle; split, with shoots in the gap.
    def window(sill_bow, cracked):
        e = [element([0, 0, 6], [16, 2 + sill_bow, 10], {'all': 'f'}), element([0, 14, 6], [16, 16, 10], {'all': 'f'}),
             element([0, 2, 6], [2, 14, 10], {'all': 'f'}), element([14, 2, 6], [16, 14, 10], {'all': 'f'}),
             element([7.5, 2 + sill_bow, 7], [8.5, 14, 9], {'all': 'f'}),
             element([2, 2 + sill_bow, 7.9], [14, 14, 8.1], {'north': 'g', 'south': 'g'})]
        if sill_bow:
            e.append(element([5, 2, 6], [11, 2 + sill_bow + 1, 10], {'all': 'f'}))
        return e
    m['frame_0'] = model(window(0, False), {'f': 'frame', 'g': 'glass'}, 'frame')
    m['frame_1'] = model(window(2, False), {'f': 'frame', 'g': 'glass'}, 'frame')
    m['frame_2'] = model(window(3, True) + [element([3, 5, 7], [13, 9, 9], {'north': 's', 'south': 's'})], {'f': 'frame', 'g': 'glass_cracked', 's': 'shoots_1'}, 'frame')
    m['washing_0'] = model(hanging('w', 0.5, 15.5, 1), {'w': 'sheet', 't': 'twine', 'p': 'post'}, 'sheet')
    m['washing_1'] = model(hanging('w', 3, 13, 6), {'w': 'shirt', 't': 'twine', 'p': 'post'}, 'shirt')
    m['washing_2'] = model(hanging('w', 2, 14, 5), {'w': 'towel', 't': 'twine', 'p': 'post'}, 'towel')
    m['washing_3'] = model(hanging('w', 4, 12, 4), {'w': 'dress', 't': 'twine', 'p': 'post'}, 'dress')
    m['line_0'] = model([element([0, 15, 7.75], [16, 15.5, 8.25], {'all': 't'})], {'t': 'twine'}, 'twine')
    # The kitchen table, two blocks long east to west: the west half (0) and the east half (1), oilcloth over the top.
    def table(east, top, leg, height=14, inset=1):
        lx = 15 - inset - 2 if east else inset
        return [element([0, height - 2, 0], [16, height, 16], {'all': top}),
                element([lx, 0, inset], [lx + 2, height - 2, inset + 2], {'all': leg}),
                element([lx, 0, 16 - inset - 2], [lx + 2, height - 2, 16 - inset], {'all': leg})]
    m['table_0'] = model(table(False, 'o', 'l'), {'o': 'oilcloth', 'l': 'leg'}, 'oilcloth')
    m['table_1'] = model(table(True, 'o', 'l'), {'o': 'oilcloth', 'l': 'leg'}, 'oilcloth')
    m['up_table_0'] = model(table(False, 'c', 'l', 12, 2), {'c': 'card_top', 'l': 'spade_iron'}, 'card_top')
    m['up_table_1'] = model(table(True, 'c', 'l', 12, 2), {'c': 'card_top', 'l': 'spade_iron'}, 'card_top')
    m['counter_0'] = model([element([0, 0, 1], [16, 14, 16], {'all': 'f', 'up': 't'}), element([0, 14, 0], [16, 16, 16], {'all': 't'})],
                           {'f': 'counter_front', 't': 'counter_top'}, 'counter_top')
    # The spade upright, its blade in the ground, the D of its handle at chest height.
    m['spade_0'] = model([element([5, 0, 7.5], [11, 6, 8.5], {'all': 'i'}), element([7.5, 6, 7.5], [8.5, 22, 8.5], {'all': 'h'}),
                          element([6, 22, 7.5], [10, 23, 8.5], {'all': 'h'}), element([6, 23, 7.5], [6.8, 25, 8.5], {'all': 'h'}),
                          element([9.2, 23, 7.5], [10, 25, 8.5], {'all': 'h'}), element([6, 25, 7.5], [10, 25.8, 8.5], {'all': 'h'})],
                         {'i': 'spade_iron', 'h': 'spade_handle'}, 'spade_iron')
    m['note_0'] = model([element([6, 0, 6], [10, 16, 10], {'all': 'p'}),
                         element([4.5, 7, 5.5], [11.5, 15, 6], {'north': 'l', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'}),
                         element([5, 13, 5.2], [11, 13.6, 5.6], {'all': 't'})],
                        {'p': 'post', 'l': 'list', 't': 'twine'}, 'list')
    # Held to the door of the fridge behind it (south of the block when facing north) by a magnet.
    m['drawing_0'] = model([element([3, 4, 15.6], [13, 14, 16], {'north': 'd', 'south': 'd', 'east': 'd', 'west': 'd', 'up': 'd', 'down': 'd'}),
                            element([7, 12.5, 15.2], [9, 14.5, 15.6], {'all': 'm'})],
                           {'d': 'drawing', 'm': 'magnet'}, 'drawing')
    def basket(full):
        e = [element([2, 0, 3], [14, 1, 13], {'all': 'w'}), element([2, 1, 3], [14, 7, 4], {'all': 'w'}), element([2, 1, 12], [14, 7, 13], {'all': 'w'}),
             element([2, 1, 4], [3, 7, 12], {'all': 'w'}), element([13, 1, 4], [14, 7, 12], {'all': 'w'})]
        if full:
            e.append(element([3, 1, 4], [13, 9, 12], {'all': 'c'}))
        return e
    m['basket_0'] = model(basket(False), {'w': 'wicker', 'c': 'folded'}, 'wicker')
    m['basket_1'] = model(basket(True), {'w': 'wicker', 'c': 'folded'}, 'wicker')
    return m


KINDS = ['shoot', 'steps', 'floor', 'frame', 'washing', 'line', 'table', 'up_table', 'counter', 'spade', 'note', 'drawing', 'basket']
STAGES = {'shoot': 4, 'steps': 3, 'floor': 4, 'frame': 3, 'washing': 4, 'table': 2, 'up_table': 2, 'basket': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'growing_fitting': 'Barretts\' Fitting'}
ITEMS = {'growing_plate': 'Plate', 'growing_pot': 'Supper Pot', 'growing_loaf': 'Loaf', 'growing_seed_head': 'Dry Seed Head'}


def files():
    out = {}
    for key, obj in fitting_models().items():
        out[f'models/block/growing/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/growing/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/growing_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
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
            assert f'block/growing/{tex.split("growing/")[1]}.png' in images, (key, tex)
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
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **{f'item.the_oldest_house.{k}': v for k, v in ITEMS.items()}}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the Barretts\' yard: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
