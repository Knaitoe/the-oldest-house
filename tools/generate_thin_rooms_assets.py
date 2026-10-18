#!/usr/bin/env python3
"""The thin rooms (0.4.105): the Masque's seven chambers and Bly's manor, in vanilla-style 16x16 pixels.

One `masque` block (19 kinds, 7 colour stages) and one `manor` block (26 kinds, 4 stages): textures, block models and states,
and their names. Textures follow Minecraft's own look: a small palette per material, one-pixel bevels and seams, scattered
single-pixel noise, nothing smooth or sampled. Run from anywhere; it rewrites only its own files and keys.
"""
import json
import math
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
ASSETS = ROOT / 'assets/the_oldest_house'
MODELS = ASSETS / 'models/block'
NS = 'the_oldest_house:block/'


# ------------------------------------------------------------------------------------------------ painting
def clamp(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def mul(c, f):
    return clamp(tuple(v * f for v in c))


def mix(a, b, t):
    return clamp(tuple(x + (y - x) * t for x, y in zip(a, b)))


def canvas(color=(0, 0, 0, 0), h=16):
    return Image.new('RGBA', (16, h), color)


def px(img, x, y, c, a=255):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), clamp(c[:3]) + (a,))


def fill(img, x0, y0, x1, y1, c, a=255):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            px(img, x, y, c, a)


def noisy(base, seed, spread=(0, 0, 0, -.04, .04, -.07)):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.choice(spread)))
    return img


def bevel(img, light=1.12, dark=.8, x0=0, y0=0, x1=15, y1=15):
    """A one-pixel lit top-left edge and shaded bottom-right edge, as vanilla's carved blocks."""
    for x in range(x0, x1 + 1):
        r, g, b, a = img.getpixel((x, y0)); px(img, x, y0, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x, y1)); px(img, x, y1, mul((r, g, b), dark), a)
    for y in range(y0 + 1, y1):
        r, g, b, a = img.getpixel((x0, y)); px(img, x0, y, mul((r, g, b), light), a)
        r, g, b, a = img.getpixel((x1, y)); px(img, x1, y, mul((r, g, b), dark), a)
    return img


# ---- the Masque
COLOURS = [('blue', (44, 64, 168)), ('purple', (106, 40, 132)), ('green', (40, 118, 62)), ('orange', (204, 112, 34)),
           ('white', (224, 220, 212)), ('violet', (138, 82, 186)), ('black', (26, 24, 30))]
PANES = [(70, 100, 230), (140, 60, 180), (60, 170, 90), (240, 140, 40), (236, 236, 240), (170, 110, 220), (200, 24, 28)]
GOLD, GOLD_DARK = (214, 172, 70), (150, 108, 36)
EBONY = (34, 28, 30)


def velvet(base, seed):
    """Hangings fall in folds: a lit ridge and a shadowed hollow every four pixels, with the nap's specks."""
    rng = random.Random(seed)
    img = canvas()
    shade = [1.14, 1.0, .82, .9]
    for x in range(16):
        f = shade[(x + (x // 8)) % 4]
        for y in range(16):
            px(img, x, y, mul(base, f + rng.choice((0, 0, 0, .03, -.03, -.05))))
    for y in (0, 1):  # gathered at the rod
        for x in range(16):
            if (x + y) % 2 == 0:
                r, g, b, a = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .86))
    return img


def parquet():
    """Polished herringbone in two woods: short boards in pairs, laid at right angles."""
    rng = random.Random(1051)
    img = canvas()
    a, b = (104, 66, 38), (82, 50, 28)
    for y in range(16):
        for x in range(16):
            cell = ((x // 4) + (y // 4)) % 2
            c = a if cell == 0 else b
            seam = (x % 4 == 0) if cell == 0 else (y % 4 == 0)
            px(img, x, y, mul(c, .7 if seam else 1 + rng.choice((0, 0, .04, -.04))))
    for _ in range(5):
        px(img, rng.randrange(16), rng.randrange(16), (150, 104, 62))  # the polish catching the light
    return img


def coffer():
    """A coffered ceiling: a dark recessed square in an ebony grid with a gilt boss at its centre."""
    img = noisy(EBONY, 1052)
    fill(img, 2, 2, 13, 13, (48, 30, 36))
    fill(img, 3, 3, 12, 12, (60, 36, 44))
    for i in range(2, 14):
        px(img, i, 2, (22, 16, 18)); px(img, 2, i, (22, 16, 18))
        px(img, i, 13, (84, 56, 62)); px(img, 13, i, (84, 56, 62))
    fill(img, 7, 7, 8, 8, GOLD)
    px(img, 7, 7, (250, 220, 130)); px(img, 6, 7, GOLD_DARK); px(img, 9, 8, GOLD_DARK); px(img, 7, 6, GOLD_DARK); px(img, 8, 9, GOLD_DARK)
    return img


def ebony():
    rng = random.Random(1053)
    img = canvas()
    for x in range(16):
        grain = rng.choice((1.0, 1.0, .85, 1.15))
        for y in range(16):
            px(img, x, y, mul(EBONY, grain + rng.choice((0, 0, .05, -.05))))
    for _ in range(4):
        x, y = rng.randrange(16), rng.randrange(16)
        px(img, x, y, (70, 60, 64))
    return img


def cornice():
    """A gilt egg-and-dart band between ebony fillets."""
    img = ebony()
    fill(img, 0, 4, 15, 11, GOLD)
    for x in range(16):
        px(img, x, 4, (248, 214, 120)); px(img, x, 11, GOLD_DARK); px(img, x, 3, (20, 16, 18)); px(img, x, 12, (20, 16, 18))
    for x in range(0, 16, 4):
        fill(img, x + 1, 6, x + 2, 9, (176, 132, 48))  # the eggs
        px(img, x + 1, 6, (240, 204, 110))
        px(img, x + 3, 7, GOLD_DARK); px(img, x + 3, 8, GOLD_DARK)  # the darts
    return img


def tracery():
    """The window surround: grey ashlar with fine joints."""
    rng = random.Random(1054)
    img = canvas()
    base = (128, 124, 120)
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.choice((0, 0, 0, .05, -.05, -.08))))
    for x in range(16):
        px(img, x, 7, mul(base, .72)); px(img, x, 15, mul(base, .72))
    for y in range(7):
        px(img, 9, y, mul(base, .72))
    for y in range(8, 15):
        px(img, 3, y, mul(base, .72))
    return img


def leaded(pane, seed, arch=False):
    """Diamond quarries in lead cames; light comes through the glass, not the lead. An arch keeps stone outside its point."""
    rng = random.Random(seed)
    img = canvas()
    lead = (46, 44, 48)
    stone = tracery()
    for y in range(16):
        for x in range(16):
            if arch:
                # A pointed (equilateral) arch: two arcs of radius 16 struck from the opposite springing points.
                inside = (x + .5 - 0) ** 2 + (16 - y - .5) ** 2 <= 16 ** 2 and (x + .5 - 16) ** 2 + (16 - y - .5) ** 2 <= 16 ** 2
                edge = inside and not ((x + .5 - 0) ** 2 + (16 - y - .5) ** 2 <= 14.6 ** 2 and (x + .5 - 16) ** 2 + (16 - y - .5) ** 2 <= 14.6 ** 2)
                if not inside:
                    r, g, b, a = stone.getpixel((x, y)); px(img, x, y, (r, g, b)); continue
                if edge:
                    px(img, x, y, lead); continue
            if (x + y) % 6 == 0 or (x - y) % 6 == 0:
                px(img, x, y, lead)
            else:
                f = 1 + rng.choice((0, 0, .08, -.06))
                px(img, x, y, mul(pane, f), 150 if sum(pane) < 600 else 120)
    if not arch:
        for y in range(16):
            px(img, 0, y, lead); px(img, 15, y, lead)
    else:
        for y in range(16):
            for x in (0, 15):
                r, g, b, a = img.getpixel((x, y))
                if a < 255:
                    px(img, x, y, lead)
    return img


def iron(seed=1055):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((58, 56, 60), 1 + rng.choice((0, 0, .08, -.08, .14))))
    return img


