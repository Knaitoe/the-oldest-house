"""The list (0.4.699): Lamesa, a cotton town on the South Plains, on a night in July.

Draws, in native Minecraft pixels from fixed seeds: the town's materials (the streets' caliche, Main Street's asphalt, its painted line,
its curbs and sidewalks; the fields' soil, the lots' dirt, the yards' grass, the playa's crust and the tracks' ballast; the houses' lap
siding, their brick wainscot and their shingles; the stores' flat roofs; carpet, linoleum, painted wallboard, paneling and popcorn
ceilings inside; the stores' tile, the video store's carpet, the fronts' buff brick, stucco and block; the gin's galvanized and rusted
iron and its slab floor; the shop's painted steel; the drive-in's lot), the roofs' stairs, the houses' panel doors and the stores'
glass doors, and every fitting of the list_fitting block, its models facing north and turned by its facing (TheListBlock lists them).

Part 1b (0.4.700) adds the school's field west of town (the turf and its lines, the track, the goalposts, the light towers, the stands
and the press box, the scoreboard, the ticket booth, the concession stand, the field house and its lockers), Amber's house (her den's
television and VCR, the shelf of rented tapes, her desk at her window) and the things in the neighbors' yards, and a steel door.

Part 1c (0.4.701) adds the pasture south of town: buffalo grass and trodden mud, barbed wire on cedar posts with a wire gap and a cattle
guard, a stock tank with its windmill (the wheel drawn apart, for its renderer to turn), three pumpjacks (their beam, cranks, pitman arms
and rod drawn apart for theirs), a tank battery, the lease's sign and the wells' plates, and the machine block's states.

Part 2 (0.4.705) adds the feed sack (on the head, held, and from inside) and the broken door. Part 3a (0.4.706) adds night one's
party at the stock tank (a portable radio, an ice chest, empty cans, a camp lantern, a blanket, an empty case) and the seed house's lamp
and bulb gone dark. Part 4a-2 (0.4.709) adds the hay hook, clean and after a finish, and the blood thrown off it.

Every title, name and word on a box, a sign or a poster here is made up for this town, except the town's and the county's own names.

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
P = NS + 'block/the_list/'


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
# A narrow face (three pixels wide, five high) for street blades, tape boxes and small signs, and a broad one (five by seven) for the
# stores' names and the tower.
TALL = {
    'A': ['.#.', '#.#', '###', '#.#', '#.#'], 'B': ['##.', '#.#', '##.', '#.#', '##.'], 'C': ['.##', '#..', '#..', '#..', '.##'],
    'D': ['##.', '#.#', '#.#', '#.#', '##.'], 'E': ['###', '#..', '##.', '#..', '###'], 'F': ['###', '#..', '##.', '#..', '#..'],
    'G': ['.##', '#..', '#.#', '#.#', '.##'], 'H': ['#.#', '#.#', '###', '#.#', '#.#'], 'I': ['###', '.#.', '.#.', '.#.', '###'],
    'J': ['..#', '..#', '..#', '#.#', '.#.'], 'K': ['#.#', '#.#', '##.', '#.#', '#.#'], 'L': ['#..', '#..', '#..', '#..', '###'],
    'M': ['#.#', '###', '###', '#.#', '#.#'], 'N': ['##.', '#.#', '#.#', '#.#', '#.#'], 'O': ['###', '#.#', '#.#', '#.#', '###'],
    'P': ['##.', '#.#', '##.', '#..', '#..'], 'Q': ['.#.', '#.#', '#.#', '##.', '.##'], 'R': ['##.', '#.#', '##.', '#.#', '#.#'],
    'S': ['.##', '#..', '.#.', '..#', '##.'], 'T': ['###', '.#.', '.#.', '.#.', '.#.'], 'U': ['#.#', '#.#', '#.#', '#.#', '###'],
    'V': ['#.#', '#.#', '#.#', '#.#', '.#.'], 'W': ['#.#', '#.#', '###', '###', '#.#'], 'X': ['#.#', '#.#', '.#.', '#.#', '#.#'],
    'Y': ['#.#', '#.#', '.#.', '.#.', '.#.'], 'Z': ['###', '..#', '.#.', '#..', '###'],
    '0': ['###', '#.#', '#.#', '#.#', '###'], '1': ['.#.', '##.', '.#.', '.#.', '###'], '2': ['##.', '..#', '.#.', '#..', '###'],
    '3': ['##.', '..#', '.#.', '..#', '##.'], '4': ['#.#', '#.#', '###', '..#', '..#'], '5': ['###', '#..', '##.', '..#', '##.'],
    '6': ['.##', '#..', '###', '#.#', '###'], '7': ['###', '..#', '.#.', '.#.', '.#.'], '8': ['###', '#.#', '###', '#.#', '###'],
    '9': ['###', '#.#', '###', '..#', '##.'], '&': ['.#.', '#.#', '.#.', '#.#', '.##'], '-': ['...', '...', '###', '...', '...'],
    '.': ['.', '.', '.', '.', '#'], "'": ['#', '#', '.', '.', '.'], ' ': ['..', '..', '..', '..', '..'],
}
BROAD = {
    'A': ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'], 'B': ['####.', '#...#', '#...#', '####.', '#...#', '#...#', '####.'],
    'C': ['.###.', '#...#', '#....', '#....', '#....', '#...#', '.###.'], 'D': ['####.', '#...#', '#...#', '#...#', '#...#', '#...#', '####.'],
    'E': ['#####', '#....', '#....', '####.', '#....', '#....', '#####'], 'F': ['#####', '#....', '#....', '####.', '#....', '#....', '#....'],
    'G': ['.###.', '#...#', '#....', '#.###', '#...#', '#...#', '.###.'], 'H': ['#...#', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'I': ['###', '.#.', '.#.', '.#.', '.#.', '.#.', '###'], 'K': ['#...#', '#..#.', '#.#..', '##...', '#.#..', '#..#.', '#...#'],
    'L': ['#....', '#....', '#....', '#....', '#....', '#....', '#####'], 'M': ['#...#', '##.##', '#.#.#', '#.#.#', '#...#', '#...#', '#...#'],
    'N': ['#...#', '##..#', '#.#.#', '#..##', '#...#', '#...#', '#...#'], 'O': ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'],
    'P': ['####.', '#...#', '#...#', '####.', '#....', '#....', '#....'], 'R': ['####.', '#...#', '#...#', '####.', '#.#..', '#..#.', '#...#'],
    'S': ['.####', '#....', '#....', '.###.', '....#', '....#', '####.'], 'T': ['#####', '..#..', '..#..', '..#..', '..#..', '..#..', '..#..'],
    'U': ['#...#', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'], 'V': ['#...#', '#...#', '#...#', '#...#', '#...#', '.#.#.', '..#..'],
    'W': ['#...#', '#...#', '#...#', '#.#.#', '#.#.#', '##.##', '#...#'], 'Y': ['#...#', '#...#', '.#.#.', '..#..', '..#..', '..#..', '..#..'],
    '&': ['.##..', '#..#.', '#.#..', '.#...', '#.#.#', '#..#.', '.##.#'], '-': ['...', '...', '...', '###', '...', '...', '...'],
    ' ': ['..', '..', '..', '..', '..', '..', '..'],
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


def centre(im, y, s, font, colour, shadow=None, width=None, x0=0):
    w = width or im.size[0]
    return write(im, x0 + (w - text_width(s, font)) // 2, y, s, font, colour, shadow)


def split(im, n):
    """A picture n blocks wide cut into its n sixteen-pixel boards, west to east."""
    return [im.crop((16 * i, 0, 16 * i + 16, im.size[1])) for i in range(n)]


# ------------------------------------------------------------------------------------------------ materials
def caliche():
    # The streets: pale crushed caliche rolled hard, small pebbles and darker grit, a faint rut of tyres along it.
    rnd = random.Random(9101); im = new('#d5cbb0', 9101, 5)
    for x in range(16):
        for y in range(16):
            if (x + 3) % 16 in (4, 5, 11, 12) and rnd.random() < .5:
                darken(im, x, y, -9)
    speckle(im, rnd, 26, ['#e7e0cc', '#efe9d8', '#ddd4bc'])
    speckle(im, rnd, 18, ['#b3a587', '#a8997a', '#c2b597'])
    speckle(im, rnd, 5, ['#8e8066', '#9a8c70'])
    return im


def asphalt():
    rnd = random.Random(9102); im = new('#4b4946', 9102, 4)
    speckle(im, rnd, 40, ['#5d5a56', '#66625d', '#3f3d3a'])
    speckle(im, rnd, 10, ['#7a756e', '#827c74'])
    # A crack sealed with tar wanders across a corner.
    for i, (x, y) in enumerate([(10, 0), (10, 1), (11, 2), (11, 3), (12, 4), (12, 5), (13, 6), (14, 7), (15, 8)]):
        dot(im, x, y, '#2f2d2b')
    return im


def main_line():
    # Main Street's two painted lines down the middle, worn where tyres cross them; the street runs east and west.
    im = asphalt(); rnd = random.Random(9103)
    for y in (5, 6, 9, 10):
        for x in range(16):
            if rnd.random() < .86:
                dot(im, x, y, '#d2ae45' if rnd.random() < .8 else '#b99a40')
    return im


def curb():
    rnd = random.Random(9104); im = new('#bcb7ac', 9104, 4)
    fill(im, 0, 0, 16, 2, '#cfcabf'); fill(im, 0, 13, 16, 16, '#8f8a80')
    for x in range(0, 16, 8):
        for y in range(2, 13):
            dot(im, x, y, '#9e998e')
    speckle(im, rnd, 12, ['#a9a499', '#d3cec3'])
    speckle(im, rnd, 4, ['#6f6a62'], (0, 12, 16, 16))
    return im


def sidewalk():
    rnd = random.Random(9105); im = new('#c3beb2', 9105, 4)
    for i in range(16):
        dot(im, i, 0, '#9d988d'); dot(im, 0, i, '#9d988d')
    speckle(im, rnd, 18, ['#b2ada1', '#d2cdc2', '#bab5a9'])
    for x, y in [(4, 6), (5, 7), (6, 7), (7, 8), (8, 8), (9, 9)]:
        dot(im, x, y, '#8f8a7f')
    return im


def soil():
    # A cotton field's furrows: red-brown sandy loam laid in ridges along the rows (north and south), clods on the ridges.
    rnd = random.Random(9106); im = new('#93613f', 9106, 7)
    for x in range(16):
        band = x % 8
        k = 6 if band in (2, 3, 4) else -6 if band in (6, 7, 0) else 0
        for y in range(16):
            darken(im, x, y, k + rnd.randint(-3, 3))
    for _ in range(9):
        x, y = rnd.randint(1, 6), rnd.randrange(16)
        dot(im, x, y, '#b07c5a'); dot(im, x + 1, y, '#a87454'); dot(im, x, (y + 1) % 16, '#7a5034')
    speckle(im, rnd, 16, ['#6f472d', '#7a5034', '#865a3c'])
    speckle(im, rnd, 5, ['#c9b8a0', '#b8a68c'])
    return im


def dirt():
    rnd = random.Random(9107); im = new('#a5815c', 9107, 5)
    speckle(im, rnd, 20, ['#b8946c', '#977350', '#8e6c4a'])
    speckle(im, rnd, 6, ['#d0c3a8', '#c4b597'])
    for x, y in [(3, 4), (12, 9), (7, 13)]:
        dot(im, x, y, '#8a8a4a'); dot(im, x + 1, y - 1, '#9c9a56'); dot(im, x - 1, y - 1, '#7f7f44')
    return im


def lawn():
    # A yard in July: Bermuda grass gone half to straw, greener where the sprinkler reaches.
    rnd = random.Random(9108); im = new('#7c8648', 9108, 6)
    for _ in range(70):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#9b9656', '#a99f5f', '#6d7840', '#8a9150', '#b1a565']))
    speckle(im, rnd, 6, ['#8e7656'])
    return im


def playa():
    # The playa's crust: dried white, split into plates, a little salt glinting in the cracks.
    rnd = random.Random(9109); im = new('#e0dbcd', 9109, 3)
    cracks = [(0, 5), (1, 5), (2, 6), (3, 6), (4, 6), (5, 7), (5, 8), (5, 9), (6, 10), (7, 10), (8, 10), (9, 11), (10, 11), (11, 11),
              (12, 10), (13, 10), (14, 10), (15, 10), (9, 0), (9, 1), (9, 2), (8, 3), (8, 4), (7, 5), (6, 6), (12, 11), (12, 12),
              (12, 13), (13, 14), (13, 15), (2, 6), (2, 7), (1, 8), (1, 9), (0, 10)]
    for x, y in cracks:
        dot(im, x, y, '#b2aa96')
    for x, y in cracks[::3]:
        dot(im, (x + 1) % 16, y, '#c9c1ae')
    speckle(im, rnd, 8, ['#f3f0e6', '#ebe6d9'])
    return im


def ballast():
    rnd = random.Random(9110); im = new('#8a857d', 9110, 6)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16); c = rnd.choice(['#a39d93', '#6f6a63', '#9a9489', '#7c766e'])
        dot(im, x, y, c); dot(im, (x + 1) % 16, y, c); dot(im, x, (y + 1) % 16, shade(c, -12))
    return im


def siding():
    # The house's lap siding: boards four pixels high, each with its shadowed lower edge, the paint gone chalky in the sun.
    rnd = random.Random(9111); im = new('#d8d1bd', 9111, 3)
    for y in range(16):
        k = {0: 8, 1: 3, 2: 0, 3: -16}[y % 4]
        for x in range(16):
            darken(im, x, y, k)
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#b9ae95')
    for x in (5, 13):
        dot(im, x, 1, '#9f9682')
    return im


def wainscot():
    # A course of brick along the foot of the house's walls.
    rnd = random.Random(9112); im = new('#b4a797', 9112, 3)
    for row in range(4):
        y0 = row * 4; off = 4 if row % 2 else 0
        for b in range(-1, 3):
            x0 = b * 8 + off
            base = rnd.choice(['#8c4a35', '#94503a', '#7f4331', '#9a5640'])
            for x in range(x0 + 1, x0 + 8):
                for y in range(y0 + 1, y0 + 4):
                    if 0 <= x < 16:
                        dot(im, x, y, shade(base, rnd.randint(-6, 6)))
    return im


def shingle():
    rnd = random.Random(9113); im = new('#68605a', 9113, 9)
    for row in range(4):
        y0 = row * 4
        for x in range(16):
            dot(im, x, y0 + 3, shade('#433d38', rnd.randint(-3, 3)))
            dot(im, x, y0, shade('#7e766c', rnd.randint(-5, 5)))
        off = 2 if row % 2 else 6
        for x in range(off, 16, 8):
            dot(im, x, y0 + 1, '#4a443e'); dot(im, x, y0 + 2, '#4a443e')
    speckle(im, rnd, 34, ['#857c70', '#57504a', '#91877a', '#4f4943'])
    return im


def flat_roof():
    rnd = random.Random(9114); im = new('#58544e', 9114, 4)
    speckle(im, rnd, 50, ['#8a857b', '#9e988c', '#6c675f', '#a7a093'])
    return im


def carpet():
    # The house's carpet: a looped pile in a warm earth colour, worn paler down a path.
    rnd = random.Random(9115); im = new('#8b6a3f', 9115, 6)
    for y in range(16):
        for x in range(16):
            if (x + y * 3) % 5 == 0:
                darken(im, x, y, 10)
            elif (x * 2 + y) % 7 == 0:
                darken(im, x, y, -10)
    return im


def lino():
    # The kitchen's linoleum: mottled squares, eight pixels each, cream and ochre, the marbling running through both.
    rnd = random.Random(9116); im = new(None)
    for sx in range(2):
        for sy in range(2):
            base = '#cfc19a' if (sx + sy) % 2 == 0 else '#a8925f'
            for x in range(8):
                for y in range(8):
                    dot(im, sx * 8 + x, sy * 8 + y, shade(base, rnd.randint(-5, 5)))
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for i in range(4):
            dot(im, (x + i) % 16, (y + (i // 2)) % 16, '#8f7a4c')
    for i in range(16):
        dot(im, i, 0, '#8a7650'); dot(im, i, 8, '#8a7650'); dot(im, 0, i, '#8a7650'); dot(im, 8, i, '#8a7650')
    return im


def drywall():
    rnd = random.Random(9117); im = new('#d6cfbc', 9117, 2)
    speckle(im, rnd, 30, ['#ccc5b1', '#ded8c7'])
    return im


def paneling():
    # Den paneling: dark veneer in four-pixel planks with a deep groove between them and grain running down.
    rnd = random.Random(9118); im = new('#6b4a2e', 9118, 4)
    for x in range(16):
        for y in range(16):
            if x % 4 == 0:
                dot(im, x, y, '#3a271a')
            elif (y + x * 5) % 6 == 0:
                darken(im, x, y, -12)
            elif (y * 2 + x) % 9 == 0:
                darken(im, x, y, 10)
    return im


def ceiling():
    rnd = random.Random(9119); im = new('#e2ddd0', 9119, 3)
    speckle(im, rnd, 40, ['#d3cdbe', '#ebe7dc', '#cbc5b5'])
    return im


def tile():
    rnd = random.Random(9120); im = new(None)
    for sx in range(2):
        for sy in range(2):
            base = '#ddd6c4' if (sx + sy) % 2 == 0 else '#c9c0aa'
            for x in range(8):
                for y in range(8):
                    dot(im, sx * 8 + x, sy * 8 + y, shade(base, rnd.randint(-3, 3)))
    speckle(im, rnd, 20, ['#a39a86', '#b5ad99', '#8e8676'])
    for i in range(16):
        dot(im, i, 0, '#b0a893'); dot(im, 0, i, '#b0a893')
    return im


def store_carpet():
    # The video store's carpet: dark, with bright confetti flecks in it.
    rnd = random.Random(9121); im = new('#2d2e42', 9121, 3)
    speckle(im, rnd, 9, ['#c94a7a', '#3fa7b8', '#d8c04a', '#7c5cc8'])
    speckle(im, rnd, 10, ['#3a3b52', '#24253a'])
    return im


def bricks(base, mortar, seed, colours):
    rnd = random.Random(seed); im = new(mortar, seed, 3)
    for row in range(4):
        y0 = row * 4; off = 4 if row % 2 else 0
        for b in range(-1, 3):
            x0 = b * 8 + off; c = rnd.choice(colours)
            for x in range(x0 + 1, x0 + 8):
                for y in range(y0, y0 + 3):
                    if 0 <= x < 16:
                        dot(im, x, y, shade(c, rnd.randint(-5, 5)))
    return im


def storefront():
    return bricks('#c9a978', '#ddd1b8', 9122, ['#c9a978', '#c2a06e', '#d1b384', '#bb9866'])


def stucco():
    rnd = random.Random(9123); im = new('#d6cbb4', 9123, 4)
    speckle(im, rnd, 30, ['#c9bda4', '#e1d7c2', '#cfc3ab'])
    for x, y in [(2, 11), (3, 12), (3, 13), (4, 14)]:
        dot(im, x, y, '#b0a389')
    return im


def cinder():
    rnd = random.Random(9124); im = new('#a7a399', 9124, 5)
    for x in range(16):
        dot(im, x, 7, '#878379'); dot(im, x, 15, '#878379')
    for y in range(8):
        dot(im, 15, y, '#878379')
    for y in range(8, 16):
        dot(im, 7, y, '#878379')
    speckle(im, rnd, 22, ['#96928a', '#b6b2a8', '#8d8980'])
    return im


def corrugated(base, seed, rust):
    rnd = random.Random(seed); im = new(base, seed, 3)
    for x in range(16):
        k = (10, 4, -4, -12)[x % 4]
        for y in range(16):
            darken(im, x, y, k)
    for x in range(0, 16, 4):
        dot(im, x + 1, 2, '#6d7072'); dot(im, x + 1, 13, '#6d7072')
    if rust:
        for _ in range(rust):
            x = rnd.randrange(16); y = rnd.randrange(16); n = rnd.randint(3, 8)
            for i in range(n):
                dot(im, x, min(15, y + i), rnd.choice(['#8d5a37', '#9d683f', '#7a4c2f', '#a8744a']))
    else:
        speckle(im, rnd, 10, ['#c5c9ca', '#d3d6d6'])
    return im


def galvanized():
    return corrugated('#a8adaf', 9125, 0)


def rusted():
    return corrugated('#8e8a82', 9126, 9)


def slab():
    rnd = random.Random(9127); im = new('#9c988f', 9127, 5)
    speckle(im, rnd, 20, ['#8b877f', '#aaa69d'])
    for x, y in [(3, 3), (4, 3), (4, 4), (5, 5)]:
        dot(im, x, y, '#6a665e')
    for _ in range(3):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                darken(im, x + dx, y + dy, -10)
    speckle(im, rnd, 5, ['#e8e4d8'])  # a few strands of lint caught on the floor
    return im


def shop_metal():
    # The shop's painted R-panel: a raised rib every four pixels, the paint dirtied up from the ground.
    rnd = random.Random(9128); im = new('#b9a983', 9128, 3)
    for x in range(16):
        for y in range(16):
            if x % 4 == 0:
                darken(im, x, y, 14)
            elif x % 4 == 1:
                darken(im, x, y, -10)
    for y in range(12, 16):
        for x in range(16):
            if rnd.random() < (y - 11) * .15:
                darken(im, x, y, -12)
    return im


def drive_lot():
    rnd = random.Random(9129); im = new('#7c7873', 9129, 4)
    speckle(im, rnd, 26, ['#8b8781', '#6d6964', '#94908a'])
    for cx, cy, r in [(5, 6, 2), (11, 12, 1)]:
        for x in range(cx - r, cx + r + 1):
            for y in range(cy - r, cy + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r + 1:
                    darken(im, x, y, -26)
    return im


def materials():
    return {'caliche': caliche(), 'asphalt': asphalt(), 'main_line': main_line(), 'curb': curb(), 'sidewalk': sidewalk(), 'soil': soil(),
            'dirt': dirt(), 'lawn': lawn(), 'playa': playa(), 'ballast': ballast(), 'siding': siding(), 'wainscot': wainscot(),
            'shingle': shingle(), 'flat_roof': flat_roof(), 'carpet': carpet(), 'lino': lino(), 'drywall': drywall(), 'paneling': paneling(),
            'ceiling': ceiling(), 'tile': tile(), 'store_carpet': store_carpet(), 'storefront': storefront(), 'stucco': stucco(),
            'cinder': cinder(), 'galvanized': galvanized(), 'rusted': rusted(), 'slab': slab(), 'shop_metal': shop_metal(),
            'drive_lot': drive_lot(), 'turf': turf(0), 'turf_dark': turf(1), 'turf_line': turf_line(0), 'turf_side': turf_line(1),
            'endzone': endzone(), 'track_cinder': track_cinder(), 'pasture': pasture(), 'mud': mud()}


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


def glow(elements, light=15):
    for e in elements:
        e['neoforge_data'] = {'block_light': light, 'sky_light': 0}
    return elements


def box_el(lo, hi, t, uv=None):
    return element(lo, hi, {'all': t}, uv=uv)


def cross(t, h=16, inset=0.8):
    # Two planes crossed on the diagonals, as vanilla's own crops and grass stand.
    r = {'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]}
    return [element([inset, 0, 8], [16 - inset, h, 8], {'north': t, 'south': t}, uv={'north': [0, 16 - h, 16, 16], 'south': [0, 16 - h, 16, 16]}, rotation=r),
            element([8, 0, inset], [8, h, 16 - inset], {'east': t, 'west': t}, uv={'east': [0, 16 - h, 16, 16], 'west': [0, 16 - h, 16, 16]}, rotation=r)]


# ------------------------------------------------------------------------------------------------ the ground's growth
LEAF, LEAF_D, LEAF_L = '#4f6e36', '#3a5428', '#6a8a45'
BOLL, BOLL_S = '#f1eee4', '#cfc9bb'
BURR = '#6b4b2e'


def cotton(stage):
    # A cotton plant in July, knee high: a woody stem, broad lobed leaves, and bolls split open on it, white lint spilling from the
    # brown burrs; a short one; one bowed over at the end of its row; and the bare stalks of last year's plant left standing.
    rnd = random.Random(9200 + stage); im = new(None)
    top = {0: 2, 1: 6, 2: 3, 3: 4}[stage]
    lean = 3 if stage == 2 else 0
    for y in range(top, 16):
        x = 8 + (lean * (16 - y) // 14 if lean else 0)
        dot(im, x, y, '#5a4a30' if stage != 3 else '#7a6040')
        if y > 9:
            dot(im, x - 1, y, '#4a3d28' if stage != 3 else '#6a5236')
    branches = [(top + 3, -1), (top + 5, 1), (top + 7, -1), (top + 9, 1)]
    for by, side in branches:
        if by >= 15:
            continue
        bx = 8 + (lean * (16 - by) // 14 if lean else 0)
        for i in range(1, 5):
            dot(im, bx + side * i, by - i // 2, '#5c4a30' if stage != 3 else '#7a6040')
    if stage != 3:
        for _ in range(9 if stage != 1 else 6):
            cx = rnd.randint(3, 12) + (lean // 2); cy = rnd.randint(top + 1, 13)
            for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1), (1, 1), (-1, 1)):
                if rnd.random() < .85:
                    dot(im, cx + dx, cy + dy, rnd.choice([LEAF, LEAF_D, LEAF_L, LEAF]))
    bolls = {0: 6, 1: 3, 2: 4, 3: 3}[stage]
    for i in range(bolls):
        bx = rnd.randint(4, 11) + (lean // 2); by = rnd.randint(top, 12)
        dot(im, bx, by + 1, BURR); dot(im, bx + 1, by + 1, BURR)
        for dx, dy in ((0, 0), (1, 0), (0, -1), (1, -1), (-1, 0), (2, 0)):
            dot(im, bx + dx, by + dy, BOLL if dy or dx in (0, 1) else BOLL_S)
        dot(im, bx + 1, by, BOLL_S)
    return im


def mesquite_bark():
    rnd = random.Random(9210); im = new('#4a3b2f', 9210, 5)
    for x in range(16):
        for y in range(16):
            if (x * 3 + y // 3) % 5 == 0:
                dot(im, x, y, '#2e241c')
            elif (x + y * 2) % 11 == 0:
                darken(im, x, y, 12)
    return im


def mesquite_end():
    im = new('#6e5238', 9211, 4)
    for r, c in ((7, '#4a3b2f'), (5, '#8a6a48'), (3, '#9a7a56'), (1, '#6e5238')):
        for x in range(16):
            for y in range(16):
                if abs((x - 7.5) ** 2 + (y - 7.5) ** 2 - r * r) < r:
                    dot(im, x, y, c)
    return im


def mesquite_leaves(thin):
    # Mesquite's leaves: fine paired leaflets in feathery sprays, light olive, with sky through them; thinner at a canopy's edge.
    rnd = random.Random(9212 + thin); im = new(None)
    n = 15 if thin else 28
    for _ in range(n):
        x0, y0 = rnd.randrange(16), rnd.randrange(16); dx, dy = rnd.choice([(1, 0), (1, 1), (0, 1), (-1, 1)])
        for i in range(5):
            x, y = (x0 + dx * i) % 16, (y0 + dy * i) % 16
            dot(im, x, y, '#5d5a36')
            for sx, sy in ((dy, -dx), (-dy, dx)):
                if rnd.random() < .8:
                    dot(im, (x + sx) % 16, (y + sy) % 16, rnd.choice(['#7d8a4c', '#8e9a58', '#6e7c42', '#99a462']))
    for _ in range(3 if not thin else 1):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#b8a660'); dot(im, (x + 1) % 16, y, '#a8955a')  # a bean pod hanging
    return im


def tumbleweed():
    rnd = random.Random(9220); im = new(None)
    for _ in range(70):
        a = rnd.random() * math.tau; r = rnd.random() ** .5 * 7
        x, y = int(7.5 + math.cos(a) * r), int(7.5 + math.sin(a) * r)
        dot(im, x, y, rnd.choice(['#a08b62', '#8a7652', '#b49e72', '#6e5c3e']))
    return im


def weed(kind):
    rnd = random.Random(9221 + kind); im = new(None)
    if kind == 1:  # broomweed: thin green stems branching up into tiny heads
        for i in range(5):
            x = 4 + i * 2
            for y in range(6, 16):
                if rnd.random() < .9:
                    dot(im, x + (y % 3 == 0), y, '#6f7a3c')
            dot(im, x, 5, '#a3a650'); dot(im, x + 1, 4, '#b6b45a'); dot(im, x - 1, 5, '#8f9447')
    else:  # dry grass: tall pale blades bent over
        for i in range(9):
            x = rnd.randint(2, 13); h = rnd.randint(6, 12); bend = rnd.choice([-1, 1])
            for j in range(h):
                dot(im, x + (bend if j > h * 2 // 3 else 0), 15 - j, rnd.choice(['#b9a571', '#a8945f', '#c7b582', '#8f8152']))
    return im


def goathead():
    rnd = random.Random(9223); im = new(None)
    for _ in range(5):
        cx, cy = rnd.randint(3, 12), rnd.randint(3, 12)
        for i in range(5):
            a = i * math.tau / 5 + rnd.random()
            for r in range(1, 4):
                dot(im, int(cx + math.cos(a) * r), int(cy + math.sin(a) * r), '#5f6e34')
        dot(im, cx, cy, '#d0b93c'); dot(im, cx + 2, cy + 2, '#8a7a4a')
    return im


def pear_pad():
    rnd = random.Random(9224); im = new('#5f7f3e', 9224, 6)
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 / 56 + (y - 7.5) ** 2 / 64 > 1:
                im.putpixel((x, y), (0, 0, 0, 0))
    for _ in range(14):
        x, y = rnd.randint(2, 13), rnd.randint(2, 13)
        if im.getpixel((x, y))[3]:
            dot(im, x, y, '#d8d0a8'); dot(im, x, y + 1, '#4a6430')
    return im


def cotton_model(stage):
    return model(cross('c'), {'c': f'cotton_{stage}'}, f'cotton_{stage}')


def mesquite_model(stage):
    t = {'b': 'mesquite_bark', 'e': 'mesquite_end', 'l': 'mesquite_leaves', 'm': 'mesquite_leaves_thin'}
    side = {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}
    if stage == 0:
        # The trunk, crooked: two lengths set off each other a pixel, as mesquite never grows straight.
        return model([element([5.5, 0, 6], [10, 9, 10.5], {**side, 'down': 'e'}), element([6.5, 8, 5], [11, 16, 9.5], {**side, 'up': 'e'})], t, 'mesquite_bark', render=None)
    if stage == 1:
        # The fork: the trunk dividing into two limbs leaning out east and west.
        return model([element([6, 0, 6], [10, 7, 10], {**side, 'down': 'e'}),
                      element([6.5, 5, 6.5], [9.5, 17, 9.5], {**side, 'up': 'e'}, rotation={'angle': 22.5, 'axis': 'z', 'origin': [8, 6, 8]}),
                      element([7, 5, 7], [10, 17, 10], {**side, 'up': 'e'}, rotation={'angle': -22.5, 'axis': 'z', 'origin': [8, 6, 8]}),
                      element([7, 6, 7], [9.5, 15, 9.5], {**side, 'up': 'e'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 6, 8]})], t, 'mesquite_bark', render=None)
    if stage in (2, 3):
        leaf = 'l' if stage == 2 else 'm'
        e = [element([0, 0, 0], [16, 16, 16], {'all': leaf})]
        # Sprays standing out of the canopy's faces on two crossed sheets, so it reads as feathery and not as a cube.
        e += [element([-2, 2, 8], [18, 14, 8], {'north': leaf, 'south': leaf}, uv={'north': [0, 2, 16, 14], 'south': [0, 2, 16, 14]}, rotation={'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]}),
              element([8, 2, -2], [8, 14, 18], {'east': leaf, 'west': leaf}, uv={'east': [0, 2, 16, 14], 'west': [0, 2, 16, 14]}, rotation={'angle': 45, 'axis': 'y', 'origin': [8, 8, 8]})]
        return model(e, t, 'mesquite_leaves')
    # A low limb out to the north, a little upturned, a few sprays along it.
    return model([element([6.5, 6, 0], [9.5, 9, 10], {'north': 'e', 'south': 'e', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 7, 10]}),
                  element([2, 6, -3], [14, 14, 9], {'north': 'm', 'south': 'm', 'up': 'm', 'east': 'm', 'west': 'm'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 7, 10]})], t, 'mesquite_bark')


def weed_model(stage):
    if stage == 0:
        t = {'w': 'tumbleweed'}
        return model([element([2, 0, 2], [14, 11, 14], {'all': 'w'}, uv={k: [2, 3, 14, 14] for k in ('north', 'south', 'east', 'west')}),
                      element([3, 1, 3], [13, 10, 13], {'all': 'w'})], t, 'tumbleweed')
    if stage == 3:
        return model([element([1, 0.1, 1], [15, 0.1, 15], {'up': 'g', 'down': 'g'})], {'g': 'goathead'}, 'goathead')
    if stage == 4:
        t = {'p': 'pear_pad'}
        e = [element([4, 0, 7.5], [12, 9, 8.5], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p'}, uv={'north': [2, 3, 14, 16], 'south': [2, 3, 14, 16]}),
             element([7, 6, 4], [8, 13, 10], {'east': 'p', 'west': 'p', 'north': 'p', 'south': 'p', 'up': 'p'}, uv={'east': [3, 2, 13, 13], 'west': [3, 2, 13, 13]}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 6, 7]}),
             element([9, 5, 7], [14, 11, 8], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p'}, uv={'north': [3, 3, 13, 14], 'south': [3, 3, 13, 14]}, rotation={'angle': -22.5, 'axis': 'z', 'origin': [10, 5, 8]})]
        return model(e, t, 'pear_pad')
    return model(cross('w', 12 if stage == 2 else 13), {'w': f'weed_{stage}'}, f'weed_{stage}')


# ------------------------------------------------------------------------------------------------ the streets
def pole_wood():
    rnd = random.Random(9230); im = new('#5e4a36', 9230, 5)
    for x in range(16):
        for y in range(16):
            if x % 5 == 2 or rnd.random() < .04:
                darken(im, x, y, -14)
    for x in range(16):
        dot(im, x, 9, '#463626')
    write(im, 5, 3, '87', TALL, '#3a2c1f')  # the year it was set, branded into it
    return im


def crossarm():
    im = new('#6a5440', 9231, 4)
    for x in range(16):
        dot(im, x, 0, '#7c654e'); dot(im, x, 15, '#4a3a2a')
    return im


def insulator():
    im = new('#7a8a7e', 9232, 3)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#5d6c62')
    return im


def wire_tex():
    im = new('#25221f', 9233, 3)
    return im


def galv():
    im = new('#a3a7a8', 9234, 4)
    for x in range(16):
        dot(im, x, 0, '#c2c5c6'); dot(im, x, 15, '#7e8283')
    return im


def lamp_head():
    # The cobra-head street light: a long grey hood, its lens underneath a sodium amber.
    im = new('#8d9092', 9235, 3)
    for y in range(16):
        dot(im, 0, y, '#6f7273'); dot(im, 15, y, '#6f7273')
    return im


def lamp_lens():
    im = new('#f2b24e', 9236, 6)
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 / 50 + (y - 7.5) ** 2 / 64 < 1:
                dot(im, x, y, shade('#ffd27a', random.Random(x * 31 + y).randint(-6, 6)))
    return im


def transformer():
    im = new('#7d8486', 9237, 3)
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#5f6567')
    fill(im, 5, 6, 11, 9, '#c9c4b0')
    return im


def chainlink():
    # Chain-link: galvanized wire woven into diamonds, mostly sky.
    im = new(None)
    for y in range(16):
        for x in range(16):
            if (x + y) % 6 == 0 or (x - y) % 6 == 0:
                dot(im, x, y, '#b4b8b9' if (x + y) % 12 else '#8f9394')
    return im


def gate_frame():
    im = galv()
    return im


def blade(text):
    # A street sign's blade: a white border and white letters on green, a little faded.
    im = new(None, size=(32, 16))
    fill(im, 0, 0, 28, 7, '#2e6a46')
    for x in range(28):
        dot(im, x, 0, '#dfe5e0'); dot(im, x, 6, '#dfe5e0')
    for y in range(7):
        dot(im, 0, y, '#dfe5e0'); dot(im, 27, y, '#dfe5e0')
    centre(im, 1, text, TALL, '#eef1ee', width=28)
    fill(im, 0, 8, 28, 15, '#2a603f')
    return im


def stop_sign():
    im = new(None)
    for x in range(16):
        for y in range(16):
            dx, dy = abs(x - 7.5), abs(y - 7.5)
            if dx + dy < 10.5 and dx < 7.6 and dy < 7.6:
                edge = dx + dy > 9.4 or dx > 6.6 or dy > 6.6
                dot(im, x, y, '#f0ece6' if edge else '#b8302a')
    write(im, 1, 6, 'STOP', TALL, '#f4f0ea')
    return im


def bus_sign():
    im = new(None)
    fill(im, 2, 1, 14, 15, '#e9e4d6'); fill(im, 3, 2, 13, 14, '#2c5a8e')
    centre(im, 3, 'BUS', TALL, '#f2efe6')
    fill(im, 4, 9, 12, 12, '#f2efe6'); dot(im, 5, 12, '#1a1a1a'); dot(im, 10, 12, '#1a1a1a')
    for x in (5, 7, 9):
        dot(im, x, 10, '#2c5a8e')
    return im


def crossbuck():
    im = new(None, size=(32, 16))
    for row, word in ((0, 'RAILROAD'), (8, 'CROSSING')):
        fill(im, 0, row, 32, row + 7, '#ece8de')
        for x in range(32):
            dot(im, x, row, '#26231f'); dot(im, x, row + 6, '#26231f')
        write(im, 1, row + 1, word, TALL, '#1f1c18')
    return im


def tie():
    im = new('#4e3d2e', 9238, 5)
    for x in range(16):
        for y in range(16):
            if y % 4 == 0:
                darken(im, x, y, -10)
    return im


def rail():
    im = new('#6d6862', 9239, 4)
    for x in range(16):
        dot(im, x, 0, '#b7b1a8'); dot(im, x, 1, '#9a948b'); dot(im, x, 15, '#4b3a2a')
    return im


def mailbox():
    im = new('#8a8e90', 9240, 3)
    for x in range(16):
        dot(im, x, 0, '#a9adaf')
    return im


def mailbox_front():
    im = new('#7f8385', 9241, 3)
    fill(im, 6, 6, 10, 8, '#555a5c')
    write(im, 2, 9, '2207', TALL, '#20201e')  # its house number, in stick-on letters
    return im


def flag_red():
    return new('#b4322a', 9242, 4)


def post_wood():
    im = new('#7a6248', 9243, 5)
    for x in (3, 11):
        for y in range(16):
            dot(im, x, y, '#5e4a36')
    return im


def pole_model(stage):
    t = {'p': 'pole_wood', 'a': 'crossarm', 'i': 'insulator', 'w': 'line_wire', 'g': 'galv', 'h': 'lamp_head', 'l': 'lamp_lens', 'x': 'transformer'}
    shaft = element([5.5, 0, 5.5], [10.5, 16, 10.5], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})
    if stage == 0:
        return model([shaft], t, 'pole_wood', render=None)
    if stage == 1:
        e = [element([5.5, 0, 5.5], [10.5, 12, 10.5], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p'}),
             element([-6, 8, 6.5], [22, 11, 9.5], {'all': 'a'}),
             element([-4, 11, 7], [-2, 14, 9], {'all': 'i'}), element([18, 11, 7], [20, 14, 9], {'all': 'i'})]
        return model(e, t, 'pole_wood', render=None)
    if stage in (2, 3, 4):
        # Two wires between the poles, sagging lower toward the middle of the span.
        y = {2: 13, 3: 11, 4: 9}[stage]
        return model([element([0, y, -3], [16, y + 0.5, -2.5], {'all': 'w'}), element([0, y, 18.5], [16, y + 0.5, 19], {'all': 'w'})], t, 'line_wire', render=None)
    if stage == 5:
        # The street light: the pole on through, a pipe arm out over the street and the cobra head on its end, lens down and lit.
        e = [shaft,
             element([7, 11, -6], [9, 12.5, 6], {'all': 'g'}),
             element([7.5, 9, 5], [8.5, 11, 6], {'all': 'g'}),
             element([5.5, 9, -15], [10.5, 12.5, -6], {'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h'})]
        lens = glow([element([6, 8.8, -14], [10, 9, -7], {'down': 'l'})])
        return model(e + lens, t, 'lamp_head')
    # A transformer can bolted to the pole's street side, its leads up to the crossarm.
    e = [shaft, element([5, 3, 0], [11, 12, 5.5], {'north': 'x', 'south': 'x', 'east': 'x', 'west': 'x', 'up': 'x', 'down': 'x'}),
         element([7.5, 12, 2], [8.5, 16, 3], {'all': 'w'})]
    return model(e, t, 'pole_wood', render=None)


def fence_model(stage):
    t = {'m': 'chainlink', 'g': 'galv'}
    if stage == 1:
        return model([element([6.5, 0, 6.5], [9.5, 15, 9.5], {'all': 'g'}), element([6, 15, 6], [10, 16, 10], {'all': 'g'})], t, 'galv', render=None)
    e = [element([0, 0, 8], [16, 14, 8], {'north': 'm', 'south': 'm'}), element([0, 13.5, 7.5], [16, 14.5, 8.5], {'all': 'g'})]
    if stage == 2:
        # The gate: a pipe frame round its own mesh, a latch fork on the east.
        e = [element([0.5, 0.5, 8], [15.5, 13.5, 8], {'north': 'm', 'south': 'm'}),
             element([0, 0, 7.5], [16, 1, 8.5], {'all': 'g'}), element([0, 13, 7.5], [16, 14, 8.5], {'all': 'g'}),
             element([0, 1, 7.5], [1, 13, 8.5], {'all': 'g'}), element([15, 1, 7.5], [16, 13, 8.5], {'all': 'g'}),
             element([14, 7, 6.5], [17, 8, 9.5], {'all': 'g'})]
    if stage == 3:
        # 0.4.701: the gate swung open on its hinge at the west post, out the way it faces: the same frame and mesh, along the cell's west edge
        # and on past it, the latch fork at its far end.
        e = [element([0.5, 0.5, -7.5], [0.5, 13.5, 7.5], {'east': 'm', 'west': 'm'}),
             element([0, 0, -8], [1, 1, 8], {'all': 'g'}), element([0, 13, -8], [1, 14, 8], {'all': 'g'}),
             element([0, 1, 7], [1, 13, 8], {'all': 'g'}), element([0, 1, -8], [1, 13, -7], {'all': 'g'}),
             element([-1, 7, -9], [2, 8, -6], {'all': 'g'})]
    return model(e, t, 'chainlink')


SIGN_NAMES = {0: ('9TH ST', 'AVE K'), 1: ('MAIN ST', 'AVE K'), 2: ('7TH ST', 'AVE K')}


def signpost_model(stage):
    t = {'g': 'galv'}
    post = element([7, 0, 7], [9, 16, 9], {'all': 'g'})
    if stage in SIGN_NAMES:
        t.update({'a': f'blade_{stage}_street', 'b': f'blade_{stage}_avenue'})
        e = [element([7, 0, 7], [9, 22, 9], {'all': 'g'}),
             element([-6, 14, 7.5], [22, 21, 8.5], {'north': 'a', 'south': 'a', 'up': 'a', 'down': 'a', 'east': 'a', 'west': 'a'},
                     uv={'north': [0, 0, 14, 7], 'south': [0, 0, 14, 7], 'up': [0, 8, 14, 9], 'down': [0, 8, 14, 9], 'east': [0, 8, 0.5, 15], 'west': [0, 8, 0.5, 15]}),
             element([7.5, 22, -6], [8.5, 29, 22], {'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b', 'north': 'b', 'south': 'b'},
                     uv={'east': [0, 0, 14, 7], 'west': [0, 0, 14, 7], 'up': [0, 8, 14, 9], 'down': [0, 8, 14, 9], 'north': [0, 8, 0.5, 15], 'south': [0, 8, 0.5, 15]})]
        return model(e, t, 'galv')
    if stage == 3:
        t['s'] = 'stop_sign'
        return model([element([7, 0, 7.5], [9, 16, 9.5], {'all': 'g'}), element([1, 14, 7], [15, 28, 7.5], {'north': 's', 'south': 'g'}, uv={'north': [1, 1, 15, 15]})], t, 'stop_sign')
    if stage == 4:
        t['s'] = 'bus_sign'
        return model([element([7, 0, 7.5], [9, 16, 9.5], {'all': 'g'}), element([2, 16, 7], [14, 30, 7.5], {'north': 's', 'south': 's'}, uv={'north': [2, 1, 14, 15], 'south': [2, 1, 14, 15]})], t, 'bus_sign')
    t['c'] = 'crossbuck'
    e = [element([7, 0, 8], [9, 26, 10], {'all': 'g'})]
    for ang, v in ((45, 0), (-45, 8)):
        e.append(element([-8, 18, 7.5 + v / 16], [24, 25, 8 + v / 16], {'north': 'c', 'south': 'c'}, uv={'north': [0, v, 16, v + 7], 'south': [0, v, 16, v + 7]},
                         rotation={'angle': ang, 'axis': 'z', 'origin': [8, 21.5, 8]}))
    return model(e, t, 'crossbuck')


def track_model():
    t = {'t': 'tie', 'r': 'rail', 'b': 'ballast'}
    e = [element([0, 0, z], [16, 1.5, z + 2.5], {'all': 't'}) for z in (1, 5, 9, 13)]
    e.append(element([4, 1.5, 0], [5.5, 3, 16], {'all': 'r'}))
    return model(e, {**t, 'b': NS + 'block/list_ballast'}, 'rail', render=None)


def mailbox_model():
    t = {'p': 'post_wood', 'm': 'mailbox', 'f': 'mailbox_front', 'r': 'flag_red'}
    return model([element([6.5, 0, 6.5], [9.5, 11, 9.5], {'all': 'p'}),
                  element([5, 11, 3], [11, 15, 13], {'north': 'f', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'}),
                  element([5.5, 15, 3], [10.5, 16, 13], {'north': 'f', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm'}, uv={'north': [4, 4, 12, 6]}),
                  element([11, 12, 9], [11.5, 17, 10], {'all': 'r'})], t, 'mailbox', render=None)


# ------------------------------------------------------------------------------------------------ the reader's house
TRIM = '#e9e4d6'


def window_glass(stage):
    # A sash window in a white aluminum frame with its screen: curtains drawn behind, dark (0), or lit from inside (1); the kitchen's
    # café curtain across the lower half with the light over the sink behind it (2); a screened window on a dark room (3); the
    # picture window's halves, its drapes pulled back and the room lit (4, 5).
    rnd = random.Random(9300 + stage); im = new(None)
    lit = stage in (1, 2, 4, 5)
    back = '#e7c27a' if lit else '#252a33'
    fill(im, 1, 1, 15, 15, back)
    if stage in (0, 1):
        cloth = '#b8a479' if lit else '#4a4438'
        for x in range(2, 14):
            k = (6, 0, -6, 0)[x % 4]
            for y in range(2, 14):
                dot(im, x, y, shade(cloth, k))
        if lit:
            for y in range(2, 14):
                dot(im, 7, y, '#f6dd9e'); dot(im, 8, y, '#f2d690')
    if stage == 2:
        for x in range(2, 14):
            for y in range(8, 14):
                dot(im, x, y, shade('#d9cfb4', (4, 0, -5, 0)[x % 4]))
            dot(im, x, 8, '#b84c3a')
        fill(im, 3, 3, 6, 6, '#f8e9b8')
    if stage == 3:
        for x in range(1, 15):
            for y in range(1, 15):
                if (x + y) % 2 == 0:
                    darken(im, x, y, 10)
    if stage in (4, 5):
        drape = '#8a6a44'
        side = range(2, 5) if stage == 4 else range(11, 14)
        for x in side:
            for y in range(1, 15):
                dot(im, x, y, shade(drape, (5, 0, -6)[x % 3]))
        fill(im, 2, 13, 14, 15, '#c6a466')
    for i in range(16):
        dot(im, i, 0, TRIM); dot(im, i, 15, '#cfc9b8'); dot(im, 0, i, TRIM); dot(im, 15, i, TRIM)
    if stage != 4 and stage != 5:
        for i in range(1, 15):
            dot(im, i, 7, TRIM)
    return im


def window_sill():
    im = new(TRIM, 9310, 2)
    for x in range(16):
        dot(im, x, 15, '#bdb6a4')
    return im


def oak_door():
    # A stained cabinet door, a raised panel in its frame.
    im = new('#7a5232', 9311, 4)
    for x in range(16):
        for y in range(16):
            if (y + x // 3) % 7 == 0:
                darken(im, x, y, -10)
    for i in range(16):
        dot(im, i, 0, '#5e3e24'); dot(im, i, 15, '#5e3e24'); dot(im, 0, i, '#5e3e24'); dot(im, 15, i, '#5e3e24')
    for i in range(3, 13):
        dot(im, i, 3, '#94683f'); dot(im, 3, i, '#94683f'); dot(im, i, 12, '#5a3a22'); dot(im, 12, i, '#5a3a22')
    return im


def cabinet_front():
    im = oak_door()
    for y in range(16):
        dot(im, 7, y, '#4a301c'); dot(im, 8, y, '#4a301c')
    dot(im, 6, 4, '#c8b88a'); dot(im, 9, 4, '#c8b88a'); dot(im, 6, 5, '#a8986a'); dot(im, 9, 5, '#a8986a')
    return im


def counter_top():
    # Speckled harvest-gold laminate.
    rnd = random.Random(9312); im = new('#c9a248', 9312, 4)
    speckle(im, rnd, 30, ['#b18a3a', '#dcb860', '#a37e34'])
    return im


def counter_edge():
    im = new('#b8b2a2', 9313, 2)
    for x in range(16):
        dot(im, x, 0, '#d8d2c2'); dot(im, x, 15, '#8f8a7e')
    return im


def steel():
    rnd = random.Random(9314); im = new('#b9bcbd', 9314, 4)
    for x in range(16):
        for y in range(16):
            if (x * 2 + y) % 9 == 0:
                darken(im, x, y, 12)
    return im


def sink_basin():
    im = steel()
    for x in range(2, 14):
        for y in range(2, 14):
            darken(im, x, y, -24 if 3 < x < 12 and 3 < y < 12 else -10)
    fill(im, 7, 7, 9, 9, '#3a3c3d')
    return im


def range_front():
    # The range's front: enamel in the kitchen's gold, the oven door's dark window, its handle, and the knobs along the top.
    im = new('#c79a3e', 9315, 3)
    fill(im, 2, 6, 14, 14, '#3a3226'); fill(im, 3, 7, 13, 13, '#221d16')
    for x in range(2, 14):
        dot(im, x, 5, '#d8d4c8')
    for x in (2, 5, 10, 13):
        dot(im, x, 2, '#2a2620'); dot(im, x, 1, '#4a463e')
    for i in range(16):
        dot(im, i, 15, '#8a6a2a')
    return im


def range_top():
    im = new('#c79a3e', 9316, 2)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
        for x in range(16):
            for y in range(16):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if d <= 8:
                    dot(im, x, y, '#1f1d1b' if d > 2 else '#3a3632')
    return im


def fridge(upper):
    im = new('#c99c40', 9317 + upper, 2)
    for y in range(16):
        dot(im, 13, y, '#a87e2c'); dot(im, 14, y, '#d8d4c6')
    if upper:
        for x in range(16):
            dot(im, x, 15, '#7a5a1e')
        # A drawing and a list held on with magnets.
        fill(im, 3, 4, 9, 11, '#f2efe6'); line(im, 4, 9, 8, 6, '#3a5ab8'); dot(im, 6, 4, '#c03a2a')
        fill(im, 9, 9, 12, 14, '#efe7c8'); line(im, 10, 10, 11, 10, '#3a3a3a'); line(im, 10, 12, 11, 12, '#3a3a3a')
    return im


def fridge_side():
    return new('#c39638', 9319, 2)


def phone_face():
    # The wall phone: its cradle and handset, the dial's buttons, and the coiled cord hanging down past the frame.
    im = new(None)
    fill(im, 3, 0, 13, 16, '#d9c79a'); fill(im, 4, 1, 12, 15, '#cdb98a')
    fill(im, 4, 1, 12, 4, '#e2d2a8')
    for y in (6, 8, 10, 12):
        for x in (5, 7, 9):
            dot(im, x, y, '#6f6450')
    return im


def phone_cord():
    im = new(None)
    for y in range(16):
        dot(im, 7 + (y % 2), y, '#cdb98a'); dot(im, 8 - (y % 2), y, '#a8956a')
    return im


def canisters():
    im = new(None)
    for i, x0 in enumerate((1, 6, 11)):
        h = (10, 8, 6)[i]
        fill(im, x0, 16 - h, x0 + 4, 16, '#d8cba6')
        fill(im, x0, 16 - h, x0 + 4, 16 - h + 2, '#8a5a32')
        for y in range(16 - h + 3, 16):
            dot(im, x0 + 1, y, '#e6dcbd')
    return im


def toaster():
    im = steel()
    for x in range(3, 13):
        dot(im, x, 2, '#2a2a2a'); dot(im, x, 5, '#2a2a2a')
    return im


def chrome():
    im = new('#c2c6c8', 9320, 5)
    for y in range(16):
        dot(im, 7, y, '#eef0f0'); dot(im, 8, y, '#9ea2a4')
    return im


def vinyl(base, seed):
    rnd = random.Random(seed); im = new(base, seed, 4)
    for x in range(16):
        dot(im, x, 0, shade(base, 14)); dot(im, x, 15, shade(base, -16))
    for _ in range(3):
        x, y = rnd.randint(2, 13), rnd.randint(2, 13)
        dot(im, x, y, shade(base, -10))
    return im


def table_top():
    rnd = random.Random(9321); im = new('#a88a5e', 9321, 3)
    for x in range(16):
        for y in range(16):
            if (y * 3 + x // 4) % 7 == 0:
                darken(im, x, y, -9)
    speckle(im, rnd, 3, ['#8a6e48'])  # a ring from a glass
    return im


def bed_frame():
    im = new('#6e4c2e', 9322, 4)
    for y in range(16):
        dot(im, 0, y, '#4e341e'); dot(im, 15, y, '#4e341e')
    return im


def headboard():
    im = bed_frame()
    for x in range(2, 14, 3):
        for y in range(3, 14):
            dot(im, x, y, '#58391f')
    return im


def comforter():
    # A plaid comforter, a little rumpled.
    rnd = random.Random(9323); im = new('#3e5878', 9323, 3)
    for x in range(16):
        for y in range(16):
            if x % 6 in (0, 1):
                dot(im, x, y, '#6a2e2e' if y % 6 in (0, 1) else '#4e3a52')
            elif y % 6 in (0, 1):
                dot(im, x, y, '#5a4058')
            if (x + y) % 7 == 0:
                darken(im, x, y, -8)
    return im


def sheet():
    im = new('#e3dfd2', 9324, 3)
    for y in range(16):
        if y % 5 == 0:
            for x in range(16):
                darken(im, x, y, -7)
    return im


def pillow():
    im = new('#ece8de', 9325, 2)
    for x in range(16):
        dot(im, x, 0, '#d4cfc2'); dot(im, x, 15, '#d4cfc2')
    for x, y in ((4, 6), (5, 7), (6, 7), (10, 8)):
        dot(im, x, y, '#d8d2c4')
    return im


def drawers():
    im = new('#7a5636', 9326, 3)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, '#4e341e')
    for y in (2, 7, 12):
        dot(im, 7, y, '#c8b88a'); dot(im, 8, y, '#c8b88a')
    return im


def wood_top():
    rnd = random.Random(9327); im = new('#86603c', 9327, 3)
    for x in range(16):
        for y in range(16):
            if (y * 5 + x) % 11 == 0:
                darken(im, x, y, -10)
    return im


def boombox():
    # A tape deck on the desk: a silver box, two speaker grilles and the cassette door between them.
    im = new('#a6a9ab', 9328, 3)
    for cx in (3, 12):
        for x in range(16):
            for y in range(16):
                d = (x - cx) ** 2 + (y - 9) ** 2
                if d <= 8:
                    dot(im, x, y, '#2a2b2c' if (x + y) % 2 else '#3c3d3e')
    fill(im, 6, 6, 10, 12, '#3a3c3e'); fill(im, 7, 8, 9, 10, '#6a5a44')
    for x in range(1, 15):
        dot(im, x, 3, '#585a5c')
    dot(im, 2, 1, '#c8323a')
    return im


def clock_radio():
    im = new('#3a3632', 9329, 2)
    fill(im, 2, 3, 14, 9, '#120c0a')
    write(im, 2, 4, '1', TALL, '#e8302a'); write(im, 5, 4, '47', TALL, '#e8302a'); dot(im, 4, 5, '#e8302a'); dot(im, 4, 7, '#e8302a')
    for x in range(2, 14, 2):
        dot(im, x, 12, '#5a5650')
    return im


def louver():
    im = new('#cfc7b2', 9330, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#a89f88')
    for y in range(16):
        dot(im, 7, y, '#8f8670'); dot(im, 8, y, '#8f8670')
    return im


def books():
    rnd = random.Random(9331); im = new('#5a3e26', 9331, 2)
    x = 1
    while x < 15:
        w = rnd.choice([1, 1, 2]); h = rnd.randint(9, 13); c = rnd.choice(['#7a2a24', '#2a4a6a', '#3e5a2e', '#8a6a2a', '#1e1e24', '#6a3a5a'])
        fill(im, x, 15 - h, min(15, x + w), 15, c)
        dot(im, x, 15 - h + 2, '#d8c88a')
        x += w + (1 if rnd.random() < .2 else 0)
    for i in range(16):
        dot(im, i, 15, '#3e2a18')
    return im


def poster(stage):
    # A band that played the coliseum in Lubbock once, a horror tape's poster, the feed store's calendar for July, a pennant for the
    # high school, a photograph in a frame, a picture of mountains bought at a motel.
    rnd = random.Random(9340 + stage); im = new(None)
    if stage == 0:
        fill(im, 1, 1, 15, 15, '#141014')
        centre(im, 2, 'VEX', TALL, '#d83a30')
        for x, y in ((9, 8), (8, 9), (7, 10), (8, 10), (9, 10), (8, 11), (7, 12), (6, 13)):
            dot(im, x, y, '#e8d040')
        fill(im, 3, 9, 6, 12, '#e6e0d0'); dot(im, 3, 10, '#141014'); dot(im, 5, 10, '#141014'); dot(im, 4, 12, '#e6e0d0')
        fill(im, 10, 9, 13, 12, '#e6e0d0'); dot(im, 10, 10, '#141014'); dot(im, 12, 10, '#141014'); dot(im, 11, 12, '#e6e0d0')
    elif stage == 1:
        fill(im, 1, 1, 15, 15, '#1a1612')
        fill(im, 5, 5, 11, 15, '#0a0806'); fill(im, 6, 3, 10, 7, '#c9b38a')  # a figure in a sack hood, a sickle up
        dot(im, 7, 5, '#0a0806'); dot(im, 9, 5, '#0a0806')
        line(im, 11, 4, 13, 2, '#9aa0a4'); line(im, 13, 2, 14, 4, '#9aa0a4')
        for x in range(1, 15):
            dot(im, x, 1, '#a8231e'); dot(im, x, 2, '#a8231e')
        fill(im, 1, 13, 15, 15, '#a8231e')
    elif stage == 2:
        fill(im, 1, 1, 15, 15, '#efe9d8')
        fill(im, 2, 2, 14, 7, '#6a9ac8'); fill(im, 2, 6, 14, 7, '#8a7a3a'); fill(im, 6, 4, 10, 6, '#3a6a2e'); dot(im, 7, 6, '#1a1a1a'); dot(im, 9, 6, '#1a1a1a')
        for y in (8, 10, 12, 14):
            for x in range(2, 14):
                dot(im, x, y, '#b8b0a0')
        for x in (4, 6, 8, 10, 12):
            for y in range(8, 15):
                dot(im, x, y, '#b8b0a0')
        dot(im, 9, 11, '#c03a2a')
    elif stage == 3:
        for y in range(3, 13):
            for x in range(1, 15):
                if (y - 8) ** 2 * 3 <= (15 - x) * 4 - 4:
                    dot(im, x, y, '#7a1e22')
        write(im, 3, 6, 'LHS', TALL, '#e3c870')
        for y in range(2, 14):
            dot(im, 1, y, '#3a2a1e')
    elif stage == 4:
        fill(im, 1, 1, 15, 15, '#5a3e26'); fill(im, 3, 3, 13, 13, '#c8bfa8'); fill(im, 3, 9, 13, 13, '#9a8a6a')
        for cx in (6, 10):
            fill(im, cx - 1, 6, cx + 1, 9, '#b08a68'); fill(im, cx - 1, 9, cx + 1, 12, '#4a5a7a')
        fill(im, 7, 4, 9, 6, '#7a5a3a')
    else:
        fill(im, 1, 3, 15, 13, '#b8a070'); fill(im, 2, 4, 14, 12, '#8ab0d0')
        for x in range(2, 14):
            h = 4 + abs((x % 7) - 3)
            for y in range(12 - h, 12):
                dot(im, x, y, '#5a6a7a' if y > 12 - h + 1 else '#eef0f2')
        fill(im, 2, 10, 14, 12, '#3a6a4a')
    return im


def corduroy():
    im = new('#7a5a38', 9350, 3)
    for x in range(16):
        if x % 2 == 0:
            for y in range(16):
                darken(im, x, y, -9)
    return im


def tv_cabinet():
    im = new('#5e4024', 9351, 3)
    for x in range(16):
        dot(im, x, 0, '#7a5634'); dot(im, x, 15, '#3e2a16')
    return im


def tv_screen():
    # The console's picture: the late movie, a car's headlights on a dark road.
    im = new('#1c2430', 9352, 3)
    for x in range(1, 15):
        for y in range(1, 15):
            if (x - 7.5) ** 2 / 49 + (y - 7.5) ** 2 / 64 > 1:
                dot(im, x, y, '#0a0c10')
    fill(im, 2, 9, 14, 13, '#2a2a26'); dot(im, 6, 8, '#f2e8b8'); dot(im, 9, 8, '#f2e8b8'); dot(im, 6, 9, '#c8b878'); dot(im, 9, 9, '#c8b878')
    return im


def tv_front():
    im = tv_cabinet()
    fill(im, 1, 1, 12, 12, '#2a2622')
    for y in (3, 6, 9):
        dot(im, 14, y, '#c8b88a')
    for y in range(13, 16):
        for x in range(1, 15, 2):
            dot(im, x, y, '#3a2810')
    return im


def lampshade():
    im = new('#e2d6b4', 9353, 2)
    for x in range(0, 16, 3):
        for y in range(16):
            darken(im, x, y, -8)
    return im


def lamp_base():
    return new('#6a8a7a', 9354, 4)


def enamel():
    im = new('#e6e3dc', 9355, 2)
    for x in range(16):
        dot(im, x, 0, '#f4f2ee')
    return im


def tub_inside():
    im = enamel()
    for x in range(2, 14):
        for y in range(2, 14):
            darken(im, x, y, -10)
    fill(im, 2, 12, 4, 14, '#7a7c7e')
    return im


def mirror():
    im = new('#9ab0b8', 9356, 3)
    for i in range(16):
        dot(im, i, 0, '#d8d4c8'); dot(im, i, 15, '#d8d4c8'); dot(im, 0, i, '#d8d4c8'); dot(im, 15, i, '#d8d4c8')
    for i in range(3, 9):
        dot(im, i, i + 1, '#c8dce2')
    return im


def cooler_side():
    # The swamp cooler's sides: louvers over the wet pads, a rust stain run down from them.
    im = new('#a4a39a', 9357, 3)
    for y in range(1, 15):
        if y % 3 == 0:
            for x in range(1, 15):
                dot(im, x, y, '#6a6a62')
        elif y % 3 == 1:
            for x in range(1, 15):
                dot(im, x, y, '#8a7a52')
    for y in range(8, 16):
        dot(im, 4, y, '#8d5a37')
    return im


def cooler_top():
    im = new('#9c9b92', 9358, 3)
    for i in range(16):
        dot(im, i, 0, '#b4b3aa'); dot(im, 0, i, '#b4b3aa')
    return im


def duct():
    im = galv()
    for y in (4, 12):
        for x in range(16):
            dot(im, x, y, '#7e8283')
    return im


def porch_lamp():
    im = new('#2a2826', 9359, 2)
    fill(im, 4, 4, 12, 13, '#f2d896')
    for y in range(4, 13):
        dot(im, 8, y, '#2a2826')
    return im


def barrel_rust():
    rnd = random.Random(9360); im = new('#7a4a2e', 9360, 6)
    for y in (2, 13):
        for x in range(16):
            dot(im, x, y, '#5a3420')
    for _ in range(6):
        x, y = rnd.randint(1, 14), rnd.randint(3, 12)
        dot(im, x, y, '#1a1210')
    return im


def ash_top():
    rnd = random.Random(9361); im = new('#3a3632', 9361, 6)
    speckle(im, rnd, 20, ['#8a8478', '#5a554e', '#a29a8c'])
    return im


def hose():
    im = new('#3e6a3a', 9362, 3)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#2e4e2a')
    return im


def concrete_step():
    rnd = random.Random(9363); im = new('#b7b2a6', 9363, 4)
    speckle(im, rnd, 10, ['#a19c90', '#c8c3b7'])
    return im


def corrugated_under():
    im = galvanized()
    return im


def rollup():
    # The shop's roll-up door: horizontal slats, galvanized, a handle at the foot.
    im = new('#a9adae', 9364, 3)
    for y in range(16):
        if y % 4 == 0:
            for x in range(16):
                dot(im, x, y, '#7c8081')
        elif y % 4 == 1:
            for x in range(16):
                dot(im, x, y, '#c4c7c8')
    return im


def rollup_low():
    im = rollup()
    fill(im, 6, 12, 10, 14, '#4a4c4d')
    return im


def wire_glass():
    im = new('#3a4048', 9365, 3)
    for x in range(16):
        for y in range(16):
            if x % 4 == 0 or y % 4 == 0:
                dot(im, x, y, '#6a7078')
    for i in range(16):
        dot(im, i, 0, '#8f9394'); dot(im, i, 15, '#8f9394'); dot(im, 0, i, '#8f9394'); dot(im, 15, i, '#8f9394')
    return im


def barn_shade():
    return new('#3e4e3e', 9366, 3)


def bulb():
    im = new('#fff2c8', 9367, 2)
    return im


def dome_glass():
    im = new('#e8e0c8', 9368, 3)
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 20:
                dot(im, x, y, '#fbf2d6')
    return im


def tube_lens():
    im = new('#eef0ea', 9369, 2)
    for y in (4, 11):
        for x in range(16):
            dot(im, x, y, '#ffffff')
    return im


def window_model(stage):
    t = {'g': f'window_{stage}', 's': 'window_sill'}
    # The pane flush with the wall's outer face, so no reveal shows to the street to catch the light of the room behind it.
    e = [element([0, 0, 0], [16, 16, 2], {'north': 'g', 'south': 'g', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}, uv={'east': [0, 0, 2, 16], 'west': [14, 0, 16, 16], 'up': [0, 0, 16, 2], 'down': [0, 14, 16, 16]})]
    if stage in (1, 2, 4, 5):
        glow(e, 10)
    return model(e, t, 'window_sill', render='cutout')


def kitchen_model(stage):
    t = {'c': 'cabinet_front', 'd': 'oak_door', 't': 'counter_top', 'e': 'counter_edge', 's': 'steel', 'b': 'sink_basin'}
    base = [element([0, 0, 1], [16, 14, 16], {'north': 'c', 'east': 'd', 'west': 'd', 'south': 'd', 'down': 'd'}),
            element([0, 0, 0], [16, 1.5, 1], {'north': 'd', 'east': 'd', 'west': 'd', 'up': 'd'}, uv={'north': [0, 14, 16, 15.5]}),
            element([0, 14, 0], [16, 16, 16], {'up': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e', 'down': 't'})]
    if stage == 0:
        return model(base, t, 'counter_top', render=None)
    if stage == 1:
        # The sink: the basin let into the counter, a faucet arching over it from the back.
        e = [element([0, 0, 1], [16, 14, 16], {'north': 'c', 'east': 'd', 'west': 'd', 'south': 'd', 'down': 'd'}),
             element([0, 0, 0], [16, 1.5, 1], {'north': 'd', 'east': 'd', 'west': 'd', 'up': 'd'}, uv={'north': [0, 14, 16, 15.5]}),
             element([0, 14, 0], [16, 16, 2], {'up': 't', 'north': 'e', 'east': 'e', 'west': 'e'}), element([0, 14, 14], [16, 16, 16], {'up': 't', 'south': 'e', 'east': 'e', 'west': 'e'}),
             element([0, 14, 2], [2, 16, 14], {'up': 't', 'east': 'e', 'west': 'e'}), element([14, 14, 2], [16, 16, 14], {'up': 't', 'west': 'e', 'east': 'e'}),
             element([2, 9, 2], [14, 14, 14], {'up': 'b', 'north': 's', 'south': 's', 'east': 's', 'west': 's'}),
             element([7.5, 16, 13], [8.5, 21, 14], {'all': 's'}), element([7.5, 20, 9], [8.5, 21, 13], {'all': 's'}),
             element([6, 16, 13.5], [7, 17.5, 14.5], {'all': 's'}), element([9, 16, 13.5], [10, 17.5, 14.5], {'all': 's'})]
        return model(e, t, 'counter_top', render=None)
    if stage == 2:
        t.update({'r': 'range_front', 'p': 'range_top', 'k': 'cabinet_front'})
        e = [element([0, 0, 1], [16, 15, 16], {'north': 'r', 'east': 'p', 'west': 'p', 'south': 'p', 'up': 'p', 'down': 'p'}, uv={'east': [0, 0, 16, 15], 'west': [0, 0, 16, 15]}),
             element([0, 15, 13], [16, 21, 16], {'north': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'south': 'p'}, uv={'north': [0, 0, 16, 6]}),
             element([2, 9, 0], [14, 10, 1], {'all': 's'})]
        return model(e, t, 'range_front', render=None)
    if stage in (3, 4):
        t.update({'f': f'fridge_{stage - 3}', 'x': 'fridge_side'})
        e = [element([1, 0, 1], [15, 16, 16], {'north': 'f', 'east': 'x', 'west': 'x', 'south': 'x', 'up': 'x', 'down': 'x'})]
        e.append(element([12, 3 if stage == 3 else 1, 0], [13, 13 if stage == 3 else 7, 1], {'all': 's'}))
        return model(e, t, 'fridge_side', render=None)
    if stage == 5:
        return model([element([0, 0, 9], [16, 16, 16], {'north': 'c', 'east': 'd', 'west': 'd', 'up': 'd', 'down': 'd', 'south': 'd'})], t, 'cabinet_front', render=None)
    if stage == 6:
        t.update({'p': 'phone_face', 'o': 'phone_cord'})
        return model([element([4, 3, 13], [12, 15, 16], {'north': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'}, uv={'north': [3, 0, 13, 16], 'east': [3, 0, 6, 12], 'west': [3, 0, 6, 12], 'up': [3, 0, 13, 3], 'down': [3, 0, 13, 3]}),
                      element([5, 11, 11.5], [11, 14.5, 13], {'all': 'p'}, uv={'north': [4, 1, 12, 4], 'south': [4, 1, 12, 4]}),
                      element([5, -8, 12], [7, 4, 12], {'north': 'o', 'south': 'o'}, uv={'north': [7, 0, 9, 12], 'south': [7, 0, 9, 12]})], t, 'phone_face')
    t.update({'n': 'canisters', 'o': 'toaster'})
    return model(base + [element([1, 16, 10], [12, 26, 15], {'north': 'n', 'south': 'n'}, uv={'north': [1, 6, 15, 16], 'south': [1, 6, 15, 16]}),
                         element([12.5, 16, 4], [15.5, 20, 10], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'o'})], t, 'counter_top', render=None)


def table_model(stage):
    t = {'m': 'chrome', 't': 'table_top', 'v': 'chair_vinyl'}
    if stage == 0:
        e = [element([0, 12, 0], [16, 13.5, 16], {'all': 't'}),
             element([2, 0, 2], [3, 12, 3], {'all': 'm'}), element([13, 0, 2], [14, 12, 3], {'all': 'm'}),
             element([2, 0, 13], [3, 12, 14], {'all': 'm'}), element([13, 0, 13], [14, 12, 14], {'all': 'm'})]
        return model(e, t, 'table_top', render=None)
    # The chair: a chrome frame, a padded seat and back in vinyl; its back to the south.
    e = [element([3.5, 0, 3.5], [4.5, 8, 4.5], {'all': 'm'}), element([11.5, 0, 3.5], [12.5, 8, 4.5], {'all': 'm'}),
         element([3.5, 0, 11.5], [4.5, 16, 12.5], {'all': 'm'}), element([11.5, 0, 11.5], [12.5, 16, 12.5], {'all': 'm'}),
         element([3, 8, 3], [13, 10, 13], {'all': 'v'}), element([4, 11, 12], [12, 16, 13.5], {'all': 'v'})]
    return model(e, t, 'chair_vinyl', render=None)


def bed_model(stage):
    t = {'f': 'bed_frame', 'h': 'headboard', 'c': 'comforter', 's': 'sheet', 'p': 'pillow'}
    if stage == 0:
        # The head: the headboard to the south, the pillow, the sheet turned down over the comforter.
        e = [element([0, 0, 0], [16, 3, 16], {'all': 'f'}), element([0, 0, 14], [16, 16, 16], {'north': 'h', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f'}),
             element([0.5, 3, 0], [15.5, 7, 14], {'up': 's', 'east': 's', 'west': 's', 'north': 's'}),
             element([0.5, 7, 0], [15.5, 8, 6], {'up': 'c', 'north': 'c', 'east': 'c', 'west': 'c'}),
             element([2, 7, 8], [14, 10, 13.5], {'all': 'p'})]
        return model(e, t, 'comforter', render=None)
    e = [element([0, 0, 0], [16, 3, 16], {'all': 'f'}), element([0, 3, 0], [16, 8, 16], {'up': 'c', 'north': 'c', 'east': 'c', 'west': 'c', 'south': 'c'}),
         element([0.5, 8, 4], [15.5, 9, 11], {'up': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}),
         element([0, 0, 0], [16, 10, 1.5], {'north': 'f', 'south': 'f', 'up': 'f', 'east': 'f', 'west': 'f'})]
    return model(e, t, 'comforter', render=None)


def room_model(stage):
    t = {'d': 'drawers', 'w': 'wood_top', 'b': 'boombox', 'r': 'clock_radio', 'l': 'louver', 'k': 'books', 'm': 'mirror'}
    if stage == 0:
        return model([element([0, 0, 4], [16, 14, 16], {'north': 'd', 'up': 'w', 'east': 'w', 'west': 'w', 'south': 'w', 'down': 'w'}),
                      element([2, 14, 13], [14, 28, 14.5], {'north': 'm', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w'})], t, 'drawers', render=None)
    if stage == 1:
        e = [element([0, 13, 3], [16, 15, 16], {'all': 'w'}), element([0, 0, 3], [2, 13, 16], {'all': 'd'}), element([14, 0, 3], [16, 13, 16], {'all': 'd'}),
             element([2, 15, 7], [14, 22, 12], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'b'}, uv={'east': [0, 0, 5, 7], 'west': [0, 0, 5, 7], 'up': [0, 0, 16, 5]}),
             element([7.5, 22, 9], [8.5, 23, 10], {'all': 'b'}), element([3, 22, 9], [13, 23, 10], {'all': 'b'})]
        return model(e, t, 'boombox', render=None)
    if stage == 2:
        e = [element([2, 0, 2], [14, 10, 14], {'north': 'd', 'up': 'w', 'east': 'w', 'west': 'w', 'south': 'w'}, uv={'north': [0, 5, 16, 15]}),
             element([4, 10, 6], [12, 13, 11], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r'}, uv={'north': [1, 2, 15, 10]})]
        glow(e[1:], 4)
        return model(e, t, 'drawers', render=None)
    if stage == 3:
        return model([element([0, 0, 13], [16, 16, 16], {'north': 'l', 'east': 'l', 'west': 'l', 'up': 'l', 'down': 'l'})], t, 'louver', render=None)
    return model([element([0, 0, 7], [16, 11, 16], {'north': 'k', 'up': 'w', 'east': 'w', 'west': 'w', 'south': 'w'}, uv={'north': [0, 4, 16, 15]})], t, 'books', render=None)


def poster_model(stage):
    return model([element([1, 1, 15.5], [15, 15, 15.5], {'north': 'p'}, uv={'north': [1, 1, 15, 15]})], {'p': f'poster_{stage}'}, f'poster_{stage}')


def den_model(stage):
    t = {'c': 'corduroy', 'w': 'wood_top', 'f': 'tv_front', 's': 'tv_screen', 'k': 'tv_cabinet', 'l': 'lampshade', 'b': 'lamp_base'}
    if stage in (0, 1):
        # A half of the couch: seat, back to the south, and the arm on its outer end.
        arm = [0, 3] if stage == 0 else [13, 16]
        e = [element([0, 0, 2], [16, 6, 15], {'all': 'c'}), element([0, 6, 3], [16, 8, 12], {'all': 'c'}),
             element([0, 6, 12], [16, 15, 15], {'all': 'c'}), element([arm[0], 6, 2], [arm[1], 11, 15], {'all': 'c'})]
        return model(e, t, 'corduroy', render=None)
    if stage == 2:
        e = [element([1, 0, 1], [15, 7, 14], {'all': 'c'}), element([2, 7, 3], [14, 9, 12], {'all': 'c'}),
             element([1, 7, 12], [15, 16, 15], {'all': 'c'}), element([1, 7, 1], [3.5, 12, 14], {'all': 'c'}), element([12.5, 7, 1], [15, 12, 14], {'all': 'c'})]
        return model(e, t, 'corduroy', render=None)
    if stage == 3:
        e = [element([1, 0, 3], [15, 14, 16], {'north': 'f', 'east': 'k', 'west': 'k', 'up': 'k', 'south': 'k', 'down': 'k'})]
        e += glow([element([2, 3, 2.9], [12, 13, 2.9], {'north': 's'}, uv={'north': [2, 2, 14, 14]})], 8)
        return model(e, t, 'tv_front', render=None)
    if stage == 4:
        e = [element([2, 0, 2], [14, 9, 14], {'all': 'w'}), element([6.5, 9, 6.5], [9.5, 15, 9.5], {'all': 'b'})]
        e += glow([element([4, 15, 4], [12, 22, 12], {'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l'})], 12)
        return model(e, t, 'lampshade')
    e = [element([1, 6, 3], [15, 7, 13], {'all': 'w'}), element([2, 0, 4], [3, 6, 5], {'all': 'w'}), element([13, 0, 4], [14, 6, 5], {'all': 'w'}),
         element([2, 0, 11], [3, 6, 12], {'all': 'w'}), element([13, 0, 11], [14, 6, 12], {'all': 'w'})]
    return model(e, t, 'wood_top', render=None)


def bath_model(stage):
    t = {'e': 'enamel', 'i': 'tub_inside', 's': 'steel', 'd': 'drawers', 'w': 'counter_top', 'b': 'sink_basin', 'm': 'mirror'}
    if stage in (0, 1):
        e = [element([0, 0, 0], [16, 9, 16], {'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e', 'down': 'e', 'up': 'i'})]
        if stage == 0:
            e.append(element([7, 9, 14], [9, 12, 15], {'all': 's'}))
        return model(e, t, 'enamel', render=None)
    if stage == 2:
        e = [element([5, 0, 5], [11, 6, 12], {'all': 'e'}), element([4, 6, 3], [12, 8, 13], {'all': 'e'}), element([3, 8, 12], [13, 15, 16], {'all': 'e'})]
        return model(e, t, 'enamel', render=None)
    if stage == 3:
        e = [element([0, 0, 4], [16, 13, 16], {'north': 'd', 'east': 'd', 'west': 'd', 'south': 'd'}, uv={'north': [0, 3, 16, 16]}),
             element([0, 13, 4], [16, 15, 16], {'up': 'b', 'north': 'w', 'east': 'w', 'west': 'w'})]
        return model(e, t, 'drawers', render=None)
    return model([element([3, 2, 14.5], [13, 14, 16], {'north': 'm', 'east': 'e', 'west': 'e', 'up': 'e', 'down': 'e'})], t, 'mirror', render=None)


def rooftop_model(stage):
    t = {'s': 'cooler_side', 'o': 'cooler_top', 'g': 'galv', 'd': 'duct'}
    if stage == 0:
        return model([element([1, 2, 1], [15, 15, 15], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 'o', 'down': 'o'}),
                      element([0, 0, 0], [16, 2, 16], {'all': 'g'})], t, 'cooler_side', render=None)
    if stage == 1:
        return model([element([4, 0, 4], [12, 16, 12], {'all': 'd'})], t, 'duct', render=None)
    if stage == 2:
        e = [element([7.5, 0, 7.5], [8.5, 16, 8.5], {'all': 'g'})]
        for y, w in ((8, 7), (11, 6), (14, 5)):
            e.append(element([8 - w, y, 7.7], [8 + w, y + 0.5, 8.3], {'all': 'g'}))
        e.append(element([7.7, 12, 2], [8.3, 12.5, 14], {'all': 'g'}))
        return model(e, t, 'galv', render=None)
    return model([element([6, 0, 6], [10, 9, 10], {'all': 'g'}), element([5.5, 9, 5.5], [10.5, 10, 10.5], {'all': 'g'})], t, 'galv', render=None)


def yard_model(stage):
    t = {'l': 'porch_lamp', 'g': 'galv', 'r': 'barrel_rust', 'a': 'ash_top', 'h': 'hose', 'c': 'concrete_step', 'u': 'corrugated_under', 'w': 'line_wire'}
    if stage == 0:
        e = [element([6, 6, 14], [10, 12, 16], {'all': 'g'}), element([5.5, 5, 12], [10.5, 6, 15], {'all': 'g'})]
        e += glow([element([6, 6.5, 12.5], [10, 12.5, 14], {'north': 'l', 'east': 'l', 'west': 'l'})], 14)
        return model(e, t, 'porch_lamp')
    if stage == 1:
        return model([element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'g'}), element([6.5, 13, -4], [9.5, 14.5, 20], {'all': 'g'})], t, 'galv', render=None)
    if stage == 2:
        return model([element([0, 13.5, z], [16, 14, z + 0.5], {'all': 'w'}) for z in (-2, 7.75, 17.5)], t, 'line_wire', render=None)
    if stage == 3:
        return model([element([3, 0, 3], [13, 14, 13], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'down': 'r'}),
                      element([3.5, 11, 3.5], [12.5, 11, 12.5], {'up': 'a'})], t, 'barrel_rust')
    if stage == 4:
        return model([element([7, 6, 14], [9, 9, 16], {'all': 'g'}), element([3, 0, 11], [13, 8, 14], {'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h'})], t, 'hose', render=None)
    if stage == 5:
        return model([element([0, 0, 0], [16, 4, 16], {'all': 'c'})], t, 'concrete_step', render=None)
    if stage == 6:
        return model([element([6, 0, 6], [10, 16, 10], {'all': 'g'})], t, 'galv', render=None)
    return model([element([0, 13, 0], [16, 16, 16], {'up': 'u', 'down': 'u', 'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'})], t, 'corrugated_under', render=None)


def shop_model(stage):
    t = {'r': 'rollup', 'q': 'rollup_low', 'w': 'wire_glass', 'm': NS + 'block/list_shop_metal', 's': 'barn_shade', 'b': 'bulb', 'g': 'galv'}
    if stage in (0, 1):
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'q' if stage == 0 else 'r', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'rollup', render=None)
    if stage == 2:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'w', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'wire_glass', render=None)
    e = [element([7.5, 12, 11], [8.5, 13, 16], {'all': 'g'}), element([4, 9, 8], [12, 12, 14], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 's'})]
    e += glow([element([5.5, 8.5, 9.5], [10.5, 9, 12.5], {'down': 'b'})], 15)
    return model(e, t, 'barn_shade')


def fixture_model(stage):
    t = {'d': 'dome_glass', 'g': 'galv', 'b': 'bulb', 'o': 'tube_lens', 'm': NS + 'block/list_ceiling'}
    if stage in (0, 1):
        w = 4 if stage == 0 else 3
        e = [element([8 - w, 15, 8 - w], [8 + w, 16, 8 + w], {'all': 'g'})]
        e += glow([element([8 - w + 0.5, 12, 8 - w + 0.5], [8 + w - 0.5, 15, 8 + w - 0.5], {'all': 'd'})], 15)
        return model(e, t, 'dome_glass')
    if stage == 2:
        e = [element([1, 15, 5], [15, 16, 11], {'all': 'g'})]
        e += glow([element([1.5, 13.5, 5.5], [14.5, 15, 10.5], {'down': 'o', 'north': 'o', 'south': 'o'})], 15)
        return model(e, t, 'tube_lens')
    if stage == 3:
        e = [element([7, 14, 7], [9, 16, 9], {'all': 'g'})]
        e += glow([element([6.5, 11, 6.5], [9.5, 14, 9.5], {'all': 'b'})], 15)
        return model(e, t, 'bulb')
    e = [element([7, 10, 12], [9, 11, 16], {'all': 'g'}), element([3, 4, 8], [13, 12, 12], {'all': 'g'})]
    e += glow([element([3.5, 4.5, 7.9], [12.5, 11.5, 7.9], {'north': 'b'})], 15)
    return model(e, t, 'galv')


# ------------------------------------------------------------------------------------------------ Main Street
ALUM = '#9ea3a6'


def display(lit):
    # A display window in its aluminum frame: lit, the shelves inside and their goods blurred behind the glass, the hours stenciled at
    # the foot; dark, the glass giving back the street.
    rnd = random.Random(9400 + lit); im = new('#e8d7a8' if lit else '#1e232a', 9400 + lit, 3)
    if lit:
        for y in (5, 10):
            for x in range(1, 15):
                dot(im, x, y, '#a89060')
                if rnd.random() < .7:
                    dot(im, x, y - 1, rnd.choice(['#c84a3a', '#3a6aa8', '#d8b848', '#6a8a4a', '#f2efe6']))
                    if rnd.random() < .5:
                        dot(im, x, y - 2, rnd.choice(['#c84a3a', '#3a6aa8', '#d8b848']))
    else:
        for i in range(5):
            dot(im, 3 + i, 9 - i, '#3a424c'); dot(im, 4 + i, 9 - i, '#2e353e')
    for i in range(16):
        dot(im, i, 0, ALUM); dot(im, i, 15, '#7a7f82'); dot(im, 0, i, ALUM); dot(im, 15, i, ALUM)
    return im


def transom():
    im = new('#2a3038', 9402, 3)
    for i in range(16):
        dot(im, i, 0, ALUM); dot(im, i, 15, ALUM); dot(im, 0, i, ALUM); dot(im, 15, i, ALUM); dot(im, 8, i, ALUM)
    return im


def awning(colour):
    im = new(None)
    for x in range(16):
        c = colour if (x // 2) % 2 == 0 else '#e9e4d6'
        for y in range(16):
            dot(im, x, y, shade(c, random.Random(x * 17 + y).randint(-5, 5)))
    return im


def valance(colour):
    im = awning(colour)
    for x in range(16):
        h = 11 + (2 if x % 4 in (1, 2) else 0)
        for y in range(h, 16):
            im.putpixel((x, y), (0, 0, 0, 0))
    return im


def pilaster_cap():
    im = new('#ddd1b8', 9403, 2)
    for x in range(16):
        dot(im, x, 15, '#b8ab90')
    return im


def door_glass(upper):
    # The stores' doors: an aluminum frame round a pane of glass, a push bar across, the hours on a card.
    im = new(None)
    fill(im, 0, 0, 16, 16, ALUM)
    fill(im, 2, 0 if not upper else 2, 14, 16 if upper else 14, '#2a313a')
    for i in range(3):
        dot(im, 4 + i, (12 if upper else 10) - i, '#4a5560')
    if not upper:
        fill(im, 2, 1, 14, 2, '#c8cccf')
    else:
        fill(im, 5, 6, 11, 11, '#efe9d8')
        for y in (7, 9):
            for x in range(6, 10):
                dot(im, x, y, '#5a5650')
    return im


def house_door(upper):
    # The house's doors: a six-panel door painted the trim's colour, a brass knob.
    im = new('#d7cfba', 9404 + upper, 2)
    panels = [(2, 2, 7, 7), (9, 2, 14, 7), (2, 9, 7, 15), (9, 9, 14, 15)] if upper else [(2, 1, 7, 7), (9, 1, 14, 7), (2, 9, 7, 14), (9, 9, 14, 14)]
    for x0, y0, x1, y1 in panels:
        for x in range(x0, x1):
            dot(im, x, y0, '#b9b19b'); dot(im, x, y1 - 1, '#efe9da')
        for y in range(y0, y1):
            dot(im, x0, y, '#b9b19b'); dot(im, x1 - 1, y, '#efe9da')
    if not upper:
        dot(im, 13, 0, '#c8a24a'); dot(im, 13, 1, '#a8822a')
    return im


def sign_board(text, n, ground, letter, edge, seed, shadow=None):
    im = new(ground, seed, 3, size=(16 * n, 16))
    for x in range(16 * n):
        dot(im, x, 0, edge); dot(im, x, 1, shade(edge, -10)); dot(im, x, 14, shade(edge, -10)); dot(im, x, 15, edge)
    centre(im, 4, text, BROAD, letter, shadow)
    return im


def drug_sign():
    return sign_board('DAWSON DRUG', 4, '#ece5d0', '#8a2a24', '#5a4a3a', 9410, '#c9bfa8')


def feed_sign():
    im = sign_board('FEED & SEED', 4, '#d9d2bf', '#2e4e2e', '#7a6a52', 9411)
    rnd = random.Random(9411)
    for _ in range(40):
        darken(im, rnd.randrange(64), rnd.randrange(2, 14), -8)
    return im


def video_sign():
    # Backlit plastic: the letters lit, the board dark round them.
    im = new('#1a1a24', 9412, 2, size=(80, 16))
    for x in range(80):
        dot(im, x, 0, '#5a5a6a'); dot(im, x, 15, '#5a5a6a')
    centre(im, 4, 'REEL TIME VIDEO', BROAD, '#f2e6a0', '#c8402a')
    return im


def law_sign():
    im = new('#5a4028', 9413, 3, size=(64, 16))
    for x in range(64):
        dot(im, x, 0, '#3e2a18'); dot(im, x, 15, '#3e2a18')
    # A six-pointed star in a ring, gold, and the word.
    cx, cy = 7.5, 7.5
    for x in range(16):
        for y in range(16):
            d = math.hypot(x - cx, y - cy)
            if 5.6 < d < 6.6:
                dot(im, x, y, '#d8b048')
    for k in range(6):
        a = k * math.tau / 6 - math.pi / 2
        for r in range(0, 5):
            dot(im, int(round(cx + math.cos(a) * r)), int(round(cy + math.sin(a) * r)), '#e8c860')
    for x in range(5, 11):
        for y in range(5, 11):
            if math.hypot(x - cx, y - cy) < 2.5:
                dot(im, x, y, '#e8c860')
    write(im, 19, 4, 'SHERIFF', BROAD, '#e8c860', '#2e1e10')
    return im


def drivein_board():
    # The drive-in's sign on its pole, three blocks by two: its name in two lines of lit letters, a band of bulbs round the edge.
    im = new('#2e3a5e', 9414, 3, size=(48, 32))
    for x in range(48):
        for y in (0, 31):
            dot(im, x, y, '#f2e6a0' if x % 3 == 0 else '#8a6a2a')
    for y in range(32):
        for x in (0, 47):
            dot(im, x, y, '#f2e6a0' if y % 3 == 0 else '#8a6a2a')
    centre(im, 4, 'CAPROCK', BROAD, '#f4ead0', '#c8402a')
    fill(im, 3, 14, 45, 16, '#c8402a')
    centre(im, 19, 'DRIVE-IN', BROAD, '#f2d070', '#1a2240')
    return im


def drivein_arrow():
    im = new(None)
    fill(im, 0, 5, 11, 11, '#f2d070')
    for i in range(6):
        for y in range(2 + i, 14 - i):
            dot(im, 10 + i, y, '#f2d070')
    for x in range(0, 11, 2):
        dot(im, x, 8, '#fff6d0')
    return im


def goods(seed):
    # A shelf's goods face on: boxes and bottles in rows, the shelves' edges between.
    rnd = random.Random(seed); im = new('#d8d2c2', seed, 2)
    for row in range(2):
        y1 = 7 + row * 8
        for x in range(16):
            dot(im, x, y1, '#8a8478')
        x = 1
        while x < 15:
            w = rnd.choice([2, 2, 3]); h = rnd.randint(3, 6); c = rnd.choice(['#c84a3a', '#3a6aa8', '#d8b848', '#6a8a4a', '#f2efe6', '#8a4a8a', '#2e2e3a'])
            fill(im, x, y1 - h, min(15, x + w), y1, c)
            dot(im, x, y1 - h + 1, shade(c, 25))
            x += w + 1
    return im


def bottles():
    rnd = random.Random(9420); im = new('#cfc8b6', 9420, 2)
    for row in range(3):
        y1 = 5 + row * 5
        for x in range(16):
            dot(im, x, y1, '#8a8478')
        for x in range(1, 15, 2):
            if rnd.random() < .85:
                fill(im, x, y1 - 3, x + 1, y1, rnd.choice(['#8a4a1e', '#a85a24', '#efe9d8']))
                dot(im, x, y1 - 3, '#f2efe6')
    return im


def counter_front_w():
    im = new('#8a6a46', 9421, 3)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#6a4e32')
    return im


def register():
    im = new('#3a3c3e', 9422, 3)
    for y in (6, 8, 10):
        for x in range(3, 13, 2):
            dot(im, x, y, '#c8c4b8')
    fill(im, 3, 1, 13, 4, '#1a2a1e')
    fill(im, 5, 2, 11, 3, '#5ac88a')
    return im


def stool_top():
    im = new('#a8322a', 9423, 4)
    for x in range(16):
        for y in range(16):
            if abs(math.hypot(x - 7.5, y - 7.5) - 5) < .7:
                darken(im, x, y, -14)
    return im


def magazines():
    rnd = random.Random(9424); im = new('#8a8e90', 9424, 2)
    for row in range(3):
        for col in range(3):
            x0, y0 = 1 + col * 5, 1 + row * 5
            c = rnd.choice(['#c84a3a', '#e8d870', '#3a6aa8', '#f2efe6', '#5a8a4a'])
            fill(im, x0, y0, x0 + 4, y0 + 4, c); fill(im, x0, y0, x0 + 4, y0 + 1, shade(c, -30))
    return im


def cards():
    rnd = random.Random(9425); im = new('#7a7e80', 9425, 2)
    for row in range(4):
        for col in range(4):
            x0, y0 = col * 4, row * 4
            fill(im, x0 + 1, y0 + 1, x0 + 4, y0 + 4, rnd.choice(['#f2c8d0', '#c8e0f2', '#f2efe6', '#e8d890', '#c8f2c8']))
    return im


def tapes(kind):
    # The video store's boxes face out: dark covers with a title's bar on the horror wall, bright ones on the new releases and the
    # double shelves, each a made-up film; a board along the top names the section.
    rnd = random.Random(9430 + kind); im = new('#3a2e22', 9430 + kind, 2)
    header = {0: 'FEAR', 1: 'NEW', 2: 'HITS'}[kind]
    fill(im, 0, 0, 16, 6, '#1a1612' if kind == 0 else '#e8e2d0')
    centre(im, 0, header, TALL, '#d83a30' if kind == 0 else '#2a4a8a')
    for row in range(2):
        y0 = 7 + row * 5
        for col in range(5):
            x0 = col * 3 + 1
            c = rnd.choice(['#0e0c0a', '#1e1414', '#2a0e0e', '#14141e']) if kind == 0 else rnd.choice(['#c84a3a', '#3a6aa8', '#e8c848', '#4a8a5a', '#a84a8a', '#f2efe6'])
            fill(im, x0, y0, x0 + 2, y0 + 4, c)
            dot(im, x0, y0 + 1, '#c8302a' if kind == 0 else '#f2efe6'); dot(im, x0 + 1, y0 + 1, '#c8302a' if kind == 0 else '#1a1a1a')
            if kind == 0 and rnd.random() < .5:
                dot(im, x0 + 1, y0 + 3, '#d8d0b8')  # a pale face on the cover
        for x in range(16):
            dot(im, x, y0 + 4, '#5a4632')
    return im


def tape_shelf_side():
    return new('#4a3a2a', 9433, 3)


def rewinder():
    im = new('#2a2a2e', 9434, 2)
    fill(im, 3, 3, 13, 9, '#3a3a40'); fill(im, 5, 5, 11, 7, '#c8302a')
    return im


def return_slot():
    im = new('#7a7e80', 9435, 3)
    fill(im, 2, 6, 14, 9, '#1a1a1a')
    centre(im, 1, 'DROP', TALL, '#efe9d8')
    return im


def standee():
    # A cardboard figure for a film in the store this week: the man in the sack mask from the posters, a sickle up, in a cornfield.
    im = new(None)
    fill(im, 3, 3, 13, 16, '#0e0c0a'); fill(im, 5, 0, 11, 5, '#c9b38a'); dot(im, 6, 2, '#0e0c0a'); dot(im, 9, 2, '#0e0c0a')
    line(im, 12, 4, 15, 1, '#a8acb0'); line(im, 15, 1, 15, 4, '#a8acb0')
    for x in range(0, 16):
        if x < 3 or x > 12:
            for y in range(10, 16):
                if (x + y) % 2:
                    dot(im, x, y, '#c8a848')
    return im


def tv_case():
    return new('#2e2c2a', 9436, 2)


def tape_screen():
    # The store's television playing a tape: a face lit from below, the tracking lines through it.
    im = new('#12141c', 9437, 3)
    fill(im, 5, 4, 11, 12, '#6a5a4a'); fill(im, 6, 5, 10, 11, '#b8a080'); dot(im, 7, 7, '#1a1410'); dot(im, 9, 7, '#1a1410')
    for y in (3, 11):
        for x in range(16):
            dot(im, x, y, '#5a6a7a')
    return im


def gov_metal():
    im = new('#7f8a7c', 9440, 3)
    for x in range(16):
        dot(im, x, 0, '#9aa596'); dot(im, x, 15, '#5e685c')
    return im


def gov_drawers():
    im = gov_metal()
    for y in (4, 8, 12):
        for x in range(16):
            dot(im, x, y, '#4e584c')
        fill(im, 6, y - 2, 10, y - 1, '#b8bcb0')
    return im


def radio_set():
    im = new('#2a2a26', 9441, 2)
    fill(im, 2, 3, 9, 8, '#3a3e30'); fill(im, 3, 4, 8, 7, '#d8a83a')
    for x in (11, 13):
        dot(im, x, 5, '#c8c4b8')
    for x in range(2, 14):
        dot(im, x, 11, '#5a5a52')
    return im


def bench_wood():
    im = new('#8a6a46', 9442, 3)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#5e4630')
    return im


def flag_tex():
    # The state flag over the substation's door: a blue band at the hoist with its white star, white over red.
    im = new(None)
    fill(im, 0, 0, 6, 16, '#22386e'); fill(im, 6, 0, 16, 8, '#efece4'); fill(im, 6, 8, 16, 16, '#b4322a')
    for x, y in ((3, 5), (2, 7), (4, 7), (3, 8), (2, 10), (4, 10), (3, 6), (1, 7), (5, 7)):
        dot(im, x, y, '#efece4')
    return im


def corkboard():
    rnd = random.Random(9443); im = new('#a8784a', 9443, 6)
    for i in range(16):
        dot(im, i, 0, '#6a4a2a'); dot(im, i, 15, '#6a4a2a'); dot(im, 0, i, '#6a4a2a'); dot(im, 15, i, '#6a4a2a')
    for x0, y0 in ((2, 2), (8, 3), (3, 9), (9, 9)):
        fill(im, x0, y0, x0 + 5, y0 + 5, '#efe9d8')
        for y in range(y0 + 1, y0 + 5, 2):
            for x in range(x0 + 1, x0 + 4):
                dot(im, x, y, '#6a6458')
        dot(im, x0 + 2, y0, '#c8302a')
    return im


def water_jug():
    im = new('#9ab8d0', 9444, 3)
    for x in range(16):
        for y in range(16):
            if y < 7:
                darken(im, x, y, 12)
    return im


def cooler_body():
    return new('#d8d4c8', 9445, 2)


def feed_sack():
    # A feed sack, paper, stitched across its top, printed with a bale and a brand.
    rnd = random.Random(9450); im = new('#cdbd96', 9450, 4)
    for x in range(16):
        dot(im, x, 1, '#8a7a5a') if x % 2 else dot(im, x, 1, '#a8987a')
    fill(im, 4, 5, 12, 10, '#2e5a32'); fill(im, 5, 6, 11, 9, '#e9e4d6'); line(im, 6, 7, 10, 7, '#2e5a32')
    for x in range(3, 13):
        dot(im, x, 12, '#7a2a24')
    for x in range(16):
        dot(im, x, 15, '#9a8a66')
    return im


def sack_top():
    im = new('#c4b48e', 9451, 4)
    for x in range(0, 16, 8):
        for y in range(16):
            dot(im, x, y, '#9a8a66')
    for y in range(0, 16, 8):
        for x in range(16):
            dot(im, x, y, '#9a8a66')
    return im


def mineral():
    im = new('#b8463a', 9452, 6)
    for x in range(8, 16):
        for y in range(16):
            dot(im, x, y, shade('#e8e2d6', random.Random(x * 7 + y).randint(-6, 6)))
    return im


def seed_bin_top():
    rnd = random.Random(9453); im = new('#d8d2c4', 9453, 5)
    speckle(im, rnd, 30, ['#bab2a2', '#eae4d8', '#9a9282'])
    for i in range(16):
        dot(im, i, 0, '#6a4e32'); dot(im, i, 15, '#6a4e32'); dot(im, 0, i, '#6a4e32'); dot(im, 15, i, '#6a4e32')
    return im


def bench_ad():
    # The bus bench's back: an advertisement for the drive-in.
    im = new('#efe9d8', 9454, 2, size=(32, 16))
    fill(im, 0, 0, 32, 16, '#2e3a5e'); fill(im, 1, 1, 31, 15, '#efe9d8')
    centre(im, 2, 'CAPROCK', TALL, '#2e3a5e'); centre(im, 9, 'DRIVE-IN', TALL, '#c8402a')
    return im


def rubber():
    return new('#2a2826', 9455, 3)


def canopy_under():
    # The stalls' canopy from below: white enamel panels in their seams.
    im = new('#dcd8cc', 9460, 2)
    for x in range(0, 16, 8):
        for y in range(16):
            dot(im, x, y, '#a8a498')
    return im


def canopy_edge():
    im = new('#c8402a', 9461, 3)
    for x in range(16):
        dot(im, x, 2, '#efe9d8'); dot(im, x, 13, '#efe9d8')
    return im


def canopy_light():
    im = new('#fff6d8', 9462, 2)
    for i in range(16):
        dot(im, i, 0, '#c8c4b8'); dot(im, i, 15, '#c8c4b8'); dot(im, 0, i, '#c8c4b8'); dot(im, 15, i, '#c8c4b8')
    return im


def menu_board():
    # The stall's lit menu: burgers, fries, shakes and their prices in rows, the button and the grille of the speaker below.
    im = new('#f2ead0', 9463, 2)
    fill(im, 0, 0, 16, 3, '#c8402a')
    for y in range(4, 14, 2):
        for x in range(1, 9):
            dot(im, x, y, '#3a3226')
        for x in range(12, 15):
            dot(im, x, y, '#8a2a24')
    return im


def speaker():
    im = new('#7a7e80', 9464, 3)
    for x in range(2, 14):
        for y in range(2, 10):
            if (x + y) % 2:
                dot(im, x, y, '#3a3c3e')
    fill(im, 6, 11, 10, 14, '#c8402a')
    return im


def order_window():
    # 0.4.702: the sliding window shut, glass a reader sees the stand through (Jenna at the drawer behind it); the frame, the
    # aluminum where the two panes meet, a streak or two, and the red band under the sill.
    im = new('#efe9d8', 9465, 2)
    fill(im, 1, 2, 15, 12, '#000000', 0)
    for y in range(2, 12):
        dot(im, 8, y, ALUM)
    for x, y in ((3, 4), (4, 3), (11, 9), (12, 8), (13, 7)):
        dot(im, x, y, '#dfe8ec')
    fill(im, 0, 12, 16, 16, '#c8402a')
    for x in range(3, 13):
        dot(im, x, 13, '#efe9d8')
    return im


def fryer():
    im = steel()
    fill(im, 2, 2, 7, 14, '#c9a04a'); fill(im, 9, 2, 14, 14, '#c9a04a')
    for x in (4, 11):
        for y in range(0, 4):
            dot(im, x, y, '#3a3a3a')
    return im


def griddle():
    im = new('#2a2826', 9466, 4)
    for x in range(16):
        dot(im, x, 0, '#9a9c9e'); dot(im, x, 15, '#9a9c9e')
    return im


def picnic():
    return bench_wood()


def front_model(stage):
    t = {'d': 'display_lit', 'k': 'display_dark', 'r': 'transom', 'a': 'awning', 'v': 'valance', 'c': 'pilaster_cap', 'b': NS + 'block/list_storefront', 'm': 'galv'}
    if stage in (0, 4):
        g = 'd' if stage == 0 else 'k'
        e = [element([0, 0, 0], [16, 16, 2], {'north': g, 'south': g, 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})]
        if stage == 0:
            glow(e, 12)
        return model(e, t, 'display_lit' if stage == 0 else 'display_dark')
    if stage == 1:
        return model([element([0, 0, 0], [16, 16, 2], {'north': 'r', 'south': 'r', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'transom')
    if stage == 2:
        # The awning sloping down and out from the wall over the sidewalk, its outer edge the lower, so the name above it is clear.
        return model([element([0, 15, 0], [16, 16, 16], {'up': 'a', 'down': 'a', 'north': 'a', 'south': 'a', 'east': 'a', 'west': 'a'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 16, 16]})], t, 'awning')
    if stage == 3:
        return model([element([0, 6, 0.5], [16, 16, 0.5], {'north': 'v', 'south': 'v'})], t, 'valance')
    return model([element([0, 0, 0], [16, 16, 16], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b', 'down': 'b', 'up': 'c'}),
                  element([-1, 14, -1], [17, 16, 1], {'all': 'c'})], t, 'pilaster_cap', render=None)


def board_model(name, light=None, depth=13):
    e = [element([0, 0, depth], [16, 16, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'}, uv={'east': [0, 0, 1, 16], 'west': [15, 0, 16, 16], 'up': [0, 0, 16, 1], 'down': [0, 15, 16, 16]})]
    if light:
        glow(e, light)
    return model(e, {'f': name}, name, render=None)


def signdrivein_model(stage):
    t = {'g': 'galv'}
    if stage == 0:
        return model([element([5, 0, 5], [11, 16, 11], {'all': 'g'})], t, 'galv', render=None)
    if stage == 7:
        t['a'] = 'drivein_arrow'
        e = glow([element([0, 2, 7.5], [16, 14, 8.5], {'north': 'a', 'south': 'a'}, uv={'north': [0, 2, 16, 14], 'south': [0, 2, 16, 14]})], 14)
        return model(e, t, 'drivein_arrow')
    t['b'] = f'drivein_board_{stage}'
    # Lit on its face; its back is the frame, so the name is never read backwards from behind.
    e = glow([element([0, 0, 6], [16, 16, 7], {'north': 'b'})], 14)
    e.append(element([0, 0, 7], [16, 16, 10], {'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'}))
    return model(e, t, f'drivein_board_{stage}')


def drug_model(stage):
    t = {'g': 'goods_0', 'h': 'goods_1', 's': 'tape_shelf_side', 'c': 'counter_front_w', 't': 'counter_top', 'r': 'register', 'b': 'bottles',
         'm': 'chrome', 'v': 'stool_top', 'z': 'magazines', 'k': 'cards', 'e': 'counter_edge'}
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'g', 'south': 'h', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'goods_0', render=None)
    if stage == 1:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'h', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'goods_1', render=None)
    if stage in (2, 3):
        e = [element([0, 0, 0], [16, 14, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}),
             element([0, 14, 0], [16, 16, 16], {'up': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'})]
        if stage == 3:
            e.append(element([3, 16, 4], [13, 22, 12], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r'}))
        return model(e, t, 'counter_front_w', render=None)
    if stage == 4:
        return model([element([0, 0, 6], [16, 16, 16], {'north': 'b', 'east': 's', 'west': 's', 'up': 's', 'down': 's', 'south': 's'})], t, 'bottles', render=None)
    if stage == 5:
        return model([element([7, 0, 7], [9, 11, 9], {'all': 'm'}), element([5, 0, 5], [11, 1, 11], {'all': 'm'}), element([4, 11, 4], [12, 12.5, 12], {'up': 'v', 'down': 'm', 'north': 'v', 'south': 'v', 'east': 'v', 'west': 'v'})], t, 'stool_top', render=None)
    if stage == 6:
        return model([element([3, 0, 3], [13, 16, 13], {'north': 'z', 'south': 'z', 'east': 'z', 'west': 'z', 'up': 's'})], t, 'magazines', render=None)
    return model([element([7, 0, 7], [9, 2, 9], {'all': 'm'}), element([3, 2, 3], [13, 16, 13], {'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k', 'up': 's'})], t, 'cards', render=None)


def video_model(stage):
    t = {'s': 'tape_shelf_side', 'c': 'counter_front_w', 't': 'counter_top', 'e': 'counter_edge', 'r': 'register', 'w': 'rewinder',
         'd': 'return_slot', 'f': 'standee', 'k': 'tv_case', 'p': 'tape_screen', 'g': 'galv'}
    if stage in (0, 1):
        t['x'] = f'tapes_{stage}'
        return model([element([0, 0, 6], [16, 16, 16], {'north': 'x', 'east': 's', 'west': 's', 'up': 's', 'down': 's', 'south': 's'})], t, f'tapes_{stage}', render=None)
    if stage == 2:
        t['x'] = 'tapes_2'
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'x', 'south': 'x', 'east': 's', 'west': 's', 'up': 's', 'down': 's'})], t, 'tapes_2', render=None)
    if stage in (3, 4):
        e = [element([0, 0, 0], [16, 14, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}),
             element([0, 14, 0], [16, 16, 16], {'up': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'})]
        if stage == 4:
            e += [element([1, 16, 5], [9, 22, 12], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r'}),
                  element([10, 16, 6], [15, 19, 12], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w'})]
        return model(e, t, 'counter_front_w', render=None)
    if stage == 5:
        return model([element([3, 2, 13], [13, 12, 16], {'north': 'd', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'})], t, 'return_slot', render=None)
    if stage == 6:
        return model([element([2, 0, 8], [14, 28, 8], {'north': 'f', 'south': 'f'}, uv={'north': [1, 0, 15, 16], 'south': [1, 0, 15, 16]}),
                      element([6, 0, 8], [10, 10, 12], {'all': 'g'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 0, 8]})], t, 'standee')
    e = [element([2, 4, 4], [14, 14, 14], {'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k', 'up': 'k', 'down': 'k'}),
         element([7, 9, 14], [9, 11, 16], {'all': 'g'})]
    e += glow([element([3, 5, 3.9], [13, 13, 3.9], {'north': 'p'}, uv={'north': [2, 2, 14, 14]})], 8)
    return model(e, t, 'tape_screen')


def law_model(stage):
    t = {'m': 'gov_metal', 'd': 'gov_drawers', 'r': 'radio_set', 'b': 'bench_wood', 'g': 'galv', 'f': 'flag', 'c': 'corkboard', 'j': 'water_jug', 'o': 'cooler_body'}
    if stage in (0, 1):
        e = [element([0, 13, 0], [16, 15, 16], {'all': 'm'}), element([0, 0, 0], [5, 13, 16], {'north': 'd', 'south': 'm', 'east': 'm', 'west': 'm'}),
             element([11, 0, 0], [16, 13, 16], {'north': 'd', 'south': 'm', 'east': 'm', 'west': 'm'}), element([5, 0, 14], [11, 13, 15], {'all': 'm'})]
        if stage == 1:
            e += [element([3, 15, 6], [13, 20, 13], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r'}),
                  element([1, 15, 3], [3, 17, 5], {'all': 'r'})]
        return model(e, t, 'gov_metal', render=None)
    if stage == 2:
        return model([element([1, 0, 1], [15, 16, 15], {'north': 'd', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'gov_drawers', render=None)
    if stage == 3:
        return model([element([0, 6, 3], [16, 8, 13], {'all': 'b'}), element([1, 0, 4], [3, 6, 12], {'all': 'b'}), element([13, 0, 4], [15, 6, 12], {'all': 'b'}),
                      element([0, 8, 11], [16, 14, 13], {'all': 'b'})], t, 'bench_wood', render=None)
    if stage == 4:
        return model([element([7, 0, 7], [9, 16, 9], {'all': 'g'})], t, 'galv', render=None)
    if stage == 5:
        return model([element([7, 0, 7], [9, 16, 9], {'all': 'g'}), element([9, 4, 8], [25, 14, 8], {'north': 'f', 'south': 'f'}), element([6.5, 15, 6.5], [9.5, 16, 9.5], {'all': 'g'})], t, 'flag')
    if stage == 6:
        return model([element([1, 2, 14.5], [15, 14, 16], {'north': 'c', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}, uv={'north': [1, 1, 15, 13]})], t, 'corkboard', render=None)
    return model([element([4, 0, 4], [12, 10, 12], {'all': 'o'}), element([5, 10, 5], [11, 16, 11], {'all': 'j'})], t, 'water_jug', render=None)


def feed_model(stage):
    t = {'s': 'feed_sack', 't': 'sack_top', 'm': 'mineral', 'c': 'counter_front_w', 'w': 'counter_top', 'e': 'counter_edge', 'b': 'bench_wood',
         'a': 'bench_ad', 'p': 'seed_bin_top', 'r': 'rubber', 'k': NS + 'block/list_slab', 'o': 'oak_door'}
    if stage == 0:
        return model([element([0, 0, 0], [16, 8, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 't', 'down': 't'}, uv={k: [0, 0, 16, 16] for k in ('north', 'south', 'east', 'west')}),
                      element([1, 8, 1], [15, 16, 15], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 't'})], t, 'feed_sack', render=None)
    if stage == 1:
        return model([element([0, 0, 0], [16, 8, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'up': 't', 'down': 't'})], t, 'feed_sack', render=None)
    if stage == 2:
        return model([element([1, 0, 1], [8, 7, 8], {'all': 'm'}), element([8, 0, 2], [15, 7, 9], {'all': 'm'}), element([3, 0, 9], [10, 7, 15], {'all': 'm'}),
                      element([4, 7, 4], [11, 10, 11], {'all': 'm'})], t, 'mineral', render=None)
    if stage == 3:
        return model([element([0, 0, 0], [16, 14, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}),
                      element([0, 14, 0], [16, 16, 16], {'up': 'w', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'})], t, 'counter_front_w', render=None)
    if stage == 4:
        return model([element([0, 0, 0], [16, 13, 16], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'down': 'o', 'up': 'p'})], t, 'seed_bin_top', render=None)
    if stage in (5, 6):
        u = 0 if stage == 5 else 8
        e = [element([0, 6, 4], [16, 8, 13], {'all': 'b'}), element([0, 8, 12], [16, 14, 13.5], {'north': 'b', 'south': 'a', 'east': 'b', 'west': 'b', 'up': 'b'}, uv={'south': [u, 2, u + 8, 8]}),
             element([2 if stage == 5 else 11, 0, 4], [5 if stage == 5 else 14, 6, 13], {'all': 'k'})]
        return model(e, t, 'bench_wood', render=None)
    return model([element([0, 0, 13], [16, 10, 16], {'all': 'r'})], t, 'rubber', render=None)


def drivein_model(stage):
    t = {'u': 'canopy_under', 'e': 'canopy_edge', 'l': 'canopy_light', 'g': 'galv', 'm': 'menu_board', 's': 'speaker', 'o': 'order_window',
         'f': 'fryer', 'd': 'griddle', 'p': 'picnic', 't': 'steel', 'r': NS + 'block/list_flat_roof'}
    if stage in (0, 1):
        e = [element([0, 12, 0], [16, 16, 16], {'down': 'u', 'up': 'r', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'})]
        if stage == 1:
            e += glow([element([3, 11.8, 3], [13, 12, 13], {'down': 'l'})], 15)
        return model(e, t, 'canopy_under', render=None)
    if stage == 2:
        return model([element([6, 0, 6], [10, 16, 10], {'all': 'g'})], t, 'galv', render=None)
    if stage == 3:
        e = [element([7, 0, 7], [9, 16, 9], {'all': 'g'}), element([2, 6, 5], [14, 15, 7], {'north': 'm', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'}),
             element([5, 2, 4], [11, 6, 7], {'north': 's', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'})]
        glow(e[1:2], 12)
        return model(e, t, 'menu_board', render=None)
    if stage == 4:
        # The pane is a pixel thick and its glass is clear, so the stand shows through it from the lot and the lot from the stand.
        e = glow([element([0, 0, 8], [16, 16, 9], {'north': 'o', 'south': 'o', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'},
                          uv={'east': [8, 0, 9, 16], 'west': [7, 0, 8, 16], 'up': [0, 8, 16, 9], 'down': [0, 7, 16, 8]})], 12)
        e.append(element([0, 5, 4], [16, 6, 8], {'all': 't'}))
        return model(e, t, 'order_window', render='cutout')
    if stage == 5:
        return model([element([0, 0, 1], [16, 15, 16], {'north': 't', 'south': 't', 'east': 't', 'west': 't', 'up': 'f', 'down': 't'})], t, 'fryer', render=None)
    if stage == 6:
        return model([element([0, 0, 1], [16, 14, 16], {'all': 't'}), element([0, 14, 1], [16, 15, 16], {'up': 'd', 'north': 't', 'south': 't', 'east': 't', 'west': 't'})], t, 'griddle', render=None)
    e = [element([0, 10, 3], [16, 12, 13], {'all': 'p'}), element([0, 5, 0], [16, 6.5, 3], {'all': 'p'}), element([0, 5, 13], [16, 6.5, 16], {'all': 'p'}),
         element([2, 0, 7], [3.5, 10, 9], {'all': 'p'}), element([12.5, 0, 7], [14, 10, 9], {'all': 'p'}), element([2, 3, 1], [3.5, 4.5, 15], {'all': 'p'}),
         element([12.5, 3, 1], [14, 4.5, 15], {'all': 'p'})]
    return model(e, t, 'picnic', render=None)


# ------------------------------------------------------------------------------------------------ the gin, the seed house and the tower
MACHINE = '#6e7a5e'


def machine(seed, base=MACHINE):
    # The gin's machines: painted steel gone chalky, bolted seams, lint caught on every ledge.
    rnd = random.Random(seed); im = new(base, seed, 4)
    for x in range(16):
        dot(im, x, 0, shade(base, 14)); dot(im, x, 15, shade(base, -18))
    for x in (1, 14):
        for y in range(1, 15, 4):
            dot(im, x, y, '#3a3e34')
    speckle(im, rnd, 9, ['#ece8dc', '#dcd6c8'])
    return im


def feeder_front():
    # The module feeder's mouth: spiked cylinders behind a guard, the cotton torn off the module going into them.
    im = machine(9500)
    fill(im, 2, 3, 14, 13, '#2a2824')
    for y in (5, 9):
        for x in range(2, 14):
            dot(im, x, y, '#8a8c8e')
            if x % 2:
                dot(im, x, y - 1, '#c8cacc')
    for x in range(2, 14, 3):
        for y in range(3, 13):
            dot(im, x, y, '#a8642a')
    rnd = random.Random(9501)
    speckle(im, rnd, 14, ['#f1eee4', '#e2ddd0'], (2, 9, 14, 13))
    return im


def stand_front():
    # A gin stand: the panel over the saws, its gauges, the lint flue's mouth.
    im = machine(9502, '#5e6a7a')
    fill(im, 3, 3, 13, 8, '#3a3e44')
    for x in range(3, 13):
        if x % 2:
            dot(im, x, 5, '#c8cacc')
    for cx in (5, 11):
        dot(im, cx, 11, '#efe9d8'); dot(im, cx + 1, 11, '#efe9d8'); dot(im, cx, 12, '#2a2a2a')
    return im


def cleaner_front():
    im = machine(9503, '#7a6e5a')
    for y in range(3, 13, 2):
        for x in range(2, 14):
            dot(im, x, y, '#4a4234')
    return im


def press_front(upper):
    # The bale press: heavy steel, its chamber's doors with their latches below, the ram's cylinder above.
    im = machine(9504 + upper, '#4e5664')
    if upper:
        fill(im, 5, 0, 11, 16, '#8a9096'); fill(im, 6, 0, 7, 16, '#b8bec4')
    else:
        for x in (7, 8):
            for y in range(1, 15):
                dot(im, x, y, '#2a2e34')
        for y in (4, 11):
            fill(im, 4, y, 7, y + 1, '#c8b048'); fill(im, 9, y, 12, y + 1, '#c8b048')
    return im


def auger_flight():
    # An auger's screw seen from above in its trough: the flighting turning in a helix, polished where cottonseed wears it.
    im = new('#5a5e60', 9506, 3)
    for x in range(16):
        y0 = (x * 2) % 16
        for dy in range(3):
            dot(im, x, (y0 + dy) % 16, '#c8ccce' if dy == 0 else '#9a9ea0')
    for x in range(16):
        dot(im, x, 7, '#3a3c3e'); dot(im, x, 8, '#3a3c3e')
    return im


def trough():
    im = new('#7a7e80', 9507, 3)
    for x in range(16):
        dot(im, x, 0, '#9ea2a4'); dot(im, x, 15, '#5a5e60')
    return im


def pipe():
    im = galvanized()
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, '#7e8283')
    return im


def bale_side():
    # A cotton bale in its bagging, six steel ties round it, the lint showing at the seams.
    rnd = random.Random(9510); im = new('#d9d2bf', 9510, 4)
    for x in range(16):
        for y in range(16):
            if (x + y * 3) % 7 == 0:
                darken(im, x, y, -8)
    for y in (2, 7, 12):
        for x in range(16):
            dot(im, x, y, '#6a6e70')
    speckle(im, rnd, 8, ['#f2efe6'])
    return im


def bale_tag():
    im = bale_side()
    fill(im, 5, 8, 11, 11, '#f2e870'); dot(im, 6, 9, '#3a3a3a'); dot(im, 8, 9, '#3a3a3a'); dot(im, 9, 9, '#3a3a3a')
    return im


def module_side():
    # A cotton module: seed cotton packed into a block the size of a truck, its sides lumpy and white with leaf trash in them.
    rnd = random.Random(9511); im = new('#ece7da', 9511, 6)
    speckle(im, rnd, 26, ['#d8d0bc', '#c8bea6', '#f6f3ea'])
    speckle(im, rnd, 6, ['#7a6a4a', '#5a5a3a'])
    return im


def tarp():
    im = new('#2e4e6e', 9512, 4)
    for y in range(0, 16, 5):
        for x in range(16):
            darken(im, x, y, -12)
    return im


def module_end():
    im = module_side()
    for x in range(16):
        dot(im, x, 0, '#2e4e6e'); dot(im, x, 1, '#2e4e6e'); dot(im, x, 2, '#3a5a7a')
    for x in (3, 12):
        for y in range(3, 9):
            dot(im, x, y, '#c8a848')
    return im


def bin_wall():
    im = new('#7a6a52', 9520, 4)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#4e4232')
    for x in (0, 15):
        for y in range(16):
            dot(im, x, y, '#5e5240')
    rnd = random.Random(9520)
    speckle(im, rnd, 8, ['#e4ded0'])
    return im


def cottonseed():
    # Cottonseed in a pile: fuzzy grey-white seeds, a little lint still on them.
    rnd = random.Random(9521); im = new('#cfc8b8', 9521, 7)
    for _ in range(50):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#e8e2d4', '#b0a898', '#9a9282', '#f2eee4']))
    return im


def ladder_tex():
    im = new(None)
    for y in range(16):
        dot(im, 1, y, '#6a6e70'); dot(im, 2, y, '#8a8e90'); dot(im, 13, y, '#6a6e70'); dot(im, 14, y, '#8a8e90')
    for y in (2, 6, 10, 14):
        for x in range(2, 14):
            dot(im, x, y, '#a8acae')
    return im


def door_sliding(upper):
    im = corrugated('#9a9f9f', 9522 + upper, 4)
    if not upper:
        fill(im, 12, 6, 14, 9, '#3a3a3a')
    else:
        for x in range(16):
            dot(im, x, 0, '#4a4c4d'); dot(im, x, 1, '#6a6e70')
    return im


def tower_paint():
    im = new('#cfd6da', 9530, 3)
    for y in range(16):
        dot(im, 0, y, '#b4bcc0'); dot(im, 15, y, '#e4eaec')
    return im


def tower_rod():
    return new('#a8b0b4', 9531, 2)


def grate():
    im = new(None)
    for x in range(16):
        for y in range(16):
            if x % 3 == 0 or y % 4 == 0:
                dot(im, x, y, '#9aa2a6' if x % 3 == 0 else '#7a8286')
    return im


def beacon():
    im = new('#d8302a', 9532, 5)
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 12:
                dot(im, x, y, '#ff7a5a')
    return im


def tank_wall(letters=None):
    # The tank's skin: painted steel plates riveted at their seams, the town's name painted round it a pair of letters to a plate.
    rnd = random.Random(9533); im = new('#d4dade', 9533, 2)
    for x in range(16):
        dot(im, x, 0, '#aeb6ba'); dot(im, x, 15, '#aeb6ba')
    for y in range(0, 16, 3):
        dot(im, 0, y, '#8e969a'); dot(im, 15, y, '#8e969a')
    for y in range(9, 16):
        for x in range(16):
            if rnd.random() < .06 * (y - 8):
                dot(im, x, y, '#9a8a6a')  # rust weeping from the lower seam
    if letters:
        centre(im, 4, letters, BROAD, '#2a3e6e')
    return im


def tank_roof():
    im = new('#c8ced2', 9534, 3)
    for x in range(16):
        dot(im, x, 15, '#9aa2a6')
    return im


def gin_model(stage):
    t = {'m': 'machine', 'f': 'feeder_front', 's': 'stand_front', 'c': 'cleaner_front', 'p': 'press_lower', 'q': 'press_upper', 'a': 'auger_flight',
         'r': 'trough', 'o': 'pipe', 'g': 'galv'}
    if stage in (0, 1):
        face = 'f' if stage == 0 else 'm'
        return model([element([0, 0, 0], [16, 16, 16], {'north': face, 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'feeder_front', render=None)
    if stage == 2:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 's', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'stand_front', render=None)
    if stage == 3:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'cleaner_front', render=None)
    if stage in (4, 5):
        face = 'p' if stage == 4 else 'q'
        return model([element([0, 0, 0], [16, 16, 16], {'north': face, 'south': face, 'east': 'm', 'west': 'm', 'up': 'm', 'down': 'm'})], t, 'press_lower', render=None)
    if stage == 6:
        # The auger in its open trough, the screw's shaft running east and west along it.
        e = [element([0, 0, 2], [16, 1, 14], {'all': 'r'}), element([0, 1, 2], [16, 9, 3], {'all': 'r'}), element([0, 1, 13], [16, 9, 14], {'all': 'r'}),
             element([0, 2, 4], [16, 8, 12], {'up': 'a', 'down': 'a', 'north': 'a', 'south': 'a'}, uv={'north': [0, 5, 16, 11], 'south': [0, 5, 16, 11]})]
        return model(e, t, 'auger_flight', render=None)
    return model([element([3, 0, 3], [13, 16, 13], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'g', 'down': 'g'})], t, 'pipe', render=None)


def bale_model(stage):
    t = {'b': 'bale_side', 'g': 'bale_tag', 'm': 'module_side', 't': 'tarp', 'e': 'module_end'}
    if stage in (0, 1):
        side = 'b' if stage == 0 else 'g'
        return model([element([0, 0, 0], [16, 16, 16], {'north': side, 'south': 'b', 'east': 'b', 'west': 'b', 'up': 'b', 'down': 'b'})], t, 'bale_side', render=None)
    if stage == 2:
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'm', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 't', 'down': 'm'})], t, 'module_side', render=None)
    return model([element([0, 0, 0], [16, 16, 16], {'north': 'e', 'south': 'm', 'east': 'm', 'west': 'm', 'up': 't', 'down': 'm'})], t, 'module_end', render=None)


def seed_model(stage):
    t = {'w': 'bin_wall', 's': 'cottonseed', 'a': 'auger_flight', 'r': 'trough', 'l': 'ladder', 'd': 'door_slide_lower', 'u': 'door_slide_upper',
         'g': 'galv', 'h': 'barn_shade', 'b': 'bulb', 'o': 'pipe'}
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'all': 'w'})], t, 'bin_wall', render=None)
    if stage == 1:
        return model([element([0, 0, 0], [16, 10, 16], {'up': 's', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'})], t, 'cottonseed', render=None)
    if stage == 2:
        # The auger in its trench under the bins: the screw open, turning east and west, seed spilling into it from both sides.
        e = [element([0, 0, 2], [16, 1, 14], {'all': 'r'}), element([0, 1, 2], [16, 8, 3], {'all': 'r'}), element([0, 1, 13], [16, 8, 14], {'all': 'r'}),
             element([0, 2, 4], [16, 7, 12], {'up': 'a', 'down': 'a', 'north': 'a', 'south': 'a'}, uv={'north': [0, 5, 16, 10], 'south': [0, 5, 16, 10]})]
        return model(e, t, 'auger_flight', render=None)
    if stage == 3:
        return model([element([1, 0, 15], [15, 16, 15], {'north': 'l', 'south': 'l'})], t, 'ladder')
    if stage in (4, 5):
        return model([element([0, 0, 13], [16, 16, 16], {'north': 'd' if stage == 4 else 'u', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'})], t, 'door_slide_lower', render=None)
    if stage == 6:
        e = [element([7.5, 12, 11], [8.5, 13, 16], {'all': 'g'}), element([4, 9, 8], [12, 12, 14], {'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h'})]
        e += glow([element([5.5, 8.5, 9.5], [10.5, 9, 12.5], {'down': 'b'})], 15)
        return model(e, t, 'barn_shade')
    return model([element([4, 0, 0], [12, 16, 16], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'g', 'down': 'g'}, rotation={'angle': 22.5, 'axis': 'x', 'origin': [8, 8, 8]})], t, 'pipe', render=None)


def tower_model(stage):
    t = {'p': 'tower_paint', 'r': 'tower_rod', 'l': 'ladder', 'g': 'grate', 'b': 'beacon', 'o': 'pipe'}
    leg = element([5, 0, 5], [11, 16, 11], {'all': 'p'})
    if stage == 0:
        return model([leg], t, 'tower_paint', render=None)
    if stage == 1:
        # A leg's panel of braces: tie rods crossing corner to corner, as the legs' own struts.
        e = [element([0, 0, 0], [2, 16, 2], {'all': 'p'}), element([14, 0, 0], [16, 16, 2], {'all': 'p'}),
             element([-3, 7.5, 0.5], [19, 8.5, 1.5], {'all': 'r'}, rotation={'angle': 45, 'axis': 'z', 'origin': [8, 8, 1]}),
             element([-3, 7.5, 0.6], [19, 8.5, 1.4], {'all': 'r'}, rotation={'angle': -45, 'axis': 'z', 'origin': [8, 8, 1]})]
        return model(e, t, 'tower_paint', render=None)
    if stage == 2:
        return model([element([2, 0, 15], [14, 16, 15], {'north': 'l', 'south': 'l'}, uv={'north': [1, 0, 15, 16], 'south': [1, 0, 15, 16]})], t, 'ladder')
    if stage == 3:
        return model([element([0, 0, 0], [16, 2, 16], {'up': 'g', 'down': 'g', 'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r'})], t, 'grate')
    if stage == 4:
        e = [element([0, 14, 14.5], [16, 15.5, 16], {'all': 'r'}), element([0, 7, 14.5], [16, 8, 16], {'all': 'r'})]
        e += [element([x, 0, 14.5], [x + 1.5, 14, 16], {'all': 'r'}) for x in (0, 14.5)]
        return model(e, t, 'tower_rod', render=None)
    if stage == 5:
        e = [element([6, 0, 6], [10, 3, 10], {'all': 'r'})]
        e += glow([element([5.5, 3, 5.5], [10.5, 8, 10.5], {'all': 'b'})], 15)
        return model(e, t, 'beacon')
    return model([element([4, 0, 4], [12, 16, 12], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'p', 'down': 'p'})], t, 'pipe', render=None)


def tank_model(stage):
    t = {'w': 'tank_wall', 'r': 'tank_roof'}
    if stage <= 3:
        t['l'] = f'tank_{stage}' if stage else 'tank_wall'
        return model([element([0, 0, 0], [16, 16, 16], {'north': 'l', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'r', 'down': 'w'})], t, 'tank_wall', render=None)
    if stage == 4:
        # The roof's slope: a step up toward the middle, its north side the eave.
        return model([element([0, 0, 0], [16, 8, 16], {'all': 'r'}), element([0, 8, 6], [16, 16, 16], {'all': 'r'})], t, 'tank_roof', render=None)
    if stage == 5:
        return model([element([2, 0, 2], [14, 6, 14], {'all': 'r'}), element([6, 6, 6], [10, 9, 10], {'all': 'r'})], t, 'tank_roof', render=None)
    return model([element([0, 8, 0], [16, 16, 16], {'all': 'w'}), element([3, 0, 3], [13, 8, 13], {'all': 'w'})], t, 'tank_wall', render=None)


# ------------------------------------------------------------------------------------------------ 0.4.700: the school's field
# The stadium west of town: the turf, its stripes and lines, the end zones and the cinder track; the goalposts, the light towers, the
# home and visitors' stands with the press box, the team benches, the scoreboard, the ticket booth, the concession stand and the field
# house with its lockers, benches, the coach's office and the showers. Then Amber's house across two streets, and the things in the
# neighbors' yards.

def turf(dark):
    # The field's grass, cut short in July and watered every night, a band mown one way then the other.
    rnd = random.Random(9601 + dark); base = '#3f6a33' if dark else '#4a7a3a'
    im = new(base, 9601 + dark, 5)
    for x in range(16):
        for y in range(16):
            if (x + y * 3 + dark) % 7 == 0:
                darken(im, x, y, -10)
    speckle(im, rnd, 14, [shade(base, 14), shade(base, -14), shade(base, 8)])
    return im


def turf_line(along_z):
    # A yard line painted across the turf, a little worn where the cleats cross it; the sidelines run the other way.
    im = turf(0); rnd = random.Random(9603 + along_z)
    for i in range(16):
        for j in (7, 8):
            x, y = (j, i) if along_z else (i, j)
            dot(im, x, y, '#e8eade' if rnd.random() > .12 else '#9aa98e')
    return im


def endzone():
    rnd = random.Random(9605); im = new('#355c2c', 9605, 4)
    for x in range(16):
        for y in range(16):
            if (x - y) % 8 == 0:
                dot(im, x, y, '#41683a')
    speckle(im, rnd, 10, ['#2c4f25', '#4a7440'])
    return im


def track_cinder():
    # The track round the field: rolled cinders, ruddy and fine, a lane line every other lane.
    rnd = random.Random(9606); im = new('#8a4a34', 9606, 6)
    speckle(im, rnd, 30, ['#9c5a42', '#76402e', '#a8664c', '#6a382a'])
    for x in range(16):
        if rnd.random() > .2:
            dot(im, x, 0, '#d8d2c4')
    return im


def goal_paint():
    im = new('#d9b52a', 9607, 4)
    for y in range(16):
        dot(im, 6, y, '#efd35a'); dot(im, 10, y, '#a8861c')
    return im


def goal_pad():
    # The pad round the goalpost's foot: vinyl over foam, laced up the back.
    im = new('#283a5a', 9608, 3)
    for y in range(16):
        dot(im, 0, y, '#1c2a44'); dot(im, 15, y, '#1c2a44')
    for y in range(2, 15, 3):
        dot(im, 7, y, '#d8d2c4'); dot(im, 8, y + 1, '#d8d2c4')
    return im


def light_bank():
    # A bank of floodlights at the top of a tower, six lamps in their housings, dark tonight.
    im = new('#6e7274', 9609, 3)
    for cx in (0, 5, 10):
        for cy in (1, 8):
            fill(im, cx + 1, cy + 1, cx + 5, cy + 6, '#2a2e30')
            dot(im, cx + 2, cy + 2, '#8a9294'); dot(im, cx + 3, cy + 3, '#4e5456')
    return im


def alum(edge):
    # The stands' planks: extruded aluminum, ribbed lengthwise, scuffed where people have stood.
    rnd = random.Random(9610 + edge); im = new('#b6babb', 9610 + edge, 3)
    for y in range(0, 16, 4 if not edge else 8):
        for x in range(16):
            dot(im, x, y, '#8e9293')
    speckle(im, rnd, 6, ['#9fa3a4', '#cfd2d2'])
    return im


def press_glass():
    # The press box's window onto the field: a dark room, the edge of a table, a microphone's stand.
    im = new(None)
    fill(im, 0, 0, 16, 16, '#d8d2c4')
    fill(im, 1, 2, 15, 14, '#1e2328')
    line(im, 2, 13, 9, 3, '#2c333a')
    fill(im, 1, 11, 15, 12, '#4a3e30')
    dot(im, 11, 9, '#101214'); dot(im, 11, 10, '#101214'); dot(im, 11, 8, '#3a3e40')
    return im


def plywood():
    rnd = random.Random(9612); im = new('#a88a5e', 9612, 4)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y) % 13 == 0:
                darken(im, x, y, -12)
    return im


def pa_horn():
    im = new('#8a8e8c', 9613, 3)
    fill(im, 4, 4, 12, 12, '#3a3e3c'); fill(im, 6, 6, 10, 10, '#1c1e1e')
    return im


def locker(upper):
    # A locker room's lockers, painted steel gone dull, vents across the top, a number plate and a latch.
    rnd = random.Random(9614 + upper); im = new('#3d5a7a', 9614 + upper, 3)
    for x in (0, 8):
        for y in range(16):
            dot(im, x, y, '#2a3e56')
    if upper:
        for door in (0, 8):
            for y in (2, 4, 6):
                for x in range(door + 2, door + 6):
                    dot(im, x, y, '#1e2c3e')
            fill(im, door + 3, 10, door + 5, 12, '#c8c2b0')
    else:
        for door in (0, 8):
            fill(im, door + 5, 2, door + 7, 5, '#9ea2a4')
            for y in (12, 13):
                for x in range(door + 2, door + 6):
                    dot(im, x, y, '#1e2c3e')
    speckle(im, rnd, 5, ['#4e6a8a', '#33506e'])
    return im


def locker_side():
    im = new('#3d5a7a', 9615, 3)
    for y in range(16):
        dot(im, 0, y, '#2a3e56'); dot(im, 15, y, '#2a3e56')
    return im


def chalkboard():
    # The coach's board: the last practice's play in chalk, a line of circles, the crosses facing them, the arrows of the routes, a frame
    # of oak.
    im = new('#2a332c', 9616, 3)
    for i in range(16):
        dot(im, i, 0, '#8a6a46'); dot(im, i, 15, '#8a6a46'); dot(im, 0, i, '#8a6a46'); dot(im, 15, i, '#8a6a46')
    chalk, faint = '#d8d6cc', '#a8a69c'
    for x in (3, 6, 9, 12):
        for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            dot(im, x + dx, 11 + dy, chalk)
    for x, y in ((4, 4), (8, 3), (12, 5)):
        for d in (-1, 0, 1):
            dot(im, x + d, y + d, chalk); dot(im, x + d, y - d, chalk)
    line(im, 3, 9, 2, 5, faint); line(im, 2, 5, 3, 3, faint)
    line(im, 12, 9, 13, 7, faint)
    line(im, 6, 9, 6, 6, faint); line(im, 6, 6, 9, 6, faint)
    return im


def concession_menu():
    # The concession stand's board, lettered on a white field: what a game night sells, each with its price to the right.
    im = new('#efebdd', 9617, 2)
    fill(im, 0, 0, 16, 2, '#8a2a24')
    for row, length in enumerate((7, 5, 8, 4)):
        y = 4 + row * 3
        for x in range(1, 1 + length):
            if x != 4 or row % 2:
                dot(im, x, y, '#2a2620')
        for x in range(12, 15):
            dot(im, x, y, '#8a2a24')
    return im


def scoreboard():
    # The scoreboard behind the south end zone, three boards across and two down: HOME, the quarter and the visitors over their bulbs,
    # dark tonight; the down, the yards and the ball's spot beneath.
    im = new('#16181a', 9618, 2, size=(48, 32))
    for x in range(48):
        dot(im, x, 0, '#c8a03a'); dot(im, x, 31, '#c8a03a')
    for y in range(32):
        dot(im, 0, y, '#c8a03a'); dot(im, 47, y, '#c8a03a')
    for label, x0, y in (('HOME', 0, 2), ('QTR', 16, 2), ('VIS', 32, 2), ('DOWN', 0, 18), ('YDS', 16, 18), ('BALL', 32, 18)):
        centre(im, y, label, TALL, '#efe9d8', width=16, x0=x0)

    def bulbs(x0, y0):
        # One digit's bulbs, dark: three across and five down, two pixels apart.
        for i in range(3):
            for j in range(5):
                if i == 1 and j in (1, 3):
                    continue
                dot(im, x0 + i * 2, y0 + j * 2, '#4a3a2c')
    for x0 in (3, 9, 35, 41):
        bulbs(x0, 8)
    bulbs(22, 8)
    for x0 in (6, 22, 38):
        for i in range(3):
            dot(im, x0 + i * 2, 26, '#4a3a2c'); dot(im, x0 + i * 2, 28, '#4a3a2c')
    return im


def ticket_window():
    im = new('#d8d2c4', 9619, 2)
    fill(im, 0, 0, 16, 5, '#8a2a24')
    centre(im, 0, 'GATE', TALL, '#efe9d8')
    fill(im, 2, 6, 14, 14, '#1e2328')
    fill(im, 6, 13, 10, 14, '#4a4440')
    return im


def door_steel(upper):
    # The field house's doors: steel painted over and over, a little wired window in the top, a push bar and a kick plate below.
    im = new('#6a7a6a', 9620 + upper, 3)
    for y in range(16):
        dot(im, 0, y, '#4e5a4e'); dot(im, 15, y, '#4e5a4e')
    if upper:
        fill(im, 5, 4, 11, 11, '#1e2328')
        for x in range(5, 11, 2):
            for y in range(4, 11, 2):
                dot(im, x, y, '#5a6260')
    else:
        fill(im, 2, 1, 14, 3, '#9ea2a4')
        fill(im, 1, 12, 15, 16, '#8e9294')
    return im


# Amber's house
def vcr():
    # The VCR under the television, its clock blinking twelve because nobody has set it, a tape half in its door.
    im = new('#1e1e20', 9622, 2)
    fill(im, 1, 3, 9, 5, '#0a0a0c')
    fill(im, 3, 2, 7, 3, '#2a2a2e')
    write(im, 10, 2, '12', TALL, '#4ad86a')
    for x in range(1, 15, 2):
        dot(im, x, 10, '#4a4a4e')
    fill(im, 1, 12, 15, 13, '#38383c')
    return im


def rental_tapes():
    # A shelf of rented tapes in their clamshells, black, the video store's sticker on every spine and the titles in marker.
    rnd = random.Random(9623); im = new('#5a3e26', 9623, 2)
    for row in range(2):
        y0 = 1 + row * 8
        x = 1
        while x < 15:
            fill(im, x, y0, x + 2, y0 + 6, '#121214')
            dot(im, x, y0 + 1, '#d84a3a'); dot(im, x + 1, y0 + 1, '#efe9d8')
            if rnd.random() < .7:
                dot(im, x, y0 + 3, '#c8c6be'); dot(im, x + 1, y0 + 4, '#c8c6be')
            x += 2 + (1 if rnd.random() < .3 else 0)
        for i in range(16):
            dot(im, i, y0 + 6, '#3e2a18')
    return im


def tape_case():
    im = new('#121214', 9624, 1)
    fill(im, 2, 2, 14, 6, '#efe9d8'); fill(im, 3, 3, 13, 5, '#d84a3a')
    for x in range(3, 13, 2):
        dot(im, x, 10, '#4a4a4e')
    return im


def notebook():
    # A spiral notebook open on the desk, the pages written to the edges, a pen in the spine.
    im = new('#efebdd', 9625, 2)
    for y in range(2, 15, 2):
        for x in range(1, 7):
            dot(im, x, y, '#6a6a80')
        for x in range(9, 15):
            if not (y > 10 and x > 11):
                dot(im, x, y, '#6a6a80')
    for y in range(16):
        dot(im, 8, y, '#8e8e94' if y % 2 else '#4a4a50')
    line(im, 2, 14, 7, 9, '#1e2a5a')
    return im


def amber_window():
    # 0.4.702: her window, glass the yard sees through to her at her desk in her lamp's light. A double-hung sash painted white, the
    # meeting rail across the middle, the blind rolled up under the head with its cord hanging; clear panes with a streak or two.
    im = new(None)
    frame, shadow = '#e8e6de', '#bdb9ac'
    fill(im, 0, 0, 16, 1, frame); fill(im, 0, 15, 16, 16, frame); fill(im, 0, 0, 1, 16, frame); fill(im, 15, 0, 16, 16, frame)
    fill(im, 1, 14, 15, 15, shadow); fill(im, 1, 1, 2, 15, shadow)
    fill(im, 1, 7, 15, 9, frame); fill(im, 1, 8, 15, 9, shadow)
    fill(im, 1, 1, 15, 3, '#d8d0b8'); fill(im, 1, 3, 15, 4, '#b8ae94')
    dot(im, 8, 4, '#b8ae94'); dot(im, 8, 5, '#d8d0b8')
    for x, y in ((4, 5), (5, 4), (11, 12), (12, 11), (13, 10)):
        dot(im, x, y, '#dfe8ec')
    return im


def remote():
    im = new('#1e1e20', 9626, 1)
    for x, y in ((5, 3), (7, 3), (9, 3), (5, 6), (7, 6), (9, 6), (5, 9), (7, 9), (9, 9)):
        dot(im, x, y, '#8a8a90')
    dot(im, 7, 1, '#c84a3a')
    return im


# The yards
def sprinkler_head():
    im = new('#3a3c3a', 9627, 2)
    fill(im, 6, 0, 10, 16, '#9a9c9a')
    return im


def spray():
    # The sprinkler's water, thrown in a fan and catching the street light.
    rnd = random.Random(9628); im = new(None)
    for _ in range(46):
        x = rnd.randrange(16); y = rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#d8e6ee', '#b8ccd8', '#eef4f6']), a=rnd.choice([120, 170, 210]))
    return im


def doghouse(roof):
    if roof:
        return shingle()
    im = new('#b8a07a', 9629, 4)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#8a7656')
    return im


def doghouse_door():
    im = doghouse(0)
    fill(im, 4, 5, 12, 16, '#141210')
    for x in range(5, 11):
        dot(im, x, 4, '#141210')
    return im


def can_galv():
    im = new('#9a9e9c', 9630, 4)
    for y in (3, 8, 13):
        for x in range(16):
            dot(im, x, y, '#7a7e7c')
    return im


def bike():
    # A kid's bike left on its side in the grass: two wheels, the frame between, a banana seat, high handlebars.
    im = new(None)
    for cx, cy in ((4, 10), (12, 10)):
        for a in range(0, 360, 20):
            dot(im, round(cx + 3 * math.cos(math.radians(a))), round(cy + 3 * math.sin(math.radians(a))), '#1e1e20')
        dot(im, cx, cy, '#9ea2a4')
    line(im, 4, 10, 8, 6, '#8a2a24'); line(im, 8, 6, 12, 10, '#8a2a24'); line(im, 8, 6, 8, 10, '#8a2a24'); line(im, 4, 10, 8, 10, '#8a2a24')
    fill(im, 6, 4, 10, 5, '#2a2a2e'); line(im, 12, 10, 11, 4, '#9ea2a4'); line(im, 10, 3, 13, 3, '#9ea2a4')
    return im


def webbing():
    # A folding lawn chair's webbing, two colours woven, frayed at one strap.
    im = new(None)
    for x in range(16):
        for y in range(16):
            c = '#4a8a5a' if (x // 3 + y // 3) % 2 else '#efe9d8'
            dot(im, x, y, c)
    return im


def grill_black():
    im = new('#1e1e20', 9631, 3)
    for x in range(16):
        dot(im, x, 2, '#3a3a3e')
    return im


def hose_green():
    im = new('#3e6a34', 9632, 3)
    for y in range(0, 16, 3):
        for x in range(16):
            dot(im, x, y, '#2c4e26')
    return im


def pool_plastic(inside):
    im = new('#3a7ab8' if not inside else '#5a9ac8', 9633 + inside, 3)
    if inside:
        rnd = random.Random(9635)
        speckle(im, rnd, 8, ['#8a7a4a', '#6a5a3a'])
        for y in range(16):
            for x in range(16):
                if (x + y) % 7 == 0:
                    darken(im, x, y, 10)
    return im


def field_model(stage):
    t = {'y': 'goal_paint', 'p': 'goal_pad', 'g': 'galv', 'b': 'light_bank'}
    if stage == 0:
        return model([element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'y'}), element([5, 0, 5], [11, 12, 11], {'all': 'p'})], t, 'goal_pad', render=None)
    if stage == 1:
        return model([element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'y'})], t, 'goal_paint', render=None)
    if stage == 2:
        # The gooseneck: the pole bends forward over the end line toward the crossbar.
        return model([element([6.5, 0, 6.5], [9.5, 9, 9.5], {'all': 'y'}), element([6.5, 6, 0], [9.5, 9, 6.5], {'all': 'y'})], t, 'goal_paint', render=None)
    if stage == 3:
        # The crossbar's middle, reaching a block either way to the ends that carry the uprights; the gooseneck's stub behind it.
        return model([element([-16, 6, 6.5], [32, 9, 9.5], {'all': 'y'}, uv={'north': [0, 0, 16, 3], 'south': [0, 0, 16, 3], 'up': [0, 0, 16, 3], 'down': [0, 0, 16, 3],
                                                                         'east': [0, 0, 3, 3], 'west': [0, 0, 3, 3]}),
                      element([6.5, 6, 9.5], [9.5, 9, 16], {'all': 'y'})], t, 'goal_paint', render=None)
    if stage == 4:
        # An end of the crossbar, its upright rising from it; turned for the other end.
        return model([element([6.5, 6, 6.5], [16, 9, 9.5], {'all': 'y'}), element([6.5, 6, 6.5], [9.5, 16, 9.5], {'all': 'y'})], t, 'goal_paint', render=None)
    if stage == 5:
        return model([element([7, 0, 7], [9, 16, 9], {'all': 'y'})], t, 'goal_paint', render=None)
    if stage == 6:
        e = [element([6, 0, 6], [10, 16, 10], {'all': 'g'})]
        e += [element([5, y, 7.5], [11, y + 1, 8.5], {'all': 'g'}) for y in (4, 12)]
        return model(e, t, 'galv', render=None)
    # The bank on its tower's top, leaning toward the field.
    e = [element([6, 0, 6], [10, 4, 10], {'all': 'g'}),
         element([-4, 4, 2], [20, 16, 6], {'north': 'b', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'},
                 uv={'north': [0, 0, 16, 16], 'east': [0, 0, 4, 12], 'west': [0, 0, 4, 12], 'up': [0, 0, 16, 4], 'down': [0, 0, 16, 4]},
                 rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 10, 4]})]
    return model(e, t, 'light_bank', render=None)


def stands_model(stage):
    t = {'a': 'alum', 'e': 'alum_edge', 'g': 'galv', 'w': 'press_glass', 'p': 'plywood', 'h': 'pa_horn', 'k': 'remote'}
    if stage in (0, 1):
        # A row of the stands: the plank at seat height (0) or a step higher (1), on its two legs. Rows rise half a block apiece, so each
        # plank is the seat of one row and the footboard of the next.
        top = 8 if stage == 0 else 16
        e = [element([0, top - 2, 1], [16, top, 15], {'up': 'a', 'down': 'a', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'}),
             element([1, 0, 7], [2.5, top - 2, 9], {'all': 'g'}), element([13.5, 0, 7], [15, top - 2, 9], {'all': 'g'})]
        return model(e, t, 'alum', render=None)
    if stage == 2:
        # The frame under the stands: an upright and a brace every few rows.
        return model([element([7, 0, 7], [9, 16, 9], {'all': 'g'}),
                      element([7, 0, -3], [9, 1.5, 19], {'all': 'g'}, rotation={'angle': 45, 'axis': 'x', 'origin': [8, 8, 8]})], t, 'galv', render=None)
    if stage == 3:
        return model([element([0, 0, 14], [16, 1.5, 15.5], {'all': 'g'}), element([0, 12, 14], [16, 13.5, 15.5], {'all': 'g'}),
                      element([0, 0, 14], [1.5, 13.5, 15.5], {'all': 'g'}), element([14.5, 0, 14], [16, 13.5, 15.5], {'all': 'g'})], t, 'galv', render=None)
    if stage == 4:
        # A team bench at the sideline, aluminum on steel legs, its back toward the stands.
        e = [element([0, 6, 3], [16, 8, 11], {'up': 'a', 'down': 'a', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'}),
             element([0, 10, 11], [16, 14, 12.5], {'north': 'a', 'south': 'a', 'up': 'e', 'down': 'e', 'east': 'e', 'west': 'e'})]
        e += [element([x, 0, 4], [x + 1.5, 6, 5.5], {'all': 'g'}) for x in (1, 13.5)]
        e += [element([x, 0, 11], [x + 1.5, 14, 12.5], {'all': 'g'}) for x in (1, 13.5)]
        return model(e, t, 'alum', render=None)
    if stage == 5:
        return model([element([0, 0, 0], [16, 16, 2], {'north': 'w', 'south': 'w', 'east': 'e', 'west': 'e', 'up': 'e', 'down': 'e'},
                               uv={'east': [0, 0, 2, 16], 'west': [14, 0, 16, 16], 'up': [0, 0, 16, 2], 'down': [0, 14, 16, 16]})], t, 'press_glass', render=None)
    if stage == 6:
        # The press box's table under its windows, the microphone on its stand, a folded program.
        e = [element([0, 10, 0], [16, 12, 9], {'all': 'p'}), element([1, 0, 1], [2.5, 10, 2.5], {'all': 'g'}), element([13.5, 0, 1], [15, 10, 2.5], {'all': 'g'}),
             element([7.5, 12, 3], [8.5, 16, 4], {'all': 'g'}), element([7, 15, 2], [9, 17, 4], {'all': 'k'})]
        return model(e, t, 'plywood', render=None)
    # The horn on the press box's roof, aimed at the field.
    return model([element([7, 0, 7], [9, 6, 9], {'all': 'g'}), element([4, 6, 0], [12, 12, 10], {'north': 'h', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'})],
                 t, 'pa_horn', render=None)


def locker_model(stage):
    t = {'l': 'locker_lower', 'u': 'locker_upper', 'k': 'locker_side', 'b': 'bench_wood', 'g': 'galv', 'm': 'gov_metal', 'c': 'chalkboard',
         'r': 'chrome', 's': 'rollup', 'n': 'concession_menu', 'p': 'plywood', 'o': 'notebook'}
    if stage in (0, 1):
        f = 'l' if stage == 0 else 'u'
        return model([element([0, 0, 4], [16, 16, 16], {'north': f, 'south': 'k', 'east': 'k', 'west': 'k', 'up': 'k', 'down': 'k'},
                               uv={'east': [0, 0, 12, 16], 'west': [0, 0, 12, 16], 'up': [0, 0, 16, 12], 'down': [0, 0, 16, 12]})], t, t[f], render=None)
    if stage == 2:
        return model([element([0, 6, 4], [16, 8, 12], {'all': 'b'}), element([2, 0, 7], [3.5, 6, 9], {'all': 'g'}), element([12.5, 0, 7], [14, 6, 9], {'all': 'g'})], t, 'bench_wood', render=None)
    if stage == 3:
        # The coach's desk: grey steel, a clipboard and a whistle on its cord.
        e = [element([0, 0, 2], [16, 12, 14], {'all': 'm'}), element([3, 12, 4], [10, 12.5, 11], {'up': 'o', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
             element([11, 12, 6], [13, 13, 8], {'all': 'r'})]
        return model(e, t, 'gov_metal', render=None)
    if stage == 4:
        return model([element([0, 2, 15], [16, 14, 16], {'north': 'c', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})], t, 'chalkboard', render=None)
    if stage == 5:
        return model([element([7, 10, 13], [9, 11, 16], {'all': 'r'}), element([6, 8, 11], [10, 10, 14], {'all': 'r'}), element([7.5, 0, 15], [8.5, 10, 16], {'all': 'r'})], t, 'chrome', render=None)
    if stage == 6:
        return model([element([0, 0, 13], [16, 16, 16], {'north': 's', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'},
                               uv={'east': [0, 0, 3, 16], 'west': [13, 0, 16, 16], 'up': [0, 0, 16, 3], 'down': [0, 13, 16, 16]})], t, 'rollup', render=None)
    return model([element([1, 2, 15], [15, 14, 16], {'north': 'n', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'})], t, 'concession_menu', render=None)


def score_model(stage):
    t = {'g': 'galv', 't': 'ticket_window', 'k': 'tv_cabinet'}
    if stage <= 5:
        t['f'] = f'score_{stage}'
        # Its back in galvanized panels, so it reads from behind the end zone too.
        return model([element([0, 0, 12], [16, 16, 16], {'north': 'f', 'south': 'g', 'east': 'k', 'west': 'k', 'up': 'k', 'down': 'k'},
                               uv={'east': [0, 0, 4, 16], 'west': [12, 0, 16, 16], 'up': [0, 0, 16, 4], 'down': [0, 12, 16, 16]})], t, f'score_{stage}', render=None)
    if stage == 6:
        return model([element([5, 0, 5], [11, 16, 11], {'all': 'g'})], t, 'galv', render=None)
    return model([element([0, 0, 0], [16, 16, 16], {'north': 't', 'south': 'k', 'east': 'k', 'west': 'k', 'up': 'k', 'down': 'k'})], t, 'ticket_window', render=None)


def amber_model(stage):
    t = {'v': 'vcr', 'k': 'tv_cabinet', 'f': 'tv_front', 's': 'tv_screen', 'w': 'wood_top', 'r': 'rental_tapes', 'c': 'tape_case', 'n': 'notebook',
         'l': 'lampshade', 'b': 'lamp_base', 'p': 'phone_face', 'd': 'drawers', 'o': 'books', 'a': 'amber_window', 'm': 'remote', 'x': 'window_sill'}
    if stage == 0:
        # The television on its stand, the VCR on the shelf under it, the set on and showing nothing much.
        e = [element([1, 0, 3], [15, 6, 15], {'north': 'v', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
             element([2, 6, 4], [14, 16, 15], {'north': 'f', 'south': 'k', 'east': 'k', 'west': 'k', 'up': 'k', 'down': 'k'})]
        e += glow([element([3, 8, 3.9], [12, 15, 3.9], {'north': 's'}, uv={'north': [2, 2, 14, 14]})], 8)
        return model(e, t, 'tv_front', render=None)
    if stage == 1:
        return model([element([0, 0, 6], [16, 16, 16], {'north': 'r', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'},
                               uv={'east': [0, 0, 10, 16], 'west': [6, 0, 16, 16], 'up': [0, 0, 16, 10], 'down': [0, 6, 16, 16]})], t, 'rental_tapes', render=None)
    if stage == 2:
        # Tapes stacked on the carpet by the set, the top one out of its case.
        e = [element([3, h * 2, 4 + h % 2], [11, h * 2 + 2, 9 + h % 2], {'up': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}) for h in range(3)]
        e.append(element([4, 6, 5], [10, 7, 9], {'all': 'v'}))
        return model(e, t, 'tape_case', render=None)
    if stage == 3:
        # Her desk under the window: the notebook open, the lamp on.
        e = [element([0, 10, 4], [16, 12, 16], {'all': 'w'}), element([0, 0, 13], [2, 10, 15], {'all': 'w'}), element([14, 0, 13], [16, 10, 15], {'all': 'w'}),
             element([1, 0, 4], [15, 10, 6], {'north': 'd', 'south': 'w', 'east': 'w', 'west': 'w'}),
             element([3, 12, 6], [10, 12.5, 12], {'up': 'n', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}),
             element([12, 12, 10], [14, 18, 12], {'all': 'b'})]
        e += glow([element([10.5, 17, 8.5], [15.5, 22, 13.5], {'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l'})], 12)
        return model(e, t, 'notebook')
    if stage == 4:
        e = [element([2, 0, 2], [14, 11, 14], {'north': 'd', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}),
             element([4, 11, 5], [11, 14, 11], {'all': 'p'})]
        return model(e, t, 'phone_face', render=None)
    if stage == 5:
        return model([element([0, 0, 8], [16, 16, 16], {'north': 'o', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'},
                               uv={'east': [0, 0, 8, 16], 'west': [8, 0, 16, 16], 'up': [0, 0, 16, 8], 'down': [0, 8, 16, 16]})], t, 'books', render=None)
    if stage == 6:
        e = glow([element([0, 0, 0], [16, 16, 2], {'north': 'a', 'south': 'a', 'east': 'x', 'west': 'x', 'up': 'x', 'down': 'x'},
                          uv={'east': [0, 0, 2, 16], 'west': [14, 0, 16, 16], 'up': [0, 0, 16, 2], 'down': [0, 14, 16, 16]})], 8)
        return model(e, t, 'amber_window', render='cutout')
    # The coffee table: the remote, two tapes out of their cases.
    e = [element([1, 6, 3], [15, 7, 13], {'all': 'w'})]
    e += [element([x, 0, z], [x + 1, 6, z + 1], {'all': 'w'}) for x in (2, 13) for z in (4, 11)]
    e += [element([3, 7, 5], [7, 8, 9], {'all': 'v'}), element([8, 7, 6], [12, 8, 10], {'all': 'c'}), element([10, 7, 4], [13, 7.5, 5.5], {'all': 'm'})]
    return model(e, t, 'wood_top', render=None)


def lot_model(stage):
    t = {'h': 'sprinkler_head', 'w': 'spray', 'd': 'doghouse_wall', 'o': 'doghouse_door', 's': NS + 'block/list_shingle', 'c': 'can_galv',
         'b': 'bike', 'a': 'alum', 'n': 'webbing', 'g': 'grill_black', 'r': 'hose_green', 'q': 'galv', 'p': 'pool_out', 'i': 'pool_in'}
    if stage == 0:
        # A sprinkler on the lawn, its fan of water standing over it.
        e = [element([7, 0, 7], [9, 3, 9], {'all': 'h'})]
        e += [element([0, 1, 8], [16, 13, 8], {'north': 'w', 'south': 'w'}, rotation={'angle': a, 'axis': 'y', 'origin': [8, 8, 8]}) for a in (-45, 45)]
        return model(e, t, 'sprinkler_head', render='translucent')
    if stage == 1:
        # A doghouse, its door toward the yard, a shingle roof on it.
        e = [element([2, 0, 2], [14, 10, 14], {'north': 'o', 'south': 'd', 'east': 'd', 'west': 'd', 'down': 'd'}),
             element([1, 10, 1], [15, 12, 15], {'all': 's'}), element([3, 12, 1], [13, 14, 15], {'all': 's'}), element([5, 14, 1], [11, 15, 15], {'all': 's'})]
        return model(e, t, 'doghouse_wall', render=None)
    if stage == 2:
        return model([element([3, 0, 3], [13, 13, 13], {'all': 'c'}), element([2.5, 13, 2.5], [13.5, 14, 13.5], {'all': 'c'}), element([7, 14, 6], [9, 15, 10], {'all': 'q'})], t, 'can_galv', render=None)
    if stage == 3:
        return model([element([0, 0.5, 0], [16, 0.5, 16], {'up': 'b', 'down': 'b'}), element([7, 0, 3], [9, 2, 13], {'all': 'q'})], t, 'bike')
    if stage == 4:
        # A folding lawn chair, aluminum tubes and woven webbing, facing the street.
        e = [element([2, 5, 2], [14, 5.5, 10], {'up': 'n', 'down': 'n'}), element([2, 5, 10], [14, 14, 10.5], {'north': 'n', 'south': 'n'})]
        e += [element([x, 0, z], [x + 1, 5, z + 1], {'all': 'a'}) for x in (2, 13) for z in (2, 9)]
        e += [element([x, 5, 10], [x + 1, 14, 11], {'all': 'a'}) for x in (1.5, 13.5)]
        return model(e, t, 'webbing', render='cutout')
    if stage == 5:
        e = [element([4, 7, 4], [12, 13, 12], {'all': 'g'}), element([5, 13, 5], [11, 14, 11], {'all': 'g'}), element([7.5, 14, 7.5], [8.5, 15, 8.5], {'all': 'q'})]
        e += [element([x, 0, z], [x + 1, 7, z + 1], {'all': 'q'}) for x, z in ((5, 5), (10, 5), (7.5, 11))]
        return model(e, t, 'grill_black', render=None)
    if stage == 6:
        e = [element([2, 0, 5], [3, 10, 11], {'all': 'q'}), element([13, 0, 5], [14, 10, 11], {'all': 'q'}), element([3, 3, 4], [13, 11, 12], {'all': 'r'}),
             element([2, 10, 7.5], [14, 11, 8.5], {'all': 'q'})]
        return model(e, t, 'hose_green', render=None)
    # A kiddie pool with nothing in it but a few leaves and an inch of rainwater.
    e = [element([0, 0, 0], [16, 3, 16], {'up': 'i', 'down': 'p', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'})]
    return model(e, t, 'pool_out', render=None)


def school_models():
    m = {}
    for s in range(8):
        m[f'field_{s}'] = field_model(s); m[f'stands_{s}'] = stands_model(s); m[f'locker_{s}'] = locker_model(s)
        m[f'score_{s}'] = score_model(s); m[f'amber_{s}'] = amber_model(s); m[f'lot_{s}'] = lot_model(s)
    return m


def school_faces():
    faces = {'goal_paint': goal_paint(), 'goal_pad': goal_pad(), 'light_bank': light_bank(), 'alum': alum(0), 'alum_edge': alum(1),
             'press_glass': press_glass(), 'plywood': plywood(), 'pa_horn': pa_horn(), 'locker_lower': locker(0), 'locker_upper': locker(1),
             'locker_side': locker_side(), 'chalkboard': chalkboard(), 'concession_menu': concession_menu(), 'ticket_window': ticket_window(), 'door_steel_lower': door_steel(0),
             'door_steel_upper': door_steel(1), 'vcr': vcr(), 'rental_tapes': rental_tapes(), 'tape_case': tape_case(), 'notebook': notebook(),
             'amber_window': amber_window(), 'remote': remote(), 'sprinkler_head': sprinkler_head(), 'spray': spray(), 'doghouse_wall': doghouse(0),
             'doghouse_door': doghouse_door(), 'can_galv': can_galv(), 'bike': bike(), 'webbing': webbing(), 'grill_black': grill_black(),
             'hose_green': hose_green(), 'pool_out': pool_plastic(0), 'pool_in': pool_plastic(1)}
    board = scoreboard()
    for row in range(2):
        for col in range(3):
            faces[f'score_{row * 3 + col}'] = board.crop((col * 16, row * 16, col * 16 + 16, row * 16 + 16))
    return faces


# ------------------------------------------------------------------------------------------------ 0.4.701: the pasture
# South of the reader's back yard and across the cotton: a county road west off Avenue K, a cattle guard in the fence, a pasture of
# buffalo grass in barbed wire on cedar posts, a stock tank with its windmill, three pumpjacks on their pads and the lease's tank battery.
# Big pieces reach into the cells round them (models may run from -16 to 32), so their faces carry their own UVs, kept inside the
# sixteen pixels of their picture.


def span_uv(a0, a1):
    n = a1 - a0
    if n >= 16:
        return 0, 16
    s = a0 % 16
    if s + n > 16:
        s = 16 - n
    return s, s + n


def uv_for(face, lo, hi):
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    if face == 'north':
        u, v = span_uv(16 - x1, 16 - x0), span_uv(16 - y1, 16 - y0)
    elif face == 'south':
        u, v = span_uv(x0, x1), span_uv(16 - y1, 16 - y0)
    elif face == 'east':
        u, v = span_uv(16 - z1, 16 - z0), span_uv(16 - y1, 16 - y0)
    elif face == 'west':
        u, v = span_uv(z0, z1), span_uv(16 - y1, 16 - y0)
    elif face == 'up':
        u, v = span_uv(x0, x1), span_uv(z0, z1)
    else:
        u, v = span_uv(x0, x1), span_uv(16 - z1, 16 - z0)
    return [round(u[0], 3), round(v[0], 3), round(u[1], 3), round(v[1], 3)]


SIDES = ('north', 'south', 'east', 'west')
ALL = SIDES + ('up', 'down')


def part(lo, hi, tex, faces=ALL, rotation=None):
    """A box whose every face keeps to its picture, however far it reaches; `tex` is one key or a {face: key} map."""
    lo = [round(v, 3) for v in lo]; hi = [round(v, 3) for v in hi]
    t = tex if isinstance(tex, dict) else {f: tex for f in faces}
    t = {f: k for f, k in t.items() if f in faces}
    return element(lo, hi, t, uv={f: uv_for(f, lo, hi) for f in t}, rotation=rotation)


# ---------------------------------------------------------------- materials
def pasture():
    # Buffalo grass grazed short in July: fine curly blades gone half to straw, the dirt showing between them, a pale seed head or two.
    rnd = random.Random(9701); im = new('#8a8656', 9701, 6)
    for _ in range(80):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#9d9862', '#a8a06a', '#7a7a4c', '#6f7446', '#b3a873', '#8f8a5a']))
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(['#9a7a58', '#8c6c4c']))
    for x, y in [(4, 3), (11, 10), (2, 12)]:
        dot(im, x, y, '#c9bd8e'); dot(im, x, y + 1, '#b7aa7c')
    return im


def mud():
    # The ground round the tank where cattle stand to drink: trodden mud, dried to a crust at the edges, cloven prints pressed into it.
    rnd = random.Random(9702); im = new('#6e5a44', 9702, 5)
    speckle(im, rnd, 18, ['#5e4c39', '#7d6850', '#655240'])
    for x, y in [(3, 3), (10, 5), (5, 11), (12, 12)]:
        for dx, dy in [(0, 0), (0, 1), (2, 0), (2, 1)]:
            dot(im, x + dx, y + dy, '#4a3b2c')
        dot(im, x + 1, y + 2, '#57463a')
    speckle(im, rnd, 6, ['#8c7a62', '#857158'])
    return im


# ---------------------------------------------------------------- faces
def cedar():
    # A cedar fence post: grey stringy bark running up it in twisted strips, a split here and there, darker at the foot.
    rnd = random.Random(9711); im = new('#7b7065', 9711, 6)
    for x in range(16):
        k = (6, -4, 2, -8)[(x + (x // 5)) % 4]
        for y in range(16):
            darken(im, x, y, k + rnd.randint(-3, 3))
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(12)
        for i in range(rnd.randint(2, 4)):
            dot(im, x, y + i, '#4e463e')
    for x in range(16):
        dot(im, x, 15, '#5a5048'); dot(im, x, 14, '#625850')
    return im


def barbed():
    # Barbed wire: two strands twisted together, rusted; the barbs a little darker and sharp.
    im = new('#5e4f43', 9712, 6)
    for x in range(16):
        dot(im, x, 7 if x % 4 < 2 else 8, '#8a7462')
    return im


def guard_pipe():
    im = new('#9a9ea0', 9713, 4)
    for x in range(16):
        dot(im, x, 0, '#c4c7c8'); dot(im, x, 1, '#b3b6b8'); dot(im, x, 14, '#6d7072'); dot(im, x, 15, '#55585a')
    rnd = random.Random(9713)
    for _ in range(4):
        x = rnd.randrange(16)
        dot(im, x, 2, '#8a5c3a'); dot(im, x, 3, '#7a5032')
    return im


def guard_pit():
    rnd = random.Random(9714); im = new('#141210', 9714, 3)
    speckle(im, rnd, 10, ['#201c18', '#2a241e', '#0e0c0a'])
    return im


def stock_wall(rim):
    # A round galvanized stock tank: deep vertical corrugations, the zinc gone dull and spotted, a band of white scale along the
    # water line inside and a rust bloom low on the seams; the rolled rim brighter where hands and chins have worn it.
    rnd = random.Random(9715 + rim); im = new('#a6abad', 9715 + rim, 4)
    if rim:
        for x in range(16):
            for y in range(16):
                darken(im, x, y, (8, 2, -6, -12)[y % 4] if y < 8 else -4)
        speckle(im, rnd, 8, ['#c9cccd', '#d4d7d7', '#8e9294'])
        return im
    for x in range(16):
        k = (10, 4, -6, -12)[x % 4]
        for y in range(16):
            darken(im, x, y, k)
    for x in range(16):
        dot(im, x, 2, '#d8d6cc'); dot(im, x, 3, '#cbc8bc')
    for x in range(0, 16, 8):
        for y in range(9, 16):
            if rnd.random() < .7:
                dot(im, x + rnd.randint(0, 1), y, rnd.choice(['#8d5a37', '#9d683f', '#7a4c2f']))
    speckle(im, rnd, 8, ['#c5c9ca', '#8f9395'])
    return im


def salt():
    rnd = random.Random(9716); im = new('#d9d0bc', 9716, 5)
    for x, y in [(5, 5), (6, 5), (7, 6), (8, 6), (9, 7), (6, 7), (7, 7), (8, 8), (7, 8)]:
        dot(im, x, y, '#b8ac94')
    speckle(im, rnd, 8, ['#c7bda6', '#e8e1d2', '#a89a80'])
    return im


def tub():
    rnd = random.Random(9717); im = new('#232220', 9717, 3)
    for x in range(16):
        dot(im, x, 0, '#3a3834'); dot(im, x, 1, '#2e2c29')
    speckle(im, rnd, 6, ['#d9d0bc', '#31302d'])
    return im


def old_hay(top):
    # A round bale fed down to its last layers and trodden flat: hay gone grey-gold, stems every way, darker where it is wet underneath.
    rnd = random.Random(9718 + top); im = new('#a08c5a' if top else '#8a7648', 9718 + top, 7)
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16); c = rnd.choice(['#b9a46c', '#7c6a40', '#c4b07a', '#6e5e3a'])
        if rnd.random() < .5:
            dot(im, x, y, c); dot(im, (x + 1) % 16, y, c)
        else:
            dot(im, x, y, c); dot(im, x, (y + 1) % 16, c)
    return im


def tower_iron():
    # The windmill tower's angle iron, galvanized long ago and gone grey and pocked, bolt heads where the girts cross.
    rnd = random.Random(9719); im = new('#8b8f90', 9719, 5)
    for y in range(16):
        dot(im, 0, y, '#a9adae'); dot(im, 15, y, '#6e7273')
    speckle(im, rnd, 8, ['#7a5236', '#a2a6a7', '#6a6e70'])
    return im


def sail():
    # A sail of the wheel: galvanized sheet curved a little, its leading edge brighter, rust where the rivets hold it to the arms.
    im = new('#b3b7b8', 9720, 3)
    for y in range(16):
        dot(im, 0, y, '#d6d9da'); dot(im, 1, y, '#c8cbcc'); dot(im, 15, y, '#7d8183')
    for y in (3, 12):
        dot(im, 4, y, '#7a5236'); dot(im, 11, y, '#7a5236')
    return im


def head_case():
    # The windmill's head: the cast housing over its gears, oil weeping from the seam and dried to a crust.
    rnd = random.Random(9721); im = new('#7d8183', 9721, 4)
    for x in range(16):
        dot(im, x, 7, '#5e6264'); dot(im, x, 8, '#9a9ea0')
    for y in range(8, 16):
        if rnd.random() < .6:
            dot(im, 5, y, '#3e3a32'); dot(im, 6, y, '#4a463c')
    return im


def vane():
    # The tail vane: a broad galvanized sheet on its boom, two stripes of old paint across it, scratched through.
    rnd = random.Random(9722); im = new('#a9adae', 9722, 4)
    for x in range(16):
        dot(im, x, 0, '#7d8183'); dot(im, x, 15, '#7d8183')
        for y in (5, 6, 10, 11):
            dot(im, x, y, '#8a2a24' if rnd.random() > .12 else '#a9adae')
    return im


def pier():
    rnd = random.Random(9723); im = new('#a39e92', 9723, 5)
    speckle(im, rnd, 12, ['#8e897e', '#b5b0a4', '#7e796f'])
    return im


def pump_iron():
    im = new('#3d4a3e', 9724, 4)
    for y in range(16):
        dot(im, 0, y, '#56645a'); dot(im, 15, y, '#2c362e')
    return im


PJ = '#4f6d6a'


def pj_paint(worn):
    # A pumpjack's paint: an old oxide finish the sun has chalked, rust coming through on every edge and seam, oil run down from the
    # bearings; `worn` for the horsehead's face where the bridle has rubbed it to bare iron.
    rnd = random.Random(9725 + worn); im = new(PJ, 9725 + worn, 5)
    speckle(im, rnd, 14, ['#5d7c78', '#43605d', '#6a8682'])
    for _ in range(3 + worn * 4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for i in range(rnd.randint(2, 5)):
            dot(im, x, min(15, y + i), rnd.choice(['#7a4c2f', '#8d5a37', '#5e3c26']))
    if worn:
        for y in range(16):
            for x in (6, 7, 8, 9):
                dot(im, x, y, rnd.choice(['#5c5a56', '#6a6862', '#4e4c48']))
    for x in range(16):
        dot(im, x, 0, '#62807c')
    return im


def weight_iron():
    rnd = random.Random(9726); im = new('#3a3836', 9726, 4)
    for x in range(16):
        dot(im, x, 0, '#4c4a47'); dot(im, x, 15, '#2a2826')
    speckle(im, rnd, 10, ['#5a3a26', '#4a4642', '#2e2c2a'])
    write(im, 3, 6, '3', TALL, '#565350')
    return im


def cable():
    im = new('#3c3a36', 9727, 3)
    for y in range(16):
        dot(im, 0, y, '#5a5650'); dot(im, 1, (y + 1) % 16, '#4a4842')
    return im


def polished():
    im = new('#b8bcbd', 9728, 3)
    for y in range(16):
        dot(im, 0, y, '#e2e4e4'); dot(im, 1, y, '#d0d3d4'); dot(im, 15, y, '#7e8284')
    return im


def wellhead():
    # The wellhead's tee and valves, painted once and rusted since; a valve's handwheel stained with oil.
    rnd = random.Random(9729); im = new('#5f6b5c', 9729, 5)
    speckle(im, rnd, 14, ['#7a4c2f', '#2e2a24', '#6e7a6a'])
    return im


def motor_case():
    # The electric motor on the skid: a ribbed housing, its nameplate long gone dark.
    im = new('#3f4a52', 9730, 4)
    for x in range(0, 16, 2):
        for y in range(16):
            darken(im, x, y, -10)
    fill(im, 5, 6, 11, 10, '#2a2e30')
    return im


def mesh():
    # The belt guard's expanded metal: a diamond lattice, the belt's shadow behind it.
    im = new(None)
    for x in range(16):
        for y in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                dot(im, x, y, '#6a7072')
    return im


def skid():
    rnd = random.Random(9731); im = new('#47433e', 9731, 4)
    for x in range(16):
        dot(im, x, 0, '#5c5852'); dot(im, x, 15, '#36332f')
    speckle(im, rnd, 10, ['#6a4430', '#2c2a26'])
    return im


def tank_steel():
    # A tank battery's tanks: welded rings of plate painted a dull tan, a seam every few feet, rust run down from the thief hatch
    # and the walkway brackets.
    rnd = random.Random(9732); im = new('#b4a48a', 9732, 5)
    for x in range(16):
        dot(im, x, 7, '#9c8c72')
    for y in range(16):
        dot(im, 0, y, '#a29276')
    for _ in range(2):
        x = rnd.randrange(1, 16)
        for y in range(rnd.randrange(6, 10), 16):
            if rnd.random() < .55:
                dot(im, x, y, rnd.choice(['#a07450', '#9a6e4c', '#ad8260']))
    return im


def tank_top():
    rnd = random.Random(9733); im = new('#a39478', 9733, 5)
    for i in range(16):
        dot(im, i, i, '#8e8066'); dot(im, 15 - i, i, '#8e8066')
    speckle(im, rnd, 8, ['#7a4c2f', '#b9aa8e'])
    return im


def lease_sign():
    # The lease's sign at the cattle guard: black letters on white, three boards wide, a bullet hole through the last.
    im = new('#e6e2d8', 9734, 2, size=(48, 16))
    for x in range(48):
        dot(im, x, 0, '#2a2a28'); dot(im, x, 15, '#2a2a28')
    for y in range(16):
        dot(im, 0, y, '#2a2a28'); dot(im, 47, y, '#2a2a28')
    centre(im, 2, 'MASSEY', TALL, '#1e1e1c')
    centre(im, 9, 'DAWSON CO', TALL, '#1e1e1c')
    dot(im, 41, 4, '#1a1a18'); dot(im, 42, 4, '#3a3a36'); dot(im, 41, 5, '#3a3a36')
    return im


def well_plate(n):
    # A well's own plate on its post: its number, stencilled.
    im = new(None)
    fill(im, 0, 3, 16, 13, '#e6e2d8')
    for x in range(16):
        dot(im, x, 3, '#2a2a28'); dot(im, x, 12, '#2a2a28')
    for y in range(3, 13):
        dot(im, 0, y, '#2a2a28'); dot(im, 15, y, '#2a2a28')
    centre(im, 5, str(n), TALL, '#1e1e1c')
    dot(im, 3, 7, '#1e1e1c'); dot(im, 12, 7, '#1e1e1c')
    return im


# ---------------------------------------------------------------- models
STRANDS = (4, 9, 14, 19)


def wire_strands(z0=7.7, x0=0, x1=16):
    return [part([x0, y, z0], [x1, y + .6, z0 + .6], 'w', faces=('north', 'south', 'up', 'down')) for y in STRANDS]


def barbs(z0=7.7):
    e = []
    for i, y in enumerate(STRANDS):
        for x in (2 + (i % 2) * 2, 8 + (i % 2) * 2, 14 - (i % 2) * 2):
            e.append(part([x, y - .4, z0 - .4], [x + .6, y + 1, z0 + 1], 'w'))
    return e


def pasture_model(stage):
    t = {'c': 'cedar', 'w': 'barbed', 'p': 'guard_pipe', 'd': 'guard_pit', 's': 'salt', 'u': 'tub', 'h': 'old_hay', 'k': 'old_hay_top', 'g': 'galv'}
    if stage == 0:
        # A cedar post, crooked as cedar grows; the four strands run through it to the spans either side. The wire keeps to the middle
        # of the fence's row, so a run meets its corner however each is turned.
        e = [part([6, 0, 6], [10, 22, 10], 'c'), part([6.5, 17, 5.5], [9, 21, 6.5], 'c', faces=('north', 'east', 'west', 'up'))]
        return model(e + wire_strands(7.7, 0, 6) + wire_strands(7.7, 10, 16), t, 'cedar')
    if stage == 1:
        return model(wire_strands() + barbs(), t, 'barbed')
    if stage == 2:
        # A wire gap, shut: the strands on three thin stays, the near end's loop over the post's top.
        e = wire_strands() + [part([x, 1, 7.5], [x + 1, 21, 8.5], 'c') for x in (2, 8, 14)]
        e.append(part([14, 19.5, 7], [17, 21, 9], 'w'))
        return model(e, t, 'barbed')
    if stage == 3:
        # The gap laid back along the fence: its strands slack on the grass, the stays lying among them.
        e = [part([0, .2 + i * .3, 3 + i * 2.5], [16, .8 + i * .3, 3.6 + i * 2.5], 'w', faces=('north', 'south', 'up')) for i in range(4)]
        e += [part([x, 0, 2], [x + 1, 1, 13], 'c', faces=('up', 'east', 'west', 'north', 'south')) for x in (3, 9)]
        return model(e, t, 'barbed')
    if stage == 4:
        # A cattle guard, set where the ground was: pipes across the road over a dark pit, a channel at each side.
        e = [part([0, 0, 0], [16, 1, 16], 'd', faces=('up',))]
        e += [part([0, 12, z], [16, 15.5, z + 2.5], 'p', faces=('up', 'north', 'south', 'east', 'west')) for z in (.5, 4.5, 8.5, 12.5)]
        e += [part([x, 1, 0], [x + 1.5, 12, 16], 'g', faces=('up', 'east', 'west')) for x in (2, 12.5)]
        return model(e, t, 'guard_pipe')
    if stage == 5:
        # A corner post, heavier than the line's, braced by a rail to the next post along and a twisted wire from the rail's far end
        # down to the corner post's foot; the strands of both runs leave it.
        e = [part([5, 0, 5], [11, 24, 11], 'c'), part([11, 17, 7], [22, 19.5, 9.5], 'c'),
             part([16.1, -2, 7.6], [16.9, 22, 8.4], 'w', faces=SIDES, rotation={'angle': 45, 'axis': 'z', 'origin': [16.5, 10, 8]})]
        e += wire_strands(7.7, 11, 16)
        e += [part([7.7, y, 11], [8.3, y + .6, 16], 'w', faces=('east', 'west', 'up', 'down')) for y in STRANDS]
        return model(e, t, 'cedar')
    if stage == 6:
        # A mineral block in a rubber tub, licked to a hollow.
        e = [part([3, 0, 3], [13, 5, 13], 'u'), part([4.5, 4, 4.5], [11.5, 8, 11.5], 's')]
        return model(e, t, 'salt')
    # A hay ring: steel bars round what is left of a round bale, trodden flat.
    e = [part([-12, 0, -12], [28, 3, 28], {'up': 'k', 'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h'}),
         part([-6, 3, -4], [20, 6, 22], {'up': 'k', 'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h'}), part([0, 6, 2], [14, 8, 15], {'up': 'k', 'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h'})]
    r = 21
    for k in range(8):
        ang = k * 45
        a = math.radians(ang)
        cx, cz = 8 + r * math.cos(a), 8 + r * math.sin(a)
        e.append(part([cx - .7, 0, cz - .7], [cx + .7, 14, cz + .7], 'g', faces=SIDES))
    # Two rails round the bars, each in eight lengths from bar to bar: laid along a side of the square and turned an eighth about the
    # ring's middle into place.
    mid, half = r * .924, r * .383 + .4
    for y in (6, 13):
        for side in range(4):
            for turn in (-22.5, 22.5):
                if side == 0:
                    lo, hi = [8 + mid - .6, y, 8 - half], [8 + mid + .6, y + 1.2, 8 + half]
                elif side == 2:
                    lo, hi = [8 - mid - .6, y, 8 - half], [8 - mid + .6, y + 1.2, 8 + half]
                elif side == 1:
                    lo, hi = [8 - half, y, 8 + mid - .6], [8 + half, y + 1.2, 8 + mid + .6]
                else:
                    lo, hi = [8 - half, y, 8 - mid - .6], [8 + half, y + 1.2, 8 - mid + .6]
                e.append(part(lo, hi, 'g', faces=SIDES + ('up',), rotation={'angle': turn, 'axis': 'y', 'origin': [8, y, 8]}))
    return model(e, t, 'old_hay_top')


def stock_model(stage):
    t = {'w': 'stock_wall', 'r': 'stock_rim', 'g': 'galv', 'p': 'pump_iron'}
    wall_s = [part([0, 0, 14], [16, 15, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}), part([0, 15, 13], [16, 16.5, 16.5], 'r')]
    wall_e = [part([14, 0, 0], [16, 15, 16], {'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}), part([13, 15, 0], [16.5, 16.5, 16], 'r')]
    if stage == 0:
        return model(wall_s, t, 'stock_wall')
    if stage == 1:
        return model([wall_s[0], part([14, 0, 0], [16, 15, 14], {'north': 'w', 'east': 'w', 'west': 'w'}), part([0, 15, 13], [16.5, 16.5, 16.5], 'r'),
                      part([13, 15, 0], [16.5, 16.5, 13], 'r', faces=('up', 'down', 'east', 'west', 'north'))], t, 'stock_wall')
    if stage == 2:
        return model([part([14, 0, 14], [16, 15, 16], 'w'), part([13.5, 15, 13.5], [16.5, 16.5, 16.5], 'r')], t, 'stock_wall')
    if stage == 3:
        # The windmill's pipe comes in low, rises up the tank's side and over the rim, and its spout hangs over the water.
        e = list(wall_s)
        e += [part([7, 4, 0], [9, 6, 11], 'p'), part([6.5, 3.5, 9.5], [9.5, 6.5, 12.5], 'p'), part([7, 6, 10], [9, 19, 12], 'p'),
              part([7, 17, 12], [9, 19, 21], 'p'), part([6.5, 14, 19], [9.5, 19.5, 22], 'p')]
        return model(e, t, 'stock_wall')
    # The windmill's pipe along the ground, a coupling halfway.
    return model([part([7, 4, 0], [9, 6, 16], 'p'), part([6.5, 3.5, 7], [9.5, 6.5, 9], 'p'), part([7, 0, 7.5], [9, 4, 8.5], 'g', faces=SIDES)], t, 'pump_iron')


# The windmill's tower: four legs leaning in from 22 pixels out at the foot to 5 at the top, eleven blocks up; a girt round them at
# every section's top and an X of rods in each panel. Section k's block stands at y = 3k and draws world heights max(0, 3k-1) to 3k+2.
TOWER_TOP = 11


def leg_offset(y):
    return 22 - 17 * y / TOWER_TOP


def windmill_section(k):
    t = {'i': 'tower_iron', 'r': 'galv', 'p': 'pump_iron', 'c': 'pier'}
    yb = 3 * k
    y0, y1 = max(0, yb - 1), yb + 2
    e = []
    step = .25
    y = y0
    while y < y1 - 1e-6:
        d = leg_offset(y + step / 2)
        for sx, sz in ((-1, -1), (1, -1), (1, 1), (-1, 1)):
            cx, cz = 8 + sx * d, 8 + sz * d
            e.append(part([cx - 1, (y - yb) * 16, cz - 1], [cx + 1, (y + step - yb) * 16, cz + 1], 'i', faces=SIDES))
        y += step
    # The girt round the legs at the section's top, a little under it.
    gy = (y1 - yb) * 16 - 1.5
    d = leg_offset(y1 - .05)
    e += [part([8 - d, gy, 8 - d - .5], [8 + d, gy + 1.2, 8 - d + .5], 'i', faces=SIDES + ('up', 'down')),
          part([8 - d, gy, 8 + d - .5], [8 + d, gy + 1.2, 8 + d + .5], 'i', faces=SIDES + ('up', 'down')),
          part([8 - d - .5, gy, 8 - d], [8 - d + .5, gy + 1.2, 8 + d], 'i', faces=SIDES + ('up', 'down')),
          part([8 + d - .5, gy, 8 - d], [8 + d + .5, gy + 1.2, 8 + d], 'i', faces=SIDES + ('up', 'down'))]
    # An X of rods across each face of the panel, at the steepness nearest the panel's own.
    ym = (y0 + y1) / 2
    dm = leg_offset(ym)
    h = (y1 - y0) * 16
    w = 2 * dm
    cy = (ym - yb) * 16
    if h < w * 1.7:
        ang, length = 45, min(w, h) * math.sqrt(2)
    else:
        ang = 22.5
        length = min(h, w / math.tan(math.radians(22.5))) / math.cos(math.radians(22.5))
    length = min(length, 2 * min(cy + 16, 32 - cy))
    for face, (axis, fixed) in {'north': ('z', 8 - dm), 'south': ('z', 8 + dm), 'west': ('x', 8 - dm), 'east': ('x', 8 + dm)}.items():
        for sign in (1, -1):
            if axis == 'z':
                lo, hi = [8 - .35, cy - length / 2, fixed - .35], [8 + .35, cy + length / 2, fixed + .35]
                origin = [8, cy, fixed]
            else:
                lo, hi = [fixed - .35, cy - length / 2, 8 - .35], [fixed + .35, cy + length / 2, 8 + .35]
                origin = [fixed, cy, 8]
            e.append(part(lo, hi, 'r', faces=SIDES, rotation={'angle': sign * ang, 'axis': axis, 'origin': origin}))
    # The pump rod down the middle.
    e.append(part([7.6, max(-16, (y0 - yb) * 16), 7.6], [8.4, (y1 - yb) * 16, 8.4], 'r', faces=SIDES))
    if k == 0:
        # The pump on its stand pipe at the foot, its spout pipe low along the ground toward the tank.
        e += [part([6, 0, 6], [10, 12, 10], 'p'), part([5.5, 11, 5.5], [10.5, 13, 10.5], 'p'), part([7, 4, -16], [9, 6, 6], 'p'),
              part([5, -1, 5], [11, 1, 11], 'c', faces=SIDES + ('up',))]
    if k == 3:
        # The platform round the top, its boards on the last girt, under the head.
        e += [part([-2, 30, -2], [18, 32, 18], {'up': 'r', 'down': 'r', 'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i'})]
    return model(e, t, 'tower_iron')


def windmill_head():
    # The head on its mast: the gear housing, the shaft boss to the front where the wheel goes on, the tail's root behind.
    t = {'h': 'head_case', 'r': 'galv', 'i': 'tower_iron'}
    e = [part([7, -16, 7], [9, 2, 9], 'r', faces=SIDES), part([5, -14, 5], [11, -12, 11], 'i'),
         part([4, 2, 2], [12, 13, 14], 'h'), part([6, 6, -1], [10, 10, 2], 'h'), part([7, 5, 13], [9, 9, 16], 'r')]
    return model(e, t, 'head_case')


def windmill_wheel():
    """The wheel, its hub at (8, 8, -2): sixteen sails between an inner and an outer ring, drawn turned about its hub by the renderer."""
    t = {'b': 'sail', 'r': 'galv', 'h': 'head_case'}
    e = [part([5, 5, -4.5], [11, 11, .5], 'h')]
    for k in range(16):
        theta = k * 22.5
        raw = 90 * int((theta + 44.9) // 90)
        rot, base = theta - raw, raw % 360
        z0 = -3.4 if k % 2 == 0 else -2.6
        # A sail from radius 6 to 23, four pixels wide, laid along `base` and turned the rest of the way about the hub.
        if base == 0:
            lo, hi = [14, 6, z0], [31, 10, z0 + .6]
        elif base == 180:
            lo, hi = [-15, 6, z0], [2, 10, z0 + .6]
        elif base == 90:
            lo, hi = [6, 14, z0], [10, 31, z0 + .6]
        else:
            lo, hi = [6, -15, z0], [10, 2, z0 + .6]
        r = {'angle': rot, 'axis': 'z', 'origin': [8, 8, -2]} if rot else None
        e.append(part(lo, hi, 'b', faces=('north', 'south', 'up', 'down', 'east', 'west'), rotation=r))
        for rad, thick in ((23.2, .8), (13.5, .7)):
            seg = 2 * math.pi * rad / 16 * .56
            if base == 0:
                lo, hi = [8 + rad - thick / 2, 8 - seg, -2.4], [8 + rad + thick / 2, 8 + seg, -1.6]
            elif base == 180:
                lo, hi = [8 - rad - thick / 2, 8 - seg, -2.4], [8 - rad + thick / 2, 8 + seg, -1.6]
            elif base == 90:
                lo, hi = [8 - seg, 8 + rad - thick / 2, -2.4], [8 + seg, 8 + rad + thick / 2, -1.6]
            else:
                lo, hi = [8 - seg, 8 - rad - thick / 2, -2.4], [8 + seg, 8 - rad + thick / 2, -1.6]
            lo = [max(-16, v) for v in lo]; hi = [min(32, v) for v in hi]
            e.append(part(lo, hi, 'r', faces=SIDES + ('up', 'down'), rotation=r))
    return model(e, t, 'sail')


def windmill_model(stage):
    if stage <= 3:
        return windmill_section(stage)
    if stage == 4:
        return windmill_head()
    if stage == 5:
        return windmill_wheel()
    t = {'c': 'pier', 'r': 'galv', 'v': 'vane'}
    if stage == 6:
        # A footing at the tower's corner: a concrete pier, the leg's foot bolted to it.
        return model([part([0, 0, 0], [6, 4, 6], 'c'), part([1, 4, 1], [3.5, 5, 3.5], 'r')], t, 'pier')
    # The tail behind the head: a boom out to the vane, the vane's sheet on edge, a brace wire to the boom's root.
    e = [part([7.2, 5.5, -12], [8.8, 7.5, 20], 'r'), part([7.5, -6, 10], [8.5, 18, 30], {'east': 'v', 'west': 'v', 'north': 'r', 'south': 'r', 'up': 'r', 'down': 'r'}),
         part([7.8, 7, -6], [8.2, 18.5, -5.6], 'r', faces=SIDES, rotation={'angle': 45, 'axis': 'x', 'origin': [8, 12, 2]})]
    return model(e, t, 'vane')


# The pumpjack, laid out about its Samson post A with the horsehead toward the facing: the wellhead three blocks in front, the gearbox
# and crank two behind, the motor three behind. The walking beam's pivot is 3.25 blocks up; the machine block above the post (at y 2)
# keeps the turning parts (TheListMachines), which its renderer draws from the models below.
def pumpjack_model(stage):
    t = {'p': 'pj_paint', 'q': 'pj_worn', 'k': 'skid', 'n': 'weight_iron', 'c': 'cable', 'o': 'polished', 'w': 'wellhead', 'm': 'motor_case',
         'x': 'mesh', 'g': 'galv'}
    if stage == 0:
        # The wellhead: the casing's flange, the tee, a valve and its handwheel, the flowline off to the side, the stuffing box on top.
        e = [part([5, 0, 5], [11, 3, 11], 'w'), part([6, 3, 6], [10, 10, 10], 'w'), part([10, 5.5, 7], [16, 7.5, 9], 'w'),
             part([11, 8, 6], [13, 10, 10], 'w'), part([11.5, 10, 7.5], [12.5, 11, 8.5], 'w'),
             part([7, 10, 7], [9, 13, 9], 'g'), part([6.5, 12, 6.5], [9.5, 13, 9.5], 'w')]
        return model(e, t, 'wellhead')
    if stage == 1:
        # The Samson post's lower half: two I-beam legs standing, two braces leaning back from the skid to them, the skid under all.
        # Each brace's foot is on the skid 11 pixels behind its leg, so that at the steepest lean the game allows its top meets the leg.
        e = []
        for x0 in (1, 12):
            e.append(part([x0, 0, 6], [x0 + 3, 32, 10], 'p'))
            e.append(part([x0 + .5, 0, 20], [x0 + 2.5, 32, 22], 'p', faces=SIDES, rotation={'angle': -22.5, 'axis': 'x', 'origin': [x0 + 1.5, 0, 21]}))
            e.append(part([x0 - 1, 0, -16], [x0 + 4, 3, 32], 'k', faces=SIDES + ('up',)))
        e += [part([4, 11, 7], [12, 13, 9], 'p'), part([4, 23, 7], [12, 25, 9], 'p')]
        return model(e, t, 'pj_paint')
    if stage == 2:
        # The walking beam's front half and the horsehead, about a pivot at (8, 8, 32): the beam an I in section, the head's face an arc
        # three blocks round from the pivot, from 25 degrees under the beam to 12 over it.
        e = [part([6, 7, -6], [10, 13, 32], 'p'), part([5, 12.5, -6], [11, 13.5, 32], 'p'), part([5, 6.5, -6], [11, 7.5, 32], 'p')]
        a = -25.0
        while a < 12 - 1e-6:
            b = min(12.0, a + 4.6)
            m = math.radians((a + b) / 2)
            zf, zb = 32 - 48 * math.cos(m), 32 - 39 * math.cos(m)
            e.append(part([4, 8 + 48 * math.sin(math.radians(a)), max(-16, zf)], [12, 8 + 48 * math.sin(math.radians(b)), zb],
                          {'north': 'q', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p', 'south': 'p'}))
            a = b
        e.append(part([5, 2, -9], [11, 12, -5], 'p'))
        return model(e, t, 'pj_paint')
    if stage == 3:
        # The beam's back half about a pivot at (8, 8, -16), to the equalizer under its end: the cross bar the two pitman arms hang from.
        e = [part([6, 7, -16], [10, 13, 16], 'p'), part([5, 12.5, -16], [11, 13.5, 16], 'p'), part([5, 6.5, -16], [11, 7.5, 16], 'p'),
             part([5, 3, 13], [11, 7, 19], 'p'), part([-2, 4.1, 14.5], [18, 7.1, 17.5], 'p'),
             part([-1.6, 4.4, 14.8], [1.6, 6.8, 17.2], 'n'), part([14.4, 4.4, 14.8], [17.6, 6.8, 17.2], 'n')]
        return model(e, t, 'pj_paint')
    if stage == 4:
        # The cranks and their counterweights about the gearbox's shaft at (8, 8, 8): an arm each side, its pin at the top when the
        # crank is at nought, the weights hanging opposite.
        e = [part([3, 7, 7], [13, 9, 9], 'g')]
        for x0, x1, px0, px1 in ((.5, 3, -1.5, .6), (13, 15.5, 15.4, 17.5)):
            e += [part([x0, -2, 5.5], [x1, 18, 10.5], 'p'), part([px0, 14.8, 6.8], [px1, 17.2, 9.2], 'g'),
                  part([x0 - .5, -10, 0], [x1 + .5, 1, 16], 'n'), part([x0 - .2, -12, 3], [x1 + .2, -10, 13], 'n')]
        return model(e, t, 'weight_iron')
    if stage == 5:
        # The two pitman arms, each from its crank pin at the foot (8, 0, 8 is their axis) up to the equalizer, 1.92 blocks long.
        e = []
        for cx in (0, 16):
            e += [part([cx - .9, 0, 7.1], [cx + .9, 30.7, 8.9], 'p'), part([cx - 1.4, -1.4, 6.6], [cx + 1.4, 1.4, 9.4], 'n'),
                  part([cx - 1.4, 29.3, 6.6], [cx + 1.4, 32, 9.4], 'n')]
        return model(e, t, 'pj_paint')
    if stage == 6:
        # The carrier bar on the polished rod, the rod clamp over it; the rod runs down 45 pixels into the stuffing box and the well.
        e = [part([7.4, -16, 7.4], [8.6, 30, 8.6], 'o', faces=SIDES), part([4, 27, 7], [12, 29, 9], 'n'), part([7, 29, 7], [9, 31, 9], 'n')]
        return model(e, t, 'polished')
    # The gearbox on the skid with the crank's shaft through it, the belt guard, and the motor behind on its own base.
    e = [part([3.5, 3, 2], [12.5, 22, 14], 'p'), part([4, 16, 3], [12, 23.5, 13], 'p'), part([0, 0, -16], [3, 3, 32], 'k', faces=SIDES + ('up',)),
         part([13, 0, -16], [16, 3, 32], 'k', faces=SIDES + ('up',)), part([3, 0, 0], [13, 3, 30], 'k', faces=('up',)),
         part([5, 3, 19], [12, 13, 30], 'm'), part([4, 5, 30], [13, 11, 31.5], 'm'), part([12, 7, 24], [17.5, 9, 26], 'g'),
         part([12.5, 13, 7], [17.5, 15, 9], 'g'),
         part([17.5, 4, 5], [20, 19, 28], {'east': 'x', 'west': 'p', 'north': 'p', 'south': 'p', 'up': 'p', 'down': 'p'})]
    return model(e, t, 'pj_paint')


def pumpjack_saddle():
    # Above the Samson post's legs: their tops, the saddle across them and the pillow blocks the beam's pivot turns in.
    t = {'p': 'pj_paint', 'n': 'weight_iron'}
    e = [part([1, 0, 6], [4, 16, 10], 'p'), part([12, 0, 6], [15, 16, 10], 'p'), part([0, 16, 5], [16, 18, 11], 'p'),
         part([3, 18, 6], [6, 22, 10], 'n'), part([10, 18, 6], [13, 22, 10], 'n')]
    return model(e, t, 'pj_paint')


def pumpjack_cable():
    """The bridle's two wire ropes, one block tall, stretched by the renderer from the carrier bar up to the horsehead."""
    t = {'c': 'cable'}
    return model([part([5.2, 0, 7.6], [6, 16, 8.4], 'c', faces=SIDES), part([10, 0, 7.6], [10.8, 16, 8.4], 'c', faces=SIDES)], t, 'cable')


