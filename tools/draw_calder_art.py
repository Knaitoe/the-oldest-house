"""Bike night (0.4.464, 0.4.471): Calder Park.

Draws, in native Minecraft pixels from fixed seeds, the park the night begins in: its materials (the back wall's brick, the path's
concrete flags, the kerb, the street's asphalt and its centre line, the trees' October leaves) and its own fittings, each a kind of
one block (calder_fitting): the iron railings and their spear tops, the gate's leaves and the chain on them, a lamp post in four
pieces with its lantern lit or dark, a slatted bench with iron ends, a wire litter bin, the sandpit's timber edging, and a sheet of
lined paper taped up by the door; and the leaves down on the grass, a block of their own (calder_litter) that gives way to whatever
is set down on it. Nothing here is one of the game's own blocks standing for something else.

0.4.471: a bench end with a heart cut into its top slat; a drinking fountain, still and running; and a payphone on the wall, its
handset on its hook and off it.

Small pieces are bars that take one pixel of the park's paint card, as the bikes' are (tools/draw_bike_night_art.py); faces big
enough to carry a picture take a part of one the size they are, so everything is sixteen pixels to the block. A fitting is drawn
facing north; its block state turns it.

Writes the models and block states and the blocks' names. --check verifies everything. --sheet PATH draws a page for review.
Drawing or checking, it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

from draw_borrasca_town_art import shade, new, fill, dot, element
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/calder/'
P = NS + DIR
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
SLIP = .05

# ---------------------------------------------------------------------------------------------------------------- the paint card
# Sixteen swatches, four pixels square, four to a row. A bar takes the pixel in the middle of its swatch.
IRON, IRON_HI, RUST, STEEL, WOOD, WOOD_DK, TIMBER, TIMBER_END, BRASS, GLASS_DARK, CONCRETE, MESH, CREAM, TAPE, PAPER, SHADOW = range(16)
CARD = ('#1c2420', '#2e3a33', '#5b3b27', '#8a8d90', '#7a5630', '#5d4024', '#6b5236', '#9a7a52',
        '#b8943a', '#30332f', '#9a9890', '#3a403c', '#e6dfc8', '#d8cfa0', '#efe9d8', '#141816')


def card():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4640); px = im.load()
    for i, colour in enumerate(CARD):
        for y in range(4):
            for x in range(4):
                px[4 * (i % 4) + x, 4 * (i // 4) + y] = shade(colour, rnd.randint(-2, 2))
    return im


def sw(i):
    x, y = 4 * (i % 4) + 1, 4 * (i // 4) + 1
    return [x, y, x + 1, y + 1]


# ---------------------------------------------------------------------------------------------------------------- materials
def brick():
    """The back wall: dull red brick in stretcher bond, the mortar gone grey."""
    im = new('#8f897e', 4641, 3); rnd = random.Random(4642)
    for course in range(4):
        y0 = course * 4
        shift = 4 if course % 2 else 0
        for k in range(-1, 3):
            x0 = k * 8 + shift
            tone = rnd.randint(-9, 9); red = '#7b3d2f' if rnd.random() < .8 else '#6a352b'
            for y in range(y0, y0 + 3):
                for x in range(x0, x0 + 7):
                    if 0 <= x < 16:
                        dot(im, x, y, shade(red, tone + rnd.randint(-4, 4)))
    return im


def asphalt():
    im = new('#2b2c2f', 4643, 4); rnd = random.Random(4644)
    for _ in range(14):
        dot(im, rnd.randint(0, 15), rnd.randint(0, 15), shade('#3c3d40', rnd.randint(-4, 6)))
    for _ in range(5):
        dot(im, rnd.randint(0, 15), rnd.randint(0, 15), shade('#1f2022', 0))
    return im


def road_line():
    """The street's centre line: a worn yellow dash down the middle of the block, a gap before the next."""
    im = asphalt(); rnd = random.Random(4645)
    for y in range(2, 14):
        for x in (7, 8):
            if rnd.random() > .12:
                dot(im, x, y, shade('#c9a52c', rnd.randint(-10, 8)))
    return im


