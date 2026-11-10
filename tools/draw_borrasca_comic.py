#!/usr/bin/env python3
"""0.4.445, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md): the comic Kelly tucks into the fort's window sill.

The owner, 10 November: "Let her tuck a borrowed comic into that sill for you. Years later, when you find her in Room 6, she
remembers which window you chose and asks if you ever finished it."

KIT & CABOOSE No. 31, "The Light in the Crater": an original space comic of the kind a drugstore spinner would still have had
years after it came out. Eight pages, each 128 by 192 pixels: the cover, six pages of story that stop on "CONTINUED IN No. 32",
and a back cover with a subscription coupon somebody began to fill in with a pencil. Everything is drawn here, with its own
four-by-five lettering; nothing is copied from any real comic.

Writes the page atlas (four pages by two) and a sun-bleached copy of it for the years-later visits, and the comic's item pictures.
Run with --check to verify the committed files.
"""
import argparse
import json
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
W, H, PAGES = 128, 192, 8

INK = (20, 18, 24); PAPER = (232, 222, 196); WHITE = (246, 242, 230); RED = (200, 44, 32); YELLOW = (244, 208, 58)
BLUE = (52, 98, 170); SKY = (16, 16, 34); GREY = (150, 150, 144); DARK = (92, 92, 96); ORANGE = (214, 120, 40)
SKIN = (236, 196, 160); RUST = (176, 70, 44); GLOW = (255, 240, 150); GREEN = (70, 150, 84); PENCIL = (96, 96, 104)

GLYPHS = {
    'A': '.##. #..# #### #..# #..#', 'B': '###. #..# ###. #..# ###.', 'C': '.### #... #... #... .###', 'D': '###. #..# #..# #..# ###.',
    'E': '#### #... ###. #... ####', 'F': '#### #... ###. #... #...', 'G': '.### #... #.## #..# .###', 'H': '#..# #..# #### #..# #..#',
    'I': '### .#. .#. .#. ###', 'J': '..## ...# ...# #..# .##.', 'K': '#..# #.#. ##.. #.#. #..#', 'L': '#... #... #... #... ####',
    'M': '#...# ##.## #.#.# #...# #...#', 'N': '#..# ##.# #.## #..# #..#', 'O': '.##. #..# #..# #..# .##.', 'P': '###. #..# ###. #... #...',
    'Q': '.##. #..# #..# #.#. .#.#', 'R': '###. #..# ###. #.#. #..#', 'S': '.### #... .##. ...# ###.', 'T': '### .#. .#. .#. .#.',
    'U': '#..# #..# #..# #..# .##.', 'V': '#.# #.# #.# #.# .#.', 'W': '#...# #...# #.#.# ##.## #...#', 'X': '#.# #.# .#. #.# #.#',
    'Y': '#.# #.# .#. .#. .#.', 'Z': '#### ...# .##. #... ####',
    '0': '.##. #..# #..# #..# .##.', '1': '.#. ##. .#. .#. ###', '2': '###. ...# .##. #... ####', '3': '###. ...# .##. ...# ###.',
    '4': '#..# #..# #### ...# ...#', '5': '#### #... ###. ...# ###.', '6': '.##. #... ###. #..# .##.', '7': '#### ...# ..#. .#.. .#..',
    '8': '.##. #..# .##. #..# .##.', '9': '.##. #..# .### ...# .##.',
    '.': '. . . . #', ',': '.. .. .. .# #.', '!': '# # # . #', '?': '###. ...# .##. .... .#..', "'": '# # . . .', '-': '... ... ### ... ...',
    ':': '. # . # .', '&': '.##.. #..#. .##.# #..#. .##.#', ' ': '.. .. .. .. ..',
}


def glyph(ch):
    rows = GLYPHS.get(ch.upper(), GLYPHS['?']).split()
    return rows, len(rows[0])


