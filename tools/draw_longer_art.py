"""A Little Longer (0.4.621): Rowan's radio repair shop, the flat above it and its roof, at night, in the rain.

Draws, in native Minecraft pixels from fixed seeds: seventeen materials (the soot brick and the parapet's coping, the roof's felt,
the shop's plaster and floorboards, the flat's paper and boards, the bathroom's small tiles, the ceilings, the shopfront's painted
timber, the pavement and the wet road, the House's own corridor paper and runner, and the brick of the buildings round the yard with
their lit and their dark windows), and every fitting of the longer_fitting block, its models facing north as drawn and turned by its
facing: the workbench in three parts; the parts trays; the mended lamp, off and lit; the radio, off, playing, and with its back off;
the twice-mended chair; the work order on its spike; the parts shelf with three loads; the shop counter; the kettle, cold and on the
boil; the two cups together, the cracked one alone and the whole one alone; the toaster up and down; the kitchen table; the armchair;
the kitchen chair; the coat hook with the coat and without; the rain window's two halves (the rain an animated strip); the worktop and
the worktop with its sink; the basin; the lavatory; the roof bench; the awning; the water tank. Writes the models, the block states and
the blocks' names. --check verifies everything, and fails if the game's lists of materials and kinds are not its own.
"""
from pathlib import Path
import argparse
import json
import random
import re
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth'
NS = 'the_oldest_house:'
P = NS + 'block/longer/'
RAIN_FRAMES = 8


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(base=None, seed=0, spread=4, h=16):
    im = Image.new('RGBA', (16, h), (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(h):
            for x in range(16):
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


# ------------------------------------------------------------------------------------------------ materials
def brick(base='#6e4a3c', mortar='#4a3e38', seed=6210, soot=True):
    # Four-pixel courses, bonds staggered by half a brick, mortar a pixel; soot gathered towards the top of each course.
    im = new(base=base, seed=seed, spread=5); rnd = random.Random(seed + 1)
    for y in range(16):
        course = y // 4; off = 0 if course % 2 == 0 else 4
        for x in range(16):
            if y % 4 == 3 or (x - off) % 8 == 7:
                dot(im, x, y, shade(mortar, rnd.randint(-3, 3)))
            elif soot and y % 4 == 0 and rnd.random() < .5:
                dot(im, x, y, shade(base, -14 + rnd.randint(-3, 3)))
    for _ in range(6):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if y % 4 != 3:
            dot(im, x, y, shade(base, 12))
    return im


def coping():
    im = new(base='#8c8a84', seed=6212, spread=4); rnd = random.Random(6213)
    for x in range(16):
        dot(im, x, 0, '#a6a49e'); dot(im, x, 15, '#64625e')
    for x in (7,):
        for y in range(16):
            dot(im, x, y, '#6e6c66')
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#5a6a4a', rnd.randint(-6, 6)))
    return im


def felt():
    # Roofing felt laid in strips, each lap a dark line with a lighter lip, grit all over, a wet sheen in the hollows.
    im = new(base='#36383a', seed=6214, spread=6); rnd = random.Random(6215)
    for y in (4, 11):
        for x in range(16):
            dot(im, x, y, '#202224'); dot(im, x, y + 1, '#4a4c4e')
    for _ in range(22):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#5a5c5e', rnd.randint(-6, 6)))
    for x in range(2, 9):
        dot(im, x, 8, '#4e565e')
    return im


def plaster():
    im = new(base='#d6cdb8', seed=6216, spread=3); rnd = random.Random(6217)
    for _ in range(5):
        cx, cy = rnd.randrange(16), rnd.randrange(16)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            dot(im, cx + dx, cy + dy, '#c4b89e')
    for y in range(9, 14):
        dot(im, 12, y, '#bfb398')
    return im


def boards(base='#7a5434', joint='#432c1a', width=4, seed=6218):
    im = new(base=base, seed=seed, spread=4); rnd = random.Random(seed + 1)
    for x in range(0, 16, width):
        for y in range(16):
            dot(im, x, y, joint)
    for col in range(0, 16, width):
        end = rnd.randrange(16)
        for xx in range(col + 1, min(16, col + width)):
            dot(im, xx, end, joint)
        dot(im, col + 1, (end + 3) % 16, '#2a1c10'); dot(im, col + width - 1, (end + 3) % 16, '#2a1c10')
    for _ in range(12):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if x % width:
            dot(im, x, y, shade(base, -12))
    # Wear down the middle of each board, where feet go.
    for col in range(0, 16, width):
        for y in range(16):
            if rnd.random() < .3:
                dot(im, col + width // 2, y, shade(base, 10))
    return im


def paper():
    # The flat's paper: a small trellis of fine lines with a leaf at each crossing, faded and a little marked.
    im = new(base='#cdbf9e', seed=6220, spread=2); rnd = random.Random(6221)
    for i in range(16):
        dot(im, i, i, '#b4a482'); dot(im, i, 15 - i, '#b4a482')
        dot(im, (i + 8) % 16, i, '#b4a482'); dot(im, (23 - i) % 16, i, '#b4a482')
    for cx, cy in ((0, 0), (8, 8), (0, 8), (8, 0)):
        dot(im, cx, cy, '#7a7e58'); dot(im, (cx + 1) % 16, cy, '#8a8e66')
    for _ in range(4):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#bcae8c')
    return im


def tile():
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(6222)
    for y in range(16):
        for x in range(16):
            px[x, y] = shade('#a8a8a2', rnd.randint(-2, 2)) if x % 4 == 3 or y % 4 == 3 else shade('#e8e6de', rnd.randint(-3, 3))
    for x, y in ((1, 1), (5, 9), (9, 5)):
        dot(im, x, y, '#f6f4ee')
    return im


def seam():
    # 0.4.629, the cut: a strip of depth that does not agree with the room. Each block is a little corridor seen end on, its edges
    # running in to a point that is too far away for a wall a block thick; nearly black, the far end the darkest.
    im = Image.new('RGBA', (16, 16)); px = im.load()
    rings = ['#2c2926', '#24221f', '#1d1b19', '#171614', '#121110', '#0e0d0c', '#0a0a09', '#070706']
    for y in range(16):
        for x in range(16):
            d = min(x, y, 15 - x, 15 - y)
            px[x, y] = shade(rings[min(d, 7)], 0)
    for i in range(8):
        for p in ((i, i), (15 - i, i), (i, 15 - i), (15 - i, 15 - i)):
            dot(im, p[0], p[1], '#34302c' if i < 3 else '#1a1816')
    return im


def ceiling():
    im = new(base='#d8d2c2', seed=6223, spread=2)
    for i, x in enumerate(range(3, 12)):
        dot(im, x, 6 + (i % 3 == 0), '#c4bca8')
    return im


def shopfront():
    # Painted timber, chipped at the edges, a beading line and a panel recess.
    im = new(base='#2e4a40', seed=6224, spread=3); rnd = random.Random(6225)
    frame(im, 2, 2, 14, 14, '#223830'); frame(im, 3, 3, 13, 13, '#3c5c50')
    for _ in range(8):
        x, y = rnd.choice([(rnd.randrange(16), rnd.choice((0, 15))), (rnd.choice((0, 15)), rnd.randrange(16))])
        dot(im, x, y, '#8a6a4a')
    return im


def pavement():
    im = new(base='#6c6a66', seed=6226, spread=4); rnd = random.Random(6227)
    for i in range(16):
        dot(im, i, 7, '#4a4844'); dot(im, 7, i, '#4a4844'); dot(im, i, 15, '#4a4844'); dot(im, 15, i, '#4a4844')
    for x, y in ((2, 3), (3, 4), (4, 4), (10, 11), (11, 12)):
        dot(im, x, y, '#52504c')
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#7e7c78')
    return im


def road():
    im = new(base='#2c2e30', seed=6228, spread=5); rnd = random.Random(6229)
    for _ in range(24):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#4a4c50', rnd.randint(-6, 6)))
    for x in range(4, 12):
        dot(im, x, 9, '#3e4652'); dot(im, x + 1, 10, '#36404c')
    return im


def corridor():
    # The House's own corridor paper: a dull damask on a ground the age of the hall, darker towards the foot.
    im = new(base='#8e8466', seed=6230, spread=2)
    motif = ['.....x..', '....xxx.', '...x.x.x', '....xxx.', '.....x..', '........', '........', '........']
    for gy in range(2):
        for gx in range(2):
            for y, row in enumerate(motif):
                for x, ch in enumerate(row):
                    if ch == 'x':
                        dot(im, gx * 8 + x - 1, gy * 8 + y + (gx * 4) % 8, '#7a7056')
    for y in range(13, 16):
        for x in range(16):
            dot(im, x, y, shade('#8e8466', -6 - (y - 13) * 3))
    return im


def runner():
    im = new(base='#5a2e2a', seed=6232, spread=3)
    for y in range(16):
        dot(im, 0, y, '#3a1e1c'); dot(im, 15, y, '#3a1e1c'); dot(im, 2, y, '#8a6a40'); dot(im, 13, y, '#8a6a40')
    for y in range(0, 16, 4):
        for x in range(5, 11):
            if (x + y) % 3 == 0:
                dot(im, x, y + 1, '#7a4a36')
    return im


def facade(lit=None):
    im = brick(base='#3e3430', mortar='#2a2422', seed=6234 + (0 if lit is None else (2 if lit else 3)), soot=True)
    if lit is None:
        return im
    # A sash window: stone head and sill, the frame, two panes; lit warm with a curtain drawn half across, or dark with a smear of
    # streetlight on the glass.
    fill(im, 2, 1, 14, 2, '#6a6862'); fill(im, 2, 14, 14, 15, '#6a6862')
    fill(im, 3, 2, 13, 14, '#2a2826')
    for y in range(3, 13):
        for x in range(4, 12):
            if lit:
                c = '#e6b65e' if y < 8 else '#d6a04c'
                if x >= 9:
                    c = '#8a5a3a'
                dot(im, x, y, shade(c, (x * 7 + y * 3) % 5 - 2))
            else:
                dot(im, x, y, '#1a1e24')
    for x in range(4, 12):
        dot(im, x, 8, '#3a3632')
    if not lit:
        for i in range(3):
            dot(im, 5 + i, 4 + i, '#3e4a58')
    return im


def materials():
    return {'brick': brick(), 'coping': coping(), 'felt': felt(), 'plaster': plaster(), 'boards': boards(),
            'paper': paper(), 'flatboards': boards(base='#a07a50', joint='#5e4228', width=5, seed=6240), 'tile': tile(),
            'ceiling': ceiling(), 'shopfront': shopfront(), 'pavement': pavement(), 'road': road(), 'corridor': corridor(),
            'runner': runner(), 'facade': facade(), 'facade_lit': facade(True), 'facade_dark': facade(False), 'seam': seam()}


# ------------------------------------------------------------------------------------------------ fitting textures
def grain(base, seed, lines=3):
    im = new(base=base, seed=seed, spread=3); rnd = random.Random(seed + 1)
    for _ in range(lines):
        y = rnd.randrange(16)
        for x in range(16):
            if rnd.random() < .7:
                dot(im, x, y, shade(base, -14))
    return im


def benchtop():
    # The workbench's top: scarred boards, a burn from the iron, a ring from a cup, a pencil line.
    im = boards(base='#8a6a46', joint='#5a4028', width=5, seed=6250)
    for x, y in ((3, 4), (4, 4), (4, 5)):
        dot(im, x, y, '#2a1c12')
    for a in range(16):
        import math
        dot(im, 11 + round(2.2 * math.cos(a * math.pi / 8)), 10 + round(2.2 * math.sin(a * math.pi / 8)), '#6a4c30')
    for x in range(2, 10):
        dot(im, x, 13, '#5a5a5e')
    return im


def drawers():
    im = grain('#6e4e30', 6252)
    for y0 in (2, 9):
        frame(im, 1, y0, 15, y0 + 6, '#4a3220'); fill(im, 7, y0 + 2, 9, y0 + 3, '#b89a5a')
    return im


def bakelite():
    im = new(base='#4a2e22', seed=6254, spread=3)
    for y in (3, 9, 14):
        for x in range(16):
            dot(im, x, y, '#5e3c2c')
    return im


def radio_face(on):
    im = new(base='#5a3a26', seed=6256, spread=3)
    # The speaker cloth behind a fretwork grille, the glass dial with its stations, two knobs.
    fill(im, 1, 1, 9, 13, '#8a7a5a')
    for y in range(2, 12):
        for x in range(2, 8):
            if (x + y) % 2 == 0:
                dot(im, x, y, '#6a5a40')
    for x in range(1, 9):
        dot(im, x, 6, '#3a2618')
    fill(im, 10, 2, 15, 8, '#e8d49a' if on else '#b8a87a')
    for x in range(10, 15):
        dot(im, x, 4, '#6a5a3a')
    dot(im, 12 if on else 11, 3, '#9a2a20'); dot(im, 12 if on else 11, 5, '#9a2a20')
    fill(im, 10, 10, 12, 12, '#2a1c14'); fill(im, 13, 10, 15, 12, '#2a1c14')
    dot(im, 10, 10, '#6a5a4a'); dot(im, 13, 10, '#6a5a4a')
    return im


def radio_back():
    # The open back: chassis, three valves in a row, a coil, a tangle of wire, and the hole where one screw should be.
    im = new(base='#8a8a86', seed=6258, spread=3)
    frame(im, 0, 0, 16, 16, '#4a2e22')
    for x0 in (2, 6, 10):
        fill(im, x0, 3, x0 + 3, 9, '#cfd6d8'); fill(im, x0 + 1, 4, x0 + 2, 8, '#e8a85a'); fill(im, x0, 9, x0 + 3, 10, '#2a2a2a')
    fill(im, 3, 11, 6, 14, '#b8742a'); fill(im, 9, 12, 14, 13, '#9a2a2a'); dot(im, 8, 11, '#2a5a2a'); dot(im, 9, 11, '#2a5a2a')
    dot(im, 14, 2, '#1a1a1a')
    return im


def shade_tex():
    # The lamp's shade: parchment-coloured fabric with a seam and a strip of tape where it was mended.
    im = new(base='#d8c49a', seed=6260, spread=3)
    for y in range(16):
        dot(im, 7, y, '#c4ae84')
    fill(im, 10, 4, 14, 7, '#e6dcc4')
    for x in range(10, 14):
        dot(im, x, 4, '#c8bea6')
    return im


def shade_lit():
    im = shade_tex(); px = im.load()
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            px[x, y] = (min(255, r + 26), min(255, g + 20), min(255, b + 4), a)
    return im


def trays():
    # Compartments, each with its parts: valves, knobs, screws, washers, a coil of wire, and "odd".
    im = new(base='#5a5e60', seed=6262, spread=2)
    for x in (0, 5, 10, 15):
        for y in range(16):
            dot(im, x, y, '#3a3e40')
    for y in (0, 8, 15):
        for x in range(16):
            dot(im, x, y, '#3a3e40')
    rnd = random.Random(6263)
    cells = [(1, 1), (6, 1), (11, 1), (1, 9), (6, 9), (11, 9)]
    colours = ['#d0d6d8', '#2a1c14', '#c8c8c0', '#b8742a', '#9a6a3a', '#a8a8a0']
    for (cx, cy), c in zip(cells, colours):
        for _ in range(6):
            dot(im, cx + rnd.randrange(4), cy + rnd.randrange(6), shade(c, rnd.randint(-10, 10)))
    return im


def order():
    # The work order on its spike: a carbon-backed form, lines ruled, pencil in the boxes.
    im = new(base='#ece4cc', seed=6264, spread=2)
    for y in (2, 5, 8, 11, 14):
        for x in range(1, 15):
            dot(im, x, y, '#b8ae96')
    for x in range(1, 7):
        dot(im, x, 1, '#7a2a24')
    rnd = random.Random(6265)
    for y in (4, 7, 10, 13):
        for x in range(2, 2 + rnd.randrange(6, 12)):
            if rnd.random() < .7:
                dot(im, x, y, '#5a5a62')
    fill(im, 7, 0, 9, 2, '#6a6a70')
    return im


def shelf_load(i):
    # Sets waiting their turn: cabinets of different woods and sizes, a tag on each dial.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(6266 + i)
    fill(im, 0, 0, 16, 16, '#3a2a1c')
    x = 0
    while x < 16:
        w = rnd.choice((5, 6, 7)); h = rnd.choice((8, 10, 12))
        base = rnd.choice(('#6a4a2e', '#4a2e22', '#8a6a46', '#2a2a2a', '#7a7468'))
        fill(im, x, 16 - h, min(16, x + w - 1), 16, base)
        fill(im, x + 1, 16 - h + 2, min(16, x + w - 3), 16 - h + 5, '#c8b88a')
        dot(im, x + w - 3, 16 - h + 3, '#ece4cc'); dot(im, x + w - 3, 16 - h + 4, '#ece4cc')
        x += w
    for x in range(16):
        dot(im, x, 15, '#2a1e14')
    return im


def counter_front():
    im = grain('#5e4430', 6270)
    frame(im, 1, 1, 15, 15, '#3e2c1e'); frame(im, 3, 3, 13, 13, '#7a5a40')
    return im


def counter_top():
    im = boards(base='#7a5a3c', joint='#4a3420', width=8, seed=6272)
    # Pale oval where elbows go.
    for y in range(5, 11):
        for x in range(4, 12):
            if ((x - 7.5) / 4.2) ** 2 + ((y - 7.5) / 3) ** 2 < 1:
                dot(im, x, y, '#94744e')
    return im


def kettle():
    im = new(base='#c8c4b4', seed=6274, spread=2)
    for x in range(16):
        dot(im, x, 1, '#9a968a'); dot(im, x, 14, '#8a867a')
    fill(im, 5, 6, 11, 10, '#5a3a2a')
    for x in range(3, 13, 3):
        dot(im, x, 12, '#a8a498')
    return im


def cup(cracked):
    im = new(base='#e8e2d4' if not cracked else '#d4dce0', seed=6276 + cracked, spread=2)
    for x in range(16):
        dot(im, x, 3, '#6a8aa0' if cracked else '#9a6a4a')
    if cracked:
        for y, x in ((4, 7), (5, 8), (6, 8), (7, 9), (8, 9), (9, 8)):
            dot(im, x, y, '#8a8476')
    return im


def tea():
    im = new(base='#6a4226', seed=6278, spread=3)
    dot(im, 5, 5, '#8a5a36'); dot(im, 10, 9, '#8a5a36')
    return im


def chrome():
    im = new(base='#b4b8bc', seed=6280, spread=3)
    for y in range(16):
        dot(im, 3, y, '#e6eaee'); dot(im, 12, y, '#7a7e82')
    return im


def toaster_top():
    im = chrome()
    fill(im, 3, 3, 13, 5, '#1e1e20'); fill(im, 3, 10, 13, 12, '#1e1e20')
    return im


def fabric(base, seed):
    im = new(base=base, seed=seed, spread=4)
    for y in range(0, 16, 2):
        for x in range((y // 2) % 2, 16, 2):
            dot(im, x, y, shade(base, -8))
    return im


def armchair():
    im = fabric('#6a5a3a', 6282)
    # Worn on the arms and the seat front.
    for x in range(16):
        if x % 3:
            dot(im, x, 1, '#8a7a5a')
    return im


def coat():
    im = new(base='#4a4640', seed=6284, spread=3)
    for y in range(16):
        dot(im, 7, y, '#36322e'); dot(im, 8, y, '#5a5650')
    for y in (3, 7, 11):
        dot(im, 9, y, '#2a2620')
    fill(im, 2, 9, 6, 12, '#3a3630')
    return im


def iron():
    im = new(base='#3a3a3c', seed=6286, spread=3)
    return im


def rain_strip():
    # The rain window's glass: mostly clear, dark at the edges of what the street lamp lights, with drops and runs moving down it.
    # One frame per row of sixteen; the drops fall a little each frame, the runs lengthen and break.
    h = 16 * RAIN_FRAMES
    im = Image.new('RGBA', (16, h), (0, 0, 0, 0)); px = im.load()
    rnd = random.Random(6290)
    drops = [(rnd.randrange(1, 15), rnd.randrange(16), rnd.choice((1, 2))) for _ in range(9)]
    for f in range(RAIN_FRAMES):
        for y in range(16):
            for x in range(16):
                px[x, f * 16 + y] = (118, 138, 160, 46)
        for i, (x, y0, speed) in enumerate(drops):
            y = (y0 + f * speed) % 16
            for k in range(3 if speed == 2 else 2):
                yy = (y - k) % 16
                px[x, f * 16 + yy] = (188, 204, 220, 150 - k * 40)
            if i % 3 == 0:
                px[x, f * 16 + (y + 1) % 16] = (226, 234, 242, 190)
    return im


def window_frame():
    im = grain('#ddd6c4', 6292, lines=1)
    return im


def worktop():
    im = new(base='#8a9a8a', seed=6294, spread=3)
    for x in range(16):
        dot(im, x, 0, '#aab8aa'); dot(im, x, 15, '#6a786a')
    rnd = random.Random(6295)
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#c8b88a')
    return im


def cupboard_front():
    im = new(base='#d4ccb4', seed=6296, spread=2)
    frame(im, 1, 1, 8, 15, '#a8a088'); frame(im, 8, 1, 15, 15, '#a8a088')
    dot(im, 6, 4, '#6a6a6a'); dot(im, 9, 4, '#6a6a6a')
    return im


def sink():
    im = worktop()
    fill(im, 2, 3, 14, 13, '#c4c8ca'); fill(im, 3, 4, 13, 12, '#a4aaae'); dot(im, 8, 8, '#4a4e52')
    fill(im, 7, 0, 9, 3, '#d0d4d6')
    return im


def porcelain():
    im = new(base='#f0eee8', seed=6298, spread=2)
    for x in range(16):
        dot(im, x, 15, '#d4d2cc')
    return im


def bowl():
    im = porcelain()
    fill(im, 3, 3, 13, 13, '#d8d6d0'); fill(im, 5, 5, 11, 11, '#a8c0c8')
    return im


def slats():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y0 in (0, 5, 10):
        fill(im, 0, y0, 16, y0 + 4, '#6a5a44')
        for x in range(16):
            dot(im, x, y0, '#8a7a60'); dot(im, x, y0 + 3, '#4a3e2e')
    return im


def corrugated():
    im = new(base='#7a7c78', seed=6300, spread=3)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#5e605c'); dot(im, x + 1, y, '#9a9c98')
    rnd = random.Random(6301)
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8a5a3a')
    return im


def galvanised():
    im = new(base='#8e9496', seed=6302, spread=4)
    for y in (3, 8, 13):
        for x in range(16):
            dot(im, x, y, '#6e7476')
    rnd = random.Random(6303)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#a6acae')
    return im


def textures():
    out = {f'block/longer_{k}.png': v for k, v in materials().items()}
    t = {
        'wood': grain('#7a5434', 6310), 'wood_dark': grain('#4a3220', 6312), 'benchtop': benchtop(), 'drawers': drawers(),
        'bakelite': bakelite(), 'radio_face_0': radio_face(False), 'radio_face_1': radio_face(True), 'radio_back': radio_back(),
        'shade': shade_tex(), 'shade_lit': shade_lit(), 'brass': new(base='#b89a4a', seed=6314, spread=4), 'trays': trays(),
        'order': order(), 'shelf_0': shelf_load(0), 'shelf_1': shelf_load(1), 'shelf_2': shelf_load(2),
        'counter_front': counter_front(), 'counter_top': counter_top(), 'kettle': kettle(), 'cup_whole': cup(0), 'cup_cracked': cup(1),
        'tea': tea(), 'chrome': chrome(), 'toaster_top': toaster_top(), 'armchair': armchair(), 'seat': fabric('#7a3a2e', 6316),
        'coat': coat(), 'iron': iron(), 'rain': rain_strip(), 'window_frame': window_frame(), 'worktop': worktop(),
        'cupboard': cupboard_front(), 'sink': sink(), 'porcelain': porcelain(), 'bowl': bowl(), 'slats': slats(),
        'corrugated': corrugated(), 'galvanised': galvanised(), 'tape': new(base='#cfc6ae', seed=6318, spread=2),
    }
    out.update({f'block/longer/{k}.png': v for k, v in t.items()})
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle, render='minecraft:cutout'):
    tex = {k: (v if ':' in v else P + v) for k, v in textures.items()}
    return {'ambientocclusion': False, 'render_type': render, 'textures': {**tex, 'particle': tex.get(particle, P + particle)}, 'elements': elements}


def legs(x0, z0, x1, z1, top, t='w', w=2):
    return [element([x, 0, z], [x + w, top, z + w], {'all': t}) for x in (x0, x1 - w) for z in (z0, z1 - w)]


def fitting_models():
    m = {}
    # The workbench: a thick scarred top over a drawer front, its ends closed; three parts, left end (0), middle (1), right end (2).
    for stage in range(3):
        els = [element([0, 13, 0], [16, 16, 16], {'all': 'w', 'up': 't'}), element([0, 2, 9], [16, 13, 16], {'all': 'w', 'north': 'd'})]
        if stage in (0, 2):
            x = 0 if stage == 0 else 13
            els.append(element([x, 0, 0], [x + 3, 13, 9], {'all': 'w'}))
        els.append(element([0, 0, 9], [16, 2, 16], {'all': 'k'}))
        m[f'workbench_{stage}'] = model(els, {'w': 'wood', 't': 'benchtop', 'd': 'drawers', 'k': 'wood_dark'}, 'benchtop')
    m['trays_0'] = model([element([1, 0, 3], [15, 3, 13], {'all': 'k', 'up': 't'}), element([2, 3, 11], [14, 4, 13], {'all': 'k'})],
                         {'k': 'iron', 't': 'trays'}, 'trays')
    # The lamp: a weighted base, a jointed arm, the mended shade.
    for stage, shade_t in ((0, 'shade'), (1, 'shade_lit')):
        m[f'lamp_{stage}'] = model([
            element([4, 0, 5], [12, 1, 13], {'all': 'k'}),
            element([7.5, 1, 8.5], [8.5, 7, 9.5], {'all': 'b'}),
            element([7.5, 6, 6], [8.5, 7, 9.5], {'all': 'b'}),
            element([5, 7, 3], [11, 12, 9], {'all': 's', 'down': 'k'}),
            element([10.9, 8, 4], [11.4, 10, 7], {'all': 'p'}),
        ], {'k': 'iron', 'b': 'brass', 's': shade_t, 'p': 'tape'}, shade_t)
    # 0.4.623: lit and turned onto the radio beside it (the radio stands to the lamp's left as it faces the room): the arm swung over,
    # the shade tipped down at the set.
    m['lamp_2'] = model([
        element([4, 0, 5], [12, 1, 13], {'all': 'k'}),
        element([7.5, 1, 8.5], [8.5, 7, 9.5], {'all': 'b'}),
        element([2.5, 6, 8.5], [8.5, 7, 9.5], {'all': 'b'}),
        element([0, 6.5, 5.5], [5.5, 11.5, 11.5], {'all': 's', 'down': 'k'}, rot={'origin': [2.75, 9, 8.5], 'axis': 'z', 'angle': 22.5}),
        element([0.2, 7.5, 5.3], [3, 9.5, 5.6], {'all': 'p'}, rot={'origin': [2.75, 9, 8.5], 'axis': 'z', 'angle': 22.5}),
    ], {'k': 'iron', 'b': 'brass', 's': 'shade_lit', 'p': 'tape'}, 'shade_lit')
    # The radio: off (0), playing (1), and turned round with its back panel off, the panel leant against it (2).
    for stage in range(2):
        m[f'radio_{stage}'] = model([element([2, 0, 5], [14, 9, 12], {'all': 'c', 'north': 'f'}, uv={'north': [1, 2, 15, 11]}),
                                     element([3, 9, 6], [13, 10, 11], {'all': 'c'})], {'c': 'bakelite', 'f': f'radio_face_{stage}'}, f'radio_face_{stage}')
    m['radio_2'] = model([element([2, 0, 5], [14, 9, 12], {'all': 'c', 'north': 'r'}, uv={'north': [1, 2, 15, 11]}),
                          element([3, 9, 6], [13, 10, 11], {'all': 'c'}),
                          element([0, 0, 2], [1, 8, 13], {'all': 'c'}, rot={'origin': [0, 0, 8], 'axis': 'z', 'angle': 22.5})],
                         {'c': 'bakelite', 'r': 'radio_back'}, 'radio_back')
    # The chair whose front leg has been mended twice: the splint shows.
    chair = [element([2, 8, 2], [14, 9.5, 14], {'all': 'w', 'up': 's'}), element([2, 9.5, 12], [14, 19, 13.5], {'all': 'w'})] + legs(2, 2, 14, 14, 8)
    chair.append(element([1.5, 1, 1.5], [4.5, 5, 4.5], {'all': 'p'}))
    m['chair_0'] = model(chair, {'w': 'wood', 's': 'seat', 'p': 'wood_dark'}, 'wood')
    m['order_0'] = model([element([4, 2, 15.5], [12, 13, 16], {'north': 'o', 'south': 'o', 'east': 'o', 'west': 'o', 'up': 'o', 'down': 'o'}, uv={'north': [4, 2, 12, 13]}),
                          element([7.5, 6, 14], [8.5, 7, 16], {'all': 'k'})], {'o': 'order', 'k': 'iron'}, 'order')
    for i in range(3):
        m[f'shelf_{i}'] = model([element([0, 0, 8], [16, 16, 16], {'all': 'w', 'north': 'l'}, uv={'north': [0, 0, 16, 16]}),
                                 element([0, 0, 7], [16, 1, 8], {'all': 'w'})], {'w': 'wood_dark', 'l': f'shelf_{i}'}, f'shelf_{i}')
    m['counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 't', 'down': 'w'})],
                           {'f': 'counter_front', 'w': 'wood', 't': 'counter_top'}, 'counter_front')
    for stage in range(2):
        els = [element([4, 0, 4], [12, 7, 12], {'all': 'k'}), element([6, 7, 6], [10, 8, 10], {'all': 'k'}),
               element([7, 8, 5], [9, 9, 11], {'all': 'd'}), element([7, 3, 1.5], [9, 5, 4], {'all': 'k'})]
        if stage == 1:
            els.append(element([7.5, 5, 0.5], [8.5, 6.5, 1.5], {'all': 'p'}))
        m[f'kettle_{stage}'] = model(els, {'k': 'kettle', 'd': 'wood_dark', 'p': 'porcelain'}, 'kettle')
    def cupel(x, t, top):
        return [element([x, 0, 6], [x + 3, 4, 9], {'all': t, 'up': top}), element([x + 3, 1, 7], [x + 4, 3, 8], {'all': t})]
    m['cups_0'] = model(cupel(3, 'a', 'e') + cupel(10, 'b', 'e'), {'a': 'cup_whole', 'b': 'cup_cracked', 'e': 'tea'}, 'cup_whole')
    m['cups_1'] = model(cupel(10, 'b', 'e'), {'b': 'cup_cracked', 'e': 'tea'}, 'cup_cracked')
    m['cups_2'] = model(cupel(3, 'a', 'e'), {'a': 'cup_whole', 'e': 'tea'}, 'cup_whole')
    for stage in range(2):
        els = [element([4, 0, 5], [12, 6, 11], {'all': 'c', 'up': 't'}), element([12, 2 if stage else 4, 7], [13, 3 if stage else 5, 9], {'all': 'k'})]
        m[f'toaster_{stage}'] = model(els, {'c': 'chrome', 't': 'toaster_top', 'k': 'iron'}, 'chrome')
    m['table_0'] = model([element([0, 13, 0], [16, 16, 16], {'all': 'w'}), element([7, 0, 7], [9, 13, 9], {'all': 'd'}), element([4, 0, 4], [12, 1, 12], {'all': 'd'})],
                         {'w': 'wood', 'd': 'wood_dark'}, 'wood')
    m['armchair_0'] = model([element([1, 2, 1], [15, 7, 15], {'all': 'a'}), element([1, 7, 11], [15, 18, 15], {'all': 'a'}),
                             element([1, 7, 1], [3, 12, 11], {'all': 'a'}), element([13, 7, 1], [15, 12, 11], {'all': 'a'})] + legs(1, 1, 15, 15, 2, 'd'),
                            {'a': 'armchair', 'd': 'wood_dark'}, 'armchair')
    m['kitchen_chair_0'] = model([element([2, 8, 2], [14, 9.5, 14], {'all': 'w'}), element([2, 9.5, 12], [14, 11, 13.5], {'all': 'w'}),
                                  element([2, 16, 12], [14, 19, 13.5], {'all': 'w'}), element([2, 11, 12], [3.5, 16, 13.5], {'all': 'w'}),
                                  element([12.5, 11, 12], [14, 16, 13.5], {'all': 'w'})] + legs(2, 2, 14, 14, 8), {'w': 'wood'}, 'wood')
    for stage in range(2):
        els = [element([4, 13, 15], [12, 14.5, 16], {'all': 'w'}), element([7, 11, 13.5], [9, 13, 15], {'all': 'k'})]
        if stage == 0:
            els += [element([4, 1, 12.5], [12, 13, 14.5], {'all': 'c'}), element([3.5, 4, 12], [6, 9, 12.5], {'all': 'c'})]
        m[f'hook_{stage}'] = model(els, {'w': 'wood_dark', 'k': 'iron', 'c': 'coat'}, 'coat' if stage == 0 else 'wood_dark')
    for stage in range(2):
        sill = [element([0, 0, 6], [16, 2, 10], {'all': 'f'})] if stage == 0 else [element([0, 14, 6], [16, 16, 10], {'all': 'f'})]
        els = sill + [element([0, 0, 6.5], [2, 16, 9.5], {'all': 'f'}), element([14, 0, 6.5], [16, 16, 9.5], {'all': 'f'}),
                      element([2, 2 if stage == 0 else 0, 7.5], [14, 16 if stage == 0 else 14, 8.5], {'north': 'g', 'south': 'g'})]
        m[f'window_{stage}'] = model(els, {'f': 'window_frame', 'g': 'rain'}, 'window_frame', 'minecraft:translucent')
    m['worktop_0'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 't', 'down': 't'})],
                           {'c': 'cupboard', 't': 'worktop'}, 'cupboard')
    m['worktop_1'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 's', 'down': 't'}),
                            element([7, 16, 13], [9, 20, 15], {'all': 'k'}), element([7, 18.5, 10], [9, 19.5, 13], {'all': 'k'})],
                           {'c': 'cupboard', 't': 'worktop', 's': 'sink', 'k': 'chrome'}, 'cupboard')
    m['basin_0'] = model([element([3, 10, 7], [13, 14, 16], {'all': 'p', 'up': 'b'}), element([6.5, 0, 11], [9.5, 10, 14], {'all': 'p'}),
                          element([7.5, 14, 14], [8.5, 16, 15.5], {'all': 'k'})], {'p': 'porcelain', 'b': 'bowl', 'k': 'chrome'}, 'porcelain')
    m['lavatory_0'] = model([element([4, 0, 4], [12, 7, 12], {'all': 'p'}), element([3, 7, 2], [13, 8, 12], {'all': 'p', 'up': 'b'}),
                             element([3, 8, 12], [13, 15, 16], {'all': 'p'}), element([2.5, 15, 11.5], [13.5, 16, 16], {'all': 'w'})],
                            {'p': 'porcelain', 'b': 'bowl', 'w': 'wood'}, 'porcelain')
    m['bench_0'] = model([element([0, 6, 3], [16, 8, 13], {'all': 'w', 'up': 's'}), element([0, 8, 11], [16, 16, 12.5], {'all': 'w', 'north': 's'}, uv={'north': [0, 0, 16, 8]})]
                         + [element([x, 0, z], [x + 2, 6, z + 2], {'all': 'k'}) for x in (1, 13) for z in (4, 10)], {'w': 'wood_dark', 's': 'slats', 'k': 'iron'}, 'slats')
    m['awning_0'] = model([element([0, 0, 0], [16, 2, 16], {'all': 'c'})], {'c': 'corrugated'}, 'corrugated')
    m['tank_0'] = model([element([1, 3, 1], [15, 16, 15], {'all': 'g'})] + legs(1, 1, 15, 15, 3, 'k'), {'g': 'galvanised', 'k': 'iron'}, 'galvanised')
    return m


