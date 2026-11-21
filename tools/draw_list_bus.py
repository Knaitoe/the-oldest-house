"""The list (0.4.735): the dawn bus.

The plan's first bus out of Lamesa, "at the feed store's bench at first light", drawn as a bus and not a fade: an intercity coach of
the time, ten blocks long, two and a half wide and two and a half high, with ribbed bright metal sides, a band of dark glass, a
two-pane door at the front of its right side, a windscreen, a roof unit, mirrors on arms, bumpers, and the sign over the screen
lit. Drawn, in native Minecraft pixels from fixed seeds, as Calder Falls's cars are (tools/draw_calder_cars.py, whose box-cutting it
uses): built in its own space (x along it, the front at +x; y up from the road; z across it, the door's side at +z) out of boxes
cut on the sixteen-pixel lines, so that no piece is longer than a block and each takes the part of its picture that lies where it is.

A model's piece may not reach more than a block outside its block, so the bus is five models, each a fifth of its length, drawn by
the game end to end (client/TownCarRenderer, TownCar.BUS); and a sixth, its lamps and its sign, in each fifth, drawn over the rest at
full brightness. Its wheels are the cars' wheel, drawn larger.

Writes the textures and models. --check verifies everything. --sheet PATH draws a page for review: the bus from its door side, from
the other side, from above, from the front and from behind.

Drawing or checking, it refuses a model in which two faces of different colours lie in one plane and overlap (tools/flicker.py).
"""
from pathlib import Path
import argparse
import json
import flicker
from PIL import Image

from draw_borrasca_town_art import shade, new, fill, dot
from draw_calder_cars import box, FACES
from flicker import fights

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
DIR = 'block/list_bus/'
CAR = 'block/calder_car/'
# Borrowed from the cars: a lamp, a tail light, chrome, the underside and the tyre.
BORROWED = ('headlight', 'taillight', 'chrome', 'under')
# Half its length and half its width, in pixels; its axles; how many fifths it is drawn in, and how long each is.
L, W = 80, 20
REAR, FRONT_AXLE, ARCH = -50, 54, 9
PARTS, PART = 5, 32
# What is added to the bus's own y and z to put a fifth in a model's reach; its x is moved by shift(i).
DY, DZ = -12, 8
# The heights: the skirt, the body under the glass, the band of glass (its lower rows are body), the roof, the unit on the roof.
SKIRT, BODY, BAND, ROOF, TOP, UNIT = 3, 9, 16, 32, 40, 42
# The door, along the door side, and its step.
DOOR = (64, 76)
METAL = '#bcc1c6'
STRIPE = '#2e4d74'


def shift(i):
    """What is added to x to put fifth i inside a model's reach: its back end at -4."""
    return -4 - (-L + PART * i)


# ---------------------------------------------------------------------------------------------------------------- the pictures
def ribbed(seed, stripe=None):
    """Bright metal ribbed along its length: a darker rib every second row; and, if asked, the line's stripe across two rows."""
    im = new(METAL, seed, 3)
    for y in range(0, 16, 2):
        fill(im, 0, y, 16, y + 1, shade(METAL, -22))
    if stripe is not None:
        fill(im, 0, stripe, 16, stripe + 2, STRIPE)
        fill(im, 0, stripe + 2, 16, stripe + 3, shade(STRIPE, -30))
    return im


def window():
    """The band: dark glass in its upper rows, a pillar every block, the frame round it; the body in its lowest rows."""
    im = ribbed(48201)
    fill(im, 0, 1, 16, 10, '#20282e')
    for y in range(1, 10):
        for x in range(16):
            if (x + y) % 9 == 0:
                dot(im, x, y, '#3a4750')
    fill(im, 0, 0, 16, 1, shade(METAL, 18)); fill(im, 0, 10, 16, 11, shade(METAL, 18))
    fill(im, 0, 1, 2, 10, shade(METAL, 6)); dot(im, 1, 5, shade(METAL, -30))
    fill(im, 0, 12, 16, 14, STRIPE); fill(im, 0, 14, 16, 15, shade(STRIPE, -30))
    return im


