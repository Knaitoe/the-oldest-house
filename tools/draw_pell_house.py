"""The Pell house (0.4.518, docs/PELL_HOUSE_PROPOSAL.md): a low brick bungalow at the head of a cul-de-sac, at the end of summer.

Draws, in native Minecraft pixels from fixed seeds, the street's and the house's own materials and the fittings of the pell_fitting
block, its models facing north and turned by its facing. This slice (the street and the lot): the brick veneer, its lowest course
with weep holes and salt bloom, and the newer brick of the bricked-up window; the roof's tiles and the eaves' board; a watered lawn
and a lawn in a watering ban; concrete, the road, the curb, the patio's pavers, the pool's coping, tiles and waterline band, deck
boards, bark mulch; the shed's iron; plaster, ceiling, carpet and its worn path, lino, the garage's slab and its oil stain; and the
paling fence, the pool fence and its child gate, the side gate, the house's doors, the security screen door, the garage's walk door
and roller door, the shed's door, the windows (clear, blinds down, painted over), the glass sliding door, and the pool's cover.
Writes the models and block states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/pell/'


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


# ------------------------------------------------------------------------------------------------ what things are made of
# 0.4.528 (docs/PELL_HOUSE_DETAIL_0_4_528.md), the owner: "add a little more detail to textures". Still sixteen pixels to the block:
# surfaces with what they are made of showing in them, from fixed seeds. Each one tiles, so a part that takes a strip of it from
# anywhere shows the same stuff; panels drawn over them take their light from the upper left (bevel, inset).
def wood(base, seed, spread=2, across=False, knot=True):
    """Timber: lines of grain along the board, each a shade off the next and drifting, darker streaks running along them, and now
    and then a small knot. The grain runs left to right, or up and down when across."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    tone, lines = 0, []
    for _ in range(16):
        tone = max(-8, min(8, tone + rnd.choice((-4, -2, 0, 0, 2, 4))))
        lines.append(tone)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, lines[x if across else y] + rnd.randint(-spread, spread))
    for _ in range(6):
        ln, start, n, k = rnd.randrange(16), rnd.randrange(16), rnd.randint(3, 9), rnd.choice((-17, -12, -9, 8))
        for i in range(start, start + n):
            x, y = (ln, i % 16) if across else (i % 16, ln)
            px[x, y] = shade(px[x, y], k)
    if knot and rnd.random() < .45:
        cx, cy = rnd.randrange(2, 14), rnd.randrange(2, 14)
        for dx, dy, k in ((0, 0, -26), (1, 0, -16), (-1, 0, -12), (0, 1, -9), (0, -1, -9)):
            px[cx + dx, cy + dy] = shade(px[cx + dx, cy + dy], k)
    return im


def metal(base, seed, spread=2):
    """Brushed metal: fine lines along it, some catching the light and some dull."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    rows = [rnd.choice((-3, -1, 0, 0, 1, 3)) for _ in range(16)]
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, rows[y] + rnd.randint(-spread, spread))
    for _ in range(7):
        y, x0, n, k = rnd.randrange(16), rnd.randrange(16), rnd.randint(3, 8), rnd.choice((13, 9, -9, -12))
        for x in range(x0, x0 + n):
            px[x % 16, y] = shade(px[x % 16, y], k)
    return im


def enamel(base, seed, specks=3):
    """Enamel, glaze or gloss paint: nearly even, a faint unevenness in the coat and a few specks."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, rnd.choice((-2, -1, 0, 0, 0, 1)))
    for _ in range(specks):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = shade(px[x, y], -9)
    return im


def cloth(base, seed, spread=2):
    """Woven cloth: a fine twill, the weave catching the light on alternate threads."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            k = (2 if (x + y) % 2 == 0 else -2) + (-4 if (x - y) % 4 == 0 else 0) + rnd.randint(-spread, spread)
            px[x, y] = shade(base, k)
    return im


def card(base, seed):
    """Cardboard: the flutes under its face showing as faint bands, a few fibres and specks."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, (-4 if y % 3 == 2 else 2 if y % 3 == 0 else 0) + rnd.randint(-2, 2))
    for _ in range(5):
        x, y, n = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 3)
        for i in range(n):
            px[(x + i) % 16, y] = shade(px[(x + i) % 16, y], -11)
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = shade(px[x, y], 10)
    return im


def plastic(base, seed):
    """Moulded plastic: smooth, a soft sheen, a scuff or two."""
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(base, rnd.choice((-1, 0, 0, 1)))
    for _ in range(2):
        x, y, n = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 4)
        for i in range(n):
            px[(x + i) % 16, (y + i) % 16] = shade(px[(x + i) % 16, (y + i) % 16], -7)
    return im


def bevel(im, x0, y0, x1, y1, k=14):
    """A raised panel from (x0, y0) to before (x1, y1): its top and left edges catch the light, its bottom and right edges are in
    shadow. What is drawn there keeps its own texture, lighter or darker."""
    px = im.load()
    for x in range(max(0, x0), min(16, x1)):
        for y, d in ((y0, k), (y1 - 1, -k)):
            if 0 <= y < 16 and px[x, y][3]:
                px[x, y] = shade(px[x, y], d, px[x, y][3])
    for y in range(max(0, y0 + 1), min(16, y1 - 1)):
        for x, d in ((x0, k), (x1 - 1, -k)):
            if 0 <= x < 16 and px[x, y][3]:
                px[x, y] = shade(px[x, y], d, px[x, y][3])


def inset(im, x0, y0, x1, y1, k=14):
    """A recess (a bowl, a screen, a groove): the opposite of a bevel, its top and left edges in shadow, its bottom and right lit."""
    bevel(im, x0, y0, x1, y1, -k)


# ------------------------------------------------------------------------------------------------ materials
def brick(face='#a8664a', mortar='#cdbfa8', seed=51801, vary=9):
    """Running bond: four courses to a block, a brick seven pixels long and three high between one-pixel joints."""
    im = new(mortar, seed, 3); rnd = random.Random(seed + 1); px = im.load()
    for course in range(4):
        y0 = course * 4; off = 0 if course % 2 == 0 else 4
        for start in (off - 8, off, off + 8):
            tone = rnd.randint(-vary, vary)
            for x in range(start, start + 7):
                for y in range(y0, y0 + 3):
                    if 0 <= x < 16:
                        px[x, y] = shade(face, tone + rnd.randint(-3, 3) + (7 if y == y0 else -7 if y == y0 + 2 else 0))
    return im


def brick_low():
    """The lowest course: a weep hole in two of its upright joints, and salt bloom drawn up out of the ground."""
    im = brick(seed=51803)
    for x in (7, 11):
        dot(im, x, 13, '#2c2622'); dot(im, x, 14, '#2c2622')
    rnd = random.Random(51804); px = im.load()
    for y in range(8, 16):
        for x in range(16):
            if rnd.random() < (y - 6) / 14:
                r, g, b, _ = px[x, y]; k = 26 + rnd.randint(0, 14)
                px[x, y] = (min(255, r + k), min(255, g + k + 2), min(255, b + k + 6), 255)
    return im


