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
                        px[x, y] = shade(face, tone + rnd.randint(-3, 3))
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
    """Concrete roof tiles in courses, each lapping the one below, their noses in shadow."""
    im = new('#8f5b49', 51806, 5)
    for y in (3, 7, 11, 15):
        fill(im, 0, y, 16, y + 1, '#5e392e')
    for row, y in enumerate((0, 4, 8, 12)):
        for x in range(0 if row % 2 == 0 else 4, 16, 8):
            fill(im, x, y, x + 1, y + 3, '#774a3b')
    speckle(im, 51807, 9, ['#a16a57', '#7f5041'])
    return im


def fascia():
    im = new('#e7e0cf', 51808, 3)
    fill(im, 0, 15, 16, 16, '#b9b09c'); fill(im, 0, 0, 16, 1, '#f3eee2')
    for x, y in ((3, 6), (4, 7), (11, 9), (12, 9)):
        dot(im, x, y, '#cfc6b2')
    return im


def lawn(dry):
    base = '#b59c5e' if dry else '#5c9a3a'
    im = new(base, 51810 + dry, 7); rnd = random.Random(51812 + dry)
    blades = ['#c9b272', '#9d8449', '#a9925a'] if dry else ['#74b24b', '#4b8530', '#67a640']
    for _ in range(34):
        x, y = rnd.randrange(16), rnd.randrange(15)
        c = rnd.choice(blades); dot(im, x, y, c); dot(im, x, y + 1, c)
    if dry:
        for _ in range(7):
            dot(im, rnd.randrange(16), rnd.randrange(16), '#8a7446')
    return im


def concrete(seed=51814, joint=True):
    im = new('#b7b3a8', seed, 4); speckle(im, seed + 1, 12, ['#a5a196', '#c6c2b8', '#9c988e'])
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
    im = new('#3d3d40', 51816, 5); speckle(im, 51817, 22, ['#55555a', '#2f2f32', '#646468'])
    return im


def kerb():
    im = new('#a19f98', 51818, 3)
    fill(im, 0, 0, 16, 1, '#c2c0ba'); fill(im, 0, 15, 16, 16, '#7f7d77'); fill(im, 7, 0, 8, 16, '#8a8882')
    return im


def paver():
    im = new('#c7ab8b', 51820, 5)
    for i in range(16):
        dot(im, i, 0, '#96806a'); dot(im, i, 8, '#96806a'); dot(im, 0, i, '#96806a'); dot(im, 8, i, '#96806a')
    speckle(im, 51821, 8, ['#b39878', '#d6bd9f'])
    return im


def coping():
    im = new('#dcd5c4', 51822, 3)
    fill(im, 0, 0, 16, 2, '#efeadd'); fill(im, 0, 14, 16, 16, '#bdb5a2')
    for x in (0, 8):
        fill(im, x, 0, x + 1, 16, '#b3ab98')
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
    if band:
        for tx in range(4):
            fill(im, tx * 4, 0, tx * 4 + 3, 3, '#1f5f8c' if tx % 2 == 0 else '#2c79a6')
            fill(im, tx * 4, 4, tx * 4 + 3, 7, '#bfe3ea')
    return im


def deck():
    im = new('#8d6f4f', 51826, 4)
    for y in (3, 7, 11, 15):
        fill(im, 0, y, 16, y + 1, '#4f3d2b')
    for x, y in ((2, 1), (13, 1), (6, 5), (10, 9), (3, 13), (12, 13)):
        dot(im, x, y, '#3d3024')
    speckle(im, 51827, 9, ['#9c7d5b', '#7c6145'])
    return im


def mulch():
    im = new('#5b3f2c', 51828, 8); rnd = random.Random(51829)
    for _ in range(20):
        x, y = rnd.randrange(15), rnd.randrange(16); c = rnd.choice(['#77543a', '#463020', '#8a6444'])
        dot(im, x, y, c); dot(im, x + 1, y, c)
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
    im = new('#e9e3d4', 51832, 2); speckle(im, 51833, 8, ['#dfd8c7', '#f0ebdf'])
    return im


