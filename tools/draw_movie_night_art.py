"""Movie Night (0.4.102): the film on the lake screen, its shadows, the shark's skin and the props, drawn in code.

*Teeth of Summer* is an original film for the House: eight backdrops (160 x 90, the film's own frame), the sprites the
client composes over them, three silhouettes the projector throws onto the screen, and the leader's countdown digits.
The client builds each frame from this atlas (MovieFilm), so the layout below is mirrored there; change both together.
The shark is a private, harmless figure; its skin follows LakeSharkModel's box layout. The speaker, projector and mortar
textures are the props' own faces.

Usage: python3 tools/draw_movie_night_art.py [--preview DIR]
"""
from pathlib import Path
import json
import math
import sys

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
FW, FH = 160, 90
ATLAS_W, ATLAS_H = 640, 320

# Plates: index -> (x, y). Sprites: name -> (x, y, w, h). MovieFilm mirrors these numbers.
PLATES = {i: ((i % 4) * FW, (i // 4) * FH) for i in range(8)}
SPRITES = {
    'legs_a': (0, 180, 32, 40), 'legs_b': (32, 180, 32, 40), 'lifeguard': (64, 180, 24, 40), 'hale': (88, 180, 24, 44),
    'boy': (112, 180, 16, 28), 'fin': (128, 180, 24, 16), 'fin_far': (128, 200, 10, 6), 'sandal': (152, 180, 16, 8),
    'flare': (152, 192, 12, 12), 'lantern': (168, 180, 16, 16), 'the_end': (184, 180, 96, 16),
    'digit_1': (280, 180, 20, 28), 'digit_2': (300, 180, 20, 28), 'digit_3': (320, 180, 20, 28), 'digit_4': (340, 180, 20, 28),
    'digit_5': (360, 180, 20, 28), 'swimmers': (380, 180, 40, 16), 'gull': (380, 200, 8, 4),
    'boat': (0, 228, 64, 32), 'cone': (64, 228, 64, 40), 'jaws': (128, 228, 96, 64),
    'rower': (224, 228, 48, 40), 'person': (272, 228, 32, 64), 'crawler': (304, 228, 64, 40),
}

# ------------------------------------------------------------------------------------------------ pixel font
FONT = {
    'A': ['01110', '10001', '10001', '11111', '10001', '10001', '10001'], 'B': ['11110', '10001', '10001', '11110', '10001', '10001', '11110'],
    'C': ['01111', '10000', '10000', '10000', '10000', '10000', '01111'], 'D': ['11110', '10001', '10001', '10001', '10001', '10001', '11110'],
    'E': ['11111', '10000', '10000', '11110', '10000', '10000', '11111'], 'F': ['11111', '10000', '10000', '11110', '10000', '10000', '10000'],
    'H': ['10001', '10001', '10001', '11111', '10001', '10001', '10001'], 'L': ['10000', '10000', '10000', '10000', '10000', '10000', '11111'],
    'M': ['10001', '11011', '10101', '10101', '10001', '10001', '10001'], 'N': ['10001', '11001', '10101', '10011', '10001', '10001', '10001'],
    'O': ['01110', '10001', '10001', '10001', '10001', '10001', '01110'], 'R': ['11110', '10001', '10001', '11110', '10100', '10010', '10001'],
    'S': ['01111', '10000', '10000', '01110', '00001', '00001', '11110'], 'T': ['11111', '00100', '00100', '00100', '00100', '00100', '00100'],
    'U': ['10001', '10001', '10001', '10001', '10001', '10001', '01110'], 'G': ['01111', '10000', '10000', '10011', '10001', '10001', '01111'],
    'I': ['11111', '00100', '00100', '00100', '00100', '00100', '11111'], 'K': ['10001', '10010', '10100', '11000', '10100', '10010', '10001'],
    'P': ['11110', '10001', '10001', '11110', '10000', '10000', '10000'], 'W': ['10001', '10001', '10001', '10101', '10101', '11011', '10001'],
    'Y': ['10001', '10001', '01010', '00100', '00100', '00100', '00100'], 'V': ['10001', '10001', '10001', '10001', '10001', '01010', '00100'],
    "'": ['00100', '00100', '01000', '00000', '00000', '00000', '00000'], '?': ['01110', '10001', '00001', '00110', '00100', '00000', '00100'],
    '.': ['00000', '00000', '00000', '00000', '00000', '00000', '00100'], ' ': ['00000'] * 7,
    '1': ['00100', '01100', '00100', '00100', '00100', '00100', '01110'], '2': ['01110', '10001', '00001', '00110', '01000', '10000', '11111'],
    '3': ['11110', '00001', '00001', '01110', '00001', '00001', '11110'], '4': ['00010', '00110', '01010', '10010', '11111', '00010', '00010'],
    '5': ['11111', '10000', '11110', '00001', '00001', '10001', '01110'],
}


def text_mask(text, scale=1, gap=1):
    w = len(text) * (5 + gap) * scale - gap * scale
    m = np.zeros((7 * scale, max(1, w)), dtype=bool)
    for i, ch in enumerate(text):
        glyph = FONT[ch]
        for y, row in enumerate(glyph):
            for x, bit in enumerate(row):
                if bit == '1':
                    x0 = (i * (5 + gap) + x) * scale
                    m[y * scale:(y + 1) * scale, x0:x0 + scale] = True
    return m


def stamp(img, mask, x, y, color, outline=None):
    """Paint a boolean mask onto an RGBA float image at (x, y), optionally ringed by an outline colour."""
    h, w = mask.shape
    if outline is not None:
        ring = np.zeros((h + 2, w + 2), dtype=bool)
        for dy in range(3):
            for dx in range(3):
                ring[dy:dy + h, dx:dx + w] |= mask
        paint_mask(img, ring, x - 1, y - 1, outline)
    paint_mask(img, mask, x, y, color)


def paint_mask(img, mask, x, y, color):
    H, W = img.shape[:2]
    h, w = mask.shape
    x0, y0, x1, y1 = max(0, x), max(0, y), min(W, x + w), min(H, y + h)
    if x1 <= x0 or y1 <= y0:
        return
    sub = mask[y0 - y:y1 - y, x0 - x:x1 - x]
    region = img[y0:y1, x0:x1]
    c = np.array(color, dtype=np.float32)
    if c.shape[0] == 3:
        c = np.append(c, 255)
    region[sub] = c


# ------------------------------------------------------------------------------------------------ canvas helpers
def canvas(w, h, color=(0, 0, 0, 0)):
    a = np.zeros((h, w, 4), dtype=np.float32)
    a[:] = color if len(color) == 4 else (*color, 255)
    return a


def vgrad(img, y0, y1, top, bottom, x0=0, x1=None):
    x1 = img.shape[1] if x1 is None else x1
    for y in range(max(0, y0), min(img.shape[0], y1)):
        t = (y - y0) / max(1, (y1 - y0 - 1))
        c = [top[i] * (1 - t) + bottom[i] * t for i in range(3)]
        img[y, x0:x1, :3] = c
        img[y, x0:x1, 3] = 255


def rect(img, x0, y0, x1, y1, color):
    H, W = img.shape[:2]
    x0, y0, x1, y1 = max(0, x0), max(0, y0), min(W, x1), min(H, y1)
    if x1 > x0 and y1 > y0:
        c = np.array(color, dtype=np.float32)
        if c.shape[0] == 3:
            c = np.append(c, 255)
        img[y0:y1, x0:x1] = c


def px(img, x, y, color):
    if 0 <= x < img.shape[1] and 0 <= y < img.shape[0]:
        c = np.array(color, dtype=np.float32)
        if c.shape[0] == 3:
            c = np.append(c, 255)
        img[y, x] = c


def poly(img, points, color):
    """Fill a polygon through PIL onto the float canvas."""
    H, W = img.shape[:2]
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).polygon(points, fill=255)
    mask = np.array(m) > 127
    c = np.array(color, dtype=np.float32)
    if c.shape[0] == 3:
        c = np.append(c, 255)
    img[mask] = c


def ellipse(img, box, color):
    H, W = img.shape[:2]
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).ellipse(box, fill=255)
    mask = np.array(m) > 127
    c = np.array(color, dtype=np.float32)
    if c.shape[0] == 3:
        c = np.append(c, 255)
    img[mask] = c


def line(img, points, color, width=1):
    H, W = img.shape[:2]
    m = Image.new('L', (W, H), 0)
    ImageDraw.Draw(m).line(points, fill=255, width=width)
    mask = np.array(m) > 127
    c = np.array(color, dtype=np.float32)
    if c.shape[0] == 3:
        c = np.append(c, 255)
    img[mask] = c


def noise(img, amount, seed, region=None):
    rng = np.random.default_rng(seed)
    y0, y1, x0, x1 = region or (0, img.shape[0], 0, img.shape[1])
    n = rng.normal(0, amount, (y1 - y0, x1 - x0, 1)).astype(np.float32)
    sub = img[y0:y1, x0:x1]
    alpha = sub[..., 3:4] > 0
    sub[..., :3] = np.where(alpha, np.clip(sub[..., :3] * (1 + n), 0, 255), sub[..., :3])


def dither_band(img, y0, y1, a, b, seed):
    """Ordered dither between two colours across a band, for a printed-film sky."""
    bayer = np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]) / 16
    for y in range(y0, y1):
        t = (y - y0) / max(1, y1 - y0)
        for x in range(img.shape[1]):
            img[y, x, :3] = b if t > bayer[y % 4, x % 4] else a
            img[y, x, 3] = 255


