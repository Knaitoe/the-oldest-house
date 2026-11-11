"""Bike night (0.4.484): the cars of Calder Falls.

The proposal's traffic: "a station wagon with wood panels, a pickup, a long sedan, a muscle car with its windows down", each "a car
driving its lane with its headlights on". Draws, in native Minecraft pixels from fixed seeds, the four cars and nothing else: each
car's paint (plain, and as its side has it, with the bright strip along the belt and the seams of its doors), the wagon's wood, the
sedan's vinyl roof, the muscle car's striped top, glass, the dark inside of a car, chrome, the grille, a headlight and a tail light
(both lit: a car here is never seen with them off), the tyre and its hubcap, the underside, and the pumpkins in the pickup's bed.

A car is four and a half blocks long, and a model's piece may not reach more than a block outside its own block, so each car is two
models, its front half and its back half, drawn by the game end to end (client/CarLook). No piece is longer than sixteen pixels
either way: a longer one would have its picture stretched over it, and everything here is sixteen pixels to the block. So a car is
built in its own space (x along it, the front at +x; y up from the road; z across it) out of boxes that are cut wherever they
cross a sixteen-pixel line, and each cut piece takes the part of its picture that lies where it is. A strip painted on one row of
a side's picture is therefore one strip along the whole car.

The lit parts (headlights and tail lights) are a third model for each car, drawn over the others at full brightness. The wheel is
one model for all of them, and a wider one for the muscle car's back axle; the game turns it.

Writes the models and the entity's name. --check verifies everything. --sheet PATH draws a page for review: every car from the
side, from above, from the front and from behind.

Drawing or checking, it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
"""
from pathlib import Path
import argparse
import json
import math
from PIL import Image

from draw_borrasca_town_art import shade, new, fill, dot, element
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/calder_car/'
P = NS + DIR
KINDS = ('wagon', 'pickup', 'sedan', 'muscle')
FACES = ('north', 'south', 'east', 'west', 'up', 'down')
# Half a car's length and half its width, in pixels; where its axles are along it; a wheel's radius, and how high its hub is.
L, W = 36, 15
AXLE, RADIUS, HUB = 21, 5.5, 5.5
# What is added to a car's own x to put each half inside a model's reach, and to its z.
FRONT, BACK, ACROSS = -10, 26, 8
PAINT = {'wagon': '#c4b188', 'pickup': '#8c392c', 'sedan': '#2e4a3d', 'muscle': '#b5461f'}
SLIP = .05


# ---------------------------------------------------------------------------------------------------------------- the pictures
def paint(kind, seed):
    return new(PAINT[kind], seed, 3)


def side(kind, seed):
    """A car's side: its paint, the bright strip along the belt, the dark sill under the doors, and a door's seam every block."""
    im = new(PAINT[kind], seed, 3)
    for y in range(16):
        dot(im, 0, y, shade(PAINT[kind], -30))
    fill(im, 0, 6, 16, 7, '#c9ccd0')
    fill(im, 0, 11, 16, 13, shade(PAINT[kind], -42))
    return im


def wood():
    """The wagon's panels: boards along the car, in a pale frame top and bottom."""
    im = new('#7b5330', 48101, 4)
    for y in (5, 9):
        fill(im, 0, y, 16, y + 1, '#5f3f22')
    for x, y in ((3, 3), (11, 7), (6, 10), (13, 4)):
        dot(im, x, y, '#684626')
    fill(im, 0, 4, 16, 5, '#d8c9a0'); fill(im, 0, 10, 16, 11, '#d8c9a0')
    for y in range(4, 11):
        dot(im, 0, y, '#d8c9a0')
    return im