def embers():
    rng = random.Random(1056)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, rng.choice(((60, 20, 10), (120, 40, 12), (220, 90, 20), (250, 170, 40), (40, 30, 30))))
    return img


def clock_base():
    """The clock's plinth: a carved ebony panel with a gilt bead round it."""
    img = ebony()
    for i in range(2, 14):
        px(img, i, 2, GOLD); px(img, i, 13, GOLD_DARK); px(img, 2, i, GOLD); px(img, 13, i, GOLD_DARK)
    fill(img, 4, 4, 11, 11, (44, 36, 40))
    bevel(img, 1.25, .7, 4, 4, 11, 11)
    fill(img, 7, 6, 8, 9, (60, 50, 56))
    return img


def pendulum_frame(angle):
    """One frame of the case door: the bob on its rod behind the glass, swung by ``angle`` radians."""
    img = ebony()
    fill(img, 3, 1, 12, 14, (16, 14, 20))  # the dark inside of the case through the glass
    for x in range(3, 13):
        px(img, x, 1, GOLD_DARK); px(img, x, 14, GOLD_DARK)
    for y in range(1, 15):
        px(img, 3, y, GOLD_DARK); px(img, 12, y, GOLD_DARK)
    pivot = (7.5, 1.5)
    for t in range(0, 9):
        x = pivot[0] + math.sin(angle) * t
        y = pivot[1] + math.cos(angle) * t
        px(img, int(round(x)), int(round(y)), (190, 150, 70))
    bx = pivot[0] + math.sin(angle) * 10
    by = pivot[1] + math.cos(angle) * 10
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            if abs(dx) + abs(dy) < 2:
                px(img, int(round(bx)) + dx, int(round(by)) + dy, GOLD if dy <= 0 else GOLD_DARK)
    px(img, int(round(bx)) - 1, int(round(by)) - 1, (250, 222, 130))
    for y in range(2, 14, 3):  # the reflection on the glass
        px(img, 10, y, (60, 56, 72))
    return img


def clock_case(running):
    if not running:
        return pendulum_frame(0.0)
    frames = [pendulum_frame(.32 * math.sin(i / 16 * 2 * math.pi)) for i in range(16)]
    strip = Image.new('RGBA', (16, 16 * len(frames)))
    for i, f in enumerate(frames):
        strip.paste(f, (0, 16 * i))
    return strip


def clock_face(stopped):
    """An ivory dial in a gilt ring. Running, it is three minutes to the hour; stopped, both hands stand at twelve."""
    img = ebony()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 8, y + .5 - 8)
            if d <= 7.2:
                px(img, x, y, GOLD if d > 6.2 else (226, 216, 190))
    for i in range(12):
        a = i / 12 * 2 * math.pi
        px(img, int(round(8 - .5 + math.sin(a) * 5)), int(round(8 - .5 - math.cos(a) * 5)), (30, 26, 28))
    hands = [(0.0, 4), (0.0, 3)] if stopped else [(-3 / 60 * 2 * math.pi, 4), (-0.5 / 12 * 2 * math.pi, 3)]
    for a, n in hands:
        for t in range(1, n + 1):
            px(img, int(round(7.5 + math.sin(a) * t)), int(round(7.5 - math.cos(a) * t)), (20, 16, 18))
    px(img, 7, 7, (20, 16, 18)); px(img, 8, 8, (120, 90, 40))
    return img


def gilt():
    rng = random.Random(1057)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(GOLD, 1 + rng.choice((0, 0, .1, -.1, -.18))))
    for x in range(16):
        px(img, x, 0, (250, 224, 140))
    return img


def music():
    """A brass desk holding a page of staves."""
    img = canvas()
    fill(img, 0, 0, 15, 15, (180, 140, 64))
    fill(img, 2, 2, 13, 13, (232, 226, 206))
    for y in (4, 8, 12):
        for x in range(2, 14):
            px(img, x, y, (90, 84, 80))
            if (x * 7 + y) % 5 == 0:
                px(img, x, y - 1, (30, 28, 30))
    return img


