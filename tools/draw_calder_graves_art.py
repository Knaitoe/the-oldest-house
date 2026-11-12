#!/usr/bin/env python3
"""0.4.489, Bike night's graveyard: Calder Hill Cemetery, drawn in code. The proposal: "headstones in many shapes, some leaning, with
carved names and dates (original, period-plausible), lichen and moss; the angel and obelisk models; the mausoleum with its iron
doors; ... the open grave's mound and tarp", and "One row of small headstones near the back all carries the same autumn date,
years before, with names and nothing else."

What is drawn: the cemetery's grass, the earth of a fresh grave and the mausoleum's marble (plain blocks); and one fitting block,
calder_grave, of nine kinds:
- stone: sixteen headstones, four shapes (a round-topped tablet, a square one under a cap, a pointed one, a low wide marker), each
  in one of four stones, some leaning back or to a side. A face too narrow for a year has the lines of an inscription and an emblem;
  what it says is said to a hand (CalderGraves.STONES).
- small: the row of small stones. Each has its year on it, large enough to read from a bike, and it is the same year on every one.
- cross: of stone; and of wood, leaning.
- obelisk and angel: three blocks each, base to top.
- mound: the heap beside the open grave. A bike rides it as one slope and is thrown by it (BikeSurface).
- tarp: a tarpaulin pegged out flat; and one thrown over a heap.
- goods: what is left at a grave: flowers in a jar, a lantern with a candle in it, a wreath on its stand, a small flag.
- door: the mausoleum's iron doors, two blocks wide and two high, in four pieces.
- roots: what hangs under a piece of ground that has come away: three clumps of them. It hangs from the top of its block.
- picket: a length of old board fence on the block's middle line, across the way it faces: upright, leaning to either side, and
  broken off short.

0.4.493, for the Hollow (the proposal: "islands of graveyard turf with their headstones still in them; crooked fences; a bridge
of coffin lids; a pumpkin vine the size of a road, curling up and down; stairs of tilted stones"): the sod an island is made of
(grass on top, earth with roots hanging at its sides, root ends underneath), the vine's own green, a coffin's wood, and three half
blocks: the vine's, a coffin's lid, and a headstone laid flat for a step.

A fitting is drawn facing north (its inscription looks north); its block state turns it. Nothing here is a photograph or a stock
texture. Writes textures, models, block states and names. --check verifies everything; --sheet PATH draws a page to look at. It
refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_borrasca_town_art import shade, element
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/calder_grave/'
P = NS + DIR
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

# The four stones: granite, slate, marble gone grey, sandstone.
STONE = ('#8c8f8a', '#5c6168', '#c2c0b6', '#a39a7e')
LICHEN, MOSS, EARTH, IRON, WOOD, TARP = '#8f9560', '#52653a', '#5a4632', '#2a2c2e', '#6e5840', '#4a6a7a'
DIGITS = {'0': ('111', '101', '101', '101', '111'), '1': ('010', '110', '010', '010', '111'), '2': ('111', '001', '111', '100', '111'),
          '3': ('111', '001', '111', '001', '111'), '4': ('101', '101', '111', '001', '001'), '5': ('111', '100', '111', '001', '111'),
          '6': ('111', '100', '111', '101', '111'), '7': ('111', '001', '010', '010', '010'), '8': ('111', '101', '111', '101', '111'),
          '9': ('111', '101', '111', '001', '111')}
# The row's year: the same on every small stone.
YEAR = '1957'
# The sixteen headstones: shape (0 round, 1 square, 2 pointed, 3 low), which stone, how it leans ('', 'back', 'left', 'right'), its
# emblem ('', 'cross', 'star', 'urn', 'willow', 'hands'), and (the low one only) the year cut on it.
STONES = [(0, 0, '', 'cross', ''), (0, 2, 'back', 'willow', ''), (0, 1, 'left', '', ''), (0, 3, '', 'urn', ''),
          (1, 2, '', 'hands', ''), (1, 0, 'right', 'cross', ''), (1, 1, '', 'star', ''), (1, 3, 'back', '', ''),
          (2, 1, '', 'cross', ''), (2, 0, 'left', 'urn', ''), (2, 2, '', 'willow', ''), (2, 3, 'right', 'star', ''),
          (3, 0, '', '', '1934'), (3, 2, '', '', '1911'), (3, 1, '', '', '1946'), (3, 3, '', '', '1898')]
SMALL = 6
# The heap: how many lumps it rises in, how far each is in from the sides, and how high each is (CalderGraveBlock's sheet runs from 2
# to 12 pixels along it, and a lump is as high as the sheet is over its middle).
MOUND_STEPS = 8
MOUND_IN = (0, .5, 1, .5, 1.5, 1, 2, 1.5)


def mound_top(i):
    return round(2 + 10 * (i + .5) / MOUND_STEPS, 3)


def noise(colour, seed, spread=5):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(seed); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(colour, rnd.randint(-spread, spread))
    return im


def put(im, x, y, colour, k=0):
    if 0 <= x < 16 and 0 <= y < 16:
        im.putpixel((x, y), shade(colour, k))


def weather(im, rnd, base, foot=True):
    """Lichen in a few patches, and moss along the foot (a low stone has only a little, at its corners: its year is there)."""
    for _ in range(3):
        cx, cy = rnd.randrange(16), rnd.randrange(3, 13)
        for _ in range(rnd.randint(2, 5)):
            put(im, cx + rnd.randint(-1, 1), cy + rnd.randint(-1, 1), LICHEN, rnd.randint(-10, 6))
    for x in range(16):
        for y in range(15 - rnd.choice((0, 0, 1, 1, 2)), 16):
            if foot or x < 1 + rnd.randrange(2) or x > 14 - rnd.randrange(2):
                put(im, x, y if foot else 15, MOSS, rnd.randint(-8, 8))
    for _ in range(5):                                                       # where the rain runs
        x, y = rnd.randrange(16), rnd.randrange(2, 9)
        for k in range(rnd.randint(2, 5)):
            put(im, x, y + k, base, -9)


def digits(im, text, x, y, colour, k):
    for ch in text:
        for row, bits in enumerate(DIGITS[ch]):
            for col, bit in enumerate(bits):
                if bit == '1':
                    put(im, x + col, y + row, colour, k)
        x += 4


EMBLEMS = {
    'cross': ('.#.', '###', '.#.', '.#.'), 'star': ('#.#', '.#.', '#.#'), 'urn': ('###', '.#.', '###', '###'),
    'willow': ('.###.', '#.#.#', '#.#.#', '..#..'), 'hands': ('#...#', '.#.#.', '..#..'),
}


def stone_face(k):
    """A headstone's face: the picture the front of its pieces is cut from, drawn where the stone is."""
    shape, stone, lean, emblem, year = STONES[k]
    base = STONE[stone]; rnd = random.Random(4890 + k); im = noise(base, 4890 + k); cut = -34
    weather(im, rnd, base, shape != 3)
    top = 16 - (13, 15, 14, 8)[shape]                                        # the picture's row at the stone's top
    if shape == 3:                                                           # the low marker: a name's line, and its year
        for x in range(2, 14):
            if (x * 7 + k) % 5:
                put(im, x, top + 1, base, cut)
        digits(im, year, 1, top + 3, base, cut - 12)
        return im
    y = top + 3
    if emblem:
        rows = EMBLEMS[emblem]
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch == '#':
                    put(im, 8 - len(row) // 2 + dx, y + dy, base, cut)
        y += len(rows) + 1
    for line in range(3):                                                    # the lines of an inscription: a name, two dates, a text
        a, b = (4, 12) if line == 0 else (5, 11) if line == 1 else (4, 12)
        for x in range(a, b):
            if line == 1 and x == 8:
                continue
            if (x * 5 + k * 3 + line) % 6:
                put(im, x, y + line * 2, base, cut + (8 if line == 2 else 0))
    return im


def small_face(k):
    """One of the row: its year, and over it the line of a name."""
    base = STONE[(1, 0, 1, 3, 0, 1)[k]]; rnd = random.Random(48950 + k); im = noise(base, 48950 + k)
    weather(im, rnd, base, False)
    for x in range(3 + k % 2, 13 - (k * 2) % 3):
        if (x + k) % 5:
            put(im, x, 9, base, -34)
    digits(im, YEAR, 1, 10, base, -50)
    return im


def plain(colour, seed, spread=5):
    return noise(colour, seed, spread)


def marble():
    im = noise('#cfcdc4', 4891, 4); rnd = random.Random(4891)
    for start in (3, 11):                                                    # two faint veins, slanting, that run on from block to block
        for y in range(16):
            x = (start + (y + 1) // 2 + (1 if y % 5 == 4 else 0)) % 16
            if rnd.randrange(5):
                put(im, x, y, '#bcbab1', rnd.randint(-3, 3))
    return im


def grave_turf():
    """The cemetery's grass: longer and darker than the school field's, with October's leaves in it."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4892); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade('#3d5230', rnd.randint(-9, 9) + (5 if (x * 3 + y * 5) % 7 == 0 else 0))
    for colour in ('#8a5a2a', '#7a4a26', '#94782e'):                          # a leaf here and there, dull: it is night and they are wet
        x, y = rnd.randrange(16), rnd.randrange(16)
        put(im, x, y, colour, rnd.randint(-8, 4))
        if rnd.randrange(2):
            put(im, (x + 1) % 16, y, colour, rnd.randint(-16, -6))
    return im


def grave_earth():
    im = noise(EARTH, 4893, 8); rnd = random.Random(4893)
    for _ in range(9):
        put(im, rnd.randrange(16), rnd.randrange(16), '#8a7a66', rnd.randint(-6, 6))    # stones in it
    for _ in range(14):                                                      # clods and the dark between them: no lines, or a heap of it reads as boards
        x, y = rnd.randrange(16), rnd.randrange(16)
        put(im, x, y, EARTH, -20 + rnd.randrange(8))
    for _ in range(8):
        put(im, rnd.randrange(16), rnd.randrange(16), EARTH, 10 + rnd.randrange(6))
    return im


def tarp():
    im = noise(TARP, 4894, 3)
    for y in (5, 11):
        for x in range(16):
            put(im, x, y, TARP, -14)                                         # folds
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        put(im, x, y, '#c9ccd0')                                             # eyelets
    for x in range(16):
        put(im, x, 0, TARP, 12); put(im, x, 15, TARP, -18)
    return im


def wood():
    im = noise(WOOD, 4895, 4)
    for y in range(16):
        for x in (3, 9, 13):
            put(im, x, y, WOOD, -16 + (y * 3 + x) % 5)
    return im


def goods():
    """What is left at a grave, on one sheet: swatches four pixels square. Glass, petals in three colours, stems, a lantern's
    frame and its flame, a wreath's green and its ribbon, a flag's cloth and its stick."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4896); px = im.load()
    for i, colour in enumerate(('#b8c8c4', '#d8d2c0', '#b3261f', '#d9b21f', '#3f6a34', '#2a2c2e', '#ffd66a', '#2f5a3a',
                                '#8a2a2a', '#d6d2c6', '#6e5840', '#7a4a8a', '#c9ccd0', '#52653a', '#5a4632', '#101214')):
        for y in range(4):
            for x in range(4):
                px[4 * (i % 4) + x, 4 * (i // 4) + y] = shade(colour, rnd.randint(-3, 3))
    return im


def door(k):
    """A quarter of the mausoleum's doors: iron, in two leaves, studded, with a ring in each and a grille over it. k: 0 lower left as
    somebody facing them sees it, 1 lower right, 2 upper left, 3 upper right."""
    im = noise(IRON, 4897 + k, 3); rnd = random.Random(4897 + k)
    right, upper = k % 2 == 1, k >= 2
    seam = 0 if right else 15
    for y in range(16):
        put(im, seam, y, IRON, -18)                                          # where the leaves meet
        put(im, 15 if right else 0, y, '#3a3d40', 6)                         # the frame
    for x in range(16):
        if upper:
            put(im, x, 0, '#3a3d40', 6)
        else:
            put(im, x, 15, '#1c1d1f')
    for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
        put(im, x, y, '#5a5d60', 8)                                          # studs
    if upper:                                                                # the grille: bars over the dark
        for y in range(4, 11):
            for x in range(4, 12):
                put(im, x, y, '#0c0d0e' if x % 2 else '#34373a')
    else:                                                                    # a ring
        x0 = 4 if right else 9
        for x, y in ((x0, 5), (x0 + 1, 4), (x0 + 2, 5), (x0 + 1, 6), (x0, 6), (x0 + 2, 6), (x0 + 1, 7)):
            put(im, x, y, '#6a6d70', 4)
    for _ in range(6):
        put(im, rnd.randrange(16), rnd.randrange(16), '#6a4a32', rnd.randint(-8, 8))    # rust
    return im


ROOT, VINE, COFFIN = '#7a6248', '#3f6a2e', '#4a3526'


def sod_side():
    """The side of a piece of ground that has come away: the grass's fringe at the top, cut earth, and roots hanging out of the bottom of it."""
    im = grave_earth(); rnd = random.Random(4930)
    for x in range(16):
        for y in range(2 + rnd.randrange(2)):
            put(im, x, y, '#3d5230', rnd.randint(-9, 9))
    for x in (1, 4, 6, 9, 12, 14):
        top = 9 + rnd.randrange(4)
        for y in range(top, 16):
            put(im, (x + (1 if y % 3 == 2 and rnd.randrange(2) else 0)) % 16, y, ROOT, rnd.randint(-10, 8))
    return im


def sod_under():
    """Underneath: dark earth and the ends of roots."""
    im = noise(EARTH, 4931, 6); rnd = random.Random(4931); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(EARTH, -18 + rnd.randint(-6, 6))
    for _ in range(12):
        x, y = rnd.randrange(16), rnd.randrange(16)
        put(im, x, y, ROOT, rnd.randint(-6, 10)); put(im, (x + 1) % 16, (y + rnd.randrange(2)) % 16, ROOT, rnd.randint(-16, 0))
    return im


def vine():
    """The pumpkin vine, the size of a road: ribbed along its length, with a paler vein and the bristle of it."""
    im = noise(VINE, 4932, 4)
    for y in range(16):
        for x in (2, 7, 8, 13):
            put(im, x, y, VINE, (10 if x in (7, 8) else -12) + (y * 5 + x) % 4)
    rnd = random.Random(4932)
    for _ in range(10):
        put(im, rnd.randrange(16), rnd.randrange(16), '#6a8a3e', rnd.randint(-6, 6))
    return im


def coffin():
    """A coffin's wood: dark stained boards."""
    im = noise(COFFIN, 4933, 3)
    for x in range(16):
        for y in (0, 5, 10, 15):
            put(im, x, y, COFFIN, -14)
    for x, y in ((3, 2), (11, 7), (6, 12)):
        put(im, x, y, COFFIN, 12); put(im, x + 1, y, COFFIN, 9)
    return im


def lid():
    """A coffin's lid from above: its boards, a pale cross let into it, and a brass handle at each side."""
    im = coffin()
    for y in range(2, 14):
        put(im, 7, y, '#b9a77a', -6 + y % 3); put(im, 8, y, '#b9a77a', -12 + y % 3)
    for x in range(4, 12):
        put(im, x, 5, '#b9a77a', -6); put(im, x, 6, '#b9a77a', -14)
    for x in (1, 14):
        for y in (7, 8, 9):
            put(im, x, y, '#b08d3a', 4 - y % 2 * 10)
    for x in range(16):
        put(im, x, 0, COFFIN, -22); put(im, x, 15, COFFIN, -22)
    for y in range(16):
        put(im, 0, y, COFFIN, -22); put(im, 15, y, COFFIN, -22)
    return im


def laid():
    """A headstone laid flat for a step: its face up, a border cut round it, the lines of its name, and moss in the cuts."""
    base = STONE[0]; im = noise(base, 4934); rnd = random.Random(4934)
    for i in range(1, 15):
        for x, y in ((i, 1), (i, 14), (1, i), (14, i)):
            put(im, x, y, base, -26)
    for line, (a, b) in enumerate(((4, 12), (5, 11), (4, 12))):
        for x in range(a, b):
            if (x * 5 + line) % 6:
                put(im, x, 5 + line * 3, base, -32)
    for _ in range(7):
        put(im, rnd.randrange(16), rnd.randrange(16), MOSS, rnd.randint(-8, 8))
    return im


def roots():
    """Roots, on one sheet: three swatches of the root's brown, light to dark, and one of earth."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4935); px = im.load()
    for i, colour in enumerate((ROOT, '#6a543c', '#5a4632', EARTH)):
        for y in range(8):
            for x in range(4):
                px[4 * i + x, y] = shade(colour, rnd.randint(-5, 5))
    for i, colour in enumerate((WOOD, '#5e4a36', '#7a6650', '#4e3e2e')):
        for y in range(8, 16):
            for x in range(4):
                px[4 * i + x, y] = shade(colour, rnd.randint(-5, 5) - (6 if x == 0 else 0))
    return im


def strand(parts, x, z, long, i, sway=0):
    """A root hanging from the top of the block: a thin piece, in one of the sheet's browns."""
    u = 4 * i + 1
    parts.append(element([round(x, 2), round(16 - long, 2), round(z, 2)], [round(x + 1, 2), 16, round(z + 1, 2)], {'all': 'r'},
                         {k: [u, 1, u + 1, 2] for k in FACES},
                         {'angle': sway, 'axis': 'z', 'origin': [round(x + .5, 2), 16, round(z + .5, 2)]} if sway else None))


def roots_model(k):
    p = []
    sets = (((2, 3, 9, 0, 0), (6, 9, 13, 1, 22.5), (10, 4, 7, 2, 0), (12, 11, 11, 0, -22.5), (4, 12, 6, 1, 0)),
            ((3, 6, 14, 0, 0), (8, 3, 9, 2, 22.5), (11, 10, 12, 1, 0), (6, 12, 5, 0, 0)),
            ((2, 9, 6, 1, -22.5), (7, 5, 15, 0, 0), (12, 7, 8, 2, 0), (9, 12, 10, 1, 22.5), (4, 3, 4, 0, 0), (13, 2, 5, 2, 0)))
    for x, z, long, i, sway in sets[k]:
        strand(p, x, z, long, i, sway)
    # A clod of the earth they came out of, still on them.
    u = 13
    p.append(element([5.25, 12.5, 5.25], [10.75, 15.5, 10.75], {'all': 'r'}, {f: [u, 1, u + 1, 2] for f in FACES}))
    return p


def board(parts, x, high, i, rot=None):
    """One board of an old fence, on the block's middle line."""
    u = 4 * i + 1
    parts.append(element([x, 0, 7.25], [x + 3, high, 8.75], {'all': 'r'}, {k: [u, 9, u + 1, 10] for k in FACES}, rot))


def picket_model(k):
    p = []
    lean = None if k in (0, 3) else {'angle': 22.5 if k == 1 else -22.5, 'axis': 'z', 'origin': [8, 0, 8]}
    highs = ((15, 13, 16, 14), (14, 16, 13, 15), (16, 13, 15, 14), (6, 9, 4, 7))[k]
    for n, high in enumerate(highs):
        board(p, .5 + 4 * n, high, n, lean)
    if k != 3:                                                               # the rail the boards are nailed to
        u = 9
        p.append(element([0, 8, 8.75], [16, 10, 9.75], {'all': 'r'}, {f: [u, 9, u + 1, 10] for f in FACES}, lean))
    return p


def textures():
    out = {'block/calder_grave_turf.png': grave_turf(), 'block/calder_grave_earth.png': grave_earth(), 'block/calder_marble.png': marble()}
    for k in range(len(STONES)):
        out[DIR + f'stone_{k}.png'] = stone_face(k)
    for k in range(SMALL):
        out[DIR + f'small_{k}.png'] = small_face(k)
    for k in range(4):
        out[DIR + f'side_{k}.png'] = plain(STONE[k], 4898 + k)
        out[DIR + f'door_{k}.png'] = door(k)
    out['block/calder_hollow_sod.png'] = sod_side()
    out['block/calder_hollow_under.png'] = sod_under()
    out['block/calder_vine.png'] = vine()
    out['block/calder_coffin.png'] = coffin()
    out[DIR + 'lid.png'] = lid()
    out[DIR + 'laid.png'] = laid()
    out[DIR + 'roots.png'] = roots()
    out[DIR + 'earth.png'] = grave_earth()
    out[DIR + 'marble.png'] = marble()
    out[DIR + 'tarp.png'] = tarp()
    out[DIR + 'wood.png'] = wood()
    out[DIR + 'goods.png'] = goods()
    return out


# ---------------------------------------------------------------------------------------------------------------- pieces
def piece(parts, lo, hi, face, side, rot=None, back=None):
    """A piece of a stone: its north face shows the part of the face's picture that is where it is, as somebody facing it sees it;
    its back and the rest are plain stone."""
    lo, hi = [round(v, 3) for v in lo], [round(v, 3) for v in hi]
    w, h, d = hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2]
    front = [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]]
    uv = {'north': front, 'south': [lo[0], 16 - hi[1], hi[0], 16 - lo[1]], 'east': [0, 16 - hi[1], d, 16 - lo[1]], 'west': [16 - d, 16 - hi[1], 16, 16 - lo[1]],
          'up': [lo[0], 0, hi[0], d], 'down': [lo[0], 16 - d, hi[0], 16]}
    for k in uv:
        uv[k] = [max(0, min(16, round(v, 3))) for v in uv[k]]
    parts.append(element(lo, hi, {'north': face, 'south': back or side, 'east': side, 'west': side, 'up': side, 'down': side}, uv, rot))


def swatch(parts, lo, hi, i, rot=None):
    """A piece in one colour of the goods' sheet."""
    x, y = 4 * (i % 4) + 1, 4 * (i // 4) + 1
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': 'g'}, {k: [x, y, x + 1, y + 1] for k in FACES}, rot))


LEAN = {'back': {'angle': 22.5, 'axis': 'x', 'origin': [8, 0, 8]}, 'left': {'angle': 22.5, 'axis': 'z', 'origin': [8, 0, 8]},
        'right': {'angle': -22.5, 'axis': 'z', 'origin': [8, 0, 8]}}


def stone(k):
    shape, mat, lean, _, _ = STONES[k]
    p = []; rot = LEAN.get(lean); f, s = 'f', 's'
    if shape == 0:
        piece(p, [2, 0, 6.5], [14, 10, 9.5], f, s, rot); piece(p, [3, 10, 6.5], [13, 12, 9.5], f, s, rot); piece(p, [5, 12, 6.5], [11, 13, 9.5], f, s, rot)
    elif shape == 1:
        piece(p, [3, 0, 6.5], [13, 14, 9.5], f, s, rot); piece(p, [2, 14, 6], [14, 15, 10], f, s, rot)
    elif shape == 2:
        piece(p, [3, 0, 6.5], [13, 10, 9.5], f, s, rot); piece(p, [4, 10, 6.5], [12, 12, 9.5], f, s, rot); piece(p, [6, 12, 6.5], [10, 14, 9.5], f, s, rot)
    else:
        piece(p, [0.5, 0, 5], [15.5, 8, 11], f, s, rot)
    return p


def small(k):
    p = []
    piece(p, [0.5, 0, 5.5], [15.5, 7, 10.5], 'f', 's'); piece(p, [1.5, 7, 5.5], [14.5, 8, 10.5], 'f', 's')
    return p


def cross(k):
    p = []; rot = None if k == 0 else LEAN['left']; t = 'm' if k == 0 else 'w'
    piece(p, [6.5, 2 if k == 0 else 0, 6.5], [9.5, 15, 9.5], t, t, rot); piece(p, [3, 9, 6.6], [13, 12, 9.4], t, t, rot)
    if k == 0:
        piece(p, [4, 0, 5], [12, 2, 11], t, t)
    return p


def obelisk(k):
    p = []; t = 'm'
    if k == 0:
        piece(p, [3, 0, 3], [13, 5, 13], t, t); piece(p, [4.5, 5, 4.5], [11.5, 16, 11.5], t, t)
    elif k == 1:
        piece(p, [5, 0, 5], [11, 16, 11], t, t)
    else:
        piece(p, [5.5, 0, 5.5], [10.5, 9, 10.5], t, t); piece(p, [6.5, 9, 6.5], [9.5, 12, 9.5], t, t); piece(p, [7.5, 12, 7.5], [8.5, 14, 8.5], t, t)
    return p


def angel(k):
    p = []; t = 'm'
    if k == 0:                                                               # the plinth
        piece(p, [1.5, 0, 1.5], [14.5, 3, 14.5], t, t); piece(p, [3, 3, 3], [13, 16, 13], t, t)
    elif k == 1:                                                             # the robe, and the arms folded across it
        piece(p, [4, 0, 5], [12, 6, 11], t, t); piece(p, [4.5, 6, 5.5], [11.5, 16, 10.5], t, t)
        piece(p, [5, 9, 4.5], [11, 11, 5.5], t, t)
    else:                                                                    # shoulders, the head bowed, and the wings behind
        piece(p, [4.5, 0, 5.5], [11.5, 4, 10.5], t, t); piece(p, [6, 4, 5], [10, 9, 9], t, t)
        # The wings: from the hips to over the head, each swept back from the shoulder it springs from.
        left = {'angle': 22.5, 'axis': 'y', 'origin': [5.5, 0, 11]}; right = {'angle': -22.5, 'axis': 'y', 'origin': [10.5, 0, 11]}
        piece(p, [0.5, -8, 10.6], [5.5, 9, 11.6], t, t, left); piece(p, [10.5, -8, 10.6], [15.5, 9, 11.6], t, t, right)
        piece(p, [1.5, 9, 10.6], [5.5, 12, 11.6], t, t, left); piece(p, [10.5, 9, 10.6], [14.5, 12, 11.6], t, t, right)
        piece(p, [5.5, -2, 10.6], [10.5, 5, 11.6], t, t)
    return p


def mound(k):
    """The heap of earth by the open grave: it rises toward the north, in eight lumps of uneven width with clods on them, so
    that it is a heap and not a stair. (A bike rides it as one slope; a body stands on the lumps.)"""
    p = []
    for i in range(MOUND_STEPS):
        piece(p, [MOUND_IN[i], 0, 14 - 2 * i], [16 - MOUND_IN[i], mound_top(i), 16 - 2 * i], 'e', 'e')
    for x, i, s in ((3, 1, 1.4), (10.5, 2, 1.2), (6, 4, 1.4), (11, 5, 1.2), (4.5, 6, 1.2)):
        z = 14 - 2 * i + .4
        piece(p, [x, mound_top(i), z], [x + s, mound_top(i) + 1, z + 1.2], 'e', 'e')
    # Lit evenly: with a top lighter than its front, each lump is a tread and the heap is a stair.
    for e in p:
        e['shade'] = False
    return p


def tarp_model(k):
    p = []
    if k == 0:
        piece(p, [0, 0, 0], [16, .6, 16], 't', 't')
        for x, z in ((0.5, 0.5), (14.5, 0.5), (0.5, 14.5), (14.5, 14.5)):
            swatch(p, [x, .6, z], [x + 1, 2, z + 1], 10)                     # pegs
    else:
        piece(p, [2, 2, 2], [14, 6, 14], 't', 't'); piece(p, [0.5, 0, 0.5], [15.5, 2, 15.5], 't', 't')
    return p


def goods_model(k):
    p = []
    if k == 0:                                                               # flowers in a jar
        swatch(p, [6.5, 0, 6.5], [9.5, 4, 9.5], 0)
        for x, z, h, c in ((7, 7, 9, 2), (8.5, 7.5, 8, 3), (7.5, 8.5, 10, 1), (8.4, 8.6, 7, 11)):
            swatch(p, [x, 4, z], [x + .5, h - 1, z + .5], 4); swatch(p, [x - .5, h - 1, z - .5], [x + 1, h, z + 1], c)
    elif k == 1:                                                             # a lantern, its candle lit
        swatch(p, [6, 0, 6], [10, 1, 10], 5); swatch(p, [6.5, 1, 6.5], [9.5, 6, 9.5], 0); swatch(p, [6, 6, 6], [10, 7, 10], 5)
        swatch(p, [7.5, 7, 7.5], [8.5, 8.5, 8.5], 5)
        e = element([7.4, 1.5, 7.4], [8.6, 4, 8.6], {'all': 'g'}, {f: [9, 5, 10, 6] for f in FACES})
        e['shade'] = False; e['neoforge_data'] = {'block_light': 15, 'sky_light': 15}; p.append(e)
    elif k == 2:                                                             # a wreath on a wire stand
        swatch(p, [7.5, 0, 9], [8.5, 9, 9.6], 12)
        for x0, y0, x1, y1 in ((4, 9, 12, 11), (4, 3, 12, 5), (4, 5, 6, 9), (10, 5, 12, 9)):
            swatch(p, [x0, y0, 7.6], [x1, y1, 9], 7)
        swatch(p, [7, 3.2, 7.2], [9, 6, 7.6], 8)
    else:                                                                    # a small flag on a stick
        swatch(p, [7.6, 0, 7.6], [8.4, 11, 8.4], 10); swatch(p, [8.4, 7, 7.9], [13, 10.5, 8.1], 9); swatch(p, [8.5, 9.1, 7.85], [10.5, 10.4, 8.15], 8)
    return p


def door_model(k):
    p = []
    parts = element([0, 0, 14.5], [16, 16, 16], {'north': 'd', 'east': 'i', 'west': 'i', 'up': 'i', 'down': 'i'},
                    {'north': [0, 0, 16, 16], 'east': [0, 0, 1.5, 16], 'west': [0, 0, 1.5, 16], 'up': [0, 0, 16, 1.5], 'down': [0, 0, 16, 1.5]})
    p.append(parts)
    return p


FITTINGS = {
    **{f'stone_{k}': ((lambda k=k: stone(k)), {'f': f'stone_{k}', 's': f'side_{STONES[k][1]}'}) for k in range(len(STONES))},
    **{f'small_{k}': ((lambda k=k: small(k)), {'f': f'small_{k}', 's': f'side_{(1, 0, 1, 3, 0, 1)[k]}'}) for k in range(SMALL)},
    'cross_0': (lambda: cross(0), {'m': 'side_0'}), 'cross_1': (lambda: cross(1), {'w': 'wood'}),
    **{f'obelisk_{k}': ((lambda k=k: obelisk(k)), {'m': 'marble'}) for k in range(3)},
    **{f'angel_{k}': ((lambda k=k: angel(k)), {'m': 'marble'}) for k in range(3)},
    'mound_0': (lambda: mound(0), {'e': 'earth'}),
    'tarp_0': (lambda: tarp_model(0), {'t': 'tarp', 'g': 'goods'}), 'tarp_1': (lambda: tarp_model(1), {'t': 'tarp'}),
    **{f'goods_{k}': ((lambda k=k: goods_model(k)), {'g': 'goods'}) for k in range(4)},
    **{f'door_{k}': ((lambda k=k: door_model(k)), {'d': f'door_{k}', 'i': 'side_1'}) for k in range(4)},
    **{f'roots_{k}': ((lambda k=k: roots_model(k)), {'r': 'roots'}) for k in range(3)},
    **{f'picket_{k}': ((lambda k=k: picket_model(k)), {'r': 'roots'}) for k in range(4)},
}
KINDS = ['stone', 'small', 'cross', 'obelisk', 'angel', 'mound', 'tarp', 'goods', 'door', 'roots', 'picket']
STAGES = {'stone': len(STONES), 'small': SMALL, 'cross': 2, 'obelisk': 3, 'angel': 3, 'mound': 1, 'tarp': 2, 'goods': 4, 'door': 4, 'roots': 3, 'picket': 4}
MAX_STAGE = 16
MATERIALS = ('grave_turf', 'grave_earth', 'marble', 'vine', 'coffin')
# A half block: what its top and bottom are drawn with, and its sides.
SLABS = {'grave_turf_slab': ('calder_grave_turf', 'calder_grave_turf'), 'gravel_slab': ('calder_gravel', 'calder_gravel'),
         'vine_slab': ('calder_vine', 'calder_vine'), 'coffin_lid': ('calder_grave/lid', 'calder_coffin'), 'stone_step': ('calder_grave/laid', 'calder_grave/side_0')}


def model(parts, names):
    keys = {f['texture'][1:] for e in parts for f in e['faces'].values()}
    first = sorted(keys)[0]
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: P + names[k] for k in sorted(keys)}, 'particle': P + names[first]}, 'elements': parts}


