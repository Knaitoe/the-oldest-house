"""What each room lets out under its door (0.4.576, docs/DOOR_BLEEDS_0_4_576.md): one drawn piece of floor for every
vignette that leaves something on the hall's boards. Sixteen pixels to the block, the game's own grid. A texture is
sixteen wide and one or two blocks deep; its first row lies against the door and the rest runs out into the hall.

Nothing here is a photograph or a filter: grains are laid by a seeded hand so that a drift thins and breaks at its edge,
and things (a leaf, an envelope, a button) are small pictures set down one at a time.

Run it to draw; --check verifies the pack against it (CI runs that). The names come from DoorBleed.java: every bleed
there that says `drawn` has a picture here, and nothing here is without one there.
"""
import argparse
import math
import random
import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
FOLDER = A / 'textures/block/bleed'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/DoorBleed.java'
W = 16


def rgb(h):
    return (h >> 16 & 255, h >> 8 & 255, h & 255)


def rng(name):
    return random.Random('door bleed: ' + name)


def canvas(depth):
    return Image.new('RGBA', (W, W * depth), (0, 0, 0, 0))


def tone(c, k):
    return tuple(max(0, min(255, round(v * k))) for v in c[:3])


def put(im, x, y, c, a=255):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), tuple(c[:3]) + (a,))


def free(im, x, y):
    return 0 <= x < im.width and 0 <= y < im.height and im.getpixel((x, y))[3] == 0


# ---------------------------------------------------------------------------------------------------------- how things lie

def drift(im, r, colours, reach, body=1.0, lean=None, veil=255, jitter=.06):
    """A drift of grains against the door: whole near it, broken toward its edge, a few grains past it. `colours` is a
    list of (hex, weight). `reach` is how many pixels out its middle goes; its sides fall short of that."""
    lean = r.uniform(5.5, 10.5) if lean is None else lean
    bag = [rgb(h) for h, w in colours for _ in range(w)]
    wobble = [r.uniform(-1, 1) for _ in range(W)]
    for x in range(W):
        mound = 1 - ((x - lean) / 9.5) ** 2
        rx = reach * max(.22, mound) + wobble[x] * reach * .12
        for y in range(im.height):
            d = y / max(1.0, rx)
            if d < .5:
                p = body
            elif d < 1.0:
                p = body * (1 - (d - .5) / .5) ** 1.3 * .85 + .07
            elif d < 1.7:
                p = .09 * (1.7 - d) / .7
            else:
                p = 0
            if x in (0, W - 1):
                p *= .4
            if r.random() < p:
                put(im, x, y, tone(r.choice(bag), 1 + r.uniform(-jitter, jitter)), veil)


def sprite(im, x, y, rows, pal, flip=False, a=255):
    for j, row in enumerate(rows):
        row = row[::-1] if flip else row
        for i, ch in enumerate(row):
            if ch != '.':
                put(im, x + i, y + j, rgb(pal[ch]), a)


def fits(im, x, y, rows, margin=0):
    h, w = len(rows), len(rows[0])
    if x < 0 or y < 0 or x + w > im.width or y + h > im.height:
        return False
    for j in range(-margin, h + margin):
        for i in range(-margin, w + margin):
            if 0 <= x + i < im.width and 0 <= y + j < im.height and im.getpixel((x + i, y + j))[3]:
                return False
    return True


def strew(im, r, things, count, near=.42, margin=1, tries=60):
    """Set `count` things down, most of them near the door. `things` is a list of (rows, palette) pictures; a palette
    may be a list of palettes to pick from."""
    placed = 0
    for _ in range(count):
        rows, pal = r.choice(things)
        pal = r.choice(pal) if isinstance(pal, list) else pal
        for _ in range(tries):
            x = r.randint(0, W - len(rows[0]))
            y = min(im.height - len(rows), int(abs(r.gauss(0, im.height * near))))
            if fits(im, x, y, rows, margin):
                sprite(im, x, y, rows, pal, flip=r.random() < .5)
                placed += 1
                break
    return placed


def line(im, x0, y0, x1, y1, c, a=255):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        put(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), c, a)


def needles(im, r, colours, count, lengths=(2, 4), near=.4):
    for _ in range(count):
        x, y = r.randint(0, W - 1), min(im.height - 1, int(abs(r.gauss(0, im.height * near))))
        ang, n = r.uniform(0, math.pi), r.randint(*lengths)
        line(im, x, y, round(x + math.cos(ang) * n), round(y + math.sin(ang) * n * .8), tone(rgb(r.choice(colours)), 1 + r.uniform(-.08, .08)))


def ferns(im, r, colours, stems, a=(150, 235)):
    """Frost: crystals that start at the door and branch as they go."""
    def grow(x, y, dx, dy, left, depth):
        for step in range(left):
            xi, yi = round(x), round(y)
            if not (0 <= xi < W and 0 <= yi < im.height):
                return
            fade = 1 - step / max(1, left)
            put(im, xi, yi, rgb(r.choice(colours)), round(a[0] + (a[1] - a[0]) * fade))
            if depth < 3 and step > 1 and r.random() < .34:
                turn = r.choice((-1, 1))
                grow(x, y, dx * .4 + turn * .8, dy * .55 + .25, max(2, int((left - step) * .6)), depth + 1)
            x += dx + r.uniform(-.25, .25)
            y += dy
    for _ in range(stems):
        grow(r.uniform(.5, 15.5), 0, r.uniform(-.35, .35), r.uniform(.7, 1.0), r.randint(int(im.height * .45), int(im.height * .95)), 0)


# ------------------------------------------------------------------------------------------------------------------ things

