"""The last swimmer (0.4.227): a small municipal pool at closing.

Draws, in native Minecraft pixels from fixed seeds: nine materials (pool tile; the dark lane line on the pool floor; the deck's
grip tile; glazed wall tile and its band; acoustic ceiling panel; the office's painted blockwork; corridor lino; the deck's
depth tiles), and every fitting of the swimmer_fitting block, its models facing north and turned by its facing: a lane rope's
floats; the pool cover flat, pressed up from underneath, and its leading edge with the bar and the strap; the cover reel full,
half run out and empty, and its stand with the crank wheel; six numbered starting blocks; the pace clock with its sweep hand at
each of twelve marks; the pool ladder; a changing-room bench; lockers shut and one ajar; the office radio off and on; the closing
checklist on its clipboard; a stack of kickboards; a ceiling lamp lit and off; the exit sign; a folded towel; a drain grate; and
two wall signs (no running; last swim). Writes the models and block states, and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import math
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/swimmer/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=4):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, a=255):
    px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
        im.putpixel((x, y), shade(colour, 0, a))


# A three-by-five pixel alphabet for the signs and numbers.
GLYPHS = {
    '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
    '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
    '6': ['111', '100', '111', '101', '111'], '9': ['111', '101', '111', '001', '111'], '.': ['000', '000', '000', '000', '010'],
    'N': ['101', '111', '111', '111', '101'], 'O': ['111', '101', '101', '101', '111'], 'R': ['110', '101', '110', '101', '101'],
    'U': ['101', '101', '101', '101', '111'], 'I': ['111', '010', '010', '010', '111'], 'G': ['111', '100', '101', '101', '111'],
    'L': ['100', '100', '100', '100', '111'], 'A': ['010', '101', '111', '101', '101'], 'S': ['111', '100', '111', '001', '111'],
    'T': ['111', '010', '010', '010', '010'], 'W': ['101', '101', '111', '111', '101'], 'M': ['101', '111', '111', '101', '101'],
    'E': ['111', '100', '110', '100', '111'], 'X': ['101', '101', '010', '101', '101'], ' ': ['000', '000', '000', '000', '000'],
}


def text(im, x, y, word, colour, step=4):
    for ch in word:
        for j, row in enumerate(GLYPHS[ch]):
            for i, on in enumerate(row):
                if on == '1':
                    dot(im, x + i, y + j, colour)
        x += step


# 0.4.403: other widths of some letters, so a word fits the face it is painted on (the signs' last letters ran off their faces), and
# an N with its diagonal (three pixels wide, it is a solid block).
NARROW = {'I': ['1', '1', '1', '1', '1'], 'L': ['10', '10', '10', '10', '11'], 'N': ['1001', '1101', '1011', '1001', '1001']}


def word(im, x, y, letters, colour, narrow=''):
    """Paints a word glyph by glyph, one pixel between letters; the letters named in {@code narrow} are drawn narrow."""
    for ch in letters:
        glyph = NARROW[ch] if ch in narrow else GLYPHS[ch]
        for j, row in enumerate(glyph):
            for i, on in enumerate(row):
                if on == '1':
                    dot(im, x + i, y + j, colour)
        x += len(glyph[0]) + 1


def width(letters, narrow=''):
    return sum(len((NARROW[ch] if ch in narrow else GLYPHS[ch])[0]) + 1 for ch in letters) - 1


# A runner, for the no-running sign: head, leaning body, one arm forward and one back, legs apart.
RUNNER = ['....11.', '....11.', '..111..', '.1.11.1', '...1...', '..1.1..', '.1...1.']


def tiles(base, grout, seed, size=4):
    im = new(base=base, seed=seed, spread=3)
    for y in range(16):
        for x in range(16):
            if x % size == 0 or y % size == 0:
                dot(im, x, y, grout)
    return im


# ------------------------------------------------------------------------------------------------ materials
def materials():
    m = {}
    m['pool_tile'] = tiles('#d6e2e6', '#a8bcc4', 227)
    line = tiles('#d6e2e6', '#a8bcc4', 2271)
    fill(line, 5, 0, 11, 16, '#2a3a4a')
    m['pool_line'] = line
    deck = new(base='#b8b2a4', seed=2272, spread=6)
    for y in range(0, 16, 8):
        for x in range(16):
            dot(deck, x, y, '#9a9486')
    for x in range(0, 16, 8):
        for y in range(16):
            dot(deck, x, y, '#9a9486')
    rnd = random.Random(2273)
    for _ in range(22):
        dot(deck, rnd.randrange(16), rnd.randrange(16), '#a8a294')
    m['deck'] = deck
    m['wall_tile'] = tiles('#e4e6e0', '#c4c6c0', 2274, 8)
    band = tiles('#3a5a72', '#2a4a62', 2275, 8)
    fill(band, 0, 7, 16, 9, '#e4e6e0')
    m['wall_band'] = band
    ceil = new(base='#d8d6ce', seed=2276, spread=3)
    rnd = random.Random(2277)
    for _ in range(40):
        dot(ceil, rnd.randrange(16), rnd.randrange(16), '#b8b6ae')
    for x in range(16):
        dot(ceil, x, 15, '#9a988e'); dot(ceil, 15, x, '#9a988e')
    m['ceiling'] = ceil
    block = new(base='#c8cabc', seed=2278, spread=3)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(block, x, y, '#a8aa9c')
    for y in range(16):
        dot(block, 0 if (y // 4) % 2 == 0 else 8, y, '#a8aa9c')
    m['blockwork'] = block
    lino = new(base='#8a8a7a', seed=2279, spread=5)
    rnd = random.Random(2280)
    for _ in range(30):
        dot(lino, rnd.randrange(16), rnd.randrange(16), rnd.choice(['#9a9a8a', '#7a7a6a']))
    m['lino'] = lino
    # 0.4.581: the pool's own lamp, a lens set in a wall tile under the water; and the same lamp off.
    for name, glass, halo in (('pool_lamp', '#fbfeff', '#cfeefa'), ('pool_lamp_off', '#9fb0b8', '#8496a0')):
        lens = tiles('#d6e2e6', '#a8bcc4', 2281)
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 7.5, y - 7.5)
                if d < 2.6:
                    dot(lens, x, y, glass)
                elif d < 4.1:
                    dot(lens, x, y, halo)
                elif d < 5.2:
                    dot(lens, x, y, '#8ea2ac' if name == 'pool_lamp' else '#6e7e88')
        m[name] = lens
    return m


def depth(n):
    im = materials()['deck'].copy()
    fill(im, 2, 4, 14, 12, '#e8e6dc')
    text(im, 3, 5, '1.0' if n == 0 else '2.0', '#1e2a3a')
    dot(im, 13, 9, '#1e2a3a'); dot(im, 13, 10, '#1e2a3a')
    return im


# ------------------------------------------------------------------------------------------------ fittings
def rope():
    out = {}
    for name, c in (('float_a', '#c83a32'), ('float_b', '#e8e8e2')):
        im = new(base=c, seed=2290 + len(name), spread=4)
        for x in range(16):
            dot(im, x, 0, shade(c, 18)[:3]); dot(im, x, 15, shade(c, -30)[:3])
        out[name] = im
    return out


def cover():
    """0.4.581: a pale blue vinyl with a welded seam every half block. It was near black, and nothing on it showed in low light."""
    im = new(base='#4a7cae', seed=2295, spread=3)
    for x in (0, 8):
        for y in range(16):
            dot(im, x, y, '#3a6898'); dot(im, x + 1, y, '#5e90c0')
    rnd = random.Random(22951)
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#5688ba')
    return im


def cover_side():
    im = new(base='#3c6a9a', seed=2296, spread=2)
    for x in range(16):
        dot(im, x, 0, '#6a9cca')
    return im


def toned(k):
    """The cover's own picture, lighter or darker throughout, so that its seams run on across a piece drawn in it."""
    im = cover(); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(px[x, y], k)
    return im


