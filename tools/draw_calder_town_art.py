"""Bike night (0.4.481): Calder Falls, the town the chase goes through.

Draws, in native Minecraft pixels from fixed seeds, what the town is built of that the park was not: its materials (stucco, tan
brick, clapboard, concrete, a tarred roof, shingles, the lots' asphalt, porch boards, gravel, and the school field's turf, plain,
with a tyre's track through its dew, and with a yard line chalked down it), and its own fittings, each a kind of one block (calder_town): chain-link fence, whole and
torn; a bleacher's plank on its frame; carved pumpkins, four faces, lit from inside; striped awnings; a fire hydrant; a post box; a
barber's pole, which turns; toilet paper hung in a tree; and a grocery cart. And the bank clock's digits (calder_digit).

And what is behind the shops (the owner: "clutter and decor in alleys would benefit it too. space between buildings, dumpsters,
trash"): a dumpster, two blocks long, shut and with its lid thrown back; trash bags; a packing crate, a milk crate and pallets;
cardboard boxes, whole and flat; oil drums; tyres; what lies on the ground (newspaper, cans, wrappers, a puddle); a drainpipe; what
is fixed to a back wall (a meter, an air conditioner, a caged bulb, a vent); and a board fence, whole and with boards gone. And a
hedge (calder_hedge).

Nothing here is a photograph or a stock texture. A fitting is drawn facing north; its block state turns it. Writes the textures,
models, block states and the blocks' names. --check verifies everything. --sheet PATH draws a page for review. Drawing or checking,
it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
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
DIR = 'block/calder_town/'
P = NS + DIR
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}

# ---------------------------------------------------------------------------------------------------------------- the paint card
STEEL, IRON, WOOD, WOOD_DK, RED, WHITE, BLUE, ORANGE, ORANGE_DK, STEM, CANVAS, CHROME, YELLOW, CONCRETE, BLACK, GREEN = range(16)
CARD = ('#8f9396', '#2a2d2f', '#7a6248', '#4f3e2c', '#b3261f', '#e9e6dc', '#2b4f8f', '#d9731a',
        '#a8540f', '#4a5a2a', '#c9bfa6', '#c9ccd0', '#d9b21f', '#a09d94', '#18191b', '#2f5a3a')


def card():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4800); px = im.load()
    for i, colour in enumerate(CARD):
        for y in range(4):
            for x in range(4):
                px[4 * (i % 4) + x, 4 * (i // 4) + y] = shade(colour, rnd.randint(-2, 2))
    return im


def sw(i):
    x, y = 4 * (i % 4) + 1, 4 * (i // 4) + 1
    return [x, y, x + 1, y + 1]


# The alleys' paint: cardboard, rust, a dumpster's green and its shadow, a trash bag and where it shines, a drainpipe, a milk crate,
# rubber and its edge, newsprint, a dumpster's lid, an oil drum and its bands, and a fence board and its shadow.
KRAFT, RUST, DUMP, DUMP_DK, BAG, BAG_HI, DRAIN, MILK, RUBBER, RUBBER_HI, PAPER, LID, DRUM, DRUM_DK, BOARD, BOARD_DK = range(16)
CARD2 = ('#a8875a', '#7a4a2a', '#3d6a5a', '#2c4e42', '#1c1d20', '#383a3e', '#5c6166', '#b83a2e',
         '#17181a', '#303236', '#d9d6c8', '#22282a', '#2b4f8f', '#1f3a6a', '#8a7658', '#5e4e3a')


def card2():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4900); px = im.load()
    for i, colour in enumerate(CARD2):
        for y in range(4):
            for x in range(4):
                px[4 * (i % 4) + x, 4 * (i // 4) + y] = shade(colour, rnd.randint(-2, 2))
    return im


# ---------------------------------------------------------------------------------------------------------------- materials
def stucco():
    """Cream render, a little dirty, with the trowel's low places."""
    im = new('#d6ccb2', 4801, 3); rnd = random.Random(4802)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#d6ccb2', -rnd.randint(8, 16)))
    return im


def tanbrick():
    """Tan brick in stretcher bond: the shops on Main Street."""
    im = new('#8c8478', 4803, 3); rnd = random.Random(4804)
    for course in range(4):
        y0 = course * 4; shift = 4 if course % 2 else 0
        for k in range(-1, 3):
            x0 = k * 8 + shift; tone = rnd.randint(-8, 8); tan = '#b08a5e' if rnd.random() < .75 else '#a27c54'
            for y in range(y0, y0 + 3):
                for x in range(x0, x0 + 7):
                    dot(im, x, y, shade(tan, tone + rnd.randint(-4, 4)))
    return im


def siding():
    """White clapboard: four boards to the block, each in the shadow of the one above."""
    im = new('#d9d6cc', 4805, 2)
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                dot(im, x, y, shade('#d9d6cc', -34))
            elif y % 4 == 0:
                dot(im, x, y, shade('#d9d6cc', 8))
    return im


def concrete():
    """Poured concrete: grey, pitted, with one board mark."""
    im = new('#a09d94', 4806, 3); rnd = random.Random(4807)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#a09d94', -rnd.randint(12, 22)))
    for x in range(16):
        dot(im, x, 9, shade('#a09d94', -9))
    return im


def roofing():
    """A flat roof: tar, and gravel in it."""
    im = new('#2b2c2e', 4808, 4); rnd = random.Random(4809)
    for _ in range(26):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#55565a', rnd.randint(-10, 14)))
    return im


