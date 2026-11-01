"""Work print (0.4.203): the closed rental shop, the rooms behind it, the things a reader carries, the film, and its monster.

Draws, in native Minecraft pixels from fixed seeds: seven materials (the shop's worn carpet tiles, its painted wall panels,
the suspended ceiling, the back room's concrete, the edit bay's pegboard, the booth's acoustic foam and the set's plywood);
every fitting of the work_print_fitting block, its models facing north and turned by its facing (the tape shelves, the
counter and its till, the rental card, the ledger, the returns bin under its slot, the phone, the mannequin in the
costume, four posters, a section sign, the ceiling tubes, the CRT dark, snowing or playing, the TV cart's two decks, the
box of blanks, folding chairs, stacked tapes, the rack of film cans, the splicer, the light table, the continuity
photographs, the script, the call sheet, the flatbed and its viewer, the trim bin, the booth's desk and microphone, the
set's flats, its bulb, its chalk mark, the House's camera on its tripod, and the slate); the seven carried things as items;
the costume's sheet (64 by 64, the player net, the arms two pixels longer); and the film itself, Hallstand, as an atlas of
128 by 96 shots, the figure at three distances, the writing hand and the handprints. The CRT's snow is an animation with
one frame, once in a long cycle, that is not snow. Writes the models and block states with them. --check verifies
everything already written.

Palette: the shop's navy carpet #1f2a44 with confetti flecks, cream wall panels #d9cfb4, ceiling tile #e8e4da, tube light
#f2f6ea, VHS black #161616, sacking #6b5a44, the film's washed tape colours (warm greys, a sodium bulb #f0c070).
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
P = NS + 'block/work_print/'
CARPET, CREAM, TILE, TUBE, VHS, SACK, BULB = '#1f2a44', '#d9cfb4', '#e8e4da', '#f2f6ea', '#161616', '#6b5a44', '#f0c070'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def new(size=(16, 16), base=None, seed=0, spread=5):
    im = Image.new('RGBA', size, (0, 0, 0, 0))
    if base:
        rnd = random.Random(seed); px = im.load()
        for y in range(size[1]):
            for x in range(size[0]):
                px[x, y] = shade(base, rnd.randint(-spread, spread))
    return im


def fill(im, x0, y0, x1, y1, colour, seed=0, spread=0, a=255):
    rnd = random.Random(seed); px = im.load()
    for y in range(max(0, y0), min(im.size[1], y1)):
        for x in range(max(0, x0), min(im.size[0], x1)):
            px[x, y] = shade(colour, rnd.randint(-spread, spread) if spread else 0, a)


def line(im, x0, y0, x1, y1, colour, a=255):
    px = im.load(); n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        x, y = round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)
        if 0 <= x < im.size[0] and 0 <= y < im.size[1]:
            px[x, y] = shade(colour, 0, a)


def frame_rect(im, x0, y0, x1, y1, colour):
    line(im, x0, y0, x1 - 1, y0, colour); line(im, x0, y1 - 1, x1 - 1, y1 - 1, colour)
    line(im, x0, y0, x0, y1 - 1, colour); line(im, x1 - 1, y0, x1 - 1, y1 - 1, colour)


# ---------------------------------------------------------------------------------------------------- materials
def carpet():
    """Commercial carpet tiles, navy with confetti flecks, a seam every eight pixels, worn paler down a walked line."""
    im = new(base=CARPET, seed=11, spread=3); px = im.load(); rnd = random.Random(12)
    for _ in range(14):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        px[x, y] = shade(rnd.choice(('#b0405a', '#3d8a8a', '#c8a040', '#7a62a8')), rnd.randint(-10, 0))
    for i in range(16):
        px[i, 7] = shade(CARPET, -7); px[7, i] = shade(CARPET, -7)
    for y in range(16):
        for x in (10, 11, 12):
            if rnd.random() < .5:
                px[x, y] = shade(px[x, y], 6)
    return im


def wall():
    """The shop's wall panels, cream gone yellow, a groove every eight pixels and scuffs at shelf height."""
    im = new(base=CREAM, seed=21, spread=3); px = im.load(); rnd = random.Random(22)
    for y in range(16):
        px[0, y] = shade(CREAM, -18); px[8, y] = shade(CREAM, -18); px[1, y] = shade(CREAM, 6); px[9, y] = shade(CREAM, 6)
    for _ in range(4):
        x, y = rnd.randint(2, 15), rnd.randint(9, 13)
        px[x, y] = shade(CREAM, -26)
    return im


def ceiling():
    """A suspended ceiling tile in its grid: pinholed, a water ring in one corner."""
    im = new(base=TILE, seed=31, spread=2); px = im.load(); rnd = random.Random(32)
    for i in range(16):
        px[i, 0] = shade('#b8b4ac', 0); px[0, i] = shade('#b8b4ac', 0)
    for _ in range(26):
        px[rnd.randint(2, 15), rnd.randint(2, 15)] = shade(TILE, -22)
    for a in range(0, 360, 20):
        x, y = 12 + round(2.5 * math.cos(math.radians(a))), 12 + round(2.5 * math.sin(math.radians(a)))
        px[x, y] = shade('#c9b98c', 0)
    return im


def concrete():
    im = new(base='#8a8783', seed=41, spread=6); px = im.load(); rnd = random.Random(42)
    for _ in range(10):
        px[rnd.randint(0, 15), rnd.randint(0, 15)] = shade('#8a8783', rnd.choice((-20, 14)))
    line(im, 2, 13, 9, 11, '#6f6c68')
    return im


def pegboard():
    """Brown hardboard with a hole every four pixels, the edit bay's tools long gone from it."""
    im = new(base='#8a6a48', seed=51, spread=3); px = im.load()
    for y in range(2, 16, 4):
        for x in range(2, 16, 4):
            px[x, y] = shade('#2a2118', 0); px[x + 1, y] = shade('#5a4430', 0)
    return im


