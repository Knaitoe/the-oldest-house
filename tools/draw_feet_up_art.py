"""Feet Up (0.4.200): the family house the children's game is played in.

Draws, in native Minecraft pixels from fixed seeds: nine wall, floor, ceiling and roof materials; every fitting the game is
played over or round (one feet_up_fitting block, its models facing north and turned by its facing): the loose boards, the
mattress, the pots and their rail, the dumbwaiter's cage, the window seat, the counters, the stove, the fridge, the linen
press, three kinds of lamp lit and out, the bath, the rules on the hall table, the notebook, the chalk's four words, the
tally on the rafter, the blankets and the cobwebs. Writes the models and block states with them. --check verifies
everything already written.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/feet_up/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=5):
    """A texture; with a base colour, per-pixel tone noise over it, as vanilla's painted and wooden surfaces have."""
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, seed=0, spread=0, a=255):
    """Fill the pixels x0..x1-1, y0..y1-1."""
    rnd = random.Random(seed); px = im.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = shade(colour, rnd.randint(-spread, spread) if spread else 0, a)


def line(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        px[round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)] = shade(colour, 0, a)


# ---------------------------------------------------------------------------------------------------- materials
def planks(base, seam, seed, spread=5, knots=0):
    """Four boards to a block, their ends staggered, the grain in darker specks."""
    im = new(base=base, seed=seed, spread=spread); px = im.load(); rnd = random.Random(seed + 1)
    for r in range(4):
        y1 = r * 4 + 3
        for x in range(16):
            px[x, y1] = shade(seam, rnd.randint(-3, 3))
        end = (r * 5 + 3) % 16
        for y in range(r * 4, y1):
            px[end, y] = shade(seam, 2)
        for _ in range(7):
            x, y = rnd.randint(0, 15), rnd.randint(r * 4, y1 - 1)
            if x != end:
                px[x, y] = shade(base, -9)
    for _ in range(knots):
        x, y = rnd.randint(1, 14), rnd.choice((1, 5, 9, 13))
        px[x, y] = shade(seam, -6); px[x + 1, y] = shade(seam, 4)
    return im


