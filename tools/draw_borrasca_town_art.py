"""Borrasca's town (0.4.422): the shops along Drisking's street.

Draws, in native Minecraft pixels from fixed seeds: three surfaces (the diner's black and white floor, the linoleum of the doctor's
office and the drugstore, the antiques shop's pressed tin ceiling) and every fitting of the borrasca_shop block, its models facing
north and turned by its facing. The diner's pie case (stage 1, one pie cut), coffee urn and letter board; the hardware pegboard
(stage 1, a flashlight gone from its hook); the gas pump's two halves, the price board, the payphone, the soda cooler and the candy
rack; the antiques shop's glass case, the miner's helmet, the map of the old workings, framed photographs and a wall clock; the
doctor's beam scale, lollipop jar (stage 1, nearly empty), corkboard of birth cards, examination table and eye chart; the
drugstore's soda fountain, pharmacy shelf and comic spinner; and a lit OPEN sign. 0.4.430: the gas station's own stock (steel
shelving of motor oil and antifreeze, a bait freezer with cups of night crawlers, a wire rack of chips, the feed store's calendar, a
painted bait sign and a stack of used tires); the general store's shelf of tins, boxes and flour, and the diner's shelf of plates,
mugs and bowls. 0.4.431: a child's bike, on its kickstand (stage 0, Kendra's) and dropped on its side (stage 1, Patrick's). 0.4.425: the things the town sells (pie,
coffee, coffee mostly milk, soda, candy, a lollipop, a malt, beer) and the money to buy them with (a child's change, a wallet), as
items. Writes the models and block states and the
blocks' names. --check verifies everything.
"""
from pathlib import Path
import argparse
import json
import random
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
P = NS + 'block/borrasca_shop/'


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k=0, a=255):
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


def frame(im, colour, x0=0, y0=0, x1=16, y1=16):
    for x in range(x0, x1):
        dot(im, x, y0, colour); dot(im, x, y1 - 1, colour)
    for y in range(y0, y1):
        dot(im, x0, y, colour); dot(im, x1 - 1, y, colour)


# A three-by-five pixel alphabet for the boards, enough for what they say.
FONT = {
    'A': '010101111101101', 'B': '110101110101110', 'C': '011100100100011', 'D': '110101101101110', 'E': '111100110100111',
    'F': '111100110100100', 'H': '101101111101101', 'I': '111010010010111', 'L': '100100100100111', 'N': '110101101101101',
    'O': '010101101101010', 'P': '110101110100100', 'R': '110101110110101', 'S': '011100010001110', 'T': '111010010010010',
    'U': '101101101101111', 'Y': '101101010010010', 'E2': '111100111100111', '1': '010110010010111', '2': '110001010100111',
    '3': '110001010001110', '4': '101101111001001', '5': '111100110001110', '9': '111101111001110', '0': '111101101101111',
    '.': '000000000000010', '-': '000000111000000', ' ': '000000000000000',
    'V': '101101101101010', 'K': '101101110101101', 'G': '011100101101011', 'M': '101111111101101', 'W': '101101101111101',
}