def mask():
    """A white Venetian mask with gold trim, its eyes cut out; transparent round it."""
    img = canvas()
    white, shade = (236, 232, 226), (196, 190, 184)
    rows = {3: (4, 11), 4: (2, 13), 5: (1, 14), 6: (1, 14), 7: (1, 14), 8: (2, 13), 9: (3, 12), 10: (5, 10), 11: (6, 9)}
    for y, (x0, x1) in rows.items():
        for x in range(x0, x1 + 1):
            px(img, x, y, white if x < 8 else shade)
        px(img, x0, y, GOLD); px(img, x1, y, GOLD_DARK)
    for x in range(4, 12):
        px(img, x, 3, GOLD)
    for ex in (4, 10):
        fill(img, ex, 6, ex + 1, 7, (0, 0, 0), 0)
        px(img, ex - 1, 6, GOLD); px(img, ex + 2, 7, GOLD)
    px(img, 7, 9, shade); px(img, 8, 9, shade)
    for x in (2, 13):  # the ribbons
        px(img, x, 9, (160, 30, 40)); px(img, x, 10, (160, 30, 40))
    return img


def invitation():
    """A cream card with a black border and a red seal."""
    img = canvas()
    fill(img, 0, 0, 15, 15, (236, 228, 204))
    for i in range(16):
        px(img, i, 0, (20, 18, 20)); px(img, i, 15, (20, 18, 20)); px(img, 0, i, (20, 18, 20)); px(img, 15, i, (20, 18, 20))
    for y in (4, 6, 8, 10):
        for x in range(3, 13):
            if (x + y * 3) % 7:
                px(img, x, y, (70, 64, 60))
    fill(img, 11, 11, 13, 13, (170, 24, 30))
    px(img, 11, 11, (220, 60, 60))
    return img


def silver():
    rng = random.Random(1058)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((196, 198, 204), 1 + rng.choice((0, 0, .06, -.08))))
    for i in range(16):
        px(img, i, i, (240, 242, 246))
    return img