def hall_tile():
    """Black and white quarry tiles, four to a side, the grout a little sunk."""
    im = new(); px = im.load(); rnd = random.Random(41)
    for y in range(16):
        for x in range(16):
            light = (x // 4 + y // 4) % 2 == 0
            if x % 4 == 0 or y % 4 == 0:
                px[x, y] = shade('#8d877b' if light else '#1f1e1d', rnd.randint(-3, 3))
            else:
                px[x, y] = shade('#dad4c5' if light else '#302e2d', rnd.randint(-4, 4))
    return im


def wallpaper(base, stripe, flower, leaf, seed):
    """Striped paper with a small sprig every eight pixels, the kind that was there when the family came."""
    im = new(base=base, seed=seed, spread=3); px = im.load()
    for x in (0, 8):
        for y in range(16):
            px[x, y] = shade(stripe, 0)
    for cx, cy in ((4, 4), (12, 12)):
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            px[cx + dx, cy + dy] = shade(flower, 0)
        px[cx, cy] = shade(flower, 28)
        px[cx + 1, cy + 2] = shade(leaf, 0); px[cx + 2, (cy + 2) % 16] = shade(leaf, 0); px[cx - 1, cy - 2] = shade(leaf, 0)
    return im


def plaster():
    im = new(base='#ebe6d8', seed=61, spread=3); px = im.load(); rnd = random.Random(62)
    for _ in range(18):
        px[rnd.randint(0, 15), rnd.randint(0, 15)] = shade('#ebe6d8', -22)
    x = 3
    for y in range(2, 13):
        px[x, y] = shade('#c9c3b3', 0); x = max(0, min(15, x + rnd.choice((-1, 0, 0, 1))))
    return im


def brick():
    im = new(base='#9a8d7d', seed=71, spread=4); px = im.load(); rnd = random.Random(72)
    for row in range(4):
        off = 0 if row % 2 == 0 else 4
        for col in range(-1, 3):
            x0 = col * 8 + off; tone = shade('#8e4a3a', rnd.randint(-16, 10))
            for y in range(row * 4, row * 4 + 3):
                for x in range(x0 + 1, x0 + 8):
                    if 0 <= x < 16:
                        px[x, y] = shade(tone, rnd.randint(-5, 5))
    return im


def roof():
    """The underside of the roof: sarking boards with a rafter across them."""
    im = planks('#4e3b2b', '#33261b', 81, spread=4)
    fill(im, 0, 6, 16, 10, '#6a533d', 82, 5); fill(im, 0, 6, 16, 7, '#7d6549'); fill(im, 0, 9, 16, 10, '#3f2f22')
    return im


def materials():
    return {
        'floor': planks('#5e3f27', '#3a261a', 11, spread=4, knots=2),
        'hall_tile': hall_tile(),
        'board': planks('#b48b5b', '#7f5d3b', 21, knots=1),
        'attic': planks('#8a7763', '#5a4c3e', 31, spread=7, knots=3),
        'paper': wallpaper('#e2d8c0', '#d2c7ac', '#b5807c', '#8c9a78', 51),
        'paper_dark': wallpaper('#8f8468', '#7f7459', '#7d5f5c', '#646e55', 52),
        'plaster': plaster(),
        'brick': brick(),
        'roof': roof(),
    }


# ---------------------------------------------------------------------------------------------------- the font
FONT = {
    'A': ('010', '101', '111', '101', '101'), 'B': ('110', '101', '110', '101', '110'), 'E': ('111', '100', '110', '100', '111'),
    'F': ('111', '100', '110', '100', '100'), 'I': ('1', '1', '1', '1', '1'), 'M': ('10001', '11011', '10101', '10001', '10001'),
    'O': ('111', '101', '101', '101', '111'), 'R': ('110', '101', '110', '101', '101'), 'S': ('111', '100', '111', '001', '111'),
    'T': ('111', '010', '010', '010', '010'), 'W': ('10001', '10001', '10101', '10101', '01010'),
}
CHALK = '#efe9da'


def chalk_pixel(im, x, y, rnd):
    """A chalk stroke's pixel: uneven, with the odd pixel skipped where the wall's grain took no chalk."""
    if 0 <= x < 16 and 0 <= y < 16 and rnd.random() > .08:
        im.load()[x, y] = shade(CHALK, rnd.randint(-10, 6), rnd.randint(185, 255))


def word(im, text, x0, y0, rnd):
    """Letters of their own widths, a pixel apart, as a child chalks them."""
    for ch in text:
        if ch == ' ':
            x0 += 2; continue
        for r, row in enumerate(FONT[ch]):
            for c, bit in enumerate(row):
                if bit == '1':
                    chalk_pixel(im, x0 + c, y0 + r, rnd)
        x0 += len(FONT[ch][0]) + 1


def chalk(stage):
    im = new(); rnd = random.Random(101 + stage)
    if stage == 0:  # BASE
        word(im, 'BASE', 0, 5, rnd)
        for x in range(1, 15):
            if rnd.random() < .6:
                chalk_pixel(im, x, 12, rnd)
    elif stage == 1:  # an arrow up and to the left: from the arm, not the seat
        for i in range(10):
            chalk_pixel(im, 12 - i, 13 - i, rnd)
        for i in range(5):
            chalk_pixel(im, 2 + i, 3, rnd); chalk_pixel(im, 2, 3 + i, rnd)
        chalk_pixel(im, 3, 4, rnd)
    elif stage == 2:  # a scrawl too small to read, three lines of it
        for y in (3, 7, 11):
            x = rnd.randint(0, 2)
            while x < 15:
                run = rnd.randint(1, 3)
                for i in range(run):
                    chalk_pixel(im, x + i, y + rnd.choice((0, 0, 1)), rnd)
                x += run + rnd.randint(1, 2)
    else:  # WAIT FOR ME, the last written, pressed hard
        word(im, 'WAIT', 0, 0, rnd); word(im, 'FOR', 0, 6, rnd); word(im, 'ME', 7, 11, rnd)
    return im


def tally():
    """Two columns of strokes under their initials, in gates of three with the bar through, and a third column with nothing in it."""
    im = new(); rnd = random.Random(111)
    word(im, 'A', 1, 0, rnd); word(im, 'W', 6, 0, rnd)
    for col, strokes in ((0, 7), (6, 8)):
        for group in range(2):
            y = 6 + group * 5; left = strokes - group * 4
            for i in range(min(3, left)):
                for yy in range(y, y + 4):
                    chalk_pixel(im, col + i * 2, yy, rnd)
            if left >= 4:
                for xx in range(col, col + 5):
                    chalk_pixel(im, xx, y + 2, rnd)
    return im


# ---------------------------------------------------------------------------------------------------- fittings
def loose_board():
    im = planks('#b48b5b', '#7f5d3b', 121)
    fill(im, 0, 7, 16, 9, '#2a1d12', 122, 4)
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        im.load()[x, y] = shade('#5b5a57', 0)
    return im


def ticking(seed, piping=False):
    im = new(base='#ebe5d6', seed=seed, spread=3); px = im.load()
    for x in (1, 5, 9, 13):
        for y in range(16):
            px[x, y] = shade('#7484a6', 0)
    for y in (7,):
        for x in range(16):
            px[x, y] = shade(px[x, y], -14)
    if piping:
        for x in range(16):
            px[x, 7] = shade('#9aa3b8', 0); px[x, 15] = shade('#8a93a8', 0)
    return im


def pot():
    """The side of a hanging pot at (0..8, 4..10): a navy rim over cream enamel; its base at (8..16, 8..16)."""
    im = new(); fill(im, 0, 4, 8, 10, '#e4e7e6', 131, 3); fill(im, 0, 4, 8, 5, '#2d3f66'); fill(im, 0, 9, 8, 10, '#b7bcbd')
    fill(im, 8, 8, 16, 16, '#dfe3e2', 132, 3); px = im.load()
    for x, y in ((10, 10), (13, 10), (10, 13), (13, 13), (11, 9), (12, 9), (9, 11), (9, 12), (14, 11), (14, 12), (11, 14), (12, 14)):
        px[x, y] = shade('#9aa0a3', 0)
    return im


def iron():
    im = new(base='#3d3b39', seed=141, spread=4)
    fill(im, 0, 0, 16, 1, '#5a5754'); return im


def cage():
    """The dumbwaiter's cage: a wooden frame and slats with the dark of the shaft between them."""
    im = new(); fill(im, 0, 0, 16, 16, '#5a4330', 151, 5)
    fill(im, 1, 1, 15, 15, '#000000', a=0)
    for x in (3, 7, 11):
        fill(im, x, 1, x + 2, 15, '#8a6a48', 152 + x, 6)
    return im


def cushion():
    im = new(base='#8f4f4c', seed=161, spread=4); px = im.load()
    for x, y in ((5, 8), (11, 8)):
        px[x, y] = shade('#5a2f2d', 0); px[x + 1, y] = shade('#a46b67', 0)
    for x in range(16):
        px[x, 0] = shade('#a86a66', 0)
    return im


def painted(base, seed, panel=None, spread=3):
    im = new(base=base, seed=seed, spread=spread)
    if panel:
        x0, y0, x1, y1, tone = panel
        for x in range(x0, x1 + 1):
            im.load()[x, y0] = shade(tone, 0); im.load()[x, y1] = shade(tone, 0)
        for y in range(y0, y1 + 1):
            im.load()[x0, y] = shade(tone, 0); im.load()[x1, y] = shade(tone, 0)
    return im


def counter_top():
    im = new(base='#e6e1d1', seed=171, spread=3); px = im.load(); rnd = random.Random(172)
    for _ in range(22):
        px[rnd.randint(0, 15), rnd.randint(1, 15)] = shade('#b9b4a4', 0)
    for x in range(16):
        px[x, 0] = shade('#c9c3b2', 0)
    return im


def doors(base, outline, knob, y0, y1, knob_y, seed):
    """Two cupboard doors across the face, their knobs at the meeting edge."""
    im = painted(base, seed)
    for x0, x1 in ((1, 7), (8, 14)):
        for x in range(x0, x1 + 1):
            im.load()[x, y0] = shade(outline, 0); im.load()[x, y1] = shade(outline, 0)
        for y in range(y0, y1 + 1):
            im.load()[x0, y] = shade(outline, 0); im.load()[x1, y] = shade(outline, 0)
    im.load()[6, knob_y] = shade(knob, 0); im.load()[9, knob_y] = shade(knob, 0)
    return im


def stove_top():
    im = new(base='#efeee6', seed=181, spread=2); px = im.load()
    for cx, cy, hot in ((4, 4, True), (11, 4, False), (4, 11, False), (11, 11, True)):
        for dx, dy in ((-2, 0), (2, 0), (0, -2), (0, 2), (-1, -1), (1, -1), (-1, 1), (1, 1)):
            px[cx + dx, cy + dy] = shade('#2b2a2a', 0)
        core = '#d8582a' if hot else '#4a4846'
        for dx, dy in ((0, 0), (-1, 0), (1, 0), (0, -1), (0, 1)):
            px[cx + dx, cy + dy] = shade(core, 0 if (dx, dy) != (0, 0) else 18)
    for x in range(16):
        px[x, 15] = shade('#8c8c88', 0)
    return im


def stove_front():
    im = new(base='#efeee6', seed=191, spread=2); px = im.load()
    for x in (3, 6, 9, 12):
        px[x, 3] = shade('#3b3a38', 0); px[x, 4] = shade('#6a6866', 0)
    fill(im, 2, 6, 14, 7, '#5a5856')
    for x in range(2, 14):
        px[x, 8] = shade('#b9b7ae', 0); px[x, 14] = shade('#b9b7ae', 0)
    for y in range(8, 15):
        px[2, y] = shade('#b9b7ae', 0); px[13, y] = shade('#b9b7ae', 0)
    fill(im, 4, 9, 12, 13, '#2d2b29', 192, 4); px[5, 10] = shade('#7a4a2a', 0); px[6, 10] = shade('#9a5a2e', 0)
    return im


def fridge(upper):
    im = new(base='#ebe8dd', seed=201 + upper, spread=2); px = im.load()
    if upper:
        for x in range(16):
            px[x, 11] = shade('#b9b6ab', 0); px[x, 0] = shade('#d7d4c8', 0)
        for y in range(4, 11):
            px[13, y] = shade('#b8bcbd', 0)
        px[13, 4] = shade('#8e9293', 0); px[13, 10] = shade('#8e9293', 0)
        px[3, 2] = shade('#8e9293', 0); px[4, 2] = shade('#8e9293', 0)
    else:
        for y in range(2, 9):
            px[13, y] = shade('#b8bcbd', 0)
        px[13, 2] = shade('#8e9293', 0); px[13, 8] = shade('#8e9293', 0)
        fill(im, 0, 14, 16, 16, '#4a4846', 203, 3)
    return im


def press(upper):
    im = doors('#6b472e', '#4a3120', '#c9a24a', 1 if upper else 0, 13 if upper else 12, 8, 211 + upper)
    px = im.load()
    for x0, x1 in ((2, 6), (9, 13)):
        y0, y1 = (3, 11) if upper else (2, 10)
        for x in range(x0, x1 + 1):
            px[x, y0] = shade('#7d5637', 0); px[x, y1] = shade('#7d5637', 0)
    if upper:
        fill(im, 0, 0, 16, 1, '#4a3120'); px[0, 1] = shade('#8a6540', 0)
        for x in range(16):
            px[x, 14] = shade('#5b3c26', 0); px[x, 15] = shade('#5b3c26', 0)
    else:
        fill(im, 0, 13, 16, 16, '#4a3120', 213, 3)
    return im


def wood(base, seam, seed):
    im = new(base=base, seed=seed, spread=4); px = im.load(); rnd = random.Random(seed + 1)
    for _ in range(14):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15); px[x, y] = shade(seam, 0)
    return im