def battery_model(stage):
    t = {'s': 'tank_steel', 'r': 'tank_top', 'g': 'galv', 'l': 'ladder', 'k': 'skid', 'c': 'cedar', 'b': 'lease_sign', 'n': 'well_plate_1'}
    if stage in (0, 1):
        # A tank three blocks across, stood on the ground (0) or its upper half with the roof and the walkway rail (1), rounded from
        # boxes of plate.
        lo, hi = (0, 32) if stage == 0 else (-16, 24)
        e = [part([8 - hx, lo, 8 - hz], [8 + hx, hi, 8 + hz], 's', faces=SIDES) for hx, hz in ((22, 8), (8, 22), (19, 15), (15, 19))]
        e.append(part([-15, lo, 6], [-13.6, hi, 10], 'l', faces=('west', 'east')))
        if stage == 1:
            e += [part([-11, 24, -11], [27, 26, 27], 'r', faces=('up', 'down') + SIDES), part([-6, 26, -6], [22, 28, 22], 'r', faces=('up',) + SIDES),
                  part([2, 28, 2], [14, 29, 14], 'r', faces=('up',) + SIDES), part([6, 29, 6], [10, 31, 10], 'g')]
            e += [part([-15, 26, -2], [-14, 31, 18], 'g', faces=SIDES + ('up',)), part([-15, 24, 6], [-13.6, 30, 10], 'l', faces=('west', 'east'))]
        return model(e, t, 'tank_steel')
    if stage == 2:
        # The separator: a vessel lying on two saddles, its gauge and the pipes off its ends.
        e = [part([-12, 4, 2], [28, 16, 14], 's'), part([-14, 6, 4], [-12, 14, 12], 'r'), part([28, 6, 4], [30, 14, 12], 'r'),
             part([-6, 0, 3], [-2, 5, 13], 'k'), part([18, 0, 3], [22, 5, 13], 'k'), part([7, 16, 7], [9, 20, 9], 'g'),
             part([30, 9, 7], [32, 11, 9], 'g')]
        return model(e, t, 'tank_steel')
    if stage == 3:
        return model([], t, 'tank_steel')
    if stage == 4:
        # The lease's sign on two posts, three boards wide.
        e = [part([-13, 0, 7], [-11, 26, 9], 'c'), part([27, 0, 7], [29, 26, 9], 'c'),
             element([-16, 10, 6], [32, 26, 7], {'north': 'b', 'south': 'c', 'up': 'c', 'down': 'c', 'east': 'c', 'west': 'c'},
                     uv={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16], 'up': [0, 0, 16, 1], 'down': [0, 0, 16, 1], 'east': [0, 0, 1, 16], 'west': [0, 0, 1, 16]})]
        return model(e, {**t, 'b': 'lease_sign'}, 'lease_sign')
    # A well's number on a short post by its wellhead.
    n = stage - 4
    e = [part([7, 0, 9], [9, 13, 11], 'c'), part([0, 10, 8], [16, 20, 9], {'north': 'n', 'south': 'c', 'up': 'c', 'down': 'c', 'east': 'c', 'west': 'c'})]
    e[1]['faces']['north']['uv'] = [0, 3, 16, 13]
    return model(e, {**t, 'n': f'well_plate_{n}'}, f'well_plate_{n}')


