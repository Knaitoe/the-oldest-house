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
items. 0.4.439: what the machine leaves on its last night, in the same native pixels. A smear for the belt where it went in (stage 0,
a first wet streak; stage 1, the belt soaked); what comes off the far end (pulp and bone, stage 0; hair and a felt slipper, stage 1);
and the band from her wrist, cut, its card stamped with her number. With them, a sound made from vanilla files: the grinder, wet.
Writes the models and block states, the blocks' names, and the sound and its subtitle. --check verifies everything.
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
    '6': '111100111101111', '7': '111001010010010', '8': '111101111101111',
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
def art(rows, palette, mottled=(), seed=0, spread=4):
    """A picture from sixteen rows of sixteen letters. The colours named in mottled vary a little, from a fixed seed."""
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), [len(r) for r in rows]
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(seed)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ' ':
                px[x, y] = shade(palette[ch], rnd.randint(-spread, spread) if ch in mottled else 0)
    return im


# 0.4.454 (owner: "Borrasca needs a custom texture pass", of the pump's flat red body and its two great digits): the pump is built of
# parts, in enamel, chrome and rubber. A steel plinth; a door with its seam, louvres and lock under a white band; a chrome waist; the
# head, all glass, with the sale and the gallons on small wheels; a white cap; a milk-glass lens on a stem; and down one side the
# hose, from its elbow at the top round to the nozzle hung in its boot.
PUMP = {'R': '#b02a23', 'L': '#c9463a', 'S': '#8a1e1a', 'D': '#63110f', 'W': '#f0ece2', 'C': '#d2ccbd', 'K': '#151517', 'k': '#38383d',
        'G': '#e6e9ea', 'M': '#b4b9bd', 'N': '#7a8086', 'E': '#e8e1cd', 'F': '#a79f8a', 'T': '#44474b', 't': '#5e6268', 'u': '#2a2c2f',
        'r': '#e0483a', 'B': '#27406e', 'O': '#c8352b'}


def pump_door():
    return art(['RRRRRRRRRRRRRRRR', 'RRRRRRRRRRRRRRRR', 'RRLWWWWWWWWWWSRR', 'RRLWWWWRRWWWWSRR', 'RRLCCCCCCCCCCSRR', 'RRLRRRRRRRRRRSRR',
                'RRLRSSSSSSSSRSRR', 'RRLRSLLLLLLSRSRR', 'RRLRSRDDDDRSRSRR', 'RRLRSRLLLLRSRSRR', 'RRLRSRDDDDRSRSRR', 'RRLRSRLLLLGSRSRR',
                'RRLRSSSSSSSSRSRR', 'RRSSSSSSSSSSSDRR', 'RRDDDDDDDDDDDDRR', 'RRDDDDDDDDDDDDRR'], PUMP, 'R', 42240, 3)


def pump_side():
    return art(['RRRRRRRRRRRRRRRR', 'RRRRRRRRRRRRRRRR', 'WWWWWWWWWWWWWWWW', 'WWWWWWWWWWWWWWWW', 'CCCCCCCCCCCCCCCC', 'RRRRRRRRRRRRRRRR',
                'RRRRRSSSSSSSRRRR', 'RRRRRSGRRRGSRRRR', 'RRRRRSRRRRRSRRRR', 'RRRRRSRRRRRSRRRR', 'RRRRRSRRRRRSRRRR', 'RRRRRSGRRRGSRRRR',
                'RRRRRSSSSSSSRRRR', 'SSSSSSSSSSSSSSSS', 'DDDDDDDDDDDDDDDD', 'DDDDDDDDDDDDDDDD'], PUMP, 'R', 42241, 3)


def pump_side_top():
    return art(['RRRRRRRRRRRRRRRR'] * 6 + ['RRRRRRDDDDDRRRRR', 'RRRRRRLLLLLRRRRR', 'RRRRRRDDDDDRRRRR', 'RRRRRRLLLLLRRRRR', 'RRRRRRDDDDDRRRRR',
                                           'RRRRRRLLLLLRRRRR', 'RRRRRRRRRRRRRRRR', 'RRRRRRRRRRRRRRRR', 'RRRRRRRRRRRRRRRR', 'SSSSSSSSSSSSSSSS'],
               PUMP, 'R', 42242, 3)


# What stands on a wheel, two pixels by two: enough to say a figure stands there, and that the two pumps stopped on different ones.
# Each stands clear of its window's edge, so the windows read as black glass with white figures behind it.
FIGURES = {'0': ('WW', 'WW'), '1': ('KW', 'KW'), '3': ('WW', 'KW'), '4': ('WK', 'WW'), '6': ('WK', 'WW'), '8': ('WW', 'WW'), '9': ('WW', 'KW'), ' ': ('KK', 'KK')}


def pump_dial(which):
    """The head's glass, front and back: the sale over the gallons, each in its own window. The first pump's wheels stopped on one
    sale, the second's on another."""
    window = 'RRGKKKKKKKKKKNRR'
    rows = ['RRRRRRRRRRRRRRRR'] * 4 + ['RRGGGGGGGGGGGMRR', 'RRGWWEEEEEEEENRR', window, window, window, window, 'RRGEOEEEEFFFENRR',
                                       window, window, window, window, 'RRMNNNNNNNNNNNRR']
    im = art(rows, PUMP, 'R', 42243 + which, 3)
    sale, gallons = ('981', ' 90') if which == 0 else ('436', ' 40')
    for top, figures in ((7, sale), (12, gallons)):
        for i, figure in enumerate(figures):
            for dy, line in enumerate(FIGURES[figure]):
                for dx, ch in enumerate(line):
                    if ch == 'W':
                        dot(im, 4 + i * 3 + dx, top + dy, PUMP['W'])
    return im


def pump_cap():
    return art(['WWWWWWWWWWWWWWWW', 'CCCCCCCCCCCCCCCC'] + ['WWWWWWWWWWWWWWWW'] * 14, PUMP, 'W', 42245, 2)


def pump_trim():
    return art([('G', 'M', 'N', 'M')[y % 4] * 16 for y in range(16)], PUMP)


def pump_globe():
    """The lens, lit from inside: a ring and a dot on milk glass. Below it in the picture, the glass of its rim."""
    return art(['     WWWWWW     ', '    WWOOOOWW    ', '    WOWBBWOW    ', '    WOWBBWOW    ', '    WWOOOOWW    ', '     WWWWWW     ',
                '                ', '                '] + ['CCCCCCCCCCCCCCCC'] * 8, PUMP)


def pump_base():
    im = art(['TTTTTTTTTTTTTTTT'] * 14 + ['tttttttttttttttt', 'uuuuuuuuuuuuuuuu'], PUMP, 'T', 42246, 3)
    for x, y in ((2, 4), (13, 4), (2, 12), (13, 12)):
        dot(im, x, y, PUMP['t'])
    return im


def pump_hose():
    im = art(['KKKKKKKKKKKKKKKK'] * 16, PUMP, 'K', 42247, 2)
    for i in range(16):
        dot(im, 4, i, PUMP['k']); dot(im, i, 12, PUMP['k'])
    return im


# 0.4.454 (owner, of the church's pews built of plank stairs: "again, custom textures would be beneficial here."): boards four pixels
# deep, golden under their varnish, a light edge above a dark seam, with a little grain. The pews' ends are cut from the same oak,
# its boards on end, with a cross let into each. The benches' slats are a browner, weathered wood, on black iron.
def boards(light, base, grain, seam):
    palette = {'o': light, 'O': base, 'Q': grain, 'q': seam}
    rows = []
    for k, (flecks, dashes) in enumerate((((6, 7), (1, 2, 3, 9, 10)), ((1, 2, 10), (5, 6, 7, 12, 13, 14)), ((12, 13), (0, 1, 7, 8, 9)), ((7, 8), (3, 4, 5, 11, 12)))):
        rows += ['o' * 16, ''.join('o' if x in flecks else 'O' for x in range(16)), ''.join('Q' if x in dashes else 'O' for x in range(16)), 'q' * 16]
    return art(rows, palette)


def pew_wood():
    return boards('#bd8a4e', '#a8733c', '#93622f', '#70461f')


def pew_end():
    """A pew's end: boards on end, a line cut round inside its edge and a cross let into it."""
    im = boards('#bd8a4e', '#a8733c', '#93622f', '#70461f').transpose(Image.Transpose.ROTATE_90)
    for y in range(6, 15):
        dot(im, 4, y, '#70461f'); dot(im, 12, y, '#70461f')
    for x in range(4, 13):
        dot(im, x, 14, '#70461f')
    for y in range(7, 13):
        dot(im, 8, y, '#5b3716')
    for x in (7, 9):
        dot(im, x, 9, '#5b3716')
    return im


def pew_books():
    """The rack on a pew's back, as the row behind sees it: the hymnals' covers (red, and one blue) with their gilt, and the pages' edges."""
    palette = {'r': '#8c2f2a', 's': '#6d211e', 'b': '#2f4a78', 'c': '#22365b', 'g': '#d9b24a', 'w': '#ece6d4', 'x': '#c9c2ad'}
    return art(['rrs bbc         ', 'rgs bgc         ', 'rrs bbc         ', 'rrs bbc         ', '                ', '                ',
                '                ', '                ', 'wxw wxw         ', 'wxw wxw         '] + ['                '] * 6, palette)


def bench_wood():
    """A bench's slats. Their tops (rows 0 to 2), and their edges one, two and three pixels deep (row 4, rows 6 and 7, rows 9 to 11)."""
    palette = {'o': '#a67a4a', 'O': '#8b6239', 'Q': '#75512d', 'q': '#57391c', ' ': None}
    def row(base, dashes):
        return ''.join('Q' if x in dashes else base for x in range(16))
    return art([row('o', (4, 5, 13)), row('O', (0, 1, 2, 8, 9)), row('O', (5, 6, 11, 12, 13)), 'q' * 16,
                row('O', (2, 3, 10)), 'q' * 16, row('o', (7, 8)), row('Q', ()), 'q' * 16,
                row('o', (1, 12, 13)), row('O', (4, 5, 6, 9)), row('Q', ()), 'q' * 16, 'q' * 16, 'q' * 16, 'q' * 16], palette)


def bench_iron():
    im = art(['IIIIIIIIIIIIIIII'] * 16, {'I': '#26272a'}, 'I', 42250, 2)
    for i in range(0, 16, 3):
        dot(im, i, (i * 5) % 16, '#3d3f44'); dot(im, (i * 7 + 3) % 16, i, '#3d3f44')
    return im


def baton():
    """0.4.457: the Sheriff's baton. Black rubber with a line of light along it; at one end the ridges of the grip."""
    im = art(['IIIIIIIIIIIIIIII'] * 16, {'I': '#1d1d20'}, 'I', 45700, 2)
    for x in range(16):
        dot(im, x, 5, '#3a3a40'); dot(im, x, 6, '#2a2a2f')
    for x in (1, 3, 5):
        for y in range(16):
            dot(im, x, y, '#0f0f11')
    return im


def baton_item():
    """The baton in a hand: a stick on end, sixteen pixels long, its side handle a third of the way up. Held as a club."""
    stick = {k: [6, 0, 7.5, 16] for k in ('north', 'south', 'east', 'west')}
    stick.update({'up': [8, 4, 9.5, 5.5], 'down': [8, 4, 9.5, 5.5]})
    side = {k: [8, 4, 11, 5.5] for k in ('north', 'south', 'up', 'down')}
    side.update({'east': [8, 4, 9.5, 5.5], 'west': [8, 4, 9.5, 5.5]})
    return {'textures': {'b': P + 'baton', 'particle': P + 'baton'},
            'elements': [element([7.25, 0, 7.25], [8.75, 16, 8.75], {'all': 'b'}, stick),
                         element([8.75, 4.5, 7.25], [11.75, 6, 8.75], {'all': 'b'}, side)],
            'display': {'thirdperson_righthand': {'rotation': [-20, 0, 0], 'translation': [0, 3.5, .5], 'scale': [.85, .85, .85]},
                        'thirdperson_lefthand': {'rotation': [-20, 0, 0], 'translation': [0, 3.5, .5], 'scale': [.85, .85, .85]},
                        'firstperson_righthand': {'rotation': [0, -20, 12], 'translation': [0, 3, 0], 'scale': [.8, .8, .8]},
                        'ground': {'rotation': [90, 0, 0], 'translation': [0, 1, 0], 'scale': [.6, .6, .6]},
                        'gui': {'rotation': [0, 0, -45], 'translation': [0, 0, 0], 'scale': [.9, .9, .9]}}}


def price(later=False):
    """The station's board: regular over unleaded. 1.09 and 1.14 in the childhood years; 2.89 and 2.99 when the reader comes back grown (0.4.454)."""
    im = new('#f0ece2', 42244, 3); frame(im, '#3a3a3a')
    for i, ch in enumerate('289' if later else '109'):
        text(im, 2 + i * 4, 2, ch, '#c0302a')
    for i, ch in enumerate('299' if later else '114'):
        text(im, 2 + i * 4, 9, ch, '#2a2a2a')
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


def open_sign():
    """0.4.444: a lit sign that hangs inside the glass: seven rows of the tile (5 to 11), red tube above and below red letters."""
    im = new('#141418', 42273, 2)
    fill(im, 0, 5, 16, 6, '#c8281c'); fill(im, 0, 11, 16, 12, '#c8281c')
    text(im, 0, 6, 'OPEN', '#ff5a48')
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


