#!/usr/bin/env python3
"""The hotel rebuilt (0.4.105): one `stanley` block (40 kinds, 4 stages) in vanilla-style 16x16 pixels.

Siding, roof and cornice for the rear wall the grounds see; the bar's gold paper and 217's faded roses; parquet, tiles, the
service corridor's paint and linoleum; windows whose room side is night and snow climbing the glass (stage 0 at the sill,
3 nearly to the top) and whose outside is lit panes behind curtains; and the desk, the register, the bar, the table for two
(laid, served, cleared), the boiler room's valve and switch, the staff time clock and the rooms' fittings. Run from anywhere;
it rewrites only its own files and keys. The older 32-pixel hotel materials in textures/block/stanley stay as they are.
"""
import json
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from generate_thin_rooms_assets import (ASSETS, MODELS, NS, box, canvas, clamp, fill, mix, mul, noisy, px, write_json,  # noqa: E402
                                        write_model)

T = 'stanley/'
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


# ------------------------------------------------------------------------------------------------ materials
def siding():
    """White clapboard: four laps a block, each lit along its top and shadowed under the lap above."""
    rng = random.Random(1401)
    img = canvas()
    base = (222, 220, 210)
    for y in range(16):
        row = y % 4
        f = 1.08 if row == 0 else .74 if row == 3 else 1.0 - row * .03
        for x in range(16):
            px(img, x, y, mul(base, f + rng.choice((0, 0, 0, .02, -.02, -.03))))
    for _ in range(3):
        px(img, rng.randrange(16), rng.randrange(16), (196, 194, 184))
    return img


def roof():
    """The red standing-seam roof: long pans with a raised seam every four pixels, lit on one side, shadowed on the other."""
    rng = random.Random(1402)
    img = canvas()
    for x in range(16):
        seam = x % 4
        for y in range(16):
            f = 1.32 if seam == 0 else .66 if seam == 1 else 1.0 - (y % 8) * .01
            px(img, x, y, mul((150, 44, 36), f + rng.choice((0, 0, 0, .03, -.03))))
    for _ in range(4):
        px(img, rng.randrange(2, 4) + 4 * rng.randrange(4), rng.randrange(16), (172, 60, 48))
    return img


def eave():
    """A white cornice: a fillet, a row of dentils and the shadow they cast."""
    img = siding()
    fill(img, 0, 0, 15, 2, (236, 234, 226))
    for x in range(16):
        px(img, x, 3, (180, 178, 168))
    for x in range(0, 16, 4):
        fill(img, x, 5, x + 1, 9, (238, 236, 228))
        fill(img, x + 2, 5, x + 3, 9, (150, 148, 140))
    fill(img, 0, 10, 15, 11, (120, 118, 112))
    return img


def damask(base, figure, seed):
    """A wallpaper repeat: a small lozenge figure twice a block, offset, on a faintly striped ground."""
    rng = random.Random(seed)
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul(base, (1.03 if x % 4 == 0 else 1.0) + rng.choice((0, 0, .02, -.02))))
    for cx, cy in ((4, 4), (12, 12)):
        for dx, dy in ((0, -3), (-1, -2), (1, -2), (-2, -1), (2, -1), (-2, 0), (2, 0), (-1, 1), (1, 1), (0, 2), (0, 0), (0, -1)):
            px(img, cx + dx, cy + dy, figure)
    return img


def gold():
    return damask((172, 128, 50), (214, 176, 92), 1403)


def rose():
    """217's paper: faded roses on a dusty stripe, a little stained."""
    img = damask((198, 172, 160), (176, 118, 116), 1404)
    for cx, cy in ((4, 4), (12, 12)):
        px(img, cx, cy, (150, 84, 86)); px(img, cx, cy - 1, (150, 84, 86))
        px(img, cx - 2, cy + 2, (110, 128, 96)); px(img, cx + 2, cy + 2, (110, 128, 96))
    for y in range(11, 16):
        for x in range(9, 14):
            if (x + y) % 3 == 0:
                r, g, b, a = img.getpixel((x, y)); px(img, x, y, mul((r, g, b), .93))
    return img