def pasture_models():
    m = {}
    for s in range(8):
        m[f'pasture_{s}'] = pasture_model(s); m[f'windmill_{s}'] = windmill_model(s); m[f'pumpjack_{s}'] = pumpjack_model(s)
        m[f'battery_{s}'] = battery_model(s)
    for s in range(5):
        m[f'stock_{s}'] = stock_model(s)
    m['pumpjack_saddle'] = pumpjack_saddle(); m['pumpjack_cable'] = pumpjack_cable()
    return m


def pasture_faces():
    faces = {'cedar': cedar(), 'barbed': barbed(), 'guard_pipe': guard_pipe(), 'guard_pit': guard_pit(), 'stock_wall': stock_wall(0),
             'stock_rim': stock_wall(1), 'salt': salt(), 'tub': tub(), 'old_hay': old_hay(0), 'old_hay_top': old_hay(1),
             'tower_iron': tower_iron(), 'sail': sail(), 'head_case': head_case(), 'vane': vane(), 'pier': pier(), 'pump_iron': pump_iron(),
             'pj_paint': pj_paint(0), 'pj_worn': pj_paint(1), 'weight_iron': weight_iron(), 'cable': cable(), 'polished': polished(),
             'wellhead': wellhead(), 'motor_case': motor_case(), 'mesh': mesh(), 'skid': skid(), 'tank_steel': tank_steel(),
             'tank_top': tank_top()}
    faces['lease_sign'] = lease_sign()
    for n in (1, 2, 3):
        faces[f'well_plate_{n}'] = well_plate(n)
    return faces


