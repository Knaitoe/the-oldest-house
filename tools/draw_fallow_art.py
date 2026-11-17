"""Fallow (0.4.672): a gas station and grocery on a two-lane road through wheat to the edge of sight, late in a dry summer.

Draws, in native Minecraft pixels from fixed seeds: twenty-six materials (the store's painted clapboard, its block plinth, its checked
linoleum, its beadboard ceiling and its tar-and-gravel roof; the forecourt's jointed concrete; the road's bleached asphalt, its painted
line and its gravel shoulders; the garage's corrugated iron and its stained floor; the keeper's trailer's ribbed sides, its coated roof
and its skirting; the yard's trodden earth, the field's furrowed soil, the pads' tracks and gravel; the storm cellar's fieldstone, its
earth floor and its timbered roof under the yard; the farmhouse's weathered boards, its shingles, its porch floor and its chimney brick;
the rig's trailer), the farmhouse's shingle stairs and the cellar's stone steps, and every fitting of the fallow_fitting block, its
models facing north and turned by its facing (FallowBlock lists them). 0.4.680: what the light leaves, the store and the canopy come
down in pieces (debris, a broken joist, the canopy's sheets and columns) and the plate windows blown in to their edges.

Writes the models and block states and the blocks' names. --check verifies everything; --out draws them somewhere else for a look.
"""
from pathlib import Path
import argparse
import json
import math
import random
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/fallow/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c1, c2, t):
    a, b = (rgb(c1) if isinstance(c1, str) else c1[:3]), (rgb(c2) if isinstance(c2, str) else c2[:3])
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


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
            px[x, y] = shade(colour, 0, a) if isinstance(colour, str) else colour


def dot(im, x, y, colour, a=255):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        im.putpixel((x, y), shade(colour, 0, a) if isinstance(colour, str) else colour)


def line(im, x0, y0, x1, y1, colour):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), colour)


def darken(im, x, y, k):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        p = im.getpixel((x, y))
        if p[3]:
            im.putpixel((x, y), shade(p, k, p[3]))


def speckle(im, rnd, n, colours, box=None):
    w, h = im.size
    x0, y0, x1, y1 = box or (0, 0, w, h)
    for _ in range(n):
        dot(im, rnd.randrange(x0, x1), rnd.randrange(y0, y1), rnd.choice(colours))


# ------------------------------------------------------------------------------------------------ lettering
# A narrow face for small signs (three pixels wide, four high), a plain one a row taller, and a broad one for the station's name.
SMALL = {
    'A': ['.#.', '#.#', '###', '#.#'], 'C': ['.##', '#..', '#..', '.##'], 'D': ['##.', '#.#', '#.#', '##.'], 'E': ['###', '##.', '#..', '###'],
    'G': ['###', '#..', '#.#', '###'], 'H': ['#.#', '###', '#.#', '#.#'], 'I': ['###', '.#.', '.#.', '###'], 'L': ['#..', '#..', '#..', '###'],
    'M': ['#.#', '###', '#.#', '#.#'], 'N': ['##.', '#.#', '#.#', '#.#'], 'O': ['###', '#.#', '#.#', '###'], 'P': ['###', '#.#', '###', '#..'], 'R': ['##.', '#.#', '##.', '#.#'],
    'S': ['.##', '#..', '..#', '##.'], 'T': ['###', '.#.', '.#.', '.#.'], 'U': ['#.#', '#.#', '#.#', '###'], 'Y': ['#.#', '.#.', '.#.', '.#.'],
    '0': ['###', '#.#', '#.#', '###'], '1': ['.#.', '##.', '.#.', '###'], '2': ['##.', '..#', '.#.', '###'], '3': ['##.', '.##', '..#', '##.'],
    '4': ['#.#', '#.#', '###', '..#'], '5': ['###', '##.', '..#', '##.'], '6': ['.##', '#..', '###', '###'], '7': ['###', '..#', '.#.', '.#.'],
    '8': ['###', '###', '#.#', '###'], '9': ['###', '###', '..#', '##.'], ' ': ['...', '...', '...', '...'], '.': ['.', '.', '.', '#'],
}
PLAIN = {
    'C': ['###', '#..', '#..', '#..', '###'], 'E': ['###', '#..', '##.', '#..', '###'], 'G': ['###', '#..', '#.#', '#.#', '###'],
    'I': ['###', '.#.', '.#.', '.#.', '###'], 'O': ['###', '#.#', '#.#', '#.#', '###'], 'R': ['##.', '#.#', '##.', '#.#', '#.#'],
    'L': ['#..', '#..', '#..', '#..', '###'], 'D': ['##.', '#.#', '#.#', '#.#', '##.'],
    'S': ['###', '#..', '###', '..#', '###'], ' ': ['...', '...', '...', '...', '...'],
}
BROAD = {
    'H': ['#...#', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'A': ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'R': ['####.', '#...#', '#...#', '####.', '#.#..', '#..#.', '#...#'],
    'L': ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
    'N': ['#...#', '##..#', '#.#.#', '#..##', '#...#', '#...#', '#...#'],
    "'": ['#', '#', '.', '.', '.', '.', '.'],
    'S': ['.####', '#....', '#....', '.###.', '....#', '....#', '####.'],
}


def text_width(s, font):
    return sum(len(font[ch][0]) for ch in s) + len(s) - 1


def write(im, x, y, s, font, colour, shadow=None):
    for ch in s:
        rows = font[ch]
        for j, row in enumerate(rows):
            for i, c in enumerate(row):
                if c == '#':
                    if shadow:
                        dot(im, x + i + 1, y + j + 1, shadow)
                    dot(im, x + i, y + j, colour)
        x += len(rows[0]) + 1
    return x


def centre(im, y, s, font, colour, shadow=None, width=None):
    w = width or im.size[0]
    return write(im, (w - text_width(s, font)) // 2, y, s, font, colour, shadow)


# ------------------------------------------------------------------------------------------------ materials
def clapboard(base, seed, peel, wood='#8f8778'):
    # Lapped boards, four to a block, each lit on its face and shadowed under the lap of the one above; the paint chalked and
    # sun-faded, worn through to the grey wood in places along the laps and at the ends of the boards.
    im = new(base, seed, 3)
    rnd = random.Random(seed + 1)
    for b in range(4):
        y0 = b * 4
        for x in range(16):
            dot(im, x, y0, shade(base, -34)); dot(im, x, y0 + 1, shade(base, 10)); dot(im, x, y0 + 3, shade(base, -8))
    for _ in range(peel):
        x, y = rnd.randrange(16), rnd.choice((1, 2, 5, 6, 9, 10, 13, 14))
        n = rnd.randint(1, 3)
        for i in range(n):
            dot(im, x + i, y, shade(wood, rnd.randint(-6, 6)))
        dot(im, x - 1, y, shade(base, 14))
    # a nail head in the middle of each board's run
    for b in range(4):
        dot(im, (b * 7 + 3) % 16, b * 4 + 2, shade(base, -46))
    return im


def siding():
    return clapboard('#d8cfb5', 72001, 9)


def farm_siding():
    im = clapboard('#c9c6bb', 72002, 18, '#7f7a70')
    rnd = random.Random(72003)
    for _ in range(5):  # weather streaks down from the laps
        x = rnd.randrange(16); y = rnd.choice((2, 6, 10))
        for k in range(rnd.randint(1, 3)):
            darken(im, x, y + k, -10)
    return im


def plinth():
    # Painted concrete block, courses of eight, joints raked a little, the paint scuffed at the foot.
    im = new('#a39e93', 72010, 3)
    rnd = random.Random(72011)
    for y in (7, 15):
        for x in range(16):
            dot(im, x, y, '#7c786f')
    for x in (3, 11):
        for y in range(0, 7):
            dot(im, x, y, '#7c786f')
    for x in (7, 15):
        for y in range(8, 15):
            dot(im, x, y, '#7c786f')
    speckle(im, rnd, 14, ['#8e897f', '#b4afa4'])
    for x in range(16):
        if rnd.random() < .5:
            dot(im, x, 14, '#8a857a')
    return im


def lino():
    # Linoleum tiles, eight pixels square, two tones laid checkerwise, a path worn paler across them, wax gone dull in the grooves.
    im = new(None)
    rnd = random.Random(72020)
    for y in range(16):
        for x in range(16):
            light = ((x // 8) + (y // 8)) % 2 == 0
            base = '#cdc4a6' if light else '#8b9273'
            im.putpixel((x, y), shade(base, rnd.randint(-4, 3)))
    for i in range(16):
        dot(im, i, 0, '#77775f'); dot(im, 0, i, '#77775f'); dot(im, i, 8, '#8d8b70'); dot(im, 8, i, '#8d8b70')
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16)
        p = im.getpixel((x, y)); im.putpixel((x, y), shade(p, 9))
    for _ in range(4):  # black heel marks
        x, y = rnd.randrange(15), rnd.randrange(16)
        dot(im, x, y, '#4d4a40'); dot(im, x + 1, y, '#5b5848')
    return im


def ceiling():
    # Painted beadboard: narrow tongue-and-groove boards with a bead along each, a water ring in one corner.
    im = new('#d6d0bd', 72030, 2)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#b0aa98'); dot(im, x + 1, y, '#e1dccb')
    rnd = random.Random(72031)
    for a in range(0, 360, 20):
        x, y = round(11 + 3 * math.cos(math.radians(a))), round(11 + 2.5 * math.sin(math.radians(a)))
        darken(im, x, y, -12)
    speckle(im, rnd, 4, ['#c6c0ac'])
    return im


def roof():
    # Tar and gravel: a dark membrane, sun-softened, with pale stones bedded in it.
    im = new('#4b4741', 72040, 4)
    rnd = random.Random(72041)
    speckle(im, rnd, 46, ['#8a847a', '#a39d91', '#6e6960', '#5c5750'])
    return im


def apron():
    # The forecourt's concrete, poured in squares a block across: a tooled joint on two edges, the edge lit beside it, fine cracks,
    # and oil dripped where cars have stood.
    im = new('#b4afa4', 72050, 4)
    rnd = random.Random(72051)
    speckle(im, rnd, 30, ['#a39e93', '#c3beb3', '#9a958a'])
    for i in range(16):
        dot(im, i, 0, '#7f7b72'); dot(im, 0, i, '#7f7b72'); dot(im, i, 1, '#c8c3b8'); dot(im, 1, i, '#c8c3b8')
    x, y = 5, 15
    for _ in range(9):
        darken(im, x, y, -26)
        x += rnd.choice((0, 1)); y -= 1
    for _ in range(2):
        cx, cy = rnd.randrange(4, 13), rnd.randrange(4, 13)
        for dx in range(-2, 3):
            for dy in range(-1, 2):
                if dx * dx + dy * dy * 2 <= 4 and rnd.random() < .85:
                    darken(im, cx + dx, cy + dy, -30 if abs(dx) < 2 else -16)
    return im


def asphalt():
    # Two-lane asphalt bleached by sun: grey aggregate in a faded binder, a crack sealed with a line of fresh tar.
    im = new('#615e58', 72060, 5)
    rnd = random.Random(72061)
    speckle(im, rnd, 40, ['#7a766e', '#8a857c', '#4f4c47', '#6c6862'])
    x = 3
    for y in range(16):
        dot(im, x, y, '#2f2d2a')
        if rnd.random() < .3:
            x = max(0, min(15, x + rnd.choice((-1, 1))))
    return im


def road_line():
    # The asphalt with the painted centre line along it, three pixels wide, worn down to the aggregate in flecks.
    im = asphalt()
    rnd = random.Random(72062)
    for x in range(16):
        for y in (6, 7, 8):
            if rnd.random() < .86:
                dot(im, x, y, shade('#d8b23e', rnd.randint(-10, 6)))
    return im


def gravel_tex(seed, base, stones):
    im = new(base, seed, 6)
    rnd = random.Random(seed + 1)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice(stones)
        dot(im, x, y, shade(c, 12)); dot(im, x + 1, y, c); dot(im, x, y + 1, shade(c, -16)); dot(im, x + 1, y + 1, shade(c, -22))
    return im


def shoulder():
    return gravel_tex(72070, '#8a8173', ['#a79e8f', '#9a8f7e', '#6d665b', '#b5ad9f'])


def gravel():
    return gravel_tex(72075, '#a29c91', ['#c2bcb1', '#b0aa9f', '#7f7a71', '#d0cbc1'])


def metal():
    # Corrugated iron, galvanized long ago: ridges four pixels apart catching the light on one flank, rivet heads along a purlin,
    # rust run down from each rivet.
    im = new(None)
    rnd = random.Random(72080)
    flank = [12, 4, -6, -14]
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade('#a6abab', flank[x % 4] + rnd.randint(-3, 3)))
    for x in (2, 10):
        dot(im, x, 5, '#5f625f'); dot(im, x, 4, '#c9cdcb')
        for k in range(rnd.randint(3, 8)):
            dot(im, x, 6 + k, mix(im.getpixel((x, 6 + k)), '#8a5a34', .55 - k * .05))
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, mix(im.getpixel((x, y)), '#7d5232', .35))
    return im


def garage_floor():
    im = new('#8f8b84', 72090, 4)
    rnd = random.Random(72091)
    speckle(im, rnd, 20, ['#7f7b74', '#9d9991'])
    for _ in range(3):
        cx, cy, r = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 4)
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r and rnd.random() < .9:
                    x, y = (cx + dx) % 16, (cy + dy) % 16
                    dot(im, x, y, mix(im.getpixel((x, y)), '#3b3833', .55 if dx * dx + dy * dy < r * r / 2 else .3))
    return im


def trailer():
    # Ribbed aluminium siding run sideways: a rib every four pixels, lit on its top and shadowed below, chalky paint, a band of the
    # trailer's trim across its middle, and road dirt splashed up from the bottom.
    im = new('#d9d6cb', 72100, 2)
    rnd = random.Random(72101)
    for y in range(16):
        for x in range(16):
            k = {0: 10, 1: 0, 2: -4, 3: -12}[y % 4]
            p = im.getpixel((x, y)); im.putpixel((x, y), shade(p, k))
    for y in (8, 9, 10, 11):
        for x in range(16):
            p = im.getpixel((x, y)); im.putpixel((x, y), mix(p, '#6f8f8a', .7))
    for x in range(16):
        for y in range(13, 16):
            if rnd.random() < .25 * (y - 12):
                darken(im, x, y, -18)
    return im


def trailer_roof():
    im = new('#c3bfb4', 72110, 3)
    rnd = random.Random(72111)
    for x in (0, 8):
        for y in range(16):
            dot(im, x, y, '#9e9a90'); dot(im, x + 1, y, '#d4d0c6')
    speckle(im, rnd, 10, ['#aaa59a', '#8f8a80'])
    return im


def skirting():
    # Painted plywood skirting nailed round the trailer's foot, a seam every half block, the paint gone at the ground.
    im = new('#8d8270', 72120, 4)
    rnd = random.Random(72121)
    for y in range(16):
        dot(im, 7, y, '#615847'); dot(im, 15, y, '#615847')
    for x in range(16):
        dot(im, x, 0, '#a99d88')
        for y in range(12, 16):
            if rnd.random() < .3:
                dot(im, x, y, shade('#6f6452', rnd.randint(-6, 6)))
    for _ in range(14):  # the grain of the ply
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#7c725f'); dot(im, x + 1, y, '#7c725f')
    return im


def yard():
    # Bare dry earth trodden hard: dust, a few pebbles, and the yellowed tufts of the grass that has given up.
    im = new('#8d7759', 72130, 5)
    rnd = random.Random(72131)
    speckle(im, rnd, 22, ['#a08a6a', '#7a6549', '#9c8b72'])
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#b5a98f'); dot(im, x + 1, y + 1, '#6c5a43')
    for _ in range(3):
        x, y = rnd.randrange(1, 15), rnd.randrange(2, 16)
        for dx, dy in ((0, 0), (-1, -1), (1, -1), (0, -2)):
            dot(im, x + dx, y + dy, rnd.choice(['#b7a66c', '#a9975d', '#c1b27a']))
    return im


def soil():
    # The field's soil between the rows: dry, a little crusted, furrowed along the drill rows, with stubble ends and straw in it.
    im = new('#7a6249', 72140, 5)
    rnd = random.Random(72141)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, shade('#5e4a36', rnd.randint(-4, 4)))
            dot(im, x, y - 1, shade('#8b7255', rnd.randint(-4, 4)))
    for _ in range(9):
        x, y = rnd.randrange(16), rnd.choice((0, 1, 4, 5, 8, 9, 12, 13))
        dot(im, x, y, rnd.choice(['#b9a462', '#a8934f', '#c9b475']))
    return im


def track():
    # A dirt track's rut, run down the middle of the block from the road to the gate: hard-packed and darker where the wheels go,
    # loose and lighter at its edges, a seam of weeds along one side.
    im = new('#8b7658', 72150, 5)
    rnd = random.Random(72151)
    for y in range(16):
        for x in range(4, 12):
            dot(im, x, y, shade('#6f5c44', rnd.randint(-5, 5)))
        for x in (4, 11):
            dot(im, x, y, shade('#5d4c38', rnd.randint(-3, 3)))
        if rnd.random() < .4:
            dot(im, rnd.choice((0, 1, 14, 15)), y, rnd.choice(['#8d8d4e', '#7c7f44', '#a29b5c']))
    return im


def cellar_stone():
    # Fieldstone laid up in lime mortar: rounded stones of several sizes, the mortar between them proud and pale.
    im = new('#a19a8a', 72160, 3)
    rnd = random.Random(72161)
    stones = []
    for _ in range(40):
        cx, cy, rx, ry = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 4), rnd.randint(1, 3)
        if all((cx - a) ** 2 + (cy - b) ** 2 > (rx + c) ** 2 for a, b, c in stones):
            stones.append((cx, cy, rx))
            c = rnd.choice(['#7d7466', '#8c8273', '#6e675c', '#857a69', '#91897a'])
            for dx in range(-rx, rx + 1):
                for dy in range(-ry, ry + 1):
                    if (dx / rx) ** 2 + (dy / max(ry, 1)) ** 2 <= 1:
                        k = 10 if dy < 0 and dx < 0 else -10 if dy > 0 else 0
                        dot(im, (cx + dx) % 16, (cy + dy) % 16, shade(c, k + rnd.randint(-3, 3)))
    return im


def cellar_floor():
    im = new('#5e4f3d', 72170, 5)
    rnd = random.Random(72171)
    speckle(im, rnd, 20, ['#6d5c48', '#4f4233', '#73624c'])
    return im


