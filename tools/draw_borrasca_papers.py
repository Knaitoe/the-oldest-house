#!/usr/bin/env python3
"""0.4.451: the papers on the front of Drisking General (the owner's notes: "try to add the faces of the actual children, especially
if they're npcs from the vignette", "Cluttered", and "Have this whole front of house filled with lost papers after vignette has
ended"). Draws the pictures of the_oldest_house:borrasca_paper and writes its models and states.

Three kinds of picture, all sixteen pixels to the block:

* a LOST sheet by itself, fourteen pixels wide: the word, and a photograph eight pixels square. Patrick's and Kendra's photographs
  are their own faces, read off the skins the game draws them with, two of a skin's pixels to one;
* a square of the papered front, as it is after the ending: one sheet over the corners and edges of older ones, no wall between;
* the store's noticeboard, two blocks by two, as it stands on each visit and after the ending.

BorrascaPapers.Sheet is the same table in the game; --check compares the two and verifies the files without writing them.
"""
from collections import Counter
from pathlib import Path
import argparse
import json
import random
import re
import sys
from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
import draw_borrasca_signs as S  # noqa: E402

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/BorrascaPapers.java'
SKINS = ASSETS / 'textures/entity'
MOD = 'the_oldest_house'
DIR = 'borrasca_paper'
NAME = 'Lost Notice'

# name, blocks, whether it is also taped to glass
SHEETS = [
    ('patrick', 1, False), ('kendra', 1, False), ('patrick_yellowed', 1, False), ('kendra_yellowed', 1, False),
    ('wall_patrick', 1, True), ('wall_kendra', 1, True), ('wall_kelly', 1, True), ('wall_dana', 1, True), ('wall_kayla', 1, True),
    ('wall_school', 1, True), ('wall_squint', 1, True), ('wall_pencil', 1, True), ('wall_holes', 1, True), ('wall_bleached', 1, True),
    ('wall_cut', 1, True), ('wall_torn', 1, True), ('wall_two', 1, True), ('wall_corners', 1, True),
    ('board', 4, False), ('board_third', 4, False), ('board_last', 4, False), ('board_covered', 4, False),
    # 0.4.462: the childhood board with the dog's notice taken down (labyrinth.BorrascaDogs). Last, so that no other sheet's blocks move.
    ('board_home', 4, False),
]

WHITE, CREAM, YELLOW, BROWN, PALE = '#e9e6dc', '#e3d9b8', '#d6c58f', '#a98a5e', '#dcd6c6'
INK, OLD_INK, RED = '#1c1a18', '#4a3622', '#a11f1a'
STAPLE = (70, 70, 74, 255)
LOST = ['#..###.###.###', '#..#.#.#....#.', '#..#.#.###..#.', '#..#.#...#..#.', '##.###.###..#.']


def blend(c, to, k):
    return tuple(int(round(c[i] + (to[i] - c[i]) * k)) for i in range(3))


def far(a, b):
    return sum((a[i] - b[i]) ** 2 for i in range(3))


def face(skin):
    """A face off a painted skin at eight pixels: its own hair, skin, eyes and mouth, two of its pixels to one."""
    im = Image.open(SKINS / f'{skin}.png').convert('RGBA')
    s = im.size[0] // 64
    src = im.crop((8 * s, 8 * s, 16 * s, 16 * s)).resize((16, 16), Image.NEAREST).load()
    grid = [[src[x, y][:3] for x in range(16)] for y in range(16)]
    hair = grid[0][8]
    skin_tone = Counter(c for row in grid[9:13] for c in row[5:11]).most_common(1)[0][0]
    out = [[None] * 8 for _ in range(8)]
    for y in range(8):
        for x in range(8):
            cell = [grid[y * 2 + j][x * 2 + i] for j in range(2) for i in range(2)]
            # Three of the four, so that a brow one pixel deep is not taken for a fringe.
            out[y][x] = hair if sum(1 for c in cell if far(c, hair) < far(c, skin_tone)) >= 3 else skin_tone
    white, iris = grid[7][3], grid[7][4]
    out[4][1], out[4][2], out[4][5], out[4][6] = white, iris, iris, white
    out[5][3] = out[5][4] = tuple(max(0, v - 8) for v in skin_tone)
    out[6][3] = out[6][4] = grid[13][7]
    return out