def rise():
    """The cover where it is drawn up round what presses on it: stretched, and a little paler for it."""
    return toned(14)


def sole():
    """The cover drawn tight over what is pressed against it from underneath: stretched thin, and much the palest there. In the
    game's frames a sole only a little paler than the cover did not show from above or from across the pool."""
    return toned(70)


def lee():
    """The cover in the shadow at the foot of a rise."""
    return toned(-30)


def edge():
    im = new(base='#8a8e94', seed=2297, spread=3)
    for x in range(16):
        dot(im, x, 0, '#aeb2b8'); dot(im, x, 15, '#5a5e64')
    return im


def strap():
    im = new(base='#d8c060', seed=2298, spread=4)
    for y in range(16):
        dot(im, 0, y, '#a89040'); dot(im, 15, y, '#a89040')
    return im


def reel_face(stage):
    im = new(base='#3a4456', seed=2300 + stage, spread=2)
    rnd = random.Random(2340 + stage)
    r = [7, 4, 1][stage]
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > r + 0.5:
                im.putpixel((x, y), shade('#3a4456', 0))
            if abs(d - 1.2) < 0.6:
                im.putpixel((x, y), shade('#9aa0a8', 0))
            elif d <= r and int(d * 2) % 3 == 0:
                im.putpixel((x, y), shade('#3a6898', 0))
            elif d <= r:
                # 0.4.581: the cover rolled up is the cover's own blue.
                im.putpixel((x, y), shade('#4a7cae', rnd.randint(-3, 3)))
    return im