def vinyl():
    im = new('#1d1c1b', 48102, 4)
    for y in range(0, 16, 4):
        for x in range((y // 4) % 2, 16, 2):
            dot(im, x, y, '#2a2826')
    return im


def striped(kind, seed):
    """The muscle car from above: two black stripes down the middle of it."""
    im = new(PAINT[kind], seed, 3)
    fill(im, 0, 1, 16, 3, '#161616'); fill(im, 0, 13, 16, 15, '#161616')
    return im


def glass():
    """A window at night: dark, with the street's light lying across it."""
    im = new('#18222b', 48103, 2)
    for i in range(16):
        dot(im, i, 15 - i, '#31434f')
        if i < 15:
            dot(im, i + 1, 15 - i, '#24323c')
    return im


def inside():
    return new('#0d0d0f', 48104, 2)


def chrome():
    im = new('#b9bcc0', 48105, 3)
    fill(im, 0, 0, 16, 1, '#dfe2e5'); fill(im, 0, 15, 16, 16, '#8e9196')
    return im


def grille():
    """Dark, with bright bars across it two pixels apart."""
    im = new('#151517', 48106, 2)
    for y in range(1, 16, 3):
        fill(im, 0, y, 16, y + 1, '#a9acb1')
    return im


def headlight():
    """Lit: a warm white lens in a bright ring."""
    im = new('#fff3c4', 48107, 2)
    for x in range(16):
        for y in range(16):
            if x % 5 == 0 or y % 4 == 0:
                dot(im, x, y, '#f2dc93')
    return im


def taillight():
    im = new('#c2241c', 48108, 4)
    for y in range(0, 16, 3):
        fill(im, 0, y, 16, y + 1, '#e9483a')
    return im


def tyre():
    return new('#19191b', 48109, 3)


def hubcap():
    """A wheel from the side: the bright cap, a dark ring of holes round it, and the tyre."""
    im = new('#19191b', 48110, 2)
    fill(im, 5, 5, 11, 11, '#b9bcc0'); fill(im, 6, 4, 10, 12, '#b9bcc0'); fill(im, 4, 6, 12, 10, '#b9bcc0')
    for x, y in ((6, 6), (9, 6), (6, 9), (9, 9)):
        dot(im, x, y, '#4b4d52')
    fill(im, 7, 7, 9, 9, '#e3e5e8')
    return im


def under():
    return new('#101012', 48111, 2)


def pumpkin():
    im = new('#d9731c', 48112, 4)
    for x in (0, 5, 10, 15):
        for y in range(16):
            dot(im, x, y, '#b3570f')
    fill(im, 6, 0, 10, 1, '#5d6b2a')
    return im


def bed():
    """The pickup's bed: worn boards."""
    im = new('#6b6a66', 48113, 5)
    for y in (3, 7, 11, 15):
        fill(im, 0, y, 16, y + 1, '#4f4e4a')
    return im


def textures():
    out = {}
    for i, kind in enumerate(KINDS):
        out[f'{DIR}{kind}.png'] = paint(kind, 48120 + i)
        out[f'{DIR}{kind}_side.png'] = side(kind, 48130 + i)
    out.update({DIR + 'muscle_top.png': striped('muscle', 48140), DIR + 'wood.png': wood(), DIR + 'vinyl.png': vinyl(), DIR + 'glass.png': glass(),
                DIR + 'inside.png': inside(), DIR + 'chrome.png': chrome(), DIR + 'grille.png': grille(), DIR + 'headlight.png': headlight(),
                DIR + 'taillight.png': taillight(), DIR + 'tyre.png': tyre(), DIR + 'hubcap.png': hubcap(), DIR + 'under.png': under(),
                DIR + 'pumpkin.png': pumpkin(), DIR + 'bed.png': bed()})
    return out


# ---------------------------------------------------------------------------------------------------------------- the pieces
def cuts(lo, hi):
    """Where a length is cut, so that no piece of it crosses a sixteen-pixel line."""
    out = [lo]; k = math.floor(lo / 16) * 16 + 16
    while k < hi - 1e-6:
        out.append(k); k += 16
    out.append(hi)
    return out


def tile(v0, v1):
    base = math.floor(v0 / 16 + 1e-9) * 16
    return round(v0 - base, 3), round(v1 - base, 3)


def box(parts, lo, hi, tex):
    """A box in the car's own space, cut on the sixteen-pixel lines. {@code tex} is one picture for every face, or a picture for each
    of: side (north and south), end (east and west), top, under, and any face by its own name. A face that would lie against the
    next cut piece of the same box is left out."""
    if isinstance(tex, str):
        tex = {'all': tex}

    def pick(face):
        group = {'north': 'side', 'south': 'side', 'east': 'end', 'west': 'end', 'up': 'top', 'down': 'under'}[face]
        return tex.get(face, tex.get(group, tex.get('all')))
    xs, ys, zs = cuts(lo[0], hi[0]), cuts(lo[1], hi[1]), cuts(lo[2], hi[2])
    for i in range(len(xs) - 1):
        for j in range(len(ys) - 1):
            for k in range(len(zs) - 1):
                x0, x1, y0, y1, z0, z1 = xs[i], xs[i + 1], ys[j], ys[j + 1], zs[k], zs[k + 1]
                lx, ly, lz = tile(x0, x1), tile(y0, y1), tile(z0, z1)
                uv = {'south': [lx[0], 16 - ly[1], lx[1], 16 - ly[0]], 'north': [16 - lx[1], 16 - ly[1], 16 - lx[0], 16 - ly[0]],
                      'east': [16 - lz[1], 16 - ly[1], 16 - lz[0], 16 - ly[0]], 'west': [lz[0], 16 - ly[1], lz[1], 16 - ly[0]],
                      'up': [lx[0], lz[0], lx[1], lz[1]], 'down': [lx[0], 16 - lz[1], lx[1], 16 - lz[0]]}
                outer = {'west': i == 0, 'east': i == len(xs) - 2, 'down': j == 0, 'up': j == len(ys) - 2, 'north': k == 0, 'south': k == len(zs) - 2}
                faces = {f: pick(f) for f in FACES if outer[f] and pick(f)}
                if faces:
                    parts.append(element([round(x0, 3), round(y0, 3), round(z0, 3)], [round(x1, 3), round(y1, 3), round(z1, 3)], faces,
                                         {f: [round(v, 3) for v in uv[f]] for f in faces}))


def both(parts, lo, hi, tex):
    """The same box on each side of the car."""
    box(parts, lo, hi, tex)
    box(parts, [lo[0], lo[1], -hi[2]], [hi[0], hi[1], -lo[2]], tex)


class Car:
    def __init__(self, kind):
        self.kind, self.body, self.lit = kind, [], []
        self.skin = {'side': kind + '_side', 'end': kind, 'top': kind, 'under': 'under'}


def lower(c, top=13, back_top=None, back_from=None):
    """The body below the windows: a tub between the wheels, and the sides with their arches cut out. The pickup's is lower behind its cab."""
    p, skin = c.body, c.skin
    spans = [(-L, L, top)] if back_from is None else [(-L, back_from, back_top), (back_from, L, top)]
    for x0, x1, t in spans:
        box(p, [x0, 3.5, -10.5], [x1, t, 10.5], {'top': skin['top'], 'under': 'under', 'end': skin['end']})
        cut = sorted({x0, x1} | {v for v in (-AXLE - 7, -AXLE + 7, AXLE - 7, AXLE + 7) if x0 < v < x1})
        for a, b in zip(cut, cut[1:]):
            arch = any(abs((a + b) / 2 - s * AXLE) < 7 for s in (-1, 1))
            if not arch or t > 11.5:
                both(p, [a, 11.5 if arch else 3.5, 10.5], [b, t, W], skin)
    both(p, [-L - 1.5, 4, -W - .5], [-L, 6.5, 0], 'chrome')      # bumpers, in two halves so that neither is longer than a block
    both(p, [L, 4, -W - .5], [L + 1.5, 6.5, 0], 'chrome')
    box(p, [L, 7.5, -8.5], [L + .3, 12, 8.5], 'grille')
    for s in (-1, 1):
        z0, z1 = (9, 14) if s > 0 else (-14, -9)
        box(c.lit, [L, 8, z0], [L + .35, 11.5, z1], 'headlight')
        box(c.lit, [-L - .35, 8, z0], [-L, 11, z1], 'taillight')


def cabin(c, x0, x1, back=3, front=3, roof=None, pillars=(), open_sides=False, low=13, mid=17.5, top=21.5):
    """The glass, in two steps so that the screens lean; its pillars, in the car's paint and a little proud of the glass; and the roof."""
    p, skin = c.body, c.skin
    pane = {'side': 'inside' if open_sides else 'glass', 'end': 'glass', 'top': 'glass'}
    box(p, [x0, low, -14], [x1, mid, 14], pane)
    box(p, [x0 + back, mid, -14], [x1 - front, top, 14], pane)
    for a, b, full in pillars:
        # A pillar at an end of the glass stands a little past it, and none of them ends level with the glass: no two faces share a plane.
        shift = .2 if b == x1 else -.2 if a == x0 else 0
        both(p, [a + shift, low, 13.6], [b + shift, top - SLIP if full else mid + SLIP, 14.4], {'side': skin['end'], 'end': skin['end'], 'top': skin['end']})
    box(p, [x0 + back - .5, top, -14.5], [x1 - front + .5, top + 1.5, 14.5], {'all': roof or c.kind, 'under': 'inside'})


def wagon():
    c = Car('wagon'); lower(c)
    cabin(c, -34, 13, back=1.5, pillars=((11, 13, False), (-3, -1, True), (-19, -17, True), (-34, -32, False)))
    for a, b in ((-L + 1, -AXLE - 7), (-AXLE + 7, AXLE - 7), (AXLE + 7, L - 1)):       # the wood, between the arches
        both(c.body, [a, 5.5, W], [b, 12, W + .3], {'side': 'wood', 'end': 'wood', 'top': 'wood', 'under': 'wood'})
    for z in (-10.5, 9.5):                                                           # the roof rack
        box(c.body, [-30, 23, z], [4, 23.8, z + 1], 'chrome')
    for x in (-29, -14, 1):
        box(c.body, [x, 23.8, -10.5], [x + 1, 24.4, 10.5], 'chrome')
    return c


def pickup():
    c = Car('pickup'); lower(c, back_top=9, back_from=-2)
    # The bed: its floor is the lowered tub, boarded over; its walls, its front and its gate. Two pumpkins ride in it.
    for a, b, low in ((-L, -AXLE - 7, 9), (-AXLE - 7, -AXLE + 7, 11.5), (-AXLE + 7, -2, 9)):      # its walls, clear of the wheels under them
        both(c.body, [a, low, 13], [b, 15, W], c.skin)
    box(c.body, [-3.5, 9, -13], [-2, 15, 13], {'side': c.kind, 'end': c.kind, 'top': c.kind})
    box(c.body, [-L, 9, -13], [-L + 1.5, 15, 13], {'side': c.kind, 'end': c.kind, 'top': c.kind})
    box(c.body, [-L + 1.5, 9, -13], [-3.5, 9.3, 13], {'top': 'bed'})
    cabin(c, -2, 14, back=0, front=2.5, pillars=((12, 14, False), (-2, 0, True)))
    box(c.body, [-23, 9.3, -8], [-17, 14.3, -2], 'pumpkin'); box(c.body, [-29, 9.3, 2], [-24, 13.3, 7], 'pumpkin')
    box(c.body, [-20.5, 14.3, -5.5], [-19.5, 15.5, -4.5], 'vinyl'); box(c.body, [-27, 13.3, 4], [-26, 14.4, 5], 'vinyl')
    return c


def sedan():
    c = Car('sedan'); lower(c)
    cabin(c, -17, 13, roof='vinyl', pillars=((11, 13, False), (-3, -1, True), (-17, -15, False)))
    return c


def muscle():
    c = Car('muscle'); c.skin = {**c.skin, 'top': 'muscle_top'}; lower(c)
    cabin(c, -16, 6, back=4, front=3, roof='muscle_top', pillars=((4, 6, False), (-16, -14, False)), open_sides=True, top=20.5)
    box(c.body, [-24, 13, -13], [-16, 16, 13], {'side': c.kind, 'end': c.kind, 'top': 'muscle_top'})      # the fastback, stepping down
    box(c.body, [14, 13, -4], [24, 14.5, 4], {'side': 'vinyl', 'end': 'grille', 'top': 'vinyl'})            # the scoop
    box(c.body, [-L, 15, -13], [-L + 3, 16, 13], {'all': c.kind})                                       # the spoiler, on two posts
    both(c.body, [-L + 1, 13, 9], [-L + 2, 15, 10.5], {'all': c.kind})
    return c


CARS = {'wagon': wagon, 'pickup': pickup, 'sedan': sedan, 'muscle': muscle}


def wheel(wide):
    """A wheel about (8, 8): the tyre an eight-sided ring made of four slabs through the hub, each a hair narrower than the last so
    that no two of their sides share a plane; and a cap on each face of it."""
    parts = []; half = round(RADIUS * math.tan(math.pi / 8), 3)
    for i, (dims, angle) in enumerate((((RADIUS, half), 0), ((half, RADIUS), 0), ((RADIUS, half), 45), ((RADIUS, half), -45))):
        w = wide / 2 - i * SLIP
        parts.append(element([round(8 - dims[0], 3), round(8 - dims[1], 3), round(8 - w, 3)], [round(8 + dims[0], 3), round(8 + dims[1], 3), round(8 + w, 3)],
                             {'all': 'tyre'}, {f: [2, 2, 14, 14] for f in FACES}, {'angle': angle, 'axis': 'z', 'origin': [8, 8, 8]} if angle else None))
    w = wide / 2 + .2
    parts.append(element([4, 4, round(8 - w, 3)], [12, 12, round(8 + w, 3)], {'north': 'hubcap', 'south': 'hubcap'}, {'north': [4, 4, 12, 12], 'south': [4, 4, 12, 12]}))
    return parts


def shifted(parts, front):
    """One half of a car's pieces, moved into a model's reach."""
    out = []
    for e in parts:
        x0, x1 = e['from'][0], e['to'][0]
        if (front and x0 < -1e-6) or (not front and x1 > 1e-6) or (front and x1 <= 1e-6 and x0 <= -1e-6):
            continue
        if not front and x0 >= -1e-6:
            continue
        dx = FRONT if front else BACK
        f = dict(e); f['from'] = [round(e['from'][0] + dx, 3), e['from'][1], round(e['from'][2] + ACROSS, 3)]
        f['to'] = [round(e['to'][0] + dx, 3), e['to'][1], round(e['to'][2] + ACROSS, 3)]
        out.append(f)
    return out


def model(parts, particle):
    keys = sorted({f['texture'][1:] for e in parts for f in e['faces'].values()})
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: P + k for k in keys}, 'particle': P + particle}, 'elements': parts}


