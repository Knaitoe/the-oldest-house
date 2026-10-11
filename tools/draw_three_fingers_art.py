"""Three Fingers (0.4.81): the withered paw's item art and the five people of the cottage, drawn in code.

This replaces the placeholders that generate_paw_assets.py writes when a file is missing. That generator never
overwrites an existing file, so the art here stays put however often it runs. Run this tool again only to redraw.

The skins use the renderer's 128 x 64 logical layout, exported at 512 x 256 like the Last Stop's. The cast model uses the
classic humanoid islands (the left limbs mirror the right ones), plus a coat extension at (64, 28) and a gown at (96, 28).
Walter, the sergeant and the man from the mill wear the coat; Ruth wears the gown; Will wears neither.

Usage: python3 tools/draw_three_fingers_art.py [--preview DIR]
"""
from pathlib import Path
import hashlib
import json
import math
import sys

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
S = 4  # pixels per logical texel
W, H = 128 * S, 64 * S


# ------------------------------------------------------------------------------------------------ canvas helpers
class Skin:
    def __init__(self, seed):
        self.a = np.zeros((H, W, 4), dtype=np.float32)
        self.rng = np.random.default_rng(seed)

    def px(self, x0, y0, x1, y1):
        """A logical rectangle to pixel bounds, covering every pixel it touches."""
        return max(0, math.floor(x0 * S)), max(0, math.floor(y0 * S)), min(W, math.ceil(x1 * S)), min(H, math.ceil(y1 * S))

    def fill(self, box, color, noise=.05, grain=None, shade=0.0, seed=None):
        """Paint a pixel box with a base colour, per-pixel noise and an optional grain."""
        x0, y0, x1, y1 = box
        if x1 <= x0 or y1 <= y0:
            return
        h, w = y1 - y0, x1 - x0
        rng = self.rng if seed is None else np.random.default_rng(seed)
        base = np.array(color[:3], dtype=np.float32)
        n = 1 + rng.normal(0, noise, (h, w, 1)).astype(np.float32)
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
        if grain == 'weave':
            n *= 1 + .025 * ((xx.astype(int) % 2) * 2 - 1)[..., None] + .015 * ((yy.astype(int) % 2) * 2 - 1)[..., None]
        elif grain == 'twill':
            n *= 1 + .04 * (((xx + yy).astype(int) % 4 < 2) * 2 - 1)[..., None]
        elif grain == 'knit':
            rib = ((xx.astype(int) // 2) % 2) * 2 - 1
            row = ((yy.astype(int) // 3) % 2)
            n *= 1 + (.07 * rib + .03 * row)[..., None]
        elif grain == 'tweed':
            n *= 1 + rng.normal(0, .07, (h, w, 1)).astype(np.float32)
            fleck = rng.random((h, w)) < .05
            n[fleck] *= 1.25
        elif grain == 'hair':
            strands = rng.normal(0, .09, (1, w, 1)).astype(np.float32)
            n *= 1 + strands + .03 * np.sin(yy * .9 + rng.random() * 6)[..., None]
        if shade:
            # Darker toward the edges of the face, as if the cloth turned away from the light.
            ex = np.minimum(xx, w - 1 - xx) / max(1, w / 2)
            n *= (1 - shade * (1 - np.clip(ex * 3, 0, 1)))[..., None]
        rgb = np.clip(base * n, 0, 255)
        self.a[y0:y1, x0:x1, :3] = rgb
        self.a[y0:y1, x0:x1, 3] = 255

    def rect(self, x0, y0, x1, y1, color, alpha=255):
        """A flat pixel rectangle (inclusive-exclusive), for details."""
        x0, y0, x1, y1 = max(0, x0), max(0, y0), min(W, x1), min(H, y1)
        if x1 <= x0 or y1 <= y0:
            return
        self.a[y0:y1, x0:x1, :3] = color[:3]
        self.a[y0:y1, x0:x1, 3] = alpha

    def dot(self, x, y, color, alpha=255):
        if 0 <= x < W and 0 <= y < H:
            self.a[y, x, :3] = color[:3]
            self.a[y, x, 3] = alpha

    def tint(self, x0, y0, x1, y1, factor):
        """Multiply what is already there, keeping its texture."""
        x0, y0, x1, y1 = max(0, x0), max(0, y0), min(W, x1), min(H, y1)
        if x1 <= x0 or y1 <= y0:
            return
        self.a[y0:y1, x0:x1, :3] = np.clip(self.a[y0:y1, x0:x1, :3] * factor, 0, 255)

    def blend(self, x0, y0, x1, y1, color, amount):
        x0, y0, x1, y1 = max(0, x0), max(0, y0), min(W, x1), min(H, y1)
        if x1 <= x0 or y1 <= y0:
            return
        c = np.array(color[:3], dtype=np.float32)
        self.a[y0:y1, x0:x1, :3] = self.a[y0:y1, x0:x1, :3] * (1 - amount) + c * amount

    def clear(self, x0, y0, x1, y1):
        x0, y0, x1, y1 = max(0, x0), max(0, y0), min(W, x1), min(H, y1)
        self.a[y0:y1, x0:x1] = 0

    def image(self):
        return Image.fromarray(np.round(self.a).astype(np.uint8), 'RGBA')


def net(u, v, w, h, d):
    """The six faces of a cube on the texture, in logical texels: top, bottom, right, front, left, back."""
    return {
        'top': (u + d, v, u + d + w, v + d),
        'bottom': (u + d + w, v, u + d + 2 * w, v + d),
        'right': (u, v + d, u + d, v + d + h),
        'front': (u + d, v + d, u + d + w, v + d + h),
        'left': (u + d + w, v + d, u + 2 * d + w, v + d + h),
        'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


HEAD = net(0, 0, 8, 8, 8)
HAT = net(32, 0, 8, 8, 8)
BODY = net(16, 16, 8, 12, 4)
ARM = net(40, 16, 4, 12, 4)
LEG = net(0, 16, 4, 12, 4)
COAT = net(64, 28, 8.8, 17, 4.8)
GOWN = net(96, 28, 10.4, 14, 6.4)
SIDES = ('right', 'front', 'left', 'back')


def shade(c, f):
    return tuple(max(0, min(255, round(v * f))) for v in c[:3])


# ------------------------------------------------------------------------------------------------ heads
def head(sk, p):
    """A head from a description: skin, hair, hairline, eyes, brows, mouth and any whiskers or spectacles."""
    skin, hair = p['skin'], p['hair']
    for name, box in HEAD.items():
        sk.fill(sk.px(*box), skin, noise=.035)
    # The scalp and back of the head.
    top = sk.px(*HEAD['top'])
    if p.get('bald'):
        sk.fill(top, shade(skin, .97), noise=.03)
        x0, y0, x1, y1 = top
        sk.fill((x0, y0, x1, y0 + 9), hair, noise=.08, grain='hair')
        sk.fill((x0, y0, x0 + 6, y1), hair, noise=.08, grain='hair')
        sk.fill((x1 - 6, y0, x1, y1), hair, noise=.08, grain='hair')
        sk.blend(x0 + 9, y0 + 12, x1 - 9, y1 - 4, (255, 236, 220), .12)
    else:
        sk.fill(top, hair, noise=.08, grain='hair')
        if p.get('parting'):
            x0, y0, x1, y1 = top
            sk.rect(x0 + 15, y0, x0 + 16, y1, shade(skin, .85))
            sk.tint(x0 + 13, y0, x0 + 15, y1, .82)
            sk.tint(x0 + 16, y0, x0 + 18, y1, .82)
    bx0, by0, bx1, by1 = sk.px(*HEAD['back'])
    sk.fill((bx0, by0, bx1, by1), hair, noise=.08, grain='hair')
    nape = p.get('nape', 24)
    if nape < 32:
        sk.fill((bx0, by0 + nape, bx1, by1), shade(skin, .9), noise=.03)
        sk.tint(bx0, by0 + nape, bx1, by0 + nape + 2, .8)
    if p.get('bun'):
        cx, cy = bx0 + 16, by0 + 13
        for y in range(by0 + 6, by0 + 21):
            for x in range(bx0 + 8, bx1 - 8):
                r = ((x - cx) / 8.5) ** 2 + ((y - cy) / 7) ** 2
                if r <= 1:
                    coil = .93 + .07 * math.sin(math.atan2(y - cy, x - cx) * 2 + r * 4) + .05 * (1 - r) - .04 * ((x + y) % 3 == 0)
                    sk.dot(x, y, shade(hair, coil))
                elif r <= 1.2:
                    sk.dot(x, y, shade(hair, .7))
    # The bottom: under the chin, the hair behind.
    ux0, uy0, ux1, uy1 = sk.px(*HEAD['bottom'])
    sk.fill((ux0, uy0, ux1, uy1), shade(skin, .78), noise=.03)
    sk.fill((ux0, uy1 - 10, ux1, uy1), hair if nape >= 28 else shade(skin, .7), noise=.06, grain='hair')
    # The sides: hair over the top and back, an ear in the middle, whiskers at the front.
    for side in ('right', 'left'):
        x0, y0, x1, y1 = sk.px(*HEAD[side])
        front_right = side == 'right'  # the right face meets the front at its right edge
        line = p.get('side_hair', 9)
        sk.fill((x0, y0, x1, y0 + line), hair, noise=.08, grain='hair')
        back = (x0, x0 + 14) if front_right else (x1 - 14, x1)
        sk.fill((back[0], y0, back[1], y0 + nape), hair, noise=.08, grain='hair')
        if p.get('long_side'):
            sk.fill((x0, y0, x1, y0 + p['long_side']), hair, noise=.08, grain='hair')
            fr = (x1 - 6, x1) if front_right else (x0, x0 + 6)
            sk.fill((fr[0], y0 + p['long_side'], fr[1], y0 + p['long_side'] + 2), shade(skin, .92), noise=.03)
        # The ear.
        ex0 = x0 + 12 if front_right else x1 - 18
        if not p.get('ears_hidden'):
            sk.fill((ex0, y0 + 13, ex0 + 6, y0 + 22), shade(skin, .96), noise=.03)
            sk.rect(ex0 + 1, y0 + 15, ex0 + 5, y0 + 20, shade(skin, .82))
            sk.rect(ex0 + 2, y0 + 16, ex0 + 4, y0 + 19, shade(skin, .7))
            sk.rect(ex0, y0 + 13, ex0 + 6, y0 + 14, shade(skin, .86))
        if p.get('whiskers'):
            wx = (x1 - 9, x1) if front_right else (x0, x0 + 9)
            sk.fill((wx[0], y0 + 8, wx[1], y0 + p['whiskers']), p.get('whisker_hair', hair), noise=.1, grain='hair')
        if p.get('spectacles'):
            # The arm of the spectacles runs back to the ear.
            ax = (ex0 + 6, x1) if front_right else (x0, ex0)
            sk.rect(ax[0], y0 + 15, ax[1], y0 + 16, p['spectacles'])
        if p.get('ruddy'):
            cx = (x1 - 6, x1) if front_right else (x0, x0 + 6)
            sk.blend(cx[0], y0 + 17, cx[1], y0 + 24, (196, 82, 66), .18)
        # Jaw shadow.
        sk.tint(x0, y1 - 3, x1, y1, .9)
    face(sk, p)


def face(sk, p):
    x0, y0, x1, y1 = sk.px(*HEAD['front'])
    skin, hair = p['skin'], p['hair']
    F = lambda x, y: (x0 + x, y0 + y)
    def r(ax, ay, bx, by, c, a=255):
        sk.rect(x0 + ax, y0 + ay, x0 + bx, y0 + by, c, a)
    def d(x, y, c):
        sk.dot(x0 + x, y0 + y, c)
    # Soft modelling: cheeks, under the brow, the sides of the face.
    sk.tint(x0, y0, x0 + 3, y1, .92)
    sk.tint(x1 - 3, y0, x1, y1, .92)
    sk.tint(x0 + 3, y0 + 27, x1 - 3, y1, .95)
    if p.get('ruddy'):
        sk.blend(x0 + 3, y0 + 18, x0 + 10, y0 + 24, (198, 80, 64), .22)
        sk.blend(x1 - 10, y0 + 18, x1 - 3, y0 + 24, (198, 80, 64), .22)
        sk.blend(x0 + 14, y0 + 19, x0 + 18, y0 + 23, (190, 78, 64), .2)
    elif p.get('cheeks'):
        sk.blend(x0 + 4, y0 + 19, x0 + 10, y0 + 24, (196, 112, 98), p['cheeks'])
        sk.blend(x1 - 10, y0 + 19, x1 - 4, y0 + 24, (196, 112, 98), p['cheeks'])
    if p.get('pale'):
        sk.blend(x0, y0, x1, y1, (226, 222, 214), p['pale'])
    if p.get('soot'):
        for (sx, sy) in p['soot']:
            sk.blend(x0 + sx, y0 + sy, x0 + sx + 3, y0 + sy + 2, (70, 60, 54), .35)
    # Hair at the top of the face.
    hl = p.get('hairline', 7)
    if p.get('bald'):
        r(0, 0, 4, 12, hair)
        r(28, 0, 32, 12, hair)
        sk.tint(x0 + 4, y0, x1 - 4, y0 + 3, .95)
    elif p.get('parting'):
        for x in range(32):
            dip = int(3.5 * abs(x - 15.5) / 15.5)
            sk.fill((x0 + x, y0, x0 + x + 1, y0 + hl + dip - 1), hair, noise=.07, grain='hair')
        r(15, 0, 17, 2, shade(skin, .86))
        r(0, 0, 3, 18, hair)
        r(29, 0, 32, 18, hair)
    else:
        fringe = p.get('fringe')
        for x in range(32):
            extra = 0
            if fringe:
                extra = int(fringe[0] + fringe[1] * math.sin(x * .9 + 1.3) + fringe[1] * math.sin(x * 1.7))
            sk.fill((x0 + x, y0, x0 + x + 1, y0 + max(2, hl + extra)), hair, noise=.07, grain='hair')
        r(0, 0, 2, 13, hair)
        r(30, 0, 32, 13, hair)
    if p.get('forehead_lines'):
        for ly in p['forehead_lines']:
            for x in range(9, 23):
                if (x + ly) % 7 != 0:
                    sk.tint(x0 + x, y0 + ly, x0 + x + 1, y0 + ly + 1, .88)
    # Brows.
    brow = p.get('brow', shade(hair, .85))
    bh = p.get('brow_h', 2)
    by = p.get('brow_y', 12)
    tilt = p.get('brow_tilt', 0)
    for i in range(7):
        lift = round(tilt * (i - 3) / 3)
        r(5 + i, by - lift, 6 + i, by - lift + bh, brow)
        r(26 - i, by - lift, 27 - i, by - lift + bh, brow)
    # Eyes: a lid line, the white, the iris and a catchlight; a shadow under.
    ey = p.get('eye_y', 15)
    iris = p['iris']
    small = p.get('small_eyes')
    for ex, inner in ((6, 1), (20, 0)):
        sk.tint(x0 + ex - 1, y0 + ey - 1, x0 + ex + 7, y0 + ey, .86)
        w = 4 if small else 6
        off = 1 if small else 0
        r(ex + off, ey, ex + off + w, ey + 3, (226, 220, 208))
        ix = ex + off + (w - 3) // 2 + (1 if inner else 0) - (0 if inner else 0)
        r(ix, ey, ix + 3, ey + 3, iris)
        r(ix + 1, ey + 1, ix + 2, ey + 3, shade(iris, .45))
        d(ix, ey, (236, 232, 222))
        r(ex + off - 1, ey - 1, ex + off + w + 1, ey, p.get('lid', (58, 40, 32)))
        if p.get('heavy_lids'):
            r(ex + off, ey, ex + off + w, ey + 1, shade(skin, .74))
        sk.tint(x0 + ex, y0 + ey + 3, x0 + ex + 6, y0 + ey + 4, .86)
        if p.get('bags'):
            sk.tint(x0 + ex + 1, y0 + ey + 4, x0 + ex + 6, y0 + ey + 5, .9)
        if p.get('crows_feet'):
            cx = ex - 2 if inner else ex + 7
            for k in range(3):
                d(cx + (0 if inner else 1), ey + k, shade(skin, .8))
    if p.get('spectacles'):
        g = p['spectacles']
        for ex in (5, 19):
            for x in range(ex, ex + 8):
                d(x, ey - 2, g)
                d(x, ey + 4, g)
            for y in range(ey - 2, ey + 5):
                d(ex, y, g)
                d(ex + 7, y, g)
            sk.blend(x0 + ex + 1, y0 + ey - 1, x0 + ex + 7, y0 + ey, (255, 250, 236), .25)
        for x in range(13, 19):
            d(x, ey - 1, g)
        r(0, ey, 5, ey + 1, g)
        r(27, ey, 32, ey + 1, g)
    # The nose: a lit ridge, a shadow side, nostrils.
    nx, ny = 14, ey + 1
    sk.tint(x0 + nx - 1, y0 + ny, x0 + nx, y0 + ny + 6, .9)
    sk.blend(x0 + nx, y0 + ny + 1, x0 + nx + 2, y0 + ny + 6, (255, 236, 214), .12)
    sk.tint(x0 + nx + 3, y0 + ny + 2, x0 + nx + 4, y0 + ny + 7, .86)
    r(nx - 1, ny + 6, nx + 5, ny + 7, shade(skin, .82))
    d(nx, ny + 6, shade(skin, .6))
    d(nx + 3, ny + 6, shade(skin, .6))
    if p.get('big_nose'):
        sk.blend(x0 + nx - 1, y0 + ny + 4, x0 + nx + 5, y0 + ny + 6, (204, 96, 80), .18)
    # Lines beside the nose and mouth for the older faces.
    if p.get('folds'):
        for k in range(6):
            d(nx - 3 - k // 3, ny + 6 + k, shade(skin, .84))
            d(nx + 6 + k // 3, ny + 6 + k, shade(skin, .84))
    # The mouth.
    my = p.get('mouth_y', 24)
    mw = p.get('mouth_w', 8)
    mx = 16 - mw // 2
    lip = p.get('lip', shade(skin, .7))
    r(mx, my, mx + mw, my + 1, shade(lip, .7))
    r(mx + 1, my + 1, mx + mw - 1, my + 2, lip)
    if p.get('mouth_down'):
        d(mx - 1, my + 1, shade(lip, .7))
        d(mx + mw, my + 1, shade(lip, .7))
    if p.get('lips'):
        r(mx + 1, my - 1, mx + mw - 1, my, shade(p['lips'], .85))
        r(mx + 1, my + 1, mx + mw - 1, my + 2, p['lips'])
    sk.tint(x0 + mx + 1, y0 + my + 3, x0 + mx + mw - 1, y0 + my + 4, .9)
    # Whiskers, moustache, beard.
    whisk = p.get('whisker_hair', hair)
    if p.get('whiskers'):
        for y in range(9, p['whiskers'] + 1):
            spread = 3 if y < 20 else 3 + (y - 20) // 2
            sk.fill((x0, y0 + y, x0 + spread, y0 + y + 1), whisk, noise=.12, grain='hair')
            sk.fill((x1 - spread, y0 + y, x1, y0 + y + 1), whisk, noise=.12, grain='hair')
    if p.get('moustache'):
        kind = p['moustache']
        mc = p.get('moustache_hair', whisk)
        if kind == 'walrus':
            for x in range(8, 24):
                droop = 0 if 11 <= x <= 20 else (1 if 9 <= x <= 22 else 2)
                sk.fill((x0 + x, y0 + my - 2, x0 + x + 1, y0 + my + 1 + droop), mc, noise=.12, grain='hair')
            r(15, my - 2, 17, my - 1, shade(mc, .8))
        elif kind == 'waxed':
            for x in range(9, 23):
                sk.fill((x0 + x, y0 + my - 2, x0 + x + 1, y0 + my), mc, noise=.1, grain='hair')
            for k, x in enumerate((8, 7, 6, 5)):
                d(x, my - 2 - k // 2, mc)
                d(31 - x, my - 2 - k // 2, mc)
            r(15, my - 2, 17, my - 1, shade(mc, .78))
        elif kind == 'thin':
            r(11, my - 2, 21, my - 1, mc)
            r(15, my - 2, 17, my - 1, shade(mc, .8))
    if p.get('stubble'):
        x, y = x0 + 4, y0 + my - 3
        for yy in range(y, y1 - 1):
            for xx in range(x, x1 - 4):
                if sk.rng.random() < p['stubble']:
                    sk.tint(xx, yy, xx + 1, yy + 1, .84)


# ------------------------------------------------------------------------------------------------ clothes
def cloth(sk, box, color, grain='weave', noise=.05, shade_=.12):
    sk.fill(sk.px(*box), color, noise=noise, grain=grain, shade=shade_)


def every(netd, names=SIDES + ('top', 'bottom')):
    return [netd[n] for n in names]


def button(sk, x, y, c, size=2):
    sk.rect(x, y, x + size, y + size, c)
    sk.dot(x, y, shade(c, 1.45))
    sk.dot(x + size - 1, y + size - 1, shade(c, .6))


def seam(sk, x0, y0, x1, y1, f=.78):
    sk.tint(x0, y0, x1, y1, f)


def limbs(sk, p):
    """Arms and legs: sleeves with cuffs and hands, trousers or stockings with boots."""
    skin = p['skin']
    # Arms.
    for name in SIDES + ('top',):
        cloth(sk, ARM[name], p['sleeve'], grain=p.get('sleeve_grain', 'weave'))
    ax0, ay0, ax1, ay1 = sk.px(*ARM['bottom'])
    sk.fill((ax0, ay0, ax1, ay1), skin, noise=.04)
    roll = p.get('rolled')
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*ARM[name])
        if roll:
            sk.fill((x0, y0 + roll * S, x1, y1), skin, noise=.04)
            sk.fill((x0, y0 + roll * S - 6, x1, y0 + roll * S), shade(p['sleeve'], 1.05), noise=.05, grain='weave')
            seam(sk, x0, y0 + roll * S - 6, x1, y0 + roll * S - 5)
            seam(sk, x0, y0 + roll * S, x1, y0 + roll * S + 1, .85)
            if p.get('forearm_hair'):
                for k in range(14):
                    sk.tint(x0 + int(sk.rng.integers(0, x1 - x0)), y0 + roll * S + 3 + int(sk.rng.integers(0, 14)), 0, 0, 1)
        else:
            hand = 3 * S
            sk.fill((x0, y1 - hand, x1, y1), skin, noise=.04)
            sk.tint(x0, y1 - hand, x1, y1 - hand + 1, .8)
            if p.get('cuff'):
                sk.fill((x0, y1 - hand - 4, x1, y1 - hand), p['cuff'], noise=.03)
                seam(sk, x0, y1 - hand - 4, x1, y1 - hand - 3, .9)
            if p.get('sleeve_band'):
                sk.fill((x0, y1 - hand - 7, x1, y1 - hand - 4), p['sleeve_band'], noise=.05)
        if name == 'front' or name == 'back':
            # Knuckles and fingers on the hand.
            for k in (3, 7, 11):
                sk.tint(x0 + k, y1 - 5, x0 + k + 1, y1, .88)
        # A fold at the elbow.
        if not roll or roll * S > 6 * S:
            sk.tint(x0 + 2, y0 + 23, x1 - 2, y0 + 24, .86)
            sk.tint(x0 + 4, y0 + 25, x1 - 4, y0 + 26, .9)
    # Legs.
    for name in SIDES + ('top',):
        cloth(sk, LEG[name], p['trousers'], grain=p.get('trouser_grain', 'twill'))
    lx0, ly0, lx1, ly1 = sk.px(*LEG['bottom'])
    sk.fill((lx0, ly0, lx1, ly1), shade(p['boots'], .6), noise=.04)
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*LEG[name])
        bh = p.get('boot_h', 3) * S
        sk.fill((x0, y1 - bh, x1, y1), p['boots'], noise=.05)
        sk.fill((x0, y1 - 2, x1, y1), shade(p['boots'], .55), noise=.03)
        if p.get('boot_laces') and name == 'front':
            for k in range(y1 - bh + 2, y1 - 3, 3):
                sk.rect(x0 + 5, k, x1 - 5, k + 1, shade(p['boots'], 1.6))
        sk.blend(x0 + 3, y1 - bh + 1, x1 - 3, y1 - bh + 3, (255, 255, 255), .08)
        if p.get('turn_up') is None:
            sk.tint(x0, y1 - bh - 1, x1, y1 - bh, .7)
        if p.get('stripes'):
            for k in range(x0 + 2, x1, 5):
                sk.tint(k, y0, k + 1, y1 - bh, p['stripes'])
        if name == 'front':
            # The crease down the front of the trouser leg.
            sk.blend(x0 + 7, y0 + 2, x0 + 8, y1 - bh - 2, (255, 255, 255), .08)
            sk.tint(x0 + 8, y0 + 2, x0 + 9, y1 - bh - 2, .9)
        # A fold behind the knee and at the ankle.
        sk.tint(x0 + 2, y0 + 25, x1 - 2, y0 + 26, .88)
    tx0, ty0, tx1, ty1 = sk.px(*LEG['top'])
    sk.tint(tx0, ty0, tx1, ty1, .8)


def torso(sk, base, grain='weave'):
    for name in SIDES + ('top', 'bottom'):
        cloth(sk, BODY[name], base, grain=grain)


# ------------------------------------------------------------------------------------------------ the five
def walter():
    """Walter Dunning: an old man by his own fire, spectacles, white side-whiskers, a waistcoat and a long green cardigan."""
    sk = Skin(901)
    p = dict(skin=(214, 168, 138), hair=(206, 202, 196), whisker_hair=(222, 218, 210), bald=True, nape=26, side_hair=7,
             whiskers=26, spectacles=(150, 124, 70), iris=(92, 108, 120), brow=(206, 200, 190), brow_h=2, brow_y=12, brow_tilt=1,
             forehead_lines=(6, 9), crows_feet=True, folds=True, bags=True, cheeks=.14, mouth_w=8, lip=(156, 100, 88))
    head(sk, p)
    # Shirt, waistcoat, watch chain.
    shirt = (226, 220, 204)
    vest = (122, 92, 62)
    torso(sk, vest, 'tweed')
    x0, y0, x1, y1 = sk.px(*BODY['front'])
    sk.fill((x0 + 11, y0, x1 - 11, y0 + 14), shirt, noise=.03)
    sk.rect(x0 + 13, y0, x1 - 13, y0 + 2, shade(shirt, .88))
    button(sk, x0 + 15, y0 + 1, (196, 172, 92))
    for k in range(3):
        sk.tint(x0 + 11 + k, y0 + 14 - k * 4, x0 + 12 + k, y0 + 18 - k * 4, .7)
        sk.tint(x1 - 12 - k, y0 + 14 - k * 4, x1 - 11 - k, y0 + 18 - k * 4, .7)
    for by in range(17, 44, 6):
        button(sk, x0 + 15, y0 + by, (70, 50, 34))
    seam(sk, x0 + 16, y0 + 14, x0 + 17, y1 - 4, .7)
    for k in range(8):
        sk.dot(x0 + 7 + k, y0 + 29 + round(2 * math.sin(k / 7 * math.pi)), (210, 178, 86))
    sk.rect(x0 + 4, y0 + 27, x0 + 9, y0 + 28, shade(vest, .6))
    sk.rect(x1 - 9, y0 + 27, x1 - 4, y0 + 28, shade(vest, .6))
    sk.fill((x0, y1 - 4, x1, y1), (70, 54, 42), noise=.05, grain='twill')
    bx0, by0, bx1, by1 = sk.px(*BODY['back'])
    sk.fill((bx0, by0, bx1, by1), shade(vest, .7), noise=.04, grain='weave')
    sk.rect(bx0 + 10, by0 + 30, bx1 - 10, by0 + 32, shade(vest, .5))
    limbs(sk, dict(skin=p['skin'], sleeve=(92, 106, 74), sleeve_grain='knit', cuff=shirt, trousers=(70, 54, 42),
                   trouser_grain='twill', boots=(112, 38, 40), boot_h=2))
    # Carpet slippers: a little pattern on the toe.
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*LEG[name])
        for k in range(x0 + 1, x1 - 1, 4):
            sk.dot(k, y1 - 6, (170, 136, 70))
    # The long cardigan: open down the front, pockets low on either side.
    knit = (92, 106, 74)
    for name in SIDES + ('top', 'bottom'):
        cloth(sk, COAT[name], knit, grain='knit', shade_=.1)
    x0, y0, x1, y1 = sk.px(*COAT['front'])
    sk.clear(x0 + 12, y0, x1 - 12, y1)
    for x in (x0 + 10, x0 + 11, x1 - 12, x1 - 11):
        sk.tint(x, y0, x + 1, y1, .78)
    for px_ in (x0 + 3, x1 - 11):
        sk.fill((px_, y0 + 44, px_ + 8, y0 + 54), shade(knit, .95), noise=.05, grain='knit')
        seam(sk, px_, y0 + 44, px_ + 8, y0 + 45, .72)
    sk.tint(x0, y1 - 4, x1, y1, .85)
    for name in ('right', 'left', 'back'):
        x0, y0, x1, y1 = sk.px(*COAT[name])
        sk.tint(x0, y1 - 4, x1, y1, .85)
        sk.tint(x0, y0 + 40, x1, y0 + 41, .9)
    # The cardigan's top is its shoulders; nothing of it shows below, where it is open.
    x0, y0, x1, y1 = sk.px(*COAT['bottom'])
    sk.clear(x0, y0, x1, y1)
    return sk


def ruth():
    """Ruth Dunning: greying hair parted and pinned in a bun, a dark plum dress, a white collar and cameo, and an apron."""
    sk = Skin(902)
    p = dict(skin=(222, 180, 150), hair=(118, 102, 90), parting=True, bun=True, hairline=6, nape=30, side_hair=12, long_side=16,
             iris=(84, 74, 62), brow=(104, 88, 76), brow_h=1, brow_y=13, brow_tilt=1, crows_feet=True, bags=True, folds=True,
             cheeks=.12, mouth_w=6, lip=(170, 104, 96), lips=(176, 106, 100), mouth_down=True, ears_hidden=True)
    head(sk, p)
    # Grey streaks through the brown.
    for box in (HEAD['top'], HEAD['back'], HEAD['right'], HEAD['left']):
        x0, y0, x1, y1 = sk.px(*box)
        for k in range(14):
            sx = int(sk.rng.integers(x0, x1))
            sk.blend(sx, y0, sx + 1, y0 + 12, (196, 192, 186), .5)
    x0, y0, x1, y1 = sk.px(*HEAD['front'])
    for sx in (x0 + 9, x0 + 12, x1 - 11, x1 - 8):
        sk.blend(sx, y0, sx + 1, y0 + 7, (198, 194, 188), .55)
    # The bun stands out a little on the hat layer.
    bx0, by0, bx1, by1 = sk.px(*HAT['back'])
    hair = p['hair']
    cx, cy = bx0 + 16, by0 + 14
    for y in range(by0 + 8, by0 + 21):
        for x in range(bx0 + 10, bx1 - 10):
            rr = ((x - cx) / 6.5) ** 2 + ((y - cy) / 5.5) ** 2
            if rr <= 1:
                sk.dot(x, y, shade(hair, .95 + .06 * math.sin(math.atan2(y - cy, x - cx) * 2 + rr * 4) + .06 * (1 - rr)))
    dress = (82, 50, 64)
    torso(sk, dress, 'weave')
    x0, y0, x1, y1 = sk.px(*BODY['front'])
    # A white collar, the cameo at the throat, tucks down the bodice.
    collar = (222, 216, 202)
    sk.fill((x0 + 6, y0, x1 - 6, y0 + 4), collar, noise=.03)
    for k in range(5):
        sk.rect(x0 + 6 + k, y0 + 4 + k // 2, x0 + 11 + k, y0 + 5 + k // 2, collar)
        sk.rect(x1 - 11 - k, y0 + 4 + k // 2, x1 - 6 - k, y0 + 5 + k // 2, collar)
    sk.rect(x0 + 14, y0 + 3, x0 + 18, y0 + 8, (196, 168, 102))
    sk.rect(x0 + 15, y0 + 4, x0 + 17, y0 + 7, (226, 210, 196))
    for by in range(11, 38, 5):
        button(sk, x0 + 15, y0 + by, (46, 30, 38))
    for k in (8, 23):
        seam(sk, x0 + k, y0 + 12, x0 + k + 1, y1 - 6, .82)
    # The apron's bib and its straps over the shoulders.
    apron = (208, 202, 186)
    sk.fill((x0 + 9, y0 + 30, x1 - 9, y1), apron, noise=.04, grain='weave')
    for xs in (x0 + 7, x1 - 9):
        sk.fill((xs, y0 + 8, xs + 2, y0 + 30), apron, noise=.03)
    sk.fill((x0, y1 - 6, x1, y1 - 3), shade(apron, .96), noise=.03)
    bx0, by0, bx1, by1 = sk.px(*BODY['back'])
    for xs in (bx0 + 9, bx1 - 11):
        sk.fill((xs, by0, xs + 2, by1 - 6), apron, noise=.03)
    sk.fill((bx0, by1 - 6, bx1, by1 - 3), shade(apron, .96), noise=.03)
    sk.fill((bx0 + 12, by1 - 9, bx1 - 12, by1), apron, noise=.03)
    seam(sk, bx0 + 16, by1 - 9, bx0 + 17, by1, .8)
    for name in ('right', 'left'):
        x0, y0, x1, y1 = sk.px(*BODY[name])
        sk.fill((x0, y1 - 6, x1, y1 - 3), shade(apron, .96), noise=.03)
    limbs(sk, dict(skin=p['skin'], sleeve=dress, cuff=collar, trousers=(40, 34, 36), trouser_grain='weave', boots=(30, 26, 26),
                   boot_h=4, boot_laces=True))
    # The skirt, its apron in front and the bow behind.
    for name in SIDES + ('top', 'bottom'):
        cloth(sk, GOWN[name], dress, grain='twill', shade_=.08)
    x0, y0, x1, y1 = sk.px(*GOWN['back'])
    sk.fill((x0, y0, W, y1), dress, noise=.05, grain='twill')
    for k in range(x0 + 4, W, 7):
        sk.tint(k, y0 + 6, k + 1, y1, .84)
    sk.fill((x0 + 14, y0, x0 + 28, y0 + 6), apron, noise=.03)
    sk.rect(x0 + 19, y0 + 1, x0 + 23, y0 + 5, shade(apron, .82))
    sk.fill((x0 + 16, y0 + 6, x0 + 19, y0 + 18), apron, noise=.03)
    sk.fill((x0 + 23, y0 + 6, x0 + 26, y0 + 16), apron, noise=.03)
    x0, y0, x1, y1 = sk.px(*GOWN['front'])
    for k in range(x0 + 3, x1, 7):
        sk.tint(k, y0 + 4, k + 1, y1, .84)
    sk.fill((x0 + 6, y0, x1 - 6, y1 - 4), apron, noise=.04, grain='weave')
    sk.tint(x0 + 6, y0, x1 - 6, y0 + 2, .86)
    seam(sk, x0 + 20, y0 + 6, x0 + 21, y1 - 6, .9)
    sk.fill((x0 + 9, y0 + 16, x0 + 18, y0 + 26), shade(apron, .97), noise=.03)
    seam(sk, x0 + 9, y0 + 16, x0 + 18, y0 + 17, .8)
    sk.tint(x0 + 6, y1 - 6, x1 - 6, y1 - 4, .9)
    sk.tint(x0, y1 - 3, x1, y1, .78)
    for name in ('right', 'left'):
        x0, y0, x1, y1 = sk.px(*GOWN[name])
        sk.fill((x0, y0, x1, y0 + 4), apron, noise=.03)
        sk.tint(x0, y1 - 3, x1, y1, .78)
    return sk


def will():
    """Will Dunning: young, dark tousled hair, a striped collarless shirt with rolled sleeves, braces, a red neckerchief."""
    sk = Skin(903)
    p = dict(skin=(218, 172, 138), hair=(62, 44, 32), hairline=8, fringe=(0, 1.1), nape=26, side_hair=10, iris=(70, 92, 66),
             brow=(52, 36, 28), brow_h=2, brow_y=12, cheeks=.12, mouth_w=8, lip=(170, 108, 94), soot=((4, 22), (23, 10)))
    head(sk, p)
    shirt = (214, 206, 186)
    torso(sk, shirt, 'weave')
    for name in SIDES + ('top',):
        x0, y0, x1, y1 = sk.px(*BODY[name])
        for k in range(x0 + 2, x1, 5):
            sk.blend(k, y0, k + 1, y1, (86, 104, 142), .45)
    x0, y0, x1, y1 = sk.px(*BODY['front'])
    # The neckerchief, knotted at the front.
    red = (150, 46, 40)
    sk.fill((x0 + 6, y0, x1 - 6, y0 + 4), red, noise=.06, grain='twill')
    sk.fill((x0 + 13, y0 + 4, x0 + 19, y0 + 10), shade(red, .9), noise=.06, grain='twill')
    sk.rect(x0 + 14, y0 + 4, x0 + 18, y0 + 6, shade(red, .7))
    for by in range(13, 40, 7):
        button(sk, x0 + 15, y0 + by, (196, 190, 172))
    seam(sk, x0 + 16, y0 + 10, x0 + 17, y1 - 5, .82)
    # Braces, a broad belt line where the trousers come up.
    braces = (88, 58, 40)
    for xs in (x0 + 6, x1 - 9):
        sk.fill((xs, y0, xs + 3, y1 - 5), braces, noise=.05)
        button(sk, xs, y1 - 7, (180, 160, 110))
    trousers = (88, 80, 70)
    sk.fill((x0, y1 - 5, x1, y1), trousers, noise=.05, grain='twill')
    bx0, by0, bx1, by1 = sk.px(*BODY['back'])
    for k in range(10):
        sk.rect(bx0 + 6 + k, by0 + k * 2, bx0 + 9 + k, by0 + k * 2 + 2, braces)
        sk.rect(bx1 - 9 - k, by0 + k * 2, bx1 - 6 - k, by0 + k * 2 + 2, braces)
    sk.fill((bx0 + 14, by0 + 20, bx1 - 14, by1 - 5), braces, noise=.05)
    sk.fill((bx0, by1 - 5, bx1, by1), trousers, noise=.05, grain='twill')
    sk.fill((bx0 + 4, by0, bx1 - 4, by0 + 3), red, noise=.06)
    for name in ('right', 'left'):
        x0, y0, x1, y1 = sk.px(*BODY[name])
        sk.fill((x0, y1 - 5, x1, y1), trousers, noise=.05, grain='twill')
        sk.fill((x0, y0, x1, y0 + 3), red, noise=.06)
    limbs(sk, dict(skin=p['skin'], sleeve=shirt, rolled=6, trousers=trousers, trouser_grain='twill', boots=(38, 32, 28),
                   boot_h=3, boot_laces=True))
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*ARM[name])
        for k in range(x0 + 2, x1, 5):
            sk.blend(k, y0, k + 1, y0 + 6 * S - 6, (86, 104, 142), .45)
        # Mill grime on the forearms and hands.
        sk.blend(x0 + 2, y1 - 9, x1 - 3, y1 - 6, (90, 80, 72), .25)
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*LEG[name])
        sk.blend(x0, y1 - 20, x1, y1 - 12, (120, 110, 96), .18)
    return sk


def sergeant():
    """Sergeant Coyle: big, sunburnt and red in the face, cropped grey hair, a waxed moustache, a buttoned dark frock coat
    with medal ribbons."""
    sk = Skin(904)
    p = dict(skin=(196, 136, 104), hair=(150, 140, 128), hairline=5, nape=24, side_hair=6, iris=(56, 44, 36), small_eyes=True,
             brow=(122, 104, 88), brow_h=2, brow_y=12, brow_tilt=-1, heavy_lids=True, ruddy=True, big_nose=True, folds=True,
             crows_feet=True, moustache='waxed', moustache_hair=(170, 132, 96), mouth_w=8, mouth_y=25, lip=(150, 82, 70))
    head(sk, p)
    navy = (40, 44, 58)
    shirt = (220, 214, 200)
    torso(sk, navy, 'twill')
    limbs(sk, dict(skin=p['skin'], sleeve=navy, sleeve_grain='twill', cuff=shirt, trousers=(92, 90, 88), trouser_grain='twill',
                   boots=(26, 24, 24), boot_h=3, stripes=.9))
    # The frock coat, buttoned to the throat over a white collar and a dark tie.
    for name in SIDES + ('top',):
        cloth(sk, COAT[name], navy, grain='twill', shade_=.1)
    x0, y0, x1, y1 = sk.px(*COAT['front'])
    sk.fill((x0 + 12, y0, x1 - 12, y0 + 5), shirt, noise=.03)
    sk.rect(x0 + 15, y0, x0 + 20, y0 + 7, (34, 30, 34))
    for k in range(6):
        sk.rect(x0 + 8 + k, y0 + k, x0 + 10 + k, y0 + k + 1, shade(navy, .7))
        sk.rect(x1 - 10 - k, y0 + k, x1 - 8 - k, y0 + k + 1, shade(navy, .7))
    seam(sk, x0 + 17, y0 + 7, x0 + 18, y0 + 46, .7)
    for by in range(10, 46, 7):
        button(sk, x0 + 14, by + y0, (176, 146, 80))
        button(sk, x0 + 20, by + y0, (176, 146, 80))
    # Medal ribbons on the left breast, a medal under them.
    rx = x1 - 13
    for k, colours in enumerate(((150, 40, 44), (40, 70, 140), (190, 150, 60))):
        sk.rect(rx + k * 3, y0 + 12, rx + k * 3 + 3, y0 + 15, colours)
    sk.rect(rx, y0 + 15, rx + 9, y0 + 16, shade(navy, .6))
    sk.rect(rx + 3, y0 + 16, rx + 6, y0 + 19, (150, 40, 44))
    sk.rect(rx + 3, y0 + 19, rx + 6, y0 + 22, (196, 176, 120))
    sk.dot(rx + 3, y0 + 19, (232, 220, 170))
    # A belt line and the skirts, split at the front over the trousers.
    sk.rect(x0, y0 + 44, x1, y0 + 46, shade(navy, .7))
    sk.clear(x0 + 15, y0 + 50, x1 - 15, y1)
    for x in (x0 + 14, x1 - 16):
        sk.tint(x, y0 + 46, x + 2, y1, .7)
    sk.tint(x0, y1 - 3, x1, y1, .8)
    for name in ('right', 'left', 'back'):
        x0, y0, x1, y1 = sk.px(*COAT[name])
        sk.rect(x0, y0 + 44, x1, y0 + 46, shade(navy, .7))
        sk.tint(x0, y1 - 3, x1, y1, .8)
    x0, y0, x1, y1 = sk.px(*COAT['back'])
    seam(sk, x0 + 17, y0 + 46, x0 + 18, y1, .65)
    button(sk, x0 + 12, y0 + 46, (176, 146, 80))
    button(sk, x0 + 21, y0 + 46, (176, 146, 80))
    x0, y0, x1, y1 = sk.px(*COAT['bottom'])
    sk.clear(x0, y0, x1, y1)
    return sk


def messenger():
    """The man from the mill: pale and careful, hair slicked flat under a black bowler, a black frock coat open over a black
    waistcoat, a white collar and black tie, grey striped trousers."""
    sk = Skin(905)
    p = dict(skin=(220, 186, 162), hair=(44, 36, 30), hairline=6, nape=26, side_hair=8, iris=(72, 66, 60), brow=(40, 32, 26),
             brow_h=1, brow_y=13, brow_tilt=1, pale=.16, mouth_w=6, mouth_down=True, lip=(160, 112, 102), moustache='thin',
             moustache_hair=(48, 38, 32), bags=True)
    head(sk, p)
    # The bowler on the hat layer: the crown and its band, the rest left clear.
    felt = (28, 26, 26)
    x0, y0, x1, y1 = sk.px(*HAT['top'])
    sk.fill((x0, y0, x1, y1), felt, noise=.05)
    sk.blend(x0 + 8, y0 + 8, x1 - 10, y1 - 12, (120, 120, 126), .12)
    for side in SIDES:
        x0, y0, x1, y1 = sk.px(*HAT[side])
        sk.fill((x0, y0, x1, y0 + 12), felt, noise=.05)
        sk.rect(x0, y0 + 9, x1, y0 + 12, (16, 14, 16))
        sk.blend(x0, y0 + 1, x1, y0 + 3, (110, 110, 118), .1)
    black = (32, 30, 32)
    shirt = (228, 226, 220)
    torso(sk, black, 'weave')
    x0, y0, x1, y1 = sk.px(*BODY['front'])
    sk.fill((x0 + 9, y0, x1 - 9, y0 + 12), shirt, noise=.02)
    sk.rect(x0 + 14, y0 + 2, x0 + 18, y0 + 16, (18, 18, 20))
    for k in range(4):
        sk.tint(x0 + 9 + k, y0 + 12 - k * 3, x0 + 10 + k, y0 + 14 - k * 3, .5)
        sk.tint(x1 - 10 - k, y0 + 12 - k * 3, x1 - 9 - k, y0 + 14 - k * 3, .5)
    for by in range(18, 44, 6):
        button(sk, x0 + 15, y0 + by, (60, 58, 60))
    for k in range(7):
        sk.dot(x0 + 6 + k, y0 + 30 + round(1.5 * math.sin(k / 6 * math.pi)), (180, 170, 150))
    trousers = (96, 96, 100)
    limbs(sk, dict(skin=shade(p['skin'], .96), sleeve=black, cuff=shirt, trousers=trousers, trouser_grain='twill',
                   boots=(20, 20, 22), boot_h=2, stripes=.82))
    for name in SIDES:
        x0, y0, x1, y1 = sk.px(*LEG[name])
        sk.blend(x0 + 3, y1 - 7, x1 - 3, y1 - 5, (255, 255, 255), .12)
    # The frock coat, open over the waistcoat, its skirts to the knee.
    for name in SIDES + ('top',):
        cloth(sk, COAT[name], black, grain='weave', shade_=.1)
    x0, y0, x1, y1 = sk.px(*COAT['front'])
    sk.clear(x0 + 11, y0, x1 - 11, y1)
    for k in range(18):
        w = max(0, 6 - k // 3)
        sk.rect(x0 + 11, y0 + k, x0 + 11 + w, y0 + k + 1, (20, 20, 22))
        sk.rect(x1 - 11 - w, y0 + k, x1 - 11, y0 + k + 1, (20, 20, 22))
    for x in (x0 + 10, x1 - 11):
        sk.tint(x, y0, x + 1, y1, .6)
    sk.tint(x0, y1 - 3, x1, y1, .8)
    for name in ('right', 'left', 'back'):
        x0, y0, x1, y1 = sk.px(*COAT[name])
        sk.tint(x0, y1 - 3, x1, y1, .8)
    x0, y0, x1, y1 = sk.px(*COAT['back'])
    seam(sk, x0 + 17, y0 + 46, x0 + 18, y1, .5)
    button(sk, x0 + 12, y0 + 44, (60, 58, 60))
    button(sk, x0 + 21, y0 + 44, (60, 58, 60))
    x0, y0, x1, y1 = sk.px(*COAT['bottom'])
    sk.clear(x0, y0, x1, y1)
    return sk


CAST = {'walter': walter, 'ruth': ruth, 'will': will, 'sergeant': sergeant, 'messenger': messenger}


# ------------------------------------------------------------------------------------------------ the paw
OUTLINE = (40, 28, 22)
DARK = (74, 52, 38)
MID = (108, 80, 58)
LIGHT = (140, 108, 78)
HIGH = (170, 140, 104)
NAIL = (58, 46, 40)
FUR = (128, 120, 108)
FUR_DARK = (88, 80, 72)
CORD = (136, 44, 38)


def paw_art(fingers):
    """The dried paw at 16 x 16, raised as if to wish. Each wish curls one finger down onto the palm."""
    grid = {}

    def put(x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            grid[(x, y)] = c

    # The palm: a shrunken pad, lit from the upper left.
    for y in range(8, 13):
        for x in range(4, 12):
            dx, dy = (x - 7.6) / 4.2, (y - 10.2) / 2.9
            if dx * dx + dy * dy <= 1:
                c = MID
                if dx < -.25 and dy < .2:
                    c = LIGHT
                if dx > .45 or dy > .55:
                    c = DARK
                put(x, y, c)
    put(5, 9, HIGH)
    put(8, 11, DARK)
    put(9, 10, DARK)
    # The thumb, folded in against the side.
    for (x, y, c) in ((3, 9, LIGHT), (3, 10, MID), (2, 10, MID), (2, 11, DARK), (3, 11, MID), (4, 12, DARK), (2, 9, NAIL)):
        put(x, y, c)
    # The wrist, the fur where it was cut, the holy man's cord.
    for y in range(12, 16):
        for x in range(6, 10):
            put(x, y, MID if x < 8 else DARK)
    for x in range(5, 11):
        put(x, 13, CORD if x % 2 else shade(CORD, .7))
    for (x, y) in ((5, 14), (6, 15), (8, 15), (10, 14), (9, 15)):
        put(x, y, FUR)
    for (x, y) in ((7, 15), (10, 15), (5, 15)):
        put(x, y, FUR_DARK)
    put(7, 12, LIGHT)
    # The three fingers that grant: long, knuckled, nailed. Those spent are bent down over the palm.
    columns = (4, 7, 10)
    tops = (3, 1, 2)
    for i, (x, top) in enumerate(zip(columns, tops)):
        if i < fingers:
            for y in range(top, 9):
                knuckle = y in (top + 2, top + 4)
                put(x, y, HIGH if knuckle else LIGHT)
                put(x + 1, y, MID if not knuckle else LIGHT)
            put(x, top, NAIL)
            put(x + 1, top, NAIL)
            put(x + 1, top + 1, DARK)
        else:
            # Folded: a knuckle hump at the palm's top edge and the nail pointing down onto the palm.
            put(x, 7, LIGHT)
            put(x + 1, 7, MID)
            put(x, 8, MID)
            put(x + 1, 8, DARK)
            put(x, 9, DARK)
            put(x + 1, 9, NAIL)
    # One dark outline around everything, as vanilla items have.
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for (x, y), c in grid.items():
        img.putpixel((x, y), c + (255,))
    for (x, y) in list(grid):
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in grid and img.getpixel((nx, ny))[3] == 0:
                img.putpixel((nx, ny), OUTLINE + (255,))
    return img


# ------------------------------------------------------------------------------------------------ previews and output
def crop(img, box):
    x0, y0, x1, y1 = box
    return img.crop((math.floor(x0 * S), math.floor(y0 * S), math.ceil(x1 * S), math.ceil(y1 * S)))


def figure(img, side, coat, gown):
    """A flat front or back view assembled from the faces, to judge the drawing by."""
    out = Image.new('RGBA', (16 * S + 8, 34 * S), (0, 0, 0, 0))
    f = side
    other = 'back' if side == 'front' else 'front'
    ox = 4

    def paste(part, box, x, y, mirror=False):
        piece = crop(img, part[box])
        if mirror:
            piece = piece.transpose(Image.FLIP_LEFT_RIGHT)
        out.alpha_composite(piece, (ox + round(x * S), round(y * S)))

    paste(HEAD, f, 4, 0)
    hat = crop(img, HAT[f]).resize((9 * S, 9 * S), Image.NEAREST)
    out.alpha_composite(hat, (ox + round(3.5 * S), round(-.5 * S) if False else 0))
    paste(BODY, f, 4, 8)
    paste(ARM, f, 0 if side == 'front' else 12, 8)
    paste(ARM, f, 12 if side == 'front' else 0, 8, mirror=True)
    paste(LEG, f, 4 if side == 'front' else 8, 20)
    paste(LEG, f, 8 if side == 'front' else 4, 20, mirror=True)
    if coat:
        piece = crop(img, COAT[f]).resize((round(8.8 * S), 17 * S), Image.NEAREST)
        out.alpha_composite(piece, (ox + round(3.6 * S), 9 * S))
    if gown:
        piece = crop(img, GOWN[f]).resize((round(10.4 * S), 14 * S), Image.NEAREST)
        out.alpha_composite(piece, (ox + round(2.8 * S), 17 * S))
    return out


def preview(folder):
    folder.mkdir(parents=True, exist_ok=True)
    sheet = Image.new('RGBA', (5 * 2 * (16 * S + 16) + 16, 34 * S + 16), (58, 54, 60, 255))
    x = 8
    for name in CAST:
        img = Image.open(ASSETS / f'textures/entity/literary_paw_{name}.png').convert('RGBA')
        coat, gown = name in ('walter', 'sergeant', 'messenger'), name == 'ruth'
        for side in ('front', 'back'):
            sheet.alpha_composite(figure(img, side, coat, gown), (x, 8))
            x += 16 * S + 16
    sheet.resize((sheet.width * 2, sheet.height * 2), Image.NEAREST).save(folder / 'cottage_cast_preview.png')
    paws = Image.new('RGBA', (4 * 20 + 4, 20), (86, 82, 90, 255))
    for i, suffix in enumerate(('', '_2', '_1', '_0')):
        paws.alpha_composite(Image.open(ASSETS / f'textures/item/withered_paw{suffix}.png').convert('RGBA'), (4 + i * 20, 2))
    paws.resize((paws.width * 12, paws.height * 12), Image.NEAREST).save(folder / 'withered_paw_preview.png')
    for name in CAST:
        img = Image.open(ASSETS / f'textures/entity/literary_paw_{name}.png').convert('RGBA')
        bg = Image.new('RGBA', img.size, (60, 60, 70, 255))
        bg.alpha_composite(img)
        bg.resize((W * 2, H * 2), Image.NEAREST).save(folder / f'atlas_{name}.png')


def main():
    entity = ASSETS / 'textures/entity'
    item = ASSETS / 'textures/item'
    record = {'version': '0.4.81', 'note': 'Drawn in code by tools/draw_three_fingers_art.py.', 'skins': [], 'items': []}
    for name, draw in CAST.items():
        target = entity / f'literary_paw_{name}.png'
        img = draw().image()
        img.save(target, optimize=True)
        raw = target.read_bytes()
        record['skins'].append({'name': name, 'path': str(target.relative_to(ROOT)), 'size': list(img.size), 'logical_uv': [128, 64],
                                'sha256': hashlib.sha256(raw).hexdigest()})
    for fingers, suffix in ((3, ''), (2, '_2'), (1, '_1'), (0, '_0')):
        target = item / f'withered_paw{suffix}.png'
        paw_art(fingers).save(target, optimize=True)
        record['items'].append({'fingers': fingers, 'path': str(target.relative_to(ROOT)), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()})
    (ROOT / 'art/inventory/three_fingers_0481.json').write_text(json.dumps(record, indent=1) + '\n')
    if '--preview' in sys.argv:
        preview(Path(sys.argv[sys.argv.index('--preview') + 1]))
    print('drew five cottage skins and four paw images')


if __name__ == '__main__':
    main()
