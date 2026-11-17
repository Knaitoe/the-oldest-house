"""Last Seen (0.4.667): a city park in the morning, and the municipal office at its edge where the archive of missing children is kept.

Draws, in native Minecraft pixels from fixed seeds: twelve materials (the park's path tiles, each of nine raised squares, and their curbs;
the beds' trodden earth; the office's render, its darker plinth, its floor of worn vinyl tiles and its ceiling of acoustic tile; the
avenue's asphalt and pavement; the houses' painted render; the bandstand's and fountain's stone; the plane trees' patched bark); and every
fitting of the last_seen_fitting block, its models facing north and turned by its facing:

- The office: COUNTER (a length of the front desk, plain or its end with the lifting flap), CARD (the typed card on it), CHAIR (an office
  chair), SHELF (a bay of steel shelving of folders spine out: three fills, one with a folder out, the top bay with archive boxes),
  POSTER and POSTER_B (sixteen posters, each a face and two lines of type; the first ten faces are taken from the painted skins of the ten
  who come back, so a poster and its child match), TABLE (the archive's long table, bare, with the day's pile, with its lamp), DESK (Nora's
  desk with the typewriter on it and its one drawer, locked, unlocked or open), LOUVRE (a louvred window), TUBE (a strip light).
- The park: RAILING (bars, and a post with its finial), GATE (a leaf, shut, chained or open), LAMP_POST (foot, shaft, globe lit or dark),
  BENCH (left end, middle, right end), FOUNTAIN (rim, rim corner, plinth, bowl), KIOSK (shuttered front, side with old bills), TRUNK (a
  silk-floss tree's foot, swollen belly, neck and fork, spined), LEAVES (a plane's canopy, a silk-floss's, a plane's thinner edge), PLAY
  (swing post, swing, slide ladder and chute, see-saw, low fence, frame bar, chains), BANDSTAND (post, balustrade, eave, step).
- The avenue: SHELTER (the bus shelter's glass, roof, sign and bench), SHUTTER (a house's window, shutter down or lit behind).

Writes the models and block states and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import math
import random
import flicker
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/last_seen/'
CHILDREN = ['rocio', 'tomas', 'micaela', 'brian', 'daiana', 'lautaro', 'abril', 'joel', 'yanina', 'santino']


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def mix(c1, c2, t):
    a, b = (rgb(c1) if isinstance(c1, str) else c1[:3]), (rgb(c2) if isinstance(c2, str) else c2[:3])
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def new(base=None, seed=0, spread=3, size=(16, 16)):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); w, h = im.size
    for y in range(max(0, y0), min(h, y1)):
        for x in range(max(0, x0), min(w, x1)):
            px[x, y] = shade(colour, 0, a) if isinstance(colour, str) else colour


def dot(im, x, y, colour, a=255):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        im.putpixel((x, y), shade(colour, 0, a) if isinstance(colour, str) else colour)


def line(im, x0, y0, x1, y1, colour):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), colour)


def darken(im, x, y, k):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        p = im.getpixel((x, y))
        if p[3]:
            im.putpixel((x, y), shade(p, k, p[3]))


# ------------------------------------------------------------------------------------------------ materials
def paving():
    # The park's path tiles, one to a block: the city's tile of nine raised squares (4, 5 and 4 pixels across), the seam between tiles
    # dark, the grooves inside lighter. Each square catches the light on its top and left and is shadowed on its bottom and right; the
    # middles are worn smooth, with grit in the grooves.
    im = new('#b6ae9f', 77001, 2)
    rnd = random.Random(77002)
    spans = ((1, 5), (6, 11), (12, 16))
    for i in range(16):
        dot(im, i, 0, '#837c6f'); dot(im, 0, i, '#837c6f')
        for g in (5, 11):
            dot(im, g, i, '#9c9588'); dot(im, i, g, '#9c9588')
    for x0, x1 in spans:
        for y0, y1 in spans:
            for x in range(x0, x1):
                dot(im, x, y0, '#c9c2b3')
                dot(im, x, y1 - 1, '#a59e90')
            for y in range(y0, y1):
                dot(im, x0, y, '#c4bdae')
                dot(im, x1 - 1, y, '#a9a294')
    for _ in range(9):
        g = rnd.choice((0, 5, 11))
        if rnd.random() < .5:
            dot(im, g, rnd.randrange(16), '#77705f')
        else:
            dot(im, rnd.randrange(16), g, '#77705f')
    return im


def curb():
    im = new('#8f8e89', 77010, 4)
    rnd = random.Random(77011)
    for _ in range(26):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#6f6e6a' if rnd.random() < .5 else '#adaca6')
    for x in range(16):
        dot(im, x, 0, '#a9a8a2'); dot(im, x, 1, '#9e9d97')
    for y in range(16):
        dot(im, 0, y, '#77766f')
    return im


def earth():
    im = new('#776045', 77020, 5)
    rnd = random.Random(77021)
    for _ in range(18):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#8e7a5b'); dot(im, x + 1, y, '#5c4a35')
    for _ in range(6):
        x, y = rnd.randrange(15), rnd.randrange(15)
        dot(im, x, y, '#9a9283')
    return im


def render():
    # The office's render: a warm, chalky off-white, rollered on years ago; a hairline crack and the grey of the city in the texture.
    im = new('#d6cfbf', 77030, 3)
    rnd = random.Random(77031)
    for _ in range(10):
        darken(im, rnd.randrange(16), rnd.randrange(16), -9)
    for i, (x, y) in enumerate(((3, 2), (4, 3), (4, 4), (5, 5), (5, 6))):
        darken(im, x, y, -24)
    return im


def plinth():
    im = new('#8b867c', 77040, 3)
    for x in range(16):
        dot(im, x, 2, '#6f6b63'); dot(im, x, 3, '#9a958b')
    rnd = random.Random(77041)
    for _ in range(5):
        x = rnd.randrange(16)
        for y in range(4, 4 + rnd.randint(2, 6)):
            darken(im, x, y, -10)
    return im


def floor():
    # Municipal vinyl tiles, two shades laid alternately, the seams dark, a scuff or two where the chairs go.
    im = new(None)
    rnd = random.Random(77050)
    for ty in (0, 8):
        for tx in (0, 8):
            base = '#a4a898' if (tx + ty) % 16 == 0 else '#969b8b'
            for y in range(8):
                for x in range(8):
                    im.putpixel((tx + x, ty + y), shade(base, rnd.randint(-3, 3)))
            for i in range(8):
                dot(im, tx + i, ty, '#7f8476'); dot(im, tx, ty + i, '#7f8476')
            for _ in range(3):
                dot(im, tx + rnd.randint(1, 7), ty + rnd.randint(1, 7), shade(base, 12))
    line(im, 9, 12, 13, 11, '#8a8f80')
    return im


def ceiling():
    im = new('#d9d5c8', 77060, 2)
    rnd = random.Random(77061)
    for _ in range(30):
        dot(im, rnd.randrange(1, 15), rnd.randrange(1, 15), '#b9b4a7')
    for i in range(16):
        dot(im, i, 0, '#a7a397'); dot(im, 0, i, '#a7a397')
    return im


def asphalt():
    im = new('#3c3d3f', 77070, 3)
    rnd = random.Random(77071)
    for _ in range(36):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#4d4e51' if rnd.random() < .6 else '#2e2f31')
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#6b6b68')
    return im


def pavement():
    im = new('#9e9a91', 77080, 3)
    for i in range(16):
        dot(im, i, 0, '#7e7a72'); dot(im, 0, i, '#7e7a72')
    rnd = random.Random(77081)
    for _ in range(14):
        darken(im, rnd.randrange(1, 16), rnd.randrange(1, 16), rnd.choice((-8, 7)))
    line(im, 10, 3, 13, 7, '#86827a')
    return im


def house():
    # The houses' painted render, dusty and stained under the sills.
    im = new('#c7a37c', 77090, 3)
    rnd = random.Random(77091)
    for _ in range(4):
        x = rnd.randrange(16)
        for y in range(rnd.randint(0, 6), 16):
            darken(im, x, y, -6)
    for _ in range(8):
        darken(im, rnd.randrange(16), rnd.randrange(16), 6)
    return im


def house_b():
    # A second house's render, limewashed: paler and chalkier, the wash worn thin at the foot of the wall.
    im = new('#cfc6b0', 77092, 3)
    rnd = random.Random(77093)
    for _ in range(10):
        darken(im, rnd.randrange(16), rnd.randrange(10, 16), -7)
    for _ in range(5):
        x = rnd.randrange(16)
        for y in range(rnd.randint(8, 13), 16):
            darken(im, x, y, -4)
    return im


def house_c():
    # A third house's render, painted and faded unevenly in patches where the sun reaches it longest.
    im = new('#b9917a', 77094, 3)
    rnd = random.Random(77095)
    for _ in range(3):
        cx, cy = rnd.randrange(16), rnd.randrange(16)
        for y in range(cy - 2, cy + 3):
            for x in range(cx - 3, cx + 4):
                darken(im, x % 16, y % 16, 7)
    for _ in range(4):
        x = rnd.randrange(16)
        for y in range(rnd.randint(0, 6), 16):
            darken(im, x, y, -5)
    return im


def road_line():
    # The avenue's centre line: a dash of road paint across the asphalt, worn through where the wheels cross it.
    im = asphalt()
    rnd = random.Random(77075)
    for x in range(16):
        for y in (7, 8):
            if rnd.random() > .12:
                dot(im, x, y, '#d9d3bc' if rnd.random() < .7 else '#c4bea7')
    return im


def crossing():
    # A stripe of the crossing at the park's gate: the paint worn grey in the middle where people step.
    im = new('#d6d0ba', 77076, 3)
    rnd = random.Random(77077)
    for _ in range(30):
        x, y = rnd.randrange(16), rnd.randrange(16)
        dot(im, x, y, '#4a4b4d' if abs(y - 8) < 5 and rnd.random() < .6 else '#bdb7a2')
    for x in range(16):
        dot(im, x, 0, '#a9a491'); dot(im, x, 15, '#a9a491')
    return im


def stone():
    # Coursed stone, blocks of eight by four, staggered, the mortar sunk.
    im = new('#a29c8d', 77100, 4)
    for y in (0, 4, 8, 12):
        for x in range(16):
            dot(im, x, y, '#837d70')
        off = 0 if (y // 4) % 2 == 0 else 4
        for yy in range(y, y + 4):
            dot(im, off, yy, '#837d70'); dot(im, off + 8, yy, '#837d70')
        for x in range(16):
            darken(im, x, y + 1, 6)
    return im


def bark():
    # A plane tree's bark: shed in plates, so the trunk is patched with olive, cream and grey.
    im = new('#8b8a6c', 77110, 4)
    rnd = random.Random(77112)
    for _ in range(9):
        cx, cy, r = rnd.randrange(16), rnd.randrange(16), rnd.randint(2, 3)
        c = rnd.choice(['#cbc4a1', '#6e6c57', '#a7a585', '#b8b393'])
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r - 1, cx + r + 2):
                if (x - cx) ** 2 * .6 + (y - cy) ** 2 <= r * r:
                    dot(im, x % 16, y % 16, shade(c, rnd.randint(-3, 3)))
    return im


def materials():
    return {'paving': paving(), 'curb': curb(), 'earth': earth(), 'render': render(), 'plinth': plinth(), 'floor': floor(),
            'ceiling': ceiling(), 'asphalt': asphalt(), 'pavement': pavement(), 'house': house(), 'stone': stone(), 'bark': bark(),
            'house_b': house_b(), 'house_c': house_c(), 'road_line': road_line(), 'crossing': crossing()}


# ------------------------------------------------------------------------------------------------ the office
def laminate():
    # The counter's front: a wood-grain laminate panel, the grain in long streaks, a dark kick plate at the foot.
    im = new('#8a6a4b', 77200, 2)
    rnd = random.Random(77201)
    for y in range(16):
        if rnd.random() < .35:
            for x in range(16):
                darken(im, x, y, -6)
    for x in range(16):
        for y in (13, 14, 15):
            dot(im, x, y, '#463b31')
        dot(im, x, 12, '#5b4c3e')
    return im


def formica():
    im = new('#c7bfae', 77210, 2)
    rnd = random.Random(77211)
    for _ in range(20):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#b3ab9b', '#d6cfbf']))
    return im


def counter_edge():
    im = new('#b9b1a0', 77220, 1)
    for x in range(16):
        dot(im, x, 0, '#d4ccbb'); dot(im, x, 15, '#8f8878')
    return im


def flap():
    im = formica()
    for x in range(16):
        dot(im, x, 1, '#8e8778')
    for x in (3, 12):
        dot(im, x, 1, '#d8d0c0')
    return im


def card():
    # The typed card: a heading in capitals, the lines under it, a corner turned.
    im = new('#e7e1cf', 77230, 2)
    for x in range(3, 13):
        dot(im, x, 2, '#3c3c3e') if x % 3 else None
    for y in (5, 7, 9, 11, 13):
        n = 11 if y != 13 else 6
        for x in range(2, 2 + n):
            if (x * 7 + y) % 5:
                dot(im, x, y, '#58585c')
    dot(im, 15, 0, '#c9c3b1'); dot(im, 14, 0, '#d4cebc'); dot(im, 15, 1, '#d4cebc')
    return im


def fabric():
    im = new('#566270', 77240, 3)
    for y in range(0, 16, 2):
        for x in range(16):
            if (x + y // 2) % 2:
                darken(im, x, y, -6)
    return im


def tube_steel():
    im = new('#8b8f93', 77250, 2)
    for x in range(16):
        dot(im, x, 0, '#b3b7ba'); dot(im, x, 1, '#a0a4a8'); dot(im, x, 15, '#666a6e')
    return im


def shelf_upright():
    # The shelving's upright: enamelled steel, punched every second pixel for the shelf clips.
    im = new('#7a888c', 77260, 2)
    for y in range(1, 16, 2):
        dot(im, 7, y, '#3c4446'); dot(im, 8, y, '#3c4446')
    for y in range(16):
        dot(im, 0, y, '#93a1a5'); dot(im, 15, y, '#5d6a6e')
    return im


SPINES = ['#c4ad80', '#b39a6c', '#cdbb92', '#7f8f9a', '#8a9a7c', '#9a6e58', '#c2a878', '#6f7c86']


def shelf_face(seed, gap=False):
    # Folders spine out on two shelves to the bay: buff card folders and a few box files in faded blue, green and red-brown, each
    # spine a shade of its own age, a shadow between neighbours, a written label on most and a finger hole on the box files. The shelf
    # lips in steel with a shadow under each. A bay with a folder out has a dark slot where it stood.
    im = new(None)
    rnd = random.Random(seed)
    for row, (top, bottom) in enumerate(((1, 7), (9, 15))):
        x = 0
        out = rnd.randrange(3, 12) if gap and row == 0 else -1
        while x < 16:
            box = rnd.random() < .25
            w = 3 if box else rnd.choice((2, 2, 3))
            c = rnd.choice(SPINES[3:] if box else SPINES[:3] + SPINES[6:7])
            lean = 0 if box else rnd.choice((0, 0, 1))
            taken = out >= x and out < x + w
            for xx in range(x, min(16, x + w)):
                for y in range(top + lean, bottom):
                    if taken:
                        im.putpixel((xx, y), shade('#262220', rnd.randint(-2, 2)))
                    else:
                        edge = -22 if xx == x else (8 if xx == x + 1 else 0)
                        im.putpixel((xx, y), shade(c, rnd.randint(-3, 3) + edge))
                if lean and not taken:
                    dot(im, xx, top, '#2e2a26')
            if not taken and w >= 2:
                lx = x + 1
                if box:
                    fill(im, x + 1, top + 1, min(16, x + w), top + 3, '#e4dcc8')
                    dot(im, x + 1 + (w > 2), bottom - 2, '#2a2420')
                elif rnd.random() < .7:
                    dot(im, lx, top + 2 + lean, '#ece4cf'); dot(im, lx, top + 3 + lean, '#e4dcc6')
                    if rnd.random() < .5:
                        dot(im, lx, top + 3 + lean, '#5a4e3c')
            x += w
        for xx in range(16):
            dot(im, xx, bottom, '#a2aeb2'); dot(im, xx, top - 1, '#36322e')
    for xx in range(16):
        dot(im, xx, 0, '#36322e')
    return im


def boxes():
    im = new('#a6895d', 77280, 3)
    for x in range(16):
        dot(im, x, 0, '#3a3632'); dot(im, x, 15, '#9aa6aa'); dot(im, x, 8, '#7e6644')
    for x in (7, 8):
        for y in range(16):
            dot(im, x, y, '#7e6644')
    for ox in (1, 10):
        fill(im, ox, 3, ox + 5, 6, '#e1d9c5')
        line(im, ox + 1, 4, ox + 3, 4, '#6a6458')
        fill(im, ox + 1, 11, ox + 4, 13, '#5a4834')
    return im


def face_of(look):
    """The face of one of the ten, drawn at eight pixels from their painted look: their skin, their hair in its own cut, their eyes and,
    on the one who wears them, their glasses. It is the photograph the family gave, taken before: there is no earth on it."""
    return drawn_face(0, look.skin, look.hair, {'long': 0, 'braids': 0, 'ponytail': 1, 'bun': 1, 'coils': 3, 'close': 4}.get(look.cut, 2),
                      look.eyes, look.glasses)


def drawn_face(seed, skin=None, hair=None, style=None, eyes=None, glasses=False):
    # A face drawn straight at eight pixels: skin, hair, two eyes, a mouth. The six nobody brings are drawn from a seed; the ten who
    # come back from their own looks (face_of).
    rnd = random.Random(seed)
    skin = skin or rnd.choice(['#d8ad8c', '#c49070', '#a7765a', '#e0bea0', '#8f6448'])
    hair = hair or rnd.choice(['#2a2018', '#4a3424', '#6a4a2a', '#1e1a18', '#8a6438'])
    style = rnd.randrange(3) if style is None else style
    eyes = eyes or rnd.choice(['#3a2e26', '#4a5a4a', '#2e2a26', '#4a4038'])
    im = new(skin, seed or 77699, 2, (8, 8))
    for x in range(8):
        dot(im, x, 0, hair)
        if style != 4:
            dot(im, x, 1, hair)
    if style == 0:
        for y in range(2, 8):
            dot(im, 0, y, hair); dot(im, 7, y, hair)
    elif style == 1:
        for y in range(2, 5):
            dot(im, 0, y, hair); dot(im, 7, y, hair)
    elif style == 2:
        for x in range(1, 4):
            dot(im, x, 2, hair)
        dot(im, 0, 2, hair); dot(im, 7, 2, hair)
    elif style == 3:
        for x in range(8):
            dot(im, x, 2 if x in (0, 1, 6, 7) else 1, hair)
        dot(im, 2, 0, shade(hair, 22)); dot(im, 5, 1, shade(hair, 22))
    dot(im, 2, 4, '#f2eee6'); dot(im, 3, 4, eyes); dot(im, 5, 4, eyes); dot(im, 6, 4, '#f2eee6')
    if glasses:
        # two rims over the eyes, the bridge between them and the arms at the temples, thin enough that the eyes still show
        for x in (2, 3, 5, 6):
            dot(im, x, 3, '#5a5a60')
        for x in (1, 4, 7):
            dot(im, x, 4, '#5a5a60')
    dot(im, 3, 6, shade(skin, -30)); dot(im, 4, 6, shade(skin, -30))
    return im


def poster(face, seed, faded=False):
    # A poster on the partition: paper going a little yellow, a photograph of a face, a line of capitals over it and two lines of type
    # under it (where, and on what day), and a pin at the top. The six older ones have been up so long the paper has gone the colour of
    # weak tea and foxed, the photograph has washed nearly into it (only the hair's line and the shadow of the eyes still show), the
    # type is broken and pale, and a bottom corner has torn away from its pin.
    paper = '#d9cba6' if faded else '#e6dfcb'
    im = new(paper, seed, 3 if faded else 2)
    for x in range(16):
        dot(im, x, 0, shade(paper, -14)); dot(im, x, 15, shade(paper, -18))
    for y in range(16):
        dot(im, 0, y, shade(paper, -10)); dot(im, 15, y, shade(paper, -20))
    ink, ink2 = ('#9a9488', '#aaa396') if faded else ('#2f2f33', '#55555a')
    for x in range(3, 13):
        if x != 8 and not (faded and (x * 7 + seed) % 3 == 0):
            dot(im, x, 2, ink)
    fill(im, 3, 3, 13, 4, shade(paper, -8))
    # the photograph, a pixel of border round it
    fill(im, 3, 4, 13, 14, shade(paper, -26 if not faded else -10))
    for y in range(8):
        for x in range(8):
            p = face.getpixel((x, y))
            if p[3] < 40:
                p = (200, 196, 186, 255)
            # printed photographs go flat and warm: fold toward the paper a little; the old ones almost all the way
            q = mix(p, '#d8c8a8', .18) if not faded else mix(p, paper, .72)
            im.putpixel((4 + x, 4 + y), q)
    # the lines of type, under the photograph's foot
    for x in range(2, 14):
        if (x * 5 + seed) % 4 and not (faded and (x + seed) % 3 == 0):
            dot(im, x, 13, ink2)
    for x in range(4, 12):
        if (x * 3 + seed) % 3 and not (faded and x % 2):
            dot(im, x, 14, shade(ink2, 14))
    if faded:
        # foxing: a few brown spots where the damp got in, and the torn corner, the partition's render showing through
        rnd = random.Random(seed)
        for _ in range(5):
            x, y = rnd.randrange(1, 15), rnd.randrange(1, 15)
            dot(im, x, y, '#b39a6c', 255)
        corner = rnd.choice(((14, 14), (1, 14)))  # inside the model's crop (pixels 1 to 14)
        cx, cy = corner
        for dx, dy in ((0, 0), (0, -1), (1 if cx == 1 else -1, 0)):
            im.putpixel((cx + dx, cy + dy), (0, 0, 0, 0))
        dot(im, 7, 1, '#6a4a3a'); dot(im, 8, 1, '#7a4a3a')
    else:
        dot(im, 7, 1, '#8a3a2e'); dot(im, 8, 1, '#b04a3a')
    return im


def wood(seed=77300, base='#6c4e37'):
    im = new(base, seed, 3)
    rnd = random.Random(seed + 1)
    for y in range(16):
        if rnd.random() < .3:
            for x in range(16):
                darken(im, x, y, -7)
    return im


def drawer_front(state):
    im = wood(77310, '#6a4c35')
    for x in range(16):
        dot(im, x, 0, '#4a3524'); dot(im, x, 15, '#4a3524')
    for x in range(5, 11):
        dot(im, x, 6, '#a7a39a')
    dot(im, 5, 7, '#7a766d'); dot(im, 10, 7, '#7a766d')
    dot(im, 8, 10, '#1e1a16')
    if state >= 1:
        dot(im, 8, 9, '#c9a64a'); dot(im, 8, 10, '#a8862e'); dot(im, 9, 9, '#c9a64a')
    return im


def typewriter():
    # The typewriter: a dark body, the keys in rows, the platen, a sheet in it.
    im = new('#3a3c3a', 77320, 2)
    for y in (9, 11, 13):
        for x in range(2 + (y - 9) // 2, 14 - (y - 9) // 2, 2):
            dot(im, x, y, '#c9c4b4')
    for x in range(1, 15):
        dot(im, x, 6, '#1e201e'); dot(im, x, 7, '#5a5c5a')
    return im


def sheet():
    im = new('#efeadb', 77330, 1)
    for y in (3, 5, 7, 9):
        for x in range(2, 14):
            if (x + y) % 4:
                dot(im, x, y, '#6a6a6e')
    return im


def lamp_shade():
    im = new('#3f5a45', 77340, 2)
    for x in range(16):
        dot(im, x, 0, '#5c7a62'); dot(im, x, 15, '#2a3a2e')
    return im


def lamp_brass():
    im = new('#a8883e', 77341, 3)
    for x in range(16):
        dot(im, x, 0, '#d0b060')
    return im


def louvre():
    # A louvred window: a metal frame, slats of glass tilted half open, the light catching their edges.
    im = new('#bdb9ad', 77350, 1)
    for y in range(1, 15):
        for x in range(1, 15):
            band = y % 3
            im.putpixel((x, y), shade('#8fa1a9' if band == 0 else '#6d7f88' if band == 1 else '#33404a', 0))
    for y in range(1, 15, 3):
        for x in range(1, 15):
            dot(im, x, y, '#c9d6db')
    return im


def tube_light():
    im = new('#f1f0e6', 77360, 1)
    for x in range(16):
        dot(im, x, 0, '#c0c0b8'); dot(im, x, 15, '#c0c0b8')
    return im


# ------------------------------------------------------------------------------------------------ the park
IRON = '#2a2c2e'


def iron():
    im = new(IRON, 77400, 3)
    rnd = random.Random(77401)
    for _ in range(5):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#5a3c2a')
    for x in range(16):
        dot(im, x, 0, '#44484b')
    return im


def railing_face():
    # Bars with spear tops, a rail near the foot and one under the spears, painted iron with the odd rust bloom.
    im = new(None)
    for x in range(1, 16, 3):
        for y in range(2, 16):
            dot(im, x, y, IRON); dot(im, x + 1, y, '#3a3d40')
        dot(im, x, 1, IRON); dot(im, x + 1, 1, IRON); dot(im, x, 0, '#44484b')
    for x in range(16):
        dot(im, x, 3, '#3a3d40'); dot(im, x, 4, IRON); dot(im, x, 13, '#3a3d40'); dot(im, x, 14, IRON)
    dot(im, 7, 9, '#6a4632'); dot(im, 13, 6, '#6a4632')
    return im


def gate_face(chained):
    im = new(None)
    for x in range(16):
        for y in (0, 1, 14, 15):
            dot(im, x, y, IRON)
    for y in range(16):
        dot(im, 0, y, IRON); dot(im, 15, y, IRON)
    for x in range(3, 15, 3):
        for y in range(2, 14):
            dot(im, x, y, '#3a3d40')
    # a scroll in the middle
    for i in range(12):
        a = i / 12 * math.tau
        dot(im, round(7.5 + 3 * math.cos(a)), round(7.5 + 3 * math.sin(a)), IRON)
    if chained:
        for y in range(4, 13):
            dot(im, 14 if y % 2 else 15, y, '#a2a39f')
            dot(im, 1 if y % 2 else 0, y, '#a2a39f')
        fill(im, 12, 9, 15, 13, '#b48a34')
        dot(im, 13, 8, '#8e8e8a'); dot(im, 14, 8, '#8e8e8a'); dot(im, 13, 11, '#3a2e1a')
    return im


def lamp_iron():
    im = new('#2c3032', 77420, 2)
    for y in range(16):
        dot(im, 4, y, '#40464a'); dot(im, 11, y, '#1c1f20')
    return im


def globe(lit):
    im = new('#f3eedf' if lit else '#9a968a', 77430 + lit, 2)
    if lit:
        for y in range(5, 11):
            for x in range(5, 11):
                dot(im, x, y, '#fffaea')
    else:
        for y in range(16):
            darken(im, 12, y, -10); darken(im, 13, y, -14)
    return im


def slats():
    im = new('#8a5d3a', 77440, 3)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#4e3420')
    rnd = random.Random(77441)
    for _ in range(8):
        darken(im, rnd.randrange(16), rnd.randrange(16), -8)
    return im


def fountain_top():
    im = stone()
    for x in range(16):
        dot(im, x, 0, '#b6b0a2'); dot(im, x, 15, '#6f6a5f')
    for x in range(3, 13):
        darken(im, x, 12, -10); darken(im, x, 13, -14)
    return im


def basin():
    # The fountain's dry basin: stone gone dark where the water stood, leaves blown into the corner.
    im = new('#7a7568', 77450, 4)
    rnd = random.Random(77451)
    for _ in range(9):
        x, y = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice(['#8a6a3a', '#a07a42', '#6a5a32'])
        dot(im, x, y, c); dot(im, x + 1, y, shade(c, -10))
    return im


def kiosk_shutter():
    im = new('#3d5a44', 77460, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#2e4634')
            dot(im, x, y + 1, '#4a6a52')
    for x in range(16):
        dot(im, x, 15, '#22342a')
    fill(im, 7, 13, 9, 15, '#8a8e86')
    return im


def kiosk_side():
    # The kiosk's side, the same green under bills pasted over bills: a concert, a lost dog, a room to let, all faded to the one tone.
    im = new('#3d5a44', 77470, 2)
    rnd = random.Random(77471)
    for _ in range(5):
        x0, y0 = rnd.randrange(0, 11), rnd.randrange(0, 11)
        w, h = rnd.randint(4, 6), rnd.randint(4, 6)
        c = rnd.choice(['#d8cfb4', '#c9b8a4', '#b8c0b0', '#d4c09a'])
        fill(im, x0, y0, x0 + w, y0 + h, c)
        for yy in range(y0 + 1, y0 + h - 1, 2):
            line(im, x0 + 1, yy, x0 + w - 2, yy, shade(c, -45))
        darken(im, x0 + w - 1, y0, -30)
    return im


def kiosk_roof():
    im = new('#2e4634', 77480, 2)
    for x in range(16):
        dot(im, x, 0, '#5a7a62')
    return im


def silk_bark(foot=False):
    # A silk-floss trunk: green-grey bark, and on it the thorns, each a cone with its lit side and its point.
    im = new('#6c7c5e', 77490 + foot, 4)
    rnd = random.Random(77495 + foot)
    for y in range(16):
        if rnd.random() < .3:
            for x in range(16):
                darken(im, x, y, 5)
    for _ in range(11 if not foot else 7):
        x, y = rnd.randrange(1, 15), rnd.randrange(2, 15)
        dot(im, x, y, '#4a5640'); dot(im, x, y - 1, '#b8b49a'); dot(im, x + 1, y, '#3a4432'); dot(im, x, y - 2, '#d8d4ba')
    if foot:
        for x in range(16):
            for y in (13, 14, 15):
                darken(im, x, y, -12)
    return im


def leaves(kind):
    # Canopies, drawn for cutout. A plane's: big lobed leaves in clusters, two greens and the light coming through. A silk-floss's:
    # smaller leaves, sparser, with its flowers in among them. The plane's edge: the same, thinner.
    im = new(None)
    rnd = random.Random(77500 + kind)
    if kind == 1:
        greens = ['#5f7a40', '#4e6834', '#71904c']
        n = 26
    else:
        greens = ['#5c7a38', '#4a662c', '#7a9a48', '#6a8a3e']
        n = 34 if kind == 0 else 18
    for _ in range(n):
        cx, cy = rnd.randrange(16), rnd.randrange(16)
        c = rnd.choice(greens)
        size = 2 if kind != 1 else 1
        for dy in range(-size, size + 1):
            for dx in range(-size, size + 1):
                if abs(dx) + abs(dy) <= size + (kind != 1):
                    dot(im, (cx + dx) % 16, (cy + dy) % 16, shade(c, -6 if dy > 0 else 4 if dy < 0 else 0))
    if kind == 1:
        for _ in range(9):
            x, y = rnd.randrange(16), rnd.randrange(16)
            dot(im, x, y, '#d98aa8'); dot(im, (x + 1) % 16, y, '#c06a8a'); dot(im, x, (y + 1) % 16, '#e8a8c0')
    return im


def paint(c, seed):
    im = new(c, seed, 3)
    for x in range(16):
        dot(im, x, 0, shade(c, 18))
    rnd = random.Random(seed + 1)
    for _ in range(3):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#6a4a34')
    return im


def chain():
    im = new(None)
    for y in range(16):
        dot(im, 7 if y % 4 < 2 else 8, y, '#9a9b97')
        if y % 4 == 0:
            dot(im, 8, y, '#6a6b67')
    return im


def rubber():
    im = new('#2b2b2c', 77520, 2)
    for x in range(16):
        dot(im, x, 0, '#444446')
    return im


def chute():
    im = new('#b6babe', 77530, 2)
    for x in range(16):
        dot(im, x, 7, '#e2e5e8'); dot(im, x, 8, '#d2d6d9')
    return im


def plank():
    return wood(77540, '#9a7a52')


def lattice():
    # The bandstand's balustrade: an iron lattice of diagonals between two rails.
    im = new(None)
    for x in range(16):
        dot(im, x, 0, IRON); dot(im, x, 1, '#3a3d40'); dot(im, x, 15, IRON)
    for i in range(16):
        for k in (0, 8):
            dot(im, (i + k) % 16, 2 + (i % 13), '#33363a')
            dot(im, (15 - i + k) % 16, 2 + (i % 13), '#33363a')
    return im


def fret():
    # The bandstand's valance: a cast-iron fret hung under the eave. A rail along the top, arches springing from little posts, and a
    # pendant dropping under each arch, drawn for cutout and painted the balustrade's iron.
    im = new(None)
    hi, lo = '#5a6062', '#33363a'
    for x in range(16):
        dot(im, x, 0, hi); dot(im, x, 1, lo)
    for x0 in range(0, 16, 8):
        for y in range(2, 6):
            dot(im, x0, y, lo)
        for dx, y in ((1, 2), (2, 3), (3, 3), (4, 3), (5, 3), (6, 2)):
            dot(im, x0 + dx, y, hi if y == 3 else lo)
        dot(im, x0 + 1, 4, lo); dot(im, x0 + 6, 4, lo)
        for y in (4, 5):
            dot(im, x0 + 3, y, lo); dot(im, x0 + 4, y, lo)
        dot(im, x0 + 3, 6, hi); dot(im, x0 + 4, 6, lo)
    return im


def glass():
    im = new(None)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), (178, 196, 202, 92))
    for i in range(5):
        dot(im, 3 + i, 9 - i, '#e6eef0', 140)
    for x in range(16):
        dot(im, x, 0, '#8a9094'); dot(im, x, 15, '#8a9094')
    for y in range(16):
        dot(im, 0, y, '#8a9094'); dot(im, 15, y, '#8a9094')
    return im


def roof_sheet():
    im = new('#b8bcb4', 77560, 2)
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#9a9e96')
    return im


def stop_sign():
    # The stop's sign: a round plate on its pole, a bus drawn on it.
    im = new(None)
    for y in range(16):
        for x in range(16):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 49:
                im.putpixel((x, y), shade('#d8d4c8', 0))
            if 36 < (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 49:
                im.putpixel((x, y), shade('#3a5a8a', 0))
    fill(im, 4, 6, 12, 10, '#3a5a8a')
    for x in (5, 7, 9):
        dot(im, x, 7, '#d8e4ec')
    dot(im, 5, 10, '#222222'); dot(im, 10, 10, '#222222')
    return im


def perforated():
    im = new('#7c8288', 77570, 2)
    for y in range(1, 16, 2):
        for x in range(1, 16, 2):
            dot(im, x, y, '#4a5056')
    return im


def shutter_face(lit, part='one'):
    # A house's window: a roller blind of wooden slats in its box. Shut by day; at night pulled half up on a lit room. One block, or the
    # low and top halves of a tall window: the box is at the top of the top half, the blind's bar at the foot of the low half.
    im = new('#8b7a5c', 77580 + lit + 2 * ('one', 'low', 'top').index(part), 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#6e5f45')
    for y in range(16):
        dot(im, 0, y, '#c9c0ad'); dot(im, 15, y, '#c9c0ad')
    if part in ('one', 'top'):
        for x in range(1, 15):
            dot(im, x, 0, '#c9c0ad'); dot(im, x, 1, '#b9b09c'); dot(im, x, 2, '#5a4a34')
    if part in ('one', 'low'):
        for x in range(1, 15):
            dot(im, x, 15, '#b9b09c'); dot(im, x, 14, '#7a6a4e')
    if lit and part == 'one':
        for y in range(9, 15):
            for x in range(1, 15):
                dot(im, x, y, '#f0c878' if y > 10 else '#d8a85a')
        for x in range(1, 15):
            dot(im, x, 8, '#5a4a34')
    if lit and part == 'low':
        # The blind is up out of this half: the room's light, and the window's cross of glazing bars against it.
        for y in range(0, 15):
            for x in range(1, 15):
                dot(im, x, y, '#f0c878' if y > 2 else '#d8a85a')
        for y in range(0, 15):
            dot(im, 7, y, '#4a3c2a'); dot(im, 8, y, '#6a5a40')
        for x in range(1, 15):
            dot(im, x, 6, '#4a3c2a')
    if lit and part == 'top':
        for x in range(1, 15):
            dot(im, x, 15, '#5a4a34'); dot(im, x, 14, '#7a6a4e')
    return im


def sill():
    # The stone of a window's sill and its head: the upper edge catching the light, a joint between its stones, its underside dark.
    im = new('#aca696', 77590, 2)
    for x in range(16):
        dot(im, x, 0, '#c8c2b2'); dot(im, x, 1, '#bcb6a6'); dot(im, x, 15, '#7c776a'); dot(im, x, 14, '#8a8577')
    for y in range(16):
        dot(im, 5, y, '#8c877a'); dot(im, 11, y, '#8c877a')
    return im


def fascia():
    # The bandstand's eave: painted boards under the tiles, their joints dark, the paint flaking at the edge.
    im = new('#cdc4aa', 77595, 3)
    rnd = random.Random(77596)
    for y in (0, 5, 10, 15):
        for x in range(16):
            dot(im, x, y, '#9a927a')
    for _ in range(7):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8a7a62')
    return im


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rotation=None):
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
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, cutout=True):
    def where(v):
        return v if ':' in v else P + v
    m = {'ambientocclusion': False, 'textures': {**{k: where(v) for k, v in textures.items()}, 'particle': where(particle)}, 'elements': elements}
    if cutout:
        m['render_type'] = 'minecraft:cutout'
    return m


def glow(elements, light):
    for e in elements:
        e['neoforge_data'] = {'block_light': light, 'sky_light': 0}


def counter_model(stage):
    t = {'f': 'laminate', 't': 'formica', 'e': 'counter_edge', 'k': 'flap'}
    top = 'k' if stage == 1 else 't'
    return model([element([0, 0, 0], [16, 15, 16], {'north': 'e', 'south': 'f', 'east': 'f', 'west': 'f', 'down': 'f'}),
                  element([0, 15, 0], [16, 16, 16], {'up': top, 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'})], t, 'laminate', cutout=False)


def card_model():
    return model([element([4, 0, 5], [12, .25, 11], {'up': 'c', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'},
                          uv={'up': [0, 0, 16, 12]})], {'c': 'card'}, 'card')


def chair_model():
    t = {'s': 'fabric', 'm': 'tube_steel'}
    e = [element([3, 8, 3], [13, 9.5, 13], {'all': 's'}),
         element([3, 9.5, 11.5], [13, 18, 13], {'all': 's'}),
         element([3.5, 0, 3.5], [4.5, 8, 4.5], {'all': 'm'}), element([11.5, 0, 3.5], [12.5, 8, 4.5], {'all': 'm'}),
         element([3.5, 0, 11.5], [4.5, 8, 12.5], {'all': 'm'}), element([11.5, 0, 11.5], [12.5, 8, 12.5], {'all': 'm'}),
         element([3.5, 2, 4.5], [4.5, 3, 11.5], {'all': 'm'}), element([11.5, 2, 4.5], [12.5, 3, 11.5], {'all': 'm'})]
    return model(e, t, 'tube_steel')


def shelf_model(stage):
    face = {0: 'shelf_a', 1: 'shelf_b', 2: 'shelf_c', 3: 'shelf_out', 4: 'boxes'}[stage]
    t = {'u': 'shelf_upright', 'f': face}
    return model([element([0, 0, 0], [16, 16, 16], {'north': 'f', 'south': 'u', 'east': 'u', 'west': 'u', 'up': 'u', 'down': 'u'})], t, 'shelf_upright', cutout=False)


def poster_model(index):
    return model([element([1, 1, 15.6], [15, 15, 16], {'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'},
                          uv={'north': [1, 1, 15, 15]})], {'p': f'poster_{index}'}, f'poster_{index}')


def table_model(stage):
    t = {'t': 'formica', 'e': 'counter_edge', 'm': 'tube_steel', 'f': 'folder_side', 'g': 'formica', 's': 'lamp_shade', 'b': 'lamp_brass'}
    e = [element([0, 12, 0], [16, 13, 16], {'up': 't', 'down': 't', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'}),
         element([1, 0, 1], [2, 12, 2], {'all': 'm'}), element([14, 0, 1], [15, 12, 2], {'all': 'm'}),
         element([1, 0, 14], [2, 12, 15], {'all': 'm'}), element([14, 0, 14], [15, 12, 15], {'all': 'm'})]
    if stage == 1:
        e += [element([3, 13, 4], [11, 15.5, 11], {'all': 'f'}, uv={'north': [0, 6, 16, 11], 'south': [0, 6, 16, 11], 'east': [0, 6, 14, 11], 'west': [0, 6, 14, 11]}),
              element([4, 15.5, 5], [10.5, 16.5, 10.5], {'all': 'f'})]
    if stage == 2:
        lamp = [element([10, 13, 9], [13, 13.5, 12], {'all': 'b'}), element([11.25, 13.5, 10.25], [11.75, 17, 10.75], {'all': 'b'}),
                element([8.5, 17, 9], [14, 19, 12], {'all': 's'})]
        glow(lamp[2:], 12)
        e += lamp
    return model(e, t, 'formica')


def desk_model(stage):
    t = {'w': 'wood', 'd': f'drawer_{min(stage, 1)}', 'k': 'typewriter', 'p': 'sheet', 'f': 'folder_side'}
    e = [element([0, 12, 1], [16, 13, 16], {'all': 'w'}),
         element([0, 0, 1], [3, 12, 16], {'all': 'w'}), element([13, 0, 1], [16, 12, 16], {'all': 'w'}),
         element([3, 2, 15], [13, 12, 16], {'all': 'w'}),
         # the typewriter, and the sheet standing in it
         element([4, 13, 5], [12, 16, 11], {'up': 'k', 'north': 'k', 'south': 'k', 'east': 'k', 'west': 'k'}),
         element([5, 15, 9.5], [11, 20, 10], {'all': 'p'})]
    if stage == 2:
        e += [element([3.5, 7, -5], [12.5, 11.5, 2], {'north': 'd', 'up': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}),
              element([4.5, 11.5, -4], [11.5, 11.75, 1], {'up': 'f'})]
    else:
        e.append(element([3.5, 7, 1], [12.5, 11.5, 2], {'north': 'd'}))
    return model(e, t, 'wood')


def louvre_model():
    return model([element([0, 0, 6], [16, 16, 10], {'north': 'l', 'south': 'l', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'})],
                 {'l': 'louvre', 'f': 'counter_edge'}, 'louvre')


def tube_model():
    e = [element([2, 14, 6], [14, 16, 10], {'all': 'm'}), element([2.5, 13.25, 6.5], [13.5, 14, 9.5], {'down': 'l', 'north': 'l', 'south': 'l'})]
    glow(e[1:], 15)
    return model(e, {'m': 'tube_steel', 'l': 'tube_light'}, 'tube_light')


def railing_model(stage):
    t = {'r': 'railing', 'i': 'iron'}
    if stage == 1:
        return model([element([6, 0, 6], [10, 15, 10], {'all': 'i'}), element([5.5, 15, 5.5], [10.5, 16, 10.5], {'all': 'i'})], t, 'iron')
    return model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'r', 'south': 'r'}),
                  element([0, 0, 7], [16, 1, 9], {'all': 'i'})], t, 'iron')


def gate_model(stage):
    face = 'gate_chained' if stage == 1 else 'gate'
    if stage == 2:
        return model([element([0, 0, 0], [1, 16, 16], {'east': 'g', 'west': 'g'}, uv={'east': [0, 0, 16, 16], 'west': [16, 0, 0, 16]})], {'g': 'gate'}, 'iron')
    return model([element([0, 0, 7.5], [16, 16, 8.5], {'north': 'g', 'south': 'g'})], {'g': face}, 'iron')


def lamp_post_model(stage):
    t = {'i': 'lamp_iron', 'g': 'globe_lit' if stage == 2 else 'globe_dark'}
    if stage == 0:
        e = [element([4.5, 0, 4.5], [11.5, 4, 11.5], {'all': 'i'}), element([5.5, 4, 5.5], [10.5, 10, 10.5], {'all': 'i'}),
             element([6.5, 10, 6.5], [9.5, 16, 9.5], {'all': 'i'})]
    elif stage == 1:
        e = [element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'i'})]
    else:
        e = [element([6, 0, 6], [10, 2, 10], {'all': 'i'}), element([3.5, 2, 3.5], [12.5, 11, 12.5], {'all': 'g'}),
             element([6, 11, 6], [10, 13, 10], {'all': 'i'})]
        if stage == 2:
            glow(e[1:2], 15)
    return model(e, t, 'lamp_iron')


def bench_model(stage):
    t = {'s': 'slats', 'i': 'iron'}
    e = [element([0, 6, 3], [16, 7, 13], {'up': 's', 'down': 's', 'north': 's', 'south': 's', 'east': 's', 'west': 's'}),
         element([0, 8, 13], [16, 14, 14], {'all': 's'})]
    if stage in (0, 2):
        x0 = 1 if stage == 0 else 13
        e += [element([x0, 0, 3], [x0 + 2, 6, 5], {'all': 'i'}), element([x0, 0, 11], [x0 + 2, 6, 13], {'all': 'i'}),
              element([x0, 6, 12.5], [x0 + 2, 14.5, 14.5], {'all': 'i'}), element([x0, 9, 3], [x0 + 2, 10, 12.5], {'all': 'i'})]
    return model(e, t, 'slats')


def fountain_model(stage):
    t = {'s': 'stone', 'f': 'fountain_top', 'b': 'basin'}
    if stage == 2:
        return model([element([3, 0, 3], [13, 16, 13], {'all': 's'}), element([2, 0, 2], [14, 3, 14], {'all': 's'})], t, 'stone')
    if stage == 3:
        return model([element([6, 0, 6], [10, 2, 10], {'all': 's'}), element([1, 2, 1], [15, 6, 15], {'up': 'b', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f', 'down': 's'})], t, 'stone')
    if stage == 1:
        return model([element([0, 0, 0], [16, 8, 16], {'up': 'f', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'})], t, 'stone')
    return model([element([0, 0, 8], [16, 8, 16], {'up': 'f', 'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's'}),
                  element([0, 0, 0], [16, 1, 8], {'up': 'b'})], t, 'stone')


def kiosk_model(stage):
    face = 'kiosk_shutter' if stage == 0 else 'kiosk_side'
    return model([element([0, 0, 0], [16, 16, 16], {'north': 'f', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'})],
                 {'f': face, 'r': 'kiosk_roof'}, face, cutout=False)


def ring(lo, hi, y0, y1, tex, top=False):
    # A square section of trunk from lo to hi (which may stand out past the block), its sides mapped onto the whole bark picture.
    sides = {k: [0, 16 - y1, 16, 16 - y0] for k in ('north', 'south', 'east', 'west')}
    faces = {'north': tex, 'south': tex, 'east': tex, 'west': tex}
    if top:
        faces['up'] = tex; sides['up'] = [2, 2, 14, 14]
    return element([lo, y0, lo], [hi, y1, hi], faces, uv=sides)


def trunk_model(stage):
    t = {'b': 'silk_bark', 'f': 'silk_foot'}
    if stage == 0:
        return model([ring(0, 16, 0, 16, 'f'), element([-2, 0, 4], [18, 3, 12], {'all': 'f'}, uv={k: [0, 13, 16, 16] for k in ('north', 'south', 'east', 'west', 'up', 'down')}),
                      element([4, 0, -2], [12, 3, 18], {'all': 'f'}, uv={k: [0, 13, 16, 16] for k in ('north', 'south', 'east', 'west', 'up', 'down')})], t, 'silk_bark')
    if stage == 1:
        return model([ring(-1, 17, 0, 4, 'b'), ring(-2, 18, 4, 12, 'b'), ring(-1, 17, 12, 16, 'b', top=True)], t, 'silk_bark')
    if stage == 2:
        return model([ring(1, 15, 0, 8, 'b', top=True), ring(3, 13, 8, 16, 'b', top=True)], t, 'silk_bark')
    return model([element([4, 0, 4], [12, 8, 12], {'all': 'b'}),
                  element([1, 6, 6], [6, 16, 10], {'all': 'b'}, rotation={'origin': [8, 8, 8], 'axis': 'z', 'angle': 22.5}),
                  element([10, 6, 6], [15, 16, 10], {'all': 'b'}, rotation={'origin': [8, 8, 8], 'axis': 'z', 'angle': -22.5})], t, 'silk_bark')


def leaves_model(stage):
    face = {0: 'leaves_plane', 1: 'leaves_silk', 2: 'leaves_edge'}[stage]
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'l'})], {'l': face}, face)


def chain_piece(x0, x1, y0, y1=16):
    # A chain hanging from the bar: four faces, each mapped onto the column of the chain picture where the links are drawn (x 7 to 9),
    # so a narrow piece never shows the picture's empty margin.
    col = {'uv': {k: [7, 16 - y1, 9, 16 - y0] for k in ('north', 'south', 'east', 'west')}}
    return element([x0, y0, 7.5], [x1, y1, 8.5], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}, uv=col['uv'])


def play_model(stage):
    t = {'p': 'paint_red', 'c': 'chain', 'r': 'rubber', 'h': 'chute', 'w': 'plank', 'b': 'paint_blue', 'i': 'iron'}
    if stage == 0:
        e = [element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'p'})]
    elif stage == 1:
        e = [element([7.5, 6, 7.5], [8.5, 16, 8.5], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}),
             element([4, 4, 6], [12, 5, 10], {'all': 'r'}),
             chain_piece(4.5, 5, 5), chain_piece(11, 11.5, 5)]
        e = [e[1], e[2], e[3]]
    elif stage == 2:
        e = [element([3, 0, 7], [4.5, 16, 9], {'all': 'i'}), element([11.5, 0, 7], [13, 16, 9], {'all': 'i'})] + \
            [element([4.5, y, 7.5], [11.5, y + 1, 8.5], {'all': 'i'}) for y in (2, 6, 10, 14)]
    elif stage == 3:
        e = [element([2, 0, 0], [14, 1, 16], {'all': 'h'}, rotation={'origin': [8, 8, 8], 'axis': 'x', 'angle': -45}),
             element([2, 1, 0], [3, 3, 16], {'all': 'h'}, rotation={'origin': [8, 8, 8], 'axis': 'x', 'angle': -45}),
             element([13, 1, 0], [14, 3, 16], {'all': 'h'}, rotation={'origin': [8, 8, 8], 'axis': 'x', 'angle': -45})]
    elif stage == 4:
        e = [element([0, 5, 6.5], [16, 6.5, 9.5], {'all': 'w'}), element([6.5, 0, 6.5], [9.5, 5, 9.5], {'all': 'p'}),
             element([2, 6.5, 7.5], [3, 9, 8.5], {'all': 'i'}), element([13, 6.5, 7.5], [14, 9, 8.5], {'all': 'i'})]
    elif stage == 5:
        e = [element([0, 0, 7.5], [1, 10, 8.5], {'all': 'b'}), element([15, 0, 7.5], [16, 10, 8.5], {'all': 'b'}),
             element([0, 8.5, 7.5], [16, 9.5, 8.5], {'all': 'b'}), element([0, 3, 7.5], [16, 4, 8.5], {'all': 'b'})]
    elif stage == 6:
        e = [element([0, 12, 6.5], [16, 15, 9.5], {'all': 'p'})]
    else:
        e = [chain_piece(4.5, 5, 0), chain_piece(11, 11.5, 0)]
    return model(e, t, 'paint_red')


def bandstand_model(stage):
    t = {'i': 'iron', 'l': 'lattice', 'f': 'fret', 's': 'stone'}
    if stage == 0:
        e = [element([6.5, 0, 6.5], [9.5, 16, 9.5], {'all': 'i'}), element([5.5, 0, 5.5], [10.5, 2, 10.5], {'all': 'i'}), element([5.5, 14, 5.5], [10.5, 16, 10.5], {'all': 'i'})]
    elif stage == 1:
        e = [element([0, 0, 7.5], [16, 12, 8.5], {'north': 'l', 'south': 'l'}), element([0, 11, 7], [16, 12.5, 9], {'all': 'i'})]
    elif stage in (2, 4):
        # The eave: boards under the tiles, and the fret hung from their outer edge (the corners on both their outer faces).
        t['e'] = 'fascia'
        e = [element([0, 15, 0], [16, 16, 16], {'all': 'e'}),
             element([0, 8, 0.5], [16, 15, 1], {'north': 'f', 'south': 'f'}, uv={'north': [0, 0, 16, 7], 'south': [16, 0, 0, 7]})]
        if stage == 4:
            e.append(element([0.5, 8, 0], [1, 15, 16], {'east': 'f', 'west': 'f'}, uv={'east': [16, 0, 0, 7], 'west': [0, 0, 16, 7]}))
    else:
        e = [element([0, 0, 8], [16, 8, 16], {'all': 's'})]
    return model(e, t, 'iron')


def shelter_model(stage):
    t = {'g': 'glass', 'r': 'roof_sheet', 'i': 'tube_steel', 's': 'stop_sign', 'p': 'perforated'}
    if stage == 0:
        e = [element([0, 0, 7.5], [16, 16, 8.5], {'north': 'g', 'south': 'g', 'east': 'i', 'west': 'i'})]
    elif stage == 1:
        e = [element([0, 13, 0], [16, 15, 16], {'all': 'r'})]
    elif stage == 2:
        e = [element([7, 0, 7], [9, 16, 9], {'all': 'i'}), element([3, 8, 7.75], [13, 18, 8.25], {'north': 's', 'south': 's'})]
    else:
        e = [element([0, 6, 8], [16, 7, 14], {'all': 'p'}), element([1, 0, 12], [2, 6, 13], {'all': 'i'}), element([14, 0, 12], [15, 6, 13], {'all': 'i'})]
    return model(e, t, 'tube_steel')


SHUTTER_FACES = ['shutter', 'shutter_lit', 'shutter_low', 'shutter_top', 'shutter_low_lit', 'shutter_top_lit']


def shutter_model(stage):
    # Set three pixels into the wall, with a stone sill under the window (wider than it, standing out from the wall) and a head over it.
    face = SHUTTER_FACES[stage]
    e = [element([0, 0, 3], [16, 16, 4], {'north': 'w', 'south': 'w'})]
    if stage in (1, 4):
        glow(e, 10)
    if stage in (0, 1, 2, 4):
        e.append(element([-1, -1, -2], [17, 1, 3], {'north': 's', 'up': 's', 'down': 's', 'east': 's', 'west': 's'},
                         uv={'north': [0, 0, 16, 2], 'up': [0, 2, 16, 7], 'down': [0, 12, 16, 16], 'east': [0, 0, 5, 2], 'west': [0, 0, 5, 2]}))
    if stage in (0, 1, 3, 5):
        e.append(element([-0.5, 15, -1], [16.5, 16.5, 3], {'north': 's', 'down': 's', 'east': 's', 'west': 's'},
                         uv={'north': [0, 0, 16, 1.5], 'down': [0, 12, 16, 16], 'east': [0, 0, 4, 1.5], 'west': [0, 0, 4, 1.5]}))
    return model(e, {'w': face, 's': 'sill'}, face)


def fitting_models():
    m = {}
    for s in range(2):
        m[f'counter_{s}'] = counter_model(s)
    m['card_0'] = card_model(); m['chair_0'] = chair_model(); m['louvre_0'] = louvre_model(); m['tube_0'] = tube_model()
    for s in range(5):
        m[f'shelf_{s}'] = shelf_model(s)
    for i in range(8):
        m[f'poster_{i}'] = poster_model(i); m[f'poster_b_{i}'] = poster_model(8 + i)
    for s in range(3):
        m[f'table_{s}'] = table_model(s); m[f'desk_{s}'] = desk_model(s); m[f'gate_{s}'] = gate_model(s)
        m[f'bench_{s}'] = bench_model(s); m[f'leaves_{s}'] = leaves_model(s)
    for s in range(2):
        m[f'railing_{s}'] = railing_model(s); m[f'kiosk_{s}'] = kiosk_model(s)
    for s in range(len(SHUTTER_FACES)):
        m[f'shutter_{s}'] = shutter_model(s)
    for s in range(4):
        m[f'lamp_post_{s}'] = lamp_post_model(s); m[f'fountain_{s}'] = fountain_model(s); m[f'trunk_{s}'] = trunk_model(s)
        m[f'shelter_{s}'] = shelter_model(s)
    for s in range(5):
        m[f'bandstand_{s}'] = bandstand_model(s)
    for s in range(8):
        m[f'play_{s}'] = play_model(s)
    return m


def folder_side():
    im = new('#c9b48a', 77600, 3)
    for y in range(6, 11):
        for x in range(16):
            if y in (7, 9):
                darken(im, x, y, -14)
    return im


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/last_seen_{name}.png'] = im
    faces = {
        'laminate': laminate(), 'formica': formica(), 'counter_edge': counter_edge(), 'flap': flap(), 'card': card(), 'fabric': fabric(),
        'tube_steel': tube_steel(), 'shelf_upright': shelf_upright(), 'shelf_a': shelf_face(77271), 'shelf_b': shelf_face(77272),
        'shelf_c': shelf_face(77273), 'shelf_out': shelf_face(77271, True), 'boxes': boxes(), 'wood': wood(), 'drawer_0': drawer_front(0),
        'drawer_1': drawer_front(1), 'typewriter': typewriter(), 'sheet': sheet(), 'lamp_shade': lamp_shade(), 'lamp_brass': lamp_brass(),
        'folder_side': folder_side(), 'louvre': louvre(), 'tube_light': tube_light(),
        'iron': iron(), 'railing': railing_face(), 'gate': gate_face(False), 'gate_chained': gate_face(True), 'lamp_iron': lamp_iron(),
        'globe_lit': globe(1), 'globe_dark': globe(0), 'slats': slats(), 'stone': stone(), 'fountain_top': fountain_top(), 'basin': basin(),
        'kiosk_shutter': kiosk_shutter(), 'kiosk_side': kiosk_side(), 'kiosk_roof': kiosk_roof(), 'silk_bark': silk_bark(),
        'silk_foot': silk_bark(True), 'leaves_plane': leaves(0), 'leaves_silk': leaves(1), 'leaves_edge': leaves(2),
        'paint_red': paint('#a8402e', 77610), 'paint_blue': paint('#3e6a8a', 77620), 'chain': chain(), 'rubber': rubber(), 'chute': chute(),
        'plank': plank(), 'lattice': lattice(), 'fret': fret(), 'glass': glass(), 'roof_sheet': roof_sheet(), 'stop_sign': stop_sign(),
        'perforated': perforated(), 'shutter': shutter_face(0), 'shutter_lit': shutter_face(1), 'shutter_low': shutter_face(0, 'low'),
        'shutter_top': shutter_face(0, 'top'), 'shutter_low_lit': shutter_face(1, 'low'), 'shutter_top_lit': shutter_face(1, 'top'),
        'sill': sill(), 'fascia': fascia(),
    }
    from paint_character_skins import LOOKS
    for i in range(16):
        face = face_of(LOOKS[f'literary_lastseen_{CHILDREN[i]}']) if i < 10 else drawn_face(77700 + i)
        faces[f'poster_{i}'] = poster(face, 77800 + i, faded=i >= 10)
    for name, im in faces.items():
        out[f'block/last_seen/{name}.png'] = im
    return out


KINDS = ['counter', 'card', 'chair', 'shelf', 'poster', 'poster_b', 'table', 'desk', 'louvre', 'tube', 'railing', 'gate', 'lamp_post', 'bench',
         'fountain', 'kiosk', 'trunk', 'leaves', 'play', 'bandstand', 'shelter', 'shutter']
STAGES = {'counter': 2, 'shelf': 5, 'poster': 8, 'poster_b': 8, 'table': 3, 'desk': 3, 'railing': 2, 'gate': 3, 'lamp_post': 4, 'bench': 3,
          'fountain': 4, 'kiosk': 2, 'trunk': 4, 'leaves': 3, 'play': 8, 'bandstand': 5, 'shelter': 4, 'shutter': 6}
STAGE_MAX = 8
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'last_seen_fitting': 'Park Fitting', 'last_seen_paving': 'Park Paving', 'last_seen_curb': 'Curbstone', 'last_seen_earth': 'Trodden Earth',
         'last_seen_render': 'Office Render', 'last_seen_plinth': 'Office Plinth', 'last_seen_floor': 'Office Floor', 'last_seen_ceiling': 'Office Ceiling',
         'last_seen_asphalt': 'Avenue Asphalt', 'last_seen_pavement': 'Avenue Pavement', 'last_seen_house': 'House Render', 'last_seen_stone': 'Park Stone',
         'last_seen_bark': 'Plane Bark', 'last_seen_house_b': 'Limewashed Render', 'last_seen_house_c': 'Painted Render',
         'last_seen_road_line': 'Road Marking', 'last_seen_crossing': 'Crossing Stripe'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/last_seen_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/last_seen_{name}'}}
        out[f'blockstates/last_seen_{name}.json'] = {'variants': {'': {'model': NS + f'block/last_seen_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/last_seen/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/last_seen/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/last_seen_fitting.json'] = {'variants': variants}
    return out


def main():
    args = argparse.ArgumentParser()
    args.add_argument('--check', action='store_true')
    args.add_argument('--out', default=None, help='write under this directory instead of the mod (for a look before it goes in)')
    opts = args.parse_args()
    check = opts.check
    base = Path(opts.out) / 'assets/the_oldest_house' if opts.out else A
    images, data = textures(), files()
    models = fitting_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            if tex.startswith(P):
                assert f'block/last_seen/{tex[len(P):]}.png' in images, (key, tex)
    # Each of the ten who come back has their own poster, taken from their own skin: no two posters are the same face.
    faces = [images[f'block/last_seen/poster_{i}.png'].crop((4, 4, 12, 12)).tobytes() for i in range(16)]
    assert len(set(faces)) == 16, 'two posters show the same face'
    if not opts.out:
        for i in range(10):
            assert (A / f'textures/entity/literary_lastseen_{CHILDREN[i]}.png').exists(), CHILDREN[i]
    # A lit globe is brighter than a dark one, and the lit window warmer than the shut.
    def mean(im, box=(2, 2, 14, 14)):
        px = [im.getpixel((x, y)) for y in range(box[1], box[3]) for x in range(box[0], box[2])]
        return [sum(p[i] for p in px) / len(px) for i in range(3)]
    assert sum(mean(images['block/last_seen/globe_lit.png'])) > sum(mean(images['block/last_seen/globe_dark.png'])) + 150
    lit, shut = mean(images['block/last_seen/shutter_lit.png'], (1, 9, 15, 15)), mean(images['block/last_seen/shutter.png'], (1, 9, 15, 15))
    assert lit[0] > shut[0] + 60, (lit, shut)
    # A bay with a folder out has a dark slot among its spines.
    out_bay = images['block/last_seen/shelf_out.png']
    assert min(sum(out_bay.getpixel((x, 4))[:3]) for x in range(16)) < 150
    for rel, im in images.items():
        path = base / 'textures' / rel
        if check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data.items():
        path = base / rel
        if check:
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    if opts.out and not check:
        print(f'Drew the park under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the park: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
