"""The House on the Corner (0.4.408): a city street at siesta, and the empty house on the corner.

Draws, in native Minecraft pixels from fixed seeds: five materials (the houses' painted render, the pavement slabs, the road's old stone
setts, the house's patterned floor tiles, and the corridor's faded paper), and every fitting of the corner_fitting block, its models
facing north and turned by its facing: one cell of the tall iron gate, shut or swung open, lower and upper; a board on the front room's
wall, bare or with a glass jar on it of small teeth, of clipped fingernails, or of something pale that can't be made out; a wall lamp
with a frosted shade, dark or lit; a tall window behind its shutters, dark or lit through the slats; the kiosk's newspaper rack; the dog
asleep; a school exercise book left on the kerb; a pebble on the kerb, alone or holding down a folded paper; jacaranda petals fallen on
the pavement; and the jacarandas' flowering crowns. Writes the models and block states and the blocks' names. --check verifies
everything.

0.4.578, the lot's ironwork and the dog. The fence was the game's iron bars, set down one to a cell with nothing joining them, so
it stood as rows of loose posts with no rail, in another iron from the gate's. It is the lot's own now, and so is the gate: every
picket, rail and post a bar of its own, in one iron. The fence is three cells high (a rail on a low plinth; a pair of rails at
the gate's head; points above), with a post near one end of some cells and one at the corner; the gate's leaves are framed, with a
rail at the foot, at the lock and at the head; the balconies have a rail round them. The dog was two boxes and a strip: it lies
on its side now with its head on the ground, its legs out along the pavement, an ear fallen across its cheek and its tail round
behind it, and its coat is painted as it is seen from above.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/corner/'


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


# ------------------------------------------------------------------------------------------------ materials
def stucco():
    im = new('#e6d9bf', 40801, 5); rnd = random.Random(40802)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#d8c9ab')
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#f0e6d0')
    # A hairline crack wandering down from the top.
    x = 11
    for y in range(0, 7):
        dot(im, x, y, '#bfae8e'); x += rnd.choice((-1, 0, 0, 1))
    return im


def paving():
    im = new('#b9b5ab', 40803, 4)
    for i in range(16):
        dot(im, i, 0, '#8f8b82'); dot(im, i, 8, '#8f8b82'); dot(im, 0, i, '#8f8b82'); dot(im, 8, i, '#8f8b82')
    rnd = random.Random(40804)
    for _ in range(8):
        dot(im, rnd.randrange(1, 16), rnd.randrange(1, 16), '#a8a49a')
    dot(im, 5, 11, '#cbc7bd'); dot(im, 12, 4, '#cbc7bd')
    return im


def setts():
    im = new('#6f6a63', 40805, 6); rnd = random.Random(40806)
    for row in range(4):
        y0 = row * 4; off = 2 if row % 2 else 0
        for i in range(16):
            dot(im, i, y0, '#3e3a35')
        for x in range(-off, 16, 4):
            for y in range(y0, y0 + 4):
                dot(im, x + off, y, '#3e3a35')
        for x in range(off, 16, 4):
            dot(im, (x + 1) % 16, y0 + 1, '#86807a')
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#5c5852')
    return im


def tiles():
    im = new('#e8dcc4', 40807, 2)
    for qx in (0, 8):
        for qy in (0, 8):
            fill(im, qx, qy, qx + 8, qy + 1, '#b9a68a'); fill(im, qx, qy, qx + 1, qy + 8, '#b9a68a')
            # A small four-petal figure in each tile's middle, and corners that meet as diamonds across the joints.
            cx, cy = qx + 4, qy + 4
            for dx, dy in ((0, -2), (0, 2), (-2, 0), (2, 0)):
                dot(im, cx + dx, cy + dy, '#a8573e')
            for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
                dot(im, cx + dx, cy + dy, '#3e6a68')
            dot(im, cx, cy, '#2d4c4a')
            for dx, dy in ((1, 1), (7, 1), (1, 7), (7, 7)):
                dot(im, qx + dx, qy + dy, '#a8573e')
    return im


def wallpaper():
    im = new('#d9cfb6', 40809, 3); rnd = random.Random(40810)
    for y in range(16):
        dot(im, 3, y, '#c8bc9e'); dot(im, 11, y, '#c8bc9e')
    for x, y in ((7, 2), (7, 10), (15, 6), (15, 14)):
        dot(im, x, y, '#8c9a76'); dot(im, x - 1, y + 1, '#8c9a76'); dot(im, x + 1, y + 1, '#8c9a76'); dot(im, x, y + 1, '#a8705c')
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#cfc3a6')
    return im


def materials():
    return {'stucco': stucco(), 'paving': paving(), 'setts': setts(), 'tiles': tiles(), 'wallpaper': wallpaper()}


# ------------------------------------------------------------------------------------------------ fittings
IRON = '#2a2826'


def iron():
    """Wrought iron painted black a long time ago: the paint gone dull, worn through to the metal at a point here and there, and
    rust starting in a few places."""
    im = new(IRON, 40810, 3); rnd = random.Random(40812)
    for _ in range(9):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#3a3734')
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#4a3226')
    return im


def plinth():
    """The low course the fence stands on, and the piers under its posts: cement render. A box takes its faces from where it
    stands, so the picture's upper rows are the top of the course, weathered pale, and its last five the side: the arris lit,
    the foot dark with damp and a little moss."""
    im = new('#a39c90', 40813, 5); rnd = random.Random(40814)
    for x in range(16):
        dot(im, x, 11, shade('#b8b2a6', rnd.randint(-3, 3)))
        for y in (12, 13):
            dot(im, x, y, shade('#958e83', rnd.randint(-4, 4)))
        dot(im, x, 14, shade('#7f796f', rnd.randint(-4, 4))); dot(im, x, 15, shade('#6b665d', rnd.randint(-4, 4)))
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.choice((14, 15)), '#66704f')
    for _ in range(4):
        dot(im, rnd.randrange(16), rnd.randrange(11), '#8f887c')
    return im


def board():
    im = new('#7a5a3c', 40811, 5)
    for x in range(16):
        dot(im, x, 0, '#5c4129'); dot(im, x, 15, '#5c4129')
    return im


def glass():
    im = new()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y == 15:
                dot(im, x, y, '#cfe0dc', 170)
    for y in range(2, 13):
        dot(im, 3, y, '#f4fbfa', 200)
    dot(im, 4, 3, '#f4fbfa', 200)
    return im


def lid():
    im = new('#8f8a80', 40812, 6)
    for x in range(16):
        dot(im, x, 15, '#6c675e')
    return im


def teeth():
    im = new('#cfc5ad', 40813, 3); rnd = random.Random(40814)
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#f2ecdc'); dot(im, x, min(15, y + 1), '#ddd3bb')
    return im


def nails():
    im = new('#c7b19e', 40815, 3); rnd = random.Random(40816)
    for _ in range(26):
        x, y = rnd.randrange(15), rnd.randrange(16)
        dot(im, x, y, '#e8d6c4'); dot(im, x + 1, y, '#e8d6c4')
    return im


def murk():
    im = new('#9b9a8a', 40817, 8)
    # Something folded on itself: a paler shape, its edges lost in the cloud.
    for y in range(5, 12):
        for x in range(4 + (y % 3 == 0), 12 - (y % 4 == 0)):
            dot(im, x, y, '#cfcab6')
    for x in range(5, 11):
        dot(im, x, 8, '#b5af99')
    return im


def lamp(lit):
    im = new('#f6efd8' if lit else '#bdb7a6', 40818, 2)
    for x in range(16):
        dot(im, x, 0, '#a08a52'); dot(im, x, 15, '#a08a52')
    return im


def brass():
    return new('#a08a52', 40819, 6)


def shutter(lit):
    im = new('#3f5a44', 40820, 3)
    for y in range(1, 16, 3):
        for x in range(1, 15):
            dot(im, x, y, '#2c4030')
            if lit:
                dot(im, x, y + 1, '#f3d48a')
    for y in range(16):
        dot(im, 0, y, '#263a2a'); dot(im, 15, y, '#263a2a'); dot(im, 7, y, '#263a2a')
    for x in range(16):
        dot(im, x, 0, '#263a2a'); dot(im, x, 15, '#263a2a')
    return im


def rack():
    im = new('#3a5a3e', 40821, 3); rnd = random.Random(40822)
    for row, y0 in enumerate((2, 7, 12)):
        for x0 in (1, 6, 11):
            c = rnd.choice(('#ece6d4', '#e0d8c2', '#d9cfb6'))
            fill(im, x0, y0, x0 + 4, y0 + 4, c)
            for x in range(x0, x0 + 4):
                dot(im, x, y0 + 1, '#5a5650')
            dot(im, x0 + 1, y0 + 3, '#8a8478')
    return im


def kiosk_side():
    return new('#3a5a3e', 40823, 3)


FUR, FUR_DARK, FUR_PALE = '#b28a5e', '#7c5a3a', '#d6c19e'


def fur(base=FUR, seed=40824):
    """A short coat: the hairs lie one way, in strokes two pixels long, some darker and a few lighter."""
    im = new(base, seed, 4); rnd = random.Random(seed + 1)
    for _ in range(24):
        x, y = rnd.randrange(16), rnd.randrange(15)
        c = shade(base, rnd.choice((-18, -12, -12, 9)))
        dot(im, x, y, c); dot(im, x, y + 1, c)
    return im


def dog_top():
    """The dog from above as it lies: on its side, its head to the north, its back to the east and its legs to the west. Each
    box of its body shows the piece of this picture it stands under. The darker saddle down its back and onto its tail; the pale
    of its belly, its throat, its jaw and its muzzle; the eye shut; the line of its lip; the fold where the thigh lies over the
    flank."""
    im = fur(seed=40829); dark = fur(FUR_DARK, 40827); pale = fur(FUR_PALE, 40833)

    def lay(src, cells):
        for x, z in cells:
            im.putpixel((x, z), src.getpixel((x, z)))
    lay(dark, [(x, z) for z in range(6, 15) for x in (11, 12)] + [(10, 7), (10, 8), (10, 13), (11, 15), (12, 15)])
    lay(pale, [(7, z) for z in range(6, 10)] + [(8, 8), (8, 9), (8, 10), (8, 11), (9, 10), (7, 12), (7, 13), (7, 14)])
    lay(pale, [(8, 0), (9, 0), (10, 0), (8, 1), (9, 1), (8, 2), (8, 3), (8, 4), (8, 5), (9, 5)])
    dot(im, 8, 0, shade(FUR_PALE, -52)); dot(im, 8, 1, shade(FUR_PALE, -44))
    dot(im, 11, 3, '#4a3526'); dot(im, 11, 2, shade(FUR, -14)); dot(im, 12, 3, shade(FUR, 10))
    dot(im, 8, 12, shade(FUR, -22)); dot(im, 9, 12, shade(FUR, -16))
    dot(im, 8, 15, FUR_PALE)
    return im


def dog_nose():
    return new('#2a201a', 40832, 2)


def notebook():
    im = new('#4f6a8a', 40826, 3)
    fill(im, 3, 4, 13, 9, '#ece6d4')
    for x in range(4, 12):
        dot(im, x, 6, '#8a8478')
    for x in range(4, 9):
        dot(im, x, 7, '#8a8478')
    for y in range(16):
        dot(im, 0, y, '#3a5070')
    return im


def paper_edge():
    im = new('#ece6d4', 40827, 2)
    for x in range(0, 16, 2):
        dot(im, x, 8, '#cfc8b4')
    return im


def pebble():
    im = new('#9a958c', 40828, 6)
    dot(im, 4, 4, '#b8b3a8'); dot(im, 5, 4, '#b8b3a8')
    return im


def folded():
    im = new('#f0eadb', 40829, 2)
    for i in range(16):
        dot(im, i, i, '#d5cdb8')
    for x in range(3, 13):
        dot(im, x, 5, '#9a958a')
    for x in range(3, 10):
        dot(im, x, 7, '#9a958a')
    return im


def petals():
    im = new(); rnd = random.Random(40830)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, rnd.choice(('#8a6ac8', '#9f86d8', '#7458b8')))
    return im


def blossom():
    im = new(); rnd = random.Random(40831); px = im.load()
    for y in range(16):
        for x in range(16):
            r = rnd.random()
            if r < .78:
                px[x, y] = shade(rnd.choice(('#8a6ac8', '#9f86d8', '#7458b8', '#6e4fb0')), rnd.randint(-6, 6))
            elif r < .86:
                px[x, y] = shade('#4c6a3a', rnd.randint(-6, 6))
    return im


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/corner_{name}.png'] = im
    faces = {
        'iron': iron(), 'plinth': plinth(), 'board': board(), 'glass': glass(), 'lid': lid(), 'teeth': teeth(), 'nails': nails(),
        'murk': murk(), 'lamp_off': lamp(False), 'lamp_on': lamp(True), 'brass': brass(), 'shutter_shut': shutter(False), 'shutter_lit': shutter(True),
        'stucco': stucco(), 'rack': rack(), 'kiosk_side': kiosk_side(), 'fur': fur(), 'fur_dark': fur(FUR_DARK, 40827), 'fur_pale': fur(FUR_PALE, 40833),
        'dog_top': dog_top(), 'dog_nose': dog_nose(), 'notebook': notebook(),
        'paper_edge': paper_edge(), 'pebble': pebble(), 'folded': folded(), 'petals': petals(), 'blossom': blossom(),
    }
    for name, im in faces.items():
        out[f'block/corner/{name}.png'] = im
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
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
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def solid(lo, hi, tex, rot=None, **over):
    """A box of one texture. Each face is a piece of the picture of the face's own size, taken from where the box stands and slid
    into the picture where it stands outside it, so nothing is stretched. A face named in over takes another texture, or a
    texture and the piece of it to show. rot turns the box as the game's models allow."""
    w, h, d = hi[0] - lo[0], hi[1] - lo[1], hi[2] - lo[2]

    def at(start, size):
        return max(0, min(16 - size, start % 16))
    u, v, t = at(lo[0], w), at(16 - hi[1], h), at(lo[2], d)
    uv = {'north': [u, v, u + w, v + h], 'south': [u, v, u + w, v + h], 'east': [t, v, t + d, v + h], 'west': [t, v, t + d, v + h],
          'up': [u, t, u + w, t + d], 'down': [u, t, u + w, t + d]}
    faces = {k: tex for k in uv}
    for k, value in over.items():
        if isinstance(value, tuple):
            faces[k], uv[k] = value
        else:
            faces[k] = value
    return element(lo, hi, faces, uv=uv, rot=rot)