KINDS = ['workbench', 'trays', 'lamp', 'radio', 'chair', 'order', 'shelf', 'counter', 'kettle', 'cups', 'toaster', 'table', 'armchair',
         'kitchen_chair', 'hook', 'window', 'worktop', 'basin', 'lavatory', 'bench', 'awning', 'tank']
STAGES = {'workbench': 3, 'lamp': 3, 'radio': 3, 'shelf': 3, 'kettle': 2, 'cups': 3, 'toaster': 2, 'hook': 2, 'window': 2, 'worktop': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'longer_fitting': "Rowan's Things", 'longer_brick': 'Soot Brick', 'longer_coping': 'Parapet Coping', 'longer_felt': 'Roofing Felt',
         'longer_plaster': 'Shop Plaster', 'longer_boards': 'Shop Floorboards', 'longer_paper': 'Trellis Wallpaper',
         'longer_flatboards': 'Flat Floorboards', 'longer_tile': 'Small Bathroom Tile', 'longer_ceiling': 'Painted Ceiling',
         'longer_shopfront': 'Painted Shopfront', 'longer_pavement': 'Paving Slab', 'longer_road': 'Wet Road',
         'longer_corridor': 'Corridor Paper', 'longer_runner': 'Corridor Runner', 'longer_facade': 'Yard Brick',
         'longer_facade_lit': 'Lit Window', 'longer_facade_dark': 'Dark Window', 'longer_seam': 'Wrong Edge'}