def shade_cloth(lit):
    base = '#f3dca3' if lit else '#b5aa90'; trim = '#c7a86a' if lit else '#8f8669'
    im = new(base=base, seed=221 + lit, spread=3 if lit else 5); px = im.load()
    for y in range(16):
        k = 12 - abs(8 - y) * 3 if lit else 0
        for x in range(16):
            px[x, y] = shade(px[x, y], k)
    for x in range(16):
        px[x, 4] = shade(trim, 0); px[x, 9] = shade(trim, 0); px[x, 2] = shade(trim, 0); px[x, 15] = shade(trim, 0)
    return im


def glass(lit):
    im = new(base='#f6e9c7' if lit else '#c9c4b6', seed=231 + lit, spread=3); px = im.load()
    for x, y in ((6, 6), (7, 6), (6, 7)):
        px[x, y] = shade(px[x, y], 14 if lit else -6)
    return im


def bath_panel():
    im = new(base='#bfd0cf', seed=241, spread=3); px = im.load()
    for x in (0, 4, 8, 12):
        for y in range(16):
            px[x, y] = shade('#9fb0ae', 0)
    return im


def enamel(seed=251):
    im = new(base='#f0f1ec', seed=seed, spread=2); px = im.load()
    for x in range(16):
        px[x, 15] = shade('#d4d9d8', 0)
    return im


