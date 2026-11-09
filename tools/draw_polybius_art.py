"""Polybius (0.4.217): the strip-mall arcade and its black cabinet.

Draws, in native Minecraft pixels from fixed seeds: three materials (the black carpet with its scattered neon shapes, the dark
painted wall panels, the black ceiling tiles); every fitting of the polybius_fitting block, its models facing north and turned by
its facing (the four cabinets, body and screen, each with its own side art and an animated attract-mode screen; the taped-up
dead cabinets; the change machine; the notice board with its clipping; the men's clipboard; neon strips in three colours; a
stool; the prize counter and its shelf; three posters for games the arcade doesn't have; a bin; the ceiling lights, lit and
dead). Writes the models and block states with them, and the blocks' names. --check verifies everything already written.

Palette: carpet black #0c0a12 with neon pink #ff4fb0, cyan #45e6ff, yellow #ffe14a, green #6dff7a; wall aubergine #241a2e;
cabinet woods and laminates per game; the black cabinet #070707 with a faint green sheen #12261c.
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
P = NS + 'block/polybius/'
NEON = ['#ff4fb0', '#45e6ff', '#6dff7a', '#ffe14a']
GAME_BODY = ['#1c2350', '#123a3c', '#4a2e18', '#070707']
GAME_ACCENT = ['#f0d080', '#7ad8e8', '#f09a3a', '#12261c']
FRAMES = 4


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


# ------------------------------------------------------------------------------------------------ materials
def carpet():
    im = new(base='#0c0a12', seed=217, spread=2); rnd = random.Random(2171)
    shapes = [((1, 2), [(0, 0), (1, 0), (2, 1)]), ((9, 1), [(0, 1), (1, 0), (2, 1), (3, 0)]), ((4, 9), [(0, 0), (0, 1), (1, 1)]),
              ((12, 7), [(0, 0), (1, 1), (2, 2)]), ((7, 13), [(0, 0), (1, 0), (2, 0)]), ((13, 13), [(0, 0), (1, 1)]), ((2, 6), [(0, 0)])]
    for (ox, oy), pts in shapes:
        c = NEON[rnd.randrange(4)]
        for dx, dy in pts:
            dot(im, (ox + dx) % 16, (oy + dy) % 16, c)
    return im


def wall():
    im = new(base='#241a2e', seed=218, spread=3)
    for y in range(16):
        dot(im, 0, y, '#1a1222'); dot(im, 8, y, '#1a1222')
    for x in range(16):
        dot(im, x, 15, '#1a1222')
    return im


def ceiling():
    im = new(base='#111014', seed=219, spread=2)
    for i in range(16):
        dot(im, i, 0, '#1c1a20'); dot(im, 0, i, '#1c1a20')
    rnd = random.Random(2191)
    for _ in range(10):
        dot(im, rnd.randrange(1, 16), rnd.randrange(1, 16), '#0a090c')
    return im


def materials():
    return {'carpet': carpet(), 'wall': wall(), 'ceiling': ceiling()}


# ------------------------------------------------------------------------------------------------ cabinets
def cab_side(g):
    im = new(base=GAME_BODY[g], seed=230 + g, spread=3)
    a = GAME_ACCENT[g]
    if g == 0:  # a moth, big, over a lamp's glow
        for x, y in [(5, 6), (6, 5), (7, 6), (9, 6), (10, 5), (11, 6), (6, 7), (10, 7), (8, 6), (8, 7), (8, 8)]:
            dot(im, x, y, a)
        fill(im, 7, 11, 10, 13, '#f8e8a0')
    elif g == 1:  # waves
        for x in range(16):
            dot(im, x, 9 + int(round(math.sin(x / 2.5) * 1.2)), a); dot(im, x, 12 + int(round(math.cos(x / 2.5) * 1.2)), shade(a, -40))
    elif g == 2:  # a dial
        for i in range(9):
            ang = math.pi * (1 + i / 8)
            dot(im, 8 + int(round(math.cos(ang) * 5)), 10 + int(round(math.sin(ang) * 5)), a)
        dot(im, 8, 10, '#e04030'); dot(im, 9, 9, '#e04030'); dot(im, 10, 8, '#e04030')
    else:  # nothing: a faint green sheen down one edge
        for y in range(16):
            dot(im, 14, y, '#0e1a14'); dot(im, 15, y, a)
    for y in range(16):
        dot(im, 0, y, shade(GAME_BODY[g], -12))
    return im


def cab_front(g):
    body = GAME_BODY[g]
    im = new(base=body, seed=240 + g, spread=2)
    # the control panel, lit from the screen above
    fill(im, 1, 0, 15, 5, shade(body, 18))
    dot(im, 4, 2, '#202020'); dot(im, 4, 1, '#e03030' if g < 3 else '#1a3a2a')  # the stick
    for x, c in [(9, '#f0d040'), (11, '#40a0f0')] if g < 3 else [(9, '#0f1f17'), (11, '#0f1f17')]:
        dot(im, x, 2, c)
    # the coin door: two slots with their lamps, unless it is the black one, which has none
    if g < 3:
        fill(im, 5, 8, 11, 13, '#2a2a2e')
        dot(im, 6, 9, '#ff7030'); dot(im, 9, 9, '#ff7030'); fill(im, 6, 11, 7, 12, '#0a0a0a'); fill(im, 9, 11, 10, 12, '#0a0a0a')
    fill(im, 1, 14, 15, 16, shade(body, -10))
    return im


def bezel(g, dead=False, look=0):
    body = '#3a3430' if dead else GAME_BODY[g]
    im = new(base=body, seed=250 + g + (10 if dead else 0) + look, spread=2)
    # the marquee: lit, and drawn with the game's light, or nothing at all
    if not dead and g < 3:
        fill(im, 1, 0, 15, 3, shade(GAME_ACCENT[g], -10))
        for x in range(3, 13, 2):
            dot(im, x, 1, '#ffffff')
    elif dead:
        fill(im, 1, 0, 15, 3, '#6a6058' if look == 0 else '#5a5450')
    fill(im, 2, 4, 14, 13, '#050505')
    if dead:
        fill(im, 3, 5, 13, 12, '#141414')
        for i in range(10):  # a strip of masking tape across the dark glass
            dot(im, 3 + i, 10 - i // 2 - look, '#d8cca0'); dot(im, 3 + i, 11 - i // 2 - look, '#c8bc90')
    return im


def screen_frames(g):
    im = Image.new('RGBA', (16, 16 * FRAMES), (0, 0, 0, 255))
    for f in range(FRAMES):
        oy = f * 16
        if g == 0:
            fill(im, 6, oy + 6, 9, oy + 9, '#ffe8a0')
            ang = f / FRAMES * 2 * math.pi
            dot(im, 7 + int(round(math.cos(ang) * 4)), oy + 7 + int(round(math.sin(ang) * 4)), '#e8dcc8')
            for i in range(3):
                dot(im, (2 + i * 5 + f * 2) % 16, oy + 2 + i * 4, '#fff0b0')
        elif g == 1:
            fill(im, 0, oy, 16, oy + 16, '#06121a')
            for i in range(3):
                dot(im, 3 + i * 4, oy + (14 - f * 3 - i * 4) % 16, '#a0d8f0')
            dot(im, 7 + (f % 2), oy + 13, '#d8c0a0'); dot(im, 8 + (f % 2), oy + 13, '#d8c0a0')
        elif g == 2:
            fill(im, 1, oy + 7, 15, oy + 10, '#d8c890')
            dot(im, 3 + f * 3, oy + 6, '#c02018'); dot(im, 3 + f * 3, oy + 7, '#c02018'); dot(im, 3 + f * 3, oy + 8, '#c02018')
            fill(im, 2 + f * 2, oy + 9, 7 + f * 2, oy + 10, '#60a040')
        else:
            for t in range(0, 60):
                tt = t * .3
                r = 1 + tt * .55
                a = tt + f * math.pi / 8
                x, y = 8 + int(round(math.cos(a) * r)), oy + 8 + int(round(math.sin(a) * r))
                if oy <= y < oy + 16:
                    k = int(40 + 30 * math.sin(tt * .4 + f))
                    dot(im, x, y, (40 + k, 140 + k // 2, 110 + k))
    return im


SCREEN_META = {'animation': {'frametime': 6}}
BLACK_META = {'animation': {'frametime': 20, 'interpolate': True}}


# ------------------------------------------------------------------------------------------------ the rest
def change(part):
    if part == 'front':
        im = new(base='#8a8a92', seed=260, spread=3)
        fill(im, 3, 2, 13, 6, '#1a1a1e'); fill(im, 4, 3, 12, 5, '#3a6a3a')  # the lit FREE PLAY card
        fill(im, 5, 8, 11, 9, '#101010')  # the bill slot
        fill(im, 4, 12, 12, 15, '#5a5a62'); fill(im, 5, 13, 11, 14, '#202024')  # the tray
        return im
    return new(base='#7a7a82', seed=261, spread=3)


def notice():
    im = new(base='#a87a4a', seed=262, spread=6)
    fill(im, 0, 0, 16, 1, '#5a3a20'); fill(im, 0, 15, 16, 16, '#5a3a20'); fill(im, 0, 0, 1, 16, '#5a3a20'); fill(im, 15, 0, 16, 16, '#5a3a20')
    fill(im, 2, 2, 9, 12, '#d8c890')
    for y in range(4, 11, 2):
        fill(im, 3, y, 8, y + 1, '#8a7a60')
    fill(im, 3, 2, 8, 3, '#3a3020')
    fill(im, 10, 9, 14, 13, '#f0ece0'); fill(im, 11, 10, 13, 11, '#2a2a2a')
    dot(im, 5, 2, '#e03030'); dot(im, 12, 9, '#3060e0')
    return im


def clipboard():
    im = new(base='#8a5a30', seed=263, spread=4)
    fill(im, 2, 3, 14, 15, '#f0ece0')
    for y in range(6, 14, 2):
        fill(im, 3, y, 12, y + 1, '#9a9a9a')
    fill(im, 5, 1, 11, 4, '#b0b0b8')
    return im


def neon(c):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 0, 6, 16, 10, c, 90)
    fill(im, 0, 7, 16, 9, c)
    fill(im, 0, 7, 16, 8, '#ffffff', 160)
    return im


def stool(part):
    if part == 'top':
        im = new(base='#a02028', seed=264, spread=4)
        fill(im, 7, 3, 9, 13, '#c8c0a0')  # tape over the split
        return im
    return new(base='#b8bcc4', seed=265, spread=5)


def counter(part):
    if part == 'front':
        im = new(base='#2a2620', seed=266, spread=3)
        fill(im, 1, 1, 15, 11, '#3a4a50')
        for x, y, c in [(3, 7, '#e04060'), (6, 8, '#40c060'), (9, 6, '#f0e040'), (12, 8, '#4080f0'), (4, 9, '#202020')]:
            fill(im, x, y, x + 2, y + 2, c)
        return im
    return new(base='#b8c8cc', seed=267, spread=2)


# 0.4.221 (owner: "Add tickets/ticket exchange. Vary textures on vendor stand."): eight prizes, each on its own shelf cell with its
# price card, and the same prizes as the things a reader carries away; and the strip of tickets every game pays out.
PRIZE_NAMES = ['spider', 'finger_trap', 'bouncy_ball', 'glow_star', 'slap_bracelet', 'kazoo', 'mood_ring', 'plush_dog']
PRIZE_COST = [5, 5, 10, 10, 15, 20, 25, 40]
DIGITS = {'0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
          '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111']}


def digits(im, x, y, text, colour):
    for ch in text:
        for r, row in enumerate(DIGITS[ch]):
            for c, bit in enumerate(row):
                if bit == '1':
                    dot(im, x + c, y + r, colour)
        x += 4


def prize_icon(k, im=None, ox=0, oy=0, small=False, mood=4):
    """One prize, drawn at 16 pixels (an item) or 7 (on the shelf, two to a row)."""
    im = im or Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    def d(x, y, c):
        if small:
            dot(im, ox + x // 2, oy + y // 2, c)
        else:
            dot(im, ox + x, oy + y, c)
    def f(x0, y0, x1, y1, c):
        for y in range(y0, y1):
            for x in range(x0, x1):
                d(x, y, c)
    if k == 0:  # a rubber spider: a black body, eight bent legs
        f(6, 6, 10, 10, '#1a1a1a'); f(7, 5, 9, 6, '#1a1a1a')
        for i, (x, y) in enumerate([(3, 4), (2, 7), (3, 10), (4, 12), (12, 4), (13, 7), (12, 10), (11, 12)]):
            sx = 1 if x < 8 else -1
            d(x, y, '#2a2a2a'); d(x + sx, y, '#2a2a2a'); d(x + 2 * sx, y + (1 if y < 8 else -1), '#2a2a2a')
        d(7, 6, '#c02020'); d(8, 6, '#c02020')
    elif k == 1:  # a woven finger trap, striped
        for x in range(2, 14):
            for y in range(6, 10):
                d(x, y, '#d84060' if (x + y) % 3 == 0 else '#f0d050' if (x + y) % 3 == 1 else '#3070c0')
        f(1, 7, 2, 9, '#a03040'); f(14, 7, 15, 9, '#a03040')
    elif k == 2:  # a bouncy ball, swirled
        for x in range(4, 12):
            for y in range(4, 12):
                if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 14:
                    d(x, y, '#f05a28' if (x * 2 + y) % 5 < 2 else '#30b0e0' if (x + y * 2) % 5 < 2 else '#f0e040')
        d(6, 5, '#ffffff')
    elif k == 3:  # a glow-in-the-dark star
        for x, y in [(7, 2), (8, 2), (7, 3), (8, 3), (6, 4), (9, 4)]:
            d(x, y, '#c8f0a0')
        f(2, 6, 14, 8, '#c8f0a0'); f(4, 8, 12, 10, '#c8f0a0'); f(4, 10, 6, 13, '#c8f0a0'); f(10, 10, 12, 13, '#c8f0a0')
        f(6, 6, 10, 9, '#e8ffd0')
    elif k == 4:  # a slap bracelet, curled, in leopard print
        for x in range(2, 14):
            for y in range(5, 11):
                if 3 <= ((x - 8) ** 2 / 36 + (y - 8) ** 2 / 9) * 4 <= 4.6 or y in (6, 10) and 3 < x < 13:
                    d(x, y, '#e0a040' if (x + y) % 4 else '#3a2010')
    elif k == 5:  # a kazoo
        f(2, 7, 12, 10, '#c03030'); f(12, 6, 14, 11, '#c03030'); f(6, 6, 8, 7, '#f0e0a0'); d(3, 8, '#801818')
    elif k == 6:  # a mood ring: a band and its stone, black until it is worn
        stone, shine = MOODS[mood]
        for x in range(4, 12):
            for y in range(7, 13):
                if 6 <= (x - 7.5) ** 2 + (y - 10) ** 2 * 2 <= 14:
                    d(x, y, '#c8b060')
        f(6, 4, 10, 8, stone); d(7, 5, shine); d(8, 6, shine)
    else:  # a plush dog with one eye
        f(3, 6, 12, 12, '#c89a68'); f(10, 3, 15, 8, '#c89a68'); f(10, 2, 12, 4, '#8a6040'); f(4, 12, 6, 14, '#c89a68'); f(9, 12, 11, 14, '#c89a68')
        d(13, 5, '#141414'); d(14, 6, '#3a2010'); d(2, 7, '#8a6040')
    return im


# 0.4.411 (owner: "Massive disappointment that I couldn't snap the bracelet onto my wrist"): the prizes in use. The mood ring's
# stone in its five colours (violet, blue, green, amber, black), the slap bracelet held up straight before it snaps, the finger trap
# stretched as it tightens, and the strip of print, gold and stones the worn bracelet and ring are drawn from.
MOODS = [('#7a3ad0', '#c8a0ff'), ('#2a6ad8', '#90c0ff'), ('#2aa860', '#a0f0c0'), ('#d89a20', '#ffe0a0'), ('#18141c', '#3a3044')]
LEOPARD = ('#e0a040', '#3a2010', '#b07028')


def bracelet_straight():
    """The slap bracelet straightened out, a stiff strip of leopard print held up to snap."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(13):
        x, y = 2 + i, 13 - i
        for w in range(2):
            c = LEOPARD[1] if (i * 3 + w) % 5 == 0 else LEOPARD[2] if w else LEOPARD[0]
            dot(im, x, y - w, c)
    dot(im, 14, 1, '#c8c0b0'); dot(im, 2, 13, '#c8c0b0')
    return im


