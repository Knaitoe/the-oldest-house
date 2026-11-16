"""A sort of thing, part 1 (0.4.641): the drawing-room behind the glass, and the ruled country.

Draws, in native Minecraft pixels from fixed seeds: the country's materials (light and dark turf for the squares, the soil under
them, box hedge, the brook, the footbridge's planks, the road's gravel, the trees' bark and boughs), each twice: filled, and ruled.
The ruled twin is derived from the filled picture by ruled() alone (the same block as its own outline and a little hatching on
paper), so the two cannot drift apart. Then the drawing-room's own materials (wallpaper, panelling, floorboards, ceiling, the
chimney-breast's stone, the hearth) and every fitting of the king_fitting block, facing north as drawn and turned by its facing:
the chimney-piece in three parts (its jambs and the fire between them), the looking-glass over it in six, the hearthrug, the
chessmen on it, the round table, the chess problem lying on it (printed backwards), the wing armchair and the tall window's two
halves. Writes the models, the block states and the blocks' names. --check verifies everything, and fails if the game's lists of
materials and kinds are not its own.
"""
from pathlib import Path
import argparse
import json
import math
import random
import re
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth'
NS = 'the_oldest_house:'
P = NS + 'block/king/'
PAPER = (234, 228, 210)
INK = (56, 60, 78)


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4, h=16, w=16):
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(h):
            for x in range(w):
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


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


def lum(p):
    return (p[0] * 3 + p[1] * 6 + p[2]) / 10


# ------------------------------------------------------------------------------------------------ the ruled twin
def ruled(im, seed):
    """The block drawn as a line drawing on paper. The filled picture's tones are smoothed and sorted into three bands by its own range of
    tone; an ink line runs only where one band meets another, so a pebble or a leaf keeps its outline but the grain of grass does not. A dark block is hatched
    lightly across, its darkest parts crossed again. Made from the filled picture alone, pixel for pixel, so a change to one is a change
    to both."""
    w, h = im.size; src = im.load(); rnd = random.Random(seed)
    def smooth(x, y):
        vals = [lum(src[(x + dx) % w, (y + dy) % h]) for dx in (-1, 0, 1) for dy in (-1, 0, 1) if src[(x + dx) % w, (y + dy) % h][3]]
        return sum(vals) / len(vals) if vals else 255
    tone = [[smooth(x, y) for x in range(w)] for y in range(h)]
    flat = [t for row in tone for t in row]; mid = sum(flat) / len(flat)
    spread = max(6.0, (sum((t - mid) ** 2 for t in flat) / len(flat)) ** .5)
    # Bands from the block's own range of tone, so a plank's seam or a ripple is outlined however dark or light the block is.
    band = [[0 if t < mid - .8 * spread else 2 if t > mid + .8 * spread else 1 for t in row] for row in tone]
    opaque = [src[x, y] for y in range(h) for x in range(w) if src[x, y][3]]
    mean = sum(lum(p) for p in opaque) / max(1, len(opaque))
    out = Image.new('RGBA', (w, h)); px = out.load()
    for y in range(h):
        for x in range(w):
            if src[x, y][3] == 0:
                px[x, y] = (0, 0, 0, 0); continue
            b = band[y][x]
            # One line to a shape: the darker side of its upper and left edges, like a pen drawing the shape's near outline.
            edge = any(src[(x + dx) % w, (y + dy) % h][3] and band[(y + dy) % h][(x + dx) % w] > b for dx, dy in ((-1, 0), (0, -1)))
            if edge:
                c = INK
            elif mean < 120 and (x + y) % 4 == 0:
                c = shade(PAPER, -70)
            elif b == 0 and mean < 150 and (x - y) % 4 == 0:
                c = shade(PAPER, -95)
            else:
                c = shade(PAPER, rnd.randint(-3, 3))
            px[x, y] = (*c[:3], 255)
    return out


# ------------------------------------------------------------------------------------------------ the country
def turf(base, seed):
    # Close-cropped grass: a mown pile with the odd clover and a darker blade; never blotchy.
    im = new(base=base, seed=seed, spread=5); rnd = random.Random(seed + 1)
    for _ in range(22):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, shade(base, rnd.choice((-14, 10))))
    for _ in range(3):
        x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
        dot(im, x, y, shade(base, 18)); dot(im, x + 1, y, shade(base, 12))
    return im


def soil():
    im = new(base='#6a5038', seed=6411, spread=6); rnd = random.Random(6412)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(('#7c6248', '#584230', '#8a8070')))
    return im


def hedge():
    # Clipped box: small leaves in clusters, darker hollows between, a flat-topped trim.
    im = new(base='#3e5a2e', seed=6421, spread=4); rnd = random.Random(6422)
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(('#4e6c38', '#5a7a40', '#2e4422')))
    for _ in range(8):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#24361a'); dot(im, x, y + 1, '#24361a')
    return im


def brook():
    # Clear running water over pebbles: a cold ground, three lines of current, the pebbles through it.
    im = new(base='#4a7088', seed=6431, spread=3); rnd = random.Random(6432)
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#6e8a8a'); dot(im, x + 1, y, '#5e7a7e')
    for row in (3, 9, 13):
        for x in range(rnd.randrange(3), 16, 1):
            if (x + row) % 5 < 3:
                dot(im, x, row, '#8cb0c4')
    return im


def bridge():
    im = new(base='#8a6a46', seed=6441, spread=4)
    for y in range(16):
        if y % 4 == 3:
            fill(im, 0, y, 16, y + 1, '#5a4228')
    for y in (1, 9):
        dot(im, 2, y, '#3a3028'); dot(im, 13, y, '#3a3028')
    return im


def path():
    im = new(base='#a49a82', seed=6451, spread=6); rnd = random.Random(6452)
    for _ in range(24):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(('#8a8068', '#bcb298', '#7a7058')))
    return im


