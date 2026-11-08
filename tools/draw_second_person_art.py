"""Second Person (0.4.274): a flat above a launderette, across a terraced street.

Draws, in native Minecraft pixels from fixed seeds: twelve materials (yellow stock brick, paving slabs, road, granite kerb, the
launderette's green fascia, the flat's faded wallpaper, the kitchen's flowered tiles, a worn carpet, chequered lino, avocado bathroom
tile, an artexed ceiling and the launderette's floor), and every fitting of the sp_fitting block, its models facing north and turned
by its facing: the children's adventure book lying on a shelf; the low bookcase; a moving box, taped, opened and full, opened and
empty, and flattened; the hall glass as it hangs and turned to the wall; the bathroom mirror cabinet, clear and steamed; the airing
cupboard's tank (lower and upper); the photograph from home, standing and face down; the drawing as a loose sheet; the coat folded
over itself; the sixteen routine items lying flat, each from that item's own picture; the launderette's fascia (three parts); the
bus stop flag on its pole; the shift rota on the wall; the tablecloth rumpled, straightened and laid for two; and the telephone on
its hook and off it. 0.4.369, winter: Iris's sewing tin, shut, open with the half-finished sleeve on its needles, and open with
only buttons and needles left; the train ticket in its envelope; the sleeve lying on the armchair's arm, the finished cardigan folded
on the seat, and the sleeve set down flat. Writes the models and block states, the carried thing's item models and the blocks' names. --check verifies
everything.
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
P = NS + 'block/second/'


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
def brick():
    # London stock brick: yellow-grey, sooted, in stretcher bond with recessed grey mortar.
    im = new(base='#b8a47a', seed=2740, spread=6); rnd = random.Random(2741)
    for y in range(16):
        for x in range(16):
            row = y // 4; off = 4 if row % 2 else 0
            if y % 4 == 3 or (x + off) % 8 == 7:
                dot(im, x, y, shade('#8a8478', rnd.randint(-4, 4)))
            elif rnd.random() < .12:
                dot(im, x, y, shade('#7a6a50', rnd.randint(-6, 6)))
    return im


def paving():
    im = new(base='#9a9890', seed=2742, spread=4); rnd = random.Random(2743)
    for i in range(16):
        dot(im, i, 7, '#6e6c66'); dot(im, i, 15, '#6e6c66'); dot(im, 7, i, '#6e6c66'); dot(im, 15, i, '#6e6c66')
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#7c7a74', rnd.randint(-5, 5)))
    return im


def road():
    im = new(base='#3c3c3e', seed=2744, spread=5); rnd = random.Random(2745)
    for _ in range(28):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#5a5a5c', rnd.randint(-6, 8)))
    return im


def kerb():
    im = new(base='#a4a4a0', seed=2746, spread=5); rnd = random.Random(2747)
    for x in range(16):
        dot(im, x, 0, '#c6c6c2'); dot(im, x, 1, '#b8b8b4'); dot(im, x, 15, '#6c6c68')
    for _ in range(14):
        dot(im, rnd.randrange(16), rnd.randrange(2, 15), shade('#888884', rnd.randint(-6, 6)))
    return im


def shopfront():
    # The launderette's fascia and stallriser: bottle-green gloss paint over boards, the paint flaking to grey at the seams.
    im = new(base='#2e5a44', seed=2748, spread=3); rnd = random.Random(2749)
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, '#1e3a2c')
    for y in range(1, 15):
        if y % 5 == 0:
            for x in range(16):
                dot(im, x, y, '#24483a')
    for _ in range(8):
        dot(im, rnd.randrange(16), rnd.randrange(1, 15), shade('#8a8c84', rnd.randint(-6, 6)))
    return im


def paper():
    # The flat's paper: faded orange-brown seventies circles on a cream ground, browned toward the top.
    im = new(base='#d8c8a0', seed=2750, spread=3)
    for cx, cy in ((4, 4), (12, 12)):
        for x in range(16):
            for y in range(16):
                d = ((x - cx) ** 2 + (y - cy) ** 2) ** .5
                if 2.2 <= d <= 3.4:
                    dot(im, x, y, '#b07840')
                elif d < 1.2:
                    dot(im, x, y, '#8a5a30')
    for x, y in ((12, 4), (4, 12)):
        dot(im, x, y, '#c09060'); dot(im, x + 1, y, '#c09060'); dot(im, x, y + 1, '#c09060')
    return im


def kitchen():
    # Cream kitchen tiles, a brown flower on every other one.
    im = new(base='#e4dcc4', seed=2752, spread=2)
    for i in range(16):
        dot(im, i, 7, '#b4ac94'); dot(im, i, 15, '#b4ac94'); dot(im, 7, i, '#b4ac94'); dot(im, 15, i, '#b4ac94')
    for ox, oy in ((0, 0), (8, 8)):
        cx, cy = ox + 3, oy + 3
        for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
            dot(im, cx + dx, cy + dy, '#9a6a3a')
        dot(im, cx, cy, '#d0a040')
    return im


def carpet():
    im = new(base='#7a5a3e', seed=2754, spread=5); rnd = random.Random(2755)
    for _ in range(40):
        dot(im, rnd.randrange(16), rnd.randrange(16), shade('#6a4c34', rnd.randint(-5, 5)))
    for _ in range(6):
        x, y = rnd.randrange(15), rnd.randrange(15)
        dot(im, x, y, '#8e6e50'); dot(im, x + 1, y, '#8e6e50')
    return im


def lino():
    im = new(base='#c8c0a8', seed=2756, spread=3)
    for y in range(16):
        for x in range(16):
            if (x // 4 + y // 4) % 2:
                dot(im, x, y, shade('#8a7a5a', (x * 7 + y * 3) % 5 - 2))
    return im


def tile():
    im = new(base='#8a9a5a', seed=2758, spread=3)
    for i in range(16):
        dot(im, i, 3, '#6a7844'); dot(im, i, 7, '#6a7844'); dot(im, i, 11, '#6a7844'); dot(im, i, 15, '#6a7844')
        dot(im, 3, i, '#6a7844'); dot(im, 7, i, '#6a7844'); dot(im, 11, i, '#6a7844'); dot(im, 15, i, '#6a7844')
    for x, y in ((1, 1), (9, 5), (5, 13)):
        dot(im, x, y, '#a8b878')
    return im


def ceiling():
    # Artex: swirled plaster, off-white, with the swirls picked out in shadow.
    im = new(base='#e4e0d4', seed=2760, spread=2); rnd = random.Random(2761)
    for cx, cy in ((4, 5), (12, 3), (8, 12), (0, 13), (15, 10)):
        for t in range(14):
            ang = t * .55; r = 1 + t * .22
            dot(im, int(cx + r * math.cos(ang)) % 16, int(cy + r * math.sin(ang)) % 16, shade('#c4c0b4', rnd.randint(-3, 3)))
    return im


def launderette():
    im = new(base='#e0dcd0', seed=2762, spread=2)
    for y in range(16):
        for x in range(16):
            if (x // 2 + y // 2) % 2:
                dot(im, x, y, shade('#2a2a2c', (x + y) % 3 - 1))
    return im


def materials():
    return {'brick': brick(), 'paving': paving(), 'road': road(), 'kerb': kerb(), 'shopfront': shopfront(), 'paper': paper(),
            'kitchen': kitchen(), 'carpet': carpet(), 'lino': lino(), 'tile': tile(), 'ceiling': ceiling(), 'launderette': launderette()}


# ------------------------------------------------------------------------------------------------ fitting textures
def grain(base, seed):
    im = new(base=base, seed=seed, spread=3); rnd = random.Random(seed + 1)
    for y in range(16):
        if rnd.random() < .3:
            for x in range(16):
                if rnd.random() < .7:
                    dot(im, x, y, shade(base, -10))
    return im


def book_cover():
    # A children's adventure book, cloth over board, rubbed pale at the corners; a white panel with a little red figure.
    im = new(base='#2a5a8a', seed=2770, spread=3)
    for x, y in ((0, 0), (15, 0), (0, 15), (15, 15), (1, 0), (0, 1), (14, 15), (15, 14)):
        dot(im, x, y, '#8aa0b8')
    fill(im, 3, 3, 13, 10, '#e8e0cc'); frame(im, 3, 3, 13, 10, '#c8b890')
    for y in range(5, 9):
        dot(im, 8, y, '#b03028')
    dot(im, 7, 6, '#b03028'); dot(im, 9, 6, '#b03028'); dot(im, 8, 4, '#d8a070')
    for x in range(4, 12):
        dot(im, x, 12, '#e8e0cc') if x % 2 else None
    return im


def pages():
    im = new(base='#e8e0c8', seed=2771, spread=2)
    for y in range(1, 16, 2):
        for x in range(16):
            dot(im, x, y, '#d0c4a4')
    return im


def shelf_front():
    im = grain('#6a4428', 2772)
    for x in range(16):
        dot(im, x, 0, '#4a2e1a'); dot(im, x, 7, '#4a2e1a'); dot(im, x, 15, '#4a2e1a')
    rnd = random.Random(2773); x = 1
    while x < 15:
        w = rnd.choice((1, 2)); col = rnd.choice(('#8a2a24', '#2a4a6a', '#5a6a2a', '#c8a050', '#e0d8c0', '#3a3a3a'))
        h = rnd.randint(4, 6)
        fill(im, x, 7 - h, x + w, 7, col); fill(im, x, 15 - h + 1, x + w, 15, col)
        x += w + (1 if rnd.random() < .3 else 0)
    return im


def cardboard(tape):
    im = new(base='#b8935c', seed=2774 + tape, spread=4); rnd = random.Random(2775)
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#9a7848')
    if tape:
        fill(im, 6, 0, 10, 16, '#c8b480')
        for y in range(16):
            dot(im, 6, y, '#a8986a'); dot(im, 9, y, '#a8986a')
    return im


def box_inside():
    im = new(base='#8a6a40', seed=2776, spread=3)
    frame(im, 0, 0, 16, 16, '#6a4c2a')
    return im


def box_full():
    im = box_inside()
    fill(im, 2, 2, 7, 8, '#e0dcd0'); fill(im, 8, 3, 14, 7, '#5a6a8a'); fill(im, 3, 9, 13, 13, '#c8b490'); dot(im, 10, 10, '#8a2a24')
    return im


def glass():
    im = new(base='#9ab0b8', seed=2777, spread=3)
    for i in range(16):
        dot(im, i, i, '#c8dce0'); dot(im, i, (i + 1) % 16, '#c8dce0')
    for i in range(3, 9):
        dot(im, 15 - i, i, '#d8eaee')
    return im


def steam():
    im = new(base='#c4ccce', seed=2778, spread=4)
    rnd = random.Random(2779)
    for _ in range(30):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#dde3e4')
    return im


def copper():
    im = new(base='#b06a3a', seed=2780, spread=6); rnd = random.Random(2781)
    for y in (3, 12):
        for x in range(16):
            dot(im, x, y, '#7a4424')
    for _ in range(10):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#d8945a')
    return im


def lagging():
    # The red jacket on the tank, quilted.
    im = new(base='#a8382c', seed=2782, spread=4)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#782620')
    for x in range(0, 16, 5):
        for y in range(16):
            dot(im, x, y, '#782620')
    return im


def photo():
    # A small house front, two figures in front of it, sun-faded.
    im = new(base='#c8c0a0', seed=2783, spread=2)
    fill(im, 0, 0, 16, 6, '#a8c0d0'); fill(im, 3, 4, 13, 12, '#b07a5a'); fill(im, 2, 3, 14, 5, '#5a4038')
    fill(im, 5, 7, 7, 9, '#e0e8e8'); fill(im, 9, 7, 11, 9, '#e0e8e8'); fill(im, 7, 9, 9, 12, '#5a3a2a')
    fill(im, 0, 12, 16, 16, '#7a9a5a')
    for x, c in ((4, '#3a3a5a'), (11, '#8a3a3a')):
        dot(im, x, 10, '#d8a888'); fill(im, x, 11, x + 1, 14, c)
    frame(im, 0, 0, 16, 16, '#f0ece0')
    return im


def sheet():
    im = new(base='#f0ece0', seed=2784, spread=2)
    for x in range(3, 13):
        dot(im, x, 5, '#3a3a3a') if x % 3 else None
    for y in range(4, 12):
        dot(im, 8, y, '#5a5a5a') if y % 2 else None
    return im


def coat():
    im = new(base='#4a4e5a', seed=2785, spread=4)
    for y in range(16):
        dot(im, 7, y, '#36383f'); dot(im, 8, y, '#36383f')
    for y in (4, 9, 13):
        dot(im, 6, y, '#c8b890')
    return im


def fascia(part):
    # LAUNDERETTE in cream letters on the green fascia, across three blocks.
    im = shopfront()
    word = 'LAUNDERETTE'
    glyphs = {
        'L': ['1..', '1..', '1..', '111'], 'A': ['.1.', '1.1', '111', '1.1'], 'U': ['1.1', '1.1', '1.1', '111'],
        'N': ['1.1', '111', '111', '1.1'], 'D': ['11.', '1.1', '1.1', '11.'], 'E': ['111', '11.', '1..', '111'],
        'R': ['11.', '1.1', '11.', '1.1'], 'T': ['111', '.1.', '.1.', '.1.'],
    }
    x0 = 2 - part * 16
    for i, ch in enumerate(word):
        g = glyphs[ch]
        for gy, row in enumerate(g):
            for gx, c in enumerate(row):
                if c == '1':
                    dot(im, x0 + i * 4 + gx, 6 + gy, '#ece0c0')
    return im


def stop_flag():
    im = new(base='#e8e8e0', seed=2786, spread=1)
    frame(im, 0, 0, 16, 16, '#b02828'); frame(im, 1, 1, 15, 15, '#b02828')
    fill(im, 4, 4, 12, 8, '#b02828')
    for x in range(5, 11):
        dot(im, x, 6, '#e8e8e0') if x % 2 else None
    for x in range(4, 12):
        dot(im, x, 11, '#2a2a2a')
    return im


def rota():
    im = new(base='#f4f0e4', seed=2787, spread=1)
    for y in range(2, 15, 2):
        for x in range(2, 14):
            dot(im, x, y, '#a4a09a')
    for x in range(2, 14, 3):
        for y in range(2, 15):
            dot(im, x, y, '#a4a09a')
    for x, y in ((5, 4), (8, 8), (11, 10), (5, 12)):
        fill(im, x, y, x + 2, y + 1, '#3a5a9a')
    return im


def cloth(rumpled):
    im = new(base='#e8e4d8', seed=2788 + rumpled, spread=2)
    for y in range(0, 16, 4):
        for x in range(16):
            dot(im, x, y, '#b8484a')
    for x in range(0, 16, 4):
        for y in range(16):
            dot(im, x, y, '#b8484a')
    if rumpled:
        rnd = random.Random(2790)
        for _ in range(18):
            dot(im, rnd.randrange(16), rnd.randrange(16), '#c4bfb2')
    return im


def plate():
    im = new(base='#f0ede4', seed=2791, spread=1)
    frame(im, 2, 2, 14, 14, '#d4d0c4'); fill(im, 5, 5, 11, 11, '#c8a060'); fill(im, 6, 6, 9, 9, '#8a4a2a')
    return im


def bakelite():
    im = new(base='#2a2624', seed=2792, spread=3)
    for x in range(16):
        dot(im, x, 1, '#4a4440')
    return im


def dial():
    im = new(base='#2a2624', seed=2793, spread=2)
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 6:
                dot(im, x, y, '#e0dcd0')
            if 3.5 < d < 5.2 and (x + y) % 3 == 0:
                dot(im, x, y, '#2a2624')
    dot(im, 7, 7, '#b08a40'); dot(im, 8, 8, '#b08a40')
    return im


def tin_lid():
    # A biscuit tin kept for sewing: a painted lid of flowers round a ring, its rim worn to metal.
    im = new(base='#2c4a6a', seed=2810, spread=3)
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 6.2 < d < 7.4:
                dot(im, x, y, '#c8b070')
            elif d < 3:
                dot(im, x, y, '#d8d0b8')
    rnd = random.Random(2811)
    for _ in range(9):
        x, y = rnd.randrange(2, 14), rnd.randrange(2, 14)
        if 3.5 < ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5 < 6:
            dot(im, x, y, '#c85a5a'); dot(im, x + 1, y, '#e8a0a0')
    return im


def tin_side():
    im = new(base='#2c4a6a', seed=2812, spread=3)
    for x in range(16):
        dot(im, x, 0, '#c8b070'); dot(im, x, 15, '#9a8a60')
        if x % 4 == 1:
            dot(im, x, 7, '#c85a5a'); dot(im, x, 8, '#e8a0a0')
    return im


def tin_in():
    # Inside: a pincushion, buttons loose in the bottom, two needles and a card of thread.
    im = new(base='#a8a294', seed=2813, spread=3)
    fill(im, 2, 2, 6, 6, '#b84a4a'); dot(im, 3, 3, '#d8d8d8'); dot(im, 4, 4, '#d8d8d8')
    rnd = random.Random(2814)
    for c in ('#e8e0c8', '#3a3a3a', '#8a5a3a', '#c8c0b0', '#5a6a8a', '#e8e0c8'):
        x, y = rnd.randrange(7, 14), rnd.randrange(2, 14)
        dot(im, x, y, c); dot(im, x + 1, y, shade(c, -30))
    for i in range(9):
        dot(im, 2 + i, 12 - i // 3, '#d0d0d8')
    fill(im, 2, 9, 5, 14, '#e8e4d8'); fill(im, 2, 10, 5, 13, '#7a3a5a')
    return im


def wool():
    # Hand knitting: rows of little V stitches, a little uneven.
    im = new(base='#8a7a9a', seed=2815, spread=4)
    for y in range(16):
        for x in range(16):
            if (x + (y // 2) % 2) % 2 == 0 and y % 2 == 0:
                dot(im, x, y, '#6a5a7a')
            elif y % 2 == 1 and x % 2 == 1:
                dot(im, x, y, '#a898b8')
    return im


def needle():
    im = new(base='#c8c8d0', seed=2816, spread=2)
    for y in range(16):
        dot(im, 0, y, '#8a8a92')
    return im


def envelope():
    # The post nobody opened: a brown envelope, its flap, a stamp and the address in a hand they know.
    im = new(base='#c8b088', seed=2817, spread=3)
    for i in range(8):
        dot(im, i, i // 2 + 1, '#a89068'); dot(im, 15 - i, i // 2 + 1, '#a89068')
    fill(im, 11, 9, 14, 13, '#c84a4a'); frame(im, 11, 9, 14, 13, '#e8e0c8')
    for x in range(3, 10):
        dot(im, x, 10, '#4a4a5a')
        if x < 8:
            dot(im, x, 12, '#4a4a5a')
    return im


def ticket():
    im = new(base='#e8dcb8', seed=2818, spread=2)
    fill(im, 0, 0, 16, 3, '#c86a3a')
    for x in range(2, 14, 2):
        dot(im, x, 6, '#3a3a4a'); dot(im, x, 9, '#3a3a4a')
    for y in range(16):
        if y % 2 == 0:
            dot(im, 12, y, '#a89a80')
    return im


def knit_folded():
    im = wool()
    for x in range(16):
        dot(im, x, 7, '#5a4a6a'); dot(im, x, 8, '#9888a8')
    for y in range(16):
        dot(im, 3, y, '#5a4a6a')
    return im


def textures():
    out = {f'block/sp_{k}.png': v for k, v in materials().items()}
    t = {
        'wood': grain('#7a5030', 2800), 'wood_dark': grain('#3e2818', 2801), 'book': book_cover(), 'pages': pages(),
        'shelf': shelf_front(), 'card': cardboard(0), 'card_tape': cardboard(1), 'box_in': box_inside(), 'box_full': box_full(),
        'glass': glass(), 'steam': steam(), 'copper': copper(), 'lagging': lagging(), 'photo': photo(), 'sheet': sheet(),
        'coat': coat(), 'fascia_0': fascia(0), 'fascia_1': fascia(1), 'fascia_2': fascia(2), 'flag': stop_flag(),
        'pole': new(base='#6a6a6c', seed=2802, spread=2), 'rota': rota(), 'cloth': cloth(0), 'cloth_rumpled': cloth(1), 'plate': plate(),
        'bakelite': bakelite(), 'dial': dial(), 'cream': new(base='#e8e2d0', seed=2803, spread=2), 'frame': new(base='#3a2c22', seed=2804, spread=2),
        'tin_lid': tin_lid(), 'tin_side': tin_side(), 'tin_in': tin_in(), 'wool': wool(), 'needle': needle(), 'envelope': envelope(),
        'ticket': ticket(), 'knit_folded': knit_folded(),
    }
    out.update({f'block/second/{k}.png': v for k, v in t.items()})
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


def model(elements, textures, particle):
    tex = {k: (v if ':' in v else P + v) for k, v in textures.items()}
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': {**tex, 'particle': tex.get(particle, P + particle)}, 'elements': elements}


def fitting_models():
    m = {}
    m['book_0'] = model([element([4, 0, 3], [12, 1.5, 13], {'up': 'b', 'down': 'b', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'w'}, uv={'up': [0, 0, 16, 16], 'west': [0, 7, 16, 9]})],
                        {'b': 'book', 'p': 'pages', 'w': 'wood_dark'}, 'book')
    # The keepsake at the end: the same book lying on the middle of the bus-stop bench, drawn with the bench's own seat under it.
    m['book_1'] = model([element([0, 0, 0], [16, 8, 16], {'all': 's'}),
                         element([4, 8, 3], [12, 9.5, 13], {'up': 'b', 'down': 'b', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'w'}, uv={'up': [0, 0, 16, 16], 'west': [0, 7, 16, 9]})],
                        {'b': 'book', 'p': 'pages', 'w': 'wood_dark', 's': 'minecraft:block/spruce_planks'}, 'book')
    m['shelf_0'] = model([element([0, 0, 2], [16, 16, 16], {'north': 's', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}, uv={'north': [0, 0, 16, 16]})],
                         {'s': 'shelf', 'w': 'wood'}, 'shelf')
    m['box_0'] = model([element([1, 0, 1], [15, 12, 15], {'all': 'c', 'up': 't'})], {'c': 'card', 't': 'card_tape'}, 'card')
    flaps = [element([1, 12, 1], [15, 12.5, 4], {'all': 'c'}, rot={'origin': [8, 12, 1], 'axis': 'x', 'angle': -45}),
             element([1, 12, 12], [15, 12.5, 15], {'all': 'c'}, rot={'origin': [8, 12, 15], 'axis': 'x', 'angle': 45})]
    m['box_1'] = model([element([1, 0, 1], [15, 12, 15], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c', 'up': 'f'})] + flaps, {'c': 'card', 'f': 'box_full'}, 'card')
    m['box_2'] = model([element([1, 0, 1], [15, 12, 15], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'down': 'c', 'up': 'i'})] + flaps, {'c': 'card', 'i': 'box_in'}, 'card')
    m['box_3'] = model([element([0, 0, 1], [16, 1.5, 15], {'all': 'c', 'up': 't'})], {'c': 'card', 't': 'card_tape'}, 'card')
    face = {'north': 'g', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'}
    m['glass_0'] = model([element([3, 1, 15], [13, 15, 16], face, uv={'north': [3, 1, 13, 15]})], {'g': 'glass', 'w': 'wood_dark'}, 'glass')
    m['glass_1'] = model([element([3, 1, 15], [13, 15, 16], {**face, 'north': 'w'})], {'w': 'wood_dark'}, 'wood_dark')
    for stage, g in ((0, 'glass'), (1, 'steam')):
        m[f'mirror_{stage}'] = model([element([2, 2, 12], [14, 14, 16], {'north': 'g', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}, uv={'north': [2, 2, 14, 14]})],
                                     {'g': g, 'c': 'cream'}, g)
    m['tank_0'] = model([element([2, 0, 2], [14, 16, 14], {'all': 'l', 'up': 'c', 'down': 'c'}), element([7, 2, 0], [9, 4, 2], {'all': 'c'})], {'l': 'lagging', 'c': 'copper'}, 'lagging')
    m['tank_1'] = model([element([2, 0, 2], [14, 8, 14], {'all': 'l', 'up': 'c'}), element([3, 8, 3], [13, 10, 13], {'all': 'c'}), element([7.5, 10, 7.5], [8.5, 16, 8.5], {'all': 'c'})],
                        {'l': 'lagging', 'c': 'copper'}, 'lagging')
    m['photo_0'] = model([element([4, 0, 7], [12, 9, 8], {'north': 'i', 'south': 'f', 'east': 'f', 'west': 'f', 'up': 'f', 'down': 'f'}, uv={'north': [0, 0, 16, 16]}),
                          element([7.5, 0, 8], [8.5, 5, 11], {'all': 'f'})], {'i': 'photo', 'f': 'frame'}, 'photo')
    m['photo_1'] = model([element([4, 0, 3], [12, 1, 12], {'up': 'f', 'down': 'i', 'north': 'f', 'south': 'f', 'east': 'f', 'west': 'f'})], {'i': 'photo', 'f': 'frame'}, 'frame')
    m['sheet_0'] = model([element([3, 0, 2], [13, 0.25, 14], {'up': 's', 'down': 's'}, uv={'up': [0, 0, 16, 16]})], {'s': 'sheet'}, 'sheet')
    m['coat_0'] = model([element([2, 0, 3], [14, 2, 13], {'all': 'c'}), element([3, 2, 4], [13, 3, 12], {'all': 'c'})], {'c': 'coat'}, 'coat')
    for i in range(16):
        m[f'routine_{i}'] = model([element([2, 0, 2], [14, 0.25, 14], {'up': 'i'}, uv={'up': [0, 0, 16, 16]})], {'i': NS + f'item/read_routine_{i}'}, 'i')
    for i in range(3):
        m[f'sign_{i}'] = model([element([0, 0, 14], [16, 16, 16], {'north': 'f', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g', 'down': 'g'}, uv={'north': [0, 0, 16, 16]})],
                               {'f': f'fascia_{i}', 'g': 'pole'}, f'fascia_{i}')
    m['stop_0'] = model([element([7, 0, 7], [9, 16, 9], {'all': 'p'}), element([3, 8, 8], [13, 16, 9], {'north': 'f', 'south': 'f', 'east': 'p', 'west': 'p', 'up': 'p', 'down': 'p'}, uv={'north': [0, 0, 16, 16], 'south': [0, 0, 16, 16]})],
                        {'p': 'pole', 'f': 'flag'}, 'flag')
    m['rota_0'] = model([element([3, 2, 15.5], [13, 14, 16], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r', 'up': 'r', 'down': 'r'}, uv={'north': [0, 0, 16, 16]})], {'r': 'rota'}, 'rota')
    m['cloth_0'] = model([element([0, 0, 0], [16, 0.5, 16], {'up': 'c', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}, rot={'origin': [8, 0, 8], 'axis': 'y', 'angle': 22.5})], {'c': 'cloth_rumpled'}, 'cloth_rumpled')
    m['cloth_1'] = model([element([0, 0, 0], [16, 0.5, 16], {'up': 'c', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'})], {'c': 'cloth'}, 'cloth')
    m['cloth_2'] = model([element([0, 0, 0], [16, 0.5, 16], {'up': 'c', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'})] +
                         [element([x, 0.5, 4], [x + 6, 1, 10], {'up': 'p', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w'}, uv={'up': [0, 0, 16, 16]}) for x in (1, 9)],
                         {'c': 'cloth', 'p': 'plate', 'w': 'cream'}, 'cloth')
    base = element([3, 0, 4], [13, 3, 12], {'all': 'k', 'up': 'd'}, uv={'up': [3, 4, 13, 12]})
    m['phone_0'] = model([base, element([2.5, 3, 6], [13.5, 5, 8], {'all': 'k'})], {'k': 'bakelite', 'd': 'dial'}, 'bakelite')
    m['phone_1'] = model([base, element([12, 0, 12], [15, 2, 15.5], {'all': 'k'}), element([3, 0, 13], [11, 2, 15], {'all': 'k'})], {'k': 'bakelite', 'd': 'dial'}, 'bakelite')
    # 0.4.370: the cradle empty while the resident has the receiver at their ear; the receiver itself, for their hand.
    m['phone_2'] = model([base, element([3, 3, 6], [4, 4, 8], {'all': 'k'}), element([12, 3, 6], [13, 4, 8], {'all': 'k'})], {'k': 'bakelite', 'd': 'dial'}, 'bakelite')
    m['receiver'] = model([element([2.5, 0, 7], [13.5, 2, 9], {'all': 'k'}), element([2.5, 2, 7], [4.5, 3, 9], {'all': 'k'}), element([11.5, 2, 7], [13.5, 3, 9], {'all': 'k'})], {'k': 'bakelite'}, 'bakelite')
    # Iris's tin: shut; open with the sleeve on its needles bunched in it and the lid leant against the back; open with only the bits.
    can = element([4, 0, 4], [12, 4, 12], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's', 'up': 'i'}, uv={'up': [4, 4, 12, 12]})
    m['tin_0'] = model([element([4, 0, 4], [12, 4, 12], {'all': 's'}), element([3.5, 4, 3.5], [12.5, 5, 12.5], {'north': 's', 'south': 's', 'east': 's', 'west': 's', 'down': 's', 'up': 'l'}, uv={'up': [0, 0, 16, 16]})],
                       {'s': 'tin_side', 'l': 'tin_lid', 'i': 'tin_in'}, 'tin_lid')
    lid = element([3.5, 0, 12.5], [12.5, 9, 13.5], {'north': 'l', 'south': 's', 'east': 's', 'west': 's', 'up': 's', 'down': 's'}, uv={'north': [0, 0, 16, 16]}, rot={'origin': [8, 0, 13], 'axis': 'x', 'angle': 22.5})
    m['tin_1'] = model([can, lid, element([5, 3, 5], [11, 5, 10], {'all': 'w'}), element([6, 4.5, 4], [6.5, 5, 12], {'all': 'n'}), element([9, 4.5, 4], [9.5, 5, 12], {'all': 'n'})],
                       {'s': 'tin_side', 'l': 'tin_lid', 'i': 'tin_in', 'w': 'wool', 'n': 'needle'}, 'tin_lid')
    m['tin_2'] = model([can, lid], {'s': 'tin_side', 'l': 'tin_lid', 'i': 'tin_in'}, 'tin_lid')
    m['ticket_0'] = model([element([3, 0, 4], [13, 0.25, 12], {'up': 'e', 'down': 'e', 'north': 'e', 'south': 'e', 'east': 'e', 'west': 'e'}, uv={'up': [0, 0, 16, 16]}),
                           element([9, 0.25, 5], [14, 0.5, 8], {'up': 't', 'down': 't', 'north': 't', 'south': 't', 'east': 't', 'west': 't'}, uv={'up': [0, 0, 16, 7]}, rot={'origin': [11, 0, 6], 'axis': 'y', 'angle': -22.5})],
                          {'e': 'envelope', 't': 'ticket'}, 'envelope')
    # The sleeve along the armchair's right arm (arm tops at 12 in the cell below, so -4 here), its two needles through the last row.
    sleeve = [element([13, -4, 2], [16, -3.25, 13], {'all': 'w'}, uv={'up': [0, 0, 6, 16]}), element([12.5, -3.25, 12], [16.5, -3, 12.5], {'all': 'n'}), element([12.5, -3.25, 13], [16.5, -3, 13.5], {'all': 'n'})]
    m['sleeve_0'] = model(sleeve, {'w': 'wool', 'n': 'needle'}, 'wool')
    m['sleeve_1'] = model([element([3, -8, 3], [13, -6, 12], {'up': 'f', 'north': 'w', 'south': 'w', 'east': 'w', 'west': 'w', 'down': 'w'}, uv={'up': [0, 0, 16, 16]})], {'f': 'knit_folded', 'w': 'wool'}, 'knit_folded')
    m['sleeve_2'] = model([element([3, 0, 2], [13, 0.75, 13], {'all': 'w'}, uv={'up': [0, 0, 16, 16]}), element([2.5, 0.75, 12], [13.5, 1, 12.5], {'all': 'n'}), element([2.5, 0.75, 13], [13.5, 1, 13.5], {'all': 'n'})],
                          {'w': 'wool', 'n': 'needle'}, 'wool')
    return m


KINDS = ['book', 'shelf', 'box', 'glass', 'mirror', 'tank', 'photo', 'sheet', 'coat', 'routine', 'sign', 'stop', 'rota', 'cloth', 'phone', 'tin', 'ticket', 'sleeve']
STAGES = {'book': 2, 'box': 4, 'glass': 2, 'mirror': 2, 'tank': 2, 'photo': 2, 'routine': 16, 'sign': 3, 'cloth': 3, 'phone': 3, 'tin': 3, 'sleeve': 3}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
MAX_STAGE = 16
DISPLAY = {
    'gui': {'rotation': [30, 225, 0], 'translation': [0, 2, 0], 'scale': [0.8, 0.8, 0.8]},
    'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.4, 0.4, 0.4]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.6, 0.6, 0.6]},
    'thirdperson_righthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.45, 0.45, 0.45]},
    'thirdperson_lefthand': {'rotation': [75, 45, 0], 'translation': [0, 2.5, 0], 'scale': [0.45, 0.45, 0.45]},
    'firstperson_righthand': {'rotation': [20, 45, 0], 'translation': [0, 2, 0], 'scale': [0.7, 0.7, 0.7]},
    'firstperson_lefthand': {'rotation': [20, 225, 0], 'translation': [0, 2, 0], 'scale': [0.7, 0.7, 0.7]},
}
# The receiver in the resident's hand: solved for the phone gesture (the arm up beside the head, xRot -2.85, yRot -0.1, zRot 0.1),
# so it runs from the hand at the temple down the side of the face to the mouth, its cups toward the head.
RECEIVER_DISPLAY = dict(DISPLAY, thirdperson_righthand={'rotation': [146.7, 8.1, 92.8], 'translation': [-4.19, -3.64, 4.32], 'scale': [0.55, 0.55, 0.55]},
                        thirdperson_lefthand={'rotation': [146.7, 8.1, 92.8], 'translation': [-4.19, -3.64, 4.32], 'scale': [0.55, 0.55, 0.55]})
# What a reader carries: the routine item by its number (custom model data 1-16), the coat (17), the drawing (18), the photograph (19);
# 0.4.369, the train ticket (20) and Iris's sleeve (21); 0.4.370, the landline's receiver in the resident's hand (22).
CARRIED = [(f'routine_{i}', f'routine_{i}') for i in range(16)] + [('coat', 'coat_0'), ('sheet', 'sheet_0'), ('photo', 'photo_0'), ('ticket', 'ticket_0'), ('sleeve', 'sleeve_2'), ('receiver', 'receiver')]
NAMES = {'sp_fitting': 'Things in the Flat', 'sp_brick': 'Stock Brick', 'sp_paving': 'Paving Slab', 'sp_road': 'Road', 'sp_kerb': 'Kerb',
         'sp_shopfront': 'Painted Shopfront', 'sp_paper': 'Faded Wallpaper', 'sp_kitchen': 'Kitchen Tiles', 'sp_carpet': 'Worn Carpet',
         'sp_lino': 'Chequered Lino', 'sp_tile': 'Avocado Tile', 'sp_ceiling': 'Artex Ceiling', 'sp_launderette': 'Launderette Floor'}
ITEM_NAMES = {'item.the_oldest_house.sp_carried': 'Something of Theirs'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/sp_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/sp_{name}'}}
        out[f'blockstates/sp_{name}.json'] = {'variants': {'': {'model': NS + f'block/sp_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/second/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(MAX_STAGE):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/second/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/sp_fitting.json'] = {'variants': variants}
    for name, parent in CARRIED:
        out[f'models/item/sp_carried_{name}.json'] = {'parent': NS + f'block/second/{parent}', 'display': RECEIVER_DISPLAY if name == 'receiver' else DISPLAY}
    out['models/item/sp_carried.json'] = {'parent': NS + 'item/sp_carried_coat',
                                          'overrides': [{'predicate': {'custom_model_data': i + 1}, 'model': NS + f'item/sp_carried_{name}'} for i, (name, _) in enumerate(CARRIED)]}
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
            if tex.startswith('minecraft:'):
                continue
            if '/second/' in tex:
                assert f'block/second/{tex.split("second/")[1]}.png' in images, (key, tex)
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
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {**{f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}, **ITEM_NAMES}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" the flat: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
