"""The bar's games (0.4.414, docs/BAR_GAMES_PROPOSAL.md): the pool table's cloth, rails, wood and pockets, the balls as the table
and the screen draw them, and the table's item picture; the dartboard's face and sides, and the dart's and board's item pictures
(0.4.415), and the bar's two keepsakes, the house chalk and the tin of darts (0.4.417). Writes the table's model and block states and its name. --check verifies
everything already written. Run from tools/.

0.4.450 (the owner: "Custom booths in last order", "Custom signs for last orders"): the league slate, four blocks by three, whose
weeks are chalked on it by the game; and the booths' own joinery: the panelled partition, its reeded glass screen under a brass
rail, the post at the aisle with the booth's number on a brass plate, the candle in red glass on each table and the picture on
the wall of each booth. Everything is sixteen pixels to the block.

Palette: cloth #1f6e3e, rails and apron mahogany #5a3218 / #3a1e0e, sights #e8d8b0, pockets #0c0806 with a leather lip #3a2414.
The table's model is drawn as its foot facing north (the head one block on, beyond z=0); the head is the same model turned round.
Pixel positions in the cloth and rail pictures are the model's own coordinates, so the pockets sit where the rails meet.
"""
from pathlib import Path
import argparse
import json
import math
from PIL import Image

from draw_polybius_art import shade, new, fill, dot, element
import draw_borrasca_signs as S

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
NS = 'the_oldest_house:'
B = NS + 'block/bar/'
CLOTH, RAIL, RAIL_DARK, SIGHT, POCKET, LIP = '#1f6e3e', '#5a3218', '#3a1e0e', '#e8d8b0', '#0c0806', '#3a2414'


def quarter(im, cx, cy, r, colour, lip=None):
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            d = math.hypot(x + .5 - cx, y + .5 - cy)
            if d < r:
                dot(im, x, y, colour)
            elif lip and d < r + 1:
                dot(im, x, y, lip)


def cloth():
    """The cloth, with the dark of the pockets at its corners: the far corners (z 14.5) and the middle ones (z 0)."""
    im = new(base=CLOTH, seed=401, spread=3)
    for cx, cy in [(1.5, 14.5), (14.5, 14.5), (1.5, 0), (14.5, 0)]:
        quarter(im, cx, cy, 2.2, POCKET)
    return im


def rail():
    """The rails' tops: mahogany, a cushion edge, three sights along each long side, and the pockets cut into the ends."""
    im = new(base=RAIL, seed=402, spread=4)
    for z in (4, 8, 12):
        dot(im, 0, z, SIGHT); dot(im, 15, z, SIGHT)
    for z in range(16):
        dot(im, 1, z, RAIL_DARK); dot(im, 14, z, RAIL_DARK)
    for x in range(16):
        dot(im, x, 14, RAIL_DARK)
    for cx, cy, r in [(1, 15, 2.2), (15, 15, 2.2), (.75, 0, 1.6), (15.25, 0, 1.6)]:
        quarter(im, cx, cy, r, POCKET, LIP)
    return im


def wood():
    im = new(base=RAIL_DARK, seed=403, spread=3)
    for y in (3, 9, 14):
        for x in range(16):
            dot(im, x, y, shade(RAIL_DARK, -10))
    return im


def leg():
    im = new(base=RAIL_DARK, seed=404, spread=2)
    for y in range(16):
        dot(im, 0, y, shade(RAIL_DARK, 12)); dot(im, 15, y, shade(RAIL_DARK, -14))
    fill(im, 0, 0, 16, 2, shade(RAIL, 6))
    return im


def disc(size=16, colour='#ffffff', band=None, edge=True):
    im = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    c = size / 2
    for y in range(size):
        for x in range(size):
            d = math.hypot(x + .5 - c, y + .5 - c)
            if d > c:
                continue
            if band and not (band[0] <= y < band[1]):
                continue
            a = 255 if d < c - 1 or not edge else int(255 * (c - d))
            dot(im, x, y, colour, max(0, min(255, a)))
    return im


def shine():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 5.5, y + .5 - 5)
            if d < 3.2 and math.hypot(x + .5 - 8, y + .5 - 8) < 7.5:
                dot(im, x, y, '#ffffff', int(140 * (1 - d / 3.2)))
            dd = math.hypot(x + .5 - 8, y + .5 - 8)
            if 6.2 < dd < 8 and y > 8:
                dot(im, x, y, '#000000', 50)
    return im


