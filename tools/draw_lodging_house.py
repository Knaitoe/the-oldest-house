"""The Ones Already Inside (0.4.549, docs/THE_ONES_ALREADY_INSIDE_PLAN.md): the lodging house.

Draws, in native Minecraft pixels from fixed seeds, the lodging house's materials, its papered walls (each room's paper, with its
skirting course and its hatch), its floors (each room's floor on top and the ceiling of the room below underneath), its two stairs and
the fittings of the lodging_fitting block, their models facing north and turned by their facing. Writes the models, the block states
and the blocks' names. --check verifies everything, and that the tool's lists of materials, pairs, parts and kinds are the game's.
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
T = 'block/lodging/'


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


def speckle(im, seed, n, colours):
    rnd = random.Random(seed)
    for _ in range(n):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(colours))


def darken(im, x0, y0, x1, y1, k):
    px = im.load()
    for y in range(max(0, y0), min(16, y1)):
        for x in range(max(0, x0), min(16, x1)):
            r, g, b, a = px[x, y]
            px[x, y] = (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


# ------------------------------------------------------------------------------------------------ papers
def stripes(base, line, motif, seed):
    """An umber paper in broad stripes, a small four-dot flower in every other stripe, gone darker near the top where the gas was."""
    im = new(base, seed, 3)
    for x in (0, 8):
        fill(im, x, 0, x + 1, 16, line)
    for x, y in ((4, 3), (4, 11), (12, 7), (12, 15)):
        for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
            dot(im, x + dx, (y + dy) % 16, motif)
        dot(im, x, y % 16, shade(motif, -18))
    darken(im, 0, 0, 16, 2, -6)
    return im


def damask(base, figure, light, seed):
    """A faded damask: a lozenge figure on a ground a shade lighter, the pile rubbed through in places."""
    im = new(base, seed, 3)
    for y in range(16):
        for x in range(16):
            d = abs(x - 8) + abs((y + 4) % 16 - 8)
            if d in (5, 6):
                dot(im, x, y, figure)
            elif d == 1:
                dot(im, x, y, light)
    for x, y in ((0, 4), (15, 4), (0, 12), (15, 12)):
        dot(im, x, y, figure)
    speckle(im, seed + 1, 6, [light])
    return im


def sprig(base, flower, leaf, seed, step=8):
    """A small sprig repeated in a half-drop, neat, the kind chosen because nobody will mind it."""
    im = new(base, seed, 2)
    for row, y in enumerate(range(2, 16, step)):
        for x in range(2 + (row % 2) * (step // 2), 16, step):
            dot(im, x, y, flower); dot(im, x + 1, y, flower); dot(im, x, y + 1, flower)
            dot(im, x - 1, y + 2, leaf); dot(im, x + 2, y + 2, leaf); dot(im, x, y + 3, leaf)
    return im


def distemper(base, seed, scuff=None):
    im = new(base, seed, 4)
    if scuff:
        rnd = random.Random(seed + 7)
        for _ in range(5):
            x, y = rnd.randrange(14), rnd.randrange(6, 14)
            fill(im, x, y, x + rnd.randint(2, 4), y + 1, scuff)
    return im


def tiles_paper(seed):
    """The scullery's walls: cream tiles four to a block, the grout gone grey, a little grime along the joints."""
    im = new('#d8d3c1', seed, 3)
    for v in (0, 8):
        fill(im, 0, v, 16, v + 1, '#a9a392'); fill(im, v, 0, v + 1, 16, '#a9a392')
    for x, y in ((3, 3), (11, 12), (13, 4)):
        dot(im, x, y, '#ece8dc')
    speckle(im, seed + 1, 7, ['#b9b39f', '#c6c0ad'])
    return im


def plaster_work(seed):
    """The workroom's walls: bare plaster full of pin holes, and paler rectangles where things were pinned and taken down."""
    im = new('#c9bfa9', seed, 3)
    fill(im, 2, 3, 8, 9, '#d6cdb8'); fill(im, 9, 9, 14, 15, '#d2c9b3')
    for x, y in ((2, 3), (7, 3), (2, 8), (7, 8), (9, 9), (13, 9), (11, 1), (4, 12), (14, 5)):
        dot(im, x, y, '#6d6553')
    return im


def vera_paper(seed):
    """Vera's room: a narrow stripe, kept clean."""
    im = new('#a3ae9f', seed, 1)
    for x in range(0, 16, 4):
        fill(im, x, 0, x + 1, 16, '#8d9a8a'); fill(im, x + 2, 0, x + 3, 16, '#b4bdb0')
    return im


PAPER_ART = {
    'hall': lambda: stripes('#6b5a44', '#5a4a37', '#8c7652', 54901),
    'parlor': lambda: damask('#a7837a', '#8e6b63', '#b8968c', 54903),
    'dining': lambda: sprig('#3f5446', '#8a8a4e', '#33463a', 54905, 8),
    'daniel': lambda: distemper('#bdb8a8', 54907, '#a39f91'),
    'distemper': lambda: distemper('#d6d0bf', 54909),
    'scullery': lambda: tiles_paper(54911),
    'room': lambda: sprig('#cfc6b0', '#8a97a0', '#9aa38a', 54913, 8),
    'work': lambda: plaster_work(54915),
    'vera': lambda: vera_paper(54917),
}
PAPERS = list(PAPER_ART)


def skirted(im, board='#3b2b22'):
    """The paper with its skirting along the foot: a board five pixels high, a moulding on top and a shadow on the floor line."""
    out = im.copy()
    fill(out, 0, 11, 16, 16, board); fill(out, 0, 11, 16, 12, shade(board, 32)); fill(out, 0, 15, 16, 16, shade(board, -14))
    for x in (2, 9, 13):
        dot(out, x, 13, shade(board, 12))
    return out


def hatched(im, seed):
    """A hatch in the wall: a panel of painted boards in a frame, a finger pull in its slide."""
    out = im.copy()
    fill(out, 3, 4, 13, 12, '#3f3127'); fill(out, 4, 5, 12, 11, '#5b4636')
    for y in (7, 9):
        fill(out, 4, y, 12, y + 1, '#4c3a2d')
    dot(out, 10, 8, '#22190f'); dot(out, 11, 8, '#22190f')
    return out


def lath(seed=54921):
    """The back of the walls: laths in courses with the plaster squeezed through between them."""
    im = new('#5c5246', seed, 3); rnd = random.Random(seed + 1)
    for y in range(0, 16, 3):
        tone = rnd.randint(-8, 8)
        fill(im, 0, y, 16, y + 2, shade('#a07c55', tone))
        for x in range(16):
            if rnd.random() < .18:
                dot(im, x, y + 2, '#b3aa96')
    return im


def hatch_back(seed=54923):
    """The hatch from behind: its slide in its frame, set into the laths."""
    out = lath(seed)
    fill(out, 3, 4, 13, 12, '#6b5440'); fill(out, 4, 5, 12, 11, '#7a6249')
    return out


