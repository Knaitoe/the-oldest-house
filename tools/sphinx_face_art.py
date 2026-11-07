"""The Sphinx's face (0.4.337), for the first crossing: the whole of it, leaning over the hall wall and looking down.

The owner: "The first sphinx vignette also needs a fully visible sphinx face." Drawn at the same sixteen pixels to a block as her eye
and her paw, in vanilla's manner (small palettes, per-pixel noise, no smooth gradients): weathered sandstone skin with the paint still on
it, a striped headcloth falling to either side, kohl round the eyes and running out to the temples, a painted brow, a long straight nose,
closed lips with the corners just lifted. Beyond her outline the picture is empty, and its edges fall away into the dark, so no block or
frame shows.

The eyes are holes in the picture. Behind each the renderer draws the white, an iris that turns toward whoever is nearest, and a lid that
blinks; those three are small pictures of their own here. Picture coordinates are shared with the game through Sphinx.faceAt.
"""
import math
import random
from PIL import Image

from draw_tank_fittings import shade

W, H = 288, 240
CX = 144
# Her eyes' centres in the picture, and their half-width and half-height (the white shows inside).
EYES = ((100, 100), (188, 100))
EYE_RX, EYE_RY = 27, 9.5
SAND = ['#b38a50', '#a8804a', '#bc9258', '#9e7844']
BLUE = ['#34496a', '#2e4262', '#3a5274', '#283a58']
GOLD = ['#a88a3a', '#9e8234', '#b29444', '#927630']
KOHL = '#1a1c2c'


def _cap(y):
    """Half-width of her headcloth's crown at row y: flat on top, swelling out to the full width by row 70."""
    if y >= 70:
        return 144.0
    t = (70 - y) / 74.0
    return 144.0 * (1 - t ** 2.4) ** (1 / 2.4)


def _lappet_outer(y):
    """The headcloth's falls narrow a little toward the bottom."""
    return 144.0 - 12.0 * max(0.0, (y - 70) / 170.0) ** 1.4


def _face_half(y):
    """Half-width of her face itself, brow to chin."""
    if y < 62:
        return 0.0
    if y <= 140:
        return 84.0 - 4.0 * ((140 - y) / 78.0) ** 2
    t = (y - 140) / 98.0
    return 84.0 * max(0.0, 1 - t * t) ** 0.62 if t < 1 else 0.0


def _eye_open(x, y, k):
    """Inside the almond of eye k: an upper lid that arches and a flatter lower lid, both meeting at sharp corners."""
    ex, ey = EYES[k]
    t = (x - ex) / EYE_RX
    if abs(t) >= 1:
        return False
    top = ey - EYE_RY * (1 - t * t) ** 0.55 + 2.5
    bottom = ey + EYE_RY * 0.85 * (1 - t * t) ** 1.0
    return top <= y <= bottom


def _lid_top(x, k):
    ex, ey = EYES[k]
    t = (x - ex) / EYE_RX
    return ey - EYE_RY * max(0.0, 1 - t * t) ** 0.55 + 2.5


def _lid_bottom(x, k):
    ex, ey = EYES[k]
    t = (x - ex) / EYE_RX
    return ey + EYE_RY * 0.85 * max(0.0, 1 - t * t) ** 1.0