def cellar_ceiling():
    # The cellar's roof seen from under it: rough-sawn boards laid across, gapped, and a joist across them.
    im = new('#6a563f', 72180, 5)
    rnd = random.Random(72181)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#2e251c')
            if rnd.random() < .25:
                dot(im, x + rnd.randint(1, 3), y, '#7c6648')
    for y in range(6, 10):
        for x in range(16):
            dot(im, x, y, shade('#57452f', 8 if y == 6 else -8 if y == 9 else 0))
    return im


def shingle():
    # Asphalt shingles in courses of four, each tab five or six pixels wide, offset course to course, granules worn off the
    # weather edges, a few tabs lifted.
    im = new('#5a534b', 72190, 4)
    rnd = random.Random(72191)
    for c in range(4):
        y0 = c * 4
        for x in range(16):
            dot(im, x, y0 + 3, '#2f2b27'); dot(im, x, y0, shade('#5a534b', 8))
        off = (c * 3) % 6
        for x in range(off, 16, 6):
            for y in range(y0, y0 + 3):
                dot(im, x, y, '#3a3530')
    speckle(im, rnd, 26, ['#6b645b', '#4b453f', '#78706a'])
    return im


def porch():
    # Porch boards run toward the road, painted and worn bare where the feet go.
    im = new('#8f8b80', 72200, 3)
    rnd = random.Random(72201)
    for y in range(16):
        for x in (0, 4, 8, 12):
            dot(im, x, y, '#5f5c55')
    for y in range(4, 12):
        for x in range(16):
            if x % 4 and rnd.random() < .35:
                dot(im, x, y, shade('#9a8a6e', rnd.randint(-6, 6)))
    return im


def brick():
    im = new('#8a4c39', 72210, 6)
    rnd = random.Random(72211)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#a69c8c')
    for row in range(4):
        off = 0 if row % 2 == 0 else 4
        for x in range(off, 16, 8):
            for y in range(row * 4, row * 4 + 3):
                dot(im, x, y, '#a69c8c')
    speckle(im, rnd, 14, ['#6e3a2b', '#9a5a44', '#5a2f23'])
    for x in range(16):  # soot toward the top
        for y in range(0, 3):
            if rnd.random() < .3:
                darken(im, x, y, -24)
    return im


def box():
    # A semi-trailer's side: aluminium sheet between posts every eight pixels, a rivet down each post, a rub rail of dirt along the foot.
    im = new('#dcdad3', 72220, 2)
    rnd = random.Random(72221)
    for x in (0, 8):
        for y in range(16):
            dot(im, x, y, '#b2b0a8'); dot(im, x + 1, y, '#e8e6df')
            if y % 4 == 2:
                dot(im, x, y, '#8c8a83')
    for x in range(16):
        for y in range(16):
            if rnd.random() < .04:
                darken(im, x, y, -8)
    return im


def materials():
    return {'siding': siding(), 'plinth': plinth(), 'lino': lino(), 'ceiling': ceiling(), 'roof': roof(), 'apron': apron(),
            'asphalt': asphalt(), 'road_line': road_line(), 'shoulder': shoulder(), 'metal': metal(), 'garage_floor': garage_floor(),
            'trailer': trailer(), 'trailer_roof': trailer_roof(), 'skirting': skirting(), 'yard': yard(), 'soil': soil(), 'track': track(),
            'gravel': gravel(), 'cellar_stone': cellar_stone(), 'cellar_floor': cellar_floor(), 'cellar_roof': cellar_ceiling(),
            'farm_siding': farm_siding(), 'farm_shingle': shingle(), 'porch': porch(), 'brick': brick(), 'box': box()}


# ------------------------------------------------------------------------------------------------ the store's fittings
def wood(base, seed, vertical=True, boards=4):
    im = new(base, seed, 3)
    rnd = random.Random(seed + 1)
    step = 16 // boards
    for i in range(0, 16, step):
        for j in range(16):
            x, y = (i, j) if vertical else (j, i)
            dot(im, x, y, shade(base, -24))
    for _ in range(18):
        a, b = rnd.randrange(16), rnd.randrange(16)
        n = rnd.randint(2, 4)
        for k in range(n):
            x, y = (a, b + k) if vertical else (a + k, b)
            darken(im, x, y, -7)
    return im


def counter_front():
    # The front of the counter, where the customers stand: knotty pine boards on end, varnished dark, a black kick plate scuffed by toes.
    im = wood('#8a6440', 72300)
    rnd = random.Random(72301)
    for x in range(16):
        for y in range(13, 16):
            dot(im, x, y, shade('#2f2a26', rnd.randint(-3, 3)))
        dot(im, x, 12, '#5a4026')
        if rnd.random() < .3:
            dot(im, x, 13 + rnd.randint(0, 1), '#5b5650')
    for _ in range(3):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 11)
        dot(im, x, y, '#5a3d22'); dot(im, x + 1, y, '#6b4a2b')
    return im


def counter_back():
    # The staff's side: open cubbies under the counter top, paper sacks folded in one, a stack of receipt books, a cigar box.
    im = new('#6e5135', 72310, 3)
    fill(im, 1, 2, 7, 13, '#2b2119'); fill(im, 9, 2, 15, 13, '#2b2119')
    fill(im, 2, 8, 6, 13, '#c4a679'); fill(im, 2, 8, 6, 9, '#d8c094')
    fill(im, 10, 9, 14, 13, '#8a8478'); fill(im, 10, 9, 14, 10, '#a8a296'); fill(im, 10, 11, 14, 12, '#7c776c')
    fill(im, 10, 5, 14, 7, '#6f3524'); dot(im, 11, 5, '#d4b26a')
    for y in range(16):
        dot(im, 0, y, '#4e3a26'); dot(im, 8, y, '#4e3a26'); dot(im, 15, y, '#4e3a26')
    for x in range(16):
        dot(im, x, 13, '#4e3a26'); dot(im, x, 14, '#3a2b1c'); dot(im, x, 15, '#3a2b1c')
    return im


def formica():
    # The counter's top: a speckled laminate gone pale in a band where hands lean and coins slide, cigarette burns near the register.
    im = new('#cfc7ad', 72320, 2)
    rnd = random.Random(72321)
    speckle(im, rnd, 26, ['#a89f84', '#b8af93', '#8f8f86', '#c9a77a'])
    for x in range(16):
        for y in range(6, 11):
            p = im.getpixel((x, y)); im.putpixel((x, y), shade(p, 6))
    dot(im, 12, 3, '#6b5038'); dot(im, 13, 3, '#8a6a48'); dot(im, 4, 13, '#6b5038')
    return im


def counter_edge():
    # The ribbed aluminium banding round the counter's edge.
    im = new(None)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade('#b5b9b9', (8, 0, -10, -2)[y % 4]))
    return im


def register():
    # A mechanical cash register: a cast body painted dark with chrome trim, the cash drawer at its foot, a crank on its side, the
    # sale flags standing in the window on top. This face is its side and back; the keys and the window have their own.
    im = new('#4a443e', 72330, 3)
    for x in range(16):
        dot(im, x, 0, '#9fa3a3'); dot(im, x, 11, '#b8bcbc'); dot(im, x, 15, '#2c2824')
    for x in range(1, 15):
        for y in range(12, 15):
            dot(im, x, y, '#3a352f')
    dot(im, 8, 13, '#c0a060'); dot(im, 7, 13, '#a88a4c')
    for y in range(3, 9):
        dot(im, 3, y, '#7a736a'); dot(im, 12, y, '#2e2a26')
    return im


def register_keys():
    # The keys, rows of round caps in columns of nine, cents to dollars, the column of the most-used worn bright.
    im = new('#2e2a26', 72340, 2)
    for col in range(5):
        for row in range(5):
            x, y = 1 + col * 3, 1 + row * 3
            c = '#e4dccb' if col < 3 else '#d2b780' if col == 3 else '#b8433a'
            if col == 1:
                c = '#f2ecdf'
            dot(im, x, y, c); dot(im, x + 1, y, shade(c, -18)); dot(im, x, y + 1, shade(c, -30)); dot(im, x + 1, y + 1, shade(c, -42))
    return im


def register_flags():
    # The window on top where the sale's amount stands up on little flags: a dark frame, the flags pale with numbers on them.
    im = new('#3a342e', 72350, 2)
    fill(im, 1, 3, 15, 12, '#eee6d2')
    for i, ch in enumerate('125'):
        write(im, 2 + i * 4, 5, ch, SMALL, '#2a2622')
    fill(im, 13, 5, 15, 9, '#b8433a')
    for x in range(1, 15):
        dot(im, x, 3, '#c8c0ac')
    return im


def candy():
    # The candy rack's front: three tiers of bars in their wrappers, and gum in packets, each a different paper.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72360)
    wraps = ['#b8352b', '#d7b13a', '#3a5e9a', '#5e3a22', '#e4dccb', '#2f7a4a', '#9a2f6a', '#d6762e']
    for tier, y0 in enumerate((1, 6, 11)):
        x = 1
        while x < 15:
            w = rnd.choice((2, 2, 3))
            c = rnd.choice(wraps)
            fill(im, x, y0, min(15, x + w), y0 + 4, shade(c, rnd.randint(-6, 6)))
            for xx in range(x, min(15, x + w)):
                dot(im, xx, y0, shade(c, 22))
            x += w
        for xx in range(16):
            dot(im, xx, y0 + 4, '#8e9296')
    for y in range(16):
        dot(im, 0, y, '#8e9296'); dot(im, 15, y, '#8e9296')
    return im


def wire():
    return new('#8e9296', 72370, 6)


def note():
    # Harlan's note: a sheet torn from a pad and folded so it stands against the register, in pencil, capitals pressed hard.
    im = new('#efe9d8', 72380, 2)
    rnd = random.Random(72381)
    for y in (3, 6, 9, 12):
        x = 2
        while x < 14:
            n = rnd.randint(2, 4)
            for k in range(n):
                if x + k < 14:
                    dot(im, x + k, y, '#4a4842' if rnd.random() < .85 else '#7a776e')
            x += n + 1
    for x in range(16):
        if x % 3 == 0:
            dot(im, x, 0, '#d6d0bf')
    return im


def stool_seat():
    # A stool's round seat in cracked vinyl, split across and mended with tape.
    im = new('#7b2f27', 72390, 4)
    rnd = random.Random(72391)
    for x in range(3, 13):
        dot(im, x, 7, '#3e1712'); dot(im, x, 8, '#9a4a3c')
    for x in range(5, 11):
        for y in range(6, 10):
            dot(im, x, y, shade('#a9a493', rnd.randint(-4, 4)))
    for _ in range(8):
        darken(im, rnd.randrange(16), rnd.randrange(16), -16)
    return im


def chrome():
    im = new(None)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade('#aeb4b7', (18, 6, -4, -12, -4, 6, 14, 2)[x % 8]))
    return im


def cabinet():
    # The cabinet under the back shelves: two plain doors, each with a turn button, a gap at the foot where the floor isn't level.
    im = wood('#7a5a3a', 72400, True, 2)
    for y in range(16):
        dot(im, 7, y, '#3a2a1a'); dot(im, 8, y, '#3a2a1a')
    for x in range(16):
        dot(im, x, 0, '#9a7a56'); dot(im, x, 15, '#2a1f14')
    dot(im, 5, 7, '#c8c0a8'); dot(im, 10, 7, '#c8c0a8')
    return im


def shelf_board():
    return wood('#8a6a46', 72410, False, 4)


def cartons():
    # The back shelf's stock: cartons of cigarettes stood on end, each brand its own band, cans of motor oil in a row.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72420)
    brands = [('#e4dccb', '#b8352b'), ('#e4dccb', '#2f5a8a'), ('#c9a75a', '#2a2622'), ('#e9e4d6', '#2f7a4a'), ('#b8352b', '#e4dccb')]
    x = 0
    while x < 16:
        a, b = rnd.choice(brands)
        fill(im, x, 2, min(16, x + 3), 16, a)
        fill(im, x, 6, min(16, x + 3), 8, b)
        dot(im, x, 2, shade(a, 16))
        for y in range(2, 16):
            dot(im, x, y, shade(a, -26))
        x += 3
    return im


def cans():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72430)
    labels = ['#3a5e9a', '#b8352b', '#2f7a4a', '#d7b13a', '#5a5550']
    for x0 in range(0, 16, 4):
        c = rnd.choice(labels)
        fill(im, x0, 5, x0 + 3, 16, c)
        fill(im, x0, 5, x0 + 3, 6, '#b5b9b9'); fill(im, x0, 15, x0 + 3, 16, '#8e9296')
        fill(im, x0, 8, x0 + 3, 11, shade(c, 40))
        dot(im, x0, 6, shade(c, 20))
    return im


def radio():
    # A table radio on the shelf: a case in cream plastic, a cloth grille, a dial with a needle, two knobs.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 3, 16, 16, '#d8ccb0')
    fill(im, 1, 5, 9, 14, '#8a7a5e')
    for y in range(5, 14):
        for x in range(1, 9):
            if (x + y) % 2:
                dot(im, x, y, '#6f6148')
    fill(im, 10, 5, 15, 9, '#e8dfc4')
    for x in range(10, 15):
        dot(im, x, 7, '#6a5e48')
    dot(im, 12, 6, '#b8352b'); dot(im, 12, 7, '#b8352b')
    fill(im, 10, 11, 12, 13, '#4a3e2e'); fill(im, 13, 11, 15, 13, '#4a3e2e')
    for x in range(16):
        dot(im, x, 3, '#efe6cf'); dot(im, x, 15, '#9a8e74')
    return im


def tv_case():
    return wood('#5a3e28', 72440, False, 2)


def tv_screen(stage):
    """The set's front: a wood surround, the curved screen inside it, and two dials on the right. The picture is the weather (0): a map
    with a front drawn across it and a low pressure; a card with nothing on it (1); or nothing (2)."""
    im = tv_case()
    fill(im, 1, 2, 11, 13, '#1a1a18')
    if stage == 0:
        fill(im, 2, 3, 10, 12, '#4f7a68')
        for x, y in ((3, 4), (4, 4), (5, 5), (6, 4), (8, 5), (8, 6), (9, 7), (3, 8), (4, 9), (6, 10), (7, 9)):
            dot(im, x, y, '#89a87a')
        for i in range(8):
            dot(im, 2 + i, 6 + (i % 3 == 0), '#d8e0e8')
        dot(im, 5, 8, '#e4e8ec'); dot(im, 6, 8, '#e4e8ec'); dot(im, 5, 9, '#e4e8ec')
        dot(im, 9, 3, '#e8d050'); dot(im, 9, 4, '#e8d050')
    elif stage == 1:
        fill(im, 2, 3, 10, 12, '#8a8e92')
        for x in range(2, 10):
            c = ['#c9c9c9', '#c9c445', '#45c1c5', '#45c145', '#c145c1', '#c14545', '#4545c1', '#c9c9c9'][x - 2]
            for y in range(3, 9):
                dot(im, x, y, c)
        fill(im, 2, 9, 10, 12, '#2a2a2a')
    else:
        fill(im, 2, 3, 10, 12, '#2e322f')
        dot(im, 3, 4, '#4a504c'); dot(im, 4, 4, '#4a504c'); dot(im, 3, 5, '#424844')
    fill(im, 12, 3, 15, 13, '#3a2a1c')
    dot(im, 13, 5, '#c8c0a8'); dot(im, 13, 9, '#c8c0a8'); dot(im, 14, 5, '#8a826e'); dot(im, 14, 9, '#8a826e')
    return im


def cooler_side():
    im = new('#d8d3c4', 72450, 2)
    for x in range(16):
        dot(im, x, 15, '#5a5650'); dot(im, x, 14, '#8a867c')
    return im


