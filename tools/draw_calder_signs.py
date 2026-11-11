"""Bike night (0.4.464): Calder Falls' painted signs.

Made as Drisking's are, and by the same hand: this draws its own pictures and gives them to tools/draw_borrasca_signs.py's pieces,
models and states, pointed at Calder's folder (calder_sign) and Calder's list in the game (CalderSigns.Sign), which --check holds
it to. A sign is a picture sixteen pixels to the block, lettered in the picture; a large one is cut into a block-sized square for
each block of it.

One thing is Calder's own. A long board that hangs free is read from both sides, so the back of each of its blocks shows the square
from the board's other end: from behind, the board reads left to right again, not as its own reflection.

This slice, the park's two: the iron board over the gate and the rules on the wall by the door. The town's come with the town.
--check verifies everything. --sheet PATH draws every sign on one page.
"""
from pathlib import Path
import math

import draw_borrasca_signs as signs
from draw_borrasca_signs import border, bolts, canvas, centre, corners, field, line, write

ROOT = Path(__file__).resolve().parents[1]


def arch(rnd):
    """The board over the park gate: cream letters on dark green iron, a rivet at each corner, its ends rounded."""
    im = canvas(96, 16)
    field(im, (0, 2, 96, 14), '#1f2a24', rnd, 2)
    border(im, (0, 2, 96, 14), '#0f1512')
    border(im, (2, 4, 94, 12), '#3a4a40')
    line(im, 3, 5, 93, 11, '#1f2a24')
    centre(im, 5, 'CALDER PARK', '#e6dfc8', True)
    bolts(im, (0, 2, 96, 14), '#8a8d90', 1)
    for x in (0, 95):
        for y in (2, 13):
            im.putpixel((x, y), (0, 0, 0, 0))
    return im, None


def rules(rnd):
    """The park's rules on cream enamel, and what somebody has added under them in marker."""
    im = canvas(64, 32)
    field(im, (0, 0, 64, 32), '#e9e3cf', rnd, 3)
    border(im, (0, 0, 64, 32), '#1f2a24')
    border(im, (1, 1, 63, 31), '#3a4a40')
    centre(im, 3, 'PARK RULES', '#1f2a24', True)
    line(im, 6, 11, 58, 12, '#1f2a24')
    centre(im, 13, 'SHUT AT DUSK', '#1f2a24')
    centre(im, 19, 'NO CYCLING', '#1f2a24')
    corners(im, 2)
    # Under them in red marker, in another hand and not straight: NO FUN.
    write(im, 21, 25, 'NO FUN', '#b3261f', wobble=(0, 1, 0, 0, -1, 0), rnd=rnd, jitter=10)
    return im, None


# ---------------------------------------------------------------------------------------------------------------- 0.4.481: the town
# A shop's front is a picture too: its window band, two blocks high, hung on the wall like a sign. What is lit behind the glass is
# drawn on the second sheet (the glow), which the game shows at full brightness; what is painted on the glass is drawn dark on that
# sheet, over the light.
ALU, GLASS, NIGHT = '#8f9396', '#1b2226', '#101316'


# A paper skeleton, as the dime store sells them: taped inside the glass of a house's window, and of two of the shops' (0.4.481: the
# proposal's "paper skeletons in shop windows").
SKELETON = ['..###..', '..#.#..', '..###..', '...#...', '.#####.', '#..#..#', '...#...', '..#.#..', '.#...#.', '.#...#.']


def pane(im, glow, box, light, rnd, frame=ALU):
    """A pane in its frame. With a light, the room behind shows in the glow; without one the glass is dark."""
    x0, y0, x1, y1 = box
    field(im, box, GLASS, rnd, 2)
    border(im, box, frame)
    if light:
        field(glow, (x0 + 1, y0 + 1, x1 - 1, y1 - 1), light, rnd, 3)


def letters(im, glow, y, word, colour, lit, big=True, scale=2, **kw):
    """A name in tubes or lit letters: painted on the board, and lit on the glow."""
    centre(im, y, word, colour, big, scale, **kw)
    centre(glow, y, word, lit, big, scale, **kw)


def wash_name(rnd):
    im, glow = canvas(128, 16), canvas(128, 16)
    # A lit box: the whole face shines, and the letters are on it.
    field(im, (0, 0, 128, 16), '#e8e6dc', rnd, 2); border(im, (0, 0, 128, 16), '#1f4f8f')
    field(glow, (1, 1, 127, 15), '#f1f5f8', rnd, 2)
    letters(im, glow, 1, 'WASH-O-MAT', '#1f4f8f', '#1f4f8f')
    return im, glow


# 0.4.486: the laundromat's front moves, and the burger place's cabinet. How many frames each has, and how many ticks each is up.
WASH_FRAMES, WASH_TICKS, CABINET_FRAMES, CABINET_TICKS = 8, 4, 4, 5
# What is on the television, a frame at a time: a late film. Each is nine wide and six high: sky, dark, pale, light.
FILM = [
    ('sssssssss', 'ssssspsss', 'sssssssss', 'sssdddsss', 'ssdddddss', 'ddddddddd'),     # a house on a hill, under the moon
    ('sssssssss', 'ssssspsss', 'sssssssss', 'sssdddsss', 'ssdldddss', 'ddddddddd'),     # and a light comes on in it
    ('ddddddddd', 'ddpdddpdd', 'ddddddddd', 'dddpppddd', 'ddddddddd', 'ddddddddd'),     # a face in the dark
    ('ddddddddd', 'dppdddppd', 'ddddddddd', 'ddpppppdd', 'dddpppddd', 'ddddddddd'),     # nearer
    ('lllllllll', 'lldlllldl', 'lllllllll', 'ldllldlll', 'lllllllll', 'lllldllll'),     # the reel jumps
    ('sssssssss', 'sssssssss', 'ssssdssss', 'sssdddsss', 'sssdddsss', 'ddddddddd'),     # somebody at the top of the hill
    ('sssssssss', 'sssdsdsss', 'sssdddsss', 'sssdddsss', 'ssddddsss', 'ddddddddd'),     # with their arms up
    ('ddddddddd', 'ddddddddd', 'dddpppddd', 'ddddddddd', 'ddddddddd', 'ddddddddd'),     # and the dark again
]


