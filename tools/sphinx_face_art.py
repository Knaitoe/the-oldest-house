"""The Sphinx's face (0.4.337), for the first crossing: the whole of it, leaning over the hall wall and looking down.

The owner: "The first sphinx vignette also needs a fully visible sphinx face." Drawn at the same sixteen pixels to a block as her eye
and her paw, in vanilla's manner (small palettes, per-pixel noise, no smooth gradients): weathered sandstone skin with the paint still on
it, a striped headcloth falling to either side, kohl round the eyes and running out to the temples, painted brows.

0.4.340, the owner: "more ominous ... a slight grimace", "the same large eyes from the original vignette", "her mouth slightly opened by
default and small, sharp teeth inside", "something that should instill fear when it presses down out of the dark", and "make the cowl
thinner". Her eyes are as large and wide open as the one eye of the first crossing; her brows slant down hard toward her nose; the
corners of her mouth are dragged down and it is just open on small, sharp teeth; her nose is broken away as hers at Giza is; the kohl
has run down her cheeks and cracks run through the stone; the stone darkens toward its edges so she comes up out of the dark; and her
headcloth is thin, a little over a block beyond her face on either side. Beyond her outline the picture is empty, and its edges fall
away into the dark, so no block or frame shows.

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
EYE_RX, EYE_RY = 30, 13.0
SAND = ['#94744a', '#8a6c44', '#9c7a4e', '#7e6240']
BLUE = ['#28344a', '#232e42', '#2e3a50', '#1e283a']
GOLD = ['#806a36', '#766230', '#8a723c', '#6c582a']
KOHL = '#1a1c2c'
# How far her upper lids come down over the eye: hardly at all. Her eyes are as wide open as the one eye of the first crossing,
# staring (0.4.340, the owner: "the same large eyes from the original vignette").
LID_DROP = 1.0


# 0.4.340 (the owner: "make the cowl thinner"): the headcloth's half-width, a little over a block beyond her face on each side
# where it once ran out to the edge of the picture.
COWL = 112.0


def _cap(y):
    """Half-width of her headcloth's crown at row y: flat on top, swelling out to its full width by row 70."""
    if y >= 70:
        return COWL
    t = (70 - y) / 74.0
    return COWL * (1 - t ** 2.4) ** (1 / 2.4)


def _lappet_outer(y):
    """The headcloth's falls narrow a little toward the bottom."""
    return COWL - 8.0 * max(0.0, (y - 70) / 170.0) ** 1.4


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
    top = ey - EYE_RY * (1 - t * t) ** 0.5 + LID_DROP
    bottom = ey + EYE_RY * 0.88 * (1 - t * t) ** 0.6
    return top <= y <= bottom


def _lid_top(x, k):
    ex, ey = EYES[k]
    t = (x - ex) / EYE_RX
    return ey - EYE_RY * max(0.0, 1 - t * t) ** 0.5 + LID_DROP