# The fence's pickets within a cell: where each stands, and whether it is one of the tall ones. The same read from either end,
# so a cell turned round still meets its neighbours: two tall ones stand together at every join and two short ones between.
PICKETS = ((2, True), (6, False), (10, False), (14, True))


def along(axis):
    """A box given by where it lies along a run (u), how high, and across it (t), for a run along x or along z."""
    def box(u0, u1, y0, y1, t0, t1, tex='i'):
        return solid([u0, y0, t0], [u1, y1, t1], tex) if axis == 'x' else solid([t0, y0, u0], [t1, y1, u1], tex)
    return box


def run(stage, u0=0, u1=16, post=None, axis='x'):
    """One cell of the fence from u0 to u1 along its line. Stage 0: the plinth, the rail on it, the pickets up from the rail.
    Stage 1: the pickets, and a rail near the top. Stage 2: a rail at the foot (with the one below it, the pair at the gate's
    head), the pickets on up, the tall ones barbed. A post stands in for the picket where it is."""
    box = along(axis); els = []
    pickets = [(u, tall) for u, tall in PICKETS if u0 < u < u1 and u != post]
    if stage == 0:
        els.append(box(u0, u1, 0, 4, 6, 10, 'p'))
        els.append(box(u0, u1, 4, 5, 7.25, 8.75))
        els += [box(u - .5, u + .5, 5, 16, 7.5, 8.5) for u, _ in pickets]
    elif stage == 1:
        els += [box(u - .5, u + .5, 0, 16, 7.5, 8.5) for u, _ in pickets]
        els.append(box(u0, u1, 13, 14, 7.25, 8.75))
    else:
        els.append(box(u0, u1, 0, 1, 7.25, 8.75))
        for u, tall in pickets:
            els.append(box(u - .5, u + .5, 1, 13 if tall else 10, 7.5, 8.5))
            if tall:
                els.append(box(u - 1.5, u + 1.5, 10, 11, 7.4, 8.6))
    return els