def bike_bar(parts, lo, hi, tex, rot=None, px=7):
    """One bar of a bike, a pixel or two thick, every face from the same pixel of its texture."""
    parts.append(element(lo, hi, {'all': tex}, {k: [px, 7, px + 1, 8] for k in ('north', 'south', 'east', 'west', 'up', 'down')}, rot))


def bike_wheel_parts(parts, cx, cy, r=5.5):
    """A wheel in the plane z 7.5..8.5 about its hub (cx, cy). 0.4.447 (owner: "Try to fix wheels"): the tyre is a closed ring of
    sixteen pieces, each as long as its share of the rim and a little over, so that none stands apart from the next (there were eight,
    with a gap between each two). Every other piece takes the tread's groove, so the wheel is seen to turn. Then the hub and eight
    spokes. A model's piece turns only by 22.5 or 45 degrees about one axis, which sixteen pieces use up."""
    z0, z1 = 7.5, 8.5
    thick = 1.2
    mid = r - thick / 2
    half = round(r * math.tan(math.pi / 16) + .12, 3)
    for k in range(16):
        phi = math.radians(k * 22.5)
        mx, my = round(cx + mid * math.cos(phi), 3), round(cy + mid * math.sin(phi), 3)
        tangent = (k * 22.5 + 90) % 180
        if tangent > 90:
            tangent -= 180
        if abs(tangent) <= 45:
            lo, hi, angle = [mx - half, my - thick / 2, z0], [mx + half, my + thick / 2, z1], tangent
        else:
            lo, hi, angle = [mx - thick / 2, my - half, z0], [mx + thick / 2, my + half, z1], tangent - 90 if tangent > 0 else tangent + 90
        lo, hi = [round(v, 3) for v in lo], [round(v, 3) for v in hi]
        bike_bar(parts, lo, hi, 't', {'angle': angle, 'axis': 'z', 'origin': [mx, my, 8]} if angle else None, 6 if k % 2 else 7)
    bike_bar(parts, [cx - .6, cy - .6, 7], [cx + .6, cy + .6, 9], 'c')            # hub
    reach = round(r - thick + .1, 3)
    bike_bar(parts, [cx - reach, cy - .2, 7.8], [cx + reach, cy + .2, 8.2], 'c')  # spokes: across, up, and the two diagonals
    bike_bar(parts, [cx - .2, cy - reach, 7.8], [cx + .2, cy + reach, 8.2], 'c')
    for angle in (45, -45):
        bike_bar(parts, [cx - reach, cy - .2, 7.8], [cx + reach, cy + .2, 8.2], 'c', {'angle': angle, 'axis': 'z', 'origin': [cx, cy, 8]})


def bike_parts(wheels=True, standing=True):
    """A bicycle on its kickstand, built from bars a pixel or two thick: two wheels, the frame, the seat, the bars and the pedals.
    It runs along x, from -4 to 20, with its wheels in the plane z 7.5..8.5. 0.4.435: without its wheels, kickstand and pedals it is
    the frame a ridden bike is drawn from (the wheels and the cranks turn on their own, bike_wheel and bike_cranks)."""
    parts = []

    def bar(lo, hi, tex, rot=None):
        bike_bar(parts, lo, hi, tex, rot)

    if wheels:
        bike_wheel_parts(parts, 1.5, 5.5)
        bike_wheel_parts(parts, 14.5, 5.5)
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
    if standing:
        bar([6.5, 3.5, 6], [7.5, 4.5, 10], 'c')                                # crank axle
        bar([6, 3.2, 5], [8, 3.8, 6], 's'); bar([6, 4.2, 10], [8, 4.8, 11], 's')  # pedals
        bar([7.5, .5, 9.5], [8.5, 4, 10.5], 'c', {'angle': 22.5, 'axis': 'x', 'origin': [8, 4, 8.5]})  # kickstand, its foot on the ground
    return parts


def bike_wheel():
    """0.4.435: one wheel about the middle of the block, turned by the ridden bike's renderer at each hub."""
    parts = []
    bike_wheel_parts(parts, 8, 8)
    return parts


def bike_cranks():
    """0.4.435: the cranks about the middle of the block, as the axle at (7, 4) is drawn turning: the left pedal (toward -z, on the
    rider's left) down and the right one up, each on an arm two and a half pixels long."""
    parts = []
    bike_bar(parts, [7.5, 7.5, 6], [8.5, 8.5, 10], 'c')                    # axle
    bike_bar(parts, [7.6, 5.5, 5.5], [8.4, 8, 6], 'c')                     # left arm, down
    bike_bar(parts, [6.75, 5, 4], [9.25, 5.75, 5.5], 's')                  # left pedal
    bike_bar(parts, [7.6, 8, 10], [8.4, 10.5, 10.5], 'c')                  # right arm, up
    bike_bar(parts, [6.75, 10.25, 10.5], [9.25, 11, 12], 's')              # right pedal
    return parts