def trap_pulled(level):
    """The finger trap pulled tighter: longer, and narrower in the middle, as the weave closes."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    half = 6 + level  # half the length grows; the middle draws in
    for x in range(8 - half, 8 + half):
        mid = abs(x - 7.5) < 3
        top, bottom = (7, 9) if mid and level >= 2 else (6 + (1 if mid and level else 0), 10 - (1 if mid and level else 0))
        for y in range(top, bottom):
            dot(im, x, y, '#d84060' if (x + y) % 3 == 0 else '#f0d050' if (x + y) % 3 == 1 else '#3070c0')
    for y in (7, 8):
        dot(im, 7 - half, y, '#a03040'); dot(im, 8 + half, y, '#a03040')
    return im


def wear():
    """What the worn prizes are drawn from: three rows of leopard print, a row of gold, and the ring's five stones, two pixels each."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    rng = random.Random(411)
    for x in range(16):
        for y in range(3):
            dot(im, x, y, LEOPARD[1] if rng.random() < .22 else LEOPARD[0] if (x + y) % 3 else LEOPARD[2])
    for x in range(16):
        dot(im, x, 4, '#d8c070' if x % 4 else '#f0e0a0')
    for m, (stone, shine) in enumerate(MOODS):
        for x in range(2):
            for y in range(6, 8):
                dot(im, m * 2 + x, y, shine if x == 0 and y == 6 else stone)
    return im