def cooler_door(top):
    """A glass-doored cooler, its chrome frame, its handle, and behind the beaded glass its stock: quarts of milk below, bottles of pop
    in rows above (the lower cell). The upper cell is the top of the door and the lit panel over it, one long panel along the run of
    doors: ICE over one door and COLD over the next (1, 2), and the plain lit panel over the others (3)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72460 + top)
    fill(im, 0, 0, 16, 16, '#b9bdbd')
    if not top:
        fill(im, 1, 1, 15, 15, '#2c3a3c')
        for x0 in (2, 6, 10):
            fill(im, x0, 10, x0 + 3, 15, '#efece2'); fill(im, x0, 10, x0 + 3, 11, '#3a6aa8')
        for x in range(2, 14):
            c = rnd.choice(['#6a2a1e', '#c8a24a', '#3a7a3a', '#8a3a2a'])
            fill(im, x, 3, x + 1, 8, c); dot(im, x, 2, '#b9bdbd')
        for x in range(1, 15):
            dot(im, x, 9, '#8e9296')
        handle_y = range(4, 12)
    else:
        fill(im, 0, 0, 16, 9, '#e8e2cf')
        # The panel's frame runs on from door to door: a chrome edge above and below, no edge at the sides.
        for x in range(16):
            dot(im, x, 0, '#b9bdbd'); dot(im, x, 8, '#9a9e9e')
        if top == 1:
            centre(im, 2, 'ICE', PLAIN, '#b8352b', shadow='#d4c9b0')
        elif top == 2:
            centre(im, 2, 'COLD', PLAIN, '#2f5a8a', shadow='#d4c9b0')
        else:
            for x in (3, 12):
                dot(im, x, 4, '#d4c9b0')
        fill(im, 1, 10, 15, 15, '#2c3a3c')
        for x in range(2, 14):
            c = rnd.choice(['#6a2a1e', '#c8a24a', '#3a7a3a'])
            fill(im, x, 11, x + 1, 15, c)
        handle_y = range(10, 15)
    for y in handle_y:
        dot(im, 14, y, '#e6e8e8')
    for _ in range(14):  # beads of cold on the glass
        x, y = rnd.randrange(1, 15), rnd.randrange(1 if not top else 10, 15)
        p = im.getpixel((x, y))
        if p[:3] != (185, 189, 189):
            im.putpixel((x, y), shade(p, 26))
    return im


def gondola_face(kind):
    """A double-sided shelving unit seen from either aisle: three shelves with their price-ticket lips, and on them canned goods (0),
    boxes of crackers and cereal and matches (1), or oil, wiper blades and spark plugs on a card (2)."""
    im = new('#5a5650', 72470 + kind, 2)
    rnd = random.Random(72475 + kind)
    for y0 in (0, 5, 10):
        for x in range(16):
            dot(im, x, y0 + 4, '#c8c4b8'); dot(im, x, y0 + 5 if y0 + 5 < 16 else 15, '#8a867c')
        x = 0
        while x < 16:
            if kind == 0:
                w = 3; c = rnd.choice(['#b8352b', '#d7b13a', '#3a5e9a', '#2f7a4a', '#e4dccb', '#c86a2a'])
                fill(im, x, y0 + 1, x + w - 1, y0 + 4, c); fill(im, x, y0 + 1, x + w - 1, y0 + 2, '#b5b9b9')
            elif kind == 1:
                w = rnd.choice((3, 4)); c = rnd.choice(['#d7b13a', '#b8352b', '#e4dccb', '#3a5e9a', '#7a4a2a'])
                fill(im, x, y0, x + w - 1, y0 + 4, c); dot(im, x + 1, y0 + 1, shade(c, 40))
            else:
                w = rnd.choice((2, 3)); c = rnd.choice(['#2f5a8a', '#d7b13a', '#2a2622', '#b8352b', '#e4dccb'])
                fill(im, x, y0 + 1, x + w - 1, y0 + 4, c); dot(im, x, y0 + 1, shade(c, 30))
            x += w
    return im


def gondola_end():
    # The shelving's end panel: perforated sheet steel, painted, scuffed by carts.
    im = new('#a8a89c', 72480, 2)
    for y in range(1, 16, 2):
        for x in range(1, 16, 2):
            dot(im, x, y, '#6e6e66')
    for x in range(16):
        dot(im, x, 15, '#4e4e48')
    return im


def gondola_top():
    im = gondola_face(1).crop((0, 0, 16, 16))
    for x in range(16):
        for y in range(0, 4):
            dot(im, x, y, '#a8a89c')
    return im


def maps():
    # The map rack's pockets, three rows, a folded road map standing in each, its cover printed with the state's name and a road.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72490)
    for y0 in (0, 5, 10):
        for x0 in (0, 5, 10):
            c = rnd.choice(['#e4dccb', '#d8c88a', '#b8d0d8', '#e0c8b0'])
            fill(im, x0, y0, x0 + 5, y0 + 5, c)
            fill(im, x0, y0, x0 + 5, y0 + 1, rnd.choice(['#b8352b', '#2f5a8a', '#2f7a4a']))
            line(im, x0 + 1, y0 + 4, x0 + 4, y0 + 2, '#b8352b')
        for x in range(16):
            dot(im, x, y0 + 4, '#8e9296')
    return im


def maps_header():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 11, '#e4dccb')
    for x in range(16):
        dot(im, x, 0, '#2f5a8a'); dot(im, x, 10, '#2f5a8a')
    centre(im, 1, 'ROAD', SMALL, '#2f5a8a')
    centre(im, 6, 'MAPS', SMALL, '#b8352b')
    return im


def coffee_stand():
    # The stand under the coffee: a painted cabinet, the paint ringed where cups have been set down wet.
    im = wood('#6f7a68', 72500, True, 2)
    for x in range(16):
        dot(im, x, 0, '#8e9a86'); dot(im, x, 15, '#3a4036')
    dot(im, 6, 8, '#c8c0a8'); dot(im, 9, 8, '#c8c0a8')
    return im


def coffee_machine():
    # The machine's front: brushed steel, its two warmers and their switches, a little lamp by the one that's on.
    im = chrome()
    for x in range(16):
        dot(im, x, 0, '#d0d4d6'); dot(im, x, 15, '#5a5e60')
    fill(im, 2, 9, 7, 11, '#2a2a28'); fill(im, 9, 9, 14, 11, '#2a2a28')
    dot(im, 3, 6, '#2a2a28'); dot(im, 10, 6, '#2a2a28'); dot(im, 4, 6, '#e05030')
    return im


def coffee_pot():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 2, 1, 14, 15, '#c8d4d4', 255)
    fill(im, 3, 6, 13, 14, '#3a2416')
    for x in range(3, 13):
        dot(im, x, 6, '#5a3a22')
    fill(im, 1, 0, 15, 2, '#2a2a28')
    for y in range(2, 15):
        dot(im, 2, y, '#e8f0f0')
    return im


def cups():
    im = new('#efebe0', 72510, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#d6d0c0')
    return im


def window_frame():
    im = new('#c9c2a8', 72520, 2)
    for x in range(16):
        dot(im, x, 0, '#e2dcc6'); dot(im, x, 15, '#8a846e')
    return im


def window_glass():
    # Plate glass, a little dusty, with a streak where it was wiped and a sun-faded decal in one corner. Mostly clear.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72530)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), (200, 210, 206, 46 + rnd.randint(-6, 6)))
    for i in range(10):
        dot(im, 3 + i, 12 - i, '#e8eeec', 90)
    fill(im, 12, 1, 15, 4, '#d8b23e', 120)
    return im


def tube_light():
    im = new('#f6f3e6', 72540, 1)
    for x in range(16):
        dot(im, x, 0, '#e2ddc8'); dot(im, x, 15, '#e2ddc8')
    return im


def signband(i):
    """One of the four boards that stand along the store's roof with the station's name on them. The name was painted across all
    four once they were up, so it runs evenly from board to board and a letter may cross the joint between two: broad letters with
    a shadow, three pixels apart and the apostrophe tucked in, on boards whose paint has gone chalky."""
    strip = new('#e3ddc8', 72550, 2, size=(64, 16))
    for x in range(64):
        dot(strip, x, 0, '#9a3328'); dot(strip, x, 1, '#b8352b'); dot(strip, x, 14, '#b8352b'); dot(strip, x, 15, '#9a3328')
    word, gaps = "HARLAN'S", [3, 3, 3, 3, 3, 1, 2]
    x = (64 - sum(len(BROAD[ch][0]) for ch in word) - sum(gaps)) // 2
    for k, ch in enumerate(word):
        write(strip, x, 4, ch, BROAD, '#b8352b', shadow='#7a7466')
        x += len(BROAD[ch][0]) + (gaps[k] if k < len(gaps) else 0)
    im = strip.crop((16 * i, 0, 16 * i + 16, 16))
    rnd = random.Random(72560 + i)
    for _ in range(8):
        x, y = rnd.randrange(16), rnd.randrange(2, 14)
        p = im.getpixel((x, y)); im.putpixel((x, y), shade(p, 12))
    return im


def signband_back():
    im = wood('#9a9488', 72570, True, 4)
    return im


# ------------------------------------------------------------------------------------------------ the forecourt
def pump_body(diesel):
    """A pump's cabinet: enamelled steel, a panel door with its louvres at the foot, a holster on its face where the nozzle hangs,
    the paint chalked by sun. The gasoline pumps are the station's own, the diesel pump set apart in another paint."""
    base = '#c9372b' if not diesel else '#3d6a3a'
    im = new(base, 72600 + diesel, 3)
    rnd = random.Random(72610 + diesel)
    for x in range(16):
        dot(im, x, 0, shade(base, 24)); dot(im, x, 15, shade(base, -40))
    for y in (11, 12, 13):
        for x in range(3, 13):
            dot(im, x, y, shade(base, -30 if y % 2 else -10))
    fill(im, 2, 2, 14, 9, shade(base, -12))
    fill(im, 4, 3, 12, 8, '#e8e2cf')
    centre(im, 4, 'DSL' if diesel else 'REG', SMALL, base)
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        p = im.getpixel((x, y)); im.putpixel((x, y), shade(p, 14))
    return im


def pump_head(diesel):
    """A pump's head: the lit panel along its top, and under it the dial window, its drums for the sale and the gallons rolled back
    to nothing."""
    base = '#c9372b' if not diesel else '#3d6a3a'
    im = new(base, 72620 + diesel, 3)
    fill(im, 1, 1, 15, 3, '#f2ecd6')
    for x in range(1, 15):
        dot(im, x, 2, '#ddd4b8')
    # The window: a chrome bezel round two rows of drums behind the glass, the sale above and the gallons below, each drum's
    # nought pale on the dark, the game's own five pixels high, and a red tick by each row for what it counts.
    fill(im, 1, 3, 15, 15, '#b9bdbd')
    fill(im, 2, 4, 14, 15, '#1e1e1c')
    for y0 in (4, 10):
        for x0 in (4, 9):
            write(im, x0, y0, 'O', PLAIN, '#ece6d2')
    dot(im, 1, 6, '#b8352b'); dot(im, 1, 12, '#b8352b')
    for x in range(16):
        dot(im, x, 15, shade(base, -40))
    return im


def pump_side(diesel):
    base = '#c9372b' if not diesel else '#3d6a3a'
    im = new(base, 72630 + diesel, 3)
    for y in range(16):
        dot(im, 0, y, shade(base, 18)); dot(im, 15, y, shade(base, -30))
    return im


def hose():
    im = new('#262422', 72640, 3)
    for y in range(16):
        dot(im, 0, y, '#3e3a36')
    return im


def nozzle():
    im = chrome()
    fill(im, 0, 10, 16, 16, '#2a2622')
    return im


def island_side():
    # The island's curb, concrete with its face painted in bands so the drivers see it, scraped where the tires rub.
    im = new('#b4afa4', 72650, 3)
    rnd = random.Random(72651)
    for x in range(16):
        c = '#e8dc6a' if (x // 4) % 2 == 0 else '#2a2622'
        for y in range(4, 16):
            dot(im, x, y, shade(c, rnd.randint(-6, 6)))
        if rnd.random() < .4:
            dot(im, x, 14 + rnd.randint(0, 1), '#8a857a')
    return im


def island_top():
    im = new('#b8b3a8', 72660, 3)
    rnd = random.Random(72661)
    speckle(im, rnd, 14, ['#a5a095', '#c6c1b6'])
    return im


def canopy_soffit():
    # The canopy's underside: sheet panels in their channels, a little rust at the seams, a wasps' nest stain.
    im = new('#d6d2c4', 72670, 2)
    rnd = random.Random(72671)
    for y in (0, 8):
        for x in range(16):
            dot(im, x, y, '#a6a294'); dot(im, x, y + 1, '#e6e2d4')
    for _ in range(4):
        x = rnd.randrange(16)
        dot(im, x, 1, '#9a7a5a')
    return im


def canopy_fascia():
    # The canopy's fascia, its edge band: a stripe of the station's paint between two of cream, as the pumps.
    im = new('#e3ddc8', 72680, 2)
    for x in range(16):
        for y in range(5, 11):
            dot(im, x, y, shade('#c9372b', -4 if y == 10 else 0))
        dot(im, x, 0, '#f0ead6'); dot(im, x, 15, '#9a948a')
    return im


def canopy_light():
    im = new('#fbf6e2', 72690, 2)
    for x in range(16):
        dot(im, x, 0, '#c8c4b4'); dot(im, x, 15, '#c8c4b4')
    for y in range(16):
        dot(im, 0, y, '#c8c4b4'); dot(im, 15, y, '#c8c4b4')
    return im


def column():
    # A canopy column: square steel tube, painted cream, chipped to primer where cars have kissed it, its foot in a concrete collar.
    im = new('#e3ddc8', 72700, 2)
    rnd = random.Random(72701)
    for y in range(16):
        dot(im, 0, y, '#f2ecd8'); dot(im, 15, y, '#a8a28e')
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#a84a34')
    return im


def column_marks():
    # The same, with the pencil marks up its side: a line and a date beside each, at a child's height and higher.
    im = column()
    for y in (4, 8, 12):
        for x in range(3, 8):
            dot(im, x, y, '#5a5650')
        for x in range(9, 13):
            if x % 2:
                dot(im, x, y, '#6e6a64')
    return im


def collar():
    im = new('#a8a398', 72710, 4)
    rnd = random.Random(72711)
    speckle(im, rnd, 12, ['#8e897f', '#bab5aa'])
    return im


def sign_pole():
    im = new(None)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade('#7a7e7e', (10, 4, -2, -10)[x % 4]))
    for x in range(16):
        if x % 5 == 0:
            dot(im, x, 7, '#8a5a34')
    return im


def name_board():
    """The station's sign by the road, three blocks across: HARLAN'S in broad letters with a shadow, and under it GROCERIES, on a
    board edged in the station's paint, the paint chalky and a rust run from each bolt."""
    im = Image.new('RGBA', (48, 16), (0, 0, 0, 0))
    rnd = random.Random(72720)
    for y in range(16):
        for x in range(48):
            im.putpixel((x, y), shade('#e8e1cb', rnd.randint(-3, 3)))
    for x in range(48):
        dot(im, x, 0, '#9a3328'); dot(im, x, 15, '#9a3328')
    for y in range(16):
        dot(im, 0, y, '#9a3328'); dot(im, 47, y, '#9a3328')
    centre(im, 1, "HARLAN'S", BROAD, '#b8352b', shadow='#8a8270')
    centre(im, 10, 'GROCERIES', PLAIN, '#2f4a6a')
    for x in (2, 45):
        for y in (2, 13):
            dot(im, x, y, '#6a5a48')
            for k in range(1, rnd.randint(2, 4)):
                darken(im, x, y + k, -16)
    return im


def price_board():
    """The price board under it: REGULAR, ETHYL and DIESEL each on a line in small capitals, and their prices in slotted numbers on
    cards in the slots, the tenth of a cent smaller and raised."""
    im = Image.new('RGBA', (48, 16), (0, 0, 0, 0))
    rnd = random.Random(72730)
    for y in range(16):
        for x in range(48):
            im.putpixel((x, y), shade('#f0ead6', rnd.randint(-3, 3)))
    for x in range(48):
        dot(im, x, 0, '#2f4a6a'); dot(im, x, 15, '#2f4a6a')
    for y in range(16):
        dot(im, 0, y, '#2f4a6a'); dot(im, 47, y, '#2f4a6a')
    for row, (name, price) in enumerate((('REGULAR', '35'), ('ETHYL', '39'), ('DIESEL', '32'))):
        y = 1 + row * 5
        write(im, 2, y, name, SMALL, '#2a2622')
        fill(im, 33, y, 42, y + 4, '#2a2622')
        write(im, 34, y, price, SMALL, '#f0ead6')
        write(im, 43, y, '9', SMALL, '#2a2622')
    return im


def sign_trim():
    return new('#9a3328', 72740, 3)


# ------------------------------------------------------------------------------------------------ the road and the field
def pole_wood():
    # A telephone pole: creosoted wood checked along its grain, darker at the foot, a tag nailed on it, step bolts.
    im = new('#5e4a36', 72800, 4)
    rnd = random.Random(72801)
    for x in (2, 6, 11, 14):
        for y in range(16):
            if rnd.random() < .7:
                dot(im, x, y, '#463626')
    for y in (4, 12):
        dot(im, 8, y, '#8e9296'); dot(im, 9, y, '#6e7276')
    return im


def crossarm():
    return wood('#7a6a54', 72810, False, 2)


def insulator():
    im = new('#7fb0a0', 72820, 4)
    for y in (3, 7, 11):
        for x in range(16):
            dot(im, x, y, '#5a8a7a')
    return im


def wire_tex():
    return new('#2a2826', 72830, 2)


