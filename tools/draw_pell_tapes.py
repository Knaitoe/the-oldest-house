#!/usr/bin/env python3
"""The Pell house's tapes (docs/PELL_HOUSE_PROPOSAL.md, "The tapes"; docs/PELL_HOUSE_TAPES_0_4_536.md): nine home recordings made by an
older woman who is never named, for somebody off camera. One picture for each, 96 by 72, in a three-by-three atlas the shed's
television plays from (client.PellTapeClient wears it like an old tape as it plays: grain, a rolling band, lines out of true).

The recordings, in the order she made them, each with its date stamped in the corner as a camcorder stamps it:
  1  her kitchen table, a mug, the net curtain behind her
  2  a floor, and a line of paint going round from her brush
  3  a garden at dusk, the line along the foot of a fence
  4  a small room, a mattress on the floor inside a painted ring
  5  a place laid at a table with nothing on the plate
  6  two school photographs side by side
  7  a child's bed with a dress laid out on it, pencil marks up the door frame
  8  water at night, a pool lit from under
  9  her again, nearer, her hand coming at the lens

Nothing in them is a wound or a body. Drawn from fixed seeds; run it to draw, --check to verify the pack against it (CI runs that).
"""
import argparse
import random
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/the_oldest_house/textures/pell/tapes.png'
W, H, COLS = 96, 72, 3
# The dates on the spines and stamped on the pictures (PellTexts.TAPE_DATES says the same).
STAMPS = ['MAR 2', 'MAR 9', 'MAR16', 'MAR23', 'MAR30', 'APR 6', 'APR13', 'APR20', 'APR27']


def rgb(c):
    c = c.lstrip('#')
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def shade(c, k):
    r, g, b = rgb(c) if isinstance(c, str) else c
    return (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)))