def wash_front(rnd):
    """The laundromat, eight frames of it: a row of dryers behind the glass, four of them turning; a woman folding alone at the
    counter; a television on a bracket with a late film on; and the door at the right."""
    im, glow = canvas(112, 32), canvas(112, 32)
    field(im, (0, 0, 112, 32), ALU, rnd, 3)
    pane(im, glow, (1, 1, 90, 31), '#d3dccb', rnd)
    cloth = ('#c94a3a', '#3a6fc9', '#e0d8c0', '#d9b21f', '#5aa06a')
    loads = []
    for k in range(6):                                       # the dryers: a ring and a dark drum
        cx = 9 + k * 14
        line(glow, cx - 5, 5, cx + 5, 16, '#9aa0a4'); line(glow, cx - 4, 6, cx + 4, 15, '#2a3034')
        loads.append([(rnd.random() * 6.283, rnd.choice((1.3, 2.2, 3.0, 3.0)), signs.tone(rnd.choice(cloth), rnd.randint(-10, 10))) for _ in range(9)])
    line(glow, 3, 23, 88, 25, '#8b6a48'); line(glow, 3, 25, 88, 30, '#6d5238')              # the counter
    for x in (20, 40):
        line(glow, x, 19, x + 9, 23, '#e6e0cf')                                              # folded things on it
    write(glow, 4, 2, 'COIN-OP', NIGHT); write(glow, 36, 2, 'WASH .50', NIGHT)
    line(glow, 86, 1, 88, 3, '#55585c'); line(glow, 76, 2, 88, 10, '#3a3a3e'); line(glow, 79, 10, 85, 11, '#2a2a2e')     # the television on its bracket
    pane(im, glow, (93, 1, 111, 31), '#d3dccb', rnd)
    line(glow, 95, 16, 109, 17, '#8f9396'); write(glow, 95, 5, 'OPEN', '#b3261f'); write(glow, 97, 22, '24H', NIGHT)
    turning = (0, 2, 3, 5)
    film = {'s': '#3a4a66', 'd': '#12141a', 'p': '#c8d2dc', 'l': '#9aa6b4'}
    hair, skin, wool, sheet = '#5a3a22', '#d9b090', '#7a5a8a', '#f0ead8'
    all_im, all_glow = canvas(112, 32 * WASH_FRAMES), canvas(112, 32 * WASH_FRAMES)
    for f in range(WASH_FRAMES):
        g = glow.copy(); px = g.load()
        for k in range(6):
            cx, cy = 9 + k * 14 - .5, 10.5
            for i, (a, r, c) in enumerate(loads[k]):
                if k in turning:                             # going round, an eighth of a turn a frame
                    a = a + f * 6.283 / WASH_FRAMES
                    x, y = round(cx + math.cos(a) * r), round(cy + math.sin(a) * r * .95)
                else:                                        # stopped, in a heap at the bottom
                    x, y = round(cx - 3 + i * .75), 13 + (i * 5) % 2
                px[max(9 + k * 14 - 3, min(9 + k * 14 + 2, x)), max(7, min(13, y))] = c
        for y, row in enumerate(FILM[f]):                    # what is on
            for x, ch in enumerate(row):
                px[77 + x, 3 + y] = signs.tone(film[ch])
        # The woman at the counter, her back to the dryers: she holds a sheet up by its corners, brings them together, lays it on
        # the counter, and smooths it down.
        line(g, 62, 11, 65, 12, hair); line(g, 61, 12, 66, 13, hair); line(g, 62, 13, 65, 16, skin); line(g, 61, 13, 62, 15, hair); line(g, 65, 13, 66, 15, hair)
        line(g, 60, 16, 67, 23, wool)
        step = f // 2
        if step == 0:
            line(g, 57, 15, 59, 17, skin); line(g, 68, 15, 70, 17, skin); line(g, 57, 17, 70, 22, sheet)
        elif step == 1:
            line(g, 60, 15, 62, 17, skin); line(g, 65, 15, 67, 17, skin); line(g, 60, 17, 67, 22, sheet)
        elif step == 2:
            line(g, 59, 19, 61, 21, skin); line(g, 66, 19, 68, 21, skin); line(g, 60, 21, 67, 23, sheet)
        else:
            line(g, 60, 20, 62, 22, skin); line(g, 64, 20, 66, 22, skin); line(g, 59, 22, 68, 23, sheet)
        all_im.paste(im, (0, 32 * f)); all_glow.paste(g, (0, 32 * f))
    return all_im, all_glow


