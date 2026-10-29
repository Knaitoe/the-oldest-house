"""0.4.172 (owner: improve Marjorie's crawl into and destruction of the cardboard box): Merry's cardboard playhouse.

One 64x64 entity texture for the hinged mesh GhostsPropRenderer draws: two side walls (one net, the east wall mirrored),
the back wall and its two gable steps, the front wall's two jambs, lintel and gable steps round the doorway, and the two
halves of the roof. Outside, Merry's crayon: a sun and vines on the sides, the two sisters holding hands on the back,
windows and flowers either side of the door, a heart over it, red shingles on the roof. Inside, plain board with the box's
printed arrow and a strip of tape, which is what shows once the walls fall open. One texel to a pixel, as the blocks are.

Usage: python3 tools/draw_ghosts_playhouse.py [--check]
"""
from pathlib import Path
import argparse
from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/the_oldest_house/textures/entity/ghosts_playhouse.png'
BOARD, FLUTE, EDGE, SHADE = '#a98a5e', '#b6976a', '#7e6341', '#94774f'
TAPE, TAPE_SHINE, PRINT = '#c9b07c', '#ddc795', '#5a4a36'
SUN, VINE, VINE_DARK, SKY, WINDOW_FRAME = '#f2c23a', '#4f9a3e', '#336b2a', '#5f8fd0', '#3c4f7a'
PINK, PURPLE, RED, ROOF, ROOF_DARK, ROOF_LINE = '#e07aa8', '#8a5bc4', '#c8423a', '#b8463a', '#8f3229', '#d36a55'


def board(d, x0, y0, x1, y1, seed=0):
    """Plain corrugated board: flutes every other column, a darker cut edge."""
    d.rectangle((x0, y0, x1 - 1, y1 - 1), fill=BOARD)
    for x in range(x0, x1):
        if (x + seed) % 2 == 0:
            d.line((x, y0, x, y1 - 1), fill=FLUTE)
    for y in range(y0, y1):
        if (y * 3 + seed) % 7 == 0:
            d.point((x0 + (y * 5 + seed) % max(1, x1 - x0), y), fill=SHADE)


def edge(d, x0, y0, x1, y1):
    """A cut edge: the flutes show as a dotted brown line."""
    d.rectangle((x0, y0, x1 - 1, y1 - 1), fill=EDGE)
    for x in range(x0, x1):
        for y in range(y0, y1):
            if (x + y) % 2 == 0:
                d.point((x, y), fill=SHADE)