def post(stage, u=2, axis='x', pier=2):
    """A post: on its pier, straight up through the middle cell, and capped under a finial in the top one."""
    box = along(axis)
    if stage == 0:
        return [box(u - pier, u + pier, 0, 5.5, 5, 11, 'p'), box(u - 1, u + 1, 5.5, 16, 7, 9)]
    if stage == 1:
        return [box(u - 1, u + 1, 0, 16, 7, 9)]
    return [box(u - 1, u + 1, 0, 12, 7, 9), box(u - 1.5, u + 1.5, 12, 13, 6.5, 9.5), box(u - .5, u + .5, 13, 15, 7.5, 8.5)]


def leaf(upper, opened):
    """One cell of a gate's leaf, hung at its west edge: a stile at each side, four pickets between; in the lower cell a rail at
    the foot and one at the lock, and the lock's plate on the stile that shuts; in the upper a rail at the head, the pickets
    standing a little above it. Opened, it has swung a quarter turn and stands along the edge it hangs from."""
    def box(u0, u1, y0, y1, t=.5):
        return solid([u0, y0, 8 - t], [u1, y1, 8 + t], 'i') if not opened else solid([1 - t, y0, u0], [1 + t, y1, u1], 'i')
    top = 15 if upper else 16
    els = [box(0, 1, 0, top), box(15, 16, 0, top)]
    els += [box(u - .5, u + .5, 0 if upper else 2, 16, .5) for u in (3.5, 6.5, 9.5, 12.5)]
    if upper:
        els.append(box(1, 15, 13, 14, .75))
    else:
        els += [box(1, 15, 1, 2, .75), box(1, 15, 13, 14, .75), box(13.5, 16, 9.5, 12.5, 1)]
    return els