def rules_page():
    """Paper on the hall table, its words in pencil pressed hard: lines, a heading, numbers down the side."""
    im = new(base='#f2ecdc', seed=261, spread=2); px = im.load(); rnd = random.Random(262)
    for x in range(5, 11):
        px[x, 3] = shade('#3a3a3a', 0); px[x, 4] = shade('#6a6a6a', 0)
    for n, y in enumerate((6, 8, 10, 12)):
        px[4, y] = shade('#3a3a3a', 0)
        x = 6
        while x < 12:
            run = rnd.randint(1, 3)
            for i in range(run):
                if x + i < 12:
                    px[x + i, y] = shade('#3a3a3a', rnd.randint(-5, 25))
            x += run + 1
    return im


def notebook_cover():
    im = new(base='#2f3340', seed=271, spread=3); fill(im, 5, 5, 11, 10, '#e8e1cc', 272, 2)
    for x in range(6, 10):
        im.load()[x, 7] = shade('#8a8372', 0)
    return im


def notebook_pages():
    im = new(base='#f0ece0', seed=281, spread=2); px = im.load()
    for y in (14, 15):
        for x in range(16):
            px[x, y] = shade('#d9d3c4' if y == 14 else '#c6c0b1', 0)
    return im


def blankets(top):
    im = new(); rows = ((0, 6, '#6d7566'), (6, 11, '#7c6650'), (11, 16, '#7d4d47')) if top else ((11, 13, '#6d7566'), (13, 14, '#7c6650'), (14, 16, '#7d4d47'))
    for y0, y1, tone in rows:
        fill(im, 0, y0, 16, y1, tone, 291 + y0, 5)
        if top:
            fill(im, 0, y1 - 1, 16, y1, shade(tone, -22)[:3])
    if top:
        for x in range(3, 13):
            im.load()[x, 8] = shade('#8e7a62', 0)
    return im


