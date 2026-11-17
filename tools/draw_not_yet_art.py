"""Not Yet (0.4.663): the ward deeper in the same machine as the Kept, where it mends what it breaks.

Draws, in native Minecraft pixels from fixed seeds: three materials (the ward's tiled floor, its painted wall and the darker gloss of
the dado under it; the machine's own casing, grating and cable ceiling are the Kept's); and every fitting of the not_yet_fitting
block, its models facing north and turned by its facing:

- COT and COT_FOOT: the two cells of a ward cot (its head in the cell it stands in, its foot in the cell it faces): a frame of steel
  tube, a thin ticked mattress, a pillow, a blanket, and a drawer under the head. Stage 0 as it is; 1 heating, the coils under the
  ticking showing through it; 2 made, the blanket pulled tight and the pillow squared.
- CURTAIN: a curtain on a rail of cable, two cells high: the rail alone (0), the rail with the curtain bunched under it at one end
  (1) and the bunch's lower half (2), the rail with the curtain drawn across (3) and its lower half (4).
- LAMP: a lamp on a jointed arm from a plate on the wall, off (0) or on (1).
- SCREEN: a screen in a steel housing on the wall: dark (0); the machine's two words (1); the far door standing open (2); the far door
  open with somebody standing in it (3); noise (4).
- COUNTER: one window of the wall of turning digits; its digit is drawn by each reader's own game.
- TABLE and TABLE_FOOT: the mending table, steel on a pedestal, with straps.
- ARM: one of the ring of arms over the table: folded up (0), let down (1), working (2).
- HATCH: a plate of the passage floor: shut (0), or open, its leaves hanging into the shaft (1).
- VENT: a grille in the wall: still (0), or letting in cold, rimed over (1).
- WINDOW: the long window's painted outdoors: sky (0), the three parts of the one cloud it knows (1-3), the four quarters of a sun
  that does not move (4-7).
- DOOR: the far door, a steel leaf sliding in its frame: lower and upper shut (0, 1), lower and upper open (2, 3).
- CHART: the ward's chart on its board by the door (the source).

Writes the models and block states and the blocks' names. Also the seams a mending leaves (an overlay drawn on a reader's skin) and the
counter's digits in the three kinds of counter (drums, flaps and segments), which each reader's game draws. --check verifies everything.
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
P = NS + 'block/not_yet/'
KEPT = NS + 'block/kept/'


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


# ------------------------------------------------------------------------------------------------ materials
def tile():
    # Square tiles, two to a block, with grout between. The machine laid them from a picture of a ward: every grout line is true but
    # one, a pixel out, and the wear is in the same place on every tile.
    im = new('#b4b8ad', 66301, 3)
    for i in range(16):
        dot(im, i, 0, '#8d9188'); dot(im, 0, i, '#8d9188'); dot(im, i, 8, '#8d9188')
        dot(im, 8 if i < 8 else 9, i, '#8d9188')
    for ox, oy in ((1, 1), (9, 1), (1, 9), (10, 9)):
        dot(im, ox + 4, oy + 5, '#a2a69b'); dot(im, ox + 5, oy + 5, '#a2a69b'); dot(im, ox + 5, oy + 4, '#a9ada2')
        dot(im, ox, oy, '#c4c8bd')
    return im


def wall():
    # Painted plaster, rollered: faint vertical bands, and a little of the casing showing at one seam where the paint has not taken.
    im = new('#a9ae9f', 66302, 2)
    for x in (3, 4, 10, 11):
        for y in range(16):
            dot(im, x, y, shade(im.getpixel((x, y)), -3))
    for y in range(6, 10):
        dot(im, 14, y, '#868a7e')
    dot(im, 14, 8, '#5a5e5a')
    return im


def dado():
    # The same wall to waist height, in a darker gloss that shows every scuff, with a rail along its top.
    im = new('#6c776e', 66303, 3)
    for x in range(16):
        dot(im, x, 0, '#8a948a'); dot(im, x, 1, '#4c564e'); dot(im, x, 15, '#4a544c')
    rnd = random.Random(66304)
    for _ in range(4):
        x, y = rnd.randrange(2, 15), rnd.randrange(4, 14)
        dot(im, x, y, '#5a645c'); dot(im, x + 1, y, '#5f6961')
    for y in (5, 11):
        dot(im, 2, y, '#7e887e')
    return im


def materials():
    return {'tile': tile(), 'wall': wall(), 'dado': dado()}


# ------------------------------------------------------------------------------------------------ the cot
STEEL = '#8e9396'


def tube():
    # Steel tube: a lit edge, the round of it, a dark edge.
    im = new(STEEL, 66310, 2)
    for i in range(16):
        dot(im, i, 0, '#b8bdbf'); dot(im, 0, i, '#b8bdbf'); dot(im, i, 15, '#5e6366'); dot(im, 15, i, '#5e6366')
    for i in range(1, 15):
        dot(im, i, 4, '#a4a9ac'); dot(im, i, 11, '#767b7e')
    return im


def tube_hot():
    # The same tube with the heat in it: the steel gone a dull cherry, the lit edge orange, the round of it brightest at the middle.
    im = new('#8e4a38', 66317, 2)
    for i in range(16):
        dot(im, i, 0, '#d88a5a'); dot(im, 0, i, '#d88a5a'); dot(im, i, 15, '#4a2820'); dot(im, 15, i, '#4a2820')
    for i in range(1, 15):
        dot(im, i, 4, '#c26a44'); dot(im, i, 7, '#e0904e'); dot(im, i, 8, '#d07c48'); dot(im, i, 11, '#6e3a2c')
    return im


def ticking(hot=False):
    # Mattress ticking: narrow stripes down its length. Heating, the coils under it show through as lines that do not follow the
    # stripes, darkest where they cross.
    im = new('#c9c4b2', 66311, 2)
    for x in (2, 3, 7, 8, 12, 13):
        for y in range(16):
            dot(im, x, y, '#8d93a0' if x in (2, 7, 12) else '#a6abb4')
    if hot:
        # the coils, one every four pixels across the stripes, dull where they show through and brighter at their middle
        def toward(x, y, c, t):
            r, g, b, a = im.getpixel((x, y)); cr, cg, cb = rgb(c)
            im.putpixel((x, y), (round(r + (cr - r) * t), round(g + (cg - g) * t), round(b + (cb - b) * t), 255))
        for y in (3, 7, 11, 15):
            for x in range(16):
                toward(x, y, '#c8642c', .55)
                if 0 < y - 1:
                    toward(x, y - 1, '#b88466', .22)
                toward(x, (y + 1) % 16, '#b88466', .22)
    return im


def ticking_side(hot=False):
    im = new('#bfb9a7', 66312, 2)
    for x in range(16):
        dot(im, x, 0, '#d8d3c2'); dot(im, x, 15, '#9a9484')
    for x in (2, 7, 12):
        for y in range(1, 15):
            dot(im, x, y, '#8d93a0')
    if hot:
        # The coils' ends glowing through the welt every four pixels, and the cloth over them scorched brown toward the bottom.
        for y in range(8, 16):
            t = (y - 7) / 9
            for x in range(16):
                r, g, b, a = im.getpixel((x, y))
                im.putpixel((x, y), (round(r + (150 - r) * t), round(g + (92 - g) * t), round(b + (60 - b) * t), 255))
        for x in (1, 5, 9, 13):
            fill(im, x, 11, x + 2, 15, '#e0783a'); dot(im, x, 12, '#f6b060'); dot(im, x + 1, 12, '#f6b060')
            dot(im, x, 10, '#a4583a'); dot(im, x + 1, 10, '#a4583a')
    return im


def blanket(made=False):
    # Grey wool, a darker band near the turned edge. Made, it is drawn smooth and tight, a crease down the middle where it was folded.
    im = new('#8a8c86', 66313 if not made else 66314, 3 if not made else 1)
    for x in range(16):
        dot(im, x, 2, '#6e706a'); dot(im, x, 3, '#6e706a')
    if made:
        for y in range(16):
            dot(im, 8, y, '#7c7e78')
        for x in range(16):
            dot(im, x, 0, '#a2a49e'); dot(im, x, 1, '#9a9c96')
    else:
        rnd = random.Random(66315)
        for _ in range(5):
            x, y = rnd.randrange(16), rnd.randrange(5, 16)
            line(im, x, y, min(15, x + 3), y + 1, '#7a7c76')
    return im


def blanket_hot():
    # The grey wool with the coils under it showing through: a dull orange line across every four pixels, singed brown along it.
    im = blanket()
    def toward(x, y, c, t):
        r, g, b, a = im.getpixel((x, y)); cr, cg, cb = rgb(c)
        im.putpixel((x, y), (round(r + (cr - r) * t), round(g + (cg - g) * t), round(b + (cb - b) * t), 255))
    rnd = random.Random(66318)
    for y in (5, 9, 13):
        for x in range(16):
            toward(x, y, '#d8743a', .62); toward(x, y - 1, '#8a5a40', .3); toward(x, y + 1, '#8a5a40', .3)
            if rnd.random() < .25:
                toward(x, y + (1 if rnd.random() < .5 else -1), '#5a3a2c', .45)
    return im


def pillow(made=False):
    im = new('#d4d0c4', 66316, 2)
    for i in range(16):
        dot(im, i, 0, '#e4e0d6'); dot(im, i, 15, '#a8a498'); dot(im, 0, i, '#c4c0b4'); dot(im, 15, i, '#b2aea2')
    if not made:
        line(im, 4, 6, 10, 9, '#b8b4a8'); line(im, 6, 11, 12, 8, '#bcb8ac')
    return im


def drawer():
    # The drawer under the cot's head: a steel front with a recessed pull and a card slot, empty.
    im = new('#7d8286', 66317, 2)
    for i in range(16):
        dot(im, i, 0, '#9ca1a4'); dot(im, 0, i, '#9ca1a4'); dot(im, i, 15, '#53585b'); dot(im, 15, i, '#53585b')
    fill(im, 5, 6, 11, 9, '#3c4043'); fill(im, 5, 6, 11, 7, '#2c3033')
    fill(im, 3, 2, 7, 4, '#5f6467'); dot(im, 3, 2, '#c8c2b0'); dot(im, 4, 2, '#c8c2b0')
    return im


def chart_card():
    # The card on the cot's footboard: a printed form, a line written in, mostly blank.
    im = new('#d8d2c0', 66318, 1)
    for i in range(16):
        dot(im, i, 0, '#8a8478'); dot(im, i, 15, '#8a8478'); dot(im, 0, i, '#8a8478'); dot(im, 15, i, '#8a8478')
    for y in (4, 7, 10, 13):
        line(im, 2, y, 13, y, '#a8a294')
    line(im, 2, 3, 8, 3, '#3a3c40')
    fill(im, 6, 0, 10, 2, '#5e6366')
    return im


# ------------------------------------------------------------------------------------------------ curtains, lamps, screens
def curtain():
    # Heavy cotton gone limp, vertical folds lit and shadowed in turn.
    im = new('#9aa49c', 66320, 2)
    for x in range(16):
        k = (0, 6, 10, 6, 0, -8, -12, -8)[x % 8]
        for y in range(16):
            r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), 255))
    for x in range(16):
        dot(im, x, 15, '#6c766e')
    return im


def ring_tex():
    im = new('#5e6366', 66321, 2)
    for i in range(16):
        dot(im, i, 0, '#8a8f92'); dot(im, i, 15, '#3e4245')
    return im


def lamp_steel():
    im = new('#6e7376', 66322, 2)
    for i in range(16):
        dot(im, i, 0, '#9a9fa2'); dot(im, 0, i, '#9a9fa2'); dot(im, i, 15, '#46494c'); dot(im, 15, i, '#46494c')
    return im


def shade_tex():
    # The shade: enamel, chipped to the iron at its rim.
    im = new('#c6c8be', 66323, 2)
    for x in range(16):
        dot(im, x, 15, '#6a6e70'); dot(im, x, 14, '#a8aaa0')
    for x in (3, 9, 12):
        dot(im, x, 14, '#3e4245')
    return im


def bulb(on):
    im = new('#fff4cc' if on else '#8a8678', 66324 if on else 66325, 2)
    if on:
        fill(im, 5, 5, 11, 11, '#ffffff')
    return im


def housing():
    im = new('#4a4f53', 66326, 2)
    for i in range(16):
        dot(im, i, 0, '#62676b'); dot(im, 0, i, '#62676b'); dot(im, i, 15, '#2e3236'); dot(im, 15, i, '#2e3236')
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        dot(im, x, y, '#7a7f83')
    return im


def screen_frame(im):
    for i in range(16):
        for k in (0, 1):
            dot(im, i, k, '#33373a'); dot(im, i, 15 - k, '#26292c'); dot(im, k, i, '#33373a'); dot(im, 15 - k, i, '#26292c')


def screen_dark():
    im = new('#141a17', 66330, 1); screen_frame(im)
    for k in range(3):
        dot(im, 4 + k, 3, '#26302a'); dot(im, 3, 4 + k, '#26302a')
    return im


# The machine's hand: square letters, every stroke the same weight, as if set by something that has never held a pen.
GLYPHS = {
    'N': ['#..#', '##.#', '#.##', '#..#', '#..#'],
    'O': ['.##.', '#..#', '#..#', '#..#', '.##.'],
    'T': ['###', '.#.', '.#.', '.#.', '.#.'],
    'Y': ['#.#', '#.#', '.#.', '.#.', '.#.'],
    'E': ['###', '#..', '##.', '#..', '###'],
}


def word(im, text, x, y, colour):
    for ch in text:
        g = GLYPHS[ch]
        for j, row in enumerate(g):
            for i, c in enumerate(row):
                if c == '#':
                    dot(im, x + i, y + j, colour)
        x += len(g[0]) + 1


def screen_words():
    # NOT on the first line, YET on the second, glowing, with the scan line rolling down: two frames.
    frames = []
    for f in range(2):
        im = new('#0f1512', 66331 + f, 1); screen_frame(im)
        word(im, 'NOT', 3, 3, '#e8f0e6'); word(im, 'YET', 3, 9, '#e8f0e6')
        for x in range(2, 14):
            y = 5 + f * 5
            r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (min(255, r + 26), min(255, g + 30), min(255, b + 26), 255))
        frames.append(im)
    sheet = Image.new('RGBA', (16, 32)); sheet.paste(frames[0], (0, 0)); sheet.paste(frames[1], (0, 16))
    return sheet


def door_picture(figure):
    # What the screen says the far door is doing: a corridor's end, the door slid open, light beyond it. With somebody in it.
    im = new('#18201c', 66333 + figure, 1); screen_frame(im)
    fill(im, 3, 3, 13, 13, '#26302b')
    for y in range(3, 13):
        dot(im, 3, y, '#1c2420'); dot(im, 12, y, '#1c2420')
    fill(im, 6, 4, 10, 12, '#c8d2c4'); fill(im, 6, 4, 10, 5, '#a8b4a6')
    line(im, 4, 12, 6, 11, '#3a463e'); line(im, 11, 12, 9, 11, '#3a463e')
    if figure:
        fill(im, 7, 6, 9, 12, '#1a201c'); fill(im, 7, 5, 9, 6, '#1a201c'); dot(im, 6, 8, '#1a201c'); dot(im, 9, 8, '#1a201c')
    return im


def screen_static():
    frames = []
    for f in range(4):
        im = new('#2a302c', 66340 + f, 1); rnd = random.Random(66345 + f)
        for y in range(2, 14):
            for x in range(2, 14):
                v = rnd.choice((40, 70, 110, 150, 190))
                im.putpixel((x, y), (v, v + 6, v, 255))
        screen_frame(im); frames.append(im)
    sheet = Image.new('RGBA', (16, 64))
    for f, im in enumerate(frames):
        sheet.paste(im, (0, f * 16))
    return sheet


def counter_frame():
    # One window of the counter: riveted steel round a deep slot, where each reader's game turns the digit.
    im = new('#3d4246', 66350, 2)
    for i in range(16):
        dot(im, i, 0, '#565b60'); dot(im, 0, i, '#565b60'); dot(im, i, 15, '#24282b'); dot(im, 15, i, '#24282b')
    fill(im, 2, 1, 14, 15, '#0c0e10')
    for i in range(2, 14):
        dot(im, i, 1, '#1c1f22')
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        dot(im, x, y, '#6e7378')
    return im


# ------------------------------------------------------------------------------------------------ the mending table and its arms
def table_steel():
    # Brushed steel with a channel worn down its middle, and a darker stain where the channel runs off.
    im = new('#9ea4a6', 66360, 2)
    for y in range(16):
        dot(im, 7, y, '#7a8082'); dot(im, 8, y, '#7a8082')
    for x in range(16):
        if x % 3 == 0:
            for y in range(16):
                r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (min(255, r + 4), min(255, g + 4), min(255, b + 4), 255))
    dot(im, 7, 15, '#5a5450'); dot(im, 8, 15, '#5a5450'); dot(im, 8, 14, '#6a6460')
    return im


def strap():
    im = new('#4a3c32', 66361, 3)
    for x in range(16):
        dot(im, x, 0, '#5e4e42'); dot(im, x, 15, '#33281f')
    fill(im, 6, 4, 10, 12, '#8e9396'); fill(im, 7, 5, 9, 11, '#4a3c32')
    return im


def arm_steel():
    im = new('#5f6569', 66362, 2)
    for i in range(16):
        dot(im, i, 0, '#868c90'); dot(im, 0, i, '#868c90'); dot(im, i, 15, '#3b3f42'); dot(im, 15, i, '#3b3f42')
    for i in range(3, 13, 4):
        dot(im, i, 8, '#2c3033'); dot(im, 8, i, '#2c3033')
    return im


def tool_tex():
    # The working end: bright where it has been used, a thread run back from its eye.
    im = new('#c4cacc', 66363, 2)
    for i in range(16):
        dot(im, i, 15, '#7c8284')
    line(im, 2, 2, 13, 13, '#e8ecec'); line(im, 3, 12, 12, 3, '#3a3634')
    return im


# ------------------------------------------------------------------------------------------------ hatch, vent, window, door, chart
def hatch_plate():
    # A floor plate cut to the grating's own pattern, so it is hard to see until it opens: only its seam and its hinge pins tell.
    im = new('#16181b', 66370, 2)
    for i in range(16):
        for k in (0, 4, 8, 12):
            dot(im, i, k, '#4a4e54'); dot(im, k, i, '#3e4248'); dot(im, i, k + 1, '#2e3136')
    for i in range(16):
        dot(im, 15, i, '#08090a')
    dot(im, 1, 2, '#6e7278'); dot(im, 1, 13, '#6e7278')
    return im


def hatch_leaf():
    im = new('#2c3034', 66371, 2)
    for i in range(16):
        dot(im, i, 0, '#4a4e54'); dot(im, i, 15, '#16181b')
    for y in (4, 8, 12):
        for x in range(16):
            dot(im, x, y, '#3e4248')
    return im


def vent(frost):
    # A floor grille: a cast frame and six dark slots between the bars. Rimed, the frost grows on the bars and the frame, white where it
    # is thick, a grey-blue fur of needles reaching out into the slots, heaviest along the bottom and in the corners where the cold
    # comes in; the slots stay dark between, so it is still a grille.
    im = new('#3a3e42', 66372, 2)
    for i in range(16):
        dot(im, i, 0, '#54595d'); dot(im, 0, i, '#54595d'); dot(im, i, 15, '#222528'); dot(im, 15, i, '#222528')
    for y in range(3, 14, 2):
        fill(im, 2, y, 14, y + 1, '#0e1012')
    if frost:
        rnd = random.Random(66374)
        def rime(x, y, v):
            im.putpixel((x, y), (v - 10, v - 2, v + 8 if v < 248 else 255, 255))
        def weight(x, y):
            # thicker toward the bottom and the two sides, thickest in the corners
            edge = max(0, 3 - min(x, 15 - x)) / 3
            return min(1, .08 + .42 * (y / 15) ** 2 + .3 * edge * edge + (.2 if y >= 13 else 0))
        bars = [y for y in range(16) if y not in range(3, 14, 2)]
        for y in bars:
            for x in range(16):
                w = weight(x, y)
                if rnd.random() < w:
                    rime(x, y, rnd.choice((214, 228, 240)))
                elif rnd.random() < w * .6:
                    rime(x, y, rnd.choice((112, 128)))
        for y in range(3, 14, 2):
            for x in range(2, 14):
                w = weight(x, y)
                # needles growing down from the bar above and up from the bar below; never across the whole slot
                if rnd.random() < w * .3:
                    rime(x, y, rnd.choice((84, 104, 128)))
        # the bottom rows and the corners all but white
        for x in range(16):
            rime(x, 15, rnd.choice((222, 236)))
            if rnd.random() < .7:
                rime(x, 14, rnd.choice((206, 222, 236)))
        for cx, cy in ((1, 1), (14, 1), (1, 13), (14, 13)):
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    if rnd.random() < (.75 if cy > 7 else .4):
                        rime(min(15, max(0, cx + dx)), min(15, max(0, cy + dy)), rnd.choice((220, 238, 248)))
    return im


SKY = ['#7aa4c8', '#82abcd', '#8bb2d1', '#94b9d5']


def sky(seed):
    # The sky in four flat bands, top to bottom, not blended: as if painted from a description. A few strokes of the brush show.
    im = Image.new('RGBA', (16, 16)); rnd = random.Random(seed)
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), shade(SKY[y // 4], rnd.randint(-1, 1)))
    for _ in range(3):
        x, y = rnd.randrange(1, 12), rnd.randrange(1, 15)
        line(im, x, y, x + 3, y, shade(SKY[y // 4], 5))
    return im


# The one cloud it knows, cut out flat with a hard white edge and a grey underside, three blocks long.
CLOUD = [
    '..............................................',
    '.................####.........................',
    '...............########.......######..........',
    '.......#####..##########....##########........',
    '.....#########################################',
    '....##########################################',
    '...###########################################',
    '..############################################',
    '..############################################',
    '..++++++++++++++++++++++++++++++++++++++++++++',
    '....++++++++++++++++++++++++++++++++++++++++..',
]


def cloud(part):
    im = sky(66380 + part)
    for j, row in enumerate(CLOUD):
        for i in range(16):
            c = row[part * 16 + i] if part * 16 + i < len(row) else '.'
            y = j + 3
            if c == '#':
                im.putpixel((i, y), shade('#f2f4f6', -2 if (i + j) % 5 == 0 else 0))
            elif c == '+':
                im.putpixel((i, y), shade('#b4bcc4', 0))
    return im


def sun(quarter):
    # A disc with flat, even rays, drawn round a compass: two blocks across, split into four. It does not move.
    big = Image.new('RGBA', (32, 32))
    for y in range(32):
        for x in range(32):
            big.putpixel((x, y), shade(SKY[(y // 4) % 4 if y < 16 else (y - 16) // 4], 0))
    cx, cy = 15.5, 15.5
    for y in range(32):
        for x in range(32):
            d = math.hypot(x - cx, y - cy)
            if d < 7.5:
                big.putpixel((x, y), shade('#f6e8a0', -6 if d > 6.4 else 0))
    for k in range(8):
        a = k * math.pi / 4
        for r in range(9, 14):
            big.putpixel((round(cx + math.cos(a) * r), round(cy + math.sin(a) * r)), shade('#f2e090', 0))
    qx, qy = (quarter % 2) * 16, (quarter // 2) * 16
    return big.crop((qx, qy, qx + 16, qy + 16))


def window_frame(im):
    for i in range(16):
        dot(im, i, 0, '#2c3034'); dot(im, 0, i, '#2c3034'); dot(im, i, 15, '#1e2124'); dot(im, 15, i, '#1e2124')
    for k in range(4):
        x, y = 3 + k, 12 - k
        r, g, b, a = im.getpixel((x, y)); im.putpixel((x, y), (min(255, r + 22), min(255, g + 22), min(255, b + 22), 255))
    return im


def door_leaf():
    # The far door: a steel leaf with a single seam across it at waist height and no handle on this side.
    im = new('#50565a', 66390, 2)
    for i in range(16):
        dot(im, 0, i, '#6a7074'); dot(im, 15, i, '#33373a')
    for x in range(16):
        dot(im, x, 9, '#2a2e31'); dot(im, x, 10, '#686e72')
    return im


def door_jamb():
    im = new('#3a3f43', 66391, 2)
    for i in range(16):
        dot(im, 0, i, '#535a5e'); dot(im, 15, i, '#22262a')
    return im


def chart():
    # The ward's chart on its board: rows of beds, a name to each, the earlier rows written over, the last rows empty.
    im = new('#cfc9b6', 66395, 1)
    for i in range(16):
        dot(im, i, 0, '#6a5a44'); dot(im, i, 15, '#6a5a44'); dot(im, 0, i, '#6a5a44'); dot(im, 15, i, '#6a5a44')
    fill(im, 5, 0, 11, 2, '#8e9396')
    for k, y in enumerate((4, 6, 8, 10, 12, 14)):
        line(im, 2, y, 13, y, '#9e9888')
        if k < 4:
            line(im, 2, y - 1, 5 + (k * 3) % 6, y - 1, '#3a3c40')
            if k < 2:
                line(im, 2, y - 1, 9, y - 1, '#5a5c60')
    return im


def board():
    im = new('#6a5a44', 66396, 3)
    for i in range(16):
        dot(im, i, 15, '#4a3e2e'); dot(im, 15, i, '#4a3e2e')
    return im


# ------------------------------------------------------------------------------------------------ what each reader's game draws
def seams():
    # A seam on a skin: a thin dark line closed with small slanting stitches of pale thread, the skin either side of it drawn up
    # into a pale ridge, so the line reads on a dark skin as well as a light one. Four of them, one to a row of four pixels, each a
    # different length, so a body drawn with several does not look stamped.
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for row, (length, start) in enumerate(((14, 1), (10, 3), (12, 2), (8, 4))):
        y = row * 4 + 1
        for x in range(start, start + length):
            im.putpixel((x, y), (62, 32, 30, 236))
            im.putpixel((x, y - 1), (226, 196, 184, 64)); im.putpixel((x, y + 1), (226, 196, 184, 64))
        for x in range(start + 1, start + length - 1, 3):
            im.putpixel((x, y - 1), (196, 186, 164, 235)); im.putpixel((x + 1, y + 1), (196, 186, 164, 235))
    return im


DIGITS = {
    0: ['.####.', '##..##', '##..##', '##..##', '##..##', '##..##', '.####.'],
    1: ['..##..', '.###..', '..##..', '..##..', '..##..', '..##..', '.####.'],
    2: ['.####.', '##..##', '....##', '...##.', '..##..', '.##...', '######'],
    3: ['.####.', '##..##', '....##', '..###.', '....##', '##..##', '.####.'],
    4: ['...##.', '..###.', '.####.', '##.##.', '######', '...##.', '...##.'],
    5: ['######', '##....', '#####.', '....##', '....##', '##..##', '.####.'],
    6: ['.####.', '##....', '#####.', '##..##', '##..##', '##..##', '.####.'],
    7: ['######', '....##', '...##.', '..##..', '..##..', '..##..', '..##..'],
    8: ['.####.', '##..##', '##..##', '.####.', '##..##', '##..##', '.####.'],
    9: ['.####.', '##..##', '##..##', '.#####', '....##', '...##.', '.###..'],
}
SEGMENTS = {0: 'abcdef', 1: 'bc', 2: 'abdeg', 3: 'abcdg', 4: 'bcfg', 5: 'acdfg', 6: 'acdefg', 7: 'abc', 8: 'abcdefg', 9: 'abcdfg'}


def digits(kind):
    """The ten digits, one above the other, each twelve pixels wide and fourteen high, as one kind of counter shows them: printed on a
    drum that turns (drum), on a card that falls in two halves (flap), or lit in seven bars (segment)."""
    im = Image.new('RGBA', (12, 140), (0, 0, 0, 0))
    for n in range(10):
        oy = n * 14
        if kind == 'segment':
            fill(im, 0, oy, 12, oy + 14, '#0a0c0a')
            on, off = '#d8f0c8', '#1a221a'
            bars = {'a': (3, 1, 9, 2), 'd': (3, 12, 9, 13), 'g': (3, 6, 9, 7), 'f': (2, 2, 3, 6), 'b': (9, 2, 10, 6), 'e': (2, 7, 3, 12), 'c': (9, 7, 10, 12)}
            for seg, (x0, y0, x1, y1) in bars.items():
                fill(im, x0, oy + y0, x1, oy + y1, on if seg in SEGMENTS[n] else off)
            continue
        if kind == 'drum':
            for y in range(14):
                k = -abs(y - 6.5) * 4
                fill(im, 0, oy + y, 12, oy + y + 1, shade('#e2dccb', int(k)))
            ink = '#1c1a18'
        else:
            fill(im, 0, oy, 12, oy + 14, '#1e2022'); fill(im, 0, oy + 6, 12, oy + 7, '#0a0b0c')
            ink = '#e6e2d4'
        for j, row in enumerate(DIGITS[n]):
            for i, c in enumerate(row):
                if c == '#':
                    dot(im, 3 + i, oy + 3 + j + (1 if j >= 3 and kind == 'flap' else 0), ink)
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
    # Heating: the rails and the mattress give off their own dull light, so the cot reads as hot across a dim ward.
    for e in elements:
        e['neoforge_data'] = {'block_light': light, 'sky_light': 0}


def cot_head(stage):
    top = {0: 'ticking', 1: 'ticking_hot', 2: 'ticking'}[stage]
    cover = {0: 'blanket', 1: 'blanket_hot', 2: 'blanket_made'}[stage]
    pil = 'pillow_made' if stage == 2 else 'pillow'
    t = {'t': 'tube_hot' if stage == 1 else 'tube', 'm': top, 's': 'ticking_side_hot' if stage == 1 else 'ticking_side', 'b': cover, 'p': pil, 'd': 'drawer', 'u': 'tube'}
    e = [
        # the head end: two posts, two bars across
        element([1, 0, 14], [2.5, 14, 15.5], {'all': 't'}), element([13.5, 0, 14], [15, 14, 15.5], {'all': 't'}),
        element([2.5, 12.5, 14.25], [13.5, 13.75, 15.25], {'all': 't'}), element([2.5, 9.5, 14.25], [13.5, 10.5, 15.25], {'all': 't'}),
        # the side rails, on into the foot's cell
        element([1.25, 6, 0], [2.25, 7, 14], {'all': 't'}), element([13.75, 6, 0], [14.75, 7, 14], {'all': 't'}),
        # the mattress, and the pillow on it
        element([2, 7, 0], [14, 9, 14], {'up': 'm', 'east': 's', 'west': 's', 'south': 's'}),
        element([3, 9, 10], [13, 10.75, 13.5], {'all': 'p'}),
        # the drawer under the head, its front to the cot's right
        element([3, 1.5, 7], [13, 5.5, 13], {'east': 'd', 'west': 'u', 'north': 'u', 'south': 'u', 'up': 'u', 'down': 'u'}),
    ]
    if stage == 1:
        glow(e[4:7], 9); glow(e[6:7], 12)
    if stage == 2:
        e.append(element([1.9, 9, 0], [14.1, 9.4, 9.75], {'up': 'b', 'east': 'b', 'west': 'b', 'south': 'b'}))
        e.append(element([1.8, 7, 0], [2, 9, 9.75], {'west': 'b'}))
        e.append(element([14, 7, 0], [14.2, 9, 9.75], {'east': 'b'}))
    else:
        e.append(element([2.1, 9, 0], [13.9, 9.5, 8.5], {'up': 'b', 'south': 'b', 'east': 'b', 'west': 'b'}))
        if stage == 1:
            glow(e[-1:], 7)
    return model(e, t, 'tube')


def cot_foot(stage):
    top = {0: 'ticking', 1: 'ticking_hot', 2: 'ticking'}[stage]
    cover = {0: 'blanket', 1: 'blanket_hot', 2: 'blanket_made'}[stage]
    t = {'t': 'tube_hot' if stage == 1 else 'tube', 'm': top, 's': 'ticking_side_hot' if stage == 1 else 'ticking_side', 'b': cover, 'c': 'card', 'u': 'tube'}
    e = [
        element([1, 0, 0.5], [2.5, 10, 2], {'all': 't'}), element([13.5, 0, 0.5], [15, 10, 2], {'all': 't'}),
        element([2.5, 8.5, 0.75], [13.5, 9.75, 1.75], {'all': 't'}),
        element([1.25, 6, 2], [2.25, 7, 16], {'all': 't'}), element([13.75, 6, 2], [14.75, 7, 16], {'all': 't'}),
        element([2, 7, 2], [14, 9, 16], {'up': 'm', 'east': 's', 'west': 's', 'north': 's'}),
        # the card on the footboard
        element([5.5, 3.5, 0.25], [10.5, 8.5, 0.5], {'north': 'c', 'south': 'c'}),
        # the cross-stay between the legs
        element([2.5, 2, 1], [13.5, 3, 1.5], {'all': 't'}),
    ]
    if stage == 1:
        glow(e[3:5], 9); glow(e[5:6], 12)
    if stage == 2:
        e.append(element([1.9, 9, 2], [14.1, 9.4, 16], {'up': 'b', 'north': 'b', 'east': 'b', 'west': 'b'}))
        e.append(element([1.8, 7, 2], [2, 9, 16], {'west': 'b'})); e.append(element([14, 7, 2], [14.2, 9, 16], {'east': 'b'}))
        e.append(element([2.5, 9.4, 2.5], [13.5, 9.9, 5], {'up': 'b', 'north': 'b', 'south': 'b'}))
    else:
        e.append(element([2.1, 9, 2], [13.9, 9.5, 16], {'up': 'b', 'north': 'b', 'east': 'b', 'west': 'b'}))
        e.append(element([2.1, 7.5, 1.75], [13.9, 9.5, 2], {'north': 'b'}))
        if stage == 1:
            glow(e[-2:], 7)
    return model(e, t, 'tube')


def curtain_model(stage):
    """Two cells high: 0 the rail alone, 1 the rail with the curtain bunched under it, 2 the bunched curtain's lower half, 3 the rail with
    the curtain drawn, 4 the drawn curtain's lower half. Facing north the rail runs east and west, and a bunch is gathered at its west end."""
    t = {'c': 'curtain', 'r': KEPT + 'cable', 'g': 'ring'}
    top = stage in (0, 1, 3)
    e = []
    if top:
        e.append(element([0, 15, 7.5], [16, 16, 8.5], {'all': 'r'}, uv={'north': [0, 0, 16, 1], 'south': [0, 0, 16, 1], 'up': [0, 0, 16, 1], 'down': [0, 0, 16, 1],
                                                                     'east': [2, 0, 3, 1], 'west': [2, 0, 3, 1]}))
    y0, y1 = (0, 14.5) if top else (1, 16)
    if stage in (1, 2):
        # gathered at the west end of the rail, in four folds
        for k, (x0, z0) in enumerate(((0, 6.5), (1.5, 7.5), (3, 6.75), (4.5, 7.25))):
            faces = {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}
            if not top:
                faces['down'] = 'c'
            e.append(element([x0, y0, z0], [x0 + 1.75, y1, z0 + 2.25], faces, uv={k2: [k * 4, 16 - y1, k * 4 + 2, 16 - y0] for k2 in ('north', 'south', 'east', 'west')}))
        if top:
            for x in (0.5, 2, 3.5, 5):
                e.append(element([x, 14.5, 7.25], [x + 1, 15.5, 8.75], {'all': 'g'}))
    elif stage in (3, 4):
        # drawn across the whole cell, in pleats a pixel in front and behind
        for k in range(4):
            z = 7 if k % 2 == 0 else 8
            faces = {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c'}
            if not top:
                faces['down'] = 'c'
            e.append(element([k * 4, y0, z], [k * 4 + 4, y1, z + 1], faces, uv={'north': [k * 4, 16 - y1, k * 4 + 4, 16 - y0], 'south': [k * 4, 16 - y1, k * 4 + 4, 16 - y0]}))
        if top:
            for x in (1, 5, 9, 13):
                e.append(element([x, 14.5, 7.25], [x + 1, 15.5, 8.75], {'all': 'g'}))
    return model(e, t, 'curtain')


def lamp_model(stage):
    t = {'s': 'lamp_steel', 'h': 'shade', 'b': 'bulb_on' if stage else 'bulb_off'}
    e = [
        element([5.5, 9, 15], [10.5, 14, 16], {'all': 's'}),                                    # the plate on the wall
        element([7, 10.5, 13], [9, 12.5, 15], {'all': 's'}),                                    # the first knuckle
        element([7.5, 11, 6], [8.5, 12, 13.5], {'all': 's'}, rotation={'origin': [8, 11.5, 13.5], 'axis': 'x', 'angle': -22.5}),
        element([7, 13, 5], [9, 15, 7], {'all': 's'}),                                          # the elbow
        element([7.5, 9, 4.75], [8.5, 14, 5.75], {'all': 's'}, rotation={'origin': [8, 14, 5.25], 'axis': 'x', 'angle': 22.5}),
        element([5, 7, 1], [11, 9.5, 7], {'all': 'h', 'down': 's'}),                            # the shade
        element([6, 9.5, 2], [10, 10.5, 6], {'all': 'h'}),
        element([6.5, 6.75, 2.5], [9.5, 7, 5.5], {'down': 'b'}),                                # the bulb under it
    ]
    return model(e, t, 'lamp_steel')


def screen_model(stage):
    face = ['screen_dark', 'screen_words', 'screen_door', 'screen_figure', 'screen_static'][stage]
    return model([element([2, 3, 12], [14, 13, 16], {'all': 'h', 'north': 'f'}, uv={'north': [0, 0, 16, 16]})], {'h': 'housing', 'f': face}, 'housing', False)


def counter_model():
    return model([element([0, 0, 13], [16, 16, 16], {'all': 'h', 'north': 'f'})], {'h': 'housing', 'f': 'counter'}, 'housing', False)


def table_head():
    t = {'s': 'table_steel', 'a': 'arm_steel', 'r': 'strap'}
    return model([
        element([1, 9, 0], [15, 11, 16], {'all': 's'}),
        element([3, 11, 12.5], [13, 12, 15.5], {'all': 's'}),                                  # the headrest
        element([5, 0, 5], [11, 9, 11], {'all': 'a'}),                                         # the pedestal
        element([3, 0, 3], [13, 1, 13], {'all': 'a'}),
        element([0.75, 8.5, 4], [15.25, 11.25, 6], {'north': 'r', 'south': 'r', 'up': 'r', 'east': 'r', 'west': 'r'}),
    ], t, 'table_steel')


def table_foot():
    t = {'s': 'table_steel', 'a': 'arm_steel', 'r': 'strap'}
    return model([
        element([1, 9, 0], [15, 11, 16], {'all': 's'}),
        element([0.75, 8.5, 6], [15.25, 11.25, 8], {'north': 'r', 'south': 'r', 'up': 'r', 'east': 'r', 'west': 'r'}),
        element([0.75, 8.5, 12], [15.25, 11.25, 14], {'north': 'r', 'south': 'r', 'up': 'r', 'east': 'r', 'west': 'r'}),
        element([7, 4, 7], [9, 9, 9], {'all': 'a'}),
    ], t, 'table_steel')


def arm_model(stage):
    """Hung from the ceiling over the table, reaching toward the cell north of it (where the table is): folded up, let down, or working
    (let down, its working end turned)."""
    t = {'a': 'arm_steel', 't': 'tool'}
    e = [element([5, 15, 5], [11, 16, 11], {'all': 'a'}), element([7, 11, 7], [9, 15, 9], {'all': 'a'}), element([6.5, 9.5, 6.5], [9.5, 11, 9.5], {'all': 'a'})]
    if stage == 0:
        e += [element([7.25, 9.5, 2], [8.75, 11, 7], {'all': 'a'}), element([7.5, 10, 0], [8.5, 13, 2], {'all': 't'})]
    else:
        lower = -10 if stage == 1 else -12
        e += [element([7.25, 1, 6], [8.75, 9.5, 7.5], {'all': 'a'}, rotation={'origin': [8, 9.5, 7], 'axis': 'x', 'angle': -22.5}),
              element([7, -1, 2.5], [9, 1.5, 4.5], {'all': 'a'}),
              element([7.5, lower, 3], [8.5, -1, 4], {'all': 't'}, rotation={'origin': [8, -1, 3.5], 'axis': 'y', 'angle': 45 if stage == 2 else 0})]
    return model(e, t, 'arm_steel')


def hatch_model(stage):
    if stage == 0:
        return model([element([0, 0, 0], [16, 16, 16], {'all': 'l', 'up': 'p'})], {'p': 'hatch_plate', 'l': 'hatch_leaf'}, 'hatch_plate', False)
    return model([element([0, 0, 0], [8, 16, 0.75], {'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'up': 'l', 'down': 'l'}),
                  element([8, 0, 15.25], [16, 16, 16], {'north': 'l', 'south': 'l', 'east': 'l', 'west': 'l', 'up': 'l', 'down': 'l'})],
                 {'l': 'hatch_leaf'}, 'hatch_leaf')


def vent_model(stage):
    return model([element([2, 2, 15], [14, 14, 16], {'all': 'v'}, uv={'north': [2, 2, 14, 14], 'south': [2, 2, 14, 14]})], {'v': 'vent_frost' if stage else 'vent'}, 'vent', False)


WINDOW_FACES = ['window_sky', 'window_cloud_0', 'window_cloud_1', 'window_cloud_2', 'window_sun_0', 'window_sun_1', 'window_sun_2', 'window_sun_3']


def window_model(stage):
    return model([element([0, 0, 0], [16, 16, 16], {'all': 'c', 'north': 'f'})], {'c': KEPT.replace('block/kept/', 'block/') + 'kept_casing', 'f': WINDOW_FACES[stage]}, 'window_sky', False)


def door_model(stage):
    t = {'d': 'door_leaf', 'j': 'door_jamb'}
    jambs = [element([0, 0, 5.5], [1.5, 16, 10.5], {'all': 'j'}), element([14.5, 0, 5.5], [16, 16, 10.5], {'all': 'j'})]
    if stage in (1, 3):
        jambs.append(element([0, 14.5, 5.5], [16, 16, 10.5], {'all': 'j'}))
    if stage in (0, 1):
        return model(jambs + [element([1.5, 0, 7], [14.5, 16 if stage == 1 else 16, 9], {'all': 'd'}, uv={'north': [1.5, 0, 14.5, 16], 'south': [1.5, 0, 14.5, 16]})], t, 'door_leaf')
    return model(jambs, t, 'door_jamb')


def chart_model():
    return model([element([3.5, 2, 15], [12.5, 14, 16], {'all': 'w', 'north': 'b'}), element([4.5, 3, 14.5], [11.5, 13, 15], {'north': 'c', 'east': 'c', 'west': 'c', 'up': 'c'})],
                 {'w': 'board', 'b': 'board', 'c': 'chart'}, 'chart')


def fitting_models():
    m = {}
    for s in range(3):
        m[f'cot_{s}'] = cot_head(s); m[f'cot_foot_{s}'] = cot_foot(s)
    for s in range(5):
        m[f'curtain_{s}'] = curtain_model(s)
    for s in range(2):
        m[f'lamp_{s}'] = lamp_model(s); m[f'hatch_{s}'] = hatch_model(s); m[f'vent_{s}'] = vent_model(s)
    for s in range(5):
        m[f'screen_{s}'] = screen_model(s)
    m['counter_0'] = counter_model(); m['table_0'] = table_head(); m['table_foot_0'] = table_foot(); m['chart_0'] = chart_model()
    for s in range(3):
        m[f'arm_{s}'] = arm_model(s)
    for s in range(8):
        m[f'window_{s}'] = window_model(s)
    for s in range(4):
        m[f'door_{s}'] = door_model(s)
    return m


def textures():
    out = {}
    for name, im in materials().items():
        out[f'block/not_yet_{name}.png'] = im
    faces = {
        'tube': tube(), 'tube_hot': tube_hot(), 'ticking': ticking(), 'ticking_hot': ticking(True), 'ticking_side': ticking_side(),
        'ticking_side_hot': ticking_side(True), 'blanket': blanket(), 'blanket_hot': blanket_hot(),
        'blanket_made': blanket(True), 'pillow': pillow(), 'pillow_made': pillow(True), 'drawer': drawer(), 'card': chart_card(),
        'curtain': curtain(), 'ring': ring_tex(), 'lamp_steel': lamp_steel(), 'shade': shade_tex(), 'bulb_on': bulb(True), 'bulb_off': bulb(False),
        'housing': housing(), 'screen_dark': screen_dark(), 'screen_words': screen_words(), 'screen_door': door_picture(0),
        'screen_figure': door_picture(1), 'screen_static': screen_static(), 'counter': counter_frame(), 'table_steel': table_steel(),
        'strap': strap(), 'arm_steel': arm_steel(), 'tool': tool_tex(), 'hatch_plate': hatch_plate(), 'hatch_leaf': hatch_leaf(),
        'vent': vent(0), 'vent_frost': vent(1), 'window_sky': window_frame(sky(66379)), 'door_leaf': door_leaf(), 'door_jamb': door_jamb(),
        'chart': chart(), 'board': board(),
    }
    for p in range(3):
        faces[f'window_cloud_{p}'] = window_frame(cloud(p))
    for q in range(4):
        faces[f'window_sun_{q}'] = window_frame(sun(q))
    for name, im in faces.items():
        out[f'block/not_yet/{name}.png'] = im
    out['entity/not_yet_seams.png'] = seams()
    for kind in ('drum', 'flap', 'segment'):
        out[f'misc/not_yet_digits_{kind}.png'] = digits(kind)
    return out


KINDS = ['cot', 'cot_foot', 'curtain', 'lamp', 'screen', 'counter', 'table', 'table_foot', 'arm', 'hatch', 'vent', 'window', 'door', 'chart']
STAGES = {'cot': 3, 'cot_foot': 3, 'curtain': 5, 'lamp': 2, 'screen': 5, 'arm': 3, 'hatch': 2, 'vent': 2, 'window': 8, 'door': 4}
STAGE_MAX = 8
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'not_yet_fitting': 'Ward Fitting', 'not_yet_tile': 'Ward Tile', 'not_yet_wall': 'Ward Wall', 'not_yet_dado': 'Ward Dado'}
ANIMATED = {'textures/block/not_yet/screen_words.png.mcmeta': {'animation': {'frametime': 6}},
            'textures/block/not_yet/screen_static.png.mcmeta': {'animation': {'frametime': 2}}}


def files():
    out = {}
    for name in materials():
        out[f'models/block/not_yet_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/not_yet_{name}'}}
        out[f'blockstates/not_yet_{name}.json'] = {'variants': {'': {'model': NS + f'block/not_yet_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/not_yet/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(STAGE_MAX):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/not_yet/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/not_yet_fitting.json'] = {'variants': variants}
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
                assert f'block/not_yet/{tex[len(P):]}.png' in images, (key, tex)
    # A screen that is on must be brighter than one that is off, and the heating ticking warmer than the cold.
    def mean(im, box=(2, 2, 14, 14)):
        px = [im.getpixel((x, y)) for y in range(box[1], box[3]) for x in range(box[0], box[2])]
        return [sum(p[i] for p in px) / len(px) for i in range(3)]
    assert sum(mean(images['block/not_yet/screen_door.png'])) > sum(mean(images['block/not_yet/screen_dark.png'])) + 60
    hot, cold = mean(images['block/not_yet/ticking_hot.png'], (0, 0, 16, 16)), mean(images['block/not_yet/ticking.png'], (0, 0, 16, 16))
    assert hot[0] - hot[2] > cold[0] - cold[2] + 20, (hot, cold)
    for warm, plain in (('tube_hot', 'tube'), ('blanket_hot', 'blanket'), ('ticking_side_hot', 'ticking_side')):
        w, c = mean(images[f'block/not_yet/{warm}.png'], (0, 0, 16, 16)), mean(images[f'block/not_yet/{plain}.png'], (0, 0, 16, 16))
        assert w[0] - w[2] > c[0] - c[2] + 20, (warm, w, c)
    # a rimed grille is still a grille: its slots stay darker than its bars
    rimed = images['block/not_yet/vent_frost.png']
    slots = sum(sum(rimed.getpixel((x, y))[:3]) for y in range(3, 14, 2) for x in range(3, 13)) / 60
    bars = sum(sum(rimed.getpixel((x, y))[:3]) for y in range(4, 13, 2) for x in range(3, 13)) / 50
    assert slots + 150 < bars, (slots, bars)
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
        print(f'Drew the ward under {opts.out}: {len(images)} textures, {len(data)} models and states.'); return
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text())
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        elif not opts.out:
            lang[key] = name
    if not check and not opts.out:
        lang_path.write_text(flicker.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the ward: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