def bark():
    # Old oak: deep vertical fissures, ridges catching the light.
    im = new(base='#5a4632', seed=6461, spread=4); rnd = random.Random(6462)
    for x in range(0, 16, 3):
        off = rnd.randrange(3)
        for y in range(16):
            if (y + off) % 7 != 0:
                dot(im, x + (1 if (y // 5) % 2 else 0), y, '#3a2c1e')
        for y in range(rnd.randrange(4), 16, 6):
            dot(im, x + 2, y, '#74604a')
    return im


def bough():
    # The tree's leaves, larger than the hedge's and looser, with gaps of shade.
    im = new(base='#46642e', seed=6471, spread=5); rnd = random.Random(6472)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16)
        fill(im, x, y, x + 2, y + 2, rnd.choice(('#587a38', '#3a5626', '#62843e')))
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#22341a')
    return im


COUNTRY = {
    'turf_light': lambda: turf('#7a9a4e', 6401), 'turf_dark': lambda: turf('#5e7e3c', 6403), 'soil': soil, 'hedge': hedge,
    'brook': brook, 'bridge': bridge, 'path': path, 'bark': bark, 'bough': bough,
}


# ------------------------------------------------------------------------------------------------ the drawing-room
def wallpaper():
    # A small trellis of leaves on a warm cream ground, the kind of paper a Victorian back drawing-room would have.
    im = new(base='#d8c8a0', seed=6501, spread=3)
    for i in range(16):
        dot(im, i, i, '#b8a678'); dot(im, 15 - i, i, '#b8a678')
    for x, y in ((7, 3), (3, 8), (11, 8), (7, 12)):
        dot(im, x, y, '#7a8a5a'); dot(im, x + 1, y, '#6a7a4a'); dot(im, x, y + 1, '#8a6a4a')
    return im


def panel():
    im = new(base='#5e3e28', seed=6511, spread=3)
    frame(im, 1, 1, 15, 15, '#4a2e1c'); frame(im, 2, 2, 14, 14, '#76543a')
    return im


def floorboards():
    im = new(base='#7a5434', seed=6521, spread=4)
    for y in range(16):
        if y % 4 == 3:
            fill(im, 0, y, 16, y + 1, '#4a301c')
    for y, x in ((1, 5), (5, 12), (9, 3), (13, 9)):
        dot(im, x, y, '#4a301c')
    return im


def ceiling():
    im = new(base='#e4dccb', seed=6531, spread=2)
    frame(im, 0, 0, 16, 16, '#cfc5b0')
    return im


def chimney():
    # The chimney-breast's dressed stone: ashlar courses, fine joints.
    im = new(base='#b0a690', seed=6541, spread=4)
    for y in range(16):
        course = y // 8
        for x in range(16):
            if y % 8 == 7 or (x + (4 if course else 0)) % 8 == 7:
                dot(im, x, y, '#8a8070')
    return im


def hearth():
    im = new(base='#4a3e36', seed=6551, spread=3)
    frame(im, 0, 0, 16, 16, '#2e2622')
    return im


ROOM = {'wallpaper': wallpaper, 'panel': panel, 'floorboards': floorboards, 'ceiling': ceiling, 'chimney': chimney, 'hearth': hearth}


def materials():
    out = {}
    for k, f in COUNTRY.items():
        out[k] = f()
    for i, (k, f) in enumerate(COUNTRY.items()):
        out[k + '_ruled'] = ruled(out[k], 6600 + i)
    for k, f in ROOM.items():
        out[k] = f()
    return out


# ------------------------------------------------------------------------------------------------ fitting textures
def marble():
    im = new(base='#e2dcd0', seed=6701, spread=3); rnd = random.Random(6702)
    x = rnd.randrange(16)
    for y in range(16):
        x = max(0, min(15, x + rnd.choice((-1, 0, 1)))); dot(im, x, y, '#b8aea0')
    return im


def gilt():
    im = new(base='#b8923e', seed=6711, spread=5)
    for y in range(0, 16, 2):
        for x in range(16):
            if (x + y) % 4 == 0:
                dot(im, x, y, '#e2c06a')
    return im


def glass():
    # The looking-glass: the room's light in it, a pale sheen running down at a slant, and nothing that can be made out.
    im = new(base='#8c9aa0', seed=6721, spread=2)
    for i in range(16):
        for d in (0, 1):
            dot(im, (i + d + 4) % 16, i, '#c4d0d4')
        dot(im, (i + 11) % 16, i, '#a8b6ba')
    return im


def grate():
    im = new(base='#2a2a2c', seed=6731, spread=2)
    for x in range(1, 16, 3):
        fill(im, x, 0, x + 1, 16, '#4a4a4e')
    return im


def fire():
    # Coals and the low flames over them, drawn as four frames for the animation.
    frames = new(h=64); rnd = random.Random(6741)
    for f in range(4):
        y0 = f * 16
        fill(frames, 0, y0 + 11, 16, y0 + 16, '#3a1e14')
        for x in range(16):
            dot(frames, x, y0 + 11 + rnd.randrange(3), rnd.choice(('#e06a2a', '#b8401e', '#f0a03a')))
            top = 4 + rnd.randrange(5)
            for y in range(top, 11):
                c = '#f8d070' if y < top + 2 else '#f08a2e' if y < 8 else '#d4502a'
                if (x + f) % 3 or y > 7:
                    dot(frames, x, y0 + y, c, 230)
    return frames


def rug():
    # The hearthrug: a deep border round a field of small medallions.
    im = new(base='#7a2e2a', seed=6751, spread=3)
    frame(im, 0, 0, 16, 16, '#3a2a3e'); frame(im, 1, 1, 15, 15, '#c8a860')
    for x, y in ((5, 5), (10, 5), (5, 10), (10, 10)):
        dot(im, x, y, '#c8a860'); dot(im, x + 1, y + 1, '#c8a860'); dot(im, x, y + 1, '#3a2a3e'); dot(im, x + 1, y, '#3a2a3e')
    return im


def rattle():
    # 0.4.643: the brothers' rattle, a nursery rattle grown large: its head painted in white and red bands, worn to the wood where it
    # has been knocked about, a ring of brass bells round its middle.
    im = new(base='#ece4d2', seed=6781, spread=3)
    for y in (2, 3, 12, 13):
        fill(im, 0, y, 16, y + 1, '#b0322a')
    fill(im, 0, 7, 16, 9, '#c9a84a')
    for x in range(1, 16, 3):
        dot(im, x, 7, '#e8cc6a'); dot(im, x, 8, '#7a6230')
    for x, y in ((3, 5), (11, 4), (6, 11), (13, 10)):
        dot(im, x, y, '#b89a6a')
    return im


def turned():
    # Its handle: pale turned wood with a ring cut into it every few pixels, polished dark where it has been held.
    im = new(base='#c8995a', seed=6782, spread=3)
    for x in (3, 8, 13):
        fill(im, x, 0, x + 1, 16, '#a2753e')
    fill(im, 5, 0, 11, 16, '#b0844a')
    return im


def peg():
    # 0.4.644: the Queen's peg, a pale whittled stake with a band of paint near its head, the paint chipped where it was driven in.
    im = new(base='#d6c09a', seed=6821, spread=3)
    for x in (4, 11):
        fill(im, x, 0, x + 1, 16, '#b89c70')
    fill(im, 0, 3, 16, 5, '#a8302b')
    for x in (2, 9, 14):
        dot(im, x, 4, '#d6c09a')
    return im


def tabletop():
    # The little glass table: a clear top with a pale edge and the light lying along it.
    im = new(base='#d4e4ea', seed=6831, spread=2)
    frame(im, 0, 0, 16, 16, '#eef6f8')
    for i in range(3, 13):
        dot(im, i, 16 - i, '#f6fbfc')
    return im


def bottle():
    # A small stoppered bottle of thin glass, a paper label round its middle, the stopper cork.
    im = new(base='#8eb8a8', seed=6841, spread=3)
    fill(im, 0, 0, 16, 3, '#b08a5a')
    fill(im, 0, 7, 16, 12, '#ece4cc'); fill(im, 0, 7, 16, 8, '#c8bea4')
    for x in (3, 5, 6, 9, 11, 12):
        dot(im, x, 9, '#3a342e')
    for x in (2, 7):
        fill(im, x, 3, x + 1, 7, '#c4e0d4')
    return im


def cake():
    # A very small cake: a sponge with a darker crust, currants pressed into its top.
    im = new(base='#d9a96a', seed=6851, spread=3)
    fill(im, 0, 0, 16, 2, '#a8743e'); fill(im, 0, 14, 16, 16, '#a8743e')
    for x, y in ((3, 5), (8, 4), (12, 7), (5, 10), (10, 11), (13, 3)):
        dot(im, x, y, '#3e2420'); dot(im, x + 1, y, '#5a3428')
    return im


def leather():
    # 0.4.644: the carriage's seats, buttoned leather gone shiny where people have sat.
    im = new(base='#5a3a2e', seed=6861, spread=3)
    for x in range(2, 16, 4):
        for y in range(2, 16, 4):
            dot(im, x, y, '#3a241c'); dot(im, x + 1, y + 1, '#6e4a3a')
    return im


def wheel():
    # A carriage wheel seen side on: an iron tyre, wooden felloes and spokes, a hub.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6.6 <= d <= 8:
                dot(im, x, y, '#3a3a3e')
            elif 5.4 <= d < 6.6:
                dot(im, x, y, '#6a4a2e')
            elif d < 1.8:
                dot(im, x, y, '#4a4a4e')
    for i in range(-5, 6):
        dot(im, 7 + (i > 0), 7 + i, '#7a5636'); dot(im, 7 + i, 7 + (i > 0), '#7a5636')
        dot(im, 7 + i, 7 + i, '#7a5636'); dot(im, 7 + i, 8 - i, '#7a5636')
    return im


def stalk():
    # 0.4.645: the mushroom's stalk, pale and fibrous, its streaks running up it, a ring of skirt a third of the way down.
    im = new(base='#e6dcc4', seed=6871, spread=3)
    for x in (2, 6, 9, 13):
        fill(im, x, 0, x + 1, 16, '#d2c6aa')
    fill(im, 0, 4, 16, 6, '#d8ccb0'); fill(im, 0, 6, 16, 7, '#c4b694')
    return im


def cap():
    # The mushroom's cap from above and at its edge: a broad brown dome gone flat, paler blotches on it, a darker rim.
    im = new(base='#9a6a42', seed=6881, spread=4)
    for x, y in ((3, 3), (10, 2), (6, 8), (12, 10), (2, 12), (8, 13)):
        fill(im, x, y, x + 2, y + 2, '#b8885a'); dot(im, x, y, '#c89c6c')
    frame(im, 0, 0, 16, 16, '#7a5232')
    return im


def gills():
    # Under the cap: the gills, fine lines running out from the stalk to the rim.
    im = new(base='#d8c8a8', seed=6891, spread=2)
    for i in range(16):
        dot(im, i, i, '#b8a684'); dot(im, 15 - i, i, '#b8a684')
        dot(im, 7, i, '#b8a684'); dot(im, i, 7, '#b8a684')
    return im


def ivory():
    return new(base='#e6dcc4', seed=6761, spread=3)


def redwood():
    return new(base='#9a2e22', seed=6762, spread=3)


def mahogany():
    im = new(base='#6a3a22', seed=6771, spread=3)
    for y in (3, 9, 14):
        fill(im, 0, y, 16, y + 1, '#5a2e1a')
    return im


def problem():
    # The chess problem, printed backwards: a little board of eight by eight, its last move left blank, a line of type under it
    # that reads right only in a glass.
    im = new(base='#ece4cc', seed=6781, spread=2)
    for r in range(8):
        for c in range(8):
            if (r + c) % 2:
                dot(im, 4 + c, 2 + r, '#5a5248')
    frame(im, 3, 1, 13, 11, '#3a342e')
    for x in (12, 10, 9, 7, 5, 4):
        dot(im, x, 13, '#3a342e')
    for x in (11, 8, 6):
        dot(im, x, 14, '#3a342e')
    return im


def velvet():
    im = new(base='#5a3a4e', seed=6791, spread=4)
    for y in range(0, 16, 4):
        for x in range(16):
            if (x + y // 4) % 4 == 0:
                dot(im, x, y, '#6e4a60')
    return im


def sash():
    im = new(base='#e8e0cc', seed=6801, spread=2)
    return im


def pane():
    # Through the window: the country as a pale wash, a strip of hedge and the sky over it.
    im = new(base='#b8ccd8', seed=6811, spread=2)
    fill(im, 0, 11, 16, 16, '#9ab07a'); fill(im, 0, 10, 16, 11, '#5e7e3c')
    for x in range(0, 16, 4):
        dot(im, x + 1, 9, '#5e7e3c')
    return im


# ------------------------------------------------------------------------------------------------ 0.4.646: the court's pack
# A card is eleven texels wide and twenty-four tall, one to one with its model, drawn whole and cut into the lower sixteen rows and the
# upper eight (a block texture is square). Pale face, a darker edge, the corners cut round; the index at two corners; the pips set as on
# a real card; the court cards a figure from the waist, crowned, drawn again upside down below the middle.
CARD_W, CARD_H = 11, 24
# ------------------------------------------------------------------------------------------------ the feast (0.4.648)
def cloth(hem=True):
    # The tablecloth: white damask, a small diamond woven into it that only shows as the light catches it, and a hemmed edge. The
    # weave is kept within a few tones of the cloth, so that drawn (its ruled twin) the cloth is clean paper with one line for its hem.
    im = new(base='#ece6d6', seed=6901, spread=1)
    for y in range(16):
        for x in range(16):
            if (abs((x % 8) - 4) + abs((y % 8) - 4)) == 3:
                dot(im, x, y, '#e5dfce')
    if hem:
        fill(im, 0, 14, 16, 15, '#c8c0aa')
    return im


def linen():
    # The same cloth where it lies on the board: no hem across the middle of the table.
    return cloth(hem=False)


def roast():
    # The leg of mutton: a browned crust scored across in two long cuts, the fat gone gold where it caught along the upper edge of
    # each. The cuts run the whole way across, so that drawn they are two clean lines and not a scatter of marks.
    im = new(base='#8a4e2a', seed=6902, spread=3)
    for y in (5, 11):
        fill(im, 0, y, 16, y + 1, '#643218'); fill(im, 0, y - 1, 16, y, '#a8683a')
    for x, y in ((3, 2), (10, 8), (6, 14)):
        fill(im, x, y, x + 3, y + 1, '#96582f')
    return im


def plate():
    # A pewter dish: a dull grey with its rim a shade lighter and a ring where the well begins.
    im = new(base='#a8a8a0', seed=6903, spread=2)
    frame(im, 0, 0, 16, 16, '#c4c4bc'); frame(im, 2, 2, 14, 14, '#909088')
    return im


def pudding():
    # The pudding's sides: dark and close, currants and peel in it.
    im = new(base='#4a2a22', seed=6904, spread=4)
    for x, y in ((2, 3), (7, 2), (12, 4), (4, 8), (10, 9), (14, 12), (1, 13), (7, 13)):
        dot(im, x, y, '#2a1610'); dot(im, x + 1, y, '#6a3a26')
    # The sauce run down from the top, further in some places than others.
    for x in range(16):
        fill(im, x, 0, x + 1, 1 + (x * 7 % 4), '#efe8d8')
    return im


def sauce():
    # Its top: white sauce poured over, a little pooled.
    im = new(base='#efe8d8', seed=6905, spread=2)
    for x, y in ((4, 5), (10, 9), (7, 12)):
        dot(im, x, y, '#ded4be')
    return im


def holly():
    # A sprig of holly stuck in the top: two dark leaves and a few berries.
    im = new(base='#2e5a2a', seed=6906, spread=3)
    for x, y in ((6, 6), (9, 7), (7, 9)):
        fill(im, x, y, x + 2, y + 2, '#b0221c')
    return im


def candle():
    # A tallow candle: off-white, a drip run down one side.
    im = new(base='#efe6cc', seed=6907, spread=2)
    fill(im, 11, 0, 13, 7, '#e2d6b6'); dot(im, 12, 7, '#e2d6b6')
    return im


def flame():
    # The flame: a small upright tongue, white at its root, yellow, its tip orange, on nothing.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        half = [0, 0, 1, 1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 3, 3, 2][y]
        for x in range(8 - half, 8 + half):
            dot(im, x, y, '#f8b040' if y < 5 else '#fce88a' if y < 12 else '#fffbe8')
    return im


def brass():
    # The candlestick's brass: smooth, a bright line down it where the light catches and a darker one where it turns away.
    im = new(base='#c09238', seed=6908, spread=2)
    fill(im, 4, 0, 6, 16, '#e0b85a'); fill(im, 11, 0, 12, 16, '#9a7228')
    return im


FEAST = {'cloth': cloth, 'linen': linen, 'brass': brass, 'roast': roast, 'plate': plate, 'pudding': pudding, 'sauce': sauce, 'holly': holly, 'candle': candle,
         'flame': flame}
# What the drawn feast (stage 0) is drawn with: each of its materials' ruled twin, made from the filled picture by the same rule as the
# country's blocks.
FEAST_RULED = ['cloth', 'roast', 'plate', 'pudding', 'sauce', 'holly', 'candle', 'flame', 'mahogany', 'velvet', 'brass', 'ivory', 'linen']


def crown_gold():
    # 0.4.648: the crown. Gold, worked: a raised line along its band, a bright edge where it catches the light.
    im = new(base='#d8a830', seed=6911, spread=3)
    fill(im, 0, 0, 16, 1, '#f4d470'); fill(im, 0, 7, 16, 8, '#b08418')
    for x in range(0, 16, 3):
        dot(im, x, 4, '#f0c850')
    return im


def crown_jewel():
    # Its stones: a deep red, a pale point of light in each.
    im = new(base='#9a1418', seed=6912, spread=4)
    for x, y in ((3, 3), (11, 5), (6, 11), (13, 13)):
        dot(im, x, y, '#f2a0a0')
    return im


def crown_pencil():
    # 0.4.649: the crown drawn: pencil, a soft dark line that is a little lighter where the lead skipped.
    im = new(base='#3a3632', seed=6913, spread=5)
    for x, y in ((2, 5), (9, 2), (13, 10), (5, 13)):
        dot(im, x, y, '#5a554e')
    return im


def nightcap():
    # 0.4.649: the King's night-cap, off his head: soft wool lying in folds down its length (the cloth of his cap as he is painted,
    # tools/paint_character_skins.py).
    im = new(base='#a8302b', seed=6914, spread=3)
    for x in (2, 6, 10, 14):
        for y in range(16):
            if (x + y) % 5:
                dot(im, x - (1 if y > 9 else 0), y, '#8e2622')
    for x, y in ((4, 3), (8, 9), (12, 5), (1, 12)):
        dot(im, x, y, '#b83a34')
    return im


def nightcap_band():
    # Its turned-up band: a brighter roll with a light upper edge and a shadow under it.
    im = new(base='#c23d36', seed=6915, spread=3)
    fill(im, 0, 0, 16, 1, '#d2524a'); fill(im, 0, 15, 16, 16, '#8c2823')
    for x in range(1, 16, 4):
        dot(im, x, 7, '#b0342e')
    return im


def tassel():
    # The tassel at its tip: pale wool, strands hanging.
    im = new(base='#e4dfd2', seed=6916, spread=3)
    for x in (1, 4, 7, 10, 13):
        fill(im, x, 3, x + 1, 16, '#c6c0b0')
    fill(im, 0, 0, 16, 3, '#d2ccbc')
    return im


CARD_FACES = ['two', 'three', 'four', 'five', 'six', 'eight', 'knave', 'queen', 'king']
CARD_PAPER, CARD_EDGE = '#efe8d8', '#8a8070'
HEART, SPADE, GOLD = '#b8323a', '#2a2a30', '#d8b04a'
HEART_PIP = [(0, 0), (2, 0), (0, 1), (1, 1), (2, 1), (1, 2)]
SPADE_PIP = [(1, 0), (0, 1), (1, 1), (2, 1), (0, 2), (2, 2)]
PIPS = {2: [(4, 2), (4, 17)], 3: [(4, 2), (4, 10), (4, 17)], 4: [(2, 2), (6, 2), (2, 17), (6, 17)],
        5: [(2, 2), (6, 2), (4, 10), (2, 17), (6, 17)], 6: [(2, 2), (6, 2), (2, 10), (6, 10), (2, 17), (6, 17)],
        8: [(2, 2), (6, 2), (2, 7), (6, 7), (2, 12), (6, 12), (2, 17), (6, 17)]}


def card_blank():
    im = Image.new('RGBA', (CARD_W, CARD_H), (0, 0, 0, 0))
    fill(im, 0, 0, CARD_W, CARD_H, CARD_PAPER)
    frame(im, 0, 0, CARD_W, CARD_H, CARD_EDGE)
    for x, y in ((0, 0), (CARD_W - 1, 0), (0, CARD_H - 1), (CARD_W - 1, CARD_H - 1)):
        im.putpixel((x, y), (0, 0, 0, 0))
    return im


def pip(im, x, y, suit, flip=False):
    shape = HEART_PIP if suit == HEART else SPADE_PIP
    for px, py in shape:
        dot(im, x + px, y + (2 - py if flip else py), suit)
    if suit == SPADE:
        dot(im, x + 1, y - 1 if flip else y + 3, suit)


def card_face(name):
    im = card_blank()
    i = CARD_FACES.index(name)
    suit = HEART if i % 2 == 0 or i >= 6 else SPADE
    if i < 6:
        count = [2, 3, 4, 5, 6, 8][i]
        for x, y in PIPS[count]:
            pip(im, x, y, suit, flip=y > 12)
        # The index: a pip in the top left corner and its turned twin in the bottom right.
        dot(im, 1, 1, suit); dot(im, CARD_W - 2, CARD_H - 2, suit)
    else:
        # The court: a figure from the waist, crowned, in the suit's colour, mirrored about the middle as on a real card.
        robe = HEART if name != 'knave' else '#3e5a8a'
        half = Image.new('RGBA', (CARD_W - 2, 11), (0, 0, 0, 0))
        fill(half, 3, 0, 6, 1, GOLD) if name != 'knave' else fill(half, 3, 0, 6, 1, '#4a3a2a')
        if name != 'knave':
            for x in (3, 5):
                dot(half, x, 0, GOLD)
        fill(half, 3, 1, 6, 4, '#e8c4a2')
        dot(half, 3, 2, '#2a201c'); dot(half, 5, 2, '#2a201c')
        if name == 'king':
            fill(half, 3, 3, 6, 5, '#d8d4c8')
        if name == 'queen':
            fill(half, 2, 1, 3, 5, '#2a201c'); fill(half, 6, 1, 7, 5, '#2a201c')
        fill(half, 1, 5, 8, 11, robe)
        fill(half, 4, 5, 5, 11, GOLD)
        dot(half, 2, 7, CARD_PAPER); dot(half, 6, 8, CARD_PAPER)
        im.alpha_composite(half, (1, 1))
        im.alpha_composite(half.transpose(Image.Transpose.ROTATE_180), (1, CARD_H - 12))
        for x in range(1, CARD_W - 1):
            dot(im, x, 12, CARD_EDGE)
        dot(im, 1, 1, HEART)
    return im


def card_back():
    im = card_blank()
    for y in range(2, CARD_H - 2):
        for x in range(2, CARD_W - 2):
            if (x + y) % 3 == 0 or (x - y) % 3 == 0:
                dot(im, x, y, '#3e5a8a')
            else:
                dot(im, x, y, '#d4ddea')
    frame(im, 1, 1, CARD_W - 1, CARD_H - 1, '#3e5a8a')
    return im


def card_halves(card):
    """The card cut into a lower and an upper texture: rows 8 to 23 at x 2 to 12 of the first, rows 0 to 7 at y 8 to 15 of the second."""
    lo = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); hi = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    lo.alpha_composite(card.crop((0, 8, CARD_W, CARD_H)), (2, 0))
    hi.alpha_composite(card.crop((0, 0, CARD_W, 8)), (2, 8))
    return lo, hi


def cards():
    out = {}
    for name in CARD_FACES + ['back']:
        card = card_back() if name == 'back' else card_face(name)
        lo, hi = card_halves(card)
        out[f'card_{name}_lo'] = lo; out[f'card_{name}_hi'] = hi
    return out


def textures():
    out = {f'block/king_{k}.png': v for k, v in materials().items()}
    t = {'marble': marble(), 'gilt': gilt(), 'glass': glass(), 'grate': grate(), 'fire': fire(), 'rug': rug(), 'ivory': ivory(),
         'redwood': redwood(), 'mahogany': mahogany(), 'problem': problem(), 'velvet': velvet(), 'sash': sash(), 'pane': pane(), 'rattle': rattle(), 'turned': turned(),
         'peg': peg(), 'tabletop': tabletop(), 'bottle': bottle(), 'cake': cake(), 'leather': leather(), 'wheel': wheel(),
         'stalk': stalk(), 'cap': cap(), 'gills': gills(), **cards(), **{k: f() for k, f in FEAST.items()},
         'nightcap': nightcap(), 'nightcap_band': nightcap_band(), 'tassel': tassel()}
    for i, k in enumerate(FEAST_RULED):
        t[k + '_ruled'] = ruled(t[k], 6950 + i)
    out.update({f'block/king/{k}.png': v for k, v in t.items()})
    # 0.4.644: the bottle and the cake in the hand.
    out.update({'item/king/bottle.png': bottle_item(), 'item/king/cake.png': cake_item()})
    # 0.4.648: the crown's gold and its stones.
    out.update({'item/king/crown_gold.png': crown_gold(), 'item/king/crown_jewel.png': crown_jewel()})
    # 0.4.649: the pencil the crown is drawn in outside the country.
    out['item/king/crown_pencil.png'] = crown_pencil()
    return out


def bottle_item():
    # The bottle as it is held: its outline in thin glass, the cork, the label with its two words too small to read.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 7, 1, 9, 3, '#b08a5a'); fill(im, 7, 3, 9, 5, '#a8ccbe')
    fill(im, 5, 5, 11, 15, '#8eb8a8'); fill(im, 6, 15, 10, 16, '#6e9888')
    fill(im, 5, 8, 11, 12, '#ece4cc')
    for x in (6, 7, 9):
        dot(im, x, 9, '#3a342e')
    for x in (6, 8, 9):
        dot(im, x, 11, '#3a342e')
    fill(im, 6, 5, 7, 8, '#c4e0d4'); dot(im, 4, 7, '#6e9888'); dot(im, 11, 7, '#6e9888')
    return im


def cake_item():
    # The cake on the hand: a little round sponge, its crust, currants on its top, a crumb off it.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 3, 7, 13, 13, '#d9a96a'); fill(im, 4, 6, 12, 7, '#e4b87a'); fill(im, 3, 12, 13, 13, '#a8743e')
    fill(im, 2, 8, 3, 12, '#c0905a'); fill(im, 13, 8, 14, 12, '#c0905a')
    for x, y in ((5, 8), (8, 7), (10, 9), (6, 10)):
        dot(im, x, y, '#3e2420')
    dot(im, 14, 14, '#d9a96a')
    return im


# ------------------------------------------------------------------------------------------------ models
def default_uv(f, lo, hi):
    """The mapping the game gives a face that names none (BlockElement.uvsByFace): the part's own place in the block."""
    if f == 'down':
        return [lo[0], 16 - hi[2], hi[0], 16 - lo[2]]
    if f == 'up':
        return [lo[0], lo[2], hi[0], hi[2]]
    if f == 'north':
        return [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]]
    if f == 'south':
        return [lo[0], 16 - hi[1], hi[0], 16 - lo[1]]
    if f == 'west':
        return [lo[2], 16 - hi[1], hi[2], 16 - lo[1]]
    return [16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]]