def cobweb():
    im = new(); rnd = random.Random(301)
    for x in range(16):
        im.load()[x, 0] = shade('#ebebe6', 0, 200 - rnd.randint(0, 40))
    for x1 in (8, 4, 12):
        line(im, 0, 0, x1, 15, '#e4e4de', 150); line(im, 15, 0, 15 - x1, 15, '#e4e4de', 150)
    for y, span in ((4, 3), (8, 5), (12, 7)):
        for x in range(span, 16 - span):
            if rnd.random() > .2:
                im.load()[x, y] = shade('#e8e8e2', 0, 120 + rnd.randint(0, 50))
    return im


# ---------------------------------------------------------------------------------------------------- models
def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def cube(side, **faces):
    tex = {'side': side, **{k: v for k, v in faces.items()}}
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'side', **{k: k for k in faces}})], tex, side)


def plate(texture):
    return model([element([0, 0, 15.98], [16, 16, 16], {'north': 't'})], {'t': texture}, texture)


def fitting_models():
    m = {}
    m['loose_board_0'] = cube('board', up='loose_board')
    m['loose_board_1'] = model([element([1, 0, 2], [15, 2, 14], {'all': 'b', 'up': 't'}, {'angle': 22.5, 'axis': 'y', 'origin': [8, 1, 8]})], {'b': 'board', 't': 'loose_board'}, 'loose_board')
    m['mattress_0'] = model([element([0, 0, 0], [16, 9, 16], {'all': 's', 'up': 't'})], {'s': 'mattress_side', 't': 'mattress_top'}, 'mattress_top')
    side = [0, 4, 8, 10]; base = [8, 8, 16, 16]
    m['pot_0'] = model([element([4, 6, 4], [12, 12, 12], {'all': 'p'}, uv={'north': side, 'south': side, 'east': side, 'west': side, 'up': base, 'down': base}),
                        element([5, 12, 7.5], [11, 13, 8.5], {'all': 'i'}), element([7.5, 13, 7.5], [8.5, 16, 8.5], {'all': 'i'})], {'p': 'pot', 'i': 'iron'}, 'pot')
    m['pot_rail_0'] = model([element([0, 12, 6], [16, 14, 10], {'all': 'i'}), element([7.5, 0, 7.5], [8.5, 12, 8.5], {'all': 'i'})], {'i': 'iron'}, 'iron')
    m['cage_0'] = cube('cage', up='board', down='board')
    m['window_seat_0'] = model([element([0, 0, 0], [16, 8, 16], {'all': 's', 'up': 'c'})], {'s': 'seat_panel', 'c': 'cushion'}, 'cushion')
    m['counter_0'] = model([element([0, 0, 0], [16, 15, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'counter_side', 'f': 'counter_front', 't': 'counter_top'}, 'counter_front')
    m['stove_0'] = model([element([0, 0, 0], [16, 14, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'stove_side', 'f': 'stove_front', 't': 'stove_top'}, 'stove_top')
    for stage, half in ((0, 'lower'), (1, 'upper')):
        m[f'fridge_{stage}'] = cube('fridge_side', north='fridge_' + half)
        m[f'press_{stage}'] = cube('press_side', north='press_' + half, up='press_top', down='press_top')
    for stage, cloth in ((0, 'shade_lit'), (1, 'shade_out')):
        m[f'lamp_{stage}'] = model([element([6, 0, 6], [10, 1, 10], {'all': 'b'}), element([7.5, 1, 7.5], [8.5, 7, 8.5], {'all': 'b'}), element([5, 7, 5], [11, 12, 11], {'all': 'c'})],
                                   {'b': 'brass', 'c': cloth}, cloth)
        m[f'floor_lamp_{stage + 1}'] = model([element([7.5, 0, 7.5], [8.5, 4, 8.5], {'all': 'b'}), element([3, 3, 3], [13, 10, 13], {'all': 'c'})], {'b': 'brass', 'c': cloth}, cloth)
        m[f'ceiling_light_{stage}'] = model([element([3, 13, 3], [13, 16, 13], {'all': 's', 'down': 'g'})], {'s': 'ceiling_light_side', 'g': 'ceiling_light_lit' if stage == 0 else 'ceiling_light_out'}, 'ceiling_light_side')
    m['floor_lamp_0'] = model([element([5, 0, 5], [11, 1, 11], {'all': 'b'}), element([7.5, 1, 7.5], [8.5, 16, 8.5], {'all': 'b'})], {'b': 'brass'}, 'brass')
    tub = [element([0, 0, 0], [16, 2, 16], {'all': 'p', 'up': 'e'}), element([0, 2, 0], [16, 10, 3], {'all': 'p', 'south': 'e', 'up': 'e'}), element([0, 2, 13], [16, 10, 16], {'all': 'p', 'north': 'e', 'up': 'e'})]
    m['bath_0'] = model(tub + [element([0, 2, 3], [3, 10, 13], {'all': 'p', 'east': 'e', 'up': 'e'})], {'p': 'bath_panel', 'e': 'enamel'}, 'enamel')
    m['bath_1'] = model(tub + [element([13, 2, 3], [16, 10, 13], {'all': 'p', 'west': 'e', 'up': 'e'}), element([14, 10, 5.5], [15, 12, 6.5], {'all': 'c'}), element([14, 10, 9.5], [15, 12, 10.5], {'all': 'c'}),
                              element([12.5, 10, 7.5], [15, 11, 8.5], {'all': 'c'})], {'p': 'bath_panel', 'e': 'enamel', 'c': 'chrome'}, 'enamel')
    m['rules_page_0'] = model([element([3, 0, 2], [13, 1, 14], {'all': 'r'})], {'r': 'rules_page'}, 'rules_page')
    m['notebook_0'] = model([element([4, 0, 3], [12, 2, 13], {'all': 'p', 'up': 'c', 'down': 'c'})], {'p': 'notebook_pages', 'c': 'notebook_cover'}, 'notebook_cover')
    for stage in range(4):
        m[f'chalk_{stage}'] = plate(f'chalk_{stage}')
    m['tally_0'] = plate('tally')
    m['blankets_0'] = model([element([0, 0, 0], [16, 5, 16], {'all': 's', 'up': 't'})], {'s': 'blankets_side', 't': 'blankets_top'}, 'blankets_top')
    half = 8 * 2 ** .5 / 2
    m['cobweb_corner_0'] = model([element([round(12 - half, 3), 8, 11.99], [round(12 + half, 3), 16, 12.01], {'north': 'w', 'south': 'w'}, {'angle': 45, 'axis': 'y', 'origin': [12, 8, 12]})], {'w': 'cobweb'}, 'cobweb')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/feet_up/{k}.png'] = v
    out['block/feet_up/loose_board.png'] = loose_board()
    out['block/feet_up/mattress_top.png'] = ticking(311); out['block/feet_up/mattress_side.png'] = ticking(312, piping=True)
    out['block/feet_up/pot.png'] = pot(); out['block/feet_up/iron.png'] = iron(); out['block/feet_up/cage.png'] = cage()
    out['block/feet_up/cushion.png'] = cushion(); out['block/feet_up/seat_panel.png'] = painted('#d3ccb9', 321, (2, 9, 13, 14, '#b9b19e'))
    out['block/feet_up/counter_top.png'] = counter_top(); out['block/feet_up/counter_front.png'] = doors('#9fb3ad', '#7d8f8a', '#3b3a38', 3, 14, 8, 331)
    out['block/feet_up/counter_side.png'] = painted('#9fb3ad', 332, (2, 3, 13, 14, '#8ea19c'))
    out['block/feet_up/stove_top.png'] = stove_top(); out['block/feet_up/stove_front.png'] = stove_front(); out['block/feet_up/stove_side.png'] = painted('#efeee6', 341, spread=2)
    for upper in (0, 1):
        out[f'block/feet_up/fridge_{"upper" if upper else "lower"}.png'] = fridge(upper)
        out[f'block/feet_up/press_{"upper" if upper else "lower"}.png'] = press(upper)
    out['block/feet_up/fridge_side.png'] = painted('#e7e4d9', 351, spread=2)
    out['block/feet_up/press_side.png'] = wood('#6b472e', '#4a3120', 361); out['block/feet_up/press_top.png'] = wood('#704b31', '#4a3120', 362)
    out['block/feet_up/brass.png'] = new(base='#a98a3c', seed=371, spread=6)
    out['block/feet_up/shade_lit.png'] = shade_cloth(True); out['block/feet_up/shade_out.png'] = shade_cloth(False)
    out['block/feet_up/ceiling_light_lit.png'] = glass(True); out['block/feet_up/ceiling_light_out.png'] = glass(False)
    out['block/feet_up/ceiling_light_side.png'] = new(base='#d8d6cf', seed=381, spread=3)
    out['block/feet_up/bath_panel.png'] = bath_panel(); out['block/feet_up/enamel.png'] = enamel(); out['block/feet_up/chrome.png'] = new(base='#c7cbcc', seed=391, spread=8)
    out['block/feet_up/rules_page.png'] = rules_page(); out['block/feet_up/notebook_cover.png'] = notebook_cover(); out['block/feet_up/notebook_pages.png'] = notebook_pages()
    for stage in range(4):
        out[f'block/feet_up/chalk_{stage}.png'] = chalk(stage)
    out['block/feet_up/tally.png'] = tally()
    out['block/feet_up/blankets_top.png'] = blankets(True); out['block/feet_up/blankets_side.png'] = blankets(False)
    out['block/feet_up/cobweb.png'] = cobweb()
    return out


KINDS = ['loose_board', 'mattress', 'pot', 'pot_rail', 'cage', 'window_seat', 'counter', 'stove', 'fridge', 'press', 'lamp', 'floor_lamp', 'ceiling_light', 'bath',
         'rules_page', 'notebook', 'chalk', 'tally', 'blankets', 'cobweb_corner']
STAGES = {'loose_board': 2, 'fridge': 2, 'press': 2, 'lamp': 2, 'ceiling_light': 2, 'bath': 2, 'floor_lamp': 3, 'chalk': 4}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {}
    for name in materials():
        out[f'models/block/feet_up_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/feet_up_{name}.json'] = {'variants': {'': {'model': NS + f'block/feet_up_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/feet_up/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/feet_up/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/feet_up_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    for key in fitting_models():
        assert key.rsplit('_', 1)[0] in KINDS, key
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
    print(('Verified' if check else 'Drew') + f' the house: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