def ceiling():
    im = new('#f1eee6', 51834, 2); speckle(im, 51835, 14, ['#e6e2d8'])
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
    im = new('#8b806d', 51836, 6); rnd = random.Random(51837)
    for _ in range(40):
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
    im = new('#d2caa9', 51838, 3)
    for i in range(16):
        dot(im, i, i, '#b9b08c'); dot(im, 15 - i, i, '#b9b08c')
    for x, y in ((0, 8), (8, 0), (15, 8), (8, 15), (8, 8)):
        dot(im, x, y, '#a89f7c')
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
    im = new('#a4a097', 51840, 4); speckle(im, 51841, 16, ['#948f86', '#b3afa6'])
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
            'coping': coping(), 'pool_tile': pool_tile(), 'pool_band': pool_tile(True), 'deck': deck(), 'mulch': mulch(), 'iron': iron(),
            'plaster': plaster(), 'ceiling': ceiling(), 'ceiling_stars': ceiling_stars(), 'carpet': carpet(False), 'carpet_worn': carpet(True), 'lino': lino(),
            'lino_worn': lino_worn(), 'lino_burn': lino_burn(),
            'slab': slab(False), 'slab_oil': slab(True)}


def like(name):
    """What each material is like to walk on and to hear (PellRegistry reads this table through its own switch; the tool checks both agree)."""
    return {'lawn': 'grass', 'lawn_dry': 'grass', 'mulch': 'grass', 'deck': 'wood', 'carpet': 'wool', 'carpet_worn': 'wool', 'iron': 'metal'}.get(name, 'stone')


# ------------------------------------------------------------------------------------------------ fittings' faces
def paling():
    """A paling fence: boards four pixels wide with a gap of shadow between, grey with weather, a rail's nails across."""
    im = new('#9a8c78', 51850, 4)
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
    fill(im, 0, 1, 16, 2, '#34373a'); fill(im, 0, 13, 16, 14, '#34373a')
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
    im = new(base, 51860 + upper + (5 if kind == 'garage' else 0), 2)
    fill(im, 0, 0, 1, 16, shade(base, -34)); fill(im, 15, 0, 16, 16, shade(base, -34))
    if upper:
        fill(im, 0, 0, 16, 1, shade(base, -34))
    if kind == 'inner':
        fill(im, 3, 2 if upper else 1, 13, 14 if upper else 13, shade(base, -9)); fill(im, 4, 3 if upper else 2, 12, 13 if upper else 12, shade(base, 3))
        if not upper:
            fill(im, 12, 12, 14, 14, '#b8a25a'); dot(im, 12, 12, '#e3d08c')
    else:
        for y in (3, 12):
            fill(im, 1, y, 15, y + 2, shade(base, -22))
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
    im = new('#cfd3cf', 51870, 2)
    for y in range(1, 16, 2):
        fill(im, 0, y, 16, y + 1, '#aab0ab')
    fill(im, 6, 13, 10, 14, '#55595c')
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
        fill(im, 0, 0, 16, 16, '#d9d2bd')
        for y in range(1, 16, 2):
            fill(im, 0, y, 16, y + 1, '#b3ab93')
        fill(im, 12, 0, 13, 16, '#8f886f')
    else:
        im = new('#f1efe8', 51872, 3)
        rnd = random.Random(51873)
        for _ in range(12):
            x, y = rnd.randrange(14), rnd.randrange(16)
            fill(im, x, y, x + 3, y + 1, '#e3e0d6')
        fill(im, 15, 3, 16, 9, '#7d99a3', 190)
    return im


def frame():
    im = new('#c9ccc8', 51874, 2); fill(im, 0, 0, 16, 1, '#e4e6e2'); fill(im, 0, 15, 16, 16, '#9a9e9a')
    return im