def roof():
    """Concrete roof tiles in courses, each lapping the one below: every tile a shade apart from its neighbours, its rounded top
    catching the light and its nose in shadow; a few spots of lichen."""
    im = new('#8f5b49', 51806, 4); rnd = random.Random(51805); px = im.load()
    for row, y0 in enumerate((0, 4, 8, 12)):
        for x0 in range((0 if row % 2 == 0 else 4) - 8, 16, 8):
            tone = rnd.randint(-8, 8)
            for x in range(max(0, x0), min(16, x0 + 8)):
                for y, k in ((y0, 12), (y0 + 1, 4), (y0 + 2, -6)):
                    px[x, y] = shade(px[x, y], tone + k)
    for y in (3, 7, 11, 15):
        fill(im, 0, y, 16, y + 1, '#5e392e')
    for row, y in enumerate((0, 4, 8, 12)):
        for x in range(0 if row % 2 == 0 else 4, 16, 8):
            fill(im, x, y, x + 1, y + 3, '#774a3b')
    speckle(im, 51807, 9, ['#a16a57', '#7f5041'])
    for x, y in ((5, 1), (6, 1), (12, 9), (2, 10)):
        dot(im, x, y, '#a8a07c')
    return im


def fascia():
    """The eaves' board: painted timber, the grain showing through the paint, a nail head, the paint flaking in two places."""
    im = wood('#ddd5c3', 51808, 1, knot=False)
    fill(im, 0, 15, 16, 16, '#b9b09c'); fill(im, 0, 14, 16, 15, '#d6cebb'); fill(im, 0, 0, 16, 1, '#f3eee2')
    for x, y in ((3, 6), (4, 7), (11, 9), (12, 9)):
        dot(im, x, y, '#cfc6b2')
    dot(im, 8, 7, '#8e8a80'); dot(im, 9, 8, '#d0c8b4')
    return im


def lawn(dry):
    """A lawn from above: the blades in clumps, each dark at its root, lighter up its length and palest at its tip, some leaning. June's
    is watered, with a little clover in it. Dry, the street's lawns in the watering ban: straw, bare soil showing in patches, a few
    tufts still green. Nothing pale enough to stand out: one bright pixel would repeat on every block of lawn as a grid of dots."""
    rnd = random.Random(51812 + dry)
    if dry:
        im = new('#a88e54', 51811, 6); root, mid, tip, bright = '#86703f', '#b59c5e', '#cdb677', '#e0cc8e'
    else:
        im = new('#4c8730', 51810, 5); root, mid, tip, bright = '#3b6c24', '#5a9a38', '#76b44a', '#92c95e'
    px = im.load()
    if dry:
        for _ in range(3):
            x, y = rnd.randrange(16), rnd.randrange(16)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (2, 1)):
                px[(x + dx) % 16, (y + dy) % 16] = shade('#8a6e46', rnd.randint(-6, 6))
    for _ in range(50 if dry else 58):
        x, y, lean = rnd.randrange(16), rnd.randrange(16), rnd.choice((0, 0, 1, -1))
        px[x, (y + 2) % 16] = shade(root, rnd.randint(-3, 3))
        px[x, (y + 1) % 16] = shade(mid, rnd.randint(-4, 4))
        px[(x + lean) % 16, y] = shade(tip if rnd.random() < .8 else bright, rnd.randint(-3, 3))
    if dry:
        for _ in range(4):
            x, y = rnd.randrange(16), rnd.randrange(16)
            px[x, y] = shade('#7a8a40', 0); px[x, (y + 1) % 16] = shade('#5e6e30', 0)
    else:
        x, y = rnd.randrange(16), rnd.randrange(16)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            px[(x + dx) % 16, (y + dy) % 16] = shade('#4e9a48', 0)
    return im


def concrete(seed=51814, joint=True):
    im = new('#b7b3a8', seed, 4); speckle(im, seed + 1, 22, ['#a5a196', '#c6c2b8', '#9c988e', '#cdc9bf', '#8f8b82'])
    if joint:
        fill(im, 0, 0, 16, 1, '#8f8b81'); fill(im, 0, 0, 1, 16, '#8f8b81')
    return im


def concrete_print():
    """The path's slab with a small hand pressed into it when it was wet, and a date scratched beside it with a stick."""
    im = concrete(51814, True)
    for x, y in ((5, 7), (6, 7), (7, 7), (5, 8), (6, 8), (7, 8), (8, 8), (5, 9), (6, 9), (7, 9), (8, 9), (6, 10), (7, 10)):
        dot(im, x, y, '#97938a')
    for x, y in ((4, 5), (5, 4), (6, 4), (7, 4), (8, 5), (9, 7)):
        dot(im, x, y, '#97938a')
    for x in range(10, 15):
        if x != 12:
            dot(im, x, 12, '#8a867c')
    dot(im, 12, 13, '#8a867c'); dot(im, 10, 13, '#8a867c'); dot(im, 14, 11, '#8a867c')
    return im


def concrete_oil():
    """The drive where a second car used to stand: an old oil stain, gone dull."""
    im = concrete(51815, True)
    for y in range(3, 14):
        for x in range(2, 14):
            d = ((x - 8) ** 2 + (y - 8.5) ** 2 * 1.5) ** .5
            if d < 5.2:
                dot(im, x, y, '#7d796f' if d < 3 else '#9a968c')
    return im


def asphalt():
    """The road, sealed years ago and gone grey in the sun: the stones of its aggregate showing, some bigger and lit on their upper
    side, and darker where the tar has come up."""
    im = new('#47474a', 51816, 4); rnd = random.Random(51817); px = im.load()
    for _ in range(46):
        x, y = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice(['#606064', '#6c6c70', '#56565a', '#7a7a7c', '#665e56'])
        px[x, y] = shade(c, rnd.randint(-3, 3))
        if rnd.random() < .3:
            px[(x + 1) % 16, (y + 1) % 16] = shade(c, -16)
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            px[(x + dx) % 16, (y + dy) % 16] = shade('#38383b', rnd.randint(-2, 2))
    return im


def kerb():
    im = new('#a19f98', 51818, 3); speckle(im, 51819, 14, ['#8f8d86', '#b2b0a9', '#97958e'])
    fill(im, 0, 0, 16, 1, '#c2c0ba'); fill(im, 0, 15, 16, 16, '#7f7d77'); fill(im, 7, 0, 8, 16, '#8a8882')
    return im


def paver():
    im = new('#c7ab8b', 51820, 5)
    rnd = random.Random(51822)
    for oy in (0, 8):
        for ox in (0, 8):
            tone = rnd.randint(-7, 7)
            for y in range(oy + 1, oy + 8):
                for x in range(ox + 1, ox + 8):
                    edge = 10 if (y == oy + 1 or x == ox + 1) else -9 if (y == oy + 7 or x == ox + 7) else 0
                    dot(im, x, y, shade(im.getpixel((x, y)), tone + edge))
    for i in range(16):
        dot(im, i, 0, '#96806a'); dot(im, i, 8, '#96806a'); dot(im, 0, i, '#96806a'); dot(im, 8, i, '#96806a')
    speckle(im, 51821, 8, ['#b39878', '#d6bd9f'])
    return im


def coping():
    im = new('#dcd5c4', 51822, 3); speckle(im, 51823, 10, ['#cfc8b6', '#e6e0d1', '#c4bcaa'])
    fill(im, 0, 0, 16, 2, '#efeadd'); fill(im, 0, 0, 16, 1, '#f7f3ea'); fill(im, 0, 14, 16, 16, '#bdb5a2'); fill(im, 0, 15, 16, 16, '#aaa290')
    for x in (0, 8):
        fill(im, x, 0, x + 1, 16, '#b3ab98'); fill(im, x + 1, 2, x + 2, 14, '#e4ded0')
    return im