def paving():
    """One concrete flag to the block, its joint along two edges, a hairline crack across a corner."""
    im = new('#8d8b84', 4646, 4)
    for i in range(16):
        dot(im, i, 0, shade('#6f6d67', 0)); dot(im, 0, i, shade('#6f6d67', 0))
    for x, y in ((10, 12), (11, 12), (12, 13), (13, 13), (13, 14), (14, 15)):
        dot(im, x, y, shade('#77756f', 0))
    return im


def kerb():
    im = new('#a19f97', 4647, 3)
    for x in range(16):
        dot(im, x, 0, shade('#b4b2aa', 0)); dot(im, x, 15, shade('#85837c', 0))
    for y in range(16):
        dot(im, 0, y, shade('#85837c', 0))
    return im


LEAF = ('#b5651d', '#c9892b', '#a8481f', '#8a5a2b', '#d2a13a', '#7d3b1c')


def leaves():
    """October foliage: rust, amber and brown, with the sky through it."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4648)
    for y in range(16):
        for x in range(16):
            if rnd.random() < .78:
                dot(im, x, y, shade(LEAF[rnd.randrange(len(LEAF))], rnd.randint(-8, 8)))
    for _ in range(18):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        if im.getpixel((x, y))[3]:
            dot(im, x, y, shade('#4c2a16', rnd.randint(-4, 4)))
    return im


def materials():
    return {'brick': brick(), 'asphalt': asphalt(), 'road_line': road_line(), 'paving': paving(), 'kerb': kerb(), 'leaves': leaves()}


# ---------------------------------------------------------------------------------------------------------------- pictures
def lamp_glass(lit):
    """One pane of the lantern, seven wide and eight high from the corner: frosted glass in an iron frame."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4650 + lit)
    for y in range(8):
        for x in range(7):
            edge = x in (0, 6) or y in (0, 7)
            if edge:
                dot(im, x, y, shade(CARD[IRON], 0))
            elif lit:
                glow = 0 if abs(x - 3) <= 1 and 2 <= y <= 5 else -18
                dot(im, x, y, shade('#ffe6a0', glow + rnd.randint(-4, 4)))
            else:
                dot(im, x, y, shade('#3b3f3a', rnd.randint(-3, 3)))
    # The lantern's underside, seven square, from (8, 0): the pane's colour inside the frame.
    for y in range(7):
        for x in range(7):
            edge = x in (0, 6) or y in (0, 6)
            dot(im, 8 + x, y, shade(CARD[IRON], 0) if edge else shade('#f3d489' if lit else '#343832', rnd.randint(-3, 3)))
    return im


def mesh():
    """The bin's wire: a diamond weave, eight wide and ten high from the corner, open between the wires."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(10):
        for x in range(8):
            if (x + y) % 3 == 0 or (x - y) % 3 == 0 or y in (0, 9):
                dot(im, x, y, shade(CARD[MESH], 6 if (x + y) % 2 else -2))
    return im


def litter(k):
    """Leaves down on the ground: a scatter, different on each of four."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4660 + k)
    for _ in range(9 + 3 * k):
        x, y = rnd.randint(0, 14), rnd.randint(0, 14); c = LEAF[rnd.randrange(len(LEAF))]; t = rnd.randint(-10, 6)
        dot(im, x, y, shade(c, t))
        if rnd.random() < .7:
            dot(im, x + 1, y, shade(c, t - 8))
        if rnd.random() < .4:
            dot(im, x, y + 1, shade(c, t - 12))
    return im


def note():
    """A sheet torn from a school pad, seven wide and nine high from the corner: ruled, pencilled, taped at the top."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4670)
    for y in range(9):
        for x in range(7):
            dot(im, x, y, shade(CARD[PAPER], rnd.randint(-3, 3)))
    for y in (2, 4, 6):
        for x in range(1, 6):
            dot(im, x, y, shade('#9fb3c8', 0))
    for x, y in ((1, 1), (2, 1), (4, 1), (1, 3), (3, 3), (4, 3), (5, 3), (2, 5), (3, 5), (1, 7), (2, 7), (4, 7)):
        dot(im, x, y, shade('#4a4a52', rnd.randint(-6, 6)))
    for x in (2, 3, 4):
        dot(im, x, 0, shade(CARD[TAPE], rnd.randint(-4, 4)))
    dot(im, 6, 8, (0, 0, 0, 0))                                          # a corner gone
    return im


def carving():
    """What is cut into the bench's back, fourteen wide and four high from the corner: its upper two rows go on the top slat and
    its lower two on the slat under it. A heart across both slats, and either side of it the strokes of a pair of letters. Only
    the cuts are drawn; the wood under them is the slats' own. Cut long ago: grey, not pale."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4710)
    rows = ('X..X..X.X..X.X',
            'XX.X.XXXXX.X.X',
            'X..X..XXX..X.X',
            'X.XX...X...XX.')
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c == 'X':
                dot(im, x, y, shade('#3b3a36', rnd.randint(-4, 6) if x < 5 or x > 9 else 0))
    return im