# ------------------------------------------------------------------------------------------------ the eight plates
def plate_title():
    img = canvas(FW, FH, (0, 0, 0))
    vgrad(img, 0, 22, (44, 20, 58), (150, 44, 52))
    vgrad(img, 22, 46, (150, 44, 52), (238, 128, 52))
    vgrad(img, 46, 54, (238, 128, 52), (252, 196, 92))
    ellipse(img, (64, 38, 96, 70), (255, 214, 120))
    vgrad(img, 54, FH, (60, 24, 36), (12, 8, 18))
    for i, y in enumerate(range(55, FH, 3)):
        w = max(2, 18 - i * 2)
        rect(img, 80 - w - (i % 2) * 2, y, 80 + w + (i % 3), y + 1, (236, 150, 70))
    # A fin far out on the sun's road, before anyone sees it.
    poly(img, [(118, 55), (124, 49), (126, 55)], (18, 10, 20))
    t1, t2 = text_mask('TEETH', 3), text_mask('OF SUMMER', 2)
    stamp(img, t1, (FW - t1.shape[1]) // 2, 6, (250, 236, 196), (40, 10, 16))
    stamp(img, t2, (FW - t2.shape[1]) // 2, 31, (250, 236, 196), (40, 10, 16))
    noise(img, .04, 11)
    return img


def plate_underwater():
    img = canvas(FW, FH, (0, 0, 0))
    vgrad(img, 0, 14, (148, 214, 220), (60, 140, 168))
    vgrad(img, 14, FH, (40, 104, 140), (4, 14, 34))
    rng = np.random.default_rng(3)
    # The moonlit surface seen from below, broken into ripples.
    for _ in range(90):
        x, y = rng.integers(0, FW), rng.integers(0, 12)
        rect(img, x, y, x + rng.integers(3, 12), y + 1, (210, 246, 240))
    for k in range(6):
        x = 14 + k * 26 + rng.integers(-4, 4)
        for y in range(14, 70):
            fade = 1 - (y - 14) / 56
            for dx in range(-1 - y // 18, 2 + y // 18):
                c = img[y, min(FW - 1, max(0, x + dx + (y // 9))), :3]
                img[y, min(FW - 1, max(0, x + dx + (y // 9))), :3] = c + (np.array([60, 90, 90]) - c * .2) * .25 * fade
    for _ in range(40):
        x, y = rng.integers(0, FW), rng.integers(20, FH)
        px(img, x, y, (90, 150, 170))
    noise(img, .05, 12)
    return img


def night_sky(img, horizon, seed, moon=True):
    vgrad(img, 0, horizon, (6, 8, 22), (26, 34, 64))
    rng = np.random.default_rng(seed)
    for _ in range(60):
        x, y = rng.integers(0, FW), rng.integers(0, horizon - 6)
        px(img, x, y, (190, 200, 230) if rng.random() < .7 else (120, 130, 170))
    if moon:
        ellipse(img, (18, 8, 32, 22), (232, 230, 204))
        ellipse(img, (22, 7, 36, 21), (14, 18, 40))


def night_sea(img, horizon, seed, moonpath=26):
    vgrad(img, horizon, FH, (16, 26, 50), (2, 4, 12))
    rng = np.random.default_rng(seed)
    for y in range(horizon + 1, FH, 2):
        for _ in range(3):
            x = moonpath + rng.integers(-3 - (y - horizon) // 2, 4 + (y - horizon) // 2)
            rect(img, x, y, x + rng.integers(2, 6), y + 1, (150, 160, 170))
        for _ in range(6):
            x = rng.integers(0, FW)
            rect(img, x, y, x + rng.integers(2, 8), y + 1, (34, 48, 78))


def buoy(img, x, y):
    rect(img, x - 3, y, x + 4, y + 6, (176, 40, 34))
    rect(img, x - 3, y + 2, x + 4, y + 3, (220, 214, 200))
    line(img, [(x - 2, y), (x, y - 6), (x + 2, y)], (150, 150, 140))
    rect(img, x - 1, y - 4, x + 2, y - 2, (200, 170, 70))
    rect(img, x - 5, y + 6, x + 6, y + 7, (60, 70, 90))


def plate_night_buoy():
    img = canvas(FW, FH, (0, 0, 0))
    night_sky(img, 40, 21)
    night_sea(img, 40, 22)
    buoy(img, 112, 38)
    noise(img, .04, 13)
    return img


def umbrella(img, x, y, a, b):
    for i in range(7):
        poly(img, [(x, y), (x - 14 + i * 4, y + 8), (x - 10 + i * 4, y + 8)], a if i % 2 == 0 else b)
    line(img, [(x, y + 1), (x, y + 22)], (230, 226, 210))


def day_beach(img, sea_top=30, shore=48):
    vgrad(img, 0, sea_top, (110, 168, 228), (186, 220, 240))
    rng = np.random.default_rng(8)
    for cx, cy in ((30, 10), (98, 6), (140, 16)):
        for _ in range(14):
            ox, oy = rng.integers(-12, 12), rng.integers(-3, 4)
            ellipse(img, (cx + ox - 5, cy + oy - 3, cx + ox + 5, cy + oy + 3), (246, 248, 250))
    vgrad(img, sea_top, shore, (40, 140, 170), (76, 190, 196))
    for y in range(shore - 4, shore):
        for _ in range(8):
            x = rng.integers(0, FW)
            rect(img, x, y, x + rng.integers(3, 10), y + 1, (236, 248, 246))
    vgrad(img, shore, FH, (236, 214, 160), (214, 182, 124))
    for _ in range(160):
        x, y = rng.integers(0, FW), rng.integers(shore, FH)
        px(img, x, y, (198, 168, 116))


def plate_beach():
    img = canvas(FW, FH, (0, 0, 0))
    day_beach(img)
    umbrella(img, 34, 44, (214, 52, 46), (246, 240, 228))
    umbrella(img, 120, 50, (240, 196, 54), (246, 240, 228))
    rect(img, 22, 70, 48, 78, (60, 110, 190))
    rect(img, 22, 72, 48, 73, (240, 240, 240))
    rect(img, 108, 74, 136, 81, (230, 120, 40))
    noise(img, .035, 14)
    return img


def plate_tower():
    img = canvas(FW, FH, (0, 0, 0))
    day_beach(img, 30, 52)
    wood, white = (176, 132, 88), (240, 236, 226)
    for x in (18, 40):
        line(img, [(x, 86), (x + (8 if x == 18 else -8), 40)], wood, 2)
    line(img, [(20, 64), (38, 64)], wood, 1)
    rect(img, 16, 30, 44, 40, white)
    rect(img, 18, 32, 42, 38, (200, 196, 186))
    poly(img, [(14, 30), (30, 20), (46, 30)], (214, 52, 46))
    line(img, [(30, 20), (30, 8)], (90, 80, 70))
    rect(img, 31, 8, 40, 14, (214, 52, 46))
    rect(img, 34, 9, 36, 13, white)
    rect(img, 33, 10, 38, 12, white)
    noise(img, .035, 15)
    return img


def plate_hotel():
    img = canvas(FW, FH, (0, 0, 0))
    vgrad(img, 0, 30, (120, 176, 230), (200, 226, 240))
    rect(img, 8, 10, 152, 64, (236, 206, 196))
    rect(img, 8, 10, 152, 14, (200, 90, 90))
    for row in range(3):
        for col in range(9):
            x, y = 16 + col * 15, 20 + row * 14
            rect(img, x, y, x + 8, y + 9, (70, 110, 150))
            rect(img, x, y + 4, x + 8, y + 5, (236, 206, 196))
            rect(img, x - 1, y + 9, x + 9, y + 10, (250, 244, 236))
    sign = text_mask("HALE'S", 1)
    rect(img, 80 - sign.shape[1] // 2 - 3, 2, 80 + sign.shape[1] // 2 + 3, 11, (30, 50, 90))
    stamp(img, sign, 80 - sign.shape[1] // 2, 3, (250, 220, 120))
    rect(img, 70, 50, 90, 64, (120, 70, 50))
    rect(img, 0, 64, FW, FH, (170, 126, 80))
    for y in range(64, FH, 4):
        rect(img, 0, y, FW, y + 1, (130, 92, 58))
    for x in (10, 150):
        rect(img, x, 40, x + 2, 66, (40, 40, 40))
        ellipse(img, (x - 2, 36, x + 4, 42), (250, 236, 180))
    noise(img, .035, 16)
    return img


def plate_dawn():
    img = canvas(FW, FH, (0, 0, 0))
    vgrad(img, 0, 22, (70, 80, 140), (210, 140, 170))
    vgrad(img, 22, 42, (210, 140, 170), (250, 200, 150))
    vgrad(img, 42, 56, (150, 150, 180), (90, 100, 140))
    rng = np.random.default_rng(31)
    for y in range(43, 56, 2):
        for _ in range(5):
            x = rng.integers(0, FW)
            rect(img, x, y, x + rng.integers(3, 9), y + 1, (240, 200, 180))
    vgrad(img, 56, FH, (200, 176, 150), (150, 126, 104))
    for _ in range(120):
        x, y = rng.integers(0, FW), rng.integers(56, FH)
        px(img, x, y, (170, 146, 120))
    noise(img, .035, 17)
    return img


def plate_night():
    img = canvas(FW, FH, (0, 0, 0))
    night_sky(img, 36, 41, moon=False)
    vgrad(img, 24, 36, (26, 34, 64), (40, 46, 70))
    night_sea(img, 36, 42, moonpath=120)
    noise(img, .04, 18)
    return img


# ------------------------------------------------------------------------------------------------ sprites
def sprite(w, h):
    return canvas(w, h)


def legs(phase):
    img = sprite(32, 40)
    skin, suit = (226, 186, 158), (210, 60, 60)
    shade = (40, 70, 90)
    rect(img, 10, 0, 22, 8, suit)
    kick = 3 if phase == 0 else -3
    for side, k in ((-1, kick), (1, -kick)):
        x = 16 + side * 4
        line(img, [(x, 6), (x + side * 2 + k, 22), (x + side + k * 2, 36)], skin, 4)
        line(img, [(x + side + k * 2, 36), (x + side * 3 + k * 2, 39)], skin, 3)
    # Seen from below, against the surface: mostly in silhouette.
    img[..., :3] = img[..., :3] * .35 + np.array(shade) * .65
    return img


def figure(w, h, shirt, trousers, skin=(222, 180, 150), hat=None, item=None):
    img = sprite(w, h)
    cx = w // 2
    rect(img, cx - 3, 2, cx + 3, 9, skin)
    rect(img, cx - 3, 1, cx + 3, 3, (70, 50, 40) if hat is None else hat)
    if hat is not None:
        rect(img, cx - 5, 3, cx + 5, 4, hat)
    rect(img, cx - 5, 9, cx + 5, 24, shirt)
    rect(img, cx - 4, 24, cx + 4, h - 2, trousers)
    rect(img, cx - 1, 26, cx + 1, h - 2, tuple(int(c * .7) for c in trousers))
    rect(img, cx - 5, h - 2, cx - 1, h, (40, 30, 26))
    rect(img, cx + 1, h - 2, cx + 5, h, (40, 30, 26))
    rect(img, cx - 7, 10, cx - 5, 22, shirt)
    if item == 'megaphone':
        rect(img, cx + 5, 10, cx + 8, 12, shirt)
        poly(img, [(cx + 7, 8), (cx + 11, 5), (cx + 11, 15), (cx + 7, 12)], (236, 236, 228))
    else:
        rect(img, cx + 5, 10, cx + 7, 22, shirt)
    px(img, cx - 1, 5, (30, 20, 20))
    px(img, cx + 1, 5, (30, 20, 20))
    return img


def boy():
    img = sprite(16, 28)
    skin = (222, 180, 150)
    rect(img, 5, 1, 11, 7, skin)
    rect(img, 5, 0, 11, 2, (120, 80, 50))
    rect(img, 4, 7, 12, 17, (240, 200, 60))
    rect(img, 5, 17, 11, 22, (50, 80, 150))
    rect(img, 5, 22, 7, 27, skin)
    rect(img, 9, 22, 11, 27, skin)
    rect(img, 2, 8, 4, 15, skin)
    rect(img, 12, 8, 14, 15, skin)
    return img


def fin(w, h, color=(28, 32, 40)):
    img = sprite(w, h)
    poly(img, [(0, h - 1), (int(w * .55), 0), (int(w * .62), 2), (w - 1, h - 1)], color)
    if w > 12:
        rect(img, 0, h - 2, w, h, (190, 210, 220))
    return img


def sandal():
    img = sprite(16, 8)
    ellipse(img, (1, 2, 14, 7), (196, 70, 110))
    line(img, [(4, 3), (8, 1), (11, 3)], (240, 200, 210))
    return img


def flare():
    img = sprite(12, 12)
    for r, c in ((6, (255, 80, 40, 90)), (4, (255, 120, 60, 170)), (2, (255, 230, 180, 255))):
        ellipse(img, (6 - r, 6 - r, 6 + r - 1, 6 + r - 1), c)
    return img


def lantern():
    img = sprite(16, 16)
    ellipse(img, (1, 1, 14, 14), (255, 200, 90, 60))
    rect(img, 6, 4, 10, 12, (250, 210, 110))
    rect(img, 5, 3, 11, 4, (60, 50, 40))
    rect(img, 5, 12, 11, 13, (60, 50, 40))
    rect(img, 7, 1, 9, 3, (60, 50, 40))
    rect(img, 3, 13, 13, 15, (40, 60, 90, 160))
    return img


def the_end():
    img = sprite(96, 16)
    m = text_mask('THE END?', 1)
    m2 = np.kron(m, np.ones((2, 2), dtype=bool))
    stamp(img, m2, (96 - m2.shape[1]) // 2, 1, (250, 236, 200), (20, 10, 10))
    return img


def digit(n):
    img = sprite(20, 28)
    m = np.kron(text_mask(str(n), 1), np.ones((3, 3), dtype=bool))
    stamp(img, m, (20 - m.shape[1]) // 2, (28 - m.shape[0]) // 2, (16, 16, 16))
    return img


def swimmers():
    img = sprite(40, 16)
    skin = (222, 180, 150)
    for i, x in enumerate((4, 16, 30)):
        rect(img, x, 4 + i % 2, x + 5, 10 + i % 2, skin)
        rect(img, x + 1, 1 + i % 2, x + 4, 4 + i % 2, (90, 60, 40))
        line(img, [(x, 6), (x - 3, 2 + i)], skin, 1)
        line(img, [(x + 5, 6), (x + 8, 3)], skin, 1)
        rect(img, x - 1, 10 + i % 2, x + 6, 12 + i % 2, (240, 248, 246))
    return img


def gull():
    img = sprite(8, 4)
    line(img, [(0, 1), (2, 0), (4, 2), (6, 0), (7, 1)], (240, 240, 240))
    return img


def boat():
    img = sprite(64, 32)
    poly(img, [(4, 20), (60, 20), (54, 30), (10, 30)], (190, 186, 170))
    rect(img, 6, 20, 58, 22, (150, 60, 50))
    rect(img, 20, 10, 40, 20, (176, 170, 156))
    rect(img, 23, 12, 29, 17, (60, 80, 100))
    rect(img, 32, 12, 38, 17, (60, 80, 100))
    rect(img, 44, 8, 48, 20, (60, 50, 44))
    rect(img, 44, 5, 48, 8, (222, 180, 150))
    rect(img, 43, 4, 49, 6, (40, 40, 60))
    rect(img, 48, 9, 54, 11, (60, 50, 44))
    rect(img, 53, 7, 57, 12, (250, 236, 170))
    line(img, [(30, 10), (30, 1)], (90, 80, 70))
    return img


def cone():
    img = sprite(64, 40)
    # A searchlight beam from the boat's lamp (left) widening to the right.
    poly(img, [(0, 18), (63, 0), (63, 39), (0, 22)], (250, 240, 190))
    xs = np.arange(64)[None, :]
    img[..., 3] = np.where(img[..., 3] > 0, 70 * (1 - xs / 80), 0)
    return img


def jaws():
    img = sprite(96, 64)
    skin, belly, mouth = (64, 72, 86), (210, 214, 214), (120, 30, 40)
    poly(img, [(10, 63), (20, 22), (48, 2), (76, 22), (86, 63)], skin)
    poly(img, [(24, 63), (30, 36), (48, 24), (66, 36), (72, 63)], mouth)
    for i in range(9):
        x = 28 + i * 5
        poly(img, [(x, 34 + abs(4 - i)), (x + 2, 40 + abs(4 - i)), (x + 4, 34 + abs(4 - i))], (246, 244, 236))
        poly(img, [(x, 63), (x + 2, 56), (x + 4, 63)], (246, 244, 236))
    poly(img, [(10, 63), (20, 40), (24, 63)], belly)
    poly(img, [(72, 63), (76, 40), (86, 63)], belly)
    ellipse(img, (30, 14, 36, 20), (8, 8, 10))
    ellipse(img, (60, 14, 66, 20), (8, 8, 10))
    px(img, 32, 15, (250, 250, 250))
    px(img, 62, 15, (250, 250, 250))
    noise(img, .05, 77)
    return img


def shadow(kind):
    """Pure silhouettes; the renderer gives them the projector's darkness."""
    if kind == 'rower':
        img = sprite(48, 40)
        ellipse(img, (18, 0, 30, 12), (0, 0, 0))
        poly(img, [(8, 39), (12, 16), (24, 11), (36, 16), (40, 39)], (0, 0, 0))
        line(img, [(2, 6), (46, 34)], (0, 0, 0), 2)
        rect(img, 40, 30, 47, 38, (0, 0, 0))
    elif kind == 'person':
        img = sprite(32, 64)
        ellipse(img, (11, 0, 21, 11), (0, 0, 0))
        poly(img, [(8, 12), (24, 12), (26, 36), (22, 36), (21, 63), (17, 63), (16, 40), (15, 63), (11, 63), (10, 36), (6, 36)], (0, 0, 0))
    else:
        # On her hands and feet, too long in the arms, hair hanging to the ground.
        img = sprite(64, 40)
        poly(img, [(12, 14), (24, 8), (40, 7), (48, 12), (44, 20), (16, 21)], (0, 0, 0))
        ellipse(img, (44, 8, 56, 20), (0, 0, 0))
        for x in range(44, 58, 2):
            line(img, [(x, 16), (x + (x % 3) - 1, 33 + (x % 5))], (0, 0, 0), 1)
        line(img, [(47, 15), (53, 27), (57, 39)], (0, 0, 0), 3)
        line(img, [(40, 13), (35, 26), (39, 39)], (0, 0, 0), 3)
        line(img, [(18, 17), (9, 27), (5, 39)], (0, 0, 0), 3)
        line(img, [(16, 17), (21, 29), (15, 39)], (0, 0, 0), 3)
    return img


def atlas():
    out = canvas(ATLAS_W, ATLAS_H)
    plates = [plate_title(), plate_underwater(), plate_night_buoy(), plate_beach(), plate_tower(), plate_hotel(), plate_dawn(), plate_night()]
    for i, p in enumerate(plates):
        x, y = PLATES[i]
        out[y:y + FH, x:x + FW] = p
    art = {
        'legs_a': legs(0), 'legs_b': legs(1), 'lifeguard': figure(24, 40, (214, 52, 46), (240, 230, 200), item='megaphone'),
        'hale': figure(24, 44, (240, 236, 222), (240, 236, 222), hat=(240, 236, 222)), 'boy': boy(), 'fin': fin(24, 16),
        'fin_far': fin(10, 6, (40, 40, 56)), 'sandal': sandal(), 'flare': flare(), 'lantern': lantern(), 'the_end': the_end(),
        'swimmers': swimmers(), 'gull': gull(), 'boat': boat(), 'cone': cone(), 'jaws': jaws(),
        'rower': shadow('rower'), 'person': shadow('person'), 'crawler': shadow('crawler'),
    }
    for n in range(1, 6):
        art[f'digit_{n}'] = digit(n)
    for name, img in art.items():
        x, y, w, h = SPRITES[name]
        assert img.shape[:2] == (h, w), (name, img.shape, (h, w))
        out[y:y + h, x:x + w] = img
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), 'RGBA')


# ------------------------------------------------------------------------------------------------ the shark
SHARK_W, SHARK_H = 256, 128
# Box layout mirrored by LakeSharkModel: name -> (u, v, w, h, d).
SHARK_BOXES = {
    'body': (0, 0, 16, 16, 32), 'head': (96, 0, 14, 12, 16), 'snout': (156, 0, 10, 6, 8), 'jaw': (192, 0, 12, 4, 14),
    'tail': (0, 48, 10, 10, 18), 'fluke_upper': (56, 48, 2, 20, 10), 'fluke_lower': (80, 48, 2, 12, 8),
    'dorsal_a': (100, 48, 2, 8, 14), 'dorsal_b': (132, 48, 2, 6, 10), 'dorsal_c': (156, 48, 2, 5, 6), 'pectoral': (172, 48, 14, 2, 10),
}


def faces(u, v, w, h, d):
    """Vanilla cube UV islands: top, bottom, then the strip right, front, left, back."""
    return {
        'top': (u + d, v, w, d), 'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h), 'front': (u + d, v + d, w, h), 'left': (u + d + w, v + d, d, h), 'back': (u + 2 * d + w, v + d, w, h),
    }


def shark_skin():
    img = canvas(SHARK_W, SHARK_H)
    dark, mid, belly = (58, 68, 82), (92, 102, 114), (222, 224, 220)
    rng = np.random.default_rng(5)
    for name, (u, v, w, h, d) in SHARK_BOXES.items():
        f = faces(u, v, w, h, d)
        fin_part = name.startswith(('dorsal', 'fluke', 'pectoral'))
        for face, (x, y, fw, fh) in f.items():
            if face == 'top':
                rect(img, x, y, x + fw, y + fh, dark)
            elif face == 'bottom':
                rect(img, x, y, x + fw, y + fh, belly if not fin_part else mid)
            else:
                split = fh if fin_part else max(1, int(fh * .58))
                rect(img, x, y, x + fw, y + split, dark)
                if split < fh:
                    for xx in range(x, x + fw):
                        jag = split + (1 if (xx * 7) % 5 < 2 else 0)
                        rect(img, xx, y + jag, xx + 1, y + fh, belly)
                        px(img, xx, y + jag - 1, mid)
    # Mouth: the jaw's top and the head's underside are inside the mouth, ringed with teeth.
    jx, jy, jw, jd = faces(*SHARK_BOXES['jaw'])['top']
    rect(img, jx, jy, jx + jw, jy + jd, (130, 40, 50))
    for i in range(jw):
        px(img, jx + i, jy, (246, 244, 236))
    for i in range(jd):
        px(img, jx, jy + i, (246, 244, 236))
        px(img, jx + jw - 1, jy + i, (246, 244, 236))
    hx, hy, hw, hd = faces(*SHARK_BOXES['head'])['bottom']
    rect(img, hx + 1, hy, hx + hw - 1, hy + hd - 2, (120, 34, 44))
    for i in range(1, hw - 1, 2):
        px(img, hx + i, hy, (246, 244, 236))
    # Eyes and gills on the head's sides, nostrils on the snout's front.
    for side in ('right', 'left'):
        x, y, fw, fh = faces(*SHARK_BOXES['head'])[side]
        ex = x + 3 if side == 'left' else x + fw - 4
        rect(img, ex, y + 4, ex + 2, y + 6, (6, 6, 8))
        px(img, ex, y + 4, (220, 220, 220))
        for g in range(5):
            gx = x + fw - 3 - g * 2 if side == 'left' else x + 2 + g * 2
            rect(img, gx, y + 3, gx + 1, y + 9, (34, 40, 50))
    x, y, fw, fh = faces(*SHARK_BOXES['snout'])['front']
    px(img, x + 2, y + 2, (20, 20, 24))
    px(img, x + fw - 3, y + 2, (20, 20, 24))
    # Old scars across the back.
    for _ in range(9):
        bx, by, bw, bd = faces(*SHARK_BOXES['body'])['top']
        x0, y0 = bx + rng.integers(1, bw - 4), by + rng.integers(1, bd - 3)
        line(img, [(x0, y0), (x0 + rng.integers(2, 5), y0 + rng.integers(1, 3))], (150, 156, 160))
    noise(img, .06, 6)
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), 'RGBA')


# ------------------------------------------------------------------------------------------------ props
def prop_textures():
    out = {}
    s = canvas(16, 16, (36, 34, 32))
    for y in range(2, 14):
        for x in range(2, 14):
            if (x + y) % 2 == 0:
                px(s, x, y, (58, 54, 50))
    ellipse(s, (4, 3, 11, 10), (20, 20, 20))
    ellipse(s, (6, 5, 9, 8), (70, 66, 60))
    rect(s, 0, 0, 16, 1, (90, 84, 70))
    rect(s, 0, 15, 16, 16, (90, 84, 70))
    noise(s, .05, 41)
    out['movie_speaker'] = s
    p = canvas(16, 16, (54, 58, 62))
    rect(p, 0, 0, 16, 2, (30, 32, 34))
    ellipse(p, (4, 4, 12, 12), (16, 16, 18))
    ellipse(p, (6, 6, 10, 10), (220, 230, 240))
    rect(p, 1, 13, 15, 15, (80, 84, 88))
    noise(p, .05, 42)
    out['movie_projector'] = p
    r = canvas(16, 16, (60, 64, 70))
    for x in range(0, 16, 3):
        rect(r, x, 0, x + 1, 16, (44, 48, 52))
    ellipse(r, (3, 3, 13, 13), (22, 22, 24))
    noise(r, .05, 43)
    out['movie_reel'] = r
    m = canvas(16, 16, (70, 50, 36))
    rect(m, 0, 0, 16, 16, (86, 62, 42))
    for y in range(0, 16, 4):
        rect(m, 0, y, 16, y + 1, (60, 42, 28))
    rect(m, 2, 6, 14, 8, (176, 36, 30))
    noise(m, .05, 44)
    out['movie_mortar'] = m
    return {k: Image.fromarray(np.clip(v, 0, 255).astype(np.uint8), 'RGBA') for k, v in out.items()}


# ------------------------------------------------------------------------------------------------ block models
def element(frm, to, textures, uv=None):
    faces = {}
    for face in ('north', 'south', 'east', 'west', 'up', 'down'):
        tex = textures.get(face, textures.get('all'))
        if tex is None:
            continue
        x0, y0, z0 = frm
        x1, y1, z1 = to
        if face in ('north', 'south'):
            box = [16 - x1, 16 - y1, 16 - x0, 16 - y0] if face == 'north' else [x0, 16 - y1, x1, 16 - y0]
        elif face in ('east', 'west'):
            box = [16 - z1, 16 - y1, 16 - z0, 16 - y0] if face == 'east' else [z0, 16 - y1, z1, 16 - y0]
        else:
            box = [x0, z0, x1, z1]
        faces[face] = {'texture': tex, 'uv': uv or box}
    return {'from': list(frm), 'to': list(to), 'faces': faces}


def prop_models():
    """Each prop faces north in its model; the blockstate turns it."""
    iron, dark, plank = 'minecraft:block/iron_block', 'minecraft:block/black_concrete', 'minecraft:block/spruce_planks'
    speaker = {'textures': {'cone': 'the_oldest_house:block/movie_speaker', 'iron': iron, 'dark': dark, 'particle': 'the_oldest_house:block/movie_speaker'},
               'elements': [element((3, 3, 2), (13, 13, 5), {'north': '#cone', 'south': '#dark', 'east': '#dark', 'west': '#dark', 'up': '#dark', 'down': '#dark'}, [0, 0, 16, 16]),
                            element((5, 5, 5), (11, 11, 11), {'all': '#dark'}),
                            element((7, 0, 9), (9, 6, 11), {'all': '#iron'})]}
    projector = {'textures': {'lens': 'the_oldest_house:block/movie_projector', 'reel': 'the_oldest_house:block/movie_reel', 'body': iron, 'dark': dark, 'particle': iron},
                 'elements': [element((3, 0, 3), (13, 9, 13), {'north': '#lens', 'south': '#body', 'east': '#body', 'west': '#body', 'up': '#body', 'down': '#dark'}, [0, 0, 16, 16]),
                              element((6, 3, 0), (10, 7, 3), {'all': '#dark'}),
                              element((7, 9, 2), (9, 15, 8), {'east': '#reel', 'west': '#reel', 'north': '#dark', 'south': '#dark', 'up': '#dark'}, [0, 0, 16, 16]),
                              element((7, 9, 9), (9, 14, 15), {'east': '#reel', 'west': '#reel', 'north': '#dark', 'south': '#dark', 'up': '#dark'}, [0, 0, 16, 16])]}
    mortar = {'textures': {'crate': 'the_oldest_house:block/movie_mortar', 'tube': dark, 'particle': 'the_oldest_house:block/movie_mortar'},
              'elements': [element((2, 0, 2), (14, 6, 14), {'all': '#crate'}, [0, 0, 16, 16]),
                           element((3, 6, 3), (7, 10, 7), {'all': '#tube'}),
                           element((9, 6, 4), (13, 10, 8), {'all': '#tube'}),
                           element((5, 6, 9), (9, 11, 13), {'all': '#tube'})]}
    return {'movie_speaker': speaker, 'movie_projector': projector, 'movie_mortar': mortar}


def write_models():
    folder = ASSETS / 'models/block'
    for name, model in prop_models().items():
        (folder / f'{name}.json').write_text(json.dumps(model, indent=2) + '\n')
    variants = {}
    for kind in ('speaker', 'projector', 'mortar'):
        for facing, y in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': f'the_oldest_house:block/movie_{kind}'}
            if y:
                v['y'] = y
            variants[f'facing={facing},kind={kind}'] = v
    (ASSETS / 'blockstates/movie_prop.json').write_text(json.dumps({'variants': variants}, indent=2) + '\n')
    (ASSETS / 'blockstates/movie_screen.json').write_text(json.dumps({'variants': {'': {'model': 'minecraft:block/black_concrete'}}}, indent=2) + '\n')


def write():
    movie = ASSETS / 'textures/movie'
    movie.mkdir(parents=True, exist_ok=True)
    atlas().save(movie / 'teeth_of_summer.png')
    shark_skin().save(ASSETS / 'textures/entity/lake_shark.png')
    for name, img in prop_textures().items():
        img.save(ASSETS / 'textures/block' / f'{name}.png')
    write_models()


def preview(folder):
    folder = Path(folder)
    folder.mkdir(parents=True, exist_ok=True)
    a = Image.open(ASSETS / 'textures/movie/teeth_of_summer.png')
    bg = Image.new('RGBA', a.size, (255, 0, 255, 255))
    bg.alpha_composite(a)
    bg.resize((a.width * 2, a.height * 2), Image.NEAREST).save(folder / 'movie_atlas_preview.png')
    s = Image.open(ASSETS / 'textures/entity/lake_shark.png')
    bg = Image.new('RGBA', s.size, (255, 0, 255, 255))
    bg.alpha_composite(s)
    bg.resize((s.width * 3, s.height * 3), Image.NEAREST).save(folder / 'lake_shark_preview.png')


def main():
    write()
    if '--preview' in sys.argv:
        preview(sys.argv[sys.argv.index('--preview') + 1])


if __name__ == '__main__':
    main()