# ------------------------------------------------------------------------------------------------ the whole set
# ------------------------------------------------------------------------------------------------ 0.4.706: night one
# The party at the stock tank and the seed house shut: a portable radio, an ice chest, empty cans, a camp lantern, a blanket on the grass,
# an empty case; then the lamp over the seed house's door and the bulb inside it, both gone dark.
def boombox_side():
    # The radio's ends, black plastic with a molded grip and the jack panel.
    im = new('#2c2d30', 9706, 3)
    for y in range(3, 13):
        dot(im, 7, y, '#3e3f43'); dot(im, 8, y, '#1e1f22')
    fill(im, 2, 11, 5, 13, '#46484c'); dot(im, 3, 12, '#111214'); dot(im, 4, 12, '#111214')
    return im


def ice_chest(top):
    # A picnic ice chest: the lid pale with its molded grip, the body a faded blue, a drain plug at the foot, scuffed at the corners.
    if top:
        im = new('#e4e2da', 9707, 2)
        fill(im, 4, 6, 11, 9, '#cfccc2'); fill(im, 5, 7, 10, 8, '#b8b4a8')
        for i in range(16):
            dot(im, i, 0, '#f2f0ea'); dot(im, i, 15, '#bcb8ac')
        return im
    im = new('#4a6e9a', 9708, 4)
    for x in range(16):
        dot(im, x, 0, '#e4e2da'); dot(im, x, 1, '#d2cfc4'); dot(im, x, 2, '#3a5a80')
    fill(im, 1, 4, 3, 6, '#3a5a80'); fill(im, 12, 4, 14, 6, '#3a5a80')
    fill(im, 2, 12, 3, 13, '#d8d6cc'); dot(im, 2, 12, '#9a9890')
    for (x, y) in [(0, 15), (15, 15), (0, 3), (15, 3), (7, 15), (8, 14)]:
        dot(im, x, y, '#6a86a8')
    return im