def pool_tile(band=False):
    """Small square tiles, three pixels and a line of grout. The band at the waterline is a darker row with a paler one under it."""
    im = new('#6bbfd0', 51824, 3)
    for i in range(0, 16, 4):
        fill(im, i + 3, 0, i + 4, 16, '#d5e9ea'); fill(im, 0, i + 3, 16, i + 4, '#d5e9ea')
    rnd = random.Random(51825)
    for ty in range(4):
        for tx in range(4):
            if rnd.random() < .3:
                fill(im, tx * 4, ty * 4, tx * 4 + 3, ty * 4 + 3, rnd.choice(['#5eb2c4', '#78c9d8']))
            dot(im, tx * 4, ty * 4, shade(im.getpixel((tx * 4, ty * 4)), 22))
    if band:
        for tx in range(4):
            fill(im, tx * 4, 0, tx * 4 + 3, 3, '#1f5f8c' if tx % 2 == 0 else '#2c79a6')
            fill(im, tx * 4, 4, tx * 4 + 3, 7, '#bfe3ea')
    return im


def pool_band_missing():
    """0.4.533: the waterline band with one of its tiles gone: the bed of adhesive where it was, combed in ridges, a chip of the old
    tile's edge left in one corner."""
    im = pool_tile(True)
    fill(im, 8, 0, 11, 3, '#8a8f88')
    for x in (8, 10):
        fill(im, x, 0, x + 1, 3, '#7a7f78')
    dot(im, 8, 0, '#1f5f8c'); dot(im, 11, 3, '#9aa098')
    return im


def pool_band_odd():
    """0.4.533: the waterline band with one tile put back that does not match: a paler, greener tile, a pixel proud of its neighbours,
    its grout lines fresher and whiter than theirs."""
    im = pool_tile(True)
    fill(im, 3, 0, 4, 4, '#f4fafa'); fill(im, 7, 0, 8, 4, '#f4fafa'); fill(im, 4, 3, 7, 4, '#f4fafa')
    fill(im, 4, 0, 7, 3, '#3f9a8a'); fill(im, 4, 0, 7, 1, '#57b2a0'); dot(im, 6, 2, '#2f8070')
    return im


def pool_drain():
    """0.4.533: the deep end's floor at its middle: the drain's grate, a square of bars in a frame among the tiles, and under it,
    seen between the bars, a child's swimming goggles: two small lenses on a bridge, the strap trailing out of the corner."""
    im = pool_tile()
    fill(im, 4, 4, 12, 12, '#c4cccc')
    fill(im, 5, 5, 11, 11, '#20292c')
    lens, rim, strap = '#b2569a', '#6e2f60', '#3a4ab0'
    # The goggles lie across the sump, under the bars: each lens two pixels by two in its rim, the bridge between, the strap out to the
    # corner.
    for x0 in (5, 8):
        fill(im, x0, 6, x0 + 3, 9, rim); fill(im, x0 + 1, 7, x0 + 2, 8, lens)
    dot(im, 7, 7, rim); dot(im, 10, 9, strap); dot(im, 10, 10, strap)
    for x in (6, 8, 10):
        fill(im, x, 5, x + 1, 11, '#d5dcdc')
    fill(im, 5, 5, 11, 6, '#aab3b3')
    return im


def deck():
    im = wood('#8d6f4f', 51826, 3, knot=False)
    for y in (0, 4, 8, 12):
        for x in range(16):
            dot(im, x, y, shade(im.getpixel((x, y)), 9))
    for y in (3, 7, 11, 15):
        fill(im, 0, y, 16, y + 1, '#4f3d2b')
    for x, y in ((2, 1), (13, 1), (6, 5), (10, 9), (3, 13), (12, 13)):
        dot(im, x, y, '#3d3024')
    speckle(im, 51827, 9, ['#9c7d5b', '#7c6145'])
    return im