def screen():
    """The windscreen over the front: glass with the frame and a pillar at the middle, the wipers parked under it; body below."""
    im = ribbed(48202)
    fill(im, 0, 1, 16, 11, '#2a343b')
    for i in range(6):
        dot(im, 3 + i, 9 - i // 2, '#56646d')
    fill(im, 0, 0, 16, 1, shade(METAL, 18)); fill(im, 0, 11, 16, 12, shade(METAL, 18)); fill(im, 0, 1, 1, 11, shade(METAL, 10))
    fill(im, 2, 10, 9, 11, '#111417')
    fill(im, 0, 13, 16, 15, STRIPE)
    return im


def rear():
    """The back of the band: the engine's louvres, the line's stripe."""
    im = ribbed(48203)
    for y in range(2, 10, 2):
        fill(im, 2, y, 14, y + 1, '#2b2f33')
    fill(im, 0, 12, 16, 14, STRIPE)
    return im


def front():
    """The front below the screen: the grille's bars."""
    im = ribbed(48204)
    for y in range(3, 14, 2):
        fill(im, 1, y, 15, y + 1, '#2b2f33')
    return im


def low_end():
    """The ends of the skirt: dark, behind the bumpers."""
    return new('#45494d', 48205, 3)


def roof():
    """The roof, a little brighter, with a seam every half block."""
    im = new(shade(METAL, 10), 48206, 3)
    for x in (0, 8):
        fill(im, x, 0, x + 1, 16, shade(METAL, -12))
    return im


def door():
    """The door, twelve pixels wide (a block's worth, as it stands at the start of one): two tall panes in a bright frame and a rubber
    line between them. It is a block high twice over, so each pane is two."""
    im = new(shade(METAL, 8), 48207, 2)
    fill(im, 1, 1, 5, 15, '#20282e'); fill(im, 7, 1, 11, 15, '#20282e')
    fill(im, 5, 0, 7, 16, '#2b2f33')
    for y in (4, 10):
        dot(im, 2, y, '#3a4750'); dot(im, 9, y + 1, '#3a4750')
    return im


def sign():
    """The sign over the screen, lit: a dark board with a line of amber cells, the place it is going."""
    im = new('#141618', 48208, 2)
    rows = ['.##.#..#.##..##.', '#...#..#.#.#.#..', '.#..#..#.##..##.', '..#.#..#.#.#.#..', '##...##..##..##.']
    for j, row in enumerate(rows):
        for i, c in enumerate(row):
            if c == '#':
                dot(im, i, 10 + j, "#f0b648")
    return im


def textures():
    return {DIR + name + '.png': im for name, im in (
        ('side', ribbed(48200, stripe=3)), ('window', window()), ('screen', screen()), ('rear', rear()), ('front', front()),
        ('low_end', low_end()), ('roof', roof()), ('roof_side', ribbed(48209)), ('door', door()), ('sign', sign()))}


# ---------------------------------------------------------------------------------------------------------------- the pieces
def body():
    p, lit = [], []
    # The skirt, between and outside the arches; the faces into an arch are the bus's underside.
    spans = [(-L, REAR - ARCH), (REAR + ARCH, FRONT_AXLE - ARCH), (FRONT_AXLE + ARCH, L)]
    for a, b in spans:
        box(p, [a, SKIRT, -W], [b, BODY, W], {'side': 'side', 'west': 'low_end' if a == -L else 'under', 'east': 'low_end' if b == L else 'under', 'under': 'under'})
    box(p, [-L, BODY, -W], [L, BAND, W], {'side': 'side', 'west': 'rear', 'east': 'front', 'under': 'under'})
    box(p, [-L, BAND, -W], [L, ROOF, W], {'side': 'window', 'west': 'rear', 'east': 'screen'})
    box(p, [-L, ROOF, -W], [L, TOP, W], {'side': 'roof_side', 'end': 'roof_side', 'top': 'roof'})
    box(p, [-60, TOP, -10], [-36, UNIT, 10], {'side': 'chrome', 'end': 'chrome', 'top': 'roof'})
    # the door, a little proud of the side, from the step to the top of the glass
    box(p, [DOOR[0], SKIRT, W], [DOOR[1], ROOF - 1, W + .4], {'south': 'door', 'end': 'chrome', 'top': 'chrome', 'under': 'chrome'})
    # the bumpers
    box(p, [L, 4, -W], [L + 1.5, 8, W], {'east': 'chrome', 'side': 'chrome', 'top': 'chrome', 'under': 'chrome'})
    box(p, [-L - 1.5, 4, -W], [-L, 8, W], {'west': 'chrome', 'side': 'chrome', 'top': 'chrome', 'under': 'chrome'})
    # the mirrors on their arms, forward of the screen at the height of the glass
    for s in (-1, 1):
        z0, z1 = (W + .1, W + 3) if s > 0 else (-W - 3, -W - .1)
        box(p, [L - 2, 29, z0], [L - 1, 30, z1], 'chrome')
        hz = (W + 2, W + 3.5) if s > 0 else (-W - 3.5, -W - 2)
        box(p, [L - 2.5, 23, hz[0]], [L - 1.5, 31, hz[1]], 'chrome')
    # lit: the lamps at the front, the tail lights behind, the sign over the screen
    for s in (-1, 1):
        z0, z1 = (12, 18) if s > 0 else (-18, -12)
        box(lit, [L, 11, z0], [L + .35, 15, z1], {'east': 'headlight', 'side': 'chrome', 'top': 'chrome', 'under': 'chrome'})
        box(lit, [-L - .35, 11, z0], [-L, 15, z1], {'west': 'taillight', 'side': 'chrome', 'top': 'chrome', 'under': 'chrome'})
    box(lit, [L, ROOF + 1, -12], [L + .3, TOP - 1, 12], {'east': 'sign', 'side': 'chrome', 'top': 'chrome', 'under': 'chrome'})
    return p, lit


def part(parts, i):
    """Fifth i of the bus's pieces, moved into a model's reach."""
    lo, hi = -L + PART * i, -L + PART * (i + 1)
    out = []
    for e in parts:
        cx = (e['from'][0] + e['to'][0]) / 2
        k = min(PARTS - 1, max(0, int((cx + L) // PART)))
        if k != i:
            continue
        dx = shift(i)
        f = dict(e)
        f['from'] = [round(e['from'][0] + dx, 3), round(e['from'][1] + DY, 3), round(e['from'][2] + DZ, 3)]
        f['to'] = [round(e['to'][0] + dx, 3), round(e['to'][1] + DY, 3), round(e['to'][2] + DZ, 3)]
        out.append(f)
    return out


def path(key):
    return NS + (CAR if key in BORROWED else DIR) + key


def model(parts, particle):
    keys = sorted({f['texture'][1:] for e in parts for f in e['faces'].values()})
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**{k: path(k) for k in keys}, 'particle': path(particle)}, 'elements': parts}


def models():
    p, lit = body(); m = {}
    for i in range(PARTS):
        m[f'part{i}'] = model(part(p, i), 'side')
        pieces = part(lit, i)
        m[f'lit{i}'] = model(pieces, 'sign') if pieces else model([], 'sign')
    return m


def files():
    return {f'models/{DIR}{key}.json': obj for key, obj in models().items()}


# ---------------------------------------------------------------------------------------------------------------- review
def review(out):
    """The bus from its door side, its other side, above, the front and behind, at three screen pixels to the pixel."""
    images = {k.split(DIR)[1][:-4]: v for k, v in textures().items()}
    for key in BORROWED:
        with Image.open(A / 'textures' / (CAR + key + '.png')) as im:
            images[key] = im.convert('RGBA')
    S = 3
    p, lit = body(); parts = p + lit

    def paste(page, ox, oy, tex, uv, x0, y0, x1, y1):
        im = images[tex]; u0, v0, u1, v1 = uv
        w, h = max(1, round((x1 - x0) * S)), max(1, round((y1 - y0) * S))
        for i in range(w):
            for j in range(h):
                u = u0 + (u1 - u0) * (i + .5) / w; v = v0 + (v1 - v0) * (j + .5) / h
                px = im.getpixel((min(15, max(0, int(u))), min(15, max(0, int(v)))))
                X, Y = ox + round(x0 * S) + i, oy - round(y1 * S) + j
                if 0 <= X < page.width and 0 <= Y < page.height and px[3]:
                    page.putpixel((X, Y), px)
    page = Image.new('RGBA', (1100, 760), (70, 84, 96, 255))
    wheels = []
    for row, (face, key, flip) in enumerate((('south', lambda e: e['to'][2], 1), ('north', lambda e: -e['from'][2], -1))):
        oy = 170 + row * 170
        for e in sorted((e for e in parts if face in e['faces']), key=key):
            f = e['faces'][face]
            x0, x1 = (e['from'][0], e['to'][0]) if flip > 0 else (-e['to'][0], -e['from'][0])
            paste(page, 290, oy, f['texture'][1:], f['uv'], x0, e['from'][1], x1, e['to'][1])
        for ax in (REAR, FRONT_AXLE):
            wheels.append((290 + flip * ax * S, oy - 8 * S))
    for cx, cy in wheels:
        for i in range(-24, 25):
            for j in range(-24, 25):
                if i * i + j * j <= 24 * 24:
                    page.putpixel((cx + i, cy + j), (25, 25, 27, 255) if i * i + j * j > 11 * 11 else (185, 188, 192, 255))
    for e in sorted((e for e in parts if 'up' in e['faces']), key=lambda e: e['to'][1]):
        f = e['faces']['up']; paste(page, 290, 470, f['texture'][1:], f['uv'], e['from'][0], -e['to'][2], e['to'][0], -e['from'][2])
    for e in sorted((e for e in parts if 'east' in e['faces']), key=lambda e: e['to'][0]):
        f = e['faces']['east']; paste(page, 650, 720, f['texture'][1:], f['uv'], -e['to'][2], e['from'][1], -e['from'][2], e['to'][1])
    for e in sorted((e for e in parts if 'west' in e['faces']), key=lambda e: -e['from'][0]):
        f = e['faces']['west']; paste(page, 900, 720, f['texture'][1:], f['uv'], e['from'][2], e['from'][1], e['to'][2], e['to'][1])
    page.save(out)


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
            assert (tex.split(DIR)[1] + '.png' in names) if DIR in tex else (A / 'textures' / (tex.split(':')[1] + '.png')).exists(), (key, tex)
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
        p = A / 'textures' / rel
        if check:
            with Image.open(p) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            p.parent.mkdir(parents=True, exist_ok=True); im.save(p, optimize=True)
    for rel, obj in data.items():
        p = A / rel
        if check:
            assert json.loads(p.read_text()) == flicker.settled(obj), rel
        else:
            p.parent.mkdir(parents=True, exist_ok=True); p.write_text(flicker.dumps(obj, indent=2) + '\n')
    print(('Verified' if check else 'Drew') + f" the list's bus: {len(images)} textures, {len(data)} models.")


if __name__ == '__main__':
    main()