def shingle():
    """Asphalt shingles on a house: four courses, the tabs staggered."""
    im = new('#553228', 4810, 4)
    for y in range(16):
        for x in range(16):
            if y % 4 == 3:
                dot(im, x, y, shade('#553228', -22))
            elif (x + (4 if (y // 4) % 2 else 0)) % 8 == 0:
                dot(im, x, y, shade('#553228', -14))
    return im


def lot():
    """A parking lot: asphalt older and paler than the street's, patched."""
    im = new('#424447', 4811, 4); rnd = random.Random(4812)
    for _ in range(18):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#5a5c60', rnd.randint(-8, 10)))
    return im


def lot_line():
    """A painted bay line across a lot."""
    im = lot(); rnd = random.Random(4813)
    for y in range(16):
        for x in (7, 8):
            if rnd.random() < .9:
                dot(im, x, y, shade('#c9c6b8', rnd.randint(-12, 6)))
    return im


def plank():
    """Porch boards, painted grey and worn to the wood where feet go."""
    im = new('#7f8386', 4814, 3); rnd = random.Random(4815)
    for y in range(16):
        for x in range(16):
            if x % 4 == 3:
                dot(im, x, y, shade('#7f8386', -30))
            elif rnd.random() < .06:
                dot(im, x, y, shade('#8a7256', rnd.randint(-6, 6)))
    return im


def gravel():
    """An alley's gravel."""
    im = new('#8b877c', 4816, 7); rnd = random.Random(4817)
    for _ in range(30):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade(rnd.choice(('#a39f93', '#6f6c63', '#97907f')), rnd.randint(-6, 6)))
    return im


def turf(track=False, chalk=False):
    """The school field: mown grass with the dew on it. With a track, two lines where a bike's tyres have taken the dew off. With
    chalk, a yard line down it, worn where the season's games went over it."""
    im = new('#34522f', 4818, 5); rnd = random.Random(4819)
    for _ in range(34):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#8fae8a', rnd.randint(-10, 18)))
    if track:
        for y in range(16):
            for x in (6, 7, 9, 10):
                dot(im, x, y, shade('#27401f', rnd.randint(-4, 4)))
    if chalk:
        for y in range(16):
            for x in (7, 8):
                if rnd.random() < .86:
                    dot(im, x, y, shade('#d2d6cc', rnd.randint(-16, 6)))
    return im


def hedge():
    """A clipped hedge, dark, with the leaves that catch a lamp and the holes between."""
    im = new('#27402a', 4940, 6); rnd = random.Random(4941)
    for _ in range(26):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#3f6a3a', rnd.randint(-10, 12)))
    for _ in range(16):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#162618', rnd.randint(-4, 4)))
    return im


def materials():
    return {'stucco': stucco(), 'tanbrick': tanbrick(), 'siding': siding(), 'concrete': concrete(), 'roofing': roofing(), 'shingle': shingle(),
            'lot': lot(), 'lot_line': lot_line(), 'plank': plank(), 'gravel': gravel(), 'turf': turf(), 'turf_track': turf(True), 'turf_line': turf(chalk=True), 'hedge': hedge()}


# ---------------------------------------------------------------------------------------------------------------- pictures
def mesh(torn=0):
    """Chain-link: galvanized wire in diamonds, nothing between. Torn: the same with the wire cut and curled back from a hole, the
    lower block's hole open to the ground (1) and the upper block's closing over it (2)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4820 + torn)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                hole = torn == 1 and 3 <= x <= 12 and y >= 2 or torn == 2 and 4 <= x <= 11 and y >= 9
                if hole:
                    continue
                dot(im, x, y, shade('#9da1a4', rnd.randint(-14, 10)))
    if torn:
        # The cut ends, bright.
        edge = [(3, y) for y in range(2, 16)] + [(12, y) for y in range(2, 16)] if torn == 1 else [(x, 9) for x in range(4, 12)]
        for x, y in edge:
            if rnd.random() < .6:
                dot(im, x, y, shade('#d0d3d6', rnd.randint(-8, 8)))
    return im


FACES_CUT = (
    # Each face is where the rind is cut through: eyes, nose, mouth. Ten wide, nine high.
    ('..........', '.##....##.', '.##....##.', '..........', '....##....', '..........', '.#.#..#.#.', '.########.', '..#.##.#..'),
    ('..........', '.#......#.', '.##....##.', '.###..###.', '..........', '....##....', '.#......#.', '..######..', '..........'),
    ('..........', '..#....#..', '.###..###.', '..#....#..', '..........', '...####...', '..#....#..', '..#.##.#..', '...####...'),
    ('..........', '.###..###.', '..#....#..', '..........', '....#.....', '...###....', '.#......#.', '.##.##.##.', '..######..'),
)


def pumpkin_side(seed):
    """A pumpkin's rind: orange, ribbed top to bottom."""
    im = new('#d9731a', 4830 + seed, 5)
    for y in range(16):
        for x in range(16):
            if x % 3 == 0:
                dot(im, x, y, shade('#a8540f', (x * 7 + y * 3) % 9 - 4))
    return im


def pumpkin_cut(k):
    """Only what is cut out of face k, as the candle shows it: for a second sheet over the rind, drawn at full brightness."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4840 + k)
    for y, row in enumerate(FACES_CUT[k]):
        for x, c in enumerate(row):
            if c == '#':
                dot(im, x, y, shade('#ffd65a', rnd.randint(-22, 10)))
    return im


def awning(k):
    """Awning canvas in stripes: red, green, or blue with cream."""
    colour = ('#a8322a', '#2f5a3a', '#2b4f8f')[k]
    im = new('#ded5bd', 4850 + k, 3); rnd = random.Random(4853 + k)
    for y in range(16):
        for x in range(16):
            if (x // 2) % 2 == 0:
                dot(im, x, y, shade(colour, rnd.randint(-5, 5)))
    return im


def pole():
    """A barber's pole, eight frames of it: red and blue stripes going up white as it turns."""
    im = Image.new('RGBA', (16, 16 * 8), (0, 0, 0, 0)); px = im.load()
    for f in range(8):
        for y in range(16):
            for x in range(16):
                band = (x + y + f * 2) % 16
                c = '#b3261f' if band < 4 else '#2b4f8f' if 8 <= band < 12 else '#ece8dc'
                px[x, f * 16 + y] = shade(c, (x * 5 + y * 3) % 5 - 2)
    return im


def streamers():
    """Toilet paper thrown over a branch: white strips hanging, each a different length."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4860)
    for x0, length in ((1, 15), (4, 10), (7, 16), (10, 12), (13, 7)):
        sway = 0
        for y in range(length):
            if rnd.random() < .3:
                sway += rnd.choice((-1, 0, 1)); sway = max(-1, min(1, sway))
            for x in (x0 + sway, x0 + sway + 1):
                dot(im, x, y, shade('#eeece4', rnd.randint(-16, 4)))
    return im


def wire():
    """A grocery cart's basket: chromed wire in squares."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4861)
    for y in range(16):
        for x in range(16):
            if x % 3 == 0 or y % 3 == 0:
                dot(im, x, y, shade('#c2c6ca', rnd.randint(-18, 12)))
    return im


SEGMENTS = {'0': 'abcdef', '1': 'bc', '2': 'abdeg', '3': 'abcdg', '4': 'bcfg', '5': 'acdfg', '6': 'acdefg', '7': 'abc', '8': 'abcdefg', '9': 'abcdfg'}


def digit(k):
    """One place of the bank clock: seven bars lit amber on black. 10 is the colon; 11 is the degree mark and its F."""
    im = new('#0d0e10', 4870, 1)
    lit = '#ffb23a'

    def seg(name):
        boxes = {'a': (5, 2, 11, 3), 'b': (11, 3, 12, 7), 'c': (11, 8, 12, 12), 'd': (5, 12, 11, 13), 'e': (4, 8, 5, 12), 'f': (4, 3, 5, 7), 'g': (5, 7, 11, 8)}
        x0, y0, x1, y1 = boxes[name]
        fill(im, x0, y0, x1, y1, lit)
    if k < 10:
        for s in SEGMENTS[str(k)]:
            seg(s)
    elif k == 10:
        fill(im, 7, 4, 9, 6, lit); fill(im, 7, 9, 9, 11, lit)
    else:
        fill(im, 3, 2, 6, 3, lit); fill(im, 3, 4, 6, 5, lit); fill(im, 3, 3, 4, 4, lit); fill(im, 5, 3, 6, 4, lit)
        fill(im, 8, 4, 12, 5, lit); fill(im, 8, 5, 9, 12, lit); fill(im, 9, 7, 11, 8, lit)
    return im


def crate_side():
    """A packing crate's side: boards in a frame, and a brace across them."""
    im = new('#8a6f4e', 4901, 4)
    for y in range(16):
        for x in range(16):
            if x in (1, 2, 13, 14) or y in (1, 2, 13, 14):
                dot(im, x, y, shade('#6a5238', (x * 3 + y * 5) % 7 - 3))
            elif y % 4 == 1:
                dot(im, x, y, shade('#8a6f4e', -28))
    for k in range(3, 13):
        dot(im, k, 15 - k, shade('#6a5238', -6)); dot(im, k + 1, 15 - k, shade('#6a5238', 6))
    return im


def milk_crate():
    """A plastic crate: a red lattice with nothing between. One picture gives a side (ten wide and eight high, from 3,5: a band two
    deep round its top, a bar across its middle and its foot, a post at each end and two between) and the floor (ten square, from
    3,6: the same posts, and a bar every third row).

    0.4.519, the owner, of this crate: "the mirroring on this crate doesn't work too well". Its bars had been every fourth pixel
    of a side ten wide, so a side had a post at one end and none at the other, a corner was a post from one side and a gap from the
    next, and nothing was drawn of a wall's inside. Now a side is the same read from either end, its paint included, so a wall's
    inside (the same picture, seen from behind) lies exactly on its outside."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4902)
    tone = [[rnd.randint(-10, 8) for _ in range(5)] for _ in range(16)]
    for y in range(5, 16):
        for x in range(3, 13):
            if x % 3 == 0 or y in (5, 6, 9, 12, 15):
                dot(im, x, y, shade('#b83a2e', tone[y][min(x - 3, 12 - x)]))
    return im


def cardboard():
    """A cardboard box: kraft, the tape down its middle, an arrow printed on it."""
    im = new('#a8875a', 4903, 4)
    for y in range(16):
        for x in range(16):
            if x in (7, 8):
                dot(im, x, y, shade('#c9b48a', (y * 3) % 5 - 2))
            elif y % 5 == 4:
                dot(im, x, y, shade('#a8875a', -12))
    for x, y in ((12, 3), (11, 4), (12, 4), (13, 4), (12, 5), (12, 6), (12, 7)):
        dot(im, x, y, '#5a4630')
    return im


def cardboard_flat():
    """Boxes flattened and left on the ground: three sheets, one over another."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4904)
    for (x0, y0, x1, y1), tone in (((1, 2, 11, 9), -8), ((6, 6, 15, 14), 4), ((2, 10, 8, 15), -2)):
        for y in range(y0, y1):
            for x in range(x0, x1):
                edge = x in (x0, x1 - 1) or y in (y0, y1 - 1)
                dot(im, x, y, shade('#a8875a', tone + (-18 if edge else rnd.randint(-5, 5))))
    return im


def trash(k):
    """What lies on an alley's ground: 0 a newspaper come apart, 1 cans and a bottle, 2 a burger's box and a cup, 3 a puddle."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4910 + k)
    if k == 0:
        for x0, y0, x1, y1 in ((1, 2, 8, 9), (7, 8, 15, 14)):
            for y in range(y0, y1):
                for x in range(x0, x1):
                    ink = (y - y0) % 2 == 1 and x0 < x < x1 - 1
                    dot(im, x, y, shade('#b9b5a8', rnd.randint(-6, 6)) if ink else shade('#d8d4c6', rnd.randint(-10, 4)))
    elif k == 1:
        fill(im, 2, 3, 6, 5, '#b3261f'); dot(im, 2, 3, '#d8d8d8'); dot(im, 5, 4, '#7a1a15')
        fill(im, 10, 9, 14, 11, '#c9ccd0'); dot(im, 13, 10, '#8f9396')
        fill(im, 4, 11, 9, 13, '#3a5a3a'); fill(im, 9, 11, 11, 12, '#3a5a3a'); dot(im, 5, 11, '#6a8a6a')
        fill(im, 11, 2, 13, 4, '#d9b21f')
    elif k == 2:
        fill(im, 2, 9, 7, 13, '#e9e6dc'); fill(im, 3, 10, 6, 12, '#b3261f')
        fill(im, 9, 3, 12, 7, '#e9e6dc'); fill(im, 9, 4, 12, 5, '#d9b21f'); dot(im, 10, 2, '#b3261f')
        for x, y in ((13, 11), (14, 12), (12, 13), (6, 4), (5, 5)):
            dot(im, x, y, '#c9bfa6')
    else:
        for y in range(16):
            for x in range(16):
                if ((x - 7.5) / 7) ** 2 + ((y - 8) / 5) ** 2 + rnd.random() * .25 < 1:
                    dot(im, x, y, shade('#23282e', rnd.randint(-4, 4)))
        for x, y in ((5, 6), (6, 6), (9, 9), (10, 9), (4, 9)):
            dot(im, x, y, '#5a6470')
    return im


def grille():
    """Louvres: an air conditioner's, or a vent's."""
    im = new('#8f9396', 4920, 3)
    for y in range(2, 14, 2):
        for x in range(2, 14):
            dot(im, x, y, shade('#3a3d40', (x * 3) % 5 - 2))
    return im


def boards(kind):
    """A board fence: four boards to the block, weathered, a dark line where two meet. 0 the lower block; 1 the upper, the boards'
    corners cut; 2 and 3 the same two with the middle two boards gone, one of them hanging in the lower block by its last nail."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(4930 + kind)
    for b in range(4):
        if kind >= 2 and b in (1, 2):
            continue
        tone = rnd.randint(-10, 10)
        for y in range(16):
            for x in range(b * 4, b * 4 + 4):
                if kind in (1, 3) and y == 0 and x in (b * 4, b * 4 + 2, b * 4 + 3):
                    continue
                seam = x == b * 4 + 3
                dot(im, x, y, shade('#5e4e3a', -14) if seam else shade('#8a7658', tone + rnd.randint(-5, 5) - (8 if (y * 7 + b * 3) % 11 == 0 else 0)))
    if kind == 2:
        for y in range(3, 16):
            x = 5 + (y - 3) * 5 // 13
            for dx in (0, 1, 2):
                dot(im, x + dx, y, shade('#8a7658', -6 + dx * 4))
    return im


def textures():
    out = {f'block/calder_{name}.png': im for name, im in materials().items()}
    out[DIR + 'town.png'] = card()
    out[DIR + 'town2.png'] = card2()
    out[DIR + 'crate.png'] = crate_side()
    out[DIR + 'milk_crate.png'] = milk_crate()
    out[DIR + 'cardboard.png'] = cardboard()
    out[DIR + 'cardboard_flat.png'] = cardboard_flat()
    for k in range(4):
        out[DIR + f'trash_{k}.png'] = trash(k)
        out[DIR + f'boards_{k}.png'] = boards(k)
    out[DIR + 'grille.png'] = grille()
    for k, name in enumerate(('mesh', 'mesh_torn', 'mesh_torn_top')):
        out[DIR + name + '.png'] = mesh(k)
    for k in range(4):
        out[DIR + f'pumpkin_{k}.png'] = pumpkin_side(k)
        out[DIR + f'pumpkin_cut_{k}.png'] = pumpkin_cut(k)
    for k in range(3):
        out[DIR + f'awning_{k}.png'] = awning(k)
    out[DIR + 'pole.png'] = pole()
    out[DIR + 'streamers.png'] = streamers()
    out[DIR + 'wire.png'] = wire()
    for k in range(12):
        out[DIR + f'digit_{k}.png'] = digit(k)
    return out


# ---------------------------------------------------------------------------------------------------------------- pieces
def bar(parts, lo, hi, swatch, rot=None):
    uv = {k: sw(swatch) for k in FACES}
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': 'k'}, uv, rot))


def bar2(parts, lo, hi, swatch, rot=None):
    """A piece in the alleys' paint."""
    uv = {k: sw(swatch) for k in FACES}
    parts.append(element([round(v, 3) for v in lo], [round(v, 3) for v in hi], {'all': 'j'}, uv, rot))


def plate(parts, lo, hi, tex, faces, glow=False, rot=None):
    """A piece that shows a picture: each face named takes the part of the picture its own size, from the corner given."""
    lo, hi = [round(v, 3) for v in lo], [round(v, 3) for v in hi]
    size = {'north': (hi[0] - lo[0], hi[1] - lo[1]), 'south': (hi[0] - lo[0], hi[1] - lo[1]), 'east': (hi[2] - lo[2], hi[1] - lo[1]),
            'west': (hi[2] - lo[2], hi[1] - lo[1]), 'up': (hi[0] - lo[0], hi[2] - lo[2]), 'down': (hi[0] - lo[0], hi[2] - lo[2])}
    uv = {}
    for k, (u, v) in faces.items():
        w, h = size[k]
        uv[k] = [u, v, round(min(16, u + w), 3), round(min(16, v + h), 3)]
    e = element(lo, hi, {k: tex for k in faces}, uv, rot)
    if glow:
        e['shade'] = False
        e['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    parts.append(e)


def chainlink(stage):
    """One block of fence on the block's middle line, a post at its west edge: 0 the lower block, with the bottom rail; 1 the upper,
    with the top rail; 2 and 3 the same two with the wire cut and pulled back from a hole a child on a bike goes through."""
    p = []
    bar(p, [0, 0, 7.2], [1.2, 16, 8.8], STEEL)
    tex = ('m', 'm', 't', 'u')[stage]
    plate(p, [1.2, 0, 7.95], [16, 16, 8.05], tex, {'north': (1.2, 0), 'south': (0, 0)})
    if stage in (0, 2):
        if stage == 0:
            bar(p, [1.2, .6, 7.6], [16, 1.4, 8.4], STEEL)
    else:
        bar(p, [1.2, 14.8, 7.5], [16, 15.8, 8.5], STEEL)
    return p


def bleacher():
    """A bleacher's seat: one plank along the block at its top, a foot board lower and in front, on a steel frame at each end."""
    p = []
    bar(p, [0, 13, 4], [16, 15, 13], WOOD)
    bar(p, [0, 6, 12.5], [16, 7, 16], WOOD_DK)
    for x in (1, 14):
        bar(p, [x, 0, 7.5], [x + 1, 13, 8.5], STEEL)
        bar(p, [x, 5, 8.5], [x + 1, 6, 16], STEEL)
    return p


def pumpkin(k):
    """A carved pumpkin, lit: its face looks north."""
    p = []
    plate(p, [3, 0, 3], [13, 9, 13], f'p{k}', {'north': (3, 7), 'south': (3, 7), 'east': (3, 7), 'west': (3, 7), 'up': (3, 3), 'down': (3, 3)})
    plate(p, [3, 0, 2.95], [13, 9, 3], f'c{k}', {'north': (0, 0)}, glow=True)
    bar(p, [7.2, 9, 7.2], [8.8, 11, 8.8], STEM)
    return p


def awning_model(k):
    """An awning over a shop's front: canvas sloping down from the wall behind (south) to a scalloped edge, on two iron arms."""
    p = []
    rot = {'angle': 22.5, 'axis': 'x', 'origin': [8, 12, 16]}
    plate(p, [0, 11.5, 2], [16, 12, 16], f'a{k}', {'up': (0, 0), 'down': (0, 0)}, rot=rot)
    plate(p, [0, 4.2, .2], [16, 6.4, .7], f'a{k}', {'north': (0, 0), 'south': (0, 0)})
    for x in (.3, 15):
        bar(p, [x, 5.6, .7], [x + .7, 6.3, 15.5], IRON)
    return p


def hydrant():
    p = []
    bar(p, [6, 0, 6], [10, 9, 10], RED)
    bar(p, [5.4, 9, 5.4], [10.6, 10.4, 10.6], RED)
    bar(p, [7, 10.4, 7], [9, 12, 9], YELLOW)
    bar(p, [4, 5, 7], [12, 7, 9], RED)
    bar(p, [7, 4.6, 4.4], [9, 7.4, 6], YELLOW)
    return p


def mailbox():
    """A post box on four short legs, its pull-down door on the street side."""
    p = []
    bar(p, [3, 3, 4], [13, 13.5, 12], BLUE)
    bar(p, [3.6, 13.5, 4.6], [12.4, 14.6, 11.4], BLUE)
    bar(p, [4.2, 10, 3.6], [11.8, 12.4, 4], WHITE)
    for x in (3.4, 11.6):
        for z in (4.4, 10.6):
            bar(p, [x, 0, z], [x + 1, 3, z + 1], IRON)
    return p


def pole_model():
    """A barber's pole on the wall behind (south): the striped cylinder between two chrome caps, on a bracket."""
    p = []
    plate(p, [6, 3, 10.5], [10, 13, 14.5], 'b', {'north': (6, 3), 'east': (6, 3), 'west': (6, 3)}, glow=True)
    bar(p, [5.5, 2, 10], [10.5, 3, 15], CHROME)
    bar(p, [5.5, 13, 10], [10.5, 14, 15], CHROME)
    bar(p, [7.2, 14, 11.7], [8.8, 15, 13.3], CHROME)
    bar(p, [7.4, 7.2, 14.5], [8.6, 8.8, 16], IRON)
    return p


def tp_model():
    p = []
    plate(p, [0, 0, 7.98], [16, 16, 8.02], 's', {'north': (0, 0), 'south': (0, 0)})
    plate(p, [7.98, 0, 0], [8.02, 16, 16], 's', {'east': (0, 0), 'west': (0, 0)})
    return p


def cart():
    """A grocery cart: a wire basket on a frame, four castors, a red handle at the back (south)."""
    p = []
    plate(p, [3.5, 5.5, 2], [12.5, 12, 2.05], 'w', {'north': (3.5, 4), 'south': (3.5, 4)})
    plate(p, [3.5, 5.5, 13.95], [12.5, 12, 14], 'w', {'north': (3.5, 4), 'south': (3.5, 4)})
    plate(p, [3.5, 5.5, 2.05], [3.55, 12, 13.95], 'w', {'east': (2, 4), 'west': (2, 4)})
    plate(p, [12.45, 5.5, 2.05], [12.5, 12, 13.95], 'w', {'east': (2, 4), 'west': (2, 4)})
    plate(p, [3.55, 5.5, 2.05], [12.45, 5.55, 13.95], 'w', {'up': (3.5, 2), 'down': (3.5, 2)})
    bar(p, [3.5, 2.2, 3], [12.5, 2.8, 13.5], CHROME)
    for x in (4, 11):
        bar(p, [x, 2.8, 12.5], [x + 1, 5.5, 13.3], CHROME)
        for z in (3, 12.2):
            bar(p, [x, .2, z], [x + 1, 2.2, z + 1.2], BLACK)
    bar(p, [3.5, 12.6, 14.2], [12.5, 13.4, 15], RED)
    for x in (3.5, 11.7):
        bar(p, [x, 11, 14], [x + .8, 12.6, 14.8], CHROME)
    return p


def dumpster(stage):
    """Half of a steel bin two blocks long, its back to the wall behind (south). 0 the half whose end is at the block's west edge, 1
    the half whose end is at its east; 2 and 3 the same two with the lid thrown back against the wall and the bags showing."""
    p = []
    west = stage in (0, 2)
    x0, x1 = (1, 16) if west else (0, 15)
    bar2(p, [x0, 1, 3], [x1, 11, 14], DUMP)
    bar2(p, [x0 - (.5 if west else 0), 11, 2.5], [x1 + (0 if west else .5), 12, 14.5], DUMP_DK)
    end = 0 if west else 15
    bar2(p, [end, 5, 5], [end + 1, 8, 12], DUMP_DK)
    for z in (4, 11.5):
        bar(p, [2 if west else 12.5, 0, z], [3.5 if west else 14, 1, z + 1.5], BLACK)
    if stage < 2:
        bar2(p, [x0, 12, 3], [x1, 13, 14], LID)
    else:
        bar2(p, [x0, 12, 13.2], [x1, 22, 14.2], LID)
        bar2(p, [x0 + (1 if west else .5), 11.2, 4], [x1 - (.5 if west else 1), 12.6, 12.6], BAG)
        bar2(p, [5 if west else 3, 12.6, 6], [11 if west else 9, 13.6, 11], BAG_HI)
    return p


def bags(stage):
    """Trash bags, tied at the neck: 0 one, 1 two, 2 three, 3 one torn open with what was in it across the ground."""
    p = []

    def bag(x0, z0, x1, z1, h):
        bar2(p, [x0, 0, z0], [x1, h, z1], BAG)
        bar2(p, [x0 + 1, h, z0 + 1], [x1 - 1, h + 1.2, z1 - 1], BAG_HI)
        cx, cz = (x0 + x1) / 2, (z0 + z1) / 2
        bar2(p, [cx - .8, h + 1.2, cz - .8], [cx + .8, h + 3, cz + .8], BAG)
    if stage == 0:
        bag(4, 5, 11, 12, 6)
    elif stage == 1:
        bag(1, 6, 8, 13, 6); bag(8.5, 3, 15, 10, 5)
    elif stage == 2:
        bag(1, 8, 8, 15, 6); bag(8.5, 8.5, 15, 15, 5); bag(4.5, 1, 11.5, 7.5, 5.5)
    else:
        bag(2, 7, 9, 14, 6)
        plate(p, [0, .04, 0], [16, .06, 16], 'r2', {'up': (0, 0)})
    return p


def crate(stage):
    """0 a packing crate; 1 a milk crate; 2 a pallet, flat; 3 a pallet stood against the wall behind (south)."""
    p = []
    if stage == 0:
        plate(p, [1, 0, 1], [15, 14, 15], 'x', {'north': (1, 1), 'south': (1, 1), 'east': (1, 1), 'west': (1, 1), 'up': (1, 1), 'down': (1, 1)})
    elif stage == 1:
        # Four walls, each with an outside, an inside and a rim, and a floor seen from over it and from under.
        side, floor, wall = (3, 5), (3, 6), .6
        plate(p, [3, 0, 3], [13, 8, 3 + wall], 'y', {'north': side, 'south': side, 'up': side})
        plate(p, [3, 0, 13 - wall], [13, 8, 13], 'y', {'north': side, 'south': side, 'up': side})
        plate(p, [3, 0, 3], [3 + wall, 8, 13], 'y', {'east': side, 'west': side})
        plate(p, [13 - wall, 0, 3], [13, 8, 13], 'y', {'east': side, 'west': side})
        for x in (3, 13 - wall):                                                             # the two rims the long walls do not have
            plate(p, [x, 7.99, 3 + wall], [x + wall, 8, 13 - wall], 'y', {'up': side})
        plate(p, [3, .3, 3], [13, .4, 13], 'y', {'up': floor, 'down': floor})
    elif stage == 2:
        for z in (1, 6.5, 12):
            bar(p, [0, 2, z], [16, 3, z + 3], WOOD)
        for x in (1, 7.25, 13.5):
            bar(p, [x, 0, 1], [x + 1.5, 2, 15], WOOD_DK)
    else:
        rot = {'angle': 22.5, 'axis': 'x', 'origin': [8, 0, 9.5]}
        for y in (.5, 6, 11.5):
            bar(p, [0, y, 8.5], [16, y + 3, 9.5], WOOD, rot)
        for x in (1, 7.25, 13.5):
            bar(p, [x, 0, 9.5], [x + 1.5, 15, 11], WOOD_DK, rot)
    return p


def boxes(stage):
    """Cardboard boxes: 0 one, 1 three in a heap, 2 flattened on the ground, 3 one with its flaps up."""
    p = []

    def box(x0, y0, z0, x1, y1, z1):
        u, w = x0 % 4, z0 % 4
        plate(p, [x0, y0, z0], [x1, y1, z1], 'cb', {'north': (u, 2), 'south': (u, 2), 'east': (w, 2), 'west': (w, 2), 'up': (u, w), 'down': (u, w)})
    if stage == 0:
        box(3, 0, 4, 13, 8, 12)
    elif stage == 1:
        box(1, 0, 6, 9, 7, 14); box(9.5, 0, 4, 15.5, 5, 11); box(3, 7, 7, 10, 12, 13)
    elif stage == 2:
        plate(p, [0, .04, 0], [16, .06, 16], 'cf', {'up': (0, 0)})
    else:
        box(3, 0, 4, 13, 7, 12)
        bar2(p, [3, 7, 3.2], [13, 10, 4], KRAFT); bar2(p, [3, 7, 12], [13, 9.5, 12.8], KRAFT)
    return p


def drum(stage):
    """Oil drums: 0 one, blue; 1 one, rusted; 2 one of each; 3 one on its side along the wall behind."""
    p = []

    def one(x0, z0, colour, bands):
        bar2(p, [x0, 0, z0], [x0 + 7, 13, z0 + 7], colour)
        for y in (4, 8.5):
            bar2(p, [x0 - .4, y, z0 - .4], [x0 + 7.4, y + .8, z0 + 7.4], bands)
        bar(p, [x0 + .5, 13, z0 + .5], [x0 + 6.5, 13.6, z0 + 6.5], STEEL)
    if stage == 0:
        one(4.5, 4.5, DRUM, DRUM_DK)
    elif stage == 1:
        one(4.5, 4.5, RUST, BOARD_DK)
    elif stage == 2:
        one(.5, 6, DRUM, DRUM_DK); one(8.5, 3, RUST, BOARD_DK)
    else:
        bar2(p, [1, 0, 6], [15, 7, 13], DRUM)
        for x in (4.5, 10):
            bar2(p, [x, .3, 5.6], [x + .8, 7.4, 13.4], DRUM_DK)
        bar(p, [.4, .8, 6.8], [1, 6.2, 12.2], STEEL)
    return p


def tyres(stage):
    """Tyres: 0 one, flat; 1 three, stacked; 2 one stood against the wall behind; 3 two small ones side by side."""
    p = []

    def tyre(x0, y0, z0, s=10, t=3):
        bar2(p, [x0, y0, z0], [x0 + s, y0 + 3, z0 + t], RUBBER); bar2(p, [x0, y0, z0 + s - t], [x0 + s, y0 + 3, z0 + s], RUBBER)
        bar2(p, [x0, y0, z0 + t], [x0 + t, y0 + 3, z0 + s - t], RUBBER_HI); bar2(p, [x0 + s - t, y0, z0 + t], [x0 + s, y0 + 3, z0 + s - t], RUBBER_HI)
    if stage == 0:
        tyre(3, 0, 3)
    elif stage == 1:
        tyre(3, 0, 3); tyre(3.6, 3, 2.4); tyre(2.6, 6, 3.4)
    elif stage == 2:
        bar2(p, [3, 0, 12.5], [13, 3, 15.5], RUBBER); bar2(p, [3, 7, 12.5], [13, 10, 15.5], RUBBER)
        bar2(p, [3, 3, 12.5], [6, 7, 15.5], RUBBER_HI); bar2(p, [10, 3, 12.5], [13, 7, 15.5], RUBBER_HI)
    else:
        tyre(.5, 0, 4, 7, 2.5); tyre(8.5, 0, 6, 7, 2.5)
    return p


def trash_model(k):
    p = []
    plate(p, [0, .04, 0], [16, .06, 16], f'r{k}', {'up': (0, 0)})
    return p


def pipe(stage):
    """A drainpipe down the wall behind (south): 0 a length of it, strapped; 1 its foot, turned out; 2 its head, under the gutter."""
    p = []
    if stage == 0:
        bar2(p, [6.5, 0, 13], [9.5, 16, 15.6], DRAIN); bar2(p, [5.6, 7, 12.6], [10.4, 8, 16], BAG_HI)
    elif stage == 1:
        bar2(p, [6.5, 3, 13], [9.5, 16, 15.6], DRAIN); bar2(p, [6.5, 0, 9], [9.5, 3, 15.6], DRAIN)
        bar2(p, [5.6, 10, 12.6], [10.4, 11, 16], BAG_HI)
    else:
        bar2(p, [6.5, 0, 13], [9.5, 10, 15.6], DRAIN); bar2(p, [4.5, 10, 11], [11.5, 14, 16], DRAIN)
    return p


def unit(stage):
    """What is fixed to a back wall (south): 0 an electric meter and its conduit; 1 an air conditioner, its grille to the alley; 2 a
    bulb in a cage, lit; 3 a vent."""
    p = []
    if stage == 0:
        bar(p, [5, 4, 13], [11, 12, 16], STEEL); bar(p, [6.5, 6.5, 12], [9.5, 9.5, 13], CHROME); bar(p, [7.4, 12, 14.4], [8.6, 16, 15.6], IRON)
    elif stage == 1:
        bar(p, [2, 3, 9], [14, 11, 16], STEEL)
        plate(p, [2.5, 3.5, 8.9], [13.5, 10.5, 9], 'g', {'north': (2, 4)})
    elif stage == 2:
        bar(p, [6.5, 10, 13.5], [9.5, 11.5, 16], IRON)
        e = element([7, 7, 13.6], [9, 10, 15.6], {'all': 'k'}, {k: sw(YELLOW) for k in FACES})
        e['shade'] = False; e['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
        p.append(e)
        for x in (6.4, 9.2):
            bar(p, [x, 6.4, 13.2], [x + .4, 10, 13.6], IRON)
        bar(p, [6.4, 6, 13.2], [9.6, 6.4, 16], IRON)
    else:
        bar(p, [3, 4, 15], [13, 12, 16], STEEL)
        plate(p, [3.5, 4.5, 14.9], [12.5, 11.5, 15], 'g', {'north': (3, 4)})
    return p


def boards_model(stage):
    """One block of board fence on the block's middle line, a post at its west edge and a rail along its back: 0 the lower block,
    1 the upper; 2 and 3 the same two with the middle boards gone, which is a way through."""
    p = []
    bar2(p, [0, 0, 7], [1.5, 16, 9], BOARD_DK)
    plate(p, [1.5, 0, 7.45], [16, 16, 7.55], f'f{stage}', {'north': (1.5, 0), 'south': (0, 0)})
    if stage < 2:
        y = 4 if stage == 0 else 10
        bar2(p, [1.5, y, 7.55], [16, y + 1.5, 8.6], BOARD_DK)
    return p


def digit_model(k):
    """One place of the clock: a lit face on the wall behind (south)."""
    p = []
    plate(p, [2, 1, 14], [14, 15, 16], f'd{k}', {'north': (2, 1)}, glow=True)
    bar(p, [1.5, .5, 14.5], [14.5, 15.5, 16], BLACK)
    return p


FITTINGS = {
    'chainlink_0': (lambda: chainlink(0), 'mesh'), 'chainlink_1': (lambda: chainlink(1), 'mesh'),
    'chainlink_2': (lambda: chainlink(2), 'mesh'), 'chainlink_3': (lambda: chainlink(3), 'mesh'),
    'bleacher_0': (bleacher, 'town'),
    'pumpkin_0': (lambda: pumpkin(0), 'pumpkin_0'), 'pumpkin_1': (lambda: pumpkin(1), 'pumpkin_1'),
    'pumpkin_2': (lambda: pumpkin(2), 'pumpkin_2'), 'pumpkin_3': (lambda: pumpkin(3), 'pumpkin_3'),
    'awning_0': (lambda: awning_model(0), 'awning_0'), 'awning_1': (lambda: awning_model(1), 'awning_1'), 'awning_2': (lambda: awning_model(2), 'awning_2'),
    'hydrant_0': (hydrant, 'town'), 'mailbox_0': (mailbox, 'town'), 'pole_0': (pole_model, 'pole'), 'tp_0': (tp_model, 'streamers'), 'cart_0': (cart, 'wire'),
    **{f'dumpster_{k}': ((lambda k=k: dumpster(k)), 'town2') for k in range(4)},
    **{f'bags_{k}': ((lambda k=k: bags(k)), 'town2') for k in range(4)},
    **{f'crate_{k}': ((lambda k=k: crate(k)), 'crate') for k in range(4)},
    **{f'boxes_{k}': ((lambda k=k: boxes(k)), 'cardboard') for k in range(4)},
    **{f'drum_{k}': ((lambda k=k: drum(k)), 'town2') for k in range(4)},
    **{f'tyres_{k}': ((lambda k=k: tyres(k)), 'town2') for k in range(4)},
    **{f'trash_{k}': ((lambda k=k: trash_model(k)), f'trash_{k}') for k in range(4)},
    **{f'pipe_{k}': ((lambda k=k: pipe(k)), 'town2') for k in range(3)},
    **{f'unit_{k}': ((lambda k=k: unit(k)), 'town') for k in range(4)},
    **{f'boards_{k}': ((lambda k=k: boards_model(k)), f'boards_{k}') for k in range(4)},
}
NAMES_OF = {'k': 'town', 'm': 'mesh', 't': 'mesh_torn', 'u': 'mesh_torn_top', 'b': 'pole', 's': 'streamers', 'w': 'wire',
            'j': 'town2', 'x': 'crate', 'y': 'milk_crate', 'cb': 'cardboard', 'cf': 'cardboard_flat', 'g': 'grille',
            **{f'r{k}': f'trash_{k}' for k in range(4)}, **{f'f{k}': f'boards_{k}' for k in range(4)},
            **{f'p{k}': f'pumpkin_{k}' for k in range(4)}, **{f'c{k}': f'pumpkin_cut_{k}' for k in range(4)}, **{f'a{k}': f'awning_{k}' for k in range(3)},
            **{f'd{k}': f'digit_{k}' for k in range(12)}}
KINDS = ['chainlink', 'bleacher', 'pumpkin', 'awning', 'hydrant', 'mailbox', 'pole', 'tp', 'cart',
         'dumpster', 'bags', 'crate', 'boxes', 'drum', 'tyres', 'trash', 'pipe', 'unit', 'boards']
STAGES = {'chainlink': 4, 'pumpkin': 4, 'awning': 3, 'dumpster': 4, 'bags': 4, 'crate': 4, 'boxes': 4, 'drum': 4, 'tyres': 4, 'trash': 4,
          'pipe': 3, 'unit': 4, 'boards': 4}
MAX_STAGE = 4


def model(parts, particle):
    keys = {f['texture'][1:] for e in parts for f in e['faces'].values()}
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout',
            'textures': {**{k: P + NAMES_OF[k] for k in sorted(keys)}, 'particle': P + particle}, 'elements': parts}


def fitting_models():
    out = {key: model(build(), particle) for key, (build, particle) in FITTINGS.items()}
    for k in range(12):
        out[f'digit_{k}'] = model(digit_model(k), f'digit_{k}')
    return out


def files():
    out = {}
    for name in materials():
        out[f'models/block/calder_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/calder_{name}'}}
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
    out['blockstates/calder_town.json'] = {'variants': variants}
    out['blockstates/calder_digit.json'] = {'variants': {f'facing={facing},value={k}': {'model': f'{P}digit_{k}', **({'y': y} if y else {})}
                                                         for facing, y in FACING_Y.items() for k in range(12)}}
    # The barber's pole turns: eight frames, three ticks each.
    out['textures/' + DIR + 'pole.png.mcmeta'] = {'animation': {'frametime': 3}}
    return out


NAMES = {'block.the_oldest_house.calder_town': 'Town Fitting', 'block.the_oldest_house.calder_digit': 'Clock Digit',
         'block.the_oldest_house.calder_stucco': 'Stucco', 'block.the_oldest_house.calder_tanbrick': 'Tan Brick',
         'block.the_oldest_house.calder_siding': 'Clapboard', 'block.the_oldest_house.calder_concrete': 'Concrete',
         'block.the_oldest_house.calder_roofing': 'Tarred Roof', 'block.the_oldest_house.calder_shingle': 'Shingles',
         'block.the_oldest_house.calder_lot': 'Lot Asphalt', 'block.the_oldest_house.calder_lot_line': 'Bay Line',
         'block.the_oldest_house.calder_plank': 'Porch Boards', 'block.the_oldest_house.calder_gravel': 'Alley Gravel',
         'block.the_oldest_house.calder_turf': 'Field Turf', 'block.the_oldest_house.calder_turf_track': 'Tracked Turf',
         'block.the_oldest_house.calder_turf_line': 'Chalked Turf', 'block.the_oldest_house.calder_hedge': 'Hedge'}


def review(path):
    from draw_bike_night_art import seen
    images = textures(); scale = 14; cells = []
    for key, (build, _) in FITTINGS.items():
        for view in ('side', 'front', 'top'):
            cells.append(seen(build(), {k: 'calder_town/' + v for k, v in NAMES_OF.items()}, images, view, scale, (-4, 20, -1, 18), 'block/'))
    w, h = cells[0].size
    cols = 9
    rows = (len(cells) + cols - 1) // cols
    page = Image.new('RGBA', (cols * (w + 4) + 4, rows * (h + 4) + 4 + 72), (30, 32, 36, 255))
    for i, one in enumerate(cells):
        page.alpha_composite(one, (4 + (i % cols) * (w + 4), 4 + (i // cols) * (h + 4)))
    x = 4
    for rel, im in sorted(images.items()):
        if im.size == (16, 16) and x + 68 < page.width:
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
    print(('Verified' if check else 'Drew') + f" Calder Falls: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
