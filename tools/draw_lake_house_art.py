"""The house at the bottom of the lake: art drawn in code.

The canoe (LakeHouseCanoeModel, 128 x 64 UV) in green paint with brown trim, three times: fresh, chipped by the culvert
and scraped down to the wood. The UV islands below must match the model's texOffs and box sizes exactly.

Nothing else writes these files. Run this tool again only to redraw.

Usage: python3 tools/draw_lake_house_art.py [--preview DIR]
"""
from pathlib import Path
import sys

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/the_oldest_house'
W, H = 128, 64

GREEN = (58, 102, 62)
GREEN_DARK = (40, 74, 46)
TRIM = (104, 66, 38)
TRIM_DARK = (78, 48, 28)
WOOD = (196, 160, 110)
WOOD_DARK = (150, 116, 76)
RIB = (128, 94, 58)
BARE = (214, 186, 140)
STAIN = (70, 78, 52)


class Canvas:
    def __init__(self, seed):
        self.a = np.zeros((H, W, 4), dtype=np.float32)
        self.rng = np.random.default_rng(seed)

    def fill(self, x0, y0, x1, y1, color, noise=.05):
        if x1 <= x0 or y1 <= y0:
            return
        h, w = y1 - y0, x1 - x0
        n = 1 + self.rng.normal(0, noise, (h, w, 1))
        self.a[y0:y1, x0:x1, :3] = np.clip(np.array(color, dtype=np.float32) / 255 * n, 0, 1)
        self.a[y0:y1, x0:x1, 3] = 1

    def tint(self, x, y, color, amount=1.0):
        if 0 <= x < W and 0 <= y < H and self.a[y, x, 3] > 0:
            c = np.array(color, dtype=np.float32) / 255
            self.a[y, x, :3] = self.a[y, x, :3] * (1 - amount) + c * amount

    def save(self, path):
        path.parent.mkdir(parents=True, exist_ok=True)
        Image.fromarray((self.a * 255).round().astype(np.uint8), 'RGBA').save(path, optimize=True)


# ------------------------------------------------------------------------------------------------ surfaces
def paint(c, x0, y0, x1, y1, waterline=False):
    """Green paint over a hull's outer face: faint plank seams along its length, a darker waterline stain."""
    c.fill(x0, y0, x1, y1, GREEN, .06)
    for y in range(y0, y1):
        if (y - y0) % 3 == 2:
            for x in range(x0, x1):
                c.tint(x, y, GREEN_DARK, .35)
    if waterline:
        for x in range(x0, x1):
            c.tint(x, y1 - 1, STAIN, .55)
            c.tint(x, y1 - 2, STAIN, .25)


def interior(c, x0, y0, x1, y1, ribs_along_u=True):
    """The wood inside the hull: planks with cross ribs every three texels."""
    c.fill(x0, y0, x1, y1, WOOD, .05)
    for y in range(y0, y1):
        for x in range(x0, x1):
            if (y - y0) % 2 == 1:
                c.tint(x, y, WOOD_DARK, .25)
            if ribs_along_u and (x - x0) % 3 == 1:
                c.tint(x, y, RIB, .7)


def trim(c, x0, y0, x1, y1):
    c.fill(x0, y0, x1, y1, TRIM, .06)
    for x in range(x0, x1):
        c.tint(x, y1 - 1, TRIM_DARK, .4)


def plain(c, x0, y0, x1, y1, color, noise=.05):
    c.fill(x0, y0, x1, y1, color, noise)