LEAF = (['.aa.', 'abba', '.ab.', '..c.'], ['.aa..', 'abbba', '.aba.'], ['aa.', 'abb', '.ba', '..c'])
LEAF_PAL = [dict(a=0x6E4A22, b=0x8F6A34, c=0x4A3118), dict(a=0x7D6A2A, b=0xA08A3C, c=0x4A3118), dict(a=0x4C6A2C, b=0x65893A, c=0x3A2A14),
            dict(a=0x5A3A1C, b=0x7A5226, c=0x3A2412)]
CURLED = (['.aa', 'ab.', 'a..'], ['aa.', '.ba', '..a'], ['ab', 'a.'], ['.a.', 'aba'])
CURLED_PAL = [dict(a=0x9A7A3A, b=0xC2A057), dict(a=0x7A5A2A, b=0xA8843E), dict(a=0x8A6A44, b=0xB59364)]
MAPLE = (['a...a', '.aba.', 'abbba', '.aba.', '..c..'], ['.a.a.', 'aabaa', '.bbb.', 'a.b.a', '..c..'])
MAPLE_PAL = [dict(a=0xC8641E, b=0xE88A2E, c=0x5A3214), dict(a=0xA83A1E, b=0xCC5A2A, c=0x4A2412), dict(a=0xC89A22, b=0xE8C03A, c=0x5A4214)]
CHIP = (['ab', 'bc'], ['abb', '.bc'], ['a', 'c'], ['ab.', 'bbc', '.cc'])
PAPER = dict(a=0xE9E3D0, b=0xD6CFB8, c=0x54504A, d=0xBFB79F)
BUTTON = (['.a.', 'aca', '.a.'], ['aa', 'aa'], ['.aa.', 'acca', 'acca', '.aa.'])
BUTTON_PAL = [dict(a=0xE6DFCC, c=0x7A7466), dict(a=0x3A3630, c=0x8A8478), dict(a=0xB89A6A, c=0x6A5230), dict(a=0xF2EEE4, c=0x9A948A)]
STONE = (['ab', 'bc'], ['.ab', 'abc', 'bc.'], ['a'], ['ab'], ['a', 'c'])


def stones(im, r, greys, count, near=.42):
    for _ in range(count):
        base = rgb(r.choice(greys))
        pal = dict(a=_hex(tone(base, 1.18)), b=_hex(base), c=_hex(tone(base, .72)))
        strew(im, r, [(rows, pal) for rows in STONE], 1, near=near, margin=0)


def _hex(c):
    return c[0] << 16 | c[1] << 8 | c[2]


# ------------------------------------------------------------------------------------------------------ one for each door

def goatman(r):
    im = canvas(1)
    needles(im, r, [0x5A4A2A, 0x6A5630], 10, (1, 3))
    strew(im, r, [(rows, LEAF_PAL) for rows in LEAF], 11, near=.5)
    return im


def ted_caver(r):
    im = canvas(1)
    drift(im, r, [(0x8A8A86, 3), (0x6E6E6A, 2), (0xA6A6A0, 1)], 6, body=.55)
    stones(im, r, [0x77777A, 0x8C8C88, 0x5E5E60], 9)
    return im


def scrap(im, r, x, y, w, h, pal, ruled=None, margin_line=None):
    """A torn piece of paper with writing on it."""
    for j in range(h):
        for i in range(w):
            if (i, j) in ((0, 0), (w - 1, h - 1)) and r.random() < .7:
                continue
            put(im, x + i, y + j, rgb(pal['a'] if (i + j * 2) % 5 else pal['b']))
    for j in range(1, h - 1, 2):
        for i in range(1, w - 1):
            if ruled is not None:
                put(im, x + i, y + j, rgb(ruled))
            elif r.random() < .62:
                put(im, x + i, y + j, rgb(pal['c']))
    if margin_line is not None:
        for j in range(h):
            put(im, x + 1, y + j, rgb(margin_line))
    for i in range(w):
        if r.random() < .45:
            put(im, x + i, y + h - 1, rgb(pal['d']))


def zampano_courtyard(r):
    im = canvas(1)
    for x, y, w, h in ((1, 0, 6, 5), (8, 1, 7, 4), (4, 6, 5, 6), (10, 7, 5, 4), (0, 11, 4, 4)):
        scrap(im, r, x, y, w, h, PAPER)
    return im