def face():
    rnd = random.Random(3370)
    im = Image.new('RGBA', (W, H), (0, 0, 0, 0)); px = im.load()
    for y in range(H):
        for x in range(W):
            dx = x - CX + .5
            inside_cap = abs(dx) <= _cap(y)
            inside_falls = y >= 60 and abs(dx) <= _lappet_outer(y)
            if not (inside_cap or inside_falls):
                continue
            fh = _face_half(y)
            k = 0
            if abs(dx) < fh and y >= 62:
                # Her face: sandstone, lit from above, falling into shadow toward its sides.
                c = rnd.choices(SAND, [4, 3, 2, 2])[0]
                k = int(12 * (1 - y / H)) - int(44 * (abs(dx) / max(fh, 1)) ** 2.6) - int(14 * max(0.0, (y - 150) / 90.0) * (abs(dx) / max(fh, 1)))
                # The band across her brow throws a shadow on her forehead, deepest at the temples.
                if y < 72:
                    k -= (72 - y) * 3
                if y < 96 and abs(dx) > fh * .72:
                    k -= int(30 * (abs(dx) / fh - .72) / .28 * (96 - y) / 34.0)
                # The brow ridge, and the shadow it throws on the lids.
                for ex, ey in EYES:
                    d = math.hypot((x - ex) / 34.0, (y - (ey - 16)) / 9.0)
                    if d < 1:
                        k += int(8 * (1 - d))
                    d2 = math.hypot((x - ex) / 32.0, (y - (ey - 4)) / 13.0)
                    if d2 < 1:
                        k -= int(14 * (1 - d2))
                # Cheekbones catch the light; the hollows under them do not.
                for ex in (96, 192):
                    d = math.hypot((x - ex) / 26.0, (y - 132) / 18.0)
                    if d < 1:
                        k += int(12 * (1 - d))
                    d = math.hypot((x - ex) / 22.0, (y - 160) / 16.0)
                    if d < 1:
                        k -= int(9 * (1 - d))
                # The nose: a lit ridge, a shadowed flank on the left, nostrils, the shadow under it.
                half = 7 + 11 * max(0.0, (y - 92) / 74.0) ** 1.6
                if 90 <= y <= 168 and abs(dx) <= half:
                    if abs(dx + 1) <= 2 and y < 158:
                        k += 16
                    elif dx < -half * .45:
                        k -= 22
                    elif dx > half * .55:
                        k -= 8
                # The tip, catching the light, and the wings of the nostrils either side of it.
                d = math.hypot(dx / 9.0, (y - 160) / 6.0)
                if d < 1:
                    k += int(12 * (1 - d))
                for side in (-1, 1):
                    d = math.hypot((dx - side * 14) / 6.5, (y - 161) / 6.0)
                    if d < 1:
                        k -= int(10 * (1 - d)) if side < 0 else int(4 * (1 - d))
                    if 164 <= y <= 167 and 7 <= side * dx <= 14 - (y - 164):
                        c, k = '#3a2a18', 0
                if 169 <= y <= 176 and abs(dx) <= 20:
                    k -= 16 - abs(int(dx)) // 2
                # The philtrum.
                if 177 <= y <= 184 and abs(dx) <= 4:
                    k -= 6 if abs(dx) > 2 else -2
                # Her mouth: closed, the corners just lifted.
                mw = 36
                if abs(dx) <= mw:
                    t = dx / mw
                    mid = 192 - 3 * (t * t) ** 1.0 * 1.0
                    mid = 192 - 3.5 * t * t
                    if mid - 7 <= y < mid - 1:
                        c, k = rnd.choice(['#8e5a3a', '#86543a', '#94603e']), int(-6 * abs(t))
                    elif mid - 1 <= y <= mid:
                        c, k = '#3e2618', 0
                    elif mid < y <= mid + 7 - 3 * abs(t):
                        c, k = rnd.choice(['#a0683f', '#986240', '#a87046']), int(6 * (1 - abs(t)))
                    elif mid + 7 - 3 * abs(t) < y <= mid + 10 and abs(t) < .7:
                        k -= 12
                for side in (-1, 1):
                    if abs(dx - side * 39) <= 1 and 185 <= y <= 191:
                        k -= 18
                # The chin, round and lit, and the dark under it.
                d = math.hypot(dx / 18.0, (y - 220) / 12.0)
                if d < 1:
                    k += int(8 * (1 - d))
                if fh - abs(dx) < 4:
                    k -= 22
            elif y >= 60 and abs(dx) >= 84:
                # The headcloth's falls beside her face: paint in stripes, worn through to stone in places.
                band = int((y + 0.0009 * dx * dx) // 8) % 2
                worn = rnd.random() < 0.18 + 0.3 * max(0.0, (y - 150) / 90.0)
                c = rnd.choice(SAND) if worn else rnd.choice(BLUE if band == 0 else GOLD)
                inner = abs(dx) - 84
                outer = _lappet_outer(y) - abs(dx)
                k = -int(34 * max(0.0, 1 - inner / 10.0)) - int(30 * max(0.0, 1 - outer / 12.0)) - int(70 * max(0.0, (y - 160) / 80.0) ** 1.2)
            elif y < 70 and abs(dx) <= _cap(y):
                # The crown: the same stripes, curving over her head, with the band across her brow.
                if 52 <= y <= 61 and abs(dx) <= 86:
                    c = rnd.choice(GOLD); k = 10 if y == 53 else (-18 if y >= 60 else 0)
                else:
                    band = int((y - 0.0016 * dx * dx + 400) // 8) % 2
                    c = rnd.choice(BLUE if band == 0 else GOLD)
                    edge = _cap(y) - abs(dx)
                    k = -int(34 * max(0.0, 1 - edge / 10.0)) - int(28 * max(0.0, 1 - y / 14.0)) + int(6 * (y / 60.0))
            else:
                # Under her chin, between the falls: her throat, in the dark.
                c = rnd.choice(['#3e3220', '#352a1b', '#2c2316'])
                k = -int(40 * max(0.0, (y - 150) / 90.0))
            px[x, y] = shade(c, k + rnd.randint(-5, 5))
    # The cobra at her brow.
    for y in range(36, 62):
        for x in range(CX - 6, CX + 7):
            dx = x - CX + .5
            hood = abs(dx) <= 6 * math.sin(math.pi * (y - 36) / 26.0) ** .6 + .5
            if hood and y < 58:
                px[x, y] = shade(rnd.choice(GOLD), 8 if abs(dx) < 2 else -10)
    for y in range(44, 58):
        px[CX, y] = shade('#2a3a5a', 0)
    px[CX - 2, 42] = shade(KOHL, 0); px[CX + 1, 42] = shade(KOHL, 0)
    # Her painted brows, arched, their tails drawn out level toward the temples.
    for k, (ex, ey) in enumerate(EYES):
        side = -1 if k == 0 else 1
        for x in range(ex - 30, ex + 31):
            t = (x - ex) / 30.0
            yb = ey - 19 - 7 * (1 - t * t) + (3 * t * side if t * side > 0 else 0)
            thick = 4 if abs(t) < .7 else 3
            for j in range(thick):
                yy = int(round(yb)) + j
                if 0 <= x < W and 0 <= yy < H and px[x, yy][3]:
                    px[x, yy] = shade(KOHL, rnd.randint(-4, 6))
        # A crease above the lid.
        for x in range(ex - 22, ex + 23):
            yy = int(round(_lid_top(x, k) - 5))
            if px[x, yy][3]:
                px[x, yy] = shade(px[x, yy], -20)
    # The eyes: holes in the picture, kohl along the upper lid running out past the outer corner, a finer line under.
    for k, (ex, ey) in enumerate(EYES):
        side = -1 if k == 0 else 1
        for y in range(int(ey - EYE_RY - 2), int(ey + EYE_RY + 2)):
            for x in range(ex - EYE_RX, ex + EYE_RX + 1):
                if _eye_open(x, y, k):
                    px[x, y] = (0, 0, 0, 0)
        for x in range(ex - EYE_RX - 1, ex + EYE_RX + 2):
            yt = _lid_top(x, k); yb = _lid_bottom(x, k)
            for j in range(3):
                yy = int(math.floor(yt)) - j
                if px[x, yy][3] or j == 0:
                    px[x, yy] = shade(KOHL, rnd.randint(-3, 6))
            yy = int(math.ceil(yb)) + 1
            px[x, yy] = shade(KOHL, 10)
        # The wing of kohl: out from the outer corner, straight, then a little down.
        x0 = ex + side * EYE_RX
        for i in range(24):
            x = x0 + side * i
            yy = ey - 1 - min(i, 8) // 4 + (1 if i > 18 else 0)
            for j in range(3 if i < 16 else 2):
                if 0 <= x < W:
                    px[x, yy + j - 1] = shade(KOHL, rnd.randint(-3, 6))
        # The inner corner's tear line, painted down toward the cheek.
        xi = ex - side * EYE_RX
        for i in range(10):
            px[xi - side * (i // 3), ey + 4 + i] = shade(KOHL, 6)
    # Her lowest edge falls away into the dark: no line where the picture stops.
    for y in range(196, H):
        f = (y - 196) / (H - 196.0)
        for x in range(W):
            r, g, b, a = px[x, y]
            if a and abs(x - CX + .5) >= _face_half(y) - 1:
                if rnd.random() < f ** 1.6:
                    px[x, y] = (0, 0, 0, 0)
                else:
                    px[x, y] = shade((r, g, b), -int(50 * f))
    # Weathering: a few dark cracks across the stone, and pits.
    for start, steps, seed in (((70, 150), 40, 1), ((210, 86), 30, 2), ((150, 214), 22, 3), ((112, 64), 18, 4), ((236, 180), 34, 5)):
        r = random.Random(3371 + seed); x, y = start
        for _ in range(steps):
            if 0 <= x < W and 0 <= y < H and px[x, y][3]:
                px[x, y] = shade(px[x, y], -36)
            x += r.choice((-1, 0, 1, 1)) * (1 if seed % 2 else -1); y += r.choice((0, 1, 1))
    for _ in range(90):
        x, y = rnd.randrange(W), rnd.randrange(H)
        if px[x, y][3]:
            px[x, y] = shade(px[x, y], -28)
    return im


def white():
    """The whites of her eyes, yellowed, with the shadow of the lids along the top."""
    rnd = random.Random(3372); im = Image.new('RGBA', (64, 32)); px = im.load()
    for y in range(32):
        for x in range(64):
            k = -int(36 * max(0.0, 1 - y / 10.0)) - int(20 * (abs(x - 31.5) / 32.0) ** 3)
            px[x, y] = shade(rnd.choice(['#e2d6b0', '#dccfa6', '#e6dbb8']), k + rnd.randint(-4, 4))
    for _ in range(6):
        x, y = rnd.choice((rnd.randint(2, 12), rnd.randint(51, 61))), rnd.randint(12, 24)
        for i in range(5):
            if 0 <= x < 64 and 0 <= y < 32:
                px[x, y] = shade('#c08060', rnd.randint(-10, 10))
            x += 1 if x < 32 else -1; y += rnd.choice((-1, 0, 1))
    return im


def iris():
    """Her iris: amber with a darker rim, a round black pupil and one highlight. Outside the circle the picture is empty."""
    rnd = random.Random(3373); im = Image.new('RGBA', (32, 32), (0, 0, 0, 0)); px = im.load()
    for y in range(32):
        for x in range(32):
            d = math.hypot(x - 15.5, y - 15.5)
            if d > 15.5:
                continue
            if d <= 5.5:
                c = '#0c0a08'; k = 0
            elif d > 13.5:
                c = '#5a3a0e'; k = rnd.randint(-6, 6)
            else:
                a = math.atan2(y - 15.5, x - 15.5)
                c = rnd.choice(['#b07a1e', '#a87018', '#bc8626', '#9c6a16'])
                k = int(10 * math.sin(a * 9)) + rnd.randint(-8, 8) - int(12 * (d - 5.5) / 8)
            px[x, y] = shade(c, k)
    for x, y in ((10, 9), (11, 9), (10, 10), (12, 10)):
        px[x, y] = (250, 245, 220, 255)
    return im


def lid():
    """Her upper lid, drawn down over the eye when she blinks: the same stone, with the kohl along its lower edge."""
    rnd = random.Random(3374); im = Image.new('RGBA', (64, 32)); px = im.load()
    for y in range(32):
        for x in range(64):
            px[x, y] = shade(rnd.choices(SAND, [4, 3, 2, 2])[0], -int(14 * y / 31.0) + rnd.randint(-5, 5))
    for y in range(28, 32):
        for x in range(64):
            px[x, y] = shade(KOHL, rnd.randint(-3, 6))
    for x in range(64):
        px[x, 6] = shade(px[x, 6], -18)
    return im


def pictures():
    return {'entity/sphinx/face.png': face(), 'entity/sphinx/face_white.png': white(), 'entity/sphinx/face_iris.png': iris(), 'entity/sphinx/face_lid.png': lid()}


def preview(path):
    """Her face with the whites and irises behind it, looking down, for checking by eye."""
    base = Image.new('RGBA', (W, H), (14, 12, 10, 255))
    wh = white().resize((EYE_RX * 2 + 6, int(EYE_RY * 2 + 6))); ir = iris().resize((26, 26))
    for ex, ey in EYES:
        base.alpha_composite(wh, (ex - EYE_RX - 3, int(ey - EYE_RY - 3)))
        base.alpha_composite(ir, (ex - 13, int(ey - 13 + 4)))
    base.alpha_composite(face())
    base.resize((W * 3, H * 3), Image.NEAREST).save(path)


if __name__ == '__main__':
    import sys
    preview(sys.argv[1] if len(sys.argv) > 1 else 'sphinx_face_preview.png')