# ------------------------------------------------------------------------------------------------ the canoe
# Each island is (u, v, w, h, d): a box net with its top at (u+d, v), bottom at (u+d+w, v), and the side row at v+d:
# east (u, w=d), north/minimum Z (u+d, w), west (u+d+w, w=d), south/maximum Z (u+2d+w, w).
def net(u, v, w, h, d):
    return {
        'top': (u + d, v, u + d + w, v + d),
        'bottom': (u + d + w, v, u + d + 2 * w, v + d),
        'east': (u, v + d, u + d, v + d + h),
        'minz': (u + d, v + d, u + d + w, v + d + h),
        'west': (u + d + w, v + d, u + 2 * d + w, v + d + h),
        'maxz': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


def hull(c):
    # The bottom and its tapered tips: wood inside, paint underneath and round the edges.
    for u, v, w, h, d in ((0, 0, 28, 1, 10), (78, 0, 2, 1, 7), (96, 0, 2, 1, 3)):
        n = net(u, v, w, h, d)
        interior(c, *n['top'])
        paint(c, *n['bottom'])
        for side in ('east', 'minz', 'west', 'maxz'):
            paint(c, *n[side])
    # The long sides and the end panels. On the -Z side the minimum-Z face is outside; on the +Z side, the maximum.
    for u, v, w, outer, inner in ((0, 11, 28, 'minz', 'maxz'), (0, 20, 28, 'maxz', 'minz'),
                                  (60, 11, 8, 'minz', 'maxz'), (60, 20, 8, 'maxz', 'minz')):
        n = net(u, v, w, 8, 1)
        paint(c, *n[outer], waterline=True)
        interior(c, *n[inner])
        trim(c, *n['top'])
        paint(c, *n['bottom'])
        plain(c, *n['east'], WOOD_DARK)
        plain(c, *n['west'], WOOD_DARK)
    # Gunwales, long and short.
    for u, v, w in ((0, 29, 28), (62, 29, 8)):
        n = net(u, v, w, 1, 2)
        for face in n.values():
            trim(c, *face)
    # The stems: brown, darker at the tip.
    n = net(80, 11, 2, 11, 1)
    for face in n.values():
        trim(c, *face)
    for x in range(80, 86):
        for y in range(12, 14):
            c.tint(x, y, TRIM_DARK, .5)
    # The seats and the yoke: pale ash.
    for u, v, w, d in ((88, 11, 3, 10), (88, 22, 1, 10)):
        n = net(u, v, w, 1, d)
        for face in n.values():
            plain(c, *face, BARE, .04)
        x0, y0, x1, y1 = n['top']
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (y - y0) % 4 == 0:
                    c.tint(x, y, WOOD_DARK, .3)


def paddles(c):
    # Shaft and T-grip in pale wood; the blade darker, its edge worn.
    for face in net(0, 34, 1, 1, 14).values():
        plain(c, *face, BARE, .04)
    for face in net(32, 34, 3, 1, 1).values():
        plain(c, *face, WOOD, .04)
    n = net(40, 34, 1, 5, 6)
    for face in n.values():
        plain(c, *face, WOOD, .05)
    for side in ('east', 'west'):
        x0, y0, x1, y1 = n[side]
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (x - x0) >= (x1 - x0) - 2:
                    c.tint(x, y, WOOD_DARK, .5)


# ------------------------------------------------------------------------------------------------ wear
def is_paint(c, x, y):
    r, g, b, a = c.a[y, x]
    return a > 0 and g > r * 1.25 and g > b * 1.2


def chip(c, rng, count, size):
    """Knocks paint off the green: small irregular flakes showing the pale wood under it."""
    painted = [(x, y) for y in range(H) for x in range(W) if is_paint(c, x, y)]
    for _ in range(count):
        x, y = painted[rng.integers(len(painted))]
        for _ in range(rng.integers(1, size + 1)):
            if is_paint(c, x, y):
                c.tint(x, y, BARE, .85)
            x += int(rng.integers(-1, 2))
            y += int(rng.integers(-1, 2)) if rng.random() < .4 else 0
            x, y = max(0, min(W - 1, x)), max(0, min(H - 1, y))


def scrape(c, rng, count):
    """Long scrapes along the outside of the hull where the culvert's walls ran against it."""
    for u, v, w, outer in ((0, 11, 28, 'minz'), (0, 20, 28, 'maxz'), (60, 11, 8, 'minz'), (60, 20, 8, 'maxz')):
        x0, y0, x1, y1 = net(u, v, w, 8, 1)[outer]
        for _ in range(max(1, count * w // 28)):
            y = int(rng.integers(y0 + 1, y1 - 1))
            start = int(rng.integers(x0, max(x0 + 1, x1 - 4)))
            for x in range(start, min(x1, start + int(rng.integers(4, 12)))):
                c.tint(x, y, BARE, .8)
                if rng.random() < .3:
                    c.tint(x, y + (1 if rng.random() < .5 else -1), BARE, .5)


def canoe(wear):
    c = Canvas(95)
    hull(c)
    paddles(c)
    rng = np.random.default_rng(9501 + wear)
    if wear >= 1:
        chip(c, rng, 18, 3)
    if wear >= 2:
        chip(c, rng, 20, 4)
        scrape(c, rng, 5)
    return c


def main():
    preview = None
    if '--preview' in sys.argv:
        preview = Path(sys.argv[sys.argv.index('--preview') + 1])
    for wear, name in enumerate(('lake_house_canoe', 'lake_house_canoe_chipped', 'lake_house_canoe_scraped')):
        c = canoe(wear)
        c.save(ASSETS / 'textures/entity' / f'{name}.png')
        if preview:
            preview.mkdir(parents=True, exist_ok=True)
            Image.fromarray((c.a * 255).round().astype(np.uint8), 'RGBA').resize((W * 6, H * 6), Image.NEAREST).save(preview / f'{name}.png')
        print(f'wrote textures/entity/{name}.png')


if __name__ == '__main__':
    main()