def drawn(hair, skin, iris, long=True, fringe=True):
    """A face nobody in the game has: the same eight pixels, from named colours."""
    h, s, i = S.rgb(hair), S.rgb(skin), S.rgb(iris)
    out = [[s] * 8 for _ in range(8)]
    for x in range(8):
        out[0][x] = h
        if fringe:
            out[1][x] = h
    out[1][0] = out[1][7] = h
    for y in range(2, 7 if long else 3):
        out[y][0] = out[y][7] = h
    out[4][1], out[4][2], out[4][5], out[4][6] = (233, 226, 206), i, i, (233, 226, 206)
    out[5][3] = out[5][4] = tuple(max(0, v - 8) for v in s)
    out[6][3] = out[6][4] = tuple(max(0, v - 50) for v in s)
    return out


def sepia(c, paper):
    g = (c[0] * 3 + c[1] * 6 + c[2]) / 10 / 255
    return blend((58, 40, 22), paper, min(1, g * 1.15))


def sheet(likeness, paper=WHITE, ink=INK, fade=0.0, brown=False, seed=1, marker=False, torn=0, cut=False, half=False, holes=False,
          pencil=False):
    """One LOST sheet, fourteen pixels by sixteen: the word, a photograph, and a name too small to letter."""
    rnd = random.Random(seed)
    im = S.canvas(14, 16)
    px = im.load()
    p = S.rgb(paper)
    for y in range(16):
        for x in range(14):
            px[x, y] = S.tone(paper, rnd.randint(-3, 3))
    head = blend(S.rgb(ink), p, fade)
    for dy, row in enumerate(LOST):
        for dx, ch in enumerate(row):
            if ch == '#':
                px[dx, 1 + dy] = head + (255,)
    ground = blend((150, 146, 136), p, fade)
    for y in range(7, 15):
        for x in range(2, 12):
            px[x, y] = ground + (255,)
    for y in range(8):
        for x in range(8):
            c = likeness[y][x]
            if brown:
                c = sepia(c, p)
            px[3 + x, 7 + y] = blend(c, p, fade) + (255,)
    name = blend(S.rgb('#5a5e66') if pencil else S.rgb(ink), p, .35 + fade * .5)
    for x in range(2, 12):
        if x % 4 != 1:
            px[x, 15] = name + (255,)
    px[1, 0] = px[12, 0] = STAPLE
    if holes:                                           # put back up more than once
        for x, y in ((0, 0), (2, 1), (13, 0), (11, 1), (0, 15), (13, 15), (12, 14), (1, 13)):
            px[x, y] = S.tone(paper, -46)
    if marker:                                          # CLOSED, in marker, across the bottom
        for x in range(1, 13):
            px[x, 15] = (16, 16, 18, 255)
            if x % 3 != 1:
                px[x, 14] = (16, 16, 18, 255)
    if cut:                                             # the photograph taken out with a blade
        for y in range(7, 15):
            for x in range(2, 12):
                px[x, y] = (0, 0, 0, 0)
    if half:                                            # the top of a sheet; the rest went with whoever took it
        for x in range(14):
            for y in range(7 + (x * 5 + seed) % 3, 16):
                px[x, y] = (0, 0, 0, 0)
    for i in range(torn):                               # the lower corner gone
        for j in range(torn - i):
            px[13 - j, 15 - i] = (0, 0, 0, 0)
    for _ in range(4):                                  # weather along one edge
        y = rnd.randint(2, 14)
        if px[13, y][3]:
            px[13, y] = S.tone(paper, -18)
    return im