def wheat(stage):
    """A crop sheaf seen side-on, drawn as vanilla draws its crops: ripe wheat, its stalks and leaves gone to straw, heavy heads with
    their awns standing up (0); the same with the heads bowed over (1); the stubble left after cutting, short and even (2); and the
    stubble burnt, black stalks, a little ash among them (3)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(72840 + stage)
    straw = ['#c9b06a', '#b89c56', '#d6bf7c', '#a88c4a']
    if stage >= 2:
        burnt = stage == 3
        for x in range(0, 16):
            if rnd.random() < .7:
                h = rnd.randint(2, 4)
                for y in range(16 - h, 16):
                    dot(im, x, y, rnd.choice(['#2a2622', '#3a342c', '#4a4238']) if burnt else rnd.choice(straw))
                if burnt and rnd.random() < .4:
                    dot(im, x, 15 - h, '#8a8680')
        return im
    for x in (1, 3, 5, 7, 9, 11, 13, 15):
        top = rnd.randint(2, 5)
        lean = rnd.choice((-1, 0, 0, 1))
        for y in range(top + 4, 16):
            xx = x + (lean if y < 10 else 0)
            dot(im, xx, y, rnd.choice(straw))
        if rnd.random() < .5:
            ly = rnd.randint(9, 13)
            dot(im, x + 1, ly, '#a89a5a'); dot(im, x + 2, ly - 1, '#9a8c4e')
        hx = x + lean
        if stage == 0:
            for y in range(top, top + 4):
                dot(im, hx, y, rnd.choice(['#d8b860', '#c8a450', '#e2c470']))
                if y % 2 == 0:
                    dot(im, hx - 1, y, '#b89448'); dot(im, hx + 1, y, '#b89448')
            dot(im, hx, top - 1, '#e8d8a0'); dot(im, hx - 1, top - 2, '#e8d8a0'); dot(im, hx + 1, top - 2, '#e8d8a0')
        else:
            for k in range(4):
                xx, yy = hx + k // 2 + 1, top + 2 + k
                dot(im, xx, yy, rnd.choice(['#d0b058', '#c09c48']))
                dot(im, xx + 1, yy, '#b08e44')
            dot(im, hx, top + 1, '#c8aa5a')
    return im


def chainlink():
    # Chain-link: galvanized wire woven in diamonds, one pixel wire on a clear field, new enough to shine.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                im.putpixel((x, y), shade('#c4c9cb', 8 if (x + y) % 8 == 0 else -6))
    return im


def galv():
    im = new(None)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade('#b8bdbf', (12, 4, -4, -12)[x % 4]))
    return im


def barb():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x in range(16):
        dot(im, x, 7, '#8e9296'); dot(im, x, 8, '#6e7276')
        if x % 5 == 2:
            dot(im, x, 6, '#a6aaae'); dot(im, x + 1, 9, '#a6aaae')
    return im


def gov_sign():
    # The sign wired to the gate: white enamel, a rim, lines of type too small to read from the track, one word larger than the rest.
    im = new('#ece8dc', 72850, 2)
    for x in range(16):
        dot(im, x, 0, '#b8352b'); dot(im, x, 15, '#b8352b')
    for y in range(16):
        dot(im, 0, y, '#b8352b'); dot(im, 15, y, '#b8352b')
    for y in (3, 5):
        for x in range(2, 14):
            if x % 4:
                dot(im, x, y, '#2a2622')
    centre(im, 7, 'NO', SMALL, '#b8352b')
    for y in (12, 13):
        for x in range(3, 13):
            if (x + y) % 3:
                dot(im, x, y, '#4a4842')
    return im


def flood(lit):
    im = new('#3a3c3e', 72860, 2)
    fill(im, 2, 2, 14, 14, '#f8f2d0' if lit else '#7a7c78')
    if lit:
        fill(im, 5, 5, 11, 11, '#ffffff')
    return im


def lid():
    # The pad's lid: one flat slab of concrete, finer than the forecourt's, with nothing growing on it.
    im = new('#b8b4ab', 72870, 2)
    rnd = random.Random(72871)
    speckle(im, rnd, 10, ['#a9a59c', '#c4c0b7'])
    return im


def lid_edge():
    # Its edge: the seam where it meets the frame round it, a dark line two pixels in, and the frame's steel lip outside it.
    im = lid()
    for x in range(16):
        dot(im, x, 2, '#3e3c38'); dot(im, x, 3, '#d4d0c6')
        for y in (0, 1):
            dot(im, x, y, '#8e9296')
    return im


def hatch():
    im = new('#7f8682', 72880, 2)
    for x in range(16):
        dot(im, x, 0, '#a2a8a4'); dot(im, x, 15, '#4e5450')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(im, x, y, '#4a4e4a'); dot(im, x, y - 1, '#b6bcb8')
    return im


# ------------------------------------------------------------------------------------------------ the garage
def lift_post():
    im = new('#c3a032', 72900, 3)
    for y in range(16):
        dot(im, 0, y, '#dcbb4c'); dot(im, 15, y, '#8a7020')
    for y in (4, 12):
        for x in range(16):
            dot(im, x, y, '#2a2622')
    return im


def lift_arm():
    im = new('#4a4844', 72910, 3)
    for x in range(16):
        dot(im, x, 0, '#6a6864')
    for x in range(2, 14, 4):
        fill(im, x, 0, x + 2, 3, '#1e1c1a')
    return im


def bench_top():
    # The workbench's top: oil-dark boards, a vise at one end, a rag, and on it the tools laid out in the order they were used.
    im = wood('#5a4630', 72920, False, 4)
    fill(im, 1, 2, 4, 6, '#3a4a5a'); fill(im, 1, 6, 3, 7, '#2a3a4a')
    fill(im, 5, 6, 15, 12, '#b8a888')
    for x in (6, 8, 10, 12):
        dot(im, x, 8, '#8e9296'); dot(im, x, 9, '#8e9296'); dot(im, x, 10, '#5a5e62')
    dot(im, 13, 7, '#b8352b'); dot(im, 13, 8, '#b8352b')
    return im


def bench_front():
    im = wood('#6a5236', 72930, True, 4)
    fill(im, 2, 3, 14, 7, '#5a4430')
    dot(im, 8, 5, '#c8c0a8')
    for x in range(16):
        dot(im, x, 2, '#3a2a1a'); dot(im, x, 7, '#3a2a1a')
    return im


def pegboard():
    """The pegboard over the bench: hardboard punched in a grid, each tool hung on its hooks over its outline drawn in marker so it
    goes back where it lives; two of the outlines are empty."""
    im = new('#b09060', 72940, 2)
    for y in range(1, 16, 2):
        for x in range(1, 16, 2):
            dot(im, x, y, '#6a5434')
    # a hammer and its outline
    line(im, 2, 2, 2, 9, '#2a2622'); fill(im, 1, 2, 4, 4, '#5a5e62'); line(im, 2, 4, 2, 9, '#7a5a3a')
    # two wrenches
    line(im, 6, 2, 6, 10, '#9ea2a6'); line(im, 8, 3, 8, 9, '#9ea2a6'); dot(im, 6, 1, '#7a7e82'); dot(im, 8, 2, '#7a7e82')
    # the empty outlines: a screwdriver and a pair of pliers, drawn round in marker with nothing in them
    for y in range(2, 10):
        dot(im, 11, y, '#2a2622')
    dot(im, 10, 2, '#2a2622'); dot(im, 12, 2, '#2a2622')
    for y in range(3, 11):
        dot(im, 13 + (y > 7), y, '#2a2622'); dot(im, 14 - (y > 7), y, '#2a2622')
    line(im, 2, 12, 14, 12, '#2a2622')
    return im


def tyre_side():
    im = new('#2a2826', 72950, 3)
    for y in range(16):
        for x in (2, 6, 10, 14):
            dot(im, x, y, '#3e3a36')
    for x in range(16):
        dot(im, x, 0, '#1e1c1a'); dot(im, x, 15, '#1e1c1a')
    return im


def tyre_top():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if 3.5 <= r <= 7.6:
                im.putpixel((x, y), shade('#2e2c2a', -8 if r > 6.8 else 0))
            elif r < 3.5:
                im.putpixel((x, y), shade('#141312', 0))
    return im


def drum():
    im = new('#3a5a7a', 72960, 4)
    rnd = random.Random(72961)
    for y in (2, 8, 14):
        for x in range(16):
            dot(im, x, y, '#2a4a6a'); dot(im, x, y + 1, '#4a6a8a')
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, mix(im.getpixel((x, y)), '#8a5a34', .5))
    return im


def rags():
    im = new('#8a8478', 72970, 8)
    rnd = random.Random(72971)
    speckle(im, rnd, 30, ['#b8352b', '#d8d0bc', '#5a5650', '#3a3632'])
    return im


# ------------------------------------------------------------------------------------------------ the yard
def trailer_window():
    # A window in the trailer's side: an aluminium frame, louvred glass, and drawn across it from inside a curtain with a print of
    # small flowers, faded where the sun reaches it.
    im = trailer()
    fill(im, 2, 3, 14, 13, '#c9cdcd')
    fill(im, 3, 4, 13, 12, '#e4d8bc')
    rnd = random.Random(72980)
    for _ in range(14):
        x, y = rnd.randrange(3, 13), rnd.randrange(4, 12)
        dot(im, x, y, rnd.choice(['#c8806a', '#7a9a6a', '#d0a060']))
    for x in (5, 8, 11):
        for y in range(4, 12):
            darken(im, x, y, -10)
    for y in (6, 9):
        for x in range(3, 13):
            dot(im, x, y, '#b8bcbc')
    return im


def hitch_metal():
    im = new('#3a3a38', 72990, 4)
    rnd = random.Random(72991)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, mix(im.getpixel((x, y)), '#8a5a34', .6))
    return im


def cinder():
    im = new('#9e9a92', 73000, 6)
    rnd = random.Random(73001)
    speckle(im, rnd, 30, ['#8a867e', '#b2aea6', '#77736c'])
    fill(im, 3, 4, 7, 12, '#5a5650'); fill(im, 9, 4, 13, 12, '#5a5650')
    for x in range(16):
        dot(im, x, 0, '#b8b4ac')
    return im


def propane():
    im = new('#e8e6dc', 73010, 2)
    for y in range(16):
        dot(im, 0, y, '#f6f4ec'); dot(im, 15, y, '#a8a69c')
    for x in range(16):
        dot(im, x, 8, '#c8c6bc')
    rnd = random.Random(73011)
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a87a5a')
    return im


def post_wood():
    return wood('#7a6a54', 73020, True, 4)


def clothes():
    """The washing on the line between the posts: the two lines themselves, a pin at each corner of what hangs on them, and on the one
    line a work shirt hung by its tails, its sleeves down; the other way round the same line carries a dish towel."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # a work shirt on the near line, hung by its tails, sleeves down, the collar lowest
    fill(im, 1, 1, 9, 9, '#6f8aa6')
    fill(im, 0, 1, 1, 6, '#6f8aa6'); fill(im, 9, 1, 10, 6, '#6f8aa6')
    for y in range(1, 9):
        dot(im, 5, y, '#5a7590')
    dot(im, 4, 3, '#e8e4d8'); dot(im, 4, 5, '#e8e4d8'); dot(im, 4, 7, '#e8e4d8')
    fill(im, 3, 8, 7, 10, '#5a7590')
    for x in (1, 8):
        dot(im, x, 0, '#c8b08a'); dot(im, x, 1, '#a8906a')
    # a dish towel, its stripe at its foot
    fill(im, 11, 1, 16, 7, '#ece8dc'); fill(im, 11, 5, 16, 6, '#b8352b')
    dot(im, 11, 0, '#c8b08a'); dot(im, 15, 0, '#c8b08a')
    return im


def line_rope():
    return new('#d8d0bc', 73030, 4)


def barrel():
    im = new('#4a3a2e', 73040, 6)
    rnd = random.Random(73041)
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#8a5a34', '#6a4a2e', '#2a2420', '#a86a3a']))
    for x, y in ((4, 9), (11, 5), (7, 12)):
        fill(im, x, y, x + 2, y + 2, '#141210')
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#2a2420')
    return im


def ash():
    im = new('#5a5652', 73050, 10)
    rnd = random.Random(73051)
    speckle(im, rnd, 30, ['#8a8680', '#2a2826', '#a8a49e', '#c86a3a'])
    return im


def field_fence():
    # Woven wire: horizontals closer together toward the foot, verticals every four pixels, a strand of barbed wire along the top,
    # sagging, rust at the knots.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in (1, 6, 10, 13, 15):
        for x in range(16):
            dot(im, x, y, '#8e9296')
    for x in range(1, 16, 4):
        for y in range(6, 16):
            dot(im, x, y, '#7e8286')
        dot(im, x, 6, '#8a5a34')
    for x in range(16):
        if x % 4 == 2:
            dot(im, x, 0, '#a6aaae'); dot(im, x, 2, '#a6aaae')
    return im


def cellar_door():
    """A leaf of the cellar's doors: tongue-and-groove boards laid along it, weathered grey and silvered, two strap hinges across the
    boards toward the outer edge, a ring pull near the meeting edge, and the hasp."""
    im = new('#8a8270', 73060, 4)
    rnd = random.Random(73061)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#4e483e'); dot(im, x + 1, y, '#a09884')
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        darken(im, x, y, -10)
    for y in (3, 12):
        for x in range(0, 7):
            dot(im, x, y, '#3a3632'); dot(im, x, y + 1, '#5a5650')
        dot(im, 1, y, '#8a5a34')
    fill(im, 13, 7, 15, 9, '#3a3632'); dot(im, 13, 7, '#8e9296')
    return im


def cellar_under():
    # The underside of a leaf, seen when it is thrown back: the boards' backs, and the battens and a brace nailed across them.
    im = new('#6e6656', 73070, 4)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#4a4438')
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#5a5244'); dot(im, x, y + 1, '#827a68')
    line(im, 1, 13, 14, 3, '#827a68'); line(im, 1, 12, 14, 2, '#5a5244')
    return im


def curb_concrete():
    im = new('#a8a398', 73080, 4)
    rnd = random.Random(73081)
    speckle(im, rnd, 14, ['#8e897f', '#bab5aa', '#7a8a5a'])
    for x in range(16):
        dot(im, x, 0, '#c2bdb2')
    return im


def jars():
    # A shelf of jars put up in summers past: peaches, beans, tomatoes, each lid dated in pencil, dust on the lids.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(73090)
    for x0 in (0, 4, 8, 12):
        c = rnd.choice(['#d89a4a', '#6a8a3a', '#b8352b', '#e0b860'])
        fill(im, x0, 3, x0 + 3, 11, c)
        fill(im, x0, 2, x0 + 3, 3, '#a8a49a')
        dot(im, x0, 4, shade(c, 40)); dot(im, x0, 5, shade(c, 30))
        for y in range(3, 11):
            dot(im, x0 + 2, y, shade(c, -24))
    for x in range(16):
        dot(im, x, 11, '#6a5236')
    return im


def tins():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 5, 4, 11, '#b5b9b9'); fill(im, 0, 7, 4, 9, '#b8352b')
    fill(im, 5, 6, 10, 11, '#d8c8a0'); fill(im, 5, 6, 10, 7, '#8a6a46')
    fill(im, 11, 4, 15, 11, '#c8d4d4'); fill(im, 11, 4, 15, 5, '#5a5650')
    dot(im, 12, 6, '#d84a2a'); dot(im, 13, 7, '#d84a2a')
    fill(im, 6, 3, 9, 6, '#ece8dc'); dot(im, 7, 4, '#b8352b')
    for x in range(16):
        dot(im, x, 11, '#6a5236')
    return im


def cot_canvas():
    im = new('#6e6c4e', 73100, 3)
    for x in range(16):
        dot(im, x, 0, '#5a583e'); dot(im, x, 15, '#5a583e')
    return im


def blanket():
    im = new('#7a4a3a', 73110, 3)
    for y in (3, 4, 11, 12):
        for x in range(16):
            dot(im, x, y, '#2a2622')
    return im


def crock():
    # A salt-glazed stoneware crock: grey with a brown rim, a number painted in cobalt on its side, a crack mended with wire.
    im = new('#b6b0a0', 73120, 3)
    for x in range(16):
        dot(im, x, 0, '#7a5a3a'); dot(im, x, 1, '#8a6a4a')
    centre(im, 5, '5', SMALL, '#3a4a8a')
    line(im, 11, 2, 13, 9, '#5a5650')
    return im


def lantern_glass(lit):
    # 0.4.681: turned low (2), the glass holds only a small flame low on the wick, and the light round it is a dim amber.
    if lit == 2:
        im = new('#9c8458', 73132, 2)
        fill(im, 7, 9, 9, 13, '#e8cc84')
        dot(im, 7, 9, '#c4a466'); dot(im, 8, 8, '#f6e2a6'); dot(im, 8, 12, '#fff0c0')
        for y in range(16):
            dot(im, 0, y, '#3a3632'); dot(im, 15, y, '#3a3632')
        return im
    im = new('#f8e0a0' if lit else '#8a8a7e', 73130 + lit, 2)
    if lit:
        fill(im, 6, 6, 10, 12, '#fff4c8')
    for y in range(16):
        dot(im, 0, y, '#3a3632'); dot(im, 15, y, '#3a3632')
    return im


def tin():
    return new('#6e6a60', 73140, 4)


# ------------------------------------------------------------------------------------------------ across the road
def farm_window():
    # A farmhouse window: a painted sash in a plain casing, six lights over six, the glass dark inside, a lace curtain at the top.
    im = farm_siding()
    fill(im, 2, 1, 14, 15, '#e8e4d8')
    fill(im, 3, 2, 13, 14, '#26282a')
    for x in (6, 9):
        for y in range(2, 14):
            dot(im, x, y, '#e8e4d8')
    for y in (5, 8, 11):
        for x in range(3, 13):
            dot(im, x, y, '#e8e4d8')
    for x in range(3, 13):
        if x % 2:
            dot(im, x, 2, '#d8d4c8'); dot(im, x, 3, '#c8c4b8')
    for x in range(2, 14):
        dot(im, x, 15, '#b8b4a8')
    return im


def porch_paint():
    im = new('#e2ded2', 73150, 2)
    for y in range(16):
        dot(im, 0, y, '#f0ece0'); dot(im, 15, y, '#a8a498')
    return im


def mailbox():
    im = new('#a8acae', 73160, 3)
    for x in range(16):
        dot(im, x, 0, '#c8ccce'); dot(im, x, 15, '#6a6e70')
    fill(im, 3, 6, 13, 9, '#d8d4c4')
    for x in range(4, 12):
        if x % 2:
            dot(im, x, 7, '#8a8678')
    return im


def mailbox_front():
    im = new('#a8acae', 73170, 3)
    for y in range(16):
        dot(im, 0, y, '#7a7e80'); dot(im, 15, y, '#7a7e80')
    fill(im, 6, 8, 10, 10, '#4a4e50')
    return im


def flag():
    return new('#b8352b', 73180, 3)


# ------------------------------------------------------------------------------------------------ the rig
def rig_paint():
    im = new('#7c2a22', 73200, 3)
    rnd = random.Random(73201)
    for x in range(16):
        dot(im, x, 0, '#9c4a3a')
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        darken(im, x, y, -14)
    return im


def rig_door():
    # The cab's door below its window: the paint, a pinstripe, the owner-operator's name lettered small in two lines, the handle.
    im = rig_paint()
    for x in range(16):
        dot(im, x, 2, '#d8b860')
    for y in (6, 9):
        for x in range(3, 13):
            if x % 3:
                dot(im, x, y, '#e8e0c8')
    fill(im, 12, 3, 15, 4, '#c8ccce')
    for y in range(16):
        dot(im, 0, y, '#4a1a14')
    return im


def rig_cab_side():
    # The door's upper half: its window, wound down a crack, a mirror's arm across it, and the sun visor seen inside.
    im = rig_paint()
    fill(im, 2, 2, 15, 13, '#2a3a42')
    fill(im, 3, 3, 14, 6, '#4a5a62')
    for x in range(2, 15):
        dot(im, x, 2, '#1a1a1a'); dot(im, x, 13, '#1a1a1a')
    for y in range(16):
        dot(im, 0, y, '#4a1a14')
    return im


def windshield():
    im = rig_paint()
    fill(im, 1, 3, 15, 14, '#2e3e46')
    for x in range(1, 15):
        dot(im, x, 3, '#151515')
    for y in range(3, 14):
        dot(im, 8, y, '#151515')
    line(im, 2, 12, 6, 8, '#5a6a72'); line(im, 9, 12, 13, 8, '#5a6a72')
    return im


def grille():
    # The radiator grille, chrome bars standing close together in a shell, bugs from the whole of the road on it.
    im = new('#2a2a28', 73210, 2)
    rnd = random.Random(73211)
    for x in range(0, 16, 2):
        for y in range(16):
            dot(im, x, y, shade('#c4c8ca', 6 if y < 8 else -6))
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#3a3426', '#6a5a3a']))
    return im


def tyre_tread():
    im = new('#262422', 73220, 3)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#141312')
    return im


def hub():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r <= 7.6:
                c = '#1e1c1a' if r > 5.6 else '#b8bcbe' if r > 2 else '#8e9296'
                im.putpixel((x, y), shade(c, 0))
    for a in range(0, 360, 45):
        dot(im, round(7.5 + 4 * math.cos(math.radians(a))), round(7.5 + 4 * math.sin(math.radians(a))), '#5a5e60')
    return im


def headlight():
    im = new('#e8e6d8', 73230, 2)
    for y in range(16):
        dot(im, 0, y, '#8e9296'); dot(im, 15, y, '#8e9296')
    for x in range(16):
        dot(im, x, 0, '#8e9296'); dot(im, x, 15, '#8e9296')
    fill(im, 5, 5, 9, 9, '#ffffff')
    return im