class Canvas:
    def __init__(self, w=W, h=H, colour=PAPER):
        self.im = Image.new('RGB', (w, h), colour); self.px = self.im.load(); self.w, self.h = w, h

    def dot(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = c

    def rect(self, x0, y0, x1, y1, c):
        for y in range(max(0, y0), min(self.h, y1)):
            for x in range(max(0, x0), min(self.w, x1)):
                self.px[x, y] = c

    def box(self, x0, y0, x1, y1, c):
        for x in range(x0, x1):
            self.dot(x, y0, c); self.dot(x, y1 - 1, c)
        for y in range(y0, y1):
            self.dot(x0, y, c); self.dot(x1 - 1, y, c)

    def ellipse(self, cx, cy, rx, ry, c, clip=None):
        for y in range(int(cy - ry), int(cy + ry) + 1):
            for x in range(int(cx - rx), int(cx + rx) + 1):
                if ((x - cx) / max(.5, rx)) ** 2 + ((y - cy) / max(.5, ry)) ** 2 <= 1 and (clip is None or (clip[0] <= x < clip[2] and clip[1] <= y < clip[3])):
                    self.dot(x, y, c)

    def line(self, x0, y0, x1, y1, c):
        n = max(abs(x1 - x0), abs(y1 - y0), 1)
        for i in range(n + 1):
            self.dot(round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), c)

    def halftone(self, x0, y0, x1, y1, c, clip=None):
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (x + y) % 2 == 0 and (clip is None or (clip[0] <= x < clip[2] and clip[1] <= y < clip[3])):
                    self.dot(x, y, c)

    def text(self, x, y, s, c, scale=1):
        for ch in s:
            rows, w = glyph(ch)
            for r, row in enumerate(rows):
                for k, bit in enumerate(row):
                    if bit == '#':
                        self.rect(x + k * scale, y + r * scale, x + (k + 1) * scale, y + (r + 1) * scale, c)
            x += (w + 1) * scale
        return x


def width(s, scale=1):
    return sum((glyph(ch)[1] + 1) * scale for ch in s) - scale


def wrap(s, max_px):
    lines, cur = [], ''
    for word in s.split(' '):
        trial = (cur + ' ' + word).strip()
        if cur and width(trial) > max_px:
            lines.append(cur); cur = word
        else:
            cur = trial
    if cur:
        lines.append(cur)
    return lines


def caption(c, x, y, w, s):
    """A narrator's box: yellow, ruled in ink. Returns its bottom."""
    lines = wrap(s, w - 6); h = len(lines) * 6 + 5
    c.rect(x, y, x + w, y + h, YELLOW); c.box(x, y, x + w, y + h, INK)
    for i, line in enumerate(lines):
        c.text(x + 3, y + 3 + i * 6, line, INK)
    return y + h


def balloon(c, x, y, s, tail=None, max_w=58, robot=False):
    """Somebody's words: a white balloon with its tail toward them. A robot's balloon has square corners. Returns its box."""
    lines = wrap(s, max_w); w = max(width(l) for l in lines) + 7; h = len(lines) * 6 + 5
    x = max(1, min(c.w - w - 1, x)); y = max(1, y)
    c.rect(x, y, x + w, y + h, WHITE); c.box(x, y, x + w, y + h, INK)
    if not robot:
        for cx, cy in ((x, y), (x + w - 1, y), (x, y + h - 1), (x + w - 1, y + h - 1)):
            c.dot(cx, cy, c.px[max(0, min(c.w - 1, cx + (-1 if cx == x else 1))), cy])
    for i, line in enumerate(lines):
        c.text(x + 4, y + 3 + i * 6, line, INK)
    if tail:
        tx, ty = tail; bx = max(x + 3, min(x + w - 5, tx)); by = y + h - 1 if ty > y else y
        step = 1 if ty > y else -1
        for k in range(1, 5):
            for j in range(max(0, 3 - k) + 1):
                c.dot(bx + j, by + k * step, WHITE)
            c.dot(bx - 1, by + k * step, INK); c.dot(bx + max(0, 3 - k) + 1, by + k * step, INK)
        for j in range(3):
            c.dot(bx + j, by, WHITE)
    return x, y, x + w, y + h


def panel(c, x0, y0, x1, y1, fill):
    c.rect(x0, y0, x1, y1, fill); c.box(x0, y0, x1, y1, INK)
    return (x0 + 1, y0 + 1, x1 - 1, y1 - 1)


def stars(c, clip, n, seed):
    rnd = random.Random(seed)
    for _ in range(n):
        x, y = rnd.randrange(clip[0], clip[2]), rnd.randrange(clip[1], clip[3])
        c.dot(x, y, WHITE if rnd.random() < .7 else YELLOW)