class Shot:
    def __init__(self, base, seed):
        self.im = Image.new('RGB', (W, H), rgb(base))
        self.px = self.im.load()
        self.rnd = random.Random(seed)

    def dot(self, x, y, c):
        if 0 <= x < W and 0 <= y < H:
            self.px[x, y] = rgb(c) if isinstance(c, str) else c

    def rect(self, x0, y0, x1, y1, c, grain=0):
        for y in range(max(0, y0), min(H, y1)):
            for x in range(max(0, x0), min(W, x1)):
                self.dot(x, y, shade(c, self.rnd.randint(-grain, grain)) if grain else c)

    def ellipse(self, cx, cy, rx, ry, c, grain=0):
        for y in range(int(cy - ry), int(cy + ry) + 1):
            for x in range(int(cx - rx), int(cx + rx) + 1):
                if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1:
                    self.dot(x, y, shade(c, self.rnd.randint(-grain, grain)) if grain else c)

    def ring(self, cx, cy, rx, ry, width, c):
        for y in range(int(cy - ry - width), int(cy + ry + width) + 1):
            for x in range(int(cx - rx - width), int(cx + rx + width) + 1):
                d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
                if abs(d ** .5 - 1) * min(rx, ry) <= width / 2:
                    self.dot(x, y, c)

    def line(self, pts, c, width=1):
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            n = max(abs(x1 - x0), abs(y1 - y0), 1)
            for i in range(n + 1):
                x, y = round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)
                for dx in range(width):
                    for dy in range(width):
                        self.dot(x + dx, y + dy, c)

    def soften(self):
        """A home camera's softness: each pixel a little of its neighbours."""
        src = self.im.copy().load()
        for y in range(H):
            for x in range(W):
                acc = [0, 0, 0]
                n = 0
                for dx, dy, k in ((0, 0, 4), (-1, 0, 1), (1, 0, 1), (0, -1, 1), (0, 1, 1)):
                    xx, yy = min(W - 1, max(0, x + dx)), min(H - 1, max(0, y + dy))
                    p = src[xx, yy]
                    for i in range(3):
                        acc[i] += p[i] * k
                    n += k
                self.px[x, y] = tuple(a // n for a in acc)


# A small camcorder font, three wide and five tall, for the stamp.
GLYPHS = {
    'A': ['010', '101', '111', '101', '101'], 'M': ['101', '111', '111', '101', '101'], 'P': ['110', '101', '110', '100', '100'],
    'R': ['110', '101', '110', '101', '101'], '0': ['111', '101', '101', '101', '111'], '1': ['010', '110', '010', '010', '111'],
    '2': ['111', '001', '111', '100', '111'], '3': ['111', '001', '011', '001', '111'], '4': ['101', '101', '111', '001', '001'],
    '5': ['111', '100', '111', '001', '111'], '6': ['111', '100', '111', '101', '111'], '7': ['111', '001', '010', '010', '010'],
    '8': ['111', '101', '111', '101', '111'], '9': ['111', '101', '111', '001', '111'], ' ': ['000'] * 5,
}


def stamp(s, text):
    """The date, low on the left, in the camera's square letters with a dark edge."""
    x0, y0 = 4, H - 10
    for i, ch in enumerate(text):
        for gy, row in enumerate(GLYPHS[ch]):
            for gx, on in enumerate(row):
                if on == '1':
                    x, y = x0 + i * 4 + gx, y0 + gy
                    s.dot(x + 1, y + 1, '#202020')
                    s.dot(x, y, '#f0e8b0')


def woman(s, cx, top, scale=1.0):
    """Her, from the chest up: grey hair pinned back, a soft face the camera never quite finds, a cardigan."""
    r = lambda v: int(round(v * scale))
    s.ellipse(cx, top + r(42), r(22), r(14), '#5c5262', 6)          # shoulders in the cardigan
    s.rect(cx - r(6), top + r(24), cx + r(6), top + r(32), '#b89884', 4)  # neck
    s.ellipse(cx, top + r(16), r(11), r(13), '#c4a28c', 5)          # face
    s.ellipse(cx, top + r(8), r(12), r(8), '#a8a49c', 8)            # hair over the brow
    s.ellipse(cx, top + r(2), r(7), r(5), '#9a968e', 6)             # the bun
    for side in (-1, 1):
        s.ellipse(cx + side * r(4), top + r(15), r(2), r(1.5), '#8a6c5c')  # the eyes, two soft shadows
    s.rect(cx - r(3), top + r(22), cx + r(3), top + r(23), '#9a7466')  # the mouth, a line
    s.rect(cx - r(9), top + r(36), cx + r(9), top + r(38), '#6c6272')  # the cardigan's buttons row
    for i in range(3):
        s.dot(cx, top + r(40) + i * r(4), '#d8d0c0')


def shot_kitchen():
    s = Shot('#8a8270', 9601)
    s.rect(0, 0, W, 50, '#8a8270', 4)
    s.rect(54, 6, 90, 40, '#c8c4b4', 6)                 # the window, net curtain over it
    for x in range(55, 90, 3):
        s.line([(x, 6), (x, 40)], '#b8b4a4')
    s.rect(54, 6, 90, 8, '#7a6a58'); s.rect(54, 38, 90, 40, '#7a6a58')
    woman(s, 40, 8)
    s.rect(0, 50, W, H, '#6a5640', 5)                   # the table
    s.rect(64, 44, 74, 54, '#d8d4cc', 3); s.rect(74, 46, 77, 51, '#d8d4cc')  # the mug
    s.soften()
    return s


def shot_floor():
    s = Shot('#9a9078', 9602)
    for y in range(0, H, 9):                            # linoleum, a tile pattern seen from above at a slant
        for x in range(0, W, 9):
            s.rect(x, y, x + 9, y + 9, '#a49a80' if (x // 9 + y // 9) % 2 else '#8e8670', 4)
    pts = [(0, 52), (14, 44), (30, 38), (48, 35), (64, 36), (80, 40), (92, 46)]
    s.line(pts, '#f2efe4', 3)                           # the paint, still wet
    s.line([(p[0], p[1] + 3) for p in pts], '#d8d4c8')
    s.ellipse(86, 30, 9, 6, '#c4a28c', 5)              # her hand on the brush
    s.rect(78, 30, 84, 33, '#6a4a30'); s.rect(72, 32, 80, 36, '#e8e4d8')  # the brush's handle and wet bristles
    s.soften()
    return s


def shot_garden():
    s = Shot('#3c4250', 9603)
    for y in range(0, 30):
        s.rect(0, y, W, y + 1, shade('#5a6070', -y))   # dusk
    s.rect(0, 30, W, 46, '#5a4c3c', 3)                 # the fence, palings
    for x in range(0, W, 5):
        s.rect(x, 28, x + 4, 46, '#6a5a48', 4)
    s.rect(0, 46, W, H, '#3e5236', 6)                   # the lawn
    s.line([(0, 52), (40, 50), (96, 48)], '#e8e6dc', 2)  # the line along the fence foot
    s.rect(76, 14, 96, 46, '#7c7466', 3); s.rect(80, 20, 88, 30, '#c8b878')  # a corner of the house, a lit window
    s.soften()
    return s


def shot_ring_room():
    s = Shot('#7c766a', 9604)
    s.rect(0, 0, W, 40, '#7c766a', 4)
    s.rect(0, 40, W, H, '#6c6050', 5)                   # bare boards
    for y in range(42, H, 6):
        s.line([(0, y), (W, y)], '#5e5444')
    s.ring(48, 56, 34, 11, 2, '#ece8dc')                # the painted ring
    s.rect(34, 50, 62, 60, '#b8b0a0', 3)                # the mattress
    s.rect(34, 50, 62, 52, '#c8c0b0')
    s.rect(70, 10, 80, 26, '#8a8478')                   # a switch plate and a bare patch where a picture hung
    s.soften()
    return s


def shot_place_laid():
    s = Shot('#6a5640', 9605)
    s.rect(0, 0, W, H, '#6a5640', 5)                    # the table, from over it
    s.rect(6, 6, 90, 66, '#d8d0bc', 3)                  # a cloth
    s.ellipse(48, 36, 20, 16, '#ecebe6', 2)            # the plate, empty
    s.ring(48, 36, 14, 11, 1, '#c8c6be')
    s.rect(22, 22, 24, 50, '#b8b8b4'); s.rect(72, 22, 74, 50, '#b8b8b4')  # fork, knife
    s.ellipse(78, 16, 5, 5, '#c0d0d4', 2)              # a glass of water
    s.ellipse(84, 66, 12, 8, '#c4a28c', 4)             # her hand at the edge, setting it straight
    s.soften()
    return s


def portrait(s, x0, y0, hair, shirt):
    s.rect(x0, y0, x0 + 30, y0 + 38, '#f0ece2')         # the print's white border
    s.rect(x0 + 2, y0 + 2, x0 + 28, y0 + 32, '#7e94a8', 3)  # the studio's backdrop
    s.ellipse(x0 + 15, y0 + 30, 11, 7, shirt, 4)
    s.ellipse(x0 + 15, y0 + 15, 6, 7, '#c8a48e', 3)
    s.ellipse(x0 + 15, y0 + 10, 8, 6, hair, 5)
    for side in (-1, 1):
        s.rect(x0 + 15 + side * 7 - (2 if side > 0 else 0), y0 + 10, x0 + 18 + side * 7 - (2 if side > 0 else 0), y0 + 26, hair, 4)


def shot_photographs():
    s = Shot('#7a5a48', 9606)
    s.rect(0, 0, W, H, '#7a5a48', 6)                    # a table runner
    portrait(s, 10, 14, '#5a4636', '#4e6a58')
    portrait(s, 54, 16, '#5c4838', '#4e6a58')
    s.soften()
    return s


def shot_bedroom():
    s = Shot('#a49c8a', 9607)
    s.rect(0, 0, W, 44, '#b0a690', 4)                   # wallpaper
    for x in range(4, W, 10):
        for y in range(4, 44, 10):
            s.dot(x, y, '#c4b49c')
    s.rect(0, 44, W, H, '#8a7c66', 4)
    s.rect(30, 34, 90, 54, '#d4ccbc', 3)                # the bed
    s.rect(30, 30, 40, 40, '#e8e2d6')                   # the pillow
    s.ellipse(64, 42, 14, 7, '#6c84a0', 4)             # a dress laid out on it
    s.rect(58, 42, 70, 52, '#6c84a0', 4)
    s.rect(4, 0, 12, H, '#9a8a70', 2)                   # the door frame, and its pencil heights
    for i, y in enumerate((52, 44, 37, 31, 26)):
        s.rect(5, y, 11, y + 1, '#4a4038')
    s.soften()
    return s


def shot_pool():
    s = Shot('#0c1014', 9608)
    s.rect(0, 0, W, 28, '#0c1014', 2)
    s.rect(0, 28, W, H, '#1a2018', 3)                   # paving, dark
    for y in range(36, 66):                             # the pool, its far end narrower, lit from under
        inset = (66 - y) // 2
        s.rect(6 + inset - 2, y, 90 - inset + 2, y + 1, '#c8c4b4' if y in (36, 65) else '#2e8a96', 0 if y in (36, 65) else 5)
        s.dot(6 + inset - 2, y, '#c8c4b4'); s.dot(6 + inset - 1, y, '#c8c4b4'); s.dot(90 - inset + 1, y, '#c8c4b4'); s.dot(90 - inset, y, '#c8c4b4')
    s.ellipse(48, 54, 24, 7, '#58b8c0', 5)
    s.ellipse(42, 56, 6, 3, '#9adcdc')                 # the light under it
    for x in range(14, 84, 7):                          # the ripples' lines
        s.line([(x, 44 + (x * 3) % 9), (x + 4, 44 + (x * 3) % 9)], '#7acccc')
    s.soften()
    return s


def shot_close():
    s = Shot('#8a8270', 9609)
    s.rect(0, 0, W, H, '#8a8270', 4)
    s.rect(60, 0, 96, 30, '#c8c4b4', 6)
    woman(s, 34, 2, 1.45)
    s.ellipse(80, 50, 24, 26, '#c4a28c', 8)            # her hand, too near to be sharp
    s.rect(60, 30, 72, 40, '#b8967e', 6)
    s.soften()
    s.soften()
    return s


SHOTS = [shot_kitchen, shot_floor, shot_garden, shot_ring_room, shot_place_laid, shot_photographs, shot_bedroom, shot_pool, shot_close]


def atlas():
    out = Image.new('RGB', (W * COLS, H * ((len(SHOTS) + COLS - 1) // COLS)), (0, 0, 0))
    for i, make in enumerate(SHOTS):
        s = make()
        stamp(s, STAMPS[i])
        out.paste(s.im, ((i % COLS) * W, (i // COLS) * H))
    return out


def main():
    check = argparse.ArgumentParser()
    check.add_argument('--check', action='store_true')
    check = check.parse_args().check
    im = atlas()
    if check:
        assert OUT.exists(), OUT
        have = Image.open(OUT).convert('RGB')
        assert have.size == im.size and have.tobytes() == im.tobytes(), 'the tapes atlas differs from tools/draw_pell_tapes.py'
        print(f'Verified the Pell house\'s {len(SHOTS)} tapes ({im.size[0]} by {im.size[1]}).')
        return
    OUT.parent.mkdir(parents=True, exist_ok=True)
    im.save(OUT, optimize=True)
    print(f'Drew the Pell house\'s {len(SHOTS)} tapes ({im.size[0]} by {im.size[1]}).')


if __name__ == '__main__':
    sys.exit(main())
