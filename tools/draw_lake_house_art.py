"""The house at the bottom of the lake: art drawn in code.

- The canoe (LakeHouseCanoeModel, 128 x 64 UV) in green paint with brown trim, three times: fresh, chipped by the culvert
  and scraped down to the wood. The UV islands below must match the model's texOffs and box sizes exactly.
- The lent gear: the gold diving helmet's shell, window and collar (its item model is worn on the head), the air tanks'
  icon and the tanks on a reader's back (LakeHouseGearClient, 32 x 32 UV), the flashlight, and the round window the helmet
  leaves of the first-person view.
- The compressor on the raft: its tank, engine, skid and gauge.
- In the house: the porcelain beaver pepper shaker, and the still life in the lounge (two plums and a table edge) with the
  face it becomes.

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


# ------------------------------------------------------------------------------------------------ the lent gear
GOLD = (214, 168, 58)
GOLD_DARK = (150, 106, 34)
GOLD_LIGHT = (246, 214, 120)
BRONZE = (120, 84, 40)
STEEL = (170, 176, 180)
STEEL_DARK = (96, 102, 108)
GLASS = (34, 58, 62)
YELLOW = (226, 188, 44)
YELLOW_DARK = (168, 128, 24)
STRAP = (40, 42, 34)
RED = (162, 44, 34)
RED_DARK = (110, 28, 24)
IRON = (62, 60, 58)


def tile(size=16, seed=1):
    c = Canvas(seed)
    c.a = np.zeros((size, size, 4), dtype=np.float32)
    return c


def put(c, x, y, color, amount=1.0):
    h, w = c.a.shape[:2]
    if 0 <= x < w and 0 <= y < h:
        col = np.array(color, dtype=np.float32) / 255
        if c.a[y, x, 3] == 0:
            c.a[y, x, :3] = col
            c.a[y, x, 3] = 1
        else:
            c.a[y, x, :3] = c.a[y, x, :3] * (1 - amount) + col * amount


def block(c, x0, y0, x1, y1, color, noise=.05):
    h, w = c.a.shape[:2]
    n = 1 + c.rng.normal(0, noise, (y1 - y0, x1 - x0, 1))
    c.a[y0:y1, x0:x1, :3] = np.clip(np.array(color, dtype=np.float32) / 255 * n, 0, 1)
    c.a[y0:y1, x0:x1, 3] = 1


def save_tile(c, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray((c.a * 255).round().astype(np.uint8), 'RGBA').save(path, optimize=True)


def helmet_shell():
    """Spun brass gone dull in the lake: a soft highlight down one side, seams, and a ring of rivets near the edge."""
    c = tile(seed=11)
    block(c, 0, 0, 16, 16, GOLD, .06)
    for y in range(16):
        for x in range(16):
            shade = .18 * (x - 7.5) / 7.5
            put(c, x, y, GOLD_DARK if shade > 0 else GOLD_LIGHT, abs(shade))
    for i in range(16):
        put(c, i, 0, GOLD_DARK, .5); put(c, i, 15, GOLD_DARK, .5); put(c, 0, i, GOLD_DARK, .5); put(c, 15, i, GOLD_DARK, .5)
    for i in range(2, 14, 3):
        for x, y in ((i, 1), (i, 14), (1, i), (14, i)):
            put(c, x, y, GOLD_LIGHT, .8)
    for x in range(16):
        if c.rng.random() < .3:
            put(c, x, int(c.rng.integers(3, 13)), (80, 96, 70), .35)
    return c


def helmet_port():
    """The front window: green-black glass behind a brass rim, with a cross guard bolted over it."""
    c = tile(seed=12)
    block(c, 0, 0, 16, 16, GOLD_DARK, .05)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 6.2:
                put(c, x, y, GLASS, 1)
                if x + y < 11 and d > 2.5:
                    put(c, x, y, (120, 150, 150), .25)
            elif d < 7.6:
                put(c, x, y, GOLD_LIGHT if (x + y) % 5 else GOLD, .7)
    for i in range(3, 13):
        put(c, i, 7, BRONZE, .9); put(c, i, 8, BRONZE, .6); put(c, 7, i, BRONZE, .9); put(c, 8, i, BRONZE, .6)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        put(c, x, y, GOLD_LIGHT, 1)
    return c


def helmet_collar():
    """The breastplate collar: darker bronze with heavy bolts where the helmet seats."""
    c = tile(seed=13)
    block(c, 0, 0, 16, 16, BRONZE, .07)
    for x in range(16):
        put(c, x, 0, GOLD, .6); put(c, x, 15, (60, 40, 20), .6)
    for x in range(1, 16, 4):
        for y in (5, 10):
            put(c, x, y, GOLD_LIGHT, .9); put(c, x + 1, y, GOLD, .6)
    return c


def tanks_icon():
    """Two yellow tanks side by side with steel valves and a black strap."""
    c = tile(seed=14)
    for x0 in (3, 8):
        for y in range(4, 15):
            for x in range(x0, x0 + 5):
                edge = x in (x0, x0 + 4)
                put(c, x, y, YELLOW_DARK if edge else YELLOW, 1)
            put(c, x0 + 1, y, (250, 230, 140), .6)
        for x in range(x0 + 1, x0 + 4):
            put(c, x, 3, YELLOW_DARK, 1)
        put(c, x0 + 2, 2, STEEL, 1); put(c, x0 + 2, 1, STEEL_DARK, 1); put(c, x0 + 1, 1, STEEL, 1); put(c, x0 + 3, 1, STEEL, 1)
    for x in range(2, 14):
        put(c, x, 7, STRAP, 1); put(c, x, 11, STRAP, 1)
    put(c, 7, 7, STEEL, 1); put(c, 7, 11, STEEL, 1)
    return c


def flashlight_icon():
    """A heavy rubber flashlight held head-up and to the right, like a tool: a thick black body, a chrome head, a warm lens."""
    c = tile(seed=15)
    for i in range(9):
        x, y = 2 + i, 13 - i
        for dx, dy in ((0, 0), (1, 0), (0, -1), (1, -1), (-1, 0), (0, 1)):
            put(c, x + dx, y + dy, (30, 34, 30), 1)
        put(c, x, y - 1, (64, 70, 60), 1)
    for x, y in ((5, 9), (6, 8)):
        put(c, x, y, (120, 124, 110), 1)
    for x in range(10, 15):
        for y in range(1, 6):
            if (x - 12) ** 2 + (y - 3) ** 2 <= 5:
                put(c, x, y, STEEL if (x + y) % 2 else STEEL_DARK, 1)
    for x, y in ((12, 2), (13, 2), (13, 3), (12, 3), (14, 2)):
        put(c, x, y, (255, 236, 160), 1)
    return c


def tanks_texture():
    """The tanks on a reader's back (LakeHouseGearClient.tanksLayer, 32 x 32): tank net at (0, 0), valve at (12, 0),
    strap at (0, 12)."""
    c = Canvas(16)
    c.a = np.zeros((32, 32, 4), dtype=np.float32)
    block(c, 3, 0, 9, 3, YELLOW_DARK, .04)                 # tank top and bottom
    block(c, 0, 3, 12, 12, YELLOW, .05)                    # tank sides
    for y in range(3, 12):
        put(c, 1, y, (250, 230, 140), .6); put(c, 7, y, (250, 230, 140), .6); put(c, 5, y, YELLOW_DARK, .5); put(c, 11, y, YELLOW_DARK, .5)
    for x in range(12):
        put(c, x, 4, (40, 40, 40), .5); put(c, x, 10, YELLOW_DARK, .6)
    block(c, 12, 0, 16, 3, STEEL, .05)                     # valves
    block(c, 0, 12, 26, 18, STRAP, .06)                    # strap
    for x in range(0, 26, 4):
        put(c, x, 14, (70, 72, 60), .6)
    return c


def compressor_tiles():
    """The compressor: a red tank streaked with rust, a grey cast engine, an iron skid and a pressure gauge."""
    tank = tile(seed=21)
    block(tank, 0, 0, 16, 16, RED, .07)
    for x in range(16):
        put(tank, x, 0, RED_DARK, .6); put(tank, x, 15, RED_DARK, .6); put(tank, x, 3, (210, 90, 70), .35)
    for _ in range(14):
        x, y = int(tank.rng.integers(16)), int(tank.rng.integers(5, 16))
        for k in range(int(tank.rng.integers(1, 4))):
            put(tank, x, min(15, y + k), (120, 62, 30), .6)
    engine = tile(seed=22)
    block(engine, 0, 0, 16, 16, (120, 124, 120), .08)
    for y in range(2, 16, 3):
        for x in range(16):
            put(engine, x, y, (80, 84, 82), .55)
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        put(engine, x, y, (190, 190, 180), .9)
    base = tile(seed=23)
    block(base, 0, 0, 16, 16, IRON, .08)
    for x in range(16):
        put(base, x, 0, (90, 86, 80), .6)
    gauge = tile(seed=24)
    block(gauge, 0, 0, 16, 16, (120, 124, 120), .06)
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** .5
            if d < 5.5:
                put(gauge, x, y, (232, 226, 206), 1)
            elif d < 6.8:
                put(gauge, x, y, STEEL_DARK, 1)
    for i in range(5):
        put(gauge, 7 + i // 2, 8 - i, (30, 30, 30), 1)
    for x in range(10, 13):
        put(gauge, x, 4, (190, 40, 30), 1)
    return {'tank': tank, 'engine': engine, 'base': base, 'gauge': gauge}


def porthole():
    """The first-person window of the helmet, 256 square: dark beyond a brass rim, clear glass inside with a faint streak."""
    n = 256
    a = np.zeros((n, n, 4), dtype=np.float32)
    yy, xx = np.mgrid[0:n, 0:n]
    d = np.hypot(xx - (n - 1) / 2, yy - (n - 1) / 2) / (n / 2)
    outside = d >= .92
    rim = (d >= .84) & ~outside
    a[outside] = [10 / 255, 9 / 255, 6 / 255, 1]
    shade = np.clip((d - .84) / .08, 0, 1)
    a[rim, 0] = (GOLD_DARK[0] + (GOLD[0] - GOLD_DARK[0]) * (1 - shade[rim])) / 255
    a[rim, 1] = (GOLD_DARK[1] + (GOLD[1] - GOLD_DARK[1]) * (1 - shade[rim])) / 255
    a[rim, 2] = (GOLD_DARK[2] + (GOLD[2] - GOLD_DARK[2]) * (1 - shade[rim])) / 255
    a[rim, 3] = 1
    # Bolts round the rim.
    for k in range(12):
        ang = k * np.pi / 6
        bx, by = (n - 1) / 2 + np.cos(ang) * n / 2 * .88, (n - 1) / 2 + np.sin(ang) * n / 2 * .88
        bolt = np.hypot(xx - bx, yy - by) < 4.5
        a[bolt] = [GOLD_LIGHT[0] / 255, GOLD_LIGHT[1] / 255, GOLD_LIGHT[2] / 255, 1]
    inside = d < .84
    # The glass darkens toward its edge and carries one pale streak.
    a[inside, 0:3] = 0.02
    a[inside, 3] = np.clip((d[inside] - .55) / .29, 0, 1) ** 2 * .7
    streak = inside & (np.abs((xx - yy) / n + .35) < .025) & (d > .3)
    a[streak, 0:3] = .85
    a[streak, 3] = .08
    return a


# ------------------------------------------------------------------------------------------------ the house
def beaver():
    """White porcelain with a brown glaze over its back, painted blue eyes and a ring of pepper holes."""
    c = tile(seed=31)
    block(c, 0, 0, 16, 16, (238, 234, 224), .03)
    for y in range(0, 16):
        for x in range(0, 16):
            if (x - 8) ** 2 / 40 + (y - 11) ** 2 / 18 < 1:
                put(c, x, y, (126, 84, 52), .8)
    for x, y in ((5, 4), (10, 4)):
        put(c, x, y, (40, 70, 150), 1)
    for x, y in ((7, 1), (8, 1), (9, 2), (6, 2)):
        put(c, x, y, (60, 50, 40), 1)
    for x in range(16):
        put(c, x, 15, (200, 196, 186), .5)
    return c


def painting(face):
    """The lounge's still life, 32 square: two plums on a table edge, dim. As the face, the same picture with something
    pale behind it, the plums its eyes and the table's edge its mouth."""
    n = 32
    rng = np.random.default_rng(41)
    a = np.zeros((n, n, 4), dtype=np.float32)
    yy, xx = np.mgrid[0:n, 0:n]
    a[..., 0:3] = np.array([30, 26, 22]) / 255 * (1 + rng.normal(0, .06, (n, n, 1)))
    a[..., 3] = 1
    if face:
        skin = ((xx - 15.5) / 11) ** 2 + ((yy - 15) / 14) ** 2 < 1
        a[skin, 0:3] = np.array([196, 186, 168]) / 255 * (1 + rng.normal(0, .04, (int(skin.sum()), 1)))
        shade = skin & (xx > 21)
        a[shade, 0:3] *= .82
    table = yy >= 24
    a[table, 0:3] = np.array([92, 58, 34]) / 255 * (1 + rng.normal(0, .05, (int(table.sum()), 1)))
    a[yy == 24, 0:3] = np.array([140, 96, 60]) / 255
    if face:
        mouth = (yy >= 23) & (yy <= 25) & (np.abs(xx - 15.5) < 7)
        a[mouth, 0:3] = np.array([60, 34, 30]) / 255
    for cx, cy in ((11, 17), (20, 18)):
        plum = ((xx - cx) / 4.2) ** 2 + ((yy - cy) / 4.6) ** 2 < 1
        a[plum, 0:3] = np.array([78, 34, 82]) / 255 if not face else np.array([24, 18, 26]) / 255
        a[plum & (xx < cx - 1) & (yy < cy - 1), 0:3] = np.array([150, 110, 160]) / 255 if not face else np.array([210, 210, 200]) / 255
    a[0, :, 0:3] = a[-1, :, 0:3] = a[:, 0, 0:3] = a[:, -1, 0:3] = np.array([70, 52, 30]) / 255
    return np.clip(a, 0, 1)