def water():
    """Drinking water in the air: pale, with the lamp in it."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4711)
    for y in range(16):
        for x in range(16):
            dot(im, x, y, shade('#bcd7e6', rnd.randint(-10, 14)))
    return im


def textures():
    out = {f'block/calder_{name}.png': im for name, im in materials().items()}
    out[DIR + 'carving.png'] = carving()
    out[DIR + 'water.png'] = water()
    out[DIR + 'park.png'] = card()
    out[DIR + 'lamp_lit.png'] = lamp_glass(1)
    out[DIR + 'lamp_dark.png'] = lamp_glass(0)
    out[DIR + 'mesh.png'] = mesh()
    for k in range(4):
        out[DIR + f'litter_{k}.png'] = litter(k)
    out[DIR + 'note.png'] = note()
    return out


# ---------------------------------------------------------------------------------------------------------------- pieces
def bar(parts, lo, hi, swatch, rot=None, ends=None):
    """One bar, every face from one swatch of the card; `ends` gives the east and west faces another (a log's end grain)."""
    uv = {k: sw(swatch) for k in FACES}
    if ends is not None:
        uv['east'] = uv['west'] = sw(ends)
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': 'k'}, uv, rot))


def plate(parts, lo, hi, tex, faces, glow=False):
    """A piece that shows a picture: each face named takes the part of the picture its own size, from the corner given."""
    lo, hi = [round(v, 3) for v in lo], [round(v, 3) for v in hi]
    size = {'north': (hi[0] - lo[0], hi[1] - lo[1]), 'south': (hi[0] - lo[0], hi[1] - lo[1]), 'east': (hi[2] - lo[2], hi[1] - lo[1]),
            'west': (hi[2] - lo[2], hi[1] - lo[1]), 'up': (hi[0] - lo[0], hi[2] - lo[2]), 'down': (hi[0] - lo[0], hi[2] - lo[2])}
    uv = {}
    for k, (u, v) in faces.items():
        w, h = size[k]
        uv[k] = [u, v, round(min(16, u + w), 3), round(min(16, v + h), 3)]
    e = element(lo, hi, {k: tex for k in faces}, uv)
    if glow:
        e['shade'] = False
        e['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    parts.append(e)


# The railings: four bars to the block, a pixel thick, on the block's middle line; the next block's follow at the same spacing.
BARS = (2, 6, 10, 14)


def spear(parts, c, y):
    """A bar's top: a collar, and over it a point (a small square turned on its corner)."""
    bar(parts, [c - .9, y, 7.1], [c + .9, y + 1, 8.9], IRON_HI)
    bar(parts, [c - .9, y + 1.1, 7.6], [c + .9, y + 2.9, 8.4], IRON, {'angle': 45, 'axis': 'z', 'origin': [c, y + 2, 8]})


def fence(upper):
    p = []
    if not upper:
        for c in BARS:
            bar(p, [c - .5, 0, 7.5], [c + .5, 16, 8.5], IRON)
        bar(p, [0, 2, 7.3], [16, 3.2, 8.7], IRON_HI)
    else:
        for c in BARS:
            bar(p, [c - .5, 0, 7.5], [c + .5, 11.5, 8.5], IRON)
            spear(p, c, 11.5)
        bar(p, [0, 7.5, 7.3], [16, 8.7, 8.7], IRON_HI)
    return p


def gate(stage):
    """One block of a gate's leaf, its heavy stile at the west edge (turned about, at the east): 0 the lower block, 1 the upper,
    2 the lower with the chain round the stile, 3 the same with the padlock hanging on the park's side."""
    p = []
    if stage == 1:
        bar(p, [0, 0, 7.2], [1.6, 13.4, 8.8], IRON)
        bar(p, [-.1, 13.4, 7.1], [1.7, 14.6, 8.9], IRON_HI)
        bar(p, [.3, 14.6, 7.5], [1.3, 15.8, 8.5], IRON)
        for c in (6, 10, 14):
            bar(p, [c - .5, 0, 7.5], [c + .5, 11.5, 8.5], IRON)
            spear(p, c, 11.5)
        bar(p, [1.6, 7.3, 7.3], [16, 8.9, 8.7], IRON_HI)
        return p
    bar(p, [0, 0, 7.2], [1.6, 16, 8.8], IRON)
    for c in (6, 10, 14):
        bar(p, [c - .5, 0, 7.5], [c + .5, 16, 8.5], IRON)
    bar(p, [1.6, 1.4, 7.3], [16, 3, 8.7], IRON_HI)
    if stage >= 2:                                                      # the chain, twice round both stiles, a band each time
        bar(p, [-SLIP, 9, 6.6], [2.2, 10, 9.4], STEEL)                  # each a hair into the other leaf, where its other half is
        bar(p, [-SLIP, 7.4, 6.8], [2.4, 8.3, 9.2], STEEL)
    if stage == 3:                                                      # the padlock: its shackle up to the chain, its body in brass
        bar(p, [.5, 6, 9.2], [.9, 7.4, 9.5], STEEL); bar(p, [1.5, 6, 9.2], [1.9, 7.4, 9.5], STEEL)
        bar(p, [.2, 3.6, 9], [2.2, 6, 9.8], BRASS)
    return p


def lamp(stage):
    """A lamp post in pieces, bottom to top: 0 the foot, 1 the post, 2 the lantern lit, 3 the lantern dark."""
    p = []
    if stage == 0:
        bar(p, [4.5, 0, 4.5], [11.5, 2, 11.5], IRON)
        bar(p, [5.5, 2, 5.5], [10.5, 5, 10.5], IRON_HI)
        bar(p, [6.5, 5, 6.5], [9.5, 7, 9.5], IRON)
        bar(p, [7, 7, 7], [9, 16, 9], IRON)
    elif stage == 1:
        bar(p, [7, 0, 7], [9, 16, 9], IRON)
        bar(p, [6.6, 9, 6.6], [9.4, 10.2, 9.4], IRON_HI)
    else:
        bar(p, [7, 0, 7], [9, 2, 9], IRON)
        bar(p, [5.5, 2, 5.5], [10.5, 3, 10.5], IRON_HI)
        tex = 'l' if stage == 2 else 'd'
        plate(p, [4.5, 3, 4.5], [11.5, 11, 11.5], tex, {'north': (0, 0), 'south': (0, 0), 'east': (0, 0), 'west': (0, 0), 'down': (8, 0)}, glow=stage == 2)
        bar(p, [3.5, 11, 3.5], [12.5, 12.2, 12.5], IRON)
        bar(p, [5, 12.2, 5], [11, 13.4, 11], IRON_HI)
        bar(p, [7.2, 13.4, 7.2], [8.8, 15.4, 8.8], IRON)
    return p


def bench(ends, carved=False):
    """A park bench looking north, its back to the south: slats on iron ends. `ends` says which of its ends this block has.
    Carved, a heart and two pairs of letters are cut into the front of its back, across both slats."""
    p = []
    if carved:
        plate(p, [2, 12.85, 11.55], [16, 14.85, 11.6], 'h', {'north': (0, 0)})
        plate(p, [2, 10.35, 11.55], [16, 12.35, 11.6], 'h', {'north': (0, 2)})
    for side in ends:
        x0, x1 = (.5, 1.7) if side == 'west' else (14.3, 15.5)
        bar(p, [x0, 0, 4.5], [x1, 7, 5.7], IRON)                          # front leg
        bar(p, [x0, 0, 11.3], [x1, 15, 12.5], IRON)                       # back leg, on up as the back's upright
        bar(p, [x0, 6, 5.7], [x1, 7, 11.3], IRON)                         # the rail the seat lies on
        bar(p, [x0 - .1, 7.9, 4.2], [x1 + .1, 9.4, 5], IRON)              # the arm's post, standing on the front slat
        bar(p, [x0 - .1, 9.4, 4.1], [x1 + .1, 10.2, 11.3], IRON_HI)       # the arm
    bar(p, [0, 7, 4], [16, 7.9, 6.3], WOOD); bar(p, [0, 7, 6.8], [16, 7.9, 9.1], WOOD_DK); bar(p, [0, 7, 9.6], [16, 7.9, 11.2], WOOD)
    bar(p, [0, 10.4, 11.6], [16, 12.3, 12.4], WOOD); bar(p, [0, 12.9, 11.6], [16, 14.8, 12.4], WOOD_DK)
    return p


# As CalderBlock has it: the fountain's pedestal is this many pixels high, so that its bowl is under a child's chin.
FOUNTAIN_HIGH = 7


def fountain(running):
    """A drinking fountain, its button to the north: a concrete pedestal, a steel bowl on it with a dark drain, and a spout at the
    back of the bowl that throws its water forward over it. Running, the water is in the air."""
    p = []
    h = FOUNTAIN_HIGH                                                      # the pedestal's top, where the bowl stands
    bar(p, [5, 0, 5], [11, h, 11], CONCRETE)
    bar(p, [4.2, 0, 4.2], [11.8, 1, 11.8], CONCRETE)
    bar(p, [3.5, h, 3.5], [12.5, h + 2.4, 12.5], STEEL)
    for lo, hi in (([3.5, h + 2.4, 3.5], [12.5, h + 3, 4.5]), ([3.5, h + 2.4, 11.5], [12.5, h + 3, 12.5]), ([3.5, h + 2.4, 4.5], [4.5, h + 3, 11.5]), ([11.5, h + 2.4, 4.5], [12.5, h + 3, 11.5])):
        bar(p, lo, hi, STEEL)                                             # the bowl's rim
    bar(p, [6.5, h + 2.4, 6], [9.5, h + 2.5, 9], SHADOW)                   # the drain
    bar(p, [7.3, h + 2.4, 10.2], [8.7, h + 4.4, 11.5], IRON_HI)            # the spout
    bar(p, [7, h - 3.5, 4.5], [9, h - 1.5, 5], BRASS)                      # the button
    if running:
        plate(p, [7.7, h + 4, 7.4], [8.3, h + 4.6, 10.2], 'w', {'up': (0, 0), 'down': (0, 0), 'east': (0, 0), 'west': (0, 0), 'north': (0, 0)})
        plate(p, [7.7, h + 2.5, 6.4], [8.3, h + 4.3, 7.4], 'w', {'up': (0, 0), 'east': (0, 0), 'west': (0, 0), 'north': (0, 0), 'south': (0, 0)})
    return p


def phone(lifted):
    """A payphone looking north, on the wall to its south: a steel box with its instructions on a card, a coin slot, a keypad and a
    coin return; a shelf under it; and at its left side the handset on its hook, a cord down from it. Lifted, the hook is bare
    and the cord goes up out of sight to whoever has it."""
    p = []
    bar(p, [3.5, 2.4, 11], [12.5, 15, 16], STEEL)
    bar(p, [3, 1.5, 9.5], [13, 2.4, 16], IRON_HI)                          # the shelf
    bar(p, [5, 9.5, 10.8], [11, 13, 11], CREAM)                            # the card
    bar(p, [9.5, 13.5, 10.8], [11.2, 14.2, 11], BRASS)                     # the coin slot
    for x in (5.4, 6.7, 8):
        for y in (4.4, 5.7, 7):
            bar(p, [x, y, 10.8], [x + .9, y + .9, 11], IRON_HI)            # the keys
    bar(p, [9.8, 4.2, 10.8], [11.4, 5.6, 11], SHADOW)                      # the coin return
    bar(p, [2.6, 11.4, 12], [3.5, 12.2, 13], IRON_HI)                      # the hook
    if lifted:
        bar(p, [2.75, 2.4, 12.3], [3.15, 11.4, 12.7], SHADOW)              # the cord, pulled up straight
    else:
        bar(p, [1.6, 4.5, 11.6], [3.1, 12.6, 13.4], SHADOW)                # the handset
        bar(p, [2.2, 2.4, 12.3], [2.6, 4.5, 12.7], SHADOW)                 # its cord
    return p


def bin_():
    p = []
    for x in (4, 11):
        for z in (4, 11):
            bar(p, [x, 0, z], [x + 1, 1, z + 1], IRON)
    bar(p, [4.6, 1.2, 4.6], [11.4, 10.6, 11.4], SHADOW)                   # the liner, and the dark inside it
    plate(p, [4, 1, 4], [12, 11, 12], 'm', {'north': (0, 0), 'south': (0, 0), 'east': (0, 0), 'west': (0, 0)})
    for lo, hi in (([3.5, 11, 3.5], [12.5, 12, 4.5]), ([3.5, 11, 11.5], [12.5, 12, 12.5]), ([3.5, 11, 4.5], [4.5, 12, 11.5]), ([11.5, 11, 4.5], [12.5, 12, 11.5])):
        bar(p, lo, hi, IRON_HI)
    return p


def border(corner):
    """The sandpit's edging: a squared timber along the block's north edge; at a corner, another along its west."""
    p = []
    bar(p, [0, 0, 0], [16, 3, 3], TIMBER, ends=TIMBER_END)
    if corner:
        e = element([0, 0, 3], [3, 3, 16], {'all': 'k'}, {**{k: sw(TIMBER) for k in FACES}, 'south': sw(TIMBER_END)})
        p.append(e)
    return p


def cap():
    """The top of a brick pier: the last courses of brick (they follow on from the block under it), a stone slab that oversails
    them, and a smaller one on that."""
    p = [element([0, 0, 0], [16, 9, 16], {'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})]
    for lo, hi in (([-1.5, 9, -1.5], [17.5, 12.5, 17.5]), ([1, 12.5, 1], [15, 14.5, 15])):
        w, d, t = hi[0] - lo[0], hi[2] - lo[2], hi[1] - lo[1]
        side = [0, 0, min(16, w), t]
        p.append(element(lo, hi, {'all': 'c'}, {'north': side, 'south': side, 'east': side, 'west': side, 'up': [0, 0, 16, 16], 'down': [0, 0, 16, 16]}))
    return p


def litter_model(k):
    p = []
    plate(p, [0, .4, 0], [16, .4, 16], f'f{k}', {'up': (0, 0)})
    return p


def note_model():
    p = []
    plate(p, [4.5, 4, 15.7], [11.5, 13, 15.95], 'n', {'north': (0, 0)})
    return p


FITTINGS = {
    'fence_0': (lambda: fence(False), 'park'), 'fence_1': (lambda: fence(True), 'park'),
    'gate_0': (lambda: gate(0), 'park'), 'gate_1': (lambda: gate(1), 'park'), 'gate_2': (lambda: gate(2), 'park'), 'gate_3': (lambda: gate(3), 'park'),
    'lamp_0': (lambda: lamp(0), 'park'), 'lamp_1': (lambda: lamp(1), 'park'), 'lamp_2': (lambda: lamp(2), 'lamp_lit'), 'lamp_3': (lambda: lamp(3), 'lamp_dark'),
    'bench_0': (lambda: bench(('west', 'east')), 'park'), 'bench_1': (lambda: bench(('west',)), 'park'), 'bench_2': (lambda: bench(('east',)), 'park'),
    'bench_3': (lambda: bench(('west',), True), 'park'),
    'fountain_0': (lambda: fountain(False), 'park'), 'fountain_1': (lambda: fountain(True), 'park'),
    'phone_0': (lambda: phone(False), 'park'), 'phone_1': (lambda: phone(True), 'park'),
    'bin_0': (bin_, 'park'), 'border_0': (lambda: border(False), 'park'), 'border_1': (lambda: border(True), 'park'), 'cap_0': (cap, '/calder_kerb'),
    'litter_0': (lambda: litter_model(0), 'litter_0'), 'litter_1': (lambda: litter_model(1), 'litter_1'),
    'litter_2': (lambda: litter_model(2), 'litter_2'), 'litter_3': (lambda: litter_model(3), 'litter_3'),
    'note_0': (note_model, 'note'),
}
# A name that begins with a slash is one of the materials, in the blocks' own folder.
NAMES_OF = {'k': 'park', 'h': 'carving', 'w': 'water', 'l': 'lamp_lit', 'd': 'lamp_dark', 'm': 'mesh', 'f0': 'litter_0', 'f1': 'litter_1', 'f2': 'litter_2', 'f3': 'litter_3', 'n': 'note',
            'b': '/calder_brick', 'c': '/calder_kerb'}
KINDS = ['fence', 'gate', 'lamp', 'bench', 'bin', 'border', 'note', 'cap', 'fountain', 'phone']
STAGES = {'fence': 2, 'gate': 4, 'lamp': 4, 'bench': 4, 'border': 2, 'fountain': 2, 'phone': 2}
MAX_STAGE = 4


def path(name):
    return NS + 'block' + name if name.startswith('/') else P + name


def model(parts, particle):
    keys = {f['texture'][1:] for e in parts for f in e['faces'].values()}
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: path(NAMES_OF[k]) for k in sorted(keys)}, 'particle': path(particle)}, 'elements': parts}


def fitting_models():
    return {key: model(build(), particle) for key, (build, particle) in FITTINGS.items()}


def files():
    out = {}
    for name in materials():
        if name == 'road_line':
            out['models/block/calder_road_line.json'] = {'parent': 'minecraft:block/cube', 'textures': {
                'up': NS + 'block/calder_road_line', 'down': NS + 'block/calder_asphalt', 'north': NS + 'block/calder_asphalt',
                'south': NS + 'block/calder_asphalt', 'east': NS + 'block/calder_asphalt', 'west': NS + 'block/calder_asphalt',
                'particle': NS + 'block/calder_asphalt'}}
            out['blockstates/calder_road_line.json'] = {'variants': {'axis=x': {'model': NS + 'block/calder_road_line', 'y': 90},
                                                                    'axis=y': {'model': NS + 'block/calder_road_line'},
                                                                    'axis=z': {'model': NS + 'block/calder_road_line'}}}
            continue
        m = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/calder_{name}'}}
        if name == 'leaves':
            m['render_type'] = 'minecraft:cutout_mipped'
        out[f'models/block/calder_{name}.json'] = m
        out[f'blockstates/calder_{name}.json'] = {'variants': {'': {'model': NS + f'block/calder_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/{DIR}{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': f'{P}{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/calder_fitting.json'] = {'variants': variants}
    out['blockstates/calder_litter.json'] = {'variants': {f'facing={facing},scatter={k}': {'model': f'{P}litter_{k}', **({'y': y} if y else {})}
                                                          for facing, y in FACING_Y.items() for k in range(4)}}
    return out


NAMES = {'block.the_oldest_house.calder_fitting': 'Park Fitting', 'block.the_oldest_house.calder_brick': 'Park Brick',
         'block.the_oldest_house.calder_asphalt': 'Asphalt', 'block.the_oldest_house.calder_road_line': 'Road Line',
         'block.the_oldest_house.calder_paving': 'Concrete Flags', 'block.the_oldest_house.calder_kerb': 'Kerbstone',
         'block.the_oldest_house.calder_leaves': 'October Leaves', 'block.the_oldest_house.calder_litter': 'Fallen Leaves'}


# ---------------------------------------------------------------------------------------------------------------- review
def review(path):
    from draw_bike_night_art import seen
    images = textures(); scale = 14; cells = []
    for key, (build, _) in FITTINGS.items():
        for view in ('side', 'front', 'top'):
            cells.append(seen(build(), {k: v[1:] if v.startswith('/') else 'calder/' + v for k, v in NAMES_OF.items()}, images, view, scale, (-4, 20, -1, 18), 'block/'))
    w, h = cells[0].size
    cols = 9
    rows = (len(cells) + cols - 1) // cols
    page = Image.new('RGBA', (cols * (w + 4) + 4, rows * (h + 4) + 4 + 72), (30, 32, 36, 255))
    for i, one in enumerate(cells):
        page.alpha_composite(one, (4 + (i % cols) * (w + 4), 4 + (i // cols) * (h + 4)))
    x = 4
    for rel, im in sorted(images.items()):
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
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    assert all(f'litter_{k}' in models for k in range(4))
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
    print(('Verified' if check else 'Drew') + f" Calder Park: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