# ---- Bly
def boards(seed=1059, base=(150, 112, 70)):
    """Worn oak floorboards: four boards, a seam under each, staggered ends, a scatter of knots."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        board = y // 4
        for x in range(16):
            f = 1.0 + rng.choice((0, 0, 0, -.05, .05))
            if y % 4 == 3:
                f = .7
            elif y % 4 == 0:
                f += .06
            if (x + board * 5) % 16 == 15 and y % 4 != 3:
                f = .8
            px(img, x, y, mul(base, f))
    for _ in range(4):
        x, y = rng.randrange(16), rng.randrange(16)
        if y % 4 != 3:
            px(img, x, y, mul(base, .75))
    return img


def wet(img, seed, width=(5, 10)):
    """The same narrow line, wet again: a dark streak down the middle with a puddle's glints. Seamless top to bottom."""
    rng = random.Random(seed)
    out = img.copy()
    for y in range(16):
        x0 = width[0] + (1 if (y // 3) % 2 else 0) - (1 if y in (5, 6, 12) else 0)
        x1 = width[1] - (1 if (y // 4) % 2 else 0) + (1 if y in (2, 9) else 0)
        for x in range(x0, x1 + 1):
            r, g, b, a = out.getpixel((x, y))
            px(out, x, y, mul((r, g, b), .58 if x in (x0, x1) else .5))
    for _ in range(3):
        x, y = rng.randint(width[0] + 1, width[1] - 1), rng.randrange(16)
        r, g, b, a = out.getpixel((x, y))
        px(out, x, y, mix((r, g, b), (150, 160, 170), .45))
    return out


def damask():
    """Green damask: a darker leaf-and-diamond figure on a mid green, tiling both ways."""
    rng = random.Random(1060)
    base, figure = (64, 92, 70), (42, 64, 50)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, 1 + rng.choice((0, 0, .03, -.03))))
    motif = ['......#.........', '.....###........', '....#.#.#.......', '...#..#..#......', '....#.#.#.......', '.....###........',
             '......#.........', '......#.........', '..............#.', '.............###', '#...........#.#.', '...........#..#.',
             '#...........#.#.', '.............###', '..............#.', '..............#.']
    for y, row in enumerate(motif):
        for x, ch in enumerate(row):
            if ch == '#':
                px(img, x, y, figure)
    for x, y in ((6, 3), (14, 11)):
        px(img, x, y, (96, 124, 92))
    return img


def wainscot():
    """Dark oak panelling: a raised panel inside a bevelled frame."""
    img = noisy((70, 46, 30), 1061)
    bevel(img, 1.2, .7)
    fill(img, 3, 3, 12, 12, (82, 54, 34))
    bevel(img, .75, 1.25, 3, 3, 12, 12)
    return img


def plaster(seed=1062, stain=False):
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((222, 216, 202), 1 + rng.choice((0, 0, 0, .02, -.03))))
    if stain:  # a faint damp bloom, darker at its uneven edge
        for y in range(16):
            for x in range(16):
                d = math.hypot(x - 10, (y - 6) * 1.3) + rng.choice((0, .4, -.4))
                if d <= 3.2:
                    r, g, b, a = img.getpixel((x, y)); px(img, x, y, mix((r, g, b), (200, 188, 160), .5 if d > 2.4 else .25))
    x, y = 2, 13
    for _ in range(6):  # a hairline crack
        px(img, x, y, (170, 164, 150)); x += 1; y -= rng.choice((0, 1))
    return img


def plaster_cornice():
    img = plaster(1063)
    for x in range(16):
        px(img, x, 0, (240, 236, 226)); px(img, x, 5, (180, 172, 160)); px(img, x, 6, (236, 230, 218)); px(img, x, 15, (170, 162, 150))
        if x % 3 == 0:
            px(img, x, 3, (190, 182, 168))
    return img


def flags(seed=1064):
    """Grey stone flags in two courses with mossy joints."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((118, 120, 116), 1 + rng.choice((0, 0, 0, .05, -.05, -.08))))
    for x in range(16):
        px(img, x, 7, (82, 90, 74)); px(img, x, 15, (82, 90, 74))
    for y in range(7):
        px(img, 10, y, (82, 90, 74))
    for y in range(8, 15):
        px(img, 4, y, (82, 90, 74))
    return img


def glazing():
    """Greenish glass in white-painted iron bars, four panes a block."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            if x in (0, 7, 8, 15) or y in (0, 7, 8, 15):
                px(img, x, y, (226, 228, 222) if (x in (0, 7) or y in (0, 7)) else (170, 174, 170))
            else:
                px(img, x, y, (150, 190, 180) if (x + y) % 9 else (210, 236, 228), 90)
    return img


def rug():
    """A faded Turkey rug: a red field, a blue lozenge and a cream border."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (150, 46, 40)
            if x in (0, 15) or y in (0, 15):
                c = (220, 200, 160)
            elif x in (1, 14) or y in (1, 14):
                c = (40, 54, 96)
            elif abs(x - 7.5) + abs(y - 7.5) < 4:
                c = (44, 60, 110) if abs(x - 7.5) + abs(y - 7.5) >= 2.5 else (210, 170, 70)
            px(img, x, y, mul(c, .92 if (x * 3 + y) % 5 == 0 else 1))
    return img


def frame_with(paint):
    """A gilt frame around a 12x13 painted canvas."""
    img = canvas()
    fill(img, 0, 0, 15, 15, (0, 0, 0), 0)
    fill(img, 2, 2, 13, 14, GOLD)
    for i in range(2, 14):
        px(img, i, 2, (250, 222, 130)); px(img, i, 14, GOLD_DARK)
    for i in range(2, 15):
        px(img, 2, i, (250, 222, 130)); px(img, 13, i, GOLD_DARK)
    paint(img)
    return img


def portrait(which):
    def lady(img):
        fill(img, 3, 3, 12, 13, (40, 44, 52))
        fill(img, 6, 4, 9, 7, (196, 186, 176))  # the face, wiped blank
        for x in range(6, 10):
            px(img, x, 5, (180, 172, 164)); px(img, x, 6, (188, 178, 170))
        fill(img, 5, 3, 10, 3, (30, 26, 24)); px(img, 5, 4, (30, 26, 24)); px(img, 10, 4, (30, 26, 24))
        fill(img, 4, 8, 11, 13, (20, 22, 30))  # the dark dress
        px(img, 7, 9, (90, 96, 110)); px(img, 8, 10, (90, 96, 110))
    def boy(img):
        fill(img, 3, 3, 12, 13, (90, 70, 50))
        fill(img, 6, 4, 9, 7, (232, 198, 168)); fill(img, 6, 3, 9, 3, (120, 80, 40))
        px(img, 6, 5, (40, 40, 60)); px(img, 9, 5, (40, 40, 60)); px(img, 7, 7, (180, 100, 100)); px(img, 8, 7, (180, 100, 100))
        fill(img, 5, 8, 10, 13, (40, 50, 90)); px(img, 7, 8, (236, 236, 236)); px(img, 8, 8, (236, 236, 236))
    def girl(img):
        fill(img, 3, 3, 12, 13, (60, 84, 70))
        fill(img, 6, 4, 9, 7, (238, 204, 176)); fill(img, 5, 3, 10, 3, (226, 188, 90)); px(img, 5, 4, (226, 188, 90)); px(img, 10, 4, (226, 188, 90))
        px(img, 5, 5, (226, 188, 90)); px(img, 10, 5, (226, 188, 90)); px(img, 6, 5, (60, 90, 160)); px(img, 9, 5, (60, 90, 160))
        fill(img, 5, 8, 10, 13, (236, 230, 220)); fill(img, 6, 9, 9, 9, (200, 120, 140))
    def master(img):
        fill(img, 3, 3, 12, 13, (60, 46, 40))
        fill(img, 6, 4, 9, 7, (220, 186, 156)); fill(img, 6, 3, 9, 3, (70, 60, 50)); fill(img, 6, 7, 9, 7, (90, 70, 60))
        px(img, 6, 5, (30, 30, 30)); px(img, 9, 5, (30, 30, 30))
        fill(img, 4, 8, 11, 13, (24, 22, 24)); px(img, 7, 8, (230, 230, 230)); px(img, 8, 8, (230, 230, 230)); px(img, 7, 9, (140, 30, 30))
    return frame_with([lady, boy, girl, master][which])


def brass():
    rng = random.Random(1065)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((196, 152, 70), 1 + rng.choice((0, 0, .1, -.1, -.16))))
    return img


def wax():
    img = noisy((236, 228, 206), 1066, (0, 0, -.03, .02))
    for x in range(16):
        px(img, x, 0, (250, 246, 232))
    return img


def flame():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5) / 8 + (15 - y) / 16 * .2
            c = (255, 244, 180) if d < .25 else (250, 190, 70) if d < .55 else (230, 110, 30)
            px(img, x, y, c)
    return img


def linen():
    """A cupboard shelf of folded sheets: stacks of cream and white between dark boards."""
    img = noisy((60, 40, 26), 1067)
    for top, bottom in ((1, 6), (9, 14)):
        x = 1
        while x < 15:
            w = 4 if x < 11 else 15 - x
            for y in range(top, bottom + 1):
                for xx in range(x, x + w):
                    c = (236, 232, 222) if (y - top) % 2 == 0 else (208, 202, 190)
                    if (xx + y) % 11 == 0:
                        c = (196, 210, 222)
                    px(img, xx, y, c)
            x += w + 1
    for x in range(16):
        px(img, x, 7, (90, 62, 40)); px(img, x, 15, (90, 62, 40)); px(img, x, 8, (40, 26, 18))
    return img


def bench_wood():
    """A potting bench's weathered grey boards, split and earth-stained."""
    img = boards(1068, (126, 112, 94))
    rng = random.Random(1069)
    for _ in range(9):
        px(img, rng.randrange(16), rng.randrange(16), (90, 72, 54))
    return img


def note():
    """The gardener's note, folded once and damp: a soft grey bloom over faded ink."""
    img = canvas()
    fill(img, 0, 0, 15, 15, (226, 218, 196))
    for x in range(16):
        px(img, x, 7, (196, 188, 168))
    for y in (2, 4, 9, 11, 13):
        for x in range(2, 14):
            if (x + y) % 5:
                px(img, x, y, (96, 92, 100))
    for y in range(16):
        for x in range(16):
            if math.hypot(x - 11, y - 11) < 3.5:
                r, g, b, a = img.getpixel((x, y)); px(img, x, y, mix((r, g, b), (150, 150, 140), .4))
    return img


def dollhouse(part):
    img = canvas()
    if part == 'front':
        fill(img, 0, 0, 15, 15, (228, 222, 206))
        for x, y in ((2, 3), (11, 3), (2, 9), (11, 9)):
            fill(img, x, y, x + 2, y + 2, (40, 52, 70)); px(img, x + 1, y, (240, 240, 236)); px(img, x, y + 1, (240, 240, 236))
        fill(img, 6, 9, 9, 15, (120, 60, 40)); px(img, 8, 12, GOLD)
        for x in range(16):
            px(img, x, 7, (180, 170, 150))
        fill(img, 6, 2, 9, 4, (40, 52, 70))
        px(img, 7, 3, (230, 230, 220))  # a small face at the attic window
    elif part == 'side':
        fill(img, 0, 0, 15, 15, (216, 210, 194))
        fill(img, 5, 4, 10, 8, (40, 52, 70))
    else:
        for y in range(16):
            for x in range(16):
                px(img, x, y, mul((110, 56, 46), .8 if y % 3 == 2 else 1.0 + (.06 if (x + (y // 3) * 2) % 4 == 0 else 0)))
    return img


def blackboard():
    rng = random.Random(1070)
    img = canvas()
    fill(img, 0, 0, 15, 15, (110, 76, 46))
    for y in range(1, 15):
        for x in range(1, 15):
            px(img, x, y, mul((44, 52, 48), 1 + rng.choice((0, 0, .04, -.04))))
    for x in range(3, 13):  # a sum, and something rubbed out
        if x not in (6, 9):
            px(img, x, 4, (220, 220, 210))
    for x in range(3, 9):
        px(img, x, 7, (220, 220, 210))
    for x in range(4, 12):
        px(img, x, 11, (120, 128, 122))
    return img


def desk_wood():
    return boards(1071, (132, 96, 60))


def globe():
    rng = random.Random(1072)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (66, 104, 150) if rng.random() > .3 or (x + y) % 5 else (200, 190, 150))
    for x, y in ((3, 4), (4, 4), (4, 5), (5, 5), (10, 9), (11, 9), (11, 10), (12, 10), (7, 12), (8, 12)):
        px(img, x, y, (120, 150, 80))
    for x in range(16):
        px(img, x, 8, (180, 170, 140))
    return img


def manor_clock(part):
    """The hall clock: a long oak case with a brass bob, and an enamelled hood dial."""
    if part == 'case':
        img = boards(1073, (110, 70, 40))
        fill(img, 4, 2, 11, 13, (26, 20, 18))
        px(img, 7, 3, (190, 150, 70)); px(img, 7, 4, (190, 150, 70)); px(img, 7, 5, (190, 150, 70)); px(img, 8, 6, (190, 150, 70))
        fill(img, 7, 9, 8, 10, GOLD)
        return img
    img = boards(1074, (110, 70, 40))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x + .5 - 8, y + .5 - 8)
            if d <= 6.5:
                px(img, x, y, GOLD if d > 5.6 else (236, 230, 214))
    for t in range(1, 5):
        px(img, int(round(7.5 + t * .7)), int(round(7.5 + t * .7)), (20, 18, 20))  # stopped at twenty past three
    for t in range(1, 3):
        px(img, 7 + t, 7, (20, 18, 20))
    return img


def doormat():
    rng = random.Random(1075)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((150, 110, 62), 1 + rng.choice((0, 0, .1, -.1))))
    for y in range(16):
        for x in range(16):
            if math.hypot(x - 8, y - 7) < 5:
                r, g, b, a = img.getpixel((x, y)); px(img, x, y, mix((r, g, b), (40, 36, 34), .55))
    return img


def cloth():
    return noisy((30, 30, 40), 1076)


def marble():
    rng = random.Random(1077)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((226, 224, 220), 1 + rng.choice((0, 0, 0, .02, -.03))))
    x, y = 0, 3
    for _ in range(20):
        px(img, x % 16, y % 16, (160, 160, 168)); x += 1; y += rng.choice((0, 0, 1, -1))
    return img


def lamp_glass(lit):
    """The old man's lamp: a box of four panes in a black iron frame, warm when lit and smoked dark when not."""
    img = canvas()
    fill(img, 0, 0, 15, 15, (36, 34, 36))
    glow = (250, 210, 120) if lit else (70, 64, 56)
    for y in range(2, 14):
        for x in range(2, 14):
            c = glow if (x not in (7, 8)) else (36, 34, 36)
            if lit and 5 <= x <= 10 and 6 <= y <= 11 and x not in (7, 8):
                c = (255, 244, 200)
            px(img, x, y, c)
    if not lit:
        for x, y in ((3, 3), (10, 4), (4, 11)):
            px(img, x, y, (96, 90, 80))
    return img


def iron_cap():
    return iron(1078)


TEXTURES = {
    **{f'masque/velvet_{n}': (lambda c=c, i=i: velvet(c, 1000 + i)) for i, (n, c) in enumerate(COLOURS)},
    **{f'masque/glass_{n}': (lambda p=PANES[i], i=i: leaded(p, 1100 + i)) for i, (n, _) in enumerate(COLOURS)},
    **{f'masque/arch_{n}': (lambda p=PANES[i], i=i: leaded(p, 1100 + i, True)) for i, (n, _) in enumerate(COLOURS)},
    'masque/parquet': parquet, 'masque/coffer': coffer, 'masque/ebony': ebony, 'masque/cornice': cornice, 'masque/tracery': tracery,
    'masque/iron': iron, 'masque/embers': embers, 'masque/clock_base': clock_base,
    'masque/clock_case': lambda: clock_case(True), 'masque/clock_case_stopped': lambda: clock_case(False),
    'masque/clock_face': lambda: clock_face(False), 'masque/clock_face_stopped': lambda: clock_face(True),
    'masque/gilt': gilt, 'masque/music': music, 'masque/mask': mask, 'masque/invitation': invitation, 'masque/silver': silver,
    'manor/boards': boards, 'manor/wet_boards': lambda: wet(boards(), 1080), 'manor/damask': damask, 'manor/wainscot': wainscot,
    'manor/cornice': plaster_cornice, 'manor/plaster': lambda: plaster(1062, True), 'manor/flags': flags,
    'manor/wet_flags': lambda: wet(flags(), 1081, (4, 11)), 'manor/glazing': glazing, 'manor/rug': rug,
    **{f'manor/portrait_{i}': (lambda i=i: portrait(i)) for i in range(4)},
    'manor/brass': brass, 'manor/wax': wax, 'manor/flame': flame, 'manor/linen': linen, 'manor/bench': bench_wood, 'manor/note': note,
    'manor/dollhouse_front': lambda: dollhouse('front'), 'manor/dollhouse_side': lambda: dollhouse('side'), 'manor/dollhouse_roof': lambda: dollhouse('roof'),
    'manor/blackboard': blackboard, 'manor/desk': desk_wood, 'manor/globe': globe,
    'manor/clock_case': lambda: manor_clock('case'), 'manor/clock_face': lambda: manor_clock('face'),
    'manor/doormat': doormat, 'manor/cloth': cloth, 'manor/marble': marble, 'manor/iron': iron_cap,
    'manor/lamp_off': lambda: lamp_glass(False), 'manor/lamp_on': lambda: lamp_glass(True),
}
ANIMATED = {'masque/clock_case': 3}


# ------------------------------------------------------------------------------------------------ models
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def box(frm, to, tex, faces_=DIRS, overrides=None, cull=None, rot=None, glow=False, shade=True):
    faces = {}
    for d in faces_:
        face = {'texture': '#' + tex}
        if cull and d in cull:
            face['cullface'] = d
        faces[d] = face
    for d, t in (overrides or {}).items():
        if t is None:
            faces.pop(d, None)
        else:
            faces[d] = {**faces.get(d, {}), 'texture': '#' + t}
    el = {'from': list(frm), 'to': list(to), 'faces': faces}
    if rot:
        el['rotation'] = rot
    if glow:
        el['neoforge_data'] = {'block_light': 15, 'sky_light': 15}
    if not shade:
        el['shade'] = False
    return el


def write_model(prefix, name, elements, textures, particle, render='cutout'):
    tex = {k: (v if ':' in v else NS + v) for k, v in textures.items()}
    tex['particle'] = particle if ':' in particle else NS + particle
    data = {'parent': 'minecraft:block/block', 'render_type': f'minecraft:{render}', 'textures': tex, 'elements': elements}
    (MODELS / f'{prefix}_{name}.json').write_text(json.dumps(data, indent=1) + '\n')


def cube(prefix, name, all_=None, side=None, top=None, bottom=None):
    side = side or all_
    top = top or all_ or side
    bottom = bottom or all_ or side
    data = {'parent': 'minecraft:block/cube', 'textures': {'particle': NS + side, 'north': NS + side, 'south': NS + side, 'east': NS + side,
                                                            'west': NS + side, 'up': NS + top, 'down': NS + bottom}}
    (MODELS / f'{prefix}_{name}.json').write_text(json.dumps(data, indent=1) + '\n')


def masque_models():
    m = lambda name, els, tex, particle, render='cutout': write_model('masque', name, els, tex, particle, render)
    for i, (n, _) in enumerate(COLOURS):
        cube('masque', f'velvet_{i}', f'masque/velvet_{n}')
        side = ('east', 'west', 'up', 'down')
        m(f'lancet_{i}', [box((0, 0, 6), (16, 16, 10), 'g', overrides={d: 't' for d in side}, cull=side)],
          {'g': f'masque/glass_{n}', 't': 'masque/tracery'}, f'masque/glass_{n}', 'translucent')
        m(f'lancet_arch_{i}', [box((0, 0, 6), (16, 16, 10), 'g', overrides={d: 't' for d in side}, cull=side)],
          {'g': f'masque/arch_{n}', 't': 'masque/tracery'}, f'masque/arch_{n}', 'translucent')
        m(f'divan_{i}', [box((0, 3, 3), (16, 7, 15), 'v'), box((0, 7, 12), (16, 14, 15.5), 'v'),
                         box((0, 7, 3), (1.5, 10, 15), 'g'), box((14.5, 7, 3), (16, 10, 15), 'g'), box((0, 14, 12), (16, 15, 15.5), 'g'),
                         *[box((x, 0, z), (x + 2, 3, z + 2), 'g') for x in (0, 14) for z in (3, 13)]],
          {'v': f'masque/velvet_{n}', 'g': 'masque/gilt'}, f'masque/velvet_{n}')
    cube('masque', 'parquet', 'masque/parquet')
    cube('masque', 'coffer', side='masque/ebony', bottom='masque/coffer', top='masque/ebony')
    cube('masque', 'ebony', 'masque/ebony')
    cube('masque', 'cornice', side='masque/cornice', top='masque/ebony', bottom='masque/ebony')
    cube('masque', 'tracery', 'masque/tracery')
    legs = [box((7, 0, 2), (8, 16, 3), 'i', rot={'origin': [8, 8, 8], 'axis': 'x', 'angle': 22.5}),
            box((3, 0, 11), (4, 16, 12), 'i', rot={'origin': [8, 8, 8], 'axis': 'z', 'angle': -22.5}),
            box((12, 0, 11), (13, 16, 12), 'i', rot={'origin': [8, 8, 8], 'axis': 'z', 'angle': 22.5}),
            box((4, 14, 4), (12, 16, 12), 'i')]
    m('tripod', legs, {'i': 'masque/iron'}, 'masque/iron')
    m('brazier', [box((3, 0, 3), (13, 5, 13), 'i'), box((2, 4, 2), (14, 6, 14), 'i', overrides={'up': 'e'}, glow=False),
                  box((3, 5.5, 3), (13, 6.2, 13), 'e', faces_=('up',), glow=True),
                  box((2, 6, 8), (14, 16, 8), 'f', faces_=('north', 'south'), glow=True, shade=False),
                  box((8, 6, 2), (8, 16, 14), 'f', faces_=('east', 'west'), glow=True, shade=False)],
      {'i': 'masque/iron', 'e': 'masque/embers', 'f': 'minecraft:block/campfire_fire'}, 'masque/iron')
    m('clock_base', [box((1, 0, 3), (15, 16, 16), 'e', overrides={'north': 'p'})], {'e': 'masque/ebony', 'p': 'masque/clock_base'}, 'masque/ebony')
    for stage, suffix in ((0, ''), (1, '_stopped')):
        m(f'clock_case_{stage}', [box((2, 0, 4), (14, 16, 16), 'e', overrides={'north': 'c'}), box((1, 0, 3), (15, 1, 16), 'e'), box((1, 15, 3), (15, 16, 16), 'e')],
          {'e': 'masque/ebony', 'c': f'masque/clock_case{suffix}'}, 'masque/ebony')
        m(f'clock_face_{stage}', [box((1, 0, 3), (15, 16, 16), 'e', overrides={'north': 'c'})], {'e': 'masque/ebony', 'c': f'masque/clock_face{suffix}'}, 'masque/ebony')
    m('clock_crown', [box((0, 0, 2), (16, 4, 16), 'g', overrides={'up': 'e', 'down': 'e'}), box((2, 4, 3), (14, 8, 15), 'e'), box((5, 8, 4), (11, 11, 14), 'e'),
                      box((7, 11, 7), (9, 14, 9), 'g')], {'e': 'masque/ebony', 'g': 'masque/cornice'}, 'masque/ebony')
    m('music_stand', [box((7.5, 0, 7.5), (8.5, 10, 8.5), 'b'), box((5, 0, 7.5), (11, 1, 8.5), 'b'), box((7.5, 0, 5), (8.5, 1, 11), 'b'),
                      box((3, 9, 7), (13, 16, 8), 'b', overrides={'north': 'p'}, rot={'origin': [8, 9, 7.5], 'axis': 'x', 'angle': -22.5})],
      {'b': 'masque/gilt', 'p': 'masque/music'}, 'masque/gilt')
    m('mask', [box((3, 3, 15.4), (13, 13, 15.4), 'k', faces_=('north',))], {'k': 'masque/mask'}, 'masque/mask')
    m('invitation', [box((4, 0, 4), (12, 1, 12), 's'), box((5, 1, 6), (11, 1.5, 10), 'c', overrides={'up': 'i'})],
      {'s': 'masque/silver', 'c': 'masque/silver', 'i': 'masque/invitation'}, 'masque/invitation')
    m('stand', [box((3, 0, 3), (13, 2, 13), 'e'), box((5, 2, 5), (11, 14, 11), 'e'), box((2, 14, 2), (14, 16, 14), 'g', overrides={'up': 'e', 'down': 'e'})],
      {'e': 'masque/ebony', 'g': 'masque/cornice'}, 'masque/ebony')


def manor_models():
    m = lambda name, els, tex, particle, render='cutout': write_model('manor', name, els, tex, particle, render)
    cube('manor', 'boards', 'manor/boards')
    cube('manor', 'wet_boards', side='manor/boards', top='manor/wet_boards')
    cube('manor', 'damask', 'manor/damask')
    cube('manor', 'wainscot', 'manor/wainscot')
    cube('manor', 'cornice', side='manor/cornice', top='manor/plaster', bottom='manor/plaster')
    cube('manor', 'plaster', 'manor/plaster')
    cube('manor', 'flags', 'manor/flags')
    cube('manor', 'wet_flags', side='manor/flags', top='manor/wet_flags')
    side = ('east', 'west', 'up', 'down')
    m('glazing_0', [box((0, 0, 7), (16, 16, 9), 'g', cull=side)], {'g': 'manor/glazing'}, 'manor/glazing', 'translucent')
    m('glazing_1', [box((0, 7, 0), (16, 9, 16), 'g', cull=('north', 'south', 'east', 'west'))], {'g': 'manor/glazing'}, 'manor/glazing', 'translucent')
    m('rug', [box((0, 0, 0), (16, 1, 16), 'r', cull=('down',))], {'r': 'manor/rug'}, 'manor/rug')
    for i in range(4):
        m(f'portrait_{i}', [box((2, 2, 15), (14, 15, 16), 'p', overrides={d: 'g' for d in ('south', 'east', 'west', 'up', 'down')})],
          {'p': f'manor/portrait_{i}', 'g': 'masque/gilt'}, f'manor/portrait_{i}')
    m('sconce', [box((6, 4, 15), (10, 12, 16), 'b'), box((7.5, 6, 12), (8.5, 7, 15), 'b'), box((6.5, 7, 11.5), (9.5, 8, 14.5), 'b'),
                 box((7.25, 8, 12.25), (8.75, 12, 13.75), 'w'), box((7.5, 12, 12.5), (8.5, 13.5, 13.5), 'f', glow=True, shade=False)],
      {'b': 'manor/brass', 'w': 'manor/wax', 'f': 'manor/flame'}, 'manor/brass')
    m('catch', [box((6, 6, 15), (10, 10, 16), 'b'), box((7.5, 7, 13), (8.5, 9, 15), 'b')], {'b': 'manor/brass'}, 'manor/brass')
    m('linen', [box((0, 0, 6), (16, 16, 16), 'w', overrides={'north': 'l'})], {'w': 'manor/wainscot', 'l': 'manor/linen'}, 'manor/linen')
    m('bench', [box((0, 12, 2), (16, 15, 16), 'w'), box((1, 4, 3), (15, 5, 15), 'w'),
                *[box((x, 0, z), (x + 2, 12, z + 2), 'w') for x in (1, 13) for z in (3, 13)],
                box((3, 5, 6), (6, 8, 9), 't'), box((9, 5, 9), (12, 8, 12), 't')],
      {'w': 'manor/bench', 't': 'minecraft:block/terracotta'}, 'manor/bench')
    m('note', [box((3, 0, 4), (13, 0.5, 12), 'n', overrides={d: 'e' for d in ('north', 'south', 'east', 'west')})],
      {'n': 'manor/note', 'e': 'manor/note'}, 'manor/note')
    roof = [box((1, 10, 4), (15, 12, 15), 'r'), box((3, 12, 4), (13, 14, 15), 'r'), box((5, 14, 4), (11, 15, 15), 'r')]
    m('dollhouse', [box((1, 0, 5), (15, 10, 15), 's', overrides={'north': 'f'}), *roof],
      {'s': 'manor/dollhouse_side', 'f': 'manor/dollhouse_front', 'r': 'manor/dollhouse_roof'}, 'manor/dollhouse_front')
    m('blackboard', [box((0, 2, 15), (16, 15, 16), 'b', overrides={d: 'w' for d in ('south', 'east', 'west', 'up', 'down')})],
      {'b': 'manor/blackboard', 'w': 'manor/desk'}, 'manor/blackboard')
    m('school_desk', [box((1, 10, 2), (15, 12, 15), 'w'), box((1, 4, 2), (15, 10, 3), 'w'), box((1, 4, 3), (2, 10, 15), 'w'), box((14, 4, 3), (15, 10, 15), 'w'),
                      *[box((x, 0, z), (x + 1.5, 4, z + 1.5), 'w') for x in (1, 13.5) for z in (2, 13.5)], box((11, 12, 4), (13, 13.5, 6), 'i')],
      {'w': 'manor/desk', 'i': 'manor/iron'}, 'manor/desk')
    m('globe', [box((6, 0, 6), (10, 1, 10), 'b'), box((7.5, 1, 7.5), (8.5, 4, 8.5), 'b'), box((5, 4, 5), (11, 10, 11), 'g'), box((4.5, 3, 7.5), (5.5, 11, 8.5), 'b')],
      {'b': 'manor/brass', 'g': 'manor/globe'}, 'manor/globe')
    m('clock_low', [box((2, 0, 4), (14, 16, 15), 'w', overrides={'north': 'c'})], {'w': 'manor/desk', 'c': 'manor/clock_case'}, 'manor/desk')
    m('clock_high', [box((1, 0, 3), (15, 12, 15), 'w', overrides={'north': 'c'}), box((2, 12, 4), (14, 14, 14), 'w')], {'w': 'manor/desk', 'c': 'manor/clock_face'}, 'manor/desk')
    m('doormat', [box((1, 0, 2), (15, 1, 14), 'd')], {'d': 'manor/doormat'}, 'manor/doormat')
    m('umbrella', [box((5, 0, 5), (11, 8, 11), 'i'), box((7, 2, 7), (9, 14, 9), 'c'), box((6.5, 6, 6.5), (9.5, 11, 9.5), 'c'), box((7, 14, 7), (9, 15, 10), 'b')],
      {'i': 'manor/iron', 'c': 'manor/cloth', 'b': 'manor/brass'}, 'manor/cloth')
    m('mantel', [box((0, 10, 6), (16, 14, 16), 'm'), box((1, 6, 12), (3, 10, 16), 'm'), box((13, 6, 12), (15, 10, 16), 'm')], {'m': 'manor/marble'}, 'manor/marble')
    for stage, name in ((0, 'lamp_off'), (1, 'lamp_on')):
        m(f'signal_lamp_{stage}', [box((0, 0, 0), (16, 16, 16), 'g', overrides={'up': 'i', 'down': 'i'}, glow=stage == 1)],
          {'g': f'manor/{name}', 'i': 'manor/iron'}, f'manor/{name}')


MASQUE_KINDS = ['velvet', 'parquet', 'coffer', 'ebony', 'cornice', 'tracery', 'lancet', 'lancet_arch', 'tripod', 'brazier', 'clock_base',
                'clock_case', 'clock_face', 'clock_crown', 'divan', 'music_stand', 'mask', 'invitation', 'stand']
MASQUE_COLOURED = {'velvet', 'lancet', 'lancet_arch', 'divan'}
MASQUE_STOPPABLE = {'clock_case', 'clock_face'}
MANOR_KINDS = ['boards', 'wet_boards', 'damask', 'wainscot', 'cornice', 'plaster', 'flags', 'wet_flags', 'glazing', 'rug', 'portrait', 'sconce',
               'catch', 'linen', 'bench', 'note', 'dollhouse', 'blackboard', 'school_desk', 'globe', 'clock_low', 'clock_high', 'doormat',
               'umbrella', 'mantel', 'signal_lamp']
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
CUBES = {'velvet', 'parquet', 'coffer', 'ebony', 'cornice', 'tracery', 'boards', 'wet_boards', 'damask', 'wainscot', 'plaster', 'flags', 'wet_flags'}


def masque_model(kind, stage):
    if kind in MASQUE_COLOURED:
        return f'masque_{kind}_{stage}'
    if kind in MASQUE_STOPPABLE:
        return f'masque_{kind}_{1 if stage == 1 else 0}'
    return f'masque_{kind}'


def manor_model(kind, stage):
    if kind == 'portrait':
        return f'manor_portrait_{stage}'
    if kind == 'glazing':
        return f'manor_glazing_{1 if stage == 1 else 0}'
    if kind == 'signal_lamp':
        return f'manor_signal_lamp_{1 if stage == 1 else 0}'
    return f'manor_{kind}'


def blockstate(name, kinds, stages, model_of):
    variants = {}
    for facing, y in TURN.items():
        for kind in kinds:
            for stage in range(stages):
                v = {'model': NS + model_of(kind, stage)}
                if y and kind not in CUBES:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    (ASSETS / f'blockstates/{name}.json').write_text(json.dumps({'variants': variants}, indent=1) + '\n')
    return len(variants)


def write_json(path, data, original):
    text = json.dumps(data, indent=2, ensure_ascii=False)
    if original.endswith('\n'):
        text += '\n'
    path.write_text(text)


def names():
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    lang['block.the_oldest_house.masque'] = 'Masque chamber piece'
    lang['block.the_oldest_house.manor'] = 'Manor piece'
    write_json(path, lang, original)


if __name__ == '__main__':
    for folder in ('masque', 'manor'):
        (ASSETS / 'textures/block' / folder).mkdir(parents=True, exist_ok=True)
    for name, make in TEXTURES.items():
        path = ASSETS / 'textures/block' / f'{name}.png'
        make().save(path)
        if name in ANIMATED:
            path.with_name(path.name + '.mcmeta').write_text(json.dumps({'animation': {'frametime': ANIMATED[name]}}, indent=1) + '\n')
    MODELS.mkdir(parents=True, exist_ok=True)
    masque_models()
    manor_models()
    print('masque variants:', blockstate('masque', MASQUE_KINDS, 7, masque_model))
    print('manor variants:', blockstate('manor', MANOR_KINDS, 4, manor_model))
    print('textures:', len(TEXTURES))
    names()