def prizes(k):
    """A shelf cell: two rows of one prize, and its price on a card at the front."""
    im = new(base='#3a2a1e', seed=268 + k, spread=3)
    for y in (6, 12):
        fill(im, 0, y, 16, y + 1, '#5a4a3a')
    for row, oy in ((0, -1), (1, 5)):
        for ox in (0, 8):
            prize_icon(k, im, ox, oy, small=True)
    text = str(PRIZE_COST[k]); w = len(text) * 4 - 1
    fill(im, 8 - w // 2 - 1, 10, 8 - w // 2 + w + 1, 16, '#f0ead8'); digits(im, 8 - w // 2, 11, text, '#c02020')
    return im


def tickets():
    """A strip of three orange tickets, folded on their perforations."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i, (x, y) in enumerate([(1, 9), (5, 6), (9, 3)]):
        fill(im, x, y, x + 6, y + 5, '#f08a30' if i % 2 == 0 else '#e87a28')
        fill(im, x + 1, y + 1, x + 5, y + 2, '#fff0d0'); dot(im, x + 2, y + 3, '#a04010'); dot(im, x + 3, y + 3, '#a04010')
        dot(im, x + 5, y + 2, '#a85a20')
    return im


# ------------------------------------------------------------------------------------------------ Block Party and the regulars' games
BRAWL_BODY = '#2a1a3a'


def brawl_front(half):
    """Block Party's control panel, two players to each half: a stick and two buttons each, in the players' colours."""
    im = new(base=BRAWL_BODY, seed=300 + half, spread=2)
    fill(im, 0, 0, 16, 6, shade(BRAWL_BODY, 22))
    colours = [['#60d0ff', '#ff7060'], ['#80e060', '#f0d050']][half]
    for i, c in enumerate(colours):
        x = 2 + i * 8
        dot(im, x, 1, c); dot(im, x, 2, '#202020'); dot(im, x + 2, 3, c); dot(im, x + 4, 3, c)
    fill(im, 4, 8, 12, 13, '#2a2a2e')
    dot(im, 5, 9, '#ff7030'); dot(im, 10, 9, '#ff7030'); fill(im, 5, 11, 7, 12, '#0a0a0a'); fill(im, 9, 11, 11, 12, '#0a0a0a')
    fill(im, 0, 14, 16, 16, shade(BRAWL_BODY, -10))
    return im


def brawl_side():
    im = new(base=BRAWL_BODY, seed=304, spread=3)
    for x, y in [(4, 5), (5, 4), (6, 5), (10, 9), (11, 8), (12, 9)]:  # two fists, flying
        dot(im, x, y, '#f0c080')
    return im


def brawl_marquee(half):
    """The marquee: a row of blocks in the four players' colours stacked up like a party, lit from behind, across both halves."""
    im = new(base='#2a1408', seed=306 + half, spread=2)
    fill(im, 0, 0, 16, 1, '#5a3010'); fill(im, 0, 15, 16, 16, '#5a3010')
    colours = ['#60d0ff', '#ff7060', '#80e060', '#f0d050']
    for i, (x, y) in enumerate([(1, 8), (5, 5), (9, 8), (12, 4)] if half == 0 else [(1, 4), (4, 8), (8, 5), (12, 8)]):
        c = colours[(i + half * 2) % 4]
        fill(im, x, y, x + 4, y + 4, c); fill(im, x, y, x + 4, y + 1, shade(c, 40)); fill(im, x + 3, y + 1, x + 4, y + 4, shade(c, -40))
    return im


def brawl_frames(half):
    """The attract mode across the wide screen: a street at night going by, two of the players walking along it."""
    im = Image.new('RGBA', (16, 16 * FRAMES), (0, 0, 0, 255))
    for f in range(FRAMES):
        oy = f * 16
        fill(im, 0, oy, 16, oy + 8, '#0a0c1e')
        for i in range(4):
            x = (i * 5 - f * 2 + half * 3) % 18 - 1
            fill(im, x, oy + 3, x + 4, oy + 8, '#161424'); dot(im, x + 1, oy + 5, '#e0b860')
        fill(im, 0, oy + 8, 16, oy + 16, '#2c2c30')
        x = 4 + (f % 2) + half * 3
        fill(im, x, oy + 9, x + 2, oy + 11, '#c89a78'); fill(im, x, oy + 11, x + 2, oy + 14, ['#60d0ff', '#80e060'][half]); dot(im, x + (f % 2), oy + 14, '#303050')
        fill(im, 12 - half * 2, oy + 9, 14 - half * 2, oy + 14, '#4a7a3a')  # a zombie, coming
    return im


OTHER_BODY = ['#3a2010', '#10302a', '#302a10']
OTHER_ACCENT = ['#f0e080', '#80e0a0', '#e0a050']


def other_frames(g):
    """The regulars' games, in play: a lighthouse beam over the sea, a dragonfly over lily pads, crates falling into a cart."""
    im = Image.new('RGBA', (16, 16 * FRAMES), (0, 0, 0, 255))
    for f in range(FRAMES):
        oy = f * 16
        if g == 0:
            fill(im, 0, oy + 11, 16, oy + 16, '#0a2040'); fill(im, 2, oy + 5, 4, oy + 11, '#d8d8d0'); dot(im, 3, oy + 4, '#f0e080')
            for i in range(6):
                dot(im, 4 + i * 2, oy + 4 + (i * (f - 1)) // 3, '#f0e080')
            dot(im, 11 - f, oy + 12, '#a0a0a0'); dot(im, 12 - f, oy + 12, '#a0a0a0')
        elif g == 1:
            fill(im, 0, oy, 16, oy + 16, '#0a2a1a')
            for x, y in [(3, 11), (10, 13), (12, 6)]:
                fill(im, x, oy + y, x + 3, oy + y + 2, '#3a8a3a')
            x, y = 5 + f * 2, 6 + (f % 2)
            dot(im, x, oy + y, '#80e0a0'); dot(im, x - 1, oy + y - 1, '#c0f0ff'); dot(im, x + 1, oy + y - 1, '#c0f0ff')
            dot(im, (9 + f * 3) % 16, oy + 3, '#e0e0a0')
        else:
            fill(im, 0, oy, 16, oy + 16, '#1a1408')
            fill(im, 5, oy + 12, 11, oy + 15, '#806030'); dot(im, 5, oy + 15, '#303030'); dot(im, 10, oy + 15, '#303030')
            fill(im, 7, oy + 2 + f * 2, 9, oy + 4 + f * 2, '#e0a050'); fill(im, 3, oy + (f * 3 + 5) % 10, 5, oy + (f * 3 + 5) % 10 + 2, '#c08040')
    return im


def other_side(g):
    im = new(base=OTHER_BODY[g], seed=320 + g, spread=3)
    for y in range(16):
        dot(im, 0, y, shade(OTHER_BODY[g], -12))
    for i in range(4):
        dot(im, 4 + i * 2, 6 + (i % 2), OTHER_ACCENT[g])
    return im


def other_front(g):
    body = OTHER_BODY[g]
    im = new(base=body, seed=330 + g, spread=2)
    fill(im, 1, 0, 15, 5, shade(body, 18)); dot(im, 4, 2, '#202020'); dot(im, 4, 1, OTHER_ACCENT[g]); dot(im, 9, 2, '#f0d040'); dot(im, 11, 2, '#40a0f0')
    fill(im, 5, 8, 11, 13, '#2a2a2e'); dot(im, 6, 9, '#ff7030'); dot(im, 9, 9, '#ff7030')
    fill(im, 1, 14, 15, 16, shade(body, -10))
    return im


def other_bezel(g):
    body = OTHER_BODY[g]
    im = new(base=body, seed=340 + g, spread=2)
    fill(im, 1, 0, 15, 3, shade(OTHER_ACCENT[g], -10))
    for x in range(3, 13, 2):
        dot(im, x, 1, '#ffffff')
    fill(im, 2, 4, 14, 13, '#050505')
    return im


def poster(s):
    base = ['#1a1040', '#401018', '#102a18'][s]
    im = new(base=base, seed=270 + s, spread=3)
    if s == 0:  # a rocket, climbing
        fill(im, 7, 3, 9, 11, '#d8d8e0'); dot(im, 7, 2, '#e04030'); dot(im, 8, 2, '#e04030'); fill(im, 6, 11, 10, 13, '#f0a030')
    elif s == 1:  # a house on a hill with one lit window
        fill(im, 4, 7, 12, 13, '#201010'); dot(im, 8, 6, '#201010'); dot(im, 7, 9, '#f0d060')
    else:  # a car, side on, very fast
        fill(im, 3, 9, 13, 11, '#e0e040'); fill(im, 5, 7, 10, 9, '#e0e040'); dot(im, 5, 11, '#101010'); dot(im, 11, 11, '#101010')
        for x in range(0, 3):
            dot(im, x, 9, '#80803a')
    fill(im, 2, 14, 14, 15, '#e0e0e0')
    return im


def bin_side():
    im = new(base='#5a5a60', seed=275, spread=4)
    for x in range(0, 16, 3):
        for y in range(16):
            dot(im, x, y, '#4a4a50')
    return im


def light(lit):
    im = new(base='#e8e8e0' if lit else '#5a5a58', seed=276 + lit, spread=2)
    fill(im, 0, 0, 16, 1, '#8a8a88'); fill(im, 0, 15, 16, 16, '#8a8a88')
    return im


# ------------------------------------------------------------------------------------------------ models
# ------------------------------------------------------------------------------------------------ the pizza counter (0.4.224)
# The owner: "add a pizza and soda booth/vendor, too! describe the pizza as overly greasy, staffed by a teenager with a poor attitude".
LETTERS = {'S': ['111', '100', '111', '001', '111'], 'L': ['100', '100', '100', '100', '111'], 'I': ['111', '010', '010', '010', '111'],
           'C': ['111', '100', '100', '100', '111'], 'E': ['111', '100', '110', '100', '111'], 'O': ['111', '101', '101', '101', '111'],
           'D': ['110', '101', '101', '101', '110'], 'A': ['111', '101', '111', '101', '101'], 'P': ['111', '101', '111', '100', '100'],
           'Z': ['111', '001', '010', '100', '111'], '3': ['111', '001', '111', '001', '111'], '4': ['101', '101', '111', '001', '001']}


def word(im, x, y, text, colour):
    for ch in text:
        if ch == ' ':
            x += 2; continue
        for r, row in enumerate(LETTERS[ch]):
            for c, bit in enumerate(row):
                if bit == '1':
                    dot(im, x + c, y + r, colour)
        x += 4


def pizza_counter(part):
    """The pizza counter: a red laminate front with a cream stripe and a steel kick plate; a speckled top with a grease ring."""
    if part == 'front':
        im = new(base='#9a2a26', seed=290, spread=3)
        fill(im, 0, 5, 16, 7, '#e8dcc0'); fill(im, 0, 7, 16, 8, '#b8ac90')
        fill(im, 0, 13, 16, 16, '#9aa0a8'); fill(im, 0, 13, 16, 14, '#c0c6cc')
        return im
    if part == 'top':
        im = new(base='#e0d6c0', seed=291, spread=3)
        for x, y in [(3, 4), (9, 2), (12, 10), (5, 12), (14, 6)]:
            dot(im, x, y, '#a89a80')
        for x, y in [(6, 7), (7, 6), (8, 6), (9, 7), (9, 8), (8, 9), (7, 9), (6, 8)]:
            dot(im, x, y, '#c8b078')  # where a slice sat
        return im
    im = new(base='#8a2622', seed=292, spread=3)
    fill(im, 0, 13, 16, 16, '#9aa0a8')
    return im


def back_counter(part):
    """Behind the counter: brushed steel doors with their handles."""
    im = new(base='#a8aeb4', seed=293, spread=4)
    for y in range(16):
        dot(im, 0, y, '#7a8086')
    if part == 'front':
        fill(im, 1, 2, 7, 13, '#b6bcc2'); fill(im, 9, 2, 15, 13, '#b6bcc2')
        fill(im, 6, 6, 7, 9, '#5a6066'); fill(im, 9, 6, 10, 9, '#5a6066')
    return im


SLICE = ['#####', '#####', ' ### ', ' ### ', '  #  ']


def warmer(part):
    """A warming case: slices on two racks behind fogged glass, everything shining orange under the lamp, grease pooled on the trays."""
    if part == 'front':
        im = new(base='#2a1c12', seed=294, spread=2)
        for shelf, y0 in enumerate((2, 8)):
            fill(im, 1, y0 + 5, 15, y0 + 6, '#7a7068')  # the tray
            for k in range(3):
                x0 = 1 + k * 5
                for r, row in enumerate(SLICE):
                    for c, bit in enumerate(row):
                        if bit == '#':
                            dot(im, x0 + c, y0 + r, '#c88038' if r == 0 else '#f0b040')
                dot(im, x0 + 1, y0 + 2, '#b02818'); dot(im, x0 + 3, y0 + 1, '#b02818')  # pepperoni
                dot(im, x0 + 2, y0 + 1, '#fff4b0')  # the shine of grease
                dot(im, x0 + 1, y0 + 5, '#d8a048'); dot(im, x0 + 3, y0 + 5, '#d8a048')  # pooled on the tray
        for y in range(16):  # the glass, fogged at the edges
            dot(im, 0, y, '#c8c0b0', 200); dot(im, 15, y, '#c8c0b0', 200)
        fill(im, 0, 0, 16, 1, '#c8c0b0')
        return im
    if part == 'top':
        im = new(base='#9aa0a8', seed=295, spread=3)
        fill(im, 3, 6, 13, 10, '#ffb050')  # the lamp
        return im
    im = new(base='#3a2a1c', seed=296, spread=2)
    fill(im, 1, 1, 15, 15, '#c8a070', 120)
    return im


def soda(part):
    """A soda fountain: three flavour panels over their levers, the ice chute, a drip tray with a puddle."""
    if part == 'front':
        im = new(base='#b8bec4', seed=297, spread=3)
        for i, c in enumerate(('#b02020', '#e0c020', '#5a3018')):
            fill(im, 1 + i * 5, 1, 5 + i * 5, 7, c); fill(im, 2 + i * 5, 2, 4 + i * 5, 4, '#f0f0f0', 180)
            fill(im, 2 + i * 5, 8, 4 + i * 5, 10, '#2a2a2e')  # the lever
        fill(im, 1, 12, 15, 14, '#3a3e44'); fill(im, 4, 13, 9, 14, '#6a4020')  # the drip tray, and what's in it
        return im
    if part == 'top':
        return new(base='#9aa0a8', seed=298, spread=3)
    im = new(base='#b0b6bc', seed=299, spread=3)
    word(im, 1, 5, 'SODA', '#c02828')
    return im


def oven(part):
    """A conveyor pizza oven: brushed steel, a dark mouth with its red glow and the belt running out of it."""
    im = new(base='#9ea4aa', seed=300, spread=4)
    if part == 'front':
        fill(im, 2, 3, 14, 8, '#1a1210'); fill(im, 2, 7, 14, 8, '#c03010')
        for x in range(1, 15, 2):
            dot(im, x, 9, '#3a3a3e')
        fill(im, 12, 11, 15, 13, '#2a2a2e'); dot(im, 13, 12, '#e04020')
    return im


def boxes(part):
    """Pizza boxes stacked flat, the top one printed with a slice and a moustache."""
    im = new(base='#c8a878', seed=301, spread=3)
    if part == 'top':
        fill(im, 1, 1, 15, 15, '#d4b484')
        fill(im, 5, 3, 11, 4, '#f4f0e8'); fill(im, 6, 2, 10, 3, '#f4f0e8')  # the chef's hat
        dot(im, 6, 6, '#3a2010'); dot(im, 9, 6, '#3a2010')
        for x in range(5, 11):
            dot(im, x, 9, '#3a2010')  # and his moustache
        dot(im, 4, 8, '#3a2010'); dot(im, 11, 8, '#3a2010')
        fill(im, 3, 12, 13, 13, '#a02018')
    else:
        for y in (3, 7, 11):
            fill(im, 0, y, 16, y + 1, '#8a6a40')
    return im


def napkins():
    """A chrome napkin dispenser and two shakers."""
    im = new(base='#c0c6cc', seed=302, spread=3)
    fill(im, 4, 2, 12, 6, '#f4f0e8'); fill(im, 1, 9, 7, 15, '#e8e0c8'); fill(im, 9, 9, 15, 15, '#c03020')
    return im


def menu_board():
    """The menu: two things, both in tickets, a slice and a cup each with its price and a ticket's notch beside it."""
    im = new(base='#141212', seed=303, spread=2)
    fill(im, 0, 0, 16, 1, '#6a4a28'); fill(im, 0, 15, 16, 16, '#6a4a28')
    for r, row in enumerate(SLICE):
        for c, bit in enumerate(row):
            if bit == '#':
                dot(im, 2 + c, 2 + r, '#c88038' if r == 0 else '#f0b040')
    dot(im, 3, 4, '#b02818')
    word(im, 9, 2, '4', '#ffd060'); fill(im, 13, 3, 15, 6, '#f08a30')
    fill(im, 3, 9, 6, 14, '#f0ece0'); fill(im, 3, 10, 6, 12, '#c02828'); dot(im, 5, 8, '#e04040')
    word(im, 9, 9, '3', '#ffd060'); fill(im, 13, 10, 15, 13, '#f08a30')
    return im


def slice_icon():
    """A slice on a paper plate gone see-through with grease."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(9, 14):
        fill(im, 2 + (13 - y) // 3, y, 14 - (13 - y) // 3, y + 1, '#e8e4d8', 230)
    for x, y in [(4, 12), (9, 12), (11, 11), (6, 10)]:
        dot(im, x, y, '#c8b890', 200)  # the grease through the plate
    for r in range(9):
        fill(im, 4 + r // 2, 2 + r, 13 - r // 2 - (1 if r > 6 else 0), 3 + r, '#f0b040')
    fill(im, 4, 2, 13, 4, '#c88038')  # the crust
    for x, y in [(6, 5), (10, 5), (8, 8)]:
        dot(im, x, y, '#b02818'); dot(im, x + 1, y, '#901810')
    for x, y in [(7, 4), (9, 7), (11, 4)]:
        dot(im, x, y, '#fff4b0')  # grease shining on top
    dot(im, 8, 11, '#d8a048'); dot(im, 8, 12, '#d8a048')  # a drip off the point
    return im


def soda_icon():
    """A waxed cup with a straw, beaded on the outside."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(4, 15):
        w = 4 - (y - 4) // 4
        fill(im, 8 - w, y, 8 + w, y + 1, '#f0ece0')
    fill(im, 4, 7, 12, 10, '#c02828')
    fill(im, 4, 3, 12, 4, '#e0dcd0')
    fill(im, 9, 0, 10, 4, '#e04040'); dot(im, 10, 0, '#e04040')
    for x, y in [(5, 11), (10, 6), (7, 13)]:
        dot(im, x, y, '#ffffff', 200)
    return im


def element(lo, hi, textures, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = textures.get(face, textures.get('all'))
        if name is None:
            continue
        faces[face] = {'texture': '#' + name}
        if uv and face in uv:
            faces[face]['uv'] = uv[face]
    return {'from': lo, 'to': hi, 'faces': faces}


def model(elements, textures, particle, render='cutout'):
    return {'ambientocclusion': False, 'render_type': 'minecraft:' + render, 'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def fitting_models():
    m = {}
    for g in range(4):
        m[f'cabinet_{g}'] = model([element([1, 0, 1], [15, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'}, {'north': [1, 0, 15, 16]}),
                                  element([1, 13, 0], [15, 15, 1], {'all': 's', 'north': 'p', 'up': 'p'})],
                                 {'s': f'cab_side_{g}', 'f': f'cab_front_{g}', 't': 'cab_top', 'p': f'cab_front_{g}'}, f'cab_side_{g}')
        m[f'screen_{g}'] = model([element([1, 0, 3], [15, 16, 15], {'all': 's', 'north': 'b', 'up': 't', 'down': 't'}),
                                 element([3, 4, 2.9], [13, 12, 3], {'north': 'v'}, {'north': [3, 4, 13, 12]}),
                                 element([1, 13, 1], [15, 16, 3], {'all': 's', 'north': 'b'}, {'north': [1, 0, 15, 3]})],
                                {'s': f'cab_side_{g}', 'b': f'bezel_{g}', 't': 'cab_top', 'v': f'screen_{g}'}, f'cab_side_{g}')
    for s in range(2):
        m[f'dead_{s}'] = model([element([1, 0, 1], [15, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'})],
                              {'s': 'dead_side', 'f': 'dead_front', 't': 'cab_top'}, 'dead_side')
        m[f'dead_screen_{s}'] = model([element([1, 0, 3], [15, 16, 15], {'all': 's', 'north': 'b', 'up': 't', 'down': 't'})],
                                     {'s': 'dead_side', 'b': f'dead_bezel_{s}', 't': 'cab_top'}, 'dead_side')
        m[f'light_{s}'] = model([element([2, 15, 2], [14, 16, 14], {'all': 'l'})], {'l': f'light_{s}'}, f'light_{s}')
    m['change_0'] = model([element([2, 0, 6], [14, 16, 16], {'all': 's', 'north': 'f', 'up': 's'})], {'s': 'change_side', 'f': 'change_front'}, 'change_side')
    m['notice_0'] = model([element([1, 1, 15], [15, 15, 16], {'all': 'e', 'north': 'n'}, {'north': [1, 1, 15, 15]})], {'n': 'notice', 'e': 'notice_edge'}, 'notice')
    m['clipboard_0'] = model([element([4, 0, 3], [12, 1, 13], {'all': 'c', 'up': 'c'}, {'up': [2, 1, 14, 15]})], {'c': 'clipboard'}, 'clipboard')
    for s in range(3):
        m[f'neon_{s}'] = model([element([0, 6, 15.5], [16, 10, 16], {'north': 'n', 'south': 'n'}, {'north': [0, 6, 16, 10], 'south': [0, 6, 16, 10]})], {'n': f'neon_{s}'}, f'neon_{s}', 'translucent')
        m[f'poster_{s}'] = model([element([1, 1, 15.5], [15, 15, 16], {'north': 'p', 'south': 'p'})], {'p': f'poster_{s}'}, f'poster_{s}')
    m['stool_0'] = model([element([7, 0, 7], [9, 8, 9], {'all': 'l'}), element([5, 0, 5], [11, 1, 11], {'all': 'l'}), element([4, 8, 4], [12, 10, 12], {'all': 's', 'up': 't'})],
                         {'l': 'stool_leg', 's': 'stool_top', 't': 'stool_top'}, 'stool_top')
    m['counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'counter_side', 'f': 'counter_front', 't': 'counter_top'}, 'counter_front')
    for k in range(8):
        m[f'prizes_{k}'] = model([element([0, 0, 8], [16, 16, 16], {'all': 's', 'north': 'f'})], {'s': 'prizes_side', 'f': f'prizes_{k}'}, f'prizes_{k}')
    for h in range(2):
        m[f'brawl_{h}'] = model([element([0, 0, 1], [16, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'}),
                                 element([0, 13, 0], [16, 15, 1], {'all': 's', 'north': 'f', 'up': 'f'})],
                                {'s': 'brawl_side', 'f': f'brawl_front_{h}', 't': 'cab_top'}, 'brawl_side')
        m[f'brawl_screen_{h}'] = model([element([0, 0, 3], [16, 16, 15], {'all': 's', 'north': 's', 'up': 't', 'down': 't'}),
                                        element([0 if h else 1, 3, 2.9], [15 if h == 0 else 16, 12, 3], {'north': 'v'}, {'north': [0, 3, 16, 12]}),
                                        element([0, 12, 1], [16, 16, 3], {'all': 's', 'north': 'm'}, {'north': [0, 0, 16, 16]})],
                                       {'s': 'brawl_side', 'm': f'brawl_marquee_{h}', 't': 'cab_top', 'v': f'brawl_screen_{h}'}, 'brawl_side')
    for g in range(3):
        m[f'other_{g}'] = model([element([1, 0, 1], [15, 16, 15], {'all': 's', 'north': 'f', 'up': 't', 'down': 't'}),
                                 element([1, 13, 0], [15, 15, 1], {'all': 's', 'north': 'f', 'up': 'f'})],
                                {'s': f'other_side_{g}', 'f': f'other_front_{g}', 't': 'cab_top'}, f'other_side_{g}')
        m[f'other_screen_{g}'] = model([element([1, 0, 3], [15, 16, 15], {'all': 's', 'north': 'b', 'up': 't', 'down': 't'}),
                                        element([3, 4, 2.9], [13, 12, 3], {'north': 'v'}, {'north': [3, 4, 13, 12]}),
                                        element([1, 13, 1], [15, 16, 3], {'all': 's', 'north': 'b'}, {'north': [1, 0, 15, 3]})],
                                       {'s': f'other_side_{g}', 'b': f'other_bezel_{g}', 't': 'cab_top', 'v': f'other_screen_{g}'}, f'other_side_{g}')
    m['bin_0'] = model([element([4, 0, 4], [12, 12, 12], {'all': 's', 'up': 'i'})], {'s': 'bin_side', 'i': 'bin_inside'}, 'bin_side')
    # 0.4.224: the pizza counter.
    m['pizza_counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'pizza_counter_side', 'f': 'pizza_counter_front', 't': 'pizza_counter_top'}, 'pizza_counter_front')
    m['back_counter_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 's', 'north': 'f', 'up': 't'})], {'s': 'back_counter_side', 'f': 'back_counter_front', 't': 'warmer_top'}, 'back_counter_front')
    m['pizza_warmer_0'] = model([element([1, 0, 3], [15, 11, 13], {'all': 's', 'north': 'f', 'south': 'f', 'up': 't', 'down': 't'}),
                                 element([1, 11, 3], [15, 12, 13], {'all': 't'})], {'s': 'warmer_side', 'f': 'warmer_front', 't': 'warmer_top'}, 'warmer_front')
    m['soda_0'] = model([element([1, 0, 3], [15, 14, 15], {'all': 's', 'north': 'f', 'up': 't'}),
                         element([3, 6, 2], [13, 7, 3], {'all': 't'})], {'s': 'soda_side', 'f': 'soda_front', 't': 'soda_top'}, 'soda_front')
    m['pizza_oven_0'] = model([element([0, 0, 1], [16, 10, 15], {'all': 's', 'north': 'f'}),
                              element([0, 0, 0], [16, 1, 1], {'all': 'f'})], {'s': 'oven_side', 'f': 'oven_front'}, 'oven_front')
    m['pizza_boxes_0'] = model([element([2, 0, 2], [14, 7, 14], {'all': 's', 'up': 't', 'down': 't'})], {'s': 'boxes_side', 't': 'boxes_top'}, 'boxes_top')
    m['napkins_0'] = model([element([3, 0, 6], [8, 6, 10], {'all': 'n'}), element([9, 0, 6], [11, 4, 8], {'all': 'n'}), element([12, 0, 7], [14, 4, 9], {'all': 'n'})],
                           {'n': 'napkins'}, 'napkins')
    m['menu_board_0'] = model([element([0, 2, 15.5], [16, 14, 16], {'north': 'm', 'south': 'm'}, {'north': [0, 2, 16, 14], 'south': [0, 2, 16, 14]})], {'m': 'menu_board'}, 'menu_board')
    return m


def textures():
    out = {}
    for k, v in materials().items():
        out[f'block/polybius/{k}.png'] = v
    for g in range(4):
        out[f'block/polybius/cab_side_{g}.png'] = cab_side(g)
        out[f'block/polybius/cab_front_{g}.png'] = cab_front(g)
        out[f'block/polybius/bezel_{g}.png'] = bezel(g)
        out[f'block/polybius/screen_{g}.png'] = screen_frames(g)
    out['block/polybius/cab_top.png'] = new(base='#181418', seed=280, spread=2)
    out['block/polybius/dead_side.png'] = new(base='#3a3430', seed=281, spread=4)
    out['block/polybius/dead_front.png'] = cab_front(0).convert('LA').convert('RGBA')
    for s in range(2):
        out[f'block/polybius/dead_bezel_{s}.png'] = bezel(0, True, s)
        out[f'block/polybius/light_{s}.png'] = light(s == 0)
    out['block/polybius/change_front.png'] = change('front'); out['block/polybius/change_side.png'] = change('side')
    out['block/polybius/notice.png'] = notice(); out['block/polybius/notice_edge.png'] = new(base='#5a3a20', seed=282, spread=3)
    out['block/polybius/clipboard.png'] = clipboard()
    for s in range(3):
        out[f'block/polybius/neon_{s}.png'] = neon(NEON[s])
        out[f'block/polybius/poster_{s}.png'] = poster(s)
    out['block/polybius/stool_top.png'] = stool('top'); out['block/polybius/stool_leg.png'] = stool('leg')
    out['block/polybius/counter_front.png'] = counter('front'); out['block/polybius/counter_top.png'] = counter('top'); out['block/polybius/counter_side.png'] = new(base='#2a2620', seed=283, spread=3)
    for k in range(8):
        out[f'block/polybius/prizes_{k}.png'] = prizes(k)
        out[f'item/polybius_prize_{PRIZE_NAMES[k]}.png'] = prize_icon(k)
    out['item/polybius_tickets.png'] = tickets()
    for m in range(4):
        out[f'item/polybius_prize_mood_ring_{m}.png'] = prize_icon(6, mood=m)
    out['item/polybius_prize_slap_bracelet_straight.png'] = bracelet_straight()
    for level in range(1, 4):
        out[f'item/polybius_prize_finger_trap_pull{level}.png'] = trap_pulled(level)
    out['entity/prize_wear.png'] = wear()
    out['block/polybius/prizes_side.png'] = new(base='#3a2a1e', seed=284, spread=3)
    for h in range(2):
        out[f'block/polybius/brawl_front_{h}.png'] = brawl_front(h); out[f'block/polybius/brawl_marquee_{h}.png'] = brawl_marquee(h)
        out[f'block/polybius/brawl_screen_{h}.png'] = brawl_frames(h)
    out['block/polybius/brawl_side.png'] = brawl_side()
    for g in range(3):
        out[f'block/polybius/other_side_{g}.png'] = other_side(g); out[f'block/polybius/other_front_{g}.png'] = other_front(g)
        out[f'block/polybius/other_bezel_{g}.png'] = other_bezel(g); out[f'block/polybius/other_screen_{g}.png'] = other_frames(g)
    out['block/polybius/bin_side.png'] = bin_side(); out['block/polybius/bin_inside.png'] = new(base='#1a1a1c', seed=285, spread=2)
    for part in ('front', 'top', 'side'):
        out[f'block/polybius/pizza_counter_{part}.png'] = pizza_counter(part)
        out[f'block/polybius/warmer_{part}.png'] = warmer(part)
        out[f'block/polybius/soda_{part}.png'] = soda(part)
    out['block/polybius/back_counter_front.png'] = back_counter('front'); out['block/polybius/back_counter_side.png'] = back_counter('side')
    out['block/polybius/oven_front.png'] = oven('front'); out['block/polybius/oven_side.png'] = oven('side')
    out['block/polybius/boxes_top.png'] = boxes('top'); out['block/polybius/boxes_side.png'] = boxes('side')
    out['block/polybius/napkins.png'] = napkins(); out['block/polybius/menu_board.png'] = menu_board()
    out['item/polybius_pizza.png'] = slice_icon(); out['item/polybius_soda.png'] = soda_icon()
    return out


KINDS = ['cabinet', 'screen', 'dead', 'dead_screen', 'change', 'notice', 'clipboard', 'neon', 'stool', 'counter', 'prizes', 'poster', 'bin', 'light',
         'brawl', 'brawl_screen', 'other', 'other_screen', 'pizza_counter', 'back_counter', 'pizza_warmer', 'soda', 'pizza_oven', 'pizza_boxes',
         'napkins', 'menu_board']
STAGES = {'cabinet': 4, 'screen': 4, 'neon': 3, 'poster': 3, 'dead': 2, 'dead_screen': 2, 'light': 2, 'prizes': 8, 'brawl': 2, 'brawl_screen': 2,
          'other': 3, 'other_screen': 3}
ITEM_NAMES = {'polybius_tickets': 'Prize Tickets', 'polybius_prize_spider': 'Rubber Spider', 'polybius_prize_finger_trap': 'Finger Trap',
              'polybius_prize_bouncy_ball': 'Bouncy Ball', 'polybius_prize_glow_star': 'Glow Star', 'polybius_prize_slap_bracelet': 'Slap Bracelet',
              'polybius_prize_kazoo': 'Kazoo', 'polybius_prize_mood_ring': 'Mood Ring', 'polybius_prize_plush_dog': 'Plush Dog with One Eye',
              'polybius_pizza': 'Slice of Pizza', 'polybius_soda': 'Fountain Soda'}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'polybius_fitting': 'Arcade Fitting', 'polybius_carpet': 'Arcade Carpet', 'polybius_wall': 'Arcade Wall', 'polybius_ceiling': 'Arcade Ceiling'}


def files():
    out = {}
    for name in materials():
        out[f'models/block/polybius_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': P + name}}
        out[f'blockstates/polybius_{name}.json'] = {'variants': {'': {'model': NS + f'block/polybius_{name}'}}}
    for key, obj in fitting_models().items():
        out[f'models/block/polybius/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(8):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/polybius/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/polybius_fitting.json'] = {'variants': variants}
    for g in range(4):
        out[f'textures/block/polybius/screen_{g}.png.mcmeta'] = BLACK_META if g == 3 else SCREEN_META
    for h in range(2):
        out[f'textures/block/polybius/brawl_screen_{h}.png.mcmeta'] = SCREEN_META
    for g in range(3):
        out[f'textures/block/polybius/other_screen_{g}.png.mcmeta'] = SCREEN_META
    for item in ITEM_NAMES:
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}'}}
    for item, prop, variants in OVERRIDES:
        for _, suffix in variants:
            out[f'models/item/{item}_{suffix}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/{item}_{suffix}'}}
        out[f'models/item/{item}.json']['overrides'] = [{'predicate': {NS + prop: value}, 'model': NS + f'item/{item}_{suffix}'} for value, suffix in variants]
    return out


# 0.4.411: the ring's stone follows its wearer (black, amber, green, blue, violet as the value rises), the bracelet straightens while
# held up, and the finger trap stretches with each pull.
OVERRIDES = [
    ('polybius_prize_mood_ring', 'mood', [(0.25, '3'), (0.5, '2'), (0.75, '1'), (1.0, '0')]),
    ('polybius_prize_slap_bracelet', 'straight', [(1.0, 'straight')]),
    ('polybius_prize_finger_trap', 'pull', [(0.3, 'pull1'), (0.6, 'pull2'), (0.95, 'pull3')]),
]


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
            assert f'block/polybius/{tex.split("polybius/")[1]}.png' in images, (key, tex)
    for item in ITEM_NAMES:
        assert f'item/{item}.png' in images, item
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
    for block, name in list(NAMES.items()) + [('item:' + k, v) for k, v in ITEM_NAMES.items()]:
        key = f'item.the_oldest_house.{block[5:]}' if block.startswith('item:') else f'block.the_oldest_house.{block}'
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the arcade: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
