"""The waxworks (0.4.749): Merriman's Wax Museum after closing.

Draws, in native Minecraft pixels from fixed seeds: nine materials (the staff side's painted block, speckled linoleum and ceiling
tile; the public side's dark panelling, wallpaper, parquet, corridor runner and plaster ceiling; and the brick behind all of it); and
every fitting of the waxworks_fitting block, its models facing north and turned by its facing:

- PLINTH, PLAQUE, ROPE (a stanchion carrying the rope on (0), or the end of a run (1)).
- CAMERA, MONITOR (dark (0), its feed (1), the grain moving).
- DESK (plain, with the shift log, with the telephone), CHAIR, CLOCK (11:52, then each hour from twelve to six).
- TIMECLOCK, NOTICE (the source), LOCKER and LOCKER_TOP (shut, open).
- FRONT (the locked doors: left lower, left upper, right lower, right upper), SIGN (left and right halves), BOOTH and BOOTH_TOP.
- TABLE (four quarters of the supper table), DRESSER (lower and upper), STOVE.
- COFFIN and COFFIN_FOOT, GRAVESTONE.
- VAT, BENCH (tools, a head on its stand, hands laid out), HEADS (a shelf of heads, lower and upper).

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
P = NS + 'block/waxworks/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k, a=255):
    r, g, b = rgb(c) if isinstance(c, str) else c[:3]
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


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
            px[x, y] = shade(colour, 0, a)


def dot(im, x, y, colour, a=255):
    w, h = im.size
    if 0 <= x < w and 0 <= y < h:
        im.putpixel((x, y), shade(colour, 0, a))


def line(im, x0, y0, x1, y1, colour):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        dot(im, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), colour)


def frame(im, colour, dark):
    w, h = im.size
    for i in range(w):
        dot(im, i, 0, colour); dot(im, i, h - 1, dark)
    for i in range(h):
        dot(im, 0, i, colour); dot(im, w - 1, i, dark)


# ------------------------------------------------------------------------------------------------ materials
def block_wall():
    # Painted concrete block, three courses to the cell, the mortar showing through the paint as shallow grooves.
    im = new('#b9b5a6', 74901, 3)
    for y in (4, 9, 14):
        for x in range(16):
            dot(im, x, y, '#a29e90')
    for y0, y1, xs in ((0, 4, (7,)), (5, 9, (3, 11)), (10, 14, (7,)), (15, 16, (3, 11))):
        for x in xs:
            for y in range(y0, y1):
                dot(im, x, y, '#a29e90')
    return im


def lino():
    # Speckled linoleum tiles, a seam every eight, the speckle the same few flecks over and over.
    im = new('#8c8a7e', 74902, 2)
    rnd = random.Random(74903)
    for _ in range(26):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(('#a7a598', '#6f6d63', '#7d7b70')))
    for i in range(16):
        dot(im, i, 0, '#76746a'); dot(im, 0, i, '#76746a'); dot(im, i, 8, '#7c7a70'); dot(im, 8, i, '#7c7a70')
    return im


def ceiling_tile():
    # Fibre ceiling tile in its grid, pocked all over.
    im = new('#c4c2b8', 74904, 2)
    rnd = random.Random(74905)
    for _ in range(18):
        dot(im, rnd.randrange(1, 15), rnd.randrange(1, 15), '#aeaca2')
    frame(im, '#9a988e', '#9a988e')
    return im


def panel():
    # Dark stained panelling: a raised field in a frame, the grain running up it, a rail across the top.
    im = new('#4a3426', 74906, 3)
    for x in range(16):
        dot(im, x, 0, '#6a4c38'); dot(im, x, 1, '#3a281c')
    for y in range(3, 15):
        dot(im, 2, y, '#5a4030'); dot(im, 13, y, '#2e2016')
    for x in range(2, 14):
        dot(im, x, 3, '#5a4030'); dot(im, x, 14, '#2e2016')
    rnd = random.Random(74907)
    for _ in range(5):
        x = rnd.randrange(4, 12)
        for y in range(4, 14):
            if rnd.random() < .6:
                dot(im, x, y, '#42301f')
    return im


def paper():
    # Heavy wallpaper above the panelling: a damask lozenge repeat, faded, a seam down one side.
    im = new('#5e4a44', 74908, 2)
    for ox, oy in ((4, 4), (12, 12), (12, -4), (-4, 12), (4, 20)):
        for dy in range(-3, 4):
            w = 3 - abs(dy)
            for dx in range(-w, w + 1):
                dot(im, ox + dx, oy + dy, '#6c5650' if abs(dx) == w else '#665049')
        dot(im, ox, oy, '#7a645c')
    for y in range(16):
        dot(im, 15, y, '#544039')
    return im


def parquet():
    # Herringbone parquet, waxed, the blocks each a little different.
    im = new('#6a4a30', 74909, 2)
    rnd = random.Random(74910)
    for by in range(0, 16, 4):
        for bx in range(0, 16, 8):
            k = rnd.randint(-8, 8)
            fill(im, bx, by, bx + 8, by + 4, shade('#6a4a30', k))
            for x in range(bx, bx + 8):
                dot(im, x, by, shade('#4a3220', k))
            dot(im, bx, by + 1, shade('#4a3220', k)); dot(im, bx, by + 2, shade('#4a3220', k)); dot(im, bx, by + 3, shade('#4a3220', k))
            dot(im, bx + 2, by + 2, shade('#7c5a3e', k))
    return im


def runner():
    # The corridor's carpet runner: a deep pile with a border pattern along each side and a worn path down the middle.
    im = new('#5a1e22', 74911, 3)
    for y in range(16):
        for x in (1, 14):
            dot(im, x, y, '#8a6a3a')
        if y % 4 == 0:
            dot(im, 2, y, '#8a6a3a'); dot(im, 13, y, '#8a6a3a')
    for y in range(16):
        for x in range(6, 10):
            dot(im, x, y, shade(im.getpixel((x, y)), 6))
    return im


def plaster():
    # Smooth plaster ceiling, a water stain at one corner.
    im = new('#b6b0a2', 74912, 2)
    for x, y in ((12, 12), (13, 12), (12, 13), (13, 13), (14, 13), (13, 14)):
        dot(im, x, y, '#a69e8a')
    return im


def brick():
    # The shell: plain brick, never meant to be seen.
    im = new('#6e3e30', 74913, 4)
    for y in (3, 7, 11, 15):
        for x in range(16):
            dot(im, x, y, '#8c8478')
    for row, y0 in enumerate((0, 4, 8, 12)):
        for x in ((7, 15) if row % 2 == 0 else (3, 11)):
            for y in range(y0, y0 + 3):
                dot(im, x, y, '#8c8478')
    return im


def materials():
    return {'block': block_wall(), 'lino': lino(), 'tile': ceiling_tile(), 'panel': panel(), 'paper': paper(), 'parquet': parquet(),
            'runner': runner(), 'plaster': plaster(), 'brick': brick()}


# ------------------------------------------------------------------------------------------------ fitting textures
WOOD, BRASS, VELVET, STEEL, GLASS = '#5a3e2a', '#b0904a', '#6a1c22', '#7a7e82', '#9aa8aa'


def wood(seed, base=WOOD):
    im = new(base, seed, 3)
    rnd = random.Random(seed + 1)
    for _ in range(4):
        y = rnd.randrange(16)
        for x in range(16):
            if rnd.random() < .7:
                dot(im, x, y, shade(base, -10))
    return im


def brass():
    im = new(BRASS, 74920, 4)
    for i in range(16):
        dot(im, i, 0, '#d4b46a'); dot(im, 0, i, '#d4b46a'); dot(im, i, 15, '#7a5e2a'); dot(im, 15, i, '#7a5e2a')
    return im


def velvet():
    im = new(VELVET, 74921, 4)
    for x in range(16):
        dot(im, x, 7, '#7e2a30'); dot(im, x, 8, '#4a1216')
    return im


def steel(seed=74922):
    im = new(STEEL, seed, 3)
    frame(im, '#9a9ea2', '#5a5e62')
    return im


def plinth_side():
    # Painted to look like stone: a marbled vein, a darker base, the paint worn at the front edge.
    im = new('#7e7a70', 74923, 4)
    line(im, 2, 3, 6, 9, '#6a665c'); line(im, 6, 9, 11, 7, '#6a665c'); line(im, 11, 7, 14, 12, '#6a665c')
    for x in range(16):
        dot(im, x, 14, '#5a564e'); dot(im, x, 15, '#4a463e'); dot(im, x, 0, '#9a968a')
    return im


def plinth_top():
    im = new('#86827a', 74924, 3)
    for i in range(16):
        dot(im, i, 0, '#9a968e'); dot(im, 0, i, '#9a968e'); dot(im, i, 15, '#5e5a52'); dot(im, 15, i, '#5e5a52')
    for x, y in ((6, 7), (7, 7), (8, 8), (9, 8)):
        dot(im, x, y, '#76726a')   # where the feet have stood
    return im


def plaque_face():
    # A brass plate engraved with three lines of small capitals.
    im = new(BRASS, 74925, 3)
    frame(im, '#d4b46a', '#7a5e2a')
    for y, w in ((4, 10), (7, 12), (10, 8)):
        x0 = (16 - w) // 2
        for x in range(x0, x0 + w):
            if (x + y) % 3:
                dot(im, x, y, '#5a4420')
    return im


def camera_body():
    im = new('#3a3c40', 74926, 2)
    frame(im, '#55585c', '#24262a')
    return im


def camera_lens():
    im = new('#24262a', 74927, 1)
    for y in range(4, 12):
        for x in range(4, 12):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 < 14:
                dot(im, x, y, '#14161a')
    dot(im, 6, 6, '#6a7a8a'); dot(im, 12, 3, '#d43a2a')   # the glint, the small lamp
    return im


def screen_dark():
    im = new('#141618', 74928, 1)
    frame(im, '#2a2c2e', '#2a2c2e')
    return im


def screen_feed():
    # A grainy feed: a gallery seen from a corner, a doorway, a plinth and a figure on it. Four frames of grain, stacked.
    im = Image.new('RGBA', (16, 64), (0, 0, 0, 255))
    for f in range(4):
        rnd = random.Random(74930 + f)
        for y in range(16):
            for x in range(16):
                v = 52 + rnd.randint(-10, 10)
                if 6 <= x <= 8 and 2 <= y <= 11:
                    v -= 22                       # the doorway, darker
                if y >= 12:
                    v += 10                       # the floor, catching the screen's own glow
                if 11 <= x <= 13 and 9 <= y <= 11:
                    v += 18                       # a plinth
                if x == 12 and 3 <= y <= 8 or 11 <= x <= 13 and 5 <= y <= 7:
                    v += 30                       # the figure on it
                if y == (f * 5) % 16:
                    v += 14                       # the roll bar
                im.putpixel((x, y + f * 16), (v, v + 4, v, 255))
        for i in range(16):
            im.putpixel((i, f * 16), (30, 32, 30, 255)); im.putpixel((0, i + f * 16), (30, 32, 30, 255))
    return im


def desk_top():
    im = wood(74931, '#4a3424')
    for x in range(16):
        dot(im, x, 15, '#6a4c36')       # the scarred front edge
    return im


def log_open():
    # The log open on the desk: two ruled pages, nothing written.
    im = desk_top()
    fill(im, 2, 3, 14, 13, '#d8d2bc')
    for y in range(4, 13, 2):
        for x in range(3, 13):
            if x != 8:
                dot(im, x, y, '#a8a28c')
    for y in range(3, 13):
        dot(im, 8, y, '#8a846e')
    return im


def phone_top():
    im = desk_top()
    fill(im, 4, 5, 12, 12, '#1e1e20')
    fill(im, 3, 3, 13, 5, '#2a2a2c')
    for x, y in ((6, 7), (8, 7), (10, 7), (6, 9), (8, 9), (10, 9)):
        dot(im, x, y, '#8a8a84')
    return im


def chair_seat():
    im = new('#3a3a3e', 74932, 3)
    line(im, 4, 3, 11, 12, '#6a6a5e')     # the tape over the split
    line(im, 5, 3, 12, 12, '#7a7a6c')
    return im


CLOCK_TIMES = [(11, 52), (12, 0), (1, 0), (2, 0), (3, 0), (4, 0), (5, 0), (6, 0)]


def clock_face(hour, minute):
    im = new('#e2dccb', 74933, 2)
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d > 7.6:
                im.putpixel((x, y), (0, 0, 0, 0))
            elif d > 6.6:
                im.putpixel((x, y), shade('#3a3630', 0))
    for h in range(12):
        a = h / 12 * 2 * math.pi
        dot(im, round(cx + math.sin(a) * 5.6), round(cy - math.cos(a) * 5.6), '#2a2620')
    am = minute / 60 * 2 * math.pi
    ah = ((hour % 12) + minute / 60) / 12 * 2 * math.pi
    line(im, 7, 7, round(cx + math.sin(am) * 5), round(cy - math.cos(am) * 5), '#1a1612')
    line(im, 8, 8, round(cx + math.sin(ah) * 3), round(cy - math.cos(ah) * 3), '#1a1612')
    dot(im, 7, 7, '#8a1a1a'); dot(im, 8, 8, '#1a1612')
    return im


def timeclock_face():
    # A punch clock: a small dial at the top, the card slot below it, the maker's plate.
    im = steel(74934)
    fill(im, 4, 2, 12, 8, '#d8d2c0')
    line(im, 8, 5, 8, 3, '#1a1612'); line(im, 8, 5, 10, 5, '#1a1612')
    fill(im, 5, 10, 11, 11, '#141414')
    fill(im, 6, 13, 10, 14, BRASS)
    return im


def card_rack():
    im = steel(74935)
    for y in (2, 6, 10):
        fill(im, 2, y, 14, y + 3, '#d8d0b8')
        for x in range(3, 13):
            if x % 3:
                dot(im, x, y + 1, '#8a8270')
    return im


def notice():
    # A typed card on a cork square, a drawing pin in each top corner.
    im = new('#8a6a46', 74936, 4)
    fill(im, 2, 2, 14, 14, '#e2dcc6')
    for y, w in ((4, 8), (6, 10), (8, 9), (10, 10), (12, 5)):
        for x in range(3, 3 + w):
            if (x * 7 + y) % 5:
                dot(im, x, y, '#3a3630')
    dot(im, 3, 3, '#b03a2a'); dot(im, 12, 3, '#b03a2a')
    return im


def locker(open_=False, top=False):
    im = steel(74937 if top else 74938)
    if not open_:
        for y in (3, 4, 5) if top else ():
            for x in range(4, 12):
                if x % 2:
                    dot(im, x, y, '#4a4e52')        # vents
        if top:
            fill(im, 4, 9, 12, 12, '#d8d2bc')       # the label: NIGHTS
            for x in range(5, 11):
                dot(im, x, 10, '#2a2620')
        else:
            fill(im, 12, 2, 14, 5, '#3a3e42')       # the handle
    else:
        fill(im, 1, 1, 15, 15, '#1a1c1e')
        if top:
            line(im, 4, 2, 4, 8, '#8a8e92')         # the hook, and the flashlight's strap on it
            fill(im, 3, 8, 6, 14, '#2a2a2c')
    return im


def front_glass(upper, right):
    # Bronze-framed glass; the street behind it dark; a chain looped through the handles on the lower halves.
    im = new('#5a4a2a', 74940 + upper * 2 + right, 3)
    fill(im, 2, 2 if upper else 0, 14, 16 if upper else 14, '#101418')
    for y in range(2, 14):
        dot(im, 4, y, '#2a3036')
    if not upper:
        x = 1 if right else 14
        fill(im, x - 1 if right else x, 5, x + 1 if right else x + 2, 9, BRASS)
        for i in range(6):
            dot(im, (14 - i if right else i + 10), 7 + (i % 2), '#7a7a74')
    if upper:
        for x in range(4, 12):
            if x % 2:
                dot(im, x, 4, '#c8a858')            # gilt lettering, too small to read
    return im


def sign_half(right):
    im = new('#2a1a14', 74950 + right, 2)
    frame(im, BRASS, '#7a5e2a')
    letters = 'MERRI' if not right else "MAN'S"
    x = 2
    for ch in letters:
        for dy in range(4, 11):
            if (ord(ch) + dy) % 3:
                dot(im, x, dy, '#c8a858')
        x += 3
    return im


def booth_front():
    im = wood(74952, '#5a3e2a')
    fill(im, 1, 1, 15, 6, '#3a2818')
    fill(im, 5, 2, 11, 5, '#d8d0b8')      # the tickets on their spindle
    dot(im, 13, 3, BRASS)                   # the bell
    return im


def booth_glass():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 0, 16, 16, GLASS, 70)
    frame(im, '#5a3e2a', '#5a3e2a')
    for x in range(4, 12):
        dot(im, x, 3, '#c8a858')            # TICKETS
    fill(im, 6, 12, 10, 16, '#000000', 0)   # the gap at the counter
    return im


def tablecloth(quarter):
    im = new('#d6d0be', 74953 + quarter, 2)
    for i in range(16):
        dot(im, i, 15 if quarter >= 2 else 0, '#bab4a2')
    plates = {0: (5, 5), 1: (10, 5), 2: (5, 10), 3: (10, 10)}[quarter]
    x, y = plates
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            if dx * dx + dy * dy <= 5:
                dot(im, x + dx, y + dy, '#eae6da')
    dot(im, x, y, '#a4704a')                # the wax food
    dot(im, x + 1, y, '#c8b060')
    return im


def dresser(top):
    im = wood(74957 + top, '#5a4430')
    if top:
        for x0 in (1, 6, 11):
            for y in range(2, 8):
                for x in range(x0, x0 + 4):
                    if (x - x0 - 1.5) ** 2 + (y - 4.5) ** 2 < 5:
                        dot(im, x, y, '#dcd6c6')
        for x in range(16):
            dot(im, x, 9, '#3a2a1c')
        for x0 in (1, 6, 11):
            for y in range(11, 15):
                for x in range(x0, x0 + 4):
                    if (x - x0 - 1.5) ** 2 + (y - 12.5) ** 2 < 4:
                        dot(im, x, y, '#cec8b8')
    else:
        for y in (3, 9):
            for x in range(1, 15):
                dot(im, x, y, '#3a2a1c')
        dot(im, 7, 6, BRASS); dot(im, 7, 12, BRASS)
    return im


def stove():
    im = new('#2a2a2c', 74959, 3)
    frame(im, '#3e3e42', '#1a1a1c')
    fill(im, 3, 8, 13, 14, '#1e1e20')
    for x in range(4, 12):
        dot(im, x, 11, '#3a3a3c')
    dot(im, 12, 4, '#8a8a84')
    return im


def satin():
    im = new('#cfc4b0', 74960, 4)
    for y in range(0, 16, 3):
        for x in range(16):
            if (x + y) % 4 == 0:
                dot(im, x, y, '#b8ad98')
    return im


def gravestone():
    im = new('#6a6a64', 74961, 5)
    for x in range(3, 13):
        for y in (5, 8, 11):
            if (x + y) % 4:
                dot(im, x, y, '#56564f')
    fill(im, 2, 13, 6, 16, '#3a5a2a')        # the felt moss
    return im


def vat_wax():
    im = new('#c8b48a', 74962, 2)
    for x, y in ((6, 6), (7, 6), (6, 7), (7, 7), (8, 7), (7, 8)):
        dot(im, x, y, '#b09c72')             # the thumbprint
    return im


def bench_top(stage):
    im = wood(74963, '#6a5038')
    if stage == 0:
        line(im, 2, 3, 9, 3, '#9a9ea2'); line(im, 2, 6, 7, 6, '#9a9ea2'); line(im, 2, 9, 10, 9, '#8a7a5a')
        for x, y in ((11, 11), (13, 11), (12, 13), (14, 13)):
            dot(im, x, y, '#e8e8e0'); dot(im, x + 1, y, '#3a5a8a')  # the dish of eyes
    elif stage == 2:
        for x0 in (1, 5, 9, 13):
            fill(im, x0, 5, x0 + 2, 12, '#d8b49a')
            dot(im, x0, 4, '#d8b49a'); dot(im, x0 + 1, 3, '#d8b49a')
    return im


def heads_shelf(top):
    im = wood(74964 + top, '#4a3424')
    for x in range(16):
        dot(im, x, 13, '#2e2016'); dot(im, x, 14, '#5a4030')
    for x0 in (2, 7, 12):
        fill(im, x0, 5, x0 + 3, 12, '#d6b294')
        fill(im, x0, 4, x0 + 3, 6, '#3a2a1e' if (x0 + top) % 2 else '#7a5a3a')
        dot(im, x0, 8, '#2a2a2a'); dot(im, x0 + 2, 8, '#2a2a2a')
    return im


def head_wax():
    im = new('#d8b49a', 74966, 3)
    dot(im, 5, 6, '#a88a70'); dot(im, 10, 6, '#a88a70')
    return im


def figure_sheet():
    im = new('#cac4b4', 74967, 3)
    for y in range(16):
        dot(im, 4, y, '#b4ae9e'); dot(im, 11, y, '#b4ae9e')
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


def plinth_model():
    return model([element([1, 0, 1], [15, 6, 15], {'all': 's', 'up': 't', 'down': 't'})], {'s': 'plinth_side', 't': 'plinth_top'}, 'plinth_side', False)


def plaque_model():
    return model([element([7.5, 0, 7.5], [8.5, 6, 8.5], {'all': 'b'}),
                  element([5, 6, 6.5], [11, 9, 8.5], {'all': 'b', 'north': 'p'}, rotation={'origin': [8, 6, 8], 'axis': 'x', 'angle': -22.5})],
                 {'b': 'brass', 'p': 'plaque'}, 'brass')


def rope_model(stage):
    e = [element([6.5, 0, 6.5], [9.5, 1, 9.5], {'all': 'b'}), element([7.5, 1, 7.5], [8.5, 14, 8.5], {'all': 'b'}),
         element([7, 14, 7], [9, 15, 9], {'all': 'b'})]
    if stage == 0:
        # The rope runs north to the next stanchion, sagging in the middle of the run.
        e += [element([7.5, 11, -4], [8.5, 12, 7.5], {'all': 'v'}, rotation={'origin': [8, 12, 7.5], 'axis': 'x', 'angle': -22.5}),
              element([7.5, 9.5, -12], [8.5, 10.5, -4], {'all': 'v'}),
              element([7.5, 11, -16], [8.5, 12, -12], {'all': 'v'}, rotation={'origin': [8, 11.5, -12], 'axis': 'x', 'angle': 22.5})]
    return model(e, {'b': 'brass', 'v': 'velvet'}, 'brass')


def camera_model():
    return model([element([6, 13, 13], [10, 15, 16], {'all': 'c'}),
                  element([5, 10, 5], [11, 14, 14], {'all': 'c', 'north': 'l'}, rotation={'origin': [8, 12, 12], 'axis': 'x', 'angle': 22.5})],
                 {'c': 'camera', 'l': 'lens'}, 'camera')


def monitor_model(stage):
    face = 'screen_feed' if stage else 'screen_dark'
    return model([element([1, -3, 5], [15, 10, 15], {'all': 'h', 'north': 'f'}, uv={'north': [0, 0, 16, 16]}),
                  element([3, -3, 15], [13, 8, 16], {'all': 'h'})], {'h': 'monitor_case', 'f': face}, 'monitor_case', False)


def desk_model(stage):
    top = ['desk_top', 'log_open', 'phone_top'][stage]
    e = [element([0, 12, 0], [16, 13, 16], {'all': 'w', 'up': 't'}), element([0, 0, 1], [16, 12, 16], {'all': 'w'})]
    if stage == 2:
        e.append(element([4, 13, 4], [12, 15, 12], {'all': 'k', 'up': 't'}))
    if stage == 1:
        e.append(element([2, 13, 3], [14, 13.5, 13], {'up': 't', 'north': 'p', 'south': 'p', 'east': 'p', 'west': 'p'}))
    return model(e, {'w': 'desk_side', 't': top, 'k': 'phone_body', 'p': 'paper_edge'}, 'desk_side', False)


def chair_model():
    t = {'s': 'chair_seat', 'f': 'steel'}
    return model([element([7, 0, 7], [9, 9, 9], {'all': 'f'}), element([3, 0, 7.5], [13, 1, 8.5], {'all': 'f'}), element([7.5, 0, 3], [8.5, 1, 13], {'all': 'f'}),
                  element([2, 9, 2], [14, 10, 14], {'all': 's'}), element([2, 10, 12.5], [14, 16, 14], {'all': 's'})], t, 'chair_seat')


def clock_model(stage):
    return model([element([2, 2, 15], [14, 14, 16], {'north': f'clock_{stage}', 'south': f'clock_{stage}'}, uv={'north': [2, 2, 14, 14], 'south': [2, 2, 14, 14]})],
                 {f'clock_{stage}': f'clock_{stage}'}, f'clock_{stage}')


def timeclock_model():
    return model([element([4, 2, 11], [12, 15, 16], {'all': 's', 'north': 'f'}),
                  element([3, 1, 14], [13, 14, 16], {'north': 'r'}, uv={'north': [0, 0, 16, 16]})],
                 {'s': 'steel', 'f': 'timeclock', 'r': 'card_rack'}, 'timeclock')


def notice_model():
    return model([element([3, 3, 15], [13, 13, 16], {'all': 'n'}, uv={'north': [2, 2, 14, 14]})], {'n': 'notice'}, 'notice', False)


def locker_model(top, stage):
    face = f"locker_{'top' if top else 'low'}_{'open' if stage else 'shut'}"
    e = [element([1, 0, 4], [15, 16, 16], {'all': 's', 'north': 'f'})]
    if stage:
        e.append(element([1, 0, -9], [2, 16, 4], {'all': 's'}))   # the door, swung out
    return model(e, {'s': 'steel', 'f': face}, 'steel', False)


def front_model(stage):
    upper, right = stage % 2, stage // 2
    return model([element([0, 0, 6], [16, 16, 10], {'all': 'b', 'north': 'g', 'south': 'g'})],
                 {'b': 'bronze', 'g': f"front_{'upper' if upper else 'lower'}_{'right' if right else 'left'}"}, 'bronze', False)


def sign_model(stage):
    return model([element([0, 2, 13], [16, 14, 16], {'all': 'w', 'north': 's'}, uv={'north': [0, 2, 16, 14]})],
                 {'w': 'desk_side', 's': f'sign_{stage}'}, 'desk_side', False)


def booth_model():
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'west': 'f', 'north': 'f'}, uv={'west': [0, 0, 16, 16], 'north': [0, 0, 16, 16]})],
                 {'w': 'booth_wood', 'f': 'booth_front'}, 'booth_wood', False)


def booth_top_model():
    return model([element([0, 0, 7], [16, 16, 9], {'north': 'g', 'south': 'g', 'up': 'w', 'east': 'w', 'west': 'w'})], {'g': 'booth_glass', 'w': 'booth_wood'}, 'booth_wood')


def table_model(stage):
    lx = 1 if stage in (0, 2) else 13
    lz = 1 if stage in (0, 1) else 13
    return model([element([0, 11, 0], [16, 12, 16], {'all': 'c', 'up': f'cloth_{stage}'}), element([lx, 0, lz], [lx + 2, 11, lz + 2], {'all': 'w'})],
                 {'c': 'cloth_0', f'cloth_{stage}': f'cloth_{stage}', 'w': 'desk_side'}, 'desk_side')


def dresser_model(stage):
    if stage == 0:
        return model([element([0, 0, 4], [16, 16, 16], {'all': 'w', 'north': 'f'})], {'w': 'desk_side', 'f': 'dresser_low'}, 'desk_side', False)
    return model([element([0, 0, 12], [16, 16, 16], {'all': 'w', 'north': 'f'}), element([0, 0, 8], [16, 1, 12], {'all': 'w'}),
                  element([0, 15, 8], [16, 16, 12], {'all': 'w'})], {'w': 'desk_side', 'f': 'dresser_high'}, 'desk_side')


def stove_model():
    return model([element([1, 0, 2], [15, 13, 16], {'all': 's', 'north': 'f'}), element([5, 13, 7], [11, 15, 11], {'all': 's'}),
                  element([10, 15, 8], [11, 16, 9], {'all': 's'})], {'s': 'stove_iron', 'f': 'stove'}, 'stove_iron', False)


def coffin_model(foot):
    e = [element([1, 0, 1], [15, 1, 15], {'all': 'w'}), element([1, 1, 1], [2, 6, 15], {'all': 'w'}), element([14, 1, 1], [15, 6, 15], {'all': 'w'}),
         element([2, 1, 2], [14, 3, 14], {'up': 's'})]
    e.append(element([1, 1, 14 if foot else 1], [15, 6, 15 if foot else 2], {'all': 'w'}))
    return model(e, {'w': 'coffin_wood', 's': 'satin'}, 'coffin_wood')


def gravestone_model():
    return model([element([3, 0, 7], [13, 13, 9], {'all': 'g'}), element([4, 13, 7], [12, 15, 9], {'all': 'g'})], {'g': 'gravestone'}, 'gravestone', False)


def vat_model():
    return model([element([1, 0, 1], [15, 12, 15], {'all': 's', 'up': 'x'}), element([0, 12, 0], [16, 13, 16], {'north': 's', 'south': 's', 'east': 's', 'west': 's'}),
                  element([2, 11.5, 2], [14, 12, 14], {'up': 'x'})], {'s': 'steel', 'x': 'vat_wax'}, 'steel', False)


def bench_model(stage):
    e = [element([0, 13, 0], [16, 14, 16], {'all': 'w', 'up': f'bench_{stage}'}), element([0, 0, 1], [2, 13, 3], {'all': 'w'}),
         element([14, 0, 1], [16, 13, 3], {'all': 'w'}), element([0, 0, 13], [2, 13, 15], {'all': 'w'}), element([14, 0, 13], [16, 13, 15], {'all': 'w'})]
    if stage == 1:
        e += [element([7, 14, 7], [9, 17, 9], {'all': 'w'}), element([5, 17, 5], [11, 24, 11], {'all': 'h'})]
    return model(e, {'w': 'bench_wood', f'bench_{stage}': f'bench_{stage}', 'h': 'head_wax'}, 'bench_wood', False)


def heads_model(stage):
    return model([element([0, 0, 12], [16, 16, 16], {'all': 'w', 'north': 'f'}), element([0, 0, 6], [16, 1, 12], {'all': 'w'}),
                  element([0, 15, 6], [16, 16, 12], {'all': 'w'})], {'w': 'desk_side', 'f': f'heads_{stage}'}, 'desk_side')


def fitting_models():
    m = {'plinth_0': plinth_model(), 'plaque_0': plaque_model(), 'camera_0': camera_model(), 'chair_0': chair_model(),
         'timeclock_0': timeclock_model(), 'notice_0': notice_model(), 'booth_0': booth_model(), 'booth_top_0': booth_top_model(),
         'stove_0': stove_model(), 'coffin_0': coffin_model(False), 'coffin_foot_0': coffin_model(True), 'gravestone_0': gravestone_model(),
         'vat_0': vat_model()}
    for s in range(2):
        m[f'rope_{s}'] = rope_model(s); m[f'monitor_{s}'] = monitor_model(s); m[f'locker_{s}'] = locker_model(False, s)
        m[f'locker_top_{s}'] = locker_model(True, s); m[f'sign_{s}'] = sign_model(s); m[f'dresser_{s}'] = dresser_model(s)
        m[f'heads_{s}'] = heads_model(s)
    for s in range(3):
        m[f'desk_{s}'] = desk_model(s); m[f'bench_{s}'] = bench_model(s)
    for s in range(8):
        m[f'clock_{s}'] = clock_model(s)
    for s in range(4):
        m[f'front_{s}'] = front_model(s); m[f'table_{s}'] = table_model(s)
    return m


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/waxworks_{name}.png'] = im
    faces = {
        'plinth_side': plinth_side(), 'plinth_top': plinth_top(), 'brass': brass(), 'plaque': plaque_face(), 'velvet': velvet(),
        'camera': camera_body(), 'lens': camera_lens(), 'monitor_case': steel(74970), 'screen_dark': screen_dark(), 'screen_feed': screen_feed(),
        'desk_side': wood(74971, '#4a3424'), 'desk_top': desk_top(), 'log_open': log_open(), 'phone_top': phone_top(), 'phone_body': new('#1e1e20', 74972, 2),
        'paper_edge': new('#d8d2bc', 74973, 2), 'chair_seat': chair_seat(), 'steel': steel(), 'timeclock': timeclock_face(), 'card_rack': card_rack(),
        'notice': notice(), 'bronze': new('#5a4a2a', 74974, 3), 'booth_wood': wood(74975), 'booth_front': booth_front(), 'booth_glass': booth_glass(),
        'dresser_low': dresser(0), 'dresser_high': dresser(1), 'stove': stove(), 'stove_iron': new('#2a2a2c', 74976, 3),
        'coffin_wood': wood(74977, '#3a2618'), 'satin': satin(), 'gravestone': gravestone(), 'vat_wax': vat_wax(), 'bench_wood': wood(74978, '#6a5038'),
        'head_wax': head_wax(), 'sheet': figure_sheet(),
    }
    for s, (h, mi) in enumerate(CLOCK_TIMES):
        faces[f'clock_{s}'] = clock_face(h, mi)
    for top in (False, True):
        for open_ in (False, True):
            faces[f"locker_{'top' if top else 'low'}_{'open' if open_ else 'shut'}"] = locker(open_, top)
    for upper in (0, 1):
        for right in (0, 1):
            faces[f"front_{'upper' if upper else 'lower'}_{'right' if right else 'left'}"] = front_glass(upper, right)
    for s in range(2):
        faces[f'sign_{s}'] = sign_half(s); faces[f'heads_{s}'] = heads_shelf(s)
    for s in range(4):
        faces[f'cloth_{s}'] = tablecloth(s)
    for s in range(3):
        faces[f'bench_{s}'] = bench_top(s)
    for name, im in faces.items():
        out[f'block/waxworks/{name}.png'] = im
    return out


KINDS = ['plinth', 'plaque', 'rope', 'camera', 'monitor', 'desk', 'chair', 'clock', 'timeclock', 'notice', 'locker', 'locker_top', 'front', 'sign',
         'booth', 'booth_top', 'table', 'dresser', 'stove', 'coffin', 'coffin_foot', 'gravestone', 'vat', 'bench', 'heads']
STAGES = {'rope': 2, 'monitor': 2, 'desk': 3, 'clock': 8, 'locker': 2, 'locker_top': 2, 'front': 4, 'sign': 2, 'table': 4, 'dresser': 2, 'bench': 3, 'heads': 2}
STAGE_MAX = 8
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'waxworks_fitting': 'Museum Fitting', 'waxworks_block': 'Painted Block', 'waxworks_lino': 'Staff Linoleum', 'waxworks_tile': 'Ceiling Tile',
         'waxworks_panel': 'Gallery Panelling', 'waxworks_paper': 'Gallery Wallpaper', 'waxworks_parquet': 'Gallery Parquet', 'waxworks_runner': 'Corridor Runner',
         'waxworks_plaster': 'Gallery Ceiling', 'waxworks_brick': 'Museum Brick'}
ANIMATED = {'textures/block/waxworks/screen_feed.png.mcmeta': {'animation': {'frametime': 3}}}


def files():
    out = {}
    for name in materials():
        out[f'models/block/waxworks_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/waxworks_{name}'}}
        out[f'blockstates/waxworks_{name}.json'] = {'variants': {'': {'model': NS + f'block/waxworks_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/waxworks/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/waxworks/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/waxworks_fitting.json'] = {'variants': variants}
    out.update(ANIMATED)
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
                assert f'block/waxworks/{tex[len(P):]}.png' in images, (key, tex)
    # A screen that is on is brighter than one that is off; the clock's hands move between its first face and its last.
    def mean(im, box=(2, 2, 14, 14)):
        px = [im.getpixel((x, y)) for y in range(box[1], box[3]) for x in range(box[0], box[2])]
        return sum(sum(p[:3]) for p in px) / len(px)
    assert mean(images['block/waxworks/screen_feed.png']) > mean(images['block/waxworks/screen_dark.png']) + 60
    assert images['block/waxworks/clock_0.png'].tobytes() != images['block/waxworks/clock_7.png'].tobytes()
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
        print(f'Drew the waxworks under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the waxworks: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