def lay(im, paper, x, y):
    """Lay a sheet on the picture: what is under its right and lower edges falls into its shadow."""
    px, src = im.load(), paper.load()
    w, h = paper.size
    for dy in range(h):
        for dx in range(w):
            if not src[dx, dy][3]:
                continue
            for sx, sy in ((dx + 1, dy), (dx, dy + 1)):
                tx, ty = x + sx, y + sy
                inside = sx < w and sy < h and src[sx, sy][3]
                if not inside and 0 <= tx < im.size[0] and 0 <= ty < im.size[1] and px[tx, ty][3]:
                    r, g, b, a = px[tx, ty]
                    px[tx, ty] = (r * 7 // 10, g * 7 // 10, b * 7 // 10, a)
    for dy in range(h):
        for dx in range(w):
            if src[dx, dy][3] and 0 <= x + dx < im.size[0] and 0 <= y + dy < im.size[1]:
                px[x + dx, y + dy] = src[dx, dy]


def likenesses():
    return {
        'patrick': face('borrasca_eli'), 'kendra': face('borrasca_wren'), 'kelly': face('borrasca_kimber'), 'dana': face('borrasca_dana'),
        'kayla': drawn('#1e1a18', '#cfa37f', '#3b4a3a'),
        'school': drawn('#2c2420', '#d9b48f', '#4a4038'), 'squint': drawn('#3a2a1c', '#c99a76', '#3c342c', long=False, fringe=False),
        'pencil': drawn('#b9a06a', '#e2c3a6', '#56707a'), 'holes': drawn('#6b3a22', '#c08a66', '#3a3028'),
        'bleached': drawn('#8a7a5c', '#e6d3b4', '#6a6a5a', long=False),
    }


def papers(who):
    """Every sheet there is, by name."""
    return {
        'patrick': sheet(who['patrick'], WHITE, seed=11),
        'kendra': sheet(who['kendra'], WHITE, seed=12),
        'patrick_yellowed': sheet(who['patrick'], YELLOW, fade=.35, seed=13, torn=3),
        'kendra_yellowed': sheet(who['kendra'], YELLOW, fade=.35, seed=14),
        'kelly': sheet(who['kelly'], PALE, fade=.3, seed=15, marker=True),
        'dana': sheet(who['dana'], CREAM, fade=.5, seed=16),
        'kayla': sheet(who['kayla'], WHITE, ink=RED, seed=17),
        'school': sheet(who['school'], BROWN, ink=OLD_INK, fade=.2, brown=True, seed=18),
        'squint': sheet(who['squint'], BROWN, ink=OLD_INK, fade=.3, brown=True, seed=19, torn=4),
        'pencil': sheet(who['pencil'], YELLOW, ink='#3a2c1e', fade=.45, brown=True, seed=20, pencil=True),
        'holes': sheet(who['holes'], BROWN, ink=OLD_INK, fade=.4, brown=True, seed=21, holes=True),
        'bleached': sheet(who['bleached'], CREAM, fade=.72, seed=22),
        'cut': sheet(who['school'], CREAM, fade=.15, seed=23, cut=True),
        'torn': sheet(who['squint'], WHITE, seed=24, half=True),
    }


def alone(paper):
    im = S.canvas(16, 16)
    lay(im, paper, 1, 0)
    return im


# The papered front, a square at a time: the sheets underneath (oldest first) and the one on top, each with where it lies.
WALL = {
    'wall_patrick': [('holes', -6, -9), ('bleached', 8, 6), ('patrick_yellowed', 1, 0)],
    'wall_kendra': [('school', 7, -8), ('dana', -9, 5), ('kendra_yellowed', 2, 0)],
    'wall_kelly': [('pencil', -8, -6), ('squint', 6, 7), ('kelly', 0, 0)],
    'wall_dana': [('school', 9, -5), ('holes', -7, 9), ('dana', 2, -1)],
    'wall_kayla': [('kelly', -6, 6), ('bleached', 8, -9), ('kayla', 1, 1)],
    'wall_school': [('bleached', -9, -4), ('pencil', 7, 8), ('school', 2, 0)],
    'wall_squint': [('dana', 8, -7), ('school', -8, 8), ('squint', 0, -1)],
    'wall_pencil': [('holes', -7, -8), ('kelly', 9, 4), ('pencil', 1, 0)],
    'wall_holes': [('squint', 6, -9), ('bleached', -9, 7), ('holes', 2, 1)],
    'wall_bleached': [('pencil', -6, 7), ('school', 9, -6), ('bleached', 0, 0)],
    'wall_cut': [('squint', -8, -9), ('holes', 3, 1), ('cut', 1, 0)],
    'wall_torn': [('dana', -7, 5), ('school', 5, 3), ('torn', 2, 0)],
    'wall_two': [('bleached', 2, -11), ('pencil', -6, 1), ('kayla', 8, 0)],
    'wall_corners': [('school', -8, -9), ('kelly', 9, -7), ('squint', -7, 8), ('patrick_yellowed', 8, 9)],
}


def wall(parts, every, seed):
    """A square of the papered front: paper to every edge."""
    rnd = random.Random(seed)
    im = S.canvas(16, 16)
    S.field(im, (0, 0, 16, 16), '#8a6f4a', rnd, 4)      # what is left of sheets older than any that can be made out
    for name, x, y in parts:
        lay(im, every[name], x, y)
    return im


def back(im):
    """A square of the papered front from behind, through glass: the same sheets, their ink a ghost in the paper."""
    out = S.canvas(*im.size)
    src, px = im.load(), out.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = src[im.size[0] - 1 - x, y]
            if a:
                px[x, y] = blend((r, g, b), (214, 204, 178), .86) + (255,)
    return out


# ---------------------------------------------------------------------------------------------------------------- the noticeboard
STAR = ['..#..', '.###.', '#####', '.###.', '.#.#.']
CROSS = ['.#.', '###', '.#.', '.#.', '.#.']
PICKS = ['#...#', '.#.#.', '..#..', '.#.#.', '#...#']
DOG = ['......#', '#...###', '.#####.', '.#####.', '.#...#.']
PIE = ['..###..', '.#####.', '#######', 'ooooooo']
BIKE = ['..#...##.', '.##...##.', '#..#.#..#', '#..###..#', '.##...##.']
STORM = ['..###..', '.#####.', '#######', '...y...', '..y....', '...y...']
PINS = ['#c0392b', '#2f6fb0', '#d9b23a', '#3c8a4a']


def note(w, h, colour, rnd, band=None, lines=(), ink='#4a4a4e'):
    """A notice: paper, a band across its head if it has one, and lines of writing too small to read."""
    im = S.canvas(w, h)
    S.field(im, (0, 0, w, h), colour, rnd, 2)
    if band:
        S.line(im, 0, 0, w, band[0], band[1])
    px = im.load()
    for y, x0, x1 in lines:
        for x in range(x0, x1):
            if (x + y * 3) % 5 != 4:
                px[x, y] = S.tone(ink)
    return im


def tabs(im, colour):
    """The strip of numbers along the foot of a notice, cut to be torn off; some have been."""
    w, h = im.size
    px = im.load()
    for x in range(w):
        if x % 2:
            px[x, h - 2] = px[x, h - 1] = S.tone(colour, -40)
    for x in (2, 3, 6):
        if x < w:
            px[x, h - 1] = (0, 0, 0, 0)
            px[x, h - 2] = (0, 0, 0, 0)


def pin(im, x, y, k):
    px = im.load()
    px[x, y] = S.tone(PINS[k % 4])
    if y + 1 < im.size[1]:
        r, g, b, a = px[x, y + 1]
        px[x, y + 1] = (r * 3 // 4, g * 3 // 4, b * 3 // 4, a)


def cork(rnd):
    im = S.canvas(32, 32)
    S.field(im, (0, 0, 32, 32), '#a8794c', rnd, 5)
    px = im.load()
    for _ in range(46):
        x, y = rnd.randint(2, 29), rnd.randint(2, 29)
        px[x, y] = S.tone('#8a5f38' if rnd.random() < .6 else '#bd8f5e', rnd.randint(-6, 6))
    return im


def framed(im):
    """The board's frame, over whatever has not been pinned across it."""
    S.border(im, (0, 0, 32, 32), '#2c2017')
    S.border(im, (0, 0, 32, 32), '#4c3726', 1)
    S.line(im, 1, 1, 31, 2, '#5d4530')
    S.line(im, 1, 1, 2, 31, '#5d4530')
    return im


def church(rnd, cancelled=False):
    im = note(9, 8, CREAM, rnd, lines=((5, 1, 8), (7, 2, 7)))
    S.stamp(im, 3, 0, CROSS, {'#': '#5a4a3a'})
    if cancelled:
        for i in range(9):
            for y in (7 - i * 7 // 8, 6 - i * 7 // 8):
                if 0 <= y < 8:
                    im.putpixel((i, y), S.tone(RED))
    return im


def curfew(rnd):
    im = note(9, 12, '#e4e2d8', rnd, band=(2, INK), lines=((9, 1, 8), (11, 2, 7)))
    S.stamp(im, 2, 3, STAR, {'#': '#b8902a'})
    return im


def prescott(rnd, stamped=False, tonight=False):
    im = note(9, 8, '#c9d0d4', rnd, band=(1, '#3d4a55'), lines=((6, 1, 8),))
    S.stamp(im, 0, 1, PICKS, {'#': '#3d4a55'})
    S.line(im, 6, 2, 9, 3, RED)
    S.line(im, 6, 4, 8, 5, '#3d4a55')
    if stamped:
        for x, y in ((5, 5), (6, 5), (7, 5), (5, 6), (7, 6), (5, 7), (6, 7), (7, 7)):
            im.putpixel((x, y), S.tone(RED, 20))
    if tonight:
        S.line(im, 0, 6, 9, 8, '#d9b23a')
        for x in (1, 3, 5, 7):
            im.putpixel((x, 7), S.tone(INK))
    return im


def dog(rnd):
    im = note(9, 10, WHITE, rnd, lines=((1, 1, 8),))
    S.stamp(im, 1, 3, DOG, {'#': '#7a5230'})
    tabs(im, WHITE)
    return im


def bake(rnd):
    im = note(8, 9, '#e9b7c0', rnd, lines=((1, 1, 7), (7, 1, 6)), ink='#8a4a58')
    S.stamp(im, 0, 2, PIE, {'#': '#c98a3a', 'o': '#8a5a2a'})
    return im


def bicycle(rnd):
    im = note(10, 8, '#e6d27a', rnd, lines=((1, 1, 9),), ink='#5a4a1a')
    S.stamp(im, 0, 2, BIKE, {'#': '#3a3020'})
    tabs(im, '#e6d27a')
    return im


def storm(rnd):
    im = note(8, 9, WHITE, rnd, lines=((8, 1, 7),))
    S.stamp(im, 0, 1, STORM, {'#': '#6a7480', 'y': '#d9b23a'})
    return im


def card(rnd, colour):
    return note(5, 3, colour, rnd, lines=((1, 1, 4),))


def board(stage, every, dog_home=False):
    """One picture of the store's noticeboard. `dog_home` is the childhood board once the lost dog's notice is down: everything
    else on it is laid exactly as before (the same sheets from the same numbers), and where the notice hung the cork is paler,
    with the hole its pin left."""
    rnd = random.Random(40 + stage)
    im = framed(cork(rnd))
    px = im.load()
    if stage == 0:      # childhood: the supper, the dog, the curfew, night work, Dana under a bake sale, an older corner
        lay(im, every['school'], 20, 6)
        px[28, 20] = px[29, 20] = px[29, 19] = S.tone(BROWN, 24)       # its curled corner
        lay(im, curfew(rnd), 19, 3); pin(im, 23, 3, 0)
        lay(im, every['dana'], 3, 3)
        lay(im, bake(rnd), 10, 10); pin(im, 13, 10, 1)
        lay(im, church(rnd), 3, 20); pin(im, 7, 20, 2)
        notice = dog(rnd)
        if dog_home:
            src = notice.load()
            for dy in range(notice.size[1]):
                for dx in range(notice.size[0]):
                    if src[dx, dy][3]:
                        r, g, b, a = px[12 + dx, 20 + dy]
                        px[12 + dx, 20 + dy] = (min(255, r + 16), min(255, g + 14), min(255, b + 10), a)
            px[15, 20] = S.tone('#5a3d24')
        else:
            lay(im, notice, 12, 20); pin(im, 15, 20, 3)
        lay(im, prescott(rnd), 20, 22); pin(im, 24, 22, 0)
        lay(im, card(rnd, '#e6d27a'), 2, 29); lay(im, card(rnd, '#d8d2c2'), 13, 29)
    elif stage == 1:    # years later: the supper, a bicycle for sale, Kelly's sheet marked CLOSED, the contract renewed
        lay(im, every['school'], 21, 7)
        lay(im, every['dana'], 17, 2)
        lay(im, note(8, 7, '#d8e0c8', rnd, lines=((1, 1, 7), (3, 1, 6), (5, 1, 7))), 21, 3); pin(im, 24, 3, 3)
        lay(im, note(6, 6, '#e6d27a', rnd, lines=((2, 1, 5), (4, 1, 4))), 18, 11); pin(im, 20, 11, 0)
        lay(im, every['kelly'], 3, 3)
        lay(im, church(rnd), 3, 20); pin(im, 7, 20, 2)
        lay(im, bicycle(rnd), 12, 20); pin(im, 16, 20, 1)
        lay(im, prescott(rnd, stamped=True), 21, 22); pin(im, 25, 22, 0)
        lay(im, card(rnd, '#d8d2c2'), 13, 29)
    elif stage == 2:    # the last night: the supper cancelled, a storm, Kayla's new sheet over Kelly's, the night shift
        for x, y in ((19, 4), (22, 5), (25, 3), (27, 6), (20, 9), (24, 10), (28, 11), (18, 14), (26, 15), (22, 13)):
            px[x, y] = S.tone('#6a4526')                               # the pin holes of sheets nobody kept
        lay(im, every['kelly'], 6, 6)
        lay(im, every['kayla'], 3, 3)
        lay(im, church(rnd, cancelled=True), 20, 3); pin(im, 24, 3, 2)
        lay(im, storm(rnd), 12, 21); pin(im, 15, 21, 1)
        lay(im, prescott(rnd, tonight=True), 21, 22); pin(im, 25, 22, 0)
        lay(im, card(rnd, '#e6d27a'), 2, 26)
    else:               # after: nothing on it that is not a LOST sheet, and no room left
        lay(im, storm(rnd), 12, 21)
        lay(im, prescott(rnd, tonight=True), 21, 22)
        for name, x, y in (('holes', 16, -5), ('school', -4, 14), ('bleached', 21, 15), ('pencil', 9, 17), ('squint', -3, -3),
                           ('dana', 19, 2), ('kelly', 6, 6), ('patrick_yellowed', 12, -2), ('kendra_yellowed', 23, 9),
                           ('kayla', 3, 3), ('cut', 15, 12)):
            lay(im, every[name], x, y)
    return im


# ------------------------------------------------------------------------------------------------------------------------ files
def pictures():
    every = papers(likenesses())
    out = {n: alone(every[n]) for n in ('patrick', 'kendra', 'patrick_yellowed', 'kendra_yellowed')}
    for k, (name, parts) in enumerate(WALL.items()):
        out[name] = wall(parts, every, 60 + k)
        out[name + '_back'] = back(out[name])
    for k, name in enumerate(('board', 'board_third', 'board_last', 'board_covered')):
        out[name] = board(k, every)
    out['board_home'] = board(0, every, dog_home=True)
    return out


def model(name, im, window, left, bottom, depth, behind=None):
    """One block's model, as draw_borrasca_signs.piece lays one out; a model looks north. The sheet is a face with no thickness,
    `depth` pixels from the front of its block, and `behind` is the picture someone on its other side sees."""
    w, h = im.size
    box = S.bounds(im, window)
    if box is None:
        raise SystemExit(f'{name}: a block of it has nothing drawn in it')
    x0, y0, x1, y1 = box

    def uv(a0, b0, a1, b1):
        return [round(a0 * 16 / w, 4), round(b0 * 16 / h, 4), round(a1 * 16 / w, 4), round(b1 * 16 / h, 4)]

    lo = [16 - (left + x1), bottom + h - y1, depth]
    hi = [16 - (left + x0), bottom + h - y0, depth]
    faces = {'north': {'texture': '#a', 'uv': uv(x0, y0, x1, y1)}}
    textures = {'a': f'{MOD}:block/{DIR}/{name}', 'particle': f'{MOD}:block/{DIR}/{name}'}
    if behind:
        faces['south'] = {'texture': '#b', 'uv': uv(w - x1, y0, w - x0, y1)}
        textures['b'] = f'{MOD}:block/{DIR}/{behind}'
    return {'ambientocclusion': False, 'render_type': 'minecraft:cutout', 'textures': textures, 'elements': [S.element(lo, hi, faces)]}


WALL_DEPTH = 15.8       # a hair off the boards
GLASS_DEPTH = 22.9      # a hair off the outside of a pane, which stands in the middle of the block behind


def models(pics):
    out = {}
    for name, blocks, glass in SHEETS:
        im = pics[name]
        if blocks == 4:
            for k in range(4):
                col, row = k % 2, k // 2
                out[f'{name}_{k}'] = model(name, im, (col * 16, row * 16, col * 16 + 16, row * 16 + 16), -16 * col, -16 * (1 - row), WALL_DEPTH)
        else:
            out[f'{name}_0'] = model(name, im, (0, 0, 16, 16), 0, 0, WALL_DEPTH)
            if glass:
                out[f'{name}_glass'] = model(name, im, (0, 0, 16, 16), 0, 0, GLASS_DEPTH, name + '_back')
    return out


def states():
    variants, n = {}, 0
    for name, blocks, glass in SHEETS:
        for k in range(blocks):
            for on_glass in (False, True):
                for facing, y in S.FACING_Y.items():
                    v = {'model': f'{MOD}:block/{DIR}/{name}_glass' if on_glass and glass else f'{MOD}:block/{DIR}/{name}_{k}'}
                    if y:
                        v['y'] = y
                    variants[f'facing={facing},glass={str(on_glass).lower()},piece={n}'] = v
            n += 1
    return {'variants': variants}


def declared():
    """BorrascaPapers.Sheet as the game has it: name, blocks, glass, in order."""
    text = JAVA.read_text()
    body = text[text.index('enum Sheet'):]
    return [(m.group(1).lower(), int(m.group(2)), m.group(3) == 'true') for m in re.finditer(r'\b([A-Z][A-Z0-9_]*)\((\d+),(true|false),"', body)]


def files():
    pics = pictures()
    out = {ASSETS / f'models/block/{DIR}/{name}.json': S.dump_model(m) for name, m in models(pics).items()}
    out[ASSETS / f'blockstates/{DIR}.json'] = S.dump_states(states())
    return pics, out


# The papered front as the game hangs it (BorrascaPapers.wall): thirteen squares by four.
MASS = [n for n, b, g in SHEETS if g]


def hung(y, z):
    if (y, z) == (1, -24):
        return 'wall_patrick'
    if (y, z) == (1, -22):
        return 'wall_kendra'
    k = (z * 5 + y * 3) % len(MASS)
    for dy, dz in ((0, 1), (0, -1), (1, 0), (-1, 0)):
        if (y + dy, z + dz) in ((1, -24), (1, -22)) and MASS[k] == hung(y + dy, z + dz):
            k = (k + 7) % len(MASS)
    return MASS[k]


def page(path, scale=8):
    """Every picture on one page, and the front as it is papered, for looking at."""
    pics = pictures()
    names = [n for n in pics if not n.endswith('_back')]
    pad = 6
    width = max(13 * 16 * scale // 2 + 2 * pad, 1300)
    spots, x, y, row = [], pad, pad, 0
    for n in names:
        w, h = pics[n].size[0] * scale, pics[n].size[1] * scale
        if x + w + pad > width:
            x, y, row = pad, y + row + pad, 0
        spots.append((n, x, y)); x += w + pad; row = max(row, h)
    top = y + row + pad
    half = scale // 2
    sheet_im = Image.new('RGBA', (width, top + 4 * 16 * half + pad), (176, 150, 104, 255))
    for n, x, y in spots:
        sheet_im.alpha_composite(pics[n].resize((pics[n].size[0] * scale, pics[n].size[1] * scale), Image.NEAREST), (x, y))
    for y in range(4):
        for i, z in enumerate(range(-17, -30, -1)):     # as someone on the porch sees it: south on the left
            if (y, z) in ((0, -23), (1, -23)) or (y in (1, 2) and z in (-21, -20)) or (y == 2 and z in (-25, -24)) or (y == 0 and z in (-27, -26)):
                continue
            im = pics[hung(y, z)]
            sheet_im.alpha_composite(im.resize((16 * half, 16 * half), Image.NEAREST), (pad + i * 16 * half, top + (3 - y) * 16 * half))
    b = pics['board_covered'].resize((32 * half, 32 * half), Image.NEAREST)
    sheet_im.alpha_composite(b, (pad + 3 * 16 * half, top + 16 * half))
    sheet_im.save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--check', action='store_true')
    ap.add_argument('--sheet')
    args = ap.parse_args()
    if args.sheet:
        page(args.sheet)
        return
    if JAVA.exists():
        theirs = declared()
        if SHEETS != theirs:
            for a, b in zip(SHEETS, theirs):
                if a != b:
                    raise SystemExit(f'the tool has {a} where BorrascaPapers.Sheet has {b}')
            raise SystemExit(f'the tool has {len(SHEETS)} sheets and BorrascaPapers.Sheet has {len(theirs)}')
    elif args.check:
        raise SystemExit('BorrascaPapers.java is missing')
    pics, texts = files()
    folder = ASSETS / f'textures/block/{DIR}'
    total = sum(b for _, b, _ in SHEETS)
    if args.check:
        for name, im in pics.items():
            path = folder / f'{name}.png'
            if not path.exists() or Image.open(path).convert('RGBA').tobytes() != im.tobytes():
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool draws')
        for path, text in texts.items():
            if not path.exists() or path.read_text() != text:
                raise SystemExit(f'{path.relative_to(ROOT)} is not what the tool writes')
        extra = {p.stem for p in folder.glob('*.png')} - set(pics) | {p.stem for p in (ASSETS / f'models/block/{DIR}').glob('*.json')} - {p.stem for p in texts}
        if extra:
            raise SystemExit(f'files the tool does not make: {sorted(extra)}')
        lang = json.loads((ASSETS / 'lang/en_us.json').read_text())
        if lang.get(f'block.{MOD}.{DIR}') != NAME:
            raise SystemExit('the notice has no name in en_us.json')
        print(f'{len(SHEETS)} sheets, {total} blocks of them, {len(pics)} pictures: as drawn')
        return
    folder.mkdir(parents=True, exist_ok=True)
    for name, im in pics.items():
        im.save(folder / f'{name}.png')
    for path, text in texts.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
    print(f'{len(SHEETS)} sheets, {total} blocks of them, {len(pics)} pictures')


if __name__ == '__main__':
    main()