def within(a, b):
    """0.4.649: one span of a face's mapping, kept on the texture. A part that reaches past its block (a chair's back, a candle, a
    crown's points) would otherwise take its picture from whatever lies beside its own in the game's sheet. The span is slid back by
    whole textures where that brings it on; a span lying across the texture's edge starts at the edge; one longer than the texture is
    fitted to it."""
    lo, hi = min(a, b), max(a, b)
    if lo >= -1e-6 and hi <= 16 + 1e-6:
        return a, b
    if hi - lo > 16:
        return (0, 16) if a < b else (16, 0)
    shift = -16 * math.floor(lo / 16)
    if hi + shift > 16 + 1e-6:
        shift = -lo
    return a + shift, b + shift


def element(lo, hi, textures, uv=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        else:
            d = default_uv(f, lo, hi)
            (u0, u1), (v0, v1) = within(d[0], d[2]), within(d[1], d[3])
            if [u0, v0, u1, v1] != d:
                faces[f]['uv'] = [round(c, 4) for c in (u0, v0, u1, v1)]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle, render='minecraft:cutout'):
    tex = {k: (v if ':' in v else P + v) for k, v in textures.items()}
    return {'ambientocclusion': False, 'render_type': render, 'textures': {**tex, 'particle': tex.get(particle, P + particle)}, 'elements': elements}