def sleeper_side():
    im = rig_paint()
    fill(im, 5, 4, 11, 8, '#2a3a42')
    for x in range(16):
        dot(im, x, 12, '#d8b860')
    for y in range(16):
        dot(im, 15, y, '#4a1a14')
    return im


def box_doors():
    """The trailer's back doors: two leaves of the same sheet as its sides, each with two lock rods running its height and their
    handles at waist height, hinges on the outer edge, a sealing wire tag on the latch, and the reflective tape along the foot."""
    im = box()
    for y in range(16):
        dot(im, 0, y, '#8e9296'); dot(im, 15, y, '#6e7276')
        for x in (5, 10):
            dot(im, x, y, '#a6aaae'); dot(im, x + 1, y, '#6e7276')
    for x in (5, 10):
        fill(im, x - 1, 8, x + 3, 9, '#8e9296')
    for y in (3, 12):
        fill(im, 0, y, 3, y + 1, '#5a5e62')
    dot(im, 7, 9, '#c8a040')
    return im


def box_frame():
    return new('#2e2c2a', 73240, 3)


def mud_flap():
    im = new('#1e1c1a', 73250, 2)
    for x in range(3, 13):
        dot(im, x, 6, '#a8acae')
    return im


# ------------------------------------------------------------------------------------------------ the afternoon (0.4.673)
def bean_cup():
    # A waxed paper cup from a school's lunch line, a little crushed at the rim: its side is laid out in the 6 x 6 square from (5, 9),
    # a ring of print round its middle and her pencil going downhill across it under the rim.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 5, 9, 11, 15, '#ece6d6')
    for x in range(5, 11):
        dot(im, x, 9, '#f8f4ea')
        dot(im, x, 12, '#7d93a8' if x % 2 else '#9fb1c2')
        dot(im, x, 14, '#d8d0bc')
    # her pencil: one faint stroke, not a mark, so that from a step away it is writing and not a pair of dark spots
    for x, y in ((5, 10), (6, 10), (7, 10), (8, 11), (9, 11), (10, 11)):
        dot(im, x, y, '#a8a49c')
    dot(im, 10, 13, '#d8d0bc')
    return im


def bean_soil():
    im = new('#4e3a26', 73310, 3)
    rnd = random.Random(73311)
    speckle(im, rnd, 6, ['#66503a', '#3a2a1c', '#7c6448'], (5, 5, 11, 11))
    dot(im, 7, 7, '#8e7656'); dot(im, 9, 9, '#33251a')
    return im