def railing(stage):
    """A balcony's rail along the edge it looks out over: a handrail, a rail at the foot, four balusters; with a return down its
    west side (stage 1) or its east (stage 2)."""
    els = [solid([0, 11, 0], [16, 12, 1.5], 'i'), solid([0, 1, .25], [16, 2, 1.25], 'i')]
    els += [solid([x - .5, 2, .25], [x + .5, 11, 1.25], 'i') for x in (2, 6, 10, 14)]
    for side, x in ((1, 0), (2, 14.5)):
        if stage == side:
            els += [solid([x, 11, 1.5], [x + 1.5, 12, 16], 'i'), solid([x + .25, 1, 1.5], [x + 1.25, 2, 16], 'i')]
            els += [solid([x + .25, 2, z - .5], [x + 1.25, 11, z + .5], 'i') for z in (5, 9, 13)]
    return els


def dog():
    """The dog asleep on its side, as a dog sleeps on a warm pavement: its head to the north and on the ground, its back to the
    east, its legs out to the west. The ribs stand highest, the loin dips and the hip rises again. The tops of its body are one
    picture of it from above; no two tops are level where they meet, and each leg runs a little into the body it comes from."""
    f, d, p, t = 'f', 'd', 'p', 't'

    def body(lo, hi, tex=f, **over):
        return solid(lo, hi, tex, up=(t, [lo[0], lo[2], hi[0], hi[2]]), **over)

    def leg(paw, hip, z0, z1, high, angle=0):
        """A leg along the ground from the body's edge (hip) out to its paw, thicker where it leaves the body and turned about
        that place."""
        rot = {'origin': [hip, 0, (z0 + z1) / 2], 'axis': 'y', 'angle': angle} if angle else None
        knee = (paw + hip) / 2 + .5
        return [solid([knee, 0, z0 - .25], [hip + .5, high + .3, z1 + .25], f, rot=rot),
                solid([paw + .75, 0, z0], [knee + .25, high, z1], f, rot=rot),
                solid([paw, 0, z0 - .1], [paw + 1, high + .1, z1 + .1], p, rot=rot)]
    return [
        body([7, 0, 6], [13, 4, 10], east=d, west=p),                       # the ribs: the saddle over its back, the pale of its belly
        body([8.5, 0, 10], [13, 3.25, 12], east=d, west=p),                 # the loin, tucked up
        body([7.5, 0, 12], [13, 3.75, 15], east=d),                         # the hip and the thigh that lies uppermost
        body([9.25, 0, 5.25], [12.75, 3.25, 6], west=p),                    # the neck
        body([8.5, 0, 2.25], [13, 3, 5.25], west=p),                        # the head, its jaw to the west
        body([8.5, 0, .4], [11, 2, 2.25], p),                               # the muzzle
        solid([9.9, .6, 0], [11.15, 2.15, .5], 'n'),                        # the nose
        solid([9.75, 3, 3.9], [12.6, 3.4, 5.25], d),                        # the ear that is uppermost, fallen across its cheek,
        solid([9.25, 3, 4.3], [9.75, 3.4, 5], d),                           # and its tip
        *leg(2.5, 7, 6.2, 7.3, 1.35),                                       # the forelegs, straight out side by side,
        *leg(2, 7, 7.8, 8.9, 1.2),                                          # the one under it the longer
        *leg(3, 8.5, 11.45, 12.55, 1.15, -22.5),                            # the hind legs: the upper one drawn up toward its belly,
        *leg(2.75, 7.5, 13, 14.1, 1.3, 22.5),                               # the one under it trailing back
        body([11.5, 0, 15], [13, 1.5, 16], d),                              # the tail: its root,
        body([8, 0, 15], [11.5, 1.25, 16], west=p),                         # and the rest of it round behind
    ]