# ------------------------------------------------------------------------------------------------ 0.4.436: the drugstore's arcade cabinets
def cab_side(game):
    """A cabinet's side: DEEP SEAM's coal black with a slanting lamp-yellow band, BIG CAT's river green with a wavy band of water."""
    if game == 0:
        im = new('#17130f', 43601, 3)
        for x in range(16):
            for w in range(3):
                dot(im, x, (12 - x // 2 + w) % 16, '#d8a42a' if w < 2 else '#8a6418')
        fill(im, 3, 3, 6, 6, '#f0d070'); dot(im, 4, 2, '#5a5040'); dot(im, 4, 6, '#a07a20')
    else:
        im = new('#173a2a', 43602, 3)
        for x in range(16):
            y = 10 + (1 if (x // 3) % 2 else 0)
            dot(im, x, y, '#3e7ea8'); dot(im, x, y + 1, '#2c5e84'); dot(im, x, y + 2, '#1e4462')
        for x, y in ((4, 4), (5, 4), (6, 5), (3, 5)):
            dot(im, x, y, '#c0b890')
    return im


def cab_coin():
    """The coin door under the controls: a steel plate, two lit coin slots, the return buttons and a scuffed kick plate."""
    im = new('#1c1c20', 43603, 2)
    fill(im, 3, 2, 13, 11, '#4a4c54'); frame(im, '#2a2a30', 3, 2, 13, 11)
    for x0 in (4, 9):
        fill(im, x0, 4, x0 + 3, 7, '#c8601c'); fill(im, x0 + 1, 4, x0 + 2, 7, '#1a1010'); dot(im, x0 + 1, 8, '#a0a0a8')
    fill(im, 2, 12, 14, 16, '#2a2a30')
    for x in (4, 9, 12):
        dot(im, x, 13, '#4a4a52')
    return im


def cab_panel():
    """The control panel's top: worn black with the player's spot rubbed lighter."""
    im = new('#141416', 43604, 2)
    fill(im, 2, 3, 8, 9, '#1e1e22')
    for x in range(16):
        dot(im, x, 0, '#2a2a30')
    return im


def cab_screen(game):
    """The screen in its bezel, as a passer-by sees it glowing: DEEP SEAM's cart on its rails, BIG CAT's boat and the line."""
    im = new('#0a0a0c', 43605 + game, 1)
    fill(im, 1, 1, 15, 13, '#000000')
    if game == 0:
        for y in (5, 8, 11):
            fill(im, 2, y, 14, y + 1, '#7a7c80')
        fill(im, 4, 6, 7, 8, '#4a4e56'); dot(im, 7, 7, '#ffe8a0'); fill(im, 10, 3, 11, 5, '#f0c048')
        for x, y in ((2, 2), (12, 2), (8, 12)):
            dot(im, x, y, '#3a2a18')
    else:
        fill(im, 1, 1, 15, 4, '#0e1430'); dot(im, 12, 2, '#e8e4c8')
        fill(im, 1, 4, 15, 13, '#06243a'); fill(im, 1, 4, 15, 5, '#6080a0')
        fill(im, 6, 3, 10, 4, '#5a4630')
        for i in range(6):
            dot(im, 10 + i // 3, 3 + i, '#c0c0c0')
        fill(im, 3, 10, 7, 12, '#6a5e50'); dot(im, 2, 11, '#9a8e7e')
    fill(im, 0, 13, 16, 16, '#1a1a1e')
    return im


def cab_marquee(game):
    """The lit marquee over the screen: a lamp over crossed picks for DEEP SEAM, a catfish with its whiskers for BIG CAT."""
    if game == 0:
        im = new('#1a120a', 43607, 2)
        fill(im, 0, 0, 16, 16, '#d89a22')
        fill(im, 1, 1, 15, 15, '#2a1a0c')
        for i in range(10):  # two picks crossed behind the lamp
            dot(im, 3 + i, 3 + i, '#c8c8c8'); dot(im, 3 + i, 12 - i, '#c8c8c8')
        fill(im, 6, 5, 10, 10, '#ffe080'); fill(im, 7, 4, 9, 5, '#5a5040'); fill(im, 6, 10, 10, 11, '#8a6418')
    else:
        im = new('#0e2a20', 43608, 2)
        fill(im, 0, 0, 16, 16, '#58c0a0')
        fill(im, 1, 1, 15, 15, '#0e2a30')
        fill(im, 3, 6, 12, 10, '#9a8e7e'); fill(im, 12, 7, 14, 9, '#9a8e7e'); fill(im, 1, 5, 3, 11, '#7a6e5e')
        dot(im, 4, 7, '#101010')
        for x in (2, 3):
            dot(im, x - 1, 9 + (3 - x), '#c8c0a8'); dot(im, x - 1, 10 + (3 - x), '#c8c0a8')
        fill(im, 3, 12, 13, 13, '#3e7ea8')
    return im


def cabinet_models(m):
    """The two halves of each cabinet. The lower: the coin door under a deep control panel, with the stick and two buttons. The
    upper: the screen set back in its bezel, under the lit marquee. Both face north, as authored, into the shop."""
    for game in (0, 1):
        tex = {'s': f'cab_side_{game}', 't': f'cab_side_top_{game}', 'k': 'black', 'c': 'cab_coin', 'p': 'cab_panel', 'r': 'cab_red', 'y': 'cab_yellow',
               'v': f'cab_screen_{game}', 'q': f'cab_marquee_{game}'}
        full = {'north': [0, 0, 16, 16]}
        lower = [element([1, 0, 3], [15, 11, 16], {'north': 'c', 'south': 'k', 'east': 's', 'west': 's', 'up': 'k', 'down': 'k'}, full),
                 element([1, 11, 0], [15, 14, 16], {'north': 'k', 'south': 'k', 'east': 's', 'west': 's', 'up': 'p', 'down': 'k'}),
                 # 0.4.444 (owner: "Arcade machines have gap in center?"): the sides carry on up behind the panel to meet the upper half.
                 element([1, 14, 5], [15, 16, 16], {'north': 'k', 'south': 'k', 'east': 's', 'west': 's'}),
                 element([4, 14, 3], [5, 16.5, 4], {'all': 'k'}), element([3.5, 16.5, 2.5], [5.5, 18, 4.5], {'all': 'r'}),
                 element([8, 14, 2.5], [9.5, 14.6, 4], {'all': 'r'}), element([10.5, 14, 2.5], [12, 14.6, 4], {'all': 'y'})]
        upper = [element([1, 0, 5], [15, 12, 16], {'north': 'v', 'south': 'k', 'east': 't', 'west': 't', 'up': 'k', 'down': 'k'}, full),
                 element([1, 12, 3], [15, 16, 16], {'north': 'q', 'south': 'k', 'east': 't', 'west': 't', 'up': 'k', 'down': 'k'}, full)]
        m[f'cabinet_{game}'] = model(lower, tex, f'cab_side_{game}')
        m[f'cabinet_top_{game}'] = model(upper, tex, f'cab_side_{game}')


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


# ------------------------------------------------------------------------------------------------ 0.4.439: the last night at the machine
def gore():
    """Pulp: wet, dark and bright by turns, clotted, with fat and gristle through it."""
    im = new('#6e0f12', 43906, 8); rnd = random.Random(43907)
    for _ in range(26):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        dot(im, x, y, '#3d0608'); dot(im, x + 1, y, '#3d0608')
    for _ in range(20):
        dot(im, rnd.randint(0, 15), rnd.randint(0, 15), '#a3201f')
    for _ in range(8):
        x, y = rnd.randint(0, 14), rnd.randint(0, 14)
        dot(im, x, y, '#d0483f'); dot(im, x, y + 1, '#a3201f')
    for _ in range(9):
        dot(im, rnd.randint(0, 15), rnd.randint(0, 15), '#e8c9a8')
    for _ in range(4):
        x, y = rnd.randint(1, 13), rnd.randint(1, 13)
        for k in range(3):
            dot(im, x + k, y, '#d9a7a0')
    return im


def smear(soaked):
    """What the belt carries out of the slot: a streak along the rubber, or all of it running wet, the edges thin and the clots
    lying in the ribs."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); rnd = random.Random(43915 + soaked)
    half = 8 if soaked else 3
    for y in range(16):
        width = half + rnd.randint(-1, 2 if soaked else 1)
        for x in range(8 - width, 8 + width):
            edge = x in (8 - width, 8 + width - 1)
            dot(im, x, y, '#4a0a0c' if edge else ('#6e0f12' if (x + y) % 3 else '#8a1416'), 150 if edge else 225)
    for _ in range(10 if soaked else 4):
        x, y = rnd.randint(8 - half + 1, 8 + half - 2), rnd.randint(0, 15)
        dot(im, x, y, '#3d0608'); dot(im, x + 1, y, '#a3201f')
    for _ in range(5 if soaked else 2):
        dot(im, rnd.randint(8 - half + 1, 8 + half - 2), rnd.randint(0, 15), '#d0483f')
    return im


def bone():
    im = new('#e3dccb', 43908, 4); rnd = random.Random(43909)
    for y in range(16):
        dot(im, rnd.randint(0, 15), y, '#c8bfa9')
    for x in range(16):
        if x % 5 == 2:
            for y in range(rnd.randint(0, 5), rnd.randint(9, 15)):
                dot(im, x, y, '#8a1d1a')
    dot(im, 7, 7, '#7a3a2a'); dot(im, 8, 7, '#7a3a2a'); dot(im, 7, 8, '#5a2418')
    return im


def hair():
    """A clump of hair, long, going grey through the brown, matted together."""
    im = new('#4a3a2e', 43910, 4); rnd = random.Random(43911)
    for x in range(16):
        for y in range(16):
            if (x * 3 + y) % 5 == 0:
                dot(im, x, y, '#6b5a4a')
            elif (x * 3 + y) % 7 == 0:
                dot(im, x, y, '#9a948a')
    for _ in range(14):
        x, y = rnd.randint(0, 15), rnd.randint(0, 15)
        dot(im, x, y, '#5b1414'); dot(im, x, y + 1, '#3d0c0c')
    return im


def slipper():
    """A felt slipper, the kind handed out by the dozen: a worn grey felt upper, its pale sole, and what got on it."""
    im = new('#8d8a84', 43912, 5); rnd = random.Random(43913)
    fill(im, 0, 12, 16, 16, '#d6d0c2'); fill(im, 0, 12, 16, 13, '#a9a294')
    for x in range(1, 15, 2):
        dot(im, x, 2, '#6f6c66')
    for _ in range(12):
        dot(im, rnd.randint(0, 15), rnd.randint(0, 11), '#a5a29b')
    for _ in range(12):
        x, y = rnd.randint(0, 15), rnd.randint(3, 15)
        dot(im, x, y, '#6e0f12'); dot(im, x, y + 1, '#4a0a0c')
    return im


def wristband():
    """The band from her wrist: a white plastic strap, cut, with its card. The card is stamped 141 under an A."""
    im = new('#e9e6de', 43914, 3)
    fill(im, 0, 0, 16, 1, '#bdb8ac'); fill(im, 0, 15, 16, 16, '#bdb8ac')
    text(im, 7, 1, 'A', '#2c3a62')
    text(im, 3, 7, '141', '#2c3a62')
    for x, y in ((1, 13), (2, 14), (14, 3), (13, 12), (14, 13)):
        dot(im, x, y, '#6e0f12')
    return im


# ---- 0.4.444: papers, each with its own face (the owner: "Give new textures to the different docs.")
def sheet(paper='#e9e3d2', seed=0):
    """A sheet fourteen pixels square inside the tile (the models show the tile's middle, pixel for pixel)."""
    im = new(); px = im.load(); rnd = random.Random(seed)
    for y in range(1, 15):
        for x in range(1, 15):
            px[x, y] = shade(paper, rnd.randint(-3, 3))
    return im


def typed_rows(im, rows, x0=3, x1=13, colour='#55524c'):
    for y in rows:
        for x in range(x0, x1):
            if (x * 7 + y * 3) % 11 != 0:
                dot(im, x, y, colour)


def clipboard(seed, grid):
    im = new(); fill(im, 1, 1, 15, 15, '#7a5230'); frame(im, '#5c3b20', 1, 1, 15, 15)
    px = im.load(); rnd = random.Random(seed)
    for y in range(3, 14):
        for x in range(2, 14):
            px[x, y] = shade('#ece7d8', rnd.randint(-3, 3))
    fill(im, 6, 1, 10, 3, '#b9bcc0'); fill(im, 7, 2, 9, 3, '#8d9196')
    if grid:
        for x in (6, 10):
            fill(im, x, 4, x + 1, 13, '#b7b09c')
        for y in (5, 7, 9, 11):
            typed_rows(im, (y,), 3, 13, '#6b6860')
    else:
        fill(im, 3, 4, 13, 5, '#b3261e')
        typed_rows(im, (6, 8, 10, 12))
    return im


def doc_dispatch():
    """The dispatch sheet, on a clipboard under a red header."""
    return clipboard(44431, False)


def doc_log():
    """The gate log: a clipboard ruled into columns, a pencil lying across its corner."""
    im = clipboard(44432, True)
    for i in range(4):
        dot(im, 9 + i, 13 - i, '#e2b53a')
    dot(im, 13, 9, '#3a2c20'); dot(im, 8, 14, '#d9a0a0')
    return im


def doc_payroll():
    """Green-bar paper: pale green and white in bands, ruled into columns, the figures down the right."""
    im = sheet('#f1eee4', 44433)
    for y in range(1, 15):
        if (y // 2) % 2 == 0:
            fill(im, 1, y, 15, y + 1, '#cfe3c8')
    fill(im, 1, 1, 15, 3, '#8fb287')
    for x in (6, 11):
        fill(im, x, 1, x + 1, 15, '#9fb89a')
    for y in range(4, 14, 2):
        typed_rows(im, (y,), 2, 6, '#3f4a3c'); dot(im, 8, y, '#3f4a3c'); dot(im, 9, y, '#3f4a3c'); dot(im, 12, y, '#3f4a3c'); dot(im, 13, y, '#3f4a3c')
    return im


def doc_signin():
    """A ruled sign-in sheet: a red margin, blue rules, a column of scrawled initials, and the pen left on it."""
    im = sheet('#f4f1e8', 44434)
    for y in range(3, 14, 2):
        fill(im, 1, y, 15, y + 1, '#b7c6de')
    fill(im, 3, 1, 4, 15, '#d98a8a')
    rnd = random.Random(44435)
    for y in range(2, 13, 2):
        x = 5
        for _ in range(rnd.randint(2, 5)):
            dot(im, x, y, '#27367a'); x += rnd.choice((1, 1, 2))
    for i in range(5):
        dot(im, 9 + i, 13 - i, '#1c2a5c')
    dot(im, 14, 8, '#c8ccd2')
    return im


def doc_intake():
    """The intake form: a black header, three ticked boxes, and a red stamp with the tag's number."""
    im = sheet('#ece4cf', 44436)
    fill(im, 2, 2, 14, 3, '#2a2826')
    for y in (4, 6):
        frame(im, '#4a4640', 2, y, 4, y + 2); typed_rows(im, (y,), 5, 13)
    dot(im, 2, 4, '#2a2826'); dot(im, 3, 5, '#2a2826')
    text(im, 3, 8, '264', '#b3261e'); fill(im, 3, 14, 14, 15, '#b3261e')
    return im


def doc_tags():
    """Unclaimed property: three manila tags on their strings, lying one over another. Nothing else: the table shows between them."""
    im = new()
    for (x0, y0, x1, y1), string in (((2, 2, 10, 6), '#b3261e'), ((5, 6, 14, 10), '#d8d2c2'), ((2, 10, 11, 14), '#b3261e')):
        fill(im, x0, y0, x1, y1, '#d9bf8a'); frame(im, '#b79a62', x0, y0, x1, y1)
        dot(im, x0 + 1, y0 + 1, '#3a2c20'); dot(im, x0, y0 + 1, string); dot(im, x0 - 1, y0 + 2, string)
        typed_rows(im, (y0 + 2,), x0 + 3, x1 - 1, '#4a4034')
    return im


def doc_forms():
    """A stack of the same report form: typed lines, and one line already printed in red on every sheet."""
    im = sheet('#efebe0', 44437)
    fill(im, 2, 14, 15, 15, '#bdb8ac'); fill(im, 14, 2, 15, 15, '#bdb8ac')
    fill(im, 3, 2, 12, 3, '#2a2826')
    typed_rows(im, (4, 6), 3, 12)
    fill(im, 3, 9, 12, 11, '#b3261e'); fill(im, 4, 9, 11, 10, '#d24a40')
    typed_rows(im, (12,), 3, 10)
    return im


def doc_ledger():
    """The night book, open: two ruled pages in a dark red cover, its ribbon down the gutter."""
    im = new(); fill(im, 1, 1, 15, 15, '#5a1f1c'); frame(im, '#3c1211', 1, 1, 15, 15)
    px = im.load(); rnd = random.Random(44438)
    for y in range(2, 14):
        for x in range(2, 14):
            px[x, y] = shade('#eadfc4', rnd.randint(-3, 3))
    fill(im, 7, 2, 9, 14, '#bfb294'); fill(im, 8, 8, 9, 15, '#a01c1c')
    for y in range(3, 13, 2):
        typed_rows(im, (y,), 3, 7, '#3d3a34'); typed_rows(im, (y,), 10, 13, '#3d3a34')
    return im


def doc_letter():
    """The pasted note: letters cut out of catalogues, each on its own scrap, stuck down in three crooked rows."""
    im = sheet('#d8cfb8', 44439); rnd = random.Random(44440)
    for row, y in enumerate((2, 6, 10)):
        x = 2 + row % 2
        for n in range(4 - row % 2):
            c = ('#f3efe6', '#e9d9a8', '#cfcac0', '#e8c4c0', '#c9d6e4')[(row * 3 + n * 2) % 5]
            dy = (row + n) % 2
            fill(im, x, y + dy, x + 2, y + dy + 3, c)
            # each scrap's letter: an upright stroke, or a short bar, never two dots side by side
            if (row + n) % 3 == 0:
                fill(im, x, y + dy + 1, x + 2, y + dy + 2, '#141210')
            else:
                fill(im, x + n % 2, y + dy, x + n % 2 + 1, y + dy + 2 + n % 2, '#141210')
            x += 3
    return im


def doc_receipt():
    """The county's receipt: a pink carbon, perforated down one side, its number in blue."""
    im = sheet('#e7b3b8', 44441)
    fill(im, 1, 1, 15, 3, '#cc8a92')
    for y in range(2, 14, 2):
        dot(im, 2, y, '#f6e6e8')
    text(im, 4, 4, '263', '#2c3a62')
    typed_rows(im, (10, 12), 4, 13, '#6a4a50')
    return im


# ---- 0.4.444: the lollipop jar, the comic spinner and the pharmacy's shelves, drawn again
def jar_glass():
    """The jar's glass, six pixels by nine in the tile's corner: clear, with a pale rim and a streak of light down one side."""
    im = new()
    fill(im, 0, 0, 6, 9, '#dff1f4', 36); frame(im, '#f4fbfc', 0, 0, 6, 9)
    for y in range(1, 7):
        dot(im, 1, y, '#ffffff', 150)
    return im


def lolly():
    """The lollipops' colours in four-pixel squares across the top (the wrapper's twist under each), and the white of their sticks."""
    im = new()
    for i, c in enumerate(('#d6402f', '#f2a43a', '#5ab04e', '#8a55c4')):
        fill(im, i * 4, 0, i * 4 + 4, 4, c); dot(im, i * 4 + 1, 1, shade(c, 60)[:3]); fill(im, i * 4, 4, i * 4 + 4, 8, shade(c, -30)[:3])
    fill(im, 0, 8, 16, 16, '#f3efe6')
    return im


def meds(alt):
    """The pharmacy's shelves: bottles with caps and labels, jars and boxes on three white shelves, not books. Two arrangements."""
    im = new('#2a2420', 44410 + alt, 2); rnd = random.Random(44420 + alt)
    for row in range(3):
        y = row * 5
        fill(im, 0, y + 4, 16, y + 5, '#d9d2c2')
        x = (row + alt) % 2
        while x < 14:
            kind = rnd.choice(('amber', 'amber', 'white', 'green', 'box', 'jar'))
            if kind == 'box':
                w = 3; c = rnd.choice(('#c94f3d', '#3f77b5', '#e6dcc6'))
                fill(im, x, y + 1, x + w, y + 4, c); fill(im, x, y + 2, x + w, y + 3, '#f3efe6')
            elif kind == 'jar':
                w = 3; fill(im, x, y + 1, x + w, y + 4, '#cfe1e6'); fill(im, x, y + 3, x + w, y + 4, '#b9a26a'); fill(im, x, y, x + w, y + 1, '#8f8f8f')
            else:
                w = 2; c = {'amber': '#a35a1f', 'white': '#ece8dd', 'green': '#3d6b4a'}[kind]
                fill(im, x, y + 1, x + w, y + 4, c); fill(im, x, y, x + w, y + 1, '#1c1a18')
                fill(im, x, y + 2, x + w, y + 3, '#7a8aa0' if kind == 'white' else '#f3efe6')
            x += w + 1
    fill(im, 0, 15, 16, 16, '#d9d2c2')
    return im


def rack():
    """The spinner's comics: twelve covers, four pixels by five, each with its masthead band and a figure. The first is Kit &
    Caboose: red, with a yellow masthead."""
    im = new(); rnd = random.Random(44402)
    fields = ['#c8281c', '#2f6fb3', '#3b8a3e', '#8a55c4', '#e08a2a', '#1f8f93', '#b03a78', '#30343c', '#d8b23a', '#7a4a2a', '#4a6ad0', '#a01818']
    for i, c in enumerate(fields):
        x, y = (i % 4) * 4, (i // 4) * 5
        fill(im, x, y, x + 4, y + 5, c)
        fill(im, x, y, x + 4, y + 1, '#f4d03a' if i == 0 else rnd.choice(('#f2ede2', '#f4d03a', '#101014')))
        dot(im, x + 1 + rnd.randrange(2), y + 2, '#f3e3c8'); dot(im, x + 1, y + 3, shade(c, 50)[:3]); dot(im, x + 2, y + 3, shade(c, -50)[:3])
        dot(im, x + 3, y + 4, shade(c, -70)[:3])
    return im


def comic_sill(aged):
    """0.4.445: Kit & Caboose No. 31 standing in the fort's window sill (tools/draw_borrasca_comic.py draws its pages). The front
    cover, seven pixels by ten, is at the left of the tile, the back cover with its coupon at the right, and the cut edge of the
    pages is the strip under them. After its years in the window (aged) the red has gone to buff and the black to a grey-blue."""
    im = new()
    # The front: the white of the spine, the masthead, the night, the light coming up out of the crater, the two of them either side.
    fill(im, 0, 0, 7, 10, '#101022'); fill(im, 0, 0, 7, 3, '#c82c20'); fill(im, 0, 3, 7, 4, '#f4d03a')
    for x in (1, 3, 5):
        dot(im, x, 1, '#f4d03a')
    dot(im, 6, 5, '#f6f2e6')
    fill(im, 0, 8, 7, 10, '#969690'); fill(im, 2, 8, 5, 9, '#141218'); dot(im, 3, 8, '#f4d03a')
    # The light spreads as it rises; Kit stands at one side of it, taller than Caboose at the other.
    fill(im, 3, 5, 4, 8, '#fff096'); dot(im, 2, 4, '#fff096'); dot(im, 4, 4, '#fff096')
    dot(im, 1, 7, '#c82c20'); dot(im, 1, 6, '#ecc4a0'); dot(im, 5, 7, '#b0462c')
    fill(im, 0, 0, 1, 10, '#f6f2e6')
    # The back: an advertisement, lines of small print, and the coupon with a name begun in pencil.
    # (as its page has them: the toy spacemen on yellow, the moon crystals on blue, and the coupon's three ruled lines)
    fill(im, 8, 0, 15, 10, '#e8dec4'); fill(im, 9, 1, 14, 3, '#f4d03a'); fill(im, 9, 4, 14, 6, '#3462aa')
    for x in (9, 11, 13):
        dot(im, x, 2, '#3462aa')
    for x in (10, 11, 12):
        dot(im, x, 5, '#f6f2e6')
    for y in (7, 9):
        for x in range(9, 14):
            dot(im, x, y, '#969690')
    dot(im, 9, 8, '#606068'); dot(im, 10, 8, '#606068'); dot(im, 11, 8, '#606068')
    # The edge of the pages.
    fill(im, 0, 12, 16, 14, '#e8dec4'); fill(im, 0, 14, 16, 16, '#c9bfa6')
    if aged:
        px = im.load()
        for y in range(16):
            for x in range(16):
                r, g, b, a = px[x, y]
                if a:
                    grey = (r * 30 + g * 50 + b * 20) // 100
                    r2, g2, b2 = (r + grey * 2) // 3, (g + grey * 2) // 3, (b * 2 + grey + 40) // 3
                    px[x, y] = (min(255, 96 + r2 * 150 // 255), min(255, 104 + g2 * 146 // 255), min(255, 112 + b2 * 140 // 255), a)
    return im


def cab_side_top(game):
    """A cabinet's side above the control panel: the same ground as below, with the game's badge: a miner's lamp, or a catfish."""
    if game == 0:
        im = new('#17130f', 43611, 3)
        fill(im, 6, 5, 10, 10, '#d8a42a'); fill(im, 7, 6, 9, 9, '#f6e7a0'); fill(im, 7, 4, 9, 5, '#8a6418'); fill(im, 6, 10, 10, 11, '#8a6418')
        for x, y in ((6, 3), (7, 2), (8, 2), (9, 3)):
            dot(im, x, y, '#5a5040')
        fill(im, 0, 13, 16, 14, '#d8a42a')
    else:
        im = new('#173a2a', 43612, 3)
        fill(im, 4, 6, 11, 9, '#c0b890'); fill(im, 11, 5, 12, 10, '#c0b890'); dot(im, 12, 4, '#c0b890'); dot(im, 12, 10, '#c0b890'); dot(im, 5, 7, '#10201a')
        for x, y in ((3, 8), (2, 9), (3, 6), (2, 5)):
            dot(im, x, y, '#8a8468')
        fill(im, 0, 13, 16, 14, '#3e7ea8'); fill(im, 0, 14, 16, 15, '#2c5e84')
    return im


def blood_pool():
    """0.4.444: what spreads under the Sheriff's body: an uneven dark pool, thirty-two pixels across, with a darker edge."""
    im = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); px = im.load(); rnd = random.Random(44401)
    lobes = [(15.5, 15.5, 11.5, 9.5)] + [(15.5 + rnd.uniform(-9, 9), 15.5 + rnd.uniform(-7, 7), rnd.uniform(3, 6), rnd.uniform(2.5, 5)) for _ in range(7)]
    inside = [[any(((x + .5 - cx) / rx) ** 2 + ((y + .5 - cy) / ry) ** 2 <= 1 for cx, cy, rx, ry in lobes) for x in range(32)] for y in range(32)]
    for y in range(32):
        for x in range(32):
            if not inside[y][x]:
                continue
            edge = any(not (0 <= x + dx < 32 and 0 <= y + dy < 32 and inside[y + dy][x + dx]) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            base = (58, 7, 9) if edge else (96, 12, 14)
            k = rnd.randint(-6, 6)
            px[x, y] = (max(0, base[0] + k), max(0, base[1] + k // 3), max(0, base[2] + k // 3), 235 if edge else 245)
    for x, y in ((12, 13), (13, 13), (17, 17), (18, 17), (19, 18), (10, 18)):
        px[x, y] = (128, 22, 22, 245)
    for x, y in ((3, 6), (27, 24), (28, 9), (5, 26)):
        px[x, y] = (70, 9, 10, 225)
    return im


def ward(course):
    """0.4.449 (owner, of the corridor under the depot: "Custom signs, wall texture"): painted block wall, one of a wall's three
    courses. 0 is the foot: green, scuffed dark along the floor. 1 is green to a line and cream above it. 2 is cream, stained where
    water has come down from the ceiling. The blocks under the paint are two courses to the tile, their upright joints staggered."""
    green, cream = '#7c9584', '#d6d0b9'
    im = new('#000000')
    rnd = random.Random(44900 + course)
    px = im.load()
    for y in range(16):
        for x in range(16):
            base = green if course == 0 or course == 1 and y >= 11 else cream
            k = rnd.randint(-3, 3)
            if y in (7, 15) or y < 7 and x == 15 or 7 < y < 15 and x == 7:
                k -= 12
            px[x, y] = shade(base, k)
    if course == 1:
        for x in range(16):
            px[x, 10] = shade('#4f6257', rnd.randint(-3, 3))
    if course == 0:
        for y in (13, 14, 15):
            for x in range(16):
                px[x, y] = shade('#56685c', rnd.randint(-6, 4))
        for _ in range(7):
            px[rnd.randrange(16), rnd.randint(9, 12)] = shade('#62776a', rnd.randint(-5, 5))
    if course == 2:
        for x0, length in ((4, 9), (11, 5)):
            for y in range(length):
                if rnd.random() < .8:
                    px[x0 + (y // 4) % 2, y] = shade('#b9ad88', rnd.randint(-5, 5))
        for x in range(16):
            if rnd.random() < .5:
                px[x, 0] = shade('#bdb69e', rnd.randint(-5, 5))
    return im


# 0.4.460 (owner: "Borrasca needs a custom texture pass"), the second part: the things that were still the game's own blocks
# standing in for something else. A pot-bellied stove and its pipe (a smoker under iron bars); a griddle and a fryer (a smoker and a
# furnace); an enamel sink, a steel cell toilet and a galvanised bucket (cauldrons); steel drums (barrels); boxed effects on steel
# shelving (barrels); a crane's block and hook (a heavy core); a counter bell (a bell); a jukebox (the game's jukebox).
IRON, IRON_L, IRON_D, IRON_S = '#2f3033', '#45474b', '#222325', '#17181a'
STAIN, STAIN_L, STAIN_D, STAIN_S = '#b9bec2', '#d9dddf', '#969ca1', '#737a80'


def every(uv):
    return {k: uv for k in ('north', 'south', 'east', 'west', 'up', 'down')}


def stove():
    """A pot-bellied stove's iron: its door, twelve by six, with a mica window, cold; its side, twelve by six,
    with a cast band; and down the right, four wide, its pipe with a seam every eight."""
    im = new()
    for y in range(12):
        for x in range(12):
            dot(im, x, y, IRON_L if (y in (0, 6)) else IRON_D if y in (5, 11) or x in (0, 11) else IRON)
    # the door
    for x in range(3, 9):
        dot(im, x, 1, IRON_S); dot(im, x, 4, IRON_S)
    for y in range(1, 5):
        dot(im, 3, y, IRON_S); dot(im, 8, y, IRON_S)
    dot(im, 5, 2, '#5c4d3b'); dot(im, 6, 2, '#75644c'); dot(im, 5, 3, '#3d3226'); dot(im, 6, 3, '#5c4d3b')   # mica, and no fire behind it in June
    dot(im, 9, 3, '#8f949a')                                    # its latch
    for x in (1, 2, 9, 10):
        dot(im, x, 5, IRON_S)                                   # draught slots at the foot
    # the side's cast band and two bolts
    for x in range(1, 11):
        dot(im, x, 8, IRON_L); dot(im, x, 9, IRON_S)
    dot(im, 2, 7, '#55585d'); dot(im, 9, 7, '#55585d')
    # the pipe: blued sheet, a highlight down it, a seam every eight
    for y in range(16):
        for x in range(12, 16):
            dot(im, x, y, ('#34373c', '#4a4e55', '#3b3f45', '#2a2c30')[x - 12])
        if y % 8 == 7:
            for x in range(12, 16):
                dot(im, x, y, '#1f2124')
    # legs and odd corners
    fill(im, 0, 12, 12, 16, IRON); fill(im, 0, 12, 12, 13, IRON_L); fill(im, 0, 15, 12, 16, IRON_D)
    return im


def stove_top():
    """The stove from above: a square plate, a round lid with its lifter's slot, the collar the pipe stands in."""
    im = new()
    fill(im, 2, 2, 14, 14, IRON)
    for x in range(2, 14):
        dot(im, x, 2, IRON_L); dot(im, x, 13, IRON_D)
    for y in range(2, 14):
        dot(im, 2, y, IRON_L); dot(im, 13, y, IRON_D)
    for x, y in ((6, 4), (7, 4), (8, 4), (9, 4), (5, 5), (10, 5), (4, 6), (11, 6), (4, 7), (11, 7), (4, 8), (11, 8), (4, 9), (11, 9), (5, 10), (10, 10), (6, 11), (7, 11), (8, 11), (9, 11)):
        dot(im, x, y, IRON_S)
    fill(im, 6, 6, 10, 10, IRON_D); fill(im, 7, 7, 9, 9, '#0f1011')
    return im


def grill_front():
    """A griddle's front, twelve rows: a strip of three knobs, a door with its bar handle, a dark kick."""
    im = new()
    for y in range(4, 16):
        for x in range(16):
            dot(im, x, y, STAIN_L if y in (4, 8) else STAIN_D if y in (7, 13) or x in (0, 15) else STAIN)
    for x in (3, 8, 12):
        dot(im, x, 5, '#1c1c1e'); dot(im, x + 1, 5, '#1c1c1e'); dot(im, x, 6, '#1c1c1e'); dot(im, x + 1, 6, '#3a3a3e'); dot(im, x, 5, '#c8352b' if x == 8 else '#1c1c1e')
    for x in range(3, 13):
        dot(im, x, 10, STAIN_S)
    dot(im, 3, 11, STAIN_D); dot(im, 12, 11, STAIN_D)
    fill(im, 0, 14, 16, 16, '#2a2c2f'); fill(im, 1, 14, 15, 15, '#3a3d41')
    return im


def grill_top():
    """The plate from above: seasoned steel, the scraper's streaks, a trough along the front, two patties and a heap of onions."""
    im = new()
    rnd = random.Random(460)
    for y in range(16):
        for x in range(16):
            dot(im, x, y, shade('#3b3a3a', rnd.randint(-5, 5)))
    for x in range(1, 15):
        dot(im, x, 1, '#1f1f20'); dot(im, x, 2, '#5a5857')    # the trough at the front (north)
    for x, y in ((3, 6), (4, 6), (5, 7), (6, 7), (9, 10), (10, 10), (11, 11)):
        dot(im, x, y, '#4c4a49')
    for ox, oy in ((2, 9), (7, 5)):
        fill(im, ox, oy, ox + 3, oy + 3, '#6b3f24'); dot(im, ox, oy, '#51301b'); dot(im, ox + 2, oy + 2, '#51301b'); dot(im, ox + 1, oy + 1, '#855030')
    for x, y in ((12, 6), (13, 6), (12, 7), (13, 8), (11, 7)):
        dot(im, x, y, '#d9c98f')
    for x in range(16):
        dot(im, x, 14, STAIN_D); dot(im, x, 15, STAIN)         # the splash's foot at the back
    return im


def fryer_front():
    """A fryer's front, twelve rows: a dial and its lamp, a plate with the maker's name worn off it, a door, a dark kick."""
    im = new()
    for y in range(4, 16):
        for x in range(16):
            dot(im, x, y, STAIN_L if y in (4, 8) else STAIN_D if y in (7, 13) or x in (0, 15) else STAIN)
    fill(im, 3, 5, 5, 7, '#1c1c1e'); dot(im, 4, 5, '#e6e2d6'); dot(im, 7, 6, '#e0752a')
    fill(im, 10, 5, 14, 7, '#8f2a22'); dot(im, 11, 6, '#d9c7c2'); dot(im, 12, 6, '#d9c7c2')
    for y in range(9, 13):
        dot(im, 8, y, STAIN_S)
    dot(im, 6, 10, STAIN_D); dot(im, 6, 11, STAIN_D); dot(im, 10, 10, STAIN_D); dot(im, 10, 11, STAIN_D)
    fill(im, 0, 14, 16, 16, '#2a2c2f'); fill(im, 1, 14, 15, 15, '#3a3d41')
    return im


def fryer_top():
    """Two wells of oil from above, a basket's wire rim in each."""
    im = new()
    fill(im, 0, 0, 16, 16, STAIN); fill(im, 0, 0, 16, 1, STAIN_L)
    for ox in (1, 9):
        fill(im, ox, 3, ox + 6, 13, '#7a4d16')
        for x, y in ((ox + 1, 5), (ox + 3, 7), (ox + 2, 10), (ox + 4, 11), (ox + 4, 4)):
            dot(im, x, y, '#b98a35')
        for x in range(ox, ox + 6):
            dot(im, x, 3, STAIN_S); dot(im, x, 12, STAIN_S)
        for y in range(3, 13):
            dot(im, ox, y, STAIN_S); dot(im, ox + 5, y, STAIN_S)
        for y in range(5, 12, 2):
            dot(im, ox + 2, y, '#94989c'); dot(im, ox + 3, y, '#94989c')
    for x in range(16):
        dot(im, x, 14, STAIN_D)
    return im


def sink_top():
    """An enamel sink from above: its rim, the basin's grey shadow and its drain, a bar of soap in its dish."""
    w, s, d = '#f1efe8', '#cfd0cc', '#a9aaa6'
    im = new()
    fill(im, 0, 0, 16, 16, w)
    fill(im, 2, 3, 14, 12, s)
    for x in range(2, 14):
        dot(im, x, 3, d)
    for y in range(3, 12):
        dot(im, 2, y, d)
    fill(im, 7, 7, 9, 9, '#3a3c3e'); dot(im, 7, 7, '#6d7073')
    fill(im, 11, 13, 14, 15, '#e3d7a5'); dot(im, 11, 13, '#f2e9c2')
    dot(im, 5, 6, '#b9bab6'); dot(im, 10, 9, '#b9bab6')        # water marks
    return im


def toilet():
    """A steel cell toilet: from above, a ring, the dark bowl and the water in it; and the plate on the wall with its button."""
    im = new()
    fill(im, 0, 0, 10, 11, STAIN); fill(im, 0, 0, 10, 1, STAIN_L)
    fill(im, 2, 2, 8, 9, STAIN_S); fill(im, 3, 3, 7, 8, '#2e3235'); fill(im, 4, 5, 6, 7, '#4f6470')
    dot(im, 2, 2, STAIN); dot(im, 7, 2, STAIN); dot(im, 2, 8, STAIN); dot(im, 7, 8, STAIN)
    # the wall plate
    for y in range(12):
        for x in range(10, 16):
            dot(im, x, y, STAIN_L if y == 0 else STAIN_D if x in (10, 15) or y == 11 else STAIN)
    fill(im, 13, 6, 15, 8, STAIN_S); dot(im, 13, 6, STAIN_L)   # the button, low and to one side
    dot(im, 11, 1, '#5d6368'); dot(im, 14, 10, '#5d6368')
    fill(im, 0, 11, 10, 16, STAIN); fill(im, 0, 15, 10, 16, STAIN_D); fill(im, 10, 12, 16, 16, STAIN)
    return im


def drum(body, light, dark, label):
    """A steel drum's side: two rolled hoops, a stencilled panel, a dent."""
    im = new()
    for y in range(16):
        for x in range(16):
            dot(im, x, y, light if x in (3, 4) else dark if x in (0, 15, 12) else body)
    for y in (5, 10):
        for x in range(16):
            dot(im, x, y, light)
            dot(im, x, y + 1, dark)
    fill(im, 6, 7, 11, 10, label)
    dot(im, 7, 8, dark); dot(im, 9, 8, dark); dot(im, 8, 8, dark)
    for x in range(16):
        dot(im, x, 0, dark); dot(im, x, 15, dark)
    dot(im, 13, 13, dark); dot(im, 14, 13, light)
    return im


def drum_top():
    """A drum's lid from above: a rim, a pressed ring, two bungs."""
    im = new()
    for y in range(16):
        for x in range(16):
            r2 = (x - 7.5) ** 2 + (y - 7.5) ** 2
            if r2 <= 38:
                dot(im, x, y, '#6e7479' if r2 > 30 else '#8d9398' if 14 < r2 <= 18 else '#7f858a')
    fill(im, 5, 4, 7, 6, '#4a4f54'); dot(im, 5, 4, '#9aa0a5'); dot(im, 10, 10, '#4a4f54')
    return im


def hook():
    """A crane's block and hook: yellow cheeks barred in black, the sheave's pin, and grey forged steel."""
    im = new()
    for y in range(8):
        for x in range(8):
            dot(im, x, y, '#1c1c1e' if (x + y) % 4 in (0, 1) and 1 <= y <= 6 else '#d8a520')
    fill(im, 3, 3, 5, 5, '#6b7076'); dot(im, 3, 3, '#aab0b5')
    for y in range(8, 16):
        for x in range(8):
            dot(im, x, y, ('#7c8288', '#9aa0a6', '#868c92', '#646a70')[x % 4])
    fill(im, 8, 0, 16, 16, '#7c8288')
    return im


def desk_bell():
    """A counter bell's brass: its dome, lit from one side, and the button on top."""
    im = new()
    for y in range(8):
        for x in range(8):
            dot(im, x, y, ('#8a6c2e', '#b8974a', '#dcc06f', '#f0dc98', '#dcc06f', '#b8974a', '#8a6c2e', '#6d5320')[x])
    for x in range(8):
        dot(im, x, 7, '#6d5320')
    fill(im, 8, 0, 16, 8, '#b8974a'); fill(im, 0, 8, 16, 16, '#1b1a19'); fill(im, 0, 8, 16, 9, '#34322f')
    return im


def juke_front(dark):
    """A jukebox's lower half, twelve rows: walnut cheeks, a grille of chrome bars over coloured cloth, the coin slot and a row
    of buttons under it. Unplugged, years on, the cloth is only brown."""
    wood, wood_d = '#6b4a2e', '#4a321e'
    cloth = ('#5b4a3a', '#51412f', '#5b4a3a') if dark else ('#c8352b', '#e0b83a', '#3f8f6a')
    im = new()
    for y in range(4, 16):
        for x in range(16):
            dot(im, x, y, wood_d if x in (0, 15) else wood)
    for y in range(8, 15):
        for x in range(3, 13):
            dot(im, x, y, cloth[(x - 3) * 3 // 10])
    for x in (4, 6, 8, 10, 12):
        for y in range(8, 15):
            dot(im, x - 1, y, '#d2d6d9' if not dark else '#8f9498')
    for x in range(3, 13):
        dot(im, x, 7, '#8f9498'); dot(im, x, 15, '#3b2817')
    for x in range(3, 13, 2):
        dot(im, x, 5, '#efe9d6' if not dark else '#9a917c')
    dot(im, 13, 5, '#1b1a19'); dot(im, 13, 6, '#b8974a')
    return im


def juke_top(dark):
    """A jukebox's head: an arch of lit tubes round a window with the record in it. Unplugged, the tubes are grey glass."""
    tube = ('#8a8f93', '#7b8084', '#6e7377') if dark else ('#f2c94c', '#ee8f3a', '#d9443a')
    im = new()
    fill(im, 2, 4, 14, 12, '#6b4a2e')
    for i, (x0, x1, y0, y1) in enumerate(((2, 14, 4, 12), (3, 13, 5, 12), (4, 12, 6, 12))):
        for x in range(x0, x1):
            dot(im, x, y0, tube[i])
        for y in range(y0, y1):
            dot(im, x0, y, tube[i]); dot(im, x1 - 1, y, tube[i])
    fill(im, 5, 7, 11, 12, '#15171a')
    for x, y in ((7, 9), (8, 9), (6, 10), (9, 10), (7, 11), (8, 11)):
        dot(im, x, y, '#2e3236')
    dot(im, 7, 10, '#b3261e' if not dark else '#5a2a26'); dot(im, 8, 10, '#b3261e' if not dark else '#5a2a26'); dot(im, 9, 8, '#6d7378')
    # the crown: three rows above
    for i, y in enumerate((1, 2, 3)):
        for x in range(4, 12):
            dot(im, x, y, tube[2 - i] if 5 <= x <= 10 else '#6b4a2e')
    return im


def pail():
    """A galvanised bucket: zinc in its crystals, a rolled rim, a band near the foot; from above, the dark inside it; and a
    square of board that does for a lid."""
    im = new()
    rnd = random.Random(461)
    for y in range(8):
        for x in range(8):
            dot(im, x, y, rnd.choice(('#a9b0b4', '#b9c0c4', '#98a0a5', '#c6ccd0', '#a9b0b4')))
    for x in range(8):
        dot(im, x, 0, '#d5dadd'); dot(im, x, 1, '#7d858a'); dot(im, x, 6, '#7d858a'); dot(im, x, 7, '#8f979c')
    fill(im, 8, 0, 16, 8, '#8f979c'); fill(im, 9, 1, 15, 7, '#3b4043'); fill(im, 10, 2, 14, 6, '#2a2e30')
    for y in range(8, 16):
        for x in range(8):
            dot(im, x, y, ('#9a7a52', '#8a6c46', '#a3835a', '#8a6c46')[(x + y // 3) % 4])
    dot(im, 2, 10, '#6d5233'); dot(im, 5, 13, '#6d5233')
    fill(im, 8, 8, 16, 16, '#7d858a')
    return im


def effects(other):
    """Cardboard boxes, each with a tag wired to it: a box's front and its top, a strip of tape across both. One has no lid, and
    what is folded in it shows."""
    card, card_d, card_l, tape, tag = '#b08a5a', '#8f6c40', '#c59f6c', '#d9c9a3', '#f1ede2'
    im = new()
    for y in range(16):
        for x in range(16):
            dot(im, x, y, card_d if x in (0, 15) or y in (7, 15) else card_l if y in (0, 8) else card)
    for y in range(16):
        dot(im, 7, y, tape); dot(im, 8, y, tape)
    for oy in (2, 10):
        fill(im, 10, oy, 14, oy + 3, tag); dot(im, 11, oy + 1, '#3d362e'); dot(im, 12, oy + 1, '#3d362e'); dot(im, 9, oy, '#6d7378')
    if other:
        fill(im, 1, 9, 7, 15, '#5d7f9a'); fill(im, 1, 9, 7, 10, '#7a9ab3'); dot(im, 3, 12, '#f1ede2'); dot(im, 4, 12, '#f1ede2')
    return im


STANDIN_TEX = {'pail': pail, 'effects_0': lambda: effects(False), 'effects_1': lambda: effects(True),
       'stove': stove, 'stove_top': stove_top, 'grill_front': grill_front, 'grill_top': grill_top, 'fryer_front': fryer_front, 'fryer_top': fryer_top,
       'sink_top': sink_top, 'toilet': toilet, 'drum_0': lambda: drum('#3f5f7a', '#5f84a3', '#2b4459', '#e9e3cf'),
       'drum_1': lambda: drum('#8a3b2a', '#ad5a44', '#5f2519', '#e9e3cf'), 'drum_top': drum_top, 'hook': hook, 'desk_bell': desk_bell,
       'juke_front_0': lambda: juke_front(False), 'juke_front_1': lambda: juke_front(True), 'juke_top_0': lambda: juke_top(False), 'juke_top_1': lambda: juke_top(True)}


def standin_models(m):
    side, legs = [0, 6, 12, 12], every([0, 12, 2, 14])
    m['stove_0'] = model(
        [element([3, 0, 3], [5, 2, 5], {'all': 's'}, legs), element([11, 0, 3], [13, 2, 5], {'all': 's'}, legs),
         element([3, 0, 11], [5, 2, 13], {'all': 's'}, legs), element([11, 0, 11], [13, 2, 13], {'all': 's'}, legs),
         element([3, 2, 3], [13, 3, 13], {'all': 's', 'up': 't', 'down': 't'}, {**every([1, 12, 11, 13]), 'up': [3, 3, 13, 13], 'down': [3, 3, 13, 13]}),
         element([2, 3, 2], [14, 9, 14], {'all': 's', 'up': 't', 'down': 't'}, {**every(side), 'north': [0, 0, 12, 6], 'up': [2, 2, 14, 14], 'down': [2, 2, 14, 14]}),
         element([3, 9, 3], [13, 11, 13], {'all': 's', 'up': 't'}, {**every([1, 6, 11, 8]), 'up': [3, 3, 13, 13]}),
         element([2, 11, 2], [14, 12, 14], {'all': 's', 'up': 't', 'down': 't'}, {**every([0, 12, 12, 13]), 'up': [2, 2, 14, 14], 'down': [2, 2, 14, 14]}),
         element([6, 12, 6], [10, 16, 10], {'all': 's'}, {**every([12, 0, 16, 4]), 'up': [12, 0, 16, 4]})],
        {'s': 'stove', 't': 'stove_top'}, 'stove')
    m['pipe_0'] = model([element([6, 0, 6], [10, 16, 10], {'all': 's'}, {**every([12, 0, 16, 16]), 'up': [12, 0, 16, 4], 'down': [12, 0, 16, 4]})], {'s': 'stove'}, 'stove')
    for name in ('grill', 'fryer'):
        parts = [element([0, 0, 0], [16, 12, 16], {'all': 'c', 'north': 'f'}), element([0, 12, 0], [16, 13, 16], {'all': 'c', 'up': 't'}, {'up': [0, 0, 16, 16]}),
                 element([0, 13, 14], [16, 16, 16], {'all': 'c'})]
        if name == 'fryer':
            parts += [element([3, 13, 0], [4, 14, 6], {'all': 'k'}), element([11, 13, 0], [12, 14, 6], {'all': 'k'})]
        m[f'{name}_0'] = model(parts, {'c': 'chrome', 'f': f'{name}_front', 't': f'{name}_top', 'k': 'black'}, 'chrome')
    leg = every([0, 0, 1, 9])
    m['sink_0'] = model(
        [element([1, 0, 3], [2, 9, 4], {'all': 'c'}, leg), element([14, 0, 3], [15, 9, 4], {'all': 'c'}, leg),
         element([1, 0, 14], [2, 9, 15], {'all': 'c'}, leg), element([14, 0, 14], [15, 9, 15], {'all': 'c'}, leg),
         element([0, 9, 2], [16, 14, 16], {'all': 'e', 'up': 't'}, {'up': [0, 0, 16, 14]}),
         element([0, 14, 14], [16, 16, 16], {'all': 'e'}),
         element([4, 14, 12], [5, 16, 13], {'all': 'c'}, every([2, 0, 3, 2])), element([11, 14, 12], [12, 16, 13], {'all': 'c'}, every([2, 0, 3, 2])),
         element([7, 15, 9], [9, 16, 14], {'all': 'c'}, every([2, 0, 4, 1]))],
        {'c': 'chrome', 'e': 'enamel', 't': 'sink_top'}, 'enamel')
    m['toilet_0'] = model(
        [element([4, 0, 3], [12, 6, 12], {'all': 't'}, every([0, 11, 8, 16])),
         element([3, 6, 2], [13, 7, 13], {'all': 't'}, {**every([0, 11, 10, 12]), 'up': [0, 0, 10, 11]}),
         element([2, 0, 12], [14, 12, 16], {'all': 't'}, {**every([10, 12, 16, 16]), 'north': [10, 0, 16, 12], 'up': [0, 11, 10, 15]})],
        {'t': 'toilet'}, 'toilet')
    for stage in range(2):
        d = {'all': 'd', 'up': 't', 'down': 't'}
        m[f'drum_{stage}'] = model(
            [element([2, 0, 4], [14, 16, 12], d, {'north': [2, 0, 14, 16], 'south': [2, 0, 14, 16], 'east': [4, 0, 12, 16], 'west': [4, 0, 12, 16]}),
             element([4, 0, 2], [12, 16, 14], d, {'north': [4, 0, 12, 16], 'south': [4, 0, 12, 16], 'east': [2, 0, 14, 16], 'west': [2, 0, 14, 16]})],
            {'d': f'drum_{stage}', 't': 'drum_top'}, f'drum_{stage}')
        w = {'all': 'w'}
        m[f'jukebox_{stage}'] = model(
            [element([1, 0, 4], [15, 12, 15], {**w, 'north': 'f'}),
             element([2, 12, 5], [14, 20, 14], {**w, 'north': 'h'}, {'north': [2, 4, 14, 12]}),
             element([4, 20, 6], [12, 23, 13], {**w, 'north': 'h'}, {'north': [4, 1, 12, 4]})],
            {'w': 'wood', 'f': f'juke_front_{stage}', 'h': f'juke_top_{stage}'}, 'wood')
    z = every([0, 1, 7, 7])
    for stage in range(2):
        parts = [element([5, 0, 5], [11, 1, 11], {'all': 'p'}, every([1, 7, 7, 8])),
                 element([4.5, 1, 4.5], [11.5, 7, 11.5], {'all': 'p'}, z),
                 element([4, 7, 4], [12, 8, 12], {'all': 'p'}, {**every([0, 0, 8, 1]), 'up': [8, 0, 16, 8]})]
        if stage:
            parts.append(element([3, 8, 3], [13, 8.6, 13], {'all': 'p'}, {**every([0, 8, 8, 9]), 'up': [0, 8, 8, 16], 'down': [0, 8, 8, 16]}))
        else:
            wire = every([8, 8, 9, 9])
            parts += [element([4, 8, 7.6], [4.4, 11, 8.4], {'all': 'p'}, wire), element([11.6, 8, 7.6], [12, 11, 8.4], {'all': 'p'}, wire),
                      element([4, 11, 7.6], [12, 11.4, 8.4], {'all': 'p'}, wire)]
        m[f'pail_{stage}'] = model(parts, {'p': 'pail'}, 'pail')
        c, st = {'all': 'c'}, {'all': 's'}
        up = every([0, 0, 1, 16])
        shelf = [element([0, 0, 3], [1, 16, 4], st, up), element([15, 0, 3], [16, 16, 4], st, up), element([0, 0, 13], [1, 16, 14], st, up), element([15, 0, 13], [16, 16, 14], st, up),
                 element([0, 0, 3], [16, 1, 14], st), element([0, 8, 3], [16, 9, 14], st)]
        front, top = [0, 8, 7, 14], [0, 0, 7, 7]
        boxes = [((1, 1, 4), (8, 7, 13), front), ((9, 1, 4), (15, 6, 13), [9, 9, 15, 14]), ((1, 9, 4), (7, 14, 13), [9, 1, 15, 6]), ((8, 9, 5), (15, 15, 12), [0, 0, 7, 6])]
        if stage:
            boxes = [((1, 1, 4), (7, 6, 13), [9, 9, 15, 14]), ((8, 1, 4), (15, 7, 13), front), ((1, 9, 5), (9, 15, 12), [0, 0, 8, 6]), ((10, 9, 4), (15, 13, 13), [9, 1, 14, 5])]
        for lo, hi, f in boxes:
            shelf.append(element(list(lo), list(hi), c, {**every(f), 'up': [0, 9, hi[0] - lo[0], 15] if stage and lo == (1, 9, 5) else [0, 0, hi[0] - lo[0], 7]}))
        m[f'effects_{stage}'] = model(shelf, {'c': f'effects_{stage}', 's': 'steel'}, f'effects_{stage}')
    steel = every([0, 8, 2, 12])
    m['hook_0'] = model(
        [element([5, 8, 6], [11, 16, 10], {'all': 'h'}, {**every([0, 0, 4, 8]), 'north': [0, 0, 6, 8], 'south': [0, 0, 6, 8]}),
         element([7, 3, 7], [9, 8, 9], {'all': 'h'}, steel), element([7, 1, 7], [12, 3, 9], {'all': 'h'}, every([0, 8, 5, 10])),
         element([10, 3, 7], [12, 5, 9], {'all': 'h'}, steel)],
        {'h': 'hook'}, 'hook')
    m['desk_bell_0'] = model(
        [element([5, 0, 5], [11, 1, 11], {'all': 'b'}, every([0, 8, 6, 9])),
         element([6, 1, 6], [10, 3, 10], {'all': 'b'}, {**every([2, 0, 6, 2]), 'up': [9, 1, 13, 5]}),
         element([7, 3, 7], [9, 3.5, 9], {'all': 'b'}, every([3, 0, 5, 1])),
         element([7.5, 3.5, 7.5], [8.5, 4.5, 8.5], {'all': 'b'}, every([3, 0, 4, 1]))],
        {'b': 'desk_bell'}, 'desk_bell')


def textures():
    out = {f'block/borrasca_{n}.png': f() for n, f in (('checker', checker), ('lino', lino), ('tin', tin))}
    out.update({f'block/borrasca_ward_{c}.png': ward(c) for c in range(3)})
    out['entity/borrasca_blood_pool.png'] = blood_pool()
    faces = {
        'wood': wood(), 'chrome': chrome(), 'glass': glass(), 'enamel': enamel(), 'black': black(),
        'pie': pie(False), 'pie_cut': pie(True), 'crust': crust(), 'urn': urn(), 'menu': menu(),
        'pegboard': pegboard(False), 'pegboard_taken': pegboard(True),
        'pump_door': pump_door(), 'pump_side': pump_side(), 'pump_side_top': pump_side_top(), 'pump_dial_0': pump_dial(0), 'pump_dial_1': pump_dial(1),
        'pump_cap': pump_cap(), 'pump_trim': pump_trim(), 'pump_globe': pump_globe(), 'pump_base': pump_base(), 'pump_hose': pump_hose(),
        'pew_wood': pew_wood(), 'pew_end': pew_end(), 'pew_books': pew_books(), 'bench_wood': bench_wood(), 'bench_iron': bench_iron(), 'baton': baton(),
        'price': price(), 'price_later': price(True),
        'payphone': payphone(), 'cooler_front': cooler_front(), 'cooler_top': cooler_top(), 'candy': candy(),
        'velvet': velvet(), 'curios': curios(), 'helmet': helmet(), 'lamp': lamp(), 'map': mine_map(), 'photos': photos(), 'clock': clock(),
        'scale': scale_face(), 'jar_glass': jar_glass(), 'lolly': lolly(), 'cards': cards(), 'exam_top': exam_top(), 'exam_side': exam_side(),
        'chart': chart(), 'fountain': fountain(), 'meds': meds(0), 'meds_b': meds(1), 'rack': rack(), 'open': open_sign(),
        'steel': steel(), 'oil_blue': oil('#2f5f9e', '#f4d03a', '#26252a'), 'oil_red': oil('#b8312b', '#f2ede2', '#d8b23a'), 'jug': jug(),
        'bait_front': bait_front(), 'bait_top': bait_top(), 'cup': cup(), 'chips': chips(), 'calendar': calendar(),
        'bait_sign': bait_sign(), 'tire': tire(), 'tire_top': tire_top(),
        'tins': tins(), 'boxes': boxes(), 'sack': sack(), 'plate': plate(), 'mug': mug(),
        'bike_paint': bike_paint(), 'bike_seat': bike_seat(),
        'cab_side_0': cab_side(0), 'cab_side_1': cab_side(1), 'cab_side_top_0': cab_side_top(0), 'cab_side_top_1': cab_side_top(1), 'cab_coin': cab_coin(), 'cab_panel': cab_panel(),
        'cab_screen_0': cab_screen(0), 'cab_screen_1': cab_screen(1), 'cab_marquee_0': cab_marquee(0), 'cab_marquee_1': cab_marquee(1),
        'cab_red': new('#c8281c', 43609, 3), 'cab_yellow': new('#e8c030', 43610, 3),
        'gore': gore(), 'smear': smear(0), 'smear_soaked': smear(1), 'bone': bone(), 'hair': hair(), 'slipper': slipper(),
        'wristband': wristband(),
        'doc_dispatch': doc_dispatch(), 'doc_payroll': doc_payroll(), 'doc_signin': doc_signin(), 'doc_intake': doc_intake(), 'doc_tags': doc_tags(),
        'doc_forms': doc_forms(), 'doc_ledger': doc_ledger(), 'doc_letter': doc_letter(), 'doc_receipt': doc_receipt(), 'doc_log': doc_log(),
        'comic': comic_sill(False), 'comic_aged': comic_sill(True),
    }
    faces.update({name: draw() for name, draw in STANDIN_TEX.items()})
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
    pump_models(m)
    seat_models(m)
    # 0.4.457: the Sheriff's baton, lying where it fell.
    m['baton_0'] = model([element([2, 0, 7], [14, 1.5, 8.5], {'all': 'b'}, {**{k: [0, 4, 12, 5.5] for k in ('north', 'south', 'up', 'down')}, 'east': [8, 4, 9.5, 5.5], 'west': [8, 4, 9.5, 5.5]}),
                          element([9, 0, 8.5], [10.5, 1.5, 11], {'all': 'b'}, {k: [8, 4, 10.5, 5.5] for k in ('north', 'south', 'east', 'west', 'up', 'down')})],
                         {'b': 'baton'}, 'baton')
    m['price_0'] = wall('price', 'chrome')
    m['price_1'] = wall('price_later', 'chrome')
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
    # 0.4.444 (owner: "This ain't exactly the candy jar you promised lol"): a glass jar with a lid, and lollipops standing in it on
    # their sticks; nearly empty, two lie in the bottom.
    def lollipop(i, x, z, h):
        u = (i % 4) * 4
        return [element([x + .25, .5, z + .25], [x + .75, h, z + .75], {'all': 'l'}, {k: [0, 9, 1, 15] for k in ('north', 'south', 'east', 'west')}),
                element([x - .25, h, z - .25], [x + 1.25, h + 1.5, z + 1.25], {'all': 'l'}, {k: [u, 0, u + 2, 2] for k in ('north', 'south', 'east', 'west', 'up', 'down')})]
    walls = {k: [0, 0, 6, 9] for k in ('north', 'south', 'east', 'west')}
    glass_jar = [element([5, 0, 5], [11, 9, 11], {'north': 'g', 'south': 'g', 'east': 'g', 'west': 'g', 'down': 'g'}, {**walls, 'down': [0, 0, 6, 6]}),
                 element([4.5, 9, 4.5], [11.5, 10, 11.5], {'all': 'c'}), element([7.25, 10, 7.25], [8.75, 11, 8.75], {'all': 'c'})]
    full = [e for i, (x, z, h) in enumerate(((6, 6, 5), (8.6, 6.2, 6), (7, 8.6, 5.5), (9, 8.8, 4.8), (6.2, 9.2, 6.2), (8, 7.4, 6.6))) for e in lollipop(i, x, z, h)]
    low = [element([6, .5, 7], [10, 1, 7.5], {'all': 'l'}, {k: [0, 9, 4, 10] for k in ('north', 'south', 'up', 'down')}),
           element([5.4, .3, 6.5], [6.9, 1.8, 8], {'all': 'l'}, {k: [0, 0, 2, 2] for k in ('north', 'south', 'east', 'west', 'up', 'down')}),
           element([8, .5, 8.5], [8.5, 1, 10.5], {'all': 'l'}, {k: [0, 9, 1, 11] for k in ('east', 'west', 'up', 'down')}),
           element([7.5, .3, 9.9], [9, 1.8, 10.9], {'all': 'l'}, {k: [8, 0, 10, 2] for k in ('north', 'south', 'east', 'west', 'up', 'down')})]
    for stage, inside in ((0, full), (1, low)):
        m[f'jar_{stage}'] = model(inside + glass_jar, {'g': 'jar_glass', 'c': 'chrome', 'l': 'lolly'}, 'lolly', True)
    m['cards_0'] = wall('cards')
    m['exam_0'] = model([element([0, 6, 2], [16, 10, 14], {'all': 's', 'up': 't'}), element([1, 0, 3], [3, 6, 5], {'all': 'c'}),
                         element([13, 0, 3], [15, 6, 5], {'all': 'c'}), element([1, 0, 11], [3, 6, 13], {'all': 'c'}),
                         element([13, 0, 11], [15, 6, 13], {'all': 'c'})], {'s': 'exam_side', 't': 'exam_top', 'c': 'chrome'}, 'exam_top')
    m['chart_0'] = wall('chart', 'enamel', 0)
    m['fountain_0'] = model([element([2, 0, 6], [14, 3, 12], {'all': 'c'}), element([3, 3, 8], [13, 10, 10], {'all': 'c', 'north': 'f'})],
                            {'c': 'chrome', 'f': 'fountain'}, 'fountain')
    for stage, face in ((0, 'meds'), (1, 'meds_b')):
        m[f'meds_{stage}'] = model([element([0, 0, 0], [16, 16, 16], {'all': 'w', 'north': 'm'})], {'w': 'wood', 'm': face}, face)
    # 0.4.444 (owner: "Needs an improved texture."): a real spinner: a pole on a foot, and on each of its four sides three wire
    # pockets of two comics, their covers outward.
    spinner = [element([7.25, 0, 7.25], [8.75, 16, 8.75], {'all': 'c'}), element([4.5, 0, 4.5], [11.5, .75, 11.5], {'all': 'k'}),
               element([6.5, 15.25, 6.5], [9.5, 16, 9.5], {'all': 'k'})]
    for tier, y in enumerate((1.5, 6.25, 11)):
        for side, (lo, hi, face, back) in enumerate((([4, y, 3.5], [12, y + 4.5, 4], 'north', 'south'), ([4, y, 12], [12, y + 4.5, 12.5], 'south', 'north'),
                                                      ([12, y, 4], [12.5, y + 4.5, 12], 'east', 'west'), ([3.5, y, 4], [4, y + 4.5, 12], 'west', 'east'))):
            u, v = ((tier + side) % 2) * 8, tier * 5
            spinner.append(element(lo, hi, {face: 'r', back: 'w', 'up': 'w', 'down': 'w'}, {face: [u, v, u + 8, v + 4.5]}))
        for lo, hi in (([3.25, y - .4, 3.25], [12.75, y, 3.75]), ([3.25, y - .4, 12.25], [12.75, y, 12.75]), ([3.25, y - .4, 3.75], [3.75, y, 12.25]), ([12.25, y - .4, 3.75], [12.75, y, 12.25])):
            spinner.append(element(lo, hi, {'all': 'c'}))
    m['rack_0'] = model(spinner, {'c': 'chrome', 'k': 'black', 'r': 'rack', 'w': 'enamel'}, 'rack')
    # 0.4.444 (owner: "open sign eating glass, also unfinished"): the sign hangs on two wires at the front of its own block, inside
    # the shop and against the window, its letters to the street and a plain housing to the room.
    m['open_0'] = model([element([0, 4, .2], [16, 11, 1.2], {'all': 'b', 'north': 'o'}, {'north': [0, 5, 16, 12]}),
                         element([2, 11, .5], [2.5, 16, 1], {'all': 'b'}), element([13.5, 11, .5], [14, 16, 1], {'all': 'b'})], {'b': 'black', 'o': 'open'}, 'open')
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
    m['bike_frame'] = model(bike_parts(wheels=False, standing=False), bike, 'bike_paint')
    m['bike_wheel'] = model(bike_wheel(), bike, 'tire')
    m['bike_cranks'] = model(bike_cranks(), bike, 'chrome')
    cabinet_models(m)
    feeding_models(m)
    paper_models(m)
    comic_models(m)
    standin_models(m)
    return m


def pump_models(m):
    """The pump's two halves, looking north; its back is a second face, and the hose hangs down its east side."""
    tex = {'d': 'pump_door', 's': 'pump_side', 'u': 'pump_side_top', 'c': 'pump_cap', 't': 'pump_trim', 'g': 'pump_globe', 'b': 'pump_base', 'h': 'pump_hose'}
    lower = [element([1, 0, 3], [15, 2, 14], {'all': 'b'}),
             element([2, 2, 4], [14, 14, 13], {'north': 'd', 'south': 'd', 'east': 's', 'west': 's'}),
             element([1.75, 14, 3.75], [14.25, 16, 13.25], {'all': 't'}),
             # The hose: down from the head, round under, and up to the nozzle hung in its boot.
             element([14.5, 4, 10.5], [15.5, 16, 11.5], {'north': 'h', 'south': 'h', 'east': 'h'}),
             element([14.5, 3, 7], [15.5, 4, 11.5], {'north': 'h', 'south': 'h', 'east': 'h', 'up': 'h', 'down': 'h'}),
             element([14.5, 4, 7], [15.5, 8, 8], {'north': 'h', 'south': 'h', 'east': 'h'}),
             element([14.25, 8, 6.5], [15.75, 10, 8.5], {'north': 'h', 'south': 'h', 'east': 'h', 'up': 'h', 'down': 'h'}),
             element([14.25, 10, 6.5], [15.75, 12, 8.5], {'north': 't', 'south': 't', 'east': 't', 'up': 't'}),
             element([14, 12, 6], [16, 15, 9], {'north': 'b', 'south': 'b', 'east': 'b', 'up': 'b', 'down': 'b'})]
    cap = {k: [2, 0, 15, 2] for k in ('north', 'south')} | {k: [3, 0, 13, 2] for k in ('east', 'west')}
    rim = {k: [0, 8, 2, 14] for k in ('east', 'west')} | {k: [0, 8, 6, 10] for k in ('up', 'down')}
    for which in (0, 1):
        upper = [element([2, 0, 4], [14, 12, 13], {'north': 'f', 'south': 'f', 'east': 'u', 'west': 'u'}),
                 element([1.5, 12, 3.5], [14.5, 14, 13.5], {'all': 'c'}, cap),
                 element([7, 14, 7.5], [9, 15, 9.5], {'north': 't', 'south': 't', 'east': 't', 'west': 't'}),
                 element([5, 15, 7.5], [11, 21, 9.5], {'all': 'g'}, {'north': [5, 0, 11, 6], 'south': [5, 0, 11, 6], **rim}),
                 element([4, 16, 7.5], [12, 20, 9.5], {'all': 'g'}, {'north': [4, 1, 12, 5], 'south': [4, 1, 12, 5], **{k: [0, 8, 2, 12] for k in ('east', 'west')}, **{k: [0, 8, 8, 10] for k in ('up', 'down')}}),
                 element([14, 8, 10], [15.5, 9.5, 12], {'north': 't', 'south': 't', 'east': 't', 'up': 't', 'down': 't'}),
                 element([14.5, 0, 10.5], [15.5, 8, 11.5], {'north': 'h', 'south': 'h', 'east': 'h'})]
        m[f'pump_{which}'] = model(lower, tex, 'pump_side')
        m[f'pump_top_{which}'] = model(upper, {**tex, 'f': f'pump_dial_{which}'}, 'pump_side_top')


def seat_models(m):
    """0.4.454: the church's pews and the town's benches, looking north: whoever sits looks north, and the back is to the south.
    An end's own end is to the west (stage 0) or the east (stage 1) as the seat looks."""
    oak = {'w': 'pew_wood', 'e': 'pew_end', 'k': 'pew_books'}

    def pew(x0, x1):
        parts = [element([x0, 6, 3], [x1, 8, 13], {'all': 'w'}), element([x0, 8, 12], [x1, 15, 14], {'all': 'w'}), element([x0, 15, 11], [x1, 16, 15], {'all': 'w'}),
                 # The rack on its back, for the row behind: a board, a lip, and the hymnals standing in it.
                 element([x0 + 1, 9, 14], [x1 - 1, 10, 16], {'all': 'w'}), element([x0 + 1, 10, 15.5], [x1 - 1, 11, 16], {'all': 'w'})]
        for x, cover in ((x0 + 2, 0), (x0 + 5, 4), (x1 - 4, 0)):
            parts.append(element([x, 10, 14.2], [x + 2, 13.5, 15.4], {'all': 'k'},
                                 {**{k: [cover, 0, cover + 2, 3.5] for k in ('north', 'south')}, **{k: [cover + 2, 0, cover + 3, 3.5] for k in ('east', 'west')},
                                  'up': [cover, 8, cover + 2, 9.2], 'down': [cover, 8, cover + 2, 9.2]}))
        return parts

    def end(x):
        """The shaped end: low under the arm at the front, rising in steps to the top of the back."""
        return [element([x, 0, 2], [x + 2, 10, 15], {'all': 'w', 'east': 'e', 'west': 'e'}), element([x, 10, 8], [x + 2, 13, 15], {'all': 'w', 'east': 'e', 'west': 'e'}),
                element([x, 13, 10], [x + 2, 16, 15], {'all': 'w', 'east': 'e', 'west': 'e'})]

    m['pew_mid_0'] = model(pew(0, 16), oak, 'pew_wood')
    m['pew_end_0'] = model(pew(2, 16) + end(0), oak, 'pew_wood')
    m['pew_end_1'] = model(pew(0, 14) + end(14), oak, 'pew_wood')

    iron = {'w': 'bench_wood', 'i': 'bench_iron'}

    def slat(lo, hi):
        """A slat, its top from the top of the picture and its edges from the rows drawn for their depth."""
        deep = {1: 4, 2: 6, 3: 9}[hi[1] - lo[1]]
        long = {k: [lo[0], deep, hi[0], deep + hi[1] - lo[1]] for k in ('north', 'south')}
        across = {k: [0, deep, hi[2] - lo[2], deep + hi[1] - lo[1]] for k in ('east', 'west')}
        return element(lo, hi, {'all': 'w'}, {**long, **across, 'up': [lo[0], 0, hi[0], hi[2] - lo[2]], 'down': [lo[0], 1, hi[0], 1 + hi[2] - lo[2]]})

    def bench(x0, x1):
        return [slat([x0, 6, 3], [x1, 7, 5]), slat([x0, 6, 6], [x1, 7, 8]), slat([x0, 6, 9], [x1, 7, 11]),
                slat([x0, 9, 12], [x1, 11, 13]), slat([x0, 12, 13], [x1, 15, 14])]

    def frame(x):
        """A cast end: the leg in front that carries the arm, the rail the slats lie on, and the post behind them that leans as the
        back does. Its posts stand clear of the slats' own faces, in front of the seat and behind the back."""
        return [element([x, 0, 2], [x + 1, 10, 3], {'all': 'i'}), element([x, 10, 2], [x + 1, 11, 13], {'all': 'i'}), element([x, 5, 3], [x + 1, 6, 13], {'all': 'i'}),
                element([x, 0, 13], [x + 1, 12, 14], {'all': 'i'}), element([x, 12, 14], [x + 1, 16, 15], {'all': 'i'})]

    m['bench_mid_0'] = model(bench(0, 16), iron, 'bench_wood')
    m['bench_end_0'] = model(bench(0, 16) + frame(0), iron, 'bench_wood')
    m['bench_end_1'] = model(bench(0, 16) + frame(15), iron, 'bench_wood')
    m['bench_0'] = model(bench(0, 16) + frame(0) + frame(15), iron, 'bench_wood')


def comic_models(m):
    """0.4.445: the comic in the fort's window. It stands past its own block, on the ledge the wall leaves inside the pane behind
    it (the glass is seven pixels beyond the block's back), and leans its top against the glass: the front cover to the room, the
    back cover to whoever looks in from the bough."""
    edge = [0, 12, 7, 12.5]
    for stage, tex in ((0, 'comic'), (1, 'comic_aged')):
        m[f'comic_{stage}'] = model([element([4.5, 0, 18.3], [11.5, 10, 18.8], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c', 'down': 'c'},
                                             {'north': [0, 0, 7, 10], 'south': [8, 0, 15, 10], 'east': [0, 12, .5, 14], 'west': [0, 12, .5, 14], 'up': edge, 'down': [0, 14, 7, 14.5]},
                                             {'origin': [8, 0, 18.55], 'axis': 'x', 'angle': 22.5})], {'c': tex}, tex)


def paper_models(m):
    """0.4.444: each paper lies flat, fourteen pixels square, its face the middle of its tile. The books and the stack stand a little
    higher, and a clipboard has its clip."""
    def flat(face, h=.3):
        return element([1, 0, 1], [15, h, 15], {'all': 'd'}, {'up': [1, 1, 15, 15], 'down': [1, 1, 15, 15], **{k: [1, 14, 15, 14 + min(1, h)] for k in ('north', 'south', 'east', 'west')}})
    for name, h, extra in (('dispatch', .6, [element([6, .6, 1], [10, 1.1, 3], {'all': 'c'})]), ('log', .6, [element([6, .6, 1], [10, 1.1, 3], {'all': 'c'})]),
                           ('payroll', .3, []), ('signin', .3, []), ('intake', .3, []), ('tags', .3, []), ('forms', 1.5, []), ('ledger', 1.2, []),
                           ('letter', .3, []), ('receipt', .3, [])):
        m[f'doc_{name}_0'] = model([flat(name, h)] + extra, {'d': f'doc_{name}', 'c': 'chrome'}, f'doc_{name}')


def feeding_models(m):
    """0.4.439: what comes off the far end of the belt, and what the belt carries out of the slot. North-facing like the rest."""
    g = {'g': 'gore', 'b': 'bone', 'h': 'hair', 's': 'slipper', 'w': 'wristband', 'm': 'smear', 'n': 'smear_soaked'}
    # Pulp and bone: a slumped heap, the lumps on it, and shards and a rib standing out of it.
    m['remains_0'] = model([element([2, 0, 2], [14, 1.5, 14], {'all': 'g'}), element([4, 1.5, 4], [10, 4, 10], {'all': 'g'}),
                            element([8, 1.5, 7], [12, 3, 12], {'all': 'g'}),
                            element([6, 2, 6], [7, 6.5, 7], {'all': 'b'}, None, {'angle': 22.5, 'axis': 'z', 'origin': [6.5, 2, 6.5]}),
                            element([8, 3, 8], [13, 3.8, 9], {'all': 'b'}, None, {'angle': -22.5, 'axis': 'y', 'origin': [10, 3, 8.5]}),
                            element([3, 1.5, 11], [7, 2.3, 12], {'all': 'b'})], g, 'gore')
    # Hair and a felt slipper in what is left of the rest.
    m['remains_1'] = model([element([1, 0, 1], [15, 1, 15], {'all': 'g'}), element([3, 1, 3], [8, 2.5, 9], {'all': 'h'}),
                            element([8, 1, 6], [12, 3.5, 14], {'all': 's', 'up': 's'}, None, {'angle': 22.5, 'axis': 'y', 'origin': [10, 1, 10]}),
                            element([4, 1, 10], [7, 3, 13], {'all': 'g'})], g, 'gore')
    # The band from her wrist, cut, lying open in a thin smear with its card turned up.
    m['wristband_0'] = model([element([2, 0, 3], [14, .3, 13], {'up': 'm', 'down': 'm'}),
                              element([3, .3, 7], [13, .9, 9], {'all': 'w'}, {k: [0, 6, 16, 9] for k in ('north', 'south', 'east', 'west', 'up', 'down')}),
                              element([6, .3, 5.5], [10, 1.1, 10.5], {'all': 'w', 'up': 'w'}, {'up': [0, 0, 16, 16]})], g, 'wristband')
    # The belt where it comes out of the slot: a streak, then all of it running wet. Thin enough to lie on the belt's rubber.
    m['smear_0'] = model([element([0, 0, 0], [16, .15, 16], {'up': 'm', 'down': 'm'})], g, 'smear', True)
    m['smear_1'] = model([element([0, 0, 0], [16, .15, 16], {'up': 'n', 'down': 'n'})], g, 'smear_soaked', True)


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
         'oil', 'bait', 'snacks', 'calendar', 'bait_sign', 'tires', 'groceries', 'crockery', 'bike', 'cabinet', 'cabinet_top',
         'remains', 'wristband', 'smear',
         'doc_dispatch', 'doc_payroll', 'doc_signin', 'doc_intake', 'doc_tags', 'doc_forms', 'doc_ledger', 'doc_letter', 'doc_receipt', 'doc_log',
         'comic', 'pew_end', 'pew_mid', 'bench', 'bench_end', 'bench_mid', 'baton',
         'stove', 'pipe', 'grill', 'fryer', 'sink', 'toilet', 'pail', 'drum', 'effects', 'hook', 'desk_bell', 'jukebox']
STAGES = {'pie_case': 2, 'pegboard': 2, 'jar': 2, 'meds': 2, 'bike': 2, 'cabinet': 2, 'cabinet_top': 2, 'remains': 2, 'smear': 2, 'comic': 2,
          'pump': 2, 'pump_top': 2, 'pew_end': 2, 'bench_end': 2, 'price': 2, 'pail': 2, 'drum': 2, 'effects': 2, 'jukebox': 2}
FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
NAMES = {'borrasca_shop': 'Shop Fitting', 'borrasca_checker': 'Checkered Floor', 'borrasca_lino': 'Linoleum', 'borrasca_tin': 'Pressed Tin',
         'borrasca_ward': 'Painted Block Wall'}


def files():
    out = {}
    for name in ('checker', 'lino', 'tin'):
        out[f'models/block/borrasca_{name}.json'] = {'parent': 'minecraft:block/cube_all', 'textures': {'all': NS + f'block/borrasca_{name}'}}
        out[f'blockstates/borrasca_{name}.json'] = {'variants': {'': {'model': NS + f'block/borrasca_{name}'}}}
    # 0.4.449: the corridor's painted wall: painted on the side it faces, the game's own bare concrete on the rest.
    bare = 'minecraft:block/light_gray_concrete'
    for c in range(3):
        out[f'models/block/borrasca_ward_{c}.json'] = {'parent': 'minecraft:block/cube', 'textures': {
            'north': NS + f'block/borrasca_ward_{c}', 'south': bare, 'east': bare, 'west': bare, 'up': bare, 'down': bare, 'particle': bare}}
    out['blockstates/borrasca_ward.json'] = {'variants': {
        f'course={c},facing={facing}': {'model': NS + f'block/borrasca_ward_{c}', **({'y': y} if y else {})} for c in range(3) for facing, y in FACING_Y.items()}}
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
    out['models/item/borrasca_baton.json'] = baton_item()
    return out


# How each side of a model is seen: which face shows, how its box lies on the page, and which boxes stand in front.
VIEWS = {
    'north': ('north', lambda lo, hi: (16 - hi[0], 16 - lo[0], hi[1], lo[1]), lambda lo, hi: -lo[2]),
    'south': ('south', lambda lo, hi: (lo[0], hi[0], hi[1], lo[1]), lambda lo, hi: hi[2]),
    'east': ('east', lambda lo, hi: (16 - hi[2], 16 - lo[2], hi[1], lo[1]), lambda lo, hi: hi[0]),
    'west': ('west', lambda lo, hi: (lo[2], hi[2], hi[1], lo[1]), lambda lo, hi: -lo[0]),
    'up': ('up', lambda lo, hi: (lo[0], hi[0], 40 - lo[2], 40 - hi[2]), lambda lo, hi: hi[1]),
}


def seen(stack, view, images, scale=10, tall=40):
    """One side of a stack of models (each with the height it stands at), straight on, as its faces are drawn: for review."""
    face, lie, near = VIEWS[view]
    page = Image.new('RGBA', (16 * scale, tall * scale), (0, 0, 0, 0))
    boxes = []
    for obj, lift in stack:
        for e in obj['elements']:
            if face in e['faces'] and 'rotation' not in e:
                lo, hi = list(e['from']), list(e['to']); lo[1] += lift; hi[1] += lift
                boxes.append((near(lo, hi), lie(lo, hi), e['faces'][face], obj['textures'], (e['from'], e['to'])))
    for _, (x0, x1, top, bottom), f, names, (lo, hi) in sorted(boxes, key=lambda b: b[0]):
        tex = images['block/borrasca_shop/' + names[f['texture'][1:]].split('borrasca_shop/')[1] + '.png']
        u0, v0, u1, v1 = f.get('uv') or {'north': [16 - hi[0], 16 - hi[1], 16 - lo[0], 16 - lo[1]], 'south': [lo[0], 16 - hi[1], hi[0], 16 - lo[1]],
                                         'east': [16 - hi[2], 16 - hi[1], 16 - lo[2], 16 - lo[1]], 'west': [lo[2], 16 - hi[1], hi[2], 16 - lo[1]],
                                         'up': [lo[0], lo[2], hi[0], hi[2]]}[face]
        left, right, y0, y1 = round(x0 * scale), round(x1 * scale), round((tall - top) * scale), round((tall - bottom) * scale)
        for py in range(y0, y1):
            for qx in range(left, right):
                u = u0 + (qx + .5 - left) / (right - left) * (u1 - u0); v = v0 + (py + .5 - y0) / (y1 - y0) * (v1 - v0)
                c = tex.getpixel((min(15, max(0, int(u))), min(15, max(0, int(v)))))
                if c[3] and 0 <= qx < page.width and 0 <= py < page.height:
                    page.putpixel((qx, py), c)
    return page


def review(path, images):
    """A page for review: the pump, a pew and a bench, each from its four sides and from above."""
    models = shop_models()
    stacks = [[(models['pump_0'], 0), (models['pump_top_0'], 16)], [(models['pump_1'], 0), (models['pump_top_1'], 16)],
              [(models['pew_end_0'], 0)], [(models['pew_mid_0'], 0)], [(models['pew_end_1'], 0)],
              [(models['bench_0'], 0)], [(models['bench_end_0'], 0)], [(models['bench_mid_0'], 0)]]
    scale = 10; page = Image.new('RGBA', (len(VIEWS) * 17 * scale, len(stacks) * 41 * scale), (58, 62, 70, 255))
    for row, stack in enumerate(stacks):
        for col, view in enumerate(VIEWS):
            one = seen(stack, view, images, scale); page.alpha_composite(one, (col * 17 * scale + 5, row * 41 * scale + 5))
    page.save(path)


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check.add_argument('--sheet')
    check = check.parse_args()
    if check.sheet:
        review(check.sheet, textures()); return
    check = check.check
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
    wanted['item.the_oldest_house.borrasca_baton'] = 'County baton'
    for key, name in wanted.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    # 0.4.439: the grinder with something in it, from vanilla's own files (bone breaking, mud, a heavy wet slap), pitched low.
    sounds_path = A / 'sounds.json'; sounds = json.loads(sounds_path.read_text())
    wet = {'subtitle': 'subtitles.the_oldest_house.borrasca.machine_wet', 'sounds':
           [{'name': f'minecraft:block/bone_block/break{i}', 'pitch': .7} for i in range(1, 6)] +
           [{'name': f'minecraft:block/mud/break{i}', 'pitch': .6} for i in range(1, 7)] +
           [{'name': f'minecraft:mob/slime/big{i}', 'pitch': .55} for i in range(1, 5)]}
    wanted['subtitles.the_oldest_house.borrasca.machine_wet'] = 'Grinder chokes, wet'
    if check:
        assert sounds.get('borrasca.machine_wet') == wet, 'borrasca.machine_wet'
        assert lang.get('subtitles.the_oldest_house.borrasca.machine_wet') == 'Grinder chokes, wet'
    else:
        sounds['borrasca.machine_wet'] = wet; sounds_path.write_text(json.dumps(sounds, indent=2) + '\n')
        lang['subtitles.the_oldest_house.borrasca.machine_wet'] = 'Grinder chokes, wet'
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f" Drisking's shops: {len(images)} textures, {len(data)} models and states.")


if __name__ == '__main__':
    main()