def inside(d, x0, y0, x1, y1, seed=0):
    """The inside of the box: board, the printed THIS WAY UP arrow and a strip of tape."""
    board(d, x0, y0, x1, y1, seed)
    w, h = x1 - x0, y1 - y0
    if w >= 8 and h >= 6:
        # Two arrows side by side, the way they are printed on a box: chevron heads, short shafts.
        cy = y0 + h // 2
        for cx in (x0 + w // 2 - 2, x0 + w // 2 + 2):
            d.point((cx, cy - 2), fill=PRINT)
            d.point((cx - 1, cy - 1), fill=PRINT)
            d.point((cx + 1, cy - 1), fill=PRINT)
            d.line((cx, cy - 1, cx, cy + 2), fill=PRINT)
    ty = y0 + (seed % max(1, h - 1))
    d.line((x0, ty, x1 - 1, ty), fill=TAPE)
    d.point((x0 + seed % max(1, w), ty), fill=TAPE_SHINE)


def window(d, x0, y0, w=3, h=3):
    d.rectangle((x0, y0, x0 + w - 1, y0 + h - 1), fill=SKY)
    d.rectangle((x0, y0, x0 + w - 1, y0 + h - 1), outline=WINDOW_FRAME)
    if w >= 3:
        d.line((x0 + w // 2, y0, x0 + w // 2, y0 + h - 1), fill=WINDOW_FRAME)


def side(d, x0, y0):
    """An outer side wall, 16 wide and 12 tall: a sun, a window, and vines along the bottom."""
    board(d, x0, y0, x0 + 16, y0 + 12, 1)
    for x, y in ((2, 1), (3, 1), (1, 2), (2, 2), (3, 2), (4, 2), (2, 3), (3, 3)):
        d.point((x0 + x, y0 + y), fill=SUN)
    for x, y in ((0, 0), (5, 0), (0, 4), (5, 4)):
        d.point((x0 + x, y0 + y), fill=SUN)
    window(d, x0 + 9, y0 + 3, 4, 3)
    for x in range(16):
        top = 9 + (x * 5) % 3
        d.line((x0 + x, y0 + top, x0 + x, y0 + 11), fill=VINE if x % 3 else VINE_DARK)
    for x in (2, 7, 12):
        d.point((x0 + x, y0 + 8), fill=PINK)


def sisters(d, x0, y0):
    """The back wall, 14 wide and 12 tall: the two sisters holding hands, one taller, in crayon."""
    board(d, x0, y0, x0 + 14, y0 + 12, 2)
    for cx, top, colour in ((4, 2, PURPLE), (9, 4, PINK)):
        d.point((x0 + cx, y0 + top), fill=PRINT)
        d.rectangle((x0 + cx - 1, y0 + top + 1, x0 + cx + 1, y0 + top + 4), fill=colour)
        d.line((x0 + cx - 1, y0 + top + 5, x0 + cx - 1, y0 + 10), fill=PRINT)
        d.line((x0 + cx + 1, y0 + top + 5, x0 + cx + 1, y0 + 10), fill=PRINT)
    d.line((x0 + 5, y0 + 5, x0 + 8, y0 + 6), fill=PRINT)
    for x, y in ((6, 1), (7, 1), (6, 2)):
        d.point((x0 + x, y0 + y), fill=RED)
    for x in range(14):
        d.point((x0 + x, y0 + 11), fill=VINE if x % 2 else VINE_DARK)


def paint():
    im = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # Side walls, box (1, 12, 16) at (0, 0): the inner face is the -x net face, the outer the +x one.
    edge(d, 16, 0, 18, 16)
    inside(d, 0, 16, 16, 28, 3)
    edge(d, 16, 16, 17, 28)
    side(d, 17, 16)
    edge(d, 33, 16, 34, 28)
    # Back wall, box (14, 12, 1) at (34, 0): outer is the -z face, inner the +z face.
    edge(d, 35, 0, 63, 1)
    edge(d, 34, 1, 35, 13)
    sisters(d, 35, 1)
    edge(d, 49, 1, 50, 13)
    inside(d, 50, 1, 64, 13, 5)
    # Gable steps over the back and front walls: outer red crayon edging, inner board.
    for u, v, w in ((34, 13, 10), (34, 16, 6), (34, 19, 10), (48, 16, 6)):
        edge(d, u + 1, v, u + 1 + 2 * w, v + 1)
        edge(d, u, v + 1, u + 1, v + 3)
        front, back = (u + 1, v + 1, u + 1 + w, v + 3), (u + 2 + w, v + 1, u + 2 + 2 * w, v + 3)
        outer, inner = (front, back) if (u, v) in ((34, 13), (34, 16)) else (back, front)
        board(d, *outer, 4)
        d.line((outer[0], outer[1], outer[2] - 1, outer[1]), fill=RED)
        inside(d, *inner, 1)
    # Front wall round the doorway: jambs (4, 12, 1) at (0, 28) and (10, 28), lintel (6, 3, 1) at (20, 28). Outer is +z.
    for u in (0, 10):
        edge(d, u + 1, 28, u + 9, 29)
        edge(d, u, 29, u + 1, 41)
        inside(d, u + 1, 29, u + 5, 41, 2 + u)
        edge(d, u + 5, 29, u + 6, 41)
        board(d, u + 6, 29, u + 10, 41, u)
        window(d, u + 6, 32, 3, 3)
        d.point((u + 7, 38), fill=PINK)
        d.point((u + 8, 39), fill=SUN)
        d.line((u + 6, 40, u + 9, 40), fill=VINE)
    edge(d, 21, 28, 33, 29)
    edge(d, 20, 29, 21, 32)
    inside(d, 21, 29, 27, 32, 1)
    edge(d, 27, 29, 28, 32)
    board(d, 28, 29, 34, 32, 0)
    for x, y in ((30, 30), (32, 30), (30, 31), (31, 31), (32, 31), (31, 32 - 1)):
        d.point((x, y), fill=RED)
    # Roof halves, box (10, 1, 18) at (0, 41): red crayon shingles on top, board underneath.
    d.rectangle((18, 41, 27, 58), fill=ROOF)
    for y in range(41, 59):
        if (y - 41) % 3 == 2:
            d.line((18, y, 27, y), fill=ROOF_DARK)
        else:
            for x in range(18, 28):
                if (x + y) % 4 == 0:
                    d.point((x, y), fill=ROOF_LINE)
    inside(d, 28, 41, 38, 59, 6)
    for box in ((0, 59, 18, 60), (18, 59, 28, 60), (28, 59, 46, 60), (46, 59, 56, 60)):
        edge(d, *box)
    return im


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    check = parser.parse_args().check
    im = paint()
    if check:
        with Image.open(OUT) as saved:
            assert saved.convert('RGBA').tobytes() == im.tobytes(), 'ghosts_playhouse.png differs from tools/draw_ghosts_playhouse.py'
        print('Verified the cardboard playhouse texture.')
    else:
        OUT.parent.mkdir(parents=True, exist_ok=True)
        im.save(OUT, optimize=True)
        print('Painted the cardboard playhouse texture.')


if __name__ == '__main__':
    main()