# ------------------------------------------------------------------------------------------------ materials
def brick(face='#7a4a3a', mortar='#8e8678', seed=54931, vary=8):
    im = new(mortar, seed, 3); rnd = random.Random(seed + 1); px = im.load()
    for course in range(4):
        y0 = course * 4; off = 0 if course % 2 == 0 else 4
        for start in (off - 8, off, off + 8):
            tone = rnd.randint(-vary, vary)
            for x in range(start, start + 7):
                for y in range(y0, y0 + 3):
                    if 0 <= x < 16:
                        px[x, y] = shade(face, tone + rnd.randint(-3, 3))
    return im


def brick_damp():
    """The cellar's lowest course: the brick dark with wet at the foot and white with salt above it."""
    im = brick(seed=54933); rnd = random.Random(54934); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if y >= 11:
                px[x, y] = (max(0, r - 30), max(0, g - 26), max(0, b - 22), 255)
            elif y >= 5 and rnd.random() < (11 - y) / 14:
                k = 34 + rnd.randint(0, 16)
                px[x, y] = (min(255, r + k), min(255, g + k + 4), min(255, b + k + 8), 255)
    return im


def flags(seed=54941):
    """Stone flags: two big slabs and a part of a third, their joints filled with dirt."""
    im = new('#6f6c66', seed, 5)
    fill(im, 0, 9, 16, 10, '#4a4844'); fill(im, 10, 0, 11, 9, '#4a4844'); fill(im, 4, 10, 5, 16, '#4a4844')
    speckle(im, seed + 1, 12, ['#7c7871', '#615e58', '#585550'])
    return im


def panel(seed=54943):
    """Varnished panelling: a raised panel with its bevel catching the light along the top and the left."""
    im = new('#5a3a24', seed, 3)
    fill(im, 2, 2, 14, 14, '#6b4a30'); fill(im, 2, 2, 14, 3, '#7e5a3c'); fill(im, 2, 2, 3, 14, '#7e5a3c')
    fill(im, 2, 13, 14, 14, '#48301e'); fill(im, 13, 2, 14, 14, '#48301e')
    return im