def piece(x, z, tall, t, king=False):
    """One chessman, at native scale: a foot, a body, a head; the king a little taller with a cross."""
    els = [element([x - 1, 1, z - 1], [x + 1, 1.5, z + 1], {'all': t}), element([x - .5, 1.5, z - .5], [x + .5, tall, z + .5], {'all': t}),
           element([x - .75, tall, z - .75], [x + .75, tall + 1, z + .75], {'all': t})]
    if king:
        els.append(element([x - .25, tall + 1, z - .25], [x + .25, tall + 2.5, z + .25], {'all': t}))
    return els


def fitting_models():
    m = {}
    stone = 'minecraft:block/air'
    # The chimney-piece: the left jamb (0), the opening with the grate and its fire (1), the right jamb (2); a marble shelf across all
    # three, overhanging the front.
    for stage in range(3):
        els = [element([0, 14, 0], [16, 16, 14], {'all': 'm'})]
        if stage in (0, 2):
            x0 = 2 if stage == 0 else 0
            els.append(element([x0, 0, 4], [x0 + 14, 14, 16], {'all': 'm'}))
        else:
            els += [element([0, 11, 4], [16, 14, 16], {'all': 'm'}), element([0, 0, 15], [16, 11, 16], {'north': 'g'}),
                    element([2, 0, 9], [14, 4, 14], {'all': 'g', 'up': 'g'}),
                    element([2, 3, 9], [14, 10, 9.01], {'north': 'f'}), element([2, 3, 12], [14, 10, 12.01], {'north': 'f'})]
        m[f'mantel_{stage}'] = model(els, {'m': 'marble', 'g': 'grate', 'f': 'fire'}, 'marble')
    # The looking-glass over it, three across and two high: a gilt frame on its outer edges, the glass behind.
    for stage in range(6):
        col, row = stage % 3, stage // 3
        els = [element([0, 0, 15], [16, 16, 15.5], {'north': 'q'})]
        if col == 0:
            els.append(element([0, 0, 14], [2, 16, 16], {'all': 'r'}))
        if col == 2:
            els.append(element([14, 0, 14], [16, 16, 16], {'all': 'r'}))
        if row == 0:
            els.append(element([0, 0, 14], [16, 2, 16], {'all': 'r'}))
        if row == 1:
            els.append(element([0, 14, 14], [16, 16, 16], {'all': 'r'}))
            if col == 1:
                els.append(element([5, 16, 14.5], [11, 18, 16], {'all': 'r'}))
        m[f'glass_{stage}'] = model(els, {'q': 'glass', 'r': 'gilt'}, 'glass')
    m['rug_0'] = model([element([0, 0, 0], [16, 0.5, 16], {'all': 'u'})], {'u': 'rug'}, 'rug')
    # The chessmen on the rug, as a game left off: a white king fallen over, a red queen standing, pawns about.
    els = [element([0, 0, 0], [16, 0.5, 16], {'all': 'u'})]
    for x, z, t in ((4, 4, 'r'), (6, 5, 'r'), (11, 4, 'w'), (12, 11, 'w'), (5, 12, 'r')):
        els += piece(x, z, 3.5, t)
    els += piece(9, 8, 5, 'r', king=True)
    els.append(element([10, .5, 11], [15, 1.5, 12], {'all': 'w'}, rot={'origin': [10, .5, 11], 'axis': 'y', 'angle': 22.5}))
    m['chessmen_0'] = model(els, {'u': 'rug', 'r': 'redwood', 'w': 'ivory'}, 'rug')
    m['table_0'] = model([element([0, 13, 0], [16, 15, 16], {'all': 'h'}), element([6, 1, 6], [10, 13, 10], {'all': 'h'}),
                          element([3, 0, 7], [13, 1, 9], {'all': 'h'}), element([7, 0, 3], [9, 1, 13], {'all': 'h'})],
                         {'h': 'mahogany'}, 'mahogany')
    m['problem_0'] = model([element([3, 0, 3], [13, 0.25, 13], {'up': 'p', 'down': 'p'}, rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': -22.5})],
                           {'p': 'problem'}, 'problem')
    # Behind the glass the same paper lies the other way round, and so reads the right way.
    m['problem_1'] = model([element([3, 0, 3], [13, 0.25, 13], {'up': 'p', 'down': 'p'}, uv={'up': [16, 0, 0, 16], 'down': [16, 0, 0, 16]},
                                    rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': 22.5})], {'p': 'problem'}, 'problem')
    m['armchair_0'] = model([element([2, 3, 2], [14, 7, 14], {'all': 'v'}), element([2, 7, 12], [14, 20, 15], {'all': 'v'}),
                             element([1, 7, 3], [3, 12, 15], {'all': 'v'}), element([13, 7, 3], [15, 12, 15], {'all': 'v'}),
                             element([0, 12, 11], [3, 19, 15], {'all': 'v'}), element([13, 12, 11], [16, 19, 15], {'all': 'v'})]
                            + [element([x, 0, z], [x + 2, 3, z + 2], {'all': 'h'}) for x in (2, 12) for z in (2, 12)],
                            {'v': 'velvet', 'h': 'mahogany'}, 'velvet')
    # 0.4.643: the rattle lying on its side in the grass: the banded head, a short neck, and the handle with a knob at its end, the
    # whole turned a little off square as if dropped.
    rot = {'origin': [8, 0, 8], 'axis': 'y', 'angle': 22.5}
    m['rattle_0'] = model([element([2, 0, 5], [7, 5, 10], {'all': 'b'}, rot=rot), element([7, 1.5, 6.5], [8, 3.5, 8.5], {'all': 't'}, rot=rot),
                           element([8, 1.75, 6.75], [13.5, 3.25, 8.25], {'all': 't'}, rot=rot),
                           element([13.5, 1.25, 6.25], [15, 3.75, 8.75], {'all': 't'}, rot=rot)],
                          {'b': 'rattle', 't': 'turned'}, 'rattle')
    # 0.4.644: the Queen's peg driven into the turf, its head a little proud of the grass.
    m['peg_0'] = model([element([7, 0, 7], [9, 6, 9], {'all': 'k'}), element([6.5, 6, 6.5], [9.5, 7, 9.5], {'all': 'k'})], {'k': 'peg'}, 'peg')
    # The glass table on its three thin gilt legs, the bottle standing on it and the cake beside it.
    m['glass_table_0'] = model([element([2, 11, 2], [14, 12, 14], {'all': 'g'}),
                                element([7.5, 0, 3], [8.5, 11, 4], {'all': 'r'}), element([3, 0, 11.5], [4, 11, 12.5], {'all': 'r'}),
                                element([12, 0, 11.5], [13, 11, 12.5], {'all': 'r'}),
                                element([4, 12, 5], [7, 18, 8], {'all': 'b'}), element([5, 18, 6], [6, 20, 7], {'all': 'b'}),
                                element([9, 12, 8], [12, 14, 11], {'all': 'c'})],
                               {'g': 'tabletop', 'r': 'gilt', 'b': 'bottle', 'c': 'cake'}, 'tabletop', render='minecraft:translucent')
    # A carriage seat: a deep leather bench with a high buttoned back against the compartment wall.
    m['seat_0'] = model([element([0, 0, 2], [16, 7, 14], {'all': 'l'}), element([0, 7, 12], [16, 22, 16], {'all': 'l'}),
                         element([0, 0, 14], [16, 7, 16], {'all': 'w'})], {'l': 'leather', 'w': 'mahogany'}, 'leather')
    # A carriage wheel, upright under the body, its hub standing out from it.
    m['wheel_0'] = model([element([0, 0, 7], [16, 16, 9], {'north': 'h', 'south': 'h'}), element([6.5, 6.5, 5], [9.5, 9.5, 11], {'all': 'i'})],
                         {'h': 'wheel', 'i': 'ivory'}, 'wheel')
    # 0.4.645: the mushroom: its stalk, and above it the rest of the stalk under a broad cap, flat on top where the Caterpillar sits,
    # the gills underneath. The cap overhangs its block on every side.
    m['stalk_0'] = model([element([5, 0, 5], [11, 16, 11], {'all': 's'})], {'s': 'stalk'}, 'stalk')
    m['cap_0'] = model([element([5, 0, 5], [11, 10, 11], {'all': 's'}),
                        element([-6, 10, -6], [22, 15, 22], {'up': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'g'},
                                uv={f: [0, 0, 16, 16] for f in ('up', 'down')} | {f: [0, 0, 16, 3] for f in ('north', 'south', 'east', 'west')})],
                       {'s': 'stalk', 'c': 'cap', 'g': 'gills'}, 'cap')
    # 0.4.646: the court's pack. Standing, the card across the middle of its block, its face to the north (the block's facing turns it),
    # its back to the south, half a pixel thick: a lower piece sixteen high and an upper eight. Fallen, the same two pieces lying flat,
    # face up, the card's head to the north.
    for name in CARD_FACES:
        t = {'fl': f'card_{name}_lo', 'fh': f'card_{name}_hi', 'bl': 'card_back_lo', 'bh': 'card_back_hi'}
        m[f'card_{name}_standing'] = model([
            element([2.5, 0, 7.75], [13.5, 16, 8.25], {'north': 'fl', 'south': 'bl', 'east': 'bl', 'west': 'bl'},
                    uv={'north': [2, 0, 13, 16], 'south': [2, 0, 13, 16], 'east': [2, 0, 2.5, 16], 'west': [2, 0, 2.5, 16]}),
            element([2.5, 16, 7.75], [13.5, 24, 8.25], {'north': 'fh', 'south': 'bh', 'east': 'bh', 'west': 'bh', 'up': 'bh'},
                    uv={'north': [2, 8, 13, 16], 'south': [2, 8, 13, 16], 'east': [2, 8, 2.5, 16], 'west': [2, 8, 2.5, 16], 'up': [2, 8, 13, 8.5]})],
            t, 'fl')
        m[f'card_{name}_fallen'] = model([
            element([2.5, 0, 4], [13.5, 0.5, 20], {'up': 'fl', 'down': 'bl'}, uv={'up': [2, 0, 13, 16], 'down': [2, 0, 13, 16]}),
            element([2.5, 0, -4], [13.5, 0.5, 4], {'up': 'fh', 'down': 'bh'}, uv={'up': [2, 8, 13, 16], 'down': [2, 8, 13, 16]})],
            t, 'fl')
    for stage in range(2):
        rail = [element([0, 0, 6], [16, 2, 10], {'all': 's'})] if stage == 0 else [element([0, 14, 6], [16, 16, 10], {'all': 's'})]
        els = rail + [element([0, 0, 6.5], [2, 16, 9.5], {'all': 's'}), element([14, 0, 6.5], [16, 16, 9.5], {'all': 's'}),
                      element([7.5, 0, 7], [8.5, 16, 9], {'all': 's'}),
                      element([2, 2 if stage == 0 else 0, 7.5], [14, 16 if stage == 0 else 14, 8.5], {'north': 'p', 'south': 'p'})]
        m[f'window_{stage}'] = model(els, {'s': 'sash', 'p': 'pane'}, 'sash')
    # 0.4.648: the feast, drawn (0: every material its ruled twin) and filled (1). Each piece of the table is its clothed board with the
    # cloth's drape hanging on the side it faces and one leg under it there; a dish piece has its dish on the board.
    for stage in range(2):
        r = '_ruled' if stage == 0 else ''
        tx = {k: k + r for k in FEAST_RULED}
        # The board is under the cloth without its hem; the drape's faces are cut from the cloth's lower rows, so its hem runs along
        # the drape's bottom edge.
        table = [element([0, 12, 0], [16, 14, 16], {'all': 'linen'}),
                 element([0, 6, 0], [16, 12, 1], {'all': 'cloth'},
                         uv={'north': [0, 9, 16, 15], 'south': [0, 9, 16, 15], 'east': [0, 9, 1, 15], 'west': [0, 9, 1, 15],
                             'up': [0, 0, 16, 1], 'down': [0, 0, 16, 1]}),
                 element([7, 0, 1.5], [9, 12, 3.5], {'all': 'mahogany'})]
        laid = ('cloth', 'linen', 'mahogany')
        m[f'feast_table_{stage}'] = model(table, {k: tx[k] for k in laid}, tx['linen'])
        # The mutton on its dish, the knuckle of bone out of one end.
        m[f'mutton_{stage}'] = model(table + [
            element([2.5, 14, 3.5], [14.5, 14.5, 12.5], {'all': 'plate'}),
            # The fat end, rounded over its top; the shank narrowing from it; the knuckle of bone out of the shank's end.
            element([4, 14.5, 5], [9.5, 17.75, 11], {'all': 'roast'}), element([4.75, 17.75, 5.75], [8.75, 18.5, 10.25], {'all': 'roast'}),
            element([9.5, 14.5, 6.25], [12, 16.5, 9.75], {'all': 'roast'}), element([12, 15, 7.25], [14, 16, 8.75], {'all': 'ivory'})],
            {k: tx[k] for k in laid + ('plate', 'roast', 'ivory')}, tx['roast'])
        # The pudding on its dish: a round of it, the sauce poured over its top, a sprig of holly stuck in.
        m[f'pudding_{stage}'] = model(table + [
            element([4, 14, 4], [12, 14.5, 12], {'all': 'plate'}),
            element([5, 14.5, 5], [11, 18, 11], {'north': 'pudding', 'south': 'pudding', 'east': 'pudding', 'west': 'pudding', 'up': 'sauce', 'down': 'pudding'}),
            element([6, 18, 6], [10, 19.25, 10], {'north': 'sauce', 'south': 'sauce', 'east': 'sauce', 'west': 'sauce', 'up': 'sauce', 'down': 'pudding'}),
            element([7.5, 19.25, 7.5], [8.5, 20.25, 8.5], {'all': 'holly'}),
            element([6.25, 19.25, 7.75], [9.75, 19.6, 8.25], {'all': 'holly'})],
            {k: tx[k] for k in laid + ('plate', 'pudding', 'sauce', 'holly')}, tx['pudding'])
        # A pair of candles on one brass stand: its foot, its stem, the arm across and a cup at each end, a candle in each, lit.
        candles = table + [
            element([5, 14, 6], [11, 15, 10], {'all': 'brass'}), element([7.5, 15, 7.5], [8.5, 21, 8.5], {'all': 'brass'}),
            element([3.5, 21, 7.5], [12.5, 22, 8.5], {'all': 'brass'})]
        for x in (3.5, 10.5):
            candles += [element([x, 22, 7], [x + 2, 23, 9], {'all': 'brass'}), element([x + .25, 23, 7.25], [x + 1.75, 28, 8.75], {'all': 'candle'}),
                        element([x + 1, 28, 7.5], [x + 1, 30.5, 8.5], {'east': 'flame', 'west': 'flame'}, uv={'east': [6, 0, 10, 16], 'west': [6, 0, 10, 16]}),
                        element([x + .5, 28, 8], [x + 1.5, 30.5, 8], {'north': 'flame', 'south': 'flame'}, uv={'north': [6, 0, 10, 16], 'south': [6, 0, 10, 16]})]
        m[f'candles_{stage}'] = model(candles, {k: tx[k] for k in laid + ('brass', 'candle', 'flame')}, tx['candle'])
        # A high-backed chair: four legs, a framed seat with its cushion, two posts and a velvet panel between, a rail across the
        # top and a brass knob on each post. Its back comes to a sitter's ears, so that from behind there is a head over every chair.
        chair = [element([x, 0, z], [x + 1.5, 7, z + 1.5], {'all': 'mahogany'}) for x in (2, 12.5) for z in (2, 12.5)]
        chair += [element([2, 7, 2], [14, 8, 14], {'all': 'mahogany'}), element([2.5, 8, 2.5], [13.5, 9, 13], {'all': 'velvet'}),
                  element([2, 9, 13], [3.5, 23, 15], {'all': 'mahogany'}), element([12.5, 9, 13], [14, 23, 15], {'all': 'mahogany'}),
                  element([3.5, 11, 13.5], [12.5, 21, 14.5], {'all': 'velvet'}), element([2, 21, 12.75], [14, 23, 15.25], {'all': 'mahogany'}),
                  element([2.25, 23, 13.5], [3.25, 24, 14.5], {'all': 'brass'}), element([12.75, 23, 13.5], [13.75, 24, 14.5], {'all': 'brass'})]
        m[f'feast_chair_{stage}'] = model(chair, {k: tx[k] for k in ('mahogany', 'velvet', 'brass')}, tx['velvet'])
    # 0.4.649: the King's night-cap, hung on a bough after he has woken (the country come back to). It hangs by its band from a
    # twig of the bough above, limp, its length sagging a little to one side and the tassel at the bottom.
    m['nightcap_0'] = model([element([7.25, 14.5, 7.25], [8.75, 16, 8.75], {'all': 'twig'}),
                             element([5, 12, 5], [11, 14.5, 11], {'all': 'band'}),
                             element([5.5, 8, 5.5], [10.5, 12, 10.5], {'all': 'wool'}),
                             element([6.5, 4.5, 6], [10, 8, 9.5], {'all': 'wool'}),
                             element([7.5, 1.5, 6.5], [9.5, 4.5, 8.5], {'all': 'wool'}),
                             element([7.75, 0, 6.75], [9.25, 1.5, 8.25], {'all': 'tassel'})],
                            {'twig': NS + 'block/king_bark_ruled', 'band': 'nightcap_band', 'wool': 'nightcap', 'tassel': 'tassel'}, 'wool')
    return m


def crown_model():
    """0.4.648: the crown, worn. Drawn round the wearer's head as the game draws whatever is worn there: the model's sixteen units make
    ten pixels, centred on the head, so the head fills 1.6 to 14.4 and its top is at 14.4. A band round the top of the head, eight points
    standing up from it (one at each corner, a taller one in the middle of each side), a stone set in the band at the front and at each
    side."""
    g, j = 'gold', 'jewel'
    els = [element([1.1, 13, 1.1], [14.9, 16, 1.7], {'all': g}), element([1.1, 13, 14.3], [14.9, 16, 14.9], {'all': g}),
           element([1.1, 13, 1.7], [1.7, 16, 14.3], {'all': g}), element([14.3, 13, 1.7], [14.9, 16, 14.3], {'all': g})]
    # The points: one at each corner and one in the middle of each side, the middle ones taller.
    for x, z, tall in ((1.1, 1.1, 2.2), (13.7, 1.1, 2.2), (1.1, 13.7, 2.2), (13.7, 13.7, 2.2)):
        els.append(element([x, 16, z], [x + 1.2, 16 + tall, z + 1.2], {'all': g}))
    for lo, hi in (([7.2, 16, 1.1], [8.8, 19.5, 1.7]), ([7.2, 16, 14.3], [8.8, 19.5, 14.9]), ([1.1, 16, 7.2], [1.7, 19.5, 8.8]), ([14.3, 16, 7.2], [14.9, 19.5, 8.8])):
        els.append(element(lo, hi, {'all': g}))
    for lo, hi in (([7.3, 13.8, 0.8], [8.7, 15.2, 1.1]), ([0.8, 13.8, 7.3], [1.1, 15.2, 8.7]), ([14.9, 13.8, 7.3], [15.2, 15.2, 8.7])):
        els.append(element(lo, hi, {'all': j}))
    return {'parent': 'minecraft:block/block', 'textures': {'gold': NS + 'item/king/crown_gold', 'jewel': NS + 'item/king/crown_jewel',
            'particle': NS + 'item/king/crown_gold'}, 'elements': els,
            # 0.4.649: outside the King's country, and unwatched, the crown is its own drawing (client.KingCrownClient).
            'overrides': [{'predicate': {NS + 'drawn': 1}, 'model': NS + 'item/king_crown_drawn'}],
            'display': CROWN_DISPLAY}


def crown_drawn_model():
    """0.4.649: the crown drawn in outline: the same crown, every edge a pencil line and nothing filled. The band's two rims and its
    four corners; each point's outline standing on the upper rim; the three stones as small squares. A line is four tenths of a unit
    thick, a pencil's width on a head."""
    t = .4; lo, hi = 1.1, 14.9; p = 'pencil'
    def bar(a, b):
        return element([min(a[i], b[i]) for i in range(3)], [max(a[i], b[i]) for i in range(3)], {'all': p})
    els = []
    # The band's rims: lower and upper, right round.
    for y in (13, 16 - t):
        els += [bar([lo, y, lo], [hi, y + t, lo + t]), bar([lo, y, hi - t], [hi, y + t, hi]),
                bar([lo, y, lo + t], [lo + t, y + t, hi - t]), bar([hi - t, y, lo + t], [hi, y + t, hi - t])]
    # Its four corners, between the rims.
    for x in (lo, hi - t):
        for z in (lo, hi - t):
            els.append(bar([x, 13 + t, z], [x + t, 16 - t, z + t]))
    # The corner points: three uprights at the corner and a line across the top of each outer face.
    for cx, cz in ((lo, lo), (hi, lo), (lo, hi), (hi, hi)):
        sx, sz = (1 if cx == lo else -1), (1 if cz == lo else -1); top = 18.2; w = 1.2
        x0, z0 = cx, cz; x1, z1 = cx + sx * w, cz + sz * w
        def post(x, z):
            return bar([x, 16, z], [x + sx * t, top, z + sz * t])
        els += [post(x0, z0), post(x1 - sx * t, z0), post(x0, z1 - sz * t),
                bar([x0, top - t, z0], [x1, top, z0 + sz * t]), bar([x0, top - t, z0], [x0 + sx * t, top, z1])]
    # The taller points in the middle of each side: two uprights and a line across the top, on the outer face.
    top = 19.5
    for z in (lo, hi - t):
        els += [bar([7.2, 16, z], [7.2 + t, top, z + t]), bar([8.8 - t, 16, z], [8.8, top, z + t]), bar([7.2, top - t, z], [8.8, top, z + t])]
    for x in (lo, hi - t):
        els += [bar([x, 16, 7.2], [x + t, top, 7.2 + t]), bar([x, 16, 8.8 - t], [x + t, top, 8.8]), bar([x, top - t, 7.2], [x + t, top, 8.8])]
    # The stones: a small square on the band, at the front and at each side, standing a little proud of it.
    j = .25
    def stone(axis, at):
        out = []
        for a, b, c, d in ((7.3, 13.8, 8.7, 13.8 + j), (7.3, 15.2 - j, 8.7, 15.2), (7.3, 13.8 + j, 7.3 + j, 15.2 - j), (8.7 - j, 13.8 + j, 8.7, 15.2 - j)):
            out.append(bar([a, b, at], [c, d, at + .3]) if axis == 'z' else bar([at, b, a], [at + .3, d, c]))
        return out
    els += stone('z', .8) + stone('x', .8) + stone('x', 14.9)
    return {'parent': 'minecraft:block/block', 'textures': {'pencil': NS + 'item/king/crown_pencil', 'particle': NS + 'item/king/crown_pencil'},
            'elements': els, 'display': CROWN_DISPLAY}


CROWN_DISPLAY = {'gui': {'rotation': [30, 225, 0], 'translation': [0, -2.5, 0], 'scale': [0.85, 0.85, 0.85]},
                        'ground': {'rotation': [0, 0, 0], 'translation': [0, -1, 0], 'scale': [0.4, 0.4, 0.4]},
                        'fixed': {'rotation': [0, 0, 0], 'translation': [0, -3, 0], 'scale': [0.7, 0.7, 0.7]},
                        'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, -1, 0], 'scale': [0.4, 0.4, 0.4]},
                        'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [0.45, 0.45, 0.45]},
                        'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [0.45, 0.45, 0.45]}}


KINDS = ['mantel', 'glass', 'rug', 'chessmen', 'table', 'problem', 'armchair', 'window', 'rattle', 'peg', 'glass_table', 'seat', 'wheel',
         'stalk', 'cap', 'feast_table', 'mutton', 'pudding', 'candles', 'feast_chair', 'nightcap']
STAGES = {'mantel': 3, 'glass': 6, 'window': 2, 'problem': 2, 'feast_table': 2, 'mutton': 2, 'pudding': 2, 'candles': 2, 'feast_chair': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 6
NAMES = {'king_fitting': 'Drawing-Room Things', 'king_turf_light': 'Light Square', 'king_turf_dark': 'Dark Square', 'king_soil': 'Soil',
         'king_hedge': 'Box Hedge', 'king_brook': 'Brook', 'king_bridge': 'Footbridge', 'king_path': 'Gravel Road', 'king_bark': 'Old Bark',
         'king_bough': 'Bough', 'king_wallpaper': 'Trellis Paper', 'king_panel': 'Dado Panel', 'king_floorboards': 'Drawing-Room Floor',
         'king_ceiling': 'Drawing-Room Ceiling', 'king_chimney': 'Chimney Stone', 'king_hearth': 'Hearth',
         'king_verge': "Brook's Edge", 'king_drawn_bridge': 'Drawn Bridge', 'king_card': 'One of the Pack'}
ITEM_NAMES = {'king_bottle': 'A Little Bottle', 'king_cake': 'A Very Small Cake', 'king_crown': 'A Crown'}
for k in list(COUNTRY):
    NAMES[f'king_{k}_ruled'] = NAMES[f'king_{k}'] + ', Ruled'


def java_lists():
    reg = (JAVA / 'KingRegistry.java').read_text()
    start = reg.index('MATERIAL_NAMES=List.of(')
    mats = re.findall(r'"(\w+)"', reg[start:reg.index(');', start)])
    blk = (JAVA / 'KingBlock.java').read_text()
    start = blk.index('enum Kind implements StringRepresentable {') + len('enum Kind implements StringRepresentable {')
    kinds = [k.strip().lower() for k in blk[start:blk.index(';', start)].split(',') if k.strip()]
    return mats, kinds


def files():
    out = {}
    for name in materials():
        out[f'models/block/king_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/king_{name}'}}
        out[f'blockstates/king_{name}.json'] = {'variants': {'': {'model': NS + f'block/king_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/king/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/king/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/king_fitting.json'] = {'variants': variants}
    # 0.4.644: the bottle and the cake, held.
    for item in ('bottle', 'cake'):
        out[f'models/item/king_{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/king/{item}'}}
    # 0.4.648: the crown, a thing in the round, worn on the head.
    out['models/item/king_crown.json'] = crown_model()
    # 0.4.649: and drawn in outline, as it is outside the country.
    out['models/item/king_crown_drawn.json'] = crown_drawn_model()
    # 0.4.642: the brook's verge is never drawn (KingVergeBlock); its model only names what breaking it would scatter.
    out['models/block/king_verge.json'] = {'textures': {'particle': NS + 'block/king_brook'}}
    out['blockstates/king_verge.json'] = {'variants': {'': {'model': NS + 'block/king_verge'}}}
    # 0.4.646: the court's pack (KingCardBlock): every facing, face and whether it has fallen.
    cv = {}
    for facing, y in FACING_Y.items():
        for i, name in enumerate(CARD_FACES):
            for fallen in (False, True):
                v = {'model': NS + f'block/king/card_{name}_{"fallen" if fallen else "standing"}'}
                if y:
                    v['y'] = y
                cv[f'face={i},facing={facing},fallen={str(fallen).lower()}'] = v
    out['blockstates/king_card.json'] = {'variants': cv}
    # 0.4.645: the drawn bridge is the footbridge's ruled drawing, and nothing else (KingDrawnBridgeBlock).
    out['models/block/king_drawn_bridge.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + 'block/king_bridge_ruled'}}
    out['blockstates/king_drawn_bridge.json'] = {'variants': {'': {'model': NS + 'block/king_drawn_bridge'}}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    mats, kinds = java_lists()
    assert mats == list(materials()), ('the game lists other materials', mats)
    assert kinds == KINDS, ('the game lists other kinds', kinds)
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert tex[len(NS):] + '.png' in images, (key, tex)
    # Every country material's ruled twin is its own picture, ruled: never drawn by hand.
    mats_drawn = materials()
    for k in COUNTRY:
        assert mats_drawn[k + '_ruled'].tobytes() == ruled(mats_drawn[k], 6600 + list(COUNTRY).index(k)).tobytes(), k
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    meta = A / 'textures/block/king/fire.png.mcmeta'; want = {'animation': {'frametime': 4, 'interpolate': False}}
    if check:
        assert json.loads(meta.read_text()) == want, 'fire animation'
    else:
        meta.write_text(json.dumps(want, indent=2) + '\n')
    for rel, obj in data.items():
        path = A / rel
        if check:
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    for key, name in {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **{f'item.the_oldest_house.{k}': v for k, v in ITEM_NAMES.items()}}.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" the ruled country: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
