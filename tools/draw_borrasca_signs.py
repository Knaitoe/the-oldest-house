#!/usr/bin/env python3
"""0.4.449: Drisking's painted signs (the owner, of the shops: "Give the different stores some variance/characterization in their
signage"; of the historical society's: "Give it an actual sign"; of the trailhead and the rooms under the depot: "custom signs").
Draws the pictures of the_oldest_house:borrasca_sign, its models and its states.

A sign is one picture, sixteen pixels to the block, lettered in a five-by-seven alphabet or a three-by-five one. A large sign is
hung as a grid of blocks, each showing its own sixteen-pixel square of the picture; a small one is a single block whose board may
reach past it (a name plate centred on a post, a road sign wider than the post it is nailed to). A sign is mounted one of four ways:
on the wall behind it; as lettering painted straight onto what is behind it (the trucks' tailgates); inside a window, against the
glass; or hanging free, read from both sides. Neon has a second picture of its tubes alone, drawn full bright a quarter of a
pixel in front (far enough that the two never flicker against each other at a distance).

The order and size of the signs is BorrascaSigns.Sign's; --check compares the two, and verifies every file without writing any.
"""
from pathlib import Path
import argparse
import json
import random
import re
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/BorrascaSigns.java'
MOD = 'the_oldest_house'
DIR = 'borrasca_sign'

