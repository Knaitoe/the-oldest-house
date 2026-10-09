"""Original pixel art and native meshes for the two-stage elk vignette (0.4.50).

The yacht the reader wakes on, the stream woods, the construction crew's site and the
cave of carcasses: 16-pixel block materials drawn in the vanilla manner, so the scene
reads as one world with the grass and stone around it. Also the killer's new atlas
(waxed coat, hood, stitched hide mask, logging boots, felling axe), the carcass and
body atlases, the prop meshes and their blockstates.

Deterministic: every texture is seeded by its own name.
Run: python3 tools/generate_elk_yacht_assets.py
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, math, random

ROOT = Path(__file__).resolve().parents[1]
A = ROOT / 'src/main/resources/assets/the_oldest_house'
MOD = 'the_oldest_house'


def write(rel, obj):
    p = A / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(obj, indent=2) + '\n')


def save(rel, im):
    p = A / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    im.save(p, optimize=True)


def clamp(c):
    return tuple(max(0, min(255, int(v))) for v in c)


def shade(c, d):
    return clamp((c[0] + d, c[1] + d, c[2] + d))


def mix(a, b, t):
    return clamp(tuple(a[i] + (b[i] - a[i]) * t for i in range(3)))


def px(im, x, y, c, a=255):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), tuple(c) + (a,))


def fill(im, rect, c, rng=None, spread=0, a=255):
    x0, y0, x1, y1 = rect
    for y in range(int(y0), int(math.ceil(y1))):
        for x in range(int(x0), int(math.ceil(x1))):
            n = rng.randint(-spread, spread) if rng and spread else 0
            px(im, x, y, shade(c, n), a)


# ---------------------------------------------------------------------------
# 16 x 16 block materials


def tex16(name):
    return Image.new('RGBA', (16, 16), (0, 0, 0, 0)), random.Random(name)


def hull():
    im, r = tex16('yacht_hull')
    for y in range(16):
        for x in range(16):
            base = mix((246, 246, 241), (226, 226, 219), y / 15)
            px(im, x, y, shade(base, r.randint(-2, 2)))
    for x in range(16):  # a faint fairing line where the topsides were laid up
        if r.random() < .8:
            px(im, x, 11, (221, 221, 214))
    for _ in range(5):  # lake scum the crew had not yet wiped
        x, y = r.randint(0, 15), r.randint(13, 15)
        px(im, x, y, (204, 201, 186))
    return im


def stripe():
    im, r = tex16('yacht_hull_stripe')
    for y in range(16):
        for x in range(16):
            if y < 8:
                c = mix((240, 240, 234), (230, 230, 223), y / 8)
            elif y == 8:
                c = (204, 168, 78) if x % 7 else (226, 192, 104)
            elif y == 9:
                c = (232, 232, 225)
            else:
                c = mix((32, 46, 84), (22, 32, 62), (y - 10) / 5)
            px(im, x, y, shade(c, r.randint(-2, 2)))
    return im


def boot():
    im, r = tex16('yacht_boot')
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade(mix((26, 38, 70), (20, 28, 54), y / 15), r.randint(-2, 2)))
    for x in range(16):
        if r.random() < .5:
            px(im, x, 0, (40, 54, 92))
    return im


def antifoul():
    im, r = tex16('yacht_antifoul')
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade((86, 30, 30), r.randint(-4, 4)))
    for _ in range(14):
        x, y = r.randint(0, 15), r.randint(0, 6)
        px(im, x, y, shade((58, 70, 42), r.randint(-4, 4)))
        if r.random() < .5:
            px(im, x, y + 1, (66, 64, 40))
    return im


def teak():
    im, r = tex16('yacht_teak')
    woods = [(162, 110, 64), (150, 101, 58), (170, 118, 70), (156, 106, 61)]
    caulk = (30, 24, 20)
    for plank in range(4):
        x0 = plank * 4
        joint = r.randint(2, 13)
        for y in range(16):
            for dx in range(3):
                c = shade(woods[plank], r.randint(-5, 5))
                if r.random() < .18:
                    c = shade(c, -14)
                px(im, x0 + dx, y, c)
            px(im, x0 + 3, y, caulk)
        for dx in range(3):
            px(im, x0 + dx, joint, caulk)
        px(im, x0 + 1, (joint + 7) % 16, shade(woods[plank], -22))  # a knot
    return im


def panel():
    im, r = tex16('yacht_salon_panel')
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            bevel_hi = (x == 2 and 2 <= y <= 13) or (y == 2 and 2 <= x <= 13)
            bevel_lo = (x == 13 and 2 <= y <= 13) or (y == 13 and 2 <= x <= 13)
            if edge:
                c = (84, 38, 24)
            elif x == 1 or y == 1 or x == 14 or y == 14:
                c = (110, 52, 33)
            elif bevel_hi:
                c = (150, 80, 52)
            elif bevel_lo:
                c = (90, 42, 27)
            else:
                c = shade((124, 60, 39), r.randint(-4, 4) + (6 if (x * 7 + y // 3) % 5 == 0 else 0))
            px(im, x, y, c)
    return im


def carpet():
    im, r = tex16('yacht_carpet')
    for y in range(16):
        for x in range(16):
            c = shade((222, 212, 190), r.randint(-3, 3))
            if (x + y) % 8 == 0 or (x - y) % 8 == 0:
                c = shade((206, 194, 168), r.randint(-2, 2))
            px(im, x, y, c)
    return im


def cushion():
    im, r = tex16('yacht_cushion')
    for y in range(16):
        for x in range(16):
            c = shade((238, 236, 230), r.randint(-2, 2))
            if x in (0, 15) or y in (0, 15):
                c = (198, 196, 188)
            elif y == 7 and x % 2 == 0:
                c = (214, 212, 204)
            px(im, x, y, c)
    return im


def canvas():
    im, r = tex16('yacht_canvas')
    for y in range(16):
        for x in range(16):
            c = shade((36, 50, 86), 5 if (x + y) % 2 == 0 else -4)
            if y == 7:
                c = (56, 70, 106)
            px(im, x, y, shade(c, r.randint(-2, 2)))
    return im


def window(broken=False):
    im, r = tex16('yacht_window')
    for y in range(16):
        for x in range(16):
            c, a = shade((32, 44, 56), r.randint(-2, 2)), 196
            if (x + y) % 11 in (0, 1) and 2 < y < 13:
                c, a = (84, 104, 122), 206
            if y in (0, 15):
                c, a = (180, 184, 186), 255
            if broken and 1 < y < 14 and 1 < x < 14 and not (x+y in (4, 5, 25, 26) or x-y in (-10, 11)):
                c, a = (0, 0, 0), 0
            px(im, x, y, c, a)
    return im


def porthole(broken=False):
    im, r = tex16('yacht_porthole')
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade((236, 236, 230), r.randint(-2, 2)))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 4.6:
                if broken:
                    shard = d > 3.2 and (x+2*y) % 5 < 2
                    im.putpixel((x, y), (118, 155, 172, 255) if shard else (0, 0, 0, 0))
                else:
                    px(im, x, y, (76, 102, 117), 100 if (x+y) % 7 > 1 else 170)
            elif d < 6.6:
                lit = (x - 7.5) + (y - 7.5) < 0
                px(im, x, y, (222, 224, 226) if lit and d > 5.4 else (124, 128, 132) if not lit and d < 5.4 else (178, 182, 186))
    for x, y in ((7, 2), (13, 7), (8, 13), (2, 8)):
        px(im, x, y, (92, 96, 100))
    return im


def stainless():
    im, r = tex16('yacht_rail')
    for y in range(16):
        for x in range(16):
            band = [232, 214, 198, 186, 176, 168, 160, 152, 160, 170, 182, 196, 210, 222, 206, 180][x]
            px(im, x, y, shade((band, band + 2, band + 5), r.randint(-1, 1)))
    return im


def trailer():
    im, r = tex16('site_siding')
    ribs = [(206, 208, 206), (184, 186, 184), (146, 148, 146), (176, 178, 176)]
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade(ribs[x % 4], r.randint(-2, 2)))
    for _ in range(6):
        x, y = r.randint(0, 15), r.randint(0, 15)
        px(im, x, y, (150, 96, 56))
        if y < 15 and r.random() < .7:
            px(im, x, y + 1, (128, 92, 64))
    return im


def safety():
    im, r = tex16('safety_fence')
    for y in range(16):
        for x in range(16):
            cx, cy = x % 4, y % 4
            hole = 1 <= cx <= 2 and 1 <= cy <= 2
            if y == 0 or y == 15:
                hole = False
            if not hole:
                px(im, x, y, shade((236, 112, 26), r.randint(-6, 6)))
    return im


def mud():
    im, r = tex16('cave_drag_mud')
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade((68, 54, 42), r.randint(-4, 4)))
    for lane in (4, 11):  # drag streaks run along the trail
        for y in range(16):
            x = lane + int(round(math.sin(y * .5 + lane) * .8))
            px(im, x, y, (94, 76, 58))
            if r.random() < .5:
                px(im, x + 1, y, (84, 68, 52))
    for _ in range(3):  # cloven scuffs
        x, y = r.randint(1, 13), r.randint(1, 13)
        px(im, x, y, (44, 34, 26)); px(im, x + 2, y, (44, 34, 26))
        px(im, x, y + 1, (50, 38, 30)); px(im, x + 2, y + 1, (50, 38, 30))
    for _ in range(4):
        px(im, r.randint(0, 15), r.randint(0, 15), (74, 30, 26))
    return im


def fur():
    im, r = tex16('elk_fur')
    for y in range(16):
        for x in range(16):
            base = mix((134, 100, 68), (108, 78, 52), abs(x - 7.5) / 8)
            px(im, x, y, shade(base, r.randint(-5, 5)))
    for _ in range(26):  # combed strands lying one way
        x, y = r.randint(0, 15), r.randint(0, 15)
        for k in range(3):
            px(im, x + k // 2, y + k, (88, 62, 40))
    for y in range(16):  # the darker line along the spine
        px(im, 7, y, (78, 56, 38)); px(im, 8, y, (84, 60, 40))
    return im


def stains():
    out = []
    shapes = ['pool', 'smear', 'spatter', 'prints']
    for i, kind in enumerate(shapes):
        im, r = tex16('stain_' + kind)
        dark, wet = (66, 14, 14), (96, 20, 18)
        if kind == 'pool':
            for y in range(16):
                for x in range(16):
                    d = math.hypot((x - 7.5) * 1.0, (y - 8) * 1.3) + math.sin(x * 1.7 + y) * 1.1
                    if d < 5.8:
                        px(im, x, y, wet if d < 3.8 else dark)
        elif kind == 'smear':
            for y in range(16):
                for lane, width in ((5, 2), (9, 3)):
                    w = width - (1 if y > 11 and r.random() < .6 else 0)
                    for dx in range(w):
                        if r.random() < .9:
                            px(im, lane + dx + (y // 6), y, dark if dx == 0 else wet)
        elif kind == 'spatter':
            for _ in range(18):
                x, y = r.randint(1, 14), r.randint(1, 14)
                px(im, x, y, wet)
                if r.random() < .4:
                    px(im, x + 1, y, dark)
            for y in range(6, 11):
                for x in range(6, 10):
                    if math.hypot(x - 7.5, y - 8.5) < 2.2:
                        px(im, x, y, dark)
        else:  # bootprints, lugged, one after the other
            for ox, oy in ((3, 9), (9, 1)):
                for y in range(6):
                    for x in range(4):
                        if (y == 2 and x in (0, 3)) or (y in (0, 5) and x in (0, 3)):
                            continue
                        if (x + y) % 2 == 0 or y in (0, 5):
                            px(im, ox + x, oy + y, (58, 40, 30) if y > 3 else dark)
        out.append((kind, im))
    return out


def ring():
    im, r = tex16('life_ring')
    for y in range(16):
        for x in range(16):
            band = (x // 4) % 2
            px(im, x, y, shade((232, 98, 30) if band == 0 else (238, 234, 226), r.randint(-3, 3)))
    for x in range(16):
        px(im, x, 7, (150, 146, 136))
        px(im, x, 8, (120, 116, 108))
    return im


def hardhat():
    im, r = tex16('hard_hat')
    for y in range(16):
        for x in range(16):
            px(im, x, y, shade(mix((246, 198, 52), (210, 160, 28), y / 15), r.randint(-3, 3)))
    for x in range(3, 13):
        px(im, x, 3, (252, 226, 120))
    return im


# ---------------------------------------------------------------------------
# Entity atlases. A box at (u, v) with integer size (w, h, d) maps its faces
# exactly as ModelPart.Cube does: top, bottom, then right/front/left/back.


def faces(u, v, w, h, d):
    return {
        'top': (u + d, v, u + d + w, v + d),
        'bottom': (u + d + w, v, u + d + 2 * w, v + d),
        'right': (u, v + d, u + d, v + d + h),
        'front': (u + d, v + d, u + d + w, v + d + h),
        'left': (u + d + w, v + d, u + 2 * d + w, v + d + h),
        'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


def paint_box(im, u, v, w, h, d, color, rng, spread=4, overrides=None):
    for face, rect in faces(u, v, w, h, d).items():
        c = (overrides or {}).get(face, color)
        fill(im, rect, c, rng, spread)
    return faces(u, v, w, h, d)


def rows(im, rect, y0, y1, c, rng=None, spread=0):
    x0, _, x1, _ = rect
    fill(im, (x0, rect[1] + y0, x1, rect[1] + y1), c, rng, spread)


SKIN = {'pale': (222, 186, 156), 'light': (206, 166, 132), 'tan': (176, 132, 96), 'brown': (132, 90, 62), 'deep': (96, 64, 46)}


def humanoid(name, skin, hair, shirt, trousers, shoes, rng, long_hair=False, collar=None, sleeves=True):
    """Paints the legacy 64x32 humanoid region every cast skin shares."""
    im = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    head = paint_box(im, 0, 0, 8, 8, 8, skin, rng, 3)
    # Hair on the crown, the back and the upper sides; long hair falls down the back.
    fill(im, head['top'], hair, rng, 4)
    for side in ('right', 'left', 'back'):
        x0, y0, x1, y1 = head[side]
        fill(im, (x0, y0, x1, y0 + (8 if long_hair and side == 'back' else 6 if long_hair else 3)), hair, rng, 4)
    fx, fy, _, _ = head['front']
    fill(im, (fx, fy, fx + 8, fy + 2), hair, rng, 4)
    px(im, fx + 2, fy + 4, (34, 28, 24)); px(im, fx + 5, fy + 4, (34, 28, 24))  # closed eyes, a line
    px(im, fx + 3, fy + 4, shade(skin, -20)); px(im, fx + 6, fy + 4, shade(skin, -20))
    px(im, fx + 3, fy + 6, shade(skin, -38)); px(im, fx + 4, fy + 6, shade(skin, -38))
    body = paint_box(im, 16, 16, 8, 12, 4, shirt, rng, 4)
    if collar:
        bx, by, _, _ = body['front']
        fill(im, (bx + 2, by, bx + 6, by + 2), collar, rng, 2)
    arm = paint_box(im, 40, 16, 4, 12, 4, shirt if sleeves else skin, rng, 4)
    for side in ('right', 'front', 'left', 'back'):
        rows(im, arm[side], 9 if sleeves else 0, 12, skin, rng, 3)
    leg = paint_box(im, 0, 16, 4, 12, 4, trousers, rng, 4)
    for side in ('right', 'front', 'left', 'back'):
        rows(im, leg[side], 10, 12, shoes, rng, 3)
    fill(im, leg['bottom'], shade(shoes, -18), rng, 2)
    return im, body, arm, leg, head


def blood(im, rect, rng, amount=6):
    x0, y0, x1, y1 = rect
    for _ in range(amount):
        x, y = rng.randint(int(x0), int(x1) - 1), rng.randint(int(y0), int(y1) - 1)
        px(im, x, y, (84, 22, 20))
        if rng.random() < .6:
            px(im, x, min(int(y1) - 1, y + 1), (64, 18, 16))


def guests():
    looks = [
        ('pale', (88, 62, 40), (34, 44, 74), (178, 160, 120), (70, 46, 30), (232, 230, 222), False),   # blazer, chinos
        ('light', (62, 40, 28), (150, 30, 36), (150, 30, 36), (40, 30, 30), None, True),                # red dress
        ('tan', (36, 30, 26), (126, 170, 208), (226, 222, 210), (180, 150, 110), None, False),          # polo, shorts
        ('deep', (26, 22, 20), (196, 176, 120), (52, 70, 108), (60, 44, 36), None, True),               # sequins, jeans
    ]
    for i, (tone, hair, shirt, trousers, shoes, collar, long_hair) in enumerate(looks):
        rng = random.Random('guest_%d' % i)
        skin = SKIN[tone]
        im, body, arm, leg, head = humanoid('guest', skin, hair, shirt, trousers, shoes, rng, long_hair, collar, sleeves=i != 2)
        if i == 0:  # an open blazer over a white shirt and a loosened tie
            bx, by, _, _ = body['front']
            fill(im, (bx + 3, by, bx + 5, by + 12), (232, 230, 222), rng, 2)
            fill(im, (bx + 3, by + 1, bx + 4, by + 7), (128, 28, 34), rng, 2)
        if i == 2:  # shorts end above the knee
            for side in ('right', 'front', 'left', 'back'):
                rows(im, leg[side], 6, 10, skin, rng, 3)
        if i == 3:  # sequins catch what light there is
            for side in ('front', 'back'):
                x0, y0, x1, y1 = body[side]
                for _ in range(10):
                    px(im, rng.randint(x0, x1 - 1), rng.randint(y0, y0 + 7), (238, 226, 170))
        if i == 1:  # the dress is a gown overlay at (96,28)
            fill(im, (96, 28, 128, 50), shirt, rng, 5)
            for x in range(96, 128, 3):
                fill(im, (x, 34, x + 1, 50), shade(shirt, -14), rng, 2)
        blood(im, body['front'], rng, 7)
        blood(im, body['back'], rng, 3)
        save('textures/entity/literary_guest_%d.png' % i, im)


def crew():
    looks = [('light', (96, 70, 44), (88, 96, 110), (236, 120, 26)), ('brown', (30, 26, 22), (142, 46, 40), (226, 216, 40)),
             ('tan', (70, 50, 34), (64, 80, 64), (236, 120, 26)), ('pale', (150, 110, 66), (110, 108, 104), (226, 216, 40))]
    hats = [(240, 240, 232), (246, 198, 52), (236, 120, 26), (246, 198, 52)]
    for i, (tone, hair, flannel, vest) in enumerate(looks):
        rng = random.Random('crew_%d' % i)
        skin = SKIN[tone]
        im, body, arm, leg, head = humanoid('crew', skin, hair, flannel, (62, 74, 98), (92, 64, 38), rng, collar=shade(flannel, -20))
        for side in ('front', 'back', 'right', 'left'):  # flannel check
            x0, y0, x1, y1 = body[side]
            for y in range(y0, y1, 3):
                fill(im, (x0, y, x1, y + 1), shade(flannel, -26), rng, 2)
        for side in ('front', 'back'):  # hi-vis vest with two reflective bands
            x0, y0, x1, y1 = body[side]
            if side == 'front':
                fill(im, (x0, y0, x0 + 3, y1 - 2), vest, rng, 4); fill(im, (x1 - 3, y0, x1, y1 - 2), vest, rng, 4)
            else:
                fill(im, (x0, y0, x1, y1 - 2), vest, rng, 4)
            fill(im, (x0, y0 + 5, x1, y0 + 6), (206, 210, 212), rng, 2)
            fill(im, (x0, y0 + 8, x1, y0 + 9), (206, 210, 212), rng, 2)
        for side in ('right', 'left'):
            x0, y0, x1, y1 = body[side]
            fill(im, (x0, y0, x1, y1 - 2), vest, rng, 4)
        for side in ('right', 'front', 'left', 'back'):  # canvas knees, worn pale
            x0, y0, x1, y1 = leg[side]
            fill(im, (x0, y0 + 5, x1, y0 + 7), (84, 96, 116), rng, 3)
        # Hard hat: dome at (64,0) 8x3x8, brim at (64,16) 10x1x10.
        paint_box(im, 64, 0, 8, 3, 8, hats[i], rng, 4)
        paint_box(im, 64, 16, 10, 1, 10, shade(hats[i], -18), rng, 3)
        blood(im, body['front'], rng, 5)
        fx, fy, _, _ = head['front']
        px(im, fx + 6, fy + 2, (84, 22, 20)); px(im, fx + 6, fy + 3, (64, 18, 16))
        save('textures/entity/literary_crew_%d.png' % i, im)


def killer():
    rng = random.Random('killer_0450')
    im = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    skin, shirt, canvas_ = (112, 92, 76), (40, 38, 34), (52, 50, 44)
    head = paint_box(im, 0, 0, 8, 8, 8, skin, rng, 3)
    paint_box(im, 16, 16, 8, 12, 4, shirt, rng, 3)
    arm = paint_box(im, 40, 16, 4, 12, 4, shirt, rng, 3)
    for side in ('right', 'front', 'left', 'back'):
        rows(im, arm[side], 9, 12, (34, 30, 28), rng, 2)
    leg = paint_box(im, 0, 16, 4, 12, 4, canvas_, rng, 4)
    for side in ('right', 'front', 'left', 'back'):  # canvas trousers, mud to the knee
        x0, y0, x1, y1 = leg[side]
        for _ in range(6):
            px(im, rng.randint(x0, x1 - 1), rng.randint(y0 + 4, y1 - 1), (70, 58, 44))
    # Hood (64,0): charcoal wool, the front face open around the mask.
    hood = paint_box(im, 64, 0, 8, 8, 8, (46, 44, 42), rng, 4)
    x0, y0, x1, y1 = hood['front']
    fill(im, (x0 + 1, y0 + 2, x1 - 1, y1), (0, 0, 0), a=0)
    for side in ('right', 'left', 'back'):
        sx0, sy0, sx1, sy1 = hood[side]
        for y in range(sy0, sy1, 2):
            px(im, sx0 + (y // 2) % (sx1 - sx0), y, (34, 32, 30))
        fill(im, (sx0, sy1 - 1, sx1, sy1), (36, 34, 32))
    # Mask (96,0): tanned elk hide stitched over the face, two uneven eye holes.
    mask = paint_box(im, 96, 0, 8, 8, 1, (152, 120, 86), rng, 6)
    mx, my, _, _ = mask['front']
    for x in range(8):
        px(im, mx + x, my + 3, (96, 70, 46))  # the seam across the brow
    for y in range(8):
        if y % 2 == 0:
            px(im, mx + 4, my + y, (70, 48, 32))  # stitches down the middle
    for x, y in ((1, 4), (2, 4), (1, 5)):
        px(im, mx + x, my + y, (14, 12, 10))
    for x, y in ((5, 4), (6, 4), (6, 5), (5, 5)):
        px(im, mx + x, my + y, (14, 12, 10))
    for x in range(2, 6):
        px(im, mx + x, my + 7, (60, 40, 28))  # the mouth slit, sewn shut
    for x in (2, 4):
        px(im, mx + x, my + 6, (70, 48, 32))
    px(im, mx + 7, my + 1, (110, 40, 30)); px(im, mx + 7, my + 2, (84, 30, 24))
    # Coat (64,16) torso and (88,16) sleeves: waxed canvas, darker creases, stains.
    coat = (76, 68, 50)
    torso = paint_box(im, 64, 16, 8, 12, 4, coat, rng, 5)
    tx, ty, tx1, ty1 = torso['front']
    fill(im, (tx + 3, ty, tx + 5, ty1), shade(coat, -16), rng, 2)  # the overlap of the front
    for y in (ty + 3, ty + 6, ty + 9):
        px(im, tx + 4, y, (40, 36, 30))  # buttons
    fill(im, (tx + 1, ty + 7, tx + 3, ty + 9), shade(coat, -10), rng, 2)  # pocket
    fill(im, (tx + 5, ty + 7, tx + 7, ty + 9), shade(coat, -10), rng, 2)
    blood(im, torso['front'], rng, 9)
    sleeve = paint_box(im, 88, 16, 4, 12, 4, coat, rng, 5)
    for side in ('right', 'front', 'left', 'back'):
        rows(im, sleeve[side], 6, 7, shade(coat, -14), rng, 2)  # elbow crease
        rows(im, sleeve[side], 9, 12, (34, 30, 28), rng, 2)      # gloves
        rows(im, sleeve[side], 8, 9, (96, 38, 30), rng, 3)        # what dried on the cuffs
    # Coat tails on each leg (0,32), mud along the hem.
    tail = paint_box(im, 0, 32, 4, 6, 4, coat, rng, 5)
    for side in ('right', 'front', 'left', 'back'):
        rows(im, tail[side], 4, 6, (64, 54, 40), rng, 4)
    # Logging boots (16,32): oiled leather, red laces up the front, lugged soles.
    bootf = paint_box(im, 16, 32, 4, 5, 4, (86, 58, 36), rng, 4)
    bx, by, _, _ = bootf['front']
    for y in range(0, 4):
        px(im, bx + 1, by + y, (182, 66, 32)); px(im, bx + 2, by + y, (182, 66, 32) if y % 2 else (60, 40, 26))
    for side in ('right', 'front', 'left', 'back'):
        rows(im, bootf[side], 4, 5, (30, 28, 26), rng, 2)
    fill(im, bootf['bottom'], (26, 24, 22), rng, 2)
    for x in range(bootf['bottom'][0], bootf['bottom'][2]):
        if x % 2 == 0:
            px(im, x, bootf['bottom'][1] + 1, (44, 40, 36))
    paint_box(im, 32, 32, 4, 2, 1, (124, 124, 118), rng, 6)  # scuffed steel toes
    # Belt (0,48) with a sheath.
    belt = paint_box(im, 0, 48, 8, 2, 4, (44, 32, 24), rng, 3)
    bx, by, _, _ = belt['front']
    px(im, bx + 4, by, (150, 140, 110)); px(im, bx + 4, by + 1, (150, 140, 110))
    # Felling axe: handle (48,32) 1x1x10 hickory with tape, head (72,32) 2x5x3 steel.
    handle = paint_box(im, 48, 32, 1, 1, 10, (164, 128, 84), rng, 6)
    for x in range(48, 52):
        px(im, x, 42, (34, 32, 30))
    paint_box(im, 72, 32, 2, 5, 3, (80, 84, 88), rng, 4)
    for y in range(35, 40):
        px(im, 74, y, (200, 202, 204)); px(im, 77, y, (196, 198, 200))  # the honed edge
    px(im, 73, 37, (124, 70, 44)); px(im, 78, 36, (90, 30, 24))
    save('textures/entity/literary_killer.png', im)


def elk_atlas(name, skinned):
    rng = random.Random(name)
    im = Image.new('RGBA', (128, 64), (0, 0, 0, 0))
    coat, dark, belly = (118, 90, 62), (70, 52, 38), (150, 120, 84)
    body = paint_box(im, 0, 0, 10, 12, 23, coat, rng, 6)
    for side in ('right', 'left'):  # darker along the back, matted pale belly
        x0, y0, x1, y1 = body[side]
        fill(im, (x0, y0, x1, y0 + 3), dark, rng, 5)
        fill(im, (x0, y1 - 3, x1, y1), belly, rng, 6)
        for _ in range(10):
            px(im, rng.randint(x0, x1 - 1), rng.randint(y0, y1 - 1), (60, 48, 36))  # mud
    fill(im, body['top'], dark, rng, 5)
    fill(im, body['bottom'], belly, rng, 6)
    if skinned:
        for side in ('right', 'left'):
            x0, y0, x1, y1 = body[side]
            for y in range(y0 + 3, y1 - 2):
                for x in range(x0 + 4, x1 - 4):
                    c = (120, 44, 38) if (x + y // 2) % 5 else (196, 176, 150)
                    px(im, x, y, shade(c, rng.randint(-8, 8)))
            for x in range(x0 + 6, x1 - 5, 3):
                fill(im, (x, y0 + 4, x + 1, y1 - 3), (210, 196, 176), rng, 4)
    neck = paint_box(im, 50, 0, 7, 15, 8, (90, 66, 46), rng, 6)
    for side in ('front', 'right', 'left'):
        x0, y0, x1, y1 = neck[side]
        for _ in range(8):
            px(im, rng.randint(x0, x1 - 1), rng.randint(y0 + 6, y1 - 1), (78, 26, 22))
    # Head (80,0) 7.6x7x10 and snout (80,20): painted over the float footprint.
    fill(im, (80, 0, 116, 18), (106, 80, 56), rng, 6)
    fill(im, (90, 10, 98, 17), (100, 74, 52), rng, 5)
    px(im, 91, 12, (24, 20, 18)); px(im, 92, 12, (24, 20, 18)); px(im, 96, 12, (24, 20, 18)); px(im, 95, 12, (24, 20, 18))
    fill(im, (80, 20, 102, 30), (72, 54, 40), rng, 4)
    fill(im, (85, 25, 91, 29), (40, 32, 28), rng, 2)
    paint_box(im, 112, 0, 6, 3, 2, (96, 72, 50), rng, 5)
    legs = paint_box(im, 0, 38, 3, 13, 3, (76, 58, 42), rng, 5)
    fill(im, (14, 38, 30, 44), (32, 28, 24), rng, 3)  # hooves
    paint_box(im, 28, 38, 2, 2, 5, (150, 124, 90), rng, 5)
    save('textures/entity/literary_' + name + '.png', im)


# ---------------------------------------------------------------------------
# Meshes


def cube(a, b, tex, uv=(0, 0, 16, 16), rot=None, faces_=None):
    e = {'from': a, 'to': b, 'faces': {f: {'texture': '#' + tex, 'uv': list(uv)} for f in (faces_ or ['north', 'south', 'east', 'west', 'up', 'down'])}}
    if rot:
        e['rotation'] = rot
    return e


def glazing():
    for name, paint in (('yacht_window', window), ('yacht_porthole', porthole)):
        variants = {}
        for broken in (False, True):
            model = name + ('_broken' if broken else '')
            save('textures/block/%s.png' % model, paint(broken))
            write('models/block/%s.json' % model, {'parent': 'minecraft:block/cube_all',
                  'render_type': 'minecraft:cutout' if broken else 'minecraft:translucent',
                  'textures': {'all': '%s:block/%s' % (MOD, model)}})
            variants['broken=%s' % str(broken).lower()] = {'model': '%s:block/%s' % (MOD, model)}
        write('blockstates/%s.json' % name, {'variants': variants})


def blocks():
    simple = {'yacht_hull': hull, 'yacht_hull_stripe': stripe, 'yacht_boot': boot, 'yacht_antifoul': antifoul, 'yacht_teak': teak,
              'yacht_salon_panel': panel, 'yacht_carpet': carpet, 'yacht_cushion': cushion, 'yacht_canvas': canvas,
              'site_siding': trailer, 'cave_drag_mud': mud}
    for name, fn in simple.items():
        save('textures/block/%s.png' % name, fn())
        write('blockstates/%s.json' % name, {'variants': {'': {'model': '%s:block/%s' % (MOD, name)}}})
        write('models/block/%s.json' % name, {'parent': 'minecraft:block/cube_all', 'textures': {'all': '%s:block/%s' % (MOD, name)}})
    glazing()
    save('textures/block/yacht_rail.png', stainless())
    save('textures/block/safety_fence.png', safety())
    pane('yacht_rail', post=[cube([7.5, 0, 7.5], [8.5, 15, 8.5], 'rail'), cube([7, 15, 7], [9, 16, 9], 'rail')],
         side=[cube([7.25, 14, 0], [8.75, 15.5, 8], 'rail'), cube([7.6, 7, 0], [8.4, 7.8, 8], 'rail')], texture='yacht_rail', cutout=False)
    pane('safety_fence', post=[cube([7.25, 0, 7.25], [8.75, 16, 8.75], 'post')],
         side=[cube([8, 0, 0], [8, 16, 8], 'mesh', faces_=['east', 'west'])], texture='safety_fence', cutout=True,
         extra={'post': 'minecraft:block/green_terracotta'})
    for kind, im in stains():
        save('textures/block/stain_%s.png' % kind, im)
    save('textures/block/elk_fur.png', fur())
    save('textures/block/life_ring.png', ring())
    save('textures/block/hard_hat.png', hardhat())


def pane(name, post, side, texture, cutout, extra=None):
    tex = {'rail': '%s:block/%s' % (MOD, texture), 'mesh': '%s:block/%s' % (MOD, texture), 'particle': '%s:block/%s' % (MOD, texture)}
    tex.update(extra or {})
    rt = {'render_type': 'minecraft:cutout'} if cutout else {}
    write('models/block/%s_post.json' % name, dict({'ambientocclusion': False, 'textures': tex, 'elements': post}, **rt))
    write('models/block/%s_side.json' % name, dict({'ambientocclusion': False, 'textures': tex, 'elements': side}, **rt))
    parts = [{'apply': {'model': '%s:block/%s_post' % (MOD, name)}}]
    for facing, rot in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
        apply = {'model': '%s:block/%s_side' % (MOD, name)}
        if rot:
            apply['y'] = rot
        parts.append({'when': {facing: 'true'}, 'apply': apply})
    write('blockstates/%s.json' % name, {'multipart': parts})


PARTICLE = {'particle': '%s:block/elk_fur' % MOD}


def props():
    """New literary prop kinds; the carcasses and bodies are drawn by the block entity renderer."""
    fur_tex = {'fur': '%s:block/elk_fur' % MOD, 'particle': '%s:block/elk_fur' % MOD}
    hide = [cube([0, 0, 1], [16, 2, 15], 'fur'), cube([1, 2, 2], [15, 4, 14], 'fur'), cube([3, 4, 4], [13, 5, 12], 'fur'),
            cube([12, 0, 0], [16, 1, 3], 'fur'), cube([0, 0, 13], [4, 1, 16], 'fur'), cube([14, 2, 5], [16, 3, 9], 'fur')]
    models = {}
    for stage in range(4):
        models[('elk_hide', stage)] = {'ambientocclusion': True, 'textures': fur_tex, 'elements': hide}
        for kind in ('carcass', 'guest_body', 'crew_body'):
            models[(kind, stage)] = {'textures': dict(PARTICLE)}
        kind = ['pool', 'smear', 'spatter', 'prints'][stage]
        models[('stain', stage)] = {'render_type': 'minecraft:cutout', 'ambientocclusion': False,
                                    'textures': {'stain': '%s:block/stain_%s' % (MOD, kind), 'particle': '%s:block/stain_%s' % (MOD, kind)},
                                    'elements': [cube([0, .05, 0], [16, .05, 16], 'stain', faces_=['up'])]}
        ring_tex = {'ring': '%s:block/life_ring' % MOD, 'particle': '%s:block/life_ring' % MOD}
        models[('life_ring', stage)] = {'textures': ring_tex, 'elements': [
            cube([3, 2, 14], [13, 4, 16], 'ring'), cube([3, 12, 14], [13, 14, 16], 'ring'),
            cube([2, 4, 14], [4, 12, 16], 'ring'), cube([12, 4, 14], [14, 12, 16], 'ring'),
            cube([7, 14, 15], [9, 15, 16], 'ring')]}
        hat_tex = {'hat': '%s:block/hard_hat' % MOD, 'particle': '%s:block/hard_hat' % MOD}
        tilt = {'origin': [8, 0, 8], 'axis': 'z', 'angle': [0, 22.5, -22.5, 0][stage]}
        models[('hard_hat', stage)] = {'textures': hat_tex, 'elements': [
            cube([3, 0, 3], [13, 1, 13], 'hat', rot=tilt), cube([4, 1, 4], [12, 4, 12], 'hat', rot=tilt), cube([5, 4, 5], [11, 5, 11], 'hat', rot=tilt)]}
    variants = {}
    for (kind, stage), model in models.items():
        name = 'literary_%s_%d' % (kind, stage)
        write('models/block/%s.json' % name, model)
        for facing, rot in (('north', 0), ('east', 90), ('south', 180), ('west', 270)):
            v = {'model': '%s:block/%s' % (MOD, name)}
            if rot:
                v['y'] = rot
            variants['facing=%s,kind=%s,stage=%d' % (facing, kind, stage)] = v
    path = A / 'blockstates/literary_prop.json'
    state = json.loads(path.read_text())
    state['variants'].update(variants)
    write('blockstates/literary_prop.json', state)
    return len(state['variants'])


def lang():
    path = A / 'lang/en_us.json'
    data = json.loads(path.read_text())
    names = {'yacht_hull': 'Yacht hull', 'yacht_hull_stripe': 'Yacht boot stripe', 'yacht_boot': 'Yacht waterline',
             'yacht_antifoul': 'Antifouling', 'yacht_teak': 'Teak deck', 'yacht_salon_panel': 'Salon panelling',
             'yacht_carpet': 'Salon carpet', 'yacht_cushion': 'Deck cushion', 'yacht_canvas': 'Bimini canvas',
             'yacht_window': 'Tinted saloon glass', 'yacht_porthole': 'Porthole', 'yacht_rail': 'Stainless rail',
             'site_siding': 'Site trailer siding', 'safety_fence': 'Safety fence', 'cave_drag_mud': 'Dragged mud'}
    for k, v in names.items():
        data['block.%s.%s' % (MOD, k)] = v
    write('lang/en_us.json', data)


def preview(prop_variants):
    paths = [A / 'textures/block' / ('%s.png' % n) for n in (
        'yacht_hull', 'yacht_hull_stripe', 'yacht_boot', 'yacht_antifoul', 'yacht_teak', 'yacht_salon_panel', 'yacht_carpet',
        'yacht_cushion', 'yacht_canvas', 'yacht_window', 'yacht_porthole', 'yacht_rail', 'site_siding', 'safety_fence',
        'cave_drag_mud', 'elk_fur', 'stain_pool', 'stain_smear', 'stain_spatter', 'stain_prints', 'life_ring', 'hard_hat')]
    ents = [A / 'textures/entity' / ('literary_%s.png' % n) for n in (
        'killer', 'elk_carcass', 'elk_carcass_skinned', 'guest_0', 'guest_1', 'guest_2', 'guest_3', 'crew_0', 'crew_1', 'crew_2', 'crew_3')]
    sheet = Image.new('RGB', (8 * 72, 3 * 80 + 6 * 136), (29, 27, 25))
    d = ImageDraw.Draw(sheet)
    for i, p in enumerate(paths):
        x, y = (i % 8) * 72, (i // 8) * 80
        sheet.paste(Image.open(p).convert('RGBA').resize((64, 64), Image.Resampling.NEAREST), (x + 4, y + 2), Image.open(p).convert('RGBA').resize((64, 64), Image.Resampling.NEAREST))
        d.text((x + 3, y + 67), p.stem[:12], fill=(217, 205, 181))
    for i, p in enumerate(ents):
        x, y = (i % 2) * 288, 3 * 80 + (i // 2) * 136
        im = Image.open(p).convert('RGBA').resize((256, 128), Image.Resampling.NEAREST)
        sheet.paste(im, (x + 4, y + 2), im)
        d.text((x + 4, y + 122), p.stem, fill=(217, 205, 181))
    out = ROOT / 'art/elk_two_stage_0450.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out)


if __name__ == '__main__':
    blocks()
    guests()
    crew()
    killer()
    elk_atlas('elk_carcass', False)
    elk_atlas('elk_carcass_skinned', True)
    n = props()
    lang()
    preview(n)
    print(json.dumps({'prop_variants': n}))