def boards(base, gap, seed, worn=None, along_x=True, stains=0):
    """Floorboards, four to a block, their joints dark, a nail at each end, and a paler path worn along the middle if it is walked."""
    im = new(base, seed, 4); rnd = random.Random(seed + 1)
    for i in range(4):
        tone = rnd.randint(-7, 7)
        fill(im, 0, i * 4, 16, i * 4 + 4, shade(base, tone))
        fill(im, 0, i * 4 + 3, 16, i * 4 + 4, gap)
        cut = rnd.randrange(16)
        dot(im, cut, i * 4, gap); dot(im, cut, i * 4 + 1, gap); dot(im, cut, i * 4 + 2, gap)
        dot(im, (cut + 2) % 16, i * 4 + 1, shade(base, -20))
    if worn:
        rnd2 = random.Random(seed + 3); px = im.load()
        for y in range(16):
            for x in range(4, 12):
                if rnd2.random() < .7:
                    r, g, b, a = px[x, y]
                    w = rgb(worn)
                    px[x, y] = ((r + w[0]) // 2, (g + w[1]) // 2, (b + w[2]) // 2, 255)
    for _ in range(stains):
        x, y = rnd.randrange(12), rnd.randrange(12)
        fill(im, x, y, x + 3, y + 2, '#3f2f22'); dot(im, x + 3, y + 1, '#3f2f22')
    if not along_x:
        im = im.rotate(90)
    return im


def tile():
    """The hall's tiles: small squares and diamonds, terracotta, black and cream, worn to the clay where the door swings."""
    im = new('#d8cdb4', 54951, 3)
    for y in range(16):
        for x in range(16):
            cx, cy = x % 8, y % 8
            if abs(cx - 3.5) + abs(cy - 3.5) <= 2:
                dot(im, x, y, '#2b2724')
            elif cx in (0, 7) or cy in (0, 7):
                dot(im, x, y, '#9a4b33')
    speckle(im, 54952, 9, ['#c9bda2', '#8d4530', '#a65a42'])
    return im


def quarry():
    """The scullery's quarry tiles, four to a block, grout gone dark."""
    im = new('#8c3f2c', 54953, 6)
    for v in (0, 8):
        fill(im, 0, v, 16, v + 1, '#5f4a3e'); fill(im, v, 0, v + 1, 16, '#5f4a3e')
    speckle(im, 54954, 8, ['#9a4a35', '#7a3626'])
    return im


def scuffed():
    """Daniel's boards: arcs scraped by a chair turned on the same spot, and one dark patch."""
    im = boards('#7b5a3c', '#3e2c1e', 54955)
    for t in range(9):
        x = 3 + t; y = 12 - abs(t - 4)
        dot(im, x, y, '#94785a')
    fill(im, 10, 2, 14, 5, '#5a4029')
    return im


def runner(edge):
    """The stair and gallery runner: a dark red ground, a border with a gold line on its outer side, worn pale down the middle."""
    im = new('#6e2a2a', 54957 + {'w': 0, 'm': 1, 'e': 2}[edge], 3)
    for y in range(0, 16, 4):
        dot(im, 7, y, '#7d3a33'); dot(im, 8, y + 2, '#7d3a33')
    if edge == 'w':
        fill(im, 0, 0, 4, 16, '#4a1d1d'); fill(im, 2, 0, 3, 16, '#9c7a3a')
    if edge == 'e':
        fill(im, 12, 0, 16, 16, '#4a1d1d'); fill(im, 13, 0, 14, 16, '#9c7a3a')
    if edge == 'm':
        speckle(im, 54960, 10, ['#82423a'])
    return im


def carpet():
    """Vera's carpet: a small repeat in plum and dust."""
    im = new('#5b4a5a', 54961, 3)
    for y in range(0, 16, 4):
        for x in range((y // 4 % 2) * 2, 16, 4):
            dot(im, x, y, '#73607a'); dot(im, x + 1, y + 1, '#4a3c49')
    return im


def joists():
    """The underside of a ground floor from the cellar: joists across, the boards dark between them, cobweb in a corner."""
    im = new('#5d4632', 54963, 3)
    for x in (0, 8):
        fill(im, x, 0, x + 3, 16, '#4f3b2a'); fill(im, x, 0, x + 1, 16, '#3c2c1f')
    dot(im, 14, 1, '#9c9a94'); dot(im, 13, 2, '#9c9a94'); dot(im, 14, 2, '#8a8882')
    return im


def ceiling():
    """A plaster ceiling, its paint gone the colour of smoke, a hair crack across it."""
    im = new('#e0dccd', 54965, 3)
    for x, y in ((2, 5), (3, 6), (4, 6), (5, 7), (6, 7), (7, 8), (8, 8), (9, 9)):
        dot(im, x, y, '#c9c3b2')
    return im


def rough():
    """The underside of the passages' floors: laths and the backs of boards."""
    im = new('#7a6249', 54967, 5)
    for y in range(0, 16, 4):
        fill(im, 0, y, 16, y + 1, '#5e4b37')
    return im


# ------------------------------------------------------------------------------------------------ fittings
def door(upper, base='#4a3328', seed=54971):
    """A four-panel door, painted, the lower panels kicked and the paint worn at the handle."""
    im = new(base, seed + upper, 2)
    for x0 in (2, 9):
        if upper:
            fill(im, x0, 3, x0 + 5, 16, shade(base, -12)); fill(im, x0, 3, x0 + 5, 4, shade(base, 14))
        else:
            fill(im, x0, 0, x0 + 5, 11, shade(base, -12)); fill(im, x0 + 4, 0, x0 + 5, 11, shade(base, 14))
    if not upper:
        fill(im, 0, 13, 16, 16, shade(base, -6)); speckle(im, seed + 5, 7, ['#6d5444'])
    else:
        fill(im, 0, 0, 16, 1, shade(base, -16))
    return im


def ledged(upper, seed=54975):
    """A ledged door of boards for the passages, a brace across it."""
    im = new('#7a6347', seed + upper, 4)
    for x in range(0, 16, 4):
        fill(im, x, 0, x + 1, 16, '#5d4a34')
    if upper:
        fill(im, 0, 3, 16, 5, '#6a5539')
    else:
        fill(im, 0, 10, 16, 12, '#6a5539')
        for t in range(10):
            dot(im, 3 + t, 9 - t // 2, '#6a5539')
    return im


def jib(upper):
    """The jib door: the hall's paper carried across it, a hair line where it meets the wall."""
    im = PAPER_ART['hall']()
    if not upper:
        im = skirted(im)
    for y in range(16):
        dot(im, 0, y, '#3e3226'); dot(im, 15, y, '#3e3226')
    if upper:
        fill(im, 0, 0, 16, 1, '#3e3226')
    return im


def cupboard(upper):
    im = panel(54977 + upper)
    if not upper:
        dot(im, 12, 3, '#b08d42'); dot(im, 12, 4, '#8e7036')
    return im


def casing():
    im = new('#e0d6bf', 54979, 2)
    fill(im, 0, 0, 1, 16, '#c6baa0'); fill(im, 15, 0, 16, 16, '#c6baa0')
    for y in (4, 9, 13):
        dot(im, 7, y, '#d2c7ae')
    return im


def recess(daniel, upper):
    """Inside a frame: plaster set back a hand's depth. Daniel's is worn smooth where his back and shoulders have been against it,
    and the straps hang either side at the height of his wrists."""
    im = new('#d2c2a8' if not daniel else '#c3b39a', 54981 + daniel * 2 + upper, 3)
    if daniel:
        if upper:
            fill(im, 4, 8, 12, 16, '#d7c9b0')
            for x in (1, 13):
                fill(im, x, 4, x + 2, 9, '#5b3b25'); dot(im, x, 6, '#8f8a7d'); dot(im, x + 1, 6, '#8f8a7d')
        else:
            fill(im, 4, 0, 12, 7, '#d7c9b0')
    else:
        fill(im, 0, 0, 16, 1, '#bba98d')
    return im


def shutter(upper):
    """Inside shutters, folded shut over the window, a bar across them screwed into the casing; the screws painted over."""
    im = new('#4f5a4a', 54985 + upper, 2)
    fill(im, 7, 0, 9, 16, '#3c4639')
    for x0 in (1, 9):
        fill(im, x0, 2, x0 + 6, 14, '#566352'); fill(im, x0, 2, x0 + 6, 3, '#64725f')
    fill(im, 0, 0, 1, 16, '#e0d6bf'); fill(im, 15, 0, 16, 16, '#e0d6bf')
    if upper:
        fill(im, 0, 0, 16, 2, '#e0d6bf')
        fill(im, 0, 9, 16, 11, '#2e2e2e'); dot(im, 2, 10, '#4f5a4a'); dot(im, 13, 10, '#4f5a4a')
    else:
        fill(im, 0, 13, 16, 16, '#e0d6bf'); fill(im, 0, 13, 16, 14, '#c6baa0')
    return im


def plate(n):
    """A white enamel number, the numeral black in a narrow black border, in the region the plate's element takes (x and y 4 to 12)."""
    im = new('#e8e4da', 54991 + n, 1)
    fill(im, 4, 4, 12, 5, '#2c2c2c'); fill(im, 4, 11, 12, 12, '#2c2c2c'); fill(im, 4, 4, 5, 12, '#2c2c2c'); fill(im, 11, 4, 12, 12, '#2c2c2c')
    glyphs = {1: ['.#.', '##.', '.#.', '.#.', '###'], 2: ['##.', '..#', '.#.', '#..', '###'], 3: ['##.', '..#', '.#.', '..#', '##.'],
              4: ['#.#', '#.#', '###', '..#', '..#'], 5: ['###', '#..', '##.', '..#', '##.'], 6: ['.##', '#..', '##.', '#.#', '.#.']}
    for dy, row in enumerate(glyphs[n]):
        for dx, ch in enumerate(row):
            if ch == '#':
                dot(im, 6 + dx, 5 + dy, '#1d1d1d')
    return im


def tally(n):
    """Scratches on the wall: in fives, then not in fives, then only lines. Eight hands of them, transparent between."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(55001 + n)
    style = n % 3
    for row in range(2):
        y0 = 2 + row * 8 + rnd.randint(-1, 1)
        x = 1 + rnd.randint(0, 2)
        groups = rnd.randint(1, 3)
        for g in range(groups):
            for i in range(4 if style == 0 else rnd.randint(2, 6)):
                for y in range(y0, y0 + 4 + rnd.randint(-1, 1)):
                    dot(im, x, y, '#5d574c', 200)
                x += 2
                if x > 14:
                    break
            if style == 0 and x - 9 >= 0:
                for t in range(9):
                    dot(im, x - 9 + t, y0 + 3 - t // 3, '#5d574c', 200)
            x += 2
            if x > 13:
                break
    return im


def timber(end=False, burned=False):
    if end:
        im = new('#7a5c3c', 55011, 3)
        for r in (2, 4, 6):
            for t in range(32):
                x = int(round(8 + r * math.cos(t / 32 * 6.283))); y = int(round(8 + r * math.sin(t / 32 * 6.283)))
                dot(im, x, y, '#5d442b')
        return im
    im = new('#6b5034', 55013 + burned, 3)
    for x in range(0, 16, 3):
        fill(im, x, 0, x + 1, 16, '#5a422a')
    if burned:
        fill(im, 0, 5, 16, 8, '#2e2219'); fill(im, 0, 6, 16, 7, '#1f1711')
    return im


def wheel(upper):
    """The ratchet's wheel, face on: an iron disc with teeth round it and an axle boss; this cell holds its lower or upper half."""
    # The wheel is drawn whole on a strip two cells high, its axle on the line between them; each cell takes its half.
    strip = Image.new('RGBA', (16, 32), (0, 0, 0, 0)); px = strip.load()
    for y in range(32):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 16)
            ang = math.atan2(y - 16, x - 7.5)
            tooth = (int((ang + math.pi) / (2 * math.pi) * 16) % 2) == 0
            if d <= 6.5 or (d <= 7.6 and tooth):
                px[x, y] = shade('#3a3a3c', -6 if d > 6.5 else 0)
            if 4.5 <= d <= 5.2:
                px[x, y] = shade('#2a2a2b', 0)
            if d <= 1.6:
                px[x, y] = shade('#56565a', 0)
    return strip.crop((0, 0, 16, 16)) if upper else strip.crop((0, 16, 16, 32))


def chain():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        dot(im, 7 if (y // 2) % 2 == 0 else 8, y, '#4a4a4a')
        if y % 4 == 0:
            dot(im, 8, y, '#5e5e5e'); dot(im, 7, y, '#5e5e5e')
    return im


def textures():
    out = {}
    for p, f in PAPER_ART.items():
        mid = f(); out[T + f'paper_{p}.png'] = mid; out[T + f'paper_{p}_low.png'] = skirted(mid)
        out[T + f'hatch_{p}.png'] = hatched(mid, 0)
    out[T + 'lath.png'] = lath(); out[T + 'hatch_lath.png'] = hatch_back()
    out[T + 'brick.png'] = brick(); out[T + 'brick_damp.png'] = brick_damp(); out[T + 'flags.png'] = flags(); out[T + 'panel.png'] = panel()
    out[T + 'roof.png'] = rough()
    # Floors and their undersides.
    out[T + 'tile.png'] = tile(); out[T + 'boards.png'] = boards('#7b5a3c', '#3e2c1e', 55101)
    out[T + 'boards_worn.png'] = boards('#7b5a3c', '#3e2c1e', 55103, worn='#9a7c5c')
    out[T + 'quarry.png'] = quarry(); out[T + 'boards_scuffed.png'] = scuffed()
    out[T + 'bare.png'] = boards('#8d7a5f', '#5c4c38', 55105)
    for e in ('w', 'm', 'e'):
        out[T + f'runner_{e}.png'] = runner(e)
    out[T + 'boards_up.png'] = boards('#8a6a48', '#4c3926', 55107)
    out[T + 'boards_work.png'] = boards('#6d5238', '#3a2a1c', 55109, stains=3)
    out[T + 'carpet.png'] = carpet(); out[T + 'joists.png'] = joists(); out[T + 'ceiling.png'] = ceiling(); out[T + 'rough.png'] = rough()
    out[T + 'floor_edge.png'] = boards('#6a4b31', '#3e2c1e', 55111, along_x=False)
    # Fittings.
    for u in (0, 1):
        s = 'high' if u else 'low'
        out[T + f'door_{s}.png'] = door(u); out[T + f'service_{s}.png'] = ledged(u); out[T + f'jib_{s}.png'] = jib(u)
        out[T + f'cupboard_{s}.png'] = cupboard(u); out[T + f'shutter_{s}.png'] = shutter(u)
        out[T + f'recess_{s}.png'] = recess(0, u); out[T + f'recess_daniel_{s}.png'] = recess(1, u)
        out[T + f'wheel_{s}.png'] = wheel(u)
    out[T + 'door_edge.png'] = new('#3d2a20', 55121, 2); out[T + 'casing.png'] = casing()
    out[T + 'rail.png'] = new('#4a3020', 55123, 3); out[T + 'ladder.png'] = boards('#9b7d57', '#7a6142', 55125, along_x=False)
    out[T + 'bulb.png'] = new('#fff6d8', 55127, 2); out[T + 'bulb_dim.png'] = new('#c9b48a', 55129, 2); out[T + 'flex.png'] = new('#2a2522', 55131, 1)
    out[T + 'iron.png'] = new('#2f2f30', 55133, 4); out[T + 'brass.png'] = new('#b08d42', 55135, 6)
    grate = new('#2b2b2b', 55137, 2)
    for x in range(1, 16, 3):
        fill(grate, x, 0, x + 1, 16, '#4a4a4c')
    out[T + 'grate.png'] = grate
    coal = new('#2b2b2b', 55139, 0)
    for x, y in ((2, 3), (6, 2), (10, 4), (13, 2), (4, 6), (9, 7)):
        fill(coal, x, y, x + 2, y + 1, '#555555')
    out[T + 'coal.png'] = coal
    out[T + 'soot.png'] = new('#1c1a19', 55141, 3)
    tiles = new('#3e5a50', 55143, 3)
    for v in (0, 4, 8, 12):
        fill(tiles, 0, v, 16, v + 1, '#2c403a')
    for x, y in ((2, 2), (9, 6), (5, 10), (12, 14)):
        dot(tiles, x, y, '#5f7d72')
    out[T + 'tiles.png'] = tiles
    out[T + 'mantel.png'] = new('#3c281c', 55145, 3)
    glass = new('#8c9092', 55147, 2)
    for t in range(10):
        dot(glass, 3 + t, 12 - t, '#b0b5b7')
    out[T + 'glass.png'] = glass
    mirror = new('#9aa0a2', 55149, 2)
    for t in range(12):
        dot(mirror, 2 + t, 13 - t, '#c4c9ca'); dot(mirror, 3 + t, 13 - t, '#b5babb')
    out[T + 'mirror.png'] = mirror
    out[T + 'table_wood.png'] = new('#5e3f28', 55151, 3); out[T + 'stand_wood.png'] = new('#4b3222', 55153, 3); out[T + 'bench_wood.png'] = new('#5a3c27', 55155, 3)
    card = new('#e6dcc3', 55157, 2)
    for y in (1, 2):
        fill(card, 1, y, 5, y + 1, '#8a7f68')
    dot(card, 1, 0, '#4a4234'); dot(card, 2, 0, '#4a4234'); dot(card, 3, 0, '#4a4234')
    out[T + 'card.png'] = card
    for n in range(1, 7):
        out[T + f'plate_{n}.png'] = plate(n)
    for n in range(8):
        out[T + f'tally_{n}.png'] = tally(n)
    out[T + 'timber.png'] = timber(); out[T + 'timber_end.png'] = timber(end=True); out[T + 'timber_burned.png'] = timber(burned=True)
    out[T + 'wedge.png'] = new('#8a6a45', 55161, 4); out[T + 'chain.png'] = chain(); out[T + 'strap.png'] = new('#5b3b25', 55163, 3)
    out[T + 'stair_wood.png'] = boards('#5e4129', '#3a281a', 55165); out[T + 'stair_bare.png'] = boards('#8a6c4c', '#5c4630', 55167)
    return out


# ------------------------------------------------------------------------------------------------ models
FACES = ('north', 'south', 'east', 'west', 'up', 'down')


def uv(face, frm, to):
    """The face's own pixels, as Minecraft would take them, brought back into the texture where an element stands outside its cell."""
    x0, y0, z0 = frm; x1, y1, z1 = to
    if face in ('north', 'south'):
        u0, u1 = (16 - x1, 16 - x0) if face == 'north' else (x0, x1); v0, v1 = 16 - y1, 16 - y0
    elif face in ('east', 'west'):
        u0, u1 = (16 - z1, 16 - z0) if face == 'east' else (z0, z1); v0, v1 = 16 - y1, 16 - y0
    else:
        u0, u1 = x0, x1; v0, v1 = (z0, z1)
    def fold(a, b):
        w = min(16, b - a)
        while a < 0:
            a += 16
        while a + w > 16:
            a -= 16
        a = max(0, a)
        return [round(a, 3), round(a + w, 3)]
    u = fold(u0, u1); v = fold(v0, v1)
    return [u[0], v[0], u[1], v[1]]


def box(frm, to, tex, cull=None, faces=FACES, rot=None):
    """An element. tex is one texture key for every face, or a dict by face (missing faces are left off)."""
    out = {}
    for f in faces:
        key = tex.get(f) if isinstance(tex, dict) else tex
        if key is None:
            continue
        face = {'texture': '#' + key, 'uv': uv(f, frm, to)}
        if cull and f in cull:
            face['cullface'] = f
        out[f] = face
    el = {'from': list(frm), 'to': list(to), 'faces': out}
    if rot:
        el['rotation'] = rot
    return el


def model(elements, textures, particle=None, cutout=False):
    keys = sorted({face['texture'][1:] for el in elements for face in el['faces'].values()})
    tx = {k: NS + T + textures[k] for k in keys}
    tx['particle'] = NS + T + (particle or textures[keys[0]])
    m = {'textures': tx, 'elements': elements}
    if cutout:
        m['render_type'] = 'minecraft:cutout'
    return m


def cube(front, back, side, top, bottom):
    """A full cell: the front faces north as drawn."""
    tex = {'north': 'front', 'south': 'back', 'east': 'side', 'west': 'side', 'up': 'top', 'down': 'bottom'}
    return model([box((0, 0, 0), (16, 16, 16), tex, cull=FACES)], {'front': front, 'back': back, 'side': side, 'top': top, 'bottom': bottom})


WALL_PAIRS = ['hall_parlor', 'hall_dining', 'hall_daniel', 'hall_scullery', 'hall_distemper', 'hall_lath', 'parlor_lath', 'dining_lath', 'daniel_lath',
              'daniel_distemper', 'daniel_scullery', 'scullery_distemper', 'scullery_lath', 'hall_room', 'room_lath', 'room_work', 'room_vera', 'hall_work',
              'hall_vera', 'work_lath', 'vera_lath']
WALL_PARTS = ['low', 'mid', 'hatch', 'hatch_open']


def paper(name, part):
    if name == 'lath':
        return 'hatch_lath' if part in ('hatch', 'hatch_open') else 'lath'
    if part == 'low':
        return f'paper_{name}_low'
    if part == 'hatch':
        return f'hatch_{name}'
    return f'paper_{name}'


def wall_model(pair, part):
    front, back = pair.split('_')
    if part != 'hatch_open':
        f, b = paper(front, part), paper(back, part)
        return cube(f, b, f, 'paper_distemper', 'paper_distemper')
    # Open: the slide pushed back beside the hole; the hole through to the far side, its reveal boards.
    f, b = paper(front, 'mid'), paper(back, 'mid')
    tex = {'front': f, 'back': b, 'reveal': 'door_edge', 'slide': 'door_low'}
    def wall_box(frm, to, inner):
        t = {'north': 'front', 'south': 'back', 'east': 'front', 'west': 'front', 'up': 'front', 'down': 'front'}
        for k in list(t):
            if k in inner:
                t[k] = 'reveal'
        cull = [k for k in FACES if k not in inner]
        return box(frm, to, t, cull=cull)
    els = [wall_box((0, 0, 0), (16, 5, 16), ('up',)), wall_box((0, 11, 0), (16, 16, 16), ('down',)),
           wall_box((0, 5, 0), (4, 11, 16), ('east',)), wall_box((12, 5, 0), (16, 11, 16), ('west',)),
           box((11, 5, -1), (16, 11, 0), 'slide')]
    return model(els, tex, particle=f)


FLOOR_PAIRS = {'tile': ('tile', 'joists'), 'boards': ('boards', 'joists'), 'worn': ('boards_worn', 'joists'), 'flags': ('quarry', 'joists'),
               'scuffed': ('boards_scuffed', 'joists'), 'passage_low': ('bare', 'joists'), 'runner_w': ('runner_w', 'ceiling'),
               'runner': ('runner_m', 'ceiling'), 'runner_e': ('runner_e', 'ceiling'), 'room': ('boards_up', 'ceiling'), 'work': ('boards_work', 'ceiling'),
               'carpet': ('carpet', 'ceiling'), 'passage': ('bare', 'rough'), 'top': ('roof', 'ceiling'), 'top_passage': ('roof', 'rough')}


def floor_model(pair):
    top, bottom = FLOOR_PAIRS[pair]
    return cube('floor_edge', 'floor_edge', 'floor_edge', top, bottom)


def leaf(tex_low, tex_high, upper, opened, extras=()):
    """A door's leaf in the middle of its cell, or swung back against the west jamb; the lining of the opening round it."""
    t = tex_high if upper else tex_low
    tx = {'leaf': t, 'edge': 'door_edge', 'lining': 'casing', 'brass': 'brass', 'iron': 'iron'}
    face = {'north': 'leaf', 'south': 'leaf', 'east': 'edge', 'west': 'edge', 'up': 'edge', 'down': 'edge'}
    els = [box((0, 0, 0), (2, 16, 16), {'north': 'edge', 'south': 'edge', 'east': 'leaf', 'west': 'leaf', 'up': 'edge', 'down': 'edge'})] if opened else [box((1, 0, 7), (15, 16, 9), face)]
    els.append(box((15, 0, 0), (16, 16, 16), 'lining', cull=('east',)))
    if not opened:
        els.append(box((0, 0, 0), (1, 16, 16), 'lining', cull=('west',)))
    if upper:
        els.append(box((0, 15, 0), (16, 16, 16), 'lining', cull=('up',)))
    if not opened and not upper:
        els += [box((12, 13, 6), (14, 15, 7), 'brass'), box((12, 13, 9), (14, 15, 10), 'brass')]
    els += list(extras)
    return model(els, tx, particle=t)


def door_models(kind, low, high):
    out = {}
    for stage in range(8 if kind == 'bolt_door' else 4):
        opened, upper, shot = stage & 1, (stage >> 1) & 1, (stage >> 2) & 1
        extras = []
        if kind == 'bolt_door' and upper and not opened:
            extras = [box((9, 3, 6), (11, 6, 7), 'iron'), box((10, 4, 5.5), (15 if shot else 12, 5, 6), 'iron'), box((14, 2, 5), (15, 7, 7), 'iron')]
        out[f'{kind}_{stage}'] = leaf(low, high, upper, opened, extras)
    return out


def jib_models():
    out = {}
    for stage in range(4):
        opened, upper = stage & 1, (stage >> 1) & 1
        t = 'jib_high' if upper else 'jib_low'
        if opened:
            out[f'jib_{stage}'] = model([box((0, 0, 0), (2, 16, 16), {'north': 'edge', 'south': 'edge', 'east': 'leaf', 'west': 'back', 'up': 'edge', 'down': 'edge'}),
                                         box((15, 0, 0), (16, 16, 16), 'lath', cull=('east',))], {'leaf': t, 'back': 'lath', 'edge': 'door_edge', 'lath': 'lath'}, particle=t)
        else:
            out[f'jib_{stage}'] = cube(t, 'lath', t, 'paper_distemper', 'paper_distemper')
    return out


def frame_models():
    out = {}
    for daniel in (0, 1):
        p = 'daniel' if daniel else 'room'
        for upper in (0, 1):
            pap = f'paper_{p}' if upper else f'paper_{p}_low'
            for east in (0, 1):
                # The jamb: wall, and the casing on the side toward the opening, standing a pixel proud of the wall.
                wall_x, case_x = ((6, 16), (0, 6)) if east else ((0, 10), (10, 16))
                els = [box((wall_x[0], 0, 0), (wall_x[1], 16, 16), {'north': 'paper', 'south': 'lath', 'east': 'paper', 'west': 'paper', 'up': 'paper', 'down': 'paper'}, cull=('south', 'up', 'down', 'west' if not east else 'east')),
                       box((case_x[0], 0, -1), (case_x[1], 16, 16), 'casing', cull=('south', 'up', 'down'))]
                out[f'frame_side_{upper | east * 2 | daniel * 4}'] = model(els, {'paper': pap, 'lath': 'lath', 'casing': 'casing'}, particle=pap)
            back = f'recess_daniel_{"high" if upper else "low"}' if daniel else f'recess_{"high" if upper else "low"}'
            els = [box((0, 0, 10), (16, 16, 16), {'north': 'back', 'south': 'lath', 'up': 'back', 'down': 'back', 'east': 'back', 'west': 'back'}, cull=('south',)),
                   box((0, 0, -1), (1, 16, 10), 'casing'), box((15, 0, -1), (16, 16, 10), 'casing')]
            if daniel and upper:
                els += [box((1, 4, 9), (3, 9, 10), 'strap'), box((13, 4, 9), (15, 9, 10), 'strap'), box((1, 6, 8.5), (3, 7, 9), 'iron'), box((13, 6, 8.5), (15, 7, 9), 'iron')]
            out[f'frame_back_{upper | daniel * 2}'] = model(els, {'back': back, 'lath': 'lath', 'casing': 'casing', 'strap': 'strap', 'iron': 'iron'}, particle=back)
        for k in range(3):
            pap = f'paper_{p}'
            if k == 1:
                els = [box((0, 6, 0), (16, 16, 16), {'north': 'paper', 'south': 'lath', 'east': 'paper', 'west': 'paper', 'up': 'paper', 'down': 'casing'}, cull=('south', 'up')),
                       box((0, 0, -1), (16, 6, 16), 'casing', cull=('south',))]
            else:
                wall_x, case_x = ((0, 10), (10, 16)) if k == 0 else ((6, 16), (0, 6))
                els = [box((wall_x[0], 0, 0), (wall_x[1], 16, 16), {'north': 'paper', 'south': 'lath', 'east': 'paper', 'west': 'paper', 'up': 'paper', 'down': 'paper'}, cull=('south', 'up')),
                       box((case_x[0], 6, 0), (case_x[1], 16, 16), {'north': 'paper', 'south': 'lath', 'east': 'paper', 'west': 'paper', 'up': 'paper', 'down': 'casing'}, cull=('south', 'up')),
                       box((case_x[0], 0, -1), (case_x[1], 6, 16), 'casing', cull=('south',))]
            out[f'frame_head_{k + daniel * 3}'] = model(els, {'paper': pap, 'lath': 'lath', 'casing': 'casing'}, particle=pap)
    return out


def fitting_models():
    m = {}
    m.update(door_models('door', 'door_low', 'door_high'))
    m.update(door_models('bolt_door', 'door_low', 'door_high'))
    m.update(door_models('service', 'service_low', 'service_high'))
    m.update(door_models('cupboard', 'cupboard_low', 'cupboard_high'))
    m.update(jib_models())
    m.update(frame_models())
    for u in (0, 1):
        s = 'high' if u else 'low'
        m[f'window_{u}'] = cube(f'shutter_{s}', 'brick', 'casing', 'casing', 'casing')
    # The gallery's rail: handrail, bottom rail and balusters along the north edge; the corner adds the west edge and a post.
    def rail_run(west=False):
        if west:
            return [box((0, 14, 0), (2, 16, 16), 'rail'), box((0, 0, 0), (2, 2, 16), 'rail')] + [box((0.5, 2, z), (1.5, 14, z + 1.5), 'rail') for z in (2.5, 6.5, 10.5, 14)]
        return [box((0, 14, 0), (16, 16, 2), 'rail'), box((0, 0, 0), (16, 2, 2), 'rail')] + [box((x, 2, 0.5), (x + 1.5, 14, 1.5), 'rail') for x in (2.5, 6.5, 10.5, 14)]
    m['rail_0'] = model(rail_run(), {'rail': 'rail'})
    m['rail_1'] = model(rail_run() + rail_run(True) + [box((0, 0, 0), (3, 17, 3), 'rail')], {'rail': 'rail'})
    # The stair's handrail along the west edge, rising north at a tread a cell; two balusters to a tread.
    m['banister_0'] = model([box((0.25, 5, -3.3), (1.75, 7, 19.3), 'rail', rot={'origin': [1, 6, 8], 'axis': 'x', 'angle': 45}),
                             box((0.5, -8, 11.5), (1.5, 3, 12.5), 'rail'), box((0.5, 0, 3.5), (1.5, 11, 4.5), 'rail')], {'rail': 'rail'})
    m['banister_1'] = model([box((12, 0, 6), (16, 20, 10), 'rail'), box((11.5, 20, 5.5), (16.5, 22, 10.5), 'rail')], {'rail': 'rail'})
    m['ladder_0'] = model([box((1, 0, 13), (3, 16, 16), 'ladder'), box((13, 0, 13), (15, 16, 16), 'ladder')] + [box((3, y, 14), (13, y + 1, 15), 'ladder') for y in (2, 6, 10, 14)], {'ladder': 'ladder'})
    m['lamp_0'] = model([box((7.5, 7, 7.5), (8.5, 16, 8.5), 'flex'), box((6.5, 2, 6.5), (9.5, 7, 9.5), 'bulb')], {'flex': 'flex', 'bulb': 'bulb'})
    m['lamp_1'] = model([box((7.5, 7, 7.5), (8.5, 16, 8.5), 'flex'), box((6.5, 2, 6.5), (9.5, 7, 9.5), 'bulb')], {'flex': 'flex', 'bulb': 'bulb_dim'})
    m['lamp_2'] = model([box((7, 6, 12), (9, 8, 16), 'brass'), box((6.5, 8, 11), (9.5, 13, 14), 'bulb')], {'brass': 'brass', 'bulb': 'bulb'})
    # The parlor's grate in its surround; above, the mantel shelf and the glass over it.
    m['fireplace_0'] = model([box((0, 0, 0), (2, 16, 16), 'tiles', cull=('west', 'south')), box((14, 0, 0), (16, 16, 16), 'tiles', cull=('east', 'south')),
                              box((2, 11, 0), (14, 16, 16), {'north': 'tiles', 'down': 'soot', 'up': 'tiles', 'south': 'soot'}, cull=('up', 'south')),
                              box((2, 0, 11), (14, 11, 16), {'north': 'soot', 'up': 'soot'}, cull=('south',)), box((3, 0, 5), (13, 4, 10), {'north': 'grate', 'up': 'coal', 'east': 'grate', 'west': 'grate'}),
                              box((1, 0, -2), (15, 2, -1), 'iron')], {'tiles': 'tiles', 'soot': 'soot', 'grate': 'grate', 'coal': 'coal', 'iron': 'iron'})
    m['fireplace_1'] = model([box((0, 0, 0), (16, 16, 16), {'north': 'paper', 'south': 'paper', 'east': 'paper', 'west': 'paper', 'up': 'paper', 'down': 'paper'}, cull=('south', 'up', 'east', 'west')),
                              box((-1, 0, -3), (17, 2, 0), 'mantel'), box((2, 4, -1), (14, 15, 0), 'glass'), box((1, 3, -1.5), (15, 4, 0), 'mantel'), box((1, 15, -1.5), (15, 16, 0), 'mantel')],
                             {'paper': 'paper_parlor', 'mantel': 'mantel', 'glass': 'glass'})
    m['coat_rail_0'] = model([box((0, 6, 14), (16, 9, 16), 'stand')] + [box((x, 4, 12), (x + 1, 7, 14), 'brass') for x in (3, 8, 13)], {'stand': 'stand_wood', 'brass': 'brass'})
    m['hall_table_0'] = model([box((1, 13, 6), (15, 15, 16), 'wood'), box((2, 0, 7), (3.5, 13, 8.5), 'wood'), box((12.5, 0, 7), (14, 13, 8.5), 'wood'),
                               box((2, 0, 14), (3.5, 13, 15.5), 'wood'), box((12.5, 0, 14), (14, 13, 15.5), 'wood'), box((3, 10, 6.5), (13, 13, 7), 'wood'),
                               box((5, 15, 10), (11, 19, 10.5), 'card')], {'wood': 'table_wood', 'card': 'card'})
    m['hall_stand_0'] = model([box((1, 0, 8), (15, 4, 16), 'wood'), box((1, 4, 13), (3, 16, 15), 'wood'), box((13, 4, 13), (15, 16, 15), 'wood'), box((1, 10, 10), (15, 12, 16), 'wood')], {'wood': 'stand_wood'})
    m['hall_stand_1'] = model([box((1, 0, 13), (3, 16, 15), 'wood'), box((13, 0, 13), (15, 16, 15), 'wood'), box((3, 1, 14), (13, 13, 15), 'mirror'), box((1, 13, 13), (15, 15, 15), 'wood'),
                               box((2, 14, 11), (3, 15, 13), 'brass'), box((13, 14, 11), (14, 15, 13), 'brass')], {'wood': 'stand_wood', 'mirror': 'mirror', 'brass': 'brass'})
    for stage, arm in ((0, (0, 2)), (1, (14, 16))):
        m[f'bench_{stage}'] = model([box((0, 6, 5), (16, 8, 16), 'wood'), box((0, 8, 13), (16, 16, 15), 'wood'), box((arm[0], 8, 5), (arm[1], 12, 13), 'wood'),
                                     box((1, 0, 6), (2.5, 6, 7.5), 'wood'), box((13.5, 0, 6), (15, 6, 7.5), 'wood'), box((1, 0, 13), (2.5, 6, 14.5), 'wood'), box((13.5, 0, 13), (15, 6, 14.5), 'wood')],
                                    {'wood': 'bench_wood'})
    for n in range(6):
        m[f'number_{n}'] = model([box((4, 4, 15), (12, 12, 16), {'north': 'plate', 'east': 'plate', 'west': 'plate', 'up': 'plate', 'down': 'plate'})], {'plate': f'plate_{n + 1}'})
    for n in range(8):
        m[f'tally_{n}'] = model([box((0, 0, 15.9), (16, 16, 15.95), {'north': 'tally'})], {'tally': f'tally_{n}'}, cutout=True)
    timber_t = {'north': 'end', 'south': 'end', 'east': 'side', 'west': 'side', 'up': 'side', 'down': 'side'}
    m['sill_0'] = model([box((2, 0, 0), (14, 8, 16), timber_t, cull=('north', 'south', 'down'))], {'end': 'timber_end', 'side': 'timber'})
    post_t = {'north': 'side', 'south': 'side', 'east': 'side', 'west': 'side', 'up': 'end', 'down': 'end'}
    m['post_0'] = model([box((2, 0, 2), (14, 16, 14), post_t, cull=('up', 'down'))], {'end': 'timber_end', 'side': 'timber'})
    m['post_1'] = model([box((2, 0, 2), (14, 16, 14), post_t, cull=('up', 'down'))], {'end': 'timber_end', 'side': 'timber_burned'})
    m['shoe_0'] = model([box((2, 0, 0), (14, 8, 16), timber_t, cull=('north', 'south', 'down')), box((1, 8, 1), (15, 13, 15), 'iron'),
                         box((2, 13, 2), (14, 16, 14), post_t, cull=('up',)), box((13, 8, 6), (15, 15, 10), 'wedge')], {'end': 'timber_end', 'side': 'timber', 'iron': 'iron', 'wedge': 'wedge'})
    m['shoe_1'] = model([box((1, 0, 1), (15, 5, 15), 'iron', cull=('down',)), box((2, 5, 2), (14, 16, 14), post_t, cull=('up',)), box((14, 0, 6), (16, 7, 10), 'wedge')],
                        {'end': 'timber_end', 'side': 'timber', 'iron': 'iron', 'wedge': 'wedge'})
    m['ratchet_0'] = model([box((0, 0, 3), (16, 3, 13), 'iron', cull=('down',)), box((1, 3, 6), (3, 16, 10), 'iron'), box((13, 3, 6), (15, 16, 10), 'iron'),
                            box((0, 0, 7), (16, 16, 9), {'north': 'wheel', 'south': 'wheel'}), box((10, 12, 5), (14, 14, 7), 'iron'), box((12, 3, 3.5), (14, 16, 5), 'iron')],
                           {'iron': 'iron', 'wheel': 'wheel_low'}, cutout=True)
    m['ratchet_1'] = model([box((1, 0, 6), (3, 12, 10), 'iron'), box((13, 0, 6), (15, 12, 10), 'iron'), box((0, 0, 7), (16, 16, 9), {'north': 'wheel', 'south': 'wheel'}),
                            box((0, 7, 7.5), (16, 9, 8.5), 'iron'), box((7.5, 12, 7.5), (8.5, 16, 8.5), {'north': 'chain', 'south': 'chain', 'east': 'chain', 'west': 'chain'})],
                           {'iron': 'iron', 'wheel': 'wheel_high', 'chain': 'chain'}, cutout=True)
    return m


def stair_models(name, top):
    out = {}; states = {}
    for shape in ('straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right'):
        tread = {'north': 'top', 'south': 'top', 'east': 'wood', 'west': 'wood', 'up': 'top', 'down': 'wood'}
        second = [8 if shape.endswith('right') and shape.startswith('outer') else 0, 8, 0], [8 if shape.startswith('outer') and shape.endswith('left') else 16, 16, 8]
        els = [box((0, 0, 0), (16, 8, 16), tread, cull=('down',)), box(tuple(second[0]), tuple(second[1]), tread)]
        if shape.startswith('inner'):
            els.append(box((0 if shape.endswith('left') else 8, 8, 8), (8 if shape.endswith('left') else 16, 16, 16), tread))
        key = f'{name}_{shape}'
        out[key] = model(els, {'top': top, 'wood': 'stair_wood' if top != 'stair_bare' else 'stair_bare'})
        for facing, angle in FACING_Y.items():
            for half in ('bottom', 'top'):
                for wet in ('true', 'false'):
                    v = {'model': NS + 'block/lodging/' + key, 'uvlock': True}
                    y = angle if half == 'bottom' else (angle + 180) % 360
                    if y:
                        v['y'] = y
                    if half == 'top':
                        v['x'] = 180
                    states[f'facing={facing},half={half},shape={shape},waterlogged={wet}'] = v
    return out, states


KINDS = ['door', 'bolt_door', 'service', 'jib', 'cupboard', 'frame_side', 'frame_back', 'frame_head', 'window', 'rail', 'banister', 'ladder', 'lamp',
         'fireplace', 'coat_rail', 'hall_table', 'hall_stand', 'bench', 'number', 'tally', 'sill', 'post', 'shoe', 'ratchet']
STAGES = {'door': 4, 'service': 4, 'jib': 4, 'cupboard': 4, 'frame_back': 4, 'bolt_door': 8, 'frame_side': 8, 'tally': 8, 'frame_head': 6, 'number': 6,
          'lamp': 3, 'window': 2, 'rail': 2, 'banister': 2, 'fireplace': 2, 'hall_stand': 2, 'bench': 2, 'ratchet': 2, 'shoe': 2, 'post': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 8
MATERIALS = ['brick', 'brick_damp', 'flags', 'roof', 'lath', 'panel', 'paper_hall', 'paper_hall_low', 'paper_parlor', 'paper_parlor_low', 'paper_room',
             'paper_room_low', 'distemper', 'distemper_low']
MATERIAL_TEXTURE = {'distemper': 'paper_distemper', 'distemper_low': 'paper_distemper_low'}
NAMES = {'lodging_fitting': 'Lodging House Fitting', 'lodging_wall': 'Papered Wall', 'lodging_floor': 'Floor', 'lodging_stair': 'Stair',
         'lodging_cellar_stair': 'Cellar Stair', 'lodging_brick': 'Brick', 'lodging_brick_damp': 'Brick, Damp', 'lodging_flags': 'Cellar Flags',
         'lodging_roof': 'Roof Boards', 'lodging_lath': 'Laths', 'lodging_panel': 'Panelling', 'lodging_paper_hall': 'Hall Paper',
         'lodging_paper_hall_low': 'Hall Paper, Skirting', 'lodging_paper_parlor': 'Parlor Paper', 'lodging_paper_parlor_low': 'Parlor Paper, Skirting',
         'lodging_paper_room': 'Guest Room Paper', 'lodging_paper_room_low': 'Guest Room Paper, Skirting', 'lodging_distemper': 'Distemper',
         'lodging_distemper_low': 'Distemper, Skirting'}


def java_names():
    """The materials as LodgingRegistry lists them, and the pairs, parts and kinds as the blocks do: the tool and the game must agree."""
    import re
    lab = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth'
    reg = (lab / 'LodgingRegistry.java').read_text()
    names = re.findall(r'"([a-z_]+)"', re.search(r'MATERIAL_NAMES=java\.util\.List\.of\(([^;]*?)\);', reg, re.S).group(1))
    def enum(path, head):
        body = re.search(head + r'\s*\{\s*([A-Z_,\s]+);', (lab / path).read_text()).group(1)
        return [k.strip().lower() for k in body.split(',') if k.strip()]
    return names, enum('LodgingBlock.java', r'enum Kind implements StringRepresentable'), enum('LodgingWall.java', r'enum Pair implements StringRepresentable'), \
        enum('LodgingWall.java', r'enum Part implements StringRepresentable'), enum('LodgingFloor.java', r'enum Pair implements StringRepresentable')


def files():
    out = {}
    for name in MATERIALS:
        tex = MATERIAL_TEXTURE.get(name, name)
        out[f'models/block/lodging/{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + T + tex}}
        out[f'blockstates/lodging_{name}.json'] = {'variants': {'': {'model': NS + f'block/lodging/{name}'}}}
    walls = {}
    for pair in WALL_PAIRS:
        for part in WALL_PARTS:
            out[f'models/block/lodging/wall_{pair}_{part}.json'] = wall_model(pair, part)
            for facing, y in FACING_Y.items():
                v = {'model': NS + f'block/lodging/wall_{pair}_{part}'}
                if y:
                    v['y'] = y
                walls[f'facing={facing},pair={pair},part={part}'] = v
    out['blockstates/lodging_wall.json'] = {'variants': walls}
    floors = {}
    for pair in FLOOR_PAIRS:
        out[f'models/block/lodging/floor_{pair}.json'] = floor_model(pair)
        floors[f'pair={pair}'] = {'model': NS + f'block/lodging/floor_{pair}'}
    out['blockstates/lodging_floor.json'] = {'variants': floors}
    for name, top in (('lodging_stair', 'runner_m'), ('lodging_cellar_stair', 'stair_bare')):
        models, states = stair_models(name, top)
        for key, obj in models.items():
            out[f'models/block/lodging/{key}.json'] = obj
        out[f'blockstates/{name}.json'] = {'variants': states}
    for key, obj in fitting_models().items():
        out[f'models/block/lodging/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/lodging/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/lodging_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    mats, kinds, pairs, parts, floors = java_names()
    assert mats == MATERIALS, (mats, MATERIALS)
    assert kinds == KINDS, (kinds, KINDS)
    assert pairs == WALL_PAIRS, (pairs, WALL_PAIRS)
    assert parts == WALL_PARTS, (parts, WALL_PARTS)
    assert floors == list(FLOOR_PAIRS), (floors, list(FLOOR_PAIRS))
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'models/block/lodging/{kind}_{stage}.json' in data, (kind, stage)
    for rel, obj in data.items():
        if rel.startswith('models/'):
            for tex in obj['textures'].values():
                assert tex.replace(NS, '') + '.png' in images, (rel, tex)
    for rel, im in images.items():
        assert im.size == (16, 16), rel
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
    assert set(NAMES) == {'lodging_fitting', 'lodging_wall', 'lodging_floor', 'lodging_stair', 'lodging_cellar_stair'} | {f'lodging_{n}' for n in MATERIALS}
    for key, name in NAMES.items():
        key = f'block.the_oldest_house.{key}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the lodging house: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
