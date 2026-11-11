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


SIGNS = [
    # name, columns, rows, mount, painter, light
    ('arch', 6, 1, 'free', arch, 0), ('rules', 4, 2, 'wall', rules, 0),
]

_piece = signs.piece


def piece(name, im, glow, mount, light, window, left, bottom):
    m = _piece(name, im, glow, mount, light, window, left, bottom)
    w, h = im.size
    if mount == 'free' and w > 16:
        x0, y0, x1, y1 = signs.bounds(im, window)
        m['elements'][0]['faces']['south'] = {'texture': '#a', 'uv': [round((w - x1) * 16 / w, 4), round(y0 * 16 / h, 4), round((w - x0) * 16 / w, 4), round(y1 * 16 / h, 4)]}
    return m


def main():
    signs.DIR = 'calder_sign'
    signs.JAVA = ROOT / 'src/main/java/io/github/knaitoe/theoldesthouse/labyrinth/CalderSigns.java'
    signs.SIGNS = SIGNS
    signs.PLACED = {}
    signs._PICS = None
    signs.piece = piece
    signs.main()


if __name__ == '__main__':
    main()