def fitting_models():
    return {key: model(build(), names) for key, (build, names) in FITTINGS.items()}


def files():
    out = {}
    for name in MATERIALS:
        out[f'models/block/calder_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/calder_{name}'}}
        out[f'blockstates/calder_{name}.json'] = {'variants': {'': {'model': NS + f'block/calder_{name}'}}}
    # The sod an island is made of: grass on top, cut earth with roots at its sides, root ends underneath.
    out['models/block/calder_hollow_sod.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
        'top': NS + 'block/calder_grave_turf', 'side': NS + 'block/calder_hollow_sod', 'bottom': NS + 'block/calder_hollow_under'}}
    out['blockstates/calder_hollow_sod.json'] = {'variants': {'': {'model': NS + 'block/calder_hollow_sod'}}}
    for name, (flat, side) in SLABS.items():
        t = {'bottom': NS + 'block/' + flat, 'top': NS + 'block/' + flat, 'side': NS + 'block/' + side}
        out[f'models/block/calder_{name}.json'] = {'parent': 'minecraft:block/slab', 'textures': t}
        out[f'models/block/calder_{name}_top.json'] = {'parent': 'minecraft:block/slab_top', 'textures': t}
        out[f'models/block/calder_{name}_double.json'] = {'parent': 'minecraft:block/cube_bottom_top', 'textures': t}
        out[f'blockstates/calder_{name}.json'] = {'variants': {'type=bottom': {'model': NS + f'block/calder_{name}'}, 'type=top': {'model': NS + f'block/calder_{name}_top'},
                                                               'type=double': {'model': NS + f'block/calder_{name}_double'}}}
    for key, obj in fitting_models().items():
        out[f'models/{DIR}{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES[kind] else 0
                v = {'model': f'{P}{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/calder_grave.json'] = {'variants': variants}
    return out


NAMES = {'block.the_oldest_house.calder_grave': 'Grave Fitting', 'block.the_oldest_house.calder_grave_turf': 'Cemetery Grass',
         'block.the_oldest_house.calder_grave_earth': 'Grave Earth', 'block.the_oldest_house.calder_marble': 'Marble',
         'block.the_oldest_house.calder_grave_turf_slab': 'Cemetery Grass Step', 'block.the_oldest_house.calder_gravel_slab': 'Gravel Step',
         'block.the_oldest_house.calder_hollow_sod': 'Sod', 'block.the_oldest_house.calder_vine': 'Vine', 'block.the_oldest_house.calder_coffin': 'Coffin Wood',
         'block.the_oldest_house.calder_vine_slab': 'Vine Step', 'block.the_oldest_house.calder_coffin_lid': 'Coffin Lid',
         'block.the_oldest_house.calder_stone_step': 'Laid Headstone'}


def review(path):
    from draw_bike_night_art import seen
    images = textures(); scale = 12; cells = []
    for key, (build, names) in FITTINGS.items():
        for view in ('front', 'side'):
            cells.append(seen(build(), {k: 'calder_grave/' + v for k, v in names.items()}, images, view, scale, (-4, 20, -8, 18), 'block/'))
    w, h = cells[0].size
    cols = 12
    rows = (len(cells) + cols - 1) // cols
    page = Image.new('RGBA', (cols * (w + 4) + 4, rows * (h + 4) + 4 + 72), (30, 32, 36, 255))
    for i, one in enumerate(cells):
        page.alpha_composite(one, (4 + (i % cols) * (w + 4), 4 + (i // cols) * (h + 4)))
    x = 4
    for rel, im in sorted(images.items()):
        if x + 68 < page.width:
            page.alpha_composite(im.resize((64, 64), Image.NEAREST), (x, page.height - 68)); x += 68
    page.save(path)


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--check', action='store_true')
    args.add_argument('--sheet')
    args = args.parse_args()
    if args.sheet:
        review(args.sheet); return
    check = args.check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES[kind]):
            assert f'{kind}_{stage}' in models, (kind, stage)
    assert len({im.tobytes() for rel, im in images.items() if 'small_' in rel}) == SMALL, 'two small stones are the same'
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert tex.split(':')[1] + '.png' in images, (key, tex)
        for e in obj['elements']:
            for v in e['from'] + e['to']:
                assert -16 <= v <= 32, (key, e['from'], e['to'])
        clash = fights(obj['elements'])
        assert not clash, (key, [(obj['elements'][a]['from'], obj['elements'][a]['to'], fa, obj['elements'][b]['from'], obj['elements'][b]['to'], fb, n)
                                 for a, fa, b, fb, n in clash[:4]])
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
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" Calder Hill: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