def foam():
    """Charcoal acoustic foam in wedges, alternating direction every eight pixels."""
    im = new(base='#2f3033', seed=61, spread=2); px = im.load()
    for y in range(16):
        for x in range(16):
            horizontal = (x // 8 + y // 8) % 2 == 0
            k = (8 - (y % 4) * 5) if horizontal else (8 - (x % 4) * 5)
            px[x, y] = shade(px[x, y], k)
    return im


def plywood():
    """The set's floor: plywood sheets, pale, the grain in long loops, a gaffer-tape square near one corner."""
    im = new(base='#b08c5e', seed=71, spread=3); px = im.load(); rnd = random.Random(72)
    for y in range(16):
        for x in range(16):
            if (int(y + 2.5 * math.sin(x * .45 + y * .1)) % 5) == 0:
                px[x, y] = shade('#b08c5e', -14 + rnd.randint(-2, 2))
    for i in range(16):
        px[i, 15] = shade('#7a5e3c', 0)
    return im


def materials():
    return {'carpet': carpet(), 'wall': wall(), 'ceiling': ceiling(), 'concrete': concrete(), 'pegboard': pegboard(), 'foam': foam(), 'plywood': plywood()}


# ---------------------------------------------------------------------------------------------------- fittings
SPINES = ['#1d1d1d', '#2a2a2a', '#c8c0a8', '#3a5a8a', '#8a2a2a', '#d0b040', '#2a6a4a', '#e0e0d8', '#5a3a6a', '#8a8a8a']


# 0.4.220 (owner: "make the shelves more discernably dvds/vhses"): the rental wall's tapes face out, as a shop shows them: each
# box a cover with its title band, its art and the shop's orange sticker, on a shelf with its strip of price tags. One stage in
# three is a shelf of DVD cases, thinner, with their blue bands.
COVERS = [('#151515', '#c0302a'), ('#101824', '#3a7ad0'), ('#1e1c10', '#e0c040'), ('#1a0c0c', '#e06020'), ('#0c1810', '#40a060'),
          ('#ece6d6', '#222222'), ('#24102a', '#a050c0'), ('#0a0a0a', '#e0e0d8'), ('#2a1a08', '#d08a30')]


def shelf_front(stage):
    """A rental shelf's face: two rows of boxes face out over strips of price tags; VHS covers, or (stage 2) DVD cases."""
    im = new(base='#3e3024', seed=81, spread=2); px = im.load(); rnd = random.Random(82 + stage)
    dvd = stage == 2
    for y0 in (1, 9):
        x, width, step = 1, (3 if dvd else 4), (4 if dvd else 5)
        while x + width <= 15:
            if rnd.random() < .08:
                x += step; continue
            base, art = rnd.choice(COVERS)
            for xx in range(x, x + width):
                for y in range(y0, y0 + 6):
                    px[xx, y] = shade(base, rnd.randint(-3, 3))
            # 0.4.220 (owner: "more discernably dvds/vhses"): the art is one of six covers a shop wall is made of: a face
            # with its eyes and mouth, a figure dark against a lit ground, a moon over a black horizon, a red drip from the
            # title, a house with one lit window, a knife; never a lone line under the title band, which read as a letter T.
            motif = rnd.randrange(6); mid = x + width // 2
            if motif == 0:
                px[x, y0 + 2] = shade(art, 20); px[x + width - 1, y0 + 2] = shade(art, 20)
                for xx in range(x + 1, x + width - 1):
                    px[xx, y0 + 4] = shade(art, -10)
            elif motif == 1:
                for xx in range(x, x + width):
                    for y in range(y0 + 1, y0 + 6):
                        px[xx, y] = shade(art, -20 + 4 * (y - y0))
                px[mid - (1 if width > 3 else 0), y0 + 2] = shade(base, 0)
                for y in range(y0 + 3, y0 + 6):
                    px[mid - (1 if width > 3 else 0), y] = shade(base, -4)
            elif motif == 2:
                px[x + width - 1, y0 + 1] = shade('#e8e2c8', 0); px[x + width - 2, y0 + 1] = shade('#e8e2c8', -8)
                for xx in range(x, x + width):
                    px[xx, y0 + 4] = shade(art, -30); px[xx, y0 + 5] = shade(art, -40)
            elif motif == 3:
                for xx in range(x, x + width):
                    px[xx, y0 + 1] = shade('#a01818', 0)
                for xx in range(x, x + width, 2):
                    px[xx, y0 + 2] = shade('#a01818', -6)
                px[x + 1, y0 + 3] = shade('#a01818', -14)
            elif motif == 4:
                for xx in range(x, x + width):
                    px[xx, y0 + 4] = shade(art, -24); px[xx, y0 + 5] = shade(art, -24)
                px[mid, y0 + 3] = shade(art, -24); px[mid - 1, y0 + 3] = shade(art, -24) if width > 3 else px[mid - 1, y0 + 3]
                px[mid, y0 + 4] = shade('#f0c040', 0)
            else:
                for y in range(y0 + 2, y0 + 5):
                    px[mid, y] = shade('#c8ccd0', 6 - 3 * (y - y0))
                px[mid, y0 + 5] = shade('#3a2a1a', 0)
            # The title band: a pale strip with the title's letters in it on VHS; the blue band DVDs carry.
            for xx in range(x, x + width):
                px[xx, y0] = shade('#2a50a8', 0) if dvd else shade(art, 40 if (xx - x) % 2 else 10)
            if not dvd:
                px[x + width - 1, y0 + 5] = shade('#f09030', 0)
            x += step
        for xx in range(16):
            px[xx, y0 + 6] = shade('#e6dfcc', 0)
        for xx in range(2, 16, 5):
            px[xx, y0 + 6] = shade('#c03028', 0); px[xx + 1, y0 + 6] = shade('#c03028', 0)
    for x in range(16):
        px[x, 0] = shade('#4e3e2e', 4); px[x, 8] = shade('#4e3e2e', 4); px[x, 15] = shade('#2a2018', 0)
    return im


def wood(base, seam, seed):
    im = new(base=base, seed=seed, spread=3); px = im.load()
    for y in range(3, 16, 5):
        for x in range(16):
            px[x, y] = shade(seam, 0)
    return im


def counter_front():
    """The counter's front: panelled in the shop's laminate, a kick strip at the floor. 0.4.220: the sticker every panel
    carried read as a row of drawer labels along the whole counter, so the panels are plain."""
    im = wood('#6a4e36', '#4a3626', 91); px = im.load()
    for x in (0, 8):
        for y in range(16):
            px[x, y] = shade('#3e2c1e', 0)
    fill(im, 0, 14, 16, 16, '#232323')
    return im


def counter_back():
    """The clerk's side of the counter: two open shelves under the top, returned tapes stacked on the upper one waiting to
    be put back, the bag of slips and a roll of receipt paper on the lower, the kick strip at the floor."""
    im = wood('#5e4430', '#4a3626', 92); px = im.load()
    fill(im, 1, 2, 15, 7, '#1c140e'); fill(im, 1, 8, 15, 13, '#1c140e')
    for x in range(16):
        px[x, 7] = shade('#4a3626', 6); px[x, 13] = shade('#4a3626', 6)
    rnd = random.Random(93)
    for x0 in (2, 5, 8, 11):
        h = rnd.randint(2, 4)
        for x in range(x0, x0 + 2):
            for y in range(7 - h, 7):
                px[x, y] = shade('#141414', rnd.randint(-2, 2))
            px[x, 7 - h] = shade('#e6dfcc', 0)
    fill(im, 2, 10, 6, 13, '#8a7a5a'); px[3, 10] = shade('#c03028', 0)
    fill(im, 9, 11, 12, 13, '#efe9da'); px[10, 11] = shade('#c8c0ae', 0)
    fill(im, 0, 14, 16, 16, '#232323')
    return im


def counter_side():
    """The counter's ends: the laminate plain, a seam down the middle, the kick strip."""
    im = wood('#6a4e36', '#4a3626', 94); px = im.load()
    for y in range(16):
        px[0, y] = shade('#3e2c1e', 0); px[15, y] = shade('#3e2c1e', 0)
    fill(im, 0, 14, 16, 16, '#232323')
    return im


def counter_top():
    im = new(base='#c9c1ae', seed=95, spread=2); px = im.load()
    for x in range(16):
        px[x, 15] = shade('#8a8270', 0)
    return im


def till(part):
    """0.4.220 (owner: "improve texture and properly face the cash register"): the shop's register, its keys toward the clerk
    behind the counter and its little display turned to the customer. part: 0 keys, 1 body, 2 display, 3 drawer, 4 open drawer."""
    if part == 0:
        im = new(base='#cfc6b2', seed=101, spread=2)
        fill(im, 1, 1, 15, 4, '#2a2a2a'); fill(im, 2, 2, 10, 3, '#3a2a10')
        for x in range(3, 9):
            im.load()[x, 2] = shade('#e8a040', 0) if x % 2 else shade('#3a2a10', 0)
        keys = ('#8a8478', '#8a8478', '#8a8478', '#c84a3a', '#4a7aa8')
        for row, y in enumerate(range(6, 15, 2)):
            for col, x in enumerate(range(2, 14, 3)):
                fill(im, x, y, x + 2, y + 1, keys[4] if col == 3 and row < 2 else keys[3] if col == 3 else keys[0])
        fill(im, 13, 6, 15, 15, '#e8e2d2')
        return im
    if part == 1:
        im = new(base='#bfb6a2', seed=102, spread=2)
        for x in range(16):
            im.load()[x, 15] = shade('#8a8270', 0)
        return im
    if part == 2:
        im = new(base='#2a2a2a', seed=103, spread=1)
        fill(im, 2, 4, 14, 12, '#0c1a10')
        digits = ['111', '101', '101', '101', '111']
        for x0 in (3, 7, 11):
            for j, row in enumerate(digits):
                for i, c in enumerate(row):
                    if c == '1' and x0 + i < 14:
                        im.load()[x0 + i, 5 + j] = shade('#40e070', 0)
        return im
    if part == 3:
        im = new(base='#b8af9a', seed=104, spread=2)
        fill(im, 1, 1, 15, 15, '#c4bba6'); fill(im, 6, 6, 10, 8, '#6a6458')
        return im
    im = new(base='#2a2a2a', seed=105, spread=2)
    for i, x0 in enumerate(range(1, 15, 3)):
        fill(im, x0, 1, x0 + 2, 8, ('#7a9a6a', '#8aa878', '#7a9a6a', '#a8b890', '#7a9a6a')[i % 5])
    for i, x0 in enumerate(range(1, 15, 3)):
        fill(im, x0, 10, x0 + 2, 14, '#a8a8a0' if i % 2 else '#c8a050')
    return im


def ledger(part):
    """The returns ledger open on the counter: two ruled pages, a column of dates; the cloth spine at the sides."""
    if part == 0:
        im = new(base='#ece4cc', seed=111, spread=2); px = im.load()
        for y in range(2, 15, 2):
            for x in range(1, 15):
                if x != 7 and x != 8:
                    px[x, y] = shade('#b8c4d4', 0)
        for x in (7, 8):
            for y in range(16):
                px[x, y] = shade('#c0b498', 0)
        for y in range(2, 15, 2):
            if y % 4 == 2:
                line(im, 9, y - 1, 13, y - 1, '#3a3a5a')
        return im
    return new(base='#5a2a2a', seed=112, spread=3)


def card():
    """The rental card: buff card stock, the shop's printed boxes, a name written in on the borrower's line."""
    im = new(base='#e6d7a8', seed=121, spread=2); px = im.load()
    frame_rect(im, 0, 0, 16, 16, '#a89060')
    for y in (4, 8, 12):
        line(im, 1, y, 14, y, '#b89c6a')
    line(im, 2, 6, 11, 6, '#2a3a8a'); px[12, 5] = shade('#2a3a8a', 0)
    fill(im, 2, 1, 9, 3, '#3a3a3a')
    return im


def returns(part):
    """The returns bin: a grey steel box under the slot. Inside it, nothing, the tape, or the stained one."""
    if part == 'front':
        im = new(base='#6f7478', seed=131, spread=2)
        fill(im, 2, 3, 14, 6, '#e8e0c8'); line(im, 3, 4, 12, 4, '#8a2a2a')
        for y in (10, 13):
            line(im, 1, y, 14, y, '#5a5e62')
        return im
    if part == 'side':
        return new(base='#64686c', seed=132, spread=2)
    im = new(base='#2a2c2e', seed=133, spread=2); px = im.load()
    frame_rect(im, 0, 0, 16, 16, '#7a7e82')
    if part in (1, 2):
        fill(im, 3, 5, 13, 11, VHS); fill(im, 5, 7, 7, 9, '#4a4a4a'); fill(im, 9, 7, 11, 9, '#4a4a4a')
        if part == 2:
            for a in range(0, 360, 15):
                x, y = 8 + round(3 * math.cos(math.radians(a))), 8 + round(2.4 * math.sin(math.radians(a)))
                px[x, y] = shade('#8a8270', 0)
    return im


def slot():
    im = new(base='#a88a3a', seed=141, spread=4)
    fill(im, 2, 6, 14, 10, '#1a1610'); line(im, 2, 5, 13, 5, '#d8b860')
    return im


def phone(part):
    """A beige desk phone; ringing, its line button lit."""
    if part in ('top', 'top_lit'):
        im = new(base='#d4c8a8', seed=151, spread=2); px = im.load()
        for y in range(5, 14, 3):
            for x in range(4, 13, 3):
                px[x, y] = shade('#7a7060', 0)
        fill(im, 1, 13, 4, 15, '#e04030' if part == 'top_lit' else '#6a3a30')
        return im
    return new(base='#c8bc9c', seed=152, spread=2)


def sack(seed=161):
    """Sacking: a coarse weave, warp and weft, darker where the threads cross."""
    im = new(base=SACK, seed=seed, spread=4); px = im.load()
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                px[x, y] = shade(px[x, y], -9)
            if x % 4 == 0 or y % 4 == 0:
                px[x, y] = shade(px[x, y], 5)
    return im


def mannequin():
    im = new(base='#d8d0c4', seed=171, spread=2)
    line(im, 8, 0, 8, 15, '#c4bcb0')
    return im


def poster(stage):
    """Four posters for films that do not exist: a face in shadow, a car's headlights, a house with one lit window, a hand."""
    rnd = random.Random(181 + stage)
    base = ('#2a1a1a', '#10182a', '#1a2a1a', '#2a2210')[stage]
    im = new(base=base, seed=181 + stage, spread=3); px = im.load()
    frame_rect(im, 0, 0, 16, 16, '#e8e0d0')
    if stage == 0:
        for y in range(3, 11):
            for x in range(4, 12):
                if (x - 8) ** 2 / 12 + (y - 7) ** 2 / 18 < 1:
                    px[x, y] = shade('#c8a088' if x < 8 else '#3a2a24', rnd.randint(-6, 6))
    elif stage == 1:
        fill(im, 3, 7, 13, 10, '#2a2a3a'); px[4, 8] = shade('#fff4c0', 0); px[11, 8] = shade('#fff4c0', 0)
        for x in range(1, 15):
            px[x, 10] = shade('#3a3a3a', 0)
    elif stage == 2:
        fill(im, 4, 5, 12, 11, '#0c0c0c'); line(im, 3, 5, 8, 2, '#0c0c0c'); line(im, 8, 2, 12, 5, '#0c0c0c'); px[9, 7] = shade(BULB, 0)
    else:
        for y in range(4, 11):
            for x in range(5, 11):
                if y > 7 or (x - 5) % 2 == 0:
                    px[x, y] = shade('#c8b098', rnd.randint(-8, 8))
    fill(im, 2, 12, 14, 14, '#c8c0b0'); line(im, 3, 12, 10, 12, '#8a2a2a')
    return im


def section():
    """A category sign on its bracket: red, the letters too small to read from the floor."""
    im = new(); fill(im, 0, 4, 16, 12, '#a02a28', 191, 2); px = im.load()
    for x in range(2, 14):
        if x % 3 != 0:
            px[x, 7] = shade('#f0e8d8', 0); px[x, 8] = shade('#f0e8d8', -10)
    return im


def tube(part):
    if part == 'frame':
        return new(base='#d8d8d0', seed=201, spread=2)
    im = new(base=TUBE if part == 'lit' else '#8a8e88', seed=202, spread=1 if part == 'lit' else 4)
    if part == 'dim':
        px = im.load()
        for x in range(0, 3):
            for y in range(16):
                px[x, y] = shade('#4a4a46', 0)
    return im


def crt(part):
    """The set: a wood-effect cabinet, a curved screen. Dark, snowing (an animation, below), or lit grey for the picture."""
    if part == 'side':
        return wood('#4a3424', '#3a281c', 211)
    if part == 'top':
        return new(base='#3a3a3a', seed=212, spread=2)
    im = new(base='#2b2b2b', seed=213, spread=2)
    screen = {'dark': '#101412', 'lit': '#5a6060'}[part]
    fill(im, 3, 5, 13, 13, screen, 214, 2)
    px = im.load()
    for x, y in ((3, 5), (12, 5), (3, 12), (12, 12)):
        px[x, y] = shade('#2b2b2b', 0)
    fill(im, 11, 1, 13, 3, '#8a8a8a'); fill(im, 8, 1, 10, 3, '#6a6a6a')
    px[4, 14] = shade('#40c040' if part == 'lit' else '#304030', 0)
    return im


def snow_frames():
    """The empty channel, twelve frames; the last, shown once in a long cycle, is a well in a clearing."""
    frames = []
    for f in range(12):
        im = crt('dark'); px = im.load(); rnd = random.Random(221 + f)
        for y in range(5, 13):
            for x in range(3, 13):
                v = rnd.randint(40, 220)
                px[x, y] = (v, v, v, 255)
        if f == 11:
            for y in range(5, 13):
                for x in range(3, 13):
                    v = 120 if y < 8 else 70
                    if 9 <= y <= 11 and 6 <= x <= 9:
                        v = 150
                    if y == 9 and 7 <= x <= 8:
                        v = 18
                    px[x, y] = (v, v, v, 255)
        frames.append(im)
    strip = Image.new('RGBA', (16, 16 * len(frames)))
    for i, im in enumerate(frames):
        strip.paste(im, (0, 16 * i))
    return strip


SNOW_META = {'animation': {'frametime': 1, 'interpolate': False, 'frames': list(range(11)) * 9 + [11]}}


def decks(stage):
    """Two VCRs stacked on the cart's top shelf: the first deck's flap and lights, the second's; dubbing, a red light."""
    im = new(base='#2a2a2c', seed=231, spread=2); px = im.load()
    for y0, loaded, light in ((1, stage >= 1, '#40d040'), (9, stage >= 2, '#40d040' if stage < 3 else '#e03020')):
        fill(im, 1, y0, 15, y0 + 6, '#3a3a3e')
        fill(im, 2, y0 + 1, 10, y0 + 3, '#141414' if not loaded else '#202024')
        if loaded:
            line(im, 2, y0 + 2, 9, y0 + 2, '#5a5a60')
        px[12, y0 + 2] = shade(light if loaded else '#2a3a2a', 0)
        fill(im, 11, y0 + 4, 14, y0 + 5, '#5a8a9a' if loaded else '#2a3a40')
    return im


def cart(part):
    if part == 'metal':
        return new(base='#5a5e64', seed=241, spread=3)
    return new(base='#3c3f44', seed=242, spread=2)


def blanks(part):
    """A carton of blank tapes, its flap open on cellophane."""
    if part == 'top':
        im = new(base='#8a6a42', seed=251, spread=3)
        for x0 in (2, 6, 10):
            fill(im, x0, 3, x0 + 3, 13, VHS); fill(im, x0, 3, x0 + 3, 4, '#d8e0e8', a=200)
        return im
    im = new(base='#9a7a50', seed=252, spread=3)
    fill(im, 2, 3, 14, 6, '#e8e0d0'); line(im, 3, 4, 10, 4, '#2a4a8a')
    return im


def chair(part):
    if part == 'seat':
        return new(base='#4a4e56', seed=261, spread=3)
    return new(base='#8a8c90', seed=262, spread=2)


def tapes(part):
    if part == 'top':
        im = new(base='#1c1c1c', seed=271, spread=2); fill(im, 3, 2, 13, 5, '#d8d0c0'); return im
    im = new(base='#1a1a1a', seed=272, spread=2); px = im.load(); rnd = random.Random(273)
    for y in range(0, 16, 3):
        c = rnd.choice(SPINES)
        for x in range(16):
            px[x, y] = shade(c, -10)
    return im


def rack(part):
    """Steel shelving with film cans on edge, their lids catching the light."""
    if part == 'side':
        return new(base='#5a5e62', seed=281, spread=2)
    im = new(base='#2a2c2e', seed=282, spread=2); px = im.load()
    for y0 in (1, 9):
        for x0 in range(1, 15, 5):
            for y in range(y0, y0 + 6):
                for x in range(x0, x0 + 4):
                    if (x - x0 - 1.5) ** 2 + (y - y0 - 2.5) ** 2 < 6:
                        px[x, y] = shade('#9aa0a6', -abs(x - x0 - 1) * 6)
            px[x0 + 1, y0 + 2] = shade('#e0e4e8', 0)
        for x in range(16):
            px[x, y0 + 6] = shade('#6a6e72', 0)
    return im


def bench(part):
    if part == 'side':
        return new(base='#6a5a48', seed=291, spread=3)
    if part == 'front':
        im = new(base='#6a5a48', seed=292, spread=3); fill(im, 2, 3, 14, 6, '#4a3e32'); fill(im, 7, 4, 9, 5, '#a89060'); return im
    return new(base='#7a6a56', seed=293, spread=2)


def splicer_top(stage):
    """The splicer: a steel block and its blade, a roll of tape; a reel beside it once there is something to cut."""
    im = bench('top'); px = im.load()
    fill(im, 4, 6, 12, 10, '#9aa0a6'); line(im, 4, 8, 11, 8, '#3a3e42'); fill(im, 13, 3, 15, 5, '#d8d0b8')
    if stage == 1:
        for a in range(0, 360, 10):
            for r in (2, 3):
                x, y = 3 + round(r * math.cos(math.radians(a))), 12 + round(r * math.sin(math.radians(a)))
                if 0 <= x < 16 and 0 <= y < 16:
                    px[x, y] = shade('#2a2420', 0)
    return im


def light_top(lit):
    im = new(base='#e8f0f0' if lit else '#9aa4a8', seed=301, spread=2 if lit else 4)
    frame_rect(im, 0, 0, 16, 16, '#5a5e62')
    fill(im, 3, 4, 7, 12, '#5a3a28' if lit else '#3a2a20', a=180); fill(im, 9, 3, 12, 10, '#5a3a28' if lit else '#3a2a20', a=160)
    return im


def photos(stage):
    """Continuity photographs pinned in rows: the same stretch of hall, a darker rectangle at its far end in each."""
    im = new(); rnd = random.Random(311 + stage); px = im.load()
    for row in range(2):
        for col in range(3):
            x0, y0 = 1 + col * 5, 2 + row * 7
            fill(im, x0, y0, x0 + 4, y0 + 5, '#d8d0c0', rnd.randint(0, 99), 3)
            depth = 1 + (stage * 6 + row * 3 + col) % 3
            fill(im, x0 + 1, y0 + 1, x0 + 3, y0 + 4, '#6a6258')
            px[x0 + 1 + (depth > 1), y0 + 1 + min(2, depth)] = shade('#2a2622', 0)
            px[x0 + 2, y0] = shade('#c83030', 0)
    return im


def paper(seed, lines, header='#2a2a2a'):
    im = new(base='#ece8dc', seed=seed, spread=2); px = im.load()
    fill(im, 2, 1, 10, 3, header)
    for y in range(4, 15, lines):
        line(im, 2, y, 13 - (y * 7) % 5, y, '#6a6a70')
    return im


def flatbed(part, stage=0):
    """The flatbed editing table: its four plates, the work print threaded across them; running, the reels a blur."""
    if part == 'side':
        return new(base='#4a5056', seed=321, spread=2)
    if part == 'front':
        im = new(base='#4a5056', seed=322, spread=2); fill(im, 2, 3, 14, 5, '#2a2e32')
        for x in (4, 7, 10):
            fill(im, x, 8, x + 2, 10, '#c8c8c0')
        return im
    if part == 'screen':
        im = new(base='#2a2e32', seed=323, spread=2); fill(im, 1, 1, 11, 9, '#1a2420' if stage < 2 else '#7a8480')
        return im
    im = new(base='#5a6066', seed=324, spread=2); px = im.load(); rnd = random.Random(325 + stage)
    for cx, cy in ((3, 4), (12, 4), (3, 12), (12, 12)):
        for y in range(16):
            for x in range(16):
                d = (x - cx) ** 2 + (y - cy) ** 2
                if d < 8:
                    px[x, y] = shade('#8a9096', -8 if d > 4 else 4)
                    if stage >= 1 and d < 6:
                        px[x, y] = shade('#2a2420', rnd.randint(-6, 6) if stage == 2 else 0)
    if stage >= 1:
        line(im, 5, 4, 10, 4, '#2a2420'); line(im, 5, 12, 10, 12, '#2a2420'); line(im, 7, 4, 7, 12, '#2a2420')
    return im


def trim_bin(part):
    if part == 'strips':
        im = new(); px = im.load()
        for x in range(1, 16, 3):
            for y in range(0, 6 + (x * 5) % 9):
                px[x, y] = shade('#3a3026', 0, 230)
        return im
    if part == 'top':
        return new(base='#1a1816', seed=331, spread=2)
    im = new(base='#c8bca0', seed=332, spread=4)
    line(im, 0, 1, 15, 1, '#8a7e64')
    return im


def desk(part):
    """The booth's mixing desk: a row of faders, a meter."""
    if part == 'top':
        im = new(base='#3a3e44', seed=341, spread=2); px = im.load()
        for x in range(2, 14, 2):
            line(im, x, 4, x, 12, '#1a1c1e'); px[x, 6 + (x * 3) % 5] = shade('#d8d8d0', 0)
        fill(im, 2, 1, 14, 3, '#2a3a2a'); px[4, 2] = shade('#60e060', 0); px[5, 2] = shade('#e0c040', 0)
        return im
    return new(base='#4a4e54', seed=342, spread=2)


def mic():
    im = new(base='#2a2a2c', seed=351, spread=2); px = im.load()
    for y in range(0, 16, 2):
        for x in range(0, 16, 2):
            px[x, y] = shade('#6a6a70', 0)
    return im


def flat(part):
    """The set's flats: papered on the side the camera saw, in a tired floral, a seam where two sheets meet; bare battens behind."""
    if part == 'paper':
        im = new(base='#b8a888', seed=361, spread=3); px = im.load()
        for y in range(16):
            for x in range(16):
                if (x * 3 + y * 2) % 9 == 0 or (x - 4) % 8 == 0 and y % 8 in (2, 3):
                    px[x, y] = shade('#8a6a5a', -6)
        for y in range(16):
            px[0, y] = shade('#a09070', 0)
        for x in range(16):
            px[x, 15] = shade('#5a4a3a', 0)
        return im
    if part == 'edge':
        return new(base='#c8a878', seed=362, spread=3)
    im = plywood(); px = im.load()
    for y in range(16):
        px[3, y] = shade('#8a6a44', 0); px[12, y] = shade('#8a6a44', 0)
    return im


def bulb(on):
    im = new(base='#fff2c8' if on else '#a8a49a', seed=371, spread=2)
    if on:
        px = im.load(); px[7, 7] = (255, 255, 255, 255); px[8, 8] = (255, 255, 255, 255)
    return im


def mark():
    """Chalk on the boards: a cross, a number beside it."""
    im = new(); px = im.load()
    for i in range(3, 13):
        px[i, i] = (232, 230, 220, 230); px[15 - i, i] = (232, 230, 220, 230)
    for x, y in ((12, 1), (13, 1), (12, 2), (13, 3), (12, 3), (14, 1), (14, 3)):
        px[x, y] = (232, 230, 220, 200)
    return im


def camera(part):
    """The House's camera: a black box with a magazine on top, its lens a dark glass eye; running, the tally light red."""
    if part == 'front':
        im = new(base='#1e1e20', seed=381, spread=2); px = im.load()
        for y in range(16):
            for x in range(16):
                d = (x - 8) ** 2 + (y - 8) ** 2
                if d < 16:
                    px[x, y] = shade('#3a3e44' if d > 9 else '#0a0c10', 0)
        px[7, 6] = (120, 140, 160, 255)
        return im
    if part == 'metal':
        return new(base='#2a2a2c', seed=382, spread=2)
    im = new(base='#242426', seed=383, spread=2)
    fill(im, 2, 2, 6, 4, '#3a3a3c')
    if part == 'side_lit':
        fill(im, 12, 2, 14, 4, '#ff3020')
    return im


def slate():
    """The slate: black board, chalked scene and take, striped sticks."""
    im = new(base='#1c1c1c', seed=391, spread=2); px = im.load()
    for x in range(16):
        for y in range(0, 3):
            px[x, y] = shade('#e8e8e0' if (x // 2) % 2 else '#1c1c1c', 0)
    line(im, 2, 5, 13, 5, '#c8c8c0'); line(im, 2, 9, 13, 9, '#c8c8c0')
    for x, y in ((3, 7), (4, 7), (5, 7), (9, 7), (10, 7), (3, 11), (4, 11)):
        px[x, y] = shade('#d8d8d0', 0)
    return im


# ---------------------------------------------------------------------------------------------------- items
def cassette(label=None, stain=False, wrap=False, seed=401):
    im = new(); fill(im, 1, 4, 15, 13, VHS, seed, 2); px = im.load()
    fill(im, 4, 7, 6, 9, '#4a4a4a'); fill(im, 10, 7, 12, 9, '#4a4a4a'); fill(im, 7, 7, 9, 9, '#2a2a2a')
    if label:
        fill(im, 3, 4, 13, 6, label)
        if label == '#efe8d8':
            pass
        else:
            line(im, 4, 5, 11, 5, '#3a3a6a')
    if stain:
        for a in range(0, 360, 12):
            x, y = 8 + round(4.5 * math.cos(math.radians(a))), 8 + round(3.2 * math.sin(math.radians(a)))
            px[x, y] = shade('#6a6458', 0)
    if wrap:
        for y in range(4, 13):
            px[2 + (y % 3), y] = (220, 230, 240, 160)
    frame_rect(im, 1, 4, 15, 13, '#2c2c2c')
    return im


def item_scene():
    """The last scene: a short roll of exposed film in a tin, marked 88."""
    im = new(); px = im.load()
    for y in range(16):
        for x in range(16):
            d = (x - 8) ** 2 + (y - 8) ** 2
            if d < 42:
                px[x, y] = shade('#9aa0a6', -8 if d > 30 else 0)
            if d < 14:
                px[x, y] = shade('#2a2420', 0)
            if d < 3:
                px[x, y] = shade('#c8c8c0', 0)
    for x, y in ((11, 3), (12, 3), (11, 4), (12, 5), (11, 5), (13, 3), (13, 4), (13, 5)):
        px[x, y] = shade('#f0f0e8', 0)
    return im


def item_camera():
    im = new(); fill(im, 2, 6, 12, 13, '#1e1e20', 411, 2)
    for x0 in (3, 8):
        fill(im, x0, 2, x0 + 4, 6, '#2a2a2c')
    fill(im, 12, 8, 15, 11, '#3a3e44'); fill(im, 13, 9, 15, 10, '#0a0c10')
    im.load()[4, 7] = (255, 48, 32, 255)
    return im


def item_costume():
    """The costume folded: sacking, a long glove hanging off it."""
    im = new(); s = sack(421); px = im.load(); sp = s.load()
    for y in range(4, 12):
        for x in range(2, 14):
            px[x, y] = sp[x, y]
    frame_rect(im, 2, 4, 14, 12, '#4a3e2e')
    for y in range(10, 16):
        px[11, y] = shade('#2a2420', 0); px[12, y] = shade('#2a2420', 0)
    px[11, 15] = (0, 0, 0, 0)
    line(im, 4, 7, 9, 7, '#3a3024')
    return im


# ---------------------------------------------------------------------------------------------------- the costume's sheet
def costume_sheet():
    """The monster: sacking over everything, stitched seams, a hood with no face, gloves a joint too long at the arms' ends."""
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(431)

    def region(x0, y0, x1, y1, base=SACK):
        for y in range(y0, y1):
            for x in range(x0, x1):
                k = rnd.randint(-5, 5) + (-8 if (x + y) % 2 == 0 else 0) + (4 if x % 4 == 0 or y % 4 == 0 else 0)
                px[x, y] = shade(base, k)
    region(0, 0, 32, 16)                  # the hood
    region(16, 16, 40, 32)                # the body
    region(40, 16, 56, 34)                # the arms, two pixels longer
    region(0, 16, 16, 32, '#5e4e3a')      # the legs
    # Seams: a stitched line down the hood's front where a face would be, round the neck, down the body's sides.
    for y in range(8, 16):
        px[12, y] = shade('#2a2218', 0) if y % 2 else shade('#8a7a5a', 0)
    for x in range(8, 16):
        px[x, 15] = shade('#2a2218', 0) if x % 2 else shade('#8a7a5a', 0)
    for y in range(20, 32):
        px[20, y] = shade('#2a2218', 0) if y % 2 else px[20, y]; px[27, y] = shade('#2a2218', 0) if y % 2 else px[27, y]
    # The hood's front: no eyes, two darker stains where eyes would be, low and too far apart.
    for x, y in ((9, 12), (10, 12), (14, 12), (15, 12)):
        px[x, y] = shade('#3e3226', 0)
    # The gloves: the arms' last six rows dark leather, split into fingers at the very end.
    for y in range(28, 34):
        for x in range(40, 56):
            px[x, y] = shade('#241e18', rnd.randint(-3, 3)) if not (y >= 32 and (x - 40) % 4 == 3) else (0, 0, 0, 0)
    # Boots: the legs' last three rows.
    for y in range(29, 32):
        for x in range(0, 16):
            px[x, y] = shade('#2a221a', rnd.randint(-2, 2))
    return im


# ---------------------------------------------------------------------------------------------------- the film
W, H = 128, 96
GLYPHS = {
    'H': ['10001', '10001', '10001', '11111', '10001', '10001', '10001'],
    'A': ['01110', '10001', '10001', '11111', '10001', '10001', '10001'],
    'L': ['10000', '10000', '10000', '10000', '10000', '10000', '11111'],
    'S': ['01111', '10000', '10000', '01110', '00001', '00001', '11110'],
    'T': ['11111', '00100', '00100', '00100', '00100', '00100', '00100'],
    'N': ['10001', '11001', '10101', '10101', '10011', '10001', '10001'],
    'D': ['11110', '10001', '10001', '10001', '10001', '10001', '11110'],
    'E': ['11111', '10000', '10000', '11110', '10000', '10000', '11111'],
}


def text(im, ox, oy, word, colour, scale=2, spacing=2):
    px = im.load(); x = ox
    for ch in word:
        if ch == ' ':
            x += 3 * scale; continue
        for r, row in enumerate(GLYPHS[ch]):
            for c, bit in enumerate(row):
                if bit == '1':
                    for dy in range(scale):
                        for dx in range(scale):
                            px[x + c * scale + dx, oy + r * scale + dy] = colour
        x += 5 * scale + spacing
    return x


def text_width(word, scale=2, spacing=2):
    return sum(3 * scale if ch == ' ' else 5 * scale + spacing for ch in word) - spacing


def hallway(far, bulb_at=.88, wall='#8a7a62', floor='#5a4632', ceiling_c='#9a9284', dark=1.0, seed=0):
    """One-point perspective down a hall: papered walls, boards, a bulb near the far end. far: the far wall's half size."""
    im = Image.new('RGBA', (W, H)); px = im.load(); rnd = random.Random(500 + seed)
    vx, vy = 64, 42
    fl, fr, ft, fb = vx - far[0], vx + far[0], vy - far[1], vy + far[1] + far[1] // 2
    s = (fr - fl) / W
    for y in range(H):
        for x in range(W):
            limits = {'left': x / fl if fl else 9, 'right': (W - 1 - x) / (W - 1 - fr), 'top': y / ft, 'bottom': (H - 1 - y) / (H - 1 - fb)}
            surface = min(limits, key=limits.get); t = limits[surface]
            if t >= 1:
                surface, t = 'far', 1
            depth = 1 / (1 - t * (1 - s))
            light = max(.18, 1 - (abs(t - bulb_at) * 1.6)) ** 1.2 * dark
            if surface in ('left', 'right'):
                stripe = int(depth * 3) % 2 == 0
                top, bottom = ft * t, H - 1 - (H - 1 - fb) * t
                v = (y - top) / max(1, bottom - top)
                c = shade(wall, (-10 if stripe else 4) + (-16 if v > .78 else 0))
            elif surface == 'bottom':
                half = (W / 2) * (1 - t * (1 - s))
                u = (x - vx) / max(1, half)
                c = shade(floor, -12 if int((u + 1) * 5) != int((u + 1) * 5 + .15) else 0)
            elif surface == 'top':
                c = shade(ceiling_c, 0)
            else:
                c = shade(wall, -24)
            k = light * (1.15 if surface == 'top' and abs(t - bulb_at) < .05 else 1)
            n = rnd.randint(-4, 4)
            px[x, y] = (max(0, min(255, int(c[0] * k) + n)), max(0, min(255, int(c[1] * k) + n)), max(0, min(255, int(c[2] * k * .92) + n)), 255)
    # The bulb, hanging from the ceiling at its depth, and its glow.
    bt = bulb_at; by = int(ft * bt) + 3; bx = vx
    for y in range(H):
        for x in range(W):
            d = math.hypot(x - bx, (y - by) * 1.3)
            if d < 16:
                r, g, b, a = px[x, y]; k = (1 - d / 16) ** 2 * 60
                px[x, y] = (min(255, int(r + k)), min(255, int(g + k * .85)), min(255, int(b + k * .5)), 255)
    line(im, bx, int(ft * bt), bx, by - 1, '#2a2622')
    for dx in (-1, 0, 1):
        px[bx + dx, by] = rgb(BULB) + (255,)
    px[bx, by + 1] = rgb(BULB) + (255,)
    return im


def plate_title():
    im = Image.new('RGBA', (W, H), (8, 8, 8, 255))
    word = 'HALLSTAND'; w = text_width(word); text(im, (W - w) // 2, 38, word, (214, 208, 196, 255))
    line(im, 34, 58, 93, 58, '#6a6660')
    return im


def plate_hall():
    im = hallway((10, 12), seed=1)
    fill(im, 8, 62, 36, 66, '#3a2c20'); fill(im, 10, 66, 12, 86, '#2a2018'); fill(im, 32, 66, 34, 86, '#2a2018')
    fill(im, 14, 60, 22, 62, '#d8d0c0')
    return im


def plate_door():
    """A door from the side it should not be seen from: the hinges toward us, a line of light along its foot."""
    im = Image.new('RGBA', (W, H), (40, 34, 28, 255)); rnd = random.Random(511); px = im.load()
    for y in range(H):
        for x in range(W):
            px[x, y] = shade('#3a3026', rnd.randint(-4, 4))
    fill(im, 36, 4, 92, 96, '#2a2018')
    fill(im, 40, 8, 88, 92, '#6a5038', 512, 4)
    for x0, y0, x1, y1 in ((44, 12, 62, 44), (66, 12, 84, 44), (44, 52, 62, 88), (66, 52, 84, 88)):
        frame_rect(im, x0, y0, x1, y1, '#4a3626')
    for y0 in (18, 76):
        fill(im, 40, y0, 44, y0 + 8, '#9a8a60')
    fill(im, 84, 48, 87, 54, '#8a7a50')
    for x in range(40, 88):
        px[x, 92] = (240, 220, 170, 255); px[x, 93] = (180, 160, 120, 255)
    return im


def plate_corridor():
    return hallway((4, 5), bulb_at=.7, wall='#6a5e4c', floor='#463626', ceiling_c='#7a7468', dark=.85, seed=2)


def plate_room():
    """A bedroom, dim: two walls meeting, a bed, a window with its blind half down."""
    im = Image.new('RGBA', (W, H)); px = im.load(); rnd = random.Random(521)
    for y in range(H):
        for x in range(W):
            if y > 64 + (x - 70) * .18 if x > 70 else y > 64 - (70 - x) * .25:
                c = shade('#3a3026', rnd.randint(-3, 3))
            else:
                c = shade('#6a6e74' if x > 70 else '#5a5e64', rnd.randint(-3, 3))
            px[x, y] = c
    line(im, 70, 0, 70, 64, '#3a3e44')
    fill(im, 84, 14, 112, 46, '#9aa8b8'); fill(im, 84, 14, 112, 28, '#c8c4b8')
    for y in range(14, 28, 2):
        line(im, 84, y, 111, y, '#a8a498')
    frame_rect(im, 83, 13, 113, 47, '#2a2e34')
    fill(im, 6, 58, 58, 76, '#8a8478'); fill(im, 6, 54, 20, 60, '#c8c0b0'); fill(im, 6, 76, 58, 84, '#3a3024')
    return im


def plate_page():
    """A sheet of paper on a table, from above; the writing is put on it by the film as it plays."""
    im = Image.new('RGBA', (W, H)); px = im.load(); rnd = random.Random(531)
    for y in range(H):
        for x in range(W):
            px[x, y] = shade('#4a3424', rnd.randint(-5, 5) + (-8 if (y + x // 9) % 7 == 0 else 0))
    for y in range(12, 86):
        for x in range(20, 110):
            if 20 + (y - 12) * .06 <= x <= 108 + (y - 12) * .04:
                px[x, y] = shade('#e8e0cc', rnd.randint(-3, 3))
    for y in range(26, 84, 9):
        for x in range(26, 104):
            if px[x, y][0] > 180:
                px[x, y] = shade('#c8c8d4', 0)
    return im


def plate_end():
    """The hall's far end, nobody under the bulb, the mark chalked on the boards."""
    im = hallway((7, 9), bulb_at=.95, wall='#5a5040', floor='#3e3022', ceiling_c='#6a6458', dark=.8, seed=3); px = im.load()
    for i in range(-4, 5):
        px[64 + i, 56 + i // 2] = (200, 196, 186, 255); px[64 + i, 56 - i // 2] = (200, 196, 186, 255)
    return im


def plate_well():
    """A stone well in a clearing at dusk, from a little way off."""
    im = Image.new('RGBA', (W, H)); px = im.load(); rnd = random.Random(541)
    for y in range(H):
        for x in range(W):
            if y < 40:
                c = shade('#8a8e98', -y)
            elif y < 46:
                c = shade('#1e2420', rnd.randint(-3, 3))
            else:
                c = shade('#3e4a34', rnd.randint(-6, 6))
            px[x, y] = c
    for y in range(56, 78):
        for x in range(44, 84):
            if ((x - 64) / 20) ** 2 + ((y - 62) / 6) ** 2 <= 1 or (y > 62 and 44 <= x < 84 and y < 76):
                px[x, y] = shade('#7a7468', rnd.randint(-8, 8) + (-12 if (x // 5 + y // 3) % 2 else 0))
    for y in range(57, 67):
        for x in range(48, 80):
            if ((x - 64) / 16) ** 2 + ((y - 62) / 4.5) ** 2 <= 1:
                px[x, y] = (6, 6, 6, 255)
    return im


def figure(scale):
    """The figure in the costume: a rounded hood, shoulders, arms too long, hanging below its knees. Transparent elsewhere."""
    w, h = 48 * scale // 4, 96 * scale // 4
    big = Image.new('RGBA', (48, 96), (0, 0, 0, 0)); px = big.load(); rnd = random.Random(551)

    def put(x, y, k=0):
        if 0 <= x < 48 and 0 <= y < 96:
            px[x, y] = shade('#3a3128', rnd.randint(-5, 5) + k)
    for y in range(4, 22):
        for x in range(14, 34):
            if ((x - 24) / 10) ** 2 + ((y - 14) / 10) ** 2 <= 1:
                put(x, y, 6 if x < 20 else 0)
    for y in range(20, 56):
        for x in range(12, 36):
            if abs(x - 24) <= 10 - max(0, 26 - y) // 2:
                put(x, y)
    for side in (-1, 1):
        for y in range(22, 78):
            for t in range(5):
                x = 24 + side * (12 + (y - 22) // 14) + side * (t - 2)
                put(x, y, 4 if t == 0 else 0)
        for f in range(4):
            for y in range(78, 86 + f % 2 * 2):
                put(24 + side * (11 + (78 - 22) // 14) + side * (f - 2), y, -10)
    for leg in (-1, 1):
        for y in range(54, 94):
            for x in range(24 + leg * 2 - 4 if leg > 0 else 24 - 8, 24 + leg * 2 + 4 if leg > 0 else 24):
                put(x, y, -4)
    if scale == 4:
        return big
    return big.resize((w, h), Image.NEAREST)


def hand():
    """A hand writing, side on, the pen in it."""
    im = Image.new('RGBA', (32, 24), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(561)
    for y in range(4, 20):
        for x in range(6, 30):
            if ((x - 18) / 12) ** 2 + ((y - 12) / 7) ** 2 <= 1:
                px[x, y] = shade('#c8a088', rnd.randint(-6, 6) - (y - 12) * 2)
    line(im, 2, 22, 12, 12, '#1a1a2a'); line(im, 3, 22, 13, 12, '#2a2a3a')
    return im


def handprints():
    """Hands pressed flat on the inside of the glass, pale and smeared."""
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(571)
    for cx, cy, ang in ((30, 50, -.3), (62, 34, .1), (96, 54, .35), (48, 74, -.1), (84, 20, .2)):
        ca, sa = math.cos(ang), math.sin(ang)

        def put(dx, dy, a):
            x, y = int(cx + dx * ca - dy * sa), int(cy + dx * sa + dy * ca)
            if 0 <= x < W and 0 <= y < H:
                px[x, y] = (226, 228, 222, max(px[x, y][3], a))
        for dy in range(-7, 8):
            for dx in range(-6, 7):
                if (dx / 6.5) ** 2 + (dy / 7.5) ** 2 <= 1:
                    put(dx, dy, 150 + rnd.randint(-30, 20))
        for f, (fx, length) in enumerate(((-5, 8), (-2, 11), (1, 12), (4, 10))):
            for dy in range(-7 - length, -6):
                for dx in (fx - 1, fx, fx + 1):
                    put(dx, dy, 130 + rnd.randint(-40, 20))
        for t in range(7):
            put(7 + t // 2, -2 + t, 120)
        for t in range(10):
            put(rnd.randint(-6, 6), 7 + t, 60)
    return im


def atlas():
    im = Image.new('RGBA', (512, 288), (0, 0, 0, 0))
    for (x, y), shot in (((0, 0), plate_title()), ((128, 0), plate_hall()), ((256, 0), plate_door()), ((384, 0), plate_corridor()),
                         ((0, 96), plate_room()), ((128, 96), plate_page()), ((256, 96), plate_end()), ((384, 96), plate_well())):
        im.paste(shot, (x, y))
    im.paste(figure(1), (0, 192)); im.paste(figure(2), (16, 192)); im.paste(figure(4), (48, 192))
    im.paste(hand(), (96, 192)); im.paste(handprints(), (128, 192))
    return im


# ---------------------------------------------------------------------------------------------------- models
def element(lo, hi, textures, rotation=None, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if rotation:
        e['rotation'] = rotation
    return e


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def legs(x0, x1, z0, z1, top, t, w=1):
    return [element([x0, 0, z0], [x0 + w, top, z0 + w], t), element([x1 - w, 0, z0], [x1, top, z0 + w], t),
            element([x0, 0, z1 - w], [x0 + w, top, z1], t), element([x1 - w, 0, z1 - w], [x1, top, z1], t)]


def fitting_models():
    m = {}
    for s in range(3):
        # 0.4.220: a lip in front of each row, so the tapes stand on shelves rather than being painted on a board.
        m[f'shelf_{s}'] = model([element([0, 0, 2], [16, 16, 14], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'}),
                                 element([0, 0, 1], [16, 1, 2], {'all': 'l'}, None, {'north': [0, 15, 16, 16], 'up': [0, 15, 16, 16]}),
                                 element([0, 8, 1], [16, 9, 2], {'all': 'l'}, None, {'north': [0, 7, 16, 8], 'up': [0, 7, 16, 8]})],
                                {'s': 'shelf_side', 'f': f'shelf_{s}', 't': 'shelf_side', 'l': f'shelf_{s}'}, 'shelf_side')
    # The counter faces the shop (its front, north in the model, is turned to the customers). 0.4.220: the till on it keys toward
    # the clerk (the model's south), its drawer under them; the display stands at the back, turned to the customer. Stage 2 is
    # the till with its drawer open.
    for s in range(3):
        es = [element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'south': 'r', 'up': 't'})]
        if s:
            es += [element([2.5, 16, 3], [13.5, 18.5, 13], {'all': 'b', 'south': 'd'}, None, {'south': [0, 0, 16, 16]}),
                   element([2.5, 18.5, 5], [13.5, 20, 13], {'all': 'b', 'up': 'k'}, None, {'up': [0, 0, 16, 16]}),
                   element([4, 20, 4], [12, 23.5, 6], {'all': 'b', 'north': 'v'}, None, {'north': [0, 2, 16, 14]})]
            if s == 2:
                es += [element([3, 16.2, 13], [13, 18.2, 17], {'all': 'b', 'up': 'o', 'south': 'd'}, None, {'up': [0, 0, 16, 16], 'south': [0, 0, 16, 16]})]
        m[f'counter_{s}'] = model(es, {'s': 'counter_side', 'r': 'counter_back', 'f': 'counter_front', 't': 'counter_top', 'b': 'till_side', 'k': 'till_front',
                                       'v': 'till_display', 'd': 'till_drawer', 'o': 'till_open'}, 'counter_top')
    m['ledger_0'] = model([element([3, 0, 3], [13, 2, 13], {'all': 's', 'up': 'p'})], {'s': 'ledger_side', 'p': 'ledger_page'}, 'ledger_page')
    m['card_0'] = model([element([4, 0, 4], [12, .5, 12], {'up': 'c', 'down': 'c', 'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'})], {'c': 'card'}, 'card')
    for s in range(3):
        m[f'returns_{s}'] = model([element([2, 0, 2], [14, 12, 14], {'all': 's', 'north': 'f', 'up': 't'}), element([2, 12, 2], [14, 12.5, 14], {'north': 's', 'south': 's', 'east': 's', 'west': 's'})],
                                  {'s': 'returns_side', 'f': 'returns_front', 't': f'returns_top_{s}'}, 'returns_side')
    m['slot_0'] = model([element([3, 8, 15], [13, 12, 16], {'all': 's'})], {'s': 'slot'}, 'slot')
    for s in range(2):
        m[f'phone_{s}'] = model([element([4, 0, 5], [12, 3, 11], {'all': 'b', 'up': 't'}), element([4, 3, 9], [12, 5, 11], {'all': 'b'}), element([4.5, 5, 9.5], [11.5, 6, 10.5], {'all': 'b'})],
                                {'b': 'phone_body', 't': 'phone_top_lit' if s else 'phone_top'}, 'phone_body')
    for s in range(2):
        es = [element([4, 0, 4], [12, 1, 12], {'all': 'm'}), element([7.5, 1, 7.5], [8.5, 6, 8.5], {'all': 'm'}),
              element([4, 6, 6], [12, 15, 10], {'all': 'c' if s == 0 else 'b'}), element([7, 15, 7], [9, 16, 9], {'all': 'b'})]
        if s == 0:
            es += [element([2, 1, 7], [4, 14, 9], {'all': 'c'}), element([12, 1, 7], [14, 14, 9], {'all': 'c'}), element([5, 14.5, 5.5], [11, 16, 10.5], {'all': 'c'}),
                   element([2, 0, 7], [4, 1, 9], {'all': 'g'}), element([12, 0, 7], [14, 1, 9], {'all': 'g'})]
        m[f'mannequin_{s}'] = model(es, {'m': 'stand', 'c': 'sack', 'b': 'mannequin', 'g': 'glove'}, 'stand')
    for s in range(4):
        m[f'poster_{s}'] = model([element([1, 1, 15.5], [15, 15, 16], {'north': 'p', 'south': 'p'})], {'p': f'poster_{s}'}, f'poster_{s}')
    m['section_0'] = model([element([0, 4, 15.5], [16, 12, 16], {'north': 's', 'south': 's', 'up': 's', 'down': 's'}, None, {'north': [0, 4, 16, 12], 'south': [0, 4, 16, 12]})], {'s': 'section'}, 'section')
    for s in range(2):
        m[f'tube_{s}'] = model([element([2, 15, 6], [14, 16, 10], {'all': 'f'}), element([2.5, 13.5, 7], [13.5, 15, 9], {'all': 'l'})], {'f': 'tube_frame', 'l': 'tube_lit' if s == 0 else 'tube_dim'}, 'tube_frame')
    for s, front in enumerate(('crt_dark', 'crt_snow', 'crt_lit')):
        m[f'crt_{s}'] = model([element([1, 0, 1], [15, 14, 15], {'all': 's', 'north': 'f', 'up': 't'}), element([5, 14, 9], [11, 15, 13], {'all': 't'})], {'s': 'crt_side', 'f': front, 't': 'crt_top'}, 'crt_side')
    metal = {'all': 'm'}
    for s in range(4):
        es = legs(0, 16, 0, 16, 16, metal) + [element([0, 15, 0], [16, 16, 16], metal), element([0, 8, 0], [16, 9, 16], metal), element([0, 1, 0], [16, 2, 16], metal),
                                              element([1, 9, 1], [15, 15, 15], {'all': 'b', 'north': 'd'}), element([2, 2, 3], [9, 5, 13], {'all': 'tp'})]
        m[f'cart_{s}'] = model(es, {'m': 'cart_metal', 'b': 'cart_body', 'd': f'decks_{s}', 'tp': 'tapes_side'}, 'cart_metal')
    m['blanks_0'] = model([element([3, 0, 4], [13, 6, 12], {'all': 's', 'up': 't'})], {'s': 'blanks_side', 't': 'blanks_top'}, 'blanks_side')
    m['chair_0'] = model(legs(3, 13, 3, 13, 7, {'all': 'm'}) + [element([3, 7, 3], [13, 8, 13], {'all': 's'}), element([3, 8, 12], [13, 15, 13], {'all': 's'}),
                                                             element([3, 7, 12], [4, 15, 13], {'all': 'm'}), element([12, 7, 12], [13, 15, 13], {'all': 'm'})], {'m': 'chair_metal', 's': 'chair_seat'}, 'chair_seat')
    m['tapes_0'] = model([element([2, 0, 2], [14, 3, 14], {'all': 's', 'up': 't'}), element([3, 3, 3], [13, 6, 12], {'all': 's', 'up': 't'}), element([2, 6, 4], [12, 8, 13], {'all': 's', 'up': 't'})],
                         {'s': 'tapes_side', 't': 'tapes_top'}, 'tapes_side')
    m['rack_0'] = model([element([0, 0, 8], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'rack_side', 'f': 'rack_front'}, 'rack_front')
    for s in range(2):
        m[f'splicer_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'bench_side', 'f': 'bench_front', 't': f'splicer_top_{s}'}, 'bench_side')
        m[f'light_table_{s}'] = model([element([0, 0, 0], [16, 14, 16], {'all': 's', 'up': 't'})], {'s': 'bench_side', 't': 'light_top_lit' if s else 'light_top_off'}, 'bench_side')
        m[f'bulb_{s}'] = model([element([7.5, 10, 7.5], [8.5, 16, 8.5], {'all': 'x'}), element([6.5, 6, 6.5], [9.5, 10, 9.5], {'all': 'b'})], {'x': 'flex', 'b': 'bulb_on' if s == 0 else 'bulb_off'}, 'flex')
        m[f'flat_{s}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'b', 'north': 'p' if s == 0 else 'b', 'up': 'e', 'down': 'e'})], {'b': 'flat_back', 'p': 'flat_paper', 'e': 'flat_edge'}, 'flat_paper')
    for s in range(3):
        m[f'photos_{s}'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 'p', 'south': 'p'})], {'p': f'photos_{s}'}, f'photos_{s}')
        top = element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})
        screen = element([2, 16, 12], [14, 26, 14], {'all': 'k', 'north': 'v'}, None, {'north': [0, 0, 12, 10]})
        stem = element([7, 16, 14], [9, 18, 15], {'all': 'k'})
        m[f'flatbed_{s}'] = model([top, screen, stem], {'s': 'flatbed_side', 'f': 'flatbed_front', 't': f'flatbed_top_{s}', 'k': 'flatbed_side', 'v': 'flatbed_screen_on' if s == 2 else 'flatbed_screen_off'}, 'flatbed_side')
        legs_t = {'all': 'l'}
        es = [element([7.5, 0, 7.5], [8.5, 11, 8.5], legs_t, {'angle': 22.5, 'axis': 'z', 'origin': [8, 11, 8]}), element([7.5, 0, 7.5], [8.5, 11, 8.5], legs_t, {'angle': -22.5, 'axis': 'z', 'origin': [8, 11, 8]}),
              element([7.5, 0, 7.5], [8.5, 11, 8.5], legs_t, {'angle': 22.5, 'axis': 'x', 'origin': [8, 11, 8]}), element([6, 11, 6], [10, 12, 10], legs_t)]
        if s < 2:
            es += [element([5, 12, 4], [11, 16, 13], {'all': 'c', 'north': 'cf'}), element([6, 16, 7], [10, 19, 12], {'all': 'm'})]
        m[f'tripod_{s}'] = model(es, {'l': 'camera_metal', 'c': 'camera_side_lit' if s == 1 else 'camera_side', 'cf': 'camera_front', 'm': 'camera_metal'}, 'camera_metal')
    m['script_0'] = model([element([2, 2, 15.5], [14, 14, 16], {'north': 'p', 'south': 'p'}, None, {'north': [2, 2, 14, 14], 'south': [2, 2, 14, 14]})], {'p': 'script'}, 'script')
    m['call_sheet_0'] = model([element([3, 3, 15.5], [13, 13, 16], {'north': 'p', 'south': 'p'}, None, {'north': [3, 3, 13, 13], 'south': [3, 3, 13, 13]})], {'p': 'call_sheet'}, 'call_sheet')
    m['trim_bin_0'] = model([element([1, 0, 1], [15, 10, 15], {'all': 's', 'up': 't'}), element([0, 15, 7.5], [16, 16, 8.5], {'all': 'm'}),
                             element([2, 6, 8], [14, 15, 8], {'north': 'f', 'south': 'f'}), element([0, 0, 7.5], [1, 15, 8.5], {'all': 'm'}), element([15, 0, 7.5], [16, 15, 8.5], {'all': 'm'})],
                            {'s': 'bin_side', 't': 'bin_top', 'm': 'camera_metal', 'f': 'strips'}, 'bin_side')
    m['desk_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'up': 't'})], {'s': 'desk_side', 't': 'desk_top'}, 'desk_top')
    m['mic_0'] = model([element([5, 0, 5], [11, 1, 11], {'all': 'm'}), element([7.5, 1, 7.5], [8.5, 12, 8.5], {'all': 'm'}), element([6.5, 12, 6.5], [9.5, 16, 9.5], {'all': 'g'})], {'m': 'camera_metal', 'g': 'mic'}, 'mic')
    m['mark_0'] = model([element([0, 0, 0], [16, .05, 16], {'up': 'x'})], {'x': 'mark'}, 'mark')
    m['slate_0'] = model([element([4, 0, 7], [12, 9, 9], {'all': 'b', 'north': 's'}, None, {'north': [4, 3, 12, 12]}), element([4, 9, 7], [12, 10, 9], {'all': 'b'}, {'angle': 22.5, 'axis': 'z', 'origin': [4, 9, 8]})],
                         {'b': 'slate', 's': 'slate'}, 'slate')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/work_print/{k}.png'] = v
    for s in range(3):
        out[f'block/work_print/shelf_{s}.png'] = shelf_front(s)
        out[f'block/work_print/returns_top_{s}.png'] = returns(s)
        out[f'block/work_print/photos_{s}.png'] = photos(s)
        out[f'block/work_print/flatbed_top_{s}.png'] = flatbed('top', s)
    out['block/work_print/shelf_side.png'] = wood('#5a4a3a', '#4a3c2e', 83)
    out['block/work_print/counter_front.png'] = counter_front(); out['block/work_print/counter_top.png'] = counter_top()
    out['block/work_print/counter_back.png'] = counter_back(); out['block/work_print/counter_side.png'] = counter_side()
    out['block/work_print/till_front.png'] = till(0); out['block/work_print/till_side.png'] = till(1)
    out['block/work_print/till_display.png'] = till(2); out['block/work_print/till_drawer.png'] = till(3); out['block/work_print/till_open.png'] = till(4)
    out['block/work_print/ledger_page.png'] = ledger(0); out['block/work_print/ledger_side.png'] = ledger(1)
    out['block/work_print/card.png'] = card()
    out['block/work_print/returns_front.png'] = returns('front'); out['block/work_print/returns_side.png'] = returns('side')
    out['block/work_print/slot.png'] = slot()
    out['block/work_print/phone_body.png'] = phone('body'); out['block/work_print/phone_top.png'] = phone('top'); out['block/work_print/phone_top_lit.png'] = phone('top_lit')
    out['block/work_print/sack.png'] = sack(); out['block/work_print/mannequin.png'] = mannequin()
    out['block/work_print/stand.png'] = new(base='#2a2a2a', seed=172, spread=2); out['block/work_print/glove.png'] = new(base='#241e18', seed=173, spread=2)
    for s in range(4):
        out[f'block/work_print/poster_{s}.png'] = poster(s)
        out[f'block/work_print/decks_{s}.png'] = decks(s)
    out['block/work_print/section.png'] = section()
    out['block/work_print/tube_frame.png'] = tube('frame'); out['block/work_print/tube_lit.png'] = tube('lit'); out['block/work_print/tube_dim.png'] = tube('dim')
    out['block/work_print/crt_side.png'] = crt('side'); out['block/work_print/crt_top.png'] = crt('top')
    out['block/work_print/crt_dark.png'] = crt('dark'); out['block/work_print/crt_lit.png'] = crt('lit'); out['block/work_print/crt_snow.png'] = snow_frames()
    out['block/work_print/cart_metal.png'] = cart('metal'); out['block/work_print/cart_body.png'] = cart('body')
    out['block/work_print/blanks_side.png'] = blanks('side'); out['block/work_print/blanks_top.png'] = blanks('top')
    out['block/work_print/chair_seat.png'] = chair('seat'); out['block/work_print/chair_metal.png'] = chair('metal')
    out['block/work_print/tapes_side.png'] = tapes('side'); out['block/work_print/tapes_top.png'] = tapes('top')
    out['block/work_print/rack_side.png'] = rack('side'); out['block/work_print/rack_front.png'] = rack('front')
    out['block/work_print/bench_side.png'] = bench('side'); out['block/work_print/bench_front.png'] = bench('front')
    out['block/work_print/splicer_top_0.png'] = splicer_top(0); out['block/work_print/splicer_top_1.png'] = splicer_top(1)
    out['block/work_print/light_top_off.png'] = light_top(False); out['block/work_print/light_top_lit.png'] = light_top(True)
    out['block/work_print/script.png'] = paper(341, 2, '#2a3a8a'); out['block/work_print/call_sheet.png'] = paper(342, 3)
    out['block/work_print/flatbed_side.png'] = flatbed('side'); out['block/work_print/flatbed_front.png'] = flatbed('front')
    out['block/work_print/flatbed_screen_off.png'] = flatbed('screen', 0); out['block/work_print/flatbed_screen_on.png'] = flatbed('screen', 2)
    out['block/work_print/bin_side.png'] = trim_bin('side'); out['block/work_print/bin_top.png'] = trim_bin('top'); out['block/work_print/strips.png'] = trim_bin('strips')
    out['block/work_print/desk_side.png'] = desk('side'); out['block/work_print/desk_top.png'] = desk('top')
    out['block/work_print/mic.png'] = mic()
    out['block/work_print/flat_paper.png'] = flat('paper'); out['block/work_print/flat_back.png'] = flat('back'); out['block/work_print/flat_edge.png'] = flat('edge')
    out['block/work_print/flex.png'] = new(base='#1a1a1a', seed=372, spread=1)
    out['block/work_print/bulb_on.png'] = bulb(True); out['block/work_print/bulb_off.png'] = bulb(False)
    out['block/work_print/mark.png'] = mark()
    out['block/work_print/camera_front.png'] = camera('front'); out['block/work_print/camera_side.png'] = camera('side')
    out['block/work_print/camera_side_lit.png'] = camera('side_lit'); out['block/work_print/camera_metal.png'] = camera('metal')
    out['block/work_print/slate.png'] = slate()
    out['item/work_print_tape.png'] = cassette()
    out['item/work_print_blank.png'] = cassette(label='#efe8d8', wrap=True, seed=402)
    out['item/work_print_copy.png'] = cassette(label='#e8dcc0', seed=403)
    out['item/work_print_stained.png'] = cassette(stain=True, seed=404)
    out['item/work_print_scene.png'] = item_scene()
    out['item/work_print_camera.png'] = item_camera()
    out['item/work_print_costume.png'] = item_costume()
    out['entity/work_print_costume.png'] = costume_sheet()
    out['work_print/hallstand.png'] = atlas()
    return out


KINDS = ['shelf', 'counter', 'ledger', 'card', 'returns', 'slot', 'phone', 'mannequin', 'poster', 'section', 'tube',
         'crt', 'cart', 'blanks', 'chair', 'tapes',
         'rack', 'splicer', 'light_table', 'photos', 'script', 'call_sheet', 'flatbed', 'trim_bin',
         'desk', 'mic',
         'flat', 'bulb', 'mark', 'tripod', 'slate']
STAGES = {'poster': 4, 'cart': 4, 'shelf': 3, 'returns': 3, 'crt': 3, 'photos': 3, 'flatbed': 3, 'tripod': 3,
          'counter': 3, 'phone': 2, 'mannequin': 2, 'tube': 2, 'splicer': 2, 'light_table': 2, 'bulb': 2, 'flat': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
ITEMS = ('work_print_tape', 'work_print_blank', 'work_print_copy', 'work_print_scene', 'work_print_camera', 'work_print_costume', 'work_print_stained')


def files():
    out = {}
    for name in materials():
        out[f'models/block/work_print_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/work_print_{name}.json'] = {'variants': {'': {'model': NS + f'block/work_print_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/work_print/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(4):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/work_print/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/work_print_fitting.json'] = {'variants': variants}
    for item in ITEMS:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    out['textures/block/work_print/crt_snow.png.mcmeta'] = SNOW_META
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
    print(('Verified' if check else 'Drew') + f' the video shop: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