def bigtop(dim):
    def paint(rnd):
        """The drive-in's sign: a tent's stripes over BIG TOP in red tubes and BURGERS in yellow. Dim, one letter of it is out."""
        im, glow = canvas(112, 32), canvas(112, 32)
        field(im, (0, 0, 112, 32), '#17171b', rnd, 2); border(im, (0, 0, 112, 32), '#b3261f')
        for x in range(2, 110):
            c = '#b3261f' if (x // 6) % 2 == 0 else '#e9e2cc'
            im.putpixel((x, 1), signs.tone(c)); glow.putpixel((x, 1), signs.tone(c, 30))
        letters(im, glow, 3, 'BIG TOP', '#7a1f1a', '#ff5a4a')
        centre(im, 17, 'BURGERS', '#7a6a1f', True, 2)
        out = centre(glow, 17, 'BURGERS', '#ffd65a', True, 2)
        if dim:
            # The G is out: its tube is there and unlit.
            xs = sorted({x for x, y in out}); x0 = xs[0]
            first = signs.measure('BUR', True, 2) + 2; width = signs.measure('G', True, 2)
            for x, y in out:
                if x0 + first <= x < x0 + first + width:
                    glow.putpixel((x, y), (0, 0, 0, 0))
        return im, glow
    return paint


def bigtop_front(rnd):
    """The drive-in's window: the counter and its hatch, the menu over it, and an arcade cabinet in the corner."""
    im, glow = canvas(112, 32), canvas(112, 32)
    field(im, (0, 0, 112, 32), '#b3261f', rnd, 3)
    for x in range(0, 112, 8):
        line(im, x, 0, x + 4, 32, '#e9e2cc')
    pane(im, glow, (26, 2, 110, 30), '#ffe2a8', rnd, '#e9e2cc')
    line(glow, 30, 4, 106, 12, '#22201c')                                                    # the menu board
    for row in range(3):
        for x in range(33, 104, 12):
            line(glow, x, 5 + row * 2, x + 8, 6 + row * 2, '#e9e2cc')
    line(glow, 28, 22, 108, 24, '#c9c2b0'); write(glow, 50, 15, 'ORDER HERE', '#7a1f1a')    # the counter
    pane(im, glow, (2, 2, 24, 30), None, rnd, '#e9e2cc')
    line(glow, 6, 6, 20, 29, '#15151a'); line(glow, 8, 9, 18, 17, '#ff5ad0'); line(glow, 8, 4, 18, 6, '#5ad0ff')   # the cabinet, its screen and its marquee
    for _ in range(10):
        glow.putpixel((8 + rnd.randrange(10), 9 + rnd.randrange(8)), signs.tone(rnd.choice(('#5ad0ff', '#ffe25a', '#1a1a24'))))
    line(glow, 9, 19, 17, 21, '#3a3a44'); glow.putpixel((11, 19), signs.tone('#ff5a4a')); glow.putpixel((15, 19), signs.tone('#ffe25a'))
    # 0.4.486: its screen is going. A ship at the bottom that moves from side to side, a shot going up from it, and three rows of
    # things coming down a step at a time.
    all_im, all_glow = canvas(112, 32 * CABINET_FRAMES), canvas(112, 32 * CABINET_FRAMES)
    for f in range(CABINET_FRAMES):
        g = glow.copy()
        line(g, 8, 9, 18, 17, '#141420')
        for row in range(3):
            for k in range(4):
                x, y = 9 + k * 2 + (f + row) % 2, 9 + row + f // 2
                g.putpixel((x, y), signs.tone(('#5ad0ff', '#ff5ad0', '#ffe25a')[row]))
        ship = 10 + (0, 2, 4, 2)[f]
        line(g, ship, 16, ship + 3, 17, '#e9e2cc'); g.putpixel((ship + 1, 15 - f), signs.tone('#ffffff'))
        all_im.paste(im, (0, 32 * f)); all_glow.paste(g, (0, 32 * f))
    return all_im, all_glow


def foodfair_name(rnd):
    im, glow = canvas(112, 32), canvas(112, 32)
    field(im, (0, 0, 112, 32), '#ece8dc', rnd, 2); border(im, (0, 0, 112, 32), '#2f5a3a'); line(im, 2, 15, 110, 16, '#2f5a3a')
    field(glow, (1, 1, 111, 31), '#f4f1e6', rnd, 2); line(glow, 2, 15, 110, 16, '#2f5a3a')
    letters(im, glow, 1, 'CALDER', '#2f5a3a', '#2f6a42')
    letters(im, glow, 17, 'FOOD FAIR', '#b3261f', '#c8322a')
    return im, glow


def food_window(words, ink, lit):
    def paint(rnd):
        """One of the grocery's windows: a special lettered by hand on butcher paper, taped up inside. Lit, the aisles show behind it."""
        im, glow = canvas(48, 32), canvas(48, 32)
        field(im, (0, 0, 48, 32), ALU, rnd, 3)
        pane(im, glow, (1, 1, 47, 31), '#e6eede' if lit else None, rnd)
        sheet = glow if lit else im
        if lit:
            for row in range(3):                                                             # shelves, far back
                line(glow, 3, 4 + row * 3, 45, 5 + row * 3, '#b9c2b0')
                for x in range(4, 44, 2):
                    glow.putpixel((x, 3 + row * 3), signs.tone(rnd.choice(('#c94a3a', '#d9b21f', '#3a6fc9', '#5aa06a', '#e0d8c0'))))
        field(sheet, (5, 13, 43, 29), '#e9dfc4' if lit else '#6f6a5c', rnd, 3)
        for x in (5, 42):
            sheet.putpixel((x, 13), signs.tone('#c9bfa0' if lit else '#55524a'))
        for i, word in enumerate(words):
            centre(sheet, 15 + i * 7, word, ink if lit else '#3a3833', rnd=rnd, wobble=(0, 0, 1, 0, -1, 0, 0, 1), jitter=8)
        return im, glow
    return paint


def marquee(top, bottom):
    def paint(rnd):
        """The theater's marquee: a lit white board, the night's two pictures in changeable letters, and bulbs round it."""
        im, glow = canvas(112, 32), canvas(112, 32)
        field(im, (0, 0, 112, 32), '#7a1f1a', rnd, 3)
        field(im, (3, 4, 109, 28), '#e9e4d2', rnd, 2); field(glow, (3, 4, 109, 28), '#f6f1de', rnd, 2)
        for x in range(2, 110, 4):
            for y in (1, 29):
                im.putpixel((x, y), signs.tone('#d9b21f')); glow.putpixel((x, y), signs.tone('#ffe9a0'))
        line(glow, 3, 15, 109, 16, '#b9b4a2')
        centre(glow, 5, top, '#18181b', False, 2); centre(glow, 17, bottom, '#18181b', False, 2)
        return im, glow
    return paint


def theater_front(rnd):
    """The theater's front: the ticket window, the doors, and a poster case each side of them."""
    im, glow = canvas(112, 32), canvas(112, 32)
    field(im, (0, 0, 112, 32), '#3a1f22', rnd, 3)
    pane(im, glow, (2, 6, 32, 26), '#ffe2a8', rnd, '#b8943a'); write(glow, 4, 8, 'TICKETS', '#3a1f22')
    line(glow, 9, 20, 25, 22, '#3a1f22')
    for x0, moon in ((35, True), (91, False)):                                               # the poster cases
        pane(im, glow, (x0, 3, x0 + 20, 29), '#1b2030', rnd, '#b8943a')
        if moon:
            line(glow, x0 + 12, 6, x0 + 16, 10, '#e9e4c8')                                     # a moon, a hill, a house on it
            for i in range(14):
                line(glow, x0 + 3 + i, 22 - min(i, 13 - i) // 2, x0 + 4 + i, 28, '#0d0f16')
            line(glow, x0 + 8, 14, x0 + 13, 20, '#0d0f16'); glow.putpixel((x0 + 10, 16), signs.tone('#ffd65a'))
        else:
            line(glow, x0 + 4, 20, x0 + 16, 28, '#3a3f4a'); line(glow, x0 + 6, 21, x0 + 14, 22, '#0d0f16')   # a well, and a hand out of it
            line(glow, x0 + 9, 12, x0 + 11, 21, '#c9cfb0')
            for dx in (-2, 0, 2):
                line(glow, x0 + 9 + dx, 9, x0 + 10 + dx, 13, '#c9cfb0')
    for x0 in (57, 73):                                                                      # the doors
        field(im, (x0, 4, x0 + 16, 31), '#5d3a26', rnd, 3, planks=4, upright=True); border(im, (x0, 4, x0 + 16, 31), '#2a1810')
        line(im, x0 + 5, 8, x0 + 11, 14, GLASS); line(glow, x0 + 6, 9, x0 + 10, 13, '#ffe2a8')
        line(im, x0 + (12 if x0 == 57 else 2), 16, x0 + (14 if x0 == 57 else 4), 22, '#b8943a')
    return im, glow


def jrhigh_name(rnd):
    """Cast letters fixed to the brick: nothing behind them."""
    im = canvas(128, 16)
    centre(im, 3, 'CALDER JR. HIGH', '#b9bcbf', False, 2, shadow='#3a3d40')
    return im, None


def school_front(rnd):
    """The school's doors, shut, and a dark window each side; a paper banner over one."""
    im = canvas(96, 32)
    field(im, (0, 0, 96, 32), '#6f6a5c', rnd, 3)
    for x0, w in ((2, 26), (74, 20)):
        field(im, (x0, 4, x0 + w, 24), NIGHT, rnd, 2); border(im, (x0, 4, x0 + w, 24), '#b9bcbf')
        for y in range(6, 23, 2):
            line(im, x0 + 2, y, x0 + w - 2, y + 1, '#2a2d30')                                 # blinds, down
    for x0 in (40, 56):
        field(im, (x0, 2, x0 + 16, 32), '#28382d', rnd, 3); border(im, (x0, 2, x0 + 16, 32), '#141c17')
        field(im, (x0 + 3, 5, x0 + 13, 16), NIGHT, rnd, 2)
        for i in range(4, 12, 3):
            line(im, x0 + i, 5, x0 + i + 1, 16, '#3a403c')                                    # wired glass
        line(im, x0 + (11 if x0 == 40 else 3), 19, x0 + (13 if x0 == 40 else 5), 25, '#b9bcbf')
    field(im, (1, 25, 39, 31), '#e9dfc4', rnd, 3); write(im, 3, 26, 'GO OTTERS', '#2b4f8f')
    return im, None


def trophy(rnd):
    """The trophy case by the school's doors, the one thing lit: three shelves of cups, and a team's photograph."""
    im, glow = canvas(32, 32), canvas(32, 32)
    field(im, (0, 0, 32, 32), '#5d4024', rnd, 3, planks=5)
    pane(im, glow, (2, 2, 30, 30), '#e9dfb8', rnd, '#3a2a18')
    for y in (11, 20):
        line(glow, 3, y, 29, y + 1, '#8b6a48')
    for x, y in ((5, 6), (13, 5), (22, 6), (6, 15), (24, 15), (9, 24), (19, 24)):
        signs.stamp(glow, x, y, ['#.#', '###', '.#.', '.#.', '###'], {'#': '#d9b21f'})
    line(glow, 13, 13, 21, 19, '#3a3833'); line(glow, 14, 14, 20, 18, '#b9b4a2')
    return im, glow


def bank_name(rnd):
    im = canvas(112, 16)
    field(im, (0, 1, 112, 15), '#bdb6a4', rnd, 3); border(im, (0, 1, 112, 15), '#8c8674')
    centre(im, 3, 'CALDER SAVINGS', '#5b5546', False, 2)
    return im, None


def bank_front(rnd):
    """The bank, shut: two tall windows with the blinds down, the night deposit, and the door."""
    im, glow = canvas(80, 32), canvas(80, 32)
    field(im, (0, 0, 80, 32), '#bdb6a4', rnd, 3)
    for x0 in (2, 22):
        field(im, (x0, 3, x0 + 18, 29), NIGHT, rnd, 2); border(im, (x0, 3, x0 + 18, 29), '#5b5546')
        for y in range(5, 28, 2):
            line(im, x0 + 2, y, x0 + 16, y + 1, '#3d3a33')
    field(im, (46, 14, 56, 22), '#b8943a', rnd, 4); border(im, (46, 14, 56, 22), '#5b4a1c'); line(im, 48, 17, 54, 18, '#3a2f12')
    write(im, 42, 7, 'NIGHT', '#5b5546')
    pane(im, glow, (61, 3, 78, 31), None, rnd, '#b8943a'); line(glow, 64, 6, 75, 12, '#ffd9a0')
    write(im, 62, 15, 'EST.', '#b8943a'); write(im, 62, 22, '1921', '#b8943a')
    return im, glow


def video_name(rnd):
    im, glow = canvas(112, 16), canvas(112, 16)
    field(im, (0, 0, 112, 16), '#141418', rnd, 2); border(im, (0, 0, 112, 16), '#5ad0ff')
    letters(im, glow, 1, 'VIDEO HUT', '#7a2f6a', '#ff5ad0')
    line(glow, 4, 15, 108, 16, '#5ad0ff')
    return im, glow


def video_front(rnd):
    """The video shop: three shelves of tapes face out, a card in the corner of the glass, and the return slot in the door, under a paper skeleton."""
    im, glow = canvas(80, 32), canvas(80, 32)
    field(im, (0, 0, 80, 32), '#141418', rnd, 2)
    pane(im, glow, (1, 1, 58, 31), '#cfd0e0', rnd, '#5ad0ff')
    for row in range(3):
        y = 4 + row * 9
        line(glow, 3, y + 6, 56, y + 7, '#3a3a44')
        for x in range(4, 55, 3):
            c = rnd.choice(('#c94a3a', '#3a6fc9', '#d9b21f', '#5aa06a', '#8a4ac9', '#e0d8c0', '#1a1a24'))
            line(glow, x, y, x + 2, y + 6, c)
    field(glow, (36, 21, 56, 29), '#ffe25a', rnd, 3); write(glow, 38, 23, 'RENT', '#7a1f1a')
    pane(im, glow, (61, 1, 79, 31), '#cfd0e0', rnd, '#5ad0ff')
    write(glow, 63, 4, 'OPEN', '#ff5a4a'); line(glow, 64, 20, 76, 22, '#141418'); write(glow, 63, 24, 'DROP', '#141418')
    signs.stamp(glow, 67, 10, SKELETON, {'#': '#f4f0e0'})                                    # a paper skeleton taped in the door
    return im, glow


def barber_name(rnd):
    im = canvas(80, 16)
    field(im, (0, 0, 80, 16), '#ece8dc', rnd, 2)
    for x in range(0, 80):
        im.putpixel((x, 0), signs.tone('#b3261f' if (x // 4) % 2 == 0 else '#2b4f8f')); im.putpixel((x, 15), signs.tone('#2b4f8f' if (x // 4) % 2 == 0 else '#b3261f'))
    centre(im, 1, 'BARBER', '#2a2522', True, 2)
    return im, None


def barber_front(rnd):
    """The barber's, shut for the night: two chairs and the long mirror, by the light left on at the back; a paper skeleton in the glass."""
    im, glow = canvas(64, 32), canvas(64, 32)
    field(im, (0, 0, 64, 32), '#ece8dc', rnd, 2)
    pane(im, glow, (1, 2, 44, 31), '#6f7468', rnd, '#2a2522')
    line(glow, 4, 5, 41, 12, '#9aa39a')                                                      # the mirror
    for x0 in (8, 26):
        line(glow, x0, 14, x0 + 9, 22, '#7a1f1a'); line(glow, x0 + 1, 22, x0 + 8, 24, '#c2c6ca'); line(glow, x0 + 4, 24, x0 + 5, 30, '#c2c6ca')
        line(glow, x0 - 1, 17, x0, 21, '#c2c6ca'); line(glow, x0 + 9, 17, x0 + 10, 21, '#c2c6ca')
    pane(im, glow, (47, 2, 63, 31), '#6f7468', rnd, '#2a2522')
    field(glow, (48, 9, 62, 17), '#ece8dc', rnd, 2); write(glow, 48, 11, 'SHUT', '#b3261f')
    signs.stamp(glow, 37, 16, SKELETON, {'#': '#d8d4c4'})                                    # and one in the corner of the window
    return im, glow


def hardware_name(rnd):
    im = canvas(112, 16)
    field(im, (0, 0, 112, 16), '#2f5a3a', rnd, 3); border(im, (0, 0, 112, 16), '#e3d9b8')
    centre(im, 3, 'HALE HARDWARE', '#e3d9b8', False, 2)
    return im, None


def hardware_front(rnd):
    """The hardware store, shut: rakes fanned out in the window, shovels, a wheelbarrow, and pumpkins by the door with a card."""
    im, glow = canvas(96, 32), canvas(96, 32)
    field(im, (0, 0, 96, 32), '#2f5a3a', rnd, 3)
    pane(im, glow, (1, 2, 72, 31), '#57584e', rnd, '#e3d9b8')
    for i, dx in enumerate((-9, -5, 0, 5, 9)):                                               # the rakes: handles down to one point, tines up
        for t in range(18):
            x = 20 + dx * t // 18; y = 28 - t
            glow.putpixel((x, y), signs.tone('#b99a6a'))
        line(glow, 20 + dx - 2, 8, 20 + dx + 3, 10, '#c2c6ca')
    for x0 in (40, 46):
        line(glow, x0, 6, x0 + 1, 24, '#b99a6a'); line(glow, x0 - 1, 24, x0 + 2, 29, '#8f9396')   # shovels
    line(glow, 52, 20, 68, 26, '#b3261f'); line(glow, 56, 26, 60, 29, '#18191b'); line(glow, 66, 18, 70, 20, '#b99a6a')   # the wheelbarrow
    pane(im, glow, (75, 2, 95, 31), '#57584e', rnd, '#e3d9b8')
    for x0, y0 in ((78, 24), (84, 25), (81, 19)):
        line(glow, x0, y0, x0 + 6, y0 + 5, '#d9731a')
    field(glow, (78, 8, 92, 15), '#e9dfc4', rnd, 2); write(glow, 79, 9, '.05', '#b3261f')
    return im, glow


def house_window(kind):
    def paint(rnd):
        """A house's front window on Halloween: lit, with a paper skeleton taped in it (0) or a lantern on the sill and the cat (1); or
        dark, with one candle (2)."""
        im, glow = canvas(32, 32), canvas(32, 32)
        field(im, (0, 0, 32, 32), '#e9e6dc', rnd, 2)
        pane(im, glow, (2, 2, 30, 28), '#ffcf8a' if kind < 2 else None, rnd, '#c9c6bc')
        sheet = glow
        if kind < 2:
            for y in range(3, 27):                                                           # curtains, drawn back
                w = 5 - (y - 3) // 8
                line(glow, 3, y, 3 + w, y + 1, '#e9dcc0'); line(glow, 29 - w, y, 29, y + 1, '#e9dcc0')
            line(glow, 15, 3, 17, 27, '#c9c6bc'); line(glow, 3, 14, 29, 15, '#c9c6bc')        # the sashes
        if kind == 0:
            signs.stamp(sheet, 11, 6, SKELETON, {'#': '#f4f0e0'})
        elif kind == 1:
            line(glow, 6, 21, 13, 27, '#d9731a'); glow.putpixel((8, 23), signs.tone('#ffe25a')); glow.putpixel((11, 23), signs.tone('#ffe25a')); line(glow, 8, 25, 12, 26, '#ffe25a')
            signs.stamp(sheet, 19, 17, ['#...#', '#####', '#.#.#', '#####', '.###.', '.###.', '.###.', '.####', '.####', '.###.'], {'#': '#15151a'})
        else:
            line(im, 15, 20, 17, 27, '#e9e6dc'); glow.putpixel((15, 18), signs.tone('#ffe25a')); glow.putpixel((16, 18), signs.tone('#ffe25a')); glow.putpixel((15, 19), signs.tone('#ffb23a')); glow.putpixel((16, 19), signs.tone('#ffb23a'))
        line(im, 1, 28, 31, 30, '#c9c6bc')                                                   # the sill
        return im, glow
    return paint


def street(name):
    def paint(rnd):
        """A street's name on a blade."""
        im = canvas(48, 16)
        field(im, (1, 4, 47, 12), '#1f5a36', rnd, 2); border(im, (1, 4, 47, 12), '#e9e6dc')
        centre(im, 6, name, '#e9e6dc')
        return im, None
    return paint


def cemetery(rnd):
    im = canvas(96, 16)
    field(im, (0, 2, 96, 14), '#16181a', rnd, 2); border(im, (0, 2, 96, 14), '#0c0d0e'); border(im, (2, 4, 94, 12), '#33373a')
    line(im, 3, 5, 93, 11, '#16181a')
    centre(im, 6, 'CALDER HILL CEMETERY', '#d6d2c2')
    for x in (0, 95):
        for y in (2, 13):
            im.putpixel((x, y), (0, 0, 0, 0))
    return im, None


def stop(rnd):
    im = canvas(16, 16)
    field(im, (0, 0, 16, 16), '#b3261f', rnd, 3)
    corners(im, 4)
    write(im, 1, 5, 'STOP', '#f1eee4')
    return im, None


def house_door(rnd):
    """A house's front door: four panels, a brass knob, and a paper bat taped to it."""
    im = canvas(16, 32)
    field(im, (1, 1, 15, 32), '#6a3b2a', rnd, 3); border(im, (1, 1, 15, 32), '#3d2218')
    for x0, y0, x1, y1 in ((3, 4, 7, 14), (9, 4, 13, 14), (3, 17, 7, 29), (9, 17, 13, 29)):
        border(im, (x0, y0, x1, y1), '#4f2c1f')
    im.putpixel((12, 16), signs.tone('#d9b24a')); im.putpixel((12, 15), signs.tone('#b8943a'))
    signs.stamp(im, 4, 7, ['#.....#', '##.#.##', '#######', '.#####.', '..#.#..'], {'#': '#15151a'})
    return im, None


def food_door(rnd):
    """The grocery's doors: IN and OUT, on the glass, and the lights on behind."""
    im, glow = canvas(32, 32), canvas(32, 32)
    field(im, (0, 0, 32, 32), ALU, rnd, 3)
    pane(im, glow, (1, 1, 16, 32), '#e6eede', rnd); pane(im, glow, (16, 1, 31, 32), '#e6eede', rnd)
    write(glow, 5, 6, 'IN', '#2f5a3a'); write(glow, 18, 6, 'OUT', '#b3261f')
    line(glow, 3, 17, 14, 18, ALU); line(glow, 18, 17, 29, 18, ALU)
    return im, glow


def drive_thru(rnd):
    im, glow = canvas(48, 16), canvas(48, 16)
    field(im, (0, 2, 48, 14), '#e9e2cc', rnd, 2); border(im, (0, 2, 48, 14), '#b3261f')
    field(glow, (1, 3, 47, 13), '#fff0c0', rnd, 2)
    write(glow, 3, 6, 'DRIVE THRU', '#7a1f1a')
    return im, glow


def deliveries(rnd):
    im = canvas(48, 16)
    field(im, (0, 3, 48, 13), '#9a9890', rnd, 3); border(im, (0, 3, 48, 13), '#55534c')
    centre(im, 6, 'DELIVERIES', '#2a2925')
    return im, None


def back_door(rnd):
    """A back door: grey steel, a square of wired glass, a handle, a kick plate at its foot."""
    im = canvas(16, 32)
    field(im, (1, 1, 15, 32), '#6c7175', rnd, 3); border(im, (1, 1, 15, 32), '#3f4346')
    field(im, (5, 5, 11, 11), '#1b2024', rnd, 2); border(im, (5, 5, 11, 11), '#4c5054')
    line(im, 11, 16, 13, 19, '#c9ccd0')
    field(im, (2, 26, 14, 31), '#8f9396', rnd, 3)
    return im, None


def shutter(rnd):
    """A roll-down shutter: its box above, slats to the ground, a padlock at the foot."""
    im = canvas(32, 32)
    field(im, (0, 0, 32, 32), '#7f8488', rnd, 3)
    for y in range(5, 32, 3):
        line(im, 1, y, 31, y + 1, '#55595c')
    field(im, (0, 0, 32, 4), '#5d6165', rnd, 2); border(im, (0, 0, 32, 32), '#3f4346')
    line(im, 15, 28, 18, 31, '#c9a53a'); line(im, 16, 27, 17, 28, '#8f9396')
    return im, None


def no_parking(rnd):
    im = canvas(32, 16)
    field(im, (0, 0, 32, 16), '#e9e6dc', rnd, 2); border(im, (0, 0, 32, 16), '#b3261f'); bolts(im, (0, 0, 32, 16), '#6f7478')
    centre(im, 2, 'NO', '#b3261f'); centre(im, 9, 'PARKING', '#b3261f')
    return im, None


def tag_rules(rnd):
    """Spray paint on a wall, in a hand, and a line under it. Nothing but the paint."""
    im = canvas(48, 32)
    centre(im, 5, 'TULLY', '#c22a22', True, wobble=(0, 1, 0, -1, 1), rnd=rnd, flake=.06)
    centre(im, 18, 'RULES', '#c22a22', True, wobble=(1, 0, -1, 0, 1), rnd=rnd, flake=.06)
    for x in range(5, 44):
        if rnd.random() < .9:
            im.putpixel((x, 28 + (x // 9) % 2), signs.tone('#c22a22', rnd.randint(-10, 10)))
    return im, None


def tag_here(rnd):
    im = canvas(48, 16)
    centre(im, 5, 'RAY WUZ HERE', '#3a66b0', wobble=(0, 1, 0, 0, -1, 0, 1), rnd=rnd, flake=.05)
    return im, None


def tag_skull(rnd):
    """A skull sprayed through a stencil."""
    im = canvas(16, 16)
    signs.stamp(im, 3, 3, ['..#####..', '.#######.', '#########', '#..###..#', '#..###..#', '####.####', '.#######.', '..#.#.#..', '..#####..'], {'#': '#e2ded0'})
    return im, None


def for_sale(rnd):
    im = canvas(32, 16)
    field(im, (1, 0, 31, 16), '#b3261f', rnd, 2); border(im, (1, 0, 31, 16), '#e9e6dc')
    centre(im, 2, 'FOR', '#f1eee4'); centre(im, 9, 'SALE', '#f1eee4')
    return im, None


def beware(rnd):
    im = canvas(32, 16)
    field(im, (0, 0, 32, 16), '#d9b21f', rnd, 3); border(im, (0, 0, 32, 16), '#18191b')
    centre(im, 2, 'BEWARE', '#18191b'); centre(im, 9, 'OF DOG', '#18191b')
    return im, None


def dead_end(rnd):
    im = canvas(32, 16)
    field(im, (0, 0, 32, 16), '#d9b21f', rnd, 3); border(im, (0, 0, 32, 16), '#18191b')
    centre(im, 2, 'DEAD', '#18191b'); centre(im, 9, 'END', '#18191b')
    return im, None


def lost_cat(rnd):
    """A paper stapled up: LOST, a cat's shape, and a line too small to read."""
    im = canvas(16, 16)
    field(im, (0, 0, 16, 16), '#ebe8dc', rnd, 3)
    write(im, 1, 1, 'LOST', '#b3261f')
    signs.stamp(im, 4, 7, ['#.....#', '##...##', '#######', '#.###.#', '#######', '.#####.'], {'#': '#2a2622'})
    line(im, 2, 14, 14, 15, '#9a968a')
    return im, None


def way(name):
    def paint(rnd):
        """A blade for a footpath, brown where a street's is green: it lies along the path it names."""
        im = canvas(48, 16)
        field(im, (1, 4, 47, 12), '#5a3a22', rnd, 2); border(im, (1, 4, 47, 12), '#e9e6dc')
        centre(im, 6, name, '#e9e6dc')
        return im, None
    return paint


def poster(kind):
    def paint(rnd):
        """A one-sheet in a lit case on the theater's side wall: 0 CELLAR DOOR 2, 1 SWAMP PROM, 2 DR. GRAVELY. Each is one of the
        pictures the marquee's three nights name."""
        im, glow = canvas(32, 32), canvas(32, 32)
        field(im, (0, 0, 32, 32), '#2a1a1c', rnd, 2); border(im, (0, 0, 32, 32), '#b8943a')
        if kind == 0:
            field(glow, (1, 1, 31, 31), '#15100e', rnd, 2)
            # The cellar's two doors in the grass, one leaf lifted, and the light under it.
            line(glow, 4, 14, 28, 17, '#2f4a2a')
            line(glow, 6, 5, 15, 15, '#6a4a2e'); line(glow, 17, 4, 26, 13, '#7a5636')
            for x in (9, 12):
                line(glow, x, 5, x + 1, 15, '#4a3220')
            for x in (20, 23):
                line(glow, x, 4, x + 1, 13, '#553a24')
            line(glow, 15, 5, 17, 15, '#ffd23a'); line(glow, 17, 13, 26, 15, '#ffd23a')
            glow.putpixel((20, 14), signs.tone('#15100e')); glow.putpixel((23, 14), signs.tone('#15100e'))   # two eyes in the light
            centre(glow, 19, 'CELLAR', '#e9dcc0', x0=1, x1=31); centre(glow, 25, 'DOOR 2', '#e0463a', x0=1, x1=31)
        elif kind == 1:
            field(glow, (1, 1, 31, 31), '#12241a', rnd, 2)
            signs.stamp(glow, 21, 3, ['.###.', '#####', '#####', '#####', '.###.'], {'#': '#e9e6c0'})          # the moon
            line(glow, 1, 13, 31, 17, '#0c1a12')                                                               # the water
            for x in (3, 9, 24, 27):
                line(glow, x, 14, x + 3, 15, '#2f5a44')
            # A hand up out of it, a corsage on its wrist.
            signs.stamp(glow, 9, 3, ['#.#.#..', '#.#.#.#', '#####.#', '#######', '.#####.', '..###..', '..###..', '..###..', '..###..', '..###..', '..###..'], {'#': '#7ab06a'})
            signs.stamp(glow, 10, 9, ['.#.', '###', '.#.'], {'#': '#ff7ab0'}); glow.putpixel((11, 10), signs.tone('#ffe25a'))
            centre(glow, 19, 'SWAMP', '#ff7ab0', x0=1, x1=31); centre(glow, 25, 'PROM', '#e9e6c0', x0=1, x1=31)
        else:
            field(glow, (1, 1, 31, 31), '#1a1420', rnd, 2)
            line(glow, 1, 15, 31, 17, '#241a2c')
            # A headstone, and the doctor's lamp strapped to it.
            signs.stamp(glow, 10, 3, ['..########..', '.##########.', '############', '############', '############', '############', '############', '############',
                                     '############', '############', '############', '############', '############'], {'#': '#8a8a92'})
            line(glow, 10, 6, 22, 7, '#2a2630')                                                                # the band
            signs.stamp(glow, 14, 4, ['.##.', '####', '####', '.##.'], {'#': '#e9e6dc'}); glow.putpixel((15, 5), signs.tone('#9ad0ff'))
            line(glow, 13, 10, 19, 11, '#3a3640'); line(glow, 14, 12, 18, 13, '#3a3640')                         # what is cut in it
            centre(glow, 19, 'DR.', '#e9e6dc', x0=1, x1=31); centre(glow, 25, 'GRAVELY', '#e0463a', x0=1, x1=31)
        return im, glow
    return paint


def scoreboard(rnd):
    """The field's scoreboard on the school's wall, switched off: the last game's numbers still stand in the dead bulbs."""
    im = canvas(80, 32)
    field(im, (0, 0, 80, 32), '#1f3a2a', rnd, 3); border(im, (0, 0, 80, 32), '#d6d2c2')
    centre(im, 3, 'OTTERS', '#d6d2c2', x0=2, x1=34); centre(im, 3, 'GUESTS', '#d6d2c2', x0=46, x1=78)
    for x0 in (5, 49):
        field(im, (x0, 11, x0 + 26, 28), '#0e1512', rnd, 2)
    centre(im, 13, '06', '#8a7436', True, 2, x0=5, x1=31); centre(im, 13, '34', '#8a7436', True, 2, x0=49, x1=75)
    centre(im, 12, 'QTR', '#d6d2c2', x0=32, x1=48); field(im, (36, 19, 44, 28), '#0e1512', rnd, 2); centre(im, 21, '4', '#8a7436', x0=36, x1=44)
    return im, None


SIGNS = [
    # name, columns, rows, mount, painter, light
    ('arch', 6, 1, 'free', arch, 0), ('rules', 4, 2, 'wall', rules, 0),
    ('wash_name', 8, 1, 'wall', wash_name, 6), ('wash_front', 7, 2, 'wall', wash_front, 10),
    ('bigtop_name', 7, 2, 'wall', bigtop(False), 8), ('bigtop_name_dim', 7, 2, 'wall', bigtop(True), 8), ('bigtop_front', 7, 2, 'wall', bigtop_front, 12),
    ('foodfair_name', 7, 2, 'wall', foodfair_name, 6),
    ('food_a', 3, 2, 'wall', food_window(('CHUCK ROAST', '.89 LB'), '#b3261f', True), 10), ('food_b', 3, 2, 'wall', food_window(('CANDY CORN', '2 FOR 1'), '#2b4f8f', True), 10),
    ('food_c', 3, 2, 'wall', food_window(('PUMPKINS', '.05 LB'), '#b3261f', True), 10),
    ('food_a_dark', 3, 2, 'wall', food_window(('CHUCK ROAST', '.89 LB'), '#b3261f', False), 0), ('food_b_dark', 3, 2, 'wall', food_window(('CANDY CORN', '2 FOR 1'), '#2b4f8f', False), 0),
    ('food_c_dark', 3, 2, 'wall', food_window(('PUMPKINS', '.05 LB'), '#b3261f', False), 0),
    ('marquee_a', 7, 2, 'wall', marquee('CELLAR DOOR 2', 'THE MOON MEN'), 14), ('marquee_b', 7, 2, 'wall', marquee('BITING HOUR', 'SWAMP PROM'), 14),
    ('marquee_c', 7, 2, 'wall', marquee('IT CAME BACK', 'DR. GRAVELY'), 14), ('theater_front', 7, 2, 'wall', theater_front, 8),
    ('jrhigh_name', 8, 1, 'wall', jrhigh_name, 0), ('school_front', 6, 2, 'wall', school_front, 0), ('trophy', 2, 2, 'wall', trophy, 8),
    ('bank_name', 7, 1, 'wall', bank_name, 0), ('bank_front', 5, 2, 'wall', bank_front, 2),
    ('video_name', 7, 1, 'wall', video_name, 8), ('video_front', 5, 2, 'wall', video_front, 10),
    ('barber_name', 5, 1, 'wall', barber_name, 0), ('barber_front', 4, 2, 'wall', barber_front, 5),
    ('hardware_name', 7, 1, 'wall', hardware_name, 0), ('hardware_front', 6, 2, 'wall', hardware_front, 4),
    ('house_a', 2, 2, 'wall', house_window(0), 8), ('house_b', 2, 2, 'wall', house_window(1), 8), ('house_c', 2, 2, 'wall', house_window(2), 3),
    ('park_st', 3, 1, 'free', street('PARK ST'), 0), ('main_st', 3, 1, 'free', street('MAIN ST'), 0), ('elm_st', 3, 1, 'free', street('ELM ST'), 0),
    ('church_ln', 3, 1, 'free', street('CHURCH LN'), 0), ('mill_st', 3, 1, 'free', street('MILL ST'), 0), ('west_ave', 3, 1, 'free', street('WEST AVE'), 0),
    ('east_ave', 3, 1, 'free', street('EAST AVE'), 0),
    ('cemetery', 6, 1, 'free', cemetery, 0), ('stop', 1, 1, 'free', stop, 0), ('house_door', 1, 2, 'wall', house_door, 0), ('food_door', 2, 2, 'wall', food_door, 9),
    ('drive_thru', 3, 1, 'free', drive_thru, 8), ('deliveries', 3, 1, 'wall', deliveries, 0),
    # 0.4.481, behind the shops and out among the houses.
    ('back_door', 1, 2, 'wall', back_door, 0), ('shutter', 2, 2, 'wall', shutter, 0), ('no_parking', 2, 1, 'wall', no_parking, 0),
    ('tag_rules', 3, 2, 'decal', tag_rules, 0), ('tag_here', 3, 1, 'decal', tag_here, 0), ('tag_skull', 1, 1, 'decal', tag_skull, 0),
    ('for_sale', 2, 1, 'free', for_sale, 0), ('beware', 2, 1, 'wall', beware, 0), ('elm_ct', 3, 1, 'free', street('ELM CT'), 0),
    ('dead_end', 2, 1, 'free', dead_end, 0), ('lost_cat', 1, 1, 'wall', lost_cat, 0),
    # The way to the cemetery, named at the mouth of the cut; three one-sheets down the theater's side; the field's scoreboard.
    ('to_cemetery', 3, 1, 'free', way('CEMETERY'), 0),
    ('poster_a', 2, 2, 'wall', poster(0), 6), ('poster_b', 2, 2, 'wall', poster(1), 6), ('poster_c', 2, 2, 'wall', poster(2), 6),
    ('scoreboard', 5, 2, 'wall', scoreboard, 0),
]

_piece = signs.piece


def piece(name, im, glow, mount, light, window, left, bottom):
    m = _piece(name, im, glow, mount, light, window, left, bottom)
    w, h = im.size
    if mount == 'free' and w > 16:
        x0, y0, x1, y1 = signs.bounds(im, window)
        m['elements'][0]['faces']['south'] = {'texture': '#a', 'uv': [round((w - x1) * 16 / w, 4), round(y0 * 16 / h, 4), round((w - x0) * 16 / w, 4), round(y1 * 16 / h, 4)]}
        if glow is not None:
            # 0.4.481: what is lit on such a board is lit on its back as well. The drive-thru's board is lettered in its light, and
            # from behind it was a blank white box. The back of a block shows the lit part of the board's other end, as its paint does.
            wx0, wy0, wx1, wy1 = window
            lit = signs.bounds(glow, (w - wx1, wy0, w - wx0, wy1))
            if lit is not None:
                g0, gy0, g1, gy1 = lit
                m['textures']['g'] = f'{signs.MOD}:block/{signs.DIR}/{name}_glow'
                m['elements'].append(signs.element([16 - left - w + g0, bottom + h - gy1, 8.75], [16 - left - w + g1, bottom + h - gy0, 8.75],
                                                   {'south': {'texture': '#g', 'uv': [round(g0 * 16 / w, 4), round(gy0 * 16 / h, 4), round(g1 * 16 / w, 4), round(gy1 * 16 / h, 4)]}}, 15))
    return m


def main():
    signs.DIR = 'calder_sign'
    signs.JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CalderSigns.java'
    signs.SIGNS = SIGNS
    signs.PLACED = {}
    signs._PICS = None
    signs.FRAMES = {'wash_front': (WASH_FRAMES, WASH_TICKS), 'bigtop_front': (CABINET_FRAMES, CABINET_TICKS)}
    signs.piece = piece
    signs.main()


if __name__ == '__main__':
    main()