def text(im, x, y, word, colour, step=4):
    for ch in word:
        bits = FONT[ch]
        for i, b in enumerate(bits):
            if b == '1':
                dot(im, x + i % 3, y + i // 3, colour)
        x += step


# ------------------------------------------------------------------------------------------------ surfaces
def checker():
    im = new()
    rnd = random.Random(42201); px = im.load()
    for y in range(16):
        for x in range(16):
            dark = (x // 8 + y // 8) % 2 == 1
            px[x, y] = shade('#2c2b30' if dark else '#e6e1d3', rnd.randint(-5, 5))
    for _ in range(6):
        dot(im, rnd.randrange(16), rnd.randrange(16), '#b9b3a3')
    return im


def lino():
    im = new('#b7c2ad', 42202, 4); rnd = random.Random(42203)
    for _ in range(26):
        dot(im, rnd.randrange(16), rnd.randrange(16), rnd.choice(('#9aa691', '#cdd5c4', '#a7b49d', '#8d9884')))
    for i in range(16):
        dot(im, i, 15, '#9aa38f'); dot(im, 15, i, '#9aa38f')
    return im


def tin():
    im = new('#a9aba6', 42204, 3)
    for x in range(16):
        for y in range(16):
            if x in (0, 8) or y in (0, 8):
                dot(im, x, y, '#8b8d88')
            elif x in (1, 9) or y in (1, 9):
                dot(im, x, y, '#c2c4bf')
    for cx, cy in ((4, 4), (12, 4), (4, 12), (12, 12)):
        dot(im, cx, cy, '#c9cbc6'); dot(im, cx + 1, cy, '#93958f'); dot(im, cx, cy + 1, '#93958f'); dot(im, cx - 1, cy, '#c9cbc6'); dot(im, cx, cy - 1, '#c9cbc6')
    return im


# ------------------------------------------------------------------------------------------------ shared faces
def wood():
    im = new('#5e4128', 42210, 5); rnd = random.Random(42211)
    for y in range(0, 16, 4):
        for x in range(16):
            if rnd.random() < .5:
                dot(im, x, y, '#4f3620')
    return im


def chrome():
    im = new('#c6c9cb', 42212, 3)
    for y in range(16):
        dot(im, 2, y, '#e4e6e8'); dot(im, 3, y, '#e4e6e8'); dot(im, 12, y, '#9ea2a5')
    return im


def glass():
    im = new()
    fill(im, 0, 0, 16, 16, '#d8eef2', 40)
    frame(im, '#e9f6f8', 0, 0, 16, 16)
    for i in range(3, 8):
        dot(im, i, 10 - i, '#ffffff', 120)
    return im


def enamel():
    return new('#ecebe5', 42213, 3)


def black():
    return new('#26252a', 42214, 3)


# ------------------------------------------------------------------------------------------------ the diner
def pie(cut):
    im = new('#c8873e', 42220, 6)
    # A lattice of crust over cherries, a fluted rim.
    for i in range(16):
        dot(im, i, 0, '#a86a2a'); dot(im, i, 15, '#a86a2a'); dot(im, 0, i, '#a86a2a'); dot(im, 15, i, '#a86a2a')
    for y in range(2, 14):
        for x in range(2, 14):
            if (x + y) % 4 == 0 or (x - y) % 4 == 0:
                dot(im, x, y, '#e2aa5e')
            else:
                dot(im, x, y, '#8e2330' if (x * 7 + y * 3) % 5 else '#6f1a25')
    if cut:
        fill(im, 8, 8, 16, 16, '#000000', 0)
        for i in range(8, 16):
            dot(im, i, 8, '#e2aa5e'); dot(im, 8, i, '#e2aa5e')
    return im


def crust():
    im = new('#b77a35', 42221, 6)
    for x in range(16):
        dot(im, x, 0, '#e2aa5e'); dot(im, x, 1, '#c98b44')
    return im


def urn():
    im = new('#b9bdbf', 42222, 3)
    for y in range(16):
        dot(im, 3, y, '#dfe2e4'); dot(im, 12, y, '#8f9396')
    fill(im, 7, 3, 9, 11, '#2a1a10')  # the gauge glass, coffee in it
    fill(im, 7, 3, 9, 5, '#d6cdbf')
    fill(im, 5, 13, 11, 15, '#2b2b2b')
    return im


def menu():
    im = black()
    frame(im, '#6a4a2e')
    text(im, 2, 2, 'PIE', '#f2efe6')
    text(im, 2, 8, 'COF', '#f2efe6')
    for x in range(2, 14, 2):
        dot(im, x, 14, '#bdb9ae')
    dot(im, 14, 3, '#d9c27a'); dot(im, 14, 9, '#d9c27a')
    return im


# ------------------------------------------------------------------------------------------------ the hardware counter
def pegboard(taken):
    im = new('#b48d5c', 42230, 4)
    for y in range(1, 16, 3):
        for x in range(1, 16, 3):
            dot(im, x, y, '#7a5c37')
    # A coil of rope.
    for x, y in ((2, 2), (3, 1), (4, 1), (5, 2), (5, 3), (4, 4), (3, 4), (2, 3), (3, 2), (4, 3)):
        dot(im, x, y, '#a07c45')
    dot(im, 3, 3, '#6b5230'); dot(im, 4, 2, '#d1ad6a')
    # Two flashlights; the second is taken when stage 1 is drawn.
    for i, x in enumerate((9, 12)):
        if taken and i == 1:
            dot(im, x, 1, '#555555'); continue
        dot(im, x, 1, '#555555'); fill(im, x - 1, 2, x + 1, 6, '#b8312a'); fill(im, x - 1, 6, x + 1, 7, '#d4d4cc')
    # Batteries on a card.
    fill(im, 2, 8, 7, 13, '#e6dfcf')
    for x in (3, 5):
        fill(im, x, 9, x + 1, 12, '#2c2c2c'); dot(im, x, 9, '#c58b2f')
    # A pocketknife.
    fill(im, 9, 10, 14, 11, '#9e2a22'); fill(im, 9, 9, 12, 10, '#d4d6d8')
    return im


# ------------------------------------------------------------------------------------------------ the gas station
def pump_front():
    im = new('#b32a26', 42240, 4)
    fill(im, 3, 2, 13, 9, '#ece7da'); frame(im, '#7e1d1a', 3, 2, 13, 9)
    text(im, 4, 3, '1', '#2a2a2a'); text(im, 8, 3, '9', '#2a2a2a')
    fill(im, 5, 11, 11, 15, '#4a1513')
    return im


def pump_side():
    im = new('#a8251f', 42241, 4)
    fill(im, 6, 3, 10, 13, '#1e1e1e')  # the hose hangs in its holster
    return im


def pump_head():
    im = new('#b32a26', 42242, 4)
    fill(im, 2, 3, 14, 11, '#ece7da'); frame(im, '#7e1d1a', 2, 3, 14, 11)
    text(im, 3, 5, '0', '#2a2a2a'); text(im, 7, 5, '9', '#2a2a2a'); text(im, 11, 5, '9', '#cc2222')
    return im


def globe():
    # Milk glass with a red band round it.
    im = new('#f1efe7', 42243, 3)
    fill(im, 0, 6, 16, 10, '#c0302a'); fill(im, 0, 7, 16, 8, '#e05a4a')
    return im


def price():
    im = new('#f0ece2', 42244, 3); frame(im, '#3a3a3a')
    text(im, 2, 2, '1', '#c0302a'); text(im, 6, 2, '0', '#c0302a'); text(im, 10, 2, '9', '#c0302a')
    text(im, 2, 9, '1', '#2a2a2a'); text(im, 6, 9, '1', '#2a2a2a'); text(im, 10, 9, '4', '#2a2a2a')
    return im


def payphone():
    im = new('#b8bcbe', 42245, 3); frame(im, '#7a7e81')
    fill(im, 2, 2, 7, 13, '#1f1f22')  # the handset in its cradle
    dot(im, 3, 3, '#3a3a3e'); dot(im, 3, 12, '#3a3a3e')
    for row in range(4):
        for col in range(3):
            dot(im, 9 + col * 2, 5 + row * 2, '#2d2d30')
    fill(im, 9, 2, 14, 4, '#d9d3b8')  # the instruction card
    return im


def cooler_front():
    # A plain red cooler, ICE stencilled on a white band; no maker's mark.
    im = new('#b8312b', 42246, 4)
    for x in range(16):
        dot(im, x, 1, '#8b1e1a'); dot(im, x, 14, '#8b1e1a')
    fill(im, 1, 4, 15, 11, '#f2ede2'); text(im, 3, 5, 'I', '#8b1e1a'); text(im, 7, 5, 'C', '#8b1e1a'); text(im, 11, 5, 'E', '#8b1e1a')
    fill(im, 6, 12, 10, 13, '#d0d0d0')
    return im


def cooler_top():
    im = new('#b52a24', 42247, 3)
    fill(im, 5, 7, 11, 9, '#cfcfcf')
    return im


def candy():
    im = new()
    fill(im, 0, 0, 16, 16, '#6b6f72', 255)
    rnd = random.Random(42248)
    for row in range(4):
        y = 1 + row * 4
        fill(im, 1, y + 3, 15, y + 4, '#9ea2a5')
        for x in range(1, 15, 2):
            fill(im, x, y, x + 2, y + 3, rnd.choice(('#7b3f1e', '#d8b23a', '#c0302a', '#2f6fb3', '#3b8a3e', '#f0e6d2')))
            dot(im, x + 1, y, '#e8e3d6')
    return im


# ------------------------------------------------------------------------------------------------ the antiques shop and the society
def velvet():
    return new('#5a1f2c', 42250, 4)


def curios():
    im = velvet()
    # A pocket watch, two spoons, an arrowhead and a pair of spectacles on the velvet.
    for x, y in ((3, 3), (4, 2), (5, 3), (4, 4)):
        dot(im, x, y, '#d4b24c')
    dot(im, 4, 3, '#f0e8c8')
    for i in range(5):
        dot(im, 9, 2 + i, '#c8cbcd'); dot(im, 12, 2 + i, '#c8cbcd')
    dot(im, 9, 1, '#e3e5e6'); dot(im, 12, 1, '#e3e5e6')
    for x, y in ((3, 11), (4, 10), (5, 11), (4, 12), (4, 11)):
        dot(im, x, y, '#6e6a63')
    for x in (9, 10, 12, 13):
        dot(im, x, 11, '#2b2b2b')
    dot(im, 11, 11, '#9a9a9a')
    return im


def helmet():
    im = new('#3f4a3a', 42251, 4)
    for x in range(16):
        dot(im, x, 15, '#2e372b')
    return im


def lamp():
    im = new('#a07a3c', 42252, 3)
    fill(im, 4, 4, 12, 12, '#e8d9a0'); fill(im, 6, 6, 10, 10, '#fff6cf')
    return im


def mine_map():
    im = new('#d9ccaa', 42253, 4); frame(im, '#4a321e')
    for x, y in ((2, 13), (3, 12), (4, 11), (5, 11), (6, 10), (7, 9), (7, 8), (8, 7), (9, 6), (10, 5), (11, 4), (12, 3)):
        dot(im, x, y, '#4a4237')
    for x, y in ((5, 11), (5, 12), (5, 13), (9, 6), (10, 7), (11, 8), (12, 8)):
        dot(im, x, y, '#4a4237')
    dot(im, 12, 2, '#a3271f'); dot(im, 13, 3, '#a3271f'); dot(im, 13, 2, '#a3271f'); dot(im, 12, 3, '#a3271f')
    return im


def photos():
    # Three framed photographs: two people on a porch, a man with a lamp on his hat, a row of children.
    im = new('#6b4f37', 42254, 3)
    for x0, y0, w, h in ((1, 1, 6, 6), (9, 1, 6, 6), (2, 9, 12, 6)):
        fill(im, x0, y0, x0 + w, y0 + h, '#2c2118'); fill(im, x0 + 1, y0 + 1, x0 + w - 1, y0 + h - 1, '#c9b48e')
    for x in (2, 4):
        dot(im, x, 2, '#4a3a28'); fill(im, x, 3, x + 1, 6, '#5a4630')
    dot(im, 11, 2, '#f0e0a0'); dot(im, 11, 3, '#4a3a28'); fill(im, 11, 4, 13, 6, '#5a4630'); dot(im, 10, 2, '#5a4630'); dot(im, 12, 2, '#5a4630')
    for x in range(3, 13, 2):
        dot(im, x, 11, '#4a3a28'); fill(im, x, 12, x + 1, 14, '#5a4630')
    return im


def clock():
    im = new()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 7.5:
                im.putpixel((x, y), shade('#3e2a18' if d > 6.2 else '#efe8d6', 0))
    for i in range(3, 8):
        dot(im, 7, i, '#1d1d1d')
    for i in range(8, 11):
        dot(im, i, 8, '#1d1d1d')
    return im


# ------------------------------------------------------------------------------------------------ the doctor's office
def scale_face():
    im = enamel()
    fill(im, 6, 0, 10, 16, '#dedcd5')
    fill(im, 1, 1, 15, 3, '#2a2a2a')
    for x in range(2, 14, 2):
        dot(im, x, 2, '#c9c9c9')
    return im


def jar(low):
    im = new()
    fill(im, 0, 0, 16, 16, '#d8eef2', 50)
    rnd = random.Random(42260)
    top = 9 if low else 3
    for y in range(top, 15, 2):
        for x in range(1, 15, 2):
            if rnd.random() < .85:
                c = rnd.choice(('#d6402f', '#f2a43a', '#5ab04e', '#8a55c4', '#e86fa6', '#f4e04a'))
                fill(im, x, y, x + 2, y + 2, c)
                dot(im, x + 1, y + 2, '#f3efe6')
    frame(im, '#eaf6f8')
    return im


def cards():
    im = new('#b88d58', 42261, 5); frame(im, '#6b4a2a')
    rnd = random.Random(42262)
    for row in range(3):
        for col in range(3):
            x, y = 1 + col * 5, 1 + row * 5
            fill(im, x, y, x + 4, y + 4, rnd.choice(('#f3c6d2', '#bcd6f2', '#f3c6d2', '#bcd6f2', '#f3ecd0')))
            dot(im, x + 1, y + 1, '#8a8a8a'); dot(im, x + 2, y + 2, '#a0a0a0')
            dot(im, x + 2, y, '#c0302a')
    return im


def exam_top():
    im = new('#6f8a72', 42263, 4)
    fill(im, 4, 0, 12, 16, '#f1efe8')
    return im


def exam_side():
    im = new('#6f8a72', 42264, 4)
    fill(im, 0, 12, 16, 16, '#b9bdbf')
    return im


def chart():
    im = new('#f4f2ec', 42265, 2); frame(im, '#9a9a9a')
    text(im, 6, 1, 'E', '#1d1d1d')
    for x in (4, 9):
        text(im, x, 7, 'F' if x == 4 else 'P', '#1d1d1d')
    for x in range(2, 14, 2):
        dot(im, x, 13, '#1d1d1d')
    return im


# ------------------------------------------------------------------------------------------------ the drugstore
def fountain():
    im = chrome()
    for x in (3, 7, 11):
        fill(im, x, 1, x + 2, 6, '#2b2b2b'); dot(im, x, 1, '#c0302a')
    fill(im, 0, 12, 16, 16, '#d8d4c8')
    return im


def meds():
    im = wood(); rnd = random.Random(42270)
    for row in range(3):
        y = 1 + row * 5
        fill(im, 0, y + 4, 16, y + 5, '#3b2817')
        x = 1
        while x < 14:
            w = rnd.choice((2, 2, 3))
            c = rnd.choice(('#a35a1f', '#f1ede4', '#7aa0c4', '#a35a1f', '#d8c87a'))
            fill(im, x, y + 1, x + w, y + 4, c); dot(im, x, y, '#f1ede4')
            x += w + 1
    return im


def rack():
    im = new('#6b6f72', 42271, 3); rnd = random.Random(42272)
    for row in range(3):
        for col in range(3):
            x, y = 1 + col * 5, 1 + row * 5
            c = rnd.choice(('#d6402f', '#2f6fb3', '#f4d03a', '#3b8a3e', '#8a55c4'))
            fill(im, x, y, x + 4, y + 4, c); fill(im, x, y, x + 4, y + 1, '#f2ede2')
    return im


def open_sign():
    im = new('#1b1b20', 42273, 2)
    text(im, 1, 5, 'O', '#ff4a3a'); text(im, 5, 5, 'P', '#ff4a3a'); text(im, 9, 5, 'E', '#ff4a3a'); text(im, 13, 5, 'N', '#ff4a3a', 3)
    return im


# ------------------------------------------------------------------------------------------------ 0.4.430: the station's own stock
def steel():
    im = new('#7f8589', 42280, 3)
    for y in range(16):
        dot(im, 0, y, '#9aa0a4'); dot(im, 15, y, '#5f6467')
    return im


def oil(base, band, cap):
    """Quarts of motor oil, one can in each four-by-eight cell: its cap, a coloured body and the band round it (no maker's name)."""
    im = new(); rnd = random.Random(42281 + len(base))
    for y0 in (0, 8):
        for x0 in range(0, 16, 4):
            fill(im, x0, y0, x0 + 4, y0 + 8, base)
            fill(im, x0, y0, x0 + 4, y0 + 1, cap); dot(im, x0 + 1, y0, '#e2e2e2')
            fill(im, x0, y0 + 3, x0 + 4, y0 + 5, band)
            for y in range(y0 + 1, y0 + 8):
                dot(im, x0, y, shade(base, 18)[:3]); dot(im, x0 + 3, y, shade(base, -22)[:3])
            dot(im, x0 + 2, y0 + rnd.randint(5, 7), shade(base, -30)[:3])
    return im


def jug():
    """Gallon jugs of antifreeze: green, a white label, the handle's hollow dark."""
    im = new('#3f9a4a', 42283, 4)
    fill(im, 0, 0, 16, 2, '#e9e6dd'); fill(im, 2, 6, 14, 11, '#efeee8'); fill(im, 4, 7, 12, 10, '#2c6e35')
    fill(im, 10, 2, 14, 5, '#2a6b33')
    return im


def bait_front():
    """A chest freezer, white enamel with a blue band: LIVE over BAIT."""
    im = new('#ecebe5', 42284, 3)
    fill(im, 0, 2, 16, 15, '#2f5f9e')
    text(im, 1, 3, 'LIVE', '#f2ede2', 4); text(im, 1, 9, 'BAIT', '#f2ede2', 4)
    for x in range(16):
        dot(im, x, 0, '#c9c8c2'); dot(im, x, 15, '#b9b8b2')
    return im


def bait_top():
    im = new('#e8e7e1', 42285, 3)
    for x in range(16):
        dot(im, x, 7, '#bdbcb6')
    fill(im, 6, 13, 10, 15, '#9ea2a5')
    return im


def cup():
    """A styrofoam cup of night crawlers, a paper lid pressed on."""
    im = new('#f4f1ea', 42286, 2)
    fill(im, 0, 0, 16, 3, '#d9cfae'); fill(im, 0, 3, 16, 4, '#bfb593')
    return im


def chips():
    """Bags of chips on a wire rack: a bright bag, a white window, a crimped top."""
    im = new(); rnd = random.Random(42287)
    for x0 in range(0, 16, 4):
        c = rnd.choice(('#d6402f', '#f4b52a', '#2f6fb3', '#3b8a3e', '#c95a1f', '#8a55c4'))
        fill(im, x0, 0, x0 + 4, 16, c)
        for x in range(x0, x0 + 4, 2):
            dot(im, x, 0, '#f2ede2')
        fill(im, x0 + 1, 6, x0 + 3, 10, '#f2ede2'); dot(im, x0 + 2, 8, '#e3b26a')
        for y in range(1, 16):
            dot(im, x0 + 3, y, shade(c, -30)[:3])
    return im


def calendar():
    """A feed store's calendar: a largemouth bass leaping over the water above, the month's grid below, one square marked."""
    im = new('#f2efe6', 42288, 2)
    fill(im, 1, 1, 15, 8, '#7fb2d6'); fill(im, 1, 6, 15, 8, '#3c6f93')
    for x, y in ((4, 4), (5, 3), (6, 3), (7, 3), (8, 3), (9, 4), (10, 3), (11, 2), (11, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 5)):
        dot(im, x, y, '#4f7a3a')
    dot(im, 5, 3, '#1b1b1b')
    for row in range(3):
        for col in range(5):
            dot(im, 2 + col * 3, 10 + row * 2, '#8a8780')
    dot(im, 8, 12, '#c0302a'); dot(im, 9, 12, '#c0302a')
    fill(im, 7, 0, 9, 1, '#26252a')
    return im


def bait_sign():
    """A painted board: WORMS over BAIT, and the arrow to the freezer."""
    im = new('#efe7d0', 42289, 3)
    text(im, 1, 2, 'WORM', '#7a2a1e', 4); text(im, 1, 8, 'BAIT', '#2f5f9e', 4)
    fill(im, 6, 14, 10, 15, '#7a2a1e')
    return im


def tire():
    """A worn tire's tread, its grooves gone shallow, and its sidewall."""
    im = new('#2a292c', 42290, 4)
    for x in range(16):
        if x % 3 == 0:
            for y in range(16):
                dot(im, x, y, '#1a1a1c')
    for y in (0, 15):
        for x in range(16):
            dot(im, x, y, '#3a393d')
    return im


def tire_top():
    im = new('#2d2c2f', 42291, 3)
    fill(im, 5, 5, 11, 11, '#141416'); frame(im, '#3e3d41', 4, 4, 12, 12)
    return im


def tins():
    """Tins of soup and beans on the store's shelf, one in each four-by-eight cell: a red top, a white band, a silver rim."""
    im = new(); rnd = random.Random(42292)
    for y0 in (0, 8):
        for x0 in range(0, 16, 4):
            top = rnd.choice(('#b8312b', '#b8312b', '#3f7a3a', '#d89a2a'))
            fill(im, x0, y0, x0 + 4, y0 + 8, '#f2ede2'); fill(im, x0, y0 + 1, x0 + 4, y0 + 4, top)
            fill(im, x0, y0, x0 + 4, y0 + 1, '#c6c9cb'); fill(im, x0, y0 + 7, x0 + 4, y0 + 8, '#9ea2a5')
            dot(im, x0 + 1, y0 + 5, shade(top, -10)[:3]); dot(im, x0 + 2, y0 + 5, shade(top, -10)[:3])
            for y in range(y0 + 1, y0 + 7):
                dot(im, x0 + 3, y, shade('#f2ede2', -30)[:3] if y > y0 + 3 else shade(top, -30)[:3])
    return im


def boxes():
    """Boxes of cereal, soap and matches, one in each four-by-eight cell, a white panel on each."""
    im = new(); rnd = random.Random(42293)
    for y0 in (0, 8):
        for x0 in range(0, 16, 4):
            c = rnd.choice(('#d6402f', '#f4b52a', '#2f6fb3', '#7a4a8a', '#3b8a3e', '#e07a2a'))
            fill(im, x0, y0, x0 + 4, y0 + 8, c); fill(im, x0 + 1, y0 + 2, x0 + 3, y0 + 5, '#f2ede2')
            dot(im, x0 + 1, y0 + 3, shade(c, -20)[:3])
            for y in range(y0, y0 + 8):
                dot(im, x0 + 3, y, shade(c, -30)[:3])
    return im


def sack():
    """A flour sack, cream cotton printed with a blue wheat sheaf, tied at the neck."""
    im = new('#efe6cf', 42294, 4)
    fill(im, 5, 0, 11, 2, '#d8cba8'); fill(im, 4, 2, 12, 3, '#b9a77e')
    for x, y in ((8, 6), (7, 7), (9, 7), (8, 8), (7, 9), (9, 9), (8, 10), (8, 11), (8, 12)):
        dot(im, x, y, '#2f5f9e')
    fill(im, 3, 13, 13, 14, '#b8312b')
    return im


def plate():
    """Plates stacked on a shelf, seen edge on: white, a blue line near the rim, every edge a darker line."""
    im = new('#f2efe8', 42295, 2)
    for y in range(0, 16, 2):
        for x in range(16):
            dot(im, x, y, '#c9c6bd')
    for x in range(16):
        dot(im, x, 1, '#5d7fb3')
    return im


def mug():
    """A heavy diner mug, white with a green stripe."""
    im = new('#efece4', 42296, 2)
    fill(im, 0, 5, 16, 7, '#3f7a3a'); fill(im, 0, 0, 16, 1, '#c9c6bd')
    return im


# ------------------------------------------------------------------------------------------------ 0.4.431: the children's bikes
def bike_paint():
    """The bike's frame: enamel gone dull, chipped to the metal at a few places."""
    im = new('#7d2620', 42297, 3)
    for x, y in ((3, 4), (11, 9), (6, 13)):
        dot(im, x, y, '#a9a59c')
    fill(im, 0, 0, 16, 1, '#9a332b')
    return im


def bike_seat():
    im = new('#26201c', 42298, 3)
    fill(im, 0, 0, 16, 1, '#3a302a')
    return im


def bike_parts():
    """A bicycle on its kickstand, built from bars a pixel or two thick: two wheels, the frame, the seat, the bars and the pedals.
    It runs along x, from -4 to 20, with its wheels in the plane z 7.5..8.5."""
    parts = []

    def bar(lo, hi, tex, rot=None):
        e = element(lo, hi, {'all': tex}, {k: [7, 7, 8, 8] for k in ('north', 'south', 'east', 'west', 'up', 'down')}, rot)
        parts.append(e)

    def wheel(cx, cy, r=5.5):
        z0, z1 = 7.5, 8.5
        bar([cx - 2, cy + r - 1, z0], [cx + 2, cy + r, z1], 't')          # top
        bar([cx - 2, cy - r, z0], [cx + 2, cy - r + 1, z1], 't')          # bottom, on the ground
        bar([cx - r, cy - 2, z0], [cx - r + 1, cy + 2, z1], 't')          # back
        bar([cx + r - 1, cy - 2, z0], [cx + r, cy + 2, z1], 't')          # front
        for sx in (-1, 1):
            for sy in (-1, 1):
                mx, my = cx + sx * (r - 1.6), cy + sy * (r - 1.6)
                bar([mx - 1.4, my - .5, z0], [mx + 1.4, my + .5, z1], 't', {'angle': -45 if sx * sy > 0 else 45, 'axis': 'z', 'origin': [mx, my, 8]})
        bar([cx - .5, cy - .5, 7], [cx + .5, cy + .5, 9], 'c')            # hub
        bar([cx - r + 1, cy - .25, 7.75], [cx + r - 1, cy + .25, 8.25], 'c')  # spokes, one across and one up
        bar([cx - .25, cy - r + 1, 7.75], [cx + .25, cy + r - 1, 8.25], 'c')

    wheel(1.5, 5.5)
    wheel(14.5, 5.5)
    bar([5, 9.5, 7.5], [14, 10.5, 8.5], 'f')                              # top tube
    bar([6.5, 4, 7.5], [7.5, 12, 8.5], 'f')                               # seat tube
    bar([1.5, 5, 7.25], [7, 6, 7.5], 'f'); bar([1.5, 5, 8.5], [7, 6, 8.75], 'f')   # chain stays
    bar([5.75, 6.5, 7.5], [14.25, 7.5, 8.5], 'f', {'angle': 45, 'axis': 'z', 'origin': [10, 7, 8]})  # down tube
    bar([.75, 7.25, 7.5], [7.75, 8.25, 8.5], 'f', {'angle': 45, 'axis': 'z', 'origin': [4.25, 7.75, 8]})  # seat stays
    bar([14, 5.5, 7.25], [15, 11, 7.5], 'c'); bar([14, 5.5, 8.5], [15, 11, 8.75], 'c')  # fork, down to the front hub
    bar([13.5, 10, 7.5], [14.5, 13.5, 8.5], 'c')                           # stem
    bar([13.5, 13.5, 3.5], [14.5, 14.5, 12.5], 'c')                        # handlebar
    bar([13.4, 13.4, 3], [14.6, 14.6, 4.5], 's'); bar([13.4, 13.4, 11.5], [14.6, 14.6, 13], 's')  # grips
    bar([6.5, 12, 7.5], [7.5, 12.5, 8.5], 'c')                             # seat post
    bar([4.5, 12.5, 7], [9, 13.5, 9], 's')                                 # seat
    bar([6.5, 3.5, 6], [7.5, 4.5, 10], 'c')                                # crank axle
    bar([6, 3.2, 5], [8, 3.8, 6], 's'); bar([6, 4.2, 10], [8, 4.8, 11], 's')  # pedals
    bar([7.5, .5, 9.5], [8.5, 4, 10.5], 'c', {'angle': 22.5, 'axis': 'x', 'origin': [8, 4, 8.5]})  # kickstand, its foot on the ground
    return parts


def lying(parts):
    """The same bike dropped on its side: the wheels' plane laid flat a little off the ground, the near grip and pedal down in the grass."""
    out = []
    for e in parts:
        (x0, y0, z0), (x1, y1, z1) = e['from'], e['to']
        f = {'from': [x0, z0 - 5.5, 16 - y1], 'to': [x1, z1 - 5.5, 16 - y0], 'faces': e['faces']}
        if 'rotation' in e:
            r = e['rotation']; ox, oy, oz = r['origin']
            axis = {'x': 'x', 'y': 'z', 'z': 'y'}[r['axis']]
            angle = r['angle']  # the turn about x carries the z axis onto y, the same way round
            f['rotation'] = {'angle': angle, 'axis': axis, 'origin': [ox, oz - 5.5, 16 - oy]}
        out.append(f)
    return out


def textures():
    out = {f'block/borrasca_{n}.png': f() for n, f in (('checker', checker), ('lino', lino), ('tin', tin))}
    faces = {
        'wood': wood(), 'chrome': chrome(), 'glass': glass(), 'enamel': enamel(), 'black': black(),
        'pie': pie(False), 'pie_cut': pie(True), 'crust': crust(), 'urn': urn(), 'menu': menu(),
        'pegboard': pegboard(False), 'pegboard_taken': pegboard(True),
        'pump_front': pump_front(), 'pump_side': pump_side(), 'pump_head': pump_head(), 'globe': globe(), 'price': price(),
        'payphone': payphone(), 'cooler_front': cooler_front(), 'cooler_top': cooler_top(), 'candy': candy(),
        'velvet': velvet(), 'curios': curios(), 'helmet': helmet(), 'lamp': lamp(), 'map': mine_map(), 'photos': photos(), 'clock': clock(),
        'scale': scale_face(), 'jar': jar(False), 'jar_low': jar(True), 'cards': cards(), 'exam_top': exam_top(), 'exam_side': exam_side(),
        'chart': chart(), 'fountain': fountain(), 'meds': meds(), 'rack': rack(), 'open': open_sign(),
        'steel': steel(), 'oil_blue': oil('#2f5f9e', '#f4d03a', '#26252a'), 'oil_red': oil('#b8312b', '#f2ede2', '#d8b23a'), 'jug': jug(),
        'bait_front': bait_front(), 'bait_top': bait_top(), 'cup': cup(), 'chips': chips(), 'calendar': calendar(),
        'bait_sign': bait_sign(), 'tire': tire(), 'tire_top': tire_top(),
        'tins': tins(), 'boxes': boxes(), 'sack': sack(), 'plate': plate(), 'mug': mug(),
        'bike_paint': bike_paint(), 'bike_seat': bike_seat(),
    }
    for name, im in faces.items():
        out[f'block/borrasca_shop/{name}.png'] = im
    for name, (draw, _) in ITEM_ART.items():
        out[f'item/borrasca_{name}.png'] = draw()
    return out


# ------------------------------------------------------------------------------------------------ models
def element(lo, hi, textures, uv=None, rot=None):
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
    if rot:
        e['rotation'] = rot
    return e


def model(elements, textures, particle, translucent=False):
    return {'ambientocclusion': False, 'render_type': 'minecraft:translucent' if translucent else 'minecraft:cutout',
            'textures': {**{k: P + v for k, v in textures.items()}, 'particle': P + particle}, 'elements': elements}


def wall(face, frame_tex='wood', inset=1):
    """A board on the wall behind it: a thin frame and the face, looking north."""
    return model([element([0, 0, 14.5], [16, 16, 16], {'all': 'f'}),
                  element([inset, inset, 14.4], [16 - inset, 16 - inset, 14.5], {'north': 'b'}, {'north': [inset, inset, 16 - inset, 16 - inset]})],
                 {'f': frame_tex, 'b': face}, face)


def shop_models():
    m = {}
    for stage, top in ((0, 'pie'), (1, 'pie_cut')):
        pies = [element([3, 1, 4], [7, 3, 8], {'all': 'c', 'up': 'p'}), element([9, 1, 4], [13, 3, 8], {'all': 'c', 'up': 'p'}),
                element([6, 1, 9], [10, 3, 13], {'all': 'c', 'up': 'q'})]
        m[f'pie_case_{stage}'] = model([element([1, 0, 2], [15, 1, 14], {'all': 'w'}), *pies,
                                        element([1, 1, 2], [15, 9, 14], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g'})],
                                       {'w': 'chrome', 'c': 'crust', 'p': 'pie', 'q': top, 'g': 'glass'}, 'glass', True)
    m['urn_0'] = model([element([4, 0, 4], [12, 12, 12], {'all': 'u', 'up': 'k'}), element([6, 12, 6], [10, 13, 10], {'all': 'k'}),
                        element([7, 3, 2], [9, 5, 4], {'all': 'k'})], {'u': 'urn', 'k': 'black'}, 'urn')
    m['menu_0'] = wall('menu')
    for stage, face in ((0, 'pegboard'), (1, 'pegboard_taken')):
        m[f'pegboard_{stage}'] = model([element([0, 0, 15], [16, 16, 16], {'all': 'b', 'north': 'p'})], {'b': 'wood', 'p': face}, face)
    m['pump_0'] = model([element([2, 0, 4], [14, 16, 13], {'all': 's', 'north': 'f', 'south': 'f'}),
                         element([14, 6, 7], [15, 12, 10], {'all': 'k'})], {'s': 'pump_side', 'f': 'pump_front', 'k': 'black'}, 'pump_front')
    m['pump_top_0'] = model([element([2, 0, 4], [14, 7, 13], {'all': 's', 'north': 'h', 'south': 'h'}),
                             element([5, 7, 6], [11, 11, 11], {'all': 'g'})], {'s': 'pump_side', 'h': 'pump_head', 'g': 'globe'}, 'pump_head')
    m['price_0'] = wall('price', 'chrome')
    m['payphone_0'] = model([element([3, 1, 11], [13, 16, 16], {'all': 'c', 'north': 'p'}), element([2, 15, 10], [14, 16, 16], {'all': 'c'})],
                            {'c': 'chrome', 'p': 'payphone'}, 'payphone')
    m['cooler_0'] = model([element([1, 0, 2], [15, 12, 15], {'all': 'f', 'up': 't'}), element([1, 12, 2], [15, 13, 15], {'all': 't'})],
                          {'f': 'cooler_front', 't': 'cooler_top'}, 'cooler_front')
    m['candy_0'] = model([element([2, 0, 7], [14, 15, 8], {'all': 'c'}), element([2, 0, 3], [3, 15, 13], {'all': 'k'}),
                          element([13, 0, 3], [14, 15, 13], {'all': 'k'})], {'c': 'candy', 'k': 'chrome'}, 'candy')
    m['case_0'] = model([element([0, 0, 0], [16, 8, 16], {'all': 'w'}), element([1, 8, 1], [15, 8.2, 15], {'up': 'v'}),
                         element([0, 8, 0], [16, 14, 16], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'up': 'g'})],
                        {'w': 'wood', 'v': 'curios', 'g': 'glass'}, 'glass', True)
    m['helmet_0'] = model([element([6, 0, 6], [10, 4, 10], {'all': 'w'}), element([4, 4, 4], [12, 8, 12], {'all': 'h'}),
                           element([3, 4, 3], [13, 5, 13], {'all': 'h'}), element([6, 5, 2.5], [10, 8, 4], {'all': 'l'})],
                          {'w': 'wood', 'h': 'helmet', 'l': 'lamp'}, 'helmet')
    m['map_0'] = wall('map')
    m['photos_0'] = model([element([0, 0, 15.5], [16, 16, 16], {'north': 'p'})], {'p': 'photos'}, 'photos')
    m['clock_0'] = model([element([4, 4, 14.5], [12, 12, 16], {'north': 'c', 'south': 'w', 'east': 'w', 'west': 'w', 'up': 'w', 'down': 'w'},
                                  {'north': [0, 0, 16, 16]})], {'c': 'clock', 'w': 'wood'}, 'clock')
    m['scale_0'] = model([element([3, 0, 3], [13, 2, 13], {'all': 'k'}), element([7, 2, 7], [9, 14, 9], {'all': 'e'}),
                          element([3, 13, 7.5], [13, 15, 8.5], {'all': 's'})], {'k': 'black', 'e': 'enamel', 's': 'scale'}, 'scale')
    for stage, face in ((0, 'jar'), (1, 'jar_low')):
        m[f'jar_{stage}'] = model([element([5, 0, 5], [11, 8, 11], {'north': 'j', 'south': 'j', 'east': 'j', 'west': 'j', 'down': 'j'}),
                                   element([5.5, 8, 5.5], [10.5, 9, 10.5], {'all': 'c'})], {'j': face, 'c': 'chrome'}, face, True)
    m['cards_0'] = wall('cards')
    m['exam_0'] = model([element([0, 6, 2], [16, 10, 14], {'all': 's', 'up': 't'}), element([1, 0, 3], [3, 6, 5], {'all': 'c'}),
                         element([13, 0, 3], [15, 6, 5], {'all': 'c'}), element([1, 0, 11], [3, 6, 13], {'all': 'c'}),
                         element([13, 0, 11], [15, 6, 13], {'all': 'c'})], {'s': 'exam_side', 't': 'exam_top', 'c': 'chrome'}, 'exam_top')
    m['chart_0'] = wall('chart', 'enamel', 0)
    m['fountain_0'] = model([element([2, 0, 6], [14, 3, 12], {'all': 'c'}), element([3, 3, 8], [13, 10, 10], {'all': 'c', 'north': 'f'})],
                            {'c': 'chrome', 'f': 'fountain'}, 'fountain')
    m['meds_0'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'north': 'm'})], {'w': 'wood', 'm': 'meds'}, 'meds')
    m['rack_0'] = model([element([7, 0, 7], [9, 15, 9], {'all': 'c'}),
                         element([3, 2, 3], [13, 15, 13], {'north': 'r', 'south': 'r', 'east': 'r', 'west': 'r'})], {'c': 'chrome', 'r': 'rack'}, 'rack')
    m['open_0'] = model([element([2, 4, 14.5], [14, 11, 16], {'all': 'b', 'north': 'o'}, {'north': [1, 2, 15, 12]})], {'b': 'black', 'o': 'open'}, 'open')
    # 0.4.430: steel shelving of motor oil, quarts on the two upper shelves and gallon jugs on the bottom, each can its own block.
    def cans(y0, h, tex, xs, z0=4, d=3, w=3, uv=(0, 0, 4, 8)):
        sides = {k: list(uv) for k in ('north', 'south', 'east', 'west')}
        return [element([x, y0, z0], [x + w, y0 + h, z0 + d], {'all': tex}, {**sides, 'up': [uv[0], uv[1], uv[2], uv[1] + 1], 'down': [uv[0], uv[1], uv[2], uv[1] + 1]}) for x in xs]
    shelf = [element([0, 0, 2], [1, 16, 14], {'all': 's'}), element([15, 0, 2], [16, 16, 14], {'all': 's'}), element([1, 0, 13], [15, 16, 14], {'all': 's'})]
    for y in (0, 5.5, 11, 15.5):
        shelf.append(element([1, y, 2], [15, y + .5 if y else 1, 13], {'all': 's'}))
    m['oil_0'] = model(shelf + cans(1, 4.5, 'j', (1.5, 5.75, 10), 3, 5, 3.75, (0, 0, 16, 16)) + cans(6, 4, 'b', (2, 5, 8, 11)) + cans(11.5, 4, 'r', (2, 5, 8, 11)),
                       {'s': 'steel', 'b': 'oil_blue', 'r': 'oil_red', 'j': 'jug'}, 'oil_blue')
    # The general store's shelf of groceries and the diner's shelf of crockery, on the same frame in wood.
    wooden = [element([0, 0, 2], [1, 16, 14], {'all': 's'}), element([15, 0, 2], [16, 16, 14], {'all': 's'}), element([1, 0, 13], [15, 16, 14], {'all': 's'})]
    for y in (0, 5.5, 11, 15.5):
        wooden.append(element([1, y, 2], [15, y + .5 if y else 1, 13], {'all': 's'}))
    m['groceries_0'] = model(wooden + cans(1, 4.5, 'k', (1.5, 5.75, 10), 3, 5, 3.75, (0, 0, 16, 16)) + cans(6, 4, 't', (2, 5, 8, 11)) + cans(11.5, 4, 'x', (1.5, 4.5, 7.5, 10.5), 3.5, 3, 3),
                             {'s': 'wood', 't': 'tins', 'x': 'boxes', 'k': 'sack'}, 'tins')
    crockery = [element([x, 1, 4], [x + 4, 4.5, 8], {'all': 'p'}, {k: [0, 0, 16, 7] for k in ('north', 'south', 'east', 'west')}) for x in (2, 6.5, 11)]
    crockery += [element([x, 6, 5], [x + 2, 8, 7], {'all': 'm'}) for x in (2.5, 5.5, 8.5, 11.5)]
    crockery += [element([x, 11.5, 4], [x + 3.5, 13, 7.5], {'all': 'p'}, {k: [0, 0, 16, 3] for k in ('north', 'south', 'east', 'west')}) for x in (2, 6, 10)]
    m['crockery_0'] = model(wooden + crockery, {'s': 'wood', 'p': 'plate', 'm': 'mug'}, 'plate')
    m['bait_0'] = model([element([1, 0, 2], [15, 10, 14], {'all': 'f', 'up': 't'}),
                         element([3, 10, 5], [6, 13, 8], {'all': 'c'}), element([7, 10, 4], [10, 13, 7], {'all': 'c'}), element([4, 10, 9], [7, 13, 12], {'all': 'c'})],
                        {'f': 'bait_front', 't': 'bait_top', 'c': 'cup'}, 'bait_front')
    rack = [element([1, 0, 6], [2, 16, 10], {'all': 'k'}), element([14, 0, 6], [15, 16, 10], {'all': 'k'})]
    for y in (1, 6, 11):
        rack.append(element([2, y, 5], [14, y + .5, 11], {'all': 'k'}))
        rack += [element([x, y + .5, 6], [x + 3, y + 4.5, 8], {'all': 'p'}, {k: [0, 0, 3, 4] for k in ('north', 'south', 'east', 'west')}, {'angle': -22.5, 'axis': 'x', 'origin': [8, y, 7]}) for x in (2.5, 6.5, 10.5)]
    m['snacks_0'] = model(rack, {'k': 'chrome', 'p': 'chips'}, 'chips')
    m['calendar_0'] = model([element([3, 1, 15.5], [13, 15, 16], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'}, {'north': [1, 0, 15, 16]})], {'c': 'calendar'}, 'calendar')
    m['bait_sign_0'] = wall('bait_sign')
    tires = []
    for i in range(3):
        y = i * 4
        tires += [element([1, y, 4], [15, y + 4, 12], {'all': 't', 'up': 'o', 'down': 'o'}, {'up': [1, 4, 15, 12], 'down': [1, 4, 15, 12]}),
                  element([4, y, 1], [12, y + 4, 15], {'all': 't', 'up': 'o', 'down': 'o'}, {'up': [4, 1, 12, 15], 'down': [4, 1, 12, 15]})]
    m['tires_0'] = model(tires, {'t': 'tire', 'o': 'tire_top'}, 'tire')
    parts = bike_parts()
    bike = {'t': 'tire', 'c': 'chrome', 'f': 'bike_paint', 's': 'bike_seat'}
    m['bike_0'] = model(parts, bike, 'bike_paint')
    m['bike_1'] = model(lying(parts), bike, 'bike_paint')
    return m


# ------------------------------------------------------------------------------------------------ 0.4.425: things to buy
# Drawn as grids, one character a pixel, in the vanilla item manner: a darker rim on every edge that meets the air.
def grid(rows, palette, rim=-46):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                c = palette[ch]
                im.putpixel((x, y), shade(c[0], 0, c[1]) if isinstance(c, tuple) else shade(c))
    px = im.load(); edge = []
    for y in range(16):
        for x in range(16):
            if px[x, y][3] != 255:
                continue
            if any(not (0 <= x + dx < 16 and 0 <= y + dy < 16) or px[x + dx, y + dy][3] == 0 for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                edge.append((x, y))
    for x, y in edge:
        r, g, b, a = px[x, y]
        px[x, y] = (max(0, r + rim), max(0, g + rim), max(0, b + rim), a)
    return im


def item_pie():
    return grid(['................', '................', '................', '................',
                 '..........hh....', '.......hhcLch...', '....hhcLccLcch..', '.hhcLccLccLcccC.',
                 '.cccccccccccccC.', '.ffgfffgffffcCC.', '.FfffFfffFfffCC.', '.CCCCCCCCCCCCCC.',
                 'wwwwwwwwwwwwwwww', 'swwwwwwwwwwwwwws', '.ssssssssssssss.', '................'],
                {'h': '#f2c87e', 'c': '#d9a252', 'C': '#a8702e', 'L': '#b47a38', 'f': '#8e2026', 'F': '#621219', 'g': '#c03a40',
                 'w': '#ece8e0', 's': '#bdb6a8'})


def item_mug(coffee, light):
    return grid(['................', '......g...g.....', '.....g...g......', '......g...g.....',
                 '................', '..OOOOOOOOO.....', '..OddDdddDO.....', '..OHWWWWWSOOO...',
                 '..OHWWWWWSO.O...', '..OHWWWWWSO.O...', '..OHWWWWWSOOO...', '..OHWWWWWSO.....',
                 '...OSSSSSO......', '....OOOOO.......', '................', '................'],
                {'g': ('#ffffff', 110), 'O': '#a8a296', 'W': '#f0ede6', 'H': '#ffffff', 'S': '#cfc9bd', 'd': coffee, 'D': light}, rim=-20)


def item_soda():
    return grid(['.......kk.......', '.......KK.......', '.......GG.......', '.......Gg.......',
                 '......GddG......', '......GddG......', '.....GdddgG.....', '.....GdddgG.....',
                 '.....LLLLLL.....', '.....LmmmmL.....', '.....LLLLLL.....', '.....GdddgG.....',
                 '.....GdddgG.....', '.....GdddgG.....', '.....GGGGGG.....', '................'],
                {'k': '#d6d6ce', 'K': '#9a9a92', 'G': '#7c9e96', 'g': '#d2eee6', 'd': '#3e2216', 'L': '#ece4cc', 'm': '#bc2e2c'})


def item_beer():
    return grid(['.......kk.......', '.......kk.......', '.......BB.......', '.......Bb.......',
                 '.......Bb.......', '......BBbB......', '.....BBBBbB.....', '.....BBBBbB.....',
                 '.....LLLLLL.....', '.....LllllL.....', '.....LLLLLL.....', '.....BBBBbB.....',
                 '.....BBBBbB.....', '.....BBBBbB.....', '.....BBBBBB.....', '................'],
                {'k': '#c8b46a', 'B': '#6a3414', 'b': '#b0622c', 'L': '#ece0c0', 'l': '#2e3e70'})


def item_malt():
    return grid(['.........s......', '....cc..s.......', '...cCcwws.......', '..wwwwwwwwww....',
                 '..wWwwwwwwWw....', '..GmmmmmmmmG....', '...GmmmmmmG.....', '...GmMmmmmG.....',
                 '....GmMmmG......', '....GmmmmG......', '.....GmmG.......', '......GG........',
                 '......GG........', '....GGGGGG......', '................', '................'],
                {'s': '#f4f0ea', 'c': '#c8202c', 'C': '#ff6a70', 'w': '#f6f2ea', 'W': '#ffffff', 'G': '#c4dcdc', 'm': '#8a5636', 'M': '#a8704a'}, rim=-30)


def item_candy():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            u, v = (x - 7.5) - (y - 7.5), (x - 7.5) + (y - 7.5)
            if abs(u) > 11 or abs(v) > 3.5:
                continue
            if u > 8.5:
                c = '#5a3018' if abs(v) < 2.5 else '#3e1e0e'
            elif u < -9.5:
                c = '#e8b0a8' if (x + y) % 2 else '#c87870'
            elif abs(u) < 3:
                c = '#efe6cc' if abs(v) < 2.5 else '#c8bc9a'
                if abs(v) < 1 and int(u) % 2 == 0:
                    c = '#3a2a6a'
            else:
                c = '#c4342c' if abs(v) < 2.5 else '#8a1e18'
            im.putpixel((x, y), shade(c))
    return im


def item_lollipop():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for i in range(7):
        x, y = 8 + i, 8 + i
        im.putpixel((x, y), shade('#f4f0e6')); im.putpixel((x, y + 1) if y < 15 else (x, y), shade('#bdb6a6'))
    import math
    for y in range(11):
        for x in range(11):
            dx, dy = x - 5, y - 5
            d = math.hypot(dx, dy)
            if d > 4.6:
                continue
            band = int((math.atan2(dy, dx) + math.pi) / (math.pi / 3) + d) % 2
            c = ('#e8344a' if band else '#f8e8ea') if d < 4 else '#a01e2e'
            im.putpixel((x + 1, y + 1), shade(c))
    im.putpixel((4, 3), shade('#ffffff'))
    return im


def item_coins():
    return grid(['................', '................', '................', '................',
                 '................', '.........oooo...', '........oPPPPo..', '........oPpPPo..',
                 '...oooo..oooo...', '..oSSSSo........', '..oShSSo..oooo..', '..oooooo.oSSSSo.',
                 '..oEEEEo.oShSSo.', '..oooooo.oooooo.', '..oEEEEo.oEEEEo.', '...oooo...oooo..'],
                {'o': '#6a6e72', 'S': '#c8ccd0', 'h': '#f2f6fa', 'E': '#9ca2a8', 'P': '#c27a4a', 'p': '#e8a06c'}, rim=0)


def item_wallet():
    return grid(['................', '................', '................', '................',
                 '....gggggg......', '...gGGGGGGg.....', '.OOOOOOOOOOOOO..', '.OLLLLLLLLLLLO..',
                 '.OLtttttttttLO..', '.OLllllllllLLO..', '.OLLLLLLLLLLLO..', '.OLtttttttttLO..',
                 '.OLLLLLLLLLLLO..', '.OOOOOOOOOOOOO..', '................', '................'],
                {'g': '#7e9a70', 'G': '#a8c49a', 'O': '#3a2416', 'L': '#5e3c24', 'l': '#7c5434', 't': '#a8825a'}, rim=0)


ITEM_ART = {'coins': (item_coins, 'Pocket change'), 'wallet': (item_wallet, 'Wallet'),
            'pie': (item_pie, 'Slice of pie'), 'coffee': (lambda: item_mug('#3a2416', '#5c3a24'), 'Cup of coffee'),
            'milk_coffee': (lambda: item_mug('#b88a5e', '#d4ac80'), 'Coffee, mostly milk'), 'soda': (item_soda, 'Bottle of soda'),
            'candy': (item_candy, 'Candy bar'), 'lollipop': (item_lollipop, 'Lollipop'), 'malt': (item_malt, 'Chocolate malt'),
            'beer': (item_beer, 'Bottle of beer')}


KINDS = ['pie_case', 'urn', 'menu', 'pegboard', 'pump', 'pump_top', 'price', 'payphone', 'cooler', 'candy', 'case', 'helmet', 'map', 'photos',
         'clock', 'scale', 'jar', 'cards', 'exam', 'chart', 'fountain', 'meds', 'rack', 'open',
         'oil', 'bait', 'snacks', 'calendar', 'bait_sign', 'tires', 'groceries', 'crockery', 'bike']
STAGES = {'pie_case': 2, 'pegboard': 2, 'jar': 2, 'bike': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'borrasca_shop': 'Shop Fitting', 'borrasca_checker': 'Checkered Floor', 'borrasca_lino': 'Linoleum', 'borrasca_tin': 'Pressed Tin'}


def files():
    out = {}
    for name in ('checker', 'lino', 'tin'):
        out[f'models/block/borrasca_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/borrasca_{name}'}}
        out[f'blockstates/borrasca_{name}.json'] = {'variants': {'': {'model': NS + f'block/borrasca_{name}'}}}
    for key, obj in shop_models().items():
        out[f'models/block/borrasca_shop/{key}.json'] = obj
    variants = {}
    for kind in KINDS:
        for facing, y in FACING_Y.items():
            for stage in range(2):
                drawn = stage if stage < STAGES.get(kind, 1) else 0
                v = {'model': NS + f'block/borrasca_shop/{kind}_{drawn}'}
                if y:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    out['blockstates/borrasca_shop.json'] = {'variants': variants}
    for name in ITEM_ART:
        out[f'models/item/borrasca_{name}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + f'item/borrasca_{name}'}}
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
    models = shop_models()
    for kind in KINDS:
        for stage in range(STAGES.get(kind, 1)):
            assert f'{kind}_{stage}' in models, (kind, stage)
    for key, obj in models.items():
        for tex in obj['textures'].values():
            assert f'block/borrasca_shop/{tex.split("borrasca_shop/")[1]}.png' in images, (key, tex)
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
    wanted = {f'block.the_oldest_house.{k}': v for k, v in NAMES.items()}
    wanted.update({f'item.the_oldest_house.borrasca_{k}': v[1] for k, v in ITEM_ART.items()})
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" Drisking's shops: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