def beer_can():
    # A can's side, standing: a silver top band, the label's broad band and a stripe, dented on one side.
    im = new('#b8bcc0', 9709, 2)
    fill(im, 0, 3, 15, 11, '#c8a24a'); fill(im, 0, 6, 15, 7, '#9a2e2a')
    for x in range(16):
        dot(im, x, 2, '#e2e4e6'); dot(im, x, 12, '#8a8e92')
    for y in range(4, 10):
        dot(im, 11, y, '#a88838')
    return im


def can_top():
    im = new('#c4c8cc', 9710, 2)
    for x in range(16):
        dot(im, x, 0, '#e0e2e4'); dot(im, x, 15, '#9a9ea2')
    fill(im, 6, 4, 9, 6, '#8c9094'); dot(im, 7, 9, '#5a5e62')
    return im


def lantern(lit):
    # The camp lantern: its globe lit by the mantle, a glare at the middle; or its painted steel.
    if lit:
        im = new('#ffe6a4', 9711, 2)
        fill(im, 5, 5, 10, 10, '#fff6dc'); fill(im, 6, 6, 9, 9, '#ffffff')
        for x in range(16):
            dot(im, x, 0, '#d8b870'); dot(im, x, 15, '#d8b870')
        for y in range(16):
            dot(im, 3, y, '#e8c880'); dot(im, 12, y, '#e8c880')
        return im
    im = new('#3c5a3e', 9712, 3)
    for x in range(16):
        dot(im, x, 0, '#56785a'); dot(im, x, 15, '#22341f')
    dot(im, 4, 6, '#8a8e86'); dot(im, 11, 6, '#8a8e86')
    return im