def wheel():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 5.6 < d < 7.6 or d < 1.6 or (d < 7 and (abs(x - 7.5) < 0.8 or abs(y - 7.5) < 0.8)):
                im.putpixel((x, y), shade('#b8bcc2', int((x - y) * 1.5)))
    dot(im, 13, 3, '#2a2a2e'); dot(im, 13, 4, '#2a2a2e')
    return im


def start(n):
    im = new(base='#e4e2dc', seed=2310 + n, spread=3)
    fill(im, 0, 0, 16, 2, '#3a5a72')
    text(im, 6, 6, str(n + 1), '#1e2a3a')
    return im


def pace(stage):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 7.6:
                im.putpixel((x, y), shade('#f0eee6', 0) if d < 6.8 else shade('#2a2a2e', 0))
    for k in range(12):
        a = k * math.pi / 6
        x, y = 7.5 + 5.6 * math.sin(a), 7.5 - 5.6 * math.cos(a)
        dot(im, round(x), round(y), '#2a2a2e' if k % 3 else '#c83a32')
    a = stage * math.pi / 6
    for t in range(0, 6):
        x, y = 7.5 + t * math.sin(a), 7.5 - t * math.cos(a)
        dot(im, round(x), round(y), '#c83a32')
    dot(im, 7, 7, '#2a2a2e'); dot(im, 8, 8, '#2a2a2e')
    return im


def lockers(open_one):
    im = new(base='#5a7a8a', seed=2320, spread=3)
    fill(im, 7, 0, 9, 16, '#3a5a6a')
    for x0 in (1, 9):
        for y in range(2, 6, 2):
            fill(im, x0 + 1, y, x0 + 5, y + 1, '#2a4a5a')
        dot(im, x0 + 5, 9, '#c8c8cc')
    if open_one:
        fill(im, 9, 0, 16, 16, '#14181c'); fill(im, 10, 3, 15, 4, '#3a3a3e')
    return im


def radio(on):
    im = new(base='#6a4a30', seed=2330, spread=3)
    for y in range(2, 14, 2):
        for x in range(2, 8):
            dot(im, x, y, '#3a2a1c')
    fill(im, 9, 3, 15, 8, '#f0d890' if on else '#8a8270')
    dot(im, 11 if on else 10, 4, '#c83a32'); dot(im, 11 if on else 10, 5, '#c83a32')
    fill(im, 10, 10, 13, 13, '#2a2a2e')
    return im