def java_lists():
    reg = (JAVA / 'LongerRegistry.java').read_text()
    mats = re.findall(r'"(\w+)"', reg[reg.index('MATERIAL_NAMES=List.of('):reg.index(');', reg.index('MATERIAL_NAMES=List.of('))])
    blk = (JAVA / 'LongerBlock.java').read_text()
    start = blk.index('enum Kind implements StringRepresentable {') + len('enum Kind implements StringRepresentable {')
    kinds = [k.strip().lower() for k in blk[start:blk.index(';', start)].split(',') if k.strip()]
    return mats, kinds


def files():
    out = {}
    for name in materials():
        out[f'models/block/longer_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/longer_{name}'}}
        out[f'blockstates/longer_{name}.json'] = {'variants': {'': {'model': NS + f'block/longer_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/longer/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/longer/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/longer_fitting.json'] = {'variants': variants}
    return out


# ------------------------------------------------------------------------------------------------ items (0.4.624, the kettle chapter)
ITEM_NAMES = {'longer_tea': 'Tea', 'longer_toast': 'Toast', 'longer_burnt_toast': 'Burnt Toast'}


def item_textures():
    """The cup without the crack, a little from above, tea in it, its handle and a wisp of steam; two slices of toast, one burnt."""
    def blank():
        return Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    t = blank()
    cup, cup_s, cup_d = (226, 214, 190, 255), (186, 170, 146, 255), (140, 124, 104, 255)
    tea, tea_l = (122, 72, 38, 255), (156, 98, 52, 255)
    for x in range(4, 12):
        for y in range(8, 15):
            t.putpixel((x, y), cup_s if x >= 10 else cup)
    for x in range(5, 11):
        t.putpixel((x, 15), cup_d)
    for x in range(4, 12):
        t.putpixel((x, 7), cup_d)
        t.putpixel((x, 14), cup_s)
    for x in range(5, 11):
        t.putpixel((x, 8), tea)
    for x in range(6, 9):
        t.putpixel((x, 8), tea_l)
    for y in range(9, 13):
        t.putpixel((12, y), cup_s)
    for xy in ((13, 10), (13, 11)):
        t.putpixel(xy, cup_s)
    for xy in ((12, 9), (12, 12)):
        t.putpixel(xy, cup_d)
    for xy in ((7, 5), (8, 4), (7, 3), (8, 2), (9, 5), (10, 4)):
        t.putpixel(xy, (235, 235, 235, 120))

    def toast(burnt):
        im = blank()
        crust = (52, 34, 24, 255) if burnt else (160, 96, 40, 255)
        crust2 = (30, 20, 14, 255) if burnt else (122, 70, 30, 255)
        face = (84, 58, 40, 255) if burnt else (222, 170, 96, 255)
        face2 = (64, 44, 30, 255) if burnt else (204, 148, 78, 255)
        for x in range(3, 13):
            for y in range(3, 14):
                if y == 3 and x in (3, 12):
                    continue
                im.putpixel((x, y), face if 3 < x < 12 and 4 < y < 13 else crust)
        for x in (4, 11):
            im.putpixel((x, 3), crust)
        for x in range(5, 11):
            im.putpixel((x, 2), crust)
        for x in range(3, 13):
            im.putpixel((x, 13), crust2)
        r = random.Random(3 if burnt else 7)
        for _ in range(14):
            im.putpixel((r.randint(5, 10), r.randint(5, 11)), face2)
        if burnt:
            for xy in ((6, 6), (9, 8), (7, 10), (10, 11), (5, 9)):
                im.putpixel(xy, (22, 16, 12, 255))
        return im
    return {'item/longer_tea.png': t, 'item/longer_toast.png': toast(False), 'item/longer_burnt_toast.png': toast(True)}


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    mats, kinds = java_lists()
    assert mats == list(materials()), ('the game lists other materials', mats)
    assert kinds == KINDS, ('the game lists other kinds', kinds)
    images, data = textures(), files()
    images.update(item_textures())
    data.update({f'models/item/{k}.json': {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{k}'}} for k in ITEM_NAMES})
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/longer/{tex.split("longer/")[1]}.png' in images, (key, tex)
    for rel, im in images.items():
        path = A / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    meta = A / 'textures/block/longer/rain.png.mcmeta'; want = {'animation': {'frametime': 3, 'interpolate': False}}
    if check:
        assert json.loads(meta.read_text()) == want, 'rain animation'
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
    print(('Verified' if check else 'Drew') + f" Rowan's shop: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
