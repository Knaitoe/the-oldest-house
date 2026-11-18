"""A Place for You (0.4.257): Mara's house, an ordinary house kept for an ordinary evening.

Draws, in native Minecraft pixels from fixed seeds: nine materials (the sage clapboard, the shingles, the hall's sprigged paper, the
sitting room's striped paper, the floorboards, the kitchen's linoleum, the painted porch boards, the bathroom's small tiles and the
cream trim), and every fitting of the pfy_fitting block, its models facing north and turned by its facing: the kitchen table; Kit's
bag, packed, open, and unpacked into a folded pile; the place card, folded once; a plate with supper, an empty plate and a stack of
three; the kettle; two cups, empty and poured; the radio; Theo's photograph standing and laid face down; the household's pictures on
the wall; the cupboard door Kit's door becomes (lower and upper halves); the serving spoon; a kitchen counter and the counter with
its sink; the stove, cold and lit; the coat hooks; the hall glass as it hangs, turned to the wallpaper and covered with a cloth; and
the sixteen routine items lying flat on a counter, each drawn from that item's own picture. Writes the models and block states, the
carried things' item models (the bag, the spoon, a plate, the cups) and the blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import flicker
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/place/'


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


def frame(im, x0, y0, x1, y1, colour):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# ------------------------------------------------------------------------------------------------ materials
def siding():
    # Sage clapboard: four-pixel boards, a dark shadow under each lap and a light edge on top.
    im = new(base='#8c9c88', seed=2570, spread=3); rnd = random.Random(2571)
    for y in range(16):
        r = y % 4
        for x in range(16):
            if r == 3:
                dot(im, x, y, shade('#5e6c5c', rnd.randint(-3, 3)))
            elif r == 0:
                dot(im, x, y, shade('#a6b4a0', rnd.randint(-3, 3)))
    return im


def shingle():
    im = new(base='#4a4440', seed=2572, spread=4); rnd = random.Random(2573)
    for row in range(4):
        y0 = row * 4; off = 0 if row % 2 == 0 else 3
        for x in range(16):
            dot(im, x, y0 + 3, '#2c2826')
        for x in range(off, 16, 6):
            for y in range(y0, y0 + 3):
                dot(im, x, y, '#2e2a28')
        for _ in range(4):
            dot(im, rnd.randrange(16), y0 + rnd.randrange(3), shade('#5e5852', rnd.randint(-4, 4)))
    return im


def paper():
    # The hall's paper: cream with a small sprig repeated on a staggered grid.
    im = new(base='#dccfae', seed=2574, spread=2)
    for gy, y in enumerate(range(2, 16, 7)):
        for x in range(2 + (gy % 2) * 4, 16, 8):
            dot(im, x, y, '#7a8c5a'); dot(im, x + 1, y + 1, '#7a8c5a'); dot(im, x, y + 2, '#6a7c4c')
            dot(im, x - 1, y + 1, '#b86a6a')
    return im


def stripe():
    im = new(base='#d8c4b4', seed=2575, spread=2)
    for x in range(16):
        if x % 8 in (0, 1):
            for y in range(16):
                dot(im, x, y, shade('#b48c88', random.Random(x * 31 + y).randint(-2, 2)))
        elif x % 8 == 4:
            for y in range(16):
                dot(im, x, y, '#c8aca0')
    return im


def boards():
    base = '#a87a4c'
    im = new(base=base, seed=2576, spread=4); rnd = random.Random(2577)
    for x in range(16):
        dot(im, x, 7, '#6a4a2c'); dot(im, x, 15, '#6a4a2c')
    for y0, joint in ((0, 5), (8, 11)):
        for y in range(y0, y0 + 7):
            dot(im, joint, y, '#6a4a2c')
    for _ in range(10):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if y % 8 != 7:
            dot(im, x, y, shade(base, -14))
    return im


def lino():
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(2578)
    for y in range(16):
        for x in range(16):
            light = ((x // 4) + (y // 4)) % 2 == 0
            px[x, y] = shade('#e2d6bc' if light else '#9a4a42', rnd.randint(-3, 3))
    return im


def porch():
    im = new(base='#7a8a94', seed=2579, spread=3); rnd = random.Random(2580)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#4e5a62')
    for _ in range(14):
        # Worn patches show the wood through the paint.
        x, y = rnd.randrange(16), rnd.randrange(16)
        if y % 4 != 3:
            dot(im, x, y, shade('#9a7a58', rnd.randint(-6, 6)))
    return im


def tile():
    im = Image.new('RGBA', (16, 16)); px = im.load(); rnd = random.Random(2581)
    for y in range(16):
        for x in range(16):
            if x % 4 == 3 or y % 4 == 3:
                px[x, y] = shade('#a8aaa4', rnd.randint(-2, 2))
            else:
                px[x, y] = shade('#ecebe4', rnd.randint(-3, 3))
    return im


def trim():
    im = new(base='#e4dccb', seed=2582, spread=2)
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, '#c8bea8')
    return im


def materials():
    return {'siding': siding(), 'shingle': shingle(), 'paper': paper(), 'stripe': stripe(), 'boards': boards(), 'lino': lino(),
            'porch': porch(), 'tile': tile(), 'trim': trim()}


# ------------------------------------------------------------------------------------------------ fitting textures
def grain(base, seed):
    im = new(base=base, seed=seed, spread=3); rnd = random.Random(seed + 1)
    for _ in range(3):
        y = rnd.randrange(16)
        for x in range(16):
            if rnd.random() < .7:
                dot(im, x, y, shade(base, -12))
    return im


def canvas():
    im = new(base='#6e6a4a', seed=2583, spread=3)
    for y in range(0, 16, 2):
        for x in range(y % 4, 16, 4):
            dot(im, x, y, '#5e5a3c')
    return im


def folded():
    # Folded clothes seen from above and the side: a shirt's stripes, a jumper's knit, a fold line.
    im = new(base='#5a6a82', seed=2584, spread=2)
    for y in range(16):
        for x in range(16):
            if y < 6:
                dot(im, x, y, '#c8c0ac' if (x // 2) % 2 == 0 else '#8a96aa')
            elif y < 10:
                dot(im, x, y, '#8a3a32' if (x + y) % 3 else '#7a3028')
    for x in range(16):
        dot(im, x, 6, '#3a4458'); dot(im, x, 10, '#3a4458')
    return im


def card():
    im = new(base='#f0ebe0', seed=2585, spread=2)
    frame(im, 0, 0, 16, 16, '#d8cfbe')
    for x in range(3, 13):
        if x not in (6, 10):
            dot(im, x, 7 + (x % 2), '#2a2a44')
    for x in range(0, 16):
        dot(im, x, 11, '#cfc4b0')
    return im


def plate(food):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(2586 + food)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d <= 7.6:
                dot(im, x, y, shade('#f2efe6' if d > 5.4 else '#e8e4da', rnd.randint(-2, 2)))
            if d > 6.6 and d <= 7.0:
                dot(im, x, y, '#6a8aa8')
    if food:
        for y in range(16):
            for x in range(16):
                d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
                if d < 4.6:
                    c = '#c8a058' if x < 8 else '#5a7a3a' if y < 8 else '#8a4a30'
                    dot(im, x, y, shade(c, rnd.randint(-8, 8)))
    return im


def kettle():
    im = new(base='#e4dcc4', seed=2587, spread=2)
    for x in range(16):
        dot(im, x, 2, '#c4b898'); dot(im, x, 13, '#b8ac8c')
    fill(im, 6, 5, 10, 9, '#3a6a5a')
    return im


def cup(tea):
    im = new(base='#f2efe8', seed=2588, spread=2)
    if tea:
        fill(im, 1, 1, 15, 15, '#8a5a32')
        for _ in range(6):
            dot(im, random.Random(2589).randrange(2, 14), random.Random(2590).randrange(2, 14), '#a8744a')
    return im


def radio():
    im = new(base='#7a5232', seed=2591, spread=3)
    fill(im, 2, 2, 9, 12, '#3a2a1c')
    for y in range(3, 12, 2):
        for x in range(3, 9):
            dot(im, x, y, '#5a4430')
    fill(im, 10, 3, 14, 7, '#d8c890'); dot(im, 12, 5, '#8a2a20')
    dot(im, 11, 10, '#c4b080'); dot(im, 13, 10, '#c4b080')
    return im


def theo():
    # A small, faded portrait: a man in a work shirt, half turned, not quite looking at the camera. The wall behind him and the edge of a
    # doorframe; his face turned a little to his right, one ear showing, a side parting, two days of stubble along the jaw; the shirt's
    # collar open; the print gone pale at the corners.
    im = new(base='#b8a888', seed=2592, spread=3)
    fill(im, 0, 0, 3, 16, '#a89878'); fill(im, 3, 0, 4, 16, '#968866')
    fill(im, 6, 3, 12, 10, '#c89a7a')
    fill(im, 6, 2, 12, 4, '#4a3a2a'); dot(im, 12, 3, '#4a3a2a'); dot(im, 6, 4, '#4a3a2a'); dot(im, 9, 2, '#5c4a36')
    dot(im, 5, 5, '#b8886a'); dot(im, 5, 6, '#b8886a')
    dot(im, 8, 5, '#3a3026'); dot(im, 10, 5, '#3a3026')
    dot(im, 9, 6, '#b08262'); dot(im, 9, 7, '#a87a5c')
    fill(im, 8, 8, 11, 9, '#9a6e56')
    for x, y in ((6, 8), (7, 9), (8, 9), (11, 9), (11, 8), (10, 9)):
        dot(im, x, y, '#a88466')
    fill(im, 8, 10, 10, 11, '#b8886a')
    fill(im, 2, 11, 15, 16, '#5a6a7a')
    for x in range(2, 15, 3):
        for y in range(11, 16):
            dot(im, x, y, '#4e5e6e')
    fill(im, 7, 11, 11, 12, '#d8d0c0'); dot(im, 8, 12, '#d8d0c0'); dot(im, 10, 12, '#d8d0c0'); dot(im, 9, 12, '#b8886a')
    for x, y in ((0, 0), (1, 0), (0, 1), (15, 0), (14, 0), (15, 1), (0, 15), (0, 14), (1, 15), (15, 15), (14, 15), (15, 14)):
        dot(im, x, y, '#d0c4a6')
    return im


def pictures(theo_in=False):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    # Three frames: a long field, the household in a row, and a small dark one nobody looks at.
    fill(im, 0, 1, 9, 7, '#3a2c22'); fill(im, 1, 2, 8, 6, '#9ab0c4'); fill(im, 1, 4, 8, 6, '#6a8a4a')
    # 0.4.686: the household's frame wider, the four of them standing a pace apart with a gap in the middle the width of one person:
    # Mara (grey hair, plum cardigan), Kit (brown hair, green), then the gap, then Alex (dark hair, slate) and Ren (fair, ochre).
    fill(im, 3, 8, 16, 16, '#3a2c22'); fill(im, 4, 9, 15, 15, '#cbbd9a'); fill(im, 4, 14, 15, 15, '#b8a888')
    people = [(5, '#9a948c', '#7a4a5a'), (7, '#6a4a2e', '#4a6a4a'), (11, '#2e2622', '#5a5a7a'), (13, '#c8a868', '#8a6a3a')]
    if theo_in:
        # The gap filled: a man in the work shirt of the photograph on the sideboard, standing where nobody stood.
        people.append((9, '#4a3a2a', '#5a6a7a'))
    for x, hair, clothes in people:
        dot(im, x, 9, hair); dot(im, x, 10, '#c89a7a'); fill(im, x, 11, x + 1, 13, clothes); dot(im, x, 13, '#3a3026')
    fill(im, 11, 1, 15, 6, '#3a2c22'); fill(im, 12, 2, 14, 5, '#2a2622')
    return im


def cupboard(upper):
    im = new(base='#d4ccb8', seed=2593 + upper, spread=2)
    frame(im, 1, 1 if upper else 0, 15, 16 if upper else 15, '#a89e88')
    frame(im, 3, 3 if upper else 2, 13, 14 if upper else 13, '#bab09a')
    if not upper:
        dot(im, 12, 1, '#8a7a5a'); dot(im, 12, 2, '#8a7a5a')
    return im


def counter():
    im = new(base='#e0d8c4', seed=2595, spread=2)
    frame(im, 1, 2, 15, 15, '#b8ae98')
    fill(im, 6, 4, 10, 5, '#8a8a84')
    for x in range(16):
        dot(im, x, 0, '#6a7a6a'); dot(im, x, 1, '#7a8a7a')
    return im


def worktop():
    im = new(base='#7a8a7a', seed=2596, spread=3)
    for _ in range(8):
        rnd = random.Random(_ + 2597)
        dot(im, rnd.randrange(16), rnd.randrange(16), '#8e9e8e')
    return im


def basin():
    im = worktop()
    fill(im, 2, 3, 14, 13, '#b8bcbe'); fill(im, 3, 4, 13, 12, '#9aa0a4'); dot(im, 8, 8, '#4a4e52'); fill(im, 7, 1, 9, 3, '#c8ccd0')
    return im


def stove():
    im = new(base='#ece8de', seed=2598, spread=2)
    fill(im, 2, 5, 14, 14, '#d8d2c4'); frame(im, 2, 5, 14, 14, '#a8a294')
    fill(im, 5, 6, 11, 7, '#8a8678')
    for x in (3, 6, 9, 12):
        dot(im, x, 2, '#2a2a2a')
    return im


def hob(lit):
    im = new(base='#2a2a2c', seed=2599 + lit, spread=2)
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
        for y in range(16):
            for x in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
                if 2.2 < d <= 3.2:
                    dot(im, x, y, '#d86a2a' if lit and (cx, cy) == (11, 4) else '#4a4a4c')
    return im


def hooks():
    im = grain('#6a4a30', 2601)
    for x in (3, 8, 13):
        dot(im, x, 7, '#c8a040'); dot(im, x, 8, '#a88430')
    return im


def coat(colour, seed):
    im = new(base=colour, seed=seed, spread=3)
    for y in range(16):
        dot(im, 7, y, shade(colour, -20)); dot(im, 8, y, shade(colour, -10))
    for y in range(2, 14, 4):
        dot(im, 9, y, '#d8d0b8')
    return im


def glass():
    im = new(base='#9aaab0', seed=2602, spread=3)
    for i in range(16):
        dot(im, i, 15 - i, '#c4d0d4')
        dot(im, i, min(15, 18 - i), '#b4c2c8')
    return im


def cloth_cover():
    im = new(base='#c8b89a', seed=2603, spread=3)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#b0a084')
    return im




def wicker():
    # The laundry basket's weave: upright stakes every four pixels, and the cane passing in front of one and behind the next,
    # row by row, so the over-and-under shows; a darker gap between rows.
    im = new(base='#b89a62', seed=2620, spread=3)
    for y in range(16):
        row = y // 3
        for x in range(16):
            if y % 3 == 2:
                dot(im, x, y, '#6e5630')
            elif x % 4 == 0:
                dot(im, x, y, '#8a6e40' if (x // 4 + row) % 2 == 0 else '#d2b47a')
            elif (x // 4 + row) % 2 == 0:
                dot(im, x, y, shade('#cdb078', (x * 7 + y) % 5 - 2))
            else:
                dot(im, x, y, shade('#a88a54', (x * 5 + y) % 5 - 2))
    return im


def linen():
    # Sheets folded flat: white with a faint fold and a shadow line at each layer.
    im = new(base='#ece8de', seed=2621, spread=2)
    for x in range(16):
        dot(im, x, 5, '#d4cec0'); dot(im, x, 11, '#d4cec0')
    for y in range(16):
        dot(im, 8, y, shade('#e2ddd0', 0))
    return im


def workshirt():
    # The work shirt folded: its body, a collar band, two buttons; the shirt in Theo's photograph.
    im = new(base='#5a6a7a', seed=2622, spread=3)
    fill(im, 4, 0, 12, 3, '#d8d0c0')
    fill(im, 6, 0, 10, 2, '#4a5868')
    for y in (6, 10, 14):
        dot(im, 8, y, '#d8d0c0')
    for y in range(3, 16):
        dot(im, 7, y, '#4e5c6c')
    return im


def dresser(open_top):
    # Mara's chest of drawers, from the front: three drawers with knobs; opened, the top one pulled out and a dark gap where it was.
    im = grain('#7a5030', 2623)
    for y0, y1 in ((1, 5), (6, 10), (11, 15)):
        frame(im, 1, y0, 15, y1, '#4e3220')
        for x in range(1, 15):
            dot(im, x, y0, '#4e3220')
        dot(im, 5, (y0 + y1) // 2, '#d8b860'); dot(im, 10, (y0 + y1) // 2, '#d8b860')
    if open_top:
        fill(im, 2, 1, 14, 5, '#1e140c')
    return im


def drawer_in():
    # Inside the drawer: a paper liner with a small print, its edges darker.
    im = new(base='#e4dcc8', seed=2624, spread=2)
    for y in range(1, 16, 4):
        for x in range((y // 4) % 2 * 2, 16, 4):
            dot(im, x, y, '#b8a8c0')
    frame(im, 0, 0, 16, 16, '#a89c84')
    return im


def porch_photo():
    # The photograph in the drawer: Mara and Theo side by side on the porch steps the day before; a white border, the house behind.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 16, '#f0ece2')
    fill(im, 1, 1, 15, 15, '#8a9a82')
    for y in range(1, 9, 2):
        for x in range(1, 15):
            dot(im, x, y, '#7a8a72')
    fill(im, 1, 10, 15, 12, '#cbbd9a'); fill(im, 1, 12, 15, 15, '#b8a888')
    for x in range(1, 15):
        dot(im, x, 12, '#9a8c6c')
    # Mara on the left: hair pinned up, her cardigan; Theo on the right, half turned, in the work shirt.
    fill(im, 4, 4, 6, 5, '#5c4030'); fill(im, 4, 5, 6, 7, '#cfa088'); fill(im, 3, 7, 7, 11, '#6a7a5c')
    fill(im, 9, 4, 11, 5, '#4a3a2a'); fill(im, 9, 5, 11, 7, '#c99a7c'); fill(im, 8, 7, 12, 11, '#5a6a7a'); fill(im, 10, 7, 11, 9, '#d8d0c0')
    return im


def room_models():
    m = {}
    walls = [element([3, 0, 3], [13, 1, 13], {'all': 'k'}),
             element([3, 1, 3], [13, 7, 4], {'all': 'k'}), element([3, 1, 12], [13, 7, 13], {'all': 'k'}),
             element([3, 1, 4], [4, 7, 12], {'all': 'k'}), element([12, 1, 4], [13, 7, 12], {'all': 'k'}),
             element([2.5, 6.5, 2.5], [13.5, 7.5, 3.5], {'all': 'd'}), element([2.5, 6.5, 12.5], [13.5, 7.5, 13.5], {'all': 'd'})]
    # Full: sheets heaped over the rim, the shirt at the bottom showing at one side; half: one sheet left and the shirt; empty: the basket.
    m['basket_0'] = model(walls + [element([4, 1, 4], [12, 7.5, 12], {'all': 'l', 'east': 's'}), element([5, 7.5, 5], [11, 9, 11], {'all': 'l'})],
                          {'k': 'wicker', 'd': 'wood_dark', 'l': 'linen', 's': 'workshirt'}, 'wicker')
    m['basket_1'] = model(walls + [element([4, 1, 4], [12, 4, 12], {'all': 'l'}), element([5, 4, 5], [11, 5, 11], {'all': 's'})],
                          {'k': 'wicker', 'd': 'wood_dark', 'l': 'linen', 's': 'workshirt'}, 'wicker')
    m['basket_2'] = model(walls, {'k': 'wicker', 'd': 'wood_dark'}, 'wicker')
    body = {'north': 'f', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}
    top = element([0.5, 14, 1.5], [15.5, 15, 16], {'all': 'w'})
    m['dresser_0'] = model([element([1, 0, 2], [15, 14, 16], body, uv={'north': [1, 1, 15, 15]}), top], {'f': 'dresser', 'w': 'wood'}, 'wood')
    # The top drawer pulled out toward the room, the paper liner inside it and the photograph lying face up on the liner.
    drawer = [element([2, 9.5, -4], [14, 13, -3], {'all': 'w', 'north': 'n'}, uv={'north': [2, 1, 14, 5]}),
              element([2, 9.5, -3], [3, 12.5, 2], {'all': 'w'}), element([13, 9.5, -3], [14, 12.5, 2], {'all': 'w'}),
              element([3, 9.5, -3], [13, 10, 2], {'all': 'w', 'up': 'i'}),
              element([5, 10, -2.5], [10, 10.1, 1.5], {'up': 'p', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}, uv={'up': [0, 0, 16, 16]})]
    m['dresser_1'] = model([element([1, 0, 2], [15, 14, 16], body, uv={'north': [1, 1, 15, 15]}), top] + drawer,
                           {'f': 'dresser_open', 'n': 'dresser', 'w': 'wood', 'i': 'drawer_in', 'p': 'porch_photo'}, 'wood')
    return m


def press(upper):
    # The cupboard in the back passage, from the front: two panelled doors in old paint worn back to the wood at the edges and round
    # the knobs; the lower half stands on a plinth, the upper has the doors' top rails and a moulded cornice.
    im = new(base='#7d8a78', seed=2631 + upper, spread=3)
    rnd = random.Random(2633 + upper)
    for x in (0, 7, 8, 15):
        for y in range(16):
            dot(im, x, y, '#5e6a5a')
    if upper:
        fill(im, 0, 0, 16, 2, '#6a7666')
        for x in range(16):
            dot(im, x, 2, '#4e5a4c')
        frame(im, 2, 4, 6, 16, '#66725f'); frame(im, 9, 4, 13, 16, '#66725f')
        for x in range(1, 7):
            dot(im, x, 3, '#8a9786')
        for x in range(9, 15):
            dot(im, x, 3, '#8a9786')
    else:
        frame(im, 2, 0, 6, 11, '#66725f'); frame(im, 9, 0, 13, 11, '#66725f')
        fill(im, 0, 13, 16, 16, '#5a4430')
        for x in range(16):
            dot(im, x, 13, '#3e2e20')
        # The knobs, brass, and the paint worn through round them where hands go.
        for kx in (6, 9):
            dot(im, kx, 2, '#9a7a4a'); dot(im, kx, 3, '#9a7a4a'); dot(im, kx, 1, '#8a6a44')
            dot(im, kx, 1 + 1, '#d8b860')
    # Wear: the paint gone to the wood down the outer edges and the meeting edges of the doors, where hands and shoulders go.
    for y in range(16):
        for x in (0, 15, 7, 8):
            if rnd.random() < (.35 if x in (7, 8) and not upper and y < 6 else .18):
                dot(im, x, y, '#8a6a48')
    return im


def press_side():
    # The side: painted boards running up and down, their joints darker, a little wear at the front edge.
    im = new(base='#76836f', seed=2635, spread=3)
    for x in (3, 8, 12):
        for y in range(16):
            dot(im, x, y, '#62705e')
    for y in (2, 9, 13):
        dot(im, 0, y, '#8a6a48')
    return im


def wedge_wood():
    # Plain pine, worn smooth along one edge, darker where a foot has pushed it in.
    im = grain('#b48c5c', 2637)
    for x in range(16):
        dot(im, x, 15, '#8c6a42')
    for y in range(4):
        dot(im, 2, y + 6, '#9a7850')
    return im


def stool():
    # The step stool's sides: old cream paint, chipped along the edges to the wood.
    im = new(base='#d4cab0', seed=2638, spread=3)
    rnd = random.Random(2639)
    for _ in range(14):
        x, y = rnd.randrange(16), rnd.randrange(16)
        if x in (0, 15) or y in (0, 15) or rnd.random() < .2:
            dot(im, x, y, '#a08060')
    return im


def stool_tread():
    # A tread from above: the paint worn through to the wood in the middle, where feet go.
    im = new(base='#d4cab0', seed=2640, spread=3)
    for y in range(3, 13):
        for x in range(2, 14):
            d = ((x - 7.5) / 5.5) ** 2 + ((y - 7.5) / 4.5) ** 2
            if d < 1:
                dot(im, x, y, '#ac8e66' if d < .55 else '#c2ae8a')
    for x in range(4, 12):
        dot(im, x, 6, '#9c7e58'); dot(im, x + (1 if x < 11 else 0), 9, '#9c7e58')
    return im


def keys():
    # The back door key on its own ring with a paper tag, the way it lay under the tea towels.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for x, y in ((4, 3), (5, 2), (6, 2), (7, 3), (7, 4), (6, 5), (5, 5), (4, 4)):
        dot(im, x, y, '#8c8c84')
    for i in range(7):
        dot(im, 6 + i, 6 + i, '#b89a50')
        dot(im, 7 + i, 6 + i, '#9a7c3a')
    dot(im, 12, 13, '#b89a50'); dot(im, 13, 11, '#b89a50'); dot(im, 11, 12, '#9a7c3a')
    fill(im, 1, 7, 5, 13, '#e8dfc6')
    frame(im, 1, 7, 5, 13, '#c8bc9c')
    dot(im, 2, 9, '#5a5a6a'); dot(im, 3, 9, '#5a5a6a'); dot(im, 2, 11, '#5a5a6a')
    dot(im, 4, 6, '#a89c84'); dot(im, 3, 6, '#a89c84')
    return im


def back_models():
    m = {}
    side = {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}
    # The cupboard, in two halves, shoved along the wall a third at a time: each shove moves it five pixels toward the room's east
    # (the block faces south, so the model's west is the world's east).
    for shove in range(3):
        dx = -5 * shove
        lower = [element([1 + dx, 0, 1], [15 + dx, 16, 15], side, uv={'north': [1, 0, 15, 16]})]
        upper = [element([1 + dx, 0, 1], [15 + dx, 14, 15], {**side, 'up': 's'}, uv={'north': [1, 2, 15, 16]}),
                 element([0.5 + dx, 14, 0.5], [15.5 + dx, 16, 15.5], {'north': 'f', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'},
                         uv={'north': [0, 0, 16, 2]})]
        m[f'press_{shove * 2}'] = model(lower, {'f': 'press', 's': 'press_side'}, 'press_side')
        m[f'press_{shove * 2 + 1}'] = model(upper, {'f': 'press_top', 's': 'press_side'}, 'press_side')
    # The wedge: lying where the cupboard stood (its block faces north, so these are world coordinates), and pushed in under the
    # back door's free edge at the passage's north-west corner, thin end first.
    w = {'all': 'w'}
    m['wedge_0'] = model([element([5, 0, 4], [11, 1, 12], w), element([5, 1, 7], [11, 2, 12], w), element([5, 2, 10], [11, 2.75, 12], w)],
                         {'w': 'wedge_wood'}, 'wedge_wood')
    m['wedge_1'] = model([element([0.5, 0, 0], [4.5, 0.75, 4], w), element([0.5, 0.75, 1.5], [4.5, 1.5, 4], w), element([0.5, 1.5, 3], [4.5, 2.25, 4], w)],
                         {'w': 'wedge_wood'}, 'wedge_wood')

    def step_stool(x0, z0):
        x1 = x0 + 8
        s = {'all': 's', 'up': 't', 'down': 's'}
        return [element([x0, 0, z0], [x0 + 1, 10, z0 + 5], {'all': 's'}), element([x0, 0, z0 + 5], [x0 + 1, 5, z0 + 10], {'all': 's'}),
                element([x1 - 1, 0, z0], [x1, 10, z0 + 5], {'all': 's'}), element([x1 - 1, 0, z0 + 5], [x1, 5, z0 + 10], {'all': 's'}),
                element([x0, 9, z0], [x1, 10, z0 + 5], s, uv={'up': [3, 4, 13, 12]}), element([x0, 4, z0 + 5], [x1, 5, z0 + 10], s, uv={'up': [3, 4, 13, 12]}),
                element([x0 + 1, 5, z0 + 5], [x1 - 1, 9, z0 + 5.5], {'all': 's'}), element([x0 + 1, 0, z0 + 0.5], [x1 - 1, 9, z0 + 1], {'all': 's'})]
    # Standing in the hall's back corner; and against the back door, its tall end to the door's free edge.
    m['stool_0'] = model(step_stool(4, 3), {'s': 'stool', 't': 'stool_tread'}, 'stool')
    m['stool_1'] = model(step_stool(0, 0), {'s': 'stool', 't': 'stool_tread'}, 'stool')
    # The back door key on its ring, for carrying.
    m['keys_0'] = model([element([3, 0, 3], [13, 0.25, 13], {'up': 'k', 'down': 'k'}, uv={'up': [0, 0, 16, 16], 'down': [0, 0, 16, 16]})], {'k': 'keys'}, 'keys')
    return m


def textures():
    out = {f'block/pfy_{k}.png': v for k, v in materials().items()}
    t = {
        'wood': grain('#8a5a34', 2604), 'wood_dark': grain('#4a3020', 2606), 'canvas': canvas(), 'strap': new(base='#5a3a22', seed=2608, spread=3),
        'brass': new(base='#c8a040', seed=2609, spread=4), 'folded': folded(), 'card': card(), 'plate_full': plate(True),
        'plate_empty': plate(False), 'china': new(base='#f0ede4', seed=2610, spread=2), 'kettle': kettle(), 'cup': cup(False),
        'tea': cup(True), 'radio': radio(), 'theo': theo(), 'frame': new(base='#3a2c22', seed=2611, spread=2),
        'card_back': new(base='#a8946c', seed=2612, spread=3), 'pictures': pictures(), 'pictures_theo': pictures(True), 'cupboard_0': cupboard(False),
        'cupboard_1': cupboard(True), 'steel': new(base='#c4c8cc', seed=2613, spread=3), 'counter': counter(), 'worktop': worktop(),
        'basin': basin(), 'stove': stove(), 'hob_0': hob(False), 'hob_1': hob(True), 'hooks': hooks(),
        'coat_a': coat('#3a4a5a', 2614), 'coat_b': coat('#7a5a3a', 2615), 'glass': glass(), 'cover': cloth_cover(),
        'wicker': wicker(), 'linen': linen(), 'workshirt': workshirt(), 'dresser': dresser(False), 'dresser_open': dresser(True),
        'drawer_in': drawer_in(), 'porch_photo': porch_photo(),
        'press': press(False), 'press_top': press(True), 'press_side': press_side(), 'wedge_wood': wedge_wood(), 'stool': stool(),
        'stool_tread': stool_tread(), 'keys': keys(),
    }
    out.update({f'block/place/{k}.png': v for k, v in t.items()})
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rotation=None, rot=None):
    faces = {}
    for f in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(f, textures.get('all'))
        if name is None:
            continue
        faces[f] = {'texture': '#' + name}
        if uv and f in uv:
            faces[f]['uv'] = uv[f]
        if rotation and f in rotation:
            faces[f]['rotation'] = rotation[f]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle):
    tex = {k: (v if ':' in v else P + v) for k, v in textures.items()}
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**tex, 'particle': tex.get(particle, P + particle)}, 'elements': elements}


def fitting_models():
    m = {}
    legs = [element([x, 0, z], [x + 2, 14, z + 2], {'all': 'w'}) for x in (1, 13) for z in (1, 13)]
    m['table_0'] = model([element([0, 14, 0], [16, 16, 16], {'all': 'w'})] + legs, {'w': 'wood'}, 'wood')
    body = element([3, 0, 4], [13, 7, 12], {'all': 'c'})
    straps = [element([x, 0, 3.75], [x + 1, 7.25, 12.25], {'all': 's'}) for x in (5, 10)]
    handles = [element([6, 7.25, 7.5], [10, 8.5, 8.5], {'all': 's'})]
    m['bag_0'] = model([body] + straps + handles + [element([7.25, 4, 3.5], [8.75, 5.5, 4], {'all': 'b'})], {'c': 'canvas', 's': 'strap', 'b': 'brass'}, 'canvas')
    m['bag_1'] = model([element([3, 0, 4], [13, 5, 12], {'all': 'c', 'up': 'f'}), element([3, 5, 11.5], [13, 11, 12.5], {'all': 'c'})] + straps[:0] +
                       [element([7.25, 9, 12.5], [8.75, 10.5, 13], {'all': 'b'})], {'c': 'canvas', 'f': 'folded', 'b': 'brass'}, 'canvas')
    m['bag_2'] = model([element([3, 0, 4], [13, 2, 12], {'all': 'f'}), element([4, 2, 5], [12, 4, 11], {'all': 'f'}), element([5, 4, 6], [11, 5, 10], {'all': 'f'})],
                       {'f': 'folded'}, 'folded')
    m['card_0'] = model([element([5, 0, 7], [11, 4, 9], {'north': 'k', 'south': 'k', 'up': 'k', 'east': 'k', 'west': 'k'},
                                 uv={'north': [5, 5, 11, 9], 'south': [5, 5, 11, 9], 'up': [5, 7, 11, 9], 'east': [7, 5, 9, 9], 'west': [7, 5, 9, 9]})],
                        {'k': 'card'}, 'card')
    m['plate_0'] = model([element([2, 0, 2], [14, 1, 14], {'up': 'p', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}, uv={'up': [2, 2, 14, 14]})],
                         {'p': 'plate_full', 'c': 'china'}, 'plate_full')
    m['plate_1'] = model([element([2, 0, 2], [14, 1, 14], {'up': 'p', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}, uv={'up': [2, 2, 14, 14]})],
                         {'p': 'plate_empty', 'c': 'china'}, 'plate_empty')
    m['plate_2'] = model([element([2, i, 2], [14, i + 1, 14], {'up': 'p', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c'}, uv={'up': [2, 2, 14, 14]}) for i in range(3)],
                         {'p': 'plate_empty', 'c': 'china'}, 'plate_empty')
    m['kettle_0'] = model([element([5, 0, 5], [11, 6, 11], {'all': 'k'}), element([7, 6, 6], [9, 8, 10], {'all': 'd'}), element([7.5, 3, 2.5], [8.5, 5, 5], {'all': 'k'})],
                          {'k': 'kettle', 'd': 'wood_dark'}, 'kettle')
    for stage, top in ((0, 'cup'), (1, 'tea')):
        m[f'cups_{stage}'] = model([element([x, 0, 6], [x + 3, 4, 9], {'all': 'c', 'up': 't'}) for x in (3, 10)] +
                                   [element([x + 3, 1, 7], [x + 4, 3, 8], {'all': 'c'}) for x in (3, 10)], {'c': 'cup', 't': top}, 'cup')
    m['radio_0'] = model([element([2, 0, 5], [14, 9, 12], {'all': 'w', 'north': 'r'}, uv={'north': [2, 7, 14, 16]})], {'w': 'wood', 'r': 'radio'}, 'radio')
    m['photo_0'] = model([element([4, 0, 7], [12, 10, 8], {'north': 't', 'south': 'b', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'}, uv={'north': [4, 3, 12, 13]}),
                          element([7.5, 0, 8], [8.5, 6, 11], {'all': 'f'})], {'t': 'theo', 'b': 'card_back', 'f': 'frame'}, 'theo')
    m['photo_1'] = model([element([4, 0, 3], [12, 1, 13], {'up': 'b', 'down': 't', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f'})],
                         {'t': 'theo', 'b': 'card_back', 'f': 'frame'}, 'card_back')
    m['pictures_0'] = model([element([0, 0, 15], [16, 16, 15.5], {'north': 'p'}, uv={'north': [0, 0, 16, 16]})], {'p': 'pictures'}, 'pictures')
    # 0.4.686: the same pictures with the gap in the household's row filled.
    m['pictures_1'] = model([element([0, 0, 15], [16, 16, 15.5], {'north': 'p'}, uv={'north': [0, 0, 16, 16]})], {'p': 'pictures_theo'}, 'pictures_theo')
    for stage in range(2):
        m[f'cupboard_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'e', 'north': f'c', 'south': f'c'})], {'c': f'cupboard_{stage}', 'e': 'cupboard_1'}, f'cupboard_{stage}')
    m['spoon_0'] = model([element([7.5, 0, 2], [8.5, 0.5, 11], {'all': 's'}), element([6.5, 0, 11], [9.5, 0.75, 14], {'all': 's'})], {'s': 'steel'}, 'steel')
    m['counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 't', 'down': 't'})], {'c': 'counter', 't': 'worktop'}, 'counter')
    m['counter_1'] = model([element([0, 0, 0], [16, 16, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'b', 'down': 't'})], {'c': 'counter', 't': 'worktop', 'b': 'basin'}, 'counter')
    for stage in range(2):
        m[f'stove_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'north': 's', 'south': 'e', 'east': 'e', 'west': 'e', 'up': 'h', 'down': 'e'})],
                                    {'s': 'stove', 'e': 'china', 'h': f'hob_{stage}'}, 'stove')
    m['hooks_0'] = model([element([1, 10, 15], [15, 12, 16], {'all': 'h'}),
                          element([2, 1, 13], [7, 11, 15], {'all': 'a'}, uv={'north': [5, 0, 10, 10], 'south': [5, 0, 10, 10]}),
                          element([9, 3, 13], [14, 11, 15], {'all': 'b'}, uv={'north': [5, 0, 10, 8], 'south': [5, 0, 10, 8]})],
                         {'h': 'hooks', 'a': 'coat_a', 'b': 'coat_b'}, 'hooks')
    face = {'north': 'g', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}
    m['glass_0'] = model([element([3, 0, 15], [13, 16, 16], face, uv={'north': [3, 0, 13, 16]})], {'g': 'glass', 'w': 'wood_dark'}, 'glass')
    m['glass_1'] = model([element([3, 0, 15], [13, 16, 16], {**face, 'north': 'w'})], {'w': 'wood_dark'}, 'wood_dark')
    m['glass_2'] = model([element([3, 0, 15], [13, 16, 16], face, uv={'north': [3, 0, 13, 16]}), element([2, 1, 14.5], [14, 16.5, 15], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c'})],
                         {'g': 'glass', 'w': 'wood_dark', 'c': 'cover'}, 'cover')
    for i in range(16):
        m[f'routine_{i}'] = model([element([2, 0, 2], [14, 0.25, 14], {'up': 'i'}, uv={'up': [0, 0, 16, 16]})], {'i': NS + f'item/read_routine_{i}'}, 'i')
    m.update(room_models())
    m.update(back_models())
    return m


KINDS = ['table', 'bag', 'card', 'plate', 'kettle', 'cups', 'radio', 'photo', 'pictures', 'cupboard', 'routine', 'spoon', 'counter', 'stove', 'hooks', 'glass',
         'basket', 'dresser', 'press', 'wedge', 'stool']
STAGES = {'pictures': 2, 'bag': 3, 'plate': 3, 'cups': 2, 'photo': 2, 'cupboard': 2, 'routine': 16, 'counter': 2, 'stove': 2, 'glass': 3,
          'basket': 3, 'dresser': 2, 'press': 6, 'wedge': 2, 'stool': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 16
# 0.4.688: Theo's photograph, carried from the chest of drawers to the fridge.
CARRIED = [('bag', 'bag_0'), ('spoon', 'spoon_0'), ('plate', 'plate_1'), ('cups', 'cups_0'), ('photo', 'photo_0'),
           ('wedge', 'wedge_0'), ('stool', 'stool_0'), ('key', 'keys_0')]
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 0, 0], 'scale': [0.625, 0.625, 0.625]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.5, 0.5, 0.5]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.375, 0.375, 0.375]},
    'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
    'firstperson_lefthand': {'rotation': [0, 225, 0], 'translation': [0, -2, 0], 'scale': [0.6, 0.6, 0.6]},
}
NAMES = {'pfy_fitting': 'Household Things', 'pfy_siding': 'Sage Clapboard', 'pfy_shingle': 'Roof Shingle', 'pfy_paper': 'Sprigged Wallpaper',
         'pfy_stripe': 'Striped Wallpaper', 'pfy_boards': 'Floorboards', 'pfy_lino': 'Kitchen Linoleum', 'pfy_porch': 'Porch Boards',
         'pfy_tile': 'Bathroom Tile', 'pfy_trim': 'Cream Trim'}
ITEM_NAMES = {'item.the_oldest_house.pfy_carried': 'Something of the Household'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/pfy_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/pfy_{name}'}}
        out[f'blockstates/pfy_{name}.json'] = {'variants': {'': {'model': NS + f'block/pfy_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/place/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/place/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/pfy_fitting.json'] = {'variants': variants}
    for name, parent in CARRIED:
        out[f'models/item/pfy_carried_{name}.json'] = {'parent': NS + f'block/place/{parent}', 'display': DISPLAY}
    out['models/item/pfy_carried.json'] = {'parent': NS + 'item/pfy_carried_bag',
                                           'overrides': [{'predicate': {'custom_model_data': i + 1}, 'model': NS + f'item/pfy_carried_{name}'} for i, (name, _) in enumerate(CARRIED)]}
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
            if '/place/' in tex:
                assert f'block/place/{tex.split("place/")[1]}.png' in images, (key, tex)
            else:
                assert (A / 'textures' / (tex.split(':')[1] + '.png')).exists(), (key, tex)
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
            assert json.loads(path.read_text()) == flicker.settled(obj), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(flicker.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" Mara's house: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