def bean_plant(stage):
    # The bean come up in the cup: its stem and its leaves, heart-shaped and veined, on a sheet that is clear everywhere else. Two
    # leaves; then a third and a fourth; then a tangle as high again as the cup; then, grown in the dark, long, thin and pale, its leaves
    # wide and its tendril reaching.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    stem, lit, dark, vein = ('#6f9a3c', '#8cbc4a', '#5a8430', '#b6d878') if stage < 3 else ('#b8c48a', '#d6dca4', '#a2ad74', '#eef0c8')

    def leaf(x, y, left):
        # a blade from its stalk at (x, y), broad near the stalk and pointed at the far end, tipped up a little, its midrib along it
        rows = ['..###..', '.#####.', '#######', '.######', '...###.', '.....#.']
        for j, row in enumerate(rows):
            for i, c in enumerate(row):
                if c == '#':
                    dot(im, x - i if left else x + i, y - 4 + j, dark if j >= 3 or (j == 2 and i == 6) else lit)
        for i in range(1, 6):
            dot(im, x - i if left else x + i, y - 2 + i // 3, vein)
    if stage == 0:
        line(im, 8, 15, 8, 11, stem)
        leaf(7, 12, True); leaf(9, 12, False)
    elif stage == 1:
        line(im, 8, 15, 8, 6, stem)
        leaf(7, 13, True); leaf(9, 11, False); leaf(8, 8, False)
    elif stage == 2:
        line(im, 8, 15, 8, 2, stem)
        leaf(7, 14, True); leaf(9, 12, False); leaf(7, 9, True); leaf(9, 6, False)
        dot(im, 7, 1, stem); dot(im, 6, 0, stem)
    else:
        line(im, 8, 15, 8, 1, stem)
        leaf(7, 14, True); leaf(9, 10, False); leaf(7, 6, True)
        for x, y in ((9, 1), (10, 0), (11, 1), (12, 0)):
            dot(im, x, y, stem)
    return im


# ------------------------------------------------------------------------------------------------ 0.4.680: what the light leaves
# The store come down: its own clapboard, beadboard, tar and gravel and joists, broken small and lying under a grey dust; the plate
# glass gone from its frames but for the edges; the canopy's sheets and columns down on the forecourt.
DEBRIS = [('#d8cfb5', 3, 2), ('#cfc6aa', 2, 1), ('#d6d0bd', 2, 3), ('#4b4741', 3, 2), ('#5c5750', 2, 2), ('#8a6a48', 4, 1),
          ('#6e5236', 1, 3), ('#a39e93', 2, 2)]


def debris_top():
    """The top of a fall of the store, seen from above: chips of painted clapboard and beadboard, lumps of the roof's tar and gravel,
    splinters of joist and a scrap of block, each with the dark of its own edge where it lies over what is under it, a glint or two
    of the plate glass, and the grey dust of it all settled in between."""
    im = new('#858077', 75010, 6)
    rnd = random.Random(75011)
    for _ in range(15):
        c, w, h = rnd.choice(DEBRIS)
        if rnd.random() < .5:
            w, h = h, w
        x, y = rnd.randrange(16), rnd.randrange(16)
        for dy in range(h):
            for dx in range(w):
                dot(im, (x + dx) % 16, (y + dy) % 16, shade(c, rnd.randint(-8, 3)))
        for dx in range(w):
            dot(im, (x + dx) % 16, (y + h) % 16, '#4f4b45')
        dot(im, (x + w) % 16, (y + h) % 16, '#45423d')
    speckle(im, rnd, 10, ['#a9a49a', '#9b968c'])
    for _ in range(3):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#e6eeec'); dot(im, x + 1, y, '#b8c4c2')
    return im


def debris_side():
    """The side of the fall: the pieces lying edge on, flat and broken, in a bank that darkens toward the ground, with the dust lying
    paler along its top."""
    im = new('#6f6a62', 75020, 5)
    rnd = random.Random(75021)
    for _ in range(18):
        c, w, h = rnd.choice(DEBRIS)
        w, h = max(w, h) + rnd.randint(0, 2), 1
        x, y = rnd.randrange(16), rnd.randrange(16)
        for dx in range(w):
            dot(im, (x + dx) % 16, y, shade(c, rnd.randint(-10, 0)))
        dot(im, x % 16, (y + 1) % 16, '#3f3c37')
    for y in range(16):
        for x in range(16):
            darken(im, x, y, -(y * 18) // 15)
    for x in range(16):
        dot(im, x, 0, shade('#9d988e', rnd.randint(-6, 6)))
    return im


def joist():
    """A roof joist broken out of its place: a length of plain timber, its grain along it, a nail head where a ceiling board was, and
    the splintered end where it broke, pale and torn."""
    im = new('#9a7a54', 75030, 3)
    rnd = random.Random(75031)
    for y in range(1, 15):  # the grain: long streaks along the timber, a knot in one
        if rnd.random() < .6:
            x0 = rnd.randrange(-4, 12)
            for x in range(x0, x0 + rnd.randint(5, 11)):
                darken(im, x, y, -16)
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        darken(im, 11 + dx, 9 + dy, -30)
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, '#6e5236')
    for y in range(1, 15):
        n = rnd.randint(0, 3)
        for x in range(n):
            dot(im, 15 - x, y, shade('#cdb48a', rnd.randint(-10, 6)))
    dot(im, 5, 7, '#3a3632'); dot(im, 9, 4, '#3a3632')
    return im


def window_shards(upper):
    """What is left of a plate window: the glass gone from the frame but for jagged pieces held at its edge, along the bottom of the
    lower half and the top of the upper, a few in from the sides, each with a glint along its broken edge. Mostly clear."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(75040 + upper)
    heights = []
    h = 3
    for x in range(16):
        h = max(0, min(7, h + rnd.choice((-2, -1, 0, 1, 1, 2))))
        if x in (0, 15):
            h = max(h, 5)
        heights.append(h)
    for x, h in enumerate(heights):
        for k in range(h):
            y = k if upper else 15 - k
            im.putpixel((x, y), (200, 210, 206, 70 + rnd.randint(-8, 8)))
        if h:
            y = h - 1 if upper else 16 - h
            im.putpixel((x, y), (236, 242, 240, 150))
    for y in range(16):  # a sliver left down each side, held by the stop
        for x in (0, 15):
            if im.getpixel((x, y))[3] == 0 and rnd.random() < .55:
                im.putpixel((x, y), (200, 210, 206, 64))
    return im


def milk_carton():
    # A quart of milk in a waxed carton, seen a little from one side: the front and its narrower side face, the roof sloping back
    # from the front to the folded fin along the top, the gable's triangle over the side, and a band of print round it with a drop.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 4, 6, 10, 15, '#f2efe6')
    fill(im, 10, 6, 12, 15, '#d6d2c6')
    fill(im, 4, 3, 10, 6, '#e4e0d4')
    for x in range(4, 10):
        dot(im, x, 3, '#d2cdc1')
    fill(im, 4, 1, 10, 3, '#f6f4ee')
    for y, x1 in ((2, 11), (3, 11), (4, 12), (5, 12)):
        fill(im, 10, y, x1, y + 1, '#c8c4b8')
    fill(im, 4, 8, 10, 13, '#3f6f9e')
    fill(im, 10, 8, 12, 13, '#34608a')
    dot(im, 7, 9, '#f2efe6'); fill(im, 6, 10, 8, 12, '#f2efe6')
    for y in range(6, 15):
        dot(im, 4, y, '#fbfaf6')
    for x in range(4, 12):
        dot(im, x, 15, '#b8b4a8')
    return im


def soda_bottle():
    # A bottle of pop with its cap on: thick glass going up into a neck, a paper label round its middle, the light along one side.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 7, 0, 9, 1, '#b0b4b8')
    fill(im, 7, 1, 9, 5, '#4e7a58')
    fill(im, 6, 5, 10, 7, '#4e7a58')
    fill(im, 5, 7, 11, 16, '#46704f')
    fill(im, 5, 9, 11, 13, '#c43a2c')
    fill(im, 6, 10, 10, 12, '#e8dccb')
    for y in range(2, 15):
        dot(im, 6 if y >= 7 else 7, y, '#86b08e')
    for x in range(5, 11):
        dot(im, x, 15, '#33553b')
    return im


def coffee_cup():
    # A paper cup of coffee from the machine: a band round it, the coffee just under its rim, a little steam going off it.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(7, 16):
        inset = (y - 7) // 4
        fill(im, 4 + inset, y, 12 - inset, y + 1, '#efebe0')
    fill(im, 4, 7, 12, 8, '#3a2416')
    fill(im, 5, 10, 11, 12, '#9a6a3c')
    for y in range(8, 15):
        dot(im, 5 + (y - 7) // 4, y, '#fbf8f0')
    for x, y, a in ((7, 5, 120), (8, 4, 100), (7, 3, 80), (9, 2, 60), (8, 1, 40)):
        dot(im, x, y, '#e8e8e8', a)
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


def model(elements, textures, particle, render='cutout'):
    def where(v):
        return v if ':' in v else P + v
    m = {'ambientocclusion': False, 'textures': {**{k: where(v) for k, v in textures.items()}, 'particle': where(particle)}, 'elements': elements}
    if render:
        m['render_type'] = 'minecraft:' + render
    return m


def glow(elements, light):
    for e in elements:
        e['neoforge_data'] = {'block_light': light, 'sky_light': 0}


def box_el(lo, hi, t):
    return element(lo, hi, {'all': t})


def counter_model(stage):
    t = {'f': 'counter_front', 'b': 'counter_back', 't': 'formica', 'e': 'counter_edge'}
    if stage == 2:
        # The flap lifted on its hinge and standing up against the end of the counter, its top to the open side, so the staff pass.
        t.update({'h': 'chrome'})
        return model([element([0, 15, 1], [1.5, 31, 15], {'east': 't', 'west': 'e', 'north': 'e', 'south': 'e', 'up': 'e', 'down': 'e'}),
                      element([0, 14.5, 2], [1, 15.5, 4], {'all': 'h'}), element([0, 14.5, 12], [1, 15.5, 14], {'all': 'h'})], t, 'formica')
    e = [element([0, 0, 0], [16, 15, 16], {'north': 'f', 'south': 'b', 'east': 'f', 'west': 'f', 'down': 'f'}),
         element([0, 15, 0], [16, 16, 16], {'up': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e', 'down': 't'})]
    if stage == 1:
        t.update({'r': 'register', 'k': 'register_keys', 'w': 'register_flags', 'h': 'chrome'})
        e += [element([3, 16, 4], [13, 22, 12], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r'}),
              element([3, 16, 12], [13, 18.5, 15], {'up': 'k', 'south': 'r', 'east': 'r', 'west': 'r'}),
              element([4, 22, 7], [12, 25, 9], {'north': 'w', 'south': 'w', 'east': 'r', 'west': 'r', 'up': 'r'}),
              element([13, 18, 7], [14.5, 19, 8], {'all': 'h'}), element([14, 17, 7], [15, 19, 8], {'all': 'h'})]
    if stage == 3:
        t.update({'c': 'candy', 'w': 'wire'})
        e += [element([2, 16, 2], [14, 22, 2.5], {'north': 'c', 'south': 'c'}, uv={'north': [1, 1, 15, 15], 'south': [1, 1, 15, 15]}),
              element([2, 16, 2.5], [14, 16.5, 7], {'up': 'w', 'north': 'w'}), element([2, 18, 3], [14, 18.5, 6], {'up': 'c'}),
              element([2, 20, 4], [14, 20.5, 7], {'up': 'c'}),
              element([1.5, 16, 2], [2, 22.5, 7], {'all': 'w'}), element([14, 16, 2], [14.5, 22.5, 7], {'all': 'w'})]
    return model(e, t, 'formica', render=None)


def note_model():
    # The note, folded once and propped against the register, leaning back a little so it can be read from the customers' side.
    return model([element([4, 0, 8], [12, 7, 8.25], {'north': 'n', 'south': 'n', 'east': 'n', 'west': 'n', 'up': 'n'},
                          uv={'north': [2, 2, 14, 14]}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 0, 8]})], {'n': 'note'}, 'note')


def stool_model():
    t = {'s': 'stool_seat', 'm': 'chrome'}
    e = [element([4, 0, 4], [5, 10, 5], {'all': 'm'}), element([11, 0, 4], [12, 10, 5], {'all': 'm'}),
         element([4, 0, 11], [5, 10, 12], {'all': 'm'}), element([11, 0, 11], [12, 10, 12], {'all': 'm'}),
         element([5, 4, 4.25], [11, 4.5, 4.75], {'all': 'm'}), element([5, 4, 11.25], [11, 4.5, 11.75], {'all': 'm'}),
         element([4.25, 4, 5], [4.75, 4.5, 11], {'all': 'm'}), element([11.25, 4, 5], [11.75, 4.5, 11], {'all': 'm'}),
         element([4, 10, 3], [12, 11.5, 13], {'all': 's'}), element([3, 10, 4], [13, 11.5, 12], {'all': 's'})]
    return model(e, t, 'stool_seat')


def backshelf_model(stage):
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'b', 'down': 'b'})],
                     {'c': 'cabinet', 'b': 'shelf_board'}, 'cabinet', render=None)
    t = {'b': 'shelf_board', 'c': 'cartons', 'n': 'cans', 'r': 'radio'}
    e = [element([0, 0, 15], [16, 16, 16], {'all': 'b'}),
         element([0, 0, 6], [16, 1, 16], {'all': 'b'}), element([0, 7.5, 6], [16, 8.5, 16], {'all': 'b'}),
         element([0, 15, 6], [16, 16, 16], {'all': 'b'}),
         element([0, 1, 6], [1, 15, 16], {'all': 'b'}), element([15, 1, 6], [16, 15, 16], {'all': 'b'}),
         element([1, 1, 8], [15, 7.5, 15], {'north': 'c', 'up': 'c', 'east': 'c', 'west': 'c'}, uv={'up': [0, 2, 16, 4]})]
    if stage == 1:
        e.append(element([1, 8.5, 9], [15, 14, 15], {'north': 'n', 'up': 'n', 'east': 'n', 'west': 'n'}, uv={'north': [0, 5, 16, 16], 'up': [0, 5, 16, 6]}))
    else:
        e += [element([2, 8.5, 9], [10, 14.5, 14], {'north': 'r', 'up': 'r', 'east': 'r', 'west': 'r'}, uv={'up': [0, 3, 16, 4]}),
              element([11, 8.5, 9], [15, 13, 15], {'north': 'n', 'up': 'n', 'east': 'n', 'west': 'n'}, uv={'north': [0, 5, 16, 16]})]
    return model(e, t, 'shelf_board')


def tv_model(stage):
    t = {'f': f'tv_{stage}', 'w': 'tv_case', 'm': 'chrome', 'l': 'hitch_metal'}
    e = [element([2, 3, 4], [14, 13, 14], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
         element([7, 2, 8], [9, 3, 16], {'all': 'l'}), element([5, 1, 14], [11, 4, 16], {'all': 'l'}),
         element([7.5, 13, 9], [8, 19, 9.5], {'all': 'm'}, rotation={'angle': 22.5, 'axis': 'z', 'origin': [8, 13, 9]}),
         element([8, 13, 9], [8.5, 19, 9.5], {'all': 'm'}, rotation={'angle': -22.5, 'axis': 'z', 'origin': [8, 13, 9]})]
    if stage < 2:
        e.append(element([3, 5, 3.9], [10.5, 11.5, 4], {'north': 'f'}, uv={'north': [2, 3, 10, 12]}))
        glow(e[-1:], 10)
    return model(e, t, 'tv_case')


def cooler_model(stage):
    t = {'d': f'cooler_{stage}', 's': 'cooler_side'}
    e = [element([0, 0, 1], [16, 16, 16], {'north': 'd', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})]
    if stage >= 1:
        e.append(element([0, 7, 0.9], [16, 16, 1], {'north': 'd'}, uv={'north': [0, 0, 16, 9]}))
        glow(e[-1:], 12)
    return model(e, t, 'cooler_side', render=None)


def gondola_model(stage):
    t = {'g': f'gondola_{min(stage, 2)}', 'e': 'gondola_end', 't': 'gondola_top'}
    if stage == 3:
        return model([element([1, 0, 3], [15, 8, 13], {'north': 't', 'south': 't', 'east': 'e', 'west': 'e', 'up': 'e', 'down': 'e'},
                               uv={'north': [0, 0, 16, 8], 'south': [0, 0, 16, 8], 'east': [3, 8, 13, 16], 'west': [3, 8, 13, 16]})], t, 'gondola_end', render=None)
    return model([element([1, 0, 3], [15, 16, 13], {'north': 'g', 'south': 'g', 'east': 'e', 'west': 'e', 'up': 'e', 'down': 'e'})], t, 'gondola_end', render=None)


def maps_model(stage):
    t = {'m': 'maps', 'w': 'wire', 'h': 'maps_header'}
    if stage == 1:
        return model([element([3, 0, 12.5], [13, 6, 13], {'north': 'h', 'south': 'h', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'},
                               uv={'north': [0, 0, 16, 11], 'south': [0, 0, 16, 11]}),
                      element([3, 0, 13], [3.5, 6, 14], {'all': 'w'}), element([12.5, 0, 13], [13, 6, 14], {'all': 'w'})], t, 'maps')
    return model([element([3, 0, 10], [13, 16, 15], {'north': 'm', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'm'}, uv={'up': [0, 0, 16, 5]}),
                  element([3, 0, 15], [13, 16, 16], {'all': 'w'})], t, 'maps')


def coffee_model():
    t = {'s': 'coffee_stand', 'm': 'coffee_machine', 'p': 'coffee_pot', 'c': 'cups', 'h': 'chrome', 'k': 'crock'}
    e = [element([1, 0, 2], [15, 10, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 'h', 'down': 's'}),
         element([2, 10, 9], [14, 16, 15], {'north': 'm', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h'}),
         element([2, 16, 8], [14, 17, 15], {'all': 'h'}),
         element([3, 10.5, 4], [7.5, 15.5, 8.5], {'all': 'p'}, uv={'up': [2, 0, 14, 2]}),
         element([7.5, 13, 5.5], [8.5, 14, 7], {'all': 'p'}),
         element([9.5, 10, 4], [11.5, 14, 6], {'all': 'c'}),
         element([12, 10, 4], [14, 12.5, 6], {'all': 'k'})]
    return model(e, t, 'coffee_machine')


def window_model(stage):
    # 0.4.680: stages 2 and 3 are the lower and upper halves blown in by the light, the frame still in the wall and the glass gone
    # but for what its edges hold.
    t = {'f': 'window_frame', 'g': 'window_glass' if stage < 2 else f'window_shards_{stage - 2}'}
    if stage % 2 == 0:
        e = [element([0, 0, 6], [16, 2, 10], {'all': 'f'}), element([0, 2, 7.5], [16, 16, 8.5], {'north': 'g', 'south': 'g'})]
    else:
        e = [element([0, 0, 7.5], [16, 14, 8.5], {'north': 'g', 'south': 'g'}), element([0, 14, 6], [16, 16, 10], {'all': 'f'})]
    return model(e, t, 'window_frame', render='translucent')


def rubble_model(stage):
    """0.4.680, what the light leaves: a low spread of the store (0), a heap of it (1), the shut cellar doors with the store come down
    on them (2), a joist fallen across a spread (3), a sheet of the canopy's deck down on the forecourt, buckled (4), another leaning
    on what caught it (5), a column of the canopy lying down (6), a fallen run of the store's wall, its boards still lapped (7)."""
    t = {'t': 'debris_top', 's': 'debris_side', 'j': 'joist', 'w': NS + 'block/fallow_siding', 'c': NS + 'block/fallow_ceiling',
         'r': NS + 'block/fallow_roof', 'd': 'canopy_soffit', 'f': 'canopy_fascia', 'o': 'column', 'k': 'collar', 'l': 'cellar_door',
         'u': 'curb_concrete'}
    pile = {'up': 't', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'}
    board = {'up': 'w', 'down': 'c', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}
    if stage == 0:
        e = [element([0, 0, 0], [16, 3, 16], pile),
             element([2, 3, 4], [13, 4, 7], board, rotation={'angle': 22.5, 'axis': 'y', 'origin': [8, 3, 8]}),
             element([9, 3, 9], [13, 5, 13], {'all': 'r'})]
    elif stage == 1:
        e = [element([0, 0, 0], [16, 5, 16], pile), element([2, 5, 2], [14, 8, 14], pile), element([5, 8, 4], [12, 10, 11], pile),
             element([1, 6, 7], [15, 7, 10], board, rotation={'angle': 22.5, 'axis': 'z', 'origin': [8, 6, 8]}),
             element([3, 8, 9], [7, 11, 13], {'all': 'r'})]
    elif stage == 2:
        e = [element([0, 0, 0], [2, 7, 16], {'all': 'u'}), element([0, 7, 0], [16, 9, 16], {'up': 'l', 'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'down': 'l'}),
             element([1, 9, 1], [15, 12, 15], pile), element([4, 12, 3], [13, 14, 12], pile),
             element([-2, 12, 6], [18, 14, 9], {'all': 'j'}, rotation={'angle': 22.5, 'axis': 'y', 'origin': [8, 12, 8]})]
    elif stage == 3:
        e = [element([0, 0, 0], [16, 2, 16], pile),
             element([-3, 2, 6], [19, 5, 9], {'all': 'j'}, rotation={'angle': -22.5, 'axis': 'y', 'origin': [8, 2, 8]}),
             element([10, 2, 2], [14, 3, 4], board)]
    elif stage == 4:
        e = [element([0, 0, 0], [16, 1, 16], pile),
             element([0, 1, 1], [16, 2, 8], {'up': 'd', 'down': 'r', 'north': 'f', 'south': 'd', 'east': 'd', 'west': 'd'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 1, 8]}),
             element([0, 1, 8], [16, 2, 15], {'up': 'd', 'down': 'r', 'north': 'd', 'south': 'd', 'east': 'd', 'west': 'd'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 1, 8]})]
    elif stage == 5:
        e = [element([0, 0, 0], [16, 2, 16], pile),
             element([0, 0, 7], [16, 16, 8], {'north': 'd', 'south': 'r', 'east': 'd', 'west': 'd', 'up': 'f', 'down': 'd'}, rotation={'angle': 45, 'axis': 'x', 'origin': [8, 0, 8]})]
    elif stage == 6:
        e = [element([0, 0, 0], [16, 1, 16], pile),
             element([-6, 1, 5], [22, 7, 11], {'north': 'o', 'south': 'o', 'up': 'o', 'down': 'o', 'east': 'k', 'west': 'o'}, uv={k: [0, 5, 16, 11] for k in ('north', 'south', 'up', 'down')}),
             element([20, 0, 4], [23, 8, 12], {'all': 'k'})]
    else:
        e = [element([0, 0, 0], [16, 2, 16], pile),
             element([0, 2, 1], [16, 4, 15], board, rotation={'angle': 22.5, 'axis': 'z', 'origin': [8, 2, 8]}),
             element([1, 3, 3], [12, 5, 13], board, rotation={'angle': -22.5, 'axis': 'y', 'origin': [8, 3, 8]})]
    return model(e, t, 'debris_top')


def tube_model():
    e = [element([2, 14, 6], [14, 16, 10], {'all': 'm'}), element([2.5, 13.25, 6.5], [13.5, 14, 9.5], {'down': 'l', 'north': 'l', 'south': 'l'})]
    glow(e[1:], 15)
    return model(e, {'m': 'chrome', 'l': 'tube_light'}, 'tube_light')


def signband_model(i):
    return model([element([0, 0, 6], [16, 16, 10], {'north': 's', 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'})],
                 {'s': f'signband_{i}', 'b': 'signband_back'}, f'signband_{i}', render=None)


def pump_model(stage):
    diesel = stage >= 2
    d = 'diesel' if diesel else 'pump'
    t = {'b': f'{d}_body', 's': f'{d}_side', 'h': f'{d}_head', 'o': 'hose', 'n': 'nozzle', 'c': 'chrome'}
    if stage % 2 == 0:
        e = [element([2, 0, 3], [14, 16, 13], {'north': 'b', 'south': 'b', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
             element([1.5, 0, 2.5], [14.5, 1.5, 13.5], {'all': 'c'}),
             element([14, 9, 6], [15.5, 13, 9], {'all': 'n'}), element([14, 6, 7], [15, 9, 8], {'all': 'o'})]
        return model(e, t, f'{d}_body', render=None)
    e = [element([3, 0, 4], [13, 12, 12], {'north': 'h', 'south': 'h', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
         element([2.5, 12, 3.5], [13.5, 13, 12.5], {'all': 'c'}),
         element([13, 2, 7], [14, 10, 8], {'all': 'o'}), element([14, 0, 7], [15, 3, 8], {'all': 'o'})]
    e.append(element([4, 7, 3.9], [12, 11, 4], {'north': 'h', 'south': 'h'}, uv={'north': [2, 5, 14, 12], 'south': [2, 5, 14, 12]}))
    glow(e[-1:], 8)
    return model(e, t, f'{d}_head', render=None)


def island_model(stage):
    t = {'s': 'island_side', 't': 'island_top'}
    if stage == 1:
        return model([element([2, 0, 3], [14, 6, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 't'}, uv={'up': [2, 3, 14, 16]}),
                      element([4, 0, 1], [12, 6, 3], {'north': 's', 'east': 's', 'west': 's', 'up': 't'})], t, 'island_top', render=None)
    return model([element([0, 0, 2], [16, 6, 14], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 't'})], t, 'island_top', render=None)


def canopy_model(stage):
    t = {'d': 'canopy_soffit', 'r': 'roof', 'f': 'canopy_fascia', 'l': 'canopy_light', 'c': 'column', 'k': 'collar', 'm': 'column_marks'}
    t['r'] = NS + 'block/fallow_roof'
    if stage in (4, 6):
        e = [element([5, 0, 5], [11, 16, 11], {'north': 'm' if stage == 4 else 'c', 'south': 'c', 'east': 'c', 'west': 'c'})]
        if stage == 6:
            e += [element([5, 16, 5], [11, 24, 11], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}, uv={k: [0, 8, 16, 16] for k in ('north', 'south', 'east', 'west')}),
                  element([4, 23, 4], [12, 24, 12], {'all': 'k'})]
        return model(e, t, 'column')
    if stage == 5:
        return model([element([4, 0, 4], [12, 3, 12], {'all': 'k'}), element([5, 3, 5], [11, 16, 11], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'})], t, 'column')
    e = [element([0, 8, 0], [16, 16, 16], {'down': 'd', 'up': 'r', 'north': 'd', 'south': 'd', 'east': 'd', 'west': 'd'})]
    if stage == 1:
        e.append(element([3, 7.5, 3], [13, 8, 13], {'down': 'l', 'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l'}))
        glow(e[-1:], 15)
    if stage in (2, 3):
        e.append(element([0, 4, 0], [16, 16, 2], {'north': 'f', 'down': 'd', 'up': 'r', 'east': 'f', 'west': 'f', 'south': 'd'}, uv={'east': [0, 4, 2, 16], 'west': [14, 4, 16, 16]}))
    if stage == 3:
        e.append(element([0, 4, 0], [2, 16, 16], {'west': 'f', 'down': 'd', 'up': 'r', 'east': 'd', 'south': 'f', 'north': 'f'}, uv={'south': [14, 4, 16, 16], 'north': [0, 4, 2, 16]}))
    return model(e, t, 'canopy_soffit')


def sign_model(stage):
    t = {'p': 'sign_pole', 'n': 'name_board', 'b': 'price_board', 'r': 'sign_trim'}
    pole = element([6, 0, 6], [10, 16, 10], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'})
    if stage == 0:
        return model([pole], t, 'sign_pole')
    board = 'b' if stage == 1 else 'n'
    e = [pole if stage == 1 else element([6, 0, 6], [10, 1, 10], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
         element([-16, 0, 6.5], [32, 16, 9.5], {'north': board, 'south': board, 'east': 'r', 'west': 'r', 'down': 'r', 'up': 'r'},
                 uv={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'east': [0, 0, 3, 16], 'west': [0, 0, 3, 16], 'up': [0, 0, 16, 3], 'down': [0, 0, 16, 3]})]
    if stage == 2:
        # The trim caps the board, lying on its top edge rather than over it, so its ends do not share the board's ends.
        e.append(element([-16, 16, 6], [32, 17, 10], {'all': 'r'}))
    return model(e, t, f'{"price" if stage == 1 else "name"}_board')


WIRE_Y = {1: 14.5, 2: 13.75, 3: 12.75, 4: 12.0}


def pole_model(stage):
    t = {'w': 'pole_wood', 'a': 'crossarm', 'i': 'insulator', 'x': 'line_wire'}
    if stage == 0:
        return model([element([5.5, 0, 5.5], [10.5, 16, 10.5], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'})], t, 'pole_wood')
    y = WIRE_Y[stage]
    wires = [element([0, y, -1.75], [16, y + .5, -1.25], {'all': 'x'}), element([0, y, 17.25], [16, y + .5, 17.75], {'all': 'x'})]
    if stage >= 2:
        return model(wires, t, 'wire')
    e = [element([5.5, 0, 5.5], [10.5, 12, 10.5], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
         element([6, 10, -4], [10, 12.5, 20], {'all': 'a'}),
         element([7, 12.5, -2.5], [9, 14.5, -0.5], {'all': 'i'}), element([7, 12.5, 16.5], [9, 14.5, 18.5], {'all': 'i'})]
    return model(e + wires, t, 'pole_wood')


def wheat_model(stage):
    h = 16 if stage < 2 else 16
    e = [element([4, 0, 0], [4, h, 16], {'east': 'c', 'west': 'c'}), element([12, 0, 0], [12, h, 16], {'east': 'c', 'west': 'c'}),
         element([0, 0, 4], [16, h, 4], {'north': 'c', 'south': 'c'}), element([0, 0, 12], [16, h, 12], {'north': 'c', 'south': 'c'})]
    return model(e, {'c': f'wheat_{stage}'}, f'wheat_{stage}')


def pad_model(stage):
    t = {'c': 'chainlink', 'g': 'galv', 'b': 'barb', 's': 'gov_sign', 'w': 'pole_wood', 'f': 'flood_lit', 'l': 'lid', 'e': 'lid_edge',
         'h': 'hatch', 'k': 'hitch_metal'}
    if stage in (0, 2):
        e = [element([0, 0, 7.75], [16, 15, 8.25], {'north': 'c', 'south': 'c'}),
             element([0, 15, 7.5], [16, 16, 8.5], {'all': 'g'})]
        e += [element([0, 17, 6.5], [16, 17.5, 7], {'north': 'b', 'south': 'b', 'up': 'b', 'down': 'b'}, uv={'north': [0, 6, 16, 10], 'south': [0, 6, 16, 10], 'up': [0, 7, 16, 7.5], 'down': [0, 7, 16, 7.5]}),
              element([0, 18.5, 5.25], [16, 19, 5.75], {'north': 'b', 'south': 'b', 'up': 'b', 'down': 'b'}, uv={'north': [0, 6, 16, 10], 'south': [0, 6, 16, 10], 'up': [0, 7, 16, 7.5], 'down': [0, 7, 16, 7.5]}),
              element([0, 20, 4], [16, 20.5, 4.5], {'north': 'b', 'south': 'b', 'up': 'b', 'down': 'b'}, uv={'north': [0, 6, 16, 10], 'south': [0, 6, 16, 10], 'up': [0, 7, 16, 7.5], 'down': [0, 7, 16, 7.5]})]
        if stage == 2:
            e += [element([0, 0, 7.5], [1, 15, 8.5], {'all': 'g'}), element([15, 0, 7.5], [16, 15, 8.5], {'all': 'g'}),
                  element([0, 0, 7.5], [16, 1, 8.5], {'all': 'g'}), element([0, 7.5, 7.5], [16, 8.5, 8.5], {'all': 'g'}),
                  element([3, 9, 7.2], [13, 14, 7.5], {'north': 's', 'south': 'g'}),
                  element([14, 6, 7], [16, 8, 9], {'all': 'k'}), element([14.5, 4, 7.25], [15.5, 6, 8.75], {'all': 'k'})]
        return model(e, t, 'chainlink')
    if stage == 1:
        return model([element([7, 0, 7], [9, 17, 9], {'all': 'g'}),
                      element([7.5, 16, 4], [8.5, 17, 8], {'all': 'g'}, rotation={'angle': 45, 'axis': 'x', 'origin': [8, 16.5, 8]})], t, 'galv')
    if stage == 3:
        return model([element([7, 0, 7], [9, 16, 9], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'})], t, 'pole_wood')
    if stage == 4:
        e = [element([7, 0, 7], [9, 6, 9], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w'}),
             element([7.5, 5, 2], [8.5, 6, 7], {'all': 'k'}),
             element([5, 3, 0], [11, 7, 4], {'north': 'f', 'down': 'f', 'east': 'k', 'west': 'k', 'up': 'k', 'south': 'k'}, uv={'down': [2, 2, 14, 14]})]
        glow(e[-1:], 15)
        return model(e, t, 'flood_lit')
    if stage in (5, 6):
        top = 'e' if stage == 5 else 'l'
        return model([element([0, 0, 0], [16, 16, 16], {'up': top, 'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'down': 'l'})], t, 'lid', render=None)
    return model([element([3, 0, 3], [13, 9, 13], {'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h', 'down': 'h'}),
                  element([4, 9, 6], [12, 9.5, 10], {'all': 'k'})], t, 'hatch')


def lift_model(stage):
    t = {'p': 'lift_post', 'a': 'lift_arm', 'b': 'bench_top', 'f': 'bench_front', 'g': 'pegboard', 'y': 'tyre_side', 'o': 'tyre_top',
         'd': 'drum', 'r': 'rags', 'm': 'hitch_metal'}
    if stage in (0, 6):
        e = [element([4, 0, 4], [12, 16 if stage == 0 else 10, 12], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'm'})]
        if stage == 6:
            e.append(element([3, 10, 3], [13, 11, 13], {'all': 'm'}))
        return model(e, t, 'lift_post')
    if stage == 1:
        return model([element([0, 12, 6], [16, 16, 10], {'all': 'a'}), element([3, 16, 6.5], [6, 16.5, 9.5], {'all': 'm'}),
                      element([10, 16, 6.5], [13, 16.5, 9.5], {'all': 'm'})], t, 'lift_arm')
    if stage == 2:
        return model([element([0, 14, 4], [16, 15, 16], {'up': 'b', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'down': 'f'}, uv={'north': [0, 0, 16, 1]}),
                      element([1, 8, 5], [15, 13, 6], {'north': 'f'}, uv={'north': [0, 2, 16, 7]}),
                      element([0.5, 0, 4.5], [2, 14, 6], {'all': 'f'}), element([14, 0, 4.5], [15.5, 14, 6], {'all': 'f'}),
                      element([0.5, 0, 14], [2, 14, 15.5], {'all': 'f'}), element([14, 0, 14], [15.5, 14, 15.5], {'all': 'f'}),
                      element([1, 3, 5], [15, 4, 15], {'all': 'b'}),
                      element([1, 15, 5], [4, 18, 8], {'all': 'm'})], t, 'bench_top')
    if stage == 3:
        return model([element([0, 0, 15], [16, 16, 16], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'})], t, 'pegboard')
    if stage == 4:
        e = []
        for y in (0, 4.5, 9):
            e += [element([2, y, 1], [14, y + 4, 15], {'north': 'y', 'south': 'y', 'east': 'y', 'west': 'y', 'up': 'o', 'down': 'o'}),
                  element([1, y, 2], [15, y + 4, 14], {'north': 'y', 'south': 'y', 'east': 'y', 'west': 'y'})]
        return model(e, t, 'tyre_side')
    return model([element([3, 0, 3], [13, 15, 13], {'north': 'd', 'south': 'd', 'east': 'd', 'west': 'd', 'down': 'd'}),
                  element([2.5, 1, 3.5], [13.5, 14, 12.5], {'north': 'd', 'south': 'd', 'east': 'd', 'west': 'd'}),
                  element([3.5, 13, 3.5], [12.5, 15.5, 12.5], {'up': 'r', 'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r'})], t, 'drum')


def trailer_model(stage):
    t = {'w': 'trailer_window', 's': NS + 'block/fallow_trailer', 'm': 'hitch_metal', 'c': 'cinder', 'p': 'propane', 'o': 'post_wood',
         'l': 'clothes', 'r': 'line_rope', 'b': 'barrel', 'a': 'ash', 'f': 'field_fence', 'g': 'galv'}
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'w', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'trailer_window', render=None)
    if stage == 1:
        return model([element([6, 0, 4], [10, 3, 8], {'all': 'c'}),
                      element([5, 3, 3], [6.5, 4.5, 16], {'all': 'm'}, rotation={'angle': 22.5, 'axis': 'y', 'origin': [5, 3, 16]}),
                      element([9.5, 3, 3], [11, 4.5, 16], {'all': 'm'}, rotation={'angle': -22.5, 'axis': 'y', 'origin': [11, 3, 16]}),
                      element([6.5, 2.5, 0], [9.5, 5.5, 3.5], {'all': 'm'})], t, 'hitch_metal')
    if stage == 2:
        return model([element([4, 0, 4], [12, 13, 12], {'all': 'p'}), element([6, 13, 6], [10, 15, 10], {'all': 'g'}),
                      element([3.5, 9, 11], [12.5, 9.5, 12.5], {'all': 'm'})], t, 'propane')
    if stage == 3:
        return model([element([0, 0, 0], [16, 4, 16], {'all': 'c'}, uv={'north': [0, 4, 16, 8], 'south': [0, 4, 16, 8], 'east': [0, 4, 16, 8], 'west': [0, 4, 16, 8]}),
                      element([0, 4, 8], [16, 8, 16], {'all': 'c'}, uv={'north': [0, 4, 16, 8], 'south': [0, 4, 16, 8], 'east': [0, 4, 8, 8], 'west': [0, 4, 8, 8]})], t, 'cinder')
    if stage == 4:
        return model([element([7, 0, 7], [9, 26, 9], {'all': 'o'}), element([2, 24, 7], [14, 26, 9], {'all': 'o'})], t, 'post_wood')
    if stage == 5:
        # Two lines at the height of the posts' crossbar, and what hangs on them: on the near line a shirt, on the far a towel.
        lines = [element([0, 9, 2.75], [16, 9.3, 3.25], {'all': 'r'}), element([0, 9, 12.75], [16, 9.3, 13.25], {'all': 'r'})]
        e = lines + [element([2, -0.5, 3], [12, 9.5, 3], {'north': 'l', 'south': 'l'}, uv={'north': [0, 0, 10, 10], 'south': [10, 0, 0, 10]}),
                     element([6, 3, 13], [11, 9.5, 13], {'north': 'l', 'south': 'l'}, uv={'north': [11, 0, 16, 7], 'south': [16, 0, 11, 7]})]
        return model(e, t, 'line_rope')
    if stage == 6:
        return model([element([3, 0, 3], [13, 14, 4], {'all': 'b'}), element([3, 0, 12], [13, 14, 13], {'all': 'b'}),
                      element([3, 0, 4], [4, 14, 12], {'all': 'b'}), element([12, 0, 4], [13, 14, 12], {'all': 'b'}),
                      element([4, 9, 4], [12, 10, 12], {'up': 'a'})], t, 'barrel')
    return model([element([0, 0, 7.75], [16, 14, 8.25], {'north': 'f', 'south': 'f'}, uv={'north': [0, 2, 16, 16], 'south': [16, 2, 0, 16]}),
                  element([0, 0, 7.25], [1, 14.5, 8.75], {'all': 'g'})], t, 'field_fence')


def cellar_model(stage, opened):
    """The cellar's doors over the well: a low concrete curb along the outer side and across the end, and the leaf on top of it, shut
    flat (CELLAR), or thrown back over the curb and lying on the yard beside it, its underside up (CELLAR_OPEN)."""
    t = {'d': 'cellar_door', 'u': 'cellar_under', 'k': 'curb_concrete', 'm': 'hitch_metal'}
    left, pos = stage < 3, stage % 3
    e = [element([0, 0, 0], [2, 7, 16], {'all': 'k'}) if left else element([14, 0, 0], [16, 7, 16], {'all': 'k'})]
    if pos == 2:
        e.append(element([0, 0, 0], [16, 7, 2], {'all': 'k'}))
    if pos == 0:
        e.append(element([0, 0, 14], [16, 7, 16], {'all': 'k'}))
    if not opened:
        uv = {'up': [0, 0, 16, 16] if left else [16, 0, 0, 16]}
        e.append(element([0, 7, 0], [16, 9, 16], {'up': 'd', 'down': 'u', 'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm'}, uv=uv))
        return model(e, t, 'cellar_door', render=None)
    # Thrown back: hinged at the curb's outer top edge, laid over onto the yard, resting at a slant on its far edge.
    if left:
        e.append(element([-16, 7, 0], [0, 9, 16], {'up': 'u', 'down': 'd', 'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm'},
                         rotation={'angle': 22.5, 'axis': 'z', 'origin': [0, 8, 8]}))
    else:
        e.append(element([16, 7, 0], [32, 9, 16], {'up': 'u', 'down': 'd', 'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm'},
                         rotation={'angle': -22.5, 'axis': 'z', 'origin': [16, 8, 8]}))
    return model(e, t, 'cellar_door', render=None)


def shelf_model(stage):
    t = {'b': 'shelf_board', 'j': 'jars' if stage == 0 else 'tins'}
    return model([element([0, 0, 8], [16, 1, 16], {'all': 'b'}), element([0, 15, 8], [16, 16, 16], {'all': 'b'}),
                  element([0, 1, 15], [16, 15, 16], {'all': 'b'}),
                  element([0, 1, 8], [1, 15, 16], {'all': 'b'}), element([15, 1, 8], [16, 15, 16], {'all': 'b'}),
                  element([1, 1, 9], [15, 10, 14], {'north': 'j', 'east': 'j', 'west': 'j', 'up': 'j'}, uv={'north': [1, 2, 15, 11], 'up': [1, 2, 15, 3] if stage == 0 else [1, 6, 15, 7]})], t, 'shelf_board')


def cot_model(stage):
    t = {'c': 'cot_canvas', 'f': 'hitch_metal', 'b': 'blanket', 'w': 'post_wood'}
    e = [element([1.5, 7, 0], [14.5, 7.5, 16], {'up': 'c', 'down': 'c'}),
         element([1, 7, 0], [2, 8.5, 16], {'all': 'w'}), element([14, 7, 0], [15, 8.5, 16], {'all': 'w'}),
         element([2, 0, 3], [3, 7, 4], {'all': 'w'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [2.5, 3.5, 3.5]}),
         element([13, 0, 3], [14, 7, 4], {'all': 'w'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [13.5, 3.5, 3.5]}),
         element([2, 0, 12], [3, 7, 13], {'all': 'w'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [2.5, 3.5, 12.5]}),
         element([13, 0, 12], [14, 7, 13], {'all': 'w'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [13.5, 3.5, 12.5]})]
    if stage == 1:
        e.append(element([2.5, 7.5, 6], [13.5, 10, 14], {'all': 'b'}))
    return model(e, t, 'cot_canvas')


def crock_model(stage):
    t = {'k': 'crock', 'w': 'post_wood', 't': 'tin', 'g': {1: 'lantern_lit', 3: 'lantern_low'}.get(stage, 'lantern_dark')}
    if stage == 0:
        return model([element([4, 0, 4], [12, 10, 12], {'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k', 'down': 'k'}),
                      element([3.5, 1, 4.5], [12.5, 9, 11.5], {'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k'}),
                      element([4.5, 10, 4.5], [11.5, 10.5, 11.5], {'all': 'w'}),
                      element([12, 6, 7.5], [13, 11, 8.5], {'all': 't'}), element([12.5, 3, 6.5], [14.5, 6, 9.5], {'all': 't'})], t, 'crock')
    e = [element([7.75, 11.5, 7.75], [8.25, 16, 8.25], {'all': 't'}),
         element([5, 3, 5], [11, 4, 11], {'all': 't'}),
         element([5.5, 4, 5.5], [10.5, 10, 10.5], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'}),
         element([5, 10, 5], [11, 11.5, 11], {'all': 't'})]
    if stage == 1:
        glow(e[2:3], 15)
    elif stage == 3:
        glow(e[2:3], 8)
    return model(e, t, 'tin')


def farm_model(stage):
    t = {'w': 'farm_window', 's': NS + 'block/fallow_farm_siding', 'p': 'porch_paint', 'm': 'mailbox', 'f': 'mailbox_front', 'g': 'flag',
         'o': 'post_wood'}
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'w', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'farm_window', render=None)
    if stage == 1:
        return model([element([6, 0, 6], [10, 16, 10], {'all': 'p'}), element([5.5, 0, 5.5], [10.5, 2, 10.5], {'all': 'p'}),
                      element([5.5, 14, 5.5], [10.5, 16, 10.5], {'all': 'p'})], t, 'porch_paint')
    if stage == 2:
        e = [element([0, 12, 7], [16, 13.5, 9], {'all': 'p'}), element([0, 1, 7.25], [16, 2, 8.75], {'all': 'p'})]
        for x in (1.5, 4.5, 7.5, 10.5, 13.5):
            e.append(element([x, 2, 7.5], [x + 1, 12, 8.5], {'all': 'p'}))
        return model(e, t, 'porch_paint')
    return model([element([7, 0, 7], [9, 12, 9], {'all': 'o'}),
                  element([5, 12, 3], [11, 16, 13], {'north': 'f', 'south': 'f', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'}),
                  element([5.5, 16, 3], [10.5, 17, 13], {'all': 'm'}),
                  element([11, 13, 9], [11.5, 14, 11], {'all': 'g'}), element([11, 12, 10.5], [11.5, 14, 11], {'all': 'g'})], t, 'mailbox')


def rig_model(stage):
    """A slice of the rig, facing west, drawn across both its cells: local x 0 to 16 is the cell it stands in, 16 to 32 the cell to its
    north. The hood with its front wheels, bumper, grille and headlamps (0 below, 1 above); the cab with its fuel tanks and steps, its
    doors, windscreen and mirrors (2, 3); the sleeper, its drive wheels reaching back under the trailer, its stacks (4, 5); the roof (6)."""
    t = {'p': 'rig_paint', 'd': 'rig_door', 'c': 'rig_cab_side', 'v': 'windshield', 'g': 'grille', 'y': 'tyre_tread', 'h': 'hub',
         'l': 'headlight', 'm': 'chrome', 'k': 'box_frame', 's': 'sleeper_side', 'f': 'mud_flap'}
    def wheel(x0, z0, z1, outer):
        side = 'west' if outer == 0 else 'east'
        faces = {'north': 'y', 'south': 'y', 'up': 'y', 'down': 'y', 'east': 'y', 'west': 'y'}
        faces[side] = 'h'
        return element([x0, 0, z0], [x0 + 4.5, 10, z1], faces)
    if stage == 0:
        e = [element([1, 3, -6], [31, 6, -4], {'all': 'm'}),
             element([5, 4, -4], [27, 16, 16], {'north': 'g', 'south': 'p', 'east': 'p', 'west': 'p', 'down': 'k'}, uv={'north': [0, 0, 16, 16]}),
             # The wheels sit a quarter inside the fenders' outer faces, so neither lies on the model's edge with the other.
             wheel(.25, -1, 9, 0), wheel(27.25, -1, 9, 1),
             element([-0.5, 9, -3], [5, 11, 11], {'all': 'p'}), element([27, 9, -3], [32, 11, 11], {'all': 'p'}),
             element([1, 11, -3], [4.5, 14, -2], {'north': 'l', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'}),
             element([27.5, 11, -3], [31, 14, -2], {'north': 'l', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'}),
             element([5, 0.5, 2], [27, 3, 14], {'all': 'k'})]
        glow(e[6:8], 6)
        return model(e, t, 'rig_paint', render=None)
    if stage == 1:
        return model([element([5, 0, -4], [27, 6, 16], {'north': 'g', 'up': 'p', 'east': 'p', 'west': 'p', 'south': 'p'}, uv={'north': [0, 0, 16, 6]}),
                      element([15, 6, -3.5], [17, 7.5, -2], {'all': 'm'})], t, 'rig_paint', render=None)
    if stage == 2:
        return model([element([2, 6, 0], [30, 16, 16], {'north': 'p', 'east': 'd', 'west': 'd', 'south': 'p', 'down': 'k'}, uv={'west': [16, 0, 0, 16]}),
                      element([0, 2, 3], [3, 7, 13], {'all': 'm'}), element([29, 2, 3], [32, 7, 13], {'all': 'm'}),
                      element([0, 0.5, 2], [2, 1.5, 6], {'all': 'k'}), element([30, 0.5, 2], [32, 1.5, 6], {'all': 'k'}),
                      element([2, 0, 1], [30, 6, 15], {'all': 'k'})], t, 'rig_paint', render=None)
    if stage == 3:
        return model([element([2, 0, 0], [30, 16, 16], {'north': 'v', 'east': 'c', 'west': 'c', 'south': 'p', 'up': 'p'},
                              uv={'north': [0, 0, 16, 16], 'west': [16, 0, 0, 16]}),
                      element([0, 2, 1], [1.5, 9, 2], {'all': 'm'}), element([1.5, 5, 1.25], [2, 5.5, 1.75], {'all': 'm'}),
                      element([30.5, 2, 1], [32, 9, 2], {'all': 'm'}), element([30, 5, 1.25], [30.5, 5.5, 1.75], {'all': 'm'})], t, 'rig_paint', render=None)
    if stage == 4:
        return model([element([2, 6, 0], [30, 16, 16], {'north': 'p', 'east': 's', 'west': 's', 'south': 'p', 'down': 'k'}, uv={'west': [16, 0, 0, 16]}),
                      wheel(-0.5, 4, 14, 0), wheel(27.5, 4, 14, 1), wheel(-0.5, 14.5, 24.5, 0), wheel(27.5, 14.5, 24.5, 1),
                      element([6, 6, 14], [26, 10, 26], {'all': 'k'}), element([4, 1, 2], [28, 6, 26], {'all': 'k'}),
                      element([0, 0, 25], [4, 7, 25.5], {'all': 'f'}), element([28, 0, 25], [32, 7, 25.5], {'all': 'f'}),
                      element([0.5, 8, 0.5], [2.5, 16, 2.5], {'all': 'm'}), element([29.5, 8, 0.5], [31.5, 16, 2.5], {'all': 'm'})], t, 'rig_paint', render=None)
    if stage == 5:
        return model([element([2, 0, 0], [30, 16, 16], {'north': 'p', 'east': 's', 'west': 's', 'south': 'p', 'up': 'p'}, uv={'west': [16, 0, 0, 16]}),
                      element([0.5, 0, 0.5], [2.5, 30, 2.5], {'all': 'm'}), element([29.5, 0, 0.5], [31.5, 30, 2.5], {'all': 'm'})], t, 'rig_paint', render=None)
    if stage == 6:
        e = [element([2, 0, 0], [30, 3, 16], {'all': 'p'}),
             element([5, 3, 1], [7, 4, 2], {'all': 'l'}), element([15, 3, 1], [17, 4, 2], {'all': 'l'}), element([25, 3, 1], [27, 4, 2], {'all': 'l'})]
        glow(e[1:], 6)
        return model(e, t, 'rig_paint', render=None)
    return {'textures': {'particle': NS + 'block/fallow_box'}, 'elements': []}


def box_model(stage):
    t = {'y': 'tyre_tread', 'h': 'hub', 'k': 'box_frame', 'm': 'hitch_metal', 'd': 'box_doors', 's': NS + 'block/fallow_box', 'f': 'mud_flap'}
    if stage == 0:
        e = [element([2, 9, 0], [30, 16, 16], {'all': 'k'})]
        for x0, side in ((-0.5, 'west'), (27.5, 'east')):
            faces = {'north': 'y', 'south': 'y', 'up': 'y', 'down': 'y', 'east': 'y', 'west': 'y'}; faces[side] = 'h'
            e.append(element([x0, 0, 3], [x0 + 4.5, 10, 13], faces))
            e.append(element([x0 + (4.5 if side == 'west' else -4.5), 0, 3], [x0 + (9 if side == 'west' else 0), 10, 13], {k: 'y' for k in ('north', 'south', 'up', 'down', 'east', 'west')}))
        return model(e, t, 'box_frame', render=None)
    if stage == 1:
        e = []
        for x in (3, 26):
            e += [element([x, 1, 6], [x + 3, 16, 9], {'all': 'm'}), element([x - 1, 0, 5], [x + 4, 1, 10], {'all': 'm'})]
        e += [element([6, 12, 7], [26, 13, 8], {'all': 'm'}), element([29, 9, 7], [31, 10, 8], {'all': 'm'})]
        return model(e, t, 'hitch_metal', render=None)
    if stage == 2:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'd', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'box_doors', render=None)
    return {'textures': {'particle': NS + 'block/fallow_box'}, 'elements': []}


def fitting_models():
    m = {}
    for s in range(4):
        m[f'counter_{s}'] = counter_model(s)
    m['note_0'] = note_model(); m['stool_0'] = stool_model(); m['coffee_0'] = coffee_model(); m['tube_0'] = tube_model()
    for s in range(3):
        m[f'backshelf_{s}'] = backshelf_model(s); m[f'tv_{s}'] = tv_model(s); m[f'crock_{s}'] = crock_model(s)
    for s in range(4):
        m[f'cooler_{s}'] = cooler_model(s)
    for s in range(4):
        m[f'window_{s}'] = window_model(s)
    for s in range(8):
        m[f'rubble_{s}'] = rubble_model(s)
    for s in range(2):
        m[f'maps_{s}'] = maps_model(s)
        m[f'island_{s}'] = island_model(s); m[f'shelf_{s}'] = shelf_model(s); m[f'cot_{s}'] = cot_model(s)
    for s in range(4):
        m[f'gondola_{s}'] = gondola_model(s); m[f'signband_{s}'] = signband_model(s); m[f'pump_{s}'] = pump_model(s)
        m[f'wheat_{s}'] = wheat_model(s); m[f'farm_{s}'] = farm_model(s); m[f'box_{s}'] = box_model(s)
    for s in range(7):
        m[f'canopy_{s}'] = canopy_model(s); m[f'lift_{s}'] = lift_model(s)
    for s in range(3):
        m[f'sign_{s}'] = sign_model(s)
    for s in range(5):
        m[f'pole_{s}'] = pole_model(s)
    for s in range(8):
        m[f'pad_{s}'] = pad_model(s); m[f'trailer_{s}'] = trailer_model(s); m[f'rig_{s}'] = rig_model(s)
    for s in range(6):
        m[f'cellar_{s}'] = cellar_model(s, False); m[f'cellar_open_{s}'] = cellar_model(s, True)
    for s in range(4):
        m[f'bean_{s}'] = bean_model(s)
    m['crock_3'] = crock_model(3)
    return m


def bean_model(stage):
    # Opal's bean in its paper cup on the ground (0.4.673): the cup six pixels across with its rolled rim, the soil a little below the
    # rim, and the plant on two crossed sheets, as vanilla's own saplings stand, taller at each stage.
    t = {'c': 'bean_cup', 's': 'bean_soil', 'b': f'bean_{stage}'}
    h = (8, 13, 16, 16)[stage]
    side = {k: [5, 9, 11, 15] for k in ('north', 'south', 'east', 'west', 'down')}
    e = [element([5, 0, 5], [11, 5.5, 11], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c', 'up': 's'}, uv={**side, 'up': [5, 5, 11, 11]}),
         element([4.75, 5, 4.75], [11.25, 6, 11.25], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}, uv={k: [5, 9, 11, 10] for k in ('north', 'south', 'east', 'west')})]
    if stage == 3:
        # 0.4.681: grown long and thin in the cellar's dark, it leans toward the way its block faces (the steps, below): its two sheets
        # stand square across each other and both tip that way from the soil, as a seedling leans to a crack of light.
        e.append(element([2, 5.5, 8], [14, 5.5 + h, 8], {'north': 'b', 'south': 'b'}, uv={'north': [2, 16 - h, 14, 16], 'south': [2, 16 - h, 14, 16]},
                         rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 5.5, 8]}))
        e.append(element([8, 5.5, 2], [8, 5.5 + h, 14], {'east': 'b', 'west': 'b'}, uv={'east': [2, 16 - h, 14, 16], 'west': [2, 16 - h, 14, 16]},
                         rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 5.5, 8]}))
        return model(e, t, 'bean_cup')
    for angle in (45, -45):
        e.append(element([2, 5.5, 8], [14, 5.5 + h, 8], {'north': 'b', 'south': 'b'}, uv={'north': [2, 16 - h, 14, 16], 'south': [2, 16 - h, 14, 16]},
                         rotation={'angle': angle, 'axis': 'y', 'origin': [8, 8, 8]}))
    return model(e, t, 'bean_cup')


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/fallow_{name}.png'] = im
    faces = {
        'counter_front': counter_front(), 'counter_back': counter_back(), 'formica': formica(), 'counter_edge': counter_edge(),
        'register': register(), 'register_keys': register_keys(), 'register_flags': register_flags(), 'candy': candy(), 'wire': wire(),
        'note': note(), 'stool_seat': stool_seat(), 'chrome': chrome(), 'cabinet': cabinet(), 'shelf_board': shelf_board(),
        'cartons': cartons(), 'cans': cans(), 'radio': radio(), 'tv_case': tv_case(), 'tv_0': tv_screen(0), 'tv_1': tv_screen(1),
        'tv_2': tv_screen(2), 'cooler_side': cooler_side(), 'cooler_0': cooler_door(0), 'cooler_1': cooler_door(1), 'cooler_2': cooler_door(2), 'cooler_3': cooler_door(3),
        'gondola_0': gondola_face(0), 'gondola_1': gondola_face(1), 'gondola_2': gondola_face(2), 'gondola_end': gondola_end(),
        'gondola_top': gondola_top(), 'maps': maps(), 'maps_header': maps_header(), 'coffee_stand': coffee_stand(),
        'coffee_machine': coffee_machine(), 'coffee_pot': coffee_pot(), 'cups': cups(), 'window_frame': window_frame(),
        'window_glass': window_glass(), 'tube_light': tube_light(), 'signband_back': signband_back(),
        'pump_body': pump_body(0), 'pump_head': pump_head(0), 'pump_side': pump_side(0), 'diesel_body': pump_body(1),
        'diesel_head': pump_head(1), 'diesel_side': pump_side(1), 'hose': hose(), 'nozzle': nozzle(), 'island_side': island_side(),
        'island_top': island_top(), 'canopy_soffit': canopy_soffit(), 'canopy_fascia': canopy_fascia(), 'canopy_light': canopy_light(),
        'column': column(), 'column_marks': column_marks(), 'collar': collar(), 'sign_pole': sign_pole(), 'name_board': name_board(),
        'price_board': price_board(), 'sign_trim': sign_trim(), 'pole_wood': pole_wood(), 'crossarm': crossarm(), 'insulator': insulator(),
        'wire': wire_tex(), 'chainlink': chainlink(), 'galv': galv(), 'barb': barb(), 'gov_sign': gov_sign(), 'flood_lit': flood(1),
        'lid': lid(), 'lid_edge': lid_edge(), 'hatch': hatch(), 'lift_post': lift_post(), 'lift_arm': lift_arm(), 'bench_top': bench_top(),
        'bench_front': bench_front(), 'pegboard': pegboard(), 'tyre_side': tyre_side(), 'tyre_top': tyre_top(), 'drum': drum(),
        'rags': rags(), 'trailer_window': trailer_window(), 'hitch_metal': hitch_metal(), 'cinder': cinder(), 'propane': propane(),
        'post_wood': post_wood(), 'clothes': clothes(), 'line_rope': line_rope(), 'barrel': barrel(), 'ash': ash(),
        'field_fence': field_fence(), 'cellar_door': cellar_door(), 'cellar_under': cellar_under(), 'curb_concrete': curb_concrete(),
        'jars': jars(), 'tins': tins(), 'cot_canvas': cot_canvas(), 'blanket': blanket(), 'crock': crock(),
        'lantern_lit': lantern_glass(1), 'lantern_dark': lantern_glass(0), 'lantern_low': lantern_glass(2), 'tin': tin(), 'farm_window': farm_window(),
        'porch_paint': porch_paint(), 'mailbox': mailbox(), 'mailbox_front': mailbox_front(), 'flag': flag(), 'rig_paint': rig_paint(),
        'rig_door': rig_door(), 'rig_cab_side': rig_cab_side(), 'windshield': windshield(), 'grille': grille(),
        'tyre_tread': tyre_tread(), 'hub': hub(), 'headlight': headlight(), 'sleeper_side': sleeper_side(), 'box_doors': box_doors(),
        'box_frame': box_frame(), 'mud_flap': mud_flap(), 'bean_cup': bean_cup(), 'bean_soil': bean_soil(),
        'debris_top': debris_top(), 'debris_side': debris_side(), 'joist': joist(), 'window_shards_0': window_shards(0),
        'window_shards_1': window_shards(1),
    }
    faces['wire'] = wire()            # the racks' wire; the telephone line has its own below
    faces['line_wire'] = wire_tex()
    for i in range(4):
        faces[f'signband_{i}'] = signband(i)
        faces[f'wheat_{i}'] = wheat(i)
        faces[f'bean_{i}'] = bean_plant(i)
    for name, im in faces.items():
        out[f'block/fallow/{name}.png'] = im
    # 0.4.673: what the afternoon's people carry: a quart of milk, a bottle of pop, a cup from the machine (the bean's cup is its block).
    for name, draw in ITEM_ART.items():
        out[f'item/fallow_{name}.png'] = draw()
    return out


KINDS = ['counter', 'note', 'stool', 'backshelf', 'tv', 'cooler', 'gondola', 'maps', 'coffee', 'window', 'tube', 'signband', 'pump',
         'island', 'canopy', 'sign', 'pole', 'wheat', 'pad', 'lift', 'trailer', 'cellar', 'cellar_open', 'shelf', 'cot', 'crock', 'farm',
         'rig', 'box', 'bean', 'rubble']
STAGES = {'counter': 4, 'backshelf': 3, 'tv': 3, 'cooler': 4, 'gondola': 4, 'maps': 2, 'window': 4, 'signband': 4, 'pump': 4, 'island': 2,
          'canopy': 7, 'sign': 3, 'pole': 5, 'wheat': 4, 'pad': 8, 'lift': 7, 'trailer': 8, 'cellar': 6, 'cellar_open': 6, 'shelf': 2,
          'cot': 2, 'crock': 4, 'farm': 4, 'rig': 8, 'box': 4, 'bean': 4, 'rubble': 8}
ITEM_ART = {'milk': milk_carton, 'soda': soda_bottle, 'coffee': coffee_cup}
ITEM_NAMES = {'fallow_milk': 'Quart of Milk', 'fallow_soda': 'Bottle of Pop', 'fallow_coffee': 'Cup of Coffee', 'fallow_bean_cup': 'Bean in a Cup'}
# The bean's cup held in a hand: its block model, turned and sized as vanilla holds a block, a little larger since the cup is small.
BEAN_HELD = {'gui': {'rotation': [30, 225, 0], 'translation': [0, 2, 0], 'scale': [1, 1, 1]},
             'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
             'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.75, 0.75, 0.75]},
             'thirdperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 1, 2.5], 'scale': [0.6, 0.6, 0.6]},
             'thirdperson_lefthand': {'rotation': [0, 45, 0], 'translation': [0, 1, 2.5], 'scale': [0.6, 0.6, 0.6]},
             'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 2, 0], 'scale': [0.7, 0.7, 0.7]},
             'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, 2, 0], 'scale': [0.7, 0.7, 0.7]}}
STAGE_MAX = 8
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MATERIAL_NAMES = {'siding': 'Painted Clapboard', 'plinth': 'Painted Block', 'lino': 'Store Linoleum', 'ceiling': 'Beadboard Ceiling',
                  'roof': 'Tar and Gravel', 'apron': 'Forecourt Concrete', 'asphalt': 'Highway Asphalt', 'road_line': 'Center Line',
                  'shoulder': 'Gravel Shoulder', 'metal': 'Corrugated Iron', 'garage_floor': 'Garage Floor', 'trailer': 'Trailer Siding',
                  'trailer_roof': 'Trailer Roof', 'skirting': 'Trailer Skirting', 'yard': 'Trodden Yard', 'soil': 'Field Soil',
                  'track': 'Dirt Track', 'gravel': 'Pad Gravel', 'cellar_stone': 'Fieldstone', 'cellar_floor': 'Cellar Floor',
                  'cellar_roof': 'Cellar Roof', 'farm_siding': 'Farmhouse Siding', 'farm_shingle': 'Asphalt Shingles', 'porch': 'Porch Boards',
                  'brick': 'Chimney Brick', 'box': 'Trailer Box'}


def stair_states(model_name):
    """Stairs' block states laid out as vanilla's own: every facing, half and shape, turned alike."""
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
                v = {'model': model_name + kind}
                if half == 'top':
                    v['x'] = 180
                y %= 360
                if y:
                    v['y'] = y
                if half == 'top' or y:
                    v['uvlock'] = True
                out[f'facing={facing},half={half},shape={shape}'] = v
    return out


def files():
    out = {}
    for name in materials():
        if name == 'cellar_roof':
            # The cellar's roof is the yard on top and the cellar's ceiling of boards below.
            out['models/block/fallow_cellar_roof.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
                'top': NS + 'block/fallow_yard', 'bottom': NS + 'block/fallow_cellar_roof', 'side': NS + 'block/fallow_yard'}}
        else:
            out[f'models/block/fallow_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/fallow_{name}'}}
        out[f'blockstates/fallow_{name}.json'] = {'variants': {'': {'model': NS + f'block/fallow_{name}'}}}
    for stairs, side, top in (('fallow_shingle_stairs', 'fallow_farm_shingle', 'fallow_farm_shingle'), ('fallow_cellar_stairs', 'fallow_cellar_stone', 'fallow_cellar_stone')):
        tex = {'bottom': NS + 'block/' + top, 'top': NS + 'block/' + top, 'side': NS + 'block/' + side}
        out[f'models/block/{stairs}.json'] = {'parent': 'minecraft:block/stairs', 'textures': tex}
        out[f'models/block/{stairs}_inner.json'] = {'parent': 'minecraft:block/inner_stairs', 'textures': tex}
        out[f'models/block/{stairs}_outer.json'] = {'parent': 'minecraft:block/outer_stairs', 'textures': tex}
        out[f'blockstates/{stairs}.json'] = {'variants': stair_states(NS + f'block/{stairs}')}
    for key, obj in fitting_models().items():
        out[f'models/block/fallow/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/fallow/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/fallow_fitting.json'] = {'variants': variants}
    for name in ITEM_ART:
        out[f'models/item/fallow_{name}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/fallow_{name}'}}
    out['models/item/fallow_bean_cup.json'] = {'parent': P + 'bean_0', 'display': BEAN_HELD}
    # 0.4.680: a reader's shadow on the station's front is drawn by its own renderer, from the body they wore; the block draws nothing.
    out['models/block/fallow_shadow.json'] = {'textures': {'particle': NS + 'block/fallow_siding'}}
    out['blockstates/fallow_shadow.json'] = {'variants': {f'facing={f}': {'model': NS + 'block/fallow_shadow'} for f in FACING_Y}}
    return out


NAMES = {'fallow_fitting': 'Station Fitting', 'fallow_shingle_stairs': 'Shingle Stairs', 'fallow_cellar_stairs': 'Cellar Steps', 'fallow_shadow': 'Shadow',
         **{f'fallow_{k}': v for k, v in MATERIAL_NAMES.items()}}


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
                assert f'block/fallow/{tex[len(P):]}.png' in images, (key, tex)
            elif tex.startswith(NS + 'block/fallow_'):
                assert f'block/{tex[len(NS) + 6:]}.png' in images, (key, tex)
        for e in obj['elements']:
            for k in range(3):
                assert -16 <= e['from'][k] <= 32 and -16 <= e['to'][k] <= 32, (key, e)
            if 'rotation' in e:
                assert e['rotation']['angle'] in (-45, -22.5, 0, 22.5, 45), (key, e)
    # Every texture keeps to whole blocks of sixteen pixels, so the atlas keeps its mipmaps.
    for rel, im in images.items():
        assert im.size[0] % 16 == 0 and im.size[1] % 16 == 0, (rel, im.size)
    # The station's name reads across its four boards, and the sign by the road says it again larger.
    assert all(any(images[f'block/fallow/signband_{i}.png'].getpixel((x, y))[:3] == rgb('#b8352b') for x in range(16) for y in range(4, 11)) for i in range(4))
    assert images['block/fallow/name_board.png'].size == (48, 16) and images['block/fallow/price_board.png'].size == (48, 16)
    # Ripe wheat stands tall; stubble keeps to the foot of its block; the burnt stubble is dark.
    def opaque(im, rows):
        return sum(im.getpixel((x, y))[3] > 0 for x in range(16) for y in rows)
    assert opaque(images['block/fallow/wheat_0.png'], range(0, 8)) > 12 and opaque(images['block/fallow/wheat_2.png'], range(0, 11)) == 0
    burnt = [images['block/fallow/wheat_3.png'].getpixel((x, y)) for x in range(16) for y in range(16)]
    assert sum(sum(p[:3]) for p in burnt if p[3]) / max(1, sum(1 for p in burnt if p[3])) < 240
    # Chain-link is mesh: more of each block is clear than wire.
    mesh = images['block/fallow/chainlink.png']
    assert 0.25 < sum(mesh.getpixel((x, y))[3] > 0 for x in range(16) for y in range(16)) / 256 < 0.6
    # The plate glass is mostly clear.
    glass = images['block/fallow/window_glass.png']
    assert sum(glass.getpixel((x, y))[3] for x in range(16) for y in range(16)) / 256 < 80
    # 0.4.680: a blown window keeps its glass only at the edge it hangs from: the lower half's at its foot, the upper's at its head;
    # its middle is open.
    for upper in (0, 1):
        shards = images[f'block/fallow/window_shards_{upper}.png']
        held = range(0, 3) if upper else range(13, 16)
        assert sum(shards.getpixel((x, y))[3] > 0 for x in range(16) for y in held) > 24, upper
        assert sum(shards.getpixel((x, y))[3] > 0 for x in range(2, 14) for y in range(7, 9)) == 0, upper
    # The fall of the store is darker at the foot of its bank than at its top.
    side = images['block/fallow/debris_side.png']
    assert sum(sum(side.getpixel((x, 1))[:3]) for x in range(16)) > sum(sum(side.getpixel((x, 14))[:3]) for x in range(16))
    # Two of the pegboard's outlines are empty: the pixels inside them show the board, not a tool.
    peg = images['block/fallow/pegboard.png']
    assert peg.getpixel((11, 1))[:3] != rgb('#9ea2a6')

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
        print(f'Drew the station under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **{f'item.the_oldest_house.{k}': v for k, v in ITEM_NAMES.items()}}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the station: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