def models():
    m = {}
    for kind, build in CARS.items():
        c = build()
        m[f'{kind}_front'] = model(shifted(c.body, True), kind)
        m[f'{kind}_back'] = model(shifted(c.body, False), kind)
        m[f'{kind}_lit_front'] = model(shifted(c.lit, True), 'headlight')
        m[f'{kind}_lit_back'] = model(shifted(c.lit, False), 'taillight')
    m['wheel'] = model(wheel(4), 'tyre')
    m['wheel_wide'] = model(wheel(5), 'tyre')
    return m


def files():
    return {f'models/{DIR}{key}.json': obj for key, obj in models().items()}


NAMES = {'entity.the_oldest_house.town_car': 'Car'}


# ---------------------------------------------------------------------------------------------------------------- review
def review(path):
    """Every car from the side, from above, from the front and from behind, at four screen pixels to the pixel: each face painted from
    its own picture, the nearer over the farther."""
    images = {k.split(DIR)[1][:-4]: v for k, v in textures().items()}
    S = 4

    def paste(page, ox, oy, tex, uv, x0, y0, x1, y1, flip=False):
        im = images[tex]; u0, v0, u1, v1 = uv
        w, h = max(1, round((x1 - x0) * S)), max(1, round((y1 - y0) * S))
        for i in range(w):
            for j in range(h):
                u = u0 + (u1 - u0) * (i + .5) / w; v = v0 + (v1 - v0) * (j + .5) / h
                px = im.getpixel((min(15, max(0, int(u))), min(15, max(0, int(v)))))
                X, Y = ox + round(x0 * S) + i, oy - round(y1 * S) + j
                if 0 <= X < page.width and 0 <= Y < page.height and px[3]:
                    page.putpixel((X, Y), px)
    page = Image.new('RGBA', (1500, 4 * 250 + 20), (70, 84, 96, 255))
    for row, (kind, build) in enumerate(CARS.items()):
        c = build(); parts = c.body + c.lit; oy = 20 + row * 250 + 150
        # the side, seen from +z: south faces, the nearer (greater z) last
        for e in sorted((e for e in parts if 'south' in e['faces']), key=lambda e: e['to'][2]):
            f = e['faces']['south']; paste(page, 190, oy, f['texture'][1:], f['uv'], e['from'][0], e['from'][1], e['to'][0], e['to'][1])
        for s in (-1, 1):                                                                    # the wheels, as discs
            for i in range(-22, 23):
                for j in range(-22, 23):
                    if i * i + j * j <= 22 * 22:
                        page.putpixel((190 + s * AXLE * S + i, oy - round(HUB * S) + j), (25, 25, 27, 255) if i * i + j * j > 10 * 10 else (185, 188, 192, 255))
        # from above: up faces, the higher last. x across the page, z down it.
        for e in sorted((e for e in parts if 'up' in e['faces']), key=lambda e: e['to'][1]):
            f = e['faces']['up']; paste(page, 560, oy - 20, f['texture'][1:], f['uv'], e['from'][0], -e['to'][2], e['to'][0], -e['from'][2])
        # the front: east faces, the nearer (greater x) last. z across the page, mirrored as it is seen.
        for e in sorted((e for e in parts if 'east' in e['faces']), key=lambda e: e['to'][0]):
            f = e['faces']['east']; paste(page, 980, oy, f['texture'][1:], f['uv'], -e['to'][2], e['from'][1], -e['from'][2], e['to'][1])
        for e in sorted((e for e in parts if 'west' in e['faces']), key=lambda e: -e['from'][0]):
            f = e['faces']['west']; paste(page, 1280, oy, f['texture'][1:], f['uv'], e['from'][2], e['from'][1], e['to'][2], e['to'][1])
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
    names = {k.split(DIR)[1] for k in images}
    for key, obj in data.items():
        for tex in obj['textures'].values():
            assert tex.split(DIR)[1] + '.png' in names, (key, tex)
        for e in obj['elements']:
            for v in e['from'] + e['to']:
                assert -16 <= v <= 32, (key, e['from'], e['to'])
            for a, b in zip(e['from'], e['to']):
                assert b - a <= 16 + 1e-6, (key, e['from'], e['to'])
            for f in e['faces'].values():
                assert all(0 <= v <= 16 for v in f['uv']), (key, e['from'], e['to'], f)
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
    print(('Verified' if check else 'Drew') + f" Calder Falls's cars: {len(images)} textures, {len(data)} models.")


if __name__ == '__main__':
    main()