def fitting_models():
    m = {}
    iron_and_plinth = {'i': 'iron', 'p': 'plinth'}
    for stage in range(4):
        m[f'gate_{stage}'] = model(leaf(stage >= 2, stage % 2 == 1), {'i': 'iron'}, 'iron')
    for stage in range(3):
        m[f'fence_{stage}'] = model(run(stage), iron_and_plinth if stage == 0 else {'i': 'iron'}, 'iron')
        m[f'fence_post_{stage}'] = model(run(stage, post=2) + post(stage), iron_and_plinth if stage == 0 else {'i': 'iron'}, 'iron')
        m[f'fence_corner_{stage}'] = model(run(stage, 0, 7) + run(stage, 0, 7, axis='z') + post(stage, u=8, pier=3),
                                           iron_and_plinth if stage == 0 else {'i': 'iron'}, 'iron')
        m[f'railing_{stage}'] = model(railing(stage), {'i': 'iron'}, 'iron')
    shelf = [element([0, 3, 8], [16, 4, 16], {'all': 'b'}), element([2, 1, 15], [3, 3, 16], {'all': 'b'}), element([13, 1, 15], [14, 3, 16], {'all': 'b'})]
    m['jar_0'] = model(shelf, {'b': 'board'}, 'board')
    for stage, contents in ((1, 'teeth'), (2, 'nails'), (3, 'murk')):
        m[f'jar_{stage}'] = model(shelf + [
            element([5.5, 4.2, 9.5], [10.5, 10, 14.5], {'all': 'c'}),
            element([5, 4, 9], [11, 12, 15], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g'}),
            element([5, 12, 9], [11, 13, 15], {'all': 'l'})], {'b': 'board', 'c': contents, 'g': 'glass', 'l': 'lid'}, 'glass')
    for stage, shade_ in ((0, 'lamp_off'), (1, 'lamp_on')):
        m[f'lamp_{stage}'] = model([element([7.5, 9, 14], [8.5, 10, 16], {'all': 'm'}), element([7, 12, 12], [9, 13, 16], {'all': 'm'}),
                                    element([5, 6, 10], [11, 12, 14], {'all': 's'})], {'m': 'brass', 's': shade_}, shade_)
    for stage, face in ((0, 'shutter_shut'), (1, 'shutter_lit')):
        m[f'window_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'r', 'north': 'w'})], {'r': 'stucco', 'w': face}, face)
    m['kiosk_0'] = model([element([1, 0, 3], [15, 14, 13], {'all': 's', 'north': 'r'})], {'s': 'kiosk_side', 'r': 'rack'}, 'rack')
    m['dog_0'] = model(dog(), {'f': 'fur', 'd': 'fur_dark', 'p': 'fur_pale', 't': 'dog_top', 'n': 'dog_nose'}, 'fur')
    m['notebook_0'] = model([element([4, 0, 4], [12, 1, 12], {'all': 'e', 'up': 'n'})], {'e': 'paper_edge', 'n': 'notebook'}, 'notebook')
    m['note_0'] = model([element([6, 0, 6], [10, 2, 10], {'all': 'p'})], {'p': 'pebble'}, 'pebble')
    m['note_1'] = model([element([4, 0, 4], [12, 0.5, 12], {'all': 'e', 'up': 'f'}), element([6, 0.5, 6], [10, 2.5, 10], {'all': 'p'})],
                        {'e': 'paper_edge', 'f': 'folded', 'p': 'pebble'}, 'folded')
    m['petals_0'] = model([element([0, 0.1, 0], [16, 0.2, 16], {'up': 'p', 'down': 'p'})], {'p': 'petals'}, 'petals')
    m['blossom_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b'})], {'b': 'blossom'}, 'blossom')
    return m


KINDS = ['gate', 'jar', 'lamp', 'window', 'kiosk', 'dog', 'notebook', 'note', 'petals', 'blossom', 'fence', 'fence_post', 'fence_corner', 'railing']
STAGES = {'gate': 4, 'jar': 4, 'lamp': 2, 'window': 2, 'note': 2, 'fence': 3, 'fence_post': 3, 'fence_corner': 3, 'railing': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 4
NAMES = {'corner_fitting': 'Street Fitting', 'corner_stucco': 'Painted Render', 'corner_paving': 'Pavement Slabs', 'corner_setts': 'Stone Setts',
         'corner_tiles': 'Patterned Tiles', 'corner_wallpaper': 'Faded Wallpaper'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/corner_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/corner_{name}'}}
        out[f'blockstates/corner_{name}.json'] = {'variants': {'': {'model': NS + f'block/corner_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/corner/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/corner/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/corner_fitting.json'] = {'variants': variants}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/corner/{tex.split("corner/")[1]}.png' in images, (key, tex)
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
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the street and the house on the corner: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