def blanket():
    # A wool blanket spread on the grass: broad bands across it, narrow stripes between, the weave showing, a corner turned under.
    im = new('#7a3a2e', 9713, 3)
    bands = ['#7a3a2e', '#7a3a2e', '#d8c49a', '#2e4a6a', '#2e4a6a', '#2e4a6a', '#d8c49a', '#c87a32', '#c87a32', '#d8c49a', '#3e5a3a',
             '#3e5a3a', '#3e5a3a', '#d8c49a', '#7a3a2e', '#7a3a2e']
    rnd = random.Random(9713)
    for y in range(16):
        for x in range(16):
            c = bands[y]
            dot(im, x, y, shade(c, -10 if (x + y) % 2 else 0))
            if rnd.random() < .06:
                dot(im, x, y, shade(c, -26))
    return im


def case_box(top):
    # A cardboard case of beer cans, torn open along its top and empty: the printed band round its sides, a twelve on the end.
    if top:
        im = new('#2a2218', 9714, 2)
        for x in range(16):
            dot(im, x, 0, '#b08a5a'); dot(im, x, 15, '#b08a5a')
        for y in range(16):
            dot(im, 0, y, '#b08a5a'); dot(im, 15, y, '#b08a5a')
        for (x, y) in [(2, 1), (3, 1), (4, 2), (11, 1), (12, 2), (13, 1)]:
            dot(im, x, y, '#c8a070')
        return im
    im = new('#b08a5a', 9715, 3)
    fill(im, 0, 5, 15, 9, '#9a2e2a'); fill(im, 0, 6, 15, 6, '#c8a24a')
    for (x, y) in [(6, 7), (7, 7), (8, 7), (9, 8), (7, 8), (8, 8)]:
        dot(im, x, y, '#e8dcc0')
    for x in range(16):
        dot(im, x, 0, '#c8a070')
    for (x, y) in [(3, 0), (4, 1), (11, 0), (12, 1)]:
        dot(im, x, y, '#7a5a3a')
    return im