def ground(c, clip, top, seed, craters=3):
    """The moon's surface from top down to the panel's foot: grey, a pale lip, dots of shadow and a few craters."""
    c.rect(clip[0], top, clip[2], clip[3], GREY); c.rect(clip[0], top, clip[2], top + 1, WHITE)
    c.halftone(clip[0], top + 3, clip[2], clip[3], (134, 134, 130), clip)
    rnd = random.Random(seed)
    for _ in range(craters):
        x, y = rnd.randrange(clip[0] + 6, clip[2] - 6), rnd.randrange(top + 5, max(top + 6, clip[3] - 3))
        c.ellipse(x, y, rnd.randrange(3, 7), 1.6, DARK, clip); c.ellipse(x, y - 1, 2.5, .8, (60, 60, 66), clip)


def earth(c, x, y, r):
    c.ellipse(x, y, r, r, BLUE); c.ellipse(x - r * .3, y - r * .2, r * .4, r * .3, GREEN); c.ellipse(x + r * .35, y + r * .3, r * .3, r * .25, GREEN)
    c.line(int(x - r * .6), int(y + r * .1), int(x + r * .2), int(y - r * .5), WHITE)


def rocket_flying(c, x, y):
    """The mail rocket in flight, nose to the right, twenty-six pixels long."""
    c.rect(x, y, x + 18, y + 7, WHITE); c.box(x, y, x + 18, y + 7, INK)
    for i in range(5):
        c.rect(x + 18 + i, y + 1 + i // 2, x + 19 + i, y + 6 - i // 2, RED)
    c.line(x + 18, y, x + 23, y + 3, INK); c.line(x + 18, y + 6, x + 23, y + 3, INK)
    c.rect(x - 1, y - 3, x + 4, y, RED); c.rect(x - 1, y + 7, x + 4, y + 10, RED); c.box(x - 1, y - 3, x + 4, y + 1, INK); c.box(x - 1, y + 6, x + 4, y + 10, INK)
    c.ellipse(x + 12, y + 3, 2, 2, BLUE); c.rect(x + 5, y + 2, x + 8, y + 5, RED)
    for i, col in enumerate((YELLOW, ORANGE, RED)):
        c.rect(x - 4 - i * 2, y + 2 + i // 2, x - 2 - i * 2, y + 5 - i // 2, col)


def rocket_landed(c, x, y):
    """The same rocket standing on its fins, twenty-four pixels tall; x, y is the ground under its middle."""
    c.rect(x - 3, y - 20, x + 4, y - 4, WHITE); c.box(x - 3, y - 20, x + 4, y - 4, INK)
    for i in range(4):
        c.rect(x - 2 + i // 2, y - 21 - i, x + 3 - i // 2, y - 20 - i, RED)
    c.ellipse(x, y - 15, 1.6, 1.6, BLUE); c.rect(x - 1, y - 11, x + 2, y - 8, RED)
    c.line(x - 3, y - 6, x - 6, y, INK); c.line(x + 3, y - 6, x + 6, y, INK); c.rect(x - 6, y - 4, x - 3, y - 1, RED); c.rect(x + 4, y - 4, x + 7, y - 1, RED)


def kit(c, x, y, facing=1, scale=1, arm=0):
    """Kit, a cadet in a red suit and a glass helmet, sixteen pixels tall at scale one; x, y is the ground between her feet."""
    s = scale
    def r(x0, y0, x1, y1, col):
        c.rect(x + x0 * s, y + y0 * s, x + x1 * s, y + y1 * s, col)
    r(-2, -5, 0, 0, (150, 30, 24)); r(1, -5, 3, 0, (150, 30, 24)); r(-3, 0, 0, 1, INK) if facing < 0 else r(-2, 0, 0, 1, INK); r(1, 0, 4 if facing > 0 else 3, 1, INK)
    r(-2, -11, 3, -5, RED); r(-2, -7, 3, -6, YELLOW)
    r(-3 if facing > 0 else 3, -10, -2 if facing > 0 else 4, -6, GREY)          # the air tank, behind her
    if arm:                                                                       # an arm out, pointing or reaching
        r(3 if facing > 0 else -5, -10, 5 if facing > 0 else -2, -9, RED); r(5 if facing > 0 else -6, -10, 6 if facing > 0 else -5, -9, WHITE)
    else:
        r(3 if facing > 0 else -3, -10, 4 if facing > 0 else -2, -6, RED)
    c.ellipse(x + .5 * s, y - 14 * s, 3 * s, 3 * s, WHITE); c.ellipse(x + .5 * s, y - 14 * s, 2.2 * s, 2.2 * s, SKIN)
    r(1 if facing > 0 else -1, -15, 2 if facing > 0 else 0, -14, INK); r(-2, -17, 3, -16, (120, 70, 40))
    c.rect(x - 2 * s, y - 11 * s, x + 3 * s, y - 11 * s + 1, WHITE)


def caboose(c, x, y, facing=1, scale=1, lamp=True):
    """Caboose, a squat robot on treads, eleven wide and twelve tall; x, y is the ground under his middle."""
    s = scale
    def r(x0, y0, x1, y1, col):
        c.rect(x + x0 * s, y + y0 * s, x + x1 * s, y + y1 * s, col)
    r(-5, -3, 6, 0, INK); r(-4, -2, 5, -1, DARK)
    for k in (-3, 0, 3):
        r(k, -2, k + 1, -1, GREY)
    r(-4, -9, 5, -3, RUST); r(-4, -6, 5, -5, (120, 40, 26)); r(-4, -9, 5, -8, ORANGE)
    r(-1, -5, 2, -4, YELLOW)
    r(-2, -12, 3, -9, GREY); r(-2, -12, 3, -11, WHITE)
    eye = 1 if facing > 0 else -1
    r(eye, -11, eye + 1, -10, GLOW if lamp else INK)
    r(0, -14, 1, -12, INK); r(0, -15, 1, -14, RED)
    r(5 if facing > 0 else -6, -7, 7 if facing > 0 else -4, -6, GREY)


def burst(c, cx, cy, rx, ry, col):
    """A jagged burst, for a blurb or a noise."""
    for k in range(16):
        import math
        a = k * math.pi / 8; r = 1.0 if k % 2 == 0 else .72
        c.line(cx, cy, int(cx + math.cos(a) * rx * r), int(cy + math.sin(a) * ry * r), col)
    c.ellipse(cx, cy, rx * .7, ry * .7, col)


# ------------------------------------------------------------------------------------------------ the pages
def cover():
    c = Canvas(colour=SKY)
    stars(c, (0, 0, W, 120), 70, 3101)
    # the masthead
    c.rect(0, 0, W, 34, RED); c.rect(0, 34, W, 36, YELLOW)
    c.text(37, 5, 'KIT &', INK, 3); c.text(36, 4, 'KIT &', YELLOW, 3)
    c.text(10, 21, 'CABOOSE', INK, 3); c.text(9, 20, 'CABOOSE', YELLOW, 3)
    c.rect(2, 2, 28, 17, WHITE); c.box(2, 2, 28, 17, INK); c.text(5, 4, 'NO.31', INK); c.text(8, 11, '35C', RED)
    earth(c, 104, 56, 9)
    # the crater: its far rim, the light coming up out of it, and the near rim they stand on
    c.rect(0, 118, W, H, GREY); c.rect(0, 118, W, 119, WHITE)
    for k in range(9):
        c.line(64, 150, 20 + k * 11, 60, GLOW if k % 2 else YELLOW)
    c.ellipse(64, 150, 44, 16, INK); c.ellipse(64, 152, 36, 11, (70, 60, 20)); c.ellipse(64, 154, 24, 7, YELLOW); c.ellipse(64, 155, 12, 4, GLOW)
    c.halftone(0, 166, W, H, (130, 130, 126))
    kit(c, 30, 148, 1, 2, arm=1); caboose(c, 100, 150, -1, 2)
    burst(c, 64, 180, 50, 10, YELLOW); c.text(64 - width("WHAT'S DOWN THERE?") // 2, 178, "WHAT'S DOWN THERE?", RED)
    return c.im


def page1():
    c = Canvas(); c.text(4, 3, 'THE LIGHT IN THE CRATER', RED)
    p = panel(c, 3, 11, 125, 96, SKY); stars(c, p, 60, 3111); earth(c, 104, 28, 7)
    ground(c, p, 76, 3112); rocket_flying(c, 40, 46)
    caption(c, 5, 13, 98, 'LUNAR MAIL RUN 31.')
    balloon(c, 8, 28, "WE'RE EARLY!", (50, 46), 40); balloon(c, 62, 60, 'WE ARE LOST.', (58, 52), 44, robot=True)
    p = panel(c, 3, 100, 125, 188, (60, 70, 96))
    c.rect(p[0], 140, p[2], p[3], (44, 50, 70)); c.rect(40, 108, 90, 136, SKY); c.box(40, 108, 90, 136, GREY); stars(c, (41, 109, 89, 135), 14, 3113)
    c.rect(20, 150, 60, 152, GREY); c.line(34, 150, 30, 140, GREY); c.ellipse(30, 139, 2, 2, RED)
    kit(c, 36, 186, 1, 2); caboose(c, 92, 186, -1, 2)
    c.rect(72, 160, 84, 170, WHITE); c.box(72, 160, 84, 170, INK); c.line(74, 163, 82, 163, BLUE); c.line(74, 166, 80, 166, BLUE)
    caption(c, 5, 102, 118, 'KIT HAS THE STICK. CABOOSE HAS THE MAP.')
    balloon(c, 8, 122, 'SAME THING!', (36, 150), 40)
    return c.im


def page2():
    c = Canvas()
    p = panel(c, 3, 3, 125, 70, SKY); stars(c, p, 40, 3121); ground(c, p, 44, 3122, 2)
    c.ellipse(70, 56, 26, 7, INK, p); c.ellipse(70, 57, 3, 1.5, GLOW, p)
    for k in range(5):
        c.line(70, 56, 58 + k * 6, 30, YELLOW)
    balloon(c, 6, 6, 'A LIGHT. NOTHING ON THE MAP.', (20, 40), 60, robot=True)
    p = panel(c, 3, 74, 62, 128, (60, 70, 96)); kit(c, 32, 126, 1, 2, arm=1)
    balloon(c, 5, 76, 'THEN THE MAP IS WRONG.', (30, 96), 48)
    p = panel(c, 66, 74, 125, 128, SKY); stars(c, p, 16, 3123); ground(c, p, 110, 3124, 1); rocket_landed(c, 96, 112)
    p = panel(c, 3, 132, 125, 188, SKY); stars(c, p, 30, 3125); ground(c, p, 160, 3126, 2)
    rocket_landed(c, 22, 164); kit(c, 60, 176, 1, 1); caboose(c, 80, 178, 1, 1)
    c.ellipse(112, 172, 16, 5, INK, p); c.line(112, 170, 112, 150, YELLOW); c.line(108, 170, 104, 152, YELLOW)
    caption(c, 5, 134, 80, 'THEY SET DOWN ON THE RIM.')
    return c.im


def footprints(c, x, y, n, dx, dy, clip):
    for k in range(n):
        c.ellipse(x + k * dx + (1 if k % 2 else -1), y + k * dy, 1.6, 1, DARK, clip)


def page3():
    c = Canvas()
    p = panel(c, 3, 3, 125, 76, SKY); stars(c, p, 30, 3131); ground(c, p, 30, 3132, 0)
    footprints(c, 30, 40, 9, 9, 4, p); kit(c, 20, 44, 1, 1, arm=1); caboose(c, 40, 38, 1, 1)
    caption(c, 5, 5, 50, 'FOOTPRINTS.')
    p = panel(c, 3, 80, 125, 134, (60, 70, 96)); caboose(c, 28, 132, 1, 2); kit(c, 96, 132, -1, 2)
    balloon(c, 6, 82, 'COUNT THEM.', (26, 104), 40, robot=True); balloon(c, 62, 92, 'ONE SET. GOING DOWN.', (92, 104), 52)
    p = panel(c, 3, 138, 125, 188, GREY); c.halftone(p[0], p[1], p[2], p[3], (134, 134, 130), p)
    for k in range(5):
        c.ellipse(20 + k * 22, 152 + (k % 2) * 14, 5, 3, DARK, p); c.ellipse(20 + k * 22, 151 + (k % 2) * 14, 3.5, 1.6, (60, 60, 66), p)
    caption(c, 46, 174, 76, 'NONE COMING BACK.')
    return c.im


def wreck(c, x, y, clip):
    """An old mail rocket half in the dust, tipped over, its beacon still going."""
    c.ellipse(x + 14, y + 2, 20, 4, DARK, clip)
    for k in range(22):
        c.rect(x + k, y - 9 + k // 5, x + k + 1, y - 2 + k // 5, (196, 190, 170))
    c.line(x, y - 9, x + 21, y - 5, INK); c.line(x, y - 3, x + 21, y + 1, INK); c.line(x, y - 9, x, y - 3, INK)
    c.rect(x + 22, y - 5, x + 27, y, RUST); c.line(x + 27, y - 5, x + 31, y - 2, INK); c.line(x + 27, y, x + 31, y - 2, INK)
    c.rect(x + 8, y - 12, x + 9, y - 8, INK); c.ellipse(x + 8, y - 13, 1.5, 1.5, GLOW)
    c.rect(x + 12, y - 5, x + 16, y - 2, INK)


def page4():
    c = Canvas()
    p = panel(c, 3, 3, 125, 72, SKY); stars(c, p, 30, 3141); ground(c, p, 46, 3142, 1); wreck(c, 44, 58, p)
    for k in range(3):
        c.line(52, 44, 44 + k * 8, 30, YELLOW)
    kit(c, 14, 66, 1, 1); caboose(c, 104, 66, -1, 1)
    caption(c, 5, 5, 72, 'A MAIL ROCKET. OLD.')
    p = panel(c, 3, 76, 125, 130, (196, 190, 170)); c.halftone(p[0], p[1], p[2], p[3], (180, 174, 154), p)
    c.text(10, 106, 'RUN 7', INK, 3); c.line(8, 124, 78, 124, RUST); caboose(c, 104, 128, -1, 2)
    balloon(c, 36, 78, 'RUN 7. IT NEVER CAME IN.', (100, 100), 84, robot=True)
    p = panel(c, 3, 134, 125, 188, (60, 50, 44))
    for k, (bx, by) in enumerate(((20, 180), (44, 184), (70, 178), (30, 166))):
        c.ellipse(bx, by, 11, 6, (150, 130, 96), p); c.line(bx - 6, by - 4, bx + 6, by - 4, INK); c.text(bx - 8, by - 2, 'MAIL', INK)
    kit(c, 104, 186, -1, 2, arm=1); c.rect(86, 160, 94, 166, WHITE); c.box(86, 160, 94, 166, INK); c.dot(92, 161, RED)
    caption(c, 5, 136, 84, 'THIRTY YEARS OF MAIL.')
    return c.im


def page5():
    c = Canvas()
    p = panel(c, 3, 3, 125, 76, SKY); stars(c, p, 24, 3151); ground(c, p, 40, 3152, 0)
    c.ellipse(84, 58, 18, 8, INK, p)
    for k in range(7):
        c.line(84, 58, 60 + k * 8, 48 + (k % 2) * 3, INK)
    caboose(c, 84, 58, 1, 1); kit(c, 30, 62, 1, 1, arm=1)
    burst(c, 44, 24, 22, 12, YELLOW); c.text(27, 19, 'KRAK!', RED, 2)
    p = panel(c, 3, 80, 62, 188, INK)
    for k in range(6):
        c.rect(p[0], 84 + k * 16, p[0] + 6 + (k * 5) % 9, 92 + k * 16, (44, 40, 44)); c.rect(p[2] - 7 - (k * 7) % 8, 90 + k * 16, p[2], 98 + k * 16, (44, 40, 44))
    caboose(c, 32, 136, 1, 1); c.line(32, 100, 32, 120, DARK); c.line(28, 104, 28, 116, DARK); c.line(37, 106, 37, 118, DARK)
    c.ellipse(33, 170, 6, 2, (60, 54, 20), p)
    p = panel(c, 66, 80, 125, 130, (60, 70, 96)); kit(c, 96, 128, -1, 2, arm=1)
    balloon(c, 68, 82, 'CABOOSE!', (92, 100), 40)
    p = panel(c, 66, 134, 125, 188, INK); c.ellipse(96, 178, 3, 1.5, GLOW, p); c.dot(96, 176, YELLOW)
    balloon(c, 68, 138, 'I AM FINE. IT IS DEEP.', (94, 172), 48, robot=True)
    return c.im


def page6():
    c = Canvas()
    p = panel(c, 3, 3, 125, 66, SKY); stars(c, p, 20, 3161); ground(c, p, 36, 3162, 0)
    c.ellipse(84, 56, 26, 9, INK, p); kit(c, 42, 54, 1, 1, arm=1); c.line(48, 44, 70, 50, (220, 200, 150)); c.line(70, 50, 74, 62, (220, 200, 150))
    balloon(c, 6, 6, "THE ROPE'S TOO SHORT!", (40, 36), 62)
    p = panel(c, 3, 70, 125, 150, INK)
    for k in range(7):
        c.rect(p[0] + k * 17, p[1], p[0] + k * 17 + 9, p[1] + 6 + (k * 3) % 7, (44, 40, 44))
    # the door in the rock: riveted iron, and a line of light where it has come away from its frame
    c.rect(66, 86, 104, 146, (70, 74, 84)); c.box(66, 86, 104, 146, GREY)
    for ry in (92, 116, 140):
        for rx in (70, 84, 99):
            c.dot(rx, ry, WHITE)
    c.rect(104, 86, 107, 146, GLOW); c.rect(107, 96, 112, 146, YELLOW); c.halftone(112, 110, 122, 146, YELLOW, p)
    c.ellipse(52, 132, 3, 3, (90, 92, 100)); caboose(c, 30, 146, 1, 2)
    for k in range(4):
        c.line(40, 124, 62 + k * 2, 100 + k * 12, (120, 110, 60))
    balloon(c, 6, 72, 'KIT. THERE IS A DOOR DOWN HERE.', (30, 118), 56, robot=True)
    balloon(c, 60, 150 - 16, 'IT IS OPENING.', (40, 126), 54, robot=True)
    c.rect(3, 154, 125, 188, RED); c.box(3, 154, 125, 188, INK)
    c.text(64 - width('CONTINUED IN NO. 32!', 1) // 2, 159, 'CONTINUED IN NO. 32!', YELLOW)
    c.text(64 - width('WHO KNOCKS FROM') // 2, 170, 'WHO KNOCKS FROM', WHITE); c.text(64 - width('THE OTHER SIDE?') // 2, 178, 'THE OTHER SIDE?', WHITE)
    return c.im


def back():
    c = Canvas(colour=(240, 232, 208))
    c.rect(3, 3, 125, 62, YELLOW); c.box(3, 3, 125, 62, INK)
    c.text(8, 7, '100 TOY SPACEMEN', RED); c.text(8, 16, 'ONLY', INK); c.text(86, 13, '1.98', INK, 2)
    for row in range(3):
        for k in range(12):
            x, y = 9 + k * 9, 30 + row * 10
            c.rect(x, y, x + 3, y + 5, (60, 120, 80) if (k + row) % 3 else BLUE); c.ellipse(x + 1, y - 2, 1.5, 1.5, WHITE); c.rect(x - 1, y + 5, x + 4, y + 6, INK)
    c.rect(3, 66, 125, 112, (190, 220, 232)); c.box(3, 66, 125, 112, INK)
    c.text(8, 70, 'GROW MOON CRYSTALS!', BLUE); c.text(8, 78, 'IN A JAR. IN A WEEK.', INK)
    for k, col in enumerate((BLUE, WHITE, (150, 90, 190), GREEN, WHITE)):
        x = 18 + k * 21
        c.rect(x - 6, 104, x + 7, 110, GREY); c.line(x, 88 + k % 2 * 3, x - 5, 104, col); c.line(x, 88 + k % 2 * 3, x + 5, 104, col); c.line(x, 88 + k % 2 * 3, x, 104, col)
    # the coupon, and what somebody began to write on it in pencil
    for x in range(3, 125, 3):
        c.dot(x, 116, INK); c.dot(x, 188, INK)
    for y in range(116, 189, 3):
        c.dot(3, y, INK); c.dot(124, y, INK)
    c.text(8, 120, 'YES! SEND ME 12 ISSUES', RED); c.text(8, 128, 'OF KIT & CABOOSE. 4.00', RED)
    c.text(8, 142, 'NAME', INK); c.line(30, 147, 118, 147, INK); c.text(34, 140, 'PAT MADDOX', PENCIL)
    c.text(8, 156, 'ADDRESS', INK); c.line(44, 161, 118, 161, INK); c.text(48, 154, 'RT 2 BOX', PENCIL)
    c.text(8, 170, 'TOWN', INK); c.line(30, 175, 118, 175, INK)
    c.rect(100, 178, 121, 186, WHITE); c.box(100, 178, 121, 186, INK); c.text(103, 180, 'CUT', INK)
    return c.im


def pages():
    return [cover(), page1(), page2(), page3(), page4(), page5(), page6(), back()]


def atlas(images):
    out = Image.new('RGBA', (W * 4, H * 2), (0, 0, 0, 0))
    for i, im in enumerate(images):
        out.paste(im.convert('RGBA'), ((i % 4) * W, (i // 4) * H))
    return out


def bleached(im):
    """Sixteen years in a west window: the reds gone to buff, the blacks to grey-blue, everything paler."""
    out = im.copy().convert('RGBA'); px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            grey = (r * 30 + g * 50 + b * 20) // 100
            r2 = (r + grey * 2) // 3; g2 = (g + grey * 2) // 3; b2 = (b * 2 + grey + 40) // 3
            px[x, y] = (min(255, 96 + r2 * 150 // 255), min(255, 104 + g2 * 146 // 255), min(255, 112 + b2 * 140 // 255), a)
    return out


def item(aged):
    """The comic in the hand: its cover, small, with the masthead and the light in the crater."""
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    def rect(x0, y0, x1, y1, col):
        for y in range(y0, y1):
            for x in range(x0, x1):
                px[x, y] = col + (255,)
    rect(3, 1, 14, 15, SKY); rect(3, 1, 14, 5, RED); rect(3, 5, 14, 6, YELLOW)
    for x in (4, 6, 8, 10, 12):
        rect(x, 2, x + 1, 4, YELLOW)
    rect(3, 11, 14, 15, GREY); rect(6, 12, 11, 14, INK); rect(7, 12, 10, 13, YELLOW)
    # The light spreads as it rises out of the crater. Kit stands to one side of it, taller than Caboose at the other.
    rect(8, 8, 9, 12, GLOW); rect(7, 7, 8, 8, GLOW); rect(9, 7, 10, 8, GLOW); rect(6, 6, 7, 7, GLOW); rect(10, 6, 11, 7, GLOW); rect(8, 6, 9, 7, GLOW)
    rect(12, 7, 13, 8, WHITE)
    rect(4, 9, 5, 11, RED); rect(4, 8, 5, 9, SKIN); rect(11, 10, 13, 11, RUST); rect(12, 9, 13, 10, YELLOW)
    rect(2, 1, 3, 15, WHITE); rect(14, 2, 15, 15, (60, 56, 60))
    return bleached(im) if aged else im


def files():
    images = pages(); sheet = atlas(images)
    return {
        'textures/gui/borrasca_comic.png': sheet, 'textures/gui/borrasca_comic_aged.png': bleached(sheet),
        'textures/item/borrasca_comic.png': item(False), 'textures/item/borrasca_comic_aged.png': item(True),
    }


def data():
    ns = 'the_oldest_house:'
    return {
        'models/item/borrasca_comic.json': {'parent': 'minecraft:item/generated', 'textures': {'layer0': ns + 'item/borrasca_comic'},
                                             'overrides': [{'predicate': {'custom_model_data': 1}, 'model': ns + 'item/borrasca_comic_aged'}]},
        'models/item/borrasca_comic_aged.json': {'parent': 'minecraft:item/generated', 'textures': {'layer0': ns + 'item/borrasca_comic_aged'}},
    }


def main():
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); parser.add_argument('--preview')
    args = parser.parse_args()
    if args.preview:
        atlas(pages()).resize((W * 4 * 2, H * 2 * 2), Image.NEAREST).save(args.preview); return
    for rel, im in files().items():
        path = A / rel
        if args.check:
            with Image.open(path) as actual:
                assert actual.convert('RGBA').tobytes() == im.convert('RGBA').tobytes(), rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); im.save(path, optimize=True)
    for rel, obj in data().items():
        path = A / rel
        if args.check:
            assert json.loads(path.read_text()) == obj, rel
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_text(json.dumps(obj, indent=2) + '\n')
    lang_path = A / 'lang/en_us.json'; lang = json.loads(lang_path.read_text()); key = 'item.the_oldest_house.borrasca_comic'
    if args.check:
        assert lang.get(key) == 'Kit & Caboose No. 31', key
    else:
        lang[key] = 'Kit & Caboose No. 31'; lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + '\n')
    print(('Verified' if args.check else 'Drew') + ' Kit & Caboose No. 31: eight pages, twice.')


if __name__ == '__main__':
    main()