def clipboard():
    im = new(base='#8a6a42', seed=2340, spread=3)
    fill(im, 2, 1, 14, 15, '#eeeae0')
    fill(im, 5, 0, 11, 2, '#9a9aa0')
    for y in range(4, 14, 2):
        fill(im, 3, y, 12 - (y % 4), y + 1, '#5a5a66')
    for y in (4, 6):
        dot(im, 12, y, '#2a6a3a')
    return im


def boards():
    im = Image.new('RGBA', (16, 16))
    for y in range(16):
        c = ['#3a6ab0', '#e0c040', '#c84a3a', '#3aa060'][(y // 4) % 4]
        for x in range(16):
            im.putpixel((x, y), shade(c, -20 if y % 4 == 3 else 0))
    return im


def lamp(on):
    im = new(base='#f4f0e0' if on else '#8a8a84', seed=2350, spread=2)
    for x in range(16):
        for y in (0, 15):
            dot(im, x, y, '#9a9a94'); dot(im, y, x, '#9a9a94')
    return im


def exit_sign():
    # 0.4.403: the face is fifteen pixels by nine (rows 3 to 11), a dark rule above and below, EXIT in white with a pixel to spare
    # all round (it was drawn sixteen wide on a twelve-pixel face, and its T ran off).
    im = new(base='#2a8a4a', seed=2360, spread=2)
    for x in range(16):
        dot(im, x, 3, '#1a5a2a'); dot(im, x, 11, '#1a5a2a')
    word(im, (15 - width('EXIT', 'I')) // 2, 5, 'EXIT', '#f0f4ee', 'I')
    return im


def towel():
    im = new(base='#e6e2d8', seed=2370, spread=3)
    for y in (3, 12):
        for x in range(16):
            dot(im, x, y, '#3a5a72')
    return im


def drain():
    im = new(base='#8a8e94', seed=2380, spread=2)
    for x in range(1, 15, 2):
        for y in range(1, 15):
            dot(im, x, y, '#2a2e34')
    return im


def sign(n):
    # 0.4.403: the whole sixteen-pixel face, a red rule along the top and the bottom. The words were cut off by a face twelve pixels
    # wide; now every letter sits inside it. No running is NO over a running figure; last swim is LAST over SWIM.
    im = new(base='#f0eee6', seed=2390 + n, spread=2)
    fill(im, 0, 0, 16, 1, '#c83a32'); fill(im, 0, 15, 16, 16, '#c83a32')
    if n == 0:
        word(im, (16 - width('NO', 'N')) // 2, 2, 'NO', '#1e2a3a', 'N')
        for j, row in enumerate(RUNNER):
            for i, on in enumerate(row):
                if on == '1':
                    dot(im, 4 + i, 8 + j, '#1e2a3a')
    else:
        word(im, (16 - width('LAST', 'L')) // 2, 3, 'LAST', '#1e2a3a', 'L')
        word(im, (16 - width('SWIM', 'I')) // 2, 9, 'SWIM', '#1e2a3a', 'I')
    return im


def element(lo, hi, textures, uv=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


# 0.4.581: a foot pressed up under the cover. Drawn once, as the left foot of someone walking north (toward z 0), in sixteenths of
# the cell: the cover swollen a little round it, and the foot standing up out of the swell, with its toes, ball, arch and heel. The
# big toe is on the inner side. The right foot is its mirror; each is turned to the way it walks. The flat sheet under them is
# never turned, so its seams still meet its neighbours'. (Two rises under the foot read, in the game, as a stepped pyramid.)
SHEET = 15.5
PAD = SHEET + .75


def rounded(lo, hi, cut=1):
    """A low rise with its corners taken off: a middle the whole length, and a strip at each side that stops short of the ends."""
    (x0, y0, z0), (x1, y1, z1) = lo, hi
    return [([x0 + cut, y0, z0], [x1 - cut, y1, z1], 'mound'), ([x0, y0, z0 + cut], [x0 + cut, y1, z1 - cut], 'mound'), ([x1 - cut, y0, z0 + cut], [x1, y1, z1 - cut], 'mound')]


FOOT = [
    # the cover swollen round it
    *rounded([1, SHEET, 2], [10, PAD, 14], 1.5),
    # four toes, the big one on the inner side and furthest forward, each a little apart from the next
    ([6.9, PAD, 3.5], [8.5, PAD + 1.5, 5], 'sole'),
    ([5.6, PAD, 3.8], [6.6, PAD + 1.5, 5], 'sole'),
    ([4.3, PAD, 4.1], [5.3, PAD + 1.5, 5], 'sole'),
    ([3, PAD, 4.4], [4, PAD + 1.5, 5], 'sole'),
    # the ball, the arch (taken in on the inner side, and not pressed so hard), the heel
    ([2.5, PAD, 5.2], [8.5, PAD + 2, 8.2], 'sole'),
    ([2.5, PAD, 8.2], [6.5, PAD + 1.25, 10.2], 'sole'),
    ([2.5, PAD, 10.2], [7.5, PAD + 2, 12.7], 'sole'),
]
FOOT_WAYS = ['north', 'east', 'south', 'west']
# The cover is laid facing west (SwimmerSite.cover), and a block's facing turns its whole model: a foot is drawn turned back by as
# much, so that the way in its stage's number is the way it walks in the pool.
COVER_FACING = 'west'


def foot_stage(way, right):
    """The cover's stage for a foot: the way it walks and which foot. SwimmerBlock.foot is the same sum; main() compares them."""
    n = FOOT_WAYS.index(way) * 2 + (1 if right else 0)
    return 1 if n == 0 else n + 2


def turned(lo, hi, way, right):
    """A box of the left foot walking north, as the other foot's (mirrored across the cell) and turned to the way it walks."""
    (x0, y0, z0), (x1, y1, z1) = lo, hi
    if right:
        x0, x1 = 16 - x1, 16 - x0
    for _ in range(FOOT_WAYS.index(way)):
        x0, z0, x1, z1 = 16 - z1, x0, 16 - z0, x1
    return [x0, y0, z0], [x1, y1, z1]


def raised(lo, hi, top, side):
    """A box standing on the sheet: no underside, and each face a piece of its picture of the face's own size."""
    (x0, y0, z0), (x1, y1, z1) = lo, hi
    h = y1 - y0
    uv = {'up': [x0, z0, x1, z1], 'north': [16 - x1, 0, 16 - x0, h], 'south': [x0, 0, x1, h], 'west': [z0, 0, z1, h], 'east': [16 - z1, 0, 16 - z0, h]}
    return element(lo, hi, {'up': top, 'north': side, 'south': side, 'east': side, 'west': side}, uv)


def fitting_models():
    m = {}
    m['rope_0'] = model([element([6.5, 13, z], [9.5, 15.5, z + 4], {'all': 'a' if z % 8 == 0 else 'b'}) for z in range(0, 16, 4)], {'a': 'float_a', 'b': 'float_b'}, 'float_a')
    flat = element([0, 13.5, 0], [16, 15.5, 16], {'up': 'c', 'down': 'c', 'north': 's', 'south': 's', 'east': 's', 'west': 's'})
    m['cover_0'] = model([flat], {'c': 'cover', 's': 'cover_side'}, 'cover')
    for way in FOOT_WAYS:
        drawn = FOOT_WAYS[(FOOT_WAYS.index(way) - FACING_Y[COVER_FACING] // 90) % 4]
        for right in (False, True):
            parts = [flat] + [raised(*turned(lo, hi, drawn, right), *(('r', 'l') if kind == 'mound' else ('t', 'l'))) for lo, hi, kind in FOOT]
            m[f'cover_{foot_stage(way, right)}'] = model(parts, {'c': 'cover', 's': 'cover_side', 'r': 'rise', 't': 'sole', 'l': 'lee'}, 'cover')
    m['cover_2'] = model([flat, element([0, 14, 0], [16, 17, 2], {'all': 'e'}), element([6, 15, 1], [10, 18, 4], {'all': 't'})], {'c': 'cover', 's': 'cover_side', 'e': 'edge', 't': 'strap'}, 'cover')
    for s, (a, b) in enumerate(((3, 13), (5, 11), (7, 9))):
        m[f'reel_{s}'] = model([element([0, a, a], [16, b, b], {'north': 'c', 'south': 'c', 'up': 'c', 'down': 'c', 'east': 'f', 'west': 'f'})], {'c': 'cover' if s < 2 else 'edge', 'f': f'reel_face_{s}'}, 'cover')
    m['reel_end_0'] = model([element([6, 0, 6], [10, 9, 10], {'all': 'e'}), element([2, 1, 13], [14, 13, 14], {'north': 'w', 'south': 'w'})], {'e': 'edge', 'w': 'wheel'}, 'edge')
    for n in range(6):
        m[f'start_{n}'] = model([element([2, 0, 2], [14, 7, 14], {'all': 'side', 'up': 'top'}), element([2, 7, 2], [14, 9, 12], {'all': 'side', 'up': 'top'})], {'side': 'start_side', 'top': f'start_{n}'}, 'start_side')
    for n in range(12):
        m[f'pace_clock_{n}'] = model([element([0, 0, 15], [16, 16, 16], {'north': 'f', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'})], {'f': f'pace_{n}', 'r': 'edge'}, 'edge')
    m['ladder_0'] = model([element([3, 0, 13], [4, 16, 14], {'all': 'e'}), element([12, 0, 13], [13, 16, 14], {'all': 'e'}), element([3, 3, 13], [13, 4, 14], {'all': 'e'}),
                           element([3, 9, 13], [13, 10, 14], {'all': 'e'}), element([3, 15, 10], [4, 16, 14], {'all': 'e'}), element([12, 15, 10], [13, 16, 14], {'all': 'e'})], {'e': 'edge'}, 'edge')
    for n in range(2):
        m[f'depth_{n}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'd', 'up': 't'})], {'d': 'deck', 't': f'depth_{n}'}, 'deck', 'solid')
        m[f'lockers_{n}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'locker_side', 'f': f'lockers_{n}'}, 'locker_side', 'solid')
        m[f'radio_{n}'] = model([element([3, 0, 6], [13, 6, 10], {'all': 's', 'north': 'f'}), element([11, 6, 8], [12, 11, 9], {'all': 'e'})], {'s': 'radio_side', 'f': f'radio_{n}', 'e': 'edge'}, 'radio_side')
        m[f'lamp_{n}'] = model([element([2, 14, 2], [14, 16, 14], {'all': 'r', 'down': 'l'})], {'r': 'edge', 'l': f'lamp_{n}'}, 'edge')
        m[f'sign_{n}'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 's', 'south': 's'}, {'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16]})], {'s': f'sign_{n}'}, f'sign_{n}')
    m['bench_0'] = model([element([0, 6, 3], [16, 8, 13], {'all': 'b'}), element([1, 0, 4], [3, 6, 12], {'all': 'e'}), element([13, 0, 4], [15, 6, 12], {'all': 'e'})], {'b': 'bench', 'e': 'edge'}, 'bench')
    m['clipboard_0'] = model([element([4, 0, 3], [12, 1, 13], {'all': 's', 'up': 'c'}, {'up': [2, 1, 14, 15]})], {'c': 'clipboard', 's': 'radio_side'}, 'clipboard')
    m['boards_0'] = model([element([2, 0, 4], [14, 7, 12], {'all': 'b'})], {'b': 'boards'}, 'boards')
    m['exit_sign_0'] = model([element([0.5, 4, 15], [15.5, 13, 16], {'north': 'x', 'south': 'x', 'up': 'r', 'down': 'r', 'east': 'r', 'west': 'r'}, {'north': [0, 3, 15, 12], 'south': [0, 3, 15, 12]})], {'x': 'exit', 'r': 'edge'}, 'exit')
    m['towel_0'] = model([element([3, 0, 4], [13, 2, 12], {'all': 't'})], {'t': 'towel'}, 'towel')
    m['drain_0'] = model([element([2, 0, 2], [14, 0.5, 14], {'up': 'd', 'down': 'd'})], {'d': 'drain'}, 'drain')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/swimmer/{k}.png'] = v
    for k, v in rope().items():
        out[f'block/swimmer/{k}.png'] = v
    out['block/swimmer/cover.png'] = cover(); out['block/swimmer/cover_side.png'] = cover_side(); out['block/swimmer/sole.png'] = sole(); out['block/swimmer/rise.png'] = rise(); out['block/swimmer/lee.png'] = lee()
    out['block/swimmer/edge.png'] = edge(); out['block/swimmer/strap.png'] = strap()
    for s in range(3):
        out[f'block/swimmer/reel_face_{s}.png'] = reel_face(s)
    out['block/swimmer/wheel.png'] = wheel()
    out['block/swimmer/start_side.png'] = new(base='#d4d2cc', seed=2315, spread=3)
    for n in range(6):
        out[f'block/swimmer/start_{n}.png'] = start(n)
    for n in range(12):
        out[f'block/swimmer/pace_{n}.png'] = pace(n)
    for n in range(2):
        out[f'block/swimmer/depth_{n}.png'] = depth(n)
        out[f'block/swimmer/lockers_{n}.png'] = lockers(n == 1)
        out[f'block/swimmer/radio_{n}.png'] = radio(n == 1)
        out[f'block/swimmer/lamp_{n}.png'] = lamp(n == 0)
        out[f'block/swimmer/sign_{n}.png'] = sign(n)
    out['block/swimmer/locker_side.png'] = new(base='#4a6a7a', seed=2321, spread=2)
    out['block/swimmer/radio_side.png'] = new(base='#5a3e28', seed=2331, spread=2)
    out['block/swimmer/bench.png'] = new(base='#b08a5a', seed=2335, spread=5)
    out['block/swimmer/clipboard.png'] = clipboard(); out['block/swimmer/boards.png'] = boards()
    out['block/swimmer/exit.png'] = exit_sign(); out['block/swimmer/towel.png'] = towel(); out['block/swimmer/drain.png'] = drain()
    return out


KINDS = ['rope', 'cover', 'reel', 'reel_end', 'start', 'pace_clock', 'ladder', 'depth', 'bench', 'lockers', 'radio', 'clipboard', 'boards', 'lamp', 'exit_sign', 'towel', 'drain', 'sign']
STAGES = {'cover': 10, 'reel': 3, 'start': 6, 'pace_clock': 12, 'depth': 2, 'lockers': 2, 'radio': 2, 'lamp': 2, 'sign': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 12
NAMES = {'swimmer_fitting': 'Pool Fitting', 'swimmer_pool_tile': 'Pool Tile', 'swimmer_pool_line': 'Lane Line Tile', 'swimmer_deck': 'Pool Deck',
         'swimmer_wall_tile': 'Wall Tile', 'swimmer_wall_band': 'Wall Tile Band', 'swimmer_ceiling': 'Ceiling Panel', 'swimmer_blockwork': 'Painted Blockwork',
         'swimmer_lino': 'Lino', 'swimmer_pool_lamp': 'Pool Light', 'swimmer_pool_lamp_off': 'Pool Light'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/swimmer_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/swimmer_{name}.json'] = {'variants': {'': {'model': NS + f'block/swimmer_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/swimmer/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/swimmer/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/swimmer_fitting.json'] = {'variants': variants}
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
            assert f'block/swimmer/{tex.split("swimmer/")[1]}.png' in images, (key, tex)
    # The block numbers the feet as this tool does.
    java = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/SwimmerBlock.java').read_text()
    assert 'case EAST->1;case SOUTH->2;case WEST->3;default->0;' in java and 'return n==0?1:n+2;' in java, 'SwimmerBlock.foot does not number the feet as the tool does'
    assert sorted(foot_stage(w, r) for w in FOOT_WAYS for r in (False, True)) == [1, 3, 4, 5, 6, 7, 8, 9]
    site = (ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/SwimmerSite.java').read_text()
    assert f'SwimmerBlock.of(COVER,Direction.{COVER_FACING.upper()},' in site, 'SwimmerSite.cover does not lay the cover the way the feet are drawn for'
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
    for block, name in NAMES.items():
        key = f'block.the_oldest_house.{block}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the pool: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