def _lid_bottom(x, k):
    ex, ey = EYES[k]
    t = (x - ex) / EYE_RX
    return ey + EYE_RY * 0.88 * max(0.0, 1 - t * t) ** 0.6


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
                k = int(10 * (1 - y / H)) - int(78 * (abs(dx) / max(fh, 1)) ** 2.0) - int(30 * max(0.0, (y - 140) / 100.0)) - int(24 * max(0.0, (y - 150) / 90.0) * (abs(dx) / max(fh, 1)))
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
                    d3 = math.hypot((x - ex) / 44.0, (y - ey) / 22.0)
                    if d3 < 1:
                        k -= int(34 * (1 - d3) ** .7)
                # Cheekbones catch the light; the hollows under them do not.
                for ex in (96, 192):
                    d = math.hypot((x - ex) / 26.0, (y - 132) / 18.0)
                    if d < 1:
                        k += int(12 * (1 - d))
                    d = math.hypot((x - ex) / 22.0, (y - 160) / 16.0)
                    if d < 1:
                        k -= int(15 * (1 - d))
                for ex, ey in EYES:
                    d = math.hypot((x - ex) / 24.0, (y - (ey + 15)) / 5.0)
                    if d < 1:
                        k -= int(12 * (1 - d))
                # The nose: a lit ridge and a shadowed flank, down to where it is broken away, as hers at Giza is. Below the ragged
                # edge the break is rough, darker and pitted, with no tip and only the holes of the nostrils left.
                half = 7 + 11 * max(0.0, (y - 92) / 74.0) ** 1.6
                edge = 146 + 3 * math.sin(dx * 1.7) + 2 * math.sin(dx * .6 + 1)
                if 90 <= y < edge and abs(dx) <= half:
                    if abs(dx + 1) <= 2:
                        k += 14
                    elif dx < -half * .45:
                        k -= 24
                    elif dx > half * .55:
                        k -= 10
                elif edge <= y <= 168 and abs(dx) <= half - 1 + 1.6 * math.sin(y * 1.3 + dx * .7):
                    # The break follows the nose's own flare, its sides ragged, the stone rougher and a little darker than the
                    # weathered face round it, with a lip of shadow along its top.
                    c = rnd.choice(SAND)
                    k = rnd.randint(-16, 6) - 10 - int(10 * (y - edge) / 22.0) - (16 if y - edge < 1.5 else 0)
                    for side in (-1, 1):
                        if 161 <= y <= 166 and 4 <= side * dx <= 8 - (y - 161) // 2:
                            c, k = '#1a110a', 0
                if 169 <= y <= 176 and abs(dx) <= 20:
                    k -= 16 - abs(int(dx)) // 2
                # The philtrum.
                if 177 <= y <= 184 and abs(dx) <= 4:
                    k -= 6 if abs(dx) > 2 else -2
                # The folds from the wings of her nose down past the corners of her mouth, cut deep by the grimace.
                for side in (-1, 1):
                    fy = 166 + (side * dx - 18) * 1.25
                    if 18 <= side * dx <= 48 and abs(y - fy) <= 1.6 and 164 <= y <= 206:
                        k -= 20 if abs(y - fy) <= .8 else 10
                # Her mouth: a slight grimace, the corners drawn down and back, the lips just parted on a dark mouth and two rows of
                # small, sharp teeth (0.4.340, the owner: "her mouth slightly opened by default and small, sharp teeth inside").
                mw = 46
                if abs(dx) <= mw:
                    t = dx / mw
                    mid = 190 + 6.5 * t * t + 1.8 * t
                    oh = 4.2 * max(0.0, 1 - t * t) ** 0.7
                    if mid - oh - 4 + 2 * abs(t) <= y < mid - oh:
                        c, k = rnd.choice(['#7e4e34', '#76482f', '#845236']), int(-10 * abs(t)) - 2
                    elif mid - oh <= y <= mid + oh:
                        c, k = rnd.choice(['#0e0705', '#0a0504', '#120806']), 0
                        if abs(t) < .8:
                            # Upper teeth hang from the top lip, lower teeth rise from the bottom one between them: small, close-set
                            # points, yellowed, catching what light gets in.
                            for tx in range(-36, 37, 3):
                                r = y - (mid - oh)
                                if 0 <= r < 4 and abs(dx - tx) <= 1.3 - r * .42:
                                    c, k = rnd.choice(['#cfc09a', '#c4b48c']), -int(16 * abs(t) + r * 7)
                                r = (mid + oh) - y
                                if 0 <= r < 3 and abs(dx - tx - 1.5) <= 1.2 - r * .45:
                                    c, k = rnd.choice(['#c2b28a', '#b8a882']), -int(22 * abs(t) + r * 7) - 8
                    elif mid + oh < y <= mid + oh + 5 - 3 * abs(t):
                        c, k = rnd.choice(['#93603c', '#8a5a3a', '#9a6640']), int(4 * (1 - abs(t))) - 4
                    elif mid + oh + 5 - 3 * abs(t) < y <= mid + oh + 9 and abs(t) < .75:
                        k -= 16
                # Tight creases where the lips meet at each corner, pulled down toward the jaw.
                for side in (-1, 1):
                    for i in range(9):
                        if abs(dx - side * (46 + i * .6)) <= .9 and abs(y - (197 + i + (2 if side > 0 else 0))) <= .6:
                            k -= 22
                # Her chin drawn up under the grimace: a dimpled, shadowed knot of stone.
                d = math.hypot(dx / 14.0, (y - 210) / 6.0)
                if d < 1:
                    k -= int(8 * (1 - d))
                # The chin, round and lit, and the dark under it.
                d = math.hypot(dx / 18.0, (y - 220) / 12.0)
                if d < 1:
                    k += int(8 * (1 - d))
                if fh - abs(dx) < 4:
                    k -= 22
            elif y >= 60 and abs(dx) >= 84:
                # The headcloth's falls beside her face: paint in stripes, worn through to stone in places.
                band = int((y + 0.0009 * dx * dx) // 8) % 2
                worn = rnd.random() < 0.26 + 0.36 * max(0.0, (y - 140) / 100.0)
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
        for x in range(ex - 33, ex + 37 if k == 0 else ex + 34):
            t = (x - ex) / 33.0
            if k == 1 and x < ex - 36:
                continue
            inner = -t * side
            # Low and straight, slanting down hard toward her nose: a scowl, with only a little lift at the outer tail.
            yb = ey - 26 - 1.5 * (1 - t * t) + 9 * (inner + 1) / 2 - (2 * t * side if t * side > .6 else 0)
            thick = 5 if inner > .2 else (4 if abs(t) < .7 else 3)
            for j in range(thick):
                yy = int(round(yb)) + j
                if 0 <= x < W and 0 <= yy < H and px[x, yy][3]:
                    px[x, yy] = shade(KOHL, rnd.randint(-4, 6))
        # Two short furrows between her brows, where the scowl gathers the stone.
        if k == 0:
            for fx in (CX - 5, CX + 4):
                for yy in range(74, 88):
                    if px[fx, yy][3]:
                        px[fx, yy] = shade(px[fx, yy], -26 if 76 <= yy <= 85 else -12)
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
    # The kohl has run: streaks from under each eye down over the cheeks, darkest where they start.
    streaks = random.Random(3373)
    for k, (ex, ey) in enumerate(EYES):
        for sx, length in ((-13, 30), (-2, 44), (9, 26), (17, 36)):
            x = float(ex + sx); y0 = int(math.ceil(_lid_bottom(ex + sx, k))) + 2
            for i in range(length):
                y = y0 + i; x += streaks.choice((-.3, 0, 0, .3))
                xi = int(round(x))
                if 0 <= xi < W and 0 <= y < H and px[xi, y][3]:
                    f = 1 - i / length
                    px[xi, y] = shade(px[xi, y], -int(10 + 46 * f))
                    if i < length // 3 and px[xi + 1, y][3]:
                        px[xi + 1, y] = shade(px[xi + 1, y], -int(20 * f))
    # Everything away from her eyes and the middle of her face sinks toward black: she comes up out of the dark.
    for y in range(H):
        for x in range(W):
            r, g, b, a = px[x, y]
            if not a:
                continue
            rr = math.hypot((x - CX + .5) / 150.0, (y - 112) / 120.0)
            if rr > .5:
                px[x, y] = shade((r, g, b), -int(96 * (rr - .5) ** 1.15))
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
    for start, steps, seed in (((70, 150), 40, 1), ((210, 86), 30, 2), ((150, 214), 22, 3), ((112, 64), 18, 4), ((236, 180), 34, 5), ((196, 66), 90, 7), ((84, 176), 28, 8), ((128, 120), 26, 9)):
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


def preview(path, dark=False):
    """Her face with the whites and irises behind it, looking down, for checking by eye. With {@code dark}, as she is in the hall: the
    stone in the hall's dim light, her eyes lit, on black."""
    base = Image.new('RGBA', (W, H), (6, 5, 4, 255) if dark else (14, 12, 10, 255))
    eyes = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    wh = white().resize((EYE_RX * 2 + 6, int(EYE_RY * 2 + 6))); ir = iris().resize((30, 30))
    for ex, ey in EYES:
        eyes.alpha_composite(wh, (ex - EYE_RX - 3, int(ey - EYE_RY - 3)))
        eyes.alpha_composite(ir, (ex - 15, int(ey - 15 + 4)))
    f = face()
    if dark:
        f = Image.merge('RGBA', [b.point(lambda v: int(v * .42)) for b in f.split()[:3]] + [f.split()[3]])
        eyes = Image.merge('RGBA', [b.point(lambda v: int(v * .92)) for b in eyes.split()[:3]] + [eyes.split()[3]])
    base.alpha_composite(eyes)
    base.alpha_composite(f)
    base.resize((W * 3, H * 3), Image.NEAREST).save(path)


if __name__ == '__main__':
    import sys
    preview(sys.argv[1] if len(sys.argv) > 1 else 'sphinx_face_preview.png', dark=len(sys.argv) > 2 and sys.argv[2] == 'dark')