def tarp(pond):
    """The pool's cover: a blue tarpaulin gone grey along its folds. Where it sags, rainwater and leaves."""
    im = new('#4f6f96', 51876, 4)
    for i in range(16):
        dot(im, i, (i * 3) % 16, '#7f96b3'); dot(im, (i * 5 + 2) % 16, i, '#3d5878')
    fill(im, 0, 7, 16, 8, '#6a84a6')
    if pond:
        for y in range(2, 14):
            for x in range(2, 14):
                d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
                if d < 5.6:
                    dot(im, x, y, '#2f3f38' if d < 4.4 else '#3f5650')
        for x, y, c in ((5, 6, '#8a6a2c'), (6, 6, '#8a6a2c'), (9, 9, '#6f5220'), (10, 8, '#9a7a34'), (7, 10, '#5a6a2a'), (4, 9, '#6f5220')):
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
    for i in range(16):
        dot(im, i, 0, '#5c4526'); dot(im, i, 15, '#5c4526'); dot(im, 0, i, '#5c4526'); dot(im, 15, i, '#5c4526')
    for y in range(5, 11):
        for x in range(4, 12):
            if (x + y) % 2 == 0:
                dot(im, x, y, '#a88954')
    return im


def faces():
    out = {'tin': tin(False), 'tin_front': tin(True), 'mat': mat(),
           'paling': paling(), 'paling_gate': paling_gate(), 'pool_bars': pool_bars(), 'pool_gate': pool_bars(True), 'pool_gate_locked': pool_bars(True, True),
           'roller': roller(), 'glass_clear': glass('clear'), 'glass_blinds': glass('blinds'), 'glass_painted': glass('painted'), 'frame': frame(),
           'tarp': tarp(False), 'tarp_pond': tarp(True), 'post': new('#6b6052', 51878, 3), 'pool_post': new('#2c2e30', 51879, 2), 'stars_glow': stars_glow()}
    for layout in range(1, len(STAR_LAYOUTS)):
        out[f'stars_{layout}'] = ceiling_stars(layout); out[f'stars_glow_{layout}'] = stars_glow(layout)
    for kind in ('inner', 'garage', 'shed'):
        for upper in (0, 1):
            out[f'door_{kind}_{upper}'] = door(upper, kind)
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
            else:
                w = thick[1] - thick[0]
                els = [element([0, 0, 0], [w, 16, 16], {'east': 'i', 'west': 'o', 'north': 'o', 'south': 'o', 'up': 'o', 'down': 'o'},
                               uv={'north': [0, 0, 1, 16], 'south': [15, 0, 16, 16], 'up': [0, 0, 1, 16], 'down': [0, 0, 1, 16]})]
            m[f'{name}_{stage}'] = model(els, {'o': o, 'i': i}, o)
    m['roller_0'] = model([element([0, 0, 6], [16, 16, 10], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'})], {'r': 'roller'}, 'roller')
    # A window: an aluminium frame in the wall's thickness, and a pane. Clear; blinds down; painted over from inside.
    def window(pane):
        return model([element([0, 0, 6], [16, 1, 10], {'all': 'f'}), element([0, 15, 6], [16, 16, 10], {'all': 'f'}),
                      element([0, 1, 6], [1, 15, 10], {'all': 'f'}), element([15, 1, 6], [16, 15, 10], {'all': 'f'}),
                      element([7.5, 1, 7], [8.5, 15, 9], {'all': 'f'}),
                      element([1, 1, 7.8], [15, 15, 8.2], {'north': 'g', 'south': 'g'})], {'f': 'frame', 'g': pane}, 'frame')
    m['window_0'] = window('glass_clear'); m['window_1'] = window('glass_blinds'); m['window_2'] = window('glass_painted')
    # The glass sliding door: stage bit 0 open (slid behind its fixed half, so there is nothing in the way), bit 1 the upper cell.
    for stage in range(4):
        upper = stage >> 1; open_ = stage & 1
        rail = [element([0, 15, 7], [16, 16, 9], {'all': 'f'})] if upper else [element([0, 0, 7], [16, 0.5, 9], {'all': 'f'})]
        pane = [] if open_ else [element([0, 0.5 if not upper else 0, 7.8], [16, 15 if upper else 16, 8.2], {'north': 'g', 'south': 'g'}),
                                 element([0, 0, 7.5], [1, 16, 8.5], {'all': 'f'}), element([15, 0, 7.5], [16, 16, 8.5], {'all': 'f'})]
        m[f'slider_{stage}'] = model(rail + pane, {'f': 'frame', 'g': 'glass_clear'}, 'frame')
    # The pool's cover, lying on the water: dry; and holding a pond.
    m['tarp_0'] = model([element([0, 13, 0], [16, 13.5, 16], {'up': 't', 'down': 't'})], {'t': 'tarp'}, 'tarp')
    m['tarp_1'] = model([element([0, 12, 0], [16, 12.5, 16], {'up': 't', 'down': 't'})], {'t': 'tarp_pond'}, 'tarp_pond')
    # The letterbox on its post, its front to the north as drawn; and the mat.
    m['letterbox_0'] = model([element([7, 0, 7], [9, 10, 9], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}),
                              element([4, 10, 5], [12, 15, 11], {'all': 't', 'north': 'f'}, uv={'north': [0, 0, 16, 16]})], {'p': 'post', 't': 'tin', 'f': 'tin_front'}, 'tin')
    m['doormat_0'] = model([element([1, 0, 3], [15, 0.6, 13], {'all': 'm'})], {'m': 'mat'}, 'mat')
    # The house's outside wall: brick to the weather (the north face as drawn; a corner's north and west), plaster to the rooms.
    for stage, course in enumerate(('brick', 'brick_low', 'brick_new')):
        for name, out in (('wall', ('north',)), ('wall_corner', ('north', 'west'))):
            m[f'{name}_{stage}'] = {'parent': 'minecraft:block/cube', 'textures': {**{k: NS + 'block/pell_' + (course if k in out else 'plaster') for k in ('north', 'south', 'east', 'west', 'up', 'down')},
                                                                                   'particle': NS + 'block/pell_' + course}}
    # The eaves: the top half of the cell. A painted board on its edges, the soffit underneath, tiles on top.
    m['eave_0'] = {'ambientocclusion': False, 'textures': {'f': NS + 'block/pell_fascia', 'c': NS + 'block/pell_ceiling', 'r': NS + 'block/pell_roof', 'particle': NS + 'block/pell_fascia'},
                   'elements': [element([0, 8, 0], [16, 16, 16], {'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'r', 'down': 'c'},
                                        uv={k: [0, 8, 16, 16] for k in ('north', 'south', 'east', 'west')})]}
    return m


KINDS = ['fence', 'pool_fence', 'pool_gate', 'gate', 'door', 'garage_door', 'shed_door', 'screen', 'roller', 'window', 'slider', 'tarp', 'letterbox', 'doormat', 'wall', 'wall_corner', 'eave']
STAGES = {'fence': 2, 'pool_fence': 2, 'pool_gate': 3, 'gate': 2, 'door': 4, 'garage_door': 4, 'shed_door': 4, 'screen': 4, 'window': 3, 'slider': 4, 'tarp': 2, 'wall': 3, 'wall_corner': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'pell_fitting': 'House Fitting', 'pell_brick': 'Brick Veneer', 'pell_brick_low': 'Brick Veneer, Lowest Course', 'pell_brick_new': 'Newer Brick',
         'pell_roof': 'Roof Tiles', 'pell_fascia': 'Eaves Board', 'pell_lawn': 'Watered Lawn', 'pell_lawn_dry': 'Dry Lawn', 'pell_concrete': 'Concrete',
         'pell_concrete_print': 'Concrete, Handprint', 'pell_concrete_oil': 'Concrete, Stained', 'pell_asphalt': 'Road', 'pell_curb': 'Curb', 'pell_paver': 'Patio Pavers', 'pell_coping': 'Pool Coping', 'pell_pool_tile': 'Pool Tiles',
         'pell_pool_band': 'Pool Tiles, Waterline', 'pell_deck': 'Deck Boards', 'pell_mulch': 'Bark Mulch', 'pell_iron': 'Corrugated Iron',
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
        out[f'blockstates/pell_{name}.json'] = {'variants': {'': {'model': NS + f'block/pell_{name}'}}}
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
                if y:
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