def bulb_dark():
    # A bulb with its filament gone dark: grey glass and the dull coil behind it.
    im = new('#5a5a52', 9716, 2)
    fill(im, 6, 6, 9, 9, '#4a4a44'); dot(im, 7, 7, '#2e2e2a'); dot(im, 8, 8, '#2e2e2a')
    return im


def party_model(stage):
    t = {'f': 'boombox', 's': 'boombox_side', 'i': 'ice_chest_side', 'l': 'ice_chest_top', 'c': 'beer_can', 'p': 'can_top', 'g': 'lantern_glass',
         'm': 'lantern_metal', 'b': 'blanket', 'x': 'case_box', 'o': 'case_top', 'h': 'barn_shade', 'd': 'bulb_dark', 'q': 'galv'}
    if stage == 0:
        # A portable radio standing on the grass, its handle up and its antenna out.
        e = [element([2, 0, 6], [14, 8, 10], {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}),
             element([3, 8, 7.5], [4, 10, 8.5], {'all': 's'}), element([12, 8, 7.5], [13, 10, 8.5], {'all': 's'}), element([3, 10, 7.5], [13, 11, 8.5], {'all': 's'}),
             element([12.5, 8, 8.75], [13, 16, 9.25], {'all': 'q'}, rotation={'angle': -22.5, 'axis': 'z', 'origin': [12.75, 8, 9]})]
        return model(e, t, 'boombox', render=None)
    if stage == 1:
        e = [element([2, 0, 4], [14, 7, 12], {'north': 'i', 'south': 'i', 'east': 'i', 'west': 'i', 'down': 'i', 'up': 'l'}),
             element([1.5, 7, 3.5], [14.5, 9, 12.5], {'up': 'l', 'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'down': 'l'})]
        return model(e, t, 'ice_chest_side', render=None)
    if stage == 2:
        # Empty cans: two standing, one on its side, one stepped flat.
        can = {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'p', 'down': 'p'}
        e = [element([3, 0, 3], [5, 4, 5], can), element([11, 0, 10], [13, 4, 12], can),
             element([7, 0, 6], [11, 2, 8], {'north': 'p', 'south': 'p', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}, rotation={'angle': 22.5, 'axis': 'y', 'origin': [9, 1, 7]}),
             element([3, 0, 11], [6, 0.75, 14], {'all': 'c'})]
        return model(e, t, 'beer_can', render=None)
    if stage == 3:
        e = [element([5, 0, 5], [11, 2, 11], {'all': 'm'}), element([5, 8, 5], [11, 9.5, 11], {'all': 'm'}), element([7.5, 9.5, 7.5], [8.5, 11, 8.5], {'all': 'q'}),
             element([7, 11, 5.5], [9, 11.75, 10.5], {'all': 'q'})]
        e += glow([element([5.5, 2, 5.5], [10.5, 8, 10.5], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'})], 14)
        return model(e, t, 'lantern_glass', render=None)
    if stage == 4:
        e = [element([0, 0, 0], [16, 0.5, 16], {'up': 'b', 'down': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}),
             element([0, 0.5, 12], [16, 1.25, 16], {'up': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})]
        return model(e, t, 'blanket', render=None)
    if stage == 5:
        e = [element([3, 0, 4], [13, 5, 12], {'north': 'x', 'south': 'x', 'east': 'x', 'west': 'x', 'down': 'x', 'up': 'o'}),
             element([3, 5, 4], [13, 8, 4], {'north': 'x', 'south': 'x'}, rotation={'angle': -22.5, 'axis': 'x', 'origin': [8, 5, 4]})]
        return model(e, t, 'case_box', render=None)
    if stage == 6:
        # The lamp over the seed house's door, gone dark.
        e = [element([7.5, 12, 11], [8.5, 13, 16], {'all': 'q'}), element([4, 9, 8], [12, 12, 14], {'north': 'h', 'south': 'h', 'east': 'h', 'west': 'h', 'up': 'h'}),
             element([5.5, 8.5, 9.5], [10.5, 9, 12.5], {'down': 'd'})]
        return model(e, t, 'barn_shade')
    # The bare bulb inside the seed house, gone dark.
    e = [element([7, 14, 7], [9, 16, 9], {'all': 'q'}), element([6.5, 11, 6.5], [9.5, 14, 9.5], {'all': 'd'})]
    return model(e, t, 'bulb_dark')


def party_faces():
    return {'boombox_side': boombox_side(), 'ice_chest_side': ice_chest(0), 'ice_chest_top': ice_chest(1), 'beer_can': beer_can(),
            'can_top': can_top(), 'lantern_glass': lantern(1), 'lantern_metal': lantern(0), 'blanket': blanket(), 'case_box': case_box(0),
            'case_top': case_box(1), 'bulb_dark': bulb_dark()}


def fitting_models():
    m = {}
    for s in range(4):
        m[f'cotton_{s}'] = cotton_model(s)
    for s in range(5):
        m[f'mesquite_{s}'] = mesquite_model(s); m[f'weed_{s}'] = weed_model(s)
    for s in range(7):
        m[f'pole_{s}'] = pole_model(s)
    for s in range(4):
        m[f'fence_{s}'] = fence_model(s)
    for s in range(6):
        m[f'signpost_{s}'] = signpost_model(s)
    m['track_0'] = track_model(); m['mailbox_0'] = mailbox_model()
    for s in range(6):
        m[f'window_{s}'] = window_model(s); m[f'poster_{s}'] = poster_model(s); m[f'den_{s}'] = den_model(s); m[f'front_{s}'] = front_model(s)
    for s in range(8):
        m[f'kitchen_{s}'] = kitchen_model(s); m[f'yard_{s}'] = yard_model(s); m[f'drug_{s}'] = drug_model(s); m[f'video_{s}'] = video_model(s)
        m[f'law_{s}'] = law_model(s); m[f'feed_{s}'] = feed_model(s); m[f'drivein_{s}'] = drivein_model(s); m[f'gin_{s}'] = gin_model(s)
        m[f'seed_{s}'] = seed_model(s); m[f'sign_drivein_{s}'] = signdrivein_model(s)
    for s in range(2):
        m[f'table_{s}'] = table_model(s); m[f'bed_{s}'] = bed_model(s)
    for s in range(5):
        m[f'room_{s}'] = room_model(s); m[f'bath_{s}'] = bath_model(s); m[f'fixture_{s}'] = fixture_model(s); m[f'sign_video_{s}'] = board_model(f'sign_video_{s}', 12)
    for s in range(4):
        m[f'rooftop_{s}'] = rooftop_model(s); m[f'shop_{s}'] = shop_model(s); m[f'bale_{s}'] = bale_model(s)
        m[f'sign_drug_{s}'] = board_model(f'sign_drug_{s}'); m[f'sign_feed_{s}'] = board_model(f'sign_feed_{s}')
    for s in range(4):
        m[f'sign_law_{s}'] = board_model(f'sign_law_{s}')
    for s in range(7):
        m[f'tower_{s}'] = tower_model(s); m[f'tank_{s}'] = tank_model(s)
    m.update(school_models())
    m.update(pasture_models())
    for st in range(8):
        m[f'party_{st}'] = party_model(st)
    for st in range(4):
        m[f'blood_{st}'] = stain_model(st)
    return m


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/list_{name}.png'] = im
    faces = {
        'mesquite_bark': mesquite_bark(), 'mesquite_end': mesquite_end(), 'mesquite_leaves': mesquite_leaves(0), 'mesquite_leaves_thin': mesquite_leaves(1),
        'tumbleweed': tumbleweed(), 'weed_1': weed(1), 'weed_2': weed(2), 'goathead': goathead(), 'pear_pad': pear_pad(),
        'pole_wood': pole_wood(), 'crossarm': crossarm(), 'insulator': insulator(), 'line_wire': wire_tex(), 'galv': galv(), 'lamp_head': lamp_head(),
        'lamp_lens': lamp_lens(), 'transformer': transformer(), 'chainlink': chainlink(), 'stop_sign': stop_sign(), 'bus_sign': bus_sign(),
        'crossbuck': crossbuck(), 'tie': tie(), 'rail': rail(), 'mailbox': mailbox(), 'mailbox_front': mailbox_front(), 'flag_red': flag_red(),
        'post_wood': post_wood(), 'window_sill': window_sill(), 'oak_door': oak_door(), 'cabinet_front': cabinet_front(), 'counter_top': counter_top(),
        'counter_edge': counter_edge(), 'steel': steel(), 'sink_basin': sink_basin(), 'range_front': range_front(), 'range_top': range_top(),
        'fridge_0': fridge(0), 'fridge_1': fridge(1), 'fridge_side': fridge_side(), 'phone_face': phone_face(), 'phone_cord': phone_cord(),
        'canisters': canisters(), 'toaster': toaster(), 'chrome': chrome(), 'chair_vinyl': vinyl('#8a3a2c', 9320), 'table_top': table_top(),
        'bed_frame': bed_frame(), 'headboard': headboard(), 'comforter': comforter(), 'sheet': sheet(), 'pillow': pillow(), 'drawers': drawers(),
        'wood_top': wood_top(), 'boombox': boombox(), 'clock_radio': clock_radio(), 'louver': louver(), 'books': books(), 'mirror': mirror(),
        'corduroy': corduroy(), 'tv_cabinet': tv_cabinet(), 'tv_screen': tv_screen(), 'tv_front': tv_front(), 'lampshade': lampshade(),
        'lamp_base': lamp_base(), 'enamel': enamel(), 'tub_inside': tub_inside(), 'cooler_side': cooler_side(), 'cooler_top': cooler_top(),
        'duct': duct(), 'porch_lamp': porch_lamp(), 'barrel_rust': barrel_rust(), 'ash_top': ash_top(), 'hose': hose(), 'concrete_step': concrete_step(),
        'corrugated_under': corrugated_under(), 'rollup': rollup(), 'rollup_low': rollup_low(), 'wire_glass': wire_glass(), 'barn_shade': barn_shade(),
        'bulb': bulb(), 'dome_glass': dome_glass(), 'tube_lens': tube_lens(),
        'display_lit': display(1), 'display_dark': display(0), 'transom': transom(), 'awning': awning('#2e5a3e'), 'valance': valance('#2e5a3e'),
        'pilaster_cap': pilaster_cap(), 'drivein_arrow': drivein_arrow(), 'goods_0': goods(9415), 'goods_1': goods(9416), 'bottles': bottles(),
        'counter_front_w': counter_front_w(), 'register': register(), 'stool_top': stool_top(), 'magazines': magazines(), 'cards': cards(),
        'tapes_0': tapes(0), 'tapes_1': tapes(1), 'tapes_2': tapes(2), 'tape_shelf_side': tape_shelf_side(), 'rewinder': rewinder(),
        'return_slot': return_slot(), 'standee': standee(), 'tv_case': tv_case(), 'tape_screen': tape_screen(), 'gov_metal': gov_metal(),
        'gov_drawers': gov_drawers(), 'radio_set': radio_set(), 'bench_wood': bench_wood(), 'flag': flag_tex(), 'corkboard': corkboard(),
        'water_jug': water_jug(), 'cooler_body': cooler_body(), 'feed_sack': feed_sack(), 'sack_top': sack_top(), 'mineral': mineral(),
        'seed_bin_top': seed_bin_top(), 'rubber': rubber(), 'canopy_under': canopy_under(), 'canopy_edge': canopy_edge(),
        'canopy_light': canopy_light(), 'menu_board': menu_board(), 'speaker': speaker(), 'order_window': order_window(), 'fryer': fryer(),
        'griddle': griddle(), 'picnic': picnic(), 'machine': machine(9505), 'feeder_front': feeder_front(), 'stand_front': stand_front(),
        'cleaner_front': cleaner_front(), 'press_lower': press_front(0), 'press_upper': press_front(1), 'auger_flight': auger_flight(),
        'trough': trough(), 'pipe': pipe(), 'bale_side': bale_side(), 'bale_tag': bale_tag(), 'module_side': module_side(), 'tarp': tarp(),
        'module_end': module_end(), 'bin_wall': bin_wall(), 'cottonseed': cottonseed(), 'ladder': ladder_tex(), 'door_slide_lower': door_sliding(0),
        'door_slide_upper': door_sliding(1), 'tower_paint': tower_paint(), 'tower_rod': tower_rod(), 'grate': grate(), 'beacon': beacon(),
        'tank_wall': tank_wall(), 'tank_1': tank_wall('LA'), 'tank_2': tank_wall('ME'), 'tank_3': tank_wall('SA'), 'tank_roof': tank_roof(),
        'bench_ad': bench_ad(), 'door_glass_lower': door_glass(0), 'door_glass_upper': door_glass(1), 'door_house_lower': house_door(0),
        'door_house_upper': house_door(1), 'door_broken_lower': broken_door(0), 'door_broken_upper': broken_door(1),
    }
    for s in range(4):
        faces[f'cotton_{s}'] = cotton(s)
    for s in range(6):
        faces[f'window_{s}'] = window_glass(s); faces[f'poster_{s}'] = poster(s)
    for s, (street, avenue) in SIGN_NAMES.items():
        faces[f'blade_{s}_street'] = blade(street); faces[f'blade_{s}_avenue'] = blade(avenue)
    for i, im in enumerate(split(drug_sign(), 4)):
        faces[f'sign_drug_{i}'] = im
    for i, im in enumerate(split(feed_sign(), 4)):
        faces[f'sign_feed_{i}'] = im
    for i, im in enumerate(split(video_sign(), 5)):
        faces[f'sign_video_{i}'] = im
    for i, im in enumerate(split(law_sign(), 4)):
        faces[f'sign_law_{i}'] = im
    board = drivein_board()
    for row in range(2):
        for col in range(3):
            faces[f'drivein_board_{1 + row * 3 + col}'] = board.crop((col * 16, row * 16, col * 16 + 16, row * 16 + 16))
    faces.update(school_faces())
    faces.update(pasture_faces())
    faces.update(party_faces())
    for name, im in faces.items():
        out[f'block/the_list/{name}.png'] = im
    # 0.4.705: the sack on the head, in the hand, and from inside.
    out['entity/the_list/sack.png'] = sack_skin()
    out['item/list_sack.png'] = sack_item()
    out['gui/list_sack_inside.png'] = sack_inside()
    # 0.4.707: the page Amber holds up to her window.
    out['entity/the_list/page.png'] = page()
    # 0.4.709: the hay hook, clean and after a finish, and the blood thrown off it.
    out['item/list_hay_hook.png'] = hay_hook(False)
    out['item/list_hay_hook_bloody.png'] = hay_hook(True)
    for i in range(4):
        out[f'particle/list_blood_{i}.png'] = blood(i)
    # 0.4.710: where it lands, the wound, and what gets on the slasher and the sack.
    for i in range(4):
        out[f'block/the_list/blood_{i}.png'] = stain(i)
    out['entity/the_list/wound.png'] = wound()
    out['entity/the_list/neck.png'] = neck()
    for i in (1, 2, 3):
        out[f'entity/the_list/spatter_{i}.png'] = spatter(i)
        out[f'entity/the_list/sack_blood_{i}.png'] = sack_blood(i)
    out['entity/the_list/sack_dirt.png'] = sack_dirt()
    out['entity/the_list/sack_torn.png'] = sack_torn()
    return out



# ------------------------------------------------------------------------------------------------ 0.4.705: the slasher's body
# The feed sack over the slasher's head, at the skin's own density: each face of the bag eight texels across, as the head's faces are.
# Burlap, a stitched seam over the top, the feed company's stencil faded on the back, twine at the neck; the eyeholes cut out of it
# (clear, so whatever face is under it shows through), and a face drawn on the front in grease pencil. The same sack seen from inside,
# across the screen; the sack held, as an item; and a house door that has given.
BURLAP = '#b39a6a'


def burlap(im, x0, y0, w, h, seed, k=0):
    """A patch of burlap: the weave's over and under in a checker, a thread run darker now and then."""
    rnd = random.Random(seed)
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            weave = 7 if (x + y) % 2 == 0 else -6
            dot(im, x, y, shade(BURLAP, weave + k + rnd.randint(-4, 4)))
    for _ in range(max(1, w * h // 14)):
        dot(im, rnd.randrange(x0, x0 + w), rnd.randrange(y0, y0 + h), shade(BURLAP, -22 + k))


def sack_skin():
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    # The bag: top, bottom, the two sides, the front and the back.
    burlap(im, 8, 0, 8, 8, 9705)
    for x in range(8, 16):
        dot(im, x, 3, '#8a744c' if x % 2 else '#6e5a3a'); dot(im, x, 4, shade(BURLAP, -14))
    fill(im, 16, 0, 24, 8, '#3a2e20')
    burlap(im, 0, 8, 8, 8, 9706, -4)
    burlap(im, 16, 8, 8, 8, 9707, -4)
    burlap(im, 8, 8, 8, 8, 9708)
    burlap(im, 24, 8, 8, 8, 9709, -2)
    # The seams down the sides, and the bag gathered darker at its foot.
    for y in range(8, 16):
        dot(im, 0, y, shade(BURLAP, -18)); dot(im, 23, y, shade(BURLAP, -18))
    for x in range(0, 32):
        darken(im, x, 15, -16)
    # The feed company's stencil on the back, faded: a band, and the shape of its lettering, pale in it.
    for x in range(25, 31):
        for y in (11, 12, 13):
            dot(im, x, y, mix('#4a5a3a', BURLAP, .45))
    for x in (26, 28, 29):
        dot(im, x, 12, mix('#d8ccaa', BURLAP, .4))
    for x in (2, 3, 4, 5):
        dot(im, x, 12, mix('#4a5a3a', BURLAP, .6))
    # The front: eyeholes cut out, frayed round; the face drawn on in grease pencil.
    f = (8, 8)
    for (x, y) in [(1, 3), (2, 3), (1, 4), (2, 4), (5, 3), (6, 3), (5, 4), (6, 4)]:
        im.putpixel((f[0] + x, f[1] + y), (0, 0, 0, 0))
    for (x, y) in [(0, 3), (3, 4), (4, 3), (7, 4), (1, 5), (6, 5)]:
        darken(im, f[0] + x, f[1] + y, -16)
    # In grease pencil, a mark for a nose and a crooked mouth, the line lifted twice, so it reads as drawn on the sack and not as a
    # face under it (a fringe and a full mouth read as hair and a moustache at this size).
    dot(im, f[0] + 4, f[1] + 5, '#5a4e40')
    for (x, y) in [(1, 6), (2, 6), (3, 7), (4, 6), (5, 6), (6, 7)]:
        dot(im, f[0] + x, f[1] + y, '#3e3630')
    # The two corners that stand up off the top.
    for u in (32, 40):
        burlap(im, u, 0, 8, 4, 9710 + u, -6)
        darken(im, u + 2, 0, -14); darken(im, u + 3, 1, -10)
    # The bunching below the tie: folds down it.
    burlap(im, 0, 16, 32, 9, 9712, -8)
    for x in range(0, 32, 2):
        for y in range(16, 25):
            darken(im, x, y, -14)
    # The twine: two strands twisted.
    rnd = random.Random(9713)
    for x in range(32, 64):
        for y in range(16, 25):
            dot(im, x, y, '#8a6a3a' if (x + y) % 3 else '#a8884e')
        darken(im, x, 24, rnd.randint(-14, -4))
    return im


def sack_item():
    # The sack held: tied off at the neck, two corners up, the eyeholes and the drawn face looking out.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(9714)
    for y in range(2, 13):
        lo, hi = (3, 13) if y < 11 else (4 + (y - 11), 12 - (y - 11))
        for x in range(lo, hi):
            weave = 6 if (x + y) % 2 == 0 else -6
            light = 10 if x < 6 else -10 if x > 10 else 0
            dot(im, x, y, shade(BURLAP, weave + light + rnd.randint(-3, 3)))
    for x, y in [(3, 1), (12, 1)]:
        dot(im, x, y, shade(BURLAP, -4))
    for x in range(3, 13):
        dot(im, x, 2, shade(BURLAP, -16))
    # The eyeholes cut uneven, the right one longer; a mark for a nose and the same crooked mouth as on the head.
    for x, y in [(5, 6), (6, 6), (5, 7), (6, 7), (9, 6), (10, 6), (9, 7), (10, 7), (10, 8)]:
        dot(im, x, y, '#120e0a')
    dot(im, 8, 8, '#5a4e40')
    for x, y in [(4, 10), (5, 10), (6, 11), (7, 10), (8, 10), (9, 10), (10, 11), (11, 11)]:
        dot(im, x, y, '#3e3630')
    for x in range(6, 10):
        dot(im, x, 13, '#a8884e' if x % 2 else '#8a6a3a')
    for x, y in [(6, 14), (7, 14), (8, 14), (9, 14), (7, 15), (8, 15)]:
        dot(im, x, y, shade(BURLAP, -18))
    # Outline, a shade darker than the burlap, so the sack reads against any slot.
    px = im.load()
    edge = [(x, y) for y in range(16) for x in range(16) if px[x, y][3] == 0 and any(
        0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] > 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
    for x, y in edge:
        dot(im, x, y, '#4a3a24')
    return im


def page():
    """A page out of a spiral notebook, held up to a window: torn along the top where the spiral was, faintly ruled, a margin, and
    three lines of thick marker capitals. The page fills twelve texels by fifteen; the rest is clear."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(7071)
    paper = '#ebe5d2'
    for y in range(1, 16):
        for x in range(2, 14):
            dot(im, x, y, shade(paper, rnd.randint(-3, 3)))
    # Torn out along the spiral: the top row ragged, a nub of torn paper left between each hole.
    for x in range(2, 14):
        if x % 3 == 1:
            im.putpixel((x, 1), (0, 0, 0, 0))
        elif x % 3 == 2:
            dot(im, x, 1, shade(paper, -14))
    # Faint rules and the margin.
    for y in (5, 8, 11, 14):
        for x in range(2, 14):
            dot(im, x, y, mix(paper, '#9fb2c8', 0.35))
    for y in range(2, 16):
        dot(im, 4, y, mix(paper, '#c87a7a', 0.3))
    # Marker capitals, two texels high, a texel between letters and two between words; the pen pressed hard, a letter's inside left.
    ink = '#16130f'
    for y, words in ((3, (3, 2)), (6, (2, 3)), (9, (4,)), (12, (2, 1))):
        x = 5
        for n in words:
            for _ in range(n):
                if x + 1 > 12:
                    break
                for dy in (0, 1):
                    dot(im, x, y + dy, ink)
                if rnd.random() < 0.6 and x + 1 <= 12:
                    dot(im, x + 1, y + rnd.randint(0, 1), ink)
                x += 2
            x += 1
    # Its right and lower edges a shade down, so it stands off the glass.
    for y in range(2, 16):
        dot(im, 13, y, shade(im.getpixel((13, y))[:3], -12) if im.getpixel((13, y))[3] else paper)
    for x in range(2, 14):
        dot(im, x, 15, shade(im.getpixel((x, 15))[:3], -12))
    return im


def sack_inside():
    """The sack from inside, laid across the screen's width: burlap close against the eye, its threads going over and under with
    the night pricking through where they cross, and two eyeholes cut crooked, the left a little lower and smaller, their cut
    threads frayed into the holes; the strip of sack between them is thin and half clear, as two eyes see past it."""
    w, h = 128, 64
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    rnd = random.Random(9715)
    holes = [(41.0, 33.6, 22.0, 19.2, .7), (87.0, 30.4, 23.2, 21.2, 2.3)]

    def reach(x, y, cx, cy, rx, ry, turn):
        a = math.atan2((y - cy) / ry, (x - cx) / rx)
        r = 1 + .09 * math.sin(3 * a + turn) + .05 * math.sin(5 * a - 2 * turn) + .03 * math.sin(9 * a + turn)
        return (((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2) / (r * r)
    # The weave: threads three texels thick with a texel between, the warp's run nudged here and there so the grid is a cloth's.
    cols = []
    x = 0
    while x < w:
        cols.append(x); x += 4 if rnd.random() < .8 else (3 if rnd.random() < .5 else 5)
    rows = []
    y = 0
    while y < h:
        rows.append(y); y += 4 if rnd.random() < .8 else (3 if rnd.random() < .5 else 5)
    col_of = [max(i for i, c in enumerate(cols) if c <= x) for x in range(w)]
    row_of = [max(i for i, r in enumerate(rows) if r <= y) for y in range(h)]
    slub_w = {i: rnd.randint(-6, 6) for i in range(len(cols))}
    slub_h = {i: rnd.randint(-6, 6) for i in range(len(rows))}
    edge = []
    for y in range(h):
        for x in range(w):
            d = min(reach(x + .5, y + .5, *hole) for hole in holes)
            fray = rnd.uniform(-.06, .05)
            if d < 1 + fray:
                continue
            ci, ri = col_of[x], row_of[y]
            nx = cols[ci + 1] if ci + 1 < len(cols) else w + 1
            ny = rows[ri + 1] if ri + 1 < len(rows) else h + 1
            gx, gy = x == nx - 1, y == ny - 1
            near = max(0, 22 - int(12 * (d - 1))) if d < 2.6 else 0
            if gx and gy:
                # Where the threads cross apart, the night comes through.
                dot(im, x, y, (14, 14, 20, rnd.randint(70, 130)))
                continue
            if gx:
                c = shade('#281f15', slub_w[ci] - 6 + near)
            elif gy:
                c = shade('#281f15', slub_h[ri] - 6 + near)
            else:
                over_h = (ci + ri) % 2 == 0
                t = (y - rows[ri]) if over_h else (x - cols[ci])
                span = (ny - rows[ri] - 1) if over_h else (nx - cols[ci] - 1)
                lift = 7 if 0 < t < span - 1 else (2 if t == 0 else -3)
                c = shade('#281f15', lift + (slub_h[ri] if over_h else slub_w[ci]) + near + rnd.randint(-2, 2))
            dot(im, x, y, c)
            if d < 1.12 + fray:
                edge.append((x, y))
    # The cut edge: raw thread ends, paler where they caught the light, a few standing out into the hole.
    for (x, y) in edge:
        if rnd.random() < .5:
            dot(im, x, y, shade('#5a4630', rnd.randint(-6, 8)))
    for hole in holes:
        cx, cy = hole[0], hole[1]
        for _ in range(26):
            x, y = rnd.choice([e for e in edge if (e[0] - cx) ** 2 + (e[1] - cy) ** 2 < (hole[2] * 1.3) ** 2])
            dx, dy = cx - x, cy - y; n = math.hypot(dx, dy) or 1
            for s in range(1, rnd.randint(2, 4)):
                px, py = int(round(x + dx / n * s)), int(round(y + dy / n * s))
                if im.getpixel((px, py))[3] == 0:
                    dot(im, px, py, shade('#4a3a26', rnd.randint(-4, 10), 200 - 40 * s))
    for y in range(h):
        for x in range(62, 66):
            p = im.getpixel((x, y))
            if p[3] == 255 and 14 < y < 50:
                im.putpixel((x, y), p[:3] + (120,))
    return im


def broken_door(upper):
    # A house door that has given: split along the lock side, the lower panel by the knob punched through to the dark, raw wood
    # showing round the break and down the split, the knob knocked crooked.
    im = house_door(upper)
    raw, pale, gap = '#c9a978', '#efe0bc', '#4a3a2c'
    if not upper:
        for (x, y) in [(14, 0), (14, 1), (13, 2), (14, 3), (13, 4), (13, 5), (12, 6), (13, 7), (12, 8), (12, 9)]:
            dot(im, x, y, gap); dot(im, x - 1, y, pale); dot(im, x + 1, y, raw)
        for (x, y) in [(10, 3), (11, 3), (10, 4), (11, 4), (12, 4), (10, 5), (11, 5)]:
            im.putpixel((x, y), (0, 0, 0, 0))
        for (x, y) in [(9, 2), (10, 2), (11, 2), (12, 3), (9, 3), (9, 4), (9, 5), (12, 5), (10, 6), (11, 6), (9, 6)]:
            dot(im, x, y, raw if (x + y) % 2 else pale)
        for (x, y) in [(8, 3), (8, 5), (13, 3), (12, 7)]:
            dot(im, x, y, pale)
        dot(im, 13, 0, '#3a3026'); dot(im, 12, 1, '#a8822a')
    else:
        for (x, y) in [(12, 15), (13, 14), (12, 13), (13, 12), (14, 11), (14, 10)]:
            dot(im, x, y, gap); dot(im, x - 1, y, pale); dot(im, x + 1, y, raw)
        for (x, y) in [(10, 14), (11, 13), (9, 15)]:
            dot(im, x, y, raw)
    return im

KINDS = ['cotton', 'mesquite', 'weed', 'pole', 'fence', 'signpost', 'track', 'mailbox', 'window', 'kitchen', 'table', 'bed', 'room', 'poster',
         'den', 'bath', 'rooftop', 'yard', 'shop', 'fixture', 'front', 'sign_drug', 'sign_feed', 'sign_video', 'sign_law', 'sign_drivein', 'drug',
         'video', 'law', 'feed', 'drivein', 'gin', 'bale', 'seed', 'tower', 'tank', 'field', 'stands', 'locker', 'score', 'amber', 'lot',
         'pasture', 'stock', 'windmill', 'pumpjack', 'battery', 'party', 'blood']
STAGES = {'cotton': 4, 'mesquite': 5, 'weed': 5, 'pole': 7, 'fence': 4, 'signpost': 6, 'window': 6, 'kitchen': 8, 'table': 2, 'bed': 2, 'room': 5,
          'poster': 6, 'den': 6, 'bath': 5, 'rooftop': 4, 'yard': 8, 'shop': 4, 'fixture': 5, 'front': 6, 'sign_drug': 4, 'sign_feed': 4,
          'sign_video': 5, 'sign_law': 4, 'sign_drivein': 8, 'drug': 8, 'video': 8, 'law': 8, 'feed': 8, 'drivein': 8, 'gin': 8, 'bale': 4, 'seed': 8,
          'tower': 7, 'tank': 7, 'field': 8, 'stands': 8, 'locker': 8, 'score': 8, 'amber': 8, 'lot': 8,
          'pasture': 8, 'stock': 5, 'windmill': 8, 'pumpjack': 8, 'battery': 8, 'party': 8, 'blood': 4}
STAGE_MAX = 8
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MATERIAL_NAMES = {'caliche': 'Caliche', 'asphalt': 'Main Street Asphalt', 'main_line': 'Painted Line', 'curb': 'Curb', 'sidewalk': 'Sidewalk',
                  'soil': 'Cotton Field', 'dirt': 'Lot Dirt', 'lawn': 'Dry Lawn', 'playa': 'Playa Crust', 'ballast': 'Ballast', 'siding': 'Lap Siding',
                  'wainscot': 'Brick Wainscot', 'shingle': 'Roof Shingles', 'flat_roof': 'Tar and Gravel', 'carpet': 'Carpet', 'lino': 'Kitchen Linoleum',
                  'drywall': 'Painted Wallboard', 'paneling': 'Wood Paneling', 'ceiling': 'Popcorn Ceiling', 'tile': 'Store Tile',
                  'store_carpet': 'Video Store Carpet', 'storefront': 'Face Brick', 'stucco': 'Stucco', 'cinder': 'Cinder Block',
                  'galvanized': 'Galvanized Iron', 'rusted': 'Rusted Iron', 'slab': 'Gin Floor', 'shop_metal': 'Shop Siding', 'drive_lot': 'Drive-in Lot',
                  'turf': 'Field Turf', 'turf_dark': 'Mown Turf', 'turf_line': 'Yard Line', 'turf_side': 'Sideline', 'endzone': 'End Zone',
                  'track_cinder': 'Cinder Track', 'pasture': 'Buffalo Grass', 'mud': 'Trodden Mud'}
DOORS = {'list_house_door': ('door_house_lower', 'door_house_upper'), 'list_glass_door': ('door_glass_lower', 'door_glass_upper'),
         'list_steel_door': ('door_steel_lower', 'door_steel_upper'), 'list_broken_door': ('door_broken_lower', 'door_broken_upper')}
NAMES = {'list_fitting': 'Lamesa Fitting', 'list_shingle_stairs': 'Shingle Stairs', 'list_shingle_slab': 'Shingle Slab', 'list_metal_stairs': 'Galvanized Stairs',
         'list_house_door': 'Panel Door', 'list_glass_door': 'Store Door', 'list_steel_door': 'Steel Door', 'list_broken_door': 'Broken Door',
         'list_machine': 'Lamesa Machine', **{f'list_{k}': v for k, v in MATERIAL_NAMES.items()}}


ITEM_NAMES = {'list_sack': 'Feed Sack', 'list_hay_hook': 'Hay Hook'}


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


def door_states(name):
    """A door's states as vanilla lays them out: each facing, half, hinge and opening, turned alike."""
    base = {'east': 0, 'south': 90, 'west': 180, 'north': 270}
    out = {}
    for facing, y in base.items():
        for half in ('lower', 'upper'):
            for hinge in ('left', 'right'):
                for opened in ('false', 'true'):
                    turn = y + (90 if opened == 'true' and hinge == 'left' else -90 if opened == 'true' else 0)
                    v = {'model': NS + f'block/{name}_{half}_{hinge}' + ('_open' if opened == 'true' else '')}
                    if turn % 360:
                        v['y'] = turn % 360
                    out[f'facing={facing},half={half},hinge={hinge},open={opened}'] = v
    return out


# ------------------------------------------------------------------------------------------------ the hook (0.4.709)
# The hay hook from the seed house: a turned wooden grip across the fist and an iron hook out between the fingers, its shank straight
# and then bent round into a point. Drawn as a held thing is (grip at the lower left, the hook up to the right), clean and, after a
# finish, with blood down the bend and off the point.
HOOK_IRON, HOOK_LIGHT, HOOK_DARK = '#4c4f54', '#8a8f96', '#2a2c30'
GRIP_WOOD, GRIP_LIGHT, GRIP_DARK = '#8c6238', '#b08452', '#563a20'
BLOOD_DRY, BLOOD_WET, BLOOD_LIGHT = '#4e0a0a', '#7c1010', '#a8221c'


def hay_hook(bloody):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # The grip: two pixels thick, across the shank at right angles, light along its top edge and dark under, its ends worn darker.
    for t in range(-3, 3):
        x, y = 3 + t, 12 + t
        dot(im, x, y, GRIP_LIGHT if t > -3 and t < 2 else GRIP_DARK)
        dot(im, x + 1, y, GRIP_WOOD if t > -3 and t < 2 else GRIP_DARK)
        if t in (-1, 0):
            dot(im, x + 1, y + 1, GRIP_DARK)
    # The ferrule where the shank goes into the grip.
    dot(im, 5, 11, '#6a6460'); dot(im, 4, 11, '#5a5450')
    # The shank, a highlight along one side.
    for k in range(6):
        dot(im, 6 + k, 10 - k, HOOK_IRON)
        dot(im, 6 + k, 9 - k, HOOK_LIGHT if k % 2 == 0 else HOOK_IRON)
    # The bend, over and round, and the point coming back down toward the grip, filed bright at the tip.
    for x, y in [(12, 4), (12, 3), (13, 2), (14, 2), (15, 3), (15, 4), (15, 5), (14, 6), (14, 7), (13, 8), (12, 8)]:
        dot(im, x, y, HOOK_IRON)
    for x, y in [(13, 3), (14, 3)]:
        dot(im, x, y, HOOK_LIGHT)
    for x, y in [(15, 5), (14, 7)]:
        dot(im, x, y, HOOK_DARK)
    dot(im, 11, 9, '#b8bec4')
    if bloody:
        for x, y in [(15, 5), (14, 6), (14, 7), (13, 8), (12, 8)]:
            dot(im, x, y, BLOOD_WET)
        dot(im, 15, 4, BLOOD_DRY); dot(im, 11, 9, BLOOD_LIGHT); dot(im, 11, 10, BLOOD_WET); dot(im, 11, 11, BLOOD_DRY)
        dot(im, 12, 5, BLOOD_DRY); dot(im, 13, 9, BLOOD_DRY)
    return im


def blood(frame):
    # A drop thrown off the hook, four ways it can look in the air: a bead, a fatter bead, a drawn-out string, a splash breaking up.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    shapes = [
        [(7, 7, BLOOD_WET), (8, 7, BLOOD_WET), (7, 8, BLOOD_DRY), (8, 8, BLOOD_WET), (7, 7, BLOOD_LIGHT)],
        [(6, 7, BLOOD_WET), (7, 6, BLOOD_WET), (8, 6, BLOOD_WET), (9, 7, BLOOD_DRY), (6, 8, BLOOD_DRY), (7, 7, BLOOD_LIGHT), (8, 7, BLOOD_WET),
         (7, 8, BLOOD_WET), (8, 8, BLOOD_DRY), (7, 9, BLOOD_DRY)],
        [(8, 4, BLOOD_DRY), (8, 5, BLOOD_WET), (7, 6, BLOOD_WET), (8, 6, BLOOD_LIGHT), (7, 7, BLOOD_WET), (8, 7, BLOOD_WET), (7, 8, BLOOD_WET),
         (7, 9, BLOOD_DRY), (7, 10, BLOOD_DRY)],
        [(5, 6, BLOOD_DRY), (7, 5, BLOOD_WET), (9, 6, BLOOD_WET), (10, 8, BLOOD_DRY), (6, 8, BLOOD_WET), (7, 7, BLOOD_LIGHT), (8, 7, BLOOD_WET),
         (8, 8, BLOOD_WET), (7, 9, BLOOD_DRY), (9, 10, BLOOD_DRY), (4, 9, BLOOD_DRY)],
    ][frame]
    for x, y, c in shapes:
        dot(im, x, y, c)
    return im


# ------------------------------------------------------------------------------------------------ 0.4.710: where it lands
# Blood on the ground, at sixteen pixels: drops; a spatter thrown one way (toward the top of the picture, north, and turned with the
# block), the drops drawn out the way they flew; a smear dragged the same way, streaked by what dragged it; a pool, wet at its edge
# and catching what light there is. Four ways the wound and the blood look on the five and on the slasher, at their skins' density.
BLOOD_DEEP = '#2e0505'


def stain(stage):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rnd = random.Random(9710 + stage)
    def drop(x, y, r):
        for dy in range(-r, r + 1):
            for dx in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r + (1 if r else 0):
                    dot(im, x + dx, y + dy, BLOOD_DRY if dx * dx + dy * dy >= r * r else BLOOD_WET)
        if r:
            dot(im, x - 1, y - 1, BLOOD_LIGHT)
    if stage == 0:
        # Drops, round where they fell straight, a few specks thrown off them.
        for x, y, r in [(4, 5, 1), (9, 3, 0), (11, 8, 1), (6, 10, 1), (3, 12, 0), (12, 13, 0), (8, 7, 0)]:
            drop(x, y, r)
        for x, y in [(5, 3), (13, 6), (10, 11), (2, 8), (7, 14)]:
            dot(im, x, y, BLOOD_DRY)
    elif stage == 1:
        # Thrown north: the heavy drops near where it came from (the foot of the picture), drawn out the further they flew, each
        # with its tail ahead of it.
        for x0, y0, n in [(7, 13, 3), (5, 11, 4), (9, 10, 5), (4, 7, 5), (11, 6, 6), (7, 6, 6), (8, 3, 4), (12, 2, 3), (3, 3, 3)]:
            for k in range(n):
                y = y0 - k
                if 0 <= y < 16:
                    dot(im, x0, y, BLOOD_WET if k < max(1, n - 2) else BLOOD_DRY)
            dot(im, x0 + 1, y0, BLOOD_WET); dot(im, x0, y0 + 1, BLOOD_DRY)
            dot(im, x0, y0, BLOOD_LIGHT)
        for x, y in [(2, 9), (13, 9), (10, 14), (6, 1), (14, 4), (1, 5)]:
            dot(im, x, y, BLOOD_DRY)
    elif stage == 2:
        # Dragged north: a band, broad where it began and thinning, streaked lengthways, ragged at its edges.
        for y in range(1, 16):
            half = 1 + (y * 3) // 15
            sway = round(math.sin(y * .7) * .8)
            for x in range(8 - half + sway, 8 + half + 1 + sway):
                streak = (x + 7) % 3 == 0
                dot(im, x, y, BLOOD_DRY if streak else BLOOD_WET)
            if rnd.random() < .5:
                dot(im, 8 - half + sway - 1, y, BLOOD_DRY)
            if rnd.random() < .5:
                dot(im, 8 + half + sway + 1, y, BLOOD_DRY)
        for y in (12, 13, 14):
            dot(im, 7, y, BLOOD_LIGHT)
        for x, y in [(3, 14), (12, 12), (5, 4), (11, 2)]:
            dot(im, x, y, BLOOD_DRY)
    else:
        # A pool: dark in the middle, a wet edge, a glint where the light is, a run off one side.
        cx, cy = 7.5, 8.5
        for y in range(16):
            for x in range(16):
                d = ((x - cx) / 6.2) ** 2 + ((y - cy) / 5.4) ** 2 + .12 * math.sin(x * 1.3 + y * .9)
                if d < .45:
                    dot(im, x, y, BLOOD_DEEP)
                elif d < .85:
                    dot(im, x, y, BLOOD_DRY if (x + y) % 5 else BLOOD_DEEP)
                elif d < 1.0:
                    dot(im, x, y, BLOOD_WET)
        for x, y in [(5, 5), (6, 5), (6, 6)]:
            dot(im, x, y, BLOOD_LIGHT)
        for x, y in [(13, 11), (14, 12), (14, 13), (15, 13)]:
            dot(im, x, y, BLOOD_WET)
    return im


def stain_model(stage):
    # Flat on what is under it, a sixty-fourth of a block above it.
    e = [element([0, .25, 0], [16, .25, 16], {'up': 'b', 'down': 'b'})]
    return model(e, {'b': f'blood_{stage}'}, f'blood_{stage}')


def bleed_run(im, x, y0, y1, rnd, wide=1, lo=0, hi=10 ** 6):
    # A run of blood down cloth: wavering a pixel now and then (never off the face it runs down), darker as it dries, a bead at its end.
    for y in range(y0, y1):
        if rnd.random() < .12:
            x = max(lo, min(hi - wide, x + rnd.choice((-1, 1))))
        for k in range(wide):
            dot(im, x + k, y, BLOOD_WET if y - y0 < (y1 - y0) * .6 else BLOOD_DRY)
    dot(im, x, y1, BLOOD_DRY); dot(im, x + wide - 1, y1, BLOOD_DRY)
    return x


def wound_at(im, cx, cy, hem, seed, lo, hi):
    # The hook's wound, on the skins' own grid (two of the sheet's pixels to one painted): a short slit torn through the cloth on a
    # slant, black inside, threads standing out of it; the soak round it uneven and heavier below, as cloth takes it; and runs from
    # the foot of the soak down to the hem.
    rnd = random.Random(seed)
    soak = set()
    for _ in range(7):
        bx, by = cx + rnd.randint(-2, 2), cy + rnd.randint(-1, 3)
        rx, ry = rnd.uniform(1.4, 2.6), rnd.uniform(1.4, 3.0)
        for dy in range(-4, 5):
            for dx in range(-4, 5):
                if (dx / rx) ** 2 + (dy / ry) ** 2 < 1:
                    soak.add((bx + dx, by + dy))
    soak = {(x, y) for x, y in soak if lo <= x <= hi}
    for x, y in soak:
        r = rnd.random()
        dot(im, x, y, BLOOD_WET if r < .62 else BLOOD_DRY if r < .9 else BLOOD_DEEP)
    for dx, dy in [(0, -1), (0, 0), (1, 0), (1, 1)]:
        dot(im, cx + dx, cy + dy, BLOOD_DEEP)
    for dx, dy in [(-1, -1), (2, 1), (2, 2)]:
        dot(im, cx + dx, cy + dy, '#5e5040')
    dot(im, cx + 1, cy - 1, BLOOD_LIGHT); dot(im, cx - 1, cy + 1, BLOOD_LIGHT)
    # The runs, from the foot of the soak to the hem, the longest to it.
    for ox, wide, reach in [(-1, 1, 1.0), (1, 2, .75), (3, 1, .5), (-2, 1, .35)]:
        x = max(lo, min(hi - wide + 1, cx + ox))
        top = max([y for (sx, y) in soak if sx == x] or [cy + 2])
        end = top + int((hem - top) * reach)
        bleed_run(im, x, top, min(hem - 1, end), rnd, wide, lo, hi + 1)


def wound():
    # Over the five's own skins (512 by 256, painted two pixels to a dot), where the coat's front is (u 68.8 to 77.6, v 32.8 to
    # 49.8), on the left of the chest just under the collarbone; and the same on the body's front under it (u 20 to 28, v 20 to 32).
    half = Image.new('RGBA', (256, 128), (0, 0, 0, 0))
    wound_at(half, 150, 70, 99, 97101, 138, 154)
    wound_at(half, 52, 45, 64, 97102, 40, 55)
    return half.resize((512, 256), Image.NEAREST)


def neck():
    # 0.4.711, the boards: where the plank came down across the back of the neck, over the five's own skins (512 by 256, painted two
    # pixels to a dot). It is only ever worn bent over a row, head down under the next one, so the blood runs toward the head: a crease
    # of broken skin across the nape where head meets collar, bruising either side of it heaviest in the middle, blood soaked into the
    # collar's lip and run from the nape up the back of the head and through the hair to the crown. On the head's back and the hair's
    # (u 24 to 32 and 56 to 64, v 8 to 16, the nape its last row), the body's back (u 32 to 40, from v 20) and the coat's (u 82.4 to
    # 91.2, from v 32.8), and the back edge of the body's top (u 20 to 28, v 16) and the coat's (u 68.8 to 77.6, v 28).
    half = Image.new('RGBA', (256, 128), (0, 0, 0, 0))
    rnd = random.Random(97111)
    bruise, dark = '#5b2a3a', '#3d2030'

    def heavy(x, x0, x1):
        return 1 - abs(x - (x0 + x1 - 1) / 2) / ((x1 - x0) / 2)

    def run_up(x, y0, y1, lo, hi):
        # From the nape toward the crown, wavering, drying as it goes.
        for y in range(y0, y1, -1):
            if rnd.random() < .15:
                x = max(lo, min(hi - 1, x + rnd.choice((-1, 1))))
            dot(half, x, y, BLOOD_WET if y0 - y < (y0 - y1) * .55 else BLOOD_DRY)
        dot(half, x, y1, BLOOD_DRY)

    # The nape: the head's last two rows, and the hair's, the crease along the bottom one, and runs up toward the crown.
    for x0, cover, runs in ((48, 1.0, ((-3, .8), (0, 1.0), (2, .6), (5, .45))), (112, .55, ((-1, .7), (3, .5)))):
        for x in range(x0 + 1, x0 + 15):
            w = heavy(x, x0 + 1, x0 + 15)
            if rnd.random() < (.35 + .55 * w) * cover:
                dot(half, x, 30, dark if rnd.random() < .5 * w else bruise)
            if rnd.random() < (.55 + .45 * w) * cover:
                dot(half, x, 31, BLOOD_DEEP if rnd.random() < .5 else BLOOD_WET)
        mid = x0 + 8
        for ox, reach in runs:
            run_up(mid + ox, 29, 29 - int(9 * reach), x0 + 1, x0 + 15)
    # The collar: the crease on the top row of the back and bruising under it; the blood has run the other way, over its lip.
    for x0, x1, top in ((64, 80, 40), (165, 182, 66)):
        for x in range(x0 + 1, x1 - 1):
            w = heavy(x, x0 + 1, x1 - 1)
            if rnd.random() < .6 + .4 * w:
                dot(half, x, top, BLOOD_DEEP if rnd.random() < .45 else BLOOD_WET)
            for r in (1, 2):
                if rnd.random() < (.3 + .6 * w) / r:
                    dot(half, x, top + r, dark if rnd.random() < .45 * w else bruise)
    # The collar's lip: the back edge of the top of the body and of the coat, soaked heaviest in the middle.
    for x0, x1, edge in ((40, 56, 32), (138, 155, 56)):
        for x in range(x0 + 1, x1 - 1):
            w = heavy(x, x0 + 1, x1 - 1)
            if rnd.random() < .5 + .5 * w:
                dot(half, x, edge, BLOOD_WET if rnd.random() < .6 else BLOOD_DEEP)
            if rnd.random() < .45 * w:
                dot(half, x, edge + 1, BLOOD_DRY)
    return half.resize((512, 256), Image.NEAREST)


def spatter(level):
    # Over a player's own skin (64 by 64), on its outer layer: the shirt's front (u 20 to 28, v 36 to 48), and the sleeves of both
    # arms (the right: outside 40-43, front 44-46, fist 48-49; the left: front 52-54, outside 56-58, fist 56-57), so that it lies on
    # whichever hand holds the hook. Thrown up the front toward the face, and down the forearm; more at each level.
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    rnd = random.Random(97100 + level)
    flecks = {1: 8, 2: 18, 3: 34}[level]
    for _ in range(flecks):
        x = 20 + min(7, int(rnd.random() ** 1.6 * 8))
        y = 36 + min(11, int(rnd.random() ** 1.3 * 9))
        dot(im, x, y, rnd.choice((BLOOD_WET, BLOOD_WET, BLOOD_DRY)))
        if level >= 2 and rnd.random() < .3:
            dot(im, x, y + 1, BLOOD_DRY)
    if level >= 2:
        bleed_run(im, 21, 38, 44 if level == 2 else 47, rnd)
        bleed_run(im, 23, 37, 41 if level == 2 else 46, rnd)
    if level >= 3:
        for x in range(20, 25):
            for y in range(36, 42):
                if rnd.random() < .6 - (y - 36) * .06:
                    dot(im, x, y, BLOOD_WET if rnd.random() < .7 else BLOOD_DRY)
        bleed_run(im, 25, 38, 46, rnd)
    # The sleeves, forearm first: from the cuff up as far as it has got.
    up = {1: 45, 2: 42, 3: 39}[level]
    for (x0, x1, fist) in ((40, 47, (48, 50)), (52, 59, (56, 58))):
        for x in range(x0, x1):
            if x in (47, 55) or (x0 == 52 and x == 55):
                continue
            for y in range(36 if x0 == 40 else 52, 48 if x0 == 40 else 64):
                row = y - (36 if x0 == 40 else 52)
                if row + 36 >= up and rnd.random() < (.25 + .18 * level) * (1 if row > 8 else .6):
                    dot(im, x, y, BLOOD_WET if rnd.random() < .6 else BLOOD_DRY)
        if level >= 2:
            for x in range(*fist):
                for y in range(32 if x0 == 40 else 48, 36 if x0 == 40 else 52):
                    dot(im, x, y, BLOOD_WET if (x + y) % 2 else BLOOD_DRY)
    return im


SACK_FRONT = (8, 8)
EYEHOLES = [(1, 3), (2, 3), (1, 4), (2, 4), (5, 3), (6, 3), (5, 4), (6, 4)]


def keep_eyeholes(im):
    for x, y in EYEHOLES:
        im.putpixel((SACK_FRONT[0] + x, SACK_FRONT[1] + y), (0, 0, 0, 0))
    return im


def sack_blood(level):
    # Over the sack (64 by 32): thrown at its face from in front, more each time, then soaked down from the mouth; never over the
    # eyeholes.
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    rnd = random.Random(97200 + level)
    fx, fy = SACK_FRONT
    for _ in range({1: 6, 2: 13, 3: 20}[level]):
        dot(im, fx + rnd.randrange(8), fy + rnd.randrange(1, 8), rnd.choice((BLOOD_WET, BLOOD_DRY)))
    for _ in range(level * 2):
        dot(im, rnd.randrange(0, 8), 8 + rnd.randrange(3, 8), BLOOD_DRY)
    if level >= 2:
        bleed_run(im, fx + 3, fy + 6, fy + 8, rnd)
        for x in range(fx + 1, fx + 7):
            if rnd.random() < .5:
                dot(im, x, 24, BLOOD_WET)
    if level >= 3:
        for x in range(fx + 1, fx + 7):
            for y in range(fy + 5, fy + 8):
                if rnd.random() < .7:
                    dot(im, x, y, BLOOD_WET if (x + y) % 3 else BLOOD_DEEP)
        bleed_run(im, fx + 5, fy + 5, fy + 8, rnd)
    return keep_eyeholes(im)


def sack_dirt():
    # Lesley's fistful: caliche dust thrown in its face, thick round the eyeholes and caught in the weave below them.
    im = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
    rnd = random.Random(97300)
    fx, fy = SACK_FRONT
    for x, y in EYEHOLES:
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1), (-1, 1), (1, 1)]:
            if rnd.random() < .6:
                dot(im, fx + x + dx, fy + y + dy, rnd.choice(('#a69a82', '#8c806a', '#bcb098')))
    for _ in range(16):
        dot(im, fx + rnd.randrange(8), fy + rnd.randrange(8), rnd.choice(('#a69a82', '#8c806a')))
    for _ in range(5):
        dot(im, rnd.randrange(8, 16), rnd.randrange(0, 8), '#a69a82')
    return keep_eyeholes(im)


def sack_torn():
    # Mel's camcorder caught it across its left cheek: a tear from the front round onto the side, open through, raw threads standing
    # at its edges.
    im = sack_skin()
    fx, fy = SACK_FRONT
    gash = [(fx + 6, fy + 6), (fx + 7, fy + 5), (fx + 7, fy + 6), (16, fy + 5), (16, fy + 4), (17, fy + 4), (17, fy + 3), (18, fy + 3)]
    for x, y in gash:
        im.putpixel((x, y), (0, 0, 0, 0))
    for x, y in [(fx + 5, fy + 6), (fx + 6, fy + 7), (fx + 7, fy + 4), (16, fy + 6), (17, fy + 5), (18, fy + 2), (18, fy + 4), (19, fy + 3)]:
        dot(im, x, y, '#d8c49a')
    for x, y in [(fx + 5, fy + 7), (16, fy + 3), (19, fy + 4)]:
        darken(im, x, y, -24)
    return im


def files():
    out = {}
    for name in materials():
        if name == 'lawn':
            out['models/block/list_lawn.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
                'top': NS + 'block/list_lawn', 'bottom': NS + 'block/list_dirt', 'side': NS + 'block/list_lawn'}}
        else:
            out[f'models/block/list_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/list_{name}'}}
        out[f'blockstates/list_{name}.json'] = {'variants': {'': {'model': NS + f'block/list_{name}'}}}
    for stairs, side in (('list_shingle_stairs', 'list_shingle'), ('list_metal_stairs', 'list_galvanized')):
        tex = {'bottom': NS + 'block/' + side, 'top': NS + 'block/' + side, 'side': NS + 'block/' + side}
        out[f'models/block/{stairs}.json'] = {'parent': 'minecraft:block/stairs', 'textures': tex}
        out[f'models/block/{stairs}_inner.json'] = {'parent': 'minecraft:block/inner_stairs', 'textures': tex}
        out[f'models/block/{stairs}_outer.json'] = {'parent': 'minecraft:block/outer_stairs', 'textures': tex}
        out[f'blockstates/{stairs}.json'] = {'variants': stair_states(NS + f'block/{stairs}')}
    # The houses' low roofs: shingle slabs, laid half a block up for every block in.
    tex = {'bottom': NS + 'block/list_shingle', 'top': NS + 'block/list_shingle', 'side': NS + 'block/list_shingle'}
    out['models/block/list_shingle_slab.json'] = {'parent': 'minecraft:block/slab', 'textures': tex}
    out['models/block/list_shingle_slab_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': tex}
    out['blockstates/list_shingle_slab.json'] = {'variants': {'type=bottom': {'model': NS + 'block/list_shingle_slab'},
        'type=top': {'model': NS + 'block/list_shingle_slab_top'}, 'type=double': {'model': NS + 'block/list_shingle'}}}
    for name, (lower, upper) in DOORS.items():
        tex = {'bottom': P + lower, 'top': P + upper}
        for half, part in (('lower', 'bottom'), ('upper', 'top')):
            for hinge in ('left', 'right'):
                out[f'models/block/{name}_{half}_{hinge}.json'] = {'parent': f'minecraft:block/door_{part}_{hinge}', 'render_type': 'minecraft:cutout', 'textures': tex}
                out[f'models/block/{name}_{half}_{hinge}_open.json'] = {'parent': f'minecraft:block/door_{part}_{hinge}_open', 'render_type': 'minecraft:cutout', 'textures': tex}
        out[f'blockstates/{name}.json'] = {'variants': door_states(name)}
    for key, obj in fitting_models().items():
        out[f'models/block/the_list/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/the_list/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/list_fitting.json'] = {'variants': variants}
    # 0.4.701: the machines' own block, its still parts drawn by the block and its turning parts by its renderer: the windmill's head and
    # the pumpjack's saddle.
    machine = {}
    for facing, y in FACING_Y.items():
        for kind, name in (('windmill', 'windmill_4'), ('pumpjack', 'pumpjack_saddle'), ('cable', 'pumpjack_cable')):
            v = {'model': NS + f'block/the_list/{name}'}
            if y:
                v['y'] = y
            machine[f'facing={facing},machine={kind}'] = v
    out['blockstates/list_machine.json'] = {'variants': machine}
    # 0.4.705: the sack, held as a picture and drawn on the head by its own layer, so the game's own drawing of a thing on the head is
    # given nothing to draw.
    out['models/item/list_sack.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/list_sack'},
                                         'display': {'head': {'scale': [0, 0, 0]}}}
    # 0.4.709: the hay hook, held as a tool is held; after a finish its picture is the bloodied one (custom model data 1).
    out['models/item/list_hay_hook.json'] = {'parent': 'minecraft:item/handheld', 'textures': {'layer0': NS + 'item/list_hay_hook'},
                                             'overrides': [{'predicate': {'custom_model_data': 1}, 'model': NS + 'item/list_hay_hook_bloody'}]}
    out['models/item/list_hay_hook_bloody.json'] = {'parent': 'minecraft:item/handheld', 'textures': {'layer0': NS + 'item/list_hay_hook_bloody'}}
    out['particles/list_blood.json'] = {'textures': [NS + f'list_blood_{i}' for i in range(4)]}
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
                assert f'block/the_list/{tex[len(P):]}.png' in images, (key, tex)
            elif tex.startswith(NS + 'block/list_'):
                assert f'block/{tex[len(NS) + 6:]}.png' in images, (key, tex)
        for e in obj['elements']:
            for k in range(3):
                assert -16 <= e['from'][k] <= 32 and -16 <= e['to'][k] <= 32, (key, e)
                assert e['from'][k] <= e['to'][k], (key, e)
            if 'rotation' in e:
                assert e['rotation']['angle'] in (-45, -22.5, 0, 22.5, 45), (key, e)
            for face in e['faces'].values():
                assert face['texture'][1:] in obj['textures'], (key, face)
    # Every texture keeps to whole blocks of sixteen pixels, so the atlas keeps its mipmaps.
    for rel, im in images.items():
        assert im.size[0] % 16 == 0 and im.size[1] % 16 == 0, (rel, im.size)
    # The stores' names read across their boards; the drive-in's board is lit letters on dark.
    def ink(im, colour):
        return sum(im.getpixel((x, y))[:3] == rgb(colour) for x in range(im.size[0]) for y in range(im.size[1]))
    assert all(ink(images[f'block/the_list/sign_drug_{i}.png'], '#8a2a24') > 6 for i in range(4))
    assert all(ink(images[f'block/the_list/sign_video_{i}.png'], '#f2e6a0') > 4 for i in range(5))
    assert all(ink(images[f'block/the_list/drivein_board_{i}.png'], '#f4ead0') + ink(images[f'block/the_list/drivein_board_{i}.png'], '#f2d070') > 4 for i in range(1, 7))
    # 0.4.705: the sack's eyeholes are cut clean through; seen from inside, more of the middle is clear than not; the broken door is holed.
    assert all(images['entity/the_list/sack.png'].getpixel((8 + x, 8 + y))[3] == 0 for x in (1, 2, 5, 6) for y in (3, 4))
    inside = images['gui/list_sack_inside.png']
    assert sum(inside.getpixel((x, y))[3] == 0 for x in range(32, 96) for y in (24, 32, 40)) > 120
    assert images['block/the_list/door_broken_lower.png'].getpixel((11, 4))[3] == 0
    # 0.4.707: Amber's page is torn at the top, clear outside the sheet, and written on.
    pg = images['entity/the_list/page.png']
    assert pg.getpixel((0, 0))[3] == 0 and pg.getpixel((15, 8))[3] == 0 and pg.getpixel((4, 1))[3] == 0 and ink(pg, '#16130f') > 14
    # 0.4.709: the hook is drawn clear round its shape; the bloodied one has blood on it and the clean one none.
    hook, red = images['item/list_hay_hook.png'], images['item/list_hay_hook_bloody.png']
    assert hook.getpixel((0, 0))[3] == 0 and hook.getpixel((15, 15))[3] == 0 and ink(hook, BLOOD_WET) == 0 and ink(red, BLOOD_WET) >= 5
    assert all(sum(images[f'particle/list_blood_{i}.png'].getpixel((x, y))[3] > 0 for x in range(16) for y in range(16)) >= 4 for i in range(4))
    # 0.4.710: every stain has blood in it, heavier at each stage; the wound sits on the coat's front and the body's; the sack's marks
    # never cover its eyeholes, and the torn sack is open where it tore.
    stained = [sum(images[f'block/the_list/blood_{i}.png'].getpixel((x, y))[3] > 0 for x in range(16) for y in range(16)) for i in range(4)]
    assert stained[0] >= 8 and stained[0] < stained[1] < stained[3] and stained[2] > stained[0], stained
    wd = images['entity/the_list/wound.png']
    assert wd.getpixel((301, 141))[3] > 0 and wd.getpixel((104, 90))[3] > 0 and wd.getpixel((10, 10))[3] == 0
    assert all(275 <= x <= 310 and 131 <= y <= 199 or 80 <= x < 112 and 80 <= y < 128 for x in range(512) for y in range(256) if wd.getpixel((x, y))[3])
    # 0.4.711: the neck's mark is on the nape and the collar behind, and nowhere else.
    nk = images['entity/the_list/neck.png']
    def inked(x0, x1, y0, y1):
        return sum(nk.getpixel((x, y))[3] > 0 for x in range(x0, x1) for y in range(y0, y1))
    assert inked(96, 128, 56, 64) >= 50 and inked(96, 128, 40, 56) >= 20 and inked(224, 256, 40, 64) >= 20
    assert inked(128, 160, 80, 86) >= 40 and inked(330, 364, 132, 138) >= 40 and inked(80, 112, 64, 68) >= 12 and inked(276, 310, 112, 116) >= 12
    # Head down over the row, the blood runs toward the head: nothing below the collar's bruise on the back.
    assert inked(128, 160, 86, 128) == 0 and inked(330, 364, 138, 200) == 0
    assert all((96 <= x < 128 or 224 <= x < 256) and 40 <= y < 64 or 128 <= x < 160 and 80 <= y < 86 or 330 <= x < 364 and 132 <= y < 138
               or 80 <= x < 112 and 64 <= y < 68 or 276 <= x < 310 and 112 <= y < 116
               for x in range(512) for y in range(256) if nk.getpixel((x, y))[3])
    for name in ('sack_blood_1', 'sack_blood_2', 'sack_blood_3', 'sack_dirt', 'sack_torn'):
        assert all(images[f'entity/the_list/{name}.png'].getpixel((8 + x, 8 + y))[3] == 0 for x, y in EYEHOLES), name
    plain = sum(images['entity/the_list/sack.png'].getpixel((x, y))[3] == 0 for x in range(8, 24) for y in range(8, 16))
    assert sum(images['entity/the_list/sack_torn.png'].getpixel((x, y))[3] == 0 for x in range(8, 24) for y in range(8, 16)) >= plain + 6
    sp = [sum(images[f'entity/the_list/spatter_{i}.png'].getpixel((x, y))[3] > 0 for x in range(64) for y in range(64)) for i in (1, 2, 3)]
    assert sp[0] < sp[1] < sp[2], sp
    # The tower spells the town across three plates.
    assert all(ink(images[f'block/the_list/tank_{i}.png'], '#2a3e6e') > 8 for i in (1, 2, 3))
    # Cotton stands with its bolls open; the bare stalks have none.
    assert ink(images['block/the_list/cotton_0.png'], BOLL) > 8 and ink(images['block/the_list/cotton_3.png'], LEAF) == 0
    # Chain-link is mesh: more of each block is clear than wire.
    mesh = images['block/the_list/chainlink.png']
    assert 0.2 < sum(mesh.getpixel((x, y))[3] > 0 for x in range(16) for y in range(16)) / 256 < 0.6
    # A lit window is bright; a dark one is dark.
    def mean(im):
        px = [im.getpixel((x, y)) for x in range(4, 12) for y in range(4, 12)]
        return sum(sum(p[:3]) for p in px) / len(px)
    assert mean(images['block/the_list/window_1.png']) > mean(images['block/the_list/window_0.png']) + 150
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
        print(f'Drew Lamesa under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    wanted.update({f'item.the_oldest_house.{k}': v for k, v in ITEM_NAMES.items()})
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' Lamesa: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