def gear():
    out = ASSETS / 'textures'
    save_tile(helmet_shell(), out / 'item/lake_house_helmet.png')
    save_tile(helmet_port(), out / 'item/lake_house_helmet_port.png')
    save_tile(helmet_collar(), out / 'item/lake_house_helmet_collar.png')
    save_tile(tanks_icon(), out / 'item/lake_house_tanks.png')
    save_tile(flashlight_icon(), out / 'item/lake_house_flashlight.png')
    save_tile(tanks_texture(), out / 'entity/lake_house_tanks.png')
    for name, c in compressor_tiles().items():
        save_tile(c, out / f'block/lake_house_compressor_{name}.png')
    path = out / 'misc/lake_house_porthole.png'
    path.parent.mkdir(parents=True, exist_ok=True)
    Image.fromarray((porthole() * 255).round().astype(np.uint8), 'RGBA').save(path, optimize=True)
    save_tile(beaver(), out / 'block/lake_house_beaver.png')
    for name, face in (('lake_house_still_life', False), ('lake_house_face', True)):
        path = out / f'painting/{name}.png'
        path.parent.mkdir(parents=True, exist_ok=True)
        Image.fromarray((painting(face) * 255).round().astype(np.uint8), 'RGBA').save(path, optimize=True)
    print('wrote the helmet, tanks, flashlight, compressor, porthole, beaver and the still life')


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
    gear()


if __name__ == '__main__':
    main()