# ---------------------------------------------------------------------------------------------------------------- the alphabets
BIG = {
    'A': ['.###.', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'], 'B': ['####.', '#...#', '#...#', '####.', '#...#', '#...#', '####.'],
    'C': ['.###.', '#...#', '#....', '#....', '#....', '#...#', '.###.'], 'D': ['####.', '#...#', '#...#', '#...#', '#...#', '#...#', '####.'],
    'E': ['#####', '#....', '#....', '####.', '#....', '#....', '#####'], 'F': ['#####', '#....', '#....', '####.', '#....', '#....', '#....'],
    'G': ['.###.', '#...#', '#....', '#.###', '#...#', '#...#', '.###.'], 'H': ['#...#', '#...#', '#...#', '#####', '#...#', '#...#', '#...#'],
    'I': ['.###.', '..#..', '..#..', '..#..', '..#..', '..#..', '.###.'], 'J': ['..###', '...#.', '...#.', '...#.', '...#.', '#..#.', '.##..'],
    'K': ['#...#', '#..#.', '#.#..', '##...', '#.#..', '#..#.', '#...#'], 'L': ['#....', '#....', '#....', '#....', '#....', '#....', '#####'],
    'M': ['#...#', '##.##', '#.#.#', '#.#.#', '#...#', '#...#', '#...#'], 'N': ['#...#', '##..#', '#.#.#', '#..##', '#...#', '#...#', '#...#'],
    'O': ['.###.', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'], 'P': ['####.', '#...#', '#...#', '####.', '#....', '#....', '#....'],
    'Q': ['.###.', '#...#', '#...#', '#...#', '#.#.#', '#..#.', '.##.#'], 'R': ['####.', '#...#', '#...#', '####.', '#.#..', '#..#.', '#...#'],
    'S': ['.####', '#....', '#....', '.###.', '....#', '....#', '####.'], 'T': ['#####', '..#..', '..#..', '..#..', '..#..', '..#..', '..#..'],
    'U': ['#...#', '#...#', '#...#', '#...#', '#...#', '#...#', '.###.'], 'V': ['#...#', '#...#', '#...#', '#...#', '#...#', '.#.#.', '..#..'],
    'W': ['#...#', '#...#', '#...#', '#.#.#', '#.#.#', '##.##', '#...#'], 'X': ['#...#', '#...#', '.#.#.', '..#..', '.#.#.', '#...#', '#...#'],
    'Y': ['#...#', '#...#', '.#.#.', '..#..', '..#..', '..#..', '..#..'], 'Z': ['#####', '....#', '...#.', '..#..', '.#...', '#....', '#####'],
    '0': ['.###.', '#...#', '#..##', '#.#.#', '##..#', '#...#', '.###.'], '1': ['..#..', '.##..', '..#..', '..#..', '..#..', '..#..', '.###.'],
    '2': ['.###.', '#...#', '....#', '...#.', '..#..', '.#...', '#####'], '3': ['####.', '....#', '....#', '.###.', '....#', '....#', '####.'],
    '4': ['...#.', '..##.', '.#.#.', '#..#.', '#####', '...#.', '...#.'], '5': ['#####', '#....', '####.', '....#', '....#', '#...#', '.###.'],
    '6': ['.###.', '#....', '#....', '####.', '#...#', '#...#', '.###.'], '7': ['#####', '....#', '...#.', '..#..', '..#..', '..#..', '..#..'],
    '8': ['.###.', '#...#', '#...#', '.###.', '#...#', '#...#', '.###.'], '9': ['.###.', '#...#', '#...#', '.####', '....#', '....#', '.###.'],
    '&': ['.##..', '#..#.', '#..#.', '.##..', '#.#.#', '#..#.', '.##.#'], "'": ['..#..', '..#..', '.....', '.....', '.....', '.....', '.....'],
    '.': ['.....', '.....', '.....', '.....', '.....', '.##..', '.##..'], ',': ['.....', '.....', '.....', '.....', '.##..', '..#..', '.#...'],
    '-': ['.....', '.....', '.....', '.###.', '.....', '.....', '.....'],
}
SMALL = {
    'A': '010101111101101', 'B': '110101110101110', 'C': '011100100100011', 'D': '110101101101110', 'E': '111100110100111',
    'F': '111100110100100', 'G': '011100101101011', 'H': '101101111101101', 'I': '111010010010111', 'J': '001001001101010',
    'K': '101101110101101', 'L': '100100100100111', 'M': '101111111101101', 'N': '101111111111101', 'O': '010101101101010',
    'P': '110101110100100', 'Q': '010101101110011', 'R': '110101110110101', 'S': '011100010001110', 'T': '111010010010010',
    'U': '101101101101111', 'V': '101101101101010', 'W': '101101101111101', 'X': '101101010101101', 'Y': '101101010010010',
    'Z': '111001010100111', '0': '111101101101111', '1': '010110010010111', '2': '110001010100111', '3': '110001010001110',
    '4': '101101111001001', '5': '111100110001110', '6': '111100111101111', '7': '111001010010010', '8': '111101111101111',
    '9': '111101111001110', '.': '000000000000010', ',': '000000000010100', '-': '000000111000000', "'": '010010000000000',
    '&': '010101010101011', ':': '000010000010000',
}


def glyph(ch, big):
    """A letter as columns of ink, its empty edges trimmed away; a space is three columns wide in the large alphabet and two in the small."""
    if ch == ' ':
        return [[False] * (7 if big else 5)] * (3 if big else 2)
    if big:
        cols = [[BIG[ch][y][x] == '#' for y in range(7)] for x in range(5)]
    else:
        cols = [[SMALL[ch][y * 3 + x] == '1' for y in range(5)] for x in range(3)]
    while not any(cols[0]):
        cols.pop(0)
    while not any(cols[-1]):
        cols.pop()
    return cols


def measure(word, big=False, scale=1):
    return (sum(len(glyph(c, big)) for c in word) + len(word) - 1) * scale


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def tone(c, k=0, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def write(im, x, y, word, colour, big=False, scale=1, shadow=None, rnd=None, wobble=None, flake=0.0, jitter=0):
    """Letters in paint. With a shadow every letter's shadow is laid first, so that none falls across its neighbour."""
    ink = []
    for i, ch in enumerate(word):
        cols = glyph(ch, big)
        dy = wobble[i % len(wobble)] if wobble else 0
        for cx, col in enumerate(cols):
            for cy, on in enumerate(col):
                if on:
                    for sx in range(scale):
                        for sy in range(scale):
                            ink.append((x + cx * scale + sx, y + dy + cy * scale + sy))
        x += (len(cols) + 1) * scale
    px = im.load()
    w, h = im.size
    if shadow:
        for X, Y in ink:
            if 0 <= X + 1 < w and 0 <= Y + 1 < h:
                px[X + 1, Y + 1] = tone(shadow)
    for X, Y in ink:
        if 0 <= X < w and 0 <= Y < h and not (rnd and flake and rnd.random() < flake):
            px[X, Y] = tone(colour, rnd.randint(-jitter, jitter) if rnd and jitter else 0)
    return ink


def centre(im, y, word, colour, big=False, scale=1, x0=0, x1=None, **kw):
    x1 = im.size[0] if x1 is None else x1
    return write(im, x0 + (x1 - x0 - measure(word, big, scale)) // 2, y, word, colour, big, scale, **kw)


def canvas(w, h):
    return Image.new('RGBA', (w, h), (0, 0, 0, 0))


def field(im, box, colour, rnd, spread=3, planks=0, upright=False):
    """A painted field: flat colour with a little grain, and seams where it is boards."""
    x0, y0, x1, y1 = box
    px = im.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            k = rnd.randint(-spread, spread)
            if planks:
                along, across = (y - y0, x - x0) if upright else (x - x0, y - y0)
                board = across // planks
                k += ((board * 37) % 5 - 2) * 3
                if across % planks == planks - 1:
                    k -= 16
                elif (along + board * 11) % 29 == 0:
                    k -= 9
            px[x, y] = tone(colour, k)


def line(im, x0, y0, x1, y1, colour):
    px = im.load()
    for y in range(y0, y1):
        for x in range(x0, x1):
            if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
                px[x, y] = tone(colour)


def border(im, box, colour, inset=0):
    x0, y0, x1, y1 = box[0] + inset, box[1] + inset, box[2] - inset, box[3] - inset
    line(im, x0, y0, x1, y0 + 1, colour); line(im, x0, y1 - 1, x1, y1, colour)
    line(im, x0, y0, x0 + 1, y1, colour); line(im, x1 - 1, y0, x1, y1, colour)


def stamp(im, x, y, rows, colours):
    px = im.load()
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in colours and 0 <= x + dx < im.size[0] and 0 <= y + dy < im.size[1]:
                px[x + dx, y + dy] = tone(colours[ch])


def corners(im, r):
    """Round the board's corners off: r pixels cut from each, in steps."""
    w, h = im.size
    for i in range(r):
        for j in range(r - i):
            for x, y in ((i, j), (w - 1 - i, j), (i, h - 1 - j), (w - 1 - i, h - 1 - j)):
                im.putpixel((x, y), (0, 0, 0, 0))


def worn(im, box, rnd, colour, count, edge=True):
    """Rust and dirt, gathered at the edges."""
    x0, y0, x1, y1 = box
    px = im.load()
    for _ in range(count):
        if edge:
            side = rnd.randint(0, 3)
            x = rnd.randint(x0, x1 - 1) if side < 2 else (x0 + rnd.randint(0, 1) if side == 2 else x1 - 1 - rnd.randint(0, 1))
            y = (y0 + rnd.randint(0, 1) if side == 0 else y1 - 1 - rnd.randint(0, 1)) if side < 2 else rnd.randint(y0, y1 - 1)
        else:
            x, y = rnd.randint(x0, x1 - 1), rnd.randint(y0, y1 - 1)
        if px[x, y][3]:
            px[x, y] = tone(colour, rnd.randint(-10, 10))


def bolts(im, box, colour, inset=1):
    x0, y0, x1, y1 = box
    for x, y in ((x0 + inset, y0 + inset), (x1 - 1 - inset, y0 + inset), (x0 + inset, y1 - 1 - inset), (x1 - 1 - inset, y1 - 1 - inset)):
        im.putpixel((x, y), tone(colour))


STAR = ['....#....', '....#....', '...###...', '#########', '.#######.', '..#####..', '..#####..', '.###.###.', '.#.....#.']
PICKS = ['##.....##', '.##...##.', '#.##.##.#', '...###...', '....#....', '...#.#...', '..#...#..', '.#.....#.', '#.......#']
CUP = ['..~.~....', '.~.~.....', '..~.~....', '.........', '######...', '######.#.', '######.#.', '.####.#..', '########.']
PIE = ['#........', '###......', '#####....', '#######..', '#########', 'ooooooooo']
PINE = ['...#...', '..###..', '...#...', '..###..', '.#####.', '..###..', '.#####.', '#######', '...|...']
MORTAR = ['.......#.', '......##.', '.....##..', '#########', '#########', '.#######.', '.#######.', '..#####..', '.#######.']
GLASS = ['#######', '.#####.', '..###..', '...#...', '...#...', '...#...', '..###..', '.#####.']
BELL = ['..#..', '.###.', '.###.', '.###.', '#####', '..#..']
DIAMOND = ['..#..', '.###.', '#####', '.###.', '..#..']


# -------------------------------------------------------------------------------------------------------------------- the signs
def diner(rnd):
    """Painted on the false front: HILLTOP in tall red letters with a shadow, CAFE under it between a cup and a slice of pie."""
    im = canvas(96, 32)
    field(im, (0, 0, 96, 32), '#efe3c4', rnd, 3, 8)
    border(im, (0, 0, 96, 32), '#a8231b'); border(im, (0, 0, 96, 32), '#a8231b', 1)
    centre(im, 4, 'HILLTOP', '#b3261e', True, 2, shadow='#5e120d', rnd=rnd, jitter=5)
    centre(im, 21, 'CAFE', '#2c1c14', True, rnd=rnd, jitter=4)
    stamp(im, 20, 20, CUP, {'#': '#2c1c14', '~': '#a89c84'}); stamp(im, 67, 22, PIE, {'#': '#b3261e', 'o': '#c98a3c'})
    worn(im, (0, 0, 96, 32), rnd, '#c9b890', 60)
    corners(im, 2)
    return im, None


def antiques(rnd):
    """Bottle green, gilt letters, a gilt line round it."""
    im = canvas(96, 16)
    field(im, (0, 0, 96, 16), '#1f3b2c', rnd, 2)
    border(im, (0, 0, 96, 16), '#b8923a', 1)
    centre(im, 4, "ROWE'S ANTIQUES", '#dcb651', True, shadow='#0f2018', rnd=rnd, jitter=6)
    for x in (4, 91):
        im.putpixel((x, 7), tone('#dcb651')); im.putpixel((x, 8), tone('#dcb651'))
    worn(im, (0, 0, 96, 16), rnd, '#16291f', 30)
    corners(im, 1)
    return im, None


def station(rnd):
    """White enamel between two red bands, blue letters, a red diamond at each end, rust at the bolts."""
    im = canvas(96, 16)
    field(im, (0, 0, 96, 16), '#f0efe6', rnd, 2)
    line(im, 0, 0, 96, 2, '#c4291c'); line(im, 0, 14, 96, 16, '#c4291c')
    centre(im, 4, 'RIDGE SERVICE', '#1f4e9e', True, rnd=rnd, jitter=4)
    stamp(im, 4, 5, DIAMOND, {'#': '#c4291c'}); stamp(im, 87, 5, DIAMOND, {'#': '#c4291c'})
    bolts(im, (0, 2, 96, 14), '#6b6b66')
    worn(im, (0, 0, 96, 16), rnd, '#8a5632', 26)
    return im, None


def doctor(rnd):
    """A white board with a thin black line round it, plain black capitals."""
    im = canvas(96, 16)
    field(im, (0, 0, 96, 16), '#f4f1e6', rnd, 2)
    border(im, (0, 0, 96, 16), '#1c1c1c', 1)
    centre(im, 4, 'R. HALE, M.D.', '#1c1c1c', True)
    for x in (7, 84):
        stamp(im, x, 5, DIAMOND, {'#': '#27406b'})
    return im, None


def drug(rnd):
    """A teal band lettered MAIN STREET over a cream field with DRUG in tall letters, a mortar to one side and the Rx to the other."""
    im = canvas(96, 32)
    field(im, (0, 0, 96, 32), '#efe7d2', rnd, 2)
    field(im, (0, 0, 96, 11), '#1d6f72', rnd, 2)
    border(im, (0, 0, 96, 32), '#1d6f72')
    centre(im, 2, 'MAIN STREET', '#f3ecd8', True)
    centre(im, 14, 'DRUG', '#1d6f72', True, 2, shadow='#b3261e')
    stamp(im, 9, 17, MORTAR, {'#': '#b3261e'})
    write(im, 77, 16, 'R', '#b3261e', True)
    for x, y in ((81, 21), (82, 22), (83, 23), (83, 21), (81, 23)):
        im.putpixel((x + 1, y + 1), tone('#b3261e'))
    worn(im, (0, 11, 96, 32), rnd, '#cfc3a4', 34)
    return im, None


def tipple(rnd):
    """A dark board and pink tube letters, a blue glass beside them and a blue line under. The tubes are a second picture."""
    im = canvas(96, 16)
    field(im, (0, 0, 96, 16), '#191317', rnd, 2)
    border(im, (0, 0, 96, 16), '#3a1f28')
    glow = canvas(96, 16)
    centre(glow, 3, 'THE TIPPLE', '#ff6fae', True)
    stamp(glow, 7, 4, GLASS, {'#': '#6fe3ff'}); stamp(glow, 82, 4, GLASS, {'#': '#6fe3ff'})
    line(glow, 19, 12, 77, 13, '#6fe3ff')
    gp, px = glow.load(), im.load()
    for y in range(16):
        for x in range(96):
            if gp[x, y][3]:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    X, Y = x + dx, y + dy
                    if 0 <= X < 96 and 0 <= Y < 16 and not gp[X, Y][3]:
                        r, g, b = gp[x, y][:3]
                        px[X, Y] = (r // 3, g // 4, b // 3, 255)
    for y in range(16):
        for x in range(96):
            if gp[x, y][3]:
                px[x, y] = gp[x, y]
    return im, glow


def store(rnd):
    """Barn red boards, white block letters flaking off: the name, and FEED & HARDWARE under a rule, with the year at the ends."""
    im = canvas(144, 32)
    field(im, (0, 0, 144, 32), '#8f2e22', rnd, 4, 8)
    border(im, (0, 0, 144, 32), '#f1e6d0')
    centre(im, 4, 'DRISKING GENERAL STORE', '#f3ead8', True, shadow='#4a140e', rnd=rnd, flake=.02, jitter=6)
    line(im, 10, 15, 134, 16, '#f1e6d0')
    centre(im, 20, 'FEED & HARDWARE', '#f3ead8', True, shadow='#4a140e', rnd=rnd, flake=.02, jitter=6)
    write(im, 8, 21, 'EST.', '#f3ead8'); write(im, 121, 21, '1961', '#f3ead8')
    worn(im, (0, 0, 144, 32), rnd, '#6d2219', 110, False)
    return im, None


def cold(rnd):
    """A blue tin sign: COLD in white with snow on it, DRINKS under."""
    im = canvas(32, 16)
    field(im, (0, 0, 32, 16), '#1f5fa8', rnd, 2)
    border(im, (0, 0, 32, 16), '#174a86')
    centre(im, 1, 'COLD', '#f4f7fa', True)
    centre(im, 10, 'DRINKS', '#ffd84a')
    worn(im, (0, 0, 32, 16), rnd, '#7a4a2a', 8)
    corners(im, 2)
    return im, None


def sheriff(rnd):
    """Dark green with gold: SHERIFF between two stars."""
    im = canvas(80, 16)
    field(im, (0, 0, 80, 16), '#1f3327', rnd, 2)
    border(im, (0, 0, 80, 16), '#b8923a', 1)
    centre(im, 4, 'SHERIFF', '#e0b944', True, shadow='#0f1a13')
    stamp(im, 6, 3, STAR, {'#': '#e0b944'}); stamp(im, 65, 3, STAR, {'#': '#e0b944'})
    return im, None


def society(rnd):
    """A cream board with a double line of oxblood: DRISKING between crossed picks, HISTORICAL SOCIETY, and when it opens."""
    im = canvas(80, 32)
    field(im, (0, 0, 80, 32), '#e9dfc4', rnd, 3)
    border(im, (0, 0, 80, 32), '#6e1f1a'); border(im, (0, 0, 80, 32), '#6e1f1a', 2)
    centre(im, 4, 'DRISKING', '#6e1f1a', True)
    stamp(im, 7, 4, PICKS, {'#': '#3a2a1c'}); stamp(im, 64, 4, PICKS, {'#': '#3a2a1c'})
    centre(im, 14, 'HISTORICAL SOCIETY', '#2a1c14')
    line(im, 12, 20, 68, 21, '#6e1f1a')
    centre(im, 22, 'OPEN SATURDAYS', '#6e1f1a')
    corners(im, 3)
    return im, None


def hardware(rnd):
    """Yellow, black block letters, a nail head in each corner."""
    im = canvas(64, 16)
    field(im, (0, 0, 64, 16), '#e2b93b', rnd, 3)
    border(im, (0, 0, 64, 16), '#1c1a14')
    centre(im, 4, 'HARDWARE', '#1c1a14', True)
    bolts(im, (0, 0, 64, 16), '#5c5648', 2)
    worn(im, (0, 0, 64, 16), rnd, '#b08f2a', 22, False)
    return im, None


def lost(rnd):
    """A piece of box card taped up, lettered in marker."""
    im = canvas(32, 16)
    field(im, (1, 1, 31, 15), '#c9a878', rnd, 4)
    write(im, 4, 2, 'LOST', '#2a2018'); write(im, 4, 9, 'FOUND', '#2a2018')
    write(im, 23, 3, '&', '#7a1f1a', True)
    for x, y in ((1, 1), (28, 1), (1, 13), (28, 13)):
        line(im, x, y, x + 3, y + 2, '#e8e2c8')
    return im, None


def trail(rnd):
    """The county's routed board at the station: the trail's name with a pine either side, and how far to the Tree and the marker."""
    im = canvas(64, 32)
    field(im, (0, 0, 64, 32), '#5b3a22', rnd, 4, 8)
    centre(im, 1, 'OZARK PINE', '#ead9b0', True)
    centre(im, 9, 'TRAIL', '#ead9b0', True)
    stamp(im, 6, 8, PINE, {'#': '#9cc47a', '|': '#ead9b0'}); stamp(im, 51, 8, PINE, {'#': '#9cc47a', '|': '#ead9b0'})
    line(im, 3, 17, 61, 18, '#e2b93b')
    write(im, 4, 20, 'TRIPLE TREE', '#ead9b0'); write(im, 57, 20, '1', '#e2b93b')
    write(im, 4, 26, 'MARKER TWO', '#ead9b0'); write(im, 57, 26, '2', '#e2b93b')
    for y in (22, 28):
        for x in range(49, 55, 2):
            im.putpixel((x, y), tone('#8a6a44'))
    return im, None


def arch(rnd):
    """The board on the beam where the street ends: the trail's name burned into a plank, a pine at each end."""
    im = canvas(112, 16)
    field(im, (0, 0, 112, 16), '#9a7646', rnd, 4, 16)
    centre(im, 4, 'OZARK PINE TRAIL', '#2e1d10', True, rnd=rnd, jitter=5)
    stamp(im, 2, 3, PINE, {'#': '#2e1d10', '|': '#2e1d10'}); stamp(im, 103, 3, PINE, {'#': '#2e1d10', '|': '#2e1d10'})
    line(im, 0, 0, 112, 1, '#6e5330'); line(im, 0, 15, 112, 16, '#6e5330')
    return im, None


def fort(rnd):
    """Three scrap boards nailed together and painted by children: the paint runs, and no two letters stand level."""
    im = canvas(48, 32)
    for i, (y0, y1, c) in enumerate(((0, 8, '#8a6a3c'), (8, 23, '#74552e'), (23, 32, '#93744a'))):
        field(im, (0 if i != 1 else 1, y0, 48 if i != 2 else 46, y1), c, rnd, 5)
        line(im, 0, y1 - 1, 48, y1, '#4a3418')
    centre(im, 2, 'TRIPLE TREE', '#f2ecd8')
    centre(im, 8, 'FORT', '#ffd84a', True, 2, shadow='#3a2812', wobble=[0, 1, 0, 1], rnd=rnd, jitter=8)
    for x in (8, 20, 33):
        for y in range(23, 23 + rnd.randint(1, 2)):
            im.putpixel((x, y), tone('#ffd84a'))
    centre(im, 26, 'NO GROWN-UPS', '#f2ecd8')
    for x, y in ((2, 3), (45, 4), (3, 14), (44, 17), (2, 27), (43, 28)):
        im.putpixel((x, y), tone('#2a2018'))
    return im, None


def depot(rnd):
    """A white enamel plate: the depot's name, OFFICE and a bell."""
    im = canvas(48, 16)
    field(im, (0, 0, 48, 16), '#eeeee6', rnd, 2)
    border(im, (0, 0, 48, 16), '#23231f')
    centre(im, 2, 'RIDGE DEPOT', '#23231f')
    write(im, 8, 9, 'OFFICE', '#23231f')
    stamp(im, 35, 8, BELL, {'#': '#23231f'})
    worn(im, (0, 0, 48, 16), rnd, '#8a5632', 18)
    return im, None


def no_entry(rnd):
    """Red tin with a white line round it: NO, ENTRY."""
    im = canvas(32, 32)
    field(im, (0, 0, 32, 32), '#c4291c', rnd, 3)
    border(im, (0, 0, 32, 32), '#8f1d14')
    worn(im, (0, 0, 32, 32), rnd, '#7a4a2a', 26)
    centre(im, 4, 'NO', '#f4f1e6', True, 2)
    centre(im, 21, 'ENTRY', '#f4f1e6', True)
    corners(im, 2)
    return im, None


def church(rnd):
    """White boards, black letters: the church, the hour, and that all are welcome."""
    im = canvas(64, 32)
    field(im, (0, 0, 64, 32), '#f1eee4', rnd, 3, 8)
    border(im, (0, 0, 64, 32), '#2a2622')
    centre(im, 2, 'FIRST BAPTIST', '#1c1a18')
    centre(im, 8, 'CHURCH', '#1c1a18', True)
    line(im, 8, 16, 56, 17, '#2a2622')
    centre(im, 18, 'SUNDAY 10 AM', '#1c1a18')
    centre(im, 24, 'ALL WELCOME', '#7a1f1a')
    return im, None


def service(rnd):
    """Green and white: SERVICE over EXIT."""
    im = canvas(48, 16)
    field(im, (0, 0, 48, 16), '#1f7a3d', rnd, 2)
    border(im, (0, 0, 48, 16), '#175c2e')
    centre(im, 1, 'SERVICE', '#eef3ea')
    centre(im, 8, 'EXIT', '#eef3ea', True)
    return im, None


def county(rnd):
    """Stencilled on the county pickup's tailgate in dark green, no board under it."""
    im = canvas(48, 16)
    centre(im, 5, 'COUNTY 17', '#1f3327', x0=10)
    stamp(im, 2, 3, STAR, {'#': '#1f3327'})
    return im, None


def prescott(rnd):
    """Lettered on Prescott's tailgate in white, with the firm's mark, on a band of dark paint between the lamps.
    0.4.461: the band, and the mark made small enough to stand inside it. The name ran across the pickup's licence plate, white
    on white, so its middle could not be read, and the mark lay over the left lamp."""
    im = canvas(48, 16)
    px = im.load()
    for x in range(5, 43):
        for y in range(4, 11):
            px[x, y] = tone('#18221a', (x * 7 + y * 13) % 3 - 1)
    write(im, 11, 5, 'PRESCOTT', '#f1eee4')
    stamp(im, 6, 6, ['.#.', '###', '.#.'], {'#': '#e2b93b'})
    return im, None


def arch_mile(rnd):
    """A plank on two chains under the arch's beam, the same on both sides."""
    im = canvas(48, 16)
    field(im, (0, 3, 48, 16), '#9a7646', rnd, 4)
    border(im, (0, 3, 48, 16), '#6e5330')
    for x in (6, 41):
        for y in range(3):
            im.putpixel((x, y), tone('#55585c' if y % 2 else '#8e9296'))
    centre(im, 4, 'TRIPLE TREE', '#2e1d10')
    centre(im, 10, '1 MILE', '#2e1d10')
    return im, None


def roundel(rnd):
    """The red disc with the white bar."""
    im = canvas(16, 16)
    px = im.load()
    for y in range(16):
        for x in range(16):
            d = (x - 7.5) ** 2 + (y - 7.5) ** 2
            if d <= 56:
                px[x, y] = tone('#c4291c' if d <= 44 else '#f4f1e6', rnd.randint(-3, 3))
    line(im, 3, 6, 13, 10, '#f4f1e6')
    worn(im, (1, 1, 15, 15), rnd, '#7a4a2a', 5, False)
    return im, None


def refining(rnd):
    """Prescott's board on the service road, wider than the post it is nailed to: white, the name in black, NO ENTRY in red."""
    im = canvas(48, 16)
    field(im, (4, 0, 44, 16), '#eeeee6', rnd, 2)
    border(im, (4, 0, 44, 16), '#23231f')
    centre(im, 2, 'PRESCOTT', '#23231f')
    centre(im, 9, 'NO ENTRY', '#c4291c')
    bolts(im, (4, 0, 44, 16), '#6b6b66', 2)
    worn(im, (4, 0, 44, 16), rnd, '#8a5632', 16)
    return im, None


def name_plate(name, style):
    """A family's name at the gate. No two are made alike."""
    def draw(rnd):
        im = canvas(32, 16)
        w = measure(name)
        x0 = (32 - w) // 2
        if style == 0:      # white enamel, black letters, a black line round it
            field(im, (x0 - 3, 3, x0 + w + 3, 13), '#f2f1ea', rnd, 2); border(im, (x0 - 3, 3, x0 + w + 3, 13), '#1c1c1c')
            write(im, x0, 6, name, '#1c1c1c')
        elif style == 1:    # a plank, the name burned in
            field(im, (x0 - 3, 4, x0 + w + 3, 13), '#9a7646', rnd, 5); line(im, x0 - 3, 12, x0 + w + 3, 13, '#6e5330')
            write(im, x0, 6, name, '#2e1d10', rnd=rnd, jitter=6)
        elif style == 2:    # green, white letters, the number over it
            field(im, (x0 - 4, 1, x0 + w + 4, 15), '#27543a', rnd, 2); border(im, (x0 - 4, 1, x0 + w + 4, 15), '#e9efe6')
            centre(im, 3, '14', '#e9efe6'); write(im, x0, 9, name, '#e9efe6')
        else:               # stencilled on grey tin, a little rust
            field(im, (x0 - 3, 3, x0 + w + 3, 13), '#8f9498', rnd, 3)
            write(im, x0, 6, name, '#1c1c1c', rnd=rnd, flake=.03)
            worn(im, (x0 - 3, 3, x0 + w + 3, 13), rnd, '#7a4a2a', 10)
        return im, None
    return draw


def room_plate(number, card):
    """A steel plate over a door under the depot: the room's number, and a slot for a card. Two of the slots have cards in."""
    def draw(rnd):
        im = canvas(32, 16)
        field(im, (4, 0, 28, 16), '#aeb2b6', rnd, 3)
        border(im, (4, 0, 28, 16), '#6c7074')
        bolts(im, (4, 0, 28, 16), '#4c5054')
        centre(im, 2, str(number), '#1d1f22', True)
        if card:
            field(im, (5, 10, 27, 15), '#e9e3cf', rnd, 2)
            centre(im, 10, card, '#2a2a2a')
        else:
            line(im, 7, 10, 25, 15, '#5a5e62'); line(im, 8, 11, 24, 14, '#44474a')
        return im, None
    return draw


def open_diner(rnd):
    """Red tube in a tube frame; the tubes are the second picture."""
    im = canvas(32, 16)
    field(im, (1, 1, 31, 15), '#151216', rnd, 1)
    glow = canvas(32, 16)
    border(glow, (2, 2, 30, 14), '#ff4a3a')
    for x, y in ((2, 2), (29, 2), (2, 13), (29, 13)):
        glow.putpixel((x, y), (0, 0, 0, 0))
    centre(glow, 4, 'OPEN', '#ff6a54', True)
    gp, px = glow.load(), im.load()
    for y in range(16):
        for x in range(32):
            if gp[x, y][3]:
                px[x, y] = gp[x, y]
    return im, glow


def open_drug(rnd):
    """A lit box: blue letters on white, a red line round it."""
    im = canvas(32, 16)
    field(im, (1, 2, 31, 14), '#f6f6f0', rnd, 1)
    border(im, (1, 2, 31, 14), '#c4291c')
    centre(im, 5, 'OPEN', '#1f4e9e', True)
    return im, im.copy()


def open_antiques(rnd):
    """A card on a string, inked by hand."""
    im = canvas(32, 16)
    for x, y in ((15, 0), (14, 1), (13, 2), (16, 0), (17, 1), (18, 2)):
        im.putpixel((x, y), tone('#6a5a44'))
    field(im, (2, 3, 30, 15), '#efe6cc', rnd, 3)
    border(im, (2, 3, 30, 15), '#2a2018')
    centre(im, 5, 'OPEN', '#2a2018', True, wobble=[0, 0, 1, 0])
    return im, None


def in_doctor(rnd):
    """The card in the doctor's window."""
    im = canvas(32, 16)
    field(im, (1, 1, 31, 15), '#f4f1e6', rnd, 2)
    border(im, (1, 1, 31, 15), '#27406b')
    centre(im, 3, 'DOCTOR', '#1c1c1c')
    centre(im, 9, 'IS IN', '#27406b')
    return im, None


CARDS = {6: '263-A', 7: '264-A'}
SIGNS = [
    # name, columns, rows, mount, painter, light
    ('diner', 6, 2, 'wall', diner, 0), ('antiques', 6, 1, 'wall', antiques, 0), ('station', 6, 1, 'wall', station, 0),
    ('doctor', 6, 1, 'wall', doctor, 0), ('drug', 6, 2, 'wall', drug, 0), ('tipple', 6, 1, 'wall', tipple, 9),
    ('store', 9, 2, 'wall', store, 0), ('cold', 2, 1, 'wall', cold, 0), ('sheriff', 5, 1, 'wall', sheriff, 0),
    ('society', 5, 2, 'wall', society, 0), ('hardware', 4, 1, 'wall', hardware, 0), ('lost', 2, 1, 'wall', lost, 0),
    ('trail', 4, 2, 'wall', trail, 0), ('arch', 7, 1, 'wall', arch, 0), ('fort', 3, 2, 'wall', fort, 0),
    ('depot', 3, 1, 'wall', depot, 0), ('no_entry', 2, 2, 'wall', no_entry, 0), ('church', 4, 2, 'wall', church, 0),
    ('service', 3, 1, 'wall', service, 0), ('county', 3, 1, 'decal', county, 0), ('prescott', 3, 1, 'decal', prescott, 0),
    ('arch_mile', 1, 1, 'free', arch_mile, 0), ('roundel', 1, 1, 'wall', roundel, 0), ('refining', 1, 1, 'wall', refining, 0),
    ('name_pruitt', 1, 1, 'wall', name_plate('PRUITT', 0), 0), ('name_hollis', 1, 1, 'wall', name_plate('HOLLIS', 1), 0),
    ('name_webb', 1, 1, 'wall', name_plate('WEBB', 2), 0), ('name_corley', 1, 1, 'wall', name_plate('CORLEY', 3), 0),
    *[(f'room_{n}', 1, 1, 'wall', room_plate(n, CARDS.get(n)), 0) for n in range(1, 13)],
    ('open_diner', 1, 1, 'window', open_diner, 7), ('open_drug', 1, 1, 'window', open_drug, 5),
    ('open_antiques', 1, 1, 'window', open_antiques, 0), ('in_doctor', 2, 1, 'window', in_doctor, 0),
]
# A single sign's picture sits centred on its block, unless it is named here: (left, bottom) of the picture from the block's corner.
PLACED = {}


# ------------------------------------------------------------------------------------------------------- pictures, models, states
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def pictures():
    """Every picture by file name. A sign's seed is its own name's, so adding a sign changes no other."""
    out = {}
    for name, cols, rows, mount, painter, light in SIGNS:
        rnd = random.Random(sum(ord(c) * (i + 7) for i, c in enumerate(name)) + 449)
        im, glow = painter(rnd)
        out[name] = im
        if glow is not None:
            out[name + '_glow'] = glow
    return out


def bounds(im, box):
    """The smallest box inside `box` that holds every pixel drawn there, or None."""
    part = im.crop(box).getbbox()
    return None if part is None else (box[0] + part[0], box[1] + part[1], box[0] + part[2], box[1] + part[3])


def element(lo, hi, faces, light=None):
    e = {'from': [round(v, 3) for v in lo], 'to': [round(v, 3) for v in hi], 'faces': faces}
    if light:
        e['shade'] = False
        e['neoforge_data'] = {'block_light': light, 'sky_light': light}
    return e


def piece(name, im, glow, mount, light, window, left, bottom):
    """One block's model. `window` is the part of the picture this block shows; `left` and `bottom` are where the picture's
    left and bottom edges lie from this block's own, in pixels, as someone facing the sign sees it. A model looks north."""
    w, h = im.size
    box = bounds(im, window)
    if box is None:
        raise SystemExit(f'{name}: a block of it has nothing drawn in it')
    x0, y0, x1, y1 = box

    def uv(a0, b0, a1, b1):
        return [round(a0 * 16 / w, 4), round(b0 * 16 / h, 4), round(a1 * 16 / w, 4), round(b1 * 16 / h, 4)]

    lo_x, hi_x = 16 - (left + x1), 16 - (left + x0)
    lo_y, hi_y = bottom + h - y1, bottom + h - y0
    for v in (lo_x, hi_x, lo_y, hi_y):
        if not -16 <= v <= 32:
            raise SystemExit(f'{name}: its board reaches more than a block past its own')
    front = {'texture': '#a', 'uv': uv(x0, y0, x1, y1)}
    back = {'texture': '#a', 'uv': uv(x1, y0, x0, y1)}      # the same paint seen from behind
    sides = {'east': {'texture': '#a', 'uv': uv(x0, y0, x0 + 1, y1)}, 'west': {'texture': '#a', 'uv': uv(x1 - 1, y0, x1, y1)},
             'up': {'texture': '#a', 'uv': uv(x1, y0, x0, y0 + 1)}, 'down': {'texture': '#a', 'uv': uv(x1, y1 - 1, x0, y1)}}
    textures = {'a': f'{MOD}:block/{DIR}/{name}', 'particle': f'{MOD}:block/{DIR}/{name}'}
    if mount == 'wall':
        elements = [element([lo_x, lo_y, 15], [hi_x, hi_y, 16], {'north': front, **sides})]
        proud = 14.75
    elif mount == 'decal':
        elements = [element([lo_x, lo_y, 15.75], [hi_x, hi_y, 15.75], {'north': front})]
        proud = 15.5
    elif mount == 'window':
        elements = [element([lo_x, lo_y, .5], [hi_x, hi_y, 1.5], {'north': front, 'south': back, **sides})]
        proud = .25
    else:   # free: read from both sides
        elements = [element([lo_x, lo_y, 7.5], [hi_x, hi_y, 8.5], {'north': front, 'south': dict(front), **sides})]
        proud = 7.25
    if glow is not None:
        lit = bounds(glow, window)
        if lit is not None:
            textures['g'] = f'{MOD}:block/{DIR}/{name}_glow'
            gx0, gy0, gx1, gy1 = lit
            elements.append(element([16 - (left + gx1), bottom + h - gy1, proud], [16 - (left + gx0), bottom + h - gy0, proud],
                                    {'north': {'texture': '#g', 'uv': uv(gx0, gy0, gx1, gy1)}}, 15))
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': textures, 'elements': elements}


def models(pics):
    out = {}
    for name, cols, rows, mount, painter, light in SIGNS:
        im, glow = pics[name], pics.get(name + '_glow')
        w, h = im.size
        if cols * rows > 1:
            if (w, h) != (cols * 16, rows * 16):
                raise SystemExit(f'{name}: {w}x{h} is not {cols}x{rows} blocks')
            for row in range(rows):
                for col in range(cols):
                    window = (col * 16, row * 16, col * 16 + 16, row * 16 + 16)
                    out[f'{name}_{row * cols + col}'] = piece(name, im, glow, mount, light, window, -16 * col, -16 * (rows - 1 - row))
        else:
            left, bottom = PLACED.get(name, ((16 - w) // 2, (16 - h) // 2))
            out[f'{name}_0'] = piece(name, im, glow, mount, light, (0, 0, w, h), left, bottom)
    return out


def states():
    variants, n = {}, 0
    for name, cols, rows, mount, painter, light in SIGNS:
        for k in range(cols * rows):
            for facing, y in FACING_Y.items():
                v = {'model': f'{MOD}:block/{DIR}/{name}_{k}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},piece={n}'] = v
            n += 1
    return {'variants': variants}


def declared():
    """BorrascaSigns.Sign as the game has it: name, columns, rows, mount, light, in order."""
    text = JAVA.read_text()
    body = text[text.index('enum Sign'):]
    out = []
    for m in re.finditer(r'\b([A-Z][A-Z0-9_]*)\((\d+),(\d+),Mount\.([A-Z]+),(\d+)', body):
        out.append((m.group(1).lower(), int(m.group(2)), int(m.group(3)), m.group(4).lower(), int(m.group(5))))
    return out


def files():
    pics = pics_cache()
    out = {}
    for name, m in models(pics).items():
        out[ASSETS / f'models/block/{DIR}/{name}.json'] = dump_model(m)
    out[ASSETS / f'blockstates/{DIR}.json'] = dump_states(states())
    return pics, out


def tight(value):
    return json.dumps(value, separators=(',', ':'))


def dump_model(m):
    """A model with one element to the line."""
    head = {k: v for k, v in m.items() if k != 'elements'}
    return tight(head)[:-1] + ',\n "elements":[\n  ' + ',\n  '.join(tight(e) for e in m['elements']) + '\n ]}\n'


def dump_states(s):
    """The states with one to the line."""
    return '{"variants":{\n ' + ',\n '.join(f'{tight(k)}:{tight(v)}' for k, v in s['variants'].items()) + '\n}}\n'


_PICS = None


def pics_cache():
    global _PICS
    if _PICS is None:
        _PICS = pictures()
    return _PICS


def sheet(path, scale=5):
    """Every sign on one page, for looking at."""
    pics = pics_cache()
    names = [s[0] for s in SIGNS]
    pad, x, y, row_h, width = 6, 6, 6, 0, 1500
    spots = []
    for n in names:
        w, h = pics[n].size[0] * scale, pics[n].size[1] * scale
        if x + w + pad > width:
            x, y, row_h = pad, y + row_h + pad, 0
        spots.append((n, x, y)); x += w + pad; row_h = max(row_h, h)
    page = Image.new('RGBA', (width, y + row_h + pad), (96, 104, 112, 255))
    for n, x, y in spots:
        im = pics[n].resize((pics[n].size[0] * scale, pics[n].size[1] * scale), Image.NEAREST)
        page.alpha_composite(im, (x, y))
    page.save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--sheet')
    args = ap.parse_args()
    if args.sheet:
        sheet(args.sheet)
        return
    mine = [(n, c, r, m, l) for n, c, r, m, p, l in SIGNS]
    if JAVA.exists():
        theirs = declared()
        if mine != theirs:
            for a, b in zip(mine, theirs):
                if a != b:
                    raise SystemExit(f'the tool has {a} where BorrascaSigns.Sign has {b}')
            raise SystemExit(f'the tool has {len(mine)} signs and BorrascaSigns.Sign has {len(theirs)}')
    elif args.check:
        raise SystemExit('BorrascaSigns.java is missing')
    pics, texts = files()
    folder = ASSETS / f'textures/block/{DIR}'
    if args.check:
        for name, im in pics.items():
            path = folder / f'{name}.png'
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool draws')
        for path, text in texts.items():
            if not path.exists() or path.read_text() != text:
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool writes')
        extra = {p.stem for p in folder.glob('*.png')} - set(pics) | {p.stem for p in (ASSETS / f'models/block/{DIR}').glob('*.json')} - {p.stem for p in texts}
        if extra:
            raise SystemExit(f'files the tool does not make: {sorted(extra)}')
        lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
        if lang.get(f'block.{MOD}.{DIR}') != 'Painted Sign':
            raise SystemExit('the sign has no name in en_us.json')
        print(f'{len(SIGNS)} signs, {sum(c * r for _, c, r, *_ in SIGNS)} blocks of them, {len(pics)} pictures: as drawn')
        return
    folder.mkdir(parents=True, exist_ok=True)
    for name, im in pics.items():
        im.save(folder / f'{name}.png')
    for path, text in texts.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
    print(f'{len(SIGNS)} signs, {sum(c * r for _, c, r, *_ in SIGNS)} blocks of them, {len(pics)} pictures')


if __name__ == '__main__':
    main()