def parquet():
    """Herringbone in light oak and walnut, polished."""
    rng = random.Random(1405)
    img = canvas()
    a, b = (178, 128, 74), (124, 82, 46)
    for y in range(16):
        for x in range(16):
            cell = ((x // 4) + (y // 4)) % 2
            seam = (x % 4 == 0) if cell == 0 else (y % 4 == 0)
            px(img, x, y, mul(a if cell == 0 else b, .72 if seam else 1 + rng.choice((0, 0, .04, -.04))))
    return img


def tile():
    """Black and white kitchen tiles, an eight-pixel check with grout."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            white = ((x // 8) + (y // 8)) % 2 == 0
            c = (226, 224, 218) if white else (38, 38, 42)
            if x % 8 == 0 or y % 8 == 0:
                c = (120, 118, 112)
            px(img, x, y, c)
    return img


def white_tile():
    """White glazed tile in staggered courses, grey grout, a glint in each."""
    img = canvas((232, 232, 228, 255))
    for y in range(16):
        for x in range(16):
            course = y // 4
            if y % 4 == 3 or (x + course * 4) % 8 == 7:
                px(img, x, y, (156, 158, 156))
    for y in (0, 4, 8, 12):
        for x in range(0, 16, 8):
            px(img, (x + 1 + (y // 4) * 4) % 16, y, (250, 250, 248))
    return img


def paint():
    return noisy((228, 218, 190), 1406, (0, 0, 0, .02, -.02, -.03))


def paint_low():
    """Service green to the dado, a brown rail at its top."""
    img = noisy((96, 120, 94), 1407, (0, 0, 0, .03, -.03))
    fill(img, 0, 0, 15, 1, (92, 64, 44))
    for x in range(16):
        px(img, x, 2, (70, 88, 70))
    return img


def lino():
    """Red-brown linoleum, speckled, in faint squares."""
    rng = random.Random(1408)
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (128, 68, 54)
            if x == 0 or y == 0:
                c = mul(c, .82)
            px(img, x, y, mul(c, 1 + rng.choice((0, 0, 0, .06, -.06, .1))))
    return img


def plaster():
    """The ceilings: cream plaster, a raised panel with a moulded rosette."""
    img = noisy((230, 222, 198), 1409, (0, 0, .02, -.02))
    for i in range(1, 15):
        px(img, i, 1, (244, 238, 220)); px(img, 1, i, (244, 238, 220)); px(img, i, 14, (190, 180, 152)); px(img, 14, i, (190, 180, 152))
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        px(img, 7 + dx, 7 + dy, (206, 194, 160))
    for dx, dy in ((-1, 0), (2, 0), (0, -1), (0, 2), (1, -1), (1, 2), (-1, 1), (2, 1)):
        px(img, 7 + dx, 7 + dy, (244, 238, 220))
    return img


def shutters():
    """Two louvered leaves in dark green, shut for the season."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (44, 74, 52)
            if x in (0, 7, 8, 15) or y in (0, 15):
                c = (32, 54, 38)
            elif y % 2 == 0:
                c = (62, 98, 70)
            px(img, x, y, c)
    return img


# ---- windows: the room side, night and the snow climbing it; the outside, lit panes behind curtains
SNOW = (2, 12, 20, 29)  # pixels of the 32-pixel window under snow, by stage


def night_sky(img, y0, seed):
    rng = random.Random(seed)
    for y in range(16):
        for x in range(16):
            t = (y0 + y) / 31
            px(img, x, y, mix((18, 22, 44), (40, 52, 82), t))
    for _ in range(9):
        px(img, rng.randrange(2, 14), rng.randrange(1, 15), (220, 226, 236))


def drift(img, y0, height, seed):
    """Snow banked against the glass from outside: a soft uneven top, blue-white, darker where it presses on the pane."""
    rng = random.Random(seed)
    for x in range(16):
        top = 31 - height + (1 if (x * 7 + seed) % 5 == 0 else 0) - (1 if (x * 3 + seed) % 7 == 0 else 0)
        for y in range(16):
            gy = y0 + y
            if gy >= top:
                depth = gy - top
                c = (178, 192, 212) if depth == 0 else (250, 252, 255) if rng.random() < .06 else mul((226, 232, 240), (1.0 if depth < 3 else .93) + rng.choice((0, 0, .02, -.03)))
                px(img, x, y, c)


def sash(img, low):
    """The casing and glazing bars over the panes: dark wood round the outside, cream bars, a brass latch on the meeting rail."""
    for y in range(16):
        for x in (0, 15):
            px(img, x, y, (70, 46, 32))
        for x in (1, 14):
            px(img, x, y, (226, 216, 190))
        px(img, 7, y, (226, 216, 190)); px(img, 8, y, (206, 196, 170))
    if low:
        fill(img, 0, 14, 15, 15, (86, 58, 40)); fill(img, 0, 13, 15, 13, (226, 216, 190))
        fill(img, 1, 0, 14, 1, (226, 216, 190)); fill(img, 6, 1, 9, 1, (196, 152, 70))
        fill(img, 1, 7, 14, 7, (226, 216, 190))
    else:
        fill(img, 0, 0, 15, 1, (70, 46, 32)); fill(img, 1, 2, 14, 2, (226, 216, 190))
        fill(img, 1, 14, 14, 15, (226, 216, 190)); fill(img, 1, 8, 14, 8, (226, 216, 190))


def window_in(low, stage):
    img = canvas()
    y0 = 16 if low else 0
    night_sky(img, y0, 1410 + stage + (0 if low else 10))
    drift(img, y0, SNOW[stage], 1420 + stage)
    sash(img, low)
    return img


def window_out(low, stage):
    """From outside: warm light through the panes, red curtains at the sides, white trim, snow on the sill."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mix((246, 206, 120), (228, 168, 82), abs(x - 7.5) / 8))
    for y in range(16):
        for x in (2, 3, 12, 13):
            px(img, x, y, (122, 30, 30) if (y + x) % 3 else (98, 22, 24))
    if low:
        drift(img, 16, max(2, SNOW[stage] - 6), 1430 + stage)
    for y in range(16):
        for x in (0, 1, 14, 15):
            px(img, x, y, (236, 234, 226))
        px(img, 7, y, (236, 234, 226)); px(img, 8, y, (214, 212, 204))
    if low:
        fill(img, 0, 14, 15, 15, (236, 234, 226)); fill(img, 1, 7, 14, 7, (236, 234, 226))
        if SNOW[stage] > 2:
            fill(img, 0, 13, 15, 13, (244, 248, 252))
    else:
        fill(img, 0, 0, 15, 1, (236, 234, 226)); fill(img, 1, 8, 14, 8, (236, 234, 226))
    return img


def frame():
    return noisy((232, 228, 218), 1440, (0, 0, .02, -.03))


# ---- furnishings
WALNUT, WALNUT_DARK, BRASS = (88, 52, 34), (52, 30, 20), (204, 160, 72)


def walnut(seed=1450):
    rng = random.Random(seed)
    img = canvas()
    for x in range(16):
        g = rng.choice((1.0, 1.0, .88, 1.1))
        for y in range(16):
            px(img, x, y, mul(WALNUT, g + rng.choice((0, 0, .04, -.04))))
    return img


def panel_front():
    """The desk's front: two raised panels and a brass kick plate."""
    img = walnut(1451)
    for x0 in (1, 9):
        for i in range(x0, x0 + 6):
            px(img, i, 2, (120, 76, 48)); px(img, i, 11, WALNUT_DARK)
        for j in range(2, 12):
            px(img, x0, j, (120, 76, 48)); px(img, x0 + 5, j, WALNUT_DARK)
    fill(img, 0, 13, 15, 15, BRASS); fill(img, 0, 13, 15, 13, (236, 200, 120))
    return img


def counter_top():
    img = walnut(1452)
    for x in range(16):
        px(img, x, 0, (130, 86, 54)); px(img, x, 15, WALNUT_DARK)
    return img


def ledger():
    """An open register: two ruled pages, a red ribbon down the gutter."""
    img = canvas()
    fill(img, 0, 2, 15, 13, (96, 26, 24))
    fill(img, 1, 3, 7, 12, (236, 228, 204)); fill(img, 8, 3, 14, 12, (230, 222, 198))
    for y in range(5, 12, 2):
        for x in list(range(2, 7)) + list(range(9, 14)):
            px(img, x, y, (150, 140, 120))
    for y in range(3, 13):
        px(img, 7, y, (176, 40, 36))
    return img


def key_rack():
    """Pigeonholes: some with a brass key on a white tag, some empty."""
    img = walnut(1453)
    for cy in (0, 8):
        for cx in (0, 4, 8, 12):
            fill(img, cx + 1, cy + 1, cx + 3, cy + 6, (34, 20, 14))
            if (cx + cy) % 12 != 0:
                px(img, cx + 2, cy + 2, BRASS); px(img, cx + 2, cy + 3, BRASS)
                px(img, cx + 2, cy + 5, (236, 230, 214)); px(img, cx + 3, cy + 5, (236, 230, 214))
    return img


def bar_front():
    """Mahogany fluted in tall panels, a brass foot rail near the floor."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            c = (104, 40, 30)
            if x % 4 == 0:
                c = (70, 24, 20)
            elif x % 4 == 1:
                c = (128, 56, 40)
            px(img, x, y, c)
    fill(img, 0, 12, 15, 13, BRASS); fill(img, 0, 12, 15, 12, (240, 206, 128))
    fill(img, 0, 0, 15, 1, (70, 24, 20))
    return img


def back_bar():
    """Shelves of bottles: green, amber and clear, catching the light."""
    img = walnut(1454)
    rng = random.Random(1455)
    for shelf in (0, 8):
        fill(img, 0, shelf + 6, 15, shelf + 7, WALNUT_DARK)
        x = 1
        while x < 15:
            c = rng.choice(((44, 110, 52), (180, 110, 40), (210, 224, 220), (110, 30, 40)))
            h = rng.choice((4, 5, 5))
            fill(img, x, shelf + 6 - h, x + 1, shelf + 5, c)
            px(img, x, shelf + 6 - h, mul(c, 1.3))
            x += 3
    return img


def cloth():
    """White damask table linen falling in folds."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (238, 236, 230) if x % 5 else (214, 212, 206))
    return img


def setting(stage):
    """The table for two from above: two plates, silver either side, glasses, a candle between. One plate is the guest's."""
    img = cloth()
    for y in range(16):
        for x in range(16):
            if (x + y) % 7 == 0:
                px(img, x, y, (226, 224, 218))
    for cz, mine in ((3, False), (12, True)):
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                if abs(dx) + abs(dz) <= 3:
                    px(img, 7 + dx, cz + dz, (250, 250, 250) if abs(dx) + abs(dz) < 3 else (200, 196, 190))
        for dz in range(-2, 3):
            px(img, 4, cz + dz, (186, 190, 196)); px(img, 11, cz + dz, (186, 190, 196))
        px(img, 12, cz - 2, (220, 228, 234))
        if mine and stage == 1:
            for dx, dz in ((-1, 0), (0, 0), (1, 0), (0, -1), (0, 1)):
                px(img, 7 + dx, cz + dz, (130, 70, 38))
            px(img, 6, cz - 1, (78, 120, 52)); px(img, 8, cz + 1, (78, 120, 52))
        if mine and stage == 2:
            px(img, 6, cz, (170, 140, 110)); px(img, 8, cz - 1, (170, 140, 110))
    fill(img, 7, 7, 8, 8, (240, 232, 200)); px(img, 7, 7, (250, 200, 80))
    return img


def card():
    img = canvas((236, 228, 206, 255))
    for i in range(16):
        px(img, i, 0, (196, 156, 70)); px(img, i, 15, (196, 156, 70)); px(img, 0, i, (196, 156, 70)); px(img, 15, i, (196, 156, 70))
    for y in (4, 6, 8, 10, 12):
        for x in range(3, 13 - (y % 4)):
            px(img, x, y, (70, 60, 52))
    fill(img, 4, 2, 11, 2, (120, 34, 30))
    return img


def easel():
    return walnut(1456)


def crystal():
    """Brass arms and crystal drops."""
    img = canvas((0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if (x + y) % 3 == 0:
                px(img, x, y, (236, 240, 248))
            elif (x * y) % 5 == 0:
                px(img, x, y, (200, 214, 230))
            else:
                px(img, x, y, (250, 232, 170))
    return img


def shade():
    """A pleated silk shade, glowing."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, mul((246, 214, 150), 1.0 if x % 3 else .9))
    return img


def brass():
    return noisy(BRASS, 1457, (0, 0, .06, -.08))


def notice():
    img = card()
    fill(img, 7, 1, 8, 2, (180, 30, 30))
    return img


def book():
    img = ledger()
    return img


def valve():
    """A red handwheel with four spokes on a black pipe."""
    img = canvas((42, 42, 46, 255))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if 5.5 <= d <= 7.4:
                px(img, x, y, (176, 34, 30) if d < 6.8 else (120, 22, 22))
            elif d < 1.6:
                px(img, x, y, (150, 150, 150))
            elif (abs(x - 7.5) < 1 or abs(y - 7.5) < 1) and d < 5.5:
                px(img, x, y, (150, 28, 26))
    return img


def switch():
    """A knife switch on a slate panel: copper blades, a black handle, warning paint."""
    img = noisy((58, 62, 66), 1458, (0, 0, .04, -.04))
    for x in (5, 10):
        fill(img, x, 3, x, 11, (196, 118, 70))
    fill(img, 4, 11, 11, 12, (196, 118, 70)); fill(img, 6, 12, 9, 14, (20, 20, 20))
    for x in range(16):
        px(img, x, 0, (210, 170, 40) if x % 4 < 2 else (20, 20, 20))
    return img


def time_clock():
    """The staff clock: a round face over the card slot, punched cards in a rack beside."""
    img = walnut(1459)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 5) ** 2) ** .5
            if d < 4.2:
                px(img, x, y, (236, 230, 210) if d < 3.6 else (190, 150, 70))
    px(img, 7, 3, (30, 30, 30)); px(img, 7, 4, (30, 30, 30)); px(img, 8, 5, (30, 30, 30))
    fill(img, 4, 11, 11, 12, (20, 14, 10))
    for x in (1, 13):
        for y in range(9, 15):
            px(img, x, y, (232, 226, 206) if y % 2 else (200, 190, 168))
    return img


def range_front():
    """A black iron range: two oven doors, chrome bars."""
    img = noisy((32, 32, 36), 1460, (0, 0, .05, -.05))
    for x0 in (1, 9):
        fill(img, x0, 4, x0 + 5, 13, (48, 48, 54))
        fill(img, x0 + 1, 5, x0 + 4, 5, (190, 194, 200))
    fill(img, 0, 0, 15, 2, (60, 60, 66))
    for x in (3, 7, 11):
        px(img, x, 1, (190, 194, 200))
    return img


def range_top():
    img = noisy((40, 40, 44), 1461, (0, 0, .05, -.05))
    for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11)):
        for y in range(16):
            for x in range(16):
                if 1.5 < ((x - cx) ** 2 + (y - cy) ** 2) ** .5 < 3:
                    px(img, x, y, (22, 22, 24))
    return img


def steel():
    rng = random.Random(1462)
    img = canvas()
    for y in range(16):
        g = rng.choice((1.0, .96, 1.04))
        for x in range(16):
            px(img, x, y, mul((176, 180, 186), g))
    return img


def enamel():
    return noisy((240, 240, 236), 1463, (0, 0, .02, -.03))


def tub_top(head):
    img = enamel()
    fill(img, 2, 0 if not head else 3, 13, 15 if head else 12, (204, 208, 210))
    if head:
        fill(img, 5, 0, 10, 1, (190, 194, 200)); px(img, 5, 1, BRASS); px(img, 10, 1, BRASS)
    return img


def trunk():
    """A steamer trunk: leather, slats, brass corners and lock."""
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (106, 64, 38) if y % 5 else (150, 104, 60))
    for cx, cy in ((0, 0), (14, 0), (0, 14), (14, 14)):
        fill(img, cx, cy, cx + 1, cy + 1, BRASS)
    fill(img, 7, 6, 8, 8, BRASS)
    return img


def linen_shelf():
    """Shelves of folded linen."""
    img = walnut(1464)
    for shelf in (0, 5, 10):
        fill(img, 0, shelf + 4, 15, shelf + 4, WALNUT_DARK)
        for x0 in (1, 6, 11):
            fill(img, x0, shelf, x0 + 3, shelf + 3, (238, 236, 228))
            for x in range(x0, x0 + 4):
                px(img, x, shelf + 1, (212, 210, 204))
    return img


def cart():
    img = canvas()
    for y in range(16):
        for x in range(16):
            px(img, x, y, (232, 230, 222) if y < 9 else (140, 120, 150))
    for x in (0, 15):
        for y in range(16):
            px(img, x, y, (170, 174, 180))
    fill(img, 0, 8, 15, 8, (170, 174, 180))
    return img


def switchboard():
    """A wooden switchboard: rows of jacks and two cords left plugged in."""
    img = walnut(1465)
    for y in range(2, 10, 2):
        for x in range(2, 15, 2):
            px(img, x, y, (16, 12, 10))
    for i in range(6):
        px(img, 4 + i // 2, 10 + i, (150, 30, 30)); px(img, 10 - i // 3, 10 + i, (30, 30, 30))
    return img


def filing():
    """An oak filing cabinet, four drawers with brass pulls and card holders."""
    img = noisy((150, 108, 64), 1466, (0, 0, .04, -.04))
    for d in range(4):
        y = d * 4
        for x in range(16):
            px(img, x, y, (96, 66, 38))
        fill(img, 6, y + 1, 9, y + 1, (232, 226, 206)); fill(img, 7, y + 2, 8, y + 2, BRASS)
    return img


TEXTURES = {
    'siding': siding, 'roof': roof, 'eave': eave, 'gold': gold, 'rose': rose, 'parquet': parquet, 'tile': tile,
    'white_tile': white_tile, 'paint': paint, 'paint_low': paint_low, 'lino': lino, 'plaster': plaster, 'shutters': shutters,
    **{f'window_low_{s}': (lambda s=s: window_in(True, s)) for s in range(4)},
    **{f'window_high_{s}': (lambda s=s: window_in(False, s)) for s in range(4)},
    **{f'outside_low_{s}': (lambda s=s: window_out(True, s)) for s in range(4)},
    **{f'outside_high_{s}': (lambda s=s: window_out(False, s)) for s in range(4)},
    'frame': frame, 'walnut16': walnut, 'panel_front': panel_front, 'counter_top': counter_top, 'ledger': ledger,
    'key_rack': key_rack, 'bar_front': bar_front, 'back_bar': back_bar, 'cloth': cloth,
    **{f'setting_{s}': (lambda s=s: setting(s)) for s in range(3)},
    'card': card, 'easel': easel, 'crystal': crystal, 'shade': shade, 'brass16': brass, 'notice': notice, 'book': book,
    'valve': valve, 'switch': switch, 'time_clock': time_clock, 'range_front': range_front, 'range_top': range_top,
    'steel16': steel, 'enamel': enamel, 'tub_head': lambda: tub_top(True), 'tub_foot': lambda: tub_top(False), 'trunk': trunk,
    'linen_shelf': linen_shelf, 'cart': cart, 'switchboard': switchboard, 'filing': filing,
}


# ------------------------------------------------------------------------------------------------ models
def m(name, elements, textures, particle, render='cutout'):
    write_model('stanley', name, elements, {k: (v if ':' in v else T + v) for k, v in textures.items()}, particle if ':' in particle else T + particle, render)


def cube6(name, north, south=None, east=None, west=None, up=None, down=None):
    side = east or north
    faces = {'north': north, 'south': south or north, 'east': side, 'west': west or side, 'up': up or side, 'down': down or up or side}
    tex = {'particle': NS + T + (side if ':' not in side else side)}
    for d, t in faces.items():
        tex[d] = t if ':' in t else NS + T + t
    (MODELS / f'stanley_{name}.json').write_text(json.dumps({'parent': 'minecraft:block/cube', 'textures': tex}, indent=1) + '\n')


def models():
    for name in ('siding', 'roof', 'gold', 'rose', 'parquet', 'tile', 'white_tile', 'paint', 'paint_low', 'lino'):
        cube6(name, name)
    cube6('eave', 'eave', up='siding', down='siding')
    cube6('ceiling', 'plaster', up='minecraft:block/dark_oak_planks', down='plaster')
    cube6('shutters', 'shutters', south='siding', east='siding', up='siding')
    for s in range(4):
        cube6(f'window_low_{s}', f'window_low_{s}', south=f'outside_low_{s}', east='frame', up='frame')
        cube6(f'window_high_{s}', f'window_high_{s}', south=f'outside_high_{s}', east='frame', up='frame')
    top = lambda t='c': box((0, 14, 0), (16, 16, 16), t)
    m('counter', [box((0, 0, 3), (16, 14, 16), 'w', overrides={'north': 'p'}), top()], {'w': 'walnut16', 'p': 'panel_front', 'c': 'counter_top'}, 'walnut16')
    m('register', [box((0, 0, 3), (16, 14, 16), 'w', overrides={'north': 'p'}), top(), box((3, 16, 4), (13, 17, 12), 'b', overrides={'up': 'l'})],
      {'w': 'walnut16', 'p': 'panel_front', 'c': 'counter_top', 'b': 'minecraft:block/red_wool', 'l': 'ledger'}, 'walnut16')
    m('key_rack', [box((0, 0, 12), (16, 16, 16), 'w', overrides={'north': 'k'})], {'w': 'walnut16', 'k': 'key_rack'}, 'key_rack')
    m('bar_counter', [box((0, 0, 3), (16, 14, 16), 'w', overrides={'north': 'f'}), top(), box((0, 2, 1), (16, 3, 2), 'r')],
      {'w': 'walnut16', 'f': 'bar_front', 'c': 'counter_top', 'r': 'brass16'}, 'bar_front')
    m('back_bar', [box((0, 0, 6), (16, 16, 16), 'w', overrides={'north': 'b'})], {'w': 'walnut16', 'b': 'back_bar'}, 'back_bar')
    m('table', [box((0, 0, 0), (16, 16, 16), 'c', overrides={'down': 'w'})], {'c': 'cloth', 'w': 'walnut16'}, 'cloth')
    for s in range(3):
        m(f'table_set_{s}', [box((0, 0, 0), (16, 16, 16), 'c', overrides={'up': 't', 'down': 'w'}), box((7, 16, 7), (9, 19, 9), 'k'),
                             box((7.5, 19, 7.5), (8.5, 20, 8.5), 'f', glow=True, shade=False)],
          {'c': 'cloth', 't': f'setting_{s}', 'w': 'walnut16', 'k': 'minecraft:block/white_candle', 'f': 'minecraft:block/fire_0'}, 'cloth')
    m('easel', [box((3, 0, 8), (4, 16, 9), 'w'), box((12, 0, 8), (13, 16, 9), 'w'), box((7.5, 0, 10), (8.5, 14, 11), 'w'),
                box((2, 5, 7), (14, 6, 9), 'w'), box((3, 6, 7.5), (13, 15, 8), 'c', overrides={'south': 'w'})], {'w': 'easel', 'c': 'card'}, 'card')
    m('chandelier', [box((7.5, 10, 7.5), (8.5, 16, 8.5), 'b'), box((3, 8, 3), (13, 10, 13), 'b', overrides={'down': 'k', 'up': 'k'}),
                     *[box((x, 3, z), (x + 2, 8, z + 2), 'k', glow=True, shade=False) for x, z in ((3, 7), (11, 7), (7, 3), (7, 11))],
                     box((6, 2, 6), (10, 8, 10), 'k', glow=True, shade=False)], {'b': 'brass16', 'k': 'crystal'}, 'crystal')
    m('sconce', [box((7, 5, 15), (9, 11, 16), 'b'), box((7.5, 7, 13), (8.5, 8, 15), 'b'), box((5.5, 8, 10.5), (10.5, 13, 15.5), 's', glow=True, shade=False)],
      {'b': 'brass16', 's': 'shade'}, 'shade')
    m('card', [box((4, 0, 5), (12, .5, 11), 'c')], {'c': 'card'}, 'card')
    m('book', [box((3, 0, 4), (13, 1, 12), 'b', overrides={'up': 'l'}), box((3.5, 1, 4.5), (12.5, 1.5, 11.5), 'p', overrides={'up': 'l'})],
      {'b': 'minecraft:block/brown_wool', 'l': 'book', 'p': 'card'}, 'book')
    m('notice', [box((3, 3, 15.5), (13, 14, 16), 'n', overrides={d: 'n' for d in DIRS})], {'n': 'notice'}, 'notice')
    m('valve', [box((6, 6, 12), (10, 10, 16), 'p'), box((3, 3, 10), (13, 13, 11), 'v', overrides={'south': 'v'})], {'p': 'minecraft:block/black_concrete', 'v': 'valve'}, 'valve')
    m('switch', [box((3, 2, 14), (13, 14, 16), 's'), box((6, 10, 12), (10, 12, 14), 'h')], {'s': 'switch', 'h': 'minecraft:block/black_concrete'}, 'switch')
    m('time_clock', [box((3, 1, 11), (13, 15, 16), 'w', overrides={'north': 't'})], {'w': 'walnut16', 't': 'time_clock'}, 'time_clock')
    m('range', [box((0, 0, 1), (16, 14, 16), 'i', overrides={'north': 'f', 'up': 't'}), box((0, 14, 13), (16, 15, 16), 'i')],
      {'i': 'minecraft:block/black_concrete', 'f': 'range_front', 't': 'range_top'}, 'range_front')
    m('steel_table', [box((0, 13, 0), (16, 16, 16), 's'), *[box((x, 0, z), (x + 2, 13, z + 2), 's') for x in (1, 13) for z in (1, 13)],
                      box((2, 4, 2), (14, 5, 14), 's')], {'s': 'steel16'}, 'steel16')
    for s, name in ((0, 'tub_head'), (1, 'tub_foot')):
        m(f'tub_{s}', [box((1, 2, 0), (15, 10, 16), 'e', overrides={'up': 't'}), *[box((x, 0, z), (x + 2, 2, z + 2), 'b') for x in (2, 12) for z in ((2,) if s == 0 else (12,))]],
          {'e': 'enamel', 'b': 'brass16', 't': name}, 'enamel')
    m('sink', [box((6, 0, 8), (10, 10, 12), 'e'), box((3, 10, 5), (13, 14, 16), 'e', overrides={'up': 'tub_foot'}), box((7.5, 14, 14), (8.5, 16, 15), 'b')],
      {'e': 'enamel', 'tub_foot': 'tub_foot', 'b': 'brass16'}, 'enamel')
    m('trunk', [box((1, 0, 2), (15, 12, 14), 't')], {'t': 'trunk'}, 'trunk')
    m('linen_shelf', [box((0, 0, 6), (16, 16, 16), 'w', overrides={'north': 'l'})], {'w': 'walnut16', 'l': 'linen_shelf'}, 'linen_shelf')
    m('cart', [box((1, 1, 2), (15, 15, 14), 'c'), *[box((x, 0, z), (x + 1, 1, z + 1), 'w') for x in (2, 13) for z in (3, 12)]],
      {'c': 'cart', 'w': 'minecraft:block/black_concrete'}, 'cart')
    m('switchboard', [box((0, 0, 12), (16, 16, 16), 'w', overrides={'north': 's'})], {'w': 'walnut16', 's': 'switchboard'}, 'switchboard')
    m('filing', [box((2, 0, 3), (14, 16, 16), 'o', overrides={'north': 'f'})], {'o': 'minecraft:block/oak_planks', 'f': 'filing'}, 'filing')


KINDS = ['siding', 'roof', 'eave', 'gold', 'rose', 'parquet', 'tile', 'white_tile', 'paint', 'paint_low', 'lino', 'ceiling', 'shutters',
         'window_low', 'window_high', 'counter', 'register', 'key_rack', 'bar_counter', 'back_bar', 'table', 'table_set', 'easel',
         'chandelier', 'sconce', 'card', 'book', 'notice', 'valve', 'switch', 'time_clock', 'range', 'steel_table', 'tub', 'sink',
         'trunk', 'linen_shelf', 'cart', 'switchboard', 'filing']
PLAIN = {'siding', 'roof', 'eave', 'gold', 'rose', 'parquet', 'tile', 'white_tile', 'paint', 'paint_low', 'lino', 'ceiling'}
TURN = {'north': 0, 'east': 90, 'south': 180, 'west': 270}


def model_of(kind, stage):
    if kind in ('window_low', 'window_high'):
        return f'stanley_{kind}_{stage}'
    if kind == 'tub':
        return f'stanley_tub_{stage}'
    if kind == 'table_set':
        return f'stanley_table_set_{stage if stage < 3 else 0}'
    return f'stanley_{kind}'


def blockstate():
    variants = {}
    for facing, y in TURN.items():
        for kind in KINDS:
            for stage in range(4):
                v = {'model': NS + model_of(kind, stage)}
                if y and kind not in PLAIN:
                    v['y'] = y
                variants[f'facing={facing},kind={kind},stage={stage}'] = v
    (ASSETS / 'blockstates/stanley.json').write_text(json.dumps({'variants': variants}, indent=1) + '\n')
    return len(variants)


def names():
    path = ASSETS / 'lang/en_us.json'
    original = path.read_text()
    lang = json.loads(original)
    lang['block.the_oldest_house.stanley'] = 'Hotel piece'
    write_json(path, lang, original)


if __name__ == '__main__':
    (ASSETS / 'textures/block/stanley').mkdir(parents=True, exist_ok=True)
    for name, make in TEXTURES.items():
        make().save(ASSETS / 'textures/block' / f'{T}{name}.png')
    MODELS.mkdir(parents=True, exist_ok=True)
    models()
    print('stanley variants:', blockstate())
    print('textures:', len(TEXTURES))
    names()