def envelope(im, x, y, w, h, pal, stamp=True):
    for j in range(h):
        for i in range(w):
            put(im, x + i, y + j, rgb(pal['a']))
    for i in range(w):
        put(im, x + i, y + h - 1, rgb(pal['d']))
    for k in range(min(w // 2, h - 1)):       # the flap
        put(im, x + k, y + k, rgb(pal['d']))
        put(im, x + w - 1 - k, y + k, rgb(pal['d']))
    if stamp:
        put(im, x + w - 2, y + h - 2, rgb(0x8A3A32))


def whale(r):
    im = canvas(1)
    envelope(im, 1, 0, 8, 4, PAPER, stamp=False)        # half under the door still
    envelope(im, 7, 5, 8, 5, dict(a=0xDDD5BC, d=0xB5AD94))
    envelope(im, 0, 9, 7, 5, PAPER)
    return im


def barn_well(r):
    im = canvas(1)
    needles(im, r, [0xD9C06A, 0xC4A850, 0xE6D38A, 0xB0943C], 46, (3, 6), near=.46)
    return im


def plain(r):
    im = canvas(2)
    drift(im, r, [(0xD8C28A, 4), (0xC9B078, 3), (0xE6D6A6, 2), (0xB89C62, 1)], 25)
    for y in (5, 11, 18):                                 # the wind's ripples
        for x in range(1, 15):
            if im.getpixel((x, y + (x // 5) % 2))[3] and r.random() < .7:
                put(im, x, y + (x // 5) % 2, rgb(0xAE9258))
    return im


def holloway_camp(r):
    im = canvas(1)
    drift(im, r, [(0xB8B4AC, 3), (0x8E8A84, 3), (0xD6D2CA, 1), (0x5E5A56, 1)], 11, body=.8)
    strew(im, r, [(rows, dict(a=0x2A2622, b=0x1A1816, c=0x0E0C0A)) for rows in CHIP], 6, margin=0)
    put(im, 6, 2, rgb(0xE8782A))
    put(im, 11, 4, rgb(0xC85A1E))
    return im


def wallpaper_nursery(r):
    im = canvas(2)
    base, dark, figure, back = rgb(0xCDB24A), rgb(0xA88A2C), rgb(0x7A6420), rgb(0xE4DCC4)
    edge = [r.choice((0, 0, 1)) for _ in range(32)]
    for y in range(27):
        lo, hi = 3 + edge[y], 12 - edge[(y * 7 + 3) % 32]
        if y > 22:                                        # the torn end
            lo, hi = lo + (y - 22), hi - (y - 23) // 2
        for x in range(lo, hi + 1):
            c = base
            if (x + y * 2) % 6 == 0 or (x * 2 - y) % 7 == 0:
                c = dark
            if (x - 7) ** 2 + ((y % 8) - 4) ** 2 <= 2:
                c = figure
            put(im, x, y, tone(c, 1 + r.uniform(-.04, .04)))
        if lo <= hi:
            put(im, lo, y, back)                         # the back of the paper, curling over
    return im


def hill_nursery(r):
    im = canvas(2)
    ferns(im, r, [0xFFFFFF, 0xE4F0FA, 0xCBE0F2], 9)
    for x in range(W):
        for y in range(3):
            if r.random() < (.9, .6, .3)[y]:
                put(im, x, y, rgb(r.choice([0xFFFFFF, 0xE4F0FA])), 220)
    return im


def second_waitress(r):
    im = canvas(2)
    ferns(im, r, [0x101418, 0x1C242C, 0x0A0C10], 9, a=(170, 245))
    for x in range(W):
        for y in range(3):
            if r.random() < (.9, .6, .3)[y]:
                put(im, x, y, rgb(r.choice([0x101418, 0x1C242C])), 235)
    return im


SHAVING = (['aa.', 'a.a', '.aa'], ['.aa', 'a..', 'aa.'], ['aa', 'a.'], ['a.a', '.a.'])
WOOD = [dict(a=0xE2C98E), dict(a=0xD2B470), dict(a=0xEED9A6)]


def miniatures(r):
    im = canvas(1)
    drift(im, r, [(0xDEC48A, 2), (0xCDB070, 1)], 4, body=.45)
    sprite(im, 9, 6, ['a...', 'a...', 'aaaa', 'a..a', 'a..a'], dict(a=0x6A4424))      # a chair for a room a hand high
    sprite(im, 2, 9, ['aaaaa', '.a.a.', '.a.a.'], dict(a=0x7A5230))                       # and its table
    strew(im, r, [(rows, WOOD) for rows in SHAVING], 9, margin=0)
    return im


def usher(r):
    im = canvas(2)
    x = 8.0
    for y in range(30):
        xi = round(x)
        put(im, xi, y, rgb(0x14110F))
        put(im, xi + 1, y, rgb(0x4A443E), 150)
        if y in (7, 15, 22):
            bx, side = float(xi), r.choice((-1, 1))
            for k in range(1, r.randint(4, 7)):
                bx += side * r.uniform(.6, 1.1)
                put(im, round(bx), y + k // 2, rgb(0x1E1A17), 230)
        x += r.choice((-1, 0, 0, 1)) * (1 if y % 3 else .5)
        x = max(3, min(12, x))
    return im


def winchester(r):
    im = canvas(1)
    drift(im, r, [(0xE2CC96, 3), (0xD0B67A, 2), (0xEEDDB0, 1)], 10, body=.85)
    strew(im, r, [(rows, WOOD) for rows in SHAVING], 6, margin=0)
    sprite(im, 11, 9, ['aa.', '.a.', '.ab'], dict(a=0x8A8E94, b=0x6A6E74))                # a bent nail
    return im


def child_room(r):
    im = canvas(1)
    for (x, y), face, letter in (((1, 1), 0xD9534A, 0xF6EFD8), ((7, 3), 0x4A82C8, 0xF6EFD8), ((11, 9), 0x5FA85A, 0xF6EFD8)):
        for j in range(4):
            for i in range(4):
                put(im, x + i, y + j, rgb(0xE8D8A8 if i in (0, 3) or j in (0, 3) else face))
        put(im, x + 1, y + 1, rgb(letter))
        put(im, x + 2, y + 2, rgb(letter))
    sprite(im, 4, 8, ['ab', 'aa'], dict(a=0x3A6AC8, b=0xCFE2FF))                          # a marble
    sprite(im, 1, 12, ['abbbc'], dict(a=0x8A2A22, b=0xC8423A, c=0xE8D8A8))                # a crayon
    sprite(im, 9, 14, ['ab'], dict(a=0xE6B83A, b=0xF2D46A))
    return im


def crimson_hall(r):
    im = canvas(2)
    drift(im, r, [(0x9A1E1A, 3), (0x7A1612, 2), (0xB8302A, 1)], 9, body=.9, lean=7.5)       # clay coming up through it
    drift(im, r, [(0xF4F6FA, 4), (0xDCE4F0, 2), (0xC8D4E6, 1)], 26, body=.72)
    for _ in range(26):                                   # and staining it
        x, y = r.randint(2, 13), int(abs(r.gauss(0, 6)))
        if 0 <= y < 32 and im.getpixel((x, y))[3]:
            put(im, x, y, rgb(r.choice([0xC8504A, 0xB0403A, 0xD87870])))
    return im


FOOT = ['.aa.', 'aaa.', 'aaaa', '.aaa', '.aaa', '.aa.', '.aa.']
TOES = ['a.aa', '....']


def bly_route(r):
    im = canvas(2)
    wet = dict(a=0x22303C)
    for x in range(1, 15):                                # what ran off her at the door
        for y in range(4):
            if r.random() < (.95, .8, .5, .25)[y]:
                put(im, x, y, rgb(0x283844), 150)
    for k, (x, y) in enumerate(((3, 4), (9, 10), (3, 17), (9, 23))):
        sprite(im, x, y + 2, FOOT, wet, flip=k % 2 == 1, a=165 - k * 22)
        sprite(im, x, y, TOES, wet, flip=k % 2 == 1, a=165 - k * 22)
    return im


def mapping_interior(r):
    im = canvas(1)
    for _ in range(34):
        x, y = r.randint(0, 15), min(15, int(abs(r.gauss(0, 6.5))))
        if free(im, x, y):
            put(im, x, y, rgb(r.choice([0x2AA6B0, 0xD2362E, 0xF4F0E6, 0xE8B82E, 0x2A4AA8])))
    sprite(im, 9, 4, ['.b', 'ab', 'aa', 'aa', 'ab', 'aa', '.a', '.c', '.c'], dict(a=0xF2EEE2, b=0x2A2420, c=0xC8BEA6))   # a feather
    return im


def holy_rabbit(r):
    im = canvas(2)
    drift(im, r, [(0xF6F8FC, 4), (0xE0E8F2, 2), (0xCCD8E8, 1)], 27, body=.9)
    track = rgb(0x8E9CB4)
    for y, x in ((3, 5), (11, 8), (19, 6), (26, 9)):       # two together, then two in a line
        for dx, dy in ((0, 0), (3, 0), (1, 3), (2, 5)):
            put(im, x + dx, y + dy, track)
    return im


HOOF = ['a.a', 'a.a', 'a.a', '.a.']


def elk_carcasses(r):
    im = canvas(2)
    drift(im, r, [(0x4E3A26, 3), (0x5E4830, 2), (0x3C2C1C, 2)], 22, body=.7)
    for k, (x, y) in enumerate(((2, 2), (9, 8), (3, 15), (10, 22))):
        sprite(im, x, y, HOOF[::-1], dict(a=0x241A10), flip=k % 2 == 1)
    return im


WRAPPER = (['a.bb.a', '.abba.'], ['ab.', 'bba', '.ba'], ['abba'])
WRAP_PAL = [dict(a=0xF2E6C8, b=0xE07A22), dict(a=0xF2E6C8, b=0x7A3AA8), dict(a=0xE8E8E8, b=0x3A9A4A), dict(a=0xF6D86A, b=0xC8322A)]


def costume_night(r):
    im = canvas(1)
    card = dict(a=0xF4F0E4, b=0x1E1C1A, c=0xCFC8B4)
    sprite(im, 9, 1, ['aaaaa', 'abbba', 'aaaba', 'aaaba', 'aaaba', 'ccccc'], card)             # an entrant's number
    strew(im, r, [(rows, WRAP_PAL) for rows in WRAPPER], 8)
    return im


POP = (['.ab', 'aba', 'ba.'], ['ab', 'ba'], ['.a.', 'abb', '.ac'], ['aab', '.b.'])
POP_PAL = [dict(a=0xF6EDCC, b=0xE8D69A, c=0x8A5A22), dict(a=0xFBF4DC, b=0xEAD48A, c=0x7A4A1A)]


def movie_night(r):
    im = canvas(1)
    strew(im, r, [(rows, POP_PAL) for rows in POP], 13, near=.5)
    for _ in range(5):
        x, y = r.randint(0, 15), r.randint(0, 15)
        if free(im, x, y):
            put(im, x, y, rgb(0x8A5A22))
    return im


def winter_lake(r):
    im = canvas(2)
    for y in range(12):                                   # ice, where it has run in under the door
        for x in range(W):
            if r.random() < (1 if y < 8 else (12 - y) / 5):
                put(im, x, y, rgb(r.choice([0xB8DCEA, 0xA6D0E2, 0xC8E6F0])), 200)
    x = 4.0
    for y in range(11):
        put(im, round(x), y, rgb(0xF2FAFE), 240)
        x += r.choice((0, 1, 1))
    line(im, 11, 2, 14, 6, rgb(0xF2FAFE), 220)
    top = canvas(2)
    drift(top, r, [(0xF6F8FC, 4), (0xDEE8F2, 2)], 24, body=.6)
    for y in range(8, 32):
        for x in range(W):
            if top.getpixel((x, y))[3]:
                im.putpixel((x, y), top.getpixel((x, y)))
    return im


def camp_blood(r):
    im = canvas(1)
    needles(im, r, [0x6E4A22, 0x8A5E2A, 0x4E6A2A, 0x7A5226], 44, (2, 4), near=.48)
    for i in range(7):                                    # a lanyard from the craft cabin
        put(im, 4 + i, 10 + (i // 3) % 2, rgb((0xC8322A, 0xF2E6C8, 0x2A5AB0)[i % 3]))
    return im


def devils_rock(r):
    im = canvas(1)
    pal = dict(a=0xF2EEE0, b=0xE2DCC8, c=0x4A4844, d=0xC8C0A8)
    for x, y, w, h in ((0, 0, 8, 7), (7, 5, 8, 6), (2, 10, 7, 6)):
        scrap(im, r, x, y, w, h, pal, ruled=0x9AB4D8, margin_line=0xD88A8A)
        for j in range(1, h - 1, 2):                      # where it came off the wire
            im.putpixel((x, y + j), (0, 0, 0, 0))
        for j in range(1, h - 1, 2):
            for i in range(3, w - 1):
                if r.random() < .5:
                    put(im, x + i, y + j, rgb(pal['c']))
    return im


DIGITS = {'1': ['.a.', 'aa.', '.a.', '.a.', 'aaa'], '4': ['a.a', 'a.a', 'aaa', '..a', '..a'], '7': ['aaa', '..a', '.a.', '.a.', '.a.'],
          '2': ['aa.', '..a', '.a.', 'a..', 'aaa'], '9': ['aaa', 'a.a', 'aaa', '..a', 'aa.']}


def wheel(r):
    im = canvas(1)
    brass = dict(a=0xC8A040)
    for digit, (x, y) in (('4', (1, 1)), ('1', (7, 3)), ('7', (11, 8)), ('2', (3, 9))):
        sprite(im, x, y, DIGITS[digit], brass)
    for x, y in ((6, 1), (10, 14), (1, 7), (14, 5)):      # the screws that held them
        put(im, x, y, rgb(0x8A8E94))
    return im


def ghosts_set(r):
    im = canvas(1)
    tape = rgb(0xE8E4D8)
    for k in range(7):                                    # a mark for an actor's feet
        for t in (0, 1):
            put(im, 8 + k + t, 5 + k, tape)
            put(im, 15 - k - t, 5 + k, tape)
    x = 3.0
    for y in range(16):                                   # and the cable that runs back under the door
        put(im, round(x), y, rgb(0x161616))
        put(im, round(x) + 1, y, rgb(0x2A2A2A))
        x += (0, 0, -.5, 0, .5, .5, 0, -.5)[y % 8]
    return im


def end_world_cabin(r):
    im = canvas(1)
    sprite(im, 2, 2, ['.aaa.', 'ababa', 'aabaa', 'ababa', '.aaa.'], dict(a=0x9AA0A6, b=0x2A2C30))    # a jar's lid, punched for air
    hopper = (['a..a', '.bb.', 'b..b'], ['.bb', 'bb.', 'a.a'])
    strew(im, r, [(rows, [dict(a=0x3A5A22, b=0x5E8A32), dict(a=0x4A6A2A, b=0x76A03E)]) for rows in hopper], 4, near=.6)
    needles(im, r, [0x4E6A2A, 0x5E7A32], 8, (1, 2))
    return im


def family_copy(r):
    im = canvas(1)
    for y in range(8):                                    # the mat, pushed half through
        for x in range(1, 15):
            edge = x in (1, 14) or y == 7
            put(im, x, y, tone(rgb(0x5E4426 if edge else 0x8A6A3A), 1 + r.uniform(-.1, .1) + ((x + y) % 2) * .06))
    for x in range(3, 13):
        put(im, x, 5, rgb(0x4A3218))
    for x in range(1, 15):                                # its fringe
        if x % 2:
            put(im, x, 8, rgb(0x8A6A3A))
    return im


def three_fingers(r):
    im = canvas(1)
    drift(im, r, [(0x4A3622, 3), (0x5A4430, 2), (0x3A2A1A, 2)], 7, body=.8)
    tread = dict(a=0x2A1E12)
    sprite(im, 9, 6, ['.aa.', 'a..a', '.aa.', 'a..a', '.aa.', '....', '.aa.', '.aa.'], tread)        # a boot off the mill road
    return im


def borrasca(r):
    im = canvas(1)
    stones(im, r, [0x8A8478, 0x9A9488, 0x6E6A62, 0xA89C86, 0x7A746A], 30, near=.5)
    needles(im, r, [0x6E4A22, 0x7A5226], 6, (2, 3))
    return im


FLAKE = (['ab', 'b.'], ['aab', '.ba'], ['a', 'b'], ['ab'])


def painted_interior(r):
    im = canvas(1)
    strew(im, r, [(rows, [dict(a=0xA8C0A0, b=0xF0ECE0), dict(a=0xD8D0B8, b=0xF4F0E6), dict(a=0x8AA890, b=0xECE8DC)]) for rows in FLAKE], 22, near=.45, margin=0)
    return im


def layered_man(r):
    im = canvas(1)
    strew(im, r, [(rows, BUTTON_PAL) for rows in BUTTON], 9, near=.5)
    x = 2.0
    for y in range(9, 16):                                # a length of thread
        put(im, round(x), y, rgb(0xD8D0C0))
        x += r.choice((0, 1, 1))
    return im


def silo(r):
    im = canvas(2)
    drift(im, r, [(0xE6B43A, 4), (0xD49A2A, 3), (0xF2CC5A, 2), (0xB87E1E, 1)], 28, body=1.0)
    return im


def way_out(r):
    im = canvas(1)
    greens = [0x4E8A2E, 0x66A83A, 0x3E7426, 0x7CBC48]
    for x in range(W):
        n = r.randint(2, 7) if r.random() < .85 else 0
        lean = r.choice((-1, 0, 0, 1))
        for y in range(n):
            put(im, x + (lean if y > n // 2 else 0), y, tone(rgb(r.choice(greens)), 1 - y * .03))
    sprite(im, 10, 6, ['.a.', 'aba', '.a.'], dict(a=0xF8F6EE, b=0xE8C02A))                   # a daisy
    sprite(im, 3, 8, ['a', 'b'], dict(a=0xF8F6EE, b=0x4E8A2E))
    return im


def whistlers(r):
    im = canvas(2)
    drift(im, r, [(0xF8FAFE, 5), (0xE2EAF4, 2), (0xCEDAEA, 1)], 28, body=.95)
    needles(im, r, [0x2E4A22, 0x3A5A2A], 7, (2, 3), near=.5)
    return im


def work_print(r):
    im = canvas(2)
    tape, shine = rgb(0x2A1C12), rgb(0x6A4A30)
    x, y, ang = 6.0, 0.0, math.pi / 2
    for step in range(120):
        xi, yi = round(x), round(y)
        if 0 <= yi < 32:
            put(im, max(0, min(15, xi)), yi, shine if step % 9 == 0 else tape)
        ang += math.sin(step * .17) * .22 + r.uniform(-.06, .06)
        x += math.cos(ang) * .55
        y += abs(math.sin(ang)) * .32 + .02
        if x < 1 or x > 14:
            ang = math.pi - ang
    return im


def polaroid_trail(r):
    im = canvas(1)
    frame = rgb(0xF4F1E8)
    for (x, y), tint in (((1, 0), 0x3A4A3A), ((8, 3), 0x4A3A2E), ((2, 8), 0x2E3A4A), ((10, 10), 0x1A1A1E)):
        for j in range(6):
            for i in range(5):
                put(im, x + i, y + j, frame)
        for j in range(1, 4):
            for i in range(1, 4):
                put(im, x + i, y + j, tone(rgb(tint), 1 + (i - j) * .08))
    return im


def left_right(r):
    im = canvas(1)
    for x in range(W):                                    # a line of salt across the sill
        for y in range(2, 6):
            gap = x in (9, 10) and y > 2                  # where a foot has been through it
            if not gap and r.random() < (.75, 1, 1, .7)[y - 2]:
                put(im, x, y, tone(rgb(0xF6F6F2), 1 - r.random() * .08))
    for _ in range(14):
        x, y = r.randint(0, 15), r.randint(6, 11)
        put(im, x, y, rgb(0xEDEDE8))
    return im


def pitt(r):
    im = canvas(1)
    drift(im, r, [(0x8A7450, 3), (0x7A6646, 3), (0x9A845E, 1)], 12, body=1.0)
    for x in (4, 7, 10):                                  # dragged by fingers
        for y in range(1, 10 + (x % 3)):
            if im.getpixel((x, y))[3]:
                put(im, x, y, rgb(0xB09A72))
    return im


def closing_hall(r):
    im = canvas(1)
    drift(im, r, [(0xECE8DE, 4), (0xD8D2C6, 2), (0xC2BCB0, 1)], 8, body=.8)
    strew(im, r, [(rows, [dict(a=0xF4F0E6, b=0xD8D2C6, c=0xB8B2A6)]) for rows in CHIP], 7, margin=0)
    return im


def storeroom(r):
    im = canvas(1)
    ink, sheen = rgb(0x0A0A10), rgb(0x2A2E4A)
    for y in range(12):                                   # ink, still coming
        half = (7.4, 7.2, 6.8, 6.0, 5.4, 4.4, 3.6, 3.0, 2.4, 2.0, 1.4, .8)[y]
        for x in range(W):
            if abs(x - 7.5 - math.sin(y * .7) * .8) <= half:
                put(im, x, y, ink)
    for x, y in ((4, 1), (5, 1), (9, 3), (6, 6)):
        put(im, x, y, sheen)
    for x, y in ((2, 9), (13, 7), (11, 12), (4, 13), (8, 14)):
        put(im, x, y, ink)
    return im


def line_(r):
    im = canvas(2)
    for y in range(32):
        put(im, 8, y, rgb(0xCDBE96 if y % 2 else 0xA8966A))
    for y, dx in ((9, -1), (17, 1), (25, -1)):            # where it has frayed
        put(im, 8 + dx, y, rgb(0xB8A67A), 200)
    return im


def polybius(r):
    im = canvas(2)
    x = 5.0
    for y in range(30):
        xi = round(x)
        for i in range(4):
            edge = y % 3 == 2
            put(im, xi + i, y, rgb(0xB8742A if edge else 0xE8A848))
        if y % 3 == 0:
            put(im, xi + 1, y, rgb(0x7A3A12))
        x += math.sin(y * .32) * .7
        x = max(0, min(12, x))
    return im


PAW = ['b.b.', '.b.b', '.aa.', 'aaaa', '.aa.']


def replacement(r):
    im = canvas(2)
    mud = dict(a=0x33261A, b=0x8E7650)                    # the pad's wet, the toes' drying: one of them shows on any floor
    for k, (x, y) in enumerate(((2, 1), (9, 6), (3, 12), (10, 18), (4, 24))):
        sprite(im, x, y, PAW[::-1], mud, flip=k % 2 == 1, a=235 - k * 30)
    return im


def vacant_possession(r):
    im = canvas(1)
    paper, crease = rgb(0xC8AE82), rgb(0xA88E62)
    for x, y, w, h in ((0, 0, 7, 6), (8, 4, 7, 6)):       # packing paper, screwed up
        for j in range(h):
            for i in range(w):
                if (i + j) % 7 != 6 and not (i in (0, w - 1) and j in (0, h - 1)):
                    put(im, x + i, y + j, crease if (i * 2 + j) % 5 == 0 else paper)
    sprite(im, 2, 9, ['.aa', 'a.a', 'aa.'], dict(a=0xA8742E))                                 # a curl of tape
    for j in range(3):                                    # bubble wrap
        for i in range(4):
            put(im, 9 + i * 2 - (j % 2), 12 + j, rgb(0xE6F0F4), 170)
    return im


def second_person(r):
    im = canvas(1)
    drift(im, r, [(0xB8BEC8, 3), (0xA6AEBA, 2), (0xCDD2DA, 1)], 9, body=.5, veil=190)
    sprite(im, 8, 9, ['aabaab', 'aabaab', '....ab'], dict(a=0xECE8E0, b=0xC8423A))            # a sock that got out
    return im


def deer_road(r):
    im = canvas(1)
    for _ in range(40):
        x, y = r.randint(0, 15), min(15, int(abs(r.gauss(0, 6))))
        put(im, x, y, rgb(r.choice([0xE8F6FA, 0xCFEAF2, 0xFFFFFF, 0xB8DCE6])), r.choice((255, 220, 190)))
    sprite(im, 3, 6, ['ab', 'bb'], dict(a=0xF2A62A, b=0xD88A1A))                              # a piece of the lamp
    sprite(im, 11, 3, ['aaa'], dict(a=0x2A2A2E))
    return im


def state_route(r):
    im = canvas(1)
    drift(im, r, [(0x3A3A3E, 3), (0x2A2A2E, 3), (0x4E4E52, 1)], 8, body=.6)
    stones(im, r, [0x5A5A5E, 0x46464A], 6)
    sprite(im, 9, 10, ['aaab'], dict(a=0xF0ECE0, b=0xC88A3A))                                 # a cigarette end
    return im


HUSK = (['a.', 'ab', 'ab', 'ab', '.b', '.a'], ['.ab', 'abb', 'ab.', 'a..'], ['aab', '.bb'])


def marsh_supper(r):
    im = canvas(1)
    strew(im, r, [(rows, [dict(a=0xC8C48A, b=0xA8A86A), dict(a=0xD8D0A0, b=0xB8B07A)]) for rows in HUSK], 7)
    for _ in range(16):
        x, y = r.randint(0, 15), min(15, int(abs(r.gauss(0, 7))))
        if free(im, x, y):
            put(im, x, y, rgb(r.choice([0xE8B83A, 0xD8A02A])))
    return im


def tall_grass(r):
    im = canvas(2)
    greens = [0x7A8E3A, 0x8EA246, 0x66782E, 0xA6B458, 0xB8A848]
    for _ in range(26):
        x, n, lean = r.uniform(0, 15), r.randint(8, 27), r.uniform(-.28, .28)
        c = rgb(r.choice(greens))
        for y in range(n):
            put(im, round(x + lean * y + math.sin(y * .3) * .5), y, tone(c, 1 - y / n * .18))
    return im


def growing_things(r):
    im = canvas(2)
    stem, leaf, tip = rgb(0x3E6A2A), rgb(0x5E9A3A), rgb(0x8EC85A)
    for x0, n in ((4, 28), (10, 21), (7, 12)):
        x = float(x0)
        for y in range(n):
            xi = round(x)
            put(im, xi, y, stem)
            if y % 4 == 2:
                side = 1 if (y // 4) % 2 else -1
                put(im, xi + side, y, leaf)
                put(im, xi + side * 2, y - 1, leaf)
            x += math.sin(y * .45 + x0) * .6
            x = max(1, min(14, x))
        put(im, round(x), n, tip)
        put(im, round(x) + 1, n + 1, tip)
    return im


def pinehook(r):
    im = canvas(1)
    needles(im, r, [0x7A5226, 0x8A5E2A, 0x6E4A22], 26, (2, 4), near=.5)
    cap = ['.aa.', 'abba', 'abba', '.aa.']
    strew(im, r, [(cap, [dict(a=0xB8BCC2, b=0xC8322A), dict(a=0xB8BCC2, b=0x2A6A3A), dict(a=0xC8CCD2, b=0xE8B82A)])], 3, near=.6)
    return im


def the_drawing(r):
    im = canvas(1)
    slip = (['aa', 'ab', 'aa'], ['aaa', 'aba'], ['aa', 'aa'], ['aba', 'aaa'])
    strew(im, r, [(rows, [dict(a=0xF6F4EC, b=0xDAD6C8)]) for rows in slip], 10, near=.5)
    sprite(im, 7, 7, ['aaa', 'aca', 'aaa'], dict(a=0xF6F4EC, c=0x141414))                    # one of them is marked
    return im


def corner_house(r):
    im = canvas(1)
    drift(im, r, [(0xB8A888, 2), (0xA89878, 1)], 5, body=.35, veil=200)
    strew(im, r, [(rows, CURLED_PAL) for rows in CURLED], 14, near=.5, margin=0)
    return im


def black_city(r):
    im = canvas(2)
    drift(im, r, [(0xF4F6FA, 4), (0xDCE4F0, 2), (0xC4D0E2, 1)], 27, body=.9)
    for _ in range(46):                                   # the stone the wind takes off the walls
        x, y = r.randint(0, 15), min(31, int(abs(r.gauss(0, 12))))
        if im.getpixel((x, y))[3]:
            put(im, x, y, rgb(r.choice([0x16161A, 0x24242A, 0x0C0C10])))
    return im


def kept(r):
    im = canvas(1)
    for y in range(13):                                   # punched tape, torn off
        for x in range(5, 10):
            if y == 12 and x in (5, 9):
                continue
            put(im, x, y, rgb(0xE6D8A0 if (x + y) % 6 else 0xD6C88E))
        put(im, 7, y, rgb(0x3A3426))                      # the feed holes
        for x in (5, 6, 8, 9):
            if (x * 7 + y * 13) % 5 < 2:
                put(im, x, y, rgb(0x1E1A12))
    for x, y in ((2, 3), (12, 2), (11, 8), (3, 11), (13, 13), (1, 7)):
        put(im, x, y, rgb(0xE6D8A0))                      # what was punched out of it
    return im


def last_orders(r):
    im = canvas(1)
    drift(im, r, [(0xD8C08A, 3), (0xC8AE74, 2)], 6, body=.55)
    sprite(im, 8, 6, ['.aaa.', 'abbba', 'abcba', 'abbba', '.aaa.'], dict(a=0xEEE6CE, b=0x8A2A22, c=0xEEE6CE))    # a beer mat
    sprite(im, 2, 11, ['aaab'], dict(a=0xF0ECE0, b=0xC88A3A))
    return im


def bike_night(r):
    im = canvas(1)
    card = dict(a=0xF6F4EC, b=0xC8322A, c=0xD8D4C8)
    sprite(im, 10, 9, ['aaaa', 'abaa', 'aaaa', 'aaba', 'aaaa', 'cccc'], card)                  # a card off somebody's spokes
    strew(im, r, [(rows, MAPLE_PAL) for rows in MAPLE], 5, near=.5)
    strew(im, r, [(rows, CURLED_PAL) for rows in CURLED], 5, near=.5, margin=0)
    return im


def pell_house(r):
    im = canvas(1)
    for x in range(W):                                    # the line June keeps round the lot
        for y in range(4, 7):
            worn = 8 <= x <= 11                           # a quarter of it scrubbed at
            if r.random() < (.28 if worn else (.9, 1, .85)[y - 4]):
                put(im, x, y, tone(rgb(0xF2F0E8), 1 - r.random() * .07), 150 if worn else 255)
    for x in (2, 6, 13):
        put(im, x, 7, rgb(0xE6E4DC), 200)
    return im


def ones_inside(r):
    im = canvas(1)
    drift(im, r, [(0xE6E0D2, 4), (0xD2CCBE, 2), (0xBEB8AA, 1)], 7, body=.7)
    needles(im, r, [0x8A6A44, 0x9A7A50, 0x7A5C38], 9, (3, 5), near=.5)      # lath
    for _ in range(5):                                    # the hair that bound the plaster
        x, y = r.randint(1, 13), r.randint(3, 13)
        line(im, x, y, x + r.randint(1, 2), y + r.choice((-1, 1)), rgb(0x2A2622), 210)
    return im


DRAWN = {
    'goatman': goatman, 'ted_caver': ted_caver, 'zampano_courtyard': zampano_courtyard, 'whale': whale, 'barn_well': barn_well, 'plain': plain,
    'holloway_camp': holloway_camp, 'wallpaper_nursery': wallpaper_nursery, 'hill_nursery': hill_nursery, 'miniatures': miniatures,
    'usher': usher, 'winchester': winchester, 'child_room': child_room, 'crimson_hall': crimson_hall, 'bly_route': bly_route,
    'mapping_interior': mapping_interior, 'holy_rabbit': holy_rabbit, 'elk_carcasses': elk_carcasses, 'costume_night': costume_night,
    'movie_night': movie_night, 'winter_lake': winter_lake, 'camp_blood': camp_blood, 'devils_rock': devils_rock, 'wheel': wheel,
    'ghosts_set': ghosts_set, 'end_world_cabin': end_world_cabin, 'family_copy': family_copy, 'three_fingers': three_fingers,
    'borrasca': borrasca, 'painted_interior': painted_interior, 'second_waitress': second_waitress, 'layered_man': layered_man, 'silo': silo,
    'way_out': way_out, 'whistlers': whistlers, 'work_print': work_print, 'polaroid_trail': polaroid_trail, 'left_right': left_right,
    'pitt': pitt, 'closing_hall': closing_hall, 'storeroom': storeroom, 'line': line_, 'polybius': polybius, 'replacement': replacement,
    'vacant_possession': vacant_possession, 'second_person': second_person, 'deer_road': deer_road, 'state_route': state_route,
    'marsh_supper': marsh_supper, 'tall_grass': tall_grass, 'growing_things': growing_things, 'pinehook': pinehook,
    'the_drawing': the_drawing, 'corner_house': corner_house, 'black_city': black_city, 'kept': kept, 'last_orders': last_orders,
    'bike_night': bike_night, 'pell_house': pell_house, 'ones_inside': ones_inside,
}


def java_drawn():
    """The bleeds DoorBleed.java says are drawn, with their depth: NAME(new Spec().drawn(2)..."""
    text = JAVA.read_text()
    found = {}
    for name, spec in re.findall(r'^\s*([A-Z][A-Z0-9_]*)\(new Spec\(\)(.*?)\)[,;]\s*(?://.*)?$', text, re.M):
        m = re.search(r'\.drawn\((\d)\)', spec)
        if m:
            found[name.lower()] = int(m.group(1))
    return found


def images():
    return {name: draw(rng(name)) for name, draw in DRAWN.items()}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--sheet')
    args = parser.parse_args()
    made, wanted = images(), java_drawn()
    assert set(made) == set(wanted), (sorted(set(made) - set(wanted)), sorted(set(wanted) - set(made)))
    for name, im in made.items():
        assert im.size == (W, W * wanted[name]), (name, im.size, wanted[name])
        assert im.getbbox() is not None, name
        solid = sum(1 for a in im.getchannel('A').tobytes() if a)
        assert 12 <= solid <= im.width * im.height * .93, (name, solid)   # something there, and never a whole tile
        path = FOLDER / f'{name}.png'
        if args.check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.tobytes(), name
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            im.save(path, optimize=True)
    stale = sorted(f.name for f in FOLDER.iterdir() if f.name not in {f'{name}.png' for name in made})
    assert not stale, stale
    if args.sheet:
        scale, cols = 8, 10
        rows = (len(made) + cols - 1) // cols
        sheet = Image.new('RGBA', (cols * (W * scale + 8) + 8, rows * (2 * W * scale + 8) + 8), (92, 78, 60, 255))
        for i, (name, im) in enumerate(sorted(made.items())):
            big = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
            sheet.alpha_composite(big, (8 + (i % cols) * (W * scale + 8), 8 + (i // cols) * (2 * W * scale + 8)))
        sheet.save(args.sheet)
    print(f"{'Verified' if args.check else 'Drew'} the doors' bleeds: {len(made)} pieces of floor.")


if __name__ == '__main__':
    main()