def pocket_gui():
    im = disc(colour=POCKET)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 8, y + .5 - 8)
            if 6.5 < d < 8:
                dot(im, x, y, LIP)
    return im


def item_icon():
    """The table from above, as an item: cloth, rails, pockets."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    fill(im, 1, 4, 15, 12, RAIL)
    fill(im, 2, 5, 14, 11, CLOTH)
    for x, y in [(1, 4), (14, 4), (1, 11), (14, 11), (7, 4), (8, 4), (7, 11), (8, 11)]:
        dot(im, x, y, POCKET)
    for x, y, c in [(10, 7, '#e8c020'), (11, 8, '#c8301e'), (10, 9, '#2850b8'), (5, 8, '#f4f0e4')]:
        dot(im, x, y, c)
    for x in (2, 13):
        for y in (12, 13):
            dot(im, x, y, RAIL_DARK)
    return im


# ------------------------------------------------------------------------------------------------------------ darts (0.4.415)
ORDER = [20, 1, 18, 4, 13, 6, 10, 15, 2, 17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5]
BLACK, CREAM, RED, GREEN, WIRE, RING = '#1a1816', '#e6d9b8', '#b8281c', '#1c6e38', '#b8bcc0', '#141210'
DIGITS = {
    '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'], '2': ['111', '001', '111', '100', '111'],
    '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'], '5': ['111', '100', '111', '001', '111'],
    '6': ['111', '100', '111', '101', '111'], '7': ['111', '001', '010', '010', '010'], '8': ['111', '101', '111', '101', '111'],
    '9': ['111', '101', '111', '001', '111'],
}


def dartboard_face():
    """The board's face at 128 pixels to the block: the double's outer wire 56 pixels out (7 of the block's 16), the number ring
    to the edge. Proportions are a standard board's; 20 at the top, 6 at the right, as seen by someone facing it."""
    size, c, scoring = 128, 64, 56
    im = Image.new('RGBA', (size, size), (0, 0, 0, 255))
    wires = [6.35, 15.9, 99, 107, 162, 170]
    for y in range(size):
        for x in range(size):
            u, v = x + .5 - c, c - (y + .5)
            r = math.hypot(u, v) / scoring * 170
            if r > 170:
                dot(im, x, y, RING if math.hypot(u, v) < 63 else shade(RING, 10))
                continue
            if r < 6.35:
                colour = RED
            elif r < 15.9:
                colour = GREEN
            else:
                a = math.degrees(math.atan2(u, v)) % 360
                i = int((a + 9) // 18) % 20
                ring = 99 <= r < 107 or r >= 162
                colour = (RED if i % 2 == 0 else GREEN) if ring else (BLACK if i % 2 == 0 else CREAM)
                edge = abs(((a + 9) % 18)) < 18 * .02 or abs(((a + 9) % 18) - 18) < 18 * .02
                if edge and r > 15.9:
                    colour = WIRE
            if any(abs(r - w) < .9 for w in wires):
                colour = WIRE
            dot(im, x, y, colour)
    for i, n in enumerate(ORDER):
        a = math.radians(i * 18)
        cx, cy = c + math.sin(a) * 59.5, c - math.cos(a) * 59.5
        text = str(n)
        w = len(text) * 4 - 1
        for k, ch in enumerate(text):
            for row, bits in enumerate(DIGITS[ch]):
                for col, bit in enumerate(bits):
                    if bit == '1':
                        dot(im, int(cx - w / 2 + k * 4 + col), int(cy - 2.5 + row), '#ece6d6')
    return im


def dartboard_side():
    im = new(base=RING, seed=405, spread=2)
    for x in range(16):
        dot(im, x, 0, shade(RING, 14)); dot(im, x, 15, shade(RING, -6))
    return im


def dart_icon():
    """A dart lying corner to corner: point bottom left, flights top right."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for k in range(2):
        dot(im, 2 + k, 13 - k, '#d8d8d8')
    for k in range(2, 6):
        dot(im, 2 + k, 13 - k, '#6e7074'); dot(im, 3 + k, 13 - k, '#55575a')
    for k in range(6, 9):
        dot(im, 2 + k, 13 - k, '#161616')
    for x, y in [(11, 4), (12, 4), (12, 3), (13, 3), (11, 2), (10, 3), (13, 5), (12, 5), (11, 5), (12, 6), (13, 2)]:
        dot(im, x, y, '#c8301e')
    dot(im, 12, 4, '#8a1e14')
    return im


def dartboard_icon():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            u, v = x + .5 - 8, 8 - (y + .5)
            r = math.hypot(u, v)
            if r > 7.6:
                continue
            if r > 6.4:
                colour = RING
            elif r < 1:
                colour = RED
            elif r < 1.9:
                colour = GREEN
            else:
                i = int(((math.degrees(math.atan2(u, v)) % 360) + 9) // 18) % 20
                ring = 3.6 <= r < 4.4 or r >= 5.6
                colour = (RED if i % 2 == 0 else GREEN) if ring else (BLACK if i % 2 == 0 else CREAM)
            dot(im, x, y, colour)
    return im


def chalk_icon():
    """The house chalk: a cube seen from above and to the left, its paper wrap round the sides and a hollow worn in the top."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    CHALK, PAPER = '#3c6cb4', '#e6dcc0'
    for y in range(3, 7):
        for x in range(3 + (7 - y), 11 + (7 - y)):
            dot(im, x, y, shade(CHALK, 18))
    for x, y in [(8, 4), (9, 4), (10, 4), (7, 5), (8, 5), (9, 5), (10, 5), (8, 6), (9, 6)]:
        dot(im, x, y, shade(CHALK, -34))
    dot(im, 10, 6, shade(CHALK, -14)); dot(im, 11, 4, shade(CHALK, 30))
    for y in range(7, 14):
        for x in range(3, 11):
            dot(im, x, y, PAPER if y > 7 else CHALK)
    for x in range(5, 9):
        for y in range(9, 12):
            dot(im, x, y, '#2a3c62' if (x + y) % 3 else shade(CHALK, -10))
    for c in range(11, 15):
        top = 7 - (c - 10)
        for y in range(top, top + 7):
            dot(im, c, y, shade(PAPER, -42) if y > top else shade(CHALK, -22))
    for x in range(3, 11):
        dot(im, x, 13, shade(PAPER, -26))
    return im


def tin_icon():
    """A round tin, open, with three darts standing in it: barrels in the tin, flights up."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    TIN = '#8a9098'
    for y in range(9, 14):
        for x in range(3, 13):
            colour = shade(TIN, 34) if x == 4 else shade(TIN, -30) if x >= 11 else TIN
            dot(im, x, y, shade(colour, -16) if y == 13 else colour)
    for x in range(3, 13):
        dot(im, x, 10, SIGHT if 3 < x < 12 else shade(SIGHT, -30)); dot(im, x, 11, SIGHT if 3 < x < 12 else shade(SIGHT, -30))
    for x in range(5, 11):
        dot(im, x, 11, RED if x in (6, 9) else SIGHT)
    for x in range(4, 12):
        dot(im, x, 6, '#c8ced6'); dot(im, x, 8, '#9aa0a8')
    dot(im, 3, 7, '#c8ced6'); dot(im, 12, 7, '#9aa0a8')
    for x in range(4, 12):
        dot(im, x, 7, '#24262a')
    for x, lean in ((5, -1), (8, 0), (11, 1)):
        bx = x
        for y in range(4, 8):
            dot(im, bx, y, '#6e7074' if y > 5 else '#161616')
        fx = bx + lean
        for y, w in ((1, 1), (2, 1), (3, 0)):
            for dx in range(-w, w + 1):
                dot(im, fx + dx, y, RED if dx else shade(RED, -40))
    return im


def dartboard_model():
    """The board hung on the south side of its block, its face to the north."""
    side = {'texture': '#side'}
    return {'ambientocclusion': False,
            'textures': {'face': B + 'dartboard_face', 'side': B + 'dartboard_side', 'particle': B + 'dartboard_side'},
            'elements': [{'from': [0, 0, 14], 'to': [16, 16, 16], 'faces': {
                'north': {'uv': [0, 0, 16, 16], 'texture': '#face'},
                'south': dict(side, uv=[0, 0, 16, 16], cullface='south'),
                'east': dict(side, uv=[14, 0, 16, 16]), 'west': dict(side, uv=[0, 0, 2, 16]),
                'up': dict(side, uv=[0, 14, 16, 16]), 'down': dict(side, uv=[0, 0, 16, 2])}}]}


# ------------------------------------------------------------------------------------------------ 0.4.450: the slate and the booths
import random

SLATE, CHALK, CHALK_DIM, CHALK_YELLOW = '#27302e', '#e9e6da', '#737b76', '#e3cf7a'
OAK, OAK_DARK = '#6b4726', '#4a3018'
BOOTH, BOOTH_LIGHT, BOOTH_DARK, BOOTH_SHINE = '#3f2a18', '#553a22', '#2c1c0f', '#6a4a2c'
BRASS, BRASS_LIGHT, BRASS_DARK = '#c9a23c', '#ecd27a', '#8a6a1e'
SLATE_COLS, SLATE_ROWS = 4, 3


def slate():
    """The league board: a slate in an oak frame, LEAGUE chalked across the top course and eight ruled squares under it. The
    rules lie on the joins between blocks, so each square is one block and the game chalks a week into it."""
    rnd = random.Random(450)
    im = S.canvas(64, 48)
    S.field(im, (0, 0, 64, 48), SLATE, rnd, 2)
    px = im.load()
    for _ in range(30):                                   # where it has been wiped: short pale smears
        x, y, n = rnd.randint(3, 54), rnd.randint(3, 42), rnd.randint(3, 7)
        for k in range(n):
            px[x + k, y] = S.tone(SLATE, rnd.randint(6, 12))
    S.border(im, (0, 0, 64, 48), OAK_DARK)
    S.border(im, (0, 0, 64, 48), OAK, 1)
    S.line(im, 1, 1, 63, 2, S.tone(OAK, 14))              # light along the frame's top
    S.line(im, 2, 45, 62, 46, S.tone(OAK, 10))            # the tray
    S.line(im, 38, 44, 42, 45, CHALK)                     # a stub of chalk
    S.line(im, 49, 43, 55, 44, '#5b4630'); S.line(im, 49, 44, 55, 45, '#9a9f98')    # the duster
    x = 9
    for ch in 'LEAGUE':
        S.write(im, x, 5, ch, CHALK, True, rnd=rnd, jitter=10)
        x += 8
    for x in (3, 56):
        S.stamp(im, x, 6, S.DIAMOND, {'#': CHALK_YELLOW})
    for x in (16, 32, 48):
        for y in range(17, 44):
            if (y * 3 + x) % 7:
                px[x, y] = S.tone(CHALK_DIM, rnd.randint(-8, 8))
    for x in range(3, 61):
        if (x * 5) % 9:
            px[x, 31] = S.tone(CHALK_DIM, rnd.randint(-8, 8))
            px[x, 16] = S.tone(CHALK_DIM, rnd.randint(-14, 0))
    return im


def booth_panel():
    """The partition's lower course, from the side: rails, stiles and one raised field, a dark kick board at the floor."""
    rnd = random.Random(451)
    im = S.canvas(16, 16)
    S.field(im, (0, 0, 16, 16), BOOTH, rnd, 2)
    S.line(im, 0, 0, 16, 1, BOOTH_LIGHT)
    S.line(im, 0, 14, 16, 16, BOOTH_DARK)
    S.border(im, (2, 2, 14, 13), BOOTH_DARK)
    S.field(im, (3, 3, 13, 12), '#4a321c', rnd, 2)
    S.line(im, 3, 3, 13, 4, BOOTH_SHINE); S.line(im, 3, 3, 4, 12, BOOTH_LIGHT)
    S.line(im, 3, 11, 13, 12, '#33200f'); S.line(im, 12, 4, 13, 12, '#33200f')
    return im


def booth_wood():
    """Rails, caps and posts: the same dark stain, its grain running along."""
    rnd = random.Random(452)
    im = S.canvas(16, 16)
    px = im.load()
    rows = [0, -3, 2, -6, 0, 3, -2, 0, -7, 1, 3, -1, -5, 2, 0, -3]
    for y in range(16):
        for x in range(16):
            px[x, y] = S.tone(BOOTH, rows[y] + rnd.randint(-2, 2))
    return im


def booth_glass():
    """Reeded glass: upright flutes two pixels wide, a pale slant of light across them."""
    rnd = random.Random(453)
    im = S.canvas(16, 16)
    px = im.load()
    for y in range(16):
        for x in range(16):
            c = '#9aada4' if x % 2 == 0 else '#6f857b'
            k = rnd.randint(-3, 3) + (12 if 9 <= x + y <= 12 else 0) + (-10 if y in (0, 15) else 0)
            px[x, y] = S.tone(c, k)
    return im


def booth_brass():
    rnd = random.Random(454)
    im = S.canvas(16, 16)
    S.field(im, (0, 0, 16, 16), BRASS, rnd, 3)
    S.line(im, 0, 0, 16, 1, BRASS_LIGHT); S.line(im, 0, 0, 1, 16, BRASS_LIGHT)
    S.line(im, 0, 15, 16, 16, BRASS_DARK); S.line(im, 15, 0, 16, 16, BRASS_DARK)
    return im


DIGITS_4 = {1: ['.##.', '###.', '.##.', '.##.', '.##.', '####'], 2: ['.##.', '#..#', '..#.', '.#..', '#...', '####'],
            3: ['###.', '...#', '.##.', '...#', '...#', '###.'], 4: ['#..#', '#..#', '####', '...#', '...#', '...#'],
            5: ['####', '#...', '###.', '...#', '...#', '###.']}


def booth_post(number):
    """The post's face to the aisle, ten pixels wide: upright grain, and the booth's number on a brass plate at eye level."""
    rnd = random.Random(455)
    im = S.canvas(16, 16)
    S.field(im, (0, 0, 16, 16), '#463019', rnd, 2)
    S.line(im, 0, 0, 1, 16, BOOTH_DARK); S.line(im, 9, 0, 10, 16, BOOTH_DARK)
    S.line(im, 1, 0, 2, 16, BOOTH_LIGHT)
    S.line(im, 0, 0, 10, 1, BOOTH_LIGHT)
    if number:
        S.line(im, 2, 4, 8, 12, BRASS)
        S.line(im, 2, 4, 8, 5, BRASS_LIGHT); S.line(im, 2, 11, 8, 12, BRASS_DARK)
        S.stamp(im, 3, 5, DIGITS_4[number], {'#': '#1c1408'})
    return im


def booth_candle():
    """A candle in a red glass on the table: four faces of it and its top, lit from inside."""
    im = S.canvas(16, 16)
    side = [['#7a1c16', '#c23a2a', '#c23a2a', '#7a1c16'], ['#c23a2a', '#ffb04a', '#ffd36a', '#c23a2a'],
            ['#b02a22', '#ff8a3a', '#ffb04a', '#b02a22'], ['#7a1c16', '#b02a22', '#b02a22', '#7a1c16']]
    top = [['#3a1410', '#7a1c16', '#7a1c16', '#3a1410'], ['#7a1c16', '#ffd36a', '#fff0b0', '#7a1c16'],
           ['#7a1c16', '#ffb04a', '#ffd36a', '#7a1c16'], ['#3a1410', '#7a1c16', '#7a1c16', '#3a1410']]
    for y in range(4):
        for x in range(4):
            im.putpixel((x, y), S.tone(side[y][x])); im.putpixel((4 + x, y), S.tone(top[y][x]))
    return im


def framed(frame, mount=None):
    """A picture's frame, fourteen by twelve on a clear ground, and the box inside it to paint in."""
    im = S.canvas(16, 16)
    S.line(im, 1, 2, 15, 14, frame)
    S.line(im, 1, 2, 15, 3, S.tone(frame, 22)); S.line(im, 1, 13, 15, 14, S.tone(frame, -22))
    if mount:
        S.line(im, 2, 3, 14, 13, mount)
    return im


def booth_picture(number):
    """What hangs in each booth: a horse, a ship, the team, a hill, and a brewery's mirror."""
    px = None
    if number == 1:
        im = framed('#b8923a'); S.line(im, 2, 3, 14, 9, '#9fb7c4'); S.line(im, 2, 9, 14, 13, '#5f7f45')
        horse = ['.........##.', '........###.', '.#.....##...', '#.#######...', '..#######...', '..#.#..#.#..', '..#.#..#.#..']
        S.stamp(im, 2, 4, horse, {'#': '#6b3d1f'})
    elif number == 2:
        im = framed('#2c1c0f'); S.line(im, 2, 3, 14, 10, '#b9c7cf'); S.line(im, 2, 10, 14, 13, '#3f5f7a')
        ship = ['....|...|...', '...ss|.ss|..', '..sss|sss|..', '..sss|sss|..', '....|...|...', '.hhhhhhhhhh.', '..hhhhhhhh..']
        S.stamp(im, 2, 4, ship, {'|': '#2c1c0f', 's': '#e9e2cc', 'h': '#3a2414'})
        im.putpixel((3, 11), S.tone('#7f9fb7')); im.putpixel((11, 12), S.tone('#7f9fb7')); im.putpixel((7, 12), S.tone('#7f9fb7'))
    elif number == 3:
        im = framed('#1c1612', '#d9c9a0'); S.line(im, 3, 4, 13, 11, '#8f7650')
        team = ['.o.o.o.o..', '.#.#.#.#..', '.#.#.#.#..', '..o.o.o...', '..#.#.#...', '..#.#.#...', '..........']
        S.stamp(im, 3, 4, team, {'o': '#e8d2a8', '#': '#3f3020'})
        for x in (5, 6, 8, 9, 10):
            im.putpixel((x, 12), S.tone('#8a7a58'))
    elif number == 4:
        im = framed('#b8923a'); S.line(im, 2, 3, 14, 13, '#c9d6d9')
        S.line(im, 10, 4, 12, 6, '#e9c46a')
        far = ['....####....', '..########..', '############']
        S.stamp(im, 2, 6, far, {'#': '#7f9a86'})
        S.line(im, 2, 9, 14, 13, '#4f6f45')
        near = ['..........##', '.......#####', '....########']
        S.stamp(im, 2, 8, near, {'#': '#5f8050'})
        S.stamp(im, 3, 6, ['.#.', '###', '###', '.|.'], {'#': '#2f4a2c', '|': '#3a2414'})
    else:
        im = framed('#b8923a'); S.line(im, 2, 3, 14, 13, '#20302b')
        px = im.load()
        for y in range(3, 13):
            for x in range(2, 14):
                if 13 <= x + y <= 14:
                    px[x, y] = S.tone('#3a5148')
        S.write(im, 3, 6, 'ALE', '#d9b24a')
        S.line(im, 4, 4, 12, 5, '#d9b24a'); S.line(im, 4, 12, 12, 13, '#d9b24a')
    return im


def slate_model(piece):
    """One block of the slate, hung on the south side of its block with its face to the north."""
    col, row = piece % SLATE_COLS, piece // SLATE_COLS
    u0, u1, v0, v1 = col * 16 / SLATE_COLS, (col + 1) * 16 / SLATE_COLS, row * 16 / SLATE_ROWS, (row + 1) * 16 / SLATE_ROWS
    r = lambda v: round(v, 4)
    edge = {'texture': '#a', 'uv': [0, 0, r(16 / 64), r(16 / 48)]}       # the frame's dark corner, for the board's thin sides
    faces = {'north': {'texture': '#a', 'uv': [r(u0), r(v0), r(u1), r(v1)]}, 'south': dict(edge, cullface='south'),
             'east': dict(edge), 'west': dict(edge), 'up': dict(edge), 'down': dict(edge)}
    return {'ambientocclusion': False, 'textures': {'a': B + 'slate', 'particle': B + 'slate'},
            'elements': [{'from': [0, 0, 15], 'to': [16, 16, 16], 'faces': faces}]}


def boxed(lo, hi, tex, uv=None, light=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        name = tex.get(face, tex.get('all'))
        if name:
            faces[face] = {'texture': '#' + name}
            if uv and face in uv:
                faces[face]['uv'] = uv[face]
    e = {'from': lo, 'to': hi, 'faces': faces}
    if light:
        e['shade'] = False; e['neoforge_data'] = {'block_light': light, 'sky_light': light}
    return e


BOOTH_TEX = {'w': B + 'booth_wood', 'g': B + 'booth_glass', 'b': B + 'booth_brass', 'particle': B + 'booth_wood'}


def screen_parts(z0):
    """The glass screen from z0 to the block's far side: a rail, reeded glass with a bar down its middle, a cap, and the brass
    rail over it on its standards. Drawn running north to south; the aisle is to the north."""
    glass = {'east': [z0, 4, 16, 14], 'west': [0, 4, 16 - z0, 14]}
    parts = [boxed([5, 0, z0], [11, 2, 16], {'all': 'w'}),
             boxed([7, 2, z0], [9, 12, 16], {'east': 'g', 'west': 'g', 'north': 'g', 'south': 'g'}, glass),
             boxed([5, 12, z0], [11, 14, 16], {'all': 'w'}),
             boxed([7.5, 14, 15], [8.5, 15, 16], {'all': 'b'}),
             boxed([7.5, 15, z0], [8.5, 16, 16], {'all': 'b'})]
    if z0 == 0:
        parts += [boxed([6.5, 2, 7.5], [9.5, 12, 8.5], {'all': 'w'}), boxed([7.5, 14, 0], [8.5, 15, 1], {'all': 'b'})]
    return parts


def screen_model():
    return {'ambientocclusion': False, 'textures': dict(BOOTH_TEX), 'elements': screen_parts(0)}


def post_model(number):
    """The post at the aisle end: ten pixels square, capped, a brass knob on it, the screen running back from it."""
    face = {'north': [0, 0, 10, 16]}
    parts = [boxed([3, 0, 0], [13, 16, 10], {'north': 'p', 'south': 'w', 'east': 'w', 'west': 'w'}, face),
             boxed([2, 16, -1], [14, 17, 11], {'all': 'w'}, {f: ([0, 0, 12, 12] if f in ('up', 'down') else [0, 0, 12, 1])
                                                               for f in ('north', 'south', 'east', 'west', 'up', 'down')}),
             boxed([7, 17, 4], [9, 19, 6], {'all': 'b'}, {f: [0, 0, 2, 2] for f in ('north', 'south', 'east', 'west', 'up', 'down')})] + screen_parts(10)
    return {'ambientocclusion': False, 'textures': dict(BOOTH_TEX, p=B + f'booth_post_{number}'), 'elements': parts}


def candle_model():
    """The red glass stands on the table under this block: the table's top is four pixels below it. It stands toward the
    wall, clear of the napkins and the sauce the table carries at its middle."""
    side = [0, 0, 4, 4]
    jar = boxed([1, -4, 6], [5, 0, 10], {'north': 'c', 'south': 'c', 'east': 'c', 'west': 'c', 'up': 'c'},
                {'north': side, 'south': side, 'east': side, 'west': side, 'up': [4, 0, 8, 4]}, 15)
    return {'ambientocclusion': False, 'textures': {'c': B + 'booth_candle', 'particle': B + 'booth_candle'}, 'elements': [jar]}


def picture_model(number):
    """A framed picture hung on the south side of its block, looking north: the frame's own fourteen by twelve."""
    faces = {'north': {'texture': '#a', 'uv': [1, 2, 15, 14]}, 'east': {'texture': '#a', 'uv': [1, 2, 2, 14]},
             'west': {'texture': '#a', 'uv': [14, 2, 15, 14]}, 'up': {'texture': '#a', 'uv': [1, 2, 15, 3]},
             'down': {'texture': '#a', 'uv': [1, 13, 15, 14]}}
    return {'ambientocclusion': False, 'textures': {'a': B + f'booth_picture_{number}', 'particle': B + f'booth_picture_{number}'},
            'elements': [{'from': [1, 2, 15], 'to': [15, 14, 16], 'faces': faces}]}


BOOTH_KINDS = ('panel', 'screen', 'post', 'candle', 'picture')


def booth_files():
    out = {'models/block/bar/booth_panel.json': {'parent': 'minecraft:block/cube_bottom_top', 'textures': {
        'side': B + 'booth_panel', 'top': B + 'booth_wood', 'bottom': B + 'booth_wood'}},
           'models/block/bar/booth_screen.json': screen_model(), 'models/block/bar/booth_candle.json': candle_model()}
    for n in range(6):
        out[f'models/block/bar/booth_post_{n}.json'] = post_model(n)
    for n in range(1, 6):
        out[f'models/block/bar/booth_picture_{n}.json'] = picture_model(n)
    variants = {}
    for facing, y in FACING_Y.items():
        for kind in BOOTH_KINDS:
            for n in range(6):
                name = {'panel': 'booth_panel', 'screen': 'booth_screen', 'candle': 'booth_candle',
                        'post': f'booth_post_{n}', 'picture': f'booth_picture_{max(1, n)}'}[kind]
                v = {'model': B + name}
                if y and kind not in ('panel', 'candle'):
                    v['y'] = y
                variants[f'facing={facing},kind={kind},number={n}'] = v
    out['blockstates/bar_booth.json'] = {'variants': variants}
    variants = {}
    for facing, y in FACING_Y.items():
        for piece in range(SLATE_COLS * SLATE_ROWS):
            out[f'models/block/bar/slate_{piece}.json'] = slate_model(piece)
            variants[f'facing={facing},piece={piece}'] = dict({'model': B + f'slate_{piece}'}, **({'y': y} if y else {}))
    out['blockstates/bar_slate.json'] = {'variants': variants}
    return out


def booth_textures():
    out = {'block/bar/slate.png': slate(), 'block/bar/booth_panel.png': booth_panel(), 'block/bar/booth_wood.png': booth_wood(),
           'block/bar/booth_glass.png': booth_glass(), 'block/bar/booth_brass.png': booth_brass(), 'block/bar/booth_candle.png': booth_candle()}
    for n in range(6):
        out[f'block/bar/booth_post_{n}.png'] = booth_post(n)
    for n in range(1, 6):
        out[f'block/bar/booth_picture_{n}.png'] = booth_picture(n)
    return out


def textures():
    return {
        'block/bar/pool_cloth.png': cloth(), 'block/bar/pool_rail.png': rail(), 'block/bar/pool_wood.png': wood(),
        'block/bar/pool_leg.png': leg(), 'block/bar/ball.png': Image.new('RGBA', (8, 8), (255, 255, 255, 255)),
        'gui/pool_ball.png': disc(), 'gui/pool_band.png': disc(band=(5, 11)), 'gui/pool_shine.png': shine(),
        'gui/pool_pocket.png': pocket_gui(), 'item/pool_table.png': item_icon(),
        'block/bar/dartboard_face.png': dartboard_face(), 'block/bar/dartboard_side.png': dartboard_side(),
        'item/dart.png': dart_icon(), 'item/dartboard.png': dartboard_icon(),
        'item/house_chalk.png': chalk_icon(), 'item/dart_tin.png': tin_icon(),
        **booth_textures(),
    }


def table_model():
    """The foot of the table, facing north: the far rail at z 16, open at z 0 where the head joins it."""
    els = [
        element([0.5, 7, 0], [15.5, 11, 15.5], {'all': 'wood'}),
        element([1.5, 11, 0], [14.5, 12, 14.5], {'up': 'cloth', 'north': 'cloth', 'east': 'wood', 'west': 'wood', 'south': 'wood'}),
        element([0, 11, 0], [1.5, 13, 16], {'up': 'rail', 'east': 'wood', 'west': 'wood', 'south': 'wood', 'north': 'wood', 'down': 'wood'}),
        element([14.5, 11, 0], [16, 13, 16], {'up': 'rail', 'east': 'wood', 'west': 'wood', 'south': 'wood', 'north': 'wood', 'down': 'wood'}),
        element([1.5, 11, 14.5], [14.5, 13, 16], {'up': 'rail', 'north': 'wood', 'south': 'wood', 'down': 'wood'}),
        element([1, 0, 12.5], [3.5, 7, 15], {'all': 'leg'}),
        element([12.5, 0, 12.5], [15, 7, 15], {'all': 'leg'}),
    ]
    tex = {'cloth': B + 'pool_cloth', 'rail': B + 'pool_rail', 'wood': B + 'pool_wood', 'leg': B + 'pool_leg', 'particle': B + 'pool_wood'}
    return {'ambientocclusion': False, 'textures': tex, 'elements': els}


FACING_Y = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def files():
    out = {'models/block/bar/pool_table.json': table_model(),
           'models/item/pool_table.json': {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/pool_table'}}}
    variants = {}
    for facing, y in FACING_Y.items():
        for part, turn in (('foot', 0), ('head', 180)):
            v = {'model': B + 'pool_table'}
            if (y + turn) % 360:
                v['y'] = (y + turn) % 360
            variants[f'facing={facing},part={part}'] = v
    out['blockstates/pool_table.json'] = {'variants': variants}
    out['models/block/bar/dartboard.json'] = dartboard_model()
    out['blockstates/dartboard.json'] = {'variants': {f'facing={f}': ({'model': B + 'dartboard', 'y': y} if y else {'model': B + 'dartboard'})
                                                      for f, y in FACING_Y.items()}}
    for item in ('dart', 'dartboard', 'house_chalk', 'dart_tin'):
        out[f'models/item/{item}.json'] = {'parent': 'minecraft:item/generated', 'textures': {'layer0': NS + 'item/' + item}}
    out.update(booth_files())
    return out


NAMES = {'block.the_oldest_house.pool_table': 'Pool Table', 'block.the_oldest_house.dartboard': 'Dartboard',
         'item.the_oldest_house.dart': 'Dart', 'entity.the_oldest_house.dart': 'Dart',
         'item.the_oldest_house.house_chalk': 'House Chalk', 'item.the_oldest_house.dart_tin': 'Tin of Darts',
         'block.the_oldest_house.bar_slate': 'League Slate', 'block.the_oldest_house.bar_booth': 'Booth Joinery'}


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    images, data = textures(), files()
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
    for key, name in NAMES.items():
        if check:
            assert lang.get(key) == name, key
        else:
            lang[key] = name
    if not check:
        lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if check else 'Drew') + f' the bar: {len(images)} textures, {len(data)} models and states.')


if __name__ == '__main__':
    main()