def mulch():
    """Bark mulch in the front garden's beds: chips of bark in several browns, each lit along its upper edge, dark gaps between them,
    and a twig or two."""
    im = new('#3a281b', 51828, 4); rnd = random.Random(51829); px = im.load()
    for _ in range(24):
        x, y = rnd.randrange(16), rnd.randrange(16)
        w, h = rnd.choice(((2, 1), (3, 1), (2, 2), (3, 2), (1, 2)))
        c = rnd.choice(['#6e4a30', '#7a553a', '#5e3e28', '#8a6444', '#7a6a5a'])
        for dy in range(h):
            for dx in range(w):
                k = 14 if dy == 0 else -10 if dy == h - 1 else 0
                px[(x + dx) % 16, (y + dy) % 16] = shade(c, k + rnd.randint(-4, 4))
    for _ in range(2):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for i in range(4):
            px[(x + i) % 16, (y + i // 2) % 16] = shade('#9a8a72', rnd.randint(-6, 6))
    return im


def iron():
    """Corrugated iron: ribs every four pixels, lit on one side, and rust creeping from a seam."""
    im = new('#9ba4a8', 51830, 3)
    for x in range(0, 16, 4):
        fill(im, x, 0, x + 1, 16, '#c3cbce'); fill(im, x + 2, 0, x + 3, 16, '#7c8589')
    rnd = random.Random(51831)
    for y in range(16):
        for x in (15, 14, 13):
            if rnd.random() < (.75 if x == 15 else .35 if x == 14 else .12):
                dot(im, x, y, rnd.choice(['#8a4a2c', '#a35a33', '#6e3a24']))
    for y in (2, 13):
        dot(im, 5, y, '#5a6165')
    return im


def plaster():
    """Plaster, painted: the trowel's long sweeps under the paint, faintly lighter and darker."""
    im = new('#e9e3d4', 51832, 2); speckle(im, 51833, 8, ['#dfd8c7', '#f0ebdf'])
    rnd = random.Random(51831)
    for _ in range(4):
        y, x0, n, k = rnd.randrange(16), rnd.randrange(16), rnd.randint(4, 8), rnd.choice((4, -4))
        for i in range(n):
            x = (x0 + i) % 16; yy = (y + (i // 3) * (1 if k > 0 else -1)) % 16
            dot(im, x, yy, shade(im.getpixel((x, yy)), k))
    return im


def ceiling():
    """The ceiling's stipple: a fine bumpy finish, its bumps lit on one side."""
    im = new('#f1eee6', 51834, 2); rnd = random.Random(51835)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#e2ded3'); dot(im, (x + 15) % 16, (y + 15) % 16, '#f8f6f0')
    return im


# Nessa's stuck-on stars: four layouts, so the ceiling is no pattern. Small stars, a few five-pointed ones, single specks, and in
# one a moon; every block of the ceiling takes one of the four, turned one of four ways (its blockstate picks by place).
STAR_SHAPES = {'s': ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)), 'd': ((0, 0),),
               'f': ((0, -2), (0, -1), (-2, 0), (-1, 0), (0, 0), (1, 0), (2, 0), (-1, 1), (0, 1), (1, 1), (-1, 2), (1, 2)),
               'm': ((1, 0), (2, 0), (0, 1), (1, 1), (0, 2), (0, 3), (0, 4), (1, 4), (1, 5), (2, 5))}
STAR_LAYOUTS = [[('s', 3, 3), ('f', 10, 9), ('d', 13, 3), ('d', 5, 12)],
                [('s', 11, 4), ('s', 4, 10), ('d', 8, 7), ('d', 13, 13)],
                [('m', 8, 5), ('d', 3, 3), ('s', 3, 12), ('d', 13, 12)],
                [('f', 5, 5), ('s', 12, 11), ('d', 12, 3), ('d', 2, 12), ('d', 8, 14)]]


def star_points(layout):
    for kind, x, y in STAR_LAYOUTS[layout]:
        for i, (dx, dy) in enumerate(STAR_SHAPES[kind]):
            yield x + dx, y + dy, kind == 'd' or (kind == 's' and i == 0) or kind in 'fm'


def ceiling_stars(layout=0):
    """Nessa's ceiling: stuck-on stars, pale by day."""
    im = ceiling()
    for x, y, _ in star_points(layout):
        dot(im, x, y, '#e4e6d2')
    return im


def stars_glow(layout=0):
    """The same stars, as they show in the dark (drawn over the ceiling, lit by themselves)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x, y, bright in star_points(layout):
        dot(im, x, y, '#c8f0b0', 210 if bright else 140)
    return im


def carpet(worn):
    im = new('#8b806d', 51836, 4); rnd = random.Random(51837)
    for y in range(16):
        for x in range(16):
            if (x + 2 * y) % 4 == 0:
                dot(im, x, y, shade(im.getpixel((x, y)), -7))
            elif (x + 2 * y) % 4 == 2:
                dot(im, x, y, shade(im.getpixel((x, y)), 5))
    for _ in range(30):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#7a705f', '#9a8f7b']))
    if worn:
        # A path worn down the middle of it, paler and flat, where one person has walked every day.
        px = im.load()
        for y in range(16):
            for x in range(4, 12):
                edge = x in (4, 11)
                r, g, b, _ = px[x, y]; k = 12 if edge else 24
                px[x, y] = (min(255, r + k), min(255, g + k), min(255, b + k - 2), 255)
    return im


def lino():
    im = new('#d2caa9', 51838, 3); speckle(im, 51839, 10, ['#c8bf9c', '#dcd5b6'])
    for i in range(16):
        dot(im, i, i, '#b9b08c'); dot(im, 15 - i, i, '#b9b08c')
    for x, y in ((0, 8), (8, 0), (15, 8), (8, 15), (8, 8)):
        dot(im, x, y, '#a89f7c')
    for x, y in ((4, 8), (8, 4), (12, 8), (8, 12)):
        dot(im, x, y, '#bfb692')
    return im


def lino_worn():
    """The kitchen's lino where one person has walked between the sink and the door every day: the pattern gone."""
    im = lino()
    px = im.load()
    for y in range(16):
        for x in range(3, 13):
            r, g, b, _ = px[x, y]; k = 8 if x in (3, 12) else 16
            px[x, y] = (min(255, r + k), min(255, g + k), min(255, b + k), 255)
    return im


def lino_burn():
    """In front of the stove: a burn in the lino the shape of a pan's base, and a smaller one."""
    im = lino()
    for y in range(16):
        for x in range(16):
            d = ((x - 7) ** 2 + (y - 6) ** 2) ** .5
            if 3.2 < d < 4.6:
                dot(im, x, y, '#6a5a3a')
            elif d <= 3.2:
                dot(im, x, y, '#8a7a56')
    dot(im, 12, 12, '#6a5a3a'); dot(im, 13, 12, '#6a5a3a'); dot(im, 12, 13, '#6a5a3a')
    return im


def slab(oil):
    im = new('#a4a097', 51840, 4); speckle(im, 51841, 24, ['#948f86', '#b3afa6', '#8a857c', '#bcb8af'])
    if oil:
        for y in range(4, 13):
            for x in range(3, 13):
                d = ((x - 7.5) ** 2 + (y - 8.2) ** 2 * 1.6) ** .5
                if d < 4.6:
                    dot(im, x, y, '#4b4840' if d < 2.4 else '#6b675e')
        dot(im, 12, 6, '#6b675e'); dot(im, 4, 11, '#6b675e')
    return im


def materials():
    return {'brick': brick(), 'brick_low': brick_low(), 'brick_new': brick('#b67253', '#e2d9c8', 51802, 4), 'roof': roof(), 'fascia': fascia(),
            'lawn': lawn(0), 'lawn_dry': lawn(1), 'concrete': concrete(), 'concrete_print': concrete_print(), 'concrete_oil': concrete_oil(), 'asphalt': asphalt(), 'curb': kerb(), 'paver': paver(),
            'coping': coping(), 'pool_tile': pool_tile(), 'pool_band': pool_tile(True), 'pool_band_missing': pool_band_missing(), 'pool_band_odd': pool_band_odd(),
            'pool_drain': pool_drain(), 'deck': deck(), 'mulch': mulch(), 'iron': iron(),
            'plaster': plaster(), 'ceiling': ceiling(), 'ceiling_stars': ceiling_stars(), 'carpet': carpet(False), 'carpet_worn': carpet(True), 'lino': lino(),
            'lino_worn': lino_worn(), 'lino_burn': lino_burn(),
            'slab': slab(False), 'slab_oil': slab(True)}


def like(name):
    """What each material is like to walk on and to hear (PellRegistry reads this table through its own switch; the tool checks both agree)."""
    return {'lawn': 'grass', 'lawn_dry': 'grass', 'mulch': 'grass', 'deck': 'wood', 'carpet': 'wool', 'carpet_worn': 'wool', 'iron': 'metal'}.get(name, 'stone')


# ------------------------------------------------------------------------------------------------ fittings' faces
def paling():
    """A paling fence: boards four pixels wide with a gap of shadow between, grey with weather and the grain standing up in it, each
    board's left edge catching the light, a rail's nails across."""
    im = wood('#9a8c78', 51850, 3, across=True)
    for x in (0, 4, 8, 12):
        for y in range(16):
            dot(im, x, y, shade(im.getpixel((x, y)), 9))
    for x in (3, 7, 11, 15):
        fill(im, x, 0, x + 1, 16, '#5e5446')
    for x in (1, 5, 9, 13):
        dot(im, x, 3, '#4a4238'); dot(im, x, 12, '#4a4238')
    speckle(im, 51851, 10, ['#8a7d6a', '#a89a85'])
    fill(im, 0, 0, 16, 1, '#b3a590')
    return im


def paling_gate():
    """The side gate: the same boards, a brace across them, a ring latch, and its foot planed off pale and new."""
    im = paling()
    for i in range(12):
        dot(im, 2 + i, 12 - (i * 8) // 11, '#6f6352'); dot(im, 2 + i, 13 - (i * 8) // 11, '#7d705d')
    fill(im, 12, 6, 14, 9, '#3a3a3c'); dot(im, 13, 7, '#8c8c90')
    fill(im, 0, 14, 16, 15, '#d8c8a8'); fill(im, 0, 15, 16, 16, (0, 0, 0), 0)
    return im


def pool_bars(gate=False, lock=False):
    """The pool's fence: black powder-coated rails, upright bars two pixels apart. The child gate has a latch box, and a new padlock."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x in range(1, 16, 3):
        fill(im, x, 1, x + 1, 16, '#26282a')
        for y in range(2, 16, 5):
            dot(im, x, y, '#4a4e52')
    fill(im, 0, 1, 16, 2, '#34373a'); fill(im, 0, 13, 16, 14, '#34373a')
    for x in range(0, 16, 4):
        dot(im, x, 1, '#4e5256'); dot(im, x, 13, '#4e5256')
    if gate:
        fill(im, 11, 4, 14, 8, '#1c1d1f'); dot(im, 12, 5, '#6a6d70')
        fill(im, 0, 1, 1, 16, '#34373a'); fill(im, 15, 1, 16, 16, '#34373a')
    if lock:
        fill(im, 11, 8, 14, 11, '#d9b23c'); dot(im, 12, 9, '#7a6018'); fill(im, 11, 7, 12, 8, '#c9ced2'); fill(im, 13, 7, 14, 8, '#c9ced2')
    return im


def door(upper, kind):
    """A door's face. kind: 'inner' a flat white door with a round knob; 'garage' a ledged door with a bolt on this side; 'shed' iron
    with a hasp and padlock."""
    if kind == 'shed':
        im = iron(); fill(im, 0, 0, 1, 16, '#5d6468'); fill(im, 15, 0, 16, 16, '#5d6468')
        if not upper:
            fill(im, 10, 2, 15, 4, '#55595c'); fill(im, 11, 4, 14, 8, '#c9a53a'); dot(im, 12, 6, '#6e5814')
        return im
    base = '#efe9da' if kind == 'inner' else '#b9c2b0'
    seed = 51860 + upper + (5 if kind == 'garage' else 0)
    im = enamel(base, seed) if kind == 'inner' else wood(base, seed, 2, across=True, knot=False)
    fill(im, 0, 0, 1, 16, shade(base, -34)); fill(im, 15, 0, 16, 16, shade(base, -34))
    fill(im, 1, 0, 2, 16, shade(base, 8)); fill(im, 14, 0, 15, 16, shade(base, -12))
    if upper:
        fill(im, 0, 0, 16, 1, shade(base, -34))
    if kind == 'inner':
        y0, y1 = (2, 14) if upper else (1, 13)
        fill(im, 3, y0, 13, y1, shade(base, -9)); fill(im, 4, y0 + 1, 12, y1 - 1, shade(base, 3))
        inset(im, 3, y0, 13, y1, 10); bevel(im, 4, y0 + 1, 12, y1 - 1, 8)
        # 0.4.566: no knob painted on; the door's model has one of its own (fitting_models).
    else:
        for y in (3, 12):
            fill(im, 1, y, 15, y + 2, shade(base, -22)); fill(im, 1, y, 15, y + 1, shade(base, -14)); fill(im, 1, y + 2, 15, y + 3, shade(base, -30))
        for x in (5, 10):
            fill(im, x, 0, x + 1, 16, shade(base, -16))
        if not upper:
            # The bolt: a barrel and its keep, on the yard's side.
            fill(im, 8, 9, 15, 11, '#3b3d3f'); fill(im, 6, 9, 8, 11, '#8e9296'); dot(im, 9, 8, '#8e9296')
    return im


def door_back(upper, kind):
    """The other face of the garage's door: no bolt on it at all."""
    im = door(upper, kind)
    if kind == 'garage' and not upper:
        base = '#b9c2b0'
        fill(im, 6, 8, 15, 11, base)
        for x in (5, 10):
            fill(im, x, 8, x + 1, 11, shade(base, -16))
    return im


def screen(upper):
    """The security screen door: a white aluminium frame, a diamond grille over flyscreen. On the lower leaf the screen is torn at a
    child's height and patched with tape."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 16, '#3a403c', 150)
    for i in range(-16, 16, 4):
        for t in range(16):
            dot(im, i + t, t, '#d9dcd6'); dot(im, i + 16 - t, t, '#d9dcd6')
    fill(im, 0, 0, 1, 16, '#eceee9'); fill(im, 15, 0, 16, 16, '#eceee9')
    fill(im, 0, 0, 16, 1, '#eceee9') if upper else fill(im, 0, 15, 16, 16, '#eceee9')
    if upper:
        fill(im, 0, 15, 16, 16, '#eceee9')
    else:
        fill(im, 12, 0, 15, 3, '#c9ced2'); dot(im, 13, 1, '#5a5f63')
        # The tear, and the tape over it.
        for x, y in ((4, 6), (5, 7), (5, 8), (6, 9), (7, 9)):
            dot(im, x, y, '#16181a')
        fill(im, 3, 5, 9, 7, '#c8b98a', 235); fill(im, 4, 8, 9, 11, '#c8b98a', 235); dot(im, 3, 6, '#a8996c'); dot(im, 8, 10, '#a8996c')
    return im


def roller():
    """The garage's roller door: slats a pixel of shadow apart, never opened; a handle low in the middle."""
    im = metal('#cfd3cf', 51870, 1)
    for y in range(1, 16, 2):
        fill(im, 0, y, 16, y + 1, '#aab0ab')
        if y + 1 < 16:
            fill(im, 0, y + 1, 16, y + 2, '#dde1dd')
    fill(im, 6, 13, 10, 14, '#55595c'); dot(im, 6, 13, '#7a7e82'); fill(im, 6, 14, 10, 15, '#8e928e')
    return im


def glass(kind):
    """A window's pane. clear: almost nothing, two streaks of sky; blinds: slats down and shut; painted: the line's white, brushed on
    from inside, a hair of the glass showing at one edge; night: the same clear pane (the model is the same)."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    if kind == 'clear':
        fill(im, 0, 0, 16, 16, '#cfe6ee', 34)
        for i in range(5):
            dot(im, 3 + i, 7 - i, '#f4fbfd', 150); dot(im, 9 + i, 12 - i, '#f4fbfd', 110)
    elif kind == 'blinds':
        # Venetian blinds, down and shut: each slat lit along its top and shaded under it, a crack of the dark room between two of
        # them, one slat bent, and the cord.
        fill(im, 0, 0, 16, 16, '#d9d2bd')
        for y in range(0, 16, 2):
            fill(im, 0, y, 16, y + 1, '#e4ddc9'); fill(im, 0, y + 1, 16, y + 2, '#b3ab93')
        fill(im, 0, 9, 16, 10, '#5a554c'); fill(im, 3, 6, 7, 7, '#ece6d4'); fill(im, 3, 7, 7, 8, '#9e967e')
        fill(im, 12, 0, 13, 16, '#8f886f'); dot(im, 12, 15, '#6e6852')
    else:
        im = new('#f1efe8', 51872, 3)
        rnd = random.Random(51873)
        for _ in range(12):
            x, y = rnd.randrange(14), rnd.randrange(16)
            fill(im, x, y, x + 3, y + 1, '#e3e0d6')
        fill(im, 15, 3, 16, 9, '#7d99a3', 190)
    return im


def soffit():
    """The eaves' lining from below: fibre-cement sheet painted cream and in the shade, the joint strip along its edge with its nails,
    and a cobweb gone grey in one corner."""
    im = enamel('#d2cab6', 51890, 2)
    fill(im, 0, 0, 16, 1, '#b8b09c'); fill(im, 0, 1, 16, 2, '#c4bca8')
    for x in (2, 7, 12):
        dot(im, x, 0, '#8e8676')
    for x, y in ((14, 13), (15, 14), (13, 14), (15, 12)):
        dot(im, x, y, '#bdb5a3')
    return im


def frame():
    im = metal('#c9ccc8', 51874, 1); fill(im, 0, 0, 16, 1, '#e4e6e2'); fill(im, 0, 1, 16, 2, '#d8dad6'); fill(im, 0, 15, 16, 16, '#9a9e9a')
    fill(im, 0, 14, 16, 15, '#b2b6b2')
    return im


def tarp(plain=False):
    """The pool's cover: a blue tarpaulin, paler where the sun has had it, faintly creased where it was folded, a little grit. Its leaves
    have all washed down into the pond. Nothing on it runs from edge to edge, and its cells are turned at random as vanilla turns
    grass, so it does not repeat in rows. Plain, only the cloth, which the pond's edges are drawn on."""
    im = new('#4f6f96', 51876, 4)
    if plain:
        return im
    rnd = random.Random(51877); px = im.load()
    for y in range(16):
        for x in range(16):
            d = ((x - 10.5) ** 2 + (y - 4.5) ** 2) ** .5
            if d < 5:
                px[x, y] = shade(px[x, y], 6 if d < 3.5 else 3)
    for x0, y0, x1, y1 in ((2, 11, 8, 8), (9, 13, 13, 12)):
        n = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(n + 1):
            x, y = round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)
            px[x, y] = shade(px[x, y], 9); px[x, y + 1] = shade(px[x, y + 1], -7)
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = shade('#435470', rnd.randint(-4, 4))
    return im


def pond(leaves=True):
    """Rainwater held where the cover sags: dark and still, the cover's blue showing through it here and there, the sky faint in two
    ripples, a smear of sludge, and one fallen gum leaf (none along the pond's edges, where they would repeat cell after cell)."""
    im = new('#2e3e3b', 51891, 3); rnd = random.Random(51892); px = im.load()
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = shade('#33494f', rnd.randint(-3, 3))
    for x, y, n in ((3, 3, 3), (9, 12, 2)):
        for dx in range(n):
            px[x + dx, y] = shade('#3e5551', rnd.randint(-2, 2))
    for x, y in ((11, 4), (12, 4), (12, 5), (5, 10), (6, 10)):
        px[x, y] = shade('#263431', rnd.randint(-2, 2))
    if leaves:
        for x, y, c in ((3, 10, '#8a6a2c'), (4, 10, '#7a5a26'), (5, 11, '#5e4a20')):
            dot(im, x, y, c)
    return im


def pond_edge(corner):
    """Where the pond meets the dry cover, as drawn facing north: the dry cloth to the north (and to the west, at a corner), the
    cloth darkened where the water has stood, a broken line of grit where it stood highest, the water's edge catching the sky, then
    the water. A corner's edge is a quarter circle ten pixels round its south-east corner, so it meets the straight edges six pixels
    in, and turned copies of the two make one pond with rounded corners. A leaf has caught at each corner's edge."""
    im = tarp(True); water = pond(False); px = im.load(); wpx = water.load(); rnd = random.Random(51893 + corner)
    for y in range(16):
        for x in range(16):
            t = 16 - ((15.5 - x) ** 2 + (15.5 - y) ** 2) ** .5 if corner else y + .5
            if t > 6:
                px[x, y] = shade(wpx[x, y], 10) if t <= 7 else wpx[x, y]
            elif t > 4:
                px[x, y] = shade(px[x, y], -14 if t > 5 else -9)
            elif t > 3 and rnd.random() < .6:
                px[x, y] = shade('#5f5c4c', rnd.randint(-5, 5))
    if corner:
        for x, y, c in ((9, 8, '#8a6a2c'), (10, 8, '#7a5a26'), (10, 9, '#5e4a20')):
            dot(im, x, y, c)
    return im


def tin(front):
    """The letterbox: tin gone dull, a dent in it. On its front, the flap's slot and the number in stick-on figures: a 9, and beside it
    the clean shape where the figure before it used to be."""
    im = new('#8f9698', 51880 + front, 4)
    fill(im, 0, 0, 16, 1, '#b7bdbe'); fill(im, 0, 15, 16, 16, '#5f6567')
    for x, y in ((3, 4), (4, 5), (4, 6), (5, 7)):
        dot(im, x, y, '#6f7577')
    if front:
        fill(im, 1, 2, 15, 4, '#2a2d2e')
        # The 9, in black vinyl.
        for x, y in ((9, 7), (10, 7), (11, 7), (9, 8), (11, 8), (9, 9), (10, 9), (11, 9), (11, 10), (11, 11), (9, 12), (10, 12), (11, 12)):
            dot(im, x, y, '#1c1d1e')
        # Where the 1 was: the tin under it never weathered.
        for y in range(7, 13):
            dot(im, 6, y, '#c4cacb')
        dot(im, 5, 8, '#c4cacb')
    return im


def mat():
    """A coir doormat: bristle, a darker border, worn pale where feet land."""
    im = new('#8a6a3c', 51884, 9)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                dot(im, x, y, shade(im.getpixel((x, y)), 7))
    for i in range(16):
        dot(im, i, 0, '#5c4526'); dot(im, i, 15, '#5c4526'); dot(im, 0, i, '#5c4526'); dot(im, 15, i, '#5c4526')
        if 0 < i < 15:
            dot(im, i, 1, '#6e5532')
    for y in range(5, 11):
        for x in range(4, 12):
            if (x + y) % 2 == 0:
                dot(im, x, y, '#a88954')
    return im


def mat_back():
    """The mat's underside, turned up when its near edge is lifted: rubber backing in a grid of nubs, and grit."""
    im = new('#36322d', 51885, 3)
    for y in range(1, 16, 3):
        for x in range(1 + (y // 3) % 2, 16, 3):
            dot(im, x, y, '#48433c')
    speckle(im, 51886, 7, ['#6a604f', '#57503f'])
    return im


def faces():
    out = {'tin': tin(False), 'tin_front': tin(True), 'mat': mat(), 'mat_back': mat_back(),
           'paling': paling(), 'paling_gate': paling_gate(), 'pool_bars': pool_bars(), 'pool_gate': pool_bars(True), 'pool_gate_locked': pool_bars(True, True),
           'roller': roller(), 'glass_clear': glass('clear'), 'glass_blinds': glass('blinds'), 'glass_painted': glass('painted'), 'frame': frame(), 'soffit': soffit(),
           'tarp': tarp(), 'tarp_pond': pond(), 'tarp_rim': pond_edge(False), 'tarp_corner': pond_edge(True), 'post': wood('#6b6052', 51878, 3, across=True), 'pool_post': metal('#2c2e30', 51879), 'stars_glow': stars_glow()}
    for layout in range(1, len(STAR_LAYOUTS)):
        out[f'stars_{layout}'] = ceiling_stars(layout); out[f'stars_glow_{layout}'] = stars_glow(layout)
    for kind in ('inner', 'garage', 'shed'):
        for upper in (0, 1):
            out[f'door_{kind}_{upper}'] = door(upper, kind)
    # 0.4.566: the inner doors' knobs, in their own brass.
    out['door_knob'] = metal('#b08a3a', 52590)
    for upper in (0, 1):
        out[f'door_garage_in_{upper}'] = door_back(upper, 'garage')
        out[f'screen_{upper}'] = screen(upper)
    return out


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/pell_{name}.png'] = im
    for name, im in faces().items():
        out[f'block/pell/{name}.png'] = im
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
    faces_ = {}
    keys = ['north', 'south', 'east', 'west', 'up', 'down']
    if 'all' in textures:
        textures = {**{k: textures['all'] for k in keys}, **{k: v for k, v in textures.items() if k != 'all'}}
    for k in keys:
        if k in textures:
            face = {'texture': '#' + textures[k]}
            if uv and k in uv:
                face['uv'] = uv[k]
            faces_[k] = face
    e = {'from': lo, 'to': hi, 'faces': faces_}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle, render='minecraft:cutout'):
    return {'ambientocclusion': False, 'render_type': render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    # The paling fence: a run across the cell; and a corner, one arm to the west and one to the north, with its post.
    m['fence_0'] = model([element([0, 0, 7], [16, 16, 9], {'north': 'p', 'south': 'p', 'east': 't', 'west': 't', 'up': 't'})], {'p': 'paling', 't': 'post'}, 'paling')
    m['fence_1'] = model([element([0, 0, 7], [9, 16, 9], {'north': 'p', 'south': 'p', 'west': 't', 'up': 't'}, uv={'north': [7, 0, 16, 16], 'south': [0, 0, 9, 16]}),
                          element([7, 0, 0], [9, 16, 7], {'east': 'p', 'west': 'p', 'north': 't', 'up': 't'}, uv={'east': [9, 0, 16, 16], 'west': [0, 0, 7, 16]}),
                          element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 't'})], {'p': 'paling', 't': 'post'}, 'paling')
    # The pool's fence: the same two shapes in black bars; its child gate shut, shut and padlocked, and swung open.
    m['pool_fence_0'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'b', 'south': 'b'})], {'b': 'pool_bars'}, 'pool_bars')
    m['pool_fence_1'] = model([element([0, 0, 7.5], [8.5, 16, 8.5], {'north': 'b', 'south': 'b'}, uv={'north': [7.5, 0, 16, 16], 'south': [0, 0, 8.5, 16]}),
                               element([7.5, 0, 0], [8.5, 16, 7.5], {'east': 'b', 'west': 'b'}, uv={'east': [8.5, 0, 16, 16], 'west': [0, 0, 7.5, 16]}),
                               element([7, 0, 7], [9, 16, 9], {'all': 't'})], {'b': 'pool_bars', 't': 'pool_post'}, 'pool_bars')
    m['pool_gate_0'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'b', 'south': 'b'})], {'b': 'pool_gate'}, 'pool_gate')
    m['pool_gate_1'] = model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'b', 'south': 'b'})], {'b': 'pool_gate_locked'}, 'pool_gate_locked')
    m['pool_gate_2'] = model([element([0, 0, 0], [1, 16, 16], {'east': 'b', 'west': 'b'})], {'b': 'pool_gate'}, 'pool_gate')
    # The side gate: shut across the passage, a finger's gap under it; and open, back against the fence.
    m['gate_0'] = model([element([0, 1, 7], [16, 16, 9], {'north': 'g', 'south': 'g', 'east': 't', 'west': 't', 'up': 't', 'down': 't'}, uv={'north': [0, 0, 16, 15], 'south': [0, 0, 16, 15]})],
                        {'g': 'paling_gate', 't': 'post'}, 'paling_gate')
    m['gate_1'] = model([element([0, 1, 0], [2, 16, 16], {'east': 'g', 'west': 'g', 'north': 't', 'south': 't', 'up': 't', 'down': 't'}, uv={'east': [0, 0, 16, 15], 'west': [0, 0, 16, 15]})],
                        {'g': 'paling_gate', 't': 'post'}, 'paling_gate')
    # Doors. Stage bit 0 open, bit 1 the upper cell.
    for name, out_tex, in_tex in (('door', 'door_inner', 'door_inner'), ('garage_door', 'door_garage', 'door_garage_in'), ('shed_door', 'door_shed', 'door_shed'), ('screen', 'screen', 'screen')):
        for stage in range(4):
            upper = stage >> 1; open_ = stage & 1
            o, i = f'{out_tex}_{upper}', f'{in_tex}_{upper}'
            thick = (7.5, 8.5) if name == 'screen' else (7, 9)
            if not open_:
                els = [element([0, 0, thick[0]], [16, 16, thick[1]], {'north': 'o', 'south': 'i', 'east': 'o', 'west': 'o', 'up': 'o', 'down': 'o'},
                               uv={'east': [0, 0, 1, 16], 'west': [15, 0, 16, 16], 'up': [0, 0, 16, 1], 'down': [0, 15, 16, 16]})]
                if name == 'door' and not upper:
                    # 0.4.566 (owner: "door handles are in the corner"): the knob was painted in the lower half's bottom corner, and
                    # on the same picture for both faces, so from one side it was on the hinge's edge. A knob of its own on each face
                    # now, at a hand's height, by the edge away from the hinge (the hinge is at x 0, where the open door lies): a
                    # round plate on the door and the knob on it.
                    for plate, knob in (((6.75, 7), (6, 6.75)), ((9, 9.25), (9.25, 10))):
                        els.append(element([12.5, 12.75, plate[0]], [14.5, 14.75, plate[1]], {'all': 'k'}))
                        els.append(element([13, 13.25, knob[0]], [14, 14.25, knob[1]], {'all': 'k'}))
            else:
                w = thick[1] - thick[0]
                els = [element([0, 0, 0], [w, 16, 16], {'east': 'i', 'west': 'o', 'north': 'o', 'south': 'o', 'up': 'o', 'down': 'o'},
                               uv={'north': [0, 0, 1, 16], 'south': [15, 0, 16, 16], 'up': [0, 0, 1, 16], 'down': [0, 0, 1, 16]})]
            m[f'{name}_{stage}'] = model(els, {'o': o, 'i': i, 'k': 'door_knob'}, o)
    m['roller_0'] = model([element([0, 0, 6], [16, 16, 10], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'})], {'r': 'roller'}, 'roller')
    # A window: an aluminium frame in the wall's thickness, and a pane. Clear; blinds down; painted over from inside. Clear glass
    # is drawn translucent: under cutout its faint tint would show as a solid pane, and the garage window has to be seen through.
    def window(pane):
        return model([element([0, 0, 6], [16, 1, 10], {'all': 'f'}), element([0, 15, 6], [16, 16, 10], {'all': 'f'}),
                      element([0, 1, 6], [1, 15, 10], {'all': 'f'}), element([15, 1, 6], [16, 15, 10], {'all': 'f'}),
                      element([7.5, 1, 7], [8.5, 15, 9], {'all': 'f'}),
                      element([1, 1, 7.8], [15, 15, 8.2], {'north': 'g', 'south': 'g'})], {'f': 'frame', 'g': pane}, 'frame',
                     'minecraft:translucent' if pane == 'glass_clear' else 'minecraft:cutout')
    m['window_0'] = window('glass_clear'); m['window_1'] = window('glass_blinds'); m['window_2'] = window('glass_painted')
    # The glass sliding door: stage bit 0 open (slid behind its fixed half, so there is nothing in the way), bit 1 the upper cell.
    for stage in range(4):
        upper = stage >> 1; open_ = stage & 1
        rail = [element([0, 15, 7], [16, 16, 9], {'all': 'f'})] if upper else [element([0, 0, 7], [16, 0.5, 9], {'all': 'f'})]
        pane = [] if open_ else [element([0, 0.5 if not upper else 0, 7.8], [16, 15 if upper else 16, 8.2], {'north': 'g', 'south': 'g'}),
                                 element([0, 0, 7.5], [1, 16, 8.5], {'all': 'f'}), element([15, 0, 7.5], [16, 16, 8.5], {'all': 'f'})]
        m[f'slider_{stage}'] = model(rail + pane, {'f': 'frame', 'g': 'glass_clear'}, 'frame', 'minecraft:translucent' if pane else 'minecraft:cutout')
    # The pool's cover, lying on the water: dry; the pond in its sag; the pond's edge, the dry side to the north as drawn; and a
    # corner of the pond, dry to the north and west. Every cell lies at the one height (0.4.529): the sheet has no sides, so a step
    # between two cells left a gap the pool's lit tiles showed through as a bright line round the pond. The sag is in the drawing.
    # Seen from under the water every cell is only the cloth.
    m['tarp_0'] = model([element([0, 13, 0], [16, 13.5, 16], {'up': 't', 'down': 't'})], {'t': 'tarp'}, 'tarp')
    for stage, name in ((1, 'tarp_pond'), (2, 'tarp_rim'), (3, 'tarp_corner')):
        m[f'tarp_{stage}'] = model([element([0, 13, 0], [16, 13.5, 16], {'up': 't', 'down': 'u'})], {'t': name, 'u': 'tarp'}, name)
    # The letterbox on its post, its front to the north as drawn; and the mat.
    m['letterbox_0'] = model([element([7, 0, 7], [9, 10, 9], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
                              element([4, 10, 5], [12, 15, 11], {'all': 't', 'north': 'f'}, uv={'north': [0, 0, 16, 16]})], {'p': 'post', 't': 'tin', 'f': 'tin_front'}, 'tin')
    m['doormat_0'] = model([element([1, 0, 3], [15, 0.6, 13], {'all': 'm'})], {'m': 'mat'}, 'mat')
    # Its near edge lifted and folded back toward the door: the line shows running on under it.
    m['doormat_1'] = model([element([1, 0, 9], [15, 0.6, 13], {'all': 'm'}),
                            element([1, 0.6, 9], [15, 1.2, 13], {'up': 'b', 'north': 'b', 'south': 'b', 'east': 'b', 'west': 'b'})], {'m': 'mat', 'b': 'mat_back'}, 'mat')
    # The house's outside wall: brick to the weather (the north face as drawn; a corner's north and west), plaster to the rooms.
    for stage, course in enumerate(('brick', 'brick_low', 'brick_new')):
        for name, out in (('wall', ('north',)), ('wall_corner', ('north', 'west'))):
            m[f'{name}_{stage}'] = {'parent': 'minecraft:block/cube', 'textures': {**{k: NS + 'block/pell_' + (course if k in out else 'plaster') for k in ('north', 'south', 'east', 'west', 'up', 'down')},
                                                                                   'particle': NS + 'block/pell_' + course}}
    # The eaves: the top half of the cell. A painted board on its edges, the soffit's lining underneath, tiles on top.
    m['eave_0'] = {'ambientocclusion': False, 'textures': {'f': NS + 'block/pell_fascia', 'c': P + 'soffit', 'r': NS + 'block/pell_roof', 'particle': NS + 'block/pell_fascia'},
                   'elements': [element([0, 8, 0], [16, 16, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'r', 'down': 'c'},
                                        uv={k: [0, 8, 16, 16] for k in ('north', 'south', 'east', 'west')})]}
    return m


KINDS = ['fence', 'pool_fence', 'pool_gate', 'gate', 'door', 'garage_door', 'shed_door', 'screen', 'roller', 'window', 'slider', 'tarp', 'letterbox', 'doormat', 'wall', 'wall_corner', 'eave']
STAGES = {'fence': 2, 'pool_fence': 2, 'pool_gate': 3, 'gate': 2, 'doormat': 2, 'door': 4, 'garage_door': 4, 'shed_door': 4, 'screen': 4, 'window': 3, 'slider': 4, 'tarp': 4, 'wall': 3, 'wall_corner': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
# Turned a quarter at random cell by cell, whatever their facing, as vanilla turns grass, so a surface with nothing running across it
# does not repeat in rows: the ground's loose surfaces, and the pool cover's dry cloth and the middle of its pond.
TURNED = ('lawn', 'lawn_dry', 'mulch', 'asphalt')
TURNED_FITTINGS = (('tarp', 0), ('tarp', 1))


def turned(model_id):
    return [{'model': model_id, **({'y': y} if y else {})} for y in (0, 90, 180, 270)]
NAMES = {'pell_fitting': 'House Fitting', 'pell_brick': 'Brick Veneer', 'pell_brick_low': 'Brick Veneer, Lowest Course', 'pell_brick_new': 'Newer Brick',
         'pell_roof': 'Roof Tiles', 'pell_fascia': 'Eaves Board', 'pell_lawn': 'Watered Lawn', 'pell_lawn_dry': 'Dry Lawn', 'pell_concrete': 'Concrete',
         'pell_concrete_print': 'Concrete, Handprint', 'pell_concrete_oil': 'Concrete, Stained', 'pell_asphalt': 'Road', 'pell_curb': 'Curb', 'pell_paver': 'Patio Pavers', 'pell_coping': 'Pool Coping', 'pell_pool_tile': 'Pool Tiles',
         'pell_pool_band': 'Pool Tiles, Waterline', 'pell_pool_band_missing': 'Pool Tiles, Waterline, One Missing', 'pell_pool_band_odd': 'Pool Tiles, Waterline, One Odd',
         'pell_pool_drain': 'Pool Drain', 'pell_deck': 'Deck Boards', 'pell_mulch': 'Bark Mulch', 'pell_iron': 'Corrugated Iron',
         'pell_plaster': 'Painted Plaster', 'pell_ceiling': 'Ceiling', 'pell_ceiling_stars': 'Ceiling, Stars', 'pell_carpet': 'Carpet', 'pell_carpet_worn': 'Carpet, Worn',
         'pell_lino': 'Lino', 'pell_lino_worn': 'Lino, Worn', 'pell_lino_burn': 'Lino, Burnt', 'pell_slab': 'Garage Slab', 'pell_slab_oil': 'Garage Slab, Stained'}


def java_names():
    """The materials as PellRegistry lists them, and the fitting's kinds as PellBlock does: the tool and the game must agree."""
    import re
    reg = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PellRegistry.java').read_text()
    blk = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/PellBlock.java').read_text()
    names = re.search(r'MATERIAL_NAMES=java\.util\.List\.of\(([^;]*?)\);', reg, re.S).group(1)
    kinds = re.search(r'enum Kind implements StringRepresentable \{\s*([A-Z_,\s]+);', blk).group(1)
    return re.findall(r'"([a-z_]+)"', names), [k.strip().lower() for k in kinds.split(',') if k.strip()]


def files():
    out = {}
    for name in materials():
        out[f'models/block/pell_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/pell_{name}'}}
        model_id = NS + f'block/pell_{name}'
        out[f'blockstates/pell_{name}.json'] = {'variants': {'': turned(model_id) if name in TURNED else {'model': model_id}}}
    # The stars show in the dark: the ceiling, and under it a sheet of the stars alone that lights itself.
    cube = {k: {'texture': '#all', 'cullface': k} for k in ('north', 'south', 'east', 'west', 'up', 'down')}
    stars = []
    for layout in range(len(STAR_LAYOUTS)):
        name = 'pell_ceiling_stars' + (f'_{layout}' if layout else '')
        day, glow = (NS + 'block/pell_ceiling_stars', P + 'stars_glow') if not layout else (P + f'stars_{layout}', P + f'stars_glow_{layout}')
        out[f'models/block/{name}.json'] = {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout',
            'textures': {'all': day, 'glow': glow, 'particle': day},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': cube},
                         {'from': [0, -0.05, 0], 'to': [16, -0.05, 16], 'shade': False, 'faces': {'down': {'texture': '#glow'}},
                          'neoforge_data': {'block_light': 15, 'sky_light': 15}}]}
        for y in (0, 90, 180, 270):
            stars.append({'model': NS + f'block/{name}', **({'y': y} if y else {})})
    out['blockstates/pell_ceiling_stars.json'] = {'variants': {'': stars}}
    for key, obj in fitting_models().items():
        out[f'models/block/pell/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/pell/{kind}_{drawn}'}
                if (kind, drawn) in TURNED_FITTINGS:
                    v = turned(v['model'])
                elif y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pell_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    mats, kinds = java_names()
    assert mats == list(materials()), (mats, list(materials()))
    assert kinds == KINDS, (kinds, KINDS)
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert tex.replace(NS, '') + '.png' in images, (key, tex)
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
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    assert set(NAMES) == {'pell_fitting'} | {f'pell_{n}' for n in materials()}, sorted(set(NAMES) ^ ({'pell_fitting'} | {f'pell_{n}' for n in materials()}))
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the Pell house: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
